
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

package java.awt.peer;

import java.awt.*;
import java.awt.event.PaintEvent;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;

import sun.awt.CausedFocusEvent;
import sun.java2d.pipe.Region;


/**
 * {@link Component} 的对等接口。这是小部件的顶级对等接口，定义了 AWT 组件对等接口的大部分方法。大多数组件对等接口需要实现此接口（通过其中一个子接口），除了实现 {@link MenuComponentPeer} 的菜单组件。
 *
 * 对等接口仅用于移植 AWT。它们不打算由应用程序开发人员使用，开发人员不应实现对等接口，也不应直接在对等实例上调用任何对等方法。
 */
public interface ComponentPeer {

    /**
     * 用于 {@link #setBounds(int, int, int, int, int)}，表示仅更改组件位置。
     *
     * @see #setBounds(int, int, int, int, int)
     */
    public static final int SET_LOCATION = 1;

    /**
     * 用于 {@link #setBounds(int, int, int, int, int)}，表示仅更改组件大小。
     *
     * @see #setBounds(int, int, int, int, int)
     */
    public static final int SET_SIZE = 2;

    /**
     * 用于 {@link #setBounds(int, int, int, int, int)}，表示同时更改组件大小和位置。
     *
     * @see #setBounds(int, int, int, int, int)
     */
    public static final int SET_BOUNDS = 3;

    /**
     * 用于 {@link #setBounds(int, int, int, int, int)}，表示更改组件的客户端大小。这用于设置窗口的“内部”大小，不包括边框内边距。
     *
     * @see #setBounds(int, int, int, int, int)
     */
    public static final int SET_CLIENT_SIZE = 4;

    /**
     * 将 setBounds() 操作重置为 DEFAULT_OPERATION。这不会传递给 {@link #setBounds(int, int, int, int, int)}。
     *
     * TODO: 这仅用于内部，应该从对等接口中移出。
     *
     * @see Component#setBoundsOp
     */
    public static final int RESET_OPERATION = 5;

    /**
     * 用于抑制嵌入式框架检查的标志。
     *
     * TODO: 这仅用于内部，应该从对等接口中移出。
     */
    public static final int NO_EMBEDDED_CHECK = (1 << 14);

    /**
     * 默认操作，即设置大小和位置。
     *
     * TODO: 这仅用于内部，应该从对等接口中移出。
     *
     * @see Component#setBoundsOp
     */
    public static final int DEFAULT_OPERATION = SET_BOUNDS;

    /**
     * 确定组件是否被遮挡，例如被重叠的窗口或其他类似情况。这用于 JViewport 优化性能。当 {@link #canDetermineObscurity()} 返回 {@code false} 时，不需要实现此方法。
     *
     * @return 如果组件被遮挡，则返回 {@code true}，否则返回 {@code false}
     *
     * @see #canDetermineObscurity()
     * @see javax.swing.JViewport#needsRepaintAfterBlit
     */
    boolean isObscured();

    /**
     * 如果对等接口可以确定组件是否被遮挡，则返回 {@code true}，否则返回 {@code false}。
     *
     * @return 如果对等接口可以确定组件是否被遮挡，则返回 {@code true}，否则返回 {@code false}
     *
     * @see #isObscured()
     * @see javax.swing.JViewport#needsRepaintAfterBlit
     */
    boolean canDetermineObscurity();

    /**
     * 使组件可见或不可见。
     *
     * @param v 如果要使组件可见，则为 {@code true}，否则为 {@code false}
     *
     * @see Component#setVisible(boolean)
     */
    void setVisible(boolean v);

    /**
     * 启用或禁用组件。禁用的组件通常会变灰并且无法激活。
     *
     * @param e 如果要启用组件，则为 {@code true}，否则为 {@code false}
     *
     * @see Component#setEnabled(boolean)
     */
    void setEnabled(boolean e);

    /**
     * 将组件绘制到指定的图形上下文中。这是由 {@link Component#paintAll(Graphics)} 调用以绘制组件。
     *
     * @param g 要绘制到的图形上下文
     *
     * @see Component#paintAll(Graphics)
     */
    void paint(Graphics g);

    /**
     * 将组件打印到指定的图形上下文中。这是由 {@link Component#printAll(Graphics)} 调用以打印组件。
     *
     * @param g 要打印到的图形上下文
     *
     * @see Component#printAll(Graphics)
     */
    void print(Graphics g);

