/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * 表示注解类型适用的上下文。注解类型可能适用的声明上下文和类型上下文在 JLS 9.6.4.1 中指定，并在源代码中由 {@link ElementType java.lang.annotation.ElementType} 枚举常量表示。
 *
 * <p>如果注解类型 {@code T} 没有 {@code @Target} 元注解，则类型 {@code T} 的注解可以作为任何声明的修饰符，但类型参数声明除外。
 *
 * <p>如果存在 {@code @Target} 元注解，编译器将根据 JLS 9.7.4 中的 {@code ElementType} 枚举常量指示的使用限制进行强制。
 *
 * <p>例如，此 {@code @Target} 元注解表示声明的类型本身是一个元注解类型。它只能用于注解类型声明：
 * <pre>
 *    &#064;Target(ElementType.ANNOTATION_TYPE)
 *    public &#064;interface MetaAnnotationType {
 *        ...
 *    }
 * </pre>
 *
 * <p>此 {@code @Target} 元注解表示声明的类型仅用于复杂注解类型声明中的成员类型。它不能直接用于注解任何内容：
 * <pre>
 *    &#064;Target({})
 *    public &#064;interface MemberType {
 *        ...
 *    }
 * </pre>
 *
 * <p>如果单个 {@code ElementType} 常量在 {@code @Target} 注解中出现多次，则会导致编译时错误。例如，以下 {@code @Target} 元注解是非法的：
 * <pre>
 *    &#064;Target({ElementType.FIELD, ElementType.METHOD, ElementType.FIELD})
 *    public &#064;interface Bogus {
 *        ...
 *    }
 * </pre>
 *
 * @since 1.5
 * @jls 9.6.4.1 @Target
 * @jls 9.7.4 注解可能出现的位置
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Target {
    /**
     * 返回注解类型可以应用到的元素种类数组。
     * @return 注解类型可以应用到的元素种类数组
     */
    ElementType[] value();
}
