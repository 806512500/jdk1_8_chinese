/*
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
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

import static sun.java2d.StateTrackable.State.*;

/**
 * 该类扩展了 <code>DataBuffer</code> 并在内部以 <code>double</code> 形式存储数据。
 * <p>
 * <a name="optimizations">
 * 请注意，某些实现可能在控制图像数据存储方式时效率更高。
 * 例如，将图像缓存在视频内存中的优化要求实现跟踪所有对该数据的修改。
 * 其他实现可能在将数据存储在 Java 数组之外的位置时运行得更好。
 * 为了保持与各种优化的最佳兼容性，最好避免使用以下方法中注明的构造函数和方法，这些方法会将底层存储暴露为 Java 数组。
 * </a>
 *
 * @since 1.4
 */

public final class DataBufferDouble extends DataBuffer {

    /** 数据库数组。 */
    double bankdata[][];

    /** 默认数据银行的引用。 */
    double data[];

    /**
     * 构造一个具有指定大小的 <code>double</code> 基础的 <code>DataBuffer</code>。
     *
     * @param size <code>DataBuffer</code> 中的元素数量。
     */
    public DataBufferDouble(int size) {
        super(STABLE, TYPE_DOUBLE, size);
        data = new double[size];
        bankdata = new double[1][];
        bankdata[0] = data;
    }

    /**
     * 构造一个具有指定数量银行的 <code>double</code> 基础的 <code>DataBuffer</code>，所有银行的大小都相同。
     *
     * @param size 每个银行中的元素数量。
     * @param numBanks <code>DataBuffer</code> 中的银行数量。
     */
    public DataBufferDouble(int size, int numBanks) {
        super(STABLE, TYPE_DOUBLE, size, numBanks);
        bankdata = new double[numBanks][];
        for (int i= 0; i < numBanks; i++) {
            bankdata[i] = new double[size];
        }
        data = bankdata[0];
    }

    /**
     * 使用指定的数据数组构造一个 <code>double</code> 基础的 <code>DataBuffer</code>。只有前 <code>size</code> 个元素可用于此 <code>DataBuffer</code>。数组必须足够大以容纳 <code>size</code> 个元素。
     * <p>
     * 请注意，通过此构造函数创建的 <code>DataBuffer</code> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray 用作此 <code>DataBuffer</code> 的第一个且唯一银行的 <code>double</code> 数组。
     * @param size 要使用的数组元素数量。
     */
    public DataBufferDouble(double dataArray[], int size) {
        super(UNTRACKABLE, TYPE_DOUBLE, size);
        data = dataArray;
        bankdata = new double[1][];
        bankdata[0] = data;
    }

    /**
     * 使用指定的数据数组构造一个 <code>double</code> 基础的 <code>DataBuffer</code>。只有 <code>offset</code> 和 <code>offset + size - 1</code> 之间的元素可用于此 <code>DataBuffer</code>。数组必须足够大以容纳 <code>offset + size</code> 个元素。
     * <p>
     * 请注意，通过此构造函数创建的 <code>DataBuffer</code> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray 用作此 <code>DataBuffer</code> 的第一个且唯一银行的 <code>double</code> 数组。
     * @param size 要使用的数组元素数量。
     * @param offset 第一个要使用的数组元素的偏移量。
     */
    public DataBufferDouble(double dataArray[], int size, int offset) {
        super(UNTRACKABLE, TYPE_DOUBLE, size, 1, offset);
        data = dataArray;
        bankdata = new double[1][];
        bankdata[0] = data;
    }

    /**
     * 使用指定的数据数组构造一个 <code>double</code> 基础的 <code>DataBuffer</code>。只有每个数组的前 <code>size</code> 个元素可用于此 <code>DataBuffer</code>。银行数量等于 <code>dataArray.length</code>。
     * <p>
     * 请注意，通过此构造函数创建的 <code>DataBuffer</code> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray 用作此 <code>DataBuffer</code> 银行的 <code>double</code> 数组数组。
     * @param size 每个数组要使用的元素数量。
     */
    public DataBufferDouble(double dataArray[][], int size) {
        super(UNTRACKABLE, TYPE_DOUBLE, size, dataArray.length);
        bankdata = (double[][]) dataArray.clone();
        data = bankdata[0];
    }

    /**
     * 使用指定的数据数组、大小和每个银行的偏移量构造一个 <code>double</code> 基础的 <code>DataBuffer</code>。银行数量等于 <code>dataArray.length</code>。每个数组必须至少与 <code>size</code> 加上相应的偏移量一样大。每个数据数组在 <code>offsets</code> 数组中必须有一个条目。
     * <p>
     * 请注意，通过此构造函数创建的 <code>DataBuffer</code> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray 用作此 <code>DataBuffer</code> 银行的 <code>double</code> 数组数组。
     * @param size 每个数组要使用的元素数量。
     * @param offsets 每个银行的整数偏移量数组。
     */
    public DataBufferDouble(double dataArray[][], int size, int offsets[]) {
        super(UNTRACKABLE, TYPE_DOUBLE, size, dataArray.length, offsets);
        bankdata = (double[][]) dataArray.clone();
        data = bankdata[0];
    }

    /**
     * 返回默认（第一个）<code>double</code> 数据数组。
     * <p>
     * 请注意，调用此方法可能会导致此 <code>DataBuffer</code> 对象与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @return 第一个 double 数据数组。
     */
    public double[] getData() {
        theTrackable.setUntrackable();
        return data;
    }

