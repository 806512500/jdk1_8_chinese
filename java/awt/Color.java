
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

import java.beans.ConstructorProperties;
import java.awt.image.ColorModel;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.color.ColorSpace;

/**
 * <code>Color</code> 类用于封装默认的 sRGB 颜色空间中的颜色，或由 {@link ColorSpace} 标识的任意颜色空间中的颜色。每个颜色都有一个隐式的 alpha 值 1.0 或在构造函数中显式提供的 alpha 值。alpha 值定义了颜色的透明度，可以表示为 0.0 - 1.0 或 0 - 255 范围内的浮点值。alpha 值为 1.0 或 255 表示颜色完全不透明，alpha 值为 0 或 0.0 表示颜色完全透明。在构造具有显式 alpha 的 <code>Color</code> 或获取 <code>Color</code> 的颜色/alpha 组件时，颜色组件从未被 alpha 组件预乘。
 * <p>
 * Java 2D(tm) API 的默认颜色空间是 sRGB，这是一个提议的标准 RGB 颜色空间。有关 sRGB 的更多信息，请参见 <A href="http://www.w3.org/pub/WWW/Graphics/Color/sRGB.html">
 * http://www.w3.org/pub/WWW/Graphics/Color/sRGB.html
 * </A>。
 * <p>
 * @version     10 Feb 1997
 * @author      Sami Shaio
 * @author      Arthur van Hoff
 * @see         ColorSpace
 * @see         AlphaComposite
 */
public class Color implements Paint, java.io.Serializable {

    /**
     * 白色。在默认的 sRGB 空间中。
     */
    public final static Color white     = new Color(255, 255, 255);

    /**
     * 白色。在默认的 sRGB 空间中。
     * @since 1.4
     */
    public final static Color WHITE = white;

    /**
     * 浅灰色。在默认的 sRGB 空间中。
     */
    public final static Color lightGray = new Color(192, 192, 192);

    /**
     * 浅灰色。在默认的 sRGB 空间中。
     * @since 1.4
     */
    public final static Color LIGHT_GRAY = lightGray;

    /**
     * 灰色。在默认的 sRGB 空间中。
     */
    public final static Color gray      = new Color(128, 128, 128);

    /**
     * 灰色。在默认的 sRGB 空间中。
     * @since 1.4
     */
    public final static Color GRAY = gray;

    /**
     * 深灰色。在默认的 sRGB 空间中。
     */
    public final static Color darkGray  = new Color(64, 64, 64);

    /**
     * 深灰色。在默认的 sRGB 空间中。
     * @since 1.4
     */
    public final static Color DARK_GRAY = darkGray;

    /**
     * 黑色。在默认的 sRGB 空间中。
     */
    public final static Color black     = new Color(0, 0, 0);

    /**
     * 黑色。在默认的 sRGB 空间中。
     * @since 1.4
     */
    public final static Color BLACK = black;

    /**
     * 红色。在默认的 sRGB 空间中。
     */
    public final static Color red       = new Color(255, 0, 0);

    /**
     * 红色。在默认的 sRGB 空间中。
     * @since 1.4
     */
    public final static Color RED = red;

    /**
     * 粉色。在默认的 sRGB 空间中。
     */
    public final static Color pink      = new Color(255, 175, 175);

    /**
     * 粉色。在默认的 sRGB 空间中。
     * @since 1.4
     */
    public final static Color PINK = pink;

    /**
     * 橙色。在默认的 sRGB 空间中。
     */
    public final static Color orange    = new Color(255, 200, 0);

    /**
     * 橙色。在默认的 sRGB 空间中。
     * @since 1.4
     */
    public final static Color ORANGE = orange;

    /**
     * 黄色。在默认的 sRGB 空间中。
     */
    public final static Color yellow    = new Color(255, 255, 0);

    /**
     * 黄色。在默认的 sRGB 空间中。
     * @since 1.4
     */
    public final static Color YELLOW = yellow;

    /**
     * 绿色。在默认的 sRGB 空间中。
     */
    public final static Color green     = new Color(0, 255, 0);

    /**
     * 绿色。在默认的 sRGB 空间中。
     * @since 1.4
     */
    public final static Color GREEN = green;

    /**
     * 品红色。在默认的 sRGB 空间中。
     */
    public final static Color magenta   = new Color(255, 0, 255);

    /**
     * 品红色。在默认的 sRGB 空间中。
     * @since 1.4
     */
    public final static Color MAGENTA = magenta;

    /**
     * 青色。在默认的 sRGB 空间中。
     */
    public final static Color cyan      = new Color(0, 255, 255);

    /**
     * 青色。在默认的 sRGB 空间中。
     * @since 1.4
     */
    public final static Color CYAN = cyan;

