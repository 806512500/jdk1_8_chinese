
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

import sun.java2d.StateTrackable.State;
import static sun.java2d.StateTrackable.State.*;
import sun.java2d.StateTrackableDelegate;

import sun.awt.image.SunWritableRaster;

import java.lang.annotation.Native;

/**
 * 本类用于包装一个或多个数据数组。每个数据数组称为一个银行。访问 DataBuffer 的银行的方法包括带银行指定符和不带银行指定符的。没有银行指定符的方法使用默认的第 0 个银行。DataBuffer 可以选择每个银行的偏移量，以便在现有数组中使用数据，即使有趣的数据不从数组位置零开始。获取或设置第 0 个元素的银行，使用 (0 + 偏移量) 位置的数组元素。size 字段指定数据数组中可用的数据量。对于给定的银行，size + 偏移量永远不应大于关联数据数组的长度。数据缓冲区的数据类型表示数据数组的类型，也可能表示其他语义，例如在字节数组中存储无符号 8 位数据。数据类型可以是 TYPE_UNDEFINED 或以下定义的类型之一。将来可能会添加其他类型。通常，类 DataBuffer 的对象将被向下转换为其特定数据类型子类之一，以访问特定于数据类型的方法，以提高性能。目前，Java 2D™ API 图像类使用 TYPE_BYTE、TYPE_USHORT、TYPE_INT、TYPE_SHORT、TYPE_FLOAT 和 TYPE_DOUBLE DataBuffers 来存储图像数据。
 * @see java.awt.image.Raster
 * @see java.awt.image.SampleModel
 */
public abstract class DataBuffer {

    /** 无符号字节数据的标签。 */
    @Native public static final int TYPE_BYTE  = 0;

    /** 无符号短整型数据的标签。 */
    @Native public static final int TYPE_USHORT = 1;

    /** 有符号短整型数据的标签。保留供将来使用。 */
    @Native public static final int TYPE_SHORT = 2;

    /** 整型数据的标签。 */
    @Native public static final int TYPE_INT   = 3;

    /** 浮点型数据的标签。保留供将来使用。 */
    @Native public static final int TYPE_FLOAT  = 4;

    /** 双精度浮点型数据的标签。保留供将来使用。 */
    @Native public static final int TYPE_DOUBLE  = 5;

    /** 未定义数据的标签。 */
    @Native public static final int TYPE_UNDEFINED = 32;

    /** 此 DataBuffer 的数据类型。 */
    protected int dataType;

    /** 此 DataBuffer 中的银行数量。 */
    protected int banks;

    /** 默认（第一个）银行中获取第一个元素的偏移量。 */
    protected int offset;

    /** 所有银行的可用大小。 */
    protected int size;

    /** 所有银行的偏移量。 */
    protected int offsets[];

    /* 当前的 StateTrackable 状态。 */
    StateTrackableDelegate theTrackable;

    /** 由 DataType 标签索引的数据类型大小。 */
    private static final int dataTypeSize[] = {8,16,16,32,32,64};

    /** 根据数据类型标签返回数据类型大小（以位为单位）。
      * @param type 定义的数据类型标签之一的值
      * @return 数据类型的大小
      * @throws IllegalArgumentException 如果 <code>type</code> 小于零或大于 {@link #TYPE_DOUBLE}
      */
    public static int getDataTypeSize(int type) {
        if (type < TYPE_BYTE || type > TYPE_DOUBLE) {
            throw new IllegalArgumentException("未知数据类型 " + type);
        }
        return dataTypeSize[type];
    }

    /**
     *  构造一个包含指定数据类型和大小的单个银行的 DataBuffer。
     *
     *  @param dataType 此 <code>DataBuffer</code> 的数据类型
     *  @param size 银行的大小
     */
    protected DataBuffer(int dataType, int size) {
        this(UNTRACKABLE, dataType, size);
    }

    /**
     *  构造一个包含指定数据类型和大小的单个银行的 DataBuffer，具有指示的初始 {@link State State}。
     *
     *  @param initialState 数据的初始 {@link State State} 状态
     *  @param dataType 此 <code>DataBuffer</code> 的数据类型
     *  @param size 银行的大小
     *  @since 1.7
     */
    DataBuffer(State initialState,
               int dataType, int size)
    {
        this.theTrackable = StateTrackableDelegate.createInstance(initialState);
        this.dataType = dataType;
        this.banks = 1;
        this.size = size;
        this.offset = 0;
        this.offsets = new int[1];  // new 初始化为 0
    }

