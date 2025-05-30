/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.image;


/**
 * <code>Kernel</code> 类定义了一个矩阵，该矩阵描述了指定像素及其周围像素如何影响过滤操作输出图像中该像素位置的值。
 * X 原点和 Y 原点表示与正在计算输出值的像素位置对应的内核矩阵元素。
 *
 * @see ConvolveOp
 */
public class Kernel implements Cloneable {
    private int  width;
    private int  height;
    private int  xOrigin;
    private int  yOrigin;
    private float data[];

    private static native void initIDs();
    static {
        ColorModel.loadLibraries();
        initIDs();
    }

    /**
     * 从一个浮点数数组构造一个 <code>Kernel</code> 对象。
     * <code>data</code> 数组的前 <code>width</code>*<code>height</code> 个元素被复制。
     * 如果 <code>data</code> 数组的长度小于 width*height，则抛出 <code>IllegalArgumentException</code>。
     * X 原点为 (width-1)/2，Y 原点为 (height-1)/2。
     * @param width         内核的宽度
     * @param height        内核的高度
     * @param data          内核数据，按行优先顺序排列
     * @throws IllegalArgumentException 如果 <code>data</code> 的长度小于 <code>width</code> 和 <code>height</code> 的乘积
     */
    public Kernel(int width, int height, float data[]) {
        this.width  = width;
        this.height = height;
        this.xOrigin  = (width-1)>>1;
        this.yOrigin  = (height-1)>>1;
        int len = width*height;
        if (data.length < len) {
            throw new IllegalArgumentException("Data array too small "+
                                               "(is "+data.length+
                                               " and should be "+len);
        }
        this.data = new float[len];
        System.arraycopy(data, 0, this.data, 0, len);

    }

    /**
     * 返回此 <code>Kernel</code> 的 X 原点。
     * @return X 原点。
     */
    final public int getXOrigin(){
        return xOrigin;
    }

    /**
     * 返回此 <code>Kernel</code> 的 Y 原点。
     * @return Y 原点。
     */
    final public int getYOrigin() {
        return yOrigin;
    }

    /**
     * 返回此 <code>Kernel</code> 的宽度。
     * @return 此 <code>Kernel</code> 的宽度。
     */
    final public int getWidth() {
        return width;
    }

    /**
     * 返回此 <code>Kernel</code> 的高度。
     * @return 此 <code>Kernel</code> 的高度。
     */
    final public int getHeight() {
        return height;
    }

    /**
     * 以行优先顺序返回内核数据。
     * 如果 <code>data</code> 为 <code>null</code>，则分配一个新的数组。
     * @param data  如果非空，则包含返回的内核数据
     * @return 包含内核数据的 <code>data</code> 数组，按行优先顺序排列；如果 <code>data</code> 为
     *         <code>null</code>，则返回一个新分配的数组，包含内核数据，按行优先顺序排列
     * @throws IllegalArgumentException 如果 <code>data</code> 的长度小于此 <code>Kernel</code> 的大小
     */
    final public float[] getKernelData(float[] data) {
        if (data == null) {
            data = new float[this.data.length];
        }
        else if (data.length < this.data.length) {
            throw new IllegalArgumentException("Data array too small "+
                                               "(should be "+this.data.length+
                                               " but is "+
                                               data.length+" )");
        }
        System.arraycopy(this.data, 0, data, 0, this.data.length);

        return data;
    }

    /**
     * 克隆此对象。
     * @return 此对象的克隆。
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们实现了 Cloneable
            throw new InternalError(e);
        }
    }
}
