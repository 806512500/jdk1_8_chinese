
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

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;

import sun.awt.image.SunVolatileImage;

/**
 * <code>GraphicsConfiguration</code> 类描述了图形目标（如打印机或显示器）的特性。
 * 一个图形设备可以有多个 <code>GraphicsConfiguration</code> 对象，表示不同的绘图模式或功能。
 * 对应的本机结构在不同的平台上会有所不同。例如，在 X11 窗口系统中，
 * 每个视觉效果都是一个不同的 <code>GraphicsConfiguration</code>。
 * 在 Microsoft Windows 上，<code>GraphicsConfiguration</code> 表示当前分辨率和颜色深度下可用的 PixelFormats。
 * <p>
 * 在虚拟设备多屏环境中，桌面区域可能跨越多个物理屏幕设备，<code>GraphicsConfiguration</code> 对象的边界
 * 是相对于虚拟坐标系统的。在设置组件的位置时，使用 {@link #getBounds() getBounds} 获取所需
 * <code>GraphicsConfiguration</code> 的边界，并使用 <code>GraphicsConfiguration</code> 的坐标偏移位置，
 * 以下代码示例说明了这一点：
 * </p>
 *
 * <pre>
 *      Frame f = new Frame(gc);  // 其中 gc 是一个 GraphicsConfiguration
 *      Rectangle bounds = gc.getBounds();
 *      f.setLocation(10 + bounds.x, 10 + bounds.y); </pre>
 *
 * <p>
 * 要确定您的环境是否为虚拟设备环境，可以对系统中的所有 <code>GraphicsConfiguration</code> 对象调用 <code>getBounds</code>。
 * 如果返回的边界中任何一个的原点不是 (0,&nbsp;0)，则您的环境是虚拟设备环境。
 *
 * <p>
 * 您还可以使用 <code>getBounds</code> 来确定虚拟设备的边界。为此，首先对系统中的所有
 * <code>GraphicsConfiguration</code> 对象调用 <code>getBounds</code>。然后计算所有返回的边界
 * 的并集。并集即为虚拟设备的边界。以下代码示例计算了虚拟设备的边界。
 *
 * <pre>{@code
 *      Rectangle virtualBounds = new Rectangle();
 *      GraphicsEnvironment ge = GraphicsEnvironment.
 *              getLocalGraphicsEnvironment();
 *      GraphicsDevice[] gs =
 *              ge.getScreenDevices();
 *      for (int j = 0; j < gs.length; j++) {
 *          GraphicsDevice gd = gs[j];
 *          GraphicsConfiguration[] gc =
 *              gd.getConfigurations();
 *          for (int i=0; i < gc.length; i++) {
 *              virtualBounds =
 *                  virtualBounds.union(gc[i].getBounds());
 *          }
 *      } }</pre>
 *
 * @see Window
 * @see Frame
 * @see GraphicsEnvironment
 * @see GraphicsDevice
 */
/*
 * REMIND:  如何处理功能？
 * 设备的功能可以通过枚举可能的功能并检查 <code>GraphicsConfiguration</code>
 * 是否实现了该功能的接口来确定。
 *
 */


public abstract class GraphicsConfiguration {

    private static BufferCapabilities defaultBufferCaps;
    private static ImageCapabilities defaultImageCaps;

    /**
     * 这是一个抽象类，不能直接实例化。实例必须通过合适的工厂或查询方法获得。
     *
     * @see GraphicsDevice#getConfigurations
     * @see GraphicsDevice#getDefaultConfiguration
     * @see GraphicsDevice#getBestConfiguration
     * @see Graphics2D#getDeviceConfiguration
     */
    protected GraphicsConfiguration() {
    }

    /**
     * 返回与此 <code>GraphicsConfiguration</code> 关联的 {@link GraphicsDevice}。
     * @return 一个与该 <code>GraphicsConfiguration</code> 关联的 <code>GraphicsDevice</code> 对象。
     */
    public abstract GraphicsDevice getDevice();

