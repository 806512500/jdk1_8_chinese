
/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 用于生成实现流元素量化谓词匹配的短路 {@code TerminalOp} 实例的工厂。支持的变体包括匹配所有、匹配任意和匹配无。
 *
 * @since 1.8
 */
final class MatchOps {

    private MatchOps() { }

    /**
     * 枚举描述量化匹配选项 -- 全部匹配、任意匹配、无匹配。
     */
    enum MatchKind {
        /** 所有元素是否匹配谓词？ */
        ANY(true, true),

        /** 任意元素是否匹配谓词？ */
        ALL(false, false),

        /** 无元素是否匹配谓词？ */
        NONE(true, false);

        private final boolean stopOnPredicateMatches;
        private final boolean shortCircuitResult;

        private MatchKind(boolean stopOnPredicateMatches,
                          boolean shortCircuitResult) {
            this.stopOnPredicateMatches = stopOnPredicateMatches;
            this.shortCircuitResult = shortCircuitResult;
        }
    }

    /**
     * 为 Stream 构建一个量化谓词匹配器。
     *
     * @param <T> 流元素的类型
     * @param predicate 应用于流元素的 {@code Predicate}
     * @param matchKind 量化匹配的类型（全部、任意、无）
     * @return 实现所需量化匹配标准的 {@code TerminalOp}
     */
    public static <T> TerminalOp<T, Boolean> makeRef(Predicate<? super T> predicate,
            MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<T> {
            MatchSink() {
                super(matchKind);
            }

            @Override
            public void accept(T t) {
                if (!stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                    stop = true;
                    value = matchKind.shortCircuitResult;
                }
            }
        }

        return new MatchOp<>(StreamShape.REFERENCE, matchKind, MatchSink::new);
    }

    /**
     * 为 {@code IntStream} 构建一个量化谓词匹配器。
     *
     * @param predicate 应用于流元素的 {@code Predicate}
     * @param matchKind 量化匹配的类型（全部、任意、无）
     * @return 实现所需量化匹配标准的 {@code TerminalOp}
     */
    public static TerminalOp<Integer, Boolean> makeInt(IntPredicate predicate,
                                                       MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<Integer> implements Sink.OfInt {
            MatchSink() {
                super(matchKind);
            }

            @Override
            public void accept(int t) {
                if (!stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                    stop = true;
                    value = matchKind.shortCircuitResult;
                }
            }
        }

        return new MatchOp<>(StreamShape.INT_VALUE, matchKind, MatchSink::new);
    }

    /**
     * 为 {@code LongStream} 构建一个量化谓词匹配器。
     *
     * @param predicate 应用于流元素的 {@code Predicate}
     * @param matchKind 量化匹配的类型（全部、任意、无）
     * @return 实现所需量化匹配标准的 {@code TerminalOp}
     */
    public static TerminalOp<Long, Boolean> makeLong(LongPredicate predicate,
                                                     MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<Long> implements Sink.OfLong {

            MatchSink() {
                super(matchKind);
            }

            @Override
            public void accept(long t) {
                if (!stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                    stop = true;
                    value = matchKind.shortCircuitResult;
                }
            }
        }

        return new MatchOp<>(StreamShape.LONG_VALUE, matchKind, MatchSink::new);
    }

    /**
     * 为 {@code DoubleStream} 构建一个量化谓词匹配器。
     *
     * @param predicate 应用于流元素的 {@code Predicate}
     * @param matchKind 量化匹配的类型（全部、任意、无）
     * @return 实现所需量化匹配标准的 {@code TerminalOp}
     */
    public static TerminalOp<Double, Boolean> makeDouble(DoublePredicate predicate,
                                                         MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<Double> implements Sink.OfDouble {

            MatchSink() {
                super(matchKind);
            }

            @Override
            public void accept(double t) {
                if (!stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                    stop = true;
                    value = matchKind.shortCircuitResult;
                }
            }
        }

        return new MatchOp<>(StreamShape.DOUBLE_VALUE, matchKind, MatchSink::new);
    }

