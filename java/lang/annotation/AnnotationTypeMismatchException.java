/*
 * 版权所有 (c) 2003, 2008, Oracle 和/或其关联公司。保留所有权利。
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

package java.lang.annotation;
import java.lang.reflect.Method;

/**
 * 抛出此异常表示程序尝试访问注解的一个元素，而该元素的类型在注解编译（或序列化）后发生了变化。
 * 此异常可以由 {@linkplain
 * java.lang.reflect.AnnotatedElement 用于反射读取注解的 API} 抛出。
 *
 * @author  Josh Bloch
 * @see     java.lang.reflect.AnnotatedElement
 * @since 1.5
 */
public class AnnotationTypeMismatchException extends RuntimeException {
    private static final long serialVersionUID = 8125925355765570191L;

    /**
     * 注解元素的 <tt>Method</tt> 对象。
     */
    private final Method element;

    /**
     * 在注解中找到的数据的（错误的）类型。此字符串可能包含值，但不要求包含。字符串的确切格式未指定。
     */
    private final String foundType;

    /**
     * 构造一个指定注解类型元素和找到的数据类型的 AnnotationTypeMismatchException。
     *
     * @param element 注解元素的 <tt>Method</tt> 对象
     * @param foundType 在注解中找到的数据的（错误的）类型。此字符串可能包含值，但不要求包含。字符串的确切格式未指定。
     */
    public AnnotationTypeMismatchException(Method element, String foundType) {
        super("在注解元素 " + element + " 中找到类型不正确的数据 (找到的数据类型为 " + foundType + ")");
        this.element = element;
        this.foundType = foundType;
    }

    /**
     * 返回类型不正确的元素的 <tt>Method</tt> 对象。
     *
     * @return 类型不正确的元素的 <tt>Method</tt> 对象
     */
    public Method element() {
        return this.element;
    }

    /**
     * 返回类型不正确的元素中找到的数据类型。返回的字符串可能包含值，但不要求包含。字符串的确切格式未指定。
     *
     * @return 类型不正确的元素中找到的数据类型
     */
    public String foundType() {
        return this.foundType;
    }
}
