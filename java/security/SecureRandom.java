
/*
 * Copyright (c) 1996, 2020, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

import java.util.*;
import java.util.regex.*;

import java.security.Provider.Service;

import sun.security.jca.*;
import sun.security.jca.GetInstance.Instance;
import sun.security.provider.SunEntries;
import sun.security.util.Debug;

/**
 * 该类提供了一个加密强度的随机数生成器 (RNG)。
 *
 * <p>加密强度的随机数至少符合
 * <a href="http://csrc.nist.gov/cryptval/140-2.htm">
 * <i>FIPS 140-2, Security Requirements for Cryptographic Modules</i></a>，
 * 第4.9.1节中规定的统计随机数生成器测试。
 * 此外，SecureRandom 必须生成非确定性输出。
 * 因此，传递给 SecureRandom 对象的任何种子材料必须是不可预测的，
 * 并且所有 SecureRandom 输出序列必须是加密强度的，如
 * <a href="http://www.ietf.org/rfc/rfc1750.txt">
 * <i>RFC 1750: Randomness Recommendations for Security</i></a> 中所述。
 *
 * <p>调用者可以通过无参数构造函数或 {@code getInstance} 方法之一获取 SecureRandom 实例：
 *
 * <pre>
 *      SecureRandom random = new SecureRandom();
 * </pre>
 *
 * <p>许多 SecureRandom 实现形式为伪随机数生成器 (PRNG)，
 * 这意味着它们使用确定性算法从真正的随机种子生成伪随机序列。
 * 其他实现可能生成真正的随机数，
 * 还有一些实现可能结合使用这两种技术。
 *
 * <p>典型的 SecureRandom 调用者调用以下方法来检索随机字节：
 *
 * <pre>
 *      SecureRandom random = new SecureRandom();
 *      byte bytes[] = new byte[20];
 *      random.nextBytes(bytes);
 * </pre>
 *
 * <p>调用者还可以调用 {@code generateSeed} 方法
 * 生成给定数量的种子字节（例如，用于播种其他随机数生成器）：
 * <pre>
 *      byte seed[] = random.generateSeed(20);
 * </pre>
 *
 * 注意：根据实现的不同，{@code generateSeed} 和
 * {@code nextBytes} 方法可能在收集熵时阻塞，
 * 例如，如果它们需要从各种类 Unix 操作系统上的 /dev/random 读取。
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
     * 提供者的实现。
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
     * <p>此构造函数遍历已注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个新的 SecureRandom 对象，封装了第一个支持 SecureRandom (RNG) 算法的
     * 提供者的 SecureRandomSpi 实现。
     * 如果没有提供者支持 RNG 算法，则返回一个实现特定的默认值。
     *
     * <p>可以通过 {@link Security#getProviders() Security.getProviders()} 方法
     * 获取已注册提供者的列表。
     *
     * <p>有关标准 RNG 算法名称的信息，请参阅
     * <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#SecureRandom">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * 中的 SecureRandom 部分。
     *
     * <p>返回的 SecureRandom 对象尚未播种。要为返回的对象播种，请调用 {@code setSeed} 方法。
     * 如果未调用 {@code setSeed}，则对 {@code nextBytes} 的首次调用将强制 SecureRandom 对象自行播种。
     * 如果之前已调用 {@code setSeed}，则不会发生自行播种。
     */
    public SecureRandom() {
        /*
         * 调用我们的超类构造函数将导致调用我们自己的 {@code setSeed} 方法，
         * 当传递零时，该方法将立即返回。
         */
        super(0);
        getDefaultPRNG(false, null);
    }

    /**
     * 构造一个实现默认随机数算法的安全随机数生成器 (RNG)。
     * 使用指定的种子字节为 SecureRandom 实例播种。
     *
     * <p>此构造函数遍历已注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个新的 SecureRandom 对象，封装了第一个支持 SecureRandom (RNG) 算法的
     * 提供者的 SecureRandomSpi 实现。
     * 如果没有提供者支持 RNG 算法，则返回一个实现特定的默认值。
     *
     * <p>可以通过 {@link Security#getProviders() Security.getProviders()} 方法
     * 获取已注册提供者的列表。
     *
     * <p>有关标准 RNG 算法名称的信息，请参阅
     * <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#SecureRandom">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * 中的 SecureRandom 部分。
     *
     * @param seed 种子。
     */
    public SecureRandom(byte seed[]) {
        super(0);
        getDefaultPRNG(true, seed);
    }


