
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
 *
 */

package java.util;

import sun.util.ResourceBundleEnumeration;

/**
 * <code>ListResourceBundle</code> 是 <code>ResourceBundle</code> 的一个抽象子类，
 * 它以方便且易于使用的方式管理特定区域设置的资源。有关资源包的更多信息，请参见 <code>ResourceBundle</code>。
 *
 * <P>
 * 子类必须重写 <code>getContents</code> 并提供一个数组，其中每个项目都是一个对象对。
 * 每个对的第一个元素是键，必须是 <code>String</code>，第二个元素是与该键关联的值。
 *
 * <p>
 * 以下 <a name="sample">示例</a> 显示了名为 "MyResources" 的资源包系列的两个成员。
 * "MyResources" 是该系列的默认成员，而 "MyResources_fr" 是法语成员。
 * 这些成员基于 <code>ListResourceBundle</code>
 * （一个相关的 <a href="PropertyResourceBundle.html#sample">示例</a> 展示了如何向此系列添加基于属性文件的资源包）。
 * 本示例中的键形式为 "s1" 等。实际的键完全由您选择，只要它们与您在程序中用于从资源包中检索对象的键相同即可。
 * 键是区分大小写的。
 * <blockquote>
 * <pre>
 *
 * public class MyResources extends ListResourceBundle {
 *     protected Object[][] getContents() {
 *         return new Object[][] {
 *         // LOCALIZE THIS
 *             {"s1", "The disk \"{1}\" contains {0}."},  // MessageFormat 模式
 *             {"s2", "1"},                               // 模式中 {0} 的位置
 *             {"s3", "My Disk"},                         // 示例磁盘名称
 *             {"s4", "no files"},                        // 第一个 ChoiceFormat 选项
 *             {"s5", "one file"},                        // 第二个 ChoiceFormat 选项
 *             {"s6", "{0,number} files"},                // 第三个 ChoiceFormat 选项
 *             {"s7", "3 Mar 96"},                        // 示例日期
 *             {"s8", new Dimension(1,5)}                 // 实际对象，而不仅仅是字符串
 *         // END OF MATERIAL TO LOCALIZE
 *         };
 *     }
 * }
 *
 * public class MyResources_fr extends ListResourceBundle {
 *     protected Object[][] getContents() {
 *         return new Object[][] {
 *         // LOCALIZE THIS
 *             {"s1", "Le disque \"{1}\" {0}."},          // MessageFormat 模式
 *             {"s2", "1"},                               // 模式中 {0} 的位置
 *             {"s3", "Mon disque"},                      // 示例磁盘名称
 *             {"s4", "ne contient pas de fichiers"},     // 第一个 ChoiceFormat 选项
 *             {"s5", "contient un fichier"},             // 第二个 ChoiceFormat 选项
 *             {"s6", "contient {0,number} fichiers"},    // 第三个 ChoiceFormat 选项
 *             {"s7", "3 mars 1996"},                     // 示例日期
 *             {"s8", new Dimension(1,3)}                 // 实际对象，而不仅仅是字符串
 *         // END OF MATERIAL TO LOCALIZE
 *         };
 *     }
 * }
 * </pre>
 * </blockquote>
 *
 * <p>
 * 如果一个 {@code ListResourceBundle} 子类被多个线程同时使用，其实现必须是线程安全的。
 * 本类中方法的默认实现是线程安全的。
 *
 * @see ResourceBundle
 * @see PropertyResourceBundle
 * @since JDK1.1
 */
public abstract class ListResourceBundle extends ResourceBundle {
    /**
     * 唯一的构造函数。通常由子类构造函数隐式调用。
     */
    public ListResourceBundle() {
    }

    // 实现 java.util.ResourceBundle.handleGetObject；继承 javadoc 规范。
    public final Object handleGetObject(String key) {
        // 惰性加载查找哈希表。
        if (lookup == null) {
            loadLookup();
        }
        if (key == null) {
            throw new NullPointerException();
        }
        return lookup.get(key); // 本类忽略区域设置
    }

    /**
     * 返回此 <code>ResourceBundle</code> 及其父资源包中包含的键的 <code>Enumeration</code>。
     *
     * @return 一个 <code>Enumeration</code>，包含此 <code>ResourceBundle</code> 及其父资源包中的键。
     * @see #keySet()
     */
    public Enumeration<String> getKeys() {
        // 惰性加载查找哈希表。
        if (lookup == null) {
            loadLookup();
        }


                    ResourceBundle parent = this.parent;
        return new ResourceBundleEnumeration(lookup.keySet(),
                (parent != null) ? parent.getKeys() : null);
    }

    /**
     * 返回一个 <code>Set</code>，其中包含仅在此 <code>ResourceBundle</code> 中的键。
     *
     * @return 一个 <code>Set</code>，其中包含仅在此 <code>ResourceBundle</code> 中的键
     * @since 1.6
     * @see #keySet()
     */
    protected Set<String> handleKeySet() {
        if (lookup == null) {
            loadLookup();
        }
        return lookup.keySet();
    }

    /**
     * 返回一个数组，其中每个元素是一个对象对的 <code>Object</code> 数组。每个对的第一个元素
     * 是键，必须是 <code>String</code>，第二个元素是与该键关联的值。详细信息请参见类描述。
     *
     * @return 一个表示键值对的 <code>Object</code> 数组
     */
    abstract protected Object[][] getContents();

    // ==================私有方法====================

    /**
     * 我们惰性加载 lookup 哈希表。此函数执行加载。
     */
    private synchronized void loadLookup() {
        if (lookup != null)
            return;

        Object[][] contents = getContents();
        HashMap<String,Object> temp = new HashMap<>(contents.length);
        for (int i = 0; i < contents.length; ++i) {
            // 键必须是非空字符串，值必须是非空
            String key = (String) contents[i][0];
            Object value = contents[i][1];
            if (key == null || value == null) {
                throw new NullPointerException();
            }
            temp.put(key, value);
        }
        lookup = temp;
    }

    private Map<String,Object> lookup = null;
}
