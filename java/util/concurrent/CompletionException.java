/*
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

/**
 * 在完成结果或任务时遇到错误或其他异常时抛出的异常。
 *
 * @since 1.8
 * @author Doug Lea
 */
public class CompletionException extends RuntimeException {
    private static final long serialVersionUID = 7830266012832686185L;

    /**
     * 构造一个没有详细消息的 {@code CompletionException}。
     * 原因未初始化，可以通过调用 {@link #initCause(Throwable) initCause} 来初始化。
     */
    protected CompletionException() { }

    /**
     * 使用指定的详细消息构造一个 {@code CompletionException}。
     * 原因未初始化，可以通过调用 {@link #initCause(Throwable) initCause} 来初始化。
     *
     * @param message 详细消息
     */
    protected CompletionException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因构造一个 {@code CompletionException}。
     *
     * @param  message 详细消息
     * @param  cause 原因（稍后通过 {@link #getCause()} 方法检索）
     */
    public CompletionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因构造一个 {@code CompletionException}。
     * 详细消息设置为 {@code (cause == null ? null : cause.toString())}（通常包含原因的类和详细消息）。
     *
     * @param  cause 原因（稍后通过 {@link #getCause()} 方法检索）
     */
    public CompletionException(Throwable cause) {
        super(cause);
    }
}
