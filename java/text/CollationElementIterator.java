
/*
 * Copyright (c) 1996, 2020, Oracle and/or its affiliates. All rights reserved.
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

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996-1998 - All Rights Reserved
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

import java.lang.Character;
import java.util.Vector;
import sun.text.CollatorUtilities;
import sun.text.normalizer.NormalizerBase;

/**
 * The <code>CollationElementIterator</code> class is used as an iterator
 * to walk through each character of an international string. Use the iterator
 * to return the ordering priority of the positioned character. The ordering
 * priority of a character, which we refer to as a key, defines how a character
 * is collated in the given collation object.
 *
 * <p>
 * For example, consider the following in Spanish:
 * <blockquote>
 * <pre>
 * "ca" &rarr; the first key is key('c') and second key is key('a').
 * "cha" &rarr; the first key is key('ch') and second key is key('a').
 * </pre>
 * </blockquote>
 * And in German,
 * <blockquote>
 * <pre>
 * "\u00e4b" &rarr; the first key is key('a'), the second key is key('e'), and
 * the third key is key('b').
 * </pre>
 * </blockquote>
 * The key of a character is an integer composed of primary order(short),
 * secondary order(byte), and tertiary order(byte). Java strictly defines
 * the size and signedness of its primitive data types. Therefore, the static
 * functions <code>primaryOrder</code>, <code>secondaryOrder</code>, and
 * <code>tertiaryOrder</code> return <code>int</code>, <code>short</code>,
 * and <code>short</code> respectively to ensure the correctness of the key
 * value.
 *
 * <p>
 * Example of the iterator usage,
 * <blockquote>
 * <pre>
 *
 *  String testString = "This is a test";
 *  Collator col = Collator.getInstance();
 *  if (col instanceof RuleBasedCollator) {
 *      RuleBasedCollator ruleBasedCollator = (RuleBasedCollator)col;
 *      CollationElementIterator collationElementIterator = ruleBasedCollator.getCollationElementIterator(testString);
 *      int primaryOrder = CollationElementIterator.primaryOrder(collationElementIterator.next());
 *          :
 *  }
 * </pre>
 * </blockquote>
 *
 * <p>
 * <code>CollationElementIterator.next</code> returns the collation order
 * of the next character. A collation order consists of primary order,
 * secondary order and tertiary order. The data type of the collation
 * order is <strong>int</strong>. The first 16 bits of a collation order
 * is its primary order; the next 8 bits is the secondary order and the
 * last 8 bits is the tertiary order.
 *
 * <p><b>Note:</b> <code>CollationElementIterator</code> is a part of
 * <code>RuleBasedCollator</code> implementation. It is only usable
 * with <code>RuleBasedCollator</code> instances.
 *
 * @see                Collator
 * @see                RuleBasedCollator
 * @author             Helena Shih, Laura Werner, Richard Gillam
 */
public final class CollationElementIterator
{
    /**
     * Null order which indicates the end of string is reached by the
     * cursor.
     */
    public final static int NULLORDER = 0xffffffff;

    /**
     * CollationElementIterator constructor.  This takes the source string and
     * the collation object.  The cursor will walk thru the source string based
     * on the predefined collation rules.  If the source string is empty,
     * NULLORDER will be returned on the calls to next().
     * @param sourceText the source string.
     * @param owner the collation object.
     */
    CollationElementIterator(String sourceText, RuleBasedCollator owner) {
        this.owner = owner;
        ordering = owner.getTables();
        if (!sourceText.isEmpty()) {
            NormalizerBase.Mode mode =
                CollatorUtilities.toNormalizerMode(owner.getDecomposition());
            text = new NormalizerBase(sourceText, mode);
        }
    }

    /**
     * CollationElementIterator constructor.  This takes the source string and
     * the collation object.  The cursor will walk thru the source string based
     * on the predefined collation rules.  If the source string is empty,
     * NULLORDER will be returned on the calls to next().
     * @param sourceText the source string.
     * @param owner the collation object.
     */
    CollationElementIterator(CharacterIterator sourceText, RuleBasedCollator owner) {
        this.owner = owner;
        ordering = owner.getTables();
        NormalizerBase.Mode mode =
            CollatorUtilities.toNormalizerMode(owner.getDecomposition());
        text = new NormalizerBase(sourceText, mode);
    }

