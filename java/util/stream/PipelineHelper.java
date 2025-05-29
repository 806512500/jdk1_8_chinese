
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
import java.util.function.IntFunction;

/**
 * 用于执行<a href="package-summary.html#StreamOps">流管道</a>的辅助类，捕获有关流管道的所有信息（输出形状、中间操作、流标志、并行性等）。
 *
 * <p>
 * 一个 {@code PipelineHelper} 描述了流管道的初始部分，包括其源、中间操作，并且可能还包括有关最后一个中间操作之后的终端（或有状态）操作的信息。
 * {@code PipelineHelper} 被传递给 {@link TerminalOp#evaluateParallel(PipelineHelper, java.util.Spliterator)},
 * {@link TerminalOp#evaluateSequential(PipelineHelper, java.util.Spliterator)}, 和
 * {@link AbstractPipeline#opEvaluateParallel(PipelineHelper, java.util.Spliterator, java.util.function.IntFunction)} 方法，
 * 这些方法可以使用 {@code PipelineHelper} 来访问有关管道的信息，如头部形状、流标志和大小，并使用辅助方法
 * 如 {@link #wrapAndCopyInto(Sink, Spliterator)}, {@link #copyInto(Sink, Spliterator)}, 和 {@link #wrapSink(Sink)} 来执行管道操作。
 *
 * @param <P_OUT> 管道输出元素的类型
 * @since 1.8
 */
abstract class PipelineHelper<P_OUT> {

    /**
     * 获取管道段源的流形状。
     *
     * @return 管道段源的流形状。
     */
    abstract StreamShape getSourceShape();

    /**
     * 获取描述的管道输出的组合流和操作标志。这将包含来自流源、所有中间操作和终端操作的流标志。
     *
     * @return 组合的流和操作标志
     * @see StreamOpFlag
     */
    abstract int getStreamAndOpFlags();

    /**
     * 返回应用此 {@code PipelineHelper} 描述的管道阶段到提供的 {@code Spliterator} 描述的输入部分的结果部分的确切输出大小，如果已知的话。如果未知或已知为无限，将返回 {@code -1}。
     *
     * @apiNote
     * 如果 {@code Spliterator} 具有 {@code SIZED} 特性，并且组合的流和操作标志中已知 {@link StreamOpFlag#SIZED} 操作标志，则确切的输出大小是已知的。
     *
     * @param spliterator 描述相关部分源数据的 spliterator
     * @return 如果已知，则返回确切的大小，如果无限或未知，则返回 -1
     */
    abstract<P_IN> long exactOutputSizeIfKnown(Spliterator<P_IN> spliterator);

    /**
     * 将此 {@code PipelineHelper} 描述的管道阶段应用于提供的 {@code Spliterator}，并将结果发送到提供的 {@code Sink}。
     *
     * @implSpec
     * 实现行为类似于：
     * <pre>{@code
     *     intoWrapped(wrapSink(sink), spliterator);
     * }</pre>
     *
     * @param sink 接收结果的 {@code Sink}
     * @param spliterator 描述要处理的源输入的 spliterator
     */
    abstract<P_IN, S extends Sink<P_OUT>> S wrapAndCopyInto(S sink, Spliterator<P_IN> spliterator);

    /**
     * 将从 {@code Spliterator} 获取的元素推送到提供的 {@code Sink}。如果已知流管道中包含短路阶段（参见 {@link StreamOpFlag#SHORT_CIRCUIT}），
     * 则在每个元素后检查 {@link Sink#cancellationRequested()}，如果请求取消则停止。
     *
     * @implSpec
     * 此方法符合在推送元素之前调用 {@code Sink.begin}，通过 {@code Sink.accept} 推送元素，以及在所有元素都被推送后调用 {@code Sink.end} 的 {@code Sink} 协议。
     *
     * @param wrappedSink 目标 {@code Sink}
     * @param spliterator 源 {@code Spliterator}
     */
    abstract<P_IN> void copyInto(Sink<P_IN> wrappedSink, Spliterator<P_IN> spliterator);

    /**
     * 将从 {@code Spliterator} 获取的元素推送到提供的 {@code Sink}，在每个元素后检查 {@link Sink#cancellationRequested()}，如果请求取消则停止。
     *
     * @implSpec
     * 此方法符合在推送元素之前调用 {@code Sink.begin}，通过 {@code Sink.accept} 推送元素，以及在所有元素都被推送后或请求取消时调用 {@code Sink.end} 的 {@code Sink} 协议。
     *
     * @param wrappedSink 目标 {@code Sink}
     * @param spliterator 源 {@code Spliterator}
     */
    abstract <P_IN> void copyIntoWithCancel(Sink<P_IN> wrappedSink, Spliterator<P_IN> spliterator);

    /**
     * 接受 {@code PipelineHelper} 输出类型的元素的 {@code Sink}，并将其包装为接受输入类型的 {@code Sink}，实现此 {@code PipelineHelper} 描述的所有中间操作，将结果传递到提供的 {@code Sink}。
     *
     * @param sink 接收结果的 {@code Sink}
     * @return 实现管道阶段并将结果发送到提供的 {@code Sink} 的 {@code Sink}
     */
    abstract<P_IN> Sink<P_IN> wrapSink(Sink<P_OUT> sink);

    /**
     *
     * @param spliterator
     * @param <P_IN>
     * @return
     */
    abstract<P_IN> Spliterator<P_OUT> wrapSpliterator(Spliterator<P_IN> spliterator);

    /**
     * 构造一个与此 {@code PipelineHelper} 的输出形状兼容的 @{link Node.Builder}。
     *
     * @param exactSizeIfKnown 如果 >=0，则创建一个具有固定容量的构建器，容量正好为 sizeIfKnown 个元素；如果 < 0，则构建器具有可变容量。如果在构建器达到容量后添加元素，固定容量构建器将失败。
     * @param generator 数组实例的工厂函数
     * @return 与此 {@code PipelineHelper} 的输出形状兼容的 {@code Node.Builder}
     */
    abstract Node.Builder<P_OUT> makeNodeBuilder(long exactSizeIfKnown,
                                                 IntFunction<P_OUT[]> generator);

                /**
     * 收集应用管道阶段到源 {@code Spliterator} 所产生的所有输出元素到一个 {@code Node} 中。
     *
     * @implNote
     * 如果管道没有中间操作，并且源是由一个 {@code Node} 支持的，那么该 {@code Node} 将被返回（或被展平后返回）。这减少了由状态操作后跟返回数组的终端操作组成的管道的复制，例如：
     * <pre>{@code
     *     stream.sorted().toArray();
     * }</pre>
     *
     * @param spliterator 源 {@code Spliterator}
     * @param flatten 如果为 true 且管道是并行管道，则返回的 {@code Node} 将不包含任何子节点；否则，{@code Node} 可能表示反映计算树形状的树的根。
     * @param generator 用于创建数组实例的工厂函数
     * @return 包含所有输出元素的 {@code Node}
     */
    abstract<P_IN> Node<P_OUT> evaluate(Spliterator<P_IN> spliterator,
                                        boolean flatten,
                                        IntFunction<P_OUT[]> generator);
}
