/*
 * 版权所有 (c) 2003, Oracle 和/或其附属公司。保留所有权利。
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

package java.util;

/**
 * 当格式化器已被关闭时抛出的未检查异常。
 *
 * <p> 除非另有说明，向此类的任何方法或构造函数传递 <tt>null</tt> 参数将导致抛出 {@link
 * NullPointerException}。
 *
 * @since 1.5
 */
public class FormatterClosedException extends IllegalStateException {

    private static final long serialVersionUID = 18111216L;

    /**
     * 构造此类的一个实例。
     */
    public FormatterClosedException() { }
}