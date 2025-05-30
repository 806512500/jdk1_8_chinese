
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

import java.io.*;
import java.lang.*;
import java.util.*;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;

/**
 * <code>Graphics</code> 类是所有允许应用程序在各种设备上实现的组件以及离屏图像上绘制的图形上下文的抽象基类。
 * <p>
 * <code>Graphics</code> 对象封装了 Java 支持的基本渲染操作所需的状态信息。这些状态信息包括以下属性：
 *
 * <ul>
 * <li>要绘制的 <code>Component</code> 对象。
 * <li>用于渲染和裁剪坐标的平移原点。
 * <li>当前裁剪区域。
 * <li>当前颜色。
 * <li>当前字体。
 * <li>当前逻辑像素操作函数（XOR 或 Paint）。
 * <li>当前 XOR 交替颜色（参见 {@link Graphics#setXORMode}）。
 * </ul>
 * <p>
 * 坐标是无限薄的，位于输出设备的像素之间。
 * 绘制图形轮廓的操作通过在路径上无限薄的路径上移动一个像素大小的笔来完成，该笔悬挂在路径上的锚点下方和右侧。
 * 填充图形的操作通过填充该无限薄路径的内部来完成。
 * 水平文本的渲染操作将字符字形的上升部分完全绘制在基线坐标之上。
 * <p>
 * 图形笔悬挂在它遍历的路径下方和右侧。这有以下含义：
 * <ul>
 * <li>如果您绘制一个覆盖给定矩形的图形，该图形在右边缘和底边缘上比填充一个由该相同矩形限定的图形多出一行像素。
 * <li>如果您沿与文本行基线相同的 <i>y</i> 坐标绘制水平线，该线将完全绘制在文本下方，除了任何降部。
 * </ul><p>
 * 作为此 <code>Graphics</code> 对象方法调用参数出现的所有坐标都被视为相对于此 <code>Graphics</code> 对象调用方法之前的平移原点。
 * <p>
 * 所有渲染操作仅修改位于当前裁剪区域内的像素，该裁剪区域由用户空间中的 {@link Shape} 定义，并由使用 <code>Graphics</code> 对象的程序控制。此 <i>用户裁剪</i>
 * 被转换为设备空间并与 <i>设备裁剪</i> 结合，后者由窗口可见性和设备范围定义。用户裁剪和设备裁剪的组合定义了 <i>复合裁剪</i>，确定了最终的裁剪区域。用户裁剪不能由渲染
 * 系统修改以反映结果的复合裁剪。用户裁剪只能通过 <code>setClip</code> 或 <code>clipRect</code> 方法更改。
 * 所有绘制或书写操作都使用当前颜色、当前绘制模式和当前字体进行。
 *
 * @author      Sami Shaio
 * @author      Arthur van Hoff
 * @see     java.awt.Component
 * @see     java.awt.Graphics#clipRect(int, int, int, int)
 * @see     java.awt.Graphics#setColor(java.awt.Color)
 * @see     java.awt.Graphics#setPaintMode()
 * @see     java.awt.Graphics#setXORMode(java.awt.Color)
 * @see     java.awt.Graphics#setFont(java.awt.Font)
 * @since       JDK1.0
 */
public abstract class Graphics {

    /**
     * 构造一个新的 <code>Graphics</code> 对象。
     * 这是图形上下文的默认构造函数。
     * <p>
     * 由于 <code>Graphics</code> 是一个抽象类，应用程序不能直接调用此构造函数。图形上下文是从其他图形上下文获取的，或者通过调用组件的 <code>getGraphics</code> 方法创建的。
     * @see        java.awt.Graphics#create()
     * @see        java.awt.Component#getGraphics
     */
    protected Graphics() {
    }

    /**
     * 创建一个新的 <code>Graphics</code> 对象，该对象是此 <code>Graphics</code> 对象的副本。
     * @return     一个新的图形上下文，它是此图形上下文的副本。
     */
    public abstract Graphics create();

    /**
     * 基于此 <code>Graphics</code> 对象创建一个新的 <code>Graphics</code> 对象，但具有新的平移和裁剪区域。
     * 新的 <code>Graphics</code> 对象的原点平移到指定点 (<i>x</i>,&nbsp;<i>y</i>)。
     * 其裁剪区域由原始裁剪区域与指定矩形的交集确定。参数都在原始 <code>Graphics</code> 对象的坐标系中解释。新的图形上下文与原始图形上下文相同，但有两个不同之处：
     *
     * <ul>
     * <li>
     * 新的图形上下文平移了 (<i>x</i>,&nbsp;<i>y</i>)。也就是说，新图形上下文中的点 (<code>0</code>,&nbsp;<code>0</code>) 与原始图形上下文中的点 (<i>x</i>,&nbsp;<i>y</i>) 相同。
     * <li>
     * 新的图形上下文有一个额外的裁剪矩形，除了从原始图形上下文继承的（平移后的）裁剪矩形之外。新裁剪矩形的原点位于 (<code>0</code>,&nbsp;<code>0</code>)，其大小由 <code>width</code> 和 <code>height</code> 参数指定。
     * </ul>
     * <p>
     * @param      x   <i>x</i> 坐标。
     * @param      y   <i>y</i> 坐标。
     * @param      width   裁剪矩形的宽度。
     * @param      height   裁剪矩形的高度。
     * @return     一个新的图形上下文。
     * @see        java.awt.Graphics#translate
     * @see        java.awt.Graphics#clipRect
     */
    public Graphics create(int x, int y, int width, int height) {
        Graphics g = create();
        if (g == null) return null;
        g.translate(x, y);
        g.clipRect(0, 0, width, height);
        return g;
    }

    /**
     * 将图形上下文的原点平移到当前坐标系中的点 (<i>x</i>,&nbsp;<i>y</i>)。
     * 修改此图形上下文，使其新原点对应于此图形上下文原始坐标系中的点 (<i>x</i>,&nbsp;<i>y</i>)。所有后续在此图形上下文上的渲染操作将相对于此新原点。
     * @param  x   <i>x</i> 坐标。
     * @param  y   <i>y</i> 坐标。
     */
    public abstract void translate(int x, int y);

