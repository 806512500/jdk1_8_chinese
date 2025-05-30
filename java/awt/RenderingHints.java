
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

package java.awt;

import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import sun.awt.SunHints;
import java.lang.ref.WeakReference;

/**
 * {@code RenderingHints} 类定义和管理键和关联值的集合，这些键和值允许应用程序提供输入，
 * 以选择其他执行渲染和图像处理服务的类所使用的算法。
 * {@link java.awt.Graphics2D} 类以及实现 {@link java.awt.image.BufferedImageOp} 和
 * {@link java.awt.image.RasterOp} 的类都提供了获取和可能设置单个或一组 {@code RenderingHints}
 * 键及其关联值的方法。
 * 当这些实现执行任何渲染或图像处理操作时，它们应检查调用者请求的任何 {@code RenderingHints}，
 * 并相应地调整所使用的算法，以尽可能接近请求的算法。
 * <p>
 * 请注意，由于这些键和值是“提示”，因此不要求给定实现支持以下所有可能的选择，或能够响应修改算法的请求。
 * 各种提示键的值之间也可能相互作用，因此在某些情况下，虽然所有变体的给定键都受支持，但当与之关联的其他键的值被修改时，实现可能会受到更多限制。
 * 例如，某些实现可能在关闭抗锯齿提示时能够提供多种类型的抖动，但在开启抗锯齿时对抖动的控制较少。
 * 完整支持的键和提示集也可能因目标而异，因为运行时可能使用不同的底层模块来渲染到屏幕、
 * {@link java.awt.image.BufferedImage} 对象或在打印时。
 * <p>
 * 实现可以完全忽略这些提示，但应尽量使用尽可能接近请求的实现算法。
 * 如果实现支持在与某个提示键关联的任何值下使用给定算法，则当该键的值是精确指定该算法的值时，必须至少这样做。
 * <p>
 * 用于控制提示的键都是特殊的值，这些值子类化了关联的 {@link RenderingHints.Key} 类。
 * 许多常见的提示在此类中作为静态常量表达，但列表并非详尽无遗。
 * 其他提示可以通过定义新的子类化 {@code Key} 类的对象并定义关联的值来由其他包创建。
 */
