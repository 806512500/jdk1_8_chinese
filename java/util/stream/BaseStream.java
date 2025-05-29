/*
 * 版权所有 (c) 2012, 2013, Oracle 和/或其附属公司。保留所有权利。
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
package java.util.stream;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

/**
 * 流的基础接口，流是支持顺序和并行聚合操作的元素序列。以下示例说明了使用流类型 {@link Stream}
 * 和 {@link IntStream} 进行的聚合操作，计算红色小部件的重量总和：
 *
 * <pre>{@code
 *     int sum = widgets.stream()
 *                      .filter(w -> w.getColor() == RED)
 *                      .mapToInt(w -> w.getWeight())
 *                      .sum();
 * }</pre>
 *
 * 有关流、流操作、流管道和并行性的更多信息，请参阅 {@link Stream} 类文档和
 * <a href="package-summary.html">java.util.stream</a> 包文档，这些文档规定了所有流类型的行为。
 *
 * @param <T> 流元素的类型
 * @param <S> 实现 {@code BaseStream} 的流类型
 * @since 1.8
 * @see Stream
 * @see IntStream
 * @see LongStream
 * @see DoubleStream
 * @see <a href="package-summary.html">java.util.stream</a>
 */
public interface BaseStream<T, S extends BaseStream<T, S>>
        extends AutoCloseable {
    /**
     * 返回此流的元素的迭代器。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * @return 此流的元素迭代器
     */
    Iterator<T> iterator();

    /**
     * 返回此流的元素的拆分器。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">终端
     * 操作</a>。
     *
     * @return 此流的元素拆分器
     */
    Spliterator<T> spliterator();

    /**
     * 返回如果执行终端操作，此流是否将以并行方式执行。在调用终端流操作方法后调用此方法可能会产生不可预测的结果。
     *
     * @return 如果此流在执行时将以并行方式执行，则返回 {@code true}
     */
    boolean isParallel();

    /**
     * 返回一个等效的顺序流。如果流已经是顺序的，或者底层流状态被修改为顺序的，则可能返回自身。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间
     * 操作</a>。
     *
     * @return 顺序流
     */
    S sequential();

    /**
     * 返回一个等效的并行流。如果流已经是并行的，或者底层流状态被修改为并行的，则可能返回自身。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间
     * 操作</a>。
     *
     * @return 并行流
     */
    S parallel();

    /**
     * 返回一个等效的无序流。如果流已经是无序的，或者底层流状态被修改为无序的，则可能返回自身。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间
     * 操作</a>。
     *
     * @return 无序流
     */
    S unordered();

    /**
     * 返回一个具有额外关闭处理程序的等效流。当调用流的 {@link #close()} 方法时，关闭处理程序将运行，并按添加的顺序执行。
     * 即使早期的关闭处理程序抛出异常，所有关闭处理程序也会运行。如果任何关闭处理程序抛出异常，第一个抛出的异常将传递给 {@code close()} 的调用者，
     * 剩余的异常将作为抑制异常添加到该异常中（除非剩余的异常与第一个异常相同，因为异常不能抑制自身）。可能返回自身。
     *
     * <p>这是一个 <a href="package-summary.html#StreamOps">中间
     * 操作</a>。
     *
     * @param closeHandler 当流关闭时执行的任务
     * @return 一个流，如果流关闭，将运行处理程序
     */
    S onClose(Runnable closeHandler);

    /**
     * 关闭此流，导致此流管道的所有关闭处理程序被调用。
     *
     * @see AutoCloseable#close()
     */
    @Override
    void close();
}
