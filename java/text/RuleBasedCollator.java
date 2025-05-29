
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

import java.text.Normalizer;
import java.util.Vector;
import java.util.Locale;

/**
 * <code>RuleBasedCollator</code> 类是 <code>Collator</code> 的一个具体子类，提供了一个简单、数据驱动的表排序器。
 * 通过这个类，您可以创建一个自定义的表基 <code>Collator</code>。 <code>RuleBasedCollator</code> 将字符映射到排序键。
 *
 * <p>
 * <code>RuleBasedCollator</code> 为了效率有以下限制（其他子类可用于更复杂的语言）：
 * <ol>
 * <li>如果指定了由 &lt;modifier&gt; 控制的特殊排序规则，则该规则适用于整个排序器对象。
 * <li>所有未提及的字符都在排序顺序的末尾。
 * </ol>
 *
 * <p>
 * 排序表由一系列排序规则组成，每个规则有以下三种形式之一：
 * <pre>
 *    &lt;modifier&gt;
 *    &lt;relation&gt; &lt;text-argument&gt;
 *    &lt;reset&gt; &lt;text-argument&gt;
 * </pre>
 * 规则元素的定义如下：
 * <UL>
 *    <LI><strong>Text-Argument</strong>: 文本参数是任何字符序列，不包括特殊字符（即，常见的空白字符 [0009-000D, 0020] 和规则语法字符
 *        [0021-002F, 003A-0040, 005B-0060, 007B-007E]）。如果需要这些字符，可以将它们放在单引号中
 *        （例如，&amp; 符号 =&gt; '&amp;'）。注意未加引号的空白字符将被忽略；例如，<code>b c</code> 被视为 <code>bc</code>。
 *    <LI><strong>Modifier</strong>: 目前有两个修饰符可以开启特殊的排序规则。
 *        <UL>
 *            <LI>'@' : 开启重音的倒序排序（次要差异），如法语。
 *            <LI>'!' : 开启泰语/老挝语元音-辅音交换。如果此规则生效，当泰语元音范围
 *                      &#92;U0E40-&#92;U0E44 位于泰语辅音范围 &#92;U0E01-&#92;U0E2E 之前，或老挝语元音范围 &#92;U0EC0-&#92;U0EC4
 *                      位于老挝语辅音范围 &#92;U0E81-&#92;U0EAE 之前，则元音在排序时会被置于辅音之后。
 *        </UL>
 *        <p>'@' : 表示重音按倒序排序，如法语。
 *    <LI><strong>Relation</strong>: 关系如下：
 *        <UL>
 *            <LI>'&lt;' : 更大，如同字母差异（主要）
 *            <LI>';' : 更大，如同重音差异（次要）
 *            <LI>',' : 更大，如同大小写差异（三级）
 *            <LI>'=' : 相等
 *        </UL>
 *    <LI><strong>Reset</strong>: 有一个重置主要用于收缩和扩展，但也可以用于在规则集末尾添加修改。
 *        <p>'&amp;' : 表示下一个规则将位于重置文本参数应排序的位置。
 * </UL>
 *
 * <p>
 * 这听起来比实际应用中更复杂。例如，以下几种表达方式是等效的：
 * <blockquote>
 * <pre>
 * a &lt; b &lt; c
 * a &lt; b &amp; b &lt; c
 * a &lt; c &amp; a &lt; b
 * </pre>
 * </blockquote>
 * 注意顺序很重要，因为后续项紧接在文本参数之后。以下几种表达方式是不等效的：
 * <blockquote>
 * <pre>
 * a &lt; b &amp; a &lt; c
 * a &lt; c &amp; a &lt; b
 * </pre>
 * </blockquote>
 * 要么文本参数必须已经存在于序列中，要么文本参数的某个初始子串必须已经存在于序列中。（例如，“a &lt; b &amp; ae &lt; e” 是有效的，因为“a”在“ae”重置之前已经存在于序列中。）在这种情况下，“ae”不会被视为单个字符；相反，“e”会被排序为两个字符：“a”后面跟着一个“e”。这种差异出现在自然语言中：在传统的西班牙语中，“ch”被视为一个收缩的单个字符（表达为“c &lt; ch &lt; d”），而在传统的德语中，a-umlaut 被视为扩展为两个字符（表达为“a,A &lt; b,B ... &amp;ae;&#92;u00e3&amp;AE;&#92;u00c3”）。
 * [&#92;u00e3 和 &#92;u00c3 当然是 a-umlaut 的转义序列。]
 * <p>
 * <strong>可忽略字符</strong>
 * <p>
 * 对于可忽略字符，第一个规则必须以关系开始（我们上面使用的例子实际上是片段；“a &lt; b” 实际上应该是 “&lt; a &lt; b”）。然而，如果第一个关系不是 “&lt;”，那么所有文本参数直到第一个 “&lt;” 都是可忽略的。例如，“, - &lt; a &lt; b” 使 “-” 成为可忽略字符，如我们之前在单词 “black-birds” 中看到的那样。在不同语言的示例中，大多数重音都是可忽略的。
 *
 * <p><strong>规范化和重音</strong>
 * <p>
 * <code>RuleBasedCollator</code> 自动处理其规则表，以包括预组合和组合字符版本的带重音字符。即使提供的规则字符串中只包含基本字符和单独的组合重音字符，所有规范组合的预组合带重音字符都将被输入到表中。
 * <p>
 * 这允许您使用 <code>RuleBasedCollator</code> 比较带重音的字符串，即使排序器设置为 NO_DECOMPOSITION。然而，有两个注意事项。首先，如果要排序的字符串包含可能未按规范顺序的组合序列，您应将排序器设置为 CANONICAL_DECOMPOSITION 或 FULL_DECOMPOSITION 以启用组合序列的排序。其次，如果字符串包含具有兼容性分解的字符（如全角和半角形式），您必须使用 FULL_DECOMPOSITION，因为规则表中只包含规范映射。
 *
 * <p><strong>错误</strong>
 * <p>
 * 以下情况是错误的：
 * <UL>
 *     <LI>文本参数包含未加引号的标点符号
 *        （例如，“a &lt; b-c &lt; d”）。
 *     <LI>关系或重置字符后没有文本参数
 *        （例如，“a &lt; ,b”）。
 *     <LI>重置时，文本参数（或文本参数的初始子串）不在序列中。
 *         （例如，“a &lt; b &amp; e &lt; f”）。
 * </UL>
 * 如果您产生这些错误之一，<code>RuleBasedCollator</code> 将抛出 <code>ParseException</code>。
 *
 * <p><strong>示例</strong>
 * <p>简单：     "&lt; a &lt; b &lt; c &lt; d"
 * <p>挪威语：  "&lt; a, A &lt; b, B &lt; c, C &lt; d, D &lt; e, E &lt; f, F
 *                 &lt; g, G &lt; h, H &lt; i, I &lt; j, J &lt; k, K &lt; l, L
 *                 &lt; m, M &lt; n, N &lt; o, O &lt; p, P &lt; q, Q &lt; r, R
 *                 &lt; s, S &lt; t, T &lt; u, U &lt; v, V &lt; w, W &lt; x, X
 *                 &lt; y, Y &lt; z, Z
 *                 &lt; &#92;u00E6, &#92;u00C6
 *                 &lt; &#92;u00F8, &#92;u00D8
 *                 &lt; &#92;u00E5 = a&#92;u030A, &#92;u00C5 = A&#92;u030A;
 *                      aa, AA"
 *
 * <p>
 * 要创建一个具有特定规则的 <code>RuleBasedCollator</code> 对象，您可以使用包含规则的 <code>String</code> 对象构造 <code>RuleBasedCollator</code>。例如：
 * <blockquote>
 * <pre>
 * String simple = "&lt; a&lt; b&lt; c&lt; d";
 * RuleBasedCollator mySimple = new RuleBasedCollator(simple);
 * </pre>
 * </blockquote>
 * 或者：
 * <blockquote>
 * <pre>
 * String Norwegian = "&lt; a, A &lt; b, B &lt; c, C &lt; d, D &lt; e, E &lt; f, F &lt; g, G &lt; h, H &lt; i, I" +
 *                    "&lt; j, J &lt; k, K &lt; l, L &lt; m, M &lt; n, N &lt; o, O &lt; p, P &lt; q, Q &lt; r, R" +
 *                    "&lt; s, S &lt; t, T &lt; u, U &lt; v, V &lt; w, W &lt; x, X &lt; y, Y &lt; z, Z" +
 *                    "&lt; &#92;u00E6, &#92;u00C6" +     // 拉丁字母 ae &amp; AE
 *                    "&lt; &#92;u00F8, &#92;u00D8" +     // 拉丁字母 o &amp; O 带斜杠
 *                    "&lt; &#92;u00E5 = a&#92;u030A," +  // 拉丁字母 a 带上方圆点
 *                    "  &#92;u00C5 = A&#92;u030A;" +  // 拉丁字母 A 带上方圆点
 *                    "  aa, AA";
 * RuleBasedCollator myNorwegian = new RuleBasedCollator(Norwegian);
 * </pre>
 * </blockquote>
 *
 * <p>
 * 可以通过连接规则字符串来创建新的排序规则字符串。例如，由 {@link #getRules()} 返回的规则可以连接起来以组合多个 <code>RuleBasedCollator</code>。
 *
 * <p>
 * 以下示例演示了如何更改非间距重音的顺序：
 * <blockquote>
 * <pre>
 * // 旧规则
 * String oldRules = "=&#92;u0301;&#92;u0300;&#92;u0302;&#92;u0308"    // 主要重音
 *                 + ";&#92;u0327;&#92;u0303;&#92;u0304;&#92;u0305"    // 主要重音
 *                 + ";&#92;u0306;&#92;u0307;&#92;u0309;&#92;u030A"    // 主要重音
 *                 + ";&#92;u030B;&#92;u030C;&#92;u030D;&#92;u030E"    // 主要重音
 *                 + ";&#92;u030F;&#92;u0310;&#92;u0311;&#92;u0312"    // 主要重音
 *                 + "&lt; a , A ; ae, AE ; &#92;u00e6 , &#92;u00c6"
 *                 + "&lt; b , B &lt; c, C &lt; e, E &amp; C &lt; d, D";
 * // 更改重音字符的顺序
 * String addOn = "&amp; &#92;u0300 ; &#92;u0308 ; &#92;u0302";
 * RuleBasedCollator myCollator = new RuleBasedCollator(oldRules + addOn);
 * </pre>
 * </blockquote>
 *
 * @see        Collator
 * @see        CollationElementIterator
 * @author     Helena Shih, Laura Werner, Richard Gillam
 */
