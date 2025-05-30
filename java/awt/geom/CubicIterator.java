/*
 * Copyright (c) 1997, Oracle and/or its affiliates. All rights reserved.
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
 * 一个实用类，用于通过 PathIterator 接口迭代三次曲线段的路径段。
 *
 * @author      Jim Graham
 */
class CubicIterator implements PathIterator {
    CubicCurve2D cubic;
    AffineTransform affine;
    int index;

    CubicIterator(CubicCurve2D q, AffineTransform at) {
        this.cubic = q;
        this.affine = at;
    }

    /**
     * 返回用于确定路径内部性的绕组规则。
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     */
    public int getWindingRule() {
        return WIND_NON_ZERO;
    }

    /**
     * 测试是否还有更多点可以读取。
     * @return 如果还有更多点可以读取，则返回 true
     */
    public boolean isDone() {
        return (index > 1);
    }

    /**
     * 将迭代器向前移动到路径的下一个段，只要在该方向上还有更多点。
     */
    public void next() {
        index++;
    }

    /**
     * 返回当前路径段的坐标和类型。
     * 返回值是路径段类型：
     * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO，或 SEG_CLOSE。
     * 必须传递一个长度为 6 的 float 数组，可以用于存储点的坐标。
     * 每个点存储为一对 float x,y 坐标。
     * SEG_MOVETO 和 SEG_LINETO 类型将返回一个点，
     * SEG_QUADTO 将返回两个点，
     * SEG_CUBICTO 将返回 3 个点，
     * 而 SEG_CLOSE 不返回任何点。
     * @see #SEG_MOVETO
     * @see #SEG_LINETO
     * @see #SEG_QUADTO
     * @see #SEG_CUBICTO
     * @see #SEG_CLOSE
     */
    public int currentSegment(float[] coords) {
        if (isDone()) {
            throw new NoSuchElementException("cubic iterator iterator out of bounds");
        }
        int type;
        if (index == 0) {
            coords[0] = (float) cubic.getX1();
            coords[1] = (float) cubic.getY1();
            type = SEG_MOVETO;
        } else {
            coords[0] = (float) cubic.getCtrlX1();
            coords[1] = (float) cubic.getCtrlY1();
            coords[2] = (float) cubic.getCtrlX2();
            coords[3] = (float) cubic.getCtrlY2();
            coords[4] = (float) cubic.getX2();
            coords[5] = (float) cubic.getY2();
            type = SEG_CUBICTO;
        }
        if (affine != null) {
            affine.transform(coords, 0, coords, 0, index == 0 ? 1 : 3);
        }
        return type;
    }

    /**
     * 返回当前路径段的坐标和类型。
     * 返回值是路径段类型：
     * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO，或 SEG_CLOSE。
     * 必须传递一个长度为 6 的 double 数组，可以用于存储点的坐标。
     * 每个点存储为一对 double x,y 坐标。
     * SEG_MOVETO 和 SEG_LINETO 类型将返回一个点，
     * SEG_QUADTO 将返回两个点，
     * SEG_CUBICTO 将返回 3 个点，
     * 而 SEG_CLOSE 不返回任何点。
     * @see #SEG_MOVETO
     * @see #SEG_LINETO
     * @see #SEG_QUADTO
     * @see #SEG_CUBICTO
     * @see #SEG_CLOSE
     */
    public int currentSegment(double[] coords) {
        if (isDone()) {
            throw new NoSuchElementException("cubic iterator iterator out of bounds");
        }
        int type;
        if (index == 0) {
            coords[0] = cubic.getX1();
            coords[1] = cubic.getY1();
            type = SEG_MOVETO;
        } else {
            coords[0] = cubic.getCtrlX1();
            coords[1] = cubic.getCtrlY1();
            coords[2] = cubic.getCtrlX2();
            coords[3] = cubic.getCtrlY2();
            coords[4] = cubic.getX2();
            coords[5] = cubic.getY2();
            type = SEG_CUBICTO;
        }
        if (affine != null) {
            affine.transform(coords, 0, coords, 0, index == 0 ? 1 : 3);
        }
        return type;
    }
}
