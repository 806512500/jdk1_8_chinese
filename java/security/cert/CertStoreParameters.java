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
 * {@code CertStore} 参数的规范。
 * <p>
 * 该接口的目的是将所有 {@code CertStore} 参数规范分组（并提供类型安全）。所有
 * {@code CertStore} 参数规范都必须实现此接口。
 * <p>
 * 通常，一个 {@code CertStoreParameters} 对象作为参数传递给
 * {@link CertStore#getInstance CertStore.getInstance} 方法之一。{@code getInstance}
 * 方法返回一个用于检索 {@code Certificate} 和 {@code CRL} 的 {@code CertStore}。返回的
 * {@code CertStore} 会使用指定的参数进行初始化。不同类型的 {@code CertStore} 可能需要的参数类型会有所不同。
 *
 * @see CertStore#getInstance
 *
 * @since       1.4
 * @author      Steve Hanna
 */
public interface CertStoreParameters extends Cloneable {

    /**
     * 复制此 {@code CertStoreParameters}。
     * <p>
     * “复制”的精确含义可能取决于 {@code CertStoreParameters} 对象的类。典型的实现
     * 会执行此对象的“深复制”，但这不是绝对的要求。某些实现可能会执行此对象某些或全部字段的“浅复制”。
     * <p>
     * 请注意，{@code CertStore.getInstance} 方法会复制指定的 {@code CertStoreParameters}。深复制
     * 实现的 {@code clone} 更安全、更健壮，因为它防止调用者通过修改其初始化参数来破坏共享的 {@code CertStore}。
     * 然而，对于需要引用 {@code CertStoreParameters} 中包含的参数的应用程序，浅复制实现的 {@code clone} 更合适。例如，
     * 浅复制克隆允许应用程序立即释放特定 {@code CertStore} 初始化参数的资源，而不是等待垃圾回收机制。这应该非常小心地完成，
     * 因为 {@code CertStore} 可能仍然被其他线程使用。
     * <p>
     * 每个子类都应说明此方法的精确行为，以便用户和开发人员知道预期的行为。
     *
     * @return 此 {@code CertStoreParameters} 的副本
     */
    Object clone();
}
