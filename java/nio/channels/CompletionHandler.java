/*
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels;

/**
 * 用于处理异步 I/O 操作结果的处理器。
 *
 * <p> 本包中定义的异步通道允许指定一个完成处理器来处理异步操作的结果。
 * 当 I/O 操作成功完成时，调用 {@link #completed completed} 方法。
 * 如果 I/O 操作失败，则调用 {@link #failed failed} 方法。
 * 这些方法的实现应尽快完成，以避免调用线程无法分派到其他完成处理器。
 *
 * @param   <V>     I/O 操作的结果类型
 * @param   <A>     附加到 I/O 操作的对象类型
 *
 * @since 1.7
 */

public interface CompletionHandler<V,A> {

    /**
     * 当操作完成时调用。
     *
     * @param   result
     *          I/O 操作的结果。
     * @param   attachment
     *          初始化 I/O 操作时附加的对象。
     */
    void completed(V result, A attachment);

    /**
     * 当操作失败时调用。
     *
     * @param   exc
     *          表示 I/O 操作失败原因的异常
     * @param   attachment
     *          初始化 I/O 操作时附加的对象。
     */
    void failed(Throwable exc, A attachment);
}
