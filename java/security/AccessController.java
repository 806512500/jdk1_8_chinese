/*
 * 版权所有 (c) 1997, 2019, Oracle 和/或其附属公司。保留所有权利。
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

package java.security;

import sun.security.util.Debug;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

/**
 * <p> AccessController 类用于访问控制操作和决策。
 *
 * <p> 更具体地说，AccessController 类用于三个目的：
 *
 * <ul>
 * <li>根据当前生效的安全策略，决定是否允许或拒绝访问关键系统资源，
 * <li>标记代码为“特权”，从而影响后续的访问决策，以及
 * <li>获取当前调用上下文的“快照”，以便从不同的上下文进行访问控制决策时可以参考保存的上下文。 </ul>
 *
 * <p> {@link #checkPermission(Permission) checkPermission} 方法
 * 确定是否应授予或拒绝由指定权限表示的访问请求。下面是一个示例调用。在这个例子中，{@code checkPermission} 将确定
 * 是否授予对“/temp”目录中名为“testFile”的文件的“读取”访问权限。
 *
 * <pre>
 *
 * FilePermission perm = new FilePermission("/temp/testFile", "read");
 * AccessController.checkPermission(perm);
 *
 * </pre>
 *
 * <p> 如果请求的访问被允许，
 * {@code checkPermission} 静默返回。如果被拒绝，则抛出
 * AccessControlException。如果请求的权限类型不正确或包含无效值，也会抛出 AccessControlException。
 * 尽可能提供此类信息。
 *
 * 假设当前线程遍历了 m 个调用者，顺序为调用者 1 到调用者 2 到调用者 m。那么调用者 m 调用了
 * {@code checkPermission} 方法。
 * {@code checkPermission} 方法根据以下算法确定是否授予访问权限：
 *
 *  <pre> {@code
 * for (int i = m; i > 0; i--) {
 *
 *     if (调用者 i 的域没有该权限)
 *         抛出 AccessControlException
 *
 *     else if (调用者 i 被标记为特权) {
 *         if (在调用 doPrivileged 时指定了上下文)
 *             context.checkPermission(permission)
 *         if (在调用 doPrivileged 时指定了有限权限) {
 *             for (每个有限权限) {
 *                 if (有限权限隐含请求的权限)
 *                     返回;
 *             }
 *         } else
 *             返回;
 *     }
 * }
 *
 * // 接下来，检查线程创建时继承的上下文。
 * // 每当创建新线程时，此时的 AccessControlContext 会被存储并与新线程关联，作为“继承”的上下文。
 *
 * inheritedContext.checkPermission(permission);
 * }</pre>
 *
 * <p> 调用者可以被标记为“特权”
 * （参见 {@link #doPrivileged(PrivilegedAction) doPrivileged} 和下文）。
 * 在进行访问控制决策时，如果 {@code checkPermission} 方法到达一个
 * 通过 {@code doPrivileged} 调用标记为“特权”的调用者，并且没有上下文参数（有关上下文参数的信息见下文），则停止检查。如果该调用者的域具有
 * 指定的权限并且至少有一个限制权限参数（如果有）隐含请求的权限，则不再进行进一步检查，
 * {@code checkPermission} 静默返回，表示允许请求的访问。如果该域没有指定的权限，则抛出异常，如常。如果调用者的域具有指定的权限
 * 但没有被任何在 {@code doPrivileged} 调用中给出的限制权限参数隐含，则权限检查将继续
 * 直到没有更多的调用者或另一个 {@code doPrivileged} 调用匹配请求的权限并正常返回。
 *
 * <p> “特权”功能的正常使用如下。如果你不需要从“特权”块中返回值，可以这样做：
 *
 *  <pre> {@code
 * somemethod() {
 *     ...正常代码在这里...
 *     AccessController.doPrivileged(new PrivilegedAction<Void>() {
 *         public Void run() {
 *             // 特权代码放在这里，例如：
 *             System.loadLibrary("awt");
 *             return null; // 没有需要返回的内容
 *         }
 *     });
 *     ...正常代码在这里...
 * }}</pre>
 *
 * <p>
 * PrivilegedAction 是一个具有单个方法的接口，名为
 * {@code run}。
 * 上面的例子展示了该接口实现的创建；提供了一个具体的
 * {@code run} 方法实现。
 * 当调用 {@code doPrivileged} 时，会传递一个
 * PrivilegedAction 实现的实例。
 * {@code doPrivileged} 方法在启用特权后调用
 * PrivilegedAction 实现中的 {@code run} 方法，并将
 * {@code run} 方法的返回值作为
 * {@code doPrivileged} 的返回值（在本例中被忽略）。
 *
 * <p> 如果你需要返回一个值，可以像下面这样操作：
 *
 *  <pre> {@code
 * somemethod() {
 *     ...正常代码在这里...
 *     String user = AccessController.doPrivileged(
 *         new PrivilegedAction<String>() {
 *         public String run() {
 *             return System.getProperty("user.name");
 *             }
 *         });
 *     ...正常代码在这里...
 * }}</pre>
 *
 * <p> 如果你的 {@code run} 方法中执行的操作可能
 * 抛出“检查”异常（即在方法的 {@code throws} 子句中列出的异常），则需要使用
 * {@code PrivilegedExceptionAction} 接口而不是
 * {@code PrivilegedAction} 接口：
 *
 *  <pre> {@code
 * somemethod() throws FileNotFoundException {
 *     ...正常代码在这里...
 *     try {
 *         FileInputStream fis = AccessController.doPrivileged(
 *         new PrivilegedExceptionAction<FileInputStream>() {
 *             public FileInputStream run() throws FileNotFoundException {
 *                 return new FileInputStream("someFile");
 *             }
 *         });
 *     } catch (PrivilegedActionException e) {
 *         // e.getException() 应该是一个 FileNotFoundException 的实例，
 *         // 因为只有“检查”异常会被“包装”在 PrivilegedActionException 中。
 *         throw (FileNotFoundException) e.getException();
 *     }
 *     ...正常代码在这里...
 *  }}</pre>
 *
 * <p> 在使用“特权”构造时要*非常*小心，并且始终记住使特权代码部分尽可能小。
 * 你可以传递 {@code Permission} 参数以进一步限制“特权”的范围（见下文）。
 *
 *
 * <p> 请注意，{@code checkPermission} 总是在当前执行线程的上下文中执行安全检查。
 * 有时需要在一个给定上下文中进行的安全检查实际上需要从
 * <i>不同的</i> 上下文（例如，从工作线程中）进行。
 * {@link #getContext() getContext} 方法和
 * AccessControlContext 类为此情况提供支持。
 * {@code getContext} 方法对当前调用上下文进行“快照”，并将其
 * 放入 AccessControlContext 对象中，然后返回该对象。一个示例调用如下：
 *
 * <pre>
 *
 * AccessControlContext acc = AccessController.getContext()
 *
 * </pre>
 *
 * <p>
 * AccessControlContext 本身也有一个 {@code checkPermission} 方法
 * 该方法基于它封装的上下文进行访问决策，而不是当前执行线程的上下文。
 * 因此，不同上下文中的代码可以调用该方法，对之前保存的 AccessControlContext 对象进行调用。一个示例调用如下：
 *
 * <pre>
 *
 * acc.checkPermission(permission)
 *
 * </pre>
 *
 * <p> 有时你无法预先知道要检查哪些权限。在这种情况下，可以使用带有上下文的
 * doPrivileged 方法。你还可以通过传递额外的 {@code Permission}
 * 参数来限制特权代码的范围。
 *
 *  <pre> {@code
 * somemethod() {
 *     AccessController.doPrivileged(new PrivilegedAction<Object>() {
 *         public Object run() {
 *             // 代码放在这里。此 run 方法中的任何权限检查将要求
 *             // 调用者的保护域和快照上下文的交集具有所需的权限。如果请求的
 *             // 权限未被限制的 FilePermission 参数隐含，则线程的检查将继续超出
 *             // doPrivileged 的调用者。
 *         }
 *     }, acc, new FilePermission("/temp/*", "read"));
 *     ...正常代码在这里...
 * }}</pre>
 * <p> 传递一个限制的 {@code Permission} 参数，如 {@code AllPermission} 的实例，等同于调用没有限制的
 * {@code Permission} 参数的等效 {@code doPrivileged} 方法。传递一个零长度的 {@code Permission} 数组
 * 将禁用代码特权，使得检查总是超出该 {@code doPrivileged} 方法的调用者。
 *
 * @see AccessControlContext
 *
 * @author Li Gong
 * @author Roland Schemers
 */


