/*
 * Copyright (c) 1995, 2007, Oracle and/or its affiliates. All rights reserved.
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
package java.awt.peer;

import java.awt.Label;

/**
 * {@link Label} 的对等接口。
 *
 * 对等接口仅用于移植 AWT。不建议应用程序开发人员使用这些接口，也不应实现对等接口或直接调用对等实例上的任何对等方法。
 */
public interface LabelPeer extends ComponentPeer {

    /**
     * 设置标签上显示的文本。
     *
     * @param label 要在标签上显示的文本
     *
     * @see Label#setText
     */
    void setText(String label);

    /**
     * 设置标签文本的对齐方式。
     *
     * @param alignment 标签文本的对齐方式
     *
     * @see Label#setAlignment(int)
     * @see Label#CENTER
     * @see Label#RIGHT
     * @see Label#LEFT
     */
    void setAlignment(int alignment);
}
