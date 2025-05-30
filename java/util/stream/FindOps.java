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

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 用于生成短路 {@code TerminalOp} 实例的工厂，这些实例在流管道中搜索元素，并在找到一个元素时终止。
 * 支持的变体包括 find-first（查找流中按顺序遇到的第一个元素）和 find-any（查找流中的任意元素，不一定是按顺序遇到的第一个）。
 *
 * @since 1.8
 */
final class FindOps {

    private FindOps() { }

    /**
     * 构建用于对象流的 {@code TerminalOp}。
     *
     * @param <T> 流中元素的类型
     * @param mustFindFirst {@code TerminalOp} 是否必须生成按顺序遇到的第一个元素
     * @return 实现查找操作的 {@code TerminalOp}
     */
    public static <T> TerminalOp<T, Optional<T>> makeRef(boolean mustFindFirst) {
        return new FindOp<>(mustFindFirst, StreamShape.REFERENCE, Optional.empty(),
                            Optional::isPresent, FindSink.OfRef::new);
    }

    /**
     * 构建用于 int 流的 {@code TerminalOp}。
     *
     * @param mustFindFirst {@code TerminalOp} 是否必须生成按顺序遇到的第一个元素
     * @return 实现查找操作的 {@code TerminalOp}
     */
    public static TerminalOp<Integer, OptionalInt> makeInt(boolean mustFindFirst) {
        return new FindOp<>(mustFindFirst, StreamShape.INT_VALUE, OptionalInt.empty(),
                            OptionalInt::isPresent, FindSink.OfInt::new);
    }

    /**
     * 构建用于 long 流的 {@code TerminalOp}。
     *
     * @param mustFindFirst {@code TerminalOp} 是否必须生成按顺序遇到的第一个元素
     * @return 实现查找操作的 {@code TerminalOp}
     */
    public static TerminalOp<Long, OptionalLong> makeLong(boolean mustFindFirst) {
        return new FindOp<>(mustFindFirst, StreamShape.LONG_VALUE, OptionalLong.empty(),
                            OptionalLong::isPresent, FindSink.OfLong::new);
    }

    /**
     * 构建用于 double 流的 {@code TerminalOp}。
     *
     * @param mustFindFirst {@code TerminalOp} 是否必须生成按顺序遇到的第一个元素
     * @return 实现查找操作的 {@code TerminalOp}
     */
    public static TerminalOp<Double, OptionalDouble> makeDouble(boolean mustFindFirst) {
        return new FindOp<>(mustFindFirst, StreamShape.DOUBLE_VALUE, OptionalDouble.empty(),
                            OptionalDouble::isPresent, FindSink.OfDouble::new);
    }

    /**
     * 一个短路 {@code TerminalOp}，在流管道中搜索元素，并在找到一个元素时终止。实现了 find-first（查找流中按顺序遇到的第一个元素）和 find-any（查找流中的任意元素，不一定是按顺序遇到的第一个）。
     *
     * @param <T> 流管道的输出类型
     * @param <O> 查找操作的结果类型，通常是一个可选类型
     */
    private static final class FindOp<T, O> implements TerminalOp<T, O> {
        private final StreamShape shape;
        final boolean mustFindFirst;
        final O emptyValue;
        final Predicate<O> presentPredicate;
        final Supplier<TerminalSink<T, O>> sinkSupplier;

        /**
         * 构建 {@code FindOp}。
         *
         * @param mustFindFirst 如果为 true，则必须找到按顺序遇到的第一个元素，否则可以找到任意元素
         * @param shape 要搜索的元素的流形状
         * @param emptyValue 对应于“未找到任何元素”的结果值
         * @param presentPredicate 对应于“找到某些元素”的结果值的谓词
         * @param sinkSupplier 实现匹配功能的 {@code TerminalSink} 的供应商
         */
        FindOp(boolean mustFindFirst,
                       StreamShape shape,
                       O emptyValue,
                       Predicate<O> presentPredicate,
                       Supplier<TerminalSink<T, O>> sinkSupplier) {
            this.mustFindFirst = mustFindFirst;
            this.shape = shape;
            this.emptyValue = emptyValue;
            this.presentPredicate = presentPredicate;
            this.sinkSupplier = sinkSupplier;
        }

        @Override
        public int getOpFlags() {
            return StreamOpFlag.IS_SHORT_CIRCUIT | (mustFindFirst ? 0 : StreamOpFlag.NOT_ORDERED);
        }

        @Override
        public StreamShape inputShape() {
            return shape;
        }