public final class AccessController {

    /**
     * 不允许任何人实例化 AccessController
     */
    private AccessController() { }

    /**
     * 执行指定的 {@code PrivilegedAction} 并启用特权。该操作将使用调用者的保护域所拥有的 <i>所有</i> 权限执行。
     *
     * <p> 如果操作的 {@code run} 方法抛出一个（未检查的）异常，它将通过此方法传播。
     *
     * <p> 注意，在执行操作时，与当前 AccessControlContext 关联的任何 DomainCombiner 都将被忽略。
     *
     * @param <T> PrivilegedAction 的 {@code run} 方法返回的值的类型。
     *
     * @param action 要执行的操作。
     *
     * @return 操作的 {@code run} 方法返回的值。
     *
     * @exception NullPointerException 如果操作为 {@code null}
     *
     * @see #doPrivileged(PrivilegedAction,AccessControlContext)
     * @see #doPrivileged(PrivilegedExceptionAction)
     * @see #doPrivilegedWithCombiner(PrivilegedAction)
     * @see java.security.DomainCombiner
     */

    @CallerSensitive
    public static native <T> T doPrivileged(PrivilegedAction<T> action);

    /**
     * 执行指定的 {@code PrivilegedAction} 并启用特权。该操作将使用调用者的保护域所拥有的 <i>所有</i> 权限执行。
     *
     * <p> 如果操作的 {@code run} 方法抛出一个（未检查的）异常，它将通过此方法传播。
     *
     * <p> 该方法在执行操作时保留当前 AccessControlContext 的 DomainCombiner（可能是 null）。
     *
     * @param <T> PrivilegedAction 的 {@code run} 方法返回的值的类型。
     *
     * @param action 要执行的操作。
     *
     * @return 操作的 {@code run} 方法返回的值。
     *
     * @exception NullPointerException 如果操作为 {@code null}
     *
     * @see #doPrivileged(PrivilegedAction)
     * @see java.security.DomainCombiner
     *
     * @since 1.6
     */
    @CallerSensitive
    public static <T> T doPrivilegedWithCombiner(PrivilegedAction<T> action) {
        AccessControlContext acc = getStackAccessControlContext();
        if (acc == null) {
            return AccessController.doPrivileged(action);
        }
        DomainCombiner dc = acc.getAssignedCombiner();
        return AccessController.doPrivileged(action,
                                             preserveCombiner(dc, Reflection.getCallerClass()));
    }


