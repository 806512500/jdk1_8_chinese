
/*
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
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

import sun.awt.AWTAccessor;

import java.io.ObjectStreamException;

import java.lang.annotation.Native;

/**
 * 一个类，用于封装表示系统中本机 GUI 对象颜色的符号颜色。对于支持动态更新系统颜色（当用户更改颜色时）
 * 的系统，这些符号颜色的实际 RGB 值也会动态更改。为了比较 <code>SystemColor</code> 对象的“当前”RGB 值
 * 与非符号颜色对象，应使用 <code>getRGB</code> 而不是 <code>equals</code>。
 * <p>
 * 请注意，这些系统颜色应用于 GUI 对象的方式可能会因平台而异，因为每个平台上的 GUI 对象可能呈现方式不同。
 * <p>
 * 系统颜色值也可以通过 <code>java.awt.Toolkit</code> 的 <code>getDesktopProperty</code> 方法获得。
 *
 * @see Toolkit#getDesktopProperty
 *
 * @author      Carl Quinn
 * @author      Amy Fowler
 */
public final class SystemColor extends Color implements java.io.Serializable {

   /**
     * {@link #desktop} 系统颜色的数组索引。
     * @see SystemColor#desktop
     */
    @Native public final static int DESKTOP = 0;

    /**
     * {@link #activeCaption} 系统颜色的数组索引。
     * @see SystemColor#activeCaption
     */
    @Native public final static int ACTIVE_CAPTION = 1;

    /**
     * {@link #activeCaptionText} 系统颜色的数组索引。
     * @see SystemColor#activeCaptionText
     */
    @Native public final static int ACTIVE_CAPTION_TEXT = 2;

    /**
     * {@link #activeCaptionBorder} 系统颜色的数组索引。
     * @see SystemColor#activeCaptionBorder
     */
    @Native public final static int ACTIVE_CAPTION_BORDER = 3;

    /**
     * {@link #inactiveCaption} 系统颜色的数组索引。
     * @see SystemColor#inactiveCaption
     */
    @Native public final static int INACTIVE_CAPTION = 4;

    /**
     * {@link #inactiveCaptionText} 系统颜色的数组索引。
     * @see SystemColor#inactiveCaptionText
     */
    @Native public final static int INACTIVE_CAPTION_TEXT = 5;

    /**
     * {@link #inactiveCaptionBorder} 系统颜色的数组索引。
     * @see SystemColor#inactiveCaptionBorder
     */
    @Native public final static int INACTIVE_CAPTION_BORDER = 6;

    /**
     * {@link #window} 系统颜色的数组索引。
     * @see SystemColor#window
     */
    @Native public final static int WINDOW = 7;

    /**
     * {@link #windowBorder} 系统颜色的数组索引。
     * @see SystemColor#windowBorder
     */
    @Native public final static int WINDOW_BORDER = 8;

    /**
     * {@link #windowText} 系统颜色的数组索引。
     * @see SystemColor#windowText
     */
    @Native public final static int WINDOW_TEXT = 9;

    /**
     * {@link #menu} 系统颜色的数组索引。
     * @see SystemColor#menu
     */
    @Native public final static int MENU = 10;

    /**
     * {@link #menuText} 系统颜色的数组索引。
     * @see SystemColor#menuText
     */
    @Native public final static int MENU_TEXT = 11;

    /**
     * {@link #text} 系统颜色的数组索引。
     * @see SystemColor#text
     */
    @Native public final static int TEXT = 12;

    /**
     * {@link #textText} 系统颜色的数组索引。
     * @see SystemColor#textText
     */
    @Native public final static int TEXT_TEXT = 13;

    /**
     * {@link #textHighlight} 系统颜色的数组索引。
     * @see SystemColor#textHighlight
     */
    @Native public final static int TEXT_HIGHLIGHT = 14;

    /**
     * {@link #textHighlightText} 系统颜色的数组索引。
     * @see SystemColor#textHighlightText
     */
    @Native public final static int TEXT_HIGHLIGHT_TEXT = 15;

    /**
     * {@link #textInactiveText} 系统颜色的数组索引。
     * @see SystemColor#textInactiveText
     */
    @Native public final static int TEXT_INACTIVE_TEXT = 16;

