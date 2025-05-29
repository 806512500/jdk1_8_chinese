/*
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

package java.nio.channels;

/**
 * 用于处理异步 I/O 操作结果的处理器。
 *
 * <p> 本包中定义的异步通道允许指定一个完成处理器来处理异步操作的结果。
 * 当 I/O 操作成功完成时，将调用 {@link #completed completed} 方法。
 * 如果 I/O 操作失败，则调用 {@link #failed failed} 方法。
 * 这些方法的实现应尽快完成，以避免调用线程无法调度到其他完成处理器。
 *
 * @param   <V>     I/O 操作的结果类型
 * @param   <A>     附加到 I/O 操作的对象类型
 *
 * @since 1.7
 */

public interface CompletionHandler<V,A> {

    /**
     * 当操作完成时调用。
     *
     * @param   result
     *          I/O 操作的结果。
     * @param   attachment
     *          初始化 I/O 操作时附加的对象。
     */
    void completed(V result, A attachment);

    /**
     * 当操作失败时调用。
     *
     * @param   exc
     *          表示 I/O 操作失败原因的异常
     * @param   attachment
     *          初始化 I/O 操作时附加的对象。
     */
    void failed(Throwable exc, A attachment);
}
