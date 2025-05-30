
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

/*
 * (C) Copyright Taligent, Inc. 1996 - 1997, All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998, All Rights Reserved
 *
 * The original version of this source code and documentation is
 * copyrighted and owned by Taligent, Inc., a wholly-owned subsidiary
 * of IBM. These materials are provided under terms of a License
 * Agreement between Taligent and Sun. This technology is protected
 * by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.awt.font;

import java.io.InvalidObjectException;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;
import java.util.HashMap;

/**
 * <code>TextAttribute</code> 类定义了用于文本渲染的属性键和属性值。
 * <p>
 * <code>TextAttribute</code> 实例用作属性键，以标识
 * {@link java.awt.Font Font}，
 * {@link java.awt.font.TextLayout TextLayout}，
 * {@link java.text.AttributedCharacterIterator AttributedCharacterIterator}，
 * 以及其他处理文本属性的类中的属性。此类中定义的其他常量可以用作属性值。
 * <p>
 * 对于每个文本属性，文档提供了以下信息：
 * <UL>
 *   <LI>其值的类型，
 *   <LI>相关的预定义常量（如果有）
 *   <LI>如果属性缺失，则默认效果
 *   <LI>如果有限制，则有效值
 *   <LI>效果的描述。
 * </UL>
 * <p>
 * <H3>值</H3>
 * <UL>
 *   <LI>属性的值必须始终是不可变的。
 *   <LI>如果给出了值的限制，任何超出该范围的值将保留用于未来使用；该值将被视为默认值。
 *   <LI>值 <code>null</code> 被视为默认值，并导致默认行为。
 *   <li>如果值不是正确的类型，该属性将被忽略。
 *   <li>值的身份无关紧要，只有实际值重要。例如，<code>TextAttribute.WEIGHT_BOLD</code> 和
 *   <code>new Float(2.0)</code>
 *   表示相同的 <code>WEIGHT</code>。
 *   <li>类型为 <code>Number</code> 的属性值（用于
 *   <code>WEIGHT</code>，<code>WIDTH</code>，<code>POSTURE</code>，
 *   <code>SIZE</code>，<code>JUSTIFICATION</code> 和
 *   <code>TRACKING</code>）可以在其自然范围内变化，不受预定义常量的限制。
 *   <code>Number.floatValue()</code> 用于从 <code>Number</code> 获取实际值。
 *   <li><code>WEIGHT</code>，<code>WIDTH</code> 和
 *   <code>POSTURE</code> 的值由系统插值，可以选择“最近可用”的字体或使用其他技术来近似用户请求。
 *
 * </UL>
 *
 * <h4>属性摘要</h4>
 * <p>
 * <table style="float:center" border="0" cellspacing="0" cellpadding="2" width="%95"
 *     summary="Key, value type, principal constants, and default value
 *     behavior of all TextAttributes">
 * <tr style="background-color:#ccccff">
 * <th valign="TOP" align="CENTER">键</th>
 * <th valign="TOP" align="CENTER">值类型</th>
 * <th valign="TOP" align="CENTER">主要常量</th>
 * <th valign="TOP" align="CENTER">默认值</th>
 * </tr>
 * <tr>
 * <td valign="TOP">{@link #FAMILY}</td>
 * <td valign="TOP">String</td>
 * <td valign="TOP">参见 Font {@link java.awt.Font#DIALOG DIALOG}，
 * {@link java.awt.Font#DIALOG_INPUT DIALOG_INPUT}，<br> {@link java.awt.Font#SERIF SERIF}，
 * {@link java.awt.Font#SANS_SERIF SANS_SERIF} 和 {@link java.awt.Font#MONOSPACED MONOSPACED}。
 * </td>
 * <td valign="TOP">"Default"（使用平台默认值）</td>
 * </tr>
 * <tr style="background-color:#eeeeff">
 * <td valign="TOP">{@link #WEIGHT}</td>
 * <td valign="TOP">Number</td>
 * <td valign="TOP">WEIGHT_REGULAR, WEIGHT_BOLD</td>
 * <td valign="TOP">WEIGHT_REGULAR</td>
 * </tr>
 * <tr>
 * <td valign="TOP">{@link #WIDTH}</td>
 * <td valign="TOP">Number</td>
 * <td valign="TOP">WIDTH_CONDENSED, WIDTH_REGULAR,<br>WIDTH_EXTENDED</td>
 * <td valign="TOP">WIDTH_REGULAR</td>
 * </tr>
 * <tr style="background-color:#eeeeff">
 * <td valign="TOP">{@link #POSTURE}</td>
 * <td valign="TOP">Number</td>
 * <td valign="TOP">POSTURE_REGULAR, POSTURE_OBLIQUE</td>
 * <td valign="TOP">POSTURE_REGULAR</td>
 * </tr>
 * <tr>
 * <td valign="TOP">{@link #SIZE}</td>
 * <td valign="TOP">Number</td>
 * <td valign="TOP">无</td>
 * <td valign="TOP">12.0</td>
 * </tr>
 * <tr style="background-color:#eeeeff">
 * <td valign="TOP">{@link #TRANSFORM}</td>
 * <td valign="TOP">{@link TransformAttribute}</td>
 * <td valign="TOP">参见 TransformAttribute {@link TransformAttribute#IDENTITY IDENTITY}</td>
 * <td valign="TOP">TransformAttribute.IDENTITY</td>
 * </tr>
 * <tr>
 * <td valign="TOP">{@link #SUPERSCRIPT}</td>
 * <td valign="TOP">Integer</td>
 * <td valign="TOP">SUPERSCRIPT_SUPER, SUPERSCRIPT_SUB</td>
 * <td valign="TOP">0（使用标准字形和度量）</td>
 * </tr>
 * <tr style="background-color:#eeeeff">
 * <td valign="TOP">{@link #FONT}</td>
 * <td valign="TOP">{@link java.awt.Font}</td>
 * <td valign="TOP">无</td>
 * <td valign="TOP">null（不覆盖字体解析）</td>
 * </tr>
 * <tr>
 * <td valign="TOP">{@link #CHAR_REPLACEMENT}</td>
 * <td valign="TOP">{@link GraphicAttribute}</td>
 * <td valign="TOP">无</td>
 * <td valign="TOP">null（使用字体字形绘制文本）</td>
 * </tr>
 * <tr style="background-color:#eeeeff">
 * <td valign="TOP">{@link #FOREGROUND}</td>
 * <td valign="TOP">{@link java.awt.Paint}</td>
 * <td valign="TOP">无</td>
 * <td valign="TOP">null（使用当前图形画笔）</td>
 * </tr>
 * <tr>
 * <td valign="TOP">{@link #BACKGROUND}</td>
 * <td valign="TOP">{@link java.awt.Paint}</td>
 * <td valign="TOP">无</td>
 * <td valign="TOP">null（不渲染背景）</td>
 * </tr>
 * <tr style="background-color:#eeeeff">
 * <td valign="TOP">{@link #UNDERLINE}</td>
 * <td valign="TOP">Integer</td>
 * <td valign="TOP">UNDERLINE_ON</td>
 * <td valign="TOP">-1（不渲染下划线）</td>
 * </tr>
 * <tr>
 * <td valign="TOP">{@link #STRIKETHROUGH}</td>
 * <td valign="TOP">Boolean</td>
 * <td valign="TOP">STRIKETHROUGH_ON</td>
 * <td valign="TOP">false（不渲染删除线）</td>
 * </tr>
 * <tr style="background-color:#eeeeff">
 * <td valign="TOP">{@link #RUN_DIRECTION}</td>
 * <td valign="TOP">Boolean</td>
 * <td valign="TOP">RUN_DIRECTION_LTR<br>RUN_DIRECTION_RTL</td>
 * <td valign="TOP">null（使用 {@link java.text.Bidi} 标准默认值）</td>
 * </tr>
 * <tr>
 * <td valign="TOP">{@link #BIDI_EMBEDDING}</td>
 * <td valign="TOP">Integer</td>
 * <td valign="TOP">无</td>
 * <td valign="TOP">0（使用基线方向）</td>
 * </tr>
 * <tr style="background-color:#eeeeff">
 * <td valign="TOP">{@link #JUSTIFICATION}</td>
 * <td valign="TOP">Number</td>
 * <td valign="TOP">JUSTIFICATION_FULL</td>
 * <td valign="TOP">JUSTIFICATION_FULL</td>
 * </tr>
 * <tr>
 * <td valign="TOP">{@link #INPUT_METHOD_HIGHLIGHT}</td>
 * <td valign="TOP">{@link java.awt.im.InputMethodHighlight},<br>{@link java.text.Annotation}</td>
 * <td valign="TOP">（参见类）</td>
 * <td valign="TOP">null（不应用输入高亮）</td>
 * </tr>
 * <tr style="background-color:#eeeeff">
 * <td valign="TOP">{@link #INPUT_METHOD_UNDERLINE}</td>
 * <td valign="TOP">Integer</td>
 * <td valign="TOP">UNDERLINE_LOW_ONE_PIXEL,<br>UNDERLINE_LOW_TWO_PIXEL</td>
 * <td valign="TOP">-1（不渲染下划线）</td>
 * </tr>
 * <tr>
 * <td valign="TOP">{@link #SWAP_COLORS}</td>
 * <td valign="TOP">Boolean</td>
 * <td valign="TOP">SWAP_COLORS_ON</td>
 * <td valign="TOP">false（不交换颜色）</td>
 * </tr>
 * <tr style="background-color:#eeeeff">
 * <td valign="TOP">{@link #NUMERIC_SHAPING}</td>
 * <td valign="TOP">{@link java.awt.font.NumericShaper}</td>
 * <td valign="TOP">无</td>
 * <td valign="TOP">null（不整形数字）</td>
 * </tr>
 * <tr>
 * <td valign="TOP">{@link #KERNING}</td>
 * <td valign="TOP">Integer</td>
 * <td valign="TOP">KERNING_ON</td>
 * <td valign="TOP">0（不请求字距）</td>
 * </tr>
 * <tr style="background-color:#eeeeff">
 * <td valign="TOP">{@link #LIGATURES}</td>
 * <td valign="TOP">Integer</td>
 * <td valign="TOP">LIGATURES_ON</td>
 * <td valign="TOP">0（不形成可选连字）</td>
 * </tr>
 * <tr>
 * <td valign="TOP">{@link #TRACKING}</td>
 * <td valign="TOP">Number</td>
 * <td valign="TOP">TRACKING_LOOSE, TRACKING_TIGHT</td>
 * <td valign="TOP">0（不添加字距）</td>
 * </tr>
 * </table>
 *
 * @see java.awt.Font
 * @see java.awt.font.TextLayout
 * @see java.text.AttributedCharacterIterator
 */
