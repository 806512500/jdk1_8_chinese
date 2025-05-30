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
 * (C) Copyright Taligent, Inc. 1996 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.text;

/**
 * <code>FieldPosition</code> 是一个简单的类，用于由 <code>Format</code>
 * 及其子类识别格式化输出中的字段。字段可以通过两种方式识别：
 * <ul>
 *  <li>通过一个整数常量，其名称通常以 <code>_FIELD</code> 结尾。这些常量定义在 <code>Format</code>
 *      的各个子类中。
 *  <li>通过一个 <code>Format.Field</code> 常量，例如 <code>DateFormat</code> 中的 <code>ERA_FIELD</code>
 *      及其同类。
 * </ul>
 * <p>
 * <code>FieldPosition</code> 通过两个索引跟踪字段在格式化输出中的位置：字段的第一个字符的索引和字段的最后一个字符的索引。
 *
 * <p>
 * <code>Format</code> 类中的一个 <code>format</code> 方法版本需要一个 <code>FieldPosition</code>
 * 对象作为参数。您使用此 <code>format</code> 方法执行部分格式化或获取有关格式化输出的信息（例如字段的位置）。
 *
 * <p>
 * 如果您对格式化字符串中所有属性的位置感兴趣，请使用 <code>Format</code> 方法
 * <code>formatToCharacterIterator</code>。
 *
 * @author      Mark Davis
 * @see         java.text.Format
 */
public class FieldPosition {

    /**
     * 输入：要确定起始和结束偏移量的所需字段。
     * 含义取决于 <code>Format</code> 的子类。
     */
    int field = 0;

    /**
     * 输出：字段在文本中的结束偏移量。
     * 如果字段未出现在文本中，则返回 0。
     */
    int endIndex = 0;

    /**
     * 输出：字段在文本中的起始偏移量。
     * 如果字段未出现在文本中，则返回 0。
     */
    int beginIndex = 0;

    /**
     * 此 <code>FieldPosition</code> 对象所对应的所需字段。
     */
    private Format.Field attribute;

    /**
     * 创建一个给定字段的 <code>FieldPosition</code> 对象。字段通过常量标识，这些常量的名称通常以 _FIELD 结尾，
     * 定义在 <code>Format</code> 的各个子类中。
     *
     * @param field 字段标识符
     * @see java.text.NumberFormat#INTEGER_FIELD
     * @see java.text.NumberFormat#FRACTION_FIELD
     * @see java.text.DateFormat#YEAR_FIELD
     * @see java.text.DateFormat#MONTH_FIELD
     */
    public FieldPosition(int field) {
        this.field = field;
    }

    /**
     * 创建一个给定字段常量的 <code>FieldPosition</code> 对象。字段通过在 <code>Format</code>
     * 各个子类中定义的常量标识。这相当于调用
     * <code>new FieldPosition(attribute, -1)</code>。
     *
     * @param attribute 识别字段的 <code>Format.Field</code> 常量
     * @since 1.4
     */
    public FieldPosition(Format.Field attribute) {
        this(attribute, -1);
    }

    /**
     * 创建一个给定字段的 <code>FieldPosition</code> 对象。字段通过来自 <code>Field</code>
     * 子类之一的属性常量以及由 <code>Format</code> 子类定义的整数字段 ID 标识。<code>Format</code>
     * 子类如果知道 <code>Field</code>，应优先使用 <code>attribute</code> 并忽略 <code>fieldID</code>
     * （如果 <code>attribute</code> 不为 null）。然而，较旧的 <code>Format</code> 子类可能不知道 <code>Field</code>
     * 并依赖于 <code>fieldID</code>。如果字段没有对应的整数常量，<code>fieldID</code> 应为 -1。
     *
     * @param attribute 识别字段的 <code>Format.Field</code> 常量
     * @param fieldID 识别字段的整数常量
     * @since 1.4
     */
    public FieldPosition(Format.Field attribute, int fieldID) {
        this.attribute = attribute;
        this.field = fieldID;
    }

