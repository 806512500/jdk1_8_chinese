
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

import java.util.*;

/**
 * <code>FlatteningPathIterator</code> 类返回另一个 {@link PathIterator} 对象的扁平化视图。其他 {@link java.awt.Shape Shape}
 * 类可以使用此类来为它们的路径提供扁平化行为，而无需自行执行插值计算。
 *
 * @author Jim Graham
 */
public class FlatteningPathIterator implements PathIterator {
    static final int GROW_SIZE = 24;    // 三次和二次曲线大小的倍数

    PathIterator src;                   // 源迭代器

    double squareflat;                  // 用于测试平方长度的平坦度参数的平方

    int limit;                          // 最大递归层数

    double hold[] = new double[14];     // 用于存储插值坐标的缓存
                                        // 注意，此数组必须足够长，以存储一个完整的三次曲线段和一个相对的三次曲线段，
                                        // 以避免在将曲线坐标复制到数组末尾时发生别名问题。
                                        // 这也恰好等于一个完整的二次曲线段和两个相对的二次曲线段的大小。

    double curx, cury;                  // 最后一个段的结束 x, y 坐标

    double movx, movy;                  // 最后一个移动段的 x, y 坐标

    int holdType;                       // 正在持有的曲线类型，用于插值

    int holdEnd;                        // 正在持有的最后一个曲线段的索引

    int holdIndex;                      // 最后一个插值的曲线段的索引。这是在下一次调用
                                        // currentSegment() 时准备返回的曲线段。

    int levels[];                       // 存储在缓存中的每个曲线的递归层级

    int levelIndex;                     // holdIndex 处的曲线段在 levels 数组中的索引

    boolean done;                       // 迭代完成时为 true

    /**
     * 构造一个新的 <code>FlatteningPathIterator</code> 对象，该对象在迭代路径时对其进行扁平化。
     * 迭代器不会将从源迭代器读取的任何曲线细分超过 10 层，这最多会产生每条曲线 1024 个线段。
     * @param src 被迭代的原始未扁平化路径
     * @param flatness 控制点与扁平化曲线之间的最大允许距离
     */
    public FlatteningPathIterator(PathIterator src, double flatness) {
        this(src, flatness, 10);
    }

    /**
     * 构造一个新的 <code>FlatteningPathIterator</code> 对象，该对象在迭代路径时对其进行扁平化。
     * <code>limit</code> 参数允许你控制迭代器在假设曲线足够平坦之前可以进行的最大递归细分层数，
     * 而无需与 <code>flatness</code> 参数进行测量。因此，扁平化迭代最多生成每条曲线 <code>(2^limit)</code> 个线段。
     * @param src 被迭代的原始未扁平化路径
     * @param flatness 控制点与扁平化曲线之间的最大允许距离
     * @param limit 允许的任何曲线段的最大递归细分层数
     * @exception IllegalArgumentException 如果 <code>flatness</code> 或 <code>limit</code> 小于零
     */
    public FlatteningPathIterator(PathIterator src, double flatness,
                                  int limit) {
        if (flatness < 0.0) {
            throw new IllegalArgumentException("flatness must be >= 0");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be >= 0");
        }
        this.src = src;
        this.squareflat = flatness * flatness;
        this.limit = limit;
        this.levels = new int[limit + 1];
        // 初始化第一个路径段
        next(false);
    }

    /**
     * 返回此迭代器的平坦度。
     * @return 此 <code>FlatteningPathIterator</code> 的平坦度。
     */
    public double getFlatness() {
        return Math.sqrt(squareflat);
    }

    /**
     * 返回此迭代器的递归限制。
     * @return 此 <code>FlatteningPathIterator</code> 的递归限制。
     */
    public int getRecursionLimit() {
        return limit;
    }

    /**
     * 返回用于确定路径内部的绕组规则。
     * @return 被迭代的原始未扁平化路径的绕组规则。
     * @see PathIterator#WIND_EVEN_ODD
     * @see PathIterator#WIND_NON_ZERO
     */
    public int getWindingRule() {
        return src.getWindingRule();
    }

    /**
     * 测试迭代是否完成。
     * @return 如果所有段都已读取，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean isDone() {
        return done;
    }

    /*
     * 确保 hold 数组可以容纳 (want) 更多的值。
     * 它当前持有 (hold.length - holdIndex) 个值。
     */
    void ensureHoldCapacity(int want) {
        if (holdIndex - want < 0) {
            int have = hold.length - holdIndex;
            int newsize = hold.length + GROW_SIZE;
            double newhold[] = new double[newsize];
            System.arraycopy(hold, holdIndex,
                             newhold, holdIndex + GROW_SIZE,
                             have);
            hold = newhold;
            holdIndex += GROW_SIZE;
            holdEnd += GROW_SIZE;
        }
    }

    /**
     * 将迭代器移动到路径的下一个段，沿着主要遍历方向前进，只要该方向还有更多点。
     */
    public void next() {
        next(true);
    }

