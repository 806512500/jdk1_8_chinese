
/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.awt.image.*;
import java.net.URL;
import java.net.URLConnection;
import java.io.File;
import sun.util.logging.PlatformLogger;
import sun.awt.image.SunWritableRaster;

/**
 * 启动屏幕可以在应用程序启动时显示，甚至在 Java 虚拟机 (JVM) 启动之前。启动屏幕显示为一个无边框窗口，包含一个图像。您可以使用 GIF、JPEG 或 PNG 文件作为图像。GIF 格式支持动画，而 GIF 和 PNG 都支持透明度。窗口位于屏幕中央。在多显示器系统上的位置未指定，具体位置取决于平台和实现。启动屏幕窗口在 Swing/AWT 显示第一个窗口时自动关闭（也可以使用 Java API 手动关闭，见下文）。
 * <P>
 * 如果您的应用程序打包在 JAR 文件中，可以使用 JAR 文件中的 "SplashScreen-Image" 选项来显示启动屏幕。将图像放在 JAR 存档中，并在选项中指定路径。路径不应有前导斜杠。
 * <BR>
 * 例如，在 <code>manifest.mf</code> 文件中：
 * <PRE>
 * Manifest-Version: 1.0
 * Main-Class: Test
 * SplashScreen-Image: filename.gif
 * </PRE>
 * <P>
 * 如果 Java 实现提供了命令行接口，并且您通过命令行或快捷方式运行应用程序，可以使用 Java 应用程序启动器选项来显示启动屏幕。Oracle 参考实现允许您使用 {@code -splash:} 选项指定启动屏幕图像的位置。
 * <BR>
 * 例如：
 * <PRE>
 * java -splash:filename.gif Test
 * </PRE>
 * 命令行接口优先于清单设置。
 * <p>
 * 启动屏幕将尽可能忠实地显示，以呈现整个启动屏幕图像，尽管目标平台和显示器的限制。
 * <p>
 * 意味着指定的图像将以“原样”方式显示在屏幕上，即保留图像文件中指定的确切颜色值。但在某些情况下，显示的图像可能会有所不同，例如在 16 或 8 位每像素 (bpp) 的屏幕上应用颜色抖动以呈现 32 bpp 的图像。原生平台显示配置也可能影响显示图像的颜色（例如颜色配置文件等）。
 * <p>
 * {@code SplashScreen} 类提供了控制启动屏幕的 API。此类可用于关闭启动屏幕、更改启动屏幕图像、获取启动屏幕原生窗口的位置/大小，以及在启动屏幕上绘制。它不能用于创建启动屏幕。您应使用 Java 实现提供的选项来创建启动屏幕。
 * <p>
 * 该类不能实例化。此类只能存在一个实例，可以通过 {@link #getSplashScreen()} 静态方法获取。如果应用程序启动时未通过命令行或清单文件选项创建启动屏幕，则 <code>getSplashScreen</code> 方法返回 <code>null</code>。
 *
 * @author Oleg Semenov
 * @since 1.6
 */
public final class SplashScreen {

    SplashScreen(long ptr) { // 非公开构造函数
        splashPtr = ptr;
    }