        @Override
        public <S> O evaluateSequential(PipelineHelper<T> helper,
                                        Spliterator<S> spliterator) {
            O result = helper.wrapAndCopyInto(sinkSupplier.get(), spliterator).get();
            return result != null ? result : emptyValue;
        }

        @Override
        public <P_IN> O evaluateParallel(PipelineHelper<T> helper,
                                         Spliterator<P_IN> spliterator) {
            return new FindTask<>(this, helper, spliterator).invoke();
        }
    }

    /**
     * 实现 {@code TerminalSink} 的类，实现查找功能，并在找到某个元素时请求取消。
     *
     * @param <T> 输入元素的类型
     * @param <O> 结果类型，通常是一个可选类型
     */
    private static abstract class FindSink<T, O> implements TerminalSink<T, O> {
        boolean hasValue;
        T value;

        FindSink() {} // 避免创建特殊访问器

        @Override
        public void accept(T value) {
            if (!hasValue) {
                hasValue = true;
                this.value = value;
            }
        }

        @Override
        public boolean cancellationRequested() {
            return hasValue;
        }

        /** {@code FindSink} 的参考流的特化 */
        static final class OfRef<T> extends FindSink<T, Optional<T>> {
            @Override
            public Optional<T> get() {
                return hasValue ? Optional.of(value) : null;
            }
        }

        /** {@code FindSink} 的 int 流的特化 */
        static final class OfInt extends FindSink<Integer, OptionalInt>
                implements Sink.OfInt {
            @Override
            public void accept(int value) {
                // 装箱在这里是可以的，因为很少有值会实际流入到 sink 中
                accept((Integer) value);
            }

            @Override
            public OptionalInt get() {
                return hasValue ? OptionalInt.of(value) : null;
            }
        }

        /** {@code FindSink} 的 long 流的特化 */
        static final class OfLong extends FindSink<Long, OptionalLong>
                implements Sink.OfLong {
            @Override
            public void accept(long value) {
                // 装箱在这里是可以的，因为很少有值会实际流入到 sink 中
                accept((Long) value);
            }

            @Override
            public OptionalLong get() {
                return hasValue ? OptionalLong.of(value) : null;
            }
        }

        /** {@code FindSink} 的 double 流的特化 */
        static final class OfDouble extends FindSink<Double, OptionalDouble>
                implements Sink.OfDouble {
            @Override
            public void accept(double value) {
                // 装箱在这里是可以的，因为很少有值会实际流入到 sink 中
                accept((Double) value);
            }

            @Override
            public OptionalDouble get() {
                return hasValue ? OptionalDouble.of(value) : null;
            }
        }
    }

    /**
     * 实现并行短路搜索的 {@code ForkJoinTask}。
     * @param <P_IN> 流管道的输入元素类型
     * @param <P_OUT> 流管道的输出元素类型
     * @param <O> 查找操作的结果类型
     */
    @SuppressWarnings("serial")
    private static final class FindTask<P_IN, P_OUT, O>
            extends AbstractShortCircuitTask<P_IN, P_OUT, O, FindTask<P_IN, P_OUT, O>> {
        private final FindOp<P_OUT, O> op;

        FindTask(FindOp<P_OUT, O> op,
                 PipelineHelper<P_OUT> helper,
                 Spliterator<P_IN> spliterator) {
            super(helper, spliterator);
            this.op = op;
        }

        FindTask(FindTask<P_IN, P_OUT, O> parent, Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.op = parent.op;
        }

        @Override
        protected FindTask<P_IN, P_OUT, O> makeChild(Spliterator<P_IN> spliterator) {
            return new FindTask<>(this, spliterator);
        }

        @Override
        protected O getEmptyResult() {
            return op.emptyValue;
        }

        private void foundResult(O answer) {
            if (isLeftmostNode())
                shortCircuit(answer);
            else
                cancelLaterNodes();
        }

        @Override
        protected O doLeaf() {
            O result = helper.wrapAndCopyInto(op.sinkSupplier.get(), spliterator).get();
            if (!op.mustFindFirst) {
                if (result != null)
                    shortCircuit(result);
                return null;
            }
            else {
                if (result != null) {
                    foundResult(result);
                    return result;
                }
                else
                    return null;
            }
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if (op.mustFindFirst) {
                    for (FindTask<P_IN, P_OUT, O> child = leftChild, p = null; child != p;
                         p = child, child = rightChild) {
                    O result = child.getLocalResult();
                    if (result != null && op.presentPredicate.test(result)) {
                        setLocalResult(result);
                        foundResult(result);
                        break;
                    }
                }
            }
            super.onCompletion(caller);
        }
    }
}