    /**
     * 蓝色。在默认的 sRGB 空间中。
     */
    public final static Color blue      = new Color(0, 0, 255);

    /**
     * 蓝色。在默认的 sRGB 空间中。
     * @since 1.4
     */
    public final static Color BLUE = blue;

    /**
     * 颜色值。
     * @serial
     * @see #getRGB
     */
    int value;

    /**
     * 默认 sRGB <code>ColorSpace</code> 中的颜色值，作为 <code>float</code> 组件（无 alpha）。
     * 如果在对象构造后为 <code>null</code>，则必须是一个使用 8 位精度构造的 sRGB 颜色，因此从 <code>int</code> 颜色值计算。
     * @serial
     * @see #getRGBColorComponents
     * @see #getRGBComponents
     */
    private float frgbvalue[] = null;

    /**
     * 本机 <code>ColorSpace</code> 中的颜色值，作为 <code>float</code> 组件（无 alpha）。
     * 如果在对象构造后为 <code>null</code>，则必须是一个使用 8 位精度构造的 sRGB 颜色，因此从 <code>int</code> 颜色值计算。
     * @serial
     * @see #getRGBColorComponents
     * @see #getRGBComponents
     */
    private float fvalue[] = null;

    /**
     * 作为 <code>float</code> 组件的 alpha 值。
     * 如果 <code>frgbvalue</code> 为 <code>null</code>，则这不是有效数据，因此从 <code>int</code> 颜色值计算。
     * @serial
     * @see #getRGBComponents
     * @see #getComponents
     */
    private float falpha = 0.0f;

    /**
     * <code>ColorSpace</code>。如果为 <code>null</code>，则默认为 sRGB。
     * @serial
     * @see #getColor
     * @see #getColorSpace
     * @see #getColorComponents
     */
    private ColorSpace cs = null;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = 118526816881161077L;

    /**
     * 初始化 JNI 字段和方法 ID
     */
    private static native void initIDs();

