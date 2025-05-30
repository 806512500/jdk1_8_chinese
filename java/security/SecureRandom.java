
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.util.regex.*;

import java.security.Provider.Service;

import sun.security.jca.*;
import sun.security.jca.GetInstance.Instance;
import sun.security.util.Debug;

/**
 * 该类提供了一个加密强度的随机数生成器 (RNG)。
 *
 * <p> 一个加密强度的随机数至少符合 <a href="http://csrc.nist.gov/cryptval/140-2.htm">
 * <i>FIPS 140-2, Security Requirements for Cryptographic Modules</i></a> 中第 4.9.1 节规定的统计随机数生成器测试。
 * 此外，SecureRandom 必须生成非确定性的输出。
 * 因此，传递给 SecureRandom 对象的任何种子材料必须是不可预测的，所有 SecureRandom 输出序列都必须是加密强度的，如
 * <a href="http://www.ietf.org/rfc/rfc1750.txt">
 * <i>RFC 1750: Randomness Recommendations for Security</i></a> 中所述。
 *
 * <p> 调用者可以通过无参数构造函数或其中一个 {@code getInstance} 方法获取 SecureRandom 实例：
 *
 * <pre>
 *      SecureRandom random = new SecureRandom();
 * </pre>
 *
 * <p> 许多 SecureRandom 实现是伪随机数生成器 (PRNG)，这意味着它们使用确定性算法从真正的随机种子生成伪随机序列。
 * 其他实现可能生成真正的随机数，还有一些实现可能结合使用这两种技术。
 *
 * <p> 典型的 SecureRandom 调用者调用以下方法来获取随机字节：
 *
 * <pre>
 *      SecureRandom random = new SecureRandom();
 *      byte bytes[] = new byte[20];
 *      random.nextBytes(bytes);
 * </pre>
 *
 * <p> 调用者还可以调用 {@code generateSeed} 方法生成指定数量的种子字节（例如，用于播种其他随机数生成器）：
 * <pre>
 *      byte seed[] = random.generateSeed(20);
 * </pre>
 *
 * 注意：根据实现的不同，{@code generateSeed} 和 {@code nextBytes} 方法可能在收集熵时阻塞，例如，如果它们需要从各种类 Unix 操作系统上的 /dev/random 读取。
 *
 * @see java.security.SecureRandomSpi
 * @see java.util.Random
 *
 * @author Benjamin Renaud
 * @author Josh Bloch
 */

public class SecureRandom extends java.util.Random {

    private static final Debug pdebug =
                        Debug.getInstance("provider", "Provider");
    private static final boolean skipDebug =
        Debug.isOn("engine=") && !Debug.isOn("securerandom");

    /**
     * 提供者。
     *
     * @serial
     * @since 1.2
     */
    private Provider provider = null;

    /**
     * 提供者实现。
     *
     * @serial
     * @since 1.2
     */
    private SecureRandomSpi secureRandomSpi = null;

    /*
     * 算法名称，如果未知则为 null。
     *
     * @serial
     * @since 1.5
     */
    private String algorithm;

    // 种子生成器
    private static volatile SecureRandom seedGenerator = null;

    /**
     * 构造一个实现默认随机数算法的安全随机数生成器 (RNG)。
     *
     * <p> 该构造函数遍历注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个封装了第一个支持 SecureRandom (RNG) 算法的提供者的 SecureRandomSpi 实现的新 SecureRandom 对象。
     * 如果没有提供者支持 RNG 算法，则返回一个实现特定的默认值。
     *
     * <p> 注意，可以通过 {@link Security#getProviders() Security.getProviders()} 方法检索注册提供者的列表。
     *
     * <p> 有关标准 RNG 算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#SecureRandom">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a> 中的 SecureRandom 部分。
     *
     * <p> 返回的 SecureRandom 对象未被播种。要播种返回的对象，调用 {@code setSeed} 方法。
     * 如果未调用 {@code setSeed}，则第一次调用 {@code nextBytes} 时将强制 SecureRandom 对象自我播种。
     * 如果之前调用了 {@code setSeed}，则不会发生自我播种。
     */
    public SecureRandom() {
        /*
         * 调用我们的超类构造函数将导致调用我们自己的 {@code setSeed} 方法，当传递零时，它将立即返回。
         */
        super(0);
        getDefaultPRNG(false, null);
    }

