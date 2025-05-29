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

package java.lang.reflect;

/**
 * {@code AnnotatedType} 表示在当前运行于此 VM 的程序中可能被注解的类型使用。该使用可以是 Java 编程语言中的任何类型，包括数组类型、参数化类型、类型变量或通配符类型。
 *
 * @since 1.8
 */
public interface AnnotatedType extends AnnotatedElement {

    /**
     * 返回此注解类型所表示的基础类型。
     *
     * @return 此注解类型所表示的类型
     */
    public Type getType();
}
