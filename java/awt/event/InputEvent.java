
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

import java.awt.Event;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.Arrays;

import sun.awt.AWTAccessor;
import sun.util.logging.PlatformLogger;
import sun.security.util.SecurityConstants;

/**
 * 所有组件级输入事件的根事件类。
 *
 * 输入事件在源组件正常处理之前会先传递给监听器。
 * 这允许监听器和组件子类“消费”事件，以便源组件不会以默认方式处理这些事件。
 * 例如，消费 Button 组件上的 mousePressed 事件将防止 Button 被激活。
 *
 * @author Carl Quinn
 *
 * @see KeyEvent
 * @see KeyAdapter
 * @see MouseEvent
 * @see MouseAdapter
 * @see MouseMotionAdapter
 *
 * @since 1.1
 */
public abstract class InputEvent extends ComponentEvent {

    private static final PlatformLogger logger = PlatformLogger.getLogger("java.awt.event.InputEvent");

    /**
     * Shift 键修饰符常量。
     * 建议使用 SHIFT_DOWN_MASK 代替。
     */
    public static final int SHIFT_MASK = Event.SHIFT_MASK;

    /**
     * Control 键修饰符常量。
     * 建议使用 CTRL_DOWN_MASK 代替。
     */
    public static final int CTRL_MASK = Event.CTRL_MASK;

    /**
     * Meta 键修饰符常量。
     * 建议使用 META_DOWN_MASK 代替。
     */
    public static final int META_MASK = Event.META_MASK;

    /**
     * Alt 键修饰符常量。
     * 建议使用 ALT_DOWN_MASK 代替。
     */
    public static final int ALT_MASK = Event.ALT_MASK;

    /**
     * AltGraph 键修饰符常量。
     */
    public static final int ALT_GRAPH_MASK = 1 << 5;

    /**
     * 鼠标按钮1的修饰符常量。
     * 建议使用 BUTTON1_DOWN_MASK 代替。
     */
    public static final int BUTTON1_MASK = 1 << 4;

    /**
     * 鼠标按钮2的修饰符常量。
     * 建议使用 BUTTON2_DOWN_MASK 代替。
     * 注意 BUTTON2_MASK 的值与 ALT_MASK 相同。
     */
    public static final int BUTTON2_MASK = Event.ALT_MASK;

    /**
     * 鼠标按钮3的修饰符常量。
     * 建议使用 BUTTON3_DOWN_MASK 代替。
     * 注意 BUTTON3_MASK 的值与 META_MASK 相同。
     */
    public static final int BUTTON3_MASK = Event.META_MASK;

    /**
     * Shift 键扩展修饰符常量。
     * @since 1.4
     */
    public static final int SHIFT_DOWN_MASK = 1 << 6;

    /**
     * Control 键扩展修饰符常量。
     * @since 1.4
     */
    public static final int CTRL_DOWN_MASK = 1 << 7;

    /**
     * Meta 键扩展修饰符常量。
     * @since 1.4
     */
    public static final int META_DOWN_MASK = 1 << 8;

    /**
     * Alt 键扩展修饰符常量。
     * @since 1.4
     */
    public static final int ALT_DOWN_MASK = 1 << 9;

    /**
     * 鼠标按钮1的扩展修饰符常量。
     * @since 1.4
     */
    public static final int BUTTON1_DOWN_MASK = 1 << 10;

    /**
     * 鼠标按钮2的扩展修饰符常量。
     * @since 1.4
     */
    public static final int BUTTON2_DOWN_MASK = 1 << 11;

    /**
     * 鼠标按钮3的扩展修饰符常量。
     * @since 1.4
     */
    public static final int BUTTON3_DOWN_MASK = 1 << 12;

    /**
     * AltGraph 键扩展修饰符常量。
     * @since 1.4
     */
    public static final int ALT_GRAPH_DOWN_MASK = 1 << 13;

