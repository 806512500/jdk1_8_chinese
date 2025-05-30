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
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.RenderingHints;
import java.util.Hashtable;
import java.util.Vector;

/**
 * 该类在关联的 ContextualRenderedImageFactory 实例的帮助下处理操作的可渲染方面。
 */
public class RenderableImageOp implements RenderableImage {

    /** 包含源和参数的 ParameterBlock。 */
    ParameterBlock paramBlock;

    /** 关联的 ContextualRenderedImageFactory。 */
    ContextualRenderedImageFactory myCRIF;

    /** 该 RenderableImageOp 的结果的边界框。 */
    Rectangle2D boundingBox;


    /**
     * 构造一个 RenderedImageOp，给定一个
     * ContextualRenderedImageFactory 对象，和
     * 一个包含 RenderableImage 源和其他
     * 参数的 ParameterBlock。ParameterBlock 中引用的任何 RenderedImage 源将被忽略。
     *
     * @param CRIF 一个 ContextualRenderedImageFactory 对象
     * @param paramBlock 一个包含此操作的源图像和其他参数的 ParameterBlock，这些参数是操作运行所必需的。
     */
    public RenderableImageOp(ContextualRenderedImageFactory CRIF,
                             ParameterBlock paramBlock) {
        this.myCRIF = CRIF;
        this.paramBlock = (ParameterBlock) paramBlock.clone();
    }

    /**
     * 返回一个包含此 RenderableImage 的图像数据源的 RenderableImages 向量。注意，此方法可能
     * 返回一个空向量，表示图像没有源，或者返回 null，表示没有可用信息。
     *
     * @return 一个（可能为空的）RenderableImages 向量，或 null。
     */
    public Vector<RenderableImage> getSources() {
        return getRenderableSources();
    }

    private Vector getRenderableSources() {
        Vector sources = null;

        if (paramBlock.getNumSources() > 0) {
            sources = new Vector();
            int i = 0;
            while (i < paramBlock.getNumSources()) {
                Object o = paramBlock.getSource(i);
                if (o instanceof RenderableImage) {
                    sources.add((RenderableImage)o);
                    i++;
                } else {
                    break;
                }
            }
        }
        return sources;
    }

    /**
     * 从图像的属性集中获取一个属性。
     * 如果属性名称未被识别，将返回 java.awt.Image.UndefinedProperty。
     *
     * @param name 要获取的属性的名称，作为字符串。
     * @return 属性对象的引用，或 java.awt.Image.UndefinedProperty 的值。
     */
    public Object getProperty(String name) {
        return myCRIF.getProperty(paramBlock, name);
    }

    /**
     * 返回 getProperty 识别的名称列表。
     * @return 属性名称列表。
     */
    public String[] getPropertyNames() {
        return myCRIF.getPropertyNames();
    }

    /**
     * 如果使用相同参数的连续渲染（即调用 createRendering() 或 createScaledRendering()）可能会产生不同的结果，则返回 true。
     * 此方法可用于确定是否可以缓存和重用现有渲染。将调用 CRIF 的 isDynamic 方法。
     * @return 如果使用相同参数的连续渲染可能会产生不同的结果，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean isDynamic() {
        return myCRIF.isDynamic();
    }

    /**
     * 获取用户坐标空间中的宽度。按照惯例，RenderableImage 的通常宽度等于图像的宽高比（宽度除以高度）。
     *
     * @return 图像在用户坐标中的宽度。
     */
    public float getWidth() {
        if (boundingBox == null) {
            boundingBox = myCRIF.getBounds2D(paramBlock);
        }
        return (float)boundingBox.getWidth();
    }

    /**
     * 获取用户坐标空间中的高度。按照惯例，RenderedImage 的通常高度等于 1.0F。
     *
     * @return 图像在用户坐标中的高度。
     */
    public float getHeight() {
        if (boundingBox == null) {
            boundingBox = myCRIF.getBounds2D(paramBlock);
        }
        return (float)boundingBox.getHeight();
    }

    /**
     * 获取渲染独立图像数据的最小 X 坐标。
     */
    public float getMinX() {
        if (boundingBox == null) {
            boundingBox = myCRIF.getBounds2D(paramBlock);
        }
        return (float)boundingBox.getMinX();
    }

    /**
     * 获取渲染独立图像数据的最小 Y 坐标。
     */
    public float getMinY() {
        if (boundingBox == null) {
            boundingBox = myCRIF.getBounds2D(paramBlock);
        }
        return (float)boundingBox.getMinY();
    }

    /**
     * 更改操作的当前 ParameterBlock，允许编辑图像渲染链。这种更改的效果将在从
     * 此 RenderableImageOp 或任何依赖的 RenderableImageOp 创建新渲染时可见。
     *
     * @param paramBlock 新的 ParameterBlock。
     * @return 旧的 ParameterBlock。
     * @see #getParameterBlock
     */
    public ParameterBlock setParameterBlock(ParameterBlock paramBlock) {
        ParameterBlock oldParamBlock = this.paramBlock;
        this.paramBlock = (ParameterBlock)paramBlock.clone();
        return oldParamBlock;
    }

    /**
     * 返回当前参数块的引用。
     * @return 此 <code>RenderableImageOp</code> 的 <code>ParameterBlock</code>。
     * @see #setParameterBlock(ParameterBlock)
     */
    public ParameterBlock getParameterBlock() {
        return paramBlock;
    }

