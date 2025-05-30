
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.awt;

import java.awt.RenderingHints.Key;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.awt.image.BufferedImageOp;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.awt.font.GlyphVector;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * 该 <code>Graphics2D</code> 类扩展了
 * {@link Graphics} 类，以提供更复杂的
 * 几何、坐标变换、颜色管理和文本布局控制。这是在 Java(tm) 平台上
 * 渲染 2 维形状、文本和图像的基本类。
 * <p>
 * <h2>坐标空间</h2>
 * 传递给 <code>Graphics2D</code> 对象的所有坐标都以一个与设备无关的坐标系统指定，称为用户空间，这是应用程序使用的。<code>Graphics2D</code> 对象包含一个
 * {@link AffineTransform} 对象作为其渲染状态的一部分，该对象定义了如何将用户空间坐标转换为设备空间中的设备依赖坐标。
 * <p>
 * 设备空间中的坐标通常指的是单个设备像素，并且对齐在这些像素之间的无限薄的间隙上。某些 <code>Graphics2D</code> 对象可以用于捕获渲染操作，以便存储到图形元文件中，稍后在未知物理分辨率的具体设备上回放。由于在捕获渲染操作时可能不知道分辨率，<code>Graphics2D</code> <code>Transform</code> 被设置为将用户坐标转换为虚拟设备空间，该空间近似于目标设备的预期分辨率。如果估计不准确，回放时可能需要进行进一步的变换。
 * <p>
 * 一些渲染属性对象执行的操作发生在设备空间中，但所有 <code>Graphics2D</code> 方法都接受用户空间坐标。
 * <p>
 * 每个 <code>Graphics2D</code> 对象都关联一个目标，该目标定义了渲染发生的位置。一个
 * {@link GraphicsConfiguration} 对象定义了渲染目标的特性，如像素格式和分辨率。在整个 <code>Graphics2D</code> 对象的生命周期中，使用相同的渲染目标。
 * <p>
 * 创建 <code>Graphics2D</code> 对象时，<code>GraphicsConfiguration</code>
 * 指定了 <code>Graphics2D</code> 目标的 <a name="deftransform">默认变换</a>（一个
 * {@link Component} 或 {@link Image}）。此默认变换将用户空间坐标系统映射到屏幕和打印机设备坐标，使得原点映射到设备目标区域的左上角，X 坐标向右增加，Y 坐标向下增加。
 * 对于接近 72 dpi 的设备，如屏幕设备，默认变换的缩放设置为恒等变换。对于高分辨率设备，如打印机，默认变换的缩放设置为每平方英寸约 72 个用户空间坐标。对于图像缓冲区，默认变换是
 * <code>Identity</code> 变换。
 *
 * <h2>渲染过程</h2>
 * 渲染过程可以分为四个由 <code>Graphics2D</code> 渲染属性控制的阶段。渲染器可以通过缓存结果、将多个虚拟步骤合并为一个操作，或者通过识别各种属性作为常见简单情况并修改操作的其他部分来优化这些步骤。
 * <p>
 * 渲染过程的步骤是：
 * <ol>
 * <li>
 * 确定要渲染的内容。
 * <li>
 * 将渲染操作限制在当前 <code>Clip</code> 内。<code>Clip</code> 由用户空间中的一个 {@link Shape} 指定，并由使用 <code>Graphics</code> 和
 * <code>Graphics2D</code> 的各种剪切操作方法的程序控制。此 <i>用户剪切</i>
 * 通过当前的 <code>Transform</code> 转换到设备空间，并与
 * <i>设备剪切</i> 结合，后者由窗口的可见性和设备范围定义。用户剪切和设备剪切的组合定义了 <i>复合剪切</i>，确定最终的剪切区域。用户剪切不会被渲染系统修改以反映结果的复合剪切。
 * <li>
 * 确定要渲染的颜色。
 * <li>
 * 使用当前的 {@link Composite} 属性将颜色应用到目标绘图表面。
 * </ol>
 * <br>
 * 三种类型的渲染操作及其各自的渲染过程详细信息如下：
 * <ol>
 * <li>
 * <b><a name="rendershape"><code>Shape</code> 操作</a></b>
 * <ol>
 * <li>
 * 如果操作是 <code>draw(Shape)</code> 操作，则使用当前 <code>Graphics2D</code> 上下文中的 {@link Stroke} 属性的
 * {@link Stroke#createStrokedShape(Shape) createStrokedShape}
 * 方法构造一个包含指定 <code>Shape</code> 轮廓的新 <code>Shape</code> 对象。
 * <li>
 * 使用当前 <code>Graphics2D</code> 上下文中的 <code>Transform</code>
 * 将 <code>Shape</code> 从用户空间转换到设备空间。
 * <li>
 * 使用 <code>Shape</code> 的
 * {@link Shape#getPathIterator(AffineTransform) getPathIterator} 方法提取 <code>Shape</code> 的轮廓，该方法返回一个
 * {@link java.awt.geom.PathIterator PathIterator}
 * 对象，该对象沿着 <code>Shape</code> 的边界迭代。
 * <li>
 * 如果 <code>Graphics2D</code> 对象无法处理 <code>PathIterator</code> 对象返回的曲线段，则可以调用 <code>Shape</code> 的
 * 替代
 * {@link Shape#getPathIterator(AffineTransform, double) getPathIterator}
 * 方法，该方法将 <code>Shape</code> 展平。
 * <li>
 * 查询当前 <code>Graphics2D</code> 上下文中的 {@link Paint} 以获取一个 {@link PaintContext}，该上下文指定要在设备空间中渲染的颜色。
 * </ol>
 * <li>
 * <b><a name=rendertext>文本操作</a></b>
 * <ol>
 * <li>
 * 以下步骤用于确定渲染指定 <code>String</code> 所需的一组字形：
 * <ol>
 * <li>
 * 如果参数是一个 <code>String</code>，则当前 <code>Graphics2D</code> 上下文中的 <code>Font</code> 被要求将 <code>String</code> 中的 Unicode 字符转换为一组字形，使用字体实现的基本布局和整形算法。
 * <li>
 * 如果参数是一个
 * {@link AttributedCharacterIterator}，
 * 则要求迭代器使用其嵌入的字体属性转换为一个
 * {@link java.awt.font.TextLayout TextLayout}
 * 对象。<code>TextLayout</code> 实现了更复杂的字形布局算法，可以自动执行多个不同书写方向的字体的 Unicode 双向布局调整。
 * <li>
 * 如果参数是一个
 * {@link GlyphVector}，则
 * <code>GlyphVector</code> 对象已经包含适当的字体特定字形代码以及每个字形位置的显式坐标。
 * </ol>
 * <li>
 * 查询当前 <code>Font</code> 以获取指定字形的轮廓。这些轮廓被视为用户空间中的形状，相对于步骤 1 中确定的每个字形的位置。
 * <li>
 * 按照上述 <a href="#rendershape"><code>Shape</code> 操作</a> 中所述填充字符轮廓。
 * <li>
 * 查询当前 <code>Paint</code> 以获取一个
 * <code>PaintContext</code>，该上下文指定要在设备空间中渲染的颜色。
 * </ol>
 * <li>
 * <b><a name= renderingimage><code>Image</code> 操作</a></b>
 * <ol>
 * <li>
 * 感兴趣的区域由源 <code>Image</code> 的边界框定义。
 * 该边界框在图像空间中指定，即 <code>Image</code> 对象的本地坐标系统。
 * <li>
 * 如果将 <code>AffineTransform</code> 传递给
 * {@link #drawImage(java.awt.Image, java.awt.geom.AffineTransform, java.awt.image.ImageObserver) drawImage(Image, AffineTransform, ImageObserver)}，
 * 则使用 <code>AffineTransform</code> 将边界框从图像空间转换到用户空间。如果没有提供 <code>AffineTransform</code>，则认为边界框已经在用户空间中。
 * <li>
 * 使用当前 <code>Transform</code> 将源 <code>Image</code> 的边界框从用户空间转换到设备空间。
 * 请注意，转换边界框的结果不一定在设备空间中形成矩形区域。
 * <li>
 * <code>Image</code> 对象根据当前 <code>Transform</code> 和可选图像变换指定的源到目标坐标映射确定要渲染的颜色。
 * </ol>
 * </ol>
 *
 * <h2>默认渲染属性</h2>
 * <code>Graphics2D</code> 渲染属性的默认值为：
 * <dl compact>
 * <dt><i><code>Paint</code></i>
 * <dd>组件的颜色。
 * <dt><i><code>Font</code></i>
 * <dd>组件的字体。
 * <dt><i><code>Stroke</code></i>
 * <dd>宽度为 1 的方形笔，无虚线，斜接线段连接和方形端帽。
 * <dt><i><code>Transform</code></i>
 * <dd>组件的 <code>GraphicsConfiguration</code> 的
 * {@link GraphicsConfiguration#getDefaultTransform() getDefaultTransform}。
 * <dt><i><code>Composite</code></i>
 * <dd>{@link AlphaComposite#SRC_OVER} 规则。
 * <dt><i><code>Clip</code></i>
 * <dd>没有渲染 <code>Clip</code>，输出被剪切到组件。
 * </dl>
 *
 * <h2>渲染兼容性问题</h2>
 * JDK(tm) 1.1 渲染模型基于一个像素化模型，该模型规定坐标
 * 是无限薄的，位于像素之间。使用一个像素宽的笔进行绘制操作，该笔填充路径锚点右侧和下方的像素。
 * JDK 1.1 渲染模型与大多数现有平台渲染器的能力一致，这些渲染器需要将整数坐标解析为必须完全位于指定数量像素上的离散笔。
 * <p>
 * Java 2D(tm)（Java(tm) 2 平台）API 支持抗锯齿渲染器。宽度为一个像素的笔不需要完全位于像素 N 而不是像素 N+1 上。笔可以部分位于两个像素上。对于宽笔，选择偏移方向不再是必要的，因为沿笔轨迹边缘发生的混合使笔的子像素位置对用户可见。另一方面，当通过将
 * {@link RenderingHints#KEY_ANTIALIASING KEY_ANTIALIASING} 提示键设置为
 * {@link RenderingHints#VALUE_ANTIALIAS_OFF VALUE_ANTIALIAS_OFF}
 * 提示值来关闭抗锯齿时，渲染器可能需要
 * 应用偏移以确定当笔位于像素边界上时要修改哪个像素，例如当它在设备空间中的整数坐标上绘制时。虽然抗锯齿渲染器的能力使得渲染模型不再需要为笔指定偏移方向，但希望抗锯齿和非抗锯齿渲染器在绘制屏幕上的单像素宽水平和垂直线等常见情况下表现相似。为了确保通过将
 * {@link RenderingHints#KEY_ANTIALIASING KEY_ANTIALIASING} 提示键设置为
 * {@link RenderingHints#VALUE_ANTIALIAS_ON VALUE_ANTIALIAS_ON}
 * 不会导致这些线突然变宽一半且透明度降低一半，希望模型为这些线指定一条路径，使它们完全覆盖特定的一组像素，以帮助增加其清晰度。
 * <p>
 * Java 2D API 保持与 JDK 1.1 渲染行为的兼容性，使得遗留操作和现有渲染器行为在 Java 2D API 下保持不变。定义了映射到通用 <code>draw</code> 和
 * <code>fill</code> 方法的遗留方法，明确指示 <code>Graphics2D</code> 如何基于 <code>Stroke</code> 和 <code>Transform</code>
 * 属性和渲染提示扩展 <code>Graphics</code>。在默认属性设置下，定义执行相同的操作。
 * 例如，默认 <code>Stroke</code> 是一个宽度为 1 且无虚线的 <code>BasicStroke</code>，屏幕绘制的默认变换是恒等变换。
 * <p>
 * 以下两条规则提供了使用抗锯齿或非抗锯齿时可预测的渲染行为。
 * <ul>
 * <li> 设备坐标定义为位于设备像素之间，这避免了抗锯齿和非抗锯齿渲染之间的任何不一致结果。如果坐标定义为位于像素中心，形状（如矩形）覆盖的一些像素可能只被部分覆盖。
 * 使用非抗锯齿渲染时，半覆盖的像素可能会被渲染在形状内部或外部。使用抗锯齿渲染时，形状的整个边缘上的像素可能会被半覆盖。另一方面，由于坐标定义为位于像素之间，形状（如矩形）将没有半覆盖的像素，无论是否使用抗锯齿渲染。
 * <li> 使用 <code>BasicStroke</code> 对象描边的线条和路径可能会被“归一化”，以在可绘制区域的不同位置提供一致的轮廓渲染，无论使用抗锯齿还是非抗锯齿渲染。此
 * 归一化过程由
 * {@link RenderingHints#KEY_STROKE_CONTROL KEY_STROKE_CONTROL} 提示控制。归一化算法的具体实现未指定，但其目标是确保线条在像素网格上的位置无论如何都具有一致的视觉外观，并在抗锯齿模式下促进更坚实的水平和垂直线，使其更接近其非抗锯齿的对应物。典型的归一化步骤可能包括将抗锯齿线端点提升到像素中心，以减少混合量，或调整非抗锯齿线的子像素位置，使浮点线宽等可能地四舍五入为偶数或奇数像素计数。此过程可以将端点移动最多半个像素（通常沿两个轴向正无穷方向移动），以促进这些一致的结果。
 * </ul>
 * <p>
 * 以下通用遗留方法的定义在默认属性设置下与先前指定的行为完全相同：
 * <ul>
 * <li>
 * 对于 <code>fill</code> 操作，包括 <code>fillRect</code>、
 * <code>fillRoundRect</code>、<code>fillOval</code>、
 * <code>fillArc</code>、<code>fillPolygon</code> 和
 * <code>clearRect</code>，现在可以调用 {@link #fill(Shape) fill} 并传入所需的 <code>Shape</code>。例如，当填充矩形时：
 * <pre>
 * fill(new Rectangle(x, y, w, h));
 * </pre>
 * 被调用。
 * <p>
 * <li>
 * 同样，对于绘制操作，包括 <code>drawLine</code>、
 * <code>drawRect</code>、<code>drawRoundRect</code>、
 * <code>drawOval</code>、<code>drawArc</code>、<code>drawPolyline</code>
 * 和 <code>drawPolygon</code>，现在可以调用 {@link #draw(Shape) draw} 并传入所需的 <code>Shape</code>。例如，当绘制矩形时：
 * <pre>
 * draw(new Rectangle(x, y, w, h));
 * </pre>
 * 被调用。
 * <p>
 * <li>
 * <code>draw3DRect</code> 和 <code>fill3DRect</code> 方法在 <code>Graphics</code> 类中是通过 <code>drawLine</code> 和
 * <code>fillRect</code> 方法实现的，这些方法的行为取决于 <code>Graphics2D</code> 上下文中的当前 <code>Stroke</code> 和 <code>Paint</code> 对象。此类覆盖了这些实现，使用当前 <code>Color</code> 独占覆盖当前 <code>Paint</code>，并使用 <code>fillRect</code> 描述与现有方法完全相同的行为，无论当前 <code>Stroke</code> 的设置如何。
 * </ul>
 * <code>Graphics</code> 类仅定义了 <code>setColor</code> 方法来控制要绘制的颜色。由于 Java 2D API 将 <code>Color</code> 对象扩展为实现新的 <code>Paint</code>
 * 接口，现有的
 * <code>setColor</code> 方法现在是一个方便的方法，用于将当前 <code>Paint</code> 属性设置为 <code>Color</code> 对象。
 * <code>setColor(c)</code> 等效于 <code>setPaint(c)</code>。
 * <p>
 * <code>Graphics</code> 类定义了两个方法来控制颜色如何应用于目标。
 * <ol>
 * <li>
 * <code>setPaintMode</code> 方法实现为一个方便的方法，用于设置默认的 <code>Composite</code>，等效于
 * <code>setComposite(new AlphaComposite.SrcOver)</code>。
 * <li>
 * <code>setXORMode(Color xorcolor)</code> 方法实现为一个方便的方法，用于设置一个特殊的 <code>Composite</code> 对象，该对象忽略源颜色的 <code>Alpha</code> 组件，并将目标颜色设置为值：
 * <pre>
 * dstpixel = (PixelOf(srccolor) ^ PixelOf(xorcolor) ^ dstpixel);
 * </pre>
 * </ol>
 *
 * @author Jim Graham
 * @see java.awt.RenderingHints
 */
public abstract class Graphics2D extends Graphics {


                /**
     * 构造一个新的 <code>Graphics2D</code> 对象。由于
     * <code>Graphics2D</code> 是一个抽象类，并且必须由子类为不同的输出设备进行定制，
     * <code>Graphics2D</code> 对象不能直接创建。
     * 相反，<code>Graphics2D</code> 对象必须从另一个
     * <code>Graphics2D</code> 对象、由 <code>Component</code> 创建，或从
     * {@link BufferedImage} 对象中获取。
     * @see java.awt.Component#getGraphics
     * @see java.awt.Graphics#create
     */
    protected Graphics2D() {
    }

    /**
     * 绘制一个3-D高亮轮廓的矩形。
     * 矩形的边缘被高亮处理，使其看起来像是从左上角被照亮的斜面。
     * <p>
     * 高亮效果使用的颜色是基于当前颜色确定的。
     * 生成的矩形覆盖的区域是
     * <code>width&nbsp;+&nbsp;1</code> 像素宽
     * 由 <code>height&nbsp;+&nbsp;1</code> 像素高。此方法
     * 仅使用当前 <code>Color</code>，并忽略当前的 <code>Paint</code>。
     * @param x 要绘制的矩形的 x 坐标。
     * @param y 要绘制的矩形的 y 坐标。
     * @param width 要绘制的矩形的宽度。
     * @param height 要绘制的矩形的高度。
     * @param raised 一个布尔值，确定矩形是否
     *                      看起来像是从表面凸起
     *                      或凹入表面。
     * @see         java.awt.Graphics#fill3DRect
     */
    public void draw3DRect(int x, int y, int width, int height,
                           boolean raised) {
        Paint p = getPaint();
        Color c = getColor();
        Color brighter = c.brighter();
        Color darker = c.darker();

        setColor(raised ? brighter : darker);
        //drawLine(x, y, x, y + height);
        fillRect(x, y, 1, height + 1);
        //drawLine(x + 1, y, x + width - 1, y);
        fillRect(x + 1, y, width - 1, 1);
        setColor(raised ? darker : brighter);
        //drawLine(x + 1, y + height, x + width, y + height);
        fillRect(x + 1, y + height, width, 1);
        //drawLine(x + width, y, x + width, y + height - 1);
        fillRect(x + width, y, 1, height);
        setPaint(p);
    }

    /**
     * 使用当前颜色绘制一个3-D高亮填充的矩形。
     * 矩形的边缘被高亮处理，使其看起来像是从左上角被照亮的斜面。
     * 高亮效果和填充使用的颜色是基于当前 <code>Color</code> 确定的。此方法
     * 仅使用当前 <code>Color</code>，并忽略当前的 <code>Paint</code>。
     * @param x 要填充的矩形的 x 坐标。
     * @param y 要填充的矩形的 y 坐标。
     * @param       width 要填充的矩形的宽度。
     * @param       height 要填充的矩形的高度。
     * @param       raised 一个布尔值，确定矩形是否
     *                      看起来像是从表面凸起
     *                      或凹入表面。
     * @see         java.awt.Graphics#draw3DRect
     */
    public void fill3DRect(int x, int y, int width, int height,
                           boolean raised) {
        Paint p = getPaint();
        Color c = getColor();
        Color brighter = c.brighter();
        Color darker = c.darker();

        if (!raised) {
            setColor(darker);
        } else if (p != c) {
            setColor(c);
        }
        fillRect(x+1, y+1, width-2, height-2);
        setColor(raised ? brighter : darker);
        //drawLine(x, y, x, y + height - 1);
        fillRect(x, y, 1, height);
        //drawLine(x + 1, y, x + width - 2, y);
        fillRect(x + 1, y, width - 2, 1);
        setColor(raised ? darker : brighter);
        //drawLine(x + 1, y + height - 1, x + width - 1, y + height - 1);
        fillRect(x + 1, y + height - 1, width - 1, 1);
        //drawLine(x + width - 1, y, x + width - 1, y + height - 2);
        fillRect(x + width - 1, y, 1, height - 1);
        setPaint(p);
    }

    /**
     * 使用当前 <code>Graphics2D</code> 上下文的设置绘制 <code>Shape</code> 的轮廓。
     * 应用的渲染属性包括 <code>Clip</code>、<code>Transform</code>、
     * <code>Paint</code>、<code>Composite</code> 和 <code>Stroke</code> 属性。
     * @param s 要渲染的 <code>Shape</code>
     * @see #setStroke
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see #transform
     * @see #setTransform
     * @see #clip
     * @see #setClip
     * @see #setComposite
     */
    public abstract void draw(Shape s);

    /**
     * 应用从图像空间到用户空间的变换后绘制图像。
     * 从用户空间到设备空间的变换是使用 <code>Graphics2D</code> 中的当前 <code>Transform</code> 完成的。
     * 指定的变换在 <code>Graphics2D</code> 上下文中的变换属性应用之前应用于图像。
     * 应用的渲染属性包括 <code>Clip</code>、<code>Transform</code> 和 <code>Composite</code> 属性。
     * 注意，如果指定的变换是非可逆的，则不会进行任何渲染。
     * @param img 要渲染的指定图像。
     *            如果 <code>img</code> 为 null，此方法不执行任何操作。
     * @param xform 从图像空间到用户空间的变换
     * @param obs 要通知的 {@link ImageObserver}
     * @return 如果 <code>Image</code> 完全加载并完全渲染，或为 null，则返回 <code>true</code>；
     *         如果 <code>Image</code> 仍在加载中，则返回 <code>false</code>。
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     */
    public abstract boolean drawImage(Image img,
                                      AffineTransform xform,
                                      ImageObserver obs);

    /**
     * 使用 {@link BufferedImageOp} 过滤并渲染一个 <code>BufferedImage</code>。
     * 应用的渲染属性包括 <code>Clip</code>、<code>Transform</code>
     * 和 <code>Composite</code> 属性。这等同于：
     * <pre>
     * img1 = op.filter(img, null);
     * drawImage(img1, new AffineTransform(1f,0f,0f,1f,x,y), null);
     * </pre>
     * @param op 应用于图像的过滤器
     * @param img 要渲染的指定 <code>BufferedImage</code>。
     *            如果 <code>img</code> 为 null，此方法不执行任何操作。
     * @param x 用户空间中图像左上角的 x 坐标
     * @param y 用户空间中图像左上角的 y 坐标
     *
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     */
    public abstract void drawImage(BufferedImage img,
                                   BufferedImageOp op,
                                   int x,
                                   int y);

    /**
     * 应用从图像空间到用户空间的变换后渲染一个 {@link RenderedImage}。
     * 从用户空间到设备空间的变换是使用 <code>Graphics2D</code> 中的当前 <code>Transform</code> 完成的。
     * 指定的变换在 <code>Graphics2D</code> 上下文中的变换属性应用之前应用于图像。
     * 应用的渲染属性包括 <code>Clip</code>、<code>Transform</code> 和 <code>Composite</code> 属性。注意
     * 如果指定的变换是非可逆的，则不会进行任何渲染。
     * @param img 要渲染的图像。如果 <code>img</code> 为 null，此方法不执行任何操作。
     * @param xform 从图像空间到用户空间的变换
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     */
    public abstract void drawRenderedImage(RenderedImage img,
                                           AffineTransform xform);

    /**
     * 应用从图像空间到用户空间的变换后渲染一个
     * {@link RenderableImage}。
     * 从用户空间到设备空间的变换是使用 <code>Graphics2D</code> 中的当前 <code>Transform</code> 完成的。
     * 指定的变换在 <code>Graphics2D</code> 上下文中的变换属性应用之前应用于图像。
     * 应用的渲染属性包括 <code>Clip</code>、<code>Transform</code> 和 <code>Composite</code> 属性。注意
     * 如果指定的变换是非可逆的，则不会进行任何渲染。
     *<p>
     * <code>Graphics2D</code> 对象上设置的渲染提示可能会
     * 用于渲染 <code>RenderableImage</code>。
     * 如果需要对特定 <code>RenderableImage</code> 识别的特定提示进行显式控制，或者需要了解使用了哪些提示，
     * 则应直接从 <code>RenderableImage</code> 获取 <code>RenderedImage</code>
     * 并使用
     *{@link #drawRenderedImage(RenderedImage, AffineTransform) drawRenderedImage} 进行渲染。
     * @param img 要渲染的图像。如果 <code>img</code> 为 null，此方法不执行任何操作。
     * @param xform 从图像空间到用户空间的变换
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     * @see #drawRenderedImage
     */
    public abstract void drawRenderableImage(RenderableImage img,
                                             AffineTransform xform);

    /**
     * 使用 <code>Graphics2D</code> 上下文中的当前文本属性状态渲染指定 <code>String</code> 的文本。
     * 第一个字符的基线位于用户空间的 (<i>x</i>,&nbsp;<i>y</i>) 位置。
     * 应用的渲染属性包括 <code>Clip</code>、<code>Transform</code>、<code>Paint</code>、<code>Font</code> 和
     * <code>Composite</code> 属性。对于希伯来语和阿拉伯语等脚本系统的字符，字形可以从右到左渲染，
     * 在这种情况下，提供的坐标是基线上最左边字符的位置。
     * @param str 要渲染的字符串
     * @param x 要渲染 <code>String</code> 的位置的 x 坐标
     * @param y 要渲染 <code>String</code> 的位置的 y 坐标
     * @throws NullPointerException 如果 <code>str</code> 为
     *         <code>null</code>
     * @see         java.awt.Graphics#drawBytes
     * @see         java.awt.Graphics#drawChars
     * @since       JDK1.0
     */
    public abstract void drawString(String str, int x, int y);

    /**
     * 使用 <code>Graphics2D</code> 上下文中的当前文本属性状态渲染指定 <code>String</code> 的文本。
     * 第一个字符的基线位于用户空间的 (<i>x</i>,&nbsp;<i>y</i>) 位置。
     * 应用的渲染属性包括 <code>Clip</code>、<code>Transform</code>、<code>Paint</code>、<code>Font</code> 和
     * <code>Composite</code> 属性。对于希伯来语和阿拉伯语等脚本系统的字符，字形可以从右到左渲染，
     * 在这种情况下，提供的坐标是基线上最左边字符的位置。
     * @param str 要渲染的 <code>String</code>
     * @param x 要渲染 <code>String</code> 的位置的 x 坐标
     * @param y 要渲染 <code>String</code> 的位置的 y 坐标
     * @throws NullPointerException 如果 <code>str</code> 为
     *         <code>null</code>
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see java.awt.Graphics#setFont
     * @see #setTransform
     * @see #setComposite
     * @see #setClip
     */
    public abstract void drawString(String str, float x, float y);

    /**
     * 根据 {@link TextAttribute} 类的规范应用其属性，渲染指定迭代器的文本。
     * <p>
     * 第一个字符的基线位于用户空间的 (<i>x</i>,&nbsp;<i>y</i>) 位置。
     * 对于希伯来语和阿拉伯语等脚本系统的字符，字形可以从右到左渲染，
     * 在这种情况下，提供的坐标是基线上最左边字符的位置。
     * @param iterator 要渲染其文本的迭代器
     * @param x 要渲染迭代器文本的位置的 x 坐标
     * @param y 要渲染迭代器文本的位置的 y 坐标
     * @throws NullPointerException 如果 <code>iterator</code> 为
     *         <code>null</code>
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see #setTransform
     * @see #setComposite
     * @see #setClip
     */
    public abstract void drawString(AttributedCharacterIterator iterator,
                                    int x, int y);

    /**
     * 根据 {@link TextAttribute} 类的规范应用其属性，渲染指定迭代器的文本。
     * <p>
     * 第一个字符的基线位于用户空间的 (<i>x</i>,&nbsp;<i>y</i>) 位置。
     * 对于希伯来语和阿拉伯语等脚本系统的字符，字形可以从右到左渲染，
     * 在这种情况下，提供的坐标是基线上最左边字符的位置。
     * @param iterator 要渲染其文本的迭代器
     * @param x 要渲染迭代器文本的位置的 x 坐标
     * @param y 要渲染迭代器文本的位置的 y 坐标
     * @throws NullPointerException 如果 <code>iterator</code> 为
     *         <code>null</code>
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see #setTransform
     * @see #setComposite
     * @see #setClip
     */
    public abstract void drawString(AttributedCharacterIterator iterator,
                                    float x, float y);

    /**
     * 使用 <code>Graphics2D</code> 上下文的渲染属性渲染指定的
     * {@link GlyphVector} 的文本。
     * 应用的渲染属性包括 <code>Clip</code>、<code>Transform</code>、<code>Paint</code> 和
     * <code>Composite</code> 属性。<code>GlyphVector</code> 指定了来自 {@link Font} 的单个字形。
     * <code>GlyphVector</code> 还可以包含字形位置。
     * 这是将一组字符渲染到屏幕上的最快方法。
     * @param g 要渲染的 <code>GlyphVector</code>
     * @param x 要渲染字形的用户空间中的 x 位置
     * @param y 要渲染字形的用户空间中的 y 位置
     * @throws NullPointerException 如果 <code>g</code> 为 <code>null</code>。
     *
     * @see java.awt.Font#createGlyphVector
     * @see java.awt.font.GlyphVector
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see #setTransform
     * @see #setComposite
     * @see #setClip
     */
    public abstract void drawGlyphVector(GlyphVector g, float x, float y);


                /**
     * 使用 <code>Graphics2D</code> 上下文的设置填充 <code>Shape</code> 的内部。应用的渲染属性包括 <code>Clip</code>、<code>Transform</code>、<code>Paint</code> 和 <code>Composite</code>。
     * @param s 要填充的 <code>Shape</code>
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     */
    public abstract void fill(Shape s);

    /**
     * 检查指定的 <code>Shape</code> 是否与指定的 {@link Rectangle}（位于设备空间中）相交。如果 <code>onStroke</code> 为 false，此方法检查指定的 <code>Shape</code> 的内部是否与指定的 <code>Rectangle</code> 相交。如果 <code>onStroke</code> 为 <code>true</code>，此方法检查指定的 <code>Shape</code> 轮廓的 <code>Stroke</code> 是否与指定的 <code>Rectangle</code> 相交。
     * 考虑的渲染属性包括 <code>Clip</code>、<code>Transform</code> 和 <code>Stroke</code> 属性。
     * @param rect 要检查的设备空间中的区域
     * @param s 要检查的 <code>Shape</code>
     * @param onStroke 用于选择测试描边或填充形状的标志。如果标志为 <code>true</code>，则测试 <code>Stroke</code> 轮廓。如果标志为 <code>false</code>，则测试填充的 <code>Shape</code>。
     * @return 如果有相交返回 <code>true</code>；否则返回 <code>false</code>。
     * @see #setStroke
     * @see #fill
     * @see #draw
     * @see #transform
     * @see #setTransform
     * @see #clip
     * @see #setClip
     */
    public abstract boolean hit(Rectangle rect,
                                Shape s,
                                boolean onStroke);

    /**
     * 返回与此 <code>Graphics2D</code> 关联的设备配置。
     * @return 此 <code>Graphics2D</code> 的设备配置。
     */
    public abstract GraphicsConfiguration getDeviceConfiguration();

    /**
     * 设置 <code>Graphics2D</code> 上下文的 <code>Composite</code>。此 <code>Composite</code> 用于所有绘制方法，如 <code>drawImage</code>、<code>drawString</code>、<code>draw</code> 和 <code>fill</code>。它指定了在渲染过程中新像素如何与图形设备上现有的像素结合。
     * <p>如果此 <code>Graphics2D</code> 上下文正在绘制到显示屏幕上的 <code>Component</code>，并且 <code>Composite</code> 是自定义对象而不是 <code>AlphaComposite</code> 类的实例，并且存在安全管理者，其 <code>checkPermission</code> 方法将被调用，权限为 <code>AWTPermission("readDisplayPixels")</code>。
     * @throws SecurityException 如果正在使用自定义 <code>Composite</code> 对象渲染到屏幕，并且设置了安全管理者，其 <code>checkPermission</code> 方法不允许此操作。
     * @param comp 要用于渲染的 <code>Composite</code> 对象
     * @see java.awt.Graphics#setXORMode
     * @see java.awt.Graphics#setPaintMode
     * @see #getComposite
     * @see AlphaComposite
     * @see SecurityManager#checkPermission
     * @see java.awt.AWTPermission
     */
    public abstract void setComposite(Composite comp);

    /**
     * 设置 <code>Graphics2D</code> 上下文的 <code>Paint</code> 属性。如果使用 <code>null</code> <code>Paint</code> 对象调用此方法，不会影响此 <code>Graphics2D</code> 的当前 <code>Paint</code> 属性。
     * @param paint 用于在渲染过程中生成颜色的 <code>Paint</code> 对象，或 <code>null</code>
     * @see java.awt.Graphics#setColor
     * @see #getPaint
     * @see GradientPaint
     * @see TexturePaint
     */
    public abstract void setPaint( Paint paint );

    /**
     * 设置 <code>Graphics2D</code> 上下文的 <code>Stroke</code>。
     * @param s 用于在渲染过程中描边 <code>Shape</code> 的 <code>Stroke</code> 对象
     * @see BasicStroke
     * @see #getStroke
     */
    public abstract void setStroke(Stroke s);

    /**
     * 设置渲染算法的单个偏好的值。提示类别包括对渲染质量和渲染过程中时间/质量权衡的控制。请参阅 <code>RenderingHints</code> 类以了解一些常见的键和值的定义。
     * @param hintKey 要设置的提示的键。
     * @param hintValue 表示指定提示类别偏好的值。
     * @see #getRenderingHint(RenderingHints.Key)
     * @see RenderingHints
     */
    public abstract void setRenderingHint(Key hintKey, Object hintValue);

    /**
     * 返回渲染算法的单个偏好的值。提示类别包括对渲染质量和渲染过程中时间/质量权衡的控制。请参阅 <code>RenderingHints</code> 类以了解一些常见的键和值的定义。
     * @param hintKey 对应于要获取的提示的键。
     * @return 一个表示指定提示键值的对象。一些键及其关联的值在 <code>RenderingHints</code> 类中定义。
     * @see RenderingHints
     * @see #setRenderingHint(RenderingHints.Key, Object)
     */
    public abstract Object getRenderingHint(Key hintKey);

    /**
     * 用指定的 <code>hints</code> 替换所有渲染算法偏好的值。现有的所有渲染提示值将被丢弃，并从指定的 {@link Map} 对象初始化新的已知提示和值。
     * 提示类别包括对渲染质量和渲染过程中时间/质量权衡的控制。请参阅 <code>RenderingHints</code> 类以了解一些常见的键和值的定义。
     * @param hints 要设置的渲染提示
     * @see #getRenderingHints
     * @see RenderingHints
     */
    public abstract void setRenderingHints(Map<?,?> hints);

    /**
     * 设置任意数量的渲染算法偏好的值。仅修改指定 <code>Map</code> 对象中存在的渲染提示的值。所有其他不在指定对象中的偏好保持不变。
     * 提示类别包括对渲染质量和渲染过程中时间/质量权衡的控制。请参阅 <code>RenderingHints</code> 类以了解一些常见的键和值的定义。
     * @param hints 要设置的渲染提示
     * @see RenderingHints
     */
    public abstract void addRenderingHints(Map<?,?> hints);

    /**
     * 获取渲染算法的偏好。提示类别包括对渲染质量和渲染过程中时间/质量权衡的控制。
     * 返回所有曾经在一个操作中指定的提示键/值对。请参阅 <code>RenderingHints</code> 类以了解一些常见的键和值的定义。
     * @return 包含当前偏好的 <code>RenderingHints</code> 实例的引用。
     * @see RenderingHints
     * @see #setRenderingHints(Map)
     */
    public abstract RenderingHints getRenderingHints();

    /**
     * 将 <code>Graphics2D</code> 上下文的原点平移到当前坐标系中的点 (<i>x</i>,&nbsp;<i>y</i>)。修改 <code>Graphics2D</code> 上下文，使其新的原点对应于 <code>Graphics2D</code> 上下文的前一个坐标系中的点 (<i>x</i>,&nbsp;<i>y</i>)。所有在此图形上下文上的后续渲染操作使用的坐标都相对于这个新的原点。
     * @param  x 指定的 x 坐标
     * @param  y 指定的 y 坐标
     * @since   JDK1.0
     */
    public abstract void translate(int x, int y);

    /**
     * 将当前 <code>Graphics2D</code> <code>Transform</code> 与平移变换组合。后续渲染相对于前一个位置平移指定的距离。这相当于调用 transform(T)，其中 T 是由以下矩阵表示的 <code>AffineTransform</code>：
     * <pre>
     *          [   1    0    tx  ]
     *          [   0    1    ty  ]
     *          [   0    0    1   ]
     * </pre>
     * @param tx 沿 x 轴平移的距离
     * @param ty 沿 y 轴平移的距离
     */
    public abstract void translate(double tx, double ty);

    /**
     * 将当前 <code>Graphics2D</code> <code>Transform</code> 与旋转变换组合。后续渲染相对于前一个原点按指定的弧度旋转。这相当于调用 <code>transform(R)</code>，其中 R 是由以下矩阵表示的 <code>AffineTransform</code>：
     * <pre>
     *          [   cos(theta)    -sin(theta)    0   ]
     *          [   sin(theta)     cos(theta)    0   ]
     *          [       0              0         1   ]
     * </pre>
     * 使用正角度 theta 旋转将正 x 轴上的点朝正 y 轴方向旋转。
     * @param theta 旋转的弧度
     */
    public abstract void rotate(double theta);

    /**
     * 将当前 <code>Graphics2D</code> <code>Transform</code> 与平移旋转变换组合。后续渲染通过构造的变换进行变换，该变换由平移到指定位置、按指定弧度旋转，然后按与原始平移相同量平移回原点组成。这相当于以下调用序列：
     * <pre>
     *          translate(x, y);
     *          rotate(theta);
     *          translate(-x, -y);
     * </pre>
     * 使用正角度 theta 旋转将正 x 轴上的点朝正 y 轴方向旋转。
     * @param theta 旋转的弧度
     * @param x 旋转的原点的 x 坐标
     * @param y 旋转的原点的 y 坐标
     */
    public abstract void rotate(double theta, double x, double y);

    /**
     * 将当前 <code>Graphics2D</code> <code>Transform</code> 与缩放变换组合。后续渲染根据指定的缩放因子相对于前一个缩放进行调整。这相当于调用 <code>transform(S)</code>，其中 S 是由以下矩阵表示的 <code>AffineTransform</code>：
     * <pre>
     *          [   sx   0    0   ]
     *          [   0    sy   0   ]
     *          [   0    0    1   ]
     * </pre>
     * @param sx 在后续渲染操作中，X 坐标相对于前一个渲染操作乘以的量。
     * @param sy 在后续渲染操作中，Y 坐标相对于前一个渲染操作乘以的量。
     */
    public abstract void scale(double sx, double sy);

    /**
     * 将当前 <code>Graphics2D</code> <code>Transform</code> 与剪切变换组合。后续渲染根据指定的乘数相对于前一个位置进行剪切。这相当于调用 <code>transform(SH)</code>，其中 SH 是由以下矩阵表示的 <code>AffineTransform</code>：
     * <pre>
     *          [   1   shx   0   ]
     *          [  shy   1    0   ]
     *          [   0    0    1   ]
     * </pre>
     * @param shx 作为 Y 坐标的函数，坐标沿正 X 轴方向平移的乘数
     * @param shy 作为 X 坐标的函数，坐标沿正 Y 轴方向平移的乘数
     */
    public abstract void shear(double shx, double shy);

    /**
     * 根据最后指定的优先级将 <code>AffineTransform</code> 对象与 <code>Graphics2D</code> 中的 <code>Transform</code> 组合。如果当前 <code>Transform</code> 为 Cx，与 Tx 组合的结果为新的 <code>Transform</code> Cx'。Cx' 成为 <code>Graphics2D</code> 的当前 <code>Transform</code>。将点 p 通过更新的 <code>Transform</code> Cx' 变换等同于首先将 p 通过 Tx 变换，然后将结果通过原始 <code>Transform</code> Cx 变换。换句话说，Cx'(p) = Cx(Tx(p))。必要时会复制 Tx，因此对 Tx 的进一步修改不会影响渲染。
     * @param Tx 要与当前 <code>Transform</code> 组合的 <code>AffineTransform</code> 对象
     * @see #setTransform
     * @see AffineTransform
     */
    public abstract void transform(AffineTransform Tx);

    /**
     * 覆盖 <code>Graphics2D</code> 上下文中的 <code>Transform</code>。
     * 警告：此方法不应用于在现有变换上应用新的坐标变换，因为 <code>Graphics2D</code> 可能已经有一个用于其他目的的变换，例如渲染 Swing 组件或应用调整打印机分辨率的缩放变换。
     * <p>要添加坐标变换，请使用 <code>transform</code>、<code>rotate</code>、<code>scale</code> 或 <code>shear</code> 方法。<code>setTransform</code> 方法仅用于在渲染后恢复原始的 <code>Graphics2D</code> 变换，如以下示例所示：
     * <pre>
     * // 获取当前变换
     * AffineTransform saveAT = g2.getTransform();
     * // 执行变换
     * g2d.transform(...);
     * // 渲染
     * g2d.draw(...);
     * // 恢复原始变换
     * g2d.setTransform(saveAT);
     * </pre>
     *
     * @param Tx 从 <code>getTransform</code> 方法检索的 <code>AffineTransform</code>
     * @see #transform
     * @see #getTransform
     * @see AffineTransform
     */
    public abstract void setTransform(AffineTransform Tx);


                /**
     * 返回当前 <code>Graphics2D</code> 上下文中的 <code>Transform</code> 的副本。
     * @return 当前 <code>Graphics2D</code> 上下文中的 <code>AffineTransform</code>。
     * @see #transform
     * @see #setTransform
     */
    public abstract AffineTransform getTransform();

    /**
     * 返回当前 <code>Graphics2D</code> 上下文中的 <code>Paint</code>。
     * @return 当前 <code>Graphics2D</code> 上下文中的 <code>Paint</code>，定义了颜色或图案。
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     */
    public abstract Paint getPaint();

    /**
     * 返回当前 <code>Graphics2D</code> 上下文中的 <code>Composite</code>。
     * @return 当前 <code>Graphics2D</code> 上下文中的 <code>Composite</code>，定义了合成样式。
     * @see #setComposite
     */
    public abstract Composite getComposite();

    /**
     * 设置 <code>Graphics2D</code> 上下文中的背景颜色。
     * 背景颜色用于清除区域。
     * 当为 <code>Component</code> 构造 <code>Graphics2D</code> 时，背景颜色从 <code>Component</code> 继承。
     * 在 <code>Graphics2D</code> 上下文中设置背景颜色仅影响随后的 <code>clearRect</code> 调用，而不影响 <code>Component</code> 的背景颜色。
     * 要更改 <code>Component</code> 的背景，请使用 <code>Component</code> 的适当方法。
     * @param color 用于随后 <code>clearRect</code> 调用的背景颜色。
     * @see #getBackground
     * @see java.awt.Graphics#clearRect
     */
    public abstract void setBackground(Color color);

    /**
     * 返回用于清除区域的背景颜色。
     * @return 当前 <code>Graphics2D</code> 上下文中的 <code>Color</code>，定义了背景颜色。
     * @see #setBackground
     */
    public abstract Color getBackground();

    /**
     * 返回当前 <code>Graphics2D</code> 上下文中的 <code>Stroke</code>。
     * @return 当前 <code>Graphics2D</code> 上下文中的 <code>Stroke</code>，定义了线条样式。
     * @see #setStroke
     */
    public abstract Stroke getStroke();

    /**
     * 将当前 <code>Clip</code> 与指定的 <code>Shape</code> 的内部相交，并将 <code>Clip</code> 设置为结果的交集。
     * 指定的 <code>Shape</code> 在与当前 <code>Clip</code> 相交之前，会使用当前 <code>Graphics2D</code> 的 <code>Transform</code> 进行变换。
     * 此方法用于使当前 <code>Clip</code> 变小。
     * 要使 <code>Clip</code> 变大，使用 <code>setClip</code>。
     * 通过此方法修改的 <i>用户剪辑</i> 独立于与设备边界和可见性相关的剪辑。
     * 如果之前未设置剪辑，或者使用 <code>setClip</code> 传递 <code>null</code> 参数清除了剪辑，则指定的 <code>Shape</code> 成为新的用户剪辑。
     * @param s 要与当前 <code>Clip</code> 相交的 <code>Shape</code>。如果 <code>s</code> 为 <code>null</code>，此方法将清除当前 <code>Clip</code>。
     */
     public abstract void clip(Shape s);

     /**
     * 获取此 <code>Graphics2D</code> 上下文中 <code>Font</code> 的渲染上下文。
     * {@link FontRenderContext} 封装了应用程序提示，如抗锯齿和分数度量，以及目标设备特定的信息，如每英寸点数。
     * 当使用执行排版格式化的对象（如 <code>Font</code> 和 <code>TextLayout</code>）时，应提供这些信息。
     * 当应用程序执行自己的布局并需要在应用了各种渲染提示后准确测量字形的各种特征（如前进量和行高）时，也应提供这些信息。
     *
     * @return <code>FontRenderContext</code> 实例的引用。
     * @see java.awt.font.FontRenderContext
     * @see java.awt.Font#createGlyphVector
     * @see java.awt.font.TextLayout
     * @since     1.2
     */

    public abstract FontRenderContext getFontRenderContext();

}
