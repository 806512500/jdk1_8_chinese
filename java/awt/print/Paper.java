/*
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.print;

import java.awt.geom.Rectangle2D;

/**
 * <code>Paper</code> 类描述了纸张的物理特性。
 * <p>
 * 创建 <code>Paper</code> 对象时，应用程序有责任确保纸张大小和可打印区域是兼容的。例如，如果纸张大小从 11 x 17 更改为 8.5 x 11，应用程序可能需要减少可打印区域，以确保打印的内容能够适应页面。
 * <p>
 * @see #setSize(double, double)
 * @see #setImageableArea(double, double, double, double)
 */
public class Paper implements Cloneable {

 /* 私有类变量 */

    private static final int INCH = 72;
    private static final double LETTER_WIDTH = 8.5 * INCH;
    private static final double LETTER_HEIGHT = 11 * INCH;

 /* 实例变量 */

    /**
     * 物理页面的高度，以 1/72 英寸为单位。该数值存储为浮点数，而不是整数，以便于从公制单位转换为 1/72 英寸，然后再转换回去。（这可能或可能不是一个足够好的理由使用浮点数）。
     */
    private double mHeight;

    /**
     * 物理页面的宽度，以 1/72 英寸为单位。
     */
    private double mWidth;

    /**
     * 页面上绘图可见的区域。该矩形之外的页面区域通常反映了打印机的硬件边距。物理页面的原点位于 (0, 0)，该矩形在此坐标系中提供。
     */
    private Rectangle2D mImageableArea;

 /* 构造函数 */

    /**
     * 创建一个带有 1 英寸边距的信纸大小的纸张。
     */
    public Paper() {
        mHeight = LETTER_HEIGHT;
        mWidth = LETTER_WIDTH;
        mImageableArea = new Rectangle2D.Double(INCH, INCH,
                                                mWidth - 2 * INCH,
                                                mHeight - 2 * INCH);
    }

 /* 实例方法 */

    /**
     * 创建一个与当前 <code>Paper</code> 内容相同的副本。
     * @return 一个与当前 <code>Paper</code> 内容相同的副本。
     */
    public Object clone() {

        Paper newPaper;

        try {
            /* 由于我们总是返回可打印区域的副本，因此将可打印区域的引用复制到克隆中是可以的。 */
            newPaper = (Paper) super.clone();

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            newPaper = null;    // 应该不会发生。
        }

        return newPaper;
    }

    /**
     * 返回页面的高度，以 1/72 英寸为单位。
     * @return 由该 <code>Paper</code> 描述的页面的高度。
     */
    public double getHeight() {
        return mHeight;
    }

    /**
     * 设置此 <code>Paper</code> 对象的宽度和高度，该对象表示打印发生时的页面属性。
     * 尺寸以 1/72 英寸为单位提供。
     * @param width 要设置的此 <code>Paper</code> 对象的宽度
     * @param height 要设置的此 <code>Paper</code> 对象的高度
     */
    public void setSize(double width, double height) {
        mWidth = width;
        mHeight = height;
    }

    /**
     * 返回页面的宽度，以 1/72 英寸为单位。
     * @return 由该 <code>Paper</code> 描述的页面的宽度。
     */
    public double getWidth() {
        return mWidth;
    }

    /**
     * 设置此 <code>Paper</code> 的可打印区域。可打印区域是页面上发生打印的区域。
     * @param x 要设置的此 <code>Paper</code> 可打印区域左上角的 X 坐标
     * @param y 要设置的此 <code>Paper</code> 可打印区域左上角的 Y 坐标
     * @param width 要设置的此 <code>Paper</code> 可打印区域的宽度
     * @param height 要设置的此 <code>Paper</code> 可打印区域的高度
     */
    public void setImageableArea(double x, double y,
                                 double width, double height) {
        mImageableArea = new Rectangle2D.Double(x, y, width, height);
    }

    /**
     * 返回此 <code>Paper</code> 对象可打印区域左上角的 X 坐标。
     * @return 可打印区域的 X 坐标。
     */
    public double getImageableX() {
        return mImageableArea.getX();
    }

    /**
     * 返回此 <code>Paper</code> 对象可打印区域左上角的 Y 坐标。
     * @return 可打印区域的 Y 坐标。
     */
    public double getImageableY() {
        return mImageableArea.getY();
    }

    /**
     * 返回此 <code>Paper</code> 对象可打印区域的宽度。
     * @return 可打印区域的宽度。
     */
    public double getImageableWidth() {
        return mImageableArea.getWidth();
    }

    /**
     * 返回此 <code>Paper</code> 对象可打印区域的高度。
     * @return 可打印区域的高度。
     */
    public double getImageableHeight() {
        return mImageableArea.getHeight();
    }
}
