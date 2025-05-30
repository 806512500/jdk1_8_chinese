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

package java.awt.event;

import java.awt.Adjustable;
import java.awt.AWTEvent;
import java.lang.annotation.Native;


/**
 * 由 {@link java.awt.Scrollbar} 和 {@link java.awt.ScrollPane} 等 Adjustable 对象发出的调整事件。
 * 当用户更改滚动组件的值时，会收到一个 {@code AdjustmentEvent} 实例。
 * <p>
 * 如果任何特定的 {@code AdjustmentEvent} 实例的 {@code id} 参数不在 {@code ADJUSTMENT_FIRST} 到 {@code ADJUSTMENT_LAST} 的范围内，
 * 将导致未指定的行为。
 * <p>
 * 任何 {@code AdjustmentEvent} 实例的 {@code type} 可以取以下值之一：
 *                     <ul>
 *                     <li> {@code UNIT_INCREMENT}
 *                     <li> {@code UNIT_DECREMENT}
 *                     <li> {@code BLOCK_INCREMENT}
 *                     <li> {@code BLOCK_DECREMENT}
 *                     <li> {@code TRACK}
 *                     </ul>
 * 赋予上述列表之外的值将导致未指定的行为。
 * @see java.awt.Adjustable
 * @see AdjustmentListener
 *
 * @author Amy Fowler
 * @since 1.1
 */
public class AdjustmentEvent extends AWTEvent {

    /**
     * 标记调整事件 ID 范围的第一个整数 ID。
     */
    public static final int ADJUSTMENT_FIRST    = 601;

    /**
     * 标记调整事件 ID 范围的最后一个整数 ID。
     */
    public static final int ADJUSTMENT_LAST     = 601;

    /**
     * 调整值更改事件。
     */
    public static final int ADJUSTMENT_VALUE_CHANGED = ADJUSTMENT_FIRST; //Event.SCROLL_LINE_UP

    /**
     * 单位增量调整类型。
     */
    @Native public static final int UNIT_INCREMENT      = 1;

    /**
     * 单位减量调整类型。
     */
    @Native public static final int UNIT_DECREMENT      = 2;

    /**
     * 块减量调整类型。
     */
    @Native public static final int BLOCK_DECREMENT     = 3;

    /**
     * 块增量调整类型。
     */
    @Native public static final int BLOCK_INCREMENT     = 4;

    /**
     * 绝对跟踪调整类型。
     */
    @Native public static final int TRACK               = 5;

    /**
     * 触发事件的可调整对象。
     *
     * @serial
     * @see #getAdjustable
     */
    Adjustable adjustable;

    /**
     * <code>value</code> 将包含可调整对象的新值。此值始终在与可调整对象关联的范围内。
     *
     * @serial
     * @see #getValue
     */
    int value;

    /**
     * <code>adjustmentType</code> 描述了可调整对象值如何变化。
     * 此值可以按块或单位量增加/减少，其中块与页面增量/减少相关联，单位与行增量/减少相关联。
     *
     * @serial
     * @see #getAdjustmentType
     */
    int adjustmentType;

    /**
     * 如果事件是多个调整事件之一，则 <code>isAdjusting</code> 为 true。
     *
     * @since 1.4
     * @serial
     * @see #getValueIsAdjusting
     */
    boolean isAdjusting;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = 5700290645205279921L;

    /**
     * 构造一个具有指定 <code>Adjustable</code> 源、事件类型、调整类型和值的 <code>AdjustmentEvent</code> 对象。
     * <p> 如果 <code>source</code> 为 <code>null</code>，此方法将抛出 <code>IllegalArgumentException</code>。
     *
     * @param source 事件源 <code>Adjustable</code> 对象
     * @param id     表示事件类型的整数。
     *                     有关允许值的信息，请参见 {@link AdjustmentEvent} 的类描述
     * @param type   表示调整类型的整数。
     *                     有关允许值的信息，请参见 {@link AdjustmentEvent} 的类描述
     * @param value  调整的当前值
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getSource()
     * @see #getID()
     * @see #getAdjustmentType()
     * @see #getValue()
     */
    public AdjustmentEvent(Adjustable source, int id, int type, int value) {
        this(source, id, type, value, false);
    }

    /**
     * 构造一个具有指定 Adjustable 源、事件类型、调整类型和值的 <code>AdjustmentEvent</code> 对象。
     * <p> 如果 <code>source</code> 为 <code>null</code>，此方法将抛出 <code>IllegalArgumentException</code>。
     *
     * @param source 事件源 <code>Adjustable</code> 对象
     * @param id     表示事件类型的整数。
     *                     有关允许值的信息，请参见 {@link AdjustmentEvent} 的类描述
     * @param type   表示调整类型的整数。
     *                     有关允许值的信息，请参见 {@link AdjustmentEvent} 的类描述
     * @param value  调整的当前值
     * @param isAdjusting 如果事件是多个调整事件之一，则为 <code>true</code>，否则为 <code>false</code>
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @since 1.4
     * @see #getSource()
     * @see #getID()
     * @see #getAdjustmentType()
     * @see #getValue()
     * @see #getValueIsAdjusting()
     */
    public AdjustmentEvent(Adjustable source, int id, int type, int value, boolean isAdjusting) {
        super(source, id);
        adjustable = source;
        this.adjustmentType = type;
        this.value = value;
        this.isAdjusting = isAdjusting;
    }

    /**
     * 返回此事件的源 <code>Adjustable</code> 对象。
     *
     * @return 此事件的源 <code>Adjustable</code> 对象
     */
    public Adjustable getAdjustable() {
        return adjustable;
    }

    /**
     * 返回调整事件中的当前值。
     *
     * @return 调整事件中的当前值
     */
    public int getValue() {
        return value;
    }

    /**
     * 返回导致值更改事件的调整类型。它将具有以下值之一：
     * <ul>
     * <li>{@link #UNIT_INCREMENT}
     * <li>{@link #UNIT_DECREMENT}
     * <li>{@link #BLOCK_INCREMENT}
     * <li>{@link #BLOCK_DECREMENT}
     * <li>{@link #TRACK}
     * </ul>
     * @return 上述列表中的一个调整值
     */
    public int getAdjustmentType() {
        return adjustmentType;
    }

    /**
     * 如果这是多个调整事件之一，则返回 <code>true</code>。
     *
     * @return 如果这是多个调整事件之一，则返回 <code>true</code>，否则返回 <code>false</code>
     * @since 1.4
     */
    public boolean getValueIsAdjusting() {
        return isAdjusting;
    }

    public String paramString() {
        String typeStr;
        switch(id) {
          case ADJUSTMENT_VALUE_CHANGED:
              typeStr = "ADJUSTMENT_VALUE_CHANGED";
              break;
          default:
              typeStr = "unknown type";
        }
        String adjTypeStr;
        switch(adjustmentType) {
          case UNIT_INCREMENT:
              adjTypeStr = "UNIT_INCREMENT";
              break;
          case UNIT_DECREMENT:
              adjTypeStr = "UNIT_DECREMENT";
              break;
          case BLOCK_INCREMENT:
              adjTypeStr = "BLOCK_INCREMENT";
              break;
          case BLOCK_DECREMENT:
              adjTypeStr = "BLOCK_DECREMENT";
              break;
          case TRACK:
              adjTypeStr = "TRACK";
              break;
          default:
              adjTypeStr = "unknown type";
        }
        return typeStr
            + ",adjType="+adjTypeStr
            + ",value="+value
            + ",isAdjusting="+isAdjusting;
    }
}
