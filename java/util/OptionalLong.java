
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

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * 一个可能包含或不包含 {@code long} 值的容器对象。
 * 如果包含值，{@code isPresent()} 将返回 {@code true}，并且 {@code getAsLong()} 将返回该值。
 *
 * <p>提供了一些依赖于值是否存在的方法，例如 {@link #orElse(long) orElse()}
 * （如果值不存在则返回默认值）和
 * {@link #ifPresent(java.util.function.LongConsumer) ifPresent()}（如果值存在则执行代码块）。
 *
 * <p>这是一个 <a href="../lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code OptionalLong} 实例使用基于身份的操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）可能会产生不可预测的结果，应避免使用。
 *
 * @since 1.8
 */
public final class OptionalLong {
    /**
     * 用于 {@code empty()} 的公共实例。
     */
    private static final OptionalLong EMPTY = new OptionalLong();

    /**
     * 如果为 true，则表示值存在，否则表示没有值存在。
     */
    private final boolean isPresent;
    private final long value;

    /**
     * 构造一个空实例。
     *
     * @implNote 通常每个 VM 只应存在一个空实例，即 {@link OptionalLong#EMPTY}。
     */
    private OptionalLong() {
        this.isPresent = false;
        this.value = 0;
    }

    /**
     * 返回一个空的 {@code OptionalLong} 实例。此 OptionalLong 没有值。
     *
     * @apiNote 尽管可能很诱人，但避免使用 {@code ==} 比较由
     * {@code Option.empty()} 返回的对象是否为空。不能保证它是单例。
     * 相反，应使用 {@link #isPresent()}。
     *
     *  @return 一个空的 {@code OptionalLong}。
     */
    public static OptionalLong empty() {
        return EMPTY;
    }

    /**
     * 构造一个包含值的实例。
     *
     * @param value 要包含的 long 值
     */
    private OptionalLong(long value) {
        this.isPresent = true;
        this.value = value;
    }

    /**
     * 返回一个包含指定值的 {@code OptionalLong}。
     *
     * @param value 要包含的值
     * @return 一个包含值的 {@code OptionalLong}
     */
    public static OptionalLong of(long value) {
        return new OptionalLong(value);
    }

    /**
     * 如果此 {@code OptionalLong} 中存在值，则返回该值，否则抛出 {@code NoSuchElementException}。
     *
     * @return 由此 {@code OptionalLong} 持有的值
     * @throws NoSuchElementException 如果没有值存在
     *
     * @see OptionalLong#isPresent()
     */
    public long getAsLong() {
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
    public void ifPresent(LongConsumer consumer) {
        if (isPresent)
            consumer.accept(value);
    }

    /**
     * 如果存在值，则返回该值，否则返回 {@code other}。
     *
     * @param other 如果没有值存在则返回的值
     * @return 如果存在值则返回该值，否则返回 {@code other}
     */
    public long orElse(long other) {
        return isPresent ? value : other;
    }

    /**
     * 如果存在值，则返回该值，否则调用 {@code other} 并返回其结果。
     *
     * @param other 一个 {@code LongSupplier}，如果不存在值则返回其结果
     * @return 如果存在值则返回该值，否则返回 {@code other.getAsLong()}
     * @throws NullPointerException 如果值不存在且 {@code other} 为 null
     */
    public long orElseGet(LongSupplier other) {
        return isPresent ? value : other.getAsLong();
    }

    /**
     * 如果存在值，则返回包含的值，否则抛出由提供的供应商创建的异常。
     *
     * @apiNote 可以使用带有空参数列表的异常构造函数的方法引用作为供应商。例如，
     * {@code IllegalStateException::new}
     *
     * @param <X> 要抛出的异常类型
     * @param exceptionSupplier 返回要抛出的异常的供应商
     * @return 当前值
     * @throws X 如果没有值存在
     * @throws NullPointerException 如果没有值存在且
     * {@code exceptionSupplier} 为 null
     */
    public<X extends Throwable> long orElseThrow(Supplier<X> exceptionSupplier) throws X {
        if (isPresent) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }


                /**
     * 表示是否有其他对象与这个 OptionalLong "相等"。如果满足以下条件之一，则认为其他对象相等：
     * <ul>
     * <li>它也是一个 {@code OptionalLong} 并且；
     * <li>两个实例都没有值存在，或者；
     * <li>存在的值通过 {@code ==} "相等"。
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

        if (!(obj instanceof OptionalLong)) {
            return false;
        }

        OptionalLong other = (OptionalLong) obj;
        return (isPresent && other.isPresent)
                ? value == other.value
                : isPresent == other.isPresent;
    }

    /**
     * 返回当前值的哈希码值，如果存在值的话，否则返回 0（零）。
     *
     * @return 当前值的哈希码值，如果不存在值则返回 0
     */
    @Override
    public int hashCode() {
        return isPresent ? Long.hashCode(value) : 0;
    }

    /**
     * {@inheritDoc}
     *
     * 返回一个非空的字符串表示，适用于调试。确切的表示格式未指定，可能在不同实现和版本之间有所不同。
     *
     * @implSpec 如果存在值，结果必须包含其字符串表示。空实例和存在值的实例必须能够明确区分。
     *
     * @return 此实例的字符串表示
     */
    @Override
    public String toString() {
        return isPresent
                ? String.format("OptionalLong[%s]", value)
                : "OptionalLong.empty";
    }
}
