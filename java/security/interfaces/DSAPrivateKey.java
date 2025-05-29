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

package java.security.interfaces;

import java.math.BigInteger;

/**
 * DSA私钥的标准接口。DSA（数字签名算法）在NIST的FIPS-186中定义。
 *
 * @see java.security.Key
 * @see java.security.Signature
 * @see DSAKey
 * @see DSAPublicKey
 *
 * @author Benjamin Renaud
 */
public interface DSAPrivateKey extends DSAKey, java.security.PrivateKey {

    // 声明serialVersionUID以与JDK1.1兼容

   /**
    * 设置类指纹以表示与类的先前版本的序列化兼容性。
    */
    static final long serialVersionUID = 7776497482533790279L;

    /**
     * 返回私钥的值，{@code x}。
     *
     * @return 私钥的值，{@code x}。
     */
    public BigInteger getX();
}
