/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * Member 是一个接口，用于反映关于单个成员（字段或方法）或构造函数的标识信息。
 *
 * @see java.lang.Class
 * @see Field
 * @see Method
 * @see Constructor
 *
 * @author Nakul Saraiya
 */
public
interface Member {

    /**
     * 识别类或接口的所有公共成员的集合，包括继承的成员。
     */
    public static final int PUBLIC = 0;

    /**
     * 识别类或接口的声明成员的集合。不包括继承的成员。
     */
    public static final int DECLARED = 1;

    /**
     * 返回表示声明此 Member 代表的成员或构造函数的类或接口的 Class 对象。
     *
     * @return 一个表示底层成员的声明类的对象
     */
    public Class<?> getDeclaringClass();

    /**
     * 返回此 Member 代表的底层成员或构造函数的简单名称。
     *
     * @return 底层成员的简单名称
     */
    public String getName();

    /**
     * 以整数形式返回此 Member 代表的成员或构造函数的 Java 语言修饰符。应使用 Modifier 类来解码整数中的修饰符。
     *
     * @return 底层成员的 Java 语言修饰符
     * @see Modifier
     */
    public int getModifiers();

    /**
     * 如果此成员是由编译器引入的，则返回 {@code true}；否则返回 {@code false}。
     *
     * @return 如果且仅如果此成员是由编译器引入的，则返回 true。
     * @jls 13.1 二进制的形式
     * @since 1.5
     */
    public boolean isSynthetic();
}
