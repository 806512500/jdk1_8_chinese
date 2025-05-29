
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
 * (C) Copyright IBM Corp. 1996, 1997 - All Rights Reserved
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

import java.util.ArrayList;

/**
 * 用于规范化和合并排序模式的工具类。
 * 模式是形式为 <entry>* 的字符串，其中 <entry> 的形式为：
 * <pattern> := <entry>*
 * <entry> := <separator><chars>{"/"<extension>}
 * <separator> := "=", ",", ";", "<", "&"
 * <chars> 和 <extension> 都是任意字符串。
 * 未加引号的空格将被忽略。
 * 'xxx' 可用于引用字符
 * 与 Collator 的一个不同之处在于，& 用于重置到当前点。换句话说，它引入了一个新的序列，该序列将被添加到旧序列中。
 * 例如： "a < b < c < d" 与 "a < b & b < c & c < d" 或 "a < b < d & b < c" 相同。
 * XXX: 使 '' 成为单引号。
 * @see PatternEntry
 * @author             Mark Davis, Helena Shih
 */

final class MergeCollation {

    /**
     * 从模式创建对象
     * @exception ParseException 如果输入的模式不正确。
     */
    public MergeCollation(String pattern) throws ParseException
    {
        for (int i = 0; i < statusArray.length; i++)
            statusArray[i] = 0;
        setPattern(pattern);
    }

    /**
     * 恢复当前模式
     */
    public String getPattern() {
        return getPattern(true);
    }

    /**
     * 恢复当前模式。
     * @param withWhiteSpace 在条目周围添加空格，并在 & 和 < 前添加 \n
     */
    public String getPattern(boolean withWhiteSpace) {
        StringBuffer result = new StringBuffer();
        PatternEntry tmp = null;
        ArrayList<PatternEntry> extList = null;
        int i;
        for (i = 0; i < patterns.size(); ++i) {
            PatternEntry entry = patterns.get(i);
            if (!entry.extension.isEmpty()) {
                if (extList == null)
                    extList = new ArrayList<>();
                extList.add(entry);
            } else {
                if (extList != null) {
                    PatternEntry last = findLastWithNoExtension(i-1);
                    for (int j = extList.size() - 1; j >= 0 ; j--) {
                        tmp = extList.get(j);
                        tmp.addToBuffer(result, false, withWhiteSpace, last);
                    }
                    extList = null;
                }
                entry.addToBuffer(result, false, withWhiteSpace, null);
            }
        }
        if (extList != null) {
            PatternEntry last = findLastWithNoExtension(i-1);
            for (int j = extList.size() - 1; j >= 0 ; j--) {
                tmp = extList.get(j);
                tmp.addToBuffer(result, false, withWhiteSpace, last);
            }
            extList = null;
        }
        return result.toString();
    }

    private final PatternEntry findLastWithNoExtension(int i) {
        for (--i;i >= 0; --i) {
            PatternEntry entry = patterns.get(i);
            if (entry.extension.isEmpty()) {
                return entry;
            }
        }
        return null;
    }

    /**
     * 为排序生成器生成模式。
     * @return 生成排序生成器可理解的字符串格式。
     */
    public String emitPattern() {
        return emitPattern(true);
    }

