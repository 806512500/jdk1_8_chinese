
/*
 * 版权所有 (c) 1999, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

package java.util.regex;

import java.util.Objects;

/**
 * 一个通过解释 {@link Pattern} 对 {@linkplain java.lang.CharSequence
 * 字符序列} 执行匹配操作的引擎。
 *
 * <p> 从模式通过调用模式的 {@link
 * Pattern#matcher matcher} 方法创建一个匹配器。一旦创建，匹配器可以用于执行三种不同类型的匹配操作：
 *
 * <ul>
 *
 *   <li><p> {@link #matches matches} 方法尝试将整个输入序列与模式匹配。 </p></li>
 *
 *   <li><p> {@link #lookingAt lookingAt} 方法尝试从开头开始将输入序列与模式匹配。 </p></li>
 *
 *   <li><p> {@link #find find} 方法扫描输入序列，查找下一个与模式匹配的子序列。 </p></li>
 *
 * </ul>
 *
 * <p> 这些方法中的每一个都返回一个布尔值，指示成功或失败。可以通过查询匹配器的状态来获取有关成功匹配的更多信息。
 *
 * <p> 匹配器在输入的一个子集（称为 <i>区域</i>）中查找匹配项。默认情况下，区域包含匹配器的所有输入。可以通过 {@link #region region} 方法修改区域，并通过 {@link #regionStart regionStart} 和 {@link #regionEnd regionEnd} 方法查询区域。可以改变区域边界与某些模式构造的交互方式。有关详细信息，请参见 {@link #useAnchoringBounds
 * useAnchoringBounds} 和 {@link #useTransparentBounds useTransparentBounds}。
 *
 * <p> 本类还定义了用于将匹配的子序列替换为新字符串的方法，新字符串的内容可以根据需要从匹配结果计算得出。可以使用 {@link #appendReplacement appendReplacement} 和 {@link
 * #appendTail appendTail} 方法协同工作，将结果收集到现有的字符串缓冲区中，或者使用更方便的 {@link
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
 * #reset(java.lang.CharSequence) reset(CharSequence)} 方法显式重置匹配器。重置匹配器会丢弃其显式状态信息并将追加位置设置为零。
 *
 * <p> 本类的实例不适合多个并发线程使用。 </p>
 *
 *
 * @author      Mike McCloskey
 * @author      Mark Reinhold
 * @author      JSR-51 专家组
 * @since       1.4
 * @spec        JSR-51
 */

public final class Matcher implements MatchResult {

    /**
     * 创建此 Matcher 的 Pattern 对象。
     */
    Pattern parentPattern;

    /**
     * 用于组的存储。如果在匹配过程中跳过了某个组，则它们可能包含无效值。
     */
    int[] groups;

    /**
     * 序列中要匹配的范围。锚点将在这些“硬”边界处匹配。更改区域会更改这些值。
     */
    int from, to;

    /**
     * 后瞻使用此值确保子表达式匹配在后瞻遇到的点结束。
     */
    int lookbehindTo;

    /**
     * 正在匹配的原始字符串。
     */
    CharSequence text;

    /**
     * 最后一个节点使用的匹配器状态。NOANCHOR 用于匹配不需要消耗所有输入的情况。ENDANCHOR 是用于匹配所有输入的模式。
     */
    static final int ENDANCHOR = 1;
    static final int NOANCHOR = 0;
    int acceptMode = NOANCHOR;

    /**
     * 最后一次匹配的模式的字符串范围。如果最后一次匹配失败，则 first 为 -1；last 最初为 0，然后它保存最后一次匹配的结束索引（这是下一次搜索的起点）。
     */
    int first = -1, last = 0;

    /**
     * 最后一次匹配操作中匹配的结束索引。
     */
    int oldLast = -1;

    /**
     * 替换操作中追加的最后一个位置的索引。
     */
    int lastAppendPosition = 0;

    /**
     * 节点用于告诉在模式中处于哪个重复项以及组开始的位置的存储。节点本身是无状态的，因此它们依赖此字段在匹配过程中保持状态。
     */
    int[] locals;

