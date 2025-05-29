/*
 * 版权所有 (c) 2003, 2020, Oracle 和/或其附属公司。保留所有权利。
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

import java.security.spec.AlgorithmParameterSpec;

/**
 * 该类指定了与OAEP填充和RSASSA-PSS签名方案中使用的掩码生成函数MGF1相关的参数集，
 * 如<a href="https://tools.ietf.org/rfc/rfc8017.txt">PKCS#1 v2.2</a>标准中定义的。
 *
 * <p>其在PKCS#1标准中的ASN.1定义如下：
 * <pre>
 * PKCS1MGFAlgorithms    ALGORITHM-IDENTIFIER ::= {
 *   { OID id-mgf1 PARAMETERS HashAlgorithm },
 *   ...  -- 允许未来扩展 --
 * }
 * </pre>
 * 其中
 * <pre>
 * HashAlgorithm ::= AlgorithmIdentifier {
 *   {OAEP-PSSDigestAlgorithms}
 * }
 *
 * OAEP-PSSDigestAlgorithms    ALGORITHM-IDENTIFIER ::= {
 *   { OID id-sha1       PARAMETERS NULL }|
 *   { OID id-sha224     PARAMETERS NULL }|
 *   { OID id-sha256     PARAMETERS NULL }|
 *   { OID id-sha384     PARAMETERS NULL }|
 *   { OID id-sha512     PARAMETERS NULL }|
 *   { OID id-sha512-224 PARAMETERS NULL }|
 *   { OID id-sha512-256 PARAMETERS NULL },
 *   ...  -- 允许未来扩展 --
 * }
 * </pre>
 * @see PSSParameterSpec
 * @see javax.crypto.spec.OAEPParameterSpec
 *
 * @author Valerie Peng
 *
 * @since 1.5
 */
public class MGF1ParameterSpec implements AlgorithmParameterSpec {

    /**
     * 使用 "SHA-1" 消息摘要的 MGF1ParameterSpec
     */
    public static final MGF1ParameterSpec SHA1 =
        new MGF1ParameterSpec("SHA-1");

    /**
     * 使用 "SHA-224" 消息摘要的 MGF1ParameterSpec
     */
    public static final MGF1ParameterSpec SHA224 =
        new MGF1ParameterSpec("SHA-224");

    /**
     * 使用 "SHA-256" 消息摘要的 MGF1ParameterSpec
     */
    public static final MGF1ParameterSpec SHA256 =
        new MGF1ParameterSpec("SHA-256");

    /**
     * 使用 "SHA-384" 消息摘要的 MGF1ParameterSpec
     */
    public static final MGF1ParameterSpec SHA384 =
        new MGF1ParameterSpec("SHA-384");

    /**
     * 使用 SHA-512 消息摘要的 MGF1ParameterSpec
     */
    public static final MGF1ParameterSpec SHA512 =
        new MGF1ParameterSpec("SHA-512");

    /**
     * 使用 SHA-512/224 消息摘要的 MGF1ParameterSpec
     */
    public static final MGF1ParameterSpec SHA512_224 =
        new MGF1ParameterSpec("SHA-512/224");

    /**
     * 使用 SHA-512/256 消息摘要的 MGF1ParameterSpec
     */
    public static final MGF1ParameterSpec SHA512_256 =
        new MGF1ParameterSpec("SHA-512/256");

    private String mdName;

    /**
     * 构造一个根据PKCS #1标准定义的掩码生成函数MGF1的参数集。
     *
     * @param mdName 用于此掩码生成函数MGF1的消息摘要算法名称。
     * @exception NullPointerException 如果 {@code mdName} 为 null。
     */
    public MGF1ParameterSpec(String mdName) {
        if (mdName == null) {
            throw new NullPointerException("digest algorithm is null");
        }
        this.mdName = mdName;
    }

    /**
     * 返回掩码生成函数使用的消息摘要算法名称。
     *
     * @return 消息摘要的算法名称。
     */
    public String getDigestAlgorithm() {
        return mdName;
    }
}
