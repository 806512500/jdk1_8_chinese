
/*
 * Copyright (c) 1996, 2020, Oracle and/or its affiliates. All rights reserved.
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

import java.util.*;
import java.lang.*;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.nio.ByteBuffer;

import sun.security.util.Debug;
import sun.security.util.MessageDigestSpi2;

import javax.crypto.SecretKey;

/**
 * 此 MessageDigest 类为应用程序提供消息摘要算法的功能，例如 SHA-1 或 SHA-256。
 * 消息摘要是安全的单向哈希函数，可以处理任意大小的数据并输出固定长度的哈希值。
 *
 * <p>MessageDigest 对象最初是初始化的。数据通过 {@link #update(byte) update}
 * 方法进行处理。在任何时候都可以调用 {@link #reset() reset} 来重置摘要。一旦所有要更新的数据
 * 都已更新，应调用其中一个 {@link #digest() digest} 方法来完成哈希计算。
 *
 * <p>{@code digest} 方法可以为给定数量的更新调用一次。调用 {@code digest} 后，MessageDigest
 * 对象将重置为其初始化状态。
 *
 * <p>实现可以自由选择实现 Cloneable 接口。客户端应用程序可以通过尝试克隆并捕获
 * CloneNotSupportedException 来测试克隆性：
 *
 * <pre>{@code
 * MessageDigest md = MessageDigest.getInstance("SHA-256");
 *
 * try {
 *     md.update(toChapter1);
 *     MessageDigest tc1 = md.clone();
 *     byte[] toChapter1Digest = tc1.digest();
 *     md.update(toChapter2);
 *     ...etc.
 * } catch (CloneNotSupportedException cnse) {
 *     throw new DigestException("无法计算部分内容的摘要");
 * }
 * }</pre>
 *
 * <p>如果给定的实现不可克隆，仍然可以通过实例化多个实例来计算中间摘要，前提是已知摘要的数量。
 *
 * <p>请注意，此类是抽象的，并且出于历史原因扩展自
 * {@code MessageDigestSpi}。应用程序开发人员应仅注意此 {@code MessageDigest} 类中定义的方法；
 * 超类中的所有方法都是为希望提供自己的消息摘要算法实现的加密服务提供商设计的。
 *
 * <p>每个 Java 平台的实现都必须支持以下标准 {@code MessageDigest} 算法：
 * <ul>
 * <li>{@code MD5}</li>
 * <li>{@code SHA-1}</li>
 * <li>{@code SHA-256}</li>
 * </ul>
 * 这些算法在 <a href=
 * "{@docRoot}/../technotes/guides/security/StandardNames.html#MessageDigest">
 * Java 加密体系结构标准算法名称文档的 MessageDigest 部分</a>中描述。请参阅您的实现的发行文档，了解是否支持其他算法。
 *
 * @author Benjamin Renaud
 *
 * @see DigestInputStream
 * @see DigestOutputStream
 */

public abstract class MessageDigest extends MessageDigestSpi {

    private static final Debug pdebug =
                        Debug.getInstance("provider", "Provider");
    private static final boolean skipDebug =
        Debug.isOn("engine=") && !Debug.isOn("messagedigest");

    private String algorithm;

    // 此摘要的状态
    private static final int INITIAL = 0;
    private static final int IN_PROGRESS = 1;
    private int state = INITIAL;

    // 提供者
    private Provider provider;

