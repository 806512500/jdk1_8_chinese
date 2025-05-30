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
 * 一个执行提交的 {@link Runnable} 任务的对象。此接口提供了一种将任务提交与每个任务将如何运行的机制（包括线程使用、调度等细节）解耦的方法。通常使用 {@code Executor} 而不是显式创建线程。例如，对于一组任务，可以使用以下代码，而不是为每个任务调用 {@code new Thread(new(RunnableTask())).start()}：
 *
 * <pre>
 * Executor executor = <em>anExecutor</em>;
 * executor.execute(new RunnableTask1());
 * executor.execute(new RunnableTask2());
 * ...
 * </pre>
 *
 * 然而，{@code Executor} 接口并不要求执行必须是异步的。在最简单的情况下，执行器可以立即在调用者的线程中运行提交的任务：
 *
 *  <pre> {@code
 * class DirectExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     r.run();
 *   }
 * }}</pre>
 *
 * 更典型的情况是，任务在调用者线程之外的某个线程中执行。下面的执行器为每个任务创建一个新线程：
 *
 *  <pre> {@code
 * class ThreadPerTaskExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     new Thread(r).start();
 *   }
 * }}</pre>
 *
 * 许多 {@code Executor} 实现对任务的调度施加某种限制。下面的执行器将任务提交序列化到第二个执行器，展示了复合执行器的一个示例：
 *
 *  <pre> {@code
 * class SerialExecutor implements Executor {
 *   final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
 *   final Executor executor;
 *   Runnable active;
 *
 *   SerialExecutor(Executor executor) {
 *     this.executor = executor;
 *   }
 *
 *   public synchronized void execute(final Runnable r) {
 *     tasks.offer(new Runnable() {
 *       public void run() {
 *         try {
 *           r.run();
 *         } finally {
 *           scheduleNext();
 *         }
 *       }
 *     });
 *     if (active == null) {
 *       scheduleNext();
 *     }
 *   }
 *
 *   protected synchronized void scheduleNext() {
 *     if ((active = tasks.poll()) != null) {
 *       executor.execute(active);
 *     }
 *   }
 * }}</pre>
 *
 * 本包中提供的 {@code Executor} 实现实现了 {@link ExecutorService}，这是一个更广泛的接口。{@link ThreadPoolExecutor} 类提供了可扩展的线程池实现。{@link Executors} 类提供了这些执行器的便捷工厂方法。
 *
 * <p>内存一致性效果：在将 {@code Runnable} 对象提交给 {@code Executor} 之前，线程中的操作 <a href="package-summary.html#MemoryVisibility"><i>先于</i></a> 其执行开始，可能在另一个线程中。
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface Executor {

    /**
     * 在未来的某个时间执行给定的命令。命令可能在新线程、线程池中的线程或调用线程中执行，具体由 {@code Executor} 实现决定。
     *
     * @param command 要运行的任务
     * @throws RejectedExecutionException 如果此任务不能被接受执行
     * @throws NullPointerException 如果命令为 null
     */
    void execute(Runnable command);
}
