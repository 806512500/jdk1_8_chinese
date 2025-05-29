/*
 * 版权所有 (c) 1994, 2008, Oracle 和/或其子公司。保留所有权利。
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
 * 如果 Java 虚拟机或 <code>ClassLoader</code> 实例尝试加载类的定义（作为正常方法调用的一部分
 * 或作为使用 <code>new</code> 表达式创建新实例的一部分）但找不到类的定义时抛出此异常。
 * <p>
 * 在当前执行的类编译时，所查找的类定义存在，但定义已无法找到。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public
class NoClassDefFoundError extends LinkageError {
    private static final long serialVersionUID = 9095859863287012458L;

    /**
     * 构造一个没有详细消息的 <code>NoClassDefFoundError</code>。
     */
    public NoClassDefFoundError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>NoClassDefFoundError</code>。
     *
     * @param   s   详细消息。
     */
    public NoClassDefFoundError(String s) {
        super(s);
    }
}
