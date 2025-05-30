
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

import java.awt.dnd.DropTarget;

import java.awt.event.*;

import java.awt.peer.ContainerPeer;
import java.awt.peer.ComponentPeer;
import java.awt.peer.LightweightPeer;

import java.beans.PropertyChangeListener;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.PrintStream;
import java.io.PrintWriter;

import java.lang.ref.WeakReference;
import java.security.AccessController;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

import javax.accessibility.*;

import sun.util.logging.PlatformLogger;

import sun.awt.AppContext;
import sun.awt.AWTAccessor;
import sun.awt.AWTAccessor.MouseEventAccessor;
import sun.awt.CausedFocusEvent;
import sun.awt.PeerEvent;
import sun.awt.SunToolkit;

import sun.awt.dnd.SunDropTargetEvent;

import sun.java2d.pipe.Region;

import sun.security.action.GetBooleanAction;

/**
 * 一个通用的抽象窗口工具包(AWT)容器对象是一个可以包含其他AWT组件的组件。
 * <p>
 * 添加到容器中的组件被跟踪在一个列表中。列表的顺序将定义组件在容器中的前后堆叠顺序。
 * 如果在将组件添加到容器时未指定索引，则它将被添加到列表的末尾
 * （并且因此位于堆叠顺序的底部）。
 * <p>
 * <b>注意</b>：有关焦点子系统的详细信息，请参阅
 * <a href="https://docs.oracle.com/javase/tutorial/uiswing/misc/focus.html">
 * 如何使用焦点子系统</a>，
 * 《Java教程》中的一个部分，以及
 * <a href="../../java/awt/doc-files/FocusSpec.html">焦点规范</a>
 * 以获取更多信息。
 *
 * @author      Arthur van Hoff
 * @author      Sami Shaio
 * @see       #add(java.awt.Component, int)
 * @see       #getComponent(int)
 * @see       LayoutManager
 * @since     JDK1.0
 */
public class Container extends Component {

    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.Container");
    private static final PlatformLogger eventLog = PlatformLogger.getLogger("java.awt.event.Container");

    private static final Component[] EMPTY_ARRAY = new Component[0];

    /**
     * 此容器中的组件。
     * @see #add
     * @see #getComponents
     */
    private java.util.List<Component> component = new ArrayList<>();

    /**
     * 此容器的布局管理器。
     * @see #doLayout
     * @see #setLayout
     * @see #getLayout
     */
    LayoutManager layoutMgr;

    /**
     * 轻量级组件的事件路由器。如果此容器是原生的，此调度器负责将事件转发和重新定位到包含的轻量级组件（如果有）。
     */
    private LightweightDispatcher dispatcher;

    /**
     * 如果此容器是焦点循环根，则管理此容器子组件键盘遍历的焦点遍历策略。如果值为null，则此容器从其焦点循环根祖先继承策略。
     * 如果此容器的所有此类祖先的策略均为null，则使用当前KeyboardFocusManager的默认策略。如果值非null，则此策略将被所有
     * 没有自己键盘遍历策略的焦点循环根子组件继承（这些焦点循环根子组件的焦点循环根子组件也将递归地继承此策略）。
     * <p>
     * 如果此容器不是焦点循环根，则该值将被记住，但不会被此容器或任何其他容器使用或继承，直到此容器成为焦点循环根。
     *
     * @see #setFocusTraversalPolicy
     * @see #getFocusTraversalPolicy
     * @since 1.4
     */
    private transient FocusTraversalPolicy focusTraversalPolicy;

    /**
     * 指示此组件是否是焦点遍历循环的根。一旦焦点进入遍历循环，通常除非按下上或下循环键，否则无法通过焦点遍历离开该循环。
     * 正常遍历仅限于此容器及其所有不是较低焦点循环根的后代。
     *
     * @see #setFocusCycleRoot
     * @see #isFocusCycleRoot
     * @since 1.4
     */
    private boolean focusCycleRoot = false;

    /**
     * 存储focusTraversalPolicyProvider属性的值。
     * @since 1.5
     * @see #setFocusTraversalPolicyProvider
     */
    private boolean focusTraversalPolicyProvider;

    // 跟踪正在打印此组件的线程
    private transient Set<Thread> printingThreads;
    // 如果至少有一个线程正在打印此组件，则为true
    private transient boolean printing = false;

    transient ContainerListener containerListener;

    /* HierarchyListener 和 HierarchyBoundsListener 支持 */
    transient int listeningChildren;
    transient int listeningBoundsChildren;
    transient int descendantsCount;

    /* 非不透明窗口支持 -- 参见 Window.setLayersOpaque */
    transient Color preserveBackgroundColor = null;

    /**
     * JDK 1.1 序列化版本ID
     */
    private static final long serialVersionUID = 4613797578919906343L;

    /**
     * 一个常量，用于切换 <code>getMouseEventTarget</code> 的一个可控行为。用于指定如果没有任何子组件是当前鼠标事件的目标，
     * 该方法是否可以返回最初调用该方法的容器。
     *
     * @see #getMouseEventTarget(int, int, boolean)
     */
    static final boolean INCLUDE_SELF = true;

    /**
     * 一个常量，用于切换 <code>getMouseEventTarget</code> 的一个可控行为。用于指定该方法是否仅搜索轻量级组件。
     *
     * @see #getMouseEventTarget(int, int, boolean)
     */
    static final boolean SEARCH_HEAVYWEIGHTS = true;

    /*
     * 此容器中的HW或LW组件的数量（包括所有后代容器）。
     */
    private transient int numOfHWComponents = 0;
    private transient int numOfLWComponents = 0;

    private static final PlatformLogger mixingLog = PlatformLogger.getLogger("java.awt.mixing.Container");

    /**
     * @serialField ncomponents                     int
     *       此容器中的组件数量。
     *       此值可以为null。
     * @serialField component                       Component[]
     *       此容器中的组件。
     * @serialField layoutMgr                       LayoutManager
     *       此容器的布局管理器。
     * @serialField dispatcher                      LightweightDispatcher
     *       轻量级组件的事件路由器。如果此容器是原生的，此调度器负责将事件转发和重新定位到包含的轻量级组件（如果有）。
     * @serialField maxSize                         Dimension
     *       此容器的最大尺寸。
     * @serialField focusCycleRoot                  boolean
     *       指示此组件是否是焦点遍历循环的根。一旦焦点进入遍历循环，通常除非按下上或下循环键，否则无法通过焦点遍历离开该循环。
     *       正常遍历仅限于此容器及其所有不是较低焦点循环根的后代。
     * @serialField containerSerializedDataVersion  int
     *       容器序列化数据版本。
     * @serialField focusTraversalPolicyProvider    boolean
     *       存储focusTraversalPolicyProvider属性的值。
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("ncomponents", Integer.TYPE),
        new ObjectStreamField("component", Component[].class),
        new ObjectStreamField("layoutMgr", LayoutManager.class),
        new ObjectStreamField("dispatcher", LightweightDispatcher.class),
        new ObjectStreamField("maxSize", Dimension.class),
        new ObjectStreamField("focusCycleRoot", Boolean.TYPE),
        new ObjectStreamField("containerSerializedDataVersion", Integer.TYPE),
        new ObjectStreamField("focusTraversalPolicyProvider", Boolean.TYPE),
    };

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }

        AWTAccessor.setContainerAccessor(new AWTAccessor.ContainerAccessor() {
            @Override
            public void validateUnconditionally(Container cont) {
                cont.validateUnconditionally();
            }

            @Override
            public Component findComponentAt(Container cont, int x, int y,
                    boolean ignoreEnabled) {
                return cont.findComponentAt(x, y, ignoreEnabled);
            }
        });
    }

    /**
     * 初始化JNI字段和方法ID，这些字段和方法可能从C中调用。
     */
    private static native void initIDs();

    /**
     * 构造一个新的容器。容器可以直接扩展，但在此情况下是轻量级的，必须由更高层次的原生组件（如Frame）包含。
     */
    public Container() {
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    void initializeFocusTraversalKeys() {
        focusTraversalKeys = new Set[4];
    }

    /**
     * 获取此面板中的组件数量。
     * <p>
     * 注意：此方法应在AWT树锁下调用。
     *
     * @return    此面板中的组件数量。
     * @see       #getComponent
     * @since     JDK1.1
     * @see Component#getTreeLock()
     */
    public int getComponentCount() {
        return countComponents();
    }

    /**
     * @deprecated 自JDK版本1.1起，
     * 替换为 getComponentCount()。
     */
    @Deprecated
    public int countComponents() {
        // 此方法未在AWT树锁下同步。
        // 相反，调用代码负责同步。详情见6784816。
        return component.size();
    }

    /**
     * 获取此容器中的第n个组件。
     * <p>
     * 注意：此方法应在AWT树锁下调用。
     *
     * @param      n   要获取的组件的索引。
     * @return     此容器中的第n个组件。
     * @exception  ArrayIndexOutOfBoundsException
     *                 如果第n个值不存在。
     * @see Component#getTreeLock()
     */
    public Component getComponent(int n) {
        // 此方法未在AWT树锁下同步。
        // 相反，调用代码负责同步。详情见6784816。
        try {
            return component.get(n);
        } catch (IndexOutOfBoundsException z) {
            throw new ArrayIndexOutOfBoundsException("No such child: " + n);
        }
    }

    /**
     * 获取此容器中的所有组件。
     * <p>
     * 注意：此方法应在AWT树锁下调用。
     *
     * @return    此容器中的所有组件数组。
     * @see Component#getTreeLock()
     */
    public Component[] getComponents() {
        // 此方法未在AWT树锁下同步。
        // 相反，调用代码负责同步。详情见6784816。
        return getComponents_NoClientCode();
    }

    // 注意：此方法可能由特权线程调用。
    //       此功能实现在一个包私有方法中，以确保客户端子类无法重写。
    //       不要在该线程上调用客户端代码！
    final Component[] getComponents_NoClientCode() {
        return component.toArray(EMPTY_ARRAY);
    }

    /*
     * getComponents() 方法的同步包装。
     */
    Component[] getComponentsSync() {
        synchronized (getTreeLock()) {
            return getComponents();
        }
    }

    /**
     * 确定此容器的边距，指示容器边框的大小。
     * <p>
     * 例如，一个 <code>Frame</code> 对象的顶部边距对应于框架标题栏的高度。
     * @return    此容器的边距。
     * @see       Insets
     * @see       LayoutManager
     * @since     JDK1.1
     */
    public Insets getInsets() {
        return insets();
    }

    /**
     * @deprecated 自JDK版本1.1起，
     * 替换为 <code>getInsets()</code>。
     */
    @Deprecated
    public Insets insets() {
        ComponentPeer peer = this.peer;
        if (peer instanceof ContainerPeer) {
            ContainerPeer cpeer = (ContainerPeer)peer;
            return (Insets)cpeer.getInsets().clone();
        }
        return new Insets(0, 0, 0, 0);
    }

    /**
     * 将指定的组件添加到此容器的末尾。
     * 这是 {@link #addImpl} 的便捷方法。
     * <p>
     * 此方法更改布局相关的信息，因此，会无效化组件层次结构。如果容器已显示，则必须验证层次结构以显示添加的组件。
     *
     * @param     comp   要添加的组件
     * @exception NullPointerException 如果 {@code comp} 为 {@code null}
     * @see #addImpl
     * @see #invalidate
     * @see #validate
     * @see javax.swing.JComponent#revalidate()
     * @return    组件参数
     */
    public Component add(Component comp) {
        addImpl(comp, null, -1);
        return comp;
    }


                /**
     * 将指定的组件添加到此容器中。
     * 这是 {@link #addImpl} 的便捷方法。
     * <p>
     * 从 1.1 版本开始，此方法已过时。请改用 <code>add(Component, Object)</code> 方法。
     * <p>
     * 此方法更改了与布局相关的信息，因此使组件层次结构失效。如果容器已经显示，则必须验证层次结构以显示添加的组件。
     *
     * @exception NullPointerException 如果 {@code comp} 为 {@code null}
     * @see #add(Component, Object)
     * @see #invalidate
     */
    public Component add(String name, Component comp) {
        addImpl(comp, name, -1);
        return comp;
    }

    /**
     * 将指定的组件添加到此容器中的指定位置。
     * 这是 {@link #addImpl} 的便捷方法。
     * <p>
     * 此方法更改了与布局相关的信息，因此使组件层次结构失效。如果容器已经显示，则必须验证层次结构以显示添加的组件。
     *
     *
     * @param     comp   要添加的组件
     * @param     index    要插入组件的位置，或 <code>-1</code> 表示将组件添加到末尾
     * @exception NullPointerException 如果 {@code comp} 为 {@code null}
     * @exception IllegalArgumentException 如果 {@code index} 无效（请参阅 {@link #addImpl} 以获取详细信息）
     * @return    组件 <code>comp</code>
     * @see #addImpl
     * @see #remove
     * @see #invalidate
     * @see #validate
     * @see javax.swing.JComponent#revalidate()
     */
    public Component add(Component comp, int index) {
        addImpl(comp, null, index);
        return comp;
    }

    /**
     * 检查组件是否不应添加到自身中。
     */
    private void checkAddToSelf(Component comp){
        if (comp instanceof Container) {
            for (Container cn = this; cn != null; cn=cn.parent) {
                if (cn == comp) {
                    throw new IllegalArgumentException("将容器的父容器添加到自身");
                }
            }
        }
    }

    /**
     * 检查组件是否不是 Window 实例。
     */
    private void checkNotAWindow(Component comp){
        if (comp instanceof Window) {
            throw new IllegalArgumentException("将窗口添加到容器中");
        }
    }

    /**
     * 检查组件 comp 是否可以添加到此容器中
     * 检查：索引在容器大小范围内，
     * comp 不是此容器的父容器，
     * 且 comp 不是窗口。
     * 组件和容器必须在同一 GraphicsDevice 上。
     * 如果 comp 是容器，则所有子组件必须在同一 GraphicsDevice 上。
     *
     * @since 1.5
     */
    private void checkAdding(Component comp, int index) {
        checkTreeLock();

        GraphicsConfiguration thisGC = getGraphicsConfiguration();

        if (index > component.size() || index < 0) {
            throw new IllegalArgumentException("非法的组件位置");
        }
        if (comp.parent == this) {
            if (index == component.size()) {
                throw new IllegalArgumentException("非法的组件位置 " +
                                                   index + " 应小于 " + component.size());
            }
        }
        checkAddToSelf(comp);
        checkNotAWindow(comp);

        Window thisTopLevel = getContainingWindow();
        Window compTopLevel = comp.getContainingWindow();
        if (thisTopLevel != compTopLevel) {
            throw new IllegalArgumentException("组件和容器应在同一顶级窗口中");
        }
        if (thisGC != null) {
            comp.checkGD(thisGC.getDevice().getIDstring());
        }
    }

    /**
     * 从此容器中删除组件 comp，而不进行不必要的更改
     * 和生成不必要的事件。此函数旨在执行优化的
     * 删除，例如，如果新父容器和当前父容器相同，则仅更改
     * 索引而不调用 removeNotify。
     * 注意：应在持有 treeLock 时调用此方法
     * 返回是否调用了 removeNotify
     * @since: 1.5
     */
    private boolean removeDelicately(Component comp, Container newParent, int newIndex) {
        checkTreeLock();

        int index = getComponentZOrder(comp);
        boolean needRemoveNotify = isRemoveNotifyNeeded(comp, this, newParent);
        if (needRemoveNotify) {
            comp.removeNotify();
        }
        if (newParent != this) {
            if (layoutMgr != null) {
                layoutMgr.removeLayoutComponent(comp);
            }
            adjustListeningChildren(AWTEvent.HIERARCHY_EVENT_MASK,
                                    -comp.numListening(AWTEvent.HIERARCHY_EVENT_MASK));
            adjustListeningChildren(AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK,
                                    -comp.numListening(AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK));
            adjustDescendants(-(comp.countHierarchyMembers()));

            comp.parent = null;
            if (needRemoveNotify) {
                comp.setGraphicsConfiguration(null);
            }
            component.remove(index);

            invalidateIfValid();
        } else {
            // 我们应该删除组件，然后
            // 在 newIndex 位置重新添加组件，即使我们在移除后将组件向左移动
            // 也不会减少 newIndex。请参阅以下规则：
            // 2->4: 012345 -> 013425, 2->5: 012345 -> 013452
            // 4->2: 012345 -> 014235
            component.remove(index);
            component.add(newIndex, comp);
        }
        if (comp.parent == null) { // 实际上已移除
            if (containerListener != null ||
                (eventMask & AWTEvent.CONTAINER_EVENT_MASK) != 0 ||
                Toolkit.enabledOnToolkit(AWTEvent.CONTAINER_EVENT_MASK)) {
                ContainerEvent e = new ContainerEvent(this,
                                                      ContainerEvent.COMPONENT_REMOVED,
                                                      comp);
                dispatchEvent(e);

            }
            comp.createHierarchyEvents(HierarchyEvent.HIERARCHY_CHANGED, comp,
                                       this, HierarchyEvent.PARENT_CHANGED,
                                       Toolkit.enabledOnToolkit(AWTEvent.HIERARCHY_EVENT_MASK));
            if (peer != null && layoutMgr == null && isVisible()) {
                updateCursorImmediately();
            }
        }
        return needRemoveNotify;
    }

    /**
     * 检查此容器是否可以包含焦点所有者组件。
     * 验证容器是否启用并显示，如果它是焦点循环根
     * 其 FTP 允许组件成为焦点所有者
     * @since 1.5
     */
    boolean canContainFocusOwner(Component focusOwnerCandidate) {
        if (!(isEnabled() && isDisplayable()
              && isVisible() && isFocusable()))
        {
            return false;
        }
        if (isFocusCycleRoot()) {
            FocusTraversalPolicy policy = getFocusTraversalPolicy();
            if (policy instanceof DefaultFocusTraversalPolicy) {
                if (!((DefaultFocusTraversalPolicy)policy).accept(focusOwnerCandidate)) {
                    return false;
                }
            }
        }
        synchronized(getTreeLock()) {
            if (parent != null) {
                return parent.canContainFocusOwner(focusOwnerCandidate);
            }
        }
        return true;
    }

    /**
     * 检查此容器是否有重量级子组件。
     * 注意：应在持有 tree lock 时调用此方法
     * @return 如果容器中至少有一个重量级子组件，则返回 true，否则返回 false
     * @since 1.5
     */
    final boolean hasHeavyweightDescendants() {
        checkTreeLock();
        return numOfHWComponents > 0;
    }

    /**
     * 检查此容器是否有轻量级子组件。
     * 注意：应在持有 tree lock 时调用此方法
     * @return 如果容器中至少有一个轻量级子组件，则返回 true，否则返回 false
     * @since 1.7
     */
    final boolean hasLightweightDescendants() {
        checkTreeLock();
        return numOfLWComponents > 0;
    }

    /**
     * 返回与此容器最近的重量级组件。如果此容器是重量级的
     * 则返回此容器。
     * @since 1.5
     */
    Container getHeavyweightContainer() {
        checkTreeLock();
        if (peer != null && !(peer instanceof LightweightPeer)) {
            return this;
        } else {
            return getNativeContainer();
        }
    }

    /**
     * 检测从当前父容器移除并添加到新父容器是否需要调用
     * removeNotify 方法。由于 removeNotify 会销毁原生窗口，这可能是（不）必要的。
     * 例如，如果新容器和旧容器相同，则不需要销毁原生窗口。
     * @since: 1.5
     */
    private static boolean isRemoveNotifyNeeded(Component comp, Container oldContainer, Container newContainer) {
        if (oldContainer == null) { // 组件没有父容器 - 不需要 removeNotify
            return false;
        }
        if (comp.peer == null) { // 组件没有 peer - 不需要 removeNotify
            return false;
        }
        if (newContainer.peer == null) {
            // 组件有 peer 但新容器没有 - 需要调用 removeNotify
            return true;
        }

        // 如果组件是轻量级非容器或轻量级容器且所有子组件都不是重量级
        // 则不需要调用 remove notify
        if (comp.isLightweight()) {
            boolean isContainer = comp instanceof Container;

            if (!isContainer || (isContainer && !((Container)comp).hasHeavyweightDescendants())) {
                return false;
            }
        }

        // 如果执行到这一点，那么组件要么是重量级的，要么是轻量级容器且有重量级子组件。

        // 三个组件都有 peer，检查 peer 是否更改
        Container newNativeContainer = oldContainer.getHeavyweightContainer();
        Container oldNativeContainer = newContainer.getHeavyweightContainer();
        if (newNativeContainer != oldNativeContainer) {
            // 原生容器更改 - 检查当前平台是否支持
            // 在原生级别上更改小部件层次结构而不重新创建。
            // 当前实现禁止将轻量级容器带有重量级子组件
            // 重新父容器化到另一个原生容器而不销毁 peer。实际上这样的操作
            // 很少。如果我们需要保存 peer，我们将不得不稍微改变
            // addDelicately() 方法以递归处理这样的轻量级容器，独立地重新父容器化
            // 每个重量级子组件。
            return !comp.peer.isReparentSupported();
        } else {
            return false;
        }
    }

    /**
     * 将指定的组件移动到容器中的指定 z-order 索引。
     * z-order 决定了组件的绘制顺序；z-order 最高的组件先绘制
     * z-order 最低的组件最后绘制。
     * 当组件重叠时，z-order 较低的组件会覆盖 z-order 较高的组件。
     * <p>
     * 如果组件是其他容器的子组件，则会
     * 从该容器中移除，然后再添加到此容器中。
     * 该方法与 <code>java.awt.Container.add(Component, int)</code> 的重要区别在于，
     * 在从其先前的容器中移除组件时，此方法不会调用 <code>removeNotify</code>
     * 除非必要且底层原生窗口系统允许。这样，如果组件具有键盘焦点，
     * 在移动到新位置时将保持焦点。
     * <p>
     * 仅对轻量级非 <code>Container</code> 组件保证此属性。
     * <p>
     * 此方法更改了与布局相关的信息，因此使组件层次结构失效。
     * <p>
     * <b>注意</b>：并非所有平台都支持在不调用 <code>removeNotify</code> 的情况下
     * 更改重量级组件从一个容器到另一个容器的 z-order。无法检测平台是否支持此操作，
     * 因此开发人员不应对此做出任何假设。
     *
     * @param     comp 要移动的组件
     * @param     index 在容器列表中插入组件的位置，其中 <code>getComponentCount()</code>
     *            表示追加到末尾
     * @exception NullPointerException 如果 <code>comp</code> 为
     *            <code>null</code>
     * @exception IllegalArgumentException 如果 <code>comp</code> 是容器的父容器之一
     * @exception IllegalArgumentException 如果 <code>index</code> 不在
     *            范围 <code>[0, getComponentCount()]</code> 内（在容器之间移动时），或不在
     *            范围 <code>[0, getComponentCount()-1]</code> 内（在容器内部移动时）
     * @exception IllegalArgumentException 如果将容器添加到自身
     * @exception IllegalArgumentException 如果将 <code>Window</code>
     *            添加到容器中
     * @see #getComponentZOrder(java.awt.Component)
     * @see #invalidate
     * @since 1.5
     */
    public void setComponentZOrder(Component comp, int index) {
         synchronized (getTreeLock()) {
             // 存储父容器，因为移除会清除它
             Container curParent = comp.parent;
             int oldZindex = getComponentZOrder(comp);

             if (curParent == this && index == oldZindex) {
                 return;
             }
             checkAdding(comp, index);

             boolean peerRecreated = (curParent != null) ?
                 curParent.removeDelicately(comp, this, index) : false;

             addDelicately(comp, curParent, index);

             // 如果 oldZindex == -1，组件被插入，
             // 而不是改变其 z-order。
             if (!peerRecreated && oldZindex != -1) {
                 // 新的 'index' 不能 == -1。
                 // 在 checkAdding() 方法中进行了检查。
                 // 因此此时 oldZIndex 和 index 都表示
                 // 某些现有位置，这实际上是 z-order 的更改。
                 comp.mixOnZOrderChanging(oldZindex, index);
             }
         }
    }

    /**
     * 遍历组件树并重新父容器化子重量级组件
     * 到新的重量级父容器。
     * @since 1.5
     */
    private void reparentTraverse(ContainerPeer parentPeer, Container child) {
        checkTreeLock();


                    for (int i = 0; i < child.getComponentCount(); i++) {
            Component comp = child.getComponent(i);
            if (comp.isLightweight()) {
                // 如果组件是轻量级的，检查它是否是容器
                // 如果是容器，它可能包含需要重新父化的重量级子组件
                if (comp instanceof Container) {
                    reparentTraverse(parentPeer, (Container)comp);
                }
            } else {
                // Q: 需要更新 NativeInLightFixer？
                comp.getPeer().reparent(parentPeer);
            }
        }
    }

    /**
     * 将子组件的对等体重新父化到此容器的对等体。
     * 容器必须是重量级的。
     * @since 1.5
     */
    private void reparentChild(Component comp) {
        checkTreeLock();
        if (comp == null) {
            return;
        }
        if (comp.isLightweight()) {
            // 如果组件是轻量级容器，需要重新父化其所有显式的重量级子组件
            if (comp instanceof Container) {
                // 遍历组件的树，直到深度优先遍历到重量级组件
                reparentTraverse((ContainerPeer)getPeer(), (Container)comp);
            }
        } else {
            comp.getPeer().reparent((ContainerPeer)getPeer());
        }
    }

    /**
     * 将组件添加到此容器。尽量减少此添加的副作用 -
     * 如果不需要，不会调用 remove notify。
     * @since 1.5
     */
    private void addDelicately(Component comp, Container curParent, int index) {
        checkTreeLock();

        // 检查是否在容器之间移动
        if (curParent != this) {
            // index == -1 表示添加到末尾。
            if (index == -1) {
                component.add(comp);
            } else {
                component.add(index, comp);
            }
            comp.parent = this;
            comp.setGraphicsConfiguration(getGraphicsConfiguration());

            adjustListeningChildren(AWTEvent.HIERARCHY_EVENT_MASK,
                                    comp.numListening(AWTEvent.HIERARCHY_EVENT_MASK));
            adjustListeningChildren(AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK,
                                    comp.numListening(AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK));
            adjustDescendants(comp.countHierarchyMembers());
        } else {
            if (index < component.size()) {
                component.set(index, comp);
            }
        }

        invalidateIfValid();
        if (peer != null) {
            if (comp.peer == null) { // 调用了 remove notify 或者它没有对等体 - 创建新的对等体
                comp.addNotify();
            } else { // 容器和子组件都有对等体，这意味着子组件的对等体应该被重新父化。
                // 在这两种情况下，都需要重新父化原生小部件。
                Container newNativeContainer = getHeavyweightContainer();
                Container oldNativeContainer = curParent.getHeavyweightContainer();
                if (oldNativeContainer != newNativeContainer) {
                    // 原生容器已更改 - 需要重新父化原生小部件
                    newNativeContainer.reparentChild(comp);
                }
                comp.updateZOrder();

                if (!comp.isLightweight() && isLightweight()) {
                    // 如果组件是重量级的，且其中一个容器是轻量级的
                    // 应该修复组件的位置。
                    comp.relocateComponent();
                }
            }
        }
        if (curParent != this) {
            /* 通知布局管理器已添加的组件。 */
            if (layoutMgr != null) {
                if (layoutMgr instanceof LayoutManager2) {
                    ((LayoutManager2)layoutMgr).addLayoutComponent(comp, null);
                } else {
                    layoutMgr.addLayoutComponent(null, comp);
                }
            }
            if (containerListener != null ||
                (eventMask & AWTEvent.CONTAINER_EVENT_MASK) != 0 ||
                Toolkit.enabledOnToolkit(AWTEvent.CONTAINER_EVENT_MASK)) {
                ContainerEvent e = new ContainerEvent(this,
                                                      ContainerEvent.COMPONENT_ADDED,
                                                      comp);
                dispatchEvent(e);
            }
            comp.createHierarchyEvents(HierarchyEvent.HIERARCHY_CHANGED, comp,
                                       this, HierarchyEvent.PARENT_CHANGED,
                                       Toolkit.enabledOnToolkit(AWTEvent.HIERARCHY_EVENT_MASK));

            // 如果组件是焦点所有者或焦点所有者的父容器，检查重新父化后
            // 焦点所有者是否移出，如果新容器禁止这种焦点所有者。
            if (comp.isFocusOwner() && !comp.canBeFocusOwnerRecursively()) {
                comp.transferFocus();
            } else if (comp instanceof Container) {
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focusOwner != null && isParentOf(focusOwner) && !focusOwner.canBeFocusOwnerRecursively()) {
                    focusOwner.transferFocus();
                }
            }
        } else {
            comp.createHierarchyEvents(HierarchyEvent.HIERARCHY_CHANGED, comp,
                                       this, HierarchyEvent.HIERARCHY_CHANGED,
                                       Toolkit.enabledOnToolkit(AWTEvent.HIERARCHY_EVENT_MASK));
        }

        if (peer != null && layoutMgr == null && isVisible()) {
            updateCursorImmediately();
        }
    }

