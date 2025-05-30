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

import java.awt.AWTEvent;
import java.awt.Event;
import java.lang.annotation.Native;

/**
 * 一个语义事件，表示组件定义的操作已发生。
 * 这个高级事件由组件（如 <code>Button</code>）在
 * 组件特定的操作发生时（如被按下）生成。
 * 该事件传递给使用组件的 <code>addActionListener</code> 方法
 * 注册以接收此类事件的每个 <code>ActionListener</code> 对象。
 * <p>
 * <b>注意：</b>要使用键盘调用 <code>ActionEvent</code>，请使用空格键。
 * <P>
 * 实现 <code>ActionListener</code> 接口的对象在事件发生时
 * 会收到此 <code>ActionEvent</code>。因此，监听器可以处理一个“有意义的”（语义的）
 * 事件，如“按钮按下”，而无需处理单个鼠标移动和鼠标点击的细节。
 * <p>
 * 如果任何特定 <code>ActionEvent</code> 实例的 {@code id} 参数
 * 不在 {@code ACTION_FIRST} 到 {@code ACTION_LAST} 的范围内，
 * 将导致未指定的行为。
 *
 * @see ActionListener
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/actionlistener.html">教程：如何编写一个 Action Listener</a>
 *
 * @author Carl Quinn
 * @since 1.1
 */
public class ActionEvent extends AWTEvent {

    /**
     * 移位修饰符。指示在事件期间按下了移位键。
     */
    public static final int SHIFT_MASK          = Event.SHIFT_MASK;

    /**
     * 控制修饰符。指示在事件期间按下了控制键。
     */
    public static final int CTRL_MASK           = Event.CTRL_MASK;

    /**
     * 元修饰符。指示在事件期间按下了元键。
     */
    public static final int META_MASK           = Event.META_MASK;

    /**
     * 替代修饰符。指示在事件期间按下了替代键。
     */
    public static final int ALT_MASK            = Event.ALT_MASK;


    /**
     * 用于动作事件的 id 范围的第一个数字。
     */
    public static final int ACTION_FIRST                = 1001;

    /**
     * 用于动作事件的 id 范围的最后一个数字。
     */
    public static final int ACTION_LAST                 = 1001;

    /**
     * 此事件 id 表示发生了有意义的动作。
     */
    @Native public static final int ACTION_PERFORMED    = ACTION_FIRST; //Event.ACTION_EVENT

    /**
     * 一个非本地化的字符串，提供有关实际导致事件的更多详细信息。
     * 此信息非常特定于触发它的组件。
     *
     * @serial
     * @see #getActionCommand
     */
    String actionCommand;

    /**
     * 此事件发生的时间戳。因为 ActionEvent 是一个高级的、语义事件，
     * 时间戳通常与底层 InputEvent 相同。
     *
     * @serial
     * @see #getWhen
     */
    long when;

    /**
     * 表示选定的键修饰符，用于确定选定键的状态。
     * 如果没有选择修饰符，它将默认为零。
     *
     * @serial
     * @see #getModifiers
     */
    int modifiers;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -7671078796273832149L;

    /**
     * 构造一个 <code>ActionEvent</code> 对象。
     * <p>
     * 如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     * <code>null</code> 的 <code>command</code> 字符串是合法的，
     * 但不推荐使用。
     *
     * @param source  事件的来源对象
     * @param id      一个标识事件的整数。
     *                     有关允许值的信息，请参阅 {@link ActionEvent} 的类描述
     * @param command 一个可能指定与事件关联的命令（可能是多个命令之一）的字符串
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getSource()
     * @see #getID()
     * @see #getActionCommand()
     */
    public ActionEvent(Object source, int id, String command) {
        this(source, id, command, 0);
    }

    /**
     * 构造一个带有修饰符键的 <code>ActionEvent</code> 对象。
     * <p>
     * 如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     * <code>null</code> 的 <code>command</code> 字符串是合法的，
     * 但不推荐使用。
     *
     * @param source  事件的来源对象
     * @param id      一个标识事件的整数。
     *                     有关允许值的信息，请参阅 {@link ActionEvent} 的类描述
     * @param command 一个可能指定与事件关联的命令（可能是多个命令之一）的字符串
     * @param modifiers 事件期间按下的修饰符键（移位、控制、替代、元）。
     *                  传递负参数不推荐。
     *                  零值表示没有传递修饰符
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getSource()
     * @see #getID()
     * @see #getActionCommand()
     * @see #getModifiers()
     */
    public ActionEvent(Object source, int id, String command, int modifiers) {
        this(source, id, command, 0, modifiers);
    }

    /**
     * 构造一个带有指定修饰符键和时间戳的 <code>ActionEvent</code> 对象。
     * <p>
     * 如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     * <code>null</code> 的 <code>command</code> 字符串是合法的，
     * 但不推荐使用。
     *
     * @param source  事件的来源对象
     * @param id      一个标识事件的整数。
     *                     有关允许值的信息，请参阅 {@link ActionEvent} 的类描述
     * @param command 一个可能指定与事件关联的命令（可能是多个命令之一）的字符串
     * @param modifiers 事件期间按下的修饰符键（移位、控制、替代、元）。
     *                  传递负参数不推荐。
     *                  零值表示没有传递修饰符
     * @param when   一个表示事件发生时间的长整数。
     *               传递负值或零值不推荐
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getSource()
     * @see #getID()
     * @see #getActionCommand()
     * @see #getModifiers()
     * @see #getWhen()
     *
     * @since 1.4
     */
    public ActionEvent(Object source, int id, String command, long when,
                       int modifiers) {
        super(source, id);
        this.actionCommand = command;
        this.when = when;
        this.modifiers = modifiers;
    }

    /**
     * 返回与此操作关联的命令字符串。
     * 此字符串允许“模态”组件根据其状态指定多个命令之一。
     * 例如，一个按钮可能在“显示详细信息”和“隐藏详细信息”之间切换。
     * 在每种情况下，源对象和事件都是相同的，但命令字符串将标识预期的操作。
     * <p>
     * 注意，如果在构造此 <code>ActionEvent</code> 时传递了 <code>null</code> 的命令字符串，
     * 则此方法返回 <code>null</code>。
     *
     * @return 识别此事件命令的字符串
     */
    public String getActionCommand() {
        return actionCommand;
    }

    /**
     * 返回此事件发生的时间戳。因为 ActionEvent 是一个高级的、语义事件，
     * 时间戳通常与底层 InputEvent 相同。
     *
     * @return 此事件的时间戳
     * @since 1.4
     */
    public long getWhen() {
        return when;
    }

    /**
     * 返回此操作事件期间按下的修饰符键。
     *
     * @return 修饰符常量的按位或
     */
    public int getModifiers() {
        return modifiers;
    }

    /**
     * 返回一个标识此操作事件的参数字符串。
     * 此方法对于事件记录和调试非常有用。
     *
     * @return 识别事件及其关联命令的字符串
     */
    public String paramString() {
        String typeStr;
        switch(id) {
          case ACTION_PERFORMED:
              typeStr = "ACTION_PERFORMED";
              break;
          default:
              typeStr = "unknown type";
        }
        return typeStr + ",cmd="+actionCommand+",when="+when+",modifiers="+
            KeyEvent.getKeyModifiersText(modifiers);
    }
}
