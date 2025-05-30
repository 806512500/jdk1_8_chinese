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

package java.util.jar;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.security.CodeSigner;
import java.security.cert.Certificate;

/**
 * 该类用于表示 JAR 文件条目。
 */
public
class JarEntry extends ZipEntry {
    Attributes attr;
    Certificate[] certs;
    CodeSigner[] signers;

    /**
     * 为指定的 JAR 文件条目名称创建一个新的 <code>JarEntry</code>。
     *
     * @param name JAR 文件条目名称
     * @exception NullPointerException 如果条目名称为 <code>null</code>
     * @exception IllegalArgumentException 如果条目名称长度超过 0xFFFF 字节。
     */
    public JarEntry(String name) {
        super(name);
    }

    /**
     * 从指定的 <code>ZipEntry</code> 对象的字段创建一个新的 <code>JarEntry</code>。
     * @param ze 用于创建 <code>JarEntry</code> 的 <code>ZipEntry</code> 对象
     */
    public JarEntry(ZipEntry ze) {
        super(ze);
    }

    /**
     * 从指定的 <code>JarEntry</code> 对象的字段创建一个新的 <code>JarEntry</code>。
     *
     * @param je 要复制的 <code>JarEntry</code>
     */
    public JarEntry(JarEntry je) {
        this((ZipEntry)je);
        this.attr = je.attr;
        this.certs = je.certs;
        this.signers = je.signers;
    }

    /**
     * 返回此条目的 <code>Manifest</code> <code>Attributes</code>，如果不存在则返回 <code>null</code>。
     *
     * @return 此条目的 <code>Manifest</code> <code>Attributes</code>，如果不存在则返回 <code>null</code>
     * @throws IOException  如果发生 I/O 错误
     */
    public Attributes getAttributes() throws IOException {
        return attr;
    }

    /**
     * 返回此条目的 <code>Certificate</code> 对象，如果不存在则返回 <code>null</code>。此方法只能在从条目输入流读取到流末尾后调用。否则，此方法将返回 <code>null</code>。
     *
     * <p>返回的证书数组包含用于验证此条目的所有签名者证书。每个签名者证书后面跟着其支持的证书链（可能为空）。每个签名者证书及其支持的证书链按从下到上的顺序排列（即，签名者证书在前，（根）证书机构在后）。
     *
     * @return 此条目的 <code>Certificate</code> 对象，如果不存在则返回 <code>null</code>。
     */
    public Certificate[] getCertificates() {
        return certs == null ? null : certs.clone();
    }

    /**
     * 返回此条目的 <code>CodeSigner</code> 对象，如果不存在则返回 <code>null</code>。此方法只能在从条目输入流读取到流末尾后调用。否则，此方法将返回 <code>null</code>。
     *
     * <p>返回的数组包含已签署此条目的所有代码签名者。
     *
     * @return 此条目的 <code>CodeSigner</code> 对象，如果不存在则返回 <code>null</code>。
     *
     * @since 1.5
     */
    public CodeSigner[] getCodeSigners() {
        return signers == null ? null : signers.clone();
    }
}
