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

import java.awt.peer.ComponentPeer;


/**
 * 一种基于容器中子组件顺序确定遍历顺序的焦点遍历策略。从特定的焦点循环根开始，该策略对组件层次结构进行先序遍历，并根据 <code>Container.getComponents()</code> 返回的数组顺序遍历容器的子组件。不可见和不可显示的层次结构部分将不会被搜索。
 * <p>
 * 如果客户端代码通过重写 <code>Component.isFocusTraversable()</code> 或 <code>Component.isFocusable()</code>，或调用 <code>Component.setFocusable()</code> 显式设置了组件的可聚焦性，那么 <code>DefaultFocusTraversalPolicy</code> 的行为将与 <code>ContainerOrderFocusTraversalPolicy</code> 完全相同。然而，如果组件依赖于默认的可聚焦性，那么 <code>DefaultFocusTraversalPolicy</code> 将拒绝所有具有不可聚焦对等体的组件。这是所有 AWT 容器的默认焦点遍历策略。
 * <p>
 * 对等体的可聚焦性取决于实现。Sun 建议特定本地平台的所有实现构造具有相同可聚焦性的对等体。对于 Windows 和 Unix，建议画布、标签、面板、滚动条、滚动窗格、窗口和轻量级组件具有不可聚焦的对等体，而所有其他组件具有可聚焦的对等体。这些建议在 Sun AWT 实现中使用。请注意，组件对等体的可聚焦性与组件本身的可聚焦性不同，也不会影响组件本身的可聚焦性。
 * <p>
 * 请参阅
 * <a href="https://docs.oracle.com/javase/tutorial/uiswing/misc/focus.html">
 * 如何使用焦点子系统</a>，
 * <em>Java 教程</em> 中的一个部分，以及
 * <a href="../../java/awt/doc-files/FocusSpec.html">焦点规范</a>
 * 以获取更多信息。
 *
 * @author David Mendenhall
 *
 * @see Container#getComponents
 * @see Component#isFocusable
 * @see Component#setFocusable
 * @since 1.4
 */
public class DefaultFocusTraversalPolicy
    extends ContainerOrderFocusTraversalPolicy
{
    /*
     * 序列化版本唯一标识符
     */
    private static final long serialVersionUID = 8876966522510157497L;

    /**
     * 确定组件是否可以作为新的焦点所有者。组件必须是可见的、可显示的和启用的才能被接受。如果客户端代码通过重写 <code>Component.isFocusTraversable()</code> 或 <code>Component.isFocusable()</code>，或调用 <code>Component.setFocusable()</code> 显式设置了组件的可聚焦性，那么组件只有在其可聚焦时才会被接受。然而，如果组件依赖于默认的可聚焦性，那么所有画布、标签、面板、滚动条、滚动窗格、窗口和轻量级组件将被拒绝。
     *
     * @param aComponent 要测试其作为焦点所有者的合适性的组件
     * @return 如果 aComponent 满足上述要求，则返回 <code>true</code>；否则返回 <code>false</code>
     */
    protected boolean accept(Component aComponent) {
        if (!(aComponent.isVisible() && aComponent.isDisplayable() &&
              aComponent.isEnabled()))
        {
            return false;
        }

        // 验证组件是否递归启用。禁用重量级容器会禁用其子组件，而禁用轻量级容器则不会。
        if (!(aComponent instanceof Window)) {
            for (Container enableTest = aComponent.getParent();
                 enableTest != null;
                 enableTest = enableTest.getParent())
            {
                if (!(enableTest.isEnabled() || enableTest.isLightweight())) {
                    return false;
                }
                if (enableTest instanceof Window) {
                    break;
                }
            }
        }

        boolean focusable = aComponent.isFocusable();
        if (aComponent.isFocusTraversableOverridden()) {
            return focusable;
        }

        ComponentPeer peer = aComponent.getPeer();
        return (peer != null && peer.isFocusable());
    }
}
