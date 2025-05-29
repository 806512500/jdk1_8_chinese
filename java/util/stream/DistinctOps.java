
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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

/**
 * 用于将流转换为无重复元素流的工厂方法，使用 {@link Object#equals(Object)} 来确定相等性。
 *
 * @since 1.8
 */
final class DistinctOps {

    private DistinctOps() { }

    /**
     * 向提供的流附加一个“distinct”操作，并返回新的流。
     *
     * @param <T> 输入和输出元素的类型
     * @param upstream 元素类型为 T 的引用流
     * @return 新的流
     */
    static <T> ReferencePipeline<T, T> makeRef(AbstractPipeline<?, T, ?> upstream) {
        return new ReferencePipeline.StatefulOp<T, T>(upstream, StreamShape.REFERENCE,
                                                      StreamOpFlag.IS_DISTINCT | StreamOpFlag.NOT_SIZED) {

            <P_IN> Node<T> reduce(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
                // 如果流是排序的，那么它也应该是有序的，因此以下操作也将保留排序顺序
                TerminalOp<T, LinkedHashSet<T>> reduceOp
                        = ReduceOps.<T, LinkedHashSet<T>>makeRef(LinkedHashSet::new, LinkedHashSet::add,
                                                                 LinkedHashSet::addAll);
                return Nodes.node(reduceOp.evaluateParallel(helper, spliterator));
            }

            @Override
            <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper,
                                              Spliterator<P_IN> spliterator,
                                              IntFunction<T[]> generator) {
                if (StreamOpFlag.DISTINCT.isKnown(helper.getStreamAndOpFlags())) {
                    // 无操作
                    return helper.evaluate(spliterator, false, generator);
                }
                else if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return reduce(helper, spliterator);
                }
                else {
                    // 由于 ConcurrentHashMap 不支持 null 值，因此使用 AtomicBoolean 作为 null 状态的持有者
                    AtomicBoolean seenNull = new AtomicBoolean(false);
                    ConcurrentHashMap<T, Boolean> map = new ConcurrentHashMap<>();
                    TerminalOp<T, Void> forEachOp = ForEachOps.makeRef(t -> {
                        if (t == null)
                            seenNull.set(true);
                        else
                            map.putIfAbsent(t, Boolean.TRUE);
                    }, false);
                    forEachOp.evaluateParallel(helper, spliterator);

                    // 如果已看到 null，则将键集复制到支持 null 值的 HashSet 中，并添加 null
                    Set<T> keys = map.keySet();
                    if (seenNull.get()) {
                        // TODO 实现更有效的集合联合视图，而不是复制
                        keys = new HashSet<>(keys);
                        keys.add(null);
                    }
                    return Nodes.node(keys);
                }
            }

            @Override
            <P_IN> Spliterator<T> opEvaluateParallelLazy(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.DISTINCT.isKnown(helper.getStreamAndOpFlags())) {
                    // 无操作
                    return helper.wrapSpliterator(spliterator);
                }
                else if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    // 非惰性，需要屏障以保持顺序
                    return reduce(helper, spliterator).spliterator();
                }
                else {
                    // 惰性
                    return new StreamSpliterators.DistinctSpliterator<>(helper.wrapSpliterator(spliterator));
                }
            }

            @Override
            Sink<T> opWrapSink(int flags, Sink<T> sink) {
                Objects.requireNonNull(sink);

                if (StreamOpFlag.DISTINCT.isKnown(flags)) {
                    return sink;
                } else if (StreamOpFlag.SORTED.isKnown(flags)) {
                    return new Sink.ChainedReference<T, T>(sink) {
                        boolean seenNull;
                        T lastSeen;

                        @Override
                        public void begin(long size) {
                            seenNull = false;
                            lastSeen = null;
                            downstream.begin(-1);
                        }

                        @Override
                        public void end() {
                            seenNull = false;
                            lastSeen = null;
                            downstream.end();
                        }

                        @Override
                        public void accept(T t) {
                            if (t == null) {
                                if (!seenNull) {
                                    seenNull = true;
                                    downstream.accept(lastSeen = null);
                                }
                            } else if (lastSeen == null || !t.equals(lastSeen)) {
                                downstream.accept(lastSeen = t);
                            }
                        }
                    };
                } else {
                    return new Sink.ChainedReference<T, T>(sink) {
                        Set<T> seen;

                        @Override
                        public void begin(long size) {
                            seen = new HashSet<>();
                            downstream.begin(-1);
                        }


                                    @Override
                        public void end() {
                            seen = null;
                            downstream.end();
                        }

                        @Override
                        public void accept(T t) {
                            if (!seen.contains(t)) {
                                seen.add(t);
                                downstream.accept(t);
                            }
                        }
                    };
                }
            }
        };
    }
}