    /**
     * 布尔值，指示是否更多的输入可以改变最后一次匹配的结果。
     *
     * 如果 hitEnd 为 true，且找到了匹配项，则更多的输入可能会导致找到不同的匹配项。
     * 如果 hitEnd 为 true 且未找到匹配项，则更多的输入可能会导致找到匹配项。
     * 如果 hitEnd 为 false 且找到了匹配项，则更多的输入不会改变匹配项。
     * 如果 hitEnd 为 false 且未找到匹配项，则更多的输入不会导致找到匹配项。
     */
    boolean hitEnd;

                /**
     * 布尔值，表示更多的输入是否可以将正匹配变为负匹配。
     *
     * 如果 requireEnd 为 true，并且找到了匹配项，那么更多的输入可能会导致匹配丢失。
     * 如果 requireEnd 为 false 并且找到了匹配项，那么更多的输入可能会改变匹配项，但匹配项不会丢失。
     * 如果没有找到匹配项，那么 requireEnd 没有意义。
     */
    boolean requireEnd;

    /**
     * 如果 transparentBounds 为 true，则此匹配器区域的边界对于尝试查看其之外的前瞻、后瞻和边界匹配构造是透明的。
     */
    boolean transparentBounds = false;

    /**
     * 如果 anchoringBounds 为 true，则此匹配器区域的边界匹配锚点，如 ^ 和 $。
     */
    boolean anchoringBounds = true;

    /**
     * 没有默认构造函数。
     */
    Matcher() {
    }

    /**
     * 所有匹配器都具有模式在匹配期间使用的状态。
     */
    Matcher(Pattern parent, CharSequence text) {
        this.parentPattern = parent;
        this.text = text;

        // 分配状态存储
        int parentGroupCount = Math.max(parent.capturingGroupCount, 10);
        groups = new int[parentGroupCount * 2];
        locals = new int[parent.localCount];

        // 将字段置于初始状态
        reset();
    }

    /**
     * 返回此匹配器解释的模式。
     *
     * @return  创建此匹配器的模式
     */
    public Pattern pattern() {
        return parentPattern;
    }

    /**
     * 以 {@link MatchResult} 的形式返回此匹配器的匹配状态。
     * 该结果不受在此匹配器上执行的后续操作的影响。
     *
     * @return  包含此匹配器状态的 <code>MatchResult</code>
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
      * 更改此 <tt>Matcher</tt> 用于查找匹配项的 <tt>Pattern</tt>。
      *
      * <p> 此方法会导致此匹配器丢失关于上次匹配发生时的组信息。匹配器在输入中的位置保持不变，其最后的追加位置也不受影响。</p>
      *
      * @param  newPattern
      *         此匹配器使用的新模式
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
     * <p> 重置匹配器会丢弃其所有显式状态信息，并将其追加位置设置为零。匹配器的区域被设置为默认区域，即其整个字符序列。此匹配器区域边界的锚定和透明性不受影响。
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
     * 使用新的输入序列重置此匹配器。
     *
     * <p> 重置匹配器会丢弃其所有显式状态信息，并将其追加位置设置为零。匹配器的区域被设置为默认区域，即其整个字符序列。此匹配器区域边界的锚定和透明性不受影响。
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
     * 返回上一次匹配的起始索引。
     *
     * @return  匹配的第一个字符的索引
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，或者上一次匹配操作失败
     */
    public int start() {
        if (first < 0)
            throw new IllegalStateException("No match available");
        return first;
    }

