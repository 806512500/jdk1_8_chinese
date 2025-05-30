/*
 * Copyright (c) 2002, 2018, Oracle and/or its affiliates. All rights reserved.
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
import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.List;
import sun.misc.JavaUtilJarAccess;

class JavaUtilJarAccessImpl implements JavaUtilJarAccess {
    /**
     * 检查 JarFile 是否具有 Class-Path 属性。
     *
     * @param jar JarFile 对象
     * @return 如果 JarFile 具有 Class-Path 属性，则返回 true；否则返回 false
     * @throws IOException 如果发生 I/O 错误
     */
    public boolean jarFileHasClassPathAttribute(JarFile jar) throws IOException {
        return jar.hasClassPathAttribute();
    }

    /**
     * 获取 JarFile 的 CodeSource 数组。
     *
     * @param jar JarFile 对象
     * @param url URL 对象
     * @return CodeSource 数组
     */
    public CodeSource[] getCodeSources(JarFile jar, URL url) {
        return jar.getCodeSources(url);
    }

    /**
     * 获取 JarFile 的 CodeSource。
     *
     * @param jar JarFile 对象
     * @param url URL 对象
     * @param name 名称
     * @return CodeSource 对象
     */
    public CodeSource getCodeSource(JarFile jar, URL url, String name) {
        return jar.getCodeSource(url, name);
    }

    /**
     * 获取 JarFile 的条目名称。
     *
     * @param jar JarFile 对象
     * @param cs CodeSource 数组
     * @return 条目名称的 Enumeration
     */
    public Enumeration<String> entryNames(JarFile jar, CodeSource[] cs) {
        return jar.entryNames(cs);
    }

    /**
     * 获取 JarFile 的条目。
     *
     * @param jar JarFile 对象
     * @return 条目的 Enumeration
     */
    public Enumeration<JarEntry> entries2(JarFile jar) {
        return jar.entries2();
    }

    /**
     * 设置 JarFile 的验证模式。
     *
     * @param jar JarFile 对象
     * @param eager 如果为 true，则启用急切验证；否则禁用
     */
    public void setEagerValidation(JarFile jar, boolean eager) {
        jar.setEagerValidation(eager);
    }

    /**
     * 获取 JarFile 的清单摘要。
     *
     * @param jar JarFile 对象
     * @return 清单摘要的 List
     */
    public List<Object> getManifestDigests(JarFile jar) {
        return jar.getManifestDigests();
    }

    /**
     * 获取 Manifest 的可信属性。
     *
     * @param man Manifest 对象
     * @param name 名称
     * @return 可信属性
     */
    public Attributes getTrustedAttributes(Manifest man, String name) {
        return man.getTrustedAttributes(name);
    }

    /**
     * 确保 JarFile 已初始化。
     *
     * @param jar JarFile 对象
     */
    public void ensureInitialization(JarFile jar) {
        jar.ensureInitialization();
    }
}
