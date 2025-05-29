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

package java.lang.annotation;

/**
 * 注解类型 {@code java.lang.annotation.Repeatable} 用于表示其（元）注解的注解类型是<em>可重复的</em>。
 * {@code @Repeatable} 的值表示可重复注解类型的<em>包含注解类型</em>。
 *
 * @since 1.8
 * @jls 9.6 注解类型
 * @jls 9.7 注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Repeatable {
    /**
     * 表示可重复注解类型的<em>包含注解类型</em>。
     * @return 包含注解类型
     */
    Class<? extends Annotation> value();
}