    /**
     * 返回上一次匹配操作中给定组捕获的子序列的起始索引。
     *
     * <p> <a href="Pattern.html#cg">捕获组</a> 从左到右索引，从 1 开始。组零表示整个模式，因此表达式 <i>m.</i><tt>start(0)</tt> 等效于
     * <i>m.</i><tt>start()</tt>。 </p>
     *
     * @param  group
     *         此匹配器模式中捕获组的索引
     *
     * @return  组捕获的第一个字符的索引，
     *          或者如果匹配成功但组本身没有匹配任何内容，则返回 <tt>-1</tt>
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，或者上一次匹配操作失败
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
     * 返回在上一次匹配操作中，由给定的<a href="Pattern.html#groupname">命名捕获组</a>捕获的子序列的起始索引。
     *
     * @param  name
     *         本匹配器模式中的命名捕获组的名称
     *
     * @return  捕获组捕获的第一个字符的索引，
     *          或者如果匹配成功但该组本身未匹配任何内容，则返回{@code -1}
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，
     *          或者上一次匹配操作失败
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
     *          如果尚未尝试匹配，
     *          或者上一次匹配操作失败
     */
    public int end() {
        if (first < 0)
            throw new IllegalStateException("No match available");
        return last;
    }

    /**
     * 返回在上一次匹配操作中，由给定的捕获组捕获的子序列的最后一个字符之后的偏移量。
     *
     * <p> <a href="Pattern.html#cg">捕获组</a>从左到右索引，从一开始。 组零表示整个模式，因此
     * 表达式 <i>m.</i><tt>end(0)</tt> 等同于
     * <i>m.</i><tt>end()</tt>。 </p>
     *
     * @param  group
     *         本匹配器模式中的捕获组的索引
     *
     * @return  捕获组捕获的最后一个字符之后的偏移量，
     *          或者如果匹配成功但该组本身未匹配任何内容，则返回<tt>-1</tt>
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，
     *          或者上一次匹配操作失败
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
     *          或者如果匹配成功但该组本身未匹配任何内容，则返回{@code -1}
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
     * 返回上一次匹配的输入子序列。
     *
     * <p> 对于具有输入序列 <i>s</i> 的匹配器 <i>m</i>，
     * 表达式 <i>m.</i><tt>group()</tt> 和
     * <i>s.</i><tt>substring(</tt><i>m.</i><tt>start(),</tt>&nbsp;<i>m.</i><tt>end())</tt>
     * 是等价的。 </p>
     *
     * <p> 注意，某些模式，例如 <tt>a*</tt>，匹配空字符串。 当模式成功匹配输入中的空字符串时，此方法将返回空字符串。 </p>
     *
     * @return 上一次匹配的（可能是空的）子序列，以字符串形式返回
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，
     *          或者上一次匹配操作失败
     */
    public String group() {
        return group(0);
    }

    /**
     * 返回在上一次匹配操作中，由给定的组捕获的输入子序列。
     *
     * <p> 对于匹配器 <i>m</i>，输入序列 <i>s</i> 和组索引
     * <i>g</i>，表达式 <i>m.</i><tt>group(</tt><i>g</i><tt>)</tt> 和
     * <i>s.</i><tt>substring(</tt><i>m.</i><tt>start(</tt><i>g</i><tt>),</tt>&nbsp;<i>m.</i><tt>end(</tt><i>g</i><tt>))</tt>
     * 是等价的。 </p>
     *
     * <p> <a href="Pattern.html#cg">捕获组</a>从左到右索引，从一开始。 组零表示整个模式，因此
     * 表达式 <tt>m.group(0)</tt> 等同于 <tt>m.group()</tt>。
     * </p>
     *
     * <p> 如果匹配成功但指定的组未能匹配输入序列的任何部分，则返回 <tt>null</tt>。 注意，某些组，例如 <tt>(a*)</tt>，匹配空字符串。
     * 当这样的组成功匹配输入中的空字符串时，此方法将返回空字符串。 </p>
     *
     * @param  group
     *         本匹配器模式中的捕获组的索引
     *
     * @return  在上一次匹配中由组捕获的（可能是空的）子序列，或者如果组未能匹配输入的部分，则返回 <tt>null</tt>
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
     * 返回在上一次匹配操作中，由给定的
     * <a href="Pattern.html#groupname">命名捕获组</a>捕获的输入子序列。
     *
     * <p> 如果匹配成功但指定的组未能匹配输入序列的任何部分，则返回<tt>null</tt>。注意
     * 有些组，例如<tt>(a*)</tt>，匹配空字符串。
     * 当这样的组在输入中成功匹配空字符串时，此方法将返回空字符串。  </p>
     *
     * @param  name
     *         本匹配器模式中的命名捕获组的名称
     *
     * @return  在上一次匹配中由命名组捕获的（可能是空的）子序列
     *          或者如果该组未能匹配输入的部分，则返回<tt>null</tt>
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
     * 返回此匹配器模式中的捕获组的数量。
     *
     * <p> 按惯例，组零表示整个模式。它不包含在此计数中。
     *
     * <p> 任何小于或等于此方法返回值的非负整数都保证是此匹配器的有效组索引。  </p>
     *
     * @return 本匹配器模式中的捕获组的数量
     */
    public int groupCount() {
        return parentPattern.capturingGroupCount - 1;
    }

