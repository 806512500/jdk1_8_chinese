/*
 * 版权所有 (c) 2000, 2007，Oracle 和/或其附属公司。保留所有权利。
 *
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
 *
 */

// -- 本文件由机械生成：不要编辑！ -- //

package java.nio.channels;


/**
 * 当一个线程在 I/O 操作中被阻塞在某个通道或通道的一部分时，如果另一个线程关闭了该通道，则该线程将收到此检查异常。
 *
 * @since 1.4
 */

public class AsynchronousCloseException
    extends ClosedChannelException
{

    private static final long serialVersionUID = 6891178312432313966L;

    /**
     * 构造此类的一个实例。
     */
    public AsynchronousCloseException() { }

}
