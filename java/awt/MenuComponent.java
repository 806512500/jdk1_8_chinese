
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

import java.awt.peer.MenuComponentPeer;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import sun.awt.AppContext;
import sun.awt.AWTAccessor;
import javax.accessibility.*;

import java.security.AccessControlContext;
import java.security.AccessController;

/**
 * 抽象类 <code>MenuComponent</code> 是所有与菜单相关的组件的超类。
 * 在这方面，类 <code>MenuComponent</code> 类似于 AWT 组件的抽象超类
 * <code>Component</code>。
 * <p>
 * 菜单组件接收并处理 AWT 事件，就像组件一样，通过 <code>processEvent</code> 方法。
 *
 * @author      Arthur van Hoff
 * @since       JDK1.0
 */
public abstract class MenuComponent implements java.io.Serializable {

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    transient MenuComponentPeer peer;
    transient MenuContainer parent;

    /**
     * 菜单组件的 <code>AppContext</code>。
     * 这个值在构造函数中设置，且不会改变。
     */
    transient AppContext appContext;

    /**
     * 菜单组件的字体。这个值可以为 <code>null</code>，此时将使用默认字体。
     * 默认值为 <code>null</code>。
     *
     * @serial
     * @see #setFont(Font)
     * @see #getFont()
     */
    volatile Font font;

    /**
     * 菜单组件的名称，默认为 <code>null</code>。
     * @serial
     * @see #getName()
     * @see #setName(String)
     */
    private String name;

    /**
     * 一个变量，用于指示名称是否显式设置。
     * 如果为 <code>true</code>，则名称将显式设置。
     * 默认值为 <code>false</code>。
     * @serial
     * @see #setName(String)
     */
    private boolean nameExplicitlySet = false;

    /**
     * 默认值为 <code>false</code>。
     * @serial
     * @see #dispatchEvent(AWTEvent)
     */
    boolean newEventsOnly = false;

    /*
     * 菜单的 AccessControlContext。
     */
    private transient volatile AccessControlContext acc =
            AccessController.getContext();

    /*
     * 返回此菜单组件构造时的 acc。
     */
    final AccessControlContext getAccessControlContext() {
        if (acc == null) {
            throw new SecurityException(
                    "MenuComponent is missing AccessControlContext");
        }
        return acc;
    }

    /*
     * 用于序列化的内部常量。
     */
    final static String actionListenerK = Component.actionListenerK;
    final static String itemListenerK = Component.itemListenerK;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -4536902356223894379L;

    static {
        AWTAccessor.setMenuComponentAccessor(
            new AWTAccessor.MenuComponentAccessor() {
                public AppContext getAppContext(MenuComponent menuComp) {
                    return menuComp.appContext;
                }
                public void setAppContext(MenuComponent menuComp,
                                          AppContext appContext) {
                    menuComp.appContext = appContext;
                }
                public MenuContainer getParent(MenuComponent menuComp) {
                    return menuComp.parent;
                }
                public Font getFont_NoClientCode(MenuComponent menuComp) {
                    return menuComp.getFont_NoClientCode();
                }
                @SuppressWarnings("unchecked")
                public <T extends MenuComponentPeer> T getPeer(MenuComponent menuComp) {
                    return (T) menuComp.peer;
                }
            });
    }

    /**
     * 创建一个 <code>MenuComponent</code>。
     * @exception HeadlessException 如果 <code>GraphicsEnvironment.isHeadless</code>
     *    返回 <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public MenuComponent() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        appContext = AppContext.getAppContext();
    }

    /**
     * 为这个 <code>MenuComponent</code> 构造一个名称。
     * 当名称为 <code>null</code> 时，由 <code>getName</code> 调用。
     * @return 一个名称，用于这个 <code>MenuComponent</code>
     */
    String constructComponentName() {
        return null; // 为了严格遵守先前平台版本的规定，未设置名称的 MenuComponent
                     // 在调用 getName() 时应返回 null
    }

