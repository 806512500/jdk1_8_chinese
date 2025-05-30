/*
 * Copyright (c) 1994, 2010, Oracle and/or its affiliates. All rights reserved.
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
 * <code>Stack</code> 类表示一个后进先出（LIFO）的对象栈。它扩展了 <tt>Vector</tt> 类，提供了五种操作，使向量可以作为栈使用。通常提供 <tt>push</tt> 和 <tt>pop</tt> 操作，以及一个方法来 <tt>peek</tt> 查看栈顶的项目，一个方法来测试栈是否 <tt>empty</tt>，以及一个方法来 <tt>search</tt> 查找栈中的项目并发现它距离栈顶有多远。
 * <p>
 * 当栈首次创建时，它不包含任何项目。
 *
 * <p>一个更完整和一致的 LIFO 栈操作集由 {@link Deque} 接口及其实现提供，应优先使用这些类。例如：
 * <pre>   {@code
 *   Deque<Integer> stack = new ArrayDeque<Integer>();}</pre>
 *
 * @author  Jonathan Payne
 * @since   JDK1.0
 */
public
class Stack<E> extends Vector<E> {
    /**
     * 创建一个空的栈。
     */
    public Stack() {
    }

    /**
     * 将一个项目压入此栈的顶部。这与以下操作完全相同：
     * <blockquote><pre>
     * addElement(item)</pre></blockquote>
     *
     * @param   item   要压入此栈的项目。
     * @return  <code>item</code> 参数。
     * @see     java.util.Vector#addElement
     */
    public E push(E item) {
        addElement(item);

        return item;
    }

    /**
     * 移除此栈顶部的对象并返回该对象作为此函数的值。
     *
     * @return  此栈顶部的对象（<tt>Vector</tt> 对象的最后一个项目）。
     * @throws  EmptyStackException  如果此栈为空。
     */
    public synchronized E pop() {
        E       obj;
        int     len = size();

        obj = peek();
        removeElementAt(len - 1);

        return obj;
    }

    /**
     * 查看此栈顶部的对象而不将其从栈中移除。
     *
     * @return  此栈顶部的对象（<tt>Vector</tt> 对象的最后一个项目）。
     * @throws  EmptyStackException  如果此栈为空。
     */
    public synchronized E peek() {
        int     len = size();

        if (len == 0)
            throw new EmptyStackException();
        return elementAt(len - 1);
    }

    /**
     * 测试此栈是否为空。
     *
     * @return  <code>true</code> 如果且仅当此栈不包含任何项目；<code>false</code> 否则。
     */
    public boolean empty() {
        return size() == 0;
    }

    /**
     * 返回对象在此栈上的 1 基位置。如果对象 <tt>o</tt> 作为项目出现在此栈中，此方法返回该对象距离栈顶最近的出现位置；栈顶的项目被认为是距离 <tt>1</tt>。使用 <tt>equals</tt> 方法比较 <tt>o</tt> 与栈中的项目。
     *
     * @param   o   所需的对象。
     * @return  对象在栈顶的位置（1 基）；返回值 <code>-1</code> 表示对象不在栈上。
     */
    public synchronized int search(Object o) {
        int i = lastIndexOf(o);

        if (i >= 0) {
            return size() - i;
        }
        return -1;
    }

    /** 使用 JDK 1.0.2 的 serialVersionUID 以确保互操作性 */
    private static final long serialVersionUID = 1224463164541339165L;
}