    /**
     * Resets the cursor to the beginning of the string.  The next call
     * to next() will return the first collation element in the string.
     */
    public void reset()
    {
        if (text != null) {
            text.reset();
            NormalizerBase.Mode mode =
                CollatorUtilities.toNormalizerMode(owner.getDecomposition());
            text.setMode(mode);
        }
        buffer = null;
        expIndex = 0;
        swapOrder = 0;
    }

    /**
     * Get the next collation element in the string.  <p>This iterator iterates
     * over a sequence of collation elements that were built from the string.
     * Because there isn't necessarily a one-to-one mapping from characters to
     * collation elements, this doesn't mean the same thing as "return the
     * collation element [or ordering priority] of the next character in the
     * string".</p>
     * <p>This function returns the collation element that the iterator is currently
     * pointing to and then updates the internal pointer to point to the next element.
     * previous() updates the pointer first and then returns the element.  This
     * means that when you change direction while iterating (i.e., call next() and
     * then call previous(), or call previous() and then call next()), you'll
     * get back the same element twice.</p>
     *
     * @return the next collation element
     */
    public int next()
    {
        if (text == null) {
            return NULLORDER;
        }
        NormalizerBase.Mode textMode = text.getMode();
        // convert the owner's mode to something the Normalizer understands
        NormalizerBase.Mode ownerMode =
            CollatorUtilities.toNormalizerMode(owner.getDecomposition());
        if (textMode != ownerMode) {
            text.setMode(ownerMode);
        }


                    // 如果缓冲区包含任何分解的字符值
        // 在继续使用 Normalizer 的 CharacterIterator 之前返回它们的强度顺序。
        if (buffer != null) {
            if (expIndex < buffer.length) {
                return strengthOrder(buffer[expIndex++]);
            } else {
                buffer = null;
                expIndex = 0;
            }
        } else if (swapOrder != 0) {
            if (Character.isSupplementaryCodePoint(swapOrder)) {
                char[] chars = Character.toChars(swapOrder);
                swapOrder = chars[1];
                return chars[0] << 16;
            }
            int order = swapOrder << 16;
            swapOrder = 0;
            return order;
        }
        int ch  = text.next();

        // 我们是否已经到达 Normalizer 的文本末尾？
        if (ch == NormalizerBase.DONE) {
            return NULLORDER;
        }

        int value = ordering.getUnicodeOrder(ch);
        if (value == RuleBasedCollator.UNMAPPED) {
            swapOrder = ch;
            return UNMAPPEDCHARVALUE;
        }
        else if (value >= RuleBasedCollator.CONTRACTCHARINDEX) {
            value = nextContractChar(ch);
        }
        if (value >= RuleBasedCollator.EXPANDCHARINDEX) {
            buffer = ordering.getExpandValueList(value);
            expIndex = 0;
            value = buffer[expIndex++];
        }

        if (ordering.isSEAsianSwapping()) {
            int consonant;
            if (isThaiPreVowel(ch)) {
                consonant = text.next();
                if (isThaiBaseConsonant(consonant)) {
                    buffer = makeReorderedBuffer(consonant, value, buffer, true);
                    value = buffer[0];
                    expIndex = 1;
                } else if (consonant != NormalizerBase.DONE) {
                    text.previous();
                }
            }
            if (isLaoPreVowel(ch)) {
                consonant = text.next();
                if (isLaoBaseConsonant(consonant)) {
                    buffer = makeReorderedBuffer(consonant, value, buffer, true);
                    value = buffer[0];
                    expIndex = 1;
                } else if (consonant != NormalizerBase.DONE) {
                    text.previous();
                }
            }
        }

        return strengthOrder(value);
    }

