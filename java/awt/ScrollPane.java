
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

import java.awt.peer.LightweightPeer;
import java.awt.peer.ScrollPanePeer;
import java.awt.event.*;
import javax.accessibility.*;
import sun.awt.ScrollPaneWheelScroller;
import sun.awt.SunToolkit;

import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

/**
 * 一个容器类，实现单个子组件的自动水平和/或垂直滚动。滚动条的显示策略可以设置为：
 * <OL>
 * <LI>按需：仅在需要时创建和显示滚动条
 * <LI>始终：始终创建并显示滚动条
 * <LI>从不：从不创建或显示滚动条
 * </OL>
 * <P>
 * 水平和垂直滚动条的状态由两个 <code>ScrollPaneAdjustable</code> 对象（每个维度一个）表示，这些对象实现了 <code>Adjustable</code> 接口。
 * API 提供了访问这些对象的方法，以便可以操作 Adjustable 对象上的属性（如 unitIncrement、value 等）。
 * <P>
 * 某些可调整属性（minimum、maximum、blockIncrement 和 visibleAmount）由滚动窗格根据滚动窗格和其子组件的几何形状内部设置，这些属性不应由使用滚动窗格的程序设置。
 * <P>
 * 如果滚动条显示策略设置为“从不”，则仍可以使用 setScrollPosition() 方法通过编程方式滚动滚动窗格，滚动窗格将适当地移动和裁剪子组件的内容。此策略在程序需要创建和管理自己的可调整控件时非常有用。
 * <P>
 * 滚动条的位置由用户在程序外部设置的平台特定属性控制。
 * <P>
 * 该容器的初始大小设置为 100x100，但可以使用 setSize() 重置。
 * <P>
 * 使用带有滚轮的鼠标进行滚动默认是启用的。可以使用 <code>setWheelScrollingEnabled</code> 禁用滚动。可以通过设置水平和垂直 Adjustable 的块和单位增量来自定义滚轮滚动。有关鼠标滚轮事件如何分发的信息，请参阅 {@link MouseWheelEvent} 类的描述。
 * <P>
 * Insets 用于定义滚动条占用的空间和滚动窗格创建的任何边框。可以使用 getInsets() 获取当前的 insets 值。如果 scrollbarsAlwaysVisible 的值为 false，则 insets 的值将根据滚动条当前是否可见而动态变化。
 *
 * @author      Tom Ball
 * @author      Amy Fowler
 * @author      Tim Prinzing
 */
public class ScrollPane extends Container implements Accessible {


    /**
     * 初始化 JNI 字段和方法 ID
     */
    private static native void initIDs();

