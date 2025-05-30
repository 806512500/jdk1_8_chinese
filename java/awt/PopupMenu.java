/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.peer.PopupMenuPeer;
import javax.accessibility.*;


import sun.awt.AWTAccessor;

/**
 * 一个实现可以在组件内指定位置动态弹出的菜单的类。
 * <p>
 * 由于继承层次结构的暗示，<code>PopupMenu</code>
 * 可以在任何可以使用<code>Menu</code>的地方使用。
 * 但是，如果你像使用<code>Menu</code>一样使用<code>PopupMenu</code>
 * （例如，你将其添加到<code>MenuBar</code>中），那么你<b>不能</b>
 * 调用该<code>PopupMenu</code>的<code>show</code>方法。
 *
 * @author      Amy Fowler
 */
public class PopupMenu extends Menu {

    private static final String base = "popup";
    static int nameCounter = 0;

    transient boolean isTrayIconPopup = false;

    static {
        AWTAccessor.setPopupMenuAccessor(
            new AWTAccessor.PopupMenuAccessor() {
                public boolean isTrayIconPopup(PopupMenu popupMenu) {
                    return popupMenu.isTrayIconPopup;
                }
            });
    }

    /*
     * JDK 1.1 序列化版本ID
     */
    private static final long serialVersionUID = -4620452533522760060L;

    /**
     * 创建一个具有空名称的新弹出菜单。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public PopupMenu() throws HeadlessException {
        this("");
    }

    /**
     * 创建一个具有指定名称的新弹出菜单。
     *
     * @param label 一个非<code>null</code>的字符串，指定
     *                弹出菜单的标签
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public PopupMenu(String label) throws HeadlessException {
        super(label);
    }

    /**
     * {@inheritDoc}
     */
    public MenuContainer getParent() {
        if (isTrayIconPopup) {
            return null;
        }
        return super.getParent();
    }

    /**
     * 为这个<code>MenuComponent</code>构建一个名称。
     * 当名称为<code>null</code>时，由<code>getName</code>调用。
     */
    String constructComponentName() {
        synchronized (PopupMenu.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 创建弹出菜单的对等体。
     * 对等体允许我们在不改变弹出菜单任何功能的情况下改变其外观。
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            // 如果我们的父级不是一个组件，那么这个 PopupMenu 实际上只是一个普通的 Menu。
            if (parent != null && !(parent instanceof Component)) {
                super.addNotify();
            }
            else {
                if (peer == null)
                    peer = Toolkit.getDefaultToolkit().createPopupMenu(this);
                int nitems = getItemCount();
                for (int i = 0 ; i < nitems ; i++) {
                    MenuItem mi = getItem(i);
                    mi.parent = this;
                    mi.addNotify();
                }
            }
        }
    }

   /**
     * 在相对于原点组件的 x, y 位置显示弹出菜单。
     * 原点组件必须包含在弹出菜单父级的组件层次结构中。原点和父级都必须在屏幕上显示，此方法才有效。
     * <p>
     * 如果这个<code>PopupMenu</code>被用作<code>Menu</code>
     * （即，它有一个非<code>Component</code>的父级），
     * 那么你不能在这个<code>PopupMenu</code>上调用此方法。
     *
     * @param origin 定义坐标空间的组件
     * @param x 显示菜单的 x 坐标位置
     * @param y 显示菜单的 y 坐标位置
     * @exception NullPointerException  如果父级为<code>null</code>
     * @exception IllegalArgumentException  如果这个<code>PopupMenu</code>
     *                有一个非<code>Component</code>的父级
     * @exception IllegalArgumentException 如果原点不在父级的层次结构中
     * @exception RuntimeException 如果父级未在屏幕上显示
     */
    public void show(Component origin, int x, int y) {
        // 为了线程安全，使用 localParent。
        MenuContainer localParent = parent;
        if (localParent == null) {
            throw new NullPointerException("parent is null");
        }
        if (!(localParent instanceof Component)) {
            throw new IllegalArgumentException(
                "PopupMenus with non-Component parents cannot be shown");
        }
        Component compParent = (Component)localParent;
        //Fixed 6278745: Incorrect exception throwing in PopupMenu.show() method
        //如果 compParent 不等于 origin 且不是 Container，则不会抛出异常
        if (compParent != origin) {
            if (compParent instanceof Container) {
                if (!((Container)compParent).isAncestorOf(origin)) {
                    throw new IllegalArgumentException("origin not in parent's hierarchy");
                }
            } else {
                throw new IllegalArgumentException("origin not in parent's hierarchy");
            }
        }
        if (compParent.getPeer() == null || !compParent.isShowing()) {
            throw new RuntimeException("parent not showing on screen");
        }
        if (peer == null) {
            addNotify();
        }
        synchronized (getTreeLock()) {
            if (peer != null) {
                ((PopupMenuPeer)peer).show(
                    new Event(origin, 0, Event.MOUSE_DOWN, x, y, 0, 0));
            }
        }
    }


/////////////////
// Accessibility support
////////////////

    /**
     * 获取与此<code>PopupMenu</code>关联的<code>AccessibleContext</code>。
     *
     * @return 此<code>PopupMenu</code>的<code>AccessibleContext</code>
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTPopupMenu();
        }
        return accessibleContext;
    }

    /**
     * PopupMenu 的内部类，用于提供对可访问性的默认支持。
     * 这个类不打算由应用程序开发人员直接使用，而是仅用于菜单组件开发人员的子类化。
     * <p>
     * 用于获取此对象的可访问角色的类。
     * @since 1.3
     */
    protected class AccessibleAWTPopupMenu extends AccessibleAWTMenu
    {
        /*
         * JDK 1.3 序列化版本ID
         */
        private static final long serialVersionUID = -4282044795947239955L;

        /**
         * 获取此对象的角色。
         *
         * @return 描述对象角色的 AccessibleRole 实例
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.POPUP_MENU;
        }

    } // class AccessibleAWTPopupMenu

}
