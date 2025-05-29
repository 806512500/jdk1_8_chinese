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

import java.security.InvalidAlgorithmParameterException;

/**
 *
 * {@link CertPathValidator CertPathValidator} 类的 <i>服务提供者接口</i> (<b>SPI</b>)。
 * 所有 {@code CertPathValidator} 实现都必须包含一个类（SPI 类），该类扩展此类（{@code CertPathValidatorSpi}）
 * 并实现其所有方法。通常，此类的实例应仅通过 {@code CertPathValidator} 类访问。
 * 有关详细信息，请参阅 Java 密码架构。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 无需保护此类的实例以防止多个线程的并发访问。需要并发访问单个
 * {@code CertPathValidatorSpi} 实例的线程应同步并提供必要的锁定，然后调用
 * 包装的 {@code CertPathValidator} 对象。
 * <p>
 * 然而，{@code CertPathValidatorSpi} 的实现仍可能遇到并发问题，因为多个线程各自
 * 操纵不同的 {@code CertPathValidatorSpi} 实例无需同步。
 *
 * @since       1.4
 * @author      Yassir Elley
 */
public abstract class CertPathValidatorSpi {

    /**
     * 默认构造函数。
     */
    public CertPathValidatorSpi() {}

    /**
     * 使用指定的算法参数集验证指定的证书路径。
     * <p>
     * 指定的 {@code CertPath} 必须是验证算法支持的类型，否则将抛出
     * {@code InvalidAlgorithmParameterException}。例如，实现 PKIX
     * 算法的 {@code CertPathValidator} 验证类型为 X.509 的 {@code CertPath} 对象。
     *
     * @param certPath 要验证的 {@code CertPath}
     * @param params 算法参数
     * @return 验证算法的结果
     * @exception CertPathValidatorException 如果 {@code CertPath} 无法验证
     * @exception InvalidAlgorithmParameterException 如果指定的参数或指定的
     * {@code CertPath} 类型不适用于此 {@code CertPathValidator}
     */
    public abstract CertPathValidatorResult
        engineValidate(CertPath certPath, CertPathParameters params)
        throws CertPathValidatorException, InvalidAlgorithmParameterException;

    /**
     * 返回此实现用于检查证书撤销状态的 {@code CertPathChecker}。PKIX 实现返回
     * 类型为 {@code PKIXRevocationChecker} 的对象。
     *
     * <p>此方法的主要目的是允许调用者指定特定于撤销检查的附加输入参数和选项。
     * 有关示例，请参阅 {@code CertPathValidator} 的类描述。
     *
     * <p>此方法是在 Java 平台标准版 1.8 版本中添加的。为了与现有的服务提供者保持向后兼容，
     * 此方法不能是抽象的，默认情况下抛出 {@code UnsupportedOperationException}。
     *
     * @return 此实现用于检查证书撤销状态的 {@code CertPathChecker}
     * @throws UnsupportedOperationException 如果此方法不受支持
     * @since 1.8
     */
    public CertPathChecker engineGetRevocationChecker() {
        throw new UnsupportedOperationException();
    }
}
