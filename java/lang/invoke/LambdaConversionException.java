/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * LambdaConversionException
 */
public class LambdaConversionException extends Exception {
    private static final long serialVersionUID = 292L + 8L;

    /**
     * 构造一个 {@code LambdaConversionException}。
     */
    public LambdaConversionException() {
    }

    /**
     * 使用消息构造一个 {@code LambdaConversionException}。
     * @param message 详细消息
     */
    public LambdaConversionException(String message) {
        super(message);
    }

    /**
     * 使用消息和原因构造一个 {@code LambdaConversionException}。
     * @param message 详细消息
     * @param cause 原因
     */
    public LambdaConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用原因构造一个 {@code LambdaConversionException}。
     * @param cause 原因
     */
    public LambdaConversionException(Throwable cause) {
        super(cause);
    }

    /**
     * 使用消息、原因和其他设置构造一个 {@code LambdaConversionException}。
     * @param message 详细消息
     * @param cause 原因
     * @param enableSuppression 是否启用抑制异常
     * @param writableStackTrace 是否可写堆栈跟踪
     */
    public LambdaConversionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
