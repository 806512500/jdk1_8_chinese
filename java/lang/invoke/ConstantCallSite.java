/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.invoke;

/**
 * 一个 {@code ConstantCallSite} 是一个目标永久不变且永远不能更改的 {@link CallSite}。
 * 与 {@code ConstantCallSite} 链接的 {@code invokedynamic} 指令永久绑定到调用站点的目标。
 * @author John Rose, JSR 292 EG
 */
public class ConstantCallSite extends CallSite {
    private final boolean isFrozen;

    /**
     * 创建一个具有永久目标的调用站点。
     * @param target 要永久关联到此调用站点的目标
     * @throws NullPointerException 如果提议的目标为 null
     */
    public ConstantCallSite(MethodHandle target) {
        super(target);
        isFrozen = true;
    }

    /**
     * 创建一个具有永久目标的调用站点，可能绑定到调用站点本身。
     * <p>
     * 在调用站点的构造过程中，调用 {@code createTargetHook} 以生成实际的目标，
     * 就好像调用了形式为 {@code (MethodHandle) createTargetHook.invoke(this)} 的调用。
     * <p>
     * 注意，用户代码不能直接在子类构造器中执行此类操作，
     * 因为目标必须在 {@code ConstantCallSite} 构造器返回之前固定。
     * <p>
     * 该钩子据说将调用站点绑定到目标方法句柄，
     * 一个典型的操作是 {@code someTarget.bindTo(this)}。
     * 然而，钩子可以自由地执行任何操作，
     * 包括忽略调用站点并返回一个常量目标。
     * <p>
     * 钩子返回的结果必须是与调用站点完全相同类型的方法句柄。
     * <p>
     * 在调用钩子时，新的 {@code ConstantCallSite} 对象处于部分构造状态。
     * 在这种状态下，
     * 调用 {@code getTarget} 或任何其他尝试使用目标的操作，
     * 都将导致 {@code IllegalStateException}。
     * 在任何时候都可以使用 {@code type} 方法获取调用站点的类型。
     *
     * @param targetType 要永久关联到此调用站点的方法句柄的类型
     * @param createTargetHook 要在调用站点上调用的方法句柄，以生成调用站点的目标
     * @throws WrongMethodTypeException 如果钩子不能在所需的参数上调用，
     *         或者钩子返回的目标不是给定的 {@code targetType}
     * @throws NullPointerException 如果钩子返回 null 值
     * @throws ClassCastException 如果钩子返回的不是 {@code MethodHandle}
     * @throws Throwable 钩子函数抛出的任何其他异常
     */
    protected ConstantCallSite(MethodType targetType, MethodHandle createTargetHook) throws Throwable {
        super(targetType, createTargetHook);
        isFrozen = true;
    }

    /**
     * 返回调用站点的目标方法，其行为
     * 类似于 {@code ConstantCallSite} 的 {@code final} 字段。
     * 也就是说，目标始终是传递给创建此实例的构造器调用的原始值。
     *
     * @return 此调用站点的不可变链接状态，一个常量方法句柄
     * @throws IllegalStateException 如果 {@code ConstantCallSite} 构造器尚未完成
     */
    @Override public final MethodHandle getTarget() {
        if (!isFrozen)  throw new IllegalStateException();
        return target;
    }

    /**
     * 始终抛出 {@link UnsupportedOperationException}。
     * 这种调用站点不能更改其目标。
     * @param ignore 为调用站点提议的新目标，被忽略
     * @throws UnsupportedOperationException 因为这种调用站点不能更改其目标
     */
    @Override public final void setTarget(MethodHandle ignore) {
        throw new UnsupportedOperationException();
    }

    /**
     * 返回此调用站点的永久目标。
     * 由于该目标永远不会改变，这是 {@link CallSite#dynamicInvoker CallSite.dynamicInvoker} 的正确实现。
     * @return 此调用站点的不可变链接状态，一个常量方法句柄
     * @throws IllegalStateException 如果 {@code ConstantCallSite} 构造器尚未完成
     */
    @Override
    public final MethodHandle dynamicInvoker() {
        return getTarget();
    }
}
