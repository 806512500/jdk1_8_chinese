
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
package java.util.stream;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;

/**
 * 一个描述某种类型 {@code T} 的有序元素序列的不可变容器。
 *
 * <p>{@code Node} 包含固定数量的元素，可以通过 {@link #count}, {@link #spliterator}, {@link #forEach},
 * {@link #asArray}, 或 {@link #copyInto} 方法访问这些元素。{@code Node} 可能有零个或多个子 {@code Node}；
 * 如果它没有子节点（通过 {@link #getChildCount} 和 {@link #getChild(int)} 访问），则被认为是 <em>扁平</em>
 * 或 <em>叶节点</em>；如果有子节点，则被认为是 <em>内部节点</em>。内部节点的大小是其子节点大小的总和。
 *
 * @apiNote
 * <p>{@code Node} 通常不直接存储元素，而是介导对一个或多个现有的（实际上是不可变的）数据结构的访问，如 {@code Collection}、数组或一组其他
 * {@code Node}。通常 {@code Node} 形成一棵树，其形状对应于生成包含在叶节点中的元素的计算树。在流框架中使用 {@code Node} 主要是为了在并行操作期间避免不必要的数据复制。
 *
 * @param <T> 元素的类型。
 * @since 1.8
 */
interface Node<T> {

    /**
     * 返回一个描述此 {@code Node} 中包含的元素的 {@link Spliterator}。
     *
     * @return 描述此 {@code Node} 中包含的元素的 {@code Spliterator}
     */
    Spliterator<T> spliterator();

    /**
     * 遍历此节点的元素，并使用提供的 {@code Consumer} 调用每个元素。如果 {@code Node} 的源具有定义的遍历顺序，则元素按遍历顺序提供。
     *
     * @param consumer 要调用的每个元素的 {@code Consumer}
     */
    void forEach(Consumer<? super T> consumer);

    /**
     * 返回此节点的子节点数量。
     *
     * @implSpec 默认实现返回零。
     *
     * @return 子节点数量
     */
    default int getChildCount() {
        return 0;
    }

    /**
     * 检索给定索引处的子 {@code Node}。
     *
     * @implSpec 默认实现总是抛出 {@code IndexOutOfBoundsException}。
     *
     * @param i 子节点的索引
     * @return 子节点
     * @throws IndexOutOfBoundsException 如果索引小于 0 或大于等于子节点数量
     */
    default Node<T> getChild(int i) {
        throw new IndexOutOfBoundsException();
    }

    /**
     * 返回一个描述此节点元素子序列的节点，从给定的包含起始偏移量开始，到给定的排除结束偏移量结束。
     *
     * @param from 包含的起始偏移量，必须在 0..count() 范围内。
     * @param to 排除的结束偏移量，必须在 0..count() 范围内。
     * @param generator 如果需要，用于创建新数组的函数，适用于引用节点。
     * @return 截断的节点
     */
    default Node<T> truncate(long from, long to, IntFunction<T[]> generator) {
        if (from == 0 && to == count())
            return this;
        Spliterator<T> spliterator = spliterator();
        long size = to - from;
        Node.Builder<T> nodeBuilder = Nodes.builder(size, generator);
        nodeBuilder.begin(size);
        for (int i = 0; i < from && spliterator.tryAdvance(e -> { }); i++) { }
        for (int i = 0; (i < size) && spliterator.tryAdvance(nodeBuilder); i++) { }
        nodeBuilder.end();
        return nodeBuilder.build();
    }

    /**
     * 提供此节点内容的数组视图。
     *
     * <p>根据底层实现，这可能会返回对内部数组的引用而不是副本。由于返回的数组可能是共享的，因此不应修改返回的数组。如果需要创建新数组，可以咨询 {@code generator} 函数。
     *
     * @param generator 一个工厂函数，接受一个整数参数并返回一个新且空的该大小和适当类型的数组
     * @return 包含此 {@code Node} 内容的数组
     */
    T[] asArray(IntFunction<T[]> generator);

    /**
     * 将此 {@code Node} 的内容复制到数组中，从数组的给定偏移量开始。调用者有责任确保数组有足够的空间，否则如果数组长度小于此节点包含的元素数量，将发生未指定的行为。
     *
     * @param array 要复制此 {@code Node} 内容的数组
     * @param offset 数组中的起始偏移量
     * @throws IndexOutOfBoundsException 如果复制会导致访问数组边界之外的数据
     * @throws NullPointerException 如果 {@code array} 为 {@code null}
     */
    void copyInto(T[] array, int offset);

    /**
     * 获取与此 {@code Node} 关联的 {@code StreamShape}。
     *
     * @implSpec {@code Node} 中的默认实现返回 {@code StreamShape.REFERENCE}
     *
     * @return 与此节点关联的流形状
     */
    default StreamShape getShape() {
        return StreamShape.REFERENCE;
    }

    /**
     * 返回此节点包含的元素数量。
     *
     * @return 此节点包含的元素数量
     */
    long count();