    /**
     * 设置组件的位置或大小或两者。位置相对于组件的父组件。{@code op} 参数指定哪些属性会改变。如果它是 {@link #SET_LOCATION}，则仅改变位置（大小参数可以忽略）。如果 {@code op} 是 {@link #SET_SIZE}，则仅改变大小（位置可以忽略）。如果 {@code op} 是 {@link #SET_BOUNDS}，则两者都会改变。有一个特殊的值 {@link #SET_CLIENT_SIZE}，仅用于类似窗口的组件，用于设置客户端的大小（即“内部”大小，不包括窗口边框的内边距）。
     *
     * @param x 组件的 X 位置
     * @param y 组件的 Y 位置
     * @param width 组件的宽度
     * @param height 组件的高度
     * @param op 操作标志
     *
     * @see #SET_BOUNDS
     * @see #SET_LOCATION
     * @see #SET_SIZE
     * @see #SET_CLIENT_SIZE
     */
    void setBounds(int x, int y, int width, int height, int op);

    /**
     * 调用以让组件对等接口处理事件。
     *
     * @param e 要处理的 AWT 事件
     *
     * @see Component#dispatchEvent(AWTEvent)
     */
    void handleEvent(AWTEvent e);

    /**
     * 调用以合并绘制事件。
     *
     * @param e 要考虑合并的绘制事件
     *
     * @see EventQueue#coalescePaintEvent
     */
    void coalescePaintEvent(PaintEvent e);

    /**
     * 确定组件在屏幕上的位置。
     *
     * @return 组件在屏幕上的位置
     *
     * @see Component#getLocationOnScreen()
     */
    Point getLocationOnScreen();

    /**
     * 确定组件的首选大小。
     *
     * @return 组件的首选大小
     *
     * @see Component#getPreferredSize()
     */
    Dimension getPreferredSize();

    /**
     * 确定组件的最小大小。
     *
     * @return 组件的最小大小
     *
     * @see Component#getMinimumSize()
     */
    Dimension getMinimumSize();

    /**
     * 返回组件使用的颜色模型。
     *
     * @return 组件使用的颜色模型
     *
     * @see Component#getColorModel()
     */
    ColorModel getColorModel();

    /**
     * 返回一个用于在组件上绘制的图形对象。
     *
     * @return 一个用于在组件上绘制的图形对象
     *
     * @see Component#getGraphics()
     */
    // TODO: 可能需要将此方法更改为强制返回 Graphics2D，因为现在许多事情会因为返回普通的 Graphics 而出问题。
    Graphics getGraphics();

    /**
     * 返回一个用于确定指定字体的度量属性的字体度量对象。
     *
     * @param font 要确定度量属性的字体
     *
     * @return 一个用于确定指定字体的度量属性的字体度量对象
     *
     * @see Component#getFontMetrics(Font)
     */
    FontMetrics getFontMetrics(Font font);

    /**
     * 释放组件对等接口持有的所有资源。当组件已从组件层次结构中断开连接并即将被垃圾回收时调用此方法。
     *
     * @see Component#removeNotify()
     */
    void dispose();

    /**
     * 设置此组件的前景色。
     *
     * @param c 要设置的前景色
     *
     * @see Component#setForeground(Color)
     */
    void setForeground(Color c);

    /**
     * 设置此组件的背景色。
     *
     * @param c 要设置的背景色
     *
     * @see Component#setBackground(Color)
     */
    void setBackground(Color c);

    /**
     * 设置此组件的字体。
     *
     * @param f 此组件的字体
     *
     * @see Component#setFont(Font)
     */
    void setFont(Font f);

    /**
     * 更新组件的光标。
     *
     * @see Component#updateCursorImmediately
     */
    void updateCursorImmediately();

    /**
     * 请求此组件获得焦点。
     *
     * @param lightweightChild 实际请求焦点的轻量级子组件
     * @param temporary 如果焦点更改是临时的，则为 {@code true}，否则为 {@code false}
     * @param focusedWindowChangeAllowed 如果允许更改包含窗口的焦点，则为 {@code true}，否则为 {@code false}
     * @param time 焦点更改请求的时间
     * @param cause 焦点更改请求的原因
     *
     * @return 如果焦点更改肯定会被授予，则返回 {@code true}，否则返回 {@code false}
     */
    boolean requestFocus(Component lightweightChild, boolean temporary,
                         boolean focusedWindowChangeAllowed, long time,
                         CausedFocusEvent.Cause cause);

    /**
     * 如果组件参与焦点遍历，则返回 {@code true}，否则返回 {@code false}。
     *
     * @return 如果组件参与焦点遍历，则返回 {@code true}，否则返回 {@code false}
     */
    boolean isFocusable();

    /**
     * 使用指定的图像生成器创建图像。
     *
     * @param producer 生成图像像素的图像生成器
     *
     * @return 创建的图像
     *
     * @see Component#createImage(ImageProducer)
     */
    Image createImage(ImageProducer producer);

    /**
     * 创建指定宽度和高度的空图像。这通常用作绘制到组件的非加速后备缓冲区（例如由 Swing 使用）。
     *
     * @param width 图像的宽度
     * @param height 图像的高度
     *
     * @return 创建的图像
     *
     * @see Component#createImage(int, int)
     */
    // TODO: 可能需要将此方法更改为返回 BufferedImage，因为如果返回不同类型的图像，某些功能会出问题。
    Image createImage(int width, int height);