private void getDefaultPRNG(boolean setSeed, byte[] seed) {
    Service prngService = null;
    String prngAlgorithm = null;
    for (Provider p : Providers.getProviderList().providers()) {
        // SUN provider uses the SunEntries.DEF_SECURE_RANDOM_ALGO
        // 作为默认的 SecureRandom 算法；对于其他提供者，
        // Provider.getDefaultSecureRandom() 将使用第一个
        // 注册的 SecureRandom 算法
        if (p.getName().equals("SUN")) {
            prngAlgorithm = SunEntries.DEF_SECURE_RANDOM_ALGO;
            prngService = p.getService("SecureRandom", prngAlgorithm);
            break;
        } else {
            prngService = p.getDefaultSecureRandomService();
            if (prngService != null) {
                prngAlgorithm = prngService.getAlgorithm();
                break;
            }
        }
    }
    // 根据 javadoc，如果没有任何提供者支持 RNG 算法，
    // 则返回实现特定的默认值。
    if (prngService == null) {
        prngAlgorithm = "SHA1PRNG";
        this.secureRandomSpi = new sun.security.provider.SecureRandom();
        this.provider = Providers.getSunProvider();
    } else {
        try {
            this.secureRandomSpi = (SecureRandomSpi)
                prngService.newInstance(null);
            this.provider = prngService.getProvider();
        } catch (NoSuchAlgorithmException nsae) {
            // 不应该发生
            throw new RuntimeException(nsae);
        }
    }
    if (setSeed) {
        this.secureRandomSpi.engineSetSeed(seed);
    }
    // JDK 1.1 基于的实现继承了 SecureRandom 而不是
    // SecureRandomSpi。它们也会通过这段代码路径，因为
    // 它们必须调用 SecureRandom 构造函数作为其超类。
    // 如果我们处理的是这样的实现，不要设置算法值，因为它是不准确的。
    if (getClass() == SecureRandom.class) {
        this.algorithm = prngAlgorithm;
    }
}

