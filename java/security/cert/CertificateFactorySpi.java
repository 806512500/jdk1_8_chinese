
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
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * 该类定义了 {@code CertificateFactory} 类的 <i>服务提供者接口</i> (<b>SPI</b>)。
 * 本类中的所有抽象方法都必须由希望为特定证书类型（例如 X.509）提供证书工厂实现的每个加密服务提供者实现。
 *
 * <p>证书工厂用于从其编码生成证书、认证路径（{@code CertPath}）和证书撤销列表 (CRL) 对象。
 *
 * <p>X.509 证书工厂必须返回 {@code java.security.cert.X509Certificate} 的实例，以及 {@code java.security.cert.X509CRL} 的实例。
 *
 * @author Hemma Prafullchandra
 * @author Jan Luehe
 * @author Sean Mullan
 *
 *
 * @see CertificateFactory
 * @see Certificate
 * @see X509Certificate
 * @see CertPath
 * @see CRL
 * @see X509CRL
 *
 * @since 1.2
 */

public abstract class CertificateFactorySpi {

    /**
     * 生成一个证书对象并使用从输入流 {@code inStream} 读取的数据初始化它。
     *
     * <p>为了利用此证书工厂支持的专用证书格式，
     * 返回的证书对象可以转换为相应的证书类。例如，如果此证书
     * 工厂实现了 X.509 证书，返回的证书对象可以转换为 {@code X509Certificate} 类。
     *
     * <p>对于 X.509 证书工厂，提供给 {@code inStream} 的证书必须是 DER 编码的，
     * 可以以二进制或可打印（Base64）编码提供。如果证书以 Base64 编码提供，则必须在开头以 -----BEGIN CERTIFICATE----- 开头，并在结尾以 -----END CERTIFICATE----- 结尾。
     *
     * <p>请注意，如果给定的输入流不支持
     * {@link java.io.InputStream#mark(int) mark} 和
     * {@link java.io.InputStream#reset() reset}，此方法将
     * 消耗整个输入流。否则，每次调用此方法都会消耗一个证书，并将输入流的读取位置定位到证书固有的结束标记之后的下一个可用字节。如果输入流中的数据不包含固有的证书结束标记（除了 EOF）并且在解析证书后有尾随数据，则会抛出 {@code CertificateException}。
     *
     * @param inStream 包含证书数据的输入流。
     *
     * @return 用输入流中的数据初始化的证书对象。
     *
     * @exception CertificateException 解析错误时抛出。
     */
    public abstract Certificate engineGenerateCertificate(InputStream inStream)
        throws CertificateException;

    /**
     * 生成一个 {@code CertPath} 对象并使用从 {@code InputStream} inStream 读取的数据初始化它。数据
     * 假定为默认编码。
     *
     * <p>此方法是在 Java 2 平台标准版 1.4 版本中添加的。为了保持与现有服务提供者的向后兼容性，
     * 此方法不能是 {@code abstract}，默认情况下抛出 {@code UnsupportedOperationException}。
     *
     * @param inStream 包含数据的 {@code InputStream}
     * @return 用 {@code InputStream} 中的数据初始化的 {@code CertPath}
     * @exception CertificateException 解码时发生异常
     * @exception UnsupportedOperationException 如果方法不受支持
     * @since 1.4
     */
    public CertPath engineGenerateCertPath(InputStream inStream)
        throws CertificateException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * 生成一个 {@code CertPath} 对象并使用从 {@code InputStream} inStream 读取的数据初始化它。数据
     * 假定为指定的编码。
     *
     * <p>此方法是在 Java 2 平台标准版 1.4 版本中添加的。为了保持与现有服务提供者的向后兼容性，
     * 此方法不能是 {@code abstract}，默认情况下抛出 {@code UnsupportedOperationException}。
     *
     * @param inStream 包含数据的 {@code InputStream}
     * @param encoding 数据使用的编码
     * @return 用 {@code InputStream} 中的数据初始化的 {@code CertPath}
     * @exception CertificateException 解码时发生异常或请求的编码不受支持
     * @exception UnsupportedOperationException 如果方法不受支持
     * @since 1.4
     */
    public CertPath engineGenerateCertPath(InputStream inStream,
        String encoding) throws CertificateException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * 生成一个 {@code CertPath} 对象并使用 {@code List} 中的 {@code Certificate} 初始化它。
     * <p>
     * 提供的证书必须是 {@code CertificateFactory} 支持的类型。它们将从提供的
     * {@code List} 对象中复制出来。
     *
     * <p>此方法是在 Java 2 平台标准版 1.4 版本中添加的。为了保持与现有服务提供者的向后兼容性，
     * 此方法不能是 {@code abstract}，默认情况下抛出 {@code UnsupportedOperationException}。
     *
     * @param certificates 一个 {@code Certificate} 的 {@code List}
     * @return 用提供的证书列表初始化的 {@code CertPath}
     * @exception CertificateException 如果发生异常
     * @exception UnsupportedOperationException 如果方法不受支持
     * @since 1.4
     */
    public CertPath
        engineGenerateCertPath(List<? extends Certificate> certificates)
        throws CertificateException
    {
        throw new UnsupportedOperationException();
    }


