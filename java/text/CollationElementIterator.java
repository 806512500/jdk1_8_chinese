
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
 * <code>CollationElementIterator</code> 类用作迭代器，用于遍历国际字符串中的每个字符。使用此迭代器返回定位字符的排序优先级。字符的排序优先级，我们称之为键，定义了字符在给定排序对象中的排序方式。
 *
 * <p>
 * 例如，在西班牙语中：
 * <blockquote>
 * <pre>
 * "ca" &rarr; 第一个键是 key('c')，第二个键是 key('a')。
 * "cha" &rarr; 第一个键是 key('ch')，第二个键是 key('a')。
 * </pre>
 * </blockquote>
 * 在德语中，
 * <blockquote>
 * <pre>
 * "\u00e4b" &rarr; 第一个键是 key('a')，第二个键是 key('e')，第三个键是 key('b')。
 * </pre>
 * </blockquote>
 * 字符的键是一个由主要顺序（short）、次要顺序（byte）和三级顺序（byte）组成的整数。Java 严格定义了其基本数据类型的大小和有符号性。因此，静态函数 <code>primaryOrder</code>、<code>secondaryOrder</code> 和 <code>tertiaryOrder</code> 分别返回 <code>int</code>、<code>short</code> 和 <code>short</code> 以确保键值的正确性。
 *
 * <p>
 * 迭代器用法示例，
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
 * <code>CollationElementIterator.next</code> 返回下一个字符的排序顺序。排序顺序由主要顺序、次要顺序和三级顺序组成。排序顺序的数据类型是 <strong>int</strong>。排序顺序的前 16 位是其主要顺序；接下来的 8 位是次要顺序，最后的 8 位是三级顺序。
 *
 * <p><b>注意：</b> <code>CollationElementIterator</code> 是 <code>RuleBasedCollator</code> 实现的一部分。它仅可用于 <code>RuleBasedCollator</code> 实例。
 *
 * @see                Collator
 * @see                RuleBasedCollator
 * @author             Helena Shih, Laura Werner, Richard Gillam
 */
public final class CollationElementIterator
{
    /**
     * 空顺序，表示字符串的末尾已被光标到达。
     */
    public final static int NULLORDER = 0xffffffff;

    /**
     * CollationElementIterator 构造函数。此构造函数接受源字符串和排序对象。光标将根据预定义的排序规则遍历源字符串。如果源字符串为空，则在调用 next() 时将返回 NULLORDER。
     * @param sourceText 源字符串。
     * @param owner 排序对象。
     */
    CollationElementIterator(String sourceText, RuleBasedCollator owner) {
        this.owner = owner;
        ordering = owner.getTables();
        if ( sourceText.length() != 0 ) {
            NormalizerBase.Mode mode =
                CollatorUtilities.toNormalizerMode(owner.getDecomposition());
            text = new NormalizerBase(sourceText, mode);
        }
    }

    /**
     * CollationElementIterator 构造函数。此构造函数接受源字符串和排序对象。光标将根据预定义的排序规则遍历源字符串。如果源字符串为空，则在调用 next() 时将返回 NULLORDER。
     * @param sourceText 源字符串。
     * @param owner 排序对象。
     */
    CollationElementIterator(CharacterIterator sourceText, RuleBasedCollator owner) {
        this.owner = owner;
        ordering = owner.getTables();
        NormalizerBase.Mode mode =
            CollatorUtilities.toNormalizerMode(owner.getDecomposition());
        text = new NormalizerBase(sourceText, mode);
    }

    /**
     * 将光标重置到字符串的开头。下一次调用 next() 将返回字符串中的第一个排序元素。
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
     * 获取字符串中的下一个排序元素。 <p>此迭代器遍历从字符串构建的排序元素序列。
     * 由于字符与排序元素之间不一定是一对一的映射，这并不意味着“返回字符串中下一个字符的排序元素 [或排序优先级]”。 </p>
     * <p>此函数返回迭代器当前指向的排序元素，然后更新内部指针以指向下一个元素。
     * previous() 先更新指针，然后返回元素。这意味着在迭代方向改变时（即调用 next() 然后调用 previous()，或调用 previous() 然后调用 next()），你会两次获得相同的元素。</p>
     *
     * @return 下一个排序元素
     */
    public int next()
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

