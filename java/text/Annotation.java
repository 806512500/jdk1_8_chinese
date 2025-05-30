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

package java.text;

/**
* 注释对象用于包装文本属性值，如果该属性具有注释特性。这些特性包括：
* <ul>
* <li>属性应用的文本范围对其语义至关重要。这意味着，属性不能应用于其应用的文本范围的子范围，
* 并且，如果两个相邻的文本范围具有相同的属性值，该属性仍然不能应用于整个组合范围。
* <li>如果底层文本发生变化，属性或其值通常不再适用。
* </ul>
*
* 例如，附加到句子的语法信息：
* 对于前一个句子，可以说“an example”是主语，但不能说“an”、“example”或“exam”是主语。
* 当文本发生变化时，语法信息通常变得无效。另一个例子是日语读音信息（yomi）。
*
* <p>
* 将属性值包装到注释对象中可以保证即使属性值相等，相邻的文本段也不会合并，
* 并指示文本容器如果底层文本被修改，应丢弃该属性。
*
* @see AttributedCharacterIterator
* @since 1.2
*/

public class Annotation {

    /**
     * 使用给定的值构造注释记录，该值可以为 null。
     *
     * @param value 属性的值
     */
    public Annotation(Object value) {
        this.value = value;
    }

    /**
     * 返回属性的值，该值可以为 null。
     *
     * @return 属性的值
     */
    public Object getValue() {
        return value;
    }

    /**
     * 返回此注释的字符串表示形式。
     *
     * @return 此 {@code Annotation} 的 {@code String} 表示形式
     */
    public String toString() {
        return getClass().getName() + "[value=" + value + "]";
    }

    private Object value;

};