public class RuleBasedCollator extends Collator{
    // IMPLEMENTATION NOTES:  排序算法的实现分布在三个类中：RuleBasedCollator、RBCollationTables 和 CollationElementIterator。
    // RuleBasedCollator 包含排序器的瞬态状态，并包括使用其他类实现比较和排序键构建的代码。RuleBasedCollator 还包含处理法语次要重音排序的逻辑。
    // RuleBasedCollator 有两个 CollationElementIterator。这些对象在调用 compare() 或 getCollationKey() 之间不需要保留状态，但为了节省额外的创建时间，它们仍然会持久化。
    // compare() 和 getCollationKey() 是同步的，以确保使用这种方案的线程安全。CollationElementIterator 负责从字符串生成排序元素并一次返回一个元素
    // （有时字符和排序元素之间存在一对一或一对多的映射——这个类处理这种情况）。
    // CollationElementIterator 依赖于 RBCollationTables，后者包含排序器的静态状态。RBCollationTables 包含指定特定语言或用途的字符排序顺序的实际数据表。
    // 它还包含 CollationElementIterator 用于从字符映射到排序元素的基本逻辑。单个 RBCollationTables 对象在相同语言的所有 RuleBasedCollators 之间共享，
    // 以及它们创建的所有 CollationElementIterators。


