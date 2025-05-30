
/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Native;

/**
 * 一个事件，表示鼠标滚轮在一个组件中被旋转。
 * <P>
 * 滚轮鼠标是一种在中间按钮位置有一个滚轮的鼠标。这个滚轮可以向用户方向或远离用户方向旋转。鼠标滚轮通常用于滚动，但也可以有其他用途。
 * <P>
 * 一个 MouseWheelEvent 对象会被传递给每一个使用组件的 <code>addMouseWheelListener</code> 方法注册以接收“有趣”的鼠标事件的 <code>MouseWheelListener</code> 对象。每个这样的监听器对象都会收到一个包含鼠标事件的 <code>MouseEvent</code>。
 * <P>
 * 由于鼠标滚轮与滚动组件的特殊关系，MouseWheelEvents 的传递方式与其他 MouseEvents 有所不同。这是因为其他鼠标事件通常会直接影响鼠标光标下的组件（例如，点击按钮），而 MouseWheelEvents 通常会在远离鼠标光标的地方产生影响（在 ScrollPane 内的一个组件上移动滚轮应该滚动 ScrollPane 的一个滚动条）。
 * <P>
 * MouseWheelEvents 从鼠标光标下的组件开始传递。如果组件上没有启用 MouseWheelEvents，事件将传递给第一个启用了 MouseWheelEvents 的祖先容器。这通常是启用了滚轮滚动的 ScrollPane。事件的源组件和 x, y 坐标将相对于事件的最终目的地（ScrollPane）。这允许将复杂的 GUI 无需修改地安装到 ScrollPane 中，并且所有 MouseWheelEvents 都会被传递到 ScrollPane 以进行滚动。
 * <P>
 * 一些 AWT 组件是使用显示自己滚动条并处理自己滚动的原生小部件实现的。这些组件的具体情况会因平台而异。当鼠标滚轮在这些组件上移动时，事件会直接传递给原生小部件，而不会传播到祖先组件。
 * <P>
 * 平台提供了对鼠标滚轮移动时应滚动的量的自定义。最常见的两种设置是滚动一定数量的“单位”（通常是在基于文本的组件中的文本行）或整个“块”（类似于翻页键）。MouseWheelEvent 提供了符合底层平台设置的方法。这些平台设置可以随时由用户更改。MouseWheelEvents 反映了最新的设置。
 * <P>
 * <code>MouseWheelEvent</code> 类包括获取鼠标滚轮旋转的“点击”次数的方法。{@link #getWheelRotation} 方法返回与滚轮旋转的刻度数相对应的整数“点击”次数。除了这个方法，<code>MouseWheelEvent</code> 类还提供了 {@link #getPreciseWheelRotation} 方法，该方法返回一个双精度“点击”次数，以处理部分旋转的情况。如果鼠标支持高分辨率滚轮（如自由旋转的滚轮，没有刻度），则 {@link #getPreciseWheelRotation} 方法非常有用。应用程序可以利用此方法更精确地处理鼠标滚轮事件，从而使得视觉感知更加平滑。
 *
 * @author Brent Christian
 * @see MouseWheelListener
 * @see java.awt.ScrollPane
 * @see java.awt.ScrollPane#setWheelScrollingEnabled(boolean)
 * @see javax.swing.JScrollPane
 * @see javax.swing.JScrollPane#setWheelScrollingEnabled(boolean)
 * @since 1.4
 */

public class MouseWheelEvent extends MouseEvent {

    /**
     * 表示按“单位”滚动（类似于使用箭头键滚动）
     *
     * @see #getScrollType
     */
    @Native public static final int WHEEL_UNIT_SCROLL = 0;

    /**
     * 表示按“块”滚动（类似于使用翻页键滚动）
     *
     * @see #getScrollType
     */
    @Native public static final int WHEEL_BLOCK_SCROLL = 1;

    /**
     * 表示基于平台设置，响应此事件应进行的滚动类型。合法值为：
     * <ul>
     * <li> WHEEL_UNIT_SCROLL
     * <li> WHEEL_BLOCK_SCROLL
     * </ul>
     *
     * @see #getScrollType
     */
    int scrollType;

    /**
     * 仅在 scrollType 为 WHEEL_UNIT_SCROLL 时有效。
     * 表示基于平台设置，每次鼠标滚轮旋转应滚动的单位数。
     *
     * @see #getScrollAmount
     * @see #getScrollType
     */
    int scrollAmount;

