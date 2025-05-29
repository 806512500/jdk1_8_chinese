
/*
 * 版权所有 (c) 1997, 2020, Oracle 和/或其附属公司。保留所有权利。
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

import java.security.spec.AlgorithmParameterSpec;

/**
 * {@code AlgorithmParameterGenerator} 类用于生成将与特定算法一起使用的一组参数。参数生成器是使用 {@code getInstance} 工厂方法（返回给定类实例的静态方法）构造的。
 *
 * <P>生成参数的对象可以通过两种不同的方式初始化：与算法无关的方式，或特定于算法的方式：
 *
 * <ul>
 * <li>与算法无关的方法利用所有参数生成器共享“大小”和随机源的概念。大小的度量被所有算法参数普遍共享，尽管它对不同算法的解释不同。例如，在 DSA 算法的参数情况下，“大小”对应于质数模的大小（以位为单位）。
 * 当使用这种方法时，算法特定的参数生成值（如果有）默认为某些标准值，除非它们可以从指定的大小派生。
 *
 * <li>另一种方法使用特定于算法的语义初始化参数生成器对象，这些语义由一组特定于算法的参数生成值表示。例如，为了生成 Diffie-Hellman 系统参数，参数生成值通常
 * 包括质数模的大小和随机指数的大小，均以位数指定。
 * </ul>
 *
 * <P>如果客户端没有显式初始化 AlgorithmParameterGenerator
 * （通过调用 {@code init} 方法），每个提供者必须提供（并记录）默认初始化。例如，Sun 提供者使用 1024 位的默认质数模大小来生成 DSA 参数。
 *
 * <p> Java 平台的每个实现都必须支持以下标准 {@code AlgorithmParameterGenerator} 算法和密钥大小（括号内）：
 * <ul>
 * <li>{@code DiffieHellman} (1024)</li>
 * <li>{@code DSA} (1024)</li>
 * </ul>
 * 这些算法在 <a href=
 * "{@docRoot}/../technotes/guides/security/StandardNames.html#AlgorithmParameterGenerator">
 * Java 密码架构标准算法名称文档的 AlgorithmParameterGenerator 部分</a>中描述。查阅您的实现的发行文档，以查看是否支持其他算法。
 *
 * @author Jan Luehe
 *
 *
 * @see AlgorithmParameters
 * @see java.security.spec.AlgorithmParameterSpec
 *
 * @since 1.2
 */

public class AlgorithmParameterGenerator {

    // 提供者
    private Provider provider;

    // 提供者实现（委托）
    private AlgorithmParameterGeneratorSpi paramGenSpi;

    // 算法
    private String algorithm;

