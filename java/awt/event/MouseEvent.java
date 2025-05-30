
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

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.awt.IllegalComponentStateException;
import java.awt.MouseInfo;

import sun.awt.AWTAccessor;
import sun.awt.SunToolkit;

/**
 * 一个事件，表示在组件中发生了鼠标动作。
 * 当且仅当鼠标光标位于组件未被遮挡的部分的边界内时，才认为鼠标动作发生在特定的组件中。
 * 对于轻量级组件（如Swing的组件），只有在组件上启用了鼠标事件类型时，鼠标事件才会被分发到该组件。
 * 鼠标事件类型可以通过向组件添加适当的基于鼠标的{@code EventListener}（{@link MouseListener}或{@link MouseMotionListener}），
 * 或通过调用{@link Component#enableEvents(long)}并使用适当的掩码参数（{@code AWTEvent.MOUSE_EVENT_MASK}或
 * {@code AWTEvent.MOUSE_MOTION_EVENT_MASK}）来启用。如果组件上未启用鼠标事件类型，
 * 则相应的鼠标事件将被分发到第一个启用了鼠标事件类型的祖先组件。
 *<p>
 * 例如，如果向组件添加了{@code MouseListener}，或者调用了
 * {@code enableEvents(AWTEvent.MOUSE_EVENT_MASK)}，则所有由{@code MouseListener}定义的事件都将被分发到该组件。
 * 另一方面，如果未添加{@code MouseMotionListener}，且未使用
 * {@code AWTEvent.MOUSE_MOTION_EVENT_MASK}调用{@code enableEvents}，则不会将鼠标移动事件分发到该组件。
 * 相反，鼠标移动事件将被分发到第一个启用了鼠标移动事件的祖先组件。
 * <P>
 * 该低级事件由组件对象生成，用于以下情况：
 * <ul>
 * <li>鼠标事件
 *     <ul>
 *     <li>鼠标按钮被按下
 *     <li>鼠标按钮被释放
 *     <li>鼠标按钮被点击（按下和释放）
 *     <li>鼠标光标进入组件几何形状的未被遮挡部分
 *     <li>鼠标光标离开组件几何形状的未被遮挡部分
 *     </ul>
 * <li>鼠标移动事件
 *     <ul>
 *     <li>鼠标被移动
 *     <li>鼠标被拖动
 *     </ul>
 * </ul>
 * <P>
 * 一个<code>MouseEvent</code>对象将传递给每个通过组件的
 * <code>addMouseListener</code>方法注册以接收“感兴趣的”鼠标事件的
 * <code>MouseListener</code>或<code>MouseAdapter</code>对象。
 * （<code>MouseAdapter</code>对象实现了<code>MouseListener</code>接口。）
 * 每个这样的监听器对象都会收到一个包含鼠标事件的<code>MouseEvent</code>。
 * <P>
 * 一个<code>MouseEvent</code>对象还将传递给每个通过组件的
 * <code>addMouseMotionListener</code>方法注册以接收鼠标移动事件的
 * <code>MouseMotionListener</code>或<code>MouseMotionAdapter</code>对象。
 * （<code>MouseMotionAdapter</code>对象实现了<code>MouseMotionListener</code>接口。）
 * 每个这样的监听器对象都会收到一个包含鼠标移动事件的<code>MouseEvent</code>。
 * <P>
 * 当鼠标按钮被点击时，事件将生成并发送到已注册的<code>MouseListener</code>s。
 * 可以使用{@link InputEvent#getModifiers}和{@link InputEvent#getModifiersEx}检索模态键的状态。
 * 由{@link InputEvent#getModifiers}返回的按钮掩码仅反映状态改变的按钮，而不是所有按钮的当前状态。
 * （注意：由于ALT_MASK/BUTTON2_MASK和META_MASK/BUTTON3_MASK的值重叠，对于涉及修饰键的鼠标事件，这并不总是正确的）。
 * 要获取所有按钮和修饰键的状态，请使用{@link InputEvent#getModifiersEx}。
 * 由{@link MouseEvent#getButton}返回的按钮是状态改变的按钮。
 * <P>
 * 例如，如果第一个鼠标按钮被按下，事件将按以下顺序发送：
 * <PRE>
 *    <b   >id           </b   >   <b   >modifiers   </b   > <b   >button </b   >
 *    <code>MOUSE_PRESSED</code>:  <code>BUTTON1_MASK</code> <code>BUTTON1</code>
 *    <code>MOUSE_RELEASED</code>: <code>BUTTON1_MASK</code> <code>BUTTON1</code>
 *    <code>MOUSE_CLICKED</code>:  <code>BUTTON1_MASK</code> <code>BUTTON1</code>
 * </PRE>
 * 当多个鼠标按钮被按下时，每次按下、释放和点击都会生成单独的事件。
 * <P>
 * 例如，如果用户先按下<b>按钮1</b>，然后按下<b>按钮2</b>，再按相同的顺序释放它们，
 * 则将生成以下事件序列：
 * <PRE>
 *    <b   >id           </b   >   <b   >modifiers   </b   > <b   >button </b   >
 *    <code>MOUSE_PRESSED</code>:  <code>BUTTON1_MASK</code> <code>BUTTON1</code>
 *    <code>MOUSE_PRESSED</code>:  <code>BUTTON2_MASK</code> <code>BUTTON2</code>
 *    <code>MOUSE_RELEASED</code>: <code>BUTTON1_MASK</code> <code>BUTTON1</code>
 *    <code>MOUSE_CLICKED</code>:  <code>BUTTON1_MASK</code> <code>BUTTON1</code>
 *    <code>MOUSE_RELEASED</code>: <code>BUTTON2_MASK</code> <code>BUTTON2</code>
 *    <code>MOUSE_CLICKED</code>:  <code>BUTTON2_MASK</code> <code>BUTTON2</code>
 * </PRE>
 * 如果先释放<b>按钮2</b>，则先到达<code>BUTTON2_MASK</code>的
 * <code>MOUSE_RELEASED</code>/<code>MOUSE_CLICKED</code>对，
 * 然后到达<code>BUTTON1_MASK</code>的对。
 * <p>
 * 为了扩展由以下常量表示的标准按钮集：{@code BUTTON1}、{@code BUTTON2}和{@code BUTTON3}，
 * 添加了一些额外的鼠标按钮。
 * 额外的按钮没有分配的{@code BUTTONx}常量，也没有分配的按钮掩码常量。
 * 然而，可以从4开始的序号可以作为按钮编号（按钮ID）使用。
 * 由{@link InputEvent#getMaskForButton(int) getMaskForButton(button)}方法返回的值可以作为按钮掩码使用。
 * <p>
 * 当鼠标按钮被按下时，直到鼠标按钮被释放，<code>MOUSE_DRAGGED</code>事件将传递给该<code>Component</code>
 * （无论鼠标位置是否在<code>Component</code>的边界内）。由于平台依赖的拖放实现，
 * 在本地拖放操作期间可能不会传递<code>MOUSE_DRAGGED</code>事件。
 *
 * 在多屏环境中，即使鼠标位置超出与该<code>Component</code>关联的
 * <code>GraphicsConfiguration</code>的边界，鼠标拖动事件也会传递给该<code>Component</code>。
 * 然而，在这种情况下，报告的鼠标拖动事件的位置可能与实际鼠标位置不同：
 * <ul>
 * <li>在没有虚拟设备的多屏环境中：
 * <br>
 * 报告的鼠标拖动事件的坐标被裁剪以适应与<code>Component</code>关联的
 * <code>GraphicsConfiguration</code>的边界。
 * <li>在有虚拟设备的多屏环境中：
 * <br>
 * 报告的鼠标拖动事件的坐标被裁剪以适应与<code>Component</code>关联的虚拟设备的边界。
 * </ul>
 * <p>
 * 如果任何特定<code>MouseEvent</code>实例的<code>id</code>参数不在
 * {@code MOUSE_FIRST}到{@code MOUSE_LAST}-1的范围内（不包括{@code MOUSE_WHEEL}），
 * 则会导致未指定的行为。
 *
 * @author Carl Quinn
 *
 * @see MouseAdapter
 * @see MouseListener
 * @see MouseMotionAdapter
 * @see MouseMotionListener
 * @see MouseWheelListener
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/mouselistener.html">教程：编写鼠标监听器</a>
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/mousemotionlistener.html">教程：编写鼠标移动监听器</a>
 *
 * @since 1.1
 */
