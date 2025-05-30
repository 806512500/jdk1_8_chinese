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
 * 通过实现 java.io.Serializable 接口来启用类的序列化。不实现此接口的类将不会有任何状态被序列化或反序列化。所有可序列化的类的子类型本身也是可序列化的。序列化接口没有方法或字段，仅用于标识可序列化的语义。 <p>
 *
 * 为了允许非序列化类的子类型被序列化，子类型可以负责保存和恢复其超类型的公共、受保护的以及（如果可访问）包字段的状态。子类型仅在它所扩展的类具有可访问的无参数构造函数来初始化类的状态时，才能承担此责任。如果情况并非如此，则声明类为可序列化是错误的。此错误将在运行时被检测。 <p>
 *
 * 在反序列化期间，非序列化类的字段将使用类的公共或受保护的无参数构造函数进行初始化。无参数构造函数必须对可序列化的子类可访问。可序列化的子类的字段将从流中恢复。 <p>
 *
 * 在遍历图时，可能会遇到不支持 Serializable 接口的对象。在这种情况下，将抛出 NotSerializableException，并标识非序列化对象的类。 <p>
 *
 * 需要在序列化和反序列化过程中进行特殊处理的类必须实现具有以下确切签名的特殊方法：
 *
 * <PRE>
 * private void writeObject(java.io.ObjectOutputStream out)
 *     throws IOException
 * private void readObject(java.io.ObjectInputStream in)
 *     throws IOException, ClassNotFoundException;
 * private void readObjectNoData()
 *     throws ObjectStreamException;
 * </PRE>
 *
 * <p>writeObject 方法负责为其特定类写入对象的状态，以便相应的 readObject 方法可以恢复它。可以通过调用 out.defaultWriteObject 来调用保存对象字段的默认机制。该方法不需要关心其超类或子类的状态。状态通过使用 writeObject 方法或 DataOutput 支持的原始数据类型方法将各个字段写入 ObjectOutputStream 来保存。
 *
 * <p>readObject 方法负责从流中读取并恢复类的字段。它可以调用 in.defaultReadObject 来调用恢复对象的非静态和非瞬态字段的默认机制。defaultReadObject 方法使用流中的信息将流中保存的对象的字段分配给当前对象中相应名称的字段。这处理了类演进以添加新字段的情况。该方法不需要关心其超类或子类的状态。状态通过使用 writeObject 方法或 DataOutput 支持的原始数据类型方法将各个字段写入 ObjectOutputStream 来保存。
 *
 * <p>readObjectNoData 方法负责在序列化流未将给定类列为对象的超类时初始化该类的特定状态。这可能发生在接收方使用的反序列化实例的类版本与发送方不同，且接收方的版本扩展了发送方版本未扩展的类的情况下。如果序列化流被篡改，也可能发生这种情况；因此，readObjectNoData 对于在“敌对”或不完整的源流中正确初始化反序列化对象非常有用。
 *
 * <p>需要在将对象写入流时指定替代对象的可序列化类应实现具有以下确切签名的特殊方法：
 *
 * <PRE>
 * ANY-ACCESS-MODIFIER Object writeReplace() throws ObjectStreamException;
 * </PRE><p>
 *
 * 如果该方法存在并且从正在序列化的对象的类中定义的方法可访问，则序列化将调用此 writeReplace 方法。因此，该方法可以具有私有、受保护和包私有访问权限。子类对这个方法的访问遵循 Java 的访问规则。 <p>
 *
 * 需要在从流中读取其实例时指定替代对象的类应实现具有以下确切签名的特殊方法：
 *
 * <PRE>
 * ANY-ACCESS-MODIFIER Object readResolve() throws ObjectStreamException;
 * </PRE><p>
 *
 * 此 readResolve 方法遵循与 writeReplace 相同的调用规则和访问规则。<p>
 *
 * 序列化运行时会为每个可序列化类关联一个版本号，称为 serialVersionUID，该版本号在反序列化时用于验证发送方和接收方的序列化对象已加载的类是否兼容。如果接收方已加载的类的 serialVersionUID 与发送方相应类的 serialVersionUID 不同，则反序列化将导致 {@link InvalidClassException}。可序列化类可以通过声明名为 <code>"serialVersionUID"</code> 的字段来显式声明自己的 serialVersionUID，该字段必须是静态的、最终的，并且类型为 <code>long</code>：
 *
 * <PRE>
 * ANY-ACCESS-MODIFIER static final long serialVersionUID = 42L;
 * </PRE>
 *
 * 如果可序列化类没有显式声明 serialVersionUID，则序列化运行时将根据类的各个方面计算该类的默认 serialVersionUID 值，如 Java(TM) 对象序列化规范中所述。但是，强烈建议所有可序列化类显式声明 serialVersionUID 值，因为默认的 serialVersionUID 计算对类的细节非常敏感，可能会因编译器实现的不同而有所不同，从而导致意外的 <code>InvalidClassException</code>。因此，为了保证在不同 Java 编译器实现中具有相同的 serialVersionUID 值，可序列化类必须显式声明一个 serialVersionUID 值。还强烈建议尽可能使用 <code>private</code> 修饰符声明显式的 serialVersionUID，因为这样的声明仅适用于立即声明的类——serialVersionUID 字段作为继承成员是无用的。数组类不能声明显式的 serialVersionUID，因此它们总是具有默认计算值，但对于数组类，匹配的 serialVersionUID 值的要求被豁免。
 *
 * @author  未署名
 * @see java.io.ObjectOutputStream
 * @see java.io.ObjectInputStream
 * @see java.io.ObjectOutput
 * @see java.io.ObjectInput
 * @see java.io.Externalizable
 * @since   JDK1.1
 */
public interface Serializable {
}
