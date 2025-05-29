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

import java.util.Spliterator;

/**
 * 流管道中的一个操作，该操作以流作为输入并生成结果或副作用。{@code TerminalOp} 具有输入类型和流形状，以及结果类型。{@code TerminalOp} 还有一组
 * <em>操作标志</em>，描述了操作如何处理流中的元素（例如短路或尊重遇到的顺序；参见 {@link StreamOpFlag}）。
 *
 * <p>{@code TerminalOp} 必须为给定的流源和一组中间操作提供顺序和并行实现。
 *
 * @param <E_IN> 输入元素的类型
 * @param <R>    结果的类型
 * @since 1.8
 */
interface TerminalOp<E_IN, R> {
    /**
     * 获取此操作的输入类型的形状。
     *
     * @implSpec 默认返回 {@code StreamShape.REFERENCE}。
     *
     * @return 此操作的输入类型的 StreamShape
     */
    default StreamShape inputShape() { return StreamShape.REFERENCE; }

    /**
     * 获取操作的流标志。终端操作可以设置 {@link StreamOpFlag} 中定义的流标志的有限子集，并且这些标志与管道中先前组合的流和中间操作标志结合。
     *
     * @implSpec 默认实现返回零。
     *
     * @return 此操作的流标志
     * @see StreamOpFlag
     */
    default int getOpFlags() { return 0; }

    /**
     * 使用指定的 {@code PipelineHelper}（描述上游中间操作）并行评估操作。
     *
     * @implSpec 默认使用指定的 {@code PipelineHelper} 顺序评估操作。
     *
     * @param helper 管道助手
     * @param spliterator 源 spliterator
     * @return 评估的结果
     */
    default <P_IN> R evaluateParallel(PipelineHelper<E_IN> helper,
                                      Spliterator<P_IN> spliterator) {
        if (Tripwire.ENABLED)
            Tripwire.trip(getClass(), "{0} triggering TerminalOp.evaluateParallel serial default");
        return evaluateSequential(helper, spliterator);
    }

    /**
     * 使用指定的 {@code PipelineHelper}（描述上游中间操作）顺序评估操作。
     *
     * @param helper 管道助手
     * @param spliterator 源 spliterator
     * @return 评估的结果
     */
    <P_IN> R evaluateSequential(PipelineHelper<E_IN> helper,
                                Spliterator<P_IN> spliterator);
}
