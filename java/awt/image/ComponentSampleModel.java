
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

import java.util.Arrays;

/**
 *  此类表示存储在 DataBuffer 的每个数据元素中的像素样本。它将组成像素的 N 个样本存储在 N 个单独的数据数组元素中。
 *  不同的波段可以存储在 DataBuffer 的不同银行中。提供了访问器方法，以便可以直接操作图像数据。此类可以支持不同的交织方式，
 *  例如波段交织、扫描行交织和像素交织。像素步长是同一扫描行上同一波段的两个样本之间的数据数组元素数。扫描行步长是给定样本
 *  与同一列下一行的相应样本之间的数据数组元素数。波段偏移表示从存储每个波段的 DataBuffer 银行的第一个数据数组元素到该波段
 *  第一个样本的数据数组元素数。波段编号从 0 到 N-1。此类可以表示每个样本是可以在 8 位、16 位或 32 位中存储的无符号整数
 *  （分别使用 <code>DataBuffer.TYPE_BYTE</code>、<code>DataBuffer.TYPE_USHORT</code> 或 <code>DataBuffer.TYPE_INT</code>），
 *  每个样本是可以在 16 位中存储的有符号整数（使用 <code>DataBuffer.TYPE_SHORT</code>），或每个样本是浮点或双精度数量
 *  （分别使用 <code>DataBuffer.TYPE_FLOAT</code> 或 <code>DataBuffer.TYPE_DOUBLE</code>）。给定 ComponentSampleModel
 *  的所有样本都以相同的精度存储。所有步长和偏移必须是非负数。此类支持
 *  {@link DataBuffer#TYPE_BYTE TYPE_BYTE}，
 *  {@link DataBuffer#TYPE_USHORT TYPE_USHORT}，
 *  {@link DataBuffer#TYPE_SHORT TYPE_SHORT}，
 *  {@link DataBuffer#TYPE_INT TYPE_INT}，
 *  {@link DataBuffer#TYPE_FLOAT TYPE_FLOAT}，
 *  {@link DataBuffer#TYPE_DOUBLE TYPE_DOUBLE}，
 *  @see java.awt.image.PixelInterleavedSampleModel
 *  @see java.awt.image.BandedSampleModel
 */

public class ComponentSampleModel extends SampleModel
{
    /** 所有波段在数据数组元素中的偏移。 */
    protected int bandOffsets[];

    /** 存储图像数据的每个波段的银行索引。 */
    protected int[] bankIndices;

    /**
     *  此 <code>ComponentSampleModel</code> 中的波段数。
     */
    protected int numBands = 1;

    /**
     *  此 <code>ComponentSampleModel</code> 中的银行数。
     */
    protected int numBanks = 1;

    /**
     *  由此 ComponentSampleModel 描述的图像数据区域的行步长（以数据数组元素为单位）。
     */
    protected int scanlineStride;

    /** 由 ComponentSampleModel 描述的图像数据区域的像素步长（以数据数组元素为单位）。 */
    protected int pixelStride;

    static private native void initIDs();
    static {
        ColorModel.loadLibraries();
        initIDs();
    }

    /**
     * 使用指定参数构造 ComponentSampleModel。波段数将由 bandOffsets 数组的长度给出。所有波段将存储在 DataBuffer 的第一个银行中。
     * @param dataType 用于存储样本的数据类型
     * @param w 此 ComponentSampleModel 描述的图像数据区域的宽度（以像素为单位）
     * @param h 此 ComponentSampleModel 描述的图像数据区域的高度（以像素为单位）
     * @param pixelStride 此 ComponentSampleModel 描述的图像数据区域的像素步长
     * @param scanlineStride 此 ComponentSampleModel 描述的图像数据区域的行步长
     * @param bandOffsets 所有波段的偏移
     * @throws IllegalArgumentException 如果 <code>w</code> 或 <code>h</code> 不大于 0
     * @throws IllegalArgumentException 如果 <code>pixelStride</code> 小于 0
     * @throws IllegalArgumentException 如果 <code>scanlineStride</code> 小于 0
     * @throws IllegalArgumentException 如果 <code>numBands</code> 小于 1
     * @throws IllegalArgumentException 如果 <code>w</code> 和 <code>h</code> 的乘积大于 <code>Integer.MAX_VALUE</code>
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是支持的数据类型之一
     */
    public ComponentSampleModel(int dataType,
                                int w, int h,
                                int pixelStride,
                                int scanlineStride,
                                int bandOffsets[]) {
        super(dataType, w, h, bandOffsets.length);
        this.dataType = dataType;
        this.pixelStride = pixelStride;
        this.scanlineStride  = scanlineStride;
        this.bandOffsets = (int[])bandOffsets.clone();
        numBands = this.bandOffsets.length;
        if (pixelStride < 0) {
            throw new IllegalArgumentException("Pixel stride must be >= 0");
        }
        // TODO - bug 4296691 - remove this check
        if (scanlineStride < 0) {
            throw new IllegalArgumentException("Scanline stride must be >= 0");
        }
        if (numBands < 1) {
            throw new IllegalArgumentException("Must have at least one band.");
        }
        if ((dataType < DataBuffer.TYPE_BYTE) ||
            (dataType > DataBuffer.TYPE_DOUBLE)) {
            throw new IllegalArgumentException("Unsupported dataType.");
        }
        bankIndices = new int[numBands];
        for (int i=0; i<numBands; i++) {
            bankIndices[i] = 0;
        }
        verify();
    }


