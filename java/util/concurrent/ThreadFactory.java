/*
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

/**
 * 一个按需创建新线程的对象。使用线程工厂可以避免直接调用 {@link Thread#Thread(Runnable) new Thread}，
 * 使应用程序能够使用特殊的线程子类、优先级等。
 *
 * <p>
 * 这个接口的最简单实现只是：
 *  <pre> {@code
 * class SimpleThreadFactory implements ThreadFactory {
 *   public Thread newThread(Runnable r) {
 *     return new Thread(r);
 *   }
 * }}</pre>
 *
 * {@link Executors#defaultThreadFactory} 方法提供了一个更实用的简单实现，
 * 该实现在线程创建后设置线程上下文到已知值。
 * @since 1.5
 * @author Doug Lea
 */
public interface ThreadFactory {

    /**
     * 构造一个新的 {@code Thread}。实现类还可以初始化优先级、名称、守护状态、{@code ThreadGroup} 等。
     *
     * @param r 由新线程实例执行的运行任务
     * @return 构造的线程，如果创建线程的请求被拒绝，则返回 {@code null}
     */
    Thread newThread(Runnable r);
}
