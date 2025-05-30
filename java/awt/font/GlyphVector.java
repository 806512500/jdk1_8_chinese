
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

/*
 * @author Charlton Innovations, Inc.
 */

package java.awt.font;

import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Polygon;        // 提醒 - 需要一个浮点版本
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.Shape;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphJustificationInfo;

/**
 * <code>GlyphVector</code> 对象是一个包含几何信息的字形集合，这些几何信息用于在转换后的坐标空间中放置每个字形，该坐标空间对应于 <code>GlyphVector</code> 最终显示的设备。
 * <p>
 * <code>GlyphVector</code> 不尝试解释其包含的字形序列。相邻字形之间的关系仅用于确定字形在视觉坐标空间中的放置。
 * <p>
 * <code>GlyphVector</code> 的实例由 {@link Font} 创建。
 * <p>
 * 在可以缓存文本中间表示的文本处理应用程序中，创建并随后缓存 <code>GlyphVector</code> 以在渲染期间使用是向用户呈现字符视觉表示的最快方法。
 * <p>
 * <code>GlyphVector</code> 与一个 <code>Font</code> 关联，并且只能在与该 <code>Font</code> 相关的情况下提供有用的数据。此外，从 <code>GlyphVector</code> 获得的度量通常不是几何可缩放的，因为像素化和间距依赖于 <code>Font</code> 中的网格拟合算法。为了准确测量 <code>GlyphVector</code> 及其组成字形，必须在创建 <code>GlyphVector</code> 时指定缩放变换、抗锯齿模式和分数度量模式。这些特性可以从目标设备中派生。
 * <p>
 * 对于 <code>GlyphVector</code> 中的每个字形，您可以获得：
 * <ul>
 * <li>字形的位置
 * <li>与字形关联的变换
 * <li>在 <code>GlyphVector</code> 上下文中字形的度量。字形的度量在不同的变换、应用程序指定的渲染提示以及 <code>GlyphVector</code> 中字形的具体实例下可能不同。
 * </ul>
 * <p>
 * 改变用于创建 <code>GlyphVector</code> 的数据不会改变 <code>GlyphVector</code> 的状态。
 * <p>
 * 提供了调整 <code>GlyphVector</code> 中字形位置的方法。这些方法最适合于执行字形呈现的对齐操作的应用程序。
 * <p>
 * 提供了变换 <code>GlyphVector</code> 中单个字形的方法。这些方法主要用于特殊效果。
 * <p>
 * 提供了返回整个 <code>GlyphVector</code> 或 <code>GlyphVector</code> 中单个字形的视觉、逻辑和像素边界的方法。
 * <p>
 * 提供了返回 <code>GlyphVector</code> 和 <code>GlyphVector</code> 中单个字形的 {@link Shape} 的方法。
 * @see Font
 * @see GlyphMetrics
 * @see TextLayout
 * @author Charlton Innovations, Inc.
 */

public abstract class GlyphVector implements Cloneable {

    //
    // 与创建时状态相关的方法
    //

    /**
     * 返回与此 <code>GlyphVector</code> 关联的 <code>Font</code>。
     * @return 用于创建此 <code>GlyphVector</code> 的 <code>Font</code>。
     * @see Font
     */
    public abstract Font getFont();

    /**
     * 返回与此 <code>GlyphVector</code> 关联的 {@link FontRenderContext}。
     * @return 用于创建此 <code>GlyphVector</code> 的 <code>FontRenderContext</code>。
     * @see FontRenderContext
     * @see Font
     */
    public abstract FontRenderContext getFontRenderContext();

    //
    // 与整个 <code>GlyphVector</code> 相关的方法
    //

    /**
     * 为 <code>GlyphVector</code> 中的每个字形分配默认位置。这可能会破坏在初始布局此 <code>GlyphVector</code> 时生成的信息。
     */
    public abstract void performDefaultLayout();

