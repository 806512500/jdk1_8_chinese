
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
 */

package java.util.regex;

import java.util.Objects;

/**
 * 一个通过解释 {@link Pattern} 来对 {@linkplain java.lang.CharSequence
 * 字符序列} 执行匹配操作的引擎。
 *
 * <p> 从模式中通过调用其 {@link
 * Pattern#matcher matcher} 方法创建一个匹配器。一旦创建，匹配器可以用于执行三种不同类型的匹配操作：
 *
 * <ul>
 *
 *   <li><p> {@link #matches matches} 方法尝试将整个输入序列与模式匹配。 </p></li>
 *
 *   <li><p> {@link #lookingAt lookingAt} 方法尝试从输入序列的开始处与模式匹配。 </p></li>
 *
 *   <li><p> {@link #find find} 方法扫描输入序列，查找下一个与模式匹配的子序列。 </p></li>
 *
 * </ul>
 *
 * <p> 这些方法中的每一个都返回一个布尔值，表示成功或失败。可以通过查询匹配器的状态来获取更多关于成功匹配的信息。
 *
 * <p> 匹配器在输入的一个子集（称为 <i>区域</i>）中查找匹配项。默认情况下，区域包含匹配器的所有输入。可以通过 {@link #region region} 方法修改区域，并通过 {@link #regionStart regionStart} 和 {@link #regionEnd regionEnd} 方法查询区域。可以改变区域边界与某些模式构造的交互方式。有关更多详细信息，请参阅 {@link #useAnchoringBounds
 * useAnchoringBounds} 和 {@link #useTransparentBounds useTransparentBounds}。
 *
 * <p> 本类还定义了用于将匹配的子序列替换为新字符串的方法，这些新字符串的内容可以根据匹配结果计算。可以使用 {@link #appendReplacement appendReplacement} 和 {@link
 * #appendTail appendTail} 方法将结果收集到现有的字符串缓冲区中，或者使用更方便的 {@link
 * #replaceAll replaceAll} 方法创建一个字符串，其中输入序列中的每个匹配子序列都被替换。
 *
 * <p> 匹配器的显式状态包括最近一次成功匹配的开始和结束索引。它还包括模式中每个 <a
 * href="Pattern.html#cg">捕获组</a> 捕获的输入子序列的开始和结束索引以及此类子序列的总数。为了方便起见，还提供了以字符串形式返回这些捕获子序列的方法。
 *
 * <p> 匹配器的显式状态最初是未定义的；在成功匹配之前尝试查询其任何部分将导致抛出 {@link
 * IllegalStateException}。每次匹配操作都会重新计算匹配器的显式状态。
 *
 * <p> 匹配器的隐式状态包括输入字符序列以及 <i>追加位置</i>，初始值为零，并由 {@link #appendReplacement appendReplacement} 方法更新。
 *
 * <p> 可以通过调用匹配器的 {@link #reset()}
 * 方法或如果需要新的输入序列，则调用其 {@link
 * #reset(java.lang.CharSequence) reset(CharSequence)} 方法显式地重置匹配器。重置匹配器会丢弃其显式状态信息并将追加位置设置为零。
 *
 * <p> 本类的实例不适合由多个并发线程使用。 </p>
 *
 *
 * @author      Mike McCloskey
 * @author      Mark Reinhold
 * @author      JSR-51 Expert Group
 * @since       1.4
 * @spec        JSR-51
 */

public final class Matcher implements MatchResult {

    /**
     * 创建此匹配器的 {@link Pattern} 对象。
     */
    Pattern parentPattern;

    /**
     * 用于存储组的数组。如果在匹配过程中跳过了某个组，则它们可能包含无效值。
     */
    int[] groups;

    /**
     * 序列中要匹配的范围。锚点将在这些“硬”边界处匹配。更改区域会更改这些值。
     */
    int from, to;

    /**
     * 回溯使用此值确保子表达式匹配在遇到回溯时结束。
     */
    int lookbehindTo;

    /**
     * 正在匹配的原始字符串。
     */
    CharSequence text;

    /**
     * 上一个节点使用的匹配器状态。NOANCHOR 表示匹配不需要消耗所有输入。ENDANCHOR 是用于匹配所有输入的模式。
     */
    static final int ENDANCHOR = 1;
    static final int NOANCHOR = 0;
    int acceptMode = NOANCHOR;

    /**
     * 上一次匹配操作中匹配的字符串范围。如果上一次匹配失败，则 first 为 -1；last 初始值为 0，然后它保存上一次匹配的结束索引（这是下一次搜索的起点）。
     */
    int first = -1, last = 0;

    /**
     * 上一次匹配操作中匹配的结束索引。
     */
    int oldLast = -1;

    /**
     * 替换操作中最后一次追加的位置索引。
     */
    int lastAppendPosition = 0;

    /**
     * 用于存储节点在模式中重复次数以及组开始位置的数组。节点本身是无状态的，因此它们依赖此字段在匹配过程中保持状态。
     */
    int[] locals;

