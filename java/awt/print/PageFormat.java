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

package java.awt.print;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.lang.annotation.Native;

/**
 * <code>PageFormat</code> 类描述了要打印的页面的大小和方向。
 */
public class PageFormat implements Cloneable
{

 /* Class Constants */

    /**
     * 原点位于纸张的左下角，x 轴从下到上，y 轴从左到右。
     * 注意，这不是 Macintosh 横向，而是 Windows 和 PostScript 横向。
     */
    @Native public static final int LANDSCAPE = 0;

    /**
     * 原点位于纸张的左上角，x 轴向右，y 轴向下。
     */
    @Native public static final int PORTRAIT = 1;

    /**
     * 原点位于纸张的右上角，x 轴从上到下，y 轴从右到左。
     * 注意，这是 Macintosh 横向。
     */
    @Native public static final int REVERSE_LANDSCAPE = 2;

 /* Instance Variables */

    /**
     * 描述物理纸张的描述。
     */
    private Paper mPaper;

    /**
     * 当前页面的方向。这将是以下常量之一：PORTRAIT, LANDSCAPE, 或 REVERSE_LANDSCAPE。
     */
    private int mOrientation = PORTRAIT;

 /* Constructors */

    /**
     * 创建一个默认的、纵向的 <code>PageFormat</code>。
     */
    public PageFormat()
    {
        mPaper = new Paper();
    }

 /* Instance Methods */

    /**
     * 创建一个与当前 <code>PageFormat</code> 内容相同的副本。
     * @return 一个与当前 <code>PageFormat</code> 内容相同的副本。
     */
    public Object clone() {
        PageFormat newPage;

        try {
            newPage = (PageFormat) super.clone();
            newPage.mPaper = (Paper)mPaper.clone();

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            newPage = null;     // 应该不会发生。
        }

        return newPage;
    }


    /**
     * 返回页面的宽度，单位为 1/72 英寸。此方法会根据页面的方向来确定宽度。
     * @return 页面的宽度。
     */
    public double getWidth() {
        double width;
        int orientation = getOrientation();

        if (orientation == PORTRAIT) {
            width = mPaper.getWidth();
        } else {
            width = mPaper.getHeight();
        }

        return width;
    }

    /**
     * 返回页面的高度，单位为 1/72 英寸。此方法会根据页面的方向来确定高度。
     * @return 页面的高度。
     */
    public double getHeight() {
        double height;
        int orientation = getOrientation();

        if (orientation == PORTRAIT) {
            height = mPaper.getHeight();
        } else {
            height = mPaper.getWidth();
        }

        return height;
    }

    /**
     * 返回与当前 <code>PageFormat</code> 关联的 <code>Paper</code> 对象的可打印区域的左上角的 x 坐标。
     * 此方法会根据页面的方向来确定 x 坐标。
     * @return 与当前 <code>PageFormat</code> 关联的 <code>Paper</code> 对象的可打印区域的左上角的 x 坐标。
     */
    public double getImageableX() {
        double x;

        switch (getOrientation()) {

        case LANDSCAPE:
            x = mPaper.getHeight()
                - (mPaper.getImageableY() + mPaper.getImageableHeight());
            break;

        case PORTRAIT:
            x = mPaper.getImageableX();
            break;

        case REVERSE_LANDSCAPE:
            x = mPaper.getImageableY();
            break;

        default:
            /* 这不应该发生，因为它表示 PageFormat 处于无效的方向。 */
            throw new InternalError("未识别的方向");

        }

        return x;
    }

    /**
     * 返回与当前 <code>PageFormat</code> 关联的 <code>Paper</code> 对象的可打印区域的左上角的 y 坐标。
     * 此方法会根据页面的方向来确定 y 坐标。
     * @return 与当前 <code>PageFormat</code> 关联的 <code>Paper</code> 对象的可打印区域的左上角的 y 坐标。
     */
    public double getImageableY() {
        double y;

        switch (getOrientation()) {

        case LANDSCAPE:
            y = mPaper.getImageableX();
            break;

        case PORTRAIT:
            y = mPaper.getImageableY();
            break;

        case REVERSE_LANDSCAPE:
            y = mPaper.getWidth()
                - (mPaper.getImageableX() + mPaper.getImageableWidth());
            break;

        default:
            /* 这不应该发生，因为它表示 PageFormat 处于无效的方向。 */
            throw new InternalError("未识别的方向");

        }

        return y;
    }

