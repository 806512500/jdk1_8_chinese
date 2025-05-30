
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

package java.awt;

import java.util.EventObject;
import java.awt.event.*;
import java.awt.peer.ComponentPeer;
import java.awt.peer.LightweightPeer;
import java.lang.reflect.Field;
import sun.awt.AWTAccessor;
import sun.util.logging.PlatformLogger;

import java.security.AccessControlContext;
import java.security.AccessController;

/**
 * 所有 AWT 事件的根事件类。
 * 这个类及其子类取代了原始的 java.awt.Event 类。
 * 定义在 java.awt.event 包之外的 AWTEvent 类的子类应定义大于
 * RESERVED_ID_MAX 的事件 ID 值。
 * <p>
 * 本类中定义的事件掩码是需要使用 Component.enableEvents() 选择未由注册监听器
 * 选择的事件类型的 Component 子类所需的。如果在组件上注册了监听器，则组件内部已
 * 经设置了适当的事件掩码。
 * <p>
 * 掩码还用于指定 AWTEventListener 应监听的事件类型。掩码通过位或运算组合在一起，
 * 并传递给 Toolkit.addAWTEventListener。
 *
 * @see Component#enableEvents
 * @see Toolkit#addAWTEventListener
 *
 * @see java.awt.event.ActionEvent
 * @see java.awt.event.AdjustmentEvent
 * @see java.awt.event.ComponentEvent
 * @see java.awt.event.ContainerEvent
 * @see java.awt.event.FocusEvent
 * @see java.awt.event.InputMethodEvent
 * @see java.awt.event.InvocationEvent
 * @see java.awt.event.ItemEvent
 * @see java.awt.event.HierarchyEvent
 * @see java.awt.event.KeyEvent
 * @see java.awt.event.MouseEvent
 * @see java.awt.event.MouseWheelEvent
 * @see java.awt.event.PaintEvent
 * @see java.awt.event.TextEvent
 * @see java.awt.event.WindowEvent
 *
 * @author Carl Quinn
 * @author Amy Fowler
 * @since 1.1
 */
public abstract class AWTEvent extends EventObject {
    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.AWTEvent");
    private byte bdata[];

    /**
     * 事件的 ID。
     * @serial
     * @see #getID()
     * @see #AWTEvent
     */
    protected int id;

    /**
     * 控制事件是否在源处理后发送回对等组件 - false 表示发送给对等组件；true 表示不发送。
     * 语义事件总是具有 'true' 值，因为它们是由对等组件响应低级事件生成的。
     * @serial
     * @see #consume
     * @see #isConsumed
     */
    protected boolean consumed = false;

   /*
    * 事件的 AccessControlContext。
    */
    private transient volatile AccessControlContext acc =
        AccessController.getContext();

   /*
    * 返回构造此事件时使用的 acc。
    */
    final AccessControlContext getAccessControlContext() {
        if (acc == null) {
            throw new SecurityException("AWTEvent is missing AccessControlContext");
        }
        return acc;
    }

    transient boolean focusManagerIsDispatching = false;
    transient boolean isPosted;

    /**
     * 指示此 AWTEvent 是由系统生成的，而不是由用户代码生成的。
     */
    private transient boolean isSystemGenerated;

    /**
     * 选择组件事件的事件掩码。
     */
    public final static long COMPONENT_EVENT_MASK = 0x01;

    /**
     * 选择容器事件的事件掩码。
     */
    public final static long CONTAINER_EVENT_MASK = 0x02;

    /**
     * 选择焦点事件的事件掩码。
     */
    public final static long FOCUS_EVENT_MASK = 0x04;

    /**
     * 选择键盘事件的事件掩码。
     */
    public final static long KEY_EVENT_MASK = 0x08;

    /**
     * 选择鼠标事件的事件掩码。
     */
    public final static long MOUSE_EVENT_MASK = 0x10;

    /**
     * 选择鼠标移动事件的事件掩码。
     */
    public final static long MOUSE_MOTION_EVENT_MASK = 0x20;

    /**
     * 选择窗口事件的事件掩码。
     */
    public final static long WINDOW_EVENT_MASK = 0x40;

    /**
     * 选择动作事件的事件掩码。
     */
    public final static long ACTION_EVENT_MASK = 0x80;

    /**
     * 选择调整事件的事件掩码。
     */
    public final static long ADJUSTMENT_EVENT_MASK = 0x100;

    /**
     * 选择项事件的事件掩码。
     */
    public final static long ITEM_EVENT_MASK = 0x200;

    /**
     * 选择文本事件的事件掩码。
     */
    public final static long TEXT_EVENT_MASK = 0x400;

