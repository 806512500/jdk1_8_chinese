
/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * 原始类型的 {@code Iterator} 的基础类型。提供了 {@link OfInt int}、{@link OfLong long} 和
 * {@link OfDouble double} 值的专用子类型。
 *
 * <p>专用子类型的默认实现 {@link Iterator#next} 和 {@link Iterator#forEachRemaining(java.util.function.Consumer)}
 * 会将原始值装箱为相应包装类的实例。这种装箱可能会抵消使用原始类型特化带来的任何优势。为了避免装箱，应使用相应的基于原始类型的方法。例如，应优先使用
 * {@link PrimitiveIterator.OfInt#nextInt()} 和 {@link PrimitiveIterator.OfInt#forEachRemaining(java.util.function.IntConsumer)}
 * 而不是 {@link PrimitiveIterator.OfInt#next()} 和 {@link PrimitiveIterator.OfInt#forEachRemaining(java.util.function.Consumer)}。
 *
 * <p>使用装箱方法 {@link Iterator#next next()} 和
 * {@link Iterator#forEachRemaining(java.util.function.Consumer) forEachRemaining()}
 * 迭代原始值不会影响遇到的值（转换为装箱值）的顺序。
 *
 * @implNote
 * 如果布尔系统属性 {@code org.openjdk.java.util.stream.tripwire} 设置为 {@code true}，则会在对原始类型子类型特化进行操作时发生装箱时报告诊断警告。
 *
 * @param <T> 此 PrimitiveIterator 返回的元素类型。该类型必须是原始类型的包装类型，例如 {@code Integer} 对于原始类型 {@code int}。
 * @param <T_CONS> 原始消费者类型。该类型必须是 {@link java.util.function.Consumer} 的原始类型特化，例如 {@link java.util.function.IntConsumer} 对于 {@code Integer}。
 *
 * @since 1.8
 */
public interface PrimitiveIterator<T, T_CONS> extends Iterator<T> {

    /**
     * 对每个剩余元素执行给定操作，按迭代时元素出现的顺序，直到所有元素都被处理或操作抛出异常。操作抛出的错误或运行时异常将传递给调用者。
     *
     * @param action 要为每个元素执行的操作
     * @throws NullPointerException 如果指定的操作为 null
     */
    @SuppressWarnings("overloads")
    void forEachRemaining(T_CONS action);

    /**
     * 专门用于 {@code int} 值的 Iterator。
     * @since 1.8
     */
    public static interface OfInt extends PrimitiveIterator<Integer, IntConsumer> {

        /**
         * 返回迭代中的下一个 {@code int} 元素。
         *
         * @return 迭代中的下一个 {@code int} 元素
         * @throws NoSuchElementException 如果迭代没有更多元素
         */
        int nextInt();

        /**
         * 对每个剩余元素执行给定操作，直到所有元素都被处理或操作抛出异常。操作按迭代顺序执行，如果指定了迭代顺序。操作抛出的异常将传递给调用者。
         *
         * @implSpec
         * <p>默认实现的行为类似于：
         * <pre>{@code
         *     while (hasNext())
         *         action.accept(nextInt());
         * }</pre>
         *
         * @param action 要为每个元素执行的操作
         * @throws NullPointerException 如果指定的操作为 null
         */
        default void forEachRemaining(IntConsumer action) {
            Objects.requireNonNull(action);
            while (hasNext())
                action.accept(nextInt());
        }

        /**
         * {@inheritDoc}
         * @implSpec
         * 默认实现调用 {@link #nextInt()} 并返回装箱结果。
         */
        @Override
        default Integer next() {
            if (Tripwire.ENABLED)
                Tripwire.trip(getClass(), "{0} calling PrimitiveIterator.OfInt.nextInt()");
            return nextInt();
        }

        /**
         * {@inheritDoc}
         * @implSpec
         * 如果操作是 {@code IntConsumer} 的实例，则将其转换为 {@code IntConsumer} 并传递给 {@link #forEachRemaining}；
         * 否则，将操作适配为 {@code IntConsumer} 的实例，通过装箱 {@code IntConsumer} 的参数，然后传递给 {@link #forEachRemaining}。
         */
        @Override
        default void forEachRemaining(Consumer<? super Integer> action) {
            if (action instanceof IntConsumer) {
                forEachRemaining((IntConsumer) action);
            }
            else {
                // 方法引用 action::accept 永远不会为 null
                Objects.requireNonNull(action);
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(), "{0} calling PrimitiveIterator.OfInt.forEachRemainingInt(action::accept)");
                forEachRemaining((IntConsumer) action::accept);
            }
        }


                }

    /**
     * 专门用于 {@code long} 值的迭代器。
     * @since 1.8
     */
    public static interface OfLong extends PrimitiveIterator<Long, LongConsumer> {

        /**
         * 返回迭代中的下一个 {@code long} 元素。
         *
         * @return 迭代中的下一个 {@code long} 元素
         * @throws NoSuchElementException 如果迭代没有更多元素
         */
        long nextLong();

        /**
         * 对每个剩余元素执行给定的操作，直到所有元素都被处理或操作抛出异常。如果指定了迭代顺序，则按该顺序执行操作。操作中抛出的异常将传递给调用者。
         *
         * @implSpec
         * <p>默认实现的行为类似于：
         * <pre>{@code
         *     while (hasNext())
         *         action.accept(nextLong());
         * }</pre>
         *
         * @param action 要为每个元素执行的操作
         * @throws NullPointerException 如果指定的操作为 null
         */
        default void forEachRemaining(LongConsumer action) {
            Objects.requireNonNull(action);
            while (hasNext())
                action.accept(nextLong());
        }

        /**
         * {@inheritDoc}
         * @implSpec
         * 默认实现调用 {@link #nextLong()} 并返回该结果的装箱版本。
         */
        @Override
        default Long next() {
            if (Tripwire.ENABLED)
                Tripwire.trip(getClass(), "{0} calling PrimitiveIterator.OfLong.nextLong()");
            return nextLong();
        }

        /**
         * {@inheritDoc}
         * @implSpec
         * 如果操作是 {@code LongConsumer} 的实例，则将其转换为 {@code LongConsumer} 并传递给 {@link #forEachRemaining}；
         * 否则，将操作适配为 {@code LongConsumer} 的实例，通过装箱 {@code LongConsumer} 的参数，然后传递给 {@link #forEachRemaining}。
         */
        @Override
        default void forEachRemaining(Consumer<? super Long> action) {
            if (action instanceof LongConsumer) {
                forEachRemaining((LongConsumer) action);
            }
            else {
                // 方法引用 action::accept 永远不会为 null
                Objects.requireNonNull(action);
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(), "{0} calling PrimitiveIterator.OfLong.forEachRemainingLong(action::accept)");
                forEachRemaining((LongConsumer) action::accept);
            }
        }
    }

    /**
     * 专门用于 {@code double} 值的迭代器。
     * @since 1.8
     */
    public static interface OfDouble extends PrimitiveIterator<Double, DoubleConsumer> {

        /**
         * 返回迭代中的下一个 {@code double} 元素。
         *
         * @return 迭代中的下一个 {@code double} 元素
         * @throws NoSuchElementException 如果迭代没有更多元素
         */
        double nextDouble();

        /**
         * 对每个剩余元素执行给定的操作，直到所有元素都被处理或操作抛出异常。如果指定了迭代顺序，则按该顺序执行操作。操作中抛出的异常将传递给调用者。
         *
         * @implSpec
         * <p>默认实现的行为类似于：
         * <pre>{@code
         *     while (hasNext())
         *         action.accept(nextDouble());
         * }</pre>
         *
         * @param action 要为每个元素执行的操作
         * @throws NullPointerException 如果指定的操作为 null
         */
        default void forEachRemaining(DoubleConsumer action) {
            Objects.requireNonNull(action);
            while (hasNext())
                action.accept(nextDouble());
        }

        /**
         * {@inheritDoc}
         * @implSpec
         * 默认实现调用 {@link #nextDouble()} 并返回该结果的装箱版本。
         */
        @Override
        default Double next() {
            if (Tripwire.ENABLED)
                Tripwire.trip(getClass(), "{0} calling PrimitiveIterator.OfDouble.nextLong()");
            return nextDouble();
        }

        /**
         * {@inheritDoc}
         * @implSpec
         * 如果操作是 {@code DoubleConsumer} 的实例，则将其转换为 {@code DoubleConsumer} 并传递给 {@link #forEachRemaining}；
         * 否则，将操作适配为 {@code DoubleConsumer} 的实例，通过装箱 {@code DoubleConsumer} 的参数，然后传递给 {@link #forEachRemaining}。
         */
        @Override
        default void forEachRemaining(Consumer<? super Double> action) {
            if (action instanceof DoubleConsumer) {
                forEachRemaining((DoubleConsumer) action);
            }
            else {
                // 方法引用 action::accept 永远不会为 null
                Objects.requireNonNull(action);
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(), "{0} calling PrimitiveIterator.OfDouble.forEachRemainingDouble(action::accept)");
                forEachRemaining((DoubleConsumer) action::accept);
            }
        }
    }
}