    /**
     * 创建指定宽度和高度的空易失图像。这通常用作绘制到组件的加速后备缓冲区（例如由 Swing 使用）。
     *
     * @param width 图像的宽度
     * @param height 图像的高度
     *
     * @return 创建的易失图像
     *
     * @see Component#createVolatileImage(int, int)
     */
    // TODO: 在这里包含功能，并修复 Component#createVolatileImage
    VolatileImage createVolatileImage(int width, int height);

    /**
     * 为在此组件上渲染准备指定的图像。这应该开始加载图像（如果尚未加载）并创建适当的屏幕表示。
     *
     * @param img 要准备的图像
     * @param w 屏幕表示的宽度
     * @param h 屏幕表示的高度
     * @param o 观察图像进度的图像观察者
     *
     * @return 如果图像已经完全准备好了，则返回 {@code true}，否则返回 {@code false}
     *
     * @see Component#prepareImage(Image, int, int, ImageObserver)
     */
    boolean prepareImage(Image img, int w, int h, ImageObserver o);

    /**
     * 确定指定图像的屏幕表示的构造状态。
     *
     * @param img 要检查的图像
     * @param w 目标宽度
     * @param h 目标高度
     * @param o 通知图像观察者
     *
     * @return 作为按位或的 ImageObserver 标志的状态
     *
     * @see Component#checkImage(Image, int, int, ImageObserver)
     */
    int checkImage(Image img, int w, int h, ImageObserver o);

    /**
     * 返回与此组件对应的图形配置。
     *
     * @return 与此组件对应的图形配置
     *
     * @see Component#getGraphicsConfiguration()
     */
    GraphicsConfiguration getGraphicsConfiguration();

    /**
     * 确定组件是否自行处理滚轮滚动。否则，将委托给组件的父组件。
     *
     * @return 如果组件处理滚轮滚动，则返回 {@code true}，否则返回 {@code false}
     *
     * @see Component#dispatchEventImpl(AWTEvent)
     */
    boolean handlesWheelScrolling();

    /**
     * 使用指定的缓冲区功能创建 {@code numBuffers} 个翻转缓冲区。
     *
     * @param numBuffers 要创建的缓冲区数量
     * @param caps 缓冲区功能
     *
     * @throws AWTException 如果不支持翻转缓冲区
     *
     * @see Component.FlipBufferStrategy#createBuffers
     */
    void createBuffers(int numBuffers, BufferCapabilities caps)
         throws AWTException;

    /**
     * 返回后缓冲区作为图像。
     *
     * @return 后缓冲区作为图像
     *
     * @see Component.FlipBufferStrategy#getBackBuffer
     */
    Image getBackBuffer();

    /**
     * 将后缓冲区移动到前缓冲区。
     *
     * @param x1 要翻转的区域，左上 X 坐标
     * @param y1 要翻转的区域，左上 Y 坐标
     * @param x2 要翻转的区域，右下 X 坐标
     * @param y2 要翻转的区域，右下 Y 坐标
     * @param flipAction 要执行的翻转操作
     *
     * @see Component.FlipBufferStrategy#flip
     */
    void flip(int x1, int y1, int x2, int y2, BufferCapabilities.FlipContents flipAction);


    /**
     * 销毁所有创建的缓冲区。
     *
     * @see Component.FlipBufferStrategy#destroyBuffers
     */
    void destroyBuffers();

    /**
     * 将此对等体重新父化到由 {@code newContainer} 对等体引用的新父容器。实现取决于工具包和容器。
     *
     * @param newContainer 新父容器的对等体
     *
     * @since 1.5
     */
    void reparent(ContainerPeer newContainer);

    /**
     * 返回此对等体是否支持在不销毁对等体的情况下重新父化到另一个父容器。
     *
     * @return 如果适当的重新父化受支持，则返回 true，否则返回 false
     *
     * @since 1.5
     */
    boolean isReparentSupported();

    /**
     * 由轻量级实现用于告诉 ComponentPeer 布局其子元素。例如，轻量级 Checkbox 需要布局复选框以及文本标签。
     *
     * @see Component#validate()
     */
    void layout();

    /**
     * 将形状应用于本机组件窗口。
     * @since 1.7
     *
     * @see Component#applyCompoundShape
     */
    void applyShape(Region shape);

    /**
     * 将此组件置于 above HW 对等体的底部。如果 above 参数为 null，则该方法将此组件置于 Z 顺序的顶部。
     */
    void setZOrder(ComponentPeer above);

    /**
     * 更新与组件的 GC 相关的内部数据结构。
     *
     * @return 如果需要重新创建对等体以使更改生效，则返回 true
     * @since 1.7
     */
    boolean updateGraphicsData(GraphicsConfiguration gc);
}
