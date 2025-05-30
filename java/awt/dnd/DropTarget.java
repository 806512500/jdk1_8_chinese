
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.dnd;

import java.util.TooManyListenersException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.datatransfer.FlavorMap;
import java.awt.datatransfer.SystemFlavorMap;
import javax.swing.Timer;
import java.awt.peer.ComponentPeer;
import java.awt.peer.LightweightPeer;
import java.awt.dnd.peer.DropTargetPeer;


/**
 * <code>DropTarget</code> 与一个 <code>Component</code> 关联，当该 <code>Component</code>
 * 希望在拖放操作期间接受拖放时。
 * <P>
 * 每个
 * <code>DropTarget</code> 都与一个 <code>FlavorMap</code> 关联。
 * 默认的 <code>FlavorMap</code> 指的是由 <code>SystemFlavorMap.getDefaultFlavorMap()</code> 返回的
 * <code>FlavorMap</code>。
 *
 * @since 1.2
 */

public class DropTarget implements DropTargetListener, Serializable {

    private static final long serialVersionUID = -6283860791671019047L;

    /**
     * 给定要与其关联的 <code>Component</code>、表示默认可接受操作的 <code>int</code>、
     * 用于处理事件的 <code>DropTargetListener</code>、指示 <code>DropTarget</code> 是否当前接受拖放的 <code>boolean</code> 以及
     * 要使用的 <code>FlavorMap</code>（或 null 表示默认 <CODE>FlavorMap</CODE>），创建一个新的 <code>DropTarget</code>。
     * <P>
     * 仅当组件启用时，才会接收拖放。
     * @param c         与此 <code>DropTarget</code> 关联的 <code>Component</code>
     * @param ops       此 <code>DropTarget</code> 的默认可接受操作
     * @param dtl       此 <code>DropTarget</code> 的 <code>DropTargetListener</code>
     * @param act       <code>DropTarget</code> 是否接受拖放。
     * @param fm        要使用的 <code>FlavorMap</code>，或 null 表示默认 <CODE>FlavorMap</CODE>
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     *            返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public DropTarget(Component c, int ops, DropTargetListener dtl,
                      boolean act, FlavorMap fm)
        throws HeadlessException
    {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }

        component = c;

        setDefaultActions(ops);

        if (dtl != null) try {
            addDropTargetListener(dtl);
        } catch (TooManyListenersException tmle) {
            // do nothing!
        }

        if (c != null) {
            c.setDropTarget(this);
            setActive(act);
        }

        if (fm != null) {
            flavorMap = fm;
        } else {
            flavorMap = SystemFlavorMap.getDefaultFlavorMap();
        }
    }

    /**
     * 给定要与其关联的 <code>Component</code>、表示默认可接受操作的 <code>int</code>、
     * 用于处理事件的 <code>DropTargetListener</code> 以及指示 <code>DropTarget</code> 是否当前接受拖放的 <code>boolean</code>，
     * 创建一个 <code>DropTarget</code>。
     * <P>
     * 仅当组件启用时，才会接收拖放。
     * @param c         与此 <code>DropTarget</code> 关联的 <code>Component</code>
     * @param ops       此 <code>DropTarget</code> 的默认可接受操作
     * @param dtl       此 <code>DropTarget</code> 的 <code>DropTargetListener</code>
     * @param act       <code>DropTarget</code> 是否接受拖放。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     *            返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public DropTarget(Component c, int ops, DropTargetListener dtl,
                      boolean act)
        throws HeadlessException
    {
        this(c, ops, dtl, act, null);
    }

    /**
     * 创建一个 <code>DropTarget</code>。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     *            返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public DropTarget() throws HeadlessException {
        this(null, DnDConstants.ACTION_COPY_OR_MOVE, null, true, null);
    }

    /**
     * 给定要与其关联的 <code>Component</code> 以及用于处理事件的 <code>DropTargetListener</code>，
     * 创建一个 <code>DropTarget</code>。
     * <P>
     * 仅当组件启用时，才会接收拖放。
     * @param c         与此 <code>DropTarget</code> 关联的 <code>Component</code>
     * @param dtl       此 <code>DropTarget</code> 的 <code>DropTargetListener</code>
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     *            返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public DropTarget(Component c, DropTargetListener dtl)
        throws HeadlessException
    {
        this(c, DnDConstants.ACTION_COPY_OR_MOVE, dtl, true, null);
    }

    /**
     * 给定要与其关联的 <code>Component</code>、表示默认可接受操作的 <code>int</code> 以及
     * 用于处理事件的 <code>DropTargetListener</code>，创建一个 <code>DropTarget</code>。
     * <P>
     * 仅当组件启用时，才会接收拖放。
     * @param c         与此 <code>DropTarget</code> 关联的 <code>Component</code>
     * @param ops       此 <code>DropTarget</code> 的默认可接受操作
     * @param dtl       此 <code>DropTarget</code> 的 <code>DropTargetListener</code>
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     *            返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public DropTarget(Component c, int ops, DropTargetListener dtl)
        throws HeadlessException
    {
        this(c, ops, dtl, true);
    }

    /**
     * 注意：此接口允许以两种方式之一安全地将 <code>DropTarget</code> 与 <code>Component</code> 关联，即：
     * <code> component.setDropTarget(droptarget); </code>
     * 或 <code> droptarget.setComponent(component); </code>
     * <P>
     * 仅当组件启用时，才会接收拖放。
     * @param c 要与此 <code>DropTarget</code> 关联的新 <code>Component</code>。
     */

