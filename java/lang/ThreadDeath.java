/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

/**
 * 当调用（已弃用的）{@link Thread#stop()} 方法时，在受害线程中抛出 {@code ThreadDeath} 的实例。
 *
 * <p>应用程序只有在必须异步终止后进行清理时才应捕获此类的实例。如果
 * {@code ThreadDeath} 被方法捕获，重要的是必须重新抛出它，以便线程实际上会终止。
 *
 * <p>如果 {@code ThreadDeath} 从未被捕获，顶级错误处理程序 {@linkplain ThreadGroup#uncaughtException}
 * 不会打印出消息。
 *
 * <p>{@code ThreadDeath} 类特别地是 {@code Error} 的子类，而不是 {@code Exception}，即使它是一个“正常现象”，
 * 因为许多应用程序会捕获所有 {@code Exception} 的发生，然后丢弃异常。
 *
 * @since   JDK1.0
 */

public class ThreadDeath extends Error {
    private static final long serialVersionUID = -4417128565033088268L;
}
