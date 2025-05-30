
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
import java.awt.Rectangle;
import java.awt.Point;

import sun.awt.image.ByteInterleavedRaster;
import sun.awt.image.ShortInterleavedRaster;
import sun.awt.image.IntegerInterleavedRaster;
import sun.awt.image.ByteBandedRaster;
import sun.awt.image.ShortBandedRaster;
import sun.awt.image.BytePackedRaster;
import sun.awt.image.SunWritableRaster;

/**
 * 一个表示像素矩形数组的类。Raster 封装了一个 DataBuffer，用于存储样本值，以及一个 SampleModel，用于描述如何在 DataBuffer 中定位给定的样本值。
 * <p>
 * Raster 定义了占据平面特定矩形区域的像素值，不一定是 (0, 0)。该矩形称为 Raster 的边界矩形，可通过 getBounds 方法获取，由 minX、minY、width 和 height 值定义。minX 和 minY 值定义了 Raster 的左上角坐标。访问边界矩形之外的像素可能会导致异常被抛出，或者可能会引用到 Raster 关联的 DataBuffer 中的意外元素。用户有责任避免访问此类像素。
 * <p>
 * SampleModel 描述了 Raster 的样本如何存储在 DataBuffer 的原始数组元素中。样本可以一对一地存储，如在 PixelInterleavedSampleModel 或 BandedSampleModel 中，也可以多个样本打包存储在一个元素中，如在 SinglePixelPackedSampleModel 或 MultiPixelPackedSampleModel 中。SampleModel 还控制样本是否进行符号扩展，允许无符号数据存储在带符号的 Java 数据类型中，如 byte、short 和 int。
 * <p>
 * 虽然 Raster 可以位于平面的任何位置，但 SampleModel 使用一个简单的从 (0, 0) 开始的坐标系统。因此，Raster 包含一个转换因子，用于将像素位置映射到 Raster 的坐标系统和 SampleModel 的坐标系统之间。SampleModel 坐标系统到 Raster 坐标系统的转换可以通过 getSampleModelTranslateX 和 getSampleModelTranslateY 方法获取。
 * <p>
 * Raster 可以通过显式构造或使用 createChild 和 createTranslatedChild 方法与另一个 Raster 共享 DataBuffer。通过这些方法创建的 Raster 可以通过 getParent 方法返回创建它的 Raster 的引用。对于不是通过调用 createTranslatedChild 或 createChild 创建的 Raster，getParent 将返回 null。
 * <p>
 * createTranslatedChild 方法返回一个新的 Raster，该 Raster 与当前 Raster 共享所有数据，但占据一个相同宽度和高度但起始点不同的边界矩形。例如，如果父 Raster 占据的区域是从 (10, 10) 到 (100, 100)，而平移后的 Raster 定义为从 (50, 50) 开始，那么父 Raster 的 (20, 20) 像素和子 Raster 的 (60, 60) 像素在两个 Raster 共享的 DataBuffer 中占据相同的位置。在第一种情况下，应将 (-10, -10) 添加到像素坐标以获得相应的 SampleModel 坐标，在第二种情况下应将 (-50, -50) 添加。
 * <p>
 * 父 Raster 和子 Raster 之间的转换可以通过从父 Raster 的 sampleModelTranslateX 和 sampleModelTranslateY 值中减去子 Raster 的相应值来确定。
 * <p>
 * createChild 方法可用于创建一个新的 Raster，该 Raster 仅占据其父 Raster 的边界矩形的子集（具有相同的或平移的坐标系统）或具有其父 Raster 的子集的波段。
 * <p>
 * 所有构造方法都是受保护的。创建 Raster 的正确方法是使用此类中定义的静态 create 方法。这些方法创建的 Raster 实例使用标准的 Interleaved、Banded 和 Packed SampleModels，可能比通过组合外部生成的 SampleModel 和 DataBuffer 创建的 Raster 处理得更高效。
 * @see java.awt.image.DataBuffer
 * @see java.awt.image.SampleModel
 * @see java.awt.image.PixelInterleavedSampleModel
 * @see java.awt.image.BandedSampleModel
 * @see java.awt.image.SinglePixelPackedSampleModel
 * @see java.awt.image.MultiPixelPackedSampleModel
 */
public class Raster {

    /**
     * 描述此 Raster 的像素如何存储在 DataBuffer 中的 SampleModel。
     */
    protected SampleModel sampleModel;

    /** 存储图像数据的 DataBuffer。 */
    protected DataBuffer dataBuffer;

    /** 此 Raster 的左上角像素的 X 坐标。 */
    protected int minX;

    /** 此 Raster 的左上角像素的 Y 坐标。 */
    protected int minY;

    /** 此 Raster 的宽度。 */
    protected int width;

    /** 此 Raster 的高度。 */
    protected int height;

    /**
     * 从 Raster 的 SampleModel 坐标空间到 Raster 坐标空间的 X 轴转换。
     */
    protected int sampleModelTranslateX;

    /**
     * 从 Raster 的 SampleModel 坐标空间到 Raster 坐标空间的 Y 轴转换。
     */
    protected int sampleModelTranslateY;

    /** 此 Raster 的波段数。 */
    protected int numBands;

    /** 每个像素的 DataBuffer 数据元素数。 */
    protected int numDataElements;

    /** 此 Raster 的父 Raster，或 null。 */
    protected Raster parent;

    static private native void initIDs();
    static {
        ColorModel.loadLibraries();
        initIDs();
    }

    /**
     * 基于指定的数据类型、宽度、高度和波段数创建一个 Raster。
     *
     * <p> Raster 的左上角由 location 参数给出。如果 location 为 null，则使用 (0, 0)。dataType 参数应为 DataBuffer 类中定义的枚举值之一。
     *
     * <p> 注意，不支持 <code>DataBuffer.TYPE_INT</code> 的交错 Raster。要创建类型为 <code>DataBuffer.TYPE_INT</code> 的 1 波段 Raster，请使用 Raster.createPackedRaster()。
     * <p> 目前仅支持 TYPE_BYTE 和 TYPE_USHORT 数据类型。
     * @param dataType 用于存储样本的数据类型
     * @param w 图像数据的宽度（以像素为单位）
     * @param h 图像数据的高度（以像素为单位）
     * @param bands 波段数
     * @param location <code>Raster</code> 的左上角
     * @return 具有指定数据类型、宽度、高度和波段数的 WritableRaster 对象。
     * @throws RasterFormatException 如果 <code>w</code> 或 <code>h</code> 小于或等于零，或者计算 <code>location.x + w</code> 或 <code>location.y + h</code> 导致整数溢出
     */
    public static WritableRaster createInterleavedRaster(int dataType,
                                                         int w, int h,
                                                         int bands,
                                                         Point location) {
        int[] bandOffsets = new int[bands];
        for (int i = 0; i < bands; i++) {
            bandOffsets[i] = i;
        }
        return createInterleavedRaster(dataType, w, h, w * bands, bands,
                                       bandOffsets, location);
    }

