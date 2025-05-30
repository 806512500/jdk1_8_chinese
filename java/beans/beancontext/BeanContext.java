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

import java.beans.DesignMode;
import java.beans.Visibility;

import java.io.InputStream;
import java.io.IOException;

import java.net.URL;

import java.util.Collection;
import java.util.Locale;

/**
 * <p>
 * BeanContext 作为 JavaBeans 的逻辑层次容器。
 * </p>
 *
 * @author Laurence P. G. Cable
 * @since 1.2
 *
 * @see java.beans.Beans
 * @see java.beans.beancontext.BeanContextChild
 * @see java.beans.beancontext.BeanContextMembershipListener
 * @see java.beans.PropertyChangeEvent
 * @see java.beans.DesignMode
 * @see java.beans.Visibility
 * @see java.util.Collection
 */

@SuppressWarnings("rawtypes")
public interface BeanContext extends BeanContextChild, Collection, DesignMode, Visibility {

    /**
     * 实例化一个名为 beanName 的 JavaBean 作为此 <code>BeanContext</code> 的子对象。
     * JavaBean 的实现由 beanName 参数的值派生，并由
     * <code>java.beans.Beans.instantiate()</code> 方法定义。
     *
     * @return 作为此 <code>BeanContext</code> 子对象的 JavaBean
     * @param beanName 要实例化为此 <code>BeanContext</code> 子对象的 JavaBean 的名称
     * @throws IOException 如果发生 IO 问题
     * @throws ClassNotFoundException 如果未找到由 beanName 参数标识的类
     */
    Object instantiateChild(String beanName) throws IOException, ClassNotFoundException;

    /**
     * 类似于 <code>java.lang.ClassLoader.getResourceAsStream()</code>，
     * 此方法允许 <code>BeanContext</code> 实现
     * 在子 <code>Component</code> 和底层 <code>ClassLoader</code> 之间插入行为。
     *
     * @param name 资源名称
     * @param bcc 指定的子对象
     * @return 用于读取资源的 <code>InputStream</code>，
     * 或者如果找不到资源，则返回 <code>null</code>。
     * @throws IllegalArgumentException 如果资源无效
     */
    InputStream getResourceAsStream(String name, BeanContextChild bcc) throws IllegalArgumentException;

    /**
     * 类似于 <code>java.lang.ClassLoader.getResource()</code>，此方法允许
     * <code>BeanContext</code> 实现在子 <code>Component</code>
     * 和底层 <code>ClassLoader</code> 之间插入行为。
     *
     * @param name 资源名称
     * @param bcc 指定的子对象
     * @return 指定子对象的命名资源的 <code>URL</code>
     * @throws IllegalArgumentException 如果资源无效
     */
    URL getResource(String name, BeanContextChild bcc) throws IllegalArgumentException;

     /**
      * 添加指定的 <code>BeanContextMembershipListener</code> 以接收
      * 从此 <code>BeanContext</code> 发出的 <code>BeanContextMembershipEvents</code>
      * 每当添加或移除子 <code>Component</code>(s) 时。
      *
      * @param bcml 要添加的 BeanContextMembershipListener
      */
    void addBeanContextMembershipListener(BeanContextMembershipListener bcml);

     /**
      * 移除指定的 <code>BeanContextMembershipListener</code>
      * 使其不再接收子 <code>Component</code>(s) 添加或移除时的
      * <code>BeanContextMembershipEvent</code>。
      *
      * @param bcml 要移除的 <code>BeanContextMembershipListener</code>
      */
    void removeBeanContextMembershipListener(BeanContextMembershipListener bcml);

    /**
     * 此全局锁由 <code>BeanContext</code> 和 <code>BeanContextServices</code> 实现者使用，
     * 用于序列化 <code>BeanContext</code> 层次结构中的更改和任何服务请求等。
     */
    public static final Object globalHierarchyLock = new Object();
}
