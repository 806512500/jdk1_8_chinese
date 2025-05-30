
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.image.ColorModel;
import java.lang.annotation.Native;
import sun.java2d.SunCompositeContext;

/**
 * <code>AlphaComposite</code> 类实现了基本的 alpha 组合规则，用于将源颜色和目标颜色组合以实现混合和透明效果。
 * 该类实现的具体规则是 T. Porter 和 T. Duff 在 "Compositing Digital Images" (SIGGRAPH 84, 253-259) 中描述的基本 12 条规则。
 * 本文档的其余部分假设读者对论文中定义的概念和定义有所了解。
 *
 * <p>
 * 该类扩展了 Porter 和 Duff 定义的标准方程，增加了一个额外的因素。
 * <code>AlphaComposite</code> 类的实例可以包含一个 alpha 值，该值用于在混合方程中修改每个源像素的不透明度或覆盖率。
 *
 * <p>
 * 需要注意的是，Porter 和 Duff 论文中定义的所有方程都假设颜色分量已经由其对应的 alpha 分量预乘。
 * 由于 <code>ColorModel</code> 和 <code>Raster</code> 类允许以预乘或非预乘形式存储像素数据，所有输入数据在应用方程之前必须规范化为预乘形式，所有结果可能需要调整回目标所需的形式，然后再存储像素值。
 *
 * <p>
 * 还需注意，该类仅定义了组合颜色和 alpha 值的方程，从纯粹的数学意义上讲。其方程的准确应用取决于数据从源中检索和在目标中存储的方式。
 * 有关更多信息，请参见 <a href="#caveats">实现注意事项</a>。
 *
 * <p>
 * 以下因素用于描述 Porter 和 Duff 论文中的混合方程：
 *
 * <blockquote>
 * <table summary="layout">
 * <tr><th align=left>因素&nbsp;&nbsp;<th align=left>定义
 * <tr><td><em>A<sub>s</sub></em><td>源像素的 alpha 分量
 * <tr><td><em>C<sub>s</sub></em><td>源像素的预乘颜色分量
 * <tr><td><em>A<sub>d</sub></em><td>目标像素的 alpha 分量
 * <tr><td><em>C<sub>d</sub></em><td>目标像素的预乘颜色分量
 * <tr><td><em>F<sub>s</sub></em><td>源像素对输出的贡献比例
 * <tr><td><em>F<sub>d</sub></em><td>目标像素对输出的贡献比例
 * <tr><td><em>A<sub>r</sub></em><td>结果的 alpha 分量
 * <tr><td><em>C<sub>r</sub></em><td>结果的预乘颜色分量
 * </table>
 * </blockquote>
 *
 * <p>
 * 使用这些因素，Porter 和 Duff 定义了 12 种选择混合因子 <em>F<sub>s</sub></em> 和 <em>F<sub>d</sub></em> 的方法，以产生 12 种理想的视觉效果。
 * 确定 <em>F<sub>s</sub></em> 和 <em>F<sub>d</sub></em> 的方程在描述 12 个静态字段的文档中给出，这些字段指定了视觉效果。
 * 例如，
 * <a href="#SRC_OVER"><code>SRC_OVER</code></a>
 * 的描述指定了 <em>F<sub>s</sub></em> = 1 和 <em>F<sub>d</sub></em> = (1-<em>A<sub>s</sub></em>)。
 * 一旦确定了一组用于确定混合因子的方程，就可以将它们应用于每个像素以产生结果，使用以下方程：
 *
 * <pre>
 *      <em>F<sub>s</sub></em> = <em>f</em>(<em>A<sub>d</sub></em>)
 *      <em>F<sub>d</sub></em> = <em>f</em>(<em>A<sub>s</sub></em>)
 *      <em>A<sub>r</sub></em> = <em>A<sub>s</sub></em>*<em>F<sub>s</sub></em> + <em>A<sub>d</sub></em>*<em>F<sub>d</sub></em>
 *      <em>C<sub>r</sub></em> = <em>C<sub>s</sub></em>*<em>F<sub>s</sub></em> + <em>C<sub>d</sub></em>*<em>F<sub>d</sub></em></pre>
 *
 * <p>
 * 以下因素将用于讨论我们对 Porter 和 Duff 论文中的混合方程的扩展：
 *
 * <blockquote>
 * <table summary="layout">
 * <tr><th align=left>因素&nbsp;&nbsp;<th align=left>定义
 * <tr><td><em>C<sub>sr</sub></em> <td>源像素的原始颜色分量之一
 * <tr><td><em>C<sub>dr</sub></em> <td>目标像素的原始颜色分量之一
 * <tr><td><em>A<sub>ac</sub></em>  <td><code>AlphaComposite</code> 实例的“额外”alpha 分量
 * <tr><td><em>A<sub>sr</sub></em> <td>源像素的原始 alpha 分量
 * <tr><td><em>A<sub>dr</sub></em><td>目标像素的原始 alpha 分量
 * <tr><td><em>A<sub>df</sub></em> <td>存储在目标中的最终 alpha 分量
 * <tr><td><em>C<sub>df</sub></em> <td>存储在目标中的最终原始颜色分量
 * </table>
 *</blockquote>
 *
 * <h3>准备输入</h3>
 *
 * <p>
 * <code>AlphaComposite</code> 类定义了一个额外的 alpha 值，该值应用于源 alpha。
 * 这个值的应用方式类似于首先将源像素与一个具有指定 alpha 的像素应用 SRC_IN 规则，通过将原始源 alpha 和原始源颜色乘以 <code>AlphaComposite</code> 中的 alpha 来实现。
 * 这导致了以下方程，用于生成 Porter 和 Duff 混合方程中使用的 alpha：
 *
 * <pre>
 *      <em>A<sub>s</sub></em> = <em>A<sub>sr</sub></em> * <em>A<sub>ac</sub></em> </pre>
 *
 * 所有原始源颜色分量都需要乘以 <code>AlphaComposite</code> 实例中的 alpha。
 * 此外，如果源不是预乘形式，颜色分量还需要乘以源 alpha。
 * 因此，生成用于 Porter 和 Duff 方程的源颜色分量的方程取决于源像素是否预乘：
 *
 * <pre>
 *      <em>C<sub>s</sub></em> = <em>C<sub>sr</sub></em> * <em>A<sub>sr</sub></em> * <em>A<sub>ac</sub></em>     (如果源不是预乘形式)
 *      <em>C<sub>s</sub></em> = <em>C<sub>sr</sub></em> * <em>A<sub>ac</sub></em>           (如果源是预乘形式) </pre>
 *
 * 目标 alpha 无需调整：
 *
 * <pre>
 *      <em>A<sub>d</sub></em> = <em>A<sub>dr</sub></em> </pre>
 *
 * <p>
 * 如果目标不是预乘形式，目标颜色分量需要调整：
 *
 * <pre>
 *      <em>C<sub>d</sub></em> = <em>C<sub>dr</sub></em> * <em>A<sub>d</sub></em>    (如果目标不是预乘形式)
 *      <em>C<sub>d</sub></em> = <em>C<sub>dr</sub></em>         (如果目标是预乘形式) </pre>
 *
 * <h3>应用混合方程</h3>
 *
 * <p>
 * 调整后的 <em>A<sub>s</sub></em>、<em>A<sub>d</sub></em>、<em>C<sub>s</sub></em> 和 <em>C<sub>d</sub></em> 用于标准的 Porter 和 Duff 方程，以计算混合因子 <em>F<sub>s</sub></em> 和 <em>F<sub>d</sub></em>，然后计算结果的预乘分量 <em>A<sub>r</sub></em> 和 <em>C<sub>r</sub></em>。
 *
 * <h3>准备结果</h3>
 *
 * <p>
 * 如果结果将存储回不存储非预乘数据的目标缓冲区中，结果需要调整，使用以下方程：
 *
 * <pre>
 *      <em>A<sub>df</sub></em> = <em>A<sub>r</sub></em>
 *      <em>C<sub>df</sub></em> = <em>C<sub>r</sub></em>                 (如果目标是预乘形式)
 *      <em>C<sub>df</sub></em> = <em>C<sub>r</sub></em> / <em>A<sub>r</sub></em>            (如果目标不是预乘形式) </pre>
 *
 * 注意，如果结果 alpha 为零，除法是未定义的，因此在这种情况下省略除法以避免“除以零”，颜色分量保持全零。
 *
 * <h3>性能考虑</h3>
 *
 * <p>
 * 为了性能原因，传递给由 <code>AlphaComposite</code> 类创建的 <code>CompositeContext</code> 对象的 <code>compose</code> 方法的 <code>Raster</code> 对象最好存储预乘数据。
 * 如果源 <code>Raster</code> 或目标 <code>Raster</code> 不是预乘形式，适当的转换将在合成操作前后进行。
 *
 * <h3><a name="caveats">实现注意事项</a></h3>
 *
 * <ul>
 * <li>
 * 许多源，如 <code>BufferedImage</code> 类中列出的一些不透明图像类型，不为它们的像素存储 alpha 值。这样的源为所有像素提供 1.0 的 alpha 值。
 *
 * <li>
 * 许多目标也没有地方存储由该类执行的混合计算产生的 alpha 值。这样的目标因此隐式地丢弃了该类产生的 alpha 值。
 * 建议这样的目标应将其存储的颜色值视为非预乘，并在存储颜色值和丢弃 alpha 值之前，将结果颜色值除以结果 alpha 值。
 *
 * <li>
 * 结果的准确性取决于像素在目标中的存储方式。
 * 提供每个颜色和 alpha 分量至少 8 位存储的图像格式至少足以用作一系列几个到十几个合成操作的目标。
 * 存储每个分量少于 8 位的图像格式在进行一两个合成操作后，舍入误差就会主导结果，因此用途有限。
 * 不单独存储颜色分量的图像格式不适合任何类型的半透明混合。
 * 例如，<code>BufferedImage.TYPE_BYTE_INDEXED</code> 不应用于混合操作，因为每次操作都可能引入大量误差，因为需要从有限的调色板中选择一个像素来匹配混合方程的结果。
 *
 * <li>
 * 几乎所有格式都以离散整数而不是上述参考方程中使用的浮点值存储像素。
 * 实现可以将整数像素值缩放到 0.0 到 1.0 的浮点值范围，或者使用稍微修改的方程，这些方程完全在整数域中操作，但产生与参考方程类似的结果。
 *
 * <p>
 * 通常，整数值与浮点值的关系是，整数 0 等于浮点值 0.0，整数 2^<em>n</em>-1（其中 <em>n</em> 是表示的位数）等于 1.0。
 * 对于 8 位表示，这意味着 0x00 表示 0.0，0xff 表示 1.0。
 *
 * <li>
 * 内部实现可以近似某些方程，并且可以消除一些步骤以避免不必要的操作。
 * 例如，考虑一个使用 8 位每分量存储的非预乘 alpha 值的离散整数图像。
 * 几乎透明的深红色的存储值可能是：
 *
 * <pre>
 *    (A, R, G, B) = (0x01, 0xb0, 0x00, 0x00)</pre>
 *
 * <p>
 * 如果使用整数数学，并且这个值以 <a href="#SRC"><code>SRC</code></a> 模式与没有额外 alpha 的值进行合成，那么数学计算将表明结果是（以整数格式）：
 *
 * <pre>
 *    (A, R, G, B) = (0x01, 0x01, 0x00, 0x00)</pre>
 *
 * <p>
 * 注意，中间值总是以预乘形式存在，这使得整数红色分量只能是 0x00 或 0x01。当我们将这个结果存储回非预乘的目标时，除以 alpha 会给我们很少的选择来确定非预乘的红色值。
 * 在这种情况下，使用整数空间进行数学计算而不使用捷径的实现可能会最终得到最终的像素值：
 *
 * <pre>
 *    (A, R, G, B) = (0x01, 0xff, 0x00, 0x00)</pre>
 *
 * <p>
 * （注意 0x01 除以 0x01 得到 1.0，这相当于 8 位存储格式中的 0xff。）
 *
 * <p>
 * 另外，使用浮点数学的实现可能会产生更准确的结果，最终返回到原始像素值，几乎没有舍入误差。
 * 或者，使用整数数学的实现可能会决定，如果在浮点空间中进行计算，方程实际上对颜色值是一个虚拟的 NOP，因此可以将像素原样传输到目标，完全避免所有数学计算。
 *
 * <p>
 * 这些实现都试图遵守相同的方程，但使用不同的整数和浮点数学以及简化或完整的方程的权衡。
 * 为了考虑这些差异，最好期望只有在预乘形式下的结果在不同实现和图像格式之间匹配。在这种情况下，两个答案以预乘形式表示都是：
 *
 * <pre>
 *    (A, R, G, B) = (0x01, 0x01, 0x00, 0x00)</pre>
 *
 * <p>
 * 因此它们都会匹配。
 *
 * <li>
 * 由于为了计算效率简化方程的技术，某些实现可能在遇到结果 alpha 值为 0.0 的非预乘目标时表现不同。
 * 注意，如果分母（alpha）为 0，SRC 规则中去除除以 alpha 的简化在技术上是无效的。
 * 但是，由于结果只有在预乘形式下查看时才应该是准确的，结果 alpha 为 0 本质上使结果颜色分量无关紧要，因此在这种情况下不应期望精确的行为。
 * </ul>
 * @see Composite
 * @see CompositeContext
 */

