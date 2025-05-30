/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.ByteBuffer;

import sun.security.jca.JCAUtil;

/**
 * 该类定义了 {@code MessageDigest} 类的 <i>服务提供程序接口</i> (<b>SPI</b>)，
 * 提供了消息摘要算法（如 MD5 或 SHA）的功能。消息摘要是安全的单向哈希函数，可以将任意大小的数据转换为固定长度的哈希值。
 *
 * <p> 所有该类中的抽象方法都必须由希望提供特定消息摘要算法实现的加密服务提供程序实现。
 *
 * <p> 实现可以自由选择实现 Cloneable 接口。
 *
 * @author Benjamin Renaud
 *
 *
 * @see MessageDigest
 */

public abstract class MessageDigestSpi {

    // 用于 engineUpdate(ByteBuffer input) 中的重用
    private byte[] tempArray;

    /**
     * 返回摘要长度（以字节为单位）。
     *
     * <p>此具体方法已添加到先前定义的抽象类中。（为了向后兼容，它不能是抽象的。）
     *
     * <p>默认行为是返回 0。
     *
     * <p>提供程序可以重写此方法以返回摘要长度。
     *
     * @return 摘要长度（以字节为单位）。
     *
     * @since 1.2
     */
    protected int engineGetDigestLength() {
        return 0;
    }

    /**
     * 使用指定的字节更新摘要。
     *
     * @param input 用于更新的字节。
     */
    protected abstract void engineUpdate(byte input);

    /**
     * 使用指定的字节数组更新摘要，从指定的偏移量开始。
     *
     * @param input 用于更新的字节数组。
     *
     * @param offset 从字节数组中的起始偏移量。
     *
     * @param len 从 {@code offset} 开始使用的字节数。
     */
    protected abstract void engineUpdate(byte[] input, int offset, int len);

    /**
     * 使用指定的 ByteBuffer 更新摘要。摘要使用 {@code input.remaining()} 个字节从 {@code input.position()} 开始更新。
     * 返回时，缓冲区的位置将等于其限制；其限制不会改变。
     *
     * @param input ByteBuffer
     * @since 1.5
     */
    protected void engineUpdate(ByteBuffer input) {
        if (input.hasRemaining() == false) {
            return;
        }
        if (input.hasArray()) {
            byte[] b = input.array();
            int ofs = input.arrayOffset();
            int pos = input.position();
            int lim = input.limit();
            engineUpdate(b, ofs + pos, lim - pos);
            input.position(lim);
        } else {
            int len = input.remaining();
            int n = JCAUtil.getTempArraySize(len);
            if ((tempArray == null) || (n > tempArray.length)) {
                tempArray = new byte[n];
            }
            while (len > 0) {
                int chunk = Math.min(len, tempArray.length);
                input.get(tempArray, 0, chunk);
                engineUpdate(tempArray, 0, chunk);
                len -= chunk;
            }
        }
    }

    /**
     * 通过执行最终操作（如填充）完成哈希计算。调用 {@code engineDigest} 后，引擎应重置（参见
     * {@link #engineReset() engineReset}）。
     * 重置是引擎实现者的责任。
     *
     * @return 结果哈希值的字节数组。
     */
    protected abstract byte[] engineDigest();

    /**
     * 通过执行最终操作（如填充）完成哈希计算。调用 {@code engineDigest} 后，引擎应重置（参见
     * {@link #engineReset() engineReset}）。
     * 重置是引擎实现者的责任。
     *
     * 此方法应该是抽象的，但为了二进制兼容性，我们将其保留为具体方法。了解情况的提供程序应重写此方法。
     *
     * @param buf 用于存储摘要的输出缓冲区。
     *
     * @param offset 输出缓冲区中的起始偏移量。
     *
     * @param len 在 buf 中分配给摘要的字节数。
     * 本默认实现和 SUN 提供程序都不会返回部分摘要。此参数的存在仅为了 API 的一致性。如果此参数的值小于实际摘要长度，
     * 该方法将抛出 DigestException。如果此参数的值大于或等于实际摘要长度，则忽略此参数。
     *
     * @return 存储在输出缓冲区中的摘要长度。
     *
     * @exception DigestException 如果发生错误。
     *
     * @since 1.2
     */
    protected int engineDigest(byte[] buf, int offset, int len)
                                                throws DigestException {

        byte[] digest = engineDigest();
        if (len < digest.length)
                throw new DigestException("不返回部分摘要");
        if (buf.length - offset < digest.length)
                throw new DigestException("输出缓冲区中没有足够的空间来存储摘要");
        System.arraycopy(digest, 0, buf, offset, digest.length);
        return digest.length;
    }

    /**
     * 重置摘要以供进一步使用。
     */
    protected abstract void engineReset();

    /**
     * 如果实现支持克隆，则返回一个克隆。
     *
     * @return 如果实现支持克隆，则返回一个克隆。
     *
     * @exception CloneNotSupportedException 如果此调用是在不支持 {@code Cloneable} 的实现上进行的。
     */
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        } else {
            throw new CloneNotSupportedException();
        }
    }
}
