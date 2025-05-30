
/*
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

/**
 * 此抽象类是所有表示字节输入流的类的超类。
 *
 * <p> 需要定义 <code>InputStream</code> 子类的应用程序必须始终提供一个返回下一个输入字节的方法。
 *
 * @author  Arthur van Hoff
 * @see     java.io.BufferedInputStream
 * @see     java.io.ByteArrayInputStream
 * @see     java.io.DataInputStream
 * @see     java.io.FilterInputStream
 * @see     java.io.InputStream#read()
 * @see     java.io.OutputStream
 * @see     java.io.PushbackInputStream
 * @since   JDK1.0
 */
public abstract class InputStream implements Closeable {

    // MAX_SKIP_BUFFER_SIZE 用于确定跳过时使用的最大缓冲区大小。
    private static final int MAX_SKIP_BUFFER_SIZE = 2048;

    /**
     * 从输入流中读取下一个字节的数据。返回的字节值是一个范围在 <code>0</code> 到 <code>255</code> 之间的 <code>int</code>。
     * 如果因为到达流的末尾而没有可用的字节，则返回值为 <code>-1</code>。此方法会阻塞，直到有输入数据可用，检测到流的末尾，或抛出异常。
     *
     * <p> 子类必须提供此方法的实现。
     *
     * @return     下一个字节的数据，或如果到达流的末尾则返回 <code>-1</code>。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public abstract int read() throws IOException;

    /**
     * 从输入流中读取一些字节并存储到缓冲数组 <code>b</code> 中。实际读取的字节数作为整数返回。此方法会阻塞，直到有输入数据可用，检测到文件末尾，或抛出异常。
     *
     * <p> 如果 <code>b</code> 的长度为零，则不读取任何字节并返回 <code>0</code>；否则，尝试至少读取一个字节。如果因为流到达文件末尾而没有可用的字节，则返回值为 <code>-1</code>；否则，至少读取一个字节并存储到 <code>b</code> 中。
     *
     * <p> 第一个读取的字节存储在元素 <code>b[0]</code> 中，下一个字节存储在 <code>b[1]</code> 中，依此类推。读取的字节数最多等于 <code>b</code> 的长度。设 <i>k</i> 为实际读取的字节数；这些字节将存储在元素 <code>b[0]</code> 到 <code>b[</code><i>k</i><code>-1]</code> 中，元素 <code>b[</code><i>k</i><code>]</code> 到 <code>b[b.length-1]</code> 保持不变。
     *
     * <p> <code>InputStream</code> 类的 <code>read(b)</code> 方法与以下效果相同： <pre><code> read(b, 0, b.length) </code></pre>
     *
     * @param      b   存储数据的缓冲区。
     * @return     读取到缓冲区中的总字节数，或如果因为到达流的末尾而没有更多数据则返回 <code>-1</code>。
     * @exception  IOException  如果因任何原因（文件末尾除外）无法读取第一个字节，或者输入流已关闭，或者发生其他 I/O 错误。
     * @exception  NullPointerException  如果 <code>b</code> 为 <code>null</code>。
     * @see        java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * 从输入流中读取最多 <code>len</code> 个字节的数据并存储到字节数组中。尝试读取尽可能多的 <code>len</code> 个字节，但实际读取的字节数可能更少。实际读取的字节数作为整数返回。
     *
     * <p> 此方法会阻塞，直到有输入数据可用，检测到文件末尾，或抛出异常。
     *
     * <p> 如果 <code>len</code> 为零，则不读取任何字节并返回 <code>0</code>；否则，尝试至少读取一个字节。如果因为流到达文件末尾而没有可用的字节，则返回值为 <code>-1</code>；否则，至少读取一个字节并存储到 <code>b</code> 中。
     *
     * <p> 第一个读取的字节存储在元素 <code>b[off]</code> 中，下一个字节存储在 <code>b[off+1]</code> 中，依此类推。读取的字节数最多等于 <code>len</code>。设 <i>k</i> 为实际读取的字节数；这些字节将存储在元素 <code>b[off]</code> 到 <code>b[off+</code><i>k</i><code>-1]</code> 中，元素 <code>b[off+</code><i>k</i><code>]</code> 到 <code>b[off+len-1]</code> 保持不变。
     *
     * <p> 在所有情况下，元素 <code>b[0]</code> 到 <code>b[off]</code> 和元素 <code>b[off+len]</code> 到 <code>b[b.length-1]</code> 保持不变。
     *
     * <p> <code>InputStream</code> 类的 <code>read(b,</code> <code>off,</code> <code>len)</code> 方法只是反复调用 <code>read()</code> 方法。如果第一次调用 <code>read()</code> 导致 <code>IOException</code>，则该异常将从 <code>read(b,</code> <code>off,</code> <code>len)</code> 方法返回。如果后续调用 <code>read()</code> 导致 <code>IOException</code>，则捕获该异常并将其视为文件末尾；读取的字节存储到 <code>b</code> 中，并返回在异常发生前读取的字节数。此方法的默认实现会阻塞，直到读取了请求的输入数据量 <code>len</code>，检测到文件末尾，或抛出异常。鼓励子类提供此方法的更高效实现。
     *
     * @param      b     存储数据的缓冲区。
     * @param      off   数组 <code>b</code> 中数据写入的起始偏移量。
     * @param      len   最大读取字节数。
     * @return     读取到缓冲区中的总字节数，或如果因为到达流的末尾而没有更多数据则返回 <code>-1</code>。
     * @exception  IOException 如果因任何原因（文件末尾除外）无法读取第一个字节，或者输入流已关闭，或者发生其他 I/O 错误。
     * @exception  NullPointerException 如果 <code>b</code> 为 <code>null</code>。
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负数，<code>len</code> 为负数，或 <code>len</code> 大于 <code>b.length - off</code>
     * @see        java.io.InputStream#read()
     */
    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte)c;

        int i = 1;
        try {
            for (; i < len ; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                b[off + i] = (byte)c;
            }
        } catch (IOException ee) {
        }
        return i;
    }

    /**
     * 跳过并丢弃此输入流中的 <code>n</code> 个字节的数据。<code>skip</code> 方法可能因各种原因最终跳过较少的字节，可能是 <code>0</code>。这可能是由于到达文件末尾之前没有跳过 <code>n</code> 个字节。实际跳过的字节数返回。如果 {@code n} 为负数，则 <code>InputStream</code> 类的 <code>skip</code> 方法始终返回 0，且不跳过任何字节。子类可以不同地处理负值。
     *
     * <p> 此类的 <code>skip</code> 方法创建一个字节数组，然后反复读取到该数组中，直到读取了 <code>n</code> 个字节或到达流的末尾。鼓励子类提供此方法的更高效实现。例如，实现可能依赖于 seek 能力。
     *
     * @param      n   要跳过的字节数。
     * @return     实际跳过的字节数。
     * @exception  IOException  如果流不支持 seek，或发生其他 I/O 错误。
     */
    public long skip(long n) throws IOException {

        long remaining = n;
        int nr;

        if (n <= 0) {
            return 0;
        }

        int size = (int)Math.min(MAX_SKIP_BUFFER_SIZE, remaining);
        byte[] skipBuffer = new byte[size];
        while (remaining > 0) {
            nr = read(skipBuffer, 0, (int)Math.min(size, remaining));
            if (nr < 0) {
                break;
            }
            remaining -= nr;
        }

        return n - remaining;
    }

    /**
     * 返回一个估计值，表示在下一次调用此输入流的方法时，可以从该输入流中读取（或跳过）的字节数，而不会阻塞。下一次调用可能是同一个线程或另一个线程。单次读取或跳过这些字节不会阻塞，但可能会读取或跳过更少的字节。
     *
     * <p> 注意，虽然某些 <code>InputStream</code> 实现会返回流中的总字节数，但许多实现不会。使用此方法的返回值来分配一个旨在存储此流中所有数据的缓冲区是不正确的。
     *
     * <p> 如果通过调用 <code>close()</code> 方法关闭了此输入流，子类的此方法实现可以选择抛出 <code>IOException</code>。
     *
     * <p> <code>InputStream</code> 类的 <code>available</code> 方法始终返回 <code>0</code>。
     *
     * <p> 应该由子类覆盖此方法。
     *
     * @return     一个估计值，表示在不阻塞的情况下可以从该输入流中读取（或跳过）的字节数，或在到达输入流末尾时返回 <code>0</code>。
     * @exception  IOException 如果发生 I/O 错误。
     */
    public int available() throws IOException {
        return 0;
    }

    /**
     * 关闭此输入流并释放与此流关联的任何系统资源。
     *
     * <p> <code>InputStream</code> 类的 <code>close</code> 方法不执行任何操作。
     *
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void close() throws IOException {}

    /**
     * 标记此输入流中的当前位置。后续调用 <code>reset</code> 方法将使此流重新定位到上次调用 <code>mark</code> 时的位置，以便后续读取会重新读取相同的字节。
     *
     * <p> <code>readlimit</code> 参数告诉此输入流在调用 <code>reset</code> 之前允许读取多少字节，标记位置才失效。
     *
     * <p> <code>mark</code> 的一般约定是，如果 <code>markSupported</code> 方法返回 <code>true</code>，则流会以某种方式记住从调用 <code>mark</code> 之后读取的所有字节，并准备好在调用 <code>reset</code> 时重新提供这些字节。但是，如果从调用 <code>mark</code> 之后读取的字节数超过 <code>readlimit</code>，则不要求流记住任何数据。
     *
     * <p> 标记已关闭的流不应对此流产生任何影响。
     *
     * <p> <code>InputStream</code> 类的 <code>mark</code> 方法不执行任何操作。
     *
     * @param   readlimit   在标记位置失效之前可以读取的最大字节数。
     * @see     java.io.InputStream#reset()
     */
    public synchronized void mark(int readlimit) {}

    /**
     * 将此流重新定位到上次调用 <code>mark</code> 方法时的位置。
     *
     * <p> <code>reset</code> 的一般约定是：
     *
     * <ul>
     * <li> 如果 <code>markSupported</code> 方法返回 <code>true</code>，则：
     *
     *     <ul><li> 如果自流创建以来未调用 <code>mark</code> 方法，或自上次调用 <code>mark</code> 以来从流中读取的字节数大于上次调用 <code>mark</code> 时的参数值，则可能会抛出 <code>IOException</code>。
     *
     *     <li> 如果没有抛出 <code>IOException</code>，则流将重置为一种状态，即所有自上次调用 <code>mark</code>（或自文件开始，如果未调用 <code>mark</code>）以来读取的字节将重新提供给后续调用 <code>read</code> 方法，然后是调用 <code>reset</code> 时的下一个输入数据。 </ul>
     *
     * <li> 如果 <code>markSupported</code> 方法返回 <code>false</code>，则：
     *
     *     <ul><li> 调用 <code>reset</code> 可能会抛出 <code>IOException</code>。
     *
     *     <li> 如果没有抛出 <code>IOException</code>，则流将重置为一种固定状态，该状态取决于特定类型的输入流及其创建方式。后续调用 <code>read</code> 方法将提供的字节取决于特定类型的输入流。 </ul></ul>
     *
     * <p> <code>InputStream</code> 类的 <code>reset</code> 方法除了抛出 <code>IOException</code> 外不执行任何操作。
     *
     * @exception  IOException  如果此流未被标记或标记已失效。
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.IOException
     */
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }


                /**
     * 测试此输入流是否支持 <code>mark</code> 和 <code>reset</code> 方法。是否支持 <code>mark</code> 和
     * <code>reset</code> 是特定输入流实例的不变属性。 <code>InputStream</code> 的 <code>markSupported</code> 方法
     * 返回 <code>false</code>。
     *
     * @return  <code>true</code> 如果此流实例支持 mark 和 reset 方法；否则返回 <code>false</code>。
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
        return false;
    }

}