    /**
     * 使用指定参数构造 ComponentSampleModel。波段数将由 bandOffsets 数组的长度给出。不同的波段可以存储在 DataBuffer 的不同银行中。
     *
     * @param dataType 用于存储样本的数据类型
     * @param w 此 ComponentSampleModel 描述的图像数据区域的宽度（以像素为单位）
     * @param h 此 ComponentSampleModel 描述的图像数据区域的高度（以像素为单位）
     * @param pixelStride 此 ComponentSampleModel 描述的图像数据区域的像素步长
     * @param scanlineStride 此 ComponentSampleModel 描述的图像数据区域的行步长
     * @param bankIndices 所有波段的银行索引
     * @param bandOffsets 所有波段的偏移
     * @throws IllegalArgumentException 如果 <code>w</code> 或 <code>h</code> 不大于 0
     * @throws IllegalArgumentException 如果 <code>pixelStride</code> 小于 0
     * @throws IllegalArgumentException 如果 <code>scanlineStride</code> 小于 0
     * @throws IllegalArgumentException 如果 <code>bankIndices</code> 的长度不等于 <code>bankOffsets</code> 的长度
     * @throws IllegalArgumentException 如果 <code>bandIndices</code> 的任何银行索引小于 0
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是支持的数据类型之一
     */
    public ComponentSampleModel(int dataType,
                                int w, int h,
                                int pixelStride,
                                int scanlineStride,
                                int bankIndices[],
                                int bandOffsets[]) {
        super(dataType, w, h, bandOffsets.length);
        this.dataType = dataType;
        this.pixelStride = pixelStride;
        this.scanlineStride  = scanlineStride;
        this.bandOffsets = (int[])bandOffsets.clone();
        this.bankIndices = (int[]) bankIndices.clone();
        if (pixelStride < 0) {
            throw new IllegalArgumentException("Pixel stride must be >= 0");
        }
        // TODO - bug 4296691 - remove this check
        if (scanlineStride < 0) {
            throw new IllegalArgumentException("Scanline stride must be >= 0");
        }
        if ((dataType < DataBuffer.TYPE_BYTE) ||
            (dataType > DataBuffer.TYPE_DOUBLE)) {
            throw new IllegalArgumentException("Unsupported dataType.");
        }
        int maxBank = this.bankIndices[0];
        if (maxBank < 0) {
            throw new IllegalArgumentException("Index of bank 0 is less than "+
                                               "0 ("+maxBank+")");
        }
        for (int i=1; i < this.bankIndices.length; i++) {
            if (this.bankIndices[i] > maxBank) {
                maxBank = this.bankIndices[i];
            }
            else if (this.bankIndices[i] < 0) {
                throw new IllegalArgumentException("Index of bank "+i+
                                                   " is less than 0 ("+
                                                   maxBank+")");
            }
        }
        numBanks         = maxBank+1;
        numBands         = this.bandOffsets.length;
        if (this.bandOffsets.length != this.bankIndices.length) {
            throw new IllegalArgumentException("Length of bandOffsets must "+
                                               "equal length of bankIndices.");
        }
        verify();
    }

    private void verify() {
        int requiredSize = getBufferSize();
    }

    /**
     * 返回与此 ComponentSampleModel 匹配的数据缓冲区所需的数据元素数。
     */
     private int getBufferSize() {
         int maxBandOff=bandOffsets[0];
         for (int i=1; i<bandOffsets.length; i++) {
             maxBandOff = Math.max(maxBandOff,bandOffsets[i]);
         }

         if (maxBandOff < 0 || maxBandOff > (Integer.MAX_VALUE - 1)) {
             throw new IllegalArgumentException("Invalid band offset");
         }

         if (pixelStride < 0 || pixelStride > (Integer.MAX_VALUE / width)) {
             throw new IllegalArgumentException("Invalid pixel stride");
         }

         if (scanlineStride < 0 || scanlineStride > (Integer.MAX_VALUE / height)) {
             throw new IllegalArgumentException("Invalid scanline stride");
         }

         int size = maxBandOff + 1;

         int val = pixelStride * (width - 1);

         if (val > (Integer.MAX_VALUE - size)) {
             throw new IllegalArgumentException("Invalid pixel stride");
         }

         size += val;

         val = scanlineStride * (height - 1);

         if (val > (Integer.MAX_VALUE - size)) {
             throw new IllegalArgumentException("Invalid scan stride");
         }

         size += val;

         return size;
     }

