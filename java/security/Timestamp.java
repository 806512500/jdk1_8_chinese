/*
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
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
import java.security.cert.Certificate;
import java.security.cert.CertPath;
import java.security.cert.X509Extension;
import java.util.Date;
import java.util.List;

/**
 * 该类封装了关于已签名时间戳的信息。
 * 它是不可变的。
 * 它包括时间戳的日期和时间以及生成和签署时间戳的时间戳机构 (TSA) 的信息。
 *
 * @since 1.5
 * @author Vincent Ryan
 */

public final class Timestamp implements Serializable {

    private static final long serialVersionUID = -5502683707821851294L;

    /**
     * 时间戳的日期和时间
     *
     * @serial
     */
    private Date timestamp;

    /**
     * TSA 的证书路径。
     *
     * @serial
     */
    private CertPath signerCertPath;

    /*
     * 此时间戳的哈希码。
     */
    private transient int myhash = -1;

    /**
     * 构造一个 Timestamp。
     *
     * @param timestamp 是时间戳的日期和时间。它不能为空。
     * @param signerCertPath 是 TSA 的证书路径。它不能为空。
     * @throws NullPointerException 如果 timestamp 或 signerCertPath 为空。
     */
    public Timestamp(Date timestamp, CertPath signerCertPath) {
        if (timestamp == null || signerCertPath == null) {
            throw new NullPointerException();
        }
        this.timestamp = new Date(timestamp.getTime()); // 克隆
        this.signerCertPath = signerCertPath;
    }

    /**
     * 返回生成时间戳的日期和时间。
     *
     * @return 时间戳的日期和时间。
     */
    public Date getTimestamp() {
        return new Date(timestamp.getTime()); // 克隆
    }

    /**
     * 返回时间戳机构的证书路径。
     *
     * @return TSA 的证书路径。
     */
    public CertPath getSignerCertPath() {
        return signerCertPath;
    }

    /**
     * 返回此时间戳的哈希码值。
     * 哈希码是使用时间戳的日期和时间以及 TSA 的证书路径生成的。
     *
     * @return 此时间戳的哈希码值。
     */
    public int hashCode() {
        if (myhash == -1) {
            myhash = timestamp.hashCode() + signerCertPath.hashCode();
        }
        return myhash;
    }

    /**
     * 测试指定对象与此时间戳是否相等。
     * 如果两个时间戳的日期和时间及其签名者的证书路径相等，则认为它们相等。
     *
     * @param obj 要测试与该时间戳是否相等的对象。
     *
     * @return 如果时间戳被认为相等，则返回 true，否则返回 false。
     */
    public boolean equals(Object obj) {
        if (obj == null || (!(obj instanceof Timestamp))) {
            return false;
        }
        Timestamp that = (Timestamp)obj;

        if (this == that) {
            return true;
        }
        return (timestamp.equals(that.getTimestamp()) &&
            signerCertPath.equals(that.getSignerCertPath()));
    }

    /**
     * 返回描述此时间戳的字符串。
     *
     * @return 包含时间戳的日期和时间及其签名者的证书的字符串。
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        sb.append("timestamp: " + timestamp);
        List<? extends Certificate> certs = signerCertPath.getCertificates();
        if (!certs.isEmpty()) {
            sb.append("TSA: " + certs.get(0));
        } else {
            sb.append("TSA: <empty>");
        }
        sb.append(")");
        return sb.toString();
    }

    // 显式将哈希码值重置为 -1
    private void readObject(ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        myhash = -1;
        timestamp = new Date(timestamp.getTime());
    }
}