    /**
     * {@link #control} 系统颜色的数组索引。
     * @see SystemColor#control
     */
    @Native public final static int CONTROL = 17;

    /**
     * {@link #controlText} 系统颜色的数组索引。
     * @see SystemColor#controlText
     */
    @Native public final static int CONTROL_TEXT = 18;

    /**
     * {@link #controlHighlight} 系统颜色的数组索引。
     * @see SystemColor#controlHighlight
     */
    @Native public final static int CONTROL_HIGHLIGHT = 19;

    /**
     * {@link #controlLtHighlight} 系统颜色的数组索引。
     * @see SystemColor#controlLtHighlight
     */
    @Native public final static int CONTROL_LT_HIGHLIGHT = 20;

    /**
     * {@link #controlShadow} 系统颜色的数组索引。
     * @see SystemColor#controlShadow
     */
    @Native public final static int CONTROL_SHADOW = 21;

    /**
     * {@link #controlDkShadow} 系统颜色的数组索引。
     * @see SystemColor#controlDkShadow
     */
    @Native public final static int CONTROL_DK_SHADOW = 22;

    /**
     * {@link #scrollbar} 系统颜色的数组索引。
     * @see SystemColor#scrollbar
     */
    @Native public final static int SCROLLBAR = 23;

    /**
     * {@link #info} 系统颜色的数组索引。
     * @see SystemColor#info
     */
    @Native public final static int INFO = 24;

    /**
     * {@link #infoText} 系统颜色的数组索引。
     * @see SystemColor#infoText
     */
    @Native public final static int INFO_TEXT = 25;

    /**
     * 系统颜色数组中的颜色数量。
     */
    @Native public final static int NUM_COLORS = 26;

    /******************************************************************************************/

    /*
     * 系统颜色的默认初始值，如果系统值不同且可用，则由工具包覆盖。
     * 应将数组初始化放在第一个使用 SystemColor 构造函数初始化的字段之前。
     */
    private static int[] systemColors = {
        0xFF005C5C,  // desktop = new Color(0,92,92);
        0xFF000080,  // activeCaption = new Color(0,0,128);
        0xFFFFFFFF,  // activeCaptionText = Color.white;
        0xFFC0C0C0,  // activeCaptionBorder = Color.lightGray;
        0xFF808080,  // inactiveCaption = Color.gray;
        0xFFC0C0C0,  // inactiveCaptionText = Color.lightGray;
        0xFFC0C0C0,  // inactiveCaptionBorder = Color.lightGray;
        0xFFFFFFFF,  // window = Color.white;
        0xFF000000,  // windowBorder = Color.black;
        0xFF000000,  // windowText = Color.black;
        0xFFC0C0C0,  // menu = Color.lightGray;
        0xFF000000,  // menuText = Color.black;
        0xFFC0C0C0,  // text = Color.lightGray;
        0xFF000000,  // textText = Color.black;
        0xFF000080,  // textHighlight = new Color(0,0,128);
        0xFFFFFFFF,  // textHighlightText = Color.white;
        0xFF808080,  // textInactiveText = Color.gray;
        0xFFC0C0C0,  // control = Color.lightGray;
        0xFF000000,  // controlText = Color.black;
        0xFFFFFFFF,  // controlHighlight = Color.white;
        0xFFE0E0E0,  // controlLtHighlight = new Color(224,224,224);
        0xFF808080,  // controlShadow = Color.gray;
        0xFF000000,  // controlDkShadow = Color.black;
        0xFFE0E0E0,  // scrollbar = new Color(224,224,224);
        0xFFE0E000,  // info = new Color(224,224,0);
        0xFF000000,  // infoText = Color.black;
    };

   /**
     * 桌面背景的颜色。
     */
    public final static SystemColor desktop = new SystemColor((byte)DESKTOP);

    /**
     * 当前激活窗口的窗口标题背景颜色。
     */
    public final static SystemColor activeCaption = new SystemColor((byte)ACTIVE_CAPTION);

