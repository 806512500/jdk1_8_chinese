/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

/**
 * 当一个线程正在等待、睡眠或以其他方式被占用时，
 * 并且该线程被中断，无论是活动开始前还是进行中，
 * 都会抛出此异常。有时一个方法可能希望测试当前
 * 线程是否已被中断，如果是，则立即抛出此异常。
 * 以下代码可以用来实现这一效果：
 * <pre>
 *  if (Thread.interrupted())  // 清除中断状态！
 *      throw new InterruptedException();
 * </pre>
 *
 * @author  Frank Yellin
 * @see     java.lang.Object#wait()
 * @see     java.lang.Object#wait(long)
 * @see     java.lang.Object#wait(long, int)
 * @see     java.lang.Thread#sleep(long)
 * @see     java.lang.Thread#interrupt()
 * @see     java.lang.Thread#interrupted()
 * @since   JDK1.0
 */
public
class InterruptedException extends Exception {
    private static final long serialVersionUID = 6700697376100628473L;

    /**
     * 构造一个没有详细信息的 <code>InterruptedException</code>。
     */
    public InterruptedException() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 <code>InterruptedException</code>。
     *
     * @param   s   详细信息消息。
     */
    public InterruptedException(String s) {
        super(s);
    }
}
