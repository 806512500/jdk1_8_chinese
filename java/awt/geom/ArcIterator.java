/*
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
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
 * 一个用于通过 PathIterator 接口迭代弧路径段的工具类。
 *
 * @author      Jim Graham
 */
class ArcIterator implements PathIterator {
    double x, y, w, h, angStRad, increment, cv;
    AffineTransform affine;
    int index;
    int arcSegs;
    int lineSegs;

    ArcIterator(Arc2D a, AffineTransform at) {
        this.w = a.getWidth() / 2;
        this.h = a.getHeight() / 2;
        this.x = a.getX() + w;
        this.y = a.getY() + h;
        this.angStRad = -Math.toRadians(a.getAngleStart());
        this.affine = at;
        double ext = -a.getAngleExtent();
        if (ext >= 360.0 || ext <= -360) {
            arcSegs = 4;
            this.increment = Math.PI / 2;
            // btan(Math.PI / 2);
            this.cv = 0.5522847498307933;
            if (ext < 0) {
                increment = -increment;
                cv = -cv;
            }
        } else {
            arcSegs = (int) Math.ceil(Math.abs(ext) / 90.0);
            this.increment = Math.toRadians(ext / arcSegs);
            this.cv = btan(increment);
            if (cv == 0) {
                arcSegs = 0;
            }
        }
        switch (a.getArcType()) {
        case Arc2D.OPEN:
            lineSegs = 0;
            break;
        case Arc2D.CHORD:
            lineSegs = 1;
            break;
        case Arc2D.PIE:
            lineSegs = 2;
            break;
        }
        if (w < 0 || h < 0) {
            arcSegs = lineSegs = -1;
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
     * @return 如果还有更多点可以读取，则返回 true
     */
    public boolean isDone() {
        return index > arcSegs + lineSegs;
    }

    /**
     * 将迭代器移动到路径的下一个段，沿着主要遍历方向前进，只要在该方向上还有更多点。
     */
    public void next() {
        index++;
    }

    /*
     * btan 计算用于近似弧段（范围小于或等于 90 度）的三次贝塞尔曲线的控制段长度 (k)。
     * 这个长度 (k) 将用于生成此类段的两个贝塞尔控制点。
     *
     *   假设：
     *     a) 弧以 0,0 为中心，半径为 1.0
     *     b) 弧范围小于 90 度
     *     c) 控制点应保持切线
     *     d) 控制段应具有相等的长度
     *
     *   初始数据：
     *     起始角度：ang1
     *     结束角度：ang2 = ang1 + 范围
     *     起始点：P1 = (x1, y1) = (cos(ang1), sin(ang1))
     *     结束点：P4 = (x4, y4) = (cos(ang2), sin(ang2))
     *
     *   控制点：
     *     P2 = (x2, y2)
     *     | x2 = x1 - k * sin(ang1) = cos(ang1) - k * sin(ang1)
     *     | y2 = y1 + k * cos(ang1) = sin(ang1) + k * cos(ang1)
     *
     *     P3 = (x3, y3)
     *     | x3 = x4 + k * sin(ang2) = cos(ang2) + k * sin(ang2)
     *     | y3 = y4 - k * cos(ang2) = sin(ang2) - k * cos(ang2)
     *
     * 该长度 (k) 的公式可以通过以下推导找到：
     *
     *   中点：
     *     a) 贝塞尔 (t = 1/2)
     *        bPm = P1 * (1-t)^3 +
     *              3 * P2 * t * (1-t)^2 +
     *              3 * P3 * t^2 * (1-t) +
     *              P4 * t^3 =
     *            = (P1 + 3P2 + 3P3 + P4)/8
     *
     *     b) 弧
     *        aPm = (cos((ang1 + ang2)/2), sin((ang1 + ang2)/2))
     *
     *   令 angb = (ang2 - ang1)/2; angb 是 ang1 和 ang2 之间角度的一半。
     *
     *   解方程 bPm == aPm
     *
     *     a) 对于 xm 坐标：
     *        x1 + 3*x2 + 3*x3 + x4 = 8*cos((ang1 + ang2)/2)
     *
     *        cos(ang1) + 3*cos(ang1) - 3*k*sin(ang1) +
     *        3*cos(ang2) + 3*k*sin(ang2) + cos(ang2) =
     *        = 8*cos((ang1 + ang2)/2)
     *
     *        4*cos(ang1) + 4*cos(ang2) + 3*k*(sin(ang2) - sin(ang1)) =
     *        = 8*cos((ang1 + ang2)/2)
     *
     *        8*cos((ang1 + ang2)/2)*cos((ang2 - ang1)/2) +
     *        6*k*sin((ang2 - ang1)/2)*cos((ang1 + ang2)/2) =
     *        = 8*cos((ang1 + ang2)/2)
     *
     *        4*cos(angb) + 3*k*sin(angb) = 4
     *
     *        k = 4 / 3 * (1 - cos(angb)) / sin(angb)
     *
     *     b) 对于 ym 坐标，我们推导出相同的公式。
     *
     * 由于此公式在小角度下可能生成 "NaN" 值，我们将推导出一个更安全的形式，不涉及除以非常小的值：
     *     (1 - cos(angb)) / sin(angb) =
     *     = (1 - cos(angb))*(1 + cos(angb)) / sin(angb)*(1 + cos(angb)) =
     *     = (1 - cos(angb)^2) / sin(angb)*(1 + cos(angb)) =
     *     = sin(angb)^2 / sin(angb)*(1 + cos(angb)) =
     *     = sin(angb) / (1 + cos(angb))
     *
     */
    private static double btan(double increment) {
        increment /= 2.0;
        return 4.0 / 3.0 * Math.sin(increment) / (1.0 + Math.cos(increment));
    }

    /**
     * 返回迭代中当前路径段的坐标和类型。
     * 返回值是路径段类型：
     * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO 或 SEG_CLOSE。
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
            throw new NoSuchElementException("arc iterator out of bounds");
        }
        double angle = angStRad;
        if (index == 0) {
            coords[0] = (float) (x + Math.cos(angle) * w);
            coords[1] = (float) (y + Math.sin(angle) * h);
            if (affine != null) {
                affine.transform(coords, 0, coords, 0, 1);
            }
            return SEG_MOVETO;
        }
        if (index > arcSegs) {
            if (index == arcSegs + lineSegs) {
                return SEG_CLOSE;
            }
            coords[0] = (float) x;
            coords[1] = (float) y;
            if (affine != null) {
                affine.transform(coords, 0, coords, 0, 1);
            }
            return SEG_LINETO;
        }
        angle += increment * (index - 1);
        double relx = Math.cos(angle);
        double rely = Math.sin(angle);
        coords[0] = (float) (x + (relx - cv * rely) * w);
        coords[1] = (float) (y + (rely + cv * relx) * h);
        angle += increment;
        relx = Math.cos(angle);
        rely = Math.sin(angle);
        coords[2] = (float) (x + (relx + cv * rely) * w);
        coords[3] = (float) (y + (rely - cv * relx) * h);
        coords[4] = (float) (x + relx * w);
        coords[5] = (float) (y + rely * h);
        if (affine != null) {
            affine.transform(coords, 0, coords, 0, 3);
        }
        return SEG_CUBICTO;
    }

    /**
     * 返回迭代中当前路径段的坐标和类型。
     * 返回值是路径段类型：
     * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO 或 SEG_CLOSE。
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
            throw new NoSuchElementException("arc iterator out of bounds");
        }
        double angle = angStRad;
        if (index == 0) {
            coords[0] = x + Math.cos(angle) * w;
            coords[1] = y + Math.sin(angle) * h;
            if (affine != null) {
                affine.transform(coords, 0, coords, 0, 1);
            }
            return SEG_MOVETO;
        }
        if (index > arcSegs) {
            if (index == arcSegs + lineSegs) {
                return SEG_CLOSE;
            }
            coords[0] = x;
            coords[1] = y;
            if (affine != null) {
                affine.transform(coords, 0, coords, 0, 1);
            }
            return SEG_LINETO;
        }
        angle += increment * (index - 1);
        double relx = Math.cos(angle);
        double rely = Math.sin(angle);
        coords[0] = x + (relx - cv * rely) * w;
        coords[1] = y + (rely + cv * relx) * h;
        angle += increment;
        relx = Math.cos(angle);
        rely = Math.sin(angle);
        coords[2] = x + (relx + cv * rely) * w;
        coords[3] = y + (rely - cv * relx) * h;
        coords[4] = x + relx * w;
        coords[5] = y + rely * h;
        if (affine != null) {
            affine.transform(coords, 0, coords, 0, 3);
        }
        return SEG_CUBICTO;
    }
}