        // 如果缓冲区中包含任何分解的字符值
        // 在继续遍历 Normalizer 的 CharacterIterator 之前返回它们的强度顺序。
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

        // 我们是否到达了 Normalizer 的文本末尾？
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
     * 获取字符串中的上一个排序元素。 <p>此迭代器遍历从字符串构建的排序元素序列。
     * 由于字符与排序元素之间不一定是一对一的映射，这并不意味着“返回字符串中上一个字符的排序元素 [或排序优先级]”。 </p>
     * <p>此函数更新迭代器的内部指针以指向当前指向的排序元素之前的元素，然后返回该元素，而 next() 返回当前元素，然后更新指针。这意味着在迭代方向改变时（即调用 next() 然后调用 previous()，或调用 previous() 然后调用 next()），你会两次获得相同的元素。</p>
     *
     * @return 上一个排序元素
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
     * 返回排序元素的主要部分。
     * @param order 排序元素
     * @return 元素的主要部分
     */
    public final static int primaryOrder(int order)
    {
        order &= RBCollationTables.PRIMARYORDERMASK;
        return (order >>> RBCollationTables.PRIMARYORDERSHIFT);
    }
    /**
     * 返回排序元素的次要部分。
     * @param order 排序元素
     * @return 元素的次要部分
     */
    public final static short secondaryOrder(int order)
    {
        order = order & RBCollationTables.SECONDARYORDERMASK;
        return ((short)(order >> RBCollationTables.SECONDARYORDERSHIFT));
    }
    /**
     * 返回排序元素的三级部分。
     * @param order 排序元素
     * @return 元素的三级部分
     */
    public final static short tertiaryOrder(int order)
    {
        return ((short)(order &= RBCollationTables.TERTIARYORDERMASK));
    }

    /**
     * 获取所需强度的比较顺序。忽略其他差异。
     * @param order 排序值
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
     * 将迭代器设置为指向与指定字符（参数是原始字符串中的字符偏移量，而不是其对应的排序元素序列中的偏移量）对应的排序元素。下一次调用 next() 方法返回的值将是文本中指定位置对应的排序元素。如果该位置位于收缩字符序列的中间，那么下一次调用 next() 的结果将是该序列的排序元素。这意味着 getOffset() 方法不保证返回与传递给 setOffset() 方法相同的值。
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

                // 如果所需的字符不参与收缩字符序列，则跳过所有回溯逻辑——我们已经坐在正确的字符上了
                if (ordering.usedInContractSeq(c)) {
                    // 通过字符串向后遍历，直到看到一个不参与收缩字符序列的字符
                    while (ordering.usedInContractSeq(c)) {
                        c = text.previous();
                    }
                    // 现在使用此对象的 next() 方法向前遍历，直到我们经过起始点，并将当前位置设置为起始位置之前或处的最后一个“字符”的开始
                    int last = text.getIndex();
                    while (text.getIndex() <= newOffset) {
                        last = text.getIndex();
                        next();
                    }
                    text.setIndexOnly(last);
                    // 我们不需要这个，因为 last 是收缩序列的起始位置
                    // text.previous();
                }
            }
        }
        buffer = null;
        expIndex = 0;
        swapOrder = 0;
    }

    /**
     * 返回与下一个排序元素对应的原始文本中的字符偏移量。（即，getOffset() 返回的位置对应于下一次调用 next() 方法将返回的排序元素。）此值始终是对应于排序元素的第一个字符的索引（当两个或多个字符都对应于同一个排序元素时，即为收缩字符序列）。这意味着如果先调用 setOffset(x)，然后立即调用 getOffset()，getOffset() 不一定会返回 x。
     *
     * @return 与下一个调用 next() 方法将返回的排序元素对应的原始文本中的字符偏移量。
     * @since 1.2
     */
    public int getOffset()
    {
        return (text != null) ? text.getIndex() : 0;
    }


