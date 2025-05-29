/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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
import java.nio.channels.spi.*;


/**
 * 实现单向管道的一对通道。
 *
 * <p> 管道由一对通道组成：一个可写 {@link
 * Pipe.SinkChannel 汇} 通道和一个可读 {@link Pipe.SourceChannel 源} 通道。一旦有一些字节写入汇通道，它们就可以从源通道中以完全相同的顺序读取。
 *
 * <p> 写入管道的线程是否会阻塞，直到另一个线程从管道中读取这些字节或之前写入的字节，这是系统依赖的，因此未指定。许多管道实现将在汇通道和源通道之间缓冲一定数量的字节，但不应假设这种缓冲。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
 * @since 1.4
 */

public abstract class Pipe {

    /**
     * 表示 {@link Pipe} 可读端的通道。
     *
     * @since 1.4
     */
    public static abstract class SourceChannel
        extends AbstractSelectableChannel
        implements ReadableByteChannel, ScatteringByteChannel
    {
        /**
         * 构造此类的新实例。
         *
         * @param  provider
         *         选择器提供者
         */
        protected SourceChannel(SelectorProvider provider) {
            super(provider);
        }

        /**
         * 返回一个操作集，标识此通道支持的操作。
         *
         * <p> 管道源通道仅支持读取，因此此方法返回 {@link SelectionKey#OP_READ}。 </p>
         *
         * @return  有效的操作集
         */
        public final int validOps() {
            return SelectionKey.OP_READ;
        }

    }

    /**
     * 表示 {@link Pipe} 可写端的通道。
     *
     * @since 1.4
     */
    public static abstract class SinkChannel
        extends AbstractSelectableChannel
        implements WritableByteChannel, GatheringByteChannel
    {
        /**
         * 初始化此类的新实例。
         *
         * @param  provider
         *         选择器提供者
         */
        protected SinkChannel(SelectorProvider provider) {
            super(provider);
        }

        /**
         * 返回一个操作集，标识此通道支持的操作。
         *
         * <p> 管道汇通道仅支持写入，因此此方法返回 {@link SelectionKey#OP_WRITE}。 </p>
         *
         * @return  有效的操作集
         */
        public final int validOps() {
            return SelectionKey.OP_WRITE;
        }

    }

    /**
     * 初始化此类的新实例。
     */
    protected Pipe() { }

    /**
     * 返回此管道的源通道。
     *
     * @return  此管道的源通道
     */
    public abstract SourceChannel source();

    /**
     * 返回此管道的汇通道。
     *
     * @return  此管道的汇通道
     */
    public abstract SinkChannel sink();

    /**
     * 打开一个管道。
     *
     * <p> 通过调用系统默认的 {@link java.nio.channels.spi.SelectorProvider}
     * 对象的 {@link java.nio.channels.spi.SelectorProvider#openPipe openPipe} 方法来创建新的管道。 </p>
     *
     * @return  一个新的管道
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public static Pipe open() throws IOException {
        return SelectorProvider.provider().openPipe();
    }

}
