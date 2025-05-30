
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

/**
 * <code>MultiPixelPackedSampleModel</code> 类表示单波段图像，并可以将多个单样本像素打包到一个数据元素中。像素不允许跨越数据元素。
 * 数据类型可以是 DataBuffer.TYPE_BYTE、DataBuffer.TYPE_USHORT 或 DataBuffer.TYPE_INT。每个像素必须是 2 的幂次位数，
 * 并且 2 的幂次数量的像素必须恰好适合一个数据元素。像素位跨度等于每个像素的位数。扫描行跨度以数据元素为单位，
 * 最后几个数据元素可能会用未使用的像素填充。数据位偏移是从 {@link DataBuffer} 开始到第一个像素的位偏移，
 * 必须是像素位跨度的倍数。
 * <p>
 * 以下代码说明了从 <code>DataBuffer</code> <code>data</code> 中提取像素 <code>x, y</code> 的位，
 * 并将像素数据存储在类型为 <code>dataType</code> 的数据元素中：
 * <pre>{@code
 *      int dataElementSize = DataBuffer.getDataTypeSize(dataType);
 *      int bitnum = dataBitOffset + x*pixelBitStride;
 *      int element = data.getElem(y*scanlineStride + bitnum/dataElementSize);
 *      int shift = dataElementSize - (bitnum & (dataElementSize-1))
 *                  - pixelBitStride;
 *      int pixel = (element >> shift) & ((1 << pixelBitStride) - 1);
 * }</pre>
 */

public class MultiPixelPackedSampleModel extends SampleModel
{
    /** 从一个像素到下一个像素的位数。 */
    int pixelBitStride;

    /** 提取数据元素中最右边像素的位掩码。 */
    int bitMask;

    /**
     * 每个数据元素中可以容纳的像素数。也用作每个像素的位数。
     */
    int pixelsPerDataElement;

    /** 数据元素的位大小。 */
    int dataElementSize;

    /** 数据数组中第一个像素开始的位偏移。 */
    int dataBitOffset;

    /** 数据缓冲区描述的数据元素中的扫描行跨度。 */
    int scanlineStride;

    /**
     * 构造一个指定数据类型、宽度、高度和每个像素位数的 <code>MultiPixelPackedSampleModel</code>。
     * @param dataType 用于存储样本的数据类型
     * @param w 描述的图像数据区域的宽度（以像素为单位）
     * @param h 描述的图像数据区域的高度（以像素为单位）
     * @param numberOfBits 每个像素的位数
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是
     *         <code>DataBuffer.TYPE_BYTE</code>、
     *         <code>DataBuffer.TYPE_USHORT</code> 或
     *         <code>DataBuffer.TYPE_INT</code>
     */
    public MultiPixelPackedSampleModel(int dataType,
                                       int w,
                                       int h,
                                       int numberOfBits) {
        this(dataType, w, h,
             numberOfBits,
            (w * numberOfBits + DataBuffer.getDataTypeSize(dataType) - 1) /
                DataBuffer.getDataTypeSize(dataType),
             0);
        if (dataType != DataBuffer.TYPE_BYTE &&
            dataType != DataBuffer.TYPE_USHORT &&
            dataType != DataBuffer.TYPE_INT) {
            throw new IllegalArgumentException("不支持的数据类型 " +
                                               dataType);
        }
    }

