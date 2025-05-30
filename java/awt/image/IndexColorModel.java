
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

package java.awt.image;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.math.BigInteger;

/**
 * <code>IndexColorModel</code> 类是一个 <code>ColorModel</code> 类，用于处理像素值由单个样本组成的颜色模型，该样本是默认 sRGB 颜色空间中固定颜色表的索引。颜色表指定了每个索引对应的红色、绿色、蓝色和可选的 alpha 分量。所有分量在颜色表中都表示为 8 位无符号整数值。某些构造函数允许调用者通过在 <code>BigInteger</code> 对象中设置位来指定颜色表中的“空洞”，即哪些颜色表条目是有效的，哪些表示不可用的颜色。此颜色模型类似于 X11 PseudoColor 视觉。
 * <p>
 * 一些构造函数提供了指定颜色表中每个像素的 alpha 分量的手段，而其他构造函数要么不提供这样的手段，要么在某些情况下提供一个标志来指示颜色表数据是否包含 alpha 值。如果构造函数中没有提供 alpha 值，则假定每个条目的 alpha 分量为不透明（alpha = 1.0）。可以提供一个可选的透明像素值，该值指示一个完全透明的像素，无论为该像素值提供的或假定的 alpha 分量如何。
 * 请注意，<code>IndexColorModel</code> 对象的颜色表中的颜色分量从未与 alpha 分量预乘。
 * <p>
 * <a name="transparency">
 * <code>IndexColorModel</code> 对象的透明度通过检查颜色表中颜色的 alpha 分量并考虑可选的 alpha 值和任何指定的透明索引来确定。最具体的值将被选择。如果颜色表中所有有效的颜色都是不透明的，并且没有有效的透明像素，则透明度值为 <code>Transparency.OPAQUE</code>。
 * 如果颜色表中所有有效的颜色要么完全不透明（alpha = 1.0），要么完全透明（alpha = 0.0），这通常发生在指定了有效的透明像素时，透明度值为 <code>Transparency.BITMASK</code>。
 * 否则，透明度值为 <code>Transparency.TRANSLUCENT</code>，表示某些有效的颜色具有既不是完全透明也不是完全不透明的 alpha 分量（0.0 &lt; alpha &lt; 1.0）。
 * </a>
 *
 * <p>
 * 如果 <code>IndexColorModel</code> 对象的透明度值为 <code>Transparency.OPAQUE</code>，则 <code>hasAlpha</code> 和 <code>getNumComponents</code> 方法（均继承自 <code>ColorModel</code>）分别返回 false 和 3。
 * 对于任何其他透明度值，<code>hasAlpha</code> 返回 true，<code>getNumComponents</code> 返回 4。
 *
 * <p>
 * <a name="index_values">
 * 用于索引颜色表的值取自像素表示的最低 <em>n</em> 位，其中 <em>n</em> 基于构造函数中指定的像素大小。对于小于 8 位的像素大小，<em>n</em> 被四舍五入到 2 的幂（3 变成 4，5、6、7 变成 8）。
 * 对于 8 到 16 位之间的像素大小，<em>n</em> 等于像素大小。大于 16 位的像素大小不受此类支持。超过 <em>n</em> 位的较高位在像素表示中被忽略。大于或等于颜色表大小但小于 2<sup><em>n</em></sup> 的索引值是未定义的，并且返回所有颜色和 alpha 分量为 0。
 * </a>
 * <p>
 * 对于使用类型为 <code>transferType</code> 的原始数组像素表示的那些方法，数组长度始终为 1。支持的传输类型是 <code>DataBuffer.TYPE_BYTE</code> 和 <code>DataBuffer.TYPE_USHORT</code>。对于此类的所有对象，单个 int 像素表示始终有效，因为总是可以使用单个 int 表示与此类一起使用的像素值。因此，使用此表示的方法不会因无效的像素值而抛出 <code>IllegalArgumentException</code>。
 * <p>
 * 本类中的许多方法都是 final 的。原因是底层的原生图形代码对本类的布局和操作做出了假设，这些假设反映在标记为 final 的方法的实现中。您可以为此类创建子类，但不能覆盖或修改这些方法的行为。
 *
 * @see ColorModel
 * @see ColorSpace
 * @see DataBuffer
 *
 */
public class IndexColorModel extends ColorModel {
    private int rgb[];
    private int map_size;
    private int pixel_mask;
    private int transparent_index = -1;
    private boolean allgrayopaque;
    private BigInteger validBits;

    private sun.awt.image.BufImgSurfaceData.ICMColorData colorData = null;

    private static int[] opaqueBits = {8, 8, 8};
    private static int[] alphaBits = {8, 8, 8, 8};

    static private native void initIDs();
    static {
        ColorModel.loadLibraries();
        initIDs();
    }
    /**
     * 从指定的红色、绿色和蓝色分量数组构造一个 <code>IndexColorModel</code>。此颜色模型描述的所有像素的 alpha 分量均为 255（未归一化，1.0 归一化），这意味着它们是完全不透明的。所有指定颜色分量的数组都必须至少有指定数量的条目。颜色空间是默认的 sRGB 空间。由于此构造函数的任何参数中都没有 alpha 信息，因此透明度值始终为 <code>Transparency.OPAQUE</code>。传输类型是可以容纳单个像素的最小的 <code>DataBuffer.TYPE_BYTE</code> 或 <code>DataBuffer.TYPE_USHORT</code>。
     * @param bits      每个像素占用的位数
     * @param size      颜色分量数组的大小
     * @param r         红色颜色分量数组
     * @param g         绿色颜色分量数组
     * @param b         蓝色颜色分量数组
     * @throws IllegalArgumentException 如果 <code>bits</code> 小于 1 或大于 16
     * @throws IllegalArgumentException 如果 <code>size</code> 小于 1
     */
    public IndexColorModel(int bits, int size,
                           byte r[], byte g[], byte b[]) {
        super(bits, opaqueBits,
              ColorSpace.getInstance(ColorSpace.CS_sRGB),
              false, false, OPAQUE,
              ColorModel.getDefaultTransferType(bits));
        if (bits < 1 || bits > 16) {
            throw new IllegalArgumentException("Number of bits must be between"
                                               +" 1 and 16.");
        }
        setRGBs(size, r, g, b, null);
        calculatePixelMask();
    }