    /**
     * 表示鼠标滚轮旋转的距离。
     *
     * @see #getWheelRotation
     */
    int wheelRotation;

    /**
     * 表示鼠标滚轮旋转的距离。
     *
     * @see #getPreciseWheelRotation
     */
    double preciseWheelRotation;

    /*
     * serialVersionUID
     */

    private static final long serialVersionUID = 6459879390515399677L;

    /**
     * 使用指定的源组件、类型、修饰符、坐标、滚动类型、滚动量和滚轮旋转构造一个 <code>MouseWheelEvent</code> 对象。
     * <p>绝对坐标 xAbs 和 yAbs 设置为源组件在屏幕上的位置加上相对坐标 x 和 y。如果源组件未显示，则 xAbs 和 yAbs 设置为零。
     * <p>注意，传递无效的 <code>id</code> 会导致未指定的行为。如果 <code>source</code> 为 <code>null</code>，此方法将抛出 <code>IllegalArgumentException</code>。
     *
     * @param source         事件的源 <code>Component</code>
     * @param id             识别事件的整数
     * @param when           事件发生的时间
     * @param modifiers      事件发生时按下的修饰键（shift, ctrl, alt, meta）
     * @param x              鼠标位置的水平 x 坐标
     * @param y              鼠标位置的垂直 y 坐标
     * @param clickCount     与事件关联的鼠标点击次数
     * @param popupTrigger   如果此事件是弹出菜单的触发器，则为 true
     * @param scrollType     响应此事件应进行的滚动类型；合法值为 <code>WHEEL_UNIT_SCROLL</code> 和 <code>WHEEL_BLOCK_SCROLL</code>
     * @param  scrollAmount  对于 scrollType <code>WHEEL_UNIT_SCROLL</code>，应滚动的单位数
     * @param wheelRotation  鼠标滚轮旋转的整数“点击”次数
     *
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see MouseEvent#MouseEvent(java.awt.Component, int, long, int, int, int, int, boolean)
     * @see MouseEvent#MouseEvent(java.awt.Component, int, long, int, int, int, int, int, int, boolean, int)
     */
    public MouseWheelEvent (Component source, int id, long when, int modifiers,
                      int x, int y, int clickCount, boolean popupTrigger,
                      int scrollType, int scrollAmount, int wheelRotation) {

        this(source, id, when, modifiers, x, y, 0, 0, clickCount,
             popupTrigger, scrollType, scrollAmount, wheelRotation);
    }

    /**
     * 使用指定的源组件、类型、修饰符、坐标、绝对坐标、滚动类型、滚动量和滚轮旋转构造一个 <code>MouseWheelEvent</code> 对象。
     * <p>注意，传递无效的 <code>id</code> 会导致未指定的行为。如果 <code>source</code> 为 <code>null</code>，此方法将抛出 <code>IllegalArgumentException</code>。<p>
     * 即使传递给构造函数的相对坐标和绝对坐标值不一致，仍然会创建 MouseWheelEvent 实例，并且不会抛出异常。
     *
     * @param source         事件的源 <code>Component</code>
     * @param id             识别事件的整数
     * @param when           事件发生的时间
     * @param modifiers      事件发生时按下的修饰键（shift, ctrl, alt, meta）
     * @param x              鼠标位置的水平 x 坐标
     * @param y              鼠标位置的垂直 y 坐标
     * @param xAbs           鼠标位置的绝对水平 x 坐标
     * @param yAbs           鼠标位置的绝对垂直 y 坐标
     * @param clickCount     与事件关联的鼠标点击次数
     * @param popupTrigger   如果此事件是弹出菜单的触发器，则为 true
     * @param scrollType     响应此事件应进行的滚动类型；合法值为 <code>WHEEL_UNIT_SCROLL</code> 和 <code>WHEEL_BLOCK_SCROLL</code>
     * @param  scrollAmount  对于 scrollType <code>WHEEL_UNIT_SCROLL</code>，应滚动的单位数
     * @param wheelRotation  鼠标滚轮旋转的整数“点击”次数
     *
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see MouseEvent#MouseEvent(java.awt.Component, int, long, int, int, int, int, boolean)
     * @see MouseEvent#MouseEvent(java.awt.Component, int, long, int, int, int, int, int, int, boolean, int)
     * @since 1.6
     */
    public MouseWheelEvent (Component source, int id, long when, int modifiers,
                            int x, int y, int xAbs, int yAbs, int clickCount, boolean popupTrigger,
                            int scrollType, int scrollAmount, int wheelRotation) {

        this(source, id, when, modifiers, x, y, xAbs, yAbs, clickCount, popupTrigger,
             scrollType, scrollAmount, wheelRotation, wheelRotation);

    }


