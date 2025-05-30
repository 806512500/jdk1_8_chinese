/*
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一个 {@code MutableCallSite} 是一个 {@link CallSite}，其目标变量的行为类似于普通字段。
 * 链接到 {@code MutableCallSite} 的 {@code invokedynamic} 指令将所有调用委托给站点的当前目标。
 * 可变调用站点的 {@linkplain CallSite#dynamicInvoker 动态调用器} 也将每个调用委托给站点的当前目标。
 * <p>
 * 以下是一个可变调用站点的示例，它在一个方法句柄链中引入了一个状态变量。
 * <!-- JavaDocExamplesTest.testMutableCallSite -->
 * <blockquote><pre>{@code
MutableCallSite name = new MutableCallSite(MethodType.methodType(String.class));
MethodHandle MH_name = name.dynamicInvoker();
MethodType MT_str1 = MethodType.methodType(String.class);
MethodHandle MH_upcase = MethodHandles.lookup()
    .findVirtual(String.class, "toUpperCase", MT_str1);
MethodHandle worker1 = MethodHandles.filterReturnValue(MH_name, MH_upcase);
name.setTarget(MethodHandles.constant(String.class, "Rocky"));
assertEquals("ROCKY", (String) worker1.invokeExact());
name.setTarget(MethodHandles.constant(String.class, "Fred"));
assertEquals("FRED", (String) worker1.invokeExact());
// (可以无限期地继续变异)
 * }</pre></blockquote>
 * <p>
 * 同一个调用站点可以在多个地方同时使用。
 * <blockquote><pre>{@code
MethodType MT_str2 = MethodType.methodType(String.class, String.class);
MethodHandle MH_cat = lookup().findVirtual(String.class,
  "concat", methodType(String.class, String.class));
MethodHandle MH_dear = MethodHandles.insertArguments(MH_cat, 1, ", dear?");
MethodHandle worker2 = MethodHandles.filterReturnValue(MH_name, MH_dear);
assertEquals("Fred, dear?", (String) worker2.invokeExact());
name.setTarget(MethodHandles.constant(String.class, "Wilma"));
assertEquals("WILMA", (String) worker1.invokeExact());
assertEquals("Wilma, dear?", (String) worker2.invokeExact());
 * }</pre></blockquote>
 * <p>
 * <em>目标值的非同步：</em>
 * 对可变调用站点目标的写入不会强制其他线程意识到更新的值。不执行与更新的调用站点相关的适当同步操作的线程可能会缓存旧的目标值，并无限期地延迟使用新的目标值。
 * （这是 Java 内存模型应用于对象字段的正常结果。）
 * <p>
 * {@link #syncAll syncAll} 操作提供了一种方法，即使没有其他同步，也可以强制线程接受新的目标值。
 * <p>
 * 对于将频繁更新的目标值，考虑使用 {@linkplain VolatileCallSite 挥发性调用站点}。
 * @author John Rose, JSR 292 EG
 */
public class MutableCallSite extends CallSite {
    /**
     * 使用给定的方法类型创建一个空白的调用站点对象。
     * 初始目标被设置为一个给定类型的方法句柄，如果调用将抛出 {@link IllegalStateException}。
     * <p>
     * 调用站点的类型被永久设置为给定的类型。
     * <p>
     * 在此 {@code CallSite} 对象从引导方法返回之前，或以其他方式调用之前，
     * 通常会通过调用 {@link CallSite#setTarget(MethodHandle) setTarget} 提供一个更有用的目标方法。
     * @param type 此调用站点将具有的方法类型
     * @throws NullPointerException 如果提议的类型为 null
     */
    public MutableCallSite(MethodType type) {
        super(type);
    }

    /**
     * 使用初始目标方法句柄创建一个调用站点对象。
     * 调用站点的类型被永久设置为初始目标的类型。
     * @param target 将成为调用站点初始目标的方法句柄
     * @throws NullPointerException 如果提议的目标为 null
     */
    public MutableCallSite(MethodHandle target) {
        super(target);
    }

    /**
     * 返回调用站点的目标方法，其行为类似于 {@code MutableCallSite} 的普通字段。
     * <p>
     * {@code getTarget} 与内存的交互与从普通变量（如数组元素或非易失性、非最终字段）读取相同。
     * <p>
     * 特别是，当前线程可以选择重用从内存中读取的目标的先前结果，并且可能无法看到其他线程对目标的最近更新。
     *
     * @return 此调用站点的链接状态，一个可以随时间变化的方法句柄
     * @see #setTarget
     */
    @Override public final MethodHandle getTarget() {
        return target;
    }