    /**
     * 获取字符串中的前一个排序元素。 <p>此迭代器遍历从字符串构建的排序元素序列。
     * 由于字符与排序元素之间不一定是一对一的映射，这并不意味着“返回字符串中前一个字符的排序元素[或排序优先级]”。</p>
     * <p>此函数将迭代器的内部指针更新为指向当前指向的排序元素之前的元素，然后返回该元素，而 next() 返回当前元素并更新指针。
     * 这意味着在迭代方向改变时（即调用 next() 然后调用 previous()，或调用 previous() 然后调用 next()），你会两次获得相同的元素。</p>
     *
     * @return 前一个排序元素
     * @since 1.2
     */
    public int previous()
    {
        if (text == null) {
            return NULLORDER;
        }
        NormalizerBase.Mode textMode = text.getMode();
        // 将所有者的模式转换为 Normalizer 可理解的模式
        NormalizerBase.Mode ownerMode =
            CollatorUtilities.toNormalizerMode(owner.getDecomposition());
        if (textMode != ownerMode) {
            text.setMode(ownerMode);
        }
        if (buffer != null) {
            if (expIndex > 0) {
                return strengthOrder(buffer[--expIndex]);
            } else {
                buffer = null;
                expIndex = 0;
            }
        } else if (swapOrder != 0) {
            if (Character.isSupplementaryCodePoint(swapOrder)) {
                char[] chars = Character.toChars(swapOrder);
                swapOrder = chars[1];
                return chars[0] << 16;
            }
            int order = swapOrder << 16;
            swapOrder = 0;
            return order;
        }
        int ch = text.previous();
        if (ch == NormalizerBase.DONE) {
            return NULLORDER;
        }

        int value = ordering.getUnicodeOrder(ch);

        if (value == RuleBasedCollator.UNMAPPED) {
            swapOrder = UNMAPPEDCHARVALUE;
            return ch;
        } else if (value >= RuleBasedCollator.CONTRACTCHARINDEX) {
            value = prevContractChar(ch);
        }
        if (value >= RuleBasedCollator.EXPANDCHARINDEX) {
            buffer = ordering.getExpandValueList(value);
            expIndex = buffer.length;
            value = buffer[--expIndex];
        }

        if (ordering.isSEAsianSwapping()) {
            int vowel;
            if (isThaiBaseConsonant(ch)) {
                vowel = text.previous();
                if (isThaiPreVowel(vowel)) {
                    buffer = makeReorderedBuffer(vowel, value, buffer, false);
                    expIndex = buffer.length - 1;
                    value = buffer[expIndex];
                } else {
                    text.next();
                }
            }
            if (isLaoBaseConsonant(ch)) {
                vowel = text.previous();
                if (isLaoPreVowel(vowel)) {
                    buffer = makeReorderedBuffer(vowel, value, buffer, false);
                    expIndex = buffer.length - 1;
                    value = buffer[expIndex];
                } else {
                    text.next();
                }
            }
        }

        return strengthOrder(value);
    }

    /**
     * 返回排序元素的主要组件。
     * @param order 排序元素
     * @return 元素的主要组件
     */
    public final static int primaryOrder(int order)
    {
        order &= RBCollationTables.PRIMARYORDERMASK;
        return (order >>> RBCollationTables.PRIMARYORDERSHIFT);
    }
    /**
     * 返回排序元素的次要组件。
     * @param order 排序元素
     * @return 元素的次要组件
     */
    public final static short secondaryOrder(int order)
    {
        order = order & RBCollationTables.SECONDARYORDERMASK;
        return ((short)(order >> RBCollationTables.SECONDARYORDERSHIFT));
    }
    /**
     * 返回排序元素的三级组件。
     * @param order 排序元素
     * @return 元素的三级组件
     */
    public final static short tertiaryOrder(int order)
    {
        return ((short)(order &= RBCollationTables.TERTIARYORDERMASK));
    }


