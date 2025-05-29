
/*
 * 版权所有 (c) 2003, 2012, Oracle 和/或其附属公司。保留所有权利。
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
 * EnumSet 的私有实现类，用于“巨型”枚举类型
 * （即，包含超过 64 个元素的枚举）。
 *
 * @author Josh Bloch
 * @since 1.5
 * @serial 排除
 */
class JumboEnumSet<E extends Enum<E>> extends EnumSet<E> {
    private static final long serialVersionUID = 334349849919042784L;

    /**
     * 该集合的位向量表示。该数组的第 j 个元素的第 i 位表示 universe[64*j + i]
     * 是否存在于此集合中。
     */
    private long elements[];

    // 为了性能而冗余维护
    private int size = 0;

    JumboEnumSet(Class<E> elementType, Enum<?>[] universe) {
        super(elementType, universe);
        elements = new long[(universe.length + 63) >>> 6];
    }

    void addRange(E from, E to) {
        int fromIndex = from.ordinal() >>> 6;
        int toIndex = to.ordinal() >>> 6;

        if (fromIndex == toIndex) {
            elements[fromIndex] = (-1L >>>  (from.ordinal() - to.ordinal() - 1))
                            << from.ordinal();
        } else {
            elements[fromIndex] = (-1L << from.ordinal());
            for (int i = fromIndex + 1; i < toIndex; i++)
                elements[i] = -1;
            elements[toIndex] = -1L >>> (63 - to.ordinal());
        }
        size = to.ordinal() - from.ordinal() + 1;
    }

    void addAll() {
        for (int i = 0; i < elements.length; i++)
            elements[i] = -1;
        elements[elements.length - 1] >>>= -universe.length;
        size = universe.length;
    }

    void complement() {
        for (int i = 0; i < elements.length; i++)
            elements[i] = ~elements[i];
        elements[elements.length - 1] &= (-1L >>> -universe.length);
        size = universe.length - size;
    }

    /**
     * 返回一个迭代器，遍历此集合中的元素。迭代器按元素的<i>自然顺序</i>（即枚举常量声明的顺序）遍历元素。
     * 返回的迭代器是一个“弱一致性”迭代器，永远不会抛出 {@link
     * ConcurrentModificationException}。
     *
     * @return 一个迭代器，遍历此集合中的元素
     */
    public Iterator<E> iterator() {
        return new EnumSetIterator<>();
    }

    private class EnumSetIterator<E extends Enum<E>> implements Iterator<E> {
        /**
         * 位向量，表示集合当前“字”中尚未由此迭代器返回的元素。
         */
        long unseen;

        /**
         * unseen 在 elements 数组中的索引。
         */
        int unseenIndex = 0;

        /**
         * 由该迭代器返回但未移除的最后一个元素的位表示，如果不存在这样的元素，则为零。
         */
        long lastReturned = 0;

        /**
         * lastReturned 在 elements 数组中的索引。
         */
        int lastReturnedIndex = 0;

        EnumSetIterator() {
            unseen = elements[0];
        }

        @Override
        public boolean hasNext() {
            while (unseen == 0 && unseenIndex < elements.length - 1)
                unseen = elements[++unseenIndex];
            return unseen != 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();
            lastReturned = unseen & -unseen;
            lastReturnedIndex = unseenIndex;
            unseen -= lastReturned;
            return (E) universe[(lastReturnedIndex << 6)
                                + Long.numberOfTrailingZeros(lastReturned)];
        }

        @Override
        public void remove() {
            if (lastReturned == 0)
                throw new IllegalStateException();
            final long oldElements = elements[lastReturnedIndex];
            elements[lastReturnedIndex] &= ~lastReturned;
            if (oldElements != elements[lastReturnedIndex]) {
                size--;
            }
            lastReturned = 0;
        }
    }

    /**
     * 返回此集合中的元素数量。
     *
     * @return 此集合中的元素数量
     */
    public int size() {
        return size;
    }

    /**
     * 如果此集合不包含任何元素，则返回 <tt>true</tt>。
     *
     * @return 如果此集合不包含任何元素，则返回 <tt>true</tt>
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 如果此集合包含指定的元素，则返回 <tt>true</tt>。
     *
     * @param e 要检查是否包含在此集合中的元素
     * @return 如果此集合包含指定的元素，则返回 <tt>true</tt>
     */
    public boolean contains(Object e) {
        if (e == null)
            return false;
        Class<?> eClass = e.getClass();
        if (eClass != elementType && eClass.getSuperclass() != elementType)
            return false;

        int eOrdinal = ((Enum<?>)e).ordinal();
        return (elements[eOrdinal >>> 6] & (1L << eOrdinal)) != 0;
    }

    // 修改操作

    /**
     * 如果指定的元素尚未存在，则将其添加到此集合中。
     *
     * @param e 要添加到此集合中的元素
     * @return 如果调用后集合发生更改，则返回 <tt>true</tt>
     *
     * @throws NullPointerException 如果 <tt>e</tt> 为 null
     */
    public boolean add(E e) {
        typeCheck(e);

        int eOrdinal = e.ordinal();
        int eWordNum = eOrdinal >>> 6;

        long oldElements = elements[eWordNum];
        elements[eWordNum] |= (1L << eOrdinal);
        boolean result = (elements[eWordNum] != oldElements);
        if (result)
            size++;
        return result;
    }

