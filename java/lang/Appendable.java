/*
 * 版权所有 (c) 2003, 2004, Oracle 及/或其附属公司。保留所有权利。
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

package java.lang;

import java.io.IOException;

/**
 * 一个可以追加 <tt>char</tt> 序列和值的对象。任何类的实例如果打算从 {@link
 * java.util.Formatter} 接收格式化输出，都必须实现 <tt>Appendable</tt> 接口。
 *
 * <p> 要追加的字符应该是如 <a href="Character.html#unicode">Unicode 字符表示</a> 中所述的有效 Unicode 字符。注意，补充字符可能由多个 16 位 <tt>char</tt> 值组成。
 *
 * <p> Appendable 对象不一定对多线程访问是安全的。线程安全是扩展和实现此接口的类的责任。
 *
 * <p> 由于此接口可能由具有不同错误处理样式的现有类实现，因此不能保证错误会被传递给调用者。
 *
 * @since 1.5
 */
public interface Appendable {

    /**
     * 将指定的字符序列追加到此 <tt>Appendable</tt>。
     *
     * <p> 根据实现字符序列 <tt>csq</tt> 的类，整个序列可能不会被追加。例如，如果 <tt>csq</tt> 是 {@link java.nio.CharBuffer}，则要追加的子序列由缓冲区的位置和限制定义。
     *
     * @param  csq
     *         要追加的字符序列。如果 <tt>csq</tt> 为 <tt>null</tt>，则将四个字符 <tt>"null"</tt> 追加到此 Appendable。
     *
     * @return  对此 <tt>Appendable</tt> 的引用
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    Appendable append(CharSequence csq) throws IOException;

    /**
     * 将指定字符序列的子序列追加到此 <tt>Appendable</tt>。
     *
     * <p> 当 <tt>csq</tt> 不为 <tt>null</tt> 时，此方法的调用形式 <tt>out.append(csq, start,
     * end)</tt> 的行为与以下调用完全相同：
     *
     * <pre>
     *     out.append(csq.subSequence(start, end)) </pre>
     *
     * @param  csq
     *         要从中追加子序列的字符序列。如果 <tt>csq</tt> 为 <tt>null</tt>，则将字符追加为 <tt>csq</tt> 包含四个字符 <tt>"null"</tt> 时的行为。
     *
     * @param  start
     *         子序列中第一个字符的索引
     *
     * @param  end
     *         子序列中最后一个字符之后的字符索引
     *
     * @return  对此 <tt>Appendable</tt> 的引用
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>start</tt> 或 <tt>end</tt> 为负数，<tt>start</tt>
     *          大于 <tt>end</tt>，或 <tt>end</tt> 大于 <tt>csq.length()</tt>
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    Appendable append(CharSequence csq, int start, int end) throws IOException;

    /**
     * 将指定的字符追加到此 <tt>Appendable</tt>。
     *
     * @param  c
     *         要追加的字符
     *
     * @return  对此 <tt>Appendable</tt> 的引用
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    Appendable append(char c) throws IOException;
}