
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
 * 用于验证认证路径（也称为证书链）的类。
 * <p>
 * 本类使用基于提供者的架构。
 * 要创建一个 {@code CertPathValidator}，
 * 调用其中一个静态 {@code getInstance} 方法，传入所需的
 * {@code CertPathValidator} 算法名称，并可选地传入所需提供者的名称。
 *
 * <p>一旦创建了 {@code CertPathValidator} 对象，就可以通过调用
 * {@link #validate validate} 方法并传入要验证的 {@code CertPath}
 * 和特定算法的参数来验证认证路径。如果成功，结果将返回到实现
 * {@code CertPathValidatorResult} 接口的对象中。
 *
 * <p>{@link #getRevocationChecker} 方法允许应用程序指定
 * 用于检查证书撤销状态的特定算法参数和选项。以下是一个使用 PKIX
 * 算法的示例：
 *
 * <pre>
 * CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
 * PKIXRevocationChecker rc = (PKIXRevocationChecker)cpv.getRevocationChecker();
 * rc.setOptions(EnumSet.of(Option.SOFT_FAIL));
 * params.addCertPathChecker(rc);
 * CertPathValidatorResult cpvr = cpv.validate(path, params);
 * </pre>
 *
 * <p>每个 Java 平台的实现都必须支持以下标准的
 * {@code CertPathValidator} 算法：
 * <ul>
 * <li>{@code PKIX}</li>
 * </ul>
 * 该算法在 <a href=
 * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathValidator">
 * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
 * 的 CertPathValidator 部分中描述。请参阅您的实现的发行文档，以查看是否支持其他算法。
 *
 * <p>
 * <b>并发访问</b>
 * <p>
 * 本类的静态方法保证是线程安全的。
 * 多个线程可以并发调用本类定义的静态方法，不会产生不良影响。
 * <p>
 * 但是，对于本类定义的非静态方法，情况并非如此。
 * 除非特定提供者另有文档说明，否则需要并发访问单个
 * {@code CertPathValidator} 实例的线程应相互同步并提供必要的锁定。
 * 每个操作不同 {@code CertPathValidator} 实例的多个线程无需同步。
 *
 * @see CertPath
 *
 * @since       1.4
 * @author      Yassir Elley
 */
public class CertPathValidator {

    /*
     * 用于在 Security 属性文件中查找以确定默认的 certpathvalidator 类型的常量。
     * 在 Security 属性文件中，默认的 certpathvalidator 类型如下所示：
     * <pre>
     * certpathvalidator.type=PKIX
     * </pre>
     */
    private static final String CPV_TYPE = "certpathvalidator.type";
    private final CertPathValidatorSpi validatorSpi;
    private final Provider provider;
    private final String algorithm;

    /**
     * 创建给定算法的 {@code CertPathValidator} 对象，并将其封装在给定的提供者实现（SPI 对象）中。
     *
     * @param validatorSpi 提供者实现
     * @param provider 提供者
     * @param algorithm 算法名称
     */
    protected CertPathValidator(CertPathValidatorSpi validatorSpi,
        Provider provider, String algorithm)
    {
        this.validatorSpi = validatorSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    /**
     * 返回实现指定算法的 {@code CertPathValidator} 对象。
     *
     * <p>此方法遍历注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个新的 CertPathValidator 对象，封装了支持指定算法的第一个
     * 提供者的 CertPathValidatorSpi 实现。
     *
     * <p>注意，可以通过
     * {@link Security#getProviders() Security.getProviders()} 方法检索注册的提供者列表。
     *
     * @param algorithm 请求的 {@code CertPathValidator} 算法的名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     *  "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathValidator">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * 的 CertPathValidator 部分。
     *
     * @return 实现指定算法的 {@code CertPathValidator} 对象。
     *
     * @exception NoSuchAlgorithmException 如果没有提供者支持指定算法的
     *          CertPathValidatorSpi 实现。
     *
     * @see java.security.Provider
     */
    public static CertPathValidator getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("CertPathValidator",
            CertPathValidatorSpi.class, algorithm);
        return new CertPathValidator((CertPathValidatorSpi)instance.impl,
            instance.provider, algorithm);
    }

    /**
     * 返回实现指定算法的 {@code CertPathValidator} 对象。
     *
     * <p>返回一个新的 CertPathValidator 对象，封装了指定提供者的
     * CertPathValidatorSpi 实现。指定的提供者必须注册在安全提供者列表中。
     *
     * <p>注意，可以通过
     * {@link Security#getProviders() Security.getProviders()} 方法检索注册的提供者列表。
     *
     * @param algorithm 请求的 {@code CertPathValidator} 算法的名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     *  "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathValidator">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * 的 CertPathValidator 部分。
     *
     * @param provider 提供者的名称。
     *
     * @return 实现指定算法的 {@code CertPathValidator} 对象。
     *
     * @exception NoSuchAlgorithmException 如果指定提供者不支持指定算法的
     *          CertPathValidatorSpi 实现。
     *
     * @exception NoSuchProviderException 如果指定的提供者未注册在安全提供者列表中。
     *
     * @exception IllegalArgumentException 如果 {@code provider} 为空或 null。
     *
     * @see java.security.Provider
     */
    public static CertPathValidator getInstance(String algorithm,
            String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        Instance instance = GetInstance.getInstance("CertPathValidator",
            CertPathValidatorSpi.class, algorithm, provider);
        return new CertPathValidator((CertPathValidatorSpi)instance.impl,
            instance.provider, algorithm);
    }

                /**
     * 返回实现指定算法的 {@code CertPathValidator} 对象。
     *
     * <p> 返回一个新的 CertPathValidator 对象，封装了来自指定 Provider
     * 对象的 CertPathValidatorSpi 实现。注意，指定的 Provider 对象
     * 不必在提供者列表中注册。
     *
     * @param algorithm 请求的 {@code CertPathValidator} 算法的名称。参见 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathValidator">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * 了解标准算法名称的信息。
     *
     * @param provider 提供者。
     *
     * @return 实现指定算法的 {@code CertPathValidator} 对象。
     *
     * @exception NoSuchAlgorithmException 如果从指定的 Provider 对象中找不到
     *          指定算法的 CertPathValidatorSpi 实现。
     *
     * @exception IllegalArgumentException 如果 {@code provider} 为
     *          null。
     *
     * @see java.security.Provider
     */
    public static CertPathValidator getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("CertPathValidator",
            CertPathValidatorSpi.class, algorithm, provider);
        return new CertPathValidator((CertPathValidatorSpi)instance.impl,
            instance.provider, algorithm);
    }

    /**
     * 返回此 {@code CertPathValidator} 的 {@code Provider}。
     *
     * @return 此 {@code CertPathValidator} 的 {@code Provider}
     */
    public final Provider getProvider() {
        return this.provider;
    }

    /**
     * 返回此 {@code CertPathValidator} 的算法名称。
     *
     * @return 此 {@code CertPathValidator} 的算法名称
     */
    public final String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * 使用指定的算法参数集验证指定的认证路径。
     * <p>
     * 指定的 {@code CertPath} 必须是验证算法支持的类型，否则将抛出
     * {@code InvalidAlgorithmParameterException}。例如，实现 PKIX
     * 算法的 {@code CertPathValidator} 验证类型为 X.509 的 {@code CertPath} 对象。
     *
     * @param certPath 要验证的 {@code CertPath}
     * @param params 算法参数
     * @return 验证算法的结果
     * @exception CertPathValidatorException 如果 {@code CertPath}
     * 不通过验证
     * @exception InvalidAlgorithmParameterException 如果指定的
     * 参数或指定的 {@code CertPath} 类型不适用于此 {@code CertPathValidator}
     */
    public final CertPathValidatorResult validate(CertPath certPath,
        CertPathParameters params)
        throws CertPathValidatorException, InvalidAlgorithmParameterException
    {
        return validatorSpi.engineValidate(certPath, params);
    }

    /**
     * 返回由 {@code certpathvalidator.type} 安全属性指定的默认
     * {@code CertPathValidator} 类型，如果不存在这样的属性，则返回字符串
     * {@literal "PKIX"}。
     *
     * <p>默认的 {@code CertPathValidator} 类型可以被不想在调用
     * {@code getInstance} 方法时使用硬编码类型的程序使用，并且在用户未指定自己的类型时提供一个默认类型。
     *
     * <p>可以通过将 {@code certpathvalidator.type} 安全属性的值设置为所需的类型来更改默认的
     * {@code CertPathValidator} 类型。
     *
     * @see java.security.Security 安全属性
     * @return 由 {@code certpathvalidator.type} 安全属性指定的默认
     * {@code CertPathValidator} 类型，如果不存在这样的属性，则返回字符串
     * {@literal "PKIX"}。
     */
    public final static String getDefaultType() {
        String cpvtype =
            AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return Security.getProperty(CPV_TYPE);
                }
            });
        return (cpvtype == null) ? "PKIX" : cpvtype;
    }

    /**
     * 返回封装的 {@code CertPathValidatorSpi} 实现用于检查证书撤销状态的
     * {@code CertPathChecker}。PKIX 实现返回类型为 {@code PKIXRevocationChecker} 的对象。
     * 每次调用此方法都会返回一个新的 {@code CertPathChecker} 实例。
     *
     * <p>此方法的主要目的是允许调用者指定与撤销检查相关的额外输入参数和选项。
     * 请参见类描述中的示例。
     *
     * @return 一个 {@code CertPathChecker}
     * @throws UnsupportedOperationException 如果服务提供者不支持此方法
     * @since 1.8
     */
    public final CertPathChecker getRevocationChecker() {
        return validatorSpi.engineGetRevocationChecker();
    }
}
