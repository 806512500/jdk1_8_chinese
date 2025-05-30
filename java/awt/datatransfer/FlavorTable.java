/*
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.datatransfer;

import java.util.List;


/**
 * 一个 FlavorMap，放宽了传统的一对一映射限制。允许一个 flavor 映射到任意数量的 natives，同样一个 native 也可以映射到任意数量的 flavors。FlavorTables 不必是对称的，但通常是对称的。
 *
 * @author David Mendenhall
 *
 * @since 1.4
 */
public interface FlavorTable extends FlavorMap {

    /**
     * 返回与指定 <code>DataFlavor</code> 对应的 <code>String</code> natives 列表。列表将按从最佳 native 到最差 native 排序。也就是说，第一个 native 最能反映指定 flavor 中的数据到底层的本地平台。返回的 <code>List</code> 是此 <code>FlavorTable</code> 内部数据的可修改副本。客户端代码可以自由修改 <code>List</code> 而不影响此对象。
     *
     * @param flav 要返回其对应 natives 的 <code>DataFlavor</code>。如果指定 <code>null</code>，则返回此 <code>FlavorTable</code> 当前已知的所有 natives，顺序不确定。
     * @return 一个 <code>java.util.List</code>，包含 <code>java.lang.String</code> 对象，这些对象是平台特定的数据格式的平台特定表示。
     */
    List<String> getNativesForFlavor(DataFlavor flav);

    /**
     * 返回与指定 <code>String</code> 对应的 <code>DataFlavor</code> 列表。列表将按从最佳 <code>DataFlavor</code> 到最差 <code>DataFlavor</code> 排序。也就是说，第一个 <code>DataFlavor</code> 最能反映指定 native 中的数据到 Java 应用程序。返回的 <code>List</code> 是此 <code>FlavorTable</code> 内部数据的可修改副本。客户端代码可以自由修改 <code>List</code> 而不影响此对象。
     *
     * @param nat 要返回其对应 <code>DataFlavor</code> 的 native。如果指定 <code>null</code>，则返回此 <code>FlavorTable</code> 当前已知的所有 <code>DataFlavor</code>，顺序不确定。
     * @return 一个 <code>java.util.List</code>，包含 <code>DataFlavor</code> 对象，这些对象可以将指定的平台特定 native 中的平台特定数据转换为 Java 数据。
     */
    List<DataFlavor> getFlavorsForNative(String nat);
}
