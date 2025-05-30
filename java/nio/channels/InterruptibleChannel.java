/*
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels;

import java.io.IOException;


/**
 * 一个可以异步关闭和中断的通道。
 *
 * <p> 实现此接口的通道是<i>异步可关闭的：</i>如果一个线程在一个I/O操作上阻塞，而另一个线程调用了通道的{@link
 * #close close}方法，这将导致阻塞的线程收到一个{@link AsynchronousCloseException}。
 *
 * <p> 实现此接口的通道也是<i>可中断的：</i>如果一个线程在一个I/O操作上阻塞，而另一个线程调用了阻塞线程的{@link Thread#interrupt()
 * interrupt}方法，这将导致通道关闭，阻塞的线程收到一个{@link ClosedByInterruptException}，并且阻塞线程的中断状态被设置。
 *
 * <p> 如果一个线程的中断状态已经设置，并且它调用了一个通道上的阻塞I/O操作，那么通道将被关闭，线程将立即收到一个{@link ClosedByInterruptException}；其中断状态将保持设置。
 *
 * <p> 一个通道支持异步关闭和中断，当且仅当它实现了此接口。如果需要，可以通过<tt>instanceof</tt>操作符在运行时进行测试。
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
     * <p> 任何当前在此通道上阻塞在I/O操作中的线程将收到一个{@link AsynchronousCloseException}。
     *
     * <p> 此方法的行为与{@link
     * Channel#close Channel}接口中指定的完全相同。 </p>
     *
     * @throws  IOException  如果发生I/O错误
     */
    public void close() throws IOException;

}
