
/*
 * Copyright (c) 2005, 2018, Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.AWTPermission;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.peer.DesktopPeer;
import sun.awt.SunToolkit;
import sun.awt.HeadlessToolkit;
import java.io.FilePermission;
import sun.security.util.SecurityConstants;

/**
 * {@code Desktop} 类允许 Java 应用程序启动在本地桌面上注册的应用程序来处理
 * {@link java.net.URI} 或文件。
 *
 * <p> 支持的操作包括：
 * <ul>
 *   <li>启动用户默认浏览器以显示指定的 URI；</li>
 *   <li>启动用户默认邮件客户端，可选包含 {@code mailto} URI；</li>
 *   <li>启动已注册的应用程序以打开、编辑或打印指定的文件。</li>
 * </ul>
 *
 * <p> 本类提供了对应这些操作的方法。这些方法查找当前平台上的已注册应用程序，并启动它来处理 URI
 * 或文件。如果没有已注册的应用程序或已注册的应用程序启动失败，将抛出异常。
 *
 * <p> 应用程序注册到 URI 或文件类型；例如，{@code "sxi"} 文件扩展名通常注册到 StarOffice。
 * 注册、访问和启动已注册应用程序的机制是平台依赖的。
 *
 * <p> 每个操作是一个由 {@link Desktop.Action} 类表示的动作类型。
 *
 * <p> 注意：当某个动作被调用并且已注册的应用程序被执行时，它将在启动 Java 应用程序的同一系统上执行。
 *
 * @since 1.6
 * @author Armin Chen
 * @author George Zhang
 */
public class Desktop {

    /**
     * 表示一个动作类型。每个平台支持不同的动作集。可以使用 {@link Desktop#isSupported}
     * 方法来确定当前平台是否支持给定的动作。
     * @see java.awt.Desktop#isSupported(java.awt.Desktop.Action)
     * @since 1.6
     */
    public static enum Action {
        /**
         * 表示一个 "open" 动作。
         * @see Desktop#open(java.io.File)
         */
        OPEN,
        /**
         * 表示一个 "edit" 动作。
         * @see Desktop#edit(java.io.File)
         */
        EDIT,
        /**
         * 表示一个 "print" 动作。
         * @see Desktop#print(java.io.File)
         */
        PRINT,
        /**
         * 表示一个 "mail" 动作。
         * @see Desktop#mail()
         * @see Desktop#mail(java.net.URI)
         */
        MAIL,
        /**
         * 表示一个 "browse" 动作。
         * @see Desktop#browse(java.net.URI)
         */
        BROWSE
    };

    private DesktopPeer peer;

    /**
     * 抑制默认构造函数以防止实例化。
     */
    private Desktop() {
        peer = Toolkit.getDefaultToolkit().createDesktopPeer(this);
    }

    /**
     * 返回当前浏览器上下文的 {@code Desktop} 实例。在某些平台上，Desktop API 可能不受支持；
     * 使用 {@link #isDesktopSupported} 方法来确定当前桌面是否受支持。
     * @return 当前浏览器上下文的 Desktop 实例
     * @throws HeadlessException 如果 {@link
     * GraphicsEnvironment#isHeadless()} 返回 {@code true}
     * @throws UnsupportedOperationException 如果当前平台不支持此类
     * @see #isDesktopSupported()
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public static synchronized Desktop getDesktop(){
        if (GraphicsEnvironment.isHeadless()) throw new HeadlessException();
        if (!Desktop.isDesktopSupported()) {
            throw new UnsupportedOperationException("Desktop API is not " +
                                                    "supported on the current platform");
        }

        sun.awt.AppContext context = sun.awt.AppContext.getAppContext();
        Desktop desktop = (Desktop)context.get(Desktop.class);

        if (desktop == null) {
            desktop = new Desktop();
            context.put(Desktop.class, desktop);
        }

        return desktop;
    }

    /**
     * 测试当前平台是否支持此类。如果支持，使用 {@link #getDesktop()} 获取一个实例。
     *
     * @return 如果当前平台支持此类，则返回 <code>true</code>；否则返回 <code>false</code>
     * @see #getDesktop()
     */
    public static boolean isDesktopSupported(){
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        if (defaultToolkit instanceof SunToolkit) {
            return ((SunToolkit)defaultToolkit).isDesktopSupported();
        }
        return false;
    }

    /**
     * 测试当前平台是否支持某个动作。
     *
     * <p>即使平台支持某个动作，文件或 URI 也可能没有注册用于该动作的应用程序。例如，大多数平台支持
     * {@link Desktop.Action#OPEN} 动作。但对于特定文件，可能没有注册用于打开它的应用程序。在这种情况下，
     * {@link #isSupported} 可能返回 {@code true}，但对应的动作方法将抛出 {@link IOException}。
     *
     * @param action 指定的 {@link Action}
     * @return 如果当前平台支持指定的动作，则返回 <code>true</code>；否则返回 <code>false</code>
     * @see Desktop.Action
     */
    public boolean isSupported(Action action) {
        return peer.isSupported(action);
    }