     /**
      * 保留波段顺序并使用新的步长因子...
      */
    int []orderBands(int orig[], int step) {
        int map[] = new int[orig.length];
        int ret[] = new int[orig.length];

        for (int i=0; i<map.length; i++) map[i] = i;

        for (int i = 0; i < ret.length; i++) {
            int index = i;
            for (int j = i+1; j < ret.length; j++) {
                if (orig[map[index]] > orig[map[j]]) {
                    index = j;
                }
            }
            ret[map[index]] = i*step;
            map[index]  = map[i];
        }
        return ret;
    }

    /**
     * 创建具有指定宽度和高度的新 <code>ComponentSampleModel</code>。新的 <code>SampleModel</code> 将具有与该 <code>SampleModel</code> 相同的波段数、存储数据类型、交织方案和像素步长。
     * @param w 结果 <code>SampleModel</code> 的宽度
     * @param h 结果 <code>SampleModel</code> 的高度
     * @return 具有指定大小的新 <code>ComponentSampleModel</code>
     * @throws IllegalArgumentException 如果 <code>w</code> 或 <code>h</code> 不大于 0
     */
    public SampleModel createCompatibleSampleModel(int w, int h) {
        SampleModel ret=null;
        long size;
        int minBandOff=bandOffsets[0];
        int maxBandOff=bandOffsets[0];
        for (int i=1; i<bandOffsets.length; i++) {
            minBandOff = Math.min(minBandOff,bandOffsets[i]);
            maxBandOff = Math.max(maxBandOff,bandOffsets[i]);
        }
        maxBandOff -= minBandOff;

        int bands   = bandOffsets.length;
        int bandOff[];
        int pStride = Math.abs(pixelStride);
        int lStride = Math.abs(scanlineStride);
        int bStride = Math.abs(maxBandOff);

        if (pStride > lStride) {
            if (pStride > bStride) {
                if (lStride > bStride) { // pix > line > band
                    bandOff = new int[bandOffsets.length];
                    for (int i=0; i<bands; i++)
                        bandOff[i] = bandOffsets[i]-minBandOff;
                    lStride = bStride+1;
                    pStride = lStride*h;
                } else { // pix > band > line
                    bandOff = orderBands(bandOffsets,lStride*h);
                    pStride = bands*lStride*h;
                }
            } else { // band > pix > line
                pStride = lStride*h;
                bandOff = orderBands(bandOffsets,pStride*w);
            }
        } else {
            if (pStride > bStride) { // line > pix > band
                bandOff = new int[bandOffsets.length];
                for (int i=0; i<bands; i++)
                    bandOff[i] = bandOffsets[i]-minBandOff;
                pStride = bStride+1;
                lStride = pStride*w;
            } else {
                if (lStride > bStride) { // line > band > pix
                    bandOff = orderBands(bandOffsets,pStride*w);
                    lStride = bands*pStride*w;
                } else { // band > line > pix
                    lStride = pStride*w;
                    bandOff = orderBands(bandOffsets,lStride*h);
                }
            }
        }


                    // 确保为负偏移量留出空间...
        int base = 0;
        if (scanlineStride < 0) {
            base += lStride * h;
            lStride *= -1;
        }
        if (pixelStride < 0) {
            base += pStride * w;
            pStride *= -1;
        }

        for (int i = 0; i < bands; i++)
            bandOff[i] += base;
        return new ComponentSampleModel(dataType, w, h, pStride,
                                        lStride, bankIndices, bandOff);
    }

    /**
     * 创建一个新的 ComponentSampleModel，其中包含此 ComponentSampleModel 的子集带。
     * 新的 ComponentSampleModel 可以与现有的 ComponentSampleModel 可以使用的任何 DataBuffer 一起使用。
     * 新的 ComponentSampleModel/DataBuffer 组合将表示一个图像，该图像具有原始 ComponentSampleModel/DataBuffer 组合的子集带。
     * @param bands 此 ComponentSampleModel 的子集带
     * @return 一个使用此 ComponentSampleModel 的子集带创建的 ComponentSampleModel。
     */
    public SampleModel createSubsetSampleModel(int bands[]) {
        if (bands.length > bankIndices.length)
            throw new RasterFormatException("只有 " +
                                            bankIndices.length +
                                            " 个带");
        int newBankIndices[] = new int[bands.length];
        int newBandOffsets[] = new int[bands.length];

        for (int i = 0; i < bands.length; i++) {
            newBankIndices[i] = bankIndices[bands[i]];
            newBandOffsets[i] = bandOffsets[bands[i]];
        }

        return new ComponentSampleModel(this.dataType, width, height,
                                        this.pixelStride,
                                        this.scanlineStride,
                                        newBankIndices, newBandOffsets);
    }

