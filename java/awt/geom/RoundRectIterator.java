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
 * 一个用于通过 PathIterator 接口迭代圆角矩形路径段的工具类。
 *
 * @author      Jim Graham
 */
class RoundRectIterator implements PathIterator {
    double x, y, w, h, aw, ah;
    AffineTransform affine;
    int index;

    RoundRectIterator(RoundRectangle2D rr, AffineTransform at) {
        this.x = rr.getX();
        this.y = rr.getY();
        this.w = rr.getWidth();
        this.h = rr.getHeight();
        this.aw = Math.min(w, Math.abs(rr.getArcWidth()));
        this.ah = Math.min(h, Math.abs(rr.getArcHeight()));
        this.affine = at;
        if (aw < 0 || ah < 0) {
            // 不绘制任何内容...
            index = ctrlpts.length;
        }
    }

    /**
     * 返回用于确定路径内部性的绕行规则。
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     */
    public int getWindingRule() {
        return WIND_NON_ZERO;
    }

    /**
     * 测试是否还有更多点可以读取。
     * @return 如果还有更多点可以读取则返回 true
     */
    public boolean isDone() {
        return index >= ctrlpts.length;
    }

    /**
     * 将迭代器向前移动到路径的下一个段，只要在该方向上还有更多点。
     */
    public void next() {
        index++;
    }

    private static final double angle = Math.PI / 4.0;
    private static final double a = 1.0 - Math.cos(angle);
    private static final double b = Math.tan(angle);
    private static final double c = Math.sqrt(1.0 + b * b) - 1 + a;
    private static final double cv = 4.0 / 3.0 * a * b / c;
    private static final double acv = (1.0 - cv) / 2.0;

    // 对于每个数组：
    //     每个点的 4 个值 {v0, v1, v2, v3}：
    //         point = (x + v0 * w + v1 * arcWidth,
    //                  y + v2 * h + v3 * arcHeight);
    private static double ctrlpts[][] = {
        {  0.0,  0.0,  0.0,  0.5 },
        {  0.0,  0.0,  1.0, -0.5 },
        {  0.0,  0.0,  1.0, -acv,
           0.0,  acv,  1.0,  0.0,
           0.0,  0.5,  1.0,  0.0 },
        {  1.0, -0.5,  1.0,  0.0 },
        {  1.0, -acv,  1.0,  0.0,
           1.0,  0.0,  1.0, -acv,
           1.0,  0.0,  1.0, -0.5 },
        {  1.0,  0.0,  0.0,  0.5 },
        {  1.0,  0.0,  0.0,  acv,
           1.0, -acv,  0.0,  0.0,
           1.0, -0.5,  0.0,  0.0 },
        {  0.0,  0.5,  0.0,  0.0 },
        {  0.0,  acv,  0.0,  0.0,
           0.0,  0.0,  0.0,  acv,
           0.0,  0.0,  0.0,  0.5 },
        {},
    };
    private static int types[] = {
        SEG_MOVETO,
        SEG_LINETO, SEG_CUBICTO,
        SEG_LINETO, SEG_CUBICTO,
        SEG_LINETO, SEG_CUBICTO,
        SEG_LINETO, SEG_CUBICTO,
        SEG_CLOSE,
    };

    /**
     * 返回迭代中当前路径段的坐标和类型。
     * 返回值是路径段类型：
     * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, 或 SEG_CLOSE。
     * 必须传递一个长度为 6 的 float 数组，并且可以用于存储点的坐标。
     * 每个点存储为一对 float x,y 坐标。
     * SEG_MOVETO 和 SEG_LINETO 类型将返回一个点，
     * SEG_QUADTO 将返回两个点，
     * SEG_CUBICTO 将返回 3 个点
     * 而 SEG_CLOSE 不返回任何点。
     * @see #SEG_MOVETO
     * @see #SEG_LINETO
     * @see #SEG_QUADTO
     * @see #SEG_CUBICTO
     * @see #SEG_CLOSE
     */
    public int currentSegment(float[] coords) {
        if (isDone()) {
            throw new NoSuchElementException("roundrect iterator out of bounds");
        }
        double ctrls[] = ctrlpts[index];
        int nc = 0;
        for (int i = 0; i < ctrls.length; i += 4) {
            coords[nc++] = (float) (x + ctrls[i + 0] * w + ctrls[i + 1] * aw);
            coords[nc++] = (float) (y + ctrls[i + 2] * h + ctrls[i + 3] * ah);
        }
        if (affine != null) {
            affine.transform(coords, 0, coords, 0, nc / 2);
        }
        return types[index];
    }

    /**
     * 返回迭代中当前路径段的坐标和类型。
     * 返回值是路径段类型：
     * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, 或 SEG_CLOSE。
     * 必须传递一个长度为 6 的 double 数组，并且可以用于存储点的坐标。
     * 每个点存储为一对 double x,y 坐标。
     * SEG_MOVETO 和 SEG_LINETO 类型将返回一个点，
     * SEG_QUADTO 将返回两个点，
     * SEG_CUBICTO 将返回 3 个点
     * 而 SEG_CLOSE 不返回任何点。
     * @see #SEG_MOVETO
     * @see #SEG_LINETO
     * @see #SEG_QUADTO
     * @see #SEG_CUBICTO
     * @see #SEG_CLOSE
     */
    public int currentSegment(double[] coords) {
        if (isDone()) {
            throw new NoSuchElementException("roundrect iterator out of bounds");
        }
        double ctrls[] = ctrlpts[index];
        int nc = 0;
        for (int i = 0; i < ctrls.length; i += 4) {
            coords[nc++] = (x + ctrls[i + 0] * w + ctrls[i + 1] * aw);
            coords[nc++] = (y + ctrls[i + 2] * h + ctrls[i + 3] * ah);
        }
        if (affine != null) {
            affine.transform(coords, 0, coords, 0, nc / 2);
        }
        return types[index];
    }
}