    /**
     * 执行指定的 {@code PrivilegedAction} 并启用特权，且受限于指定的 {@code AccessControlContext}。该操作将使用调用者的保护域所拥有的权限与指定的 {@code AccessControlContext} 所代表的域所拥有的权限的交集执行。
     * <p>
     * 如果操作的 {@code run} 方法抛出一个（未检查的）异常，它将通过此方法传播。
     * <p>
     * 如果安装了安全管理器，并且指定的 {@code AccessControlContext} 不是由系统代码创建的，且调用者的 {@code ProtectionDomain} 没有被授予 {@literal "createAccessControlContext"}
     * {@link java.security.SecurityPermission}，那么该操作将不带任何权限执行。
     *
     * @param <T> PrivilegedAction 的 {@code run} 方法返回的值的类型。
     * @param action 要执行的操作。
     * @param context 一个 <i>访问控制上下文</i>，表示在执行指定操作之前对调用者域的权限应用的限制。如果上下文为
     *                {@code null}，则不应用额外的限制。
     *
     * @return 操作的 {@code run} 方法返回的值。
     *
     * @exception NullPointerException 如果操作为 {@code null}
     *
     * @see #doPrivileged(PrivilegedAction)
     * @see #doPrivileged(PrivilegedExceptionAction,AccessControlContext)
     */
    @CallerSensitive
    public static native <T> T doPrivileged(PrivilegedAction<T> action,
                                            AccessControlContext context);


