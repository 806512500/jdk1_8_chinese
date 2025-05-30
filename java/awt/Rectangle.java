
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

import java.awt.geom.Rectangle2D;
import java.beans.Transient;

/**
 * <code>Rectangle</code> 指定坐标空间中的一个区域，该区域由 <code>Rectangle</code> 对象的左上角点
 * {@code (x,y)} 在坐标空间中的位置、宽度和高度围成。
 * <p>
 * <code>Rectangle</code> 对象的 <code>width</code> 和 <code>height</code> 是 <code>public</code> 字段。创建 <code>Rectangle</code> 的构造函数和可以修改它的方法
 * 不会阻止设置负值的宽度或高度。
 * <p>
 * <a name="Empty">
 * 宽度或高度恰好为零的 {@code Rectangle} 在这些轴上有位置，但被认为是空的。
 * {@link #isEmpty} 方法将返回 true 对于这样的 {@code Rectangle}。
 * 检查空的 {@code Rectangle} 是否包含或与点或矩形相交的方法如果任一维度为零将始终返回 false。
 * 将这样的 {@code Rectangle} 与点或矩形组合的方法将像调用 {@link #add(Point)} 方法一样将 {@code Rectangle} 在该轴上的位置包含在结果中。
 * </a>
 * <p>
 * <a name="NonExistant">
 * 宽度或高度为负的 {@code Rectangle} 在这些轴上既没有位置也没有维度。
 * 这样的 {@code Rectangle} 在包含计算中被认为是不存在的，并且检查它是否包含或与点或矩形相交的方法将始终返回 false。
 * 将这样的 {@code Rectangle} 与点或矩形组合的方法将完全忽略该 {@code Rectangle} 以生成结果。
 * 如果两个 {@code Rectangle} 对象组合且每个都有负维度，结果将至少有一个负维度。
 * </a>
 * <p>
 * 仅影响 {@code Rectangle} 位置的方法将无论其在任一轴上是否有负值或零维度都会在其位置上操作。
 * <p>
 * 请注意，使用默认无参数构造函数构造的 {@code Rectangle} 将具有 {@code 0x0} 的维度，因此是空的。
 * 该 {@code Rectangle} 仍将具有 {@code (0,0)} 的位置，并且将该位置贡献给并集和添加操作。
 * 尝试累积一组点的边界代码应因此最初构造具有特定负宽度和高度的 {@code Rectangle} 或使用集合中的第一个点来构造 {@code Rectangle}。
 * 例如：
 * <pre>{@code
 *     Rectangle bounds = new Rectangle(0, 0, -1, -1);
 *     for (int i = 0; i < points.length; i++) {
 *         bounds.add(points[i]);
 *     }
 * }</pre>
 * 或者如果我们知道 points 数组中至少有一个点：
 * <pre>{@code
 *     Rectangle bounds = new Rectangle(points[0]);
 *     for (int i = 1; i < points.length; i++) {
 *         bounds.add(points[i]);
 *     }
 * }</pre>
 * <p>
 * 该类使用 32 位整数来存储其位置和维度。
 * 通常操作可能会产生超出 32 位整数范围的结果。
 * 这些方法将以避免任何 32 位溢出的方式计算中间结果，然后选择最佳表示将最终结果存储回存储位置和维度的 32 位字段中。
 * 结果的位置将通过将真实结果裁剪到最近的 32 位值来存储到 {@link #x} 和 {@link #y} 字段中。
 * 存储到 {@link #width} 和 {@link #height} 维度字段中的值将选择尽可能涵盖真实结果最大部分的 32 位值。
 * 通常这意味着维度将独立裁剪到 32 位整数的范围内，除非位置必须移动以存储到其 32 位字段对中，然后维度将相对于位置的“最佳表示”进行调整。
 * 如果真实结果在一条或多条轴上有负维度且因此不存在，则存储的维度将在这些轴上为负数。
 * 如果真实结果在 32 位整数范围内有位置，但在一条或多条轴上为零维度，则存储的维度将在这些轴上为零。
 *
 * @author      Sami Shaio
 * @since 1.0
 */
