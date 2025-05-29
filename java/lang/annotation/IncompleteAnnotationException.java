/*
 * 版权所有 (c) 2003, 2013，Oracle 和/或其附属公司。保留所有权利。
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

/**
 * 抛出此异常以指示程序尝试访问在注解编译（或序列化）后添加到注解类型定义中的元素。如果新元素有默认值，则不会抛出此异常。
 * 此异常可以由 {@linkplain
 * java.lang.reflect.AnnotatedElement 用于反射读取注解的API} 抛出。
 *
 * @author  Josh Bloch
 * @see     java.lang.reflect.AnnotatedElement
 * @since 1.5
 */
public class IncompleteAnnotationException extends RuntimeException {
    private static final long serialVersionUID = 8445097402741811912L;

    private Class<? extends Annotation> annotationType;
    private String elementName;

    /**
     * 构造一个 IncompleteAnnotationException，以指示在指定的注解类型中缺少命名的元素。
     *
     * @param annotationType 注解类型的 Class 对象
     * @param elementName 缺少的元素的名称
     * @throws NullPointerException 如果任一参数为 {@code null}
     */
    public IncompleteAnnotationException(
            Class<? extends Annotation> annotationType,
            String elementName) {
        super(annotationType.getName() + " 缺少元素 " +
              elementName.toString());

        this.annotationType = annotationType;
        this.elementName = elementName;
    }

    /**
     * 返回缺少元素的注解类型的 Class 对象。
     *
     * @return 缺少元素的注解类型的 Class 对象
     */
    public Class<? extends Annotation> annotationType() {
        return annotationType;
    }

    /**
     * 返回缺少的元素的名称。
     *
     * @return 缺少的元素的名称
     */
    public String elementName() {
        return elementName;
    }
}
