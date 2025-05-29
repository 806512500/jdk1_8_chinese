/*
 * 版权所有 (c) 1994, 2010, Oracle 和/或其附属公司。保留所有权利。
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
 * <code>Stack</code> 类表示一个后进先出 (LIFO) 的对象栈。它通过五个操作扩展了 <tt>Vector</tt> 类，使得向量可以作为栈使用。通常提供 <tt>push</tt> 和 <tt>pop</tt> 操作，以及查看栈顶项目的 <tt>peek</tt> 方法，测试栈是否 <tt>empty</tt> 的方法，以及在栈中搜索项目并发现其距离栈顶多远的 <tt>search</tt> 方法。
 * <p>
 * 当栈首次创建时，它不包含任何项目。
 *
 * <p>由 {@link Deque} 接口及其实现提供了一组更完整和一致的 LIFO 栈操作，应优先使用这些接口和实现。例如：
 * <pre>   {@code
 *   Deque<Integer> stack = new ArrayDeque<Integer>();}</pre>
 *
 * @author  Jonathan Payne
 * @since   JDK1.0
 */
public
class Stack<E> extends Vector<E> {
    /**
     * 创建一个空栈。
     */
    public Stack() {
    }

    /**
     * 将一个项目压入此栈的顶部。这与以下操作具有完全相同的效果：
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
     * 移除此栈顶部的对象，并将该对象作为此函数的值返回。
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
     * 查看此栈顶部的对象而不从栈中移除它。
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
     * @return  如果且仅当此栈不包含任何项目时返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean empty() {
        return size() == 0;
    }

    /**
     * 返回此栈上对象的 1 基数位置。如果对象 <tt>o</tt> 作为项目出现在此栈中，此方法返回该对象最近出现在栈顶的距离；栈顶的项目被认为距离为 <tt>1</tt>。使用 <tt>equals</tt> 方法将 <tt>o</tt> 与栈中的项目进行比较。
     *
     * @param   o   所需的对象。
     * @return  对象所在位置的 1 基数距离；返回值 <code>-1</code> 表示对象不在栈上。
     */
    public synchronized int search(Object o) {
        int i = lastIndexOf(o);

        if (i >= 0) {
            return size() - i;
        }
        return -1;
    }

    /** 为了互操作性，使用 JDK 1.0.2 的 serialVersionUID */
    private static final long serialVersionUID = 1224463164541339165L;
}