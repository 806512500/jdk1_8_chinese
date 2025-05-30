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

import java.util.Iterator;

import java.util.TooManyListenersException;

import java.beans.beancontext.BeanContext;

import java.beans.beancontext.BeanContextServiceProvider;

import java.beans.beancontext.BeanContextServicesListener;


/**
 * <p>
 * BeanContextServices 接口提供了一种机制，使 BeanContext 可以向其内部的 BeanContextChild 对象公开通用的“服务”。
 * </p>
 */
public interface BeanContextServices extends BeanContext, BeanContextServicesListener {

    /**
     * 向此 BeanContext 添加服务。
     * <code>BeanContextServiceProvider</code> 调用此方法
     * 以在此上下文中注册特定的服务。
     * 如果该服务尚未添加，<code>BeanContextServices</code>
     * 将服务与 <code>BeanContextServiceProvider</code> 关联，
     * 并向所有当前注册的 <code>BeanContextServicesListeners</code>
     * 发送 <code>BeanContextServiceAvailableEvent</code>。
     * 然后该方法返回 <code>true</code>，表示服务添加成功。
     * 如果给定的服务已添加，此方法
     * 仅返回 <code>false</code>。
     * @param serviceClass     要添加的服务
     * @param serviceProvider  与服务关联的 <code>BeanContextServiceProvider</code>
     * @return 如果服务成功添加，返回 true，否则返回 false
     */
    boolean addService(Class serviceClass, BeanContextServiceProvider serviceProvider);

    /**
     * 希望从此上下文中移除当前注册服务的
     * <code>BeanContextServiceProviders</code> 可以通过调用此方法来实现。
     * 在撤销服务时，<code>BeanContextServices</code>
     * 会向其当前注册的
     * <code>BeanContextServiceRevokedListeners</code> 和
     * <code>BeanContextServicesListeners</code> 发送
     * <code>BeanContextServiceRevokedEvent</code>。
     * @param serviceClass 要从此 BeanContextServices 中撤销的服务
     * @param serviceProvider 与要撤销的特定服务关联的 BeanContextServiceProvider
     * @param revokeCurrentServicesNow 值为 <code>true</code>
     * 表示存在异常情况，<code>BeanContextServiceProvider</code> 或
     * <code>BeanContextServices</code> 希望立即
     * 终止对指定服务的所有当前引用。
     */
    void revokeService(Class serviceClass, BeanContextServiceProvider serviceProvider, boolean revokeCurrentServicesNow);

    /**
     * 报告给定服务是否
     * 当前可以从此上下文中获取。
     * @param serviceClass 问题中的服务
     * @return 如果服务可用，返回 true
     */
    boolean hasService(Class serviceClass);

    /**
     * <code>BeanContextChild</code> 或与 <code>BeanContextChild</code> 关联的任意对象
     * 可以通过调用此方法从其嵌套的 <code>BeanContextServices</code>
     * 获取当前注册的服务。调用时，此方法
     * 通过调用底层 <code>BeanContextServiceProvider</code> 的 getService() 方法来获取服务。
     * @param child 与此次请求关联的 <code>BeanContextChild</code>
     * @param requestor 请求服务的对象
     * @param serviceClass 请求的服务类
     * @param serviceSelector 服务依赖的参数
     * @param bcsrl 如果服务稍后被撤销，要通知的
     * <code>BeanContextServiceRevokedListener</code>
     * @throws TooManyListenersException 如果监听器过多
     * @return 请求的此上下文的命名服务的引用，或 <code>null</code>
     */
    Object getService(BeanContextChild child, Object requestor, Class serviceClass, Object serviceSelector, BeanContextServiceRevokedListener bcsrl) throws TooManyListenersException;

    /**
     * 通过调用底层 <code>BeanContextServiceProvider</code> 的 releaseService()
     * 释放 <code>BeanContextChild</code>（或与 BeanContextChild 关联的任意对象）
     * 对指定服务的引用。
     * @param child <code>BeanContextChild</code>
     * @param requestor 请求者
     * @param service 服务
     */
    void releaseService(BeanContextChild child, Object requestor, Object service);

    /**
     * 获取此上下文当前可用的服务。
     * @return 一个包含当前可用服务的 <code>Iterator</code>
     */
    Iterator getCurrentServiceClasses();

    /**
     * 通过调用底层 BeanContextServiceProvider 的 getCurrentServiceSelectors()
     * 获取指定服务的服务依赖服务参数（服务选择器）列表。
     * @param serviceClass 指定的服务
     * @return 指定 serviceClass 的当前可用服务选择器
     */
    Iterator getCurrentServiceSelectors(Class serviceClass);

    /**
     * 向此 BeanContext 添加 <code>BeanContextServicesListener</code>。
     * @param bcsl 要添加的 <code>BeanContextServicesListener</code>
     */
    void addBeanContextServicesListener(BeanContextServicesListener bcsl);

    /**
     * 从此 <code>BeanContext</code> 中移除 <code>BeanContextServicesListener</code>。
     * @param bcsl 要从此上下文中移除的 <code>BeanContextServicesListener</code>
     */
    void removeBeanContextServicesListener(BeanContextServicesListener bcsl);
}
