/*
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.font;

import java.text.CharacterIterator;

class CharArrayIterator implements CharacterIterator {

    private char[] chars;
    private int pos;
    private int begin;

    CharArrayIterator(char[] chars) {

        reset(chars, 0);
    }

    CharArrayIterator(char[] chars, int begin) {

        reset(chars, begin);
    }

    /**
     * 将位置设置为 getBeginIndex() 并返回该位置的字符。
     * @return 文本中的第一个字符，如果文本为空则返回 DONE
     * @see getBeginIndex
     */
    public char first() {

        pos = 0;
        return current();
    }

    /**
     * 将位置设置为 getEndIndex()-1（如果文本为空则为 getEndIndex()）并返回该位置的字符。
     * @return 文本中的最后一个字符，如果文本为空则返回 DONE
     * @see getEndIndex
     */
    public char last() {

        if (chars.length > 0) {
            pos = chars.length-1;
        }
        else {
            pos = 0;
        }
        return current();
    }

    /**
     * 获取当前位置（由 getIndex() 返回）的字符。
     * @return 当前位置的字符，如果当前位置超出文本末尾则返回 DONE
     * @see getIndex
     */
    public char current() {

        if (pos >= 0 && pos < chars.length) {
            return chars[pos];
        }
        else {
            return DONE;
        }
    }

    /**
     * 将迭代器的索引递增 1 并返回新索引处的字符。如果结果索引大于或等于 getEndIndex()，
     * 则将当前索引重置为 getEndIndex() 并返回 DONE。
     * @return 新位置的字符，如果新位置超出文本范围则返回 DONE
     */
    public char next() {

        if (pos < chars.length-1) {
            pos++;
            return chars[pos];
        }
        else {
            pos = chars.length;
            return DONE;
        }
    }

    /**
     * 将迭代器的索引递减 1 并返回新索引处的字符。如果当前索引为 getBeginIndex()，
     * 则索引保持在 getBeginIndex() 并返回 DONE。
     * @return 新位置的字符，如果当前位置等于 getBeginIndex() 则返回 DONE
     */
    public char previous() {

        if (pos > 0) {
            pos--;
            return chars[pos];
        }
        else {
            pos = 0;
            return DONE;
        }
    }

    /**
     * 将位置设置为文本中的指定位置并返回该字符。
     * @param position 文本中的位置。有效值范围从 getBeginIndex() 到 getEndIndex()。
     * 如果提供了无效值，则抛出 IllegalArgumentException。
     * @return 指定位置的字符，如果指定位置等于 getEndIndex() 则返回 DONE
     */
    public char setIndex(int position) {

        position -= begin;
        if (position < 0 || position > chars.length) {
            throw new IllegalArgumentException("Invalid index");
        }
        pos = position;
        return current();
    }

    /**
     * 返回文本的起始索引。
     * @return 文本开始的索引。
     */
    public int getBeginIndex() {
        return begin;
    }

    /**
     * 返回文本的结束索引。此索引是文本末尾后第一个字符的索引。
     * @return 文本最后一个字符后的索引
     */
    public int getEndIndex() {
        return begin+chars.length;
    }

    /**
     * 返回当前索引。
     * @return 当前索引。
     */
    public int getIndex() {
        return begin+pos;
    }

    /**
     * 创建此迭代器的副本
     * @return 此迭代器的副本
     */
    public Object clone() {
        CharArrayIterator c = new CharArrayIterator(chars, begin);
        c.pos = this.pos;
        return c;
    }

    void reset(char[] chars) {
        reset(chars, 0);
    }

    void reset(char[] chars, int begin) {

        this.chars = chars;
        this.begin = begin;
        pos = 0;
    }
}
