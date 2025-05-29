/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其附属公司。保留所有权利。
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

/**
 * 匹配操作的结果。
 *
 * <p>此接口包含用于确定正则表达式匹配结果的查询方法。通过 <code>MatchResult</code> 可以查看匹配边界、组和组边界，但不能修改。</p>
 *
 * @author  Michael McCloskey
 * @see Matcher
 * @since 1.5
 */
public interface MatchResult {

    /**
     * 返回匹配的起始索引。
     *
     * @return 匹配的第一个字符的索引
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，或上一次匹配操作失败
     */
    public int start();

    /**
     * 返回在此匹配中给定组捕获的子序列的起始索引。
     *
     * <p> <a href="Pattern.html#cg">捕获组</a> 从左到右索引，从一开始。组零表示整个模式，因此表达式 <i>m.</i><tt>start(0)</tt> 等同于
     * <i>m.</i><tt>start()</tt>。 </p>
     *
     * @param  group
     *         此匹配器模式中的捕获组的索引
     *
     * @return 捕获组捕获的第一个字符的索引，或 <tt>-1</tt>，如果匹配成功但组本身未匹配任何内容
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，或上一次匹配操作失败
     *
     * @throws  IndexOutOfBoundsException
     *          如果模式中没有给定索引的捕获组
     */
    public int start(int group);

    /**
     * 返回匹配的最后一个字符之后的偏移量。
     *
     * @return 匹配的最后一个字符之后的偏移量
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，或上一次匹配操作失败
     */
    public int end();

    /**
     * 返回在此匹配中给定组捕获的子序列的最后一个字符之后的偏移量。
     *
     * <p> <a href="Pattern.html#cg">捕获组</a> 从左到右索引，从一开始。组零表示整个模式，因此表达式 <i>m.</i><tt>end(0)</tt> 等同于
     * <i>m.</i><tt>end()</tt>。 </p>
     *
     * @param  group
     *         此匹配器模式中的捕获组的索引
     *
     * @return 捕获组捕获的最后一个字符之后的偏移量，或 <tt>-1</tt>，如果匹配成功但组本身未匹配任何内容
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，或上一次匹配操作失败
     *
     * @throws  IndexOutOfBoundsException
     *          如果模式中没有给定索引的捕获组
     */
    public int end(int group);

    /**
     * 返回上一次匹配的输入子序列。
     *
     * <p>对于具有输入序列 <i>s</i> 的匹配器 <i>m</i>，表达式 <i>m.</i><tt>group()</tt> 和
     * <i>s.</i><tt>substring(</tt><i>m.</i><tt>start(),</tt>&nbsp;<i>m.</i><tt>end())</tt>
     * 是等价的。 </p>
     *
     * <p>注意，某些模式，例如 <tt>a*</tt>，匹配空字符串。当模式成功匹配输入中的空字符串时，此方法将返回空字符串。 </p>
     *
     * @return 上一次匹配的（可能是空的）子序列，以字符串形式返回
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，或上一次匹配操作失败
     */
    public String group();

    /**
     * 返回上一次匹配操作中给定组捕获的输入子序列。
     *
     * <p>对于匹配器 <i>m</i>、输入序列 <i>s</i> 和组索引 <i>g</i>，表达式 <i>m.</i><tt>group(</tt><i>g</i><tt>)</tt> 和
     * <i>s.</i><tt>substring(</tt><i>m.</i><tt>start(</tt><i>g</i><tt>),</tt>&nbsp;<i>m.</i><tt>end(</tt><i>g</i><tt>))</tt>
     * 是等价的。 </p>
     *
     * <p> <a href="Pattern.html#cg">捕获组</a> 从左到右索引，从一开始。组零表示整个模式，因此表达式 <tt>m.group(0)</tt> 等同于 <tt>m.group()</tt>。
     * </p>
     *
     * <p>如果匹配成功但指定的组未能匹配输入序列的任何部分，则返回 <tt>null</tt>。注意，某些组，例如 <tt>(a*)</tt>，匹配空字符串。
     * 当这样的组成功匹配输入中的空字符串时，此方法将返回空字符串。 </p>
     *
     * @param  group
     *         此匹配器模式中的捕获组的索引
     *
     * @return 组在上一次匹配中捕获的（可能是空的）子序列，或如果组未能匹配输入的任何部分则返回 <tt>null</tt>
     *
     * @throws  IllegalStateException
     *          如果尚未尝试匹配，或上一次匹配操作失败
     *
     * @throws  IndexOutOfBoundsException
     *          如果模式中没有给定索引的捕获组
     */
    public String group(int group);

    /**
     * 返回此匹配结果模式中的捕获组数量。
     *
     * <p>根据惯例，组零表示整个模式。它不包含在此计数中。
     *
     * <p>任何小于或等于此方法返回值的非负整数都保证是此匹配器的有效组索引。 </p>
     *
     * @return 此匹配器模式中的捕获组数量
     */
    public int groupCount();

}
