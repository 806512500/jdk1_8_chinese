
/*
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

/*
 *
 *
 *
 *
 *
 * 由 Google Inc. 的 Josh Bloch 编写并发布到公共领域，
 * 如 http://creativecommons.org/publicdomain/zero/1.0/ 所解释。
 */

package java.util;

import java.io.Serializable;
import java.util.function.Consumer;
import sun.misc.SharedSecrets;

/**
 * {@link Deque} 接口的可调整数组实现。数组双端队列没有容量限制；它们根据需要增长以支持使用。它们不是线程安全的；在没有外部同步的情况下，它们不支持多个线程的并发访问。禁止使用 null 元素。当用作栈时，此类可能比 {@link Stack} 更快，当用作队列时，可能比 {@link LinkedList} 更快。
 *
 * <p>大多数 {@code ArrayDeque} 操作在摊销后的时间复杂度为常数。例外包括 {@link #remove(Object) remove}、{@link
 * #removeFirstOccurrence removeFirstOccurrence}、{@link #removeLastOccurrence
 * removeLastOccurrence}、{@link #contains contains}、{@link #iterator
 * iterator.remove()} 和批量操作，这些操作的时间复杂度为线性。
 *
 * <p>此类的 {@code iterator} 方法返回的迭代器是 <i>快速失败</i> 的：如果在迭代器创建后任何时候以任何方式修改了双端队列（除了通过迭代器自身的 {@code remove}
 * 方法），迭代器通常会抛出 {@link
 * ConcurrentModificationException}。因此，在并发修改的情况下，迭代器会快速且干净地失败，而不是在未来某个不确定的时间冒险出现任意的、不确定的行为。
 *
 * <p>需要注意的是，迭代器的快速失败行为不能保证，因为通常来说，在存在未同步的并发修改的情况下，无法做出任何硬性保证。快速失败的迭代器会在尽力的基础上抛出 {@code ConcurrentModificationException}。
 * 因此，编写一个依赖此异常正确性的程序是错误的：<i>迭代器的快速失败行为仅应用于检测错误。</i>
 *
 * <p>此类及其迭代器实现了 {@link Collection} 和 {@link
 * Iterator} 接口的所有 <em>可选</em> 方法。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @author  Josh Bloch 和 Doug Lea
 * @since   1.6
 * @param <E> 该集合中持有的元素类型
 */