    /**
     * 返回一个与该 <code>GraphicsConfiguration</code> 兼容的数据布局和颜色模型的 {@link BufferedImage}。
     * 此方法与设备的内存映射无关。返回的 <code>BufferedImage</code> 具有与本机设备配置最接近的布局和颜色模型，
     * 因此可以最优地传输到此设备。
     * @param width 返回的 <code>BufferedImage</code> 的宽度
     * @param height 返回的 <code>BufferedImage</code> 的高度
     * @return 一个与该 <code>GraphicsConfiguration</code> 兼容的数据布局和颜色模型的 <code>BufferedImage</code>。
     */
    public BufferedImage createCompatibleImage(int width, int height) {
        ColorModel model = getColorModel();
        WritableRaster raster =
            model.createCompatibleWritableRaster(width, height);
        return new BufferedImage(model, raster,
                                 model.isAlphaPremultiplied(), null);
    }

    /**
     * 返回一个支持指定透明度且与该 <code>GraphicsConfiguration</code> 兼容的数据布局和颜色模型的 <code>BufferedImage</code>。
     * 此方法与设备的内存映射无关。返回的 <code>BufferedImage</code> 具有可以最优地传输到具有此 <code>GraphicsConfiguration</code> 的设备的布局和颜色模型。
     * @param width 返回的 <code>BufferedImage</code> 的宽度
     * @param height 返回的 <code>BufferedImage</code> 的高度
     * @param transparency 指定的透明度模式
     * @return 一个与该 <code>GraphicsConfiguration</code> 兼容的数据布局和颜色模型且支持指定透明度的 <code>BufferedImage</code>。
     * @throws IllegalArgumentException 如果透明度不是有效值
     * @see Transparency#OPAQUE
     * @see Transparency#BITMASK
     * @see Transparency#TRANSLUCENT
     */
    public BufferedImage createCompatibleImage(int width, int height,
                                               int transparency)
    {
        if (getColorModel().getTransparency() == transparency) {
            return createCompatibleImage(width, height);
        }

        ColorModel cm = getColorModel(transparency);
        if (cm == null) {
            throw new IllegalArgumentException("未知透明度: " +
                                               transparency);
        }
        WritableRaster wr = cm.createCompatibleWritableRaster(width, height);
        return new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), null);
    }


    /**
     * 返回一个与该 <code>GraphicsConfiguration</code> 兼容的数据布局和颜色模型的 {@link VolatileImage}。
     * 返回的 <code>VolatileImage</code> 可能以最适合底层图形设备的方式存储数据，因此可能受益于平台特定的渲染加速。
     * @param width 返回的 <code>VolatileImage</code> 的宽度
     * @param height 返回的 <code>VolatileImage</code> 的高度
     * @return 一个与该 <code>GraphicsConfiguration</code> 兼容的数据布局和颜色模型的 <code>VolatileImage</code>。
     * @see Component#createVolatileImage(int, int)
     * @since 1.4
     */
    public VolatileImage createCompatibleVolatileImage(int width, int height) {
        VolatileImage vi = null;
        try {
            vi = createCompatibleVolatileImage(width, height,
                                               null, Transparency.OPAQUE);
        } catch (AWTException e) {
            // 不应该发生：我们传递的是 null caps
            assert false;
        }
        return vi;
    }

    /**
     * 返回一个与该 <code>GraphicsConfiguration</code> 兼容的数据布局和颜色模型的 {@link VolatileImage}。
     * 返回的 <code>VolatileImage</code> 可能以最适合底层图形设备的方式存储数据，因此可能受益于平台特定的渲染加速。
     * @param width 返回的 <code>VolatileImage</code> 的宽度
     * @param height 返回的 <code>VolatileImage</code> 的高度
     * @param transparency 指定的透明度模式
     * @return 一个与该 <code>GraphicsConfiguration</code> 兼容的数据布局和颜色模型的 <code>VolatileImage</code>。
     * @throws IllegalArgumentException 如果透明度不是有效值
     * @see Transparency#OPAQUE
     * @see Transparency#BITMASK
     * @see Transparency#TRANSLUCENT
     * @see Component#createVolatileImage(int, int)
     * @since 1.5
     */
    public VolatileImage createCompatibleVolatileImage(int width, int height,
                                                       int transparency)
    {
        VolatileImage vi = null;
        try {
            vi = createCompatibleVolatileImage(width, height, null, transparency);
        } catch (AWTException e) {
            // 不应该发生：我们传递的是 null caps
            assert false;
        }
        return vi;
    }

    /**
     * 返回一个与该 <code>GraphicsConfiguration</code> 兼容的数据布局和颜色模型的 {@link VolatileImage}，使用指定的图像功能。
     * 如果 <code>caps</code> 参数为 null，则忽略该参数，此方法将创建一个不考虑 <code>ImageCapabilities</code> 约束的 VolatileImage。
     *
     * 返回的 <code>VolatileImage</code> 具有与本机设备配置最接近的布局和颜色模型，因此可以最优地传输到此设备。
     * @return 一个与该 <code>GraphicsConfiguration</code> 兼容的数据布局和颜色模型的 <code>VolatileImage</code>。
     * @param width 返回的 <code>VolatileImage</code> 的宽度
     * @param height 返回的 <code>VolatileImage</code> 的高度
     * @param caps 图像功能
     * @exception AWTException 如果提供的图像功能无法满足此图形配置
     * @since 1.4
     */
    public VolatileImage createCompatibleVolatileImage(int width, int height,
        ImageCapabilities caps) throws AWTException
    {
        return createCompatibleVolatileImage(width, height, caps,
                                             Transparency.OPAQUE);
    }

    /**
     * 返回一个与该 <code>GraphicsConfiguration</code> 兼容的数据布局和颜色模型的 {@link VolatileImage}，使用指定的图像功能和透明度值。
     * 如果 <code>caps</code> 参数为 null，则忽略该参数，此方法将创建一个不考虑 <code>ImageCapabilities</code> 约束的 VolatileImage。
     *
     * 返回的 <code>VolatileImage</code> 具有与本机设备配置最接近的布局和颜色模型，因此可以最优地传输到此设备。
     * @param width 返回的 <code>VolatileImage</code> 的宽度
     * @param height 返回的 <code>VolatileImage</code> 的高度
     * @param caps 图像功能
     * @param transparency 指定的透明度模式
     * @return 一个与该 <code>GraphicsConfiguration</code> 兼容的数据布局和颜色模型的 <code>VolatileImage</code>。
     * @see Transparency#OPAQUE
     * @see Transparency#BITMASK
     * @see Transparency#TRANSLUCENT
     * @throws IllegalArgumentException 如果透明度不是有效值
     * @exception AWTException 如果提供的图像功能无法满足此图形配置
     * @see Component#createVolatileImage(int, int)
     * @since 1.5
     */
    public VolatileImage createCompatibleVolatileImage(int width, int height,
        ImageCapabilities caps, int transparency) throws AWTException
    {
        VolatileImage vi =
            new SunVolatileImage(this, width, height, transparency, caps);
        if (caps != null && caps.isAccelerated() &&
            !vi.getCapabilities().isAccelerated())
        {
            throw new AWTException("提供的图像功能无法满足此图形配置。");
        }
        return vi;
    }

    /**
     * 返回与此 <code>GraphicsConfiguration</code> 关联的 {@link ColorModel}。
     * @return 一个与此 <code>GraphicsConfiguration</code> 关联的 <code>ColorModel</code> 对象。
     */
    public abstract ColorModel getColorModel();

    /**
     * 返回与此 <code>GraphicsConfiguration</code> 关联的且支持指定透明度的 <code>ColorModel</code>。
     * @param transparency 指定的透明度模式
     * @return 一个与此 <code>GraphicsConfiguration</code> 关联的且支持指定透明度的 <code>ColorModel</code> 对象，如果透明度不是有效值则返回 null。
     * @see Transparency#OPAQUE
     * @see Transparency#BITMASK
     * @see Transparency#TRANSLUCENT
     */
    public abstract ColorModel getColorModel(int transparency);

    /**
     * 返回此 <code>GraphicsConfiguration</code> 的默认 {@link AffineTransform}。此
     * <code>AffineTransform</code> 通常是大多数正常屏幕的单位变换。默认的 <code>AffineTransform</code>
     * 将坐标映射到设备上，使得 72 个用户空间坐标单位大约等于设备空间中的 1 英寸。可以使用规范化变换来使这种映射更精确。
     * 在屏幕和打印机设备的坐标空间中，由默认 <code>AffineTransform</code> 定义的坐标空间的原点位于目标区域的左上角，
     * X 坐标向右增加，Y 坐标向下增加。对于不与设备关联的图像缓冲区，例如不是通过 <code>createCompatibleImage</code>
     * 创建的图像缓冲区，此 <code>AffineTransform</code> 是单位变换。
     * @return 此 <code>GraphicsConfiguration</code> 的默认 <code>AffineTransform</code>。
     */
    public abstract AffineTransform getDefaultTransform();


                /**
     *
     * 返回一个可以与 <code>GraphicsConfiguration</code> 的默认 <code>AffineTransform</code>
     * 连接的 <code>AffineTransform</code>，使得用户空间中的 72 个单位等于设备空间中的 1 英寸。
     * <p>
     * 对于特定的 {@link Graphics2D}，g，可以使用以下伪代码重置变换以创建这样的映射：
     * <pre>
     *      GraphicsConfiguration gc = g.getDeviceConfiguration();
     *
     *      g.setTransform(gc.getDefaultTransform());
     *      g.transform(gc.getNormalizingTransform());
     * </pre>
     * 请注意，有时这个 <code>AffineTransform</code> 是单位变换，例如对于打印机或元文件输出，
     * 并且这个 <code>AffineTransform</code> 的准确性仅取决于底层系统提供的信息。对于不与设备关联的图像缓冲区，
     * 例如不是通过 <code>createCompatibleImage</code> 创建的，这个 <code>AffineTransform</code> 是单位变换，
     * 因为没有有效的距离测量。
     * @return 一个可以连接到默认 <code>AffineTransform</code> 的 <code>AffineTransform</code>，
     * 使得用户空间中的 72 个单位映射到设备空间中的 1 英寸。
     */
    public abstract AffineTransform getNormalizingTransform();

    /**
     * 返回 <code>GraphicsConfiguration</code> 在设备坐标中的边界。在具有虚拟设备的多屏幕环境中，
     * 边界的 X 或 Y 原点可以是负数。
     * @return 由这个 <code>GraphicsConfiguration</code> 覆盖的区域的边界。
     * @since 1.3
     */
    public abstract Rectangle getBounds();

    private static class DefaultBufferCapabilities extends BufferCapabilities {
        public DefaultBufferCapabilities(ImageCapabilities imageCaps) {
            super(imageCaps, imageCaps, null);
        }
    }

    /**
     * 返回此 <code>GraphicsConfiguration</code> 的缓冲区功能。
     * @return 此图形配置对象的缓冲区功能
     * @since 1.4
     */
    public BufferCapabilities getBufferCapabilities() {
        if (defaultBufferCaps == null) {
            defaultBufferCaps = new DefaultBufferCapabilities(
                getImageCapabilities());
        }
        return defaultBufferCaps;
    }

    /**
     * 返回此 <code>GraphicsConfiguration</code> 的图像功能。
     * @return 此图形配置对象的图像功能
     * @since 1.4
     */
    public ImageCapabilities getImageCapabilities() {
        if (defaultImageCaps == null) {
            defaultImageCaps = new ImageCapabilities(false);
        }
        return defaultImageCaps;
    }

    /**
     * 返回此 {@code GraphicsConfiguration} 是否支持
     * {@link GraphicsDevice.WindowTranslucency#PERPIXEL_TRANSLUCENT
     * PERPIXEL_TRANSLUCENT} 类型的透明度。
     *
     * @return 给定的 GraphicsConfiguration 是否支持透明度效果。
     *
     * @see Window#setBackground(Color)
     *
     * @since 1.7
     */
    public boolean isTranslucencyCapable() {
        // 在子类中重写
        return false;
    }
}
