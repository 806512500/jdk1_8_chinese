
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;

/**
 * 用于创建执行流中每个元素操作的 {@code TerminalOp} 实例的工厂。支持的变体包括无序遍历（元素在可用时立即提供给 {@code Consumer}）和有序遍历（元素按遇到的顺序提供给 {@code Consumer}）。
 *
 * <p>元素将在它们可用的任何线程和任何顺序中提供给 {@code Consumer}。对于有序遍历，保证处理元素 <em>发生在</em> 遇到顺序中后续元素的处理之前。
 *
 * <p>由于将元素发送给 {@code Consumer} 而引发的异常将被传递给调用者，并且遍历将提前终止。
 *
 * @since 1.8
 */
final class ForEachOps {

    private ForEachOps() { }

    /**
     * 构造一个执行流中每个元素操作的 {@code TerminalOp}。
     *
     * @param action 接收流中所有元素的 {@code Consumer}
     * @param ordered 是否请求有序遍历
     * @param <T> 流元素的类型
     * @return {@code TerminalOp} 实例
     */
    public static <T> TerminalOp<T, Void> makeRef(Consumer<? super T> action,
                                                  boolean ordered) {
        Objects.requireNonNull(action);
        return new ForEachOp.OfRef<>(action, ordered);
    }

    /**
     * 构造一个执行 {@code IntStream} 中每个元素操作的 {@code TerminalOp}。
     *
     * @param action 接收流中所有元素的 {@code IntConsumer}
     * @param ordered 是否请求有序遍历
     * @return {@code TerminalOp} 实例
     */
    public static TerminalOp<Integer, Void> makeInt(IntConsumer action,
                                                    boolean ordered) {
        Objects.requireNonNull(action);
        return new ForEachOp.OfInt(action, ordered);
    }

    /**
     * 构造一个执行 {@code LongStream} 中每个元素操作的 {@code TerminalOp}。
     *
     * @param action 接收流中所有元素的 {@code LongConsumer}
     * @param ordered 是否请求有序遍历
     * @return {@code TerminalOp} 实例
     */
    public static TerminalOp<Long, Void> makeLong(LongConsumer action,
                                                  boolean ordered) {
        Objects.requireNonNull(action);
        return new ForEachOp.OfLong(action, ordered);
    }

    /**
     * 构造一个执行 {@code DoubleStream} 中每个元素操作的 {@code TerminalOp}。
     *
     * @param action 接收流中所有元素的 {@code DoubleConsumer}
     * @param ordered 是否请求有序遍历
     * @return {@code TerminalOp} 实例
     */
    public static TerminalOp<Double, Void> makeDouble(DoubleConsumer action,
                                                      boolean ordered) {
        Objects.requireNonNull(action);
        return new ForEachOp.OfDouble(action, ordered);
    }

    /**
     * 一个评估流管道并将输出发送给自身的 {@code TerminalSink} 的 {@code TerminalOp}。元素将在它们可用的任何线程中发送。如果遍历是无序的，它们将独立于流的遇到顺序发送。
     *
     * <p>这个终端操作是无状态的。对于并行评估，每个 {@code ForEachTask} 的叶实例将向这个类的同一个 {@code TerminalSink} 引用发送元素。
     *
     * @param <T> 流管道的输出类型
     */
    static abstract class ForEachOp<T>
            implements TerminalOp<T, Void>, TerminalSink<T, Void> {
        private final boolean ordered;

        protected ForEachOp(boolean ordered) {
            this.ordered = ordered;
        }

        // TerminalOp

        @Override
        public int getOpFlags() {
            return ordered ? 0 : StreamOpFlag.NOT_ORDERED;
        }

        @Override
        public <S> Void evaluateSequential(PipelineHelper<T> helper,
                                           Spliterator<S> spliterator) {
            return helper.wrapAndCopyInto(this, spliterator).get();
        }

        @Override
        public <S> Void evaluateParallel(PipelineHelper<T> helper,
                                         Spliterator<S> spliterator) {
            if (ordered)
                new ForEachOrderedTask<>(helper, spliterator, this).invoke();
            else
                new ForEachTask<>(helper, spliterator, helper.wrapSink(this)).invoke();
            return null;
        }

        // TerminalSink

        @Override
        public Void get() {
            return null;
        }

        // 实现

        /** 引用流的实现类 */
        static final class OfRef<T> extends ForEachOp<T> {
            final Consumer<? super T> consumer;

            OfRef(Consumer<? super T> consumer, boolean ordered) {
                super(ordered);
                this.consumer = consumer;
            }

            @Override
            public void accept(T t) {
                consumer.accept(t);
            }
        }

        /** {@code IntStream} 的实现类 */
        static final class OfInt extends ForEachOp<Integer>
                implements Sink.OfInt {
            final IntConsumer consumer;


                        OfInt(IntConsumer consumer, boolean ordered) {
                super(ordered);
                this.consumer = consumer;
            }

            @Override
            public StreamShape inputShape() {
                return StreamShape.INT_VALUE;
            }

            @Override
            public void accept(int t) {
                consumer.accept(t);
            }
        }

        /** 实现类用于 {@code LongStream} */
        static final class OfLong extends ForEachOp<Long>
                implements Sink.OfLong {
            final LongConsumer consumer;

            OfLong(LongConsumer consumer, boolean ordered) {
                super(ordered);
                this.consumer = consumer;
            }

            @Override
            public StreamShape inputShape() {
                return StreamShape.LONG_VALUE;
            }

            @Override
            public void accept(long t) {
                consumer.accept(t);
            }
        }

        /** 实现类用于 {@code DoubleStream} */
        static final class OfDouble extends ForEachOp<Double>
                implements Sink.OfDouble {
            final DoubleConsumer consumer;

            OfDouble(DoubleConsumer consumer, boolean ordered) {
                super(ordered);
                this.consumer = consumer;
            }

            @Override
            public StreamShape inputShape() {
                return StreamShape.DOUBLE_VALUE;
            }

            @Override
            public void accept(double t) {
                consumer.accept(t);
            }
        }
    }

