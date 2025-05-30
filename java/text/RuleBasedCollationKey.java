/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright Taligent, Inc. 1996 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - All Rights Reserved
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

/**
 * RuleBasedCollationKey 是 CollationKey 类的一个具体实现。
 * RuleBasedCollationKey 类由 RuleBasedCollator 类使用。
 */

final class RuleBasedCollationKey extends CollationKey {
    /**
     * 比较此 RuleBasedCollationKey 和目标。应用创建这些键的 Collator 对象的排序规则。
     * <strong>注意：</strong> 由不同 Collator 创建的 RuleBasedCollationKeys 不能进行比较。
     * @param target 目标 RuleBasedCollationKey
     * @return 返回一个整数值。如果此对象小于目标，则值小于零；如果此对象和目标相等，则值为零；如果此对象大于目标，则值大于零。
     * @see java.text.Collator#compare
     */
    public int compareTo(CollationKey target)
    {
        int result = key.compareTo(((RuleBasedCollationKey)(target)).key);
        if (result <= Collator.LESS)
            return Collator.LESS;
        else if (result >= Collator.GREATER)
            return Collator.GREATER;
        return Collator.EQUAL;
    }

    /**
     * 比较此 RuleBasedCollationKey 和目标是否相等。
     * 应用创建这些键的 Collator 对象的排序规则。
     * <strong>注意：</strong> 由不同 Collator 创建的 RuleBasedCollationKeys 不能进行比较。
     * @param target 要比较的 RuleBasedCollationKey。
     * @return 如果两个对象相等，则返回 true，否则返回 false。
     */
    public boolean equals(Object target) {
        if (this == target) return true;
        if (target == null || !getClass().equals(target.getClass())) {
            return false;
        }
        RuleBasedCollationKey other = (RuleBasedCollationKey)target;
        return key.equals(other.key);
    }

    /**
     * 为这个 RuleBasedCollationKey 创建一个哈希码。哈希值是基于键本身计算的，而不是创建键的字符串。
     * 因此，如果 x 和 y 是 RuleBasedCollationKeys，则 x.hashCode(x) == y.hashCode() 如果 x.equals(y) 为 true。
     * 这允许在哈希表中进行语言敏感的比较。有关示例，请参阅 CollationKey 类描述。
     * @return 基于字符串的排序顺序的哈希值。
     */
    public int hashCode() {
        return (key.hashCode());
    }

    /**
     * 将 RuleBasedCollationKey 转换为一系列位。如果两个 RuleBasedCollationKeys 可以合法地进行比较，
     * 则可以比较每个键的字节数组以获得相同的结果。字节数组按最高有效字节优先组织。
     */
    public byte[] toByteArray() {

        char[] src = key.toCharArray();
        byte[] dest = new byte[ 2*src.length ];
        int j = 0;
        for( int i=0; i<src.length; i++ ) {
            dest[j++] = (byte)(src[i] >>> 8);
            dest[j++] = (byte)(src[i] & 0x00ff);
        }
        return dest;
    }

    /**
     * RuleBasedCollationKey 只能由 Collator 对象生成。
     */
    RuleBasedCollationKey(String source, String key) {
        super(source);
        this.key = key;
    }
    private String key = null;

}
