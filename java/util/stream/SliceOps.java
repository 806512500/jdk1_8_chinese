
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

import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.function.IntFunction;

/**
 * 生成短路状态中间操作实例的工厂，这些操作生成其输入流的子序列。
 *
 * @since 1.8
 */
final class SliceOps {

    // 没有实例
    private SliceOps() { }

    /**
     * 计算给定当前大小、跳过元素数和限制元素数的切片大小。
     *
     * @param size 当前大小
     * @param skip 要跳过的元素数，假设 >= 0
     * @param limit 要限制的元素数，假设 >= 0，如果无限制则值为 {@code Long.MAX_VALUE}
     * @return 切片大小
     */
    private static long calcSize(long size, long skip, long limit) {
        return size >= 0 ? Math.max(-1, Math.min(size - skip, limit)) : -1;
    }

    /**
     * 计算切片边界，即切片范围索引的下一个位置
     * @param skip 要跳过的元素数，假设 >= 0
     * @param limit 要限制的元素数，假设 >= 0，如果无限制则值为 {@code Long.MAX_VALUE}
     * @return 切片边界。
     */
    private static long calcSliceFence(long skip, long limit) {
        long sliceFence = limit >= 0 ? skip + limit : Long.MAX_VALUE;
        // 检查溢出
        return (sliceFence >= 0) ? sliceFence : Long.MAX_VALUE;
    }

