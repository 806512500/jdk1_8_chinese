
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
import java.awt.Toolkit;
import java.io.IOException;
import java.io.ObjectInputStream;
import sun.awt.AWTAccessor;

/**
 * 一个事件，表示在组件中发生了按键操作。
 * <p>
 * 此低级事件由组件对象（如文本字段）生成，当按键被按下、释放或输入时触发。
 * 该事件传递给每个使用组件的 <code>addKeyListener</code> 方法注册以接收此类事件的
 * <code>KeyListener</code> 或 <code>KeyAdapter</code> 对象。
 * （<code>KeyAdapter</code> 对象实现了 <code>KeyListener</code> 接口。）
 * 每个这样的监听器对象在事件发生时都会收到此 <code>KeyEvent</code>。
 * <p>
 * <em>"键输入"事件</em> 是更高层次的事件，通常不依赖于平台或键盘布局。
 * 它们在输入 Unicode 字符时生成，是获取字符输入的首选方式。
 * 在最简单的情况下，键输入事件由单次按键（例如 'a'）生成。
 * 通常，字符由一系列按键生成（例如 'shift' + 'a'），从按键按下事件到键输入事件的映射可能是多对一或多对多。
 * 通常情况下，按键释放不是生成键输入事件所必需的，但在某些情况下（例如，通过 Windows 中的 Alt-Numpad 方法输入 ASCII 序列），
 * 直到按键释放时才会生成键输入事件。对于不生成 Unicode 字符的键（例如，操作键、修饰键等），不会生成键输入事件。
 * <p>
 * <code>getKeyChar</code> 方法总是返回一个有效的 Unicode 字符或 <code>CHAR_UNDEFINED</code>。
 * 字符输入由 <code>KEY_TYPED</code> 事件报告：对于 <code>KEY_PRESSED</code> 和 <code>KEY_RELEASED</code> 事件，
 * <code>getKeyChar</code> 方法的结果不一定与字符输入相关。因此，对于 <code>KEY_TYPED</code> 事件，
 * <code>getKeyChar</code> 方法的结果是有意义的。
 * <p>
 * 对于按键按下和按键释放事件，<code>getKeyCode</code> 方法返回事件的 keyCode。
 * 对于键输入事件，<code>getKeyCode</code> 方法总是返回 <code>VK_UNDEFINED</code>。
 * 对于许多国际键盘布局，可以使用 <code>getExtendedKeyCode</code> 方法。
 *
 * <p>
 * <em>"按键按下"和"按键释放"事件</em> 是较低层次的事件，依赖于平台和键盘布局。
 * 它们在按键被按下或释放时生成，是唯一能够检测到不生成字符输入的键（例如，操作键、修饰键等）的方式。
 * 按键被按下或释放的键由 <code>getKeyCode</code> 和 <code>getExtendedKeyCode</code> 方法指示，这些方法返回虚拟键码。
 *
 * <p>
 * <em>虚拟键码</em> 用于报告哪个键盘键被按下，而不是由一个或多个按键组合生成的字符（例如 "A"，它来自 shift 和 "a"）。
 *
 * <p>
 * 例如，按下 Shift 键将生成一个 <code>KEY_PRESSED</code> 事件，其 keyCode 为 <code>VK_SHIFT</code>，
 * 而按下 'a' 键将生成一个 <code>VK_A</code> 的 keyCode。'a' 键释放后，将生成一个 <code>KEY_RELEASED</code> 事件，其 keyCode 为 <code>VK_A</code>。
 * 单独地，将生成一个 <code>KEY_TYPED</code> 事件，其 keyChar 值为 'A'。
 *
 * <p>
 * 在键盘上按下并释放一个键将生成以下键事件（按顺序）：
 * <PRE>
 *    {@code KEY_PRESSED}
 *    {@code KEY_TYPED}（仅在可以生成有效的 Unicode 字符时生成。）
 *    {@code KEY_RELEASED}
 * </PRE>
 *
 * 但在某些情况下（例如，自动重复或输入方法激活时），顺序可能不同（且依赖于平台）。
 *
 * <p>
 * 注意：
 * <ul>
 * <li>不会生成键输入事件的键组合，例如 F1 和 HELP 键等操作键。
 * <li>并非所有键盘或系统都能生成所有虚拟键码。Java 不会人为生成这些键。
 * <li>虚拟键码不标识物理键：它们依赖于平台和键盘布局。例如，使用美国键盘布局时生成 <code>VK_Q</code> 的键，
 * 在使用法国键盘布局时将生成 <code>VK_A</code>。
 * <li>使用美国键盘布局时生成 <code>VK_Q</code> 的键在俄语或希伯来语布局中也会生成唯一的代码。
 * 对于这些和其他许多布局中的代码，没有 <code>VK_</code> 常量。这些代码可以通过使用 <code>getExtendedKeyCode</code> 获得，
 * 并在使用 <code>VK_</code> 常量时使用。
 * <li>并非所有字符都有与之关联的键码。例如，没有问号的键码，因为没有键盘上它出现在主层。
 * <li>为了支持平台无关的操作键处理，Java 平台使用了一些额外的虚拟键常量来表示
 * 否则需要通过解释虚拟键码和修饰符来识别的功能。例如，对于日本 Windows 键盘，返回 <code>VK_ALL_CANDIDATES</code>
 * 而不是带有 ALT 修饰符的 <code>VK_CONVERT</code>。
 * <li>如 <a href="../doc-files/FocusSpec.html">焦点规范</a> 所指定，默认情况下，键事件被分派给焦点所有者。
 * </ul>
 *
 * <p>
 * 警告：除了 Java 语言定义的键（<code>VK_ENTER</code>、<code>VK_BACK_SPACE</code> 和 <code>VK_TAB</code>）外，
 * 不要依赖 <code>VK_</code> 常量的值。Sun 保留根据需要更改这些值的权利，以支持更广泛的键盘。
 * <p>
 * 如果任何特定 <code>KeyEvent</code> 实例的 <code>id</code> 参数不在 <code>KEY_FIRST</code> 到 <code>KEY_LAST</code> 的范围内，
 * 将导致未指定的行为。
 *
 * @author Carl Quinn
 * @author Amy Fowler
 * @author Norbert Lindenberg
 *
 * @see KeyAdapter
 * @see KeyListener
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/keylistener.html">教程：编写键监听器</a>
 *
 * @since 1.1
 */
public class KeyEvent extends InputEvent {

    /**
     * 存储本机事件分派系统的状态
     * - true，如果在事件创建时事件代理机制处于活动状态
     * - false，如果它处于非活动状态
     * 用于在代理处于活动状态时正确分派事件
     */
    private boolean isProxyActive = false;

    /**
     * 键事件使用的 id 范围的第一个数字。
     */
    public static final int KEY_FIRST = 400;

    /**
     * 键事件使用的 id 范围的最后一个数字。
     */
    public static final int KEY_LAST  = 402;

    /**
     * "键输入"事件。此事件在字符输入时生成。在最简单的情况下，它由单次按键生成。
     * 通常，字符由一系列按键生成，从按键按下事件到键输入事件的映射可能是多对一或多对多。
     */
    public static final int KEY_TYPED = KEY_FIRST;

    /**
     * "按键按下"事件。此事件在按键被按下时生成。
     */
    public static final int KEY_PRESSED = 1 + KEY_FIRST; //Event.KEY_PRESS

    /**
     * "按键释放"事件。此事件在按键被释放时生成。
     */
    public static final int KEY_RELEASED = 2 + KEY_FIRST; //Event.KEY_RELEASE

    /* 虚拟键码。 */

    public static final int VK_ENTER          = '\n';
    public static final int VK_BACK_SPACE     = '\b';
    public static final int VK_TAB            = '\t';
    public static final int VK_CANCEL         = 0x03;
    public static final int VK_CLEAR          = 0x0C;
    public static final int VK_SHIFT          = 0x10;
    public static final int VK_CONTROL        = 0x11;
    public static final int VK_ALT            = 0x12;
    public static final int VK_PAUSE          = 0x13;
    public static final int VK_CAPS_LOCK      = 0x14;
    public static final int VK_ESCAPE         = 0x1B;
    public static final int VK_SPACE          = 0x20;
    public static final int VK_PAGE_UP        = 0x21;
    public static final int VK_PAGE_DOWN      = 0x22;
    public static final int VK_END            = 0x23;
    public static final int VK_HOME           = 0x24;

