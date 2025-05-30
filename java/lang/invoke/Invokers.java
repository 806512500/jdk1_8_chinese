
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

import java.lang.reflect.Array;
import java.util.Arrays;

import static java.lang.invoke.MethodHandleStatics.*;
import static java.lang.invoke.MethodHandleNatives.Constants.*;
import static java.lang.invoke.MethodHandles.Lookup.IMPL_LOOKUP;
import static java.lang.invoke.LambdaForm.*;

/**
 * 经常使用的调用者的构造和缓存。
 * @author jrose
 */
class Invokers {
    // 出站调用的精确类型（不包括前导目标 MH）
    private final MethodType targetType;

    // 缓存的适配器信息：
    private final @Stable MethodHandle[] invokers = new MethodHandle[INV_LIMIT];
    // invokers 的索引：
    static final int
            INV_EXACT          =  0,  // MethodHandles.exactInvoker
            INV_GENERIC        =  1,  // MethodHandles.invoker (通用调用)
            INV_BASIC          =  2,  // MethodHandles.basicInvoker
            INV_LIMIT          =  3;

    /** 计算并缓存实现给定擦除类型的所有擦除家族成员的收集适配器的通用信息。
     */
    /*non-public*/ Invokers(MethodType targetType) {
        this.targetType = targetType;
    }

    /*non-public*/ MethodHandle exactInvoker() {
        MethodHandle invoker = cachedInvoker(INV_EXACT);
        if (invoker != null)  return invoker;
        invoker = makeExactOrGeneralInvoker(true);
        return setCachedInvoker(INV_EXACT, invoker);
    }

    /*non-public*/ MethodHandle genericInvoker() {
        MethodHandle invoker = cachedInvoker(INV_GENERIC);
        if (invoker != null)  return invoker;
        invoker = makeExactOrGeneralInvoker(false);
        return setCachedInvoker(INV_GENERIC, invoker);
    }

    /*non-public*/ MethodHandle basicInvoker() {
        MethodHandle invoker = cachedInvoker(INV_BASIC);
        if (invoker != null)  return invoker;
        MethodType basicType = targetType.basicType();
        if (basicType != targetType) {
            // 双缓存；不显著使用
            return setCachedInvoker(INV_BASIC, basicType.invokers().basicInvoker());
        }
        invoker = basicType.form().cachedMethodHandle(MethodTypeForm.MH_BASIC_INV);
        if (invoker == null) {
            MemberName method = invokeBasicMethod(basicType);
            invoker = DirectMethodHandle.make(method);
            assert(checkInvoker(invoker));
            invoker = basicType.form().setCachedMethodHandle(MethodTypeForm.MH_BASIC_INV, invoker);
        }
        return setCachedInvoker(INV_BASIC, invoker);
    }

    private MethodHandle cachedInvoker(int idx) {
        return invokers[idx];
    }

    private synchronized MethodHandle setCachedInvoker(int idx, final MethodHandle invoker) {
        // 模拟 CAS，以避免结果的竞态重复。
        MethodHandle prev = invokers[idx];
        if (prev != null)  return prev;
        return invokers[idx] = invoker;
    }

    private MethodHandle makeExactOrGeneralInvoker(boolean isExact) {
        MethodType mtype = targetType;
        MethodType invokerType = mtype.invokerType();
        int which = (isExact ? MethodTypeForm.LF_EX_INVOKER : MethodTypeForm.LF_GEN_INVOKER);
        LambdaForm lform = invokeHandleForm(mtype, false, which);
        MethodHandle invoker = BoundMethodHandle.bindSingle(invokerType, lform, mtype);
        String whichName = (isExact ? "invokeExact" : "invoke");
        invoker = invoker.withInternalMemberName(MemberName.makeMethodHandleInvoke(whichName, mtype), false);
        assert(checkInvoker(invoker));
        maybeCompileToBytecode(invoker);
        return invoker;
    }

    /** 如果目标类型看起来足够常见，则急切地将调用者编译为字节码。 */
    private void maybeCompileToBytecode(MethodHandle invoker) {
        final int EAGER_COMPILE_ARITY_LIMIT = 10;
        if (targetType == targetType.erase() &&
            targetType.parameterCount() < EAGER_COMPILE_ARITY_LIMIT) {
            invoker.form.compileToBytecode();
        }
    }

