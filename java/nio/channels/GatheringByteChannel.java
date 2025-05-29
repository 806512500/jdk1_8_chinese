/*
 * 版权所有 (c) 2000, 2001, Oracle 和/或其附属公司。保留所有权利。
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
import java.nio.ByteBuffer;


/**
 * 一个可以从一系列缓冲区写入字节的通道。
 *
 * <p> 一个 <i>聚集</i> 写操作在单次调用中从一个或多个给定缓冲区序列中写入一系列字节。
 * 聚集写操作在实现网络协议或文件格式时通常很有用，例如，将数据分组为一个或多个固定长度的头部后跟一个可变长度的主体。在 {@link
 * ScatteringByteChannel} 接口中定义了类似的 <i>分散</i> 读操作。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
 * @since 1.4
 */

public interface GatheringByteChannel
    extends WritableByteChannel
{

    /**
     * 从给定缓冲区的子序列中写入一系列字节到此通道。
     *
     * <p> 尝试将最多 <i>r</i> 个字节写入此通道，其中 <i>r</i> 是给定缓冲区数组中指定子序列中剩余的总字节数，即，
     *
     * <blockquote><pre>
     * srcs[offset].remaining()
     *     + srcs[offset+1].remaining()
     *     + ... + srcs[offset+length-1].remaining()</pre></blockquote>
     *
     * 在调用此方法时。
     *
     * <p> 假设写入了一个长度为 <i>n</i> 的字节序列，其中
     * <tt>0</tt>&nbsp;<tt>&lt;=</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>。
     * 该序列的前 <tt>srcs[offset].remaining()</tt> 个字节从缓冲区 <tt>srcs[offset]</tt> 写入，接下来的
     * <tt>srcs[offset+1].remaining()</tt> 个字节从缓冲区 <tt>srcs[offset+1]</tt> 写入，依此类推，直到整个字节序列写入完毕。尽可能多的字节从每个缓冲区写入，因此除了最后一个更新的缓冲区外，每个更新的缓冲区的最终位置保证等于该缓冲区的限制。
     *
     * <p> 除非另有说明，否则写操作只有在写入所有 <i>r</i> 个请求的字节后才会返回。某些类型的通道，根据其状态，可能只写入部分字节或可能一个字节也不写。例如，非阻塞模式下的套接字通道不能写入超过套接字输出缓冲区中空闲空间的字节数。
     *
     * <p> 本方法可以在任何时候调用。但是，如果另一个线程已经在此通道上启动了一个写操作，那么本方法的调用将阻塞，直到第一个操作完成。 </p>
     *
     * @param  srcs
     *         要从中检索字节的缓冲区
     *
     * @param  offset
     *         要从中检索字节的第一个缓冲区在缓冲区数组中的偏移量；必须是非负数且不大于 <tt>srcs.length</tt>
     *
     * @param  length
     *         要访问的最大缓冲区数；必须是非负数且不大于
     *         <tt>srcs.length</tt>&nbsp;-&nbsp;<tt>offset</tt>
     *
     * @return  写入的字节数，可能为零
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>offset</tt> 和 <tt>length</tt> 参数的前置条件不成立
     *
     * @throws  NonWritableChannelException
     *          如果此通道未打开以供写入
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果另一个线程在写操作进行时关闭此通道
     *
     * @throws  ClosedByInterruptException
     *          如果另一个线程在写操作进行时中断当前线程，从而关闭通道并设置当前线程的中断状态
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public long write(ByteBuffer[] srcs, int offset, int length)
        throws IOException;


    /**
     * 从给定缓冲区写入一系列字节到此通道。
     *
     * <p> 本方法的调用形式 <tt>c.write(srcs)</tt> 的行为与调用
     *
     * <blockquote><pre>
     * c.write(srcs, 0, srcs.length);</pre></blockquote>
     *
     * 完全相同。
     *
     * @param  srcs
     *         要从中检索字节的缓冲区
     *
     * @return  写入的字节数，可能为零
     *
     * @throws  NonWritableChannelException
     *          如果此通道未打开以供写入
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果另一个线程在写操作进行时关闭此通道
     *
     * @throws  ClosedByInterruptException
     *          如果另一个线程在写操作进行时中断当前线程，从而关闭通道并设置当前线程的中断状态
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public long write(ByteBuffer[] srcs) throws IOException;

}