                /**
     * 获取所需强度的比较顺序。忽略其他差异。
     * @param order 顺序值
     */
    final int strengthOrder(int order)
    {
        int s = owner.getStrength();
        if (s == Collator.PRIMARY)
        {
            order &= RBCollationTables.PRIMARYDIFFERENCEONLY;
        } else if (s == Collator.SECONDARY)
        {
            order &= RBCollationTables.SECONDARYDIFFERENCEONLY;
        }
        return order;
    }

    /**
     * 设置迭代器指向与指定字符对应的排序元素（参数是原始字符串中的字符偏移量，而不是其对应排序元素序列中的偏移量）。下一次调用 next() 方法返回的值将是文本中指定位置对应的排序元素。如果该位置位于收缩字符序列的中间，那么下一次调用 next() 的结果将是该序列的排序元素。这意味着 getOffset() 不保证返回与先前调用 setOffset() 时传递的值相同。
     *
     * @param newOffset 原始文本中的新字符偏移量。
     * @since 1.2
     */
    @SuppressWarnings("deprecation") // getBeginIndex, getEndIndex and setIndex are deprecated
    public void setOffset(int newOffset)
    {
        if (text != null) {
            if (newOffset < text.getBeginIndex()
                || newOffset >= text.getEndIndex()) {
                    text.setIndexOnly(newOffset);
            } else {
                int c = text.setIndex(newOffset);

                // 如果所需的字符不用于收缩字符序列，则绕过所有回退逻辑——我们已经坐在正确的字符上了
                if (ordering.usedInContractSeq(c)) {
                    // 通过字符串向后遍历，直到看到一个不参与收缩字符序列的字符
                    while (ordering.usedInContractSeq(c)) {
                        c = text.previous();
                    }
                    // 现在使用此对象的 next() 方法向前遍历，直到我们超过起始点并将当前位置设置为起始位置之前或处的最后一个“字符”的开头
                    int last = text.getIndex();
                    while (text.getIndex() <= newOffset) {
                        last = text.getIndex();
                        next();
                    }
                    text.setIndexOnly(last);
                    // 我们不需要这个，因为 last 是最后一个索引
                    // 这个索引是包含 newOffset 的收缩的开始
                    // text.previous();
                }
            }
        }
        buffer = null;
        expIndex = 0;
        swapOrder = 0;
    }

    /**
     * 返回下一个排序元素在原始文本中的字符偏移量。（也就是说，getOffset() 返回的值是与下一次调用 next() 返回的排序元素对应的文本中的位置。）这个值总是对应于排序元素的第一个字符的索引（当两个或多个字符都对应于同一个排序元素时，这是一个收缩字符序列）。这意味着如果你执行 setOffset(x) 然后立即调用 getOffset()，getOffset() 不一定会返回 x。
     *
     * @return 下一次调用 next() 返回的排序元素在原始文本中的字符偏移量。
     * @since 1.2
     */
    public int getOffset()
    {
        return (text != null) ? text.getIndex() : 0;
    }


    /**
     * 返回以指定比较顺序结束的任何扩展序列的最大长度。
     * @param order 由 previous 或 next 返回的排序顺序。
     * @return 以指定顺序结束的任何扩展序列的最大长度。
     * @since 1.2
     */
    public int getMaxExpansion(int order)
    {
        return ordering.getMaxExpansion(order);
    }

    /**
     * 设置新的迭代字符串。
     *
     * @param source 新的源文本
     * @since 1.2
     */
    public void setText(String source)
    {
        buffer = null;
        swapOrder = 0;
        expIndex = 0;
        NormalizerBase.Mode mode =
            CollatorUtilities.toNormalizerMode(owner.getDecomposition());
        if (text == null) {
            text = new NormalizerBase(source, mode);
        } else {
            text.setMode(mode);
            text.setText(source);
        }
    }

    /**
     * 设置新的迭代字符串。
     *
     * @param source 新的源文本。
     * @since 1.2
     */
    public void setText(CharacterIterator source)
    {
        buffer = null;
        swapOrder = 0;
        expIndex = 0;
        NormalizerBase.Mode mode =
            CollatorUtilities.toNormalizerMode(owner.getDecomposition());
        if (text == null) {
            text = new NormalizerBase(source, mode);
        } else {
            text.setMode(mode);
            text.setText(source);
        }
    }

