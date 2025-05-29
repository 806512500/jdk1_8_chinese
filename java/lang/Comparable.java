/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
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
import java.util.*;

/**
 * 此接口对实现它的每个类的对象强加了总顺序。这种顺序称为类的<i>自然顺序</i>，类的<tt>compareTo</tt>方法称为其<i>自然比较方法</i>。<p>
 *
 * 实现此接口的对象（和数组）列表可以通过{@link Collections#sort(List) Collections.sort}（和
 * {@link Arrays#sort(Object[]) Arrays.sort}）自动排序。实现此接口的对象可以用作
 * {@linkplain SortedMap 排序映射}中的键或{@linkplain SortedSet 排序集合}中的元素，而无需指定
 * {@linkplain Comparator 比较器}。<p>
 *
 * 如果对于类<tt>C</tt>的每个<tt>e1</tt>和<tt>e2</tt>，<tt>e1.compareTo(e2) == 0</tt>与
 * <tt>e1.equals(e2)</tt>具有相同的布尔值，则称类<tt>C</tt>的自然顺序与equals<i>一致</i>。
 * 注意，<tt>null</tt>不是任何类的实例，<tt>e.compareTo(null)</tt>应该抛出
 * <tt>NullPointerException</tt>，即使<tt>e.equals(null)</tt>返回<tt>false</tt>。<p>
 *
 * 强烈建议（但不是必需的）自然顺序与equals一致。这是因为当使用自然顺序与equals不一致的元素（或键）
 * 时，没有显式比较器的排序集（或排序映射）会表现出“奇怪”的行为。特别是，这样的排序集（或排序映射）违反了
 * 以<tt>equals</tt>方法定义的集（或映射）的一般合同。<p>
 *
 * 例如，如果将两个键<tt>a</tt>和<tt>b</tt>（其中
 * {@code (!a.equals(b) && a.compareTo(b) == 0)}）添加到不使用显式比较器的排序集中，第二个<tt>add</tt>
 * 操作将返回false（并且排序集的大小不会增加），因为从排序集的角度来看，<tt>a</tt>和<tt>b</tt>是等价的。<p>
 *
 * 几乎所有实现<tt>Comparable</tt>的Java核心类都有与equals一致的自然顺序。一个例外是
 * <tt>java.math.BigDecimal</tt>，其自然顺序将具有相同值但不同精度的<tt>BigDecimal</tt>对象等同（如4.0和4.00）。<p>
 *
 * 对于数学爱好者来说，定义给定类C的自然顺序的<i>关系</i>是：<pre>
 *       {(x, y) such that x.compareTo(y) &lt;= 0}.
 * </pre> 这个全序的<i>商集</i>是： <pre>
 *       {(x, y) such that x.compareTo(y) == 0}.
 * </pre>
 *
 * 从<tt>compareTo</tt>的合同中可以立即得出，商集是<tt>C</tt>上的<i>等价关系</i>，而自然顺序是<tt>C</tt>上的<i>全序</i>。
 * 当我们说一个类的自然顺序与equals<i>一致</i>时，我们的意思是自然顺序的商集是由类的
 * {@link Object#equals(Object) equals(Object)}方法定义的等价关系：<pre>
 *     {(x, y) such that x.equals(y)}. </pre><p>
 *
 * 此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>的成员。
 *
 * @param <T> 此对象可以与之比较的对象类型
 *
 * @author  Josh Bloch
 * @see java.util.Comparator
 * @since 1.2
 */
public interface Comparable<T> {
    /**
     * 按顺序比较此对象与指定对象。如果此对象小于、等于或大于指定对象，则返回一个负整数、零或正整数。
     *
     * <p>实现者必须确保<tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt>对于所有<tt>x</tt>和<tt>y</tt>。这意味着如果
     * <tt>x.compareTo(y)</tt>抛出异常，<tt>y.compareTo(x)</tt>也必须抛出异常。
     *
     * <p>实现者还必须确保关系是传递的：<tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt>
     * 意味着<tt>x.compareTo(z)&gt;0</tt>。
     *
     * <p>最后，实现者必须确保<tt>x.compareTo(y)==0</tt>意味着
     * <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>，对于所有<tt>z</tt>。
     *
     * <p>强烈建议，但<i>不是</i>严格要求<tt>(x.compareTo(y)==0) == (x.equals(y))</tt>。
     * 通常情况下，任何实现<tt>Comparable</tt>接口并违反此条件的类都应明确指出这一事实。推荐的语言是
     * "注意：此类具有与equals不一致的自然顺序。"
     *
     * <p>在上述描述中，符号<tt>sgn(</tt><i>expression</i><tt>)</tt>表示数学上的
     * <i>符号函数</i>，定义为根据<i>expression</i>的值是负数、零还是正数返回<tt>-1</tt>、<tt>0</tt>或<tt>1</tt>。
     *
     * @param   o 要比较的对象。
     * @return  一个负整数、零或正整数，表示此对象小于、等于或大于指定对象。
     *
     * @throws NullPointerException 如果指定的对象为null
     * @throws ClassCastException 如果指定对象的类型阻止它与该对象进行比较。
     */
    public int compareTo(T o);
}
