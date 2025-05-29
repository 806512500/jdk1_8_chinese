
/*
 * Copyright (c) 1999, 2020, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Vector;
import sun.text.UCompactIntArray;
import sun.text.IntHashtable;
import sun.text.ComposedCharIter;
import sun.text.CollatorUtilities;
import sun.text.normalizer.NormalizerImpl;

/**
 * 该类包含解析 RuleBasedCollator 模式并从中构建 RBCollationTables 对象的所有代码。
 * 该类的特定实例仅在实际构建过程中存在——一旦 RBCollationTables 对象构建完成，RBTableBuilder 对象就会消失。
 * 该对象携带了仅在构建过程中需要的所有状态，以及将进入表对象本身的全部状态的“影子”副本。
 * 该对象通过 RBCollationTables.BuildAPI 类与 RBCollationTables 通信，RBCollationTables.BuildAPI 是 RBCollationTables 的内部类，提供了一个与 RBTableBuilder 通信的单独私有 API。
 * 该类不是 RBCollationTables 的内部类，因为它的体积较大。为了提高源代码的可读性，最好让构建器有自己的源文件。
 */
final class RBTableBuilder {

    public RBTableBuilder(RBCollationTables.BuildAPI tables) {
        this.tables = tables;
    }

    /**
     * 使用给定的规则创建一个基于表的排序对象。
     * 这是实际构建表并将它们存储回 RBCollationTables 对象的主要函数。它仅由 RBCollationTables 构造函数调用。
     * @see RuleBasedCollator#RuleBasedCollator
     * @exception ParseException 如果规则格式不正确。
     */

    public void build(String pattern, int decmp) throws ParseException {
        String expChars;
        String groupChars;
        if (pattern.isEmpty())
            throw new ParseException("Build rules empty.", 0);

        // 该数组将 Unicode 字符映射到其排序顺序
        mapping = new UCompactIntArray(RBCollationTables.UNMAPPED);
        // 规范化构建规则。找到所有分解字符的出现，并在将其输入构建器之前规范化规则。
        // 通过“规范化”，我们指的是所有预组合的 Unicode 字符必须转换为基础字符和一个或多个组合字符（如重音符号）。
        // 当有多个组合字符附加到基础字符时，组合字符必须按其规范顺序排列
        //
        // sherman/Note:
        //(1)decmp 仅在 ko 语言环境中为 NO_DECOMPOSITION，以防止将韩语音节分解为音素，因此我们实际上可以调用带有正常化器的 IGNORE_HANGUL 选项的分解
        //
        //(2)直接调用 NormalizerImpl 中的“特殊版本”
        //pattern = Normalizer.decompose(pattern, false, Normalizer.IGNORE_HANGUL, true);
        //
        //Normalizer.Mode mode = CollatorUtilities.toNormalizerMode(decmp);
        //pattern = Normalizer.normalize(pattern, mode, 0, true);

        pattern = NormalizerImpl.canonicalDecomposeWithSingleQuotation(pattern);

        // 构建合并的排序条目
        // 由于字符串中的规则可以以任何顺序指定
        // （例如 "c , C < d , D < e , E .... C < CH"）
        // 这将字符串中的所有规则拆分为单独的对象，然后对它们进行排序。在上述示例中，它将 "C < CH" 规则合并到 "C < D" 规则之前。
        //

        mPattern = new MergeCollation(pattern);

        int order = 0;

        // 现在遍历每个条目并将其添加到自己的表中
        for (int i = 0; i < mPattern.getCount(); ++i) {
            PatternEntry entry = mPattern.getItemAt(i);
            if (entry != null) {
                groupChars = entry.getChars();
                if (groupChars.length() > 1) {
                    switch(groupChars.charAt(groupChars.length()-1)) {
                    case '@':
                        frenchSec = true;
                        groupChars = groupChars.substring(0, groupChars.length()-1);
                        break;
                    case '!':
                        seAsianSwapping = true;
                        groupChars = groupChars.substring(0, groupChars.length()-1);
                        break;
                    }
                }

                order = increment(entry.getStrength(), order);
                expChars = entry.getExtension();

                if (!expChars.isEmpty()) {
                    addExpandOrder(groupChars, expChars, order);
                } else if (groupChars.length() > 1) {
                    char ch = groupChars.charAt(0);
                    if (Character.isHighSurrogate(ch) && groupChars.length() == 2) {
                        addOrder(Character.toCodePoint(ch, groupChars.charAt(1)), order);
                    } else {
                        addContractOrder(groupChars, order);
                    }
                } else {
                    char ch = groupChars.charAt(0);
                    addOrder(ch, order);
                }
            }
        }
        addComposedChars();

        commit();
        mapping.compact();
        /*
        System.out.println("mappingSize=" + mapping.getKSize());
        for (int j = 0; j < 0xffff; j++) {
            int value = mapping.elementAt(j);
            if (value != RBCollationTables.UNMAPPED)
                System.out.println("index=" + Integer.toString(j, 16)
                           + ", value=" + Integer.toString(value, 16));
        }
        */
        tables.fillInTables(frenchSec, seAsianSwapping, mapping, contractTable, expandTable,
                    contractFlags, maxSecOrder, maxTerOrder);
    }