    /**
     * 返回一个来自 <code>Field</code> 子类之一的属性常量形式的字段标识符。如果字段仅由整数字段 ID 指定，则可能返回 null。
     *
     * @return 字段标识符
     * @since 1.4
     */
    public Format.Field getFieldAttribute() {
        return attribute;
    }

    /**
     * 检索字段标识符。
     *
     * @return 字段标识符
     */
    public int getField() {
        return field;
    }

    /**
     * 检索请求字段的第一个字符的索引。
     *
     * @return 起始索引
     */
    public int getBeginIndex() {
        return beginIndex;
    }

    /**
     * 检索请求字段的最后一个字符之后的字符的索引。
     *
     * @return 结束索引
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * 设置起始索引。供 <code>Format</code> 的子类使用。
     *
     * @param bi 起始索引
     * @since 1.2
     */
    public void setBeginIndex(int bi) {
        beginIndex = bi;
    }

    /**
     * 设置结束索引。供 <code>Format</code> 的子类使用。
     *
     * @param ei 结束索引
     * @since 1.2
     */
    public void setEndIndex(int ei) {
        endIndex = ei;
    }

    /**
     * 返回与 <code>FieldPosition</code> 关联的 <code>Format.FieldDelegate</code> 实例。当委托被通知与
     * <code>FieldPosition</code> 关联的相同字段时，起始/结束将被调整。
     */
    Format.FieldDelegate getFieldDelegate() {
        return new Delegate();
    }

    /**
     * 覆盖 equals 方法
     */
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (!(obj instanceof FieldPosition))
            return false;
        FieldPosition other = (FieldPosition) obj;
        if (attribute == null) {
            if (other.attribute != null) {
                return false;
            }
        }
        else if (!attribute.equals(other.attribute)) {
            return false;
        }
        return (beginIndex == other.beginIndex
            && endIndex == other.endIndex
            && field == other.field);
    }

    /**
     * 返回此 <code>FieldPosition</code> 的哈希码。
     * @return 此对象的哈希码值
     */
    public int hashCode() {
        return (field << 24) | (beginIndex << 16) | endIndex;
    }

    /**
     * 返回此 <code>FieldPosition</code> 的字符串表示形式。
     * @return 此对象的字符串表示形式
     */
    public String toString() {
        return getClass().getName() +
            "[field=" + field + ",attribute=" + attribute +
            ",beginIndex=" + beginIndex +
            ",endIndex=" + endIndex + ']';
    }


    /**
     * 如果接收者需要一个 <code>Format.Field</code> 值并且 <code>attribute</code> 与之相等，则返回 true。
     */
    private boolean matchesField(Format.Field attribute) {
        if (this.attribute != null) {
            return this.attribute.equals(attribute);
        }
        return false;
    }

    /**
     * 如果接收者需要一个 <code>Format.Field</code> 值并且 <code>attribute</code> 与之相等，或者
     * 如果接收者表示一个整数常量并且 <code>field</code> 与之相等，则返回 true。
     */
    private boolean matchesField(Format.Field attribute, int field) {
        if (this.attribute != null) {
            return this.attribute.equals(attribute);
        }
        return (field == this.field);
    }


    /**
     * <code>FieldDelegate</code> 的一个实现，如果参数与 <code>FieldPosition</code> 的字段匹配，
     * 将调整 <code>FieldPosition</code> 的起始/结束。
     */
    private class Delegate implements Format.FieldDelegate {
        /**
         * 指示字段是否已遇到。如果这是 true，并且调用了 <code>formatted</code>，则不会更新起始/结束。
         */
        private boolean encounteredField;

        public void formatted(Format.Field attr, Object value, int start,
                              int end, StringBuffer buffer) {
            if (!encounteredField && matchesField(attr)) {
                setBeginIndex(start);
                setEndIndex(end);
                encounteredField = (start != end);
            }
        }

        public void formatted(int fieldID, Format.Field attr, Object value,
                              int start, int end, StringBuffer buffer) {
            if (!encounteredField && matchesField(attr, fieldID)) {
                setBeginIndex(start);
                setEndIndex(end);
                encounteredField = (start != end);
            }
        }
    }
}