    /**
     * 创建 AlgorithmParameterGenerator 对象。
     *
     * @param paramGenSpi 委托
     * @param provider 提供者
     * @param algorithm 算法
     */
    protected AlgorithmParameterGenerator
    (AlgorithmParameterGeneratorSpi paramGenSpi, Provider provider,
     String algorithm) {
        this.paramGenSpi = paramGenSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    /**
     * 返回与此参数生成器关联的算法的标准名称。
     *
     * @return 算法的字符串名称。
     */
    public final String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * 返回一个 AlgorithmParameterGenerator 对象，用于生成将与指定算法一起使用的一组参数。
     *
     * <p>此方法遍历注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个新的 AlgorithmParameterGenerator 对象，封装了支持指定算法的第一个提供者的 AlgorithmParameterGeneratorSpi 实现。
     *
     * <p>注意，可以通过 {@link Security#getProviders() Security.getProviders()} 方法检索注册的提供者列表。
     *
     * @param algorithm 与此参数生成器关联的算法名称。
     * 请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#AlgorithmParameterGenerator">
     * Java 密码架构标准算法名称文档的 AlgorithmParameterGenerator 部分</a>，了解标准算法名称的信息。
     *
     * @return 新的 AlgorithmParameterGenerator 对象。
     *
     * @exception NoSuchAlgorithmException 如果没有提供者支持指定算法的 AlgorithmParameterGeneratorSpi 实现。
     *
     * @see Provider
     */
    public static AlgorithmParameterGenerator getInstance(String algorithm)
        throws NoSuchAlgorithmException {
            try {
                Object[] objs = Security.getImpl(algorithm,
                                                 "AlgorithmParameterGenerator",
                                                 (String)null);
                return new AlgorithmParameterGenerator
                    ((AlgorithmParameterGeneratorSpi)objs[0],
                     (Provider)objs[1],
                     algorithm);
            } catch(NoSuchProviderException e) {
                throw new NoSuchAlgorithmException(algorithm + " not found");
            }
    }

    /**
     * 返回一个 AlgorithmParameterGenerator 对象，用于生成将与指定算法一起使用的一组参数。
     *
     * <p>返回一个新的 AlgorithmParameterGenerator 对象，封装了指定提供者的 AlgorithmParameterGeneratorSpi 实现。指定的提供者必须注册在安全提供者列表中。
     *
     * <p>注意，可以通过 {@link Security#getProviders() Security.getProviders()} 方法检索注册的提供者列表。
     *
     * @param algorithm 与此参数生成器关联的算法名称。
     * 请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#AlgorithmParameterGenerator">
     * Java 密码架构标准算法名称文档的 AlgorithmParameterGenerator 部分</a>，了解标准算法名称的信息。
     *
     * @param provider 提供者的字符串名称。
     *
     * @return 新的 AlgorithmParameterGenerator 对象。
     *
     * @exception NoSuchAlgorithmException 如果指定提供者不提供指定算法的 AlgorithmParameterGeneratorSpi 实现。
     *
     * @exception NoSuchProviderException 如果指定的提供者未注册在安全提供者列表中。
     *
     * @exception IllegalArgumentException 如果提供者名称为空或为空字符串。
     *
     * @see Provider
     */
    public static AlgorithmParameterGenerator getInstance(String algorithm,
                                                          String provider)
        throws NoSuchAlgorithmException, NoSuchProviderException
    {
        if (provider == null || provider.isEmpty())
            throw new IllegalArgumentException("missing provider");
        Object[] objs = Security.getImpl(algorithm,
                                         "AlgorithmParameterGenerator",
                                         provider);
        return new AlgorithmParameterGenerator
            ((AlgorithmParameterGeneratorSpi)objs[0], (Provider)objs[1],
             algorithm);
    }

                /**
     * 返回一个 AlgorithmParameterGenerator 对象，用于生成
     * 与指定算法一起使用的一组参数。
     *
     * <p> 返回一个新的 AlgorithmParameterGenerator 对象，封装了
     * 指定 Provider 对象的 AlgorithmParameterGeneratorSpi 实现。注意，指定的 Provider 对象
     * 不需要在提供者列表中注册。
     *
     * @param algorithm 与此参数生成器关联的算法的字符串名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#AlgorithmParameterGenerator">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * 中的 AlgorithmParameterGenerator 部分。
     *
     * @param provider Provider 对象。
     *
     * @return 新的 AlgorithmParameterGenerator 对象。
     *
     * @exception NoSuchAlgorithmException 如果指定 Provider 对象中没有
     *          指定算法的 AlgorithmParameterGeneratorSpi 实现。
     *
     * @exception IllegalArgumentException 如果指定的提供者为 null。
     *
     * @see Provider
     *
     * @since 1.4
     */
    public static AlgorithmParameterGenerator getInstance(String algorithm,
                                                          Provider provider)
        throws NoSuchAlgorithmException
    {
        if (provider == null)
            throw new IllegalArgumentException("missing provider");
        Object[] objs = Security.getImpl(algorithm,
                                         "AlgorithmParameterGenerator",
                                         provider);
        return new AlgorithmParameterGenerator
            ((AlgorithmParameterGeneratorSpi)objs[0], (Provider)objs[1],
             algorithm);
    }

    /**
     * 返回此算法参数生成器对象的提供者。
     *
     * @return 此算法参数生成器对象的提供者
     */
    public final Provider getProvider() {
        return this.provider;
    }

    /**
     * 为此参数生成器初始化特定大小。
     * 为了创建参数，使用最高优先级安装的提供者提供的 {@code SecureRandom}
     * 实现作为随机性的来源。
     * （如果安装的提供者中没有提供 {@code SecureRandom} 的实现，则使用系统提供的随机性来源。）
     *
     * @param size 大小（位数）。
     */
    public final void init(int size) {
        paramGenSpi.engineInit(size, new SecureRandom());
    }

    /**
     * 为此参数生成器初始化特定大小和随机性来源。
     *
     * @param size 大小（位数）。
     * @param random 随机性来源。
     */
    public final void init(int size, SecureRandom random) {
        paramGenSpi.engineInit(size, random);
    }

    /**
     * 使用一组算法特定的参数生成值初始化此参数生成器。
     * 为了生成参数，使用最高优先级安装的提供者提供的 {@code SecureRandom}
     * 实现作为随机性的来源。
     * （如果安装的提供者中没有提供 {@code SecureRandom} 的实现，则使用系统提供的随机性来源。）
     *
     * @param genParamSpec 算法特定的参数生成值集。
     *
     * @exception InvalidAlgorithmParameterException 如果给定的参数生成值
     * 不适合此参数生成器。
     */
    public final void init(AlgorithmParameterSpec genParamSpec)
        throws InvalidAlgorithmParameterException {
            paramGenSpi.engineInit(genParamSpec, new SecureRandom());
    }

    /**
     * 使用一组算法特定的参数生成值初始化此参数生成器。
     *
     * @param genParamSpec 算法特定的参数生成值集。
     * @param random 随机性来源。
     *
     * @exception InvalidAlgorithmParameterException 如果给定的参数生成值
     * 不适合此参数生成器。
     */
    public final void init(AlgorithmParameterSpec genParamSpec,
                           SecureRandom random)
        throws InvalidAlgorithmParameterException {
            paramGenSpi.engineInit(genParamSpec, random);
    }

    /**
     * 生成参数。
     *
     * @return 新的 AlgorithmParameters 对象。
     */
    public final AlgorithmParameters generateParameters() {
        return paramGenSpi.engineGenerateParameters();
    }
}
