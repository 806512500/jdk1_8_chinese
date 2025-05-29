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
import static java.lang.annotation.ElementType.*;

/**
 * 被标注为 @Deprecated 的程序元素是程序员不建议使用的，通常是因为它存在风险，
 * 或者存在更好的替代方案。编译器会在非废弃代码中使用或重写废弃的程序元素时发出警告。
 *
 * @author  Neal Gafter
 * @since 1.5
 * @jls 9.6.3.6 @Deprecated
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value={CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE})
public @interface Deprecated {
}