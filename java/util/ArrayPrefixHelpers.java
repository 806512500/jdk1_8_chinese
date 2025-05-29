
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

/*
 * 由 Doug Lea 编写，并在 JCP JSR-166 专家小组成员的帮助下发布到公共领域，如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的。
 */

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.CountedCompleter;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.DoubleBinaryOperator;

/**
 * 用于执行 Arrays.parallelPrefix 操作的 ForkJoin 任务。
 *
 * @author Doug Lea
 * @since 1.8
 */
class ArrayPrefixHelpers {
    private ArrayPrefixHelpers() {}; // 不可实例化

    /*
     * 并行前缀（又称累积、扫描）任务类
     * 大致基于 Guy Blelloch 的原始算法（http://www.cs.cmu.edu/~scandal/alg/scan.html）：
     *  保持除以二直到达到段大小的阈值，然后：
     *   第 1 轮：为每个段创建部分和的树
     *   第 2 轮：对于每个段，与左侧兄弟的偏移量累积
     *
     * 该版本主要通过允许第二轮的左侧子任务在某些右侧子任务的第一轮仍在执行时继续进行，从而在 FJ 框架内提高性能。
     * 它还合并了最左侧段的第一轮和第二轮，并跳过了最右侧段的第一轮（其结果在第二轮中不需要）。它同样设法避免要求用户为累积提供身份基础，
     * 通过跟踪使用第一个现有元素作为基础的那些段/子任务。
     *
     * 管理这一点依赖于在阶段/状态中 OR 一些位：CUMULATE、SUMMED 和 FINISHED。CUMULATE 是主要的阶段位。当为假时，段仅计算其总和。
     * 当为真时，它们累积数组元素。CUMULATE 在根节点开始第二轮时设置，然后向下传播。但也可以更早地为 lo==0 的子树（树的左脊）设置。
     * SUMMED 是一个位连接计数。对于叶子节点，当求和时设置。对于内部节点，当一个子节点求和时变为真。当第二个子节点完成求和时，
     * 我们则向上移动树以触发累积阶段。FINISHED 也是一个位连接计数。对于叶子节点，当累积时设置。对于内部节点，当一个子节点累积时变为真。
     * 当第二个子节点完成累积时，它向上移动树，在根节点完成。
     *
     * 为了更好地利用局部性和减少开销，compute 方法从当前任务开始循环，如果可能的话，移动到其一个子任务而不是分叉。
     *
     * 与这种工具通常一样，有 4 个版本，它们是彼此的简单复制/粘贴/适应变体。（double 和 int 版本与 long 版本的唯一区别是用 "long" 替换（大小写匹配））。
     */

    // 参见上文
    static final int CUMULATE = 1;
    static final int SUMMED   = 2;
    static final int FINISHED = 4;

    /** 用作阈值的最小子任务数组分区大小 */
    static final int MIN_PARTITION = 16;

    static final class CumulateTask<T> extends CountedCompleter<Void> {
        final T[] array;
        final BinaryOperator<T> function;
        CumulateTask<T> left, right;
        T in, out;
        final int lo, hi, origin, fence, threshold;

        /** 根任务构造函数 */
        public CumulateTask(CumulateTask<T> parent,
                            BinaryOperator<T> function,
                            T[] array, int lo, int hi) {
            super(parent);
            this.function = function; this.array = array;
            this.lo = this.origin = lo; this.hi = this.fence = hi;
            int p;
            this.threshold =
                    (p = (hi - lo) / (ForkJoinPool.getCommonPoolParallelism() << 3))
                    <= MIN_PARTITION ? MIN_PARTITION : p;
        }

        /** 子任务构造函数 */
        CumulateTask(CumulateTask<T> parent, BinaryOperator<T> function,
                     T[] array, int origin, int fence, int threshold,
                     int lo, int hi) {
            super(parent);
            this.function = function; this.array = array;
            this.origin = origin; this.fence = fence;
            this.threshold = threshold;
            this.lo = lo; this.hi = hi;
        }