    /**
     * 获取此图形上下文的当前颜色。
     * @return    此图形上下文的当前颜色。
     * @see       java.awt.Color
     * @see       java.awt.Graphics#setColor(Color)
     */
    public abstract Color getColor();

    /**
     * 将此图形上下文的当前颜色设置为指定颜色。所有后续使用此图形上下文的渲染操作将使用此指定颜色。
     * @param     c   新的渲染颜色。
     * @see       java.awt.Color
     * @see       java.awt.Graphics#getColor
     */
    public abstract void setColor(Color c);

    /**
     * 将此图形上下文的绘制模式设置为用此图形上下文的当前颜色覆盖目标。
     * 这将逻辑像素操作函数设置为绘制或覆盖模式。所有后续渲染操作将用当前颜色覆盖目标。
     */
    public abstract void setPaintMode();

    /**
     * 将此图形上下文的绘制模式设置为在当前颜色和新指定颜色之间交替。
     * 这指定逻辑像素操作在 XOR 模式下进行，即在当前颜色和指定的 XOR 颜色之间交替。
     * <p>
     * 在绘制操作中，当前颜色的像素将变为指定颜色，反之亦然。
     * <p>
     * 其他颜色的像素将以不可预测但可逆的方式改变；如果同一图形绘制两次，则所有像素将恢复到其原始值。
     * @param     c1  XOR 交替颜色
     */
    public abstract void setXORMode(Color c1);

    /**
     * 获取当前字体。
     * @return    此图形上下文的当前字体。
     * @see       java.awt.Font
     * @see       java.awt.Graphics#setFont(Font)
     */
    public abstract Font getFont();

    /**
     * 将此图形上下文的字体设置为指定字体。所有后续使用此图形上下文的文本操作将使用此字体。如果参数为 null，则静默忽略。
     * @param  font   字体。
     * @see     java.awt.Graphics#getFont
     * @see     java.awt.Graphics#drawString(java.lang.String, int, int)
     * @see     java.awt.Graphics#drawBytes(byte[], int, int, int, int)
     * @see     java.awt.Graphics#drawChars(char[], int, int, int, int)
    */
    public abstract void setFont(Font font);

    /**
     * 获取当前字体的字体度量。
     * @return    此图形上下文当前字体的字体度量。
     * @see       java.awt.Graphics#getFont
     * @see       java.awt.FontMetrics
     * @see       java.awt.Graphics#getFontMetrics(Font)
     */
    public FontMetrics getFontMetrics() {
        return getFontMetrics(getFont());
    }

    /**
     * 获取指定字体的字体度量。
     * @return    指定字体的字体度量。
     * @param     f 指定的字体
     * @see       java.awt.Graphics#getFont
     * @see       java.awt.FontMetrics
     * @see       java.awt.Graphics#getFontMetrics()
     */
    public abstract FontMetrics getFontMetrics(Font f);


    /**
     * 返回当前裁剪区域的边界矩形。
     * 此方法引用用户裁剪，该裁剪独立于设备边界和窗口可见性相关的裁剪。如果从未设置过裁剪，或者使用 <code>setClip(null)</code> 清除了裁剪，此方法返回 <code>null</code>。
     * 矩形中的坐标相对于此图形上下文的坐标系原点。
     * @return      当前裁剪区域的边界矩形，如果没有设置裁剪，则返回 <code>null</code>。
     * @see         java.awt.Graphics#getClip
     * @see         java.awt.Graphics#clipRect
     * @see         java.awt.Graphics#setClip(int, int, int, int)
     * @see         java.awt.Graphics#setClip(Shape)
     * @since       JDK1.1
     */
    public abstract Rectangle getClipBounds();

    /**
     * 将当前裁剪区域与指定矩形相交。
     * 结果裁剪区域是当前裁剪区域与指定矩形的交集。如果没有当前裁剪区域，或者裁剪已使用 <code>setClip(null)</code> 清除，指定的矩形将成为新的裁剪区域。
     * 此方法设置用户裁剪，该裁剪独立于设备边界和窗口可见性相关的裁剪。此方法只能用于使当前裁剪区域变小。要将当前裁剪区域变大，使用任何 setClip 方法。
     * 在裁剪区域之外的渲染操作无效。
     * @param x 要与裁剪区域相交的矩形的 <i>x</i> 坐标。
     * @param y 要与裁剪区域相交的矩形的 <i>y</i> 坐标。
     * @param width 要与裁剪区域相交的矩形的宽度。
     * @param height 要与裁剪区域相交的矩形的高度。
     * @see #setClip(int, int, int, int)
     * @see #setClip(Shape)
     */
    public abstract void clipRect(int x, int y, int width, int height);

    /**
     * 将当前裁剪区域设置为由给定坐标指定的矩形。此方法设置用户裁剪，该裁剪独立于设备边界和窗口可见性相关的裁剪。
     * 在裁剪区域之外的渲染操作无效。
     * @param       x 新裁剪矩形的 <i>x</i> 坐标。
     * @param       y 新裁剪矩形的 <i>y</i> 坐标。
     * @param       width 新裁剪矩形的宽度。
     * @param       height 新裁剪矩形的高度。
     * @see         java.awt.Graphics#clipRect
     * @see         java.awt.Graphics#setClip(Shape)
     * @see         java.awt.Graphics#getClip
     * @since       JDK1.1
     */
    public abstract void setClip(int x, int y, int width, int height);

    /**
     * 获取当前裁剪区域。
     * 此方法返回用户裁剪，该裁剪独立于设备边界和窗口可见性相关的裁剪。如果从未设置过裁剪，或者使用 <code>setClip(null)</code> 清除了裁剪，此方法返回 <code>null</code>。
     * @return      一个 <code>Shape</code> 对象，表示当前裁剪区域，如果没有设置裁剪，则返回 <code>null</code>。
     * @see         java.awt.Graphics#getClipBounds
     * @see         java.awt.Graphics#clipRect
     * @see         java.awt.Graphics#setClip(int, int, int, int)
     * @see         java.awt.Graphics#setClip(Shape)
     * @since       JDK1.1
     */
    public abstract Shape getClip();