public final class TextAttribute extends Attribute {

    // 用于 readResolve 的此类中所有实例的表
    private static final Map<String, TextAttribute>
            instanceMap = new HashMap<String, TextAttribute>(29);

    /**
     * 使用指定的名称构造一个 <code>TextAttribute</code>。
     * @param name 要分配给此 <code>TextAttribute</code> 的属性名称
     */
    protected TextAttribute(String name) {
        super(name);
        if (this.getClass() == TextAttribute.class) {
            instanceMap.put(name, this);
        }
    }

    /**
     * 将反序列化的实例解析为预定义的常量。
     */
    protected Object readResolve() throws InvalidObjectException {
        if (this.getClass() != TextAttribute.class) {
            throw new InvalidObjectException(
                "子类未正确实现 readResolve");
        }

        TextAttribute instance = instanceMap.get(getName());
        if (instance != null) {
            return instance;
        } else {
            throw new InvalidObjectException("未知的属性名称");
        }
    }

    // 与 Java 2 平台 v1.2 的序列化兼容。
    // 1.2 如果要求反序列化 INPUT_METHOD_UNDERLINE，将抛出 InvalidObjectException。
    // 这在实际生活中不应该发生。
    static final long serialVersionUID = 7744112784117861702L;

    //
    // 用于 Font。
    //