public final class AlphaComposite implements Composite {
    /**
     * 目标的颜色和 alpha 都被清除（Porter-Duff Clear 规则）。
     * 源和目标都不用作输入。
     *<p>
     * <em>F<sub>s</sub></em> = 0 和 <em>F<sub>d</sub></em> = 0，因此：
     *<pre>
     *  <em>A<sub>r</sub></em> = 0
     *  <em>C<sub>r</sub></em> = 0
     *</pre>
     */
    @Native public static final int     CLEAR           = 1;


                /**
     * 源被复制到目标
     * (Porter-Duff 源规则)。
     * 目标不作为输入。
     *<p>
     * <em>F<sub>s</sub></em> = 1 和 <em>F<sub>d</sub></em> = 0，因此：
     *<pre>
     *  <em>A<sub>r</sub></em> = <em>A<sub>s</sub></em>
     *  <em>C<sub>r</sub></em> = <em>C<sub>s</sub></em>
     *</pre>
     */
    @Native public static final int     SRC             = 2;

    /**
     * 目标保持不变
     * (Porter-Duff 目标规则)。
     *<p>
     * <em>F<sub>s</sub></em> = 0 和 <em>F<sub>d</sub></em> = 1，因此：
     *<pre>
     *  <em>A<sub>r</sub></em> = <em>A<sub>d</sub></em>
     *  <em>C<sub>r</sub></em> = <em>C<sub>d</sub></em>
     *</pre>
     * @since 1.4
     */
    @Native public static final int     DST             = 9;
    // 注意 DST 是在 1.4 版本中添加的，因此它在列表中的编号不符合顺序...

