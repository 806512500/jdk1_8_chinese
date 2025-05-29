
/*
 * 版权所有 (c) 2012, 2013, Oracle 和/或其附属公司。保留所有权利。
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
package java.util.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * 抽象基类，用于实现元素类型为 {@code U} 的中间管道阶段或管道源阶段。
 *
 * @param <P_IN> 上游源的元素类型
 * @param <P_OUT> 本阶段生成的元素类型
 *
 * @since 1.8
 */
abstract class ReferencePipeline<P_IN, P_OUT>
        extends AbstractPipeline<P_IN, P_OUT, Stream<P_OUT>>
        implements Stream<P_OUT>  {

    /**
     * 构造函数，用于创建流管道的头部。
     *
     * @param source 描述流源的 {@code Supplier<Spliterator>}
     * @param sourceFlags 流源的源标志，描述见 {@link StreamOpFlag}
     * @param parallel 如果管道是并行的，则为 {@code true}
     */
    ReferencePipeline(Supplier<? extends Spliterator<?>> source,
                      int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    /**
     * 构造函数，用于创建流管道的头部。
     *
     * @param source 描述流源的 {@code Spliterator}
     * @param sourceFlags 流源的源标志，描述见 {@link StreamOpFlag}
     * @param parallel 如果管道是并行的，则为 {@code true}
     */
    ReferencePipeline(Spliterator<?> source,
                      int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    /**
     * 构造函数，用于在现有管道上附加一个中间操作。
     *
     * @param upstream 上游元素源。
     */
    ReferencePipeline(AbstractPipeline<?, P_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    // 形状特定的方法

    @Override
    final StreamShape getOutputShape() {
        return StreamShape.REFERENCE;
    }

    @Override
    final <P_IN> Node<P_OUT> evaluateToNode(PipelineHelper<P_OUT> helper,
                                        Spliterator<P_IN> spliterator,
                                        boolean flattenTree,
                                        IntFunction<P_OUT[]> generator) {
        return Nodes.collect(helper, spliterator, flattenTree, generator);
    }

    @Override
    final <P_IN> Spliterator<P_OUT> wrap(PipelineHelper<P_OUT> ph,
                                     Supplier<Spliterator<P_IN>> supplier,
                                     boolean isParallel) {
        return new StreamSpliterators.WrappingSpliterator<>(ph, supplier, isParallel);
    }

    @Override
    final Spliterator<P_OUT> lazySpliterator(Supplier<? extends Spliterator<P_OUT>> supplier) {
        return new StreamSpliterators.DelegatingSpliterator<>(supplier);
    }

    @Override
    final void forEachWithCancel(Spliterator<P_OUT> spliterator, Sink<P_OUT> sink) {
        do { } while (!sink.cancellationRequested() && spliterator.tryAdvance(sink));
    }

    @Override
    final Node.Builder<P_OUT> makeNodeBuilder(long exactSizeIfKnown, IntFunction<P_OUT[]> generator) {
        return Nodes.builder(exactSizeIfKnown, generator);
    }


    // BaseStream

    @Override
    public final Iterator<P_OUT> iterator() {
        return Spliterators.iterator(spliterator());
    }


    // Stream

    // 无状态的中间操作

    @Override
    public Stream<P_OUT> unordered() {
        if (!isOrdered())
            return this;
        return new StatelessOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_ORDERED) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
                return sink;
            }
        };
    }

    @Override
    public final Stream<P_OUT> filter(Predicate<? super P_OUT> predicate) {
        Objects.requireNonNull(predicate);
        return new StatelessOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE,
                                     StreamOpFlag.NOT_SIZED) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
                return new Sink.ChainedReference<P_OUT, P_OUT>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(P_OUT u) {
                        if (predicate.test(u))
                            downstream.accept(u);
                    }
                };
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <R> Stream<R> map(Function<? super P_OUT, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<P_OUT, R>(this, StreamShape.REFERENCE,
                                     StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<R> sink) {
                return new Sink.ChainedReference<P_OUT, R>(sink) {
                    @Override
                    public void accept(P_OUT u) {
                        downstream.accept(mapper.apply(u));
                    }
                };
            }
        };
    }

    @Override
    public final IntStream mapToInt(ToIntFunction<? super P_OUT> mapper) {
        Objects.requireNonNull(mapper);
        return new IntPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE,
                                              StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedReference<P_OUT, Integer>(sink) {
                    @Override
                    public void accept(P_OUT u) {
                        downstream.accept(mapper.applyAsInt(u));
                    }
                };
            }
        };
    }


                @Override
    public final LongStream mapToLong(ToLongFunction<? super P_OUT> mapper) {
        Objects.requireNonNull(mapper);
        return new LongPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE,
                                      StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedReference<P_OUT, Long>(sink) {
                    @Override
                    public void accept(P_OUT u) {
                        downstream.accept(mapper.applyAsLong(u));
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream mapToDouble(ToDoubleFunction<? super P_OUT> mapper) {
        Objects.requireNonNull(mapper);
        return new DoublePipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE,
                                        StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedReference<P_OUT, Double>(sink) {
                    @Override
                    public void accept(P_OUT u) {
                        downstream.accept(mapper.applyAsDouble(u));
                    }
                };
            }
        };
    }

    @Override
    public final <R> Stream<R> flatMap(Function<? super P_OUT, ? extends Stream<? extends R>> mapper) {
        Objects.requireNonNull(mapper);
        // 我们可以通过在流无限时轮询 cancellationRequested 来做得更好
        return new StatelessOp<P_OUT, R>(this, StreamShape.REFERENCE,
                                     StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<R> sink) {
                return new Sink.ChainedReference<P_OUT, R>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(P_OUT u) {
                        try (Stream<? extends R> result = mapper.apply(u)) {
                            // 我们也可以在这里做得更好；对于 depth=0 的情况，直接获取 spliterator 并 forEach 它
                            if (result != null)
                                result.sequential().forEach(downstream);
                        }
                    }
                };
            }
        };
    }

    @Override
    public final IntStream flatMapToInt(Function<? super P_OUT, ? extends IntStream> mapper) {
        Objects.requireNonNull(mapper);
        // 我们可以通过在流无限时轮询 cancellationRequested 来做得更好
        return new IntPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE,
                                              StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedReference<P_OUT, Integer>(sink) {
                    IntConsumer downstreamAsInt = downstream::accept;
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(P_OUT u) {
                        try (IntStream result = mapper.apply(u)) {
                            // 我们也可以在这里做得更好；对于 depth=0 的情况，直接获取 spliterator 并 forEach 它
                            if (result != null)
                                result.sequential().forEach(downstreamAsInt);
                        }
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream flatMapToDouble(Function<? super P_OUT, ? extends DoubleStream> mapper) {
        Objects.requireNonNull(mapper);
        // 我们可以通过在流无限时轮询 cancellationRequested 来做得更好
        return new DoublePipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE,
                                                     StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedReference<P_OUT, Double>(sink) {
                    DoubleConsumer downstreamAsDouble = downstream::accept;
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(P_OUT u) {
                        try (DoubleStream result = mapper.apply(u)) {
                            // 我们也可以在这里做得更好；对于 depth=0 的情况，直接获取 spliterator 并 forEach 它
                            if (result != null)
                                result.sequential().forEach(downstreamAsDouble);
                        }
                    }
                };
            }
        };
    }

    @Override
    public final LongStream flatMapToLong(Function<? super P_OUT, ? extends LongStream> mapper) {
        Objects.requireNonNull(mapper);
        // 我们可以通过在流无限时轮询 cancellationRequested 来做得更好
        return new LongPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE,
                                                   StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @Override
            Sink<P_OUT> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedReference<P_OUT, Long>(sink) {
                    LongConsumer downstreamAsLong = downstream::accept;
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }


@Override
public void accept(P_OUT u) {
    try (LongStream result = mapper.apply(u)) {
        // 我们可以做得更好；针对 depth=0 的情况优化，直接获取 spliterator 并遍历
        if (result != null)
            result.sequential().forEach(downstreamAsLong);
    }
}
};

}
};