    /**
     * 字体名称的属性键。值是 <b><code>String</code></b> 的实例。默认值是
     * <code>"Default"</code>，这将使用平台默认字体系列。
     *
     * <p> <code>Font</code> 类定义了逻辑字体名称的常量
     * {@link java.awt.Font#DIALOG DIALOG}，
     * {@link java.awt.Font#DIALOG_INPUT DIALOG_INPUT}，
     * {@link java.awt.Font#SANS_SERIF SANS_SERIF}，
     * {@link java.awt.Font#SERIF SERIF} 和
     * {@link java.awt.Font#MONOSPACED MONOSPACED}。
     *
     * <p>这定义了传递给 <code>Font</code> 构造函数的 <code>name</code> 的值。允许使用逻辑和物理字体名称。如果未找到请求的字体名称，则使用默认字体。
     *
     * <p><em>注意：</em>此属性不幸地命名不当，因为它指定的是面名称而不仅仅是系列。因此，像 "Lucida Sans Bold" 这样的值如果存在将选择该面。但是，如果请求的面不存在，则使用默认值且 <em>常规</em> 粗细。名称中的 "Bold" 是面名称的一部分，而不是单独请求字体的粗细为粗体。</p>
     */
    public static final TextAttribute FAMILY =
        new TextAttribute("family");

    /**
     * 字体粗细的属性键。值是 <b><code>Number</code></b> 的实例。默认值是
     * <code>WEIGHT_REGULAR</code>。
     *
     * <p>提供了几个常量值，参见 {@link
     * #WEIGHT_EXTRA_LIGHT}，{@link #WEIGHT_LIGHT}，{@link
     * #WEIGHT_DEMILIGHT}，{@link #WEIGHT_REGULAR}，{@link
     * #WEIGHT_SEMIBOLD}，{@link #WEIGHT_MEDIUM}，{@link
     * #WEIGHT_DEMIBOLD}，{@link #WEIGHT_BOLD}，{@link #WEIGHT_HEAVY}，
     * {@link #WEIGHT_EXTRABOLD} 和 {@link #WEIGHT_ULTRABOLD}。值 <code>WEIGHT_BOLD</code> 对应于
     * 传递给 <code>Font</code> 构造函数的样式值 <code>Font.BOLD</code>。
     *
     * <p>该值大致是粗细宽度与常规粗细宽度的比例。
     *
     * <p>系统可以插值提供的值。
     */
    public static final TextAttribute WEIGHT =
        new TextAttribute("weight");

    /**
     * 最轻的预定义粗细。
     * @see #WEIGHT
     */
    public static final Float WEIGHT_EXTRA_LIGHT =
        Float.valueOf(0.5f);

    /**
     * 标准轻粗细。
     * @see #WEIGHT
     */
    public static final Float WEIGHT_LIGHT =
        Float.valueOf(0.75f);

