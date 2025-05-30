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
 * 此类扩展了 <CODE>DataBuffer</CODE>，并在内部将数据存储为 short 类型。存储在该 <CODE>DataBuffer</CODE> 的 short 数组中的值被视为无符号值。
 * <p>
 * <a name="optimizations">
 * 请注意，某些实现如果可以控制图像数据的存储方式，可能会更高效。
 * 例如，将图像缓存在视频内存中的优化要求实现跟踪对该数据的所有修改。
 * 其他实现如果可以将数据存储在 Java 数组之外的位置，可能会运行得更好。
 * 为了保持与各种优化的最佳兼容性，最好避免使用那些将底层存储暴露为 Java 数组的构造函数和方法，如以下方法的文档中所述。
 * </a>
 */
public final class DataBufferUShort extends DataBuffer
{
    /** 默认数据银行。 */
    short data[];

    /** 所有数据银行 */
    short bankdata[][];

    /**
     * 构造一个具有单个银行和指定大小的无符号 short 基础的 <CODE>DataBuffer</CODE>。
     *
     * @param size <CODE>DataBuffer</CODE> 的大小。
     */
    public DataBufferUShort(int size) {
        super(STABLE, TYPE_USHORT, size);
        data = new short[size];
        bankdata = new short[1][];
        bankdata[0] = data;
    }

    /**
     * 构造一个具有指定数量的银行且所有银行大小相同的无符号 short 基础的 <CODE>DataBuffer</CODE>。
     *
     * @param size <CODE>DataBuffer</CODE> 中银行的大小。
     * @param numBanks <CODE>DataBuffer</CODE> 中的银行数量。
    */
    public DataBufferUShort(int size, int numBanks) {
        super(STABLE, TYPE_USHORT, size, numBanks);
        bankdata = new short[numBanks][];
        for (int i= 0; i < numBanks; i++) {
            bankdata[i] = new short[size];
        }
        data = bankdata[0];
    }

    /**
     * 使用指定数组构造一个具有单个银行的无符号 short 基础的 <CODE>DataBuffer</CODE>。
     * 仅应使用此 <CODE>DataBuffer</CODE> 的访问器使用前 <CODE>size</CODE> 个元素。 <CODE>dataArray</CODE> 必须足够大以容纳 <CODE>size</CODE> 个元素。
     * <p>
     * 请注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的无符号 short 数组。
     * @param size <CODE>DataBuffer</CODE> 银行的大小。
     */
    public DataBufferUShort(short dataArray[], int size) {
        super(UNTRACKABLE, TYPE_USHORT, size);
        if (dataArray == null) {
            throw new NullPointerException("dataArray is null");
        }
        data = dataArray;
        bankdata = new short[1][];
        bankdata[0] = data;
    }

    /**
     * 使用指定数组、大小和偏移量构造一个具有单个银行的无符号 short 基础的 <CODE>DataBuffer</CODE>。 <CODE>dataArray</CODE> 必须至少有 <CODE>offset</CODE> + <CODE>size</CODE> 个元素。 仅应使用此 <CODE>DataBuffer</CODE> 的访问器使用 <CODE>offset</CODE> 到 <CODE>offset</CODE> + <CODE>size</CODE> - 1 之间的元素。
     * <p>
     * 请注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的无符号 short 数组。
     * @param size <CODE>DataBuffer</CODE> 银行的大小。
     * @param offset <CODE>dataArray</CODE> 中的偏移量。
     */
    public DataBufferUShort(short dataArray[], int size, int offset) {
        super(UNTRACKABLE, TYPE_USHORT, size, 1, offset);
        if (dataArray == null) {
            throw new NullPointerException("dataArray is null");
        }
        if ((size+offset) > dataArray.length) {
            throw new IllegalArgumentException("Length of dataArray is less "+
                                               " than size+offset.");
        }
        data = dataArray;
        bankdata = new short[1][];
        bankdata[0] = data;
    }

    /**
     * 使用指定数组构造一个具有指定大小的无符号 short 基础的 <CODE>DataBuffer</CODE>。
     * 银行数量将等于 <CODE>dataArray.length</CODE>。
     * 仅应使用此 <CODE>DataBuffer</CODE> 的访问器使用每个数组的前 <CODE>size</CODE> 个元素。
     * <p>
     * 请注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的无符号 short 数组。
     * @param size <CODE>DataBuffer</CODE> 中银行的大小。
     */
    public DataBufferUShort(short dataArray[][], int size) {
        super(UNTRACKABLE, TYPE_USHORT, size, dataArray.length);
        if (dataArray == null) {
            throw new NullPointerException("dataArray is null");
        }
        for (int i=0; i < dataArray.length; i++) {
            if (dataArray[i] == null) {
                throw new NullPointerException("dataArray["+i+"] is null");
            }
        }

        bankdata = (short[][]) dataArray.clone();
        data = bankdata[0];
    }

