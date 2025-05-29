/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * 该类定义了 {@code AlgorithmParameterGenerator} 类的 <i>服务提供者接口</i> (<b>SPI</b>)，
 * 用于生成将与特定算法一起使用的一组参数。
 *
 * <p> 本类中的所有抽象方法都必须由每个希望为特定算法提供参数生成器实现的加密服务提供商实现。
 *
 * <p> 如果客户端未显式初始化 AlgorithmParameterGenerator（通过调用 {@code engineInit} 方法），
 * 每个提供商必须提供（并记录）默认初始化。例如，Sun 提供商使用 1024 位的默认模数素数大小来生成 DSA 参数。
 *
 * @author Jan Luehe
 *
 *
 * @see AlgorithmParameterGenerator
 * @see AlgorithmParameters
 * @see java.security.spec.AlgorithmParameterSpec
 *
 * @since 1.2
 */

public abstract class AlgorithmParameterGeneratorSpi {

    /**
     * 用指定的大小和随机源初始化此参数生成器。
     *
     * @param size 大小（位数）。
     * @param random 随机源。
     */
    protected abstract void engineInit(int size, SecureRandom random);

    /**
     * 用一组算法特定的参数生成值初始化此参数生成器。
     *
     * @param genParamSpec 算法特定的参数生成值集。
     * @param random 随机源。
     *
     * @exception InvalidAlgorithmParameterException 如果给定的参数生成值不适合此参数生成器。
     */
    protected abstract void engineInit(AlgorithmParameterSpec genParamSpec,
                                       SecureRandom random)
        throws InvalidAlgorithmParameterException;

    /**
     * 生成参数。
     *
     * @return 新的 AlgorithmParameters 对象。
     */
    protected abstract AlgorithmParameters engineGenerateParameters();
}