    /**
     * 返回此 <code>GlyphVector</code> 中的字形数量。
     * @return 此 <code>GlyphVector</code> 中的字形数量。
     */
    public abstract int getNumGlyphs();

    /**
     * 返回指定字形的字形代码。
     * 该返回值对创建此 <code>GlyphVector</code> 的 <code>Font</code> 对象之外的任何对象都没有意义。
     * @param glyphIndex 对应于要检索字形代码的字形的索引。
     * @return 指定 <code>glyphIndex</code> 处的字形的字形代码。
     * @throws IndexOutOfBoundsException 如果 <code>glyphIndex</code> 小于 0 或大于或等于此 <code>GlyphVector</code> 中的字形数量
     */
    public abstract int getGlyphCode(int glyphIndex);

    /**
     * 返回指定字形的字形代码数组。
     * 该返回值的内容对创建此 <code>GlyphVector</code> 的 <code>Font</code> 之外的任何对象都没有意义。此方法用于处理字形代码时的方便和性能。如果没有传递数组，则创建新数组。
     * @param beginGlyphIndex 开始检索字形代码的索引。
     * @param numEntries 要检索的字形代码数量。
     * @param codeReturn 接收字形代码并返回的数组。
     * @return 指定字形的字形代码数组。
     * @throws IllegalArgumentException 如果 <code>numEntries</code> 小于 0
     * @throws IndexOutOfBoundsException 如果 <code>beginGlyphIndex</code> 小于 0
     * @throws IndexOutOfBoundsException 如果 <code>beginGlyphIndex</code> 和 <code>numEntries</code> 的和大于此 <code>GlyphVector</code> 中的字形数量
     */
    public abstract int[] getGlyphCodes(int beginGlyphIndex, int numEntries,
                                        int[] codeReturn);

    /**
     * 返回指定字形的字符索引。
     * 字符索引是字形表示的第一个逻辑字符的索引。默认实现假设字形与字符之间是一对一、从左到右的映射。
     * @param glyphIndex 字形的索引。
     * @return 字形表示的第一个字符的索引。
     * @since 1.4
     */
    public int getGlyphCharIndex(int glyphIndex) {
        return glyphIndex;
    }

    /**
     * 返回指定字形的字符索引数组。
     * 字符索引是字形表示的第一个逻辑字符的索引。索引按字形顺序返回。默认实现调用每个字形的 getGlyphCharIndex，子类可能希望为了性能原因重写此实现。使用此方法处理字形代码时方便且高效。如果没有传递数组，则创建新数组。
     * @param beginGlyphIndex 第一个字形的索引。
     * @param numEntries 字形索引的数量。
     * @param codeReturn 接收字符索引的数组。
     * @return 每个字形的字符索引数组。
     * @since 1.4
     */
    public int[] getGlyphCharIndices(int beginGlyphIndex, int numEntries,
                                     int[] codeReturn) {
        if (codeReturn == null) {
            codeReturn = new int[numEntries];
        }
        for (int i = 0, j = beginGlyphIndex; i < numEntries; ++i, ++j) {
            codeReturn[i] = getGlyphCharIndex(j);
        }
        return codeReturn;
     }

    /**
     * 返回此 <code>GlyphVector</code> 的逻辑边界。
     * 此方法用于将此 <code>GlyphVector</code> 与其他视觉上相邻的 <code>GlyphVector</code> 对象进行定位。
     * @return 此 <code>GlyphVector</code> 的逻辑边界。
     */
    public abstract Rectangle2D getLogicalBounds();

    /**
     * 返回此 <code>GlyphVector</code> 的视觉边界。
     * 视觉边界是此 <code>GlyphVector</code> 的轮廓的边界框。由于光栅化和像素对齐，此框可能不包含渲染此 <code>GlyphVector</code> 时受影响的所有像素。
     * @return 此 <code>GlyphVector</code> 的边界框。
     */
    public abstract Rectangle2D getVisualBounds();

