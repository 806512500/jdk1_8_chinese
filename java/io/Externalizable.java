/*
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
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

import java.io.ObjectOutput;
import java.io.ObjectInput;

/**
 * 仅在序列化流中写入 Externalizable 实例的类的标识，并且由该类负责保存和恢复其实例的内容。
 *
 * Externalizable 接口的 writeExternal 和 readExternal 方法由类实现，以使类完全控制对象及其超类型的流的格式和内容。
 * 这些方法必须显式地与超类型协调以保存其状态。这些方法覆盖了 writeObject 和 readObject 方法的自定义实现。<br>
 *
 * 对象序列化使用 Serializable 和 Externalizable 接口。对象持久化机制也可以使用它们。每个要存储的对象都会测试 Externalizable 接口。
 * 如果对象支持 Externalizable，则调用 writeExternal 方法。如果对象不支持 Externalizable 但实现了 Serializable，则使用 ObjectOutputStream 保存对象。<br>
 * 当 Externalizable 对象被重建时，使用公共无参构造函数创建实例，然后调用 readExternal 方法。Serializable 对象通过从 ObjectInputStream 读取来恢复。<br>
 *
 * Externalizable 实例可以通过 writeReplace 和 readResolve 方法（在 Serializable 接口中记录）指定替代对象。<br>
 *
 * @author  未署名
 * @see java.io.ObjectOutputStream
 * @see java.io.ObjectInputStream
 * @see java.io.ObjectOutput
 * @see java.io.ObjectInput
 * @see java.io.Serializable
 * @since   JDK1.1
 */
public interface Externalizable extends java.io.Serializable {
    /**
     * 对象实现 writeExternal 方法通过调用 DataOutput 的方法来保存其原始值，或调用 ObjectOutput 的 writeObject 方法来保存对象、字符串和数组。
     *
     * @serialData 覆盖方法应使用此标签描述此 Externalizable 对象的数据布局。
     *             列出元素类型的序列，如果可能，将元素与此 Externalizable 类的公共/受保护字段和/或方法相关联。
     *
     * @param out 要将对象写入的流
     * @exception IOException 包括可能发生的任何 I/O 异常
     */
    void writeExternal(ObjectOutput out) throws IOException;

    /**
     * 对象实现 readExternal 方法通过调用 DataInput 的方法来恢复其原始类型，以及调用 readObject 来恢复对象、字符串和数组。
     * readExternal 方法必须按 writeExternal 写入的相同顺序和相同类型读取值。
     *
     * @param in 从其中读取数据以恢复对象的流
     * @exception IOException 如果发生 I/O 错误
     * @exception ClassNotFoundException 如果无法找到正在恢复的对象的类。
     */
    void readExternal(ObjectInput in) throws IOException, ClassNotFoundException;
}
