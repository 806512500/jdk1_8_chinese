/*
 * Copyright (c) 2012, 2017, Oracle and/or its affiliates. All rights reserved.
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
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * 用于实现流操作的大多数分叉-合并任务的抽象基类。
 * 管理拆分逻辑、子任务跟踪和中间结果。
 * 每个任务都关联一个 {@link Spliterator}，描述了与此任务为根的子树相关的输入部分。
 * 任务可以是叶节点（将遍历 {@code Spliterator} 的元素）或内部节点（将 {@code Spliterator} 拆分为多个子任务）。
 *
 * @implNote
 * <p>此类基于 {@link CountedCompleter}，这是一种分叉-合并任务形式，其中每个任务都有一个未完成子任务的信号量计数，当最后一个子任务完成时，任务会隐式完成并通知。
 * 内部节点任务可能会覆盖 {@code CountedCompleter} 的 {@code onCompletion} 方法，将子任务的结果合并到当前任务的结果中。
 *
 * <p>拆分和设置子任务链接由内部节点的 {@code compute()} 方法完成。对于叶节点，在 {@code compute()} 时，保证父节点的子相关字段（包括父节点子任务的兄弟链接）将为所有子任务设置。
 *
 * <p>例如，执行 reduce 操作的任务将覆盖 {@code doLeaf()} 以使用 {@code Spliterator} 对该叶节点的块进行 reduce 操作，并覆盖 {@code onCompletion()} 以合并内部节点的子任务结果：
 *
 * <pre>{@code
 *     protected S doLeaf() {
 *         spliterator.forEach(...);
 *         return localReductionResult;
 *     }
 *
 *     public void onCompletion(CountedCompleter caller) {
 *         if (!isLeaf()) {
 *             ReduceTask<P_IN, P_OUT, T, R> child = children;
 *             R result = child.getLocalResult();
 *             child = child.nextSibling;
 *             for (; child != null; child = child.nextSibling)
 *                 result = combine(result, child.getLocalResult());
 *             setLocalResult(result);
 *         }
 *     }
 * }</pre>
 *
 * <p>序列化不支持，因为没有意图序列化由流操作管理的任务。
 *
 * @param <P_IN> 管道输入元素类型
 * @param <P_OUT> 管道输出元素类型
 * @param <R> 中间结果类型，可能与操作结果类型不同
 * @param <K> 父、子和兄弟任务类型
 * @since 1.8
 */