    private void next(boolean doNext) {
        int level;

        if (holdIndex >= holdEnd) {
            if (doNext) {
                src.next();
            }
            if (src.isDone()) {
                done = true;
                return;
            }
            holdType = src.currentSegment(hold);
            levelIndex = 0;
            levels[0] = 0;
        }

        switch (holdType) {
        case SEG_MOVETO:
        case SEG_LINETO:
            curx = hold[0];
            cury = hold[1];
            if (holdType == SEG_MOVETO) {
                movx = curx;
                movy = cury;
            }
            holdIndex = 0;
            holdEnd = 0;
            break;
        case SEG_CLOSE:
            curx = movx;
            cury = movy;
            holdIndex = 0;
            holdEnd = 0;
            break;
        case SEG_QUADTO:
            if (holdIndex >= holdEnd) {
                // 将坐标移动到数组末尾。
                holdIndex = hold.length - 6;
                holdEnd = hold.length - 2;
                hold[holdIndex + 0] = curx;
                hold[holdIndex + 1] = cury;
                hold[holdIndex + 2] = hold[0];
                hold[holdIndex + 3] = hold[1];
                hold[holdIndex + 4] = curx = hold[2];
                hold[holdIndex + 5] = cury = hold[3];
            }

            level = levels[levelIndex];
            while (level < limit) {
                if (QuadCurve2D.getFlatnessSq(hold, holdIndex) < squareflat) {
                    break;
                }

                ensureHoldCapacity(4);
                QuadCurve2D.subdivide(hold, holdIndex,
                                      hold, holdIndex - 4,
                                      hold, holdIndex);
                holdIndex -= 4;

                // 现在我们已经细分了，我们构造了两个比原曲线深度低一层的曲线。
                // 其中一个曲线在原曲线的位置，另一个曲线在下一个持有的坐标槽中。
                // 我们现在将两个曲线的层级值设置为比原曲线高一层。
                level++;
                levels[levelIndex] = level;
                levelIndex++;
                levels[levelIndex] = level;
            }

            // 该曲线段足够平坦，或者递归层级太深，无法再进行扁平化。
            // holdIndex+4 和 holdIndex+5 处的两个坐标现在包含曲线的终点，可以作为近似线段的终点。
            holdIndex += 4;
            levelIndex--;
            break;
        case SEG_CUBICTO:
            if (holdIndex >= holdEnd) {
                // 将坐标移动到数组末尾。
                holdIndex = hold.length - 8;
                holdEnd = hold.length - 2;
                hold[holdIndex + 0] = curx;
                hold[holdIndex + 1] = cury;
                hold[holdIndex + 2] = hold[0];
                hold[holdIndex + 3] = hold[1];
                hold[holdIndex + 4] = hold[2];
                hold[holdIndex + 5] = hold[3];
                hold[holdIndex + 6] = curx = hold[4];
                hold[holdIndex + 7] = cury = hold[5];
            }

            level = levels[levelIndex];
            while (level < limit) {
                if (CubicCurve2D.getFlatnessSq(hold, holdIndex) < squareflat) {
                    break;
                }

                ensureHoldCapacity(6);
                CubicCurve2D.subdivide(hold, holdIndex,
                                       hold, holdIndex - 6,
                                       hold, holdIndex);
                holdIndex -= 6;

                // 现在我们已经细分了，我们构造了两个比原曲线深度低一层的曲线。
                // 其中一个曲线在原曲线的位置，另一个曲线在下一个持有的坐标槽中。
                // 我们现在将两个曲线的层级值设置为比原曲线高一层。
                level++;
                levels[levelIndex] = level;
                levelIndex++;
                levels[levelIndex] = level;
            }

            // 该曲线段足够平坦，或者递归层级太深，无法再进行扁平化。
            // holdIndex+6 和 holdIndex+7 处的两个坐标现在包含曲线的终点，可以作为近似线段的终点。
            holdIndex += 6;
            levelIndex--;
            break;
        }
    }

    /**
     * 返回迭代中当前路径段的坐标和类型。
     * 返回值是路径段类型：SEG_MOVETO, SEG_LINETO, 或 SEG_CLOSE。
     * 必须传递一个长度为 6 的 float 数组，并可以用于存储点的坐标。
     * 每个点存储为一对 float x, y 坐标。
     * SEG_MOVETO 和 SEG_LINETO 类型返回一个点，而 SEG_CLOSE 不返回任何点。
     * @param coords 用于存储从该方法返回的数据的数组
     * @return 当前路径段的路径段类型。
     * @exception NoSuchElementException 如果扁平化路径中没有更多元素可以返回。
     * @see PathIterator#SEG_MOVETO
     * @see PathIterator#SEG_LINETO
     * @see PathIterator#SEG_CLOSE
     */
    public int currentSegment(float[] coords) {
        if (isDone()) {
            throw new NoSuchElementException("flattening iterator out of bounds");
        }
        int type = holdType;
        if (type != SEG_CLOSE) {
            coords[0] = (float) hold[holdIndex + 0];
            coords[1] = (float) hold[holdIndex + 1];
            if (type != SEG_MOVETO) {
                type = SEG_LINETO;
            }
        }
        return type;
    }

    /**
     * 返回迭代中当前路径段的坐标和类型。
     * 返回值是路径段类型：SEG_MOVETO, SEG_LINETO, 或 SEG_CLOSE。
     * 必须传递一个长度为 6 的 double 数组，并可以用于存储点的坐标。
     * 每个点存储为一对 double x, y 坐标。
     * SEG_MOVETO 和 SEG_LINETO 类型返回一个点，而 SEG_CLOSE 不返回任何点。
     * @param coords 用于存储从该方法返回的数据的数组
     * @return 当前路径段的路径段类型。
     * @exception NoSuchElementException 如果扁平化路径中没有更多元素可以返回。
     * @see PathIterator#SEG_MOVETO
     * @see PathIterator#SEG_LINETO
     * @see PathIterator#SEG_CLOSE
     */
    public int currentSegment(double[] coords) {
        if (isDone()) {
            throw new NoSuchElementException("flattening iterator out of bounds");
        }
        int type = holdType;
        if (type != SEG_CLOSE) {
            coords[0] = hold[holdIndex + 0];
            coords[1] = hold[holdIndex + 1];
            if (type != SEG_MOVETO) {
                type = SEG_LINETO;
            }
        }
        return type;
    }
}