                /** 为预组合的 Unicode 字符添加扩展条目，以便在关闭分解的情况下，这个排序器可以合理地使用。 */
    private void addComposedChars() throws ParseException {
        // 遍历 Unicode 中的所有预组合字符
        ComposedCharIter iter = new ComposedCharIter();
        int c;
        while ((c = iter.next()) != ComposedCharIter.DONE) {
            if (getCharOrder(c) == RBCollationTables.UNMAPPED) {
                //
                // 我们还没有为这个预组合字符设置排序。
                //
                // 首先，查看分解字符串是否已经在我们的表中作为一个单一的收缩字符串排序。
                // 如果是这样，只需将预组合字符映射到该排序。
                //
                // TODO: 我们真正应该做的是尝试找到分解的最长初始子字符串，该子字符串在表中作为一个收缩字符序列存在，并找到其排序。
                // 然后递归地处理剩余的字符，以便构建一个排序列表，并将该列表添加到扩展表中。
                // 这样做会更正确，但也会显著更慢，因此不确定是否值得这样做。
                //
                String s = iter.decomposition();

                // sherman/Note: 如果这是1个字符的分解字符串，唯一需要做的是检查这个分解字符
                // 在我们的排序表中是否有条目，这个排序不一定是收缩排序，如果它有条目，为预组合字符
                // 添加一个使用相同排序的条目，之前的实现不必要地添加了一个单字符扩展条目。
                if (s.length() == 1) {
                    int order = getCharOrder(s.charAt(0));
                    if (order != RBCollationTables.UNMAPPED) {
                        addOrder(c, order);
                    }
                    continue;
                } else if (s.length() == 2) {
                    char ch0 = s.charAt(0);
                    if (Character.isHighSurrogate(ch0)) {
                        int order = getCharOrder(s.codePointAt(0));
                        if (order != RBCollationTables.UNMAPPED) {
                            addOrder(c, order);
                        }
                        continue;
                    }
                }
                int contractOrder = getContractOrder(s);
                if (contractOrder != RBCollationTables.UNMAPPED) {
                    addOrder(c, contractOrder);
                } else {
                    //
                    // 我们没有为分解结果的整个字符串设置收缩排序，但如果每个单独的字符都有排序，
                    // 我们可以为预组合字符添加一个扩展表条目。
                    //
                    boolean allThere = true;
                    for (int i = 0; i < s.length(); i++) {
                        if (getCharOrder(s.charAt(i)) == RBCollationTables.UNMAPPED) {
                            allThere = false;
                            break;
                        }
                    }
                    if (allThere) {
                        addExpandOrder(c, s, RBCollationTables.UNMAPPED);
                    }
                }
            }
        }
    }

