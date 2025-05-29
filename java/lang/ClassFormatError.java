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

package java.lang;

/**
 * 当 Java 虚拟机尝试读取一个类文件并确定该文件格式错误或无法被解释为类文件时抛出。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public
class ClassFormatError extends LinkageError {
    private static final long serialVersionUID = -8420114879011949195L;

    /**
     * 构造一个没有详细信息消息的 <code>ClassFormatError</code>。
     */
    public ClassFormatError() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 <code>ClassFormatError</code>。
     *
     * @param   s   详细信息消息。
     */
    public ClassFormatError(String s) {
        super(s);
    }
}
