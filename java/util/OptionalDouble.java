
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

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * 一个可能包含或不包含 {@code double} 值的容器对象。
 * 如果包含值，{@code isPresent()} 将返回 {@code true} 并且 {@code getAsDouble()} 将返回该值。
 *
 * <p>提供了一些依赖于包含值存在或不存在的附加方法，例如 {@link #orElse(double) orElse()}
 * （如果值不存在则返回默认值）和
 * {@link #ifPresent(java.util.function.DoubleConsumer) ifPresent()}（如果值存在则执行代码块）。
 *
 * <p>这是一个 <a href="../lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code OptionalDouble} 实例使用基于身份的操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）可能会产生不可预测的结果，应避免使用。
 *
 * @since 1.8
 */
public final class OptionalDouble {
    /**
     * 用于 {@code empty()} 的公共实例。
     */
    private static final OptionalDouble EMPTY = new OptionalDouble();

    /**
     * 如果为 true，则表示值存在，否则表示没有值存在。
     */
    private final boolean isPresent;
    private final double value;

    /**
     * 构造一个空实例。
     *
     * @implNote 通常每个 VM 只应存在一个空实例，{@link OptionalDouble#EMPTY}。
     */
    private OptionalDouble() {
        this.isPresent = false;
        this.value = Double.NaN;
    }

    /**
     * 返回一个空的 {@code OptionalDouble} 实例。此 OptionalDouble 没有值。
     *
     * @apiNote 尽管可能很诱人，但避免使用 {@code ==} 比较由 {@code Option.empty()} 返回的实例来测试对象是否为空。
     * 没有保证它是单例。相反，使用 {@link #isPresent()}。
     *
     *  @return 一个空的 {@code OptionalDouble}。
     */
    public static OptionalDouble empty() {
        return EMPTY;
    }

    /**
     * 构造一个包含指定值的实例。
     *
     * @param value 要存在的 double 值。
     */
    private OptionalDouble(double value) {
        this.isPresent = true;
        this.value = value;
    }

    /**
     * 返回一个包含指定值的 {@code OptionalDouble}。
     *
     * @param value 要存在的值
     * @return 一个包含该值的 {@code OptionalDouble}
     */
    public static OptionalDouble of(double value) {
        return new OptionalDouble(value);
    }

    /**
     * 如果此 {@code OptionalDouble} 中存在值，则返回该值，否则抛出 {@code NoSuchElementException}。
     *
     * @return 由此 {@code OptionalDouble} 持有的值
     * @throws NoSuchElementException 如果没有值存在
     *
     * @see OptionalDouble#isPresent()
     */
    public double getAsDouble() {
        if (!isPresent) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * 如果存在值，则返回 {@code true}，否则返回 {@code false}。
     *
     * @return 如果存在值，则返回 {@code true}，否则返回 {@code false}
     */
    public boolean isPresent() {
        return isPresent;
    }

    /**
     * 如果存在值，则接受指定的消费者，否则不执行任何操作。
     *
     * @param consumer 如果存在值，则执行的代码块
     * @throws NullPointerException 如果值存在且 {@code consumer} 为 null
     */
    public void ifPresent(DoubleConsumer consumer) {
        if (isPresent)
            consumer.accept(value);
    }

    /**
     * 如果存在值，则返回该值，否则返回 {@code other}。
     *
     * @param other 如果没有值存在，则返回的值
     * @return 如果存在值，则返回该值，否则返回 {@code other}
     */
    public double orElse(double other) {
        return isPresent ? value : other;
    }

    /**
     * 如果存在值，则返回该值，否则调用 {@code other} 并返回其结果。
     *
     * @param other 一个 {@code DoubleSupplier}，如果不存在值，则返回其结果
     * @return 如果存在值，则返回该值，否则返回 {@code other.getAsDouble()}
     * @throws NullPointerException 如果值不存在且 {@code other} 为 null
     */
    public double orElseGet(DoubleSupplier other) {
        return isPresent ? value : other.getAsDouble();
    }

    /**
     * 如果存在值，则返回包含的值，否则抛出由提供的供应商创建的异常。
     *
     * @apiNote 可以使用带有空参数列表的异常构造函数的方法引用作为供应商。例如，
     * {@code IllegalStateException::new}
     *
     * @param <X> 要抛出的异常类型
     * @param exceptionSupplier 返回要抛出的异常的供应商
     * @return 存在的值
     * @throws X 如果没有值存在
     * @throws NullPointerException 如果没有值存在且
     * {@code exceptionSupplier} 为 null
     */
    public<X extends Throwable> double orElseThrow(Supplier<X> exceptionSupplier) throws X {
        if (isPresent) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }


/**
 * 表示其他某个对象是否与这个 OptionalDouble "相等"。如果满足以下条件之一，则认为其他对象相等：
 * <ul>
 * <li>它也是一个 {@code OptionalDouble} 并且；
 * <li>两个实例都没有值存在，或者；
 * <li>存在的值通过 {@code Double.compare() == 0} 认为是 "相等的"。
 * </ul>
 *
 * @param obj 要测试是否相等的对象
 * @return 如果其他对象与这个对象 "相等"，则返回 {@code true}，否则返回 {@code false}
 */
@Override
public boolean equals(Object obj) {
    if (this == obj) {
        return true;
    }

    if (!(obj instanceof OptionalDouble)) {
        return false;
    }

    OptionalDouble other = (OptionalDouble) obj;
    return (isPresent && other.isPresent)
           ? Double.compare(value, other.value) == 0
           : isPresent == other.isPresent;
}

/**
 * 返回当前值的哈希码值，如果存在值的话，否则返回 0（零）。
 *
 * @return 当前值的哈希码值，如果不存在值则返回 0
 */
@Override
public int hashCode() {
    return isPresent ? Double.hashCode(value) : 0;
}

/**
 * {@inheritDoc}
 *
 * 返回一个非空的字符串表示，适合调试。确切的表示格式未指定，可能在不同实现和版本之间有所不同。
 *
 * @implSpec 如果存在值，结果必须包含其字符串表示。空实例和存在值的实例必须能够明确区分。
 *
 * @return 此实例的字符串表示
 */
@Override
public String toString() {
    return isPresent
            ? String.format("OptionalDouble[%s]", value)
            : "OptionalDouble.empty";
}
