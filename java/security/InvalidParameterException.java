/*
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
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

package java.security;

/**
 * 此异常设计用于 JCA/JCE 引擎类，当向方法传递无效参数时抛出。
 *
 * @author Benjamin Renaud
 */

public class InvalidParameterException extends IllegalArgumentException {

    private static final long serialVersionUID = -857968536935667808L;

    /**
     * 构造一个没有详细消息的 InvalidParameterException。
     * 详细消息是一个描述此特定异常的字符串。
     */
    public InvalidParameterException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 InvalidParameterException。
     * 详细消息是一个描述此特定异常的字符串。
     *
     * @param msg 详细消息。
     */
    public InvalidParameterException(String msg) {
        super(msg);
    }
}
