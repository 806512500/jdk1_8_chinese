/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其关联公司。保留所有权利。
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

package java.io;

import java.io.IOException;

/**
 * 一个 {@code Closeable} 是一个可以关闭的数据源或目标。关闭方法被调用以释放对象持有的资源
 * （如打开的文件）。
 *
 * @since 1.5
 */
public interface Closeable extends AutoCloseable {

    /**
     * 关闭此流并释放与之关联的任何系统资源。如果流已经关闭，则调用此
     * 方法没有效果。
     *
     * <p> 如 {@link AutoCloseable#close()} 中所述，关闭可能失败的情况需要特别注意。强烈建议
     * 在抛出 {@code IOException} 之前，释放底层资源并内部 <em>标记</em> {@code Closeable} 为已关闭。
     *
     * @throws IOException 如果发生 I/O 错误
     */
    public void close() throws IOException;
}