    /**
     * 为排序生成器生成模式。
     * @param withWhiteSpace 在条目周围添加空格，并在 & 和 < 前添加 \n
     * @return 生成排序生成器可理解的字符串格式。
     */
    public String emitPattern(boolean withWhiteSpace) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < patterns.size(); ++i)
        {
            PatternEntry entry = patterns.get(i);
            if (entry != null) {
                entry.addToBuffer(result, true, withWhiteSpace, null);
            }
        }
        return result.toString();
    }

    /**
     * 设置模式。
     */
    public void setPattern(String pattern) throws ParseException
    {
        patterns.clear();
        addPattern(pattern);
    }

    /**
     * 向当前模式添加一个新模式。
     * @param pattern 要添加的新模式
     */
    public void addPattern(String pattern) throws ParseException
    {
        if (pattern == null)
            return;

        PatternEntry.Parser parser = new PatternEntry.Parser(pattern);

        PatternEntry entry = parser.next();
        while (entry != null) {
            fixEntry(entry);
            entry = parser.next();
        }
    }

    /**
     * 获取独立条目的数量
     * @return 模式条目的数量
     */
    public int getCount() {
        return patterns.size();
    }

    /**
     * 获取独立条目
     * @param index 所需模式条目的偏移量
     * @return 请求的模式条目
     */
    public PatternEntry getItemAt(int index) {
        return patterns.get(index);
    }

    //============================================================
    // 私有成员
    //============================================================
    ArrayList<PatternEntry> patterns = new ArrayList<>(); // PatternEntries 的列表

    private transient PatternEntry saveEntry = null;
    private transient PatternEntry lastEntry = null;

    // 这实际上是在 fixEntry 内部用作局部变量，但我们在这里缓存它以避免每次调用方法时都创建新的实例。
    private transient StringBuffer excess = new StringBuffer();

                //
    // 在构建 MergeCollation 时，我们需要进行大量的搜索以查看给定的条目是否已经存在于表中。由于我们使用的是数组，
    // 这会使算法复杂度为 O(N*N)。为了加快速度，我们使用这个位数组来记住数组是否包含以每个 Unicode 字符开头的任何条目。
    // 如果没有，我们可以避免搜索。使用 BitSet 会更简单，但速度明显较慢。
    //
    private transient byte[] statusArray = new byte[8192];
    private final byte BITARRAYMASK = (byte)0x1;
    private final int  BYTEPOWER = 3;
    private final int  BYTEMASK = (1 << BYTEPOWER) - 1;

    /*
      如果强度是 RESET，则只需将 lastEntry 更改为当前的。（如果当前条目不在 patterns 中，则发出错误信号）。
      如果不是，则移除当前条目，并将其添加到 lastEntry 之后（通常是在末尾）。
      */
    private final void fixEntry(PatternEntry newEntry) throws ParseException
    {
        // 检查新条目是否与前一个条目具有相同的字符（当声明两个规范等效的字符串之间的差异的模式被规范化时，这可能会发生）。
        // 如果是，并且强度不是 IDENTICAL 或 RESET，则抛出异常（不能声明一个字符串不等于其自身）。       --rtg 5/24/99
        if (lastEntry != null && newEntry.chars.equals(lastEntry.chars)
                && newEntry.extension.equals(lastEntry.extension)) {
            if (newEntry.strength != Collator.IDENTICAL
                && newEntry.strength != PatternEntry.RESET) {
                    throw new ParseException("The entries " + lastEntry + " and "
                            + newEntry + " are adjacent in the rules, but have conflicting "
                            + "strengths: A character can't be unequal to itself.", -1);
            } else {
                // 否则，跳过此条目并表现得好像从未看到过它
                return;
            }
        }

        boolean changeLastEntry = true;
        if (newEntry.strength != PatternEntry.RESET) {
            int oldIndex = -1;

            if ((newEntry.chars.length() == 1)) {

                char c = newEntry.chars.charAt(0);
                int statusIndex = c >> BYTEPOWER;
                byte bitClump = statusArray[statusIndex];
                byte setBit = (byte)(BITARRAYMASK << (c & BYTEMASK));

                if (bitClump != 0 && (bitClump & setBit) != 0) {
                    oldIndex = patterns.lastIndexOf(newEntry);
                } else {
                    // 我们将添加一个以该字符开头的元素，因此提前设置其位。
                    statusArray[statusIndex] = (byte)(bitClump | setBit);
                }
            } else {
                oldIndex = patterns.lastIndexOf(newEntry);
            }
            if (oldIndex != -1) {
                patterns.remove(oldIndex);
            }

            excess.setLength(0);
            int lastIndex = findLastEntry(lastEntry, excess);

            if (excess.length() != 0) {
                newEntry.extension = excess + newEntry.extension;
                if (lastIndex != patterns.size()) {
                    lastEntry = saveEntry;
                    changeLastEntry = false;
                }
            }
            if (lastIndex == patterns.size()) {
                patterns.add(newEntry);
                saveEntry = newEntry;
            } else {
                patterns.add(lastIndex, newEntry);
            }
        }
        if (changeLastEntry) {
            lastEntry = newEntry;
        }
    }

    private final int findLastEntry(PatternEntry entry,
                              StringBuffer excessChars) throws ParseException
    {
        if (entry == null)
            return 0;

        if (entry.strength != PatternEntry.RESET) {
            // 向后搜索包含此字符串的字符串；最有可能的条目是最后一个

            int oldIndex = -1;
            if ((entry.chars.length() == 1)) {
                int index = entry.chars.charAt(0) >> BYTEPOWER;
                if ((statusArray[index] &
                    (BITARRAYMASK << (entry.chars.charAt(0) & BYTEMASK))) != 0) {
                    oldIndex = patterns.lastIndexOf(entry);
                }
            } else {
                oldIndex = patterns.lastIndexOf(entry);
            }
            if ((oldIndex == -1))
                throw new ParseException("couldn't find last entry: "
                                          + entry, oldIndex);
            return oldIndex + 1;
        } else {
            int i;
            for (i = patterns.size() - 1; i >= 0; --i) {
                PatternEntry e = patterns.get(i);
                if (e.chars.regionMatches(0,entry.chars,0,
                                              e.chars.length())) {
                    excessChars.append(entry.chars.substring(e.chars.length(),
                                                            entry.chars.length()));
                    break;
                }
            }
            if (i == -1)
                throw new ParseException("couldn't find: " + entry, i);
            return i + 1;
        }
    }
}
