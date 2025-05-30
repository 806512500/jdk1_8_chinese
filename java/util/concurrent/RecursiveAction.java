/*
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

/**
 * 一个递归无结果的 {@link ForkJoinTask}。此类
 * 建立了将无结果操作参数化为 {@code Void} {@code ForkJoinTask} 的约定。因为 {@code null} 是
 * 类型 {@code Void} 的唯一有效值，所以诸如 {@code join} 等方法在完成时总是返回 {@code null}。
 *
 * <p><b>示例用法。</b> 以下是一个简单但完整的 ForkJoin
 * 排序，用于对给定的 {@code long[]} 数组进行排序：
 *
 *  <pre> {@code
 * static class SortTask extends RecursiveAction {
 *   final long[] array; final int lo, hi;
 *   SortTask(long[] array, int lo, int hi) {
 *     this.array = array; this.lo = lo; this.hi = hi;
 *   }
 *   SortTask(long[] array) { this(array, 0, array.length); }
 *   protected void compute() {
 *     if (hi - lo < THRESHOLD)
 *       sortSequentially(lo, hi);
 *     else {
 *       int mid = (lo + hi) >>> 1;
 *       invokeAll(new SortTask(array, lo, mid),
 *                 new SortTask(array, mid, hi));
 *       merge(lo, mid, hi);
 *     }
 *   }
 *   // 实现细节如下：
 *   static final int THRESHOLD = 1000;
 *   void sortSequentially(int lo, int hi) {
 *     Arrays.sort(array, lo, hi);
 *   }
 *   void merge(int lo, int mid, int hi) {
 *     long[] buf = Arrays.copyOfRange(array, lo, mid);
 *     for (int i = 0, j = lo, k = mid; i < buf.length; j++)
 *       array[j] = (k == hi || buf[i] < array[k]) ?
 *         buf[i++] : array[k++];
 *   }
 * }}</pre>
 *
 * 然后，您可以通过创建 {@code new
 * SortTask(anArray)} 并在 ForkJoinPool 中调用它来对 {@code anArray} 进行排序。作为更具体的简单示例，以下任务将数组的每个元素递增：
 *  <pre> {@code
 * class IncrementTask extends RecursiveAction {
 *   final long[] array; final int lo, hi;
 *   IncrementTask(long[] array, int lo, int hi) {
 *     this.array = array; this.lo = lo; this.hi = hi;
 *   }
 *   protected void compute() {
 *     if (hi - lo < THRESHOLD) {
 *       for (int i = lo; i < hi; ++i)
 *         array[i]++;
 *     }
 *     else {
 *       int mid = (lo + hi) >>> 1;
 *       invokeAll(new IncrementTask(array, lo, mid),
 *                 new IncrementTask(array, mid, hi));
 *     }
 *   }
 * }}</pre>
 *
 * <p>以下示例说明了一些可能提高性能的改进和惯用法：RecursiveActions 不必完全递归，只要它们保持基本的
 * 分而治之的方法即可。以下是一个类，它通过对数组进行两次重复划分，仅将右侧子任务细分，并使用 {@code next} 引用来跟踪它们，来计算数组中每个元素的平方和。它使用基于方法 {@code getSurplusQueuedTaskCount} 的动态阈值，但通过直接执行未被窃取的任务的叶操作而不是进一步细分来平衡潜在的过度分区。
 *
 *  <pre> {@code
 * double sumOfSquares(ForkJoinPool pool, double[] array) {
 *   int n = array.length;
 *   Applyer a = new Applyer(array, 0, n, null);
 *   pool.invoke(a);
 *   return a.result;
 * }
 *
 * class Applyer extends RecursiveAction {
 *   final double[] array;
 *   final int lo, hi;
 *   double result;
 *   Applyer next; // 跟踪右侧任务
 *   Applyer(double[] array, int lo, int hi, Applyer next) {
 *     this.array = array; this.lo = lo; this.hi = hi;
 *     this.next = next;
 *   }
 *
 *   double atLeaf(int l, int h) {
 *     double sum = 0;
 *     for (int i = l; i < h; ++i) // 执行最左侧的基本步骤
 *       sum += array[i] * array[i];
 *     return sum;
 *   }
 *
 *   protected void compute() {
 *     int l = lo;
 *     int h = hi;
 *     Applyer right = null;
 *     while (h - l > 1 && getSurplusQueuedTaskCount() <= 3) {
 *       int mid = (l + h) >>> 1;
 *       right = new Applyer(array, mid, h, right);
 *       right.fork();
 *       h = mid;
 *     }
 *     double sum = atLeaf(l, h);
 *     while (right != null) {
 *       if (right.tryUnfork()) // 如果未被窃取，则直接计算
 *         sum += right.atLeaf(right.lo, right.hi);
 *       else {
 *         right.join();
 *         sum += right.result;
 *       }
 *       right = right.next;
 *     }
 *     result = sum;
 *   }
 * }}</pre>
 *
 * @since 1.7
 * @author Doug Lea
 */
public abstract class RecursiveAction extends ForkJoinTask<Void> {
    private static final long serialVersionUID = 5232453952276485070L;

    /**
     * 由此任务执行的主要计算。
     */
    protected abstract void compute();

    /**
     * 始终返回 {@code null}。
     *
     * @return 始终返回 {@code null}
     */
    public final Void getRawResult() { return null; }

    /**
     * 需要 null 完成值。
     */
    protected final void setRawResult(Void mustBeNull) { }

    /**
     * 实现 RecursiveActions 的执行约定。
     */
    protected final boolean exec() {
        compute();
        return true;
    }

}
