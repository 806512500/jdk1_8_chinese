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
import java.util.Vector;
import java.awt.RenderingHints;
import java.awt.image.*;

/**
 * RenderableImage 是一个通用接口，用于表示与渲染无关的图像（这一概念包括分辨率独立性）。也就是说，
 * 图像是以描述和操作的形式存在的，这些描述和操作独立于图像的任何具体渲染。例如，RenderableImage 可以
 * 以分辨率无关的方式进行旋转和裁剪。然后，可以根据不同的具体上下文（如草稿预览、高质量屏幕显示或打印）
 * 以最优方式渲染图像。
 *
 * <p> 通过 createRendering() 方法从 RenderableImage 获取 RenderedImage，该方法接受一个 RenderContext。
 * RenderContext 指定了如何构建 RenderedImage。注意，不能直接从 RenderableImage 提取像素。
 *
 * <p> createDefaultRendering() 和 createScaledRendering() 方法是方便方法，它们在内部构造适当的 RenderContext。
 * 所有的渲染方法都可能返回对先前生成的渲染的引用。
 */
public interface RenderableImage {

    /**
     * 用于标识通过 createRendering 或 createScaledRendering 方法获取的 RenderedImage 上的属性的字符串常量。
     * 如果存在这样的属性，属性的值将是一个 RenderingHints 对象，指明在创建渲染时观察到的提示。
     */
     static final String HINTS_OBSERVED = "HINTS_OBSERVED";

    /**
     * 返回一个 Vector，其中包含为该 RenderableImage 提供图像数据的 RenderableImages。注意，此方法可能
     * 返回一个空的 Vector，以表示图像没有源，或者返回 null，以表示没有可用信息。
     *
     * @return 一个（可能是空的）RenderableImages 的 Vector，或 null。
     */
    Vector<RenderableImage> getSources();

    /**
     * 从该图像的属性集中获取一个属性。如果属性名未被识别，将返回 java.awt.Image.UndefinedProperty。
     *
     * @param name 要获取的属性的名称，作为字符串。
     * @return 属性对象的引用，或 java.awt.Image.UndefinedProperty 的值。
     */
    Object getProperty(String name);

    /**
     * 返回由 getProperty 识别的名称列表。
     * @return 属性名称列表。
     */
    String[] getPropertyNames();

    /**
     * 如果使用相同参数的连续渲染（即调用 createRendering() 或 createScaledRendering()）可能产生不同的结果，
     * 则返回 true。此方法可用于确定是否可以缓存和重用现有渲染。始终返回 true 是安全的。
     * @return 如果使用相同参数的连续渲染可能产生不同的结果，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    boolean isDynamic();

    /**
     * 获取用户坐标空间中的宽度。按照惯例，RenderableImage 的通常宽度等于图像的宽高比（宽度除以高度）。
     *
     * @return 图像在用户坐标中的宽度。
     */
    float getWidth();

    /**
     * 获取用户坐标空间中的高度。按照惯例，RenderedImage 的通常高度等于 1.0F。
     *
     * @return 图像在用户坐标中的高度。
     */
    float getHeight();

    /**
     * 获取与渲染无关的图像数据的最小 X 坐标。
     * @return 与渲染无关的图像数据的最小 X 坐标。
     */
    float getMinX();

    /**
     * 获取与渲染无关的图像数据的最小 Y 坐标。
     * @return 与渲染无关的图像数据的最小 Y 坐标。
     */
    float getMinY();

    /**
     * 创建一个宽度为 w，高度为 h 的像素的 RenderedImage 实例。RenderContext 会自动构建，包含适当的 usr2dev 变换
     * 和整个图像的兴趣区域。所有渲染提示都来自传递的 hints。
     *
     * <p> 如果 w == 0，则将其视为 Math.round(h * (getWidth() / getHeight()))。
     * 同样，如果 h == 0，则将其视为 Math.round(w * (getHeight() / getWidth()))。w 和 h 中至少有一个必须非零，
     * 否则将抛出 IllegalArgumentException。
     *
     * <p> 创建的 RenderedImage 可能具有一个由字符串 HINTS_OBSERVED 标识的属性，以指明用于创建图像的 RenderingHints。
     * 此外，通过创建的 RenderedImage 上的 getSources() 方法获取的任何 RenderedImages 也可能具有这样的属性。
     *
     * @param w 渲染图像的宽度（以像素为单位），或 0。
     * @param h 渲染图像的高度（以像素为单位），或 0。
     * @param hints 包含提示的 RenderingHints 对象。
     * @return 包含渲染数据的 RenderedImage。
     */
    RenderedImage createScaledRendering(int w, int h, RenderingHints hints);

    /**
     * 创建一个具有默认宽度和高度（以像素为单位）的 RenderedImage 实例。RenderContext 会自动构建，包含适当的 usr2dev 变换
     * 和整个图像的兴趣区域。渲染提示为空。createDefaultRendering 可能会利用存储的渲染以提高速度。
     *
     * @return 包含渲染数据的 RenderedImage。
     */
    RenderedImage createDefaultRendering();

    /**
     * 使用给定的 RenderContext 创建一个表示此图像渲染的 RenderedImage。这是获取 RenderableImage 渲染的最通用方法。
     *
     * <p> 创建的 RenderedImage 可能具有一个由字符串 HINTS_OBSERVED 标识的属性，以指明用于创建图像的 RenderingHints（来自 RenderContext）。
     * 此外，通过创建的 RenderedImage 上的 getSources() 方法获取的任何 RenderedImages 也可能具有这样的属性。
     *
     * @param renderContext 用于生成渲染的 RenderContext。
     * @return 包含渲染数据的 RenderedImage。
     */
    RenderedImage createRendering(RenderContext renderContext);
}
