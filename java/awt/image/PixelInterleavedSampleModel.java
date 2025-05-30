/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 *  此类表示以像素交错方式存储的图像数据，并且每个像素的每个样本占用 DataBuffer 中的一个数据元素。
 *  它继承自 ComponentSampleModel，但为访问像素交错图像数据提供了更高效的实现。此类
 *  将所有波段的样本数据存储在 DataBuffer 的单个银行中。提供了访问器方法，以便可以直接操作图像数据。
 *  像素步长是指同一扫描行上同一波段的两个样本之间的数据数组元素数。
 *  扫描行步长是指给定样本与其在下一扫描行同一列的对应样本之间的数据数组元素数。
 *  波段偏移表示从存储每个波段的 DataBuffer 银行的第一个数据数组元素到该波段第一个样本的数据数组元素数。
 *  波段编号从 0 到 N-1。
 *  银行索引表示数据缓冲区的银行与图像数据的波段之间的对应关系。
 *  此类支持
 *  {@link DataBuffer#TYPE_BYTE TYPE_BYTE}，
 *  {@link DataBuffer#TYPE_USHORT TYPE_USHORT}，
 *  {@link DataBuffer#TYPE_SHORT TYPE_SHORT}，
 *  {@link DataBuffer#TYPE_INT TYPE_INT}，
 *  {@link DataBuffer#TYPE_FLOAT TYPE_FLOAT} 和
 *  {@link DataBuffer#TYPE_DOUBLE TYPE_DOUBLE} 数据类型。
 */

public class PixelInterleavedSampleModel extends ComponentSampleModel
{
    /**
     * 使用指定参数构造一个 PixelInterleavedSampleModel。
     * 波段的数量将由 bandOffsets 数组的长度给出。
     * @param dataType  用于存储样本的数据类型。
     * @param w         所描述的图像数据区域的宽度（以像素为单位）。
     * @param h         所描述的图像数据区域的高度（以像素为单位）。
     * @param pixelStride 图像数据的像素步长。
     * @param scanlineStride 图像数据的行步长。
     * @param bandOffsets 所有波段的偏移量。
     * @throws IllegalArgumentException 如果 <code>w</code> 或
     *         <code>h</code> 不大于 0
     * @throws IllegalArgumentException 如果任何波段之间的偏移量
     *         大于行步长
     * @throws IllegalArgumentException 如果
     *         <code>pixelStride</code> 和 <code>w</code> 的乘积大于
     *         <code>scanlineStride</code>
     * @throws IllegalArgumentException 如果 <code>pixelStride</code> 小于
     *         任何波段之间的偏移量
     * @throws IllegalArgumentException 如果 <code>dataType</code> 不是
     *         支持的数据类型之一
     */
    public PixelInterleavedSampleModel(int dataType,
                                       int w, int h,
                                       int pixelStride,
                                       int scanlineStride,
                                       int bandOffsets[]) {
        super(dataType, w, h, pixelStride, scanlineStride, bandOffsets);
        int minBandOff=this.bandOffsets[0];
        int maxBandOff=this.bandOffsets[0];
        for (int i=1; i<this.bandOffsets.length; i++) {
            minBandOff = Math.min(minBandOff,this.bandOffsets[i]);
            maxBandOff = Math.max(maxBandOff,this.bandOffsets[i]);
        }
        maxBandOff -= minBandOff;
        if (maxBandOff > scanlineStride) {
            throw new IllegalArgumentException("波段之间的偏移量必须小于行步长");
        }
        if (pixelStride*w > scanlineStride) {
            throw new IllegalArgumentException("像素步长乘以宽度必须小于或等于行步长");
        }
        if (pixelStride < maxBandOff) {
            throw new IllegalArgumentException("像素步长必须大于或等于波段之间的偏移量");
        }
    }

    /**
     * 创建一个具有指定宽度和高度的新 PixelInterleavedSampleModel。
     * 新的 PixelInterleavedSampleModel 将具有与当前 PixelInterleavedSampleModel 相同的波段数、存储数据类型和像素步长。
     * 波段偏移量可能会被压缩，使得所有波段偏移量的最小值为零。
     * @param w 结果 <code>SampleModel</code> 的宽度
     * @param h 结果 <code>SampleModel</code> 的高度
     * @return 一个具有指定宽度和高度的新 <code>SampleModel</code>。
     * @throws IllegalArgumentException 如果 <code>w</code> 或
     *         <code>h</code> 不大于 0
     */
    public SampleModel createCompatibleSampleModel(int w, int h) {
        int minBandoff=bandOffsets[0];
        int numBands = bandOffsets.length;
        for (int i=1; i < numBands; i++) {
            if (bandOffsets[i] < minBandoff) {
                minBandoff = bandOffsets[i];
            }
        }
        int[] bandOff;
        if (minBandoff > 0) {
            bandOff = new int[numBands];
            for (int i=0; i < numBands; i++) {
                bandOff[i] = bandOffsets[i] - minBandoff;
            }
        }
        else {
            bandOff = bandOffsets;
        }
        return new PixelInterleavedSampleModel(dataType, w, h, pixelStride,
                                               pixelStride*w, bandOff);
    }

    /**
     * 创建一个具有当前 PixelInterleavedSampleModel 波段子集的新 PixelInterleavedSampleModel。
     * 新的 PixelInterleavedSampleModel 可以与当前 PixelInterleavedSampleModel 可以使用的任何 DataBuffer 一起使用。
     * 新的 PixelInterleavedSampleModel/DataBuffer 组合将表示一个具有原始
     * PixelInterleavedSampleModel/DataBuffer 组合波段子集的图像。
     */
    public SampleModel createSubsetSampleModel(int bands[]) {
        int newBandOffsets[] = new int[bands.length];
        for (int i=0; i<bands.length; i++) {
            newBandOffsets[i] = bandOffsets[bands[i]];
        }
        return new PixelInterleavedSampleModel(this.dataType, width, height,
                                               this.pixelStride,
                                               scanlineStride, newBandOffsets);
    }

    // 与其他 ComponentSampleModel 子类区分开的哈希码
    public int hashCode() {
        return super.hashCode() ^ 0x1;
    }
}