    /**
     *  构造一个包含指定数量银行的 DataBuffer。每个银行具有指定的大小和 0 偏移量。
     *
     *  @param dataType 此 <code>DataBuffer</code> 的数据类型
     *  @param size 银行的大小
     *  @param numBanks 此 <code>DataBuffer</code> 中的银行数量
     */
    protected DataBuffer(int dataType, int size, int numBanks) {
        this(UNTRACKABLE, dataType, size, numBanks);
    }

    /**
     *  构造一个包含指定数量银行的 DataBuffer，具有指示的初始 {@link State State}。每个银行具有指定的大小和 0 偏移量。
     *
     *  @param initialState 数据的初始 {@link State State} 状态
     *  @param dataType 此 <code>DataBuffer</code> 的数据类型
     *  @param size 银行的大小
     *  @param numBanks 此 <code>DataBuffer</code> 中的银行数量
     *  @since 1.7
     */
    DataBuffer(State initialState,
               int dataType, int size, int numBanks)
    {
        this.theTrackable = StateTrackableDelegate.createInstance(initialState);
        this.dataType = dataType;
        this.banks = numBanks;
        this.size = size;
        this.offset = 0;
        this.offsets = new int[banks]; // new 初始化为 0
    }

    /**
     *  构造一个包含指定数量银行的 DataBuffer。每个银行具有指定的数据类型、大小和偏移量。
     *
     *  @param dataType 此 <code>DataBuffer</code> 的数据类型
     *  @param size 银行的大小
     *  @param numBanks 此 <code>DataBuffer</code> 中的银行数量
     *  @param offset 每个银行的偏移量
     */
    protected DataBuffer(int dataType, int size, int numBanks, int offset) {
        this(UNTRACKABLE, dataType, size, numBanks, offset);
    }

    /**
     *  构造一个包含指定数量银行的 DataBuffer，具有指示的初始 {@link State State}。每个银行具有指定的数据类型、大小和偏移量。
     *
     *  @param initialState 数据的初始 {@link State State} 状态
     *  @param dataType 此 <code>DataBuffer</code> 的数据类型
     *  @param size 银行的大小
     *  @param numBanks 此 <code>DataBuffer</code> 中的银行数量
     *  @param offset 每个银行的偏移量
     *  @since 1.7
     */
    DataBuffer(State initialState,
               int dataType, int size, int numBanks, int offset)
    {
        this.theTrackable = StateTrackableDelegate.createInstance(initialState);
        this.dataType = dataType;
        this.banks = numBanks;
        this.size = size;
        this.offset = offset;
        this.offsets = new int[numBanks];
        for (int i = 0; i < numBanks; i++) {
            this.offsets[i] = offset;
        }
    }

    /**
     *  构造一个包含指定数量银行的 DataBuffer。每个银行具有指定的数据类型和大小。每个银行的偏移量由 offsets 数组中的相应条目指定。
     *
     *  @param dataType 此 <code>DataBuffer</code> 的数据类型
     *  @param size 银行的大小
     *  @param numBanks 此 <code>DataBuffer</code> 中的银行数量
     *  @param offsets 包含每个银行偏移量的数组。
     *  @throws ArrayIndexOutOfBoundsException 如果 <code>numBanks</code>
     *          不等于 <code>offsets</code> 的长度
     */
    protected DataBuffer(int dataType, int size, int numBanks, int offsets[]) {
        this(UNTRACKABLE, dataType, size, numBanks, offsets);
    }

    /**
     *  构造一个包含指定数量银行的 DataBuffer，具有指示的初始 {@link State State}。每个银行具有指定的数据类型和大小。每个银行的偏移量由 offsets 数组中的相应条目指定。
     *
     *  @param initialState 数据的初始 {@link State State} 状态
     *  @param dataType 此 <code>DataBuffer</code> 的数据类型
     *  @param size 银行的大小
     *  @param numBanks 此 <code>DataBuffer</code> 中的银行数量
     *  @param offsets 包含每个银行偏移量的数组。
     *  @throws ArrayIndexOutOfBoundsException 如果 <code>numBanks</code>
     *          不等于 <code>offsets</code> 的长度
     *  @since 1.7
     */
    DataBuffer(State initialState,
               int dataType, int size, int numBanks, int offsets[])
    {
        if (numBanks != offsets.length) {
            throw new ArrayIndexOutOfBoundsException("银行数量" +
                 " 不匹配银行偏移量的数量");
        }
        this.theTrackable = StateTrackableDelegate.createInstance(initialState);
        this.dataType = dataType;
        this.banks = numBanks;
        this.size = size;
        this.offset = offsets[0];
        this.offsets = (int[])offsets.clone();
    }

