/*
 * 版权所有 (c) 1995, 2008, Oracle 和/或其关联公司。保留所有权利。
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
 * 抛出以指示在类 <code>Object</code> 中调用的 <code>clone</code> 方法克隆了一个对象，
 * 但该对象的类未实现 <code>Cloneable</code> 接口。
 * <p>
 * 覆盖 <code>clone</code> 方法的应用程序也可以抛出此异常，以指示对象不能或不应该被克隆。
 *
 * @author  未署名
 * @see     java.lang.Cloneable
 * @see     java.lang.Object#clone()
 * @since   JDK1.0
 */

public
class CloneNotSupportedException extends Exception {
    private static final long serialVersionUID = 5195511250079656443L;

    /**
     * 构造一个没有详细消息的 <code>CloneNotSupportedException</code>。
     */
    public CloneNotSupportedException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>CloneNotSupportedException</code>。
     *
     * @param   s   详细消息。
     */
    public CloneNotSupportedException(String s) {
        super(s);
    }
}