    /**
     * 返回用于控制支持显示的系统上 Java 启动屏幕的 {@code SplashScreen} 对象。
     *
     * @throws UnsupportedOperationException 如果当前工具包不支持启动屏幕功能
     * @throws HeadlessException 如果 {@code GraphicsEnvironment.isHeadless()}
     *         返回 true
     * @return {@link SplashScreen} 实例，或 <code>null</code> 如果没有或已关闭
     */
    public static  SplashScreen getSplashScreen() {
        synchronized (SplashScreen.class) {
            if (GraphicsEnvironment.isHeadless()) {
                throw new HeadlessException();
            }
            // SplashScreen 类现在是单例
            if (!wasClosed && theInstance == null) {
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<Void>() {
                        public Void run() {
                            System.loadLibrary("splashscreen");
                            return null;
                        }
                    });
                long ptr = _getInstance();
                if (ptr != 0 && _isVisible(ptr)) {
                    theInstance = new SplashScreen(ptr);
                }
            }
            return theInstance;
        }
    }

    /**
     * 更改启动屏幕图像。新图像从指定的 URL 加载；支持 GIF、JPEG 和 PNG 图像格式。
     * 方法在图像加载完成且窗口更新后返回。
     * 启动屏幕窗口根据图像的大小调整大小，并居中显示在屏幕上。
     *
     * @param imageURL 新启动屏幕图像的非-<code>null</code> URL
     * @throws NullPointerException 如果 {@code imageURL} 为 <code>null</code>
     * @throws IOException 如果加载图像时发生错误
     * @throws IllegalStateException 如果启动屏幕已关闭
     */
    public void setImageURL(URL imageURL) throws NullPointerException, IOException, IllegalStateException {
        checkVisible();
        URLConnection connection = imageURL.openConnection();
        connection.connect();
        int length = connection.getContentLength();
        java.io.InputStream stream = connection.getInputStream();
        byte[] buf = new byte[length];
        int off = 0;
        while(true) {
            // 检查是否有可用数据
            int available = stream.available();
            if (available <= 0) {
                // 没有可用数据... 好吧，尝试读取一个字节
                // 我们会看看会发生什么
                available = 1;
            }
            // 检查缓冲区是否有足够的空间，必要时重新分配
            // 缓冲区的大小至少增加 2 倍
            if (off + available > length) {
                length = off*2;
                if (off + available > length) {
                    length = available+off;
                }
                byte[] oldBuf = buf;
                buf = new byte[length];
                System.arraycopy(oldBuf, 0, buf, 0, off);
            }
            // 现在读取数据
            int result = stream.read(buf, off, available);
            if (result < 0) {
                break;
            }
            off += result;
        }
        synchronized(SplashScreen.class) {
            checkVisible();
            if (!_setImageData(splashPtr, buf)) {
                throw new IOException("加载图像时发生格式错误或 I/O 错误");
            }
            this.imageURL = imageURL;
        }
    }

    private void checkVisible() {
        if (!isVisible()) {
            throw new IllegalStateException("没有可用的启动屏幕");
        }
    }
    /**
     * 返回当前启动屏幕图像。
     *
     * @return 当前启动屏幕图像文件的 URL
     * @throws IllegalStateException 如果启动屏幕已关闭
     */
    public URL getImageURL() throws IllegalStateException {
        synchronized (SplashScreen.class) {
            checkVisible();
            if (imageURL == null) {
                try {
                    String fileName = _getImageFileName(splashPtr);
                    String jarName = _getImageJarName(splashPtr);
                    if (fileName != null) {
                        if (jarName != null) {
                            imageURL = new URL("jar:"+(new File(jarName).toURL().toString())+"!/"+fileName);
                        } else {
                            imageURL = new File(fileName).toURL();
                        }
                    }
                }
                catch(java.net.MalformedURLException e) {
                    if (log.isLoggable(PlatformLogger.Level.FINE)) {
                        log.fine("getImageURL() 方法中捕获 MalformedURLException", e);
                    }
                }
            }
            return imageURL;
        }
    }

    /**
     * 返回启动屏幕窗口的边界作为 {@link Rectangle}。
     * 如果您希望在相同位置用您的窗口替换启动屏幕，这可能很有用。
     * <p>
     * 您不能控制启动屏幕的大小或位置。
     * 启动屏幕的大小在图像更改时自动调整。
     * <p>
     * 图像可能包含透明区域，因此报告的边界可能比屏幕上可见的启动屏幕图像更大。
     *
     * @return 包含启动屏幕边界的 {@code Rectangle}
     * @throws IllegalStateException 如果启动屏幕已关闭
     */
    public Rectangle getBounds() throws IllegalStateException {
        synchronized (SplashScreen.class) {
            checkVisible();
            float scale = _getScaleFactor(splashPtr);
            Rectangle bounds = _getBounds(splashPtr);
            assert scale > 0;
            if (scale > 0 && scale != 1) {
                bounds.setSize((int) (bounds.getWidth() / scale),
                        (int) (bounds.getHeight() / scale));
            }
            return bounds;
        }
    }

    /**
     * 返回启动屏幕窗口的大小作为 {@link Dimension}。
     * 如果您希望在启动屏幕上绘制，这可能很有用。
     * <p>
     * 您不能控制启动屏幕的大小或位置。
     * 启动屏幕的大小在图像更改时自动调整。
     * <p>
     * 图像可能包含透明区域，因此报告的大小可能比屏幕上可见的启动屏幕图像更大。
     *
     * @return 表示启动屏幕大小的 {@link Dimension} 对象
     * @throws IllegalStateException 如果启动屏幕已关闭
     */
    public Dimension getSize() throws IllegalStateException {
        return getBounds().getSize();
    }

    /**
     * 创建一个图形上下文（作为 {@link Graphics2D} 对象）用于启动屏幕覆盖图像，允许您在启动屏幕上绘制。
     * 请注意，您不是在主图像上绘制，而是在使用 alpha 混合显示在主图像上的图像上绘制。另外请注意，绘制覆盖图像不一定更新启动屏幕窗口的内容。当您希望启动屏幕立即更新时，应调用 {@code update()}。
     * <p>
     * 图形上下文坐标空间中的像素 (0, 0) 对应于启动屏幕原生窗口边界（参见 {@link #getBounds()}）的原点。
     *
     * @return 启动屏幕覆盖表面的图形上下文
     * @throws IllegalStateException 如果启动屏幕已关闭
     */
    public Graphics2D createGraphics() throws IllegalStateException {
        synchronized (SplashScreen.class) {
            checkVisible();
            if (image==null) {
                // 获取未缩放的启动图像大小
                Dimension dim = _getBounds(splashPtr).getSize();
                image = new BufferedImage(dim.width, dim.height,
                        BufferedImage.TYPE_INT_ARGB);
            }
            float scale = _getScaleFactor(splashPtr);
            Graphics2D g = image.createGraphics();
            assert (scale > 0);
            if (scale <= 0) {
                scale = 1;
            }
            g.scale(scale, scale);
            return g;
        }
    }

    /**
     * 使用覆盖图像的当前内容更新启动窗口。
     *
     * @throws IllegalStateException 如果覆盖图像不存在；
     *         例如，如果从未调用 {@code createGraphics}，或者启动屏幕已关闭
     */
    public void update() throws IllegalStateException {
        BufferedImage image;
        synchronized (SplashScreen.class) {
            checkVisible();
            image = this.image;
        }
        if (image == null) {
            throw new IllegalStateException("没有可用的覆盖图像");
        }
        DataBuffer buf = image.getRaster().getDataBuffer();
        if (!(buf instanceof DataBufferInt)) {
            throw new AssertionError("覆盖图像 DataBuffer 类型无效 == "+buf.getClass().getName());
        }
        int numBanks = buf.getNumBanks();
        if (numBanks!=1) {
            throw new AssertionError("覆盖图像 DataBuffer 中的银行数量无效 == "+numBanks);
        }
        if (!(image.getSampleModel() instanceof SinglePixelPackedSampleModel)) {
            throw new AssertionError("覆盖图像的样本模型无效 == "+image.getSampleModel().getClass().getName());
        }
        SinglePixelPackedSampleModel sm = (SinglePixelPackedSampleModel)image.getSampleModel();
        int scanlineStride = sm.getScanlineStride();
        Rectangle rect = image.getRaster().getBounds();
        // 注意我们在这里借用数据数组，但只是为了读取
        // 因此我们不需要标记 DataBuffer 为脏...
        int[] data = SunWritableRaster.stealData((DataBufferInt)buf, 0);
        synchronized(SplashScreen.class) {
            checkVisible();
            _update(splashPtr, data, rect.x, rect.y, rect.width, rect.height, scanlineStride);
        }
    }

    /**
     * 隐藏启动屏幕，关闭窗口，并释放所有相关资源。
     *
     * @throws IllegalStateException 如果启动屏幕已关闭
     */
    public void close() throws IllegalStateException {
        synchronized (SplashScreen.class) {
            checkVisible();
            _close(splashPtr);
            image = null;
            SplashScreen.markClosed();
        }
    }


                static void markClosed() {
        synchronized (SplashScreen.class) {
            wasClosed = true;
            theInstance = null;
        }
    }


    /**
     * 确定启动画面是否可见。启动画面可能使用 {@link #close()} 隐藏，当第一个 AWT/Swing 窗口可见时也会自动隐藏。
     * <p>
     * 注意，本地平台可能会延迟在屏幕上呈现启动画面的本地窗口。此方法返回值为 {@code true} 仅表示隐藏启动画面窗口的条件尚未发生。
     *
     * @return 如果启动画面可见（尚未关闭），则返回 true，否则返回 false
     */
    public boolean isVisible() {
        synchronized (SplashScreen.class) {
            return !wasClosed && _isVisible(splashPtr);
        }
    }

    private BufferedImage image; // 覆盖图像

    private final long splashPtr; // 指向本地 Splash 结构的指针
    private static boolean wasClosed = false;

    private URL imageURL;

    /**
     * 单例的实例引用。
     * （如果尚未存在实例，则为 <code>null</code>。）
     *
     * @see #getSplashScreen
     * @see #close
     */
    private static SplashScreen theInstance = null;

    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.SplashScreen");

    private native static void _update(long splashPtr, int[] data, int x, int y, int width, int height, int scanlineStride);
    private native static boolean _isVisible(long splashPtr);
    private native static Rectangle _getBounds(long splashPtr);
    private native static long _getInstance();
    private native static void _close(long splashPtr);
    private native static String _getImageFileName(long splashPtr);
    private native static String _getImageJarName(long SplashPtr);
    private native static boolean _setImageData(long SplashPtr, byte[] data);
    private native static float _getScaleFactor(long SplashPtr);

}