    /**
     * 介于 <code>WEIGHT_LIGHT</code> 和 <code>WEIGHT_STANDARD</code> 之间的中间粗细。
     * @see #WEIGHT
     */
    public static final Float WEIGHT_DEMILIGHT =
        Float.valueOf(0.875f);

    /**
     * 标准粗细。这是 <code>WEIGHT</code> 的默认值。
     * @see #WEIGHT
     */
    public static final Float WEIGHT_REGULAR =
        Float.valueOf(1.0f);

    /**
     * 比 <code>WEIGHT_REGULAR</code> 稍重的粗细。
     * @see #WEIGHT
     */
    public static final Float WEIGHT_SEMIBOLD =
        Float.valueOf(1.25f);

    /**
     * 介于 <code>WEIGHT_REGULAR</code> 和 <code>WEIGHT_BOLD</code> 之间的中间粗细。
     * @see #WEIGHT
     */
    public static final Float WEIGHT_MEDIUM =
        Float.valueOf(1.5f);

    /**
     * 比 <code>WEIGHT_BOLD</code> 稍轻的粗细。
     * @see #WEIGHT
     */
    public static final Float WEIGHT_DEMIBOLD =
        Float.valueOf(1.75f);

    /**
     * 标准粗体粗细。
     * @see #WEIGHT
     */
    public static final Float WEIGHT_BOLD =
        Float.valueOf(2.0f);

    /**
     * 比 <code>WEIGHT_BOLD</code> 稍重的粗细。
     * @see #WEIGHT
     */
    public static final Float WEIGHT_HEAVY =
        Float.valueOf(2.25f);


                /**
     * 一个额外的重权重。
     * @see #WEIGHT
     */
    public static final Float WEIGHT_EXTRABOLD =
        Float.valueOf(2.5f);

    /**
     * 最重的预定义权重。
     * @see #WEIGHT
     */
    public static final Float WEIGHT_ULTRABOLD =
        Float.valueOf(2.75f);

    /**
     * 字体宽度的属性键。值是 <b><code>Number</code></b> 的实例。默认值是
     * <code>WIDTH_REGULAR</code>。
     *
     * <p>提供了几个常量值，见 {@link
     * #WIDTH_CONDENSED}, {@link #WIDTH_SEMI_CONDENSED}, {@link
     * #WIDTH_REGULAR}, {@link #WIDTH_SEMI_EXTENDED}, {@link
     * #WIDTH_EXTENDED}。
     *
     * <p>该值大致是字体宽度与常规宽度的比例。
     *
     * <p>系统可以插值提供的值。
     */
    public static final TextAttribute WIDTH =
        new TextAttribute("width");

    /**
     * 最紧凑的预定义宽度。
     * @see #WIDTH
     */
    public static final Float WIDTH_CONDENSED =
        Float.valueOf(0.75f);

    /**
     * 适度紧凑的宽度。
     * @see #WIDTH
     */
    public static final Float WIDTH_SEMI_CONDENSED =
        Float.valueOf(0.875f);

    /**
     * 标准宽度。这是 <code>WIDTH</code> 的默认值。
     * @see #WIDTH
     */
    public static final Float WIDTH_REGULAR =
        Float.valueOf(1.0f);

    /**
     * 适度扩展的宽度。
     * @see #WIDTH
     */
    public static final Float WIDTH_SEMI_EXTENDED =
        Float.valueOf(1.25f);

    /**
     * 最扩展的预定义宽度。
     * @see #WIDTH
     */
    public static final Float WIDTH_EXTENDED =
        Float.valueOf(1.5f);

    /**
     * 字体姿态的属性键。值是 <b><code>Number</code></b> 的实例。默认值是
     * <code>POSTURE_REGULAR</code>。
     *
     * <p>提供了两个常量值，见 {@link #POSTURE_REGULAR}
     * 和 {@link #POSTURE_OBLIQUE}。值
     * <code>POSTURE_OBLIQUE</code> 对应于传递给 <code>Font</code>
     * 构造函数的样式值 <code>Font.ITALIC</code>。
     *
     * <p>该值大致是字体笔画的斜率，表示为水平距离与垂直距离的比值。正值向右倾斜。
     *
     * <p>系统可以插值提供的值。
     *
     * <p>这将影响 <code>Font.getItalicAngle</code> 返回的字体斜度。
     *
     * @see java.awt.Font#getItalicAngle()
     */
    public static final TextAttribute POSTURE =
        new TextAttribute("posture");

    /**
     * 标准姿态，直立。这是 <code>POSTURE</code> 的默认值。
     * @see #POSTURE
     */
    public static final Float POSTURE_REGULAR =
        Float.valueOf(0.0f);

    /**
     * 标准斜体姿态。
     * @see #POSTURE
     */
    public static final Float POSTURE_OBLIQUE =
        Float.valueOf(0.20f);

