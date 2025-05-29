/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
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
        throw new AssertionError("No java.nio.charset.StandardCharsets instances for you!");
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
     * 十六位 UCS 转换格式，字节序由可选的字节顺序标记标识
     */
    public static final Charset UTF_16 = Charset.forName("UTF-16");
}