    /**
     * 源被合成到目标之上
     * (Porter-Duff 源覆盖目标规则)。
     *<p>
     * <em>F<sub>s</sub></em> = 1 和 <em>F<sub>d</sub></em> = (1-<em>A<sub>s</sub></em>)，因此：
     *<pre>
     *  <em>A<sub>r</sub></em> = <em>A<sub>s</sub></em> + <em>A<sub>d</sub></em>*(1-<em>A<sub>s</sub></em>)
     *  <em>C<sub>r</sub></em> = <em>C<sub>s</sub></em> + <em>C<sub>d</sub></em>*(1-<em>A<sub>s</sub></em>)
     *</pre>
     */
    @Native public static final int     SRC_OVER        = 3;

    /**
     * 目标被合成到源之上，并且结果替换目标
     * (Porter-Duff 目标覆盖源规则)。
     *<p>
     * <em>F<sub>s</sub></em> = (1-<em>A<sub>d</sub></em>) 和 <em>F<sub>d</sub></em> = 1，因此：
     *<pre>
     *  <em>A<sub>r</sub></em> = <em>A<sub>s</sub></em>*(1-<em>A<sub>d</sub></em>) + <em>A<sub>d</sub></em>
     *  <em>C<sub>r</sub></em> = <em>C<sub>s</sub></em>*(1-<em>A<sub>d</sub></em>) + <em>C<sub>d</sub></em>
     *</pre>
     */
    @Native public static final int     DST_OVER        = 4;