public class ArrayDeque<E> extends AbstractCollection<E>
                           implements Deque<E>, Cloneable, Serializable
{
    /**
     * 存储双端队列元素的数组。
     * 双端队列的容量是此数组的长度，始终是2的幂。数组从不允许变得完全满，除非在 addX 方法中瞬时满（见 doubleCapacity），在这种情况下，一旦满就会立即调整大小，
     * 从而避免头和尾绕回到相等。我们还保证所有不持有双端队列元素的数组单元始终为 null。
     */
    transient Object[] elements; // 非私有以简化嵌套类访问

    /**
     * 双端队列头部元素的索引（即 remove() 或 pop() 将移除的元素）；或如果双端队列为空，则为与尾部相等的任意数字。
     */
    transient int head;

    /**
     * 将下一个元素添加到双端队列尾部（通过 addLast(E)、add(E) 或 push(E)）的索引。
     */
    transient int tail;

    /**
     * 新创建的双端队列的最小容量。必须是2的幂。
     */
    private static final int MIN_INITIAL_CAPACITY = 8;

    // ******  数组分配和调整大小工具 ******

    private static int calculateSize(int numElements) {
        int initialCapacity = MIN_INITIAL_CAPACITY;
        // 找到可以容纳元素的最佳2的幂。
        // 测试 "<=" 因为数组不会被填满。
        if (numElements >= initialCapacity) {
            initialCapacity = numElements;
            initialCapacity |= (initialCapacity >>>  1);
            initialCapacity |= (initialCapacity >>>  2);
            initialCapacity |= (initialCapacity >>>  4);
            initialCapacity |= (initialCapacity >>>  8);
            initialCapacity |= (initialCapacity >>> 16);
            initialCapacity++;

            if (initialCapacity < 0)   // 元素太多，必须退后
                initialCapacity >>>= 1;// 祝你好运分配 2 ^ 30 个元素
        }
        return initialCapacity;
    }

    /**
     * 分配一个空数组来容纳给定数量的元素。
     *
     * @param numElements  要容纳的元素数量
     */
    private void allocateElements(int numElements) {
        elements = new Object[calculateSize(numElements)];
    }

    /**
     * 将此双端队列的容量翻倍。仅在满时调用，即当头和尾绕回到相等时。
     */
    private void doubleCapacity() {
        assert head == tail;
        int p = head;
        int n = elements.length;
        int r = n - p; // p 右侧的元素数量
        int newCapacity = n << 1;
        if (newCapacity < 0)
            throw new IllegalStateException("对不起，双端队列太大");
        Object[] a = new Object[newCapacity];
        System.arraycopy(elements, p, a, 0, r);
        System.arraycopy(elements, 0, a, r, p);
        elements = a;
        head = 0;
        tail = n;
    }

    /**
     * 将我们元素数组中的元素按顺序（从双端队列的第一个元素到最后一个元素）复制到指定的数组中。假设数组足够大，可以容纳双端队列中的所有元素。
     *
     * @return 其参数
     */
    private <T> T[] copyElements(T[] a) {
        if (head < tail) {
            System.arraycopy(elements, head, a, 0, size());
        } else if (head > tail) {
            int headPortionLen = elements.length - head;
            System.arraycopy(elements, head, a, 0, headPortionLen);
            System.arraycopy(elements, 0, a, headPortionLen, tail);
        }
        return a;
    }

                /**
     * 构造一个空的数组双端队列，初始容量足以容纳16个元素。
     */
    public ArrayDeque() {
        elements = new Object[16];
    }

    /**
     * 构造一个空的数组双端队列，初始容量足以容纳指定数量的元素。
     *
     * @param numElements  双端队列的初始容量下限
     */
    public ArrayDeque(int numElements) {
        allocateElements(numElements);
    }

    /**
     * 构造一个包含指定集合元素的双端队列，按照集合迭代器返回的顺序排列。
     * （集合迭代器返回的第一个元素成为双端队列的第一个元素，或<i>前端</i>。）
     *
     * @param c 要放置到双端队列中的集合
     * @throws NullPointerException 如果指定的集合为null
     */
    public ArrayDeque(Collection<? extends E> c) {
        allocateElements(c.size());
        addAll(c);
    }

    // 主要的插入和提取方法是addFirst, addLast, pollFirst, pollLast。其他方法基于这些方法定义。

    /**
     * 在此双端队列的前端插入指定的元素。
     *
     * @param e 要添加的元素
     * @throws NullPointerException 如果指定的元素为null
     */
    public void addFirst(E e) {
        if (e == null)
            throw new NullPointerException();
        elements[head = (head - 1) & (elements.length - 1)] = e;
        if (head == tail)
            doubleCapacity();
    }

    /**
     * 在此双端队列的末尾插入指定的元素。
     *
     * <p>此方法等同于 {@link #add}。
     *
     * @param e 要添加的元素
     * @throws NullPointerException 如果指定的元素为null
     */
    public void addLast(E e) {
        if (e == null)
            throw new NullPointerException();
        elements[tail] = e;
        if ( (tail = (tail + 1) & (elements.length - 1)) == head)
            doubleCapacity();
    }

    /**
     * 在此双端队列的前端插入指定的元素。
     *
     * @param e 要添加的元素
     * @return {@code true}（如 {@link Deque#offerFirst} 所指定）
     * @throws NullPointerException 如果指定的元素为null
     */
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    /**
     * 在此双端队列的末尾插入指定的元素。
     *
     * @param e 要添加的元素
     * @return {@code true}（如 {@link Deque#offerLast} 所指定）
     * @throws NullPointerException 如果指定的元素为null
     */
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E removeFirst() {
        E x = pollFirst();
        if (x == null)
            throw new NoSuchElementException();
        return x;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E removeLast() {
        E x = pollLast();
        if (x == null)
            throw new NoSuchElementException();
        return x;
    }

    public E pollFirst() {
        int h = head;
        @SuppressWarnings("unchecked")
        E result = (E) elements[h];
        // 如果双端队列为空，元素为null
        if (result == null)
            return null;
        elements[h] = null;     // 必须清空插槽
        head = (h + 1) & (elements.length - 1);
        return result;
    }

    public E pollLast() {
        int t = (tail - 1) & (elements.length - 1);
        @SuppressWarnings("unchecked")
        E result = (E) elements[t];
        if (result == null)
            return null;
        elements[t] = null;
        tail = t;
        return result;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E getFirst() {
        @SuppressWarnings("unchecked")
        E result = (E) elements[head];
        if (result == null)
            throw new NoSuchElementException();
        return result;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E getLast() {
        @SuppressWarnings("unchecked")
        E result = (E) elements[(tail - 1) & (elements.length - 1)];
        if (result == null)
            throw new NoSuchElementException();
        return result;
    }

    @SuppressWarnings("unchecked")
    public E peekFirst() {
        // 如果双端队列为空，elements[head] 为 null
        return (E) elements[head];
    }

    @SuppressWarnings("unchecked")
    public E peekLast() {
        return (E) elements[(tail - 1) & (elements.length - 1)];
    }

    /**
     * 删除此双端队列中从头到尾遍历时指定元素的第一个出现。
     * 如果双端队列不包含该元素，则保持不变。
     * 更正式地说，删除第一个满足 {@code o.equals(e)} 的元素（如果存在这样的元素）。
     * 如果此双端队列包含指定的元素（或等效地，如果此双端队列因调用而改变），则返回 {@code true}。
     *
     * @param o 如果存在，则从双端队列中删除的元素
     * @return {@code true} 如果双端队列包含指定的元素
     */
    public boolean removeFirstOccurrence(Object o) {
        if (o == null)
            return false;
        int mask = elements.length - 1;
        int i = head;
        Object x;
        while ( (x = elements[i]) != null) {
            if (o.equals(x)) {
                delete(i);
                return true;
            }
            i = (i + 1) & mask;
        }
        return false;
    }

    /**
     * 删除此双端队列中从头到尾遍历时指定元素的最后一个出现。
     * 如果双端队列不包含该元素，则保持不变。
     * 更正式地说，删除最后一个满足 {@code o.equals(e)} 的元素（如果存在这样的元素）。
     * 如果此双端队列包含指定的元素（或等效地，如果此双端队列因调用而改变），则返回 {@code true}。
     *
     * @param o 如果存在，则从双端队列中删除的元素
     * @return {@code true} 如果双端队列包含指定的元素
     */
    public boolean removeLastOccurrence(Object o) {
        if (o == null)
            return false;
        int mask = elements.length - 1;
        int i = (tail - 1) & mask;
        Object x;
        while ( (x = elements[i]) != null) {
            if (o.equals(x)) {
                delete(i);
                return true;
            }
            i = (i - 1) & mask;
        }
        return false;
    }

                // *** 队列方法 ***

    /**
     * 将指定的元素插入到此双端队列的末尾。
     *
     * <p>此方法等同于 {@link #addLast}。
     *
     * @param e 要添加的元素
     * @return {@code true}（如 {@link Collection#add} 所指定）
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean add(E e) {
        addLast(e);
        return true;
    }

    /**
     * 将指定的元素插入到此双端队列的末尾。
     *
     * <p>此方法等同于 {@link #offerLast}。
     *
     * @param e 要添加的元素
     * @return {@code true}（如 {@link Queue#offer} 所指定）
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean offer(E e) {
        return offerLast(e);
    }

    /**
     * 检索并移除此双端队列表示的队列的头部。
     *
     * 此方法与 {@link #poll poll} 的不同之处在于，如果此双端队列为空，则会抛出异常。
     *
     * <p>此方法等同于 {@link #removeFirst}。
     *
     * @return 由此双端队列表示的队列的头部
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E remove() {
        return removeFirst();
    }

    /**
     * 检索并移除此双端队列表示的队列的头部（换句话说，此双端队列的第一个元素），如果此双端队列为空，则返回
     * {@code null}。
     *
     * <p>此方法等同于 {@link #pollFirst}。
     *
     * @return 由双端队列表示的队列的头部，如果此双端队列为空，则返回
     *         {@code null}
     */
    public E poll() {
        return pollFirst();
    }

    /**
     * 检索但不移除此双端队列表示的队列的头部。此方法与 {@link #peek peek} 的不同之处在于，如果此双端队列为空，则会抛出异常。
     *
     * <p>此方法等同于 {@link #getFirst}。
     *
     * @return 由双端队列表示的队列的头部
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E element() {
        return getFirst();
    }

    /**
     * 检索但不移除此双端队列表示的队列的头部，如果此双端队列为空，则返回 {@code null}。
     *
     * <p>此方法等同于 {@link #peekFirst}。
     *
     * @return 由双端队列表示的队列的头部，如果此双端队列为空，则返回
     *         {@code null}
     */
    public E peek() {
        return peekFirst();
    }

    // *** 栈方法 ***

    /**
     * 将一个元素压入此双端队列表示的栈中。换句话说，将元素插入到此双端队列的前端。
     *
     * <p>此方法等同于 {@link #addFirst}。
     *
     * @param e 要压入的元素
     * @throws NullPointerException 如果指定的元素为 null
     */
    public void push(E e) {
        addFirst(e);
    }

    /**
     * 从此双端队列表示的栈中弹出一个元素。换句话说，移除并返回此双端队列的第一个元素。
     *
     * <p>此方法等同于 {@link #removeFirst()}。
     *
     * @return 此双端队列前端的元素（即此双端队列表示的栈的顶部）
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E pop() {
        return removeFirst();
    }

    private void checkInvariants() {
        assert elements[tail] == null;
        assert head == tail ? elements[head] == null :
            (elements[head] != null &&
             elements[(tail - 1) & (elements.length - 1)] != null);
        assert elements[(head - 1) & (elements.length - 1)] == null;
    }

    /**
     * 从元素数组中移除指定位置的元素，必要时调整头和尾。这可能导致元素在数组中向前或向后移动。
     *
     * <p>此方法称为 delete 而不是 remove，以强调其语义与 {@link List#remove(int)} 不同。
     *
     * @return 如果元素向后移动，则返回 true
     */
    private boolean delete(int i) {
        checkInvariants();
        final Object[] elements = this.elements;
        final int mask = elements.length - 1;
        final int h = head;
        final int t = tail;
        final int front = (i - h) & mask;
        final int back  = (t - i) & mask;

        // 不变量：head <= i < tail mod circularity
        if (front >= ((t - h) & mask))
            throw new ConcurrentModificationException();

        // 优化以减少元素移动
        if (front < back) {
            if (h <= i) {
                System.arraycopy(elements, h, elements, h + 1, front);
            } else { // 包围
                System.arraycopy(elements, 0, elements, 1, i);
                elements[0] = elements[mask];
                System.arraycopy(elements, h, elements, h + 1, mask - h);
            }
            elements[h] = null;
            head = (h + 1) & mask;
            return false;
        } else {
            if (i < t) { // 复制空的尾部
                System.arraycopy(elements, i + 1, elements, i, back);
                tail = t - 1;
            } else { // 包围
                System.arraycopy(elements, i + 1, elements, i, mask - i);
                elements[mask] = elements[0];
                System.arraycopy(elements, 1, elements, 0, t);
                tail = (t - 1) & mask;
            }
            return true;
        }
    }

    // *** 集合方法 ***

    /**
     * 返回此双端队列中的元素数量。
     *
     * @return 此双端队列中的元素数量
     */
    public int size() {
        return (tail - head) & (elements.length - 1);
    }

    /**
     * 如果此双端队列不包含任何元素，则返回 {@code true}。
     *
     * @return 如果此双端队列不包含任何元素，则返回 {@code true}
     */
    public boolean isEmpty() {
        return head == tail;
    }


                /**
     * 返回一个迭代器，用于遍历此双端队列中的元素。元素将按从第一个（头部）到最后一个（尾部）的顺序排列。这与通过连续调用
     * {@link #remove} 或 {@link #pop} 方法出队元素的顺序相同。
     *
     * @return 一个迭代器，用于遍历此双端队列中的元素
     */
    public Iterator<E> iterator() {
        return new DeqIterator();
    }

    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    private class DeqIterator implements Iterator<E> {
        /**
         * 由后续调用 next 返回的元素的索引。
         */
        private int cursor = head;

        /**
         * 构造时记录的尾部（也在 remove 中记录），用于停止迭代器并检查是否进行了并发修改。
         */
        private int fence = tail;

        /**
         * 最近一次调用 next 返回的元素的索引。如果元素被删除调用 remove，则重置为 -1。
         */
        private int lastRet = -1;

        public boolean hasNext() {
            return cursor != fence;
        }

        public E next() {
            if (cursor == fence)
                throw new NoSuchElementException();
            @SuppressWarnings("unchecked")
            E result = (E) elements[cursor];
            // 此检查不会捕获所有可能的并发修改，但会捕获那些破坏遍历的修改
            if (tail != fence || result == null)
                throw new ConcurrentModificationException();
            lastRet = cursor;
            cursor = (cursor + 1) & (elements.length - 1);
            return result;
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            if (delete(lastRet)) { // 如果左移，撤销 next() 中的增量
                cursor = (cursor - 1) & (elements.length - 1);
                fence = tail;
            }
            lastRet = -1;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            Object[] a = elements;
            int m = a.length - 1, f = fence, i = cursor;
            cursor = f;
            while (i != f) {
                @SuppressWarnings("unchecked") E e = (E)a[i];
                i = (i + 1) & m;
                if (e == null)
                    throw new ConcurrentModificationException();
                action.accept(e);
            }
        }
    }

    private class DescendingIterator implements Iterator<E> {
        /*
         * 这个类几乎是 DeqIterator 的镜像，使用尾部而不是头部作为初始游标，使用头部而不是尾部作为围栏。
         */
        private int cursor = tail;
        private int fence = head;
        private int lastRet = -1;

        public boolean hasNext() {
            return cursor != fence;
        }

        public E next() {
            if (cursor == fence)
                throw new NoSuchElementException();
            cursor = (cursor - 1) & (elements.length - 1);
            @SuppressWarnings("unchecked")
            E result = (E) elements[cursor];
            if (head != fence || result == null)
                throw new ConcurrentModificationException();
            lastRet = cursor;
            return result;
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            if (!delete(lastRet)) {
                cursor = (cursor + 1) & (elements.length - 1);
                fence = head;
            }
            lastRet = -1;
        }
    }

    /**
     * 如果此双端队列包含指定的元素，则返回 {@code true}。更正式地说，如果此双端队列包含至少一个元素 {@code e} 使得 {@code o.equals(e)}，则返回 {@code true}。
     *
     * @param o 要检查是否包含在此双端队列中的对象
     * @return 如果此双端队列包含指定的元素，则返回 {@code true}
     */
    public boolean contains(Object o) {
        if (o == null)
            return false;
        int mask = elements.length - 1;
        int i = head;
        Object x;
        while ( (x = elements[i]) != null) {
            if (o.equals(x))
                return true;
            i = (i + 1) & mask;
        }
        return false;
    }

    /**
     * 从此双端队列中移除指定元素的一个实例。如果双端队列不包含该元素，则保持不变。
     * 更正式地说，移除第一个元素 {@code e} 使得 {@code o.equals(e)}（如果存在这样的元素）。
     * 如果此双端队列包含指定的元素（或等效地，如果此双端队列因调用而改变），则返回 {@code true}。
     *
     * <p>此方法等同于 {@link #removeFirstOccurrence(Object)}。
     *
     * @param o 要从此双端队列中移除的元素，如果存在
     * @return 如果此双端队列包含指定的元素，则返回 {@code true}
     */
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    /**
     * 从此双端队列中移除所有元素。调用此方法后，双端队列将为空。
     */
    public void clear() {
        int h = head;
        int t = tail;
        if (h != t) { // 清除所有单元格
            head = tail = 0;
            int i = h;
            int mask = elements.length - 1;
            do {
                elements[i] = null;
                i = (i + 1) & mask;
            } while (i != t);
        }
    }

    /**
     * 返回一个包含此双端队列中所有元素的数组（从第一个到最后一个元素）。
     *
     * <p>返回的数组将是“安全的”，即此双端队列不会保留对它的任何引用。（换句话说，此方法必须分配一个新数组）。因此，调用者可以自由地修改返回的数组。
     *
     * <p>此方法作为基于数组和基于集合的 API 之间的桥梁。
     *
     * @return 一个包含此双端队列中所有元素的数组
     */
    public Object[] toArray() {
        return copyElements(new Object[size()]);
    }

                /**
     * 返回一个包含此双端队列中所有元素的数组（从第一个元素到最后一个元素）；返回数组的运行时类型是指定数组的类型。如果双端队列适合指定的数组，则返回该数组。否则，将分配一个具有指定数组的运行时类型和此双端队列大小的新数组。
     *
     * <p>如果此双端队列适合指定的数组，并且数组有剩余空间（即，数组的元素比双端队列多），则紧跟在双端队列末尾的数组元素将被设置为
     * {@code null}。
     *
     * <p>像 {@link #toArray()} 方法一样，此方法充当基于数组和基于集合的API之间的桥梁。此外，此方法允许对输出数组的运行时类型进行精确控制，并且在某些情况下，可以用来节省分配成本。
     *
     * <p>假设 {@code x} 是一个已知仅包含字符串的双端队列。以下代码可以用来将双端队列转储到一个新的 {@code String} 数组中：
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * 注意，{@code toArray(new Object[0])} 与 {@code toArray()} 的功能相同。
     *
     * @param a 如果足够大，则存储双端队列元素的数组；否则，为此目的分配一个相同运行时类型的新数组
     * @return 包含此双端队列中所有元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此双端队列中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        int size = size();
        if (a.length < size)
            a = (T[])java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        copyElements(a);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    // *** Object 方法 ***

    /**
     * 返回此双端队列的一个副本。
     *
     * @return 此双端队列的一个副本
     */
    public ArrayDeque<E> clone() {
        try {
            @SuppressWarnings("unchecked")
            ArrayDeque<E> result = (ArrayDeque<E>) super.clone();
            result.elements = Arrays.copyOf(elements, elements.length);
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    private static final long serialVersionUID = 2340985798034038923L;

    /**
     * 将此双端队列保存到流中（即，序列化它）。
     *
     * @serialData 双端队列的当前大小（{@code int}），后跟所有元素（每个都是对象引用）按从第一个到最后一个的顺序。
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.defaultWriteObject();

        // 写出大小
        s.writeInt(size());

        // 按顺序写出元素。
        int mask = elements.length - 1;
        for (int i = head; i != tail; i = (i + 1) & mask)
            s.writeObject(elements[i]);
    }

    /**
     * 从流中恢复此双端队列（即，反序列化它）。
     */
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();

        // 读取大小并分配数组
        int size = s.readInt();
        int capacity = calculateSize(size);
        SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, capacity);
        allocateElements(size);
        head = 0;
        tail = size;

        // 按正确的顺序读取所有元素。
        for (int i = 0; i < size; i++)
            elements[i] = s.readObject();
    }

    /**
     * 创建一个 <em><a href="Spliterator.html#binding">延迟绑定</a></em>
     * 和 <em>快速失败</em> 的 {@link Spliterator}，覆盖此双端队列中的元素。
     *
     * <p>此 {@code Spliterator} 报告 {@link Spliterator#SIZED}，
     * {@link Spliterator#SUBSIZED}，{@link Spliterator#ORDERED} 和
     * {@link Spliterator#NONNULL}。覆盖实现应记录额外的特性值。
     *
     * @return 一个覆盖此双端队列中元素的 {@code Spliterator}
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new DeqSpliterator<E>(this, -1, -1);
    }

    static final class DeqSpliterator<E> implements Spliterator<E> {
        private final ArrayDeque<E> deq;
        private int fence;  // -1 直到首次使用
        private int index;  // 当前索引，在遍历/拆分时修改

        /** 创建一个新的覆盖给定数组和范围的 spliterator */
        DeqSpliterator(ArrayDeque<E> deq, int origin, int fence) {
            this.deq = deq;
            this.index = origin;
            this.fence = fence;
        }

        private int getFence() { // 强制初始化
            int t;
            if ((t = fence) < 0) {
                t = fence = deq.tail;
                index = deq.head;
            }
            return t;
        }

        public DeqSpliterator<E> trySplit() {
            int t = getFence(), h = index, n = deq.elements.length;
            if (h != t && ((h + 1) & (n - 1)) != t) {
                if (h > t)
                    t += n;
                int m = ((h + t) >>> 1) & (n - 1);
                return new DeqSpliterator<>(deq, h, index = m);
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> consumer) {
            if (consumer == null)
                throw new NullPointerException();
            Object[] a = deq.elements;
            int m = a.length - 1, f = getFence(), i = index;
            index = f;
            while (i != f) {
                @SuppressWarnings("unchecked") E e = (E)a[i];
                i = (i + 1) & m;
                if (e == null)
                    throw new ConcurrentModificationException();
                consumer.accept(e);
            }
        }


                    public boolean tryAdvance(Consumer<? super E> consumer) {
            if (consumer == null)
                throw new NullPointerException();
            Object[] a = deq.elements;
            int m = a.length - 1, f = getFence(), i = index;
            if (i != fence) {
                @SuppressWarnings("unchecked") E e = (E)a[i];
                index = (i + 1) & m;
                if (e == null)
                    throw new ConcurrentModificationException();
                consumer.accept(e);
                return true;
            }
            return false;
        }

        public long estimateSize() {
            int n = getFence() - index;
            if (n < 0)
                n += deq.elements.length;
            return (long) n;
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED |
                Spliterator.NONNULL | Spliterator.SUBSIZED;
        }
    }

}