public class MouseEvent extends InputEvent {

    /**
     * 用于鼠标事件的ID范围的第一个数字。
     */
    public static final int MOUSE_FIRST         = 500;

    /**
     * 用于鼠标事件的ID范围的最后一个数字。
     */
    public static final int MOUSE_LAST          = 507;

    /**
     * “鼠标点击”事件。此<code>MouseEvent</code>
     * 发生在鼠标按钮被按下和释放时。
     */
    public static final int MOUSE_CLICKED = MOUSE_FIRST;

    /**
     * “鼠标按下”事件。此<code>MouseEvent</code>
     * 发生在鼠标按钮被按下时。
     */
    public static final int MOUSE_PRESSED = 1 + MOUSE_FIRST; //Event.MOUSE_DOWN

    /**
     * “鼠标释放”事件。此<code>MouseEvent</code>
     * 发生在鼠标按钮被释放时。
     */
    public static final int MOUSE_RELEASED = 2 + MOUSE_FIRST; //Event.MOUSE_UP

    /**
     * “鼠标移动”事件。此<code>MouseEvent</code>
     * 发生在鼠标位置改变时。
     */
    public static final int MOUSE_MOVED = 3 + MOUSE_FIRST; //Event.MOUSE_MOVE

    /**
     * “鼠标进入”事件。此<code>MouseEvent</code>
     * 发生在鼠标光标进入组件几何形状的未被遮挡部分时。
     */
    public static final int MOUSE_ENTERED = 4 + MOUSE_FIRST; //Event.MOUSE_ENTER

    /**
     * “鼠标离开”事件。此<code>MouseEvent</code>
     * 发生在鼠标光标离开组件几何形状的未被遮挡部分时。
     */
    public static final int MOUSE_EXITED = 5 + MOUSE_FIRST; //Event.MOUSE_EXIT

    /**
     * “鼠标拖动”事件。此<code>MouseEvent</code>
     * 发生在鼠标位置改变且鼠标按钮被按下时。
     */
    public static final int MOUSE_DRAGGED = 6 + MOUSE_FIRST; //Event.MOUSE_DRAG

    /**
     * “鼠标滚轮”事件。这是唯一的<code>MouseWheelEvent</code>。
     * 当配备滚轮的鼠标滚动其滚轮时，此事件发生。
     * @since 1.4
     */
    public static final int MOUSE_WHEEL = 7 + MOUSE_FIRST;