                /**
     * RuleBasedCollator 构造函数。此函数接受表格规则并从中构建排序表。
     * 有关排序规则语法的更多详细信息，请参阅 RuleBasedCollator 类描述。
     * @see java.util.Locale
     * @param rules 用于构建排序表的排序规则。
     * @exception ParseException 如果规则构建过程失败，将抛出格式异常。
     * 例如，构建规则 "a < ? < d" 将导致构造函数抛出 ParseException，因为 '?' 未被引用。
     */
    public RuleBasedCollator(String rules) throws ParseException {
        this(rules, Collator.CANONICAL_DECOMPOSITION);
    }

    /**
     * RuleBasedCollator 构造函数。此函数接受表格规则并从中构建排序表。
     * 有关排序规则语法的更多详细信息，请参阅 RuleBasedCollator 类描述。
     * @see java.util.Locale
     * @param rules 用于构建排序表的排序规则。
     * @param decomp 用于构建排序表和执行比较的分解强度。
     * @exception ParseException 如果规则构建过程失败，将抛出格式异常。
     * 例如，构建规则 "a < ? < d" 将导致构造函数抛出 ParseException，因为 '?' 未被引用。
     */
    RuleBasedCollator(String rules, int decomp) throws ParseException {
        setStrength(Collator.TERTIARY);
        setDecomposition(decomp);
        tables = new RBCollationTables(rules, decomp);
    }

