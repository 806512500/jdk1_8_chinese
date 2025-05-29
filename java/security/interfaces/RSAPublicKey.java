/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * RSA公钥的接口。
 *
 * @author Jan Luehe
 *
 */

public interface RSAPublicKey extends java.security.PublicKey, RSAKey
{
    /**
     * 用于表示与类型先前版本的序列化兼容性的类型指纹。
     */
    static final long serialVersionUID = -8727434096241101194L;

    /**
     * 返回公钥指数。
     *
     * @return 公钥指数
     */
    public BigInteger getPublicExponent();
}
