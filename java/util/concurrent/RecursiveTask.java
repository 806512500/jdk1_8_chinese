/*
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

/*
 *
 *
 *
 *
 *
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据以下网址的解释发布到公共领域：
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

/**
 * 一个递归的结果承载型 {@link ForkJoinTask}。
 *
 * <p>以一个经典的例子来说，这是一个计算斐波那契数的任务：
 *
 *  <pre> {@code
 * class Fibonacci extends RecursiveTask<Integer> {
 *   final int n;
 *   Fibonacci(int n) { this.n = n; }
 *   Integer compute() {
 *     if (n <= 1)
 *       return n;
 *     Fibonacci f1 = new Fibonacci(n - 1);
 *     f1.fork();
 *     Fibonacci f2 = new Fibonacci(n - 2);
 *     return f2.compute() + f1.join();
 *   }
 * }}</pre>
 *
 * 然而，除了计算斐波那契函数的方式愚蠢（实际上有一个简单的快速线性算法可以使用）之外，这个方法的性能可能也很差，因为最小的子任务太小，不值得分解。相反，对于几乎所有的分叉/加入应用，你会选择一个最小的粒度大小（例如这里的10），对于这个大小的任务，你总是顺序解决而不是细分。
 *
 * @since 1.7
 * @author Doug Lea
 */
public abstract class RecursiveTask<V> extends ForkJoinTask<V> {
    private static final long serialVersionUID = 5232453952276485270L;

    /**
     * 计算的结果。
     */
    V result;

    /**
     * 由该任务执行的主要计算。
     * @return 计算的结果
     */
    protected abstract V compute();

    public final V getRawResult() {
        return result;
    }

    protected final void setRawResult(V value) {
        result = value;
    }

    /**
     * 为 RecursiveTask 实现执行约定。
     */
    protected final boolean exec() {
        result = compute();
        return true;
    }

}