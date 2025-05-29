/*
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

/*
 */

package java.nio.channels;

import java.io.IOException;


/**
 * 可以异步关闭和中断的通道。
 *
 * <p> 实现此接口的通道是<i>异步可关闭的：</i> 如果一个线程在一个可中断的通道上阻塞在 I/O 操作中，另一个线程可以调用通道的 {@link
 * #close close} 方法。这将导致阻塞的线程接收到一个 {@link AsynchronousCloseException}。
 *
 * <p> 实现此接口的通道也是<i>可中断的：</i> 如果一个线程在一个可中断的通道上阻塞在 I/O 操作中，另一个线程可以调用阻塞线程的 {@link Thread#interrupt()
 * interrupt} 方法。这将导致通道关闭，阻塞的线程接收到一个 {@link ClosedByInterruptException}，并且阻塞线程的中断状态被设置。
 *
 * <p> 如果一个线程的中断状态已经设置并且它在一个通道上调用阻塞的 I/O 操作，那么通道将被关闭，线程将立即接收到一个 {@link ClosedByInterruptException}；其中断状态将保持设置。
 *
 * <p> 一个通道支持异步关闭和中断当且仅当它实现了此接口。如果需要，可以在运行时通过 <tt>instanceof</tt> 操作符进行测试。
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */

public interface InterruptibleChannel
    extends Channel
{

    /**
     * 关闭此通道。
     *
     * <p> 任何当前在此通道上阻塞在 I/O 操作中的线程将接收到一个 {@link AsynchronousCloseException}。
     *
     * <p> 否则，此方法的行为与 {@link
     * Channel#close Channel} 接口指定的行为完全相同。 </p>
     *
     * @throws  IOException  如果发生 I/O 错误
     */
    public void close() throws IOException;

}
