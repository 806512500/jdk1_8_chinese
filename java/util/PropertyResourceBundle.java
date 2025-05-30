
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */

package java.util;

import java.io.InputStream;
import java.io.Reader;
import java.io.IOException;
import sun.util.ResourceBundleEnumeration;

/**
 * <code>PropertyResourceBundle</code> 是 <code>ResourceBundle</code> 的具体子类，
 * 用于使用属性文件中的静态字符串集管理特定区域设置的资源。有关资源包的更多信息，请参见
 * {@link ResourceBundle ResourceBundle}。
 *
 * <p>
 * 与其他类型的资源包不同，您不需要子类化 <code>PropertyResourceBundle</code>。相反，您需要提供包含资源数据的属性文件。
 * <code>ResourceBundle.getBundle</code> 将自动查找适当的属性文件并创建一个引用该文件的 <code>PropertyResourceBundle</code>。
 * 有关搜索和实例化策略的完整描述，请参见
 * {@link ResourceBundle#getBundle(java.lang.String, java.util.Locale, java.lang.ClassLoader) ResourceBundle.getBundle}。
 *
 * <p>
 * 以下 <a name="sample">示例</a> 显示了基名为 "MyResources" 的资源包系列的成员。
 * 该文本定义了资源包 "MyResources_de"，即该系列的德语成员。
 * 该成员基于 <code>PropertyResourceBundle</code>，因此文本是文件 "MyResources_de.properties" 的内容
 * （一个相关的 <a href="ListResourceBundle.html#sample">示例</a> 展示了如何向此系列添加实现为 <code>ListResourceBundle</code> 子类的资源包）。
 * 本示例中的键形式为 "s1" 等。实际键完全由您选择，只要它们与您在程序中用于从资源包中检索对象的键相同即可。
 * 键是区分大小写的。
 * <blockquote>
 * <pre>
 * # MessageFormat 模式
 * s1=Die Platte \"{1}\" enth&auml;lt {0}.
 *
 * # 模式中的 {0} 位置
 * s2=1
 *
 * # 示例磁盘名称
 * s3=Meine Platte
 *
 * # 第一个 ChoiceFormat 选项
 * s4=keine Dateien
 *
 * # 第二个 ChoiceFormat 选项
 * s5=eine Datei
 *
 * # 第三个 ChoiceFormat 选项
 * s6={0,number} Dateien
 *
 * # 示例日期
 * s7=3. M&auml;rz 1996
 * </pre>
 * </blockquote>
 *
 * <p>
 * 如果一个 {@code PropertyResourceBundle} 实例被多个线程同时使用，则其实现必须是线程安全的。
 * 本类中非抽象方法的默认实现是线程安全的。
 *
 * <p>
 * <strong>注意：</strong> PropertyResourceBundle 可以从一个 {@link java.io.InputStream InputStream} 或一个 {@link java.io.Reader Reader} 构建，表示一个属性文件。
 * 从 {@link java.io.InputStream InputStream} 构建 PropertyResourceBundle 实例时，要求输入流必须编码为 ISO-8859-1。
 * 在这种情况下，无法用 ISO-8859-1 编码表示的字符必须用 Unicode 转义表示，如《Java&trade; 语言规范》第 3.3 节所定义的那样。
 * 而使用 {@link java.io.Reader Reader} 的构造函数则没有此限制。
 *
 * @see ResourceBundle
 * @see ListResourceBundle
 * @see Properties
 * @since JDK1.1
 */
public class PropertyResourceBundle extends ResourceBundle {
    /**
     * 从一个 {@link java.io.InputStream InputStream} 创建一个属性资源包。
     * 使用此构造函数读取的属性文件必须编码为 ISO-8859-1。
     *
     * @param stream 一个表示属性文件的 InputStream。
     * @throws IOException 如果发生 I/O 错误
     * @throws NullPointerException 如果 <code>stream</code> 为 null
     * @throws IllegalArgumentException 如果 {@code stream} 包含格式错误的 Unicode 转义序列。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public PropertyResourceBundle (InputStream stream) throws IOException {
        Properties properties = new Properties();
        properties.load(stream);
        lookup = new HashMap(properties);
    }

    /**
     * 从一个 {@link java.io.Reader Reader} 创建一个属性资源包。
     * 与 {@link #PropertyResourceBundle(java.io.InputStream) PropertyResourceBundle(InputStream)} 构造函数不同，
     * 输入属性文件的编码没有限制。
     *
     * @param reader 一个表示属性文件的 Reader。
     * @throws IOException 如果发生 I/O 错误
     * @throws NullPointerException 如果 <code>reader</code> 为 null
     * @throws IllegalArgumentException 如果从 {@code reader} 中出现格式错误的 Unicode 转义序列。
     * @since 1.6
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public PropertyResourceBundle (Reader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        lookup = new HashMap(properties);
    }


                // Implements java.util.ResourceBundle.handleGetObject; inherits javadoc specification.
    public Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return lookup.get(key);
    }

    /**
     * 返回包含在此 <code>ResourceBundle</code> 及其父级包中的键的 <code>Enumeration</code>。
     *
     * @return 包含在此 <code>ResourceBundle</code> 及其父级包中的键的 <code>Enumeration</code>。
     * @see #keySet()
     */
    public Enumeration<String> getKeys() {
        ResourceBundle parent = this.parent;
        return new ResourceBundleEnumeration(lookup.keySet(),
                (parent != null) ? parent.getKeys() : null);
    }

    /**
     * 返回 <em>仅</em> 包含在此 <code>ResourceBundle</code> 中的键的 <code>Set</code>。
     *
     * @return 仅包含在此 <code>ResourceBundle</code> 中的键的 <code>Set</code>。
     * @since 1.6
     * @see #keySet()
     */
    protected Set<String> handleKeySet() {
        return lookup.keySet();
    }

    // ==================privates====================

    private Map<String,Object> lookup;
}
