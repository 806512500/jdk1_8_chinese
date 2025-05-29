
/*
 * 版权所有 (c) 1997, 2014, Oracle 和/或其关联公司。保留所有权利。
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

import java.util.*;

import java.security.spec.AlgorithmParameterSpec;

import java.security.Provider.Service;

import sun.security.jca.*;
import sun.security.jca.GetInstance.Instance;
import sun.security.util.Debug;

/**
 * KeyPairGenerator 类用于生成公钥和私钥对。密钥对生成器使用
 * {@code getInstance} 工厂方法（静态方法，返回给定类的实例）构建。
 *
 * <p>特定算法的密钥对生成器创建可以与该算法一起使用的公钥/私钥对。它还为每个生成的密钥关联算法特定的参数。
 *
 * <p>生成密钥对有两种方式：算法无关的方式和算法特定的方式。两者之间的唯一区别是对象的初始化：
 *
 * <ul>
 * <li><b>算法无关的初始化</b>
 * <p>所有密钥对生成器共享密钥大小和随机源的概念。密钥大小对于不同算法的解释不同（例如，在 <i>DSA</i> 算法中，密钥大小对应于模数的长度）。
 * 本 KeyPairGenerator 类中有一个
 * {@link #initialize(int, java.security.SecureRandom) initialize}
 * 方法接受这两个通用参数。还有一个只接受 {@code keysize} 参数的方法，使用最高优先级安装提供者的 {@code SecureRandom}
 * 实现作为随机源。（如果没有任何安装的提供者提供 {@code SecureRandom} 的实现，则使用系统提供的随机源。）
 *
 * <p>由于在调用上述算法无关的 {@code initialize} 方法时没有指定其他参数，因此由提供者决定如何处理与每个密钥关联的算法特定参数（如果有）。
 *
 * <p>如果算法是 <i>DSA</i> 算法，且密钥大小（模数大小）为 512、768 或 1024，则 <i>Sun</i> 提供者使用一组预计算的值作为 {@code p}、{@code q} 和
 * {@code g} 参数。如果模数大小不是上述值之一，则 <i>Sun</i> 提供者创建一组新的参数。其他提供者可能有超过上述三种模数大小的预计算参数集。还有一些提供者可能根本没有预计算参数列表，而是始终创建新的参数集。
 *
 * <li><b>算法特定的初始化</b>
 * <p>对于已经存在一组算法特定参数的情况（例如，DSA 中所谓的 <i>社区参数</i>），有两个
 * {@link #initialize(java.security.spec.AlgorithmParameterSpec)
 * initialize} 方法接受 {@code AlgorithmParameterSpec} 参数。其中一个方法还接受一个 {@code SecureRandom} 参数，而
 * 另一个方法使用最高优先级安装提供者的 {@code SecureRandom}
 * 实现作为随机源。（如果没有任何安装的提供者提供 {@code SecureRandom} 的实现，则使用系统提供的随机源。）
 * </ul>
 *
 * <p>如果客户端没有显式初始化 KeyPairGenerator（通过调用 {@code initialize} 方法），则每个提供者必须
 * 提供（并记录）默认初始化。例如，<i>Sun</i> 提供者使用 1024 位的默认模数大小（密钥大小）。
 *
 * <p>注意，此类是抽象的，并且由于历史原因扩展自
 * {@code KeyPairGeneratorSpi}。应用程序开发人员应仅关注在此
 * {@code KeyPairGenerator} 类中定义的方法；超类中的所有方法都是为希望提供自己的密钥对生成器实现的加密服务提供者设计的。
 *
 * <p>每个 Java 平台的实现都必须支持以下标准 {@code KeyPairGenerator} 算法和密钥大小（括号内）：
 * <ul>
 * <li>{@code DiffieHellman} (1024)</li>
 * <li>{@code DSA} (1024)</li>
 * <li>{@code RSA} (1024, 2048)</li>
 * </ul>
 * 这些算法在 <a href=
 * "{@docRoot}/../technotes/guides/security/StandardNames.html#KeyPairGenerator">
 * Java 加密架构标准算法名称文档的 KeyPairGenerator 部分</a>中描述。请参阅您的实现的发行文档，了解是否支持其他算法。
 *
 * @author Benjamin Renaud
 *
 * @see java.security.spec.AlgorithmParameterSpec
 */

public abstract class KeyPairGenerator extends KeyPairGeneratorSpi {

    private static final Debug pdebug =
                        Debug.getInstance("provider", "Provider");
    private static final boolean skipDebug =
        Debug.isOn("engine=") && !Debug.isOn("keypairgenerator");

