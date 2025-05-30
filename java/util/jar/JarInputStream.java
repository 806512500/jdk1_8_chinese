/*
 * Copyright (c) 1997, 2021, Oracle and/or its affiliates. All rights reserved.
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

package java.util.jar;

import java.util.zip.*;
import java.io.*;
import sun.security.util.ManifestEntryVerifier;
import sun.misc.JarIndex;

/**
 * <code>JarInputStream</code> 类用于从任何输入流中读取 JAR 文件的内容。它扩展了 <code>java.util.zip.ZipInputStream</code> 类，
 * 并支持读取可选的 <code>Manifest</code> 条目。<code>Manifest</code> 可用于存储关于 JAR 文件及其条目的元信息。
 *
 * @author  David Connelly
 * @see     Manifest
 * @see     java.util.zip.ZipInputStream
 * @since   1.2
 */
public
class JarInputStream extends ZipInputStream {
    private Manifest man;
    private JarEntry first;
    private JarVerifier jv;
    private ManifestEntryVerifier mev;
    private final boolean doVerify;
    private boolean tryManifest;

    /**
     * 创建一个新的 <code>JarInputStream</code> 并读取可选的清单。如果存在清单，还会尝试验证签名（如果 JAR 文件已签名）。
     * @param in 实际的输入流
     * @exception IOException 如果发生 I/O 错误
     */
    public JarInputStream(InputStream in) throws IOException {
        this(in, true);
    }

    /**
     * 创建一个新的 <code>JarInputStream</code> 并读取可选的清单。如果存在清单且 verify 为 true，还会尝试验证签名（如果 JAR 文件已签名）。
     *
     * @param in 实际的输入流
     * @param verify 是否验证 JAR 文件的签名（如果已签名）
     * @exception IOException 如果发生 I/O 错误
     */
    public JarInputStream(InputStream in, boolean verify) throws IOException {
        super(in);
        this.doVerify = verify;

        // 该实现假设 META-INF/MANIFEST.MF 条目应该是第一个或第二个条目（在 META-INF/ 之前）。它跳过 META-INF/ 并
        // “消耗” MANIFEST.MF 以初始化 Manifest 对象。
        JarEntry e = (JarEntry)super.getNextEntry();
        if (e != null && e.getName().equalsIgnoreCase("META-INF/"))
            e = (JarEntry)super.getNextEntry();
        first = checkManifest(e);
    }

    private JarEntry checkManifest(JarEntry e)
        throws IOException
    {
        if (e != null && JarFile.MANIFEST_NAME.equalsIgnoreCase(e.getName())) {
            man = new Manifest();
            byte bytes[] = getBytes(new BufferedInputStream(this));
            man.read(new ByteArrayInputStream(bytes));
            closeEntry();
            if (doVerify) {
                jv = new JarVerifier(e.getName(), bytes);
                mev = new ManifestEntryVerifier(man, jv.manifestName);
            }
            JarEntry nextEntry = (JarEntry)super.getNextEntry();
            if (nextEntry != null &&
                    JarFile.MANIFEST_NAME.equalsIgnoreCase(nextEntry.getName())) {
                if (JarVerifier.debug != null) {
                    JarVerifier.debug.println(JarVerifier.MULTIPLE_MANIFEST_WARNING);
                }

                jv = null;
                mev = null;
            }
            return nextEntry;
        }
        return e;
    }

    private byte[] getBytes(InputStream is)
        throws IOException
    {
        byte[] buffer = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        int n;
        while ((n = is.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, n);
        }
        return baos.toByteArray();
    }

    /**
     * 返回此 JAR 文件的 <code>Manifest</code>，如果不存在则返回 <code>null</code>。
     *
     * @return 此 JAR 文件的 <code>Manifest</code>，如果不存在则返回 <code>null</code>。
     */
    public Manifest getManifest() {
        return man;
    }

    /**
     * 读取下一个 ZIP 文件条目并将流定位到条目数据的开头。如果启用了验证，定位到下一个条目时检测到的任何无效签名都会导致异常。
     * @exception ZipException 如果发生 ZIP 文件错误
     * @exception IOException 如果发生 I/O 错误
     * @exception SecurityException 如果 JAR 文件的任何条目签名不正确
     */
    public ZipEntry getNextEntry() throws IOException {
        JarEntry e;
        if (first == null) {
            e = (JarEntry)super.getNextEntry();
            if (tryManifest) {
                e = checkManifest(e);
                tryManifest = false;
            }
        } else {
            e = first;
            if (first.getName().equalsIgnoreCase(JarIndex.INDEX_NAME))
                tryManifest = true;
            first = null;
        }
        if (jv != null && e != null) {
            // 此时，我们可能已经解析了所有元信息条目并且没有需要验证的内容。如果没有需要验证的内容，删除 JarVerifier 对象。
            if (jv.nothingToVerify() == true) {
                jv = null;
                mev = null;
            } else {
                jv.beginEntry(e, mev);
            }
        }
        return e;
    }

    /**
     * 读取下一个 JAR 文件条目并将流定位到条目数据的开头。如果启用了验证，定位到下一个条目时检测到的任何无效签名都会导致异常。
     * @return 下一个 JAR 文件条目，如果没有更多条目则返回 null
     * @exception ZipException 如果发生 ZIP 文件错误
     * @exception IOException 如果发生 I/O 错误
     * @exception SecurityException 如果 JAR 文件的任何条目签名不正确
     */
    public JarEntry getNextJarEntry() throws IOException {
        return (JarEntry)getNextEntry();
    }

    /**
     * 从当前 JAR 文件条目读取到字节数组中。如果 <code>len</code> 不为零，该方法将阻塞直到有输入可用；否则，不读取任何字节并返回 <code>0</code>。
     * 如果启用了验证，当前条目的任何无效签名将在到达条目末尾之前的某个时间点报告。
     * @param b 存储数据的缓冲区
     * @param off 目标数组 <code>b</code> 中的起始偏移量
     * @param len 要读取的最大字节数
     * @return 实际读取的字节数，如果到达条目末尾则返回 -1
     * @exception  NullPointerException 如果 <code>b</code> 为 <code>null</code>
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负数，<code>len</code> 为负数，或 <code>len</code> 大于 <code>b.length - off</code>
     * @exception ZipException 如果发生 ZIP 文件错误
     * @exception IOException 如果发生 I/O 错误
     * @exception SecurityException 如果 JAR 文件的任何条目签名不正确
     */
    public int read(byte[] b, int off, int len) throws IOException {
        int n;
        if (first == null) {
            n = super.read(b, off, len);
        } else {
            n = -1;
        }
        if (jv != null) {
            jv.update(n, b, off, len, mev);
        }
        return n;
    }

    /**
     * 为指定的 JAR 文件条目名称创建一个新的 <code>JarEntry</code>（<code>ZipEntry</code>）。指定的 JAR 文件条目名称的清单属性将复制到新的 <code>JarEntry</code> 中。
     *
     * @param name JAR/ZIP 文件条目的名称
     * @return 刚创建的 <code>JarEntry</code> 对象
     */
    protected ZipEntry createZipEntry(String name) {
        JarEntry e = new JarEntry(name);
        if (man != null) {
            e.attr = man.getAttributes(name);
        }
        return e;
    }
}