    /**
     * 根据控制 Spliterator 类型的流形状创建切片 Spliterator。要求底层 Spliterator 必须是 SUBSIZED。
     */
    @SuppressWarnings("unchecked")
    private static <P_IN> Spliterator<P_IN> sliceSpliterator(StreamShape shape,
                                                             Spliterator<P_IN> s,
                                                             long skip, long limit) {
        assert s.hasCharacteristics(Spliterator.SUBSIZED);
        long sliceFence = calcSliceFence(skip, limit);
        switch (shape) {
            case REFERENCE:
                return new StreamSpliterators
                        .SliceSpliterator.OfRef<>(s, skip, sliceFence);
            case INT_VALUE:
                return (Spliterator<P_IN>) new StreamSpliterators
                        .SliceSpliterator.OfInt((Spliterator.OfInt) s, skip, sliceFence);
            case LONG_VALUE:
                return (Spliterator<P_IN>) new StreamSpliterators
                        .SliceSpliterator.OfLong((Spliterator.OfLong) s, skip, sliceFence);
            case DOUBLE_VALUE:
                return (Spliterator<P_IN>) new StreamSpliterators
                        .SliceSpliterator.OfDouble((Spliterator.OfDouble) s, skip, sliceFence);
            default:
                throw new IllegalStateException("未知形状 " + shape);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> IntFunction<T[]> castingArray() {
        return size -> (T[]) new Object[size];
    }

    /**
     * 向提供的流附加一个“切片”操作。切片操作可能是仅跳过、仅限制或跳过和限制。
     *
     * @param <T> 输入和输出元素的类型
     * @param upstream 元素类型为 T 的引用流
     * @param skip 要跳过的元素数。必须 >= 0。
     * @param limit 结果流的最大大小，或 -1 表示不施加限制
     */
    public static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream,
                                        long skip, long limit) {
        if (skip < 0)
            throw new IllegalArgumentException("跳过数必须非负: " + skip);

        return new ReferencePipeline.StatefulOp<T, T>(upstream, StreamShape.REFERENCE,
                                                      flags(limit)) {
            Spliterator<T> unorderedSkipLimitSpliterator(Spliterator<T> s,
                                                         long skip, long limit, long sizeIfKnown) {
                if (skip <= sizeIfKnown) {
                    // 如果要跳过的元素数 <= 已知管道大小，则仅使用限制
                    limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                    skip = 0;
                }
                return new StreamSpliterators.UnorderedSliceSpliterator.OfRef<>(s, skip, limit);
            }

            @Override
            <P_IN> Spliterator<T> opEvaluateParallelLazy(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    return new StreamSpliterators.SliceSpliterator.OfRef<>(
                            helper.wrapSpliterator(spliterator),
                            skip,
                            calcSliceFence(skip, limit));
                } else if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return unorderedSkipLimitSpliterator(
                            helper.wrapSpliterator(spliterator),
                            skip, limit, size);
                }
                else {
                    // @@@ 对于 LongStream.longs().filter(i -> true).limit(n)，无论 n 的值如何，都会发生 OOME
                    //     需要调整 SliceTask 的拆分目标大小，例如从 (size / k) 调整为 min(size / k, 1 << 14)
                    //     这将限制在叶节点创建的缓冲区大小
                    //     取消操作将更加积极地取消后续任务
                    //     如果给定任务已达到目标切片大小，则应取消任务，并清除任何本地结果
                    return new SliceTask<>(this, helper, spliterator, castingArray(), skip, limit).
                            invoke().spliterator();
                }
            }


                        @Override
            <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper,
                                              Spliterator<P_IN> spliterator,
                                              IntFunction<T[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    // 因为管道是 SIZED，所以可以从源创建切片迭代器，
                    // 这需要与源的形状匹配，并且可能比从管道包装的迭代器创建切片迭代器更高效
                    Spliterator<P_IN> s = sliceSpliterator(helper.getSourceShape(), spliterator, skip, limit);
                    return Nodes.collect(helper, s, true, generator);
                } else if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    Spliterator<T> s =  unorderedSkipLimitSpliterator(
                            helper.wrapSpliterator(spliterator),
                            skip, limit, size);
                    // 使用此管道进行收集，该管道为空，因此可以与管道包装的迭代器一起使用
                    // 注意，如果管道不是 SIZED，则不能从源迭代器创建切片迭代器
                    return Nodes.collect(this, s, true, generator);
                }
                else {
                    return new SliceTask<>(this, helper, spliterator, generator, skip, limit).
                            invoke();
                }
            }

            @Override
            Sink<T> opWrapSink(int flags, Sink<T> sink) {
                return new Sink.ChainedReference<T, T>(sink) {
                    long n = skip;
                    long m = limit >= 0 ? limit : Long.MAX_VALUE;

                    @Override
                    public void begin(long size) {
                        downstream.begin(calcSize(size, skip, m));
                    }

                    @Override
                    public void accept(T t) {
                        if (n == 0) {
                            if (m > 0) {
                                m--;
                                downstream.accept(t);
                            }
                        }
                        else {
                            n--;
                        }
                    }

                    @Override
                    public boolean cancellationRequested() {
                        return m == 0 || downstream.cancellationRequested();
                    }
                };
            }
        };
    }

    /**
     * 向提供的 IntStream 附加一个“切片”操作。切片操作可能是仅跳过、仅限制或跳过和限制。
     *
     * @param upstream 一个 IntStream
     * @param skip 要跳过的元素数量。必须 >= 0。
     * @param limit 结果流的最大大小，或 -1 表示不施加限制
     */
    public static IntStream makeInt(AbstractPipeline<?, Integer, ?> upstream,
                                    long skip, long limit) {
        if (skip < 0)
            throw new IllegalArgumentException("Skip must be non-negative: " + skip);

        return new IntPipeline.StatefulOp<Integer>(upstream, StreamShape.INT_VALUE,
                                                   flags(limit)) {
            Spliterator.OfInt unorderedSkipLimitSpliterator(
                    Spliterator.OfInt s, long skip, long limit, long sizeIfKnown) {
                if (skip <= sizeIfKnown) {
                    // 如果要跳过的元素数量 <= 已知的管道大小，则仅使用限制
                    limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                    skip = 0;
                }
                return new StreamSpliterators.UnorderedSliceSpliterator.OfInt(s, skip, limit);
            }

            @Override
            <P_IN> Spliterator<Integer> opEvaluateParallelLazy(PipelineHelper<Integer> helper,
                                                               Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    return new StreamSpliterators.SliceSpliterator.OfInt(
                            (Spliterator.OfInt) helper.wrapSpliterator(spliterator),
                            skip,
                            calcSliceFence(skip, limit));
                } else if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return unorderedSkipLimitSpliterator(
                            (Spliterator.OfInt) helper.wrapSpliterator(spliterator),
                            skip, limit, size);
                }
                else {
                    return new SliceTask<>(this, helper, spliterator, Integer[]::new, skip, limit).
                            invoke().spliterator();
                }
            }

            @Override
            <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> helper,
                                                    Spliterator<P_IN> spliterator,
                                                    IntFunction<Integer[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    // 因为管道是 SIZED，所以可以从源创建切片迭代器，
                    // 这需要与源的形状匹配，并且可能比从管道包装的迭代器创建切片迭代器更高效
                    Spliterator<P_IN> s = sliceSpliterator(helper.getSourceShape(), spliterator, skip, limit);
                    return Nodes.collectInt(helper, s, true);
                } else if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    Spliterator.OfInt s =  unorderedSkipLimitSpliterator(
                            (Spliterator.OfInt) helper.wrapSpliterator(spliterator),
                            skip, limit, size);
                    // 使用此管道进行收集，该管道为空，因此可以与管道包装的迭代器一起使用
                    // 注意，如果管道不是 SIZED，则不能从源迭代器创建切片迭代器
                    return Nodes.collectInt(this, s, true);
                }
                else {
                    return new SliceTask<>(this, helper, spliterator, generator, skip, limit).
                            invoke();
                }
            }


                        @Override
            Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink) {
                    long n = skip;
                    long m = limit >= 0 ? limit : Long.MAX_VALUE;

                    @Override
                    public void begin(long size) {
                        downstream.begin(calcSize(size, skip, m));
                    }

                    @Override
                    public void accept(int t) {
                        if (n == 0) {
                            if (m > 0) {
                                m--;
                                downstream.accept(t);
                            }
                        }
                        else {
                            n--;
                        }
                    }

                    @Override
                    public boolean cancellationRequested() {
                        return m == 0 || downstream.cancellationRequested();
                    }
                };
            }
        };
    }

    /**
     * 向提供的 LongStream 附加一个 "切片" 操作。切片操作可能是仅跳过、仅限制或跳过和限制。
     *
     * @param upstream 一个 LongStream
     * @param skip 要跳过的元素数量。必须 >= 0。
     * @param limit 结果流的最大大小，或 -1 表示不施加限制
     */
    public static LongStream makeLong(AbstractPipeline<?, Long, ?> upstream,
                                      long skip, long limit) {
        if (skip < 0)
            throw new IllegalArgumentException("Skip must be non-negative: " + skip);

        return new LongPipeline.StatefulOp<Long>(upstream, StreamShape.LONG_VALUE,
                                                 flags(limit)) {
            Spliterator.OfLong unorderedSkipLimitSpliterator(
                    Spliterator.OfLong s, long skip, long limit, long sizeIfKnown) {
                if (skip <= sizeIfKnown) {
                    // 如果要跳过的元素数量 <= 已知的管道大小，则仅使用限制
                    limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                    skip = 0;
                }
                return new StreamSpliterators.UnorderedSliceSpliterator.OfLong(s, skip, limit);
            }

            @Override
            <P_IN> Spliterator<Long> opEvaluateParallelLazy(PipelineHelper<Long> helper,
                                                            Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    return new StreamSpliterators.SliceSpliterator.OfLong(
                            (Spliterator.OfLong) helper.wrapSpliterator(spliterator),
                            skip,
                            calcSliceFence(skip, limit));
                } else if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return unorderedSkipLimitSpliterator(
                            (Spliterator.OfLong) helper.wrapSpliterator(spliterator),
                            skip, limit, size);
                }
                else {
                    return new SliceTask<>(this, helper, spliterator, Long[]::new, skip, limit).
                            invoke().spliterator();
                }
            }

            @Override
            <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> helper,
                                                 Spliterator<P_IN> spliterator,
                                                 IntFunction<Long[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    // 由于管道是 SIZED，可以从源创建切片迭代器，这需要与源的形状匹配，
                    // 并且可能比从管道包装的迭代器创建切片迭代器更高效
                    Spliterator<P_IN> s = sliceSpliterator(helper.getSourceShape(), spliterator, skip, limit);
                    return Nodes.collectLong(helper, s, true);
                } else if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    Spliterator.OfLong s =  unorderedSkipLimitSpliterator(
                            (Spliterator.OfLong) helper.wrapSpliterator(spliterator),
                            skip, limit, size);
                    // 使用此管道收集，该管道为空，因此可以与管道包装的迭代器一起使用
                    // 注意，如果管道不是 SIZED，则不能从源迭代器创建切片迭代器
                    return Nodes.collectLong(this, s, true);
                }
                else {
                    return new SliceTask<>(this, helper, spliterator, generator, skip, limit).
                            invoke();
                }
            }

            @Override
            Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedLong<Long>(sink) {
                    long n = skip;
                    long m = limit >= 0 ? limit : Long.MAX_VALUE;

                    @Override
                    public void begin(long size) {
                        downstream.begin(calcSize(size, skip, m));
                    }

                    @Override
                    public void accept(long t) {
                        if (n == 0) {
                            if (m > 0) {
                                m--;
                                downstream.accept(t);
                            }
                        }
                        else {
                            n--;
                        }
                    }


@Override
public boolean cancellationRequested() {
    return m == 0 || downstream.cancellationRequested();
}
};
}
};
}