    /**
     * 当前激活窗口的窗口标题文本颜色。
     */
    public final static SystemColor activeCaptionText = new SystemColor((byte)ACTIVE_CAPTION_TEXT);

    /**
     * 当前激活窗口周围的边框颜色。
     */
    public final static SystemColor activeCaptionBorder = new SystemColor((byte)ACTIVE_CAPTION_BORDER);

    /**
     * 非激活窗口的窗口标题背景颜色。
     */
    public final static SystemColor inactiveCaption = new SystemColor((byte)INACTIVE_CAPTION);

    /**
     * 非激活窗口的窗口标题文本颜色。
     */
    public final static SystemColor inactiveCaptionText = new SystemColor((byte)INACTIVE_CAPTION_TEXT);

    /**
     * 非激活窗口周围的边框颜色。
     */
    public final static SystemColor inactiveCaptionBorder = new SystemColor((byte)INACTIVE_CAPTION_BORDER);

    /**
     * 窗口内部区域背景的颜色。
     */
    public final static SystemColor window = new SystemColor((byte)WINDOW);

    /**
     * 窗口内部区域边框的颜色。
     */
    public final static SystemColor windowBorder = new SystemColor((byte)WINDOW_BORDER);

    /**
     * 窗口内部区域文本的颜色。
     */
    public final static SystemColor windowText = new SystemColor((byte)WINDOW_TEXT);

    /**
     * 菜单背景的颜色。
     */
    public final static SystemColor menu = new SystemColor((byte)MENU);

    /**
     * 菜单文本的颜色。
     */
    public final static SystemColor menuText = new SystemColor((byte)MENU_TEXT);

    /**
     * 文本控件对象（如文本框和组合框）背景的颜色。
     */
    public final static SystemColor text = new SystemColor((byte)TEXT);

    /**
     * 文本控件对象（如文本框和组合框）文本的颜色。
     */
    public final static SystemColor textText = new SystemColor((byte)TEXT_TEXT);

    /**
     * 选中项（如菜单、组合框和文本）背景的颜色。
     */
    public final static SystemColor textHighlight = new SystemColor((byte)TEXT_HIGHLIGHT);

    /**
     * 选中项（如菜单、组合框和文本）文本的颜色。
     */
    public final static SystemColor textHighlightText = new SystemColor((byte)TEXT_HIGHLIGHT_TEXT);

    /**
     * 非活动项（如菜单）文本的颜色。
     */
    public final static SystemColor textInactiveText = new SystemColor((byte)TEXT_INACTIVE_TEXT);

    /**
     * 控制面板和控件对象（如按钮）背景的颜色。
     */
    public final static SystemColor control = new SystemColor((byte)CONTROL);

    /**
     * 控制面板和控件对象（如按钮）文本的颜色。
     */
    public final static SystemColor controlText = new SystemColor((byte)CONTROL_TEXT);

    /**
     * 3D 控件对象（如按钮）的浅色区域的颜色。此颜色通常从 <code>control</code> 背景颜色派生，以提供 3D 效果。
     */
    public final static SystemColor controlHighlight = new SystemColor((byte)CONTROL_HIGHLIGHT);

    /**
     * 3D 控件对象（如按钮）的高亮区域的颜色。此颜色通常从 <code>control</code> 背景颜色派生，以提供 3D 效果。
     */
    public final static SystemColor controlLtHighlight = new SystemColor((byte)CONTROL_LT_HIGHLIGHT);

    /**
     * 3D 控件对象（如按钮）的阴影区域的颜色。此颜色通常从 <code>control</code> 背景颜色派生，以提供 3D 效果。
     */
    public final static SystemColor controlShadow = new SystemColor((byte)CONTROL_SHADOW);

    /**
     * 3D 控件对象（如按钮）的深阴影区域的颜色。此颜色通常从 <code>control</code> 背景颜色派生，以提供 3D 效果。
     */
    public final static SystemColor controlDkShadow = new SystemColor((byte)CONTROL_DK_SHADOW);

    /**
     * 滚动条背景的颜色。
     */
    public final static SystemColor scrollbar = new SystemColor((byte)SCROLLBAR);

    /**
     * 提示信息或即时帮助背景的颜色。
     */
    public final static SystemColor info = new SystemColor((byte)INFO);

