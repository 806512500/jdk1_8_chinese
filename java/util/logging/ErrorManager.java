/*
 * Copyright (c) 2001, 2004, Oracle and/or its affiliates. All rights reserved.
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
 * ErrorManager 对象可以附加到 Handlers 以处理在日志记录期间发生的任何错误。
 * <p>
 * 在处理日志输出时，如果 Handler 遇到问题，则不应将异常抛回给发出日志调用的一方（他们可能不感兴趣），而应调用其关联的 ErrorManager。
 */

public class ErrorManager {
   private boolean reported = false;

    /*
     * 我们为重要的错误类别声明标准错误代码。
     */

    /**
     * GENERIC_FAILURE 用于不符合其他类别的失败。
     */
    public final static int GENERIC_FAILURE = 0;
    /**
     * WRITE_FAILURE 用于向输出流写入失败的情况。
     */
    public final static int WRITE_FAILURE = 1;
    /**
     * FLUSH_FAILURE 用于向输出流刷新失败的情况。
     */
    public final static int FLUSH_FAILURE = 2;
    /**
     * CLOSE_FAILURE 用于关闭输出流失败的情况。
     */
    public final static int CLOSE_FAILURE = 3;
    /**
     * OPEN_FAILURE 用于打开输出流失败的情况。
     */
    public final static int OPEN_FAILURE = 4;
    /**
     * FORMAT_FAILURE 用于因任何原因导致的格式化失败。
     */
    public final static int FORMAT_FAILURE = 5;

    /**
     * 当 Handler 发生失败时调用 error 方法。
     * <p>
     * 此方法可以在子类中重写。此基类中的默认行为是首次调用时报告给 System.err，后续调用将被忽略。
     *
     * @param msg    描述性字符串（可能为 null）
     * @param ex     异常（可能为 null）
     * @param code   在 ErrorManager 中定义的错误代码
     */
    public synchronized void error(String msg, Exception ex, int code) {
        if (reported) {
            // 我们只报告第一次错误，以避免屏幕被阻塞。
            return;
        }
        reported = true;
        String text = "java.util.logging.ErrorManager: " + code;
        if (msg != null) {
            text = text + ": " + msg;
        }
        System.err.println(text);
        if (ex != null) {
            ex.printStackTrace();
        }
    }
}
