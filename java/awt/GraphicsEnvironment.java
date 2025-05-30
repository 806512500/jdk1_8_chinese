
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

import java.awt.image.BufferedImage;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;

import sun.font.FontManager;
import sun.font.FontManagerFactory;
import sun.java2d.HeadlessGraphicsEnvironment;
import sun.java2d.SunGraphicsEnvironment;
import sun.security.action.GetPropertyAction;

/**
 *
 * <code>GraphicsEnvironment</code> 类描述了在特定平台上 Java(tm) 应用程序可用的 {@link GraphicsDevice} 对象和 {@link java.awt.Font} 对象的集合。
 * 该 <code>GraphicsEnvironment</code> 中的资源可能是本地的或远程机器上的。 <code>GraphicsDevice</code> 对象可以是屏幕、打印机或图像缓冲区，是 {@link Graphics2D} 绘图方法的目标。
 * 每个 <code>GraphicsDevice</code> 都有多个 {@link GraphicsConfiguration} 对象与之关联。这些对象指定了 <code>GraphicsDevice</code> 可以使用的不同配置。
 * @see GraphicsDevice
 * @see GraphicsConfiguration
 */

public abstract class GraphicsEnvironment {
    private static GraphicsEnvironment localEnv;

    /**
     * 工具包和 GraphicsEnvironment 的无头状态
     */
    private static Boolean headless;

    /**
     * 默认假设的无头状态
     */
    private static Boolean defaultHeadless;

    /**
     * 这是一个抽象类，不能直接实例化。实例必须从合适的工厂或查询方法中获得。
     */
    protected GraphicsEnvironment() {
    }

    /**
     * 返回本地的 <code>GraphicsEnvironment</code>。
     * @return 本地的 <code>GraphicsEnvironment</code>
     */
    public static synchronized GraphicsEnvironment getLocalGraphicsEnvironment() {
        if (localEnv == null) {
            localEnv = createGE();
        }

        return localEnv;
    }

    /**
     * 根据系统属性 'java.awt.graphicsenv' 创建并返回 GraphicsEnvironment。
     *
     * @return 图形环境
     */
    private static GraphicsEnvironment createGE() {
        GraphicsEnvironment ge;
        String nm = AccessController.doPrivileged(new GetPropertyAction("java.awt.graphicsenv", null));
        try {
//          long t0 = System.currentTimeMillis();
            Class<GraphicsEnvironment> geCls;
            try {
                // 首先尝试引导类加载器是否能找到请求的类。这样可以避免在特权块中运行。
                geCls = (Class<GraphicsEnvironment>)Class.forName(nm);
            } catch (ClassNotFoundException ex) {
                // 如果引导类加载器失败，我们再次尝试使用应用程序类加载器。
                ClassLoader cl = ClassLoader.getSystemClassLoader();
                geCls = (Class<GraphicsEnvironment>)Class.forName(nm, true, cl);
            }
            ge = geCls.newInstance();
//          long t1 = System.currentTimeMillis();
//          System.out.println("GE creation took " + (t1-t0)+ "ms.");
            if (isHeadless()) {
                ge = new HeadlessGraphicsEnvironment(ge);
            }
        } catch (ClassNotFoundException e) {
            throw new Error("Could not find class: "+nm);
        } catch (InstantiationException e) {
            throw new Error("Could not instantiate Graphics Environment: "
                            + nm);
        } catch (IllegalAccessException e) {
            throw new Error ("Could not access Graphics Environment: "
                             + nm);
        }
        return ge;
    }

    /**
     * 测试在该环境中是否可以支持显示、键盘和鼠标。如果此方法返回 true，则在依赖于显示、键盘或鼠标的 Toolkit 和 GraphicsEnvironment 的区域中将抛出 HeadlessException。
     * @return 如果该环境不能支持显示、键盘和鼠标，则返回 <code>true</code>；否则返回 <code>false</code>
     * @see java.awt.HeadlessException
     * @since 1.4
     */
    public static boolean isHeadless() {
        return getHeadlessProperty();
    }

