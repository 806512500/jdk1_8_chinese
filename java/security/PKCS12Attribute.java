
/*
 * 版权所有 (c) 2013, 2020，Oracle 及/或其附属公司。保留所有权利。
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

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.regex.Pattern;
import sun.security.util.*;

/**
 * 与 PKCS12 密钥库条目关联的属性。
 * 属性名称是一个 ASN.1 对象标识符，属性值是一组 ASN.1 类型。
 *
 * @since 1.8
 */
public final class PKCS12Attribute implements KeyStore.Entry.Attribute {

    private static final Pattern COLON_SEPARATED_HEX_PAIRS =
        Pattern.compile("^[0-9a-fA-F]{2}(:[0-9a-fA-F]{2})+$");
    private String name;
    private String value;
    private byte[] encoded;
    private int hashValue = -1;

    /**
     * 从名称和值构造 PKCS12 属性。
     * 名称是表示为以点分隔的整数列表的 ASN.1 对象标识符。
     * 字符串值表示为字符串本身。
     * 二进制值表示为以冒号分隔的十六进制数字对的字符串。
     * 多值属性表示为用方括号括起来的以逗号分隔的值列表。参见
     * {@link Arrays#toString(java.lang.Object[])}。
     * <p>
     * 字符串值将被 DER 编码为 ASN.1 UTF8String，二进制值将被 DER 编码为 ASN.1 Octet String。
     *
     * @param name 属性的标识符
     * @param value 属性的值
     *
     * @exception NullPointerException 如果 {@code name} 或 {@code value}
     *     为 {@code null}
     * @exception IllegalArgumentException 如果 {@code name} 或
     *     {@code value} 格式不正确
     */
    public PKCS12Attribute(String name, String value) {
        if (name == null || value == null) {
            throw new NullPointerException();
        }
        // 验证名称
        ObjectIdentifier type;
        try {
            type = new ObjectIdentifier(name);
        } catch (IOException e) {
            throw new IllegalArgumentException("格式不正确: name", e);
        }
        this.name = name;

        // 验证值
        int length = value.length();
        String[] values;
        if (value.charAt(0) == '[' && value.charAt(length - 1) == ']') {
            values = value.substring(1, length - 1).split(", ");
        } else {
            values = new String[]{ value };
        }
        this.value = value;

        try {
            this.encoded = encode(type, values);
        } catch (IOException e) {
            throw new IllegalArgumentException("格式不正确: value", e);
        }
    }

    /**
     * 从其 ASN.1 DER 编码构造 PKCS12 属性。
     * DER 编码由以下 ASN.1 定义指定：
     * <pre>
     *
     * Attribute ::= SEQUENCE {
     *     type   AttributeType,
     *     values SET OF AttributeValue
     * }
     * AttributeType ::= OBJECT IDENTIFIER
     * AttributeValue ::= ANY defined by type
     *
     * </pre>
     *
     * @param encoded 属性的 ASN.1 DER 编码。它被克隆以防止后续修改。
     *
     * @exception NullPointerException 如果 {@code encoded} 为
     *     {@code null}
     * @exception IllegalArgumentException 如果 {@code encoded} 格式不正确
     */
    public PKCS12Attribute(byte[] encoded) {
        if (encoded == null) {
            throw new NullPointerException();
        }
        this.encoded = encoded.clone();

        try {
            parse(encoded);
        } catch (IOException e) {
            throw new IllegalArgumentException("格式不正确: encoded", e);
        }
    }

    /**
     * 返回属性的 ASN.1 对象标识符，表示为以点分隔的整数列表。
     *
     * @return 属性的标识符
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 返回属性的 ASN.1 DER 编码值作为字符串。
     * ASN.1 DER 编码值以以下之一的 {@code String} 格式返回：
     * <ul>
     * <li> 具有自然字符串表示的基本 ASN.1 类型的 DER 编码作为字符串本身返回。
     *      这样的类型目前仅限于 BOOLEAN, INTEGER, OBJECT IDENTIFIER, UTCTime, GeneralizedTime 和
     *      以下六种 ASN.1 字符串类型：UTF8String, PrintableString, T61String, IA5String, BMPString 和
     *      GeneralString。
     * <li> 任何其他 ASN.1 类型的 DER 编码不会被解码，而是作为以冒号分隔的十六进制数字对的二进制字符串返回。
     * </ul>
     * 多值属性表示为用方括号括起来的以逗号分隔的值列表。参见
     * {@link Arrays#toString(java.lang.Object[])}。
     *
     * @return 属性值的字符串编码
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * 返回属性的 ASN.1 DER 编码。
     *
     * @return 属性的 DER 编码的克隆
     */
    public byte[] getEncoded() {
        return encoded.clone();
    }

