/*
 * Copyright (c) 1998, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.print;

/**
 * <code>PrinterAbortException</code> 类是 {@link PrinterException} 的子类，
 * 用于指示用户或应用程序在打印过程中终止了打印作业。
 */

public class PrinterAbortException extends PrinterException {

    /**
     * 构造一个新的 <code>PrinterAbortException</code>，不带详细消息。
     */
    public PrinterAbortException() {
        super();
    }

    /**
     * 构造一个新的 <code>PrinterAbortException</code>，带指定的详细消息。
     * @param msg 当抛出 <code>PrinterAbortException</code> 时生成的消息
     */
    public PrinterAbortException(String msg) {
        super(msg);
    }

}