    public synchronized void setComponent(Component c) {
        if (component == c || component != null && component.equals(c))
            return;

        Component     old;
        ComponentPeer oldPeer = null;

        if ((old = component) != null) {
            clearAutoscroll();

            component = null;

            if (componentPeer != null) {
                oldPeer = componentPeer;
                removeNotify(componentPeer);
            }

            old.setDropTarget(null);

        }

        if ((component = c) != null) try {
            c.setDropTarget(this);
        } catch (Exception e) { // undo the change
            if (old != null) {
                old.setDropTarget(this);
                addNotify(oldPeer);
            }
        }
    }

    /**
     * 获取与此 <code>DropTarget</code> 关联的 <code>Component</code>。
     * <P>
     * @return 当前的 <code>Component</code>
     */

    public synchronized Component getComponent() {
        return component;
    }

    /**
     * 设置此 <code>DropTarget</code> 的默认可接受操作
     * <P>
     * @param ops 默认操作
     * @see java.awt.dnd.DnDConstants
     */

    public void setDefaultActions(int ops) {
        getDropTargetContext().setTargetActions(ops & (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE));
    }

    /*
     * 由 DropTargetContext.setTargetActions() 调用，并进行适当的同步。
     */
    void doSetDefaultActions(int ops) {
        actions = ops;
    }

    /**
     * 获取一个表示此 <code>DropTarget</code> 当前支持的操作的 <code>int</code>。
     * <P>
     * @return 当前的默认操作
     */

    public int getDefaultActions() {
        return actions;
    }

    /**
     * 如果为 <code>true</code>，则设置 <code>DropTarget</code> 为活动状态，如果为 <code>false</code>，则设置为非活动状态。
     * <P>
     * @param isActive 设置 <code>DropTarget</code> 为（不）活动。
     */

    public synchronized void setActive(boolean isActive) {
        if (isActive != active) {
            active = isActive;
        }

        if (!active) clearAutoscroll();
    }

    /**
     * 报告此 <code>DropTarget</code> 是否当前处于活动状态（准备好接受拖放）。
     * <P>
     * @return <CODE>true</CODE> 如果活动，<CODE>false</CODE> 如果不活动
     */

    public boolean isActive() {
        return active;
    }

