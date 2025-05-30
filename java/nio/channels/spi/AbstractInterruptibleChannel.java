/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 中断通道的基实现类。
 *
 * <p> 此类封装了实现通道的异步关闭和中断所需的低级机制。具体通道类必须在调用可能无限期阻塞的 I/O 操作之前和之后分别调用 {@link #begin begin} 和 {@link #end end} 方法。
 * 为了确保 {@link #end end} 方法总是被调用，这些方法应该在 <tt>try</tt>&nbsp;...&nbsp;<tt>finally</tt> 块中使用：
 *
 * <blockquote><pre>
 * boolean completed = false;
 * try {
 *     begin();
 *     completed = ...;    // 执行阻塞 I/O 操作
 *     return ...;         // 返回结果
 * } finally {
 *     end(completed);
 * }</pre></blockquote>
 *
 * <p> 传递给 {@link #end end} 方法的 <tt>completed</tt> 参数告诉 I/O 操作是否实际完成，即是否对调用者有任何可见的影响。例如，在读取字节的操作中，如果实际有字节传输到调用者的目标缓冲区，则此参数应为 <tt>true</tt>。
 *
 * <p> 具体通道类还必须实现 {@link #implCloseChannel implCloseChannel} 方法，以便在另一个线程在通道上阻塞在本机 I/O 操作时，该操作会立即返回，无论是通过抛出异常还是正常返回。如果线程被中断或其阻塞的通道被异步关闭，则通道的 {@link #end end} 方法将抛出相应的异常。
 *
 * <p> 此类执行实现 {@link java.nio.channels.Channel} 规范所需的同步。 {@link #implCloseChannel implCloseChannel} 方法的实现不需要与其他可能尝试关闭通道的线程同步。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家组
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
     * <p> 如果通道已经关闭，则此方法立即返回。否则，它将通道标记为已关闭，然后调用 {@link #implCloseChannel implCloseChannel} 方法以完成关闭操作。 </p>
     *
     * @throws  IOException
     *          如果发生 I/O 错误
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
     * <p> 此方法由 {@link #close close} 方法调用，以执行关闭通道的实际工作。此方法仅在通道尚未关闭时调用，并且不会被调用多次。
     *
     * <p> 此方法的实现必须安排任何在本通道上阻塞在 I/O 操作的其他线程立即返回，无论是通过抛出异常还是正常返回。 </p>
     *
     * @throws  IOException
     *          如果在关闭通道时发生 I/O 错误
     */
    protected abstract void implCloseChannel() throws IOException;

    public final boolean isOpen() {
        return open;
    }


    // -- 中断机制 --

    private Interruptible interruptor;
    private volatile Thread interrupted;

    /**
     * 标记可能无限期阻塞的 I/O 操作的开始。
     *
     * <p> 此方法应与 {@link #end end} 方法一起使用，使用 <tt>try</tt>&nbsp;...&nbsp;<tt>finally</tt> 块，如上所示，以实现此通道的异步关闭和中断。 </p>
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
     * 标记可能无限期阻塞的 I/O 操作的结束。
     *
     * <p> 此方法应与 {@link #begin begin} 方法一起使用，使用 <tt>try</tt>&nbsp;...&nbsp;<tt>finally</tt> 块，如上所示，以实现此通道的异步关闭和中断。 </p>
     *
     * @param  completed
     *         <tt>true</tt> 如果且仅当 I/O 操作成功完成，即对操作的调用者有可见的影响
     *
     * @throws  AsynchronousCloseException
     *          如果通道被异步关闭
     *
     * @throws  ClosedByInterruptException
     *          如果在 I/O 操作中阻塞的线程被中断
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
