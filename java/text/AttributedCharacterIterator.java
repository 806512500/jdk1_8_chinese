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

import java.io.InvalidObjectException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 一个 {@code AttributedCharacterIterator} 允许遍历文本及其相关的属性信息。
 *
 * <p>
 * 属性是一个键值对，由键标识。给定字符上的任何两个属性都不能具有相同的键。
 *
 * <p>属性的值是不可变的，或者不应被客户端或存储修改。它们总是按引用传递，而不是克隆。
 *
 * <p>一个 <em>关于属性的运行</em> 是一个最大文本范围，其中：
 * <ul>
 * <li>属性在整个范围内未定义或为 {@code null}，或
 * <li>属性值在整个范围内定义且具有相同的非 {@code null} 值。
 * </ul>
 *
 * <p>一个 <em>关于一组属性的运行</em> 是一个最大文本范围，其中每个成员属性都满足此条件。
 *
 * <p>当获取未显式指定属性的运行时（即，调用 {@link #getRunStart()} 和 {@link #getRunLimit()}），任何具有相同属性（相同属性/值对集）的连续文本段如果这些属性是分别赋予这些文本段的，则被视为单独的运行。
 *
 * <p>返回的索引限制在迭代器的范围内。
 *
 * <p>返回的属性信息仅限于包含当前字符的运行。
 *
 * <p>
 * 属性键是 {@link AttributedCharacterIterator.Attribute} 及其子类的实例，例如 {@link java.awt.font.TextAttribute}。
 *
 * @see AttributedCharacterIterator.Attribute
 * @see java.awt.font.TextAttribute
 * @see AttributedString
 * @see Annotation
 * @since 1.2
 */

public interface AttributedCharacterIterator extends CharacterIterator {

    /**
     * 定义用于标识文本属性的属性键。这些键在 {@code AttributedCharacterIterator} 和 {@code AttributedString} 中使用。
     * @see AttributedCharacterIterator
     * @see AttributedString
     * @since 1.2
     */

    public static class Attribute implements Serializable {

        /**
         * 此 {@code Attribute} 的名称。名称主要用于 {@code readResolve} 在反序列化实例时查找相应的预定义实例。
         * @serial
         */
        private String name;

        // 用于 readResolve 的所有此类实例的表
        private static final Map<String, Attribute> instanceMap = new HashMap<>(7);

        /**
         * 使用给定名称构造一个 {@code Attribute}。
         *
         * @param name {@code Attribute} 的名称
         */
        protected Attribute(String name) {
            this.name = name;
            if (this.getClass() == Attribute.class) {
                instanceMap.put(name, this);
            }
        }

        /**
         * 比较两个对象是否相等。此版本仅在 {@code x} 和 {@code y} 引用同一个对象时返回 true，并保证对所有子类都有效。
         */
        public final boolean equals(Object obj) {
            return super.equals(obj);
        }

        /**
         * 返回对象的哈希码值。此版本与 {@code Object} 中的版本相同，但也是 final 的。
         */
        public final int hashCode() {
            return super.hashCode();
        }

        /**
         * 返回对象的字符串表示形式。此版本返回类名、{@code "("}、标识属性的名称和 {@code ")"} 的连接。
         */
        public String toString() {
            return getClass().getName() + "(" + name + ")";
        }

        /**
         * 返回属性的名称。
         *
         * @return {@code Attribute} 的名称
         */
        protected String getName() {
            return name;
        }

        /**
         * 将正在反序列化的实例解析为预定义的常量。
         *
         * @return 解析后的 {@code Attribute} 对象
         * @throws InvalidObjectException 如果要解析的对象不是 {@code Attribute} 的实例
         */
        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != Attribute.class) {
                throw new InvalidObjectException("子类没有正确实现 readResolve");
            }

            Attribute instance = instanceMap.get(getName());
            if (instance != null) {
                return instance;
            } else {
                throw new InvalidObjectException("未知的属性名称");
            }
        }

        /**
         * 用于标识某些文本语言的属性键。
         * <p> 值是 {@link java.util.Locale Locale} 的实例。
         * @see java.util.Locale
         */
        public static final Attribute LANGUAGE = new Attribute("language");

        /**
         * 用于标识某些文本读音的属性键。在书写形式和发音关系不紧密的语言（如日语）中，通常需要存储读音（发音）和书写形式。
         * <p>值是持有 {@link String} 实例的 {@link Annotation}。
         *
         * @see Annotation
         * @see java.lang.String
         */
        public static final Attribute READING = new Attribute("reading");

        /**
         * 用于输入方法段的属性键。输入方法通常将文本分解为段，这些段通常对应于单词。
         * <p>值是持有 {@code null} 引用的 {@link Annotation}。
         * @see Annotation
         */
        public static final Attribute INPUT_METHOD_SEGMENT = new Attribute("input_method_segment");

        // 确保序列化版本在编译器版本之间不发生变化
        private static final long serialVersionUID = -9142742483513960612L;

    };

    /**
     * 返回包含当前字符的关于所有属性的运行的第一个字符的索引。
     *
     * <p>任何具有相同属性（相同属性/值对集）的连续文本段如果这些属性是分别赋予这些文本段的，则被视为单独的运行。
     *
     * @return 运行的第一个字符的索引
     */
    public int getRunStart();

    /**
     * 返回包含当前字符的关于给定 {@code attribute} 的运行的第一个字符的索引。
     *
     * @param attribute 所需的属性。
     * @return 运行的第一个字符的索引
     */
    public int getRunStart(Attribute attribute);

    /**
     * 返回包含当前字符的关于给定 {@code attributes} 的运行的第一个字符的索引。
     *
     * @param attributes 一组所需的属性。
     * @return 运行的第一个字符的索引
     */
    public int getRunStart(Set<? extends Attribute> attributes);

    /**
     * 返回包含当前字符的关于所有属性的运行的第一个字符之后的索引。
     *
     * <p>任何具有相同属性（相同属性/值对集）的连续文本段如果这些属性是分别赋予这些文本段的，则被视为单独的运行。
     *
     * @return 运行的第一个字符之后的索引
     */
    public int getRunLimit();

    /**
     * 返回包含当前字符的关于给定 {@code attribute} 的运行的第一个字符之后的索引。
     *
     * @param attribute 所需的属性
     * @return 运行的第一个字符之后的索引
     */
    public int getRunLimit(Attribute attribute);

    /**
     * 返回包含当前字符的关于给定 {@code attributes} 的运行的第一个字符之后的索引。
     *
     * @param attributes 一组所需的属性
     * @return 运行的第一个字符之后的索引
     */
    public int getRunLimit(Set<? extends Attribute> attributes);

    /**
     * 返回当前字符上定义的属性的映射。
     *
     * @return 当前字符上定义的属性的映射
     */
    public Map<Attribute,Object> getAttributes();

    /**
     * 返回当前字符的命名 {@code attribute} 的值。如果 {@code attribute} 未定义，则返回 {@code null}。
     *
     * @param attribute 所需的属性
     * @return 命名的 {@code attribute} 的值或 {@code null}
     */
    public Object getAttribute(Attribute attribute);

    /**
     * 返回在迭代器的文本范围内定义的所有属性的键。如果没有定义任何属性，则该集合为空。
     *
     * @return 所有属性的键
     */
    public Set<Attribute> getAllAttributeKeys();
};
