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
 * 此类扩展了 <CODE>DataBuffer</CODE> 并将数据内部存储为字节。
 * 存储在此 <CODE>DataBuffer</CODE> 的字节数组中的值被视为无符号值。
 * <p>
 * <a name="optimizations">
 * 请注意，某些实现如果可以控制图像数据的存储方式，可能会更高效。
 * 例如，将图像缓存在视频内存中的优化要求实现跟踪对该数据的所有修改。
 * 其他实现如果可以将数据存储在 Java 数组之外的位置，可能会运行得更好。
 * 为了保持与各种优化的最佳兼容性，最好避免使用那些将底层存储暴露为 Java 数组的构造函数和方法，如下文这些方法的文档中所述。
 * </a>
 */
public final class DataBufferByte extends DataBuffer
{
    /** 默认数据银行。 */
    byte data[];

    /** 所有数据银行 */
    byte bankdata[][];

    /**
     * 构造一个具有单个银行和指定大小的基于字节的 <CODE>DataBuffer</CODE>。
     *
     * @param size <CODE>DataBuffer</CODE> 的大小。
     */
    public DataBufferByte(int size) {
      super(STABLE, TYPE_BYTE, size);
      data = new byte[size];
      bankdata = new byte[1][];
      bankdata[0] = data;
    }

    /**
     * 构造一个具有指定数量的银行且所有银行都具有指定大小的基于字节的 <CODE>DataBuffer</CODE>。
     *
     * @param size <CODE>DataBuffer</CODE> 中银行的大小。
     * @param numBanks <CODE>DataBuffer</CODE> 中的银行数量。
     */
    public DataBufferByte(int size, int numBanks) {
        super(STABLE, TYPE_BYTE, size, numBanks);
        bankdata = new byte[numBanks][];
        for (int i= 0; i < numBanks; i++) {
            bankdata[i] = new byte[size];
        }
        data = bankdata[0];
    }

    /**
     * 使用指定数组构造一个具有单个银行的基于字节的 <CODE>DataBuffer</CODE>。
     * 仅应使用此 <CODE>DataBuffer</CODE> 的前 <CODE>size</CODE> 个元素。 <CODE>dataArray</CODE> 必须足够大，以容纳 <CODE>size</CODE> 个元素。
     * <p>
     * 请注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的字节数组。
     * @param size <CODE>DataBuffer</CODE> 银行的大小。
     */
    public DataBufferByte(byte dataArray[], int size) {
        super(UNTRACKABLE, TYPE_BYTE, size);
        data = dataArray;
        bankdata = new byte[1][];
        bankdata[0] = data;
    }

    /**
     * 使用指定数组、大小和偏移量构造一个具有单个银行的基于字节的 <CODE>DataBuffer</CODE>。 <CODE>dataArray</CODE> 必须至少有 <CODE>offset</CODE> + <CODE>size</CODE> 个元素。 仅应使用此 <CODE>DataBuffer</CODE> 的 <CODE>offset</CODE> 到 <CODE>offset</CODE> + <CODE>size</CODE> - 1 之间的元素。
     * <p>
     * 请注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的字节数组。
     * @param size <CODE>DataBuffer</CODE> 银行的大小。
     * @param offset 进入 <CODE>dataArray</CODE> 的偏移量。 <CODE>dataArray</CODE> 必须至少有 <CODE>offset</CODE> + <CODE>size</CODE> 个元素。
     */
    public DataBufferByte(byte dataArray[], int size, int offset){
        super(UNTRACKABLE, TYPE_BYTE, size, 1, offset);
        data = dataArray;
        bankdata = new byte[1][];
        bankdata[0] = data;
    }

    /**
     * 使用指定数组构造一个基于字节的 <CODE>DataBuffer</CODE>。
     * 银行数量等于 <CODE>dataArray.length</CODE>。
     * 仅应使用每个数组的前 <CODE>size</CODE> 个元素。
     * <p>
     * 请注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的字节数组。
     * @param size <CODE>DataBuffer</CODE> 中银行的大小。
     */
    public DataBufferByte(byte dataArray[][], int size) {
        super(UNTRACKABLE, TYPE_BYTE, size, dataArray.length);
        bankdata = (byte[][]) dataArray.clone();
        data = bankdata[0];
    }

    /**
     * 使用指定数组、大小和偏移量构造一个基于字节的 <CODE>DataBuffer</CODE>。
     * 银行数量等于 <CODE>dataArray.length</CODE>。 每个数组必须至少有 <CODE>size</CODE> + 对应的 <CODE>offset</CODE> 个元素。
     * <CODE>offset</CODE> 数组中必须有每个 <CODE>dataArray</CODE> 条目的条目。 对于每个银行，仅应使用 <CODE>offset</CODE> 到 <CODE>offset</CODE> + <CODE>size</CODE> - 1 之间的元素。
     * <p>
     * 请注意，通过此构造函数创建的 <CODE>DataBuffer</CODE> 对象可能与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @param dataArray <CODE>DataBuffer</CODE> 的字节数组。
     * @param size <CODE>DataBuffer</CODE> 中银行的大小。
     * @param offsets 每个数组的偏移量。
     */
    public DataBufferByte(byte dataArray[][], int size, int offsets[]) {
        super(UNTRACKABLE, TYPE_BYTE, size, dataArray.length, offsets);
        bankdata = (byte[][]) dataArray.clone();
        data = bankdata[0];
    }

    /**
     * 返回默认（第一个）字节数据数组。
     * <p>
     * 请注意，调用此方法可能会导致此 {@code DataBuffer} 对象与某些实现使用的 <a href="#optimizations">性能优化</a>（如将关联图像缓存在视频内存中）不兼容。
     *
     * @return 第一个字节数据数组。
     */
    public byte[] getData() {
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
    public byte[] getData(int bank) {
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
    public byte[][] getBankData() {
        theTrackable.setUntrackable();
        return (byte[][]) bankdata.clone();
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
        return (int)(data[i+offset]) & 0xff;
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
        return (int)(bankdata[bank][i+offsets[bank]]) & 0xff;
    }

    /**
     * 将请求的数据数组元素设置为指定值。
     *
     * @param i 您想要设置的数据数组元素。
     * @param val 您想要设置的数据数组元素的整数值。
     * @see #getElem(int)
     * @see #getElem(int, int)
     */
    public void setElem(int i, int val) {
        data[i+offset] = (byte)val;
        theTrackable.markDirty();
    }

    /**
     * 将指定银行的请求数据数组元素设置为给定整数。
     * @param bank 您想要设置数据数组元素的银行。
     * @param i 您想要设置的数据数组元素。
     * @param val 您想要设置的指定数据数组元素的整数值。
     * @see #getElem(int)
     * @see #getElem(int, int)
     */
    public void setElem(int bank, int i, int val) {
        bankdata[bank][i+offsets[bank]] = (byte)val;
        theTrackable.markDirty();
    }
}
