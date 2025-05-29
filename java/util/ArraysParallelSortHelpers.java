/*
 * 版权所有 (c) 2012, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package java.util;

import java.util.concurrent.RecursiveAction;
import java.util.concurrent.CountedCompleter;

/**
 * Arrays.parallelSort 方法中的并行排序方法的帮助工具。
 *
 * 对于每种基本类型，加上 Object，我们定义一个静态类来
 * 包含该类型的 Sorter 和 Merger 实现：
 *
 * Sorter 类主要基于 CilkSort
 * <A href="http://supertech.lcs.mit.edu/cilk/"> Cilk</A>:
 * 基本算法：
 * 如果数组大小很小，就直接使用顺序快速排序（通过 Arrays.sort）
 *         否则：
 *         1. 将数组分成两半。
 *         2. 对每半部分，
 *             a. 将半部分再分成两半（即，四分之一），
 *             b. 排序四分之一
 *             c. 将它们合并在一起
 *         3. 将两个半部分合并在一起。
 *
 * 将数组分成四分之一的一个原因是这保证了
 * 最终的排序是在主数组中，而不是在工作区数组中。
 * （在每个子排序步骤中，工作区和主数组的角色会互换。） 叶级
 * 排序使用相关的顺序排序。
 *
 * Merger 类执行 Sorter 的合并。它们的结构
 * 使得如果底层排序是稳定的（如 TimSort），那么整个排序也是稳定的。 如果足够大，它们会将
 * 两个分区中较大的一个分成两半，通过二分查找找到较小分区中
 * 小于较大分区第二半部分开始位置的最大点；然后并行合并
 * 两个分区。部分是为了确保任务按保持稳定性的顺序触发，当前的 CountedCompleter 设计
 * 要求一些小任务作为触发完成任务的占位符。这些类（EmptyCompleter 和 Relay）不需要
 * 跟踪数组，并且永远不会被分叉，所以不需要保持任何任务状态。
 *
 * 原始类版本（FJByte... FJDouble）除了类型声明外，彼此完全相同。
 *
 * 基础的顺序排序依赖于非公开版本的 TimSort，
 * ComparableTimSort，和 DualPivotQuicksort 排序方法，这些方法接受
 * 我们已经分配的临时工作区数组切片，因此避免了重复分配。（除了 DualPivotQuicksort byte[]
 * 排序，它从不使用工作区数组。）
 */