    static {
        /* 确保加载了必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * 指定水平/垂直滚动条仅在子组件的大小超过滚动窗格在水平/垂直维度上的大小时显示。
     */
    public static final int SCROLLBARS_AS_NEEDED = 0;

    /**
     * 指定水平/垂直滚动条应始终显示，无论滚动窗格和子组件的大小如何。
     */
    public static final int SCROLLBARS_ALWAYS = 1;

    /**
     * 指定水平/垂直滚动条应从不显示，无论滚动窗格和子组件的大小如何。
     */
    public static final int SCROLLBARS_NEVER = 2;

    /**
     * 滚动条有 3 种显示方式。此整数将表示这 3 种显示方式之一 -
     * (SCROLLBARS_ALWAYS, SCROLLBARS_AS_NEEDED, SCROLLBARS_NEVER)
     *
     * @serial
     * @see #getScrollbarDisplayPolicy
     */
    private int scrollbarDisplayPolicy;

    /**
     * 可调整的垂直滚动条。
     * 需要注意的是，必须 <em>不要</em> 调用 3 个 <code>Adjustable</code> 方法，即：
     * <code>setMinimum()</code>、<code>setMaximum()</code>、
     * <code>setVisibleAmount()</code>。
     *
     * @serial
     * @see #getVAdjustable
     */
    private ScrollPaneAdjustable vAdjustable;

    /**
     * 可调整的水平滚动条。
     * 需要注意的是，必须 <em>不要</em> 调用 3 个 <code>Adjustable</code> 方法，即：
     * <code>setMinimum()</code>、<code>setMaximum()</code>、
     * <code>setVisibleAmount()</code>。
     *
     * @serial
     * @see #getHAdjustable
     */
    private ScrollPaneAdjustable hAdjustable;

    private static final String base = "scrollpane";
    private static int nameCounter = 0;

    private static final boolean defaultWheelScroll = true;

    /**
     * 指示在接收到 MouseWheelEvent 时是否应进行滚动。
     *
     * @serial
     * @since 1.4
     */
    private boolean wheelScrollingEnabled = defaultWheelScroll;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = 7956609840827222915L;

    /**
     * 创建一个新的滚动窗格容器，滚动条显示策略为“按需”。
     * @throws HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public ScrollPane() throws HeadlessException {
        this(SCROLLBARS_AS_NEEDED);
    }

    /**
     * 创建一个新的滚动窗格容器。
     * @param scrollbarDisplayPolicy 滚动条应显示的策略
     * @throws IllegalArgumentException 如果指定的滚动条显示策略无效
     * @throws HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    @ConstructorProperties({"scrollbarDisplayPolicy"})
    public ScrollPane(int scrollbarDisplayPolicy) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        this.layoutMgr = null;
        this.width = 100;
        this.height = 100;
        switch (scrollbarDisplayPolicy) {
            case SCROLLBARS_NEVER:
            case SCROLLBARS_AS_NEEDED:
            case SCROLLBARS_ALWAYS:
                this.scrollbarDisplayPolicy = scrollbarDisplayPolicy;
                break;
            default:
                throw new IllegalArgumentException("非法的滚动条显示策略");
        }

        vAdjustable = new ScrollPaneAdjustable(this, new PeerFixer(this),
                                               Adjustable.VERTICAL);
        hAdjustable = new ScrollPaneAdjustable(this, new PeerFixer(this),
                                               Adjustable.HORIZONTAL);
        setWheelScrollingEnabled(defaultWheelScroll);
    }

    /**
     * 为该组件构建一个名称。当名称为 null 时由 getName() 调用。
     */
    String constructComponentName() {
        synchronized (ScrollPane.class) {
            return base + nameCounter++;
        }
    }

    // 滚动窗格不能与没有窗口的子组件一起工作... 它假设它是在移动一个子窗口，因此没有窗口的子组件被包装在一个窗口中。
    private void addToPanel(Component comp, Object constraints, int index) {
        Panel child = new Panel();
        child.setLayout(new BorderLayout());
        child.add(comp);
        super.addImpl(child, constraints, index);
        validate();
    }

    /**
     * 将指定的组件添加到此滚动窗格容器中。
     * 如果滚动窗格已有子组件，则移除现有子组件并添加新的子组件。
     * @param comp 要添加的组件
     * @param constraints 不适用
     * @param index 子组件的位置（必须 <= 0）
     */
    protected final void addImpl(Component comp, Object constraints, int index) {
        synchronized (getTreeLock()) {
            if (getComponentCount() > 0) {
                remove(0);
            }
            if (index > 0) {
                throw new IllegalArgumentException("位置大于 0");
            }

            if (!SunToolkit.isLightweightOrUnknown(comp)) {
                super.addImpl(comp, constraints, index);
            } else {
                addToPanel(comp, constraints, index);
            }
        }
    }

    /**
     * 返回滚动条的显示策略。
     * @return 滚动条的显示策略
     */
    public int getScrollbarDisplayPolicy() {
        return scrollbarDisplayPolicy;
    }

    /**
     * 返回滚动窗格视口的当前大小。
     * @return 以像素为单位的视口大小
     */
    public Dimension getViewportSize() {
        Insets i = getInsets();
        return new Dimension(width - i.right - i.left,
                             height - i.top - i.bottom);
    }

    /**
     * 返回水平滚动条占用的高度，无论它当前是否由滚动窗格显示。
     * @return 以像素为单位的水平滚动条的高度
     */
    public int getHScrollbarHeight() {
        int h = 0;
        if (scrollbarDisplayPolicy != SCROLLBARS_NEVER) {
            ScrollPanePeer peer = (ScrollPanePeer)this.peer;
            if (peer != null) {
                h = peer.getHScrollbarHeight();
            }
        }
        return h;
    }

    /**
     * 返回垂直滚动条占用的宽度，无论它当前是否由滚动窗格显示。
     * @return 以像素为单位的垂直滚动条的宽度
     */
    public int getVScrollbarWidth() {
        int w = 0;
        if (scrollbarDisplayPolicy != SCROLLBARS_NEVER) {
            ScrollPanePeer peer = (ScrollPanePeer)this.peer;
            if (peer != null) {
                w = peer.getVScrollbarWidth();
            }
        }
        return w;
    }

    /**
     * 返回表示垂直滚动条状态的 <code>ScrollPaneAdjustable</code> 对象。
     * 该方法的声明返回类型为 <code>Adjustable</code> 以保持向后兼容性。
     * @see java.awt.ScrollPaneAdjustable
     */
    public Adjustable getVAdjustable() {
        return vAdjustable;
    }

    /**
     * 返回表示水平滚动条状态的 <code>ScrollPaneAdjustable</code> 对象。
     * 该方法的声明返回类型为 <code>Adjustable</code> 以保持向后兼容性。
     * @see java.awt.ScrollPaneAdjustable
     */
    public Adjustable getHAdjustable() {
        return hAdjustable;
    }

    /**
     * 滚动到子组件内的指定位置。
     * 仅当滚动窗格包含子组件时，调用此方法才有效。指定一个超出子组件合法滚动范围的位置将滚动到最近的合法位置。
     * 合法范围定义为矩形：
     * x = 0, y = 0, width = (子组件宽度 - 视口宽度),
     * height = (子组件高度 - 视口高度)。
     * 这是一个方便的方法，与表示滚动条状态的 Adjustable 对象进行交互。
     * @param x 要滚动到的 x 位置
     * @param y 要滚动到的 y 位置
     * @throws NullPointerException 如果滚动窗格不包含子组件
     */
    public void setScrollPosition(int x, int y) {
        synchronized (getTreeLock()) {
            if (getComponentCount()==0) {
                throw new NullPointerException("子组件为 null");
            }
            hAdjustable.setValue(x);
            vAdjustable.setValue(y);
        }
    }

    /**
     * 滚动到子组件内的指定位置。
     * 仅当滚动窗格包含子组件且指定位置在子组件的合法滚动范围内时，调用此方法才有效。指定一个超出子组件合法滚动范围的位置将滚动到最近的合法位置。
     * 合法范围定义为矩形：
     * x = 0, y = 0, width = (子组件宽度 - 视口宽度),
     * height = (子组件高度 - 视口高度)。
     * 这是一个方便的方法，与表示滚动条状态的 Adjustable 对象进行交互。
     * @param p 表示要滚动到的位置的 Point
     * @throws NullPointerException 如果 {@code p} 为 {@code null}
     */
    public void setScrollPosition(Point p) {
        setScrollPosition(p.x, p.y);
    }

    /**
     * 返回子组件中当前显示在滚动面板视口 0,0 位置的 x,y 位置。
     * 这是一个方便的方法，与表示滚动条状态的 Adjustable 对象进行交互。
     * @return 当前滚动位置的坐标
     * @throws NullPointerException 如果滚动窗格不包含子组件
     */
    @Transient
    public Point getScrollPosition() {
        synchronized (getTreeLock()) {
            if (getComponentCount()==0) {
                throw new NullPointerException("子组件为 null");
            }
            return new Point(hAdjustable.getValue(), vAdjustable.getValue());
        }
    }

    /**
     * 为该容器设置布局管理器。此方法被重写以防止设置布局管理器。
     * @param mgr 指定的布局管理器
     */
    public final void setLayout(LayoutManager mgr) {
        throw new AWTError("ScrollPane 控制布局");
    }


                /**
     * Lays out this container by resizing its child to its preferred size.
     * 如果新的首选大小导致当前滚动位置无效，则将滚动位置设置为最近的有效位置。
     *
     * @see Component#validate
     */
    public void doLayout() {
        layout();
    }

    /**
     * 确定分配给子组件的大小。
     * 如果视口区域大于子组件的首选大小，则子组件将被分配足够的大小以填充视口；
     * 否则，子组件将获得其首选大小。
     */
    Dimension calculateChildSize() {
        //
        // 计算视图大小，考虑边框但不考虑滚动条
        // - 不使用右/底内边距，因为它们根据上次调整大小时是否显示滚动条而变化
        //
        Dimension       size = getSize();
        Insets          insets = getInsets();
        int             viewWidth = size.width - insets.left*2;
        int             viewHeight = size.height - insets.top*2;

        //
        // 确定是否显示水平或垂直滚动条
        //
        boolean vbarOn;
        boolean hbarOn;
        Component child = getComponent(0);
        Dimension childSize = new Dimension(child.getPreferredSize());

        if (scrollbarDisplayPolicy == SCROLLBARS_AS_NEEDED) {
            vbarOn = childSize.height > viewHeight;
            hbarOn = childSize.width  > viewWidth;
        } else if (scrollbarDisplayPolicy == SCROLLBARS_ALWAYS) {
            vbarOn = hbarOn = true;
        } else { // SCROLLBARS_NEVER
            vbarOn = hbarOn = false;
        }

        //
        // 调整预测的视图大小以考虑滚动条
        //
        int vbarWidth = getVScrollbarWidth();
        int hbarHeight = getHScrollbarHeight();
        if (vbarOn) {
            viewWidth -= vbarWidth;
        }
        if(hbarOn) {
            viewHeight -= hbarHeight;
        }

        //
        // 如果子组件小于视图，则将其放大
        //
        if (childSize.width < viewWidth) {
            childSize.width = viewWidth;
        }
        if (childSize.height < viewHeight) {
            childSize.height = viewHeight;
        }

        return childSize;
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 被 <code>doLayout()</code> 替代。
     */
    @Deprecated
    public void layout() {
        if (getComponentCount()==0) {
            return;
        }
        Component c = getComponent(0);
        Point p = getScrollPosition();
        Dimension cs = calculateChildSize();
        Dimension vs = getViewportSize();

        c.reshape(- p.x, - p.y, cs.width, cs.height);
        ScrollPanePeer peer = (ScrollPanePeer)this.peer;
        if (peer != null) {
            peer.childResized(cs.width, cs.height);
        }

        // 更新调整器... 由于滚动条的显示或隐藏，视口大小可能已改变，因此在更新调整器之前先更新视口大小。
        vs = getViewportSize();
        hAdjustable.setSpan(0, cs.width, vs.width);
        vAdjustable.setSpan(0, cs.height, vs.height);
    }

    /**
     * 打印此滚动窗格中的组件。
     * @param g 指定的 Graphics 窗口
     * @see Component#print
     * @see Component#printAll
     */
    public void printComponents(Graphics g) {
        if (getComponentCount()==0) {
            return;
        }
        Component c = getComponent(0);
        Point p = c.getLocation();
        Dimension vs = getViewportSize();
        Insets i = getInsets();

        Graphics cg = g.create();
        try {
            cg.clipRect(i.left, i.top, vs.width, vs.height);
            cg.translate(p.x, p.y);
            c.printAll(cg);
        } finally {
            cg.dispose();
        }
    }

    /**
     * 创建滚动窗格的对等体。
     */
    public void addNotify() {
        synchronized (getTreeLock()) {

            int vAdjustableValue = 0;
            int hAdjustableValue = 0;

            // Bug 4124460. 保存当前的调整器值，
            // 以便在 addnotify 之后恢复。将调整器设置为 0，以防止可能的负值导致的崩溃。
            if (getComponentCount() > 0) {
                vAdjustableValue = vAdjustable.getValue();
                hAdjustableValue = hAdjustable.getValue();
                vAdjustable.setValue(0);
                hAdjustable.setValue(0);
            }

            if (peer == null)
                peer = getToolkit().createScrollPane(this);
            super.addNotify();

            // Bug 4124460. 恢复调整器值。
            if (getComponentCount() > 0) {
                vAdjustable.setValue(vAdjustableValue);
                hAdjustable.setValue(hAdjustableValue);
            }
        }
    }

    /**
     * 返回表示此 <code>ScrollPane</code> 状态的字符串。
     * 此方法仅用于调试目的，返回字符串的内容和格式可能在不同实现中有所不同。
     * 返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return 此滚动窗格的参数字符串
     */
    public String paramString() {
        String sdpStr;
        switch (scrollbarDisplayPolicy) {
            case SCROLLBARS_AS_NEEDED:
                sdpStr = "as-needed";
                break;
            case SCROLLBARS_ALWAYS:
                sdpStr = "always";
                break;
            case SCROLLBARS_NEVER:
                sdpStr = "never";
                break;
            default:
                sdpStr = "invalid display policy";
        }
        Point p = (getComponentCount()>0)? getScrollPosition() : new Point(0,0);
        Insets i = getInsets();
        return super.paramString()+",ScrollPosition=("+p.x+","+p.y+")"+
            ",Insets=("+i.top+","+i.left+","+i.bottom+","+i.right+")"+
            ",ScrollbarDisplayPolicy="+sdpStr+
        ",wheelScrollingEnabled="+isWheelScrollingEnabled();
    }

    void autoProcessMouseWheel(MouseWheelEvent e) {
        processMouseWheelEvent(e);
    }

    /**
     * 处理传递给此 <code>ScrollPane</code> 的鼠标滚轮事件，通过适当的滚动量进行滚动。
     * <p>注意，如果事件参数为 <code>null</code>，则行为未指定，可能会导致异常。
     *
     * @param e  鼠标滚轮事件
     * @since 1.4
     */
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        if (isWheelScrollingEnabled()) {
            ScrollPaneWheelScroller.handleWheelScrolling(this, e);
            e.consume();
        }
        super.processMouseWheelEvent(e);
    }

    /**
     * 如果启用了滚轮滚动，则返回 true 对于 MouseWheelEvents
     * @since 1.4
     */
    protected boolean eventTypeEnabled(int type) {
        if (type == MouseEvent.MOUSE_WHEEL && isWheelScrollingEnabled()) {
            return true;
        }
        else {
            return super.eventTypeEnabled(type);
        }
    }

    /**
     * 启用/禁用响应鼠标滚轮移动的滚动。
     * 默认情况下启用滚轮滚动。
     *
     * @param handleWheel   <code>true</code> 表示应自动为 MouseWheelEvent 进行滚动，
     *                      <code>false</code> 表示不进行滚动。
     * @see #isWheelScrollingEnabled
     * @see java.awt.event.MouseWheelEvent
     * @see java.awt.event.MouseWheelListener
     * @since 1.4
     */
    public void setWheelScrollingEnabled(boolean handleWheel) {
        wheelScrollingEnabled = handleWheel;
    }

    /**
     * 指示是否响应鼠标滚轮进行滚动。默认情况下启用滚轮滚动。
     *
     * @see #setWheelScrollingEnabled(boolean)
     * @since 1.4
     */
    public boolean isWheelScrollingEnabled() {
        return wheelScrollingEnabled;
    }


    /**
     * 将默认的可序列化字段写入流。
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        // 4352819: 我们只需要这个简化的 writeObject 来确保未来版本的此类可以将可选数据写入流。
        s.defaultWriteObject();
    }

    /**
     * 从流中读取默认的可序列化字段。
     * @exception HeadlessException 如果
     * <code>GraphicsEnvironment.isHeadless()</code> 返回
     * <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException, HeadlessException
    {
        GraphicsEnvironment.checkHeadless();
        // 4352819: 注意！不能在这里使用 s.defaultReadObject 然后继续读取可选数据。使用 GetField 代替。
        ObjectInputStream.GetField f = s.readFields();

        // 旧字段
        scrollbarDisplayPolicy = f.get("scrollbarDisplayPolicy",
                                       SCROLLBARS_AS_NEEDED);
        hAdjustable = (ScrollPaneAdjustable)f.get("hAdjustable", null);
        vAdjustable = (ScrollPaneAdjustable)f.get("vAdjustable", null);

        // 自 1.4 版本起
        wheelScrollingEnabled = f.get("wheelScrollingEnabled",
                                      defaultWheelScroll);

//      // 未来维护者的注意事项
//      if (f.defaulted("wheelScrollingEnabled")) {
//          // 我们正在读取 1.4 之前的流，该流没有可选数据，甚至没有 TC_ENDBLOCKDATA 标记。
//          // 从此点之后读取任何内容都是不安全的，因为我们将读取流中进一步的无关对象 (4352819)。
//      }
//      else {
//          // 从 1.4 或更高版本读取数据，尝试读取可选数据是安全的，因为 OptionalDataException 会正确报告 eof == true
//      }
    }

    class PeerFixer implements AdjustmentListener, java.io.Serializable
    {
        private static final long serialVersionUID = 1043664721353696630L;

        PeerFixer(ScrollPane scroller) {
            this.scroller = scroller;
        }

        /**
         * 当调整器的值发生变化时调用。
         */
        public void adjustmentValueChanged(AdjustmentEvent e) {
            Adjustable adj = e.getAdjustable();
            int value = e.getValue();
            ScrollPanePeer peer = (ScrollPanePeer) scroller.peer;
            if (peer != null) {
                peer.setValue(adj, value);
            }

            Component c = scroller.getComponent(0);
            switch(adj.getOrientation()) {
            case Adjustable.VERTICAL:
                c.move(c.getLocation().x, -(value));
                break;
            case Adjustable.HORIZONTAL:
                c.move(-(value), c.getLocation().y);
                break;
            default:
                throw new IllegalArgumentException("非法的调整器方向");
            }
        }

        private ScrollPane scroller;
    }


/////////////////
// Accessibility support
////////////////

