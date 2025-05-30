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

import java.awt.event.InputEvent;

/**
 * <code>DragSourceDragEvent</code> 由 <code>DragSourceContextPeer</code> 通过
 * <code>DragSourceContext</code> 发送到注册在该 <code>DragSourceContext</code>
 * 和其关联的 <code>DragSource</code> 上的 <code>DragSourceListener</code>。
 * <p>
 * <code>DragSourceDragEvent</code> 报告 <i>目标放置操作</i> 和 <i>用户放置操作</i>，
 * 这些反映了拖动操作的当前状态。
 * <p>
 * <i>目标放置操作</i> 是 <code>DnDConstants</code> 中的一个值，表示当前放置目标选择的放置操作，
 * 如果该放置操作被拖动源支持；否则为 <code>DnDConstants.ACTION_NONE</code>。
 * <p>
 * <i>用户放置操作</i> 取决于拖动源支持的放置操作和用户选择的放置操作。用户可以通过在拖动操作期间按住修饰键来选择放置操作：
 * <pre>
 *   Ctrl + Shift -&gt; ACTION_LINK
 *   Ctrl         -&gt; ACTION_COPY
 *   Shift        -&gt; ACTION_MOVE
 * </pre>
 * 如果用户选择了放置操作，<i>用户放置操作</i> 是 <code>DnDConstants</code> 中的一个值，表示选定的放置操作，
 * 如果该放置操作被拖动源支持；否则为 <code>DnDConstants.ACTION_NONE</code>。
 * <p>
 * 如果用户没有选择放置操作，则搜索 <code>DnDConstants</code> 中表示拖动源支持的放置操作的集合，
 * 依次查找 <code>DnDConstants.ACTION_MOVE</code>，然后查找 <code>DnDConstants.ACTION_COPY</code>，
 * 然后查找 <code>DnDConstants.ACTION_LINK</code>，<i>用户放置操作</i> 是找到的第一个常量。
 * 如果没有找到常量，则 <i>用户放置操作</i> 为 <code>DnDConstants.ACTION_NONE</code>。
 *
 * @since 1.2
 *
 */

public class DragSourceDragEvent extends DragSourceEvent {

    private static final long serialVersionUID = 481346297933902471L;

    /**
     * 构造一个 <code>DragSourceDragEvent</code>。
     * 该类通常由 <code>DragSourceContextPeer</code> 实例化，而不是直接由客户端代码实例化。
     * 该 <code>DragSourceDragEvent</code> 的坐标未指定，因此 <code>getLocation</code> 将返回
     * <code>null</code>。
     * <p>
     * 参数 <code>dropAction</code> 和 <code>action</code> 应该是 <code>DnDConstants</code> 中的一个值，
     * 表示单个操作。参数 <code>modifiers</code> 应该是旧的 <code>java.awt.event.InputEvent.*_MASK</code>
     * 常量的位掩码，或者是扩展的 <code>java.awt.event.InputEvent.*_DOWN_MASK</code> 常量的位掩码。
     * 该构造函数不会因无效的 <code>dropAction</code>、<code>action</code> 和 <code>modifiers</code>
     * 而抛出任何异常。
     *
     * @param dsc 管理此事件通知的 <code>DragSourceContext</code>。
     * @param dropAction 用户放置操作。
     * @param action 目标放置操作。
     * @param modifiers 事件期间按下的修饰键（shift, ctrl, alt, meta）
     *        应使用扩展的 _DOWN_MASK 或旧的 _MASK 修饰符，但不应在一个事件中混合使用两种模型。
     *        建议使用扩展修饰符。
     *
     * @throws IllegalArgumentException 如果 <code>dsc</code> 为 <code>null</code>。
     *
     * @see java.awt.event.InputEvent
     * @see DragSourceEvent#getLocation
     */

    public DragSourceDragEvent(DragSourceContext dsc, int dropAction,
                               int action, int modifiers) {
        super(dsc);

        targetActions    = action;
        gestureModifiers = modifiers;
        this.dropAction  = dropAction;
        if ((modifiers & ~(JDK_1_3_MODIFIERS | JDK_1_4_MODIFIERS)) != 0) {
            invalidModifiers = true;
        } else if ((getGestureModifiers() != 0) && (getGestureModifiersEx() == 0)) {
            setNewModifiers();
        } else if ((getGestureModifiers() == 0) && (getGestureModifiersEx() != 0)) {
            setOldModifiers();
        } else {
            invalidModifiers = true;
        }
    }

    /**
     * 构造一个 <code>DragSourceDragEvent</code>，给定指定的 <code>DragSourceContext</code>、
     * 用户放置操作、目标放置操作、修饰符和坐标。
     * <p>
     * 参数 <code>dropAction</code> 和 <code>action</code> 应该是 <code>DnDConstants</code> 中的一个值，
     * 表示单个操作。参数 <code>modifiers</code> 应该是旧的 <code>java.awt.event.InputEvent.*_MASK</code>
     * 常量的位掩码，或者是扩展的 <code>java.awt.event.InputEvent.*_DOWN_MASK</code> 常量的位掩码。
     * 该构造函数不会因无效的 <code>dropAction</code>、<code>action</code> 和 <code>modifiers</code>
     * 而抛出任何异常。
     *
     * @param dsc 与该事件关联的 <code>DragSourceContext</code>。
     * @param dropAction 用户放置操作。
     * @param action 目标放置操作。
     * @param modifiers 事件期间按下的修饰键（shift, ctrl, alt, meta）
     *        应使用扩展的 _DOWN_MASK 或旧的 _MASK 修饰符，但不应在一个事件中混合使用两种模型。
     *        建议使用扩展修饰符。
     * @param x   光标位置的水平坐标
     * @param y   光标位置的垂直坐标
     *
     * @throws IllegalArgumentException 如果 <code>dsc</code> 为 <code>null</code>。
     *
     * @see java.awt.event.InputEvent
     * @since 1.4
     */
    public DragSourceDragEvent(DragSourceContext dsc, int dropAction,
                               int action, int modifiers, int x, int y) {
        super(dsc, x, y);

        targetActions    = action;
        gestureModifiers = modifiers;
        this.dropAction  = dropAction;
        if ((modifiers & ~(JDK_1_3_MODIFIERS | JDK_1_4_MODIFIERS)) != 0) {
            invalidModifiers = true;
        } else if ((getGestureModifiers() != 0) && (getGestureModifiersEx() == 0)) {
            setNewModifiers();
        } else if ((getGestureModifiers() == 0) && (getGestureModifiersEx() != 0)) {
            setOldModifiers();
        } else {
            invalidModifiers = true;
        }
    }

