
/*
 * 版权所有 (c) 1996, 2013，Oracle 和/或其附属公司。保留所有权利。
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
 * 通过实现 java.io.Serializable 接口，类的可序列化功能被启用。未实现此接口的类将不会有任何状态被序列化或反序列化。
 * 所有可序列化类的子类型本身也是可序列化的。序列化接口没有方法或字段，仅用于标识可序列化的语义。 <p>
 *
 * 为了允许非可序列化类的子类型被序列化，子类型可以负责保存和恢复超类的公共、受保护的以及（如果可访问）包级字段的状态。
 * 子类型仅在它扩展的类具有可访问的无参构造函数来初始化类的状态时才能承担此责任。如果这不是情况，则声明类为可序列化是错误的。
 * 此错误将在运行时被检测。 <p>
 *
 * 在反序列化期间，非可序列化类的字段将使用类的公共或受保护的无参构造函数初始化。无参构造函数必须对可序列化的子类可访问。
 * 可序列化子类的字段将从流中恢复。 <p>
 *
 * 在遍历图时，可能会遇到不支持 Serializable 接口的对象。在这种情况下，将抛出 NotSerializableException 并标识非可序列化对象的类。 <p>
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
 * <p>writeObject 方法负责为其特定类写入对象的状态，以便相应的 readObject 方法可以恢复它。
 * 可以通过调用 out.defaultWriteObject 来调用保存对象字段的默认机制。该方法不需要关心其超类或子类的状态。
 * 通过使用 writeObject 方法或 DataOutput 支持的原始数据类型方法将各个字段写入 ObjectOutputStream 来保存状态。
 *
 * <p>readObject 方法负责从流中读取并恢复类的字段。它可以调用 in.defaultReadObject 来调用恢复对象的非静态和非瞬态字段的默认机制。
 * defaultReadObject 方法使用流中的信息为流中保存的对象的字段分配与当前对象中相应命名的字段。这处理了类已演进以添加新字段的情况。
 * 该方法不需要关心其超类或子类的状态。通过使用 writeObject 方法或 DataOutput 支持的原始数据类型方法将各个字段写入 ObjectOutputStream 来保存状态。
 *
 * <p>readObjectNoData 方法负责在序列化流未将给定类列为正在反序列化的对象的超类时，初始化其特定类的对象的状态。
 * 这可能发生在接收方使用的反序列化实例的类版本与发送方不同，且接收方的版本扩展了发送方版本未扩展的类的情况下。
 * 这也可能发生在序列化流被篡改的情况下；因此，readObjectNoData 对于在“敌对”或不完整的源流中正确初始化反序列化对象很有用。
 *
 * <p>需要在将对象写入流时指定替代对象的可序列化类应实现具有以下确切签名的特殊方法：
 *
 * <PRE>
 * ANY-ACCESS-MODIFIER Object writeReplace() throws ObjectStreamException;
 * </PRE><p>
 *
 * 如果存在此方法并且可以从正在序列化的对象的类中定义的方法访问，则序列化将调用此 writeReplace 方法。
 * 因此，该方法可以具有私有、受保护和包私有访问。此方法的子类访问遵循 java 访问规则。 <p>
 *
 * 需要在从流中读取其实例时指定替代对象的类应实现具有以下确切签名的特殊方法：
 *
 * <PRE>
 * ANY-ACCESS-MODIFIER Object readResolve() throws ObjectStreamException;
 * </PRE><p>
 *
 * 此 readResolve 方法遵循与 writeReplace 相同的调用规则和访问规则。<p>
 *
 * 序列化运行时为每个可序列化类关联一个版本号，称为 serialVersionUID，该版本号在反序列化期间用于验证发送方和接收方的序列化对象已加载的类是否兼容。
 * 如果接收方已加载的类的 serialVersionUID 与发送方相应类的 serialVersionUID 不同，则反序列化将导致 {@link InvalidClassException}。
 * 可序列化类可以通过声明名为 <code>"serialVersionUID"</code> 的字段来显式声明自己的 serialVersionUID，该字段必须是静态的、最终的，并且类型为 <code>long</code>：
 *
 * <PRE>
 * ANY-ACCESS-MODIFIER static final long serialVersionUID = 42L;
 * </PRE>
 *
 * 如果可序列化类没有显式声明 serialVersionUID，则序列化运行时将根据类的各个方面计算该类的默认 serialVersionUID 值，如 Java(TM) 对象序列化规范中所述。
 * 但是，强烈建议所有可序列化类显式声明 serialVersionUID 值，因为默认的 serialVersionUID 计算对类细节非常敏感，可能会因编译器实现的不同而变化，从而导致意外的
 * <code>InvalidClassException</code>。因此，为了保证不同 java 编译器实现之间一致的 serialVersionUID 值，可序列化类必须声明显式的 serialVersionUID 值。
 * 还强烈建议显式的 serialVersionUID 声明尽可能使用 <code>private</code> 修饰符，因为这样的声明仅适用于立即声明的类——serialVersionUID 字段作为继承成员是无用的。
 * 数组类不能声明显式的 serialVersionUID，因此它们总是具有默认计算值，但对于数组类，匹配的 serialVersionUID 值的要求被豁免。
 *
 * @author 未署名
 * @see java.io.ObjectOutputStream
 * @see java.io.ObjectInputStream
 * @see java.io.ObjectOutput
 * @see java.io.ObjectInput
 * @see java.io.Externalizable
 * @since JDK1.1
 */
public interface Serializable {
}
