/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.file;

import java.util.List;

/**
 * 表示将一个 {@link Watchable} 对象注册到 {@link WatchService} 的令牌。
 *
 * <p> 当一个可监视对象注册到监视服务时，会创建一个监视键。该键在以下情况下保持 {@link #isValid 有效}：
 * <ol>
 *   <li> 它被显式取消，通过调用其 {@link #cancel 取消} 方法，或者</li>
 *   <li> 隐式取消，因为对象不再可访问，或者 </li>
 *   <li> 通过 {@link WatchService#close 关闭} 监视服务。 </li>
 * </ol>
 *
 * <p> 监视键有一个状态。当最初创建时，该键被认为是 <em>就绪</em> 的。当检测到事件时，该键变为 <em>已触发</em>
 * 并排队，以便通过调用监视服务的 {@link WatchService#poll() 轮询} 或 {@link WatchService#take() 获取} 方法来检索。
 * 一旦被触发，键将保持在此状态，直到调用其 {@link #reset 重置} 方法将其恢复到就绪状态。当键处于已触发状态时，
 * 检测到的事件将被排队，但不会导致键重新排队以从监视服务中检索。通过调用键的 {@link #pollEvents 检索事件} 方法
 * 来检索事件。此方法检索并移除为对象累积的所有事件。当最初创建时，监视键没有待处理的事件。通常在键处于已触发状态时
 * 检索事件，这导致以下惯用法：
 *
 * <pre>
 *     for (;;) {
 *         // 检索键
 *         WatchKey key = watcher.take();
 *
 *         // 处理事件
 *         for (WatchEvent&lt;?&gt; event: key.pollEvents()) {
 *             :
 *         }
 *
 *         // 重置键
 *         boolean valid = key.reset();
 *         if (!valid) {
 *             // 对象不再注册
 *         }
 *     }
 * </pre>
 *
 * <p> 监视键可以安全地由多个并发线程使用。如果有多个线程从监视服务中检索已触发的键，则应确保仅在处理完对象的事件后
 * 才调用 {@code reset} 方法。这确保了任何时候只有一个线程在处理对象的事件。
 *
 * @since 1.7
 */

public interface WatchKey {

    /**
     * 告诉此监视键是否有效。
     *
     * <p> 监视键在创建时有效，并保持有效直到被取消，或其监视服务被关闭。
     *
     * @return  如果且仅如果此监视键有效，则返回 {@code true}
     */
    boolean isValid();

    /**
     * 检索并移除此监视键的所有待处理事件，返回检索到的事件的 {@code List}。
     *
     * <p> 注意，如果没有任何待处理的事件，此方法不会等待。
     *
     * @return  检索到的事件列表；可能为空
     */
    List<WatchEvent<?>> pollEvents();

    /**
     * 重置此监视键。
     *
     * <p> 如果此监视键已被取消或此监视键已处于就绪状态，则调用此方法不会产生任何效果。否则，如果对象有待处理的事件，
     * 则此监视键将立即重新排队到监视服务。如果没有待处理的事件，则监视键将进入就绪状态，并保持在此状态，直到检测到事件
 * 或监视键被取消。
     *
     * @return  如果监视键有效且已被重置，则返回 {@code true}；如果监视键因不再 {@link #isValid 有效} 而无法重置，
     *          则返回 {@code false}
     */
    boolean reset();

    /**
     * 取消在监视服务中的注册。返回时，监视键将无效。如果监视键正在排队，等待从监视服务中检索，则它将保持在队列中，
     * 直到被移除。待处理的事件（如果有）将保持待处理状态，并且可以在键被取消后通过调用 {@link #pollEvents pollEvents}
     * 方法来检索。
     *
     * <p> 如果此监视键已被取消，则调用此方法不会产生任何效果。一旦取消，监视键将永远无效。
     */
    void cancel();

    /**
     * 返回为此监视键创建的对象。即使键被取消，此方法也将继续返回该对象。
     *
     * <p> 由于 {@code WatchService} 旨在直接映射到本地文件事件通知设施（如果可用），因此监视已注册对象的许多细节
     * 高度依赖于实现。例如，当监视目录的更改时，如果目录在文件系统中被移动或重命名，则不能保证监视键将被取消，
     * 因此通过此方法返回的对象可能不再是目录的有效路径。
     *
     * @return  为此监视键创建的对象
     */
    Watchable watchable();
}
