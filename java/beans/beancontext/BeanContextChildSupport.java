/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

import java.beans.PropertyVetoException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * <p>
 * 这是一个通用支持类，用于实现 BeanContextChild 协议。
 *
 * 该类可以被直接子类化，或者被封装并委托给以实现给定组件的此接口。
 * </p>
 *
 * @author      Laurence P. G. Cable
 * @since       1.2
 *
 * @see java.beans.beancontext.BeanContext
 * @see java.beans.beancontext.BeanContextServices
 * @see java.beans.beancontext.BeanContextChild
 */

public class BeanContextChildSupport implements BeanContextChild, BeanContextServicesListener, Serializable {

    static final long serialVersionUID = 6328947014421475877L;

    /**
     * 构造一个 BeanContextChildSupport，其中此类已被子类化以实现 JavaBean 组件本身。
     */

    public BeanContextChildSupport() {
        super();

        beanContextChildPeer = this;

        pcSupport = new PropertyChangeSupport(beanContextChildPeer);
        vcSupport = new VetoableChangeSupport(beanContextChildPeer);
    }

    /**
     * 构造一个 BeanContextChildSupport，其中 JavaBean 组件本身实现了 BeanContextChild，并封装此对象，将该接口委托给此实现。
     * @param bcc 底层的 bean context child
     */

    public BeanContextChildSupport(BeanContextChild bcc) {
        super();

        beanContextChildPeer = (bcc != null) ? bcc : this;

        pcSupport = new PropertyChangeSupport(beanContextChildPeer);
        vcSupport = new VetoableChangeSupport(beanContextChildPeer);
    }

    /**
     * 设置此 BeanContextChildSupport 的 BeanContext。
     * @param bc 要分配给 BeanContext 属性的新值
     * @throws PropertyVetoException 如果更改被拒绝
     */
    public synchronized void setBeanContext(BeanContext bc) throws PropertyVetoException {
        if (bc == beanContext) return;

        BeanContext oldValue = beanContext;
        BeanContext newValue = bc;

        if (!rejectedSetBCOnce) {
            if (rejectedSetBCOnce = !validatePendingSetBeanContext(bc)) {
                throw new PropertyVetoException(
                    "setBeanContext() change rejected:",
                    new PropertyChangeEvent(beanContextChildPeer, "beanContext", oldValue, newValue)
                );
            }

            try {
                fireVetoableChange("beanContext",
                                   oldValue,
                                   newValue
                );
            } catch (PropertyVetoException pve) {
                rejectedSetBCOnce = true;

                throw pve; // 重新抛出
            }
        }

        if (beanContext != null) releaseBeanContextResources();

        beanContext       = newValue;
        rejectedSetBCOnce = false;

        firePropertyChange("beanContext",
                           oldValue,
                           newValue
        );

        if (beanContext != null) initializeBeanContextResources();
    }

    /**
     * 获取此 BeanContextChildSupport 的嵌套 BeanContext。
     * @return 此 BeanContextChildSupport 的嵌套 BeanContext。
     */
    public synchronized BeanContext getBeanContext() { return beanContext; }

    /**
     * 为特定属性添加 PropertyChangeListener。
     * 同一个监听器对象可以被添加多次。对于每个属性，监听器将被调用其被添加的次数。
     * 如果 name 或 pcl 为 null，则不抛出异常且不采取任何操作。
     *
     * @param name 要监听的属性的名称
     * @param pcl 要添加的 PropertyChangeListener
     */
    public void addPropertyChangeListener(String name, PropertyChangeListener pcl) {
        pcSupport.addPropertyChangeListener(name, pcl);
    }

    /**
     * 为特定属性移除 PropertyChangeListener。
     * 如果 pcl 被添加到同一事件源的指定属性多次，则移除后将少通知一次。
     * 如果 name 为 null，则不抛出异常且不采取任何操作。
     * 如果 pcl 为 null，或者从未为指定属性添加过，则不抛出异常且不采取任何操作。
     *
     * @param name 已监听的属性的名称
     * @param pcl 要移除的 PropertyChangeListener
     */
    public void removePropertyChangeListener(String name, PropertyChangeListener pcl) {
        pcSupport.removePropertyChangeListener(name, pcl);
    }

    /**
     * 为特定属性添加 VetoableChangeListener。
     * 同一个监听器对象可以被添加多次。对于每个属性，监听器将被调用其被添加的次数。
     * 如果 name 或 vcl 为 null，则不抛出异常且不采取任何操作。
     *
     * @param name 要监听的属性的名称
     * @param vcl 要添加的 VetoableChangeListener
     */
    public void addVetoableChangeListener(String name, VetoableChangeListener vcl) {
        vcSupport.addVetoableChangeListener(name, vcl);
    }

