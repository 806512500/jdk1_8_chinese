/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
 * TypeVariable 是所有类型变量的公共超接口。
 * 类型变量在第一次被反射方法需要时创建，如本包中所指定。如果类型变量 t 被类型（即类、接口或注解类型）T 引用，并且 T 由 T 的第 n 个外围类声明（参见 JLS 8.1.2），则创建 t 需要解析 T 的第 i 个外围类，其中 i = 0 到 n，包括 n。创建类型变量不应导致其边界被创建。类型变量的重复创建没有效果。
 *
 * <p>在运行时可能会实例化多个对象来表示给定的类型变量。即使类型变量只创建一次，这并不意味着需要缓存表示类型变量的实例。但是，表示类型变量的所有实例必须彼此相等()。因此，类型变量的用户不应依赖于实现此接口的类实例的身份。
 *
 * @param <D> 声明底层类型变量的泛型声明的类型。
 *
 * @since 1.5
 */
public interface TypeVariable<D extends GenericDeclaration> extends Type, AnnotatedElement {
    /**
     * 返回一个 {@code Type} 对象数组，表示此类型变量的上界。注意，如果没有显式声明上界，则上界为 {@code Object}。
     *
     * <p>对于每个上界 B：<ul> <li>如果 B 是参数化类型或类型变量，则会创建它（参见 {@link
     * java.lang.reflect.ParameterizedType ParameterizedType} 了解参数化类型的创建过程的详细信息）。
     * <li>否则，解析 B。  </ul>
     *
     * @throws TypeNotPresentException  如果任何边界引用了不存在的类型声明
     * @throws MalformedParameterizedTypeException 如果任何边界引用了由于任何原因无法实例化的参数化类型
     * @return 一个 {@code Type} 数组，表示此类型变量的上界
    */
    Type[] getBounds();

    /**
     * 返回一个 {@code GenericDeclaration} 对象，表示声明此类型变量的泛型声明。
     *
     * @return 为此类型变量声明的泛型声明。
     *
     * @since 1.5
     */
    D getGenericDeclaration();

    /**
     * 返回此类型变量的名称，如其在源代码中出现的那样。
     *
     * @return 此类型变量的名称，如其在源代码中出现的那样
     */
    String getName();

    /**
     * 返回一个 AnnotatedType 对象数组，表示用于表示此 TypeVariable 所表示的类型参数的上界的类型的使用。数组中对象的顺序对应于类型参数声明中边界出现的顺序。
     *
     * 如果类型参数声明没有边界，则返回长度为 0 的数组。
     *
     * @return 一个表示类型变量上界的对象数组
     * @since 1.8
     */
     AnnotatedType[] getAnnotatedBounds();
}
