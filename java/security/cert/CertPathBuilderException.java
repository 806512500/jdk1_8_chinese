/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security.cert;

import java.security.GeneralSecurityException;

/**
 * 表示在使用 {@code CertPathBuilder} 构建认证路径时遇到的各种问题的异常。
 * <p>
 * {@code CertPathBuilderException} 提供了对异常的包装支持。{@link #getCause getCause} 方法返回导致此异常被抛出的可抛出对象，如果有。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，否则本类中定义的方法不是线程安全的。多个需要并发访问单个对象的线程应同步并提供必要的锁定。多个线程各自操作不同的对象时不需要同步。
 *
 * @see CertPathBuilder
 *
 * @since       1.4
 * @author      Sean Mullan
 */
public class CertPathBuilderException extends GeneralSecurityException {

    private static final long serialVersionUID = 5316471420178794402L;

    /**
     * 创建一个带有 {@code null} 详细消息的 {@code CertPathBuilderException}。
     */
    public CertPathBuilderException() {
        super();
    }

    /**
     * 使用给定的详细消息创建一个 {@code CertPathBuilderException}。详细消息是一个 {@code String}，用于更详细地描述此特定异常。
     *
     * @param msg 详细消息
     */
    public CertPathBuilderException(String msg) {
        super(msg);
    }

    /**
     * 创建一个包装指定可抛出对象的 {@code CertPathBuilderException}。这允许将任何异常转换为 {@code CertPathBuilderException}，同时保留有关包装异常的信息，这些信息可能对调试有用。详细消息设置为 ({@code cause==null ? null : cause.toString()})
     * （通常包含 cause 的类和详细消息）。
     *
     * @param cause 通过 {@link #getCause getCause()} 方法稍后检索的原因（允许 {@code null} 值，表示原因不存在或未知）。
     */
    public CertPathBuilderException(Throwable cause) {
        super(cause);
    }

    /**
     * 使用指定的详细消息和原因创建一个 {@code CertPathBuilderException}。
     *
     * @param msg 详细消息
     * @param  cause 通过 {@link #getCause getCause()} 方法稍后检索的原因（允许 {@code null} 值，表示原因不存在或未知）。
     */
    public CertPathBuilderException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
