/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.geom;

import java.lang.annotation.Native;

/**
 * <code>PathIterator</code> 接口提供了实现 {@link java.awt.Shape Shape} 接口的对象返回其边界几何的机制，
 * 允许调用者一次检索边界的一个段。此接口允许这些对象一次使用 1 到 3 阶的 Bézier 曲线（即直线和二次或三次 Bézier 样条）来检索其边界的段。
 * <p>
 * 可以通过使用 "MOVETO" 段在几何中创建不连续性，从一个子路径的结束移动到下一个子路径的开始，来表达多个子路径。
 * <p>
 * 每个子路径可以通过在子路径的最后一个段的结束坐标与子路径的开始 "MOVETO" 段的开始坐标相同处结束，或者通过使用 "CLOSE" 段从最后一个点附加一条线段回到第一个点来手动关闭。
 * 请注意，手动关闭轮廓与使用 "CLOSE" 段关闭路径可能会导致子路径的端点使用不同的线条样式装饰。例如，{@link java.awt.BasicStroke BasicStroke} 对象
 * 如果遇到 "CLOSE" 段，会使用线条 "JOIN" 装饰来连接第一个和最后一个点，而仅仅在相同坐标结束路径则会在端点使用线条 "CAP" 装饰。
 *
 * @see java.awt.Shape
 * @see java.awt.BasicStroke
 *
 * @author Jim Graham
 */
public interface PathIterator {
    /**
     * 用于指定路径内部确定的奇偶规则的常量。
     * 奇偶规则指定，如果从该点向任何方向画一条射线到无穷远，该射线与路径段交叉的次数为奇数，则该点位于路径内部。
     */
    @Native public static final int WIND_EVEN_ODD       = 0;

    /**
     * 用于指定路径内部确定的非零规则的常量。
     * 非零规则指定，如果从该点向任何方向画一条射线到无穷远，该射线与路径段在逆时针方向交叉的次数与顺时针方向交叉的次数不同，则该点位于路径内部。
     */
    @Native public static final int WIND_NON_ZERO       = 1;

    /**
     * 用于指定新子路径的起始位置的点的段类型常量。
     */
    @Native public static final int SEG_MOVETO          = 0;

    /**
     * 用于指定从最近指定的点绘制的线段的结束点的段类型常量。
     */
    @Native public static final int SEG_LINETO          = 1;

    /**
     * 用于指定从最近指定的点绘制的二次参数曲线的点对的段类型常量。
     * 该曲线通过在 <code>(t=[0..1])</code> 范围内求解参数控制方程来插值，使用最近指定的（当前）点 (CP)、第一个控制点 (P1) 和最终插值控制点 (P2)。
     * 该曲线的参数控制方程为：
     * <pre>
     *          P(t) = B(2,0)*CP + B(2,1)*P1 + B(2,2)*P2
     *          0 &lt;= t &lt;= 1
     *
     *        B(n,m) = 第 n 阶 Bernstein 多项式的第 m 个系数
     *               = C(n,m) * t^(m) * (1 - t)^(n-m)
     *        C(n,m) = 从 n 个不同元素中取 m 个元素的组合数
     *               = n! / (m! * (n-m)!)
     * </pre>
     */
    @Native public static final int SEG_QUADTO          = 2;

    /**
     * 用于指定从最近指定的点绘制的三次参数曲线的 3 个点的段类型常量。
     * 该曲线通过在 <code>(t=[0..1])</code> 范围内求解参数控制方程来插值，使用最近指定的（当前）点 (CP)、第一个控制点 (P1)、第二个控制点 (P2) 和最终插值控制点 (P3)。
     * 该曲线的参数控制方程为：
     * <pre>
     *          P(t) = B(3,0)*CP + B(3,1)*P1 + B(3,2)*P2 + B(3,3)*P3
     *          0 &lt;= t &lt;= 1
     *
     *        B(n,m) = 第 n 阶 Bernstein 多项式的第 m 个系数
     *               = C(n,m) * t^(m) * (1 - t)^(n-m)
     *        C(n,m) = 从 n 个不同元素中取 m 个元素的组合数
     *               = n! / (m! * (n-m)!)
     * </pre>
     * 这种形式的曲线通常称为 Bézier 曲线。
     */
    @Native public static final int SEG_CUBICTO         = 3;

    /**
     * 用于指定通过附加一条线段回到最近的 SEG_MOVETO 点来关闭前一个子路径的段类型常量。
     */
    @Native public static final int SEG_CLOSE           = 4;

    /**
     * 返回用于确定路径内部的绕行规则。
     * @return 绕行规则。
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     */
    public int getWindingRule();

    /**
     * 测试迭代是否完成。
     * @return 如果所有段都已读取，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean isDone();

    /**
     * 将迭代器向前移动到路径的下一个段，只要在该方向上还有更多的点。
     */
    public void next();

    /**
     * 返回迭代中当前路径段的坐标和类型。
     * 返回值是路径段类型：SEG_MOVETO、SEG_LINETO、SEG_QUADTO、SEG_CUBICTO 或 SEG_CLOSE。
     * 必须传递一个长度为 6 的 float 数组，可以用于存储点的坐标。
     * 每个点存储为一对 float x,y 坐标。
     * SEG_MOVETO 和 SEG_LINETO 类型返回一个点，
     * SEG_QUADTO 返回两个点，
     * SEG_CUBICTO 返回 3 个点，
     * SEG_CLOSE 不返回任何点。
     * @param coords 一个用于存储此方法返回数据的数组
     * @return 当前路径段的路径段类型。
     * @see #SEG_MOVETO
     * @see #SEG_LINETO
     * @see #SEG_QUADTO
     * @see #SEG_CUBICTO
     * @see #SEG_CLOSE
     */
    public int currentSegment(float[] coords);

    /**
     * 返回迭代中当前路径段的坐标和类型。
     * 返回值是路径段类型：SEG_MOVETO、SEG_LINETO、SEG_QUADTO、SEG_CUBICTO 或 SEG_CLOSE。
     * 必须传递一个长度为 6 的 double 数组，可以用于存储点的坐标。
     * 每个点存储为一对 double x,y 坐标。
     * SEG_MOVETO 和 SEG_LINETO 类型返回一个点，
     * SEG_QUADTO 返回两个点，
     * SEG_CUBICTO 返回 3 个点，
     * SEG_CLOSE 不返回任何点。
     * @param coords 一个用于存储此方法返回数据的数组
     * @return 当前路径段的路径段类型。
     * @see #SEG_MOVETO
     * @see #SEG_LINETO
     * @see #SEG_QUADTO
     * @see #SEG_CUBICTO
     * @see #SEG_CLOSE
     */
    public int currentSegment(double[] coords);
}
