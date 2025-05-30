
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

import java.awt.Window;
import java.lang.annotation.Native;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

/**
 * 一个低级别的事件，表示窗口状态已更改。此低级别事件由 Window 对象生成，当窗口被打开、关闭、激活、非激活、最小化或恢复时，或者当焦点从窗口中移入或移出时。
 * <P>
 * 该事件传递给每个使用窗口的 <code>addWindowListener</code> 方法注册以接收此类事件的 <code>WindowListener</code>
 * 或 <code>WindowAdapter</code> 对象。（<code>WindowAdapter</code> 对象实现了
 * <code>WindowListener</code> 接口。）每个这样的监听器对象
 * 在事件发生时都会收到此 <code>WindowEvent</code>。
 * <p>
 * 如果任何特定的 <code>WindowEvent</code> 实例的 {@code id} 参数不在
 * {@code WINDOW_FIRST} 到 {@code WINDOW_LAST} 的范围内，则会导致未指定的行为。
 *
 * @author Carl Quinn
 * @author Amy Fowler
 *
 * @see WindowAdapter
 * @see WindowListener
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/windowlistener.html">教程：编写窗口监听器</a>
 *
 * @since JDK1.1
 */
public class WindowEvent extends ComponentEvent {

    /**
     * 用于窗口事件的 ID 范围的第一个数字。
     */
    public static final int WINDOW_FIRST        = 200;

    /**
     * 窗口打开事件。此事件仅在窗口第一次显示时传递。
     */
    @Native public static final int WINDOW_OPENED       = WINDOW_FIRST; // 200

    /**
     * “窗口正在关闭”事件。当用户尝试从窗口的系统菜单关闭窗口时传递此事件。
     * 如果程序在处理此事件时没有显式地隐藏或释放窗口，
     * 则窗口关闭操作将被取消。
     */
    @Native public static final int WINDOW_CLOSING      = 1 + WINDOW_FIRST; //Event.WINDOW_DESTROY

    /**
     * 窗口关闭事件。此事件在调用 dispose 关闭显示窗口后传递。
     * @see java.awt.Component#isDisplayable
     * @see Window#dispose
     */
    @Native public static final int WINDOW_CLOSED       = 2 + WINDOW_FIRST;

    /**
     * 窗口最小化事件。当窗口从正常状态变为最小化状态时传递此事件。
     * 对于许多平台，最小化窗口会显示为窗口的 iconImage 属性中指定的图标。
     * @see java.awt.Frame#setIconImage
     */
    @Native public static final int WINDOW_ICONIFIED    = 3 + WINDOW_FIRST; //Event.WINDOW_ICONIFY

    /**
     * 窗口恢复事件类型。当窗口从最小化状态变为正常状态时传递此事件。
     */
    @Native public static final int WINDOW_DEICONIFIED  = 4 + WINDOW_FIRST; //Event.WINDOW_DEICONIFY

    /**
     * 窗口激活事件类型。当窗口成为活动窗口时传递此事件。只有 Frame 或 Dialog 可以成为活动窗口。
     * 本机窗口系统可能会用特殊的装饰（如高亮的标题栏）表示活动窗口或其子组件。活动窗口总是焦点窗口，或焦点窗口的第一个 Frame 或 Dialog 所有者。
     */
    @Native public static final int WINDOW_ACTIVATED    = 5 + WINDOW_FIRST;

    /**
     * 窗口非激活事件类型。当窗口不再是活动窗口时传递此事件。只有 Frame 或 Dialog 可以成为活动窗口。
     * 本机窗口系统可能会用特殊的装饰（如高亮的标题栏）表示活动窗口或其子组件。活动窗口总是焦点窗口，或焦点窗口的第一个 Frame 或 Dialog 所有者。
     */
    @Native public static final int WINDOW_DEACTIVATED  = 6 + WINDOW_FIRST;

    /**
     * 窗口获得焦点事件类型。当窗口成为焦点窗口时传递此事件，这意味着窗口或其子组件将接收键盘事件。
     */
    @Native public static final int WINDOW_GAINED_FOCUS = 7 + WINDOW_FIRST;

    /**
     * 窗口失去焦点事件类型。当窗口不再是焦点窗口时传递此事件，这意味着键盘事件将不再传递给窗口或其任何子组件。
     */
    @Native public static final int WINDOW_LOST_FOCUS   = 8 + WINDOW_FIRST;

    /**
     * 窗口状态更改事件类型。当窗口因最小化、最大化等操作而更改状态时传递此事件。
     * @since 1.4
     */
    @Native public static final int WINDOW_STATE_CHANGED = 9 + WINDOW_FIRST;