    /**
     * 表示没有鼠标按钮；由{@link #getButton}使用。
     * @since 1.4
     */
    public static final int NOBUTTON = 0;

    /**
     * 表示鼠标按钮#1；由{@link #getButton}使用。
     * @since 1.4
     */
    public static final int BUTTON1 = 1;

    /**
     * 表示鼠标按钮#2；由{@link #getButton}使用。
     * @since 1.4
     */
    public static final int BUTTON2 = 2;

    /**
     * 表示鼠标按钮#3；由{@link #getButton}使用。
     * @since 1.4
     */
    public static final int BUTTON3 = 3;

    /**
     * 鼠标事件的x坐标。
     * x值相对于触发事件的组件。
     *
     * @serial
     * @see #getX()
     */
    int x;

    /**
     * 鼠标事件的y坐标。
     * y值相对于触发事件的组件。
     *
     * @serial
     * @see #getY()
     */
    int y;

    /**
     * 鼠标事件的x绝对坐标。
     * 在虚拟设备多屏环境中，桌面区域可能跨越多个物理屏幕设备，
     * 此坐标相对于虚拟坐标系统。否则，此坐标相对于与组件的
     * GraphicsConfiguration关联的坐标系统。
     *
     * @serial
   */
    private int xAbs;

    /**
     * 鼠标事件的y绝对坐标。
     * 在虚拟设备多屏环境中，桌面区域可能跨越多个物理屏幕设备，
     * 此坐标相对于虚拟坐标系统。否则，此坐标相对于与组件的
     * GraphicsConfiguration关联的坐标系统。
     *
     * @serial
     */
    private int yAbs;

    /**
     * 表示鼠标按钮的快速连续点击次数。
     * clickCount仅对以下三个鼠标事件有效：<BR>
     * <code>MOUSE_CLICKED</code>，
     * <code>MOUSE_PRESSED</code>和
     * <code>MOUSE_RELEASED</code>。
     * 对于上述事件，<code>clickCount</code>至少为1。
     * 对于所有其他事件，计数为0。
     *
     * @serial
     * @see #getClickCount()
     */
    int clickCount;

    /**
     * 表示事件是否由触摸事件引起。
     */
    private boolean causedByTouchEvent;

    /**
     * 表示哪些（如果有）鼠标按钮的状态发生了改变。
     *
     * 有效值范围从0到由
     * {@link java.awt.MouseInfo#getNumberOfButtons() MouseInfo.getNumberOfButtons()}方法返回的值。
     * 该范围已经包括了常量{@code NOBUTTON}、{@code BUTTON1}、
     * {@code BUTTON2}和{@code BUTTON3}（如果这些按钮存在）。因此，也可以使用这些常量。
     * 例如，对于有两个按钮的鼠标，此字段可能包含以下值：
     * <ul>
     * <li> 0 ({@code NOBUTTON})
     * <li> 1 ({@code BUTTON1})
     * <li> 2 ({@code BUTTON2})
     * </ul>
     * 如果鼠标有5个按钮，此字段可能包含以下值：
     * <ul>
     * <li> 0 ({@code NOBUTTON})
     * <li> 1 ({@code BUTTON1})
     * <li> 2 ({@code BUTTON2})
     * <li> 3 ({@code BUTTON3})
     * <li> 4
     * <li> 5
     * </ul>
     * 如果Java禁用了对扩展鼠标按钮的支持，则此字段可能不包含大于{@code BUTTON3}的值。
     * @serial
     * @see #getButton()
     * @see java.awt.Toolkit#areExtraMouseButtonsEnabled()
     */
    int button;

    /**
     * 用于指示是否应通过某些手势显示弹出菜单的属性。
     * 如果<code>popupTrigger</code> = <code>false</code>，则不应显示弹出菜单。
     * 如果为<code>true</code>，则应显示弹出菜单。
     *
     * @serial
     * @see java.awt.PopupMenu
     * @see #isPopupTrigger()
     */
    boolean popupTrigger = false;


                /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -991214153494842848L;

    /**
     * 在 {@code Toolkit} 机制启动时可用的鼠标按钮数量。
     */
    private static int cachedNumberOfButtons;

    static {
        /* 确保加载必要的本机库 */
        NativeLibLoader.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
        final Toolkit tk = Toolkit.getDefaultToolkit();
        if (tk instanceof SunToolkit) {
            cachedNumberOfButtons = ((SunToolkit)tk).getNumberOfButtons();
        } else {
            // 预期某些工具包（无头模式，
            // 除了 SunToolkit 之外的其他工具包）也可以运行。
            cachedNumberOfButtons = 3;
        }
        AWTAccessor.setMouseEventAccessor(
            new AWTAccessor.MouseEventAccessor() {
                public boolean isCausedByTouchEvent(MouseEvent ev) {
                    return ev.causedByTouchEvent;
                }

                public void setCausedByTouchEvent(MouseEvent ev,
                    boolean causedByTouchEvent) {
                    ev.causedByTouchEvent = causedByTouchEvent;
                }
            });
    }

    /**
     * 初始化可能从 C 语言访问的字段的 JNI 字段和方法 ID。
     */
    private static native void initIDs();

    /**
     * 返回事件的绝对 x, y 位置。
     * 在虚拟设备多屏幕环境中，桌面区域可能跨越多个物理屏幕设备，
     * 这些坐标相对于虚拟坐标系统。
     * 否则，这些坐标相对于与组件的 GraphicsConfiguration 关联的坐标系统。
     *
     * @return 一个包含绝对 x 和 y 坐标的 <code>Point</code> 对象。
     *
     * @see java.awt.GraphicsConfiguration
     * @since 1.6
     */
    public Point getLocationOnScreen(){
      return new Point(xAbs, yAbs);
    }

