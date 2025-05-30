/*
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * 一个可以读取字节的通道。
 *
 * <p> 任何时候，只能有一个读操作在一个可读通道上进行。如果一个线程启动了一个读操作，
 * 那么任何其他尝试启动另一个读操作的线程将阻塞，直到第一个操作完成。其他类型的 I/O 操作
 * 是否可以与读操作同时进行取决于通道的类型。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
 * @since 1.4
 */

public interface ReadableByteChannel extends Channel {

    /**
     * 从该通道读取一系列字节到给定的缓冲区。
     *
     * <p> 尝试从通道读取最多 <i>r</i> 个字节，其中 <i>r</i> 是缓冲区中剩余的字节数，
     * 即 <tt>dst.remaining()</tt>，在调用此方法时。
     *
     * <p> 假设读取了一个长度为 <i>n</i> 的字节序列，其中
     * <tt>0</tt>&nbsp;<tt>&lt;=</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>。
     * 这个字节序列将被传输到缓冲区，使得序列中的第一个字节位于索引 <i>p</i>，最后一个字节位于索引
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>，
     * 其中 <i>p</i> 是调用此方法时缓冲区的位置。返回时，缓冲区的位置将等于
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>；其限制不会改变。
     *
     * <p> 读操作可能不会填满缓冲区，实际上可能不会读取任何字节。这取决于通道的性质和状态。
     * 例如，处于非阻塞模式的套接字通道不能读取比套接字输入缓冲区中立即可用的字节更多的字节；
 * 类似地，文件通道不能读取比文件中剩余的字节更多的字节。然而，可以保证，如果通道处于阻塞模式并且缓冲区中至少有一个字节剩余，
 * 那么此方法将阻塞，直到至少读取一个字节。
     *
     * <p> 可以随时调用此方法。但是，如果另一个线程已经启动了对这个通道的读操作，
 * 那么调用此方法将阻塞，直到第一个操作完成。 </p>
     *
     * @param  dst
     *         要传输字节的缓冲区
     *
     * @return 读取的字节数，可能为零，或者如果通道已到达流的末尾则返回 <tt>-1</tt>
     *
     * @throws  NonReadableChannelException
     *          如果此通道未打开用于读取
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果在读操作进行过程中另一个线程关闭了此通道
     *
     * @throws  ClosedByInterruptException
     *          如果在读操作进行过程中另一个线程中断了当前线程，
 * 从而关闭了通道并设置了当前线程的中断状态
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public int read(ByteBuffer dst) throws IOException;

}
