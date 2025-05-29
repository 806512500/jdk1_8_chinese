
/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其关联公司。保留所有权利。
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

import sun.misc.SharedSecrets;

/**
 * 一个专门用于枚举类型的 {@link Set} 实现。枚举集中的所有元素都必须来自在创建集合时（显式或隐式）指定的单个枚举类型。枚举集在内部以位向量的形式表示。这种表示方式非常紧凑和高效。此类的空间和时间性能应该足够好，可以作为传统 <tt>int</tt>-based "位标志" 的高质量、类型安全的替代品。即使批量操作（如 <tt>containsAll</tt> 和 <tt>retainAll</tt>）如果其参数也是枚举集，应该运行得非常快。
 *
 * <p>由 <tt>iterator</tt> 方法返回的迭代器遍历元素的 <i>自然顺序</i>（枚举常量声明的顺序）。返回的迭代器是 <i>弱一致的</i>：它永远不会抛出 {@link ConcurrentModificationException}，并且可能或可能不会显示在迭代过程中对集合进行的任何修改的效果。
 *
 * <p>不允许空元素。尝试插入空元素将抛出 {@link NullPointerException}。然而，尝试测试空元素的存在或移除空元素将正常工作。
 *
 * <P>像大多数集合实现一样，<tt>EnumSet</tt> 不是同步的。如果多个线程同时访问枚举集，并且至少有一个线程修改了集合，应该在外部进行同步。这通常通过在一些自然封装枚举集的对象上进行同步来实现。如果不存在这样的对象，应该使用 {@link Collections#synchronizedSet} 方法“包装”集合。最好在创建时完成，以防止意外的未同步访问：
 *
 * <pre>
 * Set&lt;MyEnum&gt; s = Collections.synchronizedSet(EnumSet.noneOf(MyEnum.class));
 * </pre>
 *
 * <p>实现说明：所有基本操作都在常数时间内执行。它们可能（但不保证）比 {@link HashSet} 的对应操作快得多。即使批量操作如果其参数也是枚举集，也在常数时间内执行。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @author Josh Bloch
 * @since 1.5
 * @see EnumMap
 * @serial 排除
 */