    /**
     * 非数字键盘的 <b>左</b> 箭头键的常量。
     * @see #VK_KP_LEFT
     */
    public static final int VK_LEFT           = 0x25;

    /**
     * 非数字键盘的 <b>上</b> 箭头键的常量。
     * @see #VK_KP_UP
     */
    public static final int VK_UP             = 0x26;

    /**
     * 非数字键盘的 <b>右</b> 箭头键的常量。
     * @see #VK_KP_RIGHT
     */
    public static final int VK_RIGHT          = 0x27;

    /**
     * 非数字键盘的 <b>下</b> 箭头键的常量。
     * @see #VK_KP_DOWN
     */
    public static final int VK_DOWN           = 0x28;

    /**
     * 逗号键，"," 的常量。
     */
    public static final int VK_COMMA          = 0x2C;

    /**
     * 减号键，"-" 的常量。
     * @since 1.2
     */
    public static final int VK_MINUS          = 0x2D;

    /**
     * 句点键，"." 的常量。
     */
    public static final int VK_PERIOD         = 0x2E;

    /**
     * 斜杠键，"/" 的常量。
     */
    public static final int VK_SLASH          = 0x2F;

    /** VK_0 到 VK_9 与 ASCII '0' 到 '9' (0x30 - 0x39) 相同 */
    public static final int VK_0              = 0x30;
    public static final int VK_1              = 0x31;
    public static final int VK_2              = 0x32;
    public static final int VK_3              = 0x33;
    public static final int VK_4              = 0x34;
    public static final int VK_5              = 0x35;
    public static final int VK_6              = 0x36;
    public static final int VK_7              = 0x37;
    public static final int VK_8              = 0x38;
    public static final int VK_9              = 0x39;

    /**
     * 分号键，";" 的常量。
     */
    public static final int VK_SEMICOLON      = 0x3B;

    /**
     * 等号键，"=" 的常量。
     */
    public static final int VK_EQUALS         = 0x3D;

    /** VK_A 到 VK_Z 与 ASCII 'A' 到 'Z' (0x41 - 0x5A) 相同 */
    public static final int VK_A              = 0x41;
    public static final int VK_B              = 0x42;
    public static final int VK_C              = 0x43;
    public static final int VK_D              = 0x44;
    public static final int VK_E              = 0x45;
    public static final int VK_F              = 0x46;
    public static final int VK_G              = 0x47;
    public static final int VK_H              = 0x48;
    public static final int VK_I              = 0x49;
    public static final int VK_J              = 0x4A;
    public static final int VK_K              = 0x4B;
    public static final int VK_L              = 0x4C;
    public static final int VK_M              = 0x4D;
    public static final int VK_N              = 0x4E;
    public static final int VK_O              = 0x4F;
    public static final int VK_P              = 0x50;
    public static final int VK_Q              = 0x51;
    public static final int VK_R              = 0x52;
    public static final int VK_S              = 0x53;
    public static final int VK_T              = 0x54;
    public static final int VK_U              = 0x55;
    public static final int VK_V              = 0x56;
    public static final int VK_W              = 0x57;
    public static final int VK_X              = 0x58;
    public static final int VK_Y              = 0x59;
    public static final int VK_Z              = 0x5A;

    /**
     * 开方括号键，"[" 的常量。
     */
    public static final int VK_OPEN_BRACKET   = 0x5B;

    /**
     * 反斜杠键，"\" 的常量。
     */
    public static final int VK_BACK_SLASH     = 0x5C;

    /**
     * 闭方括号键，"]" 的常量。
     */
    public static final int VK_CLOSE_BRACKET  = 0x5D;

    public static final int VK_NUMPAD0        = 0x60;
    public static final int VK_NUMPAD1        = 0x61;
    public static final int VK_NUMPAD2        = 0x62;
    public static final int VK_NUMPAD3        = 0x63;
    public static final int VK_NUMPAD4        = 0x64;
    public static final int VK_NUMPAD5        = 0x65;
    public static final int VK_NUMPAD6        = 0x66;
    public static final int VK_NUMPAD7        = 0x67;
    public static final int VK_NUMPAD8        = 0x68;
    public static final int VK_NUMPAD9        = 0x69;
    public static final int VK_MULTIPLY       = 0x6A;
    public static final int VK_ADD            = 0x6B;

    /**
     * 此常量已过时，仅为了向后兼容而包含。
     * @see #VK_SEPARATOR
     */
    public static final int VK_SEPARATER      = 0x6C;

    /**
     * 数字键盘分隔符键的常量。
     * @since 1.4
     */
    public static final int VK_SEPARATOR      = VK_SEPARATER;

    public static final int VK_SUBTRACT       = 0x6D;
    public static final int VK_DECIMAL        = 0x6E;
    public static final int VK_DIVIDE         = 0x6F;
    public static final int VK_DELETE         = 0x7F; /* ASCII DEL */
    public static final int VK_NUM_LOCK       = 0x90;
    public static final int VK_SCROLL_LOCK    = 0x91;

    /** F1 功能键的常量。 */
    public static final int VK_F1             = 0x70;

    /** F2 功能键的常量。 */
    public static final int VK_F2             = 0x71;

    /** F3 功能键的常量。 */
    public static final int VK_F3             = 0x72;

    /** F4 功能键的常量。 */
    public static final int VK_F4             = 0x73;

    /** F5 功能键的常量。 */
    public static final int VK_F5             = 0x74;

    /** F6 功能键的常量。 */
    public static final int VK_F6             = 0x75;


                /** F7功能键的常量。 */
    public static final int VK_F7             = 0x76;

    /** F8功能键的常量。 */
    public static final int VK_F8             = 0x77;

    /** F9功能键的常量。 */
    public static final int VK_F9             = 0x78;

    /** F10功能键的常量。 */
    public static final int VK_F10            = 0x79;

    /** F11功能键的常量。 */
    public static final int VK_F11            = 0x7A;

    /** F12功能键的常量。 */
    public static final int VK_F12            = 0x7B;

    /**
     * F13功能键的常量。
     * @since 1.2
     */
    /* F13 - F24 用于IBM 3270键盘；使用随机范围的常量。 */
    public static final int VK_F13            = 0xF000;

    /**
     * F14功能键的常量。
     * @since 1.2
     */
    public static final int VK_F14            = 0xF001;

    /**
     * F15功能键的常量。
     * @since 1.2
     */
    public static final int VK_F15            = 0xF002;

    /**
     * F16功能键的常量。
     * @since 1.2
     */
    public static final int VK_F16            = 0xF003;

    /**
     * F17功能键的常量。
     * @since 1.2
     */
    public static final int VK_F17            = 0xF004;

    /**
     * F18功能键的常量。
     * @since 1.2
     */
    public static final int VK_F18            = 0xF005;

    /**
     * F19功能键的常量。
     * @since 1.2
     */
    public static final int VK_F19            = 0xF006;

    /**
     * F20功能键的常量。
     * @since 1.2
     */
    public static final int VK_F20            = 0xF007;

    /**
     * F21功能键的常量。
     * @since 1.2
     */
    public static final int VK_F21            = 0xF008;

    /**
     * F22功能键的常量。
     * @since 1.2
     */
    public static final int VK_F22            = 0xF009;

    /**
     * F23功能键的常量。
     * @since 1.2
     */
    public static final int VK_F23            = 0xF00A;

    /**
     * F24功能键的常量。
     * @since 1.2
     */
    public static final int VK_F24            = 0xF00B;

    public static final int VK_PRINTSCREEN    = 0x9A;
    public static final int VK_INSERT         = 0x9B;
    public static final int VK_HELP           = 0x9C;
    public static final int VK_META           = 0x9D;

