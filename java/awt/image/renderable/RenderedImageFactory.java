/*
 * Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved.
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

/* ********************************************************************
 **********************************************************************
 **********************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 *** As  an unpublished  work pursuant to Title 17 of the United    ***
 *** States Code.  All rights reserved.                             ***
 **********************************************************************
 **********************************************************************
 **********************************************************************/

package java.awt.image.renderable;
import java.awt.image.RenderedImage;
import java.awt.RenderingHints;

/**
 * RenderedImageFactory 接口（通常缩写为 RIF）旨在由希望充当工厂的类实现，
 * 以生成不同的渲染结果，例如根据特定的一组参数、属性和渲染提示对一组源执行一系列 BufferedImageOps。
 */
public interface RenderedImageFactory {

  /**
   * 创建一个 RenderedImage，表示给定 ParameterBlock 和 RenderingHints 的图像操作（或操作链）的结果。
   * RIF 还可以根据需要查询 ParameterBlock 引用的任何源图像的尺寸、SampleModels、属性等。
   *
   * <p> 如果 RenderedImageFactory 无法为给定的源图像集和参数生成输出，则 create() 方法可以返回 null。
   * 例如，如果 RenderedImageFactory 只能对单波段图像数据执行 3x3 卷积，而源图像有多个波段或卷积内核为 5x5，则应返回 null。
   *
   * <p> 应考虑提示，但可以忽略。创建的 RenderedImage 可能有一个由字符串 HINTS_OBSERVED 标识的属性，
   * 以指示用于创建图像的 RenderingHints。此外，通过在创建的 RenderedImage 上调用 getSources() 方法获得的任何 RenderedImages
   * 也可能具有此类属性。
   *
   * @param paramBlock 包含用于创建 RenderedImage 的源和参数的 ParameterBlock。
   * @param hints 包含提示的 RenderingHints 对象。
   * @return 包含所需输出的 RenderedImage。
   */
  RenderedImage create(ParameterBlock paramBlock,
                       RenderingHints hints);
}
