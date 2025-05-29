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
 * {@code AnnotatedWildcardType} 表示可能带有注解的通配符类型参数的使用，其上下界本身也可能表示类型使用的注解。
 *
 * @since 1.8
 */
public interface AnnotatedWildcardType extends AnnotatedType {

    /**
     * 返回此通配符类型的可能带有注解的下界。
     *
     * @return 此通配符类型的可能带有注解的下界
     */
    AnnotatedType[] getAnnotatedLowerBounds();

    /**
     * 返回此通配符类型的可能带有注解的上界。
     *
     * @return 此通配符类型的可能带有注解的上界
     */
    AnnotatedType[] getAnnotatedUpperBounds();
}