    /**
     * 构造一个实现默认随机数算法的安全随机数生成器 (RNG)。
     * 使用指定的种子字节播种 SecureRandom 实例。
     *
     * <p> 该构造函数遍历注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个封装了第一个支持 SecureRandom (RNG) 算法的提供者的 SecureRandomSpi 实现的新 SecureRandom 对象。
     * 如果没有提供者支持 RNG 算法，则返回一个实现特定的默认值。
     *
     * <p> 注意，可以通过 {@link Security#getProviders() Security.getProviders()} 方法检索注册提供者的列表。
     *
     * <p> 有关标准 RNG 算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#SecureRandom">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a> 中的 SecureRandom 部分。
     *
     * @param seed 种子。
     */
    public SecureRandom(byte seed[]) {
        super(0);
        getDefaultPRNG(true, seed);
    }

    private void getDefaultPRNG(boolean setSeed, byte[] seed) {
        String prng = getPrngAlgorithm();
        if (prng == null) {
            // 不幸，获取 SUN 实现
            prng = "SHA1PRNG";
            this.secureRandomSpi = new sun.security.provider.SecureRandom();
            this.provider = Providers.getSunProvider();
            if (setSeed) {
                this.secureRandomSpi.engineSetSeed(seed);
            }
        } else {
            try {
                SecureRandom random = SecureRandom.getInstance(prng);
                this.secureRandomSpi = random.getSecureRandomSpi();
                this.provider = random.getProvider();
                if (setSeed) {
                    this.secureRandomSpi.engineSetSeed(seed);
                }
            } catch (NoSuchAlgorithmException nsae) {
                // 不会发生，因为我们已经确保算法存在
                throw new RuntimeException(nsae);
            }
        }
        // JDK 1.1 基于的实现子类化 SecureRandom 而不是 SecureRandomSpi。它们也会通过这个代码路径，因为它们必须调用 SecureRandom 构造函数作为其超类。
        // 如果我们处理的是这样的实现，不要设置算法值，因为这将是不准确的。
        if (getClass() == SecureRandom.class) {
            this.algorithm = prng;
        }
    }

    /**
     * 创建一个 SecureRandom 对象。
     *
     * @param secureRandomSpi 安全随机数生成器实现。
     * @param provider 提供者。
     */
    protected SecureRandom(SecureRandomSpi secureRandomSpi,
                           Provider provider) {
        this(secureRandomSpi, provider, null);
    }

    private SecureRandom(SecureRandomSpi secureRandomSpi, Provider provider,
            String algorithm) {
        super(0);
        this.secureRandomSpi = secureRandomSpi;
        this.provider = provider;
        this.algorithm = algorithm;

        if (!skipDebug && pdebug != null) {
            pdebug.println("SecureRandom." + algorithm +
                " algorithm from: " + this.provider.getName());
        }
    }

