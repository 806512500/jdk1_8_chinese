/*
 * 版权所有 (c) 1996, 2013，Oracle和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

/*
 * 版权所有 (C) 1996 - 保留所有权利，Taligent, Inc.
 * 版权所有 (C) 1996 - 保留所有权利，IBM Corp.
 *
 *   本源代码和文档的原始版本受版权保护并归Taligent, Inc.所有，它是IBM的全资子公司。这些
 * 材料是根据Taligent和Sun之间的许可协议提供的。这项技术受到多项美国和国际
 * 专利的保护。本通知和对Taligent的归属不得删除。
 *   Taligent 是 Taligent, Inc. 的注册商标。
 *
 */

package java.text;

/**
 * <code>FieldPosition</code> 是一个简单的类，用于 <code>Format</code>
 * 及其子类来识别格式化输出中的字段。字段可以通过两种方式识别：
 * <ul>
 *  <li>通过一个整数常量，其名称通常以 <code>_FIELD</code> 结尾。常量在 <code>Format</code>
 *      的各个子类中定义。
 *  <li>通过一个 <code>Format.Field</code> 常量，例如 <code>DateFormat</code> 中的 <code>ERA_FIELD</code>
 *      及其相关常量。
 * </ul>
 * <p>
 * <code>FieldPosition</code> 通过两个索引跟踪字段在格式化输出中的位置：字段的第一个字符的索引
 * 和字段的最后一个字符的索引。
 *
 * <p>
 * <code>Format</code> 类中的一个版本的 <code>format</code> 方法需要一个 <code>FieldPosition</code>
 * 对象作为参数。您使用此 <code>format</code> 方法执行部分格式化或获取有关格式化输出的信息
 * （例如字段的位置）。
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
     * 输入：要确定开始和结束偏移量的期望字段。
     * 含义取决于 <code>Format</code> 的子类。
     */
    int field = 0;

    /**
     * 输出：字段在文本中的结束偏移量。
     * 如果字段未出现在文本中，则返回 0。
     */
    int endIndex = 0;

    /**
     * 输出：字段在文本中的开始偏移量。
     * 如果字段未出现在文本中，则返回 0。
     */
    int beginIndex = 0;

    /**
     * 此 <code>FieldPosition</code> 对象所针对的期望字段。
     */
    private Format.Field attribute;

    /**
     * 为给定字段创建一个 <code>FieldPosition</code> 对象。字段通过常量标识，这些常量的名称通常以 _FIELD
     * 结尾，在 <code>Format</code> 的各个子类中定义。
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
     * 为给定字段常量创建一个 <code>FieldPosition</code> 对象。字段通过在 <code>Format</code>
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
     * 为给定字段创建一个 <code>FieldPosition</code> 对象。字段通过 <code>Field</code>
     * 子类之一中的属性常量以及 <code>Format</code> 子类定义的整数字段ID标识。<code>Format</code>
     * 子类如果了解 <code>Field</code>，则应优先考虑 <code>attribute</code> 并忽略 <code>fieldID</code>
     * （如果 <code>attribute</code> 不为 null）。但是，较旧的 <code>Format</code> 子类可能不了解 <code>Field</code>
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
     * 返回一个来自 <code>Field</code> 子类之一的属性常量形式的字段标识符。如果字段仅由整数字段ID指定，
     * 则可能返回 null。
     *
     * @return 字段的标识符
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
     * 检索请求字段中第一个字符的索引。
     *
     * @return 开始索引
     */
    public int getBeginIndex() {
        return beginIndex;
    }

    /**
     * 检索请求字段中最后一个字符之后的字符的索引。
     *
     * @return 结束索引
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * 设置开始索引。供 <code>Format</code> 的子类使用。
     *
     * @param bi 开始索引
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
     * 返回与 <code>FieldPosition</code> 关联的 <code>Format.FieldDelegate</code> 实例。当委托被通知
     * 与 <code>FieldPosition</code> 关联的相同字段时，开始/结束将被调整。
     */
    Format.FieldDelegate getFieldDelegate() {
        return new Delegate();
    }

}


                /**
     * 重写 equals 方法
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
     * 返回此 FieldPosition 的哈希码。
     * @return 此对象的哈希码值
     */
    public int hashCode() {
        return (field << 24) | (beginIndex << 16) | endIndex;
    }

    /**
     * 返回此 FieldPosition 的字符串表示形式。
     * @return 此对象的字符串表示形式
     */
    public String toString() {
        return getClass().getName() +
            "[field=" + field + ",attribute=" + attribute +
            ",beginIndex=" + beginIndex +
            ",endIndex=" + endIndex + ']';
    }


    /**
     * 如果接收者希望获得一个 <code>Format.Field</code> 值并且 <code>attribute</code> 与之相等，则返回 true。
     */
    private boolean matchesField(Format.Field attribute) {
        if (this.attribute != null) {
            return this.attribute.equals(attribute);
        }
        return false;
    }

    /**
     * 如果接收者希望获得一个 <code>Format.Field</code> 值并且 <code>attribute</code> 与之相等，或者如果接收者
     * 表示一个整数常量并且 <code>field</code> 与之相等，则返回 true。
     */
    private boolean matchesField(Format.Field attribute, int field) {
        if (this.attribute != null) {
            return this.attribute.equals(attribute);
        }
        return (field == this.field);
    }


    /**
     * FieldDelegate 的一个实现，如果参数与 FieldPosition 的字段匹配，将调整 FieldPosition 的开始/结束位置。
     */
    private class Delegate implements Format.FieldDelegate {
        /**
         * 表示是否已经遇到过该字段。如果这是 true，并且调用了 <code>formatted</code>，则不会更新开始/结束位置。
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