    /**
     * 基于指定的数据类型、宽度、高度、扫描行步长、像素步长和波段偏移创建一个 Raster。波段数从 bandOffsets.length 推断得出。
     *
     * <p> Raster 的左上角由 location 参数给出。如果 location 为 null，则使用 (0, 0)。dataType 参数应为 DataBuffer 类中定义的枚举值之一。
     *
     * <p> 注意，不支持 <code>DataBuffer.TYPE_INT</code> 的交错 Raster。要创建类型为 <code>DataBuffer.TYPE_INT</code> 的 1 波段 Raster，请使用 Raster.createPackedRaster()。
     * <p> 目前仅支持 TYPE_BYTE 和 TYPE_USHORT 数据类型。
     * @param dataType 用于存储样本的数据类型
     * @param w 图像数据的宽度（以像素为单位）
     * @param h 图像数据的高度（以像素为单位）
     * @param scanlineStride 图像数据的行步长
     * @param pixelStride 图像数据的像素步长
     * @param bandOffsets 所有波段的偏移
     * @param location <code>Raster</code> 的左上角
     * @return 具有指定数据类型、宽度、高度、扫描行步长、像素步长和波段偏移的 WritableRaster 对象。
     * @throws RasterFormatException 如果 <code>w</code> 或 <code>h</code> 小于或等于零，或者计算 <code>location.x + w</code> 或 <code>location.y + h</code> 导致整数溢出
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是支持的数据类型之一，即 <code>DataBuffer.TYPE_BYTE</code> 或 <code>DataBuffer.TYPE_USHORT</code>。
     */
    public static WritableRaster createInterleavedRaster(int dataType,
                                                         int w, int h,
                                                         int scanlineStride,
                                                         int pixelStride,
                                                         int bandOffsets[],
                                                         Point location) {
        DataBuffer d;

        int size = scanlineStride * (h - 1) + // 前 (h - 1) 行
            pixelStride * w; // 最后一行

        switch (dataType) {
        case DataBuffer.TYPE_BYTE:
            d = new DataBufferByte(size);
            break;

        case DataBuffer.TYPE_USHORT:
            d = new DataBufferUShort(size);
            break;

        default:
            throw new IllegalArgumentException("Unsupported data type " +
                                                dataType);
        }

        return createInterleavedRaster(d, w, h, scanlineStride,
                                       pixelStride, bandOffsets, location);
    }

    /**
     * 基于指定的数据类型、宽度、高度和波段数创建一个 Raster。
     *
     * <p> Raster 的左上角由 location 参数给出。如果 location 为 null，则使用 (0, 0)。dataType 参数应为 DataBuffer 类中定义的枚举值之一。
     *
     * <p> 目前仅支持 TYPE_BYTE、TYPE_USHORT 和 TYPE_INT 数据类型。
     * @param dataType 用于存储样本的数据类型
     * @param w 图像数据的宽度（以像素为单位）
     * @param h 图像数据的高度（以像素为单位）
     * @param bands 波段数
     * @param location <code>Raster</code> 的左上角
     * @return 具有指定数据类型、宽度、高度和波段数的 WritableRaster 对象。
     * @throws RasterFormatException 如果 <code>w</code> 或 <code>h</code> 小于或等于零，或者计算 <code>location.x + w</code> 或 <code>location.y + h</code> 导致整数溢出
     * @throws ArrayIndexOutOfBoundsException 如果 <code>bands</code> 小于 1
     */
    public static WritableRaster createBandedRaster(int dataType,
                                                    int w, int h,
                                                    int bands,
                                                    Point location) {
        if (bands < 1) {
            throw new ArrayIndexOutOfBoundsException("Number of bands (" +
                                                     bands + ") must " +
                                                     "be greater than 0");
        }
        int[] bankIndices = new int[bands];
        int[] bandOffsets = new int[bands];
        for (int i = 0; i < bands; i++) {
            bankIndices[i] = i;
            bandOffsets[i] = 0;
        }

        return createBandedRaster(dataType, w, h, w,
                                  bankIndices, bandOffsets,
                                  location);
    }

    /**
     * 基于指定的数据类型、宽度、高度、扫描行步长、银行索引和波段偏移创建一个 Raster。波段数从 bankIndices.length 和 bandOffsets.length 推断得出，这两个长度必须相同。
     *
     * <p> Raster 的左上角由 location 参数给出。dataType 参数应为 DataBuffer 类中定义的枚举值之一。
     *
     * <p> 目前仅支持 TYPE_BYTE、TYPE_USHORT 和 TYPE_INT 数据类型。
     * @param dataType 用于存储样本的数据类型
     * @param w 图像数据的宽度（以像素为单位）
     * @param h 图像数据的高度（以像素为单位）
     * @param scanlineStride 图像数据的行步长
     * @param bankIndices 每个波段的银行索引
     * @param bandOffsets 所有波段的偏移
     * @param location <code>Raster</code> 的左上角
     * @return 具有指定数据类型、宽度、高度、扫描行步长、银行索引和波段偏移的 WritableRaster 对象。
     * @throws RasterFormatException 如果 <code>w</code> 或 <code>h</code> 小于或等于零，或者计算 <code>location.x + w</code> 或 <code>location.y + h</code> 导致整数溢出
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是支持的数据类型之一，即 <code>DataBuffer.TYPE_BYTE</code>、<code>DataBuffer.TYPE_USHORT</code> 或 <code>DataBuffer.TYPE_INT</code>
     * @throws ArrayIndexOutOfBoundsException 如果 <code>bankIndices</code> 或 <code>bandOffsets</code> 为 <code>null</code>
     */
    public static WritableRaster createBandedRaster(int dataType,
                                                    int w, int h,
                                                    int scanlineStride,
                                                    int bankIndices[],
                                                    int bandOffsets[],
                                                    Point location) {
        DataBuffer d;
        int bands = bandOffsets.length;


                    if (bankIndices == null) {
            throw new
                ArrayIndexOutOfBoundsException("Bank indices array is null");
        }
        if (bandOffsets == null) {
            throw new
                ArrayIndexOutOfBoundsException("Band offsets array is null");
        }

        // 确定银行数量和最大的带偏移
        int maxBank = bankIndices[0];
        int maxBandOff = bandOffsets[0];
        for (int i = 1; i < bands; i++) {
            if (bankIndices[i] > maxBank) {
                maxBank = bankIndices[i];
            }
            if (bandOffsets[i] > maxBandOff) {
                maxBandOff = bandOffsets[i];
            }
        }
        int banks = maxBank + 1;
        int size = maxBandOff +
            scanlineStride * (h - 1) + // 前 (h - 1) 行
            w; // 最后一行

        switch(dataType) {
        case DataBuffer.TYPE_BYTE:
            d = new DataBufferByte(size, banks);
            break;

        case DataBuffer.TYPE_USHORT:
            d = new DataBufferUShort(size, banks);
            break;

        case DataBuffer.TYPE_INT:
            d = new DataBufferInt(size, banks);
            break;

        default:
            throw new IllegalArgumentException("不支持的数据类型 " +
                                                dataType);
        }

        return createBandedRaster(d, w, h, scanlineStride,
                                  bankIndices, bandOffsets, location);
    }

    /**
     * 基于指定的数据类型、宽度、高度和带掩码创建一个基于 SinglePixelPackedSampleModel 的 Raster。
     * 带的数量从 bandMasks.length 推断得出。
     *
     * <p> Raster 的左上角由 location 参数给出。如果 location 为 null，则使用 (0, 0)。
     * dataType 参数应该是 DataBuffer 类中定义的枚举值之一。
     *
     * <p> 目前支持的数据类型有 TYPE_BYTE、TYPE_USHORT 和 TYPE_INT。
     * @param dataType  用于存储样本的数据类型
     * @param w         图像数据的宽度（以像素为单位）
     * @param h         图像数据的高度（以像素为单位）
     * @param bandMasks 每个带的数组
     * @param location  Raster 的左上角
     * @return 一个具有指定数据类型、宽度、高度和带掩码的 WritableRaster 对象。
     * @throws RasterFormatException 如果 <code>w</code> 或 <code>h</code>
     *         小于或等于零，或者计算 <code>location.x + w</code> 或
     *         <code>location.y + h</code> 导致整数溢出
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是
     *         支持的数据类型之一，即 <code>DataBuffer.TYPE_BYTE</code>、
     *         <code>DataBuffer.TYPE_USHORT</code> 或 <code>DataBuffer.TYPE_INT</code>
     */
    public static WritableRaster createPackedRaster(int dataType,
                                                    int w, int h,
                                                    int bandMasks[],
                                                    Point location) {
        DataBuffer d;

        switch(dataType) {
        case DataBuffer.TYPE_BYTE:
            d = new DataBufferByte(w*h);
            break;

        case DataBuffer.TYPE_USHORT:
            d = new DataBufferUShort(w*h);
            break;

        case DataBuffer.TYPE_INT:
            d = new DataBufferInt(w*h);
            break;

        default:
            throw new IllegalArgumentException("不支持的数据类型 " +
                                                dataType);
        }

        return createPackedRaster(d, w, h, w, bandMasks, location);
    }