    public static final int VK_BACK_QUOTE     = 0xC0;
    public static final int VK_QUOTE          = 0xDE;

    /**
     * 数字小键盘<b>上</b>箭头键的常量。
     * @see #VK_UP
     * @since 1.2
     */
    public static final int VK_KP_UP          = 0xE0;

    /**
     * 数字小键盘<b>下</b>箭头键的常量。
     * @see #VK_DOWN
     * @since 1.2
     */
    public static final int VK_KP_DOWN        = 0xE1;

    /**
     * 数字小键盘<b>左</b>箭头键的常量。
     * @see #VK_LEFT
     * @since 1.2
     */
    public static final int VK_KP_LEFT        = 0xE2;

    /**
     * 数字小键盘<b>右</b>箭头键的常量。
     * @see #VK_RIGHT
     * @since 1.2
     */
    public static final int VK_KP_RIGHT       = 0xE3;

    /* 用于欧洲键盘 */
    /** @since 1.2 */
    public static final int VK_DEAD_GRAVE               = 0x80;
    /** @since 1.2 */
    public static final int VK_DEAD_ACUTE               = 0x81;
    /** @since 1.2 */
    public static final int VK_DEAD_CIRCUMFLEX          = 0x82;
    /** @since 1.2 */
    public static final int VK_DEAD_TILDE               = 0x83;
    /** @since 1.2 */
    public static final int VK_DEAD_MACRON              = 0x84;
    /** @since 1.2 */
    public static final int VK_DEAD_BREVE               = 0x85;
    /** @since 1.2 */
    public static final int VK_DEAD_ABOVEDOT            = 0x86;
    /** @since 1.2 */
    public static final int VK_DEAD_DIAERESIS           = 0x87;
    /** @since 1.2 */
    public static final int VK_DEAD_ABOVERING           = 0x88;
    /** @since 1.2 */
    public static final int VK_DEAD_DOUBLEACUTE         = 0x89;
    /** @since 1.2 */
    public static final int VK_DEAD_CARON               = 0x8a;
    /** @since 1.2 */
    public static final int VK_DEAD_CEDILLA             = 0x8b;
    /** @since 1.2 */
    public static final int VK_DEAD_OGONEK              = 0x8c;
    /** @since 1.2 */
    public static final int VK_DEAD_IOTA                = 0x8d;
    /** @since 1.2 */
    public static final int VK_DEAD_VOICED_SOUND        = 0x8e;
    /** @since 1.2 */
    public static final int VK_DEAD_SEMIVOICED_SOUND    = 0x8f;

    /** @since 1.2 */
    public static final int VK_AMPERSAND                = 0x96;
    /** @since 1.2 */
    public static final int VK_ASTERISK                 = 0x97;
    /** @since 1.2 */
    public static final int VK_QUOTEDBL                 = 0x98;
    /** @since 1.2 */
    public static final int VK_LESS                     = 0x99;

    /** @since 1.2 */
    public static final int VK_GREATER                  = 0xa0;
    /** @since 1.2 */
    public static final int VK_BRACELEFT                = 0xa1;
    /** @since 1.2 */
    public static final int VK_BRACERIGHT               = 0xa2;

    /**
     * "@"键的常量。
     * @since 1.2
     */
    public static final int VK_AT                       = 0x0200;

    /**
     * ":"键的常量。
     * @since 1.2
     */
    public static final int VK_COLON                    = 0x0201;

    /**
     * "^"键的常量。
     * @since 1.2
     */
    public static final int VK_CIRCUMFLEX               = 0x0202;

    /**
     * "$"键的常量。
     * @since 1.2
     */
    public static final int VK_DOLLAR                   = 0x0203;

    /**
     * 欧元货币符号键的常量。
     * @since 1.2
     */
    public static final int VK_EURO_SIGN                = 0x0204;

    /**
     * "!"键的常量。
     * @since 1.2
     */
    public static final int VK_EXCLAMATION_MARK         = 0x0205;

    /**
     * 倒置感叹号键的常量。
     * @since 1.2
     */
    public static final int VK_INVERTED_EXCLAMATION_MARK = 0x0206;

    /**
     * "("键的常量。
     * @since 1.2
     */
    public static final int VK_LEFT_PARENTHESIS         = 0x0207;

    /**
     * "#"键的常量。
     * @since 1.2
     */
    public static final int VK_NUMBER_SIGN              = 0x0208;

    /**
     * "+"键的常量。
     * @since 1.2
     */
    public static final int VK_PLUS                     = 0x0209;

    /**
     * ")"键的常量。
     * @since 1.2
     */
    public static final int VK_RIGHT_PARENTHESIS        = 0x020A;

    /**
     * "_"键的常量。
     * @since 1.2
     */
    public static final int VK_UNDERSCORE               = 0x020B;

    /**
     * Microsoft Windows "Windows"键的常量。
     * 用于表示该键的左右版本。
     * @see #getKeyLocation()
     * @since 1.5
     */
    public static final int VK_WINDOWS                  = 0x020C;

    /**
     * Microsoft Windows 上下文菜单键的常量。
     * @since 1.5
     */
    public static final int VK_CONTEXT_MENU             = 0x020D;

    /* 用于亚洲键盘的输入方法支持 */

    /* 不清楚其含义 - 列在Microsoft Windows API中 */
    public static final int VK_FINAL                    = 0x0018;

    /** 转换功能键的常量。 */
    /* 日本PC 106键盘，日本Solaris键盘：henkan */
    public static final int VK_CONVERT                  = 0x001C;

    /** 不转换功能键的常量。 */
    /* 日本PC 106键盘：muhenkan */
    public static final int VK_NONCONVERT               = 0x001D;

    /** 接受或提交功能键的常量。 */
    /* 日本Solaris键盘：kakutei */
    public static final int VK_ACCEPT                   = 0x001E;

    /* 不清楚其含义 - 列在Microsoft Windows API中 */
    public static final int VK_MODECHANGE               = 0x001F;

    /* 被VK_KANA_LOCK取代用于Microsoft Windows和Solaris；
       其他平台可能仍使用 */
    public static final int VK_KANA                     = 0x0015;

    /* 被VK_INPUT_METHOD_ON_OFF取代用于Microsoft Windows和Solaris；
       其他平台可能仍使用 */
    public static final int VK_KANJI                    = 0x0019;

    /**
     * 字母数字功能键的常量。
     * @since 1.2
     */
    /* 日本PC 106键盘：eisuu */
    public static final int VK_ALPHANUMERIC             = 0x00F0;

    /**
     * 片假名功能键的常量。
     * @since 1.2
     */
    /* 日本PC 106键盘：katakana */
    public static final int VK_KATAKANA                 = 0x00F1;

    /**
     * 平假名功能键的常量。
     * @since 1.2
     */
    /* 日本PC 106键盘：hiragana */
    public static final int VK_HIRAGANA                 = 0x00F2;

    /**
     * 全角字符功能键的常量。
     * @since 1.2
     */
    /* 日本PC 106键盘：zenkaku */
    public static final int VK_FULL_WIDTH               = 0x00F3;

    /**
     * 半角字符功能键的常量。
     * @since 1.2
     */
    /* 日本PC 106键盘：hankaku */
    public static final int VK_HALF_WIDTH               = 0x00F4;

    /**
     * 罗马字符功能键的常量。
     * @since 1.2
     */
    /* 日本PC 106键盘：roumaji */
    public static final int VK_ROMAN_CHARACTERS         = 0x00F5;

    /**
     * 所有候选功能键的常量。
     * @since 1.2
     */
    /* 日本PC 106键盘 - VK_CONVERT + ALT: zenkouho */
    public static final int VK_ALL_CANDIDATES           = 0x0100;

    /**
     * 前一个候选功能键的常量。
     * @since 1.2
     */
    /* 日本PC 106键盘 - VK_CONVERT + SHIFT: maekouho */
    public static final int VK_PREVIOUS_CANDIDATE       = 0x0101;