    /**
     * 从给定的红色、绿色和蓝色分量数组构造一个 <code>IndexColorModel</code>。此颜色模型描述的所有像素的 alpha 分量均为 255（未归一化，1.0 归一化），这意味着它们是完全不透明的，除了指定的透明像素。所有指定颜色分量的数组都必须至少有指定数量的条目。颜色空间是默认的 sRGB 空间。透明度值可能是 <code>Transparency.OPAQUE</code> 或 <code>Transparency.BITMASK</code>，具体取决于参数，如上所述的 <a href="#transparency">类描述</a>。传输类型是可以容纳单个像素的最小的 <code>DataBuffer.TYPE_BYTE</code> 或 <code>DataBuffer.TYPE_USHORT</code>。
     * @param bits      每个像素占用的位数
     * @param size      颜色分量数组的大小
     * @param r         红色颜色分量数组
     * @param g         绿色颜色分量数组
     * @param b         蓝色颜色分量数组
     * @param trans     透明像素的索引
     * @throws IllegalArgumentException 如果 <code>bits</code> 小于 1 或大于 16
     * @throws IllegalArgumentException 如果 <code>size</code> 小于 1
     */
    public IndexColorModel(int bits, int size,
                           byte r[], byte g[], byte b[], int trans) {
        super(bits, opaqueBits,
              ColorSpace.getInstance(ColorSpace.CS_sRGB),
              false, false, OPAQUE,
              ColorModel.getDefaultTransferType(bits));
        if (bits < 1 || bits > 16) {
            throw new IllegalArgumentException("Number of bits must be between"
                                               +" 1 and 16.");
        }
        setRGBs(size, r, g, b, null);
        setTransparentPixel(trans);
        calculatePixelMask();
    }

    /**
     * 从给定的红色、绿色、蓝色和 alpha 分量数组构造一个 <code>IndexColorModel</code>。所有指定分量的数组都必须至少有指定数量的条目。颜色空间是默认的 sRGB 空间。透明度值可能是 <code>Transparency.OPAQUE</code>、<code>Transparency.BITMASK</code> 或 <code>Transparency.TRANSLUCENT</code>，具体取决于参数，如上所述的 <a href="#transparency">类描述</a>。传输类型是可以容纳单个像素的最小的 <code>DataBuffer.TYPE_BYTE</code> 或 <code>DataBuffer.TYPE_USHORT</code>。
     * @param bits      每个像素占用的位数
     * @param size      颜色分量数组的大小
     * @param r         红色颜色分量数组
     * @param g         绿色颜色分量数组
     * @param b         蓝色颜色分量数组
     * @param a         alpha 值分量数组
     * @throws IllegalArgumentException 如果 <code>bits</code> 小于 1 或大于 16
     * @throws IllegalArgumentException 如果 <code>size</code> 小于 1
     */
    public IndexColorModel(int bits, int size,
                           byte r[], byte g[], byte b[], byte a[]) {
        super (bits, alphaBits,
               ColorSpace.getInstance(ColorSpace.CS_sRGB),
               true, false, TRANSLUCENT,
               ColorModel.getDefaultTransferType(bits));
        if (bits < 1 || bits > 16) {
            throw new IllegalArgumentException("Number of bits must be between"
                                               +" 1 and 16.");
        }
        setRGBs (size, r, g, b, a);
        calculatePixelMask();
    }

    /**
     * 从单个包含交错的红色、绿色、蓝色和可选 alpha 分量的数组构造一个 <code>IndexColorModel</code>。数组必须有足够的值来填充指定大小的所有所需分量数组。颜色空间是默认的 sRGB 空间。透明度值可能是 <code>Transparency.OPAQUE</code>、<code>Transparency.BITMASK</code> 或 <code>Transparency.TRANSLUCENT</code>，具体取决于参数，如上所述的 <a href="#transparency">类描述</a>。传输类型是可以容纳单个像素的最小的 <code>DataBuffer.TYPE_BYTE</code> 或 <code>DataBuffer.TYPE_USHORT</code>。
     *
     * @param bits      每个像素占用的位数
     * @param size      颜色分量数组的大小
     * @param cmap      颜色分量数组
     * @param start     第一个颜色分量的起始偏移量
     * @param hasalpha  指示 <code>cmap</code> 数组中是否包含 alpha 值
     * @throws IllegalArgumentException 如果 <code>bits</code> 小于 1 或大于 16
     * @throws IllegalArgumentException 如果 <code>size</code> 小于 1
     */
    public IndexColorModel(int bits, int size, byte cmap[], int start,
                           boolean hasalpha) {
        this(bits, size, cmap, start, hasalpha, -1);
        if (bits < 1 || bits > 16) {
            throw new IllegalArgumentException("Number of bits must be between"
                                               +" 1 and 16.");
        }
    }

    /**
     * 从单个包含交错的红色、绿色、蓝色和可选 alpha 分量的数组构造一个 <code>IndexColorModel</code>。指定的透明索引表示一个完全透明的像素，无论为其指定的 alpha 值如何。数组必须有足够的值来填充指定大小的所有所需分量数组。颜色空间是默认的 sRGB 空间。透明度值可能是 <code>Transparency.OPAQUE</code>、<code>Transparency.BITMASK</code> 或 <code>Transparency.TRANSLUCENT</code>，具体取决于参数，如上所述的 <a href="#transparency">类描述</a>。传输类型是可以容纳单个像素的最小的 <code>DataBuffer.TYPE_BYTE</code> 或 <code>DataBuffer.TYPE_USHORT</code>。
     * @param bits      每个像素占用的位数
     * @param size      颜色分量数组的大小
     * @param cmap      颜色分量数组
     * @param start     第一个颜色分量的起始偏移量
     * @param hasalpha  指示 <code>cmap</code> 数组中是否包含 alpha 值
     * @param trans     完全透明像素的索引
     * @throws IllegalArgumentException 如果 <code>bits</code> 小于 1 或大于 16
     * @throws IllegalArgumentException 如果 <code>size</code> 小于 1
     */
    public IndexColorModel(int bits, int size, byte cmap[], int start,
                           boolean hasalpha, int trans) {
        // REMIND: This assumes the ordering: RGB[A]
        super(bits, opaqueBits,
              ColorSpace.getInstance(ColorSpace.CS_sRGB),
              false, false, OPAQUE,
              ColorModel.getDefaultTransferType(bits));


                    if (bits < 1 || bits > 16) {
            throw new IllegalArgumentException("位数必须在 1 到 16 之间。");
        }
        if (size < 1) {
            throw new IllegalArgumentException("映射大小 (" + size +
                                               ") 必须 >= 1");
        }
        map_size = size;
        rgb = new int[calcRealMapSize(bits, size)];
        int j = start;
        int alpha = 0xff;
        boolean allgray = true;
        int transparency = OPAQUE;
        for (int i = 0; i < size; i++) {
            int r = cmap[j++] & 0xff;
            int g = cmap[j++] & 0xff;
            int b = cmap[j++] & 0xff;
            allgray = allgray && (r == g) && (g == b);
            if (hasalpha) {
                alpha = cmap[j++] & 0xff;
                if (alpha != 0xff) {
                    if (alpha == 0x00) {
                        if (transparency == OPAQUE) {
                            transparency = BITMASK;
                        }
                        if (transparent_index < 0) {
                            transparent_index = i;
                        }
                    } else {
                        transparency = TRANSLUCENT;
                    }
                    allgray = false;
                }
            }
            rgb[i] = (alpha << 24) | (r << 16) | (g << 8) | b;
        }
        this.allgrayopaque = allgray;
        setTransparency(transparency);
        setTransparentPixel(trans);
        calculatePixelMask();
    }

