/*
 * 版权所有 (c) 1996, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * 版权所有 (C) 1996, 1997 - Taligent, Inc. 保留所有权利
 * 版权所有 (C) 1996 - 1998 - IBM Corp. 保留所有权利
 *
 * 本源代码和文档的原始版本
 * 版权所有并由 Taligent, Inc. 拥有，它是 IBM 的全资子公司。这些材料是根据 Taligent 和 Sun 之间的许可协议提供的。这项技术受到多项美国和国际专利的保护。
 *
 * 本通知和对 Taligent 的归属不得移除。Taligent 是 Taligent, Inc. 的注册商标。
 *
 */

package java.text;


/**
 * 此接口定义了文本双向迭代的协议。
 * 迭代器迭代一个有界字符序列。字符
 * 从 getBeginIndex() 返回的值开始索引，并
 * 继续到 getEndIndex() 返回的值 -1。
 * <p>
 * 迭代器维护一个当前字符索引，其有效范围是从
 * getBeginIndex() 到 getEndIndex()；包括 getEndIndex() 是为了处理零长度文本范围和历史原因。
 * 可以通过调用 getIndex() 检索当前索引，并通过调用 setIndex()、first() 和 last() 直接设置。
 * <p>
 * 方法 previous() 和 next() 用于迭代。如果它们会移动到 getBeginIndex() 到 getEndIndex() -1 范围之外，
 * 则返回 DONE，表示迭代器已到达序列的末尾。DONE 也由其他方法返回，以指示当前索引
 * 在此范围之外。
 *
 * <P>示例：<P>
 *
 * 从开始到结束遍历文本
 * <pre>{@code
 * public void traverseForward(CharacterIterator iter) {
 *     for(char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
 *         processChar(c);
 *     }
 * }
 * }</pre>
 *
 * 从结束到开始反向遍历文本
 * <pre>{@code
 * public void traverseBackward(CharacterIterator iter) {
 *     for(char c = iter.last(); c != CharacterIterator.DONE; c = iter.previous()) {
 *         processChar(c);
 *     }
 * }
 * }</pre>
 *
 * 从文本中的给定位置向前和向后遍历。
 * 本示例中的 notBoundary() 调用表示一些
 * 额外的停止条件。
 * <pre>{@code
 * public void traverseOut(CharacterIterator iter, int pos) {
 *     for (char c = iter.setIndex(pos);
 *              c != CharacterIterator.DONE && notBoundary(c);
 *              c = iter.next()) {
 *     }
 *     int end = iter.getIndex();
 *     for (char c = iter.setIndex(pos);
 *             c != CharacterIterator.DONE && notBoundary(c);
 *             c = iter.previous()) {
 *     }
 *     int start = iter.getIndex();
 *     processSection(start, end);
 * }
 * }</pre>
 *
 * @see StringCharacterIterator
 * @see AttributedCharacterIterator
 */

public interface CharacterIterator extends Cloneable
{

    /**
     * 当迭代器到达文本的开始或结束时返回的常量。值为 '\\uFFFF'，即“不是字符”的值，不应出现在任何有效的 Unicode 字符串中。
     */
    public static final char DONE = '\uFFFF';

    /**
     * 将位置设置为 getBeginIndex() 并返回该位置的字符。
     * @return 文本中的第一个字符，如果文本为空则返回 DONE
     * @see #getBeginIndex()
     */
    public char first();

    /**
     * 将位置设置为 getEndIndex()-1（如果文本为空则为 getEndIndex()）并返回该位置的字符。
     * @return 文本中的最后一个字符，如果文本为空则返回 DONE
     * @see #getEndIndex()
     */
    public char last();

    /**
     * 获取当前位置（由 getIndex() 返回）的字符。
     * @return 当前位置的字符，如果当前位置超出文本末尾则返回 DONE
     * @see #getIndex()
     */
    public char current();

    /**
     * 将迭代器的索引增加一并返回新索引处的字符。如果结果索引大于或等于
     * getEndIndex()，则当前索引重置为 getEndIndex() 并返回 DONE 的值。
     * @return 新位置的字符，如果新位置超出文本范围则返回 DONE
     */
    public char next();

    /**
     * 将迭代器的索引减少一并返回新索引处的字符。如果当前索引为 getBeginIndex()，则索引
     * 保持在 getBeginIndex() 并返回 DONE 的值。
     * @return 新位置的字符，如果当前位置等于 getBeginIndex() 则返回 DONE
     */
    public char previous();

    /**
     * 将位置设置为文本中的指定位置并返回该字符。
     * @param position 文本中的位置。有效值范围从
     * getBeginIndex() 到 getEndIndex()。如果提供无效值，则抛出 IllegalArgumentException。
     * @return 指定位置的字符，如果指定位置等于 getEndIndex() 则返回 DONE
     */
    public char setIndex(int position);

    /**
     * 返回文本的开始索引。
     * @return 文本开始的索引。
     */
    public int getBeginIndex();

    /**
     * 返回文本的结束索引。此索引是文本末尾后第一个
     * 字符的索引。
     * @return 文本最后一个字符后的索引
     */
    public int getEndIndex();

    /**
     * 返回当前索引。
     * @return 当前索引。
     */
    public int getIndex();

    /**
     * 创建此迭代器的副本
     * @return 本对象的副本
     */
    public Object clone();

}