    /**
     * 代码输入功能键的常量。
     * @since 1.2
     */
    /* 日本PC 106键盘 - VK_ALPHANUMERIC + ALT: kanji bangou */
    public static final int VK_CODE_INPUT               = 0x0102;

    /**
     * 日语片假名功能键的常量。
     * 此键切换到日语输入法并选择其片假名输入模式。
     * @since 1.2
     */
    /* 日本Macintosh键盘 - VK_JAPANESE_HIRAGANA + SHIFT */
    public static final int VK_JAPANESE_KATAKANA        = 0x0103;

    /**
     * 日语平假名功能键的常量。
     * 此键切换到日语输入法并选择其平假名输入模式。
     * @since 1.2
     */
    /* 日本Macintosh键盘 */
    public static final int VK_JAPANESE_HIRAGANA        = 0x0104;

    /**
     * 日语罗马字符功能键的常量。
     * 此键切换到日语输入法并选择其罗马直接输入模式。
     * @since 1.2
     */
    /* 日本Macintosh键盘 */
    public static final int VK_JAPANESE_ROMAN           = 0x0105;

    /**
     * 锁定假名功能键的常量。
     * 此键将键盘锁定为假名布局。
     * @since 1.3
     */
    /* 日本PC 106键盘与特殊Windows驱动程序 - eisuu + Control；日本Solaris键盘：kana */
    public static final int VK_KANA_LOCK                = 0x0106;

    /**
     * 输入方法开关键的常量。
     * @since 1.3
     */
    /* 日本PC 106键盘：kanji。日本Solaris键盘：nihongo */
    public static final int VK_INPUT_METHOD_ON_OFF      = 0x0107;

    /* 用于Sun键盘 */
    /** @since 1.2 */
    public static final int VK_CUT                      = 0xFFD1;
    /** @since 1.2 */
    public static final int VK_COPY                     = 0xFFCD;
    /** @since 1.2 */
    public static final int VK_PASTE                    = 0xFFCF;
    /** @since 1.2 */
    public static final int VK_UNDO                     = 0xFFCB;
    /** @since 1.2 */
    public static final int VK_AGAIN                    = 0xFFC9;
    /** @since 1.2 */
    public static final int VK_FIND                     = 0xFFD0;
    /** @since 1.2 */
    public static final int VK_PROPS                    = 0xFFCA;
    /** @since 1.2 */
    public static final int VK_STOP                     = 0xFFC8;

    /**
     * 组合功能键的常量。
     * @since 1.2
     */
    public static final int VK_COMPOSE                  = 0xFF20;

    /**
     * AltGraph功能键的常量。
     * @since 1.2
     */
    public static final int VK_ALT_GRAPH                = 0xFF7E;

    /**
     * 开始键的常量。
     * @since 1.5
     */
    public static final int VK_BEGIN                    = 0xFF58;

    /**
     * 表示键码未知的值。
     * KEY_TYPED事件没有键码值；使用此值代替。
     */
    public static final int VK_UNDEFINED      = 0x0;

    /**
     * 无法映射到有效Unicode字符的KEY_PRESSED和KEY_RELEASED事件使用此值作为键字符值。
     */
    public static final char CHAR_UNDEFINED   = 0xFFFF;

    /**
     * 表示键位置不确定或不相关。
     * <code>KEY_TYPED</code>事件没有键位置；使用此值代替。
     * @since 1.4
     */
    public static final int KEY_LOCATION_UNKNOWN  = 0;

    /**
     * 表示按键或释放的键未区分左右版本，
     * 且未来源于数字小键盘（或未来源于与数字小键盘对应的虚拟键）。
     * @since 1.4
     */
    public static final int KEY_LOCATION_STANDARD = 1;

    /**
     * 表示按键或释放的键位于左侧键位（该键有多个可能的位置）。
     * 例如：左侧Shift键。
     * @since 1.4
     */
    public static final int KEY_LOCATION_LEFT     = 2;

    /**
     * 表示按键或释放的键位于右侧键位（该键有多个可能的位置）。
     * 例如：右侧Shift键。
     * @since 1.4
     */
    public static final int KEY_LOCATION_RIGHT    = 3;

    /**
     * 表示键事件来源于数字小键盘或与数字小键盘对应的虚拟键。
     * @since 1.4
     */
    public static final int KEY_LOCATION_NUMPAD   = 4;

    /**
     * 分配给键盘上每个键的唯一值。
     * 大多数键盘可以触发一组通用的键码。
     * 应使用键码的符号名称，而不是其值本身。
     *
     * @serial
     * @see #getKeyCode()
     * @see #setKeyCode(int)
     */
    int  keyCode;

    /**
     * <code>keyChar</code>是由键盘上的键或键组合触发的有效Unicode字符。
     *
     * @serial
     * @see #getKeyChar()
     * @see #setKeyChar(char)
     */
    char keyChar;


                /**
     * 键盘上键的位置。
     *
     * 有些键在键盘上出现多次，例如左和右的Shift键。此外，有些键出现在数字键盘上。此变量用于区分这些键。
     *
     * 合法的值只有 <code>KEY_LOCATION_UNKNOWN</code>、
     * <code>KEY_LOCATION_STANDARD</code>、<code>KEY_LOCATION_LEFT</code>、
     * <code>KEY_LOCATION_RIGHT</code> 和 <code>KEY_LOCATION_NUMPAD</code>。
     *
     * @serial
     * @see #getKeyLocation()
     */
    int keyLocation;

    // 由本地代码设置。
    private transient long rawCode = 0;
    private transient long primaryLevelUnicode = 0;
    private transient long scancode = 0; // 仅用于 MS Windows
    private transient long extendedKeyCode = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -2352130953028126954L;

    static {
        /* 确保加载必要的本机库 */
        NativeLibLoader.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }

