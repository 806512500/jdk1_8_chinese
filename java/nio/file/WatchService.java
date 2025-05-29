
/*
 * 版权所有 (c) 2007, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.nio.file;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 一个监视服务，用于监视已注册对象的更改和事件。例如，文件管理器可以使用监视服务来监视目录的更改，以便在文件创建或删除时更新其文件列表显示。
 *
 * <p> 通过调用 {@link Watchable} 对象的 {@link Watchable#register register} 方法将其注册到监视服务，返回一个 {@link WatchKey} 以表示注册。当检测到对象的事件时，键将被<em>信号化</em>，如果当前未被信号化，则将其排队到监视服务，以便消费者通过调用 {@link #poll() poll} 或 {@link #take() take} 方法来检索键并处理事件。处理完事件后，消费者调用键的 {@link WatchKey#reset reset} 方法以重置键，允许键被信号化并重新排队以处理更多事件。
 *
 * <p> 通过调用键的 {@link WatchKey#cancel cancel} 方法可以取消与监视服务的注册。在取消时排队的键将保留在队列中，直到被检索。根据对象的不同，键可能会自动取消。例如，假设目录被监视，监视服务检测到它已被删除或其文件系统不再可访问。当以这种方式取消键时，如果当前未被信号化，它将被信号化并排队。为了确保消费者收到通知，{@code reset} 方法的返回值指示键是否有效。
 *
 * <p> 监视服务可以安全地由多个并发消费者使用。为了确保任何时候只有一个消费者处理特定对象的事件，应确保仅在处理完其事件后调用键的 {@code reset} 方法。可以在任何时候调用 {@link #close close} 方法来关闭服务，这会导致等待检索键的任何线程抛出 {@code ClosedWatchServiceException}。
 *
 * <p> 文件系统可能报告事件的速度比检索或处理的速度快，实现可能对可累积的事件数量施加未指定的限制。当实现<em>有意</em>丢弃事件时，它会安排键的 {@link WatchKey#pollEvents pollEvents} 方法返回一个事件类型为 {@link StandardWatchEventKinds#OVERFLOW OVERFLOW} 的元素。消费者可以使用此事件作为触发器，重新检查对象的状态。
 *
 * <p> 当报告事件指示监视目录中的文件已被修改时，无法保证修改文件的程序（或程序）已完成。应谨慎地与其他可能更新文件的程序协调访问。{@link java.nio.channels.FileChannel FileChannel} 类定义了方法，可以锁定文件的区域以防止其他程序访问。
 *
 * <h2>平台依赖性</h2>
 *
 * <p> 观察文件系统事件的实现旨在直接映射到可用的本机文件事件通知设施，或者在没有本机设施时使用轮询等基本机制。因此，许多关于如何检测事件、其及时性以及是否保留其顺序的细节都是高度实现特定的。例如，当监视目录中的文件被修改时，某些实现可能会导致单个 {@link StandardWatchEventKinds#ENTRY_MODIFY ENTRY_MODIFY} 事件，而其他实现可能会导致多个事件。短命文件（即创建后很快被删除的文件）可能无法被定期轮询文件系统以检测更改的基本实现检测到。
 *
 * <p> 如果被监视的文件未位于本地存储设备上，则检测文件更改的具体实现是特定的。特别是，不要求检测远程系统上执行的文件更改。
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
     * <p> 如果线程当前在 {@link #take take} 或 {@link #poll(long,TimeUnit) poll} 方法中阻塞，等待键被排队，则它会立即收到 {@link ClosedWatchServiceException}。与此监视服务关联的任何有效键都将被 {@link WatchKey#isValid 无效化}。
     *
     * <p> 关闭监视服务后，任何进一步尝试在其上执行操作都将抛出 {@link ClosedWatchServiceException}。如果此监视服务已关闭，则调用此方法不会产生任何效果。
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
     * 检索并移除下一个监视键，必要时等待指定的等待时间，如果当前没有键则等待。
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
     * 获取并移除下一个监视键，如果没有可用的键则等待。
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
