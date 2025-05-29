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
 * {@code AnnotatedTypeVariable} 表示可能带有注解的类型变量的使用，其声明可能具有表示类型注解使用的边界。
 *
 * @since 1.8
 */
public interface AnnotatedTypeVariable extends AnnotatedType {

    /**
     * 返回此类型变量的可能带有注解的边界。
     *
     * @return 此类型变量的可能带有注解的边界
     */
    AnnotatedType[] getAnnotatedBounds();
}