    /**
     * 布尔值，表示更多输入是否可能改变上一次匹配的结果。
     *
     * 如果 hitEnd 为 true，且找到了匹配，则更多输入可能导致找到不同的匹配。
     * 如果 hitEnd 为 true 且未找到匹配，则更多输入可能导致找到匹配。
     * 如果 hitEnd 为 false 且找到了匹配，则更多输入不会改变匹配。
     * 如果 hitEnd 为 false 且未找到匹配，则更多输入不会导致找到匹配。
     */
    boolean hitEnd;

    /**
     * 布尔值，表示更多输入是否可能将正匹配变为负匹配。
     *
     * 如果 requireEnd 为 true，且找到了匹配，则更多输入可能导致匹配丢失。
     * 如果 requireEnd 为 false 且找到了匹配，则更多输入可能会改变匹配但匹配不会丢失。
     * 如果未找到匹配，则 requireEnd 没有意义。
     */
    boolean requireEnd;

    /**
     * 如果 transparentBounds 为 true，则此匹配器区域的边界对尝试查看其之外的前瞻、回溯和边界匹配构造是透明的。
     */
    boolean transparentBounds = false;

    /**
     * 如果 anchoringBounds 为 true，则此匹配器区域的边界匹配锚点如 ^ 和 $。
     */
    boolean anchoringBounds = true;

    /**
     * 没有默认构造函数。
     */
    Matcher() {
    }

    /**
     * 通过此匹配器解释的模式。
     *
     * @return  创建此匹配器的模式
     */
    public Pattern pattern() {
        return parentPattern;
    }

    /**
     * 返回此匹配器的匹配状态作为 {@link MatchResult}。
     * 该结果不受在此匹配器上执行的后续操作的影响。
     *
     * @return  一个包含此匹配器状态的 <code>MatchResult</code>
     * @since 1.5
     */
    public MatchResult toMatchResult() {
        Matcher result = new Matcher(this.parentPattern, text.toString());
        result.first = this.first;
        result.last = this.last;
        result.groups = this.groups.clone();
        return result;
    }

    /**
     * 更改此匹配器用于查找匹配的 <tt>Pattern</tt>。
     *
     * <p> 此方法会导致此匹配器丢失上次匹配的组信息。匹配器在输入中的位置保持不变，其上次追加位置不受影响。</p>
     *
     * @param  newPattern
     *         此匹配器将使用的新模式
     * @return  此匹配器
     * @throws  IllegalArgumentException
     *          如果 newPattern 为 <tt>null</tt>
     * @since 1.5
     */
    public Matcher usePattern(Pattern newPattern) {
        if (newPattern == null)
            throw new IllegalArgumentException("Pattern cannot be null");
        parentPattern = newPattern;

        // 重新分配状态存储
        int parentGroupCount = Math.max(newPattern.capturingGroupCount, 10);
        groups = new int[parentGroupCount * 2];
        locals = new int[newPattern.localCount];
        for (int i = 0; i < groups.length; i++)
            groups[i] = -1;
        for (int i = 0; i < locals.length; i++)
            locals[i] = -1;
        return this;
    }

    /**
     * 重置此匹配器。
     *
     * <p> 重置匹配器会丢弃其所有显式状态信息并将追加位置设置为零。匹配器的区域被设置为默认区域，即其整个字符序列。匹配器区域边界的锚定和透明性不受影响。
     *
     * @return  此匹配器
     */
    public Matcher reset() {
        first = -1;
        last = 0;
        oldLast = -1;
        for(int i=0; i<groups.length; i++)
            groups[i] = -1;
        for(int i=0; i<locals.length; i++)
            locals[i] = -1;
        lastAppendPosition = 0;
        from = 0;
        to = getTextLength();
        return this;
    }

    /**
     * 用新的输入序列重置此匹配器。
     *
     * <p> 重置匹配器会丢弃其所有显式状态信息并将追加位置设置为零。匹配器的区域被设置为默认区域，即其整个字符序列。匹配器区域边界的锚定和透明性不受影响。
     *
     * @param  input
     *         新的输入字符序列
     *
     * @return  此匹配器
     */
    public Matcher reset(CharSequence input) {
        text = input;
        return reset();
    }

    /**
     * 返回上一次匹配的开始索引。
     *
     * @return  匹配的第一个字符的索引
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，或上一次匹配操作失败
     */
    public int start() {
        if (first < 0)
            throw new IllegalStateException("No match available");
        return first;
    }

    /**
     * 返回上一次匹配操作中给定组捕获的子序列的开始索引。
     *
     * <p> <a href="Pattern.html#cg">捕获组</a> 从左到右索引，从一开始。组零表示整个模式，因此表达式 <i>m.</i><tt>start(0)</tt> 等同于
     * <i>m.</i><tt>start()</tt>。 </p>
     *
     * @param  group
     *         此匹配器模式中的捕获组索引
     *
     * @return  组捕获的第一个字符的索引，或如果匹配成功但组本身未匹配任何内容则返回 <tt>-1</tt>
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，或上一次匹配操作失败
     *
     * @throws  IndexOutOfBoundsException
     *          如果模式中没有给定索引的捕获组
     */
    public int start(int group) {
        if (first < 0)
            throw new IllegalStateException("No match available");
        if (group < 0 || group > groupCount())
            throw new IndexOutOfBoundsException("No group " + group);
        return groups[group * 2];
    }

