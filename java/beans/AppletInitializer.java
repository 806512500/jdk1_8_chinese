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

package java.beans;

import java.applet.Applet;

import java.beans.beancontext.BeanContext;

/**
 * <p>
 * 该接口旨在与 java.beans.Beans.instantiate 协同工作。
 * 该接口的目的是在通过 java.beans.Beans.instantiate() 实例化时，提供一种机制来正确初始化也是 Applet 的 JavaBeans。
 * </p>
 *
 * @see java.beans.Beans#instantiate
 *
 * @since 1.2
 *
 */


public interface AppletInitializer {

    /**
     * <p>
     * 如果传递给 java.beans.Beans.instantiate 的适当变体，此方法将被调用，以便将新实例化的 Applet (JavaBean) 与其 AppletContext、AppletStub 和 Container 关联。
     * </p>
     * <p>
     * 符合的实现应：
     * <ol>
     * <li> 将新实例化的 Applet 与适当的 AppletContext 关联。
     *
     * <li> 实例化一个 AppletStub() 并通过调用 setStub() 将该 AppletStub 与 Applet 关联。
     *
     * <li> 如果 BeanContext 参数为 null，则应通过调用 add() 将 Applet 添加到其 Container 中，从而将其与适当的 Container 关联。如果 BeanContext 参数非 null，则在调用其 addChildren() 方法期间，BeanContext 负责将 Applet 与其 Container 关联。
     * </ol>
     *
     * @param newAppletBean  新实例化的 JavaBean
     * @param bCtxt          为此 Applet 预定的 BeanContext，或 null。
     */

    void initialize(Applet newAppletBean, BeanContext bCtxt);

    /**
     * <p>
     * 激活和/或标记 Applet 为活动状态。实现此接口的类应将此 Applet 标记为活动状态，并可选地调用其 start() 方法。
     * </p>
     *
     * @param newApplet  新实例化的 JavaBean
     */

    void activate(Applet newApplet);
}