    /**
     * 返回事件的绝对水平 x 位置。
     * 在虚拟设备多屏幕环境中，桌面区域可能跨越多个物理屏幕设备，
     * 此坐标相对于虚拟坐标系统。
     * 否则，此坐标相对于与组件的 GraphicsConfiguration 关联的坐标系统。
     *
     * @return x 一个表示绝对水平位置的整数。
     *
     * @see java.awt.GraphicsConfiguration
     * @since 1.6
     */
    public int getXOnScreen() {
        return xAbs;
    }

    /**
     * 返回事件的绝对垂直 y 位置。
     * 在虚拟设备多屏幕环境中，桌面区域可能跨越多个物理屏幕设备，
     * 此坐标相对于虚拟坐标系统。
     * 否则，此坐标相对于与组件的 GraphicsConfiguration 关联的坐标系统。
     *
     * @return y 一个表示绝对垂直位置的整数。
     *
     * @see java.awt.GraphicsConfiguration
     * @since 1.6
     */
    public int getYOnScreen() {
        return yAbs;
    }

    /**
     * 构造一个 <code>MouseEvent</code> 对象，指定源组件、
     * 类型、时间、修饰符、坐标、点击次数、弹出触发器标志和按钮编号。
     * <p>
     * 创建一个无效事件（例如使用多个旧的 _MASK，或不匹配的修饰符/按钮值）会导致未指定的行为。
     * 调用形式为
     * <tt>MouseEvent(source, id, when, modifiers, x, y, clickCount, popupTrigger, button)</tt>
     * 的方法与调用
     * <tt> {@link #MouseEvent(Component, int, long, int, int, int,
     * int, int, int, boolean, int) MouseEvent}(source, id, when, modifiers,
     * x, y, xAbs, yAbs, clickCount, popupTrigger, button)</tt>
     * 的方法行为完全相同，其中 xAbs 和 yAbs 定义为源在屏幕上的位置加上相对坐标 x 和 y。
     * 如果源未显示，则 xAbs 和 yAbs 设置为零。
     * 如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source       事件的源 <code>Component</code>
     * @param id              表示事件类型的整数。
     *                     有关允许值的信息，请参见 {@link MouseEvent} 的类描述
     * @param when         表示事件发生时间的长整数。
     *                     传递负值或零值不推荐
     * @param modifiers    描述事件期间按下的修饰键和鼠标按钮（例如，shift、ctrl、alt 和 meta）的修饰符掩码。
     *                     仅允许使用扩展修饰符作为此参数的值（请参见 {@link InputEvent#getModifiersEx}
     *                     类对扩展修饰符的描述）。
     *                     传递负值不推荐。
     *                     零值表示未传递任何修饰符
     * @param x            鼠标位置的水平 x 坐标。
     *                       允许传递负值
     * @param y            鼠标位置的垂直 y 坐标。
     *                       允许传递负值
     * @param clickCount   与事件关联的鼠标点击次数。
     *                       传递负值不推荐
     * @param popupTrigger 一个布尔值，如果此事件是弹出菜单的触发器，则为 {@code true}
     * @param button       表示哪个鼠标按钮的状态发生了变化的整数。
     * 以下规则适用于此参数：
     * <ul>
     * <li>如果 Java 禁用了对扩展鼠标按钮的支持
     * 则只允许使用标准按钮创建 {@code MouseEvent} 对象：
     * {@code NOBUTTON}, {@code BUTTON1}, {@code BUTTON2}, 和
     * {@code BUTTON3}.
     * <li> 如果 Java 启用了对扩展鼠标按钮的支持
     * 则允许使用标准按钮创建 {@code MouseEvent} 对象。
     * 如果 Java 启用了对扩展鼠标按钮的支持，则除了标准按钮外，
     * 还可以使用从 4 开始到
     * {@link java.awt.MouseInfo#getNumberOfButtons() MouseInfo.getNumberOfButtons()}
     * 的范围内的按钮创建 {@code MouseEvent} 对象，如果鼠标有超过三个按钮的话。
     * </ul>
     * @throws IllegalArgumentException 如果 {@code button} 小于零
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @throws IllegalArgumentException 如果 {@code button} 大于 BUTTON3 且 Java 禁用了对扩展鼠标按钮的支持
     * @throws IllegalArgumentException 如果 {@code button} 大于
     *                                  {@link java.awt.MouseInfo#getNumberOfButtons() 当前按钮数} 且 Java 启用了对扩展鼠标按钮的支持
     * @throws IllegalArgumentException 如果传递了无效的 <code>button</code>
     *            值
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getSource()
     * @see #getID()
     * @see #getWhen()
     * @see #getModifiers()
     * @see #getX()
     * @see #getY()
     * @see #getClickCount()
     * @see #isPopupTrigger()
     * @see #getButton()
     * @since 1.4
     */
    public MouseEvent(Component source, int id, long when, int modifiers,
                      int x, int y, int clickCount, boolean popupTrigger,
                      int button)
    {
        this(source, id, when, modifiers, x, y, 0, 0, clickCount, popupTrigger, button);
        Point eventLocationOnScreen = new Point(0, 0);
        try {
          eventLocationOnScreen = source.getLocationOnScreen();
          this.xAbs = eventLocationOnScreen.x + x;
          this.yAbs = eventLocationOnScreen.y + y;
        } catch (IllegalComponentStateException e){
          this.xAbs = 0;
          this.yAbs = 0;
        }
    }