    /**
     * 基于指定的数据类型、宽度、高度、带数和每带位数创建一个基于打包 SampleModel 的 Raster。
     * 如果带数为一，则 SampleModel 将是一个 MultiPixelPackedSampleModel。
     *
     * <p> 如果带数大于一，则 SampleModel 将是一个 SinglePixelPackedSampleModel，每个带具有 bitsPerBand 位。
     * 在任何情况下，都必须满足对应 SampleModel 对 dataType 和 bitsPerBand 的要求。
     *
     * <p> Raster 的左上角由 location 参数给出。如果 location 为 null，则使用 (0, 0)。
     * dataType 参数应该是 DataBuffer 类中定义的枚举值之一。
     *
     * <p> 目前支持的数据类型有 TYPE_BYTE、TYPE_USHORT 和 TYPE_INT。
     * @param dataType  用于存储样本的数据类型
     * @param w         图像数据的宽度（以像素为单位）
     * @param h         图像数据的高度（以像素为单位）
     * @param bands     带数
     * @param bitsPerBand 每带的位数
     * @param location  Raster 的左上角
     * @return 一个具有指定数据类型、宽度、高度、带数和每带位数的 WritableRaster 对象。
     * @throws RasterFormatException 如果 <code>w</code> 或 <code>h</code>
     *         小于或等于零，或者计算 <code>location.x + w</code> 或
     *         <code>location.y + h</code> 导致整数溢出
     * @throws IllegalArgumentException 如果 <code>bitsPerBand</code> 和 <code>bands</code> 的乘积
     *         大于 <code>dataType</code> 所持有的位数
     * @throws IllegalArgumentException 如果 <code>bitsPerBand</code> 或 <code>bands</code> 不大于零
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是
     *         支持的数据类型之一，即 <code>DataBuffer.TYPE_BYTE</code>、
     *         <code>DataBuffer.TYPE_USHORT</code> 或 <code>DataBuffer.TYPE_INT</code>
     */
    public static WritableRaster createPackedRaster(int dataType,
                                                    int w, int h,
                                                    int bands,
                                                    int bitsPerBand,
                                                    Point location) {
        DataBuffer d;

        if (bands <= 0) {
            throw new IllegalArgumentException("带数 ("+bands+
                                               ") 必须大于 0");
        }

        if (bitsPerBand <= 0) {
            throw new IllegalArgumentException("每带位数 ("+bitsPerBand+
                                               ") 必须大于 0");
        }

        if (bands != 1) {
            int[] masks = new int[bands];
            int mask = (1 << bitsPerBand) - 1;
            int shift = (bands-1)*bitsPerBand;

            /* 确保总掩码大小适合数据类型 */
            if (shift+bitsPerBand > DataBuffer.getDataTypeSize(dataType)) {
                throw new IllegalArgumentException("bitsPerBand("+
                                                   bitsPerBand+") * bands is "+
                                                   "大于数据类型大小。");
            }
            switch(dataType) {
            case DataBuffer.TYPE_BYTE:
            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_INT:
                break;
            default:
                throw new IllegalArgumentException("不支持的数据类型 " +
                                                    dataType);
            }

            for (int i = 0; i < bands; i++) {
                masks[i] = mask << shift;
                shift = shift - bitsPerBand;
            }

            return createPackedRaster(dataType, w, h, masks, location);
        }
        else {
            double fw = w;
            switch(dataType) {
            case DataBuffer.TYPE_BYTE:
                d = new DataBufferByte((int)(Math.ceil(fw/(8/bitsPerBand)))*h);
                break;

            case DataBuffer.TYPE_USHORT:
                d = new DataBufferUShort((int)(Math.ceil(fw/(16/bitsPerBand)))*h);
                break;

            case DataBuffer.TYPE_INT:
                d = new DataBufferInt((int)(Math.ceil(fw/(32/bitsPerBand)))*h);
                break;

            default:
                throw new IllegalArgumentException("不支持的数据类型 " +
                                                   dataType);
            }

            return createPackedRaster(d, w, h, bitsPerBand, location);
        }
    }

