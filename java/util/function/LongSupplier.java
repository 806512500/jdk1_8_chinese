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
package java.util.function;

/**
 * 表示提供 {@code long} 类型结果的供应商。这是 {@link Supplier} 的 {@code long} 类型生成的原始特化。
 *
 * <p>不要求每次调用供应商时都返回不同的结果。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法为 {@link #getAsLong()}。
 *
 * @see Supplier
 * @since 1.8
 */
@FunctionalInterface
public interface LongSupplier {

    /**
     * 获取一个结果。
     *
     * @return 一个结果
     */
    long getAsLong();
}