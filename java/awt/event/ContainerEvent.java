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

import java.awt.Container;
import java.awt.Component;

/**
 * 一个低级事件，表示容器的内容发生了变化，因为添加或移除了组件。
 * <P>
 * 容器事件仅用于通知目的；
 * AWT 将自动处理容器内容的变化，以确保程序正常运行，无论程序是否接收这些事件。
 * <P>
 * 当组件被添加到容器或从容器中移除时，此类低级事件由容器对象（如 Panel）生成。
 * 该事件传递给每个使用组件的 <code>addContainerListener</code> 方法注册以接收此类事件的
 * <code>ContainerListener</code> 或 <code>ContainerAdapter</code> 对象。
 * （<code>ContainerAdapter</code> 对象实现了 <code>ContainerListener</code> 接口。）
 * 每个这样的监听器对象在事件发生时都会收到此 <code>ContainerEvent</code>。
 * <p>
 * 如果任何特定的 <code>ContainerEvent</code> 实例的 {@code id} 参数不在
 * {@code CONTAINER_FIRST} 到 {@code CONTAINER_LAST} 的范围内，则会导致未指定的行为。
 *
 * @see ContainerAdapter
 * @see ContainerListener
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/containerlistener.html">教程：编写容器监听器</a>
 *
 * @author Tim Prinzing
 * @author Amy Fowler
 * @since 1.1
 */
public class ContainerEvent extends ComponentEvent {

    /**
     * 用于容器事件的 ID 范围中的第一个数字。
     */
    public static final int CONTAINER_FIRST             = 300;

    /**
     * 用于容器事件的 ID 范围中的最后一个数字。
     */
    public static final int CONTAINER_LAST              = 301;

   /**
     * 此事件表示组件被添加到容器中。
     */
    public static final int COMPONENT_ADDED     = CONTAINER_FIRST;

    /**
     * 此事件表示组件从容器中移除。
     */
    public static final int COMPONENT_REMOVED = 1 + CONTAINER_FIRST;

    /**
     * 被添加或从容器中移除的非空组件。
     *
     * @serial
     * @see #getChild()
     */
    Component child;

    /*
     * JDK 1.1 序列化版本 ID
     */
    private static final long serialVersionUID = -4114942250539772041L;

    /**
     * 构造一个 <code>ContainerEvent</code> 对象。
     * <p> 如果 <code>source</code> 为 <code>null</code>，则此方法会抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source 生成事件的 <code>Component</code> 对象（容器）
     * @param id     表示事件类型的整数。
     *               有关允许值的信息，请参见 {@link ContainerEvent} 的类描述。
     * @param child  被添加或移除的组件
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getContainer()
     * @see #getID()
     * @see #getChild()
     */
    public ContainerEvent(Component source, int id, Component child) {
        super(source, id);
        this.child = child;
    }

    /**
     * 返回事件的发起者。
     *
     * @return 生成事件的 <code>Container</code> 对象，如果不是 <code>Container</code> 则返回 <code>null</code>。
     */
    public Container getContainer() {
        return (source instanceof Container) ? (Container)source : null;
    }

    /**
     * 返回受事件影响的组件。
     *
     * @return 被添加或移除的 Component 对象
     */
    public Component getChild() {
        return child;
    }

    /**
     * 返回一个标识此事件的参数字符串。
     * 此方法对于事件记录和调试非常有用。
     *
     * @return 一个标识事件及其属性的字符串
     */
    public String paramString() {
        String typeStr;
        switch(id) {
          case COMPONENT_ADDED:
              typeStr = "COMPONENT_ADDED";
              break;
          case COMPONENT_REMOVED:
              typeStr = "COMPONENT_REMOVED";
              break;
          default:
              typeStr = "未知类型";
        }
        return typeStr + ",child="+child.getName();
    }
}