    /**
     * 基于指定的 DataBuffer、宽度、高度、扫描行步长、像素步长和带偏移创建一个基于 PixelInterleavedSampleModel 的 Raster。
     * 带数从 bandOffsets.length 推断得出。Raster 的左上角由 location 参数给出。如果 location 为 null，则使用 (0, 0)。
     * <p> 注意，不支持 <code>DataBuffer.TYPE_INT</code> 的交错 Raster。要创建一个 1 带的 <code>DataBuffer.TYPE_INT</code> Raster，请使用 Raster.createPackedRaster()。
     * @param dataBuffer 包含图像数据的 <code>DataBuffer</code>
     * @param w         图像数据的宽度（以像素为单位）
     * @param h         图像数据的高度（以像素为单位）
     * @param scanlineStride 图像数据的行步长
     * @param pixelStride 图像数据的像素步长
     * @param bandOffsets 所有带的偏移
     * @param location  Raster 的左上角
     * @return 一个具有指定 <code>DataBuffer</code>、宽度、高度、扫描行步长、像素步长和带偏移的 WritableRaster 对象。
     * @throws RasterFormatException 如果 <code>w</code> 或 <code>h</code>
     *         小于或等于零，或者计算 <code>location.x + w</code> 或
     *         <code>location.y + h</code> 导致整数溢出
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是
     *         支持的数据类型之一，即 <code>DataBuffer.TYPE_BYTE</code>、
     *         <code>DataBuffer.TYPE_USHORT</code>
     * @throws RasterFormatException 如果 <code>dataBuffer</code> 有多个银行。
     * @throws NullPointerException 如果 <code>dataBuffer</code> 为 null
     */
    public static WritableRaster createInterleavedRaster(DataBuffer dataBuffer,
                                                         int w, int h,
                                                         int scanlineStride,
                                                         int pixelStride,
                                                         int bandOffsets[],
                                                         Point location) {
        if (dataBuffer == null) {
            throw new NullPointerException("DataBuffer 不能为 null");
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        int dataType = dataBuffer.getDataType();

        PixelInterleavedSampleModel csm =
            new PixelInterleavedSampleModel(dataType, w, h,
                                            pixelStride,
                                            scanlineStride,
                                            bandOffsets);
        switch(dataType) {
        case DataBuffer.TYPE_BYTE:
            return new ByteInterleavedRaster(csm, dataBuffer, location);

        case DataBuffer.TYPE_USHORT:
            return new ShortInterleavedRaster(csm, dataBuffer, location);

        default:
            throw new IllegalArgumentException("不支持的数据类型 " +
                                                dataType);
        }
    }

    /**
     * 基于指定的 DataBuffer、宽度、高度、扫描行步长、银行索引和带偏移创建一个基于 BandedSampleModel 的 Raster。
     * 带数从 bankIndices.length 和 bandOffsets.length 推断得出，这两个长度必须相同。Raster 的左上角由 location 参数给出。如果 location 为 null，则使用 (0, 0)。
     * @param dataBuffer 包含图像数据的 <code>DataBuffer</code>
     * @param w         图像数据的宽度（以像素为单位）
     * @param h         图像数据的高度（以像素为单位）
     * @param scanlineStride 图像数据的行步长
     * @param bankIndices 每个带的银行索引
     * @param bandOffsets 所有带的偏移
     * @param location  Raster 的左上角
     * @return 一个具有指定 <code>DataBuffer</code>、宽度、高度、扫描行步长、银行索引和带偏移的 WritableRaster 对象。
     * @throws RasterFormatException 如果 <code>w</code> 或 <code>h</code>
     *         小于或等于零，或者计算 <code>location.x + w</code> 或
     *         <code>location.y + h</code> 导致整数溢出
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是
     *         支持的数据类型之一，即 <code>DataBuffer.TYPE_BYTE</code>、
     *         <code>DataBuffer.TYPE_USHORT</code> 或 <code>DataBuffer.TYPE_INT</code>
     * @throws NullPointerException 如果 <code>dataBuffer</code> 为 null
     */
    public static WritableRaster createBandedRaster(DataBuffer dataBuffer,
                                                    int w, int h,
                                                    int scanlineStride,
                                                    int bankIndices[],
                                                    int bandOffsets[],
                                                    Point location) {
        if (dataBuffer == null) {
            throw new NullPointerException("DataBuffer 不能为 null");
        }
        if (location == null) {
           location = new Point(0,0);
        }
        int dataType = dataBuffer.getDataType();

        int bands = bankIndices.length;
        if (bandOffsets.length != bands) {
            throw new IllegalArgumentException(
                                   "bankIndices.length != bandOffsets.length");
        }

        BandedSampleModel bsm =
            new BandedSampleModel(dataType, w, h,
                                  scanlineStride,
                                  bankIndices, bandOffsets);

        switch(dataType) {
        case DataBuffer.TYPE_BYTE:
            return new ByteBandedRaster(bsm, dataBuffer, location);

        case DataBuffer.TYPE_USHORT:
            return new ShortBandedRaster(bsm, dataBuffer, location);

        case DataBuffer.TYPE_INT:
            return new SunWritableRaster(bsm, dataBuffer, location);

        default:
            throw new IllegalArgumentException("不支持的数据类型 " +
                                                dataType);
        }
    }


                /**
     * 根据指定的 DataBuffer、宽度、高度、扫描线步幅和带宽掩码创建一个基于 SinglePixelPackedSampleModel 的 Raster。
     * 带宽的数量从 bandMasks.length 推断。 Raster 的左上角由 location 参数给出。如果 location 为 null，则使用 (0, 0)。
     * @param dataBuffer 包含图像数据的 <code>DataBuffer</code>
     * @param w 图像数据的宽度（以像素为单位）
     * @param h 图像数据的高度（以像素为单位）
     * @param scanlineStride 图像数据的行步幅
     * @param bandMasks 包含每个带宽的数组
     * @param location <code>Raster</code> 的左上角
     * @return 一个具有指定 <code>DataBuffer</code>、宽度、高度、扫描线步幅和带宽掩码的 WritableRaster 对象。
     * @throws RasterFormatException 如果 <code>w</code> 或 <code>h</code> 小于或等于零，或者计算 <code>location.x + w</code> 或
     *         <code>location.y + h</code> 导致整数溢出
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是支持的数据类型之一，这些类型是
     *         <code>DataBuffer.TYPE_BYTE</code>、<code>DataBuffer.TYPE_USHORT</code>
     *         或 <code>DataBuffer.TYPE_INT</code>
     * @throws RasterFormatException 如果 <code>dataBuffer</code> 有多个银行。
     * @throws NullPointerException 如果 <code>dataBuffer</code> 为 null
     */
    public static WritableRaster createPackedRaster(DataBuffer dataBuffer,
                                                    int w, int h,
                                                    int scanlineStride,
                                                    int bandMasks[],
                                                    Point location) {
        if (dataBuffer == null) {
            throw new NullPointerException("DataBuffer cannot be null");
        }
        if (location == null) {
           location = new Point(0,0);
        }
        int dataType = dataBuffer.getDataType();

        SinglePixelPackedSampleModel sppsm =
            new SinglePixelPackedSampleModel(dataType, w, h, scanlineStride,
                                             bandMasks);

        switch(dataType) {
        case DataBuffer.TYPE_BYTE:
            return new ByteInterleavedRaster(sppsm, dataBuffer, location);

        case DataBuffer.TYPE_USHORT:
            return new ShortInterleavedRaster(sppsm, dataBuffer, location);

        case DataBuffer.TYPE_INT:
            return new IntegerInterleavedRaster(sppsm, dataBuffer, location);

        default:
            throw new IllegalArgumentException("Unsupported data type " +
                                                dataType);
        }
    }

    /**
     * 根据指定的 DataBuffer、宽度、高度和每个像素的位数创建一个基于 MultiPixelPackedSampleModel 的 Raster。
     * Raster 的左上角由 location 参数给出。如果 location 为 null，则使用 (0, 0)。
     * @param dataBuffer 包含图像数据的 <code>DataBuffer</code>
     * @param w 图像数据的宽度（以像素为单位）
     * @param h 图像数据的高度（以像素为单位）
     * @param bitsPerPixel 每个像素的位数
     * @param location <code>Raster</code> 的左上角
     * @return 一个具有指定 <code>DataBuffer</code>、宽度、高度和每个像素位数的 WritableRaster 对象。
     * @throws RasterFormatException 如果 <code>w</code> 或 <code>h</code> 小于或等于零，或者计算 <code>location.x + w</code> 或
     *         <code>location.y + h</code> 导致整数溢出
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是支持的数据类型之一，这些类型是
     *         <code>DataBuffer.TYPE_BYTE</code>、<code>DataBuffer.TYPE_USHORT</code>
     *         或 <code>DataBuffer.TYPE_INT</code>
     * @throws RasterFormatException 如果 <code>dataBuffer</code> 有多个银行。
     * @throws NullPointerException 如果 <code>dataBuffer</code> 为 null
     */
    public static WritableRaster createPackedRaster(DataBuffer dataBuffer,
                                                    int w, int h,
                                                    int bitsPerPixel,
                                                    Point location) {
        if (dataBuffer == null) {
            throw new NullPointerException("DataBuffer cannot be null");
        }
        if (location == null) {
           location = new Point(0,0);
        }
        int dataType = dataBuffer.getDataType();

        if (dataType != DataBuffer.TYPE_BYTE &&
            dataType != DataBuffer.TYPE_USHORT &&
            dataType != DataBuffer.TYPE_INT) {
            throw new IllegalArgumentException("Unsupported data type " +
                                               dataType);
        }

        if (dataBuffer.getNumBanks() != 1) {
            throw new
                RasterFormatException("DataBuffer for packed Rasters"+
                                      " must only have 1 bank.");
        }

        MultiPixelPackedSampleModel mppsm =
                new MultiPixelPackedSampleModel(dataType, w, h, bitsPerPixel);

        if (dataType == DataBuffer.TYPE_BYTE &&
            (bitsPerPixel == 1 || bitsPerPixel == 2 || bitsPerPixel == 4)) {
            return new BytePackedRaster(mppsm, dataBuffer, location);
        } else {
            return new SunWritableRaster(mppsm, dataBuffer, location);
        }
    }


    /**
     * 使用指定的 SampleModel 和 DataBuffer 创建一个 Raster。
     * Raster 的左上角由 location 参数给出。如果 location 为 null，则使用 (0, 0)。
     * @param sm 指定的 <code>SampleModel</code>
     * @param db 指定的 <code>DataBuffer</code>
     * @param location <code>Raster</code> 的左上角
     * @return 一个具有指定 <code>SampleModel</code>、<code>DataBuffer</code> 和位置的 <code>Raster</code>。
     * @throws RasterFormatException 如果计算 <code>location.x + sm.getWidth()</code> 或
     *         <code>location.y + sm.getHeight()</code> 导致整数溢出
     * @throws RasterFormatException 如果 <code>db</code> 有多个银行且 <code>sm</code> 是
     *         PixelInterleavedSampleModel、SinglePixelPackedSampleModel 或 MultiPixelPackedSampleModel。
     * @throws NullPointerException 如果 SampleModel 或 DataBuffer 为 null
     */
    public static Raster createRaster(SampleModel sm,
                                      DataBuffer db,
                                      Point location) {
        if ((sm == null) || (db == null)) {
            throw new NullPointerException("SampleModel and DataBuffer cannot be null");
        }

        if (location == null) {
           location = new Point(0,0);
        }
        int dataType = sm.getDataType();

        if (sm instanceof PixelInterleavedSampleModel) {
            switch(dataType) {
                case DataBuffer.TYPE_BYTE:
                    return new ByteInterleavedRaster(sm, db, location);

                case DataBuffer.TYPE_USHORT:
                    return new ShortInterleavedRaster(sm, db, location);
            }
        } else if (sm instanceof SinglePixelPackedSampleModel) {
            switch(dataType) {
                case DataBuffer.TYPE_BYTE:
                    return new ByteInterleavedRaster(sm, db, location);

                case DataBuffer.TYPE_USHORT:
                    return new ShortInterleavedRaster(sm, db, location);

                case DataBuffer.TYPE_INT:
                    return new IntegerInterleavedRaster(sm, db, location);
            }
        } else if (sm instanceof MultiPixelPackedSampleModel &&
                   dataType == DataBuffer.TYPE_BYTE &&
                   sm.getSampleSize(0) < 8) {
            return new BytePackedRaster(sm, db, location);
        }

        // 我们无法做任何特殊处理 - 执行通用操作

        return new Raster(sm,db,location);
    }

    /**
     * 使用指定的 SampleModel 创建一个 WritableRaster。
     * Raster 的左上角由 location 参数给出。如果 location 为 null，则使用 (0, 0)。
     * @param sm 指定的 <code>SampleModel</code>
     * @param location <code>WritableRaster</code> 的左上角
     * @return 一个具有指定 <code>SampleModel</code> 和位置的 <code>WritableRaster</code>。
     * @throws RasterFormatException 如果计算 <code>location.x + sm.getWidth()</code> 或
     *         <code>location.y + sm.getHeight()</code> 导致整数溢出
     */
    public static WritableRaster createWritableRaster(SampleModel sm,
                                                      Point location) {
        if (location == null) {
           location = new Point(0,0);
        }

        return createWritableRaster(sm, sm.createDataBuffer(), location);
    }

    /**
     * 使用指定的 SampleModel 和 DataBuffer 创建一个 WritableRaster。
     * Raster 的左上角由 location 参数给出。如果 location 为 null，则使用 (0, 0)。
     * @param sm 指定的 <code>SampleModel</code>
     * @param db 指定的 <code>DataBuffer</code>
     * @param location <code>WritableRaster</code> 的左上角
     * @return 一个具有指定 <code>SampleModel</code>、<code>DataBuffer</code> 和位置的 <code>WritableRaster</code>。
     * @throws RasterFormatException 如果计算 <code>location.x + sm.getWidth()</code> 或
     *         <code>location.y + sm.getHeight()</code> 导致整数溢出
     * @throws RasterFormatException 如果 <code>db</code> 有多个银行且 <code>sm</code> 是
     *         PixelInterleavedSampleModel、SinglePixelPackedSampleModel 或 MultiPixelPackedSampleModel。
     * @throws NullPointerException 如果 SampleModel 或 DataBuffer 为 null
     */
    public static WritableRaster createWritableRaster(SampleModel sm,
                                                      DataBuffer db,
                                                      Point location) {
        if ((sm == null) || (db == null)) {
            throw new NullPointerException("SampleModel and DataBuffer cannot be null");
        }
        if (location == null) {
           location = new Point(0,0);
        }

        int dataType = sm.getDataType();

        if (sm instanceof PixelInterleavedSampleModel) {
            switch(dataType) {
                case DataBuffer.TYPE_BYTE:
                    return new ByteInterleavedRaster(sm, db, location);

                case DataBuffer.TYPE_USHORT:
                    return new ShortInterleavedRaster(sm, db, location);
            }
        } else if (sm instanceof SinglePixelPackedSampleModel) {
            switch(dataType) {
                case DataBuffer.TYPE_BYTE:
                    return new ByteInterleavedRaster(sm, db, location);

                case DataBuffer.TYPE_USHORT:
                    return new ShortInterleavedRaster(sm, db, location);

                case DataBuffer.TYPE_INT:
                    return new IntegerInterleavedRaster(sm, db, location);
            }
        } else if (sm instanceof MultiPixelPackedSampleModel &&
                   dataType == DataBuffer.TYPE_BYTE &&
                   sm.getSampleSize(0) < 8) {
            return new BytePackedRaster(sm, db, location);
        }

        // 我们无法做任何特殊处理 - 执行通用操作

        return new SunWritableRaster(sm,db,location);
    }

    /**
     * 使用给定的 SampleModel 构造一个 Raster。Raster 的左上角是 origin，其大小与 SampleModel 相同。
     * 自动创建一个足够大的 DataBuffer 来描述 Raster。
     * @param sampleModel 指定布局的 SampleModel
     * @param origin 指定的原点
     * @throws RasterFormatException 如果计算 <code>origin.x + sampleModel.getWidth()</code> 或
     *         <code>origin.y + sampleModel.getHeight()</code> 导致整数溢出
     * @throws NullPointerException 如果 <code>sampleModel</code> 或 <code>origin</code> 为 null
     */
    protected Raster(SampleModel sampleModel,
                     Point origin) {
        this(sampleModel,
             sampleModel.createDataBuffer(),
             new Rectangle(origin.x,
                           origin.y,
                           sampleModel.getWidth(),
                           sampleModel.getHeight()),
             origin,
             null);
    }

    /**
     * 使用给定的 SampleModel 和 DataBuffer 构造一个 Raster。Raster 的左上角是 origin，其大小与 SampleModel 相同。
     * DataBuffer 未初始化，必须与 SampleModel 兼容。
     * @param sampleModel 指定布局的 SampleModel
     * @param dataBuffer 包含图像数据的 DataBuffer
     * @param origin 指定的原点
     * @throws RasterFormatException 如果计算 <code>origin.x + sampleModel.getWidth()</code> 或
     *         <code>origin.y + sampleModel.getHeight()</code> 导致整数溢出
     * @throws NullPointerException 如果 <code>sampleModel</code> 或 <code>origin</code> 为 null
     */
    protected Raster(SampleModel sampleModel,
                     DataBuffer dataBuffer,
                     Point origin) {
        this(sampleModel,
             dataBuffer,
             new Rectangle(origin.x,
                           origin.y,
                           sampleModel.getWidth(),
                           sampleModel.getHeight()),
             origin,
             null);
    }

    /**
     * 使用给定的 SampleModel、DataBuffer 和父 Raster 构造一个 Raster。aRegion 指定新 Raster 的边界矩形。
     * 当转换到基 Raster 的坐标系中时，aRegion 必须包含在基 Raster 中。
     * (基 Raster 是没有父 Raster 的 Raster 的祖先。) sampleModelTranslate 指定新 Raster 的 sampleModelTranslateX 和
     * sampleModelTranslateY 值。
     *
     * 注意，通常应由其他构造函数或创建方法调用此构造函数，不应直接使用。
     * @param sampleModel 指定布局的 SampleModel
     * @param dataBuffer 包含图像数据的 DataBuffer
     * @param aRegion 指定图像区域的 Rectangle
     * @param sampleModelTranslate 指定从 SampleModel 到 Raster 坐标的转换的 Point
     * @param parent 此 Raster 的父 Raster（如果有）
     * @throws NullPointerException 如果 <code>sampleModel</code>、<code>dataBuffer</code>、<code>aRegion</code> 或
     *         <code>sampleModelTranslate</code> 为 null
     * @throws RasterFormatException 如果 <code>aRegion</code> 的宽度或高度小于或等于零，或者计算 <code>aRegion.x + aRegion.width</code> 或
     *         <code>aRegion.y + aRegion.height</code> 导致整数溢出
     */
    protected Raster(SampleModel sampleModel,
                     DataBuffer dataBuffer,
                     Rectangle aRegion,
                     Point sampleModelTranslate,
                     Raster parent) {


                    if ((sampleModel == null) || (dataBuffer == null) ||
            (aRegion == null) || (sampleModelTranslate == null)) {
            throw new NullPointerException("SampleModel, dataBuffer, aRegion and " +
                                           "sampleModelTranslate 不能为 null");
        }
       this.sampleModel = sampleModel;
       this.dataBuffer = dataBuffer;
       minX = aRegion.x;
       minY = aRegion.y;
       width = aRegion.width;
       height = aRegion.height;
       if (width <= 0 || height <= 0) {
           throw new RasterFormatException("负数或零的 " +
               ((width <= 0) ? "宽度" : "高度"));
       }
       if ((minX + width) < minX) {
           throw new RasterFormatException(
               "栅格 X 坐标的溢出条件");
       }
       if ((minY + height) < minY) {
           throw new RasterFormatException(
               "栅格 Y 坐标的溢出条件");
       }

       sampleModelTranslateX = sampleModelTranslate.x;
       sampleModelTranslateY = sampleModelTranslate.y;

       numBands = sampleModel.getNumBands();
       numDataElements = sampleModel.getNumDataElements();
       this.parent = parent;
    }


    /**
     * 返回此栅格的父栅格（如果有）或 null。
     * @return 父栅格或 <code>null</code>。
     */
    public Raster getParent() {
        return parent;
    }

    /**
     * 返回从 SampleModel 的坐标系到栅格的坐标系的 X 转换。要将像素的 X 坐标从栅格的坐标系转换为 SampleModel 的坐标系，必须减去此值。
     * @return 从栅格的 SampleModel 的坐标空间到栅格的坐标空间的 X 转换。
     */
    final public int getSampleModelTranslateX() {
        return sampleModelTranslateX;
    }

    /**
     * 返回从 SampleModel 的坐标系到栅格的坐标系的 Y 转换。要将像素的 Y 坐标从栅格的坐标系转换为 SampleModel 的坐标系，必须减去此值。
     * @return 从栅格的 SampleModel 的坐标空间到栅格的坐标空间的 Y 转换。
     */
    final public int getSampleModelTranslateY() {
        return sampleModelTranslateY;
    }

    /**
     * 创建一个与此栅格具有相同大小、相同 SampleModel 和新初始化的 DataBuffer 的兼容 WritableRaster。
     * @return 一个与此栅格具有相同样本模型和新数据缓冲区的兼容 <code>WritableRaster</code>。
     */
    public WritableRaster createCompatibleWritableRaster() {
        return new SunWritableRaster(sampleModel, new Point(0,0));
    }

    /**
     * 创建一个具有指定大小、新的 SampleModel 和新初始化的 DataBuffer 的兼容 WritableRaster。
     * @param w 指定的新 <code>WritableRaster</code> 的宽度
     * @param h 指定的新 <code>WritableRaster</code> 的高度
     * @return 一个具有指定大小和新样本模型及数据缓冲区的兼容 <code>WritableRaster</code>。
     * @exception RasterFormatException 如果宽度或高度小于或等于零。
     */
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        if (w <= 0 || h <=0) {
            throw new RasterFormatException("负数的 " +
                                          ((w <= 0) ? "宽度" : "高度"));
        }

        SampleModel sm = sampleModel.createCompatibleSampleModel(w,h);

        return new SunWritableRaster(sm, new Point(0,0));
    }

