
/*
 * 版权所有 (c) 2010, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

package java.lang.invoke;

/**
 * <p>
 * 一个 {@code SwitchPoint} 是一个可以向其他线程发布状态转换的对象。
 * 切换点最初处于 <em>有效</em> 状态，但随时可以更改为 <em>无效</em> 状态。无效化不能被逆转。
 * 切换点可以将一个 <em>受保护的对</em> 方法句柄组合成一个 <em>受保护的委托者</em>。
 * 受保护的委托者是一个方法句柄，它委托给旧的方法句柄之一。
 * 切换点的状态决定了哪一方会得到委托。
 * <p>
 * 单个切换点可以用于控制任意数量的方法句柄。
 * （因此，间接地，它可以控制任意数量的调用站点。）
 * 这是通过使用单个切换点作为工厂来组合任意数量的受保护的方法句柄对成受保护的委托者来完成的。
 * <p>
 * 当从受保护的对创建受保护的委托者时，该对被包装在一个新的方法句柄 {@code M} 中，
 * 该方法句柄永久地与创建它的切换点关联。
 * 每个对由一个目标 {@code T} 和一个回退 {@code F} 组成。
 * 当切换点有效时，对 {@code M} 的调用被委托给 {@code T}。
 * 无效化后，调用被委托给 {@code F}。
 * <p>
 * 无效化是全局且立即的，就像切换点包含一个在每次调用 {@code M} 时都会检查的易失性布尔变量。
 * 无效化也是永久的，这意味着切换点只能改变一次状态。
 * 切换点在无效化后将始终委托给 {@code F}。
 * 在这一点上，{@code guardWithTest} 可能会忽略 {@code T} 并返回 {@code F}。
 * <p>
 * 以下是一个切换点在实际中的示例：
 * <pre>{@code
 * MethodHandle MH_strcat = MethodHandles.lookup()
 *     .findVirtual(String.class, "concat", MethodType.methodType(String.class, String.class));
 * SwitchPoint spt = new SwitchPoint();
 * assert(!spt.hasBeenInvalidated());
 * // 以下步骤可以重复使用相同的切换点：
 * MethodHandle worker1 = MH_strcat;
 * MethodHandle worker2 = MethodHandles.permuteArguments(MH_strcat, MH_strcat.type(), 1, 0);
 * MethodHandle worker = spt.guardWithTest(worker1, worker2);
 * assertEquals("method", (String) worker.invokeExact("met", "hod"));
 * SwitchPoint.invalidateAll(new SwitchPoint[]{ spt });
 * assert(spt.hasBeenInvalidated());
 * assertEquals("hodmet", (String) worker.invokeExact("met", "hod"));
 * }</pre>
 * <p style="font-size:smaller;">
 * <em>讨论：</em>
 * 切换点无需子类化即可使用。它们也可以被子类化。
 * 这可能有助于将应用程序特定的无效化逻辑与切换点关联起来。
 * 请注意，切换点与其生成和消耗的方法句柄之间没有永久的关联。
 * 垃圾收集器可以独立于切换点本身的生命周期收集切换点生成或消耗的方法句柄。
 * <p style="font-size:smaller;">
 * <em>实现说明：</em>
 * 切换点的行为类似于基于 {@link MutableCallSite} 实现的，大致如下：
 * <pre>{@code
 * public class SwitchPoint {
 *     private static final MethodHandle
 *         K_true  = MethodHandles.constant(boolean.class, true),
 *         K_false = MethodHandles.constant(boolean.class, false);
 *     private final MutableCallSite mcs;
 *     private final MethodHandle mcsInvoker;
 *     public SwitchPoint() {
 *         this.mcs = new MutableCallSite(K_true);
 *         this.mcsInvoker = mcs.dynamicInvoker();
 *     }
 *     public MethodHandle guardWithTest(
 *             MethodHandle target, MethodHandle fallback) {
 *         // 注意：mcsInvoker 的类型为 ()boolean。
 *         // 目标和回退可以接受任何参数，但必须具有相同的类型。
 *         return MethodHandles.guardWithTest(this.mcsInvoker, target, fallback);
 *     }
 *     public static void invalidateAll(SwitchPoint[] spts) {
 *         List<MutableCallSite> mcss = new ArrayList<>();
 *         for (SwitchPoint spt : spts)  mcss.add(spt.mcs);
 *         for (MutableCallSite mcs : mcss)  mcs.setTarget(K_false);
 *         MutableCallSite.syncAll(mcss.toArray(new MutableCallSite[0]));
 *     }
 * }
 * }</pre>
 * @author Remi Forax, JSR 292 EG
 */
public class SwitchPoint {
    private static final MethodHandle
        K_true  = MethodHandles.constant(boolean.class, true),
        K_false = MethodHandles.constant(boolean.class, false);

