
/*
 * Copyright (c) 1995, 2015, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;
import java.util.Enumeration;
import sun.awt.AWTAccessor;
import java.awt.peer.MenuBarPeer;
import java.awt.event.KeyEvent;
import javax.accessibility.*;

/**
 * <code>MenuBar</code> 类封装了平台上的菜单栏概念，该菜单栏绑定到一个框架。为了将菜单栏与 <code>Frame</code> 对象关联，调用框架的 <code>setMenuBar</code> 方法。
 * <p>
 * <A NAME="mbexample"></A><!-- target for cross references -->
 * 这是一个菜单栏的示例：
 * <p>
 * <img src="doc-files/MenuBar-1.gif"
 * alt="Diagram of MenuBar containing 2 menus: Examples and Options.
 * Examples menu is expanded showing items: Basic, Simple, Check, and More Examples."
 * style="float:center; margin: 7px 10px;">
 * <p>
 * 菜单栏处理菜单项的键盘快捷键，并将它们传递给子菜单。
 * （键盘快捷键是可选的，为用户提供了一种替代鼠标的方法来调用菜单项及其关联的操作。）
 * 每个菜单项可以维护一个 <code>MenuShortcut</code> 实例。
 * <code>MenuBar</code> 类定义了几个方法，如 {@link MenuBar#shortcuts} 和
 * {@link MenuBar#getShortcutMenuItem}，用于检索给定菜单栏管理的快捷键信息。
 *
 * @author Sami Shaio
 * @see        java.awt.Frame
 * @see        java.awt.Frame#setMenuBar(java.awt.MenuBar)
 * @see        java.awt.Menu
 * @see        java.awt.MenuItem
 * @see        java.awt.MenuShortcut
 * @since      JDK1.0
 */