    /**
     * 将当前裁剪区域设置为任意裁剪形状。
     * 并非所有实现 <code>Shape</code> 接口的对象都可以用于设置裁剪。唯一保证支持的 <code>Shape</code> 对象是通过 <code>getClip</code> 方法和 <code>Rectangle</code> 对象获得的 <code>Shape</code> 对象。此方法设置用户裁剪，该裁剪独立于设备边界和窗口可见性相关的裁剪。
     * @param clip 用于设置裁剪的 <code>Shape</code>。
     * @see         java.awt.Graphics#getClip()
     * @see         java.awt.Graphics#clipRect
     * @see         java.awt.Graphics#setClip(int, int, int, int)
     * @since       JDK1.1
     */
    public abstract void setClip(Shape clip);


                /**
     * 复制组件的指定区域，距离由 <code>dx</code> 和 <code>dy</code> 指定。
     * 从 <code>x</code> 和 <code>y</code> 指定的点开始，此方法
     * 向下和向右复制。要将组件的区域向左或向上复制，指定 <code>dx</code> 或 <code>dy</code> 的负值。
     * 如果源矩形的一部分位于组件边界之外，或被其他窗口或组件遮挡，
     * <code>copyArea</code> 将无法复制相关像素。可以通过调用组件的 <code>paint</code> 方法来刷新被省略的区域。
     * @param       x the <i>x</i> coordinate of the source rectangle.
     * @param       y the <i>y</i> coordinate of the source rectangle.
     * @param       width the width of the source rectangle.
     * @param       height the height of the source rectangle.
     * @param       dx the horizontal distance to copy the pixels.
     * @param       dy the vertical distance to copy the pixels.
     */
    public abstract void copyArea(int x, int y, int width, int height,
                                  int dx, int dy);

    /**
     * 使用当前颜色在此图形上下文的坐标系中绘制一条从点
     * <code>(x1,&nbsp;y1)</code> 到点 <code>(x2,&nbsp;y2)</code> 的线。
     * @param   x1  the first point's <i>x</i> coordinate.
     * @param   y1  the first point's <i>y</i> coordinate.
     * @param   x2  the second point's <i>x</i> coordinate.
     * @param   y2  the second point's <i>y</i> coordinate.
     */
    public abstract void drawLine(int x1, int y1, int x2, int y2);

    /**
     * 填充指定的矩形。
     * 矩形的左边缘和右边缘分别位于
     * <code>x</code> 和 <code>x&nbsp;+&nbsp;width&nbsp;-&nbsp;1</code>。
     * 矩形的上边缘和下边缘分别位于
     * <code>y</code> 和 <code>y&nbsp;+&nbsp;height&nbsp;-&nbsp;1</code>。
     * 结果矩形覆盖了一个
     * <code>width</code> 像素宽和
     * <code>height</code> 像素高的区域。
     * 矩形使用图形上下文的当前颜色填充。
     * @param         x   the <i>x</i> coordinate
     *                         of the rectangle to be filled.
     * @param         y   the <i>y</i> coordinate
     *                         of the rectangle to be filled.
     * @param         width   the width of the rectangle to be filled.
     * @param         height   the height of the rectangle to be filled.
     * @see           java.awt.Graphics#clearRect
     * @see           java.awt.Graphics#drawRect
     */
    public abstract void fillRect(int x, int y, int width, int height);

    /**
     * 绘制指定矩形的轮廓。
     * 矩形的左边缘和右边缘分别位于
     * <code>x</code> 和 <code>x&nbsp;+&nbsp;width</code>。
     * 矩形的上边缘和下边缘分别位于
     * <code>y</code> 和 <code>y&nbsp;+&nbsp;height</code>。
     * 矩形使用图形上下文的当前颜色绘制。
     * @param         x   the <i>x</i> coordinate
     *                         of the rectangle to be drawn.
     * @param         y   the <i>y</i> coordinate
     *                         of the rectangle to be drawn.
     * @param         width   the width of the rectangle to be drawn.
     * @param         height   the height of the rectangle to be drawn.
     * @see          java.awt.Graphics#fillRect
     * @see          java.awt.Graphics#clearRect
     */
    public void drawRect(int x, int y, int width, int height) {
        if ((width < 0) || (height < 0)) {
            return;
        }

        if (height == 0 || width == 0) {
            drawLine(x, y, x + width, y + height);
        } else {
            drawLine(x, y, x + width - 1, y);
            drawLine(x + width, y, x + width, y + height - 1);
            drawLine(x + width, y + height, x + 1, y + height);
            drawLine(x, y + height, x, y + 1);
        }
    }

    /**
     * 通过使用当前绘制表面的背景色填充指定矩形来清除该矩形。此操作不使用当前的绘制模式。
     * <p>
     * 从 Java&nbsp;1.1 开始，离屏图像的背景色可能是系统依赖的。应用程序应使用 <code>setColor</code> 跟随 <code>fillRect</code> 来确保离屏图像被清除为特定颜色。
     * @param       x the <i>x</i> coordinate of the rectangle to clear.
     * @param       y the <i>y</i> coordinate of the rectangle to clear.
     * @param       width the width of the rectangle to clear.
     * @param       height the height of the rectangle to clear.
     * @see         java.awt.Graphics#fillRect(int, int, int, int)
     * @see         java.awt.Graphics#drawRect
     * @see         java.awt.Graphics#setColor(java.awt.Color)
     * @see         java.awt.Graphics#setPaintMode
     * @see         java.awt.Graphics#setXORMode(java.awt.Color)
     */
    public abstract void clearRect(int x, int y, int width, int height);

    /**
     * 使用此图形上下文的当前颜色绘制指定的圆角矩形的轮廓。矩形的左边缘和右边缘分别位于 <code>x</code> 和 <code>x&nbsp;+&nbsp;width</code>。
     * 矩形的上边缘和下边缘分别位于
     * <code>y</code> 和 <code>y&nbsp;+&nbsp;height</code>。
     * @param      x the <i>x</i> coordinate of the rectangle to be drawn.
     * @param      y the <i>y</i> coordinate of the rectangle to be drawn.
     * @param      width the width of the rectangle to be drawn.
     * @param      height the height of the rectangle to be drawn.
     * @param      arcWidth the horizontal diameter of the arc
     *                    at the four corners.
     * @param      arcHeight the vertical diameter of the arc
     *                    at the four corners.
     * @see        java.awt.Graphics#fillRoundRect
     */
    public abstract void drawRoundRect(int x, int y, int width, int height,
                                       int arcWidth, int arcHeight);

