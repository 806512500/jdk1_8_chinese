/*
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
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

/* ****************************************************************
 ******************************************************************
 ******************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997
 *** As  an unpublished  work pursuant to Title 17 of the United
 *** States Code.  All rights reserved.
 ******************************************************************
 ******************************************************************
 ******************************************************************/

package java.awt.image;

import static sun.java2d.StateTrackable.State.*;

/**
 * 该类扩展了 <CODE>DataBuffer</CODE> 并将数据内部存储为整数。
 * <p>
 * <a name="optimizations">
 * 注意，某些实现如果可以控制图像数据的存储方式，可能会运行得更高效。
 * 例如，将图像缓存在视频内存中的优化要求实现跟踪对该数据的所有修改。
 * 其他实现如果可以将数据存储在 Java 数组之外的位置，可能会运行得更好。
 * 为了保持与各种优化的最佳兼容性，最好避免使用那些将底层存储暴露为 Java 数组的构造函数和方法，如下面这些方法的文档中所述。
 * </a>
 */
public final class DataBufferInt extends DataBuffer
{
    /** 默认数据银行。 */
    int data[];

    /** 所有数据银行 */
    int bankdata[][];

    /**
     * 构造一个具有单个银行和指定大小的基于整数的 <CODE>DataBuffer</CODE>。
     *
     * @param size <CODE>DataBuffer</CODE> 的大小。
     */
    public DataBufferInt(int size) {
        super(STABLE, TYPE_INT, size);
        data = new int[size];
        bankdata = new int[1][];
        bankdata[0] = data;
    }

    /**
     * 构造一个具有指定数量的银行的基于整数的 <CODE>DataBuffer</CODE>，所有银行的大小都相同。
     *
     * @param size <CODE>DataBuffer</CODE> 中银行的大小。
     * @param numBanks <CODE>DataBuffer</CODE> 中的银行数量。
     */
    public DataBufferInt(int size, int numBanks) {
        super(STABLE, TYPE_INT, size, numBanks);
        bankdata = new int[numBanks][];
        for (int i= 0; i < numBanks; i++) {
            bankdata[i] = new int[size];
        }
        data = bankdata[0];
    }

    /**
     * 使用指定数组构造一个具有单个银行的基于整数的 <CODE>DataBuffer</CODE>。
     * 只有前 <CODE>size</CODE> 个元素应被此 <CODE>DataBuffer</CODE> 的访问器使用。 <CODE>dataArray</CODE> 必须足够大以容纳 <CODE>size</CODE> 个元素。
     * <p>
     * 注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的整数数组。
     * @param size <CODE>DataBuffer</CODE> 银行的大小。
     */
    public DataBufferInt(int dataArray[], int size) {
        super(UNTRACKABLE, TYPE_INT, size);
        data = dataArray;
        bankdata = new int[1][];
        bankdata[0] = data;
    }

    /**
     * 使用指定数组、大小和偏移量构造一个具有单个银行的基于整数的 <CODE>DataBuffer</CODE>。 <CODE>dataArray</CODE> 必须至少有 <CODE>offset</CODE> + <CODE>size</CODE> 个元素。 只有元素 <CODE>offset</CODE> 到 <CODE>offset</CODE> + <CODE>size</CODE> - 1
     * 应被此 <CODE>DataBuffer</CODE> 的访问器使用。
     * <p>
     * 注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的整数数组。
     * @param size <CODE>DataBuffer</CODE> 银行的大小。
     * @param offset <CODE>dataArray</CODE> 中的偏移量。
     */
    public DataBufferInt(int dataArray[], int size, int offset) {
        super(UNTRACKABLE, TYPE_INT, size, 1, offset);
        data = dataArray;
        bankdata = new int[1][];
        bankdata[0] = data;
    }

