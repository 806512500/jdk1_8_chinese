
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
package java.applet;

import java.awt.*;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Locale;
import javax.accessibility.*;

/**
 * 一个小程序是一个小的程序，旨在不单独运行，而是嵌入到另一个应用程序中。
 * <p>
 * 任何要嵌入到网页或由 Java 小程序查看器查看的小程序类都必须是 <code>Applet</code> 类的子类。
 * <code>Applet</code> 类提供了小程序与其环境之间的标准接口。
 *
 * @author      Arthur van Hoff
 * @author      Chris Warth
 * @since       JDK1.0
 */
public class Applet extends Panel {

    /**
     * 构造一个新的 Applet。
     * <p>
     * 注意：许多 <code>java.applet.Applet</code> 中的方法只能在小程序完全构造后才能调用；
     * 小程序应避免在构造函数中调用 <code>java.applet.Applet</code> 中的方法。
     *
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since 1.4
     */
    public Applet() throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
    }

    /**
     * 小程序可以序列化，但必须遵循以下约定：
     *
     * 序列化前：
     * 小程序必须处于 STOPPED 状态。
     *
     * 反序列化后：
     * 小程序将恢复到 STOPPED 状态（大多数客户端可能会将其移动到 RUNNING 状态）。
     * stub 字段将由读取器恢复。
     */
    transient private AppletStub stub;

    /* 序列化形式的版本 ID。 */
    private static final long serialVersionUID = -5836846270535785031L;

    /**
     * 从对象输入流中读取小程序。
     * @exception HeadlessException 如果
     * <code>GraphicsEnvironment.isHeadless()</code> 返回
     * <code>true</code>
     * @serial
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since 1.4
     */
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException, HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        s.defaultReadObject();
    }

    /**
     * 设置此小程序的 stub。这由系统自动完成。
     * <p>如果有安全管理器，其 <code> checkPermission </code>
     * 方法将使用 <code>AWTPermission("setAppletStub")</code>
     * 权限来检查是否已经设置了 stub。
     * @param   stub   新的 stub。
     * @exception SecurityException 如果调用者无法设置 stub
     */
    public final void setStub(AppletStub stub) {
        if (this.stub != null) {
            SecurityManager s = System.getSecurityManager();
            if (s != null) {
                s.checkPermission(new AWTPermission("setAppletStub"));
            }
        }
        this.stub = stub;
    }

    /**
     * 确定此小程序是否处于活动状态。小程序在其 <code>start</code> 方法被调用之前被标记为活动状态。
     * 它在 <code>stop</code> 方法被调用之前变为非活动状态。
     *
     * @return  如果小程序处于活动状态，则返回 <code>true</code>；
     *          否则返回 <code>false</code>。
     * @see     java.applet.Applet#start()
     * @see     java.applet.Applet#stop()
     */
    public boolean isActive() {
        if (stub != null) {
            return stub.isActive();
        } else {        // 如果 stub 字段未填充，小程序从未处于活动状态
            return false;
        }
    }

    /**
     * 获取嵌入此小程序的文档的 URL。例如，假设小程序包含在以下文档中：
     * <blockquote><pre>
     *    http://www.oracle.com/technetwork/java/index.html
     * </pre></blockquote>
     * 文档基址为：
     * <blockquote><pre>
     *    http://www.oracle.com/technetwork/java/index.html
     * </pre></blockquote>
     *
     * @return  包含此小程序的文档的 {@link java.net.URL}。
     * @see     java.applet.Applet#getCodeBase()
     */
    public URL getDocumentBase() {
        return stub.getDocumentBase();
    }

    /**
     * 获取基 URL。这是包含此小程序的目录的 URL。
     *
     * @return  包含此小程序的目录的基 {@link java.net.URL}。
     * @see     java.applet.Applet#getDocumentBase()
     */
    public URL getCodeBase() {
        return stub.getCodeBase();
    }

    /**
     * 返回 HTML 标签中命名参数的值。例如，如果此小程序指定为
     * <blockquote><pre>
     * &lt;applet code="Clock" width=50 height=50&gt;
     * &lt;param name=Color value="blue"&gt;
     * &lt;/applet&gt;
     * </pre></blockquote>
     * <p>
     * 那么调用 <code>getParameter("Color")</code> 将返回值 <code>"blue"</code>。
     * <p>
     * <code>name</code> 参数不区分大小写。
     *
     * @param   name   参数名称。
     * @return  命名参数的值，
     *          如果未设置则返回 <code>null</code>。
     */
     public String getParameter(String name) {
         return stub.getParameter(name);
     }

    /**
     * 确定此小程序的上下文，该上下文允许小程序查询和影响其运行环境。
     * <p>
     * 小程序的环境代表包含小程序的文档。
     *
     * @return  小程序的上下文。
     */
    public AppletContext getAppletContext() {
        return stub.getAppletContext();
    }

    /**
     * 请求调整此小程序的大小。
     *
     * @param   width    请求的小程序的新宽度。
     * @param   height   请求的小程序的新高度。
     */
    @SuppressWarnings("deprecation")
    public void resize(int width, int height) {
        Dimension d = size();
        if ((d.width != width) || (d.height != height)) {
            super.resize(width, height);
            if (stub != null) {
                stub.appletResize(width, height);
            }
        }
    }

    /**
     * 请求调整此小程序的大小。
     *
     * @param   d   一个对象，给出新的宽度和高度。
     */
    @SuppressWarnings("deprecation")
    public void resize(Dimension d) {
        resize(d.width, d.height);
    }

    /**
     * 指示此容器是否为验证根。
     * <p>
     * {@code Applet} 对象是验证根，因此它们会覆盖此方法以返回 {@code true}。
     *
     * @return {@code true}
     * @since 1.7
     * @see java.awt.Container#isValidateRoot
     */
    @Override
    public boolean isValidateRoot() {
        return true;
    }

    /**
     * 请求在“状态窗口”中显示参数字符串。许多浏览器和小程序查看器
     * 提供这样的窗口，应用程序可以在其中向用户通知其当前状态。
     *
     * @param   msg   要在状态窗口中显示的字符串。
     */
    public void showStatus(String msg) {
        getAppletContext().showStatus(msg);
    }

    /**
     * 返回一个 <code>Image</code> 对象，该对象可以绘制在屏幕上。传递的 <code>url</code> 参数
     * 必须指定一个绝对 URL。
     * <p>
     * 此方法总是立即返回，无论图像是否存在。当此小程序尝试在屏幕上绘制图像时，
     * 数据将被加载。绘制图像的图形原语将逐步在屏幕上绘制。
     *
     * @param   url   指定图像位置的绝对 URL。
     * @return  指定 URL 处的图像。
     * @see     java.awt.Image
     */
    public Image getImage(URL url) {
        return getAppletContext().getImage(url);
    }

    /**
     * 返回一个 <code>Image</code> 对象，该对象可以绘制在屏幕上。<code>url</code> 参数必须指定一个绝对
     * URL。<code>name</code> 参数是相对于 <code>url</code> 参数的指定符。
     * <p>
     * 此方法总是立即返回，无论图像是否存在。当此小程序尝试在屏幕上绘制图像时，
     * 数据将被加载。绘制图像的图形原语将逐步在屏幕上绘制。
     *
     * @param   url    指定图像基位置的绝对 URL。
     * @param   name   相对于 <code>url</code> 参数的图像位置。
     * @return  指定 URL 处的图像。
     * @see     java.awt.Image
     */
    public Image getImage(URL url, String name) {
        try {
            return getImage(new URL(url, name));
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * 从给定的 URL 获取音频剪辑。
     *
     * @param url 指向音频剪辑的 URL。
     * @return 指定 URL 处的音频剪辑。
     *
     * @since       1.2
     */
    public final static AudioClip newAudioClip(URL url) {
        return new sun.applet.AppletAudioClip(url);
    }

    /**
     * 返回由 <code>URL</code> 参数指定的 <code>AudioClip</code> 对象。
     * <p>
     * 此方法总是立即返回，无论音频剪辑是否存在。当此小程序尝试播放音频剪辑时，
     * 数据将被加载。
     *
     * @param   url  指定音频剪辑位置的绝对 URL。
     * @return  指定 URL 处的音频剪辑。
     * @see     java.applet.AudioClip
     */
    public AudioClip getAudioClip(URL url) {
        return getAppletContext().getAudioClip(url);
    }

    /**
     * 返回由 <code>URL</code> 和 <code>name</code> 参数指定的 <code>AudioClip</code> 对象。
     * <p>
     * 此方法总是立即返回，无论音频剪辑是否存在。当此小程序尝试播放音频剪辑时，
     * 数据将被加载。
     *
     * @param   url    指定音频剪辑基位置的绝对 URL。
     * @param   name   相对于 <code>url</code> 参数的音频剪辑位置。
     * @return  指定 URL 处的音频剪辑。
     * @see     java.applet.AudioClip
     */
    public AudioClip getAudioClip(URL url, String name) {
        try {
            return getAudioClip(new URL(url, name));
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * 返回有关此小程序的信息。小程序应覆盖此方法以返回一个包含
     * 作者、版本和版权信息的 <code>String</code>。
     * <p>
     * 由 <code>Applet</code> 类提供的此方法的实现返回 <code>null</code>。
     *
     * @return  包含小程序作者、版本和版权信息的字符串。
     */
    public String getAppletInfo() {
        return null;
    }

    /**
     * 获取小程序的区域设置。它允许小程序维护与浏览器或小程序查看器不同的区域设置。
     *
     * @return  小程序的区域设置；如果未设置区域设置，则返回默认区域设置。
     * @since   JDK1.1
     */
    public Locale getLocale() {
      Locale locale = super.getLocale();
      if (locale == null) {
        return Locale.getDefault();
      }
      return locale;
    }

    /**
     * 返回此小程序理解的参数信息。小程序应覆盖此方法以返回一个描述这些参数的
     * <code>Strings</code> 数组。
     * <p>
     * 数组的每个元素应是一组三个 <code>Strings</code>，包含名称、类型和描述。例如：
     * <blockquote><pre>
     * String pinfo[][] = {
     *   {"fps",    "1-10",    "frames per second"},
     *   {"repeat", "boolean", "repeat image loop"},
     *   {"imgs",   "url",     "images directory"}
     * };
     * </pre></blockquote>
     * <p>
     * 由 <code>Applet</code> 类提供的此方法的实现返回 <code>null</code>。
     *
     * @return  描述此小程序查找的参数的数组。
     */
    public String[][] getParameterInfo() {
        return null;
    }

    /**
     * 播放指定绝对 URL 的音频剪辑。如果找不到音频剪辑，则不执行任何操作。
     *
     * @param   url   指定音频剪辑位置的绝对 URL。
     */
    public void play(URL url) {
        AudioClip clip = getAudioClip(url);
        if (clip != null) {
            clip.play();
        }
    }

    /**
     * 播放由 URL 和相对于它的指定符指定的音频剪辑。如果找不到音频剪辑，则不执行任何操作。
     *
     * @param   url    指定音频剪辑基位置的绝对 URL。
     * @param   name   相对于 <code>url</code> 参数的音频剪辑位置。
     */
    public void play(URL url, String name) {
        AudioClip clip = getAudioClip(url, name);
        if (clip != null) {
            clip.play();
        }
    }

    /**
     * 由浏览器或小程序查看器调用，通知此小程序已被加载到系统中。它总是在
     * 第一次调用 <code>start</code> 方法之前被调用。
     * <p>
     * <code>Applet</code> 的子类应覆盖此方法以执行初始化。例如，具有线程的小程序
     * 可以使用 <code>init</code> 方法创建线程，并使用 <code>destroy</code> 方法终止它们。
     * <p>
     * 由 <code>Applet</code> 类提供的此方法的实现不执行任何操作。
     *
     * @see     java.applet.Applet#destroy()
     * @see     java.applet.Applet#start()
     * @see     java.applet.Applet#stop()
     */
    public void init() {
    }


                /**
     * 由浏览器或小程序查看器调用，以通知
     * 该小程序应开始其执行。它在
     * <code>init</code> 方法之后和每次小程序在
     * Web 页面中被重新访问时调用。
     * <p>
     * <code>Applet</code> 的子类如果希望在每次包含它的 Web
     * 页面被访问时执行某些操作，应覆盖此方法。例如，具有
     * 动画的小程序可能希望使用 <code>start</code> 方法来
     * 恢复动画，并使用 <code>stop</code> 方法来暂停
     * 动画。
     * <p>
     * 注意：某些方法，如 <code>getLocationOnScreen</code>，只有在小程序显示时才能
     * 提供有意义的结果。因为当小程序的
     * <code>start</code> 首次被调用时，<code>isShowing</code> 返回 <code>false</code>，
     * 因此需要 <code>isShowing</code> 返回 <code>true</code> 的方法应从
     * <code>ComponentListener</code> 中调用。
     * <p>
     * <code>Applet</code> 类提供的此方法的实现不执行任何操作。
     *
     * @see     java.applet.Applet#destroy()
     * @see     java.applet.Applet#init()
     * @see     java.applet.Applet#stop()
     * @see     java.awt.Component#isShowing()
     * @see     java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
     */
    public void start() {
    }

    /**
     * 由浏览器或小程序查看器调用，以通知
     * 该小程序应停止其执行。当包含此小程序的
     * Web 页面被另一个页面替换时，以及在小程序被销毁之前调用此方法。
     * <p>
     * <code>Applet</code> 的子类如果希望在每次包含它的 Web
     * 页面不再可见时执行某些操作，应覆盖此方法。例如，具有
     * 动画的小程序可能希望使用 <code>start</code> 方法来
     * 恢复动画，并使用 <code>stop</code> 方法来暂停
     * 动画。
     * <p>
     * <code>Applet</code> 类提供的此方法的实现不执行任何操作。
     *
     * @see     java.applet.Applet#destroy()
     * @see     java.applet.Applet#init()
     */
    public void stop() {
    }

    /**
     * 由浏览器或小程序查看器调用，以通知
     * 该小程序正在被回收，并且应销毁
     * 它已分配的任何资源。在调用 <code>destroy</code> 之前，
     * <code>stop</code> 方法将始终被调用。
     * <p>
     * <code>Applet</code> 的子类如果希望在被销毁之前执行某些操作，应覆盖此方法。例如，具有
     * 线程的小程序可以使用 <code>init</code> 方法来创建线程，并使用
     * <code>destroy</code> 方法来终止它们。
     * <p>
     * <code>Applet</code> 类提供的此方法的实现不执行任何操作。
     *
     * @see     java.applet.Applet#init()
     * @see     java.applet.Applet#start()
     * @see     java.applet.Applet#stop()
     */
    public void destroy() {
    }

    //
    // 可访问性支持
    //

    AccessibleContext accessibleContext = null;

    /**
     * 获取与此小程序关联的 AccessibleContext。
     * 对于小程序，AccessibleContext 采用 AccessibleApplet 的形式。
     * 如果必要，将创建一个新的 AccessibleApplet 实例。
     *
     * @return 一个 AccessibleApplet，作为此小程序的
     *         AccessibleContext
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleApplet();
        }
        return accessibleContext;
    }

    /**
     * 该类为 <code>Applet</code> 类实现可访问性支持。
     * 它为小程序用户界面元素提供了 Java 可访问性 API 的适当实现。
     * @since 1.3
     */
    protected class AccessibleApplet extends AccessibleAWTPanel {

        private static final long serialVersionUID = 8127374778187708896L;

        /**
         * 获取此对象的角色。
         *
         * @return 一个 AccessibleRole 实例，描述对象的角色
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.FRAME;
        }

        /**
         * 获取此对象的状态。
         *
         * @return 一个 AccessibleStateSet 实例，包含对象的当前状态集
         * @see AccessibleState
         */
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            states.add(AccessibleState.ACTIVE);
            return states;
        }

    }
}
