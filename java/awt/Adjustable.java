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

import java.awt.event.*;

import java.lang.annotation.Native;

/**
 * 具有在限定值范围内可调数值的对象的接口。
 *
 * @author Amy Fowler
 * @author Tim Prinzing
 */
public interface Adjustable {

    /**
     * 表示 <code>Adjustable</code> 具有水平方向。
     */
    @Native public static final int HORIZONTAL = 0;

    /**
     * 表示 <code>Adjustable</code> 具有垂直方向。
     */
    @Native public static final int VERTICAL = 1;

    /**
     * 表示 <code>Adjustable</code> 没有方向。
     */
    @Native public static final int NO_ORIENTATION = 2;

    /**
     * 获取可调对象的方向。
     * @return 可调对象的方向；可以是 <code>HORIZONTAL</code>、<code>VERTICAL</code> 或 <code>NO_ORIENTATION</code>
     */
    int getOrientation();

    /**
     * 设置可调对象的最小值。
     * @param min 最小值
     */
    void setMinimum(int min);

    /**
     * 获取可调对象的最小值。
     * @return 可调对象的最小值
     */
    int getMinimum();

    /**
     * 设置可调对象的最大值。
     * @param max 最大值
     */
    void setMaximum(int max);

    /**
     * 获取可调对象的最大值。
     * @return 可调对象的最大值
     */
    int getMaximum();

    /**
     * 设置可调对象的单位增量。
     * @param u 单位增量
     */
    void setUnitIncrement(int u);

    /**
     * 获取可调对象的单位增量。
     * @return 可调对象的单位增量
     */
    int getUnitIncrement();

    /**
     * 设置可调对象的块增量。
     * @param b 块增量
     */
    void setBlockIncrement(int b);

    /**
     * 获取可调对象的块增量。
     * @return 可调对象的块增量
     */
    int getBlockIncrement();

    /**
     * 设置可调对象的比例指示器的长度。
     * @param v 指示器的长度
     */
    void setVisibleAmount(int v);

    /**
     * 获取比例指示器的长度。
     * @return 比例指示器的长度
     */
    int getVisibleAmount();

    /**
     * 设置可调对象的当前值。如果提供的值小于 <code>minimum</code> 或大于 <code>maximum</code> - <code>visibleAmount</code>，
     * 则根据情况用其中一个值替代。
     * <p>
     * 调用此方法不会触发 <code>AdjustmentEvent</code>。
     *
     * @param v 当前值，介于 <code>minimum</code> 和 <code>maximum</code> - <code>visibleAmount</code> 之间
     */
    void setValue(int v);

    /**
     * 获取可调对象的当前值。
     * @return 可调对象的当前值
     */
    int getValue();

    /**
     * 添加一个监听器，当可调对象的值改变时接收调整事件。
     * @param l 接收事件的监听器
     * @see AdjustmentEvent
     */
    void addAdjustmentListener(AdjustmentListener l);

    /**
     * 移除一个调整监听器。
     * @param l 被移除的监听器
     * @see AdjustmentEvent
     */
    void removeAdjustmentListener(AdjustmentListener l);

}