    /**
     * 返回组件在容器中的 z-order 索引。
     * 组件在 z-order 层次结构中的位置越高，其索引越低。
     * z-order 索引最低的组件最后绘制，位于所有其他子组件之上。
     *
     * @param comp 被查询的组件
     * @return 组件的 z-order 索引；如果组件为 <code>null</code>
     *          或不属于容器，则返回 -1
     * @see #setComponentZOrder(java.awt.Component, int)
     * @since 1.5
     */
    public int getComponentZOrder(Component comp) {
        if (comp == null) {
            return -1;
        }
        synchronized(getTreeLock()) {
            // 快速检查 - 容器应该是组件的直接父容器
            if (comp.parent != this) {
                return -1;
            }
            return component.indexOf(comp);
        }
    }

    /**
     * 将指定的组件添加到此容器的末尾。
     * 还通知布局管理器使用指定的约束对象将组件添加到此容器的布局中。
     * 这是 {@link #addImpl} 的便捷方法。
     * <p>
     * 此方法更改布局相关的信息，因此会无效化组件层次结构。如果容器已显示，
     * 之后必须验证层次结构以显示添加的组件。
     *
     *
     * @param     comp 要添加的组件
     * @param     constraints 表达此组件布局约束的对象
     * @exception NullPointerException 如果 {@code comp} 为 {@code null}
     * @see #addImpl
     * @see #invalidate
     * @see #validate
     * @see javax.swing.JComponent#revalidate()
     * @see       LayoutManager
     * @since     JDK1.1
     */
    public void add(Component comp, Object constraints) {
        addImpl(comp, constraints, -1);
    }

    /**
     * 将指定的组件添加到此容器的指定索引处，并使用指定的约束对象通知布局管理器。
     * 这是 {@link #addImpl} 的便捷方法。
     * <p>
     * 此方法更改布局相关的信息，因此会无效化组件层次结构。如果容器已显示，
     * 之后必须验证层次结构以显示添加的组件。
     *
     *
     * @param comp 要添加的组件
     * @param constraints 表达此组件布局约束的对象
     * @param index 在容器列表中插入组件的位置；<code>-1</code> 表示插入到末尾
     * component
     * @exception NullPointerException 如果 {@code comp} 为 {@code null}
     * @exception IllegalArgumentException 如果 {@code index} 无效（请参阅
     *            {@link #addImpl} 以获取详细信息）
     * @see #addImpl
     * @see #invalidate
     * @see #validate
     * @see javax.swing.JComponent#revalidate()
     * @see #remove
     * @see LayoutManager
     */
    public void add(Component comp, Object constraints, int index) {
       addImpl(comp, constraints, index);
    }

    /**
     * 将指定的组件添加到此容器的指定索引处。此方法还通过 <code>addLayoutComponent</code>
     * 方法通知布局管理器使用指定的约束对象将组件添加到此容器的布局中。
     * <p>
     * 约束由当前使用的特定布局管理器定义。例如，<code>BorderLayout</code> 类定义了五个约束：
     * <code>BorderLayout.NORTH</code>、<code>BorderLayout.SOUTH</code>、<code>BorderLayout.EAST</code>、
     * <code>BorderLayout.WEST</code> 和 <code>BorderLayout.CENTER</code>。
     * <p>
     * <code>GridBagLayout</code> 类需要一个 <code>GridBagConstraints</code> 对象。传递不正确的约束对象类型会导致
     * <code>IllegalArgumentException</code>。
     * <p>
     * 如果当前布局管理器实现了 {@code LayoutManager2}，则调用
     * {@link LayoutManager2#addLayoutComponent(Component,Object)}。如果当前布局管理器未实现
     * {@code LayoutManager2}，且约束是一个 {@code String}，则调用
     * {@link LayoutManager#addLayoutComponent(String,Component)}。
     * <p>
     * 如果组件不是此容器的祖先且有非空父容器，则在添加到此容器之前，它将从当前父容器中移除。
     * <p>
     * 如果程序需要跟踪容器的每个添加请求，应该重写此方法，因为所有其他添加方法都会调用此方法。重写的方法通常应包括对超类版本的调用：
     *
     * <blockquote>
     * <code>super.addImpl(comp, constraints, index)</code>
     * </blockquote>
     * <p>
     * 此方法更改布局相关的信息，因此会无效化组件层次结构。如果容器已显示，
     * 之后必须验证层次结构以显示添加的组件。
     *
     * @param     comp       要添加的组件
     * @param     constraints 表达此组件布局约束的对象
     * @param     index 在容器列表中插入组件的位置，其中 <code>-1</code>
     *                 表示追加到末尾
     * @exception IllegalArgumentException 如果 {@code index} 无效；
     *            如果 {@code comp} 是此容器的子组件，有效范围是 {@code [-1, getComponentCount()-1]}；
     *            如果组件不是此容器的子组件，有效范围是
     *            {@code [-1, getComponentCount()]}
     *
     * @exception IllegalArgumentException 如果 {@code comp} 是此容器的祖先
     * @exception IllegalArgumentException 如果将窗口添加到容器
     * @exception NullPointerException 如果 {@code comp} 为 {@code null}
     * @see       #add(Component)
     * @see       #add(Component, int)
     * @see       #add(Component, java.lang.Object)
     * @see #invalidate
     * @see       LayoutManager
     * @see       LayoutManager2
     * @since     JDK1.1
     */
    protected void addImpl(Component comp, Object constraints, int index) {
        synchronized (getTreeLock()) {
            /* 检查参数是否正确：索引在范围内，
             * comp 不能是此容器的父容器之一，
             * comp 不能是窗口。
             * comp 和容器必须在同一个 GraphicsDevice 上。
             * 如果 comp 是容器，所有子组件必须在
             * 同一个 GraphicsDevice 上。
             */
            GraphicsConfiguration thisGC = this.getGraphicsConfiguration();

            if (index > component.size() || (index < 0 && index != -1)) {
                throw new IllegalArgumentException(
                          "非法的组件位置");
            }
            checkAddToSelf(comp);
            checkNotAWindow(comp);
            /* 重新父化组件并整理树的状态。 */
            if (comp.parent != null) {
                comp.parent.remove(comp);
                if (index > component.size()) {
                    throw new IllegalArgumentException("非法的组件位置");
                }
            }
            if (thisGC != null) {
                comp.checkGD(thisGC.getDevice().getIDstring());
            }



            // index == -1 表示添加到末尾。
            if (index == -1) {
                component.add(comp);
            } else {
                component.add(index, comp);
            }
            comp.parent = this;
            comp.setGraphicsConfiguration(thisGC);

            adjustListeningChildren(AWTEvent.HIERARCHY_EVENT_MASK,
                comp.numListening(AWTEvent.HIERARCHY_EVENT_MASK));
            adjustListeningChildren(AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK,
                comp.numListening(AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK));
            adjustDescendants(comp.countHierarchyMembers());

            invalidateIfValid();
            if (peer != null) {
                comp.addNotify();
            }

            /* 通知布局管理器已添加的组件。 */
            if (layoutMgr != null) {
                if (layoutMgr instanceof LayoutManager2) {
                    ((LayoutManager2)layoutMgr).addLayoutComponent(comp, constraints);
                } else if (constraints instanceof String) {
                    layoutMgr.addLayoutComponent((String)constraints, comp);
                }
            }
            if (containerListener != null ||
                (eventMask & AWTEvent.CONTAINER_EVENT_MASK) != 0 ||
                Toolkit.enabledOnToolkit(AWTEvent.CONTAINER_EVENT_MASK)) {
                ContainerEvent e = new ContainerEvent(this,
                                     ContainerEvent.COMPONENT_ADDED,
                                     comp);
                dispatchEvent(e);
            }


                        comp.createHierarchyEvents(HierarchyEvent.HIERARCHY_CHANGED, comp,
                                       this, HierarchyEvent.PARENT_CHANGED,
                                       Toolkit.enabledOnToolkit(AWTEvent.HIERARCHY_EVENT_MASK));
            if (peer != null && layoutMgr == null && isVisible()) {
                updateCursorImmediately();
            }
        }
    }

    @Override
    boolean updateGraphicsData(GraphicsConfiguration gc) {
        checkTreeLock();

        boolean ret = super.updateGraphicsData(gc);

        for (Component comp : component) {
            if (comp != null) {
                ret |= comp.updateGraphicsData(gc);
            }
        }
        return ret;
    }

    /**
     * 检查此容器包含的所有组件是否都在与该容器相同的 GraphicsDevice 上。如果不是，则抛出 IllegalArgumentException。
     */
    void checkGD(String stringID) {
        for (Component comp : component) {
            if (comp != null) {
                comp.checkGD(stringID);
            }
        }
    }

    /**
     * 从该容器中移除由 <code>index</code> 指定的组件。
     * 此方法还会通过 <code>removeLayoutComponent</code> 方法通知布局管理器从该容器的布局中移除组件。
     * <p>
     * 此方法会更改布局相关的信息，因此会使组件层次结构失效。如果容器已经显示，则必须验证层次结构以反映更改。
     *
     *
     * @param     index   要移除的组件的索引
     * @throws ArrayIndexOutOfBoundsException 如果 {@code index} 不在范围 {@code [0, getComponentCount()-1]} 内
     * @see #add
     * @see #invalidate
     * @see #validate
     * @see #getComponentCount
     * @since JDK1.1
     */
    public void remove(int index) {
        synchronized (getTreeLock()) {
            if (index < 0  || index >= component.size()) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            Component comp = component.get(index);
            if (peer != null) {
                comp.removeNotify();
            }
            if (layoutMgr != null) {
                layoutMgr.removeLayoutComponent(comp);
            }

            adjustListeningChildren(AWTEvent.HIERARCHY_EVENT_MASK,
                -comp.numListening(AWTEvent.HIERARCHY_EVENT_MASK));
            adjustListeningChildren(AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK,
                -comp.numListening(AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK));
            adjustDescendants(-(comp.countHierarchyMembers()));

            comp.parent = null;
            component.remove(index);
            comp.setGraphicsConfiguration(null);

            invalidateIfValid();
            if (containerListener != null ||
                (eventMask & AWTEvent.CONTAINER_EVENT_MASK) != 0 ||
                Toolkit.enabledOnToolkit(AWTEvent.CONTAINER_EVENT_MASK)) {
                ContainerEvent e = new ContainerEvent(this,
                                     ContainerEvent.COMPONENT_REMOVED,
                                     comp);
                dispatchEvent(e);
            }

            comp.createHierarchyEvents(HierarchyEvent.HIERARCHY_CHANGED, comp,
                                       this, HierarchyEvent.PARENT_CHANGED,
                                       Toolkit.enabledOnToolkit(AWTEvent.HIERARCHY_EVENT_MASK));
            if (peer != null && layoutMgr == null && isVisible()) {
                updateCursorImmediately();
            }
        }
    }

