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
 * 接口定义了一组特定于DSA的密钥参数，定义了一个DSA <em>密钥族</em>。DSA（数字签名算法）在NIST的FIPS-186中定义。
 *
 * @see DSAKey
 * @see java.security.Key
 * @see java.security.Signature
 *
 * @author Benjamin Renaud
 * @author Josh Bloch
 */
public interface DSAParams {

    /**
     * 返回素数 {@code p}。
     *
     * @return 素数 {@code p}。
     */
    public BigInteger getP();

    /**
     * 返回副素数 {@code q}。
     *
     * @return 副素数 {@code q}。
     */
    public BigInteger getQ();

    /**
     * 返回基 {@code g}。
     *
     * @return 基 {@code g}。
     */
    public BigInteger getG();
}