public class RenderingHints
    implements Map<Object,Object>, Cloneable
{
    /**
     * 定义与 {@link RenderingHints} 类一起使用的所有键的基本类型，以控制渲染和图像处理管道中的各种算法选择。
     * 该类的实例是不可变且唯一的，这意味着可以使用 {@code ==} 运算符而不是更昂贵的 {@code equals()} 方法来测试匹配。
     */
    public abstract static class Key {
        private static HashMap<Object,Object> identitymap = new HashMap<>(17);

        private String getIdentity() {
            // 注意，身份字符串取决于 3 个变量：
            //     - Key 的子类的名称
            //     - Key 的子类的身份哈希码
            //     - Key 的整数键
            // 从理论上讲，在多个类加载器的上下文中，两个不同的键在这 3 个属性上发生冲突是可能的，但这种情况极为罕见，
            // 我们在下面的 recordIdentity 方法中通过稍微放松唯一性保证来处理这种可能性。
            return getClass().getName()+"@"+
                Integer.toHexString(System.identityHashCode(getClass()))+":"+
                Integer.toHexString(privatekey);
        }

        private synchronized static void recordIdentity(Key k) {
            Object identity = k.getIdentity();
            Object otherref = identitymap.get(identity);
            if (otherref != null) {
                Key otherkey = (Key) ((WeakReference) otherref).get();
                if (otherkey != null && otherkey.getClass() == k.getClass()) {
                    throw new IllegalArgumentException(identity+
                                                       " already registered");
                }
                // 注意，此系统可以以一种几乎无害的方式失败。如果最终为两个不同的类生成相同的标识字符串（非常罕见的情况），
                // 那么我们正确地避免抛出上面的异常，但我们将跳过一个语句，该语句将用新 Key 子类的条目替换旧 Key 子类的条目。
                // 在那时，旧子类将容易受到有人为它生成重复的 Key 实例的影响。我们可以在方法中退出并让旧标识保持在映射中的记录，
                // 但更有可能的是，新类会比旧类更频繁地看到重复的键，因为新类可能仍处于初始化阶段。无论如何，加载两个具有相同名称和身份哈希码的类的概率几乎为零。
            }
            // 注意：使用弱引用以避免在应该卸载对象和类后仍保留它们。
            identitymap.put(identity, new WeakReference<Key>(k));
        }

        private int privatekey;

        /**
         * 使用指定的私有键构造一个键。每个 Key 的子类都维护其自己的唯一整数键域。
         * 不能构造两个具有相同整数键和相同特定子类的对象。如果尝试构造具有与现有实例相同整数键的给定类的另一个对象，将抛出异常。
         * @param privatekey 指定的键
         */
        protected Key(int privatekey) {
            this.privatekey = privatekey;
            recordIdentity(this);
        }

        /**
         * 如果指定的对象是此 Key 的有效值，则返回 true。
         * @param val 要测试有效性的 <code>Object</code>
         * @return 如果 <code>val</code> 有效，则返回 <code>true</code>；否则返回 <code>false</code>。
         */
        public abstract boolean isCompatibleValue(Object val);

        /**
         * 返回子类实例化此 Key 时使用的私有整数键。
         * @return 子类实例化此 Key 时使用的私有整数键。
         */
        protected final int intKey() {
            return privatekey;
        }

        /**
         * 所有 Key 对象的哈希码将与 System.identityHashCode() 方法定义的对象的系统身份码相同。
         */
        public final int hashCode() {
            return super.hashCode();
        }

        /**
         * 所有 Key 对象的 equals 方法将返回与相等运算符 '==' 相同的结果。
         */
        public final boolean equals(Object o) {
            return this == o;
        }
    }

    HashMap<Object,Object> hintmap = new HashMap<>(7);

    /**
     * 抗锯齿提示键。
     * {@code ANTIALIASING} 提示控制 {@link Graphics2D} 对象的几何渲染方法是否尝试减少形状边缘的锯齿伪影。
     * <p>
     * 典型的抗锯齿算法通过根据形状的估计部分像素覆盖率将形状边界像素的现有颜色与请求的填充颜色混合来工作。
     * <p>
     * 此提示的允许值为
     * <ul>
     * <li>{@link #VALUE_ANTIALIAS_ON}
     * <li>{@link #VALUE_ANTIALIAS_OFF}
     * <li>{@link #VALUE_ANTIALIAS_DEFAULT}
     * </ul>
     */
    public static final Key KEY_ANTIALIASING =
        SunHints.KEY_ANTIALIASING;

    /**
     * 抗锯齿提示值 -- 渲染时使用抗锯齿。
     * @see #KEY_ANTIALIASING
     */
    public static final Object VALUE_ANTIALIAS_ON =
        SunHints.VALUE_ANTIALIAS_ON;

    /**
     * 抗锯齿提示值 -- 渲染时禁用抗锯齿。
     * @see #KEY_ANTIALIASING
     */
    public static final Object VALUE_ANTIALIAS_OFF =
        SunHints.VALUE_ANTIALIAS_OFF;

    /**
     * 抗锯齿提示值 -- 渲染时使用由实现选择的默认抗锯齿模式。
     * @see #KEY_ANTIALIASING
     */
    public static final Object VALUE_ANTIALIAS_DEFAULT =
         SunHints.VALUE_ANTIALIAS_DEFAULT;

    /**
     * 渲染提示键。
     * {@code RENDERING} 提示是一个通用提示，提供在评估权衡时更偏向速度或质量的高级建议。
     * 此提示可以在任何渲染或图像处理操作中咨询，但通常会优先考虑其他更具体的提示。
     * <p>
     * 此提示的允许值为
     * <ul>
     * <li>{@link #VALUE_RENDER_SPEED}
     * <li>{@link #VALUE_RENDER_QUALITY}
     * <li>{@link #VALUE_RENDER_DEFAULT}
     * </ul>
     */
    public static final Key KEY_RENDERING =
         SunHints.KEY_RENDERING;

    /**
     * 渲染提示值 -- 选择渲染算法时优先考虑输出速度。
     * @see #KEY_RENDERING
     */
    public static final Object VALUE_RENDER_SPEED =
         SunHints.VALUE_RENDER_SPEED;

    /**
     * 渲染提示值 -- 选择渲染算法时优先考虑输出质量。
     * @see #KEY_RENDERING
     */
    public static final Object VALUE_RENDER_QUALITY =
         SunHints.VALUE_RENDER_QUALITY;

    /**
     * 渲染提示值 -- 由实现选择渲染算法，以在性能和质量之间取得良好的平衡。
     * @see #KEY_RENDERING
     */
    public static final Object VALUE_RENDER_DEFAULT =
         SunHints.VALUE_RENDER_DEFAULT;

    /**
     * 抖动提示键。
     * {@code DITHERING} 提示控制在存储到颜色分辨率有限的目标时如何近似颜色。
     * <p>
     * 一些渲染目标可能支持有限数量的颜色选择，这些颜色可能无法准确表示渲染操作期间可能产生的完整颜色谱。
     * 对于这样的目标，{@code DITHERING} 提示控制渲染是使用最接近请求颜色的单个像素值的平面实心填充，还是使用组合以更好地近似该颜色的颜色模式填充形状。
     * <p>
     * 此提示的允许值为
     * <ul>
     * <li>{@link #VALUE_DITHER_DISABLE}
     * <li>{@link #VALUE_DITHER_ENABLE}
     * <li>{@link #VALUE_DITHER_DEFAULT}
     * </ul>
     */
    public static final Key KEY_DITHERING =
         SunHints.KEY_DITHERING;

    /**
     * 抖动提示值 -- 渲染几何图形时不使用抖动。
     * @see #KEY_DITHERING
     */
    public static final Object VALUE_DITHER_DISABLE =
         SunHints.VALUE_DITHER_DISABLE;

    /**
     * 抖动提示值 -- 如果需要，渲染几何图形时使用抖动。
     * @see #KEY_DITHERING
     */
    public static final Object VALUE_DITHER_ENABLE =
         SunHints.VALUE_DITHER_ENABLE;

    /**
     * 抖动提示值 -- 使用由实现选择的默认抖动。
     * @see #KEY_DITHERING
     */
    public static final Object VALUE_DITHER_DEFAULT =
         SunHints.VALUE_DITHER_DEFAULT;

    /**
     * 文本抗锯齿提示键。
     * {@code TEXT_ANTIALIASING} 提示可以独立于形状渲染选择来控制文本抗锯齿算法的使用。
     * 通常，应用程序可能希望仅对文本使用抗锯齿，而不对其他形状使用。
 * 此外，用于减少文本锯齿伪影的算法通常比为一般渲染开发的算法更复杂，因此此提示键提供了额外的值，可以控制一些特定于文本的算法选择。
 * 如果保持在 {@code DEFAULT} 状态，此提示通常会遵循常规 {@link #KEY_ANTIALIASING} 提示键的值。
 * <p>
 * 此提示的允许值为
 * <ul>
 * <li>{@link #VALUE_TEXT_ANTIALIAS_ON}
 * <li>{@link #VALUE_TEXT_ANTIALIAS_OFF}
 * <li>{@link #VALUE_TEXT_ANTIALIAS_DEFAULT}
 * <li>{@link #VALUE_TEXT_ANTIALIAS_GASP}
 * <li>{@link #VALUE_TEXT_ANTIALIAS_LCD_HRGB}
 * <li>{@link #VALUE_TEXT_ANTIALIAS_LCD_HBGR}
 * <li>{@link #VALUE_TEXT_ANTIALIAS_LCD_VRGB}
 * <li>{@link #VALUE_TEXT_ANTIALIAS_LCD_VBGR}
 * </ul>
 */
    public static final Key KEY_TEXT_ANTIALIASING =
         SunHints.KEY_TEXT_ANTIALIASING;

    /**
     * 文本抗锯齿提示值 -- 文本渲染时使用某种形式的抗锯齿。
     * @see #KEY_TEXT_ANTIALIASING
     */
    public static final Object VALUE_TEXT_ANTIALIAS_ON =
         SunHints.VALUE_TEXT_ANTIALIAS_ON;


                /**
     * 文本抗锯齿提示值 -- 文本渲染不使用任何形式的抗锯齿。
     * @see #KEY_TEXT_ANTIALIASING
     */
    public static final Object VALUE_TEXT_ANTIALIAS_OFF =
         SunHints.VALUE_TEXT_ANTIALIAS_OFF;

    /**
     * 文本抗锯齿提示值 -- 文本渲染根据 {@link #KEY_ANTIALIASING} 提示或实现选择的默认值进行。
     * @see #KEY_TEXT_ANTIALIASING
     */
    public static final Object VALUE_TEXT_ANTIALIAS_DEFAULT =
         SunHints.VALUE_TEXT_ANTIALIAS_DEFAULT;

    /**
     * 文本抗锯齿提示值 -- 请求文本渲染使用字体资源中指定的信息，该信息指定了每个点大小是否应用 {@link #VALUE_TEXT_ANTIALIAS_ON} 或
     * {@link #VALUE_TEXT_ANTIALIAS_OFF}。
     * <p>
     * TrueType 字体通常在 'gasp' 表中提供此信息。如果没有此信息，则特定字体和大小的行为由实现默认值确定。
     * <p>
     * <i>注意：</i>字体设计师通常会仔细提示字体以适应最常见的用户界面点大小。因此，'gasp' 表通常会指定在这些大小下仅使用提示而不进行“平滑”。因此，在许多情况下，最终的文本显示等同于 {@code VALUE_TEXT_ANTIALIAS_OFF}。
     * 这可能是意外的，但却是正确的。
     * <p>
     * 由多个物理字体组成的逻辑字体为了保持一致性，将使用最适用于整体复合字体的设置。
     *
     * @see #KEY_TEXT_ANTIALIASING
     * @since 1.6
     */
    public static final Object VALUE_TEXT_ANTIALIAS_GASP =
         SunHints.VALUE_TEXT_ANTIALIAS_GASP;

    /**
     * 文本抗锯齿提示值 -- 请求文本显示优化适用于 LCD 显示器，其子像素从左到右的顺序为 R,G,B，使得水平子像素分辨率是全像素水平分辨率的三倍（HRGB）。
     * 这是最常见的配置。
     * 为具有其他 LCD 子像素配置的显示器选择此提示可能会导致模糊的文本。
     * <p>
     * <i>注意：</i><br>
     * 实现在选择是否应用任何 LCD 文本提示值时，可能会考虑以下因素：要求目标的颜色深度至少为每像素 15 位（即每颜色组件 5 位），
     * 字体的特性，例如嵌入的位图是否可能产生更好的结果，或在显示到非本地网络显示设备时仅在有合适协议的情况下启用，
     * 或在进行非常高分辨率渲染或目标设备不适用时忽略提示：例如打印。
     * <p>
     * 这些提示同样可以应用于软件图像，但这些图像可能不适合通用导出，因为文本已针对特定的子像素组织进行了渲染。此外，有损图像不是好的选择，图像格式如 GIF 也有颜色限制。
     * 因此，除非图像是为了在具有相同配置的显示设备上显示，否则选择其他文本抗锯齿提示（如
     * {@link #VALUE_TEXT_ANTIALIAS_ON}）可能是更好的选择。
     * <p>
     * 选择与使用的 LCD 显示器不匹配的值可能会导致文本质量下降。
     * 在不具有与 LCD 显示器相同特性的显示设备（例如 CRT）上，整体效果可能类似于标准文本抗锯齿，但质量可能会因颜色失真而下降。
     * 模拟连接的 LCD 显示器可能显示不出比标准文本抗锯齿更多的优势，且可能类似于 CRT。
     * <p>
     * 换句话说，为了获得最佳效果，请使用具有数字显示连接器的 LCD 显示器，并指定适当的子像素配置。
     *
     * @see #KEY_TEXT_ANTIALIASING
     * @since 1.6
     */
    public static final Object VALUE_TEXT_ANTIALIAS_LCD_HRGB =
         SunHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;

    /**
     * 文本抗锯齿提示值 -- 请求文本显示优化适用于 LCD 显示器，其子像素从左到右的顺序为 B,G,R，使得水平子像素分辨率是全像素水平分辨率的三倍（HBGR）。
     * 这比 HRGB 配置要少见得多。
     * 为具有其他 LCD 子像素配置的显示器选择此提示可能会导致模糊的文本。
     * 有关此提示何时应用的更多信息，请参见 {@link #VALUE_TEXT_ANTIALIAS_LCD_HRGB}。
     *
     * @see #KEY_TEXT_ANTIALIASING
     * @since 1.6
     */
    public static final Object VALUE_TEXT_ANTIALIAS_LCD_HBGR =
         SunHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR;

    /**
     * 文本抗锯齿提示值 -- 请求文本显示优化适用于 LCD 显示器，其子像素从上到下的顺序为 R,G,B，使得垂直子像素分辨率是全像素垂直分辨率的三倍（VRGB）。
     * 垂直方向非常少见，可能主要用于物理旋转的显示器。
     * 为具有其他 LCD 子像素配置的显示器选择此提示可能会导致模糊的文本。
     * 有关此提示何时应用的更多信息，请参见 {@link #VALUE_TEXT_ANTIALIAS_LCD_HRGB}。
     *
     * @see #KEY_TEXT_ANTIALIASING
     * @since 1.6
     */
    public static final Object VALUE_TEXT_ANTIALIAS_LCD_VRGB =
         SunHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB;

    /**
     * 文本抗锯齿提示值 -- 请求文本显示优化适用于 LCD 显示器，其子像素从上到下的顺序为 B,G,R，使得垂直子像素分辨率是全像素垂直分辨率的三倍（VBGR）。
     * 垂直方向非常少见，可能主要用于物理旋转的显示器。
     * 为具有其他 LCD 子像素配置的显示器选择此提示可能会导致模糊的文本。
     * 有关此提示何时应用的更多信息，请参见 {@link #VALUE_TEXT_ANTIALIAS_LCD_HRGB}。
     *
     * @see #KEY_TEXT_ANTIALIASING
     * @since 1.6
     */
    public static final Object VALUE_TEXT_ANTIALIAS_LCD_VBGR =
         SunHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR;

    /**
     * LCD 文本对比度渲染提示键。
     * 该值是一个 <code>Integer</code> 对象，当与 LCD 文本抗锯齿提示（如
     * {@link #VALUE_TEXT_ANTIALIAS_LCD_HRGB}）一起使用时，用作文本对比度调整。
     * <ul>
     * <li>值应为 100 到 250 之间的正整数。
     * <li>较低的值（如 100）在显示深色文本在浅色背景上时对应更高的对比度。
     * <li>较高的值（如 200）在显示深色文本在浅色背景上时对应更低的对比度。
     * <li>通常有用的值在 140-180 的窄范围内。
     * <li>如果没有指定值，将应用系统或实现的默认值。
     * </ul>
     * 默认值通常足以满足大多数用途，因此客户端很少需要指定此提示的值，除非他们有具体的适当值信息。
     * 较高的值并不意味着更高的对比度，事实上恰恰相反。
     * 校正以类似于显示系统非线性感知亮度响应的伽马调整的方式应用，但并不表示完全的校正。
     *
     * @see #KEY_TEXT_ANTIALIASING
     * @since 1.6
     */
    public static final Key KEY_TEXT_LCD_CONTRAST =
        SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST;

    /**
     * 字体分数度量提示键。
     * {@code FRACTIONALMETRICS} 提示控制单个字符字形的定位是否考虑字体的缩放字符进位的子像素精度，还是将这些进位向量四舍五入为整数个设备像素。
     * 此提示仅建议在定位字形时应使用多少精度，并不指定或推荐是否应修改实际的光栅化或字形的像素边界。
     * <p>
     * 将文本渲染到低分辨率设备（如屏幕）上时，必然涉及许多四舍五入操作，因为字符字形的高质量和非常精确的形状和度量定义必须与离散的设备像素匹配。
     * 理想情况下，文本布局期间字形的定位应通过根据点大小缩放字体中的设计度量来计算，但缩放后的进位宽度不一定为整数个像素。
     * 如果根据这些缩放后的设计度量以子像素精度定位字形，则光栅化应理想地针对每个可能的子像素原点进行调整。
     * <p>
     * 不幸的是，在文本布局期间按每个字形的精确子像素原点进行缩放定制是极其昂贵的，因此通常使用基于整数设备位置的简化系统来布局文本。
     * 字形的光栅化和缩放后的进位宽度一起调整，以生成在设备分辨率下看起来良好的文本，并且字形之间的整数像素距离一致，使字形看起来均匀且一致地间隔和可读。
     * <p>
     * 将光栅化字形的进位宽度四舍五入为整数距离意味着字符密度和文本字符串的总长度将与理论设计测量值不同，因为一系列小差异的累积导致每个字形的调整宽度不同。
     * 每个字形的具体差异将不同，有些更宽，有些更窄，因此字符密度和长度的总体差异将因多种因素而异，包括字体、目标的具体设备分辨率和用于表示要渲染的字符串的字形。
     * 因此，在多个设备分辨率下渲染相同的字符串可能会产生广泛变化的字符串度量。
     * <p>
     * 当启用 {@code FRACTIONALMETRICS} 时，字体的真实设计度量将按点大小缩放并以子像素精度用于布局。
     * 因此，长字符串中字符的平均密度和总长度将更接近字体的理论设计，但可读性可能受到影响，因为个别字符对之间的距离可能不总是看起来一致，具体取决于字形原点的子像素累积与设备像素网格的匹配情况。
     * 在需要在各种输出分辨率下保持一致的文本布局时，启用此提示可能是可取的。
     * 具体来说，在低分辨率设备（如屏幕）上预览文本布局以最终在高分辨率打印机或排版设备上渲染输出时，此提示可能是可取的。
     * <p>
     * 当禁用时，缩放后的设计度量将四舍五入或调整为整数距离以进行布局。
     * 任何特定字形对之间的距离在设备上将更加均匀，但长字符串的密度和总长度可能不再与字体设计师的理论意图匹配。
     * 禁用此提示通常会在低分辨率设备（如计算机显示器）上产生更可读的结果。
     * <p>
     * 此键的允许值为
     * <ul>
     * <li>{@link #VALUE_FRACTIONALMETRICS_OFF}
     * <li>{@link #VALUE_FRACTIONALMETRICS_ON}
     * <li>{@link #VALUE_FRACTIONALMETRICS_DEFAULT}
     * </ul>
     */
    public static final Key KEY_FRACTIONALMETRICS =
         SunHints.KEY_FRACTIONALMETRICS;

    /**
     * 字体分数度量提示值 -- 字符字形的进位宽度四舍五入到像素边界。
     * @see #KEY_FRACTIONALMETRICS
     */
    public static final Object VALUE_FRACTIONALMETRICS_OFF =
         SunHints.VALUE_FRACTIONALMETRICS_OFF;

    /**
     * 字体分数度量提示值 -- 字符字形以子像素精度定位。
     * @see #KEY_FRACTIONALMETRICS
     */
    public static final Object VALUE_FRACTIONALMETRICS_ON =
         SunHints.VALUE_FRACTIONALMETRICS_ON;

    /**
     * 字体分数度量提示值 -- 字符字形的精度由实现选择。
     * @see #KEY_FRACTIONALMETRICS
     */
    public static final Object VALUE_FRACTIONALMETRICS_DEFAULT =
         SunHints.VALUE_FRACTIONALMETRICS_DEFAULT;

    /**
     * 插值提示键。
     * {@code INTERPOLATION} 提示控制在图像渲染操作期间如何对图像像素进行滤波或重采样。
     * <p>
     * 隐式地，图像定义在整数坐标位置提供颜色样本。
     * 当图像以直立且无缩放的方式渲染到目标上时，图像像素映射到设备像素的选择是显而易见的，图像中整数坐标位置的样本将一对一地传输到设备像素网格中相应整数位置的像素。
     * 当图像在缩放、旋转或以其他方式变换的坐标系统中渲染时，设备像素坐标映射回图像的问题可能涉及到使用连续坐标，这些坐标位于提供的图像样本的整数位置之间。
     * 插值算法定义了函数，这些函数基于周围整数坐标处的颜色样本为图像中的任何连续坐标提供颜色样本。
     * <p>
     * 此提示的允许值为
     * <ul>
     * <li>{@link #VALUE_INTERPOLATION_NEAREST_NEIGHBOR}
     * <li>{@link #VALUE_INTERPOLATION_BILINEAR}
     * <li>{@link #VALUE_INTERPOLATION_BICUBIC}
     * </ul>
     */
    public static final Key KEY_INTERPOLATION =
         SunHints.KEY_INTERPOLATION;


                /**
     * 插值提示值 -- 使用图像中最近的整数坐标样本的颜色。
     * 概念上，图像被视为一个由每个图像像素中心的颜色组成的单位大小的方形区域网格。
     * <p>
     * 当图像放大时，看起来会相应地变得块状。
     * 当图像缩小，源像素的颜色将在输出表示中被使用或完全跳过。
     *
     * @see #KEY_INTERPOLATION
     */
    public static final Object VALUE_INTERPOLATION_NEAREST_NEIGHBOR =
         SunHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

    /**
     * 插值提示值 -- 图像中最近的4个整数坐标样本的颜色被线性插值以生成一个颜色样本。
     * 概念上，图像被视为一组无限小的点颜色样本，这些样本仅在整数坐标像素中心有值，而这些像素中心之间的空间则用连接相邻离散样本的直线颜色渐变填充。
     * <p>
     * 当图像放大时，图像中的颜色之间没有像 {@link #VALUE_INTERPOLATION_NEAREST_NEIGHBOR NEAREST_NEIGHBOR} 那样的块状边缘，但水平和垂直边缘上可能会出现一些微妙的不连续性，这是由于样本一侧到另一侧的插值斜率突然变化造成的。
     * 当图像缩小，更多的原始图像像素的颜色样本在最终输出中得到表示，因为每个输出像素从最多4个图像像素接收颜色信息。
     *
     * @see #KEY_INTERPOLATION
     */
    public static final Object VALUE_INTERPOLATION_BILINEAR =
         SunHints.VALUE_INTERPOLATION_BILINEAR;

    /**
     * 插值提示值 -- 图像中9个附近的整数坐标样本的颜色使用在 {@code X} 和 {@code Y} 方向上的三次函数插值生成一个颜色样本。
     * 概念上，图像的视图与 {@link #VALUE_INTERPOLATION_BILINEAR BILINEAR} 算法中使用的视图非常相似，不同之处在于连接样本之间的颜色渐变是弯曲的，并且在样本边界之间交叉时具有更好的斜率连续性。
     * <p>
     * 当图像放大时，没有块状边缘，插值应该看起来更平滑，对原始图像中的任何边缘的描绘也更好，比 {@code BILINEAR} 更好。当图像缩小，更多的原始颜色样本将通过并表示在最终输出中。
     *
     * @see #KEY_INTERPOLATION
     */
    public static final Object VALUE_INTERPOLATION_BICUBIC =
         SunHints.VALUE_INTERPOLATION_BICUBIC;

    /**
     * Alpha 插值提示键。
     * {@code ALPHA_INTERPOLATION} 提示是一个通用提示，提供了一个高级别的建议，即在评估权衡时，是否更倾向于选择速度或质量的 alpha 混合算法。
     * <p>
     * 这个提示可以控制选择牺牲一些精度以使用快速查找表或低精度 SIMD 指令的 alpha 混合计算。
     * 这个提示还可以控制是否在计算过程中将颜色和 alpha 值转换为线性颜色空间，以获得更线性的视觉效果，但代价是每个像素的额外计算。
     * <p>
     * 该提示的允许值为
     * <ul>
     * <li>{@link #VALUE_ALPHA_INTERPOLATION_SPEED}
     * <li>{@link #VALUE_ALPHA_INTERPOLATION_QUALITY}
     * <li>{@link #VALUE_ALPHA_INTERPOLATION_DEFAULT}
     * </ul>
     */
    public static final Key KEY_ALPHA_INTERPOLATION =
         SunHints.KEY_ALPHA_INTERPOLATION;

    /**
     * Alpha 插值提示值 -- 选择以计算速度为优先的 alpha 混合算法。
     * @see #KEY_ALPHA_INTERPOLATION
     */
    public static final Object VALUE_ALPHA_INTERPOLATION_SPEED =
         SunHints.VALUE_ALPHA_INTERPOLATION_SPEED;

    /**
     * Alpha 插值提示值 -- 选择以精度和视觉质量为优先的 alpha 混合算法。
     * @see #KEY_ALPHA_INTERPOLATION
     */
    public static final Object VALUE_ALPHA_INTERPOLATION_QUALITY =
         SunHints.VALUE_ALPHA_INTERPOLATION_QUALITY;

    /**
     * Alpha 插值提示值 -- 选择由实现决定的性能与质量之间的良好权衡的 alpha 混合算法。
     * @see #KEY_ALPHA_INTERPOLATION
     */
    public static final Object VALUE_ALPHA_INTERPOLATION_DEFAULT =
         SunHints.VALUE_ALPHA_INTERPOLATION_DEFAULT;

    /**
     * 颜色渲染提示键。
     * {@code COLOR_RENDERING} 提示控制将颜色存储到目标图像或表面时的近似和转换的准确性。
     * <p>
     * 当渲染或图像操作操作生成一个颜色值并需要将其存储到目标时，必须首先将该颜色转换为适合存储到目标图像或表面的形式。
     * 最小地，颜色分量必须转换为位表示并按正确的顺序排列，或者必须选择一个颜色查找表的索引，然后才能将数据存储到目标内存中。
     * 没有这种最小的转换，目标中的数据可能会表示随机、不正确或可能甚至不受支持的值。
     * 快速将渲染操作的结果转换为目标颜色格式的算法是众所周知且执行效率较高的。
     * <p>
     * 仅执行将颜色存储到目标的最基本颜色格式转换可能会忽略源和目标 {@link java.awt.color.ColorSpace} 之间的校准差异或其他因素，如伽马校正的线性。
     * 除非源和目标 {@code ColorSpace} 完全相同，要正确执行渲染操作并尽可能准确地表示颜色，应将源颜色转换为设备独立的 {@code ColorSpace}，然后将结果转换回目标 {@code ColorSpace}。
     * 此外，如果在渲染操作期间需要执行多个源颜色的混合等计算，如果选择的中间设备独立 {@code ColorSpace} 与人类眼睛对输出设备响应曲线的感知之间具有线性关系，可以实现更高的视觉清晰度。
     * <p>
     * 该提示的允许值为
     * <ul>
     * <li>{@link #VALUE_COLOR_RENDER_SPEED}
     * <li>{@link #VALUE_COLOR_RENDER_QUALITY}
     * <li>{@link #VALUE_COLOR_RENDER_DEFAULT}
     * </ul>
     */
    public static final Key KEY_COLOR_RENDERING =
         SunHints.KEY_COLOR_RENDERING;

    /**
     * 颜色渲染提示值 -- 执行到输出设备格式的最快颜色转换。
     * @see #KEY_COLOR_RENDERING
     */
    public static final Object VALUE_COLOR_RENDER_SPEED =
         SunHints.VALUE_COLOR_RENDER_SPEED;

    /**
     * 颜色渲染提示值 -- 以最高准确性和视觉质量执行颜色转换计算。
     * @see #KEY_COLOR_RENDERING
     */
    public static final Object VALUE_COLOR_RENDER_QUALITY =
         SunHints.VALUE_COLOR_RENDER_QUALITY;

    /**
     * 颜色渲染提示值 -- 执行由实现选择的性能与准确性之间的最佳可用权衡的颜色转换计算。
     * @see #KEY_COLOR_RENDERING
     */
    public static final Object VALUE_COLOR_RENDER_DEFAULT =
         SunHints.VALUE_COLOR_RENDER_DEFAULT;

    /**
     * 描边规范化控制提示键。
     * {@code STROKE_CONTROL} 提示控制渲染实现是否或允许修改渲染形状的几何形状以达到各种目的。
     * <p>
     * 一些实现可能能够使用优化的平台渲染库，该库在给定平台上可能比传统的软件渲染算法更快，但可能不支持浮点坐标。
     * 一些实现可能还具有复杂的算法，这些算法会扰动路径的坐标，使宽线看起来宽度和间距更均匀。
     * <p>
     * 如果实现执行任何类型的修改或“规范化”路径，它不应将坐标移动超过任何方向的半个像素。
     * <p>
     * 该提示的允许值为
     * <ul>
     * <li>{@link #VALUE_STROKE_NORMALIZE}
     * <li>{@link #VALUE_STROKE_PURE}
     * <li>{@link #VALUE_STROKE_DEFAULT}
     * </ul>
     * @since 1.3
     */
    public static final Key KEY_STROKE_CONTROL =
        SunHints.KEY_STROKE_CONTROL;

    /**
     * 描边规范化控制提示值 -- 几何形状可以被修改或保持不变，具体取决于给定实现中的权衡。
     * 通常，此设置允许实现使用快速的基于整数坐标的平台渲染库，但不特别要求为均匀性或美观性进行规范化。
     *
     * @see #KEY_STROKE_CONTROL
     * @since 1.3
     */
    public static final Object VALUE_STROKE_DEFAULT =
        SunHints.VALUE_STROKE_DEFAULT;

    /**
     * 描边规范化控制提示值 -- 几何形状应被规范化以提高线条的均匀性和间距以及整体美观性。
     * 注意，不同的规范化算法对于给定的输入路径可能比其他算法更成功。
     *
     * @see #KEY_STROKE_CONTROL
     * @since 1.3
     */
    public static final Object VALUE_STROKE_NORMALIZE =
        SunHints.VALUE_STROKE_NORMALIZE;

    /**
     * 描边规范化控制提示值 -- 几何形状应保持不变并以亚像素精度渲染。
     *
     * @see #KEY_STROKE_CONTROL
     * @since 1.3
     */
    public static final Object VALUE_STROKE_PURE =
        SunHints.VALUE_STROKE_PURE;

    /**
     * 从指定的 Map 对象初始化一个新的对象，该对象可以为 null。
     * @param init 用于初始化提示的键值对映射，或 null 如果对象应为空
     */
    public RenderingHints(Map<Key,?> init) {
        if (init != null) {
            hintmap.putAll(init);
        }
    }

    /**
     * 用指定的键值对构造一个新的对象。
     * @param key 特定提示属性的键
     * @param value 用 {@code key} 指定的提示属性的值
     */
    public RenderingHints(Key key, Object value) {
        hintmap.put(key, value);
    }

    /**
     * 返回此 <code>RenderingHints</code> 中的键值映射数量。
     *
     * @return 此 <code>RenderingHints</code> 中的键值映射数量。
     */
    public int size() {
        return hintmap.size();
    }

    /**
     * 如果此 <code>RenderingHints</code> 不包含键值映射，则返回 <code>true</code>。
     *
     * @return 如果此 <code>RenderingHints</code> 不包含键值映射，则返回 <code>true</code>。
     */
    public boolean isEmpty() {
        return hintmap.isEmpty();
    }

    /**
     * 如果此 {@code RenderingHints} 包含指定键的映射，则返回 {@code true}。
     *
     * @param key 要测试其是否存在于此 {@code RenderingHints} 中的键。
     * @return 如果此 {@code RenderingHints} 包含指定键的映射，则返回 {@code true}。
     * @exception ClassCastException 如果键不能转换为 {@code RenderingHints.Key}
     */
    public boolean containsKey(Object key) {
        return hintmap.containsKey((Key) key);
    }

    /**
     * 如果此 RenderingHints 将一个或多个键映射到指定值，则返回 true。
     * 更正式地说，当且仅当此 <code>RenderingHints</code> 包含至少一个映射到值 <code>v</code> 的键时，返回 <code>true</code>，其中
     * <pre>
     * (value==null ? v==null : value.equals(v))
     * </pre>。
     * 对于大多数 <code>RenderingHints</code> 实现，此操作可能需要与 <code>RenderingHints</code> 大小成线性的时间。
     *
     * @param value 要测试其是否存在于此 <code>RenderingHints</code> 中的值。
     * @return 如果此 <code>RenderingHints</code> 将一个或多个键映射到指定值，则返回 <code>true</code>。
     */
    public boolean containsValue(Object value) {
        return hintmap.containsValue(value);
    }

    /**
     * 返回指定键映射的值。
     * @param   key   渲染提示键
     * @return  该键在本对象中映射的值，如果该键在本对象中未映射到任何值，则返回 {@code null}。
     * @exception ClassCastException 如果键不能转换为 {@code RenderingHints.Key}
     * @see     #put(Object, Object)
     */
    public Object get(Object key) {
        return hintmap.get((Key) key);
    }

    /**
     * 在此 {@code RenderingHints} 对象中将指定的 {@code key} 映射到指定的 {@code value}。
     * 键和值都不能为 {@code null}。
     * 可以通过调用带有与原始键相等的键的 {@code get} 方法来检索值。
     * @param      key     渲染提示键。
     * @param      value   渲染提示值。
     * @return     该键在此对象中的前一个值，如果它没有前一个值，则返回 {@code null}。
     * @exception NullPointerException 如果键为 {@code null}。
     * @exception ClassCastException 如果键不能转换为 {@code RenderingHints.Key}
     * @exception IllegalArgumentException 如果指定键的
     *            {@link Key#isCompatibleValue(java.lang.Object)
     *                   Key.isCompatibleValue()}
     *            方法对指定值返回 false
     * @see     #get(Object)
     */
    public Object put(Object key, Object value) {
        if (!((Key) key).isCompatibleValue(value)) {
            throw new IllegalArgumentException(value+
                                               " incompatible with "+
                                               key);
        }
        return hintmap.put((Key) key, value);
    }


                /**
     * 将指定的 <code>RenderingHints</code> 对象中的所有键和对应的值添加到此
     * <code>RenderingHints</code> 对象中。此 <code>RenderingHints</code> 对象中存在但
     * 指定的 <code>RenderingHints</code> 对象中不存在的键不受影响。
     * @param hints 要添加到此 <code>RenderingHints</code> 对象中的键值对集合
     */
    public void add(RenderingHints hints) {
        hintmap.putAll(hints.hintmap);
    }

    /**
     * 清除此 <code>RenderingHints</code> 对象中的所有键值对。
     */
    public void clear() {
        hintmap.clear();
    }

    /**
     * 从此 <code>RenderingHints</code> 对象中移除指定的键及其对应的值。如果此
     * <code>RenderingHints</code> 对象中不存在该键，则此方法不执行任何操作。
     * @param   key   需要移除的渲染提示键
     * @exception ClassCastException 如果键不能转换为 {@code RenderingHints.Key}
     * @return  该键在此次 <code>RenderingHints</code> 对象中之前映射的值，如果该键没有映射，则返回 {@code null}
     */
    public Object remove(Object key) {
        return hintmap.remove((Key) key);
    }

    /**
     * 将指定的 {@code Map} 中的所有映射复制到此 {@code RenderingHints} 中。这些映射将替换
     * 此 {@code RenderingHints} 中当前存在于指定的 {@code Map} 中的任何键的映射。
     * @param m 指定的 {@code Map}
     * @exception ClassCastException 指定的 {@code Map} 中的键或值的类阻止其存储在
     *          此 {@code RenderingHints} 中。
     * @exception IllegalArgumentException 指定的 {@code Map} 中的键或值的某些方面
     *           阻止其存储在此 {@code RenderingHints} 中。
     */
    public void putAll(Map<?,?> m) {
        // ## javac bug?
        //if (m instanceof RenderingHints) {
        if (RenderingHints.class.isInstance(m)) {
            //hintmap.putAll(((RenderingHints) m).hintmap);
            for (Map.Entry<?,?> entry : m.entrySet())
                hintmap.put(entry.getKey(), entry.getValue());
        } else {
            // 通过我们的受保护的 put 方法将每个键值对传递
            for (Map.Entry<?,?> entry : m.entrySet())
                put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 返回包含在此 <code>RenderingHints</code> 中的键的 <code>Set</code> 视图。
     * 该 <code>Set</code> 由 <code>RenderingHints</code> 支持，因此对
     * <code>RenderingHints</code> 的更改会反映在 <code>Set</code> 中，反之亦然。
     * 如果在迭代 <code>Set</code> 时修改了 <code>RenderingHints</code>，
     * 迭代的结果是未定义的。该 <code>Set</code> 支持元素移除，这会从
     * <code>RenderingHints</code> 中移除相应的映射，通过
     * <code>Iterator.remove</code>、<code>Set.remove</code>、
     * <code>removeAll</code>、<code>retainAll</code> 和
     * <code>clear</code> 操作。它不支持 <code>add</code> 或 <code>addAll</code> 操作。
     *
     * @return 包含在此 <code>RenderingHints</code> 中的键的 <code>Set</code> 视图。
     */
    public Set<Object> keySet() {
        return hintmap.keySet();
    }

    /**
     * 返回包含在此 <code>RenderingHints</code> 中的值的 <code>Collection</code> 视图。
     * 该 <code>Collection</code> 由 <code>RenderingHints</code> 支持，因此对
     * <code>RenderingHints</code> 的更改会反映在 <code>Collection</code> 中，反之亦然。
     * 如果在迭代 <code>Collection</code> 时修改了 <code>RenderingHints</code>，
     * 迭代的结果是未定义的。该 <code>Collection</code> 支持元素移除，这会从
     * <code>RenderingHints</code> 中移除相应的映射，通过
     * <code>Iterator.remove</code>、<code>Collection.remove</code>、
     * <code>removeAll</code>、<code>retainAll</code> 和
     * <code>clear</code> 操作。它不支持 <code>add</code> 或
     * <code>addAll</code> 操作。
     *
     * @return 包含在此 <code>RenderingHints</code> 中的值的 <code>Collection</code> 视图。
     */
    public Collection<Object> values() {
        return hintmap.values();
    }

    /**
     * 返回包含在此 <code>RenderingHints</code> 中的映射的 <code>Set</code> 视图。
     * 返回的 <code>Set</code> 中的每个元素都是一个 <code>Map.Entry</code>。
     * 该 <code>Set</code> 由 <code>RenderingHints</code> 支持，因此对
     * <code>RenderingHints</code> 的更改会反映在 <code>Set</code> 中，反之亦然。
     * 如果在迭代 <code>Set</code> 时修改了 <code>RenderingHints</code>，
     * 迭代的结果是未定义的。
     * <p>
     * 从 <code>RenderingHints</code> 对象返回的 entrySet 是不可修改的。
     *
     * @return 包含在此 <code>RenderingHints</code> 中的映射的 <code>Set</code> 视图。
     */
    public Set<Map.Entry<Object,Object>> entrySet() {
        return Collections.unmodifiableMap(hintmap).entrySet();
    }

    /**
     * 将指定的 <code>Object</code> 与此 <code>RenderingHints</code> 进行比较以确定是否相等。
     * 如果指定的对象也是一个 <code>Map</code> 且两个 <code>Map</code> 对象表示相同的映射，
     * 则返回 <code>true</code>。更正式地说，如果两个 <code>Map</code> 对象
     * <code>t1</code> 和 <code>t2</code> 满足 <code>t1.keySet().equals(t2.keySet())</code> 并且对于
     * <code>t1.keySet()</code> 中的每个键 <code>k</code>，
     * <pre>
     * (t1.get(k)==null ? t2.get(k)==null : t1.get(k).equals(t2.get(k)))
     * </pre>。
     * 这确保了 <code>equals</code> 方法在不同的 <code>Map</code> 接口实现之间能够正常工作。
     *
     * @param o 要与此 <code>RenderingHints</code> 比较相等性的 <code>Object</code>。
     * @return 如果指定的 <code>Object</code> 等于此 <code>RenderingHints</code>，则返回 <code>true</code>。
     */
    public boolean equals(Object o) {
        if (o instanceof RenderingHints) {
            return hintmap.equals(((RenderingHints) o).hintmap);
        } else if (o instanceof Map) {
            return hintmap.equals(o);
        }
        return false;
    }

    /**
     * 返回此 <code>RenderingHints</code> 的哈希码值。
     * <code>RenderingHints</code> 的哈希码定义为该对象的 entrySet 视图中每个 <code>Entry</code> 的哈希码之和。
     * 这确保了 <code>t1.equals(t2)</code> 意味着
     * <code>t1.hashCode()==t2.hashCode()</code> 对于任何两个 <code>Map</code> 对象
     * <code>t1</code> 和 <code>t2</code>，这是 <code>Object.hashCode</code> 的一般合同所要求的。
     *
     * @return 此 <code>RenderingHints</code> 的哈希码值。
     * @see java.util.Map.Entry#hashCode()
     * @see Object#hashCode()
     * @see Object#equals(Object)
     * @see #equals(Object)
     */
    public int hashCode() {
        return hintmap.hashCode();
    }

    /**
     * 创建与此 <code>RenderingHints</code> 对象具有相同内容的克隆。
     * @return 此实例的克隆。
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        RenderingHints rh;
        try {
            rh = (RenderingHints) super.clone();
            if (hintmap != null) {
                rh.hintmap = (HashMap<Object,Object>) hintmap.clone();
            }
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们是可克隆的
            throw new InternalError(e);
        }

        return rh;
    }

    /**
     * 返回一个较长的字符串表示形式，其中包含此 <code>RenderingHints</code> 对象中的键值映射。
     * @return  此对象的字符串表示形式。
     */
    public String toString() {
        if (hintmap == null) {
            return getClass().getName() + "@" +
                Integer.toHexString(hashCode()) +
                " (0 hints)";
        }

        return hintmap.toString();
    }
}