    /**
     * 创建一个具有由 rect 指定的大小和位置、新的 SampleModel 和新初始化的 DataBuffer 的兼容 WritableRaster。
     * @param rect 一个 <code>Rectangle</code>，指定 <code>WritableRaster</code> 的大小和位置
     * @return 一个具有指定大小和位置以及新样本模型和数据缓冲区的兼容 <code>WritableRaster</code>。
     * @throws RasterFormatException 如果 <code>rect</code> 的宽度或高度小于或等于零，或者计算 <code>rect.x + rect.width</code> 或
     *         <code>rect.y + rect.height</code> 导致整数溢出
     * @throws NullPointerException 如果 <code>rect</code> 为 null
     */
    public WritableRaster createCompatibleWritableRaster(Rectangle rect) {
        if (rect == null) {
            throw new NullPointerException("Rect 不能为 null");
        }
        return createCompatibleWritableRaster(rect.x, rect.y,
                                              rect.width, rect.height);
    }

    /**
     * 创建一个具有指定位置 (minX, minY) 和大小 (width, height)、新的 SampleModel 和新初始化的 DataBuffer 的兼容 WritableRaster。
     * @param x 新 <code>WritableRaster</code> 的左上角的 X 坐标
     * @param y 新 <code>WritableRaster</code> 的左上角的 Y 坐标
     * @param w 指定的新 <code>WritableRaster</code> 的宽度
     * @param h 指定的新 <code>WritableRaster</code> 的高度
     * @return 一个具有指定大小和位置以及新样本模型和数据缓冲区的兼容 <code>WritableRaster</code>。
     * @throws RasterFormatException 如果 <code>w</code> 或 <code>h</code>
     *         小于或等于零，或者计算 <code>x + w</code> 或
     *         <code>y + h</code> 导致整数溢出
     */
    public WritableRaster createCompatibleWritableRaster(int x, int y,
                                                         int w, int h) {
        WritableRaster ret = createCompatibleWritableRaster(w, h);
        return ret.createWritableChild(0,0,w,h,x,y,null);
    }

