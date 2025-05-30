/*
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.geom;

/**
 * <code>IllegalPathStateException</code> 表示在路径处于非法状态时执行操作所引发的异常，
 * 例如在没有初始 moveto 的情况下向 {@link GeneralPath} 添加路径段。
 *
 */

public class IllegalPathStateException extends RuntimeException {
    /**
     * 构造一个没有详细消息的 <code>IllegalPathStateException</code>。
     *
     * @since   1.2
     */
    public IllegalPathStateException() {
    }

    /**
     * 使用指定的详细消息构造一个 <code>IllegalPathStateException</code>。
     * @param   s   详细消息
     * @since   1.2
     */
    public IllegalPathStateException(String s) {
        super (s);
    }
}
