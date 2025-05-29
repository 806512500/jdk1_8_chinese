/*
 * 版权所有 (c) 2013, Oracle 和/或其关联公司。保留所有权利。
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
package java.util.function;

/**
 * 表示一个接受一个对象值和一个 {@code long} 值参数且不返回结果的操作。这是 {@link BiConsumer} 的 {@code (reference, long)} 专业化。
 * 与其他大多数函数接口不同，{@code ObjLongConsumer} 预期通过副作用进行操作。
 *
 * <p>这是一个 <a href="package-summary.html">函数接口</a>，其函数方法是 {@link #accept(Object, long)}。
 *
 * @param <T> 操作的参数对象的类型
 *
 * @see BiConsumer
 * @since 1.8
 */
@FunctionalInterface
public interface ObjLongConsumer<T> {

    /**
     * 在给定的参数上执行此操作。
     *
     * @param t 第一个输入参数
     * @param value 第二个输入参数
     */
    void accept(T t, long value);
}