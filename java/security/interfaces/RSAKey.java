/*
 * Copyright (c) 1999, 2020, Oracle and/or its affiliates. All rights reserved.
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
import java.security.spec.AlgorithmParameterSpec;

/**
 * PKCS#1 v2.2 标准中的公钥或私钥接口，例如用于 RSA 或 RSASSA-PSS 算法。
 *
 * @author Jan Luehe
 *
 * @see RSAPublicKey
 * @see RSAPrivateKey
 *
 * @since 1.3
 */

public interface RSAKey {

    /**
     * 返回模数。
     *
     * @return 模数
     */
    public BigInteger getModulus();

    /**
     * 返回与此密钥关联的参数。
     * 参数是可选的，可以在密钥对生成期间显式指定或隐式创建。
     *
     * @implSpec
     * 默认实现返回 {@code null}。
     *
     * @return 关联的参数，可能为 null
     * @since 8
     */
    default AlgorithmParameterSpec getParams() {
        return null;
    }
}