    /**
     * 从一个整数数组构造一个 <code>IndexColorModel</code>，其中每个整数由红、绿、蓝和
     * 可选的 alpha 组件组成，默认的 RGB 颜色模型格式。指定的透明索引表示一个完全透明的像素，
     * 无论为其指定的 alpha 值如何。数组必须有足够的值来填充指定大小的所有所需组件数组。
     * <code>ColorSpace</code> 是默认的 sRGB 空间。透明度值可以是 <code>Transparency.OPAQUE</code>、
     * <code>Transparency.BITMASK</code> 或 <code>Transparency.TRANSLUCENT</code>，
     * 具体取决于参数，如类描述中所述。
     * @param bits 每个像素占用的位数
     * @param size 颜色组件数组的大小
     * @param cmap 颜色组件数组
     * @param start 第一个颜色组件的起始偏移量
     * @param hasalpha 表示 <code>cmap</code> 数组中是否包含 alpha 值
     * @param trans 完全透明像素的索引
     * @param transferType 用于表示像素值的数组的数据类型。数据类型必须是
     *           <code>DataBuffer.TYPE_BYTE</code> 或 <code>DataBuffer.TYPE_USHORT</code>。
     * @throws IllegalArgumentException 如果 <code>bits</code> 小于 1 或大于 16
     * @throws IllegalArgumentException 如果 <code>size</code> 小于 1
     * @throws IllegalArgumentException 如果 <code>transferType</code> 不是
     *           <code>DataBuffer.TYPE_BYTE</code> 或 <code>DataBuffer.TYPE_USHORT</code>
     */
    public IndexColorModel(int bits, int size,
                           int cmap[], int start,
                           boolean hasalpha, int trans, int transferType) {
        // REMIND: This assumes the ordering: RGB[A]
        super(bits, opaqueBits,
              ColorSpace.getInstance(ColorSpace.CS_sRGB),
              false, false, OPAQUE,
              transferType);

        if (bits < 1 || bits > 16) {
            throw new IllegalArgumentException("位数必须在 1 到 16 之间。");
        }
        if (size < 1) {
            throw new IllegalArgumentException("映射大小 (" + size +
                                               ") 必须 >= 1");
        }
        if ((transferType != DataBuffer.TYPE_BYTE) &&
            (transferType != DataBuffer.TYPE_USHORT)) {
            throw new IllegalArgumentException("transferType 必须是" +
                "DataBuffer.TYPE_BYTE 或 DataBuffer.TYPE_USHORT");
        }

        setRGBs(size, cmap, start, hasalpha);
        setTransparentPixel(trans);
        calculatePixelMask();
    }

    /**
     * 从一个 <code>int</code> 数组构造一个 <code>IndexColorModel</code>，其中每个 <code>int</code>
     * 由红、绿、蓝和 alpha 组件组成，默认的 RGB 颜色模型格式。数组必须有足够的值来填充指定大小的所有
     * 所需组件数组。<code>ColorSpace</code> 是默认的 sRGB 空间。透明度值可以是 <code>Transparency.OPAQUE</code>、
     * <code>Transparency.BITMASK</code> 或 <code>Transparency.TRANSLUCENT</code>，
     * 具体取决于参数，如类描述中所述。传输类型必须是 <code>DataBuffer.TYPE_BYTE</code>
     * 或 <code>DataBuffer.TYPE_USHORT</code>。<code>BigInteger</code> 对象指定 <code>cmap</code> 数组中
     * 有效/无效的像素。如果 <code>BigInteger</code> 值在该索引处设置，则该像素有效；如果 <code>BigInteger</code>
     * 位在该索引处未设置，则该像素无效。
     * @param bits 每个像素占用的位数
     * @param size 颜色组件数组的大小
     * @param cmap 颜色组件数组
     * @param start 第一个颜色组件的起始偏移量
     * @param transferType 指定的数据类型
     * @param validBits 一个 <code>BigInteger</code> 对象。如果 BigInteger 中的位设置，
     *    则该索引处的像素有效。如果位未设置，则该索引处的像素无效。如果为 null，则所有像素有效。
     *    只考虑 0 到映射大小之间的位。
     * @throws IllegalArgumentException 如果 <code>bits</code> 小于 1 或大于 16
     * @throws IllegalArgumentException 如果 <code>size</code> 小于 1
     * @throws IllegalArgumentException 如果 <code>transferType</code> 不是
     *           <code>DataBuffer.TYPE_BYTE</code> 或 <code>DataBuffer.TYPE_USHORT</code>
     *
     * @since 1.3
     */
    public IndexColorModel(int bits, int size, int cmap[], int start,
                           int transferType, BigInteger validBits) {
        super (bits, alphaBits,
               ColorSpace.getInstance(ColorSpace.CS_sRGB),
               true, false, TRANSLUCENT,
               transferType);

        if (bits < 1 || bits > 16) {
            throw new IllegalArgumentException("位数必须在 1 到 16 之间。");
        }
        if (size < 1) {
            throw new IllegalArgumentException("映射大小 (" + size +
                                               ") 必须 >= 1");
        }
        if ((transferType != DataBuffer.TYPE_BYTE) &&
            (transferType != DataBuffer.TYPE_USHORT)) {
            throw new IllegalArgumentException("transferType 必须是" +
                "DataBuffer.TYPE_BYTE 或 DataBuffer.TYPE_USHORT");
        }

        if (validBits != null) {
            // 检查是否全部有效
            for (int i = 0; i < size; i++) {
                if (!validBits.testBit(i)) {
                    this.validBits = validBits;
                    break;
                }
            }
        }

        setRGBs(size, cmap, start, true);
        calculatePixelMask();
    }

    private void setRGBs(int size, byte r[], byte g[], byte b[], byte a[]) {
        if (size < 1) {
            throw new IllegalArgumentException("映射大小 (" + size +
                                               ") 必须 >= 1");
        }
        map_size = size;
        rgb = new int[calcRealMapSize(pixel_bits, size)];
        int alpha = 0xff;
        int transparency = OPAQUE;
        boolean allgray = true;
        for (int i = 0; i < size; i++) {
            int rc = r[i] & 0xff;
            int gc = g[i] & 0xff;
            int bc = b[i] & 0xff;
            allgray = allgray && (rc == gc) && (gc == bc);
            if (a != null) {
                alpha = a[i] & 0xff;
                if (alpha != 0xff) {
                    if (alpha == 0x00) {
                        if (transparency == OPAQUE) {
                            transparency = BITMASK;
                        }
                        if (transparent_index < 0) {
                            transparent_index = i;
                        }
                    } else {
                        transparency = TRANSLUCENT;
                    }
                    allgray = false;
                }
            }
            rgb[i] = (alpha << 24) | (rc << 16) | (gc << 8) | bc;
        }
        this.allgrayopaque = allgray;
        setTransparency(transparency);
    }

