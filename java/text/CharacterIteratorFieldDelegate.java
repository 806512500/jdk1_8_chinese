/*
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;

/**
 * CharacterIteratorFieldDelegate 结合来自 Format 的通知，生成一个结果的 <code>AttributedCharacterIterator</code>。
 * 可以通过 <code>getIterator</code> 方法获取生成的 <code>AttributedCharacterIterator</code>。
 *
 */
class CharacterIteratorFieldDelegate implements Format.FieldDelegate {
    /**
     * AttributeStrings 的数组。每当为一个区域调用 <code>formatted</code> 且该区域的大小 > size 时，
     * 将向 attributedStrings 添加一个新的 AttributedString 实例。对于现有区域的后续 <code>formatted</code> 调用，
     * 将在现有的 AttributedStrings 上调用 addAttribute。
     */
    private ArrayList<AttributedString> attributedStrings;
    /**
     * 已遇到的字符数量的累计计数。
     */
    private int size;


    CharacterIteratorFieldDelegate() {
        attributedStrings = new ArrayList<>();
    }

    public void formatted(Format.Field attr, Object value, int start, int end,
                          StringBuffer buffer) {
        if (start != end) {
            if (start < size) {
                // 调整现有段的属性
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
     * 返回一个可以用来迭代格式化结果字符串的 <code>AttributedCharacterIterator</code>。
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
