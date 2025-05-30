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
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.text;


/**
 * 此接口定义了用于文本双向迭代的协议。
 * 迭代器迭代一个有界字符序列。字符从 getBeginIndex() 返回的值开始，到 getEndIndex() 返回的值减一为止。
 * <p>
 * 迭代器维护一个当前字符索引，其有效范围是从 getBeginIndex() 到 getEndIndex()；getEndIndex() 也被包括在内，以允许处理零长度文本范围和出于历史原因。
 * 可以通过调用 getIndex() 检索当前索引，并通过调用 setIndex()、first() 和 last() 直接设置当前索引。
 * <p>
 * 方法 previous() 和 next() 用于迭代。如果它们会移出 getBeginIndex() 到 getEndIndex() -1 的范围，则返回 DONE，表示迭代器已到达序列的末尾。DONE 也由其他方法返回，以指示当前索引超出此范围。
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
 * 本示例中的 notBoundary() 调用表示一些额外的停止条件。
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
     * 当迭代器到达文本的开头或结尾时返回的常量。值为 '\\uFFFF'，这是“不是字符”的值，不应出现在任何有效的 Unicode 字符串中。
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
     * 获取当前索引（由 getIndex() 返回）处的字符。
     * @return 当前索引处的字符，如果当前索引超出文本末尾则返回 DONE
     * @see #getIndex()
     */
    public char current();

    /**
     * 将迭代器的索引递增一并返回新索引处的字符。如果结果索引大于或等于 getEndIndex()，则将当前索引重置为 getEndIndex() 并返回 DONE。
     * @return 新位置处的字符，如果新位置超出文本范围则返回 DONE
     */
    public char next();

    /**
     * 将迭代器的索引递减一并返回新索引处的字符。如果当前索引为 getBeginIndex()，则索引保持在 getBeginIndex() 并返回 DONE。
     * @return 新位置处的字符，如果当前索引等于 getBeginIndex() 则返回 DONE
     */
    public char previous();

    /**
     * 将位置设置为文本中的指定位置并返回该字符。
     * @param position 文本中的位置。有效值范围从 getBeginIndex() 到 getEndIndex()。如果提供了无效值，则抛出 IllegalArgumentException。
     * @return 指定位置处的字符，如果指定位置等于 getEndIndex() 则返回 DONE
     */
    public char setIndex(int position);

    /**
     * 返回文本的起始索引。
     * @return 文本开始的索引。
     */
    public int getBeginIndex();

    /**
     * 返回文本的结束索引。此索引是文本末尾后第一个字符的索引。
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
     * @return 此迭代器的副本
     */
    public Object clone();

}