    /**
     * 返回上一次匹配操作中给定 <a href="Pattern.html#groupname">命名捕获组</a> 捕获的子序列的开始索引。
     *
     * @param  name
     *         此匹配器模式中的命名捕获组名称
     *
     * @return  组捕获的第一个字符的索引，或如果匹配成功但组本身未匹配任何内容则返回 {@code -1}
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，或上一次匹配操作失败
     *
     * @throws  IllegalArgumentException
     *          如果模式中没有给定名称的捕获组
     * @since 1.8
     */
    public int start(String name) {
        return groups[getMatchedGroupIndex(name) * 2];
    }

    /**
     * 返回匹配的最后一个字符之后的偏移量。
     *
     * @return  匹配的最后一个字符之后的偏移量
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，或上一次匹配操作失败
     */
    public int end() {
        if (first < 0)
            throw new IllegalStateException("No match available");
        return last;
    }

    /**
     * 返回上一次匹配操作中给定组捕获的子序列的最后一个字符之后的偏移量。
     *
     * <p> <a href="Pattern.html#cg">捕获组</a> 从左到右索引，从一开始。组零表示整个模式，因此表达式 <i>m.</i><tt>end(0)</tt> 等同于
     * <i>m.</i><tt>end()</tt>。 </p>
     *
     * @param  group
     *         此匹配器模式中的捕获组索引
     *
     * @return  组捕获的最后一个字符之后的偏移量，或如果匹配成功但组本身未匹配任何内容则返回 <tt>-1</tt>
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，或上一次匹配操作失败
     *
     * @throws  IndexOutOfBoundsException
     *          如果模式中没有给定索引的捕获组
     */
    public int end(int group) {
        if (first < 0)
            throw new IllegalStateException("No match available");
        if (group < 0 || group > groupCount())
            throw new IndexOutOfBoundsException("No group " + group);
        return groups[group * 2 + 1];
    }


                /**
     * 返回在上一次匹配操作中，由给定的<a href="Pattern.html#groupname">命名捕获组</a>捕获的子序列的最后一个字符之后的偏移量。
     *
     * @param  name
     *         本匹配器模式中的命名捕获组的名称
     *
     * @return  捕获组捕获的最后一个字符之后的偏移量，
     *          或如果匹配成功但该组本身未匹配任何内容，则返回{@code -1}
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，
     *          或者上一次匹配操作失败
     *
     * @throws  IllegalArgumentException
     *          如果模式中没有给定名称的捕获组
     * @since 1.8
     */
    public int end(String name) {
        return groups[getMatchedGroupIndex(name) * 2 + 1];
    }

    /**
     * 返回上一次匹配操作匹配的输入子序列。
     *
     * <p> 对于具有输入序列<i>s</i>的匹配器<i>m</i>，
     * 表达式<i>m.</i><tt>group()</tt>和
     * <i>s.</i><tt>substring(</tt><i>m.</i><tt>start(),</tt>&nbsp;<i>m.</i><tt>end())</tt>
     * 是等价的。 </p>
     *
     * <p> 注意，某些模式，例如<tt>a*</tt>，匹配空字符串。当该模式成功匹配输入中的空字符串时，
     * 此方法将返回空字符串。 </p>
     *
     * @return 上一次匹配操作匹配的（可能为空的）子序列，以字符串形式返回
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，
     *          或者上一次匹配操作失败
     */
    public String group() {
        return group(0);
    }

    /**
     * 返回在上一次匹配操作中，由给定的捕获组捕获的输入子序列。
     *
     * <p> 对于匹配器<i>m</i>，输入序列<i>s</i>，和捕获组索引
     * <i>g</i>，表达式<i>m.</i><tt>group(</tt><i>g</i><tt>)</tt>和
     * <i>s.</i><tt>substring(</tt><i>m.</i><tt>start(</tt><i>g</i><tt>),</tt>&nbsp;<i>m.</i><tt>end(</tt><i>g</i><tt>))</tt>
     * 是等价的。 </p>
     *
     * <p> <a href="Pattern.html#cg">捕获组</a>从左到右索引，从1开始。组零表示整个模式，因此
     * 表达式<tt>m.group(0)</tt>等价于<tt>m.group()</tt>。 </p>
     *
     * <p> 如果匹配成功但指定的组未能匹配输入序列的任何部分，则返回<tt>null</tt>。注意
     * 某些组，例如<tt>(a*)</tt>，匹配空字符串。当这样的组成功匹配输入中的空字符串时，
     * 此方法将返回空字符串。 </p>
     *
     * @param  group
     *         本匹配器模式中的捕获组的索引
     *
     * @return  上一次匹配操作期间捕获组捕获的（可能为空的）子序列，或如果捕获组
     *          未能匹配输入的任何部分，则返回<tt>null</tt>
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，
     *          或者上一次匹配操作失败
     *
     * @throws  IndexOutOfBoundsException
     *          如果模式中没有给定索引的捕获组
     */
    public String group(int group) {
        if (first < 0)
            throw new IllegalStateException("No match found");
        if (group < 0 || group > groupCount())
            throw new IndexOutOfBoundsException("No group " + group);
        if ((groups[group*2] == -1) || (groups[group*2+1] == -1))
            return null;
        return getSubSequence(groups[group * 2], groups[group * 2 + 1]).toString();
    }

