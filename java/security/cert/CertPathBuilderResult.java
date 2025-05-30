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

/**
 * 认证路径构建算法结果的规范。
 * {@link CertPathBuilder#build CertPathBuilder.build} 方法返回的所有结果都必须实现此接口。
 * <p>
 * 最少，一个 {@code CertPathBuilderResult} 包含由 {@code CertPathBuilder} 实例构建的
 * {@code CertPath}。此接口的实现可以添加方法以返回实现或算法特定的信息，例如调试信息或
 * 认证路径验证结果。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，此接口中定义的方法不是线程安全的。多个线程需要并发访问单个
 * 对象时，应同步并提供必要的锁定。每个操作不同对象的多个线程不需要同步。
 *
 * @see CertPathBuilder
 *
 * @since       1.4
 * @author      Sean Mullan
 */
public interface CertPathBuilderResult extends Cloneable {

    /**
     * 返回构建的认证路径。
     *
     * @return 认证路径（从不为 {@code null}）
     */
    CertPath getCertPath();

    /**
     * 复制此 {@code CertPathBuilderResult}。对副本的修改不会影响原始对象，反之亦然。
     *
     * @return 此 {@code CertPathBuilderResult} 的副本
     */
    Object clone();
}
