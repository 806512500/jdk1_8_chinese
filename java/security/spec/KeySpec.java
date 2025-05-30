/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security.spec;

/**
 * 一个（透明的）密钥材料的规范，
 * 构成一个加密密钥。
 *
 * <p>如果密钥存储在硬件设备上，其
 * 规范可能包含有助于在设备上识别密钥的信息。
 *
 * <P> 密钥可以以算法特定的方式指定，也可以以
 * 算法无关的编码格式（如 ASN.1）指定。
 * 例如，DSA 私钥可以通过其组件
 * {@code x}，{@code p}，{@code q} 和 {@code g}
 * （参见 {@link DSAPrivateKeySpec}）来指定，或者可以
 * 使用其 DER 编码来指定
 * （参见 {@link PKCS8EncodedKeySpec}）。
 *
 * <P> 该接口不包含任何方法或常量。其唯一目的
 * 是将（并为）所有密钥规范提供类型安全。
 * 所有密钥规范都必须实现此接口。
 *
 * @author Jan Luehe
 *
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see EncodedKeySpec
 * @see X509EncodedKeySpec
 * @see PKCS8EncodedKeySpec
 * @see DSAPrivateKeySpec
 * @see DSAPublicKeySpec
 *
 * @since 1.2
 */

public interface KeySpec { }
