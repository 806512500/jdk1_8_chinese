
/*
 * 版权所有 (c) 1998, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.security.cert;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.security.Provider;
import java.security.Security;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import sun.security.jca.*;
import sun.security.jca.GetInstance.Instance;

/**
 * 该类定义了证书工厂的功能，用于从其编码中生成证书、认证路径（{@code CertPath}）
 * 和证书撤销列表（CRL）对象。
 *
 * <p>对于由多个证书组成的编码，当您希望解析一个可能无关的证书集合时，使用
 * {@code generateCertificates}。否则，当您希望生成
 * 一个 {@code CertPath}（证书链）并随后使用 {@code CertPathValidator}
 * 验证它时，使用 {@code generateCertPath}。
 *
 * <p>X.509 证书工厂必须返回是 {@code java.security.cert.X509Certificate} 实例的证书，
 * 以及是 {@code java.security.cert.X509CRL} 实例的 CRL。
 *
 * <p>以下示例读取一个包含 Base64 编码证书的文件，每个证书在开头由 -----BEGIN CERTIFICATE----- 标记，
 * 在结尾由 -----END CERTIFICATE----- 标记。我们将 {@code FileInputStream}（不支持 {@code mark}
 * 和 {@code reset}）转换为支持这些方法的 {@code BufferedInputStream}，以便每次调用
 * {@code generateCertificate} 时只消耗一个证书，并且输入流的读取位置被定位到文件中的下一个证书：
 *
 * <pre>{@code
 * FileInputStream fis = new FileInputStream(filename);
 * BufferedInputStream bis = new BufferedInputStream(fis);
 *
 * CertificateFactory cf = CertificateFactory.getInstance("X.509");
 *
 * while (bis.available() > 0) {
 *    Certificate cert = cf.generateCertificate(bis);
 *    System.out.println(cert.toString());
 * }
 * }</pre>
 *
 * <p>以下示例解析存储在文件中的 PKCS#7 格式的证书回复，并从中提取所有证书：
 *
 * <pre>
 * FileInputStream fis = new FileInputStream(filename);
 * CertificateFactory cf = CertificateFactory.getInstance("X.509");
 * Collection c = cf.generateCertificates(fis);
 * Iterator i = c.iterator();
 * while (i.hasNext()) {
 *    Certificate cert = (Certificate)i.next();
 *    System.out.println(cert);
 * }
 * </pre>
 *
 * <p>每个 Java 平台的实现都必须支持以下标准的 {@code CertificateFactory} 类型：
 * <ul>
 * <li>{@code X.509}</li>
 * </ul>
 * 以及以下标准的 {@code CertPath} 编码：
 * <ul>
 * <li>{@code PKCS7}</li>
 * <li>{@code PkiPath}</li>
 * </ul>
 * 类型和编码在 <a href=
 * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertificateFactory">
 * CertificateFactory 部分</a> 和 <a href=
 * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathEncodings">
 * CertPath 编码部分</a> 的 Java 密码学架构标准算法名称文档中描述。请参阅您的实现的发行文档，以查看是否支持其他类型或编码。
 *
 * @author Hemma Prafullchandra
 * @author Jan Luehe
 * @author Sean Mullan
 *
 * @see Certificate
 * @see X509Certificate
 * @see CertPath
 * @see CRL
 * @see X509CRL
 *
 * @since 1.2
 */

public class CertificateFactory {

    // 证书类型
    private String type;

    // 提供者
    private Provider provider;

    // 提供者实现
    private CertificateFactorySpi certFacSpi;

    /**
     * 创建给定类型的 CertificateFactory 对象，并在其内部封装给定的提供者实现（SPI 对象）。
     *
     * @param certFacSpi 提供者实现。
     * @param provider 提供者。
     * @param type 证书类型。
     */
    protected CertificateFactory(CertificateFactorySpi certFacSpi,
                                 Provider provider, String type)
    {
        this.certFacSpi = certFacSpi;
        this.provider = provider;
        this.type = type;
    }

