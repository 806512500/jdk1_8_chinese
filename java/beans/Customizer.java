/*
 * Copyright (c) 1996, Oracle and/or its affiliates. All rights reserved.
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
 * 自定义类提供了一个完整的自定义 GUI，用于自定义目标 Java Bean。
 * <P>
 * 每个自定义类都应继承自 java.awt.Component 类，以便可以在 AWT 对话框或面板中实例化。
 * <P>
 * 每个自定义类都应有一个无参构造函数。
 */

public interface Customizer {

    /**
     * 设置要自定义的对象。此方法应仅调用一次，在 Customizer 被添加到任何父 AWT 容器之前。
     * @param bean  要自定义的对象。
     */
    void setObject(Object bean);

    /**
     * 注册一个 PropertyChange 事件的监听器。每当自定义器以可能需要刷新显示属性的方式更改目标 bean 时，
     * 应触发一个 PropertyChange 事件。
     *
     * @param listener  当触发 PropertyChange 事件时要调用的对象。
     */
     void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * 移除 PropertyChange 事件的监听器。
     *
     * @param listener  要移除的 PropertyChange 监听器。
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

}