    /**
     * 选择输入方法事件的事件掩码。
     */
    public final static long INPUT_METHOD_EVENT_MASK = 0x800;

    /**
     * 用于启用输入方法的伪事件掩码。
     * 我们使用事件掩码中的一个位，因此不需要单独的字段 inputMethodsEnabled。
     */
    final static long INPUT_METHODS_ENABLED_MASK = 0x1000;

    /**
     * 选择绘制事件的事件掩码。
     */
    public final static long PAINT_EVENT_MASK = 0x2000;

    /**
     * 选择调用事件的事件掩码。
     */
    public final static long INVOCATION_EVENT_MASK = 0x4000;

    /**
     * 选择层次结构事件的事件掩码。
     */
    public final static long HIERARCHY_EVENT_MASK = 0x8000;

    /**
     * 选择层次结构边界事件的事件掩码。
     */
    public final static long HIERARCHY_BOUNDS_EVENT_MASK = 0x10000;

    /**
     * 选择鼠标滚轮事件的事件掩码。
     * @since 1.4
     */
    public final static long MOUSE_WHEEL_EVENT_MASK = 0x20000;

    /**
     * 选择窗口状态事件的事件掩码。
     * @since 1.4
     */
    public final static long WINDOW_STATE_EVENT_MASK = 0x40000;

    /**
     * 选择窗口焦点事件的事件掩码。
     * @since 1.4
     */
    public final static long WINDOW_FOCUS_EVENT_MASK = 0x80000;

    /**
     * 警告：还有更多私有定义的掩码。请参见 SunToolkit.GRAB_EVENT_MASK。
     */

    /**
     * 预留 AWT 事件 ID 的最大值。定义自己的事件 ID 的程序应使用大于此值的 ID。
     */
    public final static int RESERVED_ID_MAX = 1999;

