/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.file.spi;

import java.nio.file.Path;
import java.io.IOException;

/**
 * 用于探测文件以猜测其文件类型的文件类型检测器。
 *
 * <p> 文件类型检测器是此类的具体实现，具有无参数构造函数，并实现以下指定的抽象方法。
 *
 * <p> 文件类型检测器确定文件类型的方式高度依赖于具体实现。简单的实现可能检查文件的
 * <em>文件扩展名</em>（某些平台使用的一种惯例）并将其映射到文件类型。在其他情况下，文件类型可能
 * 作为文件的<a href="../attribute/package-summary.html">属性</a>存储，或者检查文件中的字节以猜测其文件类型。
 *
 * @see java.nio.file.Files#probeContentType(Path)
 *
 * @since 1.7
 */

public abstract class FileTypeDetector {

    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("fileTypeDetector"));
        return null;
    }
    private FileTypeDetector(Void ignore) { }

    /**
     * 初始化此类的新实例。
     *
     * @throws  SecurityException
     *          如果已安装了安全经理并且它拒绝
     *          {@link RuntimePermission}<tt>("fileTypeDetector")</tt>
     */
    protected FileTypeDetector() {
        this(checkPermission());
    }

    /**
     * 探测给定文件以猜测其内容类型。
     *
     * <p> 本方法确定文件类型的方式高度依赖于具体实现。它可能仅检查文件名，可能使用文件
     * <a href="../attribute/package-summary.html">属性</a>，或者检查文件中的字节。
     *
     * <p> 探测结果是多用途互联网邮件扩展（MIME）内容类型的值的字符串形式，如
     * <a href="http://www.ietf.org/rfc/rfc2045.txt"><i>RFC&nbsp;2045:
     * 多用途互联网邮件扩展（MIME）第一部分：互联网消息体格式</i></a>中定义。字符串必须能够根据
     * RFC 2045 中的语法进行解析。
     *
     * @param   path
     *          要探测的文件的路径
     *
     * @return  内容类型或如果文件类型未被识别则返回 {@code null}
     *
     * @throws  IOException
     *          发生 I/O 错误
     * @throws  SecurityException
     *          如果实现需要访问文件，并且已安装了安全经理，且它拒绝了文件系统提供程序实现所需的未指定权限。
     *          如果文件引用与默认文件系统提供程序相关联，则调用 {@link SecurityManager#checkRead(String)} 方法
     *          检查对文件的读取权限。
     *
     * @see java.nio.file.Files#probeContentType
     */
    public abstract String probeContentType(Path path)
        throws IOException;
}