    /**
     * 字体大小的属性键。值是 <b><code>Number</code></b> 的实例。默认值是 12pt。
     *
     * <p>这对应于传递给 <code>Font</code> 构造函数的 <code>size</code> 参数。
     *
     * <p>非常大或非常小的大小会影响渲染性能，渲染系统可能不会在这些大小下渲染文本。负值是非法的，结果是默认大小。
     *
     * <p>注意，12pt 字体在 2 倍变换下的外观和度量可能与 24 点字体在没有变换下的不同。
     */
    public static final TextAttribute SIZE =
        new TextAttribute("size");

    /**
     * 字体变换的属性键。值是 <b><code>TransformAttribute</code></b> 的实例。默认值是 <code>TransformAttribute.IDENTITY</code>。
     *
     * <p><code>TransformAttribute</code> 类定义了常量 {@link TransformAttribute#IDENTITY IDENTITY}。
     *
     * <p>这对应于传递给 <code>Font.deriveFont(AffineTransform)</code> 的变换。由于该变换是可变的，而 <code>TextAttribute</code> 值必须不可变，因此使用 <code>TransformAttribute</code> 包装类。
     *
     * <p>主要目的是支持缩放和倾斜，尽管其他效果也是可能的。</p>
     *
     * <p>某些变换会导致基线旋转和/或移动。文本和基线一起变换，因此文本跟随新的基线。例如，对于水平基线上的文本，新的基线跟随通过变换传递的单位 x 向量的方向。文本度量是相对于这个新的基线测量的。因此，例如，在其他条件相同的情况下，使用旋转 <code>TRANSFORM</code> 和未旋转 <code>TRANSFORM</code> 渲染的文本将测量为具有相同的上升高度、下降高度和前进宽度。</p>
     *
     * <p>在带样式的文本中，每个这样的段落的基线依次对齐，可能创建整个文本段落的非线性基线。有关更多信息，请参见 {@link
     * TextLayout#getLayoutPath}。</p>
     *
     * @see TransformAttribute
     * @see java.awt.geom.AffineTransform
     */
     public static final TextAttribute TRANSFORM =
        new TextAttribute("transform");

    /**
     * 上标和下标的属性键。值是 <b><code>Integer</code></b> 的实例。默认值是
     * 0，表示不使用上标或下标。
     *
     * <p>提供了两个常量值，见 {@link
     * #SUPERSCRIPT_SUPER} 和 {@link #SUPERSCRIPT_SUB}。它们的值分别为 1 和 -1。更大的值定义更高层次的上标或下标，例如，2 对应于超级上标，3 对应于超级超级上标，负值也是如此，最高可达 7（或 -7）。超出此范围的值是保留的；行为是平台依赖的。
     *
     * <p><code>SUPERSCRIPT</code> 可以
     * 影响字体的上升高度和下降高度。然而，上升高度和下降高度永远不会变为负值。
     */
    public static final TextAttribute SUPERSCRIPT =
        new TextAttribute("superscript");

    /**
     * 标准上标。
     * @see #SUPERSCRIPT
     */
    public static final Integer SUPERSCRIPT_SUPER =
        Integer.valueOf(1);

    /**
     * 标准下标。
     * @see #SUPERSCRIPT
     */
    public static final Integer SUPERSCRIPT_SUB =
        Integer.valueOf(-1);

    /**
     * 用于提供用于渲染文本的字体的属性键。值是 {@link java.awt.Font} 的实例。默认
     * 值是 null，表示应从属性中正常解析 <code>Font</code>。
     *
     * <p><code>TextLayout</code> 和
     * <code>AttributedCharacterIterator</code> 以 <code>Maps</code> 的形式处理
     * <code>TextAttributes</code>。通常，所有属性都会被检查并用于选择和配置一个 <code>Font</code> 实例。但是，如果存在 <code>FONT</code>
     * 属性，其关联的 <code>Font</code> 将被使用。这为用户提供了一种覆盖将字体属性解析为 <code>Font</code> 的方法，或强制使用特定的 <code>Font</code> 实例。这也允许用户在可以子类化 <code>Font</code> 的情况下指定 <code>Font</code> 的子类。
     *
     * <p><code>FONT</code> 用于特殊情况下，客户端已经有一个 <code>Font</code> 实例但仍需要使用基于 <code>Map</code> 的 API。通常，<code>Map</code> 中除了 <code>FONT</code> 属性外不会有其他属性。在基于 <code>Map</code> 的 API 中，常见的做法是单独指定所有属性，因此 <code>FONT</code> 是不需要或不希望的。
     *
     * <p>然而，如果 <code>Map</code> 中同时存在 <code>FONT</code> 和其他属性，渲染系统将合并 <code>Font</code> 中定义的属性和附加属性。这个合并过程将 <code>TextAttributes</code> 分类为两组。一组是“主要”组，被认为是选择和度量字体行为的基础。这些属性是
     * <code>FAMILY</code>, <code>WEIGHT</code>, <code>WIDTH</code>,
     * <code>POSTURE</code>, <code>SIZE</code>,
     * <code>TRANSFORM</code>, <code>SUPERSCRIPT</code>, 和
     * <code>TRACKING</code>。另一组是“次要”组，包括所有其他定义的属性，但不包括 <code>FONT</code> 本身。
     *
     * <p>为了生成新的 <code>Map</code>，首先从 <code>FONT</code>
     * 属性中获取 <code>Font</code>，并将其所有属性提取到一个新的 <code>Map</code> 中。然后仅将原始 <code>Map</code> 中的“次要”属性添加到新 <code>Map</code> 中。因此，主要属性的值仅来自 <code>Font</code>，而次要属性的值源自 <code>Font</code> 但可以被 <code>Map</code> 中的其他值覆盖。
     *
     * <p><em>注意：</em><code>Font's</code> 基于 <code>Map</code> 的构造函数和 <code>deriveFont</code> 方法不处理 <code>FONT</code> 属性，因为这些方法用于创建新的 <code>Font</code> 对象。相反，应使用 {@link
     * java.awt.Font#getFont(Map) Font.getFont(Map)} 来处理 <code>FONT</code> 属性。
     *
     * @see java.awt.Font
     */
    public static final TextAttribute FONT =
        new TextAttribute("font");

