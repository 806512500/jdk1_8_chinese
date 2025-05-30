
/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

/*
 * (C) Copyright IBM Corp. 1999,  All rights reserved.
 */
package java.awt.font;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.im.InputMethodHighlight;
import java.text.Annotation;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map;
import sun.font.Decoration;
import sun.font.FontResolver;
import sun.text.CodePointIterator;

/**
 * 该类存储了带样本文本段落中的 Font、GraphicAttribute 和 Decoration 区间。
 * <p>
 * 当前，该类针对少量区间进行了优化（最好是 1 个）。
 */
final class StyledParagraph {

    // 段落的长度
    private int length;

    // 如果整个段落有一个单一的 Decoration，它将存储在这里。否则此字段将被忽略。

    private Decoration decoration;

    // 如果整个段落有一个单一的 Font 或 GraphicAttribute，它将存储在这里。否则此字段将被忽略。
    private Object font;

    // 如果段落中有多个 Decorations，它们将按顺序存储在此 Vector 中。否则此向量和 decorationStarts 数组为 null。
    private Vector<Decoration> decorations;
    // 如果段落中有多个 Decorations，decorationStarts[i] 包含装饰 i 开始的索引。为了方便，此数组末尾有一个额外的条目，包含段落的长度。
    int[] decorationStarts;

    // 如果段落中有多个 Fonts/GraphicAttributes，它们将按顺序存储在此 Vector 中。否则此向量和 fontStarts 数组为 null。
    private Vector<Object> fonts;
    // 如果段落中有多个 Fonts/GraphicAttributes，fontStarts[i] 包含装饰 i 开始的索引。为了方便，此数组末尾有一个额外的条目，包含段落的长度。
    int[] fontStarts;

    private static int INITIAL_SIZE = 8;

    /**
     * 创建一个新的 StyledParagraph，覆盖给定的带样本文本。
     * @param aci 文本的迭代器
     * @param chars 从 aci 中提取的字符
     */
    public StyledParagraph(AttributedCharacterIterator aci,
                           char[] chars) {

        int start = aci.getBeginIndex();
        int end = aci.getEndIndex();
        length = end - start;

        int index = start;
        aci.first();

        do {
            final int nextRunStart = aci.getRunLimit();
            final int localIndex = index - start;

            Map<? extends Attribute, ?> attributes = aci.getAttributes();
            attributes = addInputMethodAttrs(attributes);
            Decoration d = Decoration.getDecoration(attributes);
            addDecoration(d, localIndex);

            Object f = getGraphicOrFont(attributes);
            if (f == null) {
                addFonts(chars, attributes, localIndex, nextRunStart - start);
            }
            else {
                addFont(f, localIndex);
            }

            aci.setIndex(nextRunStart);
            index = nextRunStart;

        } while (index < end);

        // 在 starts 数组中添加额外的条目，包含段落的长度。'this' 用作 Vector 中的虚拟值。
        if (decorations != null) {
            decorationStarts = addToVector(this, length, decorations, decorationStarts);
        }
        if (fonts != null) {
            fontStarts = addToVector(this, length, fonts, fontStarts);
        }
    }

    /**
     * 调整 starts 中的索引以反映在 pos 之后的插入。
     * starts 中大于 pos 的任何索引将增加 1。
     */
    private static void insertInto(int pos, int[] starts, int numStarts) {

        while (starts[--numStarts] > pos) {
            starts[numStarts] += 1;
        }
    }

    /**
     * 返回一个反映在文本中插入单个字符的 StyledParagraph。此方法将尝试重用给定的段落，但可能会创建一个新的段落。
     * @param aci 文本的迭代器。文本应与用于创建（或最近更新）oldParagraph 的文本相同，除了在 insertPos 插入了一个字符。
     * @param chars aci 中的字符
     * @param insertPos 新字符在 aci 中的索引
     * @param oldParagraph 插入前 aci 中文本的 StyledParagraph
     */
    public static StyledParagraph insertChar(AttributedCharacterIterator aci,
                                             char[] chars,
                                             int insertPos,
                                             StyledParagraph oldParagraph) {

        // 如果 insertPos 处的样式与 insertPos-1 处的样式匹配，将重用 oldParagraph。否则我们创建一个新的段落。

        char ch = aci.setIndex(insertPos);
        int relativePos = Math.max(insertPos - aci.getBeginIndex() - 1, 0);

        Map<? extends Attribute, ?> attributes =
            addInputMethodAttrs(aci.getAttributes());
        Decoration d = Decoration.getDecoration(attributes);
        if (!oldParagraph.getDecorationAt(relativePos).equals(d)) {
            return new StyledParagraph(aci, chars);
        }
        Object f = getGraphicOrFont(attributes);
        if (f == null) {
            FontResolver resolver = FontResolver.getInstance();
            int fontIndex = resolver.getFontIndex(ch);
            f = resolver.getFont(fontIndex, attributes);
        }
        if (!oldParagraph.getFontOrGraphicAt(relativePos).equals(f)) {
            return new StyledParagraph(aci, chars);
        }

        // 插入到现有段落中
        oldParagraph.length += 1;
        if (oldParagraph.decorations != null) {
            insertInto(relativePos,
                       oldParagraph.decorationStarts,
                       oldParagraph.decorations.size());
        }
        if (oldParagraph.fonts != null) {
            insertInto(relativePos,
                       oldParagraph.fontStarts,
                       oldParagraph.fonts.size());
        }
        return oldParagraph;
    }