    /**
     * 使用当前颜色填充指定的圆角矩形。
     * 矩形的左边缘和右边缘分别位于
     * <code>x</code> 和 <code>x&nbsp;+&nbsp;width&nbsp;-&nbsp;1</code>。
     * 矩形的上边缘和下边缘分别位于
     * <code>y</code> 和 <code>y&nbsp;+&nbsp;height&nbsp;-&nbsp;1</code>。
     * @param       x the <i>x</i> coordinate of the rectangle to be filled.
     * @param       y the <i>y</i> coordinate of the rectangle to be filled.
     * @param       width the width of the rectangle to be filled.
     * @param       height the height of the rectangle to be filled.
     * @param       arcWidth the horizontal diameter
     *                     of the arc at the four corners.
     * @param       arcHeight the vertical diameter
     *                     of the arc at the four corners.
     * @see         java.awt.Graphics#drawRoundRect
     */
    public abstract void fillRoundRect(int x, int y, int width, int height,
                                       int arcWidth, int arcHeight);

    /**
     * 绘制指定矩形的3D高亮轮廓。矩形的边缘被高亮处理，使其看起来像是从左上角被照亮的斜面。
     * <p>
     * 高亮效果的颜色基于当前颜色确定。
     * 结果矩形覆盖了一个
     * <code>width&nbsp;+&nbsp;1</code> 像素宽
     * 且 <code>height&nbsp;+&nbsp;1</code> 像素高的区域。
     * @param       x the <i>x</i> coordinate of the rectangle to be drawn.
     * @param       y the <i>y</i> coordinate of the rectangle to be drawn.
     * @param       width the width of the rectangle to be drawn.
     * @param       height the height of the rectangle to be drawn.
     * @param       raised a boolean that determines whether the rectangle
     *                      appears to be raised above the surface
     *                      or sunk into the surface.
     * @see         java.awt.Graphics#fill3DRect
     */
    public void draw3DRect(int x, int y, int width, int height,
                           boolean raised) {
        Color c = getColor();
        Color brighter = c.brighter();
        Color darker = c.darker();

        setColor(raised ? brighter : darker);
        drawLine(x, y, x, y + height);
        drawLine(x + 1, y, x + width - 1, y);
        setColor(raised ? darker : brighter);
        drawLine(x + 1, y + height, x + width, y + height);
        drawLine(x + width, y, x + width, y + height - 1);
        setColor(c);
    }

    /**
     * 使用当前颜色绘制一个3D高亮填充的矩形。矩形的边缘将被高亮处理，使其看起来像是从左上角被照亮的斜面。
     * 高亮效果的颜色将基于当前颜色确定。
     * @param       x the <i>x</i> coordinate of the rectangle to be filled.
     * @param       y the <i>y</i> coordinate of the rectangle to be filled.
     * @param       width the width of the rectangle to be filled.
     * @param       height the height of the rectangle to be filled.
     * @param       raised a boolean value that determines whether the
     *                      rectangle appears to be raised above the surface
     *                      or etched into the surface.
     * @see         java.awt.Graphics#draw3DRect
     */
    public void fill3DRect(int x, int y, int width, int height,
                           boolean raised) {
        Color c = getColor();
        Color brighter = c.brighter();
        Color darker = c.darker();

        if (!raised) {
            setColor(darker);
        }
        fillRect(x+1, y+1, width-2, height-2);
        setColor(raised ? brighter : darker);
        drawLine(x, y, x, y + height - 1);
        drawLine(x + 1, y, x + width - 2, y);
        setColor(raised ? darker : brighter);
        drawLine(x + 1, y + height - 1, x + width - 1, y + height - 1);
        drawLine(x + width - 1, y, x + width - 1, y + height - 2);
        setColor(c);
    }

    /**
     * 绘制一个椭圆的轮廓。
     * 结果是一个适合由 <code>x</code>、<code>y</code>、
     * <code>width</code> 和 <code>height</code> 参数指定的矩形的圆或椭圆。
     * <p>
     * 椭圆覆盖了一个
     * <code>width&nbsp;+&nbsp;1</code> 像素宽
     * 且 <code>height&nbsp;+&nbsp;1</code> 像素高的区域。
     * @param       x the <i>x</i> coordinate of the upper left
     *                     corner of the oval to be drawn.
     * @param       y the <i>y</i> coordinate of the upper left
     *                     corner of the oval to be drawn.
     * @param       width the width of the oval to be drawn.
     * @param       height the height of the oval to be drawn.
     * @see         java.awt.Graphics#fillOval
     */
    public abstract void drawOval(int x, int y, int width, int height);

    /**
     * 使用当前颜色填充由指定矩形限定的椭圆。
     * @param       x the <i>x</i> coordinate of the upper left corner
     *                     of the oval to be filled.
     * @param       y the <i>y</i> coordinate of the upper left corner
     *                     of the oval to be filled.
     * @param       width the width of the oval to be filled.
     * @param       height the height of the oval to be filled.
     * @see         java.awt.Graphics#drawOval
     */
    public abstract void fillOval(int x, int y, int width, int height);

    /**
     * 绘制一个覆盖指定矩形的圆形或椭圆形弧线的轮廓。
     * <p>
     * 结果弧线从 <code>startAngle</code> 开始，并沿当前颜色延伸 <code>arcAngle</code> 度。
     * 角度解释为 0&nbsp;度位于 3&nbsp;点钟位置。
     * 正值表示逆时针旋转，而负值表示顺时针旋转。
     * <p>
     * 弧线的中心是矩形的中心，该矩形的原点为 (<i>x</i>,&nbsp;<i>y</i>)，大小由
     * <code>width</code> 和 <code>height</code> 参数指定。
     * <p>
     * 结果弧线覆盖了一个
     * <code>width&nbsp;+&nbsp;1</code> 像素宽
     * 且 <code>height&nbsp;+&nbsp;1</code> 像素高的区域。
     * <p>
     * 角度相对于非正方形的边界矩形的范围指定，使得 45 度始终位于从椭圆中心到边界矩形右上角的线上。因此，如果边界矩形在某个轴上明显更长，弧线段的起始和结束角度将沿该轴的更长部分偏移。
     * @param        x the <i>x</i> coordinate of the
     *                    upper-left corner of the arc to be drawn.
     * @param        y the <i>y</i>  coordinate of the
     *                    upper-left corner of the arc to be drawn.
     * @param        width the width of the arc to be drawn.
     * @param        height the height of the arc to be drawn.
     * @param        startAngle the beginning angle.
     * @param        arcAngle the angular extent of the arc,
     *                    relative to the start angle.
     * @see         java.awt.Graphics#fillArc
     */
    public abstract void drawArc(int x, int y, int width, int height,
                                 int startAngle, int arcAngle);