    /**
     * 构造一个指定数据类型、宽度、高度、每个像素位数、扫描行跨度和数据位偏移的 <code>MultiPixelPackedSampleModel</code>。
     * @param dataType 用于存储样本的数据类型
     * @param w 描述的图像数据区域的宽度（以像素为单位）
     * @param h 描述的图像数据区域的高度（以像素为单位）
     * @param numberOfBits 每个像素的位数
     * @param scanlineStride 图像数据的行跨度
     * @param dataBitOffset 描述的图像数据区域的数据位偏移
     * @exception RasterFormatException 如果每个像素的位数不是 2 的幂次，
     *                  或者 2 的幂次数量的像素不能恰好适合一个数据元素。
     * @throws IllegalArgumentException 如果 <code>w</code> 或
     *         <code>h</code> 不大于 0
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是
     *         <code>DataBuffer.TYPE_BYTE</code>、
     *         <code>DataBuffer.TYPE_USHORT</code> 或
     *         <code>DataBuffer.TYPE_INT</code>
     */
    public MultiPixelPackedSampleModel(int dataType, int w, int h,
                                       int numberOfBits,
                                       int scanlineStride,
                                       int dataBitOffset) {
        super(dataType, w, h, 1);
        if (dataType != DataBuffer.TYPE_BYTE &&
            dataType != DataBuffer.TYPE_USHORT &&
            dataType != DataBuffer.TYPE_INT) {
            throw new IllegalArgumentException("不支持的数据类型 " +
                                               dataType);
        }
        this.dataType = dataType;
        this.pixelBitStride = numberOfBits;
        this.scanlineStride = scanlineStride;
        this.dataBitOffset = dataBitOffset;
        this.dataElementSize = DataBuffer.getDataTypeSize(dataType);
        this.pixelsPerDataElement = dataElementSize / numberOfBits;
        if (pixelsPerDataElement * numberOfBits != dataElementSize) {
           throw new RasterFormatException("MultiPixelPackedSampleModel " +
                                             "不允许像素跨越数据元素边界");
        }
        this.bitMask = (1 << numberOfBits) - 1;
    }


    /**
     * 创建一个具有指定宽度和高度的新 <code>MultiPixelPackedSampleModel</code>。
     * 新的 <code>MultiPixelPackedSampleModel</code> 具有与这个
     * <code>MultiPixelPackedSampleModel</code> 相同的存储数据类型和每个像素的位数。
     * @param w 指定的宽度
     * @param h 指定的高度
     * @return 一个具有指定宽度和高度的 {@link SampleModel}，
     * 并且具有与这个 <code>MultiPixelPackedSampleModel</code> 相同的存储数据类型和每个像素的位数。
     * @throws IllegalArgumentException 如果 <code>w</code> 或
     *         <code>h</code> 不大于 0
     */
    public SampleModel createCompatibleSampleModel(int w, int h) {
      SampleModel sampleModel =
            new MultiPixelPackedSampleModel(dataType, w, h, pixelBitStride);
      return sampleModel;
    }

    /**
     * 创建一个与这个 <code>MultiPixelPackedSampleModel</code> 对应的 <code>DataBuffer</code>。
     * <code>DataBuffer</code> 对象的数据类型和大小与这个 <code>MultiPixelPackedSampleModel</code> 一致。
     * <code>DataBuffer</code> 有一个单银行。
     * @return 一个具有与这个 <code>MultiPixelPackedSampleModel</code> 相同数据类型和大小的 <code>DataBuffer</code>。
     */
    public DataBuffer createDataBuffer() {
        DataBuffer dataBuffer = null;

        int size = (int) scanlineStride * height;
        switch (dataType) {
        case DataBuffer.TYPE_BYTE:
            dataBuffer = new DataBufferByte(size + (dataBitOffset + 7) / 8);
            break;
        case DataBuffer.TYPE_USHORT:
            dataBuffer = new DataBufferUShort(size + (dataBitOffset + 15) / 16);
            break;
        case DataBuffer.TYPE_INT:
            dataBuffer = new DataBufferInt(size + (dataBitOffset + 31) / 32);
            break;
        }
        return dataBuffer;
    }

    /**
     * 返回通过 {@link #getDataElements} 和 {@link #setDataElements}
     * 方法传输一个像素所需的数据元素数量。对于 <code>MultiPixelPackedSampleModel</code>，这是 1。
     * @return 数据元素的数量。
     */
    public int getNumDataElements() {
        return 1;
    }

    /**
     * 返回所有波段的每个样本的位数。
     * @return 每个样本的位数。
     */
    public int[] getSampleSize() {
        int sampleSize[] = {pixelBitStride};
        return sampleSize;
    }

