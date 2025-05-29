
/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security.cert;

import java.io.ByteArrayInputStream;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * 一个不可变的证书序列（认证路径）。
 * <p>
 * 这是一个抽象类，定义了所有 {@code CertPath} 的公共方法。子类可以处理不同类型的
 * 证书（X.509, PGP 等）。
 * <p>
 * 所有 {@code CertPath} 对象都有一个类型、一个 {@code Certificate} 列表和一个或多个支持的编码。
 * 由于 {@code CertPath} 类是不可变的，因此在构造后，{@code CertPath} 不能以任何外部可见的方式更改。
 * 这一规定适用于此类及其子类添加或覆盖的所有公共字段和方法。
 * <p>
 * 类型是一个 {@code String}，用于标识认证路径中的 {@code Certificate} 类型。对于认证路径
 * {@code certPath} 中的每个证书 {@code cert}，必须满足
 * {@code cert.getType().equals(certPath.getType())} 为 {@code true}。
 * <p>
 * {@code Certificate} 列表是一个有序的 {@code List}，包含零个或多个 {@code Certificate}。
 * 此 {@code List} 及其包含的所有 {@code Certificate} 必须是不可变的。
 * <p>
 * 每个 {@code CertPath} 对象必须支持一种或多种编码，以便将对象转换为字节数组进行存储或传输给其他方。
 * 这些编码最好是文档良好的标准（如 PKCS#7）。由 {@code CertPath} 支持的编码之一被认为是默认编码。
 * 如果没有显式请求编码（例如 {@link #getEncoded() getEncoded()} 方法），则使用此编码。
 * <p>
 * 所有 {@code CertPath} 对象都是 {@code Serializable} 的。在序列化过程中，{@code CertPath}
 * 对象会被解析为一个替代的 {@link CertPathRep CertPathRep} 对象。这允许 {@code CertPath}
 * 对象被序列化为等效表示，而不管其底层实现。
 * <p>
 * 可以通过 {@code CertificateFactory} 创建 {@code CertPath} 对象，也可以由其他类（如
 * {@code CertPathBuilder}）返回。
 * <p>
 * 按照惯例，X.509 {@code CertPath}（由 {@code X509Certificate} 组成）从目标证书开始，以信任锚颁发的证书结束。
 * 即，一个证书的颁发者是下一个证书的主题。表示 {@link TrustAnchor TrustAnchor} 的证书不应包含在认证路径中。
 * 未验证的 X.509 {@code CertPath} 可能不遵循这些惯例。PKIX {@code CertPathValidator} 将检测任何导致认证路径无效的偏离，并抛出
 * {@code CertPathValidatorException}。
 *
 * <p> 每个 Java 平台的实现都必须支持以下标准的 {@code CertPath} 编码：
 * <ul>
 * <li>{@code PKCS7}</li>
 * <li>{@code PkiPath}</li>
 * </ul>
 * 这些编码在 <a href=
 * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathEncodings">
 * 认证路径编码部分</a>的 Java 加密架构标准算法名称文档中有描述。请参阅您的实现的发行文档，以了解是否支持其他编码。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 所有 {@code CertPath} 对象都必须是线程安全的。也就是说，多个线程可以同时调用此类定义的方法，对单个
 * {@code CertPath} 对象（或多个对象）进行调用，而不会产生不良影响。这一点也适用于
 * {@code CertPath.getCertificates} 返回的 {@code List}。
 * <p>
 * 要求 {@code CertPath} 对象是不可变和线程安全的，这样可以将它们传递给各种代码片段，而无需担心协调访问。
 * 提供这种线程安全性通常并不困难，因为相关的 {@code CertPath} 和 {@code List} 对象是不可变的。
 *
 * @see CertificateFactory
 * @see CertPathBuilder
 *
 * @author      Yassir Elley
 * @since       1.4
 */
public abstract class CertPath implements Serializable {

    private static final long serialVersionUID = 6068470306649138683L;

    private String type;        // 本链中的证书类型

    /**
     * 创建指定类型的 {@code CertPath}。
     * <p>
     * 该构造函数受保护，因为大多数用户应使用 {@code CertificateFactory} 来创建 {@code CertPath}。
     *
     * @param type 本路径中 {@code Certificate} 的标准名称
     */
    protected CertPath(String type) {
        this.type = type;
    }

    /**
     * 返回此认证路径中的 {@code Certificate} 类型。这与通过
     * {@link java.security.cert.Certificate#getType() cert.getType()}
     * 对认证路径中的所有 {@code Certificate} 返回的字符串相同。
     *
     * @return 此认证路径中的 {@code Certificate} 类型（从不为 null）
     */
    public String getType() {
        return type;
    }

    /**
     * 返回此认证路径支持的编码的迭代，以默认编码为首。尝试通过返回的
     * {@code Iterator} 的 {@code remove} 方法修改返回的 {@code Iterator} 将导致
     * {@code UnsupportedOperationException}。
     *
     * @return 支持的编码名称（作为字符串）的 {@code Iterator}
     */
    public abstract Iterator<String> getEncodings();

    /**
     * 比较此认证路径与指定对象是否相等。两个 {@code CertPath} 相等当且仅当它们的类型相等且它们的证书
     * {@code List}（以及隐含的 {@code List} 中的 {@code Certificate}）相等。
     * {@code CertPath} 永远不等于不是 {@code CertPath} 的对象。
     * <p>
     * 本方法实现了此算法。如果被重写，必须保持此处指定的行为。
     *
     * @param other 要与本认证路径测试相等性的对象
     * @return 如果指定对象等于此认证路径，则返回 true，否则返回 false
     */
    public boolean equals(Object other) {
        if (this == other)
            return true;


                    if (! (other instanceof CertPath))
            return false;

        CertPath otherCP = (CertPath) other;
        if (! otherCP.getType().equals(type))
            return false;

        List<? extends Certificate> thisCertList = this.getCertificates();
        List<? extends Certificate> otherCertList = otherCP.getCertificates();
        return(thisCertList.equals(otherCertList));
    }

    /**
     * 返回此证书路径的哈希码。证书路径的哈希码定义为以下计算的结果：
     * <pre>{@code
     *  hashCode = path.getType().hashCode();
     *  hashCode = 31*hashCode + path.getCertificates().hashCode();
     * }</pre>
     * 这确保了 {@code path1.equals(path2)} 意味着
     * {@code path1.hashCode()==path2.hashCode()} 对于任何两个证书路径，{@code path1} 和 {@code path2}，正如
     * {@code Object.hashCode} 的一般约定所要求的。
     *
     * @return 此证书路径的哈希码值
     */
    public int hashCode() {
        int hashCode = type.hashCode();
        hashCode = 31*hashCode + getCertificates().hashCode();
        return hashCode;
    }

    /**
     * 返回此证书路径的字符串表示形式。这将调用路径中每个
     * {@code Certificate} 的 {@code toString} 方法。
     *
     * @return 此证书路径的字符串表示形式
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Iterator<? extends Certificate> stringIterator =
                                        getCertificates().iterator();

        sb.append("\n" + type + " Cert Path: length = "
            + getCertificates().size() + ".\n");
        sb.append("[\n");
        int i = 1;
        while (stringIterator.hasNext()) {
            sb.append("=========================================="
                + "===============Certificate " + i + " start.\n");
            Certificate stringCert = stringIterator.next();
            sb.append(stringCert.toString());
            sb.append("\n========================================"
                + "=================Certificate " + i + " end.\n\n\n");
            i++;
        }

        sb.append("\n]");
        return sb.toString();
    }

    /**
     * 返回此证书路径的编码形式，使用默认编码。
     *
     * @return 编码后的字节
     * @exception CertificateEncodingException 如果发生编码错误
     */
    public abstract byte[] getEncoded()
        throws CertificateEncodingException;

    /**
     * 返回此证书路径的编码形式，使用指定的编码。
     *
     * @param encoding 要使用的编码名称
     * @return 编码后的字节
     * @exception CertificateEncodingException 如果发生编码错误或
     *   请求的编码不支持
     */
    public abstract byte[] getEncoded(String encoding)
        throws CertificateEncodingException;

    /**
     * 返回此证书路径中的证书列表。
     * 返回的 {@code List} 必须是不可变和线程安全的。
     *
     * @return 一个不可变的 {@code List}，包含 {@code Certificate}（可能是空的，但不能为 null）
     */
    public abstract List<? extends Certificate> getCertificates();

    /**
     * 用一个 {@code CertPathRep} 对象替换要序列化的 {@code CertPath}。
     *
     * @return 要序列化的 {@code CertPathRep}
     *
     * @throws ObjectStreamException 如果不能创建表示此证书路径的 {@code CertPathRep} 对象
     */
    protected Object writeReplace() throws ObjectStreamException {
        try {
            return new CertPathRep(type, getEncoded());
        } catch (CertificateException ce) {
            NotSerializableException nse =
                new NotSerializableException
                    ("java.security.cert.CertPath: " + type);
            nse.initCause(ce);
            throw nse;
        }
    }

    /**
     * 用于序列化的替代 {@code CertPath} 类。
     * @since 1.4
     */
    protected static class CertPathRep implements Serializable {

        private static final long serialVersionUID = 3015633072427920915L;

        /** 证书类型 */
        private String type;
        /** 证书路径的编码形式 */
        private byte[] data;

        /**
         * 创建一个具有指定类型和证书路径编码形式的 {@code CertPathRep}。
         *
         * @param type 证书路径类型的标准名称
         * @param data 证书路径的编码形式
         */
        protected CertPathRep(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }

        /**
         * 从类型和数据构造一个 {@code CertPath}。
         *
         * @return 解析的 {@code CertPath} 对象
         *
         * @throws ObjectStreamException 如果不能构造 {@code CertPath}
         */
        protected Object readResolve() throws ObjectStreamException {
            try {
                CertificateFactory cf = CertificateFactory.getInstance(type);
                return cf.generateCertPath(new ByteArrayInputStream(data));
            } catch (CertificateException ce) {
                NotSerializableException nse =
                    new NotSerializableException
                        ("java.security.cert.CertPath: " + type);
                nse.initCause(ce);
                throw nse;
            }
        }
    }
}