    /**
     * 构造一个 <code>MouseEvent</code> 对象，指定源组件、
     * 类型、修饰符、坐标、点击次数和弹出触发器标志。
     * 调用形式为
     * <tt>MouseEvent(source, id, when, modifiers, x, y, clickCount, popupTrigger)</tt>
     * 的方法与调用
     * <tt> {@link #MouseEvent(Component, int, long, int, int, int,
     * int, int, int, boolean, int) MouseEvent}(source, id, when, modifiers,
     * x, y, xAbs, yAbs, clickCount, popupTrigger, MouseEvent.NOBUTTON)</tt>
     * 的方法行为完全相同，其中 xAbs 和 yAbs 定义为源在屏幕上的位置加上相对坐标 x 和 y。
     * 如果源未显示，则 xAbs 和 yAbs 设置为零。
     * 如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source       事件的源 <code>Component</code>
     * @param id              表示事件类型的整数。
     *                     有关允许值的信息，请参见 {@link MouseEvent} 的类描述
     * @param when         表示事件发生时间的长整数。
     *                     传递负值或零值不推荐
     * @param modifiers    描述事件期间按下的修饰键和鼠标按钮（例如，shift、ctrl、alt 和 meta）的修饰符掩码。
     *                     仅允许使用扩展修饰符作为此参数的值（请参见 {@link InputEvent#getModifiersEx}
     *                     类对扩展修饰符的描述）。
     *                     传递负值不推荐。
     *                     零值表示未传递任何修饰符
     * @param x            鼠标位置的水平 x 坐标。
     *                       允许传递负值
     * @param y            鼠标位置的垂直 y 坐标。
     *                       允许传递负值
     * @param clickCount   与事件关联的鼠标点击次数。
     *                       传递负值不推荐
     * @param popupTrigger 一个布尔值，如果此事件是弹出菜单的触发器，则为 {@code true}
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getSource()
     * @see #getID()
     * @see #getWhen()
     * @see #getModifiers()
     * @see #getX()
     * @see #getY()
     * @see #getClickCount()
     * @see #isPopupTrigger()
     */
     public MouseEvent(Component source, int id, long when, int modifiers,
                      int x, int y, int clickCount, boolean popupTrigger) {
        this(source, id, when, modifiers, x, y, clickCount, popupTrigger, NOBUTTON);
     }


    /* 如果按钮是额外按钮且被释放或点击，则在 X 系统中其状态不会被修改。从 ExtModifiers 掩码中排除此按钮编号。*/
    transient private boolean shouldExcludeButtonFromExtModifiers = false;

    /**
     * {@inheritDoc}
     */
    public int getModifiersEx() {
        int tmpModifiers = modifiers;
        if (shouldExcludeButtonFromExtModifiers) {
            tmpModifiers &= ~(InputEvent.getMaskForButton(getButton()));
        }
        return tmpModifiers & ~JDK_1_3_MODIFIERS;
    }