    /**
     * 创建一个与此 ComponentSampleModel 对应的 DataBuffer。
     * DataBuffer 的数据类型、银行数量和大小与此 ComponentSampleModel 一致。
     * @return 一个 DataBuffer，其数据类型、银行数量和大小与此 ComponentSampleModel 一致。
     */
    public DataBuffer createDataBuffer() {
        DataBuffer dataBuffer = null;

        int size = getBufferSize();
        switch (dataType) {
        case DataBuffer.TYPE_BYTE:
            dataBuffer = new DataBufferByte(size, numBanks);
            break;
        case DataBuffer.TYPE_USHORT:
            dataBuffer = new DataBufferUShort(size, numBanks);
            break;
        case DataBuffer.TYPE_SHORT:
            dataBuffer = new DataBufferShort(size, numBanks);
            break;
        case DataBuffer.TYPE_INT:
            dataBuffer = new DataBufferInt(size, numBanks);
            break;
        case DataBuffer.TYPE_FLOAT:
            dataBuffer = new DataBufferFloat(size, numBanks);
            break;
        case DataBuffer.TYPE_DOUBLE:
            dataBuffer = new DataBufferDouble(size, numBanks);
            break;
        }

        return dataBuffer;
    }


    /** 获取像素 (x,y) 的第一个带的偏移量。
     *  可以从 DataBuffer <code>data</code> 中使用 ComponentSampleModel <code>csm</code> 获取第一个带的样本，如下所示：
     * <pre>
     *        data.getElem(csm.getOffset(x, y));
     * </pre>
     * @param x 指定像素的 X 坐标
     * @param y 指定像素的 Y 坐标
     * @return 指定像素的第一个带的偏移量。
     */
    public int getOffset(int x, int y) {
        int offset = y * scanlineStride + x * pixelStride + bandOffsets[0];
        return offset;
    }

    /** 获取像素 (x,y) 的带 b 的偏移量。
     *  可以从 DataBuffer <code>data</code> 中使用 ComponentSampleModel <code>csm</code> 获取带 <code>b</code> 的样本，如下所示：
     * <pre>
     *       data.getElem(csm.getOffset(x, y, b));
     * </pre>
     * @param x 指定像素的 X 坐标
     * @param y 指定像素的 Y 坐标
     * @param b 指定的带
     * @return 指定像素的指定带的偏移量。
     */
    public int getOffset(int x, int y, int b) {
        int offset = y * scanlineStride + x * pixelStride + bandOffsets[b];
        return offset;
    }

    /** 返回所有带的每个样本的位数。
     *  @return 一个数组，包含所有带的每个样本的位数，数组中的每个元素表示一个带。
     */
    public final int[] getSampleSize() {
        int sampleSize[] = new int[numBands];
        int sizeInBits = getSampleSize(0);

        for (int i = 0; i < numBands; i++)
            sampleSize[i] = sizeInBits;

        return sampleSize;
    }

    /** 返回指定带的每个样本的位数。
     *  @param band 指定的带
     *  @return 指定带的每个样本的位数。
     */
    public final int getSampleSize(int band) {
        return DataBuffer.getDataTypeSize(dataType);
    }

    /** 返回所有带的银行索引。
     *  @return 所有带的银行索引。
     */
    public final int[] getBankIndices() {
        return (int[]) bankIndices.clone();
    }

    /** 返回所有带的带偏移量。
     *  @return 所有带的带偏移量。
     */
    public final int[] getBandOffsets() {
        return (int[]) bandOffsets.clone();
    }

    /** 返回此 ComponentSampleModel 的扫描线步长。
     *  @return 此 ComponentSampleModel 的扫描线步长。
     */
    public final int getScanlineStride() {
        return scanlineStride;
    }

    /** 返回此 ComponentSampleModel 的像素步长。
     *  @return 此 ComponentSampleModel 的像素步长。
     */
    public final int getPixelStride() {
        return pixelStride;
    }

    /**
     * 返回使用
     * {@link #getDataElements(int, int, Object, DataBuffer) } 和
     * {@link #setDataElements(int, int, Object, DataBuffer) }
     * 方法传输一个像素所需的数据元素数量。
     * 对于 ComponentSampleModel，这与带的数量相同。
     * @return 使用 <code>getDataElements</code> 和
     *         <code>setDataElements</code> 方法传输一个像素所需的数据元素数量。
     * @see java.awt.image.SampleModel#getNumDataElements
     * @see #getNumBands
     */
    public final int getNumDataElements() {
        return getNumBands();
    }

