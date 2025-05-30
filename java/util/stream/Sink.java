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

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * {@link Consumer} 的扩展，用于通过流管道的各个阶段传递值，并提供管理大小信息、控制流等的附加方法。
 * 在第一次调用 {@code Sink} 的 {@code accept()} 方法之前，必须先调用 {@code begin()} 方法告知数据即将到达
 * （可选地告知数据量），在所有数据发送完毕后，必须调用 {@code end()} 方法。调用 {@code end()} 之后，
 * 不应在再次调用 {@code begin()} 之前调用 {@code accept()}。{@code Sink} 还提供了一种机制，
 * 通过该机制，接收方可以合作地发出信号表示不希望接收更多数据（{@code cancellationRequested()} 方法），
 * 源可以在发送更多数据到 {@code Sink} 之前进行轮询。
 *
 * <p>接收方可能处于两种状态之一：初始状态和活动状态。它从初始状态开始；{@code begin()} 方法将其转换为活动状态，
 * 而 {@code end()} 方法将其转换回初始状态，可以重新使用。数据接收方法（如 {@code accept()}）仅在活动状态下有效。
 *
 * @apiNote
 * 流管道由源、零个或多个中间阶段（如过滤或映射）以及终端阶段（如归约或遍历）组成。以具体示例考虑以下管道：
 *
 * <pre>{@code
 *     int longestStringLengthStartingWithA
 *         = strings.stream()
 *                  .filter(s -> s.startsWith("A"))
 *                  .mapToInt(String::length)
 *                  .max();
 * }</pre>
 *
 * <p>这里，我们有三个阶段：过滤、映射和归约。过滤阶段消费字符串并发出这些字符串的子集；映射阶段消费字符串并发出整数；
 * 归约阶段消费这些整数并计算最大值。
 *
 * <p>{@code Sink} 实例用于表示此管道的每个阶段，无论该阶段接受对象、整数、长整数还是双精度浮点数。
 * Sink 有 {@code accept(Object)}、{@code accept(int)} 等入口点，因此我们不需要为每个原始类型提供专门的接口。
 * （它可能被称为“万能接收器”）。管道的入口点是过滤阶段的 {@code Sink}，它将一些元素“下游”发送到映射阶段的 {@code Sink}，
 * 映射阶段又将整数值发送到归约阶段的 {@code Sink}。与给定阶段关联的 {@code Sink} 实现应知道下一阶段的数据类型，
 * 并调用下游 {@code Sink} 的正确 {@code accept} 方法。同样，每个阶段必须实现与其接受的数据类型相对应的正确 {@code accept} 方法。
 *
 * <p>专门的子类型如 {@link Sink.OfInt} 重写 {@code accept(Object)} 以调用适当的原始类型 {@code accept} 方法，
 * 实现适当的原始类型 {@code Consumer}，并重新抽象适当的原始类型 {@code accept} 方法。
 *
 * <p>链接子类型如 {@link ChainedInt} 不仅实现了 {@code Sink.OfInt}，还维护了一个表示下游 {@code Sink} 的 {@code downstream} 字段，
 * 并实现了 {@code begin()}、{@code end()} 和 {@code cancellationRequested()} 方法以委托给下游 {@code Sink}。
 * 大多数中间操作的实现将使用这些链接包装器。例如，上述示例中的映射阶段可能如下所示：
 *
 * <pre>{@code
 *     IntSink is = new Sink.ChainedReference<U>(sink) {
 *         public void accept(U u) {
 *             downstream.accept(mapper.applyAsInt(u));
 *         }
 *     };
 * }</pre>
 *
 * <p>这里，我们实现了 {@code Sink.ChainedReference<U>}，表示我们期望接收类型为 {@code U} 的元素作为输入，
 * 并将下游接收方传递给构造函数。因为下一阶段期望接收整数，所以在发出值时必须调用 {@code accept(int)} 方法。
 * {@code accept()} 方法应用从 {@code U} 到 {@code int} 的映射函数，并将结果值传递给下游 {@code Sink}。
 *
 * @param <T> 值流的元素类型
 * @since 1.8
 */
interface Sink<T> extends Consumer<T> {
    /**
     * 重置接收方状态以接收新的数据集。在向接收方发送任何数据之前必须调用此方法。调用 {@link #end()} 之后，
     * 可以调用此方法以重置接收方以进行另一项计算。
     * @param size 要推送的数据的确切大小，如果未知或无限则为 {@code -1}。
     *
     * <p>在此调用之前，接收方必须处于初始状态，调用后则处于活动状态。
     */
    default void begin(long size) {}

    /**
     * 表示所有元素均已推送。如果 {@code Sink} 是有状态的，此时应将任何存储的状态发送到下游，并清除任何累积的状态（和相关资源）。
     *
     * <p>在此调用之前，接收方必须处于活动状态，调用后则返回到初始状态。
     */
    default void end() {}

    /**
     * 表示此 {@code Sink} 不希望接收更多数据。
     *
     * @implSpec 默认实现始终返回 false。
     *
     * @return 如果请求取消则返回 true
     */
    default boolean cancellationRequested() {
        return false;
    }

    /**
     * 接受一个 int 值。
     *
     * @implSpec 默认实现抛出 IllegalStateException。
     *
     * @throws IllegalStateException 如果此接收方不接受 int 值
     */
    default void accept(int value) {
        throw new IllegalStateException("called wrong accept method");
    }

    /**
     * 接受一个 long 值。
     *
     * @implSpec 默认实现抛出 IllegalStateException。
     *
     * @throws IllegalStateException 如果此接收方不接受 long 值
     */
    default void accept(long value) {
        throw new IllegalStateException("called wrong accept method");
    }

