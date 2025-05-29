
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
import java.util.*;
import java.io.*;

import java.nio.ByteBuffer;

import sun.security.jca.JCAUtil;

/**
 * 该类定义了 {@code Signature} 类的 <i>服务提供者接口</i> (<b>SPI</b>)，
 * 用于提供数字签名算法的功能。数字签名用于数字数据的身份验证和完整性保证。
 *.
 * <p> 本类中的所有抽象方法都必须由希望提供特定签名算法实现的每个加密服务提供商实现。
 *
 * @author Benjamin Renaud
 *
 *
 * @see Signature
 */

public abstract class SignatureSpi {

    /**
     * 应用程序指定的随机性来源。
     */
    protected SecureRandom appRandom = null;

    /**
     * 使用指定的公钥初始化此签名对象，用于验证操作。
     *
     * @param publicKey 要验证其签名的身份的公钥。
     *
     * @exception InvalidKeyException 如果密钥编码不正确，缺少参数等。
     */
    protected abstract void engineInitVerify(PublicKey publicKey)
        throws InvalidKeyException;

    /**
     * 使用指定的公钥初始化此签名对象，用于验证操作。
     *
     * @param publicKey 要验证其签名的身份的公钥。
     * @param params 生成此签名的参数。
     *
     * @exception InvalidKeyException 如果密钥编码不正确，与给定参数不匹配等。
     * @exception InvalidAlgorithmParameterException 如果给定的参数无效。
     */
    void engineInitVerify(PublicKey publicKey,
            AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            try {
                engineSetParameter(params);
            } catch (UnsupportedOperationException usoe) {
                // 如果未重写，则出错
                throw new InvalidAlgorithmParameterException(usoe);
            }
        }
        engineInitVerify(publicKey);
    }

    /**
     * 使用指定的私钥初始化此签名对象，用于签名操作。
     *
     * @param privateKey 要生成其签名的身份的私钥。
     *
     * @exception InvalidKeyException 如果密钥编码不正确，缺少参数等。
     */
    protected abstract void engineInitSign(PrivateKey privateKey)
        throws InvalidKeyException;

    /**
     * 使用指定的私钥和随机性来源初始化此签名对象，用于签名操作。
     *
     * <p>此具体方法已添加到先前定义的抽象类中。（为了向后兼容，它不能是抽象的。）
     *
     * @param privateKey 要生成其签名的身份的私钥。
     * @param random 随机性来源。
     *
     * @exception InvalidKeyException 如果密钥编码不正确，缺少参数等。
     */
    protected void engineInitSign(PrivateKey privateKey,
            SecureRandom random)
            throws InvalidKeyException {
        this.appRandom = random;
        engineInitSign(privateKey);
    }

    /**
     * 使用指定的私钥和随机性来源初始化此签名对象，用于签名操作。
     *
     * <p>此具体方法已添加到先前定义的抽象类中。（为了向后兼容，它不能是抽象的。）
     *
     * @param privateKey 要生成其签名的身份的私钥。
     * @param params 生成此签名的参数。
     * @param random 随机性来源。
     *
     * @exception InvalidKeyException 如果密钥编码不正确，缺少参数等。
     * @exception InvalidAlgorithmParameterException 如果参数无效。
     */
    void engineInitSign(PrivateKey privateKey,
            AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            try {
                engineSetParameter(params);
            } catch (UnsupportedOperationException usoe) {
                // 如果未重写，则出错
                throw new InvalidAlgorithmParameterException(usoe);
            }
        }
        engineInitSign(privateKey, random);
    }

    /**
     * 使用指定的字节更新要签名或验证的数据。
     *
     * @param b 用于更新的字节。
     *
     * @exception SignatureException 如果引擎未正确初始化。
     */
    protected abstract void engineUpdate(byte b) throws SignatureException;

    /**
     * 使用指定的字节数组，从指定的偏移量开始，更新要签名或验证的数据。
     *
     * @param b 字节数组。
     * @param off 在字节数组中开始的偏移量。
     * @param len 从偏移量开始使用的字节数。
     *
     * @exception SignatureException 如果引擎未正确初始化。
     */
    protected abstract void engineUpdate(byte[] b, int off, int len)
            throws SignatureException;

    /**
     * 使用指定的 ByteBuffer 更新要签名或验证的数据。处理 {@code data.remaining()} 字节，
     * 从 {@code data.position()} 开始。返回时，缓冲区的位置将等于其限制；
     * 其限制不会改变。
     *
     * @param input ByteBuffer。
     * @since 1.5
     */
    protected void engineUpdate(ByteBuffer input) {
        if (input.hasRemaining() == false) {
            return;
        }
        try {
            if (input.hasArray()) {
                byte[] b = input.array();
                int ofs = input.arrayOffset();
                int pos = input.position();
                int lim = input.limit();
                engineUpdate(b, ofs + pos, lim - pos);
                input.position(lim);
            } else {
                int len = input.remaining();
                byte[] b = new byte[JCAUtil.getTempArraySize(len)];
                while (len > 0) {
                    int chunk = Math.min(len, b.length);
                    input.get(b, 0, chunk);
                    engineUpdate(b, 0, chunk);
                    len -= chunk;
                }
            }
        } catch (SignatureException e) {
            // 仅在引擎未初始化时发生
            // 该情况应永远不会发生，因为它在 Signature.java 中被捕获
            throw new ProviderException("update() failed", e);
        }
    }

                /**
     * 返回到目前为止更新的所有数据的签名字节。
     * 签名的格式取决于底层的签名方案。
     *
     * @return 签名操作结果的签名字节。
     *
     * @exception SignatureException 如果引擎未正确初始化，或者此签名算法无法处理提供的输入数据。
     */
    protected abstract byte[] engineSign() throws SignatureException;

    /**
     * 完成此签名操作，并将生成的签名字节存储在提供的缓冲区 {@code outbuf} 中，从
     * {@code offset} 开始。
     * 签名的格式取决于底层的签名方案。
     *
     * <p>签名实现将重置为其初始状态
     * （调用 {@code engineInitSign} 方法后的状态）
     * 并可以使用相同的私钥重新生成进一步的签名。
     *
     * 此方法应该是抽象的，但我们为了二进制兼容性而保留为具体实现。有知识的提供者应覆盖此方法。
     *
     * @param outbuf 签名结果的缓冲区。
     *
     * @param offset 签名在 {@code outbuf} 中存储的起始位置。
     *
     * @param len 在 {@code outbuf} 中为签名分配的字节数。
     * 无论是此默认实现还是 SUN 提供者都不会返回部分摘要。如果此参数的值小于实际签名长度，此方法将抛出
     * SignatureException。如果此参数的值大于或等于实际签名长度，则忽略此参数。
     *
     * @return 放入 {@code outbuf} 中的字节数。
     *
     * @exception SignatureException 如果引擎未正确初始化，此签名算法无法处理提供的输入数据，或者 {@code len} 小于
     * 实际签名长度。
     *
     * @since 1.2
     */
    protected int engineSign(byte[] outbuf, int offset, int len)
             throws SignatureException {
        byte[] sig = engineSign();
        if (len < sig.length) {
                throw new SignatureException
                    ("partial signatures not returned");
        }
        if (outbuf.length - offset < sig.length) {
                throw new SignatureException
                    ("insufficient space in the output buffer to store the "
                     + "signature");
        }
        System.arraycopy(sig, 0, outbuf, offset, sig.length);
        return sig.length;
    }

    /**
     * 验证传入的签名。
     *
     * @param sigBytes 要验证的签名字节。
     *
     * @return 如果签名被验证，则返回 true，否则返回 false。
     *
     * @exception SignatureException 如果引擎未正确初始化，传入的签名编码不正确或类型错误，此签名算法无法处理提供的输入数据等。
     */
    protected abstract boolean engineVerify(byte[] sigBytes)
            throws SignatureException;

    /**
     * 验证传入的签名，从指定的字节数组的指定偏移量开始。
     *
     * <p>注意：子类应覆盖默认实现。
     *
     *
     * @param sigBytes 要验证的签名字节。
     * @param offset 在字节数组中开始的偏移量。
     * @param length 从偏移量开始使用的字节数。
     *
     * @return 如果签名被验证，则返回 true，否则返回 false。
     *
     * @exception SignatureException 如果引擎未正确初始化，传入的签名编码不正确或类型错误，此签名算法无法处理提供的输入数据等。
     * @since 1.4
     */
    protected boolean engineVerify(byte[] sigBytes, int offset, int length)
            throws SignatureException {
        byte[] sigBytesCopy = new byte[length];
        System.arraycopy(sigBytes, offset, sigBytesCopy, 0, length);
        return engineVerify(sigBytesCopy);
    }

    /**
     * 将指定的算法参数设置为指定的值。此方法提供了一种通用机制，通过它可以设置此对象的各种参数。
     * 参数可以是算法的任何可设置参数，例如参数大小，或签名生成的随机位源（如果适用），或指示是否执行特定但可选的计算。每个参数的统一算法特定命名方案是可取的，但目前未指定。
     *
     * @param param 参数的字符串标识符。
     *
     * @param value 参数值。
     *
     * @exception InvalidParameterException 如果 {@code param} 是此签名算法引擎的无效参数，
     * 参数已设置且不能再次设置，发生安全异常等。
     *
     * @deprecated 被 {@link
     * #engineSetParameter(java.security.spec.AlgorithmParameterSpec)
     * engineSetParameter} 替代。
     */
    @Deprecated
    protected abstract void engineSetParameter(String param, Object value)
            throws InvalidParameterException;

    /**
     * <p>此方法由提供者覆盖，以使用指定的参数集初始化此签名引擎。
     *
     * @param params 参数
     *
     * @exception UnsupportedOperationException 如果此方法未被提供者覆盖
     *
     * @exception InvalidAlgorithmParameterException 如果此方法被提供者覆盖且给定的参数
     * 对此签名引擎不适当
     */
    protected void engineSetParameter(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
        throw new UnsupportedOperationException();
    }

                /**
     * <p>此方法由提供者重写，以返回与此签名引擎一起使用的参数。
     *
     * <p>如果此签名引擎之前已通过参数初始化（通过调用 {@code engineSetParameter} 方法），则此方法返回相同的参数。如果此签名引擎尚未通过参数初始化，则如果底层签名实现支持并能成功生成，此方法可能返回默认值和随机生成的参数值的组合。否则，返回 {@code null}。
     *
     * @return 与此签名引擎一起使用的参数，或 {@code null}
     *
     * @exception UnsupportedOperationException 如果此方法未被提供者重写
     * @since 1.4
     */
    protected AlgorithmParameters engineGetParameters() {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取指定算法参数的值。
     * 此方法提供了一种通用机制，通过它可以获取此对象的各种参数。参数可以是算法的任何可设置参数，例如参数大小，或签名生成的随机位源（如果适用），或是否执行特定但可选的计算的指示。每个参数的统一算法特定命名方案是可取的，但目前未指定。
     *
     * @param param 参数的字符串名称。
     *
     * @return 代表参数值的对象，如果没有则返回 {@code null}。
     *
     * @exception InvalidParameterException 如果 {@code param} 是此引擎的无效参数，或在尝试获取此参数时发生其他异常。
     *
     * @deprecated
     */
    @Deprecated
    protected abstract Object engineGetParameter(String param)
        throws InvalidParameterException;

    /**
     * 如果实现支持克隆，则返回一个克隆。
     *
     * @return 如果实现支持克隆，则返回一个克隆。
     *
     * @exception CloneNotSupportedException 如果在不支持 {@code Cloneable} 的实现上调用此方法时抛出。
     */
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        } else {
            throw new CloneNotSupportedException();
        }
    }
}
