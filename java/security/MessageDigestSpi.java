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

import java.nio.ByteBuffer;

import sun.security.jca.JCAUtil;

/**
 * 本类定义了 {@code MessageDigest} 类的 <i>服务提供者接口</i> (<b>SPI</b>)，
 * 提供了消息摘要算法（如 MD5 或 SHA）的功能。消息摘要是安全的单向哈希函数，可以接受任意大小的数据并输出固定长度的哈希值。
 *
 * <p> 本类中的所有抽象方法必须由希望提供特定消息摘要算法实现的密码服务提供者实现。
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
     * <p>提供者可以覆盖此方法以返回摘要长度。
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
     * @param offset 在字节数组中开始的位置。
     *
     * @param len 从 {@code offset} 开始使用的字节数。
     */
    protected abstract void engineUpdate(byte[] input, int offset, int len);

    /**
     * 使用指定的 ByteBuffer 更新摘要。摘要使用 {@code input.remaining()} 个字节，
     * 从 {@code input.position()} 开始。返回时，缓冲区的位置将等于其限制；
     * 其限制不会改变。
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
     * 通过执行最终操作（如填充）完成哈希计算。一旦调用 {@code engineDigest}，
     * 引擎应被重置（参见 {@link #engineReset() engineReset}）。
     * 重置是引擎实现者的责任。
     *
     * @return 结果哈希值的字节数组。
     */
    protected abstract byte[] engineDigest();

    /**
     * 通过执行最终操作（如填充）完成哈希计算。一旦调用 {@code engineDigest}，
     * 引擎应被重置（参见 {@link #engineReset() engineReset}）。
     * 重置是引擎实现者的责任。
     *
     * 此方法应该是抽象的，但为了二进制兼容性，我们保留了具体实现。了解的提供者应覆盖此方法。
     *
     * @param buf 存储摘要的输出缓冲区。
     *
     * @param offset 在输出缓冲区中开始的位置。
     *
     * @param len 在 buf 中分配给摘要的字节数。
     * 无论是此默认实现还是 SUN 提供者都不会返回部分摘要。此参数的存在仅为了 API 的一致性。如果此参数的值小于实际摘要长度，
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
                throw new DigestException("输出缓冲区中没有足够的空间存储摘要");
        System.arraycopy(digest, 0, buf, offset, digest.length);
        return digest.length;
    }

    /**
     * 重置摘要以供进一步使用。
     */
    protected abstract void engineReset();

    /**
     * 如果实现是可克隆的，则返回一个克隆。
     *
     * @return 如果实现是可克隆的，则返回一个克隆。
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
