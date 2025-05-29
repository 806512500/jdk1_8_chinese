/*
 * 版权所有 (c) 2010, Oracle 和/或其附属公司。保留所有权利。
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

import java.util.Set;

/**
 * 此接口指定加密算法、密钥（密钥大小）和其他算法参数的约束。
 * <p>
 * {@code AlgorithmConstraints} 对象是不可变的。此接口的实现不应提供可以改变实例状态的方法，一旦实例被创建。
 * <p>
 * 注意，{@code AlgorithmConstraints} 可以用于表示由安全属性
 * {@code jdk.certpath.disabledAlgorithms} 和
 * {@code jdk.tls.disabledAlgorithms} 描述的限制，或者可以由具体的
 * {@code PKIXCertPathChecker} 使用来检查认证路径中指定的证书是否包含所需的算法约束。
 *
 * @see javax.net.ssl.SSLParameters#getAlgorithmConstraints
 * @see javax.net.ssl.SSLParameters#setAlgorithmConstraints(AlgorithmConstraints)
 *
 * @since 1.7
 */

public interface AlgorithmConstraints {

    /**
     * 确定指定的加密原语是否授予算法权限。
     *
     * @param primitives 一组加密原语
     * @param algorithm 算法名称
     * @param parameters 算法参数，如果没有额外参数则为 null
     *
     * @return 如果算法被允许并且可以用于所有指定的加密原语，则返回 true
     *
     * @throws IllegalArgumentException 如果 primitives 或 algorithm 为 null 或空
     */
    public boolean permits(Set<CryptoPrimitive> primitives,
            String algorithm, AlgorithmParameters parameters);

    /**
     * 确定指定的加密原语是否授予密钥权限。
     * <p>
     * 通常用于检查密钥大小和密钥使用。
     *
     * @param primitives 一组加密原语
     * @param key 密钥
     *
     * @return 如果密钥可以用于所有指定的加密原语，则返回 true
     *
     * @throws IllegalArgumentException 如果 primitives 为 null 或空，或者密钥为 null
     */
    public boolean permits(Set<CryptoPrimitive> primitives, Key key);

    /**
     * 确定算法及其对应的密钥是否授予指定的加密原语权限。
     *
     * @param primitives 一组加密原语
     * @param algorithm 算法名称
     * @param key 密钥
     * @param parameters 算法参数，如果没有额外参数则为 null
     *
     * @return 如果密钥和算法可以用于所有指定的加密原语，则返回 true
     *
     * @throws IllegalArgumentException 如果 primitives 或 algorithm 为 null 或空，或者密钥为 null
     */
    public boolean permits(Set<CryptoPrimitive> primitives,
                String algorithm, Key key, AlgorithmParameters parameters);

}