    /**
     * 尝试将整个区域与模式匹配。
     *
     * <p> 如果匹配成功，则可以通过
     * <tt>start</tt>、<tt>end</tt>和<tt>group</tt>方法获取更多信息。  </p>
     *
     * @return  如果且仅当整个区域序列匹配此匹配器的模式时，返回<tt>true</tt>
     */
    public boolean matches() {
        return match(from, ENDANCHOR);
    }

    /**
     * 尝试找到输入序列中与模式匹配的下一个子序列。
     *
     * <p> 此方法从本匹配器区域的开始处开始，或者，如果方法的前一次调用成功且匹配器尚未重置，则从前一次匹配未匹配的第一个字符开始。
     *
     * <p> 如果匹配成功，则可以通过
     * <tt>start</tt>、<tt>end</tt>和<tt>group</tt>方法获取更多信息。  </p>
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

        // 如果下一次搜索开始于区域之外，则失败
        if (nextSearchIndex > to) {
            for (int i = 0; i < groups.length; i++)
                groups[i] = -1;
            return false;
        }
        return search(nextSearchIndex);
    }

    /**
     * 重置此匹配器，然后尝试找到从指定索引开始的输入序列中与模式匹配的下一个子序列。
     *
     * <p> 如果匹配成功，则可以通过
     * <tt>start</tt>、<tt>end</tt>和<tt>group</tt>方法获取更多信息，且后续调用
     * {@link #find()}方法将从此次匹配未匹配的第一个字符开始。  </p>
     *
     * @param start 开始搜索匹配的索引
     * @throws  IndexOutOfBoundsException
     *          如果start小于零或start大于输入序列的长度。
     *
     * @return  如果且仅当从给定索引开始的输入序列的子序列匹配此匹配器的模式时，返回<tt>true</tt>
     */
    public boolean find(int start) {
        int limit = getTextLength();
        if ((start < 0) || (start > limit))
            throw new IndexOutOfBoundsException("非法的开始索引");
        reset();
        return search(start);
    }

    /**
     * 尝试从区域的开始处匹配输入序列与模式。
     *
     * <p> 与{@link #matches matches}方法类似，此方法始终从区域的开始处开始；
     * 但与该方法不同，它不要求整个区域都被匹配。
     *
     * <p> 如果匹配成功，则可以通过
     * <tt>start</tt>、<tt>end</tt>和<tt>group</tt>方法获取更多信息。  </p>
     *
     * @return  如果且仅当输入序列的前缀匹配此匹配器的模式时，返回<tt>true</tt>
     */
    public boolean lookingAt() {
        return match(from, NOANCHOR);
    }

