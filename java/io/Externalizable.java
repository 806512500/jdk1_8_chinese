/*
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

import java.io.ObjectOutput;
import java.io.ObjectInput;

/**
 * 只有 Externalizable 实例的类标识会被写入序列化流，类负责保存和恢复其实例的内容。
 *
 * Externalizable 接口的 writeExternal 和 readExternal 方法由类实现，以使类能够完全控制对象及其超类型的流的格式和内容。
 * 这些方法必须显式地与超类型协调以保存其状态。这些方法覆盖了 writeObject 和 readObject 方法的自定义实现。<br>
 *
 * 对象序列化使用 Serializable 和 Externalizable 接口。对象持久化机制也可以使用它们。每个要存储的对象都会被测试是否实现了 Externalizable 接口。
 * 如果对象支持 Externalizable，则调用 writeExternal 方法。如果对象不支持 Externalizable 但实现了 Serializable，则使用 ObjectOutputStream 保存对象。<br>
 * 当 Externalizable 对象被重建时，使用公共无参构造函数创建一个实例，然后调用 readExternal 方法。Serializable 对象通过从 ObjectInputStream 读取来恢复。<br>
 *
 * Externalizable 实例可以通过 Serializable 接口中记录的 writeReplace 和 readResolve 方法指定替代对象。<br>
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
     * 对象实现 writeExternal 方法以通过调用 DataOutput 的方法来保存其原始值，或通过调用 ObjectOutput 的 writeObject 方法来保存对象、字符串和数组。
     *
     * @serialData 覆盖方法应使用此标签描述此 Externalizable 对象的数据布局。
     *             列出元素类型的序列，如果可能，将元素与这个 Externalizable 类的公共/受保护字段和/或方法关联起来。
     *
     * @param out 要将对象写入的流
     * @exception IOException 包括可能发生的任何 I/O 异常
     */
    void writeExternal(ObjectOutput out) throws IOException;

    /**
     * 对象实现 readExternal 方法以通过调用 DataInput 的方法来恢复其原始类型，以及通过调用 readObject 来恢复对象、字符串和数组。
     * readExternal 方法必须按与 writeExternal 写入相同的顺序和类型读取值。
     *
     * @param in 从其中读取数据以恢复对象的流
     * @exception IOException 如果发生 I/O 错误
     * @exception ClassNotFoundException 如果无法找到正在恢复的对象的类。
     */
    void readExternal(ObjectInput in) throws IOException, ClassNotFoundException;
}