    /**
     * 源位于目标内部的部分替换目标
     * (Porter-Duff 源在目标内规则)。
     *<p>
     * <em>F<sub>s</sub></em> = <em>A<sub>d</sub></em> 和 <em>F<sub>d</sub></em> = 0，因此：
     *<pre>
     *  <em>A<sub>r</sub></em> = <em>A<sub>s</sub></em>*<em>A<sub>d</sub></em>
     *  <em>C<sub>r</sub></em> = <em>C<sub>s</sub></em>*<em>A<sub>d</sub></em>
     *</pre>
     */
    @Native public static final int     SRC_IN          = 5;

    /**
     * 目标位于源内部的部分替换目标
     * (Porter-Duff 目标在源内规则)。
     *<p>
     * <em>F<sub>s</sub></em> = 0 和 <em>F<sub>d</sub></em> = <em>A<sub>s</sub></em>，因此：
     *<pre>
     *  <em>A<sub>r</sub></em> = <em>A<sub>d</sub></em>*<em>A<sub>s</sub></em>
     *  <em>C<sub>r</sub></em> = <em>C<sub>d</sub></em>*<em>A<sub>s</sub></em>
     *</pre>
     */
    @Native public static final int     DST_IN          = 6;

    /**
     * 源位于目标外部的部分替换目标
     * (Porter-Duff 源被目标遮挡规则)。
     *<p>
     * <em>F<sub>s</sub></em> = (1-<em>A<sub>d</sub></em>) 和 <em>F<sub>d</sub></em> = 0，因此：
     *<pre>
     *  <em>A<sub>r</sub></em> = <em>A<sub>s</sub></em>*(1-<em>A<sub>d</sub></em>)
     *  <em>C<sub>r</sub></em> = <em>C<sub>s</sub></em>*(1-<em>A<sub>d</sub></em>)
     *</pre>
     */
    @Native public static final int     SRC_OUT         = 7;

