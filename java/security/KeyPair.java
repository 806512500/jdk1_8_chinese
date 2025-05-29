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

import java.util.*;

/**
 * 该类是一个简单的密钥对（公钥和私钥）持有者。它不强制执行任何安全性，在初始化后，
 * 应该像对待PrivateKey一样对待。
 *
 * @see PublicKey
 * @see PrivateKey
 *
 * @author Benjamin Renaud
 */

public final class KeyPair implements java.io.Serializable {

    private static final long serialVersionUID = -7565189502268009837L;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    /**
     * 从给定的公钥和私钥构造一个密钥对。
     *
     * <p>注意，此构造函数仅在生成的密钥对中存储公钥和私钥组件的引用。这是安全的，
     * 因为 {@code Key} 对象是不可变的。
     *
     * @param publicKey 公钥。
     *
     * @param privateKey 私钥。
     */
    public KeyPair(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /**
     * 返回此密钥对的公钥组件的引用。
     *
     * @return 公钥的引用。
     */
    public PublicKey getPublic() {
        return publicKey;
    }

     /**
     * 返回此密钥对的私钥组件的引用。
     *
     * @return 私钥的引用。
     */
   public PrivateKey getPrivate() {
        return privateKey;
    }
}
