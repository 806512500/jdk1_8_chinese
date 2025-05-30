/*
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.security.cert;

/**
 * 该类是对具有不同格式但重要共同用途的证书吊销列表（CRL）的抽象。例如，所有CRL都具有列出已吊销证书的功能，并且可以查询它们是否列出了给定的证书。
 * <p>
 * 可以通过继承此抽象类来定义专门的CRL类型。
 *
 * @author Hemma Prafullchandra
 *
 *
 * @see X509CRL
 * @see CertificateFactory
 *
 * @since 1.2
 */

public abstract class CRL {

    // CRL的类型
    private String type;

    /**
     * 创建指定类型的CRL。
     *
     * @param type CRL类型的标准名称。
     * 有关标准CRL类型的详细信息，请参阅<a href=
     * "../../../../technotes/guides/security/crypto/CryptoSpec.html#AppA">
     * Java Cryptography Architecture API Specification &amp; Reference </a>的附录A。
     */
    protected CRL(String type) {
        this.type = type;
    }

    /**
     * 返回此CRL的类型。
     *
     * @return 此CRL的类型。
     */
    public final String getType() {
        return this.type;
    }

    /**
     * 返回此CRL的字符串表示形式。
     *
     * @return 此CRL的字符串表示形式。
     */
    public abstract String toString();

    /**
     * 检查给定的证书是否在该CRL上。
     *
     * @param cert 要检查的证书。
     * @return 如果给定的证书在该CRL上，则返回true，否则返回false。
     */
    public abstract boolean isRevoked(Certificate cert);
}
