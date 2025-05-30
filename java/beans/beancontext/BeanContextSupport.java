
/*
 * Copyright (c) 1997, 2019, Oracle and/or its affiliates. All rights reserved.
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

package java.beans.beancontext;

import java.awt.Component;
import java.awt.Container;

import java.beans.Beans;
import java.beans.AppletInitializer;

import java.beans.DesignMode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.PropertyVetoException;

import java.beans.Visibility;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;


/**
 * 此辅助类提供了 java.beans.beancontext.BeanContext 接口的实用实现。
 * <p>
 * 由于此类直接实现了 BeanContext 接口，因此可以通过继承此实现或通过从另一个类中委托此类的实例来使用此类。
 * </p>
 *
 * @author Laurence P. G. Cable
 * @since 1.2
 */
public class BeanContextSupport extends BeanContextChildSupport
       implements BeanContext,
                  Serializable,
                  PropertyChangeListener,
                  VetoableChangeListener {

    // 修复 bug 4282900 以通过 JCK 回归测试
    static final long serialVersionUID = -4879613978649577204L;

    /**
     * 构造一个 BeanContextSupport 实例
     *
     * @param peer      我们为其提供实现的对等 BeanContext，
     *                  如果此对象是其自己的对等对象，则为 <tt>null</tt>
     * @param lcle      此 BeanContext 的当前 Locale。如果
     *                  <tt>lcle</tt> 为 <tt>null</tt>，则将默认 locale
     *                  分配给 BeanContext 实例。
     * @param dTime     初始状态，
     *                  <tt>true</tt> 表示设计模式，
     *                  <tt>false</tt> 表示运行时。
     * @param visible   初始可见性。
     * @see java.util.Locale#getDefault()
     * @see java.util.Locale#setDefault(java.util.Locale)
     */
    public BeanContextSupport(BeanContext peer, Locale lcle, boolean dTime, boolean visible) {
        super(peer);

        locale          = lcle != null ? lcle : Locale.getDefault();
        designTime      = dTime;
        okToUseGui      = visible;

        initialize();
    }

    /**
     * 使用指定的 Locale 和设计模式创建一个实例。
     *
     * @param peer      我们为其提供实现的对等 BeanContext，
     *                  如果此对象是其自己的对等对象，则为 <tt>null</tt>
     * @param lcle      此 BeanContext 的当前 Locale。如果
     *                  <tt>lcle</tt> 为 <tt>null</tt>，则将默认 locale
     *                  分配给 BeanContext 实例。
     * @param dtime     初始状态，<tt>true</tt>
     *                  表示设计模式，
     *                  <tt>false</tt> 表示运行时。
     * @see java.util.Locale#getDefault()
     * @see java.util.Locale#setDefault(java.util.Locale)
     */
    public BeanContextSupport(BeanContext peer, Locale lcle, boolean dtime) {
        this (peer, lcle, dtime, true);
    }

    /**
     * 使用指定的 locale 创建一个实例
     *
     * @param peer      我们为其提供实现的对等 BeanContext，
     *                  如果此对象是其自己的对等对象，则为 <tt>null</tt>
     * @param lcle      此 BeanContext 的当前 Locale。如果
     *                  <tt>lcle</tt> 为 <tt>null</tt>，则将默认 locale
     *                  分配给 BeanContext 实例。
     * @see java.util.Locale#getDefault()
     * @see java.util.Locale#setDefault(java.util.Locale)
     */
    public BeanContextSupport(BeanContext peer, Locale lcle) {
        this (peer, lcle, false, true);
    }

    /**
     * 使用默认 locale 创建一个实例
     *
     * @param peer      我们为其提供实现的对等 BeanContext，
     *                  如果此对象是其自己的对等对象，则为 <tt>null</tt>
     */
    public BeanContextSupport(BeanContext peer) {
        this (peer, null, false, true);
    }

    /**
     * 创建一个不是其他对象代理的实例
     */

    public BeanContextSupport() {
        this (null, null, false, true);
    }

    /**
     * 获取此对象提供实现的 BeanContext 实例。
     * @return BeanContext 实例
     */
    public BeanContext getBeanContextPeer() { return (BeanContext)getBeanContextChildPeer(); }

    /**
     * <p>
     * instantiateChild 方法是 BeanContext 中的一个便利钩子，
     * 用于简化在 BeanContext 中嵌套实例化 Bean 的任务。
     * </p>
     * <p>
     * beanName 参数的语义由 java.beans.Beans.instantiate 定义。
     * </p>
     *
     * @param beanName 要在本 BeanContext 中实例化的 Bean 的名称
     * @throws IOException 如果在反序列化 bean 时发生 I/O 错误
     * @throws ClassNotFoundException 如果未找到由 beanName 参数标识的类
     * @return 新对象
     */
    public Object instantiateChild(String beanName)
           throws IOException, ClassNotFoundException {
        BeanContext bc = getBeanContextPeer();

        return Beans.instantiate(bc.getClass().getClassLoader(), beanName, bc);
    }

    /**
     * 获取当前嵌套在本 BeanContext 中的子对象数量。
     *
     * @return 子对象数量
     */
    public int size() {
        synchronized(children) {
            return children.size();
        }
    }

    /**
     * 报告此 BeanContext 是否为空。
     * 当 BeanContext 不包含任何嵌套子对象时，被认为是空的。
     * @return 如果没有子对象
     */
    public boolean isEmpty() {
        synchronized(children) {
            return children.isEmpty();
        }
    }

    /**
     * 确定指定对象是否当前是此 BeanContext 的子对象。
     * @param o 要检查的对象
     * @return 如果此对象是子对象
     */
    public boolean contains(Object o) {
        synchronized(children) {
            return children.containsKey(o);
        }
    }

    /**
     * 确定指定对象是否当前是此 BeanContext 的子对象。
     * @param o 要检查的对象
     * @return 如果此对象是子对象
     */
    public boolean containsKey(Object o) {
        synchronized(children) {
            return children.containsKey(o);
        }
    }

    /**
     * 获取当前嵌套在本 BeanContext 中的所有 JavaBean 或 BeanContext 实例。
     * @return 嵌套子对象的 Iterator
     */
    public Iterator iterator() {
        synchronized(children) {
            return new BCSIterator(children.keySet().iterator());
        }
    }

    /**
     * 获取当前嵌套在本 BeanContext 中的所有 JavaBean 或 BeanContext 实例。
     */
    public Object[] toArray() {
        synchronized(children) {
            return children.keySet().toArray();
        }
    }

    /**
     * 获取与 arry 中包含的类型匹配的本 BeanContext 的所有子对象的数组。
     * @param arry 感兴趣的对象类型数组。
     * @return 子对象数组
     */
    public Object[] toArray(Object[] arry) {
        synchronized(children) {
            return children.keySet().toArray(arry);
        }
    }


    /************************************************************************/

    /**
     * 封装了一个迭代器的受保护的最终子类，但实现了 no-op remove() 方法。
     */

    protected static final class BCSIterator implements Iterator {
        BCSIterator(Iterator i) { super(); src = i; }

        public boolean hasNext() { return src.hasNext(); }
        public Object  next()    { return src.next();    }
        public void    remove()  { /* do nothing */      }

        private Iterator src;
    }

    /************************************************************************/

    /*
     * 受保护的嵌套类，包含每个子对象的信息，每个子对象在 "children" 哈希表中都有一个实例。
     * 子类可以扩展此类以包含自己的每个子对象的状态。
     *
     * 注意，当 BeanContextSupport 被序列化时，此 'value' 会与相应的子对象 'key' 一起序列化。
     */

    protected class BCSChild implements Serializable {

    private static final long serialVersionUID = -5815286101609939109L;

        BCSChild(Object bcc, Object peer) {
            super();

            child     = bcc;
            proxyPeer = peer;
        }

        Object  getChild()                  { return child; }

        void    setRemovePending(boolean v) { removePending = v; }

        boolean isRemovePending()           { return removePending; }

        boolean isProxyPeer()               { return proxyPeer != null; }

        Object  getProxyPeer()              { return proxyPeer; }
        /*
         * 字段
         */


        private           Object   child;
        private           Object   proxyPeer;

        private transient boolean  removePending;
    }

    /**
     * <p>
     * 子类可以重写此方法，以在不覆盖 add() 或其他添加子对象的集合方法的情况下，插入自己的 Child 子类。
     * </p>
     * @param targetChild 代表要创建 Child 的子对象
     * @param peer        如果 targetChild 和 peer 由 BeanContextProxy 实现关联，则为 peer
     * @return 不覆盖集合方法的特定子类型的 Child 子类
     */

    protected BCSChild createBCSChild(Object targetChild, Object peer) {
        return new BCSChild(targetChild, peer);
    }

    /************************************************************************/

    /**
     * 在此 BeanContext 中添加/嵌套一个子对象。
     * <p>
     * 作为 java.beans.Beans.instantiate() 的副作用被调用。
     * 如果子对象无效，则此方法抛出 IllegalStateException。
     * </p>
     *
     * @param targetChild 要嵌套在本 BeanContext 中的子对象
     * @return 如果子对象成功添加，则返回 true。
     * @see #validatePendingAdd
     */
    public boolean add(Object targetChild) {

        if (targetChild == null) throw new IllegalArgumentException();

        // 规范要求如果子对象已经嵌套在此处，则我们不做任何操作。

        if (children.containsKey(targetChild)) return false; // 在锁定前进行测试

        synchronized(BeanContext.globalHierarchyLock) {
            if (children.containsKey(targetChild)) return false; // 再次检查

            if (!validatePendingAdd(targetChild)) {
                throw new IllegalStateException();
            }


            // 规范要求如果新添加的子对象实现了 java.beans.beancontext.BeanContextChild 接口，则调用 setBeanContext()

            BeanContextChild cbcc  = getChildBeanContextChild(targetChild);
            BeanContextChild  bccp = null;

            synchronized(targetChild) {

                if (targetChild instanceof BeanContextProxy) {
                    bccp = ((BeanContextProxy)targetChild).getBeanContextProxy();

                    if (bccp == null) throw new NullPointerException("BeanContextPeer.getBeanContextProxy()");
                }

                BCSChild bcsc  = createBCSChild(targetChild, bccp);
                BCSChild pbcsc = null;

                synchronized (children) {
                    children.put(targetChild, bcsc);

                    if (bccp != null) children.put(bccp, pbcsc = createBCSChild(bccp, targetChild));
                }

                if (cbcc != null) synchronized(cbcc) {
                    try {
                        cbcc.setBeanContext(getBeanContextPeer());
                    } catch (PropertyVetoException pve) {

                        synchronized (children) {
                            children.remove(targetChild);

                            if (bccp != null) children.remove(bccp);
                        }

                        throw new IllegalStateException();
                    }

                    cbcc.addPropertyChangeListener("beanContext", childPCL);
                    cbcc.addVetoableChangeListener("beanContext", childVCL);
                }

                Visibility v = getChildVisibility(targetChild);

                if (v != null) {
                    if (okToUseGui)
                        v.okToUseGui();
                    else
                        v.dontUseGui();
                }

                if (getChildSerializable(targetChild) != null) serializable++;

                childJustAddedHook(targetChild, bcsc);

                if (bccp != null) {
                    v = getChildVisibility(bccp);

                    if (v != null) {
                        if (okToUseGui)
                            v.okToUseGui();
                        else
                            v.dontUseGui();
                    }


                                if (getChildSerializable(bccp) != null) serializable++;

                    childJustAddedHook(bccp, pbcsc);
                }


            }

            // 规范要求我们触发更改的通知

            fireChildrenAdded(new BeanContextMembershipEvent(getBeanContextPeer(), bccp == null ? new Object[] { targetChild } : new Object[] { targetChild, bccp } ));

        }

        return true;
    }

    /**
     * 从这个 BeanContext 中移除一个子对象。如果子对象不是
     * 用于添加的，则此方法抛出 IllegalStateException。
     * @param targetChild 要移除的子对象
     * @see #validatePendingRemove
     */
    public boolean remove(Object targetChild) {
        return remove(targetChild, true);
    }

    /**
     * 内部移除方法，用于处理由意外的 <tt>setBeanContext</tt> 或
     * <tt>remove()</tt> 调用引起的移除。
     * @param targetChild 要移除的 JavaBean、BeanContext 或 Object
     * @param callChildSetBC 用于指示
     * 子对象是否应被告知它不再嵌套在
     * 此 <tt>BeanContext</tt> 中。
     * @return 是否在移除前存在
     */
    protected boolean remove(Object targetChild, boolean callChildSetBC) {

        if (targetChild == null) throw new IllegalArgumentException();

        synchronized(BeanContext.globalHierarchyLock) {
            if (!containsKey(targetChild)) return false;

            if (!validatePendingRemove(targetChild)) {
                throw new IllegalStateException();
            }

            BCSChild bcsc  = (BCSChild)children.get(targetChild);
            BCSChild pbcsc = null;
            Object   peer  = null;

            // 如果子对象实现了 java.beans.beancontext.BeanContextChild，
            // 我们需要通知它不再嵌套在此处

            synchronized(targetChild) {
                if (callChildSetBC) {
                    BeanContextChild cbcc = getChildBeanContextChild(targetChild);
                    if (cbcc != null) synchronized(cbcc) {
                        cbcc.removePropertyChangeListener("beanContext", childPCL);
                        cbcc.removeVetoableChangeListener("beanContext", childVCL);

                        try {
                            cbcc.setBeanContext(null);
                        } catch (PropertyVetoException pve1) {
                            cbcc.addPropertyChangeListener("beanContext", childPCL);
                            cbcc.addVetoableChangeListener("beanContext", childVCL);
                            throw new IllegalStateException();
                        }

                    }
                }

                synchronized (children) {
                    children.remove(targetChild);

                    if (bcsc.isProxyPeer()) {
                        pbcsc = (BCSChild)children.get(peer = bcsc.getProxyPeer());
                        children.remove(peer);
                    }
                }

                if (getChildSerializable(targetChild) != null) serializable--;

                childJustRemovedHook(targetChild, bcsc);

                if (peer != null) {
                    if (getChildSerializable(peer) != null) serializable--;

                    childJustRemovedHook(peer, pbcsc);
                }
            }

            fireChildrenRemoved(new BeanContextMembershipEvent(getBeanContextPeer(), peer == null ? new Object[] { targetChild } : new Object[] { targetChild, peer } ));

        }

        return true;
    }

    /**
     * 测试指定的 <tt>Collection</tt> 中的所有对象是否是
     * 此 <tt>BeanContext</tt> 的子对象。
     * @param c 指定的 <tt>Collection</tt>
     *
     * @return <tt>true</tt> 如果集合中的所有对象
     * 都是此 <tt>BeanContext</tt> 的子对象，否则返回 <tt>false</tt>。
     */
    public boolean containsAll(Collection c) {
        synchronized(children) {
            Iterator i = c.iterator();
            while (i.hasNext())
                if(!contains(i.next()))
                    return false;

            return true;
        }
    }

    /**
     * 将 Collection 添加到子对象集（不支持）
     * 实现必须在层次锁和 "children" 保护字段上同步
     * @throws UnsupportedOperationException 由此实现无条件抛出
     * @return 此实现无条件抛出 {@code UnsupportedOperationException}
     */
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**
     * 移除所有指定的子对象（不支持）
     * 实现必须在层次锁和 "children" 保护字段上同步
     * @throws UnsupportedOperationException 由此实现无条件抛出
     * @return 此实现无条件抛出 {@code UnsupportedOperationException}

     */
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException();
    }


    /**
     * 仅保留指定的子对象（不支持）
     * 实现必须在层次锁和 "children" 保护字段上同步
     * @throws UnsupportedOperationException 由此实现无条件抛出
     * @return 此实现无条件抛出 {@code UnsupportedOperationException}
     */
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**
     * 清除子对象（不支持）
     * 实现必须在层次锁和 "children" 保护字段上同步
     * @throws UnsupportedOperationException 由此实现无条件抛出
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * 添加 BeanContextMembershipListener
     *
     * @param  bcml 要添加的 BeanContextMembershipListener
     * @throws NullPointerException 如果参数为 null
     */

    public void addBeanContextMembershipListener(BeanContextMembershipListener bcml) {
        if (bcml == null) throw new NullPointerException("listener");

        synchronized(bcmListeners) {
            if (bcmListeners.contains(bcml))
                return;
            else
                bcmListeners.add(bcml);
        }
    }

    /**
     * 移除 BeanContextMembershipListener
     *
     * @param  bcml 要移除的 BeanContextMembershipListener
     * @throws NullPointerException 如果参数为 null
     */

    public void removeBeanContextMembershipListener(BeanContextMembershipListener bcml) {
        if (bcml == null) throw new NullPointerException("listener");

        synchronized(bcmListeners) {
            if (!bcmListeners.contains(bcml))
                return;
            else
                bcmListeners.remove(bcml);
        }
    }

    /**
     * @param name 请求的资源名称。
     * @param bcc  发起请求的子对象。
     *
     * @return 请求的资源作为 InputStream
     * @throws  NullPointerException 如果参数为 null
     */

    public InputStream getResourceAsStream(String name, BeanContextChild bcc) {
        if (name == null) throw new NullPointerException("name");
        if (bcc  == null) throw new NullPointerException("bcc");

        if (containsKey(bcc)) {
            ClassLoader cl = bcc.getClass().getClassLoader();

            return cl != null ? cl.getResourceAsStream(name)
                              : ClassLoader.getSystemResourceAsStream(name);
        } else throw new IllegalArgumentException("Not a valid child");
    }

    /**
     * @param name 请求的资源名称。
     * @param bcc  发起请求的子对象。
     *
     * @return 请求的资源作为 InputStream
     */

    public URL getResource(String name, BeanContextChild bcc) {
        if (name == null) throw new NullPointerException("name");
        if (bcc  == null) throw new NullPointerException("bcc");

        if (containsKey(bcc)) {
            ClassLoader cl = bcc.getClass().getClassLoader();

            return cl != null ? cl.getResource(name)
                              : ClassLoader.getSystemResource(name);
        } else throw new IllegalArgumentException("Not a valid child");
    }

    /**
     * 设置此 <tt>BeanContext</tt> 的新设计时间值。
     * @param dTime 新的设计时间值
     */
    public synchronized void setDesignTime(boolean dTime) {
        if (designTime != dTime) {
            designTime = dTime;

            firePropertyChange("designMode", Boolean.valueOf(!dTime), Boolean.valueOf(dTime));
        }
    }


    /**
     * 报告此对象是否当前处于设计时间模式。
     * @return <tt>true</tt> 如果处于设计时间模式，
     * <tt>false</tt> 如果不是
     */
    public synchronized boolean isDesignTime() { return designTime; }

    /**
     * 设置此 BeanContext 的区域设置。
     * @param newLocale 新的区域设置。如果 newLocale 为 <CODE>null</CODE>，此方法调用将不起作用。
     * @throws PropertyVetoException 如果新值被拒绝
     */
    public synchronized void setLocale(Locale newLocale) throws PropertyVetoException {

        if ((locale != null && !locale.equals(newLocale)) && newLocale != null) {
            Locale old = locale;

            fireVetoableChange("locale", old, newLocale); // 抛出

            locale = newLocale;

            firePropertyChange("locale", old, newLocale);
        }
    }

    /**
     * 获取此 <tt>BeanContext</tt> 的区域设置。
     *
     * @return <tt>BeanContext</tt> 的当前区域设置
     */
    public synchronized Locale getLocale() { return locale; }

    /**
     * <p>
     * 此方法通常由环境调用，以确定
     * 实现者是否“需要”GUI。
     * </p>
     * <p>
     * 本方法使用的算法测试 BeanContextPeer 及其当前子对象
     * 以确定它们是否为容器、组件，或是否实现
     * Visibility 并返回 needsGui() == true。
     * </p>
     * @return <tt>true</tt> 如果实现者需要 GUI
     */
    public synchronized boolean needsGui() {
        BeanContext bc = getBeanContextPeer();

        if (bc != this) {
            if (bc instanceof Visibility) return ((Visibility)bc).needsGui();

            if (bc instanceof Container || bc instanceof Component)
                return true;
        }

        synchronized(children) {
            for (Iterator i = children.keySet().iterator(); i.hasNext();) {
                Object c = i.next();

                try {
                        return ((Visibility)c).needsGui();
                    } catch (ClassCastException cce) {
                        // 什么都不做 ...
                    }

                    if (c instanceof Container || c instanceof Component)
                        return true;
            }
        }

        return false;
    }

    /**
     * 通知此实例它可能不再渲染 GUI。
     */

    public synchronized void dontUseGui() {
        if (okToUseGui) {
            okToUseGui = false;

            // 也让可以的子对象知道它们可能不再使用它们的 GUI
            synchronized(children) {
                for (Iterator i = children.keySet().iterator(); i.hasNext();) {
                    Visibility v = getChildVisibility(i.next());

                    if (v != null) v.dontUseGui();
               }
            }
        }
    }

    /**
     * 通知此实例它现在可以渲染 GUI
     */

    public synchronized void okToUseGui() {
        if (!okToUseGui) {
            okToUseGui = true;

            // 也让可以的子对象知道它们可以使用它们的 GUI
            synchronized(children) {
                for (Iterator i = children.keySet().iterator(); i.hasNext();) {
                    Visibility v = getChildVisibility(i.next());

                    if (v != null) v.okToUseGui();
                }
            }
        }
    }

    /**
     * 用于确定 <tt>BeanContext</tt>
     * 子对象是否避免使用其 GUI。
     * @return 此实例是否避免使用其 GUI？
     * @see Visibility
     */
    public boolean avoidingGui() {
        return !okToUseGui && needsGui();
    }

    /**
     * 此 <tt>BeanContext</tt> 是否
     * 正在被序列化？
     * @return 如果此 <tt>BeanContext</tt> 当前正在被序列化
     */
    public boolean isSerializing() { return serializing; }

    /**
     * 返回此 <tt>BeanContext</tt> 的所有子对象的迭代器。
     * @return 当前所有 BCSChild 值的迭代器
     */
    protected Iterator bcsChildren() { synchronized(children) { return children.values().iterator();  } }

    /**
     * 在 defaultWriteObject() 之后但在此类子对象序列化之前
     * 由 writeObject 调用。
     *
     * 子类可以重写此方法以在超类序列化子对象之前
     * 执行自定义状态序列化。
     *
     * 然而，子类不应使用此方法来替换它们自己的
     * writeObject() 实现（如果有）。
     * @param oos 序列化期间使用的 {@code ObjectOutputStream}
     * @throws IOException 如果序列化失败
     */

    protected void bcsPreSerializationHook(ObjectOutputStream oos) throws IOException {
    }

    /**
     * 在 defaultReadObject() 之后但在任何子对象反序列化之前
     * 由 readObject 调用。
     *
     * 子类可以重写此方法以在超类反序列化子对象之前
     * 执行自定义状态反序列化。
     *
     * 然而，子类不应使用此方法来替换它们自己的
     * readObject() 实现（如果有）。
     * @param ois 反序列化期间使用的 {@code ObjectInputStream}
     * @throws IOException 如果反序列化失败
     * @throws ClassNotFoundException 如果找不到所需类
     */

    protected void bcsPreDeserializationHook(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    }

    /**
     * 由 readObject 调用，带有新反序列化的子对象和 BCSChild。
     * @param child 新反序列化的子对象
     * @param bcsc 新反序列化的 BCSChild
     */
    protected void childDeserializedHook(Object child, BCSChild bcsc) {
        synchronized(children) {
            children.put(child, bcsc);
        }
    }

    /**
     * 由 writeObject 用于序列化 Collection。
     * @param oos 序列化期间使用的 <tt>ObjectOutputStream</tt>
     * @param coll 要序列化的 <tt>Collection</tt>
     * @throws IOException 如果序列化失败
     */
    protected final void serialize(ObjectOutputStream oos, Collection coll) throws IOException {
        int      count   = 0;
        Object[] objects = coll.toArray();

        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof Serializable)
                count++;
            else
                objects[i] = null;
        }


                    oos.writeInt(count); // 对象数量

        for (int i = 0; count > 0; i++) {
            Object o = objects[i];

            if (o != null) {
                oos.writeObject(o);
                count--;
            }
        }
    }

    /**
     * 用于 readObject 反序列化一个集合。
     * @param ois 使用的 ObjectInputStream
     * @param coll 集合
     * @throws IOException 如果反序列化失败
     * @throws ClassNotFoundException 如果需要的类未找到
     */
    protected final void deserialize(ObjectInputStream ois, Collection coll) throws IOException, ClassNotFoundException {
        int count = 0;

        count = ois.readInt();

        while (count-- > 0) {
            coll.add(ois.readObject());
        }
    }

    /**
     * 用于序列化此 <tt>BeanContext</tt> 的所有子对象。
     * @param oos 序列化期间使用的 <tt>ObjectOutputStream</tt>
     * @throws IOException 如果序列化失败
     */
    public final void writeChildren(ObjectOutputStream oos) throws IOException {
        if (serializable <= 0) return;

        boolean prev = serializing;

        serializing = true;

        int count = 0;

        synchronized(children) {
            Iterator i = children.entrySet().iterator();

            while (i.hasNext() && count < serializable) {
                Map.Entry entry = (Map.Entry)i.next();

                if (entry.getKey() instanceof Serializable) {
                    try {
                        oos.writeObject(entry.getKey());   // 子对象
                        oos.writeObject(entry.getValue()); // BCSChild
                    } catch (IOException ioe) {
                        serializing = prev;
                        throw ioe;
                    }
                    count++;
                }
            }
        }

        serializing = prev;

        if (count != serializable) {
            throw new IOException("写入的子对象数量与预期不同");
        }

    }

    /**
     * 序列化 BeanContextSupport，如果此实例有独立的对等对象（即此对象作为另一个对象的代理），则由于在反序列化子对象时出现的“先有鸡还是先有蛋”问题，此实例的子对象不会在此处序列化。
     *
     * 因此，在有独立对等对象的情况下，应始终调用 writeObject() 后跟 writeChildren()，以及 readObject() 后跟 readChildren()。
     *
     * @param oos ObjectOutputStream
     */

    private synchronized void writeObject(ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        serializing = true;

        synchronized (BeanContext.globalHierarchyLock) {
            try {
                oos.defaultWriteObject(); // 序列化 BeanContextSupport 对象

                bcsPreSerializationHook(oos);

                if (serializable > 0 && this.equals(getBeanContextPeer()))
                    writeChildren(oos);

                serialize(oos, (Collection)bcmListeners);
            } finally {
                serializing = false;
            }
        }
    }

    /**
     * 当此类的实例用作 BeanContext 协议（及其子协议）的代理实现时，在反序列化过程中存在“先有鸡还是先有蛋”问题
     * @param ois 使用的 ObjectInputStream
     * @throws IOException 如果反序列化失败
     * @throws ClassNotFoundException 如果需要的类未找到
     */

    public final void readChildren(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        int count = serializable;

        while (count-- > 0) {
            Object child = ois.readObject();
            BCSChild bscc = (BCSChild) ois.readObject();

            synchronized(child) {
                BeanContextChild bcc = null;

                try {
                    bcc = (BeanContextChild)child;
                } catch (ClassCastException cce) {
                    // 什么都不做；
                }

                if (bcc != null) {
                    try {
                        bcc.setBeanContext(getBeanContextPeer());

                       bcc.addPropertyChangeListener("beanContext", childPCL);
                       bcc.addVetoableChangeListener("beanContext", childVCL);

                    } catch (PropertyVetoException pve) {
                        continue;
                    }
                }

                childDeserializedHook(child, bscc);
            }
        }
    }

    /**
     * 反序列化内容... 如果此实例有独立的对等对象，则子对象不会在此处序列化，对等对象的 readObject() 必须在反序列化此实例后调用 readChildren()。
     */

    private synchronized void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {

        synchronized(BeanContext.globalHierarchyLock) {
            ois.defaultReadObject();

            initialize();

            bcsPreDeserializationHook(ois);

            if (serializable > 0 && this.equals(getBeanContextPeer()))
                readChildren(ois);

            deserialize(ois, bcmListeners = new ArrayList(1));
        }
    }

    /**
     * 子类可以封装以监控子对象属性更改的否决。
     */

    public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException {
        String propertyName = pce.getPropertyName();
        Object source       = pce.getSource();

        synchronized(children) {
            if ("beanContext".equals(propertyName) &&
                containsKey(source)                    &&
                !getBeanContextPeer().equals(pce.getNewValue())
            ) {
                if (!validatePendingRemove(source)) {
                    throw new PropertyVetoException("当前 BeanContext 反对 setBeanContext()", pce);
                } else ((BCSChild)children.get(source)).setRemovePending(true);
            }
        }
    }

    /**
     * 子类可以封装以监控子对象属性更改。
     */

    public void propertyChange(PropertyChangeEvent pce) {
        String propertyName = pce.getPropertyName();
        Object source       = pce.getSource();

        synchronized(children) {
            if ("beanContext".equals(propertyName) &&
                containsKey(source)                    &&
                ((BCSChild)children.get(source)).isRemovePending()) {
                BeanContext bc = getBeanContextPeer();

                if (bc.equals(pce.getOldValue()) && !bc.equals(pce.getNewValue())) {
                    remove(source, false);
                } else {
                    ((BCSChild)children.get(source)).setRemovePending(false);
                }
            }
        }
    }

    /**
     * <p>
     * 此类的子类可以重写或封装此方法，以在子对象被添加到 BeanContext 之前添加验证行为。
     * </p>
     *
     * @param targetChild 代表要创建子对象的对象
     * @return 如果子对象可以添加到此 BeanContext，则返回 true，否则返回 false。
     */

    protected boolean validatePendingAdd(Object targetChild) {
        return true;
    }

    /**
     * <p>
     * 此类的子类可以重写或封装此方法，以在子对象被从 BeanContext 移除之前添加验证行为。
     * </p>
     *
     * @param targetChild 代表要创建子对象的对象
     * @return 如果子对象可以被从此 BeanContext 移除，则返回 true，否则返回 false。
     */

    protected boolean validatePendingRemove(Object targetChild) {
        return true;
    }

    /**
     * 子类可以重写此方法以扩展 add() 语义，在子对象被添加后且事件通知发生前调用。该方法在子对象同步时被调用。
     * @param child 子对象
     * @param bcsc BCSChild
     */

    protected void childJustAddedHook(Object child, BCSChild bcsc) {
    }

    /**
     * 子类可以重写此方法以扩展 remove() 语义，在子对象被移除后且事件通知发生前调用。该方法在子对象同步时被调用。
     * @param child 子对象
     * @param bcsc BCSChild
     */

    protected void childJustRemovedHook(Object child, BCSChild bcsc) {
    }

    /**
     * 获取与指定子对象关联的 Component（如果有）。
     * @param child 指定的子对象
     * @return 与指定子对象关联的 Component（如果有）。
     */
    protected static final Visibility getChildVisibility(Object child) {
        try {
            return (Visibility)child;
        } catch (ClassCastException cce) {
            return null;
        }
    }

    /**
     * 获取与指定子对象关联的 Serializable（如果有）。
     * @param child 指定的子对象
     * @return 与指定子对象关联的 Serializable（如果有）。
     */
    protected static final Serializable getChildSerializable(Object child) {
        try {
            return (Serializable)child;
        } catch (ClassCastException cce) {
            return null;
        }
    }

    /**
     * 获取指定子对象的 PropertyChangeListener（如果有）。
     * @param child 指定的子对象
     * @return 指定子对象的 PropertyChangeListener（如果有）。
     */
    protected static final PropertyChangeListener getChildPropertyChangeListener(Object child) {
        try {
            return (PropertyChangeListener)child;
        } catch (ClassCastException cce) {
            return null;
        }
    }

    /**
     * 获取指定子对象的 VetoableChangeListener（如果有）。
     * @param child 指定的子对象
     * @return 指定子对象的 VetoableChangeListener（如果有）。
     */
    protected static final VetoableChangeListener getChildVetoableChangeListener(Object child) {
        try {
            return (VetoableChangeListener)child;
        } catch (ClassCastException cce) {
            return null;
        }
    }

    /**
     * 获取指定子对象的 BeanContextMembershipListener（如果有）。
     * @param child 指定的子对象
     * @return 指定子对象的 BeanContextMembershipListener（如果有）。
     */
    protected static final BeanContextMembershipListener getChildBeanContextMembershipListener(Object child) {
        try {
            return (BeanContextMembershipListener)child;
        } catch (ClassCastException cce) {
            return null;
        }
    }

    /**
     * 获取指定子对象的 BeanContextChild（如果有）。
     * @param child 指定的子对象
     * @return 指定子对象的 BeanContextChild（如果有）
     * @throws  IllegalArgumentException 如果子对象同时实现了 BeanContextChild 和 BeanContextProxy
     */
    protected static final BeanContextChild getChildBeanContextChild(Object child) {
        try {
            BeanContextChild bcc = (BeanContextChild)child;

            if (child instanceof BeanContextChild && child instanceof BeanContextProxy)
                throw new IllegalArgumentException("子对象不能同时实现 BeanContextChild 和 BeanContextProxy");
            else
                return bcc;
        } catch (ClassCastException cce) {
            try {
                return ((BeanContextProxy)child).getBeanContextProxy();
            } catch (ClassCastException cce1) {
                return null;
            }
        }
    }

    /**
     * 在 BeanContextMembershipListener 接口上触发 BeanContextshipEvent。
     * @param bcme 要触发的事件
     */

    protected final void fireChildrenAdded(BeanContextMembershipEvent bcme) {
        Object[] copy;

        synchronized(bcmListeners) { copy = bcmListeners.toArray(); }

        for (int i = 0; i < copy.length; i++)
            ((BeanContextMembershipListener)copy[i]).childrenAdded(bcme);
    }

    /**
     * 在 BeanContextMembershipListener 接口上触发 BeanContextshipEvent。
     * @param bcme 要触发的事件
     */

    protected final void fireChildrenRemoved(BeanContextMembershipEvent bcme) {
        Object[] copy;

        synchronized(bcmListeners) { copy = bcmListeners.toArray(); }

        for (int i = 0; i < copy.length; i++)
            ((BeanContextMembershipListener)copy[i]).childrenRemoved(bcme);
    }

    /**
     * 从构造函数和 readObject 调用的受保护方法，用于初始化 BeanContextSupport 实例的瞬态状态。
     *
     * 此类使用此方法实例化用于监控子对象 PropertyChange 和 VetoableChange 事件的内部类监听器。
     *
     * 子类可以封装此方法以添加自己的初始化行为。
     */

    protected synchronized void initialize() {
        children     = new HashMap(serializable + 1);
        bcmListeners = new ArrayList(1);

        childPCL = new PropertyChangeListener() {

            /*
             * 此适配器用于将子对象的属性更改转发到 BeanContext，避免由于行为不当的 Serializable 子对象而导致的 BeanContext 误序列化。
             */

            public void propertyChange(PropertyChangeEvent pce) {
                BeanContextSupport.this.propertyChange(pce);
            }
        };

        childVCL = new VetoableChangeListener() {

            /*
             * 此适配器用于将子对象的否决更改转发到 BeanContext，避免由于行为不当的 Serializable 子对象而导致的 BeanContext 误序列化。
             */

            public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException {
                BeanContextSupport.this.vetoableChange(pce);
             }
        };
    }

    /**
     * 获取此 BeanContext 的子对象副本。
     * @return 当前嵌套子对象的副本
     */
    protected final Object[] copyChildren() {
        synchronized(children) { return children.keySet().toArray(); }
    }

    /**
     * 测试两个类对象或它们的名称是否相等。
     * @param first 第一个对象
     * @param second 第二个对象
     * @return 如果相等则返回 true，否则返回 false
     */
    protected static final boolean classEquals(Class first, Class second) {
        return first.equals(second) || first.getName().equals(second.getName());
    }


    /*
     * 字段
     */


    /**
     * 对 <code> protected HashMap children </code> 字段的所有访问都应在此对象上同步。
     */
    protected transient HashMap         children;

    private             int             serializable  = 0; // 可序列化的子对象

    /**
     * 对 <code> protected ArrayList bcmListeners </code> 字段的所有访问都应在此对象上同步。
     */
    protected transient ArrayList       bcmListeners;


                //

    /**
     * 此 BeanContext 的当前区域设置。
     */
    protected           Locale          locale;

    /**
     * 一个 <tt>boolean</tt> 值，指示此实例是否可以渲染 GUI。
     */
    protected           boolean         okToUseGui;


    /**
     * 一个 <tt>boolean</tt> 值，指示此对象是否当前处于设计时模式。
     */
    protected           boolean         designTime;

    /*
     * transient
     */

    private transient PropertyChangeListener childPCL;

    private transient VetoableChangeListener childVCL;

    private transient boolean                serializing;
}