    /**
     * 从该容器中移除指定的组件。
     * 此方法还会通过 <code>removeLayoutComponent</code> 方法通知布局管理器从该容器的布局中移除组件。
     * <p>
     * 此方法会更改布局相关的信息，因此会使组件层次结构失效。如果容器已经显示，则必须验证层次结构以反映更改。
     *
     * @param comp 要移除的组件
     * @throws NullPointerException 如果 {@code comp} 为 {@code null}
     * @see #add
     * @see #invalidate
     * @see #validate
     * @see #remove(int)
     */
    public void remove(Component comp) {
        synchronized (getTreeLock()) {
            if (comp.parent == this)  {
                int index = component.indexOf(comp);
                if (index >= 0) {
                    remove(index);
                }
            }
        }
    }

    /**
     * 从该容器中移除所有组件。
     * 此方法还会通过 <code>removeLayoutComponent</code> 方法通知布局管理器从该容器的布局中移除组件。
     * <p>
     * 此方法会更改布局相关的信息，因此会使组件层次结构失效。如果容器已经显示，则必须验证层次结构以反映更改。
     *
     * @see #add
     * @see #remove
     * @see #invalidate
     */
    public void removeAll() {
        synchronized (getTreeLock()) {
            adjustListeningChildren(AWTEvent.HIERARCHY_EVENT_MASK,
                                    -listeningChildren);
            adjustListeningChildren(AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK,
                                    -listeningBoundsChildren);
            adjustDescendants(-descendantsCount);

            while (!component.isEmpty()) {
                Component comp = component.remove(component.size()-1);

                if (peer != null) {
                    comp.removeNotify();
                }
                if (layoutMgr != null) {
                    layoutMgr.removeLayoutComponent(comp);
                }
                comp.parent = null;
                comp.setGraphicsConfiguration(null);
                if (containerListener != null ||
                   (eventMask & AWTEvent.CONTAINER_EVENT_MASK) != 0 ||
                    Toolkit.enabledOnToolkit(AWTEvent.CONTAINER_EVENT_MASK)) {
                    ContainerEvent e = new ContainerEvent(this,
                                     ContainerEvent.COMPONENT_REMOVED,
                                     comp);
                    dispatchEvent(e);
                }

                comp.createHierarchyEvents(HierarchyEvent.HIERARCHY_CHANGED,
                                           comp, this,
                                           HierarchyEvent.PARENT_CHANGED,
                                           Toolkit.enabledOnToolkit(AWTEvent.HIERARCHY_EVENT_MASK));
            }
            if (peer != null && layoutMgr == null && isVisible()) {
                updateCursorImmediately();
            }
            invalidateIfValid();
        }
    }

