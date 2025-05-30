/*
 * Copyright (c) 1996, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.event;

import java.awt.Component;
import java.awt.Rectangle;

/**
 * 组件级别的绘制事件。
 * 此事件是一种特殊类型，用于确保 paint/update 方法调用与从事件队列传递的其他事件一起序列化。此事件不设计用于事件监听器模型；程序应继续覆盖 paint/update 方法以正确渲染自身。
 * <p>
 * 如果任何特定的 {@code PaintEvent} 实例的 {@code id} 参数不在 {@code PAINT_FIRST} 到 {@code PAINT_LAST} 的范围内，则会导致未指定的行为。
 *
 * @author Amy Fowler
 * @since 1.1
 */
public class PaintEvent extends ComponentEvent {

    /**
     * 标记绘制事件 ID 范围的第一个整数 ID。
     */
    public static final int PAINT_FIRST         = 800;

    /**
     * 标记绘制事件 ID 范围的最后一个整数 ID。
     */
    public static final int PAINT_LAST          = 801;

    /**
     * 绘制事件类型。
     */
    public static final int PAINT = PAINT_FIRST;

    /**
     * 更新事件类型。
     */
    public static final int UPDATE = PAINT_FIRST + 1; //801

    /**
     * 这个矩形表示源组件上需要重绘的区域。
     * 这个矩形不应为 null。
     *
     * @serial
     * @see java.awt.Rectangle
     * @see #setUpdateRect(Rectangle)
     * @see #getUpdateRect()
     */
    Rectangle updateRect;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = 1267492026433337593L;

    /**
     * 使用指定的源组件和类型构造一个 <code>PaintEvent</code> 对象。
     * <p> 如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source     事件的来源对象
     * @param id           识别事件类型的整数。
     *                     有关允许值的信息，请参见 {@link PaintEvent} 的类描述
     * @param updateRect 需要重绘的矩形区域
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getSource()
     * @see #getID()
     * @see #getUpdateRect()
     */
    public PaintEvent(Component source, int id, Rectangle updateRect) {
        super(source, id);
        this.updateRect = updateRect;
    }

    /**
     * 返回表示此事件响应需要重绘的区域的矩形。
     */
    public Rectangle getUpdateRect() {
        return updateRect;
    }

    /**
     * 设置表示此事件响应需要重绘的区域的矩形。
     * @param updateRect 需要重绘的矩形区域
     */
    public void setUpdateRect(Rectangle updateRect) {
        this.updateRect = updateRect;
    }

    public String paramString() {
        String typeStr;
        switch(id) {
          case PAINT:
              typeStr = "PAINT";
              break;
          case UPDATE:
              typeStr = "UPDATE";
              break;
          default:
              typeStr = "unknown type";
        }
        return typeStr + ",updateRect="+(updateRect != null ? updateRect.toString() : "null");
    }
}