    /** 用于执行并行 for-each 操作的 {@code ForkJoinTask} */
    @SuppressWarnings("serial")
    static final class ForEachTask<S, T> extends CountedCompleter<Void> {
        private Spliterator<S> spliterator;
        private final Sink<S> sink;
        private final PipelineHelper<T> helper;
        private long targetSize;

        ForEachTask(PipelineHelper<T> helper,
                    Spliterator<S> spliterator,
                    Sink<S> sink) {
            super(null);
            this.sink = sink;
            this.helper = helper;
            this.spliterator = spliterator;
            this.targetSize = 0L;
        }

        ForEachTask(ForEachTask<S, T> parent, Spliterator<S> spliterator) {
            super(parent);
            this.spliterator = spliterator;
            this.sink = parent.sink;
            this.targetSize = parent.targetSize;
            this.helper = parent.helper;
        }

        // 类似于 AbstractTask，但不需要跟踪子任务
        public void compute() {
            Spliterator<S> rightSplit = spliterator, leftSplit;
            long sizeEstimate = rightSplit.estimateSize(), sizeThreshold;
            if ((sizeThreshold = targetSize) == 0L)
                targetSize = sizeThreshold = AbstractTask.suggestTargetSize(sizeEstimate);
            boolean isShortCircuit = StreamOpFlag.SHORT_CIRCUIT.isKnown(helper.getStreamAndOpFlags());
            boolean forkRight = false;
            Sink<S> taskSink = sink;
            ForEachTask<S, T> task = this;
            while (!isShortCircuit || !taskSink.cancellationRequested()) {
                if (sizeEstimate <= sizeThreshold ||
                    (leftSplit = rightSplit.trySplit()) == null) {
                    task.helper.copyInto(taskSink, rightSplit);
                    break;
                }
                ForEachTask<S, T> leftTask = new ForEachTask<>(task, leftSplit);
                task.addToPendingCount(1);
                ForEachTask<S, T> taskToFork;
                if (forkRight) {
                    forkRight = false;
                    rightSplit = leftSplit;
                    taskToFork = task;
                    task = leftTask;
                }
                else {
                    forkRight = true;
                    taskToFork = leftTask;
                }
                taskToFork.fork();
                sizeEstimate = rightSplit.estimateSize();
            }
            task.spliterator = null;
            task.propagateCompletion();
        }
    }

    /**
     * 用于执行并行 for-each 操作的 {@code ForkJoinTask}，该操作按遇到的顺序访问元素
     */
    @SuppressWarnings("serial")
    static final class ForEachOrderedTask<S, T> extends CountedCompleter<Void> {
        /*
         * 我们的目标是确保与任务关联的元素按照计算树的中序遍历顺序进行处理。
         * 我们使用完成计数来表示这些依赖关系，以便任务在该顺序中所有前置任务完成之前不会完成。
         * 我们使用“完成映射”将任何左子节点映射到该顺序中的下一个任务。
         * 我们将此类映射右侧的任何节点的待处理计数增加一，以表示其依赖关系，
         * 当此类映射左侧的节点完成时，它会减少其对应右侧的待处理计数。
         * 当计算树通过拆分扩展时，我们必须原子地更新映射，以保持完成映射将左子节点映射到中序遍历中的下一个节点的不变性。
         *
         * 例如，考虑以下任务的计算树：
         *
         *       a
         *      / \
         *     b   c
         *    / \ / \
         *   d  e f  g
         *
         * 完成映射将包含（不一定同时）以下关联：
         *
         *   d -> e
         *   b -> f
         *   f -> g
         *
         * 任务 e, f, g 的待处理计数将增加 1。
         *
         * 以下关系成立：
         *
         *   - d 的完成“先于”e；
         *   - d 和 e 的完成“先于”b；
         *   - b 的完成“先于”f；以及
         *   - f 的完成“先于”g
         *
         * 因此，总体而言，由 forEachOrdered 操作指定的元素报告的“先于”关系成立。
         */