    /**
     * 返回指定银行的数据数组。
     * <p>
     * 请注意，调用此方法可能会导致此 <code>DataBuffer</code> 对象与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param bank 数据数组
     * @return 由 <code>bank</code> 指定的数据数组。
     */
    public double[] getData(int bank) {
        theTrackable.setUntrackable();
        return bankdata[bank];
    }

    /**
     * 返回所有银行的数据数组。
     * <p>
     * 请注意，调用此方法可能会导致此 <code>DataBuffer</code> 对象与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @return 此数据缓冲区中的所有数据数组。
     */
    public double[][] getBankData() {
        theTrackable.setUntrackable();
        return (double[][]) bankdata.clone();
    }

    /**
     * 从第一个（默认）银行返回请求的数据数组元素作为 <code>int</code>。
     *
     * @param i 所需的数据数组元素。
     * @return 数据条目作为 <code>int</code>。
     * @see #setElem(int, int)
     * @see #setElem(int, int, int)
     */
    public int getElem(int i) {
        return (int)(data[i+offset]);
    }

    /**
     * 从指定的银行返回请求的数据数组元素作为 <code>int</code>。
     *
     * @param bank 银行编号。
     * @param i 所需的数据数组元素。
     *
     * @return 数据条目作为 <code>int</code>。
     * @see #setElem(int, int)
     * @see #setElem(int, int, int)
     */
    public int getElem(int bank, int i) {
        return (int)(bankdata[bank][i+offsets[bank]]);
    }

    /**
     * 将第一个（默认）银行中请求的数据数组元素设置为给定的 <code>int</code>。
     *
     * @param i 所需的数据数组元素。
     * @param val 要设置的值。
     * @see #getElem(int)
     * @see #getElem(int, int)
     */
    public void setElem(int i, int val) {
        data[i+offset] = (double)val;
        theTrackable.markDirty();
    }

    /**
     * 将指定银行中请求的数据数组元素设置为给定的 <code>int</code>。
     *
     * @param bank 银行编号。
     * @param i 所需的数据数组元素。
     * @param val 要设置的值。
     * @see #getElem(int)
     * @see #getElem(int, int)
     */
    public void setElem(int bank, int i, int val) {
        bankdata[bank][i+offsets[bank]] = (double)val;
        theTrackable.markDirty();
    }

    /**
     * 从第一个（默认）银行返回请求的数据数组元素作为 <code>float</code>。
     *
     * @param i 所需的数据数组元素。
     *
     * @return 数据条目作为 <code>float</code>。
     * @see #setElemFloat(int, float)
     * @see #setElemFloat(int, int, float)
     */
    public float getElemFloat(int i) {
        return (float)data[i+offset];
    }

    /**
     * 从指定的银行返回请求的数据数组元素作为 <code>float</code>。
     *
     * @param bank 银行编号。
     * @param i 所需的数据数组元素。
     *
     * @return 数据条目作为 <code>float</code>。
     * @see #setElemFloat(int, float)
     * @see #setElemFloat(int, int, float)
     */
    public float getElemFloat(int bank, int i) {
        return (float)bankdata[bank][i+offsets[bank]];
    }

    /**
     * 将第一个（默认）银行中请求的数据数组元素设置为给定的 <code>float</code>。
     *
     * @param i 所需的数据数组元素。
     * @param val 要设置的值。
     * @see #getElemFloat(int)
     * @see #getElemFloat(int, int)
     */
    public void setElemFloat(int i, float val) {
        data[i+offset] = (double)val;
        theTrackable.markDirty();
    }

    /**
     * 将指定银行中请求的数据数组元素设置为给定的 <code>float</code>。
     *
     * @param bank 银行编号。
     * @param i 所需的数据数组元素。
     * @param val 要设置的值。
     * @see #getElemFloat(int)
     * @see #getElemFloat(int, int)
     */
    public void setElemFloat(int bank, int i, float val) {
        bankdata[bank][i+offsets[bank]] = (double)val;
        theTrackable.markDirty();
    }

    /**
     * 从第一个（默认）银行返回请求的数据数组元素作为 <code>double</code>。
     *
     * @param i 所需的数据数组元素。
     *
     * @return 数据条目作为 <code>double</code>。
     * @see #setElemDouble(int, double)
     * @see #setElemDouble(int, int, double)
     */
    public double getElemDouble(int i) {
        return data[i+offset];
    }

    /**
     * 从指定的银行返回请求的数据数组元素作为 <code>double</code>。
     *
     * @param bank 银行编号。
     * @param i 所需的数据数组元素。
     *
     * @return 数据条目作为 <code>double</code>。
     * @see #setElemDouble(int, double)
     * @see #setElemDouble(int, int, double)
     */
    public double getElemDouble(int bank, int i) {
        return bankdata[bank][i+offsets[bank]];
    }

    /**
     * 将第一个（默认）银行中请求的数据数组元素设置为给定的 <code>double</code>。
     *
     * @param i 所需的数据数组元素。
     * @param val 要设置的值。
     * @see #getElemDouble(int)
     * @see #getElemDouble(int, int)
     */
    public void setElemDouble(int i, double val) {
        data[i+offset] = val;
        theTrackable.markDirty();
    }

    /**
     * 将指定银行中请求的数据数组元素设置为给定的 <code>double</code>。
     *
     * @param bank 银行编号。
     * @param i 所需的数据数组元素。
     * @param val 要设置的值。
     * @see #getElemDouble(int)
     * @see #getElemDouble(int, int)
     */
    public void setElemDouble(int bank, int i, double val) {
        bankdata[bank][i+offsets[bank]] = val;
        theTrackable.markDirty();
    }
}
