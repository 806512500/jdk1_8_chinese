
/*
 * 版权所有 (c) 1997, 2017, Oracle 和/或其附属公司。保留所有权利。
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

package java.rmi.activation;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.rmi.server.RemoteRef;
import java.rmi.server.UID;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permissions;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;

/**
 * 激活使用特殊的标识符来表示可以随时间激活的远程对象。激活标识符（<code>ActivationID</code> 类的实例）包含激活对象所需的信息：
 * <ul>
 * <li> 对象激活器的远程引用（一个 {@link
 * java.rmi.server.RemoteRef RemoteRef}
 * 实例），和
 * <li> 对象的唯一标识符（一个 {@link java.rmi.server.UID UID}
 * 实例）。 </ul> <p>
 *
 * 可以通过在激活系统中注册对象来获取对象的激活标识符。注册可以通过几种方式完成： <ul>
 * <li>通过 <code>Activatable.register</code> 方法
 * <li>通过第一个 <code>Activatable</code> 构造函数（接受三个参数，同时注册和导出对象）
 * <li>通过第一个 <code>Activatable.exportObject</code> 方法
 * 接受激活描述符、对象和端口作为参数；此方法同时注册和导出对象。 </ul>
 *
 * @author      Ann Wollrath
 * @see         Activatable
 * @since       1.2
 */
public class ActivationID implements Serializable {
    /**
     * 对象的激活器
     */
    private transient Activator activator;

    /**
     * 对象的唯一标识符
     */
    private transient UID uid = new UID();

    /** 表示与 Java 2 SDK v1.2 版本的类兼容 */
    private static final long serialVersionUID = -4608673054848209235L;

    /** 一个没有权限的 AccessControlContext */
    private static final AccessControlContext NOPERMS_ACC;
    static {
        Permissions perms = new Permissions();
        ProtectionDomain[] pd = { new ProtectionDomain(null, perms) };
        NOPERMS_ACC = new AccessControlContext(pd);
    }

    /**
     * <code>ActivationID</code> 的构造函数接受一个参数，activator，该参数指定一个远程引用，指向负责激活与此标识符关联的对象的激活器。
     * <code>ActivationID</code> 的实例是全局唯一的。
     *
     * @param activator 负责激活对象的激活器的引用
     * @throws UnsupportedOperationException 如果此实现不支持激活
     * @since 1.2
     */
    public ActivationID(Activator activator) {
        this.activator = activator;
    }

    /**
     * 激活与此标识符关联的对象。
     *
     * @param force 如果为 true，强制激活器在激活对象时联系组（而不是返回缓存的引用）；
     * 如果为 false，返回缓存的值是可以接受的。
     * @return 激活的远程对象的引用
     * @exception ActivationException 如果激活失败
     * @exception UnknownObjectException 如果对象未知
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public Remote activate(boolean force)
        throws ActivationException, UnknownObjectException, RemoteException
    {
        try {
            MarshalledObject<? extends Remote> mobj =
                activator.activate(this, force);
            return AccessController.doPrivileged(
                new PrivilegedExceptionAction<Remote>() {
                    public Remote run() throws IOException, ClassNotFoundException {
                        return mobj.get();
                    }
                }, NOPERMS_ACC);
        } catch (PrivilegedActionException pae) {
            Exception ex = pae.getException();
            if (ex instanceof RemoteException) {
                throw (RemoteException) ex;
            } else {
                throw new UnmarshalException("激活失败", ex);
            }
        }

    }

    /**
     * 返回激活 ID 的哈希码。引用相同远程对象的两个标识符将具有相同的哈希码。
     *
     * @see java.util.Hashtable
     * @since 1.2
     */
    public int hashCode() {
        return uid.hashCode();
    }

    /**
     * 比较两个激活 ID 的内容是否相等。
     * 如果同时满足以下两个条件，则返回 true：
     * 1) 唯一标识符等效（按内容），并且
     * 2) 每个标识符中指定的激活器
     *    指向相同的远程对象。
     *
     * @param   obj     要比较的对象
     * @return  如果这些对象相等，则返回 true；否则返回 false。
     * @see             java.util.Hashtable
     * @since 1.2
     */
    public boolean equals(Object obj) {
        if (obj instanceof ActivationID) {
            ActivationID id = (ActivationID) obj;
            return (uid.equals(id.uid) && activator.equals(id.activator));
        } else {
            return false;
        }
    }

