/*
 * 版权所有 (c) 1995, 2004, Oracle 和/或其附属公司。保留所有权利。
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
 * 当应用程序尝试通过类的字符串名称加载类时抛出此异常，使用以下方法：
 * <ul>
 * <li>类 <code>Class</code> 中的 <code>forName</code> 方法。
 * <li>类 <code>ClassLoader</code> 中的 <code>findSystemClass</code> 方法。
 * <li>类 <code>ClassLoader</code> 中的 <code>loadClass</code> 方法。
 * </ul>
 * <p>
 * 但未找到指定名称的类定义。
 *
 * <p>从 1.4 版本开始，此异常已更新以符合通用的异常链机制。构造时可提供的“加载类时引发的可选异常”现在称为<i>原因</i>，可以通过 {@link #getException()} 方法访问，也可以通过 {@link
 * Throwable#getCause()} 方法以及上述“遗留方法”访问。
 *
 * @author 未署名
 * @see java.lang.Class#forName(java.lang.String)
 * @see java.lang.ClassLoader#findSystemClass(java.lang.String)
 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
 * @since JDK1.0
 */
public class ClassNotFoundException extends ReflectiveOperationException {
    /**
     * 为了互操作性，使用 JDK 1.1.X 的 serialVersionUID
     */
     private static final long serialVersionUID = 9176873029745254542L;

    /**
     * 如果使用了 ClassNotFoundException(String s, Throwable ex) 构造函数实例化对象，则此字段保存异常 ex
     * @serial
     * @since 1.2
     */
    private Throwable ex;

    /**
     * 构造一个没有详细消息的 <code>ClassNotFoundException</code>。
     */
    public ClassNotFoundException() {
        super((Throwable)null);  // 禁用 initCause
    }

    /**
     * 使用指定的详细消息构造一个 <code>ClassNotFoundException</code>。
     *
     * @param   s   详细消息。
     */
    public ClassNotFoundException(String s) {
        super(s, null);  //  禁用 initCause
    }

    /**
     * 使用指定的详细消息和加载类时可能引发的可选异常构造一个 <code>ClassNotFoundException</code>。
     *
     * @param s 详细消息
     * @param ex 加载类时引发的异常
     * @since 1.2
     */
    public ClassNotFoundException(String s, Throwable ex) {
        super(s, null);  //  禁用 initCause
        this.ex = ex;
    }

    /**
     * 如果尝试加载类时发生错误，返回引发的异常。否则，返回 <tt>null</tt>。
     *
     * <p>此方法早于通用的异常链机制。现在 {@link Throwable#getCause()} 方法是获取此信息的首选方式。
     *
     * @return 加载类时引发的 <code>Exception</code>
     * @since 1.2
     */
    public Throwable getException() {
        return ex;
    }

    /**
     * 返回此异常的原因（尝试加载类时引发的异常；否则 <tt>null</tt>）。
     *
     * @return 此异常的原因。
     * @since 1.4
     */
    public Throwable getCause() {
        return ex;
    }
}