@Override
public final Stream<P_OUT> peek(Consumer<? super P_OUT> action) {
    Objects.requireNonNull(action);
    return new StatelessOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE,
                                         0) {
        @Override
        Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
            return new Sink.ChainedReference<P_OUT, P_OUT>(sink) {
                @Override
                public void accept(P_OUT u) {
                    action.accept(u);
                    downstream.accept(u);
                }
            };
        }
    };
}

// 从 Stream 中的状态化中间操作

@Override
public final Stream<P_OUT> distinct() {
    return DistinctOps.makeRef(this);
}

@Override
public final Stream<P_OUT> sorted() {
    return SortedOps.makeRef(this);
}

@Override
public final Stream<P_OUT> sorted(Comparator<? super P_OUT> comparator) {
    return SortedOps.makeRef(this, comparator);
}

@Override
public final Stream<P_OUT> limit(long maxSize) {
    if (maxSize < 0)
        throw new IllegalArgumentException(Long.toString(maxSize));
    return SliceOps.makeRef(this, 0, maxSize);
}

@Override
public final Stream<P_OUT> skip(long n) {
    if (n < 0)
        throw new IllegalArgumentException(Long.toString(n));
    if (n == 0)
        return this;
    else
        return SliceOps.makeRef(this, n, -1);
}