    /**
     * 在扩展字符表中查找未映射的值。
     *
     * 当通过 addExpandOrder 构建扩展字符表时，它不知道扩展中每个字符的最终排序。
     * 相反，它只是将原始字符代码放入表中，并添加 CHARINDEX 作为标志。现在我们已经完成了映射表的构建，
     * 我们可以回过头来查找该字符，查看其实际的排序顺序，并将其放入扩展表中。这让我们可以避免以后进行两阶段查找。
     */
    private final void commit()
    {
        if (expandTable != null) {
            for (int i = 0; i < expandTable.size(); i++) {
                int[] valueList = expandTable.elementAt(i);
                for (int j = 0; j < valueList.length; j++) {
                    int order = valueList[j];
                    if (order < RBCollationTables.EXPANDCHARINDEX && order > CHARINDEX) {
                        // 找到一个尚未填充的扩展字符
                        int ch = order - CHARINDEX;

                        // 获取非填充条目的实际值
                        int realValue = getCharOrder(ch);

                        if (realValue == RBCollationTables.UNMAPPED) {
                            // 实际值仍然是未映射的，可能是可忽略的
                            valueList[j] = IGNORABLEMASK & ch;
                        } else {
                            // 只需填充值
                            valueList[j] = realValue;
                        }
                    }
                }
            }
        }
    }
    /**
     * 根据比较级别递增最后一个排序值。
     */
    private final int increment(int aStrength, int lastValue)
    {
        switch(aStrength)
        {
        case Collator.PRIMARY:
            // 递增主要排序并屏蔽次要和三级差异
            lastValue += PRIMARYORDERINCREMENT;
            lastValue &= RBCollationTables.PRIMARYORDERMASK;
            isOverIgnore = true;
            break;
        case Collator.SECONDARY:
            // 递增次要排序并屏蔽三级差异
            lastValue += SECONDARYORDERINCREMENT;
            lastValue &= RBCollationTables.SECONDARYDIFFERENCEONLY;
            // 记录具有次要差异的最大可忽略字符数
            if (!isOverIgnore)
                maxSecOrder++;
            break;
        case Collator.TERTIARY:
            // 递增三级排序
            lastValue += TERTIARYORDERINCREMENT;
            // 记录具有三级差异的最大可忽略字符数
            if (!isOverIgnore)
                maxTerOrder++;
            break;
        }
        return lastValue;
    }

                /**
     * 将字符及其指定的顺序添加到排序表中。
     */
    private final void addOrder(int ch, int anOrder)
    {
        // 查看字符是否已经在映射表中具有顺序
        int order = mapping.elementAt(ch);

        if (order >= RBCollationTables.CONTRACTCHARINDEX) {
            // 该字符在映射表中已经有一个指向收缩字符表的条目。我们不直接将字符添加到映射表中，
            // 而是必须将其添加到收缩表中。
            int length = 1;
            if (Character.isSupplementaryCodePoint(ch)) {
                length = Character.toChars(ch, keyBuf, 0);
            } else {
                keyBuf[0] = (char)ch;
            }
            addContractOrder(new String(keyBuf, 0, length), anOrder);
        } else {
            // 将条目添加到映射表中，后面的条目会替换前面的条目
            mapping.setElementAt(ch, anOrder);
        }
    }

    private final void addContractOrder(String groupChars, int anOrder) {
        addContractOrder(groupChars, anOrder, true);
    }