    /**
     * 返回实现指定证书类型的证书工厂对象。
     *
     * <p>此方法遍历注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个新的 CertificateFactory 对象，封装了支持指定类型的第一个
     * 提供者的 CertificateFactorySpi 实现。
     *
     * <p>注意，可以通过 {@link Security#getProviders() Security.getProviders()} 方法
     * 获取注册的提供者列表。
     *
     * @param type 请求的证书类型的名称。
     * 有关标准证书类型的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertificateFactory">
     * Java 密码学架构标准算法名称文档</a> 中的 CertificateFactory 部分。
     *
     * @return 指定类型的证书工厂对象。
     *
     * @exception CertificateException 如果没有提供者支持指定类型的 CertificateFactorySpi 实现。
     *
     * @see java.security.Provider
     */
    public static final CertificateFactory getInstance(String type)
            throws CertificateException {
        try {
            Instance instance = GetInstance.getInstance("CertificateFactory",
                CertificateFactorySpi.class, type);
            return new CertificateFactory((CertificateFactorySpi)instance.impl,
                instance.provider, type);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(type + " not found", e);
        }
    }

                /**
     * 返回指定证书类型的证书工厂对象。
     *
     * <p> 返回一个新的 CertificateFactory 对象，该对象封装了来自指定提供者的
     * CertificateFactorySpi 实现。指定的提供者必须在安全提供者列表中注册。
     *
     * <p> 注意，可以通过 {@link Security#getProviders() Security.getProviders()} 方法
     * 获取已注册的提供者列表。
     *
     * @param type 证书类型。
     * 有关标准证书类型的详细信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertificateFactory">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * 中的 CertificateFactory 部分。
     *
     * @param provider 提供者的名称。
     *
     * @return 指定类型的证书工厂对象。
     *
     * @exception CertificateException 如果指定算法的 CertificateFactorySpi
     *          实现无法从指定的提供者中获得。
     *
     * @exception NoSuchProviderException 如果指定的提供者未在安全提供者列表中注册。
     *
     * @exception IllegalArgumentException 如果提供者的名称为 null
     *          或为空。
     *
     * @see java.security.Provider
     */
    public static final CertificateFactory getInstance(String type,
            String provider) throws CertificateException,
            NoSuchProviderException {
        try {
            Instance instance = GetInstance.getInstance("CertificateFactory",
                CertificateFactorySpi.class, type, provider);
            return new CertificateFactory((CertificateFactorySpi)instance.impl,
                instance.provider, type);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(type + " not found", e);
        }
    }

    /**
     * 返回指定证书类型的证书工厂对象。
     *
     * <p> 返回一个新的 CertificateFactory 对象，该对象封装了来自指定 Provider
     * 对象的 CertificateFactorySpi 实现。注意，指定的 Provider 对象
     * 不必在提供者列表中注册。
     *
     * @param type 证书类型。
     * 有关标准证书类型的详细信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertificateFactory">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * 中的 CertificateFactory 部分。
     * @param provider 提供者。
     *
     * @return 指定类型的证书工厂对象。
     *
     * @exception CertificateException 如果指定算法的 CertificateFactorySpi
     *          实现无法从指定的 Provider 对象中获得。
     *
     * @exception IllegalArgumentException 如果 {@code provider} 为
     *          null。
     *
     * @see java.security.Provider
     *
     * @since 1.4
     */
    public static final CertificateFactory getInstance(String type,
            Provider provider) throws CertificateException {
        try {
            Instance instance = GetInstance.getInstance("CertificateFactory",
                CertificateFactorySpi.class, type, provider);
            return new CertificateFactory((CertificateFactorySpi)instance.impl,
                instance.provider, type);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(type + " not found", e);
        }
    }

    /**
     * 返回此证书工厂的提供者。
     *
     * @return 此证书工厂的提供者。
     */
    public final Provider getProvider() {
        return this.provider;
    }

    /**
     * 返回与此证书工厂关联的证书类型的名称。
     *
     * @return 与此证书工厂关联的证书类型的名称。
     */
    public final String getType() {
        return this.type;
    }

