/*
 * Copyright (c) 2009, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

/**
 * 一个可以持有资源（如文件或套接字句柄）的对象，直到它被关闭。{@code AutoCloseable} 对象的 {@link #close()} 方法在退出 {@code
 * try}-with-resources 语句块时会自动被调用，该对象在资源规范头中声明。这种构造确保了资源的及时释放，避免了资源耗尽异常和错误的发生。
 *
 * @apiNote
 * <p>可能，实际上也很常见，基类实现 AutoCloseable，即使不是所有子类或实例都会持有可释放的资源。对于必须完全通用的代码，或者当已知 {@code AutoCloseable}
 * 实例需要释放资源时，建议使用 {@code try}-with-resources 构造。然而，当使用支持 I/O 基础和非 I/O 基础形式的设施（如 {@link java.util.stream.Stream}）时，
 * 使用非 I/O 基础形式时通常不需要 {@code try}-with-resources 块。
 *
 * @author Josh Bloch
 * @since 1.7
 */
public interface AutoCloseable {
    /**
     * 关闭此资源，释放任何底层资源。此方法在由 {@code try}-with-resources 语句管理的对象上自动调用。
     *
     * <p>虽然此接口方法声明为抛出 {@code
     * Exception}，但实现者强烈建议声明具体的 {@code close} 方法实现来抛出更具体的异常，或者如果关闭操作不会失败，则根本不抛出异常。
     *
     * <p>关闭操作可能失败的情况需要实现者仔细注意。强烈建议在抛出异常之前释放底层资源并内部 <em>标记</em> 资源为已关闭。由于 {@code
     * close} 方法不太可能被调用多次，因此这确保了资源的及时释放。此外，这减少了当资源包装或被其他资源包装时可能出现的问题。
     *
     * <p><em>实现此接口的实现者还强烈建议不要让 {@code close} 方法抛出 {@link
     * InterruptedException}。</em>
     *
     * 此异常与线程的中断状态交互，如果 {@code InterruptedException} 被 {@linkplain Throwable#addSuppressed
     * 抑制}，则可能会导致运行时行为异常。
     *
     * 更一般地，如果异常被抑制会导致问题，{@code AutoCloseable.close}
     * 方法不应该抛出它。
     *
     * <p>请注意，与 {@link java.io.Closeable#close close}
     * 方法不同，此 {@code close} 方法 <em>不要求</em> 是幂等的。换句话说，多次调用此 {@code close} 方法可能会有一些可见的副作用，而 {@code Closeable.close} 要求多次调用没有效果。
     *
     * 然而，实现此接口的实现者强烈建议使他们的 {@code close} 方法幂等。
     *
     * @throws Exception 如果此资源无法关闭
     */
    void close() throws Exception;
}
