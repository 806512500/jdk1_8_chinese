
/*
 * Copyright (c) 1996, 2022, Oracle and/or its affiliates. All rights reserved.
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
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import java.nio.ByteBuffer;

import java.security.Provider.Service;

import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;
import sun.misc.JavaSecuritySignatureAccess;
import sun.misc.SharedSecrets;

import sun.security.util.Debug;
import sun.security.jca.*;
import sun.security.jca.GetInstance.Instance;

/**
 * Signature 类用于为应用程序提供数字签名算法的功能。数字签名用于数字数据的身份验证和完整性保证。
 *
 * <p> 签名算法可以是 NIST 标准 DSA，使用 DSA 和 SHA-256。使用 SHA-256 消息摘要算法的 DSA 算法可以指定为 {@code SHA256withDSA}。
 * 对于 RSA，签名算法可以指定为例如 {@code SHA256withRSA}。必须指定算法名称，因为没有默认值。
 *
 * <p> Signature 对象可以用于生成和验证数字签名。
 *
 * <p> 使用 Signature 对象进行签名或验证签名有三个阶段：<ol>
 *
 * <li>初始化，可以使用
 *
 *     <ul>
 *
 *     <li>公钥，初始化签名以进行验证（参见 {@link #initVerify(PublicKey) initVerify}），或
 *
 *     <li>私钥（可选地使用安全随机数生成器），初始化签名以进行签名
 *     （参见 {@link #initSign(PrivateKey)} 和 {@link #initSign(PrivateKey, SecureRandom)}）。
 *
 *     </ul>
 *
 * <li>更新
 *
 * <p>根据初始化类型，这将更新要签名或验证的字节。参见 {@link #update(byte) update} 方法。
 *
 * <li>对所有已更新的字节进行签名或验证签名。参见 {@link #sign() sign} 方法和 {@link #verify(byte[]) verify} 方法。
 *
 * </ol>
 *
 * <p>请注意，此类是抽象的，并且由于历史原因扩展自 {@code SignatureSpi}。
 * 应用程序开发人员应仅注意在此 {@code Signature} 类中定义的方法；超类中的所有方法都是为希望提供自己的数字签名算法实现的加密服务提供商设计的。
 *
 * <p> 每个 Java 平台的实现都必须支持以下标准 {@code Signature} 算法：
 * <ul>
 * <li>{@code SHA1withDSA}</li>
 * <li>{@code SHA1withRSA}</li>
 * <li>{@code SHA256withRSA}</li>
 * </ul>
 * 这些算法在 <a href=
 * "{@docRoot}/../technotes/guides/security/StandardNames.html#Signature">
 * Java Cryptography Architecture Standard Algorithm Name Documentation</a> 的签名部分中描述。
 * 请参阅您的实现的发行文档，以查看是否支持其他算法。
 *
 * @author Benjamin Renaud
 *
 */

public abstract class Signature extends SignatureSpi {

    static {
        SharedSecrets.setJavaSecuritySignatureAccess(
            new JavaSecuritySignatureAccess() {
                @Override
                public void initVerify(Signature s, PublicKey publicKey,
                        AlgorithmParameterSpec params)
                        throws InvalidKeyException,
                        InvalidAlgorithmParameterException {
                    s.initVerify(publicKey, params);
                }
                @Override
                public void initVerify(Signature s,
                        java.security.cert.Certificate certificate,
                        AlgorithmParameterSpec params)
                        throws InvalidKeyException,
                        InvalidAlgorithmParameterException {
                    s.initVerify(certificate, params);
                }
                @Override
                public void initSign(Signature s, PrivateKey privateKey,
                        AlgorithmParameterSpec params, SecureRandom random)
                        throws InvalidKeyException,
                        InvalidAlgorithmParameterException {
                    s.initSign(privateKey, params, random);
                }
        });
    }

    private static final Debug debug =
                        Debug.getInstance("jca", "Signature");

    private static final Debug pdebug =
                        Debug.getInstance("provider", "Provider");
    private static final boolean skipDebug =
        Debug.isOn("engine=") && !Debug.isOn("signature");

    /*
     * 此签名对象的算法。
     * 此值用于将 OID 映射到特定算法。
     * 映射在 AlgorithmObject.algOID(String algorithm) 中完成
     */
    private String algorithm;

    // 提供者
    Provider provider;

    /**
     * 可能的 {@link #state} 值，表示
     * 此签名对象尚未初始化。
     */
    protected final static int UNINITIALIZED = 0;

    /**
     * 可能的 {@link #state} 值，表示
     * 此签名对象已初始化用于签名。
     */
    protected final static int SIGN = 2;

    /**
     * 可能的 {@link #state} 值，表示
     * 此签名对象已初始化用于验证。
     */
    protected final static int VERIFY = 3;

    /**
     * 此签名对象的当前状态。
     */
    protected int state = UNINITIALIZED;

    /**
     * 创建指定算法的 Signature 对象。
     *
     * @param algorithm 算法的标准字符串名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#Signature">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a> 的签名部分。
     */
    protected Signature(String algorithm) {
        this.algorithm = algorithm;
    }

    // 特殊签名算法的名称
    private final static String RSA_SIGNATURE = "NONEwithRSA";

    // 等效的密码算法名称
    private final static String RSA_CIPHER = "RSA/ECB/PKCS1Padding";

    // 为与 Cipher 兼容而需要查找的所有服务
    private final static List<ServiceId> rsaIds = Arrays.asList(
        new ServiceId[] {
            new ServiceId("Signature", "NONEwithRSA"),
            new ServiceId("Cipher", "RSA/ECB/PKCS1Padding"),
            new ServiceId("Cipher", "RSA/ECB"),
            new ServiceId("Cipher", "RSA//PKCS1Padding"),
            new ServiceId("Cipher", "RSA"),
        }
    );

