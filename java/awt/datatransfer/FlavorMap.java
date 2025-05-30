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

package java.awt.datatransfer;

import java.util.Map;


/**
 * 一个双向映射，用于“本地”（字符串），这些字符串对应于平台特定的数据格式，和“风味”（DataFlavors），这些对应于平台无关的MIME类型。FlavorMaps不必是对称的，但通常是。
 *
 *
 * @since 1.2
 */
public interface FlavorMap {

    /**
     * 返回一个指定的<code>DataFlavor</code>到其对应的<code>String</code>本地的<code>Map</code>。返回的<code>Map</code>是此<code>FlavorMap</code>内部数据的可修改副本。客户端代码可以自由修改<code>Map</code>，而不会影响此对象。
     *
     * @param flavors 一个<code>DataFlavor</code>数组，将成为返回的<code>Map</code>的键集。如果指定<code>null</code>，将返回此<code>FlavorMap</code>当前已知的所有<code>DataFlavor</code>到其对应的<code>String</code>本地的映射。
     * @return 一个<code>java.util.Map</code>，将<code>DataFlavor</code>映射到<code>String</code>本地
     */
    Map<DataFlavor,String> getNativesForFlavors(DataFlavor[] flavors);

    /**
     * 返回一个指定的<code>String</code>本地到其对应的<code>DataFlavor</code>的<code>Map</code>。返回的<code>Map</code>是此<code>FlavorMap</code>内部数据的可修改副本。客户端代码可以自由修改<code>Map</code>，而不会影响此对象。
     *
     * @param natives 一个<code>String</code>数组，将成为返回的<code>Map</code>的键集。如果指定<code>null</code>，将返回此<code>FlavorMap</code>当前已知的所有<code>String</code>本地到其对应的<code>DataFlavor</code>的映射。
     * @return 一个<code>java.util.Map</code>，将<code>String</code>本地映射到<code>DataFlavor</code>
     */
    Map<String,DataFlavor> getFlavorsForNatives(String[] natives);
}