    /**
     * 将收缩字符串添加到排序表中。
     */
    private final void addContractOrder(String groupChars, int anOrder,
                                          boolean fwd)
    {
        if (contractTable == null) {
            contractTable = new Vector<>(INITIALTABLESIZE);
        }

        // 初始字符
        int ch = groupChars.codePointAt(0);
        /*
        char ch0 = groupChars.charAt(0);
        int ch = Character.isHighSurrogate(ch0)?
          Character.toCodePoint(ch0, groupChars.charAt(1)):ch0;
          */
        // 查看字符串的初始字符是否已经有收缩表。
        int entry = mapping.elementAt(ch);
        Vector<EntryPair> entryTable = getContractValuesImpl(entry - RBCollationTables.CONTRACTCHARINDEX);

        if (entryTable == null) {
            // 需要为这个基础字符创建一个新的收缩条目表
            int tableIndex = RBCollationTables.CONTRACTCHARINDEX + contractTable.size();
            entryTable = new Vector<>(INITIALTABLESIZE);
            contractTable.addElement(entryTable);

            // 首先添加初始字符的当前排序，然后
            // 更新其映射以指向此收缩表
            entryTable.addElement(new EntryPair(groupChars.substring(0,Character.charCount(ch)), entry));
            mapping.setElementAt(ch, tableIndex);
        }

        // 现在在表中添加（或替换）此字符串
        int index = RBCollationTables.getEntry(entryTable, groupChars, fwd);
        if (index != RBCollationTables.UNMAPPED) {
            EntryPair pair = entryTable.elementAt(index);
            pair.value = anOrder;
        } else {
            EntryPair pair = entryTable.lastElement();

            // 注意：这段逻辑是为了加速 CollationElementIterator
            // .nextContractChar()。这段代码确保列表中最长的序列总是列表中的最后一个。
            // 这样，nextContractChar() 就不必在整个列表中搜索最长的序列。
            if (groupChars.length() > pair.entryName.length()) {
                entryTable.addElement(new EntryPair(groupChars, anOrder, fwd));
            } else {
                entryTable.insertElementAt(new EntryPair(groupChars, anOrder,
                        fwd), entryTable.size() - 1);
            }
        }

        // 如果这是收缩字符串的正向映射，还应为其添加一个反向映射，
        // 以便 CollationElementIterator.previous 能正常工作
        if (fwd && groupChars.length() > 1) {
            addContractFlags(groupChars);
            addContractOrder(new StringBuffer(groupChars).reverse().toString(),
                             anOrder, false);
        }
    }

    /**
     * 如果给定的字符串在排序表中被指定为收缩字符串，返回其排序。
     * 否则返回 UNMAPPED。
     */
    private int getContractOrder(String groupChars)
    {
        int result = RBCollationTables.UNMAPPED;
        if (contractTable != null) {
            int ch = groupChars.codePointAt(0);
            /*
            char ch0 = groupChars.charAt(0);
            int ch = Character.isHighSurrogate(ch0)?
              Character.toCodePoint(ch0, groupChars.charAt(1)):ch0;
              */
            Vector<EntryPair> entryTable = getContractValues(ch);
            if (entryTable != null) {
                int index = RBCollationTables.getEntry(entryTable, groupChars, true);
                if (index != RBCollationTables.UNMAPPED) {
                    EntryPair pair = entryTable.elementAt(index);
                    result = pair.value;
                }
            }
        }
        return result;
    }

    private final int getCharOrder(int ch) {
        int order = mapping.elementAt(ch);

        if (order >= RBCollationTables.CONTRACTCHARINDEX) {
            Vector<EntryPair> groupList = getContractValuesImpl(order - RBCollationTables.CONTRACTCHARINDEX);
            EntryPair pair = groupList.firstElement();
            order = pair.value;
        }
        return order;
    }

    /**
     * 获取排序表中收缩字符串的哈希表条目。
     * @param ch 收缩字符串的起始字符
     */
    private Vector<EntryPair> getContractValues(int ch)
    {
        int index = mapping.elementAt(ch);
        return getContractValuesImpl(index - RBCollationTables.CONTRACTCHARINDEX);
    }

    private Vector<EntryPair> getContractValuesImpl(int index)
    {
        if (index >= 0)
        {
            return contractTable.elementAt(index);
        }
        else // 未找到
        {
            return null;
        }
    }

    /**
     * 将扩展字符串添加到排序表中。
     */
    private final void addExpandOrder(String contractChars,
                                String expandChars,
                                int anOrder) throws ParseException
    {
        // 创建一个扩展表条目
        int tableIndex = addExpansion(anOrder, expandChars);


                    // 并将其索引添加到主映射表中
        if (contractChars.length() > 1) {
            char ch = contractChars.charAt(0);
            if (Character.isHighSurrogate(ch) && contractChars.length() == 2) {
                char ch2 = contractChars.charAt(1);
                if (Character.isLowSurrogate(ch2)) {
                    // 仅在它是合法的代理对时才添加到表中
                    addOrder(Character.toCodePoint(ch, ch2), tableIndex);
                }
            } else {
                addContractOrder(contractChars, tableIndex);
            }
        } else {
            addOrder(contractChars.charAt(0), tableIndex);
        }
    }

