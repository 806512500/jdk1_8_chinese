
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.event.*;
import java.io.*;

/**
 * <b>注意：</b> <code>Event</code> 类已过时，仅为了向后兼容而提供。它已被 <code>AWTEvent</code> 类及其子类所取代。
 * <p>
 * <code>Event</code> 是一个平台独立的类，封装了平台图形用户界面在 Java 1.0 事件模型中的事件。在 Java 1.1 及更高版本中，
 * <code>Event</code> 类仅为了向后兼容而维护。此类描述中的信息旨在帮助程序员将 Java 1.0 程序转换为新的事件模型。
 * <p>
 * 在 Java 1.0 事件模型中，事件包含一个 {@link Event#id} 字段，表示事件的类型以及事件中相关的其他 <code>Event</code> 变量。
 * <p>
 * 对于键盘事件，{@link Event#key} 包含一个值，表示激活的键，而 {@link Event#modifiers} 包含该事件的修饰符。
 * 对于 KEY_PRESS 和 KEY_RELEASE 事件 ID，<code>key</code> 的值是键的 Unicode 字符代码。对于 KEY_ACTION 和
 * KEY_ACTION_RELEASE，<code>key</code> 的值是 <code>Event</code> 类中定义的动作键标识符之一（<code>PGUP</code>、
 * <code>PGDN</code>、<code>F1</code>、<code>F2</code> 等）。
 *
 * @author     Sami Shaio
 * @since      JDK1.0
 */
public class Event implements java.io.Serializable {
    private transient long data;

    /* 修饰符常量 */

    /**
     * 此标志表示事件发生时 Shift 键被按下。
     */
    public static final int SHIFT_MASK          = 1 << 0;

    /**
     * 此标志表示事件发生时 Control 键被按下。
     */
    public static final int CTRL_MASK           = 1 << 1;

    /**
     * 此标志表示事件发生时 Meta 键被按下。对于鼠标事件，此标志表示右键被按下或释放。
     */
    public static final int META_MASK           = 1 << 2;

    /**
     * 此标志表示事件发生时 Alt 键被按下。对于鼠标事件，此标志表示中键被按下或释放。
     */
    public static final int ALT_MASK            = 1 << 3;

    /* 动作键 */

    /**
     * Home 键，一个非 ASCII 动作键。
     */
    public static final int HOME                = 1000;

    /**
     * End 键，一个非 ASCII 动作键。
     */
    public static final int END                 = 1001;

    /**
     * Page Up 键，一个非 ASCII 动作键。
     */
    public static final int PGUP                = 1002;

    /**
     * Page Down 键，一个非 ASCII 动作键。
     */
    public static final int PGDN                = 1003;

    /**
     * 上箭头键，一个非 ASCII 动作键。
     */
    public static final int UP                  = 1004;

    /**
     * 下箭头键，一个非 ASCII 动作键。
     */
    public static final int DOWN                = 1005;

    /**
     * 左箭头键，一个非 ASCII 动作键。
     */
    public static final int LEFT                = 1006;

    /**
     * 右箭头键，一个非 ASCII 动作键。
     */
    public static final int RIGHT               = 1007;

    /**
     * F1 功能键，一个非 ASCII 动作键。
     */
    public static final int F1                  = 1008;

    /**
     * F2 功能键，一个非 ASCII 动作键。
     */
    public static final int F2                  = 1009;

    /**
     * F3 功能键，一个非 ASCII 动作键。
     */
    public static final int F3                  = 1010;

    /**
     * F4 功能键，一个非 ASCII 动作键。
     */
    public static final int F4                  = 1011;

    /**
     * F5 功能键，一个非 ASCII 动作键。
     */
    public static final int F5                  = 1012;

    /**
     * F6 功能键，一个非 ASCII 动作键。
     */
    public static final int F6                  = 1013;

    /**
     * F7 功能键，一个非 ASCII 动作键。
     */
    public static final int F7                  = 1014;

    /**
     * F8 功能键，一个非 ASCII 动作键。
     */
    public static final int F8                  = 1015;

    /**
     * F9 功能键，一个非 ASCII 动作键。
     */
    public static final int F9                  = 1016;

    /**
     * F10 功能键，一个非 ASCII 动作键。
     */
    public static final int F10                 = 1017;

