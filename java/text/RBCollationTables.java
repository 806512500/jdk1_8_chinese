/*
 * Copyright (c) 1999, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Vector;
import sun.text.UCompactIntArray;
import sun.text.IntHashtable;

/**
 * 此类包含 RuleBasedCollator 的静态状态：用于排序例程的各种表。多个 RuleBasedCollator 可以共享一个 RBCollationTables 对象，从而减少内存需求并提高性能。
 */
final class RBCollationTables {
    //===========================================================================================
    //  以下图显示了 RBCollationTables 对象的数据结构。
    //  假设我们有以下规则，其中 'o-umlaut' 是 Unicode 字符 0x00F6。
    //  "a, A < b, B < c, C, ch, cH, Ch, CH < d, D ... < o, O; 'o-umlaut'/E, 'O-umlaut'/E ...".
    //  该规则表示，'ch' 连字和 'c' 仅在三级差异上进行排序，
    //  并且 'o-umlaut' 始终被视为扩展为 'e'。
    //
    // 映射表                     合并列表           扩展列表
    // (包含所有 Unicode 字符
    //  条目)                   ___    ____________       _________________________
    //  ________                +>|_*_|->|'c' |v('c') |  +>|v('o')|v('umlaut')|v('e')|
    // |_\u0001_|-> v('\u0001') | |_:_|  |------------|  | |-------------------------|
    // |_\u0002_|-> v('\u0002') | |_:_|  |'ch'|v('ch')|  | |             :           |
    // |____:___|               | |_:_|  |------------|  | |-------------------------|
    // |____:___|               |        |'cH'|v('cH')|  | |             :           |
    // |__'a'___|-> v('a')      |        |------------|  | |-------------------------|
    // |__'b'___|-> v('b')      |        |'Ch'|v('Ch')|  | |             :           |
    // |____:___|               |        |------------|  | |-------------------------|
    // |____:___|               |        |'CH'|v('CH')|  | |             :           |
    // |___'c'__|----------------         ------------   | |-------------------------|
    // |____:___|                                        | |             :           |
    // |o-umlaut|----------------------------------------  |_________________________|
    // |____:___|
    //
    // 由 Helena Shih 于 6/23/97 注记
    //============================================================================================

    public RBCollationTables(String rules, int decmp) throws ParseException {
        this.rules = rules;

        RBTableBuilder builder = new RBTableBuilder(new BuildAPI());
        builder.build(rules, decmp); // 通过 BuildAPI 对象填充此对象
    }

    final class BuildAPI {
        /**
         * 私有构造函数。防止除 RBTableBuilder 之外的任何人直接访问此类的内部。
         */
        private BuildAPI() {
        }

        /**
         * 此函数用于由 RBTableBuilder 填充此对象的所有成员。实际上，构建器类充当此类的“朋友”，但为了避免改变太多逻辑，它携带所有这些变量的“影子”副本，直到构建过程结束，然后将它们一次性复制到实际的表对象中。此函数执行“一次性复制”。
         * @param f2ary 法国二级标志的值
         * @param swap 东南亚交换规则的值
         * @param map 排序器的字符映射表
         * @param cTbl 排序器的合并字符表
         * @param eTbl 排序器的扩展字符表
         * @param cFlgs 参与合并字符序列的字符的哈希表
         * @param mso 最大二级顺序的值
         * @param mto 最大三级顺序的值
         */
        void fillInTables(boolean f2ary,
                          boolean swap,
                          UCompactIntArray map,
                          Vector<Vector<EntryPair>> cTbl,
                          Vector<int[]> eTbl,
                          IntHashtable cFlgs,
                          short mso,
                          short mto) {
            frenchSec = f2ary;
            seAsianSwapping = swap;
            mapping = map;
            contractTable = cTbl;
            expandTable = eTbl;
            contractFlags = cFlgs;
            maxSecOrder = mso;
            maxTerOrder = mto;
        }
    }

    /**
     * 获取排序对象的基于表的规则。
     * @return 返回用于创建表排序对象的排序规则。
     */
    public String getRules()
    {
        return rules;
    }

    public boolean isFrenchSec() {
        return frenchSec;
    }

