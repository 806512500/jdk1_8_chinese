/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security.interfaces;

import java.math.BigInteger;

/**
 * DSA 特定密钥参数的接口，定义了一个 DSA <em>密钥族</em>。DSA（数字签名算法）在 NIST 的 FIPS-186 中定义。
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
     * 返回子素数 {@code q}。
     *
     * @return 子素数 {@code q}。
     */
    public BigInteger getQ();

    /**
     * 返回基 {@code g}。
     *
     * @return 基 {@code g}。
     */
    public BigInteger getG();
}