    private final String algorithm;

    // 提供者
    Provider provider;

    /**
     * 为指定的算法创建一个 KeyPairGenerator 对象。
     *
     * @param algorithm 算法的标准字符串名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#KeyPairGenerator">
     * Java 加密架构标准算法名称文档的 KeyPairGenerator 部分</a>。
     */
    protected KeyPairGenerator(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * 返回此密钥对生成器的算法的标准名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#KeyPairGenerator">
     * Java 加密架构标准算法名称文档的 KeyPairGenerator 部分</a>。
     *
     * @return 算法的标准字符串名称。
     */
    public String getAlgorithm() {
        return this.algorithm;
    }


                private static KeyPairGenerator getInstance(Instance instance,
            String algorithm) {
        KeyPairGenerator kpg;
        if (instance.impl instanceof KeyPairGenerator) {
            kpg = (KeyPairGenerator)instance.impl;
        } else {
            KeyPairGeneratorSpi spi = (KeyPairGeneratorSpi)instance.impl;
            kpg = new Delegate(spi, algorithm);
        }
        kpg.provider = instance.provider;

        if (!skipDebug && pdebug != null) {
            pdebug.println("KeyPairGenerator." + algorithm +
                " algorithm from: " + kpg.provider.getName());
        }

        return kpg;
    }

    /**
     * 返回一个用于生成指定算法的公钥/私钥对的 KeyPairGenerator 对象。
     *
     * <p> 该方法遍历已注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个新的 KeyPairGenerator 对象，封装了第一个支持指定算法的提供者的
     * KeyPairGeneratorSpi 实现。
     *
     * <p> 可以通过 {@link Security#getProviders() Security.getProviders()} 方法获取已注册提供者的列表。
     *
     * @param algorithm 算法的标准字符串名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#KeyPairGenerator">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a> 中的 KeyPairGenerator 部分。
     *
     * @return 新的 KeyPairGenerator 对象。
     *
     * @exception NoSuchAlgorithmException 如果没有提供者支持指定算法的 KeyPairGeneratorSpi 实现。
     *
     * @see Provider
     */
    public static KeyPairGenerator getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        List<Service> list =
                GetInstance.getServices("KeyPairGenerator", algorithm);
        Iterator<Service> t = list.iterator();
        if (t.hasNext() == false) {
            throw new NoSuchAlgorithmException
                (algorithm + " KeyPairGenerator not available");
        }
        // 查找一个可以工作的 Spi 或 KeyPairGenerator 子类
        NoSuchAlgorithmException failure = null;
        do {
            Service s = t.next();
            try {
                Instance instance =
                    GetInstance.getInstance(s, KeyPairGeneratorSpi.class);
                if (instance.impl instanceof KeyPairGenerator) {
                    return getInstance(instance, algorithm);
                } else {
                    return new Delegate(instance, t, algorithm);
                }
            } catch (NoSuchAlgorithmException e) {
                if (failure == null) {
                    failure = e;
                }
            }
        } while (t.hasNext());
        throw failure;
    }

    /**
     * 返回一个用于生成指定算法的公钥/私钥对的 KeyPairGenerator 对象。
     *
     * <p> 返回一个新的 KeyPairGenerator 对象，封装了指定提供者的
     * KeyPairGeneratorSpi 实现。指定的提供者必须在安全提供者列表中注册。
     *
     * <p> 可以通过 {@link Security#getProviders() Security.getProviders()} 方法获取已注册提供者的列表。
     *
     * @param algorithm 算法的标准字符串名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#KeyPairGenerator">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a> 中的 KeyPairGenerator 部分。
     *
     * @param provider 提供者的字符串名称。
     *
     * @return 新的 KeyPairGenerator 对象。
     *
     * @exception NoSuchAlgorithmException 如果指定提供者不支持指定算法的 KeyPairGeneratorSpi 实现。
     *
     * @exception NoSuchProviderException 如果指定的提供者未在安全提供者列表中注册。
     *
     * @exception IllegalArgumentException 如果提供者名称为空或为空字符串。
     *
     * @see Provider
     */
    public static KeyPairGenerator getInstance(String algorithm,
            String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        Instance instance = GetInstance.getInstance("KeyPairGenerator",
                KeyPairGeneratorSpi.class, algorithm, provider);
        return getInstance(instance, algorithm);
    }