    /**
     * 以 <code>TransferType</code> 类型的原始数组形式返回单个像素的数据。
     * 对于 ComponentSampleModel，这与数据类型相同，样本以每个数组元素一个的形式返回。
     * 通常，<code>obj</code> 应该传入为 <code>null</code>，以便自动创建合适的原始数据类型。
     * <p>
     * 以下代码说明了如何将数据从 <code>DataBuffer</code> <code>db1</code>（其存储布局由 <code>ComponentSampleModel</code> <code>csm1</code> 描述）传输到 <code>DataBuffer</code> <code>db2</code>（其存储布局由 <code>ComponentSampleModel</code> <code>csm2</code> 描述）。
     * 通常，这种传输比使用 <code>getPixel</code> 和 <code>setPixel</code> 更高效。
     * <pre>
     *       ComponentSampleModel csm1, csm2;
     *       DataBufferInt db1, db2;
     *       csm2.setDataElements(x, y,
     *                            csm1.getDataElements(x, y, null, db1), db2);
     * </pre>
     *
     * 使用 <code>getDataElements</code> 和 <code>setDataElements</code> 在两个 <code>DataBuffer/SampleModel</code> 对之间传输是合法的，如果 <code>SampleModel</code> 对象具有相同数量的带，对应带具有相同数量的样本位数，且 <code>TransferType</code> 相同。
     * <p>
     * 如果 <code>obj</code> 不为 <code>null</code>，则它应该是一个 <code>TransferType</code> 类型的原始数组。
     * 否则，将抛出 <code>ClassCastException</code>。如果坐标超出范围，或者 <code>obj</code> 不为 <code>null</code> 且不足以容纳像素数据，可能会抛出 <code>ArrayIndexOutOfBoundsException</code>。
     *
     * @param x         指定像素的 X 坐标
     * @param y         指定像素的 Y 坐标
     * @param obj       如果非 <code>null</code>，则在此原始数组中返回像素数据
     * @param data      包含图像数据的 <code>DataBuffer</code>
     * @return 指定像素的数据
     * @see #setDataElements(int, int, Object, DataBuffer)
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标超出范围，或者 obj 太小无法容纳输出。
     */
    public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("坐标超出范围！");
        }

        int type = getTransferType();
        int numDataElems = getNumDataElements();
        int pixelOffset = y * scanlineStride + x * pixelStride;

        switch (type) {

        case DataBuffer.TYPE_BYTE:

            byte[] bdata;

            if (obj == null)
                bdata = new byte[numDataElems];
            else
                bdata = (byte[]) obj;

            for (int i = 0; i < numDataElems; i++) {
                bdata[i] = (byte) data.getElem(bankIndices[i],
                                               pixelOffset + bandOffsets[i]);
            }

            obj = (Object) bdata;
            break;

        case DataBuffer.TYPE_USHORT:
        case DataBuffer.TYPE_SHORT:

            short[] sdata;

            if (obj == null)
                sdata = new short[numDataElems];
            else
                sdata = (short[]) obj;

            for (int i = 0; i < numDataElems; i++) {
                sdata[i] = (short) data.getElem(bankIndices[i],
                                                pixelOffset + bandOffsets[i]);
            }

            obj = (Object) sdata;
            break;

        case DataBuffer.TYPE_INT:

            int[] idata;

            if (obj == null)
                idata = new int[numDataElems];
            else
                idata = (int[]) obj;

            for (int i = 0; i < numDataElems; i++) {
                idata[i] = data.getElem(bankIndices[i],
                                        pixelOffset + bandOffsets[i]);
            }

            obj = (Object) idata;
            break;

        case DataBuffer.TYPE_FLOAT:

            float[] fdata;

            if (obj == null)
                fdata = new float[numDataElems];
            else
                fdata = (float[]) obj;

            for (int i = 0; i < numDataElems; i++) {
                fdata[i] = data.getElemFloat(bankIndices[i],
                                             pixelOffset + bandOffsets[i]);
            }

            obj = (Object) fdata;
            break;

        case DataBuffer.TYPE_DOUBLE:

            double[] ddata;

            if (obj == null)
                ddata = new double[numDataElems];
            else
                ddata = (double[]) obj;

            for (int i = 0; i < numDataElems; i++) {
                ddata[i] = data.getElemDouble(bankIndices[i],
                                              pixelOffset + bandOffsets[i]);
            }

            obj = (Object) ddata;
            break;
        }

        return obj;
    }

    /**
     * 以 int 数组形式返回指定像素的所有样本，每个样本对应数组中的一个元素。
     * 如果坐标超出范围，可能会抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x         指定像素的 X 坐标
     * @param y         指定像素的 Y 坐标
     * @param iArray    如果非 null，则在此数组中返回样本
     * @param data      包含图像数据的 DataBuffer
     * @return 指定像素的样本。
     * @see #setPixel(int, int, int[], DataBuffer)
     *
     * @throws NullPointerException 如果 data 为 null。
     * @throws ArrayIndexOutOfBoundsException 如果坐标超出范围，或者 iArray 太小无法容纳输出。
     */
    public int[] getPixel(int x, int y, int iArray[], DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("坐标超出范围！");
        }
        int pixels[];
        if (iArray != null) {
            pixels = iArray;
        } else {
            pixels = new int[numBands];
        }
        int pixelOffset = y * scanlineStride + x * pixelStride;
        for (int i = 0; i < numBands; i++) {
            pixels[i] = data.getElem(bankIndices[i],
                                     pixelOffset + bandOffsets[i]);
        }
        return pixels;
    }

    /**
     * 以 int 数组形式返回指定像素矩形的所有样本，每个样本对应数组中的一个元素。
     * 如果坐标超出范围，可能会抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x         指定像素矩形左上角的 X 坐标
     * @param y         指定像素矩形左上角的 Y 坐标
     * @param w         指定像素矩形的宽度
     * @param h         指定像素矩形的高度
     * @param iArray    如果非 null，则在此数组中返回样本
     * @param data      包含图像数据的 DataBuffer
     * @return 指定区域内的像素样本。
     * @see #setPixels(int, int, int, int, int[], DataBuffer)
     */
    public int[] getPixels(int x, int y, int w, int h,
                           int iArray[], DataBuffer data) {
        int x1 = x + w;
        int y1 = y + h;

        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || y > height || y1 < 0 || y1 > height)
        {
            throw new ArrayIndexOutOfBoundsException
                ("坐标超出范围！");
        }
        int pixels[];
        if (iArray != null) {
            pixels = iArray;
        } else {
            pixels = new int[w * h * numBands];
        }
        int lineOffset = y * scanlineStride + x * pixelStride;
        int srcOffset = 0;

        for (int i = 0; i < h; i++) {
            int pixelOffset = lineOffset;
            for (int j = 0; j < w; j++) {
                for (int k = 0; k < numBands; k++) {
                    pixels[srcOffset++] =
                        data.getElem(bankIndices[k], pixelOffset + bandOffsets[k]);
                }
                pixelOffset += pixelStride;
            }
            lineOffset += scanlineStride;
        }
        return pixels;
    }


                /**
     * 返回位于 (x,y) 的像素在指定波段的样本值，作为 int 类型。
     * 如果坐标超出范围，可能会抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x         像素位置的 X 坐标
     * @param y         像素位置的 Y 坐标
     * @param b         要返回的波段
     * @param data      包含图像数据的 <code>DataBuffer</code>
     * @return 指定像素在指定波段的样本值
     * @see #setSample(int, int, int, int, DataBuffer)
     */
    public int getSample(int x, int y, int b, DataBuffer data) {
        // 对 'b' 的边界检查将自动进行
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int sample = data.getElem(bankIndices[b],
                                  y*scanlineStride + x*pixelStride +
                                  bandOffsets[b]);
        return sample;
    }

    /**
     * 返回位于 (x,y) 的像素在指定波段的样本值，作为 float 类型。
     * 如果坐标超出范围，可能会抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x         像素位置的 X 坐标
     * @param y         像素位置的 Y 坐标
     * @param b         要返回的波段
     * @param data      包含图像数据的 <code>DataBuffer</code>
     * @return 指定像素在指定波段的样本值，作为 float 类型。
     */
    public float getSampleFloat(int x, int y, int b, DataBuffer data) {
        // 对 'b' 的边界检查将自动进行
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }

        float sample = data.getElemFloat(bankIndices[b],
                                         y*scanlineStride + x*pixelStride +
                                         bandOffsets[b]);
        return sample;
    }

    /**
     * 返回位于 (x,y) 的像素在指定波段的样本值，作为 double 类型。
     * 如果坐标超出范围，可能会抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x         像素位置的 X 坐标
     * @param y         像素位置的 Y 坐标
     * @param b         要返回的波段
     * @param data      包含图像数据的 <code>DataBuffer</code>
     * @return 指定像素在指定波段的样本值，作为 double 类型。
     */
    public double getSampleDouble(int x, int y, int b, DataBuffer data) {
        // 对 'b' 的边界检查将自动进行
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }

        double sample = data.getElemDouble(bankIndices[b],
                                           y*scanlineStride + x*pixelStride +
                                           bandOffsets[b]);
        return sample;
    }

    /**
     * 返回指定矩形区域内的像素在指定波段的样本值，作为 int 数组，每个数据数组元素一个样本。
     * 如果坐标超出范围，可能会抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x         左上角像素位置的 X 坐标
     * @param y         左上角像素位置的 Y 坐标
     * @param w         像素矩形的宽度
     * @param h         像素矩形的高度
     * @param b         要返回的波段
     * @param iArray    如果非 <code>null</code>，则将样本值返回到此数组
     * @param data      包含图像数据的 <code>DataBuffer</code>
     * @return 指定像素在指定波段的样本值
     * @see #setSamples(int, int, int, int, int, int[], DataBuffer)
     */
    public int[] getSamples(int x, int y, int w, int h, int b,
                            int iArray[], DataBuffer data) {
        // 对 'b' 的边界检查将自动进行
        if ((x < 0) || (y < 0) || (x + w > width) || (y + h > height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int samples[];
        if (iArray != null) {
           samples = iArray;
        } else {
           samples = new int [w*h];
        }
        int lineOffset = y*scanlineStride + x*pixelStride +  bandOffsets[b];
        int srcOffset = 0;

        for (int i = 0; i < h; i++) {
           int sampleOffset = lineOffset;
           for (int j = 0; j < w; j++) {
              samples[srcOffset++] = data.getElem(bankIndices[b],
                                                  sampleOffset);
              sampleOffset += pixelStride;
           }
           lineOffset += scanlineStride;
        }
        return samples;
    }

    /**
     * 从一个原始数组中设置 <code>DataBuffer</code> 中单个像素的数据，该数组的类型为 <code>TransferType</code>。
     * 对于 <code>ComponentSampleModel</code>，这与数据类型相同，样本按数组元素逐一传输。
     * <p>
     * 下面的代码说明了如何将 <code>DataBuffer</code> <code>db1</code> 中的数据（其存储布局由 <code>ComponentSampleModel</code> <code>csm1</code> 描述）传输到 <code>DataBuffer</code> <code>db2</code>（其存储布局由 <code>ComponentSampleModel</code> <code>csm2</code> 描述）。通常比使用 <code>getPixel</code> 和 <code>setPixel</code> 更高效。
     * <pre>
     *       ComponentSampleModel csm1, csm2;
     *       DataBufferInt db1, db2;
     *       csm2.setDataElements(x, y, csm1.getDataElements(x, y, null, db1),
     *                            db2);
     * </pre>
     * 使用 <code>getDataElements</code> 和 <code>setDataElements</code> 在两个 <code>DataBuffer/SampleModel</code> 对之间进行传输是合法的，如果 <code>SampleModel</code> 对象具有相同数量的波段，对应波段具有相同数量的样本位数，并且 <code>TransferType</code> 相同。
     * <p>
     * 如果 <code>obj</code> 不是 <code>TransferType</code> 类型的原始数组，则会抛出 <code>ClassCastException</code>。
     * 如果坐标超出范围，或者 <code>obj</code> 不足以容纳像素数据，可能会抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x         像素位置的 X 坐标
     * @param y         像素位置的 Y 坐标
     * @param obj       包含像素数据的原始数组
     * @param data      包含图像数据的 <code>DataBuffer</code>
     * @see #getDataElements(int, int, Object, DataBuffer)
     */
    public void setDataElements(int x, int y, Object obj, DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }

        int type = getTransferType();
        int numDataElems = getNumDataElements();
        int pixelOffset = y*scanlineStride + x*pixelStride;

        switch(type) {

        case DataBuffer.TYPE_BYTE:

            byte[] barray = (byte[])obj;

            for (int i=0; i<numDataElems; i++) {
                data.setElem(bankIndices[i], pixelOffset + bandOffsets[i],
                           ((int)barray[i])&0xff);
            }
            break;

        case DataBuffer.TYPE_USHORT:
        case DataBuffer.TYPE_SHORT:

            short[] sarray = (short[])obj;

            for (int i=0; i<numDataElems; i++) {
                data.setElem(bankIndices[i], pixelOffset + bandOffsets[i],
                           ((int)sarray[i])&0xffff);
            }
            break;

        case DataBuffer.TYPE_INT:

            int[] iarray = (int[])obj;

            for (int i=0; i<numDataElems; i++) {
                data.setElem(bankIndices[i],
                             pixelOffset + bandOffsets[i], iarray[i]);
            }
            break;

        case DataBuffer.TYPE_FLOAT:

            float[] farray = (float[])obj;

            for (int i=0; i<numDataElems; i++) {
                data.setElemFloat(bankIndices[i],
                             pixelOffset + bandOffsets[i], farray[i]);
            }
            break;

        case DataBuffer.TYPE_DOUBLE:

            double[] darray = (double[])obj;

            for (int i=0; i<numDataElems; i++) {
                data.setElemDouble(bankIndices[i],
                             pixelOffset + bandOffsets[i], darray[i]);
            }
            break;

        }
    }

    /**
     * 使用 int 数组中的样本值设置 <code>DataBuffer</code> 中的像素。如果坐标超出范围，可能会抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x         像素位置的 X 坐标
     * @param y         像素位置的 Y 坐标
     * @param iArray    包含输入样本的 int 数组
     * @param data      包含图像数据的 <code>DataBuffer</code>
     * @see #getPixel(int, int, int[], DataBuffer)
     */
    public void setPixel(int x, int y, int iArray[], DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
       int pixelOffset = y*scanlineStride + x*pixelStride;
       for (int i=0; i<numBands; i++) {
           data.setElem(bankIndices[i],
                        pixelOffset + bandOffsets[i],iArray[i]);
       }
    }

    /**
     * 从包含每个数组元素一个样本的 int 数组中设置矩形区域内的所有样本。如果坐标超出范围，可能会抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x         左上角像素位置的 X 坐标
     * @param y         左上角像素位置的 Y 坐标
     * @param w         像素矩形的宽度
     * @param h         像素矩形的高度
     * @param iArray    包含输入样本的 int 数组
     * @param data      包含图像数据的 <code>DataBuffer</code>
     * @see #getPixels(int, int, int, int, int[], DataBuffer)
     */
    public void setPixels(int x, int y, int w, int h,
                          int iArray[], DataBuffer data) {
        int x1 = x + w;
        int y1 = y + h;

        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 >  height)
        {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }

        int lineOffset = y*scanlineStride + x*pixelStride;
        int srcOffset = 0;

        for (int i = 0; i < h; i++) {
           int pixelOffset = lineOffset;
           for (int j = 0; j < w; j++) {
              for (int k=0; k < numBands; k++) {
                 data.setElem(bankIndices[k], pixelOffset + bandOffsets[k],
                              iArray[srcOffset++]);
              }
              pixelOffset += pixelStride;
           }
           lineOffset += scanlineStride;
        }
    }

    /**
     * 使用 int 类型的输入设置位于 (x,y) 的像素在指定波段的样本值。如果坐标超出范围，可能会抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x         像素位置的 X 坐标
     * @param y         像素位置的 Y 坐标
     * @param b         要设置的波段
     * @param s         输入样本值，作为 int 类型
     * @param data      包含图像数据的 <code>DataBuffer</code>
     * @see #getSample(int, int, int, DataBuffer)
     */
    public void setSample(int x, int y, int b, int s,
                          DataBuffer data) {
        // 对 'b' 的边界检查将自动进行
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        data.setElem(bankIndices[b],
                     y*scanlineStride + x*pixelStride + bandOffsets[b], s);
    }

    /**
     * 使用 float 类型的输入设置位于 (x,y) 的像素在指定波段的样本值。如果坐标超出范围，可能会抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x         像素位置的 X 坐标
     * @param y         像素位置的 Y 坐标
     * @param b         要设置的波段
     * @param s         输入样本值，作为 float 类型
     * @param data      包含图像数据的 <code>DataBuffer</code>
     * @see #getSample(int, int, int, DataBuffer)
     */
    public void setSample(int x, int y, int b,
                          float s ,
                          DataBuffer data) {
        // 对 'b' 的边界检查将自动进行
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        data.setElemFloat(bankIndices[b],
                          y*scanlineStride + x*pixelStride + bandOffsets[b],
                          s);
    }

    /**
     * 使用 double 类型的输入设置位于 (x,y) 的像素在指定波段的样本值。如果坐标超出范围，可能会抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x         像素位置的 X 坐标
     * @param y         像素位置的 Y 坐标
     * @param b         要设置的波段
     * @param s         输入样本值，作为 double 类型
     * @param data      包含图像数据的 <code>DataBuffer</code>
     * @see #getSample(int, int, int, DataBuffer)
     */
    public void setSample(int x, int y, int b,
                          double s,
                          DataBuffer data) {
        // 对 'b' 的边界检查将自动进行
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        data.setElemDouble(bankIndices[b],
                          y*scanlineStride + x*pixelStride + bandOffsets[b],
                          s);
    }

    /**
     * 从包含每个数据数组元素一个样本的 int 数组中设置指定矩形区域内的像素在指定波段的样本值。如果坐标超出范围，可能会抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x         左上角像素位置的 X 坐标
     * @param y         左上角像素位置的 Y 坐标
     * @param w         像素矩形的宽度
     * @param h         像素矩形的高度
     * @param b         要设置的波段
     * @param iArray    包含输入样本的 int 数组
     * @param data      包含图像数据的 <code>DataBuffer</code>
     * @see #getSamples(int, int, int, int, int, int[], DataBuffer)
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           int iArray[], DataBuffer data) {
        // 对 'b' 的边界检查将自动进行
        if ((x < 0) || (y < 0) || (x + w > width) || (y + h > height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int lineOffset = y*scanlineStride + x*pixelStride + bandOffsets[b];
        int srcOffset = 0;


                    for (int i = 0; i < h; i++) {
           int sampleOffset = lineOffset;
           for (int j = 0; j < w; j++) {
              data.setElem(bankIndices[b], sampleOffset, iArray[srcOffset++]);
              sampleOffset += pixelStride;
           }
           lineOffset += scanlineStride;
        }
    }

    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof ComponentSampleModel)) {
            return false;
        }

        ComponentSampleModel that = (ComponentSampleModel)o;
        return this.width == that.width &&
            this.height == that.height &&
            this.numBands == that.numBands &&
            this.dataType == that.dataType &&
            Arrays.equals(this.bandOffsets, that.bandOffsets) &&
            Arrays.equals(this.bankIndices, that.bankIndices) &&
            this.numBands == that.numBands &&
            this.numBanks == that.numBanks &&
            this.scanlineStride == that.scanlineStride &&
            this.pixelStride == that.pixelStride;
    }

    // 如果实现了 equals()，我们也必须实现 hashCode
    public int hashCode() {
        int hash = 0;
        hash = width;
        hash <<= 8;
        hash ^= height;
        hash <<= 8;
        hash ^= numBands;
        hash <<= 8;
        hash ^= dataType;
        hash <<= 8;
        for (int i = 0; i < bandOffsets.length; i++) {
            hash ^= bandOffsets[i];
            hash <<= 8;
        }
        for (int i = 0; i < bankIndices.length; i++) {
            hash ^= bankIndices[i];
            hash <<= 8;
        }
        hash ^= numBands;
        hash <<= 8;
        hash ^= numBanks;
        hash <<= 8;
        hash ^= scanlineStride;
        hash <<= 8;
        hash ^= pixelStride;
        return hash;
    }
}