    /**
     * 填充一个覆盖指定矩形的圆形或椭圆形弧线。
     * <p>
     * 结果弧线从 <code>startAngle</code> 开始，并沿 <code>arcAngle</code> 度延伸。
     * 角度解释为 0&nbsp;度位于 3&nbsp;点钟位置。
     * 正值表示逆时针旋转，而负值表示顺时针旋转。
     * <p>
     * 弧线的中心是矩形的中心，该矩形的原点为 (<i>x</i>,&nbsp;<i>y</i>)，大小由
     * <code>width</code> 和 <code>height</code> 参数指定。
     * <p>
     * 结果弧线覆盖了一个
     * <code>width&nbsp;+&nbsp;1</code> 像素宽
     * 且 <code>height&nbsp;+&nbsp;1</code> 像素高的区域。
     * <p>
     * 角度相对于非正方形的边界矩形的范围指定，使得 45 度始终位于从椭圆中心到边界矩形右上角的线上。因此，如果边界矩形在某个轴上明显更长，弧线段的起始和结束角度将沿该轴的更长部分偏移。
     * @param        x the <i>x</i> coordinate of the
     *                    upper-left corner of the arc to be filled.
     * @param        y the <i>y</i>  coordinate of the
     *                    upper-left corner of the arc to be filled.
     * @param        width the width of the arc to be filled.
     * @param        height the height of the arc to be filled.
     * @param        startAngle the beginning angle.
     * @param        arcAngle the angular extent of the arc,
     *                    relative to the start angle.
     * @see         java.awt.Graphics#drawArc
     */
    public abstract void fillArc(int x, int y, int width, int height,
                                 int startAngle, int arcAngle);


                /**
     * 绘制由 <i>x</i> 和 <i>y</i> 坐标数组定义的一系列连接的线段。
     * 每对 (<i>x</i>,&nbsp;<i>y</i>) 坐标定义一个点。
     * 如果第一个点与最后一个点不同，则图形不会闭合。
     * @param       xPoints <i>x</i> 坐标数组
     * @param       yPoints <i>y</i> 坐标数组
     * @param       nPoints 点的总数
     * @see         java.awt.Graphics#drawPolygon(int[], int[], int)
     * @since       JDK1.1
     */
    public abstract void drawPolyline(int xPoints[], int yPoints[],
                                      int nPoints);

    /**
     * 绘制由 <i>x</i> 和 <i>y</i> 坐标数组定义的闭合多边形。
     * 每对 (<i>x</i>,&nbsp;<i>y</i>) 坐标定义一个点。
     * <p>
     * 此方法绘制由 <code>nPoint</code> 线段定义的多边形，其中前 <code>nPoint&nbsp;-&nbsp;1</code>
     * 线段是从 <code>(xPoints[i&nbsp;-&nbsp;1],&nbsp;yPoints[i&nbsp;-&nbsp;1])</code>
     * 到 <code>(xPoints[i],&nbsp;yPoints[i])</code>，对于
     * 1&nbsp;&le;&nbsp;<i>i</i>&nbsp;&le;&nbsp;<code>nPoints</code>。
     * 如果最后一个点与第一个点不同，则自动绘制一条线连接最后一个点和第一个点，以闭合图形。
     * @param        xPoints   <code>x</code> 坐标数组。
     * @param        yPoints   <code>y</code> 坐标数组。
     * @param        nPoints   点的总数。
     * @see          java.awt.Graphics#fillPolygon
     * @see          java.awt.Graphics#drawPolyline
     */
    public abstract void drawPolygon(int xPoints[], int yPoints[],
                                     int nPoints);

    /**
     * 绘制由指定 <code>Polygon</code> 对象定义的多边形轮廓。
     * @param        p 要绘制的多边形。
     * @see          java.awt.Graphics#fillPolygon
     * @see          java.awt.Graphics#drawPolyline
     */
    public void drawPolygon(Polygon p) {
        drawPolygon(p.xpoints, p.ypoints, p.npoints);
    }

    /**
     * 填充由 <i>x</i> 和 <i>y</i> 坐标数组定义的闭合多边形。
     * <p>
     * 此方法绘制由 <code>nPoint</code> 线段定义的多边形，其中前 <code>nPoint&nbsp;-&nbsp;1</code>
     * 线段是从 <code>(xPoints[i&nbsp;-&nbsp;1],&nbsp;yPoints[i&nbsp;-&nbsp;1])</code>
     * 到 <code>(xPoints[i],&nbsp;yPoints[i])</code>，对于
     * 1&nbsp;&le;&nbsp;<i>i</i>&nbsp;&le;&nbsp;<code>nPoints</code>。
     * 如果最后一个点与第一个点不同，则自动绘制一条线连接最后一个点和第一个点，以闭合图形。
     * <p>
     * 多边形内部的区域使用偶数-奇数填充规则（也称为交替规则）定义。
     * @param        xPoints   <code>x</code> 坐标数组。
     * @param        yPoints   <code>y</code> 坐标数组。
     * @param        nPoints   点的总数。
     * @see          java.awt.Graphics#drawPolygon(int[], int[], int)
     */
    public abstract void fillPolygon(int xPoints[], int yPoints[],
                                     int nPoints);

    /**
     * 使用此图形上下文的当前颜色填充由指定 Polygon 对象定义的多边形。
     * <p>
     * 多边形内部的区域使用偶数-奇数填充规则（也称为交替规则）定义。
     * @param        p 要填充的多边形。
     * @see          java.awt.Graphics#drawPolygon(int[], int[], int)
     */
    public void fillPolygon(Polygon p) {
        fillPolygon(p.xpoints, p.ypoints, p.npoints);
    }