    /**
     * 目标位于源外部的部分替换目标
     * (Porter-Duff 目标被源遮挡规则)。
     *<p>
     * <em>F<sub>s</sub></em> = 0 和 <em>F<sub>d</sub></em> = (1-<em>A<sub>s</sub></em>)，因此：
     *<pre>
     *  <em>A<sub>r</sub></em> = <em>A<sub>d</sub></em>*(1-<em>A<sub>s</sub></em>)
     *  <em>C<sub>r</sub></em> = <em>C<sub>d</sub></em>*(1-<em>A<sub>s</sub></em>)
     *</pre>
     */
    @Native public static final int     DST_OUT         = 8;

    // 规则 9 是 DST，已在上面定义，逻辑上适合放在那里，而不是按数字顺序
    //
    // public static final int  DST             = 9;

    /**
     * 源位于目标内部的部分被合成到目标上
     * (Porter-Duff 源在目标上规则)。
     *<p>
     * <em>F<sub>s</sub></em> = <em>A<sub>d</sub></em> 和 <em>F<sub>d</sub></em> = (1-<em>A<sub>s</sub></em>)，因此：
     *<pre>
     *  <em>A<sub>r</sub></em> = <em>A<sub>s</sub></em>*<em>A<sub>d</sub></em> + <em>A<sub>d</sub></em>*(1-<em>A<sub>s</sub></em>) = <em>A<sub>d</sub></em>
     *  <em>C<sub>r</sub></em> = <em>C<sub>s</sub></em>*<em>A<sub>d</sub></em> + <em>C<sub>d</sub></em>*(1-<em>A<sub>s</sub></em>)
     *</pre>
     * @since 1.4
     */
    @Native public static final int     SRC_ATOP        = 10;

