/*
 * 版权所有 (c) 1996, 2011, Oracle 和/或其附属公司。保留所有权利。
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
 * {@code Void} 类是一个不可实例化的占位符类，用于保存表示 Java 关键字
 * void 的 {@code Class} 对象。
 *
 * @author  未署名
 * @since   JDK1.1
 */
public final
class Void {

    /**
     * 表示与关键字 {@code void} 对应的伪类型的 {@code Class} 对象。
     */
    @SuppressWarnings("unchecked")
    public static final Class<Void> TYPE = (Class<Void>) Class.getPrimitiveClass("void");

    /*
     * Void 类不能被实例化。
     */
    private Void() {}
}