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

package java.lang;

import java.lang.annotation.*;

/**
 * 表示方法声明旨在覆盖超类型中的方法声明。如果一个方法用此注解类型标注，
 * 编译器必须生成错误信息，除非以下条件之一成立：
 *
 * <ul><li>
 * 该方法确实覆盖或实现了超类型中声明的方法。
 * </li><li>
 * 该方法的签名与 {@linkplain Object} 中声明的任何公共方法的签名等效。
 * </li></ul>
 *
 * @author  Peter von der Ah&eacute;
 * @author  Joshua Bloch
 * @jls 9.6.1.4 @Override
 * @since 1.5
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Override {
}
