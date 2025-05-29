/*
 * 版权所有 (c) 2003, 2013，Oracle 和/或其附属公司。保留所有权利。
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
 * 此不可变类指定用于生成椭圆曲线 (EC) 域参数的参数集。
 *
 * @see AlgorithmParameterSpec
 *
 * @author Valerie Peng
 *
 * @since 1.5
 */
public class ECGenParameterSpec implements AlgorithmParameterSpec {

    private String name;

    /**
     * 使用标准（或预定义）名称 {@code stdName} 创建 EC 参数生成的参数规范，
     * 以生成相应的（预计算的）椭圆曲线域参数。有关支持的名称列表，请参阅将要使用的提供者的文档。
     * @param stdName 要生成的 EC 域参数的标准名称。
     * @exception NullPointerException 如果 {@code stdName} 为 null。
     */
    public ECGenParameterSpec(String stdName) {
        if (stdName == null) {
            throw new NullPointerException("stdName is null");
        }
        this.name = stdName;
    }

    /**
     * 返回要生成的 EC 域参数的标准或预定义名称。
     * @return 标准或预定义名称。
     */
    public String getName() {
        return name;
    }
}
