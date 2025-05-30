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
import java.awt.Rectangle;
import java.util.Dictionary;
import java.util.Vector;

/**
 * RenderedImage 是一个通用接口，用于包含或可以生成 Rasters 形式的图像数据的对象。图像数据可以存储/生成为单个平铺或规则的平铺数组。
 */

public interface RenderedImage {

    /**
     * 返回一个包含此 RenderedImage 的直接图像数据源的 RenderedImages 向量。如果 RenderedImage 对象没有关于其直接源的信息，则此方法返回 null。
     * 如果 RenderedImage 对象没有直接源，则返回一个空的 Vector。
     * @return 一个 <code>RenderedImage</code> 对象的向量。
     */
    Vector<RenderedImage> getSources();

    /**
     * 从该图像的属性集中获取一个属性。属性集及其是否不可变由实现类确定。如果指定的属性未为此 RenderedImage 定义，则此方法返回
     * java.awt.Image.UndefinedProperty。
     * @param name 属性的名称
     * @return 由指定名称指示的属性。
     * @see java.awt.Image#UndefinedProperty
     */
    Object getProperty(String name);

    /**
      * 返回一个由 {@link #getProperty(String) getProperty(String)} 识别的名称数组，或 <code>null</code>，如果没有任何属性名称被识别。
      * @return 一个包含所有 <code>getProperty(String)</code> 识别的属性名称的 <code>String</code> 数组；
      * 或 <code>null</code> 如果没有任何属性名称被识别。
      */
    String[] getPropertyNames();

    /**
     * 返回与此图像关联的 ColorModel。从此图像返回的所有 Rasters 都将具有此 ColorModel。这可以返回 null。
     * @return 此图像的 <code>ColorModel</code>。
     */
    ColorModel getColorModel();

    /**
     * 返回与此图像关联的 SampleModel。从此图像返回的所有 Rasters 都将具有此 SampleModel。
     * @return 此图像的 <code>SampleModel</code>。
     */
    SampleModel getSampleModel();

    /**
     * 返回 RenderedImage 的宽度。
     * @return 此 <code>RenderedImage</code> 的宽度。
     */
    int getWidth();

    /**
     * 返回 RenderedImage 的高度。
     * @return 此 <code>RenderedImage</code> 的高度。
     */
    int getHeight();

    /**
     * 返回 RenderedImage 的最小 X 坐标（包含）。
     * @return 此 <code>RenderedImage</code> 的 X 坐标。
     */
    int getMinX();

    /**
     * 返回 RenderedImage 的最小 Y 坐标（包含）。
     * @return 此 <code>RenderedImage</code> 的 Y 坐标。
     */
    int getMinY();

    /**
     * 返回 X 方向的平铺数量。
     * @return X 方向的平铺数量。
     */
    int getNumXTiles();

    /**
     * 返回 Y 方向的平铺数量。
     * @return Y 方向的平铺数量。
     */
    int getNumYTiles();

    /**
     * 返回 X 方向的最小平铺索引。
     * @return X 方向的最小平铺索引。
     */
    int getMinTileX();

    /**
     * 返回 Y 方向的最小平铺索引。
     * @return X 方向的最小平铺索引。
     */
    int getMinTileY();

    /**
     * 返回平铺宽度（以像素为单位）。所有平铺必须具有相同的宽度。
     * @return 平铺宽度（以像素为单位）。
     */
    int getTileWidth();

    /**
     * 返回平铺高度（以像素为单位）。所有平铺必须具有相同的高度。
     * @return 平铺高度（以像素为单位）。
     */
    int getTileHeight();

    /**
     * 返回平铺网格相对于原点的 X 偏移量，即平铺 (0, 0) 的左上角像素的 X 坐标。
     * （注意，平铺 (0, 0) 可能实际上并不存在。）
     * @return 平铺网格相对于原点的 X 偏移量。
     */
    int getTileGridXOffset();

    /**
     * 返回平铺网格相对于原点的 Y 偏移量，即平铺 (0, 0) 的左上角像素的 Y 坐标。
     * （注意，平铺 (0, 0) 可能实际上并不存在。）
     * @return 平铺网格相对于原点的 Y 偏移量。
     */
    int getTileGridYOffset();

    /**
     * 返回 (tileX, tileY) 平铺。注意，tileX 和 tileY 是平铺数组中的索引，而不是像素位置。返回的 Raster 是活动的，如果图像发生变化，它将被更新。
     * @param tileX 请求的平铺在平铺数组中的 X 索引
     * @param tileY 请求的平铺在平铺数组中的 Y 索引
     * @return 指定索引的平铺。
     */
   Raster getTile(int tileX, int tileY);

    /**
     * 返回图像作为一个大平铺（对于基于平铺的图像，这将需要获取整个图像并复制图像数据）。返回的 Raster 是图像数据的副本，如果图像发生变化，它将不会被更新。
     * @return 图像作为一个大平铺。
     */
    Raster getData();

    /**
     * 计算并返回 RenderedImage 的任意区域。返回的 Raster 是图像数据的副本，如果图像发生变化，它将不会被更新。
     * @param rect 要返回的 RenderedImage 的区域。
     * @return 由指定 <code>Rectangle</code> 指示的 <code>RenderedImage</code> 的区域。
     */
    Raster getData(Rectangle rect);

    /**
     * 计算 RenderedImage 的任意矩形区域并将其复制到调用者提供的 WritableRaster 中。要计算的区域由提供的 WritableRaster 的边界确定。
     * 提供的 WritableRaster 必须具有与此图像兼容的 SampleModel。如果 raster 为 null，则创建一个合适的 WritableRaster。
     * @param raster 用于保存返回的图像部分的 WritableRaster，或 null。
     * @return 对提供的或创建的 WritableRaster 的引用。
     */
    WritableRaster copyData(WritableRaster raster);
}
