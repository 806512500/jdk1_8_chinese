/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

import java.lang.annotation.*;

/**
 * 一个信息性注解类型，用于表示接口类型声明旨在成为 Java 语言规范中定义的<i>函数式接口</i>。
 *
 * 概念上，一个函数式接口恰好有一个抽象方法。由于 {@linkplain java.lang.reflect.Method#isDefault()
 * 默认方法} 有实现，因此它们不是抽象的。如果接口声明了一个覆盖 {@code java.lang.Object} 的公共方法的抽象方法，
 * 那么这也<em>不</em>计入接口的抽象方法数量，因为任何接口的实现都将从 {@code java.lang.Object} 或其他地方获得实现。
 *
 * <p>请注意，可以使用 lambda 表达式、方法引用或构造函数引用来创建函数式接口的实例。
 *
 * <p>如果一个类型被此注解类型标注，编译器必须生成错误消息，除非：
 *
 * <ul>
 * <li> 该类型是接口类型而不是注解类型、枚举或类。
 * <li> 被注解的类型满足函数式接口的要求。
 * </ul>
 *
 * <p>然而，编译器会将任何符合函数式接口定义的接口视为函数式接口，无论接口声明中是否包含 {@code FunctionalInterface}
 * 注解。
 *
 * @jls 4.3.2. The Class Object
 * @jls 9.8 Functional Interfaces
 * @jls 9.4.3 Interface Method Body
 * @since 1.8
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FunctionalInterface {}