    /**
     * 目标位于源内部的部分被合成到源上，并且结果替换目标
     * (Porter-Duff 目标在源上规则)。
     *<p>
     * <em>F<sub>s</sub></em> = (1-<em>A<sub>d</sub></em>) 和 <em>F<sub>d</sub></em> = <em>A<sub>s</sub></em>，因此：
     *<pre>
     *  <em>A<sub>r</sub></em> = <em>A<sub>s</sub></em>*(1-<em>A<sub>d</sub></em>) + <em>A<sub>d</sub></em>*<em>A<sub>s</sub></em> = <em>A<sub>s</sub></em>
     *  <em>C<sub>r</sub></em> = <em>C<sub>s</sub></em>*(1-<em>A<sub>d</sub></em>) + <em>C<sub>d</sub></em>*<em>A<sub>s</sub></em>
     *</pre>
     * @since 1.4
     */
    @Native public static final int     DST_ATOP        = 11;

    /**
     * 源位于目标外部的部分与目标位于源外部的部分结合
     * (Porter-Duff 源异或目标规则)。
     *<p>
     * <em>F<sub>s</sub></em> = (1-<em>A<sub>d</sub></em>) 和 <em>F<sub>d</sub></em> = (1-<em>A<sub>s</sub></em>)，因此：
     *<pre>
     *  <em>A<sub>r</sub></em> = <em>A<sub>s</sub></em>*(1-<em>A<sub>d</sub></em>) + <em>A<sub>d</sub></em>*(1-<em>A<sub>s</sub></em>)
     *  <em>C<sub>r</sub></em> = <em>C<sub>s</sub></em>*(1-<em>A<sub>d</sub></em>) + <em>C<sub>d</sub></em>*(1-<em>A<sub>s</sub></em>)
     *</pre>
     * @since 1.4
     */
    @Native public static final int     XOR             = 12;

    /**
     * 实现不透明 CLEAR 规则的 <code>AlphaComposite</code> 对象，alpha 值为 1.0f。
     * @see #CLEAR
     */
    public static final AlphaComposite Clear    = new AlphaComposite(CLEAR);

    /**
     * 实现不透明 SRC 规则的 <code>AlphaComposite</code> 对象，alpha 值为 1.0f。
     * @see #SRC
     */
    public static final AlphaComposite Src      = new AlphaComposite(SRC);

    /**
     * 实现不透明 DST 规则的 <code>AlphaComposite</code> 对象，alpha 值为 1.0f。
     * @see #DST
     * @since 1.4
     */
    public static final AlphaComposite Dst      = new AlphaComposite(DST);

    /**
     * 实现不透明 SRC_OVER 规则的 <code>AlphaComposite</code> 对象，alpha 值为 1.0f。
     * @see #SRC_OVER
     */
    public static final AlphaComposite SrcOver  = new AlphaComposite(SRC_OVER);

    /**
     * 实现不透明 DST_OVER 规则的 <code>AlphaComposite</code> 对象，alpha 值为 1.0f。
     * @see #DST_OVER
     */
    public static final AlphaComposite DstOver  = new AlphaComposite(DST_OVER);

    /**
     * 实现不透明 SRC_IN 规则的 <code>AlphaComposite</code> 对象，alpha 值为 1.0f。
     * @see #SRC_IN
     */
    public static final AlphaComposite SrcIn    = new AlphaComposite(SRC_IN);

    /**
     * 实现不透明 DST_IN 规则的 <code>AlphaComposite</code> 对象，alpha 值为 1.0f。
     * @see #DST_IN
     */
    public static final AlphaComposite DstIn    = new AlphaComposite(DST_IN);

    /**
     * 实现不透明 SRC_OUT 规则的 <code>AlphaComposite</code> 对象，alpha 值为 1.0f。
     * @see #SRC_OUT
     */
    public static final AlphaComposite SrcOut   = new AlphaComposite(SRC_OUT);