    /**
     * 用于窗口事件的 ID 范围的最后一个数字。
     */
    public static final int WINDOW_LAST         = WINDOW_STATE_CHANGED;

    /**
     * 与此焦点或激活更改相关的其他窗口。对于 WINDOW_ACTIVATED 或 WINDOW_GAINED_FOCUS 事件，这是失去激活或焦点的窗口。
     * 对于 WINDOW_DEACTIVATED 或 WINDOW_LOST_FOCUS 事件，这是获得激活或焦点的窗口。对于任何其他类型的 WindowEvent，
     * 或者如果焦点或激活更改发生在本机应用程序、不同 VM 中的 Java 应用程序或没有其他窗口的情况下，返回 null。
     *
     * @see #getOppositeWindow
     * @since 1.4
     */
    transient Window opposite;

    /**
     * TBS
     */
    int oldState;
    int newState;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -1567959133147912127L;

    /**
     * 构造一个 <code>WindowEvent</code> 对象。
     * <p>如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source    事件的来源 <code>Window</code> 对象
     * @param id        表示事件类型的整数。有关允许值的信息，请参见 {@link WindowEvent} 的类描述
     * @param opposite  与此焦点或激活更改相关的其他窗口，或 <code>null</code>
     * @param oldState  窗口状态更改事件的窗口先前状态。有关允许值，请参见 {@code #getOldState()}
     * @param newState  窗口状态更改事件的窗口新状态。有关允许值，请参见 {@code #getNewState()}
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getWindow()
     * @see #getID()
     * @see #getOppositeWindow()
     * @see #getOldState()
     * @see #getNewState()
     * @since 1.4
     */
    public WindowEvent(Window source, int id, Window opposite,
                       int oldState, int newState)
    {
        super(source, id);
        this.opposite = opposite;
        this.oldState = oldState;
        this.newState = newState;
    }

    /**
     * 构造一个具有指定相反 <code>Window</code> 的 <code>WindowEvent</code> 对象。相反的
     * <code>Window</code> 是与此焦点或激活更改相关的其他 <code>Window</code>。
     * 对于 <code>WINDOW_ACTIVATED</code> 或 <code>WINDOW_GAINED_FOCUS</code> 事件，这是
     * 失去激活或焦点的 <code>Window</code>。对于 <code>WINDOW_DEACTIVATED</code> 或
     * <code>WINDOW_LOST_FOCUS</code> 事件，这是获得激活或焦点的 <code>Window</code>。
     * 如果此焦点更改发生在本机应用程序、不同 VM 中的 Java 应用程序或没有其他 <code>Window</code> 的情况下，则相反的窗口为 <code>null</code>。
     * <p>如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source     事件的来源 <code>Window</code> 对象
     * @param id        表示事件类型的整数。有关允许值的信息，请参见 {@link WindowEvent} 的类描述。
     *                  期望此构造函数仅用于
     *                  {@code WINDOW_ACTIVATED},{@code WINDOW_DEACTIVATED},
     *                  {@code WINDOW_GAINED_FOCUS}, 或 {@code WINDOW_LOST_FOCUS}
     *                  事件类型，因为其他事件类型的相反 <code>Window</code> 始终为 {@code null}。
     * @param opposite   与此焦点或激活更改相关的其他 <code>Window</code>，或 <code>null</code>
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getWindow()
     * @see #getID()
     * @see #getOppositeWindow()
     * @since 1.4
     */
    public WindowEvent(Window source, int id, Window opposite) {
        this(source, id, opposite, 0, 0);
    }

    /**
     * 构造一个具有指定先前和新窗口状态的 <code>WindowEvent</code> 对象。
     * <p>如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source    事件的来源 <code>Window</code> 对象
     * @param id        表示事件类型的整数。有关允许值的信息，请参见 {@link WindowEvent} 的类描述。
     *                  期望此构造函数仅用于
     *                  {@code WINDOW_STATE_CHANGED}
     *                  事件类型，因为其他事件类型的先前和新窗口状态没有意义。
     * @param oldState  表示窗口先前状态的整数。有关允许值，请参见 {@code #getOldState()}
     * @param newState  表示窗口新状态的整数。有关允许值，请参见 {@code #getNewState()}
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getWindow()
     * @see #getID()
     * @see #getOldState()
     * @see #getNewState()
     * @since 1.4
     */
    public WindowEvent(Window source, int id, int oldState, int newState) {
        this(source, id, null, oldState, newState);
    }