    static {
        /** 4112352 - 调用 getDefaultToolkit()
         ** 这里可能会导致此类在完全初始化之前被访问。不要这样做！！！
         **
         ** Toolkit.getDefaultToolkit();
         **/

        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * 检查提供的颜色整数组件的有效性。
     * 如果值超出范围，则抛出 {@link IllegalArgumentException}。
     * @param r 红色组件
     * @param g 绿色组件
     * @param b 蓝色组件
     **/
    private static void testColorValueRange(int r, int g, int b, int a) {
        boolean rangeError = false;
        String badComponentString = "";

        if ( a < 0 || a > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Alpha";
        }
        if ( r < 0 || r > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Red";
        }
        if ( g < 0 || g > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Green";
        }
        if ( b < 0 || b > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Blue";
        }
        if ( rangeError == true ) {
        throw new IllegalArgumentException("Color parameter outside of expected range:"
                                           + badComponentString);
        }
    }

    /**
     * 检查提供的颜色 <code>float</code> 组件的有效性。
     * 如果值超出范围，则抛出 <code>IllegalArgumentException</code>。
     * @param r 红色组件
     * @param g 绿色组件
     * @param b 蓝色组件
     **/
    private static void testColorValueRange(float r, float g, float b, float a) {
        boolean rangeError = false;
        String badComponentString = "";
        if ( a < 0.0 || a > 1.0) {
            rangeError = true;
            badComponentString = badComponentString + " Alpha";
        }
        if ( r < 0.0 || r > 1.0) {
            rangeError = true;
            badComponentString = badComponentString + " Red";
        }
        if ( g < 0.0 || g > 1.0) {
            rangeError = true;
            badComponentString = badComponentString + " Green";
        }
        if ( b < 0.0 || b > 1.0) {
            rangeError = true;
            badComponentString = badComponentString + " Blue";
        }
        if ( rangeError == true ) {
        throw new IllegalArgumentException("Color parameter outside of expected range:"
                                           + badComponentString);
        }
    }

    /**
     * 使用指定的红、绿、蓝值（范围 0 - 255）创建一个不透明的 sRGB 颜色。
     * 实际用于渲染的颜色取决于在给定输出设备上找到的最佳匹配颜色空间。
     * Alpha 默认为 255。
     *
     * @throws IllegalArgumentException 如果 <code>r</code>、<code>g</code>
     *        或 <code>b</code> 超出 0 到 255 的范围（包括 0 和 255）
     * @param r 红色组件
     * @param g 绿色组件
     * @param b 蓝色组件
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getRGB
     */
    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    /**
     * 使用指定的红、绿、蓝、alpha 值（范围 0 - 255）创建一个 sRGB 颜色。
     *
     * @throws IllegalArgumentException 如果 <code>r</code>、<code>g</code>、
     *        <code>b</code> 或 <code>a</code> 超出 0 到 255 的范围（包括 0 和 255）
     * @param r 红色组件
     * @param g 绿色组件
     * @param b 蓝色组件
     * @param a alpha 组件
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getAlpha
     * @see #getRGB
     */
    @ConstructorProperties({"red", "green", "blue", "alpha"})
    public Color(int r, int g, int b, int a) {
        value = ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                ((b & 0xFF) << 0);
        testColorValueRange(r,g,b,a);
    }

    /**
     * 使用指定的组合 RGB 值创建一个不透明的 sRGB 颜色，该值由红色组件在位 16-23、绿色组件在位 8-15 和蓝色组件在位 0-7 组成。实际用于渲染的颜色取决于在给定输出设备上找到的最佳匹配颜色空间。Alpha 默认为 255。
     *
     * @param rgb 组合的 RGB 组件
     * @see java.awt.image.ColorModel#getRGBdefault
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getRGB
     */
    public Color(int rgb) {
        value = 0xff000000 | rgb;
    }

    /**
     * 使用指定的组合 RGBA 值创建一个 sRGB 颜色，该值由 alpha 组件在位 24-31、红色组件在位 16-23、绿色组件在位 8-15 和蓝色组件在位 0-7 组成。如果 <code>hasalpha</code> 参数为 <code>false</code>，则 alpha 默认为 255。
     *
     * @param rgba 组合的 RGBA 组件
     * @param hasalpha <code>true</code> 表示 alpha 位有效；<code>false</code> 表示 alpha 位无效
     * @see java.awt.image.ColorModel#getRGBdefault
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getAlpha
     * @see #getRGB
     */
    public Color(int rgba, boolean hasalpha) {
        if (hasalpha) {
            value = rgba;
        } else {
            value = 0xff000000 | rgba;
        }
    }

    /**
     * 使用指定的红、绿、蓝值（范围 0.0 - 1.0）创建一个不透明的 sRGB 颜色。Alpha 默认为 1.0。实际用于渲染的颜色取决于在给定输出设备上找到的最佳匹配颜色空间。
     *
     * @throws IllegalArgumentException 如果 <code>r</code>、<code>g</code>
     *        或 <code>b</code> 超出 0.0 到 1.0 的范围（包括 0.0 和 1.0）
     * @param r 红色组件
     * @param g 绿色组件
     * @param b 蓝色组件
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getRGB
     */
    public Color(float r, float g, float b) {
        this( (int) (r*255+0.5), (int) (g*255+0.5), (int) (b*255+0.5));
        testColorValueRange(r,g,b,1.0f);
        frgbvalue = new float[3];
        frgbvalue[0] = r;
        frgbvalue[1] = g;
        frgbvalue[2] = b;
        falpha = 1.0f;
        fvalue = frgbvalue;
    }

    /**
     * 使用指定的红、绿、蓝、alpha 值（范围 0.0 - 1.0）创建一个 sRGB 颜色。实际用于渲染的颜色取决于在给定输出设备上找到的最佳匹配颜色空间。
     * @throws IllegalArgumentException 如果 <code>r</code>、<code>g</code>
     *        <code>b</code> 或 <code>a</code> 超出 0.0 到 1.0 的范围（包括 0.0 和 1.0）
     * @param r 红色组件
     * @param g 绿色组件
     * @param b 蓝色组件
     * @param a alpha 组件
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getAlpha
     * @see #getRGB
     */
    public Color(float r, float g, float b, float a) {
        this((int)(r*255+0.5), (int)(g*255+0.5), (int)(b*255+0.5), (int)(a*255+0.5));
        frgbvalue = new float[3];
        frgbvalue[0] = r;
        frgbvalue[1] = g;
        frgbvalue[2] = b;
        falpha = a;
        fvalue = frgbvalue;
    }


                /**
     * 在指定的 <code>ColorSpace</code> 中创建颜色
     * 使用 <code>float</code> 数组中指定的颜色组件和指定的 alpha。组件的数量
     * 由 <code>ColorSpace</code> 的类型决定。例如，RGB 需要 3 个组件，而 CMYK 需要 4
     * 个组件。
     * @param cspace 用于解释组件的 <code>ColorSpace</code>
     * @param components 与 <code>ColorSpace</code> 兼容的任意数量的颜色组件
     * @param alpha alpha 值
     * @throws IllegalArgumentException 如果 <code>components</code> 数组中的任何值或 <code>alpha</code> 超出 0.0 到 1.0 的范围
     * @see #getComponents
     * @see #getColorComponents
     */
    public Color(ColorSpace cspace, float components[], float alpha) {
        boolean rangeError = false;
        String badComponentString = "";
        int n = cspace.getNumComponents();
        fvalue = new float[n];
        for (int i = 0; i < n; i++) {
            if (components[i] < 0.0 || components[i] > 1.0) {
                rangeError = true;
                badComponentString = badComponentString + "Component " + i
                                     + " ";
            } else {
                fvalue[i] = components[i];
            }
        }
        if (alpha < 0.0 || alpha > 1.0) {
            rangeError = true;
            badComponentString = badComponentString + "Alpha";
        } else {
            falpha = alpha;
        }
        if (rangeError) {
            throw new IllegalArgumentException(
                "Color parameter outside of expected range: " +
                badComponentString);
        }
        frgbvalue = cspace.toRGB(fvalue);
        cs = cspace;
        value = ((((int)(falpha*255)) & 0xFF) << 24) |
                ((((int)(frgbvalue[0]*255)) & 0xFF) << 16) |
                ((((int)(frgbvalue[1]*255)) & 0xFF) << 8)  |
                ((((int)(frgbvalue[2]*255)) & 0xFF) << 0);
    }

    /**
     * 返回默认 sRGB 空间中 0-255 范围内的红色组件。
     * @return 红色组件。
     * @see #getRGB
     */
    public int getRed() {
        return (getRGB() >> 16) & 0xFF;
    }

    /**
     * 返回默认 sRGB 空间中 0-255 范围内的绿色组件。
     * @return 绿色组件。
     * @see #getRGB
     */
    public int getGreen() {
        return (getRGB() >> 8) & 0xFF;
    }

    /**
     * 返回默认 sRGB 空间中 0-255 范围内的蓝色组件。
     * @return 蓝色组件。
     * @see #getRGB
     */
    public int getBlue() {
        return (getRGB() >> 0) & 0xFF;
    }

    /**
     * 返回 0-255 范围内的 alpha 组件。
     * @return alpha 组件。
     * @see #getRGB
     */
    public int getAlpha() {
        return (getRGB() >> 24) & 0xff;
    }

    /**
     * 返回表示颜色在默认 sRGB {@link ColorModel} 中的 RGB 值。
     * (位 24-31 是 alpha，16-23 是红色，8-15 是绿色，0-7 是蓝色)。
     * @return 颜色在默认 sRGB <code>ColorModel</code> 中的 RGB 值。
     * @see java.awt.image.ColorModel#getRGBdefault
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @since JDK1.0
     */
    public int getRGB() {
        return value;
    }

    private static final double FACTOR = 0.7;

    /**
     * 创建此 <code>Color</code> 的更亮版本。
     * <p>
     * 此方法将任意缩放因子应用于此 <code>Color</code> 的三个 RGB
     * 组件，以创建此 <code>Color</code> 的更亮版本。
     * <code>alpha</code> 值保持不变。
     * 尽管 <code>brighter</code> 和
     * <code>darker</code> 是逆操作，但由于舍入误差，多次调用这两个方法的结果可能不一致。
     * @return 一个更亮版本的 <code>Color</code> 对象
     *         且具有相同的 <code>alpha</code> 值。
     * @see java.awt.Color#darker
     * @since JDK1.0
     */
    public Color brighter() {
        int r = getRed();
        int g = getGreen();
        int b = getBlue();
        int alpha = getAlpha();

        /* 从 2D 组：
         * 1. black.brighter() 应返回灰色
         * 2. 应用 brighter 到蓝色将始终返回蓝色，更亮
         * 3. 非纯色（非零 rgb）最终将返回白色
         */
        int i = (int)(1.0/(1.0-FACTOR));
        if ( r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i, alpha);
        }
        if ( r > 0 && r < i ) r = i;
        if ( g > 0 && g < i ) g = i;
        if ( b > 0 && b < i ) b = i;

        return new Color(Math.min((int)(r/FACTOR), 255),
                         Math.min((int)(g/FACTOR), 255),
                         Math.min((int)(b/FACTOR), 255),
                         alpha);
    }

