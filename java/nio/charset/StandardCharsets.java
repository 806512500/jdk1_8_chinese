/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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
package java.nio.charset;

/**
 * 标准 {@link Charset Charsets} 的常量定义。这些字符集在每个 Java 平台的实现中都保证可用。
 *
 * @see <a href="Charset#standard">标准字符集</a>
 * @since 1.7
 */
public final class StandardCharsets {

    private StandardCharsets() {
        throw new AssertionError("没有 java.nio.charset.StandardCharsets 的实例给你！");
    }
    /**
     * 七位 ASCII，也称为 ISO646-US，也称为 Unicode 字符集的基本拉丁块
     */
    public static final Charset US_ASCII = Charset.forName("US-ASCII");
    /**
     * ISO 拉丁字母表 No. 1，也称为 ISO-LATIN-1
     */
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    /**
     * 八位 UCS 转换格式
     */
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    /**
     * 十六位 UCS 转换格式，大端字节序
     */
    public static final Charset UTF_16BE = Charset.forName("UTF-16BE");
    /**
     * 十六位 UCS 转换格式，小端字节序
     */
    public static final Charset UTF_16LE = Charset.forName("UTF-16LE");
    /**
     * 十六位 UCS 转换格式，字节序由可选的字节顺序标记识别
     */
    public static final Charset UTF_16 = Charset.forName("UTF-16");
}
