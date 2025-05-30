/*
 * Copyright (c) 1998, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.beans.beancontext.BeanContextEvent;

import java.beans.beancontext.BeanContextServices;

/**
 * <p>
 * 此事件类型由 <code>BeanContextServiceRevokedListener</code> 使用，以标识正在撤销的服务。
 * </p>
 */
public class BeanContextServiceRevokedEvent extends BeanContextEvent {
    private static final long serialVersionUID = -1295543154724961754L;

    /**
     * 构造一个 <code>BeanContextServiceEvent</code>。
     * @param bcs 正在从中撤销此服务的 <code>BeanContextServices</code>
     * @param sc 正在撤销的服务
     * @param invalidate 立即撤销时为 <code>true</code>
     */
    public BeanContextServiceRevokedEvent(BeanContextServices bcs, Class sc, boolean invalidate) {
        super((BeanContext)bcs);

        serviceClass    = sc;
        invalidateRefs  = invalidate;
    }

    /**
     * 获取源作为 <code>BeanContextServices</code> 类型的引用
     * @return 正在从中撤销此服务的 <code>BeanContextServices</code>
     */
    public BeanContextServices getSourceAsBeanContextServices() {
        return (BeanContextServices)getBeanContext();
    }

    /**
     * 获取此通知的主题服务类
     * @return 一个指向正在撤销的服务的 <code>Class</code> 引用
     */
    public Class getServiceClass() { return serviceClass; }

    /**
     * 检查此事件以确定正在撤销的服务是否属于特定类。
     * @param service 感兴趣的服务（应为非空）
     * @return 如果正在撤销的服务与指定的服务属于同一类，则返回 <code>true</code>
     */
    public boolean isServiceClass(Class service) {
        return serviceClass.equals(service);
    }

    /**
     * 报告当前服务是否正在被强制撤销，如果是，则引用已失效且无法使用。
     * @return 如果当前服务正在被强制撤销，则返回 <code>true</code>
     */
    public boolean isCurrentServiceInvalidNow() { return invalidateRefs; }

    /**
     * 字段
     */

    /**
     * 一个指向正在撤销的服务的 <code>Class</code> 引用。
     */
    protected Class                      serviceClass;
    private   boolean                    invalidateRefs;
}