    public boolean isSEAsianSwapping() {
        return seAsianSwapping;
    }

    // ==============================================================
    // 内部（供 CollationElementIterator 使用）
    // ==============================================================

    /**
     * 获取排序表中合并字符串的哈希表条目。
     * @param ch 合并字符串的起始字符
     */
    Vector<EntryPair> getContractValues(int ch)
    {
        int index = mapping.elementAt(ch);
        return getContractValuesImpl(index - CONTRACTCHARINDEX);
    }

    // 通过索引从 contractTable 获取合并值
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
     * 如果此字符出现在任何合并字符序列中，则返回 true。 (由 CollationElementIterator.setOffset() 使用。)
     */
    boolean usedInContractSeq(int c) {
        return contractFlags.get(c) == 1;
    }

    /**
      * 返回以指定比较顺序结束的任何扩展序列的最大长度。
      *
      * @param order 由 previous 或 next 返回的排序顺序。
      * @return 以指定顺序结束的任何扩展序列的最大长度。
      *
      * @see CollationElementIterator#getMaxExpansion
      */
    int getMaxExpansion(int order) {
        int result = 1;

        if (expandTable != null) {
            // 目前这会对整个扩展表进行线性搜索。如果排序器有大量扩展，这可能会导致性能问题，但实际上这种情况很少发生
            for (int i = 0; i < expandTable.size(); i++) {
                int[] valueList = expandTable.elementAt(i);
                int length = valueList.length;

                if (length > result && valueList[length-1] == order) {
                    result = length;
                }
            }
        }

        return result;
    }

    /**
     * 获取排序表中扩展字符串的哈希表条目。
     * @param idx 扩展字符串值列表的索引
     */
    final int[] getExpandValueList(int idx) {
        return expandTable.elementAt(idx - EXPANDCHARINDEX);
    }

    /**
     * 从排序表中获取字符的比较顺序。
     * @return 字符的比较顺序。
     */
    int getUnicodeOrder(int ch) {
        return mapping.elementAt(ch);
    }

    short getMaxSecOrder() {
        return maxSecOrder;
    }

    short getMaxTerOrder() {
        return maxTerOrder;
    }

    /**
     * 反转字符串。
     */
    //shemran/Note: 用于二级顺序值反转，无需考虑补充对。
    static void reverse (StringBuffer result, int from, int to)
    {
        int i = from;
        char swap;

        int j = to - 1;
        while (i < j) {
            swap =  result.charAt(i);
            result.setCharAt(i, result.charAt(j));
            result.setCharAt(j, swap);
            i++;
            j--;
        }
    }

    final static int getEntry(Vector<EntryPair> list, String name, boolean fwd) {
        for (int i = 0; i < list.size(); i++) {
            EntryPair pair = list.elementAt(i);
            if (pair.fwd == fwd && pair.entryName.equals(name)) {
                return i;
            }
        }
        return UNMAPPED;
    }

    // ==============================================================
    // 常量
    // ==============================================================
    //sherman/Todo: 值是否足够大?????
    final static int EXPANDCHARINDEX = 0x7E000000; // 扩展索引跟随
    final static int CONTRACTCHARINDEX = 0x7F000000;  // 合并索引跟随
    final static int UNMAPPED = 0xFFFFFFFF;

    final static int PRIMARYORDERMASK = 0xffff0000;
    final static int SECONDARYORDERMASK = 0x0000ff00;
    final static int TERTIARYORDERMASK = 0x000000ff;
    final static int PRIMARYDIFFERENCEONLY = 0xffff0000;
    final static int SECONDARYDIFFERENCEONLY = 0xffffff00;
    final static int PRIMARYORDERSHIFT = 16;
    final static int SECONDARYORDERSHIFT = 8;

    // ==============================================================
    // 实例变量
    // ==============================================================
    private String rules = null;
    private boolean frenchSec = false;
    private boolean seAsianSwapping = false;

    private UCompactIntArray mapping = null;
    private Vector<Vector<EntryPair>> contractTable = null;
    private Vector<int[]> expandTable = null;
    private IntHashtable contractFlags = null;

    private short maxSecOrder = 0;
    private short maxTerOrder = 0;
}
