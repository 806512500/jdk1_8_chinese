/*
 * Copyright (c) 1996, 1997, Oracle and/or its affiliates. All rights reserved.
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
 * 当一个 bean 更改了“受约束”的属性时，会触发一个 VetoableChange 事件。你可以注册一个
 * VetoableChangeListener 到源 bean，以便在任何受约束的属性更新时收到通知。
 */
public interface VetoableChangeListener extends java.util.EventListener {
    /**
     * 当一个受约束的属性被更改时，此方法将被调用。
     *
     * @param     evt 一个 <code>PropertyChangeEvent</code> 对象，描述了事件源和已更改的属性。
     * @exception PropertyVetoException 如果接收者希望撤销属性更改。
     */
    void vetoableChange(PropertyChangeEvent evt)
                                throws PropertyVetoException;
}