    /**
     * 返回指定波段的每个样本的位数。
     * @param band 指定的波段
     * @return 指定波段的每个样本的位数。
     */
    public int getSampleSize(int band) {
        return pixelBitStride;
    }

    /**
     * 返回像素 (x, y) 在数据数组元素中的偏移。
     * @param x 指定像素的 X 坐标
     * @param y 指定像素的 Y 坐标
     * @return 指定像素的偏移。
     */
    public int getOffset(int x, int y) {
        int offset = y * scanlineStride;
        offset +=  (x * pixelBitStride + dataBitOffset) / dataElementSize;
        return offset;
    }

    /**
     * 返回存储第 x 个像素的扫描行中的数据元素中的位偏移。
     * 这个偏移在所有扫描行中是相同的。
     * @param x 指定的像素
     * @return 指定像素的位偏移。
     */
    public int getBitOffset(int x){
       return  (x * pixelBitStride + dataBitOffset) % dataElementSize;
    }

    /**
     * 返回扫描行跨度。
     * @return 这个 <code>MultiPixelPackedSampleModel</code> 的扫描行跨度。
     */
    public int getScanlineStride() {
        return scanlineStride;
    }

    /**
     * 返回像素位跨度（以位为单位）。这个值与每个像素的位数相同。
     * @return 这个 <code>MultiPixelPackedSampleModel</code> 的 <code>pixelBitStride</code>。
     */
    public int getPixelBitStride() {
        return pixelBitStride;
    }

    /**
     * 返回数据位偏移（以位为单位）。
     * @return 这个 <code>MultiPixelPackedSampleModel</code> 的 <code>dataBitOffset</code>。
     */
    public int getDataBitOffset() {
        return dataBitOffset;
    }

    /**
     * 返回通过 <code>getDataElements</code> 和 <code>setDataElements</code>
     * 方法传输像素的 TransferType。TransferType 可能与存储 DataType 相同，也可能不同。
     * TransferType 是 DataBuffer.TYPE_BYTE、DataBuffer.TYPE_USHORT 或 DataBuffer.TYPE_INT 之一。
     * @return transfertype。
     */
    public int getTransferType() {
        if (pixelBitStride > 16)
            return DataBuffer.TYPE_INT;
        else if (pixelBitStride > 8)
            return DataBuffer.TYPE_USHORT;
        else
            return DataBuffer.TYPE_BYTE;
    }

    /**
     * 创建一个具有这个 <code>MultiPixelPackedSampleModel</code> 的子集波段的新 <code>MultiPixelPackedSampleModel</code>。
     * 由于 <code>MultiPixelPackedSampleModel</code> 只有一个波段，因此 bands 参数必须有一个长度为 1 的数组，
     * 并且表示第 0 个波段。
     * @param bands 指定的波段
     * @return 一个具有这个 <code>MultiPixelPackedSampleModel</code> 的子集波段的新 <code>SampleModel</code>。
     * @exception RasterFormatException 如果请求的波段数量不是 1。
     * @throws IllegalArgumentException 如果 <code>w</code> 或
     *         <code>h</code> 不大于 0
     */
    public SampleModel createSubsetSampleModel(int bands[]) {
        if (bands != null) {
           if (bands.length != 1)
            throw new RasterFormatException("MultiPixelPackedSampleModel 有 "
                                            + "只有一个波段。");
        }
        SampleModel sm = createCompatibleSampleModel(width, height);
        return sm;
    }

