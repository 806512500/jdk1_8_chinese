
/*
 * 版权所有 (c) 1996, 2011, Oracle 和/或其附属公司。保留所有权利。
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

import java.rmi.Remote;
import java.rmi.NoSuchObjectException;
import java.lang.reflect.Proxy;
import sun.rmi.server.Util;

/**
 * <code>RemoteObject</code> 类实现了远程对象的
 * <code>java.lang.Object</code> 行为。通过实现 hashCode、equals 和 toString 方法，
 * <code>RemoteObject</code> 提供了 Object 的远程语义。
 *
 * @author      Ann Wollrath
 * @author      Laird Dornin
 * @author      Peter Jones
 * @since       JDK1.1
 */
public abstract class RemoteObject implements Remote, java.io.Serializable {

    /** 远程对象的远程引用。 */
    transient protected RemoteRef ref;

    /** 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = -3215090123894869218L;

    /**
     * 创建一个远程对象。
     */
    protected RemoteObject() {
        ref = null;
    }

    /**
     * 创建一个远程对象，并使用指定的远程引用进行初始化。
     * @param newref 远程引用
     */
    protected RemoteObject(RemoteRef newref) {
        ref = newref;
    }

    /**
     * 返回远程对象的远程引用。
     *
     * <p>注意：此方法返回的对象可能是特定实现类的实例。通过其自定义的
     * <code>writeObject</code> 和 <code>readObject</code> 方法，<code>RemoteObject</code>
     * 类确保其实例的远程引用的序列化可移植性。不应在 <code>RemoteObject</code> 包装实例之外
     * 序列化 <code>RemoteRef</code> 实例，否则结果可能不可移植。
     *
     * @return 远程对象的远程引用
     * @since 1.2
     */
    public RemoteRef getRef() {
        return ref;
    }

    /**
     * 返回作为参数传递的远程对象 <code>obj</code> 的存根。此操作仅在
     * 对象导出后有效。
     * @param obj 需要存根的远程对象
     * @return 远程对象 <code>obj</code> 的存根。
     * @exception NoSuchObjectException 如果找不到远程对象的存根。
     * @since 1.2
     */
    public static Remote toStub(Remote obj) throws NoSuchObjectException {
        if (obj instanceof RemoteStub ||
            (obj != null &&
             Proxy.isProxyClass(obj.getClass()) &&
             Proxy.getInvocationHandler(obj) instanceof
             RemoteObjectInvocationHandler))
        {
            return obj;
        } else {
            return sun.rmi.transport.ObjectTable.getStub(obj);
        }
    }

    /**
     * 返回远程对象的哈希码。两个引用相同远程对象的远程对象存根将具有相同的哈希码
     * （以支持远程对象作为哈希表中的键）。
     *
     * @see             java.util.Hashtable
     */
    public int hashCode() {
        return (ref == null) ? super.hashCode() : ref.remoteHashCode();
    }

    /**
     * 比较两个远程对象是否相等。
     * 返回一个布尔值，指示此远程对象是否与指定的对象相等。此方法用于将远程对象存储在哈希表中。
     * 如果指定的对象本身不是 <code>RemoteObject</code> 的实例，则此方法通过调用参数的
     * <code>equals</code> 方法并将此远程对象作为参数传递来委托处理。
     * @param   obj     要比较的对象
     * @return  如果这些对象相等则返回 true；否则返回 false。
     * @see             java.util.Hashtable
     */
    public boolean equals(Object obj) {
        if (obj instanceof RemoteObject) {
            if (ref == null) {
                return obj == this;
            } else {
                return ref.remoteEquals(((RemoteObject)obj).ref);
            }
        } else if (obj != null) {
            /*
             * 修复 4099660：如果对象不是 <code>RemoteObject</code> 的实例，
             * 使用其 <code>equals</code> 方法的结果，以支持远程对象实现类
             * 不扩展 <code>RemoteObject</code> 但仍希望支持与存根对象相等的情况。
             */
            return obj.equals(this);
        } else {
            return false;
        }
    }

