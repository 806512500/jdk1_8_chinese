
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

import java.awt.peer.ScrollbarPeer;
import java.awt.event.*;
import java.util.EventListener;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import javax.accessibility.*;


/**
 * <code>Scrollbar</code> 类体现了一个滚动条，这是一个用户界面中常见的对象。滚动条提供了一种方便的手段，允许用户从一系列值中进行选择。以下三个垂直滚动条可以用作选择颜色的红色、绿色和蓝色分量的滑块控件：
 * <p>
 * <img src="doc-files/Scrollbar-1.gif" alt="图像显示了三个并排的垂直滑块。"
 * style="float:center; margin: 7px 10px;">
 * <p>
 * 本例中的每个滚动条都可以使用类似于以下代码创建：
 *
 * <hr><blockquote><pre>
 * redSlider=new Scrollbar(Scrollbar.VERTICAL, 0, 1, 0, 255);
 * add(redSlider);
 * </pre></blockquote><hr>
 * <p>
 * 或者，滚动条可以表示一个值的范围。例如，如果滚动条用于滚动文本，滚动条的“气泡”（也称为“拇指”或“滚动框”）的宽度可以用来表示可见的文本量。以下是一个表示范围的滚动条示例：
 * <p>
 * <img src="doc-files/Scrollbar-2.gif"
 * alt="图像显示了一个水平滑块，起始范围为 0，结束范围为 300。滑块拇指标记为 60。"
 * style="float:center; margin: 7px 10px;">
 * <p>
 * 本例中气泡表示的值范围是“可见量”。此水平滚动条可以使用类似于以下代码创建：
 *
 * <hr><blockquote><pre>
 * ranger = new Scrollbar(Scrollbar.HORIZONTAL, 0, 60, 0, 300);
 * add(ranger);
 * </pre></blockquote><hr>
 * <p>
 * 请注意，滚动条的实际最大值是<code>maximum</code>减去<code>visible amount</code>。在上一个示例中，因为<code>maximum</code>是300，<code>visible amount</code>是60，所以实际最大值是240。滚动条轨道的范围是0到300。气泡的左侧表示滚动条的值。
 * <p>
 * 通常情况下，用户通过鼠标手势来改变滚动条的值。例如，用户可以拖动滚动条的气泡上下移动，或者点击滚动条的单位增量或块增量区域。键盘手势也可以映射到滚动条。按照惯例，<b>Page&nbsp;Up</b>和<b>Page&nbsp;Down</b>键等同于点击滚动条的块增量和块减量区域。
 * <p>
 * 当用户改变滚动条的值时，滚动条会接收到一个<code>AdjustmentEvent</code>的实例。滚动条处理这个事件，并将其传递给任何已注册的监听器。
 * <p>
 * 任何希望在滚动条的值发生变化时收到通知的对象都应该实现<code>AdjustmentListener</code>接口，该接口定义在<code>java.awt.event</code>包中。监听器可以通过调用<code>addAdjustmentListener</code>和<code>removeAdjustmentListener</code>方法动态添加和移除。
 * <p>
 * <code>AdjustmentEvent</code>类定义了五种类型的调整事件，如下所示：
 *
 * <ul>
 * <li><code>AdjustmentEvent.TRACK</code>在用户拖动滚动条的气泡时发送。
 * <li><code>AdjustmentEvent.UNIT_INCREMENT</code>在用户点击水平滚动条的左箭头、垂直滚动条的上箭头或从键盘进行等效手势时发送。
 * <li><code>AdjustmentEvent.UNIT_DECREMENT</code>在用户点击水平滚动条的右箭头、垂直滚动条的下箭头或从键盘进行等效手势时发送。
 * <li><code>AdjustmentEvent.BLOCK_INCREMENT</code>在用户点击轨道，水平滚动条的气泡左侧或垂直滚动条的气泡上方时发送。按照惯例，如果用户使用的是定义了<b>Page&nbsp;Up</b>键的键盘，<b>Page&nbsp;Up</b>键等同于此操作。
 * <li><code>AdjustmentEvent.BLOCK_DECREMENT</code>在用户点击轨道，水平滚动条的气泡右侧或垂直滚动条的气泡下方时发送。按照惯例，如果用户使用的是定义了<b>Page&nbsp;Down</b>键的键盘，<b>Page&nbsp;Down</b>键等同于此操作。
 * </ul>
 * <p>
 * JDK&nbsp;1.0事件系统为了向后兼容而支持，但在较新版本的平台中不建议使用。JDK&nbsp;1.1引入的五种调整事件类型对应于以前平台版本中与滚动条关联的五种事件类型。以下列表给出了调整事件类型及其替换的JDK&nbsp;1.0事件类型。
 *
 * <ul>
 * <li><code>AdjustmentEvent.TRACK</code>替换<code>Event.SCROLL_ABSOLUTE</code>
 * <li><code>AdjustmentEvent.UNIT_INCREMENT</code>替换<code>Event.SCROLL_LINE_UP</code>
 * <li><code>AdjustmentEvent.UNIT_DECREMENT</code>替换<code>Event.SCROLL_LINE_DOWN</code>
 * <li><code>AdjustmentEvent.BLOCK_INCREMENT</code>替换<code>Event.SCROLL_PAGE_UP</code>
 * <li><code>AdjustmentEvent.BLOCK_DECREMENT</code>替换<code>Event.SCROLL_PAGE_DOWN</code>
 * </ul>
 * <p>
 * <b>注意</b>：我们建议仅使用<code>Scrollbar</code>进行值选择。如果您希望在容器内实现可滚动的组件，我们建议您使用<code>{@link ScrollPane ScrollPane}</code>。如果您将<code>Scrollbar</code>用于此目的，可能会遇到绘制、键处理、大小和定位方面的问题。
 *
 * @author      Sami Shaio
 * @see         java.awt.event.AdjustmentEvent
 * @see         java.awt.event.AdjustmentListener
 * @since       JDK1.0
 */
