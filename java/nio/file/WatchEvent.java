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

package java.nio.file;

/**
 * 一个已注册到 {@link WatchService} 的对象的事件或重复事件。
 *
 * <p> 事件通过其 {@link #kind() 类型} 进行分类，并具有一个 {@link #count() 计数} 以指示观察到该事件的次数。这允许高效地表示重复事件。{@link #context() 上下文} 方法返回与事件关联的任何上下文。在重复事件的情况下，所有事件的上下文都是相同的。
 *
 * <p> 监视事件是不可变的，并且可以安全地被多个并发线程使用。
 *
 * @param   <T>     与事件关联的上下文对象的类型
 *
 * @since 1.7
 */

public interface WatchEvent<T> {

    /**
     * 用于标识的事件类型。
     *
     * @since 1.7
     * @see StandardWatchEventKinds
     */
    public static interface Kind<T> {
        /**
         * 返回事件类型的名称。
         *
         * @return 事件类型的名称
         */
        String name();

        /**
         * 返回 {@link WatchEvent#context 上下文} 值的类型。
         *
         *
         * @return 上下文值的类型
         */
        Class<T> type();
    }

    /**
     * 一个修饰符，用于限定 {@link Watchable} 如何注册到 {@link WatchService}。
     *
     * <p> 本版本未定义任何 <em>标准</em> 修饰符。
     *
     * @since 1.7
     * @see Watchable#register
     */
    public static interface Modifier {
        /**
         * 返回修饰符的名称。
         *
         * @return 修饰符的名称
         */
        String name();
    }

    /**
     * 返回事件类型。
     *
     * @return 事件类型
     */
    Kind<T> kind();

    /**
     * 返回事件计数。如果事件计数大于 {@code 1}，则这是一个重复事件。
     *
     * @return 事件计数
     */
    int count();

    /**
     * 返回事件的上下文。
     *
     * <p> 在 {@link StandardWatchEventKinds#ENTRY_CREATE ENTRY_CREATE}、
     * {@link StandardWatchEventKinds#ENTRY_DELETE ENTRY_DELETE} 和 {@link
     * StandardWatchEventKinds#ENTRY_MODIFY ENTRY_MODIFY} 事件的情况下，上下文是一个 {@code Path}，表示注册到监视服务的目录与创建、删除或修改的条目之间的 {@link Path#relativize 相对} 路径。
     *
     * @return 事件上下文；可能为 {@code null}
     */
    T context();
}