        @SuppressWarnings("unchecked")
        public final void compute() {
            final BinaryOperator<T> fn;
            final T[] a;
            if ((fn = this.function) == null || (a = this.array) == null)
                throw new NullPointerException();    // 提前检查
            int th = threshold, org = origin, fnc = fence, l, h;
            CumulateTask<T> t = this;
            outer: while ((l = t.lo) >= 0 && (h = t.hi) <= a.length) {
                if (h - l > th) {
                    CumulateTask<T> lt = t.left, rt = t.right, f;
                    if (lt == null) {                // 第一轮
                        int mid = (l + h) >>> 1;
                        f = rt = t.right =
                                new CumulateTask<T>(t, fn, a, org, fnc, th, mid, h);
                        t = lt = t.left  =
                                new CumulateTask<T>(t, fn, a, org, fnc, th, l, mid);
                    }
                    else {                           // 可能重新分叉
                        T pin = t.in;
                        lt.in = pin;
                        f = t = null;
                        if (rt != null) {
                            T lout = lt.out;
                            rt.in = (l == org ? lout :
                                     fn.apply(pin, lout));
                            for (int c;;) {
                                if (((c = rt.getPendingCount()) & CUMULATE) != 0)
                                    break;
                                if (rt.compareAndSetPendingCount(c, c|CUMULATE)){
                                    t = rt;
                                    break;
                                }
                            }
                        }
                        for (int c;;) {
                            if (((c = lt.getPendingCount()) & CUMULATE) != 0)
                                break;
                            if (lt.compareAndSetPendingCount(c, c|CUMULATE)) {
                                if (t != null)
                                    f = t;
                                t = lt;
                                break;
                            }
                        }
                        if (t == null)
                            break;
                    }
                    if (f != null)
                        f.fork();
                }
                else {
                    int state; // 转换为求和、累积或两者
                    for (int b;;) {
                        if (((b = t.getPendingCount()) & FINISHED) != 0)
                            break outer;                      // 已经完成
                        state = ((b & CUMULATE) != 0? FINISHED :
                                 (l > org) ? SUMMED : (SUMMED|FINISHED));
                        if (t.compareAndSetPendingCount(b, b|state))
                            break;
                    }


                                T sum;
                    if (state != SUMMED) {
                        int first;
                        if (l == org) {                       // 最左边的部分；没有输入
                            sum = a[org];
                            first = org + 1;
                        }
                        else {
                            sum = t.in;
                            first = l;
                        }
                        for (int i = first; i < h; ++i)       // 累加
                            a[i] = sum = fn.apply(sum, a[i]);
                    }
                    else if (h < fnc) {                       // 跳过最右边的部分
                        sum = a[l];
                        for (int i = l + 1; i < h; ++i)       // 只求和
                            sum = fn.apply(sum, a[i]);
                    }
                    else
                        sum = t.in;
                    t.out = sum;
                    for (CumulateTask<T> par;;) {             // 传播
                        if ((par = (CumulateTask<T>)t.getCompleter()) == null) {
                            if ((state & FINISHED) != 0)      // 启用 join
                                t.quietlyComplete();
                            break outer;
                        }
                        int b = par.getPendingCount();
                        if ((b & state & FINISHED) != 0)
                            t = par;                          // 两者都已完成
                        else if ((b & state & SUMMED) != 0) { // 两者都已求和
                            int nextState; CumulateTask<T> lt, rt;
                            if ((lt = par.left) != null &&
                                (rt = par.right) != null) {
                                T lout = lt.out;
                                par.out = (rt.hi == fnc ? lout :
                                           fn.apply(lout, rt.out));
                            }
                            int refork = (((b & CUMULATE) == 0 &&
                                           par.lo == org) ? CUMULATE : 0);
                            if ((nextState = b|state|refork) == b ||
                                par.compareAndSetPendingCount(b, nextState)) {
                                state = SUMMED;               // 丢弃已完成状态
                                t = par;
                                if (refork != 0)
                                    par.fork();
                            }
                        }
                        else if (par.compareAndSetPendingCount(b, b|state))
                            break outer;                      // 兄弟任务未准备好
                    }
                }
            }
        }
    }