    /**
     * “复制构造函数。” 用于 clone() 以提高性能。
     */
    private RuleBasedCollator(RuleBasedCollator that) {
        setStrength(that.getStrength());
        setDecomposition(that.getDecomposition());
        tables = that.tables;
    }

    /**
     * 获取排序对象的基于表的规则。
     * @return 返回用于创建表排序对象的排序规则。
     */
    public String getRules()
    {
        return tables.getRules();
    }

    /**
     * 返回给定字符串的 CollationElementIterator。
     *
     * @param source 要排序的字符串
     * @return 一个 {@code CollationElementIterator} 对象
     * @see java.text.CollationElementIterator
     */
    public CollationElementIterator getCollationElementIterator(String source) {
        return new CollationElementIterator( source, this );
    }

    /**
     * 返回给定 CharacterIterator 的 CollationElementIterator。
     *
     * @param source 要排序的字符迭代器
     * @return 一个 {@code CollationElementIterator} 对象
     * @see java.text.CollationElementIterator
     * @since 1.2
     */
    public CollationElementIterator getCollationElementIterator(
                                                CharacterIterator source) {
        return new CollationElementIterator( source, this );
    }

    /**
     * 根据排序规则比较存储在两个不同字符串中的字符数据。返回关于一个字符串是否小于、大于或等于另一个字符串的信息。
     * 这可以在子类中被重写。
     *
     * @exception NullPointerException 如果 <code>source</code> 或 <code>target</code> 为 null。
     */
    public synchronized int compare(String source, String target)
    {
        if (source == null || target == null) {
            throw new NullPointerException();
        }

        // 这里的基本算法是使用 CollationElementIterators 遍历源字符串和目标字符串。我们比较源字符串中的每个排序元素
        // 与目标字符串中对应的排序元素，检查差异。
        //
        // 如果发现差异，我们将 <result> 设置为 LESS 或 GREATER，以指示源字符串是否小于或大于目标字符串。
        //
        // 但是，这并不那么简单。如果在字符串的开头附近发现三级差异（例如 'A' vs. 'a'），它可能会被字符串后面的一级差异（例如 "A" vs. "B"）覆盖。
        // 例如，"AA" < "aB"，即使 'A' > 'a'。
        //
        // 为了跟踪这一点，我们使用 strengthResult 来跟踪迄今为止发现的最重要的差异的强度。当我们发现一个强度大于
        // strengthResult 的差异时，它会覆盖最后发现的差异（如果有）。

        int result = Collator.EQUAL;

        if (sourceCursor == null) {
            sourceCursor = getCollationElementIterator(source);
        } else {
            sourceCursor.setText(source);
        }
        if (targetCursor == null) {
            targetCursor = getCollationElementIterator(target);
        } else {
            targetCursor.setText(target);
        }

        int sOrder = 0, tOrder = 0;

        boolean initialCheckSecTer = getStrength() >= Collator.SECONDARY;
        boolean checkSecTer = initialCheckSecTer;
        boolean checkTertiary = getStrength() >= Collator.TERTIARY;

        boolean gets = true, gett = true;

        while(true) {
            // 获取每个字符串中的下一个排序元素，除非我们被要求跳过它。
            if (gets) sOrder = sourceCursor.next(); else gets = true;
            if (gett) tOrder = targetCursor.next(); else gett = true;

            // 如果我们已经到达其中一个字符串的末尾，跳出循环
            if ((sOrder == CollationElementIterator.NULLORDER)||
                (tOrder == CollationElementIterator.NULLORDER))
                break;

            int pSOrder = CollationElementIterator.primaryOrder(sOrder);
            int pTOrder = CollationElementIterator.primaryOrder(tOrder);

            // 如果在此位置没有差异，我们可以跳过它
            if (sOrder == tOrder) {
                if (tables.isFrenchSec() && pSOrder != 0) {
                    if (!checkSecTer) {
                        // 在法语中，更靠右的二级差异更强，
                        // 因此每个基本元素都需要检查重音
                        checkSecTer = initialCheckSecTer;
                        // 但三级差异比第一个二级差异不那么重要，因此保持不检查三级差异
                        checkTertiary = false;
                    }
                }
                continue;
            }

cpp
                        // 首先比较主要差异。
            if ( pSOrder != pTOrder )
            {
                if (sOrder == 0) {
                    // 源元素的整个部分是可以忽略的。
                    // 跳过当前的源元素，但不获取下一个目标元素。
                    gett = false;
                    continue;
                }
                if (tOrder == 0) {
                    gets = false;
                    continue;
                }

                // 源和目标元素都不是可以忽略的，但其中一个元素的主要部分仍可能是可以忽略的....

                if (pSOrder == 0)  // 源中的主要顺序是可以忽略的
                {
                    // 源的主要部分是可以忽略的，但目标的不是。我们将可忽略的部分视为次要差异，因此记住我们发现了一个。
                    if (checkSecTer) {
                        result = Collator.GREATER;  // (强度为次要)
                        checkSecTer = false;
                    }
                    // 跳过当前的源元素，但不获取下一个目标元素。
                    gett = false;
                }
                else if (pTOrder == 0)
                {
                    // 记录差异 - 参见上面的注释。
                    if (checkSecTer) {
                        result = Collator.LESS;  // (强度为次要)
                        checkSecTer = false;
                    }
                    // 跳过当前的源元素，但不获取下一个目标元素。
                    gets = false;
                } else {
                    // 两个顺序都不是可以忽略的，且我们已经知道主要顺序不同，因为上面的 (pSOrder != pTOrder) 测试。
                    // 记录差异并停止比较。
                    if (pSOrder < pTOrder) {
                        return Collator.LESS;  // (强度为主要)
                    } else {
                        return Collator.GREATER;  // (强度为主要)
                    }
                }
            } else { // else of if ( pSOrder != pTOrder )
                // 主要顺序相同，但完整顺序不同。因此，在这一点上没有基元素，只有可忽略的元素（因为字符串是规范化的）

                if (checkSecTer) {
                    // 次要或三级差异可能仍然重要
                    short secSOrder = CollationElementIterator.secondaryOrder(sOrder);
                    short secTOrder = CollationElementIterator.secondaryOrder(tOrder);
                    if (secSOrder != secTOrder) {
                        // 存在次要差异
                        result = (secSOrder < secTOrder) ? Collator.LESS : Collator.GREATER;
                                                // (强度为次要)
                        checkSecTer = false;
                        // （即使在法语中，基字符内的第一个次要差异也是重要的）
                    } else {
                        if (checkTertiary) {
                            // 三级差异可能仍然重要
                            short terSOrder = CollationElementIterator.tertiaryOrder(sOrder);
                            short terTOrder = CollationElementIterator.tertiaryOrder(tOrder);
                            if (terSOrder != terTOrder) {
                                // 存在三级差异
                                result = (terSOrder < terTOrder) ? Collator.LESS : Collator.GREATER;
                                                // (强度为三级)
                                checkTertiary = false;
                            }
                        }
                    }
                } // if (checkSecTer)

            }  // if ( pSOrder != pTOrder )
        } // while()

        if (sOrder != CollationElementIterator.NULLORDER) {
            // (tOrder 必须是 CollationElementIterator::NULLORDER，
            //  因为只有当 sOrder 或 tOrder 是 NULLORDER 时才会到达这一点。)
            // 源字符串有更多的元素，但目标字符串没有。
            do {
                if (CollationElementIterator.primaryOrder(sOrder) != 0) {
                    // 我们在源字符串中找到了额外的不可忽略的基字符。
                    // 这是一个主要差异，因此源字符串更大
                    return Collator.GREATER; // (强度为主要)
                }
                else if (CollationElementIterator.secondaryOrder(sOrder) != 0) {
                    // 额外的次要元素意味着源字符串更大
                    if (checkSecTer) {
                        result = Collator.GREATER;  // (强度为次要)
                        checkSecTer = false;
                    }
                }
            } while ((sOrder = sourceCursor.next()) != CollationElementIterator.NULLORDER);
        }
        else if (tOrder != CollationElementIterator.NULLORDER) {
            // 目标字符串有更多的元素，但源字符串没有。
            do {
                if (CollationElementIterator.primaryOrder(tOrder) != 0)
                    // 我们在目标字符串中找到了额外的不可忽略的基字符。
                    // 这是一个主要差异，因此源字符串更小
                    return Collator.LESS; // (强度为主要)
                else if (CollationElementIterator.secondaryOrder(tOrder) != 0) {
                    // 目标中的额外次要元素意味着源字符串更小
                    if (checkSecTer) {
                        result = Collator.LESS;  // (强度为次要)
                        checkSecTer = false;
                    }
                }
            } while ((tOrder = targetCursor.next()) != CollationElementIterator.NULLORDER);
        }


                    // 对于IDENTICAL比较，如果所有其他条件都相同，我们使用按位字符比较作为决胜条件
        // 作为决胜条件
        if (result == 0 && getStrength() == IDENTICAL) {
            int mode = getDecomposition();
            Normalizer.Form form;
            if (mode == CANONICAL_DECOMPOSITION) {
                form = Normalizer.Form.NFD;
            } else if (mode == FULL_DECOMPOSITION) {
                form = Normalizer.Form.NFKD;
            } else {
                return source.compareTo(target);
            }

            String sourceDecomposition = Normalizer.normalize(source, form);
            String targetDecomposition = Normalizer.normalize(target, form);
            return sourceDecomposition.compareTo(targetDecomposition);
        }
        return result;
    }

