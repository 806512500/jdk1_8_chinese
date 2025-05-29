
/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import sun.security.util.Debug;

import sun.security.jca.*;
import sun.security.jca.GetInstance.Instance;

/**
 * 用于构建认证路径（也称为证书链）的类。
 * <p>
 * 该类使用基于提供者的架构。
 * 要创建一个 {@code CertPathBuilder}，调用
 * 一个静态的 {@code getInstance} 方法，传入
 * 所需的 {@code CertPathBuilder} 的算法名称，以及可选的
 * 提供者名称。
 *
 * <p>一旦创建了 {@code CertPathBuilder} 对象，就可以通过调用
 * {@link #build build} 方法并传入特定算法的参数来构建认证路径。
 * 如果成功，结果（包括构建的 {@code CertPath}）将
 * 在实现 {@code CertPathBuilderResult} 接口的对象中返回。
 *
 * <p>{@link #getRevocationChecker} 方法允许应用程序指定
 * 用于检查证书撤销状态的特定算法参数和选项。
 * 以下是一个使用 PKIX 算法的示例：
 *
 * <pre>
 * CertPathBuilder cpb = CertPathBuilder.getInstance("PKIX");
 * PKIXRevocationChecker rc = (PKIXRevocationChecker)cpb.getRevocationChecker();
 * rc.setOptions(EnumSet.of(Option.PREFER_CRLS));
 * params.addCertPathChecker(rc);
 * CertPathBuilderResult cpbr = cpb.build(params);
 * </pre>
 *
 * <p>每个 Java 平台的实现都必须支持以下标准的 {@code CertPathBuilder} 算法：
 * <ul>
 * <li>{@code PKIX}</li>
 * </ul>
 * 该算法在 <a href=
 * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathBuilder">
 * Java 密码架构标准算法名称文档的 CertPathBuilder 部分</a>中描述。
 * 请参阅您的实现的发行文档，以查看是否支持其他算法。
 *
 * <p>
 * <b>并发访问</b>
 * <p>
 * 该类的静态方法保证是线程安全的。
 * 多个线程可以并发调用该类定义的静态方法，而不会产生不良影响。
 * <p>
 * 但是，对于该类定义的非静态方法则不成立。
 * 除非特定提供者另有文档说明，否则需要并发访问单个
 * {@code CertPathBuilder} 实例的线程应该同步并提供必要的锁定。
 * 每个操作不同 {@code CertPathBuilder} 实例的多个线程不需要同步。
 *
 * @see CertPath
 *
 * @since       1.4
 * @author      Sean Mullan
 * @author      Yassir Elley
 */
public class CertPathBuilder {

    /*
     * 用于在 Security 属性文件中查找的常量，以确定
     * 默认的 certpathbuilder 类型。在 Security 属性文件中，
     * 默认的 certpathbuilder 类型如下所示：
     * <pre>
     * certpathbuilder.type=PKIX
     * </pre>
     */
    private static final String CPB_TYPE = "certpathbuilder.type";
    private final CertPathBuilderSpi builderSpi;
    private final Provider provider;
    private final String algorithm;

    /**
     * 创建给定算法的 {@code CertPathBuilder} 对象，
     * 并将其封装在给定的提供者实现（SPI 对象）中。
     *
     * @param builderSpi 提供者实现
     * @param provider 提供者
     * @param algorithm 算法名称
     */
    protected CertPathBuilder(CertPathBuilderSpi builderSpi, Provider provider,
        String algorithm)
    {
        this.builderSpi = builderSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    /**
     * 返回实现指定算法的 {@code CertPathBuilder} 对象。
     *
     * <p> 该方法遍历注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个新的 CertPathBuilder 对象，封装了
     * 第一个支持指定算法的提供者的 CertPathBuilderSpi 实现。
     *
     * <p> 注意，可以通过
     * {@link Security#getProviders() Security.getProviders()} 方法检索
     * 注册的提供者列表。
     *
     * @param algorithm 请求的 {@code CertPathBuilder} 算法名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     *  "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathBuilder">
     * Java 密码架构标准算法名称文档的 CertPathBuilder 部分</a>。
     *
     * @return 实现指定算法的 {@code CertPathBuilder} 对象。
     *
     * @throws NoSuchAlgorithmException 如果没有提供者支持
     *          指定算法的 CertPathBuilderSpi 实现。
     *
     * @see java.security.Provider
     */
    public static CertPathBuilder getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("CertPathBuilder",
            CertPathBuilderSpi.class, algorithm);
        return new CertPathBuilder((CertPathBuilderSpi)instance.impl,
            instance.provider, algorithm);
    }