    /**
     * 执行指定的 {@code PrivilegedAction} 并启用特权，且受限于指定的 {@code AccessControlContext}，并且特权范围受限于指定的 {@code Permission} 参数。
     *
     * 该操作将使用调用者的保护域所拥有的权限与指定的 {@code AccessControlContext} 所代表的域所拥有的权限的交集执行。
     * <p>
     * 如果操作的 {@code run} 方法抛出一个（未检查的）异常，它将通过此方法传播。
     * <p>
     * 如果安装了安全管理器，并且指定的 {@code AccessControlContext} 不是由系统代码创建的，且调用者的 {@code ProtectionDomain} 没有被授予 {@literal "createAccessControlContext"}
     * {@link java.security.SecurityPermission}，那么该操作将不带任何权限执行。
     *
     * @param <T> PrivilegedAction 的 {@code run} 方法返回的值的类型。
     * @param action 要执行的操作。
     * @param context 一个 <i>访问控制上下文</i>，表示在执行指定操作之前对调用者域的权限应用的限制。如果上下文为
     *                {@code null}，
     *                则不应用额外的限制。
     * @param perms 限制调用者权限范围的 {@code Permission} 参数。参数数量是可变的。
     *
     * @return 操作的 {@code run} 方法返回的值。
     *
     * @throws NullPointerException 如果 action 或 perms 或 perms 的任何元素为 {@code null}
     *
     * @see #doPrivileged(PrivilegedAction)
     * @see #doPrivileged(PrivilegedExceptionAction,AccessControlContext)
     *
     * @since 1.8
     */
    @CallerSensitive
    public static <T> T doPrivileged(PrivilegedAction<T> action,
        AccessControlContext context, Permission... perms) {


                    AccessControlContext parent = getContext();
        if (perms == null) {
            throw new NullPointerException("null permissions parameter");
        }
        Class <?> caller = Reflection.getCallerClass();
        DomainCombiner dc = (context == null) ? null : context.getCombiner();
        return AccessController.doPrivileged(action, createWrapper(dc,
            caller, parent, context, perms));
    }


    /**
     * 执行指定的 {@code PrivilegedAction}，启用权限并受指定的
     * {@code AccessControlContext} 限制，权限范围由指定的 {@code Permission} 参数限制。
     *
     * 该操作在调用者的保护域所拥有的权限与指定的
     * {@code AccessControlContext} 所代表的域所拥有的权限的交集中执行。
     * <p>
     * 如果操作的 {@code run} 方法抛出（未检查的）异常，它将传播到此方法。
     *
     * <p> 在执行操作时，此方法保留当前 AccessControlContext 的 DomainCombiner（可以为 null）。
     * <p>
     * 如果安装了安全管理器，并且指定的 {@code AccessControlContext} 不是由系统代码创建的，并且调用者的
     * {@code ProtectionDomain} 没有被授予 {@literal "createAccessControlContext"}
     * {@link java.security.SecurityPermission}，那么操作将没有任何权限执行。
     *
     * @param <T> 由 PrivilegedAction 的 {@code run} 方法返回的值的类型。
     * @param action 要执行的操作。
     * @param context 一个 <i>访问控制上下文</i>，表示在执行指定操作之前对调用者域的权限应用的限制。如果上下文为
     *                {@code null}，则不应用额外的限制。
     * @param perms 限制调用者权限范围的 {@code Permission} 参数。参数数量是可变的。
     *
     * @return 操作的 {@code run} 方法返回的值。
     *
     * @throws NullPointerException 如果 action 或 perms 或 perms 的任何元素为 {@code null}
     *
     * @see #doPrivileged(PrivilegedAction)
     * @see #doPrivileged(PrivilegedExceptionAction,AccessControlContext)
     * @see java.security.DomainCombiner
     *
     * @since 1.8
     */
    @CallerSensitive
    public static <T> T doPrivilegedWithCombiner(PrivilegedAction<T> action,
        AccessControlContext context, Permission... perms) {

        AccessControlContext parent = getContext();
        DomainCombiner dc = parent.getCombiner();
        if (dc == null && context != null) {
            dc = context.getCombiner();
        }
        if (perms == null) {
            throw new NullPointerException("null permissions parameter");
        }
        Class <?> caller = Reflection.getCallerClass();
        return AccessController.doPrivileged(action, createWrapper(dc, caller,
            parent, context, perms));
    }

