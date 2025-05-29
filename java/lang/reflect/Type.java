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
 * Type 是 Java 编程语言中所有类型的通用超接口。这些类型包括原始类型、参数化类型、
 * 数组类型、类型变量和原始类型。
 *
 * @since 1.5
 */
public interface Type {
    /**
     * 返回描述此类型的字符串，包括任何类型参数的信息。
     *
     * @implSpec 默认实现调用 {@code toString}。
     *
     * @return 描述此类型的字符串
     * @since 1.8
     */
    default String getTypeName() {
        return toString();
    }
}