    /**
     * 生成一个证书对象并使用从输入流 {@code inStream} 读取的数据初始化它。
     *
     * <p>为了利用此证书工厂支持的专用证书格式，
     * 可以将返回的证书对象转换为相应的证书类。例如，如果此证书
     * 工厂实现 X.509 证书，可以将返回的证书对象转换为 {@code X509Certificate} 类。
     *
     * <p>对于 X.509 证书的证书工厂，提供给 {@code inStream} 的证书必须是 DER 编码的，
     * 可以以二进制或可打印（Base64）编码形式提供。如果证书以 Base64 编码提供，则必须以
     * -----BEGIN CERTIFICATE----- 开头，并以 -----END CERTIFICATE----- 结尾。
     *
     * <p>注意，如果给定的输入流不支持
     * {@link java.io.InputStream#mark(int) mark} 和
     * {@link java.io.InputStream#reset() reset}，此方法将
     * 消耗整个输入流。否则，每次调用此方法都会消耗一个证书，并将输入流的读取位置定位到
     * 证书固有的结束标记之后的下一个可用字节。如果输入流中的数据
     * 不包含固有的证书结束标记（除了 EOF 之外）并且在解析证书后有尾随数据，则会抛出
     * {@code CertificateException}。
     *
     * @param inStream 包含证书数据的输入流。
     *
     * @return 使用输入流中的数据初始化的证书对象。
     *
     * @exception CertificateException 在解析错误时。
     */
    public final Certificate generateCertificate(InputStream inStream)
        throws CertificateException
    {
        return certFacSpi.engineGenerateCertificate(inStream);
    }

                /**
     * 返回由此证书工厂支持的 {@code CertPath} 编码的迭代，以默认编码为首。有关标准编码名称及其格式的信息，请参阅
     * <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathEncodings">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * 中的 CertPath 编码部分。
     * <p>
     * 试图通过返回的 {@code Iterator} 的 {@code remove} 方法修改返回的 {@code Iterator} 将导致
     * {@code UnsupportedOperationException}。
     *
     * @return 一个迭代器，包含支持的 {@code CertPath} 编码的名称（作为 {@code String}）
     * @since 1.4
     */
    public final Iterator<String> getCertPathEncodings() {
        return(certFacSpi.engineGetCertPathEncodings());
    }

    /**
     * 生成一个 {@code CertPath} 对象，并使用从 {@code InputStream} inStream 读取的数据初始化它。假设数据为默认编码。默认编码的名称是
     * {@link #getCertPathEncodings getCertPathEncodings} 方法返回的 {@code Iterator} 的第一个元素。
     *
     * @param inStream 包含数据的 {@code InputStream}
     * @return 一个使用来自 {@code InputStream} 的数据初始化的 {@code CertPath}
     * @exception CertificateException 如果解码时发生异常
     * @since 1.4
     */
    public final CertPath generateCertPath(InputStream inStream)
        throws CertificateException
    {
        return(certFacSpi.engineGenerateCertPath(inStream));
    }

    /**
     * 生成一个 {@code CertPath} 对象，并使用从 {@code InputStream} inStream 读取的数据初始化它。假设数据为指定的编码。有关标准编码名称及其格式的信息，请参阅
     * <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathEncodings">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * 中的 CertPath 编码部分。
     *
     * @param inStream 包含数据的 {@code InputStream}
     * @param encoding 数据的编码
     * @return 一个使用来自 {@code InputStream} 的数据初始化的 {@code CertPath}
     * @exception CertificateException 如果解码时发生异常或请求的编码不受支持
     * @since 1.4
     */
    public final CertPath generateCertPath(InputStream inStream,
        String encoding) throws CertificateException
    {
        return(certFacSpi.engineGenerateCertPath(inStream, encoding));
    }

    /**
     * 生成一个 {@code CertPath} 对象，并使用 {@code List} 中的 {@code Certificate} 初始化它。
     * <p>
     * 提供的证书必须是 {@code CertificateFactory} 支持的类型。它们将从提供的 {@code List} 对象中复制出来。
     *
     * @param certificates 一个包含 {@code Certificate} 的 {@code List}
     * @return 一个使用提供的证书列表初始化的 {@code CertPath}
     * @exception CertificateException 如果发生异常
     * @since 1.4
     */
    public final CertPath
        generateCertPath(List<? extends Certificate> certificates)
        throws CertificateException
    {
        return(certFacSpi.engineGenerateCertPath(certificates));
    }

