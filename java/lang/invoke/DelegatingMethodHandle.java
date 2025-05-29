/*
 * 版权所有 (c) 2014, Oracle 和/或其附属公司。保留所有权利。
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

import java.util.Arrays;
import static java.lang.invoke.LambdaForm.*;
import static java.lang.invoke.MethodHandleStatics.*;

/**
 * 一个方法句柄，其调用行为由目标确定。
 * 委托的方法句柄本身可以持有超出简单行为的额外“意图”。
 * @author jrose
 */
/*非公开*/
abstract class DelegatingMethodHandle extends MethodHandle {
    protected DelegatingMethodHandle(MethodHandle target) {
        this(target.type(), target);
    }

    protected DelegatingMethodHandle(MethodType type, MethodHandle target) {
        super(type, chooseDelegatingForm(target));
    }

    protected DelegatingMethodHandle(MethodType type, LambdaForm form) {
        super(type, form);
    }

    /** 定义此方法以提取提供调用行为的委托目标。 */
    abstract protected MethodHandle getTarget();

    @Override
    abstract MethodHandle asTypeUncached(MethodType newType);

    @Override
    MemberName internalMemberName() {
        return getTarget().internalMemberName();
    }

    @Override
    boolean isInvokeSpecial() {
        return getTarget().isInvokeSpecial();
    }

    @Override
    Class<?> internalCallerClass() {
        return getTarget().internalCallerClass();
    }

    @Override
    MethodHandle copyWith(MethodType mt, LambdaForm lf) {
        // FIXME: 重新考虑 'copyWith' 协议；它对于所有方法句柄来说太底层了
        throw newIllegalArgumentException("不要使用此方法");
    }

    @Override
    String internalProperties() {
        return "\n& Class="+getClass().getSimpleName()+
               "\n& Target="+getTarget().debugString();
    }

    @Override
    BoundMethodHandle rebind() {
        return getTarget().rebind();
    }

    private static LambdaForm chooseDelegatingForm(MethodHandle target) {
        if (target instanceof SimpleMethodHandle)
            return target.internalForm();  // 无需间接调用
        return makeReinvokerForm(target, MethodTypeForm.LF_DELEGATE, DelegatingMethodHandle.class, NF_getTarget);
    }

    static LambdaForm makeReinvokerForm(MethodHandle target,
                                        int whichCache,
                                        Object constraint,
                                        NamedFunction getTargetFn) {
        String debugString;
        switch(whichCache) {
            case MethodTypeForm.LF_REBIND:            debugString = "BMH.reinvoke";      break;
            case MethodTypeForm.LF_DELEGATE:          debugString = "MH.delegate";       break;
            default:                                  debugString = "MH.reinvoke";       break;
        }
        // 无需预操作。
        return makeReinvokerForm(target, whichCache, constraint, debugString, true, getTargetFn, null);
    }
    /** 创建一个简单的 LF，该 LF 仅重新调用给定基本类型的目标。 */
    static LambdaForm makeReinvokerForm(MethodHandle target,
                                        int whichCache,
                                        Object constraint,
                                        String debugString,
                                        boolean forceInline,
                                        NamedFunction getTargetFn,
                                        NamedFunction preActionFn) {
        MethodType mtype = target.type().basicType();
        boolean customized = (whichCache < 0 ||
                mtype.parameterSlotCount() > MethodType.MAX_MH_INVOKER_ARITY);
        boolean hasPreAction = (preActionFn != null);
        LambdaForm form;
        if (!customized) {
            form = mtype.form().cachedLambdaForm(whichCache);
            if (form != null)  return form;
        }
        final int THIS_DMH    = 0;
        final int ARG_BASE    = 1;
        final int ARG_LIMIT   = ARG_BASE + mtype.parameterCount();
        int nameCursor = ARG_LIMIT;
        final int PRE_ACTION   = hasPreAction ? nameCursor++ : -1;
        final int NEXT_MH     = customized ? -1 : nameCursor++;
        final int REINVOKE    = nameCursor++;
        LambdaForm.Name[] names = LambdaForm.arguments(nameCursor - ARG_LIMIT, mtype.invokerType());
        assert(names.length == nameCursor);
        names[THIS_DMH] = names[THIS_DMH].withConstraint(constraint);
        Object[] targetArgs;
        if (hasPreAction) {
            names[PRE_ACTION] = new LambdaForm.Name(preActionFn, names[THIS_DMH]);
        }
        if (customized) {
            targetArgs = Arrays.copyOfRange(names, ARG_BASE, ARG_LIMIT, Object[].class);
            names[REINVOKE] = new LambdaForm.Name(target, targetArgs);  // 调用者是目标本身
        } else {
            names[NEXT_MH] = new LambdaForm.Name(getTargetFn, names[THIS_DMH]);
            targetArgs = Arrays.copyOfRange(names, THIS_DMH, ARG_LIMIT, Object[].class);
            targetArgs[0] = names[NEXT_MH];  // 覆盖此 MH 为下一个 MH
            names[REINVOKE] = new LambdaForm.Name(mtype, targetArgs);
        }
        form = new LambdaForm(debugString, ARG_LIMIT, names, forceInline);
        if (!customized) {
            form = mtype.form().setCachedLambdaForm(whichCache, form);
        }
        return form;
    }

    static final NamedFunction NF_getTarget;
    static {
        try {
            NF_getTarget = new NamedFunction(DelegatingMethodHandle.class
                                             .getDeclaredMethod("getTarget"));
        } catch (ReflectiveOperationException ex) {
            throw newInternalError(ex);
        }
    }
}
