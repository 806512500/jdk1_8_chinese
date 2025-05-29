/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.security.cert;

import java.io.IOException;

import sun.misc.HexDumpEncoder;
import sun.security.util.DerValue;

/**
 * 由 ASN.1 PolicyQualifierInfo 结构表示的不可变策略限定符。
 *
 * <p>ASN.1 定义如下：
 * <pre>
 *   PolicyQualifierInfo ::= SEQUENCE {
 *        policyQualifierId       PolicyQualifierId,
 *        qualifier               ANY DEFINED BY policyQualifierId }
 * </pre>
 * <p>
 * 如果 X.509 版本 3 证书中存在证书策略扩展，则该扩展包含一个或多个策略信息项的序列，每个项由一个对象标识符 (OID) 和可选的限定符组成。在终端实体证书中，这些策略信息项表示证书的颁发策略以及证书的使用目的。在 CA 证书中，这些策略信息项限制了包含此证书的认证路径的策略集。
 * <p>
 * 通过 {@link PolicyNode#getPolicyQualifiers PolicyNode.getPolicyQualifiers} 方法返回一个 {@code PolicyQualifierInfo} 对象的 {@code Set}。这允许具有特定策略要求的应用程序处理和验证每个策略限定符。需要处理策略限定符的应用程序应在验证认证路径之前显式地将 {@code policyQualifiersRejected} 标志设置为 false（通过调用 {@link PKIXParameters#setPolicyQualifiersRejected PKIXParameters.setPolicyQualifiersRejected} 方法）。
 *
 * <p>注意，PKIX 认证路径验证算法规定，证书策略扩展中任何被标记为关键的策略限定符都必须被处理和验证。否则，认证路径必须被拒绝。如果 {@code policyQualifiersRejected} 标志设置为 false，则应用程序必须以这种方式验证所有策略限定符，以符合 PKIX 标准。
 *
 * <p><b>并发访问</b>
 *
 * <p>所有 {@code PolicyQualifierInfo} 对象必须是不可变的和线程安全的。也就是说，多个线程可以同时调用此类中定义的方法，对单个 {@code PolicyQualifierInfo} 对象（或多个对象）进行调用，而不会产生不良影响。要求 {@code PolicyQualifierInfo} 对象是不可变的和线程安全的，允许它们被传递给各种代码部分，而无需担心协调访问。
 *
 * @author      seth proctor
 * @author      Sean Mullan
 * @since       1.4
 */
public class PolicyQualifierInfo {

    private byte [] mEncoded;
    private String mId;
    private byte [] mData;
    private String pqiString;

    /**
     * 从编码的字节创建 {@code PolicyQualifierInfo} 的实例。在构造时复制编码的字节数组。
     *
     * @param encoded 包含限定符的 DER 编码的字节数组
     * @exception IOException 如果字节数组不代表有效且可解析的策略限定符，则抛出此异常
     */
    public PolicyQualifierInfo(byte[] encoded) throws IOException {
        mEncoded = encoded.clone();

        DerValue val = new DerValue(mEncoded);
        if (val.tag != DerValue.tag_Sequence)
            throw new IOException("无效的 PolicyQualifierInfo 编码");

        mId = (val.data.getDerValue()).getOID().toString();
        byte [] tmp = val.data.toByteArray();
        if (tmp == null) {
            mData = null;
        } else {
            mData = new byte[tmp.length];
            System.arraycopy(tmp, 0, mData, 0, tmp.length);
        }
    }

    /**
     * 返回此 {@code PolicyQualifierInfo} 的 {@code policyQualifierId} 字段。{@code policyQualifierId}
     * 是由非负整数集合表示的对象标识符 (OID)，这些整数由点分隔。
     *
     * @return OID（从不为 {@code null}）
     */
    public final String getPolicyQualifierId() {
        return mId;
    }

    /**
     * 返回此 {@code PolicyQualifierInfo} 的 ASN.1 DER 编码形式。
     *
     * @return ASN.1 DER 编码的字节（从不为 {@code null}）。注意返回的是副本，因此每次调用此方法时都会克隆数据。
     */
    public final byte[] getEncoded() {
        return mEncoded.clone();
    }

    /**
     * 返回此 {@code PolicyQualifierInfo} 的 {@code qualifier} 字段的 ASN.1 DER 编码形式。
     *
     * @return {@code qualifier} 字段的 ASN.1 DER 编码字节。注意返回的是副本，因此每次调用此方法时都会克隆数据。
     */
    public final byte[] getPolicyQualifier() {
        return (mData == null ? null : mData.clone());
    }

    /**
     * 返回此 {@code PolicyQualifierInfo} 的可打印表示形式。
     *
     * @return 描述此 {@code PolicyQualifierInfo} 内容的 {@code String}
     */
    public String toString() {
        if (pqiString != null)
            return pqiString;
        HexDumpEncoder enc = new HexDumpEncoder();
        StringBuffer sb = new StringBuffer();
        sb.append("PolicyQualifierInfo: [\n");
        sb.append("  qualifierID: " + mId + "\n");
        sb.append("  qualifier: " +
            (mData == null ? "null" : enc.encodeBuffer(mData)) + "\n");
        sb.append("]");
        pqiString = sb.toString();
        return pqiString;
    }
}
