/*
 * Copyright (c) 1996, 1997, Oracle and/or its affiliates. All rights reserved.
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

package java.awt;

/**
 * 表示 AWT 组件不在请求操作的适当状态。
 *
 * @author      Jonni Kanerva
 */
public class IllegalComponentStateException extends IllegalStateException {
    /*
     * JDK 1.1 序列化版本号
     */
     private static final long serialVersionUID = -1889339587208144238L;

    /**
     * 构造一个没有详细消息的 IllegalComponentStateException。
     * 详细消息是一个描述此特定异常的字符串。
     */
    public IllegalComponentStateException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 IllegalComponentStateException。
     * 详细消息是一个描述此特定异常的字符串。
     * @param s 包含详细消息的字符串
     */
    public IllegalComponentStateException(String s) {
        super(s);
    }
}
