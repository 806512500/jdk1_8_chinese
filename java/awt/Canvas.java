/*
 * Copyright (c) 1995, 2010, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.image.BufferStrategy;
import java.awt.peer.CanvasPeer;
import javax.accessibility.*;

/**
 * 一个 <code>Canvas</code> 组件表示屏幕上的一个空白矩形区域，
 * 应用程序可以在该区域上绘制或从该区域捕获用户的输入事件。
 * <p>
 * 应用程序必须子类化 <code>Canvas</code> 类以获得有用的功能，例如创建自定义组件。
 * 必须覆盖 <code>paint</code> 方法以在画布上执行自定义图形。
 *
 * @author      Sami Shaio
 * @since       JDK1.0
 */
public class Canvas extends Component implements Accessible {

    private static final String base = "canvas";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = -2284879212465893870L;

    /**
     * 构造一个新的 Canvas。
     */
    public Canvas() {
    }

    /**
     * 给定一个 GraphicsConfiguration 对象构造一个新的 Canvas。
     *
     * @param config 一个引用到 GraphicsConfiguration 对象。
     *
     * @see GraphicsConfiguration
     */
    public Canvas(GraphicsConfiguration config) {
        this();
        setGraphicsConfiguration(config);
    }

    @Override
    void setGraphicsConfiguration(GraphicsConfiguration gc) {
        synchronized(getTreeLock()) {
            CanvasPeer peer = (CanvasPeer)getPeer();
            if (peer != null) {
                gc = peer.getAppropriateGraphicsConfiguration(gc);
            }
            super.setGraphicsConfiguration(gc);
        }
    }

    /**
     * 为这个组件构建一个名称。当名称为 null 时由 getName() 调用。
     */
    String constructComponentName() {
        synchronized (Canvas.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 创建画布的对等体。这个对等体允许你在不改变其功能的情况下更改画布的用户界面。
     * @see     java.awt.Toolkit#createCanvas(java.awt.Canvas)
     * @see     java.awt.Component#getToolkit()
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createCanvas(this);
            super.addNotify();
        }
    }

    /**
     * 绘制这个画布。
     * <p>
     * 大多数子类化 <code>Canvas</code> 的应用程序应该覆盖此方法以执行某些有用的操作
     * （通常是画布的自定义绘制）。
     * 默认操作只是清除画布。
     * 覆盖此方法的应用程序不必调用 super.paint(g)。
     *
     * @param      g   指定的 Graphics 上下文
     * @see        #update(Graphics)
     * @see        Component#paint(Graphics)
     */
    public void paint(Graphics g) {
        g.clearRect(0, 0, width, height);
    }

    /**
     * 更新这个画布。
     * <p>
     * 在调用 <code>repaint</code> 时调用此方法。
     * 通过用背景色填充画布来清除画布，然后通过调用此画布的
     * <code>paint</code> 方法完全重绘。
     * 注意：覆盖此方法的应用程序应调用 super.update(g) 或将上述功能
     * 纳入自己的代码中。
     *
     * @param g 指定的 Graphics 上下文
     * @see   #paint(Graphics)
     * @see   Component#update(Graphics)
     */
    public void update(Graphics g) {
        g.clearRect(0, 0, width, height);
        paint(g);
    }

    boolean postsOldMouseEvents() {
        return true;
    }

    /**
     * 在此组件上创建一个新的多缓冲策略。多缓冲对渲染性能很有用。此方法
     * 尝试使用提供的缓冲区数量创建最佳策略。它将始终创建一个具有该数量缓冲区的
     * <code>BufferStrategy</code>。
     * 首先尝试页面翻转策略，然后尝试使用加速缓冲区的混合策略。
     * 最后，使用未加速的混合策略。
     * <p>
     * 每次调用此方法时，
     * 都会丢弃此组件的现有缓冲策略。
     * @param numBuffers 要创建的缓冲区数量，包括前台缓冲区
     * @exception IllegalArgumentException 如果 numBuffers 小于 1。
     * @exception IllegalStateException 如果组件不可显示
     * @see #isDisplayable
     * @see #getBufferStrategy
     * @since 1.4
     */
    public void createBufferStrategy(int numBuffers) {
        super.createBufferStrategy(numBuffers);
    }

    /**
     * 在此组件上创建一个新的多缓冲策略，具有所需的缓冲功能。例如，如果仅需要
     * 加速内存或页面翻转（如缓冲功能所指定的）。
     * <p>
     * 每次调用此方法时，都会丢弃此组件的现有缓冲策略。
     * @param numBuffers 要创建的缓冲区数量
     * @param caps 创建缓冲策略所需的缓冲功能；不能为 <code>null</code>
     * @exception AWTException 如果提供的功能无法支持或满足；例如，如果当前没有足够的
     * 加速内存，或者指定了页面翻转但不可能。
     * @exception IllegalArgumentException 如果 numBuffers 小于 1，或者
     * caps 为 <code>null</code>
     * @see #getBufferStrategy
     * @since 1.4
     */
    public void createBufferStrategy(int numBuffers,
        BufferCapabilities caps) throws AWTException {
        super.createBufferStrategy(numBuffers, caps);
    }

    /**
     * 返回此组件使用的 <code>BufferStrategy</code>。如果尚未创建或已释放
     * <code>BufferStrategy</code>，则此方法将返回 null。
     *
     * @return 此组件使用的缓冲策略
     * @see #createBufferStrategy
     * @since 1.4
     */
    public BufferStrategy getBufferStrategy() {
        return super.getBufferStrategy();
    }

    /*
     * --- Accessibility Support ---
     *
     */

    /**
     * 获取与此 Canvas 关联的 AccessibleContext。对于画布，AccessibleContext
     * 采用 AccessibleAWTCanvas 的形式。如果需要，将创建一个新的 AccessibleAWTCanvas 实例。
     *
     * @return 一个 AccessibleAWTCanvas，作为此 Canvas 的 AccessibleContext
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTCanvas();
        }
        return accessibleContext;
    }

    /**
     * 为 <code>Canvas</code> 类实现可访问性支持。它为画布用户界面元素提供了
     * Java 可访问性 API 的实现。
     * @since 1.3
     */
    protected class AccessibleAWTCanvas extends AccessibleAWTComponent
    {
        private static final long serialVersionUID = -6325592262103146699L;

        /**
         * 获取此对象的角色。
         *
         * @return 一个 AccessibleRole 实例，描述对象的角色
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.CANVAS;
        }

    } // inner class AccessibleAWTCanvas
}
