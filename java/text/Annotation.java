/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.text;

/**
 * 一个 Annotation 对象用于包装文本属性值，如果该属性具有注解特性。这些特性包括：
 * <ul>
 * <li>属性应用的文本范围对其语义至关重要。这意味着，该属性不能应用于其适用的文本范围的子范围，
 * 并且，如果两个相邻的文本范围具有此属性的相同值，该属性仍然不能应用于整个组合范围。
 * <li>如果底层文本发生变化，该属性或其值通常不再适用。
 * </ul>
 *
 * 例如，附加到句子的语法信息：
 * 对于前一句，可以说 "an example" 是主语，但不能说 "an"、"example" 或 "exam" 也是主语。
 * 当文本发生变化时，语法信息通常会变得无效。另一个例子是日语读音信息（yomi）。
 *
 * <p>
 * 将属性值包装到 Annotation 对象中可以保证即使属性值相同，相邻的文本段也不会被合并，
 * 并指示文本容器如果底层文本被修改，应丢弃该属性。
 *
 * @see AttributedCharacterIterator
 * @since 1.2
 */

public class Annotation {

    /**
     * 使用给定的值构造一个注解记录，该值可以为 null。
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
     * 返回此 Annotation 的字符串表示形式。
     *
     * @return 此 {@code Annotation} 的 {@code String} 表示形式
     */
    public String toString() {
        return getClass().getName() + "[value=" + value + "]";
    }

    private Object value;

};