    /**
     * 添加一个新的 <code>DropTargetListener</code>（单播源）。
     * <P>
     * @param dtl 新的 <code>DropTargetListener</code>
     * <P>
     * @throws TooManyListenersException 如果一个
     * <code>DropTargetListener</code> 已经添加到此
     * <code>DropTarget</code>。
     */

    public synchronized void addDropTargetListener(DropTargetListener dtl) throws TooManyListenersException {
        if (dtl == null) return;

        if (equals(dtl)) throw new IllegalArgumentException("DropTarget 不能是自己的监听器");

        if (dtListener == null)
            dtListener = dtl;
        else
            throw new TooManyListenersException();
    }

    /**
     * 移除当前的 <code>DropTargetListener</code>（单播源）。
     * <P>
     * @param dtl 要注销的 DropTargetListener。
     */

    public synchronized void removeDropTargetListener(DropTargetListener dtl) {
        if (dtl != null && dtListener != null) {
            if(dtListener.equals(dtl))
                dtListener = null;
            else
                throw new IllegalArgumentException("监听器不匹配");
        }
    }

    /**
     * 调用注册的
     * <code>DropTargetListener</code> 的 <code>dragEnter</code> 方法，并传递指定的 <code>DropTargetDragEvent</code>。
     * 如果此 <code>DropTarget</code> 不处于活动状态，则没有效果。
     *
     * @param dtde 指定的 <code>DropTargetDragEvent</code>
     *
     * @throws NullPointerException 如果此 <code>DropTarget</code>
     *         处于活动状态且 <code>dtde</code> 为 <code>null</code>
     *
     * @see #isActive
     */
    public synchronized void dragEnter(DropTargetDragEvent dtde) {
        isDraggingInside = true;

        if (!active) return;

        if (dtListener != null) {
            dtListener.dragEnter(dtde);
        } else
            dtde.getDropTargetContext().setTargetActions(DnDConstants.ACTION_NONE);

        initializeAutoscrolling(dtde.getLocation());
    }

    /**
     * 调用注册的
     * <code>DropTargetListener</code> 的 <code>dragOver</code> 方法，并传递指定的 <code>DropTargetDragEvent</code>。
     * 如果此 <code>DropTarget</code> 不处于活动状态，则没有效果。
     *
     * @param dtde 指定的 <code>DropTargetDragEvent</code>
     *
     * @throws NullPointerException 如果此 <code>DropTarget</code>
     *         处于活动状态且 <code>dtde</code> 为 <code>null</code>
     *
     * @see #isActive
     */
    public synchronized void dragOver(DropTargetDragEvent dtde) {
        if (!active) return;

        if (dtListener != null && active) dtListener.dragOver(dtde);

        updateAutoscroll(dtde.getLocation());
    }

    /**
     * 调用注册的
     * <code>DropTargetListener</code> 的 <code>dropActionChanged</code> 方法，并传递指定的 <code>DropTargetDragEvent</code>。
     * 如果此 <code>DropTarget</code> 不处于活动状态，则没有效果。
     *
     * @param dtde 指定的 <code>DropTargetDragEvent</code>
     *
     * @throws NullPointerException 如果此 <code>DropTarget</code>
     *         处于活动状态且 <code>dtde</code> 为 <code>null</code>
     *
     * @see #isActive
     */
    public synchronized void dropActionChanged(DropTargetDragEvent dtde) {
        if (!active) return;

        if (dtListener != null) dtListener.dropActionChanged(dtde);

        updateAutoscroll(dtde.getLocation());
    }

    /**
     * 调用注册的
     * <code>DropTargetListener</code> 的 <code>dragExit</code> 方法，并传递指定的 <code>DropTargetEvent</code>。
     * 如果此 <code>DropTarget</code> 不处于活动状态，则没有效果。
     * <p>
     * 此方法本身不会因参数为 null 而抛出任何异常，但会抛出监听器相应方法抛出的异常。
     *
     * @param dte 指定的 <code>DropTargetEvent</code>
     *
     * @see #isActive
     */
    public synchronized void dragExit(DropTargetEvent dte) {
        isDraggingInside = false;

        if (!active) return;

        if (dtListener != null && active) dtListener.dragExit(dte);

        clearAutoscroll();
    }

