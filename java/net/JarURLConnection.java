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

package java.net;

import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.security.Permission;
import sun.net.www.ParseUtil;

/**
 * 一个到 Java ARchive (JAR) 文件或 JAR 文件中的条目的 URL 连接。
 *
 * <p>JAR URL 的语法是：
 *
 * <pre>
 * jar:&lt;url&gt;!/{entry}
 * </pre>
 *
 * <p>例如：
 *
 * <p>{@code jar:http://www.foo.com/bar/baz.jar!/COM/foo/Quux.class}
 *
 * <p>应使用 JAR URL 引用 JAR 文件或 JAR 文件中的条目。上面的示例是一个引用 JAR 条目的 JAR URL。如果省略了条目名称，URL 将引用整个 JAR 文件：
 *
 * {@code jar:http://www.foo.com/bar/baz.jar!/}
 *
 * <p>当用户知道他们创建的 URL 是 JAR URL，并且需要 JAR 特定的功能时，应将通用 URLConnection 转换为 JarURLConnection。例如：
 *
 * <pre>
 * URL url = new URL("jar:file:/home/duke/duke.jar!/");
 * JarURLConnection jarConnection = (JarURLConnection)url.openConnection();
 * Manifest manifest = jarConnection.getManifest();
 * </pre>
 *
 * <p>JarURLConnection 实例只能用于从 JAR 文件中读取。不能使用此类获取 {@link java.io.OutputStream} 来修改或写入底层的 JAR 文件。
 * <p>示例：
 *
 * <dl>
 *
 * <dt>JAR 条目
 * <dd>{@code jar:http://www.foo.com/bar/baz.jar!/COM/foo/Quux.class}
 *
 * <dt>JAR 文件
 * <dd>{@code jar:http://www.foo.com/bar/baz.jar!/}
 *
 * <dt>JAR 目录
 * <dd>{@code jar:http://www.foo.com/bar/baz.jar!/COM/foo/}
 *
 * </dl>
 *
 * <p>{@code !/} 被称为 <em>分隔符</em>。
 *
 * <p>当通过 {@code new URL(context, spec)} 构造 JAR URL 时，适用以下规则：
 *
 * <ul>
 *
 * <li>如果没有上下文 URL，并且传递给 URL 构造函数的规范中不包含分隔符，则认为该 URL 引用一个 JAR 文件。
 *
 * <li>如果有上下文 URL，则假定上下文 URL 引用一个 JAR 文件或 JAR 目录。
 *
 * <li>如果规范以 '/' 开头，则忽略 JAR 目录，认为规范位于 JAR 文件的根目录。
 *
 * <p>示例：
 *
 * <dl>
 *
 * <dt>上下文: <b>jar:http://www.foo.com/bar/jar.jar!/</b>,
 * 规范:<b>baz/entry.txt</b>
 *
 * <dd>URL:<b>jar:http://www.foo.com/bar/jar.jar!/baz/entry.txt</b>
 *
 * <dt>上下文: <b>jar:http://www.foo.com/bar/jar.jar!/baz</b>,
 * 规范:<b>entry.txt</b>
 *
 * <dd>URL:<b>jar:http://www.foo.com/bar/jar.jar!/baz/entry.txt</b>
 *
 * <dt>上下文: <b>jar:http://www.foo.com/bar/jar.jar!/baz</b>,
 * 规范:<b>/entry.txt</b>
 *
 * <dd>URL:<b>jar:http://www.foo.com/bar/jar.jar!/entry.txt</b>
 *
 * </dl>
 *
 * </ul>
 *
 * @see java.net.URL
 * @see java.net.URLConnection
 *
 * @see java.util.jar.JarFile
 * @see java.util.jar.JarInputStream
 * @see java.util.jar.Manifest
 * @see java.util.zip.ZipEntry
 *
 * @author Benjamin Renaud
 * @since 1.2
 */
public abstract class JarURLConnection extends URLConnection {

    private URL jarFileURL;
    private String entryName;

    /**
     * 如果已初始化连接，则返回到 JAR 文件 URL 的连接。应由 connect 设置。
     */
    protected URLConnection jarFileURLConnection;

    /**
     * 创建到指定 URL 的新 JarURLConnection。
     * @param url URL
     * @throws MalformedURLException 如果在规范字符串中找不到合法的协议或字符串无法解析。
     */

