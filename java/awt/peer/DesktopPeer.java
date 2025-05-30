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

package java.awt.peer;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.awt.Desktop.Action;

/**
 * {@code DesktopPeer} 接口提供了通过启动关联应用程序来操作打开、编辑、打印、浏览和发送邮件的方法，
 * 通过给定的 URL 或文件。
 * <p>
 * 每个平台都有这个接口的实现类。
 *
 */
public interface DesktopPeer {

    /**
     * 返回当前平台是否支持给定的操作。
     * @param action 要测试的当前平台是否支持的操作类型。
     * @return 如果当前平台支持给定的操作，则返回 {@code true}；否则返回 {@code false}。
     */
    boolean isSupported(Action action);

    /**
     * 启动关联的应用程序来打开给定的文件。关联的应用程序被注册为给定文件类型的默认文件查看器。
     *
     * @param file 给定的文件。
     * @throws IOException 如果给定的文件没有关联的应用程序，或者关联的应用程序启动失败。
     */
    void open(File file) throws IOException;

    /**
     * 启动关联的编辑器并打开给定的文件进行编辑。关联的编辑器被注册为给定文件类型的默认编辑器。
     *
     * @param file 给定的文件。
     * @throws IOException 如果给定的文件没有关联的编辑器，或者关联的应用程序启动失败。
     */
    void edit(File file) throws IOException;

    /**
     * 使用关联应用程序的打印命令，通过本机桌面打印功能打印给定的文件。
     *
     * @param file 给定的文件。
     * @throws IOException 如果给定的文件没有可以用于打印的关联应用程序。
     */
    void print(File file) throws IOException;

    /**
     * 启动用户的默认邮件客户端的邮件撰写窗口，并使用给定的 mailto URL 填充消息字段，包括收件人、抄送等。
     *
     * @param mailtoURL 表示一个带有指定消息值的 mailto URL。
     *        mailto URL 的语法由
     *        <a href="http://www.ietf.org/rfc/rfc2368.txt">RFC2368: The mailto
     *        URL scheme</a> 定义。
     * @throws IOException 如果找不到用户的默认邮件客户端，或者启动失败。
     */
    void mail(URI mailtoURL) throws IOException;

    /**
     * 启动用户的默认浏览器来显示给定的 URI。
     *
     * @param uri 给定的 URI。
     * @throws IOException 如果找不到用户的默认浏览器，或者启动失败。
     */
    void browse(URI uri) throws IOException;
}