    /**
     * 用于额外按钮的扩展修饰符数组。
     * @see getButtonDownMasks
     * 有二十个按钮可以适应4字节的空间。
     * 保留了一个额外的位用于 FIRST_HIGH_BIT。
     * @since 7.0
     */
    private static final int [] BUTTON_DOWN_MASK = new int [] { BUTTON1_DOWN_MASK,
                                                               BUTTON2_DOWN_MASK,
                                                               BUTTON3_DOWN_MASK,
                                                               1<<14, // 第4个物理按钮（这不是滚轮！）
                                                               1<<15, //（这不是滚轮！）
                                                               1<<16,
                                                               1<<17,
                                                               1<<18,
                                                               1<<19,
                                                               1<<20,
                                                               1<<21,
                                                               1<<22,
                                                               1<<23,
                                                               1<<24,
                                                               1<<25,
                                                               1<<26,
                                                               1<<27,
                                                               1<<28,
                                                               1<<29,
                                                               1<<30};

    /**
     * 获取额外按钮的扩展修饰符数组的方法。
     * @since 7.0
     */
    private static int [] getButtonDownMasks(){
        return Arrays.copyOf(BUTTON_DOWN_MASK, BUTTON_DOWN_MASK.length);
    }


    /**
     * 获取任何现有鼠标按钮的掩码的方法。
     * 返回的掩码可用于不同的目的。以下是一些用途：
     * <ul>
     * <li> {@link java.awt.Robot#mousePress(int) mousePress(buttons)} 和
     *      {@link java.awt.Robot#mouseRelease(int) mouseRelease(buttons)}
     * <li> 作为创建新的 {@link MouseEvent} 实例时的 {@code modifiers} 参数
     * <li> 检查现有 {@code MouseEvent} 的 {@link MouseEvent#getModifiersEx() modifiersEx}
     * </ul>
     * @param button 代表按钮的数字，从1开始。
     * 例如，
     * <pre>
     * int button = InputEvent.getMaskForButton(1);
     * </pre>
     * 与
     * <pre>
     * int button = InputEvent.getMaskForButton(MouseEvent.BUTTON1);
     * </pre>
     * 具有相同的含义，因为 {@link MouseEvent#BUTTON1 MouseEvent.BUTTON1} 等于 1。
     * 如果鼠标有三个启用的按钮（参见 {@link java.awt.MouseInfo#getNumberOfButtons() MouseInfo.getNumberOfButtons()}），
     * 则传递到方法中的左列值将返回右列对应的值：
     * <PRE>
     *    <b>button </b>   <b>返回的掩码</b>
     *    {@link MouseEvent#BUTTON1 BUTTON1}  {@link MouseEvent#BUTTON1_DOWN_MASK BUTTON1_DOWN_MASK}
     *    {@link MouseEvent#BUTTON2 BUTTON2}  {@link MouseEvent#BUTTON2_DOWN_MASK BUTTON2_DOWN_MASK}
     *    {@link MouseEvent#BUTTON3 BUTTON3}  {@link MouseEvent#BUTTON3_DOWN_MASK BUTTON3_DOWN_MASK}
     * </PRE>
     * 如果鼠标有超过三个启用的按钮，则可以接受更多的值（4, 5, 等等）。这些扩展按钮没有分配常量。
     * 通过此方法返回的额外按钮的按钮掩码没有像前三个按钮掩码那样的名称。
     * <p>
     * 此方法有以下实现限制。
     * 它仅返回有限数量的按钮的掩码。最大数量是实现依赖的，可能会有所不同。
     * 这个限制由鼠标上可能存在的按钮数量定义，但大于
     * {@link java.awt.MouseInfo#getNumberOfButtons() MouseInfo.getNumberOfButtons()}。
     * <p>
     * @throws IllegalArgumentException 如果 {@code button} 小于零或大于为按钮保留的掩码数量
     * @since 7.0
     * @see java.awt.MouseInfo#getNumberOfButtons()
     * @see Toolkit#areExtraMouseButtonsEnabled()
     * @see MouseEvent#getModifiers()
     * @see MouseEvent#getModifiersEx()
     */
    public static int getMaskForButton(int button) {
        if (button <= 0 || button > BUTTON_DOWN_MASK.length) {
            throw new IllegalArgumentException("button doesn\'t exist " + button);
        }
        return BUTTON_DOWN_MASK[button - 1];
    }

