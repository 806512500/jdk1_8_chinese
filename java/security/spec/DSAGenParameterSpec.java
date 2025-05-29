/*
 * 版权所有 (c) 2012, 2013, Oracle 及/或其附属公司。保留所有权利。
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
 * 该不可变类指定了用于生成 DSA 参数的参数集，如
 * <a href="http://csrc.nist.gov/publications/fips/fips186-3/fips_186-3.pdf">FIPS 186-3 数字签名标准 (DSS)</a> 中所指定。
 *
 * @see AlgorithmParameterSpec
 *
 * @since 8
 */
public final class DSAGenParameterSpec implements AlgorithmParameterSpec {

    private final int pLen;
    private final int qLen;
    private final int seedLen;

    /**
     * 使用 {@code primePLen} 和 {@code subprimeQLen} 创建 DSA 参数生成的域参数规范。
     * {@code subprimeQLen} 的值也用作域参数种子的默认长度（以位为单位）。
     * @param primePLen 期望的素数 P 的长度（以位为单位）。
     * @param subprimeQLen 期望的子素数 Q 的长度（以位为单位）。
     * @exception IllegalArgumentException 如果 {@code primePLen}
     * 或 {@code subprimeQLen} 根据 FIPS 186-3 的规范是非法的。
     */
    public DSAGenParameterSpec(int primePLen, int subprimeQLen) {
        this(primePLen, subprimeQLen, subprimeQLen);
    }

    /**
     * 使用 {@code primePLen}，{@code subprimeQLen} 和 {@code seedLen} 创建 DSA 参数生成的域参数规范。
     * @param primePLen 期望的素数 P 的长度（以位为单位）。
     * @param subprimeQLen 期望的子素数 Q 的长度（以位为单位）。
     * @param seedLen 期望的域参数种子的长度（以位为单位），应等于或大于 {@code subprimeQLen}。
     * @exception IllegalArgumentException 如果 {@code primePLenLen}，
     * {@code subprimeQLen} 或 {@code seedLen} 根据 FIPS 186-3 的规范是非法的。
     */
    public DSAGenParameterSpec(int primePLen, int subprimeQLen, int seedLen) {
        switch (primePLen) {
        case 1024:
            if (subprimeQLen != 160) {
                throw new IllegalArgumentException
                    ("当 primePLen=1024 时，subprimeQLen 必须为 160");
            }
            break;
        case 2048:
            if (subprimeQLen != 224 && subprimeQLen != 256) {
               throw new IllegalArgumentException
                   ("当 primePLen=2048 时，subprimeQLen 必须为 224 或 256");
            }
            break;
        case 3072:
            if (subprimeQLen != 256) {
                throw new IllegalArgumentException
                    ("当 primePLen=3072 时，subprimeQLen 必须为 256");
            }
            break;
        default:
            throw new IllegalArgumentException
                ("primePLen 必须为 1024, 2048 或 3072");
        }
        if (seedLen < subprimeQLen) {
            throw new IllegalArgumentException
                ("seedLen 必须等于或大于 subprimeQLen");
        }
        this.pLen = primePLen;
        this.qLen = subprimeQLen;
        this.seedLen = seedLen;
    }

    /**
     * 返回待生成的 DSA 域参数的素数 P 的期望长度（以位为单位）。
     * @return 素数 P 的长度。
     */
    public int getPrimePLength() {
        return pLen;
    }

    /**
     * 返回待生成的 DSA 域参数的子素数 Q 的期望长度（以位为单位）。
     * @return 子素数 Q 的长度。
     */
    public int getSubprimeQLength() {
        return qLen;
    }

    /**
     * 返回域参数种子的期望长度（以位为单位）。
     * @return 域参数种子的长度。
     */
    public int getSeedLength() {
        return seedLen;
    }
}
