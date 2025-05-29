
/*
 * Copyright (c) 2012, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
package java.util.stream;

import java.util.IntSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;

/**
 * 抽象基类，用于实现元素类型为 {@code int} 的中间管道阶段或管道源阶段。
 *
 * @param <E_IN> 上游源中的元素类型
 * @since 1.8
 */
abstract class IntPipeline<E_IN>
        extends AbstractPipeline<E_IN, Integer, IntStream>
        implements IntStream {

    /**
     * 构造函数，用于创建流管道的头部。
     *
     * @param source 描述流源的 {@code Supplier<Spliterator>}
     * @param sourceFlags 流源的源标志，描述见 {@link StreamOpFlag}
     * @param parallel 如果管道是并行的则为 {@code true}
     */
    IntPipeline(Supplier<? extends Spliterator<Integer>> source,
                int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    /**
     * 构造函数，用于创建流管道的头部。
     *
     * @param source 描述流源的 {@code Spliterator}
     * @param sourceFlags 流源的源标志，描述见 {@link StreamOpFlag}
     * @param parallel 如果管道是并行的则为 {@code true}
     */
    IntPipeline(Spliterator<Integer> source,
                int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    /**
     * 构造函数，用于在现有管道上附加一个中间操作。
     *
     * @param upstream 上游元素源
     * @param opFlags 新操作的操作标志
     */
    IntPipeline(AbstractPipeline<?, E_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    /**
     * 将 {@code Sink<Integer>} 适配为 {@code IntConsumer}，理想情况下仅通过类型转换实现。
     */
    private static IntConsumer adapt(Sink<Integer> sink) {
        if (sink instanceof IntConsumer) {
            return (IntConsumer) sink;
        }
        else {
            if (Tripwire.ENABLED)
                Tripwire.trip(AbstractPipeline.class,
                              "using IntStream.adapt(Sink<Integer> s)");
            return sink::accept;
        }
    }

    /**
     * 将 {@code Spliterator<Integer>} 适配为 {@code Spliterator.OfInt}。
     *
     * @implNote
     * 实现尝试将 {@code Spliterator<Integer>} 转换为 {@code Spliterator.OfInt}，如果转换不可行则抛出异常。
     */
    private static Spliterator.OfInt adapt(Spliterator<Integer> s) {
        if (s instanceof Spliterator.OfInt) {
            return (Spliterator.OfInt) s;
        }
        else {
            if (Tripwire.ENABLED)
                Tripwire.trip(AbstractPipeline.class,
                              "using IntStream.adapt(Spliterator<Integer> s)");
            throw new UnsupportedOperationException("IntStream.adapt(Spliterator<Integer> s)");
        }
    }


    // 形状特定的方法

    @Override
    final StreamShape getOutputShape() {
        return StreamShape.INT_VALUE;
    }

    @Override
    final <P_IN> Node<Integer> evaluateToNode(PipelineHelper<Integer> helper,
                                              Spliterator<P_IN> spliterator,
                                              boolean flattenTree,
                                              IntFunction<Integer[]> generator) {
        return Nodes.collectInt(helper, spliterator, flattenTree);
    }

    @Override
    final <P_IN> Spliterator<Integer> wrap(PipelineHelper<Integer> ph,
                                           Supplier<Spliterator<P_IN>> supplier,
                                           boolean isParallel) {
        return new StreamSpliterators.IntWrappingSpliterator<>(ph, supplier, isParallel);
    }

    @Override
    @SuppressWarnings("unchecked")
    final Spliterator.OfInt lazySpliterator(Supplier<? extends Spliterator<Integer>> supplier) {
        return new StreamSpliterators.DelegatingSpliterator.OfInt((Supplier<Spliterator.OfInt>) supplier);
    }

    @Override
    final void forEachWithCancel(Spliterator<Integer> spliterator, Sink<Integer> sink) {
        Spliterator.OfInt spl = adapt(spliterator);
        IntConsumer adaptedSink = adapt(sink);
        do { } while (!sink.cancellationRequested() && spl.tryAdvance(adaptedSink));
    }

    @Override
    final Node.Builder<Integer> makeNodeBuilder(long exactSizeIfKnown,
                                                IntFunction<Integer[]> generator) {
        return Nodes.intBuilder(exactSizeIfKnown);
    }


    // IntStream 方法

    @Override
    public final PrimitiveIterator.OfInt iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public final Spliterator.OfInt spliterator() {
        return adapt(super.spliterator());
    }

    // IntStream 中的状态无关的中间操作

    @Override
    public final LongStream asLongStream() {
        return new LongPipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE,
                                                     StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedInt<Long>(sink) {
                    @Override
                    public void accept(int t) {
                        downstream.accept((long) t);
                    }
                };
            }
        };
    }


                @Override
    public final DoubleStream asDoubleStream() {
        return new DoublePipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE,
                                                       StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedInt<Double>(sink) {
                    @Override
                    public void accept(int t) {
                        downstream.accept((double) t);
                    }
                };
            }
        };
    }

    @Override
    public final Stream<Integer> boxed() {
        // 将 IntStream 转换为 Stream<Integer>
        return mapToObj(Integer::valueOf);
    }

    @Override
    public final IntStream map(IntUnaryOperator mapper) {
        // 检查映射器是否为 null
        Objects.requireNonNull(mapper);
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE,
                                        StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink) {
                    @Override
                    public void accept(int t) {
                        // 将每个元素通过映射器转换
                        downstream.accept(mapper.applyAsInt(t));
                    }
                };
            }
        };
    }

    @Override
    public final <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
        // 检查映射器是否为 null
        Objects.requireNonNull(mapper);
        return new ReferencePipeline.StatelessOp<Integer, U>(this, StreamShape.INT_VALUE,
                                                             StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            Sink<Integer> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedInt<U>(sink) {
                    @Override
                    public void accept(int t) {
                        // 将每个元素通过映射器转换为对象
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final LongStream mapToLong(IntToLongFunction mapper) {
        // 检查映射器是否为 null
        Objects.requireNonNull(mapper);
        return new LongPipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE,
                                                     StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedInt<Long>(sink) {
                    @Override
                    public void accept(int t) {
                        // 将每个元素通过映射器转换为 long
                        downstream.accept(mapper.applyAsLong(t));
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream mapToDouble(IntToDoubleFunction mapper) {
        // 检查映射器是否为 null
        Objects.requireNonNull(mapper);
        return new DoublePipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE,
                                                       StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedInt<Double>(sink) {
                    @Override
                    public void accept(int t) {
                        // 将每个元素通过映射器转换为 double
                        downstream.accept(mapper.applyAsDouble(t));
                    }
                };
            }
        };
    }

    @Override
    public final IntStream flatMap(IntFunction<? extends IntStream> mapper) {
        // 检查映射器是否为 null
        Objects.requireNonNull(mapper);
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE,
                                        StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(int t) {
                        try (IntStream result = mapper.apply(t)) {
                            // 我们可以在这里进行优化，特别是对于深度为 0 的情况，直接获取 spliterator 并遍历
                            if (result != null)
                                result.sequential().forEach(i -> downstream.accept(i));
                        }
                    }
                };
            }
        };
    }

    @Override
    public IntStream unordered() {
        // 如果当前流已经是无序的，则直接返回
        if (!isOrdered())
            return this;
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_ORDERED) {
            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return sink;
            }
        };
    }

    @Override
    public final IntStream filter(IntPredicate predicate) {
        // 检查谓词是否为 null
        Objects.requireNonNull(predicate);
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE,
                                        StreamOpFlag.NOT_SIZED) {
            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(int t) {
                        // 如果谓词测试通过，则传递元素
                        if (predicate.test(t))
                            downstream.accept(t);
                    }
                };
            }
        };
    }

    @Override
    public final IntStream peek(IntConsumer action) {
        // 检查消费者是否为 null
        Objects.requireNonNull(action);
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE,
                                        0) {
            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink) {
                    @Override
                    public void accept(int t) {
                        // 执行消费者操作并传递元素
                        action.accept(t);
                        downstream.accept(t);
                    }
                };
            }
        };
    }

                // 状态化中间操作来自 IntStream

    @Override
    public final IntStream limit(long maxSize) {
        if (maxSize < 0)
            throw new IllegalArgumentException(Long.toString(maxSize));
        return SliceOps.makeInt(this, 0, maxSize);
    }

    @Override
    public final IntStream skip(long n) {
        if (n < 0)
            throw new IllegalArgumentException(Long.toString(n));
        if (n == 0)
            return this;
        else
            return SliceOps.makeInt(this, n, -1);
    }

    @Override
    public final IntStream sorted() {
        return SortedOps.makeInt(this);
    }

    @Override
    public final IntStream distinct() {
        // 虽然功能性和快速实现，但这种方法效率不高。
        // 高效版本需要一个特定于 int 的 map/set 实现。
        return boxed().distinct().mapToInt(i -> i);
    }

    // 终端操作来自 IntStream

    @Override
    public void forEach(IntConsumer action) {
        evaluate(ForEachOps.makeInt(action, false));
    }

    @Override
    public void forEachOrdered(IntConsumer action) {
        evaluate(ForEachOps.makeInt(action, true));
    }

    @Override
    public final int sum() {
        return reduce(0, Integer::sum);
    }

    @Override
    public final OptionalInt min() {
        return reduce(Math::min);
    }

    @Override
    public final OptionalInt max() {
        return reduce(Math::max);
    }

    @Override
    public final long count() {
        return mapToLong(e -> 1L).sum();
    }

    @Override
    public final OptionalDouble average() {
        long[] avg = collect(() -> new long[2],
                             (ll, i) -> {
                                 ll[0]++;
                                 ll[1] += i;
                             },
                             (ll, rr) -> {
                                 ll[0] += rr[0];
                                 ll[1] += rr[1];
                             });
        return avg[0] > 0
               ? OptionalDouble.of((double) avg[1] / avg[0])
               : OptionalDouble.empty();
    }

    @Override
    public final IntSummaryStatistics summaryStatistics() {
        return collect(IntSummaryStatistics::new, IntSummaryStatistics::accept,
                       IntSummaryStatistics::combine);
    }

    @Override
    public final int reduce(int identity, IntBinaryOperator op) {
        return evaluate(ReduceOps.makeInt(identity, op));
    }

    @Override
    public final OptionalInt reduce(IntBinaryOperator op) {
        return evaluate(ReduceOps.makeInt(op));
    }

    @Override
    public final <R> R collect(Supplier<R> supplier,
                               ObjIntConsumer<R> accumulator,
                               BiConsumer<R, R> combiner) {
        Objects.requireNonNull(combiner);
        BinaryOperator<R> operator = (left, right) -> {
            combiner.accept(left, right);
            return left;
        };
        return evaluate(ReduceOps.makeInt(supplier, accumulator, operator));
    }

    @Override
    public final boolean anyMatch(IntPredicate predicate) {
        return evaluate(MatchOps.makeInt(predicate, MatchOps.MatchKind.ANY));
    }

    @Override
    public final boolean allMatch(IntPredicate predicate) {
        return evaluate(MatchOps.makeInt(predicate, MatchOps.MatchKind.ALL));
    }

    @Override
    public final boolean noneMatch(IntPredicate predicate) {
        return evaluate(MatchOps.makeInt(predicate, MatchOps.MatchKind.NONE));
    }

    @Override
    public final OptionalInt findFirst() {
        return evaluate(FindOps.makeInt(true));
    }

    @Override
    public final OptionalInt findAny() {
        return evaluate(FindOps.makeInt(false));
    }

    @Override
    public final int[] toArray() {
        return Nodes.flattenInt((Node.OfInt) evaluateToArrayNode(Integer[]::new))
                        .asPrimitiveArray();
    }

    //

    /**
     * IntStream 的源阶段。
     *
     * @param <E_IN> 上游源中的元素类型
     * @since 1.8
     */
    static class Head<E_IN> extends IntPipeline<E_IN> {
        /**
         * IntStream 源阶段的构造函数。
         *
         * @param source 描述流源的 {@code Supplier<Spliterator>}
         * @param sourceFlags 流源的源标志，描述见 {@link StreamOpFlag}
         * @param parallel 如果管道是并行的则为 {@code true}
         */
        Head(Supplier<? extends Spliterator<Integer>> source,
             int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        /**
         * IntStream 源阶段的构造函数。
         *
         * @param source 描述流源的 {@code Spliterator}
         * @param sourceFlags 流源的源标志，描述见 {@link StreamOpFlag}
         * @param parallel 如果管道是并行的则为 {@code true}
         */
        Head(Spliterator<Integer> source,
             int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        @Override
        final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        final Sink<E_IN> opWrapSink(int flags, Sink<Integer> sink) {
            throw new UnsupportedOperationException();
        }

        // 管道头部的优化顺序终端操作

        @Override
        public void forEach(IntConsumer action) {
            if (!isParallel()) {
                adapt(sourceStageSpliterator()).forEachRemaining(action);
            }
            else {
                super.forEach(action);
            }
        }

        @Override
        public void forEachOrdered(IntConsumer action) {
            if (!isParallel()) {
                adapt(sourceStageSpliterator()).forEachRemaining(action);
            }
            else {
                super.forEachOrdered(action);
            }
        }
    }

                /**
     * 无状态中间阶段的 IntStream 的基类
     *
     * @param <E_IN> 上游源中元素的类型
     * @since 1.8
     */
    abstract static class StatelessOp<E_IN> extends IntPipeline<E_IN> {
        /**
         * 通过向现有流附加一个无状态中间操作来构造一个新的 IntStream。
         * @param upstream 上游管道阶段
         * @param inputShape 上游管道阶段的流形状
         * @param opFlags 新阶段的操作标志
         */
        StatelessOp(AbstractPipeline<?, E_IN, ?> upstream,
                    StreamShape inputShape,
                    int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == inputShape;
        }

        @Override
        final boolean opIsStateful() {
            return false;
        }
    }

    /**
     * 有状态中间阶段的 IntStream 的基类。
     *
     * @param <E_IN> 上游源中元素的类型
     * @since 1.8
     */
    abstract static class StatefulOp<E_IN> extends IntPipeline<E_IN> {
        /**
         * 通过向现有流附加一个有状态中间操作来构造一个新的 IntStream。
         * @param upstream 上游管道阶段
         * @param inputShape 上游管道阶段的流形状
         * @param opFlags 新阶段的操作标志
         */
        StatefulOp(AbstractPipeline<?, E_IN, ?> upstream,
                   StreamShape inputShape,
                   int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == inputShape;
        }

        @Override
        final boolean opIsStateful() {
            return true;
        }

        @Override
        abstract <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> helper,
                                                         Spliterator<P_IN> spliterator,
                                                         IntFunction<Integer[]> generator);
    }
}