    /**
     * 用于在字符的标准字形之外显示用户定义的字形的属性键。值是 GraphicAttribute 的实例。默认值是 null，
     * 表示应使用字体提供的标准字形。
     *
     * <p>此属性用于在文本行中预留图形或其他组件的空间。在执行双向重新排序（见 {@link java.text.Bidi}）时，它用于正确定位行内的“内联”组件。每个字符（Unicode 代码点）将使用提供的 GraphicAttribute 渲染。通常，应用此属性的字符应为 <code>&#92;uFFFC</code>。
     *
     * <p>GraphicAttribute 确定文本的逻辑和视觉边界；实际的 Font 值被忽略。
     *
     * @see GraphicAttribute
     */
    public static final TextAttribute CHAR_REPLACEMENT =
        new TextAttribute("char_replacement");

    //
    // 添加到文本的装饰。
    //

    /**
     * 用于渲染文本的画笔的属性键。值是 <b><code>Paint</code></b> 的实例。默认值是
     * null，表示使用渲染时 <code>Graphics2D</code> 上设置的 <code>Paint</code>。
     *
     * <p>无论 <code>Graphics</code> 上设置的 <code>Paint</code> 值如何，字形都将使用此
     * <code>Paint</code> 渲染（但请参见 {@link #SWAP_COLORS}）。
     *
     * @see java.awt.Paint
     * @see #SWAP_COLORS
     */
    public static final TextAttribute FOREGROUND =
        new TextAttribute("foreground");

    /**
     * 用于渲染文本背景的画笔的属性键。值是 <b><code>Paint</code></b> 的实例。默认值是
     * null，表示不应渲染背景。
     *
     * <p>将使用此 <code>Paint</code> 填充文本的逻辑边界，然后在上面渲染文本（但请参见 {@link #SWAP_COLORS}）。
     *
     * <p>如果需要，文本的视觉边界将扩展以包括逻辑边界。轮廓不受影响。
     *
     * @see java.awt.Paint
     * @see #SWAP_COLORS
     */
    public static final TextAttribute BACKGROUND =
        new TextAttribute("background");

    /**
     * 下划线的属性键。值是 <b><code>Integer</code></b> 的实例。默认值是 -1，表示没有下划线。
     *
     * <p>提供了常量值 {@link #UNDERLINE_ON}。
     *
     * <p>下划线影响文本的视觉边界和轮廓。
     */
    public static final TextAttribute UNDERLINE =
        new TextAttribute("underline");

    /**
     * 标准下划线。
     *
     * @see #UNDERLINE
     */
    public static final Integer UNDERLINE_ON =
        Integer.valueOf(0);

    /**
     * 删除线的属性键。值是 <b><code>Boolean</code></b> 的实例。默认值是
     * <code>false</code>，表示没有删除线。
     *
     * <p>提供了常量值 {@link #STRIKETHROUGH_ON}。
     *
     * <p>删除线影响文本的视觉边界和轮廓。
     */
    public static final TextAttribute STRIKETHROUGH =
        new TextAttribute("strikethrough");

    /**
     * 单一删除线。
     *
     * @see #STRIKETHROUGH
     */
    public static final Boolean STRIKETHROUGH_ON =
        Boolean.TRUE;

    //
    // 用于控制文本在行上的布局的属性。
    //

    /**
     * 行的运行方向的属性键。值是 <b><code>Boolean</code></b> 的实例。默认值是
     * null，表示应使用标准双向算法确定运行方向，值为 {@link
     * java.text.Bidi#DIRECTION_DEFAULT_LEFT_TO_RIGHT}。
     *
     * <p>提供了常量值 {@link #RUN_DIRECTION_RTL} 和 {@link
     * #RUN_DIRECTION_LTR}。
     *
     * <p>这决定了传递给 {@link
     * java.text.Bidi} 构造函数的值，以选择段落中文本的主要方向。
     *
     * <p><em>注意：</em>此属性应在段落中的所有文本中具有相同的值，否则行为是不确定的。
     *
     * @see java.text.Bidi
     */
    public static final TextAttribute RUN_DIRECTION =
        new TextAttribute("run_direction");