    private void setRGBs(int size, int cmap[], int start, boolean hasalpha) {
        map_size = size;
        rgb = new int[calcRealMapSize(pixel_bits, size)];
        int j = start;
        int transparency = OPAQUE;
        boolean allgray = true;
        BigInteger validBits = this.validBits;
        for (int i = 0; i < size; i++, j++) {
            if (validBits != null && !validBits.testBit(i)) {
                continue;
            }
            int cmaprgb = cmap[j];
            int r = (cmaprgb >> 16) & 0xff;
            int g = (cmaprgb >>  8) & 0xff;
            int b = (cmaprgb      ) & 0xff;
            allgray = allgray && (r == g) && (g == b);
            if (hasalpha) {
                int alpha = cmaprgb >>> 24;
                if (alpha != 0xff) {
                    if (alpha == 0x00) {
                        if (transparency == OPAQUE) {
                            transparency = BITMASK;
                        }
                        if (transparent_index < 0) {
                            transparent_index = i;
                        }
                    } else {
                        transparency = TRANSLUCENT;
                    }
                    allgray = false;
                }
            } else {
                cmaprgb |= 0xff000000;
            }
            rgb[i] = cmaprgb;
        }
        this.allgrayopaque = allgray;
        setTransparency(transparency);
    }

    private int calcRealMapSize(int bits, int size) {
        int newSize = Math.max(1 << bits, size);
        return Math.max(newSize, 256);
    }

    private BigInteger getAllValid() {
        int numbytes = (map_size + 7) / 8;
        byte[] valid = new byte[numbytes];
        java.util.Arrays.fill(valid, (byte) 0xff);
        valid[0] = (byte) (0xff >>> (numbytes * 8 - map_size));

        return new BigInteger(1, valid);
    }

    /**
     * 返回透明度。返回 OPAQUE、BITMASK 或 TRANSLUCENT。
     * @return 此 <code>IndexColorModel</code> 的透明度
     * @see Transparency#OPAQUE
     * @see Transparency#BITMASK
     * @see Transparency#TRANSLUCENT
     */
    public int getTransparency() {
        return transparency;
    }

    /**
     * 返回每个颜色/alpha 组件的位数数组。数组包含红色、绿色、蓝色组件，按此顺序排列，
     * 后跟 alpha 组件（如果存在）。
     * @return 包含此 <code>IndexColorModel</code> 的每个颜色和 alpha 组件的位数的数组
     */
    public int[] getComponentSize() {
        if (nBits == null) {
            if (supportsAlpha) {
                nBits = new int[4];
                nBits[3] = 8;
            }
            else {
                nBits = new int[3];
            }
            nBits[0] = nBits[1] = nBits[2] = 8;
        }
        return nBits.clone();
    }

    /**
     * 返回此 <code>IndexColorModel</code> 中颜色/alpha 组件数组的大小。
     * @return 颜色和 alpha 组件数组的大小。
     */
    final public int getMapSize() {
        return map_size;
    }

    /**
     * 返回此 <code>IndexColorModel</code> 中透明像素的索引，或 -1（如果没有任何像素的 alpha 值为 0）。
     * 如果在构造函数中通过索引显式指定了透明像素，则优先返回该索引，否则，可能返回任何完全透明的像素的索引。
     * @return 此 <code>IndexColorModel</code> 对象中透明像素的索引，或 -1（如果不存在这样的像素）
     */
    final public int getTransparentPixel() {
        return transparent_index;
    }

    /**
     * 将红色颜色组件数组复制到指定数组中。仅写入由
     * {@link #getMapSize() getMapSize} 指定的数组的初始条目。
     * @param r 要复制红色颜色组件数组元素的指定数组
     */
    final public void getReds(byte r[]) {
        for (int i = 0; i < map_size; i++) {
            r[i] = (byte) (rgb[i] >> 16);
        }
    }

    /**
     * 将绿色颜色组件数组复制到指定数组中。仅写入由
     * <code>getMapSize</code> 指定的数组的初始条目。
     * @param g 要复制绿色颜色组件数组元素的指定数组
     */
    final public void getGreens(byte g[]) {
        for (int i = 0; i < map_size; i++) {
            g[i] = (byte) (rgb[i] >> 8);
        }
    }

    /**
     * 将蓝色颜色组件数组复制到指定数组中。仅写入由
     * <code>getMapSize</code> 指定的数组的初始条目。
     * @param b 要复制蓝色颜色组件数组元素的指定数组
     */
    final public void getBlues(byte b[]) {
        for (int i = 0; i < map_size; i++) {
            b[i] = (byte) rgb[i];
        }
    }

    /**
     * 将颜色和 alpha 组件数组中的每个索引的数据转换为默认 RGB ColorModel 格式的整数，
     * 并将生成的 32 位 ARGB 值复制到指定数组中。仅写入由
     * <code>getMapSize</code> 指定的数组的初始条目。
     * @param rgb 要复制从此颜色和 alpha 组件数组转换的 ARGB 值的指定数组。
     */
    final public void getRGBs(int rgb[]) {
        System.arraycopy(this.rgb, 0, rgb, 0, map_size);
    }


                private void setTransparentPixel(int trans) {
        if (trans >= 0 && trans < map_size) {
            rgb[trans] &= 0x00ffffff;
            transparent_index = trans;
            allgrayopaque = false;
            if (this.transparency == OPAQUE) {
                setTransparency(BITMASK);
            }
        }
    }

    private void setTransparency(int transparency) {
        if (this.transparency != transparency) {
            this.transparency = transparency;
            if (transparency == OPAQUE) {
                supportsAlpha = false;
                numComponents = 3;
                nBits = opaqueBits;
            } else {
                supportsAlpha = true;
                numComponents = 4;
                nBits = alphaBits;
            }
        }
    }

    /**
     * 从构造函数中调用此方法以设置 pixel_mask 值，该值基于 pixel_bits 的值。pixel_mask
     * 值用于屏蔽 getRed()、getGreen()、getBlue()、getAlpha() 和 getRGB() 等方法的像素参数。
     */
    private final void calculatePixelMask() {
        // 注意，我们调整掩码，以便此处的掩码行为与本机渲染循环的掩码行为一致。
        int maskbits = pixel_bits;
        if (maskbits == 3) {
            maskbits = 4;
        } else if (maskbits > 4 && maskbits < 8) {
            maskbits = 8;
        }
        pixel_mask = (1 << maskbits) - 1;
    }

