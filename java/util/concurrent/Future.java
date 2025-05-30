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
 * 一个 {@code Future} 表示异步计算的结果。提供了检查计算是否完成、等待其完成以及检索计算结果的方法。
 * 只有在计算完成时才能使用 {@code get} 方法检索结果，必要时会阻塞直到结果准备好。取消操作由 {@code cancel} 方法执行。
 * 还提供了确定任务是否正常完成或被取消的方法。一旦计算完成，就无法取消。如果希望使用 {@code Future} 仅为了可取消性而不需要提供可用结果，
 * 可以声明形式为 {@code Future<?>} 的类型，并将底层任务的结果返回为 {@code null}。
 *
 * <p>
 * <b>示例用法</b>（以下类都是虚构的。）
 * <pre> {@code
 * interface ArchiveSearcher { String search(String target); }
 * class App {
 *   ExecutorService executor = ...
 *   ArchiveSearcher searcher = ...
 *   void showSearch(final String target)
 *       throws InterruptedException {
 *     Future<String> future
 *       = executor.submit(new Callable<String>() {
 *         public String call() {
 *             return searcher.search(target);
 *         }});
 *     displayOtherThings(); // 在搜索时执行其他操作
 *     try {
 *       displayText(future.get()); // 使用 future
 *     } catch (ExecutionException ex) { cleanup(); return; }
 *   }
 * }}</pre>
 *
 * {@link FutureTask} 类是 {@code Future} 的一个实现，实现了 {@code Runnable}，因此可以由 {@code Executor} 执行。
 * 例如，上述使用 {@code submit} 的构造可以替换为：
 *  <pre> {@code
 * FutureTask<String> future =
 *   new FutureTask<String>(new Callable<String>() {
 *     public String call() {
 *       return searcher.search(target);
 *   }});
 * executor.execute(future);}</pre>
 *
 * <p>内存一致性效果：异步计算采取的操作 <a href="package-summary.html#MemoryVisibility"> <i>先于</i></a>
 * 在另一个线程中跟随的相应 {@code Future.get()} 操作。
 *
 * @see FutureTask
 * @see Executor
 * @since 1.5
 * @author Doug Lea
 * @param <V> 此 Future 的 {@code get} 方法返回的结果类型
 */
public interface Future<V> {

    /**
     * 尝试取消此任务的执行。如果任务已经完成、已经被取消或由于其他原因无法取消，则此尝试将失败。
     * 如果成功，并且在调用 {@code cancel} 时此任务尚未开始，则此任务不应运行。
     * 如果任务已经开始，则 {@code mayInterruptIfRunning} 参数确定是否应中断执行此任务的线程以尝试停止任务。
     *
     * <p>此方法返回后，后续对 {@link #isDone} 的调用将始终返回 {@code true}。
     * 如果此方法返回 {@code true}，则后续对 {@link #isCancelled} 的调用将始终返回 {@code true}。
     *
     * @param mayInterruptIfRunning 如果应中断执行此任务的线程，则为 {@code true}；否则，允许进行中的任务完成
     * @return 如果任务无法取消，通常是因为它已经正常完成，则返回 {@code false}；否则返回 {@code true}
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * 如果此任务在正常完成前被取消，则返回 {@code true}。
     *
     * @return 如果此任务在正常完成前被取消，则返回 {@code true}
     */
    boolean isCancelled();

    /**
     * 如果此任务已完成，则返回 {@code true}。
     *
     * 完成可能是由于正常终止、异常或取消——在所有这些情况下，此方法将返回 {@code true}。
     *
     * @return 如果此任务已完成，则返回 {@code true}
     */
    boolean isDone();

    /**
     * 必要时等待计算完成，然后检索其结果。
     *
     * @return 计算结果
     * @throws CancellationException 如果计算被取消
     * @throws ExecutionException 如果计算抛出异常
     * @throws InterruptedException 如果当前线程在等待时被中断
     */
    V get() throws InterruptedException, ExecutionException;

    /**
     * 必要时最多等待给定时间，以完成计算，然后在可用时检索其结果。
     *
     * @param timeout 最大等待时间
     * @param unit 超时参数的时间单位
     * @return 计算结果
     * @throws CancellationException 如果计算被取消
     * @throws ExecutionException 如果计算抛出异常
     * @throws InterruptedException 如果当前线程在等待时被中断
     * @throws TimeoutException 如果等待超时
     */
    V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
