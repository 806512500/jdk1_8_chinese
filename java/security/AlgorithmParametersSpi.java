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

import java.io.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

/**
 * 本类定义了 {@code AlgorithmParameters} 类的 <i>服务提供者接口</i> (<b>SPI</b>)，
 * 用于管理算法参数。
 *
 * <p> 本类中的所有抽象方法都必须由每个希望为特定算法提供参数管理的加密服务提供者实现。
 *
 * @author Jan Luehe
 *
 *
 * @see AlgorithmParameters
 * @see java.security.spec.AlgorithmParameterSpec
 * @see java.security.spec.DSAParameterSpec
 *
 * @since 1.2
 */

public abstract class AlgorithmParametersSpi {

    /**
     * 使用 {@code paramSpec} 中指定的参数初始化此参数对象。
     *
     * @param paramSpec 参数规范。
     *
     * @exception InvalidParameterSpecException 如果给定的参数规范不适合初始化此参数对象。
     */
    protected abstract void engineInit(AlgorithmParameterSpec paramSpec)
        throws InvalidParameterSpecException;

    /**
     * 导入指定的参数并根据参数的主要解码格式进行解码。
     * 参数的主要解码格式是 ASN.1，如果存在此类参数的 ASN.1 规范。
     *
     * @param params 编码的参数。
     *
     * @exception IOException 解码错误时抛出
     */
    protected abstract void engineInit(byte[] params)
        throws IOException;

    /**
     * 从 {@code params} 导入参数并根据指定的解码格式进行解码。
     * 如果 {@code format} 为 null，则使用参数的主要解码格式。主要解码格式是 ASN.1，如果存在此类参数的 ASN.1 规范。
     *
     * @param params 编码的参数。
     *
     * @param format 解码格式的名称。
     *
     * @exception IOException 解码错误时抛出
     */
    protected abstract void engineInit(byte[] params, String format)
        throws IOException;

    /**
     * 返回此参数对象的（透明）规范。
     * {@code paramSpec} 识别参数应返回的规范类。例如，可以是 {@code DSAParameterSpec.class}，表示参数应返回为 {@code DSAParameterSpec} 类的实例。
     *
     * @param <T> 要返回的参数规范的类型
     *
     * @param paramSpec 参数应返回的规范类。
     *
     * @return 参数规范。
     *
     * @exception InvalidParameterSpecException 如果请求的参数规范不适合此参数对象。
     */
    protected abstract
        <T extends AlgorithmParameterSpec>
        T engineGetParameterSpec(Class<T> paramSpec)
        throws InvalidParameterSpecException;

    /**
     * 以主要编码格式返回参数。
     * 参数的主要编码格式是 ASN.1，如果存在此类参数的 ASN.1 规范。
     *
     * @return 使用其主要编码格式编码的参数。
     *
     * @exception IOException 编码错误时抛出。
     */
    protected abstract byte[] engineGetEncoded() throws IOException;

    /**
     * 以指定的格式返回编码的参数。
     * 如果 {@code format} 为 null，则使用参数的主要编码格式。主要编码格式是 ASN.1，如果存在此类参数的 ASN.1 规范。
     *
     * @param format 编码格式的名称。
     *
     * @return 使用指定编码方案编码的参数。
     *
     * @exception IOException 编码错误时抛出。
     */
    protected abstract byte[] engineGetEncoded(String format)
        throws IOException;

    /**
     * 返回描述参数的格式化字符串。
     *
     * @return 描述参数的格式化字符串。
     */
    protected abstract String engineToString();
}