/**
 * Appends a "slice" operation to the provided DoubleStream.  The slice
 * operation may be may be skip-only, limit-only, or skip-and-limit.
 *
 * @param upstream A DoubleStream
 * @param skip The number of elements to skip.  Must be >= 0.
 * @param limit The maximum size of the resulting stream, or -1 if no limit
 *        is to be imposed
 */
public static DoubleStream makeDouble(AbstractPipeline<?, Double, ?> upstream,
                                      long skip, long limit) {
    if (skip < 0)
        throw new IllegalArgumentException("Skip must be non-negative: " + skip);

    return new DoublePipeline.StatefulOp<Double>(upstream, StreamShape.DOUBLE_VALUE,
                                                 flags(limit)) {
        Spliterator.OfDouble unorderedSkipLimitSpliterator(
                Spliterator.OfDouble s, long skip, long limit, long sizeIfKnown) {
            if (skip <= sizeIfKnown) {
                // Use just the limit if the number of elements
                // to skip is <= the known pipeline size
                limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                skip = 0;
            }
            return new StreamSpliterators.UnorderedSliceSpliterator.OfDouble(s, skip, limit);
        }

        @Override
        <P_IN> Spliterator<Double> opEvaluateParallelLazy(PipelineHelper<Double> helper,
                                                          Spliterator<P_IN> spliterator) {
            long size = helper.exactOutputSizeIfKnown(spliterator);
            if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                return new StreamSpliterators.SliceSpliterator.OfDouble(
                        (Spliterator.OfDouble) helper.wrapSpliterator(spliterator),
                        skip,
                        calcSliceFence(skip, limit));
            } else if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                return unorderedSkipLimitSpliterator(
                        (Spliterator.OfDouble) helper.wrapSpliterator(spliterator),
                        skip, limit, size);
            }
            else {
                return new SliceTask<>(this, helper, spliterator, Double[]::new, skip, limit).
                        invoke().spliterator();
            }
        }

        @Override
        <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper,
                                               Spliterator<P_IN> spliterator,
                                               IntFunction<Double[]> generator) {
            long size = helper.exactOutputSizeIfKnown(spliterator);
            if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                // Because the pipeline is SIZED the slice spliterator
                // can be created from the source, this requires matching
                // to shape of the source, and is potentially more efficient
                // than creating the slice spliterator from the pipeline
                // wrapping spliterator
                Spliterator<P_IN> s = sliceSpliterator(helper.getSourceShape(), spliterator, skip, limit);
                return Nodes.collectDouble(helper, s, true);
            } else if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                Spliterator.OfDouble s =  unorderedSkipLimitSpliterator(
                        (Spliterator.OfDouble) helper.wrapSpliterator(spliterator),
                        skip, limit, size);
                // Collect using this pipeline, which is empty and therefore
                // can be used with the pipeline wrapping spliterator
                // Note that we cannot create a slice spliterator from
                // the source spliterator if the pipeline is not SIZED
                return Nodes.collectDouble(this, s, true);
            }
            else {
                return new SliceTask<>(this, helper, spliterator, generator, skip, limit).
                        invoke();
            }
        }

        @Override
        Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            return new Sink.ChainedDouble<Double>(sink) {
                long n = skip;
                long m = limit >= 0 ? limit : Long.MAX_VALUE;

                @Override
                public void begin(long size) {
                    downstream.begin(calcSize(size, skip, m));
                }

                @Override
                public void accept(double t) {
                    if (n == 0) {
                        if (m > 0) {
                            m--;
                            downstream.accept(t);
                        }
                    }
                    else {
                        n--;
                    }
                }

                @Override
                public boolean cancellationRequested() {
                    return m == 0 || downstream.cancellationRequested();
                }
            };
        }
    };
}

