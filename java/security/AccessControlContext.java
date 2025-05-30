
/*
 * Copyright (c) 1997, 2015, Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;
import sun.security.util.Debug;
import sun.security.util.SecurityConstants;


/**
 * AccessControlContext 用于根据其封装的上下文做出系统资源访问决策。
 *
 * <p>更具体地说，它封装了一个上下文，并且有一个 {@code checkPermission} 方法，
 * 该方法与 AccessController 类中的 {@code checkPermission} 方法等效，但有一个不同点：
 * AccessControlContext 的 {@code checkPermission} 方法基于其封装的上下文做出访问决策，
 * 而不是当前执行线程的上下文。
 *
 * <p>因此，AccessControlContext 的目的是在需要在给定上下文中进行安全检查的情况下，
 * 实际上需要从 <i>不同</i> 的上下文中进行（例如，从工作线程中）。
 *
 * <p> AccessControlContext 是通过调用 {@code AccessController.getContext} 方法创建的。
 * {@code getContext} 方法对当前调用上下文进行“快照”，并将其放入 AccessControlContext 对象中，然后返回该对象。一个示例调用如下：
 *
 * <pre>
 *   AccessControlContext acc = AccessController.getContext()
 * </pre>
 *
 * <p>
 * 在不同上下文中的代码可以随后调用之前保存的 AccessControlContext 对象的
 * {@code checkPermission} 方法。一个示例调用如下：
 *
 * <pre>
 *   acc.checkPermission(permission)
 * </pre>
 *
 * @see AccessController
 *
 * @author Roland Schemers
 */

public final class AccessControlContext {

    private ProtectionDomain context[];
    // isPrivileged 和 isAuthorized 由虚拟机引用 - 不要删除或更改它们的名称
    private boolean isPrivileged;
    private boolean isAuthorized = false;

    // 注意：此字段由虚拟机的本机代码直接使用。不要更改它。
    private AccessControlContext privilegedContext;

    private DomainCombiner combiner = null;

    // 有限特权范围
    private Permission permissions[];
    private AccessControlContext parent;
    private boolean isWrapped;

    // 是否受有限特权范围的限制？
    private boolean isLimited;
    private ProtectionDomain limitedContext[];

    private static boolean debugInit = false;
    private static Debug debug = null;

    static Debug getDebug()
    {
        if (debugInit)
            return debug;
        else {
            if (Policy.isSet()) {
                debug = Debug.getInstance("access");
                debugInit = true;
            }
            return debug;
        }
    }

    /**
     * 使用给定的 ProtectionDomains 数组创建 AccessControlContext。
     * context 不得为 null。重复的域将从上下文中移除。
     *
     * @param context 与此上下文关联的 ProtectionDomains。
     * 非重复的域将从数组中复制。对数组的后续更改不会影响此 AccessControlContext。
     * @throws NullPointerException 如果 {@code context} 为 {@code null}
     */
    public AccessControlContext(ProtectionDomain context[])
    {
        if (context.length == 0) {
            this.context = null;
        } else if (context.length == 1) {
            if (context[0] != null) {
                this.context = context.clone();
            } else {
                this.context = null;
            }
        } else {
            List<ProtectionDomain> v = new ArrayList<>(context.length);
            for (int i =0; i< context.length; i++) {
                if ((context[i] != null) &&  (!v.contains(context[i])))
                    v.add(context[i]);
            }
            if (!v.isEmpty()) {
                this.context = new ProtectionDomain[v.size()];
                this.context = v.toArray(this.context);
            }
        }
    }

    /**
     * 使用给定的 AccessControlContext 和 DomainCombiner 创建新的 AccessControlContext。
     * 此构造函数将提供的 DomainCombiner 与提供的 AccessControlContext 关联。
     *
     * <p>
     *
     * @param acc 与提供的 DomainCombiner 关联的 AccessControlContext。
     *
     * @param combiner 要与提供的 AccessControlContext 关联的 DomainCombiner。
     *
     * @exception NullPointerException 如果提供的 context 为 null。
     *
     * @exception SecurityException 如果安装了安全经理且调用者没有 "createAccessControlContext"
     *          {@link SecurityPermission}
     * @since 1.3
     */
    public AccessControlContext(AccessControlContext acc,
                                DomainCombiner combiner) {

        this(acc, combiner, false);
    }

