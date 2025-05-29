/*
 * 版权所有 (c) 2000, 2011, Oracle 及/或其附属公司。保留所有权利。
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

package java.lang;

/**
 * 抛出以表示断言失败。
 *
 * <p>该类提供的七个单参数公共构造函数确保通过以下调用返回的断言错误：
 * <pre>
 *     new AssertionError(<i>expression</i>)
 * </pre>
 * 其详细消息为 <i>expression</i> 的 <i>字符串转换</i>（如《Java&trade; 语言规范》第 15.18.1.1 节所定义），
 * 无论 <i>expression</i> 的类型如何。
 *
 * @since   1.4
 */
public class AssertionError extends Error {
    private static final long serialVersionUID = -5013299493970297370L;

    /**
     * 构造一个没有详细消息的 AssertionError。
     */
    public AssertionError() {
    }

    /**
     * 此内部构造函数不对字符串参数进行任何处理，即使它是 null 引用。公共构造函数永远不会用 null 参数调用此构造函数。
     */
    private AssertionError(String detailMessage) {
        super(detailMessage);
    }

    /**
     * 构造一个其详细消息从指定对象派生的 AssertionError，该对象根据《Java&trade; 语言规范》第 15.18.1.1 节定义转换为字符串。
     * <p>
     * 如果指定的对象是 {@code Throwable} 的实例，则它将成为新构造的断言错误的 <i>原因</i>。
     *
     * @param detailMessage 用于构建详细消息的值
     * @see   Throwable#getCause()
     */
    public AssertionError(Object detailMessage) {
        this(String.valueOf(detailMessage));
        if (detailMessage instanceof Throwable)
            initCause((Throwable) detailMessage);
    }

    /**
     * 构造一个其详细消息从指定的 <code>boolean</code> 派生的 AssertionError，该 <code>boolean</code> 根据《Java&trade; 语言规范》第 15.18.1.1 节定义转换为字符串。
     *
     * @param detailMessage 用于构建详细消息的值
     */
    public AssertionError(boolean detailMessage) {
        this(String.valueOf(detailMessage));
    }

    /**
     * 构造一个其详细消息从指定的 <code>char</code> 派生的 AssertionError，该 <code>char</code> 根据《Java&trade; 语言规范》第 15.18.1.1 节定义转换为字符串。
     *
     * @param detailMessage 用于构建详细消息的值
     */
    public AssertionError(char detailMessage) {
        this(String.valueOf(detailMessage));
    }

    /**
     * 构造一个其详细消息从指定的 <code>int</code> 派生的 AssertionError，该 <code>int</code> 根据《Java&trade; 语言规范》第 15.18.1.1 节定义转换为字符串。
     *
     * @param detailMessage 用于构建详细消息的值
     */
    public AssertionError(int detailMessage) {
        this(String.valueOf(detailMessage));
    }

    /**
     * 构造一个其详细消息从指定的 <code>long</code> 派生的 AssertionError，该 <code>long</code> 根据《Java&trade; 语言规范》第 15.18.1.1 节定义转换为字符串。
     *
     * @param detailMessage 用于构建详细消息的值
     */
    public AssertionError(long detailMessage) {
        this(String.valueOf(detailMessage));
    }

    /**
     * 构造一个其详细消息从指定的 <code>float</code> 派生的 AssertionError，该 <code>float</code> 根据《Java&trade; 语言规范》第 15.18.1.1 节定义转换为字符串。
     *
     * @param detailMessage 用于构建详细消息的值
     */
    public AssertionError(float detailMessage) {
        this(String.valueOf(detailMessage));
    }

    /**
     * 构造一个其详细消息从指定的 <code>double</code> 派生的 AssertionError，该 <code>double</code> 根据《Java&trade; 语言规范》第 15.18.1.1 节定义转换为字符串。
     *
     * @param detailMessage 用于构建详细消息的值
     */
    public AssertionError(double detailMessage) {
        this(String.valueOf(detailMessage));
    }

    /**
     * 构造一个新的 {@code AssertionError}，具有指定的详细消息和原因。
     *
     * <p>请注意，与 {@code cause} 关联的详细消息 <i>不会</i> 自动包含在此错误的详细消息中。
     *
     * @param  message 详细消息，可以为 {@code null}
     * @param  cause 原因，可以为 {@code null}
     *
     * @since 1.7
     */
    public AssertionError(String message, Throwable cause) {
        super(message, cause);
    }
}