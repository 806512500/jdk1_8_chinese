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
 * 表示一个注解类型是自动继承的。如果在一个注解类型声明上存在 Inherited 元注解，并且用户在一个类声明上查询该注解类型，而该类声明没有此类型的注解，
 * 那么将自动查询该类的超类以查找注解类型。这个过程将重复进行，直到找到此类型的注解，或者到达类层次结构的顶部（Object）。
 * 如果没有超类具有此类型的注解，那么查询将表明该类没有此类注解。
 *
 * <p>请注意，如果注解类型用于注解除类以外的任何其他内容，此元注解类型将不起作用。还请注意，此元注解仅导致从超类继承注解；
 * 实现的接口上的注解没有效果。
 *
 * @author  Joshua Bloch
 * @since 1.5
 * @jls 9.6.3.3 @Inherited
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Inherited {
}
