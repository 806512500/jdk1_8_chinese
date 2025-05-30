
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

import java.util.*;

import java.security.Provider.Service;
import java.security.spec.KeySpec;
import java.security.spec.InvalidKeySpecException;

import sun.security.util.Debug;
import sun.security.jca.*;
import sun.security.jca.GetInstance.Instance;

/**
 * 密钥工厂用于将 <I>密钥</I>（不透明的加密密钥，类型为 {@code Key}）转换为 <I>密钥规范</I>
 * （透明的底层密钥材料表示），反之亦然。
 *
 * <P> 密钥工厂是双向的。也就是说，它们允许你从给定的密钥规范（密钥材料）构建一个不透明的密钥对象，
 * 或者从密钥对象中检索底层密钥材料，以合适的格式返回。
 *
 * <P> 同一个密钥可能存在多个兼容的密钥规范。例如，DSA 公钥可以使用
 * {@code DSAPublicKeySpec} 或
 * {@code X509EncodedKeySpec} 来指定。密钥工厂可以用于在兼容的密钥规范之间进行转换。
 *
 * <P> 以下是一个如何使用密钥工厂实例化 DSA 公钥的示例。假设 Alice 收到了 Bob 的数字签名。
 * Bob 还发送了他自己的公钥（编码格式）以验证他的签名。Alice 然后执行以下操作：
 *
 * <pre>
 * X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(bobEncodedPubKey);
 * KeyFactory keyFactory = KeyFactory.getInstance("DSA");
 * PublicKey bobPubKey = keyFactory.generatePublic(bobPubKeySpec);
 * Signature sig = Signature.getInstance("DSA");
 * sig.initVerify(bobPubKey);
 * sig.update(data);
 * sig.verify(signature);
 * </pre>
 *
 * <p> 每个 Java 平台的实现都必须支持以下标准的 {@code KeyFactory} 算法：
 * <ul>
 * <li>{@code DiffieHellman}</li>
 * <li>{@code DSA}</li>
 * <li>{@code RSA}</li>
 * </ul>
 * 这些算法在 <a href=
 * "{@docRoot}/../technotes/guides/security/StandardNames.html#KeyFactory">
 * Java 密码架构标准算法名称文档的 KeyFactory 部分</a> 中有描述。
 * 请参阅你的实现的发行文档，以了解是否支持其他算法。
 *
 * @author Jan Luehe
 *
 * @see Key
 * @see PublicKey
 * @see PrivateKey
 * @see java.security.spec.KeySpec
 * @see java.security.spec.DSAPublicKeySpec
 * @see java.security.spec.X509EncodedKeySpec
 *
 * @since 1.2
 */

public class KeyFactory {

    private static final Debug debug =
                        Debug.getInstance("jca", "KeyFactory");

    // 与此密钥工厂关联的算法
    private final String algorithm;

    // 提供者
    private Provider provider;

    // 提供者实现（委托）
    private volatile KeyFactorySpi spi;

    // 用于在提供者选择期间互斥的锁
    private final Object lock = new Object();

    // 在提供者选择期间剩余的服务
    // 一旦选择提供者后为 null
    private Iterator<Service> serviceIterator;

    /**
     * 创建一个 KeyFactory 对象。
     *
     * @param keyFacSpi 委托
     * @param provider 提供者
     * @param algorithm 要与此 {@code KeyFactory} 关联的算法名称
     */
    protected KeyFactory(KeyFactorySpi keyFacSpi, Provider provider,
                         String algorithm) {
        this.spi = keyFacSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    private KeyFactory(String algorithm) throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
        List<Service> list = GetInstance.getServices("KeyFactory", algorithm);
        serviceIterator = list.iterator();
        // 获取并实例化初始 spi
        if (nextSpi(null) == null) {
            throw new NoSuchAlgorithmException
                (algorithm + " KeyFactory not available");
        }
    }

