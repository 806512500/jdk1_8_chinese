
/*
 * Copyright (c) 1999, 2023, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.peer.RobotPeer;
import java.lang.reflect.InvocationTargetException;
import sun.awt.ComponentFactory;
import sun.awt.SunToolkit;
import sun.awt.image.SunWritableRaster;
import sun.security.util.SecurityConstants;

/**
 * 该类用于生成本机系统输入事件，以实现测试自动化、自运行演示和其他需要控制鼠标和键盘的应用程序。Robot 类的主要目的是促进 Java 平台实现的自动化测试。
 * <p>
 * 使用此类生成输入事件与将事件发布到 AWT 事件队列或 AWT 组件不同，因为这些事件是在平台的本机输入队列中生成的。例如，<code>Robot.mouseMove</code> 实际上会移动鼠标指针，而不仅仅是生成鼠标移动事件。
 * <p>
 * 请注意，某些平台需要特殊权限或扩展才能访问低级输入控制。如果当前平台配置不允许输入控制，则在尝试构造 Robot 对象时会抛出 <code>AWTException</code>。例如，如果 X 服务器不支持（或未启用）XTEST 2.2 标准扩展，X-Window 系统将抛出该异常。
 * <p>
 * 将 Robot 用于自测试以外目的的应用程序应优雅地处理这些错误条件。
 * <p>
 * 平台和桌面环境可能对实现 Robot 类所需的所有功能施加限制或限制。例如：
 * <ul>
 * <li> 阻止访问桌面或桌面上不属于运行应用程序的任何部分的内容。</li>
 * <li> 将窗口装饰视为非所属内容。</li>
 * <li> 忽略或限制特定的窗口操作请求。</li>
 * <li> 忽略或限制特定的由 Robot 生成（合成）的与键盘和鼠标等相关的事件请求。</li>
 * <li> 要求特定或全局权限才能访问窗口内容，甚至是应用程序所属的内容，或执行有限的事件合成。</li>
 * </ul>
 *
 * Robot API 规范要求授予这些权限才能全面运行。
 * 如果未授予这些权限，API 将如上所述降级。
 * 相关的具体 API 方法可能会记录更具体的限制和要求。
 * 根据桌面环境的策略，
 * 上述权限可能：
 * <ul>
 * <li>每次都需要</li>
 * <li>或在应用程序的生命周期内持续有效，</li>
 * <li>或在多个用户桌面会话中持续有效</li>
 * <li>是细粒度的权限</li>
 * <li>与特定的二进制应用程序或一类二进制应用程序相关联。</li>
 * </ul>
 *
 * 当需要交互式授予这些权限时，可能会阻碍应用程序的正常运行，直到获得批准，如果未批准或不可能，或无法持久化，则会降级此类的功能，进而影响依赖于它的应用程序的任何部分的运行。
 *
 * @author      Robi Khan
 * @since       1.3
 */
public class Robot {
    private static final int MAX_DELAY = 60000;
    private RobotPeer peer;
    private boolean isAutoWaitForIdle = false;
    private int autoDelay = 0;
    private static int LEGAL_BUTTON_MASK = 0;

    private DirectColorModel screenCapCM = null;

