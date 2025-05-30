
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.peer.MenuPeer;
import java.awt.event.KeyEvent;
import javax.accessibility.*;
import sun.awt.AWTAccessor;

/**
 * <code>Menu</code> 对象是一个下拉菜单组件，部署在菜单栏中。
 * <p>
 * 菜单可以是可分离的。可分离的菜单可以被打开并从其父菜单栏或菜单中拖出。
 * 释放鼠标按钮后，它仍然保留在屏幕上。可分离菜单的机制取决于平台，因为其外观由其对等体决定。
 * 在不支持可分离菜单的平台上，可分离属性将被忽略。
 * <p>
 * 菜单中的每个项目必须属于 <code>MenuItem</code> 类。它可以是 <code>MenuItem</code> 的实例，
 * 子菜单（<code>Menu</code> 的实例），或复选框（<code>CheckboxMenuItem</code> 的实例）。
 *
 * @author Sami Shaio
 * @see     java.awt.MenuItem
 * @see     java.awt.CheckboxMenuItem
 * @since   JDK1.0
 */
public class Menu extends MenuItem implements MenuContainer, Accessible {

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }

        AWTAccessor.setMenuAccessor(
            new AWTAccessor.MenuAccessor() {
                public Vector<MenuComponent> getItems(Menu menu) {
                    return menu.items;
                }
            });
    }

    /**
     * 将成为菜单一部分的项目的向量。
     *
     * @serial
     * @see #countItems()
     */
    Vector<MenuComponent> items = new Vector<>();

    /**
     * 此字段指示菜单是否具有可分离属性。如果菜单具有可分离属性，则设置为
     * <code>true</code>，否则设置为 <code>false</code>。
     * 用户可以在不再需要时删除可分离的菜单。
     *
     * @serial
     * @see #isTearOff()
     */
    boolean             tearOff;

    /**
     * 如果该菜单实际上是帮助菜单，则此字段设置为 <code>true</code>。
     * 否则设置为 <code>false</code>。
     *
     * @serial
     */
    boolean             isHelpMenu;

    private static final String base = "menu";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 序列化版本号
     */
     private static final long serialVersionUID = -8809584163345499784L;

    /**
     * 构造一个带有空标签的新菜单。此菜单不是可分离菜单。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since      JDK1.1
     */
    public Menu() throws HeadlessException {
        this("", false);
    }

    /**
     * 构造一个带有指定标签的新菜单。此菜单不是可分离菜单。
     * @param       label 菜单在菜单栏中的标签，或在另一个菜单中作为子菜单的标签。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public Menu(String label) throws HeadlessException {
        this(label, false);
    }

    /**
     * 构造一个带有指定标签的新菜单，指示菜单是否可分离。
     * <p>
     * 并非所有 AWT 实现都支持可分离功能。如果特定实现不支持可分离菜单，此值将被忽略。
     * @param       label 菜单在菜单栏中的标签，或在另一个菜单中作为子菜单的标签。
     * @param       tearOff   如果为 <code>true</code>，则菜单是可分离菜单。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since       JDK1.0.
     */
    public Menu(String label, boolean tearOff) throws HeadlessException {
        super(label);
        this.tearOff = tearOff;
    }

    /**
     * 为这个 MenuComponent 构造一个名称。当名称为 null 时，由 getName() 调用。
     */
    String constructComponentName() {
        synchronized (Menu.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 创建菜单的对等体。对等体允许我们在不改变菜单功能的情况下修改其外观。
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = Toolkit.getDefaultToolkit().createMenu(this);
            int nitems = getItemCount();
            for (int i = 0 ; i < nitems ; i++) {
                MenuItem mi = getItem(i);
                mi.parent = this;
                mi.addNotify();
            }
        }
    }

    /**
     * 移除菜单的对等体。对等体允许我们在不改变菜单功能的情况下修改其外观。
     */
    public void removeNotify() {
        synchronized (getTreeLock()) {
            int nitems = getItemCount();
            for (int i = 0 ; i < nitems ; i++) {
                getItem(i).removeNotify();
            }
            super.removeNotify();
        }
    }

    /**
     * 指示此菜单是否是可分离菜单。
     * <p>
     * 并非所有 AWT 实现都支持可分离功能。如果特定实现不支持可分离菜单，此值将被忽略。
     * @return      如果是可分离菜单，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean isTearOff() {
        return tearOff;
    }

    /**
      * 获取此菜单中的项目数。
      * @return     此菜单中的项目数。
      * @since      JDK1.1
      */
    public int getItemCount() {
        return countItems();
    }

    /**
     * @deprecated 自 JDK 1.1 起，
     * 替换为 <code>getItemCount()</code>。
     */
    @Deprecated
    public int countItems() {
        return countItemsImpl();
    }

    /*
     * 由本机代码调用，因此客户端代码不能在工具包线程上调用。
     */
    final int countItemsImpl() {
        return items.size();
    }

    /**
     * 获取此菜单中指定索引处的项目。
     * @param     index 要返回的项目的索引。
     * @return    指定索引处的项目。
     */
    public MenuItem getItem(int index) {
        return getItemImpl(index);
    }

    /*
     * 由本机代码调用，因此客户端代码不能在工具包线程上调用。
     */
    final MenuItem getItemImpl(int index) {
        return (MenuItem)items.elementAt(index);
    }

    /**
     * 将指定的菜单项添加到此菜单中。如果菜单项已属于另一个菜单，则从该菜单中移除。
     *
     * @param       mi   要添加的菜单项
     * @return      添加的菜单项
     * @see         java.awt.Menu#insert(java.lang.String, int)
     * @see         java.awt.Menu#insert(java.awt.MenuItem, int)
     */
    public MenuItem add(MenuItem mi) {
        synchronized (getTreeLock()) {
            if (mi.parent != null) {
                mi.parent.remove(mi);
            }
            items.addElement(mi);
            mi.parent = this;
            MenuPeer peer = (MenuPeer)this.peer;
            if (peer != null) {
                mi.addNotify();
                peer.addItem(mi);
            }
            return mi;
        }
    }

    /**
     * 将带有指定标签的项目添加到此菜单中。
     *
     * @param       label   项目上的文本
     * @see         java.awt.Menu#insert(java.lang.String, int)
     * @see         java.awt.Menu#insert(java.awt.MenuItem, int)
     */
    public void add(String label) {
        add(new MenuItem(label));
    }

    /**
     * 在指定位置插入一个菜单项。
     *
     * @param         menuitem  要插入的菜单项。
     * @param         index     菜单项应插入的位置。
     * @see           java.awt.Menu#add(java.lang.String)
     * @see           java.awt.Menu#add(java.awt.MenuItem)
     * @exception     IllegalArgumentException 如果 <code>index</code> 的值小于零
     * @since         JDK1.1
     */

    public void insert(MenuItem menuitem, int index) {
        synchronized (getTreeLock()) {
            if (index < 0) {
                throw new IllegalArgumentException("index less than zero.");
            }

            int nitems = getItemCount();
            Vector<MenuItem> tempItems = new Vector<>();

            /* 从索引处移除项目，nitems-index 次
               将它们存储在一个临时向量中，按其在菜单中出现的顺序存储。
            */
            for (int i = index ; i < nitems; i++) {
                tempItems.addElement(getItem(index));
                remove(index);
            }

            add(menuitem);

            /* 将移除的项目重新添加到菜单中，它们已经在临时向量中按正确顺序排列。
            */
            for (int i = 0; i < tempItems.size()  ; i++) {
                add(tempItems.elementAt(i));
            }
        }
    }

    /**
     * 在指定位置插入带有指定标签的菜单项。这是 <code>insert(menuItem, index)</code> 的便捷方法。
     *
     * @param       label 项目上的文本
     * @param       index 菜单项应插入的位置
     * @see         java.awt.Menu#add(java.lang.String)
     * @see         java.awt.Menu#add(java.awt.MenuItem)
     * @exception     IllegalArgumentException 如果 <code>index</code> 的值小于零
     * @since       JDK1.1
     */

    public void insert(String label, int index) {
        insert(new MenuItem(label), index);
    }

    /**
     * 在当前位置向菜单中添加分隔线或破折号。
     * @see         java.awt.Menu#insertSeparator(int)
     */
    public void addSeparator() {
        add("-");
    }

    /**
     * 在指定位置插入分隔线。
     * @param       index 菜单分隔线应插入的位置。
     * @exception   IllegalArgumentException 如果 <code>index</code> 的值小于 0。
     * @see         java.awt.Menu#addSeparator
     * @since       JDK1.1
     */

    public void insertSeparator(int index) {
        synchronized (getTreeLock()) {
            if (index < 0) {
                throw new IllegalArgumentException("index less than zero.");
            }

            int nitems = getItemCount();
            Vector<MenuItem> tempItems = new Vector<>();

            /* 从索引处移除项目，nitems-index 次
               将它们存储在一个临时向量中，按其在菜单中出现的顺序存储。
            */
            for (int i = index ; i < nitems; i++) {
                tempItems.addElement(getItem(index));
                remove(index);
            }

            addSeparator();

            /* 将移除的项目重新添加到菜单中，它们已经在临时向量中按正确顺序排列。
            */
            for (int i = 0; i < tempItems.size()  ; i++) {
                add(tempItems.elementAt(i));
            }
        }
    }

    /**
     * 从此菜单中移除指定索引处的菜单项。
     * @param       index 要移除的项目的索引。
     */
    public void remove(int index) {
        synchronized (getTreeLock()) {
            MenuItem mi = getItem(index);
            items.removeElementAt(index);
            MenuPeer peer = (MenuPeer)this.peer;
            if (peer != null) {
                peer.delItem(index);
                mi.removeNotify();
                mi.parent = null;
            }
        }
    }

    /**
     * 从此菜单中移除指定的菜单项。
     * @param  item 要从菜单中移除的项目。
     *         如果 <code>item</code> 为 <code>null</code>
     *         或不在此菜单中，此方法不执行任何操作。
     */
    public void remove(MenuComponent item) {
        synchronized (getTreeLock()) {
            int index = items.indexOf(item);
            if (index >= 0) {
                remove(index);
            }
        }
    }

    /**
     * 从此菜单中移除所有项目。
     * @since       JDK1.0.
     */
    public void removeAll() {
        synchronized (getTreeLock()) {
            int nitems = getItemCount();
            for (int i = nitems-1 ; i >= 0 ; i--) {
                remove(i);
            }
        }
    }

    /*
     * 将 ActionEvent 发送到与指定键盘事件（在按键按下时）关联的 MenuPeer 的目标。
     * 如果有与之关联的键盘事件，则返回 true。
     */
    boolean handleShortcut(KeyEvent e) {
        int nitems = getItemCount();
        for (int i = 0 ; i < nitems ; i++) {
            MenuItem mi = getItem(i);
            if (mi.handleShortcut(e)) {
                return true;
            }
        }
        return false;
    }

    MenuItem getShortcutMenuItem(MenuShortcut s) {
        int nitems = getItemCount();
        for (int i = 0 ; i < nitems ; i++) {
            MenuItem mi = getItem(i).getShortcutMenuItem(s);
            if (mi != null) {
                return mi;
            }
        }
        return null;
    }


                synchronized Enumeration<MenuShortcut> shortcuts() {
        Vector<MenuShortcut> shortcuts = new Vector<>();
        int nitems = getItemCount();
        for (int i = 0 ; i < nitems ; i++) {
            MenuItem mi = getItem(i);
            if (mi instanceof Menu) {
                Enumeration<MenuShortcut> e = ((Menu)mi).shortcuts();
                while (e.hasMoreElements()) {
                    shortcuts.addElement(e.nextElement());
                }
            } else {
                MenuShortcut ms = mi.getShortcut();
                if (ms != null) {
                    shortcuts.addElement(ms);
                }
            }
        }
        return shortcuts.elements();
    }

    void deleteShortcut(MenuShortcut s) {
        int nitems = getItemCount();
        for (int i = 0 ; i < nitems ; i++) {
            getItem(i).deleteShortcut(s);
        }
    }


    /* Serialization support.  A MenuContainer is responsible for
     * restoring the parent fields of its children.
     */

    /**
     * 菜单序列化数据版本。
     *
     * @serial
     */
    private int menuSerializedDataVersion = 1;

    /**
     * 将默认的可序列化字段写入流。
     *
     * @param s 要写入的 <code>ObjectOutputStream</code>
     * @see AWTEventMulticaster#save(ObjectOutputStream, String, EventListener)
     * @see #readObject(ObjectInputStream)
     */
    private void writeObject(java.io.ObjectOutputStream s)
      throws java.io.IOException
    {
      s.defaultWriteObject();
    }

    /**
     * 读取 <code>ObjectInputStream</code>。
     * 无法识别的键或值将被忽略。
     *
     * @param s 要读取的 <code>ObjectInputStream</code>
     * @exception HeadlessException 如果
     *   <code>GraphicsEnvironment.isHeadless</code> 返回
     *   <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see #writeObject(ObjectOutputStream)
     */
    private void readObject(ObjectInputStream s)
      throws IOException, ClassNotFoundException, HeadlessException
    {
      // HeadlessException 将从 MenuComponent 的 readObject 抛出
      s.defaultReadObject();
      for(int i = 0; i < items.size(); i++) {
        MenuItem item = (MenuItem)items.elementAt(i);
        item.parent = this;
      }
    }

    /**
     * 返回表示此 <code>Menu</code> 状态的字符串。
     * 此方法仅用于调试目的，返回字符串的内容和格式可能因实现而异。返回的字符串可以为空，但不能为
     * <code>null</code>。
     *
     * @return 此菜单的参数字符串
     */
    public String paramString() {
        String str = ",tearOff=" + tearOff+",isHelpMenu=" + isHelpMenu;
        return super.paramString() + str;
    }

    /**
     * 初始化 JNI 字段和方法 ID
     */
    private static native void initIDs();


