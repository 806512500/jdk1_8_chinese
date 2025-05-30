/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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

/**
 * <p>
 * 此接口旨在由 java.beans.beancontext.BeanContext 的实例实现或委托，以便向其嵌套的
 * java.beans.beancontext.BeanContextChild 实例层次结构传播当前的 "designTime" 属性。
 * <p>
 * JavaBeans&trade; 规范定义了设计时间的概念，即 JavaBeans 实例在交互式设计、组合或构建工具中
 * 组成和自定义时应具有的模式，与运行时相反，即 JavaBean 是 applet、应用程序或其他活动的 Java 可执行抽象的一部分。
 *
 * @author Laurence P. G. Cable
 * @since 1.2
 *
 * @see java.beans.beancontext.BeanContext
 * @see java.beans.beancontext.BeanContextChild
 * @see java.beans.beancontext.BeanContextMembershipListener
 * @see java.beans.PropertyChangeEvent
 */

public interface DesignMode {

    /**
     * 从 BeanContext 或其他 PropertyChangeEvents 源触发的 propertyName 的标准值。
     */

    static String PROPERTYNAME = "designTime";

    /**
     * 设置 "designTime" 属性的 "value"。
     * <p>
     * 如果实现对象是 java.beans.beancontext.BeanContext 的实例，或其子接口，则该 BeanContext 应触发一个
     * PropertyChangeEvent，向其注册的 BeanContextMembershipListeners 发送，参数为：
     * <ul>
     *    <li><code>propertyName</code> - <code>java.beans.DesignMode.PROPERTYNAME</code>
     *    <li><code>oldValue</code> - "designTime" 的先前值
     *    <li><code>newValue</code> - "designTime" 的当前值
     * </ul>
     * 注意，BeanContextChild 不得调用与其嵌套的 BeanContext 相关的此方法。
     *
     * @param designTime  "designTime" 属性的当前 "value"
     * @see java.beans.beancontext.BeanContext
     * @see java.beans.beancontext.BeanContextMembershipListener
     * @see java.beans.PropertyChangeEvent
     */

    void setDesignTime(boolean designTime);

    /**
     * true 表示 JavaBeans 应在设计时间模式下运行，false 表示运行时行为。
     *
     * @return "designTime" 属性的当前 "value"。
     */

    boolean isDesignTime();
}
