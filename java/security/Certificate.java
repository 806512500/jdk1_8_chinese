/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.util.Date;

/**
 * <p>这是一个用于管理各种身份证书的抽象方法接口。
 * 身份证书是由一个主体（principal）对另一个主体的公钥的保证。主体可以代表个人用户、组或公司。
 *
 * <p>特别是，此接口旨在成为具有不同格式但重要共同用途的构造的通用抽象。例如，不同类型的证书，如X.509证书和PGP证书，
 * 具有通用的证书功能（编码和解码证书的需求）和一些类型的信息，如公钥、拥有该公钥的主体以及保证该公钥属于指定主体的保证人。
 * 因此，X.509证书的实现和PGP证书的实现都可以利用Certificate接口，即使它们的格式和存储的额外类型和数量的信息不同。
 *
 * <p><b>重要</b>：此接口对于编目和分组共享某些共同用途的对象很有用。它本身没有任何语义。特别是，Certificate对象不会对绑定的<i>有效性</i>做出任何声明。
 * 实现此接口的应用程序有责任验证证书并确保其有效性。
 *
 * @author Benjamin Renaud
 * @deprecated 在Java平台中创建了一个新的证书处理包。
 *             此Certificate接口完全被弃用，
 *             保留在此是为了平稳过渡到新的
 *             包。
 * @see java.security.cert.Certificate
 */
@Deprecated
public interface Certificate {

    /**
     * 返回证书的保证人，即保证此证书关联的公钥属于证书关联的主体的主体。对于X.509证书，保证人通常是证书机构
     * （如美国邮政服务或Verisign, Inc.）。
     *
     * @return 保证主体-密钥绑定的保证人。
     */
    public abstract Principal getGuarantor();

    /**
     * 返回由保证人保证的主体-密钥对的主体。
     *
     * @return 与此证书绑定的主体。
     */
    public abstract Principal getPrincipal();

    /**
     * 返回由保证人保证的主体-密钥对的密钥。
     *
     * @return 该证书证明属于特定主体的公钥。
     */
    public abstract PublicKey getPublicKey();

    /**
     * 将证书编码到输出流中，格式可以由{@code decode}方法解码。
     *
     * @param stream 要编码证书的输出流。
     *
     * @exception KeyException 如果证书未正确初始化，或数据缺失等。
     *
     * @exception IOException 如果尝试将编码后的证书输出到输出流时发生流异常。
     *
     * @see #decode
     * @see #getFormat
     */
    public abstract void encode(OutputStream stream)
        throws KeyException, IOException;

    /**
     * 从输入流中解码证书。格式应该是由{@code getFormat}返回并由
     * {@code encode}生成的格式。
     *
     * @param stream 从中获取解码数据的输入流。
     *
     * @exception KeyException 如果证书未正确初始化，或数据缺失等。
     *
     * @exception IOException 如果尝试从输入流中输入编码后的证书时发生异常。
     *
     * @see #encode
     * @see #getFormat
     */
    public abstract void decode(InputStream stream)
        throws KeyException, IOException;


    /**
     * 返回编码格式的名称。这用于作为找到合适解析器的提示。可能是"X.509"、"PGP"等。这是
     * 由{@code encode}和{@code decode}方法生成和理解的格式。
     *
     * @return 编码格式的名称。
     */
    public abstract String getFormat();

    /**
     * 返回表示证书内容的字符串。
     *
     * @param detailed 是否提供关于证书的详细信息
     *
     * @return 代表证书内容的字符串
     */
    public String toString(boolean detailed);
}
