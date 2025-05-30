/*
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Toolkit;
import java.awt.Transparency;

/**
 * VolatileImage 是一种可能在任何时候由于超出应用程序控制的情况（例如，操作系统或其他应用程序引起的情况）而丢失其内容的图像。由于硬件加速的潜力，VolatileImage 对象在某些平台上可能具有显著的性能优势。
 * <p>
 * 图像的绘制表面（图像内容实际存储的内存）可能会丢失或失效，导致该内存中的内容消失。因此，需要恢复或重新创建绘制表面，并重新渲染该表面的内容。VolatileImage 提供了一个接口，允许用户检测这些问题并在发生时修复它们。
 * <p>
 * 创建 VolatileImage 对象时，可能会分配有限的系统资源，如视频内存（VRAM），以支持该图像。当 VolatileImage 对象不再使用时，它可能会被垃圾回收，这些系统资源将被释放，但这个过程没有保证的时间。创建许多 VolatileImage 对象的应用程序（例如，调整大小的窗口可能会在大小变化时强制重新创建其后备缓冲区）可能会因为旧对象尚未从系统中移除而耗尽新 VolatileImage 对象的最佳系统资源。虽然仍然可以创建新的 VolatileImage 对象，但它们的性能可能不如在加速内存中创建的对象。可以随时调用 flush 方法以主动释放 VolatileImage 使用的资源，使其不会妨碍后续 VolatileImage 对象的加速。通过这种方式，应用程序可以对过时的 VolatileImage 对象占用的资源状态有更多的控制。
 * <p>
 * 此图像不应直接子类化，而应通过使用 {@link java.awt.Component#createVolatileImage(int, int) Component.createVolatileImage} 或
 * {@link java.awt.GraphicsConfiguration#createCompatibleVolatileImage(int, int) GraphicsConfiguration.createCompatibleVolatileImage(int, int)} 方法创建。
 * <P>
 * 以下是一个使用 VolatileImage 对象的示例：
 * <pre>
 * // 图像创建
 * VolatileImage vImg = createVolatileImage(w, h);
 *
 *
 * // 渲染到图像
 * void renderOffscreen() {
 *      do {
 *          if (vImg.validate(getGraphicsConfiguration()) ==
 *              VolatileImage.IMAGE_INCOMPATIBLE)
 *          {
 *              // 旧的 vImg 与新的 GraphicsConfig 不兼容；重新创建它
 *              vImg = createVolatileImage(w, h);
 *          }
 *          Graphics2D g = vImg.createGraphics();
 *          //
 *          // 各种渲染命令...
 *          //
 *          g.dispose();
 *      } while (vImg.contentsLost());
 * }
 *
 *
 * // 从图像复制（这里，gScreen 是屏幕窗口的 Graphics 对象）
 * do {
 *      int returnCode = vImg.validate(getGraphicsConfiguration());
 *      if (returnCode == VolatileImage.IMAGE_RESTORED) {
 *          // 内容需要恢复
 *          renderOffscreen();      // 恢复内容
 *      } else if (returnCode == VolatileImage.IMAGE_INCOMPATIBLE) {
 *          // 旧的 vImg 与新的 GraphicsConfig 不兼容；重新创建它
 *          vImg = createVolatileImage(w, h);
 *          renderOffscreen();
 *      }
 *      gScreen.drawImage(vImg, 0, 0, this);
 * } while (vImg.contentsLost());
 * </pre>
 * <P>
 * 请注意，此类继承自 {@link Image} 类，该类包含接受 {@link ImageObserver} 参数的方法，用于在从潜在的 {@link ImageProducer} 接收信息时进行异步通知。由于此 <code>VolatileImage</code> 不是从异步源加载的，因此接受 <code>ImageObserver</code> 参数的各种方法的行为将如同数据已经从 <code>ImageProducer</code> 获取一样。具体来说，这意味着这些方法的返回值永远不会表示信息尚未可用，并且在这些方法中使用的 <code>ImageObserver</code> 从未需要记录用于异步回调通知。
 * @since 1.4
 */
public abstract class VolatileImage extends Image implements Transparency
{

    // validate() 方法的返回代码

    /**
     * 验证后的图像可以立即使用。
     */
    public static final int IMAGE_OK = 0;

    /**
     * 验证后的图像已被恢复，现在可以使用。请注意，恢复会导致图像内容丢失。
     */
    public static final int IMAGE_RESTORED = 1;

    /**
     * 验证后的图像与提供的 <code>GraphicsConfiguration</code> 对象不兼容，应适当重新创建。在从 <code>validate</code> 接收到此返回代码后继续使用该图像将导致未定义的行为。
     */
    public static final int IMAGE_INCOMPATIBLE = 2;

    /**
     * 返回此对象的静态快照图像。返回的 <code>BufferedImage</code> 仅在请求时与 <code>VolatileImage</code> 保持一致，不会随 <code>VolatileImage</code> 的任何未来更改而更新。
     * @return 此 <code>VolatileImage</code> 的 {@link BufferedImage} 表示形式
     * @see BufferedImage
     */
    public abstract BufferedImage getSnapshot();

    /**
     * 返回 <code>VolatileImage</code> 的宽度。
     * @return 此 <code>VolatileImage</code> 的宽度。
     */
    public abstract int getWidth();

    /**
     * 返回 <code>VolatileImage</code> 的高度。
     * @return 此 <code>VolatileImage</code> 的高度。
     */
    public abstract int getHeight();

    // Image 覆盖

