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
 * 一个定义了选择 {@code CRL} 的一组标准的选择器。
 * 实现此接口的类通常用于指定应从 {@code CertStore} 中检索哪些 {@code CRL}。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，此接口中定义的方法不是线程安全的。需要并发访问单个对象的多个线程应自行同步并提供必要的锁定。每个操作不同对象的多个线程不需要同步。
 *
 * @see CRL
 * @see CertStore
 * @see CertStore#getCRLs
 *
 * @author      Steve Hanna
 * @since       1.4
 */
public interface CRLSelector extends Cloneable {

    /**
     * 决定是否应选择一个 {@code CRL}。
     *
     * @param   crl     要检查的 {@code CRL}
     * @return  如果应选择 {@code CRL}，则返回 {@code true}，否则返回 {@code false}
     */
    boolean match(CRL crl);

    /**
     * 创建此 {@code CRLSelector} 的副本。对副本的修改不会影响原始对象，反之亦然。
     *
     * @return 此 {@code CRLSelector} 的副本
     */
    Object clone();
}
