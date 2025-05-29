/*
 * 版权所有 (c) 2003, 2004, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang.management;

/**
 * {@link MemoryPoolMXBean 内存池} 的类型。
 *
 * @author  Mandy Chung
 * @since   1.5
 */
public enum MemoryType {

    /**
     * 堆内存类型。
     * <p>
     * Java 虚拟机有一个 <i>堆</i>
     * 作为运行时数据区域，从中为所有类实例和数组分配内存。
     */
    HEAP("堆内存"),

    /**
     * 非堆内存类型。
     * <p>
     * Java 虚拟机管理除堆之外的内存
     * （称为 <i>非堆内存</i>）。非堆内存包括
     * <i>方法区</i> 和 Java 虚拟机内部
     * 处理或优化所需的内存。
     * 它存储每个类的结构，如运行时
     * 常量池、字段和方法数据，以及
     * 方法和构造函数的代码。
     */
    NON_HEAP("非堆内存");

    private final String description;

    private MemoryType(String s) {
        this.description = s;
    }

    /**
     * 返回此 <tt>MemoryType</tt> 的字符串表示形式。
     * @return 此 <tt>MemoryType</tt> 的字符串表示形式。
     */
    public String toString() {
        return description;
    }

    private static final long serialVersionUID = 6992337162326171013L;
}
