/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

import sun.awt.AWTAccessor;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.peer.ScrollPanePeer;
import java.io.Serializable;


/**
 * 该类表示一个水平或垂直滚动条的状态，这些滚动条属于 <code>ScrollPane</code>。此类对象
 * 由 <code>ScrollPane</code> 方法返回。
 *
 * @since       1.4
 */
public class ScrollPaneAdjustable implements Adjustable, Serializable {

    /**
     * 该对象所属的 <code>ScrollPane</code>。
     * @serial
     */
    private ScrollPane sp;

    /**
     * 该滚动条的方向。
     *
     * @serial
     * @see #getOrientation
     * @see java.awt.Adjustable#HORIZONTAL
     * @see java.awt.Adjustable#VERTICAL
     */
    private int orientation;

    /**
     * 该滚动条的值。
     * <code>value</code> 应大于 <code>minimum</code> 且小于 <code>maximum</code>。
     *
     * @serial
     * @see #getValue
     * @see #setValue
     */
    private int value;

    /**
     * 该滚动条的最小值。
     * 该值只能由 <code>ScrollPane</code> 设置。
     * <p>
     * <strong>注意：</strong> 在当前实现中，<code>minimum</code> 始终为 <code>0</code>。此字段只能通过
     * <code>setSpan</code> 方法更改，而 <code>ScrollPane</code> 始终调用该方法并将 <code>0</code>
     * 作为最小值。<code>getMinimum</code> 方法始终返回 <code>0</code> 而不检查此字段。
     *
     * @serial
     * @see #getMinimum
     * @see #setSpan(int, int, int)
     */
    private int minimum;

    /**
     * 该滚动条的最大值。
     * 该值只能由 <code>ScrollPane</code> 设置。
     *
     * @serial
     * @see #getMaximum
     * @see #setSpan(int, int, int)
     */
    private int maximum;

    /**
     * 该滚动条可见部分的大小。
     * 该值只能由 <code>ScrollPane</code> 设置。
     *
     * @serial
     * @see #getVisibleAmount
     * @see #setSpan(int, int, int)
     */
    private int visibleAmount;

    /**
     * 滚动条的调整状态。
     * 如果值正在由于用户的操作而改变，则为 true。
     *
     * @see #getValueIsAdjusting
     * @see #setValueIsAdjusting
     * @since 1.4
     */
    private transient boolean isAdjusting;

    /**
     * 滚动条值在逐行上下移动时的变化量。
     * 该值应为非负整数。
     *
     * @serial
     * @see #getUnitIncrement
     * @see #setUnitIncrement
     */
    private int unitIncrement  = 1;

    /**
     * 滚动条值在逐页上下移动时的变化量。
     * 该值应为非负整数。
     *
     * @serial
     * @see #getBlockIncrement
     * @see #setBlockIncrement
     */
    private int blockIncrement = 1;

    private AdjustmentListener adjustmentListener;

    /**
     * 当调用其中一个公共但不受支持的方法时报告的 <code>AWTError</code> 错误消息。
     */
    private static final String SCROLLPANE_ONLY =
        "Can be set by scrollpane only";


