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

package java.nio;


/**
 * 用于字节顺序的类型安全枚举。
 *
 * @author Mark Reinhold
 * @author JSR-51 专家组
 * @since 1.4
 */

public final class ByteOrder {

    private String name;

    private ByteOrder(String name) {
        this.name = name;
    }

    /**
     * 表示大端字节顺序的常量。在这种顺序中，多字节值的字节从最高有效位到最低有效位排列。
     */
    public static final ByteOrder BIG_ENDIAN
        = new ByteOrder("BIG_ENDIAN");

    /**
     * 表示小端字节顺序的常量。在这种顺序中，多字节值的字节从最低有效位到最高有效位排列。
     */
    public static final ByteOrder LITTLE_ENDIAN
        = new ByteOrder("LITTLE_ENDIAN");

    /**
     * 获取底层平台的本机字节顺序。
     *
     * <p> 此方法定义的目的是为了让性能敏感的 Java 代码可以使用与硬件相同的字节顺序分配直接缓冲区。
     * 当使用这样的缓冲区时，本地代码库通常更高效。 </p>
     *
     * @return 运行此 Java 虚拟机的硬件的本机字节顺序
     */
    public static ByteOrder nativeOrder() {
        return Bits.byteOrder();
    }

    /**
     * 构造描述此对象的字符串。
     *
     * <p> 此方法为 {@link #BIG_ENDIAN} 返回字符串 <tt>"BIG_ENDIAN"</tt>，为 {@link #LITTLE_ENDIAN} 返回字符串 <tt>"LITTLE_ENDIAN"</tt>。
     * </p>
     *
     * @return 指定的字符串
     */
    public String toString() {
        return name;
    }

}
