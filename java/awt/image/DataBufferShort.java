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
 * 该类扩展了 <CODE>DataBuffer</CODE> 并将数据内部存储为 short 类型。
 * <p>
 * <a name="optimizations">
 * 注意，某些实现如果能够控制图像数据的存储方式，可能会运行得更高效。
 * 例如，将图像缓存在视频内存中的优化要求实现能够跟踪对该数据的所有修改。
 * 其他实现如果能够将数据存储在 Java 数组之外的位置，可能会运行得更好。
 * 为了保持与各种优化的最佳兼容性，最好避免使用那些将底层存储暴露为 Java 数组的构造函数和方法，如以下方法文档中所述。
 * </a>
 */
public final class DataBufferShort extends DataBuffer
{
    /** 默认数据银行。 */
    short data[];

    /** 所有数据银行 */
    short bankdata[][];

    /**
     * 构造一个基于 short 的 <CODE>DataBuffer</CODE>，具有单个银行和指定的大小。
     *
     * @param size <CODE>DataBuffer</CODE> 的大小。
     */
    public DataBufferShort(int size) {
        super(STABLE, TYPE_SHORT,size);
        data = new short[size];
        bankdata = new short[1][];
        bankdata[0] = data;
    }

    /**
     * 构造一个基于 short 的 <CODE>DataBuffer</CODE>，具有指定数量的银行，所有银行的大小都相同。
     *
     * @param size <CODE>DataBuffer</CODE> 中银行的大小。
     * @param numBanks <CODE>DataBuffer</CODE> 中的银行数量。
     */
    public DataBufferShort(int size, int numBanks) {
        super(STABLE, TYPE_SHORT,size,numBanks);
        bankdata = new short[numBanks][];
        for (int i= 0; i < numBanks; i++) {
            bankdata[i] = new short[size];
        }
        data = bankdata[0];
    }

    /**
     * 使用指定的数组构造一个基于 short 的 <CODE>DataBuffer</CODE>，具有单个银行。
     * 仅应使用 <CODE>dataArray</CODE> 的前 <CODE>size</CODE> 个元素。
     * <CODE>dataArray</CODE> 必须足够大，以容纳 <CODE>size</CODE> 个元素。
     * <p>
     * 注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的 short 数组。
     * @param size <CODE>DataBuffer</CODE> 银行的大小。
     */
    public DataBufferShort(short dataArray[], int size) {
        super(UNTRACKABLE, TYPE_SHORT, size);
        data = dataArray;
        bankdata = new short[1][];
        bankdata[0] = data;
    }

    /**
     * 使用指定的数组、大小和偏移量构造一个基于 short 的 <CODE>DataBuffer</CODE>，具有单个银行。
     * <CODE>dataArray</CODE> 必须至少有 <CODE>offset</CODE> + <CODE>size</CODE> 个元素。
     * 仅应使用 <CODE>offset</CODE> 到 <CODE>offset</CODE> + <CODE>size</CODE> - 1 之间的元素。
     * <p>
     * 注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的 short 数组。
     * @param size <CODE>DataBuffer</CODE> 银行的大小。
     * @param offset <CODE>dataArray</CODE> 的偏移量。
     */
    public DataBufferShort(short dataArray[], int size, int offset) {
        super(UNTRACKABLE, TYPE_SHORT, size, 1, offset);
        data = dataArray;
        bankdata = new short[1][];
        bankdata[0] = data;
    }

    /**
     * 使用指定的数组构造一个基于 short 的 <CODE>DataBuffer</CODE>。
     * 银行数量将等于 <CODE>dataArray.length</CODE>。
     * 仅应使用每个数组的前 <CODE>size</CODE> 个元素。
     * <p>
     * 注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的 short 数组。
     * @param size <CODE>DataBuffer</CODE> 中银行的大小。
     */
    public DataBufferShort(short dataArray[][], int size) {
        super(UNTRACKABLE, TYPE_SHORT, size, dataArray.length);
        bankdata = (short[][]) dataArray.clone();
        data = bankdata[0];
    }

    /**
     * 使用指定的数组、大小和偏移量构造一个基于 short 的 <CODE>DataBuffer</CODE>。
     * 银行数量等于 <CODE>dataArray.length</CODE>。每个数组必须至少有 <CODE>size</CODE> + 对应的偏移量个元素。
     * 必须为每个 <CODE>dataArray</CODE> 入口提供一个偏移量数组入口。对于每个银行，仅应使用 <CODE>offset</CODE> 到 <CODE>offset</CODE> + <CODE>size</CODE> - 1 之间的元素。
     * <p>
     * 注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的 short 数组。
     * @param size <CODE>DataBuffer</CODE> 中银行的大小。
     * @param offsets 每个数组的偏移量。
     */
    public DataBufferShort(short dataArray[][], int size, int offsets[]) {
        super(UNTRACKABLE, TYPE_SHORT, size, dataArray.length, offsets);
        bankdata = (short[][]) dataArray.clone();
        data = bankdata[0];
    }

    /**
     * 返回默认（第一个）short 数据数组。
     * <p>
     * 注意，调用此方法可能会导致此 <CODE>DataBuffer</CODE> 对象与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @return 第一个 short 数据数组。
     */
    public short[] getData() {
        theTrackable.setUntrackable();
        return data;
    }

    /**
     * 返回指定银行的数据数组。
     * <p>
     * 注意，调用此方法可能会导致此 <CODE>DataBuffer</CODE> 对象与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param bank 您想获取数据数组的银行。
     * @return 指定银行的数据数组。
     */
    public short[] getData(int bank) {
        theTrackable.setUntrackable();
        return bankdata[bank];
    }

    /**
     * 返回所有银行的数据数组。
     * <p>
     * 注意，调用此方法可能会导致此 <CODE>DataBuffer</CODE> 对象与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @return 所有的数据数组。
     */
    public short[][] getBankData() {
        theTrackable.setUntrackable();
        return (short[][]) bankdata.clone();
    }

    /**
     * 从第一个（默认）银行返回请求的数据数组元素。
     *
     * @param i 您想获取的数据数组元素。
     * @return 请求的数据数组元素作为整数。
     * @see #setElem(int, int)
     * @see #setElem(int, int, int)
     */
    public int getElem(int i) {
        return (int)(data[i+offset]);
    }

    /**
     * 从指定的银行返回请求的数据数组元素。
     *
     * @param bank 您想从中获取数据数组元素的银行。
     * @param i 您想获取的数据数组元素。
     * @return 请求的数据数组元素作为整数。
     * @see #setElem(int, int)
     * @see #setElem(int, int, int)
     */
    public int getElem(int bank, int i) {
        return (int)(bankdata[bank][i+offsets[bank]]);
    }

    /**
     * 将请求的数据数组元素在第一个（默认）银行设置为指定的值。
     *
     * @param i 您想设置的数据数组元素。
     * @param val 您想将数据数组元素设置为的整数值。
     * @see #getElem(int)
     * @see #getElem(int, int)
     */
    public void setElem(int i, int val) {
        data[i+offset] = (short)val;
        theTrackable.markDirty();
    }

    /**
     * 将请求的数据数组元素在指定的银行从给定的整数设置。
     * @param bank 您想设置数据数组元素的银行。
     * @param i 您想设置的数据数组元素。
     * @param val 您想将指定的数据数组元素设置为的整数值。
     * @see #getElem(int)
     * @see #getElem(int, int)
     */
    public void setElem(int bank, int i, int val) {
        bankdata[bank][i+offsets[bank]] = (short)val;
        theTrackable.markDirty();
    }
}