    // 应仅在持有树锁时调用
    int numListening(long mask) {
        int superListening = super.numListening(mask);

        if (mask == AWTEvent.HIERARCHY_EVENT_MASK) {
            if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
                // 验证 listeningChildren 是否正确
                int sum = 0;
                for (Component comp : component) {
                    sum += comp.numListening(mask);
                }
                if (listeningChildren != sum) {
                    eventLog.fine("断言 (listeningChildren == sum) 失败");
                }
            }
            return listeningChildren + superListening;
        } else if (mask == AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK) {
            if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
                // 验证 listeningBoundsChildren 是否正确
                int sum = 0;
                for (Component comp : component) {
                    sum += comp.numListening(mask);
                }
                if (listeningBoundsChildren != sum) {
                    eventLog.fine("断言 (listeningBoundsChildren == sum) 失败");
                }
            }
            return listeningBoundsChildren + superListening;
        } else {
            // assert false;
            if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
                eventLog.fine("此代码不应执行");
            }
            return superListening;
        }
    }

    // 应仅在持有树锁时调用
    void adjustListeningChildren(long mask, int num) {
        if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
            boolean toAssert = (mask == AWTEvent.HIERARCHY_EVENT_MASK ||
                                mask == AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK ||
                                mask == (AWTEvent.HIERARCHY_EVENT_MASK |
                                         AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK));
            if (!toAssert) {
                eventLog.fine("断言失败");
            }
        }

        if (num == 0)
            return;

        if ((mask & AWTEvent.HIERARCHY_EVENT_MASK) != 0) {
            listeningChildren += num;
        }
        if ((mask & AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK) != 0) {
            listeningBoundsChildren += num;
        }

        adjustListeningChildrenOnParent(mask, num);
    }

    // 应仅在持有树锁时调用
    void adjustDescendants(int num) {
        if (num == 0)
            return;

        descendantsCount += num;
        adjustDecendantsOnParent(num);
    }

    // 应仅在持有树锁时调用
    void adjustDecendantsOnParent(int num) {
        if (parent != null) {
            parent.adjustDescendants(num);
        }
    }

    // 应仅在持有树锁时调用
    int countHierarchyMembers() {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            // 验证 descendantsCount 是否正确
            int sum = 0;
            for (Component comp : component) {
                sum += comp.countHierarchyMembers();
            }
            if (descendantsCount != sum) {
                log.fine("断言 (descendantsCount == sum) 失败");
            }
        }
        return descendantsCount + 1;
    }

    private int getListenersCount(int id, boolean enabledOnToolkit) {
        checkTreeLock();
        if (enabledOnToolkit) {
            return descendantsCount;
        }
        switch (id) {
          case HierarchyEvent.HIERARCHY_CHANGED:
            return listeningChildren;
          case HierarchyEvent.ANCESTOR_MOVED:
          case HierarchyEvent.ANCESTOR_RESIZED:
            return listeningBoundsChildren;
          default:
            return 0;
        }
    }

    final int createHierarchyEvents(int id, Component changed,
        Container changedParent, long changeFlags, boolean enabledOnToolkit)
    {
        checkTreeLock();
        int listeners = getListenersCount(id, enabledOnToolkit);

        for (int count = listeners, i = 0; count > 0; i++) {
            count -= component.get(i).createHierarchyEvents(id, changed,
                changedParent, changeFlags, enabledOnToolkit);
        }
        return listeners +
            super.createHierarchyEvents(id, changed, changedParent,
                                        changeFlags, enabledOnToolkit);
    }

    final void createChildHierarchyEvents(int id, long changeFlags,
        boolean enabledOnToolkit)
    {
        checkTreeLock();
        if (component.isEmpty()) {
            return;
        }
        int listeners = getListenersCount(id, enabledOnToolkit);

        for (int count = listeners, i = 0; count > 0; i++) {
            count -= component.get(i).createHierarchyEvents(id, this, parent,
                changeFlags, enabledOnToolkit);
        }
    }

    /**
     * 获取此容器的布局管理器。
     * @see #doLayout
     * @see #setLayout
     */
    public LayoutManager getLayout() {
        return layoutMgr;
    }

    /**
     * 设置此容器的布局管理器。
     * <p>
     * 此方法会更改布局相关的信息，因此会使组件层次结构失效。
     *
     * @param mgr 指定的布局管理器
     * @see #doLayout
     * @see #getLayout
     * @see #invalidate
     */
    public void setLayout(LayoutManager mgr) {
        layoutMgr = mgr;
        invalidateIfValid();
    }

    /**
     * 使此容器布局其组件。大多数程序不应直接调用此方法，而应调用
     * <code>validate</code> 方法。
     * @see LayoutManager#layoutContainer
     * @see #setLayout
     * @see #validate
     * @since JDK1.1
     */
    public void doLayout() {
        layout();
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>doLayout()</code>。
     */
    @Deprecated
    public void layout() {
        LayoutManager layoutMgr = this.layoutMgr;
        if (layoutMgr != null) {
            layoutMgr.layoutContainer(this);
        }
    }

    /**
     * 指示此容器是否为 <i>验证根</i>。
     * <p>
     * 布局相关的更改，如验证根后代的边界，不会影响验证根的父级布局。此特性使 {@code invalidate()} 方法在遇到验证根时停止使组件层次结构失效。然而，为了保持向后兼容性，这种新的优化行为仅在 {@code java.awt.smartInvalidate} 系统属性值设置为 {@code true} 时启用。
     * <p>
     * 如果组件层次结构中包含验证根且新的优化 {@code invalidate()} 行为已启用，则必须在先前失效的组件的验证根上调用 {@code validate()} 方法以稍后恢复层次结构的有效性。否则，应在顶级容器（如 {@code Frame} 对象）上调用 {@code validate()} 方法以恢复组件层次结构的有效性。
     * <p>
     * AWT 中的 {@code Window} 类和 {@code Applet} 类是验证根。Swing 引入了更多的验证根。
     *
     * @return 此容器是否为验证根
     * @see #invalidate
     * @see java.awt.Component#invalidate
     * @see javax.swing.JComponent#isValidateRoot
     * @see javax.swing.JComponent#revalidate
     * @since 1.7
     */
    public boolean isValidateRoot() {
        return false;
    }

    private static final boolean isJavaAwtSmartInvalidate;
    static {
        // 不要延迟读取，因为每个应用程序都会使用 invalidate()
        isJavaAwtSmartInvalidate = AccessController.doPrivileged(
                new GetBooleanAction("java.awt.smartInvalidate"));
    }

    /**
     * 除非容器是验证根，否则使容器的父级失效。
     */
    @Override
    void invalidateParent() {
        if (!isJavaAwtSmartInvalidate || !isValidateRoot()) {
            super.invalidateParent();
        }
    }

    /**
     * 使容器失效。
     * <p>
     * 如果安装在此容器上的 {@code LayoutManager} 是 {@code LayoutManager2} 接口的实例，则调用其
     * {@link LayoutManager2#invalidateLayout(Container)} 方法，参数为该 {@code Container}。
     * <p>
     * 之后，此方法标记此容器为无效，并使祖先失效。有关更多详细信息，请参阅 {@link Component#invalidate} 方法。
     *
     * @see #validate
     * @see #layout
     * @see LayoutManager2
     */
    @Override
    public void invalidate() {
        LayoutManager layoutMgr = this.layoutMgr;
        if (layoutMgr instanceof LayoutManager2) {
            LayoutManager2 lm = (LayoutManager2) layoutMgr;
            lm.invalidateLayout(this);
        }
        super.invalidate();
    }


                /**
     * 验证此容器及其所有子组件。
     * <p>
     * 验证容器意味着布局其子组件。
     * 与布局相关的更改，例如设置组件的边界，或将组件添加到容器中，会自动使容器失效。
     * 请注意，容器的祖先也可能被无效（有关详细信息，请参阅 {@link Component#invalidate}）。
     * 因此，为了恢复层次结构的有效性，应在层次结构中最高级别的无效容器上调用 {@code
     * validate()} 方法。
     * <p>
     * 验证容器可能是一个相当耗时的操作。出于性能原因，开发人员可能会推迟层次结构的验证，直到完成一组与布局相关的操作，例如在将所有子组件添加到容器后。
     * <p>
     * 如果此 {@code Container} 无效，此方法将调用
     * {@code validateTree} 方法并标记此 {@code Container}
     * 为有效。否则，不执行任何操作。
     *
     * @see #add(java.awt.Component)
     * @see #invalidate
     * @see Container#isValidateRoot
     * @see javax.swing.JComponent#revalidate()
     * @see #validateTree
     */
    public void validate() {
        boolean updateCur = false;
        synchronized (getTreeLock()) {
            if ((!isValid() || descendUnconditionallyWhenValidating)
                    && peer != null)
            {
                ContainerPeer p = null;
                if (peer instanceof ContainerPeer) {
                    p = (ContainerPeer) peer;
                }
                if (p != null) {
                    p.beginValidate();
                }
                validateTree();
                if (p != null) {
                    p.endValidate();
                    // 如果这是内部调用，避免更新光标。
                    // 有关详细信息，请参阅 validateUnconditionally()。
                    if (!descendUnconditionallyWhenValidating) {
                        updateCur = isVisible();
                    }
                }
            }
        }
        if (updateCur) {
            updateCursorImmediately();
        }
    }

    /**
     * 指示有效容器是否也遍历其子组件并调用 validateTree() 方法。
     *
     * 同步：TreeLock。
     *
     * 只要 TreeLock 本身是静态的，该字段就可以是静态的。
     *
     * @see #validateUnconditionally()
     */
    private static boolean descendUnconditionallyWhenValidating = false;

    /**
     * 无条件验证组件层次结构。
     */
    final void validateUnconditionally() {
        boolean updateCur = false;
        synchronized (getTreeLock()) {
            descendUnconditionallyWhenValidating = true;

            validate();
            if (peer instanceof ContainerPeer) {
                updateCur = isVisible();
            }

            descendUnconditionallyWhenValidating = false;
        }
        if (updateCur) {
            updateCursorImmediately();
        }
    }

    /**
     * 递归遍历容器树并重新计算任何标记为需要重新布局的子树（那些标记为无效的）。同步应由调用此方法的方法提供： <code>validate</code>。
     *
     * @see #doLayout
     * @see #validate
     */
    protected void validateTree() {
        checkTreeLock();
        if (!isValid() || descendUnconditionallyWhenValidating) {
            if (peer instanceof ContainerPeer) {
                ((ContainerPeer)peer).beginLayout();
            }
            if (!isValid()) {
                doLayout();
            }
            for (int i = 0; i < component.size(); i++) {
                Component comp = component.get(i);
                if (   (comp instanceof Container)
                       && !(comp instanceof Window)
                       && (!comp.isValid() ||
                           descendUnconditionallyWhenValidating))
                {
                    ((Container)comp).validateTree();
                } else {
                    comp.validate();
                }
            }
            if (peer instanceof ContainerPeer) {
                ((ContainerPeer)peer).endLayout();
            }
        }
        super.validate();
    }

    /**
     * 递归遍历容器树并使所有包含的组件失效。
     */
    void invalidateTree() {
        synchronized (getTreeLock()) {
            for (int i = 0; i < component.size(); i++) {
                Component comp = component.get(i);
                if (comp instanceof Container) {
                    ((Container)comp).invalidateTree();
                }
                else {
                    comp.invalidateIfValid();
                }
            }
            invalidateIfValid();
        }
    }

    /**
     * 设置此容器的字体。
     * <p>
     * 此方法更改与布局相关的信息，因此使组件层次结构失效。
     *
     * @param f 要成为此容器字体的字体。
     * @see Component#getFont
     * @see #invalidate
     * @since JDK1.0
     */
    public void setFont(Font f) {
        boolean shouldinvalidate = false;

        Font oldfont = getFont();
        super.setFont(f);
        Font newfont = getFont();
        if (newfont != oldfont && (oldfont == null ||
                                   !oldfont.equals(newfont))) {
            invalidateTree();
        }
    }

    /**
     * 返回此容器的首选大小。如果未通过 {@link Component#setPreferredSize(Dimension)}
     * 显式设置首选大小，并且此 {@code Container} 有一个 {@code non-null} {@link LayoutManager}，
     * 则使用 {@link LayoutManager#preferredLayoutSize(Container)}
     * 计算首选大小。
     *
     * <p>注意：某些实现可能会缓存从
     * {@code LayoutManager} 返回的值。缓存的实现不必在每次调用此方法时都调用
     * {@code preferredLayoutSize}，而是在 {@code Container} 失效后才查询 {@code LayoutManager}。
     *
     * @return    一个表示此容器首选大小的 <code>Dimension</code> 实例。
     * @see       #getMinimumSize
     * @see       #getMaximumSize
     * @see       #getLayout
     * @see       LayoutManager#preferredLayoutSize(Container)
     * @see       Component#getPreferredSize
     */
    public Dimension getPreferredSize() {
        return preferredSize();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getPreferredSize()</code>。
     */
    @Deprecated
    public Dimension preferredSize() {
        /* 如果有合理的缓存大小值可用，则避免获取锁。
         */
        Dimension dim = prefSize;
        if (dim == null || !(isPreferredSizeSet() || isValid())) {
            synchronized (getTreeLock()) {
                prefSize = (layoutMgr != null) ?
                    layoutMgr.preferredLayoutSize(this) :
                    super.preferredSize();
                dim = prefSize;
            }
        }
        if (dim != null){
            return new Dimension(dim);
        }
        else{
            return dim;
        }
    }

    /**
     * 返回此容器的最小大小。如果未通过 {@link Component#setMinimumSize(Dimension)}
     * 显式设置最小大小，并且此 {@code Container} 有一个 {@code non-null} {@link LayoutManager}，
     * 则使用 {@link LayoutManager#minimumLayoutSize(Container)}
     * 计算最小大小。
     *
     * <p>注意：某些实现可能会缓存从
     * {@code LayoutManager} 返回的值。缓存的实现不必在每次调用此方法时都调用
     * {@code minimumLayoutSize}，而是在 {@code Container} 失效后才查询 {@code LayoutManager}。
     *
     * @return    一个表示此容器最小大小的 <code>Dimension</code> 实例。
     * @see       #getPreferredSize
     * @see       #getMaximumSize
     * @see       #getLayout
     * @see       LayoutManager#minimumLayoutSize(Container)
     * @see       Component#getMinimumSize
     * @since     JDK1.1
     */
    public Dimension getMinimumSize() {
        return minimumSize();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getMinimumSize()</code>。
     */
    @Deprecated
    public Dimension minimumSize() {
        /* 如果有合理的缓存大小值可用，则避免获取锁。
         */
        Dimension dim = minSize;
        if (dim == null || !(isMinimumSizeSet() || isValid())) {
            synchronized (getTreeLock()) {
                minSize = (layoutMgr != null) ?
                    layoutMgr.minimumLayoutSize(this) :
                    super.minimumSize();
                dim = minSize;
            }
        }
        if (dim != null){
            return new Dimension(dim);
        }
        else{
            return dim;
        }
    }

    /**
     * 返回此容器的最大大小。如果未通过 {@link Component#setMaximumSize(Dimension)}
     * 显式设置最大大小，并且安装在此 {@code Container} 上的 {@link LayoutManager}
     * 是 {@link LayoutManager2} 的实例，则使用
     * {@link LayoutManager2#maximumLayoutSize(Container)}
     * 计算最大大小。
     *
     * <p>注意：某些实现可能会缓存从
     * {@code LayoutManager2} 返回的值。缓存的实现不必在每次调用此方法时都调用
     * {@code maximumLayoutSize}，而是在 {@code Container} 失效后才查询 {@code LayoutManager2}。
     *
     * @return    一个表示此容器最大大小的 <code>Dimension</code> 实例。
     * @see       #getPreferredSize
     * @see       #getMinimumSize
     * @see       #getLayout
     * @see       LayoutManager2#maximumLayoutSize(Container)
     * @see       Component#getMaximumSize
     */
    public Dimension getMaximumSize() {
        /* 如果有合理的缓存大小值可用，则避免获取锁。
         */
        Dimension dim = maxSize;
        if (dim == null || !(isMaximumSizeSet() || isValid())) {
            synchronized (getTreeLock()) {
               if (layoutMgr instanceof LayoutManager2) {
                    LayoutManager2 lm = (LayoutManager2) layoutMgr;
                    maxSize = lm.maximumLayoutSize(this);
               } else {
                    maxSize = super.getMaximumSize();
               }
               dim = maxSize;
            }
        }
        if (dim != null){
            return new Dimension(dim);
        }
        else{
            return dim;
        }
    }

    /**
     * 返回 x 轴上的对齐方式。这指定了组件相对于其他组件的对齐方式。值应在 0 到 1 之间
     * 其中 0 表示对齐到原点，1 表示对齐到离原点最远的位置，0.5 表示居中，等等。
     */
    public float getAlignmentX() {
        float xAlign;
        if (layoutMgr instanceof LayoutManager2) {
            synchronized (getTreeLock()) {
                LayoutManager2 lm = (LayoutManager2) layoutMgr;
                xAlign = lm.getLayoutAlignmentX(this);
            }
        } else {
            xAlign = super.getAlignmentX();
        }
        return xAlign;
    }

    /**
     * 返回 y 轴上的对齐方式。这指定了组件相对于其他组件的对齐方式。值应在 0 到 1 之间
     * 其中 0 表示对齐到原点，1 表示对齐到离原点最远的位置，0.5 表示居中，等等。
     */
    public float getAlignmentY() {
        float yAlign;
        if (layoutMgr instanceof LayoutManager2) {
            synchronized (getTreeLock()) {
                LayoutManager2 lm = (LayoutManager2) layoutMgr;
                yAlign = lm.getLayoutAlignmentY(this);
            }
        } else {
            yAlign = super.getAlignmentY();
        }
        return yAlign;
    }

    /**
     * 绘制容器。这将绘制转发给此容器的任何轻量级子组件。如果重新实现此方法，应调用 super.paint(g) 以确保轻量级组件正确渲染。如果子组件完全被 g 中的当前裁剪设置裁剪，paint() 将不会转发给该子组件。
     *
     * @param g 指定的 Graphics 窗口
     * @see   Component#update(Graphics)
     */
    public void paint(Graphics g) {
        if (isShowing()) {
            synchronized (getObjectLock()) {
                if (printing) {
                    if (printingThreads.contains(Thread.currentThread())) {
                        return;
                    }
                }
            }

            // 容器在屏幕上显示，并且
            // 此 paint() 不是从 print() 调用的。
            // 绘制自身并转发绘制到轻量级子组件。

            // super.paint(); -- 不必调用，因为它是一个空操作。

            GraphicsCallback.PaintCallback.getInstance().
                runComponents(getComponentsSync(), g, GraphicsCallback.LIGHTWEIGHTS);
        }
    }

    /**
     * 更新容器。这将更新转发给此容器的任何轻量级子组件。如果重新实现此方法，应调用 super.update(g) 以确保轻量级组件正确渲染。如果子组件完全被 g 中的当前裁剪设置裁剪，update() 将不会转发给该子组件。
     *
     * @param g 指定的 Graphics 窗口
     * @see   Component#update(Graphics)
     */
    public void update(Graphics g) {
        if (isShowing()) {
            if (! (peer instanceof LightweightPeer)) {
                g.clearRect(0, 0, width, height);
            }
            paint(g);
        }
    }

    /**
     * 打印容器。这将打印转发给此容器的任何轻量级子组件。如果重新实现此方法，应调用 super.print(g) 以确保轻量级组件正确渲染。如果子组件完全被 g 中的当前裁剪设置裁剪，print() 将不会转发给该子组件。
     *
     * @param g 指定的 Graphics 窗口
     * @see   Component#update(Graphics)
     */
    public void print(Graphics g) {
        if (isShowing()) {
            Thread t = Thread.currentThread();
            try {
                synchronized (getObjectLock()) {
                    if (printingThreads == null) {
                        printingThreads = new HashSet<>();
                    }
                    printingThreads.add(t);
                    printing = true;
                }
                super.print(g);  // 默认情况下，Component.print() 调用 paint()
            } finally {
                synchronized (getObjectLock()) {
                    printingThreads.remove(t);
                    printing = !printingThreads.isEmpty();
                }
            }


                        GraphicsCallback.PrintCallback.getInstance().
                runComponents(getComponentsSync(), g, GraphicsCallback.LIGHTWEIGHTS);
        }
    }

    /**
     * 绘制此容器中的每个组件。
     * @param     g   图形上下文。
     * @see       Component#paint
     * @see       Component#paintAll
     */
    public void paintComponents(Graphics g) {
        if (isShowing()) {
            GraphicsCallback.PaintAllCallback.getInstance().
                runComponents(getComponentsSync(), g, GraphicsCallback.TWO_PASSES);
        }
    }

    /**
     * 模拟轻量级容器的打印回调。
     * @param     g   用于打印的图形上下文。
     * @see       Component#printAll
     * @see       #printComponents
     */
    void lightweightPaint(Graphics g) {
        super.lightweightPaint(g);
        paintHeavyweightComponents(g);
    }

    /**
     * 绘制所有重量级子组件。
     */
    void paintHeavyweightComponents(Graphics g) {
        if (isShowing()) {
            GraphicsCallback.PaintHeavyweightComponentsCallback.getInstance().
                runComponents(getComponentsSync(), g,
                              GraphicsCallback.LIGHTWEIGHTS | GraphicsCallback.HEAVYWEIGHTS);
        }
    }

    /**
     * 打印此容器中的每个组件。
     * @param     g   图形上下文。
     * @see       Component#print
     * @see       Component#printAll
     */
    public void printComponents(Graphics g) {
        if (isShowing()) {
            GraphicsCallback.PrintAllCallback.getInstance().
                runComponents(getComponentsSync(), g, GraphicsCallback.TWO_PASSES);
        }
    }

    /**
     * 模拟轻量级容器的打印回调。
     * @param     g   用于打印的图形上下文。
     * @see       Component#printAll
     * @see       #printComponents
     */
    void lightweightPrint(Graphics g) {
        super.lightweightPrint(g);
        printHeavyweightComponents(g);
    }

    /**
     * 打印所有重量级子组件。
     */
    void printHeavyweightComponents(Graphics g) {
        if (isShowing()) {
            GraphicsCallback.PrintHeavyweightComponentsCallback.getInstance().
                runComponents(getComponentsSync(), g,
                              GraphicsCallback.LIGHTWEIGHTS | GraphicsCallback.HEAVYWEIGHTS);
        }
    }

    /**
     * 添加指定的容器监听器以接收来自此容器的容器事件。
     * 如果 l 为 null，则不会抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l 容器监听器
     *
     * @see #removeContainerListener
     * @see #getContainerListeners
     */
    public synchronized void addContainerListener(ContainerListener l) {
        if (l == null) {
            return;
        }
        containerListener = AWTEventMulticaster.add(containerListener, l);
        newEventsOnly = true;
    }

    /**
     * 移除指定的容器监听器，使其不再接收来自此容器的容器事件。
     * 如果 l 为 null，则不会抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param   l 容器监听器
     *
     * @see #addContainerListener
     * @see #getContainerListeners
     */
    public synchronized void removeContainerListener(ContainerListener l) {
        if (l == null) {
            return;
        }
        containerListener = AWTEventMulticaster.remove(containerListener, l);
    }

    /**
     * 返回注册到此容器的所有容器监听器的数组。
     *
     * @return 此容器的所有 <code>ContainerListener</code>，如果没有注册容器监听器，则返回空数组
     *
     * @see #addContainerListener
     * @see #removeContainerListener
     * @since 1.4
     */
    public synchronized ContainerListener[] getContainerListeners() {
        return getListeners(ContainerListener.class);
    }

    /**
     * 返回当前注册为 <code><em>Foo</em>Listener</code> 的所有对象的数组。
     * <code><em>Foo</em>Listener</code> 是使用 <code>add<em>Foo</em>Listener</code> 方法注册的。
     *
     * <p>
     * 可以使用类字面量指定 <code>listenerType</code> 参数，例如
     * <code><em>Foo</em>Listener.class</code>。
     * 例如，可以使用以下代码查询 <code>Container</code> <code>c</code>
     * 的容器监听器：
     *
     * <pre>ContainerListener[] cls = (ContainerListener[])(c.getListeners(ContainerListener.class));</pre>
     *
     * 如果没有这样的监听器存在，此方法返回空数组。
     *
     * @param listenerType 请求的监听器类型；此参数应指定一个继承自
     *          <code>java.util.EventListener</code> 的接口
     * @return 注册为 <code><em>Foo</em>Listener</code> 的所有对象的数组，
     *          如果没有添加这样的监听器，则返回空数组
     * @exception ClassCastException 如果 <code>listenerType</code>
     *          不指定一个实现 <code>java.util.EventListener</code> 的类或接口
     * @exception NullPointerException 如果 {@code listenerType} 为 {@code null}
     *
     * @see #getContainerListeners
     *
     * @since 1.3
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if  (listenerType == ContainerListener.class) {
            l = containerListener;
        } else {
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l, listenerType);
    }

    // REMIND: remove when filtering is done at lower level
    boolean eventEnabled(AWTEvent e) {
        int id = e.getID();

        if (id == ContainerEvent.COMPONENT_ADDED ||
            id == ContainerEvent.COMPONENT_REMOVED) {
            if ((eventMask & AWTEvent.CONTAINER_EVENT_MASK) != 0 ||
                containerListener != null) {
                return true;
            }
            return false;
        }
        return super.eventEnabled(e);
    }

    /**
     * 处理此容器上的事件。如果事件是 <code>ContainerEvent</code>，则调用
     * <code>processContainerEvent</code> 方法，否则调用其超类的 <code>processEvent</code> 方法。
     * <p>注意，如果事件参数为 <code>null</code>，则行为未指定，可能会导致异常。
     *
     * @param e 事件
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof ContainerEvent) {
            processContainerEvent((ContainerEvent)e);
            return;
        }
        super.processEvent(e);
    }

    /**
     * 通过分派给任何已注册的 ContainerListener 对象来处理此容器上的容器事件。
     * 注意：除非为此组件启用了容器事件，否则此方法不会被调用；这发生在以下情况之一：
     * <ul>
     * <li>通过 <code>addContainerListener</code> 注册了 ContainerListener 对象
     * <li>通过 <code>enableEvents</code> 启用了容器事件
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>，则行为未指定，可能会导致异常。
     *
     * @param e 容器事件
     * @see Component#enableEvents
     */
    protected void processContainerEvent(ContainerEvent e) {
        ContainerListener listener = containerListener;
        if (listener != null) {
            switch(e.getID()) {
              case ContainerEvent.COMPONENT_ADDED:
                listener.componentAdded(e);
                break;
              case ContainerEvent.COMPONENT_REMOVED:
                listener.componentRemoved(e);
                break;
            }
        }
    }

    /*
     * 将事件分派给此组件或其子组件之一。
     * 在响应 COMPONENT_RESIZED 和 COMPONENT_MOVED 事件时创建 ANCESTOR_RESIZED 和 ANCESTOR_MOVED 事件。
     * 必须在这里而不是在 processComponentEvent 中进行此操作，因为 ComponentEvents 可能未为此 Container 启用。
     * @param e 事件
     */
    void dispatchEventImpl(AWTEvent e) {
        if ((dispatcher != null) && dispatcher.dispatchEvent(e)) {
            // 事件已发送到轻量级组件。原生容器上的原生生成事件必须由对等体正确处理，因此它被转发。
            // 如果由于发送轻量级事件而移除了原生主机，则对等体引用将为 null。
            e.consume();
            if (peer != null) {
                peer.handleEvent(e);
            }
            return;
        }

        super.dispatchEventImpl(e);

        synchronized (getTreeLock()) {
            switch (e.getID()) {
              case ComponentEvent.COMPONENT_RESIZED:
                createChildHierarchyEvents(HierarchyEvent.ANCESTOR_RESIZED, 0,
                                           Toolkit.enabledOnToolkit(AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK));
                break;
              case ComponentEvent.COMPONENT_MOVED:
                createChildHierarchyEvents(HierarchyEvent.ANCESTOR_MOVED, 0,
                                       Toolkit.enabledOnToolkit(AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK));
                break;
              default:
                break;
            }
        }
    }

    /*
     * 将事件分派给此组件，而不尝试将其转发给任何子组件
     * @param e 事件
     */
    void dispatchEventToSelf(AWTEvent e) {
        super.dispatchEventImpl(e);
    }

    /**
     * 获取最顶层（最深）的轻量级组件，该组件对接收鼠标事件感兴趣。
     */
    Component getMouseEventTarget(int x, int y, boolean includeSelf) {
        return getMouseEventTarget(x, y, includeSelf,
                                   MouseEventTargetFilter.FILTER,
                                   !SEARCH_HEAVYWEIGHTS);
    }

    /**
     * 获取最顶层（最深）的组件以接收 SunDropTargetEvents。
     */
    Component getDropTargetEventTarget(int x, int y, boolean includeSelf) {
        return getMouseEventTarget(x, y, includeSelf,
                                   DropTargetEventTargetFilter.FILTER,
                                   SEARCH_HEAVYWEIGHTS);
    }

    /**
     * getMouseEventTarget 的私有版本，具有两个额外的可控行为。此方法搜索包含给定坐标的此容器的最顶层
     * 子代，并且该子代被给定的过滤器接受。如果最后一个参数为 <code>false</code>，则搜索将限制在轻量级子代。
     *
     * @param filter 用于确定给定组件是否是此事件的有效目标的 EventTargetFilter 实例。
     * @param searchHeavyweights 如果为 <code>false</code>，则方法将在搜索期间跳过重量级组件。
     */
    private Component getMouseEventTarget(int x, int y, boolean includeSelf,
                                          EventTargetFilter filter,
                                          boolean searchHeavyweights) {
        Component comp = null;
        if (searchHeavyweights) {
            comp = getMouseEventTargetImpl(x, y, includeSelf, filter,
                                           SEARCH_HEAVYWEIGHTS,
                                           searchHeavyweights);
        }

        if (comp == null || comp == this) {
            comp = getMouseEventTargetImpl(x, y, includeSelf, filter,
                                           !SEARCH_HEAVYWEIGHTS,
                                           searchHeavyweights);
        }

        return comp;
    }

    /**
     * getMouseEventTarget 的私有版本，具有三个额外的可控行为。此方法搜索包含给定坐标的此容器的最顶层
     * 子代，并且该子代被给定的过滤器接受。搜索将限制在仅轻量级子代或仅重量级子代，具体取决于 searchHeavyweightChildren。
     * 如果 searchHeavyweightDescendants 为 <code>false</code>，则搜索将限制在仅轻量级子代。
     *
     * @param filter 用于确定选定组件是否是此事件的有效目标的 EventTargetFilter 实例。
     * @param searchHeavyweightChildren 如果为 <code>true</code>，则方法将在搜索期间跳过直接的轻量级子代。
     *        如果为 <code>false</code>，则方法将在搜索期间跳过直接的重量级子代。
     * @param searchHeavyweightDescendants 如果为 <code>false</code>，则方法将在搜索期间跳过非直接的重量级子代。
     *        如果为 <code>true</code>，则方法将在搜索期间遍历轻量级和重量级子代。
     */
    private Component getMouseEventTargetImpl(int x, int y, boolean includeSelf,
                                         EventTargetFilter filter,
                                         boolean searchHeavyweightChildren,
                                         boolean searchHeavyweightDescendants) {
        synchronized (getTreeLock()) {

            for (int i = 0; i < component.size(); i++) {
                Component comp = component.get(i);
                if (comp != null && comp.visible &&
                    ((!searchHeavyweightChildren &&
                      comp.peer instanceof LightweightPeer) ||
                     (searchHeavyweightChildren &&
                      !(comp.peer instanceof LightweightPeer))) &&
                    comp.contains(x - comp.x, y - comp.y)) {

                    // 找到一个与点相交的组件，看看是否有更深的可能性。
                    if (comp instanceof Container) {
                        Container child = (Container) comp;
                        Component deeper = child.getMouseEventTarget(
                                x - child.x,
                                y - child.y,
                                includeSelf,
                                filter,
                                searchHeavyweightDescendants);
                        if (deeper != null) {
                            return deeper;
                        }
                    } else {
                        if (filter.accept(comp)) {
                            // 没有更深的目标，但此组件是目标
                            return comp;
                        }
                    }
                }
            }


                        boolean isPeerOK;
            boolean isMouseOverMe;

            isPeerOK = (peer instanceof LightweightPeer) || includeSelf;
            isMouseOverMe = contains(x,y);

            // 没有找到子目标，如果这是一个可能的目标则返回此组件
            if (isMouseOverMe && isPeerOK && filter.accept(this)) {
                return this;
            }
            // 没有可能的目标
            return null;
        }
    }

    static interface EventTargetFilter {
        boolean accept(final Component comp);
    }

    static class MouseEventTargetFilter implements EventTargetFilter {
        static final EventTargetFilter FILTER = new MouseEventTargetFilter();

        private MouseEventTargetFilter() {}

        public boolean accept(final Component comp) {
            return (comp.eventMask & AWTEvent.MOUSE_MOTION_EVENT_MASK) != 0
                || (comp.eventMask & AWTEvent.MOUSE_EVENT_MASK) != 0
                || (comp.eventMask & AWTEvent.MOUSE_WHEEL_EVENT_MASK) != 0
                || comp.mouseListener != null
                || comp.mouseMotionListener != null
                || comp.mouseWheelListener != null;
        }
    }

    static class DropTargetEventTargetFilter implements EventTargetFilter {
        static final EventTargetFilter FILTER = new DropTargetEventTargetFilter();

        private DropTargetEventTargetFilter() {}

        public boolean accept(final Component comp) {
            DropTarget dt = comp.getDropTarget();
            return dt != null && dt.isActive();
        }
    }

    /**
     * 这是由希望包含窗口父级启用某些事件的轻量级组件调用的。
     * 这对于通常只分派给窗口的事件是必要的，以便它们可以被转发到
     * 已启用这些事件的轻量级组件。
     */
    void proxyEnableEvents(long events) {
        if (peer instanceof LightweightPeer) {
            // 该容器是轻量级的....继续向上发送
            if (parent != null) {
                parent.proxyEnableEvents(events);
            }
        } else {
            // 这是一个原生容器，因此它需要托管其子级。如果在创建对等项之前调用此函数，
            // 我们还没有分发器，因为尚未确定此实例是否为轻量级。
            if (dispatcher != null) {
                dispatcher.enableEvents(events);
            }
        }
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>dispatchEvent(AWTEvent e)</code>
     */
    @Deprecated
    public void deliverEvent(Event e) {
        Component comp = getComponentAt(e.x, e.y);
        if ((comp != null) && (comp != this)) {
            e.translate(-comp.x, -comp.y);
            comp.deliverEvent(e);
        } else {
            postEvent(e);
        }
    }

    /**
     * 定位包含 x,y 位置的组件。如果组件之间有重叠，则返回最顶层的子组件。
     * 这是通过查找声明包含给定点的组件来确定的，但具有原生对等项的组件优先于没有原生对等项的组件（即轻量级组件）。
     *
     * @param x x 坐标
     * @param y y 坐标
     * @return 如果组件不包含该位置，则返回 null。如果没有子组件位于请求的点，并且该点在容器的边界内，则返回容器本身；否则返回最顶层的子组件。
     * @see Component#contains
     * @since JDK1.1
     */
    public Component getComponentAt(int x, int y) {
        return locate(x, y);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getComponentAt(int, int)</code>。
     */
    @Deprecated
    public Component locate(int x, int y) {
        if (!contains(x, y)) {
            return null;
        }
        Component lightweight = null;
        synchronized (getTreeLock()) {
            // 优化的两遍版本：
            // 请参阅 sun.awt.SunGraphicsCallback 中的注释
            for (final Component comp : component) {
                if (comp.contains(x - comp.x, y - comp.y)) {
                    if (!comp.isLightweight()) {
                        // 尽快返回原生组件
                        return comp;
                    }
                    if (lightweight == null) {
                        // 保存并稍后返回第一个轻量级组件
                        lightweight = comp;
                    }
                }
            }
        }
        return lightweight != null ? lightweight : this;
    }

    /**
     * 获取包含指定点的组件。
     * @param      p   点。
     * @return     返回包含该点的组件，或 <code>null</code> 如果组件不包含该点。
     * @see        Component#contains
     * @since      JDK1.1
     */
    public Component getComponentAt(Point p) {
        return getComponentAt(p.x, p.y);
    }

    /**
     * 如果容器在鼠标指针下方，则返回鼠标指针在该容器坐标空间中的位置，否则返回 <code>null</code>。
     * 此方法类似于 {@link Component#getMousePosition()}，但可以考虑容器的子级。
     * 如果 <code>allowChildren</code> 为 <code>false</code>，此方法仅在鼠标指针直接位于容器上方时返回非 null 值，不考虑被子级遮挡的部分。
     * 如果 <code>allowChildren</code> 为 <code>true</code>，此方法在鼠标指针位于容器或其任何后代上方时返回非 null 值。
     *
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
     * @param     allowChildren 如果应考虑子级，则为 true
     * @see       Component#getMousePosition
     * @return    相对于此 <code>Component</code> 的鼠标坐标，或 null
     * @since     1.5
     */
    public Point getMousePosition(boolean allowChildren) throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        PointerInfo pi = java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<PointerInfo>() {
                public PointerInfo run() {
                    return MouseInfo.getPointerInfo();
                }
            }
        );
        synchronized (getTreeLock()) {
            Component inTheSameWindow = findUnderMouseInWindow(pi);
            if (isSameOrAncestorOf(inTheSameWindow, allowChildren)) {
                return  pointRelativeToComponent(pi.getLocation());
            }
            return null;
        }
    }

    boolean isSameOrAncestorOf(Component comp, boolean allowChildren) {
        return this == comp || (allowChildren && isParentOf(comp));
    }

    /**
     * 定位包含指定位置的可见子组件。如果组件之间有重叠，则返回最顶层的子组件。
     * 如果包含的子组件是 Container，则此方法将继续搜索最深层的嵌套子组件。搜索期间忽略不可见的组件。<p>
     *
     * findComponentAt 方法与 getComponentAt 不同之处在于 getComponentAt 仅搜索容器的直接子级；
     * 如果包含的组件是 Container，则 findComponentAt 将搜索该子级以查找嵌套的组件。
     *
     * @param x x 坐标
     * @param y y 坐标
     * @return 如果组件不包含该位置，则返回 null。如果没有子组件位于请求的点，并且该点在容器的边界内，则返回容器本身。
     * @see Component#contains
     * @see #getComponentAt
     * @since 1.2
     */
    public Component findComponentAt(int x, int y) {
        return findComponentAt(x, y, true);
    }

    /**
     * findComponentAt 的私有版本，具有可控的行为。将 'ignoreEnabled' 设置为 'false' 可以在搜索过程中跳过禁用的组件。
     * 此行为由 sun.awt.GlobalCursorManager 中的轻量级光标支持使用。
     *
     * 添加此功能是临时的，等待采用新的公共 API 导出此功能。
     */
    final Component findComponentAt(int x, int y, boolean ignoreEnabled) {
        synchronized (getTreeLock()) {
            if (isRecursivelyVisible()){
                return findComponentAtImpl(x, y, ignoreEnabled);
            }
        }
        return null;
    }

    final Component findComponentAtImpl(int x, int y, boolean ignoreEnabled) {
        // checkTreeLock(); 为性能原因注释掉

        if (!(contains(x, y) && visible && (ignoreEnabled || enabled))) {
            return null;
        }
        Component lightweight = null;
        // 优化的两遍版本：
        // 请参阅 sun.awt.SunGraphicsCallback 中的注释
        for (final Component comp : component) {
            final int x1 = x - comp.x;
            final int y1 = y - comp.y;
            if (!comp.contains(x1, y1)) {
                continue; // 快速路径
            }
            if (!comp.isLightweight()) {
                final Component child = getChildAt(comp, x1, y1, ignoreEnabled);
                if (child != null) {
                    // 尽快返回原生组件
                    return child;
                }
            } else {
                if (lightweight == null) {
                    // 保存并稍后返回第一个轻量级组件
                    lightweight = getChildAt(comp, x1, y1, ignoreEnabled);
                }
            }
        }
        return lightweight != null ? lightweight : this;
    }

    /**
     * findComponentAtImpl 的辅助方法。使用 findComponentAtImpl 为 Container 查找子组件，使用 getComponentAt 为 Component 查找子组件。
     */
    private static Component getChildAt(Component comp, int x, int y,
                                        boolean ignoreEnabled) {
        if (comp instanceof Container) {
            comp = ((Container) comp).findComponentAtImpl(x, y,
                                                          ignoreEnabled);
        } else {
            comp = comp.getComponentAt(x, y);
        }
        if (comp != null && comp.visible &&
                (ignoreEnabled || comp.enabled)) {
            return comp;
        }
        return null;
    }

    /**
     * 定位包含指定点的可见子组件。如果组件之间有重叠，则返回最顶层的子组件。
     * 如果包含的子组件是 Container，则此方法将继续搜索最深层的嵌套子组件。搜索期间忽略不可见的组件。<p>
     *
     * findComponentAt 方法与 getComponentAt 不同之处在于 getComponentAt 仅搜索容器的直接子级；
     * 如果包含的组件是 Container，则 findComponentAt 将搜索该子级以查找嵌套的组件。
     *
     * @param      p   点。
     * @return null 如果组件不包含该位置。如果没有子组件位于请求的点，并且该点在容器的边界内，则返回容器本身。
     * @throws NullPointerException 如果 {@code p} 为 {@code null}
     * @see Component#contains
     * @see #getComponentAt
     * @since 1.2
     */
    public Component findComponentAt(Point p) {
        return findComponentAt(p.x, p.y);
    }

    /**
     * 通过连接到原生屏幕资源使此容器可显示。使容器可显示将导致其所有子级也被设为可显示。
     * 此方法由工具包内部调用，不应由程序直接调用。
     * @see Component#isDisplayable
     * @see #removeNotify
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            // 在子级上调用 addNotify() 可能会导致在此实例上启用代理事件，因此我们首先调用 super.addNotify() 并
            // 可能创建一个轻量级事件分发器，然后再在可能是轻量级的子级上调用 addNotify()。
            super.addNotify();
            if (! (peer instanceof LightweightPeer)) {
                dispatcher = new LightweightDispatcher(this);
            }

            // 我们不应该使用迭代器，因为 Swing 菜单实现的特殊性：
            // 菜单被分配为 JLayeredPane 的子级，而不是特定的组件，因此在菜单显示或隐藏时总是会影响组件集合。
            for (int i = 0; i < component.size(); i++) {
                component.get(i).addNotify();
            }
        }
    }

    /**
     * 通过断开与原生屏幕资源的连接使此容器不可显示。使容器不可显示将导致其所有子级也被设为不可显示。
     * 此方法由工具包内部调用，不应由程序直接调用。
     * @see Component#isDisplayable
     * @see #addNotify
     */
    public void removeNotify() {
        synchronized (getTreeLock()) {
            // 我们不应该使用迭代器，因为 Swing 菜单实现的特殊性：
            // 菜单被分配为 JLayeredPane 的子级，而不是特定的组件，因此在菜单显示或隐藏时总是会影响组件集合。
            for (int i = component.size()-1 ; i >= 0 ; i--) {
                Component comp = component.get(i);
                if (comp != null) {
                    // 修复 6607170。
                    // 我们希望在处置获得焦点的组件时抑制焦点更改。但由于焦点是异步的，我们应该在处置过程中抑制每个组件的焦点更改
                    // 以防它在处置过程中接收到原生焦点。
                    comp.setAutoFocusTransferOnDisposal(false);
                    comp.removeNotify();
                    comp.setAutoFocusTransferOnDisposal(true);
                 }
             }
            // 如果某些子级在处置前有焦点，则它仍然有焦点。
            // 如果启用了自动转移焦点，则自动将焦点转移到下一个（或上一个）组件。
            if (containsFocus() && KeyboardFocusManager.isAutoFocusTransferEnabledFor(this)) {
                if (!transferFocus(false)) {
                    transferFocusBackward(true);
                }
            }
            if ( dispatcher != null ) {
                dispatcher.dispose();
                dispatcher = null;
            }
            super.removeNotify();
        }
    }


                /**
     * 检查组件是否包含在该容器的组件层次结构中。
     * @param c 组件
     * @return     <code>true</code> 如果它是祖先；
     *             <code>false</code> 否则。
     * @since      JDK1.1
     */
    public boolean isAncestorOf(Component c) {
        Container p;
        if (c == null || ((p = c.getParent()) == null)) {
            return false;
        }
        while (p != null) {
            if (p == this) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    /*
     * 以下代码是为了支持模态 JInternalFrames 而添加的
     * 不幸的是，这段代码必须添加在这里，以便我们可以访问一些私有的 AWT 类，如 SequencedEvent。
     *
     * 轻量级组件的本机容器设置此字段
     * 以告知它应该阻止所有轻量级子组件的鼠标事件，除了模态组件。
     *
     * 在嵌套的模态组件的情况下，我们将前一个
     * 模态组件存储在新模态组件的 modalComp 值中；
     */

    transient Component modalComp;
    transient AppContext modalAppContext;

    private void startLWModal() {
        // 存储此组件显示的应用程序上下文。
        // 该应用程序上下文的事件调度线程将一直休眠，直到
        // 我们通过 hideAndDisposeHandler() 中的任何事件将其唤醒。
        modalAppContext = AppContext.getAppContext();

        // 在焦点转移之前阻止键盘事件的分发
        long time = Toolkit.getEventQueue().getMostRecentKeyEventTime();
        Component predictedFocusOwner = (Component.isInstanceOf(this, "javax.swing.JInternalFrame")) ? ((javax.swing.JInternalFrame)(this)).getMostRecentFocusOwner() : null;
        if (predictedFocusOwner != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().
                enqueueKeyEvents(time, predictedFocusOwner);
        }
        // 我们有两种阻止机制：1. 如果我们在 EventDispatchThread 上，启动新的事件泵。2. 如果我们在任何其他线程上，调用 treelock 的 wait()。
        final Container nativeContainer;
        synchronized (getTreeLock()) {
            nativeContainer = getHeavyweightContainer();
            if (nativeContainer.modalComp != null) {
                this.modalComp =  nativeContainer.modalComp;
                nativeContainer.modalComp = this;
                return;
            }
            else {
                nativeContainer.modalComp = this;
            }
        }

        Runnable pumpEventsForHierarchy = new Runnable() {
            public void run() {
                EventDispatchThread dispatchThread =
                    (EventDispatchThread)Thread.currentThread();
                dispatchThread.pumpEventsForHierarchy(
                        new Conditional() {
                        public boolean evaluate() {
                        return ((windowClosingException == null) && (nativeContainer.modalComp != null)) ;
                        }
                        }, Container.this);
            }
        };

        if (EventQueue.isDispatchThread()) {
            SequencedEvent currentSequencedEvent =
                KeyboardFocusManager.getCurrentKeyboardFocusManager().
                getCurrentSequencedEvent();
            if (currentSequencedEvent != null) {
                currentSequencedEvent.dispose();
            }

            pumpEventsForHierarchy.run();
        } else {
            synchronized (getTreeLock()) {
                Toolkit.getEventQueue().
                    postEvent(new PeerEvent(this,
                                pumpEventsForHierarchy,
                                PeerEvent.PRIORITY_EVENT));
                while ((windowClosingException == null) &&
                       (nativeContainer.modalComp != null))
                {
                    try {
                        getTreeLock().wait();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }
        if (windowClosingException != null) {
            windowClosingException.fillInStackTrace();
            throw windowClosingException;
        }
        if (predictedFocusOwner != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().
                dequeueKeyEvents(time, predictedFocusOwner);
        }
    }

    private void stopLWModal() {
        synchronized (getTreeLock()) {
            if (modalAppContext != null) {
                Container nativeContainer = getHeavyweightContainer();
                if(nativeContainer != null) {
                    if (this.modalComp !=  null) {
                        nativeContainer.modalComp = this.modalComp;
                        this.modalComp = null;
                        return;
                    }
                    else {
                        nativeContainer.modalComp = null;
                    }
                }
                // 唤醒最初显示对话框的事件调度线程
                SunToolkit.postEvent(modalAppContext,
                        new PeerEvent(this,
                                new WakingRunnable(),
                                PeerEvent.PRIORITY_EVENT));
            }
            EventQueue.invokeLater(new WakingRunnable());
            getTreeLock().notifyAll();
        }
    }

    final static class WakingRunnable implements Runnable {
        public void run() {
        }
    }

    /* End of JOptionPane support code */

    /**
     * 返回表示此 <code>Container</code> 状态的字符串。
     * 此方法仅用于调试目的，返回字符串的内容和格式可能因实现而异。返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return    该容器的参数字符串
     */
    protected String paramString() {
        String str = super.paramString();
        LayoutManager layoutMgr = this.layoutMgr;
        if (layoutMgr != null) {
            str += ",layout=" + layoutMgr.getClass().getName();
        }
        return str;
    }

    /**
     * 将此容器的列表打印到指定的输出流。列表从指定的缩进开始。
     * <p>
     * 容器的直接子组件以 <code>indent+1</code> 的缩进打印。这些子组件的子组件以 <code>indent+2</code> 的缩进打印，依此类推。
     *
     * @param    out      打印流
     * @param    indent   缩进的空格数
     * @throws   NullPointerException 如果 {@code out} 为 {@code null}
     * @see      Component#list(java.io.PrintStream, int)
     * @since    JDK1.0
     */
    public void list(PrintStream out, int indent) {
        super.list(out, indent);
        synchronized(getTreeLock()) {
            for (int i = 0; i < component.size(); i++) {
                Component comp = component.get(i);
                if (comp != null) {
                    comp.list(out, indent+1);
                }
            }
        }
    }

    /**
     * 将列表打印到指定的打印写入器，从指定的缩进开始。
     * <p>
     * 容器的直接子组件以 <code>indent+1</code> 的缩进打印。这些子组件的子组件以 <code>indent+2</code> 的缩进打印，依此类推。
     *
     * @param    out      打印写入器
     * @param    indent   缩进的空格数
     * @throws   NullPointerException 如果 {@code out} 为 {@code null}
     * @see      Component#list(java.io.PrintWriter, int)
     * @since    JDK1.1
     */
    public void list(PrintWriter out, int indent) {
        super.list(out, indent);
        synchronized(getTreeLock()) {
            for (int i = 0; i < component.size(); i++) {
                Component comp = component.get(i);
                if (comp != null) {
                    comp.list(out, indent+1);
                }
            }
        }
    }

    /**
     * 为给定的遍历操作设置此容器的焦点遍历键。
     * <p>
     * 容器的焦点遍历键的默认值因实现而异。Sun 建议特定本机平台的所有实现使用相同的默认值。Windows 和 Unix 的推荐默认值如下。这些推荐值在 Sun AWT 实现中使用。
     *
     * <table border=1 summary="Recommended default values for a Container's focus traversal keys">
     * <tr>
     *    <th>标识符</th>
     *    <th>含义</th>
     *    <th>默认值</th>
     * </tr>
     * <tr>
     *    <td>KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS</td>
     *    <td>正常的前向键盘遍历</td>
     *    <td>TAB on KEY_PRESSED, CTRL-TAB on KEY_PRESSED</td>
     * </tr>
     * <tr>
     *    <td>KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS</td>
     *    <td>正常的反向键盘遍历</td>
     *    <td>SHIFT-TAB on KEY_PRESSED, CTRL-SHIFT-TAB on KEY_PRESSED</td>
     * </tr>
     * <tr>
     *    <td>KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS</td>
     *    <td>上移一个焦点遍历周期</td>
     *    <td>无</td>
     * </tr>
     * <tr>
     *    <td>KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS<td>
     *    <td>下移一个焦点遍历周期</td>
     *    <td>无</td>
     * </tr>
     * </table>
     *
     * 要禁用遍历键，可以使用空集；推荐使用 Collections.EMPTY_SET。
     * <p>
     * 使用 AWTKeyStroke API，客户端代码可以指定焦点遍历操作将在两个特定的 KeyEvent 之一（KEY_PRESSED 或 KEY_RELEASED）上发生。然而，无论指定了哪个 KeyEvent，与焦点遍历键相关的所有 KeyEvent，包括关联的 KEY_TYPED 事件，都将被消耗，不会分发到任何容器。将 KEY_TYPED 事件指定为映射到焦点遍历操作，或映射同一事件到多个默认焦点遍历操作，都是运行时错误。
     * <p>
     * 如果为 Set 指定了 null 值，此容器将继承其父容器的 Set。如果此容器的所有祖先都为 Set 指定了 null，则使用当前 KeyboardFocusManager 的默认 Set。
     * <p>
     * 如果 keystrokes 中的任何 Object 不是 AWTKeyStroke，此方法可能会抛出 {@code ClassCastException}。
     *
     * @param id 以下之一：KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS、
     *        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS、
     *        KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS 或
     *        KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS
     * @param keystrokes 指定操作的 AWTKeyStroke 集合
     * @see #getFocusTraversalKeys
     * @see KeyboardFocusManager#FORWARD_TRAVERSAL_KEYS
     * @see KeyboardFocusManager#BACKWARD_TRAVERSAL_KEYS
     * @see KeyboardFocusManager#UP_CYCLE_TRAVERSAL_KEYS
     * @see KeyboardFocusManager#DOWN_CYCLE_TRAVERSAL_KEYS
     * @throws IllegalArgumentException 如果 id 不是以下之一：
     *         KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS、
     *         KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS、
     *         KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS 或
     *         KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS，或者 keystrokes 包含 null，或者任何 keystroke 表示 KEY_TYPED 事件，或者任何 keystroke 已经映射到此容器的另一个默认焦点遍历操作
     * @since 1.4
     * @beaninfo
     *       bound: true
     */
    public void setFocusTraversalKeys(int id,
                                      Set<? extends AWTKeyStroke> keystrokes)
    {
        if (id < 0 || id >= KeyboardFocusManager.TRAVERSAL_KEY_LENGTH) {
            throw new IllegalArgumentException("无效的焦点遍历键标识符");
        }

        // 不要调用 super.setFocusTraversalKey。Component 参数检查
        // 不允许 DOWN_CYCLE_TRAVERSAL_KEYS，但我们允许。
        setFocusTraversalKeys_NoIDCheck(id, keystrokes);
    }

    /**
     * 返回给定遍历操作的焦点遍历键集。（请参阅
     * <code>setFocusTraversalKeys</code> 以获取每个键的完整描述。）
     * <p>
     * 如果未为此容器显式定义遍历键集，则返回此容器的父容器的集。如果未为此容器的任何祖先显式定义集，则返回当前 KeyboardFocusManager 的默认集。
     *
     * @param id 以下之一：KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS、
     *        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS、
     *        KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS 或
     *        KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS
     * @return 指定操作的 AWTKeyStrokes 集。该集将不可修改，且可能为空。永远不会返回 null。
     * @see #setFocusTraversalKeys
     * @see KeyboardFocusManager#FORWARD_TRAVERSAL_KEYS
     * @see KeyboardFocusManager#BACKWARD_TRAVERSAL_KEYS
     * @see KeyboardFocusManager#UP_CYCLE_TRAVERSAL_KEYS
     * @see KeyboardFocusManager#DOWN_CYCLE_TRAVERSAL_KEYS
     * @throws IllegalArgumentException 如果 id 不是以下之一：
     *         KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS、
     *         KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS、
     *         KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS 或
     *         KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS
     * @since 1.4
     */
    public Set<AWTKeyStroke> getFocusTraversalKeys(int id) {
        if (id < 0 || id >= KeyboardFocusManager.TRAVERSAL_KEY_LENGTH) {
            throw new IllegalArgumentException("无效的焦点遍历键标识符");
        }

        // 不要调用 super.getFocusTraversalKey。Component 参数检查
        // 不允许 DOWN_CYCLE_TRAVERSAL_KEY，但我们允许。
        return getFocusTraversalKeys_NoIDCheck(id);
    }

    /**
     * 返回给定焦点遍历操作的焦点遍历键集是否已为此容器显式定义。如果此方法返回 <code>false</code>，则此容器从祖先或当前 KeyboardFocusManager 继承该集。
     *
     * @param id 以下之一：KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS、
     *        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS、
     *        KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS 或
     *        KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS
     * @return <code>true</code> 如果已为此组件显式定义给定焦点遍历操作的焦点遍历键集；否则返回 <code>false</code>。
     * @throws IllegalArgumentException 如果 id 不是以下之一：
     *         KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS、
     *        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS、
     *        KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS 或
     *        KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS
     * @since 1.4
     */
    public boolean areFocusTraversalKeysSet(int id) {
        if (id < 0 || id >= KeyboardFocusManager.TRAVERSAL_KEY_LENGTH) {
            throw new IllegalArgumentException("无效的焦点遍历键标识符");
        }


    /**
     * 返回指定的容器是否是此容器的焦点遍历周期的焦点循环根。每个焦点遍历周期只有一个焦点循环根，每个不是焦点循环根的容器只属于一个焦点遍历周期。焦点循环根容器属于两个周期：一个以容器本身为根，另一个以容器的最近的焦点循环根祖先为根。在这种情况下，此方法将为这两个容器返回<code>true</code>。
     *
     * @param container 要测试的容器
     * @return 如果指定的容器是此容器的焦点循环根，则返回<code>true</code>；否则返回<code>false</code>
     * @see #isFocusCycleRoot()
     * @since 1.4
     */
    public boolean isFocusCycleRoot(Container container) {
        if (isFocusCycleRoot() && container == this) {
            return true;
        } else {
            return super.isFocusCycleRoot(container);
        }
    }

    private Container findTraversalRoot() {
        // 我可能有两个根，我自己和我的根父容器
        // 如果我是当前的根，则使用我
        // 如果我的任何父容器都不是根，则使用我
        // 如果我的根父容器是当前的根，则使用我的根父容器
        // 如果我或我的根父容器都不是当前的根，则
        // 使用我的根父容器（猜测）

        Container currentFocusCycleRoot = KeyboardFocusManager.
            getCurrentKeyboardFocusManager().getCurrentFocusCycleRoot();
        Container root;

        if (currentFocusCycleRoot == this) {
            root = this;
        } else {
            root = getFocusCycleRootAncestor();
            if (root == null) {
                root = this;
            }
        }

        if (root != currentFocusCycleRoot) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().
                setGlobalCurrentFocusCycleRootPriv(root);
        }
        return root;
    }

    final boolean containsFocus() {
        final Component focusOwner = KeyboardFocusManager.
            getCurrentKeyboardFocusManager().getFocusOwner();
        return isParentOf(focusOwner);
    }

    /**
     * 检查此组件是否是此容器或其子容器的子组件。
     * 注意：此函数获取 treeLock
     * 注意：此函数仅在一个窗口中遍历子树。
     * @param comp 要测试的组件，不能为空
     */
    private boolean isParentOf(Component comp) {
        synchronized(getTreeLock()) {
            while (comp != null && comp != this && !(comp instanceof Window)) {
                comp = comp.getParent();
            }
            return (comp == this);
        }
    }

    void clearMostRecentFocusOwnerOnHide() {
        boolean reset = false;
        Window window = null;

        synchronized (getTreeLock()) {
            window = getContainingWindow();
            if (window != null) {
                Component comp = KeyboardFocusManager.getMostRecentFocusOwner(window);
                reset = ((comp == this) || isParentOf(comp));
                // 此同步应始终是成对的第二个
                // (tree lock, KeyboardFocusManager.class)
                synchronized(KeyboardFocusManager.class) {
                    Component storedComp = window.getTemporaryLostComponent();
                    if (isParentOf(storedComp) || storedComp == this) {
                        window.setTemporaryLostComponent(null);
                    }
                }
            }
        }

        if (reset) {
            KeyboardFocusManager.setMostRecentFocusOwner(window, null);
        }
    }

    void clearCurrentFocusCycleRootOnHide() {
        KeyboardFocusManager kfm =
            KeyboardFocusManager.getCurrentKeyboardFocusManager();
        Container cont = kfm.getCurrentFocusCycleRoot();

        if (cont == this || isParentOf(cont)) {
            kfm.setGlobalCurrentFocusCycleRootPriv(null);
        }
    }

    final Container getTraversalRoot() {
        if (isFocusCycleRoot()) {
            return findTraversalRoot();
        }

        return super.getTraversalRoot();
    }

    /**
     * 设置将管理此容器子组件键盘遍历的焦点遍历策略，如果此容器是焦点循环根。如果参数为 null，则此容器从其焦点循环根祖先继承策略。如果参数非 null，则此策略将被所有没有自己的键盘遍历策略的焦点循环根子容器继承（递归地，这些焦点循环根子容器的子容器也是如此）。
     * <p>
     * 如果此容器不是焦点循环根，策略将被记住，但不会被此容器或任何其他容器使用或继承，直到此容器成为焦点循环根。
     *
     * @param policy 此容器的新焦点遍历策略
     * @see #getFocusTraversalPolicy
     * @see #setFocusCycleRoot
     * @see #isFocusCycleRoot
     * @since 1.4
     * @beaninfo
     *       bound: true
     */
    public void setFocusTraversalPolicy(FocusTraversalPolicy policy) {
        FocusTraversalPolicy oldPolicy;
        synchronized (this) {
            oldPolicy = this.focusTraversalPolicy;
            this.focusTraversalPolicy = policy;
        }
        firePropertyChange("focusTraversalPolicy", oldPolicy, policy);
    }

    /**
     * 返回将管理此容器子组件键盘遍历的焦点遍历策略，如果此容器不是焦点循环根，则返回 null。如果未为此容器显式设置遍历策略，则返回此容器的焦点循环根祖先的策略。
     *
     * @return 此容器的焦点遍历策略，如果此容器不是焦点循环根，则返回 null。
     * @see #setFocusTraversalPolicy
     * @see #setFocusCycleRoot
     * @see #isFocusCycleRoot
     * @since 1.4
     */
    public FocusTraversalPolicy getFocusTraversalPolicy() {
        if (!isFocusTraversalPolicyProvider() && !isFocusCycleRoot()) {
            return null;
        }

        FocusTraversalPolicy policy = this.focusTraversalPolicy;
        if (policy != null) {
            return policy;
        }

        Container rootAncestor = getFocusCycleRootAncestor();
        if (rootAncestor != null) {
            return rootAncestor.getFocusTraversalPolicy();
        } else {
            return KeyboardFocusManager.getCurrentKeyboardFocusManager().
                getDefaultFocusTraversalPolicy();
        }
    }

    /**
     * 返回是否已为此容器显式设置焦点遍历策略。如果此方法返回<code>false</code>，此容器将从祖先继承其焦点遍历策略。
     *
     * @return 如果已为此容器显式设置焦点遍历策略，则返回<code>true</code>；否则返回<code>false</code>。
     * @since 1.4
     */
    public boolean isFocusTraversalPolicySet() {
        return (focusTraversalPolicy != null);
    }

    /**
     * 设置此容器是否是焦点遍历周期的根。一旦焦点进入遍历周期，通常不能通过焦点遍历离开它，除非按下了上或下周期键。正常遍历仅限于此容器及其不是下级焦点循环根的后代。请注意，焦点遍历策略可能会改变这些限制。例如，ContainerOrderFocusTraversalPolicy 支持隐式下周期遍历。
     * <p>
     * 指定此容器子组件遍历顺序的另一种方法是使此容器成为
     * <a href="doc-files/FocusSpec.html#FocusTraversalPolicyProviders">焦点遍历策略提供者</a>。
     *
     * @param focusCycleRoot 指示此容器是否是焦点遍历周期的根
     * @see #isFocusCycleRoot()
     * @see #setFocusTraversalPolicy
     * @see #getFocusTraversalPolicy
     * @see ContainerOrderFocusTraversalPolicy
     * @see #setFocusTraversalPolicyProvider
     * @since 1.4
     * @beaninfo
     *       bound: true
     */
    public void setFocusCycleRoot(boolean focusCycleRoot) {
        boolean oldFocusCycleRoot;
        synchronized (this) {
            oldFocusCycleRoot = this.focusCycleRoot;
            this.focusCycleRoot = focusCycleRoot;
        }
        firePropertyChange("focusCycleRoot", oldFocusCycleRoot,
                           focusCycleRoot);
    }

    /**
     * 返回此容器是否是焦点遍历周期的根。一旦焦点进入遍历周期，通常不能通过焦点遍历离开它，除非按下了上或下周期键。正常遍历仅限于此容器及其不是下级焦点循环根的后代。请注意，焦点遍历策略可能会改变这些限制。例如，ContainerOrderFocusTraversalPolicy 支持隐式下周期遍历。
     *
     * @return 此容器是否是焦点遍历周期的根
     * @see #setFocusCycleRoot
     * @see #setFocusTraversalPolicy
     * @see #getFocusTraversalPolicy
     * @see ContainerOrderFocusTraversalPolicy
     * @since 1.4
     */
    public boolean isFocusCycleRoot() {
        return focusCycleRoot;
    }

    /**
     * 设置此容器是否用于提供焦点遍历策略。具有此属性为<code>true</code>的容器将用于获取焦点遍历策略，而不是最近的焦点循环根祖先。
     * @param provider 指示此容器是否用于提供焦点遍历策略
     * @see #setFocusTraversalPolicy
     * @see #getFocusTraversalPolicy
     * @see #isFocusTraversalPolicyProvider
     * @since 1.5
     * @beaninfo
     *        bound: true
     */
    public final void setFocusTraversalPolicyProvider(boolean provider) {
        boolean oldProvider;
        synchronized(this) {
            oldProvider = focusTraversalPolicyProvider;
            focusTraversalPolicyProvider = provider;
        }
        firePropertyChange("focusTraversalPolicyProvider", oldProvider, provider);
    }

    /**
     * 返回此容器是否提供焦点遍历策略。如果此属性设置为<code>true</code>，则当键盘焦点管理器在容器层次结构中搜索焦点遍历策略并在此容器之前遇到任何其他具有此属性为 true 或焦点循环根的容器时，将使用此容器的焦点遍历策略，而不是焦点循环根的策略。
     * @see #setFocusTraversalPolicy
     * @see #getFocusTraversalPolicy
     * @see #setFocusCycleRoot
     * @see #setFocusTraversalPolicyProvider
     * @return 如果此容器提供焦点遍历策略，则返回<code>true</code>，否则返回<code>false</code>
     * @since 1.5
     * @beaninfo
     *        bound: true
     */
    public final boolean isFocusTraversalPolicyProvider() {
        return focusTraversalPolicyProvider;
    }

    /**
     * 将焦点向下传递一个焦点遍历周期。如果此容器是焦点循环根，则焦点所有者将设置为此容器的默认组件以聚焦，并将当前焦点循环根设置为此容器。如果此容器不是焦点循环根，则不发生焦点遍历操作。
     *
     * @see       Component#requestFocus()
     * @see       #isFocusCycleRoot
     * @see       #setFocusCycleRoot
     * @since     1.4
     */
    public void transferFocusDownCycle() {
        if (isFocusCycleRoot()) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().
                setGlobalCurrentFocusCycleRootPriv(this);
            Component toFocus = getFocusTraversalPolicy().
                getDefaultComponent(this);
            if (toFocus != null) {
                toFocus.requestFocus(CausedFocusEvent.Cause.TRAVERSAL_DOWN);
            }
        }
    }

    void preProcessKeyEvent(KeyEvent e) {
        Container parent = this.parent;
        if (parent != null) {
            parent.preProcessKeyEvent(e);
        }
    }

    void postProcessKeyEvent(KeyEvent e) {
        Container parent = this.parent;
        if (parent != null) {
            parent.postProcessKeyEvent(e);
        }
    }

    boolean postsOldMouseEvents() {
        return true;
    }

    /**
     * 设置此容器及其包含的所有组件的<code>ComponentOrientation</code>属性。
     * <p>
     * 此方法更改与布局相关的信息，因此，使组件层次结构无效。
     *
     * @param o 此容器及其包含的组件的新组件方向。
     * @exception NullPointerException 如果<code>orientation</code>为 null。
     * @see Component#setComponentOrientation
     * @see Component#getComponentOrientation
     * @see #invalidate
     * @since 1.4
     */
    public void applyComponentOrientation(ComponentOrientation o) {
        super.applyComponentOrientation(o);
        synchronized (getTreeLock()) {
            for (int i = 0; i < component.size(); i++) {
                Component comp = component.get(i);
                comp.applyComponentOrientation(o);
            }
        }
    }

    /**
     * 将 PropertyChangeListener 添加到监听器列表。监听器注册此类的所有绑定属性，包括以下内容：
     * <ul>
     *    <li>此容器的字体("font")</li>
     *    <li>此容器的背景颜色("background")</li>
     *    <li>此容器的前景颜色("foreground")</li>
     *    <li>此容器的焦点能力("focusable")</li>
     *    <li>此容器的焦点遍历键启用状态("focusTraversalKeysEnabled")</li>
     *    <li>此容器的 FORWARD_TRAVERSAL_KEYS 集合("forwardFocusTraversalKeys")</li>
     *    <li>此容器的 BACKWARD_TRAVERSAL_KEYS 集合("backwardFocusTraversalKeys")</li>
     *    <li>此容器的 UP_CYCLE_TRAVERSAL_KEYS 集合("upCycleFocusTraversalKeys")</li>
     *    <li>此容器的 DOWN_CYCLE_TRAVERSAL_KEYS 集合("downCycleFocusTraversalKeys")</li>
     *    <li>此容器的焦点遍历策略("focusTraversalPolicy")</li>
     *    <li>此容器的焦点循环根状态("focusCycleRoot")</li>
     * </ul>
     * 注意，如果此容器正在继承一个绑定属性，则不会因继承属性的更改而触发事件。
     * <p>
     * 如果监听器为 null，则不抛出异常且不执行任何操作。
     *
     * @param    listener  要添加的 PropertyChangeListener
     *
     * @see Component#removePropertyChangeListener
     * @see #addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
    }


                /**
     * 为特定属性的监听器列表添加一个 PropertyChangeListener。指定的属性可以是用户定义的，也可以是以下默认值之一：
     * <ul>
     *    <li>此 Container 的字体 ("font")</li>
     *    <li>此 Container 的背景颜色 ("background")</li>
     *    <li>此 Container 的前景颜色 ("foreground")</li>
     *    <li>此 Container 的可聚焦性 ("focusable")</li>
     *    <li>此 Container 的焦点遍历键启用状态 ("focusTraversalKeysEnabled")</li>
     *    <li>此 Container 的 FORWARD_TRAVERSAL_KEYS 集合 ("forwardFocusTraversalKeys")</li>
     *    <li>此 Container 的 BACKWARD_TRAVERSAL_KEYS 集合 ("backwardFocusTraversalKeys")</li>
     *    <li>此 Container 的 UP_CYCLE_TRAVERSAL_KEYS 集合 ("upCycleFocusTraversalKeys")</li>
     *    <li>此 Container 的 DOWN_CYCLE_TRAVERSAL_KEYS 集合 ("downCycleFocusTraversalKeys")</li>
     *    <li>此 Container 的焦点遍历策略 ("focusTraversalPolicy")</li>
     *    <li>此 Container 的焦点循环根状态 ("focusCycleRoot")</li>
     *    <li>此 Container 的焦点遍历策略提供者状态 ("focusTraversalPolicyProvider")</li>
     *    <li>此 Container 的焦点遍历策略提供者状态 ("focusTraversalPolicyProvider")</li>
     * </ul>
     * 注意，如果此 Container 继承了一个绑定属性，那么在继承属性更改时不会触发任何事件。
     * <p>
     * 如果监听器为 null，则不会抛出异常，也不会执行任何操作。
     *
     * @param propertyName 以上列出的属性名称之一
     * @param listener 要添加的 PropertyChangeListener
     *
     * @see #addPropertyChangeListener(java.beans.PropertyChangeListener)
     * @see Component#removePropertyChangeListener
     */
    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener listener) {
        super.addPropertyChangeListener(propertyName, listener);
    }

    // 序列化支持。Container 负责恢复其组件子项的父字段。

    /**
     * Container 序列化数据版本。
     */
    private int containerSerializedDataVersion = 1;

    /**
     * 将此 <code>Container</code> 序列化到指定的 <code>ObjectOutputStream</code>。
     * <ul>
     *    <li>将默认的可序列化字段写入流中。</li>
     *    <li>将可序列化的 ContainerListener 列表作为可选数据写入。非可序列化的 ContainerListener 将被检测到，并不会尝试序列化它们。</li>
     *    <li>仅当此 Container 的 FocusTraversalPolicy 是可序列化时，才写入该策略；否则，写入 <code>null</code>。</li>
     * </ul>
     *
     * @param s 要写入的 <code>ObjectOutputStream</code>
     * @serialData 以 <code>null</code> 结尾的 0 个或多个对的序列；每个对由一个 <code>String</code> 和一个 <code>Object</code> 组成；
     *   <code>String</code> 表示对象的类型，可以是以下之一：
     *   <code>containerListenerK</code> 表示一个 <code>ContainerListener</code> 对象；
     *   此 <code>Container</code> 的 <code>FocusTraversalPolicy</code>，或 <code>null</code>
     *
     * @see AWTEventMulticaster#save(java.io.ObjectOutputStream, java.lang.String, java.util.EventListener)
     * @see Container#containerListenerK
     * @see #readObject(ObjectInputStream)
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        ObjectOutputStream.PutField f = s.putFields();
        f.put("ncomponents", component.size());
        f.put("component", component.toArray(EMPTY_ARRAY));
        f.put("layoutMgr", layoutMgr);
        f.put("dispatcher", dispatcher);
        f.put("maxSize", maxSize);
        f.put("focusCycleRoot", focusCycleRoot);
        f.put("containerSerializedDataVersion", containerSerializedDataVersion);
        f.put("focusTraversalPolicyProvider", focusTraversalPolicyProvider);
        s.writeFields();

        AWTEventMulticaster.save(s, containerListenerK, containerListener);
        s.writeObject(null);

        if (focusTraversalPolicy instanceof java.io.Serializable) {
            s.writeObject(focusTraversalPolicy);
        } else {
            s.writeObject(null);
        }
    }

    /**
     * 从指定的 <code>ObjectInputStream</code> 反序列化此 <code>Container</code>。
     * <ul>
     *    <li>从流中读取默认的可序列化字段。</li>
     *    <li>读取可序列化的 ContainerListener 列表作为可选数据。如果列表为 null，则不安装任何监听器。</li>
     *    <li>读取此 Container 的 FocusTraversalPolicy，该策略可能是 null，作为可选数据。</li>
     * </ul>
     *
     * @param s 要读取的 <code>ObjectInputStream</code>
     * @serial
     * @see #addContainerListener
     * @see #writeObject(ObjectOutputStream)
     */
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException
    {
        ObjectInputStream.GetField f = s.readFields();
        // 组件数组可能不在流中或可能为 null
        Component [] tmpComponent = (Component[])f.get("component", null);
        if (tmpComponent == null) {
            tmpComponent = EMPTY_ARRAY;
        }
        int ncomponents = (Integer) f.get("ncomponents", 0);
        if (ncomponents < 0 || ncomponents > tmpComponent.length) {
            throw new InvalidObjectException("组件数量不正确");
        }
        component = new java.util.ArrayList<Component>(ncomponents);
        for (int i = 0; i < ncomponents; ++i) {
            component.add(tmpComponent[i]);
        }
        layoutMgr = (LayoutManager)f.get("layoutMgr", null);
        dispatcher = (LightweightDispatcher)f.get("dispatcher", null);
        // 旧流。不包含 Component 的 maxSize 字段。
        if (maxSize == null) {
            maxSize = (Dimension)f.get("maxSize", null);
        }
        focusCycleRoot = f.get("focusCycleRoot", false);
        containerSerializedDataVersion = f.get("containerSerializedDataVersion", 1);
        focusTraversalPolicyProvider = f.get("focusTraversalPolicyProvider", false);
        java.util.List<Component> component = this.component;
        for(Component comp : component) {
            comp.parent = this;
            adjustListeningChildren(AWTEvent.HIERARCHY_EVENT_MASK,
                                    comp.numListening(AWTEvent.HIERARCHY_EVENT_MASK));
            adjustListeningChildren(AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK,
                                    comp.numListening(AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK));
            adjustDescendants(comp.countHierarchyMembers());
        }

        Object keyOrNull;
        while(null != (keyOrNull = s.readObject())) {
            String key = ((String)keyOrNull).intern();

            if (containerListenerK == key) {
                addContainerListener((ContainerListener)(s.readObject()));
            } else {
                // 跳过未识别键的值
                s.readObject();
            }
        }

        try {
            Object policy = s.readObject();
            if (policy instanceof FocusTraversalPolicy) {
                focusTraversalPolicy = (FocusTraversalPolicy)policy;
            }
        } catch (java.io.OptionalDataException e) {
            // JDK 1.1/1.2/1.3 实例将没有此可选数据。
            // e.eof 将为 true，表示此对象没有更多数据。如果 e.eof 不为 true，则抛出异常，因为这可能是由与 focusTraversalPolicy 无关的原因引起的。

            if (!e.eof) {
                throw e;
            }
        }
    }

    /*
     * --- Accessibility Support ---
     */

    /**
     * Container 的内部类，用于提供对辅助功能的默认支持。此类不建议由应用程序开发人员直接使用，而是仅建议由容器开发人员子类化。
     * <p>
     * 用于获取此对象的辅助功能角色的类，以及实现 AccessibleContainer 接口中的许多方法。
     * @since 1.3
     */
    protected class AccessibleAWTContainer extends AccessibleAWTComponent {

        /**
         * JDK1.3 序列化版本 ID
         */
        private static final long serialVersionUID = 5081320404842566097L;

        /**
         * 返回对象中的辅助功能子项数量。如果此对象的所有子项都实现了 <code>Accessible</code>，则此方法应返回此对象的子项数量。
         *
         * @return 对象中的辅助功能子项数量
         */
        public int getAccessibleChildrenCount() {
            return Container.this.getAccessibleChildrenCount();
        }

        /**
         * 返回对象的第 n 个 <code>Accessible</code> 子项。
         *
         * @param i 从零开始的子项索引
         * @return 对象的第 n 个 <code>Accessible</code> 子项
         */
        public Accessible getAccessibleChild(int i) {
            return Container.this.getAccessibleChild(i);
        }

        /**
         * 返回在本地坐标 <code>Point</code> 处包含的 <code>Accessible</code> 子项，如果存在的话。
         *
         * @param p 定义 <code>Accessible</code> 左上角的点，坐标空间为对象的父级
         * @return 指定位置处的 <code>Accessible</code>，如果存在的话；否则为 <code>null</code>
         */
        public Accessible getAccessibleAt(Point p) {
            return Container.this.getAccessibleAt(p);
        }

        /**
         * 注册的 PropertyChangeListener 对象数量。用于在目标 Container 的状态变化时添加/移除 ContainerListener。
         */
        private volatile transient int propertyListenersCount = 0;

        protected ContainerListener accessibleContainerHandler = null;

        /**
         * 当添加或移除子项时，如果注册了 <code>PropertyChange</code> 监听器，则触发该监听器。
         * @since 1.3
         */
        protected class AccessibleContainerHandler
            implements ContainerListener {
            public void componentAdded(ContainerEvent e) {
                Component c = e.getChild();
                if (c != null && c instanceof Accessible) {
                    AccessibleAWTContainer.this.firePropertyChange(
                        AccessibleContext.ACCESSIBLE_CHILD_PROPERTY,
                        null, ((Accessible) c).getAccessibleContext());
                }
            }
            public void componentRemoved(ContainerEvent e) {
                Component c = e.getChild();
                if (c != null && c instanceof Accessible) {
                    AccessibleAWTContainer.this.firePropertyChange(
                        AccessibleContext.ACCESSIBLE_CHILD_PROPERTY,
                        ((Accessible) c).getAccessibleContext(), null);
                }
            }
        }

        /**
         * 向监听器列表中添加一个 PropertyChangeListener。
         *
         * @param listener 要添加的 PropertyChangeListener
         */
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            if (accessibleContainerHandler == null) {
                accessibleContainerHandler = new AccessibleContainerHandler();
            }
            if (propertyListenersCount++ == 0) {
                Container.this.addContainerListener(accessibleContainerHandler);
            }
            super.addPropertyChangeListener(listener);
        }

        /**
         * 从监听器列表中移除一个 PropertyChangeListener。这将移除为所有属性注册的 PropertyChangeListener。
         *
         * @param listener 要移除的 PropertyChangeListener
         */
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            if (--propertyListenersCount == 0) {
                Container.this.removeContainerListener(accessibleContainerHandler);
            }
            super.removePropertyChangeListener(listener);
        }

    } // 内部类 AccessibleAWTContainer

    /**
     * 返回在本地坐标 <code>Point</code> 处包含的 <code>Accessible</code> 子项，如果存在的话。否则返回 <code>null</code>。
     *
     * @param p 定义 <code>Accessible</code> 左上角的点，坐标空间为对象的父级
     * @return 指定位置处的 <code>Accessible</code>，如果存在的话；否则返回 <code>null</code>
     */
    Accessible getAccessibleAt(Point p) {
        synchronized (getTreeLock()) {
            if (this instanceof Accessible) {
                Accessible a = (Accessible)this;
                AccessibleContext ac = a.getAccessibleContext();
                if (ac != null) {
                    AccessibleComponent acmp;
                    Point location;
                    int nchildren = ac.getAccessibleChildrenCount();
                    for (int i=0; i < nchildren; i++) {
                        a = ac.getAccessibleChild(i);
                        if ((a != null)) {
                            ac = a.getAccessibleContext();
                            if (ac != null) {
                                acmp = ac.getAccessibleComponent();
                                if ((acmp != null) && (acmp.isShowing())) {
                                    location = acmp.getLocation();
                                    Point np = new Point(p.x-location.x,
                                                         p.y-location.y);
                                    if (acmp.contains(np)){
                                        return a;
                                    }
                                }
                            }
                        }
                    }
                }
                return (Accessible)this;
            } else {
                Component ret = this;
                if (!this.contains(p.x,p.y)) {
                    ret = null;
                } else {
                    int ncomponents = this.getComponentCount();
                    for (int i=0; i < ncomponents; i++) {
                        Component comp = this.getComponent(i);
                        if ((comp != null) && comp.isShowing()) {
                            Point location = comp.getLocation();
                            if (comp.contains(p.x-location.x,p.y-location.y)) {
                                ret = comp;
                            }
                        }
                    }
                }
                if (ret instanceof Accessible) {
                    return (Accessible) ret;
                }
            }
            return null;
        }
    }


/**
 * 返回对象中可访问子对象的数量。如果此对象的所有子对象都实现了 <code>Accessible</code>，
 * 则此方法应返回此对象的子对象数量。
 *
 * @return 对象中可访问子对象的数量
 */
int getAccessibleChildrenCount() {
    synchronized (getTreeLock()) {
        int count = 0;
        Component[] children = this.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof Accessible) {
                count++;
            }
        }
        return count;
    }
}

/**
 * 返回对象的第 n 个 <code>Accessible</code> 子对象。
 *
 * @param i 子对象的零基索引
 * @return 对象的第 n 个 <code>Accessible</code> 子对象
 */
Accessible getAccessibleChild(int i) {
    synchronized (getTreeLock()) {
        Component[] children = this.getComponents();
        int count = 0;
        for (int j = 0; j < children.length; j++) {
            if (children[j] instanceof Accessible) {
                if (count == i) {
                    return (Accessible) children[j];
                } else {
                    count++;
                }
            }
        }
        return null;
    }
}

// ************************** MIXING CODE *******************************

final void increaseComponentCount(Component c) {
    synchronized (getTreeLock()) {
        if (!c.isDisplayable()) {
            throw new IllegalStateException(
                "Peer does not exist while invoking the increaseComponentCount() method"
            );
        }

        int addHW = 0;
        int addLW = 0;

        if (c instanceof Container) {
            addLW = ((Container)c).numOfLWComponents;
            addHW = ((Container)c).numOfHWComponents;
        }
        if (c.isLightweight()) {
            addLW++;
        } else {
            addHW++;
        }

        for (Container cont = this; cont != null; cont = cont.getContainer()) {
            cont.numOfLWComponents += addLW;
            cont.numOfHWComponents += addHW;
        }
    }
}

final void decreaseComponentCount(Component c) {
    synchronized (getTreeLock()) {
        if (!c.isDisplayable()) {
            throw new IllegalStateException(
                "Peer does not exist while invoking the decreaseComponentCount() method"
            );
        }

        int subHW = 0;
        int subLW = 0;

        if (c instanceof Container) {
            subLW = ((Container)c).numOfLWComponents;
            subHW = ((Container)c).numOfHWComponents;
        }
        if (c.isLightweight()) {
            subLW++;
        } else {
            subHW++;
        }

        for (Container cont = this; cont != null; cont = cont.getContainer()) {
            cont.numOfLWComponents -= subLW;
            cont.numOfHWComponents -= subHW;
        }
    }
}

private int getTopmostComponentIndex() {
    checkTreeLock();
    if (getComponentCount() > 0) {
        return 0;
    }
    return -1;
}

private int getBottommostComponentIndex() {
    checkTreeLock();
    if (getComponentCount() > 0) {
        return getComponentCount() - 1;
    }
    return -1;
}

/*
 * 该方法被重写以处理非不透明容器中的不透明子对象。
 */
@Override
final Region getOpaqueShape() {
    checkTreeLock();
    if (isLightweight() && isNonOpaqueForMixing()
            && hasLightweightDescendants())
    {
        Region s = Region.EMPTY_REGION;
        for (int index = 0; index < getComponentCount(); index++) {
            Component c = getComponent(index);
            if (c.isLightweight() && c.isShowing()) {
                s = s.getUnion(c.getOpaqueShape());
            }
        }
        return s.getIntersection(getNormalShape());
    }
    return super.getOpaqueShape();
}

final void recursiveSubtractAndApplyShape(Region shape) {
    recursiveSubtractAndApplyShape(shape, getTopmostComponentIndex(), getBottommostComponentIndex());
}

final void recursiveSubtractAndApplyShape(Region shape, int fromZorder) {
    recursiveSubtractAndApplyShape(shape, fromZorder, getBottommostComponentIndex());
}

final void recursiveSubtractAndApplyShape(Region shape, int fromZorder, int toZorder) {
    checkTreeLock();
    if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
        mixingLog.fine("this = " + this +
            "; shape=" + shape + "; fromZ=" + fromZorder + "; toZ=" + toZorder);
    }
    if (fromZorder == -1) {
        return;
    }
    if (shape.isEmpty()) {
        return;
    }
    // 无效的容器且布局不为空应被混合代码忽略，容器将在稍后验证，混合代码将在稍后执行。
    if (getLayout() != null && !isValid()) {
        return;
    }
    for (int index = fromZorder; index <= toZorder; index++) {
        Component comp = getComponent(index);
        if (!comp.isLightweight()) {
            comp.subtractAndApplyShape(shape);
        } else if (comp instanceof Container &&
                ((Container)comp).hasHeavyweightDescendants() && comp.isShowing()) {
            ((Container)comp).recursiveSubtractAndApplyShape(shape);
        }
    }
}

final void recursiveApplyCurrentShape() {
    recursiveApplyCurrentShape(getTopmostComponentIndex(), getBottommostComponentIndex());
}

final void recursiveApplyCurrentShape(int fromZorder) {
    recursiveApplyCurrentShape(fromZorder, getBottommostComponentIndex());
}

final void recursiveApplyCurrentShape(int fromZorder, int toZorder) {
    checkTreeLock();
    if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
        mixingLog.fine("this = " + this +
            "; fromZ=" + fromZorder + "; toZ=" + toZorder);
    }
    if (fromZorder == -1) {
        return;
    }
    // 无效的容器且布局不为空应被混合代码忽略，容器将在稍后验证，混合代码将在稍后执行。
    if (getLayout() != null && !isValid()) {
        return;
    }
    for (int index = fromZorder; index <= toZorder; index++) {
        Component comp = getComponent(index);
        if (!comp.isLightweight()) {
            comp.applyCurrentShape();
        }
        if (comp instanceof Container &&
                ((Container)comp).hasHeavyweightDescendants()) {
            ((Container)comp).recursiveApplyCurrentShape();
        }
    }
}