    /**
     * <code>writeObject</code> 用于自定义序列化。
     *
     * <p>此方法按如下方式写入此类的序列化形式：
     *
     * <p>调用 <code>writeObject</code> 方法
     * 传递此对象的唯一标识符
     * （一个 {@link java.rmi.server.UID UID} 实例）作为参数。
     *
     * <p>接下来，调用激活器的
     * <code>RemoteRef</code> 实例的 {@link
     * java.rmi.server.RemoteRef#getRefClass(java.io.ObjectOutput)
     * getRefClass} 方法以获取其外部引用类型名称。然后，调用 <code>writeUTF</code> 方法
     * 传递 <code>getRefClass</code> 返回的值，并调用
     * <code>RemoteRef</code> 实例的 <code>writeExternal</code> 方法
     * 传递 <code>out</code> 作为参数。
     *
     * @serialData 此类的序列化数据包括一个
     * <code>java.rmi.server.UID</code>（使用
     * <code>ObjectOutput.writeObject</code> 写入），后跟激活器的
     * <code>RemoteRef</code> 实例的外部引用类型名称（使用
     * <code>ObjectOutput.writeUTF</code> 写入的字符串），再后跟
     * <code>RemoteRef</code> 实例的外部形式，由其 <code>writeExternal</code> 方法写入。
     *
     * <p><code>RemoteRef</code> 实例的外部引用类型名称
     * 通过 {@link java.rmi.server.RemoteObject
     * RemoteObject} <code>writeObject</code> 方法
     * <b>serialData</b> 规范中定义的外部引用类型名称确定。同样，由
     * <code>RemoteRef</code> 实现类的 <code>writeExternal</code> 方法写入的数据和
     * <code>readExternal</code> 方法读取的数据
     * 对应于每个定义的外部引用类型名称，在 {@link
     * java.rmi.server.RemoteObject RemoteObject}
     * <code>writeObject</code> 方法 <b>serialData</b>
     * 规范中指定。
     **/
    private void writeObject(ObjectOutputStream out)
        throws IOException, ClassNotFoundException
    {
        out.writeObject(uid);


                    RemoteRef ref;
        if (activator instanceof RemoteObject) {
            ref = ((RemoteObject) activator).getRef();
        } else if (Proxy.isProxyClass(activator.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(activator);
            if (!(handler instanceof RemoteObjectInvocationHandler)) {
                throw new InvalidObjectException(
                    "unexpected invocation handler");
            }
            ref = ((RemoteObjectInvocationHandler) handler).getRef();

        } else {
            throw new InvalidObjectException("unexpected activator type");
        }
        out.writeUTF(ref.getRefClass(out));
        ref.writeExternal(out);
    }

    /**
     * <code>readObject</code> for custom serialization.
     *
     * <p>This method reads this object's serialized form for this
     * class as follows:
     *
     * <p>The <code>readObject</code> method is invoked on
     * <code>in</code> to read this object's unique identifier
     * (a {@link java.rmi.server.UID UID} instance).
     *
     * <p>Next, the <code>readUTF</code> method is invoked on
     * <code>in</code> to read the external ref type name of the
     * <code>RemoteRef</code> instance for this object's
     * activator.  Next, the <code>RemoteRef</code>
     * instance is created of an implementation-specific class
     * corresponding to the external ref type name (returned by
     * <code>readUTF</code>), and the <code>readExternal</code>
     * method is invoked on that <code>RemoteRef</code> instance
     * to read the external form corresponding to the external
     * ref type name.
     *
     * <p>Note: If the external ref type name is
     * <code>"UnicastRef"</code>, <code>"UnicastServerRef"</code>,
     * <code>"UnicastRef2"</code>, <code>"UnicastServerRef2"</code>,
     * or <code>"ActivatableRef"</code>, a corresponding
     * implementation-specific class must be found, and its
     * <code>readExternal</code> method must read the serial data
     * for that external ref type name as specified to be written
     * in the <b>serialData</b> documentation for this class.
     * If the external ref type name is any other string (of non-zero
     * length), a <code>ClassNotFoundException</code> will be thrown,
     * unless the implementation provides an implementation-specific
     * class corresponding to that external ref type name, in which
     * case the <code>RemoteRef</code> will be an instance of
     * that implementation-specific class.
     */
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        uid = (UID)in.readObject();

        try {
            Class<? extends RemoteRef> refClass =
                Class.forName(RemoteRef.packagePrefix + "." + in.readUTF())
                .asSubclass(RemoteRef.class);
            RemoteRef ref = refClass.newInstance();
            ref.readExternal(in);
            activator = (Activator)
                Proxy.newProxyInstance(null,
                                       new Class<?>[] { Activator.class },
                                       new RemoteObjectInvocationHandler(ref));

        } catch (InstantiationException e) {
            throw (IOException)
                new InvalidObjectException(
                    "Unable to create remote reference").initCause(e);
        } catch (IllegalAccessException e) {
            throw (IOException)
                new InvalidObjectException(
                    "Unable to create remote reference").initCause(e);
        }
    }
}