    /**
     * 创建此 <code>Color</code> 的更暗版本。
     * <p>
     * 此方法将任意缩放因子应用于此 <code>Color</code> 的三个 RGB
     * 组件，以创建此 <code>Color</code> 的更暗版本。
     * <code>alpha</code> 值保持不变。
     * 尽管 <code>brighter</code> 和
     * <code>darker</code> 是逆操作，但由于舍入误差，多次调用这两个方法的结果可能不一致。
     * @return 一个更暗版本的 <code>Color</code> 对象
     *         且具有相同的 <code>alpha</code> 值。
     * @see java.awt.Color#brighter
     * @since JDK1.0
     */
    public Color darker() {
        return new Color(Math.max((int)(getRed()  *FACTOR), 0),
                         Math.max((int)(getGreen()*FACTOR), 0),
                         Math.max((int)(getBlue() *FACTOR), 0),
                         getAlpha());
    }

    /**
     * 计算此 <code>Color</code> 的哈希码。
     * @return 此对象的哈希码值。
     * @since JDK1.0
     */
    public int hashCode() {
        return value;
    }

    /**
     * 确定另一个对象是否等于此 <code>Color</code>。
     * <p>
     * 结果为 <code>true</code> 当且仅当参数不为
     * <code>null</code> 且是一个 <code>Color</code> 对象，其红色、绿色、蓝色和 alpha 值与该对象相同。
     * @param obj 要测试是否与该 <code>Color</code> 相等的对象
     * @return 如果对象相同，则为 <code>true</code>；否则为 <code>false</code>。
     * @since JDK1.0
     */
    public boolean equals(Object obj) {
        return obj instanceof Color && ((Color)obj).getRGB() == this.getRGB();
    }