    /**
     * 在主屏幕的坐标系中构造一个 Robot 对象。
     * <p>
     *
     * @throws  AWTException 如果平台配置不允许低级输入控制。当 GraphicsEnvironment.isHeadless() 返回 true 时，此异常总是被抛出
     * @throws  SecurityException 如果未授予 <code>createRobot</code> 权限
     * @see     java.awt.GraphicsEnvironment#isHeadless
     * @see     SecurityManager#checkPermission
     * @see     AWTPermission
     */
    public Robot() throws AWTException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new AWTException("headless environment");
        }
        init(GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice());
    }

    /**
     * 为给定的屏幕设备创建一个 Robot。传递给 Robot 方法调用（如 mouseMove 和 createScreenCapture）的坐标将被解释为与指定屏幕相同的坐标系。请注意，根据平台配置，多个屏幕可能：
     * <ul>
     * <li>共享相同的坐标系以形成一个组合的虚拟屏幕</li>
     * <li>使用不同的坐标系以独立屏幕的形式存在</li>
     * </ul>
     * 该构造函数适用于后一种情况。
     * <p>
     * 如果屏幕设备被重新配置，导致坐标系受到影响，则现有 Robot 对象的行为是未定义的。
     *
     * @param screen    指示 Robot 将在其上操作的坐标系的屏幕 GraphicsDevice。
     * @throws  AWTException 如果平台配置不允许低级输入控制。当 GraphicsEnvironment.isHeadless() 返回 true 时，此异常总是被抛出。
     * @throws  IllegalArgumentException 如果 <code>screen</code> 不是屏幕 GraphicsDevice。
     * @throws  SecurityException 如果未授予 <code>createRobot</code> 权限
     * @see     java.awt.GraphicsEnvironment#isHeadless
     * @see     GraphicsDevice
     * @see     SecurityManager#checkPermission
     * @see     AWTPermission
     */
    public Robot(GraphicsDevice screen) throws AWTException {
        checkIsScreenDevice(screen);
        init(screen);
    }

    private void init(GraphicsDevice screen) throws AWTException {
        checkRobotAllowed();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if (toolkit instanceof ComponentFactory) {
            peer = ((ComponentFactory)toolkit).createRobot(this, screen);
            disposer = new RobotDisposer(peer);
            sun.java2d.Disposer.addRecord(anchor, disposer);
        }
        initLegalButtonMask();
    }

    private static synchronized void initLegalButtonMask() {
        if (LEGAL_BUTTON_MASK != 0) return;

        int tmpMask = 0;
        if (Toolkit.getDefaultToolkit().areExtraMouseButtonsEnabled()){
            if (Toolkit.getDefaultToolkit() instanceof SunToolkit) {
                final int buttonsNumber = ((SunToolkit)(Toolkit.getDefaultToolkit())).getNumberOfButtons();
                for (int i = 0; i < buttonsNumber; i++){
                    tmpMask |= InputEvent.getMaskForButton(i+1);
                }
            }
        }
        tmpMask |= InputEvent.BUTTON1_MASK|
            InputEvent.BUTTON2_MASK|
            InputEvent.BUTTON3_MASK|
            InputEvent.BUTTON1_DOWN_MASK|
            InputEvent.BUTTON2_DOWN_MASK|
            InputEvent.BUTTON3_DOWN_MASK;
        LEGAL_BUTTON_MASK = tmpMask;
    }

    /* 确定安全策略是否允许创建 Robot */
    private void checkRobotAllowed() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(SecurityConstants.AWT.CREATE_ROBOT_PERMISSION);
        }
    }

    /* 检查给定的设备是否为屏幕设备 */
    private void checkIsScreenDevice(GraphicsDevice device) {
        if (device == null || device.getType() != GraphicsDevice.TYPE_RASTER_SCREEN) {
            throw new IllegalArgumentException("not a valid screen device");
        }
    }

    private transient Object anchor = new Object();

    static class RobotDisposer implements sun.java2d.DisposerRecord {
        private final RobotPeer peer;
        public RobotDisposer(RobotPeer peer) {
            this.peer = peer;
        }
        public void dispose() {
            if (peer != null) {
                peer.dispose();
            }
        }
    }

    private transient RobotDisposer disposer;

    /**
     * 将鼠标指针移动到给定的屏幕坐标。
     * <p>
     * 在某些平台上，鼠标指针可能不会视觉上移动，但随后的 mousePress 和 mouseRelease 可以传递到正确的位置
     *
     * @param x         X 位置
     * @param y         Y 位置
     */
    public synchronized void mouseMove(int x, int y) {
        peer.mouseMove(x, y);
        afterEvent();
    }

    /**
     * 按下一个或多个鼠标按钮。应使用 {@link #mouseRelease(int)} 方法释放鼠标按钮。
     *
     * @param buttons 按钮掩码；一个或多个鼠标按钮掩码的组合。
     * <p>
     * 允许使用有效的值的组合作为 {@code buttons} 参数。
     * 有效的组合包括 {@code InputEvent.BUTTON1_DOWN_MASK}、
     * {@code InputEvent.BUTTON2_DOWN_MASK}、{@code InputEvent.BUTTON3_DOWN_MASK}
     * 以及由
     * {@link InputEvent#getMaskForButton(int) InputEvent.getMaskForButton(button)} 方法返回的值。
     *
     * 有效的组合还取决于
     * {@link Toolkit#areExtraMouseButtonsEnabled() Toolkit.areExtraMouseButtonsEnabled()} 的值，如下所示：
     * <ul>
     * <li> 如果 Java 禁用了扩展鼠标按钮的支持
     * 则只允许使用以下标准按钮掩码：
     * {@code InputEvent.BUTTON1_DOWN_MASK}、{@code InputEvent.BUTTON2_DOWN_MASK}、
     * {@code InputEvent.BUTTON3_DOWN_MASK}。
     * <li> 如果 Java 启用了扩展鼠标按钮的支持
     * 则允许使用标准按钮掩码和现有扩展鼠标按钮的掩码，如果鼠标有超过三个按钮。
     * 这样，允许使用对应于按钮 1 到 {@link java.awt.MouseInfo#getNumberOfButtons() MouseInfo.getNumberOfButtons()} 范围内的按钮掩码。
     * <br>
     * 建议使用 {@link InputEvent#getMaskForButton(int) InputEvent.getMaskForButton(button)} 方法通过按钮编号获取任何鼠标按钮的掩码。
     * </ul>
     * <p>
     * 以下标准按钮掩码也被接受：
     * <ul>
     * <li>{@code InputEvent.BUTTON1_MASK}
     * <li>{@code InputEvent.BUTTON2_MASK}
     * <li>{@code InputEvent.BUTTON3_MASK}
     * </ul>
     * 但是，建议使用 {@code InputEvent.BUTTON1_DOWN_MASK}、
     * {@code InputEvent.BUTTON2_DOWN_MASK}、  {@code InputEvent.BUTTON3_DOWN_MASK}。
     * 应使用扩展的 {@code _DOWN_MASK} 或旧的 {@code _MASK} 值之一，但不应混合使用这两种模型。
     * @throws IllegalArgumentException 如果 {@code buttons} 掩码包含额外鼠标按钮的掩码
     *         且 Java 禁用了扩展鼠标按钮的支持
     * @throws IllegalArgumentException 如果 {@code buttons} 掩码包含额外鼠标按钮的掩码
     *         且该按钮在鼠标上不存在且 Java 启用了扩展鼠标按钮的支持
     * @see #mouseRelease(int)
     * @see InputEvent#getMaskForButton(int)
     * @see Toolkit#areExtraMouseButtonsEnabled()
     * @see java.awt.MouseInfo#getNumberOfButtons()
     * @see java.awt.event.MouseEvent
     */
    public synchronized void mousePress(int buttons) {
        checkButtonsArgument(buttons);
        peer.mousePress(buttons);
        afterEvent();
    }

    /**
     * 释放一个或多个鼠标按钮。
     *
     * @param buttons 按钮掩码；一个或多个鼠标按钮掩码的组合。
     * <p>
     * 允许使用有效的值的组合作为 {@code buttons} 参数。
     * 有效的组合包括 {@code InputEvent.BUTTON1_DOWN_MASK}、
     * {@code InputEvent.BUTTON2_DOWN_MASK}、{@code InputEvent.BUTTON3_DOWN_MASK}
     * 以及由
     * {@link InputEvent#getMaskForButton(int) InputEvent.getMaskForButton(button)} 方法返回的值。
     *
     * 有效的组合还取决于
     * {@link Toolkit#areExtraMouseButtonsEnabled() Toolkit.areExtraMouseButtonsEnabled()} 的值，如下所示：
     * <ul>
     * <li> 如果 Java 禁用了扩展鼠标按钮的支持
     * 则只允许使用以下标准按钮掩码：
     * {@code InputEvent.BUTTON1_DOWN_MASK}、{@code InputEvent.BUTTON2_DOWN_MASK}、
     * {@code InputEvent.BUTTON3_DOWN_MASK}。
     * <li> 如果 Java 启用了扩展鼠标按钮的支持
     * 则允许使用标准按钮掩码和现有扩展鼠标按钮的掩码，如果鼠标有超过三个按钮。
     * 这样，允许使用对应于按钮 1 到 {@link java.awt.MouseInfo#getNumberOfButtons() MouseInfo.getNumberOfButtons()} 范围内的按钮掩码。
     * <br>
     * 建议使用 {@link InputEvent#getMaskForButton(int) InputEvent.getMaskForButton(button)} 方法通过按钮编号获取任何鼠标按钮的掩码。
     * </ul>
     * <p>
     * 以下标准按钮掩码也被接受：
     * <ul>
     * <li>{@code InputEvent.BUTTON1_MASK}
     * <li>{@code InputEvent.BUTTON2_MASK}
     * <li>{@code InputEvent.BUTTON3_MASK}
     * </ul>
     * 但是，建议使用 {@code InputEvent.BUTTON1_DOWN_MASK}、
     * {@code InputEvent.BUTTON2_DOWN_MASK}、  {@code InputEvent.BUTTON3_DOWN_MASK}。
     * 应使用扩展的 {@code _DOWN_MASK} 或旧的 {@code _MASK} 值之一，但不应混合使用这两种模型。
     * @throws IllegalArgumentException 如果 {@code buttons} 掩码包含额外鼠标按钮的掩码
     *         且 Java 禁用了扩展鼠标按钮的支持
     * @throws IllegalArgumentException 如果 {@code buttons} 掩码包含额外鼠标按钮的掩码
     *         且该按钮在鼠标上不存在且 Java 启用了扩展鼠标按钮的支持
     * @see #mousePress(int)
     * @see InputEvent#getMaskForButton(int)
     * @see Toolkit#areExtraMouseButtonsEnabled()
     * @see java.awt.MouseInfo#getNumberOfButtons()
     * @see java.awt.event.MouseEvent
     */
    public synchronized void mouseRelease(int buttons) {
        checkButtonsArgument(buttons);
        peer.mouseRelease(buttons);
        afterEvent();
    }


                private void checkButtonsArgument(int buttons) {
        if ( (buttons|LEGAL_BUTTON_MASK) != LEGAL_BUTTON_MASK ) {
            throw new IllegalArgumentException("无效的按钮组合");
        }
    }

    /**
     * 旋转带有滚轮的鼠标上的滚轮。
     *
     * @param wheelAmt  鼠标滚轮移动的“刻度”数
     *                  负值表示向上/远离用户移动，
     *                  正值表示向下/朝向用户移动。
     *
     * @since 1.4
     */
    public synchronized void mouseWheel(int wheelAmt) {
        peer.mouseWheel(wheelAmt);
        afterEvent();
    }

    /**
     * 按下指定的键。应使用 <code>keyRelease</code> 方法释放该键。
     * <p>
     * 有多个物理键与之关联的键码（例如 <code>KeyEvent.VK_SHIFT</code> 可能表示左或右 Shift 键）
     * 将映射到左侧的键。
     *
     * @param   keycode 要按下的键（例如 <code>KeyEvent.VK_A</code>）
     * @throws  IllegalArgumentException 如果 <code>keycode</code> 不是有效的键
     * @see     #keyRelease(int)
     * @see     java.awt.event.KeyEvent
     */
    public synchronized void keyPress(int keycode) {
        checkKeycodeArgument(keycode);
        peer.keyPress(keycode);
        afterEvent();
    }

    /**
     * 释放指定的键。
     * <p>
     * 有多个物理键与之关联的键码（例如 <code>KeyEvent.VK_SHIFT</code> 可能表示左或右 Shift 键）
     * 将映射到左侧的键。
     *
     * @param   keycode 要释放的键（例如 <code>KeyEvent.VK_A</code>）
     * @throws  IllegalArgumentException 如果 <code>keycode</code> 不是有效的键
     * @see  #keyPress(int)
     * @see     java.awt.event.KeyEvent
     */
    public synchronized void keyRelease(int keycode) {
        checkKeycodeArgument(keycode);
        peer.keyRelease(keycode);
        afterEvent();
    }

    private void checkKeycodeArgument(int keycode) {
        // 而不是在这里构建一个大表或 switch 语句，我们只检查键是否不是 VK_UNDEFINED 并假设
        // peer 实现将为其他无效值（例如 -1, 999999）抛出异常
        if (keycode == KeyEvent.VK_UNDEFINED) {
            throw new IllegalArgumentException("无效的键码");
        }
    }

    /**
     * 返回给定屏幕坐标处的像素颜色。
     * <p>
     * 如果桌面环境要求授予捕获屏幕内容的权限，而未授予所需的权限，
     * 则可能会抛出 {@code SecurityException}，
     * 或返回的 {@code Color} 内容未定义。
     * </p>
     * @apiNote 建议避免在 AWT 事件调度线程上调用此方法，因为屏幕捕获可能是一个耗时的操作，
     * 特别是在需要获取权限且涉及用户交互时。
     *
     * @param   x       像素的 X 位置
     * @param   y       像素的 Y 位置
     * @throws  SecurityException 如果未授予 {@code readDisplayPixels} 权限，或桌面环境拒绝访问屏幕
     * @return  像素的颜色
     */
    public synchronized Color getPixelColor(int x, int y) {
        checkScreenCaptureAllowed();
        Color color = new Color(peer.getRGBPixel(x, y));
        return color;
    }

    /**
     * 创建一个包含从屏幕读取的像素的图像。
     * <p>
     * 如果桌面环境要求授予捕获屏幕内容的权限，而未授予所需的权限，
     * 则可能会抛出 {@code SecurityException}，
     * 或返回的 {@code BufferedImage} 内容未定义。
     * </p>
     * @apiNote 建议避免在 AWT 事件调度线程上调用此方法，因为屏幕捕获可能是一个耗时的操作，
     * 特别是在需要获取权限且涉及用户交互时。
     *
     * @param   screenRect      要捕获的屏幕坐标矩形
     * @return  捕获的图像
     * @throws  IllegalArgumentException 如果 {@code screenRect} 的宽度和高度不大于零
     * @throws  SecurityException 如果未授予 {@code readDisplayPixels} 权限，或桌面环境拒绝访问屏幕
     * @see     SecurityManager#checkPermission
     * @see     AWTPermission
     */
    public synchronized BufferedImage createScreenCapture(Rectangle screenRect) {
        checkScreenCaptureAllowed();

        checkValidRect(screenRect);

        BufferedImage image;
        DataBufferInt buffer;
        WritableRaster raster;

        if (screenCapCM == null) {
            /*
             * 修复 4285201
             * 创建一个与默认 RGB ColorModel 等效的 DirectColorModel，
             * 但没有 Alpha 组件。
             */

            screenCapCM = new DirectColorModel(24,
                                               /* 红色掩码 */    0x00FF0000,
                                               /* 绿色掩码 */  0x0000FF00,
                                               /* 蓝色掩码 */   0x000000FF);
        }

        // 在抓取像素之前需要同步工具包，因为在某些情况下，渲染到屏幕可能会延迟
        Toolkit.getDefaultToolkit().sync();

        int pixels[];
        int[] bandmasks = new int[3];

        pixels = peer.getRGBPixels(screenRect);
        buffer = new DataBufferInt(pixels, pixels.length);

        bandmasks[0] = screenCapCM.getRedMask();
        bandmasks[1] = screenCapCM.getGreenMask();
        bandmasks[2] = screenCapCM.getBlueMask();

        raster = Raster.createPackedRaster(buffer, screenRect.width, screenRect.height, screenRect.width, bandmasks, null);
        SunWritableRaster.makeTrackable(buffer);

        image = new BufferedImage(screenCapCM, raster, false, null);

        return image;
    }

    private static void checkValidRect(Rectangle rect) {
        if (rect.width <= 0 || rect.height <= 0) {
            throw new IllegalArgumentException("矩形的宽度和高度必须大于 0");
        }
    }

    private static void checkScreenCaptureAllowed() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(
                SecurityConstants.AWT.READ_DISPLAY_PIXELS_PERMISSION);
        }
    }

    /*
     * 事件生成后调用
     */
    private void afterEvent() {
        autoWaitForIdle();
        autoDelay();
    }

    /**
     * 返回此 Robot 是否在生成事件后自动调用 <code>waitForIdle</code>。
     * @return 是否自动调用 <code>waitForIdle</code>
     */
    public synchronized boolean isAutoWaitForIdle() {
        return isAutoWaitForIdle;
    }

    /**
     * 设置此 Robot 是否在生成事件后自动调用 <code>waitForIdle</code>。
     * @param   isOn    是否自动调用 <code>waitForIdle</code>
     */
    public synchronized void setAutoWaitForIdle(boolean isOn) {
        isAutoWaitForIdle = isOn;
    }

    /*
     * 如果需要，在每个事件后调用 waitForIdle。
     */
    private void autoWaitForIdle() {
        if (isAutoWaitForIdle) {
            waitForIdle();
        }
    }

    /**
     * 返回此 Robot 在生成事件后休眠的毫秒数。
     */
    public synchronized int getAutoDelay() {
        return autoDelay;
    }

    /**
     * 设置此 Robot 在生成事件后休眠的毫秒数。
     * @throws  IllegalArgumentException 如果 <code>ms</code> 不在 0 到 60,000 毫秒之间（含）
     */
    public synchronized void setAutoDelay(int ms) {
        checkDelayArgument(ms);
        autoDelay = ms;
    }

    /*
     * 在事件生成后自动休眠指定的时间间隔。
     */
    private void autoDelay() {
        delay(autoDelay);
    }

    /**
     * 休眠指定的时间。
     * 要捕获任何 <code>InterruptedException</code>，可以使用 <code>Thread.sleep()</code>。
     * @param   ms      休眠的时间（毫秒）
     * @throws  IllegalArgumentException 如果 <code>ms</code> 不在 0 到 60,000 毫秒之间（含）
     * @see     java.lang.Thread#sleep
     */
    public synchronized void delay(int ms) {
        checkDelayArgument(ms);
        try {
            Thread.sleep(ms);
        } catch(InterruptedException ite) {
            ite.printStackTrace();
        }
    }

    private void checkDelayArgument(int ms) {
        if (ms < 0 || ms > MAX_DELAY) {
            throw new IllegalArgumentException("延迟必须在 0 到 60,000ms 之间");
        }
    }

    /**
     * 等待事件队列中的所有当前事件都被处理。
     * @throws  IllegalThreadStateException 如果在 AWT 事件调度线程上调用
     */
    public synchronized void waitForIdle() {
        checkNotDispatchThread();
        // 向队列中发布一个虚拟事件，以便我们知道
        // 在它之前的所有事件都已处理
        try {
            SunToolkit.flushPendingEvents();
            EventQueue.invokeAndWait( new Runnable() {
                                            public void run() {
                                                // 虚拟实现
                                            }
                                        } );
        } catch(InterruptedException ite) {
            System.err.println("Robot.waitForIdle, 捕获到非致命异常：");
            ite.printStackTrace();
        } catch(InvocationTargetException ine) {
            System.err.println("Robot.waitForIdle, 捕获到非致命异常：");
            ine.printStackTrace();
        }
    }

    private void checkNotDispatchThread() {
        if (EventQueue.isDispatchThread()) {
            throw new IllegalThreadStateException("不能从事件调度线程调用此方法");
        }
    }

    /**
     * 返回此 Robot 的字符串表示形式。
     *
     * @return  字符串表示形式。
     */
    public synchronized String toString() {
        String params = "autoDelay = "+getAutoDelay()+", "+"autoWaitForIdle = "+isAutoWaitForIdle();
        return getClass().getName() + "[ " + params + " ]";
    }
}
