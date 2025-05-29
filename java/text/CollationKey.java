/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 一个 <code>CollationKey</code> 表示一个 <code>String</code>，遵循特定的 <code>Collator</code> 对象的规则。比较两个
 * <code>CollationKey</code> 返回它们所代表的 <code>String</code> 的相对顺序。使用 <code>CollationKey</code> 比较
 * <code>String</code> 通常比使用 <code>Collator.compare</code> 更快。因此，当需要多次比较 <code>String</code> 时，例如对
 * <code>String</code> 列表进行排序时，使用 <code>CollationKey</code> 更高效。
 *
 * <p>
 * 你不能直接创建 <code>CollationKey</code>。而是通过调用 <code>Collator.getCollationKey</code> 生成它们。你只能比较由
 * 同一个 <code>Collator</code> 对象生成的 <code>CollationKey</code>。
 *
 * <p>
 * 为一个 <code>String</code> 生成一个 <code>CollationKey</code> 涉及检查整个 <code>String</code>
 * 并将其转换为可以进行位比较的一系列位。这允许在生成键后进行快速比较。生成键的成本在需要多次比较 <code>String</code> 时通过更快的比较得到补偿。另一方面，比较的结果通常由每个 <code>String</code> 的前几个字符决定。
 * <code>Collator.compare</code> 只检查它需要的字符，这使得在进行单次比较时它更快。
 * <p>
 * 以下示例显示了如何使用 <code>CollationKey</code> 对 <code>String</code> 列表进行排序。
 * <blockquote>
 * <pre>{@code
 * // 为要排序的字符串创建一个 CollationKeys 数组。
 * Collator myCollator = Collator.getInstance();
 * CollationKey[] keys = new CollationKey[3];
 * keys[0] = myCollator.getCollationKey("Tom");
 * keys[1] = myCollator.getCollationKey("Dick");
 * keys[2] = myCollator.getCollationKey("Harry");
 * sort(keys);
 *
 * //...
 *
 * // 在排序例程的主体中，以这种方式比较键
 * if (keys[i].compareTo(keys[j]) > 0)
 *    // 交换 keys[i] 和 keys[j]
 *
 * //...
 *
 * // 最后，当我们从排序返回时。
 * System.out.println(keys[0].getSourceString());
 * System.out.println(keys[1].getSourceString());
 * System.out.println(keys[2].getSourceString());
 * }</pre>
 * </blockquote>
 *
 * @see          Collator
 * @see          RuleBasedCollator
 * @author       Helena Shih
 */

public abstract class CollationKey implements Comparable<CollationKey> {
    /**
     * 将此 CollationKey 与目标 CollationKey 进行比较。应用创建这些键的 Collator 对象的排序规则。<strong>注意：</strong>
     * 由不同 Collator 创建的 CollationKeys 不能进行比较。
     * @param target 目标 CollationKey
     * @return 返回一个整数值。如果此键小于目标，则值小于零；如果此键和目标相等，则值为零；如果此键大于目标，则值大于零。
     * @see java.text.Collator#compare
     */
    abstract public int compareTo(CollationKey target);

    /**
     * 返回此 CollationKey 表示的字符串。
     *
     * @return 此 CollationKey 的源字符串
     */
    public String getSourceString() {
        return source;
    }


    /**
     * 将 CollationKey 转换为一系列位。如果两个 CollationKeys 可以合法地进行比较，那么可以比较每个键的字节数组以获得相同的结果。字节数组按最高有效字节优先组织。
     *
     * @return CollationKey 的字节数组表示
     */
    abstract public byte[] toByteArray();


  /**
   * CollationKey 构造函数。
   *
   * @param source 源字符串
   * @exception NullPointerException 如果 {@code source} 为 null
   * @since 1.6
   */
    protected CollationKey(String source) {
        if (source==null){
            throw new NullPointerException();
        }
        this.source = source;
    }

    final private String source;
}
