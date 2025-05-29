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
 * {@code AnnotatedArrayType} 表示可能带有注解的数组类型的使用，其组件类型本身也可能表示类型使用的注解。
 *
 * @since 1.8
 */
public interface AnnotatedArrayType extends AnnotatedType {

    /**
     * 返回此数组类型的可能带有注解的泛型组件类型。
     *
     * @return 此数组类型的可能带有注解的泛型组件类型
     */
    AnnotatedType  getAnnotatedGenericComponentType();
}
