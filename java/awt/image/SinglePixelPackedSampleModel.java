
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
 * 此类表示打包的像素数据，使得构成单个像素的 N 个样本存储在一个数据数组元素中，每个数据数组元素仅包含一个像素的样本。
 * 该类支持
 * {@link DataBuffer#TYPE_BYTE TYPE_BYTE},
 * {@link DataBuffer#TYPE_USHORT TYPE_USHORT},
 * {@link DataBuffer#TYPE_INT TYPE_INT} 数据类型。
 * 所有数据数组元素都位于 DataBuffer 的第一个银行中。提供了访问器方法以便直接操作图像数据。扫描线步长是给定样本与其在下一扫描线相同列的对应样本之间的数据数组元素数。位掩码是从像素中提取表示各波段样本所需的掩码。
 * 位偏移是表示像素各波段样本在数据数组元素中的位偏移。
 * <p>
 * 以下代码说明了如何从 DataBuffer <code>data</code> 中提取表示像素 <code>x,y</code> 的波段 <code>b</code> 的样本位：
 * <pre>{@code
 *      int sample = data.getElem(y * scanlineStride + x);
 *      sample = (sample & bitMasks[b]) >>> bitOffsets[b];
 * }</pre>
 */

public class SinglePixelPackedSampleModel extends SampleModel
{
    /** 图像数据所有波段的位掩码。 */
    private int bitMasks[];

    /** 图像数据所有波段的位偏移。 */
    private int bitOffsets[];

    /** 图像数据所有波段的位大小。 */
    private int bitSizes[];

    /** 最大位大小。 */
    private int maxBitSize;

    /** 由该 SinglePixelPackedSampleModel 描述的图像数据区域的扫描线步长。 */
    private int scanlineStride;

    private static native void initIDs();
    static {
        ColorModel.loadLibraries();
        initIDs();
    }

    /**
     * 构造一个具有 bitMasks.length 波段的 SinglePixelPackedSampleModel。
     * 每个样本存储在数据数组元素中，其位置对应于其位掩码。每个位掩码必须是连续的，并且掩码不能重叠。超过数据类型容量的位掩码将被截断。
     * @param dataType  用于存储样本的数据类型。
     * @param w         由该 SampleModel 描述的图像数据区域的宽度（以像素为单位）。
     * @param h         由该 SampleModel 描述的图像数据区域的高度（以像素为单位）。
     * @param bitMasks  所有波段的位掩码。
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是
     *         <code>DataBuffer.TYPE_BYTE</code>、
     *         <code>DataBuffer.TYPE_USHORT</code> 或
     *         <code>DataBuffer.TYPE_INT</code>
     */
    public SinglePixelPackedSampleModel(int dataType, int w, int h,
                                   int bitMasks[]) {
        this(dataType, w, h, w, bitMasks);
        if (dataType != DataBuffer.TYPE_BYTE &&
            dataType != DataBuffer.TYPE_USHORT &&
            dataType != DataBuffer.TYPE_INT) {
            throw new IllegalArgumentException("Unsupported data type "+
                                               dataType);
        }
    }

    /**
     * 构造一个具有 bitMasks.length 波段和扫描线步长等于 scanlineStride 数据数组元素的 SinglePixelPackedSampleModel。
     * 每个样本存储在数据数组元素中，其位置对应于其位掩码。每个位掩码必须是连续的，并且掩码不能重叠。超过数据类型容量的位掩码将被截断。
     * @param dataType  用于存储样本的数据类型。
     * @param w         由该 SampleModel 描述的图像数据区域的宽度（以像素为单位）。
     * @param h         由该 SampleModel 描述的图像数据区域的高度（以像素为单位）。
     * @param scanlineStride 图像数据的扫描线步长。
     * @param bitMasks  所有波段的位掩码。
     * @throws IllegalArgumentException 如果 <code>w</code> 或
     *         <code>h</code> 不大于 0
     * @throws IllegalArgumentException 如果 <code>bitMask</code> 中的任何掩码不是连续的
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是
     *         <code>DataBuffer.TYPE_BYTE</code>、
     *         <code>DataBuffer.TYPE_USHORT</code> 或
     *         <code>DataBuffer.TYPE_INT</code>
     */
    public SinglePixelPackedSampleModel(int dataType, int w, int h,
                                   int scanlineStride, int bitMasks[]) {
        super(dataType, w, h, bitMasks.length);
        if (dataType != DataBuffer.TYPE_BYTE &&
            dataType != DataBuffer.TYPE_USHORT &&
            dataType != DataBuffer.TYPE_INT) {
            throw new IllegalArgumentException("Unsupported data type "+
                                               dataType);
        }
        this.dataType = dataType;
        this.bitMasks = (int[]) bitMasks.clone();
        this.scanlineStride = scanlineStride;

        this.bitOffsets = new int[numBands];
        this.bitSizes = new int[numBands];

        int maxMask = (int)((1L << DataBuffer.getDataTypeSize(dataType)) - 1);

        this.maxBitSize = 0;
        for (int i=0; i<numBands; i++) {
            int bitOffset = 0, bitSize = 0, mask;
            this.bitMasks[i] &= maxMask;
            mask = this.bitMasks[i];
            if (mask != 0) {
                while ((mask & 1) == 0) {
                    mask = mask >>> 1;
                    bitOffset++;
                }
                while ((mask & 1) == 1) {
                    mask = mask >>> 1;
                    bitSize++;
                }
                if (mask != 0) {
                    throw new IllegalArgumentException("Mask "+bitMasks[i]+
                                                       " must be contiguous");
                }
            }
            bitOffsets[i] = bitOffset;
            bitSizes[i] = bitSize;
            if (bitSize > maxBitSize) {
                maxBitSize = bitSize;
            }
        }
    }

    /**
     * 返回通过 getDataElements 和 setDataElements 方法传输一个像素所需的数据元素数量。
     * 对于 SinglePixelPackedSampleModel，这是 1。
     */
    public int getNumDataElements() {
        return 1;
    }

    /**
     * 返回与该 SinglePixelPackedSampleModel 匹配的数据缓冲区所需的缓冲区大小（以数据数组元素为单位）。
     */
    private long getBufferSize() {
      long size = scanlineStride * (height-1) + width;
      return size;
    }

    /**
     * 创建一个具有指定宽度和高度的新 SinglePixelPackedSampleModel。新的 SinglePixelPackedSampleModel 将具有与该
     * SinglePixelPackedSampleModel 相同的存储数据类型和位掩码。
     * @param w 结果 <code>SampleModel</code> 的宽度
     * @param h 结果 <code>SampleModel</code> 的高度
     * @return 一个具有指定宽度和高度的 <code>SinglePixelPackedSampleModel</code>。
     * @throws IllegalArgumentException 如果 <code>w</code> 或
     *         <code>h</code> 不大于 0
     */
    public SampleModel createCompatibleSampleModel(int w, int h) {
      SampleModel sampleModel = new SinglePixelPackedSampleModel(dataType, w, h,
                                                              bitMasks);
      return sampleModel;
    }

    /**
     * 创建一个与该 SinglePixelPackedSampleModel 对应的数据缓冲区。数据缓冲区的数据类型和大小将与该 SinglePixelPackedSampleModel 一致。数据缓冲区将具有一个银行。
     */
    public DataBuffer createDataBuffer() {
        DataBuffer dataBuffer = null;

        int size = (int)getBufferSize();
        switch (dataType) {
        case DataBuffer.TYPE_BYTE:
            dataBuffer = new DataBufferByte(size);
            break;
        case DataBuffer.TYPE_USHORT:
            dataBuffer = new DataBufferUShort(size);
            break;
        case DataBuffer.TYPE_INT:
            dataBuffer = new DataBufferInt(size);
            break;
        }
        return dataBuffer;
    }

    /** 返回所有波段的每样本位数。 */
    public int[] getSampleSize() {
        return bitSizes.clone();
    }

    /** 返回指定波段的每样本位数。 */
    public int getSampleSize(int band) {
        return bitSizes[band];
    }

    /** 返回表示像素 (x,y) 的数据数组元素的偏移量（以数据数组元素为单位）。
     *  可以从 DataBuffer <code>data</code> 中使用 SinglePixelPackedSampleModel <code>sppsm</code> 检索包含像素 <code>x,y</code> 的数据元素：
     * <pre>
     *        data.getElem(sppsm.getOffset(x, y));
     * </pre>
     * @param x 指定像素的 X 坐标
     * @param y 指定像素的 Y 坐标
     * @return 指定像素的偏移量。
     */
    public int getOffset(int x, int y) {
        int offset = y * scanlineStride + x;
        return offset;
    }

    /** 返回表示像素所有波段的位偏移。
     *  @return 表示像素所有波段的位偏移。
     */
    public int [] getBitOffsets() {
      return (int[])bitOffsets.clone();
    }

    /** 返回所有波段的位掩码。
     *  @return 所有波段的位掩码。
     */
    public int [] getBitMasks() {
      return (int[])bitMasks.clone();
    }

    /** 返回该 SinglePixelPackedSampleModel 的扫描线步长。
     *  @return 该 <code>SinglePixelPackedSampleModel</code> 的扫描线步长。
     */
    public int getScanlineStride() {
      return scanlineStride;
    }

    /**
     * 创建一个具有该 SinglePixelPackedSampleModel 子集波段的新 SinglePixelPackedSampleModel。新的
     * SinglePixelPackedSampleModel 可以与现有的 SinglePixelPackedSampleModel 可以使用的任何 DataBuffer 一起使用。新的
     * SinglePixelPackedSampleModel/DataBuffer 组合将表示原始
     * SinglePixelPackedSampleModel/DataBuffer 组合的子集图像。
     * @exception RasterFormatException 如果 bands 参数的长度大于样本模型中的波段数。
     */
    public SampleModel createSubsetSampleModel(int bands[]) {
        if (bands.length > numBands)
            throw new RasterFormatException("There are only " +
                                            numBands +
                                            " bands");
        int newBitMasks[] = new int[bands.length];
        for (int i=0; i<bands.length; i++)
            newBitMasks[i] = bitMasks[bands[i]];

        return new SinglePixelPackedSampleModel(this.dataType, width, height,
                                           this.scanlineStride, newBitMasks);
    }

    /**
     * 返回一个像素的单个数据，以 TransferType 类型的原始数组形式。对于 SinglePixelPackedSampleModel，数组将
     * 有一个元素，类型与存储数据类型相同。通常，obj
     * 应该传递为 null，以便自动创建对象并且具有正确的原始数据类型。
     * <p>
     * 以下代码说明了如何将数据从存储布局由
     * SinglePixelPackedSampleModel <code>sppsm1</code> 描述的 DataBuffer <code>db1</code> 转移到
     * 存储布局由 SinglePixelPackedSampleModel <code>sppsm2</code> 描述的 DataBuffer <code>db2</code>。
     * 该传输通常比使用 getPixel/setPixel 更高效。
     * <pre>
     *       SinglePixelPackedSampleModel sppsm1, sppsm2;
     *       DataBufferInt db1, db2;
     *       sppsm2.setDataElements(x, y, sppsm1.getDataElements(x, y, null,
     *                              db1), db2);
     * </pre>
     * 使用 getDataElements/setDataElements 在两个 DataBuffer/SampleModel 对之间传输数据是合法的，如果样本模型具有相同数量的波段，对应波段具有相同的每样本位数，并且 TransferTypes 相同。
     * <p>
     * 如果 obj 非空，它应该是一个 TransferType 类型的原始数组。否则，将抛出 ClassCastException。如果坐标超出范围，或者 obj 非空且不足以容纳像素数据，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param obj       如果非空，用于返回像素数据的原始数组。
     * @param data      包含图像数据的 DataBuffer。
     * @return 指定像素的数据。
     * @see #setDataElements(int, int, Object, DataBuffer)
     */
    public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
        // 'b' 的边界检查将自动执行
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }


                    int type = getTransferType();

        switch(type) {

        case DataBuffer.TYPE_BYTE:

            byte[] bdata;

            if (obj == null)
                bdata = new byte[1];
            else
                bdata = (byte[])obj;

            bdata[0] = (byte)data.getElem(y * scanlineStride + x);

            obj = (Object)bdata;
            break;

        case DataBuffer.TYPE_USHORT:

            short[] sdata;

            if (obj == null)
                sdata = new short[1];
            else
                sdata = (short[])obj;

            sdata[0] = (short)data.getElem(y * scanlineStride + x);

            obj = (Object)sdata;
            break;

        case DataBuffer.TYPE_INT:

            int[] idata;

            if (obj == null)
                idata = new int[1];
            else
                idata = (int[])obj;

            idata[0] = data.getElem(y * scanlineStride + x);

            obj = (Object)idata;
            break;
        }

        return obj;
    }

    /**
     * 返回指定像素的所有样本，以 int 数组形式。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param iArray    如果非空，则将样本返回到此数组。
     * @param data      包含图像数据的 DataBuffer。
     * @return 指定像素的所有样本。
     * @see #setPixel(int, int, int[], DataBuffer)
     */
    public int [] getPixel(int x, int y, int iArray[], DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int pixels[];
        if (iArray == null) {
            pixels = new int [numBands];
        } else {
            pixels = iArray;
        }

        int value = data.getElem(y * scanlineStride + x);
        for (int i=0; i<numBands; i++) {
            pixels[i] = (value & bitMasks[i]) >>> bitOffsets[i];
        }
        return pixels;
    }

    /**
     * 返回指定像素矩形的所有样本，以 int 数组形式，每个数组元素一个样本。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         左上角像素位置的 X 坐标。
     * @param y         左上角像素位置的 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param iArray    如果非空，则将样本返回到此数组。
     * @param data      包含图像数据的 DataBuffer。
     * @return 指定像素区域的所有样本。
     * @see #setPixels(int, int, int, int, int[], DataBuffer)
     */
    public int[] getPixels(int x, int y, int w, int h,
                           int iArray[], DataBuffer data) {
        int x1 = x + w;
        int y1 = y + h;

        if (x < 0 || x >= width || w > width || x1 < 0 || x1 > width ||
            y < 0 || y >= height || h > height || y1 < 0 || y1 >  height)
        {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int pixels[];
        if (iArray != null) {
           pixels = iArray;
        } else {
           pixels = new int [w*h*numBands];
        }
        int lineOffset = y*scanlineStride + x;
        int dstOffset = 0;

        for (int i = 0; i < h; i++) {
           for (int j = 0; j < w; j++) {
              int value = data.getElem(lineOffset+j);
              for (int k=0; k < numBands; k++) {
                  pixels[dstOffset++] =
                     ((value & bitMasks[k]) >>> bitOffsets[k]);
              }
           }
           lineOffset += scanlineStride;
        }
        return pixels;
    }

    /**
     * 返回位于 (x,y) 的像素在指定波段的样本，以 int 形式。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param b         要返回的波段。
     * @param data      包含图像数据的 DataBuffer。
     * @return 指定像素在指定波段的样本。
     * @see #setSample(int, int, int, int, DataBuffer)
     */
    public int getSample(int x, int y, int b, DataBuffer data) {
        // 对 'b' 的边界检查将自动进行
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int sample = data.getElem(y * scanlineStride + x);
        return ((sample & bitMasks[b]) >>> bitOffsets[b]);
    }

    /**
     * 返回指定像素矩形在指定波段的所有样本，以 int 数组形式，每个数组元素一个样本。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         左上角像素位置的 X 坐标。
     * @param y         左上角像素位置的 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param b         要返回的波段。
     * @param iArray    如果非空，则将样本返回到此数组。
     * @param data      包含图像数据的 DataBuffer。
     * @return 指定像素区域在指定波段的所有样本。
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
        int lineOffset = y*scanlineStride + x;
        int dstOffset = 0;

        for (int i = 0; i < h; i++) {
           for (int j = 0; j < w; j++) {
              int value = data.getElem(lineOffset+j);
              samples[dstOffset++] =
                 ((value & bitMasks[b]) >>> bitOffsets[b]);
           }
           lineOffset += scanlineStride;
        }
        return samples;
    }

    /**
     * 从类型为 TransferType 的原始数组中设置 DataBuffer 中单个像素的数据。
     * 对于 SinglePixelPackedSampleModel，数组中只有第一个元素包含有效数据，且数组的类型必须与 SinglePixelPackedSampleModel 的存储数据类型相同。
     * <p>
     * 以下代码说明了如何将 DataBuffer <code>db1</code> 中的数据（其存储布局由 SinglePixelPackedSampleModel <code>sppsm1</code> 描述）传输到 DataBuffer <code>db2</code>（其存储布局由 SinglePixelPackedSampleModel <code>sppsm2</code> 描述）。
     * 该传输通常比使用 getPixel/setPixel 更高效。
     * <pre>
     *       SinglePixelPackedSampleModel sppsm1, sppsm2;
     *       DataBufferInt db1, db2;
     *       sppsm2.setDataElements(x, y, sppsm1.getDataElements(x, y, null,
     *                              db1), db2);
     * </pre>
     * 使用 getDataElements/setDataElements 在两个 DataBuffer/SampleModel 对之间进行传输是合法的，前提是 SampleModels 具有相同数量的波段，对应波段具有相同数量的样本位数，且 TransferTypes 相同。
     * <p>
     * obj 必须是类型为 TransferType 的原始数组。否则，将抛出 ClassCastException。如果坐标超出范围，或 obj 不足以容纳像素数据，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param obj       包含像素数据的原始数组。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getDataElements(int, int, Object, DataBuffer)
     */
    public void setDataElements(int x, int y, Object obj, DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }

        int type = getTransferType();

        switch(type) {

        case DataBuffer.TYPE_BYTE:

            byte[] barray = (byte[])obj;
            data.setElem(y*scanlineStride+x, ((int)barray[0])&0xff);
            break;

        case DataBuffer.TYPE_USHORT:

            short[] sarray = (short[])obj;
            data.setElem(y*scanlineStride+x, ((int)sarray[0])&0xffff);
            break;

        case DataBuffer.TYPE_INT:

            int[] iarray = (int[])obj;
            data.setElem(y*scanlineStride+x, iarray[0]);
            break;
        }
    }

    /**
     * 使用 int 数组中的样本设置 DataBuffer 中的像素。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param iArray    输入样本的 int 数组。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getPixel(int, int, int[], DataBuffer)
     */
    public void setPixel(int x, int y,
                         int iArray[],
                         DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int lineOffset = y * scanlineStride + x;
        int value = data.getElem(lineOffset);
        for (int i=0; i < numBands; i++) {
            value &= ~bitMasks[i];
            value |= ((iArray[i] << bitOffsets[i]) & bitMasks[i]);
        }
        data.setElem(lineOffset, value);
    }

    /**
     * 从包含每个数组元素一个样本的 int 数组中设置像素矩形的所有样本。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         左上角像素位置的 X 坐标。
     * @param y         左上角像素位置的 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param iArray    输入样本的 int 数组。
     * @param data      包含图像数据的 DataBuffer。
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

        int lineOffset = y*scanlineStride + x;
        int srcOffset = 0;

        for (int i = 0; i < h; i++) {
           for (int j = 0; j < w; j++) {
               int value = data.getElem(lineOffset+j);
               for (int k=0; k < numBands; k++) {
                   value &= ~bitMasks[k];
                   int srcValue = iArray[srcOffset++];
                   value |= ((srcValue << bitOffsets[k])
                             & bitMasks[k]);
               }
               data.setElem(lineOffset+j, value);
           }
           lineOffset += scanlineStride;
        }
    }

    /**
     * 使用 int 设置位于 (x,y) 的像素在指定波段的样本。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标。
     * @param y         像素位置的 Y 坐标。
     * @param b         要设置的波段。
     * @param s         输入样本的 int。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getSample(int, int, int, DataBuffer)
     */
    public void setSample(int x, int y, int b, int s,
                          DataBuffer data) {
        // 对 'b' 的边界检查将自动进行
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int value = data.getElem(y*scanlineStride + x);
        value &= ~bitMasks[b];
        value |= (s << bitOffsets[b]) & bitMasks[b];
        data.setElem(y*scanlineStride + x,value);
    }

    /**
     * 从包含每个数组元素一个样本的 int 数组中设置指定像素矩形在指定波段的所有样本。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         左上角像素位置的 X 坐标。
     * @param y         左上角像素位置的 Y 坐标。
     * @param w         像素矩形的宽度。
     * @param h         像素矩形的高度。
     * @param b         要设置的波段。
     * @param iArray    输入样本的 int 数组。
     * @param data      包含图像数据的 DataBuffer。
     * @see #getSamples(int, int, int, int, int, int[], DataBuffer)
     */
    public void setSamples(int x, int y, int w, int h, int b,
                          int iArray[], DataBuffer data) {
        // 对 'b' 的边界检查将自动进行
        if ((x < 0) || (y < 0) || (x + w > width) || (y + h > height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int lineOffset = y*scanlineStride + x;
        int srcOffset = 0;

        for (int i = 0; i < h; i++) {
           for (int j = 0; j < w; j++) {
              int value = data.getElem(lineOffset+j);
              value &= ~bitMasks[b];
              int sample = iArray[srcOffset++];
              value |= ((int)sample << bitOffsets[b]) & bitMasks[b];
              data.setElem(lineOffset+j,value);
           }
           lineOffset += scanlineStride;
        }
    }

    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof SinglePixelPackedSampleModel)) {
            return false;
        }

        SinglePixelPackedSampleModel that = (SinglePixelPackedSampleModel)o;
        return this.width == that.width &&
            this.height == that.height &&
            this.numBands == that.numBands &&
            this.dataType == that.dataType &&
            Arrays.equals(this.bitMasks, that.bitMasks) &&
            Arrays.equals(this.bitOffsets, that.bitOffsets) &&
            Arrays.equals(this.bitSizes, that.bitSizes) &&
            this.maxBitSize == that.maxBitSize &&
            this.scanlineStride == that.scanlineStride;
    }


                // 如果我们实现了 equals()，我们也必须实现 hashCode
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
        for (int i = 0; i < bitMasks.length; i++) {
            hash ^= bitMasks[i];
            hash <<= 8;
        }
        for (int i = 0; i < bitOffsets.length; i++) {
            hash ^= bitOffsets[i];
            hash <<= 8;
        }
        for (int i = 0; i < bitSizes.length; i++) {
            hash ^= bitSizes[i];
            hash <<= 8;
        }
        hash ^= maxBitSize;
        hash <<= 8;
        hash ^= scanlineStride;
        return hash;
    }
}