// 从 Stream 中的终端操作

@Override
public void forEach(Consumer<? super P_OUT> action) {
    evaluate(ForEachOps.makeRef(action, false));
}

@Override
public void forEachOrdered(Consumer<? super P_OUT> action) {
    evaluate(ForEachOps.makeRef(action, true));
}

@Override
@SuppressWarnings("unchecked")
public final <A> A[] toArray(IntFunction<A[]> generator) {
    // 由于 A 与 U 之间没有关系（无法声明 A 是 U 的上界）
    // 因此不会有静态类型检查。
    // 因此使用原始类型并假设 A == U，而不是在整个代码库中传播 A 和 U 的分离。
    // 运行时类型 U 从未与 A[] 的运行时类型的组件类型进行等同性检查。
    // 当元素存储在 A[] 中时，将执行运行时检查，因此如果 A 不是 U 的超类型，则会抛出 ArrayStoreException。
    @SuppressWarnings("rawtypes")
    IntFunction rawGenerator = (IntFunction) generator;
    return (A[]) Nodes.flatten(evaluateToArrayNode(rawGenerator), rawGenerator)
                          .asArray(rawGenerator);
}

@Override
public final Object[] toArray() {
    return toArray(Object[]::new);
}

@Override
public final boolean anyMatch(Predicate<? super P_OUT> predicate) {
    return evaluate(MatchOps.makeRef(predicate, MatchOps.MatchKind.ANY));
}

@Override
public final boolean allMatch(Predicate<? super P_OUT> predicate) {
    return evaluate(MatchOps.makeRef(predicate, MatchOps.MatchKind.ALL));
}

@Override
public final boolean noneMatch(Predicate<? super P_OUT> predicate) {
    return evaluate(MatchOps.makeRef(predicate, MatchOps.MatchKind.NONE));
}

@Override
public final Optional<P_OUT> findFirst() {
    return evaluate(FindOps.makeRef(true));
}

@Override
public final Optional<P_OUT> findAny() {
    return evaluate(FindOps.makeRef(false));
}

@Override
public final P_OUT reduce(final P_OUT identity, final BinaryOperator<P_OUT> accumulator) {
    return evaluate(ReduceOps.makeRef(identity, accumulator, accumulator));
}

@Override
public final Optional<P_OUT> reduce(BinaryOperator<P_OUT> accumulator) {
    return evaluate(ReduceOps.makeRef(accumulator));
}

@Override
public final <R> R reduce(R identity, BiFunction<R, ? super P_OUT, R> accumulator, BinaryOperator<R> combiner) {
    return evaluate(ReduceOps.makeRef(identity, accumulator, combiner));
}