    /**
     * 返回在上一次匹配操作中，由给定的<a href="Pattern.html#groupname">命名捕获组</a>捕获的输入子序列。
     *
     * <p> 如果匹配成功但指定的组未能匹配输入序列的任何部分，则返回<tt>null</tt>。注意
     * 某些组，例如<tt>(a*)</tt>，匹配空字符串。当这样的组成功匹配输入中的空字符串时，
     * 此方法将返回空字符串。 </p>
     *
     * @param  name
     *         本匹配器模式中的命名捕获组的名称
     *
     * @return  上一次匹配操作期间命名组捕获的（可能为空的）子序列，或如果捕获组
     *          未能匹配输入的任何部分，则返回<tt>null</tt>
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，
     *          或者上一次匹配操作失败
     *
     * @throws  IllegalArgumentException
     *          如果模式中没有给定名称的捕获组
     * @since 1.7
     */
    public String group(String name) {
        int group = getMatchedGroupIndex(name);
        if ((groups[group*2] == -1) || (groups[group*2+1] == -1))
            return null;
        return getSubSequence(groups[group * 2], groups[group * 2 + 1]).toString();
    }

    /**
     * 返回本匹配器模式中的捕获组数量。
     *
     * <p> 按照惯例，组零表示整个模式。它不包括在此计数中。
     *
     * <p> 任何小于或等于此方法返回值的非负整数都保证是此匹配器的有效组索引。 </p>
     *
     * @return 本匹配器模式中的捕获组数量
     */
    public int groupCount() {
        return parentPattern.capturingGroupCount - 1;
    }

    /**
     * 尝试将整个区域与模式匹配。
     *
     * <p> 如果匹配成功，则可以通过<tt>start</tt>，<tt>end</tt>和<tt>group</tt>方法获取更多信息。 </p>
     *
     * @return  如果且仅当整个区域序列匹配此匹配器的模式时，返回<tt>true</tt>
     */
    public boolean matches() {
        return match(from, ENDANCHOR);
    }

    /**
     * 尝试找到输入序列中匹配模式的下一个子序列。
     *
     * <p> 如果上一次调用此方法成功且匹配器尚未重置，则此方法从上一次匹配未匹配的第一个字符开始；
     * 否则，从匹配器区域的开始处开始。
     *
     * <p> 如果匹配成功，则可以通过<tt>start</tt>，<tt>end</tt>和<tt>group</tt>方法获取更多信息。 </p>
     *
     * @return  如果且仅当输入序列的子序列匹配此匹配器的模式时，返回<tt>true</tt>
     */
    public boolean find() {
        int nextSearchIndex = last;
        if (nextSearchIndex == first)
            nextSearchIndex++;

        // 如果下一次搜索开始于区域之前，则从区域开始
        if (nextSearchIndex < from)
            nextSearchIndex = from;

        // 如果下一次搜索开始于区域之后，则失败
        if (nextSearchIndex > to) {
            for (int i = 0; i < groups.length; i++)
                groups[i] = -1;
            return false;
        }
        return search(nextSearchIndex);
    }

    /**
     * 重置此匹配器，然后尝试从指定索引开始找到输入序列中匹配模式的下一个子序列。
     *
     * <p> 如果匹配成功，则可以通过<tt>start</tt>，<tt>end</tt>和<tt>group</tt>方法获取更多信息，
     * 并且后续调用{@link #find()}方法将从此次匹配未匹配的第一个字符开始。 </p>
     *
     * @param start 从该索引开始搜索匹配
     * @throws  IndexOutOfBoundsException
     *          如果start小于零或大于输入序列的长度
     *
     * @return  如果且仅当从给定索引开始的输入序列的子序列匹配此匹配器的模式时，返回<tt>true</tt>
     */
    public boolean find(int start) {
        int limit = getTextLength();
        if ((start < 0) || (start > limit))
            throw new IndexOutOfBoundsException("Illegal start index");
        reset();
        return search(start);
    }

    /**
     * 尝试从区域的开始处开始，将输入序列与模式匹配。
     *
     * <p> 与{@link #matches matches}方法类似，此方法总是从区域的开始处开始；
     * 与该方法不同的是，它不要求整个区域都匹配。
     *
     * <p> 如果匹配成功，则可以通过<tt>start</tt>，<tt>end</tt>和<tt>group</tt>方法获取更多信息。 </p>
     *
     * @return  如果且仅当输入序列的前缀匹配此匹配器的模式时，返回<tt>true</tt>
     */
    public boolean lookingAt() {
        return match(from, NOANCHOR);
    }

