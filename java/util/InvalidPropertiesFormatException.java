/*
 * 版权所有 (c) 2003, 2012, Oracle 和/或其关联公司。保留所有权利。
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

import java.io.NotSerializableException;
import java.io.IOException;

/**
 * 抛出此异常表示操作无法完成，因为输入不符合 {@link Properties}
 * 规范中定义的属性集合的适当 XML 文档类型。<p>
 *
 * 注意，虽然 InvalidPropertiesFormatException 继承了 Exception 的 Serializable 接口，但它并不打算被序列化。实现了适当的序列化方法来抛出 NotSerializableException。
 *
 * @see     Properties
 * @since   1.5
 * @serial exclude
 */

public class InvalidPropertiesFormatException extends IOException {

    private static final long serialVersionUID = 7763056076009360219L;

    /**
     * 使用指定的原因构造一个 InvalidPropertiesFormatException。
     *
     * @param  cause 原因（将保存以供 {@link Throwable#getCause()} 方法稍后检索）。
     */
    public InvalidPropertiesFormatException(Throwable cause) {
        super(cause==null ? null : cause.toString());
        this.initCause(cause);
    }

   /**
    * 使用指定的详细消息构造一个 InvalidPropertiesFormatException。
    *
    * @param   message 详细消息。详细消息将保存以供 {@link Throwable#getMessage()} 方法稍后检索。
    */
    public InvalidPropertiesFormatException(String message) {
        super(message);
    }

    /**
     * 抛出 NotSerializableException，因为 InvalidPropertiesFormatException
     * 对象不打算被序列化。
     */
    private void writeObject(java.io.ObjectOutputStream out)
        throws NotSerializableException
    {
        throw new NotSerializableException("不可序列化。");
    }

    /**
     * 抛出 NotSerializableException，因为 InvalidPropertiesFormatException
     * 对象不打算被序列化。
     */
    private void readObject(java.io.ObjectInputStream in)
        throws NotSerializableException
    {
        throw new NotSerializableException("不可序列化。");
    }

}