    /**
     * 返回表示此远程对象值的字符串。
     */
    public String toString() {
        String classname = Util.getUnqualifiedName(getClass());
        return (ref == null) ? classname :
            classname + "[" + ref.remoteToString() + "]";
    }

    /**
     * 用于自定义序列化的 <code>writeObject</code>。
     *
     * <p>此方法按如下方式写入此类的序列化形式：
     *
     * <p>调用此对象的 <code>ref</code> 字段的
     * {@link RemoteRef#getRefClass(java.io.ObjectOutput) getRefClass}
     * 方法以获取其外部引用类型名称。
     * 如果 <code>getRefClass</code> 返回的值是非 <code>null</code> 且长度大于零的字符串，
     * 则调用 <code>out</code> 的 <code>writeUTF</code> 方法，参数为 <code>getRefClass</code>
     * 返回的值，然后调用此对象的 <code>ref</code> 字段的 <code>writeExternal</code> 方法，
     * 参数为 <code>out</code>；否则，
     * 调用 <code>out</code> 的 <code>writeUTF</code> 方法，参数为零长度字符串 (<code>""</code>)，
     * 然后调用 <code>out</code> 的 <code>writeObject</code> 方法，参数为此对象的 <code>ref</code> 字段。
     *
     * @serialData
     *
     * 该类的序列化数据包括一个字符串（使用 <code>ObjectOutput.writeUTF</code> 写入），
     * 该字符串要么是包含的 <code>RemoteRef</code> 实例（<code>ref</code> 字段）的外部引用类型名称，
     * 要么是一个零长度字符串，后跟 <code>ref</code> 字段的外部形式（如果字符串长度非零，则由其
     * <code>writeExternal</code> 方法写入），或 <code>ref</code> 字段的序列化形式（如果字符串长度为零，
     * 则通过将其传递给序列化流的 <code>writeObject</code> 方法写入）。
     *
     * <p>如果此对象是
     * {@link RemoteStub} 或 {@link RemoteObjectInvocationHandler}
     * 的实例，并且是从任何 <code>UnicastRemoteObject.exportObject</code> 方法返回的，
     * 且未使用自定义套接字工厂，
     * 则外部引用类型名称为 <code>"UnicastRef"</code>。
     *
     * 如果此对象是
     * <code>RemoteStub</code> 或 <code>RemoteObjectInvocationHandler</code>
     * 的实例，并且是从任何 <code>UnicastRemoteObject.exportObject</code> 方法返回的，
     * 且使用了自定义套接字工厂，
     * 则外部引用类型名称为 <code>"UnicastRef2"</code>。
     *
     * 如果此对象是
     * <code>RemoteStub</code> 或 <code>RemoteObjectInvocationHandler</code>
     * 的实例，并且是从任何 <code>java.rmi.activation.Activatable.exportObject</code> 方法返回的，
     * 则外部引用类型名称为 <code>"ActivatableRef"</code>。
     *
     * 如果此对象是
     * <code>RemoteStub</code> 或 <code>RemoteObjectInvocationHandler</code>
     * 的实例，并且是从 <code>RemoteObject.toStub</code> 方法返回的（且传递给 <code>toStub</code> 的参数
     * 本身不是 <code>RemoteStub</code>），
     * 则外部引用类型名称取决于传递给 <code>toStub</code> 的远程对象的导出方式，如上所述。
     *
     * 如果此对象是
     * <code>RemoteStub</code> 或 <code>RemoteObjectInvocationHandler</code>
     * 的实例，并且是通过反序列化创建的，
     * 则外部引用类型名称与反序列化此对象时读取的相同。
     *
     * <p>如果此对象是
     * <code>java.rmi.server.UnicastRemoteObject</code> 的实例且未使用自定义套接字工厂，
     * 则外部引用类型名称为 <code>"UnicastServerRef"</code>。
     *
     * 如果此对象是
     * <code>UnicastRemoteObject</code> 的实例且使用了自定义套接字工厂，
     * 则外部引用类型名称为 <code>"UnicastServerRef2"</code>。
     *
     * <p>以下是必须由 <code>RemoteRef</code> 实现类的
     * <code>writeExternal</code> 方法写入并由 <code>readExternal</code> 方法读取的数据，
     * 对应于每个定义的外部引用类型名称：
     *
     * <p>对于 <code>"UnicastRef"</code>：
     *
     * <ul>
     *
     * <li>引用的远程对象的主机名，
     * 由 {@link java.io.ObjectOutput#writeUTF(String)} 写入
     *
     * <li>引用的远程对象的端口，
     * 由 {@link java.io.ObjectOutput#writeInt(int)} 写入
     *
     * <li>调用
     * {link java.rmi.server.ObjID#write(java.io.ObjectOutput)}
     * 在引用中包含的 <code>ObjID</code> 实例上写入的数据
     *
     * <li>布尔值 <code>false</code>，
     * 由 {@link java.io.ObjectOutput#writeBoolean(boolean)} 写入
     *
     * </ul>
     *
     * <p>对于 <code>"UnicastRef2"</code> 且 <code>null</code> 客户端套接字工厂：
     *
     * <ul>
     *
     * <li>字节值 <code>0x00</code>（表示 <code>null</code> 客户端套接字工厂），
     * 由 {@link java.io.ObjectOutput#writeByte(int)} 写入
     *
     * <li>引用的远程对象的主机名，
     * 由 {@link java.io.ObjectOutput#writeUTF(String)} 写入
     *
     * <li>引用的远程对象的端口，
     * 由 {@link java.io.ObjectOutput#writeInt(int)} 写入
     *
     * <li>调用
     * {link java.rmi.server.ObjID#write(java.io.ObjectOutput)}
     * 在引用中包含的 <code>ObjID</code> 实例上写入的数据
     *
     * <li>布尔值 <code>false</code>，
     * 由 {@link java.io.ObjectOutput#writeBoolean(boolean)} 写入
     *
     * </ul>
     *
     * <p>对于 <code>"UnicastRef2"</code> 且非 <code>null</code> 客户端套接字工厂：
     *
     * <ul>
     *
     * <li>字节值 <code>0x01</code>（表示非 <code>null</code> 客户端套接字工厂），
     * 由 {@link java.io.ObjectOutput#writeByte(int)} 写入
     *
     * <li>引用的远程对象的主机名，
     * 由 {@link java.io.ObjectOutput#writeUTF(String)} 写入
     *
     * <li>客户端套接字工厂（类型为 <code>java.rmi.server.RMIClientSocketFactory</code> 的对象），
     * 通过将其传递给流实例的 <code>writeObject</code> 调用写入
     *
     * <li>调用
     * {link java.rmi.server.ObjID#write(java.io.ObjectOutput)}
     * 在引用中包含的 <code>ObjID</code> 实例上写入的数据
     *
     * <li>布尔值 <code>false</code>，
     * 由 {@link java.io.ObjectOutput#writeBoolean(boolean)} 写入
     *
     * </ul>
     *
     * <p>对于 <code>"ActivatableRef"</code> 且 <code>null</code> 嵌套远程引用：
     *
     * <ul>
     *
     * <li><code>java.rmi.activation.ActivationID</code> 的实例，
     * 通过将其传递给流实例的 <code>writeObject</code> 调用写入
     *
     * <li>零长度字符串 (<code>""</code>)，
     * 由 {@link java.io.ObjectOutput#writeUTF(String)} 写入
     *
     * </ul>
     *
     * <p>对于 <code>"ActivatableRef"</code> 且非 <code>null</code> 嵌套远程引用：
     *
     * <ul>
     *
     * <li><code>java.rmi.activation.ActivationID</code> 的实例，
     * 通过将其传递给流实例的 <code>writeObject</code> 调用写入
     *
     * <li>嵌套远程引用的外部引用类型名称，必须为 <code>"UnicastRef2"</code>，
     * 由 {@link java.io.ObjectOutput#writeUTF(String)} 写入
     *
     * <li>嵌套远程引用的外部形式，通过调用其 <code>writeExternal</code> 方法并传递流实例写入
     * （参见 <code>"UnicastRef2"</code> 的外部形式描述）
     *
     * </ul>
     *
     * <p>对于 <code>"UnicastServerRef"</code> 和 <code>"UnicastServerRef2"</code>，
     * <code>writeExternal</code> 方法不写入任何数据，<code>readExternal</code> 方法也不读取任何数据。
     */
    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException, java.lang.ClassNotFoundException
    {
        if (ref == null) {
            throw new java.rmi.MarshalException("Invalid remote object");
        } else {
            String refClassName = ref.getRefClass(out);
            if (refClassName == null || refClassName.length() == 0) {
                /*
                 * 未指定引用类名称，因此序列化远程引用。
                 */
                out.writeUTF("");
                out.writeObject(ref);
            } else {
                /*
                 * 指定了内置引用类，因此委托给引用以写入其外部形式。
                 */
                out.writeUTF(refClassName);
                ref.writeExternal(out);
            }
        }
    }