    /**
     * 使用指定的源组件、类型、修饰符、坐标、绝对坐标、滚动类型、滚动量和滚轮旋转构造一个 <code>MouseWheelEvent</code> 对象。
     * <p>注意，传递无效的 <code>id</code> 参数会导致未指定的行为。如果 <code>source</code> 等于 <code>null</code>，此方法将抛出 <code>IllegalArgumentException</code>。
     * <p>即使传递给构造函数的相对坐标和绝对坐标值不一致，仍然会创建 <code>MouseWheelEvent</code> 实例，并且不会抛出异常。
     *
     * @param source         事件的源 <code>Component</code>
     * @param id             识别事件的整数值
     * @param when           事件发生的时间
     * @param modifiers      事件发生时按下的修饰键（shift, ctrl, alt, meta）
     * @param x              鼠标位置的水平 <code>x</code> 坐标
     * @param y              鼠标位置的垂直 <code>y</code> 坐标
     * @param xAbs           鼠标位置的绝对水平 <code>x</code> 坐标
     * @param yAbs           鼠标位置的绝对垂直 <code>y</code> 坐标
     * @param clickCount     与事件关联的鼠标点击次数
     * @param popupTrigger   如果此事件是弹出菜单的触发器，则为 true
     * @param scrollType     响应此事件应进行的滚动类型；合法值为 <code>WHEEL_UNIT_SCROLL</code> 和 <code>WHEEL_BLOCK_SCROLL</code>
     * @param  scrollAmount  对于 scrollType <code>WHEEL_UNIT_SCROLL</code>，应滚动的单位数
     * @param wheelRotation  鼠标滚轮旋转的整数“点击”次数
     * @param preciseWheelRotation 鼠标滚轮旋转的双精度“点击”次数
     *
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see MouseEvent#MouseEvent(java.awt.Component, int, long, int, int, int, int, boolean)
     * @see MouseEvent#MouseEvent(java.awt.Component, int, long, int, int, int, int, int, int, boolean, int)
     * @since 1.7
     */
    public MouseWheelEvent (Component source, int id, long when, int modifiers,
                            int x, int y, int xAbs, int yAbs, int clickCount, boolean popupTrigger,
                            int scrollType, int scrollAmount, int wheelRotation, double preciseWheelRotation) {

        super(source, id, when, modifiers, x, y, xAbs, yAbs, clickCount,
              popupTrigger, MouseEvent.NOBUTTON);

        this.scrollType = scrollType;
        this.scrollAmount = scrollAmount;
        this.wheelRotation = wheelRotation;
        this.preciseWheelRotation = preciseWheelRotation;

    }

    /**
     * 返回响应此事件应进行的滚动类型。这是由原生平台确定的。合法值为：
     * <ul>
     * <li> MouseWheelEvent.WHEEL_UNIT_SCROLL
     * <li> MouseWheelEvent.WHEEL_BLOCK_SCROLL
     * </ul>
     *
     * @return 依赖于原生平台配置的 MouseWheelEvent.WHEEL_UNIT_SCROLL 或 MouseWheelEvent.WHEEL_BLOCK_SCROLL。
     * @see java.awt.Adjustable#getUnitIncrement
     * @see java.awt.Adjustable#getBlockIncrement
     * @see javax.swing.Scrollable#getScrollableUnitIncrement
     * @see javax.swing.Scrollable#getScrollableBlockIncrement
     */
    public int getScrollType() {
        return scrollType;
    }


                /**
     * 返回每次鼠标滚轮旋转应滚动的单位数。
     * 仅在 <code>getScrollType</code> 返回
     * <code>MouseWheelEvent.WHEEL_UNIT_SCROLL</code> 时有效。
     *
     * @return 要滚动的单位数，如果 <code>getScrollType</code> 返回
     *  <code>MouseWheelEvent.WHEEL_BLOCK_SCROLL</code>，则返回未定义的值
     * @see #getScrollType
     */
    public int getScrollAmount() {
        return scrollAmount;
    }

