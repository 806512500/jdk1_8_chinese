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
 * RSA私钥的接口。
 *
 * @author Jan Luehe
 *
 *
 * @see RSAPrivateCrtKey
 */

public interface RSAPrivateKey extends java.security.PrivateKey, RSAKey
{

    /**
     * 用于表示与类型先前版本的序列化兼容性的类型指纹。
     */
    static final long serialVersionUID = 5187144804936595022L;

    /**
     * 返回私钥指数。
     *
     * @return 私钥指数
     */
    public BigInteger getPrivateExponent();
}
