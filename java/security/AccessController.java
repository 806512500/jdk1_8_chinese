
/*
 * Copyright (c) 1997, 2019, Oracle and/or its affiliates. All rights reserved.
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
 * <li>标记代码为“特权”，从而影响后续的访问决策，
 * <li>获取当前调用上下文的“快照”，以便从不同的上下文进行访问控制决策时，可以基于保存的上下文进行决策。 </ul>
 *
 * <p> {@link #checkPermission(Permission) checkPermission} 方法确定是否应授予指定权限表示的访问请求。下面是一个示例。在这个例子中，{@code checkPermission} 将确定是否授予“/temp”目录中名为“testFile”的文件的“读”访问权限。
 *
 * <pre>
 *
 * FilePermission perm = new FilePermission("/temp/testFile", "read");
 * AccessController.checkPermission(perm);
 *
 * </pre>
 *
 * <p> 如果请求的访问被允许，{@code checkPermission} 会静默返回。如果被拒绝，则会抛出 AccessControlException。
 * AccessControlException 也可能在请求的权限类型不正确或包含无效值时抛出。尽可能提供此类信息。
 *
 * 假设当前线程遍历了 m 个调用者，顺序为调用者 1 到调用者 2 到调用者 m。然后调用者 m 调用了 {@code checkPermission} 方法。
 * {@code checkPermission} 方法基于以下算法确定是否授予访问权限：
 *
 *  <pre> {@code
 * for (int i = m; i > 0; i--) {
 *
 *     if (调用者 i 的域没有该权限)
 *         抛出 AccessControlException
 *
 *     else if (调用者 i 被标记为特权) {
 *         if (在 doPrivileged 调用中指定了上下文)
 *             context.checkPermission(permission)
 *         if (在 doPrivileged 调用中指定了有限权限) {
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
 * // 每当创建新线程时，当时的 AccessControlContext 会被存储并关联到新线程，作为“继承”的上下文。
 *
 * inheritedContext.checkPermission(permission);
 * }</pre>
 *
 * <p> 调用者可以被标记为“特权”（参见 {@link #doPrivileged(PrivilegedAction) doPrivileged} 和下文）。
 * 在进行访问控制决策时，如果 {@code checkPermission} 方法到达一个通过没有上下文参数的 {@code doPrivileged} 调用标记为“特权”的调用者，它将停止检查。如果该调用者的域具有指定的权限，并且至少有一个限制权限参数（如果有）隐含请求的权限，则不再进行进一步的检查，{@code checkPermission} 静默返回，表示请求的访问被允许。
 * 如果该域没有指定的权限，则会像往常一样抛出异常。如果调用者的域具有指定的权限，但没有被 doPrivileged 调用中的任何限制权限参数隐含，则权限检查将继续，直到没有更多的调用者或另一个 {@code doPrivileged} 调用匹配请求的权限并正常返回。
 *
 * <p> “特权”功能的正常使用如下。如果你不需要从“特权”块中返回值，可以这样做：
 *
 *  <pre> {@code
 * somemethod() {
 *     ...正常代码...
 *     AccessController.doPrivileged(new PrivilegedAction<Void>() {
 *         public Void run() {
 *             // 特权代码放在这里，例如：
 *             System.loadLibrary("awt");
 *             return null; // 没有需要返回的内容
 *         }
 *     });
 *     ...正常代码...
 * }}</pre>
 *
 * <p>
 * PrivilegedAction 是一个只有一个方法的接口，名为 {@code run}。
 * 上面的例子展示了该接口的实现的创建；提供了 {@code run} 方法的具体实现。
 * 当调用 {@code doPrivileged} 时，会传递一个 PrivilegedAction 实现的实例。
 * {@code doPrivileged} 方法在启用特权后调用 PrivilegedAction 实现的 {@code run} 方法，并将 {@code run} 方法的返回值作为 {@code doPrivileged} 的返回值（在这个例子中被忽略）。
 *
 * <p> 如果你需要返回一个值，可以这样做：
 *
 *  <pre> {@code
 * somemethod() {
 *     ...正常代码...
 *     String user = AccessController.doPrivileged(
 *         new PrivilegedAction<String>() {
 *         public String run() {
 *             return System.getProperty("user.name");
 *             }
 *         });
 *     ...正常代码...
 * }}</pre>
 *
 * <p> 如果你的 {@code run} 方法中执行的操作可能抛出“检查”异常（即在方法的 {@code throws} 子句中列出的异常），则需要使用 {@code PrivilegedExceptionAction} 接口而不是 {@code PrivilegedAction} 接口：
 *
 *  <pre> {@code
 * somemethod() throws FileNotFoundException {
 *     ...正常代码...
 *     try {
 *         FileInputStream fis = AccessController.doPrivileged(
 *         new PrivilegedExceptionAction<FileInputStream>() {
 *             public FileInputStream run() throws FileNotFoundException {
 *                 return new FileInputStream("someFile");
 *             }
 *         });
 *     } catch (PrivilegedActionException e) {
 *         // e.getException() 应该是一个 FileNotFoundException 的实例，
 *         // 因为只有“检查”异常会被包装在 PrivilegedActionException 中。
 *         throw (FileNotFoundException) e.getException();
 *     }
 *     ...正常代码...
 *  }}</pre>
 *
 * <p> 在使用“特权”构造时要非常小心，并始终记住使特权代码部分尽可能小。
 * 你可以传递 {@code Permission} 参数以进一步限制“特权”的范围（参见下文）。
 *
 *
 * <p> 注意，{@code checkPermission} 始终在当前执行线程的上下文中执行安全检查。
 * 有时需要在一个给定上下文中进行的安全检查实际上需要从一个 <i>不同的</i> 上下文（例如，从一个工作线程）中完成。
 * {@link #getContext() getContext} 方法和 AccessControlContext 类为此情况提供支持。
 * {@code getContext} 方法对当前调用上下文进行“快照”，并将其放入 AccessControlContext 对象中，然后返回该对象。一个示例如下：
 *
 * <pre>
 *
 * AccessControlContext acc = AccessController.getContext()
 *
 * </pre>
 *
 * <p>
 * AccessControlContext 本身也有一个 {@code checkPermission} 方法，该方法基于它封装的上下文进行访问决策，而不是基于当前执行线程的上下文。
 * 因此，不同上下文中的代码可以调用该方法，对之前保存的 AccessControlContext 对象进行调用。一个示例如下：
 *
 * <pre>
 *
 * acc.checkPermission(permission)
 *
 * </pre>
 *
 * <p> 有时你事先不知道要检查哪些权限。在这种情况下，可以使用带有上下文的 doPrivileged 方法。你还可以通过传递额外的 {@code Permission} 参数来限制特权代码的范围。
 *
 *  <pre> {@code
 * somemethod() {
 *     AccessController.doPrivileged(new PrivilegedAction<Object>() {
 *         public Object run() {
 *             // 代码放在这里。run 方法中的任何权限检查都需要调用者的保护域和快照上下文的交集具有所需的权限。如果请求的权限没有被限制的 FilePermission 参数隐含，则线程的检查将继续超过 doPrivileged 的调用者。
 *         }
 *     }, acc, new FilePermission("/temp/*", "read"));
 *     ...正常代码...
 * }}</pre>
 * <p> 传递一个限制的 {@code Permission} 参数的实例为 {@code AllPermission} 等同于调用没有限制的 {@code Permission} 参数的等效 {@code doPrivileged} 方法。传递一个零长度的 {@code Permission} 数组将禁用代码特权，因此检查总是会超过该 {@code doPrivileged} 方法的调用者。
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
     * 以启用特权的方式执行指定的 {@code PrivilegedAction}。该操作将在调用者的保护域所拥有的 <i>所有</i> 权限下执行。
     *
     * <p> 如果操作的 {@code run} 方法抛出一个（未检查的）异常，它将通过此方法传播。
     *
     * <p> 注意，执行操作时，当前 AccessControlContext 关联的任何 DomainCombiner 将被忽略。
     *
     * @param <T> PrivilegedAction 的 {@code run} 方法返回值的类型。
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
     * 以启用特权的方式执行指定的 {@code PrivilegedAction}。该操作将在调用者的保护域所拥有的 <i>所有</i> 权限下执行。
     *
     * <p> 如果操作的 {@code run} 方法抛出一个（未检查的）异常，它将通过此方法传播。
     *
     * <p> 该方法在执行操作时保留当前 AccessControlContext 的 DomainCombiner（可能为 null）。
     *
     * @param <T> PrivilegedAction 的 {@code run} 方法返回值的类型。
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
     * 以启用特权的方式执行指定的 {@code PrivilegedAction}，并由指定的 {@code AccessControlContext} 限制。该操作将在调用者的保护域所拥有的权限和指定的 {@code AccessControlContext} 所代表的域所拥有的权限的交集中执行。
     * <p>
     * 如果操作的 {@code run} 方法抛出一个（未检查的）异常，它将通过此方法传播。
     * <p>
     * 如果安装了安全经理，并且指定的 {@code AccessControlContext} 不是由系统代码创建的，且调用者的 {@code ProtectionDomain} 没有被授予 {@literal "createAccessControlContext"}
     * {@link java.security.SecurityPermission}，则操作将在没有任何权限的情况下执行。
     *
     * @param <T> PrivilegedAction 的 {@code run} 方法返回值的类型。
     * @param action 要执行的操作。
     * @param context 一个 <i>访问控制上下文</i>，表示在执行指定操作之前对调用者域的权限应用的限制。如果上下文为 {@code null}，则不应用额外的限制。
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
     * 以启用特权的方式执行指定的 {@code PrivilegedAction}，并由指定的 {@code AccessControlContext} 限制，且特权范围由指定的 {@code Permission} 参数限制。
     *
     * 该操作将在调用者的保护域所拥有的权限和指定的 {@code AccessControlContext} 所代表的域所拥有的权限的交集中执行。
     * <p>
     * 如果操作的 {@code run} 方法抛出一个（未检查的）异常，它将通过此方法传播。
     * <p>
     * 如果安装了安全经理，并且指定的 {@code AccessControlContext} 不是由系统代码创建的，且调用者的 {@code ProtectionDomain} 没有被授予 {@literal "createAccessControlContext"}
     * {@link java.security.SecurityPermission}，则操作将在没有任何权限的情况下执行。
     *
     * @param <T> PrivilegedAction 的 {@code run} 方法返回值的类型。
     * @param action 要执行的操作。
     * @param context 一个 <i>访问控制上下文</i>，表示在执行指定操作之前对调用者域的权限应用的限制。如果上下文为 {@code null}，则不应用额外的限制。
     * @param perms 限制调用者权限范围的 {@code Permission} 参数。参数数量是可变的。
     *
     * @return 操作的 {@code run} 方法返回的值。
     *
     * @throws NullPointerException 如果 action 或 perms 或 perms 中的任何元素为 {@code null}
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
     * 执行指定的 {@code PrivilegedAction}，启用特权并受指定的
     * {@code AccessControlContext} 限制，特权范围由指定的
     * {@code Permission} 参数限制。
     *
     * 该操作以调用者保护域和指定的
     * {@code AccessControlContext} 所代表的域所拥有的权限的交集来执行。
     * <p>
     * 如果操作的 {@code run} 方法抛出一个（未检查的）异常，它将通过此方法传播。
     *
     * <p> 此方法在执行操作时保留当前 AccessControlContext 的
     * DomainCombiner（可能为 null）。
     * <p>
     * 如果安装了安全管理器，并且指定的
     * {@code AccessControlContext} 不是由系统代码创建的，且调用者的
     * {@code ProtectionDomain} 没有被授予
     * {@literal "createAccessControlContext"}
     * {@link java.security.SecurityPermission}，则操作将不带任何权限执行。
     *
     * @param <T> 由 PrivilegedAction 的 {@code run} 方法返回的值的类型。
     * @param action 要执行的操作。
     * @param context 一个 <i>访问控制上下文</i>，表示在执行指定操作之前要应用于调用者域的特权的限制。如果上下文为
     *                {@code null}，则不应用额外的限制。
     * @param perms 限制调用者特权范围的 {@code Permission} 参数。参数的数量是可变的。
     *
     * @return 操作的 {@code run} 方法返回的值。
     *
     * @throws NullPointerException 如果 action 或 perms 或 perms 的任何元素为
     *         {@code null}
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
     * 执行指定的 {@code PrivilegedExceptionAction}，启用特权。该操作以调用者保护域所拥有的 <i>所有</i> 权限来执行。
     *
     * <p> 如果操作的 {@code run} 方法抛出一个 <i>未检查的</i>
     * 异常，它将通过此方法传播。
     *
     * <p> 注意，在执行操作时，与当前 AccessControlContext 关联的任何 DomainCombiner 将被忽略。
     *
     * @param <T> 由 PrivilegedExceptionAction 的 {@code run} 方法返回的值的类型。
     *
     * @param action 要执行的操作
     *
     * @return 操作的 {@code run} 方法返回的值
     *
     * @exception PrivilegedActionException 如果指定的操作的
     *         {@code run} 方法抛出了一个 <i>检查的</i> 异常
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
     * 执行指定的 {@code PrivilegedExceptionAction}，启用特权。该操作以调用者保护域所拥有的 <i>所有</i> 权限来执行。
     *
     * <p> 如果操作的 {@code run} 方法抛出一个 <i>未检查的</i>
     * 异常，它将通过此方法传播。
     *
     * <p> 此方法在执行操作时保留当前 AccessControlContext 的
     * DomainCombiner（可能为 null）。
     *
     * @param <T> 由 PrivilegedExceptionAction 的 {@code run} 方法返回的值的类型。
     *
     * @param action 要执行的操作。
     *
     * @return 操作的 {@code run} 方法返回的值
     *
     * @exception PrivilegedActionException 如果指定的操作的
     *         {@code run} 方法抛出了一个 <i>检查的</i> 异常
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
     * 创建一个包含有限特权范围数据的包装器。
     */
    private static AccessControlContext
        createWrapper(DomainCombiner combiner, Class<?> caller,
                      AccessControlContext parent, AccessControlContext context,
                      Permission[] perms)
    {
        ProtectionDomain callerPD = getCallerPD(caller);
        // 检查调用者是否有权创建上下文
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
     * 执行指定的 {@code PrivilegedExceptionAction}，启用特权并受指定的
     * {@code AccessControlContext} 限制。该操作以调用者保护域所拥有的权限和指定的
     * {@code AccessControlContext} 所代表的域所拥有的权限的交集来执行。
     * <p>
     * 如果操作的 {@code run} 方法抛出一个 <i>未检查的</i>
     * 异常，它将通过此方法传播。
     * <p>
     * 如果安装了安全管理器，并且指定的
     * {@code AccessControlContext} 不是由系统代码创建的，且调用者的
     * {@code ProtectionDomain} 没有被授予
     * {@literal "createAccessControlContext"}
     * {@link java.security.SecurityPermission}，则操作将不带任何权限执行。
     *
     * @param <T> 由 PrivilegedExceptionAction 的 {@code run} 方法返回的值的类型。
     * @param action 要执行的操作
     * @param context 一个 <i>访问控制上下文</i>，表示在执行指定操作之前要应用于调用者域的特权的限制。如果上下文为
     *                {@code null}，则不应用额外的限制。
     *
     * @return 操作的 {@code run} 方法返回的值
     *
     * @exception PrivilegedActionException 如果指定的操作的
     *         {@code run} 方法抛出了一个 <i>检查的</i> 异常
     * @exception NullPointerException 如果操作为 {@code null}
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
     * 执行指定的 {@code PrivilegedExceptionAction}，启用特权并受指定的
     * {@code AccessControlContext} 限制，特权范围由指定的
     * {@code Permission} 参数限制。
     *
     * 该操作以调用者保护域和指定的
     * {@code AccessControlContext} 所代表的域所拥有的权限的交集来执行。
     * <p>
     * 如果操作的 {@code run} 方法抛出一个（未检查的）异常，它将通过此方法传播。
     * <p>
     * 如果安装了安全管理器，并且指定的
     * {@code AccessControlContext} 不是由系统代码创建的，且调用者的
     * {@code ProtectionDomain} 没有被授予
     * {@literal "createAccessControlContext"}
     * {@link java.security.SecurityPermission}，则操作将不带任何权限执行。
     *
     * @param <T> 由 PrivilegedExceptionAction 的 {@code run} 方法返回的值的类型。
     * @param action 要执行的操作。
     * @param context 一个 <i>访问控制上下文</i>，表示在执行指定操作之前要应用于调用者域的特权的限制。如果上下文为
     *                {@code null}，
     *                则不应用额外的限制。
     * @param perms 限制调用者特权范围的 {@code Permission} 参数。参数的数量是可变的。
     *
     * @return 操作的 {@code run} 方法返回的值。
     *
     * @throws PrivilegedActionException 如果指定的操作的
     *         {@code run} 方法抛出了一个 <i>检查的</i> 异常
     * @throws NullPointerException 如果 action 或 perms 或 perms 的任何元素为
     *         {@code null}
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
     * 执行指定的 {@code PrivilegedExceptionAction}，启用特权并受指定的
     * {@code AccessControlContext} 限制，特权范围由指定的
     * {@code Permission} 参数限制。
     *
     * 该操作以调用者保护域和指定的
     * {@code AccessControlContext} 所代表的域所拥有的权限的交集来执行。
     * <p>
     * 如果操作的 {@code run} 方法抛出一个（未检查的）异常，它将通过此方法传播。
     *
     * <p> 此方法在执行操作时保留当前 AccessControlContext 的
     * DomainCombiner（可能为 null）。
     * <p>
     * 如果安装了安全管理器，并且指定的
     * {@code AccessControlContext} 不是由系统代码创建的，且调用者的
     * {@code ProtectionDomain} 没有被授予
     * {@literal "createAccessControlContext"}
     * {@link java.security.SecurityPermission}，则操作将不带任何权限执行。
     *
     * @param <T> 由 PrivilegedExceptionAction 的 {@code run} 方法返回的值的类型。
     * @param action 要执行的操作。
     * @param context 一个 <i>访问控制上下文</i>，表示在执行指定操作之前要应用于调用者域的特权的限制。如果上下文为
     *                {@code null}，
     *                则不应用额外的限制。
     * @param perms 限制调用者特权范围的 {@code Permission} 参数。参数的数量是可变的。
     *
     * @return 操作的 {@code run} 方法返回的值。
     *
     * @throws PrivilegedActionException 如果指定的操作的
     *         {@code run} 方法抛出了一个 <i>检查的</i> 异常
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
     * 返回访问控制上下文。即，它获取堆栈上所有调用者的保护域，
     * 从堆栈中第一个具有非空保护域的类开始。
     *
     * @return 基于当前堆栈的访问控制上下文，如果没有非特权系统代码则返回 null。
     */

    private static native AccessControlContext getStackAccessControlContext();


    /**
     * 返回“继承的”访问控制上下文。这是线程创建时存在的上下文。包私有，以便
     * AccessControlContext 可以使用它。
     */

    static native AccessControlContext getInheritedAccessControlContext();

    /**
     * 此方法对当前调用上下文进行“快照”，包括当前线程继承的 AccessControlContext 和任何
     * 有限的特权范围，并将其放入 AccessControlContext 对象中。此上下文可以在稍后的时间点进行检查，可能在另一个线程中。
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
     * 根据当前 AccessControlContext 和安全策略，确定是否应允许或拒绝
     * 指定权限的访问请求。如果访问请求被允许，此方法将静默返回；否则，将抛出 AccessControlException。AccessControlException 的
     * getPermission 方法返回 {@code perm} Permission 对象实例。
     *
     * @param perm 请求的权限。
     *
     * @exception AccessControlException 如果根据当前安全策略，指定的权限未被允许。
     * @exception NullPointerException 如果指定的权限为 {@code null}，并且根据当前生效的安全策略进行检查。
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
