/*
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.annotation;

/**
 * 注释保留策略。此枚举类型的常量描述了保留注释的各种策略。它们与 {@link Retention} 元注释类型一起使用，以指定注释应保留多长时间。
 *
 * @author  Joshua Bloch
 * @since 1.5
 */
public enum RetentionPolicy {
    /**
     * 注释应由编译器丢弃。
     */
    SOURCE,

    /**
     * 注释应由编译器记录在类文件中，但运行时虚拟机不必保留。这是默认行为。
     */
    CLASS,

    /**
     * 注释应由编译器记录在类文件中，并且运行时由虚拟机保留，因此可以反射读取。
     *
     * @see java.lang.reflect.AnnotatedElement
     */
    RUNTIME
}