    /**
     * 一个短路的 {@code TerminalOp}，它对流的元素进行谓词评估，并确定这些元素是否全部、任意或无匹配谓词。
     *
     * @param <T> 流管道的输出类型
     */
    private static final class MatchOp<T> implements TerminalOp<T, Boolean> {
        private final StreamShape inputShape;
        final MatchKind matchKind;
        final Supplier<BooleanTerminalSink<T>> sinkSupplier;


                    /**
         * 构造一个 {@code MatchOp}。
         *
         * @param shape 流管道的输出形状
         * @param matchKind 量化的匹配类型（全部、任意、无）
         * @param sinkSupplier 用于实现匹配操作的适当形状的 {@code Sink} 的 {@code Supplier}
         */
        MatchOp(StreamShape shape,
                MatchKind matchKind,
                Supplier<BooleanTerminalSink<T>> sinkSupplier) {
            this.inputShape = shape;
            this.matchKind = matchKind;
            this.sinkSupplier = sinkSupplier;
        }

        @Override
        public int getOpFlags() {
            return StreamOpFlag.IS_SHORT_CIRCUIT | StreamOpFlag.NOT_ORDERED;
        }

        @Override
        public StreamShape inputShape() {
            return inputShape;
        }

        @Override
        public <S> Boolean evaluateSequential(PipelineHelper<T> helper,
                                              Spliterator<S> spliterator) {
            return helper.wrapAndCopyInto(sinkSupplier.get(), spliterator).getAndClearState();
        }

        @Override
        public <S> Boolean evaluateParallel(PipelineHelper<T> helper,
                                            Spliterator<S> spliterator) {
            // 并行实现的方法：
            // - 按照常规进行分解
            // - 在叶块上运行匹配，调用结果 "b"
            // - 如果 b == matchKind.shortCircuitOn，则提前完成并返回 b
            // - 否则，如果正常完成，返回 !shortCircuitOn

            return new MatchTask<>(this, helper, spliterator).invoke();
        }
    }

    /**
     * 布尔特定的终端接收器，以避免在返回结果时进行装箱操作的成本。子类实现特定形状的功能。
     *
     * @param <T> 流管道的输出类型
     */
    private static abstract class BooleanTerminalSink<T> implements Sink<T> {
        boolean stop;
        boolean value;

        BooleanTerminalSink(MatchKind matchKind) {
            value = !matchKind.shortCircuitResult;
        }

        public boolean getAndClearState() {
            return value;
        }

        @Override
        public boolean cancellationRequested() {
            return stop;
        }
    }

    /**
     * ForkJoinTask 实现，用于实现并行短路量化的匹配
     *
     * @param <P_IN> 管道的源元素类型
     * @param <P_OUT> 管道的输出元素类型
     */
    @SuppressWarnings("serial")
    private static final class MatchTask<P_IN, P_OUT>
            extends AbstractShortCircuitTask<P_IN, P_OUT, Boolean, MatchTask<P_IN, P_OUT>> {
        private final MatchOp<P_OUT> op;

        /**
         * 根节点的构造函数
         */
        MatchTask(MatchOp<P_OUT> op, PipelineHelper<P_OUT> helper,
                  Spliterator<P_IN> spliterator) {
            super(helper, spliterator);
            this.op = op;
        }

        /**
         * 非根节点的构造函数
         */
        MatchTask(MatchTask<P_IN, P_OUT> parent, Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.op = parent.op;
        }

        @Override
        protected MatchTask<P_IN, P_OUT> makeChild(Spliterator<P_IN> spliterator) {
            return new MatchTask<>(this, spliterator);
        }

        @Override
        protected Boolean doLeaf() {
            boolean b = helper.wrapAndCopyInto(op.sinkSupplier.get(), spliterator).getAndClearState();
            if (b == op.matchKind.shortCircuitResult)
                shortCircuit(b);
            return null;
        }

        @Override
        protected Boolean getEmptyResult() {
            return !op.matchKind.shortCircuitResult;
        }
    }
}
