/*
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Iterator;

/**
 * <p>
 * BeanContext 的主要功能之一是作为 JavaBeans 和 BeanContextServiceProviders 之间的交汇点。
 * </p>
 * <p>
 * 嵌套在 BeanContext 中的 JavaBean 可能会要求该 BeanContext 提供一个基于表示该服务的 Java 类对象的服务实例。
 * </p>
 * <p>
 * 如果该服务已注册到上下文或其嵌套上下文中，在上下文委托其上下文以满足服务请求的情况下，与该服务关联的 BeanContextServiceProvider 将被要求提供该服务的实例。
 * </p>
 * <p>
 * 服务提供者可以始终返回相同的实例，也可以为每次请求构造一个新实例。
 * </p>
 */

public interface BeanContextServiceProvider {

   /**
    * 由 <code>BeanContextServices</code> 调用，此方法请求从此 <code>BeanContextServiceProvider</code> 获取服务实例。
    *
    * @param bcs 与此次请求相关的 <code>BeanContextServices</code>。此参数使 <code>BeanContextServiceProvider</code> 能够区分来自多个来源的服务请求。
    *
    * @param requestor 请求服务的对象
    *
    * @param serviceClass 请求的服务
    *
    * @param serviceSelector 特定服务的服务依赖参数，如果不适用则为 <code>null</code>。
    *
    * @return 请求的服务的引用
    */
    Object getService(BeanContextServices bcs, Object requestor, Class serviceClass, Object serviceSelector);

    /**
     * 由 <code>BeanContextServices</code> 调用，此方法释放嵌套的 <code>BeanContextChild</code>（或与 <code>BeanContextChild</code> 关联的任意对象）对指定服务的引用。
     *
     * @param bcs 与此次释放请求相关的 <code>BeanContextServices</code>
     *
     * @param requestor 请求释放服务的对象
     *
     * @param service 要释放的服务
     */
    public void releaseService(BeanContextServices bcs, Object requestor, Object service);

    /**
     * 由 <code>BeanContextServices</code> 调用，此方法获取指定服务的当前服务选择器。
     * 服务选择器是特定于服务的参数，常见的示例包括：服务实现类的构造函数参数、特定服务的属性值或现有实现映射中的键。
     *
     * @param bcs 此请求的 <code>BeanContextServices</code>
     * @param serviceClass 指定的服务
     * @return 指定 serviceClass 的当前服务选择器
     */
    Iterator getCurrentServiceSelectors(BeanContextServices bcs, Class serviceClass);
}
