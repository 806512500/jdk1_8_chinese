/*
 * Copyright (c) 2010, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * 一个 {@code VolatileCallSite} 是一个 {@link CallSite}，其目标行为类似于一个易失变量。
 * 一个链接到 {@code VolatileCallSite} 的 {@code invokedynamic} 指令会立即看到对其调用站点目标的更新，
 * 即使更新发生在另一个线程中。
 * 这种紧密的线程耦合可能会带来性能损失。
 * <p>
 * 与 {@code MutableCallSite} 不同，易失调用站点上没有
 * {@linkplain MutableCallSite#syncAll syncAll 操作}，因为对易失变量的每次写入都隐式地
 * 与读取线程同步。
 * <p>
 * 在其他方面，一个 {@code VolatileCallSite} 可以与 {@code MutableCallSite} 互换使用。
 * @see MutableCallSite
 * @author John Rose, JSR 292 EG
 */
public class VolatileCallSite extends CallSite {
    /**
     * 创建一个具有易失绑定到其目标的调用站点。
     * 初始目标被设置为一个方法句柄，如果被调用，该方法句柄将抛出一个 {@code IllegalStateException}。
     * @param type 此调用站点将具有的方法类型
     * @throws NullPointerException 如果提议的类型为 null
     */
    public VolatileCallSite(MethodType type) {
        super(type);
    }

    /**
     * 创建一个具有易失绑定到其目标的调用站点。
     * 目标被设置为给定的值。
     * @param target 将成为调用站点初始目标的方法句柄
     * @throws NullPointerException 如果提议的目标为 null
     */
    public VolatileCallSite(MethodHandle target) {
        super(target);
    }

    /**
     * 返回调用站点的目标方法，其行为
     * 类似于 {@code VolatileCallSite} 的 {@code volatile} 字段。
     * <p>
     * {@code getTarget} 与内存的交互与从 {@code volatile} 字段读取相同。
     * <p>
     * 特别是，当前线程必须从内存中进行一次新的读取目标，并且不得错过
     * 另一个线程对目标的最近更新。
     *
     * @return 此调用站点的链接状态，一个可以随时间变化的方法句柄
     * @see #setTarget
     */
    @Override public final MethodHandle getTarget() {
        return getTargetVolatile();
    }

    /**
     * 更新此调用站点的目标方法，作为易失变量。
     * 新目标的类型必须与旧目标的类型一致。
     * <p>
     * 与内存的交互与对易失字段的写入相同。
     * 特别是，任何线程在下次调用 {@code getTarget} 时都保证能看到更新的目标。
     * @param newTarget 新目标
     * @throws NullPointerException 如果提议的新目标为 null
     * @throws WrongMethodTypeException 如果提议的新目标
     *         具有与前一个目标不同的方法类型
     * @see #getTarget
     */
    @Override public void setTarget(MethodHandle newTarget) {
        checkTargetChange(getTargetVolatile(), newTarget);
        setTargetVolatile(newTarget);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final MethodHandle dynamicInvoker() {
        return makeDynamicInvoker();
    }
}