    /**
     * @return 如果默认假设无头状态，则返回警告消息；否则返回 null
     * @since 1.5
     */
    static String getHeadlessMessage() {
        if (headless == null) {
            getHeadlessProperty(); // 初始化值
        }
        return defaultHeadless != Boolean.TRUE ? null :
            "\nNo X11 DISPLAY variable was set, " +
            "but this program performed an operation which requires it.";
    }

    /**
     * @return 属性 "java.awt.headless" 的值
     * @since 1.4
     */
    private static boolean getHeadlessProperty() {
        if (headless == null) {
            AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                String nm = System.getProperty("java.awt.headless");

                if (nm == null) {
                    /* 在浏览器中运行时不需要询问 DISPLAY */
                    if (System.getProperty("javaplugin.version") != null) {
                        headless = defaultHeadless = Boolean.FALSE;
                    } else {
                        String osName = System.getProperty("os.name");
                        if (osName.contains("OS X") && "sun.awt.HToolkit".equals(
                                System.getProperty("awt.toolkit")))
                        {
                            headless = defaultHeadless = Boolean.TRUE;
                        } else {
                            final String display = System.getenv("DISPLAY");
                            headless = defaultHeadless =
                                ("Linux".equals(osName) ||
                                 "SunOS".equals(osName) ||
                                 "FreeBSD".equals(osName) ||
                                 "NetBSD".equals(osName) ||
                                 "OpenBSD".equals(osName) ||
                                 "AIX".equals(osName)) &&
                                 (display == null || display.trim().isEmpty());
                        }
                    }
                } else {
                    headless = Boolean.valueOf(nm);
                }
                return null;
            });
        }
        return headless;
    }

    /**
     * 检查无头状态并在无头时抛出 HeadlessException
     * @since 1.4
     */
    static void checkHeadless() throws HeadlessException {
        if (isHeadless()) {
            throw new HeadlessException();
        }
    }

    /**
     * 返回在该图形环境中是否可以支持显示、键盘和鼠标。如果此方法返回 true，则在依赖于显示、键盘或鼠标的图形环境区域中将抛出 <code>HeadlessException</code>。
     * @return 如果在该环境中可以支持显示、键盘和鼠标，则返回 <code>true</code>；否则返回 <code>false</code>
     * @see java.awt.HeadlessException
     * @see #isHeadless
     * @since 1.4
     */
    public boolean isHeadlessInstance() {
        // 默认（本地图形环境），仅检查无头属性。
        return getHeadlessProperty();
    }

    /**
     * 返回所有屏幕 <code>GraphicsDevice</code> 对象的数组。
     * @return 包含所有表示屏幕设备的 <code>GraphicsDevice</code> 对象的数组
     * @exception HeadlessException 如果 isHeadless() 返回 true
     * @see #isHeadless()
     */
    public abstract GraphicsDevice[] getScreenDevices()
        throws HeadlessException;

    /**
     * 返回默认的屏幕 <code>GraphicsDevice</code>。
     * @return 表示默认屏幕设备的 <code>GraphicsDevice</code>
     * @exception HeadlessException 如果 isHeadless() 返回 true
     * @see #isHeadless()
     */
    public abstract GraphicsDevice getDefaultScreenDevice()
        throws HeadlessException;

    /**
     * 返回一个用于在指定的 {@link BufferedImage} 中渲染的 <code>Graphics2D</code> 对象。
     * @param img 指定的 <code>BufferedImage</code>
     * @return 用于在指定的 <code>BufferedImage</code> 中渲染的 <code>Graphics2D</code>
     * @throws NullPointerException 如果 <code>img</code> 为 null
     */
    public abstract Graphics2D createGraphics(BufferedImage img);

    /**
     * 返回一个包含此 <code>GraphicsEnvironment</code> 中所有可用字体的一点大小实例的数组。典型用法是允许用户选择特定的字体。然后，应用程序可以通过调用选定实例的 <code>deriveFont</code> 方法来调整字体大小和设置各种字体属性。
     * <p>
     * 此方法为应用程序提供了对用于渲染文本的 <code>Font</code> 实例的最精确控制。如果此 <code>GraphicsEnvironment</code> 中的字体有多个可编程变体，则数组中仅返回该字体的一个实例，其他变体必须由应用程序派生。
     * <p>
     * 如果此环境中的字体有多个可编程变体，例如 Multiple-Master 字体，数组中仅返回该字体的一个实例，其他变体必须由应用程序派生。
     *
     * @return <code>Font</code> 对象数组
     * @see #getAvailableFontFamilyNames
     * @see java.awt.Font
     * @see java.awt.Font#deriveFont
     * @see java.awt.Font#getFontName
     * @since 1.2
     */
    public abstract Font[] getAllFonts();

    /**
     * 返回一个包含此 <code>GraphicsEnvironment</code> 中所有字体家族名称的数组，这些名称已针对默认区域设置进行了本地化，该区域设置由 <code>Locale.getDefault()</code> 返回。
     * <p>
     * 典型用法是向用户呈现特定的家族名称以供选择。应用程序然后可以指定此名称，结合样式（如粗体或斜体）来创建字体，从而让字体系统在同一家族中的多个字体中选择最佳匹配。
     *
     * @return 包含默认区域设置的字体家族名称的 <code>String</code> 数组，或如果该区域设置下没有名称，则返回合适的替代名称。
     * @see #getAllFonts
     * @see java.awt.Font
     * @see java.awt.Font#getFamily
     * @since 1.2
     */
    public abstract String[] getAvailableFontFamilyNames();

    /**
     * 返回一个包含此 <code>GraphicsEnvironment</code> 中所有字体家族名称的数组，这些名称已针对指定的区域设置进行了本地化。
     * <p>
     * 典型用法是向用户呈现特定的家族名称以供选择。应用程序然后可以指定此名称，结合样式（如粗体或斜体）来创建字体，从而让字体系统在同一家族中的多个字体中选择最佳匹配。
     *
     * @param l 表示特定地理、政治或文化区域的 {@link Locale} 对象。指定 <code>null</code> 等同于指定 <code>Locale.getDefault()</code>。
     * @return 包含指定 <code>Locale</code> 的字体家族名称的 <code>String</code> 数组，或如果指定的区域设置下没有名称，则返回合适的替代名称。
     * @see #getAllFonts
     * @see java.awt.Font
     * @see java.awt.Font#getFamily
     * @since 1.2
     */
    public abstract String[] getAvailableFontFamilyNames(Locale l);

    /**
     * 在此 <code>GraphicsEnvironment</code> 中注册一个 <i>创建的</i> <code>Font</code>。
     * 创建的字体是指通过调用 {@link Font#createFont} 返回的字体，或通过调用 {@link Font#deriveFont} 从创建的字体派生的字体。
     * 调用此方法后，该字体可用于通过名称或家族名称构造新的 <code>Font</code>，并在该应用程序或小程序的执行上下文中由 {@link #getAvailableFontFamilyNames} 和 {@link #getAllFonts} 枚举。这意味着小程序不能以其他小程序可见的方式注册字体。
     * <p>
     * 以下原因可能导致此方法不注册字体并因此返回 <code>false</code>：
     * <ul>
     * <li>该字体不是 <i>创建的</i> <code>Font</code>。
     * <li>该字体与此 <code>GraphicsEnvironment</code> 中已有的非创建 <code>Font</code> 冲突。例如，如果名称是系统字体或 {@link Font} 类文档中描述的逻辑字体。具体实现可能还取决于该字体是否与系统字体具有相同的家族名称。
     * <p>请注意，应用程序可以用新的字体替换先前创建的字体的注册。
     * </ul>
     * @return 如果 <code>font</code> 成功在此 <code>GraphicsEnvironment</code> 中注册，则返回 <code>true</code>。
     * @throws NullPointerException 如果 <code>font</code> 为 null
     * @since 1.6
     */
    public boolean registerFont(Font font) {
        if (font == null) {
            throw new NullPointerException("font cannot be null.");
        }
        FontManager fm = FontManagerFactory.getInstance();
        return fm.registerFont(font);
    }

    /**
     * 表示在逻辑字体映射到物理字体时优先使用特定于区域设置的字体。调用此方法表示字体渲染应主要使用主要书写系统（由默认编码和初始默认区域设置指示的系统）的字体。例如，如果主要书写系统是日语，则应尽可能使用日语字体来渲染字符，其他字体仅用于日语字体没有字形的字符。
     * <p>
     * 调用此方法实际导致的字体渲染行为的变化是实现依赖的；可能根本没有效果，或者请求的行为可能已经与默认行为匹配。轻量级组件和对等组件之间的字体渲染行为可能有所不同。由于调用此方法请求了不同的字体，客户端应期望不同的度量，并可能需要重新计算窗口大小和布局。因此，应在用户界面初始化之前调用此方法。
     * @since 1.5
     */
    public void preferLocaleFonts() {
        FontManager fm = FontManagerFactory.getInstance();
        fm.preferLocaleFonts();
    }


                /**
     * 表示在逻辑字体映射到物理字体时，更倾向于比例字体（例如，双间距的CJK字体）。如果默认映射包含存在比例和非比例变体的字体，则调用
     * 此方法表示映射应使用比例变体。
     * <p>
     * 调用此方法后实际的字体渲染行为变化取决于实现；可能完全没有任何效果。轻量级组件和对等组件之间的字体渲染行为可能有所不同。由于调用此方法请求了
     * 不同的字体，客户端应预期不同的度量，并可能需要重新计算窗口大小和布局。因此，应在用户界面初始化之前调用此方法。
     * @since 1.5
     */
    public void preferProportionalFonts() {
        FontManager fm = FontManagerFactory.getInstance();
        fm.preferProportionalFonts();
    }

    /**
     * 返回窗口应居中的点。
     * 建议检查居中的窗口以确保它们适合使用 getMaximumWindowBounds() 获取的可用显示区域。
     * @return 窗口应居中的点
     *
     * @exception HeadlessException 如果 isHeadless() 返回 true
     * @see #getMaximumWindowBounds
     * @since 1.4
     */
    public Point getCenterPoint() throws HeadlessException {
    // 默认实现：返回默认屏幕设备的可用边界中心。
        Rectangle usableBounds =
         SunGraphicsEnvironment.getUsableBounds(getDefaultScreenDevice());
        return new Point((usableBounds.width / 2) + usableBounds.x,
                         (usableBounds.height / 2) + usableBounds.y);
    }

    /**
     * 返回居中窗口的最大边界。
     * 这些边界考虑了本机窗口系统中的对象，如任务栏和菜单栏。返回的边界将位于单个显示器上，有一个例外：在多屏幕系统中，如果窗口应跨所有显示器居中，
     * 此方法将返回整个显示区域的边界。
     * <p>
     * 要获取单个显示器的可用边界，请使用
     * <code>GraphicsConfiguration.getBounds()</code> 和
     * <code>Toolkit.getScreenInsets()</code>。
     * @return 居中窗口的最大边界
     *
     * @exception HeadlessException 如果 isHeadless() 返回 true
     * @see #getCenterPoint
     * @see GraphicsConfiguration#getBounds
     * @see Toolkit#getScreenInsets
     * @since 1.4
     */
    public Rectangle getMaximumWindowBounds() throws HeadlessException {
    // 默认实现：返回默认屏幕设备的可用边界。这适用于 Microsoft Windows 和非 Xinerama X11。
        return SunGraphicsEnvironment.getUsableBounds(getDefaultScreenDevice());
    }
}
