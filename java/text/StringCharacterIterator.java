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
 * <code>StringCharacterIterator</code> 实现了 <code>CharacterIterator</code> 协议，用于 <code>String</code>。
 * <code>StringCharacterIterator</code> 类遍历整个 <code>String</code>。
 *
 * @see CharacterIterator
 */

public final class StringCharacterIterator implements CharacterIterator
{
    private String text;
    private int begin;
    private int end;
    // 不变性：begin <= pos <= end
    private int pos;

    /**
     * 构造一个初始索引为 0 的迭代器。
     *
     * @param text 要迭代的 {@code String}
     */
    public StringCharacterIterator(String text)
    {
        this(text, 0);
    }

    /**
     * 构造一个具有指定初始索引的迭代器。
     *
     * @param  text   要迭代的 String
     * @param  pos    初始迭代器位置
     */
    public StringCharacterIterator(String text, int pos)
    {
        this(text, 0, text.length(), pos);
    }

    /**
     * 构造一个在给定字符串的给定范围内迭代的迭代器，并将索引设置在指定位置。
     *
     * @param  text   要迭代的 String
     * @param  begin  第一个字符的索引
     * @param  end    最后一个字符之后的字符索引
     * @param  pos    初始迭代器位置
     */
    public StringCharacterIterator(String text, int begin, int end, int pos) {
        if (text == null)
            throw new NullPointerException();
        this.text = text;

        if (begin < 0 || begin > end || end > text.length())
            throw new IllegalArgumentException("无效的子字符串范围");

        if (pos < begin || pos > end)
            throw new IllegalArgumentException("无效的位置");

        this.begin = begin;
        this.end = end;
        this.pos = pos;
    }

    /**
     * 重置此迭代器以指向新的字符串。此包可见方法用于其他 java.text 类，以避免每次调用 setText 方法时都分配新的 StringCharacterIterator 对象。
     *
     * @param  text   要迭代的 String
     * @since 1.2
     */
    public void setText(String text) {
        if (text == null)
            throw new NullPointerException();
        this.text = text;
        this.begin = 0;
        this.end = text.length();
        this.pos = 0;
    }

    /**
     * 为 String 实现 CharacterIterator.first()。
     * @see CharacterIterator#first
     */
    public char first()
    {
        pos = begin;
        return current();
    }

    /**
     * 为 String 实现 CharacterIterator.last()。
     * @see CharacterIterator#last
     */
    public char last()
    {
        if (end != begin) {
            pos = end - 1;
        } else {
            pos = end;
        }
        return current();
    }

    /**
     * 为 String 实现 CharacterIterator.setIndex()。
     * @see CharacterIterator#setIndex
     */
    public char setIndex(int p)
    {
        if (p < begin || p > end)
            throw new IllegalArgumentException("无效的索引");
        pos = p;
        return current();
    }

    /**
     * 为 String 实现 CharacterIterator.current()。
     * @see CharacterIterator#current
     */
    public char current()
    {
        if (pos >= begin && pos < end) {
            return text.charAt(pos);
        }
        else {
            return DONE;
        }
    }

    /**
     * 为 String 实现 CharacterIterator.next()。
     * @see CharacterIterator#next
     */
    public char next()
    {
        if (pos < end - 1) {
            pos++;
            return text.charAt(pos);
        }
        else {
            pos = end;
            return DONE;
        }
    }

    /**
     * 为 String 实现 CharacterIterator.previous()。
     * @see CharacterIterator#previous
     */
    public char previous()
    {
        if (pos > begin) {
            pos--;
            return text.charAt(pos);
        }
        else {
            return DONE;
        }
    }

    /**
     * 为 String 实现 CharacterIterator.getBeginIndex()。
     * @see CharacterIterator#getBeginIndex
     */
    public int getBeginIndex()
    {
        return begin;
    }

    /**
     * 为 String 实现 CharacterIterator.getEndIndex()。
     * @see CharacterIterator#getEndIndex
     */
    public int getEndIndex()
    {
        return end;
    }

    /**
     * 为 String 实现 CharacterIterator.getIndex()。
     * @see CharacterIterator#getIndex
     */
    public int getIndex()
    {
        return pos;
    }

    /**
     * 比较两个 StringCharacterIterator 对象的相等性。
     * @param obj 要比较的 StringCharacterIterator 对象。
     * @return 如果给定的 obj 与这个 StringCharacterIterator 对象相同，则返回 true；否则返回 false。
     */
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof StringCharacterIterator))
            return false;

        StringCharacterIterator that = (StringCharacterIterator) obj;

        if (hashCode() != that.hashCode())
            return false;
        if (!text.equals(that.text))
            return false;
        if (pos != that.pos || begin != that.begin || end != that.end)
            return false;
        return true;
    }

    /**
     * 计算此迭代器的哈希码。
     * @return 哈希码
     */
    public int hashCode()
    {
        return text.hashCode() ^ pos ^ begin ^ end;
    }

    /**
     * 创建此迭代器的副本。
     * @return 此迭代器的副本
     */
    public Object clone()
    {
        try {
            StringCharacterIterator other
            = (StringCharacterIterator) super.clone();
            return other;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

}
