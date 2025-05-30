/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.ByteBuffer;
import java.io.IOException;

/**
 * 一个维护当前<i>位置</i>并允许更改位置的字节通道。
 *
 * <p> 可寻址字节通道连接到一个实体，通常是文件，该实体包含可读写的可变长度字节序列。当前位置可以被{@link #position() <i>查询</i>}和{@link #position(long) <i>修改</i>}。通道还提供了访问连接到该通道的实体的当前<i>大小</i>的方法。当在当前大小之外写入字节时，大小会增加；当被{@link #truncate <i>截断</i>}时，大小会减少。
 *
 * <p> 不返回其他值的{@link #position(long) 位置}和{@link #truncate 截断}方法被指定为返回调用它们的通道。这允许方法调用被链式调用。实现此接口的类应专门化返回类型，以便可以在实现类上链式调用方法。
 *
 * @since 1.7
 * @see java.nio.file.Files#newByteChannel
 */

public interface SeekableByteChannel
    extends ByteChannel
{
    /**
     * 从该通道读取一系列字节到给定的缓冲区。
     *
     * <p> 从该通道的当前位置开始读取字节，并将实际读取的字节数更新到当前位置。否则，此方法的行为与在{@link
     * ReadableByteChannel}接口中指定的行为完全相同。
     */
    @Override
    int read(ByteBuffer dst) throws IOException;

    /**
     * 从给定的缓冲区写入一系列字节到该通道。
     *
     * <p> 从该通道的当前位置开始写入字节，除非该通道连接到一个以{@link java.nio.file.StandardOpenOption#APPEND APPEND}选项打开的实体，这种情况下，位置首先被推进到末尾。如果需要，连接的实体将扩展以容纳写入的字节，然后将实际写入的字节数更新到当前位置。否则，此方法的行为与{@link WritableByteChannel}接口中指定的行为完全相同。
     */
    @Override
    int write(ByteBuffer src) throws IOException;

    /**
     * 返回该通道的位置。
     *
     * @return 该通道的位置，一个非负整数，表示从实体的开始到当前位置的字节数。
     *
     * @throws  ClosedChannelException
     *          如果该通道已关闭
     * @throws  IOException
     *          如果发生其他I/O错误
     */
    long position() throws IOException;

    /**
     * 设置该通道的位置。
     *
     * <p> 将位置设置为大于当前大小的值是合法的，但不会改变实体的大小。稍后尝试在这样的位置读取字节将立即返回文件结束指示。稍后尝试在这样的位置写入字节将导致实体扩展以容纳新字节；从之前的文件结束到新写入的字节之间的字节值是未指定的。
     *
     * <p> 当连接到一个以{@link
     * java.nio.file.StandardOpenOption#APPEND APPEND}选项打开的实体时，不建议设置通道的位置。当以追加模式打开时，写入前位置首先被推进到末尾。
     *
     * @param  newPosition
     *         新的位置，一个非负整数，表示从实体的开始到新位置的字节数。
     *
     * @return 该通道
     *
     * @throws  ClosedChannelException
     *          如果该通道已关闭
     * @throws  IllegalArgumentException
     *          如果新位置为负
     * @throws  IOException
     *          如果发生其他I/O错误
     */
    SeekableByteChannel position(long newPosition) throws IOException;

    /**
     * 返回该通道连接的实体的当前大小。
     *
     * @return 当前大小，以字节为单位
     *
     * @throws  ClosedChannelException
     *          如果该通道已关闭
     * @throws  IOException
     *          如果发生其他I/O错误
     */
    long size() throws IOException;

    /**
     * 将该通道连接的实体截断到给定的大小。
     *
     * <p> 如果给定的大小小于当前大小，则实体被截断，丢弃任何超出新末尾的字节。如果给定的大小大于或等于当前大小，则实体不被修改。在任何情况下，如果当前位置大于给定的大小，则将其设置为该大小。
     *
     * <p> 实现此接口的类可能禁止在连接到以{@link
     * java.nio.file.StandardOpenOption#APPEND APPEND}选项打开的实体时截断。
     *
     * @param  size
     *         新的大小，一个非负字节数
     *
     * @return 该通道
     *
     * @throws  NonWritableChannelException
     *          如果该通道未打开用于写入
     * @throws  ClosedChannelException
     *          如果该通道已关闭
     * @throws  IllegalArgumentException
     *          如果新的大小为负
     * @throws  IOException
     *          如果发生其他I/O错误
     */
    SeekableByteChannel truncate(long size) throws IOException;
}
