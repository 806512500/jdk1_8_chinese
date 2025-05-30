/*
 * Copyright (c) 2003, 2023, Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.security.cert.CertPath;

/**
 * 该类封装了关于代码签名者的信息。
 * 它是不可变的。
 *
 * @since 1.5
 * @author Vincent Ryan
 */

public final class CodeSigner implements Serializable {

    private static final long serialVersionUID = 6819288105193937581L;

    /**
     * 签名者的证书路径。
     *
     * @serial
     */
    private CertPath signerCertPath;

    /*
     * 签名时间戳。
     *
     * @serial
     */
    private Timestamp timestamp;

    /*
     * 该代码签名者的哈希码。
     */
    private transient int myhash = -1;

    /**
     * 构造一个 CodeSigner 对象。
     *
     * @param signerCertPath 签名者的证书路径。
     *                       它不能为空。
     * @param timestamp 签名时间戳。
     *                  如果为空，则签名没有生成时间戳。
     * @throws NullPointerException 如果 {@code signerCertPath} 为空。
     */
    public CodeSigner(CertPath signerCertPath, Timestamp timestamp) {
        if (signerCertPath == null) {
            throw new NullPointerException();
        }
        this.signerCertPath = signerCertPath;
        this.timestamp = timestamp;
    }

    /**
     * 返回签名者的证书路径。
     *
     * @return 证书路径。
     */
    public CertPath getSignerCertPath() {
        return signerCertPath;
    }

    /**
     * 返回签名时间戳。
     *
     * @return 时间戳，如果不存在则返回 {@code null}。
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * 返回该代码签名者的哈希码值。
     * 哈希码是使用签名者的证书路径和时间戳（如果存在）生成的。
     *
     * @return 该代码签名者的哈希码值。
     */
    public int hashCode() {
        if (myhash == -1) {
            if (timestamp == null) {
                myhash = signerCertPath.hashCode();
            } else {
                myhash = signerCertPath.hashCode() + timestamp.hashCode();
            }
        }
        return myhash;
    }

    /**
     * 测试指定对象与该代码签名者是否相等。
     * 两个代码签名者被认为相等，如果它们的签名证书路径相等，并且它们的时间戳相等（如果都存在）。
     *
     * @param obj 要测试是否与该对象相等的对象。
     *
     * @return 如果对象被认为相等则返回 true，否则返回 false。
     */
    public boolean equals(Object obj) {
        if (obj == null || (!(obj instanceof CodeSigner))) {
            return false;
        }
        CodeSigner that = (CodeSigner)obj;

        if (this == that) {
            return true;
        }
        Timestamp thatTimestamp = that.getTimestamp();
        if (timestamp == null) {
            if (thatTimestamp != null) {
                return false;
            }
        } else {
            if (thatTimestamp == null ||
                (! timestamp.equals(thatTimestamp))) {
                return false;
            }
        }
        return signerCertPath.equals(that.getSignerCertPath());
    }

    /**
     * 返回描述该代码签名者的字符串。
     *
     * @return 包含签名者的证书和时间戳（如果存在）的字符串。
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        sb.append("Signer: ").append(signerCertPath.getCertificates().get(0));
        if (timestamp != null) {
            sb.append("timestamp: ").append(timestamp);
        }
        sb.append(")");
        return sb.toString();
    }

    // 显式重置哈希码值为 -1
    private void readObject(ObjectInputStream ois)
            throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        if (signerCertPath == null) {
            throw new InvalidObjectException("signerCertPath is null");
        }
        myhash = -1;
    }
}