    /**
     * 将字符串转换为一系列可以与CollationKey.compareTo比较的字符。
     * 这个方法覆盖了java.text.Collator.getCollationKey。
     * 可以在子类中重写。
     */
    public synchronized CollationKey getCollationKey(String source)
    {
        //
        // 这里的基本算法是找到源字符串中每个字符的所有排序元素，将它们转换为字符表示，
        // 并将它们放入排序键中。但这比这更复杂。
        // 字符串中的每个排序元素有三个组成部分：主要（A vs B）、次要（A vs A-acute）和第三级（A' vs a）；
        // 字符串末尾的主要差异优先于字符串前面的次要或第三级差异。
        //
        // 为了处理这一点，我们将所有主要顺序放在字符串的开头，其次是次要和第三级顺序，用空字符分隔。
        //
        // 这是一个假设的例子，排序元素表示为三位数，一位代表主要，一位代表次要，等等。
        //
        // 字符串:              A     a     B   \u00e9 <--(e-acute)
        // 排序元素:            101   100   201  510
        //
        // 排序键:             1125<null>0001<null>1010
        //
        // 更复杂的是，在具有法语次要排序的语言中，次要差异（重音符号）从字符串的*末尾*开始比较。
        // 但在比较单个基本字符上的重音符号时，它们从开头比较。为了处理这一点，我们将属于每个基本字符的所有重音符号反转，
        // 然后在最后反转整个次要顺序字符串。以上面的例子为例，法语排序器可能会返回以下结果：
        //
        // 排序键:             1125<null>1000<null>1010
        //
        if (source == null)
            return null;

        if (primResult == null) {
            primResult = new StringBuffer();
            secResult = new StringBuffer();
            terResult = new StringBuffer();
        } else {
            primResult.setLength(0);
            secResult.setLength(0);
            terResult.setLength(0);
        }
        int order = 0;
        boolean compareSec = (getStrength() >= Collator.SECONDARY);
        boolean compareTer = (getStrength() >= Collator.TERTIARY);
        int secOrder = CollationElementIterator.NULLORDER;
        int terOrder = CollationElementIterator.NULLORDER;
        int preSecIgnore = 0;

        if (sourceCursor == null) {
            sourceCursor = getCollationElementIterator(source);
        } else {
            sourceCursor.setText(source);
        }

        // 遍历每个字符
        while ((order = sourceCursor.next()) !=
               CollationElementIterator.NULLORDER)
        {
            secOrder = CollationElementIterator.secondaryOrder(order);
            terOrder = CollationElementIterator.tertiaryOrder(order);
            if (!CollationElementIterator.isIgnorable(order))
            {
                primResult.append((char) (CollationElementIterator.primaryOrder(order)
                                    + COLLATIONKEYOFFSET));

                if (compareSec) {
                    //
                    // 累加附加到给定基本字符的所有可忽略/次要字符
                    //
                    if (tables.isFrenchSec() && preSecIgnore < secResult.length()) {
                        //
                        // 我们正在进行反转的次要排序，并且遇到了一个基本（非可忽略）字符。
                        // 反转适用于最后一个基本字符的任何次要排序。（参见上面的块注释。）
                        //
                        RBCollationTables.reverse(secResult, preSecIgnore, secResult.length());
                    }
                    // 记住我们在次要排序中的位置 - 这是我们需要反转它们时回退的距离。
                    secResult.append((char)(secOrder+ COLLATIONKEYOFFSET));
                    preSecIgnore = secResult.length();
                }
                if (compareTer) {
                    terResult.append((char)(terOrder+ COLLATIONKEYOFFSET));
                }
            }
            else
            {
                if (compareSec && secOrder != 0)
                    secResult.append((char)
                        (secOrder + tables.getMaxSecOrder() + COLLATIONKEYOFFSET));
                if (compareTer && terOrder != 0)
                    terResult.append((char)
                        (terOrder + tables.getMaxTerOrder() + COLLATIONKEYOFFSET));
            }
        }
        if (tables.isFrenchSec())
        {
            if (preSecIgnore < secResult.length()) {
                // 如果我们在最后一个基本字符之后累积了任何次要字符，反转它们。
                RBCollationTables.reverse(secResult, preSecIgnore, secResult.length());
            }
            // 现在反转整个secResult以获得法语次要排序。
            RBCollationTables.reverse(secResult, 0, secResult.length());
        }
        primResult.append((char)0);
        secResult.append((char)0);
        secResult.append(terResult.toString());
        primResult.append(secResult.toString());


                    if (getStrength() == IDENTICAL) {
            primResult.append((char)0);
            int mode = getDecomposition();
            if (mode == CANONICAL_DECOMPOSITION) {
                primResult.append(Normalizer.normalize(source, Normalizer.Form.NFD));
            } else if (mode == FULL_DECOMPOSITION) {
                primResult.append(Normalizer.normalize(source, Normalizer.Form.NFKD));
            } else {
                primResult.append(source);
            }
        }
        return new RuleBasedCollationKey(source, primResult.toString());
    }