    /**
     * 使用此图形上下文的当前字体和颜色绘制指定字符串的文本。文本的基线左端字符位于此图形上下文坐标系中的位置 (<i>x</i>,&nbsp;<i>y</i>)。
     * @param       str      要绘制的字符串。
     * @param       x        <i>x</i> 坐标。
     * @param       y        <i>y</i> 坐标。
     * @throws NullPointerException 如果 <code>str</code> 为 <code>null</code>。
     * @see         java.awt.Graphics#drawBytes
     * @see         java.awt.Graphics#drawChars
     */
    public abstract void drawString(String str, int x, int y);

    /**
     * 根据 {@link java.awt.font.TextAttribute TextAttribute} 类的规范，应用其属性渲染指定迭代器的文本。
     * <p>
     * 文本的基线左端字符位于此图形上下文坐标系中的位置 (<i>x</i>,&nbsp;<i>y</i>)。
     * @param       iterator 要绘制的文本的迭代器
     * @param       x        <i>x</i> 坐标。
     * @param       y        <i>y</i> 坐标。
     * @throws NullPointerException 如果 <code>iterator</code> 为 <code>null</code>。
     * @see         java.awt.Graphics#drawBytes
     * @see         java.awt.Graphics#drawChars
     */
   public abstract void drawString(AttributedCharacterIterator iterator,
                                    int x, int y);

    /**
     * 使用此图形上下文的当前字体和颜色绘制由指定字符数组定义的文本。文本的基线左端字符位于此图形上下文坐标系中的位置 (<i>x</i>,&nbsp;<i>y</i>)。
     * @param data 要绘制的字符数组
     * @param offset 数据中的起始偏移量
     * @param length 要绘制的字符数
     * @param x 文本基线的 <i>x</i> 坐标
     * @param y 文本基线的 <i>y</i> 坐标
     * @throws NullPointerException 如果 <code>data</code> 为 <code>null</code>。
     * @throws IndexOutOfBoundsException 如果 <code>offset</code> 或 <code>length</code> 小于零，或 <code>offset+length</code> 大于 <code>data</code> 数组的长度。
     * @see         java.awt.Graphics#drawBytes
     * @see         java.awt.Graphics#drawString
     */
    public void drawChars(char data[], int offset, int length, int x, int y) {
        drawString(new String(data, offset, length), x, y);
    }

    /**
     * 使用此图形上下文的当前字体和颜色绘制由指定字节数组定义的文本。文本的基线左端字符位于此图形上下文坐标系中的位置 (<i>x</i>,&nbsp;<i>y</i>)。
     * <p>
     * 不推荐使用此方法，因为每个字节都被解释为 0 到 255 范围内的 Unicode 代码点，因此只能用于绘制该范围内的拉丁字符。
     * @param data 要绘制的数据
     * @param offset 数据中的起始偏移量
     * @param length 要绘制的字节数
     * @param x 文本基线的 <i>x</i> 坐标
     * @param y 文本基线的 <i>y</i> 坐标
     * @throws NullPointerException 如果 <code>data</code> 为 <code>null</code>。
     * @throws IndexOutOfBoundsException 如果 <code>offset</code> 或 <code>length</code> 小于零，或 <code>offset+length</code> 大于 <code>data</code> 数组的长度。
     * @see         java.awt.Graphics#drawChars
     * @see         java.awt.Graphics#drawString
     */
    public void drawBytes(byte data[], int offset, int length, int x, int y) {
        drawString(new String(data, 0, offset, length), x, y);
    }

    /**
     * 绘制当前可用的指定图像的尽可能多的部分。
     * 图像的左上角位于此图形上下文坐标空间中的 (<i>x</i>,&nbsp;<i>y>) 位置。透明像素不会影响已经存在的像素。
     * <p>
     * 此方法在所有情况下都会立即返回，即使图像尚未完全加载，且尚未进行抖动和转换以适应当前输出设备。
     * <p>
     * 如果图像已完全加载且其像素不再更改，则 <code>drawImage</code> 返回 <code>true</code>。
     * 否则，<code>drawImage</code> 返回 <code>false</code>，并且随着更多图像变得可用或需要绘制动画的另一帧，
     * 加载图像的过程会通知指定的图像观察者。
     * @param    img 要绘制的指定图像。如果 <code>img</code> 为 null，则此方法不执行任何操作。
     * @param    x   <i>x</i> 坐标。
     * @param    y   <i>y</i> 坐标。
     * @param    observer    图像转换时要通知的对象。
     * @return   如果图像像素仍在更改，则返回 <code>false</code>；否则返回 <code>true</code>。
     * @see      java.awt.Image
     * @see      java.awt.image.ImageObserver
     * @see      java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     */
    public abstract boolean drawImage(Image img, int x, int y,
                                      ImageObserver observer);

    /**
     * 绘制已缩放以适应指定矩形的指定图像的尽可能多的部分。
     * <p>
     * 图像在此图形上下文坐标空间中的指定矩形内绘制，必要时进行缩放。透明像素不会影响已经存在的像素。
     * <p>
     * 此方法在所有情况下都会立即返回，即使图像尚未完全缩放、抖动和转换以适应当前输出设备。
     * 如果当前输出表示尚未完成，则 <code>drawImage</code> 返回 <code>false</code>。随着更多图像变得可用，加载图像的过程会通知图像观察者。
     * <p>
     * 即使已为此输出设备构建了未缩放版本的图像，缩放版本的图像也不一定会立即可用。每个大小的图像可能会单独缓存，并从原始数据在单独的图像生成序列中生成。
     * @param    img    要绘制的指定图像。如果 <code>img</code> 为 null，则此方法不执行任何操作。
     * @param    x      <i>x</i> 坐标。
     * @param    y      <i>y</i> 坐标。
     * @param    width  矩形的宽度。
     * @param    height 矩形的高度。
     * @param    observer    图像转换时要通知的对象。
     * @return   如果图像像素仍在更改，则返回 <code>false</code>；否则返回 <code>true</code>。
     * @see      java.awt.Image
     * @see      java.awt.image.ImageObserver
     * @see      java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     */
    public abstract boolean drawImage(Image img, int x, int y,
                                      int width, int height,
                                      ImageObserver observer);