    /**
     * 调用注册的
     * <code>DropTargetListener</code> 的 <code>drop</code> 方法，并传递指定的 <code>DropTargetDropEvent</code>
     * 如果此 <code>DropTarget</code> 处于活动状态。
     *
     * @param dtde 指定的 <code>DropTargetDropEvent</code>
     *
     * @throws NullPointerException 如果 <code>dtde</code> 为 null
     *         且以下任一条件为真：此
     *         <code>DropTarget</code> 不处于活动状态，或没有注册 <code>DropTargetListener</code>。
     *
     * @see #isActive
     */
    public synchronized void drop(DropTargetDropEvent dtde) {
        isDraggingInside = false;


                    clearAutoscroll();

        if (dtListener != null && active)
            dtListener.drop(dtde);
        else { // we should'nt get here ...
            dtde.rejectDrop();
        }
    }

    /**
     * 获取与此 <code>DropTarget</code> 关联的 <code>FlavorMap</code>。
     * 如果没有为该 <code>DropTarget</code> 设置 <code>FlavorMap</code>，则关联默认的 <code>FlavorMap</code>。
     * <P>
     * @return 此 DropTarget 的 FlavorMap
     */

    public FlavorMap getFlavorMap() { return flavorMap; }

    /**
     * 设置与此 <code>DropTarget</code> 关联的 <code>FlavorMap</code>。
     * <P>
     * @param fm 新的 <code>FlavorMap</code>，或 null 以关联默认的 FlavorMap 与此 DropTarget。
     */

    public void setFlavorMap(FlavorMap fm) {
        flavorMap = fm == null ? SystemFlavorMap.getDefaultFlavorMap() : fm;
    }

    /**
     * 通知 DropTarget 它已被关联到一个 Component
     *
     **********************************************************************
     * 该方法通常由与此 DropTarget 关联的 Component 的 java.awt.Component.addNotify() 调用，以通知 DropTarget
     * 一个 ComponentPeer 已与该 Component 关联。
     *
     * 除通知此 DropTarget 关联 ComponentPeer 与 Component 外，调用此方法可能导致 DnD 系统故障。
     **********************************************************************
     * <P>
     * @param peer 我们关联的 Component 的 Peer!
     *
     */

    public void addNotify(ComponentPeer peer) {
        if (peer == componentPeer) return;

        componentPeer = peer;

        for (Component c = component;
             c != null && peer instanceof LightweightPeer; c = c.getParent()) {
            peer = c.getPeer();
        }

        if (peer instanceof DropTargetPeer) {
            nativePeer = peer;
            ((DropTargetPeer)peer).addDropTarget(this);
        } else {
            nativePeer = null;
        }
    }

    /**
     * 通知 DropTarget 它已被解除与一个 Component 的关联
     *
     **********************************************************************
     * 该方法通常由与此 DropTarget 关联的 Component 的 java.awt.Component.removeNotify() 调用，以通知 DropTarget
     * 一个 ComponentPeer 已与该 Component 解除关联。
     *
     * 除通知此 DropTarget 解除 ComponentPeer 与 Component 的关联外，调用此方法可能导致 DnD 系统故障。
     **********************************************************************
     * <P>
     * @param peer 我们解除关联的 Component 的 Peer!
     */

    public void removeNotify(ComponentPeer peer) {
        if (nativePeer != null)
            ((DropTargetPeer)nativePeer).removeDropTarget(this);

        componentPeer = nativePeer = null;

        synchronized (this) {
            if (isDraggingInside) {
                dragExit(new DropTargetEvent(getDropTargetContext()));
            }
        }
    }

    /**
     * 获取与此 <code>DropTarget</code> 关联的 <code>DropTargetContext</code>。
     * <P>
     * @return 与此 <code>DropTarget</code> 关联的 <code>DropTargetContext</code>。
     */