    // 这个方法是从 LambdaForm.NamedFunction.<init> 调用的。
    /*non-public*/ static MemberName invokeBasicMethod(MethodType basicType) {
        assert(basicType == basicType.basicType());
        try {
            //Lookup.findVirtual(MethodHandle.class, name, type);
            return IMPL_LOOKUP.resolveOrFail(REF_invokeVirtual, MethodHandle.class, "invokeBasic", basicType);
        } catch (ReflectiveOperationException ex) {
            throw newInternalError("JVM 无法找到 " + basicType + " 的调用者", ex);
        }
    }

    private boolean checkInvoker(MethodHandle invoker) {
        assert(targetType.invokerType().equals(invoker.type()))
                : java.util.Arrays.asList(targetType, targetType.invokerType(), invoker);
        assert(invoker.internalMemberName() == null ||
               invoker.internalMemberName().getMethodType().equals(targetType));
        assert(!invoker.isVarargsCollector());
        return true;
    }

    /**
     * 查找或创建一个调用者，该调用者传递给定数量的不变参数，并从尾部数组参数中展开其余参数。
     * 调用者目标类型是展开后的类型 {@code (TYPEOF(uarg*), TYPEOF(sarg*))=>RT}。
     * 所有的 {@code sarg} 必须有一个公共类型 {@code C}。 （如果没有，则假设为 {@code Object}。）
     * @param leadingArgCount 不变（非展开）参数的数量
     * @return {@code invoker.invokeExact(mh, uarg*, C[]{sarg*}) := (RT)mh.invoke(uarg*, sarg*)}
     */
    /*non-public*/ MethodHandle spreadInvoker(int leadingArgCount) {
        int spreadArgCount = targetType.parameterCount() - leadingArgCount;
        MethodType postSpreadType = targetType;
        Class<?> argArrayType = impliedRestargType(postSpreadType, leadingArgCount);
        if (postSpreadType.parameterSlotCount() <= MethodType.MAX_MH_INVOKER_ARITY) {
            return genericInvoker().asSpreader(argArrayType, spreadArgCount);
        }
        // 无法在此处构建类型为 ginvoker.invoke(mh, a*[254]) 的通用调用者。
        // 而是将 sinvoker.invoke(mh, a) 分解为 ainvoker.invoke(filter(mh), a)
        // 其中 filter(mh) == mh.asSpreader(Object[], spreadArgCount)
        MethodType preSpreadType = postSpreadType
            .replaceParameterTypes(leadingArgCount, postSpreadType.parameterCount(), argArrayType);
        MethodHandle arrayInvoker = MethodHandles.invoker(preSpreadType);
        MethodHandle makeSpreader = MethodHandles.insertArguments(Lazy.MH_asSpreader, 1, argArrayType, spreadArgCount);
        return MethodHandles.filterArgument(arrayInvoker, 0, makeSpreader);
    }

    private static Class<?> impliedRestargType(MethodType restargType, int fromPos) {
        if (restargType.isGeneric())  return Object[].class;  // 只能是这个
        int maxPos = restargType.parameterCount();
        if (fromPos >= maxPos)  return Object[].class;  // 合理的默认值
        Class<?> argType = restargType.parameterType(fromPos);
        for (int i = fromPos+1; i < maxPos; i++) {
            if (argType != restargType.parameterType(i))
                throw newIllegalArgumentException("需要同质的剩余参数", restargType);
        }
        if (argType == Object.class)  return Object[].class;
        return Array.newInstance(argType, 0).getClass();
    }

    public String toString() {
        return "Invokers" + targetType;
    }

    static MemberName methodHandleInvokeLinkerMethod(String name,
                                                     MethodType mtype,
                                                     Object[] appendixResult) {
        int which;
        switch (name) {
        case "invokeExact":  which = MethodTypeForm.LF_EX_LINKER; break;
        case "invoke":       which = MethodTypeForm.LF_GEN_LINKER; break;
        default:             throw new InternalError("不是调用者: " + name);
        }
        LambdaForm lform;
        if (mtype.parameterSlotCount() <= MethodType.MAX_MH_ARITY - MH_LINKER_ARG_APPENDED) {
            lform = invokeHandleForm(mtype, false, which);
            appendixResult[0] = mtype;
        } else {
            lform = invokeHandleForm(mtype, true, which);
        }
        return lform.vmentry;
    }

