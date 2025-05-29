/*
 * 版权所有 (c) 1994, 2008, Oracle 和/或其附属公司。保留所有权利。
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
 * 由 <code>Stack</code> 类中的方法抛出，以指示栈为空。
 *
 * @author  Jonathan Payne
 * @see     java.util.Stack
 * @since   JDK1.0
 */
public
class EmptyStackException extends RuntimeException {
    private static final long serialVersionUID = 5084686378493302095L;

    /**
     * 构造一个新的 <code>EmptyStackException</code>，其错误消息字符串为 <tt>null</tt>。
     */
    public EmptyStackException() {
    }
}