    /**
     * 实现不透明 DST_OUT 规则的 <code>AlphaComposite</code> 对象，alpha 值为 1.0f。
     * @see #DST_OUT
     */
    public static final AlphaComposite DstOut   = new AlphaComposite(DST_OUT);

    /**
     * 实现不透明 SRC_ATOP 规则的 <code>AlphaComposite</code> 对象，alpha 值为 1.0f。
     * @see #SRC_ATOP
     * @since 1.4
     */
    public static final AlphaComposite SrcAtop  = new AlphaComposite(SRC_ATOP);

    /**
     * 实现不透明 DST_ATOP 规则的 <code>AlphaComposite</code> 对象，alpha 值为 1.0f。
     * @see #DST_ATOP
     * @since 1.4
     */
    public static final AlphaComposite DstAtop  = new AlphaComposite(DST_ATOP);

    /**
     * 实现不透明 XOR 规则的 <code>AlphaComposite</code> 对象，alpha 值为 1.0f。
     * @see #XOR
     * @since 1.4
     */
    public static final AlphaComposite Xor      = new AlphaComposite(XOR);

    @Native private static final int MIN_RULE = CLEAR;
    @Native private static final int MAX_RULE = XOR;

    float extraAlpha;
    int rule;

    private AlphaComposite(int rule) {
        this(rule, 1.0f);
    }

    private AlphaComposite(int rule, float alpha) {
        if (rule < MIN_RULE || rule > MAX_RULE) {
            throw new IllegalArgumentException("未知的合成规则");
        }
        if (alpha >= 0.0f && alpha <= 1.0f) {
            this.rule = rule;
            this.extraAlpha = alpha;
        } else {
            throw new IllegalArgumentException("alpha 值超出范围");
        }
    }

    /**
     * 创建具有指定规则的 <code>AlphaComposite</code> 对象。
     * @param rule 合成规则
     * @throws IllegalArgumentException 如果 <code>rule</code> 不是以下之一：
     *         {@link #CLEAR}, {@link #SRC}, {@link #DST},
     *         {@link #SRC_OVER}, {@link #DST_OVER}, {@link #SRC_IN},
     *         {@link #DST_IN}, {@link #SRC_OUT}, {@link #DST_OUT},
     *         {@link #SRC_ATOP}, {@link #DST_ATOP}, 或 {@link #XOR}
     */
    public static AlphaComposite getInstance(int rule) {
        switch (rule) {
        case CLEAR:
            return Clear;
        case SRC:
            return Src;
        case DST:
            return Dst;
        case SRC_OVER:
            return SrcOver;
        case DST_OVER:
            return DstOver;
        case SRC_IN:
            return SrcIn;
        case DST_IN:
            return DstIn;
        case SRC_OUT:
            return SrcOut;
        case DST_OUT:
            return DstOut;
        case SRC_ATOP:
            return SrcAtop;
        case DST_ATOP:
            return DstAtop;
        case XOR:
            return Xor;
        default:
            throw new IllegalArgumentException("未知的合成规则");
        }
    }

    /**
     * 创建具有指定规则和与源 alpha 值相乘的常量 alpha 的 <code>AlphaComposite</code> 对象。
     * 源在与目标合成之前与指定的 alpha 值相乘。
     * @param rule 合成规则
     * @param alpha 与源 alpha 值相乘的常量 alpha。<code>alpha</code> 必须是 [0.0, 1.0] 范围内的浮点数。
     * @throws IllegalArgumentException 如果
     *         <code>alpha</code> 小于 0.0 或大于 1.0，或者
     *         <code>rule</code> 不是以下之一：
     *         {@link #CLEAR}, {@link #SRC}, {@link #DST},
     *         {@link #SRC_OVER}, {@link #DST_OVER}, {@link #SRC_IN},
     *         {@link #DST_IN}, {@link #SRC_OUT}, {@link #DST_OUT},
     *         {@link #SRC_ATOP}, {@link #DST_ATOP}, 或 {@link #XOR}
     */
    public static AlphaComposite getInstance(int rule, float alpha) {
        if (alpha == 1.0f) {
            return getInstance(rule);
        }
        return new AlphaComposite(rule, alpha);
    }