    /**
     * 初始化 JNI 字段和方法 ID。
     */
    private static native void initIDs();

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
        AWTAccessor.setScrollPaneAdjustableAccessor(new AWTAccessor.ScrollPaneAdjustableAccessor() {
            public void setTypedValue(final ScrollPaneAdjustable adj,
                                      final int v, final int type) {
                adj.setTypedValue(v, type);
            }
        });
    }

    /**
     * JDK 1.1 序列化版本 ID。
     */
    private static final long serialVersionUID = -3359745691033257079L;


    /**
     * 构造一个新的对象，以表示指定的 <code>ScrollPane</code> 的指定滚动条。
     * 只有 ScrollPane 创建此类的实例。
     * @param sp           <code>ScrollPane</code>
     * @param l            创建时要添加的 <code>AdjustmentListener</code>。
     * @param orientation  指定此对象代表的滚动条，可以是 <code>Adjustable.HORIZONTAL</code>
     *                     或 <code>Adjustable.VERTICAL</code>。
     */
    ScrollPaneAdjustable(ScrollPane sp, AdjustmentListener l, int orientation) {
        this.sp = sp;
        this.orientation = orientation;
        addAdjustmentListener(l);
    }

    /**
     * 由滚动条本身调用以更新 <code>minimum</code>、<code>maximum</code> 和
     * <code>visible</code> 值。只有滚动条才能更改这些值，因为它是这些值的来源。
     */
    void setSpan(int min, int max, int visible) {
        // 调整值以使其合理
        minimum = min;
        maximum = Math.max(max, minimum + 1);
        visibleAmount = Math.min(visible, maximum - minimum);
        visibleAmount = Math.max(visibleAmount, 1);
        blockIncrement = Math.max((int)(visible * .90), 1);
        setValue(value);
    }

    /**
     * 返回该滚动条的方向。
     * @return    该滚动条的方向，可以是 <code>Adjustable.HORIZONTAL</code> 或
     *            <code>Adjustable.VERTICAL</code>
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * 用户代码<strong>不应</strong>调用此方法。
     * 该方法是公共的，以便此类正确实现 <code>Adjustable</code> 接口。
     *
     * @throws AWTError 始终在调用时抛出错误。
     */
    public void setMinimum(int min) {
        throw new AWTError(SCROLLPANE_ONLY);
    }

    public int getMinimum() {
        // XXX: 这依赖于 setSpan 始终被调用，且最小值为 0（这是当前的情况）。
        return 0;
    }

    /**
     * 用户代码<strong>不应</strong>调用此方法。
     * 该方法是公共的，以便此类正确实现 <code>Adjustable</code> 接口。
     *
     * @throws AWTError 始终在调用时抛出错误。
     */
    public void setMaximum(int max) {
        throw new AWTError(SCROLLPANE_ONLY);
    }

    public int getMaximum() {
        return maximum;
    }

    public synchronized void setUnitIncrement(int u) {
        if (u != unitIncrement) {
            unitIncrement = u;
            if (sp.peer != null) {
                ScrollPanePeer peer = (ScrollPanePeer) sp.peer;
                peer.setUnitIncrement(this, u);
            }
        }
    }

    public int getUnitIncrement() {
        return unitIncrement;
    }

    public synchronized void setBlockIncrement(int b) {
        blockIncrement = b;
    }

    public int getBlockIncrement() {
        return blockIncrement;
    }

    /**
     * 用户代码<strong>不应</strong>调用此方法。
     * 该方法是公共的，以便此类正确实现 <code>Adjustable</code> 接口。
     *
     * @throws AWTError 始终在调用时抛出错误。
     */
    public void setVisibleAmount(int v) {
        throw new AWTError(SCROLLPANE_ONLY);
    }

    public int getVisibleAmount() {
        return visibleAmount;
    }


    /**
     * 设置 <code>valueIsAdjusting</code> 属性。
     *
     * @param b 新的调整进行中状态
     * @see #getValueIsAdjusting
     * @since 1.4
     */
    public void setValueIsAdjusting(boolean b) {
        if (isAdjusting != b) {
            isAdjusting = b;
            AdjustmentEvent e =
                new AdjustmentEvent(this,
                        AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED,
                        AdjustmentEvent.TRACK, value, b);
            adjustmentListener.adjustmentValueChanged(e);
        }
    }

    /**
     * 如果值正在由于用户的操作而改变，则返回 true。
     *
     * @return <code>valueIsAdjusting</code> 属性的值
     * @see #setValueIsAdjusting
     */
    public boolean getValueIsAdjusting() {
        return isAdjusting;
    }

    /**
     * 将此滚动条的值设置为指定值。
     * <p>
     * 如果提供的值小于当前最小值或大于当前最大值，则用其中一个值替换，如适用。
     *
     * @param v 滚动条的新值
     */
    public void setValue(int v) {
        setTypedValue(v, AdjustmentEvent.TRACK);
    }

    /**
     * 将此滚动条的值设置为指定值。
     * <p>
     * 如果提供的值小于当前最小值或大于当前最大值，则用其中一个值替换，如适用。此外，创建并分发
     * 指定类型和值的 AdjustementEvent。
     *
     * @param v 滚动条的新值
     * @param type 发生的滚动操作类型
     */
    private void setTypedValue(int v, int type) {
        v = Math.max(v, minimum);
        v = Math.min(v, maximum - visibleAmount);

        if (v != value) {
            value = v;
            // 同步通知监听器，以确保在再次更改 Adjustable 之前，监听器已更新
            AdjustmentEvent e =
                new AdjustmentEvent(this,
                        AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED,
                        type, value, isAdjusting);
            adjustmentListener.adjustmentValueChanged(e);
        }
    }

    public int getValue() {
        return value;
    }

    /**
     * 添加指定的调整监听器以接收此 <code>ScrollPaneAdjustable</code> 的调整事件。
     * 如果 <code>l</code> 为 <code>null</code>，则不抛出异常且不执行任何操作。
     * <p>请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a> 以了解 AWT 的线程模型。
     *
     * @param    l   调整监听器。
     * @see      #removeAdjustmentListener
     * @see      #getAdjustmentListeners
     * @see      java.awt.event.AdjustmentListener
     * @see      java.awt.event.AdjustmentEvent
     */
    public synchronized void addAdjustmentListener(AdjustmentListener l) {
        if (l == null) {
            return;
        }
        adjustmentListener = AWTEventMulticaster.add(adjustmentListener, l);
    }

    /**
     * 移除指定的调整监听器，使其不再接收此 <code>ScrollPaneAdjustable</code> 的调整事件。
     * 如果 <code>l</code> 为 <code>null</code>，则不抛出异常且不执行任何操作。
     * <p>请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a> 以了解 AWT 的线程模型。
     *
     * @param         l     调整监听器。
     * @see           #addAdjustmentListener
     * @see           #getAdjustmentListeners
     * @see           java.awt.event.AdjustmentListener
     * @see           java.awt.event.AdjustmentEvent
     * @since         JDK1.1
     */
    public synchronized void removeAdjustmentListener(AdjustmentListener l){
        if (l == null) {
            return;
        }
        adjustmentListener = AWTEventMulticaster.remove(adjustmentListener, l);
    }

    /**
     * 返回注册在此 <code>ScrollPaneAdjustable</code> 上的所有调整监听器的数组。
     *
     * @return 此 <code>ScrollPaneAdjustable</code> 的所有
     *         <code>AdjustmentListener</code>，如果当前没有注册调整
     *         监听器，则返回一个空数组。
     *
     * @see           #addAdjustmentListener
     * @see           #removeAdjustmentListener
     * @see           java.awt.event.AdjustmentListener
     * @see           java.awt.event.AdjustmentEvent
     * @since 1.4
     */
    public synchronized AdjustmentListener[] getAdjustmentListeners() {
        return (AdjustmentListener[])(AWTEventMulticaster.getListeners(
                                      adjustmentListener,
                                      AdjustmentListener.class));
    }

    /**
     * 返回此滚动条及其值的字符串表示形式。
     * @return    此滚动条的字符串表示形式。
     */
    public String toString() {
        return getClass().getName() + "[" + paramString() + "]";
    }

    /**
     * 返回表示此滚动条状态的字符串。
     * 此方法仅用于调试目的，返回字符串的内容和格式可能因实现而异。返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return      此滚动条的参数字符串。
     */
    public String paramString() {
        return ((orientation == Adjustable.VERTICAL ? "vertical,"
                                                    :"horizontal,")
                + "[0.."+maximum+"]"
                + ",val=" + value
                + ",vis=" + visibleAmount
                + ",unit=" + unitIncrement
                + ",block=" + blockIncrement
                + ",isAdjusting=" + isAdjusting);
    }
}
