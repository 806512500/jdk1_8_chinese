/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.security.spec;

/**
 * 一个（透明的）密钥材料的规范，构成一个加密密钥。
 *
 * <p>如果密钥存储在硬件设备上，其规范可能包含有助于在设备上识别密钥的信息。
 *
 * <P> 密钥可以以算法特定的方式，或以算法独立的编码格式（如 ASN.1）来指定。
 * 例如，DSA 私钥可以通过其组件 {@code x}、{@code p}、{@code q} 和 {@code g}
 * （参见 {@link DSAPrivateKeySpec}）来指定，或者可以使用其 DER 编码来指定
 * （参见 {@link PKCS8EncodedKeySpec}）。
 *
 * <P> 此接口不包含任何方法或常量。其唯一目的是将所有密钥规范分组（并提供类型安全性）。
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
