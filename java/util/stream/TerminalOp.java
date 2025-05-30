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

import java.util.Spliterator;

/**
 * 流管道中的一个操作，该操作以流作为输入并生成结果或副作用。{@code TerminalOp} 具有输入类型和流形状，以及结果类型。{@code TerminalOp} 还有一组
 * <em>操作标志</em>，描述了操作如何处理流中的元素（例如短路或尊重遇到顺序；参见 {@link StreamOpFlag}）。
 *
 * <p>{@code TerminalOp} 必须提供相对于给定流源和一组中间操作的顺序和并行操作实现。
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
     * 使用指定的 {@code PipelineHelper} 执行操作的并行评估，该 {@code PipelineHelper} 描述了上游的中间操作。
     *
     * @implSpec 默认使用指定的 {@code PipelineHelper} 执行操作的顺序评估。
     *
     * @param helper 管道助手
     * @param spliterator 源拆分器
     * @return 评估的结果
     */
    default <P_IN> R evaluateParallel(PipelineHelper<E_IN> helper,
                                      Spliterator<P_IN> spliterator) {
        if (Tripwire.ENABLED)
            Tripwire.trip(getClass(), "{0} 触发 TerminalOp.evaluateParallel 顺序默认");
        return evaluateSequential(helper, spliterator);
    }

    /**
     * 使用指定的 {@code PipelineHelper} 执行操作的顺序评估，该 {@code PipelineHelper} 描述了上游的中间操作。
     *
     * @param helper 管道助手
     * @param spliterator 源拆分器
     * @return 评估的结果
     */
    <P_IN> R evaluateSequential(PipelineHelper<E_IN> helper,
                                Spliterator<P_IN> spliterator);
}