    /**
     * 创建一个与此栅格具有相同大小、SampleModel 和 DataBuffer 但位置不同的栅格。新栅格将具有对当前栅格的引用，可通过其 getParent() 方法访问。
     *
     * @param childMinX 新 <code>Raster</code> 的左上角的 X 坐标
     * @param childMinY 新 <code>Raster</code> 的左上角的 Y 坐标
     * @return 一个与此 <code>Raster</code> 具有相同大小、SampleModel 和 DataBuffer 但具有指定位置的新 <code>Raster</code>。
     * @throws RasterFormatException 如果计算 <code>childMinX + this.getWidth()</code> 或
     *         <code>childMinY + this.getHeight()</code> 导致整数溢出
     */
    public Raster createTranslatedChild(int childMinX, int childMinY) {
        return createChild(minX,minY,width,height,
                           childMinX,childMinY,null);
    }

    /**
     * 返回一个与当前栅格共享全部或部分 DataBuffer 的新栅格。新栅格将具有对当前栅格的引用，可通过其 getParent() 方法访问。
     *
     * <p> parentX, parentY, width 和 height 参数在当前栅格的坐标空间中形成一个矩形，表示要共享的像素区域。如果此矩形不在当前栅格的边界内，将抛出错误。
     *
     * <p> 新栅格还可以被翻译到与当前栅格不同的平面坐标系。childMinX 和 childMinY 参数给出了返回的栅格的左上角的新 (x, y) 坐标；新栅格中的 (childMinX, childMinY) 坐标将映射到当前栅格中的 (parentX, parentY) 坐标。
     *
     * <p> 新栅格可以被定义为仅包含当前栅格的部分波段，可能重新排序，通过 bandList 参数实现。如果 bandList 为 null，则认为包括当前栅格的所有波段，按当前顺序。
     *
     * <p> 要创建一个包含当前栅格的子区域但共享其坐标系和波段的新栅格，应调用此方法，使 childMinX 等于 parentX，childMinY 等于 parentY，bandList 等于 null。
     *
     * @param parentX 当前栅格坐标中的左上角的 X 坐标
     * @param parentY 当前栅格坐标中的左上角的 Y 坐标
     * @param width   从 (parentX, parentY) 开始的区域的宽度
     * @param height  从 (parentX, parentY) 开始的区域的高度。
     * @param childMinX 返回的栅格的左上角的 X 坐标
     * @param childMinY 返回的栅格的左上角的 Y 坐标
     * @param bandList  波段索引数组，或 null 以使用所有波段
     * @return 一个新的 <code>Raster</code>。
     * @exception RasterFormatException 如果指定的子区域在栅格边界之外。
     * @throws RasterFormatException 如果 <code>width</code> 或
     *         <code>height</code>
     *         小于或等于零，或者计算 <code>parentX + width</code>、<code>parentY + height</code>、
     *         <code>childMinX + width</code> 或
     *         <code>childMinY + height</code> 导致整数溢出
     */
    public Raster createChild(int parentX, int parentY,
                              int width, int height,
                              int childMinX, int childMinY,
                              int bandList[]) {
        if (parentX < this.minX) {
            throw new RasterFormatException("parentX 在栅格之外");
        }
        if (parentY < this.minY) {
            throw new RasterFormatException("parentY 在栅格之外");
        }
        if ((parentX + width < parentX) ||
            (parentX + width > this.width + this.minX)) {
            throw new RasterFormatException("(parentX + width) 在栅格之外");
        }
        if ((parentY + height < parentY) ||
            (parentY + height > this.height + this.minY)) {
            throw new RasterFormatException("(parentY + height) 在栅格之外");
        }

        SampleModel subSampleModel;
        // 注意：子栅格的 SampleModel 应具有与父栅格相同的宽度和高度，因为它表示像素数据的物理布局。子栅格的宽度和高度表示像素数据的“虚拟”视图，因此可能与 SampleModel 的宽度和高度不同。
        if (bandList == null) {
            subSampleModel = sampleModel;
        } else {
            subSampleModel = sampleModel.createSubsetSampleModel(bandList);
        }

        int deltaX = childMinX - parentX;
        int deltaY = childMinY - parentY;

        return new Raster(subSampleModel, getDataBuffer(),
                          new Rectangle(childMinX, childMinY, width, height),
                          new Point(sampleModelTranslateX + deltaX,
                                    sampleModelTranslateY + deltaY), this);
    }

