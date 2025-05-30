/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * {@code DomainCombiner} 提供了一种动态更新与当前
 * {@code AccessControlContext} 关联的 ProtectionDomains 的方法。
 *
 * <p> {@code DomainCombiner} 作为参数传递给
 * {@code AccessControlContext} 的适当构造函数。
 * 然后将新构造的上下文传递给
 * {@code AccessController.doPrivileged(..., context)} 方法
 * 以将提供的上下文（及其关联的 {@code DomainCombiner}）
 * 绑定到当前执行线程。随后调用
 * {@code AccessController.getContext} 或
 * {@code AccessController.checkPermission}
 * 会导致调用 {@code DomainCombiner.combine} 方法。
 *
 * <p> combine 方法接受两个参数。第一个参数表示
 * 自上次调用 {@code AccessController.doPrivileged} 以来
 * 当前执行线程的 ProtectionDomains 数组。
 * 如果没有调用 doPrivileged，则第一个参数将包含
 * 当前执行线程的所有 ProtectionDomains。
 * 第二个参数表示继承的 ProtectionDomains 数组，
 * 可能为 {@code null}。ProtectionDomains 可能从父线程继承，
 * 或从特权上下文继承。如果没有调用 doPrivileged，
 * 则第二个参数将包含从父线程继承的 ProtectionDomains。
 * 如果调用了一次或多次 doPrivileged，且最近的一次调用是
 * doPrivileged(action, context)，则第二个参数将包含
 * 特权上下文的 ProtectionDomains。如果最近的一次调用是
 * doPrivileged(action)，则没有特权上下文，
 * 第二个参数将为 {@code null}。
 *
 * <p> combine 方法调查两个输入的 ProtectionDomains 数组，
 * 并返回一个包含更新后的 ProtectionDomains 的数组。
 * 在最简单的情况下，combine 方法将两个堆栈合并为一个。
 * 在更复杂的情况下，combine 方法返回一个修改后的
 * ProtectionDomains 堆栈。修改可能包括添加新的 ProtectionDomains，
 * 删除某些 ProtectionDomains，或简单地更新现有的 ProtectionDomains。
 * 重新排序和其他优化也是允许的。通常，
 * combine 方法基于 {@code DomainCombiner} 封装的信息进行更新。
 *
 * <p> 在 {@code AccessController.getContext} 方法
 * 从 {@code DomainCombiner} 接收到合并后的 ProtectionDomains 堆栈后，
 * 它返回一个新的 AccessControlContext，该上下文包含合并后的 ProtectionDomains
 * 以及 {@code DomainCombiner}。
 *
 * @see AccessController
 * @see AccessControlContext
 * @since 1.3
 */
public interface DomainCombiner {

    /**
     * 修改或更新提供的 ProtectionDomains。
     * 可以向提供的 ProtectionDomains 添加或删除 ProtectionDomains。
     * ProtectionDomains 可以重新排序。
     * 个别 ProtectionDomains 可以修改（例如，使用新的权限集）。
     *
     * <p>
     *
     * @param currentDomains 与当前执行线程关联的 ProtectionDomains，
     *          一直到最近的特权 {@code ProtectionDomain}。
     *          ProtectionDomains 按执行顺序列出，
     *          最近执行的 {@code ProtectionDomain}
     *          位于数组的开头。如果当前执行线程
     *          没有关联的 ProtectionDomains，此参数可能为 {@code null}。<p>
     *
     * @param assignedDomains 继承的 ProtectionDomains 数组。
     *          ProtectionDomains 可能从父线程继承，
 *          或从特权 {@code AccessControlContext} 继承。
     *          如果没有继承的 ProtectionDomains，此参数可能为 {@code null}。
     *
     * @return 包含更新后的 ProtectionDomains 的新数组，
     *          或 {@code null}。
     */
    ProtectionDomain[] combine(ProtectionDomain[] currentDomains,
                                ProtectionDomain[] assignedDomains);
}