    /**
     * 返回指定像素的红色颜色分量，范围从 0 到 255，默认的 RGB 颜色空间为 sRGB。像素值
     * 以 int 形式指定。仅使用像素值的最低 <em>n</em> 位（如上所述的
     * <a href="#index_values">类描述</a>）来计算返回值。
     * 返回的值是非预乘值。
     * @param pixel 指定的像素
     * @return 指定像素的红色颜色分量的值
     */
    final public int getRed(int pixel) {
        return (rgb[pixel & pixel_mask] >> 16) & 0xff;
    }

    /**
     * 返回指定像素的绿色颜色分量，范围从 0 到 255，默认的 RGB 颜色空间为 sRGB。像素值
     * 以 int 形式指定。仅使用像素值的最低 <em>n</em> 位（如上所述的
     * <a href="#index_values">类描述</a>）来计算返回值。
     * 返回的值是非预乘值。
     * @param pixel 指定的像素
     * @return 指定像素的绿色颜色分量的值
     */
    final public int getGreen(int pixel) {
        return (rgb[pixel & pixel_mask] >> 8) & 0xff;
    }

    /**
     * 返回指定像素的蓝色颜色分量，范围从 0 到 255，默认的 RGB 颜色空间为 sRGB。像素值
     * 以 int 形式指定。仅使用像素值的最低 <em>n</em> 位（如上所述的
     * <a href="#index_values">类描述</a>）来计算返回值。
     * 返回的值是非预乘值。
     * @param pixel 指定的像素
     * @return 指定像素的蓝色颜色分量的值
     */
    final public int getBlue(int pixel) {
        return rgb[pixel & pixel_mask] & 0xff;
    }

    /**
     * 返回指定像素的 alpha 分量，范围从 0 到 255。像素值以 int 形式指定。
     * 仅使用像素值的最低 <em>n</em> 位（如上所述的
     * <a href="#index_values">类描述</a>）来计算返回值。
     * @param pixel 指定的像素
     * @return 指定像素的 alpha 分量的值
     */
    final public int getAlpha(int pixel) {
        return (rgb[pixel & pixel_mask] >> 24) & 0xff;
    }

    /**
     * 返回像素在默认 RGB 颜色模型格式中的颜色/alpha 分量。像素值以 int 形式指定。
     * 仅使用像素值的最低 <em>n</em> 位（如上所述的
     * <a href="#index_values">类描述</a>）来计算返回值。
     * 返回的值是非预乘格式。
     * @param pixel 指定的像素
     * @return 指定像素的颜色和 alpha 分量
     * @see ColorModel#getRGBdefault
     */
    final public int getRGB(int pixel) {
        return rgb[pixel & pixel_mask];
    }

    private static final int CACHESIZE = 40;
    private int lookupcache[] = new int[CACHESIZE];

    /**
     * 返回此 ColorModel 中像素的数据元素数组表示形式，给定默认 RGB 颜色模型中的整数像素表示形式。
     * 此数组可以传递给 {@link WritableRaster#setDataElements(int, int, java.lang.Object) setDataElements}
     * 方法的 {@link WritableRaster} 对象。如果 pixel 变量为 <code>null</code>，则分配新数组。
     * 如果 <code>pixel</code> 不为 <code>null</code>，则必须是类型为 <code>transferType</code> 的基本数组；
     * 否则，将抛出 <code>ClassCastException</code>。如果 <code>pixel</code> 不足以容纳此
     * <code>ColorModel</code> 的像素值，则抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * 返回像素数组。
     * <p>
     * 由于 <code>IndexColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不重写该方法，
     * 并且使用不受支持的 <code>transferType</code>，则会抛出异常。
     *
     * @param rgb 默认 RGB 颜色模型中的整数像素表示形式
     * @param pixel 指定的像素
     * @return 指定像素在此 <code>IndexColorModel</code> 中的数组表示形式。
     * @throws ClassCastException 如果 <code>pixel</code>
     *  不是类型为 <code>transferType</code> 的基本数组
     * @throws ArrayIndexOutOfBoundsException 如果
     *  <code>pixel</code> 不足以容纳此 <code>ColorModel</code> 的像素值
     * @throws UnsupportedOperationException 如果 <code>transferType</code>
     *         无效
     * @see WritableRaster#setDataElements
     * @see SampleModel#setDataElements
     */
    public synchronized Object getDataElements(int rgb, Object pixel) {
        int red = (rgb>>16) & 0xff;
        int green = (rgb>>8) & 0xff;
        int blue  = rgb & 0xff;
        int alpha = (rgb>>>24);
        int pix = 0;

        // 注意，像素存储在 lookupcache[2*i] 中
        // 而搜索的 rgb 存储在
        // lookupcache[2*i+1] 中。此外，像素首先
        // 使用一元补码运算符反转
        // 以确保它永远不会是 0。
        for (int i = CACHESIZE - 2; i >= 0; i -= 2) {
            if ((pix = lookupcache[i]) == 0) {
                break;
            }
            if (rgb == lookupcache[i+1]) {
                return installpixel(pixel, ~pix);
            }
        }

        if (allgrayopaque) {
            // IndexColorModel 对象都被标记为
            // 非预乘，因此忽略传入颜色的 alpha 值，
            // 将非预乘颜色分量转换为灰度值，并在调色板中搜索最接近的
            // 灰度值。由于调色板中的所有颜色都是灰色的，因此我们只需要比较
            // 一个颜色分量即可匹配
            // 使用简单的线性距离公式。

            int minDist = 256;
            int d;
            int gray = (int) (red*77 + green*150 + blue*29 + 128)/256;

            for (int i = 0; i < map_size; i++) {
                if (this.rgb[i] == 0x0) {
                    // 对于 allgrayopaque 调色板，条目为 0
                    // 表示无效颜色，应在颜色搜索中忽略。
                    continue;
                }
                d = (this.rgb[i] & 0xff) - gray;
                if (d < 0) d = -d;
                if (d < minDist) {
                    pix = i;
                    if (d == 0) {
                        break;
                    }
                    minDist = d;
                }
            }
        } else if (transparency == OPAQUE) {
            // IndexColorModel 对象都被标记为
            // 非预乘，因此忽略传入颜色的 alpha 值，并使用 3 个分量
            // 欧几里得距离公式独立搜索最接近的颜色匹配。
            // 对于不透明的调色板，条目为 0
            // 表示无效颜色，应在颜色搜索中忽略。
            // 作为一种优化，不透明调色板中的精确颜色搜索
            // 可能相当常见，因此我们首先进行快速搜索
            // 以查找精确匹配。

            int smallestError = Integer.MAX_VALUE;
            int lut[] = this.rgb;
            int lutrgb;
            for (int i=0; i < map_size; i++) {
                lutrgb = lut[i];
                if (lutrgb == rgb && lutrgb != 0) {
                    pix = i;
                    smallestError = 0;
                    break;
                }
            }

            if (smallestError != 0) {
                for (int i=0; i < map_size; i++) {
                    lutrgb = lut[i];
                    if (lutrgb == 0) {
                        continue;
                    }

                    int tmp = ((lutrgb >> 16) & 0xff) - red;
                    int currentError = tmp*tmp;
                    if (currentError < smallestError) {
                        tmp = ((lutrgb >> 8) & 0xff) - green;
                        currentError += tmp * tmp;
                        if (currentError < smallestError) {
                            tmp = (lutrgb & 0xff) - blue;
                            currentError += tmp * tmp;
                            if (currentError < smallestError) {
                                pix = i;
                                smallestError = currentError;
                            }
                        }
                    }
                }
            }
        } else if (alpha == 0 && transparent_index >= 0) {
            // 特殊情况 - 透明颜色映射到
            // 指定的透明像素，如果有。

            pix = transparent_index;
        } else {
            // IndexColorModel 对象都被标记为
            // 非预乘，因此在距离计算中使用非预乘
            // 颜色分量。使用 4 个分量
            // 欧几里得距离公式查找最接近的匹配。

            int smallestError = Integer.MAX_VALUE;
            int lut[] = this.rgb;
            for (int i=0; i < map_size; i++) {
                int lutrgb = lut[i];
                if (lutrgb == rgb) {
                    if (validBits != null && !validBits.testBit(i)) {
                        continue;
                    }
                    pix = i;
                    break;
                }

                int tmp = ((lutrgb >> 16) & 0xff) - red;
                int currentError = tmp*tmp;
                if (currentError < smallestError) {
                    tmp = ((lutrgb >> 8) & 0xff) - green;
                    currentError += tmp * tmp;
                    if (currentError < smallestError) {
                        tmp = (lutrgb & 0xff) - blue;
                        currentError += tmp * tmp;
                        if (currentError < smallestError) {
                            tmp = (lutrgb >>> 24) - alpha;
                            currentError += tmp * tmp;
                            if (currentError < smallestError &&
                                (validBits == null || validBits.testBit(i)))
                            {
                                pix = i;
                                smallestError = currentError;
                            }
                        }
                    }
                }
            }
        }
        System.arraycopy(lookupcache, 2, lookupcache, 0, CACHESIZE - 2);
        lookupcache[CACHESIZE - 1] = rgb;
        lookupcache[CACHESIZE - 2] = ~pix;
        return installpixel(pixel, pix);
    }

