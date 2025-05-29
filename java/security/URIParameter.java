/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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
 * 包含指向 PolicySpi 或 ConfigurationSpi 实现所需数据的 URI 的参数。
 *
 * @since 1.6
 */
public class URIParameter implements
        Policy.Parameters, javax.security.auth.login.Configuration.Parameters {

    private java.net.URI uri;

    /**
     * 使用指向 SPI 实现所需数据的 URI 构造 URIParameter。
     *
     * @param uri 指向数据的 URI。
     *
     * @exception NullPointerException 如果指定的 URI 为 null。
     */
    public URIParameter(java.net.URI uri) {
        if (uri == null) {
            throw new NullPointerException("invalid null URI");
        }
        this.uri = uri;
    }

    /**
     * 返回 URI。
     *
     * @return uri URI。
     */
    public java.net.URI getURI() {
        return uri;
    }
}
