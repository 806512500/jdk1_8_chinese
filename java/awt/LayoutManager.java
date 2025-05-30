/*
 * Copyright (c) 1995, 2005, Oracle and/or its affiliates. All rights reserved.
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
package java.awt;

/**
 * 定义了知道如何布局 <code>Container</code> 的类的接口。
 * <p>
 * Swing 的绘制架构假设 <code>JComponent</code> 的子组件不会重叠。如果
 * <code>JComponent</code> 的 <code>LayoutManager</code> 允许子组件重叠，
 * 则 <code>JComponent</code> 必须覆盖 <code>isOptimizedDrawingEnabled</code>
 * 以返回 false。
 *
 * @see Container
 * @see javax.swing.JComponent#isOptimizedDrawingEnabled
 *
 * @author      Sami Shaio
 * @author      Arthur van Hoff
 */
public interface LayoutManager {
    /**
     * 如果布局管理器使用每个组件的字符串，则将组件 <code>comp</code> 添加到布局中，
     * 并将其与 <code>name</code> 指定的字符串关联。
     *
     * @param name 要与组件关联的字符串
     * @param comp 要添加的组件
     */
    void addLayoutComponent(String name, Component comp);

    /**
     * 从布局中移除指定的组件。
     * @param comp 要移除的组件
     */
    void removeLayoutComponent(Component comp);

    /**
     * 计算指定容器的首选尺寸，考虑其包含的组件。
     * @param parent 要布局的容器
     *
     * @see #minimumLayoutSize
     */
    Dimension preferredLayoutSize(Container parent);

    /**
     * 计算指定容器的最小尺寸，考虑其包含的组件。
     * @param parent 要布局的容器
     * @see #preferredLayoutSize
     */
    Dimension minimumLayoutSize(Container parent);

    /**
     * 布局指定的容器。
     * @param parent 要布局的容器
     */
    void layoutContainer(Container parent);
}
