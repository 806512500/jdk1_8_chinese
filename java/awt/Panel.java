/*
 * Copyright (c) 1995, 2007, Oracle and/or its affiliates. All rights reserved.
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

import javax.accessibility.*;

/**
 * <code>Panel</code> 是最简单的容器类。面板
 * 提供空间，应用程序可以在其中附加任何其他
 * 组件，包括其他面板。
 * <p>
 * 面板的默认布局管理器是
 * <code>FlowLayout</code> 布局管理器。
 *
 * @author      Sami Shaio
 * @see     java.awt.FlowLayout
 * @since   JDK1.0
 */
public class Panel extends Container implements Accessible {
    private static final String base = "panel";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = -2728009084054400034L;

    /**
     * 使用默认布局管理器创建一个新的面板。
     * 所有面板的默认布局管理器是
     * <code>FlowLayout</code> 类。
     */
    public Panel() {
        this(new FlowLayout());
    }

    /**
     * 使用指定的布局管理器创建一个新的面板。
     * @param layout 这个面板的布局管理器。
     * @since JDK1.1
     */
    public Panel(LayoutManager layout) {
        setLayout(layout);
    }

    /**
     * 为这个组件构建一个名称。当名称为 null 时由 getName() 调用。
     */
    String constructComponentName() {
        synchronized (Panel.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 创建面板的对等体。对等体允许您修改
     * 面板的外观而不改变其功能。
     */

    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createPanel(this);
            super.addNotify();
        }
    }

/////////////////
// Accessibility support
////////////////

    /**
     * 获取与此面板关联的 AccessibleContext。
     * 对于面板，AccessibleContext 的形式为
     * AccessibleAWTPanel。如果必要，将创建一个新的 AccessibleAWTPanel 实例。
     *
     * @return 一个 AccessibleAWTPanel，作为
     *         此面板的 AccessibleContext
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTPanel();
        }
        return accessibleContext;
    }

    /**
     * 该类为 <code>Panel</code> 类实现辅助功能支持。
     * 它为面板用户界面元素提供了适当的
     * Java 辅助功能 API 实现。
     * @since 1.3
     */
    protected class AccessibleAWTPanel extends AccessibleAWTContainer {

        private static final long serialVersionUID = -6409552226660031050L;

        /**
         * 获取此对象的角色。
         *
         * @return 一个 AccessibleRole 实例，描述对象的角色
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PANEL;
        }
    }

}