    static final class LongCumulateTask extends CountedCompleter<Void> {
        final long[] array;
        final LongBinaryOperator function;
        LongCumulateTask left, right;
        long in, out;
        final int lo, hi, origin, fence, threshold;

        /** 根任务构造函数 */
        public LongCumulateTask(LongCumulateTask parent,
                                LongBinaryOperator function,
                                long[] array, int lo, int hi) {
            super(parent);
            this.function = function; this.array = array;
            this.lo = this.origin = lo; this.hi = this.fence = hi;
            int p;
            this.threshold =
                    (p = (hi - lo) / (ForkJoinPool.getCommonPoolParallelism() << 3))
                    <= MIN_PARTITION ? MIN_PARTITION : p;
        }

        /** 子任务构造函数 */
        LongCumulateTask(LongCumulateTask parent, LongBinaryOperator function,
                         long[] array, int origin, int fence, int threshold,
                         int lo, int hi) {
            super(parent);
            this.function = function; this.array = array;
            this.origin = origin; this.fence = fence;
            this.threshold = threshold;
            this.lo = lo; this.hi = hi;
        }

        public final void compute() {
            final LongBinaryOperator fn;
            final long[] a;
            if ((fn = this.function) == null || (a = this.array) == null)
                throw new NullPointerException();    // 提前检查
            int th = threshold, org = origin, fnc = fence, l, h;
            LongCumulateTask t = this;
            outer: while ((l = t.lo) >= 0 && (h = t.hi) <= a.length) {
                if (h - l > th) {
                    LongCumulateTask lt = t.left, rt = t.right, f;
                    if (lt == null) {                // 第一次遍历
                        int mid = (l + h) >>> 1;
                        f = rt = t.right =
                                new LongCumulateTask(t, fn, a, org, fnc, th, mid, h);
                        t = lt = t.left  =
                                new LongCumulateTask(t, fn, a, org, fnc, th, l, mid);
                    }
                    else {                           // 可能需要重新分叉
                        long pin = t.in;
                        lt.in = pin;
                        f = t = null;
                        if (rt != null) {
                            long lout = lt.out;
                            rt.in = (l == org ? lout :
                                     fn.applyAsLong(pin, lout));
                            for (int c;;) {
                                if (((c = rt.getPendingCount()) & CUMULATE) != 0)
                                    break;
                                if (rt.compareAndSetPendingCount(c, c|CUMULATE)){
                                    t = rt;
                                    break;
                                }
                            }
                        }
                        for (int c;;) {
                            if (((c = lt.getPendingCount()) & CUMULATE) != 0)
                                break;
                            if (lt.compareAndSetPendingCount(c, c|CUMULATE)) {
                                if (t != null)
                                    f = t;
                                t = lt;
                                break;
                            }
                        }
                        if (t == null)
                            break;
                    }
                    if (f != null)
                        f.fork();
                }
                else {
                    int state; // 转换为求和、累加或两者
                    for (int b;;) {
                        if (((b = t.getPendingCount()) & FINISHED) != 0)
                            break outer;                      // 已经完成
                        state = ((b & CUMULATE) != 0? FINISHED :
                                 (l > org) ? SUMMED : (SUMMED|FINISHED));
                        if (t.compareAndSetPendingCount(b, b|state))
                            break;
                    }


                                long sum;
                    if (state != SUMMED) {
                        int first;
                        if (l == org) {                       // 左边；没有输入
                            sum = a[org];
                            first = org + 1;
                        }
                        else {
                            sum = t.in;
                            first = l;
                        }
                        for (int i = first; i < h; ++i)       // 累加
                            a[i] = sum = fn.applyAsLong(sum, a[i]);
                    }
                    else if (h < fnc) {                       // 跳过右边
                        sum = a[l];
                        for (int i = l + 1; i < h; ++i)       // 只求和
                            sum = fn.applyAsLong(sum, a[i]);
                    }
                    else
                        sum = t.in;
                    t.out = sum;
                    for (LongCumulateTask par;;) {            // 传播
                        if ((par = (LongCumulateTask)t.getCompleter()) == null) {
                            if ((state & FINISHED) != 0)      // 允许加入
                                t.quietlyComplete();
                            break outer;
                        }
                        int b = par.getPendingCount();
                        if ((b & state & FINISHED) != 0)
                            t = par;                          // 两者都完成
                        else if ((b & state & SUMMED) != 0) { // 两者都累加
                            int nextState; LongCumulateTask lt, rt;
                            if ((lt = par.left) != null &&
                                (rt = par.right) != null) {
                                long lout = lt.out;
                                par.out = (rt.hi == fnc ? lout :
                                           fn.applyAsLong(lout, rt.out));
                            }
                            int refork = (((b & CUMULATE) == 0 &&
                                           par.lo == org) ? CUMULATE : 0);
                            if ((nextState = b|state|refork) == b ||
                                par.compareAndSetPendingCount(b, nextState)) {
                                state = SUMMED;               // 丢弃完成
                                t = par;
                                if (refork != 0)
                                    par.fork();
                            }
                        }
                        else if (par.compareAndSetPendingCount(b, b|state))
                            break outer;                      // 兄弟未准备好
                    }
                }
            }
        }
    }

