/*
 * 版权所有 (c) 2010, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

package java.security;

/**
 * 加密原语的枚举。
 *
 * @since 1.7
 */
public enum CryptoPrimitive {
    /**
     * 哈希函数
     */
    MESSAGE_DIGEST,

    /**
     * 加密随机数生成器
     */
    SECURE_RANDOM,

    /**
     * 对称原语：分组密码
     */
    BLOCK_CIPHER,

    /**
     * 对称原语：流密码
     */
    STREAM_CIPHER,

    /**
     * 对称原语：消息认证码
     */
    MAC,

    /**
     * 对称原语：密钥包装
     */
    KEY_WRAP,

    /**
     * 非对称原语：公钥加密
     */
    PUBLIC_KEY_ENCRYPTION,

    /**
     * 非对称原语：签名方案
     */
    SIGNATURE,

    /**
     * 非对称原语：密钥封装机制
     */
    KEY_ENCAPSULATION,

    /**
     * 非对称原语：密钥协商和密钥分发
     */
    KEY_AGREEMENT
}
