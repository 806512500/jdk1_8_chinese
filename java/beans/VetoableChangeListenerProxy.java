/*
 * Copyright (c) 2000, Oracle and/or its affiliates. All rights reserved.
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

package java.beans;

import java.util.EventListenerProxy;

/**
 * 一个扩展了 {@code EventListenerProxy} 的类，
 * 专门用于添加一个带有“约束”属性的 {@code VetoableChangeListener}。
 * 该类的实例可以作为 {@code VetoableChangeListener} 添加到
 * 支持触发可否决更改事件的 bean 中。
 * <p>
 * 如果对象有一个 {@code getVetoableChangeListeners} 方法，
 * 那么返回的数组可以是 {@code VetoableChangeListener} 和
 * {@code VetoableChangeListenerProxy} 对象的混合体。
 *
 * @see java.util.EventListenerProxy
 * @see VetoableChangeSupport#getVetoableChangeListeners
 * @since 1.4
 */
public class VetoableChangeListenerProxy
        extends EventListenerProxy<VetoableChangeListener>
        implements VetoableChangeListener {

    private final String propertyName;

    /**
     * 构造函数，将 {@code VetoableChangeListener}
     * 绑定到特定属性。
     *
     * @param propertyName  要监听的属性名称
     * @param listener      监听器对象
     */
    public VetoableChangeListenerProxy(String propertyName, VetoableChangeListener listener) {
        super(listener);
        this.propertyName = propertyName;
    }

    /**
    * 将属性更改事件转发给监听器代理。
    *
    * @param event  属性更改事件
    *
    * @exception PropertyVetoException 如果接收者希望撤销属性更改
    */
    public void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException{
        getListener().vetoableChange(event);
    }

    /**
     * 返回与监听器关联的命名属性的名称。
     *
     * @return 与监听器关联的命名属性的名称
     */
    public String getPropertyName() {
        return this.propertyName;
    }
}
