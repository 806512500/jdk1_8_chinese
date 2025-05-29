/*
 * 版权所有 (c) 2000, 2005, Oracle 和/或其附属公司。保留所有权利。
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
 * 可以写入字节的通道。
 *
 * <p> 在任何给定时间，可写通道上只能有一个写操作进行。如果一个线程启动了一个写操作，那么任何尝试启动另一个写操作的线程将阻塞，直到第一个操作完成。其他类型的 I/O 操作是否可以与写操作同时进行取决于通道的类型。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
 * @since 1.4
 */

public interface WritableByteChannel
    extends Channel
{

    /**
     * 从给定的缓冲区写入一系列字节到此通道。
     *
     * <p> 尝试向通道写入最多 <i>r</i> 个字节，其中 <i>r</i> 是缓冲区中剩余的字节数，即 <tt>src.remaining()</tt>，在调用此方法时的值。
     *
     * <p> 假设写入了一个长度为 <i>n</i> 的字节序列，其中
     * <tt>0</tt>&nbsp;<tt>&lt;=</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>。
     * 这个字节序列将从索引 <i>p</i> 开始从缓冲区传输，其中 <i>p</i> 是调用此方法时缓冲区的位置；最后一个写入的字节的索引将是
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>。
     * 返回时，缓冲区的位置将等于
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>；其限制不会改变。
     *
     * <p> 除非另有说明，否则写操作将在写入所有 <i>r</i> 个请求的字节后返回。某些类型的通道，根据其状态，可能只能写入部分字节或根本不写入。例如，处于非阻塞模式的套接字通道不能写入超过套接字输出缓冲区中可用的字节数。
     *
     * <p> 本方法可以在任何时候调用。但是，如果另一个线程已经在此通道上启动了一个写操作，那么调用本方法将阻塞，直到第一个操作完成。 </p>
     *
     * @param  src
     *         从中检索字节的缓冲区
     *
     * @return 写入的字节数，可能为零
     *
     * @throws  NonWritableChannelException
     *          如果此通道未打开以进行写入
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
    public int write(ByteBuffer src) throws IOException;

}
