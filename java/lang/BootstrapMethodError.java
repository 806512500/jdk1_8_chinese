/*
 * Copyright (c) 2008, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

/**
 * 抛出以指示 {@code invokedynamic} 指令未能找到其引导方法，
 * 或引导方法未能提供具有正确 {@linkplain java.lang.invoke.MethodHandle#type 方法类型} 的
 * {@linkplain java.lang.invoke.CallSite call site}。
 *
 * @author John Rose, JSR 292 EG
 * @since 1.7
 */
public class BootstrapMethodError extends LinkageError {
    private static final long serialVersionUID = 292L;

    /**
     * 构造一个没有详细消息的 {@code BootstrapMethodError}。
     */
    public BootstrapMethodError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 {@code BootstrapMethodError}。
     *
     * @param s 详细消息。
     */
    public BootstrapMethodError(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和原因构造一个 {@code BootstrapMethodError}。
     *
     * @param s 详细消息。
     * @param cause 原因，可以为 {@code null}。
     */
    public BootstrapMethodError(String s, Throwable cause) {
        super(s, cause);
    }

    /**
     * 使用指定的原因构造一个 {@code BootstrapMethodError}。
     *
     * @param cause 原因，可以为 {@code null}。
     */
    public BootstrapMethodError(Throwable cause) {
        // 参见 Throwable(Throwable cause) 构造函数。
        super(cause == null ? null : cause.toString());
        initCause(cause);
    }
}
