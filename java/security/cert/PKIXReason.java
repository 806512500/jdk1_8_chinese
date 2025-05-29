/*
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security.cert;

/**
 * {@code PKIXReason} 枚举了根据 PKIX (RFC 3280) 标准，X.509 证书路径可能无效的特定于 PKIX 的潜在原因。
 * 这些原因除了 {@code CertPathValidatorException.BasicReason} 枚举中列出的原因之外。
 *
 * @since 1.7
 */
public enum PKIXReason implements CertPathValidatorException.Reason {
    /**
     * 证书无法正确链接。
     */
    NAME_CHAINING,

    /**
     * 证书的密钥用法无效。
     */
    INVALID_KEY_USAGE,

    /**
     * 策略约束被违反。
     */
    INVALID_POLICY,

    /**
     * 未找到可接受的信任锚点。
     */
    NO_TRUST_ANCHOR,

    /**
     * 证书包含一个或多个未识别的关键扩展。
     */
    UNRECOGNIZED_CRIT_EXT,

    /**
     * 证书不是 CA 证书。
     */
    NOT_CA_CERT,

    /**
     * 路径长度约束被违反。
     */
    PATH_TOO_LONG,

    /**
     * 名称约束被违反。
     */
    INVALID_NAME
}
