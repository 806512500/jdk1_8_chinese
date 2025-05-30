/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

/**
 * 一个可以读取和写入字节的异步通道。
 *
 * <p> 一些通道可能不允许在任何给定时间有多个读取或写入操作未完成。如果一个线程在前一个读取操作完成之前调用读取方法，则会抛出 {@link ReadPendingException}。
 * 同样，如果在前一个写入操作完成之前调用写入方法，则会抛出 {@link WritePendingException}。其他类型的 I/O 操作是否可以与读取操作同时进行取决于通道的类型。
 *
 * <p> 注意 {@link java.nio.ByteBuffer ByteBuffer} 不适合多个并发线程使用。当启动读取或写入操作时，必须确保在操作完成之前不访问缓冲区。
 *
 * @see Channels#newInputStream(AsynchronousByteChannel)
 * @see Channels#newOutputStream(AsynchronousByteChannel)
 *
 * @since 1.7
 */

public interface AsynchronousByteChannel
    extends AsynchronousChannel
{
    /**
     * 从该通道读取一系列字节到给定的缓冲区。
     *
     * <p> 此方法启动一个异步读取操作，从该通道读取一系列字节到给定的缓冲区。{@code handler} 参数是一个完成处理器，当读取操作完成（或失败）时被调用。
     * 传递给完成处理器的结果是读取的字节数，或者如果通道已到达流末尾而无法读取字节，则为 {@code -1}。
     *
     * <p> 读取操作可能从通道读取多达 <i>r</i> 个字节，其中 <i>r</i> 是缓冲区中剩余的字节数，即在尝试读取时 {@code dst.remaining()} 的值。
     * 如果 <i>r</i> 为 0，则读取操作立即完成，结果为 {@code 0}，而不会启动 I/O 操作。
     *
     * <p> 假设读取了一个长度为 <i>n</i> 的字节序列，其中 <tt>0</tt>&nbsp;<tt>&lt;</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>。
     * 这个字节序列将被传输到缓冲区，使得序列中的第一个字节位于索引 <i>p</i>，最后一个字节位于索引 <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>，
     * 其中 <i>p</i> 是执行读取时缓冲区的位置。完成时，缓冲区的位置将等于 <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>；其限制不会改变。
     *
     * <p> 缓冲区不适合多个并发线程使用，因此必须确保在操作完成之前不访问缓冲区。
     *
     * <p> 此方法可以在任何时候调用。某些通道类型可能不允许在任何给定时间有多个读取操作未完成。如果一个线程在前一个读取操作完成之前启动读取操作，则会抛出 {@link ReadPendingException}。
     *
     * @param   <A>
     *          附件的类型
     * @param   dst
     *          要传输字节的缓冲区
     * @param   attachment
     *          要附加到 I/O 操作的对象；可以为 {@code null}
     * @param   handler
     *          完成处理器
     *
     * @throws  IllegalArgumentException
     *          如果缓冲区是只读的
     * @throws  ReadPendingException
     *          如果通道不允许有多个未完成的读取操作，且前一个读取操作尚未完成
     * @throws  ShutdownChannelGroupException
     *          如果通道与一个已终止的 {@link AsynchronousChannelGroup 组} 关联
     */
    <A> void read(ByteBuffer dst,
                  A attachment,
                  CompletionHandler<Integer,? super A> handler);

    /**
     * 从该通道读取一系列字节到给定的缓冲区。
     *
     * <p> 此方法启动一个异步读取操作，从该通道读取一系列字节到给定的缓冲区。此方法的行为与 {@link
     * #read(ByteBuffer,Object,CompletionHandler)
     * read(ByteBuffer,Object,CompletionHandler)} 方法完全相同，只是不指定完成处理器，而是返回一个表示未完成结果的 {@code Future}。
     * {@code Future} 的 {@link Future#get() get} 方法返回读取的字节数，或者如果通道已到达流末尾而无法读取字节，则返回 {@code -1}。
     *
     * @param   dst
     *          要传输字节的缓冲区
     *
     * @return  一个表示操作结果的 Future
     *
     * @throws  IllegalArgumentException
     *          如果缓冲区是只读的
     * @throws  ReadPendingException
     *          如果通道不允许有多个未完成的读取操作，且前一个读取操作尚未完成
     */
    Future<Integer> read(ByteBuffer dst);

    /**
     * 从给定的缓冲区写入一系列字节到该通道。
     *
     * <p> 此方法启动一个异步写入操作，从给定的缓冲区写入一系列字节到该通道。{@code handler} 参数是一个完成处理器，当写入操作完成（或失败）时被调用。
     * 传递给完成处理器的结果是写入的字节数。
     *
     * <p> 写入操作可能向通道写入多达 <i>r</i> 个字节，其中 <i>r</i> 是缓冲区中剩余的字节数，即在尝试写入时 {@code src.remaining()} 的值。
     * 如果 <i>r</i> 为 0，则写入操作立即完成，结果为 {@code 0}，而不会启动 I/O 操作。
     *
     * <p> 假设写入了一个长度为 <i>n</i> 的字节序列，其中 <tt>0</tt>&nbsp;<tt>&lt;</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>。
     * 这个字节序列将从缓冲区开始位置 <i>p</i> 转移，其中 <i>p</i> 是执行写入时缓冲区的位置；最后一个字节的索引为 <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>。
     * 完成时，缓冲区的位置将等于 <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>；其限制不会改变。
     *
     * <p> 缓冲区不适合多个并发线程使用，因此必须确保在操作完成之前不访问缓冲区。
     *
     * <p> 此方法可以在任何时候调用。某些通道类型可能不允许在任何给定时间有多个写入操作未完成。如果一个线程在前一个写入操作完成之前启动写入操作，则会抛出 {@link WritePendingException}。
     *
     * @param   <A>
     *          附件的类型
     * @param   src
     *          要从中获取字节的缓冲区
     * @param   attachment
     *          要附加到 I/O 操作的对象；可以为 {@code null}
     * @param   handler
     *          完成处理器对象
     *
     * @throws  WritePendingException
     *          如果通道不允许有多个未完成的写入操作，且前一个写入操作尚未完成
     * @throws  ShutdownChannelGroupException
     *          如果通道与一个已终止的 {@link AsynchronousChannelGroup 组} 关联
     */
    <A> void write(ByteBuffer src,
                   A attachment,
                   CompletionHandler<Integer,? super A> handler);

    /**
     * 从给定的缓冲区写入一系列字节到该通道。
     *
     * <p> 此方法启动一个异步写入操作，从给定的缓冲区写入一系列字节到该通道。此方法的行为与 {@link
     * #write(ByteBuffer,Object,CompletionHandler)
     * write(ByteBuffer,Object,CompletionHandler)} 方法完全相同，只是不指定完成处理器，而是返回一个表示未完成结果的 {@code Future}。
     * {@code Future} 的 {@link Future#get() get} 方法返回写入的字节数。
     *
     * @param   src
     *          要从中获取字节的缓冲区
     *
     * @return  一个表示操作结果的 Future
     *
     * @throws  WritePendingException
     *          如果通道不允许有多个未完成的写入操作，且前一个写入操作尚未完成
     */
    Future<Integer> write(ByteBuffer src);
}
