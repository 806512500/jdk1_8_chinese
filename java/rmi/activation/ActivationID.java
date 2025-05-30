/*
 * Copyright (c) 1997, 2017, Oracle and/or its affiliates. All rights reserved.
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
 * 激活使用特殊标识符来表示可以随时间激活的远程对象。激活标识符（<code>ActivationID</code>类的实例）包含激活对象所需的信息：
 * <ul>
 * <li> 对象激活器的远程引用（一个 {@link java.rmi.server.RemoteRef RemoteRef} 实例），以及
 * <li> 对象的唯一标识符（一个 {@link java.rmi.server.UID UID} 实例）。 </ul> <p>
 *
 * 可以通过在激活系统中注册对象来获取对象的激活标识符。注册可以通过几种方式完成： <ul>
 * <li>通过 <code>Activatable.register</code> 方法
 * <li>通过第一个 <code>Activatable</code> 构造函数（该构造函数接受三个参数，同时注册和导出对象），以及
 * <li>通过第一个 <code>Activatable.exportObject</code> 方法（该方法接受激活描述符、对象和端口作为参数；此方法同时注册和导出对象）。 </ul>
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
     * <code>ActivationID</code> 的构造函数接受一个参数，activator，该参数指定了一个远程引用，指向负责激活与此标识符关联的对象的激活器。
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
     * 激活此标识符的对象。
     *
     * @param force 如果为 true，则强制激活器在激活对象时联系组（而不是返回缓存的引用）；如果为 false，则返回缓存的值是可以接受的。
     * @return 活动远程对象的引用
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
     * 1) 唯一标识符等效（按内容），以及
     * 2) 每个标识符中指定的激活器引用相同的远程对象。
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
     * <p>此方法按如下方式编写此类的序列化形式：
     *
     * <p>调用 <code>out</code> 的 <code>writeObject</code> 方法，将此对象的唯一标识符
     * （一个 {@link java.rmi.server.UID UID} 实例）作为参数传递。
     *
     * <p>接下来，调用激活器的 <code>RemoteRef</code> 实例的 {@link
     * java.rmi.server.RemoteRef#getRefClass(java.io.ObjectOutput)
     * getRefClass} 方法以获取其外部引用类型名称。接下来，调用 <code>out</code> 的 <code>writeUTF</code> 方法
     * 将 <code>getRefClass</code> 返回的值写入，然后调用 <code>RemoteRef</code> 实例的
     * <code>writeExternal</code> 方法，将 <code>out</code> 作为参数传递。
     *
     * @serialData 该类的序列化数据包括一个 <code>java.rmi.server.UID</code>
     * （使用 <code>ObjectOutput.writeObject</code> 写入），后跟激活器的
     * <code>RemoteRef</code> 实例的外部引用类型名称（使用 <code>ObjectOutput.writeUTF</code> 写入的字符串），
     * 再后跟 <code>RemoteRef</code> 实例的外部形式，由其 <code>writeExternal</code> 方法写入。
     *
     * <p><code>RemoteRef</code> 实例的外部引用类型名称
     * 使用 {@link java.rmi.server.RemoteObject RemoteObject} <code>writeObject</code> 方法
     * <b>serialData</b> 规范中指定的外部引用类型名称定义来确定。同样，由 <code>RemoteRef</code> 实现类的
     * <code>writeExternal</code> 方法写入和 <code>readExternal</code> 方法读取的数据
     * 对应于每个定义的外部引用类型名称，也在 {@link
     * java.rmi.server.RemoteObject RemoteObject} <code>writeObject</code> 方法 <b>serialData</b>
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
                    "意外的调用处理器");
            }
            ref = ((RemoteObjectInvocationHandler) handler).getRef();

        } else {
            throw new InvalidObjectException("意外的激活器类型");
        }
        out.writeUTF(ref.getRefClass(out));
        ref.writeExternal(out);
    }

    /**
     * <code>readObject</code> 用于自定义序列化。
     *
     * <p>此方法按如下方式读取此类的序列化形式：
     *
     * <p>调用 <code>in</code> 的 <code>readObject</code> 方法读取此对象的唯一标识符
     * （一个 {@link java.rmi.server.UID UID} 实例）。
     *
     * <p>接下来，调用 <code>in</code> 的 <code>readUTF</code> 方法读取此对象激活器的
     * <code>RemoteRef</code> 实例的外部引用类型名称。接下来，创建一个与外部引用类型名称对应的实现特定类的
     * <code>RemoteRef</code> 实例（由 <code>readUTF</code> 返回），并调用该 <code>RemoteRef</code> 实例的
     * <code>readExternal</code> 方法读取与外部引用类型名称对应的外部形式。
     *
     * <p>注意：如果外部引用类型名称为
     * <code>"UnicastRef"</code>、<code>"UnicastServerRef"</code>、
     * <code>"UnicastRef2"</code>、<code>"UnicastServerRef2"</code> 或 <code>"ActivatableRef"</code>，
     * 必须找到一个对应的实现特定类，并且其 <code>readExternal</code> 方法必须读取
     * 该外部引用类型名称的序列数据，如该类的 <b>serialData</b> 文档中指定的那样。
     * 如果外部引用类型名称是任何其他字符串（非零长度），将抛出 <code>ClassNotFoundException</code>，
     * 除非实现提供了与该外部引用类型名称对应的实现特定类，在这种情况下，<code>RemoteRef</code> 将是该实现特定类的实例。
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
                    "无法创建远程引用").initCause(e);
        } catch (IllegalAccessException e) {
            throw (IOException)
                new InvalidObjectException(
                    "无法创建远程引用").initCause(e);
        }
    }
}
