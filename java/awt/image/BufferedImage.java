
/*
 * Copyright (c) 1997, 2015, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import sun.awt.image.ByteComponentRaster;
import sun.awt.image.BytePackedRaster;
import sun.awt.image.IntegerComponentRaster;
import sun.awt.image.OffScreenImageSource;
import sun.awt.image.ShortComponentRaster;

/**
 *
 * <code>BufferedImage</code> 子类描述了一个具有可访问图像数据缓冲区的 {@link
 * java.awt.Image Image}。一个 <code>BufferedImage</code> 由一个 {@link ColorModel} 和一个
 * {@link Raster} 的图像数据组成。{@link SampleModel} 中的波段数量和类型必须与
 * <code>ColorModel</code> 所需的数量和类型匹配，以表示其颜色和 alpha 组件。所有
 * <code>BufferedImage</code> 对象的左上角坐标为 (0,&nbsp;0)。因此，用于构造
 * <code>BufferedImage</code> 的任何 <code>Raster</code> 必须具有 minX=0 和 minY=0。
 *
 * <p>
 * 该类依赖于 <code>Raster</code> 的数据获取和设置方法，以及 <code>ColorModel</code> 的颜色特征化方法。
 *
 * @see ColorModel
 * @see Raster
 * @see WritableRaster
 */