    /**
     * 返回位于 (x, y) 的像素在指定波段中的样本，作为 <code>int</code>。
     * 如果坐标不在范围内，则抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x 指定像素的 X 坐标
     * @param y 指定像素的 Y 坐标
     * @param b 返回的波段，假设为 0
     * @param data 包含图像数据的 <code>DataBuffer</code>
     * @return 指定波段中包含的指定像素的样本。
     * @exception ArrayIndexOutOfBoundsException 如果指定的坐标不在范围内。
     * @see #setSample(int, int, int, int, DataBuffer)
     */
    public int getSample(int x, int y, int b, DataBuffer data) {
        // 'b' 必须为 0
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height) ||
            (b != 0)) {
            throw new ArrayIndexOutOfBoundsException
                ("坐标超出范围！");
        }
        int bitnum = dataBitOffset + x * pixelBitStride;
        int element = data.getElem(y * scanlineStride + bitnum / dataElementSize);
        int shift = dataElementSize - (bitnum & (dataElementSize - 1))
                    - pixelBitStride;
        return (element >> shift) & bitMask;
    }

    /**
     * 使用 <code>int</code> 作为输入，在 <code>DataBuffer</code> 中位于 (x, y) 的像素的指定波段中设置样本。
     * 如果坐标不在范围内，则抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x 指定像素的 X 坐标
     * @param y 指定像素的 Y 坐标
     * @param b 返回的波段，假设为 0
     * @param s 输入样本，作为 <code>int</code>
     * @param data 存储图像数据的 <code>DataBuffer</code>
     * @exception ArrayIndexOutOfBoundsException 如果坐标不在范围内。
     * @see #getSample(int, int, int, DataBuffer)
     */
    public void setSample(int x, int y, int b, int s,
                          DataBuffer data) {
        // 'b' 必须为 0
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height) ||
            (b != 0)) {
            throw new ArrayIndexOutOfBoundsException
                ("坐标超出范围！");
        }
        int bitnum = dataBitOffset + x * pixelBitStride;
        int index = y * scanlineStride + (bitnum / dataElementSize);
        int shift = dataElementSize - (bitnum & (dataElementSize - 1))
                    - pixelBitStride;
        int element = data.getElem(index);
        element &= ~(bitMask << shift);
        element |= (s & bitMask) << shift;
        data.setElem(index, element);
    }


                /**
     * 返回类型为 TransferType 的原始数组中的单个像素数据。对于 <code>MultiPixelPackedSampleModel</code>，
     * 数组有一个元素，类型是 DataBuffer.TYPE_BYTE、DataBuffer.TYPE_USHORT 或 DataBuffer.TYPE_INT
     * 中能容纳单个像素的最小类型。通常，<code>obj</code> 应该传入为 <code>null</code>，
     * 以便自动创建并确保是正确的原始数据类型。
     * <p>
     * 以下代码说明了如何将数据从 <code>DataBuffer</code> <code>db1</code>（其存储布局由
     * <code>MultiPixelPackedSampleModel</code> <code>mppsm1</code> 描述）传输到
     * <code>DataBuffer</code> <code>db2</code>（其存储布局由 <code>MultiPixelPackedSampleModel</code>
     * <code>mppsm2</code> 描述）。这种传输通常比使用 <code>getPixel</code> 或 <code>setPixel</code>
     * 更高效。
     * <pre>
     *       MultiPixelPackedSampleModel mppsm1, mppsm2;
     *       DataBufferInt db1, db2;
     *       mppsm2.setDataElements(x, y, mppsm1.getDataElements(x, y, null,
     *                              db1), db2);
     * </pre>
     * 使用 <code>getDataElements</code> 或 <code>setDataElements</code> 在两个
     * <code>DataBuffer/SampleModel</code> 对之间传输是合法的，前提是 <code>SampleModels</code>
     * 具有相同数量的波段，对应波段具有相同数量的每样本位数，并且 TransferTypes 相同。
     * <p>
     * 如果 <code>obj</code> 不为 <code>null</code>，则它应该是类型为 TransferType 的原始数组。
     * 否则，将抛出 <code>ClassCastException</code>。如果坐标超出范围，或者 <code>obj</code>
     * 不为 <code>null</code> 且不足以容纳像素数据，将抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x 指定像素的 X 坐标
     * @param y 指定像素的 Y 坐标
     * @param obj 用于返回像素数据的原始数组或 <code>null</code>。
     * @param data 包含图像数据的 <code>DataBuffer</code>。
     * @return 包含指定像素数据的 <code>Object</code>。
     * @exception ClassCastException 如果 <code>obj</code> 不是类型为 TransferType 的原始数组或不为 <code>null</code>
     * @exception ArrayIndexOutOfBoundsException 如果坐标超出范围，或者 <code>obj</code> 不为 <code>null</code> 或不足以容纳像素数据
     * @see #setDataElements(int, int, Object, DataBuffer)
     */
    public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }

        int type = getTransferType();
        int bitnum = dataBitOffset + x*pixelBitStride;
        int shift = dataElementSize - (bitnum & (dataElementSize-1))
                    - pixelBitStride;
        int element = 0;

        switch(type) {

        case DataBuffer.TYPE_BYTE:

            byte[] bdata;

            if (obj == null)
                bdata = new byte[1];
            else
                bdata = (byte[])obj;

            element = data.getElem(y*scanlineStride +
                                    bitnum/dataElementSize);
            bdata[0] = (byte)((element >> shift) & bitMask);

            obj = (Object)bdata;
            break;

        case DataBuffer.TYPE_USHORT:

            short[] sdata;

            if (obj == null)
                sdata = new short[1];
            else
                sdata = (short[])obj;

            element = data.getElem(y*scanlineStride +
                                   bitnum/dataElementSize);
            sdata[0] = (short)((element >> shift) & bitMask);

            obj = (Object)sdata;
            break;

        case DataBuffer.TYPE_INT:

            int[] idata;

            if (obj == null)
                idata = new int[1];
            else
                idata = (int[])obj;

            element = data.getElem(y*scanlineStride +
                                   bitnum/dataElementSize);
            idata[0] = (element >> shift) & bitMask;

            obj = (Object)idata;
            break;
        }

        return obj;
    }

    /**
     * 返回指定单波段像素的 <code>int</code> 数组中的第一个元素。
     * 如果坐标超出范围，将抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x 指定像素的 X 坐标
     * @param y 指定像素的 Y 坐标
     * @param iArray 包含要返回的像素的数组或 <code>null</code>
     * @param data 存储图像数据的 <code>DataBuffer</code>
     * @return 包含指定像素的数组。
     * @exception ArrayIndexOutOfBoundsException 如果坐标超出范围
     * @see #setPixel(int, int, int[], DataBuffer)
     */
    public int[] getPixel(int x, int y, int iArray[], DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int pixels[];
        if (iArray != null) {
           pixels = iArray;
        } else {
           pixels = new int [numBands];
        }
        int bitnum = dataBitOffset + x*pixelBitStride;
        int element = data.getElem(y*scanlineStride + bitnum/dataElementSize);
        int shift = dataElementSize - (bitnum & (dataElementSize-1))
                    - pixelBitStride;
        pixels[0] = (element >> shift) & bitMask;
        return pixels;
    }

    /**
     * 从类型为 TransferType 的原始数组中设置指定 <code>DataBuffer</code> 中的单个像素数据。
     * 对于 <code>MultiPixelPackedSampleModel</code>，数组中只有第一个元素包含有效数据，
     * 类型必须是 DataBuffer.TYPE_BYTE、DataBuffer.TYPE_USHORT 或 DataBuffer.TYPE_INT
     * 中能容纳单个像素的最小类型。
     * <p>
     * 以下代码说明了如何将数据从 <code>DataBuffer</code> <code>db1</code>（其存储布局由
     * <code>MultiPixelPackedSampleModel</code> <code>mppsm1</code> 描述）传输到
     * <code>DataBuffer</code> <code>db2</code>（其存储布局由 <code>MultiPixelPackedSampleModel</code>
     * <code>mppsm2</code> 描述）。这种传输通常比使用 <code>getPixel</code> 或 <code>setPixel</code>
     * 更高效。
     * <pre>
     *       MultiPixelPackedSampleModel mppsm1, mppsm2;
     *       DataBufferInt db1, db2;
     *       mppsm2.setDataElements(x, y, mppsm1.getDataElements(x, y, null,
     *                              db1), db2);
     * </pre>
     * 使用 <code>getDataElements</code> 或 <code>setDataElements</code> 在两个
     * <code>DataBuffer/SampleModel</code> 对之间传输是合法的，前提是 <code>SampleModel</code> 对象
     * 具有相同数量的波段，对应波段具有相同数量的每样本位数，并且 TransferTypes 相同。
     * <p>
     * <code>obj</code> 必须是类型为 TransferType 的原始数组。否则，将抛出 <code>ClassCastException</code>。
     * 如果坐标超出范围，或者 <code>obj</code> 不足以容纳像素数据，将抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x 像素位置的 X 坐标
     * @param y 像素位置的 Y 坐标
     * @param obj 包含像素数据的原始数组
     * @param data 包含图像数据的 <code>DataBuffer</code>
     * @see #getDataElements(int, int, Object, DataBuffer)
     */
    public void setDataElements(int x, int y, Object obj, DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }

        int type = getTransferType();
        int bitnum = dataBitOffset + x * pixelBitStride;
        int index = y * scanlineStride + (bitnum / dataElementSize);
        int shift = dataElementSize - (bitnum & (dataElementSize-1))
                    - pixelBitStride;
        int element = data.getElem(index);
        element &= ~(bitMask << shift);

        switch(type) {

        case DataBuffer.TYPE_BYTE:

            byte[] barray = (byte[])obj;
            element |= ( ((int)(barray[0])&0xff) & bitMask) << shift;
            data.setElem(index, element);
            break;

        case DataBuffer.TYPE_USHORT:

            short[] sarray = (short[])obj;
            element |= ( ((int)(sarray[0])&0xffff) & bitMask) << shift;
            data.setElem(index, element);
            break;

        case DataBuffer.TYPE_INT:

            int[] iarray = (int[])obj;
            element |= (iarray[0] & bitMask) << shift;
            data.setElem(index, element);
            break;
        }
    }

    /**
     * 使用 <code>int</code> 数组作为输入，在 <code>DataBuffer</code> 中设置一个像素。
     * 如果坐标超出范围，将抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param x 像素位置的 X 坐标
     * @param y 像素位置的 Y 坐标
     * @param iArray 输入像素的 <code>int</code> 数组
     * @param data 包含图像数据的 <code>DataBuffer</code>
     * @see #getPixel(int, int, int[], DataBuffer)
     */
    public void setPixel(int x, int y, int[] iArray, DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int bitnum = dataBitOffset + x * pixelBitStride;
        int index = y * scanlineStride + (bitnum / dataElementSize);
        int shift = dataElementSize - (bitnum & (dataElementSize-1))
                    - pixelBitStride;
        int element = data.getElem(index);
        element &= ~(bitMask << shift);
        element |= (iArray[0] & bitMask) << shift;
        data.setElem(index,element);
    }

    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof MultiPixelPackedSampleModel)) {
            return false;
        }

        MultiPixelPackedSampleModel that = (MultiPixelPackedSampleModel)o;
        return this.width == that.width &&
            this.height == that.height &&
            this.numBands == that.numBands &&
            this.dataType == that.dataType &&
            this.pixelBitStride == that.pixelBitStride &&
            this.bitMask == that.bitMask &&
            this.pixelsPerDataElement == that.pixelsPerDataElement &&
            this.dataElementSize == that.dataElementSize &&
            this.dataBitOffset == that.dataBitOffset &&
            this.scanlineStride == that.scanlineStride;
    }

    // 如果实现了 equals()，则必须实现 hashCode
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
        hash ^= pixelBitStride;
        hash <<= 8;
        hash ^= bitMask;
        hash <<= 8;
        hash ^= pixelsPerDataElement;
        hash <<= 8;
        hash ^= dataElementSize;
        hash <<= 8;
        hash ^= dataBitOffset;
        hash <<= 8;
        hash ^= scanlineStride;
        return hash;
    }
}