    /**
     * 返回实现指定算法的 {@code CertPathBuilder} 对象。
     *
     * <p> 返回一个新的 CertPathBuilder 对象，封装了
     * 指定提供者的 CertPathBuilderSpi 实现。指定的提供者必须
     * 在安全提供者列表中注册。
     *
     * <p> 注意，可以通过
     * {@link Security#getProviders() Security.getProviders()} 方法检索
     * 注册的提供者列表。
     *
     * @param algorithm 请求的 {@code CertPathBuilder} 算法名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     *  "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathBuilder">
     * Java 密码架构标准算法名称文档的 CertPathBuilder 部分</a>。
     *
     * @param provider 提供者的名称。
     *
     * @return 实现指定算法的 {@code CertPathBuilder} 对象。
     *
     * @throws NoSuchAlgorithmException 如果指定提供者不支持
     *          指定算法的 CertPathBuilderSpi 实现。
     *
     * @throws NoSuchProviderException 如果指定的提供者未
     *          在安全提供者列表中注册。
     *
     * @exception IllegalArgumentException 如果 {@code provider} 为
     *          null 或空。
     *
     * @see java.security.Provider
     */
    public static CertPathBuilder getInstance(String algorithm, String provider)
           throws NoSuchAlgorithmException, NoSuchProviderException {
        Instance instance = GetInstance.getInstance("CertPathBuilder",
            CertPathBuilderSpi.class, algorithm, provider);
        return new CertPathBuilder((CertPathBuilderSpi)instance.impl,
            instance.provider, algorithm);
    }


                /**
     * 返回实现指定算法的 {@code CertPathBuilder} 对象。
     *
     * <p> 返回一个新的 CertPathBuilder 对象，封装了来自指定 Provider
     * 对象的 CertPathBuilderSpi 实现。请注意，指定的 Provider 对象
     * 不必注册在提供者列表中。
     *
     * @param algorithm 请求的 {@code CertPathBuilder} 算法的名称。请参阅
     *  <a href="{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathBuilder">
     * Java 密码架构标准算法名称文档</a>中的 CertPathBuilder 部分，了解标准算法名称的信息。
     *
     * @param provider 提供者。
     *
     * @return 实现指定算法的 {@code CertPathBuilder} 对象。
     *
     * @exception NoSuchAlgorithmException 如果指定提供者对象中没有可用的
     *          指定算法的 CertPathBuilderSpi 实现。
     *
     * @exception IllegalArgumentException 如果 {@code provider} 为
     *          null。
     *
     * @see java.security.Provider
     */
    public static CertPathBuilder getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("CertPathBuilder",
            CertPathBuilderSpi.class, algorithm, provider);
        return new CertPathBuilder((CertPathBuilderSpi)instance.impl,
            instance.provider, algorithm);
    }

    /**
     * 返回此 {@code CertPathBuilder} 的提供者。
     *
     * @return 此 {@code CertPathBuilder} 的提供者。
     */
    public final Provider getProvider() {
        return this.provider;
    }

    /**
     * 返回此 {@code CertPathBuilder} 的算法名称。
     *
     * @return 此 {@code CertPathBuilder} 的算法名称。
     */
    public final String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * 尝试使用指定的算法参数集构建证书路径。
     *
     * @param params 算法参数
     * @return 构建算法的结果
     * @throws CertPathBuilderException 如果构建器无法构造满足指定参数的
     *  证书路径
     * @throws InvalidAlgorithmParameterException 如果指定的参数
     * 不适合此 {@code CertPathBuilder}
     */
    public final CertPathBuilderResult build(CertPathParameters params)
        throws CertPathBuilderException, InvalidAlgorithmParameterException
    {
        return builderSpi.engineBuild(params);
    }

    /**
     * 返回由 {@code certpathbuilder.type} 安全属性指定的默认
     * {@code CertPathBuilder} 类型，如果没有这样的属性，则返回字符串
     * {@literal "PKIX"}。
     *
     * <p>默认的 {@code CertPathBuilder} 类型可以被不希望在调用
     * {@code getInstance} 方法时使用硬编码类型的应用程序使用，并且在用户
     * 没有指定自己的类型时提供默认类型。
     *
     * <p>可以通过将 {@code certpathbuilder.type} 安全属性的值设置为所需的类型来
     * 更改默认的 {@code CertPathBuilder} 类型。
     *
     * @see java.security.Security 安全属性
     * @return 由 {@code certpathbuilder.type} 安全属性指定的默认
     * {@code CertPathBuilder} 类型，如果没有这样的属性，则返回字符串
     * {@literal "PKIX"}。
     */
    public final static String getDefaultType() {
        String cpbtype =
            AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return Security.getProperty(CPB_TYPE);
                }
            });
        return (cpbtype == null) ? "PKIX" : cpbtype;
    }

    /**
     * 返回封装的 {@code CertPathBuilderSpi} 实现用于检查证书撤销状态的
     * {@code CertPathChecker}。PKIX 实现返回类型为
     * {@code PKIXRevocationChecker} 的对象。此方法的每次调用
     * 都返回一个新的 {@code CertPathChecker} 实例。
     *
     * <p>此方法的主要目的是允许调用者指定特定于撤销检查的附加输入参数和选项。
     * 请参阅类描述中的示例。
     *
     * @return 一个 {@code CertPathChecker}
     * @throws UnsupportedOperationException 如果服务提供者不支持此方法
     * @since 1.8
     */
    public final CertPathChecker getRevocationChecker() {
        return builderSpi.engineGetRevocationChecker();
    }
}