    /**
     * 如果指定的元素存在，则从此集合中移除。
     *
     * @param e 如果存在，则要从此集合中移除的元素
     * @return 如果集合包含指定的元素，则返回 <tt>true</tt>
     */
    public boolean remove(Object e) {
        if (e == null)
            return false;
        Class<?> eClass = e.getClass();
        if (eClass != elementType && eClass.getSuperclass() != elementType)
            return false;
        int eOrdinal = ((Enum<?>)e).ordinal();
        int eWordNum = eOrdinal >>> 6;


                    long oldElements = elements[eWordNum];
        elements[eWordNum] &= ~(1L << eOrdinal);
        boolean result = (elements[eWordNum] != oldElements);
        if (result)
            size--;
        return result;
    }

    // 批量操作

    /**
     * 如果此集合包含指定集合中的所有元素，则返回 <tt>true</tt>。
     *
     * @param c 要检查是否包含在此集合中的集合
     * @return 如果此集合包含指定集合中的所有元素，则返回 <tt>true</tt>
     * @throws NullPointerException 如果指定的集合为 null
     */
    public boolean containsAll(Collection<?> c) {
        if (!(c instanceof JumboEnumSet))
            return super.containsAll(c);

        JumboEnumSet<?> es = (JumboEnumSet<?>)c;
        if (es.elementType != elementType)
            return es.isEmpty();

        for (int i = 0; i < elements.length; i++)
            if ((es.elements[i] & ~elements[i]) != 0)
                return false;
        return true;
    }

    /**
     * 将指定集合中的所有元素添加到此集合中。
     *
     * @param c 要添加到此集合中的集合的元素
     * @return 如果此集合因调用而更改，则返回 <tt>true</tt>
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     */
    public boolean addAll(Collection<? extends E> c) {
        if (!(c instanceof JumboEnumSet))
            return super.addAll(c);

        JumboEnumSet<?> es = (JumboEnumSet<?>)c;
        if (es.elementType != elementType) {
            if (es.isEmpty())
                return false;
            else
                throw new ClassCastException(
                    es.elementType + " != " + elementType);
        }

        for (int i = 0; i < elements.length; i++)
            elements[i] |= es.elements[i];
        return recalculateSize();
    }

    /**
     * 从此集合中删除包含在指定集合中的所有元素。
     *
     * @param c 要从此集合中删除的元素
     * @return 如果此集合因调用而更改，则返回 <tt>true</tt>
     * @throws NullPointerException 如果指定的集合为 null
     */
    public boolean removeAll(Collection<?> c) {
        if (!(c instanceof JumboEnumSet))
            return super.removeAll(c);

        JumboEnumSet<?> es = (JumboEnumSet<?>)c;
        if (es.elementType != elementType)
            return false;

        for (int i = 0; i < elements.length; i++)
            elements[i] &= ~es.elements[i];
        return recalculateSize();
    }

    /**
     * 仅保留此集合中包含在指定集合中的元素。
     *
     * @param c 要保留在此集合中的元素
     * @return 如果此集合因调用而更改，则返回 <tt>true</tt>
     * @throws NullPointerException 如果指定的集合为 null
     */
    public boolean retainAll(Collection<?> c) {
        if (!(c instanceof JumboEnumSet))
            return super.retainAll(c);

        JumboEnumSet<?> es = (JumboEnumSet<?>)c;
        if (es.elementType != elementType) {
            boolean changed = (size != 0);
            clear();
            return changed;
        }

        for (int i = 0; i < elements.length; i++)
            elements[i] &= es.elements[i];
        return recalculateSize();
    }

    /**
     * 从此集合中删除所有元素。
     */
    public void clear() {
        Arrays.fill(elements, 0);
        size = 0;
    }

    /**
     * 将指定对象与此集合进行相等性比较。如果给定对象也是一个集合，两个集合的大小相同，并且给定集合的每个成员都包含在此集合中，则返回 <tt>true</tt>。
     *
     * @param o 要与此集合进行相等性比较的对象
     * @return 如果指定对象等于此集合，则返回 <tt>true</tt>
     */
    public boolean equals(Object o) {
        if (!(o instanceof JumboEnumSet))
            return super.equals(o);

        JumboEnumSet<?> es = (JumboEnumSet<?>)o;
        if (es.elementType != elementType)
            return size == 0 && es.size == 0;

        return Arrays.equals(es.elements, elements);
    }

    /**
     * 重新计算集合的大小。如果大小已更改，则返回 true。
     */
    private boolean recalculateSize() {
        int oldSize = size;
        size = 0;
        for (long elt : elements)
            size += Long.bitCount(elt);

        return size != oldSize;
    }

    public EnumSet<E> clone() {
        JumboEnumSet<E> result = (JumboEnumSet<E>) super.clone();
        result.elements = result.elements.clone();
        return result;
    }
}