    /**
     * 一个可变的 {@code Node} 构建器，实现 {@link Sink}，构建一个包含已推送元素的扁平节点。
     */
    interface Builder<T> extends Sink<T> {

        /**
         * 构建节点。应在所有元素都已推送并用 {@link Sink#end()} 信号调用后调用。
         *
         * @return 构建的 {@code Node}
         */
        Node<T> build();

        /**
         * 专门用于 int 元素的 {@code Node.Builder}
         */
        interface OfInt extends Node.Builder<Integer>, Sink.OfInt {
            @Override
            Node.OfInt build();
        }

        /**
         * 专门用于 long 元素的 {@code Node.Builder}
         */
        interface OfLong extends Node.Builder<Long>, Sink.OfLong {
            @Override
            Node.OfLong build();
        }

        /**
         * 专门用于 double 元素的 {@code Node.Builder}
         */
        interface OfDouble extends Node.Builder<Double>, Sink.OfDouble {
            @Override
            Node.OfDouble build();
        }
    }

    public interface OfPrimitive<T, T_CONS, T_ARR,
                                 T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>,
                                 T_NODE extends OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>>
            extends Node<T> {

        /**
         * {@inheritDoc}
         *
         * @return 描述此节点元素的 {@link Spliterator.OfPrimitive}
         */
        @Override
        T_SPLITR spliterator();

        /**
         * 遍历此节点的元素，并使用提供的 {@code action} 调用每个元素。
         *
         * @param action 要调用的每个元素的消费者
         */
        @SuppressWarnings("overloads")
        void forEach(T_CONS action);

        @Override
        default T_NODE getChild(int i) {
            throw new IndexOutOfBoundsException();
        }

        T_NODE truncate(long from, long to, IntFunction<T[]> generator);

        /**
         * {@inheritDoc}
         *
         * @implSpec 默认实现调用生成器创建一个长度为 {@link #count()} 的装箱原始数组实例，然后调用 {@link #copyInto(T[], int)} 将该数组在偏移量 0 处复制。
         */
        @Override
        default T[] asArray(IntFunction<T[]> generator) {
            if (java.util.stream.Tripwire.ENABLED)
                java.util.stream.Tripwire.trip(getClass(), "{0} calling Node.OfPrimitive.asArray");

            long size = count();
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            T[] boxed = generator.apply((int) count());
            copyInto(boxed, 0);
            return boxed;
        }

        /**
         * 将此节点视为原始数组。
         *
         * <p>根据底层实现，这可能会返回对内部数组的引用而不是副本。调用者有责任决定是将此节点还是数组用作数据的主要引用。</p>
         *
         * @return 包含此 {@code Node} 内容的数组
         */
        T_ARR asPrimitiveArray();

        /**
         * 创建一个新的原始数组。
         *
         * @param count 原始数组的长度。
         * @return 新的原始数组。
         */
        T_ARR newArray(int count);

        /**
         * 将此 {@code Node} 的内容复制到原始数组中，从数组的给定偏移量开始。调用者有责任确保数组有足够的空间。
         *
         * @param array 要复制此 {@code Node} 内容的数组
         * @param offset 数组中的起始偏移量
         * @throws IndexOutOfBoundsException 如果复制会导致访问数组边界之外的数据
         * @throws NullPointerException 如果 {@code array} 为 {@code null}
         */
        void copyInto(T_ARR array, int offset);
    }

    /**
     * 专门用于 int 元素的 {@code Node}
     */
    interface OfInt extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, OfInt> {

        /**
         * {@inheritDoc}
         *
         * @param consumer 要调用的每个元素的 {@code Consumer}。如果这是 {@code IntConsumer}，则将其转换为 {@code IntConsumer} 以便在不装箱的情况下处理元素。
         */
        @Override
        default void forEach(Consumer<? super Integer> consumer) {
            if (consumer instanceof IntConsumer) {
                forEach((IntConsumer) consumer);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(), "{0} calling Node.OfInt.forEachRemaining(Consumer)");
                spliterator().forEachRemaining(consumer);
            }
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec 默认实现调用 {@link #asPrimitiveArray()} 获取 int[] 数组，然后将该 int[] 数组中的元素复制到装箱的 Integer[] 数组中。这效率不高，建议调用 {@link #copyInto(Object, int)}。
         */
        @Override
        default void copyInto(Integer[] boxed, int offset) {
            if (Tripwire.ENABLED)
                Tripwire.trip(getClass(), "{0} calling Node.OfInt.copyInto(Integer[], int)");

            int[] array = asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = array[i];
            }
        }

        @Override
        default Node.OfInt truncate(long from, long to, IntFunction<Integer[]> generator) {
            if (from == 0 && to == count())
                return this;
            long size = to - from;
            Spliterator.OfInt spliterator = spliterator();
            Node.Builder.OfInt nodeBuilder = Nodes.intBuilder(size);
            nodeBuilder.begin(size);
            for (int i = 0; i < from && spliterator.tryAdvance((IntConsumer) e -> { }); i++) { }
            for (int i = 0; (i < size) && spliterator.tryAdvance((IntConsumer) nodeBuilder); i++) { }
            nodeBuilder.end();
            return nodeBuilder.build();
        }

