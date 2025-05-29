
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
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;

/**
 * 用于实现流操作的大多数分叉-合并任务的抽象基类。
 * 管理拆分逻辑、子任务的跟踪和中间结果。
 * 每个任务都与一个 {@link Spliterator} 相关联，该 {@code Spliterator} 描述了与此任务为根的子树相关联的输入部分。
 * 任务可以是叶节点（将遍历 {@code Spliterator} 的元素）或内部节点（将 {@code Spliterator} 拆分为多个子任务）。
 *
 * @implNote
 * <p>此类基于 {@link CountedCompleter}，这是一种分叉-合并任务的形式，其中每个任务都有一个未完成子任务的信号量计数，
 * 当最后一个子任务完成时，任务会隐式完成并通知。
 * 内部节点任务可能会重写 {@code CountedCompleter} 的 {@code onCompletion} 方法，以将子任务的结果合并到当前任务的结果中。
 *
 * <p>拆分和设置子任务链接由内部节点的 {@code compute()} 方法完成。对于叶节点，在 {@code compute()} 时，
 * 可以保证所有子任务的父任务的子相关字段（包括父任务子任务的兄弟链接）将设置好。
 *
 * <p>例如，执行 reduce 操作的任务会重写 {@code doLeaf()} 方法，使用 {@code Spliterator} 对该叶节点的块执行 reduce，
 * 并重写 {@code onCompletion()} 方法，将内部节点的子任务结果合并：
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
 * <p>不支持序列化，因为没有意图序列化由流操作管理的任务。
 *
 * @param <P_IN> 管道输入元素的类型
 * @param <P_OUT> 管道输出元素的类型
 * @param <R> 中间结果的类型，可能与操作结果类型不同
 * @param <K> 父任务、子任务和兄弟任务的类型
 * @since 1.8
 */
@SuppressWarnings("serial")
abstract class AbstractTask<P_IN, P_OUT, R,
                            K extends AbstractTask<P_IN, P_OUT, R, K>>
        extends CountedCompleter<R> {

    /**
     * 并行分解的叶任务的默认目标因子。
     * 为了允许负载平衡，我们过度分区，目前大约每个处理器四个任务，这使得其他任务可以在叶任务不均匀或某些处理器忙碌时帮助。
     */
    static final int LEAF_TARGET = ForkJoinPool.getCommonPoolParallelism() << 2;

    /** 所有任务在计算中共享的管道助手 */
    protected final PipelineHelper<P_OUT> helper;

    /**
     * 与此任务为根的子树相关联的输入部分的 {@code Spliterator}
     */
    protected Spliterator<P_IN> spliterator;

    /** 计算中所有任务共享的目标叶大小 */
    protected long targetSize; // 可能延迟初始化

    /**
     * 左子任务。
     * 如果没有子任务则为 null
     * 如果非 null，则 rightChild 也非 null
     */
    protected K leftChild;

    /**
     * 右子任务。
     * 如果没有子任务则为 null
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
     * 构造一个类型为 T 的新节点，其父节点为接收者；必须调用带有接收者和提供的 {@code Spliterator} 的 AbstractTask(T, Spliterator) 构造函数。
     *
     * @param spliterator 描述以该节点为根的子树的 {@code Spliterator}，通过拆分父 {@code Spliterator} 获得
     * @return 新构造的子节点
     */
    protected abstract K makeChild(Spliterator<P_IN> spliterator);

    /**
     * 计算与叶节点相关联的结果。将由 {@code compute()} 调用，并将结果传递给 @{code setLocalResult()}
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
        long est = sizeEstimate / LEAF_TARGET;
        return est > 0L ? est : 1L;
    }


    /**
     * 返回 targetSize，如果尚未初始化，则通过提供的大小估计值进行初始化。
     */
    protected final long getTargetSize(long sizeEstimate) {
        long s;
        return ((s = targetSize) != 0 ? s :
                (targetSize = suggestTargetSize(sizeEstimate)));
    }

    /**
     * 如果存在，返回本地结果。子类应使用
     * {@link #setLocalResult(Object)} 和 {@link #getLocalResult()} 来管理
     * 结果。此方法返回本地结果，以便从 fork-join 框架内的调用将返回正确的结果。
     *
     * @return 之前使用 {@link #setLocalResult} 存储的此节点的本地结果
     */
    @Override
    public R getRawResult() {
        return localResult;
    }

    /**
     * 不执行任何操作；相反，子类应使用
     * {@link #setLocalResult(Object)}} 来管理结果。
     *
     * @param result 必须为 null，否则将抛出异常（这是一个安全陷阱，用于检测何时使用
     *        {@code setRawResult()} 而不是 {@code setLocalResult()}
     */
    @Override
    protected void setRawResult(R result) {
        if (result != null)
            throw new IllegalStateException();
    }

    /**
     * 检索之前使用 {@link #setLocalResult} 存储的结果
     *
     * @return 之前使用 {@link #setLocalResult} 存储的此节点的本地结果
     */
    protected R getLocalResult() {
        return localResult;
    }

    /**
     * 将结果与任务关联，可以通过 {@link #getLocalResult} 检索
     *
     * @param localResult 此节点的本地结果
     */
    protected void setLocalResult(R localResult) {
        this.localResult = localResult;
    }

    /**
     * 指示此任务是否为叶节点。 （仅在调用此节点的
     * {@link #compute} 之后有效）。如果节点不是叶节点，则子节点将不为 null 且 numChildren 将为正数。
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
     * 决定是否进一步拆分任务或直接计算。如果直接计算，调用 {@code doLeaf} 并将结果传递给 {@code setRawResult}。
     * 否则拆分子任务，分叉一个并继续另一个。
     *
     * <p> 该方法旨在跨各种用途节省资源。循环继续使用拆分时的一个子任务，以避免深度递归。为了应对可能偏向于左重或
     * 右重拆分的 spliterators，我们在循环中交替分叉与继续的子任务。
     */
    @Override
    public void compute() {
        Spliterator<P_IN> rs = spliterator, ls; // 右、左 spliterators
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
     * 清除 spliterator 和 children 字段。覆写者必须在最后调用
     * {@code super.onCompletion} 以清除这些字段。
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
