
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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.FlavorMap;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.util.EventListener;
import sun.awt.dnd.SunDragSourceContextPeer;
import sun.security.action.GetIntegerAction;


/**
 * <code>DragSource</code> 是负责启动拖放操作的实体，可以在多种场景中使用：
 * <UL>
 * <LI>每个JVM生命周期内有一个默认实例。
 * <LI>每个潜在拖动发起对象类（例如 TextField）有一个实例。[实现依赖]
 * <LI>每个特定 <code>Component</code> 实例或与 GUI 中 <code>Component</code> 实例关联的应用程序特定对象有一个实例。[实现依赖]
 * <LI>其他任意关联。[实现依赖]
 *</UL>
 *
 * 一旦获得 <code>DragSource</code>，还应获得一个 <code>DragGestureRecognizer</code>
 * 以将 <code>DragSource</code> 与特定的 <code>Component</code> 关联。
 * <P>
 * 用户手势的初始解释和随后的拖动操作的启动
 * 由实现的 <code>Component</code> 负责，通常由 <code>DragGestureRecognizer</code> 实现。
 *<P>
 * 当拖动手势发生时，应调用 <code>DragSource</code> 的
 * startDrag() 方法以处理用户的导航手势并传递拖放协议通知。
 * <code>DragSource</code> 仅允许一次拖放操作同时进行，并且
 * 会在现有操作完成之前拒绝任何进一步的 startDrag() 请求
 * 通过抛出 <code>IllegalDnDOperationException</code>。
 * <P>
 * startDrag() 方法调用 createDragSourceContext() 方法来
 * 实例化一个适当的 <code>DragSourceContext</code>
 * 并将其与 <code>DragSourceContextPeer</code> 关联。
 * <P>
 * 如果拖放系统由于某种原因无法启动拖动操作，
 * startDrag() 方法会抛出 <code>java.awt.dnd.InvalidDnDOperationException</code>
 * 以指示此类情况。通常在底层平台
 * 无法启动拖动或指定的参数无效时抛出此异常。
 * <P>
 * 请注意，在拖动期间，源在拖动开始时公开的操作集
 * 不能改变，直到操作完成。
 * 操作在操作期间相对于 <code>DragSource</code> 是常量。
 *
 * @since 1.2
 */

public class DragSource implements Serializable {

    private static final long serialVersionUID = 6236096958971414066L;

    /*
     * 加载系统默认光标
     */

    private static Cursor load(String name) {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }

