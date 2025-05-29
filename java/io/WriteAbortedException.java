/*
 * 版权所有 (c) 1996, 2005, Oracle 和/或其附属公司。保留所有权利。
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

package java.io;

/**
 * 表示在写操作期间抛出了 ObjectStreamExceptions 之一。在读操作期间，当写操作期间抛出了 ObjectStreamExceptions 之一时抛出。
 * 终止写操作的异常可以在 detail 字段中找到。流将重置为其初始状态，所有已反序列化的对象引用都将被丢弃。
 *
 * <p>从 1.4 版本开始，此异常已被改造以符合通用的异常链机制。构造时提供的“导致中止的异常”
 * 以及通过公共 {@link #detail} 字段访问的异常现在被称为<i>原因</i>，并且可以通过 {@link Throwable#getCause()}
 * 方法以及上述“遗留字段”访问。
 *
 * @author  未署名
 * @since   JDK1.1
 */
public class WriteAbortedException extends ObjectStreamException {
    private static final long serialVersionUID = -3326426625597282442L;

    /**
     * 在写 ObjectStream 时捕获的异常。
     *
     * <p>此字段早于通用异常链机制。现在 {@link Throwable#getCause()} 方法是获取此信息的首选方式。
     *
     * @serial
     */
    public Exception detail;

    /**
     * 使用描述异常的字符串和导致中止的异常构造 WriteAbortedException。
     * @param s   描述异常的字符串。
     * @param ex  导致中止的异常。
     */
    public WriteAbortedException(String s, Exception ex) {
        super(s);
        initCause(null);  // 禁止后续的 initCause
        detail = ex;
    }

    /**
     * 生成消息并包括嵌套异常的消息（如果有）。
     */
    public String getMessage() {
        if (detail == null)
            return super.getMessage();
        else
            return super.getMessage() + "; " + detail.toString();
    }

    /**
     * 返回终止操作的异常（<i>原因</i>）。
     *
     * @return  终止操作的异常（<i>原因</i>），可能为 null。
     * @since   1.4
     */
    public Throwable getCause() {
        return detail;
    }
}