    private Object installpixel(Object pixel, int pix) {
        switch (transferType) {
        case DataBuffer.TYPE_INT:
            int[] intObj;
            if (pixel == null) {
                pixel = intObj = new int[1];
            } else {
                intObj = (int[]) pixel;
            }
            intObj[0] = pix;
            break;
        case DataBuffer.TYPE_BYTE:
            byte[] byteObj;
            if (pixel == null) {
                pixel = byteObj = new byte[1];
            } else {
                byteObj = (byte[]) pixel;
            }
            byteObj[0] = (byte) pix;
            break;
        case DataBuffer.TYPE_USHORT:
            short[] shortObj;
            if (pixel == null) {
                pixel = shortObj = new short[1];
            } else {
                shortObj = (short[]) pixel;
            }
            shortObj[0] = (short) pix;
            break;
        default:
            throw new UnsupportedOperationException("此方法尚未为 transferType " + transferType + " 实现");
        }
        return pixel;
    }

    /**
     * 返回此 <code>ColorModel</code> 中指定像素的非归一化颜色/alpha 分量数组。像素值
     * 以 int 形式指定。如果 <code>components</code> 数组为 <code>null</code>，
     * 则分配一个包含 <code>offset + getNumComponents()</code> 元素的新数组。
     * 返回 <code>components</code> 数组，仅当 <code>hasAlpha</code> 返回 true 时包含 alpha 分量。
     * 颜色/alpha 分量从 <code>offset</code> 开始存储在 <code>components</code> 数组中，
     * 即使该数组是由此方法分配的。
     * 如果 <code>components</code> 数组不为 <code>null</code> 且不足以容纳从 <code>offset</code> 开始的所有颜色和 alpha 分量，
     * 则抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param pixel 指定的像素
     * @param components 用于接收指定像素的颜色和 alpha 分量的数组
     * @param offset <code>components</code> 数组中开始存储颜色和 alpha 分量的位置
     * @return 从指定偏移量开始包含指定像素的颜色和 alpha 分量的数组。
     * @see ColorModel#hasAlpha
     * @see ColorModel#getNumComponents
     */
    public int[] getComponents(int pixel, int[] components, int offset) {
        if (components == null) {
            components = new int[offset+numComponents];
        }


                    // REMIND: 需要在不同颜色空间时进行更改
        components[offset+0] = getRed(pixel);
        components[offset+1] = getGreen(pixel);
        components[offset+2] = getBlue(pixel);
        if (supportsAlpha && (components.length-offset) > 3) {
            components[offset+3] = getAlpha(pixel);
        }

        return components;
    }

