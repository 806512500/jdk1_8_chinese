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

import java.awt.image.ImageProducer;
import java.awt.image.ImageObserver;
import java.awt.image.ImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.ReplicateScaleFilter;

import sun.awt.image.SurfaceManager;


/**
 * 抽象类 <code>Image</code> 是所有表示图形图像的类的超类。图像必须以平台特定的方式获取。
 *
 * @author      Sami Shaio
 * @author      Arthur van Hoff
 * @since       JDK1.0
 */
public abstract class Image {

    /**
     * 方便对象；我们可以使用这个单个静态对象来处理所有不创建自己的图像功能的图像；它持有默认的（非加速的）属性。
     */
    private static ImageCapabilities defaultImageCaps =
        new ImageCapabilities(false);

    /**
     * 加速此图像的优先级。子类可以自由设置不同的默认优先级，应用程序也可以自由设置特定图像的优先级，通过
     * <code>setAccelerationPriority(float)</code> 方法。
     * @since 1.5
     */
    protected float accelerationPriority = .5f;

    /**
     * 确定图像的宽度。如果宽度尚不可知，此方法返回 <code>-1</code>，并且指定的
     * <code>ImageObserver</code> 对象稍后将收到通知。
     * @param     observer   等待图像加载的对象。
     * @return    该图像的宽度，或 <code>-1</code>
     *                   如果宽度尚不可知。
     * @see       java.awt.Image#getHeight
     * @see       java.awt.image.ImageObserver
     */
    public abstract int getWidth(ImageObserver observer);

    /**
     * 确定图像的高度。如果高度尚不可知，此方法返回 <code>-1</code>，并且指定的
     * <code>ImageObserver</code> 对象稍后将收到通知。
     * @param     observer   等待图像加载的对象。
     * @return    该图像的高度，或 <code>-1</code>
     *                   如果高度尚不可知。
     * @see       java.awt.Image#getWidth
     * @see       java.awt.image.ImageObserver
     */
    public abstract int getHeight(ImageObserver observer);

    /**
     * 获取生成图像像素的对象。
     * 此方法由图像过滤类和执行图像转换和缩放的方法调用。
     * @return     生成此图像像素的图像生成器。
     * @see        java.awt.image.ImageProducer
     */
    public abstract ImageProducer getSource();

    /**
     * 创建一个用于绘制到离屏图像的图形上下文。
     * 此方法只能用于离屏图像。
     * @return  用于绘制离屏图像的图形上下文。
     * @exception UnsupportedOperationException 如果调用的是非离屏图像。
     * @see     java.awt.Graphics
     * @see     java.awt.Component#createImage(int, int)
     */
    public abstract Graphics getGraphics();

    /**
     * 按名称获取此图像的属性。
     * <p>
     * 个别属性名称由各种图像格式定义。如果特定图像未定义某个属性，此方法返回
     * <code>UndefinedProperty</code> 对象。
     * <p>
     * 如果此图像的属性尚不可知，此方法返回 <code>null</code>，并且
     * <code>ImageObserver</code> 对象稍后将收到通知。
     * <p>
     * 属性名称 <code>"comment"</code> 应用于存储可呈现给应用程序的图像描述、来源或作者。
     * @param       name   属性名称。
     * @param       observer   等待此图像加载的对象。
     * @return      命名属性的值。
     * @throws      NullPointerException 如果属性名称为 null。
     * @see         java.awt.image.ImageObserver
     * @see         java.awt.Image#UndefinedProperty
     */
    public abstract Object getProperty(String name, ImageObserver observer);

    /**
     * 每当获取未为特定图像定义的属性时，应返回 <code>UndefinedProperty</code> 对象。
     */
    public static final Object UndefinedProperty = new Object();

