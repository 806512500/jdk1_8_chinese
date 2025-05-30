/*
 * Copyright (c) 1997, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.util.EventObject;

import java.beans.beancontext.BeanContext;

/**
 * <p>
 * <code>BeanContextEvent</code> 是所有从 <code>BeanContext</code> 发出的事件的抽象根类，
 * 与 <code>BeanContext</code> 的语义有关。此类引入了一种机制，允许通过 <code>BeanContext</code> 层次结构
 * 传播 <code>BeanContextEvent</code> 子类。<code>setPropagatedFrom()</code>
 * 和 <code>getPropagatedFrom()</code> 方法允许 <code>BeanContext</code> 识别自身为传播事件的来源。
 * </p>
 *
 * @author      Laurence P. G. Cable
 * @since       1.2
 * @see         java.beans.beancontext.BeanContext
 */

public abstract class BeanContextEvent extends EventObject {
    private static final long serialVersionUID = 7267998073569045052L;

    /**
     * 构造一个 BeanContextEvent
     *
     * @param bc        BeanContext 源
     */
    protected BeanContextEvent(BeanContext bc) {
        super(bc);
    }

    /**
     * 获取与此事件关联的 <code>BeanContext</code>。
     * @return 与此事件关联的 <code>BeanContext</code>。
     */
    public BeanContext getBeanContext() { return (BeanContext)getSource(); }

    /**
     * 设置此事件传播自的 <code>BeanContext</code>。
     * @param bc 此事件传播自的 <code>BeanContext</code>
     */
    public synchronized void setPropagatedFrom(BeanContext bc) {
        propagatedFrom = bc;
    }

    /**
     * 获取此事件传播自的 <code>BeanContext</code>。
     * @return 传播此事件的 <code>BeanContext</code>
     */
    public synchronized BeanContext getPropagatedFrom() {
        return propagatedFrom;
    }

    /**
     * 报告此事件是否从其他 <code>BeanContext</code> 传播。
     * @return 如果传播则返回 <code>true</code>，否则返回 <code>false</code>
     */
    public synchronized boolean isPropagated() {
        return propagatedFrom != null;
    }

    /*
     * 字段
     */

    /**
     * 传播此事件的 <code>BeanContext</code>
     */
    protected BeanContext propagatedFrom;
}
