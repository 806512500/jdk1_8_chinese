
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
 * 该类表示以带状交错方式存储的图像数据，每个像素的每个样本占用 DataBuffer 中的一个数据元素。
 * 它继承自 ComponentSampleModel，但为访问带状交错图像数据提供了更高效的实现。此类通常用于处理将每个带的样本数据存储在 DataBuffer 的不同银行中的图像。
 * 提供了访问器方法，以便可以直接操作图像数据。像素步幅是同一扫描行上同一带的两个样本之间的数据数组元素数。BandedSampleModel 的像素步幅为一。
 * 扫描行步幅是一个给定样本与其在下一扫描行的同一列中的对应样本之间的数据数组元素数。带偏移表示从 DataBuffer 的每个银行的第一个数据数组元素到该带的第一个样本的数据数组元素数。
 * 带编号从 0 到 N-1。银行索引表示数据缓冲区的银行与图像数据的带之间的对应关系。此类支持
 * {@link DataBuffer#TYPE_BYTE TYPE_BYTE}，
 * {@link DataBuffer#TYPE_USHORT TYPE_USHORT}，
 * {@link DataBuffer#TYPE_SHORT TYPE_SHORT}，
 * {@link DataBuffer#TYPE_INT TYPE_INT}，
 * {@link DataBuffer#TYPE_FLOAT TYPE_FLOAT} 和
 * {@link DataBuffer#TYPE_DOUBLE TYPE_DOUBLE} 数据类型
 */


public final class BandedSampleModel extends ComponentSampleModel
{

    /**
     * 使用指定参数构造一个 BandedSampleModel。像素步幅将是一个数据元素。扫描行步幅将与宽度相同。每个带将存储在不同的银行中，所有带偏移将为零。
     * @param dataType 用于存储样本的数据类型。
     * @param w 图像数据描述的区域的宽度（以像素为单位）。
     * @param h 图像数据描述的区域的高度（以像素为单位）。
     * @param numBands 图像数据的带数。
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是支持的数据类型之一
     */
    public BandedSampleModel(int dataType, int w, int h, int numBands) {
        super(dataType, w, h, 1, w,
              BandedSampleModel.createIndicesArray(numBands),
              BandedSampleModel.createOffsetArray(numBands));
    }

    /**
     * 使用指定参数构造一个 BandedSampleModel。带数将从 bandOffsets 和 bankIndices 数组的长度推断得出，这两个数组必须相等。像素步幅将是一个数据元素。
     * @param dataType 用于存储样本的数据类型。
     * @param w 图像数据描述的区域的宽度（以像素为单位）。
     * @param h 图像数据描述的区域的高度（以像素为单位）。
     * @param scanlineStride 图像数据的行步幅。
     * @param bankIndices 每个带的银行索引。
     * @param bandOffsets 每个带的带偏移。
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是支持的数据类型之一
     */
    public BandedSampleModel(int dataType,
                             int w, int h,
                             int scanlineStride,
                             int bankIndices[],
                             int bandOffsets[]) {

        super(dataType, w, h, 1,scanlineStride, bankIndices, bandOffsets);
    }

    /**
     * 创建一个具有指定宽度和高度的新 BandedSampleModel。新的 BandedSampleModel 将具有与该 BandedSampleModel 相同的带数、存储数据类型和银行索引。
     * 带偏移将被压缩，使得带之间的偏移为 w*pixelStride，并且所有带偏移的最小值为零。
     * @param w 结果 <code>BandedSampleModel</code> 的宽度。
     * @param h 结果 <code>BandedSampleModel</code> 的高度。
     * @return 一个具有指定宽度和高度的新 <code>BandedSampleModel</code>。
     * @throws IllegalArgumentException 如果 <code>w</code> 或 <code>h</code> 等于 <code>Integer.MAX_VALUE</code> 或 <code>Integer.MIN_VALUE</code>
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是支持的数据类型之一
     */
    public SampleModel createCompatibleSampleModel(int w, int h) {
        int[] bandOffs;

        if (numBanks == 1) {
            bandOffs = orderBands(bandOffsets, w*h);
        }
        else {
            bandOffs = new int[bandOffsets.length];
        }

        SampleModel sampleModel =
            new BandedSampleModel(dataType, w, h, w, bankIndices, bandOffs);
        return sampleModel;
    }

    /**
     * 创建一个具有该 BandedSampleModel 的子集带的新 BandedSampleModel。新的 BandedSampleModel 可以与现有的 BandedSampleModel 可以使用的任何 DataBuffer 一起使用。
     * 新的 BandedSampleModel/DataBuffer 组合将表示具有原始 BandedSampleModel/DataBuffer 组合的子集带的图像。
     * @throws RasterFormatException 如果带数大于此样本模型的银行数。
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是支持的数据类型之一
     */
    public SampleModel createSubsetSampleModel(int bands[]) {
        if (bands.length > bankIndices.length)
            throw new RasterFormatException("只有 " +
                                            bankIndices.length +
                                            " 个带");
        int newBankIndices[] = new int[bands.length];
        int newBandOffsets[] = new int[bands.length];

        for (int i=0; i<bands.length; i++) {
            newBankIndices[i] = bankIndices[bands[i]];
            newBandOffsets[i] = bandOffsets[bands[i]];
        }

        return new BandedSampleModel(this.dataType, width, height,
                                     this.scanlineStride,
                                     newBankIndices, newBandOffsets);
    }

    /**
     * 创建一个与该 BandedSampleModel 对应的 DataBuffer。DataBuffer 的数据类型、银行数和大小将与此 BandedSampleModel 一致。
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是支持的类型之一。
     */
    public DataBuffer createDataBuffer() {
        DataBuffer dataBuffer = null;

        int size = scanlineStride * height;
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
        default:
            throw new IllegalArgumentException("dataType 不是支持的类型之一。");
        }

        return dataBuffer;
    }


    /**
     * 以 TransferType 类型的原始数组形式返回单个像素的数据。对于 BandedSampleModel，这将与数据类型相同，样本将按数组元素返回。通常，obj
     * 应该传递为 null，以便自动创建对象并具有正确的原始数据类型。
     * <p>
     * 以下代码说明了如何将数据从 DataBuffer <code>db1</code>（其存储布局由 BandedSampleModel <code>bsm1</code> 描述）传输到 DataBuffer <code>db2</code>（其存储布局由
     * BandedSampleModel <code>bsm2</code> 描述）。传输通常比使用 getPixel/setPixel 更高效。
     * <pre>
     *       BandedSampleModel bsm1, bsm2;
     *       DataBufferInt db1, db2;
     *       bsm2.setDataElements(x, y, bsm1.getDataElements(x, y, null, db1),
     *                            db2);
     * </pre>
     * 使用 getDataElements/setDataElements 在两个 DataBuffer/SampleModel 对之间传输数据是合法的，如果 SampleModels 具有相同数量的带，对应带具有相同数量的样本位数，并且 TransferTypes 相同。
     * <p>
     * 如果 obj 非空，则它应该是一个 TransferType 类型的原始数组。否则，将抛出 ClassCastException。如果坐标超出范围，或者 obj 非空且不足以容纳像素数据，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标
     * @param y         像素位置的 Y 坐标
     * @param obj       如果非空，则在其中返回像素数据的原始数组。
     * @param data      包含图像数据的 DataBuffer。
     * @return 指定像素的数据。
     * @see #setDataElements(int, int, Object, DataBuffer)
     */
    public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("坐标超出范围！");
        }
        int type = getTransferType();
        int numDataElems = getNumDataElements();
        int pixelOffset = y*scanlineStride + x;

        switch(type) {

        case DataBuffer.TYPE_BYTE:

            byte[] bdata;

            if (obj == null) {
                bdata = new byte[numDataElems];
            } else {
                bdata = (byte[])obj;
            }

            for (int i=0; i<numDataElems; i++) {
                bdata[i] = (byte)data.getElem(bankIndices[i],
                                              pixelOffset + bandOffsets[i]);
            }

            obj = (Object)bdata;
            break;

        case DataBuffer.TYPE_USHORT:
        case DataBuffer.TYPE_SHORT:

            short[] sdata;

            if (obj == null) {
                sdata = new short[numDataElems];
            } else {
                sdata = (short[])obj;
            }

            for (int i=0; i<numDataElems; i++) {
                sdata[i] = (short)data.getElem(bankIndices[i],
                                               pixelOffset + bandOffsets[i]);
            }

            obj = (Object)sdata;
            break;

        case DataBuffer.TYPE_INT:

            int[] idata;

            if (obj == null) {
                idata = new int[numDataElems];
            } else {
                idata = (int[])obj;
            }

            for (int i=0; i<numDataElems; i++) {
                idata[i] = data.getElem(bankIndices[i],
                                        pixelOffset + bandOffsets[i]);
            }

            obj = (Object)idata;
            break;

        case DataBuffer.TYPE_FLOAT:

            float[] fdata;

            if (obj == null) {
                fdata = new float[numDataElems];
            } else {
                fdata = (float[])obj;
            }

            for (int i=0; i<numDataElems; i++) {
                fdata[i] = data.getElemFloat(bankIndices[i],
                                             pixelOffset + bandOffsets[i]);
            }

            obj = (Object)fdata;
            break;

        case DataBuffer.TYPE_DOUBLE:

            double[] ddata;

            if (obj == null) {
                ddata = new double[numDataElems];
            } else {
                ddata = (double[])obj;
            }

            for (int i=0; i<numDataElems; i++) {
                ddata[i] = data.getElemDouble(bankIndices[i],
                                              pixelOffset + bandOffsets[i]);
            }

            obj = (Object)ddata;
            break;
        }

        return obj;
    }

    /**
     * 以 int 数组形式返回指定像素的所有样本。如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标
     * @param y         像素位置的 Y 坐标
     * @param iArray    如果非空，则返回样本的数组
     * @param data      包含图像数据的 DataBuffer
     * @return 指定像素的样本。
     * @see #setPixel(int, int, int[], DataBuffer)
     */
    public int[] getPixel(int x, int y, int iArray[], DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("坐标超出范围！");
        }


                    int[] pixels;

        if (iArray != null) {
           pixels = iArray;
        } else {
           pixels = new int [numBands];
        }

        int pixelOffset = y*scanlineStride + x;
        for (int i=0; i<numBands; i++) {
            pixels[i] = data.getElem(bankIndices[i],
                                     pixelOffset + bandOffsets[i]);
        }
        return pixels;
    }

    /**
     * 返回指定像素矩形的所有样本，每个样本对应数据数组中的一个元素。
     * 如果坐标超出范围，可能会抛出ArrayIndexOutOfBoundsException。
     * @param x         像素位置的X坐标
     * @param y         像素位置的Y坐标
     * @param w         像素矩形的宽度
     * @param h         像素矩形的高度
     * @param iArray    如果非空，将样本返回到此数组
     * @param data      包含图像数据的DataBuffer
     * @return 指定区域内的像素样本。
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
        int[] pixels;

        if (iArray != null) {
           pixels = iArray;
        } else {
           pixels = new int[w*h*numBands];
        }

        for (int k = 0; k < numBands; k++) {
            int lineOffset = y*scanlineStride + x + bandOffsets[k];
            int srcOffset = k;
            int bank = bankIndices[k];

            for (int i = 0; i < h; i++) {
                int pixelOffset = lineOffset;
                for (int j = 0; j < w; j++) {
                    pixels[srcOffset] = data.getElem(bank, pixelOffset++);
                    srcOffset += numBands;
                }
                lineOffset += scanlineStride;
            }
        }
        return pixels;
    }

    /**
     * 返回位于(x,y)的像素在指定波段的样本。
     * 如果坐标超出范围，可能会抛出ArrayIndexOutOfBoundsException。
     * @param x         像素位置的X坐标
     * @param y         像素位置的Y坐标
     * @param b         要返回的波段
     * @param data      包含图像数据的DataBuffer
     * @return 指定像素在指定波段的样本。
     * @see #setSample(int, int, int, int, DataBuffer)
     */
    public int getSample(int x, int y, int b, DataBuffer data) {
        // 对'b'的边界检查将自动执行
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int sample =
            data.getElem(bankIndices[b],
                         y*scanlineStride + x + bandOffsets[b]);
        return sample;
    }

    /**
     * 返回位于(x,y)的像素在指定波段的样本，以float形式表示。
     * 如果坐标超出范围，可能会抛出ArrayIndexOutOfBoundsException。
     * @param x         像素位置的X坐标
     * @param y         像素位置的Y坐标
     * @param b         要返回的波段
     * @param data      包含图像数据的DataBuffer
     * @return 一个float值，表示指定像素在指定波段的样本。
     */
    public float getSampleFloat(int x, int y, int b, DataBuffer data) {
        // 对'b'的边界检查将自动执行
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }

        float sample = data.getElemFloat(bankIndices[b],
                                    y*scanlineStride + x + bandOffsets[b]);
        return sample;
    }

    /**
     * 返回位于(x,y)的像素在指定波段的样本，以double形式表示。
     * 如果坐标超出范围，可能会抛出ArrayIndexOutOfBoundsException。
     * @param x         像素位置的X坐标
     * @param y         像素位置的Y坐标
     * @param b         要返回的波段
     * @param data      包含图像数据的DataBuffer
     * @return 一个double值，表示指定像素在指定波段的样本。
     */
    public double getSampleDouble(int x, int y, int b, DataBuffer data) {
        // 对'b'的边界检查将自动执行
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }

        double sample = data.getElemDouble(bankIndices[b],
                                       y*scanlineStride + x + bandOffsets[b]);
        return sample;
    }

    /**
     * 返回指定矩形内像素在指定波段的样本，每个样本对应数据数组中的一个元素。
     * 如果坐标超出范围，可能会抛出ArrayIndexOutOfBoundsException。
     * @param x         像素位置的X坐标
     * @param y         像素位置的Y坐标
     * @param w         像素矩形的宽度
     * @param h         像素矩形的高度
     * @param b         要返回的波段
     * @param iArray    如果非空，将样本返回到此数组
     * @param data      包含图像数据的DataBuffer
     * @return 指定区域内像素在指定波段的样本。
     * @see #setSamples(int, int, int, int, int, int[], DataBuffer)
     */
    public int[] getSamples(int x, int y, int w, int h, int b,
                            int iArray[], DataBuffer data) {
        // 对'b'的边界检查将自动执行
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

        int lineOffset = y*scanlineStride + x + bandOffsets[b];
        int srcOffset = 0;
        int bank = bankIndices[b];

        for (int i = 0; i < h; i++) {
           int sampleOffset = lineOffset;
           for (int j = 0; j < w; j++) {
               samples[srcOffset++] = data.getElem(bank, sampleOffset++);
           }
           lineOffset += scanlineStride;
        }
        return samples;
    }

    /**
     * 从一个原始数组中设置指定DataBuffer中单个像素的数据。对于BandedSampleModel，
     * 这将与数据类型相同，样本将按数组元素逐一传输。
     * <p>
     * 以下代码说明了如何从DataBuffer <code>db1</code>（其存储布局由BandedSampleModel <code>bsm1</code>描述）
     * 将数据传输到DataBuffer <code>db2</code>（其存储布局由BandedSampleModel <code>bsm2</code>描述）。
     * 该传输通常比使用getPixel/setPixel更高效。
     * <pre>
     *       BandedSampleModel bsm1, bsm2;
     *       DataBufferInt db1, db2;
     *       bsm2.setDataElements(x, y, bsm1.getDataElements(x, y, null, db1),
     *                            db2);
     * </pre>
     * 使用getDataElements/setDataElements在两个DataBuffer/SampleModel对之间传输数据是合法的，如果SampleModels具有相同数量的波段，
     * 对应波段具有相同数量的样本位数，且TransferTypes相同。
     * <p>
     * obj必须是一个原始数组，类型为TransferType。否则，将抛出ClassCastException。如果坐标超出范围，或obj不足以容纳像素数据，
     * 可能会抛出ArrayIndexOutOfBoundsException。
     * @param x         像素位置的X坐标
     * @param y         像素位置的Y坐标
     * @param obj       如果非空，返回此对象中的原始数组
     * @param data      包含图像数据的DataBuffer
     * @see #getDataElements(int, int, Object, DataBuffer)
     */
    public void setDataElements(int x, int y, Object obj, DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int type = getTransferType();
        int numDataElems = getNumDataElements();
        int pixelOffset = y*scanlineStride + x;

        switch(type) {

        case DataBuffer.TYPE_BYTE:

            byte[] barray = (byte[])obj;

            for (int i=0; i<numDataElems; i++) {
                data.setElem(bankIndices[i], pixelOffset + bandOffsets[i],
                             barray[i] & 0xff);
            }
            break;

        case DataBuffer.TYPE_USHORT:
        case DataBuffer.TYPE_SHORT:

            short[] sarray = (short[])obj;

            for (int i=0; i<numDataElems; i++) {
                data.setElem(bankIndices[i], pixelOffset + bandOffsets[i],
                             sarray[i] & 0xffff);
            }
            break;

        case DataBuffer.TYPE_INT:

            int[] iarray = (int[])obj;

            for (int i=0; i<numDataElems; i++) {
                data.setElem(bankIndices[i], pixelOffset + bandOffsets[i],
                             iarray[i]);
            }
            break;

        case DataBuffer.TYPE_FLOAT:

            float[] farray = (float[])obj;

            for (int i=0; i<numDataElems; i++) {
                data.setElemFloat(bankIndices[i], pixelOffset + bandOffsets[i],
                                  farray[i]);
            }
            break;

        case DataBuffer.TYPE_DOUBLE:

            double[] darray = (double[])obj;

            for (int i=0; i<numDataElems; i++) {
                data.setElemDouble(bankIndices[i], pixelOffset + bandOffsets[i],
                                   darray[i]);
            }
            break;

        }
    }

    /**
     * 使用int数组中的样本设置DataBuffer中的像素。
     * 如果坐标超出范围，可能会抛出ArrayIndexOutOfBoundsException。
     * @param x         像素位置的X坐标
     * @param y         像素位置的Y坐标
     * @param iArray    输入样本的int数组
     * @param data      包含图像数据的DataBuffer
     * @see #getPixel(int, int, int[], DataBuffer)
     */
    public void setPixel(int x, int y, int iArray[], DataBuffer data) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
       int pixelOffset = y*scanlineStride + x;
       for (int i=0; i<numBands; i++) {
           data.setElem(bankIndices[i], pixelOffset + bandOffsets[i],
                        iArray[i]);
       }
    }

    /**
     * 从包含每个样本的int数组中设置矩形像素的所有样本。
     * 如果坐标超出范围，可能会抛出ArrayIndexOutOfBoundsException。
     * @param x         像素位置的X坐标
     * @param y         像素位置的Y坐标
     * @param w         像素矩形的宽度
     * @param h         像素矩形的高度
     * @param iArray    输入样本的int数组
     * @param data      包含图像数据的DataBuffer
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

        for (int k = 0; k < numBands; k++) {
            int lineOffset = y*scanlineStride + x + bandOffsets[k];
            int srcOffset = k;
            int bank = bankIndices[k];

            for (int i = 0; i < h; i++) {
                int pixelOffset = lineOffset;
                for (int j = 0; j < w; j++) {
                    data.setElem(bank, pixelOffset++, iArray[srcOffset]);
                    srcOffset += numBands;
                }
                lineOffset += scanlineStride;
           }
        }
    }

    /**
     * 使用int输入设置位于(x,y)的像素在指定波段的样本。
     * 如果坐标超出范围，可能会抛出ArrayIndexOutOfBoundsException。
     * @param x         像素位置的X坐标
     * @param y         像素位置的Y坐标
     * @param b         要设置的波段
     * @param s         输入样本，类型为int
     * @param data      包含图像数据的DataBuffer
     * @see #getSample(int, int, int, DataBuffer)
     */
    public void setSample(int x, int y, int b, int s,
                          DataBuffer data) {
        // 对'b'的边界检查将自动执行
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        data.setElem(bankIndices[b],
                     y*scanlineStride + x + bandOffsets[b], s);
    }

    /**
     * 使用float输入设置位于(x,y)的像素在指定波段的样本。
     * 如果坐标超出范围，可能会抛出ArrayIndexOutOfBoundsException。
     * @param x         像素位置的X坐标
     * @param y         像素位置的Y坐标
     * @param b         要设置的波段
     * @param s         输入样本，类型为float
     * @param data      包含图像数据的DataBuffer
     * @see #getSample(int, int, int, DataBuffer)
     */
    public void setSample(int x, int y, int b,
                          float s ,
                          DataBuffer data) {
        // 对'b'的边界检查将自动执行
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        data.setElemFloat(bankIndices[b],
                          y*scanlineStride + x + bandOffsets[b], s);
    }


                /**
     * 为位于 (x,y) 的像素在指定波段设置样本，使用 double 类型输入。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         像素位置的 X 坐标
     * @param y         像素位置的 Y 坐标
     * @param b         要设置的波段
     * @param s         输入样本，double 类型
     * @param data      包含图像数据的 DataBuffer
     * @see #getSample(int, int, int, DataBuffer)
     */
    public void setSample(int x, int y, int b,
                          double s,
                          DataBuffer data) {
        // 'b' 的边界检查将自动进行
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            throw new ArrayIndexOutOfBoundsException
                ("坐标超出范围!");
        }
        data.setElemDouble(bankIndices[b],
                          y*scanlineStride + x + bandOffsets[b], s);
    }

    /**
     * 为指定矩形区域的像素在指定波段设置样本，从包含每个数据数组元素一个样本的 int 数组中获取样本。
     * 如果坐标超出范围，可能会抛出 ArrayIndexOutOfBoundsException。
     * @param x         左上角像素位置的 X 坐标
     * @param y         左上角像素位置的 Y 坐标
     * @param w         像素矩形的宽度
     * @param h         像素矩形的高度
     * @param b         要设置的波段
     * @param iArray    输入样本数组
     * @param data      包含图像数据的 DataBuffer
     * @see #getSamples(int, int, int, int, int, int[], DataBuffer)
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           int iArray[], DataBuffer data) {
        // 'b' 的边界检查将自动进行
        if ((x < 0) || (y < 0) || (x + w > width) || (y + h > height)) {
            throw new ArrayIndexOutOfBoundsException
                ("坐标超出范围!");
        }
        int lineOffset = y*scanlineStride + x + bandOffsets[b];
        int srcOffset = 0;
        int bank = bankIndices[b];

        for (int i = 0; i < h; i++) {
           int sampleOffset = lineOffset;
           for (int j = 0; j < w; j++) {
              data.setElem(bank, sampleOffset++, iArray[srcOffset++]);
           }
           lineOffset += scanlineStride;
        }
    }

    private static int[] createOffsetArray(int numBands) {
        int[] bandOffsets = new int[numBands];
        for (int i=0; i < numBands; i++) {
            bandOffsets[i] = 0;
        }
        return bandOffsets;
    }

    private static int[] createIndicesArray(int numBands) {
        int[] bankIndices = new int[numBands];
        for (int i=0; i < numBands; i++) {
            bankIndices[i] = i;
        }
        return bankIndices;
    }

    // 使哈希码与其它 ComponentSampleModel 子类不同
    public int hashCode() {
        return super.hashCode() ^ 0x2;
    }
}