    // 用于尾部“附加值”（通常是 mtype）的参数计数
    private static final int MH_LINKER_ARG_APPENDED = 1;

    /** 返回一个用于 invokeExact 或通用调用的适配器，作为 MH 或常量池链接器。
     * 如果 !customized，调用者负责在适配器执行期间提供 mtype 的副本。这是因为适配器可能会被泛化为
     * 基本类型。
     * @param mtype 调用者的方法类型（可以是基本类型或完全自定义类型）
     * @param customized 是否使用尾部附加参数（携带 mtype）
     * @param which 位编码 0x01 是否是 CP 适配器（“链接器”）或 MHs.invoker 值（“调用者”）；
     *                          0x02 是否用于 invokeExact 或通用调用
     */
    private static LambdaForm invokeHandleForm(MethodType mtype, boolean customized, int which) {
        boolean isCached;
        if (!customized) {
            mtype = mtype.basicType();  // 将 Z 规范化为 I，将 String 规范化为 Object 等
            isCached = true;
        } else {
            isCached = false;  // 如果 mtype == mtype.basicType()，则可能缓存
        }
        boolean isLinker, isGeneric;
        String debugName;
        switch (which) {
        case MethodTypeForm.LF_EX_LINKER:   isLinker = true;  isGeneric = false; debugName = "invokeExact_MT"; break;
        case MethodTypeForm.LF_EX_INVOKER:  isLinker = false; isGeneric = false; debugName = "exactInvoker"; break;
        case MethodTypeForm.LF_GEN_LINKER:  isLinker = true;  isGeneric = true;  debugName = "invoke_MT"; break;
        case MethodTypeForm.LF_GEN_INVOKER: isLinker = false; isGeneric = true;  debugName = "invoker"; break;
        default: throw new InternalError();
        }
        LambdaForm lform;
        if (isCached) {
            lform = mtype.form().cachedLambdaForm(which);
            if (lform != null)  return lform;
        }
        // exactInvokerForm (Object,Object)Object
        //   link with java.lang.invoke.MethodHandle.invokeBasic(MethodHandle,Object,Object)Object/invokeSpecial
        final int THIS_MH      = 0;
        final int CALL_MH      = THIS_MH + (isLinker ? 0 : 1);
        final int ARG_BASE     = CALL_MH + 1;
        final int OUTARG_LIMIT = ARG_BASE + mtype.parameterCount();
        final int INARG_LIMIT  = OUTARG_LIMIT + (isLinker && !customized ? 1 : 0);
        int nameCursor = OUTARG_LIMIT;
        final int MTYPE_ARG    = customized ? -1 : nameCursor++;  // 可能是最后一个输入参数
        final int CHECK_TYPE   = nameCursor++;
        final int CHECK_CUSTOM = (CUSTOMIZE_THRESHOLD >= 0) ? nameCursor++ : -1;
        final int LINKER_CALL  = nameCursor++;
        MethodType invokerFormType = mtype.invokerType();
        if (isLinker) {
            if (!customized)
                invokerFormType = invokerFormType.appendParameterTypes(MemberName.class);
        } else {
            invokerFormType = invokerFormType.invokerType();
        }
        Name[] names = arguments(nameCursor - INARG_LIMIT, invokerFormType);
        assert(names.length == nameCursor)
                : Arrays.asList(mtype, customized, which, nameCursor, names.length);
        if (MTYPE_ARG >= INARG_LIMIT) {
            assert(names[MTYPE_ARG] == null);
            BoundMethodHandle.SpeciesData speciesData = BoundMethodHandle.speciesData_L();
            names[THIS_MH] = names[THIS_MH].withConstraint(speciesData);
            NamedFunction getter = speciesData.getterFunction(0);
            names[MTYPE_ARG] = new Name(getter, names[THIS_MH]);
            // 否则，如果 isLinker，则 MTYPE 由调用者（例如 JVM）传递
        }

        // 进行最终调用。如果 isGeneric，则前置类型检查的结果。
        MethodType outCallType = mtype.basicType();
        Object[] outArgs = Arrays.copyOfRange(names, CALL_MH, OUTARG_LIMIT, Object[].class);
        Object mtypeArg = (customized ? mtype : names[MTYPE_ARG]);
        if (!isGeneric) {
            names[CHECK_TYPE] = new Name(NF_checkExactType, names[CALL_MH], mtypeArg);
            // mh.invokeExact(a*):R => checkExactType(mh, TYPEOF(a*:R)); mh.invokeBasic(a*)
        } else {
            names[CHECK_TYPE] = new Name(NF_checkGenericType, names[CALL_MH], mtypeArg);
            // mh.invokeGeneric(a*):R => checkGenericType(mh, TYPEOF(a*:R)).invokeBasic(a*)
            outArgs[0] = names[CHECK_TYPE];
        }
        if (CHECK_CUSTOM != -1) {
            names[CHECK_CUSTOM] = new Name(NF_checkCustomized, outArgs[0]);
        }
        names[LINKER_CALL] = new Name(outCallType, outArgs);
        lform = new LambdaForm(debugName, INARG_LIMIT, names);
        if (isLinker)
            lform.compileToBytecode();  // JVM 需要一个真实的方法Oop
        if (isCached)
            lform = mtype.form().setCachedLambdaForm(which, lform);
        return lform;
    }