    /**
     * 执行指定的 {@code PrivilegedExceptionAction}，启用权限。该操作使用调用者保护域所拥有的 <i>所有</i> 权限执行。
     *
     * <p> 如果操作的 {@code run} 方法抛出 <i>未检查的</i> 异常，它将传播到此方法。
     *
     * <p> 注意，在执行操作时，与当前 AccessControlContext 关联的任何 DomainCombiner 都将被忽略。
     *
     * @param <T> 由 PrivilegedExceptionAction 的 {@code run} 方法返回的值的类型。
     *
     * @param action 要执行的操作
     *
     * @return 操作的 {@code run} 方法返回的值
     *
     * @exception PrivilegedActionException 如果指定的操作的 {@code run} 方法抛出了 <i>检查的</i> 异常
     * @exception NullPointerException 如果操作为 {@code null}
     *
     * @see #doPrivileged(PrivilegedAction)
     * @see #doPrivileged(PrivilegedExceptionAction,AccessControlContext)
     * @see #doPrivilegedWithCombiner(PrivilegedExceptionAction)
     * @see java.security.DomainCombiner
     */
    @CallerSensitive
    public static native <T> T
        doPrivileged(PrivilegedExceptionAction<T> action)
        throws PrivilegedActionException;


    /**
     * 执行指定的 {@code PrivilegedExceptionAction}，启用权限。该操作使用调用者保护域所拥有的 <i>所有</i> 权限执行。
     *
     * <p> 如果操作的 {@code run} 方法抛出 <i>未检查的</i> 异常，它将传播到此方法。
     *
     * <p> 在执行操作时，此方法保留当前 AccessControlContext 的 DomainCombiner（可以为 null）。
     *
     * @param <T> 由 PrivilegedExceptionAction 的 {@code run} 方法返回的值的类型。
     *
     * @param action 要执行的操作。
     *
     * @return 操作的 {@code run} 方法返回的值
     *
     * @exception PrivilegedActionException 如果指定的操作的 {@code run} 方法抛出了 <i>检查的</i> 异常
     * @exception NullPointerException 如果操作为 {@code null}
     *
     * @see #doPrivileged(PrivilegedAction)
     * @see #doPrivileged(PrivilegedExceptionAction,AccessControlContext)
     * @see java.security.DomainCombiner
     *
     * @since 1.6
     */
    @CallerSensitive
    public static <T> T doPrivilegedWithCombiner(PrivilegedExceptionAction<T> action)
        throws PrivilegedActionException
    {
        AccessControlContext acc = getStackAccessControlContext();
        if (acc == null) {
            return AccessController.doPrivileged(action);
        }
        DomainCombiner dc = acc.getAssignedCombiner();
        return AccessController.doPrivileged(action,
                                             preserveCombiner(dc, Reflection.getCallerClass()));
    }


                /**
     * 在 doPrivileged 调用中保留组合器
     */
    private static AccessControlContext preserveCombiner(DomainCombiner combiner,
                                                         Class<?> caller)
    {
        return createWrapper(combiner, caller, null, null, null);
    }

    /**
     * 创建一个包装器来包含有限的特权范围数据。
     */
    private static AccessControlContext
        createWrapper(DomainCombiner combiner, Class<?> caller,
                      AccessControlContext parent, AccessControlContext context,
                      Permission[] perms)
    {
        ProtectionDomain callerPD = getCallerPD(caller);
        // 检查调用者是否有权限创建上下文
        if (context != null && !context.isAuthorized() &&
            System.getSecurityManager() != null &&
            !callerPD.impliesCreateAccessControlContext())
        {
            ProtectionDomain nullPD = new ProtectionDomain(null, null);
            return new AccessControlContext(new ProtectionDomain[] { nullPD });
        } else {
            return new AccessControlContext(callerPD, combiner, parent,
                                            context, perms);
        }
    }

    private static ProtectionDomain getCallerPD(final Class <?> caller) {
        ProtectionDomain callerPd = doPrivileged
            (new PrivilegedAction<ProtectionDomain>() {
            public ProtectionDomain run() {
                return caller.getProtectionDomain();
            }
        });

        return callerPd;
    }

