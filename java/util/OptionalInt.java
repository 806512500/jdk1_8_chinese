/*
 * 版权所有 (c) 2012, 2013, Oracle 及/或其附属公司。保留所有权利。
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

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * 一个可能包含或不包含 {@code int} 值的容器对象。
 * 如果存在值，{@code isPresent()} 将返回 {@code true}，并且 {@code getAsInt()} 将返回该值。
 *
 * <p>提供了依赖于包含值存在或不存在的其他方法，例如 {@link #orElse(int) orElse()}
 * （如果值不存在则返回默认值）和
 * {@link #ifPresent(java.util.function.IntConsumer) ifPresent()}（如果值存在则执行代码块）。
 *
 * <p>这是一个 <a href="../lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code OptionalInt} 实例使用基于身份的操作（包括引用相等性
 * （{@code ==}）、身份哈希码或同步）可能会产生不可预测的结果，应避免使用。
 *
 * @since 1.8
 */
public final class OptionalInt {
    /**
     * 用于 {@code empty()} 的通用实例。
     */
    private static final OptionalInt EMPTY = new OptionalInt();

    /**
     * 如果为 true，则表示值存在，否则表示没有值存在。
     */
    private final boolean isPresent;
    private final int value;

    /**
     * 构造一个空实例。
     *
     * @implNote 通常每个 VM 只应存在一个空实例，{@link OptionalInt#EMPTY}。
     */
    private OptionalInt() {
        this.isPresent = false;
        this.value = 0;
    }

    /**
     * 返回一个空的 {@code OptionalInt} 实例。此 OptionalInt 没有值存在。
     *
     * @apiNote 尽管可能很诱人，但应避免通过与 {@code Option.empty()} 返回的实例进行 {@code ==} 比较来测试对象是否为空。
     * 没有保证它是单例。相反，应使用 {@link #isPresent()}。
     *
     *  @return 一个空的 {@code OptionalInt}
     */
    public static OptionalInt empty() {
        return EMPTY;
    }

    /**
     * 构造一个包含值的实例。
     *
     * @param value 要存在的 int 值
     */
    private OptionalInt(int value) {
        this.isPresent = true;
        this.value = value;
    }

    /**
     * 返回一个包含指定值的 {@code OptionalInt}。
     *
     * @param value 要存在的值
     * @return 一个包含值的 {@code OptionalInt}
     */
    public static OptionalInt of(int value) {
        return new OptionalInt(value);
    }

    /**
     * 如果此 {@code OptionalInt} 中存在值，则返回该值，否则抛出 {@code NoSuchElementException}。
     *
     * @return 由此 {@code OptionalInt} 持有的值
     * @throws NoSuchElementException 如果没有值存在
     *
     * @see OptionalInt#isPresent()
     */
    public int getAsInt() {
        if (!isPresent) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * 如果存在值，则返回 {@code true}，否则返回 {@code false}。
     *
     * @return 如果存在值则返回 {@code true}，否则返回 {@code false}
     */
    public boolean isPresent() {
        return isPresent;
    }

    /**
     * 如果存在值，则让指定的消费者接受该值，否则不执行任何操作。
     *
     * @param consumer 如果存在值则执行的代码块
     * @throws NullPointerException 如果值存在且 {@code consumer} 为 null
     */
    public void ifPresent(IntConsumer consumer) {
        if (isPresent)
            consumer.accept(value);
    }

    /**
     * 如果存在值，则返回该值，否则返回 {@code other}。
     *
     * @param other 如果没有值存在则返回的值
     * @return 如果存在值则返回该值，否则返回 {@code other}
     */
    public int orElse(int other) {
        return isPresent ? value : other;
    }

    /**
     * 如果存在值，则返回该值，否则调用 {@code other} 并返回其结果。
     *
     * @param other 一个 {@code IntSupplier}，如果不存在值则返回其结果
     * @return 如果存在值则返回该值，否则返回 {@code other.getAsInt()}
     * @throws NullPointerException 如果值不存在且 {@code other} 为 null
     */
    public int orElseGet(IntSupplier other) {
        return isPresent ? value : other.getAsInt();
    }

    /**
     * 如果存在值，则返回包含的值，否则抛出由提供的供应商创建的异常。
     *
     * @apiNote 可以使用对具有空参数列表的异常构造函数的方法引用作为供应商。例如，
     * {@code IllegalStateException::new}
     *
     * @param <X> 要抛出的异常类型
     * @param exceptionSupplier 返回要抛出的异常的供应商
     * @return 当前值
     * @throws X 如果没有值存在
     * @throws NullPointerException 如果没有值存在且 {@code exceptionSupplier} 为 null
     */
    public<X extends Throwable> int orElseThrow(Supplier<X> exceptionSupplier) throws X {
        if (isPresent) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * 指示其他对象是否“等于”此 OptionalInt。如果满足以下条件之一，则认为其他对象相等：
     * <ul>
     * <li>它也是一个 {@code OptionalInt} 并且；
     * <li>两个实例都没有值存在或；
     * <li>存在的值通过 {@code ==} 是“相等的”。
     * </ul>
     *
     * @param obj 要测试相等性的对象
     * @return 如果其他对象“等于”此对象则返回 {@code true}，否则返回 {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }


        if (!(obj instanceof OptionalInt)) {
            return false;
        }

        OptionalInt other = (OptionalInt) obj;
        return (isPresent && other.isPresent)
                ? value == other.value
                : isPresent == other.isPresent;
    }

    /**
     * 返回当前值的哈希码值（如果有），如果没有任何值，则返回 0（零）。
     *
     * @return 当前值的哈希码值，如果没有值则返回 0
     */
    @Override
    public int hashCode() {
        return isPresent ? Integer.hashCode(value) : 0;
    }

    /**
     * {@inheritDoc}
     *
     * 返回一个非空的字符串表示，适用于调试。确切的表示格式未指定，可能在不同实现和版本之间有所不同。
     *
     * @implSpec 如果存在值，结果必须包含其字符串表示。空实例和存在值的实例必须能够明确区分。
     *
     * @return 该实例的字符串表示
     */
    @Override
    public String toString() {
        return isPresent
                ? String.format("OptionalInt[%s]", value)
                : "OptionalInt.empty";
    }
}
