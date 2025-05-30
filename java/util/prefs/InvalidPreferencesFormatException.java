/*
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
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

package java.util.prefs;

import java.io.NotSerializableException;

/**
 * 抛出以指示操作无法完成，因为输入不符合 {@link Preferences} 规范中指定的首选项集合的适当 XML 文档类型。
 *
 * @author  Josh Bloch
 * @see     Preferences
 * @since   1.4
 */
public class InvalidPreferencesFormatException extends Exception {
    /**
     * 使用指定的原因构造一个 InvalidPreferencesFormatException。
     *
     * @param  cause 原因（稍后通过 {@link Throwable#getCause()} 方法检索）。
     */
    public InvalidPreferencesFormatException(Throwable cause) {
        super(cause);
    }

   /**
    * 使用指定的详细消息构造一个 InvalidPreferencesFormatException。
    *
    * @param   message  详细消息。详细消息稍后通过 {@link Throwable#getMessage()} 方法检索。
    */
    public InvalidPreferencesFormatException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因构造一个 InvalidPreferencesFormatException。
     *
     * @param  message  详细消息。详细消息稍后通过 {@link Throwable#getMessage()} 方法检索。
     * @param  cause 原因（稍后通过 {@link Throwable#getCause()} 方法检索）。
     */
    public InvalidPreferencesFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = -791715184232119669L;
}