private void recursiveShowHeavyweightChildren() {
    if (!hasHeavyweightDescendants() || !isVisible()) {
        return;
    }
    for (int index = 0; index < getComponentCount(); index++) {
        Component comp = getComponent(index);
        if (comp.isLightweight()) {
            if  (comp instanceof Container) {
                ((Container)comp).recursiveShowHeavyweightChildren();
            }
        } else {
            if (comp.isVisible()) {
                ComponentPeer peer = comp.getPeer();
                if (peer != null) {
                    peer.setVisible(true);
                }
            }
        }
    }
}

private void recursiveHideHeavyweightChildren() {
    if (!hasHeavyweightDescendants()) {
        return;
    }
    for (int index = 0; index < getComponentCount(); index++) {
        Component comp = getComponent(index);
        if (comp.isLightweight()) {
            if  (comp instanceof Container) {
                ((Container)comp).recursiveHideHeavyweightChildren();
            }
        } else {
            if (comp.isVisible()) {
                ComponentPeer peer = comp.getPeer();
                if (peer != null) {
                    peer.setVisible(false);
                }
            }
        }
    }
}

private void recursiveRelocateHeavyweightChildren(Point origin) {
    for (int index = 0; index < getComponentCount(); index++) {
        Component comp = getComponent(index);
        if (comp.isLightweight()) {
            if  (comp instanceof Container &&
                    ((Container)comp).hasHeavyweightDescendants())
            {
                final Point newOrigin = new Point(origin);
                newOrigin.translate(comp.getX(), comp.getY());
                ((Container)comp).recursiveRelocateHeavyweightChildren(newOrigin);
            }
        } else {
            ComponentPeer peer = comp.getPeer();
            if (peer != null) {
                peer.setBounds(origin.x + comp.getX(), origin.y + comp.getY(),
                        comp.getWidth(), comp.getHeight(),
                        ComponentPeer.SET_LOCATION);
            }
        }
    }
}