    /**
     * 比较此 {@code PKCS12Attribute} 和指定对象是否相等。
     *
     * @param obj 比较对象
     *
     * @return 如果 {@code obj} 是一个 {@code PKCS12Attribute} 且它们的 DER 编码相等，则返回 true。
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PKCS12Attribute)) {
            return false;
        }
        return Arrays.equals(encoded, ((PKCS12Attribute) obj).getEncoded());
    }

    /**
     * 返回此 {@code PKCS12Attribute} 的哈希码。
     * 哈希码从其 DER 编码计算得出。
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        if (hashValue == -1) {
            Arrays.hashCode(encoded);
        }
        return hashValue;
    }


                /**
     * 返回此 {@code PKCS12Attribute} 的字符串表示形式。
     *
     * @return 由等号分隔的名称/值对
     */
    @Override
    public String toString() {
        return (name + "=" + value);
    }

    private byte[] encode(ObjectIdentifier type, String[] values)
            throws IOException {
        DerOutputStream attribute = new DerOutputStream();
        attribute.putOID(type);
        DerOutputStream attrContent = new DerOutputStream();
        for (String value : values) {
            if (COLON_SEPARATED_HEX_PAIRS.matcher(value).matches()) {
                byte[] bytes =
                    new BigInteger(value.replace(":", ""), 16).toByteArray();
                if (bytes[0] == 0) {
                    bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
                }
                attrContent.putOctetString(bytes);
            } else {
                attrContent.putUTF8String(value);
            }
        }
        attribute.write(DerValue.tag_Set, attrContent);
        DerOutputStream attributeValue = new DerOutputStream();
        attributeValue.write(DerValue.tag_Sequence, attribute);

        return attributeValue.toByteArray();
    }

    private void parse(byte[] encoded) throws IOException {
        DerInputStream attributeValue = new DerInputStream(encoded);
        DerValue[] attrSeq = attributeValue.getSequence(2);
        if (attrSeq.length != 2) {
            throw new IOException("PKCS12Attribute 的长度无效");
        }
        ObjectIdentifier type = attrSeq[0].getOID();
        DerInputStream attrContent =
            new DerInputStream(attrSeq[1].toByteArray());
        DerValue[] attrValueSet = attrContent.getSet(1);
        String[] values = new String[attrValueSet.length];
        String printableString;
        for (int i = 0; i < attrValueSet.length; i++) {
            if (attrValueSet[i].tag == DerValue.tag_OctetString) {
                values[i] = Debug.toString(attrValueSet[i].getOctetString());
            } else if ((printableString = attrValueSet[i].getAsString())
                != null) {
                values[i] = printableString;
            } else if (attrValueSet[i].tag == DerValue.tag_ObjectId) {
                values[i] = attrValueSet[i].getOID().toString();
            } else if (attrValueSet[i].tag == DerValue.tag_GeneralizedTime) {
                values[i] = attrValueSet[i].getGeneralizedTime().toString();
            } else if (attrValueSet[i].tag == DerValue.tag_UtcTime) {
                values[i] = attrValueSet[i].getUTCTime().toString();
            } else if (attrValueSet[i].tag == DerValue.tag_Integer) {
                values[i] = attrValueSet[i].getBigInteger().toString();
            } else if (attrValueSet[i].tag == DerValue.tag_Boolean) {
                values[i] = String.valueOf(attrValueSet[i].getBoolean());
            } else {
                values[i] = Debug.toString(attrValueSet[i].getDataBytes());
            }
        }

        this.name = type.toString();
        this.value = values.length == 1 ? values[0] : Arrays.toString(values);
    }
}