    /**
     * 返回此栅格的边界矩形。此函数返回与 getMinX/MinY/Width/Height 相同的信息。
     * @return 此 <code>Raster</code> 的边界框。
     */
    public Rectangle getBounds() {
        return new Rectangle(minX, minY, width, height);
    }

    /** 返回栅格的最小有效 X 坐标。
     *  @return 此 <code>Raster</code> 的最小 x 坐标。
     */
    final public int getMinX() {
        return minX;
    }

    /** 返回栅格的最小有效 Y 坐标。
     *  @return 此 <code>Raster</code> 的最小 y 坐标。
     */
    final public int getMinY() {
        return minY;
    }

    /** 返回栅格的像素宽度。
     *  @return 此 <code>Raster</code> 的宽度。
     */
    final public int getWidth() {
        return width;
    }

    /** 返回栅格的像素高度。
     *  @return 此 <code>Raster</code> 的高度。
     */
    final public int getHeight() {
        return height;
    }

    /** 返回此栅格的波段数（每个像素的样本数）。
     *  @return 此 <code>Raster</code> 的波段数。
     */
    final public int getNumBands() {
        return numBands;
    }

    /**
     *  返回通过 getDataElements 和 setDataElements 方法传输一个像素所需的数据元素数量。当通过这些方法传输像素时，它们可能以打包或未打包的格式传输，具体取决于底层 SampleModel 的实现。使用这些方法，像素作为 getNumDataElements() 个元素的数组传输，元素类型由 getTransferType() 给出。传输类型可能与 DataBuffer 的存储数据类型相同或不同。
     *  @return 数据元素的数量。
     */
    final public int getNumDataElements() {
        return sampleModel.getNumDataElements();
    }

    /**
     *  返回通过 getDataElements 和 setDataElements 方法传输像素时使用的传输类型。当通过这些方法传输像素时，它们可能以打包或未打包的格式传输，具体取决于底层 SampleModel 的实现。使用这些方法，像素作为 getNumDataElements() 个元素的数组传输，元素类型由 getTransferType() 给出。传输类型可能与 DataBuffer 的存储数据类型相同或不同。传输类型将是 DataBuffer 中定义的类型之一。
     *  @return 此传输类型。
     */
    final public int getTransferType() {
        return sampleModel.getTransferType();
    }

    /** 返回与此栅格关联的 DataBuffer。
     *  @return 此 <code>Raster</code> 的 <code>DataBuffer</code>。
     */
    public DataBuffer getDataBuffer() {
        return dataBuffer;
    }

    /** 返回描述图像数据布局的 SampleModel。
     *  @return 此 <code>Raster</code> 的 <code>SampleModel</code>。
     */
    public SampleModel getSampleModel() {
        return sampleModel;
    }

    /**
     * 以 TransferType 类型的原始数组形式返回单个像素的数据。对于 Java 2D API 支持的图像数据，这将是 DataBuffer.TYPE_BYTE、DataBuffer.TYPE_USHORT、
     * DataBuffer.TYPE_INT、DataBuffer.TYPE_SHORT、DataBuffer.TYPE_FLOAT 或 DataBuffer.TYPE_DOUBLE 之一。数据可能以打包格式返回，从而提高数据传输的效率。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。但是，不保证显式边界检查。
     * 如果输入对象非 null 且引用的不是 TransferType 类型的数组，将抛出 ClassCastException。
     * @see java.awt.image.SampleModel#getDataElements(int, int, Object, DataBuffer)
     * @param x        像素位置的 X 坐标
     * @param y        像素位置的 Y 坐标
     * @param outData  由 getTransferType() 定义的类型和长度为 getNumDataElements() 的数组的对象引用。如果为 null，将分配适当类型和大小的数组
     * @return         由 getTransferType() 定义的类型且包含请求的像素数据的数组的对象引用。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 outData 太小无法容纳输出。
     */
    public Object getDataElements(int x, int y, Object outData) {
        return sampleModel.getDataElements(x - sampleModelTranslateX,
                                           y - sampleModelTranslateY,
                                           outData, dataBuffer);
    }


                /**
     * 返回指定像素矩形的像素数据，以类型为 TransferType 的原始数组形式。
     * 对于 Java 2D API 支持的图像数据，这将是 DataBuffer.TYPE_BYTE、DataBuffer.TYPE_USHORT、
     * DataBuffer.TYPE_INT、DataBuffer.TYPE_SHORT、DataBuffer.TYPE_FLOAT 或 DataBuffer.TYPE_DOUBLE 之一。
     * 数据可以以打包格式返回，从而提高数据传输的效率。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。但是，不保证显式边界检查。
     * 如果输入对象非空且引用的不是 TransferType 的数组，将抛出 ClassCastException。
     * @see java.awt.image.SampleModel#getDataElements(int, int, int, int, Object, DataBuffer)
     * @param x    像素位置的 X 坐标
     * @param y    像素位置的 Y 坐标
     * @param w    像素矩形的宽度
     * @param h   像素矩形的高度
     * @param outData  一个由 getTransferType() 定义的类型和长度为 w*h*getNumDataElements() 的数组的引用。
     *                 如果为 null，将分配一个适当类型和大小的数组。
     * @return         一个由 getTransferType() 定义的类型和包含请求的像素数据的数组的引用。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 outData 太小而无法容纳输出。
     */
    public Object getDataElements(int x, int y, int w, int h, Object outData) {
        return sampleModel.getDataElements(x - sampleModelTranslateX,
                                           y - sampleModelTranslateY,
                                           w, h, outData, dataBuffer);
    }