    /**
     * 返回在给定 <code>FontRenderContext</code> 下于指定位置渲染此 <code>GlyphVector</code> 时的像素边界。renderFRC 不必与 <code>GlyphVector</code> 的 <code>FontRenderContext</code> 相同，可以为 null。如果为 null，则使用此 <code>GlyphVector</code> 的 <code>FontRenderContext</code>。默认实现返回视觉边界，偏移到 x, y 并四舍五入到下一个整数值（即返回一个包含视觉边界的整数矩形），并忽略 FRC。子类应重写此方法。
     * @param renderFRC <code>Graphics</code> 的 <code>FontRenderContext</code>。
     * @param x 渲染此 <code>GlyphVector</code> 的 x 坐标。
     * @param y 渲染此 <code>GlyphVector</code> 的 y 坐标。
     * @return 一个包围受影响像素的 <code>Rectangle</code>。
     * @since 1.4
     */
    public Rectangle getPixelBounds(FontRenderContext renderFRC, float x, float y) {
                Rectangle2D rect = getVisualBounds();
                int l = (int)Math.floor(rect.getX() + x);
                int t = (int)Math.floor(rect.getY() + y);
                int r = (int)Math.ceil(rect.getMaxX() + x);
                int b = (int)Math.ceil(rect.getMaxY() + y);
                return new Rectangle(l, t, r - l, b - t);
        }


    /**
     * 返回一个 <code>Shape</code>，其内部对应于此 <code>GlyphVector</code> 的视觉表示。
     * @return 一个 <code>Shape</code>，其内部对应于此 <code>GlyphVector</code> 的轮廓。
     */
    public abstract Shape getOutline();

    /**
     * 返回一个 <code>Shape</code>，其内部对应于此 <code>GlyphVector</code> 在 x, y 处渲染时的视觉表示。
     * @param x 此 <code>GlyphVector</code> 的 X 坐标。
     * @param y 此 <code>GlyphVector</code> 的 Y 坐标。
     * @return 一个 <code>Shape</code>，其内部对应于此 <code>GlyphVector</code> 在指定坐标处渲染时的轮廓。
     */
    public abstract Shape getOutline(float x, float y);

    /**
     * 返回一个 <code>Shape</code>，其内部对应于此 <code>GlyphVector</code> 中指定字形的视觉表示。
     * 通过此方法返回的轮廓位于每个单独字形的原点周围。
     * @param glyphIndex 此 <code>GlyphVector</code> 中的索引。
     * @return 一个 <code>Shape</code>，其内部对应于此 <code>GlyphVector</code> 中指定 <code>glyphIndex</code> 处的字形的轮廓。
     * @throws IndexOutOfBoundsException 如果 <code>glyphIndex</code> 小于 0 或大于或等于此 <code>GlyphVector</code> 中的字形数量
     */
    public abstract Shape getGlyphOutline(int glyphIndex);

    /**
     * 返回一个 <code>Shape</code>，其内部对应于此 <code>GlyphVector</code> 中指定字形在 x, y 处渲染时的视觉表示。
     * 通过此方法返回的轮廓位于每个单独字形的原点周围。
     * @param glyphIndex 此 <code>GlyphVector</code> 中的索引。
     * @param x 此 <code>GlyphVector</code> 的 X 坐标。
     * @param y 此 <code>GlyphVector</code> 的 Y 坐标。
     * @return 一个 <code>Shape</code>，其内部对应于此 <code>GlyphVector</code> 中指定 <code>glyphIndex</code> 处的字形在指定坐标处渲染时的轮廓。
     * @throws IndexOutOfBoundsException 如果 <code>glyphIndex</code> 小于 0 或大于或等于此 <code>GlyphVector</code> 中的字形数量
     * @since 1.4
     */
    public Shape getGlyphOutline(int glyphIndex, float x, float y) {
        Shape s = getGlyphOutline(glyphIndex);
        AffineTransform at = AffineTransform.getTranslateInstance(x,y);
        return at.createTransformedShape(s);
        }