    /**
     * 包私有，允许 ProtectionDomain 在不执行 {@linkplain SecurityConstants.CREATE_ACC_PERMISSION}
     * 权限的安全检查的情况下调用
     */
    AccessControlContext(AccessControlContext acc,
                        DomainCombiner combiner,
                        boolean preauthorized) {
        if (!preauthorized) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(SecurityConstants.CREATE_ACC_PERMISSION);
                this.isAuthorized = true;
            }
        } else {
            this.isAuthorized = true;
        }

        this.context = acc.context;

        // 无需对提供的 ACC 运行 combine 方法。上下文最初检索时已经“组合”过了。
        //
        // 此时，我们只需丢弃旧的 combiner 并使用新提供的 combiner。
        this.combiner = combiner;
    }

    /**
     * 包私有，供 AccessController 使用
     *
     * 此“参数包装器”上下文将作为内部 doPrivileged() 调用实现中的实际上下文参数传递。
     */
    AccessControlContext(ProtectionDomain caller, DomainCombiner combiner,
        AccessControlContext parent, AccessControlContext context,
        Permission[] perms)
    {
        /*
         * 如果必要，将 doPrivileged() 上下文中的域组合到我们的包装器上下文中。
         */
        ProtectionDomain[] callerPDs = null;
        if (caller != null) {
             callerPDs = new ProtectionDomain[] { caller };
        }
        if (context != null) {
            if (combiner != null) {
                this.context = combiner.combine(callerPDs, context.context);
            } else {
                this.context = combine(callerPDs, context.context);
            }
        } else {
            /*
             * 即使似乎没有要组合的内容，也要调用 combiner。
             */
            if (combiner != null) {
                this.context = combiner.combine(callerPDs, null);
            } else {
                this.context = combine(callerPDs, null);
            }
        }
        this.combiner = combiner;

        Permission[] tmp = null;
        if (perms != null) {
            tmp = new Permission[perms.length];
            for (int i=0; i < perms.length; i++) {
                if (perms[i] == null) {
                    throw new NullPointerException("permission can't be null");
                }

                /*
                 * 如果权限参数是 AllPermission，则等同于调用没有限制权限的 doPrivileged()。
                 */
                if (perms[i].getClass() == AllPermission.class) {
                    parent = null;
                }
                tmp[i] = perms[i];
            }
        }

        /*
         * 对于具有有限特权范围的 doPrivileged()，初始化相关字段。
         *
         * limitedContext 字段包含此有限特权范围内的所有域的并集。换句话说，
         * 它包含如果限制权限中的任何一个没有隐含请求的权限，则可能进行检查的所有域。
         */
        if (parent != null) {
            this.limitedContext = combine(parent.context, parent.limitedContext);
            this.isLimited = true;
            this.isWrapped = true;
            this.permissions = tmp;
            this.parent = parent;
            this.privilegedContext = context; // 用于 checkPermission2()
        }
        this.isAuthorized = true;
    }


    /**
     * 包私有构造函数，供 AccessController.getContext() 使用
     */

    AccessControlContext(ProtectionDomain context[],
                         boolean isPrivileged)
    {
        this.context = context;
        this.isPrivileged = isPrivileged;
        this.isAuthorized = true;
    }

    /**
     * 用于 JavaSecurityAccess.doIntersectionPrivilege() 的构造函数
     */
    AccessControlContext(ProtectionDomain[] context,
                         AccessControlContext privilegedContext)
    {
        this.context = context;
        this.privilegedContext = privilegedContext;
        this.isPrivileged = true;
    }

    /**
     * 返回此上下文的上下文。
     */
    ProtectionDomain[] getContext() {
        return context;
    }

    /**
     * 如果此上下文是特权上下文，则返回 true。
     */
    boolean isPrivileged()
    {
        return isPrivileged;
    }

    /**
     * 从特权或继承的上下文中获取分配的 combiner
     */
    DomainCombiner getAssignedCombiner() {
        AccessControlContext acc;
        if (isPrivileged) {
            acc = privilegedContext;
        } else {
            acc = AccessController.getInheritedAccessControlContext();
        }
        if (acc != null) {
            return acc.combiner;
        }
        return null;
    }

    /**
     * 获取与此 AccessControlContext 关联的 DomainCombiner。
     *
     * <p>
     *
     * @return 与此 AccessControlContext 关联的 DomainCombiner，如果没有则返回 null。
     *
     * @exception SecurityException 如果安装了安全经理且调用者没有 "getDomainCombiner"
     *          {@link SecurityPermission}
     * @since 1.3
     */
    public DomainCombiner getDomainCombiner() {

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_COMBINER_PERMISSION);
        }
        return getCombiner();
    }

    /**
     * 包私有，供 AccessController 使用
     */
    DomainCombiner getCombiner() {
        return combiner;
    }

    boolean isAuthorized() {
        return isAuthorized;
    }

    /**
     * 根据当前的安全策略和此对象中的上下文，确定是否应允许或拒绝由指定权限表示的访问请求。
     * 只有当上下文中的每个 ProtectionDomain 都隐含该权限时，请求才被允许。否则，请求被拒绝。
     *
     * <p>
     * 如果访问请求被允许，则此方法静默返回；否则，抛出合适的 AccessControlException。
     *
     * @param perm 请求的权限。
     *
     * @exception AccessControlException 如果根据当前的安全策略和此对象封装的上下文，指定的权限未被允许。
     * @exception NullPointerException 如果要检查的权限为 null。
     */
    public void checkPermission(Permission perm)
        throws AccessControlException
    {
        boolean dumpDebug = false;

        if (perm == null) {
            throw new NullPointerException("permission can't be null");
        }
        if (getDebug() != null) {
            // 如果未指定 "codebase"，则默认转储信息。
            dumpDebug = !Debug.isOn("codebase=");
            if (!dumpDebug) {
                // 如果指定了 "codebase"，则仅在指定的代码值在堆栈中时转储。
                for (int i = 0; context != null && i < context.length; i++) {
                    if (context[i].getCodeSource() != null &&
                        context[i].getCodeSource().getLocation() != null &&
                        Debug.isOn("codebase=" + context[i].getCodeSource().getLocation().toString())) {
                        dumpDebug = true;
                        break;
                    }
                }
            }

            dumpDebug &= !Debug.isOn("permission=") ||
                Debug.isOn("permission=" + perm.getClass().getCanonicalName());

            if (dumpDebug && Debug.isOn("stack")) {
                Thread.dumpStack();
            }

            if (dumpDebug && Debug.isOn("domain")) {
                if (context == null) {
                    debug.println("domain (context is null)");
                } else {
                    for (int i=0; i< context.length; i++) {
                        debug.println("domain "+i+" "+context[i]);
                    }
                }
            }
        }

        /*
         * 遍历上下文中的 ProtectionDomains。
         * 在第一个不允许请求权限的 ProtectionDomain 处停止（抛出异常）。
         *
         */

        /* 如果 ctxt 为 null，则堆栈上只有系统域，
           或第一个域是特权系统域。这是为了使系统代码的常见情况非常快速 */

        if (context == null) {
            checkPermission2(perm);
            return;
        }

        for (int i=0; i< context.length; i++) {
            if (context[i] != null &&  !context[i].implies(perm)) {
                if (dumpDebug) {
                    debug.println("access denied " + perm);
                }


                            if (Debug.isOn("failure") && debug != null) {
                    // 确保在失败时始终显示此信息，
                    // 但如果已显示则不再显示。
                    if (!dumpDebug) {
                        debug.println("访问被拒绝 " + perm);
                    }
                    Thread.dumpStack();
                    final ProtectionDomain pd = context[i];
                    final Debug db = debug;
                    AccessController.doPrivileged (new PrivilegedAction<Void>() {
                        public Void run() {
                            db.println("失败的域 " + pd);
                            return null;
                        }
                    });
                }
                throw new AccessControlException("访问被拒绝 " + perm, perm);
            }
        }

        // 如果所有域都允许访问，则允许访问。
        if (dumpDebug) {
            debug.println("访问允许 " + perm);
        }

        checkPermission2(perm);
    }

    /*
     * 检查与有限特权范围关联的域。
     */
    private void checkPermission2(Permission perm) {
        if (!isLimited) {
            return;
        }

        /*
         * 检查 doPrivileged() 上下文参数（如果存在）。
         */
        if (privilegedContext != null) {
            privilegedContext.checkPermission2(perm);
        }

        /*
         * 忽略包装上下文的有限权限和父字段，因为它们已经合并到未包装的上下文中。
         */
        if (isWrapped) {
            return;
        }

        /*
         * 尝试匹配任何有限的特权范围。
         */
        if (permissions != null) {
            Class<?> permClass = perm.getClass();
            for (int i = 0; i < permissions.length; i++) {
                Permission limit = permissions[i];
                if (limit.getClass().equals(permClass) && limit.implies(perm)) {
                    return;
                }
            }
        }

        /*
         * 检查调用堆栈或此 ACC 的继承父线程调用堆栈中的有限特权范围。
         */
        if (parent != null) {
            /*
             * 作为一种优化，如果父上下文是从父线程继承的调用堆栈上下文，
             * 则检查父上下文的保护域是多余的，因为它们已经通过 optimize() 合并到子线程的上下文中。
             * 当父上下文设置为继承上下文时，此上下文不是由有限范围的 doPrivileged() 直接创建的，
             * 并且它没有自己的有限权限。
             */
            if (permissions == null) {
                parent.checkPermission2(perm);
            } else {
                parent.checkPermission(perm);
            }
        }
    }

    /**
     * 将基于堆栈的上下文（this）与特权或继承的上下文结合，如果需要的话。
     * 任何有限的特权范围都会被标记，无论分配的上下文是否来自立即封闭的有限 doPrivileged()。
     * 有限的特权范围可以间接地从继承的父线程或通过 getContext() 之前捕获的分配上下文流过来。
     */
    AccessControlContext optimize() {
        // 分配的（特权或继承的）上下文
        AccessControlContext acc;
        DomainCombiner combiner = null;
        AccessControlContext parent = null;
        Permission[] permissions = null;

        if (isPrivileged) {
            acc = privilegedContext;
            if (acc != null) {
                /*
                 * 如果上下文来自有限范围的 doPrivileged()，则从创建来保存它们的包装上下文中复制权限和父字段。
                 */
                if (acc.isWrapped) {
                    permissions = acc.permissions;
                    parent = acc.parent;
                }
            }
        } else {
            acc = AccessController.getInheritedAccessControlContext();
            if (acc != null) {
                /*
                 * 如果继承的上下文受有限范围的 doPrivileged() 限制，则将其设置为父上下文，
                 * 以便我们处理与域无关的状态。
                 */
                if (acc.isLimited) {
                    parent = acc;
                }
            }
        }

        // 如果堆栈上只有系统代码，则忽略堆栈上下文
        boolean skipStack = (context == null);

        // 如果只有系统代码参与，则忽略分配的上下文
        boolean skipAssigned = (acc == null || acc.context == null);
        ProtectionDomain[] assigned = (skipAssigned) ? null : acc.context;
        ProtectionDomain[] pd;

        // 如果堆栈或从父线程继承的上下文中没有封闭的有限特权范围
        boolean skipLimited = ((acc == null || !acc.isWrapped) && parent == null);

        if (acc != null && acc.combiner != null) {
            // 让分配的 acc 的 combiner 执行其操作
            if (getDebug() != null) {
                debug.println("AccessControlContext 调用 Combiner");
            }

            // 无需克隆当前和分配的上下文
            // combine() 不会更新它们
            combiner = acc.combiner;
            pd = combiner.combine(context, assigned);
        } else {
            if (skipStack) {
                if (skipAssigned) {
                    calculateFields(acc, parent, permissions);
                    return this;
                } else if (skipLimited) {
                    return acc;
                }
            } else if (assigned != null) {
                if (skipLimited) {
                    // 优化：如果堆栈中只有一个域，并且该域已经在分配的上下文中；无需合并
                    if (context.length == 1 && context[0] == assigned[0]) {
                        return acc;
                    }
                }
            }

            pd = combine(context, assigned);
            if (skipLimited && !skipAssigned && pd == assigned) {
                return acc;
            } else if (skipAssigned && pd == context) {
                calculateFields(acc, parent, permissions);
                return this;
            }
        }

        // 重用现有的 ACC
        this.context = pd;
        this.combiner = combiner;
        this.isPrivileged = false;

        calculateFields(acc, parent, permissions);
        return this;
    }


    /*
     * 合并当前（堆栈）和分配的域。
     */
    private static ProtectionDomain[] combine(ProtectionDomain[] current,
        ProtectionDomain[] assigned) {

        // 如果堆栈上只有系统代码，则忽略堆栈上下文
        boolean skipStack = (current == null);

        // 如果只有系统代码参与，则忽略分配的上下文
        boolean skipAssigned = (assigned == null);

        int slen = (skipStack) ? 0 : current.length;

        // 优化：如果没有分配的上下文且堆栈长度小于或等于两个；无需压缩堆栈上下文，它已经是压缩的
        if (skipAssigned && slen <= 2) {
            return current;
        }

        int n = (skipAssigned) ? 0 : assigned.length;

        // 现在我们合并它们，并创建一个新的上下文
        ProtectionDomain pd[] = new ProtectionDomain[slen + n];

        // 首先复制分配的上下文域，无需压缩
        if (!skipAssigned) {
            System.arraycopy(assigned, 0, pd, 0, n);
        }

        // 现在添加堆栈上下文域，丢弃 null 和重复项
    outer:
        for (int i = 0; i < slen; i++) {
            ProtectionDomain sd = current[i];
            if (sd != null) {
                for (int j = 0; j < n; j++) {
                    if (sd == pd[j]) {
                        continue outer;
                    }
                }
                pd[n++] = sd;
            }
        }

        // 如果长度不相等，我们需要缩短数组
        if (n != pd.length) {
            // 优化：如果实际上没有合并任何内容
            if (!skipAssigned && n == assigned.length) {
                return assigned;
            } else if (skipAssigned && n == slen) {
                return current;
            }
            ProtectionDomain tmp[] = new ProtectionDomain[n];
            System.arraycopy(pd, 0, tmp, 0, n);
            pd = tmp;
        }

        return pd;
    }


    /*
     * 计算通过有限特权范围可能到达的额外域。
     * 如果可达域（如果有）已经包含在此域上下文中（在这种情况下，任何有限特权范围检查都是多余的），
     * 则将上下文标记为受有限特权范围影响。
     */
    private void calculateFields(AccessControlContext assigned,
        AccessControlContext parent, Permission[] permissions)
    {
        ProtectionDomain[] parentLimit = null;
        ProtectionDomain[] assignedLimit = null;
        ProtectionDomain[] newLimit;

        parentLimit = (parent != null)? parent.limitedContext: null;
        assignedLimit = (assigned != null)? assigned.limitedContext: null;
        newLimit = combine(parentLimit, assignedLimit);
        if (newLimit != null) {
            if (context == null || !containsAllPDs(newLimit, context)) {
                this.limitedContext = newLimit;
                this.permissions = permissions;
                this.parent = parent;
                this.isLimited = true;
            }
        }
    }


    /**
     * 检查两个 AccessControlContext 对象是否相等。
     * 检查 <i>obj</i> 是否是
     * AccessControlContext，并且具有与此上下文相同的 ProtectionDomain 集合。
     * <P>
     * @param obj 要测试与此对象相等的对象。
     * @return 如果 <i>obj</i> 是 AccessControlContext，并且具有与此上下文相同的 ProtectionDomain 集合，则返回 true，否则返回 false。
     */
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (! (obj instanceof AccessControlContext))
            return false;

        AccessControlContext that = (AccessControlContext) obj;

        if (!equalContext(that))
            return false;

        if (!equalLimitedContext(that))
            return false;

        return true;
    }

    /*
     * 基于不受有限特权复杂性影响的状态进行相等性比较。
     */
    private boolean equalContext(AccessControlContext that) {
        if (!equalPDs(this.context, that.context))
            return false;

        if (this.combiner == null && that.combiner != null)
            return false;

        if (this.combiner != null && !this.combiner.equals(that.combiner))
            return false;

        return true;
    }

    private boolean equalPDs(ProtectionDomain[] a, ProtectionDomain[] b) {
        if (a == null) {
            return (b == null);
        }

        if (b == null)
            return false;

        if (!(containsAllPDs(a, b) && containsAllPDs(b, a)))
            return false;

        return true;
    }

    /*
     * 基于在有限特权范围生效时调用 AccessController.getContext() 时捕获的状态进行相等性比较。
     */
    private boolean equalLimitedContext(AccessControlContext that) {
        if (that == null)
            return false;

        /*
         * 如果两个实例都没有有限的特权范围，则完成。
         */
        if (!this.isLimited && !that.isLimited)
            return true;

        /*
         * 如果只有一个实例有有限的特权范围，则完成。
         */
         if (!(this.isLimited && that.isLimited))
             return false;

        /*
         * 包装实例不应逃逸到实现类和 AccessController 之外，
         * 因此这可能永远不会发生，但只有在它们都有相同的 isWrapped 状态时才有意义进行比较。
         */
        if ((this.isWrapped && !that.isWrapped) ||
            (!this.isWrapped && that.isWrapped)) {
            return false;
        }

        if (this.permissions == null && that.permissions != null)
            return false;

        if (this.permissions != null && that.permissions == null)
            return false;

        if (!(this.containsAllLimits(that) && that.containsAllLimits(this)))
            return false;

        /*
         * 跳过任何包装的上下文。
         */
        AccessControlContext thisNextPC = getNextPC(this);
        AccessControlContext thatNextPC = getNextPC(that);

        /*
         * 特权上下文的保护域和 combiner 无关紧要，因为它们已经通过 optimize() 包含在此实例的上下文中，
         * 因此我们只关心它们可能具有的任何有限特权状态。
         */
        if (thisNextPC == null && thatNextPC != null && thatNextPC.isLimited)
            return false;

        if (thisNextPC != null && !thisNextPC.equalLimitedContext(thatNextPC))
            return false;

        if (this.parent == null && that.parent != null)
            return false;

        if (this.parent != null && !this.parent.equals(that.parent))
            return false;

        return true;
    }

    /*
     * 跟随 privilegedContext 链，尽量跳过任何包装的上下文。
     */
    private static AccessControlContext getNextPC(AccessControlContext acc) {
        while (acc != null && acc.privilegedContext != null) {
            acc = acc.privilegedContext;
            if (!acc.isWrapped)
                return acc;
        }
        return null;
    }

    private static boolean containsAllPDs(ProtectionDomain[] thisContext,
        ProtectionDomain[] thatContext) {
        boolean match = false;

        //
        // 保护域在 ACC 中目前不能为 null，
        // 这是由构造函数和各种 optimize 方法强制执行的。然而，历史上此逻辑尝试支持 null PD 的概念，
        // 因此此逻辑继续支持该概念。
        ProtectionDomain thisPd;
        for (int i = 0; i < thisContext.length; i++) {
            match = false;
            if ((thisPd = thisContext[i]) == null) {
                for (int j = 0; (j < thatContext.length) && !match; j++) {
                    match = (thatContext[j] == null);
                }
            } else {
                Class<?> thisPdClass = thisPd.getClass();
                ProtectionDomain thatPd;
                for (int j = 0; (j < thatContext.length) && !match; j++) {
                    thatPd = thatContext[j];


                                // 类检查以避免 PD 暴露 (4285406)
                    match = (thatPd != null &&
                        thisPdClass == thatPd.getClass() && thisPd.equals(thatPd));
                }
            }
            if (!match) return false;
        }
        return match;
    }

    private boolean containsAllLimits(AccessControlContext that) {
        boolean match = false;
        Permission thisPerm;

        if (this.permissions == null && that.permissions == null)
            return true;

        for (int i = 0; i < this.permissions.length; i++) {
            Permission limit = this.permissions[i];
            Class <?> limitClass = limit.getClass();
            match = false;
            for (int j = 0; (j < that.permissions.length) && !match; j++) {
                Permission perm = that.permissions[j];
                match = (limitClass.equals(perm.getClass()) &&
                    limit.equals(perm));
            }
            if (!match) return false;
        }
        return match;
    }


    /**
     * 返回此上下文的哈希码值。哈希码是通过将上下文中所有保护域的哈希码进行异或运算来计算的。
     *
     * @return 此上下文的哈希码值。
     */

    public int hashCode() {
        int hashCode = 0;

        if (context == null)
            return hashCode;

        for (int i =0; i < context.length; i++) {
            if (context[i] != null)
                hashCode ^= context[i].hashCode();
        }

        return hashCode;
    }
}