        AWTAccessor.setKeyEventAccessor(
            new AWTAccessor.KeyEventAccessor() {
                public void setRawCode(KeyEvent ev, long rawCode) {
                    ev.rawCode = rawCode;
                }

                public void setPrimaryLevelUnicode(KeyEvent ev,
                                                   long primaryLevelUnicode) {
                    ev.primaryLevelUnicode = primaryLevelUnicode;
                }

                public void setExtendedKeyCode(KeyEvent ev,
                                               long extendedKeyCode) {
                    ev.extendedKeyCode = extendedKeyCode;
                }

                public Component getOriginalSource( KeyEvent ev ) {
                    return ev.originalSource;
                }
            });
    }

    /**
     * 初始化可以从 C 访问的字段的 JNI 字段和方法 ID。
     */
    private static native void initIDs();

    /**
     * 原始事件源。
     *
     * 事件源在处理过程中可能会改变，但在某些情况下
     * 我们需要能够获取原始源。
     */
    private Component originalSource;

    private KeyEvent(Component source, int id, long when, int modifiers,
                    int keyCode, char keyChar, int keyLocation, boolean isProxyActive) {
        this(source, id, when, modifiers, keyCode, keyChar, keyLocation);
        this.isProxyActive = isProxyActive;
    }

    /**
     * 构造一个 <code>KeyEvent</code> 对象。
     * <p>如果 <code>source</code>
     * 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source    事件的起源 <code>Component</code>
     * @param id              表示事件类型的整数。
     *                  有关允许值的信息，请参见
     *                  {@link KeyEvent} 的类描述
     * @param when      指定事件发生时间的长整数。
     *                     不建议传递负值或零值
     * @param modifiers 事件期间按下的修饰键（shift, ctrl,
     *                  alt, meta）。
     *                     不建议传递负值。
     *                     零值表示没有传递修饰键。
     *                  使用扩展的 _DOWN_MASK 或旧的 _MASK 修饰符，
     *                  但不要在一个事件中混合使用这两种模型。
     *                  推荐使用扩展的修饰符
     * @param keyCode   实际键的整数代码，或 VK_UNDEFINED
     *                  （对于键入事件）
     * @param keyChar   由此事件生成的 Unicode 字符，或
     *                  CHAR_UNDEFINED（对于不映射到有效 Unicode 字符的
     *                  按键和释放事件）
     * @param keyLocation  识别键位置。合法的值只有 <code>KEY_LOCATION_UNKNOWN</code>，
     *        <code>KEY_LOCATION_STANDARD</code>、<code>KEY_LOCATION_LEFT</code>、
     *        <code>KEY_LOCATION_RIGHT</code> 和 <code>KEY_LOCATION_NUMPAD</code>。
     * @throws IllegalArgumentException
     *     如果 <code>id</code> 为 <code>KEY_TYPED</code> 且
     *       <code>keyChar</code> 为 <code>CHAR_UNDEFINED</code>；
     *     或者如果 <code>id</code> 为 <code>KEY_TYPED</code> 且
     *       <code>keyCode</code> 不为 <code>VK_UNDEFINED</code>；
     *     或者如果 <code>id</code> 为 <code>KEY_TYPED</code> 且
     *       <code>keyLocation</code> 不为 <code>KEY_LOCATION_UNKNOWN</code>；
     *     或者如果 <code>keyLocation</code> 不是上述枚举的合法值。
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getSource()
     * @see #getID()
     * @see #getWhen()
     * @see #getModifiers()
     * @see #getKeyCode()
     * @see #getKeyChar()
     * @see #getKeyLocation()
     * @since 1.4
     */
    public KeyEvent(Component source, int id, long when, int modifiers,
                    int keyCode, char keyChar, int keyLocation) {
        super(source, id, when, modifiers);
        if (id == KEY_TYPED) {
            if (keyChar == CHAR_UNDEFINED) {
                throw new IllegalArgumentException("无效的 keyChar");
            }
            if (keyCode != VK_UNDEFINED) {
                throw new IllegalArgumentException("无效的 keyCode");
            }
            if (keyLocation != KEY_LOCATION_UNKNOWN) {
                throw new IllegalArgumentException("无效的 keyLocation");
            }
        }

        this.keyCode = keyCode;
        this.keyChar = keyChar;

        if ((keyLocation < KEY_LOCATION_UNKNOWN) ||
            (keyLocation > KEY_LOCATION_NUMPAD)) {
            throw new IllegalArgumentException("无效的 keyLocation");
        }
        this.keyLocation = keyLocation;
        if ((getModifiers() != 0) && (getModifiersEx() == 0)) {
            setNewModifiers();
        } else if ((getModifiers() == 0) && (getModifiersEx() != 0)) {
            setOldModifiers();
        }
        originalSource = source;
    }

    /**
     * 构造一个 <code>KeyEvent</code> 对象。
     * <p>如果 <code>source</code>
     * 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source    事件的起源 <code>Component</code>
     * @param id              表示事件类型的整数。
     *                  有关允许值的信息，请参见
     *                  {@link KeyEvent} 的类描述
     * @param when      指定事件发生时间的长整数。
     *                     不建议传递负值或零值
     * @param modifiers 事件期间按下的修饰键（shift, ctrl,
     *                  alt, meta）。
     *                     不建议传递负值。
     *                     零值表示没有传递修饰键。
     *                  使用扩展的 _DOWN_MASK 或旧的 _MASK 修饰符，
     *                  但不要在一个事件中混合使用这两种模型。
     *                  推荐使用扩展的修饰符
     * @param keyCode   实际键的整数代码，或 VK_UNDEFINED
     *                  （对于键入事件）
     * @param keyChar   由此事件生成的 Unicode 字符，或
     *                  CHAR_UNDEFINED（对于不映射到有效 Unicode 字符的
     *                  按键和释放事件）
     * @throws IllegalArgumentException  如果 <code>id</code> 为
     *     <code>KEY_TYPED</code> 且 <code>keyChar</code> 为
     *     <code>CHAR_UNDEFINED</code>；或者如果 <code>id</code> 为
     *     <code>KEY_TYPED</code> 且 <code>keyCode</code> 不为
     *     <code>VK_UNDEFINED</code>
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getSource()
     * @see #getID()
     * @see #getWhen()
     * @see #getModifiers()
     * @see #getKeyCode()
     * @see #getKeyChar()
     */
    public KeyEvent(Component source, int id, long when, int modifiers,
                    int keyCode, char keyChar) {
        this(source, id, when, modifiers, keyCode, keyChar,
          KEY_LOCATION_UNKNOWN);
    }

    /**
     * @deprecated 自 JDK1.1 起
     */
    @Deprecated
    public KeyEvent(Component source, int id, long when, int modifiers,
                    int keyCode) {
        this(source, id, when, modifiers, keyCode, (char)keyCode);
    }

    /**
     * 返回与此事件中的键关联的整数 keyCode。
     *
     * @return 键盘上实际键的整数代码。
     *         （对于 <code>KEY_TYPED</code> 事件，keyCode 为
     *         <code>VK_UNDEFINED</code>。）
     */
    public int getKeyCode() {
        return keyCode;
    }

    /**
     * 设置 keyCode 值以表示物理键。
     *
     * @param keyCode 与键盘上实际键对应的整数。
     */
    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    /**
     * 返回与此事件中的键关联的字符。
     * 例如，shift + "a" 的 <code>KEY_TYPED</code> 事件返回 "A" 的值。
     * <p>
     * <code>KEY_PRESSED</code> 和 <code>KEY_RELEASED</code> 事件
     * 不用于报告字符输入。因此，此方法返回的值
     * 仅对 <code>KEY_TYPED</code> 事件有意义。
     *
     * @return 此键事件定义的 Unicode 字符。
     *         如果此键事件没有有效的 Unicode 字符，
     *         则返回 <code>CHAR_UNDEFINED</code>。
     */
    public char getKeyChar() {
        return keyChar;
    }

    /**
     * 设置 keyChar 值以表示逻辑字符。
     *
     * @param keyChar 与组成此事件的按键组合对应的字符。
     */
    public void setKeyChar(char keyChar) {
        this.keyChar = keyChar;
    }

    /**
     * 设置修饰符以表示按下的附加键
     * （例如 shift, ctrl, alt, meta），这些修饰符定义为 InputEvent 的一部分。
     * <p>
     * 注意：不推荐使用此方法，因为许多 AWT
     * 实现不识别修饰符的变化。这尤其适用于 <code>KEY_TYPED</code> 事件，
     * 其中 shift 修饰符发生变化。
     *
     * @param modifiers 修饰符常量的整数组合。
     * @see InputEvent
     * @deprecated 自 JDK1.1.4 起
     */
    @Deprecated
    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
        if ((getModifiers() != 0) && (getModifiersEx() == 0)) {
            setNewModifiers();
        } else if ((getModifiers() == 0) && (getModifiersEx() != 0)) {
            setOldModifiers();
        }
    }

    /**
     * 返回此键事件的起源键的位置。
     *
     * 有些键在键盘上出现多次，例如左和右的Shift键。此外，有些键出现在数字键盘上。这提供了一种区分这些键的方法。
     *
     * @return 按下或释放的键的位置。
     *         对于 <code>KEY_TYPED</code> 事件，始终返回 <code>KEY_LOCATION_UNKNOWN</code>。
     * @since 1.4
     */
    public int getKeyLocation() {
        return keyLocation;
    }

    /**
     * 返回描述 keyCode 的字符串，例如 "HOME"、"F1" 或 "A"。
     * 这些字符串可以通过更改 awt.properties 文件来本地化。
     *
     * @return 包含物理键文本描述的字符串，由其 keyCode 标识
     */
    public static String getKeyText(int keyCode) {
        if (keyCode >= VK_0 && keyCode <= VK_9 ||
            keyCode >= VK_A && keyCode <= VK_Z) {
            return String.valueOf((char)keyCode);
        }

        switch(keyCode) {
          case VK_ENTER: return Toolkit.getProperty("AWT.enter", "Enter");
          case VK_BACK_SPACE: return Toolkit.getProperty("AWT.backSpace", "Backspace");
          case VK_TAB: return Toolkit.getProperty("AWT.tab", "Tab");
          case VK_CANCEL: return Toolkit.getProperty("AWT.cancel", "Cancel");
          case VK_CLEAR: return Toolkit.getProperty("AWT.clear", "Clear");
          case VK_COMPOSE: return Toolkit.getProperty("AWT.compose", "Compose");
          case VK_PAUSE: return Toolkit.getProperty("AWT.pause", "Pause");
          case VK_CAPS_LOCK: return Toolkit.getProperty("AWT.capsLock", "Caps Lock");
          case VK_ESCAPE: return Toolkit.getProperty("AWT.escape", "Escape");
          case VK_SPACE: return Toolkit.getProperty("AWT.space", "Space");
          case VK_PAGE_UP: return Toolkit.getProperty("AWT.pgup", "Page Up");
          case VK_PAGE_DOWN: return Toolkit.getProperty("AWT.pgdn", "Page Down");
          case VK_END: return Toolkit.getProperty("AWT.end", "End");
          case VK_HOME: return Toolkit.getProperty("AWT.home", "Home");
          case VK_LEFT: return Toolkit.getProperty("AWT.left", "Left");
          case VK_UP: return Toolkit.getProperty("AWT.up", "Up");
          case VK_RIGHT: return Toolkit.getProperty("AWT.right", "Right");
          case VK_DOWN: return Toolkit.getProperty("AWT.down", "Down");
          case VK_BEGIN: return Toolkit.getProperty("AWT.begin", "Begin");

          // 修饰符
          case VK_SHIFT: return Toolkit.getProperty("AWT.shift", "Shift");
          case VK_CONTROL: return Toolkit.getProperty("AWT.control", "Control");
          case VK_ALT: return Toolkit.getProperty("AWT.alt", "Alt");
          case VK_META: return Toolkit.getProperty("AWT.meta", "Meta");
          case VK_ALT_GRAPH: return Toolkit.getProperty("AWT.altGraph", "Alt Graph");

          // 标点符号
          case VK_COMMA: return Toolkit.getProperty("AWT.comma", "Comma");
          case VK_PERIOD: return Toolkit.getProperty("AWT.period", "Period");
          case VK_SLASH: return Toolkit.getProperty("AWT.slash", "Slash");
          case VK_SEMICOLON: return Toolkit.getProperty("AWT.semicolon", "Semicolon");
          case VK_EQUALS: return Toolkit.getProperty("AWT.equals", "Equals");
          case VK_OPEN_BRACKET: return Toolkit.getProperty("AWT.openBracket", "Open Bracket");
          case VK_BACK_SLASH: return Toolkit.getProperty("AWT.backSlash", "Back Slash");
          case VK_CLOSE_BRACKET: return Toolkit.getProperty("AWT.closeBracket", "Close Bracket");

          // 数字键盘上的数字键在下面处理
          case VK_MULTIPLY: return Toolkit.getProperty("AWT.multiply", "NumPad *");
          case VK_ADD: return Toolkit.getProperty("AWT.add", "NumPad +");
          case VK_SEPARATOR: return Toolkit.getProperty("AWT.separator", "NumPad ,");
          case VK_SUBTRACT: return Toolkit.getProperty("AWT.subtract", "NumPad -");
          case VK_DECIMAL: return Toolkit.getProperty("AWT.decimal", "NumPad .");
          case VK_DIVIDE: return Toolkit.getProperty("AWT.divide", "NumPad /");
          case VK_DELETE: return Toolkit.getProperty("AWT.delete", "Delete");
          case VK_NUM_LOCK: return Toolkit.getProperty("AWT.numLock", "Num Lock");
          case VK_SCROLL_LOCK: return Toolkit.getProperty("AWT.scrollLock", "Scroll Lock");


                      case VK_WINDOWS: return Toolkit.getProperty("AWT.windows", "Windows");
          case VK_CONTEXT_MENU: return Toolkit.getProperty("AWT.context", "上下文菜单");

          case VK_F1: return Toolkit.getProperty("AWT.f1", "F1");
          case VK_F2: return Toolkit.getProperty("AWT.f2", "F2");
          case VK_F3: return Toolkit.getProperty("AWT.f3", "F3");
          case VK_F4: return Toolkit.getProperty("AWT.f4", "F4");
          case VK_F5: return Toolkit.getProperty("AWT.f5", "F5");
          case VK_F6: return Toolkit.getProperty("AWT.f6", "F6");
          case VK_F7: return Toolkit.getProperty("AWT.f7", "F7");
          case VK_F8: return Toolkit.getProperty("AWT.f8", "F8");
          case VK_F9: return Toolkit.getProperty("AWT.f9", "F9");
          case VK_F10: return Toolkit.getProperty("AWT.f10", "F10");
          case VK_F11: return Toolkit.getProperty("AWT.f11", "F11");
          case VK_F12: return Toolkit.getProperty("AWT.f12", "F12");
          case VK_F13: return Toolkit.getProperty("AWT.f13", "F13");
          case VK_F14: return Toolkit.getProperty("AWT.f14", "F14");
          case VK_F15: return Toolkit.getProperty("AWT.f15", "F15");
          case VK_F16: return Toolkit.getProperty("AWT.f16", "F16");
          case VK_F17: return Toolkit.getProperty("AWT.f17", "F17");
          case VK_F18: return Toolkit.getProperty("AWT.f18", "F18");
          case VK_F19: return Toolkit.getProperty("AWT.f19", "F19");
          case VK_F20: return Toolkit.getProperty("AWT.f20", "F20");
          case VK_F21: return Toolkit.getProperty("AWT.f21", "F21");
          case VK_F22: return Toolkit.getProperty("AWT.f22", "F22");
          case VK_F23: return Toolkit.getProperty("AWT.f23", "F23");
          case VK_F24: return Toolkit.getProperty("AWT.f24", "F24");

          case VK_PRINTSCREEN: return Toolkit.getProperty("AWT.printScreen", "打印屏幕");
          case VK_INSERT: return Toolkit.getProperty("AWT.insert", "插入");
          case VK_HELP: return Toolkit.getProperty("AWT.help", "帮助");
          case VK_BACK_QUOTE: return Toolkit.getProperty("AWT.backQuote", "反引号");
          case VK_QUOTE: return Toolkit.getProperty("AWT.quote", "引号");

          case VK_KP_UP: return Toolkit.getProperty("AWT.up", "上");
          case VK_KP_DOWN: return Toolkit.getProperty("AWT.down", "下");
          case VK_KP_LEFT: return Toolkit.getProperty("AWT.left", "左");
          case VK_KP_RIGHT: return Toolkit.getProperty("AWT.right", "右");

          case VK_DEAD_GRAVE: return Toolkit.getProperty("AWT.deadGrave", "重音符");
          case VK_DEAD_ACUTE: return Toolkit.getProperty("AWT.deadAcute", "尖音符");
          case VK_DEAD_CIRCUMFLEX: return Toolkit.getProperty("AWT.deadCircumflex", "环音符");
          case VK_DEAD_TILDE: return Toolkit.getProperty("AWT.deadTilde", "波浪线");
          case VK_DEAD_MACRON: return Toolkit.getProperty("AWT.deadMacron", "长音符");
          case VK_DEAD_BREVE: return Toolkit.getProperty("AWT.deadBreve", "短音符");
          case VK_DEAD_ABOVEDOT: return Toolkit.getProperty("AWT.deadAboveDot", "点音符");
          case VK_DEAD_DIAERESIS: return Toolkit.getProperty("AWT.deadDiaeresis", "分音符");
          case VK_DEAD_ABOVERING: return Toolkit.getProperty("AWT.deadAboveRing", "环音符");
          case VK_DEAD_DOUBLEACUTE: return Toolkit.getProperty("AWT.deadDoubleAcute", "双尖音符");
          case VK_DEAD_CARON: return Toolkit.getProperty("AWT.deadCaron", "尖音符");
          case VK_DEAD_CEDILLA: return Toolkit.getProperty("AWT.deadCedilla", "软音符");
          case VK_DEAD_OGONEK: return Toolkit.getProperty("AWT.deadOgonek", "钩音符");
          case VK_DEAD_IOTA: return Toolkit.getProperty("AWT.deadIota", "约塔音符");
          case VK_DEAD_VOICED_SOUND: return Toolkit.getProperty("AWT.deadVoicedSound", "浊音符");
          case VK_DEAD_SEMIVOICED_SOUND: return Toolkit.getProperty("AWT.deadSemivoicedSound", "半浊音符");

          case VK_AMPERSAND: return Toolkit.getProperty("AWT.ampersand", "和号");
          case VK_ASTERISK: return Toolkit.getProperty("AWT.asterisk", "星号");
          case VK_QUOTEDBL: return Toolkit.getProperty("AWT.quoteDbl", "双引号");
          case VK_LESS: return Toolkit.getProperty("AWT.Less", "小于号");
          case VK_GREATER: return Toolkit.getProperty("AWT.greater", "大于号");
          case VK_BRACELEFT: return Toolkit.getProperty("AWT.braceLeft", "左大括号");
          case VK_BRACERIGHT: return Toolkit.getProperty("AWT.braceRight", "右大括号");
          case VK_AT: return Toolkit.getProperty("AWT.at", "at符号");
          case VK_COLON: return Toolkit.getProperty("AWT.colon", "冒号");
          case VK_CIRCUMFLEX: return Toolkit.getProperty("AWT.circumflex", "环音符");
          case VK_DOLLAR: return Toolkit.getProperty("AWT.dollar", "美元符号");
          case VK_EURO_SIGN: return Toolkit.getProperty("AWT.euro", "欧元符号");
          case VK_EXCLAMATION_MARK: return Toolkit.getProperty("AWT.exclamationMark", "感叹号");
          case VK_INVERTED_EXCLAMATION_MARK: return Toolkit.getProperty("AWT.invertedExclamationMark", "倒感叹号");
          case VK_LEFT_PARENTHESIS: return Toolkit.getProperty("AWT.leftParenthesis", "左括号");
          case VK_NUMBER_SIGN: return Toolkit.getProperty("AWT.numberSign", "井号");
          case VK_MINUS: return Toolkit.getProperty("AWT.minus", "减号");
          case VK_PLUS: return Toolkit.getProperty("AWT.plus", "加号");
          case VK_RIGHT_PARENTHESIS: return Toolkit.getProperty("AWT.rightParenthesis", "右括号");
          case VK_UNDERSCORE: return Toolkit.getProperty("AWT.underscore", "下划线");

          case VK_FINAL: return Toolkit.getProperty("AWT.final", "最终");
          case VK_CONVERT: return Toolkit.getProperty("AWT.convert", "转换");
          case VK_NONCONVERT: return Toolkit.getProperty("AWT.noconvert", "不转换");
          case VK_ACCEPT: return Toolkit.getProperty("AWT.accept", "接受");
          case VK_MODECHANGE: return Toolkit.getProperty("AWT.modechange", "模式切换");
          case VK_KANA: return Toolkit.getProperty("AWT.kana", "假名");
          case VK_KANJI: return Toolkit.getProperty("AWT.kanji", "汉字");
          case VK_ALPHANUMERIC: return Toolkit.getProperty("AWT.alphanumeric", "字母数字");
          case VK_KATAKANA: return Toolkit.getProperty("AWT.katakana", "片假名");
          case VK_HIRAGANA: return Toolkit.getProperty("AWT.hiragana", "平假名");
          case VK_FULL_WIDTH: return Toolkit.getProperty("AWT.fullWidth", "全角");
          case VK_HALF_WIDTH: return Toolkit.getProperty("AWT.halfWidth", "半角");
          case VK_ROMAN_CHARACTERS: return Toolkit.getProperty("AWT.romanCharacters", "罗马字符");
          case VK_ALL_CANDIDATES: return Toolkit.getProperty("AWT.allCandidates", "所有候选");
          case VK_PREVIOUS_CANDIDATE: return Toolkit.getProperty("AWT.previousCandidate", "上一个候选");
          case VK_CODE_INPUT: return Toolkit.getProperty("AWT.codeInput", "代码输入");
          case VK_JAPANESE_KATAKANA: return Toolkit.getProperty("AWT.japaneseKatakana", "日语片假名");
          case VK_JAPANESE_HIRAGANA: return Toolkit.getProperty("AWT.japaneseHiragana", "日语平假名");
          case VK_JAPANESE_ROMAN: return Toolkit.getProperty("AWT.japaneseRoman", "日语罗马字");
          case VK_KANA_LOCK: return Toolkit.getProperty("AWT.kanaLock", "假名锁定");
          case VK_INPUT_METHOD_ON_OFF: return Toolkit.getProperty("AWT.inputMethodOnOff", "输入方法开关");

          case VK_AGAIN: return Toolkit.getProperty("AWT.again", "再次");
          case VK_UNDO: return Toolkit.getProperty("AWT.undo", "撤销");
          case VK_COPY: return Toolkit.getProperty("AWT.copy", "复制");
          case VK_PASTE: return Toolkit.getProperty("AWT.paste", "粘贴");
          case VK_CUT: return Toolkit.getProperty("AWT.cut", "剪切");
          case VK_FIND: return Toolkit.getProperty("AWT.find", "查找");
          case VK_PROPS: return Toolkit.getProperty("AWT.props", "属性");
          case VK_STOP: return Toolkit.getProperty("AWT.stop", "停止");
        }

        if (keyCode >= VK_NUMPAD0 && keyCode <= VK_NUMPAD9) {
            String numpad = Toolkit.getProperty("AWT.numpad", "数字键");
            char c = (char)(keyCode - VK_NUMPAD0 + '0');
            return numpad + "-" + c;
        }

        if ((keyCode & 0x01000000) != 0) {
            return String.valueOf((char)(keyCode ^ 0x01000000 ));
        }
        String unknown = Toolkit.getProperty("AWT.unknown", "未知");
        return unknown + " keyCode: 0x" + Integer.toString(keyCode, 16);
    }

    /**
     * 返回描述修饰键（如 "Shift" 或 "Ctrl+Shift"）的字符串。
     * 这些字符串可以通过更改 <code>awt.properties</code> 文件进行本地化。
     * <p>
     * 注意 <code>InputEvent.ALT_MASK</code> 和 <code>InputEvent.BUTTON2_MASK</code> 的值相同，
     * 因此对于这两个修饰键都返回字符串 "Alt"。同样地，
     * <code>InputEvent.META_MASK</code> 和 <code>InputEvent.BUTTON3_MASK</code> 的值相同，
     * 因此对于这两个修饰键都返回字符串 "Meta"。
     *
     * @return 一个描述事件期间按下的修饰键组合的文本
     * @see InputEvent#getModifiersExText(int)
     */
    public static String getKeyModifiersText(int modifiers) {
        StringBuilder buf = new StringBuilder();
        if ((modifiers & InputEvent.META_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.meta", "Meta"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.CTRL_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.control", "Ctrl"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.ALT_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.alt", "Alt"));
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
        if (buf.length() > 0) {
            buf.setLength(buf.length()-1); // 移除尾部的 '+'
        }
        return buf.toString();
    }


    /**
     * 返回此事件中的键是否为 "动作" 键。
     * 通常，动作键不会触发 Unicode 字符，也不是修饰键。
     *
     * @return 如果键是 "动作" 键，则返回 <code>true</code>，否则返回 <code>false</code>
     */
    public boolean isActionKey() {
        switch (keyCode) {
          case VK_HOME:
          case VK_END:
          case VK_PAGE_UP:
          case VK_PAGE_DOWN:
          case VK_UP:
          case VK_DOWN:
          case VK_LEFT:
          case VK_RIGHT:
          case VK_BEGIN:

          case VK_KP_LEFT:
          case VK_KP_UP:
          case VK_KP_RIGHT:
          case VK_KP_DOWN:

          case VK_F1:
          case VK_F2:
          case VK_F3:
          case VK_F4:
          case VK_F5:
          case VK_F6:
          case VK_F7:
          case VK_F8:
          case VK_F9:
          case VK_F10:
          case VK_F11:
          case VK_F12:
          case VK_F13:
          case VK_F14:
          case VK_F15:
          case VK_F16:
          case VK_F17:
          case VK_F18:
          case VK_F19:
          case VK_F20:
          case VK_F21:
          case VK_F22:
          case VK_F23:
          case VK_F24:
          case VK_PRINTSCREEN:
          case VK_SCROLL_LOCK:
          case VK_CAPS_LOCK:
          case VK_NUM_LOCK:
          case VK_PAUSE:
          case VK_INSERT:

          case VK_FINAL:
          case VK_CONVERT:
          case VK_NONCONVERT:
          case VK_ACCEPT:
          case VK_MODECHANGE:
          case VK_KANA:
          case VK_KANJI:
          case VK_ALPHANUMERIC:
          case VK_KATAKANA:
          case VK_HIRAGANA:
          case VK_FULL_WIDTH:
          case VK_HALF_WIDTH:
          case VK_ROMAN_CHARACTERS:
          case VK_ALL_CANDIDATES:
          case VK_PREVIOUS_CANDIDATE:
          case VK_CODE_INPUT:
          case VK_JAPANESE_KATAKANA:
          case VK_JAPANESE_HIRAGANA:
          case VK_JAPANESE_ROMAN:
          case VK_KANA_LOCK:
          case VK_INPUT_METHOD_ON_OFF:

          case VK_AGAIN:
          case VK_UNDO:
          case VK_COPY:
          case VK_PASTE:
          case VK_CUT:
          case VK_FIND:
          case VK_PROPS:
          case VK_STOP:

          case VK_HELP:
          case VK_WINDOWS:
          case VK_CONTEXT_MENU:
              return true;
        }
        return false;
    }

    /**
     * 返回一个标识此事件的参数字符串。
     * 此方法对于事件日志记录和调试非常有用。
     *
     * @return 一个标识事件及其属性的字符串
     */
    public String paramString() {
        StringBuilder str = new StringBuilder(100);

        switch (id) {
          case KEY_PRESSED:
            str.append("KEY_PRESSED");
            break;
          case KEY_RELEASED:
            str.append("KEY_RELEASED");
            break;
          case KEY_TYPED:
            str.append("KEY_TYPED");
            break;
          default:
            str.append("未知类型");
            break;
        }

        str.append(",keyCode=").append(keyCode);
        str.append(",keyText=").append(getKeyText(keyCode));

        /* 有些键字符打印效果不佳，例如：转义、退格、制表符、回车、删除、取消。使用 keyCode 的 keyText 而不是 keyChar。 */
        str.append(",keyChar=");
        switch (keyChar) {
          case '\b':
            str.append(getKeyText(VK_BACK_SPACE));
            break;
          case '\t':
            str.append(getKeyText(VK_TAB));
            break;
          case '\n':
            str.append(getKeyText(VK_ENTER));
            break;
          case '\u0018':
            str.append(getKeyText(VK_CANCEL));
            break;
          case '\u001b':
            str.append(getKeyText(VK_ESCAPE));
            break;
          case '\u007f':
            str.append(getKeyText(VK_DELETE));
            break;
          case CHAR_UNDEFINED:
            str.append(Toolkit.getProperty("AWT.undefined", "未定义"));
            str.append(" keyChar");
            break;
          default:
            str.append("'").append(keyChar).append("'");
            break;
        }

        if (getModifiers() != 0) {
            str.append(",modifiers=").append(getKeyModifiersText(modifiers));
        }
        if (getModifiersEx() != 0) {
            str.append(",extModifiers=").append(getModifiersExText(modifiers));
        }

        str.append(",keyLocation=");
        switch (keyLocation) {
          case KEY_LOCATION_UNKNOWN:
            str.append("KEY_LOCATION_UNKNOWN");
            break;
          case KEY_LOCATION_STANDARD:
            str.append("KEY_LOCATION_STANDARD");
            break;
          case KEY_LOCATION_LEFT:
            str.append("KEY_LOCATION_LEFT");
            break;
          case KEY_LOCATION_RIGHT:
            str.append("KEY_LOCATION_RIGHT");
            break;
          case KEY_LOCATION_NUMPAD:
            str.append("KEY_LOCATION_NUMPAD");
            break;
          default:
            str.append("KEY_LOCATION_UNKNOWN");
            break;
        }
        str.append(",rawCode=").append(rawCode);
        str.append(",primaryLevelUnicode=").append(primaryLevelUnicode);
        str.append(",scancode=").append(scancode);
        str.append(",extendedKeyCode=0x").append(Long.toHexString(extendedKeyCode));


                    return str.toString();
    }
    /**
     * 返回事件的扩展键代码。
     * 扩展键代码是分配给键盘上一个键的唯一ID
     * 就像 {@code keyCode} 一样。但是，与 {@code keyCode} 不同，此值取决于
     * 当前的键盘布局。例如，在常见的英文布局中按下左上角的字母键
     * 会产生与 {@code keyCode}，即 {@code VK_Q} 相同的值。
     * 在常规的俄文布局中按下相同的键会产生另一个代码，该代码对于
     * 字母 "Cyrillic I short" 是唯一的。
     *
     * @since 1.7
     *
     */
    public  int getExtendedKeyCode() {
        return (int)extendedKeyCode;
    }
    /**
     * 返回一个Unicode字符的扩展键代码。
     *
     * @return 对于具有相应 {@code VK_} 常量的Unicode字符，返回此
     *   {@code VK_} 常量；对于出现在已知键盘布局主层的字符，返回一个唯一的整数。
     *   如果字符不出现在已知键盘的主层，
     *   返回 {@code VK_UNDEFINED}。
     *
     * @since 1.7
     *
     */
    public static int getExtendedKeyCodeForChar(int c) {
        // 返回与字符关联的键代码（如果有）。
        return sun.awt.ExtendedKeyCodes.getExtendedKeyCodeForChar(c);
    }

    /**
     * 通过旧的修饰符设置新的修饰符。键修饰符
     * 覆盖重叠的鼠标修饰符。
     */
    private void setNewModifiers() {
        if ((modifiers & SHIFT_MASK) != 0) {
            modifiers |= SHIFT_DOWN_MASK;
        }
        if ((modifiers & ALT_MASK) != 0) {
            modifiers |= ALT_DOWN_MASK;
        }
        if ((modifiers & CTRL_MASK) != 0) {
            modifiers |= CTRL_DOWN_MASK;
        }
        if ((modifiers & META_MASK) != 0) {
            modifiers |= META_DOWN_MASK;
        }
        if ((modifiers & ALT_GRAPH_MASK) != 0) {
            modifiers |= ALT_GRAPH_DOWN_MASK;
        }
        if ((modifiers & BUTTON1_MASK) != 0) {
            modifiers |= BUTTON1_DOWN_MASK;
        }
    }

    /**
     * 通过新的修饰符设置旧的修饰符。
     */
    private void setOldModifiers() {
        if ((modifiers & SHIFT_DOWN_MASK) != 0) {
            modifiers |= SHIFT_MASK;
        }
        if ((modifiers & ALT_DOWN_MASK) != 0) {
            modifiers |= ALT_MASK;
        }
        if ((modifiers & CTRL_DOWN_MASK) != 0) {
            modifiers |= CTRL_MASK;
        }
        if ((modifiers & META_DOWN_MASK) != 0) {
            modifiers |= META_MASK;
        }
        if ((modifiers & ALT_GRAPH_DOWN_MASK) != 0) {
            modifiers |= ALT_GRAPH_MASK;
        }
        if ((modifiers & BUTTON1_DOWN_MASK) != 0) {
            modifiers |= BUTTON1_MASK;
        }
    }

    /**
     * 通过旧的修饰符设置新的修饰符。键修饰符
     * 覆盖重叠的鼠标修饰符。
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