    //============================================================
    // privates
    //============================================================

    /**
     * 确定字符是否为泰语元音（排序在其基本辅音之后）。
     */
    private final static boolean isThaiPreVowel(int ch) {
        return (ch >= 0x0e40) && (ch <= 0x0e44);
    }

    /**
     * 确定字符是否为泰语基本辅音
     */
    private final static boolean isThaiBaseConsonant(int ch) {
        return (ch >= 0x0e01) && (ch <= 0x0e2e);
    }

    /**
     * 确定字符是否为老挝语元音（排序在其基本辅音之后）。
     */
    private final static boolean isLaoPreVowel(int ch) {
        return (ch >= 0x0ec0) && (ch <= 0x0ec4);
    }


                /**
     * 确定一个字符是否是老挝基础辅音
     */
    private final static boolean isLaoBaseConsonant(int ch) {
        return (ch >= 0x0e81) && (ch <= 0x0eae);
    }

    /**
     * 此方法生成一个包含两个字符的排序元素的缓冲区，其中 colFirst 的值在另一个字符之前。
     * 假设另一个字符在逻辑顺序中位于 colFirst 之前（否则你就不需要这个方法了，对吧？）。
     * 假设另一个字符的值已经计算完毕。如果此字符只有一个元素，则作为 lastValue 传递给此方法，而 lastExpansion 为 null。
     * 如果它有扩展，则在 lastExpansion 中传递，colLastValue 被忽略。
     */
    private int[] makeReorderedBuffer(int colFirst,
                                      int lastValue,
                                      int[] lastExpansion,
                                      boolean forward) {

        int[] result;

        int firstValue = ordering.getUnicodeOrder(colFirst);
        if (firstValue >= RuleBasedCollator.CONTRACTCHARINDEX) {
            firstValue = forward? nextContractChar(colFirst) : prevContractChar(colFirst);
        }

        int[] firstExpansion = null;
        if (firstValue >= RuleBasedCollator.EXPANDCHARINDEX) {
            firstExpansion = ordering.getExpandValueList(firstValue);
        }

        if (!forward) {
            int temp1 = firstValue;
            firstValue = lastValue;
            lastValue = temp1;
            int[] temp2 = firstExpansion;
            firstExpansion = lastExpansion;
            lastExpansion = temp2;
        }

        if (firstExpansion == null && lastExpansion == null) {
            result = new int [2];
            result[0] = firstValue;
            result[1] = lastValue;
        }
        else {
            int firstLength = firstExpansion==null? 1 : firstExpansion.length;
            int lastLength = lastExpansion==null? 1 : lastExpansion.length;
            result = new int[firstLength + lastLength];

            if (firstExpansion == null) {
                result[0] = firstValue;
            }
            else {
                System.arraycopy(firstExpansion, 0, result, 0, firstLength);
            }

            if (lastExpansion == null) {
                result[firstLength] = lastValue;
            }
            else {
                System.arraycopy(lastExpansion, 0, result, firstLength, lastLength);
            }
        }

        return result;
    }

    /**
     * 检查比较顺序是否可忽略。
     * @return 如果字符可忽略则返回 true，否则返回 false。
     */
    final static boolean isIgnorable(int order)
    {
        return ((primaryOrder(order) == 0) ? true : false);
    }

