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
import sun.awt.AppContext;
import sun.awt.SunToolkit;

/**
 * 一个低级事件，表示组件获得了或失去了输入焦点。此低级事件由组件（如文本字段）生成。事件将传递给每个使用组件的 <code>addFocusListener</code> 方法注册以接收此类事件的 <code>FocusListener</code> 或 <code>FocusAdapter</code> 对象。（<code>FocusAdapter</code> 对象实现了 <code>FocusListener</code> 接口。）每个这样的监听器对象在事件发生时都会收到此 <code>FocusEvent</code>。
 * <p>
 * 焦点事件有两个级别：永久和临时。永久焦点更改事件发生在焦点直接从一个组件移动到另一个组件时，例如通过调用 <code>requestFocus()</code> 或用户使用 TAB 键遍历组件。临时焦点更改事件发生在组件因其他操作而暂时失去焦点时，例如窗口去激活或滚动条拖动。在这种情况下，一旦该操作完成，或者对于窗口去激活的情况，当窗口重新激活时，原始焦点状态将自动恢复。永久和临时焦点事件都使用 FOCUS_GAINED 和 FOCUS_LOST 事件 ID 传递；可以使用 <code>isTemporary()</code> 方法在事件中区分这两个级别。
 * <p>
 * 如果任何特定 <code>FocusEvent</code> 实例的 <code>id</code> 参数不在从 <code>FOCUS_FIRST</code> 到 <code>FOCUS_LAST</code> 的范围内，将导致未指定的行为。
 *
 * @see FocusAdapter
 * @see FocusListener
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/focuslistener.html">教程：编写焦点监听器</a>
 *
 * @author Carl Quinn
 * @author Amy Fowler
 * @since 1.1
 */
public class FocusEvent extends ComponentEvent {

    /**
     * 用于焦点事件的 ID 范围中的第一个数字。
     */
    public static final int FOCUS_FIRST         = 1004;

    /**
     * 用于焦点事件的 ID 范围中的最后一个数字。
     */
    public static final int FOCUS_LAST          = 1005;

    /**
     * 此事件表示组件现在是焦点所有者。
     */
    public static final int FOCUS_GAINED = FOCUS_FIRST; //Event.GOT_FOCUS

    /**
     * 此事件表示组件不再是焦点所有者。
     */
    public static final int FOCUS_LOST = 1 + FOCUS_FIRST; //Event.LOST_FOCUS

    /**
     * 焦点事件可以有两个不同的级别，永久和临时。如果某个操作暂时带走了焦点并打算在事件完成时恢复焦点，则此值将设置为 true。否则将设置为 false。
     *
     * @serial
     * @see #isTemporary
     */
    boolean temporary;

    /**
     * 此焦点更改涉及的另一个组件。对于 FOCUS_GAINED 事件，这是失去焦点的组件。对于 FOCUS_LOST 事件，这是获得焦点的组件。如果此焦点更改发生在本机应用程序、不同 VM 中的 Java 应用程序或没有其他组件的情况下，则相反的组件为 null。
     *
     * @see #getOppositeComponent
     * @since 1.4
     */
    transient Component opposite;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = 523753786457416396L;

    /**
     * 使用指定的临时状态和相反的 <code>Component</code> 构造 <code>FocusEvent</code> 对象。相反的 <code>Component</code> 是此焦点更改涉及的另一个 <code>Component</code>。对于 <code>FOCUS_GAINED</code> 事件，这是失去焦点的 <code>Component</code>。对于 <code>FOCUS_LOST</code> 事件，这是获得焦点的 <code>Component</code>。如果此焦点更改发生在本机应用程序、不同 VM 中的 Java 应用程序或没有其他 <code>Component</code> 的情况下，则相反的 <code>Component</code> 为 <code>null</code>。
     * <p> 如果 <code>source</code> 为 <code>null</code>，此方法将抛出 <code>IllegalArgumentException</code>。
     *
     * @param source     事件的源 <code>Component</code>
     * @param id         表示事件类型的整数。有关允许值的信息，请参阅 {@link FocusEvent} 的类描述
     * @param temporary  如果焦点更改是临时的，则为 <code>true</code>；否则为 <code>false</code>
     * @param opposite   焦点更改涉及的另一个组件，或 <code>null</code>
     * @throws IllegalArgumentException 如果 <code>source</code> 等于 {@code null}
     * @see #getSource()
     * @see #getID()
     * @see #isTemporary()
     * @see #getOppositeComponent()
     * @since 1.4
     */
    public FocusEvent(Component source, int id, boolean temporary,
                      Component opposite) {
        super(source, id);
        this.temporary = temporary;
        this.opposite = opposite;
    }

    /**
     * 构造 <code>FocusEvent</code> 对象并标识更改是否为临时的。
     * <p> 如果 <code>source</code> 为 <code>null</code>，此方法将抛出 <code>IllegalArgumentException</code>。
     *
     * @param source    事件的源 <code>Component</code>
     * @param id        表示事件类型的整数。有关允许值的信息，请参阅 {@link FocusEvent} 的类描述
     * @param temporary 如果焦点更改是临时的，则为 <code>true</code>；否则为 <code>false</code>
     * @throws IllegalArgumentException 如果 <code>source</code> 等于 {@code null}
     * @see #getSource()
     * @see #getID()
     * @see #isTemporary()
     */
    public FocusEvent(Component source, int id, boolean temporary) {
        this(source, id, temporary, null);
    }

    /**
     * 构造 <code>FocusEvent</code> 对象并将其标识为永久的焦点更改。
     * <p> 如果 <code>source</code> 为 <code>null</code>，此方法将抛出 <code>IllegalArgumentException</code>。
     *
     * @param source    事件的源 <code>Component</code>
     * @param id        表示事件类型的整数。有关允许值的信息，请参阅 {@link FocusEvent} 的类描述
     * @throws IllegalArgumentException 如果 <code>source</code> 等于 {@code null}
     * @see #getSource()
     * @see #getID()
     */
    public FocusEvent(Component source, int id) {
        this(source, id, false);
    }

    /**
     * 标识焦点更改事件是临时的还是永久的。
     *
     * @return 如果焦点更改是临时的，则为 <code>true</code>；否则为 <code>false</code>
     */
    public boolean isTemporary() {
        return temporary;
    }

    /**
     * 返回此焦点更改涉及的另一个组件。对于 FOCUS_GAINED 事件，这是失去焦点的组件。对于 FOCUS_LOST 事件，这是获得焦点的组件。如果此焦点更改发生在本机应用程序、不同 VM 中的 Java 应用程序或没有其他组件的情况下，则返回 null。
     *
     * @return 此焦点更改涉及的另一个组件，或 null
     * @since 1.4
     */
    public Component getOppositeComponent() {
        if (opposite == null) {
            return null;
        }

        return (SunToolkit.targetToAppContext(opposite) ==
                AppContext.getAppContext())
            ? opposite
            : null;
    }

    /**
     * 返回一个标识此事件的参数字符串。此方法对于事件记录和调试非常有用。
     *
     * @return 一个标识事件及其属性的字符串
     */
    public String paramString() {
        String typeStr;
        switch(id) {
          case FOCUS_GAINED:
              typeStr = "FOCUS_GAINED";
              break;
          case FOCUS_LOST:
              typeStr = "FOCUS_LOST";
              break;
          default:
              typeStr = "unknown type";
        }
        return typeStr + (temporary ? ",temporary" : ",permanent") +
            ",opposite=" + getOppositeComponent();
    }

}
