/*
 * 版权所有 (c) 2000, 2007，Oracle 和/或其关联公司。保留所有权利。
 *
 * ORACLE 专有/机密。使用受许可条款的约束。
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

// -- 本文件由机械生成：请勿编辑！ -- //

package java.nio.channels;


/**
 * 当尝试在一个已关闭的组中构造通道，或 I/O 操作的完成处理程序因为通道组已终止而无法调用时抛出的未检查异常。
 *
 * @since 1.7
 */

public class ShutdownChannelGroupException
    extends IllegalStateException
{

    private static final long serialVersionUID = -3903801676350154157L;

    /**
     * 构造此类的一个实例。
     */
    public ShutdownChannelGroupException() { }

}
