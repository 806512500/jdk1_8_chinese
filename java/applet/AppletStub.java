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

import java.net.URL;

/**
 * 当第一次创建 applet 时，会使用 applet 的 <code>setStub</code> 方法将 applet 存根附加到它上。
 * 这个存根作为 applet 和浏览器环境或 applet 查看器环境之间的接口。
 *
 * @author      Arthur van Hoff
 * @see         java.applet.Applet#setStub(java.applet.AppletStub)
 * @since       JDK1.0
 */
public interface AppletStub {
    /**
     * 确定 applet 是否处于活动状态。applet 在其 <code>start</code> 方法被调用之前处于活动状态。
     * 它在 <code>stop</code> 方法被调用之前变为非活动状态。
     *
     * @return  如果 applet 处于活动状态，则返回 <code>true</code>；
     *          否则返回 <code>false</code>。
     */
    boolean isActive();

    /**
     * 获取包含 applet 的文档的 URL。例如，假设 applet 包含在以下文档中：
     * <blockquote><pre>
     *    http://www.oracle.com/technetwork/java/index.html
     * </pre></blockquote>
     * 文档基址为：
     * <blockquote><pre>
     *    http://www.oracle.com/technetwork/java/index.html
     * </pre></blockquote>
     *
     * @return  包含 applet 的文档的 {@link java.net.URL}。
     * @see     java.applet.AppletStub#getCodeBase()
     */
    URL getDocumentBase();

    /**
     * 获取基 URL。这是包含 applet 的目录的 URL。
     *
     * @return  包含 applet 的目录的基 {@link java.net.URL}。
     * @see     java.applet.AppletStub#getDocumentBase()
     */
    URL getCodeBase();

    /**
     * 返回 HTML 标签中命名参数的值。例如，如果 applet 的指定方式如下：
     * <blockquote><pre>
     * &lt;applet code="Clock" width=50 height=50&gt;
     * &lt;param name=Color value="blue"&gt;
     * &lt;/applet&gt;
     * </pre></blockquote>
     * <p>
     * 那么调用 <code>getParameter("Color")</code> 将返回值 <code>"blue"</code>。
     *
     * @param   name   参数名称。
     * @return  命名参数的值，如果未设置则返回 <tt>null</tt>。
     */
    String getParameter(String name);

    /**
     * 返回 applet 的上下文。
     *
     * @return  applet 的上下文。
     */
    AppletContext getAppletContext();

    /**
     * 当 applet 希望调整大小时调用。
     *
     * @param   width    applet 的新请求宽度。
     * @param   height   applet 的新请求高度。
     */
    void appletResize(int width, int height);
}