/*package*/ class ArraysParallelSortHelpers {

    /*
     * 风格说明：任务类有很多参数，这些参数
     * 存储为任务字段，复制到本地变量，并在
     * compute() 方法中使用。我们将这些参数打包到尽可能少的行中，
     * 并在主循环之前提升它们之间的一致性检查，以减少干扰。
     */

    /**
     * Sorters 的占位符任务，用于最低
     * 四分位任务，不需要维护数组状态。
     */
    static final class EmptyCompleter extends CountedCompleter<Void> {
        static final long serialVersionUID = 2446542900576103244L;
        EmptyCompleter(CountedCompleter<?> p) { super(p); }
        public final void compute() { }
    }

    /**
     * 两个合并的二次合并触发器
     */
    static final class Relay extends CountedCompleter<Void> {
        static final long serialVersionUID = 2446542900576103244L;
        final CountedCompleter<?> task;
        Relay(CountedCompleter<?> task) {
            super(null, 1);
            this.task = task;
        }
        public final void compute() { }
        public final void onCompletion(CountedCompleter<?> t) {
            task.compute();
        }
    }

    /** Object + Comparator 支持类 */
    static final class FJObject {
        static final class Sorter<T> extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final T[] a, w;
            final int base, size, wbase, gran;
            Comparator<? super T> comparator;
            Sorter(CountedCompleter<?> par, T[] a, T[] w, int base, int size,
                   int wbase, int gran,
                   Comparator<? super T> comparator) {
                super(par);
                this.a = a; this.w = w; this.base = base; this.size = size;
                this.wbase = wbase; this.gran = gran;
                this.comparator = comparator;
            }
            public final void compute() {
                CountedCompleter<?> s = this;
                Comparator<? super T> c = this.comparator;
                T[] a = this.a, w = this.w; // 本地化所有参数
                int b = this.base, n = this.size, wb = this.wbase, g = this.gran;
                while (n > g) {
                    int h = n >>> 1, q = h >>> 1, u = h + q; // 四分位
                    Relay fc = new Relay(new Merger<T>(s, w, a, wb, h,
                                                       wb+h, n-h, b, g, c));
                    Relay rc = new Relay(new Merger<T>(fc, a, w, b+h, q,
                                                       b+u, n-u, wb+h, g, c));
                    new Sorter<T>(rc, a, w, b+u, n-u, wb+u, g, c).fork();
                    new Sorter<T>(rc, a, w, b+h, q, wb+h, g, c).fork();;
                    Relay bc = new Relay(new Merger<T>(fc, a, w, b, q,
                                                       b+q, h-q, wb, g, c));
                    new Sorter<T>(bc, a, w, b+q, h-q, wb+q, g, c).fork();
                    s = new EmptyCompleter(bc);
                    n = q;
                }
                TimSort.sort(a, b, b + n, c, w, wb, n);
                s.tryComplete();
            }
        }

        static final class Merger<T> extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final T[] a, w; // 主数组和工作区数组
            final int lbase, lsize, rbase, rsize, wbase, gran;
            Comparator<? super T> comparator;
            Merger(CountedCompleter<?> par, T[] a, T[] w,
                   int lbase, int lsize, int rbase,
                   int rsize, int wbase, int gran,
                   Comparator<? super T> comparator) {
                super(par);
                this.a = a; this.w = w;
                this.lbase = lbase; this.lsize = lsize;
                this.rbase = rbase; this.rsize = rsize;
                this.wbase = wbase; this.gran = gran;
                this.comparator = comparator;
            }


                        public final void compute() {
                Comparator<? super T> c = this.comparator;
                T[] a = this.a, w = this.w; // localize all params
                int lb = this.lbase, ln = this.lsize, rb = this.rbase,
                    rn = this.rsize, k = this.wbase, g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0 ||
                    c == null)
                    throw new IllegalStateException(); // hoist checks
                for (int lh, rh;;) {  // split larger, find point in smaller
                    if (ln >= rn) {
                        if (ln <= g)
                            break;
                        rh = rn;
                        T split = a[(lh = ln >>> 1) + lb];
                        for (int lo = 0; lo < rh; ) {
                            int rm = (lo + rh) >>> 1;
                            if (c.compare(split, a[rm + rb]) <= 0)
                                rh = rm;
                            else
                                lo = rm + 1;
                        }
                    }
                    else {
                        if (rn <= g)
                            break;
                        lh = ln;
                        T split = a[(rh = rn >>> 1) + rb];
                        for (int lo = 0; lo < lh; ) {
                            int lm = (lo + lh) >>> 1;
                            if (c.compare(split, a[lm + lb]) <= 0)
                                lh = lm;
                            else
                                lo = lm + 1;
                        }
                    }
                    Merger<T> m = new Merger<T>(this, a, w, lb + lh, ln - lh,
                                                rb + rh, rn - rh,
                                                k + lh + rh, g, c);
                    rn = rh;
                    ln = lh;
                    addToPendingCount(1);
                    m.fork();
                }

                int lf = lb + ln, rf = rb + rn; // index bounds
                while (lb < lf && rb < rf) {
                    T t, al, ar;
                    if (c.compare((al = a[lb]), (ar = a[rb])) <= 0) {
                        lb++; t = al;
                    }
                    else {
                        rb++; t = ar;
                    }
                    w[k++] = t;
                }
                if (rb < rf)
                    System.arraycopy(a, rb, w, k, rf - rb);
                else if (lb < lf)
                    System.arraycopy(a, lb, w, k, lf - lb);

                tryComplete();
            }

        }
    } // FJObject

    /** byte support class */
    static final class FJByte {
        static final class Sorter extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final byte[] a, w;
            final int base, size, wbase, gran;
            Sorter(CountedCompleter<?> par, byte[] a, byte[] w, int base,
                   int size, int wbase, int gran) {
                super(par);
                this.a = a; this.w = w; this.base = base; this.size = size;
                this.wbase = wbase; this.gran = gran;
            }
            public final void compute() {
                CountedCompleter<?> s = this;
                byte[] a = this.a, w = this.w; // localize all params
                int b = this.base, n = this.size, wb = this.wbase, g = this.gran;
                while (n > g) {
                    int h = n >>> 1, q = h >>> 1, u = h + q; // quartiles
                    Relay fc = new Relay(new Merger(s, w, a, wb, h,
                                                    wb+h, n-h, b, g));
                    Relay rc = new Relay(new Merger(fc, a, w, b+h, q,
                                                    b+u, n-u, wb+h, g));
                    new Sorter(rc, a, w, b+u, n-u, wb+u, g).fork();
                    new Sorter(rc, a, w, b+h, q, wb+h, g).fork();;
                    Relay bc = new Relay(new Merger(fc, a, w, b, q,
                                                    b+q, h-q, wb, g));
                    new Sorter(bc, a, w, b+q, h-q, wb+q, g).fork();
                    s = new EmptyCompleter(bc);
                    n = q;
                }
                DualPivotQuicksort.sort(a, b, b + n - 1);
                s.tryComplete();
            }
        }

        static final class Merger extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final byte[] a, w; // main and workspace arrays
            final int lbase, lsize, rbase, rsize, wbase, gran;
            Merger(CountedCompleter<?> par, byte[] a, byte[] w,
                   int lbase, int lsize, int rbase,
                   int rsize, int wbase, int gran) {
                super(par);
                this.a = a; this.w = w;
                this.lbase = lbase; this.lsize = lsize;
                this.rbase = rbase; this.rsize = rsize;
                this.wbase = wbase; this.gran = gran;
            }

            public final void compute() {
                byte[] a = this.a, w = this.w; // localize all params
                int lb = this.lbase, ln = this.lsize, rb = this.rbase,
                    rn = this.rsize, k = this.wbase, g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0)
                    throw new IllegalStateException(); // hoist checks
                for (int lh, rh;;) {  // split larger, find point in smaller
                    if (ln >= rn) {
                        if (ln <= g)
                            break;
                        rh = rn;
                        byte split = a[(lh = ln >>> 1) + lb];
                        for (int lo = 0; lo < rh; ) {
                            int rm = (lo + rh) >>> 1;
                            if (split <= a[rm + rb])
                                rh = rm;
                            else
                                lo = rm + 1;
                        }
                    }
                    else {
                        if (rn <= g)
                            break;
                        lh = ln;
                        byte split = a[(rh = rn >>> 1) + rb];
                        for (int lo = 0; lo < lh; ) {
                            int lm = (lo + lh) >>> 1;
                            if (split <= a[lm + lb])
                                lh = lm;
                            else
                                lo = lm + 1;
                        }
                    }
                    Merger m = new Merger(this, a, w, lb + lh, ln - lh,
                                          rb + rh, rn - rh,
                                          k + lh + rh, g);
                    rn = rh;
                    ln = lh;
                    addToPendingCount(1);
                    m.fork();
                }


                            int lf = lb + ln, rf = rb + rn; // 索引边界
                while (lb < lf && rb < rf) {
                    byte t, al, ar;
                    if ((al = a[lb]) <= (ar = a[rb])) {
                        lb++; t = al;
                    }
                    else {
                        rb++; t = ar;
                    }
                    w[k++] = t;
                }
                if (rb < rf)
                    System.arraycopy(a, rb, w, k, rf - rb);
                else if (lb < lf)
                    System.arraycopy(a, lb, w, k, lf - lb);
                tryComplete();
            }
        }
    } // FJByte

    /** char 支持类 */
    static final class FJChar {
        static final class Sorter extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final char[] a, w;
            final int base, size, wbase, gran;
            Sorter(CountedCompleter<?> par, char[] a, char[] w, int base,
                   int size, int wbase, int gran) {
                super(par);
                this.a = a; this.w = w; this.base = base; this.size = size;
                this.wbase = wbase; this.gran = gran;
            }
            public final void compute() {
                CountedCompleter<?> s = this;
                char[] a = this.a, w = this.w; // 本地化所有参数
                int b = this.base, n = this.size, wb = this.wbase, g = this.gran;
                while (n > g) {
                    int h = n >>> 1, q = h >>> 1, u = h + q; // 四分位数
                    Relay fc = new Relay(new Merger(s, w, a, wb, h,
                                                    wb+h, n-h, b, g));
                    Relay rc = new Relay(new Merger(fc, a, w, b+h, q,
                                                    b+u, n-u, wb+h, g));
                    new Sorter(rc, a, w, b+u, n-u, wb+u, g).fork();
                    new Sorter(rc, a, w, b+h, q, wb+h, g).fork();;
                    Relay bc = new Relay(new Merger(fc, a, w, b, q,
                                                    b+q, h-q, wb, g));
                    new Sorter(bc, a, w, b+q, h-q, wb+q, g).fork();
                    s = new EmptyCompleter(bc);
                    n = q;
                }
                DualPivotQuicksort.sort(a, b, b + n - 1, w, wb, n);
                s.tryComplete();
            }
        }

        static final class Merger extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final char[] a, w; // 主数组和工作区数组
            final int lbase, lsize, rbase, rsize, wbase, gran;
            Merger(CountedCompleter<?> par, char[] a, char[] w,
                   int lbase, int lsize, int rbase,
                   int rsize, int wbase, int gran) {
                super(par);
                this.a = a; this.w = w;
                this.lbase = lbase; this.lsize = lsize;
                this.rbase = rbase; this.rsize = rsize;
                this.wbase = wbase; this.gran = gran;
            }

            public final void compute() {
                char[] a = this.a, w = this.w; // 本地化所有参数
                int lb = this.lbase, ln = this.lsize, rb = this.rbase,
                    rn = this.rsize, k = this.wbase, g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0)
                    throw new IllegalStateException(); // 提前检查
                for (int lh, rh;;) {  // 分割较大的部分，找到较小部分中的点
                    if (ln >= rn) {
                        if (ln <= g)
                            break;
                        rh = rn;
                        char split = a[(lh = ln >>> 1) + lb];
                        for (int lo = 0; lo < rh; ) {
                            int rm = (lo + rh) >>> 1;
                            if (split <= a[rm + rb])
                                rh = rm;
                            else
                                lo = rm + 1;
                        }
                    }
                    else {
                        if (rn <= g)
                            break;
                        lh = ln;
                        char split = a[(rh = rn >>> 1) + rb];
                        for (int lo = 0; lo < lh; ) {
                            int lm = (lo + lh) >>> 1;
                            if (split <= a[lm + lb])
                                lh = lm;
                            else
                                lo = lm + 1;
                        }
                    }
                    Merger m = new Merger(this, a, w, lb + lh, ln - lh,
                                          rb + rh, rn - rh,
                                          k + lh + rh, g);
                    rn = rh;
                    ln = lh;
                    addToPendingCount(1);
                    m.fork();
                }

                int lf = lb + ln, rf = rb + rn; // 索引边界
                while (lb < lf && rb < rf) {
                    char t, al, ar;
                    if ((al = a[lb]) <= (ar = a[rb])) {
                        lb++; t = al;
                    }
                    else {
                        rb++; t = ar;
                    }
                    w[k++] = t;
                }
                if (rb < rf)
                    System.arraycopy(a, rb, w, k, rf - rb);
                else if (lb < lf)
                    System.arraycopy(a, lb, w, k, lf - lb);
                tryComplete();
            }
        }
    } // FJChar

    /** short 支持类 */
    static final class FJShort {
        static final class Sorter extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final short[] a, w;
            final int base, size, wbase, gran;
            Sorter(CountedCompleter<?> par, short[] a, short[] w, int base,
                   int size, int wbase, int gran) {
                super(par);
                this.a = a; this.w = w; this.base = base; this.size = size;
                this.wbase = wbase; this.gran = gran;
            }
            public final void compute() {
                CountedCompleter<?> s = this;
                short[] a = this.a, w = this.w; // 本地化所有参数
                int b = this.base, n = this.size, wb = this.wbase, g = this.gran;
                while (n > g) {
                    int h = n >>> 1, q = h >>> 1, u = h + q; // 四分位数
                    Relay fc = new Relay(new Merger(s, w, a, wb, h,
                                                    wb+h, n-h, b, g));
                    Relay rc = new Relay(new Merger(fc, a, w, b+h, q,
                                                    b+u, n-u, wb+h, g));
                    new Sorter(rc, a, w, b+u, n-u, wb+u, g).fork();
                    new Sorter(rc, a, w, b+h, q, wb+h, g).fork();;
                    Relay bc = new Relay(new Merger(fc, a, w, b, q,
                                                    b+q, h-q, wb, g));
                    new Sorter(bc, a, w, b+q, h-q, wb+q, g).fork();
                    s = new EmptyCompleter(bc);
                    n = q;
                }
                DualPivotQuicksort.sort(a, b, b + n - 1, w, wb, n);
                s.tryComplete();
            }
        }


                    static final class Merger extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final short[] a, w; // 主数组和工作区数组
            final int lbase, lsize, rbase, rsize, wbase, gran;
            Merger(CountedCompleter<?> par, short[] a, short[] w,
                   int lbase, int lsize, int rbase,
                   int rsize, int wbase, int gran) {
                super(par);
                this.a = a; this.w = w;
                this.lbase = lbase; this.lsize = lsize;
                this.rbase = rbase; this.rsize = rsize;
                this.wbase = wbase; this.gran = gran;
            }

            public final void compute() {
                short[] a = this.a, w = this.w; // 本地化所有参数
                int lb = this.lbase, ln = this.lsize, rb = this.rbase,
                    rn = this.rsize, k = this.wbase, g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0)
                    throw new IllegalStateException(); // 提前进行检查
                for (int lh, rh;;) {  // 分割较大的部分，找到较小部分中的点
                    if (ln >= rn) {
                        if (ln <= g)
                            break;
                        rh = rn;
                        short split = a[(lh = ln >>> 1) + lb];
                        for (int lo = 0; lo < rh; ) {
                            int rm = (lo + rh) >>> 1;
                            if (split <= a[rm + rb])
                                rh = rm;
                            else
                                lo = rm + 1;
                        }
                    }
                    else {
                        if (rn <= g)
                            break;
                        lh = ln;
                        short split = a[(rh = rn >>> 1) + rb];
                        for (int lo = 0; lo < lh; ) {
                            int lm = (lo + lh) >>> 1;
                            if (split <= a[lm + lb])
                                lh = lm;
                            else
                                lo = lm + 1;
                        }
                    }
                    Merger m = new Merger(this, a, w, lb + lh, ln - lh,
                                          rb + rh, rn - rh,
                                          k + lh + rh, g);
                    rn = rh;
                    ln = lh;
                    addToPendingCount(1);
                    m.fork();
                }

                int lf = lb + ln, rf = rb + rn; // 索引边界
                while (lb < lf && rb < rf) {
                    short t, al, ar;
                    if ((al = a[lb]) <= (ar = a[rb])) {
                        lb++; t = al;
                    }
                    else {
                        rb++; t = ar;
                    }
                    w[k++] = t;
                }
                if (rb < rf)
                    System.arraycopy(a, rb, w, k, rf - rb);
                else if (lb < lf)
                    System.arraycopy(a, lb, w, k, lf - lb);
                tryComplete();
            }
        }
    } // FJShort

    /** int 支持类 */
    static final class FJInt {
        static final class Sorter extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final int[] a, w;
            final int base, size, wbase, gran;
            Sorter(CountedCompleter<?> par, int[] a, int[] w, int base,
                   int size, int wbase, int gran) {
                super(par);
                this.a = a; this.w = w; this.base = base; this.size = size;
                this.wbase = wbase; this.gran = gran;
            }
            public final void compute() {
                CountedCompleter<?> s = this;
                int[] a = this.a, w = this.w; // 本地化所有参数
                int b = this.base, n = this.size, wb = this.wbase, g = this.gran;
                while (n > g) {
                    int h = n >>> 1, q = h >>> 1, u = h + q; // 四分位数
                    Relay fc = new Relay(new Merger(s, w, a, wb, h,
                                                    wb+h, n-h, b, g));
                    Relay rc = new Relay(new Merger(fc, a, w, b+h, q,
                                                    b+u, n-u, wb+h, g));
                    new Sorter(rc, a, w, b+u, n-u, wb+u, g).fork();
                    new Sorter(rc, a, w, b+h, q, wb+h, g).fork();;
                    Relay bc = new Relay(new Merger(fc, a, w, b, q,
                                                    b+q, h-q, wb, g));
                    new Sorter(bc, a, w, b+q, h-q, wb+q, g).fork();
                    s = new EmptyCompleter(bc);
                    n = q;
                }
                DualPivotQuicksort.sort(a, b, b + n - 1, w, wb, n);
                s.tryComplete();
            }
        }

        static final class Merger extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final int[] a, w; // 主数组和工作区数组
            final int lbase, lsize, rbase, rsize, wbase, gran;
            Merger(CountedCompleter<?> par, int[] a, int[] w,
                   int lbase, int lsize, int rbase,
                   int rsize, int wbase, int gran) {
                super(par);
                this.a = a; this.w = w;
                this.lbase = lbase; this.lsize = lsize;
                this.rbase = rbase; this.rsize = rsize;
                this.wbase = wbase; this.gran = gran;
            }

            public final void compute() {
                int[] a = this.a, w = this.w; // 本地化所有参数
                int lb = this.lbase, ln = this.lsize, rb = this.rbase,
                    rn = this.rsize, k = this.wbase, g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0)
                    throw new IllegalStateException(); // 提前进行检查
                for (int lh, rh;;) {  // 分割较大的部分，找到较小部分中的点
                    if (ln >= rn) {
                        if (ln <= g)
                            break;
                        rh = rn;
                        int split = a[(lh = ln >>> 1) + lb];
                        for (int lo = 0; lo < rh; ) {
                            int rm = (lo + rh) >>> 1;
                            if (split <= a[rm + rb])
                                rh = rm;
                            else
                                lo = rm + 1;
                        }
                    }
                    else {
                        if (rn <= g)
                            break;
                        lh = ln;
                        int split = a[(rh = rn >>> 1) + rb];
                        for (int lo = 0; lo < lh; ) {
                            int lm = (lo + lh) >>> 1;
                            if (split <= a[lm + lb])
                                lh = lm;
                            else
                                lo = lm + 1;
                        }
                    }
                    Merger m = new Merger(this, a, w, lb + lh, ln - lh,
                                          rb + rh, rn - rh,
                                          k + lh + rh, g);
                    rn = rh;
                    ln = lh;
                    addToPendingCount(1);
                    m.fork();
                }


                            int lf = lb + ln, rf = rb + rn; // 索引边界
                while (lb < lf && rb < rf) {
                    int t, al, ar;
                    if ((al = a[lb]) <= (ar = a[rb])) {
                        lb++; t = al;
                    }
                    else {
                        rb++; t = ar;
                    }
                    w[k++] = t;
                }
                if (rb < rf)
                    System.arraycopy(a, rb, w, k, rf - rb);
                else if (lb < lf)
                    System.arraycopy(a, lb, w, k, lf - lb);
                tryComplete();
            }
        }
    } // FJInt

    /** 长整型支持类 */
    static final class FJLong {
        static final class Sorter extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final long[] a, w;
            final int base, size, wbase, gran;
            Sorter(CountedCompleter<?> par, long[] a, long[] w, int base,
                   int size, int wbase, int gran) {
                super(par);
                this.a = a; this.w = w; this.base = base; this.size = size;
                this.wbase = wbase; this.gran = gran;
            }
            public final void compute() {
                CountedCompleter<?> s = this;
                long[] a = this.a, w = this.w; // 本地化所有参数
                int b = this.base, n = this.size, wb = this.wbase, g = this.gran;
                while (n > g) {
                    int h = n >>> 1, q = h >>> 1, u = h + q; // 四分位数
                    Relay fc = new Relay(new Merger(s, w, a, wb, h,
                                                    wb+h, n-h, b, g));
                    Relay rc = new Relay(new Merger(fc, a, w, b+h, q,
                                                    b+u, n-u, wb+h, g));
                    new Sorter(rc, a, w, b+u, n-u, wb+u, g).fork();
                    new Sorter(rc, a, w, b+h, q, wb+h, g).fork();;
                    Relay bc = new Relay(new Merger(fc, a, w, b, q,
                                                    b+q, h-q, wb, g));
                    new Sorter(bc, a, w, b+q, h-q, wb+q, g).fork();
                    s = new EmptyCompleter(bc);
                    n = q;
                }
                DualPivotQuicksort.sort(a, b, b + n - 1, w, wb, n);
                s.tryComplete();
            }
        }

        static final class Merger extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final long[] a, w; // 主数组和工作区数组
            final int lbase, lsize, rbase, rsize, wbase, gran;
            Merger(CountedCompleter<?> par, long[] a, long[] w,
                   int lbase, int lsize, int rbase,
                   int rsize, int wbase, int gran) {
                super(par);
                this.a = a; this.w = w;
                this.lbase = lbase; this.lsize = lsize;
                this.rbase = rbase; this.rsize = rsize;
                this.wbase = wbase; this.gran = gran;
            }

            public final void compute() {
                long[] a = this.a, w = this.w; // 本地化所有参数
                int lb = this.lbase, ln = this.lsize, rb = this.rbase,
                    rn = this.rsize, k = this.wbase, g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0)
                    throw new IllegalStateException(); // 提前检查
                for (int lh, rh;;) {  // 分割较大者，找到较小者中的点
                    if (ln >= rn) {
                        if (ln <= g)
                            break;
                        rh = rn;
                        long split = a[(lh = ln >>> 1) + lb];
                        for (int lo = 0; lo < rh; ) {
                            int rm = (lo + rh) >>> 1;
                            if (split <= a[rm + rb])
                                rh = rm;
                            else
                                lo = rm + 1;
                        }
                    }
                    else {
                        if (rn <= g)
                            break;
                        lh = ln;
                        long split = a[(rh = rn >>> 1) + rb];
                        for (int lo = 0; lo < lh; ) {
                            int lm = (lo + lh) >>> 1;
                            if (split <= a[lm + lb])
                                lh = lm;
                            else
                                lo = lm + 1;
                        }
                    }
                    Merger m = new Merger(this, a, w, lb + lh, ln - lh,
                                          rb + rh, rn - rh,
                                          k + lh + rh, g);
                    rn = rh;
                    ln = lh;
                    addToPendingCount(1);
                    m.fork();
                }

                int lf = lb + ln, rf = rb + rn; // 索引边界
                while (lb < lf && rb < rf) {
                    long t, al, ar;
                    if ((al = a[lb]) <= (ar = a[rb])) {
                        lb++; t = al;
                    }
                    else {
                        rb++; t = ar;
                    }
                    w[k++] = t;
                }
                if (rb < rf)
                    System.arraycopy(a, rb, w, k, rf - rb);
                else if (lb < lf)
                    System.arraycopy(a, lb, w, k, lf - lb);
                tryComplete();
            }
        }
    } // FJLong

    /** 浮点型支持类 */
    static final class FJFloat {
        static final class Sorter extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final float[] a, w;
            final int base, size, wbase, gran;
            Sorter(CountedCompleter<?> par, float[] a, float[] w, int base,
                   int size, int wbase, int gran) {
                super(par);
                this.a = a; this.w = w; this.base = base; this.size = size;
                this.wbase = wbase; this.gran = gran;
            }
            public final void compute() {
                CountedCompleter<?> s = this;
                float[] a = this.a, w = this.w; // 本地化所有参数
                int b = this.base, n = this.size, wb = this.wbase, g = this.gran;
                while (n > g) {
                    int h = n >>> 1, q = h >>> 1, u = h + q; // 四分位数
                    Relay fc = new Relay(new Merger(s, w, a, wb, h,
                                                    wb+h, n-h, b, g));
                    Relay rc = new Relay(new Merger(fc, a, w, b+h, q,
                                                    b+u, n-u, wb+h, g));
                    new Sorter(rc, a, w, b+u, n-u, wb+u, g).fork();
                    new Sorter(rc, a, w, b+h, q, wb+h, g).fork();;
                    Relay bc = new Relay(new Merger(fc, a, w, b, q,
                                                    b+q, h-q, wb, g));
                    new Sorter(bc, a, w, b+q, h-q, wb+q, g).fork();
                    s = new EmptyCompleter(bc);
                    n = q;
                }
                DualPivotQuicksort.sort(a, b, b + n - 1, w, wb, n);
                s.tryComplete();
            }
        }


                    static final class Merger extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final float[] a, w; // 主数组和工作区数组
            final int lbase, lsize, rbase, rsize, wbase, gran;
            Merger(CountedCompleter<?> par, float[] a, float[] w,
                   int lbase, int lsize, int rbase,
                   int rsize, int wbase, int gran) {
                super(par);
                this.a = a; this.w = w;
                this.lbase = lbase; this.lsize = lsize;
                this.rbase = rbase; this.rsize = rsize;
                this.wbase = wbase; this.gran = gran;
            }

            public final void compute() {
                float[] a = this.a, w = this.w; // 本地化所有参数
                int lb = this.lbase, ln = this.lsize, rb = this.rbase,
                    rn = this.rsize, k = this.wbase, g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0)
                    throw new IllegalStateException(); // 提前进行检查
                for (int lh, rh;;) {  // 分割较大的部分，找到较小部分中的点
                    if (ln >= rn) {
                        if (ln <= g)
                            break;
                        rh = rn;
                        float split = a[(lh = ln >>> 1) + lb];
                        for (int lo = 0; lo < rh; ) {
                            int rm = (lo + rh) >>> 1;
                            if (split <= a[rm + rb])
                                rh = rm;
                            else
                                lo = rm + 1;
                        }
                    }
                    else {
                        if (rn <= g)
                            break;
                        lh = ln;
                        float split = a[(rh = rn >>> 1) + rb];
                        for (int lo = 0; lo < lh; ) {
                            int lm = (lo + lh) >>> 1;
                            if (split <= a[lm + lb])
                                lh = lm;
                            else
                                lo = lm + 1;
                        }
                    }
                    Merger m = new Merger(this, a, w, lb + lh, ln - lh,
                                          rb + rh, rn - rh,
                                          k + lh + rh, g);
                    rn = rh;
                    ln = lh;
                    addToPendingCount(1);
                    m.fork();
                }

                int lf = lb + ln, rf = rb + rn; // 索引边界
                while (lb < lf && rb < rf) {
                    float t, al, ar;
                    if ((al = a[lb]) <= (ar = a[rb])) {
                        lb++; t = al;
                    }
                    else {
                        rb++; t = ar;
                    }
                    w[k++] = t;
                }
                if (rb < rf)
                    System.arraycopy(a, rb, w, k, rf - rb);
                else if (lb < lf)
                    System.arraycopy(a, lb, w, k, lf - lb);
                tryComplete();
            }
        }
    } // FJFloat

    /** double 支持类 */
    static final class FJDouble {
        static final class Sorter extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final double[] a, w;
            final int base, size, wbase, gran;
            Sorter(CountedCompleter<?> par, double[] a, double[] w, int base,
                   int size, int wbase, int gran) {
                super(par);
                this.a = a; this.w = w; this.base = base; this.size = size;
                this.wbase = wbase; this.gran = gran;
            }
            public final void compute() {
                CountedCompleter<?> s = this;
                double[] a = this.a, w = this.w; // 本地化所有参数
                int b = this.base, n = this.size, wb = this.wbase, g = this.gran;
                while (n > g) {
                    int h = n >>> 1, q = h >>> 1, u = h + q; // 四分位数
                    Relay fc = new Relay(new Merger(s, w, a, wb, h,
                                                    wb+h, n-h, b, g));
                    Relay rc = new Relay(new Merger(fc, a, w, b+h, q,
                                                    b+u, n-u, wb+h, g));
                    new Sorter(rc, a, w, b+u, n-u, wb+u, g).fork();
                    new Sorter(rc, a, w, b+h, q, wb+h, g).fork();;
                    Relay bc = new Relay(new Merger(fc, a, w, b, q,
                                                    b+q, h-q, wb, g));
                    new Sorter(bc, a, w, b+q, h-q, wb+q, g).fork();
                    s = new EmptyCompleter(bc);
                    n = q;
                }
                DualPivotQuicksort.sort(a, b, b + n - 1, w, wb, n);
                s.tryComplete();
            }
        }

        static final class Merger extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final double[] a, w; // 主数组和工作区数组
            final int lbase, lsize, rbase, rsize, wbase, gran;
            Merger(CountedCompleter<?> par, double[] a, double[] w,
                   int lbase, int lsize, int rbase,
                   int rsize, int wbase, int gran) {
                super(par);
                this.a = a; this.w = w;
                this.lbase = lbase; this.lsize = lsize;
                this.rbase = rbase; this.rsize = rsize;
                this.wbase = wbase; this.gran = gran;
            }

            public final void compute() {
                double[] a = this.a, w = this.w; // 本地化所有参数
                int lb = this.lbase, ln = this.lsize, rb = this.rbase,
                    rn = this.rsize, k = this.wbase, g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0)
                    throw new IllegalStateException(); // 提前进行检查
                for (int lh, rh;;) {  // 分割较大的部分，找到较小部分中的点
                    if (ln >= rn) {
                        if (ln <= g)
                            break;
                        rh = rn;
                        double split = a[(lh = ln >>> 1) + lb];
                        for (int lo = 0; lo < rh; ) {
                            int rm = (lo + rh) >>> 1;
                            if (split <= a[rm + rb])
                                rh = rm;
                            else
                                lo = rm + 1;
                        }
                    }
                    else {
                        if (rn <= g)
                            break;
                        lh = ln;
                        double split = a[(rh = rn >>> 1) + rb];
                        for (int lo = 0; lo < lh; ) {
                            int lm = (lo + lh) >>> 1;
                            if (split <= a[lm + lb])
                                lh = lm;
                            else
                                lo = lm + 1;
                        }
                    }
                    Merger m = new Merger(this, a, w, lb + lh, ln - lh,
                                          rb + rh, rn - rh,
                                          k + lh + rh, g);
                    rn = rh;
                    ln = lh;
                    addToPendingCount(1);
                    m.fork();
                }


                            int lf = lb + ln, rf = rb + rn; // 索引边界
                while (lb < lf && rb < rf) {
                    double t, al, ar;
                    if ((al = a[lb]) <= (ar = a[rb])) {
                        lb++; t = al;
                    }
                    else {
                        rb++; t = ar;
                    }
                    w[k++] = t;
                }
                if (rb < rf)
                    System.arraycopy(a, rb, w, k, rf - rb);
                else if (lb < lf)
                    System.arraycopy(a, lb, w, k, lf - lb);
                tryComplete();
            }
        }
    } // FJDouble

}