    /**
     * 返回指定字形相对于此 <code>GlyphVector</code> 原点的位置。
     * 如果 <code>glyphIndex</code> 等于此 <code>GlyphVector</code> 中的字形数量，此方法返回最后一个字形之后的位置。此位置用于定义整个 <code>GlyphVector</code> 的前进距离。
     * @param glyphIndex 此 <code>GlyphVector</code> 中的索引。
     * @return 一个 {@link Point2D} 对象，表示指定 <code>glyphIndex</code> 处的字形的位置。
     * @throws IndexOutOfBoundsException 如果 <code>glyphIndex</code> 小于 0 或大于此 <code>GlyphVector</code> 中的字形数量
     * @see #setGlyphPosition
     */
    public abstract Point2D getGlyphPosition(int glyphIndex);


                /**
     * 设置指定字形在此
     * <code>GlyphVector</code> 中的位置。
     * 如果 <code>glyphIndex</code> 等于此
     * <code>GlyphVector</code> 中的字形数量，此方法设置最后一个字形之后的位置。此位置用于定义
     * 整个 <code>GlyphVector</code> 的前进距离。
     * @param glyphIndex 此 <code>GlyphVector</code> 的索引
     * @param newPos 用于定位指定 <code>glyphIndex</code> 处字形的 <code>Point2D</code>
     * @throws IndexOutOfBoundsException 如果 <code>glyphIndex</code>
     *   小于 0 或大于此 <code>GlyphVector</code> 中的字形数量
     * @see #getGlyphPosition
     */
    public abstract void setGlyphPosition(int glyphIndex, Point2D newPos);

    /**
     * 返回指定字形在此
     * <code>GlyphVector</code> 中的变换。变换相对于字形位置。如果没有应用特殊变换，
     * 可以返回 <code>null</code>。返回 <code>null</code> 表示恒等变换。
     * @param glyphIndex 此 <code>GlyphVector</code> 的索引
     * @return 一个 {@link AffineTransform}，表示指定 <code>glyphIndex</code> 处字形的变换。
     * @throws IndexOutOfBoundsException 如果 <code>glyphIndex</code>
     *   小于 0 或大于等于此 <code>GlyphVector</code> 中的字形数量
     * @see #setGlyphTransform
     */
    public abstract AffineTransform getGlyphTransform(int glyphIndex);

    /**
     * 设置指定字形在此
     * <code>GlyphVector</code> 中的变换。变换相对于字形位置。对于 <code>newTX</code>
     * 的 <code>null</code> 参数表示不为指定字形应用特殊变换。
     * 此方法可用于旋转、镜像、平移和缩放字形。添加变换可能会导致性能显著变化。
     * @param glyphIndex 此 <code>GlyphVector</code> 的索引
     * @param newTX 指定 <code>glyphIndex</code> 处字形的新变换
     * @throws IndexOutOfBoundsException 如果 <code>glyphIndex</code>
     *   小于 0 或大于等于此 <code>GlyphVector</code> 中的字形数量
     * @see #getGlyphTransform
     */
    public abstract void setGlyphTransform(int glyphIndex, AffineTransform newTX);

    /**
     * 返回描述 GlyphVector 全局状态的标志。未在下方描述的标志保留。默认
     * 实现对于位置调整、变换、rtl 和复杂标志返回 0（表示 false）。
     * 子类应重写此方法，并确保它正确描述 GlyphVector 且与相关调用的结果对应。
     * @return 一个包含描述状态的标志的 int
     * @see #FLAG_HAS_POSITION_ADJUSTMENTS
     * @see #FLAG_HAS_TRANSFORMS
     * @see #FLAG_RUN_RTL
     * @see #FLAG_COMPLEX_GLYPHS
     * @see #FLAG_MASK
     * @since 1.4
     */
    public int getLayoutFlags() {
                return 0;
        }

