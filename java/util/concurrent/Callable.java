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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并按照
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的那样发布到公共领域。
 */

package java.util.concurrent;

/**
 * 一个返回结果并可能抛出异常的任务。
 * 实现者定义一个无参数的方法，称为 {@code call}。
 *
 * <p>{@code Callable} 接口类似于 {@link java.lang.Runnable}，因为两者都是为那些实例可能由另一个线程执行的类设计的。
 * 但是，{@code Runnable} 不返回结果，也不能抛出检查异常。
 *
 * <p>{@link Executors} 类包含将其他常见形式转换为 {@code Callable} 类的实用方法。
 *
 * @see Executor
 * @since 1.5
 * @author Doug Lea
 * @param <V> 方法 {@code call} 的结果类型
 */
@FunctionalInterface
public interface Callable<V> {
    /**
     * 计算结果，如果无法计算结果则抛出异常。
     *
     * @return 计算结果
     * @throws Exception 如果无法计算结果
     */
    V call() throws Exception;
}