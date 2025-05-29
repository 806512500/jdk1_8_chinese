/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其关联公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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
     * 此代码签名者的哈希码。
     */
    private transient int myhash = -1;

    /**
     * 构造一个 CodeSigner 对象。
     *
     * @param signerCertPath 签名者的证书路径。
     *                       它不能为空。
     * @param timestamp 签名时间戳。
     *                  如果为空，则签名时未生成时间戳。
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
     * @return 时间戳或如果不存在则返回 {@code null}。
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * 返回此代码签名者的哈希码值。
     * 哈希码使用签名者的证书路径和时间戳（如果存在）生成。
     *
     * @return 此代码签名者的哈希码值。
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
     * 测试指定对象与此代码签名者是否相等。
     * 如果两个代码签名者的签名证书路径相等，并且如果两者都存在，则时间戳也相等，则认为它们相等。
     *
     * @param obj 要与此对象测试相等性的对象。
     *
     * @return 如果对象被认为相等，则返回 true，否则返回 false。
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
     * 返回描述此代码签名者的字符串。
     *
     * @return 包含签名者的证书和时间戳（如果存在）的字符串。
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        sb.append("Signer: " + signerCertPath.getCertificates().get(0));
        if (timestamp != null) {
            sb.append("timestamp: " + timestamp);
        }
        sb.append(")");
        return sb.toString();
    }

    // 显式将哈希码值重置为 -1
    private void readObject(ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
     ois.defaultReadObject();
     myhash = -1;
    }
}
