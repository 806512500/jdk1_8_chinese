
/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates. All rights reserved.
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

import java.util.DoubleSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntFunction;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;

/**
 * 抽象基类，用于实现元素类型为 {@code double} 的中间管道阶段或管道源阶段。
 *
 * @param <E_IN> 上游源中的元素类型
 *
 * @since 1.8
 */
abstract class DoublePipeline<E_IN>
        extends AbstractPipeline<E_IN, Double, DoubleStream>
        implements DoubleStream {

    /**
     * 构造函数，用于创建流管道的头部。
     *
     * @param source 描述流源的 {@code Supplier<Spliterator>}
     * @param sourceFlags 流源的源标志，详见 {@link StreamOpFlag}
     */
    DoublePipeline(Supplier<? extends Spliterator<Double>> source,
                   int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    /**
     * 构造函数，用于创建流管道的头部。
     *
     * @param source 描述流源的 {@code Spliterator}
     * @param sourceFlags 流源的源标志，详见 {@link StreamOpFlag}
     */
    DoublePipeline(Spliterator<Double> source,
                   int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    /**
     * 构造函数，用于在现有管道上追加一个中间操作。
     *
     * @param upstream 上游元素源。
     * @param opFlags 操作标志
     */
    DoublePipeline(AbstractPipeline<?, E_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    /**
     * 将 {@code Sink<Double>} 适配为 {@code DoubleConsumer}，理想情况下只需简单地进行类型转换。
     */
    private static DoubleConsumer adapt(Sink<Double> sink) {
        if (sink instanceof DoubleConsumer) {
            return (DoubleConsumer) sink;
        } else {
            if (Tripwire.ENABLED)
                Tripwire.trip(AbstractPipeline.class,
                              "using DoubleStream.adapt(Sink<Double> s)");
            return sink::accept;
        }
    }

    /**
     * 将 {@code Spliterator<Double>} 适配为 {@code Spliterator.OfDouble}。
     *
     * @implNote
     * 实现尝试将 {@code Spliterator<Double>} 转换为 {@code Spliterator.OfDouble}，如果转换不可行则抛出异常。
     */
    private static Spliterator.OfDouble adapt(Spliterator<Double> s) {
        if (s instanceof Spliterator.OfDouble) {
            return (Spliterator.OfDouble) s;
        } else {
            if (Tripwire.ENABLED)
                Tripwire.trip(AbstractPipeline.class,
                              "using DoubleStream.adapt(Spliterator<Double> s)");
            throw new UnsupportedOperationException("DoubleStream.adapt(Spliterator<Double> s)");
        }
    }


    // 形状特定的方法

    @Override
    final StreamShape getOutputShape() {
        return StreamShape.DOUBLE_VALUE;
    }

    @Override
    final <P_IN> Node<Double> evaluateToNode(PipelineHelper<Double> helper,
                                             Spliterator<P_IN> spliterator,
                                             boolean flattenTree,
                                             IntFunction<Double[]> generator) {
        return Nodes.collectDouble(helper, spliterator, flattenTree);
    }

    @Override
    final <P_IN> Spliterator<Double> wrap(PipelineHelper<Double> ph,
                                          Supplier<Spliterator<P_IN>> supplier,
                                          boolean isParallel) {
        return new StreamSpliterators.DoubleWrappingSpliterator<>(ph, supplier, isParallel);
    }

    @Override
    @SuppressWarnings("unchecked")
    final Spliterator.OfDouble lazySpliterator(Supplier<? extends Spliterator<Double>> supplier) {
        return new StreamSpliterators.DelegatingSpliterator.OfDouble((Supplier<Spliterator.OfDouble>) supplier);
    }

    @Override
    final void forEachWithCancel(Spliterator<Double> spliterator, Sink<Double> sink) {
        Spliterator.OfDouble spl = adapt(spliterator);
        DoubleConsumer adaptedSink = adapt(sink);
        do { } while (!sink.cancellationRequested() && spl.tryAdvance(adaptedSink));
    }

    @Override
    final  Node.Builder<Double> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Double[]> generator) {
        return Nodes.doubleBuilder(exactSizeIfKnown);
    }


    // DoubleStream

    @Override
    public final PrimitiveIterator.OfDouble iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public final Spliterator.OfDouble spliterator() {
        return adapt(super.spliterator());
    }

    // 无状态的中间操作

    @Override
    public final Stream<Double> boxed() {
        return mapToObj(Double::valueOf);
    }

    @Override
    public final DoubleStream map(DoubleUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE,
                                       StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedDouble<Double>(sink) {
                    @Override
                    public void accept(double t) {
                        downstream.accept(mapper.applyAsDouble(t));
                    }
                };
            }
        };
    }


@Override
public final <U> Stream<U> mapToObj(DoubleFunction<? extends U> mapper) {
    Objects.requireNonNull(mapper);
    return new ReferencePipeline.StatelessOp<Double, U>(this, StreamShape.DOUBLE_VALUE,
                                                        StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
        @Override
        Sink<Double> opWrapSink(int flags, Sink<U> sink) {
            return new Sink.ChainedDouble<U>(sink) {
                @Override
                public void accept(double t) {
                    downstream.accept(mapper.apply(t));
                }
            };
        }
    };
}

@Override
public final IntStream mapToInt(DoubleToIntFunction mapper) {
    Objects.requireNonNull(mapper);
    return new IntPipeline.StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE,
                                               StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
        @Override
        Sink<Double> opWrapSink(int flags, Sink<Integer> sink) {
            return new Sink.ChainedDouble<Integer>(sink) {
                @Override
                public void accept(double t) {
                    downstream.accept(mapper.applyAsInt(t));
                }
            };
        }
    };
}

