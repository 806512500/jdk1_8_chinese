/*
 * Copyright (c) 2008, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang.invoke;

/**
 * 抛出此异常表示代码尝试通过错误的方法类型调用方法句柄。与正常 Java 方法调用的字节码表示一样，方法句柄调用严格类型化为与调用点关联的特定类型描述符。
 * <p>
 * 当两个方法句柄组合时，如果系统检测到它们的类型无法正确匹配，也可能抛出此异常。这相当于在方法句柄构造时对类型不匹配进行早期评估，
 * 而不是在调用不匹配的方法句柄时进行评估。
 *
 * @author John Rose, JSR 292 EG
 * @since 1.7
 */
public class WrongMethodTypeException extends RuntimeException {
    private static final long serialVersionUID = 292L;

    /**
     * 构造一个没有详细消息的 {@code WrongMethodTypeException}。
     */
    public WrongMethodTypeException() {
        super();
    }

    /**
     * 构造一个具有指定详细消息的 {@code WrongMethodTypeException}。
     *
     * @param s 详细消息。
     */
    public WrongMethodTypeException(String s) {
        super(s);
    }

    /**
     * 构造一个具有指定详细消息和原因的 {@code WrongMethodTypeException}。
     *
     * @param s 详细消息。
     * @param cause 异常的原因，或 null。
     */
    //FIXME: 在 MR1 中将其公开
    /*non-public*/ WrongMethodTypeException(String s, Throwable cause) {
        super(s, cause);
    }

    /**
     * 构造一个具有指定原因的 {@code WrongMethodTypeException}。
     *
     * @param cause 异常的原因，或 null。
     */
    //FIXME: 在 MR1 中将其公开
    /*non-public*/ WrongMethodTypeException(Throwable cause) {
        super(cause);
    }
}
