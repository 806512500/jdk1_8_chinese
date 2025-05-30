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
import java.awt.Component;
import java.awt.Rectangle;
import java.lang.annotation.Native;

/**
 * 一个低级事件，表示组件移动、改变大小或改变可见性（同时也是其他组件级事件的根类）。
 * <P>
 * 组件事件仅用于通知目的；
 * AWT 将自动处理组件的移动和调整大小，以确保 GUI 布局正常工作，无论程序是否接收这些事件。
 * <P>
 * 除了作为其他与组件相关的事件（InputEvent、FocusEvent、WindowEvent、ContainerEvent）的基础类之外，
 * 该类还定义了表示组件大小、位置或可见性变化的事件。
 * <P>
 * 当组件移动、调整大小、变为不可见或再次变为可见时，此类事件由组件对象（如 List）生成。
 * 该事件传递给使用组件的 <code>addComponentListener</code> 方法注册以接收此类事件的每个 <code>ComponentListener</code>
 * 或 <code>ComponentAdapter</code> 对象。每个这样的监听器对象在事件发生时都会收到此 <code>ComponentEvent</code>。
 * <p>
 * 如果任何特定 <code>ComponentEvent</code> 实例的 {@code id} 参数不在 {@code COMPONENT_FIRST} 到 {@code COMPONENT_LAST} 的范围内，
 * 将导致未指定的行为。
 *
 * @see ComponentAdapter
 * @see ComponentListener
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/componentlistener.html">教程：编写组件监听器</a>
 *
 * @author Carl Quinn
 * @since 1.1
 */
public class ComponentEvent extends AWTEvent {

    /**
     * 用于组件事件的 id 范围中的第一个数字。
     */
    public static final int COMPONENT_FIRST             = 100;

    /**
     * 用于组件事件的 id 范围中的最后一个数字。
     */
    public static final int COMPONENT_LAST              = 103;

   /**
     * 此事件表示组件的位置已更改。
     */
    @Native public static final int COMPONENT_MOVED     = COMPONENT_FIRST;

    /**
     * 此事件表示组件的大小已更改。
     */
    @Native public static final int COMPONENT_RESIZED   = 1 + COMPONENT_FIRST;

    /**
     * 此事件表示组件已变为可见。
     */
    @Native public static final int COMPONENT_SHOWN     = 2 + COMPONENT_FIRST;

    /**
     * 此事件表示组件已变为不可见。
     */
    @Native public static final int COMPONENT_HIDDEN    = 3 + COMPONENT_FIRST;

    /*
     * JDK 1.1 序列化版本 ID
     */
    private static final long serialVersionUID = 8101406823902992965L;

    /**
     * 构造一个 <code>ComponentEvent</code> 对象。
     * <p> 如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source 事件的来源 <code>Component</code>
     * @param id     表示事件类型的整数。
     *                     有关允许值的信息，请参阅 {@link ComponentEvent} 的类描述
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getComponent()
     * @see #getID()
     */
    public ComponentEvent(Component source, int id) {
        super(source, id);
    }

    /**
     * 返回事件的来源。
     *
     * @return 事件的来源 <code>Component</code> 对象，如果对象不是 <code>Component</code>，则返回 <code>null</code>。
     */
    public Component getComponent() {
        return (source instanceof Component) ? (Component)source : null;
    }

    /**
     * 返回一个标识此事件的参数字符串。
     * 此方法对于事件记录和调试非常有用。
     *
     * @return 一个标识事件及其属性的字符串
     */
    public String paramString() {
        String typeStr;
        Rectangle b = (source != null
                       ? ((Component)source).getBounds()
                       : null);

        switch(id) {
          case COMPONENT_SHOWN:
              typeStr = "COMPONENT_SHOWN";
              break;
          case COMPONENT_HIDDEN:
              typeStr = "COMPONENT_HIDDEN";
              break;
          case COMPONENT_MOVED:
              typeStr = "COMPONENT_MOVED ("+
                         b.x+","+b.y+" "+b.width+"x"+b.height+")";
              break;
          case COMPONENT_RESIZED:
              typeStr = "COMPONENT_RESIZED ("+
                         b.x+","+b.y+" "+b.width+"x"+b.height+")";
              break;
          default:
              typeStr = "unknown type";
        }
        return typeStr;
    }
}