    /**
     * 构造一个 <code>MouseEvent</code> 对象，指定源组件、
     * 类型、时间、修饰符、坐标、绝对坐标、点击次数、弹出触发器标志和按钮编号。
     * <p>
     * 创建一个无效事件（例如使用多个旧的 _MASK，或不匹配的修饰符/按钮值）会导致未指定的行为。
     * 即使传递给构造函数的相对坐标和绝对坐标不一致，鼠标事件实例仍然会被创建且不会抛出异常。
     * 如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source       事件的源 <code>Component</code>
     * @param id              表示事件类型的整数。
     *                     有关允许值的信息，请参见 {@link MouseEvent} 的类描述
     * @param when         表示事件发生时间的长整数。
     *                     传递负值或零值不推荐
     * @param modifiers    描述事件期间按下的修饰键和鼠标按钮（例如，shift、ctrl、alt 和 meta）的修饰符掩码。
     *                     仅允许使用扩展修饰符作为此参数的值（请参见 {@link InputEvent#getModifiersEx}
     *                     类对扩展修饰符的描述）。
     *                     传递负值不推荐。
     *                     零值表示未传递任何修饰符
     * @param x            鼠标位置的水平 x 坐标。
     *                       允许传递负值
     * @param y            鼠标位置的垂直 y 坐标。
     *                       允许传递负值
     * @param xAbs           鼠标位置的绝对水平 x 坐标
     *                       允许传递负值
     * @param yAbs           鼠标位置的绝对垂直 y 坐标
     *                       允许传递负值
     * @param clickCount   与事件关联的鼠标点击次数。
     *                       传递负值不推荐
     * @param popupTrigger 一个布尔值，如果此事件是弹出菜单的触发器，则为 {@code true}
     * @param button       表示哪个鼠标按钮的状态发生了变化的整数。
     * 以下规则适用于此参数：
     * <ul>
     * <li>如果 Java 禁用了对扩展鼠标按钮的支持
     * 则只允许使用标准按钮创建 {@code MouseEvent} 对象：
     * {@code NOBUTTON}, {@code BUTTON1}, {@code BUTTON2}, 和
     * {@code BUTTON3}.
     * <li> 如果 Java 启用了对扩展鼠标按钮的支持
     * 则允许使用标准按钮创建 {@code MouseEvent} 对象。
     * 如果 Java 启用了对扩展鼠标按钮的支持，则除了标准按钮外，
     * 还可以使用从 4 开始到
     * {@link java.awt.MouseInfo#getNumberOfButtons() MouseInfo.getNumberOfButtons()}
     * 的范围内的按钮创建 {@code MouseEvent} 对象，如果鼠标有超过三个按钮的话。
     * </ul>
     * @throws IllegalArgumentException 如果 {@code button} 小于零
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @throws IllegalArgumentException 如果 {@code button} 大于 BUTTON3 且 Java 禁用了对扩展鼠标按钮的支持
     * @throws IllegalArgumentException 如果 {@code button} 大于
     *                                  {@link java.awt.MouseInfo#getNumberOfButtons() 当前按钮数} 且 Java 启用了对扩展鼠标按钮的支持
     * @throws IllegalArgumentException 如果传递了无效的 <code>button</code>
     *            值
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getSource()
     * @see #getID()
     * @see #getWhen()
     * @see #getModifiers()
     * @see #getX()
     * @see #getY()
     * @see #getXOnScreen()
     * @see #getYOnScreen()
     * @see #getClickCount()
     * @see #isPopupTrigger()
     * @see #getButton()
     * @see #button
     * @see Toolkit#areExtraMouseButtonsEnabled()
     * @see java.awt.MouseInfo#getNumberOfButtons()
     * @see InputEvent#getMaskForButton(int)
     * @since 1.6
     */
    public MouseEvent(Component source, int id, long when, int modifiers,
                      int x, int y, int xAbs, int yAbs,
                      int clickCount, boolean popupTrigger, int button)
    {
        super(source, id, when, modifiers);
        this.x = x;
        this.y = y;
        this.xAbs = xAbs;
        this.yAbs = yAbs;
        this.clickCount = clickCount;
        this.popupTrigger = popupTrigger;
        if (button < NOBUTTON){
            throw new IllegalArgumentException("Invalid button value :" + button);
        }
        if (button > BUTTON3) {
            if (!Toolkit.getDefaultToolkit().areExtraMouseButtonsEnabled()){
                throw new IllegalArgumentException("Extra mouse events are disabled " + button);
            } else {
                if (button > cachedNumberOfButtons) {
                    throw new IllegalArgumentException("Nonexistent button " + button);
                }
            }
            // XToolkit: 额外按钮不会正确报告其状态。
            // 被按下时，它们在按下和释放时都会报告状态=0。
            // 对于 1-3 按钮，按下时状态值为零，释放时状态值非零。
            // 其他修饰符如 Shift, ALT 等与额外按钮一起报告良好。
            // 问题表现为：一个按钮被按下，然后另一个按钮被按下并释放。
            // 因此，getModifiersEx() 由于第一个按钮不会为零，我们将跳过这个修饰符。
            // 如果可能，这可能需要移动到对等代码中。
        }
    }