    /**
     * 返回一个转换指定算法的公钥/私钥的 KeyFactory 对象。
     *
     * <p> 该方法遍历注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个新的 KeyFactory 对象，封装了支持指定算法的第一个
     * 提供者的 KeyFactorySpi 实现。
     *
     * <p> 可以通过 {@link Security#getProviders() Security.getProviders()} 方法
     * 获取注册提供者的列表。
     *
     * @param algorithm 请求的密钥算法的名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#KeyFactory">
     * Java 密码架构标准算法名称文档的 KeyFactory 部分</a>。
     *
     * @return 新的 KeyFactory 对象。
     *
     * @exception NoSuchAlgorithmException 如果没有提供者支持指定算法的
     *          KeyFactorySpi 实现。
     *
     * @see Provider
     */
    public static KeyFactory getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        return new KeyFactory(algorithm);
    }

    /**
     * 返回一个转换指定算法的公钥/私钥的 KeyFactory 对象。
     *
     * <p> 返回一个新的 KeyFactory 对象，封装了指定提供者的
     * KeyFactorySpi 实现。指定的提供者必须在安全提供者列表中注册。
     *
     * <p> 可以通过 {@link Security#getProviders() Security.getProviders()} 方法
     * 获取注册提供者的列表。
     *
     * @param algorithm 请求的密钥算法的名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#KeyFactory">
     * Java 密码架构标准算法名称文档的 KeyFactory 部分</a>。
     *
     * @param provider 提供者的名称。
     *
     * @return 新的 KeyFactory 对象。
     *
     * @exception NoSuchAlgorithmException 如果指定提供者不支持指定算法的
     *          KeyFactorySpi 实现。
     *
     * @exception NoSuchProviderException 如果指定的提供者未在安全提供者列表中注册。
     *
     * @exception IllegalArgumentException 如果提供者名称为空或为 null。
     *
     * @see Provider
     */
    public static KeyFactory getInstance(String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        Instance instance = GetInstance.getInstance("KeyFactory",
            KeyFactorySpi.class, algorithm, provider);
        return new KeyFactory((KeyFactorySpi)instance.impl,
            instance.provider, algorithm);
    }

    /**
     * 返回一个转换指定算法的公钥/私钥的 KeyFactory 对象。
     *
     * <p> 返回一个新的 KeyFactory 对象，封装了指定 Provider 对象的
     * KeyFactorySpi 实现。注意，指定的 Provider 对象不一定要在提供者列表中注册。
     *
     * @param algorithm 请求的密钥算法的名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#KeyFactory">
     * Java 密码架构标准算法名称文档的 KeyFactory 部分</a>。
     *
     * @param provider 提供者。
     *
     * @return 新的 KeyFactory 对象。
     *
     * @exception NoSuchAlgorithmException 如果指定 Provider 对象不支持指定算法的
     *          KeyFactorySpi 实现。
     *
     * @exception IllegalArgumentException 如果指定的提供者为 null。
     *
     * @see Provider
     *
     * @since 1.4
     */
    public static KeyFactory getInstance(String algorithm, Provider provider)
            throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("KeyFactory",
            KeyFactorySpi.class, algorithm, provider);
        return new KeyFactory((KeyFactorySpi)instance.impl,
            instance.provider, algorithm);
    }

    /**
     * 返回此密钥工厂对象的提供者。
     *
     * @return 此密钥工厂对象的提供者
     */
    public final Provider getProvider() {
        synchronized (lock) {
            // 在此调用后禁用进一步的故障转移
            serviceIterator = null;
            return provider;
        }
    }

    /**
     * 获取与此 {@code KeyFactory} 关联的算法名称。
     *
     * @return 与此 {@code KeyFactory} 关联的算法名称
     */
    public final String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * 更新此类的活动 KeyFactorySpi 并返回下一个实现以进行故障转移。如果不再有可用的实现，
     * 此方法返回 null。但是，此类的活动 spi 从不设置为 null。
     */
    private KeyFactorySpi nextSpi(KeyFactorySpi oldSpi) {
        synchronized (lock) {
            // 其他线程并发地进行了故障转移
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
                    Object obj = s.newInstance(null);
                    if (obj instanceof KeyFactorySpi == false) {
                        continue;
                    }
                    KeyFactorySpi spi = (KeyFactorySpi)obj;
                    provider = s.getProvider();
                    this.spi = spi;
                    return spi;
                } catch (NoSuchAlgorithmException e) {
                    // 忽略
                }
            }
            serviceIterator = null;
            return null;
        }
    }

    /**
     * 从提供的密钥规范（密钥材料）生成公钥对象。
     *
     * @param keySpec 公钥的规范（密钥材料）。
     *
     * @return 公钥。
     *
     * @exception InvalidKeySpecException 如果给定的密钥规范不适合此密钥工厂生成公钥。
     */
    public final PublicKey generatePublic(KeySpec keySpec)
            throws InvalidKeySpecException {
        if (serviceIterator == null) {
            return spi.engineGeneratePublic(keySpec);
        }
        Exception failure = null;
        KeyFactorySpi mySpi = spi;
        do {
            try {
                return mySpi.engineGeneratePublic(keySpec);
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi);
            }
        } while (mySpi != null);
        if (failure instanceof RuntimeException) {
            throw (RuntimeException)failure;
        }
        if (failure instanceof InvalidKeySpecException) {
            throw (InvalidKeySpecException)failure;
        }
        throw new InvalidKeySpecException
                ("Could not generate public key", failure);
    }

    /**
     * 从提供的密钥规范（密钥材料）生成私钥对象。
     *
     * @param keySpec 私钥的规范（密钥材料）。
     *
     * @return 私钥。
     *
     * @exception InvalidKeySpecException 如果给定的密钥规范不适合此密钥工厂生成私钥。
     */
    public final PrivateKey generatePrivate(KeySpec keySpec)
            throws InvalidKeySpecException {
        if (serviceIterator == null) {
            return spi.engineGeneratePrivate(keySpec);
        }
        Exception failure = null;
        KeyFactorySpi mySpi = spi;
        do {
            try {
                return mySpi.engineGeneratePrivate(keySpec);
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi);
            }
        } while (mySpi != null);
        if (failure instanceof RuntimeException) {
            throw (RuntimeException)failure;
        }
        if (failure instanceof InvalidKeySpecException) {
            throw (InvalidKeySpecException)failure;
        }
        throw new InvalidKeySpecException
                ("Could not generate private key", failure);
    }

    /**
     * 返回给定密钥对象的规范（密钥材料）。{@code keySpec} 识别应返回密钥材料的规范类。
     * 例如，可以是 {@code DSAPublicKeySpec.class}，以指示密钥材料应返回为
     * {@code DSAPublicKeySpec} 类的实例。
     *
     * @param <T> 要返回的密钥规范类型
     *
     * @param key 密钥。
     *
     * @param keySpec 应返回密钥材料的规范类。
     *
     * @return 请求的规范类的实例中的底层密钥规范（密钥材料）。
     *
     * @exception InvalidKeySpecException 如果请求的密钥规范不适合给定的密钥，或者给定的密钥无法处理
     * （例如，给定的密钥具有未识别的算法或格式）。
     */
    public final <T extends KeySpec> T getKeySpec(Key key, Class<T> keySpec)
            throws InvalidKeySpecException {
        if (serviceIterator == null) {
            return spi.engineGetKeySpec(key, keySpec);
        }
        Exception failure = null;
        KeyFactorySpi mySpi = spi;
        do {
            try {
                return mySpi.engineGetKeySpec(key, keySpec);
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi);
            }
        } while (mySpi != null);
        if (failure instanceof RuntimeException) {
            throw (RuntimeException)failure;
        }
        if (failure instanceof InvalidKeySpecException) {
            throw (InvalidKeySpecException)failure;
        }
        throw new InvalidKeySpecException
                ("Could not get key spec", failure);
    }


                /**
     * 将一个提供者可能未知或不可信的密钥对象转换为与此密钥工厂相对应的密钥对象。
     *
     * @param key 提供者未知或不可信的密钥。
     *
     * @return 转换后的密钥。
     *
     * @exception InvalidKeyException 如果给定的密钥无法被此密钥工厂处理。
     */
    public final Key translateKey(Key key) throws InvalidKeyException {
        if (serviceIterator == null) {
            return spi.engineTranslateKey(key);
        }
        Exception failure = null;
        KeyFactorySpi mySpi = spi;
        do {
            try {
                return mySpi.engineTranslateKey(key);
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi);
            }
        } while (mySpi != null);
        if (failure instanceof RuntimeException) {
            throw (RuntimeException)failure;
        }
        if (failure instanceof InvalidKeyException) {
            throw (InvalidKeyException)failure;
        }
        throw new InvalidKeyException
                ("无法转换密钥", failure);
    }

}