    /**  返回此 DataBuffer 的数据类型。
     *   @return 此 <code>DataBuffer</code> 的数据类型。
     */
    public int getDataType() {
        return dataType;
    }

    /**  返回所有银行的大小（以数组元素为单位）。
     *   @return 所有银行的大小。
     */
    public int getSize() {
        return size;
    }

    /** 返回默认银行的偏移量（以数组元素为单位）。
     *  @return 默认银行的偏移量。
     */
    public int getOffset() {
        return offset;
    }

    /** 返回所有银行的偏移量（以数组元素为单位）。
     *  @return 所有银行的偏移量。
     */
    public int[] getOffsets() {
        return (int[])offsets.clone();
    }

    /** 返回此 DataBuffer 中的银行数量。
     *  @return 银行数量。
     */
    public int getNumBanks() {
        return banks;
    }

    /**
     * 从第一个（默认）银行返回请求的数据数组元素作为整数。
     * @param i 请求的数据数组元素的索引
     * @return 指定索引处的数据数组元素。
     * @see #setElem(int, int)
     * @see #setElem(int, int, int)
     */
    public int getElem(int i) {
        return getElem(0,i);
    }

    /**
     * 从指定的银行返回请求的数据数组元素作为整数。
     * @param bank 指定的银行
     * @param i 请求的数据数组元素的索引
     * @return 从指定银行的指定索引处返回的数据数组元素。
     * @see #setElem(int, int)
     * @see #setElem(int, int, int)
     */
    public abstract int getElem(int bank, int i);

    /**
     * 在第一个（默认）银行中设置请求的数据数组元素为给定的整数。
     * @param i 指定的数据数组索引
     * @param val 要设置的数据数组中指定索引处的元素
     * @see #getElem(int)
     * @see #getElem(int, int)
     */
    public void  setElem(int i, int val) {
        setElem(0,i,val);
    }

    /**
     * 在指定的银行中设置请求的数据数组元素为给定的整数。
     * @param bank 指定的银行
     * @param i 指定的数据数组索引
     * @param val 要设置的数据数组中指定银行的指定索引处的元素
     * @see #getElem(int)
     * @see #getElem(int, int)
     */
    public abstract void setElem(int bank, int i, int val);

    /**
     * 从第一个（默认）银行返回请求的数据数组元素作为浮点数。此类中的实现是将 getElem(i) 转换为浮点数。如果需要其他实现，子类可以覆盖此方法。
     * @param i 请求的数据数组元素的索引
     * @return 代表指定索引处的数据数组元素的浮点值。
     * @see #setElemFloat(int, float)
     * @see #setElemFloat(int, int, float)
     */
    public float getElemFloat(int i) {
        return (float)getElem(i);
    }

    /**
     * 从指定的银行返回请求的数据数组元素作为浮点数。此类中的实现是将 {@link #getElem(int, int)}
     * 转换为浮点数。如果需要其他实现，子类可以覆盖此方法。
     * @param bank 指定的银行
     * @param i 请求的数据数组元素的索引
     * @return 从指定银行的指定索引处返回的浮点值。
     * @see #setElemFloat(int, float)
     * @see #setElemFloat(int, int, float)
     */
    public float getElemFloat(int bank, int i) {
        return (float)getElem(bank,i);
    }


                /**
     * 在第一个（默认）银行中设置请求的数据数组元素
     * 从给定的浮点数。此类中的实现是将
     * val 转换为 int 并调用 {@link #setElem(int, int)}。 如果需要其他实现，
     * 子类可以重写此方法。
     * @param i 指定的索引
     * @param val 要设置的元素值，位于指定索引处
     * 的数据数组中
     * @see #getElemFloat(int)
     * @see #getElemFloat(int, int)
     */
    public void setElemFloat(int i, float val) {
        setElem(i,(int)val);
    }

