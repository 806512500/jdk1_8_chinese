
/*
 * Copyright (c) 1997, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.security;

import java.security.spec.AlgorithmParameterSpec;
import java.util.*;
import java.io.*;

import java.nio.ByteBuffer;

import sun.security.jca.JCAUtil;

/**
 * 本类定义了 {@code Signature} 类的 <i>服务提供者接口</i> (<b>SPI</b>)，
 * 用于提供数字签名算法的功能。数字签名用于数字数据的身份验证和完整性保证。
 * <p> 本类中的所有抽象方法都必须由每个希望提供特定签名算法实现的加密服务提供商实现。
 *
 * @author Benjamin Renaud
 *
 *
 * @see Signature
 */

public abstract class SignatureSpi {

    /**
     * 应用程序指定的随机源。
     */
    protected SecureRandom appRandom = null;

    /**
     * 使用指定的公钥初始化此签名对象，用于验证操作。
     *
     * @param publicKey 要验证其签名的身份的公钥。
     *
     * @exception InvalidKeyException 如果密钥编码不正确，参数缺失等。
     */
    protected abstract void engineInitVerify(PublicKey publicKey)
        throws InvalidKeyException;

    /**
     * 使用指定的公钥初始化此签名对象，用于验证操作。
     *
     * @param publicKey 要验证其签名的身份的公钥。
     * @param params 生成此签名的参数。
     *
     * @exception InvalidKeyException 如果密钥编码不正确，不适用于给定的参数等。
     * @exception InvalidAlgorithmParameterException 如果给定的参数无效。
     */
    void engineInitVerify(PublicKey publicKey,
            AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            try {
                engineSetParameter(params);
            } catch (UnsupportedOperationException usoe) {
                // 如果未重写，则抛出错误
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
     * @exception InvalidKeyException 如果密钥编码不正确，参数缺失等。
     */
    protected abstract void engineInitSign(PrivateKey privateKey)
        throws InvalidKeyException;

    /**
     * 使用指定的私钥和随机源初始化此签名对象，用于签名操作。
     *
     * <p>此具体方法已添加到先前定义的抽象类中。（为了向后兼容，它不能是抽象的。）
     *
     * @param privateKey 要生成其签名的身份的私钥。
     * @param random 随机源。
     *
     * @exception InvalidKeyException 如果密钥编码不正确，参数缺失等。
     */
    protected void engineInitSign(PrivateKey privateKey,
            SecureRandom random)
            throws InvalidKeyException {
        this.appRandom = random;
        engineInitSign(privateKey);
    }

    /**
     * 使用指定的私钥和随机源初始化此签名对象，用于签名操作。
     *
     * <p>此具体方法已添加到先前定义的抽象类中。（为了向后兼容，它不能是抽象的。）
     *
     * @param privateKey 要生成其签名的身份的私钥。
     * @param params 生成此签名的参数。
     * @param random 随机源。
     *
     * @exception InvalidKeyException 如果密钥编码不正确，参数缺失等。
     * @exception InvalidAlgorithmParameterException 如果参数无效。
     */
    void engineInitSign(PrivateKey privateKey,
            AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            try {
                engineSetParameter(params);
            } catch (UnsupportedOperationException usoe) {
                // 如果未重写，则抛出错误
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
     * 使用指定的 ByteBuffer 更新要签名或验证的数据。处理 {@code data.remaining()} 个字节，
     * 从 {@code data.position()} 开始。返回时，缓冲区的位置将等于其限制；其限制不会改变。
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
            // 仅在引擎未初始化时指定发生
            // 该情况应永远不会发生，因为它在 Signature.java 中被捕获
            throw new ProviderException("update() failed", e);
        }
    }

    /**
     * 返回到目前为止更新的所有数据的签名字节。签名的格式取决于底层签名方案。
     *
     * @return 签名操作结果的签名字节。
     *
     * @exception SignatureException 如果引擎未正确初始化，或者此签名算法无法处理提供的输入数据。
     */
    protected abstract byte[] engineSign() throws SignatureException;

    /**
     * 完成此签名操作，并将生成的签名字节存储在提供的缓冲区 {@code outbuf} 中，从 {@code offset} 开始。
     * 签名的格式取决于底层签名方案。
     *
     * <p>签名实现将重置为其初始状态（调用 {@code engineInitSign} 方法后的状态），
     * 并可以使用相同的私钥重新生成进一步的签名。
     *
     * 此方法应该是抽象的，但为了二进制兼容性，我们将其保留为具体方法。有知识的提供者应重写此方法。
     *
     * @param outbuf 用于存储签名结果的缓冲区。
     *
     * @param offset 在 {@code outbuf} 中存储签名的偏移量。
     *
     * @param len 在 {@code outbuf} 中为签名分配的字节数。
     * 无论是此默认实现还是 SUN 提供者都不会返回部分摘要。如果此参数的值小于实际签名长度，
     * 此方法将抛出 SignatureException。如果此参数的值大于或等于实际签名长度，则忽略此参数。
     *
     * @return 放入 {@code outbuf} 中的字节数。
     *
     * @exception SignatureException 如果引擎未正确初始化，此签名算法无法处理提供的输入数据，
     * 或者 {@code len} 小于实际签名长度。
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
     * @return 如果签名验证成功，返回 true；否则返回 false。
     *
     * @exception SignatureException 如果引擎未正确初始化，传入的签名编码不正确或类型错误，
     * 或者此签名算法无法处理提供的输入数据等。
     */
    protected abstract boolean engineVerify(byte[] sigBytes)
            throws SignatureException;

    /**
     * 验证传入的签名，从指定的字节数组的指定偏移量开始。
     *
     * <p> 注意：子类应覆盖默认实现。
     *
     *
     * @param sigBytes 要验证的签名字节。
     * @param offset 在字节数组中开始的偏移量。
     * @param length 从偏移量开始使用的字节数。
     *
     * @return 如果签名验证成功，返回 true；否则返回 false。
     *
     * @exception SignatureException 如果引擎未正确初始化，传入的签名编码不正确或类型错误，
     * 或者此签名算法无法处理提供的输入数据等。
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
     * 参数可以是算法的任何可设置参数，例如参数大小，或用于签名生成的随机位源（如果适用），
     * 或指示是否执行特定但可选的计算。每个参数的统一算法特定命名方案是可取的，但目前未指定。
     *
     * @param param 参数的字符串标识符。
     *
     * @param value 参数值。
     *
     * @exception InvalidParameterException 如果 {@code param} 是此签名算法引擎的无效参数，
     * 参数已设置且不能再次设置，发生安全异常等。
     *
     * @deprecated 替换为 {@link
     * #engineSetParameter(java.security.spec.AlgorithmParameterSpec)
     * engineSetParameter}。
     */
    @Deprecated
    protected abstract void engineSetParameter(String param, Object value)
            throws InvalidParameterException;

    /**
     * <p>此方法由提供者重写，以使用指定的参数集初始化此签名引擎。
     *
     * @param params 参数。
     *
     * @exception UnsupportedOperationException 如果此方法未被提供者重写。
     *
     * @exception InvalidAlgorithmParameterException 如果此方法被提供者重写且给定的参数
     * 不适合此签名引擎。
     */
    protected void engineSetParameter(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>此方法由提供者重写，以返回与此签名引擎一起使用的参数。
     *
     * <p> 如果此签名引擎已通过调用 {@code engineSetParameter} 方法初始化了参数，此方法返回相同的参数。
     * 如果此签名引擎未初始化参数，此方法可能返回默认和随机生成的参数值的组合（如果底层签名实现支持并能成功生成它们）。
     * 否则，返回 {@code null}。
     *
     * @return 与此签名引擎一起使用的参数，或 {@code null}。
     *
     * @exception UnsupportedOperationException 如果此方法未被提供者重写。
     * @since 1.4
     */
    protected AlgorithmParameters engineGetParameters() {
        throw new UnsupportedOperationException();
    }


                /**
     * 获取指定算法参数的值。
     * 此方法提供了一种通用机制，通过它可以获取此对象的各种参数。参数
     * 可能是算法的任何可设置参数，例如参数
     * 大小，或用于签名生成的随机位源（如果适用），或是否执行
     * 特定但可选的计算的指示。每个参数的统一算法特定
     * 命名方案是可取的，但目前未指定。
     *
     * @param param 参数的字符串名称。
     *
     * @return 代表参数值的对象，如果没有则返回 {@code null}。
     *
     * @exception InvalidParameterException 如果 {@code param} 是
     * 此引擎的无效参数，或者在尝试获取此参数时发生其他异常。
     *
     * @deprecated
     */
    @Deprecated
    protected abstract Object engineGetParameter(String param)
        throws InvalidParameterException;

    /**
     * 如果实现可克隆，则返回一个克隆。
     *
     * @return 如果实现可克隆，则返回一个克隆。
     *
     * @exception CloneNotSupportedException 如果此调用
     * 在不支持 {@code Cloneable} 的实现上调用时抛出。
     */
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        } else {
            throw new CloneNotSupportedException();
        }
    }
}