    /**
     * 使用指定的算法名称创建消息摘要。
     *
     * @param algorithm 摘要算法的标准名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#MessageDigest">
     * Java 加密体系结构标准算法名称文档的 MessageDigest 部分</a>。
     */
    protected MessageDigest(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * 返回实现指定摘要算法的 MessageDigest 对象。
     *
     * <p>此方法遍历注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个新的 MessageDigest 对象，封装了支持指定算法的第一个
     * 提供者的 MessageDigestSpi 实现。
     *
     * <p>可以通过 {@link Security#getProviders() Security.getProviders()} 方法
     * 获取注册提供者的列表。
     *
     * @param algorithm 请求的算法名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#MessageDigest">
     * Java 加密体系结构标准算法名称文档的 MessageDigest 部分</a>。
     *
     * @return 实现指定算法的 MessageDigest 对象。
     *
     * @exception NoSuchAlgorithmException 如果没有提供者支持指定算法的
     *          MessageDigestSpi 实现。
     *
     * @see Provider
     */
    public static MessageDigest getInstance(String algorithm)
    throws NoSuchAlgorithmException {
        try {
            MessageDigest md;
            Object[] objs = Security.getImpl(algorithm, "MessageDigest",
                                             (String)null);
            if (objs[0] instanceof MessageDigest) {
                md = (MessageDigest)objs[0];
            } else {
                md = new Delegate((MessageDigestSpi)objs[0], algorithm);
            }
            md.provider = (Provider)objs[1];

            if (!skipDebug && pdebug != null) {
                pdebug.println("MessageDigest." + algorithm +
                    " 算法来自: " + md.provider.getName());
            }

            return md;

        } catch(NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(algorithm + " 未找到");
        }
    }

    /**
     * 返回实现指定摘要算法的 MessageDigest 对象。
     *
     * <p>返回一个新的 MessageDigest 对象，封装了指定提供者的
     * MessageDigestSpi 实现。指定的提供者必须注册在安全提供者列表中。
     *
     * <p>可以通过 {@link Security#getProviders() Security.getProviders()} 方法
     * 获取注册提供者的列表。
     *
     * @param algorithm 请求的算法名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#MessageDigest">
     * Java 加密体系结构标准算法名称文档的 MessageDigest 部分</a>。
     *
     * @param provider 提供者的名称。
     *
     * @return 实现指定算法的 MessageDigest 对象。
     *
     * @exception NoSuchAlgorithmException 如果指定提供者不支持指定算法的
     *          MessageDigestSpi 实现。
     *
     * @exception NoSuchProviderException 如果指定的提供者未注册在安全提供者列表中。
     *
     * @exception IllegalArgumentException 如果提供者名称为空或为空字符串。
     *
     * @see Provider
     */
    public static MessageDigest getInstance(String algorithm, String provider)
        throws NoSuchAlgorithmException, NoSuchProviderException
    {
        if (provider == null || provider.length() == 0)
            throw new IllegalArgumentException("缺少提供者");
        Object[] objs = Security.getImpl(algorithm, "MessageDigest", provider);
        if (objs[0] instanceof MessageDigest) {
            MessageDigest md = (MessageDigest)objs[0];
            md.provider = (Provider)objs[1];
            return md;
        } else {
            MessageDigest delegate =
                new Delegate((MessageDigestSpi)objs[0], algorithm);
            delegate.provider = (Provider)objs[1];
            return delegate;
        }
    }

    /**
     * 返回实现指定摘要算法的 MessageDigest 对象。
     *
     * <p>返回一个新的 MessageDigest 对象，封装了指定 Provider 对象的
     * MessageDigestSpi 实现。请注意，指定的 Provider 对象
     * 不必注册在提供者列表中。
     *
     * @param algorithm 请求的算法名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#MessageDigest">
     * Java 加密体系结构标准算法名称文档的 MessageDigest 部分</a>。
     *
     * @param provider 提供者。
     *
     * @return 实现指定算法的 MessageDigest 对象。
     *
     * @exception NoSuchAlgorithmException 如果指定 Provider 对象不支持指定算法的
     *          MessageDigestSpi 实现。
     *
     * @exception IllegalArgumentException 如果指定的提供者为空。
     *
     * @see Provider
     *
     * @since 1.4
     */
    public static MessageDigest getInstance(String algorithm,
                                            Provider provider)
        throws NoSuchAlgorithmException
    {
        if (provider == null)
            throw new IllegalArgumentException("缺少提供者");
        Object[] objs = Security.getImpl(algorithm, "MessageDigest", provider);
        if (objs[0] instanceof MessageDigest) {
            MessageDigest md = (MessageDigest)objs[0];
            md.provider = (Provider)objs[1];
            return md;
        } else {
            MessageDigest delegate =
                new Delegate((MessageDigestSpi)objs[0], algorithm);
            delegate.provider = (Provider)objs[1];
            return delegate;
        }
    }

    /**
     * 返回此消息摘要对象的提供者。
     *
     * @return 此消息摘要对象的提供者
     */
    public final Provider getProvider() {
        return this.provider;
    }

    /**
     * 使用指定的字节更新摘要。
     *
     * @param input 用于更新摘要的字节。
     */
    public void update(byte input) {
        engineUpdate(input);
        state = IN_PROGRESS;
    }

    /**
     * 使用指定的字节数组，从指定的偏移量开始更新摘要。
     *
     * @param input 字节数组。
     *
     * @param offset 从字节数组中的偏移量开始。
     *
     * @param len 从 {@code offset} 开始使用的字节数。
     */
    public void update(byte[] input, int offset, int len) {
        if (input == null) {
            throw new IllegalArgumentException("未提供输入缓冲区");
        }
        if (input.length - offset < len) {
            throw new IllegalArgumentException("输入缓冲区太短");
        }
        engineUpdate(input, offset, len);
        state = IN_PROGRESS;
    }

    /**
     * 使用指定的字节数组更新摘要。
     *
     * @param input 字节数组。
     */
    public void update(byte[] input) {
        engineUpdate(input, 0, input.length);
        state = IN_PROGRESS;
    }

    /**
     * 使用指定的 ByteBuffer 更新摘要。摘要使用 {@code input.remaining()} 字节
     * 从 {@code input.position()} 开始更新。返回时，缓冲区的位置将等于其限制；
     * 其限制不会改变。
     *
     * @param input ByteBuffer
     * @since 1.5
     */
    public final void update(ByteBuffer input) {
        if (input == null) {
            throw new NullPointerException();
        }
        engineUpdate(input);
        state = IN_PROGRESS;
    }

    /**
     * 通过执行最终操作（如填充）完成哈希计算。调用此方法后，摘要将重置。
     *
     * @return 结果哈希值的字节数组。
     */
    public byte[] digest() {
        /* 重置是实现者的责任。 */
        byte[] result = engineDigest();
        state = INITIAL;
        return result;
    }

    /**
     * 通过执行最终操作（如填充）完成哈希计算。调用此方法后，摘要将重置。
     *
     * @param buf 用于存储计算摘要的输出缓冲区
     *
     * @param offset 输出缓冲区中存储摘要的偏移量
     *
     * @param len buf 中分配给摘要的字节数
     *
     * @return 放入 {@code buf} 的字节数
     *
     * @exception DigestException 如果发生错误。
     */
    public int digest(byte[] buf, int offset, int len) throws DigestException {
        if (buf == null) {
            throw new IllegalArgumentException("未提供输出缓冲区");
        }
        if (buf.length - offset < len) {
            throw new IllegalArgumentException
                ("输出缓冲区对于指定的偏移量和长度来说太小");
        }
        int numBytes = engineDigest(buf, offset, len);
        state = INITIAL;
        return numBytes;
    }

    /**
     * 使用指定的字节数组对摘要进行最终更新，然后完成摘要计算。也就是说，此方法首先调用
     * {@link #update(byte[]) update(input)}，将 <i>input</i> 数组传递给
     * {@code update} 方法，然后调用 {@link #digest() digest()}。
     *
     * @param input 在完成摘要之前要更新的输入。
     *
     * @return 结果哈希值的字节数组。
     */
    public byte[] digest(byte[] input) {
        update(input);
        return digest();
    }


    /**
     * 返回此消息摘要对象的字符串表示形式。
     */
    public String toString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream p = new PrintStream(baos);
        p.print(algorithm+" Message Digest from "+provider.getName()+", ");
        switch (state) {
        case INITIAL:
            p.print("<initialized>");
            break;
        case IN_PROGRESS:
            p.print("<in progress>");
            break;
        }
        p.println();
        return (baos.toString());
    }

    /**
     * 比较两个摘要是否相等。进行简单的字节比较。
     *
     * @implNote
     * 检查 {@code digesta} 中的所有字节以确定相等性。
     * 计算时间仅取决于 {@code digesta} 的长度。
     * 与 {@code digestb} 的长度或 {@code digesta} 和 {@code digestb} 的内容无关。
     *
     * @param digesta 要比较的一个摘要。
     *
     * @param digestb 要比较的另一个摘要。
     *
     * @return 如果摘要相等则返回 true，否则返回 false。
     */
    public static boolean isEqual(byte[] digesta, byte[] digestb) {
        if (digesta == digestb) return true;
        if (digesta == null || digestb == null) {
            return false;
        }

        int lenA = digesta.length;
        int lenB = digestb.length;

        if (lenB == 0) {
            return lenA == 0;
        }

        int result = 0;
        result |= lenA - lenB;

        // 时间常量比较
        for (int i = 0; i < lenA; i++) {
            // 如果 i >= lenB，indexB 为 0；否则为 i。
            int indexB = ((i - lenB) >>> 31) * i;
            result |= digesta[i] ^ digestb[indexB];
        }
        return result == 0;
    }

    /**
     * 重置摘要以供进一步使用。
     */
    public void reset() {
        engineReset();
        state = INITIAL;
    }

    /**
     * 返回标识算法的字符串，独立于实现细节。名称应为标准的
     * Java 安全名称（如 "SHA-256"）。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#MessageDigest">
     * Java 加密体系结构标准算法名称文档</a>。
     *
     * @return 算法的名称
     */
    public final String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * 返回摘要的字节长度，如果提供者不支持此操作且实现不可克隆，则返回 0。
     *
     * @return 摘要的字节长度，如果提供者不支持此操作且实现不可克隆，则返回 0。
     *
     * @since 1.2
     */
    public final int getDigestLength() {
        int digestLen = engineGetDigestLength();
        if (digestLen == 0) {
            try {
                MessageDigest md = (MessageDigest)clone();
                byte[] digest = md.digest();
                return digest.length;
            } catch (CloneNotSupportedException e) {
                return digestLen;
            }
        }
        return digestLen;
    }

    /**
     * 如果实现可克隆，则返回一个克隆。
     *
     * @return 如果实现可克隆，则返回一个克隆。
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




    /*
     * 以下类允许提供者扩展自 MessageDigestSpi
     * 而不是从 MessageDigest 扩展。它表示一个带有封装的、提供者提供的 SPI 对象（类型为 MessageDigestSpi）的消息摘要。
     * 如果提供者实现是 MessageDigestSpi 的实例，
     * 上面的 getInstance() 方法将返回此类的一个实例，其中封装了 SPI 对象。
     *
     * 注意：原始 MessageDigest 类中的所有 SPI 方法都已移到层次结构中更高一级的新类（MessageDigestSpi）中，
     * 该类插入在 API（MessageDigest）和其原始父类（Object）之间。
     */

    static class Delegate extends MessageDigest implements MessageDigestSpi2 {

        // 提供者实现（代理）
        private MessageDigestSpi digestSpi;

        // 构造函数
        public Delegate(MessageDigestSpi digestSpi, String algorithm) {
            super(algorithm);
            this.digestSpi = digestSpi;
        }

        /**
         * 如果代理可克隆，则返回一个克隆。
         *
         * @return 如果代理可克隆，则返回一个克隆。
         *
         * @exception CloneNotSupportedException 如果此调用是在不支持 {@code Cloneable} 的代理上进行的。
         */
        public Object clone() throws CloneNotSupportedException {
            if (digestSpi instanceof Cloneable) {
                MessageDigestSpi digestSpiClone =
                    (MessageDigestSpi)digestSpi.clone();
                // 因为 'algorithm'、'provider' 和 'state' 是我们超类的私有成员，
                // 必须进行强制转换以访问它们。
                MessageDigest that =
                    new Delegate(digestSpiClone,
                                 ((MessageDigest)this).algorithm);
                that.provider = ((MessageDigest)this).provider;
                that.state = ((MessageDigest)this).state;
                return that;
            } else {
                throw new CloneNotSupportedException();
            }
        }

        protected int engineGetDigestLength() {
            return digestSpi.engineGetDigestLength();
        }

        protected void engineUpdate(byte input) {
            digestSpi.engineUpdate(input);
        }

        protected void engineUpdate(byte[] input, int offset, int len) {
            digestSpi.engineUpdate(input, offset, len);
        }

        protected void engineUpdate(ByteBuffer input) {
            digestSpi.engineUpdate(input);
        }

        public void engineUpdate(SecretKey key) throws InvalidKeyException {
            if (digestSpi instanceof MessageDigestSpi2) {
                ((MessageDigestSpi2)digestSpi).engineUpdate(key);
            } else {
                throw new UnsupportedOperationException
                ("Digest does not support update of SecretKey object");
            }
        }
        protected byte[] engineDigest() {
            return digestSpi.engineDigest();
        }

        protected int engineDigest(byte[] buf, int offset, int len)
            throws DigestException {
                return digestSpi.engineDigest(buf, offset, len);
        }

        protected void engineReset() {
            digestSpi.engineReset();
        }
    }
}