                /**
     * 返回由该证书工厂支持的 {@code CertPath} 编码的迭代，首先列出默认编码。有关标准编码名称的信息，请参阅
     * <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathEncodings">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * 中的 CertPath 编码部分。
     * <p>
     * 试图通过返回的 {@code Iterator} 的 {@code remove} 方法修改返回的 {@code Iterator} 将导致
     * {@code UnsupportedOperationException}。
     *
     * <p> 此方法是在 Java 2 平台标准版 1.4 版中添加的。为了保持与现有服务提供者的向后兼容性，此方法不能是 {@code abstract}，
     * 默认情况下会抛出 {@code UnsupportedOperationException}。
     *
     * @return 支持的 {@code CertPath} 编码名称（作为 {@code String}）的 {@code Iterator}
     * @exception UnsupportedOperationException 如果该方法不受支持
     * @since 1.4
     */
    public Iterator<String> engineGetCertPathEncodings() {
        throw new UnsupportedOperationException();
    }

    /**
     * 从给定的输入流 {@code inStream} 读取证书，返回一个（可能为空的）集合视图。
     *
     * <p>为了利用此证书工厂支持的专用证书格式，返回的集合视图中的每个元素都可以转换为相应的证书类。例如，如果此证书
     * 工厂实现 X.509 证书，返回的集合中的元素可以转换为 {@code X509Certificate} 类。
     *
     * <p>对于 X.509 证书的证书工厂，{@code inStream} 可能包含一个 DER 编码的证书，格式如
     * {@link CertificateFactory#generateCertificate(java.io.InputStream)
     * generateCertificate} 所描述。此外，{@code inStream} 可能包含一个 PKCS#7 证书链。这是一个 PKCS#7 <i>SignedData</i> 对象，
     * 唯一重要的字段是 <i>certificates</i>。特别是，签名和内容被忽略。这种格式允许一次下载多个证书。如果没有证书存在，
     * 返回一个空的集合。
     *
     * <p>注意，如果给定的输入流不支持
     * {@link java.io.InputStream#mark(int) mark} 和
     * {@link java.io.InputStream#reset() reset}，此方法将消耗整个输入流。
     *
     * @param inStream 包含证书的输入流。
     *
     * @return 一个（可能为空的）java.security.cert.Certificate 对象集合视图，使用输入流中的数据初始化。
     *
     * @exception CertificateException 解析错误时抛出。
     */
    public abstract Collection<? extends Certificate>
            engineGenerateCertificates(InputStream inStream)
            throws CertificateException;

    /**
     * 生成一个证书撤销列表 (CRL) 对象，并使用从输入流 {@code inStream} 读取的数据初始化它。
     *
     * <p>为了利用此证书工厂支持的专用 CRL 格式，
     * 返回的 CRL 对象可以转换为相应的 CRL 类。例如，如果此证书
     * 工厂实现 X.509 CRLs，返回的 CRL 对象可以转换为 {@code X509CRL} 类。
     *
     * <p>注意，如果给定的输入流不支持
     * {@link java.io.InputStream#mark(int) mark} 和
     * {@link java.io.InputStream#reset() reset}，此方法将消耗整个输入流。否则，每次调用此
     * 方法消耗一个 CRL，并将输入流的读取位置定位到 CRL 内在结束标记后的下一个可用字节。如果输入流中的数据不包含 CRL 的内在结束标记（除了 EOF 之外）
     * 并且在解析 CRL 后有尾随数据，将抛出 {@code CRLException}。
     *
     * @param inStream 包含 CRL 数据的输入流。
     *
     * @return 一个使用输入流中的数据初始化的 CRL 对象。
     *
     * @exception CRLException 解析错误时抛出。
     */
    public abstract CRL engineGenerateCRL(InputStream inStream)
        throws CRLException;

    /**
     * 从给定的输入流 {@code inStream} 读取 CRL，返回一个（可能为空的）集合视图。
     *
     * <p>为了利用此证书工厂支持的专用 CRL 格式，返回的集合视图中的每个元素都可以转换为相应的 CRL 类。例如，如果此证书
     * 工厂实现 X.509 CRLs，返回的集合中的元素可以转换为 {@code X509CRL} 类。
     *
     * <p>对于 X.509 CRLs 的证书工厂，
     * {@code inStream} 可能包含一个 DER 编码的 CRL。此外，{@code inStream} 可能包含一个 PKCS#7 CRL
     * 集合。这是一个 PKCS#7 <i>SignedData</i> 对象，唯一重要的字段是 <i>crls</i>。特别是，签名和内容被忽略。这种格式允许一次下载多个 CRL。如果没有 CRL 存在，
     * 返回一个空的集合。
     *
     * <p>注意，如果给定的输入流不支持
     * {@link java.io.InputStream#mark(int) mark} 和
     * {@link java.io.InputStream#reset() reset}，此方法将消耗整个输入流。
     *
     * @param inStream 包含 CRLs 的输入流。
     *
     * @return 一个（可能为空的）java.security.cert.CRL 对象集合视图，使用输入流中的数据初始化。
     *
     * @exception CRLException 解析错误时抛出。
     */
    public abstract Collection<? extends CRL> engineGenerateCRLs
            (InputStream inStream) throws CRLException;
}
