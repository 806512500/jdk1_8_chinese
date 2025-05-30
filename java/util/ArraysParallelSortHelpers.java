
/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java.util;

import java.util.concurrent.RecursiveAction;
import java.util.concurrent.CountedCompleter;

/**
 * 并行排序方法中的辅助工具类。
 *
 * 对于每种基本类型和对象，我们定义一个静态类来包含该类型的排序器和合并器实现：
 *
 * 排序器类主要基于 CilkSort
 * <A href="http://supertech.lcs.mit.edu/cilk/"> Cilk</A>：
 * 基本算法：
 * 如果数组大小很小，直接使用顺序快速排序（通过 Arrays.sort）
 *         否则：
 *         1. 将数组分成两半。
 *         2. 对每半部分，
 *             a. 将半部分再分成两半（即四分之一），
 *             b. 排序四分之一
 *             c. 将它们合并在一起
 *         3. 将两个半部分合并在一起。
 *
 * 将数组分成四分之一的原因之一是这保证了最终的排序是在主数组中，而不是在工作区数组中。
 * （在每个子排序步骤中，主数组和工作区数组的角色会互换。） 叶级排序使用关联的顺序排序。
 *
 * 合并器类执行排序器的合并操作。它们的结构确保如果底层排序是稳定的（如 TimSort 是稳定的），那么整个排序也是稳定的。
 * 如果足够大，它们会将两个分区中较大的一个分成两半，通过二分查找找到较小分区中小于较大分区第二半部分起始点的最大点；
 * 然后并行合并两个分区。为了确保任务以保持稳定性的顺序触发，当前的 CountedCompleter 设计要求一些小任务作为触发完成任务的占位符。
 * 这些类（EmptyCompleter 和 Relay）不需要跟踪数组，并且永远不会被分叉，因此不保存任何任务状态。
 *
 * 原始类版本（FJByte... FJDouble）彼此相同，除了类型声明。
 *
 * 基本的顺序排序依赖于非公开版本的 TimSort、ComparableTimSort 和 DualPivotQuicksort 排序方法，
 * 这些方法接受我们已经分配的临时工作区数组切片，因此避免了冗余分配。（除了 DualPivotQuicksort byte[] 排序，它从不使用工作区数组。）
 */
/*package*/ class ArraysParallelSortHelpers {

    /*
     * 样式说明：任务类有很多参数，这些参数存储为任务字段并复制到局部变量中，在 compute() 方法中使用。
     * 我们将这些参数尽可能地打包到少量行中，并在主循环之前提升它们之间的一致性检查，以减少干扰。
     */

    /**
     * 排序器的占位符任务，用于最低的四分之一任务，不需要维护数组状态。
     */
    static final class EmptyCompleter extends CountedCompleter<Void> {
        static final long serialVersionUID = 2446542900576103244L;
        EmptyCompleter(CountedCompleter<?> p) { super(p); }
        public final void compute() { }
    }

    /**
     * 两个合并的二次合并的触发器
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

    /** 对象 + 比较器支持类 */
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
                    int h = n >>> 1, q = h >>> 1, u = h + q; // 四分之一
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
                T[] a = this.a, w = this.w; // 本地化所有参数
                int lb = this.lbase, ln = this.lsize, rb = this.rbase,
                    rn = this.rsize, k = this.wbase, g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0 ||
                    c == null)
                    throw new IllegalStateException(); // 提升检查
                for (int lh, rh;;) {  // 分割较大的部分，找到较小部分中的点
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

                int lf = lb + ln, rf = rb + rn; // 索引边界
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

    /** byte 支持类 */
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
                byte[] a = this.a, w = this.w; // 本地化所有参数
                int b = this.base, n = this.size, wb = this.wbase, g = this.gran;
                while (n > g) {
                    int h = n >>> 1, q = h >>> 1, u = h + q; // 四分之一
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
            final byte[] a, w; // 主数组和工作区数组
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
                byte[] a = this.a, w = this.w; // 本地化所有参数
                int lb = this.lbase, ln = this.lsize, rb = this.rbase,
                    rn = this.rsize, k = this.wbase, g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0)
                    throw new IllegalStateException(); // 提升检查
                for (int lh, rh;;) {  // 分割较大的部分，找到较小部分中的点
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
                    int h = n >>> 1, q = h >>> 1, u = h + q; // 四分之一
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


                    public final void compute() {
                        long[] a = this.a, w = this.w; // 本地化所有参数
                        int lb = this.lbase, ln = this.lsize, rb = this.rbase,
                            rn = this.rsize, k = this.wbase, g = this.gran;
                        if (a == null || w == null || lb < 0 || rb < 0 || k < 0)
                            throw new IllegalStateException(); // 提前检查
                        for (int lh, rh;;) {  // 分割较大的部分，找到较小部分的点
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
                            throw new IllegalStateException(); // 提前检查
                        for (int lh, rh;;) {  // 分割较大的部分，找到较小部分的点
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
                            throw new IllegalStateException(); // 提前检查
                        for (int lh, rh;;) {  // 分割较大的部分，找到较小部分的点
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

            /** long 支持类 */
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
                long[] a = this.a, w = this.w; // 定位所有参数
                int lb = this.lbase, ln = this.lsize, rb = this.rbase,
                    rn = this.rsize, k = this.wbase, g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0)
                    throw new IllegalStateException(); // 提前检查
                for (int lh, rh;;) {  // 分割较大的部分，找到较小部分的点
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

    /** 浮点数支持类 */
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
                float[] a = this.a, w = this.w; // 定位所有参数
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
                float[] a = this.a, w = this.w; // 定位所有参数
                int lb = this.lbase, ln = this.lsize, rb = this.rbase,
                    rn = this.rsize, k = this.wbase, g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0)
                    throw new IllegalStateException(); // 提前检查
                for (int lh, rh;;) {  // 分割较大的部分，找到较小部分的点
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

    /** 双精度浮点数支持类 */
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
                double[] a = this.a, w = this.w; // 定位所有参数
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
                double[] a = this.a, w = this.w; // 定位所有参数
                int lb = this.lbase, ln = this.lsize, rb = this.rbase,
                    rn = this.rsize, k = this.wbase, g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0)
                    throw new IllegalStateException(); // 提前检查
                for (int lh, rh;;) {  // 分割较大的部分，找到较小部分的点
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