        @Override
        default int[] newArray(int count) {
            return new int[count];
        }

        /**
         * {@inheritDoc}
         * @implSpec {@code Node.OfInt} 中的默认实现返回 {@code StreamShape.INT_VALUE}
         */
        default StreamShape getShape() {
            return StreamShape.INT_VALUE;
        }
    }

    /**
     * 专门用于 long 元素的 {@code Node}
     */
    interface OfLong extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, OfLong> {

        /**
         * {@inheritDoc}
         *
         * @param consumer 要调用的每个元素的 {@code Consumer}。如果这是 {@code LongConsumer}，则将其转换为 {@code LongConsumer} 以便在不装箱的情况下处理元素。
         */
        @Override
        default void forEach(Consumer<? super Long> consumer) {
            if (consumer instanceof LongConsumer) {
                forEach((LongConsumer) consumer);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(), "{0} calling Node.OfLong.forEachRemaining(Consumer)");
                spliterator().forEachRemaining(consumer);
            }
        }


        /**
         * {@inheritDoc}
         *
         * @implSpec 默认实现调用 {@link #asPrimitiveArray()}
         * 以获取一个 long[] 数组，然后将这些元素从 long[] 数组复制到装箱的 Long[] 数组中。这效率不高，
         * 建议调用 {@link #copyInto(Object, int)}。
         */
        @Override
        default void copyInto(Long[] boxed, int offset) {
            if (Tripwire.ENABLED)
                Tripwire.trip(getClass(), "{0} calling Node.OfInt.copyInto(Long[], int)");

            long[] array = asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = array[i];
            }
        }

        @Override
        default Node.OfLong truncate(long from, long to, IntFunction<Long[]> generator) {
            if (from == 0 && to == count())
                return this;
            long size = to - from;
            Spliterator.OfLong spliterator = spliterator();
            Node.Builder.OfLong nodeBuilder = Nodes.longBuilder(size);
            nodeBuilder.begin(size);
            for (int i = 0; i < from && spliterator.tryAdvance((LongConsumer) e -> { }); i++) { }
            for (int i = 0; (i < size) && spliterator.tryAdvance((LongConsumer) nodeBuilder); i++) { }
            nodeBuilder.end();
            return nodeBuilder.build();
        }

        @Override
        default long[] newArray(int count) {
            return new long[count];
        }

        /**
         * {@inheritDoc}
         * @implSpec {@code Node.OfLong} 中的默认实现返回
         * {@code StreamShape.LONG_VALUE}
         */
        default StreamShape getShape() {
            return StreamShape.LONG_VALUE;
        }
    }

    /**
     * 专门用于 double 元素的 {@code Node}
     */
    interface OfDouble extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, OfDouble> {

        /**
         * {@inheritDoc}
         *
         * @param consumer 一个 {@code Consumer}，将被调用以处理此 {@code Node} 中的每个元素。
         *        如果这是一个 {@code DoubleConsumer}，则将其转换为 {@code DoubleConsumer}
         *        以便在不装箱的情况下处理元素。
         */
        @Override
        default void forEach(Consumer<? super Double> consumer) {
            if (consumer instanceof DoubleConsumer) {
                forEach((DoubleConsumer) consumer);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(), "{0} calling Node.OfLong.forEachRemaining(Consumer)");
                spliterator().forEachRemaining(consumer);
            }
        }

        //

        /**
         * {@inheritDoc}
         *
         * @implSpec 默认实现调用 {@link #asPrimitiveArray()}
         * 以获取一个 double[] 数组，然后将这些元素从 double[] 数组复制到装箱的 Double[] 数组中。这效率不高，
         * 建议调用 {@link #copyInto(Object, int)}。
         */
        @Override
        default void copyInto(Double[] boxed, int offset) {
            if (Tripwire.ENABLED)
                Tripwire.trip(getClass(), "{0} calling Node.OfDouble.copyInto(Double[], int)");

            double[] array = asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = array[i];
            }
        }

        @Override
        default Node.OfDouble truncate(long from, long to, IntFunction<Double[]> generator) {
            if (from == 0 && to == count())
                return this;
            long size = to - from;
            Spliterator.OfDouble spliterator = spliterator();
            Node.Builder.OfDouble nodeBuilder = Nodes.doubleBuilder(size);
            nodeBuilder.begin(size);
            for (int i = 0; i < from && spliterator.tryAdvance((DoubleConsumer) e -> { }); i++) { }
            for (int i = 0; (i < size) && spliterator.tryAdvance((DoubleConsumer) nodeBuilder); i++) { }
            nodeBuilder.end();
            return nodeBuilder.build();
        }

        @Override
        default double[] newArray(int count) {
            return new double[count];
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec {@code Node.OfDouble} 中的默认实现返回
         * {@code StreamShape.DOUBLE_VALUE}
         */
        default StreamShape getShape() {
            return StreamShape.DOUBLE_VALUE;
        }
    }
}
