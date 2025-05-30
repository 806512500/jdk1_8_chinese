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
import java.util.concurrent.atomic.AtomicReference;

/**
 * 用于实现短路流操作的 fork-join 任务的抽象类，这些操作可以在不处理流中所有元素的情况下产生结果。
 *
 * @param <P_IN> 管道输入元素的类型
 * @param <P_OUT> 管道输出元素的类型
 * @param <R> 中间结果的类型，可能与操作结果类型不同
 * @param <K> 子任务和兄弟任务的类型
 * @since 1.8
 */
@SuppressWarnings("serial")
abstract class AbstractShortCircuitTask<P_IN, P_OUT, R,
                                        K extends AbstractShortCircuitTask<P_IN, P_OUT, R, K>>
        extends AbstractTask<P_IN, P_OUT, R, K> {
    /**
     * 该计算的结果；在所有任务中共享并仅设置一次
     */
    protected final AtomicReference<R> sharedResult;

    /**
     * 表示此任务是否已被取消。在计算过程中，任务可能在各种条件下取消其他任务，例如在 find-first 操作中，找到值的任务将取消所有后续任务。
     */
    protected volatile boolean canceled;

    /**
     * 根任务的构造函数。
     *
     * @param helper 描述到此操作为止的流管道的 {@code PipelineHelper}
     * @param spliterator 描述此管道源的 {@code Spliterator}
     */
    protected AbstractShortCircuitTask(PipelineHelper<P_OUT> helper,
                                       Spliterator<P_IN> spliterator) {
        super(helper, spliterator);
        sharedResult = new AtomicReference<>(null);
    }

    /**
     * 非根节点的构造函数。
     *
     * @param parent 计算树中的父任务
     * @param spliterator 描述此任务所描述的计算树部分的 {@code Spliterator}
     */
    protected AbstractShortCircuitTask(K parent,
                                       Spliterator<P_IN> spliterator) {
        super(parent, spliterator);
        sharedResult = parent.sharedResult;
    }

    /**
     * 返回一个值，表示计算完成但没有任务找到短路结果。例如，对于 "find" 操作，这可能是 null 或一个空的 {@code Optional}。
     *
     * @return 当没有任务找到结果时返回的结果
     */
    protected abstract R getEmptyResult();

    /**
     * 覆盖 AbstractTask 版本，以包括在拆分或计算时的早期退出检查。
     */
    @Override
    public void compute() {
        Spliterator<P_IN> rs = spliterator, ls;
        long sizeEstimate = rs.estimateSize();
        long sizeThreshold = getTargetSize(sizeEstimate);
        boolean forkRight = false;
        @SuppressWarnings("unchecked") K task = (K) this;
        AtomicReference<R> sr = sharedResult;
        R result;
        while ((result = sr.get()) == null) {
            if (task.taskCanceled()) {
                result = task.getEmptyResult();
                break;
            }
            if (sizeEstimate <= sizeThreshold || (ls = rs.trySplit()) == null) {
                result = task.doLeaf();
                break;
            }
            K leftChild, rightChild, taskToFork;
            task.leftChild  = leftChild = task.makeChild(ls);
            task.rightChild = rightChild = task.makeChild(rs);
            task.setPendingCount(1);
            if (forkRight) {
                forkRight = false;
                rs = ls;
                task = leftChild;
                taskToFork = rightChild;
            }
            else {
                forkRight = true;
                task = rightChild;
                taskToFork = leftChild;
            }
            taskToFork.fork();
            sizeEstimate = rs.estimateSize();
        }
        task.setLocalResult(result);
        task.tryComplete();
    }


    /**
     * 声明已找到全局有效的结果。如果其他任务尚未找到答案，则将结果安装在 {@code sharedResult} 中。{@code compute()} 方法将在继续计算之前检查 {@code sharedResult}，因此这会导致计算提前终止。
     *
     * @param result 找到的结果
     */
    protected void shortCircuit(R result) {
        if (result != null)
            sharedResult.compareAndSet(null, result);
    }

    /**
     * 为该任务设置本地结果。如果此任务是根任务，则设置共享结果（如果尚未设置）。
     *
     * @param localResult 要为该任务设置的结果
     */
    @Override
    protected void setLocalResult(R localResult) {
        if (isRoot()) {
            if (localResult != null)
                sharedResult.compareAndSet(null, localResult);
        }
        else
            super.setLocalResult(localResult);
    }

    /**
     * 检索该任务的本地结果
     */
    @Override
    public R getRawResult() {
        return getLocalResult();
    }

    /**
     * 检索该任务的本地结果。如果此任务是根任务，则检索共享结果。
     */
    @Override
    public R getLocalResult() {
        if (isRoot()) {
            R answer = sharedResult.get();
            return (answer == null) ? getEmptyResult() : answer;
        }
        else
            return super.getLocalResult();
    }

    /**
     * 标记此任务为已取消
     */
    protected void cancel() {
        canceled = true;
    }

    /**
     * 查询此任务是否已取消。如果此任务或其任何父任务已被取消，则认为此任务已取消。
     *
     * @return 如果此任务或任何父任务已取消，则返回 {@code true}。
     */
    protected boolean taskCanceled() {
        boolean cancel = canceled;
        if (!cancel) {
            for (K parent = getParent(); !cancel && parent != null; parent = parent.getParent())
                cancel = parent.canceled;
        }

        return cancel;
    }

    /**
     * 取消此任务之后的所有任务。这包括取消当前任务的所有右侧兄弟任务，以及其所有父任务的后续右侧兄弟任务。
     */
    protected void cancelLaterNodes() {
        // 沿树向上，取消此节点及其所有父节点的右侧兄弟任务
        for (@SuppressWarnings("unchecked") K parent = getParent(), node = (K) this;
             parent != null;
             node = parent, parent = parent.getParent()) {
            // 如果节点是父节点的左侧子节点，则有右侧兄弟节点
            if (parent.leftChild == node) {
                K rightSibling = parent.rightChild;
                if (!rightSibling.canceled)
                    rightSibling.cancel();
            }
        }
    }
}