    /**
     * 返回鼠标滚轮旋转的“点击”次数，作为整数。
     * 如果鼠标支持高分辨率滚轮，可能会发生部分旋转。
     * 在这种情况下，方法将返回零，直到累积了一个完整的“点击”。
     *
     * @return 如果鼠标滚轮向上/远离用户旋转，则返回负值；
     * 如果鼠标滚轮向下/朝向用户旋转，则返回正值
     * @see #getPreciseWheelRotation
     */
    public int getWheelRotation() {
        return wheelRotation;
    }

    /**
     * 返回鼠标滚轮旋转的“点击”次数，作为双精度浮点数。
     * 如果鼠标支持高分辨率滚轮，可能会发生部分旋转。
     * 在这种情况下，返回值将包括一个分数“点击”。
     *
     * @return 如果鼠标滚轮向上或远离用户旋转，则返回负值；
     * 如果鼠标滚轮向下或朝向用户旋转，则返回正值
     * @see #getWheelRotation
     * @since 1.7
     */
    public double getPreciseWheelRotation() {
        return preciseWheelRotation;
    }

    /**
     * 这是一个方便的方法，用于实现常见的 MouseWheelListener - 以符合平台设置的量滚动 ScrollPane 或
     * JScrollPane。请注意，<code>ScrollPane</code> 和
     * <code>JScrollPane</code> 已经内置了此功能。
     * <P>
     * 当滚动类型为 MouseWheelEvent.WHEEL_UNIT_SCROLL 时，此方法返回滚动的单位数，
     * 仅在 <code>getScrollType</code> 返回 MouseWheelEvent.WHEEL_UNIT_SCROLL 时调用。
     * <P>
     * 滚动方向、滚轮移动量和平台滚轮滚动设置都已考虑在内。
     * 此方法不考虑 Adjustable/Scrollable 单位增量的值，因为这在不同的滚动组件中会有所不同。
     * <P>
     * 以下是如何在监听器中使用此方法的一个简化示例：
     * <pre>
     *  mouseWheelMoved(MouseWheelEvent event) {
     *      ScrollPane sp = getScrollPaneFromSomewhere();
     *      Adjustable adj = sp.getVAdjustable()
     *      if (MouseWheelEvent.getScrollType() == WHEEL_UNIT_SCROLL) {
     *          int totalScrollAmount =
     *              event.getUnitsToScroll() *
     *              adj.getUnitIncrement();
     *          adj.setValue(adj.getValue() + totalScrollAmount);
     *      }
     *  }
     * </pre>
     *
     * @return 基于鼠标滚轮旋转的方向和量以及本机平台滚轮滚动设置的滚动单位数
     * @see #getScrollType
     * @see #getScrollAmount
     * @see MouseWheelListener
     * @see java.awt.Adjustable
     * @see java.awt.Adjustable#getUnitIncrement
     * @see javax.swing.Scrollable
     * @see javax.swing.Scrollable#getScrollableUnitIncrement
     * @see java.awt.ScrollPane
     * @see java.awt.ScrollPane#setWheelScrollingEnabled
     * @see javax.swing.JScrollPane
     * @see javax.swing.JScrollPane#setWheelScrollingEnabled
     */
    public int getUnitsToScroll() {
        return scrollAmount * wheelRotation;
    }

    /**
     * 返回一个标识此事件的参数字符串。
     * 此方法对于事件记录和调试非常有用。
     *
     * @return 一个标识事件及其属性的字符串
     */
    public String paramString() {
        String scrollTypeStr = null;

        if (getScrollType() == WHEEL_UNIT_SCROLL) {
            scrollTypeStr = "WHEEL_UNIT_SCROLL";
        }
        else if (getScrollType() == WHEEL_BLOCK_SCROLL) {
            scrollTypeStr = "WHEEL_BLOCK_SCROLL";
        }
        else {
            scrollTypeStr = "unknown scroll type";
        }
        return super.paramString()+",scrollType="+scrollTypeStr+
         ",scrollAmount="+getScrollAmount()+",wheelRotation="+
         getWheelRotation()+",preciseWheelRotation="+getPreciseWheelRotation();
    }
}