    /**
     * 使用指定数组构造一个基于整数的 <CODE>DataBuffer</CODE>。
     * 银行数量将等于 <CODE>dataArray.length</CODE>。
     * 只有每个数组的前 <CODE>size</CODE> 个元素应被此 <CODE>DataBuffer</CODE> 的访问器使用。
     * <p>
     * 注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的整数数组。
     * @param size <CODE>DataBuffer</CODE> 中银行的大小。
     */
    public DataBufferInt(int dataArray[][], int size) {
        super(UNTRACKABLE, TYPE_INT, size, dataArray.length);
        bankdata = (int [][]) dataArray.clone();
        data = bankdata[0];
    }

    /**
     * 使用指定数组、大小和偏移量构造一个基于整数的 <CODE>DataBuffer</CODE>。
     * 银行数量等于 <CODE>dataArray.length</CODE>。 每个数组必须至少有 <CODE>size</CODE> + 对应偏移量的大小。 偏移量数组中必须有 <CODE>dataArray</CODE> 中每个条目的条目。 对于每个银行，只有 <CODE>offset</CODE> 到
     * <CODE>offset</CODE> + <CODE>size</CODE> - 1 的元素应被此 <CODE>DataBuffer</CODE> 的访问器使用。
     * <p>
     * 注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的整数数组。
     * @param size <CODE>DataBuffer</CODE> 中银行的大小。
     * @param offsets 每个数组中的偏移量。
     */
    public DataBufferInt(int dataArray[][], int size, int offsets[]) {
        super(UNTRACKABLE, TYPE_INT, size, dataArray.length, offsets);
        bankdata = (int [][]) dataArray.clone();
        data = bankdata[0];
    }

    /**
     * 返回 <CODE>DataBuffer</CODE> 中的默认（第一个）整数数据数组。
     * <p>
     * 注意，调用此方法可能导致此 <CODE>DataBuffer</CODE> 对象与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @return 第一个整数数据数组。
     */
    public int[] getData() {
        theTrackable.setUntrackable();
        return data;
    }

    /**
     * 返回指定银行的数据数组。
     * <p>
     * 注意，调用此方法可能导致此 <CODE>DataBuffer</CODE> 对象与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param bank 您想要获取数据数组的银行。
     * @return 指定银行的数据数组。
     */
    public int[] getData(int bank) {
        theTrackable.setUntrackable();
        return bankdata[bank];
    }

    /**
     * 返回所有银行的数据数组。
     * <p>
     * 注意，调用此方法可能导致此 <CODE>DataBuffer</CODE> 对象与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @return 所有数据数组。
     */
    public int[][] getBankData() {
        theTrackable.setUntrackable();
        return (int [][]) bankdata.clone();
    }

    /**
     * 从第一个（默认）银行返回请求的数据数组元素。
     *
     * @param i 您想要获取的数据数组元素。
     * @return 请求的数据数组元素作为整数。
     * @see #setElem(int, int)
     * @see #setElem(int, int, int)
     */
    public int getElem(int i) {
        return data[i+offset];
    }

    /**
     * 从指定银行返回请求的数据数组元素。
     *
     * @param bank 您想要从中获取数据数组元素的银行。
     * @param i 您想要获取的数据数组元素。
     * @return 请求的数据数组元素作为整数。
     * @see #setElem(int, int)
     * @see #setElem(int, int, int)
     */
    public int getElem(int bank, int i) {
        return bankdata[bank][i+offsets[bank]];
    }

    /**
     * 将第一个（默认）银行中请求的数据数组元素设置为指定值。
     *
     * @param i 您想要设置的数据数组元素。
     * @param val 您想要设置的数据数组元素的整数值。
     * @see #getElem(int)
     * @see #getElem(int, int)
     */
    public void setElem(int i, int val) {
        data[i+offset] = val;
        theTrackable.markDirty();
    }

    /**
     * 将指定银行中请求的数据数组元素设置为整数值 <CODE>i</CODE>。
     * @param bank 您想要设置数据数组元素的银行。
     * @param i 您想要设置的数据数组元素。
     * @param val 您想要设置的指定数据数组元素的整数值。
     * @see #getElem(int)
     * @see #getElem(int, int)
     */
    public void setElem(int bank, int i, int val) {
        bankdata[bank][i+offsets[bank]] = (int)val;
        theTrackable.markDirty();
    }
}