    /**
     * 返回页面的可打印区域的宽度，单位为 1/72 英寸。此方法会根据页面的方向来确定宽度。
     * @return 页面的宽度。
     */
    public double getImageableWidth() {
        double width;

        if (getOrientation() == PORTRAIT) {
            width = mPaper.getImageableWidth();
        } else {
            width = mPaper.getImageableHeight();
        }

        return width;
    }

    /**
     * 返回页面的可打印区域的高度，单位为 1/72 英寸。此方法会根据页面的方向来确定高度。
     * @return 页面的高度。
     */
    public double getImageableHeight() {
        double height;

        if (getOrientation() == PORTRAIT) {
            height = mPaper.getImageableHeight();
        } else {
            height = mPaper.getImageableWidth();
        }

        return height;
    }


    /**
     * 返回与当前 <code>PageFormat</code> 关联的 <code>Paper</code> 对象的副本。对从该方法返回的 <code>Paper</code> 对象所做的更改不会影响当前 <code>PageFormat</code> 的 <code>Paper</code> 对象。要更新当前 <code>PageFormat</code> 的 <code>Paper</code> 对象，需要创建一个新的 <code>Paper</code> 对象并使用 {@link #setPaper(Paper)} 方法设置。
     * @return 与当前 <code>PageFormat</code> 关联的 <code>Paper</code> 对象的副本。
     * @see #setPaper
     */
    public Paper getPaper() {
        return (Paper)mPaper.clone();
    }

    /**
     * 设置当前 <code>PageFormat</code> 的 <code>Paper</code> 对象。
     * @param paper 要设置的 <code>Paper</code> 对象。
     * @exception NullPointerException 如果传递的参数为 null。
     * @see #getPaper
     */
     public void setPaper(Paper paper) {
         mPaper = (Paper)paper.clone();
     }

    /**
     * 设置页面的方向。<code>orientation</code> 必须是以下常量之一：PORTRAIT, LANDSCAPE, 或 REVERSE_LANDSCAPE。
     * @param orientation 页面的新方向。
     * @throws IllegalArgumentException 如果请求了未知的方向。
     * @see #getOrientation
     */
    public void setOrientation(int orientation) throws IllegalArgumentException
    {
        if (0 <= orientation && orientation <= REVERSE_LANDSCAPE) {
            mOrientation = orientation;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 返回当前 <code>PageFormat</code> 的方向。
     * @return 当前 <code>PageFormat</code> 对象的方向。
     * @see #setOrientation
     */
    public int getOrientation() {
        return mOrientation;
    }

    /**
     * 返回一个转换矩阵，用于将用户空间的渲染转换为页面的请求方向。这些值将以 { m00, m10, m01, m11, m02, m12 } 的形式放入数组中，这是 {@link AffineTransform} 构造函数所需的形式。
     * @return 用于将用户空间的渲染转换为页面方向的矩阵。
     * @see java.awt.geom.AffineTransform
     */
    public double[] getMatrix() {
        double[] matrix = new double[6];

        switch (mOrientation) {

        case LANDSCAPE:
            matrix[0] =  0;     matrix[1] = -1;
            matrix[2] =  1;     matrix[3] =  0;
            matrix[4] =  0;     matrix[5] =  mPaper.getHeight();
            break;

        case PORTRAIT:
            matrix[0] =  1;     matrix[1] =  0;
            matrix[2] =  0;     matrix[3] =  1;
            matrix[4] =  0;     matrix[5] =  0;
            break;

        case REVERSE_LANDSCAPE:
            matrix[0] =  0;                     matrix[1] =  1;
            matrix[2] = -1;                     matrix[3] =  0;
            matrix[4] =  mPaper.getWidth();     matrix[5] =  0;
            break;

        default:
            throw new IllegalArgumentException();
        }

        return matrix;
    }
}
