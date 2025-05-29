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

package java.security.spec;

/**
 * 本类表示以编码格式的公钥或私钥。
 *
 * @author Jan Luehe
 *
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see X509EncodedKeySpec
 * @see PKCS8EncodedKeySpec
 *
 * @since 1.2
 */

public abstract class EncodedKeySpec implements KeySpec {

    private byte[] encodedKey;

    /**
     * 使用给定的编码密钥创建新的 EncodedKeySpec。
     *
     * @param encodedKey 编码密钥。数组的内容被复制以防止后续修改。
     * @exception NullPointerException 如果 {@code encodedKey}
     * 是 null。
     */
    public EncodedKeySpec(byte[] encodedKey) {
        this.encodedKey = encodedKey.clone();
    }

    /**
     * 返回编码密钥。
     *
     * @return 编码密钥。每次调用此方法时返回一个新的数组。
     */
    public byte[] getEncoded() {
        return this.encodedKey.clone();
    }

    /**
     * 返回与此密钥规范关联的编码格式的名称。
     *
     * <p>如果密钥的不透明表示形式
     * （参见 {@link java.security.Key Key}）可以转换
     * （参见 {@link java.security.KeyFactory KeyFactory}）
     * 为这种密钥规范（或其子类），
     * 则在不透明密钥上调用 {@code getFormat}
     * 返回的值与这种密钥规范的
     * {@code getFormat} 方法返回的值相同。
     *
     * @return 编码格式的字符串表示形式。
     */
    public abstract String getFormat();
}