    /**
     * 返回此 <code>ColorModel</code> 中指定像素的未归一化颜色/alpha 组件数组。指定的像素值由作为对象引用传递的类型为
     * <code>transferType</code> 的数据元素数组指定。如果 <code>pixel</code> 不是类型为
     * <code>transferType</code> 的原始数组，则抛出 <code>ClassCastException</code>。
     * 如果 <code>pixel</code> 不足以容纳此 <code>ColorModel</code> 的像素值，则抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * 如果 <code>components</code> 数组为 <code>null</code>，则分配一个包含
     * <code>offset + getNumComponents()</code> 元素的新数组。返回 <code>components</code> 数组，
     * 仅当 <code>hasAlpha</code> 返回 true 时才包含 alpha 组件。颜色/alpha 组件从 <code>offset</code> 开始存储在
     * <code>components</code> 数组中，即使该数组是由此方法分配的。如果 <code>components</code> 数组不为
     * <code>null</code> 且不足以从 <code>offset</code> 开始容纳所有颜色和 alpha 组件，则也抛出
     * <code>ArrayIndexOutOfBoundsException</code>。
     * <p>
     * 由于 <code>IndexColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不重写该方法，则在使用不受支持的
     * <code>transferType</code> 时会抛出异常。
     *
     * @param pixel 指定的像素
     * @param components 接收指定像素的颜色和 alpha 组件的数组
     * @param offset 指定像素的颜色和 alpha 组件在 <code>components</code> 数组中开始存储的索引
     * @return 从指定偏移开始包含指定像素的颜色和 alpha 组件的数组。
     * @throws ArrayIndexOutOfBoundsException 如果 <code>pixel</code>
     *            不足以容纳此 <code>ColorModel</code> 的像素值，或者 <code>components</code> 数组不为
     *            <code>null</code> 且不足以从 <code>offset</code> 开始容纳所有颜色和 alpha 组件
     * @throws ClassCastException 如果 <code>pixel</code> 不是类型为 <code>transferType</code> 的原始数组
     * @throws UnsupportedOperationException 如果 <code>transferType</code>
     *         不是支持的传输类型之一
     * @see ColorModel#hasAlpha
     * @see ColorModel#getNumComponents
     */
    public int[] getComponents(Object pixel, int[] components, int offset) {
        int intpixel;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])pixel;
               intpixel = bdata[0] & 0xff;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])pixel;
               intpixel = sdata[0] & 0xffff;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])pixel;
               intpixel = idata[0];
            break;
            default:
               throw new UnsupportedOperationException("此方法尚未为传输类型 " + transferType + " 实现");
        }
        return getComponents(intpixel, components, offset);
    }

    /**
     * 返回此 <code>ColorModel</code> 中给定未归一化颜色/alpha 组件数组的像素值，表示为 int。如果
     * <code>components</code> 数组不足以从 <code>offset</code> 开始容纳所有颜色和 alpha 组件，则抛出
     * <code>ArrayIndexOutOfBoundsException</code>。由于 <code>ColorModel</code> 可以被子类化，子类继承此方法的实现，
     * 如果它们不重写该方法，则在使用不受支持的传输类型时会抛出异常。
     * @param components 未归一化的颜色和 alpha 组件数组
     * @param offset 从 <code>components</code> 中开始检索颜色和 alpha 组件的索引
     * @return 与此 <code>ColorModel</code> 中指定组件对应的 int 像素值。
     * @throws ArrayIndexOutOfBoundsException 如果
     *  <code>components</code> 数组不足以从 <code>offset</code> 开始容纳所有颜色和 alpha 组件
     * @throws UnsupportedOperationException 如果 <code>transferType</code>
     *         无效
     */
    public int getDataElement(int[] components, int offset) {
        int rgb = (components[offset+0]<<16)
            | (components[offset+1]<<8) | (components[offset+2]);
        if (supportsAlpha) {
            rgb |= (components[offset+3]<<24);
        }
        else {
            rgb |= 0xff000000;
        }
        Object inData = getDataElements(rgb, null);
        int pixel;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               pixel = bdata[0] & 0xff;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])inData;
               pixel = sdata[0];
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               pixel = idata[0];
            break;
            default:
               throw new UnsupportedOperationException("此方法尚未为传输类型 " + transferType + " 实现");
        }
        return pixel;
    }

    /**
     * 返回此 <code>ColorModel</code> 中给定未归一化颜色/alpha 组件数组的数据元素数组表示。然后可以将此数组传递给
     * <code>WritableRaster</code> 对象的 <code>setDataElements</code> 方法。如果
     * <code>components</code> 数组不足以从 <code>offset</code> 开始容纳所有颜色和 alpha 组件，则抛出
     * <code>ArrayIndexOutOfBoundsException</code>。如果 <code>pixel</code> 为 <code>null</code>，则分配一个新数组。
     * 如果 <code>pixel</code> 不为 <code>null</code>，则必须是类型为 <code>transferType</code> 的原始数组；
     * 否则，抛出 <code>ClassCastException</code>。如果 <code>pixel</code> 不足以容纳此
     * <code>ColorModel</code> 的像素值，则抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * <p>
     * 由于 <code>IndexColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不重写该方法，则在使用不受支持的
     * <code>transferType</code> 时会抛出异常。
     *
     * @param components 未归一化的颜色和 alpha 组件数组
     * @param offset 从 <code>components</code> 中开始检索颜色和 alpha 组件的索引
     * @param pixel 代表颜色和 alpha 组件数组的 <code>Object</code>
     * @return 代表颜色和 alpha 组件数组的 <code>Object</code>。
     * @throws ClassCastException 如果 <code>pixel</code>
     *  不是类型为 <code>transferType</code> 的原始数组
     * @throws ArrayIndexOutOfBoundsException 如果
     *  <code>pixel</code> 不足以容纳此 <code>ColorModel</code> 的像素值，或者 <code>components</code>
     *  数组不足以从 <code>offset</code> 开始容纳所有颜色和 alpha 组件
     * @throws UnsupportedOperationException 如果 <code>transferType</code>
     *         不是支持的传输类型之一
     * @see WritableRaster#setDataElements
     * @see SampleModel#setDataElements
     */
    public Object getDataElements(int[] components, int offset, Object pixel) {
        int rgb = (components[offset+0]<<16) | (components[offset+1]<<8)
            | (components[offset+2]);
        if (supportsAlpha) {
            rgb |= (components[offset+3]<<24);
        }
        else {
            rgb &= 0xff000000;
        }
        return getDataElements(rgb, pixel);
    }

    /**
     * 创建一个具有指定宽度和高度的 <code>WritableRaster</code>，其数据布局（<code>SampleModel</code>）
     * 与此 <code>ColorModel</code> 兼容。此方法仅适用于每个像素 16 位或更少的色彩模型。
     * <p>
     * 由于 <code>IndexColorModel</code> 可以被子类化，任何支持每个像素超过 16 位的子类必须重写此方法。
     *
     * @param w 应用于新 <code>WritableRaster</code> 的宽度
     * @param h 应用于新 <code>WritableRaster</code> 的高度
     * @return 具有指定宽度和高度的 <code>WritableRaster</code> 对象。
     * @throws UnsupportedOperationException 如果每个像素的位数大于 16
     * @see WritableRaster
     * @see SampleModel
     */
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        WritableRaster raster;

        if (pixel_bits == 1 || pixel_bits == 2 || pixel_bits == 4) {
            // TYPE_BINARY
            raster = Raster.createPackedRaster(DataBuffer.TYPE_BYTE,
                                               w, h, 1, pixel_bits, null);
        }
        else if (pixel_bits <= 8) {
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                                                  w,h,1,null);
        }
        else if (pixel_bits <= 16) {
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_USHORT,
                                                  w,h,1,null);
        }
        else {
            throw new
                UnsupportedOperationException("此方法不支持每个像素位数大于 16 的情况。");
        }
        return raster;
    }

    /**
      * 如果 <code>raster</code> 与此 <code>ColorModel</code> 兼容，则返回 <code>true</code>；
      * 否则返回 <code>false</code>。
      * @param raster 要测试兼容性的 {@link Raster} 对象
      * @return 如果 <code>raster</code> 与此 <code>ColorModel</code> 兼容，则返回 <code>true</code>；
      * 否则返回 <code>false</code>。
      *
      */
    public boolean isCompatibleRaster(Raster raster) {

        int size = raster.getSampleModel().getSampleSize(0);
        return ((raster.getTransferType() == transferType) &&
                (raster.getNumBands() == 1) && ((1 << size) >= map_size));
    }

    /**
     * 创建一个具有指定宽度和高度的 <code>SampleModel</code>，其数据布局与此 <code>ColorModel</code> 兼容。
     * @param w 应用于新 <code>SampleModel</code> 的宽度
     * @param h 应用于新 <code>SampleModel</code> 的高度
     * @return 具有指定宽度和高度的 <code>SampleModel</code> 对象。
     * @throws IllegalArgumentException 如果 <code>w</code> 或
     *         <code>h</code> 不大于 0
     * @see SampleModel
     */
    public SampleModel createCompatibleSampleModel(int w, int h) {
        int[] off = new int[1];
        off[0] = 0;
        if (pixel_bits == 1 || pixel_bits == 2 || pixel_bits == 4) {
            return new MultiPixelPackedSampleModel(transferType, w, h,
                                                   pixel_bits);
        }
        else {
            return new ComponentSampleModel(transferType, w, h, 1, w,
                                            off);
        }
    }

    /**
     * 检查指定的 <code>SampleModel</code> 是否与此 <code>ColorModel</code> 兼容。如果 <code>sm</code> 为
     * <code>null</code>，则此方法返回 <code>false</code>。
     * @param sm 指定的 <code>SampleModel</code>，
     *           或 <code>null</code>
     * @return 如果指定的 <code>SampleModel</code> 与此 <code>ColorModel</code> 兼容，则返回 <code>true</code>；
     * 否则返回 <code>false</code>。
     * @see SampleModel
     */
    public boolean isCompatibleSampleModel(SampleModel sm) {
        // fix 4238629
        if (! (sm instanceof ComponentSampleModel) &&
            ! (sm instanceof MultiPixelPackedSampleModel)   ) {
            return false;
        }

        // 传输类型必须相同
        if (sm.getTransferType() != transferType) {
            return false;
        }

        if (sm.getNumBands() != 1) {
            return false;
        }

        return true;
    }

    /**
     * 返回一个 TYPE_INT_ARGB 或 TYPE_INT_RGB 的新 <code>BufferedImage</code>，其 <code>Raster</code> 包含通过扩展
     * 源 <code>Raster</code> 中的索引值并使用此 <code>ColorModel</code> 的颜色/alpha 组件数组计算的像素数据。
     * 仅使用源 <code>Raster</code> 中每个索引值的低 <em>n</em> 位，如上文
     * <a href="#index_values">类描述</a> 中所述，来计算返回图像中的颜色/alpha 值。
     * 如果 <code>forceARGB</code> 为 <code>true</code>，则无论此 <code>ColorModel</code> 是否具有 alpha 组件数组或透明像素，
     * 都返回 TYPE_INT_ARGB 图像。
     * @param raster 指定的 <code>Raster</code>
     * @param forceARGB 如果为 <code>true</code>，则返回的
     *     <code>BufferedImage</code> 为 TYPE_INT_ARGB；否则为 TYPE_INT_RGB
     * @return 使用指定 <code>Raster</code> 创建的 <code>BufferedImage</code>
     * @throws IllegalArgumentException 如果 raster 参数与此 IndexColorModel 不兼容
     */
    public BufferedImage convertToIntDiscrete(Raster raster,
                                              boolean forceARGB) {
        ColorModel cm;

        if (!isCompatibleRaster(raster)) {
            throw new IllegalArgumentException("此 raster 与此 IndexColorModel 不兼容。");
        }
        if (forceARGB || transparency == TRANSLUCENT) {
            cm = ColorModel.getRGBdefault();
        }
        else if (transparency == BITMASK) {
            cm = new DirectColorModel(25, 0xff0000, 0x00ff00, 0x0000ff,
                                      0x1000000);
        }
        else {
            cm = new DirectColorModel(24, 0xff0000, 0x00ff00, 0x0000ff);
        }


                    int w = raster.getWidth();
        int h = raster.getHeight();
        WritableRaster discreteRaster =
                  cm.createCompatibleWritableRaster(w, h);
        Object obj = null;
        int[] data = null;

        int rX = raster.getMinX();
        int rY = raster.getMinY();

        for (int y=0; y < h; y++, rY++) {
            obj = raster.getDataElements(rX, rY, w, 1, obj);
            if (obj instanceof int[]) {
                data = (int[])obj;
            } else {
                data = DataBuffer.toIntArray(obj);
            }
            for (int x=0; x < w; x++) {
                data[x] = rgb[data[x] & pixel_mask];
            }
            discreteRaster.setDataElements(0, y, w, 1, data);
        }

        return new BufferedImage(cm, discreteRaster, false, null);
    }

    /**
     * 返回指定像素是否有效。
     * @param pixel 指定的像素值
     * @return 如果 <code>pixel</code> 有效，则返回 <code>true</code>；
     * 否则返回 <code>false</code>。
     * @since 1.3
     */
    public boolean isValid(int pixel) {
        return ((pixel >= 0 && pixel < map_size) &&
                (validBits == null || validBits.testBit(pixel)));
    }

    /**
     * 返回所有像素是否都有效。
     * @return 如果所有像素都有效，则返回 <code>true</code>；
     * 否则返回 <code>false</code>。
     * @since 1.3
     */
    public boolean isValid() {
        return (validBits == null);
    }

    /**
     * 返回一个 <code>BigInteger</code>，表示颜色表中的有效/无效像素。
     * 如果 <code>BigInteger</code> 在某个索引处的值被设置，则该位有效；
     * 如果 <code>BigInteger</code> 在某个索引处的值未被设置，则该位无效。
     * 在 <code>BigInteger</code> 中查询的有效范围是 0 到地图大小之间。
     * @return 一个 <code>BigInteger</code>，表示有效/无效像素。
     * @since 1.3
     */
    public BigInteger getValidPixels() {
        if (validBits == null) {
            return getAllValid();
        }
        else {
            return validBits;
        }
    }

    /**
     * 释放与此 <code>ColorModel</code> 关联的系统资源，一旦此 <code>ColorModel</code> 不再被引用。
     */
    public void finalize() {
    }

    /**
     * 返回此 <code>ColorModel</code> 对象的内容的 <code>String</code> 表示形式。
     * @return 一个 <code>String</code>，表示此 <code>ColorModel</code> 对象的内容。
     */
    public String toString() {
       return new String("IndexColorModel: #pixelBits = "+pixel_bits
                         + " numComponents = "+numComponents
                         + " color space = "+colorSpace
                         + " transparency = "+transparency
                         + " transIndex   = "+transparent_index
                         + " has alpha = "+supportsAlpha
                         + " isAlphaPre = "+isAlphaPremultiplied
                         );
    }
}
