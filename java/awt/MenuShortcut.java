/*
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.event.KeyEvent;

/**
 * <code>MenuShortcut</code> 类表示一个菜单项的键盘加速器。
 * <p>
 * 菜单快捷键是使用虚拟键码创建的，而不是字符。
 * 例如，创建一个 Ctrl-a 的菜单快捷键（假设 Control 是加速键）可以使用以下代码：
 * <p>
 * <code>MenuShortcut ms = new MenuShortcut(KeyEvent.VK_A, false);</code>
 * <p> 或者
 * <p>
 * <code>MenuShortcut ms = new MenuShortcut(KeyEvent.getExtendedKeyCodeForChar('A'), false);</code>
 * <p>
 * 菜单快捷键也可以使用 <code>java.awt.event.KeyEvent.getExtendedKeyCodeForChar</code> 调用来创建更广泛的键码。
 * 例如，创建一个 "Ctrl+cyrillic ef" 的菜单快捷键可以使用：
 * <p>
 * <code>MenuShortcut ms = new MenuShortcut(KeyEvent.getExtendedKeyCodeForChar('\u0444'), false);</code>
 * <p>
 * 注意，使用在 <code>KeyEvent</code> 中定义的键码或扩展键码创建的快捷键无论当前键盘布局如何都能工作。
 * 但是，使用未在 <code>KeyEvent</code> 中列出的扩展键码创建的快捷键只有在当前键盘布局产生相应字母时才能工作。
 * <p>
 * 加速键是平台相关的，可以通过 {@link Toolkit#getMenuShortcutKeyMask} 获取。
 *
 * @author Thomas Ball
 * @since JDK1.1
 */
public class MenuShortcut implements java.io.Serializable
{
    /**
     * 菜单快捷键的虚拟键码。
     * 这是创建菜单快捷键时使用的键码。
     * 注意，这是一个虚拟键码，而不是字符，例如 KeyEvent.VK_A，而不是 'a'。
     * 注意：在 1.1.x 中，必须在菜单项上调用 setActionCommand() 以便其快捷键生效，否则它将触发一个空的
     * 动作命令。
     *
     * @serial
     * @see #getKey()
     * @see #usesShiftModifier()
     * @see java.awt.event.KeyEvent
     * @since JDK1.1
     */
    int key;

    /**
     * 指示是否按下了 Shift 键。
     * 如果为 true，表示按下了 Shift 键。
     * 如果为 false，表示未按下 Shift 键。
     *
     * @serial
     * @see #usesShiftModifier()
     * @since JDK1.1
     */
    boolean usesShift;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = 143448358473180225L;

    /**
     * 构造一个新的 MenuShortcut，使用指定的虚拟键码。
     * @param key 这个 MenuShortcut 的原始键码，如果按下该键，将在 {@link java.awt.event.KeyEvent KeyEvent} 的 keyCode 字段中返回。
     * @see java.awt.event.KeyEvent
     **/
    public MenuShortcut(int key) {
        this(key, false);
    }

    /**
     * 构造一个新的 MenuShortcut，使用指定的虚拟键码。
     * @param key 这个 MenuShortcut 的原始键码，如果按下该键，将在 {@link java.awt.event.KeyEvent KeyEvent} 的 keyCode 字段中返回。
     * @param useShiftModifier 指示这个 MenuShortcut 是否在按下 SHIFT 键时被调用。
     * @see java.awt.event.KeyEvent
     **/
    public MenuShortcut(int key, boolean useShiftModifier) {
        this.key = key;
        this.usesShift = useShiftModifier;
    }

    /**
     * 返回这个 MenuShortcut 的原始键码。
     * @return 这个 MenuShortcut 的原始键码。
     * @see java.awt.event.KeyEvent
     * @since JDK1.1
     */
    public int getKey() {
        return key;
    }

    /**
     * 返回这个 MenuShortcut 是否必须使用 SHIFT 键调用。
     * @return 如果这个 MenuShortcut 必须使用 SHIFT 键调用，返回 <code>true</code>，否则返回 <code>false</code>。
     * @since JDK1.1
     */
    public boolean usesShiftModifier() {
        return usesShift;
    }

    /**
     * 返回这个 MenuShortcut 是否与另一个相同：
     * 相等的定义是两个 MenuShortcuts 使用相同的键，并且都使用或不使用 SHIFT 键。
     * @param s 要与之比较的 MenuShortcut。
     * @return 如果这个 MenuShortcut 与另一个相同，返回 <code>true</code>，否则返回 <code>false</code>。
     * @since JDK1.1
     */
    public boolean equals(MenuShortcut s) {
        return (s != null && (s.getKey() == key) &&
                (s.usesShiftModifier() == usesShift));
    }

    /**
     * 返回这个 MenuShortcut 是否与另一个相同：
     * 相等的定义是两个 MenuShortcuts 使用相同的键，并且都使用或不使用 SHIFT 键。
     * @param obj 要与之比较的对象。
     * @return 如果这个 MenuShortcut 与另一个相同，返回 <code>true</code>，否则返回 <code>false</code>。
     * @since 1.2
     */
    public boolean equals(Object obj) {
        if (obj instanceof MenuShortcut) {
            return equals( (MenuShortcut) obj );
        }
        return false;
    }

    /**
     * 返回这个 MenuShortcut 的哈希码。
     * @return 这个 MenuShortcut 的哈希码。
     * @since 1.2
     */
    public int hashCode() {
        return (usesShift) ? (~key) : key;
    }

    /**
     * 返回这个 MenuShortcut 的国际化描述。
     * @return 这个 MenuShortcut 的字符串表示。
     * @since JDK1.1
     */
    public String toString() {
        int modifiers = 0;
        if (!GraphicsEnvironment.isHeadless()) {
            modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        }
        if (usesShiftModifier()) {
            modifiers |= Event.SHIFT_MASK;
        }
        return KeyEvent.getKeyModifiersText(modifiers) + "+" +
               KeyEvent.getKeyText(key);
    }

    /**
     * 返回表示这个 MenuShortcut 状态的参数字符串。这个字符串对于调试很有用。
     * @return 这个 MenuShortcut 的参数字符串。
     * @since JDK1.1
     */
    protected String paramString() {
        String str = "key=" + key;
        if (usesShiftModifier()) {
            str += ",usesShiftModifier";
        }
        return str;
    }
}
