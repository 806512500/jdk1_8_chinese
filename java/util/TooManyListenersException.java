/*
 * Copyright (c) 1996, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

/**
 * <p>
 * <code> TooManyListenersException </code> 异常用于 Java 事件模型，以注释和实现多播事件源的单播特殊情况。
 * </p>
 * <p>
 * 在任何给定的具体实现中，如果 "void addXyzEventListener" 事件监听器注册模式中包含 "throws TooManyListenersException" 子句，
 * 则用于注释该接口实现了一个单播监听器的特殊情况，即在特定的事件监听器源上只能同时注册一个监听器。
 * </p>
 *
 * @see java.util.EventObject
 * @see java.util.EventListener
 *
 * @author Laurence P. G. Cable
 * @since  JDK1.1
 */

public class TooManyListenersException extends Exception {
    private static final long serialVersionUID = 5074640544770687831L;

    /**
     * 构造一个没有详细消息的 TooManyListenersException。
     * 详细消息是一个描述此特定异常的字符串。
     */

    public TooManyListenersException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 TooManyListenersException。
     * 详细消息是一个描述此特定异常的字符串。
     * @param s 详细消息
     */

    public TooManyListenersException(String s) {
        super(s);
    }
}
