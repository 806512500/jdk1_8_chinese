/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
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
     * 对已写入流中的对象的引用。
     */
    final static byte TC_REFERENCE =    (byte)0x71;

    /**
     * 新的类描述符。
     */
    final static byte TC_CLASSDESC =    (byte)0x72;

    /**
     * 新的对象。
     */
    final static byte TC_OBJECT =       (byte)0x73;

    /**
     * 新的字符串。
     */
    final static byte TC_STRING =       (byte)0x74;

    /**
     * 新的数组。
     */
    final static byte TC_ARRAY =        (byte)0x75;

    /**
     * 类的引用。
     */
    final static byte TC_CLASS =        (byte)0x76;

    /**
     * 可选数据块。标签后面的字节表示此块数据中的字节数。
     */
    final static byte TC_BLOCKDATA =    (byte)0x77;

    /**
     * 对象的可选数据块的结束。
     */
    final static byte TC_ENDBLOCKDATA = (byte)0x78;

    /**
     * 重置流上下文。流中写入的所有句柄都被重置。
     */
    final static byte TC_RESET =        (byte)0x79;

    /**
     * 长数据块。标签后面的长整型表示此块数据中的字节数。
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
    /* ObjectStreamClass 标志的位掩码*/

    /**
     * ObjectStreamClass 标志的位掩码。表示可序列化类定义了自己的 writeObject 方法。
     */
    final static byte SC_WRITE_METHOD = 0x01;

    /**
     * ObjectStreamClass 标志的位掩码。表示以块数据模式写入外部化数据。
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
     * 启用覆盖 readObject 和 writeObject。
     *
     * @see java.io.ObjectOutputStream#writeObjectOverride(Object)
     * @see java.io.ObjectInputStream#readObjectOverride()
     * @since 1.2
     */
    final static SerializablePermission SUBCLASS_IMPLEMENTATION_PERMISSION =
                    new SerializablePermission("enableSubclassImplementation");
   /**
    * 流协议版本。 <p>
    *
    * 调用此方法后，所有外部化数据都以 JDK 1.1 外部数据格式写入。此版本用于写入可以被
    * 预 JDK 1.1.6 JVM 读取的包含外部化数据的流。
    *
    * @see java.io.ObjectOutputStream#useProtocolVersion(int)
    * @since 1.2
    */
    public final static int PROTOCOL_VERSION_1 = 1;


   /**
    * 流协议版本。 <p>
    *
    * 此协议由 JVM 1.2 写入。
    *
    * 外部化数据以块数据模式写入，并以 TC_ENDBLOCKDATA 结束。外部化类描述符
    * 标志已启用 SC_BLOCK_DATA。JVM 1.1.6 及更高版本可以读取此格式更改。
    *
    * 启用将非可序列化类描述符写入流。非可序列化类的 serialVersionUID
    * 设置为 0L。
    *
    * @see java.io.ObjectOutputStream#useProtocolVersion(int)
    * @see #SC_BLOCK_DATA
    * @since 1.2
    */
    public final static int PROTOCOL_VERSION_2 = 2;
}
