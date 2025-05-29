
/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels.spi;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.nio.ch.Interruptible;


/**
 * 可中断通道的基础实现类。
 *
 * <p> 该类封装了实现通道异步关闭和中断所需的低级机制。具体通道类必须在可能无限期阻塞的I/O操作之前和之后分别调用 {@link #begin begin} 和 {@link #end end} 方法。
 * 为了确保 {@link #end end} 方法总是被调用，这些方法应该在 <tt>try</tt>&nbsp;...&nbsp;<tt>finally</tt> 块中使用：
 *
 * <blockquote><pre>
 * boolean completed = false;
 * try {
 *     begin();
 *     completed = ...;    // 执行阻塞I/O操作
 *     return ...;         // 返回结果
 * } finally {
 *     end(completed);
 * }</pre></blockquote>
 *
 * <p> 传递给 {@link #end end} 方法的 <tt>completed</tt> 参数告诉是否I/O操作实际完成，即是否对调用者有任何可见的影响。例如，在读取字节的操作中，如果实际将一些字节传输到调用者的目标缓冲区，则此参数应为 <tt>true</tt>。
 *
 * <p> 具体的通道类还必须实现 {@link #implCloseChannel implCloseChannel} 方法，以便如果在另一个线程在通道上阻塞在本机I/O操作时调用该方法，则该操作应立即返回，无论是通过抛出异常还是正常返回。如果线程被中断或其阻塞的通道被异步关闭，则通道的 {@link #end end} 方法将抛出相应的异常。
 *
 * <p> 该类执行实现 {@link java.nio.channels.Channel} 规范所需的同步。 {@link #implCloseChannel implCloseChannel} 方法的实现无需对可能尝试关闭通道的其他线程进行同步。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */

public abstract class AbstractInterruptibleChannel
    implements Channel, InterruptibleChannel
{

    private final Object closeLock = new Object();
    private volatile boolean open = true;

    /**
     * 初始化此类的新实例。
     */
    protected AbstractInterruptibleChannel() { }

    /**
     * 关闭此通道。
     *
     * <p> 如果通道已经关闭，则此方法立即返回。否则，它将通道标记为关闭，然后调用 {@link #implCloseChannel implCloseChannel} 方法以完成关闭操作。 </p>
     *
     * @throws  IOException
     *          如果发生I/O错误
     */
    public final void close() throws IOException {
        synchronized (closeLock) {
            if (!open)
                return;
            open = false;
            implCloseChannel();
        }
    }

    /**
     * 关闭此通道。
     *
     * <p> 此方法由 {@link #close close} 方法调用，以执行实际的关闭操作。仅当通道尚未关闭时才调用此方法，并且从不调用超过一次。
     *
     * <p> 该方法的实现必须安排任何在本通道上阻塞在I/O操作的其他线程立即返回，无论是通过抛出异常还是正常返回。 </p>
     *
     * @throws  IOException
     *          如果关闭通道时发生I/O错误
     */
    protected abstract void implCloseChannel() throws IOException;

    public final boolean isOpen() {
        return open;
    }


    // -- 中断机制 --

    private Interruptible interruptor;
    private volatile Thread interrupted;

    /**
     * 标记可能无限期阻塞的I/O操作的开始。
     *
     * <p> 该方法应与 {@link #end end} 方法一起使用，使用 <tt>try</tt>&nbsp;...&nbsp;<tt>finally</tt> 块，如上所示，以实现此通道的异步关闭和中断。 </p>
     */
    protected final void begin() {
        if (interruptor == null) {
            interruptor = new Interruptible() {
                    public void interrupt(Thread target) {
                        synchronized (closeLock) {
                            if (!open)
                                return;
                            open = false;
                            interrupted = target;
                            try {
                                AbstractInterruptibleChannel.this.implCloseChannel();
                            } catch (IOException x) { }
                        }
                    }};
        }
        blockedOn(interruptor);
        Thread me = Thread.currentThread();
        if (me.isInterrupted())
            interruptor.interrupt(me);
    }

    /**
     * 标记可能无限期阻塞的I/O操作的结束。
     *
     * <p> 该方法应与 {@link #begin begin} 方法一起使用，使用 <tt>try</tt>&nbsp;...&nbsp;<tt>finally</tt> 块，如上所示，以实现此通道的异步关闭和中断。 </p>
     *
     * @param  completed
     *         <tt>true</tt> 如果且仅当I/O操作成功完成，即对操作的调用者有一些可见的影响
     *
     * @throws  AsynchronousCloseException
     *          如果通道被异步关闭
     *
     * @throws  ClosedByInterruptException
     *          如果在I/O操作中阻塞的线程被中断
     */
    protected final void end(boolean completed)
        throws AsynchronousCloseException
    {
        blockedOn(null);
        Thread interrupted = this.interrupted;
        if (interrupted != null && interrupted == Thread.currentThread()) {
            interrupted = null;
            throw new ClosedByInterruptException();
        }
        if (!completed && !open)
            throw new AsynchronousCloseException();
    }


    // -- sun.misc.SharedSecrets --
    static void blockedOn(Interruptible intr) {         // package-private
        sun.misc.SharedSecrets.getJavaLangAccess().blockedOn(Thread.currentThread(),
                                                             intr);
    }
}
