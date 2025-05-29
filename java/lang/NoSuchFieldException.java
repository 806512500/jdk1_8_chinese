/*
 * 版权所有 (c) 1996, 2008, Oracle 和/或其附属公司。保留所有权利。
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
 * 表示类中没有指定名称的字段。
 *
 * @author 未署名
 * @since JDK1.1
 */
public class NoSuchFieldException extends ReflectiveOperationException {
    private static final long serialVersionUID = -6143714805279938260L;

    /**
     * 构造函数。
     */
    public NoSuchFieldException() {
        super();
    }

    /**
     * 带有详细消息的构造函数。
     *
     * @param s 详细消息
     */
    public NoSuchFieldException(String s) {
        super(s);
    }
}