    /**
     * 返回实现指定签名算法的 Signature 对象。
     *
     * <p> 此方法遍历注册的安全提供者列表，从最优先的提供者开始。
     * 返回一个新的 Signature 对象，封装了支持指定算法的第一个提供者的
     * SignatureSpi 实现。
     *
     * <p> 可以通过 {@link Security#getProviders() Security.getProviders()} 方法检索注册提供者的列表。
     *
     * @param algorithm 请求的算法的标准名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#Signature">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a> 的签名部分。
     *
     * @return 新的 Signature 对象。
     *
     * @exception NoSuchAlgorithmException 如果没有提供者支持指定算法的 Signature 实现。
     *
     * @see Provider
     */
    public static Signature getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        List<Service> list;
        if (algorithm.equalsIgnoreCase(RSA_SIGNATURE)) {
            list = GetInstance.getServices(rsaIds);
        } else {
            list = GetInstance.getServices("Signature", algorithm);
        }
        Iterator<Service> t = list.iterator();
        if (t.hasNext() == false) {
            throw new NoSuchAlgorithmException
                (algorithm + " Signature not available");
        }
        // 尝试服务，直到找到一个 Spi 或一个工作的 Signature 子类
        NoSuchAlgorithmException failure;
        do {
            Service s = t.next();
            if (isSpi(s)) {
                return new Delegate(s, t, algorithm);
            } else {
                // 必须是 Signature 的子类，禁用动态选择
                try {
                    Instance instance =
                        GetInstance.getInstance(s, SignatureSpi.class);
                    return getInstance(instance, algorithm);
                } catch (NoSuchAlgorithmException e) {
                    failure = e;
                }
            }
        } while (t.hasNext());
        throw failure;
    }

    private static Signature getInstance(Instance instance, String algorithm) {
        Signature sig;
        if (instance.impl instanceof Signature) {
            sig = (Signature)instance.impl;
            sig.algorithm = algorithm;
        } else {
            SignatureSpi spi = (SignatureSpi)instance.impl;
            sig = new Delegate(spi, algorithm);
        }
        sig.provider = instance.provider;
        return sig;
    }

    private final static Map<String,Boolean> signatureInfo;

    static {
        signatureInfo = new ConcurrentHashMap<String,Boolean>();
        Boolean TRUE = Boolean.TRUE;
        // 用我们 SignatureSpi 实现的值预初始化
        signatureInfo.put("sun.security.provider.DSA$RawDSA", TRUE);
        signatureInfo.put("sun.security.provider.DSA$SHA1withDSA", TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$MD2withRSA", TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$MD5withRSA", TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA1withRSA", TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA256withRSA", TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA384withRSA", TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA512withRSA", TRUE);
        signatureInfo.put("sun.security.rsa.RSAPSSSignature", TRUE);
        signatureInfo.put("com.sun.net.ssl.internal.ssl.RSASignature", TRUE);
        signatureInfo.put("sun.security.pkcs11.P11Signature", TRUE);
    }

    private static boolean isSpi(Service s) {
        if (s.getType().equals("Cipher")) {
            // 必须是 CipherSpi，我们可以用 CipherAdapter 包装
            return true;
        }
        String className = s.getClassName();
        Boolean result = signatureInfo.get(className);
        if (result == null) {
            try {
                Object instance = s.newInstance(null);
                // Signature 扩展自 SignatureSpi
                // 因此如果它是 SignatureSpi 的实例但不是 Signature 的实例，则它是一个“真正的”Spi
                boolean r = (instance instanceof SignatureSpi)
                                && (instance instanceof Signature == false);
                if ((debug != null) && (r == false)) {
                    debug.println("Not a SignatureSpi " + className);
                    debug.println("Delayed provider selection may not be "
                        + "available for algorithm " + s.getAlgorithm());
                }
                result = Boolean.valueOf(r);
                signatureInfo.put(className, result);
            } catch (Exception e) {
                // 出现错误，假设不是 SPI
                return false;
            }
        }
        return result.booleanValue();
    }

    /**
     * 返回实现指定签名算法的 Signature 对象。
     *
     * <p> 返回一个新的 Signature 对象，封装了指定提供者的
     * SignatureSpi 实现。指定的提供者必须在安全提供者列表中注册。
     *
     * <p> 可以通过 {@link Security#getProviders() Security.getProviders()} 方法检索注册提供者的列表。
     *
     * @param algorithm 请求的算法名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#Signature">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a> 的签名部分。
     *
     * @param provider 提供者的名称。
     *
     * @return 新的 Signature 对象。
     *
     * @exception NoSuchAlgorithmException 如果指定提供者没有提供指定算法的 SignatureSpi 实现。
     *
     * @exception NoSuchProviderException 如果指定的提供者未在安全提供者列表中注册。
     *
     * @exception IllegalArgumentException 如果提供者名称为空或为空字符串。
     *
     * @see Provider
     */
    public static Signature getInstance(String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if (algorithm.equalsIgnoreCase(RSA_SIGNATURE)) {
            // 与现有代码的异常兼容性
            if ((provider == null) || (provider.length() == 0)) {
                throw new IllegalArgumentException("missing provider");
            }
            Provider p = Security.getProvider(provider);
            if (p == null) {
                throw new NoSuchProviderException
                    ("no such provider: " + provider);
            }
            return getInstanceRSA(p);
        }
        Instance instance = GetInstance.getInstance
                ("Signature", SignatureSpi.class, algorithm, provider);
        return getInstance(instance, algorithm);
    }


                /**
     * 返回实现指定签名算法的 Signature 对象。
     *
     * <p> 返回一个新的 Signature 对象，封装了从指定 Provider 对象获取的
     * SignatureSpi 实现。请注意，指定的 Provider 对象不必注册在提供程序列表中。
     *
     * @param algorithm 请求的算法名称。
     * 有关标准算法名称的信息，请参阅 <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#Signature">
     * Java 密码体系结构标准算法名称文档</a> 中的 Signature 部分。
     *
     * @param provider 提供程序。
     *
     * @return 新的 Signature 对象。
     *
     * @exception NoSuchAlgorithmException 如果从指定的 Provider 对象中无法获取
     *          指定算法的 SignatureSpi 实现。
     *
     * @exception IllegalArgumentException 如果提供程序为 null。
     *
     * @see Provider
     *
     * @since 1.4
     */
    public static Signature getInstance(String algorithm, Provider provider)
            throws NoSuchAlgorithmException {
        if (algorithm.equalsIgnoreCase(RSA_SIGNATURE)) {
            // 与现有代码的异常兼容性
            if (provider == null) {
                throw new IllegalArgumentException("缺少提供程序");
            }
            return getInstanceRSA(provider);
        }
        Instance instance = GetInstance.getInstance
                ("Signature", SignatureSpi.class, algorithm, provider);
        return getInstance(instance, algorithm);
    }

    // 返回 NONEwithRSA 的实现，这是一个特殊情况
    // 因为 Cipher.RSA/ECB/PKCS1Padding 兼容性包装器
    private static Signature getInstanceRSA(Provider p)
            throws NoSuchAlgorithmException {
        // 首先尝试 Signature
        Service s = p.getService("Signature", RSA_SIGNATURE);
        if (s != null) {
            Instance instance = GetInstance.getInstance(s, SignatureSpi.class);
            return getInstance(instance, RSA_SIGNATURE);
        }
        // 检查 Cipher
        try {
            Cipher c = Cipher.getInstance(RSA_CIPHER, p);
            return new Delegate(new CipherAdapter(c), RSA_SIGNATURE);
        } catch (GeneralSecurityException e) {
            // 为了避免混淆，抛出 Signature 风格的异常信息，
            // 但将 Cipher 异常作为原因附加
            throw new NoSuchAlgorithmException("没有这样的算法: "
                + RSA_SIGNATURE + " 对于提供程序 " + p.getName(), e);
        }
    }

    /**
     * 返回此签名对象的提供程序。
     *
     * @return 此签名对象的提供程序
     */
    public final Provider getProvider() {
        chooseFirstProvider();
        return this.provider;
    }

    private String getProviderName() {
        return (provider == null)  ? "(无提供程序)" : provider.getName();
    }

    void chooseFirstProvider() {
        // 空，由 Delegate 覆盖
    }

    /**
     * 初始化此对象以进行验证。如果此方法再次被调用且参数不同，则会抵消此次调用的效果。
     *
     * @param publicKey 要验证其签名的身份的公钥。
     *
     * @exception InvalidKeyException 如果密钥无效。
     */
    public final void initVerify(PublicKey publicKey)
            throws InvalidKeyException {
        engineInitVerify(publicKey);
        state = VERIFY;

        if (!skipDebug && pdebug != null) {
            pdebug.println("Signature." + algorithm +
                " 验证算法来自: " + getProviderName());
        }
    }

    /**
     * 初始化此对象以进行验证。如果此方法再次被调用且参数不同，则会抵消此次调用的效果。
     *
     * @param publicKey 要验证其签名的身份的公钥。
     * @param params 用于验证此签名的参数。
     *
     * @exception InvalidKeyException 如果密钥无效。
     * @exception InvalidAlgorithmParameterException 如果参数无效。
     */
    final void initVerify(PublicKey publicKey, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        engineInitVerify(publicKey, params);
        state = VERIFY;

        if (!skipDebug && pdebug != null) {
            pdebug.println("Signature." + algorithm +
                " 验证算法来自: " + getProviderName());
        }
    }

    private static PublicKey getPublicKeyFromCert(Certificate cert)
            throws InvalidKeyException {
        // 如果证书是 X509Certificate 类型，
        // 我们应该检查它是否有一个标记为关键的 Key Usage 扩展。
        //if (cert instanceof java.security.cert.X509Certificate) {
        if (cert instanceof X509Certificate) {
            // 检查证书是否有一个标记为关键扩展的 Key Usage 扩展。
            // KeyUsage 扩展的 OID 是 2.5.29.15。
            X509Certificate c = (X509Certificate)cert;
            Set<String> critSet = c.getCriticalExtensionOIDs();

            if (critSet != null && !critSet.isEmpty()
                && critSet.contains("2.5.29.15")) {
                boolean[] keyUsageInfo = c.getKeyUsage();
                // keyUsageInfo[0] 用于数字签名。
                if ((keyUsageInfo != null) && (keyUsageInfo[0] == false))
                    throw new InvalidKeyException("错误的密钥用途");
            }
        }
        return cert.getPublicKey();
    }

    /**
     * 使用给定证书中的公钥初始化此对象以进行验证。
     * <p>如果证书是 X.509 类型且具有标记为关键的 <i>密钥用途</i>
     * 扩展字段，且 <i>密钥用途</i> 扩展字段的值表明证书中的公钥及其对应的私钥
     * 不应用于数字签名，则抛出 {@code InvalidKeyException}。
     *
     * @param certificate 要验证其签名的身份的证书。
     *
     * @exception InvalidKeyException  如果证书中的公钥编码不正确或不包含所需的参数信息
     * 或不能用于数字签名。
     * @since 1.3
     */
    public final void initVerify(Certificate certificate)
            throws InvalidKeyException {
        engineInitVerify(getPublicKeyFromCert(certificate));
        state = VERIFY;

        if (!skipDebug && pdebug != null) {
            pdebug.println("Signature." + algorithm +
                " 验证算法来自: " + getProviderName());
        }
    }

    /**
     * 使用给定证书中的公钥初始化此对象以进行验证。
     * <p>如果证书是 X.509 类型且具有标记为关键的 <i>密钥用途</i>
     * 扩展字段，且 <i>密钥用途</i> 扩展字段的值表明证书中的公钥及其对应的私钥
     * 不应用于数字签名，则抛出 {@code InvalidKeyException}。
     *
     * @param certificate 要验证其签名的身份的证书。
     * @param params 用于验证此签名的参数。
     *
     * @exception InvalidKeyException  如果证书中的公钥编码不正确或不包含所需的参数信息
     * 或不能用于数字签名。
     * @exception InvalidAlgorithmParameterException 如果参数无效。
     */
    final void initVerify(Certificate certificate,
            AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        engineInitVerify(getPublicKeyFromCert(certificate), params);
        state = VERIFY;

        if (!skipDebug && pdebug != null) {
            pdebug.println("Signature." + algorithm +
                " 验证算法来自: " + getProviderName());
        }
    }

    /**
     * 初始化此对象以进行签名。如果此方法再次被调用且参数不同，则会抵消此次调用的效果。
     *
     * @param privateKey 要生成其签名的身份的私钥。
     *
     * @exception InvalidKeyException 如果密钥无效。
     */
    public final void initSign(PrivateKey privateKey)
            throws InvalidKeyException {
        engineInitSign(privateKey);
        state = SIGN;

        if (!skipDebug && pdebug != null) {
            pdebug.println("Signature." + algorithm +
                " 签名算法来自: " + getProviderName());
        }
    }

    /**
     * 初始化此对象以进行签名。如果此方法再次被调用且参数不同，则会抵消此次调用的效果。
     *
     * @param privateKey 要生成其签名的身份的私钥。
     *
     * @param random 此签名的随机性来源。
     *
     * @exception InvalidKeyException 如果密钥无效。
     */
    public final void initSign(PrivateKey privateKey, SecureRandom random)
            throws InvalidKeyException {
        engineInitSign(privateKey, random);
        state = SIGN;

        if (!skipDebug && pdebug != null) {
            pdebug.println("Signature." + algorithm +
                " 签名算法来自: " + getProviderName());
        }
    }

    /**
     * 初始化此对象以进行签名。如果此方法再次被调用且参数不同，则会抵消此次调用的效果。
     *
     * @param privateKey 要生成其签名的身份的私钥。
     * @param params 用于生成签名的参数。
     * @param random 此签名的随机性来源。
     *
     * @exception InvalidKeyException 如果密钥无效。
     * @exception InvalidAlgorithmParameterException 如果参数无效
     */
    final void initSign(PrivateKey privateKey,
            AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        engineInitSign(privateKey, params, random);
        state = SIGN;

        if (!skipDebug && pdebug != null) {
            pdebug.println("Signature." + algorithm +
                " 签名算法来自: " + getProviderName());
        }
    }

    /**
     * 返回所有已更新数据的签名字节。签名的格式取决于底层签名方案。
     *
     * <p>调用此方法会将此签名对象重置为之前通过调用 {@code initSign(PrivateKey)} 初始化签名时的状态。
     * 即，对象被重置，如果需要，可以使用相同的签名者通过新的 {@code update} 和 {@code sign} 调用生成另一个签名。
     *
     * @return 签名操作结果的签名字节。
     *
     * @exception SignatureException 如果此签名对象未正确初始化或此签名算法无法处理提供的输入数据。
     */
    public final byte[] sign() throws SignatureException {
        if (state == SIGN) {
            return engineSign();
        }
        throw new SignatureException("对象未初始化用于签名");
    }

    /**
     * 完成签名操作并将结果签名字节存储在提供的缓冲区 {@code outbuf} 中，从 {@code offset} 开始。
     * 签名的格式取决于底层签名方案。
     *
     * <p>此签名对象被重置为其初始状态（调用 {@code initSign} 方法后的状态），并可以使用相同的私钥重新生成签名。
     *
     * @param outbuf 签名结果的缓冲区。
     *
     * @param offset {@code outbuf} 中存储签名的起始位置。
     *
     * @param len {@code outbuf} 中分配给签名的字节数。
     *
     * @return 放入 {@code outbuf} 的字节数。
     *
     * @exception SignatureException 如果此签名对象未正确初始化，此签名算法无法处理提供的输入数据，或 {@code len} 小于实际签名长度。
     *
     * @since 1.2
     */
    public final int sign(byte[] outbuf, int offset, int len)
        throws SignatureException {
        if (outbuf == null) {
            throw new IllegalArgumentException("未提供输出缓冲区");
        }
        if (offset < 0 || len < 0) {
            throw new IllegalArgumentException("offset 或 len 小于 0");
        }
        if (outbuf.length - offset < len) {
            throw new IllegalArgumentException
                ("指定的 offset 和 length 下输出缓冲区太小");
        }
        if (state != SIGN) {
            throw new SignatureException("对象未初始化用于签名");
        }
        return engineSign(outbuf, offset, len);
    }

    /**
     * 验证传入的签名。
     *
     * <p>调用此方法会将此签名对象重置为之前通过调用 {@code initVerify(PublicKey)} 初始化验证时的状态。
     * 即，对象被重置，可以验证使用在 {@code initVerify} 调用中指定的公钥的身份的另一个签名。
     *
     * @param signature 要验证的签名字节。
     *
     * @return 如果签名被验证成功，则返回 true，否则返回 false。
     *
     * @exception SignatureException 如果此签名对象未正确初始化，传入的签名编码不正确或类型错误，或此签名算法无法处理提供的输入数据等。
     */
    public final boolean verify(byte[] signature) throws SignatureException {
        if (state == VERIFY) {
            return engineVerify(signature);
        }
        throw new SignatureException("对象未初始化用于验证");
    }

    /**
     * 验证指定字节数组中从指定偏移量开始的传入签名。
     *
     * <p>调用此方法会将此签名对象重置为之前通过调用 {@code initVerify(PublicKey)} 初始化验证时的状态。
     * 即，对象被重置，可以验证使用在 {@code initVerify} 调用中指定的公钥的身份的另一个签名。
     *
     * @param signature 要验证的签名字节。
     * @param offset 要开始的字节数组中的偏移量。
     * @param length 从偏移量开始使用的字节数。
     *
     * @return 如果签名被验证成功，则返回 true，否则返回 false。
     *
     * @exception SignatureException 如果此签名对象未正确初始化，传入的签名编码不正确或类型错误，或此签名算法无法处理提供的输入数据等。
     * @exception IllegalArgumentException 如果 {@code signature} 字节数组为 {@code null}，或 {@code offset} 或 {@code length} 小于 0，或 {@code offset} 和
     * {@code length} 的和大于 {@code signature} 字节数组的长度。
     * @since 1.4
     */
    public final boolean verify(byte[] signature, int offset, int length)
        throws SignatureException {
        if (state == VERIFY) {
            if (signature == null) {
                throw new IllegalArgumentException("signature 为 null");
            }
            if (offset < 0 || length < 0) {
                throw new IllegalArgumentException
                    ("offset 或 length 小于 0");
            }
            if (signature.length - offset < length) {
                throw new IllegalArgumentException
                    ("signature 对于指定的 offset 和 length 太小");
            }


    /**
     * 使用一个字节更新要签名或验证的数据。
     *
     * @param b 用于更新的字节。
     *
     * @exception SignatureException 如果此签名对象未正确初始化。
     */
    public final void update(byte b) throws SignatureException {
        if (state == VERIFY || state == SIGN) {
            engineUpdate(b);
        } else {
            throw new SignatureException("对象未初始化用于签名或验证");
        }
    }

    /**
     * 使用指定的字节数组更新要签名或验证的数据。
     *
     * @param data 用于更新的字节数组。
     *
     * @exception SignatureException 如果此签名对象未正确初始化。
     */
    public final void update(byte[] data) throws SignatureException {
        update(data, 0, data.length);
    }

    /**
     * 使用指定的字节数组，从指定的偏移量开始更新要签名或验证的数据。
     *
     * @param data 字节数组。
     * @param off 在字节数组中开始的偏移量。
     * @param len 从偏移量开始使用的字节数。
     *
     * @exception SignatureException 如果此签名对象未正确初始化。
     */
    public final void update(byte[] data, int off, int len)
            throws SignatureException {
        if (state == SIGN || state == VERIFY) {
            if (data == null) {
                throw new IllegalArgumentException("data is null");
            }
            if (off < 0 || len < 0) {
                throw new IllegalArgumentException("off or len is less than 0");
            }
            if (data.length - off < len) {
                throw new IllegalArgumentException
                    ("data too small for specified offset and length");
            }
            engineUpdate(data, off, len);
        } else {
            throw new SignatureException("对象未初始化用于签名或验证");
        }
    }

    /**
     * 使用指定的ByteBuffer更新要签名或验证的数据。处理 {@code data.remaining()} 个字节，
     * 从 {@code data.position()} 开始。返回时，缓冲区的位置将等于其限制；其限制不会改变。
     *
     * @param data ByteBuffer
     *
     * @exception SignatureException 如果此签名对象未正确初始化。
     * @since 1.5
     */
    public final void update(ByteBuffer data) throws SignatureException {
        if ((state != SIGN) && (state != VERIFY)) {
            throw new SignatureException("对象未初始化用于签名或验证");
        }
        if (data == null) {
            throw new NullPointerException();
        }
        engineUpdate(data);
    }

    /**
     * 返回此签名对象的算法名称。
     *
     * @return 此签名对象的算法名称。
     */
    public final String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * 返回此签名对象的字符串表示形式，提供包括对象状态和使用的算法名称在内的信息。
     *
     * @return 此签名对象的字符串表示形式。
     */
    public String toString() {
        String initState = "";
        switch (state) {
        case UNINITIALIZED:
            initState = "<未初始化>";
            break;
        case VERIFY:
            initState = "<已初始化用于验证>";
            break;
        case SIGN:
            initState = "<已初始化用于签名>";
            break;
        }
        return "签名对象: " + getAlgorithm() + initState;
    }

    /**
     * 将指定的算法参数设置为指定的值。此方法提供了一种通用机制，通过它可以设置此对象的各种参数。
     * 参数可以是算法的任何可设置参数，例如参数大小，或签名生成的随机位源（如果适用），
     * 或指示是否执行特定但可选的计算。每个参数的统一算法特定命名方案是可取的，但在此时未指定。
     *
     * @param param 参数的字符串标识符。
     * @param value 参数值。
     *
     * @exception InvalidParameterException 如果 {@code param} 是此签名算法引擎的无效参数，
     * 参数已设置且不能再次设置，发生安全异常等。
     *
     * @see #getParameter
     *
     * @deprecated 使用
     * {@link #setParameter(java.security.spec.AlgorithmParameterSpec)
     * setParameter}。
     */
    @Deprecated
    public final void setParameter(String param, Object value)
            throws InvalidParameterException {
        engineSetParameter(param, value);
    }

    /**
     * 使用指定的参数集初始化此签名引擎。
     *
     * @param params 参数
     *
     * @exception InvalidAlgorithmParameterException 如果给定的参数不适合此签名引擎
     *
     * @see #getParameters
     */
    public final void setParameter(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
        engineSetParameter(params);
    }

    /**
     * 返回与此签名对象一起使用的参数。
     *
     * <p> 如果此签名已通过调用 {@code setParameter} 方法初始化了参数，此方法返回相同的参数。
     * 如果此签名未初始化参数，此方法可能返回默认和随机生成的参数值的组合（如果底层签名实现支持并能成功生成它们）。
     * 否则，返回 {@code null}。
     *
     * @return 与此签名一起使用的参数，或 {@code null}
     *
     * @see #setParameter(AlgorithmParameterSpec)
     * @since 1.4
     */
    public final AlgorithmParameters getParameters() {
        return engineGetParameters();
    }

    /**
     * 获取指定算法参数的值。此方法提供了一种通用机制，通过它可以获取此对象的各种参数。
     * 参数可以是算法的任何可设置参数，例如参数大小，或签名生成的随机位源（如果适用），
     * 或指示是否执行特定但可选的计算。每个参数的统一算法特定命名方案是可取的，但在此时未指定。
     *
     * @param param 参数的字符串名称。
     *
     * @return 代表参数值的对象，或 {@code null} 如果没有。
     *
     * @exception InvalidParameterException 如果 {@code param} 是此引擎的无效参数，
     * 或在尝试获取此参数时发生其他异常。
     *
     * @see #setParameter(String, Object)
     *
     * @deprecated
     */
    @Deprecated
    public final Object getParameter(String param)
            throws InvalidParameterException {
        return engineGetParameter(param);
    }

    /**
     * 如果实现支持克隆，则返回一个克隆。
     *
     * @return 如果实现支持克隆，则返回一个克隆。
     *
     * @exception CloneNotSupportedException 如果此实现不支持 {@code Cloneable}。
     */
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        } else {
            throw new CloneNotSupportedException();
        }
    }

    /*
     * 以下类允许提供者扩展自 SignatureSpi 而不是从 Signature。它表示一个带有封装的、提供者提供的 SPI 对象（类型为 SignatureSpi）的签名。
     * 如果提供者实现是 SignatureSpi 的实例，上述的 getInstance() 方法将返回此类的一个实例，其中封装了 SPI 对象。
     *
     * 注意：原始 Signature 类中的所有 SPI 方法已移至层次结构中的新类（SignatureSpi），该类已插入 API（Signature）和其原始父类（Object）之间。
     */

    @SuppressWarnings("deprecation")
    private static class Delegate extends Signature {

        // 提供者实现（委托）
        // 一旦选择提供者后填充
        private SignatureSpi sigSpi;

        // 用于提供者选择期间的互斥锁
        private final Object lock;

        // 提供者选择期间要尝试的下一个服务
        // 一旦选择提供者后为 null
        private Service firstService;

        // 提供者选择期间要尝试的剩余服务
        // 一旦选择提供者后为 null
        private Iterator<Service> serviceIterator;

        // 构造函数
        Delegate(SignatureSpi sigSpi, String algorithm) {
            super(algorithm);
            this.sigSpi = sigSpi;
            this.lock = null; // 不需要锁
        }

        // 用于延迟提供者选择
        Delegate(Service service,
                        Iterator<Service> iterator, String algorithm) {
            super(algorithm);
            this.firstService = service;
            this.serviceIterator = iterator;
            this.lock = new Object();
        }

        /**
         * 如果委托支持克隆，则返回一个克隆。
         *
         * @return 如果委托支持克隆，则返回一个克隆。
         *
         * @exception CloneNotSupportedException 如果此委托不支持 {@code Cloneable}。
         */
        public Object clone() throws CloneNotSupportedException {
            chooseFirstProvider();
            if (sigSpi instanceof Cloneable) {
                SignatureSpi sigSpiClone = (SignatureSpi)sigSpi.clone();
                // 因为 'algorithm' 和 'provider' 是我们超类的私有成员，必须执行强制转换以访问它们。
                Signature that =
                    new Delegate(sigSpiClone, ((Signature)this).algorithm);
                that.provider = ((Signature)this).provider;
                return that;
            } else {
                throw new CloneNotSupportedException();
            }
        }

        private static SignatureSpi newInstance(Service s)
                throws NoSuchAlgorithmException {
            if (s.getType().equals("Cipher")) {
                // 必须是 NONEwithRSA
                try {
                    Cipher c = Cipher.getInstance(RSA_CIPHER, s.getProvider());
                    return new CipherAdapter(c);
                } catch (NoSuchPaddingException e) {
                    throw new NoSuchAlgorithmException(e);
                }
            } else {
                Object o = s.newInstance(null);
                if (o instanceof SignatureSpi == false) {
                    throw new NoSuchAlgorithmException
                        ("Not a SignatureSpi: " + o.getClass().getName());
                }
                return (SignatureSpi)o;
            }
        }

        // 从 chooseFirstProvider() 中打印的最大调试警告数
        private static int warnCount = 10;

        /**
         * 从第一个可用的提供者中选择 Spi。如果延迟提供者选择不可能，因为 initSign()/
         * initVerify() 不是调用的第一个方法，则使用此方法。
         */
        void chooseFirstProvider() {
            if (sigSpi != null) {
                return;
            }
            synchronized (lock) {
                if (sigSpi != null) {
                    return;
                }
                if (debug != null) {
                    int w = --warnCount;
                    if (w >= 0) {
                        debug.println("Signature.init() not first method "
                            + "called, disabling delayed provider selection");
                        if (w == 0) {
                            debug.println("Further warnings of this type will "
                                + "be suppressed");
                        }
                        new Exception("Debug call trace").printStackTrace();
                    }
                }
                Exception lastException = null;
                while ((firstService != null) || serviceIterator.hasNext()) {
                    Service s;
                    if (firstService != null) {
                        s = firstService;
                        firstService = null;
                    } else {
                        s = serviceIterator.next();
                    }
                    if (isSpi(s) == false) {
                        continue;
                    }
                    try {
                        sigSpi = newInstance(s);
                        provider = s.getProvider();
                        // 不再需要
                        firstService = null;
                        serviceIterator = null;
                        return;
                    } catch (NoSuchAlgorithmException e) {
                        lastException = e;
                    }
                }
                ProviderException e = new ProviderException
                        ("Could not construct SignatureSpi instance");
                if (lastException != null) {
                    e.initCause(lastException);
                }
                throw e;
            }
        }

        // 用于 engineSetParameter/engineInitSign/engineInitVerify() 以
        // 使用提供的密钥、参数、随机源找到正确的提供者
        private void chooseProvider(int type, Key key,
                AlgorithmParameterSpec params, SecureRandom random)
                throws InvalidKeyException, InvalidAlgorithmParameterException {
            synchronized (lock) {
                if (sigSpi != null) {
                    return;
                }
                Exception lastException = null;
                while ((firstService != null) || serviceIterator.hasNext()) {
                    Service s;
                    if (firstService != null) {
                        s = firstService;
                        firstService = null;
                    } else {
                        s = serviceIterator.next();
                    }
                    // 如果提供者表示不支持此密钥，忽略它
                    if (key != null && s.supportsParameter(key) == false) {
                        continue;
                    }
                    // 如果实例不是 SignatureSpi，忽略它
                    if (isSpi(s) == false) {
                        continue;
                    }
                    try {
                        SignatureSpi spi = newInstance(s);
                        tryOperation(spi, type, key, params, random);
                        provider = s.getProvider();
                        sigSpi = spi;
                        firstService = null;
                        serviceIterator = null;
                        return;
                    } catch (Exception e) {
                        // NoSuchAlgorithmException from newInstance()
                        // InvalidKeyException from init()
                        // RuntimeException (ProviderException) from init()
                        if (lastException == null) {
                            lastException = e;
                        }
                    }
                }
                // 未找到有效提供者，失败
                if (lastException instanceof InvalidKeyException) {
                    throw (InvalidKeyException)lastException;
                }
                if (lastException instanceof RuntimeException) {
                    throw (RuntimeException)lastException;
                }
                if (lastException instanceof InvalidAlgorithmParameterException) {
                    throw (InvalidAlgorithmParameterException)lastException;
                }


                            String k = (key != null) ? key.getClass().getName() : "(null)";
                throw new InvalidKeyException
                    ("没有安装的提供者支持此密钥： "
                    + k, lastException);
            }
        }

        private static final int I_PUB           = 1;
        private static final int I_PRIV          = 2;
        private static final int I_PRIV_SR       = 3;
        private static final int I_PUB_PARAM     = 4;
        private static final int I_PRIV_PARAM_SR = 5;
        private static final int S_PARAM         = 6;

        private void tryOperation(SignatureSpi spi, int type, Key  key,
                AlgorithmParameterSpec params, SecureRandom random)
                throws InvalidKeyException, InvalidAlgorithmParameterException {

            switch (type) {
            case I_PUB:
                spi.engineInitVerify((PublicKey)key);
                break;
            case I_PUB_PARAM:
                spi.engineInitVerify((PublicKey)key, params);
                break;
            case I_PRIV:
                spi.engineInitSign((PrivateKey)key);
                break;
            case I_PRIV_SR:
                spi.engineInitSign((PrivateKey)key, random);
                break;
            case I_PRIV_PARAM_SR:
                spi.engineInitSign((PrivateKey)key, params, random);
                break;
            case S_PARAM:
                spi.engineSetParameter(params);
                break;
            default:
                throw new AssertionError("内部错误： " + type);
            }
        }

        protected void engineInitVerify(PublicKey publicKey)
                throws InvalidKeyException {
            if (sigSpi != null) {
                sigSpi.engineInitVerify(publicKey);
            } else {
                try {
                    chooseProvider(I_PUB, publicKey, null, null);
                } catch (InvalidAlgorithmParameterException iape) {
                    // 不应发生，以防万一重新抛出为 IKE
                    throw new InvalidKeyException(iape);
                }
            }
        }

        void engineInitVerify(PublicKey publicKey,
                AlgorithmParameterSpec params)
                throws InvalidKeyException, InvalidAlgorithmParameterException {
            if (sigSpi != null) {
                sigSpi.engineInitVerify(publicKey, params);
            } else {
                chooseProvider(I_PUB_PARAM, publicKey, params, null);
            }
        }

        protected void engineInitSign(PrivateKey privateKey)
                throws InvalidKeyException {
            if (sigSpi != null) {
                sigSpi.engineInitSign(privateKey);
            } else {
                try {
                    chooseProvider(I_PRIV, privateKey, null, null);
                } catch (InvalidAlgorithmParameterException iape) {
                    // 不应发生，以防万一重新抛出为 IKE
                    throw new InvalidKeyException(iape);
                }
            }
        }

        protected void engineInitSign(PrivateKey privateKey, SecureRandom sr)
                throws InvalidKeyException {
            if (sigSpi != null) {
                sigSpi.engineInitSign(privateKey, sr);
            } else {
                try {
                    chooseProvider(I_PRIV_SR, privateKey, null, sr);
                } catch (InvalidAlgorithmParameterException iape) {
                    // 不应发生，以防万一重新抛出为 IKE
                    throw new InvalidKeyException(iape);
                }
            }
        }

        void engineInitSign(PrivateKey privateKey,
                AlgorithmParameterSpec params, SecureRandom sr)
                throws InvalidKeyException, InvalidAlgorithmParameterException {
            if (sigSpi != null) {
                sigSpi.engineInitSign(privateKey, params, sr);
            } else {
                chooseProvider(I_PRIV_PARAM_SR, privateKey, params, sr);
            }
        }

        protected void engineUpdate(byte b) throws SignatureException {
            chooseFirstProvider();
            sigSpi.engineUpdate(b);
        }

        protected void engineUpdate(byte[] b, int off, int len)
                throws SignatureException {
            chooseFirstProvider();
            sigSpi.engineUpdate(b, off, len);
        }

        protected void engineUpdate(ByteBuffer data) {
            chooseFirstProvider();
            sigSpi.engineUpdate(data);
        }

        protected byte[] engineSign() throws SignatureException {
            chooseFirstProvider();
            return sigSpi.engineSign();
        }

        protected int engineSign(byte[] outbuf, int offset, int len)
                throws SignatureException {
            chooseFirstProvider();
            return sigSpi.engineSign(outbuf, offset, len);
        }

        protected boolean engineVerify(byte[] sigBytes)
                throws SignatureException {
            chooseFirstProvider();
            return sigSpi.engineVerify(sigBytes);
        }

        protected boolean engineVerify(byte[] sigBytes, int offset, int length)
                throws SignatureException {
            chooseFirstProvider();
            return sigSpi.engineVerify(sigBytes, offset, length);
        }

        protected void engineSetParameter(String param, Object value)
                throws InvalidParameterException {
            chooseFirstProvider();
            sigSpi.engineSetParameter(param, value);
        }

        protected void engineSetParameter(AlgorithmParameterSpec params)
                throws InvalidAlgorithmParameterException {
            if (sigSpi != null) {
                sigSpi.engineSetParameter(params);
            } else {
                try {
                    chooseProvider(S_PARAM, null, params, null);
                } catch (InvalidKeyException ike) {
                    // 不应发生，以防万一重新抛出
                    throw new InvalidAlgorithmParameterException(ike);
                }
            }
        }

        protected Object engineGetParameter(String param)
                throws InvalidParameterException {
            chooseFirstProvider();
            return sigSpi.engineGetParameter(param);
        }

        protected AlgorithmParameters engineGetParameters() {
            chooseFirstProvider();
            return sigSpi.engineGetParameters();
        }
    }

    // RSA/ECB/PKCS1Padding 密码的适配器
    @SuppressWarnings("deprecation")
    private static class CipherAdapter extends SignatureSpi {

        private final Cipher cipher;

        private ByteArrayOutputStream data;

        CipherAdapter(Cipher cipher) {
            this.cipher = cipher;
        }

        protected void engineInitVerify(PublicKey publicKey)
                throws InvalidKeyException {
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            if (data == null) {
                data = new ByteArrayOutputStream(128);
            } else {
                data.reset();
            }
        }

        protected void engineInitSign(PrivateKey privateKey)
                throws InvalidKeyException {
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            data = null;
        }

        protected void engineInitSign(PrivateKey privateKey,
                SecureRandom random) throws InvalidKeyException {
            cipher.init(Cipher.ENCRYPT_MODE, privateKey, random);
            data = null;
        }

        protected void engineUpdate(byte b) throws SignatureException {
            engineUpdate(new byte[] {b}, 0, 1);
        }

        protected void engineUpdate(byte[] b, int off, int len)
                throws SignatureException {
            if (data != null) {
                data.write(b, off, len);
                return;
            }
            byte[] out = cipher.update(b, off, len);
            if ((out != null) && (out.length != 0)) {
                throw new SignatureException
                    ("密码意外返回数据");
            }
        }

        protected byte[] engineSign() throws SignatureException {
            try {
                return cipher.doFinal();
            } catch (IllegalBlockSizeException e) {
                throw new SignatureException("doFinal() 失败", e);
            } catch (BadPaddingException e) {
                throw new SignatureException("doFinal() 失败", e);
            }
        }

        protected boolean engineVerify(byte[] sigBytes)
                throws SignatureException {
            try {
                byte[] out = cipher.doFinal(sigBytes);
                byte[] dataBytes = data.toByteArray();
                data.reset();
                return MessageDigest.isEqual(out, dataBytes);
            } catch (BadPaddingException e) {
                // 例如，使用了错误的公钥
                // 返回 false 而不是抛出异常
                return false;
            } catch (IllegalBlockSizeException e) {
                throw new SignatureException("doFinal() 失败", e);
            }
        }

        protected void engineSetParameter(String param, Object value)
                throws InvalidParameterException {
            throw new InvalidParameterException("不支持参数");
        }

        protected Object engineGetParameter(String param)
                throws InvalidParameterException {
            throw new InvalidParameterException("不支持参数");
        }

    }

}