    /**
     * 使用指定的 {@code AccessControlContext} 限制的特权执行指定的 {@code PrivilegedExceptionAction}。
     * 动作是在调用者的保护域所拥有的权限与指定的 {@code AccessControlContext} 所代表的域所拥有的权限的交集中执行的。
     * <p>
     * 如果动作的 {@code run} 方法抛出一个 <i>未检查的</i> 异常，它将通过此方法传播。
     * <p>
     * 如果安装了安全管理器，并且指定的 {@code AccessControlContext} 不是由系统代码创建的，且调用者的 {@code ProtectionDomain} 没有被授予 {@literal "createAccessControlContext"}
     * {@link java.security.SecurityPermission}，那么动作将在没有权限的情况下执行。
     *
     * @param <T> 由 PrivilegedExceptionAction 的 {@code run} 方法返回的值的类型。
     * @param action 要执行的动作
     * @param context 一个 <i>访问控制上下文</i>，表示在执行指定动作之前应用于调用者域的权限的限制。如果上下文为 {@code null}，则不应用额外的限制。
     *
     * @return 动作的 {@code run} 方法返回的值
     *
     * @exception PrivilegedActionException 如果指定的动作的 {@code run} 方法抛出了一个 <i>检查的</i> 异常
     * @exception NullPointerException 如果动作为 {@code null}
     *
     * @see #doPrivileged(PrivilegedAction)
     * @see #doPrivileged(PrivilegedAction,AccessControlContext)
     */
    @CallerSensitive
    public static native <T> T
        doPrivileged(PrivilegedExceptionAction<T> action,
                     AccessControlContext context)
        throws PrivilegedActionException;