    public DropTargetContext getDropTargetContext() {
        return dropTargetContext;
    }

    /**
     * 创建与此 DropTarget 关联的 DropTargetContext。
     * 子类可以重写此方法以实例化自己的 DropTargetContext 子类。
     *
     * 通常，此调用仅由平台的 DropTargetContextPeer 在拖动操作遇到此 DropTarget 时调用。
     * 在没有当前拖动操作的情况下访问 Context 会导致未定义的结果。
     */

    protected DropTargetContext createDropTargetContext() {
        return new DropTargetContext(this);
    }

    /**
     * 序列化此 <code>DropTarget</code>。执行默认序列化，
     * 并写入此对象的 <code>DropTargetListener</code>，如果它可以被序列化的话。
     * 如果不能，写入 <code>null</code>。
     *
     * @serialData 默认的可序列化字段，按字母顺序排列，
     *             后跟 <code>DropTargetListener</code> 实例或 <code>null</code>。
     * @since 1.4
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

        s.writeObject(SerializationTester.test(dtListener)
                      ? dtListener : null);
    }

    /**
     * 反序列化此 <code>DropTarget</code>。首先对所有非 <code>transient</code> 字段执行默认反序列化。
     * 然后尝试反序列化此对象的 <code>DropTargetListener</code>。
     * 首先尝试反序列化字段 <code>dtListener</code>，因为在 1.4 之前的版本中，一个名为此的非 <code>transient</code> 字段存储了 <code>DropTargetListener</code>。
     * 如果失败，使用流中的下一个对象。
     *
     * @since 1.4
     */
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException
    {
        ObjectInputStream.GetField f = s.readFields();

        try {
            dropTargetContext =
                (DropTargetContext)f.get("dropTargetContext", null);
        } catch (IllegalArgumentException e) {
            // Pre-1.4 支持。'dropTargetContext' 以前是 transient
        }
        if (dropTargetContext == null) {
            dropTargetContext = createDropTargetContext();
        }

        component = (Component)f.get("component", null);
        actions = f.get("actions", DnDConstants.ACTION_COPY_OR_MOVE);
        active = f.get("active", true);

        // Pre-1.4 支持。'dtListener' 以前是非 transient
        try {
            dtListener = (DropTargetListener)f.get("dtListener", null);
        } catch (IllegalArgumentException e) {
            // 1.4 兼容的字节流。'dtListener' 显式写入
            dtListener = (DropTargetListener)s.readObject();
        }
    }

    /*********************************************************************/

    /**
     * 此受保护的嵌套类实现自动滚动
     */

    protected static class DropTargetAutoScroller implements ActionListener {

        /**
         * 构造一个 DropTargetAutoScroller
         * <P>
         * @param c <code>Component</code>
         * @param p <code>Point</code>
         */

        protected DropTargetAutoScroller(Component c, Point p) {
            super();

            component  = c;
            autoScroll = (Autoscroll)component;

            Toolkit t  = Toolkit.getDefaultToolkit();

            Integer    initial  = Integer.valueOf(100);
            Integer    interval = Integer.valueOf(100);

            try {
                initial = (Integer)t.getDesktopProperty("DnD.Autoscroll.initialDelay");
            } catch (Exception e) {
                // 忽略
            }

            try {
                interval = (Integer)t.getDesktopProperty("DnD.Autoscroll.interval");
            } catch (Exception e) {
                // 忽略
            }

            timer  = new Timer(interval.intValue(), this);

            timer.setCoalesce(true);
            timer.setInitialDelay(initial.intValue());

            locn = p;
            prev = p;

            try {
                hysteresis = ((Integer)t.getDesktopProperty("DnD.Autoscroll.cursorHysteresis")).intValue();
            } catch (Exception e) {
                // 忽略
            }

            timer.start();
        }

        /**
         * 更新自动滚动区域的几何形状
         */

