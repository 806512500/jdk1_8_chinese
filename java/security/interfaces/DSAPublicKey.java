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

package java.security.interfaces;

import java.math.BigInteger;

/**
 * DSA公钥的接口。DSA（数字签名算法）在NIST的FIPS-186中定义。
 *
 * @see java.security.Key
 * @see java.security.Signature
 * @see DSAKey
 * @see DSAPrivateKey
 *
 * @author Benjamin Renaud
 */
public interface DSAPublicKey extends DSAKey, java.security.PublicKey {

    // 声明serialVersionUID以与JDK1.1兼容

   /**
    * 设置类指纹以表示与类的先前版本的序列化兼容性。
    */
    static final long serialVersionUID = 1234526332779022332L;

    /**
     * 返回公钥的值，{@code y}。
     *
     * @return 公钥的值，{@code y}。
     */
    public BigInteger getY();
}