public class Scrollbar extends Component implements Adjustable, Accessible {

    /**
     * 表示水平滚动条的常量。
     */
    public static final int     HORIZONTAL = 0;

    /**
     * 表示垂直滚动条的常量。
     */
    public static final int     VERTICAL   = 1;

    /**
     * <code>Scrollbar</code>的值。
     * 此属性必须大于或等于<code>minimum</code>且小于或等于<code>maximum - visibleAmount</code>。
     *
     * @serial
     * @see #getValue
     * @see #setValue
     */
    int value;

    /**
     * <code>Scrollbar</code>的最大值。
     * 此值必须大于<code>minimum</code>值。<br>
     *
     * @serial
     * @see #getMaximum
     * @see #setMaximum
     */
    int maximum;

    /**
     * <code>Scrollbar</code>的最小值。
     * 此值必须小于<code>maximum</code>值。<br>
     *
     * @serial
     * @see #getMinimum
     * @see #setMinimum
     */
    int minimum;

    /**
     * <code>Scrollbar</code>的气泡大小。
     * 当滚动条用于选择值的范围时，<code>visibleAmount</code>表示此范围的大小。根据平台，这可能通过气泡的大小来视觉表示。
     *
     * @serial
     * @see #getVisibleAmount
     * @see #setVisibleAmount
     */
    int visibleAmount;

    /**
     * <code>Scrollbar</code>的方向，可以是水平或垂直。
     * 创建滚动条时应指定此值。<BR>
     * 方向可以是：<code>VERTICAL</code>或<code>HORIZONTAL</code>。
     *
     * @serial
     * @see #getOrientation
     * @see #setOrientation
     */
    int orientation;

    /**
     * 滚动条值在上下移动一行时的变化量。
     * 此值必须大于零。
     *
     * @serial
     * @see #getLineIncrement
     * @see #setLineIncrement
     */
    int lineIncrement = 1;

    /**
     * 滚动条值在上下移动一页时的变化量。
     * 此值必须大于零。
     *
     * @serial
     * @see #getPageIncrement
     * @see #setPageIncrement
     */
    int pageIncrement = 10;

    /**
     * <code>Scrollbar</code>的调整状态。
     * 如果值正在因用户操作而变化，则为真。
     *
     * @see #getValueIsAdjusting
     * @see #setValueIsAdjusting
     * @since 1.4
     */
    transient boolean isAdjusting;

    transient AdjustmentListener adjustmentListener;

    private static final String base = "scrollbar";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = 8451667562882310543L;

    /**
     * 初始化JNI字段和方法ID。
     */
    private static native void initIDs();

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * 构造一个新的垂直滚动条。
     * 滚动条的默认属性如下表所示：
     *
     * <table border=1 summary="Scrollbar默认属性">
     * <tr>
     *   <th>属性</th>
     *   <th>描述</th>
     *   <th>默认值</th>
     * </tr>
     * <tr>
     *   <td>方向</td>
     *   <td>表示滚动条是垂直还是水平</td>
     *   <td><code>Scrollbar.VERTICAL</code></td>
     * </tr>
     * <tr>
     *   <td>值</td>
     *   <td>控制滚动条气泡位置的值</td>
     *   <td>0</td>
     * </tr>
     * <tr>
     *   <td>可见量</td>
     *   <td>滚动条范围的可见量，通常由滚动条气泡的大小表示</td>
     *   <td>10</td>
     * </tr>
     * <tr>
     *   <td>最小值</td>
     *   <td>滚动条的最小值</td>
     *   <td>0</td>
     * </tr>
     * <tr>
     *   <td>最大值</td>
     *   <td>滚动条的最大值</td>
     *   <td>100</td>
     * </tr>
     * <tr>
     *   <td>单位增量</td>
     *   <td>当按下Line Up或Line Down键或点击滚动条的端箭头时，值的变化量</td>
     *   <td>1</td>
     * </tr>
     * <tr>
     *   <td>块增量</td>
     *   <td>当按下Page Up或Page Down键或点击滚动条轨道的气泡两侧时，值的变化量</td>
     *   <td>10</td>
     * </tr>
     * </table>
     *
     * @exception HeadlessException 如果GraphicsEnvironment.isHeadless()返回true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public Scrollbar() throws HeadlessException {
        this(VERTICAL, 0, 10, 0, 100);
    }