        private void updateRegion() {
           Insets    i    = autoScroll.getAutoscrollInsets();
           Dimension size = component.getSize();

           if (size.width != outer.width || size.height != outer.height)
                outer.reshape(0, 0, size.width, size.height);

           if (inner.x != i.left || inner.y != i.top)
                inner.setLocation(i.left, i.top);

           int newWidth  = size.width -  (i.left + i.right);
           int newHeight = size.height - (i.top  + i.bottom);

           if (newWidth != inner.width || newHeight != inner.height)
                inner.setSize(newWidth, newHeight);

        }

        /**
         * 引发自动滚动
         * <P>
         * @param newLocn <code>Point</code>
         */

        protected synchronized void updateLocation(Point newLocn) {
            prev = locn;
            locn = newLocn;

            if (Math.abs(locn.x - prev.x) > hysteresis ||
                Math.abs(locn.y - prev.y) > hysteresis) {
                if (timer.isRunning()) timer.stop();
            } else {
                if (!timer.isRunning()) timer.start();
            }
        }

        /**
         * 停止自动滚动
         */

        protected void stop() { timer.stop(); }

        /**
         * 引发自动滚动
         * <P>
         * @param e <code>ActionEvent</code>
         */

        public synchronized void actionPerformed(ActionEvent e) {
            updateRegion();

            if (outer.contains(locn) && !inner.contains(locn))
                autoScroll.autoscroll(locn);
        }

        /*
         * 字段
         */

        private Component  component;
        private Autoscroll autoScroll;

        private Timer      timer;

        private Point      locn;
        private Point      prev;

        private Rectangle  outer = new Rectangle();
        private Rectangle  inner = new Rectangle();

        private int        hysteresis = 10;
    }

    /*********************************************************************/

    /**
     * 创建一个嵌入的自动滚动器
     * <P>
     * @param c <code>Component</code>
     * @param p <code>Point</code>
     */

    protected DropTargetAutoScroller createDropTargetAutoScroller(Component c, Point p) {
        return new DropTargetAutoScroller(c, p);
    }

    /**
     * 初始化自动滚动
     * <P>
     * @param p <code>Point</code>
     */

    protected void initializeAutoscrolling(Point p) {
        if (component == null || !(component instanceof Autoscroll)) return;

        autoScroller = createDropTargetAutoScroller(component, p);
    }

    /**
     * 使用当前光标位置更新自动滚动
     * <P>
     * @param dragCursorLocn <code>Point</code>
     */

    protected void updateAutoscroll(Point dragCursorLocn) {
        if (autoScroller != null) autoScroller.updateLocation(dragCursorLocn);
    }

    /**
     * 清除自动滚动
     */

    protected void clearAutoscroll() {
        if (autoScroller != null) {
            autoScroller.stop();
            autoScroller = null;
        }
    }

    /**
     * 与此 DropTarget 关联的 DropTargetContext。
     *
     * @serial
     */
    private DropTargetContext dropTargetContext = createDropTargetContext();

    /**
     * 与此 DropTarget 关联的 Component。
     *
     * @serial
     */
    private Component component;

    /*
     * 该 Component 的 Peer
     */
    private transient ComponentPeer componentPeer;

    /*
     * 该 Component 的 "native" Peer
     */
    private transient ComponentPeer nativePeer;


    /**
     * 此 DropTarget 支持的默认允许操作。
     *
     * @see #setDefaultActions
     * @see #getDefaultActions
     * @serial
     */
    int     actions = DnDConstants.ACTION_COPY_OR_MOVE;

    /**
     * <code>true</code> 表示 DropTarget 正在接受拖放操作。
     *
     * @serial
     */
    boolean active = true;

    /*
     * 自动滚动对象
     */

    private transient DropTargetAutoScroller autoScroller;

    /*
     * 委托
     */

    private transient DropTargetListener dtListener;

    /*
     * FlavorMap
     */

    private transient FlavorMap flavorMap;

    /*
     * 如果当前拖动在该 DropTarget 内
     */
    private transient boolean isDraggingInside;
}
