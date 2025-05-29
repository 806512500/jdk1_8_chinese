/*
 * Copyright (c) 2004, 2011, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 当应用程序尝试通过名称访问枚举常量，而枚举类型中不包含指定名称的常量时抛出。
 * 此异常可以由 {@linkplain
 * java.lang.reflect.AnnotatedElement 用于反射读取注解的API} 抛出。
 *
 * @author  Josh Bloch
 * @see     java.lang.reflect.AnnotatedElement
 * @since   1.5
 */
@SuppressWarnings("rawtypes") /* 原始类型是公共API的一部分 */
public class EnumConstantNotPresentException extends RuntimeException {
    private static final long serialVersionUID = -6046998521960521108L;

    /**
     * 缺失的枚举常量的类型。
     */
    private Class<? extends Enum> enumType;

    /**
     * 缺失的枚举常量的名称。
     */
    private String constantName;

    /**
     * 为指定的常量构造一个 <tt>EnumConstantNotPresentException</tt>。
     *
     * @param enumType 缺失的枚举常量的类型
     * @param constantName 缺失的枚举常量的名称
     */
    public EnumConstantNotPresentException(Class<? extends Enum> enumType,
                                           String constantName) {
        super(enumType.getName() + "." + constantName);
        this.enumType = enumType;
        this.constantName  = constantName;
    }

    /**
     * 返回缺失的枚举常量的类型。
     *
     * @return 缺失的枚举常量的类型
     */
    public Class<? extends Enum> enumType() { return enumType; }

    /**
     * 返回缺失的枚举常量的名称。
     *
     * @return 缺失的枚举常量的名称
     */
    public String constantName() { return constantName; }
}