    /**
     * 返回此 <code>Color</code> 的字符串表示形式。此方法仅用于调试目的。返回的字符串的内容和格式可能因实现而异。返回的字符串可能是空的，但不能为 <code>null</code>。
     *
     * @return 此 <code>Color</code> 的字符串表示形式。
     */
    public String toString() {
        return getClass().getName() + "[r=" + getRed() + ",g=" + getGreen() + ",b=" + getBlue() + "]";
    }

    /**
     * 将 <code>String</code> 转换为整数并返回指定的不透明 <code>Color</code>。此方法处理表示八进制和十六进制数的字符串格式。
     * @param nm 代表不透明颜色的 24 位整数的 <code>String</code>
     * @return 新的 <code>Color</code> 对象。
     * @see java.lang.Integer#decode
     * @exception NumberFormatException 如果指定的字符串不能被解释为十进制、八进制或十六进制整数。
     * @since JDK1.1
     */
    public static Color decode(String nm) throws NumberFormatException {
        Integer intval = Integer.decode(nm);
        int i = intval.intValue();
        return new Color((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
    }

    /**
     * 在系统属性中查找颜色。
     * <p>
     * 参数被视为要获取的系统属性的名称。该属性的字符串值然后被解释为整数，然后转换为 <code>Color</code>
     * 对象。
     * <p>
     * 如果未找到指定的属性或不能解析为整数，则返回 <code>null</code>。
     * @param nm 颜色属性的名称
     * @return 从系统属性转换的颜色。
     * @see java.lang.System#getProperty(java.lang.String)
     * @see java.lang.Integer#getInteger(java.lang.String)
     * @see java.awt.Color#Color(int)
     * @since JDK1.0
     */
    public static Color getColor(String nm) {
        return getColor(nm, null);
    }

    /**
     * 在系统属性中查找颜色。
     * <p>
     * 第一个参数被视为要获取的系统属性的名称。该属性的字符串值然后被解释为整数，然后转换为 <code>Color</code>
     * 对象。
     * <p>
     * 如果未找到指定的属性或不能解析为整数，则返回第二个参数指定的颜色。
     * @param nm 颜色属性的名称
     * @param v 默认颜色
     * @return 从系统属性转换的颜色，或指定的颜色。
     * @see java.lang.System#getProperty(java.lang.String)
     * @see java.lang.Integer#getInteger(java.lang.String)
     * @see java.awt.Color#Color(int)
     * @since JDK1.0
     */
    public static Color getColor(String nm, Color v) {
        Integer intval = Integer.getInteger(nm);
        if (intval == null) {
            return v;
        }
        int i = intval.intValue();
        return new Color((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
    }

    /**
     * 在系统属性中查找颜色。
     * <p>
     * 第一个参数被视为要获取的系统属性的名称。该属性的字符串值然后被解释为整数，然后转换为 <code>Color</code>
     * 对象。
     * <p>
     * 如果未找到指定的属性或不能解析为整数，则使用整数值 <code>v</code> 代替，并转换为 <code>Color</code> 对象。
     * @param nm 颜色属性的名称
     * @param v 默认颜色值，作为整数
     * @return 从系统属性或指定整数转换的颜色。
     * @see java.lang.System#getProperty(java.lang.String)
     * @see java.lang.Integer#getInteger(java.lang.String)
     * @see java.awt.Color#Color(int)
     * @since JDK1.0
     */
    public static Color getColor(String nm, int v) {
        Integer intval = Integer.getInteger(nm);
        int i = (intval != null) ? intval.intValue() : v;
        return new Color((i >> 16) & 0xFF, (i >> 8) & 0xFF, (i >> 0) & 0xFF);
    }

    /**
     * 将 HSB 模型中指定的颜色组件转换为默认 RGB 模型中的一组等效值。
     * <p>
     * <code>saturation</code> 和 <code>brightness</code> 组件应该是 0 到 1 之间的浮点值
     * （0.0-1.0 范围内的数字）。<code>hue</code> 组件可以是任何浮点数。从该数字中减去其整数部分以创建 0 到 1 之间的分数。然后将此分数乘以 360 以产生 HSB 颜色模型中的色调角度。
     * <p>
     * <code>HSBtoRGB</code> 返回的整数在整数值的 0-23 位中编码颜色值，格式与 {@link #getRGB() getRGB} 方法使用的格式相同。
     * 此整数可以作为参数传递给接受单个整数参数的 <code>Color</code> 构造函数。
     * @param hue 颜色的色调组件
     * @param saturation 颜色的饱和度
     * @param brightness 颜色的亮度
     * @return 具有指定色调、饱和度和亮度的颜色的 RGB 值。
     * @see java.awt.Color#getRGB()
     * @see java.awt.Color#Color(int)
     * @see java.awt.image.ColorModel#getRGBdefault()
     * @since JDK1.0
     */
    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float)Math.floor(hue)) * 6.0f;
            float f = h - (float)java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
            case 0:
                r = (int) (brightness * 255.0f + 0.5f);
                g = (int) (t * 255.0f + 0.5f);
                b = (int) (p * 255.0f + 0.5f);
                break;
            case 1:
                r = (int) (q * 255.0f + 0.5f);
                g = (int) (brightness * 255.0f + 0.5f);
                b = (int) (p * 255.0f + 0.5f);
                break;
            case 2:
                r = (int) (p * 255.0f + 0.5f);
                g = (int) (brightness * 255.0f + 0.5f);
                b = (int) (t * 255.0f + 0.5f);
                break;
            case 3:
                r = (int) (p * 255.0f + 0.5f);
                g = (int) (q * 255.0f + 0.5f);
                b = (int) (brightness * 255.0f + 0.5f);
                break;
            case 4:
                r = (int) (t * 255.0f + 0.5f);
                g = (int) (p * 255.0f + 0.5f);
                b = (int) (brightness * 255.0f + 0.5f);
                break;
            case 5:
                r = (int) (brightness * 255.0f + 0.5f);
                g = (int) (p * 255.0f + 0.5f);
                b = (int) (q * 255.0f + 0.5f);
                break;
            }
        }
        return 0xff000000 | (r << 16) | (g << 8) | (b << 0);
    }

    /**
     * 将默认 RGB 模型中指定的颜色组件转换为 HSB 模型中的色调、饱和度和亮度的一组等效值。
     * <p>
     * 如果 <code>hsbvals</code> 参数为 <code>null</code>，则分配一个新数组以返回结果。否则，该方法返回数组 <code>hsbvals</code>，并将值放入该数组中。
     * @param r 颜色的红色组件
     * @param g 颜色的绿色组件
     * @param b 颜色的蓝色组件
     * @param hsbvals 用于返回三个 HSB 值的数组，或 <code>null</code>
     * @return 包含色调、饱和度和亮度（按此顺序）的数组，这些值对应于具有指定红色、绿色和蓝色组件的颜色。
     * @see java.awt.Color#getRGB()
     * @see java.awt.Color#Color(int)
     * @see java.awt.image.ColorModel#getRGBdefault()
     * @since JDK1.0
     */
    public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
        float hue, saturation, brightness;
        if (hsbvals == null) {
            hsbvals = new float[3];
        }
        int cmax = (r > g) ? r : g;
        if (b > cmax) cmax = b;
        int cmin = (r < g) ? r : g;
        if (b < cmin) cmin = b;


                    brightness = ((float) cmax) / 255.0f;
        if (cmax != 0)
            saturation = ((float) (cmax - cmin)) / ((float) cmax);
        else
            saturation = 0;
        if (saturation == 0)
            hue = 0;
        else {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }

    /**
     * 根据指定的 HSB 颜色模型值创建一个 <code>Color</code> 对象。
     * <p>
     * <code>s</code> 和 <code>b</code> 组件应该是 0 到 1 之间的浮点值
     * （范围在 0.0 到 1.0 之间）。<code>h</code> 组件可以是任何浮点数。从这个数中减去其整数部分，
     * 以生成一个 0 到 1 之间的分数。然后将这个分数乘以 360 以产生 HSB 颜色模型中的色相角度。
     * @param  h   色相组件
     * @param  s   颜色的饱和度
     * @param  b   颜色的亮度
     * @return  一个具有指定色相、饱和度和亮度的 <code>Color</code> 对象。
     * @since   JDK1.0
     */
    public static Color getHSBColor(float h, float s, float b) {
        return new Color(HSBtoRGB(h, s, b));
    }

    /**
     * 返回一个包含 <code>Color</code> 的颜色和 alpha 组件的 <code>float</code> 数组，
     * 以默认的 sRGB 颜色空间表示。
     * 如果 <code>compArray</code> 为 <code>null</code>，则创建一个长度为 4 的数组作为返回值。
     * 否则，<code>compArray</code> 必须具有 4 或更大的长度，并且它将被填充组件并返回。
     * @param compArray 一个数组，此方法将用颜色和 alpha 组件填充并返回
     * @return 一个包含 RGBA 组件的 <code>float</code> 数组。
     */
    public float[] getRGBComponents(float[] compArray) {
        float[] f;
        if (compArray == null) {
            f = new float[4];
        } else {
            f = compArray;
        }
        if (frgbvalue == null) {
            f[0] = ((float)getRed())/255f;
            f[1] = ((float)getGreen())/255f;
            f[2] = ((float)getBlue())/255f;
            f[3] = ((float)getAlpha())/255f;
        } else {
            f[0] = frgbvalue[0];
            f[1] = frgbvalue[1];
            f[2] = frgbvalue[2];
            f[3] = falpha;
        }
        return f;
    }

    /**
     * 返回一个包含 <code>Color</code> 的颜色组件的 <code>float</code> 数组，
     * 以默认的 sRGB 颜色空间表示。如果 <code>compArray</code> 为 <code>null</code>，
     * 则创建一个长度为 3 的数组作为返回值。否则，<code>compArray</code> 必须具有 3 或更大的长度，
     * 并且它将被填充组件并返回。
     * @param compArray 一个数组，此方法将用颜色组件填充并返回
     * @return 一个包含 RGB 组件的 <code>float</code> 数组。
     */
    public float[] getRGBColorComponents(float[] compArray) {
        float[] f;
        if (compArray == null) {
            f = new float[3];
        } else {
            f = compArray;
        }
        if (frgbvalue == null) {
            f[0] = ((float)getRed())/255f;
            f[1] = ((float)getGreen())/255f;
            f[2] = ((float)getBlue())/255f;
        } else {
            f[0] = frgbvalue[0];
            f[1] = frgbvalue[1];
            f[2] = frgbvalue[2];
        }
        return f;
    }

    /**
     * 返回一个包含 <code>Color</code> 的颜色和 alpha 组件的 <code>float</code> 数组，
     * 以 <code>Color</code> 的 <code>ColorSpace</code> 表示。
     * 如果 <code>compArray</code> 为 <code>null</code>，则创建一个长度等于关联的
     * <code>ColorSpace</code> 组件数加一的数组作为返回值。否则，<code>compArray</code> 必须具有至少这个长度，
     * 并且它将被填充组件并返回。
     * @param compArray 一个数组，此方法将用此 <code>Color</code> 在其 <code>ColorSpace</code> 中的颜色和 alpha 组件填充并返回
     * @return 一个包含颜色和 alpha 组件的 <code>float</code> 数组。
     */
    public float[] getComponents(float[] compArray) {
        if (fvalue == null)
            return getRGBComponents(compArray);
        float[] f;
        int n = fvalue.length;
        if (compArray == null) {
            f = new float[n + 1];
        } else {
            f = compArray;
        }
        for (int i = 0; i < n; i++) {
            f[i] = fvalue[i];
        }
        f[n] = falpha;
        return f;
    }

    /**
     * 返回一个包含 <code>Color</code> 的颜色组件的 <code>float</code> 数组，
     * 以 <code>Color</code> 的 <code>ColorSpace</code> 表示。
     * 如果 <code>compArray</code> 为 <code>null</code>，则创建一个长度等于关联的
     * <code>ColorSpace</code> 组件数的数组作为返回值。否则，<code>compArray</code> 必须具有至少这个长度，
     * 并且它将被填充组件并返回。
     * @param compArray 一个数组，此方法将用此 <code>Color</code> 在其 <code>ColorSpace</code> 中的颜色组件填充并返回
     * @return 一个包含颜色组件的 <code>float</code> 数组。
     */
    public float[] getColorComponents(float[] compArray) {
        if (fvalue == null)
            return getRGBColorComponents(compArray);
        float[] f;
        int n = fvalue.length;
        if (compArray == null) {
            f = new float[n];
        } else {
            f = compArray;
        }
        for (int i = 0; i < n; i++) {
            f[i] = fvalue[i];
        }
        return f;
    }

    /**
     * 返回一个包含 <code>Color</code> 的颜色和 alpha 组件的 <code>float</code> 数组，
     * 以 <code>cspace</code> 参数指定的 <code>ColorSpace</code> 表示。
     * 如果 <code>compArray</code> 为 <code>null</code>，则创建一个长度等于
     * <code>cspace</code> 组件数加一的数组作为返回值。否则，<code>compArray</code> 必须具有至少这个长度，
     * 并且它将被填充组件并返回。
     * @param cspace 指定的 <code>ColorSpace</code>
     * @param compArray 一个数组，此方法将用此 <code>Color</code> 在指定的 <code>ColorSpace</code> 中的颜色和 alpha 组件填充并返回
     * @return 一个包含颜色和 alpha 组件的 <code>float</code> 数组。
     */
    public float[] getComponents(ColorSpace cspace, float[] compArray) {
        if (cs == null) {
            cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        }
        float f[];
        if (fvalue == null) {
            f = new float[3];
            f[0] = ((float)getRed())/255f;
            f[1] = ((float)getGreen())/255f;
            f[2] = ((float)getBlue())/255f;
        } else {
            f = fvalue;
        }
        float tmp[] = cs.toCIEXYZ(f);
        float tmpout[] = cspace.fromCIEXYZ(tmp);
        if (compArray == null) {
            compArray = new float[tmpout.length + 1];
        }
        for (int i = 0 ; i < tmpout.length ; i++) {
            compArray[i] = tmpout[i];
        }
        if (fvalue == null) {
            compArray[tmpout.length] = ((float)getAlpha())/255f;
        } else {
            compArray[tmpout.length] = falpha;
        }
        return compArray;
    }

    /**
     * 返回一个包含 <code>Color</code> 的颜色组件的 <code>float</code> 数组，
     * 以 <code>cspace</code> 参数指定的 <code>ColorSpace</code> 表示。
     * 如果 <code>compArray</code> 为 <code>null</code>，则创建一个长度等于
     * <code>cspace</code> 组件数的数组作为返回值。否则，<code>compArray</code> 必须具有至少这个长度，
     * 并且它将被填充组件并返回。
     * @param cspace 指定的 <code>ColorSpace</code>
     * @param compArray 一个数组，此方法将用此 <code>Color</code> 在指定的 <code>ColorSpace</code> 中的颜色组件填充
     * @return 一个包含颜色组件的 <code>float</code> 数组。
     */
    public float[] getColorComponents(ColorSpace cspace, float[] compArray) {
        if (cs == null) {
            cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        }
        float f[];
        if (fvalue == null) {
            f = new float[3];
            f[0] = ((float)getRed())/255f;
            f[1] = ((float)getGreen())/255f;
            f[2] = ((float)getBlue())/255f;
        } else {
            f = fvalue;
        }
        float tmp[] = cs.toCIEXYZ(f);
        float tmpout[] = cspace.fromCIEXYZ(tmp);
        if (compArray == null) {
            return tmpout;
        }
        for (int i = 0 ; i < tmpout.length ; i++) {
            compArray[i] = tmpout[i];
        }
        return compArray;
    }

    /**
     * 返回此 <code>Color</code> 的 <code>ColorSpace</code>。
     * @return 此 <code>Color</code> 对象的 <code>ColorSpace</code>。
     */
    public ColorSpace getColorSpace() {
        if (cs == null) {
            cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        }
        return cs;
    }

    /**
     * 创建并返回一个用于生成纯色场图案的 {@link PaintContext}。
     * 有关 null 参数处理的信息，请参见 {@link Paint#createContext} 方法在 {@link Paint} 接口中的说明。
     *
     * @param cm 一个表示调用者接收像素数据最方便格式的首选 {@link ColorModel}，或 {@code null}
     *           如果没有偏好。
     * @param r 正在渲染的图形基元的设备空间边界框。
     * @param r2d 正在渲染的图形基元的用户空间边界框。
     * @param xform 从用户空间到设备空间的 {@link AffineTransform}。
     * @param hints 一个提示集，上下文对象可以使用这些提示在渲染选项之间进行选择。
     * @return 用于生成颜色图案的 {@code PaintContext}。
     * @see Paint
     * @see PaintContext
     * @see ColorModel
     * @see Rectangle
     * @see Rectangle2D
     * @see AffineTransform
     * @see RenderingHints
     */
    public synchronized PaintContext createContext(ColorModel cm, Rectangle r,
                                                   Rectangle2D r2d,
                                                   AffineTransform xform,
                                                   RenderingHints hints) {
        return new ColorPaintContext(getRGB(), cm);
    }

    /**
     * 返回此 <code>Color</code> 的透明度模式。这是实现 <code>Paint</code> 接口所必需的。
     * @return 此 <code>Color</code> 对象的透明度模式。
     * @see Paint
     * @see Transparency
     * @see #createContext
     */
    public int getTransparency() {
        int alpha = getAlpha();
        if (alpha == 0xff) {
            return Transparency.OPAQUE;
        }
        else if (alpha == 0) {
            return Transparency.BITMASK;
        }
        else {
            return Transparency.TRANSLUCENT;
        }
    }

}
