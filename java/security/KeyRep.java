/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.*;
import java.util.Locale;

import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

/**
 * 序列化 Key 对象的标准表示。
 *
 * <p>
 *
 * 注意，序列化的 Key 可能包含不应在不受信任环境中暴露的敏感信息。有关更多信息，请参阅
 * <a href="../../../platform/serialization/spec/security.html">
 * 序列化规范的安全附录</a>。
 *
 * @see Key
 * @see KeyFactory
 * @see javax.crypto.spec.SecretKeySpec
 * @see java.security.spec.X509EncodedKeySpec
 * @see java.security.spec.PKCS8EncodedKeySpec
 *
 * @since 1.5
 */

public class KeyRep implements Serializable {

    private static final long serialVersionUID = -4757683898830641853L;

    /**
     * 密钥类型。
     *
     * @since 1.5
     */
    public static enum Type {

        /** 秘密密钥类型。 */
        SECRET,

        /** 公钥类型。 */
        PUBLIC,

        /** 私钥类型。 */
        PRIVATE,

    }

    private static final String PKCS8 = "PKCS#8";
    private static final String X509 = "X.509";
    private static final String RAW = "RAW";

    /**
     * Type.SECRET、Type.PUBLIC 或 Type.PRIVATE 之一。
     *
     * @serial
     */
    private Type type;

    /**
     * 密钥算法。
     *
     * @serial
     */
    private String algorithm;

    /**
     * 密钥编码格式。
     *
     * @serial
     */
    private String format;

    /**
     * 编码后的密钥字节。
     *
     * @serial
     */
    private byte[] encoded;

    /**
     * 构造替代的 Key 类。
     *
     * <p>
     *
     * @param type Type.SECRET、Type.PUBLIC 或 Type.PRIVATE 之一。
     * @param algorithm 从 {@code Key.getAlgorithm()} 返回的算法。
     * @param format 从 {@code Key.getFormat()} 返回的编码格式。
     * @param encoded 从 {@code Key.getEncoded()} 返回的编码字节。
     *
     * @exception NullPointerException
     *          如果 type 为 {@code null}，
     *          如果 algorithm 为 {@code null}，
     *          如果 format 为 {@code null}，
     *          或者如果 encoded 为 {@code null}
     */
    public KeyRep(Type type, String algorithm,
                String format, byte[] encoded) {

        if (type == null || algorithm == null ||
            format == null || encoded == null) {
            throw new NullPointerException("无效的 null 输入");
        }

        this.type = type;
        this.algorithm = algorithm;
        this.format = format.toUpperCase(Locale.ENGLISH);
        this.encoded = encoded.clone();
    }

    /**
     * 解析 Key 对象。
     *
     * <p> 该方法支持三种 Type/format 组合：
     * <ul>
     * <li> Type.SECRET/"RAW" - 返回使用编码密钥字节和算法构造的 SecretKeySpec 对象
     * <li> Type.PUBLIC/"X.509" - 获取密钥算法的 KeyFactory 实例，使用编码密钥字节构造 X509EncodedKeySpec，并从 spec 生成公钥
     * <li> Type.PRIVATE/"PKCS#8" - 获取密钥算法的 KeyFactory 实例，使用编码密钥字节构造 PKCS8EncodedKeySpec，并从 spec 生成私钥
     * </ul>
     *
     * <p>
     *
     * @return 解析后的 Key 对象
     *
     * @exception ObjectStreamException 如果 Type/format 组合未被识别，如果算法、密钥格式或编码密钥字节未被识别/无效，或者如果因任何原因导致密钥解析失败
     */
    protected Object readResolve() throws ObjectStreamException {
        try {
            if (type == Type.SECRET && RAW.equals(format)) {
                return new SecretKeySpec(encoded, algorithm);
            } else if (type == Type.PUBLIC && X509.equals(format)) {
                KeyFactory f = KeyFactory.getInstance(algorithm);
                return f.generatePublic(new X509EncodedKeySpec(encoded));
            } else if (type == Type.PRIVATE && PKCS8.equals(format)) {
                KeyFactory f = KeyFactory.getInstance(algorithm);
                return f.generatePrivate(new PKCS8EncodedKeySpec(encoded));
            } else {
                throw new NotSerializableException
                        ("未识别的 type/format 组合: " +
                        type + "/" + format);
            }
        } catch (NotSerializableException nse) {
            throw nse;
        } catch (Exception e) {
            NotSerializableException nse = new NotSerializableException
                                        ("java.security.Key: " +
                                        "[" + type + "] " +
                                        "[" + algorithm + "] " +
                                        "[" + format + "]");
            nse.initCause(e);
            throw nse;
        }
    }
}
