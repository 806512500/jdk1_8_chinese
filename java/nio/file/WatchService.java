/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.file;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 一个监视已注册对象的更改和事件的监视服务。例如，文件管理器可以使用监视服务来监视目录的更改，以便在文件创建或删除时更新其文件列表显示。
 *
 * <p> 通过调用 {@link Watchable} 对象的 {@link Watchable#register register} 方法将其注册到监视服务，返回一个 {@link WatchKey} 以表示注册。当检测到对象的事件时，键将被<em>信号</em>，如果当前未被信号，则将其排队到监视服务，以便消费者调用 {@link #poll() poll} 或 {@link #take() take} 方法来检索键并处理事件。处理完事件后，消费者调用键的 {@link WatchKey#reset reset} 方法来重置键，这允许键被信号并重新排队以处理进一步的事件。
 *
 * <p> 通过调用键的 {@link WatchKey#cancel cancel} 方法可以取消注册。如果键在取消时已排队，则它将保留在队列中直到被检索。根据对象的不同，键可能会自动取消。例如，假设目录被监视，监视服务检测到它已被删除或其文件系统不再可访问。在这种情况下，键将被信号并排队，如果当前未被信号。为了确保消费者收到通知，{@code reset} 方法的返回值指示键是否有效。
 *
 * <p> 监视服务可以安全地由多个并发消费者使用。为了确保任何时候只有一个消费者处理特定对象的事件，应确保在处理完键的事件后才调用键的 {@code reset} 方法。可以在任何时间调用 {@link #close close} 方法来关闭服务，这将导致等待检索键的线程抛出 {@code ClosedWatchServiceException}。
 *
 * <p> 文件系统可能报告事件的速度快于它们可以被检索或处理的速度，实现可能对可以累积的事件数量施加未指定的限制。当实现<em>明知地</em>丢弃事件时，它会安排键的 {@link WatchKey#pollEvents pollEvents} 方法返回一个事件类型为 {@link StandardWatchEventKinds#OVERFLOW OVERFLOW} 的元素。消费者可以使用此事件作为触发器来重新检查对象的状态。
 *
 * <p> 当报告事件表示已监视目录中的文件已被修改时，无法保证修改文件的程序（或程序）已完成。应小心与其他可能正在更新文件的程序协调访问。{@link java.nio.channels.FileChannel FileChannel} 类定义了锁定文件区域以防止其他程序访问的方法。
 *
 * <h2>平台依赖性</h2>
 *
 * <p> 观察文件系统事件的实现旨在直接映射到可用的本机文件事件通知设施，或者在没有本机设施时使用原始机制（如轮询）。因此，许多关于事件检测方式、及时性和顺序是否保留的细节都是高度实现特定的。例如，当已监视目录中的文件被修改时，某些实现可能只生成一个 {@link StandardWatchEventKinds#ENTRY_MODIFY ENTRY_MODIFY} 事件，而其他实现可能生成多个事件。短命文件（即创建后很快被删除的文件）可能无法被使用轮询机制定期检测文件系统更改的原始实现检测到。
 *
 * <p> 如果已监视文件未位于本地存储设备上，则实现特定于是否可以检测到文件的更改。特别是，不要求检测在远程系统上执行的文件更改。
 *
 * @since 1.7
 *
 * @see FileSystem#newWatchService
 */

public interface WatchService
    extends Closeable
{

    /**
     * 关闭此监视服务。
     *
     * <p> 如果线程当前在 {@link #take take} 或 {@link #poll(long,TimeUnit) poll} 方法中阻塞，等待键被排队，则它会立即收到 {@link ClosedWatchServiceException}。与此监视服务关联的所有有效键将被 {@link WatchKey#isValid 无效化}。
     *
     * <p> 关闭监视服务后，任何进一步尝试在其上调用操作都将抛出 {@link ClosedWatchServiceException}。如果此监视服务已关闭，则调用此方法不会产生任何效果。
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    @Override
    void close() throws IOException;

    /**
     * 检索并移除下一个监视键，如果不存在则返回 {@code null}。
     *
     * @return  下一个监视键，或 {@code null}
     *
     * @throws  ClosedWatchServiceException
     *          如果此监视服务已关闭
     */
    WatchKey poll();

    /**
     * 检索并移除下一个监视键，必要时等待指定的等待时间，如果不存在则返回 {@code null}。
     *
     * @param   timeout
     *          在放弃前等待的时间，以 unit 为单位
     * @param   unit
     *          一个 {@code TimeUnit}，用于解释 timeout 参数
     *
     * @return  下一个监视键，或 {@code null}
     *
     * @throws  ClosedWatchServiceException
     *          如果此监视服务已关闭，或在等待下一个键时关闭
     * @throws  InterruptedException
     *          如果在等待时被中断
     */
    WatchKey poll(long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 检索并移除下一个监视键，必要时等待，直到存在。
     *
     * @return  下一个监视键
     *
     * @throws  ClosedWatchServiceException
     *          如果此监视服务已关闭，或在等待下一个键时关闭
     * @throws  InterruptedException
     *          如果在等待时被中断
     */
    WatchKey take() throws InterruptedException;
}