@Override
public final LongStream mapToLong(DoubleToLongFunction mapper) {
    Objects.requireNonNull(mapper);
    return new LongPipeline.StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE,
                                                StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
        @Override
        Sink<Double> opWrapSink(int flags, Sink<Long> sink) {
            return new Sink.ChainedDouble<Long>(sink) {
                @Override
                public void accept(double t) {
                    downstream.accept(mapper.applyAsLong(t));
                }
            };
        }
    };
}

@Override
public final DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper) {
    Objects.requireNonNull(mapper);
    return new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE,
                                    StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
        @Override
        Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            return new Sink.ChainedDouble<Double>(sink) {
                @Override
                public void begin(long size) {
                    downstream.begin(-1);
                }

                @Override
                public void accept(double t) {
                    try (DoubleStream result = mapper.apply(t)) {
                        // We can do better that this too; optimize for depth=0 case and just grab spliterator and forEach it
                        if (result != null)
                            result.sequential().forEach(i -> downstream.accept(i));
                    }
                }
            };
        }
    };
}

@Override
public DoubleStream unordered() {
    if (!isOrdered())
        return this;
    return new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_ORDERED) {
        @Override
        Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            return sink;
        }
    };
}

@Override
public final DoubleStream filter(DoublePredicate predicate) {
    Objects.requireNonNull(predicate);
    return new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE,
                                   StreamOpFlag.NOT_SIZED) {
        @Override
        Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            return new Sink.ChainedDouble<Double>(sink) {
                @Override
                public void begin(long size) {
                    downstream.begin(-1);
                }

                @Override
                public void accept(double t) {
                    if (predicate.test(t))
                        downstream.accept(t);
                }
            };
        }
    };
}

@Override
public final DoubleStream peek(DoubleConsumer action) {
    Objects.requireNonNull(action);
    return new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE,
                                   0) {
        @Override
        Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            return new Sink.ChainedDouble<Double>(sink) {
                @Override
                public void accept(double t) {
                    action.accept(t);
                    downstream.accept(t);
                }
            };
        }
    };
}

