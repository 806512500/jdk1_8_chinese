/*
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
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
package java.rmi.server;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.security.AccessController;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;
import sun.security.action.GetPropertyAction;

/**
 * 一个 <code>ObjID</code> 用于标识导出到 RMI 运行时的远程对象。当一个远程对象被导出时，它会被分配一个对象标识符，这取决于用于导出的 API，可以是隐式或显式分配。
 *
 * <p>{@link #ObjID()} 构造函数可以用于生成一个唯一的对象标识符。这样的 <code>ObjID</code> 在生成它的主机上是唯一的。
 *
 * {@link #ObjID(int)} 构造函数可以用于创建一个“知名”的对象标识符。知名 <code>ObjID</code> 的作用范围取决于它导出到的 RMI 运行时。
 *
 * <p>一个 <code>ObjID</code> 实例包含一个对象编号（类型为 <code>long</code>）和一个地址空间标识符（类型为 {@link UID}）。在唯一的 <code>ObjID</code> 中，地址空间标识符在给定主机上是唯一的。在知名的 <code>ObjID</code> 中，地址空间标识符等同于调用 {@link UID#UID(short)} 构造函数并传入值零返回的标识符。
 *
 * <p>如果系统属性 <code>java.rmi.server.randomIDs</code> 被定义为等于字符串 <code>"true"</code>（不区分大小写），则 {@link #ObjID()} 构造函数将使用一个加密强度的随机数生成器来选择返回的 <code>ObjID</code> 的对象编号。
 *
 * @author      Ann Wollrath
 * @author      Peter Jones
 * @since       JDK1.1
 */
public final class ObjID implements Serializable {

    /** 用于标识注册表的知名 <code>ObjID</code> 的对象编号。 */
    public static final int REGISTRY_ID = 0;

    /** 用于标识激活器的知名 <code>ObjID</code> 的对象编号。 */
    public static final int ACTIVATOR_ID = 1;

    /**
     * 用于标识分布式垃圾收集器的知名 <code>ObjID</code> 的对象编号。
     */
    public static final int DGC_ID = 2;

    /** 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = -6386392263968365220L;

    private static final AtomicLong nextObjNum = new AtomicLong(0);
    private static final UID mySpace = new UID();
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * @serial 对象编号
     * @see #hashCode
     */
    private final long objNum;

    /**
     * @serial 地址空间标识符（在主机上唯一）
     */
    private final UID space;

    /**
     * 生成一个唯一的对象标识符。
     *
     * <p>如果系统属性 <code>java.rmi.server.randomIDs</code> 被定义为等于字符串 <code>"true"</code>（不区分大小写），则此构造函数将使用一个加密强度的随机数生成器来选择返回的 <code>ObjID</code> 的对象编号。
     */
    public ObjID() {
        /*
         * 如果生成随机对象编号，创建一个新的 UID 以确保唯一性；否则，使用共享的 UID，因为顺序的对象编号已经确保了唯一性。
         */
        if (useRandomIDs()) {
            space = new UID();
            objNum = secureRandom.nextLong();
        } else {
            space = mySpace;
            objNum = nextObjNum.getAndIncrement();
        }
    }

    /**
     * 创建一个“知名”的对象标识符。
     *
     * <p>通过此构造函数创建的 <code>ObjID</code> 不会与通过无参构造函数生成的任何 <code>ObjID</code> 冲突。
     *
     * @param   objNum 知名对象标识符的对象编号
     */
    public ObjID(int objNum) {
        space = new UID((short) 0);
        this.objNum = objNum;
    }

    /**
     * 从流中读取数据构造一个对象标识符。
     */
    private ObjID(long objNum, UID space) {
        this.objNum = objNum;
        this.space = space;
    }

    /**
     * 将此 <code>ObjID</code> 的二进制表示形式写入 <code>ObjectOutput</code> 实例。
     *
     * <p>具体来说，此方法首先调用给定流的 {@link ObjectOutput#writeLong(long)} 方法，将此对象标识符的对象编号写入，然后通过调用其 {@link UID#write(DataOutput)} 方法将地址空间标识符写入流。
     *
     * @param   out 要写入此 <code>ObjID</code> 的 <code>ObjectOutput</code> 实例
     *
     * @throws  IOException 如果在执行此操作时发生 I/O 错误
     */
    public void write(ObjectOutput out) throws IOException {
        out.writeLong(objNum);
        space.write(out);
    }

    /**
     * 从 <code>ObjectInput</code> 实例中解组二进制表示形式，构造并返回一个新的 <code>ObjID</code> 实例。
     *
     * <p>具体来说，此方法首先调用给定流的 {@link ObjectInput#readLong()} 方法读取对象编号，然后通过调用 {@link UID#read(DataInput)} 方法读取地址空间标识符，然后创建并返回一个包含从流中读取的对象编号和地址空间标识符的新 <code>ObjID</code> 实例。
     *
     * @param   in 要从中读取 <code>ObjID</code> 的 <code>ObjectInput</code> 实例
     *
     * @return  解组的 <code>ObjID</code> 实例
     *
     * @throws  IOException 如果在执行此操作时发生 I/O 错误
     */
    public static ObjID read(ObjectInput in) throws IOException {
        long num = in.readLong();
        UID space = UID.read(in);
        return new ObjID(num, space);
    }

    /**
     * 返回此对象标识符的哈希码值，即对象编号。
     *
     * @return  此对象标识符的哈希码值
     */
    public int hashCode() {
        return (int) objNum;
    }

    /**
     * 将指定的对象与此 <code>ObjID</code> 进行比较以确定是否相等。
     *
     * 此方法仅当指定的对象是一个具有与本对象相同对象编号和地址空间标识符的 <code>ObjID</code> 实例时返回 <code>true</code>。
     *
     * @param   obj 要与此 <code>ObjID</code> 比较的对象
     *
     * @return  如果给定对象与此对象等价，则返回 <code>true</code>，否则返回 <code>false</code>
     */
    public boolean equals(Object obj) {
        if (obj instanceof ObjID) {
            ObjID id = (ObjID) obj;
            return objNum == id.objNum && space.equals(id.space);
        } else {
            return false;
        }
    }

    /**
     * 返回此对象标识符的字符串表示形式。
     *
     * @return  此对象标识符的字符串表示形式
     */
    /*
     * 仅当地址空间标识符不表示本地地址空间（或设置了 randomIDs 属性）时，地址空间标识符才包含在字符串表示形式中。
     */
    public String toString() {
        return "[" + (space.equals(mySpace) ? "" : space + ", ") +
            objNum + "]";
    }

    private static boolean useRandomIDs() {
        String value = AccessController.doPrivileged(
            new GetPropertyAction("java.rmi.server.randomIDs"));
        return value == null ? true : Boolean.parseBoolean(value);
    }
}