    /**
     * 返回此 VolatileImage 的 ImageProducer。请注意，VolatileImage 对象针对渲染操作和将图像绘制到屏幕或其他 VolatileImage 对象进行了优化，而不是读取图像的像素。因此，如 <code>getSource</code> 之类的操作可能不如不依赖读取像素的操作快。此外，从图像读取的像素值仅在检索时与图像中的像素值保持一致。此方法在请求时对图像进行快照，并返回的 ImageProducer 对象处理该静态快照图像，而不是原始的 VolatileImage。调用 getSource() 等同于调用 getSnapshot().getSource()。
     * @return 一个可以用于生成 <code>BufferedImage</code> 表示形式的像素的 {@link ImageProducer}。
     * @see ImageProducer
     * @see #getSnapshot()
     */
    public ImageProducer getSource() {
        // REMIND: 确保此功能与规范一致。特别是，我们返回的是静态图像（快照）的源，而不是变化的图像（VolatileImage）的源。因此，如果用户期望源与当前的 VolatileImage 内容保持一致，他们可能会失望...
        // REMIND: 假设 getSnapshot() 返回的是有效的对象，而不是此类返回的默认 null 对象（因此假设实际的 VolatileImage 对象是从某个正确处理的子类派生的，例如 SunVolatileImage）。
        return getSnapshot().getSource();
    }

    // REMIND: 如果我们希望 getScaledInstance() 有良好的性能，我们应该覆盖 Image 的实现...

    /**
     * 此方法返回一个 {@link Graphics2D}，但为了向后兼容而存在。{@link #createGraphics() createGraphics} 更方便，因为它声明返回一个 <code>Graphics2D</code>。
     * @return 一个 <code>Graphics2D</code>，可以用于在此图像中绘制。
     */
    public Graphics getGraphics() {
        return createGraphics();
    }

    /**
     * 创建一个 <code>Graphics2D</code>，可以用于在此 <code>VolatileImage</code> 中绘制。
     * @return 一个 <code>Graphics2D</code>，用于在此图像中绘制。
     */
    public abstract Graphics2D createGraphics();


    // Volatile 管理方法

    /**
     * 尝试恢复图像的绘制表面，如果自上次 <code>validate</code> 调用以来表面已丢失。还验证此图像与给定的 GraphicsConfiguration 参数是否兼容，以确定从该图像到 GraphicsConfiguration 的操作是否兼容。例如，一种不兼容的情况可能是 VolatileImage 对象在一个图形设备上创建，然后用于在另一个图形设备上渲染。由于 VolatileImage 对象通常非常特定于设备，因此此操作可能无法按预期工作，因此此 validate 调用的返回代码会指出不兼容性。gc 为 null 或不正确可能导致 <code>validate</code> 返回不正确的值，并可能导致渲染时出现问题。
     *
     * @param   gc   一个 <code>GraphicsConfiguration</code> 对象，用于验证此图像。null gc 意味着 validate 方法应跳过兼容性测试。
     * @return  <code>IMAGE_OK</code> 如果图像不需要验证<BR>
     *          <code>IMAGE_RESTORED</code> 如果图像需要恢复。恢复意味着图像的内容可能已受影响，图像可能需要重新渲染。<BR>
     *          <code>IMAGE_INCOMPATIBLE</code> 如果图像与传递给 <code>validate</code> 方法的 <code>GraphicsConfiguration</code> 对象不兼容。不兼容意味着可能需要使用新的 <code>Component</code> 或 <code>GraphicsConfiguration</code> 重新创建图像，以获得可以成功用于此 <code>GraphicsConfiguration</code> 的图像。不兼容的图像不会检查是否需要恢复，因此在返回值为 <code>IMAGE_INCOMPATIBLE</code> 后，图像的状态不变，此返回值不意味着图像是否需要恢复。
     * @see java.awt.GraphicsConfiguration
     * @see java.awt.Component
     * @see #IMAGE_OK
     * @see #IMAGE_RESTORED
     * @see #IMAGE_INCOMPATIBLE
     */
    public abstract int validate(GraphicsConfiguration gc);

    /**
     * 如果自上次 <code>validate</code> 调用以来渲染数据已丢失，则返回 <code>true</code>。应用程序应在任何一系列渲染操作结束时调用此方法，以查看图像是否需要验证和重新渲染。
     * @return 如果绘制表面需要恢复，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public abstract boolean contentsLost();

    /**
     * 返回一个 ImageCapabilities 对象，可以查询此 VolatileImage 的特定功能。这允许程序员获取更多关于他们创建的特定 VolatileImage 对象的运行时信息。例如，用户可能创建了一个 VolatileImage，但系统可能没有足够的视频内存来创建该大小的图像，因此虽然该对象是一个 VolatileImage，但它可能不如该平台上的其他 VolatileImage 对象加速。用户可能需要这些信息来寻找其他解决方案。
     * @return 包含此 <code>VolatileImage</code> 功能的 <code>ImageCapabilities</code> 对象。
     * @since 1.4
     */
    public abstract ImageCapabilities getCapabilities();

    /**
     * 创建此图像时的透明度值。
     * @see java.awt.GraphicsConfiguration#createCompatibleVolatileImage(int,
     *      int,int)
     * @see java.awt.GraphicsConfiguration#createCompatibleVolatileImage(int,
     *      int,ImageCapabilities,int)
     * @see Transparency
     * @since 1.5
     */
    protected int transparency = TRANSLUCENT;

    /**
     * 返回透明度。返回 OPAQUE、BITMASK 或 TRANSLUCENT。
     * @return 此 <code>VolatileImage</code> 的透明度。
     * @see Transparency#OPAQUE
     * @see Transparency#BITMASK
     * @see Transparency#TRANSLUCENT
     * @since 1.5
     */
    public int getTransparency() {
        return transparency;
    }
}
