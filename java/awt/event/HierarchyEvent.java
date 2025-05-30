/*
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.Component;
import java.awt.Container;

/**
 * 一个事件，表示 <code>Component</code> 所属的 <code>Component</code> 层次结构的变化。
 * <ul>
 * <li>层次结构变化事件 (HierarchyListener)
 *     <ul>
 *     <li> 添加祖先
 *     <li> 移除祖先
 *     <li> 层次结构变为可显示
 *     <li> 层次结构变为不可显示
 *     <li> 层次结构在屏幕上显示（既可见且可显示）
 *     <li> 层次结构在屏幕上隐藏（不可见或不可显示）
 *     </ul>
 * <li>祖先重塑事件 (HierarchyBoundsListener)
 *     <ul>
 *     <li> 祖先被调整大小
 *     <li> 祖先被移动
 *     </ul>
 * </ul>
 * <p>
 * 层次结构事件仅用于通知目的。AWT 将自动处理层次结构的内部变化，以确保 GUI 布局和可显示性正常工作，无论程序是否接收这些事件。
 * <p>
 * 当容器被添加、移除、移动或调整大小时，由 Container 对象（如 Panel）生成此事件，并沿层次结构传递。当调用对象的
 * <code>addNotify</code>、<code>removeNotify</code>、<code>show</code> 或
 * <code>hide</code> 方法时，也会由 Component 对象生成此事件。{@code ANCESTOR_MOVED} 和
 * {@code ANCESTOR_RESIZED}
 * 事件将分发给每个使用 Component 的 <code>addHierarchyBoundsListener</code> 方法注册以接收此类事件的
 * <code>HierarchyBoundsListener</code> 或 <code>HierarchyBoundsAdapter</code> 对象。
 * （<code>HierarchyBoundsAdapter</code> 对象实现了 <code>HierarchyBoundsListener</code> 接口。）
 * {@code HIERARCHY_CHANGED} 事件将分发给每个使用 Component 的 <code>addHierarchyListener</code> 方法注册以接收此类事件的
 * <code>HierarchyListener</code> 对象。每个这样的监听器对象在事件发生时都会收到此 <code>HierarchyEvent</code>。
 * <p>
 * 如果任何特定的 {@code HierarchyEvent} 实例的 {@code id} 参数不在 {@code HIERARCHY_FIRST} 到 {@code HIERARCHY_LAST} 的范围内，
 * 将导致未指定的行为。
 * <br>
 * 任何 {@code HierarchyEvent} 实例的 {@code changeFlags} 参数可以取以下值之一：
 * <ul>
 * <li> {@code HierarchyEvent.PARENT_CHANGED}
 * <li> {@code HierarchyEvent.DISPLAYABILITY_CHANGED}
 * <li> {@code HierarchyEvent.SHOWING_CHANGED}
 * </ul>
 * 分配不同于上述值的值将导致未指定的行为。
 *
 * @author      David Mendenhall
 * @see         HierarchyListener
 * @see         HierarchyBoundsAdapter
 * @see         HierarchyBoundsListener
 * @since       1.3
 */
public class HierarchyEvent extends AWTEvent {
    /*
     * serialVersionUID
     */
    private static final long serialVersionUID = -5337576970038043990L;

    /**
     * 标记层次结构事件 ID 范围的第一个整数 ID。
     */
    public static final int HIERARCHY_FIRST = 1400; // 1300 由 sun.awt.windows.ModalityEvent 使用

    /**
     * 表示对整个层次结构树进行了修改的事件 ID。
     */
    public static final int HIERARCHY_CHANGED = HIERARCHY_FIRST;

    /**
     * 表示祖先容器被移动的事件 ID。
     */
    public static final int ANCESTOR_MOVED = 1 + HIERARCHY_FIRST;

    /**
     * 表示祖先容器被调整大小的事件 ID。
     */
    public static final int ANCESTOR_RESIZED = 2 + HIERARCHY_FIRST;

    /**
     * 标记祖先事件 ID 范围的最后一个整数 ID。
     */
    public static final int HIERARCHY_LAST = ANCESTOR_RESIZED;

    /**
     * 表示 <code>HIERARCHY_CHANGED</code> 事件是由重新父化操作生成的更改标志。
     */
    public static final int PARENT_CHANGED = 0x1;

    /**
     * 表示 <code>HIERARCHY_CHANGED</code> 事件是由于层次结构的可显示性变化而生成的更改标志。
     * 要确定层次结构的当前可显示性，请调用 <code>Component.isDisplayable</code> 方法。可显示性变化发生在
     * 显式或隐式调用 <code>Component.addNotify</code> 和 <code>Component.removeNotify</code> 方法时。
     *
     * @see java.awt.Component#isDisplayable()
     * @see java.awt.Component#addNotify()
     * @see java.awt.Component#removeNotify()
     */
    public static final int DISPLAYABILITY_CHANGED = 0x2;

    /**
     * 表示 <code>HIERARCHY_CHANGED</code> 事件是由于层次结构的显示状态变化而生成的更改标志。
     * 要确定层次结构的当前显示状态，请调用 <code>Component.isShowing</code> 方法。显示状态变化发生在
     * 层次结构的可显示性或可见性发生变化时。可见性变化发生在显式或隐式调用 <code>Component.show</code> 和
     * <code>Component.hide</code> 方法时。
     *
     * @see java.awt.Component#isShowing()
     * @see java.awt.Component#addNotify()
     * @see java.awt.Component#removeNotify()
     * @see java.awt.Component#show()
     * @see java.awt.Component#hide()
     */
    public static final int SHOWING_CHANGED = 0x4;