    /**
     * 返回指定<code>String</code>的字面替换<code>String</code>。
     *
     * 此方法生成一个可以在{@link Matcher}类的
     * <code>appendReplacement</code>方法中作为字面替换<code>s</code>工作的<code>String</code>。
     * 生成的<code>String</code>将匹配<code>s</code>中字符序列的字面值。斜杠('\')和
     * 美元符号('$')将不被赋予特殊含义。
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
     * 实现一个非终结的追加和替换步骤。
     *
     * <p> 此方法执行以下操作： </p>
     *
     * <ol>
     *
     *   <li><p> 从输入序列的追加位置开始读取字符，并将它们追加到给定的字符串缓冲区。它在读取前一个匹配之前的最后一个字符后停止，即索引 {@link
     *   #start()}&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>。 </p></li>
     *
     *   <li><p> 将给定的替换字符串追加到字符串缓冲区。 </p></li>
     *
     *   <li><p> 将此匹配器的追加位置设置为最后一个匹配字符的索引加一，即 {@link #end()}。 </p></li>
     *
     * </ol>
     *
     * <p> 替换字符串可能包含在前一次匹配期间捕获的子序列的引用：每个出现的
     * <tt>${</tt><i>name</i><tt>}</tt> 或 <tt>$</tt><i>g</i>
     * 都将被替换为相应的 {@link #group(String) group(name)} 或 {@link #group(int) group(g)}
     * 的结果。对于 <tt>$</tt><i>g</i>，
     * <tt>$</tt> 后面的第一个数字始终被视为组引用的一部分。如果后续数字可以形成合法的组引用，则它们将被合并到 g 中。只有数字 '0'
     * 到 '9' 被视为组引用的潜在组成部分。例如，如果第二个组匹配字符串 <tt>"foo"</tt>，则传递替换字符串 <tt>"$2bar"</tt> 将导致
     * <tt>"foobar"</tt> 被追加到字符串缓冲区。可以通过在美元符号 (<tt>$</tt>) 前面加上反斜杠 (<tt>\$</tt>) 来在替换字符串中包含字面量的美元符号。
     *
     * <p> 请注意，替换字符串中的反斜杠 (<tt>\</tt>) 和美元符号 (<tt>$</tt>) 可能会使结果与将其视为字面量替换字符串时不同。美元符号可能被视为对捕获子序列的引用，如上所述，而反斜杠用于转义替换字符串中的字面量字符。
     *
     * <p> 此方法旨在与 {@link #appendTail appendTail} 和 {@link #find find} 方法一起在循环中使用。例如，以下代码将 <tt>one dog two dogs in the
     * yard</tt> 写入标准输出流： </p>
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
     *          如果尚未尝试匹配，或者前一次匹配操作失败
     *
     * @throws  IllegalArgumentException
     *          如果替换字符串引用了模式中不存在的命名捕获组
     *
     * @throws  IndexOutOfBoundsException
     *          如果替换字符串引用了模式中不存在的捕获组
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
                // 跳过 $
                cursor++;
                // 如果此 "$" 是替换字符串中的最后一个字符，则抛出 IAE
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
                    // 第一个数字始终是一个组
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
     * <p> 该方法从追加位置开始读取输入序列中的字符，并将它们追加到给定的字符串缓冲区。它旨在在调用一个或多个 {@link
     * #appendReplacement appendReplacement} 方法之后调用，以复制输入序列的其余部分。 </p>
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
     * 将输入序列中与模式匹配的每个子序列替换为给定的替换字符串。
     *
     * <p> 该方法首先重置此匹配器。然后扫描输入序列，查找模式的匹配项。不是任何匹配项一部分的字符直接追加到结果字符串；每个匹配项在结果中由替换字符串替换。替换字符串可以包含如 {@link
     * #appendReplacement appendReplacement} 方法中所述的捕获子序列的引用。
     *
     * <p> 注意，替换字符串中的反斜杠（<tt>\</tt>）和美元符号（<tt>$</tt>）可能导致结果与将其视为字面替换字符串时不同。美元符号可以作为上述捕获子序列的引用处理，而反斜杠用于转义替换字符串中的字面字符。
     *
     * <p> 给定正则表达式 <tt>a*b</tt>，输入
     * <tt>"aabfooaabfooabfoob"</tt>，以及替换字符串
     * <tt>"-"</tt>，对此表达式的匹配器调用此方法将生成字符串 <tt>"-foo-foo-foo-"</tt>。
     *
     * <p> 调用此方法会更改此匹配器的状态。如果匹配器要在进一步的匹配操作中使用，则应首先重置。 </p>
     *
     * @param  replacement
     *         替换字符串
     *
     * @return  通过将每个匹配子序列替换为替换字符串（根据需要替换捕获子序列）构建的字符串
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
     * 将输入序列中与模式匹配的第一个子序列替换为给定的替换字符串。
     *
     * <p> 该方法首先重置此匹配器。然后扫描输入序列，查找模式的匹配项。不是匹配项一部分的字符直接追加到结果字符串；匹配项在结果中由替换字符串替换。替换字符串可以包含如 {@link
     * #appendReplacement appendReplacement} 方法中所述的捕获子序列的引用。
     *
     * <p> 注意，替换字符串中的反斜杠（<tt>\</tt>）和美元符号（<tt>$</tt>）可能导致结果与将其视为字面替换字符串时不同。美元符号可以作为上述捕获子序列的引用处理，而反斜杠用于转义替换字符串中的字面字符。
     *
     * <p> 给定正则表达式 <tt>dog</tt>，输入
     * <tt>"zzzdogzzzdogzzz"</tt>，以及替换字符串
     * <tt>"cat"</tt>，对此表达式的匹配器调用此方法将生成字符串 <tt>"zzzcatzzzdogzzz"</tt>。 </p>
     *
     * <p> 调用此方法会更改此匹配器的状态。如果匹配器要在进一步的匹配操作中使用，则应首先重置。 </p>
     *
     * @param  replacement
     *         替换字符串
     * @return  通过将第一个匹配子序列替换为替换字符串（根据需要替换捕获子序列）构建的字符串
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
     * 设置此匹配器的区域限制。区域是将被搜索以找到匹配项的输入序列的部分。调用此方法将重置匹配器，然后将区域设置为从由 <code>start</code> 参数指定的索引开始，到由 <code>end</code> 参数指定的索引结束。
     *
     * <p>根据使用的透明性和锚定（参见
     * {@link #useTransparentBounds useTransparentBounds} 和
     * {@link #useAnchoringBounds useAnchoringBounds}），某些构造（如锚点）在区域边界处或附近的行为可能不同。
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
     * <p> 如果此匹配器使用<i>透明</i>边界，则此方法返回<tt>true</tt>；如果使用<i>不透明</i>边界，则返回<tt>false</tt>。
     *
     * <p> 有关透明和不透明边界的描述，请参见 {@link #useTransparentBounds useTransparentBounds}。
     *
     * <p> 默认情况下，匹配器使用不透明的区域边界。
     *
     * @return <tt>true</tt> 表示此匹配器正在使用透明边界，<tt>false</tt> 表示否则。
     * @see java.util.regex.Matcher#useTransparentBounds(boolean)
     * @since 1.5
     */
    public boolean hasTransparentBounds() {
        return transparentBounds;
    }