    /**
     * 返回一个用于生成指定算法的公钥/私钥对的 KeyPairGenerator 对象。
     *
     * <p> 返回一个新的 KeyPairGenerator 对象，封装了指定 Provider 对象的
     * KeyPairGeneratorSpi 实现。注意，指定的 Provider 对象不需要在提供者列表中注册。
     *
     * @param algorithm 算法的标准字符串名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#KeyPairGenerator">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a> 中的 KeyPairGenerator 部分。
     *
     * @param provider 提供者。
     *
     * @return 新的 KeyPairGenerator 对象。
     *
     * @exception NoSuchAlgorithmException 如果指定 Provider 对象不支持指定算法的 KeyPairGeneratorSpi 实现。
     *
     * @exception IllegalArgumentException 如果指定的提供者为 null。
     *
     * @see Provider
     *
     * @since 1.4
     */
    public static KeyPairGenerator getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("KeyPairGenerator",
                KeyPairGeneratorSpi.class, algorithm, provider);
        return getInstance(instance, algorithm);
    }


                /**
     * 返回此密钥对生成器对象的提供者。
     *
     * @return 此密钥对生成器对象的提供者
     */
    public final Provider getProvider() {
        disableFailover();
        return this.provider;
    }

    void disableFailover() {
        // 空，由 Delegate 覆盖
    }

    /**
     * 使用默认参数集和最高优先级已安装提供者的 {@code SecureRandom}
     * 实现作为随机性来源，初始化密钥对生成器以生成特定密钥大小的密钥。
     * （如果已安装的提供者中没有提供 {@code SecureRandom} 的实现，则使用系统提供的随机性来源。）
     *
     * @param keysize 密钥大小。这是一个算法特定的度量，例如以位数指定的模长。
     *
     * @exception InvalidParameterException 如果此 KeyPairGenerator 对象不支持 {@code keysize}。
     */
    public void initialize(int keysize) {
        initialize(keysize, JCAUtil.getSecureRandom());
    }

    /**
     * 使用给定的随机性来源（和默认参数集）初始化密钥对生成器以生成特定密钥大小的密钥。
     *
     * @param keysize 密钥大小。这是一个算法特定的度量，例如以位数指定的模长。
     * @param random 随机性来源。
     *
     * @exception InvalidParameterException 如果此 KeyPairGenerator 对象不支持 {@code keysize}。
     *
     * @since 1.2
     */
    public void initialize(int keysize, SecureRandom random) {
        // 这里不执行任何操作，因为：
        // 1. 由 getInstance() 返回的实现对象是 KeyPairGenerator 的实例，它有自己的
        //    initialize(keysize, random) 方法，因此应用程序会直接调用该方法，或者
        // 2. 由 getInstance() 返回的实现是 Delegate 的实例，在这种情况下，initialize(keysize, random)
        //    被覆盖以调用相应的 SPI 方法。
        // （这是一个特殊情况，因为 API 和 SPI 方法名称相同。）
    }

    /**
     * 使用指定的参数集和最高优先级已安装提供者的 {@code SecureRandom}
     * 实现作为随机性来源，初始化密钥对生成器。
     * （如果已安装的提供者中没有提供 {@code SecureRandom} 的实现，则使用系统提供的随机性来源。）
     *
     * <p>此具体方法已添加到先前定义的抽象类中。
     * 此方法调用 KeyPairGeneratorSpi 的
     * {@link KeyPairGeneratorSpi#initialize(
     * java.security.spec.AlgorithmParameterSpec,
     * java.security.SecureRandom) initialize} 方法，
     * 传递给它 {@code params} 和随机性来源（从最高优先级已安装提供者或系统提供的来源获取，如果已安装的提供者中没有提供的话）。
     * 如果该 {@code initialize} 方法未被提供者覆盖，则始终抛出
     * UnsupportedOperationException。
     *
     * @param params 用于生成密钥的参数集。
     *
     * @exception InvalidAlgorithmParameterException 如果给定的参数不适合此密钥对生成器。
     *
     * @since 1.2
     */
    public void initialize(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
        initialize(params, JCAUtil.getSecureRandom());
    }

    /**
     * 使用给定的参数集和随机性来源初始化密钥对生成器。
     *
     * <p>此具体方法已添加到先前定义的抽象类中。
     * 此方法调用 KeyPairGeneratorSpi 的 {@link
     * KeyPairGeneratorSpi#initialize(
     * java.security.spec.AlgorithmParameterSpec,
     * java.security.SecureRandom) initialize} 方法，
     * 传递给它 {@code params} 和 {@code random}。
     * 如果该 {@code initialize}
     * 方法未被提供者覆盖，则始终抛出
     * UnsupportedOperationException。
     *
     * @param params 用于生成密钥的参数集。
     * @param random 随机性来源。
     *
     * @exception InvalidAlgorithmParameterException 如果给定的参数不适合此密钥对生成器。
     *
     * @since 1.2
     */
    public void initialize(AlgorithmParameterSpec params,
                           SecureRandom random)
        throws InvalidAlgorithmParameterException
    {
        // 这里不执行任何操作，因为：
        // 1. 由 getInstance() 返回的实现对象是 KeyPairGenerator 的实例，它有自己的
        //    initialize(params, random) 方法，因此应用程序会直接调用该方法，或者
        // 2. 由 getInstance() 返回的实现是 Delegate 的实例，在这种情况下，initialize(params, random)
        //    被覆盖以调用相应的 SPI 方法。
        // （这是一个特殊情况，因为 API 和 SPI 方法名称相同。）
    }

    /**
     * 生成一个密钥对。
     *
     * <p>如果此 KeyPairGenerator 未显式初始化，则将使用提供者特定的默认值来确定生成的密钥的大小和其他
     * （算法特定的）值。
     *
     * <p>每次调用此方法时都会生成一个新的密钥对。
     *
     * <p>此方法在功能上等同于
     * {@link #generateKeyPair() generateKeyPair}。
     *
     * @return 生成的密钥对
     *
     * @since 1.2
     */
    public final KeyPair genKeyPair() {
        return generateKeyPair();
    }

    /**
     * 生成一个密钥对。
     *
     * <p>如果此 KeyPairGenerator 未显式初始化，则将使用提供者特定的默认值来确定生成的密钥的大小和其他
     * （算法特定的）值。
     *
     * <p>每次调用此方法时都会生成一个新的密钥对。
     *
     * <p>此方法在功能上等同于
     * {@link #genKeyPair() genKeyPair}。
     *
     * @return 生成的密钥对
     */
    public KeyPair generateKeyPair() {
        // 这里不执行任何操作（除了返回 null），因为：
        //
        // 1. 由 getInstance() 返回的实现对象是 KeyPairGenerator 的实例，它有自己的 generateKeyPair 实现
        //    （覆盖了这个方法），因此应用程序会直接调用该方法，或者
        //
        // 2. 由 getInstance() 返回的实现是 Delegate 的实例，在这种情况下，generateKeyPair
        //    被覆盖以调用相应的 SPI 方法。
        //
        // （这是一个特殊情况，因为在 JDK 1.1.x 中，generateKeyPair
        // 方法同时用作 API 和 SPI 方法。）
        return null;
    }

    /*
     * 以下类允许提供者从 KeyPairGeneratorSpi 而不是 KeyPairGenerator 继承。
     * 它表示一个封装了提供者提供的 SPI 对象（类型为 KeyPairGeneratorSpi）的 KeyPairGenerator。
     * 如果提供者的实现是 KeyPairGeneratorSpi 的实例，那么上面的 getInstance() 方法将返回
     * 该类的一个实例，其中封装了 SPI 对象。
     *
     * 注意：原始 KeyPairGenerator 类中的所有 SPI 方法都已移至层次结构中的新类（KeyPairGeneratorSpi），
     * 该类位于 API（KeyPairGenerator）和其原始父类（Object）之间。
     */

    //
    // 错误故障转移说明：
    //
    //  . 如果实现者在初始化过程中抛出错误，我们通过在其他提供者上重试初始化来故障转移
    //
    //  . 如果初始化成功，但随后调用 generateKeyPair() 失败，我们也会故障转移。为了使这有效，
    //    我们需要记住上次成功调用 init 的参数，并使用这些参数初始化下一个 spi。
    //
    //  . 虽然没有明确规定，但 KeyPairGenerators 可能是线程安全的，因此我们确保不会干扰这一点
    //
    //  . 如果满足以下条件，故障转移不可用：
    //    . 使用 getInstance(algorithm, provider)
    //    . 提供者扩展 KeyPairGenerator 而不是 KeyPairGeneratorSpi（JDK 1.1 风格）
    //    . 一旦调用 getProvider()
    //

    private static final class Delegate extends KeyPairGenerator {

        // 提供者实现（代理）
        private volatile KeyPairGeneratorSpi spi;

        private final Object lock = new Object();

        private Iterator<Service> serviceIterator;

        private final static int I_NONE   = 1;
        private final static int I_SIZE   = 2;
        private final static int I_PARAMS = 3;

        private int initType;
        private int initKeySize;
        private AlgorithmParameterSpec initParams;
        private SecureRandom initRandom;

        // 构造函数
        Delegate(KeyPairGeneratorSpi spi, String algorithm) {
            super(algorithm);
            this.spi = spi;
        }

        Delegate(Instance instance, Iterator<Service> serviceIterator,
                String algorithm) {
            super(algorithm);
            spi = (KeyPairGeneratorSpi)instance.impl;
            provider = instance.provider;
            this.serviceIterator = serviceIterator;
            initType = I_NONE;

            if (!skipDebug && pdebug != null) {
                pdebug.println("KeyPairGenerator." + algorithm +
                    " algorithm from: " + provider.getName());
            }
        }

        /**
         * 更新此类的活动 spi 并返回下一个故障转移实现。如果没有更多实现可用，此方法返回 null。
         * 但是，此类的活动 spi 从不设置为 null。
         */
        private KeyPairGeneratorSpi nextSpi(KeyPairGeneratorSpi oldSpi,
                boolean reinit) {
            synchronized (lock) {
                // 有人同时进行了故障转移
                // 现在尝试该 spi
                if ((oldSpi != null) && (oldSpi != spi)) {
                    return spi;
                }
                if (serviceIterator == null) {
                    return null;
                }
                while (serviceIterator.hasNext()) {
                    Service s = serviceIterator.next();
                    try {
                        Object inst = s.newInstance(null);
                        // 忽略非 spi
                        if (inst instanceof KeyPairGeneratorSpi == false) {
                            continue;
                        }
                        if (inst instanceof KeyPairGenerator) {
                            continue;
                        }
                        KeyPairGeneratorSpi spi = (KeyPairGeneratorSpi)inst;
                        if (reinit) {
                            if (initType == I_SIZE) {
                                spi.initialize(initKeySize, initRandom);
                            } else if (initType == I_PARAMS) {
                                spi.initialize(initParams, initRandom);
                            } else if (initType != I_NONE) {
                                throw new AssertionError
                                    ("KeyPairGenerator initType: " + initType);
                            }
                        }
                        provider = s.getProvider();
                        this.spi = spi;
                        return spi;
                    } catch (Exception e) {
                        // 忽略
                    }
                }
                disableFailover();
                return null;
            }
        }

        void disableFailover() {
            serviceIterator = null;
            initType = 0;
            initParams = null;
            initRandom = null;
        }

        // 引擎方法
        public void initialize(int keysize, SecureRandom random) {
            if (serviceIterator == null) {
                spi.initialize(keysize, random);
                return;
            }
            RuntimeException failure = null;
            KeyPairGeneratorSpi mySpi = spi;
            do {
                try {
                    mySpi.initialize(keysize, random);
                    initType = I_SIZE;
                    initKeySize = keysize;
                    initParams = null;
                    initRandom = random;
                    return;
                } catch (RuntimeException e) {
                    if (failure == null) {
                        failure = e;
                    }
                    mySpi = nextSpi(mySpi, false);
                }
            } while (mySpi != null);
            throw failure;
        }

        // 引擎方法
        public void initialize(AlgorithmParameterSpec params,
                SecureRandom random) throws InvalidAlgorithmParameterException {
            if (serviceIterator == null) {
                spi.initialize(params, random);
                return;
            }
            Exception failure = null;
            KeyPairGeneratorSpi mySpi = spi;
            do {
                try {
                    mySpi.initialize(params, random);
                    initType = I_PARAMS;
                    initKeySize = 0;
                    initParams = params;
                    initRandom = random;
                    return;
                } catch (Exception e) {
                    if (failure == null) {
                        failure = e;
                    }
                    mySpi = nextSpi(mySpi, false);
                }
            } while (mySpi != null);
            if (failure instanceof RuntimeException) {
                throw (RuntimeException)failure;
            }
            // 必须是 InvalidAlgorithmParameterException
            throw (InvalidAlgorithmParameterException)failure;
        }

}


                    // 引擎方法
        public KeyPair generateKeyPair() {
            if (serviceIterator == null) {
                return spi.generateKeyPair();
            }
            RuntimeException failure = null;
            KeyPairGeneratorSpi mySpi = spi;
            do {
                try {
                    return mySpi.generateKeyPair();
                } catch (RuntimeException e) {
                    if (failure == null) {
                        failure = e;
                    }
                    mySpi = nextSpi(mySpi, true);
                }
            } while (mySpi != null);
            throw failure;
        }
    }

}
