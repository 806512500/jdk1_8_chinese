
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

/**
 * 该类提供了 <tt>Set</tt> 接口的骨架实现，以最小化实现此接口所需的努力。 <p>
 *
 * 通过扩展此类来实现集合的过程与通过扩展 AbstractCollection 来实现 Collection 的过程相同，
 * 但此类的所有子类的方法和构造函数必须遵守 <tt>Set</tt> 接口施加的额外约束（例如，add 方法不得允许多个实例
 * 的对象添加到集合中）。<p>
 *
 * 请注意，此类并未覆盖 <tt>AbstractCollection</tt> 类中的任何实现。它只是添加了 <tt>equals</tt>
 * 和 <tt>hashCode</tt> 的实现。<p>
 *
 * 该类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @param <E> 该集合维护的元素类型
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see Collection
 * @see AbstractCollection
 * @see Set
 * @since 1.2
 */

public abstract class AbstractSet<E> extends AbstractCollection<E> implements Set<E> {
    /**
     * 唯一的构造函数。 （通常由子类构造函数调用，通常是隐式的。）
     */
    protected AbstractSet() {
    }

    // 比较和哈希

    /**
     * 将指定对象与此集合进行比较以确定相等性。 如果给定对象也是一个集合，两个集合的大小相同，并且给定集合的每个成员都包含在
     * 此集合中，则返回 <tt>true</tt>。 这确保了 <tt>equals</tt> 方法在 <tt>Set</tt> 接口的不同实现之间
     * 能够正常工作。<p>
     *
     * 此实现首先检查指定对象是否是此集合；如果是，则返回 <tt>true</tt>。 然后，它检查指定对象是否是一个大小与此集合
     * 相同的集合；如果不是，则返回 false。 如果是，则返回 <tt>containsAll((Collection) o)</tt>。
     *
     * @param o 要与此集合进行相等性比较的对象
     * @return 如果指定对象等于此集合，则返回 <tt>true</tt>
     */
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Set))
            return false;
        Collection<?> c = (Collection<?>) o;
        if (c.size() != size())
            return false;
        try {
            return containsAll(c);
        } catch (ClassCastException unused)   {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    /**
     * 返回此集合的哈希码值。 集合的哈希码定义为集合中元素的哈希码之和，其中 <tt>null</tt> 元素的哈希码定义为零。
     * 这确保了 <tt>s1.equals(s2)</tt> 意味着 <tt>s1.hashCode()==s2.hashCode()</tt> 对于任何两个集合 <tt>s1</tt>
     * 和 <tt>s2</tt>，如 {@link Object#hashCode} 的一般契约所要求的。
     *
     * <p>此实现遍历集合，调用集合中每个元素的 <tt>hashCode</tt> 方法，并将结果相加。
     *
     * @return 此集合的哈希码值
     * @see Object#equals(Object)
     * @see Set#equals(Object)
     */
    public int hashCode() {
        int h = 0;
        Iterator<E> i = iterator();
        while (i.hasNext()) {
            E obj = i.next();
            if (obj != null)
                h += obj.hashCode();
        }
        return h;
    }

    /**
     * 从此集合中移除所有包含在指定集合中的元素（可选操作）。 如果指定的集合也是一个集合，此操作实际上会修改此集合，
     * 使其值为两个集合的 <i>非对称集合差集</i>。
     *
     * <p>此实现通过调用每个集合的 <tt>size</tt> 方法来确定此集合和指定集合中哪个较小。 如果此集合元素较少，则实现
     * 遍历此集合，检查迭代器返回的每个元素是否包含在指定集合中。 如果包含，则使用迭代器的 <tt>remove</tt> 方法
     * 从该集合中移除。 如果指定集合元素较少，则实现遍历指定集合，使用此集合的 <tt>remove</tt> 方法移除迭代器返回的
     * 每个元素。
     *
     * <p>请注意，如果 <tt>iterator</tt> 方法返回的迭代器未实现 <tt>remove</tt> 方法，此实现将抛出
     * <tt>UnsupportedOperationException</tt>。
     *
     * @param  c 包含要从此集合中移除的元素的集合
     * @return 如果此集合因调用而改变，则返回 <tt>true</tt>
     * @throws UnsupportedOperationException 如果此集合不支持 <tt>removeAll</tt> 操作
     * @throws ClassCastException 如果此集合中元素的类与指定集合不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果此集合包含 null 元素且指定集合不允许 null 元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)，或者指定集合为 null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;


                    if (size() > c.size()) {
            for (Iterator<?> i = c.iterator(); i.hasNext(); )
                modified |= remove(i.next());
        } else {
            for (Iterator<?> i = iterator(); i.hasNext(); ) {
                if (c.contains(i.next())) {
                    i.remove();
                    modified = true;
                }
            }
        }
        return modified;
    }

}