    /**
     * 绘制当前可用的指定图像的尽可能多的部分。
     * 图像的左上角位于此图形上下文坐标空间中的 (<i>x</i>,&nbsp;<i>y>) 位置。透明像素以指定的背景色绘制。
     * <p>
     * 此操作等同于使用给定颜色填充指定图像的宽度和高度的矩形，然后在此矩形上绘制图像，但可能更高效。
     * <p>
     * 此方法在所有情况下都会立即返回，即使图像尚未完全加载，且尚未进行抖动和转换以适应当前输出设备。
     * <p>
     * 如果图像已完全加载且其像素不再更改，则 <code>drawImage</code> 返回 <code>true</code>。
     * 否则，<code>drawImage</code> 返回 <code>false</code>，并且随着更多图像变得可用或需要绘制动画的另一帧，
     * 加载图像的过程会通知指定的图像观察者。
     * @param    img 要绘制的指定图像。如果 <code>img</code> 为 null，则此方法不执行任何操作。
     * @param    x      <i>x</i> 坐标。
     * @param    y      <i>y</i> 坐标。
     * @param    bgcolor 用于绘制非不透明部分的背景色。
     * @param    observer    图像转换时要通知的对象。
     * @return   如果图像像素仍在更改，则返回 <code>false</code>；否则返回 <code>true</code>。
     * @see      java.awt.Image
     * @see      java.awt.image.ImageObserver
     * @see      java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     */
    public abstract boolean drawImage(Image img, int x, int y,
                                      Color bgcolor,
                                      ImageObserver observer);

    /**
     * 绘制已缩放以适应指定矩形的指定图像的尽可能多的部分。
     * <p>
     * 图像在此图形上下文坐标空间中的指定矩形内绘制，必要时进行缩放。透明像素以指定的背景色绘制。
     * 此操作等同于使用给定颜色填充指定图像的宽度和高度的矩形，然后在此矩形上绘制图像，但可能更高效。
     * <p>
     * 此方法在所有情况下都会立即返回，即使图像尚未完全缩放、抖动和转换以适应当前输出设备。
     * 如果当前输出表示尚未完成，则 <code>drawImage</code> 返回 <code>false</code>。随着更多图像变得可用，加载图像的过程会通知图像观察者。
     * <p>
     * 即使已为此输出设备构建了未缩放版本的图像，缩放版本的图像也不一定会立即可用。每个大小的图像可能会单独缓存，并从原始数据在单独的图像生成序列中生成。
     * @param    img       要绘制的指定图像。如果 <code>img</code> 为 null，则此方法不执行任何操作。
     * @param    x         <i>x</i> 坐标。
     * @param    y         <i>y</i> 坐标。
     * @param    width     矩形的宽度。
     * @param    height    矩形的高度。
     * @param    bgcolor   用于绘制非不透明部分的背景色。
     * @param    observer    图像转换时要通知的对象。
     * @return   如果图像像素仍在更改，则返回 <code>false</code>；否则返回 <code>true</code>。
     * @see      java.awt.Image
     * @see      java.awt.image.ImageObserver
     * @see      java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     */
    public abstract boolean drawImage(Image img, int x, int y,
                                      int width, int height,
                                      Color bgcolor,
                                      ImageObserver observer);


                /**
     * 绘制指定图像的指定区域中当前可用的部分，实时缩放以适应
     * 指定的目标可绘制区域。透明像素不会影响已经存在的像素。
     * <p>
     * 无论图像区域是否已被缩放、抖动和转换为当前输出设备，
     * 此方法都会立即返回。
     * 如果当前输出表示尚未完成，则 <code>drawImage</code> 返回 <code>false</code>。
     * 随着更多图像变得可用，加载图像的过程会通知指定的图像观察者。
     * <p>
     * 此方法始终使用图像的未缩放版本来渲染缩放矩形，并实时执行所需的缩放。
     * 它不会为此操作使用缓存的缩放版本。从源到目标的图像缩放以这样的方式进行：
     * 源矩形的第一个坐标映射到目标矩形的第一个坐标，源矩形的第二个坐标映射到目标矩形的第二个坐标。
     * 子图像根据需要进行缩放和翻转以保持这些映射。
     * @param       img 要绘制的指定图像。如果 <code>img</code> 为 null，则此方法不执行任何操作。
     * @param       dx1 目标矩形第一个角的 <i>x</i> 坐标。
     * @param       dy1 目标矩形第一个角的 <i>y</i> 坐标。
     * @param       dx2 目标矩形第二个角的 <i>x</i> 坐标。
     * @param       dy2 目标矩形第二个角的 <i>y</i> 坐标。
     * @param       sx1 源矩形第一个角的 <i>x</i> 坐标。
     * @param       sy1 源矩形第一个角的 <i>y</i> 坐标。
     * @param       sx2 源矩形第二个角的 <i>x</i> 坐标。
     * @param       sy2 源矩形第二个角的 <i>y</i> 坐标。
     * @param       observer 当更多图像被缩放和转换时要通知的对象。
     * @return   如果图像像素仍在变化，则返回 <code>false</code>；否则返回 <code>true</code>。
     * @see         java.awt.Image
     * @see         java.awt.image.ImageObserver
     * @see         java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     * @since       JDK1.1
     */
    public abstract boolean drawImage(Image img,
                                      int dx1, int dy1, int dx2, int dy2,
                                      int sx1, int sy1, int sx2, int sy2,
                                      ImageObserver observer);

