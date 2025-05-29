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
 * {@code AnnotatedParameterizedType} 表示可能带有注解的参数化类型的使用，其类型参数本身也可能表示带有注解的类型的使用。
 *
 * @since 1.8
 */
public interface AnnotatedParameterizedType extends AnnotatedType {

    /**
     * 返回此参数化类型的可能带有注解的实际类型参数。
     *
     * @return 此参数化类型的可能带有注解的实际类型参数
     */
    AnnotatedType[] getAnnotatedActualTypeArguments();
}