    /**
     * 一个与 getLayoutFlags 一起使用的标志，表示此 <code>GlyphVector</code> 具有
     * 每个字形的变换。
     * @since 1.4
     */
    public static final int FLAG_HAS_TRANSFORMS = 1;

    /**
     * 一个与 getLayoutFlags 一起使用的标志，表示此 <code>GlyphVector</code> 具有
     * 位置调整。当此标志为 true 时，字形位置不匹配字形的默认累积前进距离（例如，如果进行了字距调整）。
     * @since 1.4
     */
    public static final int FLAG_HAS_POSITION_ADJUSTMENTS = 2;

    /**
     * 一个与 getLayoutFlags 一起使用的标志，表示此 <code>GlyphVector</code> 具有
     * 从右到左的运行方向。这指的是字形到字符的映射，不意味着字形的视觉位置一定是这种顺序，
     * 尽管通常它们会是。
     * @since 1.4
     */
    public static final int FLAG_RUN_RTL = 4;

    /**
     * 一个与 getLayoutFlags 一起使用的标志，表示此 <code>GlyphVector</code> 具有
     * 复杂的字形到字符映射（不以严格升序或降序一对一映射字形到字符，匹配运行方向）。
     * @since 1.4
     */
    public static final int FLAG_COMPLEX_GLYPHS = 8;

    /**
     * 一个用于 getLayoutFlags 支持的标志的掩码。只有掩码覆盖的位应进行测试。
     * @since 1.4
     */
    public static final int FLAG_MASK =
        FLAG_HAS_TRANSFORMS |
        FLAG_HAS_POSITION_ADJUSTMENTS |
        FLAG_RUN_RTL |
        FLAG_COMPLEX_GLYPHS;

    /**
     * 返回指定字形的字形位置数组。此方法用于处理字形位置时的方便和性能。
     * 如果没有传递数组，则创建新数组。
     * 从位置零开始的偶数编号的数组条目是编号为 <code>beginGlyphIndex + position/2</code> 的字形的 X
     * 坐标。从位置一开始的奇数编号的数组条目是编号为 <code>beginGlyphIndex + (position-1)/2</code> 的字形的 Y
     * 坐标。如果 <code>beginGlyphIndex</code> 等于此
     * <code>GlyphVector</code> 中的字形数量，此方法获取最后一个字形之后的位置，此位置用于定义
     * 整个 <code>GlyphVector</code> 的前进距离。
     * @param beginGlyphIndex 开始检索字形位置的索引
     * @param numEntries 要检索的字形数量
     * @param positionReturn 接收字形位置的数组，然后返回。
     * @return 由 <code>beginGlyphIndex</code> 和 <code>numEntries</code> 指定的字形位置数组。
     * @throws IllegalArgumentException 如果 <code>numEntries</code> 小于 0
     * @throws IndexOutOfBoundsException 如果 <code>beginGlyphIndex</code> 小于 0
     * @throws IndexOutOfBoundsException 如果 <code>beginGlyphIndex</code> 和 <code>numEntries</code>
     *   的和大于此 <code>GlyphVector</code> 中的字形数量加一
     */
    public abstract float[] getGlyphPositions(int beginGlyphIndex, int numEntries,
                                              float[] positionReturn);

    /**
     * 返回指定字形在此
     * <code>GlyphVector</code> 中的逻辑边界。
     * 这些逻辑边界总共有四条边，其中两条边在字形变换后的基线下方平行，另外两条边与相邻字形共享（如果存在）。此
     * 方法用于指定字形的命中测试、在字形的前缘或后缘定位光标，以及绘制围绕指定字形的高亮区域。
     * @param glyphIndex 对应于要检索其逻辑边界的字形的此 <code>GlyphVector</code> 的索引
     * @return 一个 <code>Shape</code>，表示指定 <code>glyphIndex</code> 处字形的逻辑边界。
     * @throws IndexOutOfBoundsException 如果 <code>glyphIndex</code>
     *   小于 0 或大于等于此 <code>GlyphVector</code> 中的字形数量
     * @see #getGlyphVisualBounds
     */
    public abstract Shape getGlyphLogicalBounds(int glyphIndex);