                        if (getModifiersEx() != 0) { // 至少有一个按钮处于按下状态。
                if (id == MouseEvent.MOUSE_RELEASED || id == MouseEvent.MOUSE_CLICKED){
                    shouldExcludeButtonFromExtModifiers = true;
                }
            }
        }

        this.button = button;

        if ((getModifiers() != 0) && (getModifiersEx() == 0)) {
            setNewModifiers();
        } else if ((getModifiers() == 0) &&
                   (getModifiersEx() != 0 || button != NOBUTTON) &&
                   (button <= BUTTON3))
        {
            setOldModifiers();
        }
    }

    /**
     * 返回事件相对于源组件的水平 x 位置。
     *
     * @return x  表示相对于组件的水平位置的整数
     */
    public int getX() {
        return x;
    }

    /**
     * 返回事件相对于源组件的垂直 y 位置。
     *
     * @return y  表示相对于组件的垂直位置的整数
     */
    public int getY() {
        return y;
    }

    /**
     * 返回事件相对于源组件的 x, y 位置。
     *
     * @return 一个包含相对于源组件的 x 和 y 坐标的 <code>Point</code> 对象
     *
     */
    public Point getPoint() {
        int x;
        int y;
        synchronized (this) {
            x = this.x;
            y = this.y;
        }
        return new Point(x, y);
    }

    /**
     * 通过添加指定的 <code>x</code>（水平）和 <code>y</code>
     * （垂直）偏移量来翻译事件的坐标。
     *
     * @param x 要添加到当前 x 坐标位置的水平 x 值
     * @param y 要添加到当前 y 坐标位置的垂直 y 值
     */
    public synchronized void translatePoint(int x, int y) {
        this.x += x;
        this.y += y;
    }

    /**
     * 返回与此事件关联的鼠标点击次数。
     *
     * @return 点击次数的整数值
     */
    public int getClickCount() {
        return clickCount;
    }

    /**
     * 返回任何已更改状态的鼠标按钮。
     * 返回的值范围从 0 到 {@link java.awt.MouseInfo#getNumberOfButtons() MouseInfo.getNumberOfButtons()}
     * 值。返回的值至少包括以下常量：
     * <ul>
     * <li> {@code NOBUTTON}
     * <li> {@code BUTTON1}
     * <li> {@code BUTTON2}
     * <li> {@code BUTTON3}
     * </ul>
     * 在应用程序中可以使用这些常量与返回的按钮编号进行比较。
     * 例如，
     * <pre>
     * if (anEvent.getButton() == MouseEvent.BUTTON1) {
     * </pre>
     * 特别是，对于具有一个、两个或三个按钮的鼠标，此方法可能返回以下值：
     * <ul>
     * <li> 0 ({@code NOBUTTON})
     * <li> 1 ({@code BUTTON1})
     * <li> 2 ({@code BUTTON2})
     * <li> 3 ({@code BUTTON3})
     * </ul>
     * 如果安装了具有五个按钮的鼠标，此方法可能返回以下值：
     * <ul>
     * <li> 0 ({@code NOBUTTON})
     * <li> 1 ({@code BUTTON1})
     * <li> 2 ({@code BUTTON2})
     * <li> 3 ({@code BUTTON3})
     * <li> 4
     * <li> 5
     * </ul>
     * <p>
     * 注意：如果 Java 禁用了对扩展鼠标按钮的支持，则 AWT 事件子系统不会生成扩展鼠标按钮的鼠标事件。
     * 因此，不期望此方法返回除 {@code NOBUTTON}、{@code BUTTON1}、{@code BUTTON2}、{@code BUTTON3} 之外的任何值。
     *
     * @return 如果 Java 启用了对扩展鼠标按钮的支持，则返回从 0 到 {@link java.awt.MouseInfo#getNumberOfButtons() MouseInfo.getNumberOfButtons()}
     *         的值。该范围包括 {@code NOBUTTON}、{@code BUTTON1}、{@code BUTTON2}、{@code BUTTON3}；
     *         <br>
     *         如果 Java 禁用了对扩展鼠标按钮的支持，则返回 {@code NOBUTTON}、{@code BUTTON1}、{@code BUTTON2} 或 {@code BUTTON3}
     * @since 1.4
     * @see Toolkit#areExtraMouseButtonsEnabled()
     * @see java.awt.MouseInfo#getNumberOfButtons()
     * @see #MouseEvent(Component, int, long, int, int, int, int, int, int, boolean, int)
     * @see InputEvent#getMaskForButton(int)
     */
    public int getButton() {
        return button;
    }

    /**
     * 返回此鼠标事件是否是平台的弹出菜单触发事件。
     * <p><b>注意</b>: 弹出菜单在不同的系统上触发方式不同。
     * 因此，应在 <code>mousePressed</code> 和 <code>mouseReleased</code>
     * 中检查 <code>isPopupTrigger</code> 以确保跨平台功能。
     *
     * @return 布尔值，如果此事件是平台的弹出菜单触发事件，则为 true
     */
    public boolean isPopupTrigger() {
        return popupTrigger;
    }

    /**
     * 返回一个描述事件期间按下修饰键和鼠标按钮的 <code>String</code> 实例，例如 "Shift"，
     * 或 "Ctrl+Shift"。这些字符串可以通过更改 <code>awt.properties</code> 文件进行本地化。
     * <p>
     * 注意，<code>InputEvent.ALT_MASK</code> 和
     * <code>InputEvent.BUTTON2_MASK</code> 的值相等，
     * 因此对于这两个修饰符都会返回 "Alt" 字符串。同样，
     * <code>InputEvent.META_MASK</code> 和
     * <code>InputEvent.BUTTON3_MASK</code> 的值相等，
     * 因此对于这两个修饰符都会返回 "Meta" 字符串。
     * <p>
     * 注意，传递负参数是不正确的，
     * 将导致返回一个未指定的字符串。零参数表示没有传递修饰符，将
     * 导致返回一个空字符串。
     * <p>
     * @param modifiers 一个描述事件期间按下修饰键和鼠标按钮的修饰符掩码
     * @return string   事件期间按下修饰键和鼠标按钮组合的字符串描述
     * @see InputEvent#getModifiersExText(int)
     * @since 1.4
     */
    public static String getMouseModifiersText(int modifiers) {
        StringBuilder buf = new StringBuilder();
        if ((modifiers & InputEvent.ALT_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.alt", "Alt"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.META_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.meta", "Meta"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.CTRL_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.control", "Ctrl"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.shift", "Shift"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.altGraph", "Alt Graph"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.button1", "Button1"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.BUTTON2_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.button2", "Button2"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.BUTTON3_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.button3", "Button3"));
            buf.append("+");
        }

        int mask;

        // TODO: 添加一个工具包字段，用于保存鼠标上的按钮数量。
        // 由于 getMouseModifiersText() 方法是静态的，并且接受一个整数参数，
        // 因此我们不能用鼠标上安装的按钮数量来限制它。
        // 这是一个临时解决方案。我们需要在其他地方保存按钮数量。
        for (int i = 1; i <= cachedNumberOfButtons; i++){
            mask = InputEvent.getMaskForButton(i);
            if ((modifiers & mask) != 0 &&
                buf.indexOf(Toolkit.getProperty("AWT.button"+i, "Button"+i)) == -1) //1,2,3 按钮可能已经存在；因此不要重复。
            {
                buf.append(Toolkit.getProperty("AWT.button"+i, "Button"+i));
                buf.append("+");
            }
        }

        if (buf.length() > 0) {
            buf.setLength(buf.length()-1); // 删除尾部的 '+'
        }
        return buf.toString();
    }

    /**
     * 返回一个标识此事件的参数字符串。
     * 此方法对于事件日志记录和调试非常有用。
     *
     * @return 一个标识事件及其属性的字符串
     */
    public String paramString() {
        StringBuilder str = new StringBuilder(80);

        switch(id) {
          case MOUSE_PRESSED:
              str.append("MOUSE_PRESSED");
              break;
          case MOUSE_RELEASED:
              str.append("MOUSE_RELEASED");
              break;
          case MOUSE_CLICKED:
              str.append("MOUSE_CLICKED");
              break;
          case MOUSE_ENTERED:
              str.append("MOUSE_ENTERED");
              break;
          case MOUSE_EXITED:
              str.append("MOUSE_EXITED");
              break;
          case MOUSE_MOVED:
              str.append("MOUSE_MOVED");
              break;
          case MOUSE_DRAGGED:
              str.append("MOUSE_DRAGGED");
              break;
          case MOUSE_WHEEL:
              str.append("MOUSE_WHEEL");
              break;
           default:
              str.append("unknown type");
        }

        // (x,y) 坐标
        str.append(",(").append(x).append(",").append(y).append(")");
        str.append(",absolute(").append(xAbs).append(",").append(yAbs).append(")");

        if (id != MOUSE_DRAGGED && id != MOUSE_MOVED){
            str.append(",button=").append(getButton());
        }

        if (getModifiers() != 0) {
            str.append(",modifiers=").append(getMouseModifiersText(modifiers));
        }

        if (getModifiersEx() != 0) {
            // 使用 "modifiers" 会显示字符串事件表示中的扩展按钮。
            // getModifiersEx() 解决了这个问题。
            str.append(",extModifiers=").append(getModifiersExText(getModifiersEx()));
        }

        str.append(",clickCount=").append(clickCount);

        return str.toString();
    }

    /**
     * 通过旧的修饰符设置新的修饰符。
     * 也设置按钮。
     */
    private void setNewModifiers() {
        if ((modifiers & BUTTON1_MASK) != 0) {
            modifiers |= BUTTON1_DOWN_MASK;
        }
        if ((modifiers & BUTTON2_MASK) != 0) {
            modifiers |= BUTTON2_DOWN_MASK;
        }
        if ((modifiers & BUTTON3_MASK) != 0) {
            modifiers |= BUTTON3_DOWN_MASK;
        }
        if (id == MOUSE_PRESSED
            || id == MOUSE_RELEASED
            || id == MOUSE_CLICKED)
        {
            if ((modifiers & BUTTON1_MASK) != 0) {
                button = BUTTON1;
                modifiers &= ~BUTTON2_MASK & ~BUTTON3_MASK;
                if (id != MOUSE_PRESSED) {
                    modifiers &= ~BUTTON1_DOWN_MASK;
                }
            } else if ((modifiers & BUTTON2_MASK) != 0) {
                button = BUTTON2;
                modifiers &= ~BUTTON1_MASK & ~BUTTON3_MASK;
                if (id != MOUSE_PRESSED) {
                    modifiers &= ~BUTTON2_DOWN_MASK;
                }
            } else if ((modifiers & BUTTON3_MASK) != 0) {
                button = BUTTON3;
                modifiers &= ~BUTTON1_MASK & ~BUTTON2_MASK;
                if (id != MOUSE_PRESSED) {
                    modifiers &= ~BUTTON3_DOWN_MASK;
                }
            }
        }
        if ((modifiers & InputEvent.ALT_MASK) != 0) {
            modifiers |= InputEvent.ALT_DOWN_MASK;
        }
        if ((modifiers & InputEvent.META_MASK) != 0) {
            modifiers |= InputEvent.META_DOWN_MASK;
        }
        if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
            modifiers |= InputEvent.SHIFT_DOWN_MASK;
        }
        if ((modifiers & InputEvent.CTRL_MASK) != 0) {
            modifiers |= InputEvent.CTRL_DOWN_MASK;
        }
        if ((modifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
            modifiers |= InputEvent.ALT_GRAPH_DOWN_MASK;
        }
    }

    /**
     * 通过新的修饰符设置旧的修饰符。
     */
    private void setOldModifiers() {
        if (id == MOUSE_PRESSED
            || id == MOUSE_RELEASED
            || id == MOUSE_CLICKED)
        {
            switch(button) {
            case BUTTON1:
                modifiers |= BUTTON1_MASK;
                break;
            case BUTTON2:
                modifiers |= BUTTON2_MASK;
                break;
            case BUTTON3:
                modifiers |= BUTTON3_MASK;
                break;
            }
        } else {
            if ((modifiers & BUTTON1_DOWN_MASK) != 0) {
                modifiers |= BUTTON1_MASK;
            }
            if ((modifiers & BUTTON2_DOWN_MASK) != 0) {
                modifiers |= BUTTON2_MASK;
            }
            if ((modifiers & BUTTON3_DOWN_MASK) != 0) {
                modifiers |= BUTTON3_MASK;
            }
        }
        if ((modifiers & ALT_DOWN_MASK) != 0) {
            modifiers |= ALT_MASK;
        }
        if ((modifiers & META_DOWN_MASK) != 0) {
            modifiers |= META_MASK;
        }
        if ((modifiers & SHIFT_DOWN_MASK) != 0) {
            modifiers |= SHIFT_MASK;
        }
        if ((modifiers & CTRL_DOWN_MASK) != 0) {
            modifiers |= CTRL_MASK;
        }
        if ((modifiers & ALT_GRAPH_DOWN_MASK) != 0) {
            modifiers |= ALT_GRAPH_MASK;
        }
    }

    /**
     * 通过旧的修饰符设置新的修饰符。
     * @serial
     */
    private void readObject(ObjectInputStream s)
      throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (getModifiers() != 0 && getModifiersEx() == 0) {
            setNewModifiers();
        }
    }
}