    /**
     * 调整 starts 中的索引以反映从 deleteAt 之后的删除。
     * starts 中大于 deleteAt 的任何索引将减少 1。调用者有责任确保不会出现 0 长度的区间。
     */
    private static void deleteFrom(int deleteAt, int[] starts, int numStarts) {

        while (starts[--numStarts] > deleteAt) {
            starts[numStarts] -= 1;
        }
    }

    /**
     * 返回一个反映在文本中删除单个字符的 StyledParagraph。此方法将尝试重用给定的段落，但可能会创建一个新的段落。
     * @param aci 文本的迭代器。文本应与用于创建（或最近更新）oldParagraph 的文本相同，除了在 deletePos 删除了一个字符。
     * @param chars aci 中的字符
     * @param deletePos 被删除字符的索引
     * @param oldParagraph 删除前 aci 中文本的 StyledParagraph
     */
    public static StyledParagraph deleteChar(AttributedCharacterIterator aci,
                                             char[] chars,
                                             int deletePos,
                                             StyledParagraph oldParagraph) {

        // 我们将重用 oldParagraph，除非在 deletePos 有一个长度为 1 的区间。我们可以做更多的工作来检查单个的 Font 和 Decoration 区间，但我们现在没有这样做...
        deletePos -= aci.getBeginIndex();

        if (oldParagraph.decorations == null && oldParagraph.fonts == null) {
            oldParagraph.length -= 1;
            return oldParagraph;
        }

        if (oldParagraph.getRunLimit(deletePos) == deletePos + 1) {
            if (deletePos == 0 || oldParagraph.getRunLimit(deletePos - 1) == deletePos) {
                return new StyledParagraph(aci, chars);
            }
        }

        oldParagraph.length -= 1;
        if (oldParagraph.decorations != null) {
            deleteFrom(deletePos,
                       oldParagraph.decorationStarts,
                       oldParagraph.decorations.size());
        }
        if (oldParagraph.fonts != null) {
            deleteFrom(deletePos,
                       oldParagraph.fontStarts,
                       oldParagraph.fonts.size());
        }
        return oldParagraph;
    }

    /**
     * 返回与给定索引处的 Font、GraphicAttribute 或 Decoration 不同的索引。
     * @param index 段落中的有效索引
     * @return 从 index 开始属性发生变化的第一个索引
     */
    public int getRunLimit(int index) {

        if (index < 0 || index >= length) {
            throw new IllegalArgumentException("index out of range");
        }
        int limit1 = length;
        if (decorations != null) {
            int run = findRunContaining(index, decorationStarts);
            limit1 = decorationStarts[run + 1];
        }
        int limit2 = length;
        if (fonts != null) {
            int run = findRunContaining(index, fontStarts);
            limit2 = fontStarts[run + 1];
        }
        return Math.min(limit1, limit2);
    }

    /**
     * 返回给定索引处生效的 Decoration。
     * @param index 段落中的有效索引
     * @return index 处的 Decoration。
     */
    public Decoration getDecorationAt(int index) {

        if (index < 0 || index >= length) {
            throw new IllegalArgumentException("index out of range");
        }
        if (decorations == null) {
            return decoration;
        }
        int run = findRunContaining(index, decorationStarts);
        return decorations.elementAt(run);
    }

    /**
     * 返回给定索引处生效的 Font 或 GraphicAttribute。
     * 客户端必须测试返回值的类型以确定其类型。
     * @param index 段落中的有效索引
     * @return index 处的 Font 或 GraphicAttribute。
     */
    public Object getFontOrGraphicAt(int index) {

        if (index < 0 || index >= length) {
            throw new IllegalArgumentException("index out of range");
        }
        if (fonts == null) {
            return font;
        }
        int run = findRunContaining(index, fontStarts);
        return fonts.elementAt(run);
    }

