/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security;

/**
 * <p>一个公钥。此接口不包含任何方法或常量。
 * 它仅用于将所有公钥接口分组（并提供类型安全）。
 *
 * 注意：专门的公钥接口扩展了此接口。
 * 例如，参见 {@code java.security.interfaces} 中的 DSAPublicKey 接口。
 *
 * @see Key
 * @see PrivateKey
 * @see Certificate
 * @see Signature#initVerify
 * @see java.security.interfaces.DSAPublicKey
 * @see java.security.interfaces.RSAPublicKey
 *
 */

public interface PublicKey extends Key {
    // 声明 serialVersionUID 以与 JDK1.1 兼容
    /**
     * 用于表示与类的先前版本序列化兼容的类指纹。
     */
    static final long serialVersionUID = 7187392471159151072L;
}