    /**
     * 接受一个 double 值。
     *
     * @implSpec 默认实现抛出 IllegalStateException。
     *
     * @throws IllegalStateException 如果此接收方不接受 double 值
     */
    default void accept(double value) {
        throw new IllegalStateException("called wrong accept method");
    }

    /**
     * 实现 {@code Sink<Integer>} 的 {@code Sink}，重新抽象 {@code accept(int)}，
     * 并将 {@code accept(Integer)} 桥接到 {@code accept(int)}。
     */
    interface OfInt extends Sink<Integer>, IntConsumer {
        @Override
        void accept(int value);

        @Override
        default void accept(Integer i) {
            if (Tripwire.ENABLED)
                Tripwire.trip(getClass(), "{0} calling Sink.OfInt.accept(Integer)");
            accept(i.intValue());
        }
    }

    /**
     * 实现 {@code Sink<Long>} 的 {@code Sink}，重新抽象 {@code accept(long)}，
     * 并将 {@code accept(Long)} 桥接到 {@code accept(long)}。
     */
    interface OfLong extends Sink<Long>, LongConsumer {
        @Override
        void accept(long value);

        @Override
        default void accept(Long i) {
            if (Tripwire.ENABLED)
                Tripwire.trip(getClass(), "{0} calling Sink.OfLong.accept(Long)");
            accept(i.longValue());
        }
    }

    /**
     * 实现 {@code Sink<Double>} 的 {@code Sink}，重新抽象 {@code accept(double)}，
     * 并将 {@code accept(Double)} 桥接到 {@code accept(double)}。
     */
    interface OfDouble extends Sink<Double>, DoubleConsumer {
        @Override
        void accept(double value);

        @Override
        default void accept(Double i) {
            if (Tripwire.ENABLED)
                Tripwire.trip(getClass(), "{0} calling Sink.OfDouble.accept(Double)");
            accept(i.doubleValue());
        }
    }

    /**
     * 用于创建接收方链的抽象 {@code Sink} 实现。{@code begin}、{@code end} 和
     * {@code cancellationRequested} 方法被链接到下游的 {@code Sink}。此实现接受未知输入形状的下游 {@code Sink}，
     * 并生成一个 {@code Sink<T>}。{@code accept()} 方法的实现必须调用下游 {@code Sink} 的正确 {@code accept()} 方法。
     */
    static abstract class ChainedReference<T, E_OUT> implements Sink<T> {
        protected final Sink<? super E_OUT> downstream;

        public ChainedReference(Sink<? super E_OUT> downstream) {
            this.downstream = Objects.requireNonNull(downstream);
        }

        @Override
        public void begin(long size) {
            downstream.begin(size);
        }

        @Override
        public void end() {
            downstream.end();
        }

        @Override
        public boolean cancellationRequested() {
            return downstream.cancellationRequested();
        }
    }

    /**
     * 用于创建接收方链的抽象 {@code Sink} 实现。{@code begin}、{@code end} 和
     * {@code cancellationRequested} 方法被链接到下游的 {@code Sink}。此实现接受未知输入形状的下游 {@code Sink}，
     * 并生成一个 {@code Sink.OfInt}。{@code accept()} 方法的实现必须调用下游 {@code Sink} 的正确 {@code accept()} 方法。
     */
    static abstract class ChainedInt<E_OUT> implements Sink.OfInt {
        protected final Sink<? super E_OUT> downstream;

        public ChainedInt(Sink<? super E_OUT> downstream) {
            this.downstream = Objects.requireNonNull(downstream);
        }

        @Override
        public void begin(long size) {
            downstream.begin(size);
        }

        @Override
        public void end() {
            downstream.end();
        }

        @Override
        public boolean cancellationRequested() {
            return downstream.cancellationRequested();
        }
    }

    /**
     * 用于创建接收方链的抽象 {@code Sink} 实现。{@code begin}、{@code end} 和
     * {@code cancellationRequested} 方法被链接到下游的 {@code Sink}。此实现接受未知输入形状的下游 {@code Sink}，
     * 并生成一个 {@code Sink.OfLong}。{@code accept()} 方法的实现必须调用下游 {@code Sink} 的正确 {@code accept()} 方法。
     */
    static abstract class ChainedLong<E_OUT> implements Sink.OfLong {
        protected final Sink<? super E_OUT> downstream;

        public ChainedLong(Sink<? super E_OUT> downstream) {
            this.downstream = Objects.requireNonNull(downstream);
        }

        @Override
        public void begin(long size) {
            downstream.begin(size);
        }

        @Override
        public void end() {
            downstream.end();
        }

        @Override
        public boolean cancellationRequested() {
            return downstream.cancellationRequested();
        }
    }

    /**
     * 用于创建接收方链的抽象 {@code Sink} 实现。{@code begin}、{@code end} 和
     * {@code cancellationRequested} 方法被链接到下游的 {@code Sink}。此实现接受未知输入形状的下游 {@code Sink}，
     * 并生成一个 {@code Sink.OfDouble}。{@code accept()} 方法的实现必须调用下游 {@code Sink} 的正确 {@code accept()} 方法。
     */
    static abstract class ChainedDouble<E_OUT> implements Sink.OfDouble {
        protected final Sink<? super E_OUT> downstream;

        public ChainedDouble(Sink<? super E_OUT> downstream) {
            this.downstream = Objects.requireNonNull(downstream);
        }

        @Override
        public void begin(long size) {
            downstream.begin(size);
        }

        @Override
        public void end() {
            downstream.end();
        }

        @Override
        public boolean cancellationRequested() {
            return downstream.cancellationRequested();
        }
    }
}
