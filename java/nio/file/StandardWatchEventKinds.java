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

/**
 * 定义 <em>标准</em> 事件类型。
 *
 * @since 1.7
 */

public final class StandardWatchEventKinds {
    private StandardWatchEventKinds() { }

    /**
     * 一个特殊事件，用于指示可能已丢失或丢弃了事件。
     *
     * <p> 该事件的 {@link WatchEvent#context 上下文} 是实现特定的，可能是 {@code null}。该事件的 {@link
     * WatchEvent#count 计数} 可能大于 {@code 1}。
     *
     * @see WatchService
     */
    public static final WatchEvent.Kind<Object> OVERFLOW =
        new StdWatchEventKind<Object>("OVERFLOW", Object.class);

    /**
     * 目录条目创建。
     *
     * <p> 当目录注册了此事件时，如果观察到目录中创建了条目或条目被重命名到目录中，则 {@link WatchKey}
     * 会被排队。此事件的 {@link WatchEvent#count 计数} 始终为 {@code 1}。
     */
    public static final WatchEvent.Kind<Path> ENTRY_CREATE =
        new StdWatchEventKind<Path>("ENTRY_CREATE", Path.class);

    /**
     * 目录条目删除。
     *
     * <p> 当目录注册了此事件时，如果观察到目录中的条目被删除或重命名出目录，则 {@link WatchKey}
     * 会被排队。此事件的 {@link WatchEvent#count 计数} 始终为 {@code 1}。
     */
    public static final WatchEvent.Kind<Path> ENTRY_DELETE =
        new StdWatchEventKind<Path>("ENTRY_DELETE", Path.class);

    /**
     * 目录条目修改。
     *
     * <p> 当目录注册了此事件时，如果观察到目录中的条目被修改，则 {@link WatchKey}
     * 会被排队。此事件的 {@link WatchEvent#count 计数} 为 {@code 1} 或更大。
     */
    public static final WatchEvent.Kind<Path> ENTRY_MODIFY =
        new StdWatchEventKind<Path>("ENTRY_MODIFY", Path.class);

    private static class StdWatchEventKind<T> implements WatchEvent.Kind<T> {
        private final String name;
        private final Class<T> type;
        StdWatchEventKind(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }
        @Override public String name() { return name; }
        @Override public Class<T> type() { return type; }
        @Override public String toString() { return name; }
    }
}