        try {
            return (Cursor)Toolkit.getDefaultToolkit().getDesktopProperty(name);
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException("failed to load system cursor: " + name + " : " + e.getMessage());
        }
    }


    /**
     * 用于复制操作的默认 <code>Cursor</code>，表示当前允许放置。如果
     * <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code>，则为 <code>null</code>。
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public static final Cursor DefaultCopyDrop =
        load("DnD.Cursor.CopyDrop");

    /**
     * 用于移动操作的默认 <code>Cursor</code>，表示当前允许放置。如果
     * <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code>，则为 <code>null</code>。
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public static final Cursor DefaultMoveDrop =
        load("DnD.Cursor.MoveDrop");

    /**
     * 用于链接操作的默认 <code>Cursor</code>，表示当前允许放置。如果
     * <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code>，则为 <code>null</code>。
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public static final Cursor DefaultLinkDrop =
        load("DnD.Cursor.LinkDrop");

    /**
     * 用于复制操作的默认 <code>Cursor</code>，表示当前不允许放置。如果
     * <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code>，则为 <code>null</code>。
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public static final Cursor DefaultCopyNoDrop =
        load("DnD.Cursor.CopyNoDrop");

    /**
     * 用于移动操作的默认 <code>Cursor</code>，表示当前不允许放置。如果
     * <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code>，则为 <code>null</code>。
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public static final Cursor DefaultMoveNoDrop =
        load("DnD.Cursor.MoveNoDrop");

    /**
     * 用于链接操作的默认 <code>Cursor</code>，表示当前不允许放置。如果
     * <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code>，则为 <code>null</code>。
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public static final Cursor DefaultLinkNoDrop =
        load("DnD.Cursor.LinkNoDrop");

    private static final DragSource dflt =
        (GraphicsEnvironment.isHeadless()) ? null : new DragSource();

    /**
     * 用于序列化的内部常量。
     */
    static final String dragSourceListenerK = "dragSourceL";
    static final String dragSourceMotionListenerK = "dragSourceMotionL";

    /**
     * 获取与底层平台关联的 <code>DragSource</code> 对象。
     *
     * @return 平台 DragSource
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     *            返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public static DragSource getDefaultDragSource() {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        } else {
            return dflt;
        }
    }

    /**
     * 报告底层平台是否支持拖动图像。
     * <P>
     * @return 如果此平台支持拖动图像，则返回 true
     */

    public static boolean isDragImageSupported() {
        Toolkit t = Toolkit.getDefaultToolkit();

        Boolean supported;

        try {
            supported = (Boolean)Toolkit.getDefaultToolkit().getDesktopProperty("DnD.isDragImageSupported");

            return supported.booleanValue();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 创建一个新的 <code>DragSource</code>。
     *
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     *            返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public DragSource() throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
    }

    /**
     * 根据引发拖动的 <code>DragGestureEvent</code>、初始使用的 <code>Cursor</code>、
     * 要拖动的 <code>Image</code>、<code>Image</code> 原点与 <code>Cursor</code> 热点的偏移量、
     * 拖动的主体数据 <code>Transferable</code>、<code>DragSourceListener</code> 和 <code>FlavorMap</code>
     * 启动拖动。
     * <P>
     * @param trigger        引发拖动的 <code>DragGestureEvent</code>
     * @param dragCursor     本次拖动操作的初始 {@code Cursor}，或 {@code null} 表示默认光标处理；
     *                       有关拖动过程中光标处理机制的详细信息，请参阅 <a href="DragSourceContext.html#defaultCursor">DragSourceContext</a>
     * @param dragImage      要拖动的图像或 {@code null}
     * @param imageOffset    <code>Image</code> 原点与 <code>Cursor</code> 热点在触发瞬间的偏移量
     * @param transferable   拖动的主体数据
     * @param dsl            <code>DragSourceListener</code>
     * @param flavorMap      要使用的 <code>FlavorMap</code>，或 <code>null</code>
     * <P>
     * @throws java.awt.dnd.InvalidDnDOperationException
     *    如果拖放系统无法启动拖动操作，或用户尝试在现有拖动操作仍在执行时启动拖动
     */

    public void startDrag(DragGestureEvent   trigger,
                          Cursor             dragCursor,
                          Image              dragImage,
                          Point              imageOffset,
                          Transferable       transferable,
                          DragSourceListener dsl,
                          FlavorMap          flavorMap) throws InvalidDnDOperationException {

        SunDragSourceContextPeer.setDragDropInProgress(true);

        try {
            if (flavorMap != null) this.flavorMap = flavorMap;

            DragSourceContextPeer dscp = Toolkit.getDefaultToolkit().createDragSourceContextPeer(trigger);

            DragSourceContext     dsc = createDragSourceContext(dscp,
                                                                trigger,
                                                                dragCursor,
                                                                dragImage,
                                                                imageOffset,
                                                                transferable,
                                                                dsl
                                                                );

            if (dsc == null) {
                throw new InvalidDnDOperationException();
            }

            dscp.startDrag(dsc, dsc.getCursor(), dragImage, imageOffset); // 可能抛出异常
        } catch (RuntimeException e) {
            SunDragSourceContextPeer.setDragDropInProgress(false);
            throw e;
        }
    }

    /**
     * 根据引发拖动的 <code>DragGestureEvent</code>、初始使用的 <code>Cursor</code>、
     * 拖动的主体数据 <code>Transferable</code>、<code>DragSourceListener</code> 和 <code>FlavorMap</code>
     * 启动拖动。
     * <P>
     * @param trigger        引发拖动的 <code>DragGestureEvent</code>
     * @param dragCursor     本次拖动操作的初始 {@code Cursor}，或 {@code null} 表示默认光标处理；
     *                       有关拖动过程中光标处理机制的详细信息，请参阅 <a href="DragSourceContext.html#defaultCursor">DragSourceContext</a>
     * @param transferable   拖动的主体数据
     * @param dsl            <code>DragSourceListener</code>
     * @param flavorMap      要使用的 <code>FlavorMap</code> 或 <code>null</code>
     * <P>
     * @throws java.awt.dnd.InvalidDnDOperationException
     *    如果拖放系统无法启动拖动操作，或用户尝试在现有拖动操作仍在执行时启动拖动
     */

    public void startDrag(DragGestureEvent   trigger,
                          Cursor             dragCursor,
                          Transferable       transferable,
                          DragSourceListener dsl,
                          FlavorMap          flavorMap) throws InvalidDnDOperationException {
        startDrag(trigger, dragCursor, null, null, transferable, dsl, flavorMap);
    }

    /**
     * 根据引发拖动的 <code>DragGestureEvent</code>、初始使用的 <code>Cursor</code>、
     * 要拖动的 <code>Image</code>、<code>Image</code> 原点与 <code>Cursor</code> 热点的偏移量、
     * 拖动的主体数据和 <code>DragSourceListener</code>
     * 启动拖动。
     * <P>
     * @param trigger           引发拖动的 <code>DragGestureEvent</code>
     * @param dragCursor     本次拖动操作的初始 {@code Cursor}，或 {@code null} 表示默认光标处理；
     *                       有关拖动过程中光标处理机制的详细信息，请参阅 <a href="DragSourceContext.html#defaultCursor">DragSourceContext</a>
     * @param dragImage         要拖动的 <code>Image</code> 或 <code>null</code>
     * @param dragOffset        <code>Image</code> 原点与 <code>Cursor</code> 热点在触发瞬间的偏移量
     * @param transferable      拖动的主体数据
     * @param dsl               <code>DragSourceListener</code>
     * <P>
     * @throws java.awt.dnd.InvalidDnDOperationException
     *    如果拖放系统无法启动拖动操作，或用户尝试在现有拖动操作仍在执行时启动拖动
     */


                public void startDrag(DragGestureEvent   trigger,
                          Cursor             dragCursor,
                          Image              dragImage,
                          Point              dragOffset,
                          Transferable       transferable,
                          DragSourceListener dsl) throws InvalidDnDOperationException {
        startDrag(trigger, dragCursor, dragImage, dragOffset, transferable, dsl, null);
    }

    /**
     * 开始拖动，给定引发拖动的 <code>DragGestureEvent</code>，
     * 初始使用的 <code>Cursor</code>，
     * 拖动的主题数据 <code>Transferable</code> 和 <code>DragSourceListener</code>。
     * <P>
     * @param trigger           引发拖动的 <code>DragGestureEvent</code>
     * @param dragCursor     本次拖动操作的初始 {@code Cursor}，
     *                       或 {@code null} 表示默认的光标处理；
     *                       有关拖放过程中光标处理机制的详细信息，请参阅
     *                       <a href="DragSourceContext.html#defaultCursor">DragSourceContext</a> 类
     * @param transferable      拖动的主题数据
     * @param dsl               <code>DragSourceListener</code>
     * <P>
     * @throws java.awt.dnd.InvalidDnDOperationException
     *    如果拖放系统无法启动拖动操作，或者用户
     *    尝试在现有拖动操作仍在执行时启动拖动
     */

    public void startDrag(DragGestureEvent   trigger,
                          Cursor             dragCursor,
                          Transferable       transferable,
                          DragSourceListener dsl) throws InvalidDnDOperationException {
        startDrag(trigger, dragCursor, null, null, transferable, dsl, null);
    }

    /**
     * 创建用于处理当前拖动操作的 {@code DragSourceContext}。
     * <p>
     * 若要引入新的 <code>DragSourceContext</code> 子类，
     * 可以继承 <code>DragSource</code> 并覆盖此方法。
     * <p>
     * 如果 <code>dragImage</code> 为 <code>null</code>，则不使用图像来表示此拖动操作的拖动反馈，
     * 但不会抛出 <code>NullPointerException</code>。
     * <p>
     * 如果 <code>dsl</code> 为 <code>null</code>，则不会将拖动源监听器注册到创建的 <code>DragSourceContext</code>，
     * 但不会抛出 <code>NullPointerException</code>。
     *
     * @param dscp          本次拖动的 <code>DragSourceContextPeer</code>
     * @param dgl           触发拖动的 <code>DragGestureEvent</code>
     * @param dragCursor     本次拖动操作的初始 {@code Cursor}，
     *                       或 {@code null} 表示默认的光标处理；
     *                       有关拖放过程中光标处理机制的详细信息，请参阅
     *                       <a href="DragSourceContext.html#defaultCursor">DragSourceContext</a> 类
     * @param dragImage     要拖动的 <code>Image</code> 或 <code>null</code>
     * @param imageOffset   <code>Image</code> 原点与光标热点之间的偏移量
     * @param t             拖动的主题数据
     * @param dsl           <code>DragSourceListener</code>
     *
     * @return <code>DragSourceContext</code>
     *
     * @throws NullPointerException 如果 <code>dscp</code> 为 <code>null</code>
     * @throws NullPointerException 如果 <code>dgl</code> 为 <code>null</code>
     * @throws NullPointerException 如果 <code>dragImage</code> 不为
     *    <code>null</code> 且 <code>imageOffset</code> 为 <code>null</code>
     * @throws NullPointerException 如果 <code>t</code> 为 <code>null</code>
     * @throws IllegalArgumentException 如果触发事件关联的 <code>Component</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果触发事件的 <code>DragSource</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果触发事件的拖动操作为 <code>DnDConstants.ACTION_NONE</code>。
     * @throws IllegalArgumentException 如果触发事件关联的 <code>DragGestureRecognizer</code> 的源操作等于 <code>DnDConstants.ACTION_NONE</code>。
     */

    protected DragSourceContext createDragSourceContext(DragSourceContextPeer dscp, DragGestureEvent dgl, Cursor dragCursor, Image dragImage, Point imageOffset, Transferable t, DragSourceListener dsl) {
        return new DragSourceContext(dscp, dgl, dragCursor, dragImage, imageOffset, t, dsl);
    }

    /**
     * 此方法返回此 <code>DragSource</code> 的
     * <code>FlavorMap</code>。
     * <P>
     * @return 此 <code>DragSource</code> 的 <code>FlavorMap</code>
     */

    public FlavorMap getFlavorMap() { return flavorMap; }

    /**
     * 创建一个新的 <code>DragGestureRecognizer</code>
     * 实现指定的 <code>DragGestureRecognizer</code> 抽象子类，
     * 并在新创建的对象上设置指定的 <code>Component</code>
     * 和 <code>DragGestureListener</code>。
     * <P>
     * @param recognizerAbstractClass 请求的抽象类型
     * @param actions                 允许的源拖动操作
     * @param c                       <code>Component</code> 目标
     * @param dgl        要通知的 <code>DragGestureListener</code>
     * <P>
     * @return 新的 <code>DragGestureRecognizer</code> 或 <code>null</code>
     *    如果 <code>Toolkit.createDragGestureRecognizer</code> 方法
     *    没有可用的实现来创建请求的 <code>DragGestureRecognizer</code>
     *    子类，则返回 <code>null</code>
     */

    public <T extends DragGestureRecognizer> T
        createDragGestureRecognizer(Class<T> recognizerAbstractClass,
                                    Component c, int actions,
                                    DragGestureListener dgl)
    {
        return Toolkit.getDefaultToolkit().createDragGestureRecognizer(recognizerAbstractClass, this, c, actions, dgl);
    }


    /**
     * 创建一个新的 <code>DragGestureRecognizer</code>
     * 实现此 <code>DragSource</code> 的默认
     * <code>DragGestureRecognizer</code> 抽象子类，
     * 并在新创建的对象上设置指定的 <code>Component</code>
     * 和 <code>DragGestureListener</code>。
     *
     * 对于此 <code>DragSource</code>，默认值为 <code>MouseDragGestureRecognizer</code>。
     * <P>
     * @param c       识别器的 <code>Component</code> 目标
     * @param actions 允许的源操作
     * @param dgl     要通知的 <code>DragGestureListener</code>
     * <P>
     * @return 新的 <code>DragGestureRecognizer</code> 或 <code>null</code>
     *    如果 <code>Toolkit.createDragGestureRecognizer</code> 方法
     *    没有可用的实现来创建请求的 <code>DragGestureRecognizer</code>
     *    子类，则返回 <code>null</code>
     */

    public DragGestureRecognizer createDefaultDragGestureRecognizer(Component c, int actions, DragGestureListener dgl) {
        return Toolkit.getDefaultToolkit().createDragGestureRecognizer(MouseDragGestureRecognizer.class, this, c, actions, dgl);
    }

    /**
     * 将指定的 <code>DragSourceListener</code> 添加到此
     * <code>DragSource</code> 以在使用此 <code>DragSource</code> 启动的拖动操作期间接收拖动源事件。
     * 如果指定了 <code>null</code> 监听器，则不采取任何操作且不抛出异常。
     *
     * @param dsl 要添加的 <code>DragSourceListener</code>
     *
     * @see      #removeDragSourceListener
     * @see      #getDragSourceListeners
     * @since 1.4
     */
    public void addDragSourceListener(DragSourceListener dsl) {
        if (dsl != null) {
            synchronized (this) {
                listener = DnDEventMulticaster.add(listener, dsl);
            }
        }
    }

    /**
     * 从此 <code>DragSource</code> 中移除指定的 <code>DragSourceListener</code>。
     * 如果指定了 <code>null</code> 监听器，则不采取任何操作且不抛出异常。
     * 如果参数指定的监听器未先前添加到此 <code>DragSource</code>，则不采取任何操作且不抛出异常。
     *
     * @param dsl 要移除的 <code>DragSourceListener</code>
     *
     * @see      #addDragSourceListener
     * @see      #getDragSourceListeners
     * @since 1.4
     */
    public void removeDragSourceListener(DragSourceListener dsl) {
        if (dsl != null) {
            synchronized (this) {
                listener = DnDEventMulticaster.remove(listener, dsl);
            }
        }
    }

    /**
     * 获取注册到此 <code>DragSource</code> 的所有 <code>DragSourceListener</code>。
     *
     * @return 此 <code>DragSource</code> 的所有
     *         <code>DragSourceListener</code> 或空数组，如果没有注册这样的监听器
     *
     * @see      #addDragSourceListener
     * @see      #removeDragSourceListener
     * @since    1.4
     */
    public DragSourceListener[] getDragSourceListeners() {
        return getListeners(DragSourceListener.class);
    }

    /**
     * 将指定的 <code>DragSourceMotionListener</code> 添加到此
     * <code>DragSource</code> 以在使用此 <code>DragSource</code> 启动的拖动操作期间接收拖动运动事件。
     * 如果指定了 <code>null</code> 监听器，则不采取任何操作且不抛出异常。
     *
     * @param dsml 要添加的 <code>DragSourceMotionListener</code>
     *
     * @see      #removeDragSourceMotionListener
     * @see      #getDragSourceMotionListeners
     * @since 1.4
     */
    public void addDragSourceMotionListener(DragSourceMotionListener dsml) {
        if (dsml != null) {
            synchronized (this) {
                motionListener = DnDEventMulticaster.add(motionListener, dsml);
            }
        }
    }

    /**
     * 从此 <code>DragSource</code> 中移除指定的 <code>DragSourceMotionListener</code>。
     * 如果指定了 <code>null</code> 监听器，则不采取任何操作且不抛出异常。
     * 如果参数指定的监听器未先前添加到此 <code>DragSource</code>，则不采取任何操作且不抛出异常。
     *
     * @param dsml 要移除的 <code>DragSourceMotionListener</code>
     *
     * @see      #addDragSourceMotionListener
     * @see      #getDragSourceMotionListeners
     * @since 1.4
     */
    public void removeDragSourceMotionListener(DragSourceMotionListener dsml) {
        if (dsml != null) {
            synchronized (this) {
                motionListener = DnDEventMulticaster.remove(motionListener, dsml);
            }
        }
    }

    /**
     * 获取注册到此 <code>DragSource</code> 的所有
     * <code><em>Foo</em>Listener</code>。
     * <code><em>Foo</em>Listener</code> 通过 <code>add<em>Foo</em>Listener</code> 方法注册。
     *
     * @param listenerType 请求的监听器类型；此参数应指定一个继承自
     *          <code>java.util.EventListener</code> 的类或接口
     * @return 注册到此 <code>DragSource</code> 的所有
     *          <code><em>Foo</em>Listener</code>，或空数组，如果没有添加这样的监听器
     * @exception ClassCastException 如果 <code>listenerType</code>
     *          不指定实现 <code>java.util.EventListener</code> 的类或接口
     *
     * @see #getDragSourceListeners
     * @see #getDragSourceMotionListeners
     * @since 1.4
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if (listenerType == DragSourceListener.class) {
            l = listener;
        } else if (listenerType == DragSourceMotionListener.class) {
            l = motionListener;
        }
        return DnDEventMulticaster.getListeners(l, listenerType);
    }

    /**
     * 调用注册到此 <code>DragSource</code> 的
     * <code>DragSourceListener</code> 的 <code>dragEnter</code> 方法，
     * 并传递指定的 <code>DragSourceDragEvent</code>。
     *
     * @param dsde <code>DragSourceDragEvent</code>
     */
    void processDragEnter(DragSourceDragEvent dsde) {
        DragSourceListener dsl = listener;
        if (dsl != null) {
            dsl.dragEnter(dsde);
        }
    }

    /**
     * 调用注册到此 <code>DragSource</code> 的
     * <code>DragSourceListener</code> 的 <code>dragOver</code> 方法，
     * 并传递指定的 <code>DragSourceDragEvent</code>。
     *
     * @param dsde <code>DragSourceDragEvent</code>
     */
    void processDragOver(DragSourceDragEvent dsde) {
        DragSourceListener dsl = listener;
        if (dsl != null) {
            dsl.dragOver(dsde);
        }
    }

    /**
     * 调用注册到此 <code>DragSource</code> 的
     * <code>DragSourceListener</code> 的 <code>dropActionChanged</code> 方法，
     * 并传递指定的 <code>DragSourceDragEvent</code>。
     *
     * @param dsde <code>DragSourceDragEvent</code>
     */
    void processDropActionChanged(DragSourceDragEvent dsde) {
        DragSourceListener dsl = listener;
        if (dsl != null) {
            dsl.dropActionChanged(dsde);
        }
    }

    /**
     * 调用注册到此 <code>DragSource</code> 的
     * <code>DragSourceListener</code> 的 <code>dragExit</code> 方法，
     * 并传递指定的 <code>DragSourceEvent</code>。
     *
     * @param dse <code>DragSourceEvent</code>
     */
    void processDragExit(DragSourceEvent dse) {
        DragSourceListener dsl = listener;
        if (dsl != null) {
            dsl.dragExit(dse);
        }
    }

    /**
     * 调用注册到此 <code>DragSource</code> 的
     * <code>DragSourceListener</code> 的 <code>dragDropEnd</code> 方法，
     * 并传递指定的 <code>DragSourceDropEvent</code>。
     *
     * @param dsde <code>DragSourceEvent</code>
     */
    void processDragDropEnd(DragSourceDropEvent dsde) {
        DragSourceListener dsl = listener;
        if (dsl != null) {
            dsl.dragDropEnd(dsde);
        }
    }


                /**
     * 此方法调用注册到此 <code>DragSource</code> 的 <code>DragSourceMotionListener</code> 的 <code>dragMouseMoved</code> 方法，
     * 并将指定的 <code>DragSourceDragEvent</code> 传递给它们。
     *
     * @param dsde <code>DragSourceEvent</code>
     */
    void processDragMouseMoved(DragSourceDragEvent dsde) {
        DragSourceMotionListener dsml = motionListener;
        if (dsml != null) {
            dsml.dragMouseMoved(dsde);
        }
    }

    /**
     * 序列化此 <code>DragSource</code>。此方法首先执行默认序列化。接下来，如果此对象的 <code>FlavorMap</code> 可以被序列化，
     * 则将其写入；否则，写入 <code>null</code>。接下来，写入注册到此对象的 <code>Serializable</code> 监听器。
     * 监听器以 <code>null</code> 终止的 0 个或多个对的形式写入。对由一个 <code>String</code> 和一个 <code>Object</code> 组成；
     * <code>String</code> 表示 <code>Object</code> 的类型，可以是以下之一：
     * <ul>
     * <li><code>dragSourceListenerK</code> 表示一个 <code>DragSourceListener</code> 对象；
     * <li><code>dragSourceMotionListenerK</code> 表示一个 <code>DragSourceMotionListener</code> 对象。
     * </ul>
     *
     * @serialData 要么是一个 <code>FlavorMap</code> 实例，要么是 <code>null</code>，后跟一个 <code>null</code> 终止的
     *      0 个或多个对的序列；对由一个 <code>String</code> 和一个 <code>Object</code> 组成；<code>String</code>
     *      表示 <code>Object</code> 的类型，可以是以下之一：
     *      <ul>
     *      <li><code>dragSourceListenerK</code> 表示一个 <code>DragSourceListener</code> 对象；
     *      <li><code>dragSourceMotionListenerK</code> 表示一个 <code>DragSourceMotionListener</code> 对象。
     *      </ul>.
     * @since 1.4
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

        s.writeObject(SerializationTester.test(flavorMap) ? flavorMap : null);

        DnDEventMulticaster.save(s, dragSourceListenerK, listener);
        DnDEventMulticaster.save(s, dragSourceMotionListenerK, motionListener);
        s.writeObject(null);
    }

    /**
     * 反序列化此 <code>DragSource</code>。此方法首先执行默认反序列化。接下来，使用流中的下一个对象反序列化此对象的 <code>FlavorMap</code>。
     * 如果结果的 <code>FlavorMap</code> 为 <code>null</code>，则将此对象的 <code>FlavorMap</code> 设置为该线程的 <code>ClassLoader</code> 的默认 <code>FlavorMap</code>。
     * 接下来，通过从流中读取一个 <code>null</code> 终止的 0 个或多个键/值对的序列来反序列化此对象的监听器：
     * <ul>
     * <li>如果键对象是一个等于 <code>dragSourceListenerK</code> 的 <code>String</code>，则使用相应的值对象反序列化一个 <code>DragSourceListener</code> 并添加到此 <code>DragSource</code>。
     * <li>如果键对象是一个等于 <code>dragSourceMotionListenerK</code> 的 <code>String</code>，则使用相应的值对象反序列化一个 <code>DragSourceMotionListener</code> 并添加到此 <code>DragSource</code>。
     * <li>否则，跳过键/值对。
     * </ul>
     *
     * @see java.awt.datatransfer.SystemFlavorMap#getDefaultFlavorMap
     * @since 1.4
     */
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException {
        s.defaultReadObject();

        // 'flavorMap' 被显式写入
        flavorMap = (FlavorMap)s.readObject();

        // 实现假设 'flavorMap' 从不为 null。
        if (flavorMap == null) {
            flavorMap = SystemFlavorMap.getDefaultFlavorMap();
        }

        Object keyOrNull;
        while (null != (keyOrNull = s.readObject())) {
            String key = ((String)keyOrNull).intern();

            if (dragSourceListenerK == key) {
                addDragSourceListener((DragSourceListener)(s.readObject()));
            } else if (dragSourceMotionListenerK == key) {
                addDragSourceMotionListener(
                    (DragSourceMotionListener)(s.readObject()));
            } else {
                // 跳过未识别键的值
                s.readObject();
            }
        }
    }

    /**
     * 返回拖动手势移动阈值。拖动手势移动阈值定义了 {@link MouseDragGestureRecognizer} 的推荐行为。
     * <p>
     * 如果系统属性 <code>awt.dnd.drag.threshold</code> 被设置为正整数，此方法返回该系统属性的值；
     * 否则，如果相关的桌面属性可用且由 Java 平台的实现支持，此方法返回该属性的值；
     * 否则，此方法返回某个默认值。相关的桌面属性可以使用
     * <code>java.awt.Toolkit.getDesktopProperty("DnD.gestureMotionThreshold")</code> 查询。
     *
     * @return 拖动手势移动阈值
     * @see MouseDragGestureRecognizer
     * @since 1.5
     */
    public static int getDragThreshold() {
        int ts = AccessController.doPrivileged(
                new GetIntegerAction("awt.dnd.drag.threshold", 0)).intValue();
        if (ts > 0) {
            return ts;
        } else {
            Integer td = (Integer)Toolkit.getDefaultToolkit().
                    getDesktopProperty("DnD.gestureMotionThreshold");
            if (td != null) {
                return td.intValue();
            }
        }
        return 5;
    }

    /*
     * 字段
     */

    private transient FlavorMap flavorMap = SystemFlavorMap.getDefaultFlavorMap();

    private transient DragSourceListener listener;

    private transient DragSourceMotionListener motionListener;
}