    private final MutableCallSite mcs;
    private final MethodHandle mcsInvoker;

    /**
     * 创建一个新的切换点。
     */
    public SwitchPoint() {
        this.mcs = new MutableCallSite(K_true);
        this.mcsInvoker = mcs.dynamicInvoker();
    }

    /**
     * 确定此切换点是否已被无效化。
     *
     * <p style="font-size:smaller;">
     * <em>讨论：</em>
     * 由于无效化的单向性质，一旦切换点开始返回 {@code hasBeenInvalidated} 为 true，
     * 它将始终在未来这样做。
     * 另一方面，对其他线程可见的有效切换点可能由于其他线程的请求而随时被无效化。
     * <p style="font-size:smaller;">
     * 由于无效化是一个全局且立即的操作，
     * 在有效切换点上执行此查询时，
     * 必须与可能导致无效化的任何其他线程内部排序。
     * 因此，此查询可能代价高昂。
     * 构建一个查询切换点 {@code s} 无效化状态的布尔值方法句柄的推荐方法是
     * 调用 {@code s.guardWithTest}，
     * 并使用 {@link MethodHandles#constant} true 和 false 方法句柄。
     *
     * @return 如果此切换点已被无效化，则返回 true
     */
    public boolean hasBeenInvalidated() {
        return (mcs.getTarget() != K_true);
    }

                /**
     * 返回一个方法句柄，该方法句柄始终委托给目标或回退方法。
     * 只要切换点有效，方法句柄将始终委托给目标。
     * 之后，它将永久委托给回退方法。
     * <p>
     * 目标和回退方法必须具有完全相同的方法类型，
     * 生成的组合方法句柄也将是这种类型。
     *
     * @param target 只要切换点有效，由切换点选择的方法句柄
     * @param fallback 切换点无效后，由切换点选择的方法句柄
     * @return 一个始终调用目标或回退方法的组合方法句柄
     * @throws NullPointerException 如果任一参数为 null
     * @throws IllegalArgumentException 如果两个方法类型不匹配
     * @see MethodHandles#guardWithTest
     */
    public MethodHandle guardWithTest(MethodHandle target, MethodHandle fallback) {
        if (mcs.getTarget() == K_false)
            return fallback;  // 已经无效
        return MethodHandles.guardWithTest(mcsInvoker, target, fallback);
    }

    /**
     * 将给定的所有切换点设置为无效状态。
     * 执行此调用后，任何线程都不会观察到任何切换点处于有效状态。
     * <p>
     * 此操作可能代价高昂，应谨慎使用。
     * 如果可能，应缓冲以批量处理切换点集。
     * <p>
     * 如果 {@code switchPoints} 包含 null 元素，
     * 将引发 {@code NullPointerException}。
     * 在这种情况下，数组中的一些非 null 元素可能在方法异常返回之前被处理。
     * 这些元素（如果有）是实现依赖的。
     *
     * <p style="font-size:smaller;">
     * <em>讨论：</em>
     * 由于性能原因，{@code invalidateAll} 不是单个切换点上的虚拟方法，
     * 而是应用于一组切换点。
     * 一些实现可能在处理一个或多个无效操作时产生较大的固定开销成本，
     * 但每个额外的无效操作的成本较小。
     * 无论如何，此操作可能是昂贵的，因为
     * 其他线程可能需要以某种方式中断
     * 以便注意到更新的切换点状态。
     * 然而，可以观察到，一次调用使多个切换点无效
     * 与多次调用，每次仅使一个切换点无效，具有相同的形式效果。
     *
     * <p style="font-size:smaller;">
     * <em>实现说明：</em>
     * {@code SwitchPoint} 的简单实现可能使用
     * 私有的 {@link MutableCallSite} 来发布切换点的状态。
     * 在这种实现中，{@code invalidateAll} 方法可以
     * 简单地更改调用站点的目标，并发出一个调用
     * {@linkplain MutableCallSite#syncAll 同步} 所有
     * 私有调用站点。
     *
     * @param switchPoints 要同步的调用站点数组
     * @throws NullPointerException 如果 {@code switchPoints} 数组引用为 null
     *                              或数组包含 null
     */
    public static void invalidateAll(SwitchPoint[] switchPoints) {
        if (switchPoints.length == 0)  return;
        MutableCallSite[] sites = new MutableCallSite[switchPoints.length];
        for (int i = 0; i < switchPoints.length; i++) {
            SwitchPoint spt = switchPoints[i];
            if (spt == null)  break;  // MSC.syncAll 将触发 NPE
            sites[i] = spt.mcs;
            spt.mcs.setTarget(K_false);
        }
        MutableCallSite.syncAll(sites);
    }
}
