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
import java.awt.ItemSelectable;

/**
 * 一个语义事件，表示一个项目被选中或取消选中。
 * 这个高级事件由一个 ItemSelectable 对象（如 List）生成，当用户选中或取消选中一个项目时触发。
 * 该事件传递给每个使用组件的 <code>addItemListener</code> 方法注册以接收此类事件的 <code>ItemListener</code> 对象。
 * <P>
 * 实现 <code>ItemListener</code> 接口的对象在事件发生时会收到这个 <code>ItemEvent</code>。
 * 监听器可以处理“有意义的”（语义）事件，如“项目选中”或“项目取消选中”，而无需处理单个鼠标移动和鼠标点击的细节。
 * <p>
 * 如果任何特定的 <code>ItemEvent</code> 实例的 {@code id} 参数不在 {@code ITEM_FIRST} 到 {@code ITEM_LAST} 的范围内，将导致未指定的行为。
 * <p>
 * 任何 <code>ItemEvent</code> 实例的 {@code stateChange} 可以取以下值之一：
 *                     <ul>
 *                     <li> {@code ItemEvent.SELECTED}
 *                     <li> {@code ItemEvent.DESELECTED}
 *                     </ul>
 * 分配上述列表以外的值将导致未指定的行为。
 *
 * @author Carl Quinn
 *
 * @see java.awt.ItemSelectable
 * @see ItemListener
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/itemlistener.html">教程：编写 Item Listener</a>
 *
 * @since 1.1
 */
public class ItemEvent extends AWTEvent {

    /**
     * 用于项目事件的 ID 范围的第一个数字。
     */
    public static final int ITEM_FIRST          = 701;

    /**
     * 用于项目事件的 ID 范围的最后一个数字。
     */
    public static final int ITEM_LAST           = 701;

    /**
     * 此事件 ID 表示项目的状态已更改。
     */
    public static final int ITEM_STATE_CHANGED  = ITEM_FIRST; //Event.LIST_SELECT

    /**
     * 此状态更改值表示项目已被选中。
     */
    public static final int SELECTED = 1;

    /**
     * 此状态更改值表示已选中的项目已被取消选中。
     */
    public static final int DESELECTED  = 2;

    /**
     * 选择状态已更改的项目。
     *
     * @serial
     * @see #getItem()
     */
    Object item;

    /**
     * <code>stateChange</code> 表示 <code>item</code> 是否被选中或取消选中。
     *
     * @serial
     * @see #getStateChange()
     */
    int stateChange;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -608708132447206933L;

    /**
     * 构造一个 <code>ItemEvent</code> 对象。
     * <p> 如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source 生成事件的 <code>ItemSelectable</code> 对象
     * @param id     识别事件类型的整数。
     *               有关允许值的信息，请参见 {@link ItemEvent} 的类描述
     * @param item   受事件影响的对象
     * @param stateChange 一个整数，表示项目是被选中还是取消选中。
     *               有关允许值的信息，请参见 {@link ItemEvent} 的类描述
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getItemSelectable()
     * @see #getID()
     * @see #getStateChange()
     */
    public ItemEvent(ItemSelectable source, int id, Object item, int stateChange) {
        super(source, id);
        this.item = item;
        this.stateChange = stateChange;
    }

    /**
     * 返回事件的发起者。
     *
     * @return 生成事件的 ItemSelectable 对象。
     */
    public ItemSelectable getItemSelectable() {
        return (ItemSelectable)source;
    }

   /**
    * 返回受事件影响的项目。
    *
    * @return 受事件影响的项目（对象）
    */
    public Object getItem() {
        return item;
    }

   /**
    * 返回状态更改的类型（选中或取消选中）。
    *
    * @return 一个整数，表示项目是被选中还是取消选中
    *
    * @see #SELECTED
    * @see #DESELECTED
    */
    public int getStateChange() {
        return stateChange;
    }

    /**
     * 返回一个标识此项目事件的参数字符串。
     * 此方法对于事件日志记录和调试非常有用。
     *
     * @return 一个标识事件及其属性的字符串
     */
    public String paramString() {
        String typeStr;
        switch(id) {
          case ITEM_STATE_CHANGED:
              typeStr = "ITEM_STATE_CHANGED";
              break;
          default:
              typeStr = "unknown type";
        }

        String stateStr;
        switch(stateChange) {
          case SELECTED:
              stateStr = "SELECTED";
              break;
          case DESELECTED:
              stateStr = "DESELECTED";
              break;
          default:
              stateStr = "unknown type";
        }
        return typeStr + ",item="+item + ",stateChange="+stateStr;
    }

}
