/*
 * 版权所有 (c) 2003, 2004, Oracle 和/或其子公司。保留所有权利。
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
 * {@code GenericArrayType} 表示一个组件类型为参数化类型或类型变量的数组类型。
 * @since 1.5
 */
public interface GenericArrayType extends Type {
    /**
     * 返回表示此数组组件类型的 {@code Type} 对象。此方法创建数组的组件类型。
     * 有关参数化类型创建过程的语义，请参见 {@link
     * java.lang.reflect.ParameterizedType ParameterizedType} 的声明，
     * 有关类型变量的创建过程，请参见 {@link java.lang.reflect.TypeVariable TypeVariable}。
     *
     * @return  表示此数组组件类型的 {@code Type} 对象
     * @throws TypeNotPresentException 如果底层数组类型的组件类型引用了不存在的类型声明
     * @throws MalformedParameterizedTypeException 如果底层数组类型的组件类型引用了
     *     由于任何原因无法实例化的参数化类型
     */
    Type getGenericComponentType();
}