    /**
     * F11 功能键，一个非 ASCII 动作键。
     */
    public static final int F11                 = 1018;

    /**
     * F12 功能键，一个非 ASCII 动作键。
     */
    public static final int F12                 = 1019;

    /**
     * Print Screen 键，一个非 ASCII 动作键。
     */
    public static final int PRINT_SCREEN        = 1020;

    /**
     * Scroll Lock 键，一个非 ASCII 动作键。
     */
    public static final int SCROLL_LOCK         = 1021;

    /**
     * Caps Lock 键，一个非 ASCII 动作键。
     */
    public static final int CAPS_LOCK           = 1022;

    /**
     * Num Lock 键，一个非 ASCII 动作键。
     */
    public static final int NUM_LOCK            = 1023;

    /**
     * Pause 键，一个非 ASCII 动作键。
     */
    public static final int PAUSE               = 1024;

    /**
     * Insert 键，一个非 ASCII 动作键。
     */
    public static final int INSERT              = 1025;

    /* 非动作键 */

    /**
     * Enter 键。
     */
    public static final int ENTER               = '\n';

    /**
     * BackSpace 键。
     */
    public static final int BACK_SPACE          = '\b';

    /**
     * Tab 键。
     */
    public static final int TAB                 = '\t';

    /**
     * Escape 键。
     */
    public static final int ESCAPE              = 27;

    /**
     * Delete 键。
     */
    public static final int DELETE              = 127;

    /* 所有窗口事件的基础。 */
    private static final int WINDOW_EVENT       = 200;

    /**
     * 用户要求窗口管理器关闭窗口。
     */
    public static final int WINDOW_DESTROY      = 1 + WINDOW_EVENT;

    /**
     * 用户要求窗口管理器显示窗口。
     */
    public static final int WINDOW_EXPOSE       = 2 + WINDOW_EVENT;

    /**
     * 用户要求窗口管理器最小化窗口。
     */
    public static final int WINDOW_ICONIFY      = 3 + WINDOW_EVENT;

    /**
     * 用户要求窗口管理器取消最小化窗口。
     */
    public static final int WINDOW_DEICONIFY    = 4 + WINDOW_EVENT;

    /**
     * 用户要求窗口管理器移动窗口。
     */
    public static final int WINDOW_MOVED        = 5 + WINDOW_EVENT;

    /* 所有键盘事件的基础。 */
    private static final int KEY_EVENT          = 400;

    /**
     * 用户按下了普通键。
     */
    public static final int KEY_PRESS           = 1 + KEY_EVENT;

    /**
     * 用户释放了普通键。
     */
    public static final int KEY_RELEASE         = 2 + KEY_EVENT;

    /**
     * 用户按下了非 ASCII <em>动作</em> 键。<code>key</code> 字段包含一个值，表示事件发生在以下动作键之一上：
     * 12 个功能键、箭头（光标）键、Page Up、Page Down、Home、End、Print Screen、Scroll Lock、
     * Caps Lock、Num Lock、Pause 和 Insert。
     */
    public static final int KEY_ACTION          = 3 + KEY_EVENT;

    /**
     * 用户释放了非 ASCII <em>动作</em> 键。<code>key</code> 字段包含一个值，表示事件发生在以下动作键之一上：
     * 12 个功能键、箭头（光标）键、Page Up、Page Down、Home、End、Print Screen、Scroll Lock、
     * Caps Lock、Num Lock、Pause 和 Insert。
     */
    public static final int KEY_ACTION_RELEASE  = 4 + KEY_EVENT;

    /* 所有鼠标事件的基础。 */
    private static final int MOUSE_EVENT        = 500;

    /**
     * 用户按下了鼠标按钮。<code>ALT_MASK</code> 标志表示中键被按下。
     * <code>META_MASK</code> 标志表示右键被按下。
     * @see     java.awt.Event#ALT_MASK
     * @see     java.awt.Event#META_MASK
     */
    public static final int MOUSE_DOWN          = 1 + MOUSE_EVENT;

    /**
     * 用户释放了鼠标按钮。<code>ALT_MASK</code> 标志表示中键被释放。
     * <code>META_MASK</code> 标志表示右键被释放。
     * @see     java.awt.Event#ALT_MASK
     * @see     java.awt.Event#META_MASK
     */
    public static final int MOUSE_UP            = 2 + MOUSE_EVENT;