public class Rectangle extends Rectangle2D
    implements Shape, java.io.Serializable
{

    /**
     * <code>Rectangle</code> 的左上角的 X 坐标。
     *
     * @serial
     * @see #setLocation(int, int)
     * @see #getLocation()
     * @since 1.0
     */
    public int x;

    /**
     * <code>Rectangle</code> 的左上角的 Y 坐标。
     *
     * @serial
     * @see #setLocation(int, int)
     * @see #getLocation()
     * @since 1.0
     */
    public int y;

    /**
     * <code>Rectangle</code> 的宽度。
     * @serial
     * @see #setSize(int, int)
     * @see #getSize()
     * @since 1.0
     */
    public int width;

    /**
     * <code>Rectangle</code> 的高度。
     *
     * @serial
     * @see #setSize(int, int)
     * @see #getSize()
     * @since 1.0
     */
    public int height;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = -4345857070255674764L;

    /**
     * 初始化 JNI 字段和方法 ID
     */
    private static native void initIDs();

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * 构造一个新的 <code>Rectangle</code>，其左上角位于坐标空间的 (0,&nbsp;0)，宽度和高度均为零。
     */
    public Rectangle() {
        this(0, 0, 0, 0);
    }

    /**
     * 构造一个新的 <code>Rectangle</code>，初始化为指定的 <code>Rectangle</code> 的值。
     * @param r 从中复制初始值以构造新的 <code>Rectangle</code> 的 <code>Rectangle</code>
     * @since 1.1
     */
    public Rectangle(Rectangle r) {
        this(r.x, r.y, r.width, r.height);
    }

    /**
     * 构造一个新的 <code>Rectangle</code>，其左上角指定为
     * {@code (x,y)}，宽度和高度由同名参数指定。
     * @param     x 指定的 X 坐标
     * @param     y 指定的 Y 坐标
     * @param     width <code>Rectangle</code> 的宽度
     * @param     height <code>Rectangle</code> 的高度
     * @since 1.0
     */
    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * 构造一个新的 <code>Rectangle</code>，其左上角位于坐标空间的 (0,&nbsp;0)，宽度和高度由同名参数指定。
     * @param width <code>Rectangle</code> 的宽度
     * @param height <code>Rectangle</code> 的高度
     */
    public Rectangle(int width, int height) {
        this(0, 0, width, height);
    }

    /**
     * 构造一个新的 <code>Rectangle</code>，其左上角由 {@link Point} 参数指定，宽度和高度由
     * {@link Dimension} 参数指定。
     * @param p <code>Rectangle</code> 的左上角
     * @param d 表示 <code>Rectangle</code> 宽度和高度的 <code>Dimension</code>
     */
    public Rectangle(Point p, Dimension d) {
        this(p.x, p.y, d.width, d.height);
    }

    /**
     * 构造一个新的 <code>Rectangle</code>，其左上角由指定的 <code>Point</code> 指定，宽度和高度均为零。
     * @param p <code>Rectangle</code> 的左上角
     */
    public Rectangle(Point p) {
        this(p.x, p.y, 0, 0);
    }

    /**
     * 构造一个新的 <code>Rectangle</code>，其左上角为 (0,&nbsp;0)，宽度和高度由 <code>Dimension</code> 参数指定。
     * @param d 指定宽度和高度的 <code>Dimension</code>
     */
    public Rectangle(Dimension d) {
        this(0, 0, d.width, d.height);
    }

    /**
     * 返回 <code>Rectangle</code> 的 X 坐标，精度为 <code>double</code>。
     * @return <code>Rectangle</code> 的 X 坐标。
     */
    public double getX() {
        return x;
    }

    /**
     * 返回 <code>Rectangle</code> 的 Y 坐标，精度为 <code>double</code>。
     * @return <code>Rectangle</code> 的 Y 坐标。
     */
    public double getY() {
        return y;
    }

    /**
     * 返回 <code>Rectangle</code> 的宽度，精度为 <code>double</code>。
     * @return <code>Rectangle</code> 的宽度。
     */
    public double getWidth() {
        return width;
    }

    /**
     * 返回 <code>Rectangle</code> 的高度，精度为 <code>double</code>。
     * @return <code>Rectangle</code> 的高度。
     */
    public double getHeight() {
        return height;
    }

    /**
     * 获取此 <code>Rectangle</code> 的边界 <code>Rectangle</code>。
     * <p>
     * 包含此方法是为了完整性，以对应于
     * {@link Component} 的 <code>getBounds</code> 方法。
     * @return 一个新的 <code>Rectangle</code>，等于此 <code>Rectangle</code> 的边界 <code>Rectangle</code>。
     * @see       java.awt.Component#getBounds
     * @see       #setBounds(Rectangle)
     * @see       #setBounds(int, int, int, int)
     * @since     1.1
     */
    @Transient
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public Rectangle2D getBounds2D() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * 将此 <code>Rectangle</code> 的边界 <code>Rectangle</code> 设置为与指定的 <code>Rectangle</code> 匹配。
     * <p>
     * 包含此方法是为了完整性，以对应于 <code>Component</code> 的 <code>setBounds</code> 方法。
     * @param r 指定的 <code>Rectangle</code>
     * @see       #getBounds
     * @see       java.awt.Component#setBounds(java.awt.Rectangle)
     * @since     1.1
     */
    public void setBounds(Rectangle r) {
        setBounds(r.x, r.y, r.width, r.height);
    }

    /**
     * 将此 <code>Rectangle</code> 的边界 <code>Rectangle</code> 设置为指定的
     * <code>x</code>，<code>y</code>，<code>width</code>，
     * 和 <code>height</code>。
     * <p>
     * 包含此方法是为了完整性，以对应于 <code>Component</code> 的 <code>setBounds</code> 方法。
     * @param x 为这个 <code>Rectangle</code> 的左上角设置的新 X 坐标
     * @param y 为这个 <code>Rectangle</code> 的左上角设置的新 Y 坐标
     * @param width 为这个 <code>Rectangle</code> 设置的新宽度
     * @param height 为这个 <code>Rectangle</code> 设置的新高度
     * @see       #getBounds
     * @see       java.awt.Component#setBounds(int, int, int, int)
     * @since     1.1
     */
    public void setBounds(int x, int y, int width, int height) {
        reshape(x, y, width, height);
    }

    /**
     * 将此 {@code Rectangle} 的边界设置为指定的 {@code x}，{@code y}，{@code width}，
     * 和 {@code height} 的整数边界。
     * 如果参数指定的 {@code Rectangle} 超出整数的最大范围，结果将是与最大整数边界相交的指定 {@code Rectangle} 的最佳表示。
     * @param x 指定矩形左上角的 X 坐标
     * @param y 指定矩形左上角的 Y 坐标
     * @param width 指定矩形的宽度
     * @param height 指定矩形的新高度
     */
    public void setRect(double x, double y, double width, double height) {
        int newx, newy, neww, newh;

        if (x > 2.0 * Integer.MAX_VALUE) {
            // 在正 X 方向上表示得太远...
            // 即使将 x 和 width 都设置为 MAX_VALUE，我们甚至无法到达指定矩形的左侧。
            // 与“最大整数矩形”的交集不存在，因此我们应该使用宽度 < 0。
            // REMIND: 我们是否应该尝试确定一个更有意义的 neww 调整值而不是“-1”？
            newx = Integer.MAX_VALUE;
            neww = -1;
        } else {
            newx = clip(x, false);
            if (width >= 0) width += x - newx;
            neww = clip(width, width >= 0);
        }

        if (y > 2.0 * Integer.MAX_VALUE) {
            // 在正 Y 方向上表示得太远...
            newy = Integer.MAX_VALUE;
            newh = -1;
        } else {
            newy = clip(y, false);
            if (height >= 0) height += y - newy;
            newh = clip(height, height >= 0);
        }

        reshape(newx, newy, neww, newh);
    }
    // 返回 v 的最佳整数表示，裁剪到整数范围并进行向下或向上取整，取决于布尔值。
    private static int clip(double v, boolean doceil) {
        if (v <= Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        if (v >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) (doceil ? Math.ceil(v) : Math.floor(v));
    }


                /**
     * 设置此 <code>Rectangle</code> 的边界 <code>Rectangle</code> 为指定的
     * <code>x</code>，<code>y</code>，<code>width</code>，
     * 和 <code>height</code>。
     * <p>
     * @param x 此 <code>Rectangle</code> 的新 X 坐标
     * @param y 此 <code>Rectangle</code> 的新 Y 坐标
     * @param width 此 <code>Rectangle</code> 的新宽度
     * @param height 此 <code>Rectangle</code> 的新高度
     * @deprecated 从 JDK 1.1 版本开始，
     * 替换为 <code>setBounds(int, int, int, int)</code>。
     */
    @Deprecated
    public void reshape(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * 返回此 <code>Rectangle</code> 的位置。
     * <p>
     * 此方法是为了完整性而包含的，以与 <code>Component</code> 的
     * <code>getLocation</code> 方法平行。
     * @return 一个 <code>Point</code>，表示此 <code>Rectangle</code> 的左上角。
     * @see       java.awt.Component#getLocation
     * @see       #setLocation(Point)
     * @see       #setLocation(int, int)
     * @since     1.1
     */
    public Point getLocation() {
        return new Point(x, y);
    }

    /**
     * 将此 <code>Rectangle</code> 移动到指定的位置。
     * <p>
     * 此方法是为了完整性而包含的，以与 <code>Component</code> 的
     * <code>setLocation</code> 方法平行。
     * @param p 指定此 <code>Rectangle</code> 的新位置的 <code>Point</code>
     * @see       java.awt.Component#setLocation(java.awt.Point)
     * @see       #getLocation
     * @since     1.1
     */
    public void setLocation(Point p) {
        setLocation(p.x, p.y);
    }

    /**
     * 将此 <code>Rectangle</code> 移动到指定的位置。
     * <p>
     * 此方法是为了完整性而包含的，以与 <code>Component</code> 的
     * <code>setLocation</code> 方法平行。
     * @param x 新位置的 X 坐标
     * @param y 新位置的 Y 坐标
     * @see       #getLocation
     * @see       java.awt.Component#setLocation(int, int)
     * @since     1.1
     */
    public void setLocation(int x, int y) {
        move(x, y);
    }

    /**
     * 将此 <code>Rectangle</code> 移动到指定的位置。
     * <p>
     * @param x 新位置的 X 坐标
     * @param y 新位置的 Y 坐标
     * @deprecated 从 JDK 1.1 版本开始，
     * 替换为 <code>setLocation(int, int)</code>。
     */
    @Deprecated
    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * 将此 <code>Rectangle</code> 沿 X 坐标轴向右移动指定的距离，
     * 沿 Y 坐标轴向下移动指定的距离。
     * @param dx 沿 X 轴移动此 <code>Rectangle</code> 的距离
     * @param dy 沿 Y 轴移动此 <code>Rectangle</code> 的距离
     * @see       java.awt.Rectangle#setLocation(int, int)
     * @see       java.awt.Rectangle#setLocation(java.awt.Point)
     */
    public void translate(int dx, int dy) {
        int oldv = this.x;
        int newv = oldv + dx;
        if (dx < 0) {
            // 向左移动
            if (newv > oldv) {
                // 负溢出
                // 只有在宽度有效（>= 0）时才调整宽度。
                if (width >= 0) {
                    // 以下概念上等同于：
                    // width += newv; newv = MIN_VALUE; width -= newv;
                    width += newv - Integer.MIN_VALUE;
                    // 如果右边缘超过 MIN_VALUE，则宽度可能变为负数，但不会溢出，因为任何非负数 + MIN_VALUE 都不会溢出。
                }
                newv = Integer.MIN_VALUE;
            }
        } else {
            // 向右移动（或保持不动）
            if (newv < oldv) {
                // 正溢出
                if (width >= 0) {
                    // 以下概念上等同于：
                    // width += newv; newv = MAX_VALUE; width -= newv;
                    width += newv - Integer.MAX_VALUE;
                    // 在大宽度和大位移的情况下，我们可能会溢出，因此需要检查。
                    if (width < 0) width = Integer.MAX_VALUE;
                }
                newv = Integer.MAX_VALUE;
            }
        }
        this.x = newv;

        oldv = this.y;
        newv = oldv + dy;
        if (dy < 0) {
            // 向上移动
            if (newv > oldv) {
                // 负溢出
                if (height >= 0) {
                    height += newv - Integer.MIN_VALUE;
                    // 请参阅上述关于此情况下不会溢出的注释
                }
                newv = Integer.MIN_VALUE;
            }
        } else {
            // 向下移动（或保持不动）
            if (newv < oldv) {
                // 正溢出
                if (height >= 0) {
                    height += newv - Integer.MAX_VALUE;
                    if (height < 0) height = Integer.MAX_VALUE;
                }
                newv = Integer.MAX_VALUE;
            }
        }
        this.y = newv;
    }

    /**
     * 获取此 <code>Rectangle</code> 的大小，由返回的 <code>Dimension</code> 表示。
     * <p>
     * 此方法是为了完整性而包含的，以与 <code>Component</code> 的
     * <code>getSize</code> 方法平行。
     * @return 一个 <code>Dimension</code>，表示此 <code>Rectangle</code> 的大小。
     * @see       java.awt.Component#getSize
     * @see       #setSize(Dimension)
     * @see       #setSize(int, int)
     * @since     1.1
     */
    public Dimension getSize() {
        return new Dimension(width, height);
    }

    /**
     * 将此 <code>Rectangle</code> 的大小设置为与指定的 <code>Dimension</code> 匹配。
     * <p>
     * 此方法是为了完整性而包含的，以与 <code>Component</code> 的
     * <code>setSize</code> 方法平行。
     * @param d <code>Dimension</code> 对象的新大小
     * @see       java.awt.Component#setSize(java.awt.Dimension)
     * @see       #getSize
     * @since     1.1
     */
    public void setSize(Dimension d) {
        setSize(d.width, d.height);
    }

    /**
     * 将此 <code>Rectangle</code> 的大小设置为指定的宽度和高度。
     * <p>
     * 此方法是为了完整性而包含的，以与 <code>Component</code> 的
     * <code>setSize</code> 方法平行。
     * @param width 此 <code>Rectangle</code> 的新宽度
     * @param height 此 <code>Rectangle</code> 的新高度
     * @see       java.awt.Component#setSize(int, int)
     * @see       #getSize
     * @since     1.1
     */
    public void setSize(int width, int height) {
        resize(width, height);
    }

    /**
     * 将此 <code>Rectangle</code> 的大小设置为指定的宽度和高度。
     * <p>
     * @param width 此 <code>Rectangle</code> 的新宽度
     * @param height 此 <code>Rectangle</code> 的新高度
     * @deprecated 从 JDK 1.1 版本开始，
     * 替换为 <code>setSize(int, int)</code>。
     */
    @Deprecated
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * 检查指定的 <code>Point</code> 是否在此 <code>Rectangle</code> 内。
     * @param p 要测试的 <code>Point</code>
     * @return    如果指定的 <code>Point</code>
     *            在此 <code>Rectangle</code> 内，则返回 <code>true</code>；
     *            否则返回 <code>false</code>。
     * @since     1.1
     */
    public boolean contains(Point p) {
        return contains(p.x, p.y);
    }

    /**
     * 检查指定位置 {@code (x,y)} 的点是否在此 <code>Rectangle</code> 内。
     *
     * @param  x 指定的 X 坐标
     * @param  y 指定的 Y 坐标
     * @return    如果点
     *            {@code (x,y)} 在此
     *            <code>Rectangle</code> 内，则返回 <code>true</code>；
     *            否则返回 <code>false</code>。
     * @since     1.1
     */
    public boolean contains(int x, int y) {
        return inside(x, y);
    }

    /**
     * 检查指定的 <code>Rectangle</code> 是否完全在此 <code>Rectangle</code> 内。
     *
     * @param     r   指定的 <code>Rectangle</code>
     * @return    如果 <code>Rectangle</code>
     *            完全在此 <code>Rectangle</code> 内，则返回 <code>true</code>；
     *            否则返回 <code>false</code>
     * @since     1.2
     */
    public boolean contains(Rectangle r) {
        return contains(r.x, r.y, r.width, r.height);
    }

    /**
     * 检查此 <code>Rectangle</code> 是否完全包含指定位置 {@code (X,Y)} 的
     * 指定尺寸 {@code (W,H)} 的 <code>Rectangle</code>。
     * @param     X 指定的 X 坐标
     * @param     Y 指定的 Y 坐标
     * @param     W   <code>Rectangle</code> 的宽度
     * @param     H   <code>Rectangle</code> 的高度
     * @return    如果指定的 <code>Rectangle</code> 由
     *            {@code (X, Y, W, H)}
     *            完全包含在此 <code>Rectangle</code> 内，则返回 <code>true</code>；
     *            否则返回 <code>false</code>。
     * @since     1.1
     */
    public boolean contains(int X, int Y, int W, int H) {
        int w = this.width;
        int h = this.height;
        if ((w | h | W | H) < 0) {
            // 至少有一个维度是负数...
            return false;
        }
        // 注意：如果任何维度为零，下面的测试必须返回 false...
        int x = this.x;
        int y = this.y;
        if (X < x || Y < y) {
            return false;
        }
        w += x;
        W += X;
        if (W <= X) {
            // X+W 溢出或 W 为零，如果...
            // 原始 w 或 W 为零或
            // x+w 没有溢出或
            // 溢出的 x+w 小于溢出的 X+W
            if (w >= x || W > w) return false;
        } else {
            // X+W 没有溢出且 W 不为零，如果...
            // 原始 w 为零或
            // x+w 没有溢出且 x+w 小于 X+W
            if (w >= x && W > w) return false;
        }
        h += y;
        H += Y;
        if (H <= Y) {
            if (h >= y || H > h) return false;
        } else {
            if (h >= y && H > h) return false;
        }
        return true;
    }

    /**
     * 检查指定位置 {@code (X,Y)} 的点是否在此 <code>Rectangle</code> 内。
     *
     * @param  X 指定的 X 坐标
     * @param  Y 指定的 Y 坐标
     * @return    如果点
     *            {@code (X,Y)} 在此
     *            <code>Rectangle</code> 内，则返回 <code>true</code>；
     *            否则返回 <code>false</code>。
     * @deprecated 从 JDK 1.1 版本开始，
     * 替换为 <code>contains(int, int)</code>。
     */
    @Deprecated
    public boolean inside(int X, int Y) {
        int w = this.width;
        int h = this.height;
        if ((w | h) < 0) {
            // 至少有一个维度是负数...
            return false;
        }
        // 注意：如果任何维度为零，下面的测试必须返回 false...
        int x = this.x;
        int y = this.y;
        if (X < x || Y < y) {
            return false;
        }
        w += x;
        h += y;
        //    溢出或相交
        return ((w < x || w > X) &&
                (h < y || h > Y));
    }

    /**
     * 确定此 <code>Rectangle</code> 和指定的 <code>Rectangle</code> 是否相交。两个矩形相交如果它们的交集非空。
     *
     * @param r 指定的 <code>Rectangle</code>
     * @return    如果指定的 <code>Rectangle</code>
     *            和此 <code>Rectangle</code> 相交，则返回 <code>true</code>；
     *            否则返回 <code>false</code>。
     */
    public boolean intersects(Rectangle r) {
        int tw = this.width;
        int th = this.height;
        int rw = r.width;
        int rh = r.height;
        if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
            return false;
        }
        int tx = this.x;
        int ty = this.y;
        int rx = r.x;
        int ry = r.y;
        rw += rx;
        rh += ry;
        tw += tx;
        th += ty;
        //      溢出或相交
        return ((rw < rx || rw > tx) &&
                (rh < ry || rh > ty) &&
                (tw < tx || tw > rx) &&
                (th < ty || th > ry));
    }

    /**
     * 计算此 <code>Rectangle</code> 与指定的 <code>Rectangle</code> 的交集。返回一个新的 <code>Rectangle</code>
     * 表示两个矩形的交集。
     * 如果两个矩形不相交，结果将是一个空矩形。
     *
     * @param     r   指定的 <code>Rectangle</code>
     * @return    两个指定的 <code>Rectangle</code> 和此 <code>Rectangle</code> 中包含的最大 <code>Rectangle</code>；
     *            或如果矩形不相交，则返回一个空矩形。
     */
    public Rectangle intersection(Rectangle r) {
        int tx1 = this.x;
        int ty1 = this.y;
        int rx1 = r.x;
        int ry1 = r.y;
        long tx2 = tx1; tx2 += this.width;
        long ty2 = ty1; ty2 += this.height;
        long rx2 = rx1; rx2 += r.width;
        long ry2 = ry1; ry2 += r.height;
        if (tx1 < rx1) tx1 = rx1;
        if (ty1 < ry1) ty1 = ry1;
        if (tx2 > rx2) tx2 = rx2;
        if (ty2 > ry2) ty2 = ry2;
        tx2 -= tx1;
        ty2 -= ty1;
        // tx2,ty2 永远不会溢出（它们永远不会大于两个源 w,h 中的最小值）
        // 它们可能会下溢...
        if (tx2 < Integer.MIN_VALUE) tx2 = Integer.MIN_VALUE;
        if (ty2 < Integer.MIN_VALUE) ty2 = Integer.MIN_VALUE;
        return new Rectangle(tx1, ty1, (int) tx2, (int) ty2);
    }

    /**
     * 计算此 <code>Rectangle</code> 与指定的 <code>Rectangle</code> 的并集。返回一个新的
     * <code>Rectangle</code> 表示两个矩形的并集。
     * <p>
     * 如果两个 <code>Rectangle</code> 中的任何一个的任何维度小于零
     * 则应用 <a href=#NonExistant>不存在</a> 矩形的规则。
     * 如果只有一个维度小于零，则结果将是另一个 <code>Rectangle</code> 的副本。
     * 如果两个维度都小于零，则结果将至少有一个维度小于零。
     * <p>
     * 如果结果的 <code>Rectangle</code> 的任何维度太大而无法表示为 <code>int</code>，
     * 则结果在该维度上的值为 <code>Integer.MAX_VALUE</code>。
     * @param r 指定的 <code>Rectangle</code>
     * @return    包含指定的 <code>Rectangle</code> 和此
     *            <code>Rectangle</code> 的最小 <code>Rectangle</code>。
     */
    public Rectangle union(Rectangle r) {
        long tx2 = this.width;
        long ty2 = this.height;
        if ((tx2 | ty2) < 0) {
            // 此矩形有负维度...
            // 如果 r 有非负维度，则它是答案。
            // 如果 r 不存在（有负维度），则两个都是不存在的，我们可以返回任何不存在的矩形作为答案。
            // 因此，返回 r 满足该条件。
            // 无论如何，r 是我们的答案。
            return new Rectangle(r);
        }
        long rx2 = r.width;
        long ry2 = r.height;
        if ((rx2 | ry2) < 0) {
            return new Rectangle(this);
        }
        int tx1 = this.x;
        int ty1 = this.y;
        tx2 += tx1;
        ty2 += ty1;
        int rx1 = r.x;
        int ry1 = r.y;
        rx2 += rx1;
        ry2 += ry1;
        if (tx1 > rx1) tx1 = rx1;
        if (ty1 > ry1) ty1 = ry1;
        if (tx2 < rx2) tx2 = rx2;
        if (ty2 < ry2) ty2 = ry2;
        tx2 -= tx1;
        ty2 -= ty1;
        // tx2,ty2 永远不会下溢，因为两个原始矩形已经被证明是非空的
        // 它们可能会溢出...
        if (tx2 > Integer.MAX_VALUE) tx2 = Integer.MAX_VALUE;
        if (ty2 > Integer.MAX_VALUE) ty2 = Integer.MAX_VALUE;
        return new Rectangle(tx1, ty1, (int) tx2, (int) ty2);
    }


                /**
     * 添加一个由整数参数 {@code newx,newy} 指定的点到此 {@code Rectangle} 的边界。
     * <p>
     * 如果此 {@code Rectangle} 有任何维度小于零，
     * 则应用 <a href=#NonExistant>不存在</a>
     * 矩形的规则。
     * 在这种情况下，此 {@code Rectangle} 的新边界将
     * 有一个位置等于指定的坐标和
     * 宽度和高度等于零。
     * <p>
     * 添加一个点后，调用带有
     * 添加的点作为参数的 <code>contains</code> 方法并不一定会返回
     * <code>true</code>。对于位于 <code>Rectangle</code> 右边或底边的点，
     * <code>contains</code> 方法不会返回 <code>true</code>。因此，如果添加的点
     * 落在此 <code>Rectangle</code> 的右边或底边，
     * <code>contains</code> 返回 <code>false</code>。
     * 如果必须包含指定的点，则应添加一个 1x1 的矩形：
     * <pre>
     *     r.add(newx, newy, 1, 1);
     * </pre>
     * @param newx 新点的 X 坐标
     * @param newy 新点的 Y 坐标
     */
    public void add(int newx, int newy) {
        if ((width | height) < 0) {
            this.x = newx;
            this.y = newy;
            this.width = this.height = 0;
            return;
        }
        int x1 = this.x;
        int y1 = this.y;
        long x2 = this.width;
        long y2 = this.height;
        x2 += x1;
        y2 += y1;
        if (x1 > newx) x1 = newx;
        if (y1 > newy) y1 = newy;
        if (x2 < newx) x2 = newx;
        if (y2 < newy) y2 = newy;
        x2 -= x1;
        y2 -= y1;
        if (x2 > Integer.MAX_VALUE) x2 = Integer.MAX_VALUE;
        if (y2 > Integer.MAX_VALUE) y2 = Integer.MAX_VALUE;
        reshape(x1, y1, (int) x2, (int) y2);
    }

    /**
     * 将指定的 {@code Point} 添加到此
     * {@code Rectangle} 的边界。
     * <p>
     * 如果此 {@code Rectangle} 有任何维度小于零，
     * 则应用 <a href=#NonExistant>不存在</a>
     * 矩形的规则。
     * 在这种情况下，此 {@code Rectangle} 的新边界将
     * 有一个位置等于指定的
     * {@code Point} 的坐标和宽度和高度等于零。
     * <p>
     * 添加一个 <code>Point</code> 后，调用带有
     * 添加的 <code>Point</code> 作为参数的 <code>contains</code> 方法并不一定会返回
     * <code>true</code>。对于位于 <code>Rectangle</code> 右边
     * 或底边的点，<code>contains</code> 方法不会返回 <code>true</code>。因此，如果添加的
     * <code>Point</code> 落在此 <code>Rectangle</code> 的右边或底边，
     * <code>contains</code> 返回 <code>false</code>。
     * 如果必须包含指定的点，则应添加一个 1x1 的矩形：
     * <pre>
     *     r.add(pt.x, pt.y, 1, 1);
     * </pre>
     * @param pt 要添加到此
     *           <code>Rectangle</code> 的新 <code>Point</code>
     */
    public void add(Point pt) {
        add(pt.x, pt.y);
    }

    /**
     * 将一个 <code>Rectangle</code> 添加到此 <code>Rectangle</code>。
     * 结果的 <code>Rectangle</code> 是两个矩形的并集。
     * <p>
     * 如果两个 {@code Rectangle} 中的任何一个有任何维度小于 0，结果将具有另一个 {@code Rectangle} 的维度。
     * 如果两个 {@code Rectangle} 都至少有一个维度小于 0，结果将至少有一个维度小于 0。
     * <p>
     * 如果两个 {@code Rectangle} 中的任何一个有一个或两个维度等于 0，结果在这些轴上的维度将等同于将
     * 对应的原点坐标添加到结果矩形的这些轴上，类似于 {@link #add(Point)} 方法的操作，
     * 但不会进一步增加这些轴上的维度。
     * <p>
     * 如果结果的 {@code Rectangle} 在某个维度上的尺寸太大，无法用 {@code int} 表示，结果
     * 在该维度上的尺寸将为 {@code Integer.MAX_VALUE}。
     * @param  r 指定的 <code>Rectangle</code>
     */
    public void add(Rectangle r) {
        long tx2 = this.width;
        long ty2 = this.height;
        if ((tx2 | ty2) < 0) {
            reshape(r.x, r.y, r.width, r.height);
        }
        long rx2 = r.width;
        long ry2 = r.height;
        if ((rx2 | ry2) < 0) {
            return;
        }
        int tx1 = this.x;
        int ty1 = this.y;
        tx2 += tx1;
        ty2 += ty1;
        int rx1 = r.x;
        int ry1 = r.y;
        rx2 += rx1;
        ry2 += ry1;
        if (tx1 > rx1) tx1 = rx1;
        if (ty1 > ry1) ty1 = ry1;
        if (tx2 < rx2) tx2 = rx2;
        if (ty2 < ry2) ty2 = ry2;
        tx2 -= tx1;
        ty2 -= ty1;
        // tx2,ty2 不会下溢，因为两个原始矩形都是非空的
        // 但可能会溢出...
        if (tx2 > Integer.MAX_VALUE) tx2 = Integer.MAX_VALUE;
        if (ty2 > Integer.MAX_VALUE) ty2 = Integer.MAX_VALUE;
        reshape(tx1, ty1, (int) tx2, (int) ty2);
    }

    /**
     * 水平和垂直地调整 <code>Rectangle</code> 的大小。
     * <p>
     * 此方法将 <code>Rectangle</code> 修改为
     * 左右两侧各增加 <code>h</code> 个单位，
     * 顶部和底部各增加 <code>v</code> 个单位。
     * <p>
     * 新的 <code>Rectangle</code> 的左上角为 {@code (x - h, y - v)}，
     * 宽度为 {@code (width + 2h)}，
     * 高度为 {@code (height + 2v)}。
     * <p>
     * 如果为 <code>h</code> 和
     * <code>v</code> 提供负值，<code>Rectangle</code> 的大小将相应减小。
     * <code>grow</code> 方法将检查整数溢出和下溢，但不会检查
     * {@code width} 和 {@code height} 的结果值是否从负变为非负或从非负变为负。
     * @param h 水平扩展
     * @param v 垂直扩展
     */
    public void grow(int h, int v) {
        long x0 = this.x;
        long y0 = this.y;
        long x1 = this.width;
        long y1 = this.height;
        x1 += x0;
        y1 += y0;

        x0 -= h;
        y0 -= v;
        x1 += h;
        y1 += v;

        if (x1 < x0) {
            // X 方向上不存在
            // 最终宽度必须保持负值，因此在裁剪 x0 之前减去 x0 以避免
            // 裁剪 x0 可能会反转 x0 和 x1 的顺序。
            x1 -= x0;
            if (x1 < Integer.MIN_VALUE) x1 = Integer.MIN_VALUE;
            if (x0 < Integer.MIN_VALUE) x0 = Integer.MIN_VALUE;
            else if (x0 > Integer.MAX_VALUE) x0 = Integer.MAX_VALUE;
        } else { // (x1 >= x0)
            // 在从 x1 中减去 x0 之前裁剪 x0，以防止裁剪
            // 影响矩形的可表示区域。
            if (x0 < Integer.MIN_VALUE) x0 = Integer.MIN_VALUE;
            else if (x0 > Integer.MAX_VALUE) x0 = Integer.MAX_VALUE;
            x1 -= x0;
            // 现在 x1 为负的唯一方式是我们在 MIN 处裁剪了 x0，而 x1 小于 MIN - 在这种情况下
            // 我们希望保持宽度为负，因为结果没有与可表示区域相交。
            if (x1 < Integer.MIN_VALUE) x1 = Integer.MIN_VALUE;
            else if (x1 > Integer.MAX_VALUE) x1 = Integer.MAX_VALUE;
        }

        if (y1 < y0) {
            // Y 方向上不存在
            y1 -= y0;
            if (y1 < Integer.MIN_VALUE) y1 = Integer.MIN_VALUE;
            if (y0 < Integer.MIN_VALUE) y0 = Integer.MIN_VALUE;
            else if (y0 > Integer.MAX_VALUE) y0 = Integer.MAX_VALUE;
        } else { // (y1 >= y0)
            if (y0 < Integer.MIN_VALUE) y0 = Integer.MIN_VALUE;
            else if (y0 > Integer.MAX_VALUE) y0 = Integer.MAX_VALUE;
            y1 -= y0;
            if (y1 < Integer.MIN_VALUE) y1 = Integer.MIN_VALUE;
            else if (y1 > Integer.MAX_VALUE) y1 = Integer.MAX_VALUE;
        }

        reshape((int) x0, (int) y0, (int) x1, (int) y1);
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean isEmpty() {
        return (width <= 0) || (height <= 0);
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public int outcode(double x, double y) {
        /*
         * 关于下面转换为 double 的注解。如果在 int 中进行
         * x+w 或 y+h 的算术运算，可能会发生整数溢出。通过在加法之前转换为 double，
         * 我们强制加法在 double 中进行以避免在比较时溢出。
         *
         * 请参阅 bug 4320890 了解这可能引起的问题。
         */
        int out = 0;
        if (this.width <= 0) {
            out |= OUT_LEFT | OUT_RIGHT;
        } else if (x < this.x) {
            out |= OUT_LEFT;
        } else if (x > this.x + (double) this.width) {
            out |= OUT_RIGHT;
        }
        if (this.height <= 0) {
            out |= OUT_TOP | OUT_BOTTOM;
        } else if (y < this.y) {
            out |= OUT_TOP;
        } else if (y > this.y + (double) this.height) {
            out |= OUT_BOTTOM;
        }
        return out;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public Rectangle2D createIntersection(Rectangle2D r) {
        if (r instanceof Rectangle) {
            return intersection((Rectangle) r);
        }
        Rectangle2D dest = new Rectangle2D.Double();
        Rectangle2D.intersect(this, r, dest);
        return dest;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public Rectangle2D createUnion(Rectangle2D r) {
        if (r instanceof Rectangle) {
            return union((Rectangle) r);
        }
        Rectangle2D dest = new Rectangle2D.Double();
        Rectangle2D.union(this, r, dest);
        return dest;
    }

    /**
     * 检查两个矩形是否相等。
     * <p>
     * 结果为 <code>true</code> 当且仅当参数不为
     * <code>null</code> 并且是一个 <code>Rectangle</code> 对象，该对象具有与
     * 此 <code>Rectangle</code> 相同的左上角、宽度和高度。
     * @param obj 要与
     *                此 <code>Rectangle</code> 比较的 <code>Object</code>
     * @return    如果对象相等则返回 <code>true</code>；
     *            否则返回 <code>false</code>。
     */
    public boolean equals(Object obj) {
        if (obj instanceof Rectangle) {
            Rectangle r = (Rectangle)obj;
            return ((x == r.x) &&
                    (y == r.y) &&
                    (width == r.width) &&
                    (height == r.height));
        }
        return super.equals(obj);
    }

    /**
     * 返回一个表示此
     * <code>Rectangle</code> 及其值的 <code>String</code>。
     * @return 一个表示此
     *               <code>Rectangle</code> 对象的坐标和大小值的 <code>String</code>。
     */
    public String toString() {
        return getClass().getName() + "[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]";
    }
}