    /**
     * 构造一个具有指定方向的新滚动条。
     * <p>
     * <code>orientation</code>参数必须取两个值之一：<code>Scrollbar.HORIZONTAL</code>或<code>Scrollbar.VERTICAL</code>，分别表示水平或垂直滚动条。
     *
     * @param       orientation   指示滚动条的方向
     * @exception   IllegalArgumentException    当<code>orientation</code>参数的值非法时抛出
     * @exception HeadlessException 如果GraphicsEnvironment.isHeadless()返回true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public Scrollbar(int orientation) throws HeadlessException {
        this(orientation, 0, 10, 0, 100);
    }

    /**
     * 构造一个具有指定方向、初始值、可见量、最小值和最大值的新滚动条。
     * <p>
     * <code>orientation</code>参数必须取两个值之一：<code>Scrollbar.HORIZONTAL</code>或<code>Scrollbar.VERTICAL</code>，分别表示水平或垂直滚动条。
     * <p>
     * 传递给此构造函数的参数受{@link #setValues(int, int, int, int)}中描述的约束。
     *
     * @param     orientation   指示滚动条的方向。
     * @param     value     滚动条的初始值
     * @param     visible   滚动条的可见量，通常由气泡的大小表示
     * @param     minimum   滚动条的最小值
     * @param     maximum   滚动条的最大值
     * @exception IllegalArgumentException    当<code>orientation</code>参数的值非法时抛出
     * @exception HeadlessException 如果GraphicsEnvironment.isHeadless()返回true。
     * @see #setValues
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public Scrollbar(int orientation, int value, int visible, int minimum,
        int maximum) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        switch (orientation) {
          case HORIZONTAL:
          case VERTICAL:
            this.orientation = orientation;
            break;
          default:
            throw new IllegalArgumentException("非法的滚动条方向");
        }
        setValues(value, visible, minimum, maximum);
    }

    /**
     * 为该组件构建一个名称。当名称为<code>null</code>时，由<code>getName</code>调用。
     */
    String constructComponentName() {
        synchronized (Scrollbar.class) {
            return base + nameCounter++;
        }
    }


