
/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 一个可能包含非空值的容器对象。如果包含值，{@code isPresent()} 将返回 {@code true} 并且
 * {@code get()} 将返回该值。
 *
 * <p>提供了一些依赖于包含值存在或不存在的附加方法，例如
 * {@link #orElse(java.lang.Object) orElse()}（如果值不存在则返回默认值）和
 * {@link #ifPresent(java.util.function.Consumer) ifPresent()}（如果值存在则执行代码块）。
 *
 * <p>这是一个 <a href="../lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code Optional} 实例使用身份敏感操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）可能会产生不可预测的结果，应避免使用。
 *
 * @since 1.8
 */
public final class Optional<T> {
    /**
     * 用于 {@code empty()} 的公共实例。
     */
    private static final Optional<?> EMPTY = new Optional<>();

    /**
     * 如果非空，则为值；如果为空，则表示没有值。
     */
    private final T value;

    /**
     * 构造一个空实例。
     *
     * @implNote 通常每个 VM 只应存在一个空实例，{@link Optional#EMPTY}。
     */
    private Optional() {
        this.value = null;
    }

    /**
     * 返回一个空的 {@code Optional} 实例。此 {@code Optional} 没有值。
     *
     * @apiNote 尽管可能诱人，但避免通过与 {@code Option.empty()} 返回的实例
     * 比较 {@code ==} 来测试对象是否为空。没有保证它是单例。相反，使用 {@link #isPresent()}。
     *
     * @param <T> 不存在值的类型
     * @return 一个空的 {@code Optional}
     */
    public static<T> Optional<T> empty() {
        @SuppressWarnings("unchecked")
        Optional<T> t = (Optional<T>) EMPTY;
        return t;
    }

    /**
     * 使用存在的值构造一个实例。
     *
     * @param value 必须存在的非空值
     * @throws NullPointerException 如果值为 null
     */
    private Optional(T value) {
        this.value = Objects.requireNonNull(value);
    }

    /**
     * 返回一个包含指定存在的非空值的 {@code Optional}。
     *
     * @param <T> 值的类型
     * @param value 必须存在的值
     * @return 一个包含值的 {@code Optional}
     * @throws NullPointerException 如果值为 null
     */
    public static <T> Optional<T> of(T value) {
        return new Optional<>(value);
    }

    /**
     * 返回一个描述指定值的 {@code Optional}，如果值为非空，否则返回一个空的 {@code Optional}。
     *
     * @param <T> 值的类型
     * @param value 可能为空的值
     * @return 一个包含值的 {@code Optional}，如果指定值为非空，否则一个空的 {@code Optional}
     */
    public static <T> Optional<T> ofNullable(T value) {
        return value == null ? empty() : of(value);
    }

    /**
     * 如果此 {@code Optional} 包含值，返回该值，否则抛出 {@code NoSuchElementException}。
     *
     * @return 由此 {@code Optional} 持有的非空值
     * @throws NoSuchElementException 如果没有值存在
     *
     * @see Optional#isPresent()
     */
    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * 如果存在值，返回 {@code true}，否则返回 {@code false}。
     *
     * @return 如果存在值则返回 {@code true}，否则返回 {@code false}
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * 如果存在值，使用该值调用指定的消费者，否则不执行任何操作。
     *
     * @param consumer 如果存在值则执行的代码块
     * @throws NullPointerException 如果值存在且 {@code consumer} 为 null
     */
    public void ifPresent(Consumer<? super T> consumer) {
        if (value != null)
            consumer.accept(value);
    }

    /**
     * 如果存在值，并且该值匹配给定的谓词，则返回一个描述该值的 {@code Optional}，否则返回一个空的 {@code Optional}。
     *
     * @param predicate 如果存在值则应用的谓词
     * @return 一个描述此 {@code Optional} 值的 {@code Optional}，如果存在值且值匹配给定的谓词，否则一个空的 {@code Optional}
     * @throws NullPointerException 如果谓词为 null
     */
    public Optional<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent())
            return this;
        else
            return predicate.test(value) ? this : empty();
    }


                /**
     * 如果存在值，应用提供的映射函数，如果结果非空，则返回描述结果的 {@code Optional}。否则返回空的 {@code Optional}。
     *
     * @apiNote 此方法支持对可选值进行后处理，无需显式检查返回状态。例如，以下代码遍历文件名流，选择一个尚未处理的文件名，然后打开该文件，返回一个 {@code Optional<FileInputStream>}：
     *
     * <pre>{@code
     *     Optional<FileInputStream> fis =
     *         names.stream().filter(name -> !isProcessedYet(name))
     *                       .findFirst()
     *                       .map(name -> new FileInputStream(name));
     * }</pre>
     *
     * 在这里，{@code findFirst} 返回一个 {@code Optional<String>}，然后 {@code map} 返回一个描述所需文件的 {@code Optional<FileInputStream>}（如果存在）。
     *
     * @param <U> 映射函数结果的类型
     * @param mapper 如果存在值，则应用的映射函数
     * @return 如果存在值，则描述应用映射函数结果的 {@code Optional}，否则返回空的 {@code Optional}
     * @throws NullPointerException 如果映射函数为 null
     */
    public<U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return empty();
        else {
            return Optional.ofNullable(mapper.apply(value));
        }
    }

    /**
     * 如果存在值，应用提供的返回 {@code Optional} 的映射函数，返回该结果，否则返回空的 {@code Optional}。此方法类似于 {@link #map(Function)}，
     * 但提供的映射函数的结果已经是一个 {@code Optional}，如果调用，{@code flatMap} 不会再用额外的 {@code Optional} 包装它。
     *
     * @param <U> 返回的 {@code Optional} 的类型参数
     * @param mapper 如果存在值，则应用的映射函数
     * @return 如果存在值，则返回应用返回 {@code Optional} 的映射函数的结果，否则返回空的 {@code Optional}
     * @throws NullPointerException 如果映射函数为 null 或返回 null 结果
     */
    public<U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return empty();
        else {
            return Objects.requireNonNull(mapper.apply(value));
        }
    }

    /**
     * 如果存在值，返回该值，否则返回 {@code other}。
     *
     * @param other 如果没有值存在，则返回的值，可以为 null
     * @return 如果存在值，则返回该值，否则返回 {@code other}
     */
    public T orElse(T other) {
        return value != null ? value : other;
    }

    /**
     * 如果存在值，返回该值，否则调用 {@code other} 并返回其结果。
     *
     * @param other 如果没有值存在，则调用的 {@code Supplier}
     * @return 如果存在值，则返回该值，否则返回 {@code other.get()} 的结果
     * @throws NullPointerException 如果没有值存在且 {@code other} 为 null
     */
    public T orElseGet(Supplier<? extends T> other) {
        return value != null ? value : other.get();
    }

    /**
     * 如果存在值，返回包含的值，否则抛出由提供的供应商创建的异常。
     *
     * @apiNote 可以使用带有空参数列表的异常构造函数的方法引用来作为供应商。例如，{@code IllegalStateException::new}
     *
     * @param <X> 要抛出的异常类型
     * @param exceptionSupplier 返回要抛出的异常的供应商
     * @return 当前值
     * @throws X 如果没有值存在
     * @throws NullPointerException 如果没有值存在且 {@code exceptionSupplier} 为 null
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * 指示其他对象是否与这个 Optional "相等"。如果满足以下条件之一，则认为其他对象相等：
     * <ul>
     * <li>它也是一个 {@code Optional} 并且；
     * <li>两个实例都没有值存在或；
     * <li>存在的值通过 {@code equals()} 方法 "相等"。
     * </ul>
     *
     * @param obj 要测试相等性的对象
     * @return 如果其他对象与这个对象 "相等"，则返回 {@code true}，否则返回 {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Optional)) {
            return false;
        }

        Optional<?> other = (Optional<?>) obj;
        return Objects.equals(value, other.value);
    }

    /**
     * 返回当前值的哈希码值，如果不存在值，则返回 0（零）。
     *
     * @return 如果存在值，则返回其哈希码值，否则返回 0
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * 返回一个非空的字符串表示，适合调试。确切的表示格式未指定，可能在实现和版本之间有所不同。
     *
     * @implSpec 如果存在值，结果必须包含其字符串表示。空的和存在的 Optionals 必须能够明确区分。
     *
     * @return 此实例的字符串表示
     */
    @Override
    public String toString() {
        return value != null
            ? String.format("Optional[%s]", value)
            : "Optional.empty";
    }
}
