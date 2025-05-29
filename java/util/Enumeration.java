/*
 * 版权所有 (c) 1994, 2005，Oracle和/或其附属公司。保留所有权利。
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

package java.util;

/**
 * 实现 Enumeration 接口的对象一次生成一系列元素中的一个。对 <code>nextElement</code> 方法的连续调用将返回该系列的连续元素。
 * <p>
 * 例如，要打印 <tt>Vector&lt;E&gt;</tt> <i>v</i> 的所有元素：
 * <pre>
 *   for (Enumeration&lt;E&gt; e = v.elements(); e.hasMoreElements();)
 *       System.out.println(e.nextElement());</pre>
 * <p>
 * 提供了枚举向量的元素、哈希表的键以及哈希表中的值的方法。枚举还用于指定 <code>SequenceInputStream</code> 的输入流。
 * <p>
 * 注意：此接口的功能已被 Iterator 接口复制。此外，Iterator 增加了一个可选的移除操作，并且具有更短的方法名。新实现应考虑优先使用 Iterator 而不是 Enumeration。
 *
 * @see     java.util.Iterator
 * @see     java.io.SequenceInputStream
 * @see     java.util.Enumeration#nextElement()
 * @see     java.util.Hashtable
 * @see     java.util.Hashtable#elements()
 * @see     java.util.Hashtable#keys()
 * @see     java.util.Vector
 * @see     java.util.Vector#elements()
 *
 * @author  Lee Boynton
 * @since   JDK1.0
 */
public interface Enumeration<E> {
    /**
     * 测试此枚举是否包含更多元素。
     *
     * @return  如果此枚举对象至少包含一个更多元素，则返回 <code>true</code>；
     *          否则返回 <code>false</code>。
     */
    boolean hasMoreElements();

    /**
     * 如果此枚举对象至少包含一个更多元素，则返回此枚举的下一个元素。
     *
     * @return     此枚举的下一个元素。
     * @exception  NoSuchElementException  如果没有更多元素存在。
     */
    E nextElement();
}