    /**
     * 提示信息或即时帮助文本的颜色。
     */
    public final static SystemColor infoText = new SystemColor((byte)INFO_TEXT);

    /*
     * JDK 1.1 序列化版本 UID。
     */
    private static final long serialVersionUID = 4503142729533789064L;

    /*
     * SystemColor 对象或值数组的索引。
     */
    private transient int index;

    private static SystemColor systemColorObjects [] = {
        SystemColor.desktop,
        SystemColor.activeCaption,
        SystemColor.activeCaptionText,
        SystemColor.activeCaptionBorder,
        SystemColor.inactiveCaption,
        SystemColor.inactiveCaptionText,
        SystemColor.inactiveCaptionBorder,
        SystemColor.window,
        SystemColor.windowBorder,
        SystemColor.windowText,
        SystemColor.menu,
        SystemColor.menuText,
        SystemColor.text,
        SystemColor.textText,
        SystemColor.textHighlight,
        SystemColor.textHighlightText,
        SystemColor.textInactiveText,
        SystemColor.control,
        SystemColor.controlText,
        SystemColor.controlHighlight,
        SystemColor.controlLtHighlight,
        SystemColor.controlShadow,
        SystemColor.controlDkShadow,
        SystemColor.scrollbar,
        SystemColor.info,
        SystemColor.infoText
    };


                static {
        AWTAccessor.setSystemColorAccessor(SystemColor::updateSystemColors);
        updateSystemColors();
    }

    /**
     * 从 {@code <init>} 和工具包调用以更新上述 systemColors 缓存。
     */
    private static void updateSystemColors() {
        if (!GraphicsEnvironment.isHeadless()) {
            Toolkit.getDefaultToolkit().loadSystemColors(systemColors);
        }
        for (int i = 0; i < systemColors.length; i++) {
            systemColorObjects[i].value = systemColors[i];
        }
    }

    /**
     * 创建一个表示系统颜色缓存中索引条目的符号颜色。用于上述静态系统颜色。
     */
    private SystemColor(byte index) {
        super(systemColors[index]);
        this.index = index;
    }

    /**
     * 返回此 <code>Color</code> 值的字符串表示形式。
     * 此方法仅用于调试目的，返回的字符串的内容和格式可能因实现而异。
     * 返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return  此 <code>Color</code> 的字符串表示形式
     */
    public String toString() {
        return getClass().getName() + "[i=" + (index) + "]";
    }

    /**
     * {@code SystemColor} 类的设计假设存储在上述静态最终字段中的
     * {@code SystemColor} 对象实例是开发人员可以使用的唯一实例。
     * 此方法通过使用对象序列化形式的值字段中存储的索引来替换序列化的
     * 对象，以替换为 {@code SystemColor} 的等效静态对象常量字段。
     * 有关这些对象序列化形式的更多信息，请参阅 {@link #writeReplace} 方法。
     * @return 指向相同系统颜色的 {@code SystemColor} 静态对象字段之一。
     */
    private Object readResolve() {
        // SystemColor 的实例受到严格控制，只有上述静态
        // 常量中出现的规范实例是允许的。SystemColor
        // 对象的序列化形式存储颜色索引作为值。在这里我们
        // 将该索引映射回规范实例。
        return systemColorObjects[value];
    }

    /**
     * 返回一个用于写入序列化流的 {@code SystemColor} 对象的专用版本。
     * @serialData
     * 序列化的 {@code SystemColor} 对象的值字段包含系统颜色的数组索引，而不是
     * 系统颜色的 rgb 数据。
     * 该索引由 {@link #readResolve} 方法使用，以将反序列化的对象解析回原始
     * 静态常量版本，以确保每个 {@code SystemColor} 对象的唯一实例。
     * @return 一个代理 {@code SystemColor} 对象，其值被替换为相应的系统颜色索引。
     */
    private Object writeReplace() throws ObjectStreamException
    {
        // 在序列化时，我们将数组索引放入 SystemColor.value 以保持兼容性。
        SystemColor color = new SystemColor((byte)index);
        color.value = index;
        return color;
    }
}