public abstract class EnumSet<E extends Enum<E>> extends AbstractSet<E>
    implements Cloneable, java.io.Serializable
{
    /**
     * 此集合中所有元素的类。
     */
    final Class<E> elementType;

    /**
     * 组成 T 的所有值。 （为提高性能而缓存。）
     */
    final Enum<?>[] universe;

    private static Enum<?>[] ZERO_LENGTH_ENUM_ARRAY = new Enum<?>[0];

    EnumSet(Class<E> elementType, Enum<?>[] universe) {
        this.elementType = elementType;
        this.universe    = universe;
    }

    /**
     * 创建一个具有指定元素类型的空枚举集。
     *
     * @param <E> 集合中元素的类
     * @param elementType 此枚举集元素类型的类对象
     * @return 指定类型的空枚举集。
     * @throws NullPointerException 如果 <tt>elementType</tt> 为 null
     */
    public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType) {
        Enum<?>[] universe = getUniverse(elementType);
        if (universe == null)
            throw new ClassCastException(elementType + " 不是枚举");

        if (universe.length <= 64)
            return new RegularEnumSet<>(elementType, universe);
        else
            return new JumboEnumSet<>(elementType, universe);
    }

    /**
     * 创建一个包含指定元素类型中所有元素的枚举集。
     *
     * @param <E> 集合中元素的类
     * @param elementType 此枚举集元素类型的类对象
     * @return 包含指定类型中所有元素的枚举集。
     * @throws NullPointerException 如果 <tt>elementType</tt> 为 null
     */
    public static <E extends Enum<E>> EnumSet<E> allOf(Class<E> elementType) {
        EnumSet<E> result = noneOf(elementType);
        result.addAll();
        return result;
    }

    /**
     * 将适当枚举类型的所有元素添加到此枚举集中，调用前此枚举集为空。
     */
    abstract void addAll();

    /**
     * 创建一个与指定枚举集具有相同元素类型且初始包含相同元素（如果有）的枚举集。
     *
     * @param <E> 集合中元素的类
     * @param s 用于初始化此枚举集的枚举集
     * @return 指定枚举集的副本。
     * @throws NullPointerException 如果 <tt>s</tt> 为 null
     */
    public static <E extends Enum<E>> EnumSet<E> copyOf(EnumSet<E> s) {
        return s.clone();
    }

    /**
     * 从指定集合初始化枚举集。如果指定的集合是 <tt>EnumSet</tt> 实例，此静态工厂方法的行为与 {@link #copyOf(EnumSet)} 完全相同。否则，指定的集合必须至少包含一个元素（以确定新枚举集的元素类型）。
     *
     * @param <E> 集合中元素的类
     * @param c 用于初始化此枚举集的集合
     * @return 从给定集合初始化的枚举集。
     * @throws IllegalArgumentException 如果 <tt>c</tt> 不是 <tt>EnumSet</tt> 实例且不包含任何元素
     * @throws NullPointerException 如果 <tt>c</tt> 为 null
     */
    public static <E extends Enum<E>> EnumSet<E> copyOf(Collection<E> c) {
        if (c instanceof EnumSet) {
            return ((EnumSet<E>)c).clone();
        } else {
            if (c.isEmpty())
                throw new IllegalArgumentException("集合为空");
            Iterator<E> i = c.iterator();
            E first = i.next();
            EnumSet<E> result = EnumSet.of(first);
            while (i.hasNext())
                result.add(i.next());
            return result;
        }
    }


                /**
     * 创建一个与指定枚举集具有相同元素类型的枚举集，最初包含此类型中
     * <i>不</i> 包含在指定集中的所有元素。
     *
     * @param <E> 枚举集中元素的类
     * @param s 用于初始化此枚举集的补集的枚举集
     * @return 指定集在此集中的补集
     * @throws NullPointerException 如果 <tt>s</tt> 为 null
     */
    public static <E extends Enum<E>> EnumSet<E> complementOf(EnumSet<E> s) {
        EnumSet<E> result = copyOf(s);
        result.complement();
        return result;
    }

    /**
     * 创建一个最初包含指定元素的枚举集。
     *
     * 本方法存在重载，用于初始化包含一到五个元素的枚举集。还提供了一个使用可变参数特性的重载。
     * 此重载可用于创建最初包含任意数量元素的枚举集，但可能比不使用可变参数的重载运行得慢。
     *
     * @param <E> 指定元素和集合的类
     * @param e 该集合最初应包含的元素
     * @throws NullPointerException 如果 <tt>e</tt> 为 null
     * @return 一个最初包含指定元素的枚举集
     */
    public static <E extends Enum<E>> EnumSet<E> of(E e) {
        EnumSet<E> result = noneOf(e.getDeclaringClass());
        result.add(e);
        return result;
    }

    /**
     * 创建一个最初包含指定元素的枚举集。
     *
     * 本方法存在重载，用于初始化包含一到五个元素的枚举集。还提供了一个使用可变参数特性的重载。
     * 此重载可用于创建最初包含任意数量元素的枚举集，但可能比不使用可变参数的重载运行得慢。
     *
     * @param <E> 参数元素和集合的类
     * @param e1 该集合最初应包含的元素
     * @param e2 该集合最初应包含的另一个元素
     * @throws NullPointerException 如果任何参数为 null
     * @return 一个最初包含指定元素的枚举集
     */
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2) {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        return result;
    }

    /**
     * 创建一个最初包含指定元素的枚举集。
     *
     * 本方法存在重载，用于初始化包含一到五个元素的枚举集。还提供了一个使用可变参数特性的重载。
     * 此重载可用于创建最初包含任意数量元素的枚举集，但可能比不使用可变参数的重载运行得慢。
     *
     * @param <E> 参数元素和集合的类
     * @param e1 该集合最初应包含的元素
     * @param e2 该集合最初应包含的另一个元素
     * @param e3 该集合最初应包含的另一个元素
     * @throws NullPointerException 如果任何参数为 null
     * @return 一个最初包含指定元素的枚举集
     */
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3) {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        result.add(e3);
        return result;
    }

    /**
     * 创建一个最初包含指定元素的枚举集。
     *
     * 本方法存在重载，用于初始化包含一到五个元素的枚举集。还提供了一个使用可变参数特性的重载。
     * 此重载可用于创建最初包含任意数量元素的枚举集，但可能比不使用可变参数的重载运行得慢。
     *
     * @param <E> 参数元素和集合的类
     * @param e1 该集合最初应包含的元素
     * @param e2 该集合最初应包含的另一个元素
     * @param e3 该集合最初应包含的另一个元素
     * @param e4 该集合最初应包含的另一个元素
     * @throws NullPointerException 如果任何参数为 null
     * @return 一个最初包含指定元素的枚举集
     */
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4) {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        result.add(e3);
        result.add(e4);
        return result;
    }

    /**
     * 创建一个最初包含指定元素的枚举集。
     *
     * 本方法存在重载，用于初始化包含一到五个元素的枚举集。还提供了一个使用可变参数特性的重载。
     * 此重载可用于创建最初包含任意数量元素的枚举集，但可能比不使用可变参数的重载运行得慢。
     *
     * @param <E> 参数元素和集合的类
     * @param e1 该集合最初应包含的元素
     * @param e2 该集合最初应包含的另一个元素
     * @param e3 该集合最初应包含的另一个元素
     * @param e4 该集合最初应包含的另一个元素
     * @param e5 该集合最初应包含的另一个元素
     * @throws NullPointerException 如果任何参数为 null
     * @return 一个最初包含指定元素的枚举集
     */
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4,
                                                    E e5)
    {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        result.add(e3);
        result.add(e4);
        result.add(e5);
        return result;
    }

                /**
     * 创建一个枚举集，该集最初包含指定的元素。
     * 该工厂方法使用可变参数列表，可以用来创建一个枚举集，最初包含任意数量的元素，
     * 但其执行速度可能比不使用可变参数的重载方法慢。
     *
     * @param <E> 参数元素和集合的类
     * @param first 集合最初应包含的一个元素
     * @param rest 集合最初应包含的其余元素
     * @throws NullPointerException 如果指定的任何元素为 null，或者 <tt>rest</tt> 为 null
     * @return 一个枚举集，最初包含指定的元素
     */
    @SafeVarargs
    public static <E extends Enum<E>> EnumSet<E> of(E first, E... rest) {
        EnumSet<E> result = noneOf(first.getDeclaringClass());
        result.add(first);
        for (E e : rest)
            result.add(e);
        return result;
    }

    /**
     * 创建一个枚举集，该集最初包含由两个指定端点定义的范围内的所有元素。返回的集合将包含端点本身，
     * 这些端点可以相同但不能无序。
     *
     * @param <E> 参数元素和集合的类
     * @param from 范围中的第一个元素
     * @param to 范围中的最后一个元素
     * @throws NullPointerException 如果 {@code from} 或 {@code to} 为 null
     * @throws IllegalArgumentException 如果 {@code from.compareTo(to) > 0}
     * @return 一个枚举集，最初包含由两个指定端点定义的范围内的所有元素
     */
    public static <E extends Enum<E>> EnumSet<E> range(E from, E to) {
        if (from.compareTo(to) > 0)
            throw new IllegalArgumentException(from + " > " + to);
        EnumSet<E> result = noneOf(from.getDeclaringClass());
        result.addRange(from, to);
        return result;
    }

    /**
     * 将指定的范围添加到此枚举集，该集在调用之前为空。
     */
    abstract void addRange(E from, E to);

    /**
     * 返回此集合的副本。
     *
     * @return 此集合的副本
     */
    @SuppressWarnings("unchecked")
    public EnumSet<E> clone() {
        try {
            return (EnumSet<E>) super.clone();
        } catch(CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 补充此枚举集的内容。
     */
    abstract void complement();

    /**
     * 如果 e 不是此枚举集的正确类型，则抛出异常。
     */
    final void typeCheck(E e) {
        Class<?> eClass = e.getClass();
        if (eClass != elementType && eClass.getSuperclass() != elementType)
            throw new ClassCastException(eClass + " != " + elementType);
    }

    /**
     * 返回组成 E 的所有值。
     * 结果未克隆，缓存并由所有调用者共享。
     */
    private static <E extends Enum<E>> E[] getUniverse(Class<E> elementType) {
        return SharedSecrets.getJavaLangAccess()
                                        .getEnumConstantsShared(elementType);
    }

    /**
     * 此类用于序列化所有 EnumSet 实例，无论实现类型如何。它捕获它们的“逻辑内容”，
     * 并使用公共静态工厂方法重建。这是必要的，以确保特定实现类型的存在是一个实现细节。
     *
     * @serial include
     */
    private static class SerializationProxy <E extends Enum<E>>
        implements java.io.Serializable
    {
        /**
         * 此枚举集的元素类型。
         *
         * @serial
         */
        private final Class<E> elementType;

        /**
         * 此枚举集包含的元素。
         *
         * @serial
         */
        private final Enum<?>[] elements;

        SerializationProxy(EnumSet<E> set) {
            elementType = set.elementType;
            elements = set.toArray(ZERO_LENGTH_ENUM_ARRAY);
        }

        // 为了防止伪造流的注入，我们或许应该使用 elementType.cast() 而不是转换为 E，但这会减慢实现速度
        @SuppressWarnings("unchecked")
        private Object readResolve() {
            EnumSet<E> result = EnumSet.noneOf(elementType);
            for (Enum<?> e : elements)
                result.add((E)e);
            return result;
        }

        private static final long serialVersionUID = 362491234563181265L;
    }

    Object writeReplace() {
        return new SerializationProxy<>(this);
    }

    // 用于序列化代理模式的 readObject 方法
    // 参见《Effective Java》，第二版，项目 78。
    private void readObject(java.io.ObjectInputStream stream)
        throws java.io.InvalidObjectException {
        throw new java.io.InvalidObjectException("Proxy required");
    }
}