    /**
     * 返回目标放置操作。
     *
     * @return 目标放置操作。
     */
    public int getTargetActions() {
        return targetActions;
    }


    private static final int JDK_1_3_MODIFIERS = InputEvent.SHIFT_DOWN_MASK - 1;
    private static final int JDK_1_4_MODIFIERS =
            ((InputEvent.ALT_GRAPH_DOWN_MASK << 1) - 1) & ~JDK_1_3_MODIFIERS;

    /**
     * 返回一个 <code>int</code>，表示与用户手势关联的输入设备修饰符的当前状态。
     * 通常这些会是鼠标按钮或键盘修饰符。
     * <P>
     * 如果构造函数传递的 <code>modifiers</code> 无效，此方法将返回它们不变。
     *
     * @return 输入设备修饰符的当前状态
     */

    public int getGestureModifiers() {
        return invalidModifiers ? gestureModifiers : gestureModifiers & JDK_1_3_MODIFIERS;
    }

    /**
     * 返回一个 <code>int</code>，表示与用户手势关联的输入设备扩展修饰符的当前状态。
     * 请参阅 {@link InputEvent#getModifiersEx}
     * <P>
     * 如果构造函数传递的 <code>modifiers</code> 无效，此方法将返回它们不变。
     *
     * @return 输入设备扩展修饰符的当前状态
     * @since 1.4
     */

    public int getGestureModifiersEx() {
        return invalidModifiers ? gestureModifiers : gestureModifiers & JDK_1_4_MODIFIERS;
    }

    /**
     * 返回用户放置操作。
     *
     * @return 用户放置操作。
     */
    public int getUserAction() { return dropAction; }

    /**
     * 返回目标放置操作与拖动源支持的放置操作集的逻辑交集。
     *
     * @return 目标放置操作与拖动源支持的放置操作集的逻辑交集。
     */
    public int getDropAction() {
        return targetActions & getDragSourceContext().getSourceActions();
    }

    /*
     * 字段
     */

    /**
     * 目标放置操作。
     *
     * @serial
     */
    private int     targetActions    = DnDConstants.ACTION_NONE;

    /**
     * 用户放置操作。
     *
     * @serial
     */
    private int     dropAction       = DnDConstants.ACTION_NONE;

    /**
     * 与用户手势关联的输入设备修饰符的状态。
     *
     * @serial
     */
    private int     gestureModifiers = 0;

    /**
     * 表示 <code>gestureModifiers</code> 是否无效。
     *
     * @serial
     */
    private boolean invalidModifiers;

    /**
     * 通过旧的修饰符设置新的修饰符。
     * 鼠标修饰符的优先级高于叠加的键盘修饰符。
     */
    private void setNewModifiers() {
        if ((gestureModifiers & InputEvent.BUTTON1_MASK) != 0) {
            gestureModifiers |= InputEvent.BUTTON1_DOWN_MASK;
        }
        if ((gestureModifiers & InputEvent.BUTTON2_MASK) != 0) {
            gestureModifiers |= InputEvent.BUTTON2_DOWN_MASK;
        }
        if ((gestureModifiers & InputEvent.BUTTON3_MASK) != 0) {
            gestureModifiers |= InputEvent.BUTTON3_DOWN_MASK;
        }
        if ((gestureModifiers & InputEvent.SHIFT_MASK) != 0) {
            gestureModifiers |= InputEvent.SHIFT_DOWN_MASK;
        }
        if ((gestureModifiers & InputEvent.CTRL_MASK) != 0) {
            gestureModifiers |= InputEvent.CTRL_DOWN_MASK;
        }
        if ((gestureModifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
            gestureModifiers |= InputEvent.ALT_GRAPH_DOWN_MASK;
        }
    }

    /**
     * 通过新的修饰符设置旧的修饰符。
     */
    private void setOldModifiers() {
        if ((gestureModifiers & InputEvent.BUTTON1_DOWN_MASK) != 0) {
            gestureModifiers |= InputEvent.BUTTON1_MASK;
        }
        if ((gestureModifiers & InputEvent.BUTTON2_DOWN_MASK) != 0) {
            gestureModifiers |= InputEvent.BUTTON2_MASK;
        }
        if ((gestureModifiers & InputEvent.BUTTON3_DOWN_MASK) != 0) {
            gestureModifiers |= InputEvent.BUTTON3_MASK;
        }
        if ((gestureModifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            gestureModifiers |= InputEvent.SHIFT_MASK;
        }
        if ((gestureModifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
            gestureModifiers |= InputEvent.CTRL_MASK;
        }
        if ((gestureModifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
            gestureModifiers |= InputEvent.ALT_GRAPH_MASK;
        }
    }
}