    /*non-public*/ static
    WrongMethodTypeException newWrongMethodTypeException(MethodType actual, MethodType expected) {
        // FIXME: 与 JVM 逻辑合并以抛出 WMTE
        return new WrongMethodTypeException("expected " + expected + " but found " + actual);
    }

    /** MethodHandle.invokeExact 检查代码的静态定义。 */
    /*non-public*/ static
    @ForceInline
    void checkExactType(Object mhObj, Object expectedObj) {
        MethodHandle mh = (MethodHandle) mhObj;
        MethodType expected = (MethodType) expectedObj;
        MethodType actual = mh.type();
        if (actual != expected)
            throw newWrongMethodTypeException(expected, actual);
    }

    /** MethodHandle.invokeGeneric 检查代码的静态定义。
     * 直接返回类型调整后的 MH 以调用，如下：
     * {@code (R)MH.invoke(a*) => MH.asType(TYPEOF(a*:R)).invokeBasic(a*)}
     */
    /*non-public*/ static
    @ForceInline
    Object checkGenericType(Object mhObj, Object expectedObj) {
        MethodHandle mh = (MethodHandle) mhObj;
        MethodType expected = (MethodType) expectedObj;
        return mh.asType(expected);
        /* 可能在此处添加更多路径。可能的优化：
         * 对于 (R)MH.invoke(a*),
         * 令 MT0 = TYPEOF(a*:R), MT1 = MH.type
         *
         * 如果 MT0==MT1 或 MT1 可以由 MT0 安全调用
         *  => MH.invokeBasic(a*)
         * 如果 MT1 可以由 MT0[R := Object] 安全调用
         *  => MH.invokeBasic(a*) & checkcast(R)
         * 如果 MT1 可以由 MT0[* := Object] 安全调用
         *  => checkcast(A)* & MH.invokeBasic(a*) & checkcast(R)
         * 如果可以从 (MT0,MT1) 中提取出一个大的适配器 BA
         *  => BA.invokeBasic(MT0,MH,a*)
         * 如果可以在静态 CS0 = new GICS(MT0) 上缓存一个本地适配器 LA
         *  => CS0.LA.invokeBasic(MH,a*)
         * 否则
         *  => MH.asType(MT0).invokeBasic(A*)
         */
    }


                static MemberName linkToCallSiteMethod(MethodType mtype) {
        LambdaForm lform = callSiteForm(mtype, false);
        return lform.vmentry;
    }

    static MemberName linkToTargetMethod(MethodType mtype) {
        LambdaForm lform = callSiteForm(mtype, true);
        return lform.vmentry;
    }