    /**
     * 返回以指定比较顺序结束的任何扩展序列的最大长度。
     * @param order 由 previous 或 next 方法返回的排序顺序。
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
     * @param source  新的源文本
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
     * @param source  新的源文本。
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
     * 确定字符是否为泰语元音（排序在其基础辅音之后）。
     */
    private final static boolean isThaiPreVowel(int ch) {
        return (ch >= 0x0e40) && (ch <= 0x0e44);
    }

    /**
     * 确定字符是否为泰语基础辅音
     */
    private final static boolean isThaiBaseConsonant(int ch) {
        return (ch >= 0x0e01) && (ch <= 0x0e2e);
    }

    /**
     * 确定字符是否为老挝语元音（排序在其基础辅音之后）。
     */
    private final static boolean isLaoPreVowel(int ch) {
        return (ch >= 0x0ec0) && (ch <= 0x0ec4);
    }

    /**
     * 确定字符是否为老挝语基础辅音
     */
    private final static boolean isLaoBaseConsonant(int ch) {
        return (ch >= 0x0e81) && (ch <= 0x0eae);
    }

    /**
     * 此方法生成一个缓冲区，其中包含两个字符的排序元素，colFirst 的值在另一个字符之前。假设另一个字符在逻辑顺序中位于 colFirst 之前（否则你不需要这个方法，对吧？）。假设另一个字符的值已经计算出来。如果此字符有一个单一元素，则作为 lastValue 传递给此方法，lastExpansion 为 null。如果它有一个扩展，则在 lastExpansion 中传递，colLastValue 被忽略。
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
     * @return 如果字符可忽略，返回 true，否则返回 false。
     */
    final static boolean isIgnorable(int order)
    {
        return ((primaryOrder(order) == 0) ? true : false);
    }

    /**
     * 获取字符串中下一个收缩字符的排序优先级。
     * @param ch 收缩字符令牌的起始字符
     * @return 下一个收缩字符的排序。如果到达字符串末尾，返回 NULLORDER。
     */
    private int nextContractChar(int ch)
    {
        // 首先获取此单个字符的排序，它始终是列表中的第一个元素
        Vector<EntryPair> list = ordering.getContractValues(ch);
        EntryPair pair = list.firstElement();
        int order = pair.value;

        // 找出列表中最长的收缩字符序列的长度。构建器代码中有逻辑确保最长的序列总是最后一个。
        pair = list.lastElement();
        int maxLength = pair.entryName.length();

        // （在这里克隆 Normalizer 以确保我们在下一个循环中进行的查找不会影响文本中的实际位置）
        NormalizerBase tempText = (NormalizerBase)text.clone();

        // 提取字符串中的下一个 maxLength 个字符（我们必须使用 Normalizer 以确保我们的偏移量与迭代器其余部分使用的偏移量对应）并将其存储在“fragment”中。
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
        // 退出此循环时，maxLength 将包含匹配序列的长度，order 将包含对应于此序列的排序元素值
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

        // 将当前迭代位置移动到匹配序列的末尾，并返回适当的排序元素值（如果没有匹配的序列，我们已经移动到正确的位置，order 已经包含单个字符的正确排序元素值）
        while (maxLength > 1) {
            c = text.next();
            maxLength -= Character.charCount(c);
        }
        return order;
    }

    /**
     * 获取字符串中上一个收缩字符的排序优先级。
     * @param ch 收缩字符令牌的起始字符
     * @return 上一个收缩字符的排序。如果到达字符串末尾，返回 NULLORDER。
     */
    private int prevContractChar(int ch)
    {
        // 此函数与 nextContractChar() 完全相同，只是我们将 Normalizer 上的 next() 和 previous() 调用进行了交换，并且跳过了 fwd 标志打开的条目对。注意，我们在处理片段时仍然使用 append() 和 startsWith()。这是因为用于反向迭代的条目对的名称已经被反转。
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

    final static int UNMAPPEDCHARVALUE = 0x7FFF0000;

    private NormalizerBase text = null;
    private int[] buffer = null;
    private int expIndex = 0;
    private StringBuffer key = new StringBuffer(5);
    private int swapOrder = 0;
    private RBCollationTables ordering;
    private RuleBasedCollator owner;
}
