
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.peer.FontPeer;
import java.io.*;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import sun.font.StandardGlyphVector;

import sun.font.AttributeMap;
import sun.font.AttributeValues;
import sun.font.CompositeFont;
import sun.font.CreatedFontTracker;
import sun.font.Font2D;
import sun.font.Font2DHandle;
import sun.font.FontAccess;
import sun.font.FontManager;
import sun.font.FontManagerFactory;
import sun.font.FontUtilities;
import sun.font.GlyphLayout;
import sun.font.FontLineMetrics;
import sun.font.CoreMetrics;

import static sun.font.EAttribute.*;

/**
 * <code>Font</code> 类表示用于以可见方式呈现文本的字体。
 * 字体提供了将字符序列映射到字形序列所需的信息，并在 <code>Graphics</code> 和
 * <code>Component</code> 对象上呈现字形序列。
 *
 * <h3>字符和字形</h3>
 *
 * <em>字符</em> 是一个符号，表示一个抽象的项目，如字母、数字或标点符号。例如，<code>'g'</code>，
 * LATIN SMALL LETTER G，是一个字符。
 * <p>
 * <em>字形</em> 是用于呈现字符或字符序列的形状。在简单的书写系统中，如拉丁文，通常一个字形表示一个字符。
 * 但是，字符和字形通常没有一对一的对应关系。例如，字符 '&aacute;' LATIN SMALL LETTER A WITH ACUTE，可以
 * 由两个字形表示：一个表示 'a'，一个表示 '&acute;'。另一方面，两个字符的字符串 "fi" 可以由一个字形表示，
 * 即 "fi" 连字。在复杂的书写系统中，如阿拉伯文或南亚和东南亚的书写系统，字符和字形之间的关系可能更加复杂，
 * 涉及上下文依赖的字形选择以及字形重新排序。
 *
 * 字体封装了呈现选定字符集所需的字形集合以及将字符序列映射到相应字形序列所需的表。
 *
 * <h3>物理字体和逻辑字体</h3>
 *
 * Java 平台区分两种字体：<em>物理</em>字体和<em>逻辑</em>字体。
 * <p>
 * <em>物理</em>字体是包含字形数据和表的实际字体库，用于将字符序列映射到字形序列，使用如 TrueType 或 PostScript Type 1
 * 等字体技术。所有 Java 平台的实现必须支持 TrueType 字体；对其他字体技术的支持取决于实现。物理字体可能使用如
 * Helvetica、Palatino、HonMincho 等名称。通常，每个物理字体仅支持有限的书写系统，例如仅支持拉丁字符或仅支持日文和基本拉丁文。
 * 可用的物理字体集因配置而异。需要特定字体的应用程序可以将它们捆绑并使用 {@link #createFont createFont} 方法实例化。
 * <p>
 * <em>逻辑</em>字体是 Java 平台定义的五个字体系列，必须由任何 Java 运行时环境支持：Serif、SansSerif、Monospaced、Dialog 和 DialogInput。
 * 这些逻辑字体不是实际的字体库。相反，逻辑字体名称由 Java 运行时环境映射到物理字体。这种映射取决于实现，通常还取决于区域设置，因此它们提供的外观和度量标准会有所不同。
 * 通常，每个逻辑字体名称映射到多个物理字体，以覆盖大量字符。
 * <p>
 * 具有对等组件的 AWT 组件，如 {@link Label Label} 和 {@link TextField TextField}，只能使用逻辑字体。
 * <p>
 * 关于使用物理字体或逻辑字体的优缺点的讨论，请参阅
 * <a href="http://www.oracle.com/technetwork/java/javase/tech/faq-jsp-138165.html">国际化 FAQ</a> 文档。
 *
 * <h3>字体面和名称</h3>
 *
 * 一个 <code>Font</code> 可以有许多面，如 heavy、medium、oblique、gothic 和 regular。所有这些面都有相似的排版设计。
 * <p>
 * 从 <code>Font</code> 对象中可以获取三种不同的名称。<em>逻辑字体名称</em> 是用于构造字体的名称。
 * <em>字体面名称</em>，或简称 <em>字体名称</em>，是特定字体面的名称，如 Helvetica Bold。<em>字体系列名称</em> 是确定多个面的排版设计的字体系列的名称，如 Helvetica。
 * <p>
 * <code>Font</code> 类表示系统资源中存在的一组字体面中的一个字体面的实例。例如，Arial Bold 和 Courier Bold Italic 是字体面。
 * 可以有多个 <code>Font</code> 对象与一个字体面关联，每个对象在大小、样式、变换和字体特性上有所不同。
 * <p>
 * <code>GraphicsEnvironment</code> 类的 {@link GraphicsEnvironment#getAllFonts() getAllFonts} 方法返回系统中所有可用的字体面。
 * 这些字体面作为 <code>Font</code> 对象返回，大小为 1，变换为恒等变换，字体特性为默认值。这些基础字体可以用于派生具有不同大小、样式、变换和字体特性的新 <code>Font</code> 对象，
 * 通过此类中的 <code>deriveFont</code> 方法实现。
 *
 * <h3>字体和 TextAttribute</h3>
 *
 * <p><code>Font</code> 支持大多数 <code>TextAttribute</code>。这使得一些操作，如呈现带下划线的文本，更加方便，因为不需要显式构造 <code>TextLayout</code> 对象。
 * 可以通过使用 <code>TextAttribute</code> 值的 <code>Map</code> 构造或派生字体来设置属性。
 *
 * <p>某些 <code>TextAttributes</code> 的值不可序列化，因此尝试序列化包含这些值的 <code>Font</code> 实例将不会序列化这些值。
 * 这意味着从这样的流中反序列化的 Font 将不会与包含不可序列化属性的原始 Font 相等。这很少会成为一个问题，因为这些属性通常仅在特殊情况下使用，不太可能被序列化。
 *
 * <ul>
 * <li><code>FOREGROUND</code> 和 <code>BACKGROUND</code> 使用 <code>Paint</code> 值。子类 <code>Color</code> 是可序列化的，而 <code>GradientPaint</code> 和 <code>TexturePaint</code> 不是。</li>
 * <li><code>CHAR_REPLACEMENT</code> 使用 <code>GraphicAttribute</code> 值。子类 <code>ShapeGraphicAttribute</code> 和 <code>ImageGraphicAttribute</code> 不是可序列化的。</li>
 * <li><code>INPUT_METHOD_HIGHLIGHT</code> 使用 <code>InputMethodHighlight</code> 值，这些值不可序列化。请参阅 {@link java.awt.im.InputMethodHighlight}。</li>
 * </ul>
 *
 * <p>创建 <code>Paint</code> 和 <code>GraphicAttribute</code> 的自定义子类的客户端可以使它们可序列化，以避免此问题。使用输入方法高亮的客户端可以将这些高亮转换为当前平台上的平台特定属性，并将它们设置在 Font 上作为变通方法。
 *
 * <p><code>Map</code> 基础的构造函数和 <code>deriveFont</code> API 忽略 FONT 属性，并且 Font 不保留它；如果可能包含 FONT 属性，应使用静态 {@link #getFont} 方法。请参阅 {@link java.awt.font.TextAttribute#FONT} 以获取更多信息。</p>
 *
 * <p>某些属性会导致额外的渲染开销并可能调用布局。如果 <code>Font</code> 具有这些属性，<code>{@link #hasLayoutAttributes()}</code> 方法将返回 true。</p>
 *
 * <p>注意：字体旋转可能会导致文本基线旋转。为了考虑这种（罕见的）可能性，字体 API 被指定为返回度量标准并采用 '基线相对坐标'。这将 'x' 坐标映射到基线上的前进距离，（正 x 沿基线前进），将 'y' 坐标映射到 'x' 处基线的垂直距离（正 y 为基线向量顺时针 90 度）。对于这一点特别重要的 API，将被标记为具有 '基线相对坐标'。</p>
 */
public class Font implements java.io.Serializable
{
    private static class FontAccessImpl extends FontAccess {
        public Font2D getFont2D(Font font) {
            return font.getFont2D();
        }

        public void setFont2D(Font font, Font2DHandle handle) {
            font.font2DHandle = handle;
        }

        public void setCreatedFont(Font font) {
            font.createdFont = true;
        }