    /**
     * 鼠标移动且没有按钮被按下。
     */
    public static final int MOUSE_MOVE          = 3 + MOUSE_EVENT;

    /**
     * 鼠标进入组件。
     */
    public static final int MOUSE_ENTER         = 4 + MOUSE_EVENT;

    /**
     * 鼠标离开组件。
     */
    public static final int MOUSE_EXIT          = 5 + MOUSE_EVENT;

    /**
     * 用户按住按钮移动鼠标。<code>ALT_MASK</code> 标志表示中键被按住。
     * <code>META_MASK</code> 标志表示右键被按住。
     * @see     java.awt.Event#ALT_MASK
     * @see     java.awt.Event#META_MASK
     */
    public static final int MOUSE_DRAG          = 6 + MOUSE_EVENT;

    /* 滚动事件 */
    private static final int SCROLL_EVENT       = 600;

    /**
     * 用户激活了滚动条的 <em>向上行</em> 区域。
     */
    public static final int SCROLL_LINE_UP      = 1 + SCROLL_EVENT;

    /**
     * 用户激活了滚动条的 <em>向下行</em> 区域。
     */
    public static final int SCROLL_LINE_DOWN    = 2 + SCROLL_EVENT;

    /**
     * 用户激活了滚动条的 <em>向上页</em> 区域。
     */
    public static final int SCROLL_PAGE_UP      = 3 + SCROLL_EVENT;

    /**
     * 用户激活了滚动条的 <em>向下页</em> 区域。
     */
    public static final int SCROLL_PAGE_DOWN    = 4 + SCROLL_EVENT;

    /**
     * 用户在滚动条中移动了气泡（拇指），移动到一个“绝对”位置，而不是相对于上一个位置的偏移量。
     */
    public static final int SCROLL_ABSOLUTE     = 5 + SCROLL_EVENT;

    /**
     * 滚动开始事件。
     */
    public static final int SCROLL_BEGIN        = 6 + SCROLL_EVENT;

    /**
     * 滚动结束事件。
     */
    public static final int SCROLL_END          = 7 + SCROLL_EVENT;

    /* 列表事件 */
    private static final int LIST_EVENT         = 700;

    /**
     * 列表中的项目被选中。
     */
    public static final int LIST_SELECT         = 1 + LIST_EVENT;

    /**
     * 列表中的项目被取消选中。
     */
    public static final int LIST_DESELECT       = 2 + LIST_EVENT;

    /* 其他事件 */
    private static final int MISC_EVENT         = 1000;

    /**
     * 此事件表示用户希望发生某些操作。
     */
    public static final int ACTION_EVENT        = 1 + MISC_EVENT;

    /**
     * 文件加载事件。
     */
    public static final int LOAD_FILE           = 2 + MISC_EVENT;

    /**
     * 文件保存事件。
     */
    public static final int SAVE_FILE           = 3 + MISC_EVENT;

    /**
     * 组件获得焦点。
     */
    public static final int GOT_FOCUS           = 4 + MISC_EVENT;

    /**
     * 组件失去焦点。
     */
    public static final int LOST_FOCUS          = 5 + MISC_EVENT;

    /**
     * 事件的目标组件。这表示事件发生或与事件相关的组件。此对象已被 AWTEvent.getSource() 替代。
     *
     * @serial
     * @see java.awt.AWTEvent#getSource()
     */
    public Object target;

    /**
     * 时间戳。已被 InputEvent.getWhen() 替代。
     *
     * @serial
     * @see java.awt.event.InputEvent#getWhen()
     */
    public long when;

    /**
     * 表示事件的类型，以及事件中相关的其他 <code>Event</code> 变量。已被 AWTEvent.getID() 替代。
     *
     * @serial
     * @see java.awt.AWTEvent#getID()
     */
    public int id;

    /**
     * 事件的 <i>x</i> 坐标。已被 MouseEvent.getX() 替代。
     *
     * @serial
     * @see java.awt.event.MouseEvent#getX()
     */
    public int x;

    /**
     * 事件的 <i>y</i> 坐标。已被 MouseEvent.getY() 替代。
     *
     * @serial
     * @see java.awt.event.MouseEvent#getY()
     */
    public int y;

