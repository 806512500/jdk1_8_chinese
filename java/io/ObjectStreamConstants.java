/*
 * 版权所有 (c) 1996, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.io;

/**
 * 写入对象序列化流中的常量。
 *
 * @author 未署名
 * @since JDK 1.1
 */
public interface ObjectStreamConstants {

    /**
     * 写入流头的魔数。
     */
    final static short STREAM_MAGIC = (short)0xaced;

    /**
     * 写入流头的版本号。
     */
    final static short STREAM_VERSION = 5;

    /* 流中的每个项目前面都有一个标签
     */

    /**
     * 第一个标签值。
     */
    final static byte TC_BASE = 0x70;

    /**
     * 空对象引用。
     */
    final static byte TC_NULL =         (byte)0x70;

    /**
     * 引用已写入流中的对象。
     */
    final static byte TC_REFERENCE =    (byte)0x71;

    /**
     * 新的类描述符。
     */
    final static byte TC_CLASSDESC =    (byte)0x72;

    /**
     * 新对象。
     */
    final static byte TC_OBJECT =       (byte)0x73;

    /**
     * 新字符串。
     */
    final static byte TC_STRING =       (byte)0x74;

    /**
     * 新数组。
     */
    final static byte TC_ARRAY =        (byte)0x75;

    /**
     * 类引用。
     */
    final static byte TC_CLASS =        (byte)0x76;

    /**
     * 可选数据块。跟随标签的字节表示此块数据中的字节数。
     */
    final static byte TC_BLOCKDATA =    (byte)0x77;

    /**
     * 对象的可选块数据块的结束。
     */
    final static byte TC_ENDBLOCKDATA = (byte)0x78;

    /**
     * 重置流上下文。流中写入的所有句柄都被重置。
     */
    final static byte TC_RESET =        (byte)0x79;

    /**
     * 长数据块。跟随标签的长整型表示此块数据中的字节数。
     */
    final static byte TC_BLOCKDATALONG= (byte)0x7A;

    /**
     * 写入期间的异常。
     */
    final static byte TC_EXCEPTION =    (byte)0x7B;

    /**
     * 长字符串。
     */
    final static byte TC_LONGSTRING =   (byte)0x7C;

    /**
     * 新的代理类描述符。
     */
    final static byte TC_PROXYCLASSDESC =       (byte)0x7D;

    /**
     * 新的枚举常量。
     * @since 1.5
     */
    final static byte TC_ENUM =         (byte)0x7E;

    /**
     * 最后一个标签值。
     */
    final static byte TC_MAX =          (byte)0x7E;

    /**
     * 要分配的第一个线程句柄。
     */
    final static int baseWireHandle = 0x7e0000;


    /******************************************************/
    /* ObjectStreamClass 标志的位掩码。*/

    /**
     * ObjectStreamClass 标志的位掩码。表示可序列化类定义了自己的 writeObject 方法。
     */
    final static byte SC_WRITE_METHOD = 0x01;

    /**
     * ObjectStreamClass 标志的位掩码。表示以块数据模式写入的外部化数据。
     * 为 PROTOCOL_VERSION_2 添加。
     *
     * @see #PROTOCOL_VERSION_2
     * @since 1.2
     */
    final static byte SC_BLOCK_DATA = 0x08;

    /**
     * ObjectStreamClass 标志的位掩码。表示类是可序列化的。
     */
    final static byte SC_SERIALIZABLE = 0x02;

    /**
     * ObjectStreamClass 标志的位掩码。表示类是外部化的。
     */
    final static byte SC_EXTERNALIZABLE = 0x04;

    /**
     * ObjectStreamClass 标志的位掩码。表示类是枚举类型。
     * @since 1.5
     */
    final static byte SC_ENUM = 0x10;


    /* *******************************************************************/
    /* 安全权限 */

    /**
     * 在序列化/反序列化期间启用一个对象替换为另一个对象。
     *
     * @see java.io.ObjectOutputStream#enableReplaceObject(boolean)
     * @see java.io.ObjectInputStream#enableResolveObject(boolean)
     * @since 1.2
     */
    final static SerializablePermission SUBSTITUTION_PERMISSION =
                           new SerializablePermission("enableSubstitution");

    /**
     * 启用重写 readObject 和 writeObject。
     *
     * @see java.io.ObjectOutputStream#writeObjectOverride(Object)
     * @see java.io.ObjectInputStream#readObjectOverride()
     * @since 1.2
     */
    final static SerializablePermission SUBCLASS_IMPLEMENTATION_PERMISSION =
                    new SerializablePermission("enableSubclassImplementation");
   /**
    * 一个流协议版本。 <p>
    *
    * 调用此方法后，所有外部化数据均以 JDK 1.1 外部数据格式写入。此版本用于写入包含外部化数据的流，这些数据可以被预 JDK 1.1.6 的 JVM 读取。
    *
    * @see java.io.ObjectOutputStream#useProtocolVersion(int)
    * @since 1.2
    */
    public final static int PROTOCOL_VERSION_1 = 1;


   /**
    * 一个流协议版本。 <p>
    *
    * 此协议由 JVM 1.2 写入。
    *
    * 外部化数据以块数据模式写入，并以 TC_ENDBLOCKDATA 结束。外部化类描述符标志启用 SC_BLOCK_DATA。JVM 1.1.6 及更高版本可以读取此格式更改。
    *
    * 启用将非可序列化类描述符写入流中。非可序列化类的 serialVersionUID 设置为 0L。
    *
    * @see java.io.ObjectOutputStream#useProtocolVersion(int)
    * @see #SC_BLOCK_DATA
    * @since 1.2
    */
    public final static int PROTOCOL_VERSION_2 = 2;
}