    /**
     * 在指定的银行中设置请求的数据数组元素
     * 从给定的浮点数。此类中的实现是将
     * val 转换为 int 并调用 {@link #setElem(int, int)}。 如果需要其他实现，
     * 子类可以重写此方法。
     * @param bank 指定的银行
     * @param i 指定的索引
     * @param val 要设置的元素值，位于指定银行
     * 的指定索引处的数据数组中
     * @see #getElemFloat(int)
     * @see #getElemFloat(int, int)
     */
    public void setElemFloat(int bank, int i, float val) {
        setElem(bank,i,(int)val);
    }

    /**
     * 从第一个（默认）银行返回请求的数据数组元素
     * 作为双精度浮点数。此类中的实现是将
     * {@link #getElem(int)}
     * 转换为双精度浮点数。 如果需要其他实现，
     * 子类可以重写此方法。
     * @param i 指定的索引
     * @return 一个双精度浮点数值，表示数据数组中
     * 指定索引处的元素。
     * @see #setElemDouble(int, double)
     * @see #setElemDouble(int, int, double)
     */
    public double getElemDouble(int i) {
        return (double)getElem(i);
    }

    /**
     * 从指定的银行返回请求的数据数组元素
     * 作为双精度浮点数。此类中的实现是将 getElem(bank, i)
     * 转换为双精度浮点数。 如果需要其他实现，
     * 子类可以重写此方法。
     * @param bank 指定的银行
     * @param i 指定的索引
     * @return 一个双精度浮点数值，表示数据数组中
     * 指定银行的指定索引处的元素。
     * @see #setElemDouble(int, double)
     * @see #setElemDouble(int, int, double)
     */
    public double getElemDouble(int bank, int i) {
        return (double)getElem(bank,i);
    }

    /**
     * 在第一个（默认）银行中设置请求的数据数组元素
     * 从给定的双精度浮点数。此类中的实现是将
     * val 转换为 int 并调用 {@link #setElem(int, int)}。 如果需要其他实现，
     * 子类可以重写此方法。
     * @param i 指定的索引
     * @param val 要设置的元素值，位于指定索引处
     * 的数据数组中
     * @see #getElemDouble(int)
     * @see #getElemDouble(int, int)
     */
    public void setElemDouble(int i, double val) {
        setElem(i,(int)val);
    }

    /**
     * 在指定的银行中设置请求的数据数组元素
     * 从给定的双精度浮点数。此类中的实现是将
     * val 转换为 int 并调用 {@link #setElem(int, int)}。 如果需要其他实现，
     * 子类可以重写此方法。
     * @param bank 指定的银行
     * @param i 指定的索引
     * @param val 要设置的元素值，位于指定银行
     * 的指定索引处的数据数组中
     * @see #getElemDouble(int)
     * @see #getElemDouble(int, int)
     */
    public void setElemDouble(int bank, int i, double val) {
        setElem(bank,i,(int)val);
    }

    static int[] toIntArray(Object obj) {
        if (obj instanceof int[]) {
            return (int[])obj;
        } else if (obj == null) {
            return null;
        } else if (obj instanceof short[]) {
            short sdata[] = (short[])obj;
            int idata[] = new int[sdata.length];
            for (int i = 0; i < sdata.length; i++) {
                idata[i] = (int)sdata[i] & 0xffff;
            }
            return idata;
        } else if (obj instanceof byte[]) {
            byte bdata[] = (byte[])obj;
            int idata[] = new int[bdata.length];
            for (int i = 0; i < bdata.length; i++) {
                idata[i] = 0xff & (int)bdata[i];
            }
            return idata;
        }
        return null;
    }

    static {
        SunWritableRaster.setDataStealer(new SunWritableRaster.DataStealer() {
            public byte[] getData(DataBufferByte dbb, int bank) {
                return dbb.bankdata[bank];
            }

            public short[] getData(DataBufferUShort dbus, int bank) {
                return dbus.bankdata[bank];
            }

            public int[] getData(DataBufferInt dbi, int bank) {
                return dbi.bankdata[bank];
            }

            public StateTrackableDelegate getTrackable(DataBuffer db) {
                return db.theTrackable;
            }

            public void setTrackable(DataBuffer db,
                                     StateTrackableDelegate trackable)
            {
                db.theTrackable = trackable;
            }
        });
    }
}