    /**
     * 获取与此 ScrollPane 关联的 AccessibleContext。
     * 对于滚动窗格，AccessibleContext 采用 AccessibleAWTScrollPane 的形式。
     * 如果需要，将创建一个新的 AccessibleAWTScrollPane 实例。
     *
     * @return 一个 AccessibleAWTScrollPane，作为此 ScrollPane 的 AccessibleContext
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTScrollPane();
        }
        return accessibleContext;
    }

    /**
     * 为 <code>ScrollPane</code> 类实现辅助功能支持。
     * 它为滚动窗格用户界面元素提供了适当的 Java 辅助功能 API 实现。
     * @since 1.3
     */
    protected class AccessibleAWTScrollPane extends AccessibleAWTContainer
    {
        /*
         * JDK 1.3 serialVersionUID
         */
        private static final long serialVersionUID = 6100703663886637L;

        /**
         * 获取此对象的角色。
         *
         * @return 一个 AccessibleRole 实例，描述对象的角色
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SCROLL_PANE;
        }

    } // class AccessibleAWTScrollPane

}

/*
 * 在 JDK 1.1.1 中，pkg 私有类 java.awt.PeerFixer 被移动到 ScrollPane 的内部类，这破坏了使用 JDK 1.1 序列化的 ScrollPane 对象。
 * 为了不将其移回，这将破坏所有 JDK 1.1.x 版本，我们保持 PeerFixer 在两个地方。由于作用域规则，
 * 在 ScrollPane 中使用的 PeerFixer 将是内部类。下面的 pkg 私有 PeerFixer 类仅在使用 Java 2 平台反序列化使用 JDK1.1 序列化的 ScrollPane 对象时使用。
 */
class PeerFixer implements AdjustmentListener, java.io.Serializable {
    /*
     * serialVersionUID
     */
    private static final long serialVersionUID = 7051237413532574756L;

    PeerFixer(ScrollPane scroller) {
        this.scroller = scroller;
    }

    /**
     * 当调整器的值发生变化时调用。
     */
    public void adjustmentValueChanged(AdjustmentEvent e) {
        Adjustable adj = e.getAdjustable();
        int value = e.getValue();
        ScrollPanePeer peer = (ScrollPanePeer) scroller.peer;
        if (peer != null) {
            peer.setValue(adj, value);
        }

        Component c = scroller.getComponent(0);
        switch(adj.getOrientation()) {
        case Adjustable.VERTICAL:
            c.move(c.getLocation().x, -(value));
            break;
        case Adjustable.HORIZONTAL:
            c.move(-(value), c.getLocation().y);
            break;
        default:
            throw new IllegalArgumentException("非法的调整器方向");
        }
    }

    private ScrollPane scroller;
}