    /**
     * 创建此图像的缩放版本。
     * 返回一个新的 <code>Image</code> 对象，该对象默认将图像渲染为指定的 <code>width</code> 和
     * <code>height</code>。即使原始源图像已完全加载，新的 <code>Image</code> 对象也可能异步加载。
     *
     * <p>
     *
     * 如果 <code>width</code>
     * 或 <code>height</code> 是负数，则将替换一个值以保持原始图像尺寸的纵横比。如果
     * <code>width</code> 和 <code>height</code> 均为负数，则使用原始图像尺寸。
     *
     * @param width 要缩放的图像宽度。
     * @param height 要缩放的图像高度。
     * @param hints 指示图像重采样类型标志。
     * @return     缩放后的图像版本。
     * @exception IllegalArgumentException 如果 <code>width</code>
     *             或 <code>height</code> 为零。
     * @see        java.awt.Image#SCALE_DEFAULT
     * @see        java.awt.Image#SCALE_FAST
     * @see        java.awt.Image#SCALE_SMOOTH
     * @see        java.awt.Image#SCALE_REPLICATE
     * @see        java.awt.Image#SCALE_AREA_AVERAGING
     * @since      JDK1.1
     */
    public Image getScaledInstance(int width, int height, int hints) {
        ImageFilter filter;
        if ((hints & (SCALE_SMOOTH | SCALE_AREA_AVERAGING)) != 0) {
            filter = new AreaAveragingScaleFilter(width, height);
        } else {
            filter = new ReplicateScaleFilter(width, height);
        }
        ImageProducer prod;
        prod = new FilteredImageSource(getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(prod);
    }

    /**
     * 使用默认的图像缩放算法。
     * @since JDK1.1
     */
    public static final int SCALE_DEFAULT = 1;

    /**
     * 选择一个优先考虑缩放速度而非缩放后图像平滑度的图像缩放算法。
     * @since JDK1.1
     */
    public static final int SCALE_FAST = 2;

    /**
     * 选择一个优先考虑图像平滑度而非缩放速度的图像缩放算法。
     * @since JDK1.1
     */
    public static final int SCALE_SMOOTH = 4;

    /**
     * 使用 <code>ReplicateScaleFilter</code> 类中的图像缩放算法。
     * <code>Image</code> 对象可以自由地替换一个执行相同算法但更高效地集成到工具包提供的图像基础设施中的不同过滤器。
     * @see        java.awt.image.ReplicateScaleFilter
     * @since      JDK1.1
     */
    public static final int SCALE_REPLICATE = 8;

    /**
     * 使用区域平均图像缩放算法。图像对象可以自由地替换一个执行相同算法但更高效地集成到工具包提供的图像基础设施中的不同过滤器。
     * @see java.awt.image.AreaAveragingScaleFilter
     * @since JDK1.1
     */
    public static final int SCALE_AREA_AVERAGING = 16;

    /**
     * 刷新此 Image 对象正在使用的所有可重建资源。
     * 这包括用于渲染到屏幕的任何缓存像素数据以及可以重新创建的任何系统资源。
     * 图像被重置为类似于首次创建时的状态，因此如果再次渲染，图像数据将需要重新创建或从其源重新获取。
     * <p>
     * 该方法对特定类型 Image 对象的影响示例：
     * <ul>
     * <li>
     * BufferedImage 对象保留存储其像素的主要 Raster，但刷新有关这些像素的任何缓存信息，例如上传到显示硬件以加速 blits 的副本。
     * <li>
     * 通过 Component 方法创建的 Image 对象（这些方法接受宽度和高度）保留其主要像素缓冲区，但释放所有缓存信息，类似于 BufferedImage 对象。
     * <li>
     * VolatileImage 对象释放其所有像素资源，包括通常存储在显示硬件上的主要副本，这些资源稀缺。这些对象可以使用其
     * {@link java.awt.image.VolatileImage#validate validate}
     * 方法稍后恢复。
     * <li>
     * 由 Toolkit 和 Component 类从文件、URL 或由 {@link ImageProducer} 生成的 Image 对象被卸载并释放所有本地资源。
     * 这些对象在需要时可以从其原始源重新加载，就像首次创建时一样。
     * </ul>
     */
    public void flush() {
        if (surfaceManager != null) {
            surfaceManager.flush();
        }
    }

    /**
     * 返回一个 ImageCapabilities 对象，该对象可以查询此
     * Image 在指定 GraphicsConfiguration 上的能力。
     * 这允许程序员获取有关他们创建的特定 Image
     * 对象的更多运行时信息。例如，用户可能创建了一个 BufferedImage，但系统可能
     * 没有足够的视频内存来在给定的 GraphicsConfiguration 上创建该大小的图像，因此尽管该对象
     * 通常可以加速，但在该 GraphicsConfiguration 上不具备该能力。
     * @param gc 一个 <code>GraphicsConfiguration</code> 对象。此参数的值为 null
     * 将导致获取默认 <code>GraphicsConfiguration</code> 的图像能力。
     * @return 一个 <code>ImageCapabilities</code> 对象，其中包含此 <code>Image</code> 在指定
     * GraphicsConfiguration 上的能力。
     * @see java.awt.image.VolatileImage#getCapabilities()
     * VolatileImage.getCapabilities()
     * @since 1.5
     */
    public ImageCapabilities getCapabilities(GraphicsConfiguration gc) {
        if (surfaceManager != null) {
            return surfaceManager.getCapabilities(gc);
        }
        // 注意：这只是在没有来自 surfaceManager 的更具体信息时返回的默认对象。
        // Image 的子类应覆盖此方法或确保始终有一个非空的 SurfaceManager
        // 以返回适合其给定子类类型的 ImageCapabilities 对象。
        return defaultImageCaps;
    }

    /**
     * 为该图像设置一个关于加速重要性的提示。
     * 该优先级提示用于与其他 Image 对象的优先级进行比较，以确定如何使用稀缺的加速
     * 资源，如视频内存。当且仅当可能加速此 Image 时，如果没有足够的资源可用
     * 以提供该加速，但可以通过去加速优先级较低的其他图像来释放足够的资源，则该其他
     * Image 可能会被去加速以优先于此图像。具有相同优先级的图像按先到先得的原则占用资源。
     * @param priority 一个介于 0 和 1 之间的值，其中较高的值表示加速更重要。值 0
     * 表示此 Image 永远不应被加速。其他值仅用于确定相对于其他
     * Images 的加速优先级。
     * @throws IllegalArgumentException 如果 <code>priority</code> 小于零或大于 1。
     * @since 1.5
     */
    public void setAccelerationPriority(float priority) {
        if (priority < 0 || priority > 1) {
            throw new IllegalArgumentException("Priority must be a value " +
                                               "between 0 and 1, inclusive");
        }
        accelerationPriority = priority;
        if (surfaceManager != null) {
            surfaceManager.setAccelerationPriority(accelerationPriority);
        }
    }

    /**
     * 返回加速优先级提示的当前值。
     * @see #setAccelerationPriority(float priority) setAccelerationPriority
     * @return 介于 0 和 1 之间的值，表示当前优先级值
     * @since 1.5
     */
    public float getAccelerationPriority() {
        return accelerationPriority;
    }

    SurfaceManager surfaceManager;

    static {
        SurfaceManager.setImageAccessor(new SurfaceManager.ImageAccessor() {
            public SurfaceManager getSurfaceManager(Image img) {
                return img.surfaceManager;
            }
            public void setSurfaceManager(Image img, SurfaceManager mgr) {
                img.surfaceManager = mgr;
            }
        });
    }
}
