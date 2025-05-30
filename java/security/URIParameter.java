/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
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
            throw new NullPointerException("无效的 null URI");
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
