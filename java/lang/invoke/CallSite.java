
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

import sun.invoke.empty.Empty;
import static java.lang.invoke.MethodHandleStatics.*;
import static java.lang.invoke.MethodHandles.Lookup.IMPL_LOOKUP;

/**
 * {@code CallSite} 是一个持有可变 {@link MethodHandle} 的容器，
 * 称为它的 {@code target}。
 * 一个 {@code invokedynamic} 指令链接到一个 {@code CallSite} 会将所有调用委托给该站点的当前目标。
 * 一个 {@code CallSite} 可能与多个 {@code invokedynamic} 指令关联，或者“自由浮动”，不与任何指令关联。
 * 在任何情况下，都可以通过与之关联的方法句柄（称为其 {@linkplain #dynamicInvoker 动态调用者}）来调用它。
 * <p>
 * {@code CallSite} 是一个抽象类，不允许用户直接继承。它有三个直接的、具体的子类，可以实例化或继承。
 * <ul>
 * <li>如果不需要可变目标，可以通过 {@linkplain ConstantCallSite 常量调用站点} 永久绑定一个 {@code invokedynamic} 指令。
 * <li>如果需要具有易失变量语义的可变目标，因为对目标的更新必须立即且可靠地被其他线程观察到，可以使用 {@linkplain VolatileCallSite 易失调用站点}。
 * <li>否则，如果需要可变目标，可以使用 {@linkplain MutableCallSite 可变调用站点}。
 * </ul>
 * <p>
 * 非常量调用站点可以通过更改其目标来 <em>重新链接</em>。
 * 新目标必须与旧目标具有相同的 {@linkplain MethodHandle#type() 类型}。
 * 因此，虽然调用站点可以重新链接到一系列连续的目标，但不能改变其类型。
 * <p>
 * 以下是一个调用站点和引导方法的示例用法，该示例将每个动态调用站点链接到打印其参数：
<blockquote><pre>{@code
static void test() throws Throwable {
    // THE FOLLOWING LINE IS PSEUDOCODE FOR A JVM INSTRUCTION
    InvokeDynamic[#bootstrapDynamic].baz("baz arg", 2, 3.14);
}
private static void printArgs(Object... args) {
  System.out.println(java.util.Arrays.deepToString(args));
}
private static final MethodHandle printArgs;
static {
  MethodHandles.Lookup lookup = MethodHandles.lookup();
  Class thisClass = lookup.lookupClass();  // (who am I?)
  printArgs = lookup.findStatic(thisClass,
      "printArgs", MethodType.methodType(void.class, Object[].class));
}
private static CallSite bootstrapDynamic(MethodHandles.Lookup caller, String name, MethodType type) {
  // ignore caller and name, but match the type:
  return new ConstantCallSite(printArgs.asType(type));
}
}</pre></blockquote>
 * @author John Rose, JSR 292 EG
 */
abstract
public class CallSite {
    static { MethodHandleImpl.initStatics(); }

    // 该调用站点的实际负载：
    /*package-private*/
    MethodHandle target;    // 注意：此字段为 JVM 所知。不要更改。

    /**
     * 创建一个具有给定方法类型的空白调用站点对象。
     * 提供一个初始目标方法，如果调用该方法将抛出
     * 一个 {@link IllegalStateException}。
     * <p>
     * 在从引导方法返回此 {@code CallSite} 对象之前，
     * 通常会通过调用 {@link CallSite#setTarget(MethodHandle) setTarget} 方法为其提供一个更有用的目标方法。
     * @throws NullPointerException 如果提供的类型为 null
     */
    /*package-private*/
    CallSite(MethodType type) {
        target = makeUninitializedCallSite(type);
    }

    /**
     * 创建一个具有初始目标方法句柄的调用站点对象。
     * @param target 将成为调用站点初始目标的方法句柄
     * @throws NullPointerException 如果提供的目标为 null
     */
    /*package-private*/
    CallSite(MethodHandle target) {
        target.type();  // null 检查
        this.target = target;
    }

    /**
     * 创建一个具有初始目标方法句柄的调用站点对象。
     * @param targetType 调用站点的期望类型
     * @param createTargetHook 一个钩子，用于将调用站点绑定到目标方法句柄
     * @throws WrongMethodTypeException 如果钩子不能在所需参数上调用，
     *         或者钩子返回的目标不是给定的 {@code targetType}
     * @throws NullPointerException 如果钩子返回 null 值
     * @throws ClassCastException 如果钩子返回的不是 {@code MethodHandle}
     * @throws Throwable 钩子函数抛出的任何其他异常
     */
    /*package-private*/
    CallSite(MethodType targetType, MethodHandle createTargetHook) throws Throwable {
        this(targetType);
        ConstantCallSite selfCCS = (ConstantCallSite) this;
        MethodHandle boundTarget = (MethodHandle) createTargetHook.invokeWithArguments(selfCCS);
        checkTargetChange(this.target, boundTarget);
        this.target = boundTarget;
    }

