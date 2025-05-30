/*
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
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
 * 一个可以将字节读入一系列缓冲区的通道。
 *
 * <p> 一个<i>分散</i>读操作在单次调用中读取一系列字节到一个或多个给定缓冲区序列中。
 * 分散读操作通常在网络协议或文件格式中很有用，例如，将数据分组为一个或多个固定长度的头部后跟一个可变长度的主体。
 * 类似的<i>聚集</i>写操作在{@link GatheringByteChannel}接口中定义。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家组
 * @since 1.4
 */

public interface ScatteringByteChannel
    extends ReadableByteChannel
{

    /**
     * 从该通道读取一系列字节到给定缓冲区的子序列中。
     *
     * <p> 该方法的调用尝试从该通道读取最多<i>r</i>个字节，其中<i>r</i>是给定缓冲区数组的指定子序列中剩余字节的总数，即，
     *
     * <blockquote><pre>
     * dsts[offset].remaining()
     *     + dsts[offset+1].remaining()
     *     + ... + dsts[offset+length-1].remaining()</pre></blockquote>
     *
     * 在调用此方法时。
     *
     * <p> 假设读取了一个长度为<i>n</i>的字节序列，其中
     * <tt>0</tt>&nbsp;<tt>&lt;=</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>。
     * 该序列的前<tt>dsts[offset].remaining()</tt>个字节最多被传输到缓冲区<tt>dsts[offset]</tt>中，
     * 接下来的<tt>dsts[offset+1].remaining()</tt>个字节最多被传输到缓冲区<tt>dsts[offset+1]</tt>中，依此类推，
     * 直到整个字节序列被传输到给定的缓冲区中。尽可能多的字节被传输到每个缓冲区中，因此除了最后一个更新的缓冲区外，
     * 每个更新的缓冲区的最终位置都保证等于该缓冲区的限制。
     *
     * <p> 该方法可以在任何时候调用。但是，如果另一个线程已经在此通道上启动了一个读操作，
     * 那么该方法的调用将被阻塞，直到第一个操作完成。 </p>
     *
     * @param  dsts
     *         要传输字节的缓冲区
     *
     * @param  offset
     *         缓冲区数组中第一个要传输字节的缓冲区的偏移量；必须是非负数且不大于<tt>dsts.length</tt>
     *
     * @param  length
     *         要访问的最大缓冲区数；必须是非负数且不大于
     *         <tt>dsts.length</tt>&nbsp;-&nbsp;<tt>offset</tt>
     *
     * @return 读取的字节数，可能为零，
     *         或者如果通道已到达流的末尾，则返回<tt>-1</tt>
     *
     * @throws  IndexOutOfBoundsException
     *          如果<tt>offset</tt>和<tt>length</tt>参数的前置条件不成立
     *
     * @throws  NonReadableChannelException
     *          如果该通道未打开以供读取
     *
     * @throws  ClosedChannelException
     *          如果该通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果另一个线程在读操作进行时关闭了该通道
     *
     * @throws  ClosedByInterruptException
     *          如果另一个线程在读操作进行时中断了当前线程，
     *          从而关闭了该通道并设置了当前线程的中断状态
     *
     * @throws  IOException
     *          如果发生其他I/O错误
     */
    public long read(ByteBuffer[] dsts, int offset, int length)
        throws IOException;

    /**
     * 从该通道读取一系列字节到给定的缓冲区中。
     *
     * <p> 该方法的调用形式<tt>c.read(dsts)</tt>的行为与调用
     *
     * <blockquote><pre>
     * c.read(dsts, 0, dsts.length);</pre></blockquote>
     *
     * 完全相同。
     *
     * @param  dsts
     *         要传输字节的缓冲区
     *
     * @return 读取的字节数，可能为零，
     *         或者如果通道已到达流的末尾，则返回<tt>-1</tt>
     *
     * @throws  NonReadableChannelException
     *          如果该通道未打开以供读取
     *
     * @throws  ClosedChannelException
     *          如果该通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果另一个线程在读操作进行时关闭了该通道
     *
     * @throws  ClosedByInterruptException
     *          如果另一个线程在读操作进行时中断了当前线程，
     *          从而关闭了该通道并设置了当前线程的中断状态
     *
     * @throws  IOException
     *          如果发生其他I/O错误
     */
    public long read(ByteBuffer[] dsts) throws IOException;

}