    static final class DoubleCumulateTask extends CountedCompleter<Void> {
        final double[] array;
        final DoubleBinaryOperator function;
        DoubleCumulateTask left, right;
        double in, out;
        final int lo, hi, origin, fence, threshold;

        /** 根任务构造函数 */
        public DoubleCumulateTask(DoubleCumulateTask parent,
                                  DoubleBinaryOperator function,
                                  double[] array, int lo, int hi) {
            super(parent);
            this.function = function; this.array = array;
            this.lo = this.origin = lo; this.hi = this.fence = hi;
            int p;
            this.threshold =
                    (p = (hi - lo) / (ForkJoinPool.getCommonPoolParallelism() << 3))
                    <= MIN_PARTITION ? MIN_PARTITION : p;
        }

        /** 子任务构造函数 */
        DoubleCumulateTask(DoubleCumulateTask parent, DoubleBinaryOperator function,
                           double[] array, int origin, int fence, int threshold,
                           int lo, int hi) {
            super(parent);
            this.function = function; this.array = array;
            this.origin = origin; this.fence = fence;
            this.threshold = threshold;
            this.lo = lo; this.hi = hi;
        }

        public final void compute() {
            final DoubleBinaryOperator fn;
            final double[] a;
            if ((fn = this.function) == null || (a = this.array) == null)
                throw new NullPointerException();    // 提前检查
            int th = threshold, org = origin, fnc = fence, l, h;
            DoubleCumulateTask t = this;
            outer: while ((l = t.lo) >= 0 && (h = t.hi) <= a.length) {
                if (h - l > th) {
                    DoubleCumulateTask lt = t.left, rt = t.right, f;
                    if (lt == null) {                // 第一次通过
                        int mid = (l + h) >>> 1;
                        f = rt = t.right =
                                new DoubleCumulateTask(t, fn, a, org, fnc, th, mid, h);
                        t = lt = t.left  =
                                new DoubleCumulateTask(t, fn, a, org, fnc, th, l, mid);
                    }
                    else {                           // 可能重新分叉
                        double pin = t.in;
                        lt.in = pin;
                        f = t = null;
                        if (rt != null) {
                            double lout = lt.out;
                            rt.in = (l == org ? lout :
                                     fn.applyAsDouble(pin, lout));
                            for (int c;;) {
                                if (((c = rt.getPendingCount()) & CUMULATE) != 0)
                                    break;
                                if (rt.compareAndSetPendingCount(c, c|CUMULATE)){
                                    t = rt;
                                    break;
                                }
                            }
                        }
                        for (int c;;) {
                            if (((c = lt.getPendingCount()) & CUMULATE) != 0)
                                break;
                            if (lt.compareAndSetPendingCount(c, c|CUMULATE)) {
                                if (t != null)
                                    f = t;
                                t = lt;
                                break;
                            }
                        }
                        if (t == null)
                            break;
                    }
                    if (f != null)
                        f.fork();
                }
                else {
                    int state; // 转换为求和、累加或两者
                    for (int b;;) {
                        if (((b = t.getPendingCount()) & FINISHED) != 0)
                            break outer;                      // 已经完成
                        state = ((b & CUMULATE) != 0? FINISHED :
                                 (l > org) ? SUMMED : (SUMMED|FINISHED));
                        if (t.compareAndSetPendingCount(b, b|state))
                            break;
                    }


                                double sum;
                    if (state != SUMMED) {
                        int first;
                        if (l == org) {                       // 最左边；没有输入
                            sum = a[org];
                            first = org + 1;
                        }
                        else {
                            sum = t.in;
                            first = l;
                        }
                        for (int i = first; i < h; ++i)       // 累加
                            a[i] = sum = fn.applyAsDouble(sum, a[i]);
                    }
                    else if (h < fnc) {                       // 跳过最右边
                        sum = a[l];
                        for (int i = l + 1; i < h; ++i)       // 只求和
                            sum = fn.applyAsDouble(sum, a[i]);
                    }
                    else
                        sum = t.in;
                    t.out = sum;
                    for (DoubleCumulateTask par;;) {            // 传播
                        if ((par = (DoubleCumulateTask)t.getCompleter()) == null) {
                            if ((state & FINISHED) != 0)      // 启用 join
                                t.quietlyComplete();
                            break outer;
                        }
                        int b = par.getPendingCount();
                        if ((b & state & FINISHED) != 0)
                            t = par;                          // 两者都已完成
                        else if ((b & state & SUMMED) != 0) { // 两者都已求和
                            int nextState; DoubleCumulateTask lt, rt;
                            if ((lt = par.left) != null &&
                                (rt = par.right) != null) {
                                double lout = lt.out;
                                par.out = (rt.hi == fnc ? lout :
                                           fn.applyAsDouble(lout, rt.out));
                            }
                            int refork = (((b & CUMULATE) == 0 &&
                                           par.lo == org) ? CUMULATE : 0);
                            if ((nextState = b|state|refork) == b ||
                                par.compareAndSetPendingCount(b, nextState)) {
                                state = SUMMED;               // 丢弃已完成
                                t = par;
                                if (refork != 0)
                                    par.fork();
                            }
                        }
                        else if (par.compareAndSetPendingCount(b, b|state))
                            break outer;                      // 兄弟任务未准备好
                    }
                }
            }
        }
    }

