/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;

import java.beans.beancontext.BeanContext;

/**
 * <p>
 * 希望嵌套在 BeanContext 中并获取其执行环境或上下文引用的 JavaBeans 应实现此接口。
 * </p>
 * <p>
 * 符合规范的 BeanContext 在添加 BeanContextChild 对象时，应通过此接口的 setBeanContext() 方法传递自身引用。
 * </p>
 * <p>
 * 注意，BeanContextChild 可能会通过抛出 PropertyVetoException 来拒绝状态更改。
 * </p>
 * <p>
 * 为了使持久化机制在各种场景中正确地作用于 BeanContextChild 实例，实现此接口的类需要将可能包含或表示嵌套 BeanContext 实例或其他从 BeanContext 通过任何未指定机制获取的资源的所有字段或实例变量定义为 transient。
 * </p>
 *
 * @author      Laurence P. G. Cable
 * @since       1.2
 *
 * @see java.beans.beancontext.BeanContext
 * @see java.beans.PropertyChangeEvent
 * @see java.beans.PropertyChangeListener
 * @see java.beans.PropertyVetoException
 * @see java.beans.VetoableChangeListener
 */

public interface BeanContextChild {

    /**
     * <p>
     * 实现此接口的对象应触发一个 java.beans.PropertyChangeEvent，参数为：
     *
     * 属性名 "beanContext"，旧值（之前的嵌套 <code>BeanContext</code> 实例，或 <code>null</code>），
     * 新值（当前的嵌套 <code>BeanContext</code> 实例，或 <code>null</code>）。
     * <p>
     * 此 BeanContextChild 的嵌套 BeanContext 属性值的更改可能通过抛出适当的异常来被拒绝。
     * </p>
     * @param bc 要与此 <code>BeanContextChild</code> 关联的 <code>BeanContext</code>。
     * @throws PropertyVetoException 如果拒绝添加指定的 <code>BeanContext</code>。
     */
    void setBeanContext(BeanContext bc) throws PropertyVetoException;

    /**
     * 获取与此 <code>BeanContextChild</code> 关联的 <code>BeanContext</code>。
     * @return 与此 <code>BeanContextChild</code> 关联的 <code>BeanContext</code>。
     */
    BeanContext getBeanContext();

    /**
     * 添加一个 <code>PropertyChangeListener</code>
     * 到此 <code>BeanContextChild</code>
     * 以便在指定属性更改时接收 <code>PropertyChangeEvent</code>。
     * @param name 要监听的属性名
     * @param pcl 要添加的 <code>PropertyChangeListener</code>
     */
    void addPropertyChangeListener(String name, PropertyChangeListener pcl);

    /**
     * 从此 <code>BeanContextChild</code> 移除一个 <code>PropertyChangeListener</code>
     * 以便在指定属性更改时不再接收 <code>PropertyChangeEvents</code>。
     *
     * @param name 要监听的属性名
     * @param pcl 要移除的 <code>PropertyChangeListener</code>
     */
    void removePropertyChangeListener(String name, PropertyChangeListener pcl);

    /**
     * 添加一个 <code>VetoableChangeListener</code>
     * 到此 <code>BeanContextChild</code>
     * 以便在指定属性更改时接收事件。
     * @param name 要监听的属性名
     * @param vcl 要添加的 <code>VetoableChangeListener</code>
     */
    void addVetoableChangeListener(String name, VetoableChangeListener vcl);

    /**
     * 从此 <code>BeanContextChild</code> 移除一个 <code>VetoableChangeListener</code>
     * 以便在指定属性更改时不再接收事件。
     * @param name 要监听的属性名。
     * @param vcl 要移除的 <code>VetoableChangeListener</code>。
     */
    void removeVetoableChangeListener(String name, VetoableChangeListener vcl);

}