    /**
     * 从左到右的运行方向。
     * @see #RUN_DIRECTION
     */
    public static final Boolean RUN_DIRECTION_LTR =
        Boolean.FALSE;

    /**
     * 从右到左的运行方向。
     * @see #RUN_DIRECTION
     */
    public static final Boolean RUN_DIRECTION_RTL =
        Boolean.TRUE;

    /**
     * 文本的嵌入级别的属性键。值是 <b><code>Integer</code></b> 的实例。默认值是
     * <code>null</code>，表示双向算法应无显式嵌入地运行。
     *
     * <p>正值 1 到 61 是 <em>嵌入</em> 级别，负值 -1 到 -61 是 <em>覆盖</em> 级别。
     * 值 0 表示使用基线方向。这些级别作为嵌入级别数组传递给 {@link
     * java.text.Bidi} 构造函数。
     *
     * <p><em>注意：</em>当此属性在段落中的任何位置存在时，段落中的任何 Unicode 双向控制字符（RLO，
     * LRO, RLE, LRE, 和 PDF）将被忽略，且此属性不存在的文本段落将被视为存在且值为 0。
     *
     * @see java.text.Bidi
     */
    public static final TextAttribute BIDI_EMBEDDING =
        new TextAttribute("bidi_embedding");


                /**
     * 段落对齐的属性键。值为 <b><code>Number</code></b> 的实例。默认值为 1，表示对齐应使用提供的全部宽度。值被限制在 [0..1] 范围内。
     *
     * <p>提供了常量 {@link #JUSTIFICATION_FULL} 和 {@link
     * #JUSTIFICATION_NONE}。
     *
     * <p>当请求对 <code>TextLayout</code> 进行对齐时，指定使用额外空间的分数。例如，如果行宽为 50 点，并请求对齐到 70 点，值为 0.75 将填充剩余空间的四分之三，即 15 点，使得结果行的长度为 65 点。
     *
     * <p><em>注意：</em> 这个值应该对段落中的所有文本都相同，否则行为是不确定的。
     *
     * @see TextLayout#getJustifiedLayout
     */
    public static final TextAttribute JUSTIFICATION =
        new TextAttribute("justification");

    /**
     * 将行对齐到请求的全宽。这是 <code>JUSTIFICATION</code> 的默认值。
     * @see #JUSTIFICATION
     */
    public static final Float JUSTIFICATION_FULL =
        Float.valueOf(1.0f);

    /**
     * 不允许行对齐。
     * @see #JUSTIFICATION
     */
    public static final Float JUSTIFICATION_NONE =
        Float.valueOf(0.0f);

    //
    // 供输入方法使用。
    //

    /**
     * 输入方法高亮样式的属性键。
     *
     * <p>值为 {@link
     * java.awt.im.InputMethodHighlight} 或 {@link
     * java.text.Annotation} 的实例。默认值为 <code>null</code>，表示在渲染前不应应用输入方法样式。
     *
     * <p>如果需要单独渲染具有相同 <code>InputMethodHighlight</code> 的相邻文本段，则应将 <code>InputMethodHighlights</code> 包装在 <code>Annotation</code> 实例中。
     *
     * <p>输入方法高亮在文本由输入方法组成时使用。文本编辑组件应保留这些高亮，即使它们通常只处理无样式文本，并将它们提供给绘图例程。
     *
     * @see java.awt.Font
     * @see java.awt.im.InputMethodHighlight
     * @see java.text.Annotation
     */
    public static final TextAttribute INPUT_METHOD_HIGHLIGHT =
        new TextAttribute("input method highlight");

    /**
     * 输入方法下划线的属性键。值为 <b><code>Integer</code></b> 的实例。默认值为 <code>-1</code>，表示无下划线。
     *
     * <p>提供了几个常量值，参见 {@link
     * #UNDERLINE_LOW_ONE_PIXEL}, {@link #UNDERLINE_LOW_TWO_PIXEL},
     * {@link #UNDERLINE_LOW_DOTTED}, {@link #UNDERLINE_LOW_GRAY} 和
     * {@link #UNDERLINE_LOW_DASHED}。
     *
     * <p>如果需要，可以与 {@link #UNDERLINE} 一起使用。主要目的是供输入方法使用。其他用于简单装饰的下划线可能会使用户混淆。
     *
     * <p>输入方法下划线会影响文本的视觉边界和轮廓。
     *
     * @since 1.3
     */
    public static final TextAttribute INPUT_METHOD_UNDERLINE =
        new TextAttribute("input method underline");

    /**
     * 单像素实线下划线。
     * @see #INPUT_METHOD_UNDERLINE
     * @since 1.3
     */
    public static final Integer UNDERLINE_LOW_ONE_PIXEL =
        Integer.valueOf(1);

