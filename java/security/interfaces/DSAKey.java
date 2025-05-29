/*
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
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

/**
 * DSA公钥或私钥的接口。DSA（数字签名算法）在NIST的FIPS-186中定义。
 *
 * @see DSAParams
 * @see java.security.Key
 * @see java.security.Signature
 *
 * @author Benjamin Renaud
 * @author Josh Bloch
 */
public interface DSAKey {

    /**
     * 返回DSA特定的密钥参数。这些参数从不是秘密的。
     *
     * @return DSA特定的密钥参数。
     *
     * @see DSAParams
     */
    public DSAParams getParams();
}
