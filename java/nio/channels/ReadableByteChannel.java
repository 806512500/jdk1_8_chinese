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
 * 可以读取字节的通道。
 *
 * <p> 在任何给定时间，可读通道上只能进行一次读取操作。如果一个线程在通道上启动了一个读取操作，
 * 那么任何尝试启动另一个读取操作的线程将阻塞，直到第一个操作完成。其他类型的 I/O 操作是否可以
 * 与读取操作同时进行取决于通道的类型。 </p>
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
     * 即 <tt>dst.remaining()</tt>，在调用此方法时的值。
     *
     * <p> 假设读取了一个长度为 <i>n</i> 的字节序列，其中
     * <tt>0</tt>&nbsp;<tt>&lt;=</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>。
     * 该字节序列将被传输到缓冲区，使得序列中的第一个字节位于索引 <i>p</i>，最后一个字节位于索引
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>，
     * 其中 <i>p</i> 是调用此方法时缓冲区的位置。返回时，缓冲区的位置将等于
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>；其限制不会改变。
     *
     * <p> 读取操作可能不会填满缓冲区，实际上可能根本不会读取任何字节。这取决于通道的性质和状态。
     * 例如，处于非阻塞模式的套接字通道不能读取比套接字输入缓冲区中立即可用的字节更多的字节；
 * 同样，文件通道不能读取比文件中剩余的字节更多的字节。然而，可以保证，如果通道处于阻塞模式，并且缓冲区中至少有一个字节剩余，
 * 那么此方法将阻塞，直到至少读取一个字节。
     *
     * <p> 该方法可以在任何时候调用。但是，如果另一个线程已经在此通道上启动了一个读取操作，
 * 那么调用此方法将阻塞，直到第一个操作完成。 </p>
     *
     * @param  dst
     *         要传输字节的缓冲区
     *
     * @return  读取的字节数，可能为零，或者如果通道已到达流的末尾则返回 <tt>-1</tt>
     *
     * @throws  NonReadableChannelException
     *          如果此通道未打开以进行读取
     *
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     *
     * @throws  AsynchronousCloseException
     *          如果另一个线程在读取操作进行中关闭了此通道
     *
     * @throws  ClosedByInterruptException
     *          如果另一个线程在读取操作进行中中断了当前线程，从而关闭了通道并设置了当前线程的中断状态
     *
     * @throws  IOException
     *          如果发生其他 I/O 错误
     */
    public int read(ByteBuffer dst) throws IOException;

}
