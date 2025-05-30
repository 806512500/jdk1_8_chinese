/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
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
 * 当调用（已弃用的）{@link Thread#stop()} 方法时，会在受害者线程中抛出 {@code ThreadDeath} 的实例。
 *
 * <p>应用程序只有在需要在异步终止后进行清理时才应捕获此类的实例。如果方法捕获了 {@code ThreadDeath}，
 * 重要的是必须重新抛出它，以便线程实际终止。
 *
 * <p>如果 {@code ThreadDeath} 从未被捕获，顶级错误处理器 {@linkplain ThreadGroup#uncaughtException}
 * 不会打印任何消息。
 *
 * <p>{@code ThreadDeath} 类特别地是 {@code Error} 的子类，而不是 {@code Exception}，
 * 即使它是一个“正常事件”，因为许多应用程序会捕获所有 {@code Exception} 的发生并丢弃异常。
 *
 * @since   JDK1.0
 */

public class ThreadDeath extends Error {
    private static final long serialVersionUID = -4417128565033088268L;
}