    /**
     * 移除 VetoableChangeListener。
     * 如果 pcl 被添加到同一事件源的指定属性多次，则移除后将少通知一次。
     * 如果 name 为 null，则不抛出异常且不采取任何操作。
     * 如果 vcl 为 null，或者从未为指定属性添加过，则不抛出异常且不采取任何操作。
     *
     * @param name 已监听的属性的名称
     * @param vcl 要移除的 VetoableChangeListener
     */
    public void removeVetoableChangeListener(String name, VetoableChangeListener vcl) {
        vcSupport.removeVetoableChangeListener(name, vcl);
    }

    /**
     * 嵌套 BeanContext 提供的服务已被撤销。
     *
     * 子类可以重写此方法以实现自己的行为。
     * @param bcsre 由于服务被撤销而触发的 BeanContextServiceRevokedEvent
     */
    public void serviceRevoked(BeanContextServiceRevokedEvent bcsre) { }

    /**
     * 嵌套 BeanContext 提供了新的服务。
     *
     * 子类可以重写此方法以实现自己的行为。
     * @param bcsae 由于服务可用而触发的 BeanContextServiceAvailableEvent
     */
    public void serviceAvailable(BeanContextServiceAvailableEvent bcsae) { }

    /**
     * 获取与此 BeanContextChildSupport 关联的 BeanContextChild。
     *
     * @return 此类的 BeanContextChild 同行
     */
    public BeanContextChild getBeanContextChildPeer() { return beanContextChildPeer; }

    /**
     * 报告此类是否是另一个类的代理。
     *
     * @return 如果此类是另一个类的代理，则返回 true
     */
    public boolean isDelegated() { return !this.equals(beanContextChildPeer); }

    /**
     * 向任何已注册的监听器报告绑定属性的更新。如果 old 和 new 相等且非 null，则不触发事件。
     * @param name 已更改的属性的程序名称
     * @param oldValue 属性的旧值
     * @param newValue 属性的新值
     */
    public void firePropertyChange(String name, Object oldValue, Object newValue) {
        pcSupport.firePropertyChange(name, oldValue, newValue);
    }

    /**
     * 向任何已注册的监听器报告可否决属性的更新。
     * 如果有人否决更改，则触发一个新事件，将所有人恢复到旧值，然后重新抛出 PropertyVetoException。
     * 如果 old 和 new 相等且非 null，则不触发事件。
     * @param name 即将更改的属性的程序名称
     * @param oldValue 属性的旧值
     * @param newValue 属性的新值
     * @throws PropertyVetoException 如果接收者希望撤销属性更改
     */
    public void fireVetoableChange(String name, Object oldValue, Object newValue) throws PropertyVetoException {
        vcSupport.fireVetoableChange(name, oldValue, newValue);
    }

    /**
     * 从 setBeanContext 调用以验证（或以其他方式）嵌套 BeanContext 属性值的待处理更改。
     * 返回 false 将导致 setBeanContext 抛出 PropertyVetoException。
     * @param newValue 请求的 BeanContext 属性的新值
     * @return 如果更改操作应被否决，则返回 true
     */
    public boolean validatePendingSetBeanContext(BeanContext newValue) {
        return true;
    }

    /**
     * 子类可以重写此方法以提供自己的释放行为。当调用此方法时，应释放从此实例当前 BeanContext 属性获取的任何资源，因为对象不再嵌套在该 BeanContext 中。
     */

    protected  void releaseBeanContextResources() {
        // 什么都不做
    }

    /**
     * 子类可以重写此方法以提供自己的初始化行为。当调用此方法时，应从当前 BeanContext 获取 BeanContextChild 所需的任何资源。
     */

    protected void initializeBeanContextResources() {
        // 什么都不做
    }

    /**
     * 写入对象的持久状态。
     */

    private void writeObject(ObjectOutputStream oos) throws IOException {

        /*
         * 如果我们是代理且代理对象不是可序列化的，则不进行序列化。
         */

        if (!equals(beanContextChildPeer) && !(beanContextChildPeer instanceof Serializable))
            throw new IOException("BeanContextChildSupport beanContextChildPeer not Serializable");

        else
            oos.defaultWriteObject();

    }


    /**
     * 恢复持久对象，必须等待后续的 setBeanContext() 以完全恢复从新的嵌套 BeanContext 获取的任何资源。
     */

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }

    /*
     * 字段
     */

    /**
     * 此 BeanContextChild 嵌套在其中的 BeanContext。
     */
    public    BeanContextChild      beanContextChildPeer;

   /**
    * 与此 BeanContextChildSupport 关联的 PropertyChangeSupport。
    */
    protected PropertyChangeSupport pcSupport;

   /**
    * 与此 BeanContextChildSupport 关联的 VetoableChangeSupport。
    */
    protected VetoableChangeSupport vcSupport;

    /**
     * bean context。
     */
    protected transient BeanContext           beanContext;

   /**
    * 表示至少有一个 PropertyChangeVetoException
    * 被抛出用于尝试的 setBeanContext 操作。
    */
    protected transient boolean               rejectedSetBCOnce;

}