    protected JarURLConnection(URL url) throws MalformedURLException {
        super(url);
        parseSpecs(url);
    }

    /* 从缓存中获取给定 URL 的规范，如果缓存中没有，则计算并缓存它们。 */
    private void parseSpecs(URL url) throws MalformedURLException {
        String spec = url.getFile();

        int separator = spec.indexOf("!/");
        /*
         * REMIND: 我们不处理嵌套的 JAR URL
         */
        if (separator == -1) {
            throw new MalformedURLException("在 URL 规范中未找到 !/：" + spec);
        }

        jarFileURL = new URL(spec.substring(0, separator++));
        entryName = null;

        /* 如果 ! 是 innerURL 的最后一个字母，则 entryName 为 null */
        if (++separator != spec.length()) {
            entryName = spec.substring(separator, spec.length());
            entryName = ParseUtil.decode(entryName);
        }
    }

    /**
     * 返回此连接的 JAR 文件的 URL。
     *
     * @return 此连接的 JAR 文件的 URL。
     */
    public URL getJarFileURL() {
        return jarFileURL;
    }

    /**
     * 返回此连接的条目名称。如果此连接对应的 JAR 文件 URL 指向 JAR 文件而不是 JAR 文件条目，则返回 null。
     *
     * @return 此连接的条目名称，如果有的话。
     */
    public String getEntryName() {
        return entryName;
    }

    /**
     * 返回此连接的 JAR 文件。
     *
     * @return 此连接的 JAR 文件。如果连接是指向 JAR 文件的条目，则返回 JAR 文件对象。
     *
     * @exception IOException 如果在尝试连接到此连接的 JAR 文件时发生 IOException。
     *
     * @see #connect
     */
    public abstract JarFile getJarFile() throws IOException;

    /**
     * 返回此连接的清单，如果没有则返回 null。
     *
     * @return 与此连接的 JAR 文件对象对应的清单对象。
     *
     * @exception IOException 如果在获取此连接的 JAR 文件时引发 IOException。
     *
     * @see #getJarFile
     */
    public Manifest getManifest() throws IOException {
        return getJarFile().getManifest();
    }

    /**
     * 返回此连接的 JAR 条目对象，如果有的话。如果此连接对应的 JAR 文件 URL 指向 JAR 文件而不是 JAR 文件条目，则返回 null。
     *
     * @return 此连接的 JAR 条目对象，如果此连接的 JAR URL 指向 JAR 文件，则返回 null。
     *
     * @exception IOException 如果在获取此连接的 JAR 文件时引发 IOException。
     *
     * @see #getJarFile
     * @see #getJarEntry
     */
    public JarEntry getJarEntry() throws IOException {
        return getJarFile().getJarEntry(entryName);
    }

    /**
     * 如果此连接的 URL 指向 JAR 文件条目，则返回此连接的属性对象，否则返回 null。
     *
     * @return 如果此连接的 URL 指向 JAR 文件条目，则返回此连接的属性对象，否则返回 null。
     *
     * @exception IOException 如果在获取 JAR 条目时引发 IOException。
     *
     * @see #getJarEntry
     */
    public Attributes getAttributes() throws IOException {
        JarEntry e = getJarEntry();
        return e != null ? e.getAttributes() : null;
    }

    /**
     * 返回此连接的 JAR 文件的主属性。
     *
     * @return 此连接的 JAR 文件的主属性。
     *
     * @exception IOException 如果在获取清单时引发 IOException。
     *
     * @see #getJarFile
     * @see #getManifest
     */
    public Attributes getMainAttributes() throws IOException {
        Manifest man = getManifest();
        return man != null ? man.getMainAttributes() : null;
    }

    /**
     * 如果此连接的 URL 指向 JAR 文件条目，则返回此连接的证书对象，否则返回 null。此方法只能在连接完全验证后调用，即从输入流读取到流的末尾。否则，此方法将返回 {@code null}。
     *
     * @return 如果此连接的 URL 指向 JAR 文件条目，则返回此连接的证书对象，否则返回 null。
     *
     * @exception IOException 如果在获取 JAR 条目时引发 IOException。
     *
     * @see #getJarEntry
     */
    public java.security.cert.Certificate[] getCertificates()
         throws IOException
    {
        JarEntry e = getJarEntry();
        return e != null ? e.getCertificates() : null;
    }
}
