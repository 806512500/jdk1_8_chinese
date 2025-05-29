/*
 * 版权所有 (c) 2000, 2006，Oracle 和/或其附属公司。保留所有权利。
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
 * 用于表示 <tt>List</tt> 实现支持快速（通常为常数时间）随机访问的标记接口。此接口的主要目的是允许通用算法根据列表是随机访问还是顺序访问来调整其行为，以提供良好的性能。
 *
 * <p>操纵随机访问列表（如 <tt>ArrayList</tt>）的最佳算法在应用于顺序访问列表（如 <tt>LinkedList</tt>）时可能会产生二次行为。鼓励通用列表算法在应用可能导致性能低下的算法之前，检查给定列表是否为该接口的实例，并在必要时调整其行为以保证可接受的性能。
 *
 * <p>认识到随机访问和顺序访问之间的区别往往是模糊的。例如，某些 <tt>List</tt> 实现如果变得非常大，可能会提供渐近线性访问时间，但在实践中提供常数访问时间。这样的 <tt>List</tt> 实现通常应实现此接口。作为一个经验法则，如果对于该类的典型实例，以下循环：
 * <pre>
 *     for (int i=0, n=list.size(); i &lt; n; i++)
 *         list.get(i);
 * </pre>
 * 运行速度比以下循环快：
 * <pre>
 *     for (Iterator i=list.iterator(); i.hasNext(); )
 *         i.next();
 * </pre>
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @since 1.4
 */
public interface RandomAccess {
}