    /**
     * 键盘事件中按下的键的键码。已被 KeyEvent.getKeyCode() 替代。
     *
     * @serial
     * @see java.awt.event.KeyEvent#getKeyCode()
     */
    public int key;

    /**
     * 键盘事件中按下的键的键字符。
     */
//    public char keyChar;

    /**
     * 修饰键的状态。已被 InputEvent.getModifiers() 替代。在 Java 1.1 中，MouseEvent 和 KeyEvent 是 InputEvent 的子类。
     *
     * @serial
     * @see java.awt.event.InputEvent#getModifiers()
     */
    public int modifiers;

    /**
     * 对于 <code>MOUSE_DOWN</code> 事件，此字段表示连续点击的次数。对于其他事件，其值为 <code>0</code>。
     * 此字段已被 MouseEvent.getClickCount() 替代。
     *
     * @serial
     * @see java.awt.event.MouseEvent#getClickCount()
     */
    public int clickCount;


/**
 * 事件的任意参数。此字段的值取决于事件的类型。
 * <code>arg</code> 已被特定于事件的属性所取代。
 *
 * @serial
 */
public Object arg;

/**
 * 下一个事件。当将事件放入链表时设置此字段。
 * 这已被 EventQueue 取代。
 *
 * @serial
 * @see java.awt.EventQueue
 */
public Event evt;

/* 用于将旧 Event 动作键映射到 KeyEvent 虚拟键的表。 */
private static final int actionKeyCodes[][] = {
/*    虚拟键              动作键   */
    { KeyEvent.VK_HOME,        Event.HOME         },
    { KeyEvent.VK_END,         Event.END          },
    { KeyEvent.VK_PAGE_UP,     Event.PGUP         },
    { KeyEvent.VK_PAGE_DOWN,   Event.PGDN         },
    { KeyEvent.VK_UP,          Event.UP           },
    { KeyEvent.VK_DOWN,        Event.DOWN         },
    { KeyEvent.VK_LEFT,        Event.LEFT         },
    { KeyEvent.VK_RIGHT,       Event.RIGHT        },
    { KeyEvent.VK_F1,          Event.F1           },
    { KeyEvent.VK_F2,          Event.F2           },
    { KeyEvent.VK_F3,          Event.F3           },
    { KeyEvent.VK_F4,          Event.F4           },
    { KeyEvent.VK_F5,          Event.F5           },
    { KeyEvent.VK_F6,          Event.F6           },
    { KeyEvent.VK_F7,          Event.F7           },
    { KeyEvent.VK_F8,          Event.F8           },
    { KeyEvent.VK_F9,          Event.F9           },
    { KeyEvent.VK_F10,         Event.F10          },
    { KeyEvent.VK_F11,         Event.F11          },
    { KeyEvent.VK_F12,         Event.F12          },
    { KeyEvent.VK_PRINTSCREEN, Event.PRINT_SCREEN },
    { KeyEvent.VK_SCROLL_LOCK, Event.SCROLL_LOCK  },
    { KeyEvent.VK_CAPS_LOCK,   Event.CAPS_LOCK    },
    { KeyEvent.VK_NUM_LOCK,    Event.NUM_LOCK     },
    { KeyEvent.VK_PAUSE,       Event.PAUSE        },
    { KeyEvent.VK_INSERT,      Event.INSERT       }
};

/**
 * 此字段控制事件是否在目标处理后返回给对等体 -
 * false 表示发送给对等体，true 表示不发送。
 *
 * @serial
 * @see #isConsumed()
 */
private boolean consumed = false;

/*
 * JDK 1.1 serialVersionUID
 */
private static final long serialVersionUID = 5488922509400504703L;

static {
    /* 确保加载必要的本机库 */
    Toolkit.loadLibraries();
    if (!GraphicsEnvironment.isHeadless()) {
        initIDs();
    }
}

/**
 * 初始化可以从 C 访问的字段的 JNI 字段和方法 ID。
 */
private static native void initIDs();

/**
 * <b>注意：</b> <code>Event</code> 类已过时，仅出于向后兼容的目的提供。它已被 <code>AWTEvent</code> 类及其子类所取代。
 * <p>
 * 创建一个 <code>Event</code> 实例，指定目标组件、时间戳、事件类型、<i>x</i> 和 <i>y</i>
 * 坐标、键盘键、修饰键的状态和参数。
 * @param     target     目标组件。
 * @param     when       时间戳。
 * @param     id         事件类型。
 * @param     x          <i>x</i> 坐标。
 * @param     y          <i>y</i> 坐标。
 * @param     key        键盘事件中按下的键。
 * @param     modifiers  修饰键的状态。
 * @param     arg        指定的参数。
 */
public Event(Object target, long when, int id, int x, int y, int key,
             int modifiers, Object arg) {
    this.target = target;
    this.when = when;
    this.id = id;
    this.x = x;
    this.y = y;
    this.key = key;
    this.modifiers = modifiers;
    this.arg = arg;
    this.data = 0;
    this.clickCount = 0;
    switch(id) {
      case ACTION_EVENT:
      case WINDOW_DESTROY:
      case WINDOW_ICONIFY:
      case WINDOW_DEICONIFY:
      case WINDOW_MOVED:
      case SCROLL_LINE_UP:
      case SCROLL_LINE_DOWN:
      case SCROLL_PAGE_UP:
      case SCROLL_PAGE_DOWN:
      case SCROLL_ABSOLUTE:
      case SCROLL_BEGIN:
      case SCROLL_END:
      case LIST_SELECT:
      case LIST_DESELECT:
        consumed = true; // 这些类型不会返回给对等体
        break;
      default:
    }
}

/**
 * <b>注意：</b> <code>Event</code> 类已过时，仅出于向后兼容的目的提供。它已被 <code>AWTEvent</code> 类及其子类所取代。
 * <p>
 * 创建一个 <code>Event</code> 实例，指定目标组件、时间戳、事件类型、<i>x</i> 和 <i>y</i>
 * 坐标、键盘键、修饰键的状态，参数设置为 <code>null</code>。
 * @param     target     目标组件。
 * @param     when       时间戳。
 * @param     id         事件类型。
 * @param     x          <i>x</i> 坐标。
 * @param     y          <i>y</i> 坐标。
 * @param     key        键盘事件中按下的键。
 * @param     modifiers  修饰键的状态。
 */
public Event(Object target, long when, int id, int x, int y, int key, int modifiers) {
    this(target, when, id, x, y, key, modifiers, null);
}

/**
 * <b>注意：</b> <code>Event</code> 类已过时，仅出于向后兼容的目的提供。它已被 <code>AWTEvent</code> 类及其子类所取代。
 * <p>
 * 创建一个 <code>Event</code> 实例，指定目标组件、事件类型和参数。
 * @param     target     目标组件。
 * @param     id         事件类型。
 * @param     arg        指定的参数。
 */
public Event(Object target, int id, Object arg) {
    this(target, 0, id, 0, 0, 0, 0, arg);
}

/**
 * <b>注意：</b> <code>Event</code> 类已过时，仅出于向后兼容的目的提供。它已被 <code>AWTEvent</code> 类及其子类所取代。
 * <p>
 * 将此事件翻译，使其 <i>x</i> 和 <i>y</i> 坐标分别增加 <i>dx</i> 和 <i>dy</i>。
 * <p>
 * 此方法将事件相对于给定组件进行翻译。这至少涉及将坐标翻译成给定组件的局部坐标系。在暴露事件的情况下，它还可能涉及翻译一个区域。
 * @param     dx     增加 <i>x</i> 坐标的距离。
 * @param     dy     增加 <i>y</i> 坐标的距离。
 */
public void translate(int dx, int dy) {
    this.x += dx;
    this.y += dy;
}

/**
 * <b>注意：</b> <code>Event</code> 类已过时，仅出于向后兼容的目的提供。它已被 <code>AWTEvent</code> 类及其子类所取代。
 * <p>
 * 检查 Shift 键是否按下。
 * @return    <code>true</code> 如果键按下；<code>false</code> 否则。
 * @see       java.awt.Event#modifiers
 * @see       java.awt.Event#controlDown
 * @see       java.awt.Event#metaDown
 */
public boolean shiftDown() {
    return (modifiers & SHIFT_MASK) != 0;
}

/**
 * <b>注意：</b> <code>Event</code> 类已过时，仅出于向后兼容的目的提供。它已被 <code>AWTEvent</code> 类及其子类所取代。
 * <p>
 * 检查 Control 键是否按下。
 * @return    <code>true</code> 如果键按下；<code>false</code> 否则。
 * @see       java.awt.Event#modifiers
 * @see       java.awt.Event#shiftDown
 * @see       java.awt.Event#metaDown
 */
public boolean controlDown() {
    return (modifiers & CTRL_MASK) != 0;
}

/**
 * <b>注意：</b> <code>Event</code> 类已过时，仅出于向后兼容的目的提供。它已被 <code>AWTEvent</code> 类及其子类所取代。
 * <p>
 * 检查 Meta 键是否按下。
 *
 * @return    <code>true</code> 如果键按下；<code>false</code> 否则。
 * @see       java.awt.Event#modifiers
 * @see       java.awt.Event#shiftDown
 * @see       java.awt.Event#controlDown
 */
public boolean metaDown() {
    return (modifiers & META_MASK) != 0;
}

/**
 * <b>注意：</b> <code>Event</code> 类已过时，仅出于向后兼容的目的提供。它已被 <code>AWTEvent</code> 类及其子类所取代。
 */
void consume() {
    switch(id) {
      case KEY_PRESS:
      case KEY_RELEASE:
      case KEY_ACTION:
      case KEY_ACTION_RELEASE:
          consumed = true;
          break;
      default:
          // 事件类型不能被消耗
    }
}

/**
 * <b>注意：</b> <code>Event</code> 类已过时，仅出于向后兼容的目的提供。它已被 <code>AWTEvent</code> 类及其子类所取代。
 */
boolean isConsumed() {
    return consumed;
}

/*
 * <b>注意：</b> <code>Event</code> 类已过时，仅出于向后兼容的目的提供。它已被 <code>AWTEvent</code> 类及其子类所取代。
 * <p>
 * 返回与此事件中的键关联的整数键码，如 java.awt.Event 中所述。
 */
static int getOldEventKey(KeyEvent e) {
    int keyCode = e.getKeyCode();
    for (int i = 0; i < actionKeyCodes.length; i++) {
        if (actionKeyCodes[i][0] == keyCode) {
            return actionKeyCodes[i][1];
        }
    }
    return (int)e.getKeyChar();
}

/*
 * <b>注意：</b> <code>Event</code> 类已过时，仅出于向后兼容的目的提供。它已被 <code>AWTEvent</code> 类及其子类所取代。
 * <p>
 * 返回与这个旧事件的 int 键相对应的新 KeyEvent 字符。
 */
char getKeyEventChar() {
   for (int i = 0; i < actionKeyCodes.length; i++) {
        if (actionKeyCodes[i][1] == key) {
            return KeyEvent.CHAR_UNDEFINED;
        }
   }
   return (char)key;
}

/**
 * <b>注意：</b> <code>Event</code> 类已过时，仅出于向后兼容的目的提供。它已被 <code>AWTEvent</code> 类及其子类所取代。
 * <p>
 * 返回表示此 <code>Event</code> 状态的字符串。此方法仅用于调试目的，返回字符串的内容和格式可能因实现而异。返回的字符串可以为空，但不能为 <code>null</code>。
 *
 * @return    此事件的参数字符串
 */
protected String paramString() {
    String str = "id=" + id + ",x=" + x + ",y=" + y;
    if (key != 0) {
        str += ",key=" + key;
    }
    if (shiftDown()) {
        str += ",shift";
    }
    if (controlDown()) {
        str += ",control";
    }
    if (metaDown()) {
        str += ",meta";
    }
    if (target != null) {
        str += ",target=" + target;
    }
    if (arg != null) {
        str += ",arg=" + arg;
    }
    return str;
}

/**
 * <b>注意：</b> <code>Event</code> 类已过时，仅出于向后兼容的目的提供。它已被 <code>AWTEvent</code> 类及其子类所取代。
 * <p>
 * 返回表示此事件值的字符串。
 * @return    代表事件及其成员字段值的字符串。
 * @see       java.awt.Event#paramString
 * @since     JDK1.1
 */
public String toString() {
    return getClass().getName() + "[" + paramString() + "]";
}