    /**
     * 返回此调用站点目标的类型。
     * 虽然目标可能会改变，但任何调用站点的类型是永久的，不能改变为不相等的类型。
     * {@code setTarget} 方法通过拒绝任何与旧目标类型不同的新目标来强制执行此不变性。
     * @return 当前目标的类型，也是任何未来目标的类型
     */
    public MethodType type() {
        // 警告：不要在这里调用 getTarget，因为 CCS.getTarget 可能会抛出 IllegalStateException
        return target.type();
    }

    /**
     * 根据此调用站点特定类定义的行为返回调用站点的目标方法。
     * {@code CallSite} 的直接子类记录了此方法的类特定行为。
     *
     * @return 调用站点的当前链接状态，即其目标方法句柄
     * @see ConstantCallSite
     * @see VolatileCallSite
     * @see #setTarget
     * @see ConstantCallSite#getTarget
     * @see MutableCallSite#getTarget
     * @see VolatileCallSite#getTarget
     */
    public abstract MethodHandle getTarget();

    /**
     * 根据此调用站点特定类定义的行为更新调用站点的目标方法。
     * {@code CallSite} 的直接子类记录了此方法的类特定行为。
     * <p>
     * 新目标的类型必须与旧目标的类型 {@linkplain MethodType#equals 相等}。
     *
     * @param newTarget 新目标
     * @throws NullPointerException 如果提供的新目标为 null
     * @throws WrongMethodTypeException 如果提供的新目标
     *         的方法类型与旧目标不同
     * @see CallSite#getTarget
     * @see ConstantCallSite#setTarget
     * @see MutableCallSite#setTarget
     * @see VolatileCallSite#setTarget
     */
    public abstract void setTarget(MethodHandle newTarget);

    void checkTargetChange(MethodHandle oldTarget, MethodHandle newTarget) {
        MethodType oldType = oldTarget.type();
        MethodType newType = newTarget.type();  // null 检查！
        if (!newType.equals(oldType))
            throw wrongTargetType(newTarget, oldType);
    }

    private static WrongMethodTypeException wrongTargetType(MethodHandle target, MethodType type) {
        return new WrongMethodTypeException(String.valueOf(target)+" should be of type "+type);
    }

    /**
     * 生成一个方法句柄，等同于已链接到此调用站点的 invokedynamic 指令。
     * <p>
     * 此方法等同于以下代码：
     * <blockquote><pre>{@code
     * MethodHandle getTarget, invoker, result;
     * getTarget = MethodHandles.publicLookup().bind(this, "getTarget", MethodType.methodType(MethodHandle.class));
     * invoker = MethodHandles.exactInvoker(this.type());
     * result = MethodHandles.foldArguments(invoker, getTarget)
     * }</pre></blockquote>
     *
     * @return 一个方法句柄，始终调用此调用站点的当前目标
     */
    public abstract MethodHandle dynamicInvoker();

    /*non-public*/ MethodHandle makeDynamicInvoker() {
        MethodHandle getTarget = GET_TARGET.bindArgumentL(0, this);
        MethodHandle invoker = MethodHandles.exactInvoker(this.type());
        return MethodHandles.foldArguments(invoker, getTarget);
    }

    private static final MethodHandle GET_TARGET;
    private static final MethodHandle THROW_UCS;
    static {
        try {
            GET_TARGET = IMPL_LOOKUP.
                findVirtual(CallSite.class, "getTarget", MethodType.methodType(MethodHandle.class));
            THROW_UCS = IMPL_LOOKUP.
                findStatic(CallSite.class, "uninitializedCallSite", MethodType.methodType(Object.class, Object[].class));
        } catch (ReflectiveOperationException e) {
            throw newInternalError(e);
        }
    }

    /** 如果在构造函数中提供了 MethodType，则将其滚动到默认目标中。 */
    private static Object uninitializedCallSite(Object... ignore) {
        throw new IllegalStateException("uninitialized call site");
    }

    private MethodHandle makeUninitializedCallSite(MethodType targetType) {
        MethodType basicType = targetType.basicType();
        MethodHandle invoker = basicType.form().cachedMethodHandle(MethodTypeForm.MH_UNINIT_CS);
        if (invoker == null) {
            invoker = THROW_UCS.asType(basicType);
            invoker = basicType.form().setCachedMethodHandle(MethodTypeForm.MH_UNINIT_CS, invoker);
        }
        // 未检查视图是可以的，因为不会接收或返回任何值
        return invoker.viewAsType(targetType, false);
    }

