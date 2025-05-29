/*
 * 版权所有 (c) 2003, 2004, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang.reflect;

/**
 * WildcardType 表示一个通配符类型表达式，例如
 * {@code ?}, {@code ? extends Number}, 或 {@code ? super Integer}。
 *
 * @since 1.5
 */
public interface WildcardType extends Type {
    /**
     * 返回一个 {@code Type} 对象数组，表示此类型变量的上界。注意，如果没有显式声明上界，
     * 上界是 {@code Object}。
     *
     * <p>对于每个上界 B：
     * <ul>
     *  <li>如果 B 是参数化类型或类型变量，则创建它，
     *  (参见 {@link java.lang.reflect.ParameterizedType ParameterizedType}
     *  了解参数化类型的创建过程)。
     *  <li>否则，解析 B。
     * </ul>
     *
     * @return 一个 Types 数组，表示此类型变量的上界
     * @throws TypeNotPresentException 如果任何边界引用了不存在的类型声明
     * @throws MalformedParameterizedTypeException 如果任何边界引用了无法实例化的参数化类型
     */
    Type[] getUpperBounds();

    /**
     * 返回一个 {@code Type} 对象数组，表示此类型变量的下界。注意，如果没有显式声明下界，
     * 下界是 {@code null} 的类型。在这种情况下，返回一个零长度的数组。
     *
     * <p>对于每个下界 B：
     * <ul>
     *   <li>如果 B 是参数化类型或类型变量，则创建它，
     *  (参见 {@link java.lang.reflect.ParameterizedType ParameterizedType}
     *  了解参数化类型的创建过程)。
     *   <li>否则，解析 B。
     * </ul>
     *
     * @return 一个 Types 数组，表示此类型变量的下界
     * @throws TypeNotPresentException 如果任何边界引用了不存在的类型声明
     * @throws MalformedParameterizedTypeException 如果任何边界引用了无法实例化的参数化类型
     */
    Type[] getLowerBounds();
    // 一个或多个？取决于语言规范；目前只有一个，但此 API
    // 允许泛化。
}
