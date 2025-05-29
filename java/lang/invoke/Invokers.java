
/*
 * Copyright (c) 2008, 2020, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.invoke;

import java.lang.reflect.Array;
import java.util.Arrays;

import static java.lang.invoke.MethodHandleStatics.*;
import static java.lang.invoke.MethodHandleNatives.Constants.*;
import static java.lang.invoke.MethodHandles.Lookup.IMPL_LOOKUP;
import static java.lang.invoke.LambdaForm.*;

/**
 * 常用调用者的构造和缓存。
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

    /** 计算并缓存实现给定擦除类型的所有收集适配器的通用信息。
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

    // 下一个是从 LambdaForm.NamedFunction.<init> 调用的。
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
     * 查找或创建一个调用者，该调用者传递给定数量的不变参数，并展开其余的尾随数组参数。
     * 调用者的目标类型是展开后的类型 {@code (TYPEOF(uarg*), TYPEOF(sarg*))=>RT}。
     * 所有的 {@code sarg} 必须有一个共同的类型 {@code C}。 （如果没有，则假定为 {@code Object}。）
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
        // 相反，将 sinvoker.invoke(mh, a) 分解为 ainvoker.invoke(filter(mh), a)
        // 其中 filter(mh) == mh.asSpreader(Object[], spreadArgCount)
        MethodType preSpreadType = postSpreadType
            .replaceParameterTypes(leadingArgCount, postSpreadType.parameterCount(), argArrayType);
        MethodHandle arrayInvoker = MethodHandles.invoker(preSpreadType);
        MethodHandle makeSpreader = MethodHandles.insertArguments(Lazy.MH_asSpreader, 1, argArrayType, spreadArgCount);
        return MethodHandles.filterArgument(arrayInvoker, 0, makeSpreader);
    }


                private static Class<?> impliedRestargType(MethodType restargType, int fromPos) {
        if (restargType.isGeneric())  return Object[].class;  // 可能是其他任何类型
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
        return "Invokers"+targetType;
    }

    static MemberName methodHandleInvokeLinkerMethod(String name,
                                                     MethodType mtype,
                                                     Object[] appendixResult) {
        int which;
        switch (name) {
        case "invokeExact":  which = MethodTypeForm.LF_EX_LINKER; break;
        case "invoke":       which = MethodTypeForm.LF_GEN_LINKER; break;
        default:             throw new InternalError("不是调用者: "+name);
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

    // 用于计算尾部“附加值”（通常是 mtype）的参数计数
    private static final int MH_LINKER_ARG_APPENDED = 1;

    /** 返回一个适配器，用于精确调用或通用调用，作为 MH 或常量池链接器。
     * 如果 !customized，调用者负责在适配器执行期间提供 mtype 的精确副本。这是因为适配器可能会被泛化为基本类型。
     * @param mtype 调用者的方法类型（可以是基本类型或完全自定义类型）
     * @param customized 是否使用尾部附加参数（用于携带 mtype）
     * @param which 位编码 0x01 表示它是 CP 适配器（“链接器”）还是 MHs.invoker 值（“调用者”）；
     *                          0x02 表示它是用于精确调用还是通用调用
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
        //   使用 java.lang.invoke.MethodHandle.invokeBasic(MethodHandle,Object,Object)Object/invokeSpecial 进行链接
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
            // 否则，如果 isLinker，则 MTYPE 由调用者（例如，JVM）传递
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
            lform.compileToBytecode();  // JVM 需要一个实际的方法Oop
        if (isCached)
            lform = mtype.form().setCachedLambdaForm(which, lform);
        return lform;
    }


                /*non-public*/ static
    WrongMethodTypeException newWrongMethodTypeException(MethodType actual, MethodType expected) {
        // FIXME: merge with JVM logic for throwing WMTE
        return new WrongMethodTypeException("expected "+expected+" but found "+actual);
    }

    /** Static definition of MethodHandle.invokeExact checking code. */
    /*non-public*/ static
    @ForceInline
    void checkExactType(Object mhObj, Object expectedObj) {
        MethodHandle mh = (MethodHandle) mhObj;
        MethodType expected = (MethodType) expectedObj;
        MethodType actual = mh.type();
        if (actual != expected)
            throw newWrongMethodTypeException(expected, actual);
    }

    /** Static definition of MethodHandle.invokeGeneric checking code.
     * Directly returns the type-adjusted MH to invoke, as follows:
     * {@code (R)MH.invoke(a*) => MH.asType(TYPEOF(a*:R)).invokeBasic(a*)}
     */
    /*non-public*/ static
    @ForceInline
    Object checkGenericType(Object mhObj, Object expectedObj) {
        MethodHandle mh = (MethodHandle) mhObj;
        MethodType expected = (MethodType) expectedObj;
        return mh.asType(expected);
        /* Maybe add more paths here.  Possible optimizations:
         * for (R)MH.invoke(a*),
         * let MT0 = TYPEOF(a*:R), MT1 = MH.type
         *
         * if MT0==MT1 or MT1 can be safely called by MT0
         *  => MH.invokeBasic(a*)
         * if MT1 can be safely called by MT0[R := Object]
         *  => MH.invokeBasic(a*) & checkcast(R)
         * if MT1 can be safely called by MT0[* := Object]
         *  => checkcast(A)* & MH.invokeBasic(a*) & checkcast(R)
         * if a big adapter BA can be pulled out of (MT0,MT1)
         *  => BA.invokeBasic(MT0,MH,a*)
         * if a local adapter LA can be cached on static CS0 = new GICS(MT0)
         *  => CS0.LA.invokeBasic(MH,a*)
         * else
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

    // skipCallSite is true if we are optimizing a ConstantCallSite
    private static LambdaForm callSiteForm(MethodType mtype, boolean skipCallSite) {
        mtype = mtype.basicType();  // normalize Z to I, String to Object, etc.
        final int which = (skipCallSite ? MethodTypeForm.LF_MH_LINKER : MethodTypeForm.LF_CS_LINKER);
        LambdaForm lform = mtype.form().cachedLambdaForm(which);
        if (lform != null)  return lform;
        // exactInvokerForm (Object,Object)Object
        //   link with java.lang.invoke.MethodHandle.invokeBasic(MethodHandle,Object,Object)Object/invokeSpecial
        final int ARG_BASE     = 0;
        final int OUTARG_LIMIT = ARG_BASE + mtype.parameterCount();
        final int INARG_LIMIT  = OUTARG_LIMIT + 1;
        int nameCursor = OUTARG_LIMIT;
        final int APPENDIX_ARG = nameCursor++;  // the last in-argument
        final int CSITE_ARG    = skipCallSite ? -1 : APPENDIX_ARG;
        final int CALL_MH      = skipCallSite ? APPENDIX_ARG : nameCursor++;  // result of getTarget
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
        // prepend MH argument:
        System.arraycopy(outArgs, 0, outArgs, PREPEND_COUNT, outArgs.length - PREPEND_COUNT);
        outArgs[PREPEND_MH] = names[CALL_MH];
        names[LINKER_CALL] = new Name(mtype, outArgs);
        lform = new LambdaForm((skipCallSite ? "linkToTargetMethod" : "linkToCallSite"), INARG_LIMIT, names);
        lform.compileToBytecode();  // JVM needs a real methodOop
        lform = mtype.form().setCachedLambdaForm(which, lform);
        return lform;
    }

    /** Static definition of MethodHandle.invokeGeneric checking code. */
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

    // Local constant functions:
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
                // Each nf must be statically invocable or we get tied up in our bootstraps.
                assert(InvokerBytecodeGenerator.isStaticallyInvocable(nf.member)) : nf;
                nf.resolve();
            }
        } catch (ReflectiveOperationException ex) {
            throw newInternalError(ex);
        }
    }


                private static class Lazy {
        // 定义一个静态的 MethodHandle 变量，用于存储 asSpreader 方法的句柄
        private static final MethodHandle MH_asSpreader;

        // 静态初始化块，用于初始化 MH_asSpreader 变量
        static {
            try {
                // 通过 IMPL_LOOKUP 查找 MethodHandle 类中的 asSpreader 方法，并将其句柄赋值给 MH_asSpreader
                MH_asSpreader = IMPL_LOOKUP.findVirtual(MethodHandle.class, "asSpreader",
                        MethodType.methodType(MethodHandle.class, Class.class, int.class));
            } catch (ReflectiveOperationException ex) {
                // 如果查找过程中发生异常，抛出一个新的内部错误
                throw newInternalError(ex);
            }
        }
    }
}
