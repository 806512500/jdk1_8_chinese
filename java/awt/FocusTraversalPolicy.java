/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 一个 FocusTraversalPolicy 定义了在特定焦点循环根中组件的遍历顺序。实例可以应用于任意的焦点循环根，允许它们在容器之间共享。当组件层次结构的焦点循环根发生变化时，它们不需要重新初始化。
 * <p>
 * FocusTraversalPolicy 的核心职责是提供算法，确定在用户界面中向前或向后遍历时下一个和上一个应获得焦点的组件。每个 FocusTraversalPolicy 还必须提供算法，确定遍历周期中的第一个、最后一个和默认组件。第一个和最后一个组件分别用于正常前向和后向遍历的循环。默认组件是首次进入新的焦点遍历周期时接收焦点的组件。
 * 一个 FocusTraversalPolicy 可以选择性地提供算法，确定窗口的初始组件。初始组件是窗口首次显示时接收焦点的组件。
 * <p>
 * FocusTraversalPolicy 考虑 <a
 * href="doc-files/FocusSpec.html#FocusTraversalPolicyProviders">焦点遍历策略提供者</a>。当搜索第一个/最后一个/下一个/上一个组件时，如果遇到焦点遍历策略提供者，将使用其焦点遍历策略执行搜索操作。
 * <p>
 * 请参阅
 * <a href="https://docs.oracle.com/javase/tutorial/uiswing/misc/focus.html">
 * 如何使用焦点子系统</a>，
 * 《Java教程》中的一个部分，以及
 * <a href="../../java/awt/doc-files/FocusSpec.html">焦点规范</a>
 * 以获取更多信息。
 *
 * @author David Mendenhall
 *
 * @see Container#setFocusTraversalPolicy
 * @see Container#getFocusTraversalPolicy
 * @see Container#setFocusCycleRoot
 * @see Container#isFocusCycleRoot
 * @see Container#setFocusTraversalPolicyProvider
 * @see Container#isFocusTraversalPolicyProvider
 * @see KeyboardFocusManager#setDefaultFocusTraversalPolicy
 * @see KeyboardFocusManager#getDefaultFocusTraversalPolicy
 * @since 1.4
 */
public abstract class FocusTraversalPolicy {

    /**
     * 返回 aComponent 之后应接收焦点的组件。aContainer 必须是 aComponent 的焦点循环根或焦点遍历策略提供者。
     *
     * @param aContainer aComponent 的焦点循环根或焦点遍历策略提供者
     * @param aComponent aContainer 的（可能是间接的）子组件，或 aContainer 本身
     * @return aComponent 之后应接收焦点的组件，如果没有找到合适的组件则返回 null
     * @throws IllegalArgumentException 如果 aContainer 不是 aComponent 的焦点循环根或焦点遍历策略提供者，或者 aContainer 或 aComponent 为 null
     */
    public abstract Component getComponentAfter(Container aContainer,
                                                Component aComponent);

    /**
     * 返回 aComponent 之前应接收焦点的组件。aContainer 必须是 aComponent 的焦点循环根或焦点遍历策略提供者。
     *
     * @param aContainer aComponent 的焦点循环根或焦点遍历策略提供者
     * @param aComponent aContainer 的（可能是间接的）子组件，或 aContainer 本身
     * @return aComponent 之前应接收焦点的组件，如果没有找到合适的组件则返回 null
     * @throws IllegalArgumentException 如果 aContainer 不是 aComponent 的焦点循环根或焦点遍历策略提供者，或者 aContainer 或 aComponent 为 null
     */
    public abstract Component getComponentBefore(Container aContainer,
                                                 Component aComponent);

    /**
     * 返回遍历周期中的第一个组件。此方法用于确定前向遍历时下一个应接收焦点的组件。
     *
     * @param aContainer 要返回第一个组件的焦点循环根或焦点遍历策略提供者
     * @return aContainer 遍历周期中的第一个组件，如果没有找到合适的组件则返回 null
     * @throws IllegalArgumentException 如果 aContainer 为 null
     */
    public abstract Component getFirstComponent(Container aContainer);

    /**
     * 返回遍历周期中的最后一个组件。此方法用于确定反向遍历时下一个应接收焦点的组件。
     *
     * @param aContainer 要返回最后一个组件的焦点循环根或焦点遍历策略提供者
     * @return aContainer 遍历周期中的最后一个组件，如果没有找到合适的组件则返回 null
     * @throws IllegalArgumentException 如果 aContainer 为 null
     */
    public abstract Component getLastComponent(Container aContainer);

    /**
     * 返回默认应接收焦点的组件。此组件将是首次进入以 aContainer 为根的新焦点遍历周期时接收焦点的组件。
     *
     * @param aContainer 要返回默认组件的焦点循环根或焦点遍历策略提供者
     * @return aContainer 遍历周期中的默认组件，如果没有找到合适的组件则返回 null
     * @throws IllegalArgumentException 如果 aContainer 为 null
     */
    public abstract Component getDefaultComponent(Container aContainer);

    /**
     * 返回窗口首次显示时应接收焦点的组件。窗口通过调用 <code>show()</code> 或 <code>setVisible(true)</code> 显示后，初始组件将不再使用。相反，如果窗口失去并重新获得焦点，或变为不可见或不可显示，然后再次变为可见和可显示，窗口的最近聚焦组件将成为焦点所有者。此方法的默认实现返回默认组件。
     *
     * @param window 要返回初始组件的窗口
     * @return 窗口首次显示时应接收焦点的组件，如果没有找到合适的组件则返回 null
     * @see #getDefaultComponent
     * @see Window#getMostRecentFocusOwner
     * @throws IllegalArgumentException 如果 window 为 null
     */
    public Component getInitialComponent(Window window) {
        if ( window == null ){
            throw new IllegalArgumentException("window cannot be equal to null.");
        }
        Component def = getDefaultComponent(window);
        if (def == null && window.isFocusableWindow()) {
            def = window;
        }
        return def;
    }
}
