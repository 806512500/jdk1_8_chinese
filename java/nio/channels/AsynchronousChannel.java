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

package java.nio.channels;

import java.io.IOException;
import java.util.concurrent.Future;  // javadoc

/**
 * 支持异步 I/O 操作的通道。异步 I/O 操作通常有两种形式：
 *
 * <ol>
 * <li><pre>{@link Future}&lt;V&gt; <em>operation</em>(<em>...</em>)</pre></li>
 * <li><pre>void <em>operation</em>(<em>...</em> A attachment, {@link
 *   CompletionHandler}&lt;V,? super A&gt; handler)</pre></li>
 * </ol>
 *
 * 其中 <i>operation</i> 是 I/O 操作的名称（例如读取或写入），<i>V</i> 是 I/O 操作的结果类型，<i>A</i> 是附加到 I/O 操作的对象类型，用于在消费结果时提供上下文。当使用 <em>无状态</em> {@code CompletionHandler} 消费多个 I/O 操作的结果时，附件非常重要。
 *
 * <p> 在第一种形式中，可以使用 {@link Future Future} 接口定义的方法来检查操作是否完成、等待其完成以及检索结果。在第二种形式中，当 I/O 操作完成或失败时，将调用 {@link
 * CompletionHandler} 来消费 I/O 操作的结果。
 *
 * <p> 实现此接口的通道是 <em>异步可关闭的</em>：如果通道上有未完成的 I/O 操作，并且调用了通道的 {@link #close close} 方法，则 I/O 操作将以 {@link AsynchronousCloseException} 异常失败。
 *
 * <p> 异步通道可以安全地由多个并发线程使用。某些通道实现可能支持并发读写，但可能不允许同时有多个读取和写入操作未完成。
 *
 * <h2>取消</h2>
 *
 * <p> {@code Future} 接口定义了 {@link Future#cancel cancel} 方法来取消执行。这会导致所有等待 I/O 操作结果的线程抛出 {@link java.util.concurrent.CancellationException}。底层 I/O 操作是否可以取消高度依赖于实现，因此未指定。如果取消操作使通道或其连接的实体处于不一致状态，则通道将进入实现特定的 <em>错误状态</em>，防止进一步尝试发起与被取消操作 <i>类似</i> 的 I/O 操作。例如，如果读取操作被取消，但实现不能保证没有从通道读取字节，则它将通道置于错误状态；进一步尝试发起 {@code read} 操作将导致抛出未指定的运行时异常。同样，如果写入操作被取消，但实现不能保证没有将字节写入通道，则后续尝试发起 {@code write} 将因未指定的运行时异常而失败。
 *
 * <p> 如果调用 {@link Future#cancel cancel} 方法时将 {@code
 * mayInterruptIfRunning} 参数设置为 {@code true}，则可以通过关闭通道来中断 I/O 操作。在这种情况下，所有等待 I/O 操作结果的线程将抛出 {@code CancellationException}，通道上所有未完成的其他 I/O 操作将以 {@link AsynchronousCloseException} 异常完成。
 *
 * <p> 如果调用 {@code cancel} 方法取消读取或写入操作，则建议丢弃用于 I/O 操作的所有缓冲区，或确保在通道保持打开状态时不会访问这些缓冲区。
 *
 *  @since 1.7
 */

public interface AsynchronousChannel
    extends Channel
{
    /**
     * 关闭此通道。
     *
     * <p> 任何在此通道上的未完成异步操作将以 {@link AsynchronousCloseException} 异常完成。通道关闭后，进一步尝试发起异步 I/O 操作将立即以 {@link ClosedChannelException} 原因完成。
     *
     * <p> 此方法在其他方面的行为与 {@link
     * Channel} 接口指定的行为完全相同。
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    @Override
    void close() throws IOException;
}