    /**
     * 设置此匹配器区域边界的透明度。
     *
     * <p> 使用<tt>true</tt>作为参数调用此方法将设置此匹配器使用<i>透明</i>边界。如果布尔参数为<tt>false</tt>，则使用<i>不透明</i>边界。
     *
     * <p> 使用透明边界时，此匹配器区域的边界对于前瞻、后顾和边界匹配构造是透明的。这些构造可以超越区域的边界来查看匹配是否合适。
     *
     * <p> 使用不透明边界时，此匹配器区域的边界对于可能试图超越它们的前瞻、后顾和边界匹配构造是不透明的。这些构造不能超越边界，因此它们将无法匹配区域外的任何内容。
     *
     * <p> 默认情况下，匹配器使用不透明的边界。
     *
     * @param  b 表示是否使用不透明或透明区域的布尔值
     * @return 此匹配器
     * @see java.util.regex.Matcher#hasTransparentBounds
     * @since 1.5
     */
    public Matcher useTransparentBounds(boolean b) {
        transparentBounds = b;
        return this;
    }

    /**
     * 查询此匹配器区域边界的锚定情况。
     *
     * <p> 如果此匹配器使用<i>锚定</i>边界，则此方法返回<tt>true</tt>；否则返回<tt>false</tt>。
     *
     * <p> 有关锚定边界的描述，请参见 {@link #useAnchoringBounds useAnchoringBounds}。
     *
     * <p> 默认情况下，匹配器使用锚定的区域边界。
     *
     * @return <tt>true</tt> 表示此匹配器正在使用锚定边界，<tt>false</tt> 表示否则。
     * @see java.util.regex.Matcher#useAnchoringBounds(boolean)
     * @since 1.5
     */
    public boolean hasAnchoringBounds() {
        return anchoringBounds;
    }

