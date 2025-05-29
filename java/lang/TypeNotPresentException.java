/*
 * Copyright (c) 2003, 2008, Oracle and/or its affiliates. All rights reserved.
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
 * 当应用程序尝试使用表示类型的字符串访问类型，但找不到指定名称的类型定义时抛出此异常。
 * 该异常与 {@link ClassNotFoundException} 不同，因为 <tt>ClassNotFoundException</tt> 是一个检查型异常，
 * 而此异常是未检查型的。
 *
 * <p>请注意，当访问未定义的类型变量以及加载类型（例如，类、接口或注解类型）时，也可能使用此异常。
 * 特别是，此异常可以由 {@linkplain
 * java.lang.reflect.AnnotatedElement 用于反射读取注解的API} 抛出。
 *
 * @author  Josh Bloch
 * @see     java.lang.reflect.AnnotatedElement
 * @since 1.5
 */
public class TypeNotPresentException extends RuntimeException {
    private static final long serialVersionUID = -5101214195716534496L;

    private String typeName;

    /**
     * 构造一个指定类型的 <tt>TypeNotPresentException</tt>，并带有指定的原因。
     *
     * @param typeName 无法访问的类型的完全限定名称
     * @param cause 系统尝试加载指定类型时抛出的异常，如果不可用或不适用则为 <tt>null</tt>
     */
    public TypeNotPresentException(String typeName, Throwable cause) {
        super("Type " + typeName + " not present", cause);
        this.typeName = typeName;
    }

    /**
     * 返回无法访问的类型的完全限定名称。
     *
     * @return 无法访问的类型的完全限定名称
     */
    public String typeName() { return typeName;}
}