    /**
     * 检查文件是否为有效文件且可读。
     *
     * @throws SecurityException 如果存在安全管理器且其
     *         {@link SecurityManager#checkRead(java.lang.String)} 方法
     *         拒绝读取文件的权限
     * @throws NullPointerException 如果文件为 null
     * @throws IllegalArgumentException 如果文件不存在
     */
    private static void checkFileValidation(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("文件: "
                                               + file.getPath() + " 不存在。");
        }
    }

    /**
     * 检查动作类型是否受支持。
     *
     * @param actionType 要检查的动作类型
     * @throws UnsupportedOperationException 如果当前平台不支持指定的动作类型
     */
    private void checkActionSupport(Action actionType){
        if (!isSupported(actionType)) {
            throw new UnsupportedOperationException("当前平台不支持 " + actionType.name()
                                                    + " 动作！");
        }
    }


    /**
     * 调用安全管理器的 <code>checkPermission</code> 方法，使用
     * <code>AWTPermission("showWindowWithoutWarningBanner")</code>
     * 权限。
     */
    private void checkAWTPermission(){
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AWTPermission(
                                   "showWindowWithoutWarningBanner"));
        }
    }

    /**
     * 启动与文件关联的应用程序以打开文件。
     *
     * <p> 如果指定的文件是一个目录，则启动当前平台的文件管理器以打开它。
     *
     * @param file 要使用关联应用程序打开的文件
     * @throws NullPointerException 如果 {@code file} 为 {@code null}
     * @throws IllegalArgumentException 如果指定的文件不存在
     * @throws UnsupportedOperationException 如果当前平台不支持 {@link Desktop.Action#OPEN} 动作
     * @throws IOException 如果指定的文件没有关联的应用程序或关联的应用程序启动失败
     * @throws SecurityException 如果存在安全管理器且其
     * {@link java.lang.SecurityManager#checkRead(java.lang.String)}
     * 方法拒绝读取文件的权限，或其拒绝
     * <code>AWTPermission("showWindowWithoutWarningBanner")</code>
     * 权限，或调用线程不允许创建子进程
     * @see java.awt.AWTPermission
     */
    public void open(File file) throws IOException {
        file = new File(file.getPath());
        checkAWTPermission();
        checkExec();
        checkActionSupport(Action.OPEN);
        checkFileValidation(file);

        peer.open(file);
    }

    /**
     * 启动与文件关联的编辑器应用程序并打开文件以进行编辑。
     *
     * @param file 要打开以进行编辑的文件
     * @throws NullPointerException 如果指定的文件为 {@code null}
     * @throws IllegalArgumentException 如果指定的文件不存在
     * @throws UnsupportedOperationException 如果当前平台不支持 {@link Desktop.Action#EDIT} 动作
     * @throws IOException 如果指定的文件没有关联的编辑器，或关联的应用程序启动失败
     * @throws SecurityException 如果存在安全管理器且其
     * {@link java.lang.SecurityManager#checkRead(java.lang.String)}
     * 方法拒绝读取文件的权限，或其
     * {@link java.lang.SecurityManager#checkWrite(java.lang.String)} 方法
     * 拒绝写入文件的权限，或其拒绝
     * <code>AWTPermission("showWindowWithoutWarningBanner")</code>
     * 权限，或调用线程不允许创建子进程
     * @see java.awt.AWTPermission
     */
    public void edit(File file) throws IOException {
        file = new File(file.getPath());
        checkAWTPermission();
        checkExec();
        checkActionSupport(Action.EDIT);
        file.canWrite();
        checkFileValidation(file);

        peer.edit(file);
    }

    /**
     * 使用本地桌面打印功能和关联应用程序的打印命令打印文件。
     *
     * @param file 要打印的文件
     * @throws NullPointerException 如果指定的文件为 {@code null}
     * @throws IllegalArgumentException 如果指定的文件不存在
     * @throws UnsupportedOperationException 如果当前平台不支持 {@link Desktop.Action#PRINT} 动作
     * @throws IOException 如果指定的文件没有可以用于打印的关联应用程序
     * @throws SecurityException 如果存在安全管理器且其
     * {@link java.lang.SecurityManager#checkRead(java.lang.String)}
     * 方法拒绝读取文件的权限，或其
     * {@link java.lang.SecurityManager#checkPrintJobAccess()} 方法
     * 拒绝打印文件的权限，或调用线程不允许创建子进程
     */
    public void print(File file) throws IOException {
        file = new File(file.getPath());
        checkExec();
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPrintJobAccess();
        }
        checkActionSupport(Action.PRINT);
        checkFileValidation(file);

        peer.print(file);
    }

    /**
     * 启动默认浏览器以显示 {@code URI}。如果默认浏览器无法处理指定的
     * {@code URI}，则调用注册用于处理指定类型的 {@code URI} 的应用程序。应用程序
     * 由 {@code URI} 类定义的协议和路径确定。
     * <p>
     * 如果调用线程没有必要的权限，并且这是在 applet 中调用的，
     * 则使用 {@code AppletContext.showDocument()}。类似地，如果调用线程没有必要的权限，并且这是在
     * Java Web Started 应用程序中调用的，则使用 {@code BasicService.showDocument()}。
     *
     * @param uri 要在用户默认浏览器中显示的 URI
     * @throws NullPointerException 如果 {@code uri} 为 {@code null}
     * @throws UnsupportedOperationException 如果当前平台不支持 {@link Desktop.Action#BROWSE} 动作
     * @throws IOException 如果用户默认浏览器未找到，或其启动失败，或默认处理应用程序启动失败
     * @throws SecurityException 如果存在安全管理器且其
     * <code>AWTPermission("showWindowWithoutWarningBanner")</code>
     * 权限被拒绝，或调用线程不允许创建子进程；且不是在 applet 或 Java Web Started 应用程序中调用的
     * @throws IllegalArgumentException 如果必要的权限不可用且 URI 无法转换为 {@code URL}
     * @see java.net.URI
     * @see java.awt.AWTPermission
     * @see java.applet.AppletContext
     */
    public void browse(URI uri) throws IOException {
        SecurityException securityException = null;
        try {
            checkAWTPermission();
            checkExec();
        } catch (SecurityException e) {
            securityException = e;
        }
        checkActionSupport(Action.BROWSE);
        if (uri == null) {
            throw new NullPointerException();
        }
        if (securityException == null) {
            peer.browse(uri);
            return;
        }

        // 调用线程没有必要的权限。
        // 委托给 DesktopBrowse 以便在 applet/webstart 中工作。
        URL url = null;
        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("无法将 URI 转换为 URL", e);
        }
        sun.awt.DesktopBrowse db = sun.awt.DesktopBrowse.getInstance();
        if (db == null) {
            // 不在 webstart/applet 中，抛出异常。
            throw securityException;
        }
        db.browse(url);
    }


                /**
     * 启动用户默认邮件客户端的邮件撰写窗口。
     *
     * @throws UnsupportedOperationException 如果当前平台不支持 {@link Desktop.Action#MAIL} 操作
     * @throws IOException 如果找不到用户默认邮件客户端，或者启动失败
     * @throws SecurityException 如果存在安全经理并且它拒绝
     * <code>AWTPermission("showWindowWithoutWarningBanner")</code>
     * 权限，或者调用线程不允许创建子进程
     * @see java.awt.AWTPermission
     */
    public void mail() throws IOException {
        checkAWTPermission();
        checkExec();
        checkActionSupport(Action.MAIL);
        URI mailtoURI = null;
        try{
            mailtoURI = new URI("mailto:?");
            peer.mail(mailtoURI);
        } catch (URISyntaxException e){
            // 不会到达这里。
        }
    }

    /**
     * 启动用户默认邮件客户端的邮件撰写窗口，并使用 {@code
     * mailto:} URI 指定的消息字段填充消息。
     *
     * <p> 一个 <code>mailto:</code> URI 可以指定消息字段
     * 包括 <i>"to"</i>，<i>"cc"</i>，<i>"subject"</i>，
     * <i>"body"</i> 等。参见 <a
     * href="http://www.ietf.org/rfc/rfc2368.txt">The mailto URL
     * scheme (RFC 2368)</a> 了解 {@code mailto:} URI 规范的详细信息。
     *
     * @param mailtoURI 指定的 {@code mailto:} URI
     * @throws NullPointerException 如果指定的 URI 为 {@code
     * null}
     * @throws IllegalArgumentException 如果 URI 方案不是
     *         <code>"mailto"</code>
     * @throws UnsupportedOperationException 如果当前平台不支持
     * {@link Desktop.Action#MAIL} 操作
     * @throws IOException 如果找不到用户默认邮件客户端或启动失败
     * @throws SecurityException 如果存在安全经理并且它拒绝
     * <code>AWTPermission("showWindowWithoutWarningBanner")</code>
     * 权限，或者调用线程不允许创建子进程
     * @see java.net.URI
     * @see java.awt.AWTPermission
     */
    public  void mail(URI mailtoURI) throws IOException {
        checkAWTPermission();
        checkExec();
        checkActionSupport(Action.MAIL);
        if (mailtoURI == null) throw new NullPointerException();

        if (!"mailto".equalsIgnoreCase(mailtoURI.getScheme())) {
            throw new IllegalArgumentException("URI scheme is not \"mailto\"");
        }

        peer.mail(mailtoURI);
    }

    private void checkExec() throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new FilePermission("<<ALL FILES>>",
                                                  SecurityConstants.FILE_EXECUTE_ACTION));
        }
    }
}