    static final class IntCumulateTask extends CountedCompleter<Void> {
        final int[] array;
        final IntBinaryOperator function;
        IntCumulateTask left, right;
        int in, out;
        final int lo, hi, origin, fence, threshold;

        /** 根任务构造函数 */
        public IntCumulateTask(IntCumulateTask parent,
                               IntBinaryOperator function,
                               int[] array, int lo, int hi) {
            super(parent);
            this.function = function; this.array = array;
            this.lo = this.origin = lo; this.hi = this.fence = hi;
            int p;
            this.threshold =
                    (p = (hi - lo) / (ForkJoinPool.getCommonPoolParallelism() << 3))
                    <= MIN_PARTITION ? MIN_PARTITION : p;
        }

        /** 子任务构造函数 */
        IntCumulateTask(IntCumulateTask parent, IntBinaryOperator function,
                        int[] array, int origin, int fence, int threshold,
                        int lo, int hi) {
            super(parent);
            this.function = function; this.array = array;
            this.origin = origin; this.fence = fence;
            this.threshold = threshold;
            this.lo = lo; this.hi = hi;
        }

        public final void compute() {
            final IntBinaryOperator fn;
            final int[] a;
            if ((fn = this.function) == null || (a = this.array) == null)
                throw new NullPointerException();    // 提前检查
            int th = threshold, org = origin, fnc = fence, l, h;
            IntCumulateTask t = this;
            outer: while ((l = t.lo) >= 0 && (h = t.hi) <= a.length) {
                if (h - l > th) {
                    IntCumulateTask lt = t.left, rt = t.right, f;
                    if (lt == null) {                // 第一次遍历
                        int mid = (l + h) >>> 1;
                        f = rt = t.right =
                                new IntCumulateTask(t, fn, a, org, fnc, th, mid, h);
                        t = lt = t.left  =
                                new IntCumulateTask(t, fn, a, org, fnc, th, l, mid);
                    }
                    else {                           // 可能重新分叉
                        int pin = t.in;
                        lt.in = pin;
                        f = t = null;
                        if (rt != null) {
                            int lout = lt.out;
                            rt.in = (l == org ? lout :
                                     fn.applyAsInt(pin, lout));
                            for (int c;;) {
                                if (((c = rt.getPendingCount()) & CUMULATE) != 0)
                                    break;
                                if (rt.compareAndSetPendingCount(c, c|CUMULATE)){
                                    t = rt;
                                    break;
                                }
                            }
                        }
                        for (int c;;) {
                            if (((c = lt.getPendingCount()) & CUMULATE) != 0)
                                break;
                            if (lt.compareAndSetPendingCount(c, c|CUMULATE)) {
                                if (t != null)
                                    f = t;
                                t = lt;
                                break;
                            }
                        }
                        if (t == null)
                            break;
                    }
                    if (f != null)
                        f.fork();
                }
                else {
                    int state; // 转换为求和、累加或两者
                    for (int b;;) {
                        if (((b = t.getPendingCount()) & FINISHED) != 0)
                            break outer;                      // 已经完成
                        state = ((b & CUMULATE) != 0? FINISHED :
                                 (l > org) ? SUMMED : (SUMMED|FINISHED));
                        if (t.compareAndSetPendingCount(b, b|state))
                            break;
                    }


                                int sum;
                    if (state != SUMMED) {
                        int first;
                        if (l == org) {                       // 最左边的；没有输入
                            sum = a[org];
                            first = org + 1;
                        }
                        else {
                            sum = t.in;
                            first = l;
                        }
                        for (int i = first; i < h; ++i)       // 累加
                            a[i] = sum = fn.applyAsInt(sum, a[i]);
                    }
                    else if (h < fnc) {                       // 跳过最右边的
                        sum = a[l];
                        for (int i = l + 1; i < h; ++i)       // 只求和
                            sum = fn.applyAsInt(sum, a[i]);
                    }
                    else
                        sum = t.in;
                    t.out = sum;
                    for (IntCumulateTask par;;) {            // 传播
                        if ((par = (IntCumulateTask)t.getCompleter()) == null) {
                            if ((state & FINISHED) != 0)      // 启用 join
                                t.quietlyComplete();
                            break outer;
                        }
                        int b = par.getPendingCount();
                        if ((b & state & FINISHED) != 0)
                            t = par;                          // 两者都已完成
                        else if ((b & state & SUMMED) != 0) { // 两者都已求和
                            int nextState; IntCumulateTask lt, rt;
                            if ((lt = par.left) != null &&
                                (rt = par.right) != null) {
                                int lout = lt.out;
                                par.out = (rt.hi == fnc ? lout :
                                           fn.applyAsInt(lout, rt.out));
                            }
                            int refork = (((b & CUMULATE) == 0 &&
                                           par.lo == org) ? CUMULATE : 0);
                            if ((nextState = b|state|refork) == b ||
                                par.compareAndSetPendingCount(b, nextState)) {
                                state = SUMMED;               // 丢弃已完成
                                t = par;
                                if (refork != 0)
                                    par.fork();
                            }
                        }
                        else if (par.compareAndSetPendingCount(b, b|state))
                            break outer;                      // 兄弟节点未准备好
                    }
                }
            }
        }
    }
}