                /**
     * 自定义序列化的 <code>readObject</code> 方法。
     *
     * <p>此方法按如下方式读取此类对象的序列化形式：
     *
     * <p>调用 <code>in</code> 上的 <code>readUTF</code> 方法来读取要填充到此对象的 <code>ref</code> 字段中的 <code>RemoteRef</code>
     * 实例的外部引用类型名称。如果 <code>readUTF</code> 返回的字符串长度为零，则调用 <code>in</code> 上的 <code>readObject</code> 方法，
     * 并将 <code>readObject</code> 返回的值转换为 <code>RemoteRef</code>，然后将此对象的 <code>ref</code> 字段设置为该值。
     * 否则，此对象的 <code>ref</code> 字段将设置为一个 <code>RemoteRef</code> 实例，该实例是由与 <code>readUTF</code> 返回的外部引用类型名称
     * 对应的实现特定类创建的，然后调用此对象的 <code>ref</code> 字段上的 <code>readExternal</code> 方法。
     *
     * <p>如果外部引用类型名称为 <code>"UnicastRef"</code>、<code>"UnicastServerRef"</code>、
     * <code>"UnicastRef2"</code>、<code>"UnicastServerRef2"</code> 或 <code>"ActivatableRef"</code>，
     * 必须找到一个对应的实现特定类，并且其 <code>readExternal</code> 方法必须读取该外部引用类型名称的序列化数据，如本类的 <b>serialData</b>
     * 文档中指定的那样。如果外部引用类型名称为任何其他字符串（长度非零），则将抛出 <code>ClassNotFoundException</code>，
     * 除非实现提供了与该外部引用类型名称对应的实现特定类，在这种情况下，此对象的 <code>ref</code> 字段将设置为该实现特定类的实例。
     */
    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, java.lang.ClassNotFoundException
    {
        String refClassName = in.readUTF();
        if (refClassName == null || refClassName.length() == 0) {
            /*
             * 未指定引用类名称，因此从其序列化形式构造远程引用。
             */
            ref = (RemoteRef) in.readObject();
        } else {
            /*
             * 指定了内置引用类，因此委托给内部引用类以从其外部形式初始化其字段。
             */
            String internalRefClassName =
                RemoteRef.packagePrefix + "." + refClassName;
            Class<?> refClass = Class.forName(internalRefClassName);
            try {
                ref = (RemoteRef) refClass.newInstance();

                /*
                 * 如果此步骤失败，假设我们找到了一个不是可序列化引用类型的内部类。
                 */
            } catch (InstantiationException e) {
                throw new ClassNotFoundException(internalRefClassName, e);
            } catch (IllegalAccessException e) {
                throw new ClassNotFoundException(internalRefClassName, e);
            } catch (ClassCastException e) {
                throw new ClassNotFoundException(internalRefClassName, e);
            }
            ref.readExternal(in);
        }
    }
}