@SuppressWarnings("serial")
abstract class AbstractTask<P_IN, P_OUT, R,
                            K extends AbstractTask<P_IN, P_OUT, R, K>>
        extends CountedCompleter<R> {

    private static final int LEAF_TARGET = ForkJoinPool.getCommonPoolParallelism() << 2;

    /** 与所有任务计算相关的管道助手 */
    protected final PipelineHelper<P_OUT> helper;

    /**
     * 与此任务为根的子树相关的输入部分的 {@code Spliterator}
     */
    protected Spliterator<P_IN> spliterator;

    /** 目标叶大小，与所有任务计算相关 */
    protected long targetSize; // 可能延迟初始化

    /**
     * 左子任务。
     * 如果没有子任务，则为 null
     * 如果非 null，则 rightChild 也非 null
     */
    protected K leftChild;

    /**
     * 右子任务。
     * 如果没有子任务，则为 null
     * 如果非 null，则 leftChild 也非 null
     */
    protected K rightChild;

    /** 如果已完成，则此节点的结果 */
    private R localResult;

    /**
     * 根节点的构造函数。
     *
     * @param helper 描述到此操作的流管道的 {@code PipelineHelper}
     * @param spliterator 描述此管道源的 {@code Spliterator}
     */
    protected AbstractTask(PipelineHelper<P_OUT> helper,
                           Spliterator<P_IN> spliterator) {
        super(null);
        this.helper = helper;
        this.spliterator = spliterator;
        this.targetSize = 0L;
    }

    /**
     * 非根节点的构造函数。
     *
     * @param parent 此节点的父任务
     * @param spliterator 描述以该节点为根的子树的 {@code Spliterator}，通过拆分父 {@code Spliterator} 获得
     */
    protected AbstractTask(K parent,
                           Spliterator<P_IN> spliterator) {
        super(parent);
        this.spliterator = spliterator;
        this.helper = parent.helper;
        this.targetSize = parent.targetSize;
    }

    /**
     * 并行分解的叶任务的默认目标。
     * 为了允许负载均衡，我们过度分区，目前每个处理器大约有四个任务，这使得其他任务可以在叶任务不均匀或某些处理器忙碌时提供帮助。
     */
    public static int getLeafTarget() {
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread) {
            return ((ForkJoinWorkerThread) t).getPool().getParallelism() << 2;
        }
        else {
            return LEAF_TARGET;
        }
    }

    /**
     * 构造一个类型为 T 的新节点，其父节点为接收者；必须调用带有接收者和提供的 {@code Spliterator} 的 {@code AbstractTask(T, Spliterator)} 构造函数。
     *
     * @param spliterator 描述以该节点为根的子树的 {@code Spliterator}，通过拆分父 {@code Spliterator} 获得
     * @return 新构造的子节点
     */
    protected abstract K makeChild(Spliterator<P_IN> spliterator);

    /**
     * 计算与叶节点相关的结果。将由 {@code compute()} 调用，并将结果传递给 @{code setLocalResult()}
     *
     * @return 计算的叶节点结果
     */
    protected abstract R doLeaf();

    /**
     * 根据初始大小估计返回建议的目标叶大小。
     *
     * @return 建议的目标叶大小
     */
    public static long suggestTargetSize(long sizeEstimate) {
        long est = sizeEstimate / getLeafTarget();
        return est > 0L ? est : 1L;
    }

    /**
     * 返回目标大小，如果尚未初始化，则通过提供的大小估计进行初始化。
     */
    protected final long getTargetSize(long sizeEstimate) {
        long s;
        return ((s = targetSize) != 0 ? s :
                (targetSize = suggestTargetSize(sizeEstimate)));
    }

    /**
     * 返回本地结果（如果有）。子类应使用 {@link #setLocalResult(Object)} 和 {@link #getLocalResult()} 来管理结果。这返回本地结果，以便从分叉-合并框架内的调用返回正确的结果。
     *
     * @return 通过 {@link #setLocalResult} 之前存储的本地结果
     */
    @Override
    public R getRawResult() {
        return localResult;
    }

    /**
     * 什么都不做；相反，子类应使用 {@link #setLocalResult(Object)}} 来管理结果。
     *
     * @param result 必须为 null，否则将抛出异常（这是一个安全陷阱，用于检测是否使用了 {@code setRawResult()} 而不是 {@code setLocalResult()}）
     */
    @Override
    protected void setRawResult(R result) {
        if (result != null)
            throw new IllegalStateException();
    }

    /**
     * 检索通过 {@link #setLocalResult} 之前存储的结果。
     *
     * @return 通过 {@link #setLocalResult} 之前存储的本地结果
     */
    protected R getLocalResult() {
        return localResult;
    }

    /**
     * 将结果与任务关联，可以通过 {@link #getLocalResult} 检索。
     *
     * @param localResult 本地结果
     */
    protected void setLocalResult(R localResult) {
        this.localResult = localResult;
    }

    /**
     * 指示此任务是否为叶节点。（仅在调用 {@link #compute} 后对此节点有效）。如果节点不是叶节点，则子任务将非 null 且 numChildren 将为正。
     *
     * @return 如果此任务是叶节点，则返回 {@code true}
     */
    protected boolean isLeaf() {
        return leftChild == null;
    }

    /**
     * 指示此任务是否为根节点
     *
     * @return 如果此任务是根节点，则返回 {@code true}
     */
    protected boolean isRoot() {
        return getParent() == null;
    }

    /**
     * 返回此任务的父任务，如果此任务是根任务，则返回 null
     *
     * @return 此任务的父任务，如果此任务是根任务，则返回 null
     */
    @SuppressWarnings("unchecked")
    protected K getParent() {
        return (K) getCompleter();
    }

    /**
     * 决定是否进一步拆分任务或直接计算。如果直接计算，调用 {@code doLeaf} 并将结果传递给 {@code setRawResult}。否则拆分子任务，分叉一个并继续另一个。
     *
     * <p> 该方法旨在在各种用途中节省资源。当拆分时，循环继续一个子任务，以避免深度递归。为了应对可能偏向左重或右重拆分的 {@code Spliterator}，我们在循环中交替分叉和继续子任务。
     */
    @Override
    public void compute() {
        Spliterator<P_IN> rs = spliterator, ls; // 右、左拆分器
        long sizeEstimate = rs.estimateSize();
        long sizeThreshold = getTargetSize(sizeEstimate);
        boolean forkRight = false;
        @SuppressWarnings("unchecked") K task = (K) this;
        while (sizeEstimate > sizeThreshold && (ls = rs.trySplit()) != null) {
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
        task.setLocalResult(task.doLeaf());
        task.tryComplete();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote
     * 清除 {@code spliterator} 和子任务字段。重写者必须在最后调用 {@code super.onCompletion} 以清除这些字段。
     */
    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        spliterator = null;
        leftChild = rightChild = null;
    }

    /**
     * 返回此节点是否为“最左”节点——从根到此节点的路径是否仅涉及遍历最左子链接。对于叶节点，这意味着它是遍历顺序中的第一个叶节点。
     *
     * @return 如果此节点是“最左”节点，则返回 {@code true}
     */
    protected boolean isLeftmostNode() {
        @SuppressWarnings("unchecked")
        K node = (K) this;
        while (node != null) {
            K parent = node.getParent();
            if (parent != null && parent.leftChild != node)
                return false;
            node = parent;
        }
        return true;
    }
}