/**
 * 检查容器及其直接轻量级容器是否可见。
 *
 * 考虑到重型容器会自动隐藏或显示 HW 子对象。因此我们只关心轻量级容器的可见性。
 *
 * 此方法必须在 TreeLock 下调用。
 */
final boolean isRecursivelyVisibleUpToHeavyweightContainer() {
    if (!isLightweight()) {
        return true;
    }

    for (Container cont = this;
            cont != null && cont.isLightweight();
            cont = cont.getContainer())
    {
        if (!cont.isVisible()) {
            return false;
        }
    }
    return true;
}

@Override
void mixOnShowing() {
    synchronized (getTreeLock()) {
        if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
            mixingLog.fine("this = " + this);
        }

        boolean isLightweight = isLightweight();

        if (isLightweight && isRecursivelyVisibleUpToHeavyweightContainer()) {
            recursiveShowHeavyweightChildren();
        }

        if (!isMixingNeeded()) {
            return;
        }

        if (!isLightweight || (isLightweight && hasHeavyweightDescendants())) {
            recursiveApplyCurrentShape();
        }

        super.mixOnShowing();
    }
}

@Override
void mixOnHiding(boolean isLightweight) {
    synchronized (getTreeLock()) {
        if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
            mixingLog.fine("this = " + this +
                    "; isLightweight=" + isLightweight);
        }
        if (isLightweight) {
            recursiveHideHeavyweightChildren();
        }
        super.mixOnHiding(isLightweight);
    }
}