public class MenuBar extends MenuComponent implements MenuContainer, Accessible {

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
        AWTAccessor.setMenuBarAccessor(
            new AWTAccessor.MenuBarAccessor() {
                public Menu getHelpMenu(MenuBar menuBar) {
                    return menuBar.helpMenu;
                }

                public Vector<Menu> getMenus(MenuBar menuBar) {
                    return menuBar.menus;
                }
            });
    }

    /**
     * 此字段表示将作为菜单栏一部分的实际菜单的向量。
     *
     * @serial
     * @see #countMenus()
     */
    Vector<Menu> menus = new Vector<>();

    /**
     * 此菜单是一个专门用于帮助的菜单。需要注意的是，在某些平台上，它会出现在菜单栏的右侧。
     *
     * @serial
     * @see #getHelpMenu()
     * @see #setHelpMenu(Menu)
     */
    Menu helpMenu;

    private static final String base = "menubar";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 序列化版本ID
     */
     private static final long serialVersionUID = -4930327919388951260L;

    /**
     * 创建一个新的菜单栏。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public MenuBar() throws HeadlessException {
    }

    /**
     * 为这个 MenuComponent 构建一个名称。当名称为 null 时，由 getName() 调用。
     */
    String constructComponentName() {
        synchronized (MenuBar.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 创建菜单栏的对等体。对等体允许我们在不改变菜单栏功能的情况下改变其外观。
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = Toolkit.getDefaultToolkit().createMenuBar(this);

            int nmenus = getMenuCount();
            for (int i = 0 ; i < nmenus ; i++) {
                getMenu(i).addNotify();
            }
        }
    }

    /**
     * 移除菜单栏的对等体。对等体允许我们在不改变菜单栏功能的情况下改变其外观。
     */
    public void removeNotify() {
        synchronized (getTreeLock()) {
            int nmenus = getMenuCount();
            for (int i = 0 ; i < nmenus ; i++) {
                getMenu(i).removeNotify();
            }
            super.removeNotify();
        }
    }

    /**
     * 获取菜单栏上的帮助菜单。
     * @return    此菜单栏上的帮助菜单。
     */
    public Menu getHelpMenu() {
        return helpMenu;
    }

    /**
     * 将指定的菜单设置为菜单栏的帮助菜单。
     * 如果此菜单栏已有帮助菜单，旧的帮助菜单将从菜单栏中移除，并替换为指定的菜单。
     * @param m    要设置为帮助菜单的菜单
     */
    public void setHelpMenu(final Menu m) {
        synchronized (getTreeLock()) {
            if (helpMenu == m) {
                return;
            }
            if (helpMenu != null) {
                remove(helpMenu);
            }
            helpMenu = m;
            if (m != null) {
                if (m.parent != this) {
                    add(m);
                }
                m.isHelpMenu = true;
                m.parent = this;
                MenuBarPeer peer = (MenuBarPeer)this.peer;
                if (peer != null) {
                    if (m.peer == null) {
                        m.addNotify();
                    }
                    peer.addHelpMenu(m);
                }
            }
        }
    }

    /**
     * 将指定的菜单添加到菜单栏。
     * 如果该菜单已属于另一个菜单栏，则从该菜单栏中移除。
     *
     * @param        m   要添加的菜单
     * @return       添加的菜单
     * @see          java.awt.MenuBar#remove(int)
     * @see          java.awt.MenuBar#remove(java.awt.MenuComponent)
     */
    public Menu add(Menu m) {
        synchronized (getTreeLock()) {
            if (m.parent != null) {
                m.parent.remove(m);
            }
            m.parent = this;

            MenuBarPeer peer = (MenuBarPeer)this.peer;
            if (peer != null) {
                if (m.peer == null) {
                    m.addNotify();
                }
                menus.addElement(m);
                peer.addMenu(m);
            } else {
                menus.addElement(m);
            }
            return m;
        }
    }

    /**
     * 从菜单栏中移除位于指定索引的菜单。
     * @param        index   要移除的菜单的位置。
     * @see          java.awt.MenuBar#add(java.awt.Menu)
     */
    public void remove(final int index) {
        synchronized (getTreeLock()) {
            Menu m = getMenu(index);
            menus.removeElementAt(index);
            MenuBarPeer peer = (MenuBarPeer)this.peer;
            if (peer != null) {
                peer.delMenu(index);
                m.removeNotify();
                m.parent = null;
            }
            if (helpMenu == m) {
                helpMenu = null;
                m.isHelpMenu = false;
            }
        }
    }

    /**
     * 从菜单栏中移除指定的菜单组件。
     * @param        m 要移除的菜单组件。
     * @see          java.awt.MenuBar#add(java.awt.Menu)
     */
    public void remove(MenuComponent m) {
        synchronized (getTreeLock()) {
            int index = menus.indexOf(m);
            if (index >= 0) {
                remove(index);
            }
        }
    }

    /**
     * 获取菜单栏上的菜单数量。
     * @return     菜单栏上的菜单数量。
     * @since      JDK1.1
     */
    public int getMenuCount() {
        return countMenus();
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 被 <code>getMenuCount()</code> 替代。
     */
    @Deprecated
    public int countMenus() {
        return getMenuCountImpl();
    }

    /*
     * 由本机代码调用，因此客户端代码不能在工具包线程上调用。
     */
    final int getMenuCountImpl() {
        return menus.size();
    }

    /**
     * 获取指定索引的菜单。
     * @param      i 要返回的菜单的索引位置。
     * @return     此菜单栏中指定索引的菜单。
     */
    public Menu getMenu(int i) {
        return getMenuImpl(i);
    }

    /*
     * 由本机代码调用，因此客户端代码不能在工具包线程上调用。
     */
    final Menu getMenuImpl(int i) {
        return menus.elementAt(i);
    }

    /**
     * 获取此菜单栏管理的所有菜单快捷键的枚举。
     * @return      此菜单栏管理的菜单快捷键的枚举。
     * @see         java.awt.MenuShortcut
     * @since       JDK1.1
     */
    public synchronized Enumeration<MenuShortcut> shortcuts() {
        Vector<MenuShortcut> shortcuts = new Vector<>();
        int nmenus = getMenuCount();
        for (int i = 0 ; i < nmenus ; i++) {
            Enumeration<MenuShortcut> e = getMenu(i).shortcuts();
            while (e.hasMoreElements()) {
                shortcuts.addElement(e.nextElement());
            }
        }
        return shortcuts.elements();
    }

    /**
     * 获取与指定 <code>MenuShortcut</code> 对象关联的 <code>MenuItem</code> 实例，
     * 如果此菜单栏管理的菜单项中没有与指定菜单快捷键关联的项，则返回 <code>null</code>。
     * @param        s 指定的菜单快捷键。
     * @see          java.awt.MenuItem
     * @see          java.awt.MenuShortcut
     * @since        JDK1.1
     */
     public MenuItem getShortcutMenuItem(MenuShortcut s) {
        int nmenus = getMenuCount();
        for (int i = 0 ; i < nmenus ; i++) {
            MenuItem mi = getMenu(i).getShortcutMenuItem(s);
            if (mi != null) {
                return mi;
            }
        }
        return null;  // MenuShortcut 未找到
     }

    /*
     * 将与指定键盘事件（在按键按下时）关联的 ACTION_EVENT 发送到 MenuPeer 的目标。
     * 如果存在关联的键盘事件，则返回 true。
     */
    boolean handleShortcut(KeyEvent e) {
        // 是否是键盘事件？
        int id = e.getID();
        if (id != KeyEvent.KEY_PRESSED && id != KeyEvent.KEY_RELEASED) {
            return false;
        }

        // 加速键是否被按下？
        int accelKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        if ((e.getModifiers() & accelKey) == 0) {
            return false;
        }

        // 将 MenuShortcut 传递给子菜单。
        int nmenus = getMenuCount();
        for (int i = 0 ; i < nmenus ; i++) {
            Menu m = getMenu(i);
            if (m.handleShortcut(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除指定的菜单快捷键。
     * @param     s 要删除的菜单快捷键。
     * @since     JDK1.1
     */
    public void deleteShortcut(MenuShortcut s) {
        int nmenus = getMenuCount();
        for (int i = 0 ; i < nmenus ; i++) {
            getMenu(i).deleteShortcut(s);
        }
    }

    /* 序列化支持。在此恢复菜单栏菜单的（瞬态）父字段。 */

    /**
     * 菜单栏的序列化数据版本。
     *
     * @serial
     */
    private int menuBarSerializedDataVersion = 1;

    /**
     * 将默认的可序列化字段写入流。
     *
     * @param s 要写入的 <code>ObjectOutputStream</code>
     * @see AWTEventMulticaster#save(ObjectOutputStream, String, EventListener)
     * @see #readObject(java.io.ObjectInputStream)
     */
    private void writeObject(java.io.ObjectOutputStream s)
      throws java.lang.ClassNotFoundException,
             java.io.IOException
    {
      s.defaultWriteObject();
    }

    /**
     * 读取 <code>ObjectInputStream</code>。
     * 未识别的键或值将被忽略。
     *
     * @param s 要读取的 <code>ObjectInputStream</code>
     * @exception HeadlessException 如果
     *   <code>GraphicsEnvironment.isHeadless</code> 返回
     *   <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see #writeObject(java.io.ObjectOutputStream)
     */
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException, HeadlessException
    {
      // HeadlessException 将从 MenuComponent 的 readObject 抛出
      s.defaultReadObject();
      for (int i = 0; i < menus.size(); i++) {
        Menu m = menus.elementAt(i);
        m.parent = this;
      }
    }

    /**
     * 初始化 JNI 字段和方法 ID
     */
    private static native void initIDs();


/////////////////
// Accessibility support
////////////////

    /**
     * 获取与此 MenuBar 关联的 AccessibleContext。
     * 对于菜单栏，AccessibleContext 的形式为 AccessibleAWTMenuBar。
     * 如果必要，将创建一个新的 AccessibleAWTMenuBar 实例。
     *
     * @return 一个 AccessibleAWTMenuBar，作为此 MenuBar 的 AccessibleContext
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTMenuBar();
        }
        return accessibleContext;
    }

    /**
     * 在 MenuComponent 中定义。在此处重写。
     */
    int getAccessibleChildIndex(MenuComponent child) {
        return menus.indexOf(child);
    }

    /**
     * MenuBar 的内部类，用于提供对可访问性的默认支持。
     * 此类不是供应用程序开发人员直接使用的，而是仅供菜单组件开发人员子类化。
     * <p>
     * 此类实现了适用于菜单栏用户界面元素的 Java 可访问性 API。
     * @since 1.3
     */
    protected class AccessibleAWTMenuBar extends AccessibleAWTMenuComponent
    {
        /*
         * JDK 1.3 序列化版本ID
         */
        private static final long serialVersionUID = -8577604491830083815L;


                    /**
         * 获取此对象的角色。
         *
         * @return 一个描述对象角色的 AccessibleRole 实例
         * @since 1.4
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.MENU_BAR;
        }

    } // class AccessibleAWTMenuBar

}