    /**
     * 绘制指定图像的指定区域中当前可用的部分，实时缩放以适应
     * 指定的目标可绘制区域。
     * <p>
     * 透明像素以指定的背景颜色绘制。此操作等同于用指定的颜色填充一个与指定图像宽度和高度相同的矩形，然后在上面绘制图像，但可能更高效。
     * <p>
     * 无论图像区域是否已被缩放、抖动和转换为当前输出设备，
     * 此方法都会立即返回。
     * 如果当前输出表示尚未完成，则 <code>drawImage</code> 返回 <code>false</code>。
     * 随着更多图像变得可用，加载图像的过程会通知指定的图像观察者。
     * <p>
     * 此方法始终使用图像的未缩放版本来渲染缩放矩形，并实时执行所需的缩放。
     * 它不会为此操作使用缓存的缩放版本。从源到目标的图像缩放以这样的方式进行：
     * 源矩形的第一个坐标映射到目标矩形的第一个坐标，源矩形的第二个坐标映射到目标矩形的第二个坐标。
     * 子图像根据需要进行缩放和翻转以保持这些映射。
     * @param       img 要绘制的指定图像。如果 <code>img</code> 为 null，则此方法不执行任何操作。
     * @param       dx1 目标矩形第一个角的 <i>x</i> 坐标。
     * @param       dy1 目标矩形第一个角的 <i>y</i> 坐标。
     * @param       dx2 目标矩形第二个角的 <i>x</i> 坐标。
     * @param       dy2 目标矩形第二个角的 <i>y</i> 坐标。
     * @param       sx1 源矩形第一个角的 <i>x</i> 坐标。
     * @param       sy1 源矩形第一个角的 <i>y</i> 坐标。
     * @param       sx2 源矩形第二个角的 <i>x</i> 坐标。
     * @param       sy2 源矩形第二个角的 <i>y</i> 坐标。
     * @param       bgcolor 用于绘制非不透明部分的背景颜色。
     * @param       observer 当更多图像被缩放和转换时要通知的对象。
     * @return   如果图像像素仍在变化，则返回 <code>false</code>；否则返回 <code>true</code>。
     * @see         java.awt.Image
     * @see         java.awt.image.ImageObserver
     * @see         java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     * @since       JDK1.1
     */
    public abstract boolean drawImage(Image img,
                                      int dx1, int dy1, int dx2, int dy2,
                                      int sx1, int sy1, int sx2, int sy2,
                                      Color bgcolor,
                                      ImageObserver observer);

    /**
     * 释放此图形上下文并释放其使用的任何系统资源。
     * 一旦调用 <code>dispose</code>，就无法再使用 <code>Graphics</code> 对象。
     * <p>
     * 当 Java 程序运行时，可能会在短时间内创建大量 <code>Graphics</code> 对象。
     * 尽管垃圾收集器的最终化过程也会释放相同的系统资源，但最好手动通过调用此方法来释放相关资源，而不是依赖于可能长时间无法完成的最终化过程。
     * <p>
     * 作为组件的 <code>paint</code> 和 <code>update</code> 方法的参数提供的 <code>Graphics</code> 对象会由系统在这些方法返回时自动释放。
     * 为了提高效率，程序员仅在直接从组件或其他 <code>Graphics</code> 对象创建 <code>Graphics</code> 对象时，才应在使用完毕后调用 <code>dispose</code>。
     * @see         java.awt.Graphics#finalize
     * @see         java.awt.Component#paint
     * @see         java.awt.Component#update
     * @see         java.awt.Component#getGraphics
     * @see         java.awt.Graphics#create
     */
    public abstract void dispose();

    /**
     * 释放此图形上下文，一旦它不再被引用。
     * @see #dispose
     */
    public void finalize() {
        dispose();
    }

    /**
     * 返回一个表示此 <code>Graphics</code> 对象值的 <code>String</code> 对象。
     * @return       此图形上下文的字符串表示形式。
     */
    public String toString() {
        return getClass().getName() + "[font=" + getFont() + ",color=" + getColor() + "]";
    }

    /**
     * 返回当前剪切区域的边界矩形。
     * @return      当前剪切区域的边界矩形，或如果未设置剪切区域则返回 <code>null</code>。
     * @deprecated 从 JDK 1.1 版本开始，
     * 替换为 <code>getClipBounds()</code>。
     */
    @Deprecated
    public Rectangle getClipRect() {
        return getClipBounds();
    }

    /**
     * 如果指定的矩形区域可能与当前剪切区域相交，则返回 true。
     * 指定矩形区域的坐标在用户坐标空间中，并相对于此图形上下文的坐标系原点。
     * 此方法可能使用一种快速计算结果的算法，但有时可能会返回 true，即使指定的矩形区域不与剪切区域相交。
     * 因此，所使用的特定算法可能以速度换取准确性，但除非可以保证指定的矩形区域不与当前剪切区域相交，否则不会返回 false。
     * 此方法使用的剪切区域可以表示通过此图形上下文的剪切方法指定的用户剪切区域，以及与设备或图像边界和窗口可见性相关的剪切区域的交集。
     *
     * @param x 要测试的矩形的 x 坐标。
     * @param y 要测试的矩形的 y 坐标。
     * @param width 要测试的矩形的宽度。
     * @param height 要测试的矩形的高度。
     * @return <code>true</code> 如果指定的矩形与当前剪切区域的边界相交；<code>false</code>
     *         否则。
     */
    public boolean hitClip(int x, int y, int width, int height) {
        // 注意，此实现效率不高。
        // 子类应覆盖此方法并更直接地计算结果。
        Rectangle clipRect = getClipBounds();
        if (clipRect == null) {
            return true;
        }
        return clipRect.intersects(x, y, width, height);
    }

    /**
     * 返回当前剪切区域的边界矩形。
     * 矩形中的坐标相对于此图形上下文的坐标系原点。此方法与 {@link #getClipBounds() getClipBounds} 不同之处在于使用了现有的矩形而不是分配新的矩形。
     * 此方法引用的是用户剪切区域，该区域独立于与设备边界和窗口可见性相关的剪切区域。
     * 如果未设置剪切区域，或使用 <code>setClip(null)</code> 清除了剪切区域，则此方法返回指定的 <code>Rectangle</code>。
     * @param  r    当前剪切区域复制到的矩形。此矩形中的任何当前值都会被覆盖。
     * @return      当前剪切区域的边界矩形。
     */
    public Rectangle getClipBounds(Rectangle r) {
        // 注意，此实现效率不高。
        // 子类应覆盖此方法并避免 <code>getClipBounds()</code> 的分配开销。
        Rectangle clipRect = getClipBounds();
        if (clipRect != null) {
            r.x = clipRect.x;
            r.y = clipRect.y;
            r.width = clipRect.width;
            r.height = clipRect.height;
        } else if (r == null) {
            throw new NullPointerException("null rectangle parameter");
        }
        return r;
    }
}