    /**
     * 获取字符串中下一个收缩字符的排序优先级。
     * @param ch 收缩字符标记的起始字符
     * @return 下一个收缩字符的排序。如果到达字符串末尾，则返回 NULLORDER。
     */
    private int nextContractChar(int ch)
    {
        // 首先获取此单个字符的排序，这总是列表中的第一个元素
        Vector<EntryPair> list = ordering.getContractValues(ch);
        EntryPair pair = list.firstElement();
        int order = pair.value;

        // 查找列表中最长的收缩字符序列的长度。
        // 构建器代码中的逻辑确保最长的序列总是最后一个。
        pair = list.lastElement();
        int maxLength = pair.entryName.length();

        // （在这里克隆 Normalizer 以确保我们在下一个循环中进行的查找不会影响文本中的实际位置）
        NormalizerBase tempText = (NormalizerBase)text.clone();

        // 提取字符串中的下一个 maxLength 字符（我们必须使用 Normalizer 来确保我们的偏移量与迭代器其余部分使用的偏移量对应）并将其存储在 "fragment" 中。
        tempText.previous();
        key.setLength(0);
        int c = tempText.next();
        while (maxLength > 0 && c != NormalizerBase.DONE) {
            if (Character.isSupplementaryCodePoint(c)) {
                key.append(Character.toChars(c));
                maxLength -= 2;
            } else {
                key.append((char)c);
                --maxLength;
            }
            c = tempText.next();
        }
        String fragment = key.toString();
        // 现在我们有了这个片段，遍历此列表，查找与实际文本中的字符匹配的最长序列。（maxLength 用于跟踪最长序列的长度）
        // 退出此循环时，maxLength 将包含匹配序列的长度，order 将包含与此序列对应的排序元素值
        maxLength = 1;
        for (int i = list.size() - 1; i > 0; i--) {
            pair = list.elementAt(i);
            if (!pair.fwd)
                continue;

            if (fragment.startsWith(pair.entryName) && pair.entryName.length()
                    > maxLength) {
                maxLength = pair.entryName.length();
                order = pair.value;
            }
        }

        // 将当前迭代位置移动到匹配序列的末尾，并返回相应的排序元素值（如果没有匹配的序列，我们已经移动到正确的位置，order 已经包含单个字符的正确排序元素值）
        while (maxLength > 1) {
            c = text.next();
            maxLength -= Character.charCount(c);
        }
        return order;
    }

    /**
     * 获取字符串中前一个收缩字符的排序优先级。
     * @param ch 收缩字符标记的起始字符
     * @return 前一个收缩字符的排序。如果到达字符串末尾，则返回 NULLORDER。
     */
    private int prevContractChar(int ch)
    {
        // 此函数与 nextContractChar() 完全相同，只是我们在 Normalizer 上交换了 next() 和 previous() 调用，并且跳过了 fwd 标志打开的条目对。
        // 请注意，我们在处理片段时仍然使用 append() 和 startsWith()。这是因为用于反向迭代的条目对的名称已经反转。
        Vector<EntryPair> list = ordering.getContractValues(ch);
        EntryPair pair = list.firstElement();
        int order = pair.value;


                    pair = list.lastElement();
        int maxLength = pair.entryName.length();

        NormalizerBase tempText = (NormalizerBase)text.clone();

        tempText.next();
        key.setLength(0);
        int c = tempText.previous();
        while (maxLength > 0 && c != NormalizerBase.DONE) {
            if (Character.isSupplementaryCodePoint(c)) {
                key.append(Character.toChars(c));
                maxLength -= 2;
            } else {
                key.append((char)c);
                --maxLength;
            }
            c = tempText.previous();
        }
        String fragment = key.toString();

        maxLength = 1;
        for (int i = list.size() - 1; i > 0; i--) {
            pair = list.elementAt(i);
            if (pair.fwd)
                continue;

            if (fragment.startsWith(pair.entryName) && pair.entryName.length()
                    > maxLength) {
                maxLength = pair.entryName.length();
                order = pair.value;
            }
        }

        while (maxLength > 1) {
            c = text.previous();
            maxLength -= Character.charCount(c);
        }
        return order;
    }

    // 未映射字符的默认值
    final static int UNMAPPEDCHARVALUE = 0x7FFF0000;

    // 文本规范化器
    private NormalizerBase text = null;
    // 缓冲区
    private int[] buffer = null;
    // 扩展索引
    private int expIndex = 0;
    // 用于构建键的字符串缓冲区
    private StringBuffer key = new StringBuffer(5);
    // 交换顺序
    private int swapOrder = 0;
    // 排序表
    private RBCollationTables ordering;
    // 规则基础排序器的所有者
    private RuleBasedCollator owner;
}