    /**
     * 使用指定数组、大小和偏移量构造一个具有无符号 short 基础的 <CODE>DataBuffer</CODE>。
     * 银行数量等于 <CODE>dataArray.length</CODE>。 每个数组必须至少有 <CODE>size</CODE> + 对应的偏移量个元素。 偏移量数组中必须有每个 <CODE>dataArray</CODE> 条目的条目。 对于每个银行，仅应使用此 <CODE>DataBuffer</CODE> 的访问器使用 <CODE>offset</CODE> 到 <CODE>offset</CODE> + <CODE>size</CODE> - 1 之间的元素。
     * <p>
     * 请注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的无符号 short 数组。
     * @param size <CODE>DataBuffer</CODE> 中银行的大小。
     * @param offsets 每个数组的偏移量。
     */
    public DataBufferUShort(short dataArray[][], int size, int offsets[]) {
        super(UNTRACKABLE, TYPE_USHORT, size, dataArray.length, offsets);
        if (dataArray == null) {
            throw new NullPointerException("dataArray is null");
        }
        for (int i=0; i < dataArray.length; i++) {
            if (dataArray[i] == null) {
                throw new NullPointerException("dataArray["+i+"] is null");
            }
            if ((size+offsets[i]) > dataArray[i].length) {
                throw new IllegalArgumentException("Length of dataArray["+i+
                                                   "] is less than size+"+
                                                   "offsets["+i+"].");
            }

        }
        bankdata = (short[][]) dataArray.clone();
        data = bankdata[0];
    }

    /**
     * 返回默认（第一个）无符号 short 数据数组。
     * <p>
     * 请注意，调用此方法可能会导致此 {@code DataBuffer} 对象与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @return 第一个无符号 short 数据数组。
     */
    public short[] getData() {
        theTrackable.setUntrackable();
        return data;
    }

    /**
     * 返回指定银行的数据数组。
     * <p>
     * 请注意，调用此方法可能会导致此 {@code DataBuffer} 对象与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param bank 您想要获取数据数组的银行。
     * @return 指定银行的数据数组。
     */
    public short[] getData(int bank) {
        theTrackable.setUntrackable();
        return bankdata[bank];
    }

    /**
     * 返回所有银行的数据数组。
     * <p>
     * 请注意，调用此方法可能会导致此 {@code DataBuffer} 对象与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
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
     * @param i 您想要获取的数据数组元素。
     * @return 请求的数据数组元素作为整数。
     * @see #setElem(int, int)
     * @see #setElem(int, int, int)
     */
    public int getElem(int i) {
        return (int)(data[i+offset]&0xffff);
    }

    /**
     * 从指定银行返回请求的数据数组元素。
     *
     * @param bank 您想要从其中获取数据数组元素的银行。
     * @param i 您想要获取的数据数组元素。
     * @return 请求的数据数组元素作为整数。
     * @see #setElem(int, int)
     * @see #setElem(int, int, int)
     */
    public int getElem(int bank, int i) {
        return (int)(bankdata[bank][i+offsets[bank]]&0xffff);
    }

    /**
     * 将第一个（默认）银行中请求的数据数组元素设置为指定值。
     *
     * @param i 您想要设置的数据数组元素。
     * @param val 您想要将数据数组元素设置为的整数值。
     * @see #getElem(int)
     * @see #getElem(int, int)
     */
    public void setElem(int i, int val) {
        data[i+offset] = (short)(val&0xffff);
        theTrackable.markDirty();
    }

    /**
     * 将指定银行中请求的数据数组元素设置为给定整数。
     * @param bank 您想要设置数据数组元素的银行。
     * @param i 您想要设置的数据数组元素。
     * @param val 您想要将指定数据数组元素设置为的整数值。
     * @see #getElem(int)
     * @see #getElem(int, int)
     */
    public void setElem(int bank, int i, int val) {
        bankdata[bank][i+offsets[bank]] = (short)(val&0xffff);
        theTrackable.markDirty();
    }
}
