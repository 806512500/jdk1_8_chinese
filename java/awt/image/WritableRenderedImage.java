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
import java.awt.Point;

/**
 * WriteableRenderedImage 是一个通用接口，用于包含或可以生成 Rasters 形式的图像数据的对象，
 * 并且可以被修改和/或覆盖。图像数据可以存储/生成为单个图块或规则的图块数组。
 * <p>
 * WritableRenderedImage 在图块被签出用于写入（通过 getWritableTile 方法）时，
 * 以及当特定图块的最后一个写入者释放其访问权限（通过调用 releaseWritableTile）时，
 * 会通知其他感兴趣的对象。此外，它允许任何调用者确定是否有任何图块当前被签出（通过 hasTileWriters），
 * 以及获取这些图块的列表（通过 getWritableTileIndices，形式为 Point 对象的 Vector）。
 * <p>
 * 希望在图块可写性发生变化时收到通知的对象必须实现 TileObserver 接口，
 * 并通过调用 addTileObserver 添加。多次调用 addTileObserver 为同一对象将导致多次通知。
 * 现有的观察者可以通过调用 removeTileObserver 减少其通知；如果观察者没有通知，操作将是一个空操作。
 * <p>
 * WritableRenderedImage 必须确保仅在第一个写入者获取图块和最后一个写入者释放图块时才发生通知。
 *
 */

public interface WritableRenderedImage extends RenderedImage
{

  /**
   * 添加一个观察者。如果观察者已经存在，它将接收多次通知。
   * @param to 指定的 <code>TileObserver</code>
   */
  public void addTileObserver(TileObserver to);

  /**
   * 移除一个观察者。如果观察者未注册，什么也不会发生。如果观察者注册了多次通知，
   * 它现在将注册一次较少的通知。
   * @param to 指定的 <code>TileObserver</code>
   */
  public void removeTileObserver(TileObserver to);

  /**
   * 签出一个图块用于写入。
   *
   * WritableRenderedImage 负责在图块从没有写入者变为有一个写入者时通知所有 TileObservers。
   *
   * @param tileX 图块的 X 索引。
   * @param tileY 图块的 Y 索引。
   * @return 一个可写的图块。
   */
  public WritableRaster getWritableTile(int tileX, int tileY);

  /**
   * 放弃写入图块的权利。如果调用者继续写入图块，结果是未定义的。
   * 对此方法的调用应仅与对 getWritableTile 的调用成对出现；任何其他使用将导致未定义的结果。
   *
   * WritableRenderedImage 负责在图块从有一个写入者变为没有写入者时通知所有 TileObservers。
   *
   * @param tileX 图块的 X 索引。
   * @param tileY 图块的 Y 索引。
   */
  public void releaseWritableTile(int tileX, int tileY);

  /**
   * 返回指定图块是否当前被签出用于写入。
   *
   * @param tileX 图块的 X 索引。
   * @param tileY 图块的 Y 索引。
   * @return 如果指定图块被签出用于写入，则返回 <code>true</code>；否则返回 <code>false</code>。
   */
  public boolean isTileWritable(int tileX, int tileY);

  /**
   * 返回一个 Point 对象数组，指示哪些图块被签出用于写入。如果没有图块被签出，则返回 null。
   * @return 一个包含被签出用于写入的图块位置的数组。
   */
  public Point[] getWritableTileIndices();

  /**
   * 返回是否有任何图块被签出用于写入。语义上等同于 (getWritableTileIndices() != null)。
   * @return 如果有任何图块被签出用于写入，则返回 <code>true</code>；否则返回 <code>false</code>。
   */
  public boolean hasTileWriters();

  /**
   * 将图像的一个矩形区域设置为 Raster r 的内容，假设 Raster r 与 WritableRenderedImage 处于相同的坐标空间。
   * 该操作将被裁剪到 WritableRenderedImage 的边界。
   * @param r 指定的 <code>Raster</code>
   */
  public void setData(Raster r);

}
