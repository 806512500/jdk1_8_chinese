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
 * 一个定义了选择 {@code Certificate} 的一组标准的选择器。实现此接口的类
 * 通常用于指定应从 {@code CertStore} 中检索哪些 {@code Certificate}。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，此接口中定义的方法不是线程安全的。多个线程需要并发访问单个
 * 对象时，应同步并提供必要的锁定。每个线程操作不同对象时，无需同步。
 *
 * @see Certificate
 * @see CertStore
 * @see CertStore#getCertificates
 *
 * @author      Steve Hanna
 * @since       1.4
 */
public interface CertSelector extends Cloneable {

    /**
     * 决定是否应选择一个 {@code Certificate}。
     *
     * @param   cert    要检查的 {@code Certificate}
     * @return  如果应选择该 {@code Certificate}，则返回 {@code true}，否则返回 {@code false}
     */
    boolean match(Certificate cert);

    /**
     * 创建此 {@code CertSelector} 的副本。对副本的修改不会影响原始对象，反之亦然。
     *
     * @return 此 {@code CertSelector} 的副本
     */
    Object clone();
}