/////////////////
// Accessibility support
////////////////

    /**
     * 获取与此 Menu 关联的 AccessibleContext。
     * 对于菜单，AccessibleContext 的形式为 AccessibleAWTMenu。
     * 如果需要，将创建一个新的 AccessibleAWTMenu 实例。
     *
     * @return 一个 AccessibleAWTMenu，作为此 Menu 的 AccessibleContext
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTMenu();
        }
        return accessibleContext;
    }

    /**
     * 在 MenuComponent 中定义。在此处重写。
     */
    int getAccessibleChildIndex(MenuComponent child) {
        return items.indexOf(child);
    }

    /**
     * Menu 的内部类，用于提供对可访问性的默认支持。
     * 此类不打算由应用程序开发人员直接使用，而是仅供菜单组件开发人员子类化。
     * <p>
     * 此类实现了适用于菜单用户界面元素的 Java 可访问性 API。
     * @since 1.3
     */
    protected class AccessibleAWTMenu extends AccessibleAWTMenuItem
    {
        /*
         * JDK 1.3 serialVersionUID
         */
        private static final long serialVersionUID = 5228160894980069094L;

        /**
         * 获取此对象的角色。
         *
         * @return 一个 AccessibleRole 实例，描述对象的角色
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.MENU;
        }

    } // class AccessibleAWTMenu

}