    // 不安全的操作：
    private static final long TARGET_OFFSET;
    static {
        try {
            TARGET_OFFSET = UNSAFE.objectFieldOffset(CallSite.class.getDeclaredField("target"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    /*package-private*/
    void setTargetNormal(MethodHandle newTarget) {
        MethodHandleNatives.setCallSiteTargetNormal(this, newTarget);
    }
    /*package-private*/
    MethodHandle getTargetVolatile() {
        return (MethodHandle) UNSAFE.getObjectVolatile(this, TARGET_OFFSET);
    }
    /*package-private*/
    void setTargetVolatile(MethodHandle newTarget) {
        MethodHandleNatives.setCallSiteTargetVolatile(this, newTarget);
    }

    // 这实现了来自 JVM 的上行调用，MethodHandleNatives.makeDynamicCallSite：
    static CallSite makeSite(MethodHandle bootstrapMethod,
                             // 被调用者信息：
                             String name, MethodType type,
                             // BSM 的额外参数，如果有：
                             Object info,
                             // 调用者信息：
                             Class<?> callerClass) {
        MethodHandles.Lookup caller = IMPL_LOOKUP.in(callerClass);
        CallSite site;
        try {
            Object binding;
            info = maybeReBox(info);
            if (info == null) {
                binding = bootstrapMethod.invoke(caller, name, type);
            } else if (!info.getClass().isArray()) {
                binding = bootstrapMethod.invoke(caller, name, type, info);
            } else {
                Object[] argv = (Object[]) info;
                maybeReBoxElements(argv);
                switch (argv.length) {
                case 0:
                    binding = bootstrapMethod.invoke(caller, name, type);
                    break;
                case 1:
                    binding = bootstrapMethod.invoke(caller, name, type,
                                                     argv[0]);
                    break;
                case 2:
                    binding = bootstrapMethod.invoke(caller, name, type,
                                                     argv[0], argv[1]);
                    break;
                case 3:
                    binding = bootstrapMethod.invoke(caller, name, type,
                                                     argv[0], argv[1], argv[2]);
                    break;
                case 4:
                    binding = bootstrapMethod.invoke(caller, name, type,
                                                     argv[0], argv[1], argv[2], argv[3]);
                    break;
                case 5:
                    binding = bootstrapMethod.invoke(caller, name, type,
                                                     argv[0], argv[1], argv[2], argv[3], argv[4]);
                    break;
                case 6:
                    binding = bootstrapMethod.invoke(caller, name, type,
                                                     argv[0], argv[1], argv[2], argv[3], argv[4], argv[5]);
                    break;
                default:
                    final int NON_SPREAD_ARG_COUNT = 3;  // (caller, name, type)
                    if (NON_SPREAD_ARG_COUNT + argv.length > MethodType.MAX_MH_ARITY)
                        throw new BootstrapMethodError("too many bootstrap method arguments");
                    MethodType bsmType = bootstrapMethod.type();
                    MethodType invocationType = MethodType.genericMethodType(NON_SPREAD_ARG_COUNT + argv.length);
                    MethodHandle typedBSM = bootstrapMethod.asType(invocationType);
                    MethodHandle spreader = invocationType.invokers().spreadInvoker(NON_SPREAD_ARG_COUNT);
                    binding = spreader.invokeExact(typedBSM, (Object)caller, (Object)name, (Object)type, argv);
                }
            }
            //System.out.println("BSM for "+name+type+" => "+binding);
            if (binding instanceof CallSite) {
                site = (CallSite) binding;
            }  else {
                throw new ClassCastException("bootstrap method failed to produce a CallSite");
            }
            if (!site.getTarget().type().equals(type))
                throw wrongTargetType(site.getTarget(), type);
        } catch (Throwable ex) {
            BootstrapMethodError bex;
            if (ex instanceof BootstrapMethodError)
                bex = (BootstrapMethodError) ex;
            else
                bex = new BootstrapMethodError("call site initialization exception", ex);
            throw bex;
        }
        return site;
    }


                private static Object maybeReBox(Object x) {
        if (x instanceof Integer) {
            int xi = (int) x;
            if (xi == (byte) xi)
                x = xi;  // 必须重新装箱；参见 JLS 5.1.7
        }
        return x;
    }
    private static void maybeReBoxElements(Object[] xa) {
        for (int i = 0; i < xa.length; i++) {
            xa[i] = maybeReBox(xa[i]);
        }
    }
}