    /**
     * 创建用于合成操作的上下文。
     * 该上下文包含用于执行合成操作的状态。
     * @param srcColorModel  源的 {@link ColorModel}
     * @param dstColorModel  目标的 <code>ColorModel</code>
     * @return 用于执行合成操作的 <code>CompositeContext</code> 对象。
     */
    public CompositeContext createContext(ColorModel srcColorModel,
                                          ColorModel dstColorModel,
                                          RenderingHints hints) {
        return new SunCompositeContext(this, srcColorModel, dstColorModel);
    }

    /**
     * 返回此 <code>AlphaComposite</code> 的 alpha 值。如果此 <code>AlphaComposite</code> 没有 alpha 值，则返回 1.0。
     * @return 此 <code>AlphaComposite</code> 的 alpha 值。
     */
    public float getAlpha() {
        return extraAlpha;
    }

    /**
     * 返回此 <code>AlphaComposite</code> 的合成规则。
     * @return 此 <code>AlphaComposite</code> 的合成规则。
     */
    public int getRule() {
        return rule;
    }

    /**
     * 返回一个使用指定合成规则的类似的 <code>AlphaComposite</code> 对象。
     * 如果此对象已经使用指定的合成规则，则返回此对象。
     * @return 一个从此对象派生的使用指定合成规则的 <code>AlphaComposite</code> 对象。
     * @param rule 合成规则
     * @throws IllegalArgumentException 如果
     *         <code>rule</code> 不是以下之一：
     *         {@link #CLEAR}, {@link #SRC}, {@link #DST},
     *         {@link #SRC_OVER}, {@link #DST_OVER}, {@link #SRC_IN},
     *         {@link #DST_IN}, {@link #SRC_OUT}, {@link #DST_OUT},
     *         {@link #SRC_ATOP}, {@link #DST_ATOP}, 或 {@link #XOR}
     * @since 1.6
     */
    public AlphaComposite derive(int rule) {
        return (this.rule == rule)
            ? this
            : getInstance(rule, this.extraAlpha);
    }

    /**
     * 返回一个使用指定 alpha 值的类似的 <code>AlphaComposite</code> 对象。
     * 如果此对象已经具有指定的 alpha 值，则返回此对象。
     * @return 一个从此对象派生的使用指定 alpha 值的 <code>AlphaComposite</code> 对象。
     * @param alpha 与源 alpha 值相乘的常量 alpha。<code>alpha</code> 必须是 [0.0, 1.0] 范围内的浮点数。
     * @throws IllegalArgumentException 如果
     *         <code>alpha</code> 小于 0.0 或大于 1.0
     * @since 1.6
     */
    public AlphaComposite derive(float alpha) {
        return (this.extraAlpha == alpha)
            ? this
            : getInstance(this.rule, alpha);
    }

    /**
     * 返回此合成对象的哈希码。
     * @return 此合成对象的哈希码。
     */
    public int hashCode() {
        return (Float.floatToIntBits(extraAlpha) * 31 + rule);
    }


                /**
     * 确定指定的对象是否等于此
     * <code>AlphaComposite</code>。
     * <p>
     * 当且仅当参数不是 <code>null</code> 并且是一个
     * <code>AlphaComposite</code> 对象，且具有与此对象相同的
     * 组合规则和 alpha 值时，结果为 <code>true</code>。
     *
     * @param obj 要测试的 <code>Object</code>
     * @return <code>true</code> 如果 <code>obj</code> 等于此
     * <code>AlphaComposite</code>；否则为 <code>false</code>。
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof AlphaComposite)) {
            return false;
        }

        AlphaComposite ac = (AlphaComposite) obj;

        if (rule != ac.rule) {
            return false;
        }

        if (extraAlpha != ac.extraAlpha) {
            return false;
        }

        return true;
    }

}
