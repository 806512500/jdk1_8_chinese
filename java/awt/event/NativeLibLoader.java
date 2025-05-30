/*
 * Copyright (c) 1998, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.event;

class NativeLibLoader {

    /**
     * 这是从 java.awt.Toolkit 复制过来的，因为我们还需要在 sun.awt.image 中加载库：
     *
     * 警告：这是一个临时的解决方法，用于解决 AWT 加载本机库时的一个问题。此包（sun.awt.image）中的许多类都有一个本机方法 initIDs()，
     * 该方法初始化其实现的本机部分中使用的 JNI 字段和方法 ID。
     *
     * 由于这些 ID 的使用和存储是由实现库完成的，因此这些方法的实现由特定的 AWT 实现（例如，"Toolkit"s/Peer），如 Motif、Microsoft Windows 或 Tiny 提供。
     * 问题在于，这意味着本机库必须由 java.* 类加载，而这些类不一定知道要加载的库的名称。更好的方法是提供一个单独的库，该库定义 java.awt.* initIDs，
     * 并将相关的符号导出到实现库。
     *
     * 目前，我们知道这是由实现完成的，并假设库的名称为 "awt"。 -br.
     */
    static void loadLibraries() {
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    System.loadLibrary("awt");
                    return null;
                }
            });
    }
}
