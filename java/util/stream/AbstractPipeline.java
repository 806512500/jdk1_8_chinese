
/*
 * Copyright (c) 2012, 2014, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * 抽象基类，用于实现 Stream 接口及其原始类型特化的“管道”类。管理流管道的构建和评估。
 *
 * <p>{@code AbstractPipeline} 表示流管道的初始部分，封装了流源和零个或多个中间操作。各个 {@code AbstractPipeline} 对象通常被称为 <em>阶段</em>，每个阶段描述了流源或中间操作。
 *
 * <p>具体的中间阶段通常由 {@code AbstractPipeline} 构建，特定形状的管道类（例如 {@code IntPipeline}）扩展它，该类也是抽象的，以及特定操作的具象类扩展该类。{@code AbstractPipeline} 包含了评估管道的大部分机制，并实现了操作将使用的方法；特定形状的类添加了用于将结果收集到适当形状的容器中的辅助方法。
 *
 * <p>在链接新的中间操作或执行终端操作后，流被视为已消耗，不允许在此流实例上进行更多的中间或终端操作。
 *
 * @implNote
 * <p>对于顺序流和没有 <a href="package-summary.html#StreamOps">状态中间操作</a> 的并行流，管道评估是在单次遍历中“压缩”所有操作完成的。对于具有状态操作的并行流，执行被划分为段，每个状态操作标记一个段的结束，每个段分别评估，结果用作下一个段的输入。在所有情况下，直到终端操作开始时，源数据才被消耗。
 *
 * @param <E_IN>  输入元素的类型
 * @param <E_OUT> 输出元素的类型
 * @param <S> 实现 {@code BaseStream} 的子类类型
 * @since 1.8
 */