    /**
     * 标准重写；语义无变化。
     */
    public Object clone() {
        // 如果我们知道实际上不是 RuleBasedCollator 的子类
        // （这个类应该被声明为 final），绕过
        // Object.clone() 并使用我们的“复制构造函数”。这更快。
        if (getClass() == RuleBasedCollator.class) {
            return new RuleBasedCollator(this);
        }
        else {
            RuleBasedCollator result = (RuleBasedCollator) super.clone();
            result.primResult = null;
            result.secResult = null;
            result.terResult = null;
            result.sourceCursor = null;
            result.targetCursor = null;
            return result;
        }
    }

    /**
     * 比较两个排序对象的相等性。
     * @param obj 要与当前对象比较的基于表的排序对象。
     * @return 如果当前基于表的排序对象与基于表的排序对象 obj 相同，则返回 true；否则返回 false。
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!super.equals(obj)) return false;  // super 进行类检查
        RuleBasedCollator other = (RuleBasedCollator) obj;
        // 所有其他非瞬态信息也包含在规则中。
        return (getRules().equals(other.getRules()));
    }

    /**
     * 生成基于表的排序对象的哈希码
     */
    public int hashCode() {
        return getRules().hashCode();
    }

    /**
     * 允许 CollationElementIterator 访问 tables 对象
     */
    RBCollationTables getTables() {
        return tables;
    }

    // ==============================================================
    // private
    // ==============================================================

    final static int CHARINDEX = 0x70000000;  // 需要在 .commit() 中查找
    final static int EXPANDCHARINDEX = 0x7E000000; // 扩展索引跟随
    final static int CONTRACTCHARINDEX = 0x7F000000;  // 收缩索引跟随
    final static int UNMAPPED = 0xFFFFFFFF;

    private final static int COLLATIONKEYOFFSET = 1;

    private RBCollationTables tables = null;

    // 内部对象，缓存跨调用，以便在每次调用 compare() 和 getCollationKey() 时不必创建/销毁
    private StringBuffer primResult = null;
    private StringBuffer secResult = null;
    private StringBuffer terResult = null;
    private CollationElementIterator sourceCursor = null;
    private CollationElementIterator targetCursor = null;
}