        public boolean isCreatedFont(Font font) {
            return font.createdFont;
        }
    }

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        initIDs();
        FontAccess.setFontAccess(new FontAccessImpl());
    }

    /**
     * 现在仅在序列化时使用。通常为 null。
     *
     * @serial
     * @see #getAttributes()
     */
    private Hashtable<Object, Object> fRequestedAttributes;

    /*
     * 用于逻辑字体系列名称的常量。
     */

    /**
     * 逻辑字体 "Dialog" 的规范系列名称的字符串常量。在字体构造中提供编译时验证名称时非常有用。
     * @since 1.6
     */
    public static final String DIALOG = "Dialog";

    /**
     * 逻辑字体 "DialogInput" 的规范系列名称的字符串常量。在字体构造中提供编译时验证名称时非常有用。
     * @since 1.6
     */
    public static final String DIALOG_INPUT = "DialogInput";

    /**
     * 逻辑字体 "SansSerif" 的规范系列名称的字符串常量。在字体构造中提供编译时验证名称时非常有用。
     * @since 1.6
     */
    public static final String SANS_SERIF = "SansSerif";

    /**
     * 逻辑字体 "Serif" 的规范系列名称的字符串常量。在字体构造中提供编译时验证名称时非常有用。
     * @since 1.6
     */
    public static final String SERIF = "Serif";

    /**
     * 逻辑字体 "Monospaced" 的规范系列名称的字符串常量。在字体构造中提供编译时验证名称时非常有用。
     * @since 1.6
     */
    public static final String MONOSPACED = "Monospaced";

    /*
     * 用于样式的常量。可以组合使用以混合样式。
     */

    /**
     * 平常样式常量。
     */
    public static final int PLAIN       = 0;

    /**
     * 加粗样式常量。可以与其它样式常量（除 PLAIN 外）组合使用以混合样式。
     */
    public static final int BOLD        = 1;

    /**
     * 斜体样式常量。可以与其它样式常量（除 PLAIN 外）组合使用以混合样式。
     */
    public static final int ITALIC      = 2;

    /**
     * 在布局文本时大多数罗马脚本使用的基线。
     */
    public static final int ROMAN_BASELINE = 0;

    /**
     * 在布局文本时中文、日文和韩文等表意脚本使用的基线。
     */
    public static final int CENTER_BASELINE = 1;

    /**
     * 在布局文本时天城文等脚本使用的基线。
     */
    public static final int HANGING_BASELINE = 2;

    /**
     * 识别类型为 TRUETYPE 的字体资源。用于指定 TrueType 字体资源给 {@link #createFont} 方法。
     * TrueType 格式被扩展为 OpenType 格式，后者增加了对具有 Postscript 轮廓的字体的支持，因此此标签也引用这些字体，以及具有 TrueType 轮廓的字体。
     * @since 1.3
     */

    public static final int TRUETYPE_FONT = 0;

    /**
     * 识别类型为 TYPE1 的字体资源。用于指定 Type1 字体资源给 {@link #createFont} 方法。
     * @since 1.5
     */
    public static final int TYPE1_FONT = 1;

    /**
     * 传递给构造函数的此 <code>Font</code> 的逻辑名称。
     * @since JDK1.0
     *
     * @serial
     * @see #getName
     */
    protected String name;

    /**
     * 传递给构造函数的此 <code>Font</code> 的样式。此样式可以是 PLAIN、BOLD、ITALIC 或 BOLD+ITALIC。
     * @since JDK1.0
     *
     * @serial
     * @see #getStyle()
     */
    protected int style;

    /**
     * 四舍五入到整数的此 <code>Font</code> 的点大小。
     * @since JDK1.0
     *
     * @serial
     * @see #getSize()
     */
    protected int size;

    /**
     * 此 <code>Font</code> 的点大小，以 <code>float</code> 表示。
     *
     * @serial
     * @see #getSize()
     * @see #getSize2D()
     */
    protected float pointSize;


                /**
     * 平台特定的字体信息。
     */
    private transient FontPeer peer;
    private transient long pData;       // 本地 JDK1.1 字体指针
    private transient Font2DHandle font2DHandle;

    private transient AttributeValues values;
    private transient boolean hasLayoutAttributes;

    /*
     * 如果字体的来源是创建的字体，则此属性
     * 必须设置在所有派生字体上。
     */
    private transient boolean createdFont = false;

    /*
     * 如果字体变换不是恒等变换，则此值为 true。它
     * 用于避免不必要的 AffineTransform 实例化。
     */
    private transient boolean nonIdentityTx;

    /*
     * 当需要变换时使用的缓存值。此值不能暴露给调用者，因为 AffineTransform
     * 是可变的。
     */
    private static final AffineTransform identityTx = new AffineTransform();

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -4206021311591459213L;

    /**
     * 获取此 <code>Font</code> 的 peer。
     * @return 该 <code>Font</code> 的 peer。
     * @since JDK1.1
     * @deprecated 字体渲染现在是平台无关的。
     */
    @Deprecated
    public FontPeer getPeer(){
        return getPeer_NoClientCode();
    }
    // 注意：此方法由特权线程调用。
    // 我们在包私有方法中实现此功能
    // 以确保它不能被客户端子类覆盖。
    // 不要在该线程上调用客户端代码！
    @SuppressWarnings("deprecation")
    final FontPeer getPeer_NoClientCode() {
        if(peer == null) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            this.peer = tk.getFontPeer(name, style);
        }
        return peer;
    }

    /**
     * 返回与此字体关联的 AttributeValues 对象。大多数情况下，内部对象为 null。
     * 如果需要，将从字体的“标准”状态创建它。只有非默认值将
     * 设置在 AttributeValues 对象中。
     *
     * <p>由于 AttributeValues 对象是可变的，并且在字体中缓存，因此必须小心确保
     * 它不会被修改。
     */
    private AttributeValues getAttributeValues() {
        if (values == null) {
            AttributeValues valuesTmp = new AttributeValues();
            valuesTmp.setFamily(name);
            valuesTmp.setSize(pointSize); // 期望浮点值。

            if ((style & BOLD) != 0) {
                valuesTmp.setWeight(2); // WEIGHT_BOLD
            }

            if ((style & ITALIC) != 0) {
                valuesTmp.setPosture(.2f); // POSTURE_OBLIQUE
            }
            valuesTmp.defineAll(PRIMARY_MASK); // 为了流兼容性
            values = valuesTmp;
        }

        return values;
    }

    private Font2D getFont2D() {
        FontManager fm = FontManagerFactory.getInstance();
        if (fm.usingPerAppContextComposites() &&
            font2DHandle != null &&
            font2DHandle.font2D instanceof CompositeFont &&
            ((CompositeFont)(font2DHandle.font2D)).isStdComposite()) {
            return fm.findFont2D(name, style,
                                          FontManager.LOGICAL_FALLBACK);
        } else if (font2DHandle == null) {
            font2DHandle =
                fm.findFont2D(name, style,
                              FontManager.LOGICAL_FALLBACK).handle;
        }
        /* 不要缓存解除引用的 font2D。必须显式解除引用
         * 以在原始字体被标记为无效时选择一个有效的字体
         */
        return font2DHandle.font2D;
    }

    /**
     * 从指定的名称、样式和点大小创建一个新的 <code>Font</code>。
     * <p>
     * 字体名称可以是字体面名称或字体族名称。它与样式一起使用以找到合适的字体面。
     * 当指定字体族名称时，样式参数用于从族中选择最合适的面。当指定字体面
     * 名称时，面的样式和样式参数将合并以从同一族中找到最匹配的字体。
     * 例如，如果指定了面名称 "Arial Bold" 并且样式为
     * <code>Font.ITALIC</code>，则字体系统会在 "Arial" 族中查找粗体和斜体的面，
     * 并可能将字体实例与物理字体面 "Arial Bold Italic" 关联。
     * 样式参数与指定面的样式合并，而不是添加或减去。
     * 这意味着，指定粗体面和粗体样式不会使字体加倍加粗，而指定粗体面和平面
     * 样式也不会使字体变轻。
     * <p>
     * 如果找不到请求样式的面，字体系统可能会应用算法样式以实现所需的样式。
     * 例如，如果请求 <code>ITALIC</code>，但没有斜体面可用，可能会从平面
     * 应用算法斜体（倾斜）。
     * <p>
     * 字体名称查找不区分大小写，使用美国地区的大小写折叠规则。
     * <p>
     * 如果 <code>name</code> 参数表示的不是逻辑字体，即被解释为物理字体面或族，
     * 并且实现无法将其映射到物理字体或兼容的替代品，则字体系统将把字体实例
     * 映射到 "Dialog"，例如，由 {@link #getFamily() getFamily} 报告的族名称
     * 将是 "Dialog"。
     * <p>
     *
     * @param name 字体名称。这可以是字体面名称或字体族名称，可以表示逻辑字体或
     * 物理字体。逻辑字体的族名称为：Dialog, DialogInput,
     * Monospaced, Serif, 或 SansSerif。预定义的字符串常量存在
     * 用于所有这些名称，例如，{@code DIALOG}。如果 {@code name} 为
     * {@code null}，则新 {@code Font} 的 <em>逻辑字体名称</em> 由 {@code getName()} 返回
     * 设置为 "Default"。
     * @param style 字体的样式常量
     * 样式参数是一个整数位掩码，可以是 {@code PLAIN}，或 {@code BOLD} 和/或
     * {@code ITALIC} 的按位或（例如，{@code ITALIC} 或 {@code BOLD|ITALIC}）。
     * 如果样式参数不符合预期的整数位掩码，则样式设置为 {@code PLAIN}。
     * @param size 字体的点大小
     * @see GraphicsEnvironment#getAllFonts
     * @see GraphicsEnvironment#getAvailableFontFamilyNames
     * @since JDK1.0
     */
    public Font(String name, int style, int size) {
        this.name = (name != null) ? name : "Default";
        this.style = (style & ~0x03) == 0 ? style : 0;
        this.size = size;
        this.pointSize = size;
    }

    private Font(String name, int style, float sizePts) {
        this.name = (name != null) ? name : "Default";
        this.style = (style & ~0x03) == 0 ? style : 0;
        this.size = (int)(sizePts + 0.5);
        this.pointSize = sizePts;
    }

    /* 此构造函数由 deriveFont 在属性为 null 时使用 */
    private Font(String name, int style, float sizePts,
                 boolean created, Font2DHandle handle) {
        this(name, style, sizePts);
        this.createdFont = created;
        /* 从流创建的字体将使用与父字体相同的 font2D 实例。
         * 一个例外是，如果请求的派生字体是不同样式，则还检查它是否为 CompositeFont
         * 如果是，则从该样式的组件构建新的 CompositeFont。
         * CompositeFonts 只有在用于为物理字体添加回退时才能标记为“创建”。而非组合字体
         * 始终来自 "Font.createFont()"，不应接受这种处理。
         */
        if (created) {
            if (handle.font2D instanceof CompositeFont &&
                handle.font2D.getStyle() != style) {
                FontManager fm = FontManagerFactory.getInstance();
                this.font2DHandle = fm.getNewComposite(null, style, handle);
            } else {
                this.font2DHandle = handle;
            }
        }
    }

    /* 用于实现 Font.createFont */
    private Font(File fontFile, int fontFormat,
                 boolean isCopy, CreatedFontTracker tracker)
        throws FontFormatException {
        this.createdFont = true;
        /* 通过此方法创建的 Font2D 实例跟踪其字体文件
         * 以便在 Font2D 被 GC 时也可以删除文件。
         */
        FontManager fm = FontManagerFactory.getInstance();
        this.font2DHandle = fm.createFont2D(fontFile, fontFormat, isCopy,
                                            tracker).handle;
        this.name = this.font2DHandle.font2D.getFontName(Locale.getDefault());
        this.style = Font.PLAIN;
        this.size = 1;
        this.pointSize = 1f;
    }

    /* 当一个字体从另一个字体派生时使用此构造函数。
     * 从流创建的字体将使用与父字体相同的 font2D 实例。它们可以通过
     * "created" 参数为 "true" 来区分。由于无法重新创建这些字体，因此
     * 需要传递底层 font2D 的句柄。
     * "created" 也为 true 时，当引用特殊组合时，原因基本相同。
     * 但在这些情况下，两个特定属性需要特别注意：族/面和样式。
     * 这些情况下的“组合”需要使用新的族和样式的最佳字体重新创建。
     * 对于使用 createFont() 创建的字体，这些属性的处理方式不同。
     * JDK 通常可以合成不同的样式（例如，从平面合成粗体）。对于使用
     * "createFont" 创建的字体，这是一个合理的解决方案，但也可以（尽管罕见）派生一个
     * 不同族属性的字体。在这种情况下，JDK 需要
     * 与原始 Font2D 断开联系并找到新的字体。
     * 提供 oldName 和 oldStyle 以便与 Font2D 和 values 进行比较。为了加快速度：
     * oldName == null 将被解释为名称未更改。
     * oldStyle = -1 将被解释为样式未更改。
     * 在这些情况下，无需查询 "values"。
     */
    private Font(AttributeValues values, String oldName, int oldStyle,
                 boolean created, Font2DHandle handle) {

        this.createdFont = created;
        if (created) {
            this.font2DHandle = handle;

            String newName = null;
            if (oldName != null) {
                newName = values.getFamily();
                if (oldName.equals(newName)) newName = null;
            }
            int newStyle = 0;
            if (oldStyle == -1) {
                newStyle = -1;
            } else {
                if (values.getWeight() >= 2f)   newStyle  = BOLD;
                if (values.getPosture() >= .2f) newStyle |= ITALIC;
                if (oldStyle == newStyle)       newStyle  = -1;
            }
            if (handle.font2D instanceof CompositeFont) {
                if (newStyle != -1 || newName != null) {
                    FontManager fm = FontManagerFactory.getInstance();
                    this.font2DHandle =
                        fm.getNewComposite(newName, newStyle, handle);
                }
            } else if (newName != null) {
                this.createdFont = false;
                this.font2DHandle = null;
            }
        }
        initFromValues(values);
    }

    /**
     * 使用指定的属性创建一个新的 <code>Font</code>。只有在 {@link java.awt.font.TextAttribute TextAttribute}
     * 中定义的键被识别。此外，FONT 属性
     * 不被此构造函数识别（参见 {@link #getAvailableAttributes}）。只有具有有效类型的属性值
     * 会影响新的 <code>Font</code>。
     * <p>
     * 如果 <code>attributes</code> 为 <code>null</code>，则使用默认值初始化新的
     * <code>Font</code>。
     * @see java.awt.font.TextAttribute
     * @param attributes 要分配给新 <code>Font</code> 的属性，或 <code>null</code>
     */
    public Font(Map<? extends Attribute, ?> attributes) {
        initFromValues(AttributeValues.fromMap(attributes, RECOGNIZED_MASK));
    }

    /**
     * 从指定的 <code>font</code> 创建一个新的 <code>Font</code>。
     * 此构造函数旨在供子类使用。
     * @param font 用于创建此 <code>Font</code> 的字体。
     * @throws NullPointerException 如果 <code>font</code> 为 null
     * @since 1.6
     */
    protected Font(Font font) {
        if (font.values != null) {
            initFromValues(font.getAttributeValues().clone());
        } else {
            this.name = font.name;
            this.style = font.style;
            this.size = font.size;
            this.pointSize = font.pointSize;
        }
        this.font2DHandle = font.font2DHandle;
        this.createdFont = font.createdFont;
    }

    /**
     * 字体识别所有属性，除了 FONT。
     */
    private static final int RECOGNIZED_MASK = AttributeValues.MASK_ALL
        & ~AttributeValues.getMask(EFONT);

    /**
     * 这些属性被认为是 FONT 属性的主要属性。
     */
    private static final int PRIMARY_MASK =
        AttributeValues.getMask(EFAMILY, EWEIGHT, EWIDTH, EPOSTURE, ESIZE,
                                ETRANSFORM, ESUPERSCRIPT, ETRACKING);

    /**
     * 这些属性被认为是 FONT 属性的次要属性。
     */
    private static final int SECONDARY_MASK =
        RECOGNIZED_MASK & ~PRIMARY_MASK;

    /**
     * 这些属性由布局处理。
     */
    private static final int LAYOUT_MASK =
        AttributeValues.getMask(ECHAR_REPLACEMENT, EFOREGROUND, EBACKGROUND,
                                EUNDERLINE, ESTRIKETHROUGH, ERUN_DIRECTION,
                                EBIDI_EMBEDDING, EJUSTIFICATION,
                                EINPUT_METHOD_HIGHLIGHT, EINPUT_METHOD_UNDERLINE,
                                ESWAP_COLORS, ENUMERIC_SHAPING, EKERNING,
                                ELIGATURES, ETRACKING, ESUPERSCRIPT);

    private static final int EXTRA_MASK =
            AttributeValues.getMask(ETRANSFORM, ESUPERSCRIPT, EWIDTH);

    /**
     * 从 values 对象初始化标准字体字段。
     */
    private void initFromValues(AttributeValues values) {
        this.values = values;
        values.defineAll(PRIMARY_MASK); // 为了 1.5 流兼容性

        this.name = values.getFamily();
        this.pointSize = values.getSize();
        this.size = (int)(values.getSize() + 0.5);
        if (values.getWeight() >= 2f) this.style |= BOLD; // 不等于 2f
        if (values.getPosture() >= .2f) this.style |= ITALIC; // 不等于 .2f


                    this.nonIdentityTx = values.anyNonDefault(EXTRA_MASK);
        this.hasLayoutAttributes =  values.anyNonDefault(LAYOUT_MASK);
    }

    /**
     * 返回一个适合属性的<code>Font</code>。
     * 如果<code>attributes</code>包含一个有效的<code>Font</code>作为<code>FONT</code>属性的值，
     * 它将与任何剩余的属性合并。有关更多信息，请参见
     * {@link java.awt.font.TextAttribute#FONT}。
     *
     * @param attributes 要分配给新<code>Font</code>的属性
     * @return 用指定属性创建的新<code>Font</code>
     * @throws NullPointerException 如果<code>attributes</code>为null。
     * @since 1.2
     * @see java.awt.font.TextAttribute
     */
    public static Font getFont(Map<? extends Attribute, ?> attributes) {
        // 优化两种情况：
        // 1) 仅FONT属性
        // 2) 属性，但没有FONT

        // 避免无故将属性映射转换为普通映射
        if (attributes instanceof AttributeMap &&
            ((AttributeMap)attributes).getValues() != null) {
            AttributeValues values = ((AttributeMap)attributes).getValues();
            if (values.isNonDefault(EFONT)) {
                Font font = values.getFont();
                if (!values.anyDefined(SECONDARY_MASK)) {
                    return font;
                }
                // 合并
                values = font.getAttributeValues().clone();
                values.merge(attributes, SECONDARY_MASK);
                return new Font(values, font.name, font.style,
                                font.createdFont, font.font2DHandle);
            }
            return new Font(attributes);
        }

        Font font = (Font)attributes.get(TextAttribute.FONT);
        if (font != null) {
            if (attributes.size() > 1) { // 好吧，检查是否有其他内容
                AttributeValues values = font.getAttributeValues().clone();
                values.merge(attributes, SECONDARY_MASK);
                return new Font(values, font.name, font.style,
                                font.createdFont, font.font2DHandle);
            }

            return font;
        }

        return new Font(attributes);
    }

    /**
     * 用于从流创建字体时的字节数跟踪器。
     * 如果线程可以创建临时文件，就没有必要计算字体字节数。
     */
    private static boolean hasTempPermission() {

        if (System.getSecurityManager() == null) {
            return true;
        }
        File f = null;
        boolean hasPerm = false;
        try {
            f = Files.createTempFile("+~JT", ".tmp").toFile();
            f.delete();
            f = null;
            hasPerm = true;
        } catch (Throwable t) {
            /* 包括任何类型的SecurityException */
        }
        return hasPerm;
    }

    /**
     * 使用指定的字体类型和输入数据返回一个新的<code>Font</code>。
     * 新的<code>Font</code>以1点大小和样式{@link #PLAIN PLAIN}创建。
     * 可以使用此类中的<code>deriveFont</code>方法从这个基础字体派生出具有不同大小、样式、变换和字体特征的新<code>Font</code>对象。
     * 此方法不会关闭{@link InputStream}。
     * <p>
     * 要使<code>Font</code>可用于Font构造函数，必须通过调用
     * {@link GraphicsEnvironment#registerFont(Font) registerFont(Font)}将返回的<code>Font</code>注册到<code>GraphicsEnviroment</code>。
     * @param fontFormat 字体类型，如果指定了TrueType资源，则为
     * {@link #TRUETYPE_FONT TRUETYPE_FONT}，如果指定了Type 1资源，则为
     * {@link #TYPE1_FONT TYPE1_FONT}。
     * @param fontStream 代表字体输入数据的<code>InputStream</code>对象。
     * @return 用指定字体类型创建的新<code>Font</code>。
     * @throws IllegalArgumentException 如果<code>fontFormat</code>不是
     *     <code>TRUETYPE_FONT</code>或<code>TYPE1_FONT</code>。
     * @throws FontFormatException 如果<code>fontStream</code>数据不包含指定格式所需的字体表。
     * @throws IOException 如果<code>fontStream</code>
     *     无法完全读取。
     * @see GraphicsEnvironment#registerFont(Font)
     * @since 1.3
     */
    public static Font createFont(int fontFormat, InputStream fontStream)
        throws java.awt.FontFormatException, java.io.IOException {

        if (hasTempPermission()) {
            return createFont0(fontFormat, fontStream, null);
        }

        // 否则，特别注意待创建的临时文件和资源处理
        CreatedFontTracker tracker = CreatedFontTracker.getTracker();
        boolean acquired = false;
        try {
            acquired = tracker.acquirePermit();
            if (!acquired) {
                throw new IOException("等待资源超时。");
            }
            return createFont0(fontFormat, fontStream, tracker);
        } catch (InterruptedException e) {
            throw new IOException("读取字体数据时出现问题。");
        } finally {
            if (acquired) {
                tracker.releasePermit();
            }
        }
    }

    private static Font createFont0(int fontFormat, InputStream fontStream,
                                    CreatedFontTracker tracker)
        throws java.awt.FontFormatException, java.io.IOException {

        if (fontFormat != Font.TRUETYPE_FONT &&
            fontFormat != Font.TYPE1_FONT) {
            throw new IllegalArgumentException ("字体格式未识别");
        }
        boolean copiedFontData = false;
        try {
            final File tFile = AccessController.doPrivileged(
                new PrivilegedExceptionAction<File>() {
                    public File run() throws IOException {
                        return Files.createTempFile("+~JF", ".tmp").toFile();
                    }
                }
            );
            if (tracker != null) {
                tracker.add(tFile);
            }

            int totalSize = 0;
            try {
                final OutputStream outStream =
                    AccessController.doPrivileged(
                        new PrivilegedExceptionAction<OutputStream>() {
                            public OutputStream run() throws IOException {
                                return new FileOutputStream(tFile);
                            }
                        }
                    );
                if (tracker != null) {
                    tracker.set(tFile, outStream);
                }
                try {
                    byte[] buf = new byte[8192];
                    for (;;) {
                        int bytesRead = fontStream.read(buf);
                        if (bytesRead < 0) {
                            break;
                        }
                        if (tracker != null) {
                            if (totalSize+bytesRead > CreatedFontTracker.MAX_FILE_SIZE) {
                                throw new IOException("文件太大。");
                            }
                            if (totalSize+tracker.getNumBytes() >
                                CreatedFontTracker.MAX_TOTAL_BYTES)
                              {
                                throw new IOException("总文件太大。");
                            }
                            totalSize += bytesRead;
                            tracker.addBytes(bytesRead);
                        }
                        outStream.write(buf, 0, bytesRead);
                    }
                    /* 不关闭输入流 */
                } finally {
                    outStream.close();
                }
                /* 在所有对Font2D的引用都被释放后，文件将被删除。
                 * 为了支持长期存在的AppContexts，我们需要减去文件的大小。
                 * 如果数据不是有效的字体，实现将在构造函数返回之前删除临时文件并减少跟踪器对象中的字节数，
                 * 因此我们可以在这里将'copiedFontData'设置为true，而无需等待构造函数的结果。
                 */
                copiedFontData = true;
                Font font = new Font(tFile, fontFormat, true, tracker);
                return font;
            } finally {
                if (tracker != null) {
                    tracker.remove(tFile);
                }
                if (!copiedFontData) {
                    if (tracker != null) {
                        tracker.subBytes(totalSize);
                    }
                    AccessController.doPrivileged(
                        new PrivilegedExceptionAction<Void>() {
                            public Void run() {
                                tFile.delete();
                                return null;
                            }
                        }
                    );
                }
            }
        } catch (Throwable t) {
            if (t instanceof FontFormatException) {
                throw (FontFormatException)t;
            }
            if (t instanceof IOException) {
                throw (IOException)t;
            }
            Throwable cause = t.getCause();
            if (cause instanceof FontFormatException) {
                throw (FontFormatException)cause;
            }
            throw new IOException("读取字体数据时出现问题。");
        }
    }

    /**
     * 使用指定的字体类型和指定的字体文件返回一个新的<code>Font</code>。
     * 新的<code>Font</code>以1点大小和样式{@link #PLAIN PLAIN}创建。
     * 可以使用此类中的<code>deriveFont</code>方法从这个基础字体派生出具有不同大小、样式、变换和字体特征的新<code>Font</code>对象。
     * 只要返回的字体或其派生字体被引用，实现可能会继续访问<code>fontFile</code>以检索字体数据。
     * 因此，如果文件被更改或变得不可访问，结果是未定义的。
     * <p>
     * 要使<code>Font</code>可用于Font构造函数，必须通过调用
     * {@link GraphicsEnvironment#registerFont(Font) registerFont(Font)}将返回的<code>Font</code>注册到<code>GraphicsEnviroment</code>。
     * @param fontFormat 字体类型，如果指定了TrueType资源，则为
     * {@link #TRUETYPE_FONT TRUETYPE_FONT}，如果指定了Type 1资源，则为
     * {@link #TYPE1_FONT TYPE1_FONT}。
     * @param fontFile 代表字体输入数据的<code>File</code>对象。
     * @return 用指定字体类型创建的新<code>Font</code>。
     * @throws IllegalArgumentException 如果<code>fontFormat</code>不是
     *     <code>TRUETYPE_FONT</code>或<code>TYPE1_FONT</code>。
     * @throws NullPointerException 如果<code>fontFile</code>为null。
     * @throws IOException 如果<code>fontFile</code>无法读取。
     * @throws FontFormatException 如果<code>fontFile</code>不包含指定格式所需的字体表。
     * @throws SecurityException 如果执行代码没有权限读取文件。
     * @see GraphicsEnvironment#registerFont(Font)
     * @since 1.5
     */
    public static Font createFont(int fontFormat, File fontFile)
        throws java.awt.FontFormatException, java.io.IOException {

        fontFile = new File(fontFile.getPath());

        if (fontFormat != Font.TRUETYPE_FONT &&
            fontFormat != Font.TYPE1_FONT) {
            throw new IllegalArgumentException ("字体格式未识别");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            FilePermission filePermission =
                new FilePermission(fontFile.getPath(), "read");
            sm.checkPermission(filePermission);
        }
        if (!fontFile.canRead()) {
            throw new IOException("无法读取 " + fontFile);
        }
        return new Font(fontFile, fontFormat, false, null);
    }

    /**
     * 返回与此<code>Font</code>关联的变换的副本。
     * 此变换不一定用于构造字体。如果字体有算法上标或宽度调整，这将被纳入返回的<code>AffineTransform</code>中。
     * <p>
     * 通常，字体不会被变换。客户端通常应该先调用{@link #isTransformed}，只有当<code>isTransformed</code>返回true时才调用此方法。
     *
     * @return 一个表示此<code>Font</code>对象变换属性的{@link AffineTransform}对象。
     */
    public AffineTransform getTransform() {
        /* 最常见的情况是单位变换。大多数调用者应该先调用isTransformed()，以决定是否需要获取变换，
         * 但有些可能不会。这里我们检查是否有非单位变换，只有在这种情况下才执行获取和/或计算变换的工作，
         * 否则返回一个新的单位变换。
         *
         * 注意，变换不一定与作为Map中的Attribute传递的变换相同，因为返回的变换还将反映WIDTH和SUPERSCRIPT属性的效果。
         * 需要实际变换的客户端应该调用getRequestedAttributes。
         */
        if (nonIdentityTx) {
            AttributeValues values = getAttributeValues();

            AffineTransform at = values.isNonDefault(ETRANSFORM)
                ? new AffineTransform(values.getTransform())
                : new AffineTransform();

            if (values.getSuperscript() != 0) {
                // 无法在这里获取ascent和descent，因为会递归调用此函数，
                // 所以使用pointsize
                // 允许用户组合上标和下标

                int superscript = values.getSuperscript();

                double trans = 0;
                int n = 0;
                boolean up = superscript > 0;
                int sign = up ? -1 : 1;
                int ss = up ? superscript : -superscript;

                while ((ss & 7) > n) {
                    int newn = ss & 7;
                    trans += sign * (ssinfo[newn] - ssinfo[n]);
                    ss >>= 3;
                    sign = -sign;
                    n = newn;
                }
                trans *= pointSize;
                double scale = Math.pow(2./3., n);

                at.preConcatenate(AffineTransform.getTranslateInstance(0, trans));
                at.scale(scale, scale);


                            // 有关放置和斜体的注释
                // 我们预先连接变换，因为我们不想沿着斜体角度平移，而是纯粹垂直于基线。虽然这对上标看起来不错，但可能导致下标堆叠在一起
                // 并使后续文本过于靠近。我们处理斜体可能导致的潜在碰撞的方式是调整相邻字形向量的水平间距。检查两个向量的斜体角度，如果其中一个非零，
                // 计算最小上升和下降，然后沿其（偏移）基线的斜体角度计算每个向量的x位置。计算x位置之间的差异，并使用最大差异来调整右侧gv的位置。
            }

            if (values.isNonDefault(EWIDTH)) {
                at.scale(values.getWidth(), 1f);
            }

            return at;
        }

        return new AffineTransform();
    }

    // x = r^0 + r^1 + r^2... r^n
    // rx = r^1 + r^2 + r^3... r^(n+1)
    // x - rx = r^0 - r^(n+1)
    // x (1 - r) = r^0 - r^(n+1)
    // x = (r^0 - r^(n+1)) / (1 - r)
    // x = (1 - r^(n+1)) / (1 - r)

    // 缩放比例为 2/3
    // trans = 上升高度的一半 * x
    // 假设上升高度是点大小的 3/4

    private static final float[] ssinfo = {
        0.0f,
        0.375f,
        0.625f,
        0.7916667f,
        0.9027778f,
        0.9768519f,
        1.0262346f,
        1.0591564f,
    };

    /**
     * 返回此 <code>Font</code> 的家族名称。
     *
     * <p>字体的家族名称是特定于字体的。例如，Helvetica Italic 和 Helvetica Bold 有相同的家族名称，<i>Helvetica</i>，
     * 而它们的字体面名称分别是 <i>Helvetica Bold</i> 和 <i>Helvetica Italic</i>。可以通过使用
     * {@link GraphicsEnvironment#getAvailableFontFamilyNames()} 方法获取可用的家族名称列表。
     *
     * <p>使用 <code>getName</code> 获取字体的逻辑名称。使用 <code>getFontName</code> 获取字体的字体面名称。
     * @return 一个 <code>String</code>，表示此 <code>Font</code> 的家族名称。
     *
     * @see #getName
     * @see #getFontName
     * @since JDK1.1
     */
    public String getFamily() {
        return getFamily_NoClientCode();
    }
    // 注意：此方法由特权线程调用。
    //       我们在包私有方法中实现此功能，以确保它不能被客户端子类覆盖。
    //       不要在该线程上调用客户端代码！
    final String getFamily_NoClientCode() {
        return getFamily(Locale.getDefault());
    }

    /**
     * 返回此 <code>Font</code> 的家族名称，针对指定的区域设置进行本地化。
     *
     * <p>字体的家族名称是特定于字体的。例如，Helvetica Italic 和 Helvetica Bold 有相同的家族名称，<i>Helvetica</i>，
     * 而它们的字体面名称分别是 <i>Helvetica Bold</i> 和 <i>Helvetica Italic</i>。可以通过使用
     * {@link GraphicsEnvironment#getAvailableFontFamilyNames()} 方法获取可用的家族名称列表。
     *
     * <p>使用 <code>getFontName</code> 获取字体的字体面名称。
     * @param l 要获取家族名称的区域设置
     * @return 一个 <code>String</code>，表示字体的家族名称，针对指定的区域设置进行本地化。
     * @see #getFontName
     * @see java.util.Locale
     * @since 1.2
     */
    public String getFamily(Locale l) {
        if (l == null) {
            throw new NullPointerException("null locale doesn't mean default");
        }
        return getFont2D().getFamilyName(l);
    }

    /**
     * 返回此 <code>Font</code> 的 PostScript 名称。
     * 使用 <code>getFamily</code> 获取字体的家族名称。使用 <code>getFontName</code> 获取字体的字体面名称。
     * @return 一个 <code>String</code>，表示此 <code>Font</code> 的 PostScript 名称。
     * @since 1.2
     */
    public String getPSName() {
        return getFont2D().getPostscriptName();
    }

    /**
     * 返回此 <code>Font</code> 的逻辑名称。
     * 使用 <code>getFamily</code> 获取字体的家族名称。使用 <code>getFontName</code> 获取字体的字体面名称。
     * @return 一个 <code>String</code>，表示此 <code>Font</code> 的逻辑名称。
     * @see #getFamily
     * @see #getFontName
     * @since JDK1.0
     */
    public String getName() {
        return name;
    }

    /**
     * 返回此 <code>Font</code> 的字体面名称。例如，可以返回 Helvetica Bold 作为字体面名称。
     * 使用 <code>getFamily</code> 获取字体的家族名称。使用 <code>getName</code> 获取字体的逻辑名称。
     * @return 一个 <code>String</code>，表示此 <code>Font</code> 的字体面名称。
     * @see #getFamily
     * @see #getName
     * @since 1.2
     */
    public String getFontName() {
      return getFontName(Locale.getDefault());
    }

    /**
     * 返回此 <code>Font</code> 的字体面名称，针对指定的区域设置进行本地化。例如，可以返回 Helvetica Fett 作为字体面名称。
     * 使用 <code>getFamily</code> 获取字体的家族名称。
     * @param l 要获取字体面名称的区域设置
     * @return 一个 <code>String</code>，表示字体面名称，针对指定的区域设置进行本地化。
     * @see #getFamily
     * @see java.util.Locale
     */
    public String getFontName(Locale l) {
        if (l == null) {
            throw new NullPointerException("null locale doesn't mean default");
        }
        return getFont2D().getFontName(l);
    }

    /**
     * 返回此 <code>Font</code> 的样式。样式可以是 PLAIN、BOLD、ITALIC 或 BOLD+ITALIC。
     * @return 此 <code>Font</code> 的样式
     * @see #isPlain
     * @see #isBold
     * @see #isItalic
     * @since JDK1.0
     */
    public int getStyle() {
        return style;
    }

    /**
     * 返回此 <code>Font</code> 的点大小，四舍五入为整数。
     * 大多数用户熟悉使用 <i>点大小</i> 来指定字体中字形的大小。这个点大小定义了单行文本文档中一行基线到下一行基线的距离。
     * 点大小基于 <i>印刷点</i>，大约是 1/72 英寸。
     * <p>
     * Java(tm)2D API 采用的约定是，一个点相当于用户坐标中的一个单位。当使用规范化变换将用户空间坐标转换为设备空间坐标时，
     * 72 个用户空间单位等于设备空间中的 1 英寸。在这种情况下，一个点是 1/72 英寸。
     * @return 此 <code>Font</code> 的点大小，单位为 1/72 英寸。
     * @see #getSize2D
     * @see GraphicsConfiguration#getDefaultTransform
     * @see GraphicsConfiguration#getNormalizingTransform
     * @since JDK1.0
     */
    public int getSize() {
        return size;
    }

    /**
     * 返回此 <code>Font</code> 的点大小，以 <code>float</code> 值表示。
     * @return 此 <code>Font</code> 的点大小，以 <code>float</code> 值表示。
     * @see #getSize
     * @since 1.2
     */
    public float getSize2D() {
        return pointSize;
    }

    /**
     * 指示此 <code>Font</code> 对象的样式是否为 PLAIN。
     * @return    如果此 <code>Font</code> 的样式为 PLAIN，则返回 <code>true</code>；
     *            否则返回 <code>false</code>。
     * @see       java.awt.Font#getStyle
     * @since     JDK1.0
     */
    public boolean isPlain() {
        return style == 0;
    }

    /**
     * 指示此 <code>Font</code> 对象的样式是否为 BOLD。
     * @return    如果此 <code>Font</code> 对象的样式为 BOLD，则返回 <code>true</code>；
     *            否则返回 <code>false</code>。
     * @see       java.awt.Font#getStyle
     * @since     JDK1.0
     */
    public boolean isBold() {
        return (style & BOLD) != 0;
    }

    /**
     * 指示此 <code>Font</code> 对象的样式是否为 ITALIC。
     * @return    如果此 <code>Font</code> 对象的样式为 ITALIC，则返回 <code>true</code>；
     *            否则返回 <code>false</code>。
     * @see       java.awt.Font#getStyle
     * @since     JDK1.0
     */
    public boolean isItalic() {
        return (style & ITALIC) != 0;
    }

    /**
     * 指示此 <code>Font</code> 对象是否有影响其大小的变换，除了 Size 属性之外。
     * @return  如果此 <code>Font</code> 对象具有非恒等的 AffineTransform 属性，则返回 <code>true</code>；
     *          否则返回 <code>false</code>。
     * @see     java.awt.Font#getTransform
     * @since   1.4
     */
    public boolean isTransformed() {
        return nonIdentityTx;
    }

    /**
     * 如果此 Font 包含需要额外布局处理的属性，则返回 true。
     * @return 如果字体具有布局属性，则返回 true
     * @since 1.6
     */
    public boolean hasLayoutAttributes() {
        return hasLayoutAttributes;
    }

    /**
     * 从系统属性列表中返回一个 <code>Font</code> 对象。
     * <code>nm</code> 被视为要获取的系统属性的名称。该属性的 <code>String</code> 值随后被解释为一个 <code>Font</code> 对象，
     * 具体解释方式参见 <code>Font.decode(String)</code>。
     * 如果未找到指定的属性，或执行代码没有权限读取该属性，则返回 null。
     *
     * @param nm 属性名称
     * @return 由属性名称描述的 <code>Font</code> 对象，如果不存在这样的属性，则返回 null。
     * @throws NullPointerException 如果 nm 为 null。
     * @since 1.2
     * @see #decode(String)
     */
    public static Font getFont(String nm) {
        return getFont(nm, null);
    }

    /**
     * 返回 <code>str</code> 参数描述的 <code>Font</code>。
     * 为了确保此方法返回所需的字体，请将 <code>str</code> 参数格式化为以下形式之一：
     *
     * <ul>
     * <li><em>fontname-style-pointsize</em>
     * <li><em>fontname-pointsize</em>
     * <li><em>fontname-style</em>
     * <li><em>fontname</em>
     * <li><em>fontname style pointsize</em>
     * <li><em>fontname pointsize</em>
     * <li><em>fontname style</em>
     * <li><em>fontname</em>
     * </ul>
     * 其中 <i>style</i> 是四个不区分大小写的字符串之一：
     * <code>"PLAIN"</code>、<code>"BOLD"</code>、<code>"BOLDITALIC"</code> 或 <code>"ITALIC"</code>，点大小是正十进制整数表示的点大小。
     * 例如，如果您想要一个 Arial、粗体、点大小为 18 的字体，您将调用此方法，参数为：
     * "Arial-BOLD-18"。
     * 这相当于调用 Font 构造函数：
     * <code>new Font("Arial", Font.BOLD, 18);</code>
     * 并且这些值的解释方式与该构造函数指定的方式相同。
     * <p>
     * 有效的尾随十进制字段始终被解释为点大小。因此，包含尾随十进制值的字体名称不应在仅字体名称的形式中使用。
     * <p>
     * 如果样式名称字段不是有效的样式字符串，则将其解释为字体名称的一部分，并使用默认样式。
     * <p>
     * 在输入中只能使用 ' ' 或 '-' 中的一个来分隔字段。确定的分隔符是字符串末尾最接近的分隔符，它将有效的点大小或有效的样式名称与字符串的其余部分分开。
     * 空（空）点大小和样式字段被视为具有该字段默认值的有效字段。
     * <p>
     * 一些字体名称可能包含分隔符字符 ' ' 或 '-'。如果 <code>str</code> 不是由 3 个组件组成的，例如 <code>style</code> 或 <code>pointsize</code> 字段不在 <code>str</code> 中，
     * 并且 <code>fontname</code> 也包含一个确定为分隔符字符的字符，则这些字符在 <code>fontname</code> 中作为分隔符出现的地方可能会被解释为分隔符，
     * 因此字体名称可能无法正确识别。
     *
     * <p>
     * 默认大小为 12，默认样式为 PLAIN。如果 <code>str</code> 没有指定有效的大小，则返回的 <code>Font</code> 的大小为 12。
     * 如果 <code>str</code> 没有指定有效的样式，则返回的 Font 的样式为 PLAIN。如果 <code>str</code> 参数中没有指定有效的字体名称，
     * 此方法将返回一个家族名称为 "Dialog" 的字体。要确定系统上可用的字体家族名称，可以使用
     * {@link GraphicsEnvironment#getAvailableFontFamilyNames()} 方法。如果 <code>str</code> 为 <code>null</code>，
     * 则返回一个新的 <code>Font</code>，家族名称为 "Dialog"，大小为 12，样式为 PLAIN。
     * @param str 字体名称，或 <code>null</code>
     * @return 由 <code>str</code> 描述的 <code>Font</code> 对象，如果 <code>str</code> 为 <code>null</code>，则返回一个新的默认 <code>Font</code>。
     * @see #getFamily
     * @since JDK1.1
     */
    public static Font decode(String str) {
        String fontName = str;
        String styleName = "";
        int fontSize = 12;
        int fontStyle = Font.PLAIN;

        if (str == null) {
            return new Font(DIALOG, fontStyle, fontSize);
        }

        int lastHyphen = str.lastIndexOf('-');
        int lastSpace = str.lastIndexOf(' ');
        char sepChar = (lastHyphen > lastSpace) ? '-' : ' ';
        int sizeIndex = str.lastIndexOf(sepChar);
        int styleIndex = str.lastIndexOf(sepChar, sizeIndex-1);
        int strlen = str.length();

        if (sizeIndex > 0 && sizeIndex+1 < strlen) {
            try {
                fontSize =
                    Integer.valueOf(str.substring(sizeIndex+1)).intValue();
                if (fontSize <= 0) {
                    fontSize = 12;
                }
            } catch (NumberFormatException e) {
                /* 如果我们没有找到样式字符串的开始，也许这是样式 */
                styleIndex = sizeIndex;
                sizeIndex = strlen;
                if (str.charAt(sizeIndex-1) == sepChar) {
                    sizeIndex--;
                }
            }
        }


                    if (styleIndex >= 0 && styleIndex+1 < strlen) {
            styleName = str.substring(styleIndex+1, sizeIndex);
            styleName = styleName.toLowerCase(Locale.ENGLISH);
            if (styleName.equals("bolditalic")) {
                fontStyle = Font.BOLD | Font.ITALIC;
            } else if (styleName.equals("italic")) {
                fontStyle = Font.ITALIC;
            } else if (styleName.equals("bold")) {
                fontStyle = Font.BOLD;
            } else if (styleName.equals("plain")) {
                fontStyle = Font.PLAIN;
            } else {
                /* 这个字符串不是预期的任何样式，因此
                 * 假设它是字体名称的一部分
                 */
                styleIndex = sizeIndex;
                if (str.charAt(styleIndex-1) == sepChar) {
                    styleIndex--;
                }
            }
            fontName = str.substring(0, styleIndex);

        } else {
            int fontEnd = strlen;
            if (styleIndex > 0) {
                fontEnd = styleIndex;
            } else if (sizeIndex > 0) {
                fontEnd = sizeIndex;
            }
            if (fontEnd > 0 && str.charAt(fontEnd-1) == sepChar) {
                fontEnd--;
            }
            fontName = str.substring(0, fontEnd);
        }

        return new Font(fontName, fontStyle, fontSize);
    }

    /**
     * 从系统属性列表中获取指定的 <code>Font</code>。
     * 与 <code>System</code> 的 <code>getProperty</code> 方法一样，
     * 第一个参数被视为要获取的系统属性的名称。
     * 该属性的 <code>String</code> 值随后被解释为一个 <code>Font</code> 对象。
     * <p>
     * 属性值应为 <code>Font.decode(String)</code> 接受的形式之一。
     * 如果未找到指定的属性，或者执行代码没有权限读取该属性，则返回 <code>font</code> 参数。
     * @param nm 属性名称，不区分大小写
     * @param font 如果属性 <code>nm</code> 未定义，则返回的默认 <code>Font</code>
     * @return 属性的 <code>Font</code> 值。
     * @throws NullPointerException 如果 nm 为 null。
     * @see #decode(String)
     */
    public static Font getFont(String nm, Font font) {
        String str = null;
        try {
            str =System.getProperty(nm);
        } catch(SecurityException e) {
        }
        if (str == null) {
            return font;
        }
        return decode ( str );
    }

    transient int hash;
    /**
     * 返回此 <code>Font</code> 的哈希码。
     * @return 此 <code>Font</code> 的哈希码值。
     * @since JDK1.0
     */
    public int hashCode() {
        if (hash == 0) {
            hash = name.hashCode() ^ style ^ size;
            /* 许多字体可能仅在变换上有所不同。
             * 因此在哈希计算中包括变换。
             * nonIdentityTx 在 'values' 中有变换时设置。测试 null 是必需的，因为它也可能因其他原因而设置。
             */
            if (nonIdentityTx &&
                values != null && values.getTransform() != null) {
                hash ^= values.getTransform().hashCode();
            }
        }
        return hash;
    }

    /**
     * 将此 <code>Font</code> 对象与指定的 <code>Object</code> 进行比较。
     * @param obj 要比较的 <code>Object</code>
     * @return 如果对象相同，或者参数是一个 <code>Font</code> 对象，描述的字体与此对象相同，则返回 <code>true</code>；
     *          否则返回 <code>false</code>。
     * @since JDK1.0
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj != null) {
            try {
                Font font = (Font)obj;
                if (size == font.size &&
                    style == font.style &&
                    nonIdentityTx == font.nonIdentityTx &&
                    hasLayoutAttributes == font.hasLayoutAttributes &&
                    pointSize == font.pointSize &&
                    name.equals(font.name)) {

                    /* 'values' 通常懒初始化，除非字体是从 Map 构造的，或者使用 Map 或其他值派生的。
                     * 因此，如果只有一个字体初始化了该字段，我们需要在另一个实例中初始化并比较。
                     */
                    if (values == null) {
                        if (font.values == null) {
                            return true;
                        } else {
                            return getAttributeValues().equals(font.values);
                        }
                    } else {
                        return values.equals(font.getAttributeValues());
                    }
                }
            }
            catch (ClassCastException e) {
            }
        }
        return false;
    }

    /**
     * 将此 <code>Font</code> 对象转换为 <code>String</code> 表示形式。
     * @return 此 <code>Font</code> 对象的 <code>String</code> 表示形式。
     * @since JDK1.0
     */
    // 注意：此方法可能由特权线程调用。
    //       不要在该线程上调用客户端代码！
    public String toString() {
        String  strStyle;

        if (isBold()) {
            strStyle = isItalic() ? "bolditalic" : "bold";
        } else {
            strStyle = isItalic() ? "italic" : "plain";
        }

        return getClass().getName() + "[family=" + getFamily() + ",name=" + name + ",style=" +
            strStyle + ",size=" + size + "]";
    } // toString()


    /** 序列化支持。需要一个 <code>readObject</code>
     *  方法，因为构造函数创建了字体的对等体，而我们不能序列化对等体。
     *  同样，计算出的字体“家族”在 <code>readObject</code> 时可能与
     *  <code>writeObject</code> 时不同。写入一个整数版本，以便未来版本的此类能够识别此版本的序列化输出。
     */
    /**
     * <code>Font</code> 可序列化数据形式。
     *
     * @serial
     */
    private int fontSerializedDataVersion = 1;

    /**
     * 将默认的可序列化字段写入流。
     *
     * @param s 要写入的 <code>ObjectOutputStream</code>
     * @see AWTEventMulticaster#save(ObjectOutputStream, String, EventListener)
     * @see #readObject(java.io.ObjectInputStream)
     */
    private void writeObject(java.io.ObjectOutputStream s)
      throws java.lang.ClassNotFoundException,
             java.io.IOException
    {
        if (values != null) {
          synchronized(values) {
            // transient
            fRequestedAttributes = values.toSerializableHashtable();
            s.defaultWriteObject();
            fRequestedAttributes = null;
          }
        } else {
          s.defaultWriteObject();
        }
    }

    /**
     * 读取 <code>ObjectInputStream</code>。
     * 未识别的键或值将被忽略。
     *
     * @param s 要读取的 <code>ObjectInputStream</code>
     * @serial
     * @see #writeObject(java.io.ObjectOutputStream)
     */
    private void readObject(java.io.ObjectInputStream s)
      throws java.lang.ClassNotFoundException,
             java.io.IOException
    {
        s.defaultReadObject();
        if (pointSize == 0) {
            pointSize = (float)size;
        }

        // 处理 fRequestedAttributes。
        // 在 1.5 中，我们总是流出了字体值加上
        // TRANSFORM, SUPERSCRIPT, 和 WIDTH，无论这些值是否为默认值。在 1.6 中我们只流出定义的值。
        // 因此，1.6 从 1.5 流中读取时，会检查每个值，如果值为默认值，则“取消定义”。

        if (fRequestedAttributes != null) {
            try {
            values = getAttributeValues(); // 初始化
            AttributeValues extras =
                AttributeValues.fromSerializableHashtable(fRequestedAttributes);
            if (!AttributeValues.is16Hashtable(fRequestedAttributes)) {
                extras.unsetDefault(); // 如果是旧版本流，取消定义这些值
            }
            values = getAttributeValues().merge(extras);
            this.nonIdentityTx = values.anyNonDefault(EXTRA_MASK);
            this.hasLayoutAttributes =  values.anyNonDefault(LAYOUT_MASK);
            } catch (Throwable t) {
                throw new IOException(t);
            } finally {
            fRequestedAttributes = null; // 不再需要
        }
    }
    }

    /**
     * 返回此 <code>Font</code> 中的字形数量。此 <code>Font</code> 的字形代码范围从 0 到
     * <code>getNumGlyphs()</code> - 1。
     * @return 此 <code>Font</code> 中的字形数量。
     * @since 1.2
     */
    public int getNumGlyphs() {
        return  getFont2D().getNumGlyphs();
    }

    /**
     * 返回当此 <code>Font</code> 没有指定的 Unicode 代码点的字形时使用的字形代码。
     * @return 此 <code>Font</code> 的字形代码。
     * @since 1.2
     */
    public int getMissingGlyphCode() {
        return getFont2D().getMissingGlyphCode();
    }

    /**
     * 返回适合显示此字符的基线。
     * <p>
     * 大字体可以支持不同的书写系统，每个系统可以使用不同的基线。
     * 字符参数确定要使用的书写系统。客户端不应假设所有字符使用相同的基线。
     *
     * @param c 用于识别书写系统的字符
     * @return 适合指定字符的基线。
     * @see LineMetrics#getBaselineOffsets
     * @see #ROMAN_BASELINE
     * @see #CENTER_BASELINE
     * @see #HANGING_BASELINE
     * @since 1.2
     */
    public byte getBaselineFor(char c) {
        return getFont2D().getBaselineFor(c);
    }

    /**
     * 返回此 <code>Font</code> 中可用的字体属性映射。属性包括连字和字形替换等。
     * @return 此 <code>Font</code> 的属性映射。
     */
    public Map<TextAttribute,?> getAttributes(){
        return new AttributeMap(getAttributeValues());
    }

    /**
     * 返回此 <code>Font</code> 支持的所有属性的键。这些属性可用于派生其他字体。
     * @return 包含此 <code>Font</code> 支持的所有属性的键的数组。
     * @since 1.2
     */
    public Attribute[] getAvailableAttributes() {
        // FONT 不被 Font 支持

        Attribute attributes[] = {
            TextAttribute.FAMILY,
            TextAttribute.WEIGHT,
            TextAttribute.WIDTH,
            TextAttribute.POSTURE,
            TextAttribute.SIZE,
            TextAttribute.TRANSFORM,
            TextAttribute.SUPERSCRIPT,
            TextAttribute.CHAR_REPLACEMENT,
            TextAttribute.FOREGROUND,
            TextAttribute.BACKGROUND,
            TextAttribute.UNDERLINE,
            TextAttribute.STRIKETHROUGH,
            TextAttribute.RUN_DIRECTION,
            TextAttribute.BIDI_EMBEDDING,
            TextAttribute.JUSTIFICATION,
            TextAttribute.INPUT_METHOD_HIGHLIGHT,
            TextAttribute.INPUT_METHOD_UNDERLINE,
            TextAttribute.SWAP_COLORS,
            TextAttribute.NUMERIC_SHAPING,
            TextAttribute.KERNING,
            TextAttribute.LIGATURES,
            TextAttribute.TRACKING,
        };

        return attributes;
    }

    /**
     * 通过复制此 <code>Font</code> 对象并应用新的样式和大小，创建一个新的 <code>Font</code> 对象。
     * @param style 新 <code>Font</code> 的样式
     * @param size 新 <code>Font</code> 的大小
     * @return 新的 <code>Font</code> 对象。
     * @since 1.2
     */
    public Font deriveFont(int style, float size){
        if (values == null) {
            return new Font(name, style, size, createdFont, font2DHandle);
        }
        AttributeValues newValues = getAttributeValues().clone();
        int oldStyle = (this.style != style) ? this.style : -1;
        applyStyle(style, newValues);
        newValues.setSize(size);
        return new Font(newValues, null, oldStyle, createdFont, font2DHandle);
    }

    /**
     * 通过复制此 <code>Font</code> 对象并应用新的样式和变换，创建一个新的 <code>Font</code> 对象。
     * @param style 新 <code>Font</code> 的样式
     * @param trans 与新 <code>Font</code> 关联的 <code>AffineTransform</code>
     * @return 新的 <code>Font</code> 对象。
     * @throws IllegalArgumentException 如果 <code>trans</code> 为 <code>null</code>
     * @since 1.2
     */
    public Font deriveFont(int style, AffineTransform trans){
        AttributeValues newValues = getAttributeValues().clone();
        int oldStyle = (this.style != style) ? this.style : -1;
        applyStyle(style, newValues);
        applyTransform(trans, newValues);
        return new Font(newValues, null, oldStyle, createdFont, font2DHandle);
    }

    /**
     * 通过复制当前的 <code>Font</code> 对象并应用新的大小，创建一个新的 <code>Font</code> 对象。
     * @param size 新 <code>Font</code> 的大小。
     * @return 新的 <code>Font</code> 对象。
     * @since 1.2
     */
    public Font deriveFont(float size){
        if (values == null) {
            return new Font(name, style, size, createdFont, font2DHandle);
        }
        AttributeValues newValues = getAttributeValues().clone();
        newValues.setSize(size);
        return new Font(newValues, null, -1, createdFont, font2DHandle);
    }

    /**
     * 通过复制当前的 <code>Font</code> 对象并应用新的变换，创建一个新的 <code>Font</code> 对象。
     * @param trans 与新 <code>Font</code> 关联的 <code>AffineTransform</code>
     * @return 新的 <code>Font</code> 对象。
     * @throws IllegalArgumentException 如果 <code>trans</code> 为 <code>null</code>
     * @since 1.2
     */
    public Font deriveFont(AffineTransform trans){
        AttributeValues newValues = getAttributeValues().clone();
        applyTransform(trans, newValues);
        return new Font(newValues, null, -1, createdFont, font2DHandle);
    }

    /**
     * 通过复制当前的 <code>Font</code> 对象并应用新的样式，创建一个新的 <code>Font</code> 对象。
     * @param style 新 <code>Font</code> 的样式
     * @return 新的 <code>Font</code> 对象。
     * @since 1.2
     */
    public Font deriveFont(int style){
        if (values == null) {
           return new Font(name, style, size, createdFont, font2DHandle);
        }
        AttributeValues newValues = getAttributeValues().clone();
        int oldStyle = (this.style != style) ? this.style : -1;
        applyStyle(style, newValues);
        return new Font(newValues, null, oldStyle, createdFont, font2DHandle);
    }


                /**
     * 创建一个新的 <code>Font</code> 对象，通过复制当前的 <code>Font</code> 对象并应用一组新的字体属性。
     *
     * @param attributes 为新 <code>Font</code> 启用的属性映射
     * @return 一个新的 <code>Font</code> 对象。
     * @since 1.2
     */
    public Font deriveFont(Map<? extends Attribute, ?> attributes) {
        if (attributes == null) {
            return this;
        }
        AttributeValues newValues = getAttributeValues().clone();
        newValues.merge(attributes, RECOGNIZED_MASK);

        return new Font(newValues, name, style, createdFont, font2DHandle);
    }

    /**
     * 检查此 <code>Font</code> 是否有指定字符的字形。
     *
     * <p> <b>注意：</b> 该方法无法处理 <a
     * href="../../java/lang/Character.html#supplementary"> 补充字符</a>。为了支持所有 Unicode 字符，包括
     * 补充字符，使用 {@link #canDisplay(int)}
     * 方法或 <code>canDisplayUpTo</code> 方法。
     *
     * @param c 需要字形的字符
     * @return 如果此 <code>Font</code> 有此字符的字形，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since 1.2
     */
    public boolean canDisplay(char c){
        return getFont2D().canDisplay(c);
    }

    /**
     * 检查此 <code>Font</code> 是否有指定字符的字形。
     *
     * @param codePoint 需要字形的字符（Unicode 代码点）。
     * @return 如果此 <code>Font</code> 有该字符的字形，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @throws IllegalArgumentException 如果代码点不是有效的 Unicode 代码点。
     * @see Character#isValidCodePoint(int)
     * @since 1.5
     */
    public boolean canDisplay(int codePoint) {
        if (!Character.isValidCodePoint(codePoint)) {
            throw new IllegalArgumentException("无效的代码点: " +
                                               Integer.toHexString(codePoint));
        }
        return getFont2D().canDisplay(codePoint);
    }

    /**
     * 指示此 <code>Font</code> 是否可以显示指定的 <code>String</code>。对于具有 Unicode 编码的字符串，
     * 了解特定字体是否可以显示该字符串非常重要。此方法返回一个指向 <code>String</code>
     * <code>str</code> 的偏移量，该偏移量指向此 <code>Font</code> 无法显示的第一个字符。
     * 如果 <code>Font</code> 可以显示所有字符，则返回 -1。
     * @param str 一个 <code>String</code> 对象
     * @return 一个指向 <code>str</code> 的偏移量，该偏移量指向 <code>str</code> 中此
     *          <code>Font</code> 无法显示的第一个字符；或 <code>-1</code>，如果
     *          此 <code>Font</code> 可以显示 <code>str</code> 中的所有字符。
     * @since 1.2
     */
    public int canDisplayUpTo(String str) {
        Font2D font2d = getFont2D();
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (font2d.canDisplay(c)) {
                continue;
            }
            if (!Character.isHighSurrogate(c)) {
                return i;
            }
            if (!font2d.canDisplay(str.codePointAt(i))) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * 指示此 <code>Font</code> 是否可以显示指定的 <code>text</code>
     * 从 <code>start</code> 开始到 <code>limit</code> 结束的字符。此方法是一个方便的重载方法。
     * @param text 指定的 <code>char</code> 值数组
     * @param start 指定的 <code>char</code> 值数组中的指定起始偏移量（以 <code>char</code> 为单位）
     * @param limit 指定的 <code>char</code> 值数组中的指定结束偏移量（以 <code>char</code> 为单位）
     * @return 一个指向 <code>text</code> 的偏移量，该偏移量指向 <code>text</code> 中此
     *          <code>Font</code> 无法显示的第一个字符；或 <code>-1</code>，如果
     *          此 <code>Font</code> 可以显示 <code>text</code> 中的所有字符。
     * @since 1.2
     */
    public int canDisplayUpTo(char[] text, int start, int limit) {
        Font2D font2d = getFont2D();
        for (int i = start; i < limit; i++) {
            char c = text[i];
            if (font2d.canDisplay(c)) {
                continue;
            }
            if (!Character.isHighSurrogate(c)) {
                return i;
            }
            if (!font2d.canDisplay(Character.codePointAt(text, i, limit))) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * 指示此 <code>Font</code> 是否可以显示由 <code>iter</code> 指定的文本
     * 从 <code>start</code> 开始到 <code>limit</code> 结束的字符。
     *
     * @param iter 一个 {@link CharacterIterator} 对象
     * @param start 指定的 <code>CharacterIterator</code> 中的指定起始偏移量
     * @param limit 指定的 <code>CharacterIterator</code> 中的指定结束偏移量
     * @return 一个指向 <code>iter</code> 的偏移量，该偏移量指向 <code>iter</code> 中此
     *          <code>Font</code> 无法显示的第一个字符；或 <code>-1</code>，如果
     *          此 <code>Font</code> 可以显示 <code>iter</code> 中的所有字符。
     * @since 1.2
     */
    public int canDisplayUpTo(CharacterIterator iter, int start, int limit) {
        Font2D font2d = getFont2D();
        char c = iter.setIndex(start);
        for (int i = start; i < limit; i++, c = iter.next()) {
            if (font2d.canDisplay(c)) {
                continue;
            }
            if (!Character.isHighSurrogate(c)) {
                return i;
            }
            char c2 = iter.next();
            // c2 可能是 CharacterIterator.DONE，它不是一个低代理。
            if (!Character.isLowSurrogate(c2)) {
                return i;
            }
            if (!font2d.canDisplay(Character.toCodePoint(c, c2))) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * 返回此 <code>Font</code> 的斜体角度。斜体角度是与此
     * <code>Font</code> 的姿势最匹配的光标的逆斜率。
     * @see TextAttribute#POSTURE
     * @return 此 <code>Font</code> 的斜体样式的角度。
     */
    public float getItalicAngle() {
        return getItalicAngle(null);
    }

    /* FRC 提示不会影响斜体角度的值，但我们需要传递它们以查找一个打击。
     * 如果我们可以传递已经在使用的提示，则可以防止额外的打击被分配。注意，斜体角度是
     * 字体的属性，因此需要字体变换而不是设备变换。最后，这是私有的，但在 JDK 中
     * 唯一的调用者——也是最可能的调用者——是在这个相同的类中。
     */
    private float getItalicAngle(FontRenderContext frc) {
        Object aa, fm;
        if (frc == null) {
            aa = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
            fm = RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
        } else {
            aa = frc.getAntiAliasingHint();
            fm = frc.getFractionalMetricsHint();
        }
        return getFont2D().getItalicAngle(this, identityTx, aa, fm);
    }

    /**
     * 检查此 <code>Font</code> 是否具有统一的行度量。逻辑 <code>Font</code> 可能是一个
     * 复合字体，这意味着它由不同的物理字体组成，以覆盖不同的代码范围。这些
     * 字体可能具有不同的 <code>LineMetrics</code>。如果逻辑 <code>Font</code> 是一个
     * 单个字体，则度量将是统一的。
     * @return 如果此 <code>Font</code> 具有统一的行度量，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean hasUniformLineMetrics() {
        return false;   // REMIND 总是安全的，但阻止调用者优化
    }

    private transient SoftReference<FontLineMetrics> flmref;
    private FontLineMetrics defaultLineMetrics(FontRenderContext frc) {
        FontLineMetrics flm = null;
        if (flmref == null
            || (flm = flmref.get()) == null
            || !flm.frc.equals(frc)) {

            /* frc 中的设备变换在获取行度量时不使用，尽管它可能应该使用：REMIND 找出为什么没有使用？
             * 使用字体变换，但其应用在 getFontMetrics 中，所以这里只传递单位变换
             */
            float [] metrics = new float[8];
            getFont2D().getFontMetrics(this, identityTx,
                                       frc.getAntiAliasingHint(),
                                       frc.getFractionalMetricsHint(),
                                       metrics);
            float ascent  = metrics[0];
            float descent = metrics[1];
            float leading = metrics[2];
            float ssOffset = 0;
            if (values != null && values.getSuperscript() != 0) {
                ssOffset = (float)getTransform().getTranslateY();
                ascent -= ssOffset;
                descent += ssOffset;
            }
            float height = ascent + descent + leading;

            int baselineIndex = 0; // 需要实际的索引，假设所有都是罗马的
            // 需要实际的基线
            float[] baselineOffsets = { 0, (descent/2f - ascent) / 2f, -ascent };

            float strikethroughOffset = metrics[4];
            float strikethroughThickness = metrics[5];

            float underlineOffset = metrics[6];
            float underlineThickness = metrics[7];

            float italicAngle = getItalicAngle(frc);

            if (isTransformed()) {
                AffineTransform ctx = values.getCharTransform(); // 提取旋转
                if (ctx != null) {
                    Point2D.Float pt = new Point2D.Float();
                    pt.setLocation(0, strikethroughOffset);
                    ctx.deltaTransform(pt, pt);
                    strikethroughOffset = pt.y;
                    pt.setLocation(0, strikethroughThickness);
                    ctx.deltaTransform(pt, pt);
                    strikethroughThickness = pt.y;
                    pt.setLocation(0, underlineOffset);
                    ctx.deltaTransform(pt, pt);
                    underlineOffset = pt.y;
                    pt.setLocation(0, underlineThickness);
                    ctx.deltaTransform(pt, pt);
                    underlineThickness = pt.y;
                }
            }
            strikethroughOffset += ssOffset;
            underlineOffset += ssOffset;

            CoreMetrics cm = new CoreMetrics(ascent, descent, leading, height,
                                             baselineIndex, baselineOffsets,
                                             strikethroughOffset, strikethroughThickness,
                                             underlineOffset, underlineThickness,
                                             ssOffset, italicAngle);

            flm = new FontLineMetrics(0, cm, frc);
            flmref = new SoftReference<FontLineMetrics>(flm);
        }

        return (FontLineMetrics)flm.clone();
    }

    /**
     * 使用指定的 <code>String</code> 和 {@link FontRenderContext} 创建一个 {@link LineMetrics} 对象。
     * @param str 指定的 <code>String</code>
     * @param frc 指定的 <code>FontRenderContext</code>
     * @return 使用指定的 <code>String</code> 和 {@link FontRenderContext} 创建的 <code>LineMetrics</code> 对象。
     */
    public LineMetrics getLineMetrics( String str, FontRenderContext frc) {
        FontLineMetrics flm = defaultLineMetrics(frc);
        flm.numchars = str.length();
        return flm;
    }

    /**
     * 使用指定的参数创建一个 <code>LineMetrics</code> 对象。
     * @param str 指定的 <code>String</code>
     * @param beginIndex <code>str</code> 的初始偏移量
     * @param limit <code>str</code> 的结束偏移量
     * @param frc 指定的 <code>FontRenderContext</code>
     * @return 使用指定的参数创建的 <code>LineMetrics</code> 对象。
     */
    public LineMetrics getLineMetrics( String str,
                                    int beginIndex, int limit,
                                    FontRenderContext frc) {
        FontLineMetrics flm = defaultLineMetrics(frc);
        int numChars = limit - beginIndex;
        flm.numchars = (numChars < 0)? 0: numChars;
        return flm;
    }

    /**
     * 使用指定的参数创建一个 <code>LineMetrics</code> 对象。
     * @param chars 字符数组
     * @param beginIndex <code>chars</code> 的初始偏移量
     * @param limit <code>chars</code> 的结束偏移量
     * @param frc 指定的 <code>FontRenderContext</code>
     * @return 使用指定的参数创建的 <code>LineMetrics</code> 对象。
     */
    public LineMetrics getLineMetrics(char [] chars,
                                    int beginIndex, int limit,
                                    FontRenderContext frc) {
        FontLineMetrics flm = defaultLineMetrics(frc);
        int numChars = limit - beginIndex;
        flm.numchars = (numChars < 0)? 0: numChars;
        return flm;
    }

    /**
     * 使用指定的参数创建一个 <code>LineMetrics</code> 对象。
     * @param ci 指定的 <code>CharacterIterator</code>
     * @param beginIndex <code>ci</code> 中的初始偏移量
     * @param limit <code>ci</code> 的结束偏移量
     * @param frc 指定的 <code>FontRenderContext</code>
     * @return 使用指定的参数创建的 <code>LineMetrics</code> 对象。
     */
    public LineMetrics getLineMetrics(CharacterIterator ci,
                                    int beginIndex, int limit,
                                    FontRenderContext frc) {
        FontLineMetrics flm = defaultLineMetrics(frc);
        int numChars = limit - beginIndex;
        flm.numchars = (numChars < 0)? 0: numChars;
        return flm;
    }

    /**
     * 返回指定 <code>String</code> 在指定 <code>FontRenderContext</code> 中的逻辑边界。逻辑边界
     * 包含原点、上升、前进和高度，其中包括前导。逻辑边界并不总是包含所有文本。例如，在某些语言和某些字体中，重音
     * 标记可能位于上升之上或下降之下。要获取包含所有文本的视觉边界框，使用
     * <code>TextLayout</code> 的 {@link TextLayout#getBounds() getBounds} 方法。
     * <p>注意：返回的边界是以基线为基准的坐标（参见 {@link java.awt.Font 类说明}）。
     * @param str 指定的 <code>String</code>
     * @param frc 指定的 <code>FontRenderContext</code>
     * @return 一个 {@link Rectangle2D}，它是指定 <code>String</code> 在指定
     * <code>FontRenderContext</code> 中的边界框。
     * @see FontRenderContext
     * @see Font#createGlyphVector
     * @since 1.2
     */
    public Rectangle2D getStringBounds( String str, FontRenderContext frc) {
        char[] array = str.toCharArray();
        return getStringBounds(array, 0, array.length, frc);
    }


               /**
     * 返回指定 <code>String</code> 在指定 <code>FontRenderContext</code> 中的逻辑边界。逻辑边界包含原点、上升高度、前进距离和高度，其中包括前导。逻辑边界并不总是包含所有文本。例如，在某些语言和字体中，重音符号可以位于上升高度之上或下降高度之下。要获取包含所有文本的视觉边界框，请使用 <code>TextLayout</code> 的 {@link TextLayout#getBounds() getBounds} 方法。
     * <p>注意：返回的边界是基线相对坐标（参见 {@link java.awt.Font 类说明}）。
     * @param str 指定的 <code>String</code>
     * @param beginIndex <code>str</code> 的初始偏移量
     * @param limit <code>str</code> 的结束偏移量
     * @param frc 指定的 <code>FontRenderContext</code>
     * @return 一个 <code>Rectangle2D</code>，它是指定 <code>String</code> 在指定 <code>FontRenderContext</code> 中的边界框。
     * @throws IndexOutOfBoundsException 如果 <code>beginIndex</code> 小于零，或 <code>limit</code> 大于 <code>str</code> 的长度，或 <code>beginIndex</code> 大于 <code>limit</code>。
     * @see FontRenderContext
     * @see Font#createGlyphVector
     * @since 1.2
     */
    public Rectangle2D getStringBounds( String str,
                                    int beginIndex, int limit,
                                        FontRenderContext frc) {
        String substr = str.substring(beginIndex, limit);
        return getStringBounds(substr, frc);
    }

   /**
     * 返回指定字符数组在指定 <code>FontRenderContext</code> 中的逻辑边界。逻辑边界包含原点、上升高度、前进距离和高度，其中包括前导。逻辑边界并不总是包含所有文本。例如，在某些语言和字体中，重音符号可以位于上升高度之上或下降高度之下。要获取包含所有文本的视觉边界框，请使用 <code>TextLayout</code> 的 {@link TextLayout#getBounds() getBounds} 方法。
     * <p>注意：返回的边界是基线相对坐标（参见 {@link java.awt.Font 类说明}）。
     * @param chars 字符数组
     * @param beginIndex 字符数组中的初始偏移量
     * @param limit 字符数组中的结束偏移量
     * @param frc 指定的 <code>FontRenderContext</code>
     * @return 一个 <code>Rectangle2D</code>，它是指定字符数组在指定 <code>FontRenderContext</code> 中的边界框。
     * @throws IndexOutOfBoundsException 如果 <code>beginIndex</code> 小于零，或 <code>limit</code> 大于 <code>chars</code> 的长度，或 <code>beginIndex</code> 大于 <code>limit</code>。
     * @see FontRenderContext
     * @see Font#createGlyphVector
     * @since 1.2
     */
    public Rectangle2D getStringBounds(char [] chars,
                                    int beginIndex, int limit,
                                       FontRenderContext frc) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("beginIndex: " + beginIndex);
        }
        if (limit > chars.length) {
            throw new IndexOutOfBoundsException("limit: " + limit);
        }
        if (beginIndex > limit) {
            throw new IndexOutOfBoundsException("range length: " +
                                                (limit - beginIndex));
        }

        // 这段代码应该在 textlayout 中
        // 对简单文本进行快速检查，假设 GV 可以使用

        boolean simple = values == null ||
            (values.getKerning() == 0 && values.getLigatures() == 0 &&
              values.getBaselineTransform() == null);
        if (simple) {
            simple = ! FontUtilities.isComplexText(chars, beginIndex, limit);
        }

        if (simple) {
            GlyphVector gv = new StandardGlyphVector(this, chars, beginIndex,
                                                     limit - beginIndex, frc);
            return gv.getLogicalBounds();
        } else {
            // 需要字符数组构造函数的 textlayout
            String str = new String(chars, beginIndex, limit - beginIndex);
            TextLayout tl = new TextLayout(str, this, frc);
            return new Rectangle2D.Float(0, -tl.getAscent(), tl.getAdvance(),
                                         tl.getAscent() + tl.getDescent() +
                                         tl.getLeading());
        }
    }

   /**
     * 返回指定 {@link CharacterIterator} 中索引的字符在指定 <code>FontRenderContext</code> 中的逻辑边界。逻辑边界包含原点、上升高度、前进距离和高度，其中包括前导。逻辑边界并不总是包含所有文本。例如，在某些语言和字体中，重音符号可以位于上升高度之上或下降高度之下。要获取包含所有文本的视觉边界框，请使用 <code>TextLayout</code> 的 {@link TextLayout#getBounds() getBounds} 方法。
     * <p>注意：返回的边界是基线相对坐标（参见 {@link java.awt.Font 类说明}）。
     * @param ci 指定的 <code>CharacterIterator</code>
     * @param beginIndex <code>ci</code> 中的初始偏移量
     * @param limit <code>ci</code> 中的结束偏移量
     * @param frc 指定的 <code>FontRenderContext</code>
     * @return 一个 <code>Rectangle2D</code>，它是指定 <code>CharacterIterator</code> 中索引的字符在指定 <code>FontRenderContext</code> 中的边界框。
     * @see FontRenderContext
     * @see Font#createGlyphVector
     * @since 1.2
     * @throws IndexOutOfBoundsException 如果 <code>beginIndex</code> 小于 <code>ci</code> 的起始索引，或 <code>limit</code> 大于 <code>ci</code> 的结束索引，或 <code>beginIndex</code> 大于 <code>limit</code>
     */
    public Rectangle2D getStringBounds(CharacterIterator ci,
                                    int beginIndex, int limit,
                                       FontRenderContext frc) {
        int start = ci.getBeginIndex();
        int end = ci.getEndIndex();

        if (beginIndex < start) {
            throw new IndexOutOfBoundsException("beginIndex: " + beginIndex);
        }
        if (limit > end) {
            throw new IndexOutOfBoundsException("limit: " + limit);
        }
        if (beginIndex > limit) {
            throw new IndexOutOfBoundsException("range length: " +
                                                (limit - beginIndex));
        }

        char[]  arr = new char[limit - beginIndex];

        ci.setIndex(beginIndex);
        for(int idx = 0; idx < arr.length; idx++) {
            arr[idx] = ci.current();
            ci.next();
        }

        return getStringBounds(arr,0,arr.length,frc);
    }

    /**
     * 返回在指定 <code>FontRenderContext</code> 中定义的最大边界字符的边界。
     * <p>注意：返回的边界是基线相对坐标（参见 {@link java.awt.Font 类说明}）。
     * @param frc 指定的 <code>FontRenderContext</code>
     * @return 一个 <code>Rectangle2D</code>，它是最大边界字符的边界框。
     */
    public Rectangle2D getMaxCharBounds(FontRenderContext frc) {
        float [] metrics = new float[4];

        getFont2D().getFontMetrics(this, frc, metrics);

        return new Rectangle2D.Float(0, -metrics[0],
                                metrics[3],
                                metrics[0] + metrics[1] + metrics[2]);
    }

    /**
     * 通过基于此 <code>Font</code> 中的 Unicode cmap 将字符映射到字形来创建一个 {@link java.awt.font.GlyphVector GlyphVector}。此方法除了将字形映射到字符外不进行其他处理。这意味着此方法对于需要重新排序、成形或连字替换的脚本（如阿拉伯文、希伯来文、泰文和印度文）没有用。
     * @param frc 指定的 <code>FontRenderContext</code>
     * @param str 指定的 <code>String</code>
     * @return 一个使用指定 <code>String</code> 和指定 <code>FontRenderContext</code> 创建的新 <code>GlyphVector</code>。
     */
    public GlyphVector createGlyphVector(FontRenderContext frc, String str)
    {
        return (GlyphVector)new StandardGlyphVector(this, str, frc);
    }

    /**
     * 通过基于此 <code>Font</code> 中的 Unicode cmap 将字符映射到字形来创建一个 {@link java.awt.font.GlyphVector GlyphVector}。此方法除了将字形映射到字符外不进行其他处理。这意味着此方法对于需要重新排序、成形或连字替换的脚本（如阿拉伯文、希伯来文、泰文和印度文）没有用。
     * @param frc 指定的 <code>FontRenderContext</code>
     * @param chars 指定的字符数组
     * @return 一个使用指定字符数组和指定 <code>FontRenderContext</code> 创建的新 <code>GlyphVector</code>。
     */
    public GlyphVector createGlyphVector(FontRenderContext frc, char[] chars)
    {
        return (GlyphVector)new StandardGlyphVector(this, chars, frc);
    }

    /**
     * 通过基于此 <code>Font</code> 中的 Unicode cmap 将指定字符映射到字形来创建一个 {@link java.awt.font.GlyphVector GlyphVector}。此方法除了将字形映射到字符外不进行其他处理。这意味着此方法对于需要重新排序、成形或连字替换的脚本（如阿拉伯文、希伯来文、泰文和印度文）没有用。
     * @param frc 指定的 <code>FontRenderContext</code>
     * @param ci 指定的 <code>CharacterIterator</code>
     * @return 一个使用指定 <code>CharacterIterator</code> 和指定 <code>FontRenderContext</code> 创建的新 <code>GlyphVector</code>。
     */
    public GlyphVector createGlyphVector(   FontRenderContext frc,
                                            CharacterIterator ci)
    {
        return (GlyphVector)new StandardGlyphVector(this, ci, frc);
    }

    /**
     * 通过基于此 <code>Font</code> 中的 Unicode cmap 将字符映射到字形来创建一个 {@link java.awt.font.GlyphVector GlyphVector}。此方法除了将字形映射到字符外不进行其他处理。这意味着此方法对于需要重新排序、成形或连字替换的脚本（如阿拉伯文、希伯来文、泰文和印度文）没有用。
     * @param frc 指定的 <code>FontRenderContext</code>
     * @param glyphCodes 指定的整数数组
     * @return 一个使用指定整数数组和指定 <code>FontRenderContext</code> 创建的新 <code>GlyphVector</code>。
     */
    public GlyphVector createGlyphVector(   FontRenderContext frc,
                                            int [] glyphCodes)
    {
        return (GlyphVector)new StandardGlyphVector(this, glyphCodes, frc);
    }

    /**
     * 返回一个新的 <code>GlyphVector</code> 对象，如果可能的话，执行文本的完整布局。对于复杂文本（如阿拉伯文或印地文），需要进行完整布局。不同脚本的支持取决于字体和实现。
     * <p>
     * 布局需要进行双向分析（由 <code>Bidi</code> 执行），并且应该仅在文本具有统一方向时进行。方向在标志参数中指示，使用 LAYOUT_RIGHT_TO_LEFT 表示从右到左（阿拉伯文和希伯来文）的运行方向，或使用 LAYOUT_LEFT_TO_RIGHT 表示从左到右（英语）的运行方向。
     * <p>
     * 此外，某些操作（如阿拉伯文成形）需要上下文，以便起始和结束字符可以具有适当的形状。有时缓冲区中提供的范围之外的数据没有有效数据。可以在标志参数中添加 LAYOUT_NO_START_CONTEXT 和 LAYOUT_NO_LIMIT_CONTEXT 值，以指示不应检查起始之前或结束之后的文本。
     * <p>
     * 标志参数的所有其他值都是保留的。
     *
     * @param frc 指定的 <code>FontRenderContext</code>
     * @param text 要布局的文本
     * @param start 用于 <code>GlyphVector</code> 的文本的起始位置
     * @param limit 用于 <code>GlyphVector</code> 的文本的结束位置
     * @param flags 如上所述的控制标志
     * @return 一个新的 <code>GlyphVector</code>，表示从起始到结束的文本，选择和定位字形以最好地表示文本
     * @throws ArrayIndexOutOfBoundsException 如果起始或结束位置超出范围
     * @see java.text.Bidi
     * @see #LAYOUT_LEFT_TO_RIGHT
     * @see #LAYOUT_RIGHT_TO_LEFT
     * @see #LAYOUT_NO_START_CONTEXT
     * @see #LAYOUT_NO_LIMIT_CONTEXT
     * @since 1.4
     */
    public GlyphVector layoutGlyphVector(FontRenderContext frc,
                                         char[] text,
                                         int start,
                                         int limit,
                                         int flags) {

        GlyphLayout gl = GlyphLayout.get(null); // !!! 没有自定义布局引擎
        StandardGlyphVector gv = gl.layout(this, frc, text,
                                           start, limit-start, flags, null);
        GlyphLayout.done(gl);
        return gv;
    }

    /**
     * 一个标志，表示通过 Bidi 分析确定的文本是从左到右的。
     */
    public static final int LAYOUT_LEFT_TO_RIGHT = 0;

    /**
     * 一个标志，表示通过 Bidi 分析确定的文本是从右到左的。
     */
    public static final int LAYOUT_RIGHT_TO_LEFT = 1;

    /**
     * 一个标志，表示不应检查字符数组中指示的起始位置之前的文本。
     */
    public static final int LAYOUT_NO_START_CONTEXT = 2;

    /**
     * 一个标志，表示不应检查字符数组中指示的结束位置之后的文本。
     */
    public static final int LAYOUT_NO_LIMIT_CONTEXT = 4;


    private static void applyTransform(AffineTransform trans, AttributeValues values) {
        if (trans == null) {
            throw new IllegalArgumentException("转换不能为空");
        }
        values.setTransform(trans);
    }

    private static void applyStyle(int style, AttributeValues values) {
        // WEIGHT_BOLD, WEIGHT_REGULAR
        values.setWeight((style & BOLD) != 0 ? 2f : 1f);
        // POSTURE_OBLIQUE, POSTURE_REGULAR
        values.setPosture((style & ITALIC) != 0 ? .2f : 0f);
    }

    /*
     * Initialize JNI field and method IDs
     */
    private static native void initIDs();
}
