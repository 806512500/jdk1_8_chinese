/*
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
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
package java.text;

import java.util.ArrayList;

/**
 * CharacterIteratorFieldDelegate 结合来自 Format 的通知，生成一个 <code>AttributedCharacterIterator</code>。
 * 可以通过 <code>getIterator</code> 方法获取生成的 <code>AttributedCharacterIterator</code>。
 *
 */
class CharacterIteratorFieldDelegate implements Format.FieldDelegate {
    /**
     * AttributeStrings 的数组。每当 <code>formatted</code> 被调用的区域大于 size 时，
     * 将向 attributedStrings 添加一个新的 AttributedString 实例。对于现有区域的后续调用 <code>formatted</code>，
     * 将在现有的 AttributedStrings 上调用 addAttribute。
     */
    private ArrayList<AttributedString> attributedStrings;
    /**
     * 遇到的字符数量的累计计数。
     */
    private int size;


    CharacterIteratorFieldDelegate() {
        attributedStrings = new ArrayList<>();
    }

    public void formatted(Format.Field attr, Object value, int start, int end,
                          StringBuffer buffer) {
        if (start != end) {
            if (start < size) {
                // 调整现有运行的属性
                int index = size;
                int asIndex = attributedStrings.size() - 1;

                while (start < index) {
                    AttributedString as = attributedStrings.
                                           get(asIndex--);
                    int newIndex = index - as.length();
                    int aStart = Math.max(0, start - newIndex);

                    as.addAttribute(attr, value, aStart, Math.min(
                                    end - start, as.length() - aStart) +
                                    aStart);
                    index = newIndex;
                }
            }
            if (size < start) {
                // 填充属性
                attributedStrings.add(new AttributedString(
                                          buffer.substring(size, start)));
                size = start;
            }
            if (size < end) {
                // 添加新字符串
                int aStart = Math.max(start, size);
                AttributedString string = new AttributedString(
                                   buffer.substring(aStart, end));

                string.addAttribute(attr, value);
                attributedStrings.add(string);
                size = end;
            }
        }
    }

    public void formatted(int fieldID, Format.Field attr, Object value,
                          int start, int end, StringBuffer buffer) {
        formatted(attr, value, start, end, buffer);
    }

    /**
     * 返回一个 <code>AttributedCharacterIterator</code>，可用于迭代生成的格式化字符串。
     *
     * @param string 格式化结果。
     */
    public AttributedCharacterIterator getIterator(String string) {
        // 必要时添加最后一个 AttributedCharacterIterator
        // assert(size <= string.length());
        if (string.length() > size) {
            attributedStrings.add(new AttributedString(
                                  string.substring(size)));
            size = string.length();
        }
        int iCount = attributedStrings.size();
        AttributedCharacterIterator iterators[] = new
                                    AttributedCharacterIterator[iCount];

        for (int counter = 0; counter < iCount; counter++) {
            iterators[counter] = attributedStrings.
                                  get(counter).getIterator();
        }
        return new AttributedString(iterators).getIterator();
    }
}