@Override
void mixOnReshaping() {
    synchronized (getTreeLock()) {
        if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
            mixingLog.fine("this = " + this);
        }

        boolean isMixingNeeded = isMixingNeeded();

        if (isLightweight() && hasHeavyweightDescendants()) {
            final Point origin = new Point(getX(), getY());
            for (Container cont = getContainer();
                    cont != null && cont.isLightweight();
                    cont = cont.getContainer())
            {
                origin.translate(cont.getX(), cont.getY());
            }

            recursiveRelocateHeavyweightChildren(origin);

            if (!isMixingNeeded) {
                return;
            }

            recursiveApplyCurrentShape();
        }

        if (!isMixingNeeded) {
            return;
        }

        super.mixOnReshaping();
    }
}

@Override
void mixOnZOrderChanging(int oldZorder, int newZorder) {
    synchronized (getTreeLock()) {
        if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
            mixingLog.fine("this = " + this +
                "; oldZ=" + oldZorder + "; newZ=" + newZorder);
        }

        if (!isMixingNeeded()) {
            return;
        }

        boolean becameHigher = newZorder < oldZorder;

        if (becameHigher && isLightweight() && hasHeavyweightDescendants()) {
            recursiveApplyCurrentShape();
        }
        super.mixOnZOrderChanging(oldZorder, newZorder);
    }
}

@Override
void mixOnValidating() {
    synchronized (getTreeLock()) {
        if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
            mixingLog.fine("this = " + this);
        }

        if (!isMixingNeeded()) {
            return;
        }

        if (hasHeavyweightDescendants()) {
            recursiveApplyCurrentShape();
        }

        if (isLightweight() && isNonOpaqueForMixing()) {
            subtractAndApplyShapeBelowMe();
        }

        super.mixOnValidating();
    }
}

// ****************** END OF MIXING CODE ********************************
}

/**
 * 管理将 MouseEvents 派发到轻量级子对象以及将 SunDropTargetEvents 派发到轻量级和重型子对象
 * 的类，这些子对象由本机容器包含。
 *
 * 注意：类名已不再合适，但我们不能更改它，因为必须保持序列化兼容性。
 *
 * @author Timothy Prinzing
 */
class LightweightDispatcher implements java.io.Serializable, AWTEventListener {

    /*
     * JDK 1.1 序列化版本 ID
     */
    private static final long serialVersionUID = 5184291520170872969L;
    /*
     * 当我们从另一个重型容器拖动时，我们自己的鼠标事件
     */
    private static final int  LWD_MOUSE_DRAGGED_OVER = 1500;