    /**
     * 返回一个实现指定随机数生成器 (RNG) 算法的 SecureRandom 对象。
     *
     * <p> 该方法遍历注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个封装了第一个支持指定算法的提供者的 SecureRandomSpi 实现的新 SecureRandom 对象。
     *
     * <p> 注意，可以通过 {@link Security#getProviders() Security.getProviders()} 方法检索注册提供者的列表。
     *
     * <p> 返回的 SecureRandom 对象未被播种。要播种返回的对象，调用 {@code setSeed} 方法。
     * 如果未调用 {@code setSeed}，则第一次调用 {@code nextBytes} 时将强制 SecureRandom 对象自我播种。
     * 如果之前调用了 {@code setSeed}，则不会发生自我播种。
     *
     * @param algorithm RNG 算法的名称。
     * 有关标准 RNG 算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#SecureRandom">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a> 中的 SecureRandom 部分。
     *
     * @return 新的 SecureRandom 对象。
     *
     * @exception NoSuchAlgorithmException 如果没有提供者支持指定算法的 SecureRandomSpi 实现。
     *
     * @see Provider
     *
     * @since 1.2
     */
    public static SecureRandom getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("SecureRandom",
            SecureRandomSpi.class, algorithm);
        return new SecureRandom((SecureRandomSpi)instance.impl,
            instance.provider, algorithm);
    }

    /**
     * 返回一个实现指定随机数生成器 (RNG) 算法的 SecureRandom 对象。
     *
     * <p> 返回一个封装了指定提供者的 SecureRandomSpi 实现的新 SecureRandom 对象。指定的提供者必须在安全提供者列表中注册。
     *
     * <p> 注意，可以通过 {@link Security#getProviders() Security.getProviders()} 方法检索注册提供者的列表。
     *
     * <p> 返回的 SecureRandom 对象未被播种。要播种返回的对象，调用 {@code setSeed} 方法。
     * 如果未调用 {@code setSeed}，则第一次调用 {@code nextBytes} 时将强制 SecureRandom 对象自我播种。
     * 如果之前调用了 {@code setSeed}，则不会发生自我播种。
     *
     * @param algorithm RNG 算法的名称。
     * 有关标准 RNG 算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#SecureRandom">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a> 中的 SecureRandom 部分。
     *
     * @param provider 提供者的名称。
     *
     * @return 新的 SecureRandom 对象。
     *
     * @exception NoSuchAlgorithmException 如果指定提供者不支持指定算法的 SecureRandomSpi 实现。
     *
     * @exception NoSuchProviderException 如果指定的提供者未在安全提供者列表中注册。
     *
     * @exception IllegalArgumentException 如果提供者名称为空或为 null。
     *
     * @see Provider
     *
     * @since 1.2
     */
    public static SecureRandom getInstance(String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        Instance instance = GetInstance.getInstance("SecureRandom",
            SecureRandomSpi.class, algorithm, provider);
        return new SecureRandom((SecureRandomSpi)instance.impl,
            instance.provider, algorithm);
    }

    /**
     * 返回一个实现指定随机数生成器 (RNG) 算法的 SecureRandom 对象。
     *
     * <p> 返回一个封装了指定 Provider 对象的 SecureRandomSpi 实现的新 SecureRandom 对象。注意，指定的 Provider 对象不必在提供者列表中注册。
     *
     * <p> 返回的 SecureRandom 对象未被播种。要播种返回的对象，调用 {@code setSeed} 方法。
     * 如果未调用 {@code setSeed}，则第一次调用 {@code nextBytes} 时将强制 SecureRandom 对象自我播种。
     * 如果之前调用了 {@code setSeed}，则不会发生自我播种。
     *
     * @param algorithm RNG 算法的名称。
     * 有关标准 RNG 算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#SecureRandom">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a> 中的 SecureRandom 部分。
     *
     * @param provider 提供者。
     *
     * @return 新的 SecureRandom 对象。
     *
     * @exception NoSuchAlgorithmException 如果指定 Provider 对象不支持指定算法的 SecureRandomSpi 实现。
     *
     * @exception IllegalArgumentException 如果指定的提供者为 null。
     *
     * @see Provider
     *
     * @since 1.4
     */
    public static SecureRandom getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("SecureRandom",
            SecureRandomSpi.class, algorithm, provider);
        return new SecureRandom((SecureRandomSpi)instance.impl,
            instance.provider, algorithm);
    }

    /**
     * 返回此 SecureRandom 对象的 SecureRandomSpi。
     */
    SecureRandomSpi getSecureRandomSpi() {
        return secureRandomSpi;
    }


                /**
     * 返回此 SecureRandom 对象的提供者。
     *
     * @return 此 SecureRandom 对象的提供者。
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * 返回此 SecureRandom 对象实现的算法的名称。
     *
     * @return 算法的名称或 {@code unknown}
     *          如果无法确定算法名称。
     * @since 1.5
     */
    public String getAlgorithm() {
        return (algorithm != null) ? algorithm : "unknown";
    }

    /**
     * 重新播种此随机对象。给定的种子补充而不是替换现有的种子。因此，重复调用保证永远不会减少随机性。
     *
     * @param seed 种子。
     *
     * @see #getSeed
     */
    synchronized public void setSeed(byte[] seed) {
        secureRandomSpi.engineSetSeed(seed);
    }

    /**
     * 重新播种此随机对象，使用给定的 {@code long seed} 中包含的八个字节。给定的种子补充而不是替换现有的种子。因此，重复调用保证永远不会减少随机性。
     *
     * <p>此方法是为了与 {@code java.util.Random} 兼容而定义的。
     *
     * @param seed 种子。
     *
     * @see #getSeed
     */
    @Override
    public void setSeed(long seed) {
        /*
         * 忽略来自超类构造函数的调用（以及任何其他不幸地传递 0 的调用）。忽略超类构造函数的调用至关重要，因为此时 digest 尚未初始化。
         */
        if (seed != 0) {
            secureRandomSpi.engineSetSeed(longToByteArray(seed));
        }
    }

    /**
     * 生成用户指定数量的随机字节。
     *
     * <p>如果之前没有调用 {@code setSeed}，则此方法的第一次调用将强制此 SecureRandom 对象自播种。如果之前调用了 {@code setSeed}，则不会发生自播种。
     *
     * @param bytes 要填充随机字节的数组。
     */
    @Override
    public void nextBytes(byte[] bytes) {
        secureRandomSpi.engineNextBytes(bytes);
    }

    /**
     * 生成包含用户指定数量的伪随机位的整数（右对齐，前导零）。此方法覆盖了 {@code java.util.Random} 的方法，并为从该类继承的所有方法（例如 {@code nextInt}、
     * {@code nextLong} 和 {@code nextFloat}）提供随机位的来源。
     *
     * @param numBits 要生成的伪随机位数，其中
     * {@code 0 <= numBits <= 32}。
     *
     * @return 包含用户指定数量的伪随机位的 {@code int}（右对齐，前导零）。
     */
    @Override
    final protected int next(int numBits) {
        int numBytes = (numBits+7)/8;
        byte b[] = new byte[numBytes];
        int next = 0;

        nextBytes(b);
        for (int i = 0; i < numBytes; i++) {
            next = (next << 8) + (b[i] & 0xFF);
        }

        return next >>> (numBytes*8 - numBits);
    }

    /**
     * 返回使用此类用于自播种的种子生成算法计算的指定数量的种子字节。此调用可用于播种其他随机数生成器。
     *
     * <p>此方法仅包含以保持向后兼容性。建议调用者使用其中一个替代的
     * {@code getInstance} 方法来获取 SecureRandom 对象，并调用该对象的 {@code generateSeed} 方法以获取种子字节。
     *
     * @param numBytes 要生成的种子字节数。
     *
     * @return 种子字节。
     *
     * @see #setSeed
     */
    public static byte[] getSeed(int numBytes) {
        if (seedGenerator == null) {
            seedGenerator = new SecureRandom();
        }
        return seedGenerator.generateSeed(numBytes);
    }

    /**
     * 返回使用此类用于自播种的种子生成算法计算的指定数量的种子字节。此调用可用于播种其他随机数生成器。
     *
     * @param numBytes 要生成的种子字节数。
     *
     * @return 种子字节。
     */
    public byte[] generateSeed(int numBytes) {
        return secureRandomSpi.engineGenerateSeed(numBytes);
    }

    /**
     * 辅助函数，将 long 转换为字节数组（最低有效字节在前）。
     */
    private static byte[] longToByteArray(long l) {
        byte[] retVal = new byte[8];

        for (int i = 0; i < 8; i++) {
            retVal[i] = (byte) l;
            l >>= 8;
        }

        return retVal;
    }

    /**
     * 通过查找所有已注册的提供者来获取默认的 PRNG 算法。返回第一个提供者注册的 SecureRandom 实现的第一个 PRNG 算法，如果没有已注册的提供者提供 SecureRandom 实现，则返回 null。
     */
    private static String getPrngAlgorithm() {
        for (Provider p : Providers.getProviderList().providers()) {
            for (Service s : p.getServices()) {
                if (s.getType().equals("SecureRandom")) {
                    return s.getAlgorithm();
                }
            }
        }
        return null;
    }

    /*
     * 延迟初始化，因为 Pattern.compile() 很重。
     * 《Effective Java（第 2 版）》第 71 条。
     */
    private static final class StrongPatternHolder {
        /*
         * 条目由 alg:prov 分隔，用逗号分隔。
         * 允许在条目之间有前导和尾随空格。
         *
         * 捕获组：
         *     1 - alg
         *     2 - :prov（可选）
         *     3 - prov（可选）
         *     4 - ,nextEntry（可选）
         *     5 - nextEntry（可选）
         */
        private static Pattern pattern =
            Pattern.compile(
                "\\s*([\\S&&[^:,]]*)(\\:([\\S&&[^,]]*))?\\s*(\\,(.*))?");
    }

    /**
     * 返回一个使用 {@code securerandom.strongAlgorithms} {@link Security} 属性中指定的算法/提供者选择的 {@code SecureRandom} 对象。
     * <p>
     * 有些情况需要强随机值，例如创建高价值/长期秘密（如 RSA 公钥/私钥）。为了帮助应用程序选择合适的强
     * {@code SecureRandom} 实现，Java 发行版在 {@code securerandom.strongAlgorithms} 安全属性中包含了一组已知的强
     * {@code SecureRandom} 实现列表。
     * <p>
     * 每个 Java 平台的实现都必须支持至少一个强 {@code SecureRandom} 实现。
     *
     * @return 由 {@code securerandom.strongAlgorithms} 安全属性指示的强 {@code SecureRandom} 实现。
     *
     * @throws NoSuchAlgorithmException 如果没有可用的算法。
     *
     * @see Security#getProperty(String)
     *
     * @since 1.8
     */
    public static SecureRandom getInstanceStrong()
            throws NoSuchAlgorithmException {

        String property = AccessController.doPrivileged(
            new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return Security.getProperty(
                        "securerandom.strongAlgorithms");
                }
            });

        if ((property == null) || (property.length() == 0)) {
            throw new NoSuchAlgorithmException(
                "Null/empty securerandom.strongAlgorithms Security Property");
        }

        String remainder = property;
        while (remainder != null) {
            Matcher m;
            if ((m = StrongPatternHolder.pattern.matcher(
                    remainder)).matches()) {

                String alg = m.group(1);
                String prov = m.group(3);

                try {
                    if (prov == null) {
                        return SecureRandom.getInstance(alg);
                    } else {
                        return SecureRandom.getInstance(alg, prov);
                    }
                } catch (NoSuchAlgorithmException |
                        NoSuchProviderException e) {
                }
                remainder = m.group(5);
            } else {
                remainder = null;
            }
        }

        throw new NoSuchAlgorithmException(
            "No strong SecureRandom impls available: " + property);
    }

    // 声明 serialVersionUID 以与 JDK1.1 兼容
    static final long serialVersionUID = 4940670005562187L;

    // 保留从 JDK1.1 序列化的未使用值
    /**
     * @serial
     */
    private byte[] state;
    /**
     * @serial
     */
    private MessageDigest digest = null;
    /**
     * @serial
     *
     * 我们知道 MessageDigest 类不实现 java.io.Serializable。然而，由于此字段不再使用，它将始终为 NULL，不会影响 SecureRandom 类本身的序列化。
     */
    private byte[] randomBytes;
    /**
     * @serial
     */
    private int randomBytesUsed;
    /**
     * @serial
     */
    private long counter;
}