    /**
     * 构造一个 <code>WindowEvent</code> 对象。
     * <p>如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source 事件的来源 <code>Window</code> 对象
     * @param id     表示事件类型的整数。有关允许值的信息，请参见 {@link WindowEvent} 的类描述。
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getWindow()
     * @see #getID()
     */
    public WindowEvent(Window source, int id) {
        this(source, id, null, 0, 0);
    }

    /**
     * 返回事件的来源。
     *
     * @return 事件的来源 <code>Window</code> 对象
     */
    public Window getWindow() {
        return (source instanceof Window) ? (Window)source : null;
    }

    /**
     * 返回与此焦点或激活更改相关的其他窗口。对于 WINDOW_ACTIVATED 或 WINDOW_GAINED_FOCUS 事件，这是失去激活或焦点的窗口。
     * 对于 WINDOW_DEACTIVATED 或 WINDOW_LOST_FOCUS 事件，这是获得激活或焦点的窗口。对于任何其他类型的 WindowEvent，
     * 或者如果焦点或激活更改发生在本机应用程序、不同 VM 或上下文中的 Java 应用程序或没有其他窗口的情况下，返回 null。
     *
     * @return 与此焦点或激活更改相关的其他窗口，或 null
     * @since 1.4
     */
    public Window getOppositeWindow() {
        if (opposite == null) {
            return null;
        }

        return (SunToolkit.targetToAppContext(opposite) ==
                AppContext.getAppContext())
            ? opposite
            : null;
    }

    /**
     * 对于 <code>WINDOW_STATE_CHANGED</code> 事件，返回窗口的先前状态。状态表示为位掩码。
     * <ul>
     * <li><code>NORMAL</code>
     * <br>表示没有设置状态位。
     * <li><code>ICONIFIED</code>
     * <li><code>MAXIMIZED_HORIZ</code>
     * <li><code>MAXIMIZED_VERT</code>
     * <li><code>MAXIMIZED_BOTH</code>
     * <br>组合 <code>MAXIMIZED_HORIZ</code> 和 <code>MAXIMIZED_VERT</code>。
     * </ul>
     *
     * @return 窗口的先前状态位掩码
     * @see java.awt.Frame#getExtendedState()
     * @since 1.4
     */
    public int getOldState() {
        return oldState;
    }

    /**
     * 对于 <code>WINDOW_STATE_CHANGED</code> 事件，返回窗口的新状态。状态表示为位掩码。
     * <ul>
     * <li><code>NORMAL</code>
     * <br>表示没有设置状态位。
     * <li><code>ICONIFIED</code>
     * <li><code>MAXIMIZED_HORIZ</code>
     * <li><code>MAXIMIZED_VERT</code>
     * <li><code>MAXIMIZED_BOTH</code>
     * <br>组合 <code>MAXIMIZED_HORIZ</code> 和 <code>MAXIMIZED_VERT</code>。
     * </ul>
     *
     * @return 窗口的新状态位掩码
     * @see java.awt.Frame#getExtendedState()
     * @since 1.4
     */
    public int getNewState() {
        return newState;
    }


                /**
     * 返回一个标识此事件的参数字符串。
     * 此方法对于事件记录和调试非常有用。
     *
     * @return 一个标识事件及其属性的字符串
     */
    public String paramString() {
        String typeStr;
        switch(id) {
          case WINDOW_OPENED:
              typeStr = "WINDOW_OPENED";
              break;
          case WINDOW_CLOSING:
              typeStr = "WINDOW_CLOSING";
              break;
          case WINDOW_CLOSED:
              typeStr = "WINDOW_CLOSED";
              break;
          case WINDOW_ICONIFIED:
              typeStr = "WINDOW_ICONIFIED";
              break;
          case WINDOW_DEICONIFIED:
              typeStr = "WINDOW_DEICONIFIED";
              break;
          case WINDOW_ACTIVATED:
              typeStr = "WINDOW_ACTIVATED";
              break;
          case WINDOW_DEACTIVATED:
              typeStr = "WINDOW_DEACTIVATED";
              break;
          case WINDOW_GAINED_FOCUS:
              typeStr = "WINDOW_GAINED_FOCUS";
              break;
          case WINDOW_LOST_FOCUS:
              typeStr = "WINDOW_LOST_FOCUS";
              break;
          case WINDOW_STATE_CHANGED:
              typeStr = "WINDOW_STATE_CHANGED";
              break;
          default:
              typeStr = "未知类型";
        }
        typeStr += ",opposite=" + getOppositeWindow()
            + ",oldState=" + oldState + ",newState=" + newState;

        return typeStr;
    }
}
