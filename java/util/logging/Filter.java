/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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


package java.util.logging;

/**
 * 一个 Filter 可以用于提供细粒度的控制，超越日志级别提供的控制。
 * <p>
 * 每个 Logger 和每个 Handler 都可以关联一个过滤器。
 * Logger 或 Handler 将调用 isLoggable 方法来检查
 * 给定的 LogRecord 是否应该被发布。如果 isLoggable 返回
 * false，LogRecord 将被丢弃。
 *
 * @since 1.4
 */
@FunctionalInterface
public interface Filter {

    /**
     * 检查给定的日志记录是否应该被发布。
     * @param record 一个 LogRecord
     * @return 如果日志记录应该被发布，则返回 true。
     */
    public boolean isLoggable(LogRecord record);
}
