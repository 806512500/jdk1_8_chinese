/*
 * 版权所有 (c) 2007, 2011, Oracle 和/或其附属公司。保留所有权利。
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

import java.nio.ByteBuffer;
import java.io.IOException;

/**
 * 一个维护当前 <i>位置</i> 并允许更改位置的字节通道。
 *
 * <p> 可寻址字节通道连接到一个实体，通常是文件，该实体包含可以读取和写入的可变长度的字节序列。当前位置可以被 {@link #position() <i>查询</i>} 和
 * {@link #position(long) <i>修改</i>}。通道还提供了访问连接到通道的实体的当前 <i>大小</i> 的方法。当写入的字节超出当前大小时，大小会增加；当它被 {@link #truncate <i>截断</i>} 时，大小会减少。
 *
 * <p> 不需要返回值的 {@link #position(long) 位置} 和 {@link #truncate 截断} 方法被指定为返回调用它们的通道。这允许方法调用被链式调用。此接口的实现应专门化返回类型，以便可以在实现类上链式调用方法。
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
     * <p> 从该通道的当前位置开始读取字节，然后使用实际读取的字节数更新位置。否则，此方法的行为与 {@link
     * ReadableByteChannel} 接口中的指定完全相同。
     */
    @Override
    int read(ByteBuffer dst) throws IOException;

    /**
     * 从给定的缓冲区写入一系列字节到该通道。
     *
     * <p> 从该通道的当前位置开始写入字节，除非通道连接到一个以 {@link java.nio.file.StandardOpenOption#APPEND APPEND} 选项打开的实体（如文件），在这种情况下，位置首先被推进到末尾。如果需要，连接到通道的实体将扩展以容纳写入的字节，然后使用实际写入的字节数更新位置。否则，此方法的行为与 {@link WritableByteChannel} 接口中的指定完全相同。
     */
    @Override
    int write(ByteBuffer src) throws IOException;

    /**
     * 返回此通道的位置。
     *
     * @return 该通道的位置，一个从实体开头到当前位置的非负整数
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    long position() throws IOException;

    /**
     * 设置此通道的位置。
     *
     * <p> 将位置设置为大于当前大小的值是合法的，但不会改变实体的大小。稍后尝试在这样的位置读取字节将立即返回文件结束指示。稍后尝试在这样的位置写入字节将导致实体扩展以容纳新字节；从先前的文件结束到新写入字节之间的任何字节的值是未指定的。
     *
     * <p> 当连接到一个通常以 {@link
     * java.nio.file.StandardOpenOption#APPEND APPEND} 选项打开的实体（如文件）时，不建议设置通道的位置。当以追加模式打开时，写入前位置首先被推进到末尾。
     *
     * @param  newPosition
     *         新位置，一个从实体开头的非负整数
     *
     * @return 该通道
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     * @throws  IllegalArgumentException
     *          如果新位置为负
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    SeekableByteChannel position(long newPosition) throws IOException;

    /**
     * 返回此通道连接的实体的当前大小。
     *
     * @return 当前大小，以字节为单位
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    long size() throws IOException;

    /**
     * 将此通道连接的实体截断到给定的大小。
     *
     * <p> 如果给定的大小小于当前大小，则实体被截断，丢弃任何超出新末尾的字节。如果给定的大小大于或等于当前大小，则实体不会被修改。无论哪种情况，如果当前位置大于给定的大小，则将其设置为该大小。
     *
     * <p> 此接口的实现可能禁止当连接到一个通常以 {@link
     * java.nio.file.StandardOpenOption#APPEND APPEND} 选项打开的实体（如文件）时进行截断。
     *
     * @param  size
     *         新大小，一个非负字节数
     *
     * @return 该通道
     *
     * @throws  NonWritableChannelException
     *          如果此通道未打开用于写入
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     * @throws  IllegalArgumentException
     *          如果新大小为负
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    SeekableByteChannel truncate(long size) throws IOException;
}
