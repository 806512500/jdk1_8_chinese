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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的条款发布到公共领域。
 */

package java.util.concurrent;
import java.util.Collection;
import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.function.Consumer;

/**
 * 一个使用内部 {@link CopyOnWriteArrayList} 执行所有操作的 {@link java.util.Set}。
 * 因此，它共享相同的基属性：
 * <ul>
 *  <li>它最适合于集合大小通常保持较小，读取操作远多于修改操作，且需要
 *       在遍历时防止线程间的干扰的应用程序。
 *  <li>它是线程安全的。
 *  <li>修改操作（如 {@code add}、{@code set}、{@code remove} 等）由于通常需要复制整个底层数组而代价高昂。
 *  <li>迭代器不支持修改的 {@code remove} 操作。
 *  <li>通过迭代器遍历速度快且不会遇到其他线程的干扰。迭代器依赖于构建迭代器时数组的不变快照。
 * </ul>
 *
 * <p><b>示例用法。</b> 以下代码示例使用一个复制写入集合来维护一组在状态更新时执行某些操作的 Handler 对象。
 *
 *  <pre> {@code
 * class Handler { void handle(); ... }
 *
 * class X {
 *   private final CopyOnWriteArraySet<Handler> handlers
 *     = new CopyOnWriteArraySet<Handler>();
 *   public void addHandler(Handler h) { handlers.add(h); }
 *
 *   private long internalState;
 *   private synchronized void changeState() { internalState = ...; }
 *
 *   public void update() {
 *     changeState();
 *     for (Handler handler : handlers)
 *       handler.handle();
 *   }
 * }}</pre>
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @see CopyOnWriteArrayList
 * @since 1.5
 * @author Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
public class CopyOnWriteArraySet<E> extends AbstractSet<E>
        implements java.io.Serializable {
    private static final long serialVersionUID = 5457747651344034263L;

    private final CopyOnWriteArrayList<E> al;

    /**
     * 创建一个空集合。
     */
    public CopyOnWriteArraySet() {
        al = new CopyOnWriteArrayList<E>();
    }

    /**
     * 创建一个包含指定集合中所有元素的集合。
     *
     * @param c 要初始包含的元素集合
     * @throws NullPointerException 如果指定的集合为 null
     */
    public CopyOnWriteArraySet(Collection<? extends E> c) {
        if (c.getClass() == CopyOnWriteArraySet.class) {
            @SuppressWarnings("unchecked") CopyOnWriteArraySet<E> cc =
                (CopyOnWriteArraySet<E>)c;
            al = new CopyOnWriteArrayList<E>(cc.al);
        }
        else {
            al = new CopyOnWriteArrayList<E>();
            al.addAllAbsent(c);
        }
    }

    /**
     * 返回此集合中的元素数量。
     *
     * @return 此集合中的元素数量
     */
    public int size() {
        return al.size();
    }

    /**
     * 如果此集合不包含任何元素，则返回 {@code true}。
     *
     * @return 如果此集合不包含任何元素，则返回 {@code true}
     */
    public boolean isEmpty() {
        return al.isEmpty();
    }

    /**
     * 如果此集合包含指定的元素，则返回 {@code true}。
     * 更正式地说，如果且仅当此集合包含一个元素 {@code e} 使得
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>，则返回 {@code true}。
     *
     * @param o 要测试其是否存在于此集合中的元素
     * @return 如果此集合包含指定的元素，则返回 {@code true}
     */
    public boolean contains(Object o) {
        return al.contains(o);
    }

    /**
     * 返回一个包含此集合中所有元素的数组。
     * 如果此集合对其迭代器返回元素的顺序有任何保证，则此方法必须以相同的顺序返回元素。
     *
     * <p>返回的数组将是“安全的”，即此集合不会维护对它的任何引用。（换句话说，即使此集合由数组支持，此方法也必须分配一个新数组。）
     * 因此，调用者可以自由地修改返回的数组。
     *
     * <p>此方法充当基于数组的 API 和基于集合的 API 之间的桥梁。
     *
     * @return 一个包含此集合中所有元素的数组
     */
    public Object[] toArray() {
        return al.toArray();
    }

    /**
     * 返回一个包含此集合中所有元素的数组；返回数组的运行时类型是指定数组的运行时类型。
     * 如果集合适合指定的数组，则返回该数组。否则，分配一个运行时类型为指定数组且大小为此集合大小的新数组。
     *
     * <p>如果此集合适合指定的数组且有剩余空间（即，数组的元素多于此集合的元素），则数组中紧跟在此集合末尾的元素设置为
     * {@code null}。（这仅在调用者知道此集合不包含任何 null 元素时，对于确定此集合的长度是有用的。）
     *
     * <p>如果此集合对其迭代器返回元素的顺序有任何保证，则此方法必须以相同的顺序返回元素。
     *
     * <p>与 {@link #toArray()} 方法一样，此方法充当基于数组的 API 和基于集合的 API 之间的桥梁。此外，此方法允许精确控制输出数组的运行时类型，并且在某些情况下，可以用来节省分配成本。
     *
     * <p>假设 {@code x} 是一个已知仅包含字符串的集合。以下代码可用于将集合转储到新分配的 {@code String} 数组中：
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * 注意，{@code toArray(new Object[0])} 在功能上与 {@code toArray()} 相同。
     *
     * @param a 如果足够大，则用于存储此集合元素的数组；否则，为此目的分配一个相同运行时类型的数组。
     * @return 一个包含此集合中所有元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此集合中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    public <T> T[] toArray(T[] a) {
        return al.toArray(a);
    }

    /**
     * 从此集合中移除所有元素。
     * 此方法调用后，集合将为空。
     */
    public void clear() {
        al.clear();
    }

    /**
     * 如果此集合包含指定元素，则将其移除。
     * 更正式地说，如果此集合包含一个元素 {@code e} 使得
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>，
     * 则移除该元素。如果此集合包含该元素，则返回 {@code true}。
     * （此方法调用返回后，此集合将不再包含该元素。）
     *
     * @param o 要从此集合中移除的对象（如果存在）
     * @return 如果此集合包含指定元素，则返回 {@code true}
     */
    public boolean remove(Object o) {
        return al.remove(o);
    }

    /**
     * 如果此集合中尚未包含指定元素，则将其添加到此集合中。
     * 更正式地说，如果此集合不包含任何元素 {@code e2} 使得
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>，
     * 则将指定元素 {@code e} 添加到此集合中。如果此集合已包含该元素，
     * 则调用后集合保持不变并返回 {@code false}。
     *
     * @param e 要添加到此集合中的元素
     * @return 如果此集合之前不包含指定元素，则返回 {@code true}
     */
    public boolean add(E e) {
        return al.addIfAbsent(e);
    }

    /**
     * 如果此集合包含指定集合中的所有元素，则返回 {@code true}。
     * 如果指定的集合也是一个集合，则此方法返回 {@code true} 如果它是此集合的 <i>子集</i>。
     *
     * @param  c 要检查是否包含在此集合中的集合
     * @return 如果此集合包含指定集合中的所有元素，则返回 {@code true}
     * @throws NullPointerException 如果指定的集合为 null
     * @see #contains(Object)
     */
    public boolean containsAll(Collection<?> c) {
        return al.containsAll(c);
    }

    /**
     * 如果指定集合中的元素尚未存在于此集合中，则将它们全部添加到此集合中。
     * 如果指定的集合也是一个集合，则 {@code addAll} 操作实际上会修改此集合，
     * 使其值为两个集合的 <i>并集</i>。如果在操作进行过程中修改了指定的集合，
     * 则此操作的行为是未定义的。
     *
     * @param  c 包含要添加到此集合中的元素的集合
     * @return 如果此集合因调用而发生更改，则返回 {@code true}
     * @throws NullPointerException 如果指定的集合为 null
     * @see #add(Object)
     */
    public boolean addAll(Collection<? extends E> c) {
        return al.addAllAbsent(c) > 0;
    }

    /**
     * 从此集合中移除包含在指定集合中的所有元素。
     * 如果指定的集合也是一个集合，则此操作实际上会修改此集合，
     * 使其值为两个集合的 <i>非对称集合差</i>。
     *
     * @param  c 包含要从此集合中移除的元素的集合
     * @return 如果此集合因调用而发生更改，则返回 {@code true}
     * @throws ClassCastException 如果此集合中的元素类与指定集合不兼容（可选）
     * @throws NullPointerException 如果此集合包含 null 元素而指定集合不允许 null 元素（可选），
     *         或者如果指定的集合为 null
     * @see #remove(Object)
     */
    public boolean removeAll(Collection<?> c) {
        return al.removeAll(c);
    }

    /**
     * 仅保留此集合中包含在指定集合中的元素。
     * 换句话说，从此集合中移除所有不包含在指定集合中的元素。
     * 如果指定的集合也是一个集合，则此操作实际上会修改此集合，
     * 使其值为两个集合的 <i>交集</i>。
     *
     * @param  c 包含要保留在此集合中的元素的集合
     * @return 如果此集合因调用而发生更改，则返回 {@code true}
     * @throws ClassCastException 如果此集合中的元素类与指定集合不兼容（可选）
     * @throws NullPointerException 如果此集合包含 null 元素而指定集合不允许 null 元素（可选），
     *         或者如果指定的集合为 null
     * @see #remove(Object)
     */
    public boolean retainAll(Collection<?> c) {
        return al.retainAll(c);
    }

    /**
     * 返回一个迭代器，按元素添加的顺序遍历此集合中的元素。
     *
     * <p>返回的迭代器提供了一个此集合在迭代器构造时的状态快照。
     * 遍历迭代器时不需要同步。迭代器 <em>不支持</em> {@code remove} 方法。
     *
     * @return 一个遍历此集合中元素的迭代器
     */
    public Iterator<E> iterator() {
        return al.iterator();
    }

    /**
     * 将指定对象与此集合进行相等性比较。
     * 如果指定对象与此对象是同一个对象，或者它也是一个 {@link Set} 并且
     * 通过 {@linkplain Set#iterator() 迭代器} 遍历指定集合返回的元素
     * 与遍历此集合返回的元素相同，则返回 {@code true}。
     * 更正式地说，如果两个迭代器返回相同数量的元素，并且对于指定集合的迭代器返回的每个元素 {@code e1}，
     * 都存在一个此集合的迭代器返回的元素 {@code e2} 使得
     * {@code (e1==null ? e2==null : e1.equals(e2))}，则认为两个迭代器返回相同的元素。
     *
     * @param o 要与此集合进行相等性比较的对象
     * @return 如果指定对象与此集合相等，则返回 {@code true}
     */
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Set))
            return false;
        Set<?> set = (Set<?>)(o);
        Iterator<?> it = set.iterator();


        // 使用 O(n^2) 算法，仅适用于小型集合，而 CopyOnWriteArraySets 应该是小型的。

        // 使用底层数组的单个快照
        Object[] elements = al.getArray();
        int len = elements.length;
        // 标记已匹配的元素以避免重新检查
        boolean[] matched = new boolean[len];
        int k = 0;
        outer: while (it.hasNext()) {
            if (++k > len)
                return false;
            Object x = it.next();
            for (int i = 0; i < len; ++i) {
                if (!matched[i] && eq(x, elements[i])) {
                    matched[i] = true;
                    continue outer;
                }
            }
            return false;
        }
        return k == len;
    }

    public boolean removeIf(Predicate<? super E> filter) {
        return al.removeIf(filter);
    }

    public void forEach(Consumer<? super E> action) {
        al.forEach(action);
    }

    /**
     * 返回一个 {@link Spliterator}，遍历此集合中的元素，顺序为这些元素被添加的顺序。
     *
     * <p>该 {@code Spliterator} 报告 {@link Spliterator#IMMUTABLE}，
     * {@link Spliterator#DISTINCT}，{@link Spliterator#SIZED} 和
     * {@link Spliterator#SUBSIZED}。
     *
     * <p>该 spliterator 提供了在 spliterator 构造时集合状态的快照。
     * 在操作 spliterator 时不需要同步。
     *
     * @return 一个遍历此集合中元素的 {@code Spliterator}
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator
            (al.getArray(), Spliterator.IMMUTABLE | Spliterator.DISTINCT);
    }

    /**
     * 测试相等性，处理 null 值。
     */
    private static boolean eq(Object o1, Object o2) {
        return (o1 == null) ? o2 == null : o1.equals(o2);
    }
}