private static int flags(long limit) {
    return StreamOpFlag.NOT_SIZED | ((limit != -1) ? StreamOpFlag.IS_SHORT_CIRCUIT : 0);
}

/**
 * {@code ForkJoinTask} implementing slice computation.
 *
 * @param <P_IN> Input element type to the stream pipeline
 * @param <P_OUT> Output element type from the stream pipeline
 */
@SuppressWarnings("serial")
private static final class SliceTask<P_IN, P_OUT>
        extends AbstractShortCircuitTask<P_IN, P_OUT, Node<P_OUT>, SliceTask<P_IN, P_OUT>> {
    private final AbstractPipeline<P_OUT, P_OUT, ?> op;
    private final IntFunction<P_OUT[]> generator;
    private final long targetOffset, targetSize;
    private long thisNodeSize;


                    private volatile boolean completed;

        SliceTask(AbstractPipeline<P_OUT, P_OUT, ?> op,
                  PipelineHelper<P_OUT> helper,
                  Spliterator<P_IN> spliterator,
                  IntFunction<P_OUT[]> generator,
                  long offset, long size) {
            super(helper, spliterator);
            this.op = op;
            this.generator = generator;
            this.targetOffset = offset;
            this.targetSize = size;
        }

        SliceTask(SliceTask<P_IN, P_OUT> parent, Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.op = parent.op;
            this.generator = parent.generator;
            this.targetOffset = parent.targetOffset;
            this.targetSize = parent.targetSize;
        }

        @Override
        protected SliceTask<P_IN, P_OUT> makeChild(Spliterator<P_IN> spliterator) {
            return new SliceTask<>(this, spliterator);
        }

        @Override
        protected final Node<P_OUT> getEmptyResult() {
            return Nodes.emptyNode(op.getOutputShape());
        }

        @Override
        protected final Node<P_OUT> doLeaf() {
            if (isRoot()) {
                long sizeIfKnown = StreamOpFlag.SIZED.isPreserved(op.sourceOrOpFlags)
                                   ? op.exactOutputSizeIfKnown(spliterator)
                                   : -1;
                final Node.Builder<P_OUT> nb = op.makeNodeBuilder(sizeIfKnown, generator);
                Sink<P_OUT> opSink = op.opWrapSink(helper.getStreamAndOpFlags(), nb);
                helper.copyIntoWithCancel(helper.wrapSink(opSink), spliterator);
                // 无需截断，因为操作已经执行了元素的跳过和限制
                return nb.build();
            }
            else {
                Node<P_OUT> node = helper.wrapAndCopyInto(helper.makeNodeBuilder(-1, generator),
                                                          spliterator).build();
                thisNodeSize = node.count();
                completed = true;
                spliterator = null;
                return node;
            }
        }

        @Override
        public final void onCompletion(CountedCompleter<?> caller) {
            if (!isLeaf()) {
                Node<P_OUT> result;
                thisNodeSize = leftChild.thisNodeSize + rightChild.thisNodeSize;
                if (canceled) {
                    thisNodeSize = 0;
                    result = getEmptyResult();
                }
                else if (thisNodeSize == 0)
                    result = getEmptyResult();
                else if (leftChild.thisNodeSize == 0)
                    result = rightChild.getLocalResult();
                else {
                    result = Nodes.conc(op.getOutputShape(),
                                        leftChild.getLocalResult(), rightChild.getLocalResult());
                }
                setLocalResult(isRoot() ? doTruncate(result) : result);
                completed = true;
            }
            if (targetSize >= 0
                && !isRoot()
                && isLeftCompleted(targetOffset + targetSize))
                    cancelLaterNodes();

            super.onCompletion(caller);
        }

        @Override
        protected void cancel() {
            super.cancel();
            if (completed)
                setLocalResult(getEmptyResult());
        }

        private Node<P_OUT> doTruncate(Node<P_OUT> input) {
            long to = targetSize >= 0 ? Math.min(input.count(), targetOffset + targetSize) : thisNodeSize;
            return input.truncate(targetOffset, to, generator);
        }

        /**
         * 确定此节点及其左侧节点中完成的元素数量是否大于或等于目标大小。
         *
         * @param target 目标大小
         * @return 如果元素数量大于或等于目标大小，则返回 true，否则返回 false。
         */
        private boolean isLeftCompleted(long target) {
            long size = completed ? thisNodeSize : completedSize(target);
            if (size >= target)
                return true;
            for (SliceTask<P_IN, P_OUT> parent = getParent(), node = this;
                 parent != null;
                 node = parent, parent = parent.getParent()) {
                if (node == parent.rightChild) {
                    SliceTask<P_IN, P_OUT> left = parent.leftChild;
                    if (left != null) {
                        size += left.completedSize(target);
                        if (size >= target)
                            return true;
                    }
                }
            }
            return size >= target;
        }

        /**
         * 计算此节点中完成的元素数量。
         * <p>
         * 如果所有节点都已处理或完成的元素数量大于或等于目标大小，则计算终止。
         *
         * @param target 目标大小
         * @return 完成的元素数量
         */
        private long completedSize(long target) {
            if (completed)
                return thisNodeSize;
            else {
                SliceTask<P_IN, P_OUT> left = leftChild;
                SliceTask<P_IN, P_OUT> right = rightChild;
                if (left == null || right == null) {
                    // 必须已完成
                    return thisNodeSize;
                }
                else {
                    long leftSize = left.completedSize(target);
                    return (leftSize >= target) ? leftSize : leftSize + right.completedSize(target);
                }
            }
        }
    }
}