    private final void addExpandOrder(int ch, String expandChars, int anOrder)
      throws ParseException
    {
        int tableIndex = addExpansion(anOrder, expandChars);
        addOrder(ch, tableIndex);
    }

    /**
     * 在扩展表中创建一个新条目，包含给定字符的排序。
     * 如果 anOrder 有效，它将被添加到扩展排序列表的开头。
     */
    private int addExpansion(int anOrder, String expandChars) {
        if (expandTable == null) {
            expandTable = new Vector<>(INITIALTABLESIZE);
        }

        // 如果 anOrder 有效，我们希望将其添加到列表的开头
        int offset = (anOrder == RBCollationTables.UNMAPPED) ? 0 : 1;

        int[] valueList = new int[expandChars.length() + offset];
        if (offset == 1) {
            valueList[0] = anOrder;
        }

        int j = offset;
        for (int i = 0; i < expandChars.length(); i++) {
            char ch0 = expandChars.charAt(i);
            char ch1;
            int ch;
            if (Character.isHighSurrogate(ch0)) {
                if (++i == expandChars.length() ||
                    !Character.isLowSurrogate(ch1=expandChars.charAt(i))) {
                    // 要么缺少低代理，要么下一个字符不是合法的低代理，因此停止循环
                    break;
                }
                ch = Character.toCodePoint(ch0, ch1);

            } else {
                ch = ch0;
            }

            int mapValue = getCharOrder(ch);

            if (mapValue != RBCollationTables.UNMAPPED) {
                valueList[j++] = mapValue;
            } else {
                // 无法在表中找到，将在 commit() 中填充
                valueList[j++] = CHARINDEX + ch;
            }
        }
        if (j < valueList.length) {
            // 至少有一个补充字符，valueList 的大小比实际需要的大
            int[] tmpBuf = new int[j];
            while (--j >= 0) {
                tmpBuf[j] = valueList[j];
            }
            valueList = tmpBuf;
        }
        // 将扩展字符列表添加到扩展表中
        int tableIndex = RBCollationTables.EXPANDCHARINDEX + expandTable.size();
        expandTable.addElement(valueList);

        return tableIndex;
    }

    private void addContractFlags(String chars) {
        char c0;
        int c;
        int len = chars.length();
        for (int i = 0; i < len; i++) {
            c0 = chars.charAt(i);
            c = Character.isHighSurrogate(c0)
                          ?Character.toCodePoint(c0, chars.charAt(++i))
                          :c0;
            contractFlags.put(c, 1);
        }
    }

    // ==============================================================
    // 常量
    // ==============================================================
    final static int CHARINDEX = 0x70000000;  // 需要在 .commit() 中查找

    private final static int IGNORABLEMASK = 0x0000ffff;
    private final static int PRIMARYORDERINCREMENT = 0x00010000;
    private final static int SECONDARYORDERINCREMENT = 0x00000100;
    private final static int TERTIARYORDERINCREMENT = 0x00000001;
    private final static int INITIALTABLESIZE = 20;
    private final static int MAXKEYSIZE = 5;

    // ==============================================================
    // 实例变量
    // ==============================================================

    // 构建过程中使用的变量
    private RBCollationTables.BuildAPI tables = null;
    private MergeCollation mPattern = null;
    private boolean isOverIgnore = false;
    private char[] keyBuf = new char[MAXKEYSIZE];
    private IntHashtable contractFlags = new IntHashtable(100);

    // RBCollationTables 中实例变量的“影子”副本
    // （这些变量中的值将在构建过程结束时复制回 RBCollationTables）
    private boolean frenchSec = false;
    private boolean seAsianSwapping = false;

    private UCompactIntArray mapping = null;
    private Vector<Vector<EntryPair>>   contractTable = null;
    private Vector<int[]>   expandTable = null;

    private short maxSecOrder = 0;
    private short maxTerOrder = 0;
}
