/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.security;

/**
 * {@code DomainCombiner} 提供了一种动态更新与当前
 * {@code AccessControlContext} 关联的 ProtectionDomains 的方法。
 *
 * <p> {@code DomainCombiner} 作为参数传递给 {@code AccessControlContext}
 * 的适当构造函数。然后将新构造的上下文传递给
 * {@code AccessController.doPrivileged(..., context)} 方法
 * 以将提供的上下文（及其关联的 {@code DomainCombiner}）与当前执行线程绑定。
 * 随后调用 {@code AccessController.getContext} 或
 * {@code AccessController.checkPermission}
 * 会导致调用 {@code DomainCombiner.combine} 方法。
 *
 * <p> combine 方法接受两个参数。第一个参数表示自最近一次调用
 * {@code AccessController.doPrivileged} 以来当前执行线程的 ProtectionDomains 数组。
 * 如果没有调用 doPrivileged，则第一个参数将包含当前执行线程的所有 ProtectionDomains。
 * 第二个参数表示继承的 ProtectionDomains 数组，可能为 {@code null}。
 * ProtectionDomains 可能从父线程或特权上下文继承。如果没有调用
 * doPrivileged，则第二个参数将包含从父线程继承的 ProtectionDomains。
 * 如果调用了一次或多次 doPrivileged，且最近一次调用为
 * doPrivileged(action, context)，则第二个参数将包含特权上下文的 ProtectionDomains。
 * 如果最近一次调用为 doPrivileged(action)，则没有特权上下文，
 * 第二个参数将为 {@code null}。
 *
 * <p> {@code combine} 方法检查两个输入的 ProtectionDomains 数组，并返回一个包含更新后的
 * ProtectionDomains 的单一数组。在最简单的情况下，{@code combine}
 * 方法将两个栈合并为一个。在更复杂的情况下，
 * {@code combine} 方法返回一个修改后的 ProtectionDomains 栈。
 * 修改可能包括添加新的 ProtectionDomains，移除某些 ProtectionDomains，或简单地
 * 更新现有的 ProtectionDomains。重新排序和其他优化也是允许的。
 * 通常，{@code combine} 方法基于 {@code DomainCombiner} 封装的信息进行更新。
 *
 * <p> 在 {@code AccessController.getContext} 方法
 * 从 {@code DomainCombiner} 接收到合并的 ProtectionDomains 栈后，
 * 它返回一个新的 AccessControlContext，该上下文既包含合并的 ProtectionDomains，
 * 也包含 {@code DomainCombiner}。
 *
 * @see AccessController
 * @see AccessControlContext
 * @since 1.3
 */
public interface DomainCombiner {

    /**
     * 修改或更新提供的 ProtectionDomains。
     * 可以向给定的 ProtectionDomains 添加或移除 ProtectionDomains。
     * ProtectionDomains 可能会被重新排序。单个 ProtectionDomains 可能会被修改
     * （例如，使用一组新的权限）。
     *
     * <p>
     *
     * @param currentDomains 与当前执行线程关联的 ProtectionDomains，
     *          直到最近一次特权的 {@code ProtectionDomain}。
     *          ProtectionDomains 按执行顺序列出，
     *          最近执行的 {@code ProtectionDomain} 位于数组的开头。
     *          如果当前执行线程没有关联的 ProtectionDomains，此参数可能为 {@code null}。<p>
     *
     * @param assignedDomains 继承的 ProtectionDomains 数组。
     *          ProtectionDomains 可能从父线程或特权的 {@code AccessControlContext} 继承。
     *          如果没有继承的 ProtectionDomains，此参数可能为 {@code null}。
     *
     * @return 一个包含更新后的 ProtectionDomains 的新数组，或 {@code null}。
     */
    ProtectionDomain[] combine(ProtectionDomain[] currentDomains,
                                ProtectionDomain[] assignedDomains);
}
