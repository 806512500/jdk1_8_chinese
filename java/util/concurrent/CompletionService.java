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
 * 一个服务，用于将新异步任务的生成与已完成任务结果的消费解耦。生产者 {@code submit} 任务以执行。消费者 {@code take}
 * 已完成的任务并按完成顺序处理其结果。例如，可以使用 {@code CompletionService} 来管理异步 I/O，其中执行读取任务的部分
 * 在程序或系统的某一部分提交，而在读取完成时，不同的部分会处理这些读取任务，可能与请求的顺序不同。
 *
 * <p>通常，{@code CompletionService} 依赖于单独的 {@link Executor} 来实际执行任务，此时 {@code CompletionService}
 * 仅管理内部完成队列。{@link ExecutorCompletionService} 类提供了这种方法的实现。
 *
 * <p>内存一致性效果：线程在提交任务到 {@code CompletionService} 之前的操作
 * <a href="package-summary.html#MemoryVisibility"><i>先于</i></a>
 * 该任务的操作，而这些任务的操作又 <i>先于</i> 对应的 {@code take()} 成功返回后的操作。
 */
public interface CompletionService<V> {
    /**
     * 提交一个返回值的任务以执行，并返回一个 Future 表示任务的待完成结果。完成时，可以通过此任务进行获取或轮询。
     *
     * @param task 要提交的任务
     * @return 一个 Future 表示任务的待完成结果
     * @throws RejectedExecutionException 如果任务无法被调度执行
     * @throws NullPointerException 如果任务为 null
     */
    Future<V> submit(Callable<V> task);

    /**
     * 提交一个 Runnable 任务以执行，并返回一个 Future 表示该任务。完成时，可以通过此任务进行获取或轮询。
     *
     * @param task 要提交的任务
     * @param result 成功完成时返回的结果
     * @return 一个 Future 表示任务的待完成结果，其 {@code get()} 方法将在完成时返回给定的结果值
     * @throws RejectedExecutionException 如果任务无法被调度执行
     * @throws NullPointerException 如果任务为 null
     */
    Future<V> submit(Runnable task, V result);

    /**
     * 检索并移除表示下一个已完成任务的 Future，如果没有任务已完成，则等待。
     *
     * @return 表示下一个已完成任务的 Future
     * @throws InterruptedException 如果在等待时被中断
     */
    Future<V> take() throws InterruptedException;

    /**
     * 检索并移除表示下一个已完成任务的 Future，如果没有任务已完成，则返回 {@code null}。
     *
     * @return 表示下一个已完成任务的 Future，如果没有任务已完成，则返回 {@code null}
     */
    Future<V> poll();

    /**
     * 检索并移除表示下一个已完成任务的 Future，如果必要，等待指定的等待时间。
     *
     * @param timeout 在放弃前等待的时间，以 {@code unit} 为单位
     * @param unit 一个 {@code TimeUnit} 确定如何解释 {@code timeout} 参数
     * @return 表示下一个已完成任务的 Future 或者如果指定的等待时间已过期且没有任务完成，则返回 {@code null}
     * @throws InterruptedException 如果在等待时被中断
     */
    Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException;
}