    Component changed;
    Container changedParent;
    long      changeFlags;

    /**
     * 构造一个 <code>HierarchyEvent</code> 对象，以标识 <code>Component</code> 层次结构的变化。
     * <p>如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source          生成事件的 <code>Component</code> 对象
     * @param id              表示事件类型的整数。有关允许值的信息，请参阅 {@link HierarchyEvent} 的类描述
     * @param changed         被更改的层次结构顶部的 <code>Component</code>
     * @param changedParent   被更改的组件的父容器。这可能是更改前或更改后的父容器，具体取决于更改类型
     * @throws IllegalArgumentException 如果 <code>source</code> 为 {@code null}
     * @see #getSource()
     * @see #getID()
     * @see #getChanged()
     * @see #getChangedParent()
     */
    public HierarchyEvent(Component source, int id, Component changed,
                          Container changedParent) {
        super(source, id);
        this.changed = changed;
        this.changedParent = changedParent;
    }

    /**
     * 构造一个 <code>HierarchyEvent</code> 对象，以标识 <code>Component</code> 层次结构的变化。
     * <p>如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source          生成事件的 <code>Component</code> 对象
     * @param id              表示事件类型的整数。有关允许值的信息，请参阅 {@link HierarchyEvent} 的类描述
     * @param changed         被更改的层次结构顶部的 <code>Component</code>
     * @param changedParent   被更改的组件的父容器。这可能是更改前或更改后的父容器，具体取决于更改类型
     * @param changeFlags     表示此事件对象中表示的 <code>HIERARCHY_CHANGED</code> 事件类型的位掩码。
     *                        有关允许值的信息，请参阅 {@link HierarchyEvent} 的类描述
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getSource()
     * @see #getID()
     * @see #getChanged()
     * @see #getChangedParent()
     * @see #getChangeFlags()
     */
    public HierarchyEvent(Component source, int id, Component changed,
                          Container changedParent, long changeFlags) {
        super(source, id);
        this.changed = changed;
        this.changedParent = changedParent;
        this.changeFlags = changeFlags;
    }

    /**
     * 返回事件的发起者。
     *
     * @return 生成事件的 <code>Component</code> 对象，如果对象不是 <code>Component</code>，则返回 <code>null</code>
     */
    public Component getComponent() {
        return (source instanceof Component) ? (Component)source : null;
    }

    /**
     * 返回层次结构中被更改的顶部组件。
     *
     * @return 被更改的组件
     */
    public Component getChanged() {
        return changed;
    }

    /**
     * 返回 <code>getChanged()</code> 返回的组件的父容器。对于类型为 PARENT_CHANGED 的 HIERARCHY_CHANGED 事件，
     * 如果是通过调用 <code>Container.add</code> 进行更改的，则返回的父容器是添加操作后的父容器。对于类型为 PARENT_CHANGED 的
     * HIERARCHY_CHANGED 事件，如果是通过调用 <code>Container.remove</code> 进行更改的，则返回的父容器是移除操作前的父容器。
     * 对于所有其他事件和类型，返回的父容器是操作期间的父容器。
     *
     * @return 被更改组件的父容器
     */
    public Container getChangedParent() {
        return changedParent;
    }

    /**
     * 返回一个位掩码，表示此事件对象中表示的 HIERARCHY_CHANGED 事件类型。这些位已通过位或操作组合在一起。
     *
     * @return 位掩码，如果这不是 HIERARCHY_CHANGED 事件，则返回 0
     */
    public long getChangeFlags() {
        return changeFlags;
    }

    /**
     * 返回一个参数字符串，用于标识此事件。此方法对于事件记录和调试非常有用。
     *
     * @return 识别事件及其属性的字符串
     */
    public String paramString() {
        String typeStr;
        switch(id) {
          case ANCESTOR_MOVED:
              typeStr = "ANCESTOR_MOVED ("+changed+","+changedParent+")";
              break;
          case ANCESTOR_RESIZED:
              typeStr = "ANCESTOR_RESIZED ("+changed+","+changedParent+")";
              break;
          case HIERARCHY_CHANGED: {
              typeStr = "HIERARCHY_CHANGED (";
              boolean first = true;
              if ((changeFlags & PARENT_CHANGED) != 0) {
                  first = false;
                  typeStr += "PARENT_CHANGED";
              }
              if ((changeFlags & DISPLAYABILITY_CHANGED) != 0) {
                  if (first) {
                      first = false;
                  } else {
                      typeStr += ",";
                  }
                  typeStr += "DISPLAYABILITY_CHANGED";
              }
              if ((changeFlags & SHOWING_CHANGED) != 0) {
                  if (first) {
                      first = false;
                  } else {
                      typeStr += ",";
                  }
                  typeStr += "SHOWING_CHANGED";
              }
              if (!first) {
                  typeStr += ",";
              }
              typeStr += changed + "," + changedParent + ")";
              break;
          }
          default:
              typeStr = "unknown type";
        }
        return typeStr;
    }
}