    // 安全相关
    private static Field inputEvent_CanAccessSystemClipboard_Field = null;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -1825314779160409405L;

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
        AWTAccessor.setAWTEventAccessor(
            new AWTAccessor.AWTEventAccessor() {
                public void setPosted(AWTEvent ev) {
                    ev.isPosted = true;
                }

                public void setSystemGenerated(AWTEvent ev) {
                    ev.isSystemGenerated = true;
                }

                public boolean isSystemGenerated(AWTEvent ev) {
                    return ev.isSystemGenerated;
                }

                public AccessControlContext getAccessControlContext(AWTEvent ev) {
                    return ev.getAccessControlContext();
                }

                public byte[] getBData(AWTEvent ev) {
                    return ev.bdata;
                }

                public void setBData(AWTEvent ev, byte[] bdata) {
                    ev.bdata = bdata;
                }

            });
    }

    private static synchronized Field get_InputEvent_CanAccessSystemClipboard() {
        if (inputEvent_CanAccessSystemClipboard_Field == null) {
            inputEvent_CanAccessSystemClipboard_Field =
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<Field>() {
                            public Field run() {
                                Field field = null;
                                try {
                                    field = InputEvent.class.
                                        getDeclaredField("canAccessSystemClipboard");
                                    field.setAccessible(true);
                                    return field;
                                } catch (SecurityException e) {
                                    if (log.isLoggable(PlatformLogger.Level.FINE)) {
                                        log.fine("AWTEvent.get_InputEvent_CanAccessSystemClipboard() got SecurityException ", e);
                                    }
                                } catch (NoSuchFieldException e) {
                                    if (log.isLoggable(PlatformLogger.Level.FINE)) {
                                        log.fine("AWTEvent.get_InputEvent_CanAccessSystemClipboard() got NoSuchFieldException ", e);
                                    }
                                }
                                return null;
                            }
                        });
        }

        return inputEvent_CanAccessSystemClipboard_Field;
    }

    /**
     * 初始化可以从 C 访问的字段的 JNI 字段和方法 ID。
     */
    private static native void initIDs();

    /**
     * 从 1.0 风格事件的参数构造 AWTEvent 对象。
     * @param event 旧风格的事件
     */
    public AWTEvent(Event event) {
        this(event.target, event.id);
    }

    /**
     * 使用指定的源对象和类型构造 AWTEvent 对象。
     *
     * @param source 事件的源对象
     * @param id 事件类型
     */
    public AWTEvent(Object source, int id) {
        super(source);
        this.id = id;
        switch(id) {
          case ActionEvent.ACTION_PERFORMED:
          case ItemEvent.ITEM_STATE_CHANGED:
          case AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED:
          case TextEvent.TEXT_VALUE_CHANGED:
            consumed = true;
            break;
          default:
        }
    }

    /**
     * 将事件重新定向到新的源。此方法通常用于将事件重新定向到原始重量级源的轻量级子组件。
     * <p>
     * 此方法仅用于事件目标子系统，例如客户端定义的 KeyboardFocusManagers。不用于一般客户端。
     *
     * @param newSource 事件应分派到的新对象
     * @since 1.4
     */
    public void setSource(Object newSource) {
        if (source == newSource) {
            return;
        }

        Component comp = null;
        if (newSource instanceof Component) {
            comp = (Component)newSource;
            while (comp != null && comp.peer != null &&
                   (comp.peer instanceof LightweightPeer)) {
                comp = comp.parent;
            }
        }

        synchronized (this) {
            source = newSource;
            if (comp != null) {
                ComponentPeer peer = comp.peer;
                if (peer != null) {
                    nativeSetSource(peer);
                }
            }
        }
    }

    private native void nativeSetSource(ComponentPeer peer);

    /**
     * 返回事件类型。
     */
    public int getID() {
        return id;
    }

    /**
     * 返回此对象的字符串表示形式。
     */
    public String toString() {
        String srcName = null;
        if (source instanceof Component) {
            srcName = ((Component)source).getName();
        } else if (source instanceof MenuComponent) {
            srcName = ((MenuComponent)source).getName();
        }
        return getClass().getName() + "[" + paramString() + "] on " +
            (srcName != null? srcName : source);
    }

    /**
     * 返回表示此 Event 状态的字符串。
     * 此方法仅用于调试目的，返回的字符串的内容和格式可能因实现而异。
     * 返回的字符串可以为空，但不能为 null。
     *
     * @return 事件的字符串表示形式
     */
    public String paramString() {
        return "";
    }

    /**
     * 如果此事件可以被消耗，则消耗此事件。只有低级、系统事件可以被消耗。
     */
    protected void consume() {
        switch(id) {
          case KeyEvent.KEY_PRESSED:
          case KeyEvent.KEY_RELEASED:
          case MouseEvent.MOUSE_PRESSED:
          case MouseEvent.MOUSE_RELEASED:
          case MouseEvent.MOUSE_MOVED:
          case MouseEvent.MOUSE_DRAGGED:
          case MouseEvent.MOUSE_ENTERED:
          case MouseEvent.MOUSE_EXITED:
          case MouseEvent.MOUSE_WHEEL:
          case InputMethodEvent.INPUT_METHOD_TEXT_CHANGED:
          case InputMethodEvent.CARET_POSITION_CHANGED:
              consumed = true;
              break;
          default:
              // 事件类型不能被消耗
        }
    }

    /**
     * 返回此事件是否已被消耗。
     */
    protected boolean isConsumed() {
        return consumed;
    }

    /**
     * 将新事件转换为旧事件（用于兼容性）。
     * 如果新事件不能转换（因为没有旧的等效事件存在），则返回 null。
     *
     * 注意：此方法位于这里而不是位于 java.awt.event 中的每个单独的新事件类中，
     * 因为我们不想将其公开，并且需要从 java.awt 调用。
     */
    Event convertToOld() {
        Object src = getSource();
        int newid = id;

        switch(id) {
          case KeyEvent.KEY_PRESSED:
          case KeyEvent.KEY_RELEASED:
              KeyEvent ke = (KeyEvent)this;
              if (ke.isActionKey()) {
                  newid = (id == KeyEvent.KEY_PRESSED?
                           Event.KEY_ACTION : Event.KEY_ACTION_RELEASE);
              }
              int keyCode = ke.getKeyCode();
              if (keyCode == KeyEvent.VK_SHIFT ||
                  keyCode == KeyEvent.VK_CONTROL ||
                  keyCode == KeyEvent.VK_ALT) {
                  return null;  // 在旧事件模型中抑制修饰键。
              }
              // 旧 Event 中没有 button1 的掩码 - 去除它
              return new Event(src, ke.getWhen(), newid, 0, 0,
                               Event.getOldEventKey(ke),
                               (ke.getModifiers() & ~InputEvent.BUTTON1_MASK));


                      case MouseEvent.MOUSE_PRESSED:
          case MouseEvent.MOUSE_RELEASED:
          case MouseEvent.MOUSE_MOVED:
          case MouseEvent.MOUSE_DRAGGED:
          case MouseEvent.MOUSE_ENTERED:
          case MouseEvent.MOUSE_EXITED:
              MouseEvent me = (MouseEvent)this;
              // 旧 Event 中没有按钮1的掩码 - 去除它
              Event olde = new Event(src, me.getWhen(), newid,
                               me.getX(), me.getY(), 0,
                               (me.getModifiers() & ~InputEvent.BUTTON1_MASK));
              olde.clickCount = me.getClickCount();
              return olde;

          case FocusEvent.FOCUS_GAINED:
              return new Event(src, Event.GOT_FOCUS, null);

          case FocusEvent.FOCUS_LOST:
              return new Event(src, Event.LOST_FOCUS, null);

          case WindowEvent.WINDOW_CLOSING:
          case WindowEvent.WINDOW_ICONIFIED:
          case WindowEvent.WINDOW_DEICONIFIED:
              return new Event(src, newid, null);

          case ComponentEvent.COMPONENT_MOVED:
              if (src instanceof Frame || src instanceof Dialog) {
                  Point p = ((Component)src).getLocation();
                  return new Event(src, 0, Event.WINDOW_MOVED, p.x, p.y, 0, 0);
              }
              break;

          case ActionEvent.ACTION_PERFORMED:
              ActionEvent ae = (ActionEvent)this;
              String cmd;
              if (src instanceof Button) {
                  cmd = ((Button)src).getLabel();
              } else if (src instanceof MenuItem) {
                  cmd = ((MenuItem)src).getLabel();
              } else {
                  cmd = ae.getActionCommand();
              }
              return new Event(src, 0, newid, 0, 0, 0, ae.getModifiers(), cmd);

          case ItemEvent.ITEM_STATE_CHANGED:
              ItemEvent ie = (ItemEvent)this;
              Object arg;
              if (src instanceof List) {
                  newid = (ie.getStateChange() == ItemEvent.SELECTED?
                           Event.LIST_SELECT : Event.LIST_DESELECT);
                  arg = ie.getItem();
              } else {
                  newid = Event.ACTION_EVENT;
                  if (src instanceof Choice) {
                      arg = ie.getItem();

                  } else { // Checkbox
                      arg = Boolean.valueOf(ie.getStateChange() == ItemEvent.SELECTED);
                  }
              }
              return new Event(src, newid, arg);

          case AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED:
              AdjustmentEvent aje = (AdjustmentEvent)this;
              switch(aje.getAdjustmentType()) {
                case AdjustmentEvent.UNIT_INCREMENT:
                  newid = Event.SCROLL_LINE_DOWN;
                  break;
                case AdjustmentEvent.UNIT_DECREMENT:
                  newid = Event.SCROLL_LINE_UP;
                  break;
                case AdjustmentEvent.BLOCK_INCREMENT:
                  newid = Event.SCROLL_PAGE_DOWN;
                  break;
                case AdjustmentEvent.BLOCK_DECREMENT:
                  newid = Event.SCROLL_PAGE_UP;
                  break;
                case AdjustmentEvent.TRACK:
                  if (aje.getValueIsAdjusting()) {
                      newid = Event.SCROLL_ABSOLUTE;
                  }
                  else {
                      newid = Event.SCROLL_END;
                  }
                  break;
                default:
                  return null;
              }
              return new Event(src, newid, Integer.valueOf(aje.getValue()));

          default:
        }
        return null;
    }

    /**
     * 将此事件中的所有私有数据复制到 that 中。
     * 为复制的数据分配空间，这些空间将在 that 被终结时释放。完成后，
     * 此事件不会发生改变。
     */
    void copyPrivateDataInto(AWTEvent that) {
        that.bdata = this.bdata;
        // 从 this 复制 canAccessSystemClipboard 值到 that。
        if (this instanceof InputEvent && that instanceof InputEvent) {
            Field field = get_InputEvent_CanAccessSystemClipboard();
            if (field != null) {
                try {
                    boolean b = field.getBoolean(this);
                    field.setBoolean(that, b);
                } catch(IllegalAccessException e) {
                    if (log.isLoggable(PlatformLogger.Level.FINE)) {
                        log.fine("AWTEvent.copyPrivateDataInto() got IllegalAccessException ", e);
                    }
                }
            }
        }
        that.isSystemGenerated = this.isSystemGenerated;
    }

    void dispatched() {
        if (this instanceof InputEvent) {
            Field field = get_InputEvent_CanAccessSystemClipboard();
            if (field != null) {
                try {
                    field.setBoolean(this, false);
                } catch(IllegalAccessException e) {
                    if (log.isLoggable(PlatformLogger.Level.FINE)) {
                        log.fine("AWTEvent.dispatched() got IllegalAccessException ", e);
                    }
                }
            }
        }
    }
} // class AWTEvent