                /**
     * 创建 <code>Scrollbar</code> 的对等体。对等体允许您在不改变 <code>Scrollbar</code> 任何功能的情况下修改其外观。
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createScrollbar(this);
            super.addNotify();
        }
    }

    /**
     * 返回此滚动条的方向。
     *
     * @return    此滚动条的方向，可以是
     *               <code>Scrollbar.HORIZONTAL</code> 或
     *               <code>Scrollbar.VERTICAL</code>
     * @see       java.awt.Scrollbar#setOrientation
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * 设置此滚动条的方向。
     *
     * @param orientation  此滚动条的方向，可以是
     *               <code>Scrollbar.HORIZONTAL</code> 或
     *               <code>Scrollbar.VERTICAL</code>
     * @see       java.awt.Scrollbar#getOrientation
     * @exception   IllegalArgumentException  如果提供的 <code>orientation</code> 值不合法
     * @since     JDK1.1
     */
    public void setOrientation(int orientation) {
        synchronized (getTreeLock()) {
            if (orientation == this.orientation) {
                return;
            }
            switch (orientation) {
                case HORIZONTAL:
                case VERTICAL:
                    this.orientation = orientation;
                    break;
                default:
                    throw new IllegalArgumentException("非法滚动条方向");
            }
            /* 创建具有指定方向的新对等体。 */
            if (peer != null) {
                removeNotify();
                addNotify();
                invalidate();
            }
        }
        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    ((orientation == VERTICAL)
                     ? AccessibleState.HORIZONTAL : AccessibleState.VERTICAL),
                    ((orientation == VERTICAL)
                     ? AccessibleState.VERTICAL : AccessibleState.HORIZONTAL));
        }
    }

    /**
     * 获取此滚动条的当前值。
     *
     * @return      此滚动条的当前值
     * @see         java.awt.Scrollbar#getMinimum
     * @see         java.awt.Scrollbar#getMaximum
     */
    public int getValue() {
        return value;
    }

    /**
     * 将此滚动条的值设置为指定值。
     * <p>
     * 如果提供的值小于当前 <code>minimum</code> 或大于当前 <code>maximum - visibleAmount</code>，
     * 则分别用 <code>minimum</code> 或 <code>maximum - visibleAmount</code> 替换。
     * <p>
     * 通常，程序应仅通过调用 <code>setValues</code> 来更改滚动条的值。
     * <code>setValues</code> 方法同时同步设置滚动条的最小值、最大值、可见量和值属性，以确保它们相互一致。
     * <p>
     * 调用此方法不会触发 <code>AdjustmentEvent</code>。
     *
     * @param       newValue   滚动条的新值
     * @see         java.awt.Scrollbar#setValues
     * @see         java.awt.Scrollbar#getValue
     * @see         java.awt.Scrollbar#getMinimum
     * @see         java.awt.Scrollbar#getMaximum
     */
    public void setValue(int newValue) {
        // 使用 setValues 以确保最小值、最大值、可见量和值之间的策略一致。
        setValues(newValue, visibleAmount, minimum, maximum);
    }

    /**
     * 获取此滚动条的最小值。
     *
     * @return      此滚动条的最小值
     * @see         java.awt.Scrollbar#getValue
     * @see         java.awt.Scrollbar#getMaximum
     */
    public int getMinimum() {
        return minimum;
    }

    /**
     * 设置此滚动条的最小值。
     * <p>
     * 调用 <code>setMinimum</code> 时，最小值将更改，并且其他值（包括最大值、可见量和当前滚动条值）将调整为与新的最小值一致。
     * <p>
     * 通常，程序应仅通过调用 <code>setValues</code> 来更改滚动条的最小值。
     * <code>setValues</code> 方法同时同步设置滚动条的最小值、最大值、可见量和值属性，以确保它们相互一致。
     * <p>
     * 注意，将最小值设置为 <code>Integer.MAX_VALUE</code> 将导致新的最小值被设置为 <code>Integer.MAX_VALUE - 1</code>。
     *
     * @param       newMinimum   此滚动条的新最小值
     * @see         java.awt.Scrollbar#setValues
     * @see         java.awt.Scrollbar#setMaximum
     * @since       JDK1.1
     */
    public void setMinimum(int newMinimum) {
        // 无需在此方法中进行检查，因为 minimum 是 setValues 函数中第一个检查的变量。

        // 使用 setValues 以确保最小值、最大值、可见量和值之间的策略一致。
        setValues(value, visibleAmount, newMinimum, maximum);
    }

    /**
     * 获取此滚动条的最大值。
     *
     * @return      此滚动条的最大值
     * @see         java.awt.Scrollbar#getValue
     * @see         java.awt.Scrollbar#getMinimum
     */
    public int getMaximum() {
        return maximum;
    }

    /**
     * 设置此滚动条的最大值。
     * <p>
     * 调用 <code>setMaximum</code> 时，最大值将更改，并且其他值（包括最小值、可见量和当前滚动条值）将调整为与新的最大值一致。
     * <p>
     * 通常，程序应仅通过调用 <code>setValues</code> 来更改滚动条的最大值。
     * <code>setValues</code> 方法同时同步设置滚动条的最小值、最大值、可见量和值属性，以确保它们相互一致。
     * <p>
     * 注意，将最大值设置为 <code>Integer.MIN_VALUE</code> 将导致新的最大值被设置为 <code>Integer.MIN_VALUE + 1</code>。
     *
     * @param       newMaximum   此滚动条的新最大值
     * @see         java.awt.Scrollbar#setValues
     * @see         java.awt.Scrollbar#setMinimum
     * @since       JDK1.1
     */
    public void setMaximum(int newMaximum) {
        // 在 setValues 中首先检查 minimum，因此需要在这里强制执行 minimum 和 maximum 检查。
        if (newMaximum == Integer.MIN_VALUE) {
            newMaximum = Integer.MIN_VALUE + 1;
        }

        if (minimum >= newMaximum) {
            minimum = newMaximum - 1;
        }

        // 使用 setValues 以确保最小值、最大值、可见量和值之间的策略一致。
        setValues(value, visibleAmount, minimum, newMaximum);
    }

    /**
     * 获取此滚动条的可见量。
     * <p>
     * 当滚动条用于选择一个值范围时，可见量用于表示当前可见的值范围。滚动条的气泡（也称为拇指或滚动框）的大小通常给出了可见量与滚动条范围之间关系的视觉表示。
     * 请注意，根据平台的不同，可见量属性的值可能不会通过气泡的大小来视觉指示。
     * <p>
     * 当气泡不可移动时（例如，当气泡占据滚动条轨道的整个长度或滚动条被禁用时），滚动条的气泡可能不会显示。气泡是否显示不会影响 <code>getVisibleAmount</code> 返回的值。
     *
     * @return      此滚动条的可见量
     * @see         java.awt.Scrollbar#setVisibleAmount
     * @since       JDK1.1
     */
    public int getVisibleAmount() {
        return getVisible();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getVisibleAmount()</code>。
     */
    @Deprecated
    public int getVisible() {
        return visibleAmount;
    }

    /**
     * 设置此滚动条的可见量。
     * <p>
     * 当滚动条用于选择一个值范围时，可见量用于表示当前可见的值范围。滚动条的气泡（也称为拇指或滚动框）的大小通常给出了可见量与滚动条范围之间关系的视觉表示。
     * 请注意，根据平台的不同，可见量属性的值可能不会通过气泡的大小来视觉指示。
     * <p>
     * 当气泡不可移动时（例如，当气泡占据滚动条轨道的整个长度或滚动条被禁用时），滚动条的气泡可能不会显示。气泡是否显示不会影响 <code>getVisibleAmount</code> 返回的值。
     * <p>
     * 如果提供的可见量小于 <code>one</code> 或大于当前 <code>maximum - minimum</code>，
     * 则分别用 <code>one</code> 或 <code>maximum - minimum</code> 替换。
     * <p>
     * 通常，程序应仅通过调用 <code>setValues</code> 来更改滚动条的值。
     * <code>setValues</code> 方法同时同步设置滚动条的最小值、最大值、可见量和值属性，以确保它们相互一致。
     *
     * @param       newAmount 新的可见量
     * @see         java.awt.Scrollbar#getVisibleAmount
     * @see         java.awt.Scrollbar#setValues
     * @since       JDK1.1
     */
    public void setVisibleAmount(int newAmount) {
        // 使用 setValues 以确保最小值、最大值、可见量和值之间的策略一致。
        setValues(value, newAmount, minimum, maximum);
    }

    /**
     * 设置此滚动条的单位增量。
     * <p>
     * 单位增量是当用户激活滚动条的单位增量区域时添加或减去的值，通常通过滚动条接收到的调整事件中的鼠标或键盘手势来激活。
     * 单位增量必须大于零。尝试将单位增量设置为小于 1 的值将导致设置为 1。
     * <p>
     * 在某些操作系统中，底层控件可能会忽略此属性。
     *
     * @param        v  增加或减少滚动条值的量
     * @see          java.awt.Scrollbar#getUnitIncrement
     * @since        JDK1.1
     */
    public void setUnitIncrement(int v) {
        setLineIncrement(v);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>setUnitIncrement(int)</code>。
     */
    @Deprecated
    public synchronized void setLineIncrement(int v) {
        int tmp = (v < 1) ? 1 : v;

        if (lineIncrement == tmp) {
            return;
        }
        lineIncrement = tmp;

        ScrollbarPeer peer = (ScrollbarPeer)this.peer;
        if (peer != null) {
            peer.setLineIncrement(lineIncrement);
        }
    }

    /**
     * 获取此滚动条的单位增量。
     * <p>
     * 单位增量是当用户激活滚动条的单位增量区域时添加或减去的值，通常通过滚动条接收到的调整事件中的鼠标或键盘手势来激活。
     * 单位增量必须大于零。
     * <p>
     * 在某些操作系统中，底层控件可能会忽略此属性。
     *
     * @return      此滚动条的单位增量
     * @see         java.awt.Scrollbar#setUnitIncrement
     * @since       JDK1.1
     */
    public int getUnitIncrement() {
        return getLineIncrement();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getUnitIncrement()</code>。
     */
    @Deprecated
    public int getLineIncrement() {
        return lineIncrement;
    }

    /**
     * 设置此滚动条的块增量。
     * <p>
     * 块增量是当用户激活滚动条的块增量区域时添加或减去的值，通常通过滚动条接收到的调整事件中的鼠标或键盘手势来激活。
     * 块增量必须大于零。尝试将块增量设置为小于 1 的值将导致设置为 1。
     *
     * @param        v  增加或减少滚动条值的量
     * @see          java.awt.Scrollbar#getBlockIncrement
     * @since        JDK1.1
     */
    public void setBlockIncrement(int v) {
        setPageIncrement(v);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>setBlockIncrement()</code>。
     */
    @Deprecated
    public synchronized void setPageIncrement(int v) {
        int tmp = (v < 1) ? 1 : v;

        if (pageIncrement == tmp) {
            return;
        }
        pageIncrement = tmp;

        ScrollbarPeer peer = (ScrollbarPeer)this.peer;
        if (peer != null) {
            peer.setPageIncrement(pageIncrement);
        }
    }

    /**
     * 获取此滚动条的块增量。
     * <p>
     * 块增量是当用户激活滚动条的块增量区域时添加或减去的值，通常通过滚动条接收到的调整事件中的鼠标或键盘手势来激活。
     * 块增量必须大于零。
     *
     * @return      此滚动条的块增量
     * @see         java.awt.Scrollbar#setBlockIncrement
     * @since       JDK1.1
     */
    public int getBlockIncrement() {
        return getPageIncrement();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getBlockIncrement()</code>。
     */
    @Deprecated
    public int getPageIncrement() {
        return pageIncrement;
    }

    /**
     * 设置此滚动条的四个属性值：<code>value</code>、<code>visibleAmount</code>、
     * <code>minimum</code> 和 <code>maximum</code>。如果提供的这些属性值不一致或不正确，它们将被更改以确保一致性。
     * <p>
     * 此方法同时同步设置滚动条的四个属性值，确保这些属性值相互一致。它强制执行以下约束：
     * <code>maximum</code> 必须大于 <code>minimum</code>，
     * <code>maximum - minimum</code> 必须不大于 <code>Integer.MAX_VALUE</code>，
     * <code>visibleAmount</code> 必须大于零。
     * <code>visibleAmount</code> 必须不大于 <code>maximum - minimum</code>，
     * <code>value</code> 必须不小于 <code>minimum</code>，
     * <code>value</code> 必须不大于 <code>maximum - visibleAmount</code>
     * <p>
     * 调用此方法不会触发 <code>AdjustmentEvent</code>。
     *
     * @param      value 是当前窗口中的位置
     * @param      visible 是滚动条的可见量
     * @param      minimum 是滚动条的最小值
     * @param      maximum 是滚动条的最大值
     * @see        #setMinimum
     * @see        #setMaximum
     * @see        #setVisibleAmount
     * @see        #setValue
     */
    public void setValues(int value, int visible, int minimum, int maximum) {
        int oldValue;
        synchronized (this) {
            if (minimum == Integer.MAX_VALUE) {
                minimum = Integer.MAX_VALUE - 1;
            }
            if (maximum <= minimum) {
                maximum = minimum + 1;
            }


                        long maxMinusMin = (long) maximum - (long) minimum;
            if (maxMinusMin > Integer.MAX_VALUE) {
                maxMinusMin = Integer.MAX_VALUE;
                maximum = minimum + (int) maxMinusMin;
            }
            if (visible > (int) maxMinusMin) {
                visible = (int) maxMinusMin;
            }
            if (visible < 1) {
                visible = 1;
            }

            if (value < minimum) {
                value = minimum;
            }
            if (value > maximum - visible) {
                value = maximum - visible;
            }

            oldValue = this.value;
            this.value = value;
            this.visibleAmount = visible;
            this.minimum = minimum;
            this.maximum = maximum;
            ScrollbarPeer peer = (ScrollbarPeer)this.peer;
            if (peer != null) {
                peer.setValues(value, visibleAmount, minimum, maximum);
            }
        }

        if ((oldValue != value) && (accessibleContext != null))  {
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                    Integer.valueOf(oldValue),
                    Integer.valueOf(value));
        }
    }

    /**
     * 如果值正在由于用户操作而改变，则返回 true。
     *
     * @return <code>valueIsAdjusting</code> 属性的值
     * @see #setValueIsAdjusting
     * @since 1.4
     */
    public boolean getValueIsAdjusting() {
        return isAdjusting;
    }

    /**
     * 设置 <code>valueIsAdjusting</code> 属性。
     *
     * @param b 新的调整中状态
     * @see #getValueIsAdjusting
     * @since 1.4
     */
    public void setValueIsAdjusting(boolean b) {
        boolean oldValue;

        synchronized (this) {
            oldValue = isAdjusting;
            isAdjusting = b;
        }

        if ((oldValue != b) && (accessibleContext != null)) {
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    ((oldValue) ? AccessibleState.BUSY : null),
                    ((b) ? AccessibleState.BUSY : null));
        }
    }



    /**
     * 添加指定的调整监听器以接收来自此滚动条的 <code>AdjustmentEvent</code> 实例。
     * 如果 l 为 <code>null</code>，则不抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param        l 调整监听器
     * @see          #removeAdjustmentListener
     * @see          #getAdjustmentListeners
     * @see          java.awt.event.AdjustmentEvent
     * @see          java.awt.event.AdjustmentListener
     * @since        JDK1.1
     */
    public synchronized void addAdjustmentListener(AdjustmentListener l) {
        if (l == null) {
            return;
        }
        adjustmentListener = AWTEventMulticaster.add(adjustmentListener, l);
        newEventsOnly = true;
    }

    /**
     * 移除指定的调整监听器，使其不再接收来自此滚动条的 <code>AdjustmentEvent</code> 实例。
     * 如果 l 为 <code>null</code>，则不抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param           l    调整监听器
     * @see             #addAdjustmentListener
     * @see             #getAdjustmentListeners
     * @see             java.awt.event.AdjustmentEvent
     * @see             java.awt.event.AdjustmentListener
     * @since           JDK1.1
     */
    public synchronized void removeAdjustmentListener(AdjustmentListener l) {
        if (l == null) {
            return;
        }
        adjustmentListener = AWTEventMulticaster.remove(adjustmentListener, l);
    }

    /**
     * 返回在此滚动条上注册的所有调整监听器的数组。
     *
     * @return 此滚动条的所有 <code>AdjustmentListener</code>，如果没有注册调整监听器，则返回空数组
     * @see             #addAdjustmentListener
     * @see             #removeAdjustmentListener
     * @see             java.awt.event.AdjustmentEvent
     * @see             java.awt.event.AdjustmentListener
     * @since 1.4
     */
    public synchronized AdjustmentListener[] getAdjustmentListeners() {
        return getListeners(AdjustmentListener.class);
    }

    /**
     * 返回在此 <code>Scrollbar</code> 上注册的所有 <code><em>Foo</em>Listener</code> 的数组。
     * <code><em>Foo</em>Listener</code> 是使用 <code>add<em>Foo</em>Listener</code> 方法注册的。
     * <p>
     * 可以使用类字面量指定 <code>listenerType</code> 参数，例如
     * <code><em>Foo</em>Listener.class</code>。
     * 例如，可以使用以下代码查询 <code>Scrollbar</code> <code>c</code> 的鼠标监听器：
     *
     * <pre>MouseListener[] mls = (MouseListener[])(c.getListeners(MouseListener.class));</pre>
     *
     * 如果没有此类监听器存在，此方法返回空数组。
     *
     * @param listenerType 请求的监听器类型；此参数应指定一个继承自
     *          <code>java.util.EventListener</code> 的类或接口
     * @return 在此组件上注册的所有 <code><em>Foo</em>Listener</code> 的数组，
     *          如果没有添加此类监听器，则返回空数组
     * @exception ClassCastException 如果 <code>listenerType</code>
     *          不指定一个实现 <code>java.util.EventListener</code> 的类或接口
     *
     * @since 1.3
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if  (listenerType == AdjustmentListener.class) {
            l = adjustmentListener;
        } else {
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l, listenerType);
    }

    // REMIND: remove when filtering is done at lower level
    boolean eventEnabled(AWTEvent e) {
        if (e.id == AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED) {
            if ((eventMask & AWTEvent.ADJUSTMENT_EVENT_MASK) != 0 ||
                adjustmentListener != null) {
                return true;
            }
            return false;
        }
        return super.eventEnabled(e);
    }

    /**
     * 处理此滚动条上的事件。如果事件是 <code>AdjustmentEvent</code> 的实例，则调用
     * <code>processAdjustmentEvent</code> 方法。否则，调用其父类的
     * <code>processEvent</code> 方法。
     * <p>注意，如果事件参数为 <code>null</code>，则行为未指定，可能会导致异常。
     *
     * @param        e 事件
     * @see          java.awt.event.AdjustmentEvent
     * @see          java.awt.Scrollbar#processAdjustmentEvent
     * @since        JDK1.1
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof AdjustmentEvent) {
            processAdjustmentEvent((AdjustmentEvent)e);
            return;
        }
        super.processEvent(e);
    }

    /**
     * 处理在此滚动条上发生的调整事件，通过将它们分发给所有已注册的
     * <code>AdjustmentListener</code> 对象。
     * <p>
     * 只有在调整事件对此组件启用时，此方法才会被调用。调整事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addAdjustmentListener</code> 注册了 <code>AdjustmentListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了调整事件。
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>，则行为未指定，可能会导致异常。
     *
     * @param       e 调整事件
     * @see         java.awt.event.AdjustmentEvent
     * @see         java.awt.event.AdjustmentListener
     * @see         java.awt.Scrollbar#addAdjustmentListener
     * @see         java.awt.Component#enableEvents
     * @since       JDK1.1
     */
    protected void processAdjustmentEvent(AdjustmentEvent e) {
        AdjustmentListener listener = adjustmentListener;
        if (listener != null) {
            listener.adjustmentValueChanged(e);
        }
    }

    /**
     * 返回表示此 <code>Scrollbar</code> 状态的字符串。
     * 此方法仅用于调试目的，返回的字符串的内容和格式可能因实现而异。返回的字符串可以为空，但不能为
     * <code>null</code>。
     *
     * @return 此滚动条的参数字符串
     */
    protected String paramString() {
        return super.paramString() +
            ",val=" + value +
            ",vis=" + visibleAmount +
            ",min=" + minimum +
            ",max=" + maximum +
            ((orientation == VERTICAL) ? ",vert" : ",horz") +
            ",isAdjusting=" + isAdjusting;
    }


    /* Serialization support.
     */

    /**
     * 滚动条的序列化数据版本。
     *
     * @serial
     */
    private int scrollbarSerializedDataVersion = 1;

    /**
     * 将默认的可序列化字段写入流中。将可序列化的 <code>AdjustmentListeners</code> 列表作为可选数据写入。
     * 非可序列化的监听器将被检测到，不会尝试序列化它们。
     *
     * @param s 要写入的 <code>ObjectOutputStream</code>
     * @serialData 以 <code>null</code> 结尾的 0 个或多个对的序列；每个对由一个 <code>String</code>
     *   和一个 <code>Object</code> 组成；<code>String</code> 表示对象的类型，可以是以下之一：
     *   <code>adjustmentListenerK</code> 表示一个 <code>AdjustmentListener</code> 对象
     *
     * @see AWTEventMulticaster#save(ObjectOutputStream, String, EventListener)
     * @see java.awt.Component#adjustmentListenerK
     * @see #readObject(ObjectInputStream)
     */
    private void writeObject(ObjectOutputStream s)
      throws IOException
    {
      s.defaultWriteObject();

      AWTEventMulticaster.save(s, adjustmentListenerK, adjustmentListener);
      s.writeObject(null);
    }

    /**
     * 读取 <code>ObjectInputStream</code>，如果它不为 <code>null</code>，则添加一个监听器以接收
     * <code>Scrollbar</code> 触发的调整事件。未识别的键或值将被忽略。
     *
     * @param s 要读取的 <code>ObjectInputStream</code>
     * @exception HeadlessException 如果 <code>GraphicsEnvironment.isHeadless</code> 返回
     *   <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see #writeObject(ObjectOutputStream)
     */
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException, HeadlessException
    {
      GraphicsEnvironment.checkHeadless();
      s.defaultReadObject();

      Object keyOrNull;
      while(null != (keyOrNull = s.readObject())) {
        String key = ((String)keyOrNull).intern();

        if (adjustmentListenerK == key)
          addAdjustmentListener((AdjustmentListener)(s.readObject()));

        else // skip value for unrecognized key
          s.readObject();
      }
    }


/////////////////
// Accessibility support
////////////////

    /**
     * 获取与此 <code>Scrollbar</code> 关联的 <code>AccessibleContext</code>。对于滚动条，
     * <code>AccessibleContext</code> 采用 <code>AccessibleAWTScrollBar</code> 的形式。如果必要，将创建一个新的
     * <code>AccessibleAWTScrollBar</code> 实例。
     *
     * @return 一个 <code>AccessibleAWTScrollBar</code>，作为此 <code>ScrollBar</code> 的
     *         <code>AccessibleContext</code>
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTScrollBar();
        }
        return accessibleContext;
    }

    /**
     * 为 <code>Scrollbar</code> 类实现辅助功能支持。它为滚动条用户界面元素提供了适当的
     * Java 辅助功能 API 实现。
     * @since 1.3
     */
    protected class AccessibleAWTScrollBar extends AccessibleAWTComponent
        implements AccessibleValue
    {
        /*
         * JDK 1.3 serialVersionUID
         */
        private static final long serialVersionUID = -344337268523697807L;

        /**
         * 获取此对象的状态集。
         *
         * @return 一个包含对象当前状态的 <code>AccessibleState</code> 实例
         * @see AccessibleState
         */
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (getValueIsAdjusting()) {
                states.add(AccessibleState.BUSY);
            }
            if (getOrientation() == VERTICAL) {
                states.add(AccessibleState.VERTICAL);
            } else {
                states.add(AccessibleState.HORIZONTAL);
            }
            return states;
        }

        /**
         * 获取此对象的角色。
         *
         * @return 一个描述对象角色的 <code>AccessibleRole</code> 实例
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SCROLL_BAR;
        }

        /**
         * 获取与此对象关联的 <code>AccessibleValue</code>。在此类的 Java 辅助功能 API 实现中，
         * 返回此对象，它负责代表自身实现 <code>AccessibleValue</code> 接口。
         *
         * @return 此对象
         */
        public AccessibleValue getAccessibleValue() {
            return this;
        }

        /**
         * 获取此对象的辅助值。
         *
         * @return 此对象的当前值。
         */
        public Number getCurrentAccessibleValue() {
            return Integer.valueOf(getValue());
        }

        /**
         * 将此对象的值设置为一个数字。
         *
         * @return 如果值已设置，则返回 true。
         */
        public boolean setCurrentAccessibleValue(Number n) {
            if (n instanceof Integer) {
                setValue(n.intValue());
                return true;
            } else {
                return false;
            }
        }

        /**
         * 获取此对象的最小辅助值。
         *
         * @return 此对象的最小值。
         */
        public Number getMinimumAccessibleValue() {
            return Integer.valueOf(getMinimum());
        }


                    /**
         * 获取此对象的最大可访问值。
         *
         * @return 此对象的最大值。
         */
        public Number getMaximumAccessibleValue() {
            return Integer.valueOf(getMaximum());
        }

    } // AccessibleAWTScrollBar

}