abstract class AbstractPipeline<E_IN, E_OUT, S extends BaseStream<E_OUT, S>>
        extends PipelineHelper<E_OUT> implements BaseStream<E_OUT, S> {
    private static final String MSG_STREAM_LINKED = "流已操作或关闭";
    private static final String MSG_CONSUMED = "源已消耗或关闭";

    /**
     * 回链到管道链的头部（如果这是源阶段，则为自身）。
     */
    @SuppressWarnings("rawtypes")
    private final AbstractPipeline sourceStage;

    /**
     * 上游管道，如果这是源阶段，则为 null。
     */
    @SuppressWarnings("rawtypes")
    private final AbstractPipeline previousStage;

    /**
     * 由该管道对象表示的中间操作的操作标志。
     */
    protected final int sourceOrOpFlags;

    /**
     * 下一个管道阶段，如果这是最后一个阶段，则为 null。在链接到下一个管道时实际上是最终的。
     */
    @SuppressWarnings("rawtypes")
    private AbstractPipeline nextStage;

    /**
     * 从该管道对象到流源之间的中间操作数，如果是顺序的，或者到并行的前一个状态操作数。在管道准备评估时有效。
     */
    private int depth;

    /**
     * 源和所有操作（包括由该管道对象表示的操作）的组合源和操作标志。在管道准备评估时有效。
     */
    private int combinedFlags;

    /**
     * 源拆分器。仅对头部管道有效。在管道未消耗时，如果非空，则 {@code sourceSupplier} 必须为 null。在管道消耗后，如果非空，则设置为 null。
     */
    private Spliterator<?> sourceSpliterator;

    /**
     * 源供应器。仅对头部管道有效。在管道未消耗时，如果非空，则 {@code sourceSpliterator} 必须为 null。在管道消耗后，如果非空，则设置为 null。
     */
    private Supplier<? extends Spliterator<?>> sourceSupplier;

    /**
     * 如果此管道已链接或消耗，则为 true。
     */
    private boolean linkedOrConsumed;

    /**
     * 如果管道中存在任何状态操作，则为 true；仅对源阶段有效。
     */
    private boolean sourceAnyStateful;

    private Runnable sourceCloseAction;

    /**
     * 如果管道是并行的，则为 true，否则为顺序；仅对源阶段有效。
     */
    private boolean parallel;

    /**
     * 构造流管道的头部。
     *
     * @param source 描述流源的 {@code Supplier<Spliterator>}
     * @param sourceFlags 流源的源标志，描述见 {@link StreamOpFlag}
     * @param parallel 如果管道是并行的，则为 true
     */
    AbstractPipeline(Supplier<? extends Spliterator<?>> source,
                     int sourceFlags, boolean parallel) {
        this.previousStage = null;
        this.sourceSupplier = source;
        this.sourceStage = this;
        this.sourceOrOpFlags = sourceFlags & StreamOpFlag.STREAM_MASK;
        // 以下是一个优化：
        // StreamOpFlag.combineOpFlags(sourceOrOpFlags, StreamOpFlag.INITIAL_OPS_VALUE);
        this.combinedFlags = (~(sourceOrOpFlags << 1)) & StreamOpFlag.INITIAL_OPS_VALUE;
        this.depth = 0;
        this.parallel = parallel;
    }

    /**
     * 构造流管道的头部。
     *
     * @param source 描述流源的 {@code Spliterator}
     * @param sourceFlags 流源的源标志，描述见 {@link StreamOpFlag}
     * @param parallel 如果管道是并行的，则为 true
     */
    AbstractPipeline(Spliterator<?> source,
                     int sourceFlags, boolean parallel) {
        this.previousStage = null;
        this.sourceSpliterator = source;
        this.sourceStage = this;
        this.sourceOrOpFlags = sourceFlags & StreamOpFlag.STREAM_MASK;
        // 以下是一个优化：
        // StreamOpFlag.combineOpFlags(sourceOrOpFlags, StreamOpFlag.INITIAL_OPS_VALUE);
        this.combinedFlags = (~(sourceOrOpFlags << 1)) & StreamOpFlag.INITIAL_OPS_VALUE;
        this.depth = 0;
        this.parallel = parallel;
    }

    /**
     * 构造一个中间操作阶段并附加到现有管道。
     *
     * @param previousStage 上游管道阶段
     * @param opFlags 新阶段的操作标志，描述见 {@link StreamOpFlag}
     */
    AbstractPipeline(AbstractPipeline<?, E_IN, ?> previousStage, int opFlags) {
        if (previousStage.linkedOrConsumed)
            throw new IllegalStateException(MSG_STREAM_LINKED);
        previousStage.linkedOrConsumed = true;
        previousStage.nextStage = this;

        this.previousStage = previousStage;
        this.sourceOrOpFlags = opFlags & StreamOpFlag.OP_MASK;
        this.combinedFlags = StreamOpFlag.combineOpFlags(opFlags, previousStage.combinedFlags);
        this.sourceStage = previousStage.sourceStage;
        if (opIsStateful())
            sourceStage.sourceAnyStateful = true;
        this.depth = previousStage.depth + 1;
    }


    // 终端评估方法

    /**
     * 使用终端操作评估管道以生成结果。
     *
     * @param <R> 结果的类型
     * @param terminalOp 要应用于管道的终端操作。
     * @return 结果
     */
    final <R> R evaluate(TerminalOp<E_OUT, R> terminalOp) {
        assert getOutputShape() == terminalOp.inputShape();
        if (linkedOrConsumed)
            throw new IllegalStateException(MSG_STREAM_LINKED);
        linkedOrConsumed = true;

        return isParallel()
               ? terminalOp.evaluateParallel(this, sourceSpliterator(terminalOp.getOpFlags()))
               : terminalOp.evaluateSequential(this, sourceSpliterator(terminalOp.getOpFlags()));
    }

    /**
     * 收集从管道阶段输出的元素。
     *
     * @param generator 用于创建数组实例的数组生成器
     * @return 包含收集的输出元素的扁平数组支持的 Node
     */
    @SuppressWarnings("unchecked")
    final Node<E_OUT> evaluateToArrayNode(IntFunction<E_OUT[]> generator) {
        if (linkedOrConsumed)
            throw new IllegalStateException(MSG_STREAM_LINKED);
        linkedOrConsumed = true;

        // 如果最后一个中间操作是状态操作，则直接评估以避免额外的收集步骤
        if (isParallel() && previousStage != null && opIsStateful()) {
            // 将此最后一个管道阶段的深度设置为零，以切分管道，使此操作不包含在上游切片中，上游操作也不包含在此切片中
            depth = 0;
            return opEvaluateParallel(previousStage, previousStage.sourceSpliterator(0), generator);
        }
        else {
            return evaluate(sourceSpliterator(0), true, generator);
        }
    }

    /**
     * 如果此管道阶段是源阶段，则获取源阶段的拆分器。调用此方法并成功返回后，管道将被消耗。
     *
     * @return 源阶段的拆分器
     * @throws IllegalStateException 如果此管道阶段不是源阶段。
     */
    @SuppressWarnings("unchecked")
    final Spliterator<E_OUT> sourceStageSpliterator() {
        if (this != sourceStage)
            throw new IllegalStateException();

        if (linkedOrConsumed)
            throw new IllegalStateException(MSG_STREAM_LINKED);
        linkedOrConsumed = true;

        if (sourceStage.sourceSpliterator != null) {
            @SuppressWarnings("unchecked")
            Spliterator<E_OUT> s = sourceStage.sourceSpliterator;
            sourceStage.sourceSpliterator = null;
            return s;
        }
        else if (sourceStage.sourceSupplier != null) {
            @SuppressWarnings("unchecked")
            Spliterator<E_OUT> s = (Spliterator<E_OUT>) sourceStage.sourceSupplier.get();
            sourceStage.sourceSupplier = null;
            return s;
        }
        else {
            throw new IllegalStateException(MSG_CONSUMED);
        }
    }

    // BaseStream

    @Override
    @SuppressWarnings("unchecked")
    public final S sequential() {
        sourceStage.parallel = false;
        return (S) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final S parallel() {
        sourceStage.parallel = true;
        return (S) this;
    }

    @Override
    public void close() {
        linkedOrConsumed = true;
        sourceSupplier = null;
        sourceSpliterator = null;
        if (sourceStage.sourceCloseAction != null) {
            Runnable closeAction = sourceStage.sourceCloseAction;
            sourceStage.sourceCloseAction = null;
            closeAction.run();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public S onClose(Runnable closeHandler) {
        Objects.requireNonNull(closeHandler);
        Runnable existingHandler = sourceStage.sourceCloseAction;
        sourceStage.sourceCloseAction =
                (existingHandler == null)
                ? closeHandler
                : Streams.composeWithExceptions(existingHandler, closeHandler);
        return (S) this;
    }

    // 原始类型特化使用协变覆盖，因此不是 final
    @Override
    @SuppressWarnings("unchecked")
    public Spliterator<E_OUT> spliterator() {
        if (linkedOrConsumed)
            throw new IllegalStateException(MSG_STREAM_LINKED);
        linkedOrConsumed = true;

        if (this == sourceStage) {
            if (sourceStage.sourceSpliterator != null) {
                @SuppressWarnings("unchecked")
                Spliterator<E_OUT> s = (Spliterator<E_OUT>) sourceStage.sourceSpliterator;
                sourceStage.sourceSpliterator = null;
                return s;
            }
            else if (sourceStage.sourceSupplier != null) {
                @SuppressWarnings("unchecked")
                Supplier<Spliterator<E_OUT>> s = (Supplier<Spliterator<E_OUT>>) sourceStage.sourceSupplier;
                sourceStage.sourceSupplier = null;
                return lazySpliterator(s);
            }
            else {
                throw new IllegalStateException(MSG_CONSUMED);
            }
        }
        else {
            return wrap(this, () -> sourceSpliterator(0), isParallel());
        }
    }

    @Override
    public final boolean isParallel() {
        return sourceStage.parallel;
    }


    /**
     * 返回流源和所有中间操作的流标志的组合。
     *
     * @return 流源和所有中间操作的流标志的组合
     * @see StreamOpFlag
     */
    final int getStreamFlags() {
        return StreamOpFlag.toStreamFlags(combinedFlags);
    }

    /**
     * 获取此管道阶段的源拆分器。对于顺序或无状态并行管道，这是源拆分器。对于有状态并行管道，这是描述所有计算结果的拆分器，包括最近的状态操作。
     */
    @SuppressWarnings("unchecked")
    private Spliterator<?> sourceSpliterator(int terminalFlags) {
        // 获取管道的源拆分器
        Spliterator<?> spliterator = null;
        if (sourceStage.sourceSpliterator != null) {
            spliterator = sourceStage.sourceSpliterator;
            sourceStage.sourceSpliterator = null;
        }
        else if (sourceStage.sourceSupplier != null) {
            spliterator = (Spliterator<?>) sourceStage.sourceSupplier.get();
            sourceStage.sourceSupplier = null;
        }
        else {
            throw new IllegalStateException(MSG_CONSUMED);
        }


                    if (isParallel() && sourceStage.sourceAnyStateful) {
            // 适配源拆分器，评估管道中直到包括此管道阶段的所有状态操作。
            // 每个管道阶段的深度和标志相应地进行调整。
            int depth = 1;
            for (@SuppressWarnings("rawtypes") AbstractPipeline u = sourceStage, p = sourceStage.nextStage, e = this;
                 u != e;
                 u = p, p = p.nextStage) {

                int thisOpFlags = p.sourceOrOpFlags;
                if (p.opIsStateful()) {
                    depth = 0;

                    if (StreamOpFlag.SHORT_CIRCUIT.isKnown(thisOpFlags)) {
                        // 为下一个管道阶段清除短路标志
                        // 本阶段封装了短路，下一个阶段可能没有短路操作，
                        // 如果是这样，应使用 spliterator.forEachRemaining 进行遍历
                        thisOpFlags = thisOpFlags & ~StreamOpFlag.IS_SHORT_CIRCUIT;
                    }

                    spliterator = p.opEvaluateParallelLazy(u, spliterator);

                    // 根据阶段的拆分器注入或清除 SIZED 标志
                    thisOpFlags = spliterator.hasCharacteristics(Spliterator.SIZED)
                            ? (thisOpFlags & ~StreamOpFlag.NOT_SIZED) | StreamOpFlag.IS_SIZED
                            : (thisOpFlags & ~StreamOpFlag.IS_SIZED) | StreamOpFlag.NOT_SIZED;
                }
                p.depth = depth++;
                p.combinedFlags = StreamOpFlag.combineOpFlags(thisOpFlags, u.combinedFlags);
            }
        }

        if (terminalFlags != 0)  {
            // 将终端操作的标志应用到最后一个管道阶段
            combinedFlags = StreamOpFlag.combineOpFlags(terminalFlags, combinedFlags);
        }

        return spliterator;
    }

    // PipelineHelper

    @Override
    final StreamShape getSourceShape() {
        @SuppressWarnings("rawtypes")
        AbstractPipeline p = AbstractPipeline.this;
        while (p.depth > 0) {
            p = p.previousStage;
        }
        return p.getOutputShape();
    }

    @Override
    final <P_IN> long exactOutputSizeIfKnown(Spliterator<P_IN> spliterator) {
        return StreamOpFlag.SIZED.isKnown(getStreamAndOpFlags()) ? spliterator.getExactSizeIfKnown() : -1;
    }

    @Override
    final <P_IN, S extends Sink<E_OUT>> S wrapAndCopyInto(S sink, Spliterator<P_IN> spliterator) {
        copyInto(wrapSink(Objects.requireNonNull(sink)), spliterator);
        return sink;
    }

    @Override
    final <P_IN> void copyInto(Sink<P_IN> wrappedSink, Spliterator<P_IN> spliterator) {
        Objects.requireNonNull(wrappedSink);

        if (!StreamOpFlag.SHORT_CIRCUIT.isKnown(getStreamAndOpFlags())) {
            wrappedSink.begin(spliterator.getExactSizeIfKnown());
            spliterator.forEachRemaining(wrappedSink);
            wrappedSink.end();
        }
        else {
            copyIntoWithCancel(wrappedSink, spliterator);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    final <P_IN> void copyIntoWithCancel(Sink<P_IN> wrappedSink, Spliterator<P_IN> spliterator) {
        @SuppressWarnings({"rawtypes","unchecked"})
        AbstractPipeline p = AbstractPipeline.this;
        while (p.depth > 0) {
            p = p.previousStage;
        }
        wrappedSink.begin(spliterator.getExactSizeIfKnown());
        p.forEachWithCancel(spliterator, wrappedSink);
        wrappedSink.end();
    }

    @Override
    final int getStreamAndOpFlags() {
        return combinedFlags;
    }

    final boolean isOrdered() {
        return StreamOpFlag.ORDERED.isKnown(combinedFlags);
    }

    @Override
    @SuppressWarnings("unchecked")
    final <P_IN> Sink<P_IN> wrapSink(Sink<E_OUT> sink) {
        Objects.requireNonNull(sink);

        for ( @SuppressWarnings("rawtypes") AbstractPipeline p=AbstractPipeline.this; p.depth > 0; p=p.previousStage) {
            sink = p.opWrapSink(p.previousStage.combinedFlags, sink);
        }
        return (Sink<P_IN>) sink;
    }

    @Override
    @SuppressWarnings("unchecked")
    final <P_IN> Spliterator<E_OUT> wrapSpliterator(Spliterator<P_IN> sourceSpliterator) {
        if (depth == 0) {
            return (Spliterator<E_OUT>) sourceSpliterator;
        }
        else {
            return wrap(this, () -> sourceSpliterator, isParallel());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    final <P_IN> Node<E_OUT> evaluate(Spliterator<P_IN> spliterator,
                                      boolean flatten,
                                      IntFunction<E_OUT[]> generator) {
        if (isParallel()) {
            // @@@ 如果此管道阶段的操作是状态操作，则进行优化
            return evaluateToNode(this, spliterator, flatten, generator);
        }
        else {
            Node.Builder<E_OUT> nb = makeNodeBuilder(
                    exactOutputSizeIfKnown(spliterator), generator);
            return wrapAndCopyInto(nb, spliterator).build();
        }
    }


    // 形状特定的抽象方法，由 XxxPipeline 类实现

    /**
     * 获取管道的输出形状。如果管道是头部，
     * 则其输出形状对应于源的形状。
     * 否则，其输出形状对应于关联操作的输出形状。
     *
     * @return 输出形状
     */
    abstract StreamShape getOutputShape();

    /**
     * 将管道输出的元素收集到一个 Node 中，该 Node 持有此形状的元素。
     *
     * @param helper 描述管道阶段的管道辅助器
     * @param spliterator 源拆分器
     * @param flattenTree 如果返回的节点应被展平，则为 true
     * @param generator 数组生成器
     * @return 持有管道输出的 Node
     */
    abstract <P_IN> Node<E_OUT> evaluateToNode(PipelineHelper<E_OUT> helper,
                                               Spliterator<P_IN> spliterator,
                                               boolean flattenTree,
                                               IntFunction<E_OUT[]> generator);

    /**
     * 创建一个拆分器，该拆分器包装一个源拆分器，与此流形状兼容，
     * 并与 {@link PipelineHelper} 关联的操作兼容。
     *
     * @param ph 描述管道阶段的管道辅助器
     * @param supplier 拆分器的供应商
     * @return 与此形状兼容的包装拆分器
     */
    abstract <P_IN> Spliterator<E_OUT> wrap(PipelineHelper<E_OUT> ph,
                                            Supplier<Spliterator<P_IN>> supplier,
                                            boolean isParallel);

    /**
     * 创建一个懒惰拆分器，该拆分器在懒惰拆分器上调用方法时，
     * 包装并获取提供的拆分器。
     * @param supplier 拆分器的供应商
     */
    abstract Spliterator<E_OUT> lazySpliterator(Supplier<? extends Spliterator<E_OUT>> supplier);

    /**
     * 遍历与此流形状兼容的拆分器的元素，
     * 将这些元素推送到接收器。如果接收器请求取消，
     * 则不再拉取或推送更多元素。
     *
     * @param spliterator 要从中拉取元素的拆分器
     * @param sink 要将元素推送到的接收器
     */
    abstract void forEachWithCancel(Spliterator<E_OUT> spliterator, Sink<E_OUT> sink);

    /**
     * 创建一个与此流形状兼容的节点构建器。
     *
     * @param exactSizeIfKnown 如果 {@literal >=0}，则将创建一个固定容量为最多 sizeIfKnown 元素的节点构建器。
     * 如果 {@literal < 0}，则节点构建器具有不确定的容量。固定容量的节点构建器在构建器达到容量后添加元素或在构建器未达到容量前构建时会抛出异常。
     *
     * @param generator 用于创建 T[] 数组实例的数组生成器。对于支持原始节点的实现，此参数可能被忽略。
     * @return 节点构建器
     */
    @Override
    abstract Node.Builder<E_OUT> makeNodeBuilder(long exactSizeIfKnown,
                                                 IntFunction<E_OUT[]> generator);


    // 操作特定的抽象方法，由操作类实现

    /**
     * 返回此操作是否为状态操作。如果是状态操作，
     * 则必须覆盖方法
     * {@link #opEvaluateParallel(PipelineHelper, java.util.Spliterator, java.util.function.IntFunction)}。
     *
     * @return 如果此操作是状态操作，则返回 {@code true}
     */
    abstract boolean opIsStateful();

    /**
     * 接受一个 {@code Sink}，该接收器将接收此操作的结果，
     * 并返回一个 {@code Sink}，该接收器接受此操作的输入类型元素，
     * 并执行操作，将结果传递给提供的 {@code Sink}。
     *
     * @apiNote
     * 实现可以使用 {@code flags} 参数优化接收器包装。例如，
     * 如果输入已经是 {@code DISTINCT}，则 {@code Stream#distinct()} 方法的实现可以直接返回传递给它的接收器。
     *
     * @param flags 直到但不包括此操作的组合流和操作标志
     * @param sink 要将处理后的元素发送到的接收器
     * @return 一个接收元素的接收器，对每个元素执行操作，并将结果（如果有）传递给提供的 {@code Sink}。
     */
    abstract Sink<E_IN> opWrapSink(int flags, Sink<E_OUT> sink);

    /**
     * 使用指定的 {@code PipelineHelper} 并行评估操作，该 {@code PipelineHelper} 描述了上游的中间操作。
     * 仅在状态操作上调用。如果 {@link #opIsStateful()} 返回 true，则实现必须覆盖默认实现。
     *
     * @implSpec 默认实现总是抛出 {@code UnsupportedOperationException}。
     *
     * @param helper 描述管道阶段的管道辅助器
     * @param spliterator 源 {@code Spliterator}
     * @param generator 数组生成器
     * @return 描述评估结果的 {@code Node}
     */
    <P_IN> Node<E_OUT> opEvaluateParallel(PipelineHelper<E_OUT> helper,
                                          Spliterator<P_IN> spliterator,
                                          IntFunction<E_OUT[]> generator) {
        throw new UnsupportedOperationException("Parallel evaluation is not supported");
    }

    /**
     * 返回一个描述并行评估操作的 {@code Spliterator}，使用指定的 {@code PipelineHelper} 描述上游的中间操作。
     * 仅在状态操作上调用。在这里进行完整的计算结果是不必要的（尽管是可以接受的）；
     * 如果可能，最好通过懒惰评估的拆分器来描述结果。
     *
     * @implSpec 默认实现的行为类似于：
     * <pre>{@code
     *     return evaluateParallel(helper, i -> (E_OUT[]) new
     * Object[i]).spliterator();
     * }</pre>
     * 适用于无法进行更好的完全同步评估的实现。
     *
     * @param helper 管道辅助器
     * @param spliterator 源 {@code Spliterator}
     * @return 描述评估结果的 {@code Spliterator}
     */
    @SuppressWarnings("unchecked")
    <P_IN> Spliterator<E_OUT> opEvaluateParallelLazy(PipelineHelper<E_OUT> helper,
                                                     Spliterator<P_IN> spliterator) {
        return opEvaluateParallel(helper, spliterator, i -> (E_OUT[]) new Object[i]).spliterator();
    }
}
