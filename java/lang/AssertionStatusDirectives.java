/*
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
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
 * 断言状态指令的集合（例如“在包 p 中启用断言”或“在类 c 中禁用断言”）。此类用于 JVM 与
 * <tt>java</tt> 命令行标志 <tt>-enableassertions</tt> (<tt>-ea</tt>) 和
 * <tt>-disableassertions</tt> (<tt>-da</tt>) 暗示的断言状态指令进行通信。
 *
 * @since  1.4
 * @author Josh Bloch
 */
class AssertionStatusDirectives {
    /**
     * 要启用或禁用断言的类。此数组中的字符串是完全限定的类名（例如，“com.xyz.foo.Bar”）。
     */
    String[] classes;

    /**
     * 与 <tt>classes</tt> 平行的数组，指示每个类是否应启用或禁用断言。对于 <tt>classEnabled[i]</tt>
     * 的值为 <tt>true</tt> 表示 <tt>classes[i]</tt> 命名的类应启用断言；值为 <tt>false</tt>
     * 表示应禁用该类的断言。此数组必须与 <tt>classes</tt> 具有相同数量的元素。
     *
     * <p>对于同一类的冲突指令，给定类的最后一个指令获胜。换句话说，如果字符串 <tt>s</tt>
     * 在 <tt>classes</tt> 数组中多次出现，且 <tt>i</tt> 是最大的整数，使得
     * <tt>classes[i].equals(s)</tt>，则 <tt>classEnabled[i]</tt> 表示是否在类 <tt>s</tt>
     * 中启用断言。
     */
    boolean[] classEnabled;

    /**
     * 要启用或禁用断言的包树。此数组中的字符串是完整或部分包名（例如，“com.xyz”或“com.xyz.foo”）。
     */
    String[] packages;

    /**
     * 与 <tt>packages</tt> 平行的数组，指示每个包树是否应启用或禁用断言。对于 <tt>packageEnabled[i]</tt>
     * 的值为 <tt>true</tt> 表示 <tt>packages[i]</tt> 命名的包树应启用断言；值为 <tt>false</tt>
     * 表示应禁用该包树的断言。此数组必须与 <tt>packages</tt> 具有相同数量的元素。
     *
     * 对于同一包树的冲突指令，给定包树的最后一个指令获胜。换句话说，如果字符串 <tt>s</tt>
     * 在 <tt>packages</tt> 数组中多次出现，且 <tt>i</tt> 是最大的整数，使得
     * <tt>packages[i].equals(s)</tt>，则 <tt>packageEnabled[i]</tt> 表示是否在包树 <tt>s</tt>
     * 中启用断言。
     */
    boolean[] packageEnabled;

    /**
     * 是否默认启用非系统类中的断言。
     */
    boolean deflt;
}
