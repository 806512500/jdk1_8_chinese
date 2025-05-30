/*
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
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
  * 一个接口，用于希望在 WritableRenderedImage 的平铺（tiles）通过调用 getWritableTile 变得可由某些写入者修改，以及通过最后一次调用 releaseWritableTile 变得不可修改时，收到通知的对象。
  *
  * @see WritableRenderedImage
  *
  * @author Thomas DeWeese
  * @author Daniel Rice
  */
public interface TileObserver {

  /**
    * 一个平铺即将被更新（它即将被获取以进行写入，或者它正在被释放）。
    *
    * @param source 拥有该平铺的图像。
    * @param tileX 即将被更新的平铺的 X 索引。
    * @param tileY 即将被更新的平铺的 Y 索引。
    * @param willBeWritable 如果为 true，平铺将被获取以进行写入；否则它正在被释放。
    */
    public void tileUpdate(WritableRenderedImage source,
                           int tileX, int tileY,
                           boolean willBeWritable);

}