/**
 * 创建一个 SecureRandom 对象。
 *
 * @param secureRandomSpi SecureRandom 实现。
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
            " 算法来自: " + this.provider.getName());
    }
}

/**
 * 返回实现指定随机数生成器 (RNG) 算法的 SecureRandom 对象。
 *
 * <p> 此方法遍历注册的安全提供者列表，从最优先的提供者开始。
 * 返回一个新的 SecureRandom 对象，封装了
 * 第一个支持指定算法的提供者的 SecureRandomSpi 实现。
 *
 * <p> 可以通过 {@link Security#getProviders() Security.getProviders()} 方法
 * 获取注册提供者的列表。
 *
 * <p> 返回的 SecureRandom 对象尚未播种。要播种返回的对象，调用 {@code setSeed} 方法。
 * 如果未调用 {@code setSeed}，则对 {@code nextBytes} 的第一次调用将强制 SecureRandom 对象自我播种。
 * 如果之前调用了 {@code setSeed}，则不会进行自我播种。
 *
 * @param algorithm RNG 算法的名称。
 * 有关标准 RNG 算法名称的信息，请参阅 Java 密码架构标准算法名称文档的 SecureRandom 部分。
 * <a href="{@docRoot}/../technotes/guides/security/StandardNames.html#SecureRandom">
 * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
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
 * 返回实现指定随机数生成器 (RNG) 算法的 SecureRandom 对象。
 *
 * <p> 返回一个新的 SecureRandom 对象，封装了
 * 指定提供者的 SecureRandomSpi 实现。指定的提供者必须注册在安全提供者列表中。
 *
 * <p> 可以通过 {@link Security#getProviders() Security.getProviders()} 方法
 * 获取注册提供者的列表。
 *
 * <p> 返回的 SecureRandom 对象尚未播种。要播种返回的对象，调用 {@code setSeed} 方法。
 * 如果未调用 {@code setSeed}，则对 {@code nextBytes} 的第一次调用将强制 SecureRandom 对象自我播种。
 * 如果之前调用了 {@code setSeed}，则不会进行自我播种。
 *
 * @param algorithm RNG 算法的名称。
 * 有关标准 RNG 算法名称的信息，请参阅 Java 密码架构标准算法名称文档的 SecureRandom 部分。
 * <a href="{@docRoot}/../technotes/guides/security/StandardNames.html#SecureRandom">
 * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
 *
 * @param provider 提供者的名称。
 *
 * @return 新的 SecureRandom 对象。
 *
 * @exception NoSuchAlgorithmException 如果指定提供者不支持指定算法的 SecureRandomSpi 实现。
 *
 * @exception NoSuchProviderException 如果指定的提供者未注册在安全提供者列表中。
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
     * 返回实现指定随机数生成器（RNG）算法的 SecureRandom 对象。
     *
     * <p> 返回一个新的 SecureRandom 对象，该对象封装了来自指定 Provider 对象的
     * SecureRandomSpi 实现。请注意，指定的 Provider 对象不必在提供者列表中注册。
     *
     * <p> 返回的 SecureRandom 对象尚未播种。为了播种返回的对象，调用 {@code setSeed} 方法。
     * 如果未调用 {@code setSeed}，则对 {@code nextBytes} 的首次调用将强制 SecureRandom 对象自我播种。
     * 如果之前调用了 {@code setSeed}，则不会发生自我播种。
     *
     * @param algorithm 随机数生成器算法的名称。
     * 参见 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#SecureRandom">
     * Java 密码架构标准算法名称文档</a> 中的 SecureRandom 部分，了解标准 RNG 算法名称的信息。
     *
     * @param provider 提供者。
     *
     * @return 新的 SecureRandom 对象。
     *
     * @exception NoSuchAlgorithmException 如果从指定的 Provider 对象中无法获得指定算法的 SecureRandomSpi 实现。
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
     * @return 算法的名称，如果无法确定算法名称，则返回 {@code unknown}。
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
     * 使用给定的 {@code long seed} 中包含的八个字节重新播种此随机对象。给定的种子补充而不是替换现有的种子。因此，重复调用保证永远不会减少随机性。
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
         * 忽略来自超类构造函数的调用（以及任何不幸地传递 0 的其他调用）。在这一点上，忽略来自超类构造函数的调用至关重要，因为摘要尚未初始化。
         */
        if (seed != 0) {
            secureRandomSpi.engineSetSeed(longToByteArray(seed));
        }
    }

    /**
     * 生成用户指定数量的随机字节。
     *
     * <p> 如果之前没有调用 {@code setSeed}，则对本方法的首次调用将强制此 SecureRandom 对象自我播种。如果之前调用了 {@code setSeed}，则不会发生自我播种。
     *
     * @param bytes 要用随机字节填充的数组。
     */
    @Override
    public void nextBytes(byte[] bytes) {
        secureRandomSpi.engineNextBytes(bytes);
    }

    /**
     * 生成包含用户指定数量的伪随机位（右对齐，前导零）的整数。此方法覆盖了 {@code java.util.Random} 的方法，并为从该类继承的所有方法（例如 {@code nextInt}、
     * {@code nextLong} 和 {@code nextFloat}）提供随机位的来源。
     *
     * @param numBits 要生成的伪随机位数，其中 {@code 0 <= numBits <= 32}。
     *
     * @return 包含用户指定数量的伪随机位（右对齐，前导零）的 {@code int}。
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
     * 使用此类用于自我播种的种子生成算法计算并返回指定数量的种子字节。此调用可用于播种其他随机数生成器。
     *
     * <p>此方法仅为了向后兼容而包含。建议调用者使用其中一个替代的
     * {@code getInstance} 方法来获取 SecureRandom 对象，然后调用该对象的 {@code generateSeed} 方法来获取种子字节。
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
     * 使用此类用于自我播种的种子生成算法计算并返回指定数量的种子字节。此调用可用于播种其他随机数生成器。
     *
     * @param numBytes 要生成的种子字节数。
     *
     * @return 种子字节。
     */
    public byte[] generateSeed(int numBytes) {
        if (numBytes < 0) {
            throw new NegativeArraySizeException("numBytes cannot be negative");
        }
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

    /*
     * 延迟初始化，因为 Pattern.compile() 操作较重。
     * 《Effective Java（第 2 版）》第 71 条。
     */
    private static final class StrongPatternHolder {
        /*
         * 条目由 alg:prov 以逗号分隔。
         * 允许在条目之间有前置或后置的空格。
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
     * 返回一个 {@code SecureRandom} 对象，该对象使用 {@code
     * securerandom.strongAlgorithms} {@link Security} 属性中指定的算法/提供者选择。
     * <p>
     * 有些情况下需要强随机值，例如在创建高价值/长期秘密（如 RSA 公钥/私钥）时。
     * 为了帮助应用程序选择合适的强 {@code SecureRandom} 实现，Java 发行版
     * 在 {@code securerandom.strongAlgorithms} 安全属性中包含了一组已知的强 {@code SecureRandom}
     * 实现列表。
     * <p>
     * 每个 Java 平台的实现都必须支持至少一个强 {@code SecureRandom} 实现。
     *
     * @return 由 {@code securerandom.strongAlgorithms} 安全属性指示的强 {@code SecureRandom} 实现
     *
     * @throws NoSuchAlgorithmException 如果没有可用的算法
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

        if (property == null || property.isEmpty()) {
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
     * 我们知道 MessageDigest 类没有实现 java.io.Serializable。然而，由于这个字段不再使用，
     * 它将始终为 NULL，不会影响 SecureRandom 类本身的序列化。
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
