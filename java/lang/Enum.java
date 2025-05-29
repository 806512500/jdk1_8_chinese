
/*
 * 版权所有 (c) 2003, 2011, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.Serializable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;

/**
 * 这是所有 Java 语言枚举类型的基础类。
 *
 * 有关枚举的更多信息，包括编译器合成的隐式声明方法的描述，可以在
 * <cite>The Java&trade; Language Specification</cite> 的第 8.9 节中找到。
 *
 * <p> 注意，当使用枚举类型作为集合的类型或作为映射中的键类型时，可以使用专门化且高效的
 * {@linkplain java.util.EnumSet 集合} 和 {@linkplain
 * java.util.EnumMap 映射} 实现。
 *
 * @param <E> 枚举类型子类
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see     Class#getEnumConstants()
 * @see     java.util.EnumSet
 * @see     java.util.EnumMap
 * @since   1.5
 */
public abstract class Enum<E extends Enum<E>>
        implements Comparable<E>, Serializable {
    /**
     * 此枚举常量的名称，如枚举声明中所声明的。
     * 大多数程序员应使用 {@link #toString} 方法而不是访问此字段。
     */
    private final String name;

    /**
     * 返回此枚举常量的名称，完全如其枚举声明中所声明的。
     *
     * <b>大多数程序员应使用 {@link #toString} 方法，因为该方法可能返回
     * 更用户友好的名称。</b> 该方法主要用于需要获取准确名称的特殊情况下，该名称不会随版本而变化。
     *
     * @return 此枚举常量的名称
     */
    public final String name() {
        return name;
    }

    /**
     * 此枚举常量的序数（其在枚举声明中的位置，初始常量被分配为零）。
     *
     * 大多数程序员将不会使用此字段。它设计用于复杂的基于枚举的数据结构，如
     * {@link java.util.EnumSet} 和 {@link java.util.EnumMap}。
     */
    private final int ordinal;

    /**
     * 返回此枚举常量的序数（其在枚举声明中的位置，初始常量被分配为零）。
     *
     * 大多数程序员将不会使用此方法。它设计用于复杂的基于枚举的数据结构，如
     * {@link java.util.EnumSet} 和 {@link java.util.EnumMap}。
     *
     * @return 此枚举常量的序数
     */
    public final int ordinal() {
        return ordinal;
    }

    /**
     * 唯一构造函数。程序员不能调用此构造函数。
     * 它用于编译器在响应枚举类型声明时生成的代码。
     *
     * @param name - 此枚举常量的名称，即用于声明它的标识符。
     * @param ordinal - 此枚举常量的序数（其在枚举声明中的位置，初始常量被分配为零）。
     */
    protected Enum(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    /**
     * 返回此枚举常量的名称，如声明中所包含的。此方法可以被重写，但通常没有必要或不希望这样做。
     * 当存在更“程序员友好”的字符串形式时，枚举类型应重写此方法。
     *
     * @return 此枚举常量的名称
     */
    public String toString() {
        return name;
    }

    /**
     * 如果指定的对象等于此枚举常量，则返回 true。
     *
     * @param other 要与该对象进行相等性比较的对象。
     * @return  如果指定的对象等于此枚举常量，则返回 true。
     */
    public final boolean equals(Object other) {
        return this == other;
    }

    /**
     * 返回此枚举常量的哈希码。
     *
     * @return 此枚举常量的哈希码。
     */
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * 抛出 CloneNotSupportedException。这保证枚举永远不会被克隆，这对于保持它们的“单例”状态是必要的。
     *
     * @return (从不返回)
     */
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * 按顺序将此枚举与指定的对象进行比较。如果此对象小于、等于或大于指定的对象，则分别返回负整数、零或正整数。
     *
     * 枚举常量仅可与其他相同枚举类型的枚举常量进行比较。此方法实现的自然顺序是常量声明的顺序。
     */
    public final int compareTo(E o) {
        Enum<?> other = (Enum<?>) o;
        Enum<E> self = this;
        if (self.getClass() != other.getClass() && // 优化
            self.getDeclaringClass() != other.getDeclaringClass())
            throw new ClassCastException();
        return self.ordinal - other.ordinal;
    }

    /**
     * 返回与此枚举常量的枚举类型对应的 Class 对象。两个枚举常量 e1 和 e2 是相同枚举类型的
     * 当且仅当 e1.getDeclaringClass() == e2.getDeclaringClass()。
     * （此方法返回的值可能与具有常量特定类体的枚举常量的 {@link Object#getClass} 方法返回的值不同。）
     *
     * @return 与此枚举常量的枚举类型对应的 Class 对象
     */
    @SuppressWarnings("unchecked")
    public final Class<E> getDeclaringClass() {
        Class<?> clazz = getClass();
        Class<?> zuper = clazz.getSuperclass();
        return (zuper == Enum.class) ? (Class<E>) clazz : (Class<E>) zuper;
    }

                    /**
     * 返回指定枚举类型中具有指定名称的枚举常量。名称必须与用于声明此类型中的枚举常量的标识符完全匹配。（不允许有额外的空白字符。）
     *
     * <p>请注意，对于特定的枚举类型 {@code T}，可以使用该枚举上隐式声明的 {@code public static T valueOf(String)}
     * 方法来代替此方法，将名称映射到相应的枚举常量。通过调用该类型的隐式 {@code public static T[] values()} 方法，
     * 可以获得该枚举类型的所有常量。
     *
     * @param <T> 要返回的枚举类型的类型
     * @param enumType 要从中返回常量的枚举类型的 {@code Class} 对象
     * @param name 要返回的常量的名称
     * @return 指定枚举类型中具有指定名称的枚举常量
     * @throws IllegalArgumentException 如果指定的枚举类型没有具有指定名称的常量，或者指定的类对象不表示枚举类型
     * @throws NullPointerException 如果 {@code enumType} 或 {@code name} 为 null
     * @since 1.5
     */
    public static <T extends Enum<T>> T valueOf(Class<T> enumType,
                                                String name) {
        T result = enumType.enumConstantDirectory().get(name);
        if (result != null)
            return result;
        if (name == null)
            throw new NullPointerException("Name is null");
        throw new IllegalArgumentException(
            "No enum constant " + enumType.getCanonicalName() + "." + name);
    }

    /**
     * 枚举类不能有 finalize 方法。
     */
    protected final void finalize() { }

    /**
     * 防止默认的反序列化
     */
    private void readObject(ObjectInputStream in) throws IOException,
        ClassNotFoundException {
        throw new InvalidObjectException("can't deserialize enum");
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("can't deserialize enum");
    }
}
