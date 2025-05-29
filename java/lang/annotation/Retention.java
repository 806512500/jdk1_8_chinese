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

package java.lang.annotation;

/**
 * 表示带有注解类型的注解应保留多长时间。如果注解类型声明上没有 Retention 注解，
 * 则保留策略默认为 {@code RetentionPolicy.CLASS}。
 *
 * <p>如果元注解类型直接用于注解，则 Retention 元注解才有效。如果元注解类型作为
 * 另一个注解类型中的成员类型使用，则没有效果。
 *
 * @author  Joshua Bloch
 * @since 1.5
 * @jls 9.6.3.2 @Retention
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Retention {
    /**
     * 返回保留策略。
     * @return 保留策略
     */
    RetentionPolicy value();
}
