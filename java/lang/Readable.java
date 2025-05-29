/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang;

import java.io.IOException;

/**
 * 一个 <tt>Readable</tt> 是字符的来源。来自 <tt>Readable</tt> 的字符通过
 * {@link java.nio.CharBuffer CharBuffer} 提供给调用者。
 *
 * @since 1.5
 */
public interface Readable {

    /**
     * 尝试将字符读入指定的字符缓冲区。
     * 缓冲区作为字符的存储库使用：唯一的变化是 put 操作的结果。不会对缓冲区进行翻转或重绕。
     *
     * @param cb 用于读入字符的缓冲区
     * @return 添加到缓冲区的 {@code char} 值的数量，
     *                 或者如果此字符来源已结束，则返回 -1
     * @throws IOException 如果发生 I/O 错误
     * @throws NullPointerException 如果 cb 为 null
     * @throws java.nio.ReadOnlyBufferException 如果 cb 是只读缓冲区
     */
    public int read(java.nio.CharBuffer cb) throws IOException;
}