    /**
     * 返回从给定输入流 {@code inStream} 读取的证书的（可能为空的）集合视图。
     *
     * <p>为了利用此证书工厂支持的专用证书格式，可以将返回的集合视图中的每个元素转换为相应的证书类。例如，如果此证书
     * 工厂实现 X.509 证书，则可以将返回的集合中的元素转换为 {@code X509Certificate} 类。
     *
     * <p>对于 X.509 证书的证书工厂，{@code inStream} 可能包含
     * {@link #generateCertificate(java.io.InputStream) generateCertificate} 中描述的格式的 DER 编码证书序列。
     * 此外，{@code inStream} 可能包含 PKCS#7 证书链。这是一个 PKCS#7 <i>SignedData</i> 对象，唯一重要的字段是 <i>certificates</i>。特别是，签名和内容被忽略。这种格式允许多个证书一次性下载。如果没有证书存在，则返回一个空集合。
     *
     * <p>请注意，如果给定的输入流不支持
     * {@link java.io.InputStream#mark(int) mark} 和
     * {@link java.io.InputStream#reset() reset}，此方法将消耗整个输入流。
     *
     * @param inStream 包含证书的输入流。
     *
     * @return 一个（可能为空的）java.security.cert.Certificate 对象集合视图
     * 初始化为输入流中的数据。
     *
     * @exception CertificateException 解析错误时。
     */
    public final Collection<? extends Certificate> generateCertificates
            (InputStream inStream) throws CertificateException {
        return certFacSpi.engineGenerateCertificates(inStream);
    }

    /**
     * 生成一个证书撤销列表 (CRL) 对象，并使用从输入流 {@code inStream} 读取的数据初始化它。
     *
     * <p>为了利用此证书工厂支持的专用 CRL 格式，
     * 可以将返回的 CRL 对象转换为相应的 CRL 类。例如，如果此证书
     * 工厂实现 X.509 CRLs，返回的 CRL 对象
     * 可以转换为 {@code X509CRL} 类。
     *
     * <p>请注意，如果给定的输入流不支持
     * {@link java.io.InputStream#mark(int) mark} 和
     * {@link java.io.InputStream#reset() reset}，此方法将消耗整个输入流。否则，每次调用此
     * 方法都会消耗一个 CRL，并将输入流的读取位置定位到 CRL 内部结束标记后的下一个可用字节。如果输入流中的数据不包含 CRL 内部结束标记（除了 EOF 之外）并且在解析 CRL 后有尾随数据，则会抛出
     * {@code CRLException}。
     *
     * @param inStream 包含 CRL 数据的输入流。
     *
     * @return 一个使用输入流中的数据初始化的 CRL 对象。
     *
     * @exception CRLException 解析错误时。
     */
    public final CRL generateCRL(InputStream inStream)
        throws CRLException
    {
        return certFacSpi.engineGenerateCRL(inStream);
    }

                /**
     * 返回从给定输入流 {@code inStream} 读取的 CRLs 的（可能是空的）集合视图。
     *
     * <p>为了利用此证书工厂支持的专用 CRL 格式，返回的集合视图中的每个元素可以转换为相应的
     * CRL 类。例如，如果此证书工厂实现了 X.509 CRLs，返回的集合中的元素可以转换为
     * {@code X509CRL} 类。
     *
     * <p>对于 X.509 CRLs 的证书工厂，{@code inStream} 可能包含一系列 DER 编码的 CRLs。
     * 此外，{@code inStream} 可能包含一个 PKCS#7 CRL
     * 集。这是一个 PKCS#7 <i>SignedData</i> 对象，唯一重要的字段是 <i>crls</i>。特别是，
     * 签名和内容被忽略。此格式允许一次下载多个 CRLs。如果没有 CRLs，将返回一个空集合。
     *
     * <p>请注意，如果给定的输入流不支持
     * {@link java.io.InputStream#mark(int) mark} 和
     * {@link java.io.InputStream#reset() reset}，此方法将
     * 消耗整个输入流。
     *
     * @param inStream 包含 CRLs 的输入流。
     *
     * @return 一个（可能是空的）集合视图，其中包含从输入流初始化的
     * java.security.cert.CRL 对象。
     *
     * @exception CRLException 解析错误时抛出。
     */
    public final Collection<? extends CRL> generateCRLs(InputStream inStream)
            throws CRLException {
        return certFacSpi.engineGenerateCRLs(inStream);
    }
}