    // skipCallSite 为 true 表示我们正在优化一个 ConstantCallSite
    private static LambdaForm callSiteForm(MethodType mtype, boolean skipCallSite) {
        mtype = mtype.basicType();  // 将 Z 规范化为 I，将 String 规范化为 Object 等。
        final int which = (skipCallSite ? MethodTypeForm.LF_MH_LINKER : MethodTypeForm.LF_CS_LINKER);
        LambdaForm lform = mtype.form().cachedLambdaForm(which);
        if (lform != null)  return lform;
        // exactInvokerForm (Object,Object)Object
        //   通过 java.lang.invoke.MethodHandle.invokeBasic(MethodHandle,Object,Object)Object/invokeSpecial 进行链接
        final int ARG_BASE     = 0;
        final int OUTARG_LIMIT = ARG_BASE + mtype.parameterCount();
        final int INARG_LIMIT  = OUTARG_LIMIT + 1;
        int nameCursor = OUTARG_LIMIT;
        final int APPENDIX_ARG = nameCursor++;  // 最后一个输入参数
        final int CSITE_ARG    = skipCallSite ? -1 : APPENDIX_ARG;
        final int CALL_MH      = skipCallSite ? APPENDIX_ARG : nameCursor++;  // getTarget 的结果
        final int LINKER_CALL  = nameCursor++;
        MethodType invokerFormType = mtype.appendParameterTypes(skipCallSite ? MethodHandle.class : CallSite.class);
        Name[] names = arguments(nameCursor - INARG_LIMIT, invokerFormType);
        assert(names.length == nameCursor);
        assert(names[APPENDIX_ARG] != null);
        if (!skipCallSite)
            names[CALL_MH] = new Name(NF_getCallSiteTarget, names[CSITE_ARG]);
        // (site.)invokedynamic(a*):R => mh = site.getTarget(); mh.invokeBasic(a*)
        final int PREPEND_MH = 0, PREPEND_COUNT = 1;
        Object[] outArgs = Arrays.copyOfRange(names, ARG_BASE, OUTARG_LIMIT + PREPEND_COUNT, Object[].class);
        // 添加 MH 参数：
        System.arraycopy(outArgs, 0, outArgs, PREPEND_COUNT, outArgs.length - PREPEND_COUNT);
        outArgs[PREPEND_MH] = names[CALL_MH];
        names[LINKER_CALL] = new Name(mtype, outArgs);
        lform = new LambdaForm((skipCallSite ? "linkToTargetMethod" : "linkToCallSite"), INARG_LIMIT, names);
        lform.compileToBytecode();  // JVM 需要一个实际的方法Oop
        lform = mtype.form().setCachedLambdaForm(which, lform);
        return lform;
    }

    /** MethodHandle.invokeGeneric 静态定义检查代码。 */
    /*non-public*/ static
    @ForceInline
    Object getCallSiteTarget(Object site) {
        return ((CallSite)site).getTarget();
    }

    /*non-public*/ static
    @ForceInline
    void checkCustomized(Object o) {
        MethodHandle mh = (MethodHandle)o;
        if (mh.form.customized == null) {
            maybeCustomize(mh);
        }
    }

    /*non-public*/ static
    @DontInline
    void maybeCustomize(MethodHandle mh) {
        byte count = mh.customizationCount;
        if (count >= CUSTOMIZE_THRESHOLD) {
            mh.customize();
        } else {
            mh.customizationCount = (byte)(count+1);
        }
    }

    // 本地常量函数：
    private static final NamedFunction
        NF_checkExactType,
        NF_checkGenericType,
        NF_getCallSiteTarget,
        NF_checkCustomized;
    static {
        try {
            NamedFunction nfs[] = {
                NF_checkExactType = new NamedFunction(Invokers.class
                        .getDeclaredMethod("checkExactType", Object.class, Object.class)),
                NF_checkGenericType = new NamedFunction(Invokers.class
                        .getDeclaredMethod("checkGenericType", Object.class, Object.class)),
                NF_getCallSiteTarget = new NamedFunction(Invokers.class
                        .getDeclaredMethod("getCallSiteTarget", Object.class)),
                NF_checkCustomized = new NamedFunction(Invokers.class
                        .getDeclaredMethod("checkCustomized", Object.class))
            };
            for (NamedFunction nf : nfs) {
                // 每个 nf 必须是静态可调用的，否则我们会陷入自举循环。
                assert(InvokerBytecodeGenerator.isStaticallyInvocable(nf.member)) : nf;
                nf.resolve();
            }
        } catch (ReflectiveOperationException ex) {
            throw newInternalError(ex);
        }
    }

    private static class Lazy {
        private static final MethodHandle MH_asSpreader;

        static {
            try {
                MH_asSpreader = IMPL_LOOKUP.findVirtual(MethodHandle.class, "asSpreader",
                        MethodType.methodType(MethodHandle.class, Class.class, int.class));
            } catch (ReflectiveOperationException ex) {
                throw newInternalError(ex);
            }
        }
    }
}