    /**
     * 返回 i 使得 starts[i] &lt;= index &lt; starts[i+1]。starts
     * 必须按递增顺序排列，且至少有一个元素大于 index。
     */
    private static int findRunContaining(int index, int[] starts) {

        for (int i = 1; true; i++) {
            if (starts[i] > index) {
                return i - 1;
            }
        }
    }

    /**
     * 将给定的 Object 追加到给定的 Vector 中。将
     * 给定的索引添加到给定的 starts 数组中。如果
     * starts 数组没有足够的空间容纳索引，则创建
     * 新数组并返回。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static int[] addToVector(Object obj,
                                     int index,
                                     Vector v,
                                     int[] starts) {

        if (!v.lastElement().equals(obj)) {
            v.addElement(obj);
            int count = v.size();
            if (starts.length == count) {
                int[] temp = new int[starts.length * 2];
                System.arraycopy(starts, 0, temp, 0, starts.length);
                starts = temp;
            }
            starts[count - 1] = index;
        }
        return starts;
    }

    /**
     * 在给定索引处添加具有给定 Decoration 的新 Decoration 区间。
     */
    private void addDecoration(Decoration d, int index) {

        if (decorations != null) {
            decorationStarts = addToVector(d,
                                           index,
                                           decorations,
                                           decorationStarts);
        }
        else if (decoration == null) {
            decoration = d;
        }
        else {
            if (!decoration.equals(d)) {
                decorations = new Vector<Decoration>(INITIAL_SIZE);
                decorations.addElement(decoration);
                decorations.addElement(d);
                decorationStarts = new int[INITIAL_SIZE];
                decorationStarts[0] = 0;
                decorationStarts[1] = index;
            }
        }
    }

    /**
     * 在给定索引处添加具有给定对象的新 Font/GraphicAttribute 区间。
     */
    private void addFont(Object f, int index) {

        if (fonts != null) {
            fontStarts = addToVector(f, index, fonts, fontStarts);
        }
        else if (font == null) {
            font = f;
        }
        else {
            if (!font.equals(f)) {
                fonts = new Vector<Object>(INITIAL_SIZE);
                fonts.addElement(font);
                fonts.addElement(f);
                fontStarts = new int[INITIAL_SIZE];
                fontStarts[0] = 0;
                fontStarts[1] = index;
            }
        }
    }

    /**
     * 使用 FontResolver 解析给定的 chars 为 Fonts，然后为每个 Font 添加区间。
     */
    private void addFonts(char[] chars, Map<? extends Attribute, ?> attributes,
                          int start, int limit) {

        FontResolver resolver = FontResolver.getInstance();
        CodePointIterator iter = CodePointIterator.create(chars, start, limit);
        for (int runStart = iter.charIndex(); runStart < limit; runStart = iter.charIndex()) {
            int fontIndex = resolver.nextFontRunIndex(iter);
            addFont(resolver.getFont(fontIndex, attributes), runStart);
        }
    }


                /**
     * 返回一个包含 oldStyles 条目以及输入方法条目的 Map，如果有的话。
     */
    static Map<? extends Attribute, ?>
           addInputMethodAttrs(Map<? extends Attribute, ?> oldStyles) {

        Object value = oldStyles.get(TextAttribute.INPUT_METHOD_HIGHLIGHT);

        try {
            if (value != null) {
                if (value instanceof Annotation) {
                    value = ((Annotation)value).getValue();
                }

                InputMethodHighlight hl;
                hl = (InputMethodHighlight) value;

                Map<? extends Attribute, ?> imStyles = null;
                try {
                    imStyles = hl.getStyle();
                } catch (NoSuchMethodError e) {
                }

                if (imStyles == null) {
                    Toolkit tk = Toolkit.getDefaultToolkit();
                    imStyles = tk.mapInputMethodHighlight(hl);
                }

                if (imStyles != null) {
                    HashMap<Attribute, Object>
                        newStyles = new HashMap<>(5, (float)0.9);
                    newStyles.putAll(oldStyles);

                    newStyles.putAll(imStyles);

                    return newStyles;
                }
            }
        }
        catch(ClassCastException e) {
        }

        return oldStyles;
    }

    /**
     * 从给定的属性中提取一个 GraphicAttribute 或 Font。
     * 如果属性中不包含 GraphicAttribute、Font 或 Font 家族条目，此方法返回 null。
     */
    private static Object getGraphicOrFont(
            Map<? extends Attribute, ?> attributes) {

        Object value = attributes.get(TextAttribute.CHAR_REPLACEMENT);
        if (value != null) {
            return value;
        }
        value = attributes.get(TextAttribute.FONT);
        if (value != null) {
            return value;
        }

        if (attributes.get(TextAttribute.FAMILY) != null) {
            return Font.getFont(attributes);
        }
        else {
            return null;
        }
    }
}