    /**
     * 返回指定字形在 <code>GlyphVector</code> 中的视觉边界。
     * 此方法返回的边界围绕每个单独字形的原点。
     * @param glyphIndex 对应于要检索其视觉边界的字形的此 <code>GlyphVector</code> 的索引
     * @return 一个 <code>Shape</code>，表示指定 <code>glyphIndex</code> 处字形的视觉边界。
     * @throws IndexOutOfBoundsException 如果 <code>glyphIndex</code>
     *   小于 0 或大于等于此 <code>GlyphVector</code> 中的字形数量
     * @see #getGlyphLogicalBounds
     */
    public abstract Shape getGlyphVisualBounds(int glyphIndex);

    /**
     * 返回当此 <code>GlyphVector</code> 在给定的 <code>FontRenderContext</code> 和位置
     * 渲染到 <code>Graphics</code> 时，索引处字形的像素边界。renderFRC 不必与此
     * <code>GlyphVector</code> 的 <code>FontRenderContext</code> 相同，可以为 null。如果为 null，则使用
     * 此 <code>GlyphVector</code> 的 <code>FontRenderContext</code>。默认实现返回字形的视觉边界，
     * 偏移到 x, y 并四舍五入到下一个整数值，并忽略 FRC。子类应重写此方法。
     * @param index 字形的索引。
     * @param renderFRC <code>Graphics</code> 的 <code>FontRenderContext</code>。
     * @param x 渲染此 <code>GlyphVector</code> 的 X 位置。
     * @param y 渲染此 <code>GlyphVector</code> 的 Y 位置。
     * @return 一个 <code>Rectangle</code>，包围可能受影响的像素。
     * @since 1.4
     */
    public Rectangle getGlyphPixelBounds(int index, FontRenderContext renderFRC, float x, float y) {
                Rectangle2D rect = getGlyphVisualBounds(index).getBounds2D();
                int l = (int)Math.floor(rect.getX() + x);
                int t = (int)Math.floor(rect.getY() + y);
                int r = (int)Math.ceil(rect.getMaxX() + x);
                int b = (int)Math.ceil(rect.getMaxY() + y);
                return new Rectangle(l, t, r - l, b - t);
        }

    /**
     * 返回指定索引处字形的度量信息。
     * @param glyphIndex 对应于要检索其度量信息的字形的此 <code>GlyphVector</code> 的索引
     * @return 一个 {@link GlyphMetrics} 对象，表示指定 <code>glyphIndex</code> 处字形的度量信息。
     * @throws IndexOutOfBoundsException 如果 <code>glyphIndex</code>
     *   小于 0 或大于等于此 <code>GlyphVector</code> 中的字形数量
     */
    public abstract GlyphMetrics getGlyphMetrics(int glyphIndex);

    /**
     * 返回指定索引处字形的对齐信息。
     * @param glyphIndex 对应于要检索其对齐属性的字形的此 <code>GlyphVector</code> 的索引
     * @return 一个 {@link GlyphJustificationInfo} 对象，表示指定 <code>glyphIndex</code> 处字形的对齐属性。
     * @throws IndexOutOfBoundsException 如果 <code>glyphIndex</code>
     *   小于 0 或大于等于此 <code>GlyphVector</code> 中的字形数量
     */
    public abstract GlyphJustificationInfo getGlyphJustificationInfo(int glyphIndex);

    //
    // 通用实用方法
    //

    /**
     * 测试指定的 <code>GlyphVector</code> 是否与此 <code>GlyphVector</code> 完全相等。
     * @param set 要测试的指定 <code>GlyphVector</code>
     * @return 如果指定的 <code>GlyphVector</code> 等于此 <code>GlyphVector</code>，则返回 <code>true</code>；
     *   否则返回 <code>false</code>。
     */
    public abstract boolean equals(GlyphVector set);
}
