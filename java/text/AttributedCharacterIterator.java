
/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.text;

import java.io.InvalidObjectException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@code AttributedCharacterIterator} 允许迭代文本及其相关属性信息。
 *
 * <p>
 * 属性是由键/值对标识的，通过键来识别。给定字符上的两个属性不能具有相同的键。
 *
 * <p>属性的值是不可变的，或者客户端或存储不应修改。它们总是通过引用传递，而不是克隆。
 *
 * <p><em>关于属性的运行</em> 是一个最大文本范围，其中：
 * <ul>
 * <li>属性在整个范围内未定义或为 {@code null}，或
 * <li>属性值在整个范围内定义且具有相同的非-{@code null} 值。
 * </ul>
 *
 * <p><em>关于一组属性的运行</em> 是一个最大文本范围，其中每个成员属性都满足此条件。
 *
 * <p>当获取未显式指定属性的运行时（即，调用 {@link #getRunStart()} 和 {@link #getRunLimit()}），如果属性已分别赋予这些文本段，则具有相同属性（相同的属性/值对集）的任何连续文本段被视为单独的运行。
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

        // 用于 readResolve 的此类中所有实例的表
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
         * 比较两个对象是否相等。此版本仅在 {@code x} 和 {@code y} 引用同一个对象时返回 true，并保证对所有子类都如此。
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
                throw new InvalidObjectException("子类未正确实现 readResolve");
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
         * <p>值是 {@link java.util.Locale Locale} 的实例。
         * @see java.util.Locale
         */
        public static final Attribute LANGUAGE = new Attribute("language");

        /**
         * 用于标识某些文本读音的属性键。在书写形式和发音之间关系松散的语言（如日语）中，通常需要存储读音（发音）以及书写形式。
         * <p>值是持有 {@link String} 实例的 {@link Annotation} 实例。
         *
         * @see Annotation
         * @see java.lang.String
         */
        public static final Attribute READING = new Attribute("reading");

        /**
         * 用于输入方法段的属性键。输入方法通常将文本分解成段，这些段通常对应于单词。
         * <p>值是持有 {@code null} 引用的 {@link Annotation} 实例。
         * @see Annotation
         */
        public static final Attribute INPUT_METHOD_SEGMENT = new Attribute("input_method_segment");

                    // 确保序列化版本在编译器版本之间不发生变化
        private static final long serialVersionUID = -9142742483513960612L;

    };

    /**
     * 返回包含当前字符的所有属性的运行的第一个字符的索引。
     *
     * <p>任何具有相同属性（相同的属性/值对）的连续文本段，如果这些属性是分别赋予这些文本段的，则被视为单独的运行。
     *
     * @return 运行的第一个字符的索引
     */
    public int getRunStart();

    /**
     * 返回给定 {@code attribute} 包含当前字符的运行的第一个字符的索引。
     *
     * @param attribute 所需的属性。
     * @return 运行的第一个字符的索引
     */
    public int getRunStart(Attribute attribute);

    /**
     * 返回给定 {@code attributes} 包含当前字符的运行的第一个字符的索引。
     *
     * @param attributes 一组所需的属性。
     * @return 运行的第一个字符的索引
     */
    public int getRunStart(Set<? extends Attribute> attributes);

    /**
     * 返回包含当前字符的所有属性的运行后第一个字符的索引。
     *
     * <p>任何具有相同属性（相同的属性/值对）的连续文本段，如果这些属性是分别赋予这些文本段的，则被视为单独的运行。
     *
     * @return 运行后第一个字符的索引
     */
    public int getRunLimit();

    /**
     * 返回给定 {@code attribute} 包含当前字符的运行后第一个字符的索引。
     *
     * @param attribute 所需的属性
     * @return 运行后第一个字符的索引
     */
    public int getRunLimit(Attribute attribute);

    /**
     * 返回给定 {@code attributes} 包含当前字符的运行后第一个字符的索引。
     *
     * @param attributes 一组所需的属性
     * @return 运行后第一个字符的索引
     */
    public int getRunLimit(Set<? extends Attribute> attributes);

    /**
     * 返回定义在当前字符上的属性的映射。
     *
     * @return 定义在当前字符上的属性的映射
     */
    public Map<Attribute,Object> getAttributes();

    /**
     * 返回当前字符的命名 {@code attribute} 的值。如果 {@code attribute} 未定义，则返回 {@code null}。
     *
     * @param attribute 所需的属性
     * @return 命名 {@code attribute} 的值或 {@code null}
     */
    public Object getAttribute(Attribute attribute);

    /**
     * 返回迭代器文本范围上定义的所有属性的键。如果没有定义任何属性，则该集合为空。
     *
     * @return 所有属性的键
     */
    public Set<Attribute> getAllAttributeKeys();
};