// Stateful intermediate ops from DoubleStream

@Override
public final DoubleStream limit(long maxSize) {
    if (maxSize < 0)
        throw new IllegalArgumentException(Long.toString(maxSize));
    return SliceOps.makeDouble(this, (long) 0, maxSize);
}

@Override
public final DoubleStream skip(long n) {
    if (n < 0)
        throw new IllegalArgumentException(Long.toString(n));
    if (n == 0)
        return this;
    else {
        long limit = -1;
        return SliceOps.makeDouble(this, n, limit);
    }
}

@Override
public final DoubleStream sorted() {
    return SortedOps.makeDouble(this);
}

@Override
public final DoubleStream distinct() {
    // While functional and quick to implement, this approach is not very efficient.
    // An efficient version requires a double-specific map/set implementation.
    return boxed().distinct().mapToDouble(i -> (double) i);
}

// Terminal ops from DoubleStream

@Override
public void forEach(DoubleConsumer consumer) {
    evaluate(ForEachOps.makeDouble(consumer, false));
}


                @Override
    public void forEachOrdered(DoubleConsumer consumer) {
        evaluate(ForEachOps.makeDouble(consumer, true));
    }

    @Override
    public final double sum() {
        /*
         * 在为收集操作分配的数组中，索引 0 保存运行总和的高阶位，索引 1 保存通过补偿求和计算的总和的低阶位，
         * 索引 2 保存用于计算结果的简单总和，如果流包含相同符号的无限值。
         */
        double[] summation = collect(() -> new double[3],
                               (ll, d) -> {
                                   Collectors.sumWithCompensation(ll, d);
                                   ll[2] += d;
                               },
                               (ll, rr) -> {
                                   Collectors.sumWithCompensation(ll, rr[0]);
                                   Collectors.sumWithCompensation(ll, rr[1]);
                                   ll[2] += rr[2];
                               });

        return Collectors.computeFinalSum(summation);
    }

    @Override
    public final OptionalDouble min() {
        return reduce(Math::min);
    }

    @Override
    public final OptionalDouble max() {
        return reduce(Math::max);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote {@code double} 格式可以表示从 -2<sup>53</sup> 到 2<sup>53</sup> 的所有连续整数。
     * 如果管道中的值超过 2<sup>53</sup>，平均计算中的除数将饱和在 2<sup>53</sup>，导致额外的数值错误。
     */
    @Override
    public final OptionalDouble average() {
        /*
         * 在为收集操作分配的数组中，索引 0 保存运行总和的高阶位，索引 1 保存通过补偿求和计算的总和的低阶位，
         * 索引 2 保存已看到的值的数量，索引 3 保存简单总和。
         */
        double[] avg = collect(() -> new double[4],
                               (ll, d) -> {
                                   ll[2]++;
                                   Collectors.sumWithCompensation(ll, d);
                                   ll[3] += d;
                               },
                               (ll, rr) -> {
                                   Collectors.sumWithCompensation(ll, rr[0]);
                                   Collectors.sumWithCompensation(ll, rr[1]);
                                   ll[2] += rr[2];
                                   ll[3] += rr[3];
                               });
        return avg[2] > 0
            ? OptionalDouble.of(Collectors.computeFinalSum(avg) / avg[2])
            : OptionalDouble.empty();
    }

    @Override
    public final long count() {
        return mapToLong(e -> 1L).sum();
    }

    @Override
    public final DoubleSummaryStatistics summaryStatistics() {
        return collect(DoubleSummaryStatistics::new, DoubleSummaryStatistics::accept,
                       DoubleSummaryStatistics::combine);
    }

    @Override
    public final double reduce(double identity, DoubleBinaryOperator op) {
        return evaluate(ReduceOps.makeDouble(identity, op));
    }

    @Override
    public final OptionalDouble reduce(DoubleBinaryOperator op) {
        return evaluate(ReduceOps.makeDouble(op));
    }

    @Override
    public final <R> R collect(Supplier<R> supplier,
                               ObjDoubleConsumer<R> accumulator,
                               BiConsumer<R, R> combiner) {
        Objects.requireNonNull(combiner);
        BinaryOperator<R> operator = (left, right) -> {
            combiner.accept(left, right);
            return left;
        };
        return evaluate(ReduceOps.makeDouble(supplier, accumulator, operator));
    }

    @Override
    public final boolean anyMatch(DoublePredicate predicate) {
        return evaluate(MatchOps.makeDouble(predicate, MatchOps.MatchKind.ANY));
    }

    @Override
    public final boolean allMatch(DoublePredicate predicate) {
        return evaluate(MatchOps.makeDouble(predicate, MatchOps.MatchKind.ALL));
    }

    @Override
    public final boolean noneMatch(DoublePredicate predicate) {
        return evaluate(MatchOps.makeDouble(predicate, MatchOps.MatchKind.NONE));
    }

    @Override
    public final OptionalDouble findFirst() {
        return evaluate(FindOps.makeDouble(true));
    }

    @Override
    public final OptionalDouble findAny() {
        return evaluate(FindOps.makeDouble(false));
    }

    @Override
    public final double[] toArray() {
        return Nodes.flattenDouble((Node.OfDouble) evaluateToArrayNode(Double[]::new))
                        .asPrimitiveArray();
    }

    //

    /**
     * DoubleStream 的源阶段
     *
     * @param <E_IN> 上游源中的元素类型
     */
    static class Head<E_IN> extends DoublePipeline<E_IN> {
        /**
         * DoubleStream 源阶段的构造函数。
         *
         * @param source 描述流源的 {@code Supplier<Spliterator>}
         * @param sourceFlags 流源的源标志，描述见 {@link StreamOpFlag}
         * @param parallel 如果管道是并行的，则为 {@code true}
         */
        Head(Supplier<? extends Spliterator<Double>> source,
             int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        /**
         * DoubleStream 源阶段的构造函数。
         *
         * @param source 描述流源的 {@code Spliterator}
         * @param sourceFlags 流源的源标志，描述见 {@link StreamOpFlag}
         * @param parallel 如果管道是并行的，则为 {@code true}
         */
        Head(Spliterator<Double> source,
             int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }


                    @Override
        final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        final Sink<E_IN> opWrapSink(int flags, Sink<Double> sink) {
            throw new UnsupportedOperationException();
        }

        // 优化的管道头部的顺序终端操作

        @Override
        public void forEach(DoubleConsumer consumer) {
            if (!isParallel()) {
                adapt(sourceStageSpliterator()).forEachRemaining(consumer);
            }
            else {
                super.forEach(consumer);
            }
        }

        @Override
        public void forEachOrdered(DoubleConsumer consumer) {
            if (!isParallel()) {
                adapt(sourceStageSpliterator()).forEachRemaining(consumer);
            }
            else {
                super.forEachOrdered(consumer);
            }
        }

    }

    /**
     * 双精度流的无状态中间阶段的基类。
     *
     * @param <E_IN> 上游源中的元素类型
     * @since 1.8
     */
    abstract static class StatelessOp<E_IN> extends DoublePipeline<E_IN> {
        /**
         * 通过在现有流上附加一个无状态中间操作来构造一个新的双精度流。
         *
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
     * 双精度流的有状态中间阶段的基类。
     *
     * @param <E_IN> 上游源中的元素类型
     * @since 1.8
     */
    abstract static class StatefulOp<E_IN> extends DoublePipeline<E_IN> {
        /**
         * 通过在现有流上附加一个有状态中间操作来构造一个新的双精度流。
         *
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
        abstract <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper,
                                                        Spliterator<P_IN> spliterator,
                                                        IntFunction<Double[]> generator);
    }
}