@Override
@SuppressWarnings("unchecked")
public final <R, A> R collect(Collector<? super P_OUT, A, R> collector) {
    A container;
    if (isParallel()
            && (collector.characteristics().contains(Collector.Characteristics.CONCURRENT))
            && (!isOrdered() || collector.characteristics().contains(Collector.Characteristics.UNORDERED))) {
        container = collector.supplier().get();
        BiConsumer<A, ? super P_OUT> accumulator = collector.accumulator();
        forEach(u -> accumulator.accept(container, u));
    }
    else {
        container = evaluate(ReduceOps.makeRef(collector));
    }
    return collector.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)
           ? (R) container
           : collector.finisher().apply(container);
}

@Override
public final <R> R collect(Supplier<R> supplier,
                           BiConsumer<R, ? super P_OUT> accumulator,
                           BiConsumer<R, R> combiner) {
    return evaluate(ReduceOps.makeRef(supplier, accumulator, combiner));
}

@Override
public final Optional<P_OUT> max(Comparator<? super P_OUT> comparator) {
    return reduce(BinaryOperator.maxBy(comparator));
}

@Override
public final Optional<P_OUT> min(Comparator<? super P_OUT> comparator) {
    return reduce(BinaryOperator.minBy(comparator));
}

@Override
public final long count() {
    return mapToLong(e -> 1L).sum();
}

    //

    /**
     * 引用管道的源阶段。
     *
     * @param <E_IN> 上游源中的元素类型
     * @param <E_OUT> 本阶段生成的元素类型
     * @since 1.8
     */
    static class Head<E_IN, E_OUT> extends ReferencePipeline<E_IN, E_OUT> {
        /**
         * 流的源阶段的构造函数。
         *
         * @param source 描述流源的 {@code Supplier<Spliterator>}
         * @param sourceFlags 流源的源标志，描述见 {@link StreamOpFlag}
         */
        Head(Supplier<? extends Spliterator<?>> source,
             int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        /**
         * 流的源阶段的构造函数。
         *
         * @param source 描述流源的 {@code Spliterator}
         * @param sourceFlags 流源的源标志，描述见 {@link StreamOpFlag}
         */
        Head(Spliterator<?> source,
             int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        @Override
        final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        final Sink<E_IN> opWrapSink(int flags, Sink<E_OUT> sink) {
            throw new UnsupportedOperationException();
        }

        // 管道头部的优化顺序终端操作

        @Override
        public void forEach(Consumer<? super E_OUT> action) {
            if (!isParallel()) {
                sourceStageSpliterator().forEachRemaining(action);
            }
            else {
                super.forEach(action);
            }
        }

        @Override
        public void forEachOrdered(Consumer<? super E_OUT> action) {
            if (!isParallel()) {
                sourceStageSpliterator().forEachRemaining(action);
            }
            else {
                super.forEachOrdered(action);
            }
        }
    }

    /**
     * 流的无状态中间阶段的基类。
     *
     * @param <E_IN> 上游源中的元素类型
     * @param <E_OUT> 本阶段生成的元素类型
     * @since 1.8
     */
    abstract static class StatelessOp<E_IN, E_OUT>
            extends ReferencePipeline<E_IN, E_OUT> {
        /**
         * 通过在现有流上附加一个无状态中间操作来构造一个新的流。
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
     * 流的有状态中间阶段的基类。
     *
     * @param <E_IN> 上游源中的元素类型
     * @param <E_OUT> 本阶段生成的元素类型
     * @since 1.8
     */
    abstract static class StatefulOp<E_IN, E_OUT>
            extends ReferencePipeline<E_IN, E_OUT> {
        /**
         * 通过在现有流上附加一个有状态中间操作来构造一个新的流。
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
        abstract <P_IN> Node<E_OUT> opEvaluateParallel(PipelineHelper<E_OUT> helper,
                                                       Spliterator<P_IN> spliterator,
                                                       IntFunction<E_OUT[]> generator);
    }
}
