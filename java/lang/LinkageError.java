/*
 * 版权所有 (c) 1995, 2010, Oracle 和/或其附属公司。保留所有权利。
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
 * {@code LinkageError} 的子类表示一个类对另一个类有依赖关系；然而，后者类在前者类编译后发生了不兼容的更改。
 *
 *
 * @author  Frank Yellin
 * @since   JDK1.0
 */
public
class LinkageError extends Error {
    private static final long serialVersionUID = 3579600108157160122L;

    /**
     * 构造一个没有详细消息的 {@code LinkageError}。
     */
    public LinkageError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 {@code LinkageError}。
     *
     * @param   s   详细消息。
     */
    public LinkageError(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和原因构造一个 {@code LinkageError}。
     *
     * @param s     详细消息。
     * @param cause 原因，可能是 {@code null}
     * @since 1.7
     */
    public LinkageError(String s, Throwable cause) {
        super(s, cause);
    }
}