    private static final PlatformLogger eventLog = PlatformLogger.getLogger("java.awt.event.LightweightDispatcher");

    private static final int BUTTONS_DOWN_MASK;

    static {
        int[] buttonsDownMask = AWTAccessor.getInputEventAccessor().
                getButtonDownMasks();
        int mask = 0;
        for (int buttonDownMask : buttonsDownMask) {
            mask |= buttonDownMask;
        }
        BUTTONS_DOWN_MASK = mask;
    }

    LightweightDispatcher(Container nativeContainer) {
        this.nativeContainer = nativeContainer;
        mouseEventTarget = new WeakReference<>(null);
        targetLastEntered = new WeakReference<>(null);
        targetLastEnteredDT = new WeakReference<>(null);
        eventMask = 0;
    }

    /*
     * 清理在创建调度器时分配的任何资源；应在 Container.removeNotify 中调用
     */
    void dispose() {
        //System.out.println("Disposing lw dispatcher");
        stopListeningForOtherDrags();
        mouseEventTarget.clear();
        targetLastEntered.clear();
        targetLastEnteredDT.clear();
    }


    /**
     * 启用子组件的事件。
     */
    void enableEvents(long events) {
        eventMask |= events;
    }

    /**
     * 如果需要，将事件分派给子组件，并
     * 返回事件是否已转发给子组件。
     *
     * @param e 事件
     */
    boolean dispatchEvent(AWTEvent e) {
        boolean ret = false;

        /*
         * BugTraq Id 4389284 的修复。
         * 无论 eventMask 值如何，都分派 SunDropTargetEvents。
         * 在分派 SunDropTargetEvents 时不要更新光标。
         */
        if (e instanceof SunDropTargetEvent) {

            SunDropTargetEvent sdde = (SunDropDropTargetEvent) e;
            ret = processDropTargetEvent(sdde);

        } else {
            if (e instanceof MouseEvent && (eventMask & MOUSE_MASK) != 0) {
                MouseEvent me = (MouseEvent) e;
                ret = processMouseEvent(me);
            }

            if (e.getID() == MouseEvent.MOUSE_MOVED) {
                nativeContainer.updateCursorImmediately();
            }
        }

        return ret;
    }

    /* 此方法实际上返回事件发生前鼠标按钮是否处于按下状态。
     * 更好的方法名可能是 wasAMouseButtonDownBeforeThisEvent()。
     */
    private boolean isMouseGrab(MouseEvent e) {
        int modifiers = e.getModifiersEx();

        if (e.getID() == MouseEvent.MOUSE_PRESSED
                || e.getID() == MouseEvent.MOUSE_RELEASED) {
            modifiers ^= InputEvent.getMaskForButton(e.getButton());
        }
        /* modifiers 现在是事件发生前的状态 */
        return ((modifiers & BUTTONS_DOWN_MASK) != 0);
    }

    /**
     * 此方法尝试将鼠标事件分发给轻量级组件。它尽量避免不必要的组件树探查，
     * 以最小化确定事件路由位置的开销，因为鼠标移动事件通常数量大且频繁。
     */
    private boolean processMouseEvent(MouseEvent e) {
        int id = e.getID();
        Component mouseOver =   // 对鼠标事件敏感
            nativeContainer.getMouseEventTarget(e.getX(), e.getY(),
                                                Container.INCLUDE_SELF);

        trackMouseEnterExit(mouseOver, e);

        Component met = mouseEventTarget.get();
        // 4508327 : MOUSE_CLICKED 应仅发送给接收到伴随的 MOUSE_PRESSED 的接收者，
        // 因此在 MOUSE_CLICKED 时不要重置 mouseEventTarget。
        if (!isMouseGrab(e) && id != MouseEvent.MOUSE_CLICKED) {
            met = (mouseOver != nativeContainer) ? mouseOver : null;
            mouseEventTarget = new WeakReference<>(met);
        }

        if (met != null) {
            switch (id) {
                case MouseEvent.MOUSE_ENTERED:
                case MouseEvent.MOUSE_EXITED:
                    break;
                case MouseEvent.MOUSE_PRESSED:
                    retargetMouseEvent(met, id, e);
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    retargetMouseEvent(met, id, e);
                    break;
                case MouseEvent.MOUSE_CLICKED:
                    // 4508327: MOUSE_CLICKED 事件不应分派给除接收到 MOUSE_PRESSED 事件的组件之外的其他组件。
                    // 如果鼠标现在位于不同的组件上，不要分派事件。
                    // 之前的类似问题修复与 bug 4155217 相关。
                    if (mouseOver == met) {
                        retargetMouseEvent(mouseOver, id, e);
                    }
                    break;
                case MouseEvent.MOUSE_MOVED:
                    retargetMouseEvent(met, id, e);
                    break;
                case MouseEvent.MOUSE_DRAGGED:
                    if (isMouseGrab(e)) {
                        retargetMouseEvent(met, id, e);
                    }
                    break;
                case MouseEvent.MOUSE_WHEEL:
                    // 这可能会将事件发送到未启用 MouseWheelEvents 的地方。
                    // 在这种情况下，Component.dispatchEventImpl() 将事件重新定向到启用了事件的父组件。
                    if (eventLog.isLoggable(PlatformLogger.Level.FINEST) && (mouseOver != null)) {
                        eventLog.finest("将鼠标滚轮事件重新定向到 " +
                                mouseOver.getName() + ", " +
                                mouseOver.getClass());
                    }
                    retargetMouseEvent(mouseOver, id, e);
                    break;
            }
            // 滚轮事件的消费在 "retargetMouseEvent" 中实现。
            if (id != MouseEvent.MOUSE_WHEEL) {
                e.consume();
            }
        }
        return e.isConsumed();
    }

    private boolean processDropTargetEvent(SunDropTargetEvent e) {
        int id = e.getID();
        int x = e.getX();
        int y = e.getY();

        /*
         * BugTraq ID 4395290 的修复。
         * SunDropTargetEvent 的 Point 可能位于 native container 边界之外。在这种情况下，我们截断坐标。
         */
        if (!nativeContainer.contains(x, y)) {
            final Dimension d = nativeContainer.getSize();
            if (d.width <= x) {
                x = d.width - 1;
            } else if (x < 0) {
                x = 0;
            }
            if (d.height <= y) {
                y = d.height - 1;
            } else if (y < 0) {
                y = 0;
            }
        }
        Component mouseOver =   // 不一定对鼠标事件敏感
            nativeContainer.getDropTargetEventTarget(x, y,
                                                     Container.INCLUDE_SELF);
        trackMouseEnterExit(mouseOver, e);

        if (mouseOver != nativeContainer && mouseOver != null) {
            switch (id) {
            case SunDropTargetEvent.MOUSE_ENTERED:
            case SunDropTargetEvent.MOUSE_EXITED:
                break;
            default:
                retargetMouseEvent(mouseOver, id, e);
                e.consume();
                break;
            }
        }
        return e.isConsumed();
    }

    /*
     * 生成 dnd 进入/退出事件，当鼠标在轻量级组件上移动时
     * @param targetOver       鼠标所在的目标（包括 native container）
     * @param e                native container 中的 SunDropTarget 鼠标事件
     */
    private void trackDropTargetEnterExit(Component targetOver, MouseEvent e) {
        int id = e.getID();
        if (id == MouseEvent.MOUSE_ENTERED && isMouseDTInNativeContainer) {
            // 如果轻量级组件启动了拖动并且有相关的 drop target，这可能会发生。
            // 当鼠标已经在 native container 中时，会收到 MOUSE_ENTERED 事件。
            // 为了正确传播此事件，我们应该清除 targetLastEnteredDT。
            targetLastEnteredDT.clear();
        } else if (id == MouseEvent.MOUSE_ENTERED) {
            isMouseDTInNativeContainer = true;
        } else if (id == MouseEvent.MOUSE_EXITED) {
            isMouseDTInNativeContainer = false;
        }
        Component tle = retargetMouseEnterExit(targetOver, e,
                                                     targetLastEnteredDT.get(),
                                                     isMouseDTInNativeContainer);
        targetLastEnteredDT = new WeakReference<>(tle);
    }

    /*
     * 生成进入/退出事件，当鼠标在轻量级组件上移动时
     * @param targetOver        鼠标所在的目标（包括 native container）
     * @param e                 native container 中的鼠标事件
     */
    private void trackMouseEnterExit(Component targetOver, MouseEvent e) {
        if (e instanceof SunDropTargetEvent) {
            trackDropTargetEnterExit(targetOver, e);
            return;
        }
        int id = e.getID();

        if ( id != MouseEvent.MOUSE_EXITED &&
             id != MouseEvent.MOUSE_DRAGGED &&
             id != LWD_MOUSE_DRAGGED_OVER &&
                !isMouseInNativeContainer) {
            // 任何除退出或拖动之外的事件都意味着我们处于 native container 中
            isMouseInNativeContainer = true;
            startListeningForOtherDrags();
        } else if (id == MouseEvent.MOUSE_EXITED) {
            isMouseInNativeContainer = false;
            stopListeningForOtherDrags();
        }
        Component tle = retargetMouseEnterExit(targetOver, e,
                                                   targetLastEntered.get(),
                                                   isMouseInNativeContainer);
        targetLastEntered = new WeakReference<>(tle);
    }

    private Component retargetMouseEnterExit(Component targetOver, MouseEvent e,
                                             Component lastEntered,
                                             boolean inNativeContainer) {
        int id = e.getID();
        Component targetEnter = inNativeContainer ? targetOver : null;

        if (lastEntered != targetEnter) {
            if (lastEntered != null) {
                retargetMouseEvent(lastEntered, MouseEvent.MOUSE_EXITED, e);
            }
            if (id == MouseEvent.MOUSE_EXITED) {
                // 如果我们生成了一个退出事件，则消费原生退出事件
                e.consume();
            }

            if (targetEnter != null) {
                retargetMouseEvent(targetEnter, MouseEvent.MOUSE_ENTERED, e);
            }
            if (id == MouseEvent.MOUSE_ENTERED) {
                // 如果我们生成了一个进入事件，则消费原生进入事件
                e.consume();
            }
        }
        return targetEnter;
    }

    /*
     * 监听全局鼠标拖动事件，以便即使从其他重量级容器开始的拖动也会在本容器中生成进入/退出事件
     */
    private void startListeningForOtherDrags() {
        //System.out.println("Adding AWTEventListener");
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Object>() {
                public Object run() {
                    nativeContainer.getToolkit().addAWTEventListener(
                        LightweightDispatcher.this,
                        AWTEvent.MOUSE_EVENT_MASK |
                        AWTEvent.MOUSE_MOTION_EVENT_MASK);
                    return null;
                }
            }
        );
    }

    private void stopListeningForOtherDrags() {
        //System.out.println("Removing AWTEventListener");
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Object>() {
                public Object run() {
                    nativeContainer.getToolkit().removeAWTEventListener(LightweightDispatcher.this);
                    return null;
                }
            }
        );
    }

    /*
     * (实现 AWTEventListener)
     * 监听在其他 hw 组件中发布的拖动事件，以便我们可以跟踪进入/退出，无论拖动从何处开始
     */
    public void eventDispatched(AWTEvent e) {
        boolean isForeignDrag = (e instanceof MouseEvent) &&
                                !(e instanceof SunDropTargetEvent) &&
                                (e.id == MouseEvent.MOUSE_DRAGGED) &&
                                (e.getSource() != nativeContainer);

        if (!isForeignDrag) {
            // 只对来自其他 hw 组件的拖动感兴趣
            return;
        }

        MouseEvent      srcEvent = (MouseEvent)e;
        MouseEvent      me;

        synchronized (nativeContainer.getTreeLock()) {
            Component srcComponent = srcEvent.getComponent();

            // 拖动事件发布后，组件可能已消失
            // （例如 Swing 分层菜单）
            if ( !srcComponent.isShowing() ) {
                return;
            }

            // 参见 5083555
            // 检查 srcComponent 是否在任何模态阻塞的窗口中
            Component c = nativeContainer;
            while ((c != null) && !(c instanceof Window)) {
                c = c.getParent_NoClientCode();
            }
            if ((c == null) || ((Window)c).isModalBlocked()) {
                return;
            }

            //
            // 创建一个内部 'dragged-over' 事件，表示
            // 我们正从另一个 hw 组件被拖动
            //
            me = new MouseEvent(nativeContainer,
                               LWD_MOUSE_DRAGGED_OVER,
                               srcEvent.getWhen(),
                               srcEvent.getModifiersEx() | srcEvent.getModifiers(),
                               srcEvent.getX(),
                               srcEvent.getY(),
                               srcEvent.getXOnScreen(),
                               srcEvent.getYOnScreen(),
                               srcEvent.getClickCount(),
                               srcEvent.isPopupTrigger(),
                               srcEvent.getButton());
            MouseEventAccessor meAccessor = AWTAccessor.getMouseEventAccessor();
            meAccessor.setCausedByTouchEvent(me,
                meAccessor.isCausedByTouchEvent(srcEvent));
            ((AWTEvent)srcEvent).copyPrivateDataInto(me);
            // 将坐标转换为本 native container
            final Point ptSrcOrigin = srcComponent.getLocationOnScreen();

            if (AppContext.getAppContext() != nativeContainer.appContext) {
                final MouseEvent mouseEvent = me;
                Runnable r = new Runnable() {
                        public void run() {
                            if (!nativeContainer.isShowing() ) {
                                return;
                            }

                            Point       ptDstOrigin = nativeContainer.getLocationOnScreen();
                            mouseEvent.translatePoint(ptSrcOrigin.x - ptDstOrigin.x,
                                              ptSrcOrigin.y - ptDstOrigin.y );
                            Component targetOver =
                                nativeContainer.getMouseEventTarget(mouseEvent.getX(),
                                                                    mouseEvent.getY(),
                                                                    Container.INCLUDE_SELF);
                            trackMouseEnterExit(targetOver, mouseEvent);
                        }
                    };
                SunToolkit.executeOnEventHandlerThread(nativeContainer, r);
                return;
            } else {
                if (!nativeContainer.isShowing() ) {
                    return;
                }

                Point   ptDstOrigin = nativeContainer.getLocationOnScreen();
                me.translatePoint( ptSrcOrigin.x - ptDstOrigin.x, ptSrcOrigin.y - ptDstOrigin.y );
            }
        }
        //System.out.println("Track event: " + me);
        // 直接将 'dragged-over' 事件传递给进入/退出代码（不是真实事件，因此不要传递给 dispatchEvent）
        Component targetOver =
            nativeContainer.getMouseEventTarget(me.getX(), me.getY(),
                                                Container.INCLUDE_SELF);
        trackMouseEnterExit(targetOver, me);
    }


                /**
     * 使用给定的事件（发送到窗口主机）作为 srcEvent，向当前鼠标事件接收者发送鼠标事件。如果鼠标事件目标仍在组件树中，
     * 事件的坐标将转换为目标的坐标。如果目标已被移除，则不会发送消息。
     */
    void retargetMouseEvent(Component target, int id, MouseEvent e) {
        if (target == null) {
            return; // 鼠标位于其他硬件组件上或目标已禁用
        }

        int x = e.getX(), y = e.getY();
        Component component;

        for(component = target;
            component != null && component != nativeContainer;
            component = component.getParent()) {
            x -= component.x;
            y -= component.y;
        }
        MouseEvent retargeted;
        if (component != null) {
            if (e instanceof SunDropTargetEvent) {
                retargeted = new SunDropTargetEvent(target,
                                                    id,
                                                    x,
                                                    y,
                                                    ((SunDropTargetEvent)e).getDispatcher());
            } else if (id == MouseEvent.MOUSE_WHEEL) {
                retargeted = new MouseWheelEvent(target,
                                      id,
                                       e.getWhen(),
                                       e.getModifiersEx() | e.getModifiers(),
                                       x,
                                       y,
                                       e.getXOnScreen(),
                                       e.getYOnScreen(),
                                       e.getClickCount(),
                                       e.isPopupTrigger(),
                                       ((MouseWheelEvent)e).getScrollType(),
                                       ((MouseWheelEvent)e).getScrollAmount(),
                                       ((MouseWheelEvent)e).getWheelRotation(),
                                       ((MouseWheelEvent)e).getPreciseWheelRotation());
            }
            else {
                retargeted = new MouseEvent(target,
                                            id,
                                            e.getWhen(),
                                            e.getModifiersEx() | e.getModifiers(),
                                            x,
                                            y,
                                            e.getXOnScreen(),
                                            e.getYOnScreen(),
                                            e.getClickCount(),
                                            e.isPopupTrigger(),
                                            e.getButton());
                MouseEventAccessor meAccessor = AWTAccessor.getMouseEventAccessor();
                meAccessor.setCausedByTouchEvent(retargeted,
                    meAccessor.isCausedByTouchEvent(e));
            }

            ((AWTEvent)e).copyPrivateDataInto(retargeted);

            if (target == nativeContainer) {
                // 避免递归调用 LightweightDispatcher...
                ((Container)target).dispatchEventToSelf(retargeted);
            } else {
                assert AppContext.getAppContext() == target.appContext;

                if (nativeContainer.modalComp != null) {
                    if (((Container)nativeContainer.modalComp).isAncestorOf(target)) {
                        target.dispatchEvent(retargeted);
                    } else {
                        e.consume();
                    }
                } else {
                    target.dispatchEvent(retargeted);
                }
            }
            if (id == MouseEvent.MOUSE_WHEEL && retargeted.isConsumed()) {
                // 轮播事件传递给原生系统的一个例外。
                // 在 "processMouseEvent" 中，轮播事件的总事件消费被跳过。
                // 防止 Java 接受的轮播事件的传递。
                e.consume();
            }
        }
    }

    // --- 成员变量 -------------------------------

    /**
     * 可能为主机子组件事件的窗口容器。
     */
    private Container nativeContainer;

    /**
     * 此变量未使用，但保留用于序列化兼容性。
     */
    private Component focus;

    /**
     * 当前由该窗口组件托管的子组件，事件被转发给它。如果此值为 null，则当前没有事件被转发给子组件。
     */
    private transient WeakReference<Component> mouseEventTarget;

    /**
     * 最后一个由 {@code MouseEvent} 进入的组件。
     */
    private transient  WeakReference<Component> targetLastEntered;

    /**
     * 最后一个由 {@code SunDropTargetEvent} 进入的组件。
     */
    private transient  WeakReference<Component> targetLastEnteredDT;

    /**
     * 鼠标是否在原生容器上。
     */
    private transient boolean isMouseInNativeContainer = false;

    /**
     * DnD 是否在原生容器上。
     */
    private transient boolean isMouseDTInNativeContainer = false;

    /**
     * 此变量未使用，但保留用于序列化兼容性。
     */
    private Cursor nativeCursor;

    /**
     * 轻量级组件的事件掩码。轻量级组件需要一个窗口容器来托管与窗口相关的事件。这个单独的掩码表示由包含的轻量级组件请求的事件，
     * 而不会影响窗口组件本身的掩码。
     */
    private long eventMask;

    /**
     * 从窗口主机路由到轻量级组件的事件类型。
     */
    private static final long PROXY_EVENT_MASK =
        AWTEvent.FOCUS_EVENT_MASK |
        AWTEvent.KEY_EVENT_MASK |
        AWTEvent.MOUSE_EVENT_MASK |
        AWTEvent.MOUSE_MOTION_EVENT_MASK |
        AWTEvent.MOUSE_WHEEL_EVENT_MASK;

    private static final long MOUSE_MASK =
        AWTEvent.MOUSE_EVENT_MASK |
        AWTEvent.MOUSE_MOTION_EVENT_MASK |
        AWTEvent.MOUSE_WHEEL_EVENT_MASK;
}
