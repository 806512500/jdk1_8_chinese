/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款的约束。
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

import java.util.function.Consumer;

/**
 * 一个用于遍历集合的迭代器。{@code Iterator} 在 Java 集合框架中取代了
 * {@link Enumeration}。迭代器与枚举器在两个方面有所不同：
 *
 * <ul>
 *      <li> 迭代器允许调用者在迭代过程中以明确定义的语义从底层集合中移除元素。
 *      <li> 方法名称得到了改进。
 * </ul>
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a>的成员。
 *
 * @param <E> 此迭代器返回的元素类型
 *
 * @author  Josh Bloch
 * @see Collection
 * @see ListIterator
 * @see Iterable
 * @since 1.2
 */
public interface Iterator<E> {
    /**
     * 如果迭代还有更多元素，则返回 {@code true}。
     * （换句话说，如果 {@link #next} 会返回一个元素而不是抛出异常，则返回 {@code true}。）
     *
     * @return 如果迭代还有更多元素，则返回 {@code true}
     */
    boolean hasNext();

    /**
     * 返回迭代中的下一个元素。
     *
     * @return 迭代中的下一个元素
     * @throws NoSuchElementException 如果迭代没有更多元素
     */
    E next();

    /**
     * 从此迭代器返回的最后一个元素中移除底层集合中的元素（可选操作）。此方法只能在每次调用 {@link #next} 之后调用一次。如果在迭代过程中以任何方式修改了底层集合（通过调用此方法除外），则迭代器的行为是未指定的。
     *
     * @implSpec
     * 默认实现抛出一个 {@link UnsupportedOperationException} 实例，并不执行其他操作。
     *
     * @throws UnsupportedOperationException 如果此迭代器不支持 {@code remove} 操作
     *
     * @throws IllegalStateException 如果尚未调用 {@code next} 方法，或者在上次调用 {@code next} 方法之后已经调用了 {@code remove} 方法
     */
    default void remove() {
        throw new UnsupportedOperationException("remove");
    }

    /**
     * 对每个剩余元素执行给定的操作，直到所有元素都被处理或操作抛出异常。如果指定了迭代顺序，则按该顺序执行操作。操作抛出的异常将传递给调用者。
     *
     * @implSpec
     * <p>默认实现的行为类似于：
     * <pre>{@code
     *     while (hasNext())
     *         action.accept(next());
     * }</pre>
     *
     * @param action 要为每个元素执行的操作
     * @throws NullPointerException 如果指定的操作为 null
     * @since 1.8
     */
    default void forEachRemaining(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        while (hasNext())
            action.accept(next());
    }
}