    // 如果要添加额外的修饰符位，下面的常量必须更新！
    // 实际上，不建议添加修饰符位
    // 因为这可能会破坏应用程序
    // 参见 bug# 5066958
    static final int FIRST_HIGH_BIT = 1 << 31;

    static final int JDK_1_3_MODIFIERS = SHIFT_DOWN_MASK - 1;
    static final int HIGH_MODIFIERS = ~( FIRST_HIGH_BIT - 1 );

    /**
     * 输入事件的时间戳，格式为 UTC。时间戳表示输入事件创建的时间。
     *
     * @serial
     * @see #getWhen()
     */
    long when;

    /**
     * 输入事件触发时的修饰符掩码状态。
     *
     * @serial
     * @see #getModifiers()
     * @see #getModifiersEx()
     * @see java.awt.event.KeyEvent
     * @see java.awt.event.MouseEvent
     */
    int modifiers;

    /*
     * 表示此实例是否可以访问系统剪贴板的标志。
     */
    private transient boolean canAccessSystemClipboard;

    static {
        /* 确保加载必要的本机库 */
        NativeLibLoader.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
        AWTAccessor.setInputEventAccessor(
            new AWTAccessor.InputEventAccessor() {
                public int[] getButtonDownMasks() {
                    return InputEvent.getButtonDownMasks();
                }
            });
    }

    /**
     * 使用指定的源组件、修饰符和类型构造 InputEvent 对象。
     * <p> 如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source 事件的来源对象
     * @param id 识别事件类型的整数。
     *           允许传递任何 {@code InputEvent} 类的子类允许的值。
     *           传递不同的值会导致未指定的行为
     * @param when 事件发生的时间，以长整数表示。
     *             不建议传递负值或零值
     * @param modifiers 描述事件期间按下的修饰键和鼠标按钮（例如，shift、ctrl、alt 和 meta）的修饰符掩码。
     *                  只允许使用扩展修饰符作为此参数的值（参见 {@link InputEvent#getModifiersEx}
     *                  类中对扩展修饰符的描述）。
     *                  不建议传递负值。
     *                  零值表示没有传递修饰符
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getSource()
     * @see #getID()
     * @see #getWhen()
     * @see #getModifiers()
     */
    InputEvent(Component source, int id, long when, int modifiers) {
        super(source, id);
        this.when = when;
        this.modifiers = modifiers;
        canAccessSystemClipboard = canAccessSystemClipboard();
    }