    /**
     * 使用指定的 {@code AccessControlContext} 限制的特权和由指定的 {@code Permission} 参数限制的特权范围执行指定的 {@code PrivilegedExceptionAction}。
     *
     * 动作是在调用者的保护域所拥有的权限与指定的 {@code AccessControlContext} 所代表的域所拥有的权限的交集中执行的。
     * <p>
     * 如果动作的 {@code run} 方法抛出一个 (未检查的) 异常，它将通过此方法传播。
     * <p>
     * 如果安装了安全管理器，并且指定的 {@code AccessControlContext} 不是由系统代码创建的，且调用者的 {@code ProtectionDomain} 没有被授予 {@literal "createAccessControlContext"}
     * {@link java.security.SecurityPermission}，那么动作将在没有权限的情况下执行。
     *
     * @param <T> 由 PrivilegedExceptionAction 的 {@code run} 方法返回的值的类型。
     * @param action 要执行的动作。
     * @param context 一个 <i>访问控制上下文</i>，表示在执行指定动作之前应用于调用者域的权限的限制。如果上下文为 {@code null}，
     *                则不应用额外的限制。
     * @param perms 限制调用者权限范围的 {@code Permission} 参数。参数数量是可变的。
     *
     * @return 动作的 {@code run} 方法返回的值。
     *
     * @throws PrivilegedActionException 如果指定的动作的 {@code run} 方法抛出了一个 <i>检查的</i> 异常
     * @throws NullPointerException 如果动作或 perms 或 perms 的任何元素为 {@code null}
     *
     * @see #doPrivileged(PrivilegedAction)
     * @see #doPrivileged(PrivilegedAction,AccessControlContext)
     *
     * @since 1.8
     */
    @CallerSensitive
    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action,
                                     AccessControlContext context, Permission... perms)
        throws PrivilegedActionException
    {
        AccessControlContext parent = getContext();
        if (perms == null) {
            throw new NullPointerException("null permissions parameter");
        }
        Class <?> caller = Reflection.getCallerClass();
        DomainCombiner dc = (context == null) ? null : context.getCombiner();
        return AccessController.doPrivileged(action, createWrapper(dc, caller, parent, context, perms));
    }

    /**
     * 使用指定的 {@code PrivilegedExceptionAction} 执行操作，启用并由指定的
     * {@code AccessControlContext} 限制权限，并且权限范围由指定的 {@code Permission} 参数限制。
     *
     * 该操作的执行权限是调用者保护域所拥有的权限与指定的
     * {@code AccessControlContext} 所代表的域所拥有的权限的交集。
     * <p>
     * 如果操作的 {@code run} 方法抛出一个（未检查的）异常，它将传播到此方法。
     *
     * <p> 该方法在执行操作时保留当前 AccessControlContext 的 DomainCombiner（可以为 null）。
     * <p>
     * 如果安装了安全管理器，并且指定的 {@code AccessControlContext} 不是由系统代码创建的，且调用者的
     * {@code ProtectionDomain} 没有被授予 {@literal "createAccessControlContext"}
     * {@link java.security.SecurityPermission}，那么操作将没有权限执行。
     *
     * @param <T> PrivilegedExceptionAction 的 {@code run} 方法返回的值的类型。
     * @param action 要执行的操作。
     * @param context 一个 <i>访问控制上下文</i>，表示在执行指定操作之前对调用者域的权限应用的限制。如果上下文为
     *                {@code null}，
     *                则不应用额外的限制。
     * @param perms 限制调用者权限范围的 {@code Permission} 参数。参数的数量是可变的。
     *
     * @return 操作的 {@code run} 方法返回的值。
     *
     * @throws PrivilegedActionException 如果指定的操作的
     *         {@code run} 方法抛出了一个 <i>检查</i> 异常
     * @throws NullPointerException 如果 action 或 perms 或 perms 的任何元素为
     *         {@code null}
     *
     * @see #doPrivileged(PrivilegedAction)
     * @see #doPrivileged(PrivilegedAction,AccessControlContext)
     * @see java.security.DomainCombiner
     *
     * @since 1.8
     */
    @CallerSensitive
    public static <T> T doPrivilegedWithCombiner(PrivilegedExceptionAction<T> action,
                                                 AccessControlContext context,
                                                 Permission... perms)
        throws PrivilegedActionException
    {
        AccessControlContext parent = getContext();
        DomainCombiner dc = parent.getCombiner();
        if (dc == null && context != null) {
            dc = context.getCombiner();
        }
        if (perms == null) {
            throw new NullPointerException("null permissions parameter");
        }
        Class <?> caller = Reflection.getCallerClass();
        return AccessController.doPrivileged(action, createWrapper(dc, caller,
            parent, context, perms));
    }

    /**
     * 返回 AccessControl 上下文。即，它获取堆栈上所有调用者的保护域，
     * 从第一个具有非空 ProtectionDomain 的类开始。
     *
     * @return 基于当前堆栈的访问控制上下文，或者如果只有特权系统代码，则为 null。
     */

    private static native AccessControlContext getStackAccessControlContext();


    /**
     * 返回“继承的”AccessControl 上下文。这是线程创建时存在的上下文。包私有，以便
     * AccessControlContext 可以使用它。
     */

    static native AccessControlContext getInheritedAccessControlContext();

    /**
     * 此方法对当前调用上下文进行“快照”，包括当前线程继承的 AccessControlContext 和任何
     * 有限的权限范围，并将其放入 AccessControlContext 对象中。此上下文随后可以在其他线程中进行检查。
     *
     * @see AccessControlContext
     *
     * @return 基于当前上下文的 AccessControlContext。
     */

    public static AccessControlContext getContext()
    {
        AccessControlContext acc = getStackAccessControlContext();
        if (acc == null) {
            // 我们只有特权系统代码。我们不希望返回 null，所以我们构造一个真实的 ACC。
            return new AccessControlContext(null, true);
        } else {
            return acc.optimize();
        }
    }

    /**
     * 根据当前 AccessControlContext 和安全策略确定是否应允许或拒绝
     * 指定权限的访问请求。如果访问请求被允许，此方法将静默返回，否则将抛出 AccessControlException。AccessControlException
     * 的 getPermission 方法返回 {@code perm} Permission 对象实例。
     *
     * @param perm 请求的权限。
     *
     * @exception AccessControlException 如果根据当前安全策略不允许指定的权限。
     * @exception NullPointerException 如果指定的权限为 {@code null}，并且根据
     *            当前生效的安全策略进行检查。
     */

    public static void checkPermission(Permission perm)
        throws AccessControlException
    {
        //System.err.println("checkPermission "+perm);
        //Thread.currentThread().dumpStack();

        if (perm == null) {
            throw new NullPointerException("permission can't be null");
        }

        AccessControlContext stack = getStackAccessControlContext();
        // 如果上下文为 null，堆栈上有特权系统代码。
        if (stack == null) {
            Debug debug = AccessControlContext.getDebug();
            boolean dumpDebug = false;
            if (debug != null) {
                dumpDebug = !Debug.isOn("codebase=");
                dumpDebug &= !Debug.isOn("permission=") ||
                    Debug.isOn("permission=" + perm.getClass().getCanonicalName());
            }


                        if (dumpDebug && Debug.isOn("stack")) {
                Thread.dumpStack();
            }

            if (dumpDebug && Debug.isOn("domain")) {
                debug.println("domain (context is null)");
            }

            if (dumpDebug) {
                debug.println("access allowed "+perm);
            }
            return;
        }

        AccessControlContext acc = stack.optimize();
        acc.checkPermission(perm);
    }
}