public class BufferedImage extends java.awt.Image
                           implements WritableRenderedImage, Transparency
{
    private int imageType = TYPE_CUSTOM;
    private ColorModel colorModel;
    private final WritableRaster raster;
    private OffScreenImageSource osis;
    private Hashtable<String, Object> properties;

    /**
     * 图像类型常量
     */

    /**
     * 图像类型未被识别，因此必须是自定义图像。此类型仅用作 getType() 方法的返回值。
     */
    public static final int TYPE_CUSTOM = 0;

    /**
     * 表示一个具有 8 位 RGB 颜色组件的图像，这些组件被压缩到整数像素中。该图像具有一个没有 alpha 的
     * {@link DirectColorModel}。当具有非不透明 alpha 的数据存储在该类型的图像中时，颜色数据必须调整为非预乘形式，并且 alpha 被丢弃，具体如
     * {@link java.awt.AlphaComposite} 文档中所述。
     */
    public static final int TYPE_INT_RGB = 1;

    /**
     * 表示一个具有 8 位 RGBA 颜色组件的图像，这些组件被压缩到整数像素中。该图像具有一个带有 alpha 的
     * <code>DirectColorModel</code>。此图像中的颜色数据被认为不是与 alpha 预乘的。当此类型用作
     * <code>BufferedImage</code> 构造函数的 <code>imageType</code> 参数时，创建的图像与 JDK1.1 及更早版本中创建的图像一致。
     */
    public static final int TYPE_INT_ARGB = 2;

    /**
     * 表示一个具有 8 位 RGBA 颜色组件的图像，这些组件被压缩到整数像素中。该图像具有一个带有 alpha 的
     * <code>DirectColorModel</code>。此图像中的颜色数据被认为与 alpha 预乘。
     */
    public static final int TYPE_INT_ARGB_PRE = 3;

    /**
     * 表示一个具有 8 位 RGB 颜色组件的图像，对应于 Windows 或 Solaris 风格的 BGR 颜色模型，颜色 Blue、Green 和 Red 被压缩到整数像素中。没有 alpha。
     * 该图像具有一个 {@link DirectColorModel}。当具有非不透明 alpha 的数据存储在该类型的图像中时，颜色数据必须调整为非预乘形式，并且 alpha 被丢弃，具体如
     * {@link java.awt.AlphaComposite} 文档中所述。
     */
    public static final int TYPE_INT_BGR = 4;

    /**
     * 表示一个具有 8 位 RGB 颜色组件的图像，对应于 Windows 风格的 BGR 颜色模型，颜色 Blue、Green 和 Red 存储在 3 个字节中。没有 alpha。该图像具有一个
     * <code>ComponentColorModel</code>。当具有非不透明 alpha 的数据存储在该类型的图像中时，颜色数据必须调整为非预乘形式，并且 alpha 被丢弃，具体如
     * {@link java.awt.AlphaComposite} 文档中所述。
     */
    public static final int TYPE_3BYTE_BGR = 5;

    /**
     * 表示一个具有 8 位 RGBA 颜色组件的图像，颜色 Blue、Green 和 Red 存储在 3 个字节中，1 个字节的 alpha。该图像具有一个带有 alpha 的
     * <code>ComponentColorModel</code>。此图像中的颜色数据被认为不是与 alpha 预乘的。字节数据在单个字节数组中交错，顺序为 A, B, G, R
     * 从低到高字节地址在每个像素内。
     */
    public static final int TYPE_4BYTE_ABGR = 6;

    /**
     * 表示一个具有 8 位 RGBA 颜色组件的图像，颜色 Blue、Green 和 Red 存储在 3 个字节中，1 个字节的 alpha。该图像具有一个带有 alpha 的
     * <code>ComponentColorModel</code>。此图像中的颜色数据被认为与 alpha 预乘。字节数据在单个字节数组中交错，顺序为 A, B, G, R 从低到高字节地址在每个像素内。
     */
    public static final int TYPE_4BYTE_ABGR_PRE = 7;

    /**
     * 表示一个具有 5-6-5 RGB 颜色组件的图像（5 位红色，6 位绿色，5 位蓝色），没有 alpha。该图像具有一个
     * <code>DirectColorModel</code>。当具有非不透明 alpha 的数据存储在该类型的图像中时，颜色数据必须调整为非预乘形式，并且 alpha 被丢弃，具体如
     * {@link java.awt.AlphaComposite} 文档中所述。
     */
    public static final int TYPE_USHORT_565_RGB = 8;

    /**
     * 表示一个具有 5-5-5 RGB 颜色组件的图像（5 位红色，5 位绿色，5 位蓝色），没有 alpha。该图像具有一个
     * <code>DirectColorModel</code>。当具有非不透明 alpha 的数据存储在该类型的图像中时，颜色数据必须调整为非预乘形式，并且 alpha 被丢弃，具体如
     * {@link java.awt.AlphaComposite} 文档中所述。
     */
    public static final int TYPE_USHORT_555_RGB = 9;

    /**
     * 表示一个无符号字节灰度图像，非索引。该图像具有一个带有 CS_GRAY
     * {@link ColorSpace} 的 <code>ComponentColorModel</code>。当具有非不透明 alpha 的数据存储在该类型的图像中时，颜色数据必须调整为非预乘形式，并且 alpha 被丢弃，具体如
     * {@link java.awt.AlphaComposite} 文档中所述。
     */
    public static final int TYPE_BYTE_GRAY = 10;

    /**
     * 表示一个无符号短整型灰度图像，非索引。该图像具有一个带有 CS_GRAY
     * <code>ColorSpace</code> 的 <code>ComponentColorModel</code>。当具有非不透明 alpha 的数据存储在该类型的图像中时，颜色数据必须调整为非预乘形式，并且 alpha 被丢弃，具体如
     * {@link java.awt.AlphaComposite} 文档中所述。
     */
    public static final int TYPE_USHORT_GRAY = 11;

    /**
     * 表示一个不透明的 1、2 或 4 位字节打包图像。该图像具有一个没有 alpha 的
     * {@link IndexColorModel}。当此类型用作 <code>BufferedImage</code> 构造函数的
     * <code>imageType</code> 参数，但没有 <code>ColorModel</code> 参数时，会创建一个 1 位图像，其中
     * <code>IndexColorModel</code> 包含两个颜色，默认的 sRGB <code>ColorSpace</code>：{0,&nbsp;0,&nbsp;0} 和
     * {255,&nbsp;255,&nbsp;255}。
     *
     * <p> 2 或 4 位每像素的图像可以通过提供适当映射大小的
     * <code>ColorModel</code>，使用带有 <code>ColorModel</code> 参数的
     * <code>BufferedImage</code> 构造函数来构建。
     *
     * <p> 8 位每像素的图像应使用图像类型
     * <code>TYPE_BYTE_INDEXED</code> 或 <code>TYPE_BYTE_GRAY</code>，具体取决于其
     * <code>ColorModel</code>。

     * <p> 当颜色数据存储在该类型的图像中时，<code>IndexColorModel</code> 确定颜色映射中最接近的颜色，并存储相应的索引。根据
     * <code>IndexColorModel</code> 颜色映射中的颜色，可能会导致颜色或 alpha 组件的近似和丢失。
     */
    public static final int TYPE_BYTE_BINARY = 12;

    /**
     * 表示一个索引字节图像。当此类型用作 <code>BufferedImage</code> 构造函数的
     * <code>imageType</code> 参数，但没有 <code>ColorModel</code> 参数时，会创建一个
     * <code>IndexColorModel</code>，其中包含一个 256 色 6/6/6 颜色立方体调色板，其余颜色从 216-255 由默认 sRGB 颜色空间中的灰度值填充。
     *
     * <p> 当颜色数据存储在该类型的图像中时，<code>IndexColorModel</code> 确定颜色映射中最接近的颜色，并存储相应的索引。根据
     * <code>IndexColorModel</code> 颜色映射中的颜色，可能会导致颜色或 alpha 组件的近似和丢失。
     */
    public static final int TYPE_BYTE_INDEXED = 13;

    private static final int DCM_RED_MASK   = 0x00ff0000;
    private static final int DCM_GREEN_MASK = 0x0000ff00;
    private static final int DCM_BLUE_MASK  = 0x000000ff;
    private static final int DCM_ALPHA_MASK = 0xff000000;
    private static final int DCM_565_RED_MASK = 0xf800;
    private static final int DCM_565_GRN_MASK = 0x07E0;
    private static final int DCM_565_BLU_MASK = 0x001F;
    private static final int DCM_555_RED_MASK = 0x7C00;
    private static final int DCM_555_GRN_MASK = 0x03E0;
    private static final int DCM_555_BLU_MASK = 0x001F;
    private static final int DCM_BGR_RED_MASK = 0x0000ff;
    private static final int DCM_BGR_GRN_MASK = 0x00ff00;
    private static final int DCM_BGR_BLU_MASK = 0xff0000;


    static private native void initIDs();
    static {
        ColorModel.loadLibraries();
        initIDs();
    }

    /**
     * 构造一个预定义图像类型之一的 <code>BufferedImage</code>。该图像的 <code>ColorSpace</code> 是默认的 sRGB 空间。
     * @param width     创建的图像的宽度
     * @param height    创建的图像的高度
     * @param imageType 创建的图像类型
     * @see ColorSpace
     * @see #TYPE_INT_RGB
     * @see #TYPE_INT_ARGB
     * @see #TYPE_INT_ARGB_PRE
     * @see #TYPE_INT_BGR
     * @see #TYPE_3BYTE_BGR
     * @see #TYPE_4BYTE_ABGR
     * @see #TYPE_4BYTE_ABGR_PRE
     * @see #TYPE_BYTE_GRAY
     * @see #TYPE_USHORT_GRAY
     * @see #TYPE_BYTE_BINARY
     * @see #TYPE_BYTE_INDEXED
     * @see #TYPE_USHORT_565_RGB
     * @see #TYPE_USHORT_555_RGB
     */
    public BufferedImage(int width,
                         int height,
                         int imageType) {
        switch (imageType) {
        case TYPE_INT_RGB:
            {
                colorModel = new DirectColorModel(24,
                                                  0x00ff0000,   // Red
                                                  0x0000ff00,   // Green
                                                  0x000000ff,   // Blue
                                                  0x0           // Alpha
                                                  );
                raster = colorModel.createCompatibleWritableRaster(width,
                                                                   height);
            }
        break;

        case TYPE_INT_ARGB:
            {
                colorModel = ColorModel.getRGBdefault();

                raster = colorModel.createCompatibleWritableRaster(width,
                                                                   height);
            }
        break;

        case TYPE_INT_ARGB_PRE:
            {
                colorModel = new
                    DirectColorModel(
                                     ColorSpace.getInstance(ColorSpace.CS_sRGB),
                                     32,
                                     0x00ff0000,// Red
                                     0x0000ff00,// Green
                                     0x000000ff,// Blue
                                     0xff000000,// Alpha
                                     true,       // Alpha Premultiplied
                                     DataBuffer.TYPE_INT
                                     );
                raster = colorModel.createCompatibleWritableRaster(width,
                                                                   height);
            }
        break;

        case TYPE_INT_BGR:
            {
                colorModel = new DirectColorModel(24,
                                                  0x000000ff,   // Red
                                                  0x0000ff00,   // Green
                                                  0x00ff0000    // Blue
                                                  );
                raster = colorModel.createCompatibleWritableRaster(width,
                                                                   height);
            }
        break;

        case TYPE_3BYTE_BGR:
            {
                ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
                int[] nBits = {8, 8, 8};
                int[] bOffs = {2, 1, 0};
                colorModel = new ComponentColorModel(cs, nBits, false, false,
                                                     Transparency.OPAQUE,
                                                     DataBuffer.TYPE_BYTE);
                raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                                                        width, height,
                                                        width*3, 3,
                                                        bOffs, null);
            }
        break;


/*
 *
 *  FOR NOW THE CODE WHICH DEFINES THE RASTER TYPE IS DUPLICATED BY DVF
 *  SEE THE METHOD DEFINERASTERTYPE @ RASTEROUTPUTMANAGER
 *
 */
    public BufferedImage (ColorModel cm,
                          WritableRaster raster,
                          boolean isRasterPremultiplied,
                          Hashtable<?,?> properties) {

        if (!cm.isCompatibleRaster(raster)) {
            throw new
                IllegalArgumentException("Raster "+raster+
                                         " 与 ColorModel 不兼容 "+
                                         cm);
        }

        if ((raster.minX != 0) || (raster.minY != 0)) {
            throw new
                IllegalArgumentException("Raster "+raster+
                                         " 的 minX 或 minY 不等于零: "
                                         + raster.minX + " " + raster.minY);
        }

        colorModel = cm;
        this.raster  = raster;
        if (properties != null && !properties.isEmpty()) {
            this.properties = new Hashtable<>();
            for (final Object key : properties.keySet()) {
                if (key instanceof String) {
                    this.properties.put((String) key, properties.get(key));
                }
            }
        }
        int numBands = raster.getNumBands();
        boolean isAlphaPre = cm.isAlphaPremultiplied();
        final boolean isStandard = isStandard(cm, raster);
        ColorSpace cs;

        // 强制使光栅数据的 alpha 状态与颜色模型中的预乘状态匹配
        coerceData(isRasterPremultiplied);

        SampleModel sm = raster.getSampleModel();
        cs = cm.getColorSpace();
        int csType = cs.getType();
        if (csType != ColorSpace.TYPE_RGB) {
            if (csType == ColorSpace.TYPE_GRAY &&
                isStandard &&
                cm instanceof ComponentColorModel) {
                // 检查这是否可能是子光栅（修复 bug 4240596）
                if (sm instanceof ComponentSampleModel &&
                    ((ComponentSampleModel)sm).getPixelStride() != numBands) {
                    imageType = TYPE_CUSTOM;
                } else if (raster instanceof ByteComponentRaster &&
                       raster.getNumBands() == 1 &&
                       cm.getComponentSize(0) == 8 &&
                       ((ByteComponentRaster)raster).getPixelStride() == 1) {
                    imageType = TYPE_BYTE_GRAY;
                } else if (raster instanceof ShortComponentRaster &&
                       raster.getNumBands() == 1 &&
                       cm.getComponentSize(0) == 16 &&
                       ((ShortComponentRaster)raster).getPixelStride() == 1) {
                    imageType = TYPE_USHORT_GRAY;
                }
            } else {
                imageType = TYPE_CUSTOM;
            }
            return;
        }

        if ((raster instanceof IntegerComponentRaster) &&
            (numBands == 3 || numBands == 4)) {
            IntegerComponentRaster iraster =
                (IntegerComponentRaster) raster;
            // 检查光栅参数和颜色模型是否正确
            int pixSize = cm.getPixelSize();
            if (iraster.getPixelStride() == 1 &&
                isStandard &&
                cm instanceof DirectColorModel  &&
                (pixSize == 32 || pixSize == 24))
            {
                // 现在检查 DirectColorModel 参数
                DirectColorModel dcm = (DirectColorModel) cm;
                int rmask = dcm.getRedMask();
                int gmask = dcm.getGreenMask();
                int bmask = dcm.getBlueMask();
                if (rmask == DCM_RED_MASK && gmask == DCM_GREEN_MASK &&
                    bmask == DCM_BLUE_MASK)
                {
                    if (dcm.getAlphaMask() == DCM_ALPHA_MASK) {
                        imageType = (isAlphaPre
                                     ? TYPE_INT_ARGB_PRE
                                     : TYPE_INT_ARGB);
                    }
                    else {
                        // 没有 Alpha
                        if (!dcm.hasAlpha()) {
                            imageType = TYPE_INT_RGB;
                        }
                    }
                }   // if (dcm.getRedMask() == DCM_RED_MASK &&
                else if (rmask == DCM_BGR_RED_MASK && gmask == DCM_BGR_GRN_MASK
                         && bmask == DCM_BGR_BLU_MASK) {
                    if (!dcm.hasAlpha()) {
                        imageType = TYPE_INT_BGR;
                    }
                }  // if (rmask == DCM_BGR_RED_MASK &&
            }   // if (iraster.getPixelStride() == 1
        }   // ((raster instanceof IntegerComponentRaster) &&
        else if ((cm instanceof IndexColorModel) && (numBands == 1) &&
                 isStandard &&
                 (!cm.hasAlpha() || !isAlphaPre))
        {
            IndexColorModel icm = (IndexColorModel) cm;
            int pixSize = icm.getPixelSize();

            if (raster instanceof BytePackedRaster) {
                imageType = TYPE_BYTE_BINARY;
            }   // if (raster instanceof BytePackedRaster)
            else if (raster instanceof ByteComponentRaster) {
                ByteComponentRaster braster = (ByteComponentRaster) raster;
                if (braster.getPixelStride() == 1 && pixSize <= 8) {
                    imageType = TYPE_BYTE_INDEXED;
                }
            }
        }   // else if (cm instanceof IndexColorModel) && (numBands == 1))
        else if ((raster instanceof ShortComponentRaster)
                 && (cm instanceof DirectColorModel)
                 && isStandard
                 && (numBands == 3)
                 && !cm.hasAlpha())
        {
            DirectColorModel dcm = (DirectColorModel) cm;
            if (dcm.getRedMask() == DCM_565_RED_MASK) {
                if (dcm.getGreenMask() == DCM_565_GRN_MASK &&
                    dcm.getBlueMask()  == DCM_565_BLU_MASK) {
                    imageType = TYPE_USHORT_565_RGB;
                }
            }
            else if (dcm.getRedMask() == DCM_555_RED_MASK) {
                if (dcm.getGreenMask() == DCM_555_GRN_MASK &&
                    dcm.getBlueMask() == DCM_555_BLU_MASK) {
                    imageType = TYPE_USHORT_555_RGB;
                }
            }
        }   // else if ((cm instanceof IndexColorModel) && (numBands == 1))
        else if ((raster instanceof ByteComponentRaster)
                 && (cm instanceof ComponentColorModel)
                 && isStandard
                 && (raster.getSampleModel() instanceof PixelInterleavedSampleModel)
                 && (numBands == 3 || numBands == 4))
        {
            ComponentColorModel ccm = (ComponentColorModel) cm;
            PixelInterleavedSampleModel csm =
                (PixelInterleavedSampleModel)raster.getSampleModel();
            ByteComponentRaster braster = (ByteComponentRaster) raster;
            int[] offs = csm.getBandOffsets();
            if (ccm.getNumComponents() != numBands) {
                throw new RasterFormatException("ColorModel 中的组件数量（"+
                                                ccm.getNumComponents()+
                                                "）与 Raster 中的数量（"+numBands+") 不匹配");
            }
            int[] nBits = ccm.getComponentSize();
            boolean is8bit = true;
            for (int i=0; i < numBands; i++) {
                if (nBits[i] != 8) {
                    is8bit = false;
                    break;
                }
            }
            if (is8bit &&
                braster.getPixelStride() == numBands &&
                offs[0] == numBands-1 &&
                offs[1] == numBands-2 &&
                offs[2] == numBands-3)
            {
                if (numBands == 3 && !ccm.hasAlpha()) {
                    imageType = TYPE_3BYTE_BGR;
                }
                else if (offs[3] == 0 && ccm.hasAlpha()) {
                    imageType = (isAlphaPre
                                 ? TYPE_4BYTE_ABGR_PRE
                                 : TYPE_4BYTE_ABGR);
                }
            }
        }   // else if ((raster instanceof ByteComponentRaster) &&
    }


                private static boolean isStandard(ColorModel cm, WritableRaster wr) {
        final Class<? extends ColorModel> cmClass = cm.getClass();
        final Class<? extends WritableRaster> wrClass = wr.getClass();
        final Class<? extends SampleModel> smClass = wr.getSampleModel().getClass();

        final PrivilegedAction<Boolean> checkClassLoadersAction =
                new PrivilegedAction<Boolean>()
        {

            @Override
            public Boolean run() {
                final ClassLoader std = System.class.getClassLoader();

                return (cmClass.getClassLoader() == std) &&
                        (smClass.getClassLoader() == std) &&
                        (wrClass.getClassLoader() == std);
            }
        };
        return AccessController.doPrivileged(checkClassLoadersAction);
    }

    /**
     * 返回图像类型。如果它不是已知类型之一，则返回 TYPE_CUSTOM。
     * @return 此 <code>BufferedImage</code> 的图像类型。
     * @see #TYPE_INT_RGB
     * @see #TYPE_INT_ARGB
     * @see #TYPE_INT_ARGB_PRE
     * @see #TYPE_INT_BGR
     * @see #TYPE_3BYTE_BGR
     * @see #TYPE_4BYTE_ABGR
     * @see #TYPE_4BYTE_ABGR_PRE
     * @see #TYPE_BYTE_GRAY
     * @see #TYPE_BYTE_BINARY
     * @see #TYPE_BYTE_INDEXED
     * @see #TYPE_USHORT_GRAY
     * @see #TYPE_USHORT_565_RGB
     * @see #TYPE_USHORT_555_RGB
     * @see #TYPE_CUSTOM
     */
    public int getType() {
        return imageType;
    }

    /**
     * 返回 <code>ColorModel</code>。
     * @return 此 <code>BufferedImage</code> 的 <code>ColorModel</code>。
     */
    public ColorModel getColorModel() {
        return colorModel;
    }

    /**
     * 返回 {@link WritableRaster}。
     * @return 此 <code>BufferedImage</code> 的 <code>WriteableRaster</code>。
     */
    public WritableRaster getRaster() {
        return raster;
    }


    /**
     * 返回一个表示 <code>BufferedImage</code> 对象的 alpha 通道的 <code>WritableRaster</code>，
     * 适用于支持独立空间 alpha 通道的 <code>ColorModel</code> 对象，如 <code>ComponentColorModel</code> 和
     * <code>DirectColorModel</code>。如果此图像的 <code>ColorModel</code> 没有关联的 alpha 通道，则返回 <code>null</code>。
     * 该方法假设对于所有非 <code>IndexColorModel</code> 的 <code>ColorModel</code> 对象，如果支持 alpha，
     * 则存在一个独立的 alpha 通道，该通道存储为图像数据的最后一波段。
     * 如果图像使用的是 <code>IndexColorModel</code>，并且 alpha 存在于查找表中，则此方法返回 <code>null</code>，
     * 因为没有空间上独立的 alpha 通道。此方法创建一个新的 <code>WritableRaster</code>，但共享数据数组。
     * @return 一个 <code>WritableRaster</code> 或 <code>null</code>，如果此 <code>BufferedImage</code>
     *          没有关联的 alpha 通道。
     */
    public WritableRaster getAlphaRaster() {
        return colorModel.getAlphaRaster(raster);
    }

    /**
     * 返回默认 RGB 颜色模型 (TYPE_INT_ARGB) 和默认 sRGB 颜色空间中的整数像素。
     * 如果此默认模型与图像的 <code>ColorModel</code> 不匹配，则进行颜色转换。使用此方法返回的数据中，
     * 每个颜色分量只有 8 位精度。
     *
     * <p>
     *
     * 如果坐标不在边界内，可能会抛出 <code>ArrayOutOfBoundsException</code>。
     * 但是，显式边界检查不是必需的。
     *
     * @param x 从其中获取默认 RGB 颜色模型和 sRGB 颜色空间的像素的 X 坐标
     * @param y 从其中获取默认 RGB 颜色模型和 sRGB 颜色空间的像素的 Y 坐标
     * @return 默认 RGB 颜色模型和默认 sRGB 颜色空间中的整数像素。
     * @see #setRGB(int, int, int)
     * @see #setRGB(int, int, int, int, int[], int, int)
     */
    public int getRGB(int x, int y) {
        return colorModel.getRGB(raster.getDataElements(x, y, null));
    }

    /**
     * 从图像数据的一部分返回默认 RGB 颜色模型 (TYPE_INT_ARGB) 和默认 sRGB 颜色空间中的整数像素数组。
     * 如果默认模型与图像的 <code>ColorModel</code> 不匹配，则进行颜色转换。使用此方法返回的数据中，
     * 每个颜色分量只有 8 位精度。对于图像中的指定坐标 (x, y)，可以这样访问 ARGB 像素：
     *
     * <pre>
     *    pixel   = rgbArray[offset + (y-startY)*scansize + (x-startX)]; </pre>
     *
     * <p>
     *
     * 如果区域不在边界内，可能会抛出 <code>ArrayOutOfBoundsException</code>。
     * 但是，显式边界检查不是必需的。
     *
     * @param startX 起始 X 坐标
     * @param startY 起始 Y 坐标
     * @param w 区域宽度
     * @param h 区域高度
     * @param rgbArray 如果不为 <code>null</code>，则将 RGB 像素写入此处
     * @param offset <code>rgbArray</code> 中的偏移量
     * @param scansize <code>rgbArray</code> 的扫描线跨度
     * @return RGB 像素数组。
     * @see #setRGB(int, int, int)
     * @see #setRGB(int, int, int, int, int[], int, int)
     */
    public int[] getRGB(int startX, int startY, int w, int h,
                        int[] rgbArray, int offset, int scansize) {
        int yoff  = offset;
        int off;
        Object data;
        int nbands = raster.getNumBands();
        int dataType = raster.getDataBuffer().getDataType();
        switch (dataType) {
        case DataBuffer.TYPE_BYTE:
            data = new byte[nbands];
            break;
        case DataBuffer.TYPE_USHORT:
            data = new short[nbands];
            break;
        case DataBuffer.TYPE_INT:
            data = new int[nbands];
            break;
        case DataBuffer.TYPE_FLOAT:
            data = new float[nbands];
            break;
        case DataBuffer.TYPE_DOUBLE:
            data = new double[nbands];
            break;
        default:
            throw new IllegalArgumentException("未知的数据缓冲区类型: " +
                                               dataType);
        }

        if (rgbArray == null) {
            rgbArray = new int[offset+h*scansize];
        }

        for (int y = startY; y < startY+h; y++, yoff+=scansize) {
            off = yoff;
            for (int x = startX; x < startX+w; x++) {
                rgbArray[off++] = colorModel.getRGB(raster.getDataElements(x,
                                                                        y,
                                                                        data));
            }
        }

        return rgbArray;
    }


    /**
     * 将此 <code>BufferedImage</code> 中的像素设置为指定的 RGB 值。假设像素在默认的 RGB 颜色模型中，
     * 类型为 TYPE_INT_ARGB，并且在默认的 sRGB 颜色空间中。对于具有 <code>IndexColorModel</code> 的图像，
     * 选择最近的颜色索引。
     *
     * <p>
     *
     * 如果坐标不在边界内，可能会抛出 <code>ArrayOutOfBoundsException</code>。
     * 但是，显式边界检查不是必需的。
     *
     * @param x 要设置的像素的 X 坐标
     * @param y 要设置的像素的 Y 坐标
     * @param rgb RGB 值
     * @see #getRGB(int, int)
     * @see #getRGB(int, int, int, int, int[], int, int)
     */
    public synchronized void setRGB(int x, int y, int rgb) {
        raster.setDataElements(x, y, colorModel.getDataElements(rgb, null));
    }

    /**
     * 将默认 RGB 颜色模型 (TYPE_INT_ARGB) 和默认 sRGB 颜色空间中的整数像素数组设置到图像数据的一部分。
     * 如果默认模型与图像的 <code>ColorModel</code> 不匹配，则进行颜色转换。使用此方法返回的数据中，
     * 每个颜色分量只有 8 位精度。对于图像中的指定坐标 (x, y)，可以这样访问 ARGB 像素：
     * <pre>
     *    pixel   = rgbArray[offset + (y-startY)*scansize + (x-startX)];
     * </pre>
     * 警告：不会进行抖动处理。
     *
     * <p>
     *
     * 如果区域不在边界内，可能会抛出 <code>ArrayOutOfBoundsException</code>。
     * 但是，显式边界检查不是必需的。
     *
     * @param startX 起始 X 坐标
     * @param startY 起始 Y 坐标
     * @param w 区域宽度
     * @param h 区域高度
     * @param rgbArray RGB 像素数组
     * @param offset <code>rgbArray</code> 中的偏移量
     * @param scansize <code>rgbArray</code> 的扫描线跨度
     * @see #getRGB(int, int)
     * @see #getRGB(int, int, int, int, int[], int, int)
     */
    public void setRGB(int startX, int startY, int w, int h,
                        int[] rgbArray, int offset, int scansize) {
        int yoff  = offset;
        int off;
        Object pixel = null;

        for (int y = startY; y < startY+h; y++, yoff+=scansize) {
            off = yoff;
            for (int x = startX; x < startX+w; x++) {
                pixel = colorModel.getDataElements(rgbArray[off++], pixel);
                raster.setDataElements(x, y, pixel);
            }
        }
    }


    /**
     * 返回 <code>BufferedImage</code> 的宽度。
     * @return 此 <code>BufferedImage</code> 的宽度
     */
    public int getWidth() {
        return raster.getWidth();
    }

    /**
     * 返回 <code>BufferedImage</code> 的高度。
     * @return 此 <code>BufferedImage</code> 的高度
     */
    public int getHeight() {
        return raster.getHeight();
    }

    /**
     * 返回 <code>BufferedImage</code> 的宽度。
     * @param observer 忽略
     * @return 此 <code>BufferedImage</code> 的宽度
     */
    public int getWidth(ImageObserver observer) {
        return raster.getWidth();
    }

    /**
     * 返回 <code>BufferedImage</code> 的高度。
     * @param observer 忽略
     * @return 此 <code>BufferedImage</code> 的高度
     */
    public int getHeight(ImageObserver observer) {
        return raster.getHeight();
    }

    /**
     * 返回生成图像像素的对象。
     * @return 用于生成此图像像素的 {@link ImageProducer}。
     * @see ImageProducer
     */
    public ImageProducer getSource() {
        if (osis == null) {
            if (properties == null) {
                properties = new Hashtable();
            }
            osis = new OffScreenImageSource(this, properties);
        }
        return osis;
    }


    /**
     * 通过名称返回图像的属性。各个属性名称由不同的图像格式定义。如果特定图像未定义某个属性，
     * 则此方法返回 <code>UndefinedProperty</code> 字段。如果此图像的属性尚不可知，则此方法返回
     * <code>null</code>，并且 <code>ImageObserver</code> 对象稍后会收到通知。属性名称 "comment" 应用于
     * 存储可呈现给用户的描述图像、其来源或作者的可选注释。
     * @param name 属性名称
     * @param observer 接收图像信息通知的 <code>ImageObserver</code>
     * @return 指定 <code>name</code> 的属性所指的 {@link Object} 或 <code>null</code>，如果此图像的属性尚不可知。
     * @throws NullPointerException 如果属性名称为 null。
     * @see ImageObserver
     * @see java.awt.Image#UndefinedProperty
     */
    public Object getProperty(String name, ImageObserver observer) {
        return getProperty(name);
    }

    /**
     * 通过名称返回图像的属性。
     * @param name 属性名称
     * @return 指定 <code>name</code> 的属性所指的 <code>Object</code>。
     * @throws NullPointerException 如果属性名称为 null。
     */
    public Object getProperty(String name) {
        if (name == null) {
            throw new NullPointerException("不允许属性名称为 null");
        }
        if (properties == null) {
            return java.awt.Image.UndefinedProperty;
        }
        Object o = properties.get(name);
        if (o == null) {
            o = java.awt.Image.UndefinedProperty;
        }
        return o;
    }

    /**
     * 返回一个 {@link Graphics2D}，但为了向后兼容而存在。{@link #createGraphics() createGraphics} 更方便，
     * 因为它声明返回一个 <code>Graphics2D</code>。
     * @return 可用于在此图像中绘制的 <code>Graphics2D</code>。
     */
    public java.awt.Graphics getGraphics() {
        return createGraphics();
    }

    /**
     * 创建一个可用于在此 <code>BufferedImage</code> 中绘制的 <code>Graphics2D</code>。
     * @return 用于在此图像中绘制的 <code>Graphics2D</code>。
     */
    public Graphics2D createGraphics() {
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        return env.createGraphics(this);
    }

    /**
     * 返回由指定矩形区域定义的子图像。返回的 <code>BufferedImage</code> 与原始图像共享相同的数据数组。
     * @param x 指定矩形区域左上角的 X 坐标
     * @param y 指定矩形区域左上角的 Y 坐标
     * @param w 指定矩形区域的宽度
     * @param h 指定矩形区域的高度
     * @return 此 <code>BufferedImage</code> 的子图像。
     * @exception RasterFormatException 如果指定的区域不在此 <code>BufferedImage</code> 内。
     */
    public BufferedImage getSubimage (int x, int y, int w, int h) {
        return new BufferedImage (colorModel,
                                  raster.createWritableChild(x, y, w, h,
                                                             0, 0, null),
                                  colorModel.isAlphaPremultiplied(),
                                  properties);
    }


/**
 * 返回 alpha 是否已被预乘。如果没有 alpha，则返回 <code>false</code>。
 * @return 如果 alpha 已被预乘，则返回 <code>true</code>；否则返回 <code>false</code>。
 */
public boolean isAlphaPremultiplied() {
    return colorModel.isAlphaPremultiplied();
}

/**
 * 强制数据与 <code>isAlphaPremultiplied</code> 变量指定的状态匹配。它可能会乘以或除以颜色光栅数据的 alpha，或者如果数据处于正确状态，则不执行任何操作。
 * @param isAlphaPremultiplied 如果 alpha 已被预乘，则返回 <code>true</code>；否则返回 <code>false</code>。
 */
public void coerceData (boolean isAlphaPremultiplied) {
    if (colorModel.hasAlpha() &&
        colorModel.isAlphaPremultiplied() != isAlphaPremultiplied) {
        // 让颜色模型执行转换
        colorModel = colorModel.coerceData (raster, isAlphaPremultiplied);
    }
}

/**
 * 返回此 <code>BufferedImage</code> 对象及其值的 <code>String</code> 表示。
 * @return 代表此 <code>BufferedImage</code> 的 <code>String</code>。
 */
public String toString() {
    return "BufferedImage@"+Integer.toHexString(hashCode())
        +": type = "+imageType
        +" "+colorModel+" "+raster;
}

/**
 * 返回一个包含 <code>RenderedImage</code> 对象的 {@link Vector}，这些对象是此 <code>BufferedImage</code> 的直接数据源，而不是这些直接数据源的源。
 * 如果 <code>BufferedImage</code> 没有关于其直接源的信息，则此方法返回 <code>null</code>。如果 <code>BufferedImage</code> 没有直接源，则返回一个空的 <code>Vector</code>。
 * @return 包含此 <code>BufferedImage</code> 对象图像数据的直接源的 <code>Vector</code>，或者如果此 <code>BufferedImage</code> 没有关于其直接源的信息，则返回 <code>null</code>，或者如果此 <code>BufferedImage</code> 没有直接源，则返回一个空的 <code>Vector</code>。
 */
public Vector<RenderedImage> getSources() {
    return null;
}

/**
 * 返回由 {@link #getProperty(String) getProperty(String)} 认识的名称数组，或者如果没有任何名称被认识，则返回 <code>null</code>。
 * @return 一个包含 <code>getProperty(String)</code> 认识的所有属性名称的 <code>String</code> 数组；如果没有属性名称被认识，则返回 <code>null</code>。
 */
public String[] getPropertyNames() {
    if (properties == null || properties.isEmpty()) {
        return null;
    }
    final Set<String> keys = properties.keySet();
    return keys.toArray(new String[keys.size()]);
}

/**
 * 返回此 <code>BufferedImage</code> 的最小 x 坐标。这始终为零。
 * @return 此 <code>BufferedImage</code> 的最小 x 坐标。
 */
public int getMinX() {
    return raster.getMinX();
}

/**
 * 返回此 <code>BufferedImage</code> 的最小 y 坐标。这始终为零。
 * @return 此 <code>BufferedImage</code> 的最小 y 坐标。
 */
public int getMinY() {
    return raster.getMinY();
}

/**
 * 返回与此 <code>BufferedImage</code> 关联的 <code>SampleModel</code>。
 * @return 此 <code>BufferedImage</code> 的 <code>SampleModel</code>。
 */
public SampleModel getSampleModel() {
    return raster.getSampleModel();
}

/**
 * 返回 x 方向的平铺数。这始终为一。
 * @return x 方向的平铺数。
 */
public int getNumXTiles() {
    return 1;
}

/**
 * 返回 y 方向的平铺数。这始终为一。
 * @return y 方向的平铺数。
 */
public int getNumYTiles() {
    return 1;
}

/**
 * 返回 x 方向的最小平铺索引。这始终为零。
 * @return x 方向的最小平铺索引。
 */
public int getMinTileX() {
    return 0;
}

/**
 * 返回 y 方向的最小平铺索引。这始终为零。
 * @return y 方向的最小平铺索引。
 */
public int getMinTileY() {
    return 0;
}

/**
 * 返回平铺的宽度（以像素为单位）。
 * @return 平铺的宽度（以像素为单位）。
 */
public int getTileWidth() {
   return raster.getWidth();
}

/**
 * 返回平铺的高度（以像素为单位）。
 * @return 平铺的高度（以像素为单位）。
 */
public int getTileHeight() {
   return raster.getHeight();
}

/**
 * 返回平铺网格相对于原点的 x 偏移量，例如，平铺 (0,&nbsp;0) 的 x 坐标。这始终为零。
 * @return 平铺网格的 x 偏移量。
 */
public int getTileGridXOffset() {
    return raster.getSampleModelTranslateX();
}

/**
 * 返回平铺网格相对于原点的 y 偏移量，例如，平铺 (0,&nbsp;0) 的 y 坐标。这始终为零。
 * @return 平铺网格的 y 偏移量。
 */
public int getTileGridYOffset() {
    return raster.getSampleModelTranslateY();
}

/**
 * 返回平铺 (<code>tileX</code>,&nbsp;<code>tileY</code>)。注意，<code>tileX</code> 和 <code>tileY</code> 是平铺数组中的索引，而不是像素位置。返回的 <code>Raster</code> 是活动的，这意味着如果图像发生变化，它将被更新。
 * @param tileX 请求的平铺在平铺数组中的 x 索引
 * @param tileY 请求的平铺在平铺数组中的 y 索引
 * @return 由参数 <code>tileX</code> 和 <code>tileY</code> 定义的 <code>Raster</code> 平铺。
 * @exception ArrayIndexOutOfBoundsException 如果 <code>tileX</code> 和 <code>tileY</code> 都不等于 0
 */
public Raster getTile(int tileX, int tileY) {
    if (tileX == 0 && tileY == 0) {
        return raster;
    }
    throw new ArrayIndexOutOfBoundsException("BufferedImages only have"+
         " one tile with index 0,0");
}

/**
 * 返回图像作为一个大平铺。返回的 <code>Raster</code> 是图像数据的副本，如果图像发生变化，则不会更新。
 * @return 图像数据的副本 <code>Raster</code>。
 * @see #setData(Raster)
 */
public Raster getData() {

    // 提醒：如果 raster 是一个子平铺，这将分配一个全新的平铺。（它只复制请求的区域）
    // 我们应该做些更聪明的事情。
    int width = raster.getWidth();
    int height = raster.getHeight();
    int startX = raster.getMinX();
    int startY = raster.getMinY();
    WritableRaster wr =
       Raster.createWritableRaster(raster.getSampleModel(),
                         new Point(raster.getSampleModelTranslateX(),
                                   raster.getSampleModelTranslateY()));

    Object tdata = null;

    for (int i = startY; i < startY+height; i++)  {
        tdata = raster.getDataElements(startX,i,width,1,tdata);
        wr.setDataElements(startX,i,width,1, tdata);
    }
    return wr;
}

/**
 * 计算并返回 <code>BufferedImage</code> 的任意区域。返回的 <code>Raster</code> 是图像数据的副本，如果图像发生变化，则不会更新。
 * @param rect 要返回的 <code>BufferedImage</code> 的区域。
 * @return 指定区域的 <code>BufferedImage</code> 图像数据的副本 <code>Raster</code>。
 * @see #setData(Raster)
 */
public Raster getData(Rectangle rect) {
    SampleModel sm = raster.getSampleModel();
    SampleModel nsm = sm.createCompatibleSampleModel(rect.width,
                                                     rect.height);
    WritableRaster wr = Raster.createWritableRaster(nsm,
                                                  rect.getLocation());
    int width = rect.width;
    int height = rect.height;
    int startX = rect.x;
    int startY = rect.y;

    Object tdata = null;

    for (int i = startY; i < startY+height; i++)  {
        tdata = raster.getDataElements(startX,i,width,1,tdata);
        wr.setDataElements(startX,i,width,1, tdata);
    }
    return wr;
}

/**
 * 计算 <code>BufferedImage</code> 的任意矩形区域，并将其复制到指定的 <code>WritableRaster</code> 中。要计算的区域由指定的 <code>WritableRaster</code> 的边界确定。指定的 <code>WritableRaster</code> 必须具有与此图像兼容的 <code>SampleModel</code>。如果 <code>outRaster</code> 为 <code>null</code>，则创建一个适当的 <code>WritableRaster</code>。
 * @param outRaster 用于保存返回的图像部分的 <code>WritableRaster</code>，或者 <code>null</code>
 * @return 一个指向提供的或创建的 <code>WritableRaster</code> 的引用。
 */
public WritableRaster copyData(WritableRaster outRaster) {
    if (outRaster == null) {
        return (WritableRaster) getData();
    }
    int width = outRaster.getWidth();
    int height = outRaster.getHeight();
    int startX = outRaster.getMinX();
    int startY = outRaster.getMinY();

    Object tdata = null;

    for (int i = startY; i < startY+height; i++)  {
        tdata = raster.getDataElements(startX,i,width,1,tdata);
        outRaster.setDataElements(startX,i,width,1, tdata);
    }

    return outRaster;
}

/**
 * 将图像的一个矩形区域设置为指定 <code>Raster</code> <code>r</code> 的内容，假设 <code>Raster</code> 与 <code>BufferedImage</code> 在相同的坐标空间中。操作将被裁剪到 <code>BufferedImage</code> 的边界。
 * @param r 指定的 <code>Raster</code>
 * @see #getData
 * @see #getData(Rectangle)
 */
public void setData(Raster r) {
    int width = r.getWidth();
    int height = r.getHeight();
    int startX = r.getMinX();
    int startY = r.getMinY();

    int[] tdata = null;

    // 裁剪到当前 Raster
    Rectangle rclip = new Rectangle(startX, startY, width, height);
    Rectangle bclip = new Rectangle(0, 0, raster.width, raster.height);
    Rectangle intersect = rclip.intersection(bclip);
    if (intersect.isEmpty()) {
        return;
    }
    width = intersect.width;
    height = intersect.height;
    startX = intersect.x;
    startY = intersect.y;

    // 提醒：如果 Rasters 兼容，使用 get/setDataElements 以提高速度
    for (int i = startY; i < startY+height; i++)  {
        tdata = r.getPixels(startX,i,width,1,tdata);
        raster.setPixels(startX,i,width,1, tdata);
    }
}

/**
 * 添加一个平铺观察者。如果观察者已存在，则它会收到多次通知。
 * @param to 指定的 {@link TileObserver}
 */
public void addTileObserver (TileObserver to) {
}

/**
 * 移除一个平铺观察者。如果观察者未注册，则不会发生任何事情。如果观察者注册了多次通知，则现在注册的次数减少一次。
 * @param to 指定的 <code>TileObserver</code>。
 */
public void removeTileObserver (TileObserver to) {
}

/**
 * 返回指定索引的平铺是否当前被签出用于写入。
 * @param tileX 平铺的 x 索引。
 * @param tileY 平铺的 y 索引。
 * @return 如果指定索引的平铺被签出用于写入，则返回 <code>true</code>；否则返回 <code>false</code>。
 * @exception ArrayIndexOutOfBoundsException 如果 <code>tileX</code> 和 <code>tileY</code> 都不等于 0
 */
public boolean isTileWritable (int tileX, int tileY) {
    if (tileX == 0 && tileY == 0) {
        return true;
    }
    throw new IllegalArgumentException("Only 1 tile in image");
}

/**
 * 返回一个 {@link Point} 对象数组，指示哪些平铺被签出用于写入。如果没有平铺被签出用于写入，则返回 <code>null</code>。
 * @return 一个 <code>Point</code> 数组，指示被签出用于写入的平铺，或者如果没有平铺被签出用于写入，则返回 <code>null</code>。
 */
public Point[] getWritableTileIndices() {
    Point[] p = new Point[1];
    p[0] = new Point(0, 0);

    return p;
}

/**
 * 返回是否有平铺被签出用于写入。在语义上等同于
 * <pre>
 * (getWritableTileIndices() != null).
 * </pre>
 * @return 如果有任何平铺被签出用于写入，则返回 <code>true</code>；否则返回 <code>false</code>。
 */
public boolean hasTileWriters () {
    return true;
}

/**
 * 签出一个平铺用于写入。当平铺从没有写入者变为有一个写入者时，所有注册的 <code>TileObservers</code> 都会收到通知。
 * @param tileX 平铺的 x 索引
 * @param tileY 平铺的 y 索引
 * @return 一个 <code>WritableRaster</code>，表示要签出用于写入的平铺，由指定的索引指示。
 */
public WritableRaster getWritableTile (int tileX, int tileY) {
    return raster;
}

/**
 * 放弃对平铺的写入权限。如果调用者继续写入平铺，结果是未定义的。此方法的调用应仅与 {@link #getWritableTile(int, int) getWritableTile(int, int)} 的调用成对出现。任何其他情况将导致未定义的结果。当平铺从有一个写入者变为没有写入者时，所有注册的 <code>TileObservers</code> 都会收到通知。
 * @param tileX 平铺的 x 索引
 * @param tileY 平铺的 y 索引
 */
public void releaseWritableTile (int tileX, int tileY) {
}

/**
 * 返回透明度。返回 OPAQUE、BITMASK 或 TRANSLUCENT。
 * @return 此 <code>BufferedImage</code> 的透明度。
 * @see Transparency#OPAQUE
 * @see Transparency#BITMASK
 * @see Transparency#TRANSLUCENT
 * @since 1.5
 */
public int getTransparency() {
    return colorModel.getTransparency();
}
}