    /**
     * 更新此调用站点的目标方法，作为普通变量。
     * 新目标的类型必须与旧目标的类型一致。
     * <p>
     * 与内存的交互与写入普通变量（如数组元素或非易失性、非最终字段）相同。
     * <p>
     * 特别是，无关线程可能在执行内存读取之前看不到更新的目标。
     * 可以通过在引导方法和/或在给定调用站点使用的任何目标方法中放置适当的操作来创建更强的保证。
     *
     * @param newTarget 新的目标
     * @throws NullPointerException 如果提议的新目标为 null
     * @throws WrongMethodTypeException 如果提议的新目标具有与前一个目标不同的方法类型
     * @see #getTarget
     */
    @Override public void setTarget(MethodHandle newTarget) {
        checkTargetChange(this.target, newTarget);
        setTargetNormal(newTarget);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final MethodHandle dynamicInvoker() {
        return makeDynamicInvoker();
    }

    /**
     * 对给定数组中的每个调用站点执行同步操作，强制所有其他线程丢弃之前从任何调用站点的目标加载的任何缓存值。
     * <p>
     * 此操作不会撤销已经在旧目标值上开始的任何调用。
     * （Java 仅支持 {@linkplain java.lang.Object#wait() 向前时间旅行}。）
     * <p>
     * 总体效果是强制所有未来的读取者接受每个调用站点目标的最新存储值。
     * （“最新”是相对于 {@code syncAll} 本身而言的。）
     * 相反，{@code syncAll} 调用可能会阻塞，直到所有读取者（以某种方式）取消缓存每个调用站点目标的所有先前版本。
     * <p>
     * 为避免竞争条件，对 {@code setTarget} 和 {@code syncAll} 的调用通常应在某种互斥机制下执行。
     * 注意，读取线程可能在 {@code setTarget} 调用安装值时（甚至在 {@code syncAll} 确认值之前）观察到更新的目标。
     * 另一方面，读取线程可能在 {@code syncAll} 调用返回之前（甚至在尝试传达更新版本的 {@code setTarget} 之后）观察到目标的先前版本。
     * <p>
     * 此操作可能是昂贵的，应谨慎使用。
     * 如果可能，应缓冲以批处理方式处理调用站点集。
     * <p>
     * 如果 {@code sites} 包含 null 元素，将引发 {@code NullPointerException}。
     * 在这种情况下，数组中的一些非 null 元素可能在方法异常返回之前被处理。
     * 这些元素（如果有）是实现依赖的。
     *
     * <h1>Java 内存模型细节</h1>
     * 从 Java 内存模型的角度来看，此操作执行一个同步操作，其效果类似于当前线程写入一个易失性变量，并且每个其他可能访问受影响调用站点的线程最终读取一个易失性变量。
     * <p>
     * 对于每个单独的调用站点 {@code S}，以下效果是显而易见的：
     * <ul>
     * <li>创建一个新的易失性变量 {@code V}，并由当前线程写入。
     *     根据 JMM，此写入是一个全局同步事件。
     * <li>与线程本地写入事件的排序一样，当前线程已经执行的每个操作都被视为在对 {@code V} 的易失性写入之前发生。
     *     （在某些实现中，这意味着当前线程执行全局释放操作。）
     * <li>特别是，对 {@code S} 的当前目标的写入被视为在对 {@code V} 的易失性写入之前发生。
     * <li>对 {@code V} 的易失性写入被放置在全局同步顺序中（以实现特定的方式）。
     * <li>考虑一个任意线程 {@code T}（除了当前线程）。
     *     如果 {@code T} 在对 {@code V} 的易失性写入之后（在全局同步顺序中）执行同步操作 {@code A}，
     *     则它在读取 {@code S} 的目标时必须看到 {@code S} 的当前目标或该目标的后续写入。
     *     （此约束称为“同步顺序一致性”。）
     * <li>JMM 特别允许优化编译器省略已知无用的变量的读取或写入。
     *     这样的省略读取和写入对 happens-before 关系没有影响。尽管如此，易失性 {@code V} 不会被省略，
     *     即使其写入值是不确定的，其读取值未被使用。
     * </ul>
     * 由于最后一点，实现的行为就像 {@code T} 在其操作 {@code A} 之后立即执行了对 {@code V} 的易失性读取。
     * 在 {@code T} 中操作的本地排序中，此读取发生在对 {@code S} 的目标的任何未来读取之前。
     * 就像实现任意选择了一个由 {@code T} 读取的 {@code S} 的目标，并强制在它之前读取 {@code V}，
     * 从而确保新目标值的通信。
     * <p>
     * 只要遵守 Java 内存模型的约束，实现可以在其他线程（上面的 {@code T}）继续使用 {@code S} 的目标的先前值时延迟 {@code syncAll} 操作的完成。
     * 然而，实现（始终）鼓励避免死锁，并最终要求所有线程考虑更新的目标。
     *
     * <p style="font-size:smaller;">
     * <em>讨论：</em>
     * 为了性能原因，{@code syncAll} 不是单个调用站点上的虚拟方法，而是应用于一组调用站点。
     * 一些实现可能在处理一个或多个同步操作时产生较大的固定开销成本，但每个额外调用站点的增量成本较小。
     * 无论如何，此操作可能是昂贵的，因为其他线程可能需要以某种方式中断，以便注意到更新的目标值。
     * 然而，可以观察到，一次调用同步多个站点具有与多次调用相同的形式效果，每次调用仅针对一个站点。
     *
     * <p style="font-size:smaller;">
     * <em>实现注释：</em>
     * {@code MutableCallSite} 的简单实现可能使用易失性变量作为可变调用站点的目标。
     * 在这种实现中，{@code syncAll} 方法可以是一个空操作，但仍然符合上述 JMM 行为。
     *
     * @param sites 要同步的调用站点数组
     * @throws NullPointerException 如果 {@code sites} 数组引用为 null 或数组包含 null
     */
    public static void syncAll(MutableCallSite[] sites) {
        if (sites.length == 0)  return;
        STORE_BARRIER.lazySet(0);
        for (int i = 0; i < sites.length; i++) {
            sites[i].getClass();  // 触发第一个 null 的 NPE
        }
        // FIXME: NYI
    }
    private static final AtomicInteger STORE_BARRIER = new AtomicInteger();
}