    /**
     * 获取菜单组件的名称。
     * @return 菜单组件的名称
     * @see java.awt.MenuComponent#setName(java.lang.String)
     * @since JDK1.1
     */
    public String getName() {
        if (name == null && !nameExplicitlySet) {
            synchronized(this) {
                if (name == null && !nameExplicitlySet)
                    name = constructComponentName();
            }
        }
        return name;
    }

    /**
     * 将组件的名称设置为指定的字符串。
     * @param name 菜单组件的名称
     * @see java.awt.MenuComponent#getName
     * @since JDK1.1
     */
    public void setName(String name) {
        synchronized(this) {
            this.name = name;
            nameExplicitlySet = true;
        }
    }

    /**
     * 返回此菜单组件的父容器。
     * @return 包含此菜单组件的菜单组件，如果此菜单组件是顶级组件（菜单栏本身），则返回 <code>null</code>
     */
    public MenuContainer getParent() {
        return getParent_NoClientCode();
    }
    // 注意：此方法可能由特权线程调用。
    // 此功能实现在包私有方法中，以确保客户端子类无法覆盖。
    // 不要在该线程上调用客户端代码！
    final MenuContainer getParent_NoClientCode() {
        return parent;
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 程序不应直接操作对等组件。
     */
    @Deprecated
    public MenuComponentPeer getPeer() {
        return peer;
    }

    /**
     * 获取此菜单组件使用的字体。
     * @return 如果有字体，则返回此菜单组件使用的字体；否则返回 <code>null</code>
     * @see java.awt.MenuComponent#setFont
     */
    public Font getFont() {
        Font font = this.font;
        if (font != null) {
            return font;
        }
        MenuContainer parent = this.parent;
        if (parent != null) {
            return parent.getFont();
        }
        return null;
    }

    // 注意：此方法可能由特权线程调用。
    // 此功能实现在包私有方法中，以确保客户端子类无法覆盖。
    // 不要在该线程上调用客户端代码！
    final Font getFont_NoClientCode() {
        Font font = this.font;
        if (font != null) {
            return font;
        }

        // MenuContainer 接口没有 getFont_NoClientCode() 方法，因为它是包私有的。因此，我们必须手动转换实现
        // MenuContainer 的类。
        Object parent = this.parent;
        if (parent != null) {
            if (parent instanceof Component) {
                font = ((Component)parent).getFont_NoClientCode();
            } else if (parent instanceof MenuComponent) {
                font = ((MenuComponent)parent).getFont_NoClientCode();
            }
        }
        return font;
    } // getFont_NoClientCode()


    /**
     * 将此菜单组件的字体设置为指定的字体。此字体也将用于此菜单组件的所有子组件，除非这些子组件指定了不同的字体。
     * <p>
     * 某些平台可能不支持设置菜单组件的所有字体属性；在这种情况下，调用 <code>setFont</code>
     * 将不会影响此菜单组件的不受支持的字体属性。除非此菜单组件的子组件指定了不同的字体，否则此字体将由这些
     * 子组件使用，前提是底层平台支持。
     *
     * @param f 要设置的字体
     * @see #getFont
     * @see Font#getAttributes
     * @see java.awt.font.TextAttribute
     */
    public void setFont(Font f) {
        synchronized (getTreeLock()) {
            font = f;
            // Fixed 6312943: NullPointerException in method MenuComponent.setFont(Font)
            MenuComponentPeer peer = this.peer;
            if (peer != null) {
                peer.setFont(f);
            }
        }
    }

    /**
     * 移除菜单组件的对等组件。对等组件允许我们在不改变菜单组件功能的情况下修改其外观。
     */
    public void removeNotify() {
        synchronized (getTreeLock()) {
            MenuComponentPeer p = this.peer;
            if (p != null) {
                Toolkit.getEventQueue().removeSourceEvents(this, true);
                this.peer = null;
                p.dispose();
            }
        }
    }

    /**
     * 将指定的事件发布到菜单。
     * 此方法是 Java 1.0 事件系统的一部分，仅为了向后兼容而保留。
     * 不建议使用，将来可能不再支持。
     * @param evt 要发生的事件
     * @deprecated 自 JDK 1.1 版本起，被 {@link
     * #dispatchEvent(AWTEvent) dispatchEvent} 替代。
     */
    @Deprecated
    public boolean postEvent(Event evt) {
        MenuContainer parent = this.parent;
        if (parent != null) {
            parent.postEvent(evt);
        }
        return false;
    }

    /**
     * 将事件传递给此组件或其子组件。
     * @param e 事件
     */
    public final void dispatchEvent(AWTEvent e) {
        dispatchEventImpl(e);
    }

    void dispatchEventImpl(AWTEvent e) {
        EventQueue.setCurrentEventAndMostRecentTime(e);

        Toolkit.getDefaultToolkit().notifyAWTEventListeners(e);

        if (newEventsOnly ||
            (parent != null && parent instanceof MenuComponent &&
             ((MenuComponent)parent).newEventsOnly)) {
            if (eventEnabled(e)) {
                processEvent(e);
            } else if (e instanceof ActionEvent && parent != null) {
                e.setSource(parent);
                ((MenuComponent)parent).dispatchEvent(e);
            }

        } else { // 向后兼容
            Event olde = e.convertToOld();
            if (olde != null) {
                postEvent(olde);
            }
        }
    }

    // REMIND: remove when filtering is done at lower level
    boolean eventEnabled(AWTEvent e) {
        return false;
    }
    /**
     * 处理在此菜单组件上发生的事件。
     * <p>注意，如果事件参数为 <code>null</code>，则行为是未指定的，可能会导致异常。
     *
     * @param e 事件
     * @since JDK1.1
     */
    protected void processEvent(AWTEvent e) {
    }

    /**
     * 返回表示此 <code>MenuComponent</code> 状态的字符串。此方法仅用于调试目的，返回字符串的内容和格式
     * 可能在不同实现中有所不同。返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return 此菜单组件的参数字符串
     */
    protected String paramString() {
        String thisName = getName();
        return (thisName != null? thisName : "");
    }

    /**
     * 返回此菜单组件的字符串表示形式。
     * @return 此菜单组件的字符串表示形式
     */
    public String toString() {
        return getClass().getName() + "[" + paramString() + "]";
    }

    /**
     * 获取用于 AWT 组件树和布局操作的锁定对象（拥有线程同步监视器的对象）。
     * @return 此组件的锁定对象
     */
    protected final Object getTreeLock() {
        return Component.LOCK;
    }

    /**
     * 从对象输入流中读取菜单组件。
     *
     * @param s 要读取的 <code>ObjectInputStream</code>
     * @exception HeadlessException 如果 <code>GraphicsEnvironment.isHeadless</code> 返回
     *   <code>true</code>
     * @serial
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException, HeadlessException
    {
        GraphicsEnvironment.checkHeadless();

        acc = AccessController.getContext();

        s.defaultReadObject();

        appContext = AppContext.getAppContext();
    }

    /**
     * 初始化 JNI 字段和方法 ID。
     */
    private static native void initIDs();


    /*
     * --- Accessibility Support ---
     *
     *  MenuComponent 将包含接口 Accessible 中的所有方法，
     *  虽然它不会实际实现该接口 - 这将由扩展 MenuComponent 的各个对象来实现。
     */

    AccessibleContext accessibleContext = null;

    /**
     * 获取与此 <code>MenuComponent</code> 关联的 <code>AccessibleContext</code>。
     *
     * 该方法由基类实现，返回 <code>null</code>。扩展 <code>MenuComponent</code>
     * 的类应实现此方法，返回与子类关联的 <code>AccessibleContext</code>。
     *
     * @return 与此 <code>MenuComponent</code> 关联的 <code>AccessibleContext</code>
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        return accessibleContext;
    }


                /**
     * <code>MenuComponent</code> 的内部类，用于提供对可访问性的默认支持。
     * 此类不建议由应用程序开发人员直接使用，而是仅供菜单组件开发人员继承。
     * <p>
     * 用于获取此对象的可访问角色的类。
     * @since 1.3
     */
    protected abstract class AccessibleAWTMenuComponent
        extends AccessibleContext
        implements java.io.Serializable, AccessibleComponent,
                   AccessibleSelection
    {
        /*
         * JDK 1.3 serialVersionUID
         */
        private static final long serialVersionUID = -4269533416223798698L;

        /**
         * 尽管该类是抽象的，但所有子类都应调用此构造函数。
         */
        protected AccessibleAWTMenuComponent() {
        }

        // AccessibleContext 方法
        //

        /**
         * 获取与此对象关联的 <code>AccessibleSelection</code>，允许其 <code>Accessible</code> 子对象被选中。
         *
         * @return 如果对象支持 <code>AccessibleSelection</code>，则返回 <code>AccessibleSelection</code>；否则返回 <code>null</code>
         * @see AccessibleSelection
         */
        public AccessibleSelection getAccessibleSelection() {
            return this;
        }

        /**
         * 获取此对象的可访问名称。此方法几乎不应返回 <code>java.awt.MenuComponent.getName</code>，因为这通常不是一个本地化的名称，对用户没有意义。
         * 如果对象本质上是一个文本对象（例如菜单项），则可访问名称应为对象的文本（例如 "save"）。
         * 如果对象有工具提示，工具提示文本也可能是一个合适的返回字符串。
         *
         * @return 对象的本地化名称 -- 如果此对象没有名称，则可以返回 <code>null</code>
         * @see AccessibleContext#setAccessibleName
         */
        public String getAccessibleName() {
            return accessibleName;
        }

        /**
         * 获取此对象的可访问描述。这应该是一个简洁的、本地化的描述，说明此对象是什么 -- 对用户有什么意义。
         * 如果对象有工具提示，工具提示文本可能是一个合适的返回字符串，前提是它包含对象的简洁描述（而不是只是对象的名称 -- 例如，工具栏上的 "Save" 图标，如果工具提示文本是 "save"，则不应返回工具提示文本作为描述，而应返回类似 "保存当前文本文档" 的描述）。
         *
         * @return 对象的本地化描述 -- 如果此对象没有描述，则可以返回 <code>null</code>
         * @see AccessibleContext#setAccessibleDescription
         */
        public String getAccessibleDescription() {
            return accessibleDescription;
        }

        /**
         * 获取此对象的角色。
         *
         * @return 一个 <code>AccessibleRole</code> 实例，描述对象的角色
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.AWT_COMPONENT; // 非特定 -- 在子类中覆盖
        }

        /**
         * 获取此对象的状态。
         *
         * @return 一个 <code>AccessibleStateSet</code> 实例，包含对象的当前状态集
         * @see AccessibleState
         */
        public AccessibleStateSet getAccessibleStateSet() {
            return MenuComponent.this.getAccessibleStateSet();
        }

        /**
         * 获取此对象的可访问父对象。如果此对象的父对象实现了 <code>Accessible</code>，则此方法应直接返回 <code>getParent</code>。
         *
         * @return 此对象的 <code>Accessible</code> 父对象 -- 如果此对象没有 <code>Accessible</code> 父对象，则可以返回 <code>null</code>
         */
        public Accessible getAccessibleParent() {
            if (accessibleParent != null) {
                return accessibleParent;
            } else {
                MenuContainer parent = MenuComponent.this.getParent();
                if (parent instanceof Accessible) {
                    return (Accessible) parent;
                }
            }
            return null;
        }

        /**
         * 获取此对象在其可访问父对象中的索引。
         *
         * @return 此对象在其父对象中的索引；如果此对象没有可访问父对象，则返回 -1
         * @see #getAccessibleParent
         */
        public int getAccessibleIndexInParent() {
            return MenuComponent.this.getAccessibleIndexInParent();
        }

        /**
         * 返回对象中的可访问子对象数量。如果此对象的所有子对象都实现了 <code>Accessible</code>，则此方法应返回此对象的子对象数量。
         *
         * @return 对象中的可访问子对象数量
         */
        public int getAccessibleChildrenCount() {
            return 0; // MenuComponents 没有子对象
        }

        /**
         * 返回对象的第 n 个 <code>Accessible</code> 子对象。
         *
         * @param i 子对象的零基索引
         * @return 对象的第 n 个 <code>Accessible</code> 子对象
         */
        public Accessible getAccessibleChild(int i) {
            return null; // MenuComponents 没有子对象
        }

        /**
         * 返回此对象的区域设置。
         *
         * @return 此对象的区域设置
         */
        public java.util.Locale getLocale() {
            MenuContainer parent = MenuComponent.this.getParent();
            if (parent instanceof Component)
                return ((Component)parent).getLocale();
            else
                return java.util.Locale.getDefault();
        }

        /**
         * 获取与此对象关联的 <code>AccessibleComponent</code>，如果存在。否则返回 <code>null</code>。
         *
         * @return 组件
         */
        public AccessibleComponent getAccessibleComponent() {
            return this;
        }


        // AccessibleComponent 方法
        //
        /**
         * 获取此对象的背景颜色。
         *
         * @return 对象的背景颜色，如果支持；否则返回 <code>null</code>
         */
        public Color getBackground() {
            return null; // MenuComponents 不支持
        }

        /**
         * 设置此对象的背景颜色。（有关透明度，请参见 <code>isOpaque</code>。）
         *
         * @param c 对象的新 <code>Color</code> 背景颜色
         * @see Component#isOpaque
         */
        public void setBackground(Color c) {
            // MenuComponents 不支持
        }

        /**
         * 获取此对象的前景颜色。
         *
         * @return 对象的前景颜色，如果支持；否则返回 <code>null</code>
         */
        public Color getForeground() {
            return null; // MenuComponents 不支持
        }

        /**
         * 设置此对象的前景颜色。
         *
         * @param c 对象的新 <code>Color</code> 前景颜色
         */
        public void setForeground(Color c) {
            // MenuComponents 不支持
        }

        /**
         * 获取此对象的 <code>Cursor</code>。
         *
         * @return 对象的 <code>Cursor</code>，如果支持；否则返回 <code>null</code>
         */
        public Cursor getCursor() {
            return null; // MenuComponents 不支持
        }

        /**
         * 设置此对象的 <code>Cursor</code>。
         * <p>
         * 如果 Java 平台实现和/或本机系统不支持更改鼠标指针形状，此方法可能没有视觉效果。
         * @param cursor 对象的新 <code>Cursor</code>
         */
        public void setCursor(Cursor cursor) {
            // MenuComponents 不支持
        }

        /**
         * 获取此对象的 <code>Font</code>。
         *
         * @return 对象的 <code>Font</code>，如果支持；否则返回 <code>null</code>
         */
        public Font getFont() {
            return MenuComponent.this.getFont();
        }

        /**
         * 设置此对象的 <code>Font</code>。
         *
         * @param f 对象的新 <code>Font</code>
         */
        public void setFont(Font f) {
            MenuComponent.this.setFont(f);
        }

        /**
         * 获取此对象的 <code>FontMetrics</code>。
         *
         * @param f <code>Font</code>
         * @return 对象的 <code>FontMetrics</code>，如果支持；否则返回 <code>null</code>
         * @see #getFont
         */
        public FontMetrics getFontMetrics(Font f) {
            return null; // MenuComponents 不支持
        }

        /**
         * 确定对象是否已启用。
         *
         * @return 如果对象已启用，则返回 true；否则返回 false
         */
        public boolean isEnabled() {
            return true; // MenuComponents 不支持
        }

        /**
         * 设置对象的启用状态。
         *
         * @param b 如果为 true，则启用此对象；否则禁用此对象
         */
        public void setEnabled(boolean b) {
            // MenuComponents 不支持
        }

        /**
         * 确定对象是否可见。注意：这表示对象打算可见；但是，由于包含此对象的某个对象不可见，对象可能实际上并未显示在屏幕上。要确定对象是否显示在屏幕上，请使用 <code>isShowing</code>。
         *
         * @return 如果对象可见，则返回 true；否则返回 false
         */
        public boolean isVisible() {
            return true; // MenuComponents 不支持
        }

        /**
         * 设置对象的可见状态。
         *
         * @param b 如果为 true，则显示此对象；否则隐藏此对象
         */
        public void setVisible(boolean b) {
            // MenuComponents 不支持
        }

        /**
         * 确定对象是否显示。这是通过检查对象及其祖先的可见性来确定的。注意：即使对象被其他对象遮挡（例如，它恰好在被拉下的菜单下方），此方法也会返回 true。
         *
         * @return 如果对象显示，则返回 true；否则返回 false
         */
        public boolean isShowing() {
            return true; // MenuComponents 不支持
        }

        /**
         * 检查指定的点是否在对象的边界内，其中点的 x 和 y 坐标相对于对象的坐标系定义。
         *
         * @param p 相对于对象坐标系的 <code>Point</code>
         * @return 如果对象包含 <code>Point</code>，则返回 true；否则返回 false
         */
        public boolean contains(Point p) {
            return false; // MenuComponents 不支持
        }

        /**
         * 返回对象在屏幕上的位置。
         *
         * @return 对象在屏幕上的位置 -- 如果此对象不在屏幕上，则可以返回 <code>null</code>
         */
        public Point getLocationOnScreen() {
            return null; // MenuComponents 不支持
        }

        /**
         * 获取对象相对于父对象的位置，形式为一个表示对象左上角在屏幕坐标空间中的点的 <code>Point</code>。
         *
         * @return 一个 <code>Point</code> 实例，表示对象边界在屏幕坐标空间中的左上角；如果此对象或其父对象不在屏幕上，则返回 <code>null</code>
         */
        public Point getLocation() {
            return null; // MenuComponents 不支持
        }

        /**
         * 设置对象相对于父对象的位置。
         */
        public void setLocation(Point p) {
            // MenuComponents 不支持
        }

        /**
         * 以 <code>Rectangle</code> 对象的形式获取此对象的边界。
         * 边界指定了此对象的宽度、高度和相对于其父对象的位置。
         *
         * @return 一个表示此组件边界的矩形；如果此对象不在屏幕上，则返回 <code>null</code>
         */
        public Rectangle getBounds() {
            return null; // MenuComponents 不支持
        }

        /**
         * 以 <code>Rectangle</code> 对象的形式设置此对象的边界。
         * 边界指定了此对象的宽度、高度和相对于其父对象的位置。
         *
         * @param r 一个表示此组件边界的矩形
         */
        public void setBounds(Rectangle r) {
            // MenuComponents 不支持
        }

        /**
         * 以 <code>Dimension</code> 对象的形式返回此对象的大小。<code>Dimension</code> 对象的 height 字段包含此对象的高度，width 字段包含此对象的宽度。
         *
         * @return 一个 <code>Dimension</code> 对象，表示此组件的大小；如果此对象不在屏幕上，则返回 <code>null</code>
         */
        public Dimension getSize() {
            return null; // MenuComponents 不支持
        }

        /**
         * 调整此对象的大小。
         *
         * @param d - 指定对象新大小的 <code>Dimension</code>
         */
        public void setSize(Dimension d) {
            // MenuComponents 不支持
        }

        /**
         * 返回在本地坐标 <code>Point</code> 处的 <code>Accessible</code> 子对象（如果存在）。
         * 如果没有 <code>Accessible</code> 子对象，则返回 <code>null</code>。
         *
         * @param p 定义 <code>Accessible</code> 左上角的点，以对象父对象的坐标空间给出
         * @return 如果存在，则返回指定位置的 <code>Accessible</code>；否则返回 <code>null</code>
         */
        public Accessible getAccessibleAt(Point p) {
            return null; // MenuComponents 没有子对象
        }


                    /**
         * 返回此对象是否可以接受焦点。
         *
         * @return 如果对象可以接受焦点，则返回 true；否则返回 false
         */
        public boolean isFocusTraversable() {
            return true; // MenuComponents 不支持
        }

        /**
         * 请求此对象的焦点。
         */
        public void requestFocus() {
            // MenuComponents 不支持
        }

        /**
         * 添加指定的焦点监听器以接收此组件的焦点事件。
         *
         * @param l 焦点监听器
         */
        public void addFocusListener(java.awt.event.FocusListener l) {
            // MenuComponents 不支持
        }

        /**
         * 移除指定的焦点监听器，使其不再接收此组件的焦点事件。
         *
         * @param l 焦点监听器
         */
        public void removeFocusListener(java.awt.event.FocusListener l) {
            // MenuComponents 不支持
        }

        // AccessibleSelection 方法
        //

        /**
         * 返回当前选中的 <code>Accessible</code> 子对象的数量。
         * 如果没有选中的子对象，返回值为 0。
         *
         * @return 当前选中的项目数量
         */
         public int getAccessibleSelectionCount() {
             return 0;  //  将在未来的版本中完全实现
         }

        /**
         * 返回表示对象中指定选中子对象的 <code>Accessible</code>。
         * 如果没有选中，或者选中的子对象数量少于传入的整数，返回值将为 <code>null</code>。
         * <p>注意，索引表示第 i 个选中的子对象，这与第 i 个子对象不同。
         *
         * @param i 选中子对象的零基索引
         * @return 第 i 个选中的子对象
         * @see #getAccessibleSelectionCount
         */
         public Accessible getAccessibleSelection(int i) {
             return null;  //  将在未来的版本中完全实现
         }

        /**
         * 确定此对象的当前子对象是否被选中。
         *
         * @return 如果此对象的当前子对象被选中，则返回 true；否则返回 false
         * @param i 此 <code>Accessible</code> 对象中的子对象的零基索引
         * @see AccessibleContext#getAccessibleChild
         */
         public boolean isAccessibleChildSelected(int i) {
             return false;  //  将在未来的版本中完全实现
         }

        /**
         * 将对象的指定 <code>Accessible</code> 子对象添加到对象的选择中。
         * 如果对象支持多选，指定的子对象将被添加到任何现有的选择中，否则它将替换对象中现有的选择。
         * 如果指定的子对象已被选中，此方法没有效果。
         *
         * @param i 子对象的零基索引
         * @see AccessibleContext#getAccessibleChild
         */
         public void addAccessibleSelection(int i) {
               //  将在未来的版本中完全实现
         }

        /**
         * 从对象的选择中移除指定的子对象。
         * 如果指定的项目当前未被选中，此方法没有效果。
         *
         * @param i 子对象的零基索引
         * @see AccessibleContext#getAccessibleChild
         */
         public void removeAccessibleSelection(int i) {
               //  将在未来的版本中完全实现
         }

        /**
         * 清除对象中的选择，使对象中的任何子对象都不被选中。
         */
         public void clearAccessibleSelection() {
               //  将在未来的版本中完全实现
         }

        /**
         * 如果对象支持多选，则选择对象中的每个子对象。
         */
         public void selectAllAccessibleSelection() {
               //  将在未来的版本中完全实现
         }

    } // 内部类 AccessibleAWTComponent

    /**
     * 获取此对象在其可访问父对象中的索引。
     *
     * @return 如果此对象没有可访问的父对象，则返回 -1；否则返回子对象在其可访问父对象中的索引。
     */
    int getAccessibleIndexInParent() {
        MenuContainer localParent = parent;
        if (!(localParent instanceof MenuComponent)) {
            // MenuComponents 仅在位于 MenuComponents 内部时才有可访问索引
            return -1;
        }
        MenuComponent localParentMenu = (MenuComponent)localParent;
        return localParentMenu.getAccessibleChildIndex(this);
    }

    /**
     * 获取此 MenuComponent 中子对象的索引。
     *
     * @param child 我们感兴趣的子对象 MenuComponent。
     * @return 如果此对象不包含子对象，则返回 -1；否则返回子对象的索引。
     */
    int getAccessibleChildIndex(MenuComponent child) {
        return -1; // 在子类中重写。
    }

    /**
     * 获取此对象的状态。
     *
     * @return 包含对象当前状态集的 <code>AccessibleStateSet</code> 实例
     * @see AccessibleState
     */
    AccessibleStateSet getAccessibleStateSet() {
        AccessibleStateSet states = new AccessibleStateSet();
        return states;
    }

}