    /**
     * 双像素实线下划线。
     * @see #INPUT_METHOD_UNDERLINE
     * @since 1.3
     */
    public static final Integer UNDERLINE_LOW_TWO_PIXEL =
        Integer.valueOf(2);

    /**
     * 单像素点线下划线。
     * @see #INPUT_METHOD_UNDERLINE
     * @since 1.3
     */
    public static final Integer UNDERLINE_LOW_DOTTED =
        Integer.valueOf(3);

    /**
     * 双像素灰色下划线。
     * @see #INPUT_METHOD_UNDERLINE
     * @since 1.3
     */
    public static final Integer UNDERLINE_LOW_GRAY =
        Integer.valueOf(4);

    /**
     * 单像素虚线下划线。
     * @see #INPUT_METHOD_UNDERLINE
     * @since 1.3
     */
    public static final Integer UNDERLINE_LOW_DASHED =
        Integer.valueOf(5);

    /**
     * 交换前景和背景 <code>Paints</code> 的属性键。值为 <b><code>Boolean</code></b> 的实例。默认值为 <code>false</code>，表示不交换颜色。
     *
     * <p>定义了常量值 {@link #SWAP_COLORS_ON}。
     *
     * <p>如果设置了 {@link #FOREGROUND} 属性，其 <code>Paint</code> 将用作背景，否则使用当前在 <code>Graphics</code> 上的 <code>Paint</code>。如果设置了 {@link #BACKGROUND} 属性，其 <code>Paint</code> 将用作前景，否则系统将找到与（解析后的）背景对比鲜明的颜色，以使文本可见。
     *
     * @see #FOREGROUND
     * @see #BACKGROUND
     */
    public static final TextAttribute SWAP_COLORS =
        new TextAttribute("swap_colors");

    /**
     * 交换前景和背景。
     * @see #SWAP_COLORS
     * @since 1.3
     */
    public static final Boolean SWAP_COLORS_ON =
        Boolean.TRUE;

    /**
     * 将 ASCII 十进制数字转换为其他十进制范围的属性键。值为 {@link NumericShaper} 的实例。默认值为 <code>null</code>，表示不执行数字整形。
     *
     * <p>当定义了数字整形器时，文本首先由整形器处理，然后再进行其他文本分析。
     *
     * <p><em>注意：</em> 这个值应该对段落中的所有文本都相同，否则行为是不确定的。
     *
     * @see NumericShaper
     * @since 1.4
     */
    public static final TextAttribute NUMERIC_SHAPING =
        new TextAttribute("numeric_shaping");

    /**
     * 请求字距调整的属性键。值为 <b><code>Integer</code></b> 的实例。默认值为 <code>0</code>，表示不请求字距调整。
     *
     * <p>提供了常量值 {@link #KERNING_ON}。
     *
     * <p>某些字符序列的单个字符的默认间距不适当，例如 "To" 或 "AWAY"。没有字距调整时，相邻字符看起来间距过大。字距调整会使选定的字符序列间距不同，以获得更悦目的视觉效果。
     *
     * @since 1.6
     */
    public static final TextAttribute KERNING =
        new TextAttribute("kerning");

    /**
     * 请求标准字距调整。
     * @see #KERNING
     * @since 1.6
     */
    public static final Integer KERNING_ON =
        Integer.valueOf(1);


    /**
     * 请求可选连字的属性键。值为 <b><code>Integer</code></b> 的实例。默认值为 <code>0</code>，表示不使用可选连字。
     *
     * <p>定义了常量值 {@link #LIGATURES_ON}。
     *
     * <p>书写系统要求的连字始终启用。
     *
     * @since 1.6
     */
    public static final TextAttribute LIGATURES =
        new TextAttribute("ligatures");

    /**
     * 请求标准可选连字。
     * @see #LIGATURES
     * @since 1.6
     */
    public static final Integer LIGATURES_ON =
        Integer.valueOf(1);

    /**
     * 控制字距的属性键。值为 <b><code>Number</code></b> 的实例。默认值为 <code>0</code>，表示无额外字距。
     *
     * <p>提供了常量值 {@link #TRACKING_TIGHT} 和 {@link
     * #TRACKING_LOOSE}。
     *
     * <p>字距值乘以字体点大小，并通过字体变换确定要添加到每个字形簇的额外量。正值会抑制可选连字的形成。字距值通常在 <code>-0.1</code> 和 <code>0.3</code> 之间；超出此范围的值通常不理想。
     *
     * @since 1.6
     */
    public static final TextAttribute TRACKING =
        new TextAttribute("tracking");

    /**
     * 执行紧密字距。
     * @see #TRACKING
     * @since 1.6
     */
    public static final Float TRACKING_TIGHT =
        Float.valueOf(-.04f);

    /**
     * 执行宽松字距。
     * @see #TRACKING
     * @since 1.6
     */
    public static final Float TRACKING_LOOSE =
        Float.valueOf(.04f);
}