    private boolean canAccessSystemClipboard() {
        boolean b = false;

        if (!GraphicsEnvironment.isHeadless()) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                try {
                    sm.checkPermission(SecurityConstants.AWT.ACCESS_CLIPBOARD_PERMISSION);
                    b = true;
                } catch (SecurityException se) {
                    if (logger.isLoggable(PlatformLogger.Level.FINE)) {
                        logger.fine("InputEvent.canAccessSystemClipboard() got SecurityException ", se);
                    }
                }
            } else {
                b = true;
            }
        }

        return b;
    }

    /**
     * 返回此事件中 Shift 修饰符是否按下。
     */
    public boolean isShiftDown() {
        return (modifiers & SHIFT_MASK) != 0;
    }

    /**
     * 返回此事件中 Control 修饰符是否按下。
     */
    public boolean isControlDown() {
        return (modifiers & CTRL_MASK) != 0;
    }

    /**
     * 返回此事件中 Meta 修饰符是否按下。
     */
    public boolean isMetaDown() {
        return (modifiers & META_MASK) != 0;
    }

    /**
     * 返回此事件中 Alt 修饰符是否按下。
     */
    public boolean isAltDown() {
        return (modifiers & ALT_MASK) != 0;
    }

    /**
     * 返回此事件中 AltGraph 修饰符是否按下。
     */
    public boolean isAltGraphDown() {
        return (modifiers & ALT_GRAPH_MASK) != 0;
    }

    /**
     * 返回此事件发生的时间与 1970 年 1 月 1 日 00:00:00 UTC 之间的毫秒差。
     */
    public long getWhen() {
        return when;
    }

    /**
     * 返回此事件的修饰符掩码。
     */
    public int getModifiers() {
        return modifiers & (JDK_1_3_MODIFIERS | HIGH_MODIFIERS);
    }

    /**
     * 返回此事件的扩展修饰符掩码。
     * <P>
     * 扩展修饰符是以 _DOWN_MASK 后缀结尾的修饰符，例如 ALT_DOWN_MASK、BUTTON1_DOWN_MASK 等。
     * <P>
     * 扩展修饰符表示所有模式键（如 ALT、CTRL、META 和鼠标按钮）在事件发生后的状态。
     * <P>
     * 例如，如果用户按下 <b>按钮 1</b> 然后按下 <b>按钮 2</b>，再按相同的顺序释放它们，
     * 将生成以下事件序列：
     * <PRE>
     *    <code>MOUSE_PRESSED</code>:  <code>BUTTON1_DOWN_MASK</code>
     *    <code>MOUSE_PRESSED</code>:  <code>BUTTON1_DOWN_MASK | BUTTON2_DOWN_MASK</code>
     *    <code>MOUSE_RELEASED</code>: <code>BUTTON2_DOWN_MASK</code>
     *    <code>MOUSE_CLICKED</code>:  <code>BUTTON2_DOWN_MASK</code>
     *    <code>MOUSE_RELEASED</code>:
     *    <code>MOUSE_CLICKED</code>:
     * </PRE>
     * <P>
     * 不建议使用 <code>==</code> 比较此方法的返回值，因为将来可能会添加新的修饰符。
     * 例如，检查 SHIFT 和 BUTTON1 是否按下，而 CTRL 未按下的适当方法如下所示：
     * <PRE>
     *    int onmask = SHIFT_DOWN_MASK | BUTTON1_DOWN_MASK;
     *    int offmask = CTRL_DOWN_MASK;
     *    if ((event.getModifiersEx() &amp; (onmask | offmask)) == onmask) {
     *        ...
     *    }
     * </PRE>
     * 上面的代码即使添加了新的修饰符也能正常工作。
     *
     * @since 1.4
     */
    public int getModifiersEx() {
        return modifiers & ~JDK_1_3_MODIFIERS;
    }


                /**
     * 消费此事件，使其不会以默认方式由生成它的源处理。
     */
    public void consume() {
        consumed = true;
    }

    /**
     * 返回此事件是否已被消费。
     * @see #consume
     */
    public boolean isConsumed() {
        return consumed;
    }

    // 与 JDK 1.1 的状态序列化兼容
    static final long serialVersionUID = -2482525981698309786L;

    /**
     * 返回描述扩展修饰键和鼠标按钮的字符串，例如 "Shift"、"Button1" 或 "Ctrl+Shift"。
     * 这些字符串可以通过更改 <code>awt.properties</code> 文件进行本地化。
     * <p>
     * 注意，传递负参数是不正确的，将导致返回一个未指定的字符串。
     * 零参数表示没有传递修饰符，将导致返回一个空字符串。
     *
     * @param modifiers 描述事件的扩展修饰键和鼠标按钮的修饰符掩码
     * @return 事件期间按下的扩展修饰键和鼠标按钮的组合的文本描述。
     * @since 1.4
     */
    public static String getModifiersExText(int modifiers) {
        StringBuilder buf = new StringBuilder();
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.meta", "Meta"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.control", "Ctrl"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.alt", "Alt"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.shift", "Shift"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.altGraph", "Alt Graph"));
            buf.append("+");
        }

        int buttonNumber = 1;
        for (int mask : InputEvent.BUTTON_DOWN_MASK){
            if ((modifiers & mask) != 0) {
                buf.append(Toolkit.getProperty("AWT.button"+buttonNumber, "Button"+buttonNumber));
                buf.append("+");
            }
            buttonNumber++;
        }
        if (buf.length() > 0) {
            buf.setLength(buf.length()-1); // 移除尾部的 '+'
        }
        return buf.toString();
    }
}