    /**
     * 设置此匹配器区域边界的锚定情况。
     *
     * <p> 使用<tt>true</tt>作为参数调用此方法将设置此匹配器使用<i>锚定</i>边界。如果布尔参数为<tt>false</tt>，则使用<i>非锚定</i>边界。
     *
     * <p> 使用锚定边界时，此匹配器区域的边界匹配如^和$这样的锚点。
     *
     * <p> 不使用锚定边界时，此匹配器区域的边界将不匹配如^和$这样的锚点。
     *
     * <p> 默认情况下，匹配器使用锚定的区域边界。
     *
     * @param  b 表示是否使用锚定边界的布尔值。
     * @return 此匹配器
     * @see java.util.regex.Matcher#hasAnchoringBounds
     * @since 1.5
     */
    public Matcher useAnchoringBounds(boolean b) {
        anchoringBounds = b;
        return this;
    }

    /**
     * <p>返回此匹配器的字符串表示形式。此<code>Matcher</code>的字符串表示形式包含可能对调试有用的信息。确切的格式未指定。
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
     * <p>如果在此匹配器执行的最后一个匹配操作中搜索引擎到达了输入的末尾，则返回true。
     *
     * <p>当此方法返回true时，更多的输入可能会改变最后一个搜索的结果。
     *
     * @return  如果在最后一个匹配中到达了输入的末尾，则返回true；否则返回false
     * @since 1.5
     */
    public boolean hitEnd() {
        return hitEnd;
    }

    /**
     * <p>如果更多的输入可能会将一个正匹配变为负匹配，则返回true。
     *
     * <p>如果此方法返回true，并且找到了匹配项，那么更多的输入可能会导致匹配丢失。如果此方法返回false，并且找到了匹配项，那么更多的输入可能会改变匹配，但匹配不会丢失。如果未找到匹配项，则requireEnd没有意义。
     *
     * @return  如果更多的输入可能会将一个正匹配变为负匹配，则返回true。
     * @since 1.5
     */
    public boolean requireEnd() {
        return requireEnd;
    }

                /**
     * 启动一个搜索，以在给定范围内查找一个模式。
     * 组被填充为默认值，并调用状态机根的匹配。状态机将在此匹配器中保持匹配的状态。
     * 
     * Matcher.from 在这里未设置，因为它是指定搜索开始的“硬”边界，锚点将设置为该边界。from 参数
     * 是搜索开始的“软”边界，意味着正则表达式尝试在该索引处匹配，但 ^ 不会在此处匹配。后续
     * 对搜索方法的调用将从新的“软”边界开始，该边界是前一次匹配的结束。
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
     * 在给定范围内启动一个锚定匹配的搜索。组被填充为默认值，并调用状态机根的匹配。状态机将在此匹配器中保持匹配的状态。
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
     * @return 文本中最后一个字符之后的索引
     */
    int getTextLength() {
        return text.length();
    }

    /**
     * 从指定范围内生成一个字符串。
     *
     * @param  beginIndex   开始索引，包含
     * @param  endIndex     结束索引，不包含
     * @return 从该匹配器的输入生成的字符串
     */
    CharSequence getSubSequence(int beginIndex, int endIndex) {
        return text.subSequence(beginIndex, endIndex);
    }

    /**
     * 返回此匹配器输入在索引 i 处的字符。
     *
     * @return 指定索引处的字符
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
        Objects.requireNonNull(name, "Group name");
        if (first < 0)
            throw new IllegalStateException("No match found");
        if (!parentPattern.namedGroups().containsKey(name))
            throw new IllegalArgumentException("No group with name <" + name + ">");
        return parentPattern.namedGroups().get(name);
    }
}
