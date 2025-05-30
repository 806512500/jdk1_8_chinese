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

import java.awt.Image;
import java.awt.Graphics;
import java.awt.image.ColorModel;
import java.net.URL;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * 此接口对应于 applet 的环境：包含 applet 的文档和其他在同一文档中的 applet。
 * <p>
 * 该接口中的方法可用于 applet 获取其环境的信息。
 *
 * @author      Arthur van Hoff
 * @since       JDK1.0
 */
public interface AppletContext {
    /**
     * 创建一个音频剪辑。
     *
     * @param   url   指定音频剪辑位置的绝对 URL。
     * @return  指定 URL 的音频剪辑。
     */
    AudioClip getAudioClip(URL url);

    /**
     * 返回一个可以绘制到屏幕上的 <code>Image</code> 对象。传递的 <code>url</code> 参数必须指定一个绝对 URL。
     * <p>
     * 无论图像是否存在，此方法总是立即返回。当 applet 尝试在屏幕上绘制图像时，数据将被加载。绘制图像的图形原语将逐步在屏幕上绘制。
     *
     * @param   url   指定图像位置的绝对 URL。
     * @return  指定 URL 的图像。
     * @see     java.awt.Image
     */
    Image getImage(URL url);

    /**
     * 查找并返回此 applet 上下文中表示的文档中具有给定名称的 applet。名称可以在 HTML 标签中通过设置 <code>name</code> 属性来设置。
     *
     * @param   name   applet 名称。
     * @return  具有给定名称的 applet，如果未找到则返回 <code>null</code>。
     */
    Applet getApplet(String name);

    /**
     * 查找此 applet 上下文中表示的文档中的所有 applet。
     *
     * @return  此 applet 上下文中表示的文档中的所有 applet 的枚举。
     */
    Enumeration<Applet> getApplets();

    /**
     * 请求浏览器或 applet 查看器显示由 <code>url</code> 参数指定的 Web 页面。浏览器或 applet 查看器确定在哪个窗口或框架中显示 Web 页面。此方法可能被非浏览器的 applet 上下文忽略。
     *
     * @param   url   指定文档位置的绝对 URL。
     */
    void showDocument(URL url);

    /**
     * 请求浏览器或 applet 查看器显示由 <code>url</code> 参数指定的 Web 页面。<code>target</code> 参数指示在哪个 HTML 框架中显示文档。
     * <code>target</code> 参数的解释如下：
     *
     * <center><table border="3" summary="Target arguments and their descriptions">
     * <tr><th>Target Argument</th><th>Description</th></tr>
     * <tr><td><code>"_self"</code>  <td>在包含 applet 的窗口和框架中显示。</tr>
     * <tr><td><code>"_parent"</code><td>在 applet 的父框架中显示。如果 applet 的框架没有父框架，则与 "_self" 相同。</tr>
     * <tr><td><code>"_top"</code>   <td>在 applet 窗口的顶级框架中显示。如果 applet 的框架是顶级框架，则与 "_self" 相同。</tr>
     * <tr><td><code>"_blank"</code> <td>在新的、未命名的顶级窗口中显示。</tr>
     * <tr><td><i>name</i><td>在名为 <i>name</i> 的框架或窗口中显示。如果名为 <i>name</i> 的目标尚不存在，则创建一个新的顶级窗口，并在其中显示文档。</tr>
     * </table> </center>
     * <p>
     * applet 查看器或浏览器可以忽略 <code>showDocument</code>。
     *
     * @param   url   指定文档位置的绝对 URL。
     * @param   target   指示显示页面位置的 <code>String</code>。
     */
    public void showDocument(URL url, String target);

    /**
     * 请求在“状态窗口”中显示参数字符串。许多浏览器和 applet 查看器都提供这样的窗口，应用程序可以在其中向用户通知其当前状态。
     *
     * @param   status   要在状态窗口中显示的字符串。
     */
    void showStatus(String status);

    /**
     * 将指定的流与此 applet 上下文中的指定键关联。如果 applet 上下文之前包含此键的映射，则旧值将被替换。
     * <p>
     * 由于安全原因，流和键的映射存在于每个代码库中。换句话说，来自一个代码库的 applet 无法访问来自不同代码库的 applet 创建的流。
     * <p>
     * @param key 要与指定值关联的键。
     * @param stream 要与指定键关联的流。如果此参数为 <code>null</code>，则在 applet 上下文中删除指定的键。
     * @throws IOException 如果流大小超过某个大小限制。大小限制由实现此接口的提供者决定。
     * @since 1.4
     */
    public void setStream(String key, InputStream stream)throws IOException;

    /**
     * 返回与此 applet 上下文中的指定键关联的流。如果 applet 上下文中没有此键的流，则返回 <tt>null</tt>。
     * <p>
     * 由于安全原因，流和键的映射存在于每个代码库中。换句话说，来自一个代码库的 applet 无法访问来自不同代码库的 applet 创建的流。
     * <p>
     * @return 与此 applet 上下文映射的键关联的流。
     * @param key 要返回其关联流的键。
     * @since 1.4
     */
    public InputStream getStream(String key);

    /**
     * 查找此 applet 上下文中的所有流的键。
     * <p>
     * 由于安全原因，流和键的映射存在于每个代码库中。换句话说，来自一个代码库的 applet 无法访问来自不同代码库的 applet 创建的流。
     * <p>
     * @return 此 applet 上下文中的所有流名称的迭代器。
     * @since 1.4
     */
    public Iterator<String> getStreamKeys();
}