    /**
     * 创建一个宽度为 w，高度为 h 的像素的 RenderedImage 实例。RenderContext 自动构建
     * 一个适当的 usr2dev 变换和一个全图像的兴趣区域。所有渲染提示都来自传递的提示。
     *
     * <p> 如果 w == 0，则将取值为
     * Math.round(h*(getWidth()/getHeight()))。
     * 同样，如果 h == 0，则将取值为
     * Math.round(w*(getHeight()/getWidth()))。w 或 h 必须有一个非零值，否则将抛出 IllegalArgumentException。
     *
     * <p> 创建的 RenderedImage 可能具有一个由字符串 HINTS_OBSERVED 标识的属性，以指示用于创建图像的 RenderingHints。
     * 此外，通过在创建的 RenderedImage 上调用 getSources() 方法获得的任何 RenderedImages 也可能具有此类属性。
     *
     * @param w 渲染图像的宽度（以像素为单位），或 0。
     * @param h 渲染图像的高度（以像素为单位），或 0。
     * @param hints 包含提示的 RenderingHints 对象。
     * @return 包含渲染数据的 RenderedImage。
     */
    public RenderedImage createScaledRendering(int w, int h,
                                               RenderingHints hints) {
        // DSR -- 代码尝试获取单位比例
        double sx = (double)w/getWidth();
        double sy = (double)h/getHeight();
        if (Math.abs(sx/sy - 1.0) < 0.01) {
            sx = sy;
        }
        AffineTransform usr2dev = AffineTransform.getScaleInstance(sx, sy);
        RenderContext newRC = new RenderContext(usr2dev, hints);
        return createRendering(newRC);
    }

    /**
     * 获取一个默认宽度和高度（以像素为单位）的 RenderedImage 实例。RenderContext 自动构建
     * 一个适当的 usr2dev 变换和一个全图像的兴趣区域。所有渲染提示都来自传递的提示。实现此接口的类必须确保有定义的默认宽度和高度。
     *
     * @return 包含渲染数据的 RenderedImage。
     */
    public RenderedImage createDefaultRendering() {
        AffineTransform usr2dev = new AffineTransform(); // Identity
        RenderContext newRC = new RenderContext(usr2dev);
        return createRendering(newRC);
    }

    /**
     * 创建一个表示此 RenderableImageOp（包括其 Renderable 源）的 RenderedImage，根据给定的 RenderContext 进行渲染。
     *
     * <p> 此方法支持 Renderable 或 RenderedImage 操作的链式调用。如果用于构造 RenderableImageOp 的 ParameterBlock 中的源是 RenderableImages，则遵循三步过程：
     *
     * <ol>
     * <li> 对每个 RenderableImage 源调用关联 CRIF 的 mapRenderContext()；
     * <li> 使用在第 1 步中获得的反向映射的 RenderContext 调用每个 RenderableImage 源的 createRendering()，从而生成每个源的渲染；
     * <li> 使用包含 RenderableImageOp 的参数和由 createRendering() 调用生成的 RenderedImages 的新 ParameterBlock 调用 ContextualRenderedImageFactory.create()。
     * </ol>
     *
     * <p> 如果用于构造 RenderableImageOp 的 ParameterBlock 的源向量的元素是 RenderedImage 的实例，则立即使用原始 ParameterBlock 调用 CRIF.create() 方法。
     * 这为递归提供了基础情况。
     *
     * <p> 创建的 RenderedImage 可能具有一个由字符串 HINTS_OBSERVED 标识的属性，以指示用于创建图像的 RenderingHints（来自 RenderContext）。
     * 此外，通过在创建的 RenderedImage 上调用 getSources() 方法获得的任何 RenderedImages 也可能具有此类属性。
     *
     * @param renderContext 用于执行渲染的 RenderContext。
     * @return 包含所需输出图像的 RenderedImage。
     */
    public RenderedImage createRendering(RenderContext renderContext) {
        RenderedImage image = null;
        RenderContext rcOut = null;

        // 克隆原始 ParameterBlock；如果 ParameterBlock 包含 RenderableImage 源，则将它们替换为 RenderedImages。
        ParameterBlock renderedParamBlock = (ParameterBlock)paramBlock.clone();
        Vector sources = getRenderableSources();

        try {
            // 假设如果没有可渲染的源，则 paramBlock 中有已渲染的源

            if (sources != null) {
                Vector renderedSources = new Vector();
                for (int i = 0; i < sources.size(); i++) {
                    rcOut = myCRIF.mapRenderContext(i, renderContext,
                                                    paramBlock, this);
                    RenderedImage rdrdImage =
                       ((RenderableImage)sources.elementAt(i)).createRendering(rcOut);
                    if (rdrdImage == null) {
                        return null;
                    }

                    // 将此渲染图像添加到 ParameterBlock 的 RenderedImages 列表中。
                    renderedSources.addElement(rdrdImage);
                }

                if (renderedSources.size() > 0) {
                    renderedParamBlock.setSources(renderedSources);
                }
            }

            return myCRIF.create(renderContext, renderedParamBlock);
        } catch (ArrayIndexOutOfBoundsException e) {
            // 这不应该发生
            return null;
        }
    }
}