                    private final PipelineHelper<T> helper;
        private Spliterator<S> spliterator;
        private final long targetSize;
        private final ConcurrentHashMap<ForEachOrderedTask<S, T>, ForEachOrderedTask<S, T>> completionMap;
        private final Sink<T> action;
        private final ForEachOrderedTask<S, T> leftPredecessor;
        private Node<T> node;

        protected ForEachOrderedTask(PipelineHelper<T> helper,
                                     Spliterator<S> spliterator,
                                     Sink<T> action) {
            super(null);
            this.helper = helper;
            this.spliterator = spliterator;
            this.targetSize = AbstractTask.suggestTargetSize(spliterator.estimateSize());
            // 大小映射以避免并发调整大小
            this.completionMap = new ConcurrentHashMap<>(Math.max(16, AbstractTask.LEAF_TARGET << 1));
            this.action = action;
            this.leftPredecessor = null;
        }

        ForEachOrderedTask(ForEachOrderedTask<S, T> parent,
                           Spliterator<S> spliterator,
                           ForEachOrderedTask<S, T> leftPredecessor) {
            super(parent);
            this.helper = parent.helper;
            this.spliterator = spliterator;
            this.targetSize = parent.targetSize;
            this.completionMap = parent.completionMap;
            this.action = parent.action;
            this.leftPredecessor = leftPredecessor;
        }

        @Override
        public final void compute() {
            doCompute(this);
        }

        private static <S, T> void doCompute(ForEachOrderedTask<S, T> task) {
            Spliterator<S> rightSplit = task.spliterator, leftSplit;
            long sizeThreshold = task.targetSize;
            boolean forkRight = false;
            while (rightSplit.estimateSize() > sizeThreshold &&
                   (leftSplit = rightSplit.trySplit()) != null) {
                ForEachOrderedTask<S, T> leftChild =
                    new ForEachOrderedTask<>(task, leftSplit, task.leftPredecessor);
                ForEachOrderedTask<S, T> rightChild =
                    new ForEachOrderedTask<>(task, rightSplit, leftChild);

                // 分叉父任务
                // 左右子任务的完成发生在父任务完成之前
                task.addToPendingCount(1);
                // 左子任务的完成发生在右子任务完成之前
                rightChild.addToPendingCount(1);
                task.completionMap.put(leftChild, rightChild);

                // 如果任务不在左脊柱上
                if (task.leftPredecessor != null) {
                    /*
                     * 左前驱或左子树的完成发生在右子树最左边叶节点的完成之前。
                     * 在左子任务与完成映射关联之前，需要更新左子任务的待完成计数，否则左子任务可能会提前完成并违反“完成之前”的约束。
                     */
                    leftChild.addToPendingCount(1);
                    // 更新左前驱与右子树最左边叶节点的关联
                    if (task.completionMap.replace(task.leftPredecessor, task, leftChild)) {
                        // 如果替换成功，调整父任务的待完成计数，使其在子任务完成时完成
                        task.addToPendingCount(-1);
                    } else {
                        // 左前驱已经完成，父任务的待完成计数由左前驱调整；
                        // 左子任务准备好完成
                        leftChild.addToPendingCount(-1);
                    }
                }

                ForEachOrderedTask<S, T> taskToFork;
                if (forkRight) {
                    forkRight = false;
                    rightSplit = leftSplit;
                    task = leftChild;
                    taskToFork = rightChild;
                }
                else {
                    forkRight = true;
                    task = rightChild;
                    taskToFork = leftChild;
                }
                taskToFork.fork();
            }

            /*
             * 任务的待完成计数为0或1。如果为1，则完成映射将包含一个值为任务的项，需要两次调用tryComplete才能完成，一次在下面，另一次由任务的左前驱在onCompletion中触发。因此，在if块内没有数据竞争。
             */
            if (task.getPendingCount() > 0) {
                // 不能立即完成，因此将元素缓冲到Node中，以便在完成时使用
                @SuppressWarnings("unchecked")
                IntFunction<T[]> generator = size -> (T[]) new Object[size];
                Node.Builder<T> nb = task.helper.makeNodeBuilder(
                        task.helper.exactOutputSizeIfKnown(rightSplit),
                        generator);
                task.node = task.helper.wrapAndCopyInto(nb, rightSplit).build();
                task.spliterator = null;
            }
            task.tryComplete();
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if (node != null) {
                // 将此叶节点缓冲的元素转储到接收器中
                node.forEach(action);
                node = null;
            }
            else if (spliterator != null) {
                // 将此叶节点管道输出的元素转储到接收器中
                helper.wrapAndCopyInto(action, spliterator);
                spliterator = null;
            }

            // 该任务的完成*和*元素的转储发生在右子树最左边叶任务的完成之前（如果有，可以是此任务的右兄弟）
            //
            ForEachOrderedTask<S, T> leftDescendant = completionMap.remove(this);
            if (leftDescendant != null)
                leftDescendant.tryComplete();
        }
    }
}