    /**
     * 返回指定像素的样本，以 int 数组形式。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。但是，不保证显式边界检查。
     * @param x 像素位置的 X 坐标
     * @param y 像素位置的 Y 坐标
     * @param iArray 一个可选预分配的 int 数组
     * @return 指定像素的样本。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 iArray 太小而无法容纳输出。
     */
    public int[] getPixel(int x, int y, int iArray[]) {
        return sampleModel.getPixel(x - sampleModelTranslateX,
                                    y - sampleModelTranslateY,
                                    iArray, dataBuffer);
    }

    /**
     * 返回指定像素的样本，以 float 数组形式。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。但是，不保证显式边界检查。
     * @param x 像素位置的 X 坐标
     * @param y 像素位置的 Y 坐标
     * @param fArray 一个可选预分配的 float 数组
     * @return 指定像素的样本。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 fArray 太小而无法容纳输出。
     */
    public float[] getPixel(int x, int y, float fArray[]) {
        return sampleModel.getPixel(x - sampleModelTranslateX,
                                    y - sampleModelTranslateY,
                                    fArray, dataBuffer);
    }

    /**
     * 返回指定像素的样本，以 double 数组形式。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。但是，不保证显式边界检查。
     * @param x 像素位置的 X 坐标
     * @param y 像素位置的 Y 坐标
     * @param dArray 一个可选预分配的 double 数组
     * @return 指定像素的样本。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 dArray 太小而无法容纳输出。
     */
    public double[] getPixel(int x, int y, double dArray[]) {
        return sampleModel.getPixel(x - sampleModelTranslateX,
                                    y - sampleModelTranslateY,
                                    dArray, dataBuffer);
    }

    /**
     * 返回一个包含矩形像素区域内所有样本的 int 数组，每个数组元素一个样本。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。但是，不保证显式边界检查。
     * @param x      像素区域左上角的 X 坐标
     * @param y      像素区域左上角的 Y 坐标
     * @param w      像素矩形的宽度
     * @param h      像素矩形的高度
     * @param iArray 一个可选预分配的 int 数组
     * @return 指定矩形像素区域的样本。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 iArray 太小而无法容纳输出。
     */
    public int[] getPixels(int x, int y, int w, int h, int iArray[]) {
        return sampleModel.getPixels(x - sampleModelTranslateX,
                                     y - sampleModelTranslateY, w, h,
                                     iArray, dataBuffer);
    }

    /**
     * 返回一个包含矩形像素区域内所有样本的 float 数组，每个数组元素一个样本。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。但是，不保证显式边界检查。
     * @param x        像素位置的 X 坐标
     * @param y        像素位置的 Y 坐标
     * @param w        像素矩形的宽度
     * @param h        像素矩形的高度
     * @param fArray   一个可选预分配的 float 数组
     * @return 指定矩形像素区域的样本。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 fArray 太小而无法容纳输出。
     */
    public float[] getPixels(int x, int y, int w, int h,
                             float fArray[]) {
        return sampleModel.getPixels(x - sampleModelTranslateX,
                                     y - sampleModelTranslateY, w, h,
                                     fArray, dataBuffer);
    }

    /**
     * 返回一个包含矩形像素区域内所有样本的 double 数组，每个数组元素一个样本。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。但是，不保证显式边界检查。
     * @param x        像素区域左上角的 X 坐标
     * @param y        像素区域左上角的 Y 坐标
     * @param w        像素矩形的宽度
     * @param h        像素矩形的高度
     * @param dArray   一个可选预分配的 double 数组
     * @return 指定矩形像素区域的样本。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标不在范围内，或者 dArray 太小而无法容纳输出。
     */
    public double[] getPixels(int x, int y, int w, int h,
                              double dArray[]) {
        return sampleModel.getPixels(x - sampleModelTranslateX,
                                     y - sampleModelTranslateY,
                                     w, h, dArray, dataBuffer);
    }


    /**
     * 返回位于 (x,y) 的像素在指定波段的样本，以 int 形式。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。但是，不保证显式边界检查。
     * @param x        像素位置的 X 坐标
     * @param y        像素位置的 Y 坐标
     * @param b        要返回的波段
     * @return 指定坐标处像素在指定波段的样本。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引不在范围内。
     */
    public int getSample(int x, int y, int b) {
        return sampleModel.getSample(x - sampleModelTranslateX,
                                     y - sampleModelTranslateY, b,
                                     dataBuffer);
    }

    /**
     * 返回位于 (x,y) 的像素在指定波段的样本，以 float 形式。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。但是，不保证显式边界检查。
     * @param x        像素位置的 X 坐标
     * @param y        像素位置的 Y 坐标
     * @param b        要返回的波段
     * @return 指定坐标处像素在指定波段的样本。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引不在范围内。
     */
    public float getSampleFloat(int x, int y, int b) {
        return sampleModel.getSampleFloat(x - sampleModelTranslateX,
                                          y - sampleModelTranslateY, b,
                                          dataBuffer);
    }

    /**
     * 返回位于 (x,y) 的像素在指定波段的样本，以 double 形式。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。但是，不保证显式边界检查。
     * @param x        像素位置的 X 坐标
     * @param y        像素位置的 Y 坐标
     * @param b        要返回的波段
     * @return 指定坐标处像素在指定波段的样本。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引不在范围内。
     */
    public double getSampleDouble(int x, int y, int b) {
        return sampleModel.getSampleDouble(x - sampleModelTranslateX,
                                           y - sampleModelTranslateY,
                                           b, dataBuffer);
    }

    /**
     * 返回指定矩形像素区域内指定波段的样本，以 int 数组形式，每个数组元素一个样本。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。但是，不保证显式边界检查。
     * @param x        像素区域左上角的 X 坐标
     * @param y        像素区域左上角的 Y 坐标
     * @param w        像素矩形的宽度
     * @param h        像素矩形的高度
     * @param b        要返回的波段
     * @param iArray   一个可选预分配的 int 数组
     * @return 指定矩形像素区域内指定波段的样本。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引不在范围内，或者 iArray 太小而无法容纳输出。
     */
    public int[] getSamples(int x, int y, int w, int h, int b,
                            int iArray[]) {
        return sampleModel.getSamples(x - sampleModelTranslateX,
                                      y - sampleModelTranslateY,
                                      w, h, b, iArray,
                                      dataBuffer);
    }

    /**
     * 返回指定矩形像素区域内指定波段的样本，以 float 数组形式，每个数组元素一个样本。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。但是，不保证显式边界检查。
     * @param x        像素区域左上角的 X 坐标
     * @param y        像素区域左上角的 Y 坐标
     * @param w        像素矩形的宽度
     * @param h        像素矩形的高度
     * @param b        要返回的波段
     * @param fArray   一个可选预分配的 float 数组
     * @return 指定矩形像素区域内指定波段的样本。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引不在范围内，或者 fArray 太小而无法容纳输出。
     */
    public float[] getSamples(int x, int y, int w, int h, int b,
                              float fArray[]) {
        return sampleModel.getSamples(x - sampleModelTranslateX,
                                      y - sampleModelTranslateY,
                                      w, h, b, fArray, dataBuffer);
    }

    /**
     * 返回指定矩形像素区域内指定波段的样本，以 double 数组形式，每个数组元素一个样本。
     * 如果坐标不在范围内，可能会抛出 ArrayIndexOutOfBoundsException。但是，不保证显式边界检查。
     * @param x        像素区域左上角的 X 坐标
     * @param y        像素区域左上角的 Y 坐标
     * @param w        像素矩形的宽度
     * @param h        像素矩形的高度
     * @param b        要返回的波段
     * @param dArray   一个可选预分配的 double 数组
     * @return 指定矩形像素区域内指定波段的样本。
     *
     * @throws ArrayIndexOutOfBoundsException 如果坐标或波段索引不在范围内，或者 dArray 太小而无法容纳输出。
     */
    public double[] getSamples(int x, int y, int w, int h, int b,
                               double dArray[]) {
         return sampleModel.getSamples(x - sampleModelTranslateX,
                                       y - sampleModelTranslateY,
                                       w, h, b, dArray, dataBuffer);
    }

}
