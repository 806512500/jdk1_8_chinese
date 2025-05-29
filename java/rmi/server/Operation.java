/*
 * 版权所有 (c) 1996, 2004, Oracle 和/或其附属公司。保留所有权利。
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

package java.rmi.server;

/**
 * 一个 <code>Operation</code> 包含一个 Java 方法的描述。
 * <code>Operation</code> 对象在 JDK1.1 版本的存根和骨架中使用。
 * 对于 1.2 风格的存根（使用 <code>rmic -v1.2</code> 生成的存根），<code>Operation</code> 类不再需要；
 * 因此，此类已弃用。
 *
 * @since JDK1.1
 * @deprecated 没有替代
 */
@Deprecated
public class Operation {
    private String operation;

    /**
     * 创建一个新的 Operation 对象。
     * @param op 方法名
     * @deprecated 没有替代
     * @since JDK1.1
     */
    @Deprecated
    public Operation(String op) {
        operation = op;
    }

    /**
     * 返回方法的名称。
     * @return 方法名
     * @deprecated 没有替代
     * @since JDK1.1
     */
    @Deprecated
    public String getOperation() {
        return operation;
    }

    /**
     * 返回操作的字符串表示形式。
     * @deprecated 没有替代
     * @since JDK1.1
     */
    @Deprecated
    public String toString() {
        return operation;
    }
}