    /**
     * 返回指定<code>String</code>的字面替换<code>String</code>。
     *
     * 此方法生成一个<code>String</code>，该字符串可以用作{@link Matcher}类的
     * <code>appendReplacement</code>方法中的字面替换<code>s</code>。
     * 生成的<code>String</code>将匹配<code>s</code>中的字符序列，当作字面序列处理。斜杠('\')和
     * 美元符号('$')将不赋予特殊含义。
     *
     * @param  s 要字面化的字符串
     * @return  字面字符串替换
     * @since 1.5
     */
    public static String quoteReplacement(String s) {
        if ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1))
            return s;
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '$') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 实现一个非终端的追加和替换步骤。
     *
     * <p> 此方法执行以下操作： </p>
     *
     * <ol>
     *
     *   <li><p> 从输入序列的追加位置开始读取字符，并将它们追加到给定的字符串缓冲区。它
     *   在读取上一次匹配之前的最后一个字符，即索引{@link
     *   #start()}&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>处停止。 </p></li>
     *
     *   <li><p> 将给定的替换字符串追加到字符串缓冲区。
     *   </p></li>
     *
     *   <li><p> 将此匹配器的追加位置设置为最后一个匹配字符的索引，加一，即设置为{@link #end()}。
     *   </p></li>
     *
     * </ol>
     *
     * <p> 替换字符串可能包含在上一次匹配中捕获的子序列的引用：每个出现的
     * <tt>${</tt><i>name</i><tt>}</tt>或<tt>$</tt><i>g</i>
     * 将被替换为相应的
     * {@link #group(String) group(name)}或{@link #group(int) group(g)}的评估结果。对于<tt>$</tt><i>g</i>，
     * <tt>$</tt>后的第一个数字总是被视为组引用的一部分。后续数字如果可以形成合法的组引用，则会被合并到g中。
     * 只有数字'0'到'9'被认为是组引用的潜在组成部分。例如，如果第二个组匹配字符串<tt>"foo"</tt>，
     * 那么传递替换字符串<tt>"$2bar"</tt>将导致<tt>"foobar"</tt>被追加到字符串缓冲区。美元符号(<tt>$</tt>)
     * 可以通过在其前面加上反斜杠(<tt>\$</tt>)作为字面值包含在替换字符串中。
     *
     * <p> 注意，反斜杠(<tt>\</tt>)和美元符号(<tt>$</tt>)在替换字符串中可能导致结果与将其作为字面值替换字符串处理时不同。
     * 美元符号可能被视为捕获子序列的引用，如上所述，反斜杠用于转义替换字符串中的字面字符。
     *
     * <p> 此方法旨在与{@link #appendTail appendTail}和{@link #find find}方法一起在循环中使用。例如，以下代码
     * 将<tt>one dog two dogs in the yard</tt>写入标准输出流： </p>
     *
     * <blockquote><pre>
     * Pattern p = Pattern.compile("cat");
     * Matcher m = p.matcher("one cat two cats in the yard");
     * StringBuffer sb = new StringBuffer();
     * while (m.find()) {
     *     m.appendReplacement(sb, "dog");
     * }
     * m.appendTail(sb);
     * System.out.println(sb.toString());</pre></blockquote>
     *
     * @param  sb
     *         目标字符串缓冲区
     *
     * @param  replacement
     *         替换字符串
     *
     * @return  此匹配器
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，
     *          或者上一次匹配操作失败
     *
     * @throws  IllegalArgumentException
     *          如果替换字符串引用了不存在于模式中的命名捕获组
     *
     * @throws  IndexOutOfBoundsException
     *          如果替换字符串引用了不存在于模式中的捕获组
     */
    public Matcher appendReplacement(StringBuffer sb, String replacement) {

        // 如果没有匹配，返回错误
        if (first < 0)
            throw new IllegalStateException("No match available");

        // 处理替换字符串，将组引用替换为组
        int cursor = 0;
        StringBuilder result = new StringBuilder();

        while (cursor < replacement.length()) {
            char nextChar = replacement.charAt(cursor);
            if (nextChar == '\\') {
                cursor++;
                if (cursor == replacement.length())
                    throw new IllegalArgumentException(
                        "character to be escaped is missing");
                nextChar = replacement.charAt(cursor);
                result.append(nextChar);
                cursor++;
            } else if (nextChar == '$') {
                // 跳过$
                cursor++;
                // 如果"$"是替换字符串的最后一个字符，则抛出IAE
                if (cursor == replacement.length())
                   throw new IllegalArgumentException(
                        "Illegal group reference: group index is missing");
                nextChar = replacement.charAt(cursor);
                int refNum = -1;
                if (nextChar == '{') {
                    cursor++;
                    StringBuilder gsb = new StringBuilder();
                    while (cursor < replacement.length()) {
                        nextChar = replacement.charAt(cursor);
                        if (ASCII.isLower(nextChar) ||
                            ASCII.isUpper(nextChar) ||
                            ASCII.isDigit(nextChar)) {
                            gsb.append(nextChar);
                            cursor++;
                        } else {
                            break;
                        }
                    }
                    if (gsb.length() == 0)
                        throw new IllegalArgumentException(
                            "named capturing group has 0 length name");
                    if (nextChar != '}')
                        throw new IllegalArgumentException(
                            "named capturing group is missing trailing '}'");
                    String gname = gsb.toString();
                    if (ASCII.isDigit(gname.charAt(0)))
                        throw new IllegalArgumentException(
                            "capturing group name {" + gname +
                            "} starts with digit character");
                    if (!parentPattern.namedGroups().containsKey(gname))
                        throw new IllegalArgumentException(
                            "No group with name {" + gname + "}");
                    refNum = parentPattern.namedGroups().get(gname);
                    cursor++;
                } else {
                    // 第一个数字总是组
                    refNum = (int)nextChar - '0';
                    if ((refNum < 0)||(refNum > 9))
                        throw new IllegalArgumentException(
                            "Illegal group reference");
                    cursor++;
                    // 捕获最大的合法组字符串
                    boolean done = false;
                    while (!done) {
                        if (cursor >= replacement.length()) {
                            break;
                        }
                        int nextDigit = replacement.charAt(cursor) - '0';
                        if ((nextDigit < 0)||(nextDigit > 9)) { // 不是数字
                            break;
                        }
                        int newRefNum = (refNum * 10) + nextDigit;
                        if (groupCount() < newRefNum) {
                            done = true;
                        } else {
                            refNum = newRefNum;
                            cursor++;
                        }
                    }
                }
                // 追加组
                if (start(refNum) != -1 && end(refNum) != -1)
                    result.append(text, start(refNum), end(refNum));
            } else {
                result.append(nextChar);
                cursor++;
            }
        }
        // 追加中间文本
        sb.append(text, lastAppendPosition, first);
        // 追加匹配替换
        sb.append(result);


                    lastAppendPosition = last;
        return this;
    }

    /**
     * 实现一个终端追加和替换步骤。
     *
     * <p> 此方法从输入序列的追加位置开始读取字符，并将它们追加到给定的字符串缓冲区。它旨在在调用一个或多个 {@link
     * #appendReplacement appendReplacement} 方法后调用，以复制输入序列的剩余部分。 </p>
     *
     * @param  sb
     *         目标字符串缓冲区
     *
     * @return  目标字符串缓冲区
     */
    public StringBuffer appendTail(StringBuffer sb) {
        sb.append(text, lastAppendPosition, getTextLength());
        return sb;
    }

    /**
     * 用给定的替换字符串替换输入序列中与模式匹配的每个子序列。
     *
     * <p> 此方法首先重置此匹配器。然后扫描输入序列，查找模式的匹配项。不属于任何匹配项的字符直接追加到结果字符串；每个匹配项在结果中由替换字符串替换。替换字符串可以包含对捕获子序列的引用，如同 {@link
     * #appendReplacement appendReplacement} 方法中的引用。
     *
     * <p> 注意，替换字符串中的反斜杠 (<tt>\</tt>) 和美元符号 (<tt>$</tt>) 可能会导致结果与将其视为字面替换字符串时不同。美元符号可能被解释为对捕获子序列的引用，而反斜杠用于转义替换字符串中的字面字符。
     *
     * <p> 给定正则表达式 <tt>a*b</tt>，输入
     * <tt>"aabfooaabfooabfoob"</tt>，以及替换字符串
     * <tt>"-"</tt>，对此表达式的匹配器调用此方法将生成字符串 <tt>"-foo-foo-foo-"</tt>。
     *
     * <p> 调用此方法会改变此匹配器的状态。如果匹配器将在进一步的匹配操作中使用，则应先重置。 </p>
     *
     * @param  replacement
     *         替换字符串
     *
     * @return  由替换每个匹配子序列的替换字符串构造的字符串，根据需要替换捕获的子序列
     */
    public String replaceAll(String replacement) {
        reset();
        boolean result = find();
        if (result) {
            StringBuffer sb = new StringBuffer();
            do {
                appendReplacement(sb, replacement);
                result = find();
            } while (result);
            appendTail(sb);
            return sb.toString();
        }
        return text.toString();
    }

    /**
     * 用给定的替换字符串替换输入序列中与模式匹配的第一个子序列。
     *
     * <p> 此方法首先重置此匹配器。然后扫描输入序列，查找模式的匹配项。不属于匹配项的字符直接追加到结果字符串；匹配项在结果中由替换字符串替换。替换字符串可以包含对捕获子序列的引用，如同 {@link
     * #appendReplacement appendReplacement} 方法中的引用。
     *
     * <p> 注意，替换字符串中的反斜杠 (<tt>\</tt>) 和美元符号 (<tt>$</tt>) 可能会导致结果与将其视为字面替换字符串时不同。美元符号可能被解释为对捕获子序列的引用，而反斜杠用于转义替换字符串中的字面字符。
     *
     * <p> 给定正则表达式 <tt>dog</tt>，输入
     * <tt>"zzzdogzzzdogzzz"</tt>，以及替换字符串
     * <tt>"cat"</tt>，对此表达式的匹配器调用此方法将生成字符串 <tt>"zzzcatzzzdogzzz"</tt>。 </p>
     *
     * <p> 调用此方法会改变此匹配器的状态。如果匹配器将在进一步的匹配操作中使用，则应先重置。 </p>
     *
     * @param  replacement
     *         替换字符串
     * @return  由替换第一个匹配子序列的替换字符串构造的字符串，根据需要替换捕获的子序列
     */
    public String replaceFirst(String replacement) {
        if (replacement == null)
            throw new NullPointerException("replacement");
        reset();
        if (!find())
            return text.toString();
        StringBuffer sb = new StringBuffer();
        appendReplacement(sb, replacement);
        appendTail(sb);
        return sb.toString();
    }

    /**
     * 设置此匹配器区域的限制。区域是将在其中搜索匹配项的输入序列的部分。调用此方法会重置匹配器，然后将区域设置为从由 <code>start</code> 参数指定的索引开始，到由 <code>end</code> 参数指定的索引结束。
     *
     * <p> 根据使用的透明度和锚定（参见
     * {@link #useTransparentBounds useTransparentBounds} 和
     * {@link #useAnchoringBounds useAnchoringBounds}），某些构造（如锚点）在区域边界处或周围的行为可能会有所不同。
     *
     * @param  start
     *         开始搜索的索引（包含）
     * @param  end
     *         结束搜索的索引（不包含）
     * @throws  IndexOutOfBoundsException
     *          如果 start 或 end 小于零，如果
     *          start 大于输入序列的长度，如果
     *          end 大于输入序列的长度，或者如果
     *          start 大于 end。
     * @return  此匹配器
     * @since 1.5
     */
    public Matcher region(int start, int end) {
        if ((start < 0) || (start > getTextLength()))
            throw new IndexOutOfBoundsException("start");
        if ((end < 0) || (end > getTextLength()))
            throw new IndexOutOfBoundsException("end");
        if (start > end)
            throw new IndexOutOfBoundsException("start > end");
        reset();
        from = start;
        to = end;
        return this;
    }

    /**
     * 报告此匹配器区域的起始索引。此匹配器进行的搜索仅限于在 {@link #regionStart regionStart}（包含）和
     * {@link #regionEnd regionEnd}（不包含）之间查找匹配项。
     *
     * @return  此匹配器区域的起始点
     * @since 1.5
     */
    public int regionStart() {
        return from;
    }

    /**
     * 报告此匹配器区域的结束索引（不包含）。此匹配器进行的搜索仅限于在 {@link #regionStart regionStart}（包含）和
     * {@link #regionEnd regionEnd}（不包含）之间查找匹配项。
     *
     * @return  此匹配器区域的结束点
     * @since 1.5
     */
    public int regionEnd() {
        return to;
    }

    /**
     * 查询此匹配器区域边界的透明度。
     *
     * <p> 如果此匹配器使用 <i>透明</i> 边界，此方法返回 <tt>true</tt>；否则返回 <tt>false</tt>。
     *
     * <p> 有关透明和不透明边界的描述，请参见 {@link #useTransparentBounds useTransparentBounds}。
     *
     * <p> 默认情况下，匹配器使用不透明的区域边界。
     *
     * @return <tt>true</tt> 表示此匹配器使用透明边界，<tt>false</tt> 表示使用不透明边界。
     * @see java.util.regex.Matcher#useTransparentBounds(boolean)
     * @since 1.5
     */
    public boolean hasTransparentBounds() {
        return transparentBounds;
    }

    /**
     * 设置此匹配器区域边界的透明度。
     *
     * <p> 用 <tt>true</tt> 作为参数调用此方法将设置此匹配器使用 <i>透明</i> 边界。如果布尔参数为 <tt>false</tt>，则使用 <i>不透明</i> 边界。
     *
     * <p> 使用透明边界时，此匹配器区域的边界对前瞻、后顾和边界匹配构造是透明的。这些构造可以超越区域边界，查看匹配是否适当。
     *
     * <p> 使用不透明边界时，此匹配器区域的边界对尝试超越它们的前瞻、后顾和边界匹配构造是不透明的。这些构造无法超越边界，因此它们将无法匹配区域外的任何内容。
     *
     * <p> 默认情况下，匹配器使用不透明边界。
     *
     * @param  b 一个布尔值，指示是否使用不透明或透明区域
     * @return 此匹配器
     * @see java.util.regex.Matcher#hasTransparentBounds
     * @since 1.5
     */
    public Matcher useTransparentBounds(boolean b) {
        transparentBounds = b;
        return this;
    }

    /**
     * 查询此匹配器区域边界的锚定。
     *
     * <p> 如果此匹配器使用 <i>锚定</i> 边界，此方法返回 <tt>true</tt>；否则返回 <tt>false</tt>。
     *
     * <p> 有关锚定边界的描述，请参见 {@link #useAnchoringBounds useAnchoringBounds}。
     *
     * <p> 默认情况下，匹配器使用锚定区域边界。
     *
     * @return <tt>true</tt> 表示此匹配器使用锚定边界，<tt>false</tt> 表示不使用锚定边界。
     * @see java.util.regex.Matcher#useAnchoringBounds(boolean)
     * @since 1.5
     */
    public boolean hasAnchoringBounds() {
        return anchoringBounds;
    }

    /**
     * 设置此匹配器区域边界的锚定。
     *
     * <p> 用 <tt>true</tt> 作为参数调用此方法将设置此匹配器使用 <i>锚定</i> 边界。如果布尔参数为 <tt>false</tt>，则使用 <i>非锚定</i> 边界。
     *
     * <p> 使用锚定边界时，此匹配器区域的边界匹配锚点，如 ^ 和 $。
     *
     * <p> 不使用锚定边界时，此匹配器区域的边界将不匹配锚点，如 ^ 和 $。
     *
     * <p> 默认情况下，匹配器使用锚定区域边界。
     *
     * @param  b 一个布尔值，指示是否使用锚定边界。
     * @return 此匹配器
     * @see java.util.regex.Matcher#hasAnchoringBounds
     * @since 1.5
     */
    public Matcher useAnchoringBounds(boolean b) {
        anchoringBounds = b;
        return this;
    }

    /**
     * <p>返回此匹配器的字符串表示形式。此 <code>Matcher</code> 的字符串表示形式包含可能对调试有用的详细信息。确切的格式未指定。
     *
     * @return  此匹配器的字符串表示形式
     * @since 1.5
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("java.util.regex.Matcher");
        sb.append("[pattern=" + pattern());
        sb.append(" region=");
        sb.append(regionStart() + "," + regionEnd());
        sb.append(" lastmatch=");
        if ((first >= 0) && (group() != null)) {
            sb.append(group());
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * <p>如果在由该匹配器执行的最后一个匹配操作中搜索引擎到达了输入的末尾，则返回 true。
     *
     * <p>当此方法返回 true 时，更多的输入可能会改变最后一个搜索的结果。
     *
     * @return  如果在最后一个匹配中到达了输入的末尾，则返回 true；否则返回 false
     * @since 1.5
     */
    public boolean hitEnd() {
        return hitEnd;
    }

    /**
     * <p>如果更多的输入可以将一个正匹配变为负匹配，则返回 true。
     *
     * <p>如果此方法返回 true，并且找到了匹配项，那么更多的输入可能会导致匹配丢失。如果此方法返回 false 并且找到了匹配项，那么更多的输入可能会改变匹配项，但匹配项不会丢失。如果未找到匹配项，则 requireEnd 没有意义。
     *
     * @return  如果更多的输入可以将一个正匹配变为负匹配，则返回 true。
     * @since 1.5
     */
    public boolean requireEnd() {
        return requireEnd;
    }

    /**
     * 在给定边界内启动搜索以查找模式。组被填充默认值，然后调用状态机根的匹配方法。状态机将在此匹配器中保持匹配的状态。
     *
     * Matcher.from 在这里未设置，因为它是搜索开始的“硬”边界，锚点会设置到该边界。from 参数是搜索开始的“软”边界，意味着正则表达式尝试在该索引处匹配，但 ^ 不会匹配那里。后续对搜索方法的调用将从新的“软”边界开始，该边界是前一个匹配的结束。
     */
    boolean search(int from) {
        this.hitEnd = false;
        this.requireEnd = false;
        from        = from < 0 ? 0 : from;
        this.first  = from;
        this.oldLast = oldLast < 0 ? from : oldLast;
        for (int i = 0; i < groups.length; i++)
            groups[i] = -1;
        acceptMode = NOANCHOR;
        boolean result = parentPattern.root.match(this, from, text);
        if (!result)
            this.first = -1;
        this.oldLast = this.last;
        return result;
    }

    /**
     * 在给定边界内启动搜索以查找锚定匹配模式。组被填充默认值，然后调用状态机根的匹配方法。状态机将在此匹配器中保持匹配的状态。
     */
    boolean match(int from, int anchor) {
        this.hitEnd = false;
        this.requireEnd = false;
        from        = from < 0 ? 0 : from;
        this.first  = from;
        this.oldLast = oldLast < 0 ? from : oldLast;
        for (int i = 0; i < groups.length; i++)
            groups[i] = -1;
        acceptMode = anchor;
        boolean result = parentPattern.matchRoot.match(this, from, text);
        if (!result)
            this.first = -1;
        this.oldLast = this.last;
        return result;
    }

    /**
     * 返回文本的结束索引。
     *
     * @return 文本最后一个字符之后的索引
     */
    int getTextLength() {
        return text.length();
    }

    /**
     * 从指定范围生成此匹配器输入的字符串。
     *
     * @param  beginIndex   开始索引，包含
     * @param  endIndex     结束索引，不包含
     * @return 从此匹配器输入生成的字符串
     */
    CharSequence getSubSequence(int beginIndex, int endIndex) {
        return text.subSequence(beginIndex, endIndex);
    }


                /**
     * 返回此 Matcher 的输入字符在索引 i 处的字符。
     *
     * @return 从指定索引返回的字符
     */
    char charAt(int i) {
        return text.charAt(i);
    }

    /**
     * 返回匹配的捕获组的组索引。
     *
     * @return 命名捕获组的索引
     */
    int getMatchedGroupIndex(String name) {
        Objects.requireNonNull(name, "组名");
        if (first < 0)
            throw new IllegalStateException("未找到匹配");
        if (!parentPattern.namedGroups().containsKey(name))
            throw new IllegalArgumentException("没有名为 <" + name + "> 的组");
        return parentPattern.namedGroups().get(name);
    }
}
