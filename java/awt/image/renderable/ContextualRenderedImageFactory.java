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
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;

/**
 * ContextualRenderedImageFactory 提供了一个接口，用于 RenderableImageOp 实例之间可能不同的功能。
 * 因此，通过使用多个 ContextualRenderedImageFactory 实例，可以通过单个类（如 RenderedImageOp）执行
 * RenderableImages 的不同操作。ContextualRenderedImageFactory 的名称通常简称为 "CRIF"。
 *
 * <p> 所有要在渲染独立链中使用的操作都必须实现 ContextualRenderedImageFactory。
 *
 * <p> 实现此接口的类必须提供一个无参数的构造函数。
 */
public interface ContextualRenderedImageFactory extends RenderedImageFactory {

    /**
     * 将操作的输出 RenderContext 映射到操作每个源的 RenderContext。这对于可以完全或部分
     * 仅通过 RenderContext 的更改来表达的操作（如仿射映射）或希望获取较低质量的源以节省处理
     * 努力或传输带宽的操作非常有用。某些操作（如模糊）也可以使用此机制来避免获取高于必要的源质量。
     *
     * @param i 源图像的索引。
     * @param renderContext 应用于操作的 RenderContext。
     * @param paramBlock 包含操作源和参数的 ParameterBlock。
     * @param image 正在渲染的 RenderableImage。
     * @return 指定参数 Vector 中指定索引处的源的 <code>RenderContext</code>。
     */
    RenderContext mapRenderContext(int i,
                                   RenderContext renderContext,
                                   ParameterBlock paramBlock,
                                   RenderableImage image);

    /**
     * 给定 RenderContext 和包含操作源和参数的 ParameterBlock，创建一个渲染。
     * 输出是一个 RenderedImage，它根据 RenderContext 确定其尺寸和在图像平面上的位置。
     * 此方法包含了允许渲染独立操作适应特定 RenderContext 的“智能”。
     *
     * @param renderContext 指定渲染的 RenderContext。
     * @param paramBlock 包含操作源和参数的 ParameterBlock。
     * @return 从指定 ParameterBlock 中的源和参数以及指定 RenderContext 中的渲染指令生成的 <code>RenderedImage</code>。
     */
    RenderedImage create(RenderContext renderContext,
                         ParameterBlock paramBlock);

    /**
     * 返回给定源集上执行操作的输出的渲染独立空间中的边界框。
     * 边界以 Rectangle2D 形式返回，即具有浮点角坐标的轴对齐矩形。
     *
     * @param paramBlock 包含操作源和参数的 ParameterBlock。
     * @return 指定操作输出的渲染独立边界框的 Rectangle2D。
     */
    Rectangle2D getBounds2D(ParameterBlock paramBlock);

    /**
     * 获取由 name 参数指定的属性的适当实例。当多个源都指定了该属性时，此方法必须确定返回哪个属性实例。
     *
     * @param paramBlock 包含操作源和参数的 ParameterBlock。
     * @param name 指定所需属性的字符串。
     * @return 请求属性的值的对象引用。
     */
    Object getProperty(ParameterBlock paramBlock, String name);

    /**
     * 返回 getProperty 识别的属性名称列表。
     * @return 属性名称列表。
     */
    String[] getPropertyNames();

    /**
     * 如果使用相同参数的连续渲染（即调用 create(RenderContext, ParameterBlock)）可能产生不同的结果，则返回 true。
     * 此方法可用于确定是否可以缓存和重用现有渲染。始终返回 true 是安全的。
     * @return <code>true</code> 如果使用相同参数的连续渲染可能产生不同的结果；<code>false</code> 否则。
     */
    boolean isDynamic();
}
