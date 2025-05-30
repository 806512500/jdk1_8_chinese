
/*
 * Copyright (c) 2008, 2017, Oracle and/or its affiliates. All rights reserved.
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

import sun.misc.Unsafe;
import java.lang.reflect.Method;
import java.util.Arrays;
import sun.invoke.util.VerifyAccess;
import static java.lang.invoke.MethodHandleNatives.Constants.*;
import static java.lang.invoke.LambdaForm.*;
import static java.lang.invoke.MethodTypeForm.*;
import static java.lang.invoke.MethodHandleStatics.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import sun.invoke.util.ValueConversions;
import sun.invoke.util.VerifyType;
import sun.invoke.util.Wrapper;

/**
 * 实现对类成员的常量引用的方法句柄的变体。
 * @author jrose
 */
class DirectMethodHandle extends MethodHandle {
    final MemberName member;

    // 本类中的构造函数和工厂方法必须是包级作用域或私有的。
    private DirectMethodHandle(MethodType mtype, LambdaForm form, MemberName member) {
        super(mtype, form);
        if (!member.isResolved())  throw new InternalError();

        if (member.getDeclaringClass().isInterface() &&
                member.isMethod() && !member.isAbstract()) {
            // 检查特殊情况：调用接口的 Object 方法
            MemberName m = new MemberName(Object.class, member.getName(), member.getMethodType(), member.getReferenceKind());
            m = MemberName.getFactory().resolveOrNull(m.getReferenceKind(), m, null);
            if (m != null && m.isPublic()) {
                assert(member.getReferenceKind() == m.getReferenceKind());  // 否则 this.form 是错误的
                member = m;
            }
        }

        this.member = member;
    }

    // 工厂方法：
    static DirectMethodHandle make(byte refKind, Class<?> receiver, MemberName member) {
        MethodType mtype = member.getMethodOrFieldType();
        if (!member.isStatic()) {
            if (!member.getDeclaringClass().isAssignableFrom(receiver) || member.isConstructor())
                throw new InternalError(member.toString());
            mtype = mtype.insertParameterTypes(0, receiver);
        }
        if (!member.isField()) {
            switch (refKind) {
                case REF_invokeSpecial: {
                    member = member.asSpecial();
                    LambdaForm lform = preparedLambdaForm(member);
                    return new Special(mtype, lform, member);
                }
                case REF_invokeInterface: {
                    LambdaForm lform = preparedLambdaForm(member);
                    return new Interface(mtype, lform, member, receiver);
                }
                default: {
                    LambdaForm lform = preparedLambdaForm(member);
                    return new DirectMethodHandle(mtype, lform, member);
                }
            }
        } else {
            LambdaForm lform = preparedFieldLambdaForm(member);
            if (member.isStatic()) {
                long offset = MethodHandleNatives.staticFieldOffset(member);
                Object base = MethodHandleNatives.staticFieldBase(member);
                return new StaticAccessor(mtype, lform, member, base, offset);
            } else {
                long offset = MethodHandleNatives.objectFieldOffset(member);
                assert(offset == (int)offset);
                return new Accessor(mtype, lform, member, (int)offset);
            }
        }
    }
    static DirectMethodHandle make(Class<?> receiver, MemberName member) {
        byte refKind = member.getReferenceKind();
        if (refKind == REF_invokeSpecial)
            refKind =  REF_invokeVirtual;
        return make(refKind, receiver, member);
    }
    static DirectMethodHandle make(MemberName member) {
        if (member.isConstructor())
            return makeAllocator(member);
        return make(member.getDeclaringClass(), member);
    }
    static DirectMethodHandle make(Method method) {
        return make(method.getDeclaringClass(), new MemberName(method));
    }
    static DirectMethodHandle make(Field field) {
        return make(field.getDeclaringClass(), new MemberName(field));
    }
    private static DirectMethodHandle makeAllocator(MemberName ctor) {
        assert(ctor.isConstructor() && ctor.getName().equals("<init>"));
        Class<?> instanceClass = ctor.getDeclaringClass();
        ctor = ctor.asConstructor();
        assert(ctor.isConstructor() && ctor.getReferenceKind() == REF_newInvokeSpecial) : ctor;
        MethodType mtype = ctor.getMethodType().changeReturnType(instanceClass);
        LambdaForm lform = preparedLambdaForm(ctor);
        MemberName init = ctor.asSpecial();
        assert(init.getMethodType().returnType() == void.class);
        return new Constructor(mtype, lform, ctor, init, instanceClass);
    }

    @Override
    BoundMethodHandle rebind() {
        return BoundMethodHandle.makeReinvoker(this);
    }

    @Override
    MethodHandle copyWith(MethodType mt, LambdaForm lf) {
        assert(this.getClass() == DirectMethodHandle.class);  // 必须在子类中重写
        return new DirectMethodHandle(mt, lf, member);
    }

    @Override
    String internalProperties() {
        return "\n& DMH.MN="+internalMemberName();
    }

    //// 实现方法。
    @Override
    @ForceInline
    MemberName internalMemberName() {
        return member;
    }

    private static final MemberName.Factory IMPL_NAMES = MemberName.getFactory();

    /**
     * 创建一个可以调用给定方法的 LF。
     * 在具有相同 basicType 和 refKind 的所有方法之间缓存并共享此结构。
     */
    private static LambdaForm preparedLambdaForm(MemberName m) {
        assert(m.isInvocable()) : m;  // 调用 preparedFieldLambdaForm 代替
        MethodType mtype = m.getInvocationType().basicType();
        assert(!m.isMethodHandleInvoke()) : m;
        int which;
        switch (m.getReferenceKind()) {
        case REF_invokeVirtual:    which = LF_INVVIRTUAL;    break;
        case REF_invokeStatic:     which = LF_INVSTATIC;     break;
        case REF_invokeSpecial:    which = LF_INVSPECIAL;    break;
        case REF_invokeInterface:  which = LF_INVINTERFACE;  break;
        case REF_newInvokeSpecial: which = LF_NEWINVSPECIAL; break;
        default:  throw new InternalError(m.toString());
        }
        if (which == LF_INVSTATIC && shouldBeInitialized(m)) {
            // 预计算无屏障版本：
            preparedLambdaForm(mtype, which);
            which = LF_INVSTATIC_INIT;
        }
        LambdaForm lform = preparedLambdaForm(mtype, which);
        maybeCompile(lform, m);
        assert(lform.methodType().dropParameterTypes(0, 1)
                .equals(m.getInvocationType().basicType()))
                : Arrays.asList(m, m.getInvocationType().basicType(), lform, lform.methodType());
        return lform;
    }

    private static LambdaForm preparedLambdaForm(MethodType mtype, int which) {
        LambdaForm lform = mtype.form().cachedLambdaForm(which);
        if (lform != null)  return lform;
        lform = makePreparedLambdaForm(mtype, which);
        return mtype.form().setCachedLambdaForm(which, lform);
    }

    private static LambdaForm makePreparedLambdaForm(MethodType mtype, int which) {
        boolean needsInit = (which == LF_INVSTATIC_INIT);
        boolean doesAlloc = (which == LF_NEWINVSPECIAL);
        boolean needsReceiverCheck = (which == LF_INVINTERFACE);
        String linkerName, lambdaName;
        switch (which) {
        case LF_INVVIRTUAL:    linkerName = "linkToVirtual";    lambdaName = "DMH.invokeVirtual";    break;
        case LF_INVSTATIC:     linkerName = "linkToStatic";     lambdaName = "DMH.invokeStatic";     break;
        case LF_INVSTATIC_INIT:linkerName = "linkToStatic";     lambdaName = "DMH.invokeStaticInit"; break;
        case LF_INVSPECIAL:    linkerName = "linkToSpecial";    lambdaName = "DMH.invokeSpecial";    break;
        case LF_INVINTERFACE:  linkerName = "linkToInterface";  lambdaName = "DMH.invokeInterface";  break;
        case LF_NEWINVSPECIAL: linkerName = "linkToSpecial";    lambdaName = "DMH.newInvokeSpecial"; break;
        default:  throw new InternalError("which="+which);
        }
        MethodType mtypeWithArg = mtype.appendParameterTypes(MemberName.class);
        if (doesAlloc)
            mtypeWithArg = mtypeWithArg
                    .insertParameterTypes(0, Object.class)  // 插入新分配的对象
                    .changeReturnType(void.class);          // <init> 返回 void
        MemberName linker = new MemberName(MethodHandle.class, linkerName, mtypeWithArg, REF_invokeStatic);
        try {
            linker = IMPL_NAMES.resolveOrFail(REF_invokeStatic, linker, null, NoSuchMethodException.class);
        } catch (ReflectiveOperationException ex) {
            throw newInternalError(ex);
        }
        final int DMH_THIS    = 0;
        final int ARG_BASE    = 1;
        final int ARG_LIMIT   = ARG_BASE + mtype.parameterCount();
        int nameCursor = ARG_LIMIT;
        final int NEW_OBJ     = (doesAlloc ? nameCursor++ : -1);
        final int GET_MEMBER  = nameCursor++;
        final int CHECK_RECEIVER = (needsReceiverCheck ? nameCursor++ : -1);
        final int LINKER_CALL = nameCursor++;
        Name[] names = arguments(nameCursor - ARG_LIMIT, mtype.invokerType());
        assert(names.length == nameCursor);
        if (doesAlloc) {
            // names = { argx,y,z,... new C, init method }
            names[NEW_OBJ] = new Name(Lazy.NF_allocateInstance, names[DMH_THIS]);
            names[GET_MEMBER] = new Name(Lazy.NF_constructorMethod, names[DMH_THIS]);
        } else if (needsInit) {
            names[GET_MEMBER] = new Name(Lazy.NF_internalMemberNameEnsureInit, names[DMH_THIS]);
        } else {
            names[GET_MEMBER] = new Name(Lazy.NF_internalMemberName, names[DMH_THIS]);
        }
        assert(findDirectMethodHandle(names[GET_MEMBER]) == names[DMH_THIS]);
        Object[] outArgs = Arrays.copyOfRange(names, ARG_BASE, GET_MEMBER+1, Object[].class);
        if (needsReceiverCheck) {
            names[CHECK_RECEIVER] = new Name(Lazy.NF_checkReceiver, names[DMH_THIS], names[ARG_BASE]);
            outArgs[0] = names[CHECK_RECEIVER];
        }
        assert(outArgs[outArgs.length-1] == names[GET_MEMBER]);  // 查看，参数已移位！
        int result = LAST_RESULT;
        if (doesAlloc) {
            assert(outArgs[outArgs.length-2] == names[NEW_OBJ]);  // 需要移动这个
            System.arraycopy(outArgs, 0, outArgs, 1, outArgs.length-2);
            outArgs[0] = names[NEW_OBJ];
            result = NEW_OBJ;
        }
        names[LINKER_CALL] = new Name(linker, outArgs);
        lambdaName += "_" + shortenSignature(basicTypeSignature(mtype));
        LambdaForm lform = new LambdaForm(lambdaName, ARG_LIMIT, names, result);
        // 这是一段棘手的代码。不要通过 LF 解释器发送。
        lform.compileToBytecode();
        return lform;
    }

    static Object findDirectMethodHandle(Name name) {
        if (name.function == Lazy.NF_internalMemberName ||
            name.function == Lazy.NF_internalMemberNameEnsureInit ||
            name.function == Lazy.NF_constructorMethod) {
            assert(name.arguments.length == 1);
            return name.arguments[0];
        }
        return null;
    }

    private static void maybeCompile(LambdaForm lform, MemberName m) {
        if (VerifyAccess.isSamePackage(m.getDeclaringClass(), MethodHandle.class))
            // 帮助引导...
            lform.compileToBytecode();
    }

    /** DirectMethodHandle.internalMemberName 的静态包装器。 */
    @ForceInline
    /*non-public*/ static Object internalMemberName(Object mh) {
        return ((DirectMethodHandle)mh).member;
    }

    /** DirectMethodHandle.internalMemberName 的静态包装器。
     * 这个方法还强制初始化。
     */
    /*non-public*/ static Object internalMemberNameEnsureInit(Object mh) {
        DirectMethodHandle dmh = (DirectMethodHandle)mh;
        dmh.ensureInitialized();
        return dmh.member;
    }

    /*non-public*/ static
    boolean shouldBeInitialized(MemberName member) {
        switch (member.getReferenceKind()) {
        case REF_invokeStatic:
        case REF_getStatic:
        case REF_putStatic:
        case REF_newInvokeSpecial:
            break;
        default:
            // 无需在此类成员上初始化类。
            return false;
        }
        Class<?> cls = member.getDeclaringClass();
        if (cls == ValueConversions.class ||
            cls == MethodHandleImpl.class ||
            cls == Invokers.class) {
            // 这些类有很多 <clinit> DMH 创建，但我们知道
            // 直到系统启动后才会使用这些 MH。
            return false;
        }
        if (VerifyAccess.isSamePackage(MethodHandle.class, cls) ||
            VerifyAccess.isSamePackage(ValueConversions.class, cls)) {
            // 这是一个系统类。它可能正在初始化，但为了安全起见，我们会帮助它。
            if (UNSAFE.shouldBeInitialized(cls)) {
                UNSAFE.ensureClassInitialized(cls);
            }
            return false;
        }
        return UNSAFE.shouldBeInitialized(cls);
    }

    private static class EnsureInitialized extends ClassValue<WeakReference<Thread>> {
        @Override
        protected WeakReference<Thread> computeValue(Class<?> type) {
            UNSAFE.ensureClassInitialized(type);
            if (UNSAFE.shouldBeInitialized(type))
                // 如果上一个调用没有阻塞，这可能会发生。
                // 我们正在执行 <clinit>。
                return new WeakReference<>(Thread.currentThread());
            return null;
        }
        static final EnsureInitialized INSTANCE = new EnsureInitialized();
    }

    private void ensureInitialized() {
        if (checkInitialized(member)) {
            // 没有障碍。删除 <clinit> 障碍。
            if (member.isField())
                updateForm(preparedFieldLambdaForm(member));
            else
                updateForm(preparedLambdaForm(member));
        }
    }
    private static boolean checkInitialized(MemberName member) {
        Class<?> defc = member.getDeclaringClass();
        WeakReference<Thread> ref = EnsureInitialized.INSTANCE.get(defc);
        if (ref == null) {
            return true;  // 最终状态
        }
        Thread clinitThread = ref.get();
        // 可能有人正在运行 defc.<clinit>。
        if (clinitThread == Thread.currentThread()) {
            // 如果有人正在运行 defc.<clinit>，那就是这个线程。
            if (UNSAFE.shouldBeInitialized(defc))
                // 是的，我们正在运行它；暂时保留障碍。
                return false;
        } else {
            // 我们在一个随机线程中。阻塞。
            UNSAFE.ensureClassInitialized(defc);
        }
        assert(!UNSAFE.shouldBeInitialized(defc));
        // 进入最终状态
        EnsureInitialized.INSTANCE.remove(defc);
        return true;
    }


                /*non-public*/ static void ensureInitialized(Object mh) {
        ((DirectMethodHandle)mh).ensureInitialized();
    }

    /** 此子类表示 invokespecial 指令。 */
    static class Special extends DirectMethodHandle {
        private Special(MethodType mtype, LambdaForm form, MemberName member) {
            super(mtype, form, member);
        }
        @Override
        boolean isInvokeSpecial() {
            return true;
        }
        @Override
        MethodHandle copyWith(MethodType mt, LambdaForm lf) {
            return new Special(mt, lf, member);
        }
    }

    /** 此子类表示 invokeinterface 指令。 */
    static class Interface extends DirectMethodHandle {
        private final Class<?> refc;
        private Interface(MethodType mtype, LambdaForm form, MemberName member, Class<?> refc) {
            super(mtype, form, member);
            assert refc.isInterface() : refc;
            this.refc = refc;
        }
        @Override
        MethodHandle copyWith(MethodType mt, LambdaForm lf) {
            return new Interface(mt, lf, member, refc);
        }

        Object checkReceiver(Object recv) {
            if (!refc.isInstance(recv)) {
                String msg = String.format("类 %s 不实现请求的接口 %s",
                        recv.getClass().getName(), refc.getName());
                throw new IncompatibleClassChangeError(msg);
            }
            return recv;
        }
    }

    /** 此子类处理构造函数引用。 */
    static class Constructor extends DirectMethodHandle {
        final MemberName initMethod;
        final Class<?>   instanceClass;

        private Constructor(MethodType mtype, LambdaForm form, MemberName constructor,
                            MemberName initMethod, Class<?> instanceClass) {
            super(mtype, form, constructor);
            this.initMethod = initMethod;
            this.instanceClass = instanceClass;
            assert(initMethod.isResolved());
        }
        @Override
        MethodHandle copyWith(MethodType mt, LambdaForm lf) {
            return new Constructor(mt, lf, member, initMethod, instanceClass);
        }
    }

    /*non-public*/ static Object constructorMethod(Object mh) {
        Constructor dmh = (Constructor)mh;
        return dmh.initMethod;
    }

    /*non-public*/ static Object allocateInstance(Object mh) throws InstantiationException {
        Constructor dmh = (Constructor)mh;
        return UNSAFE.allocateInstance(dmh.instanceClass);
    }

    /** 此子类处理非静态字段引用。 */
    static class Accessor extends DirectMethodHandle {
        final Class<?> fieldType;
        final int      fieldOffset;
        private Accessor(MethodType mtype, LambdaForm form, MemberName member,
                         int fieldOffset) {
            super(mtype, form, member);
            this.fieldType   = member.getFieldType();
            this.fieldOffset = fieldOffset;
        }

        @Override Object checkCast(Object obj) {
            return fieldType.cast(obj);
        }
        @Override
        MethodHandle copyWith(MethodType mt, LambdaForm lf) {
            return new Accessor(mt, lf, member, fieldOffset);
        }
    }

    @ForceInline
    /*non-public*/ static long fieldOffset(Object accessorObj) {
        // 注意：我们返回一个 long，因为这是 Unsafe.getObject 所需要的。
        // 我们存储一个普通的 int，因为它更紧凑。
        return ((Accessor)accessorObj).fieldOffset;
    }

    @ForceInline
    /*non-public*/ static Object checkBase(Object obj) {
        // 注意，对象的类已经验证过，
        // 因为 Accessor 方法句柄的参数类型
        // 是 member.getDeclaringClass 或其子类。
        // 这在 DirectMethodHandle.make 中已经验证。
        // 因此，唯一剩下的检查是 null。
        // 由于 Unsafe.getInt 及其同类方法
        // 不能保证这一点，因此需要在这里进行显式检查。
        obj.getClass();  // 可能抛出 NPE
        return obj;
    }

    /** 此子类处理静态字段引用。 */
    static class StaticAccessor extends DirectMethodHandle {
        final private Class<?> fieldType;
        final private Object   staticBase;
        final private long     staticOffset;

        private StaticAccessor(MethodType mtype, LambdaForm form, MemberName member,
                               Object staticBase, long staticOffset) {
            super(mtype, form, member);
            this.fieldType    = member.getFieldType();
            this.staticBase   = staticBase;
            this.staticOffset = staticOffset;
        }

        @Override Object checkCast(Object obj) {
            return fieldType.cast(obj);
        }
        @Override
        MethodHandle copyWith(MethodType mt, LambdaForm lf) {
            return new StaticAccessor(mt, lf, member, staticBase, staticOffset);
        }
    }

    @ForceInline
    /*non-public*/ static Object nullCheck(Object obj) {
        obj.getClass();
        return obj;
    }

    @ForceInline
    /*non-public*/ static Object staticBase(Object accessorObj) {
        return ((StaticAccessor)accessorObj).staticBase;
    }

    @ForceInline
    /*non-public*/ static long staticOffset(Object accessorObj) {
        return ((StaticAccessor)accessorObj).staticOffset;
    }

    @ForceInline
    /*non-public*/ static Object checkCast(Object mh, Object obj) {
        return ((DirectMethodHandle) mh).checkCast(obj);
    }

    Object checkCast(Object obj) {
        return member.getReturnType().cast(obj);
    }

    // 字段访问器的缓存机制：
    private static byte
            AF_GETFIELD        = 0,
            AF_PUTFIELD        = 1,
            AF_GETSTATIC       = 2,
            AF_PUTSTATIC       = 3,
            AF_GETSTATIC_INIT  = 4,
            AF_PUTSTATIC_INIT  = 5,
            AF_LIMIT           = 6;
    // 使用 Wrapper 枚举不同的字段类型，
    // 并添加一个额外的 case 用于检查引用。
    private static int
            FT_LAST_WRAPPER    = Wrapper.values().length-1,
            FT_UNCHECKED_REF   = Wrapper.OBJECT.ordinal(),
            FT_CHECKED_REF     = FT_LAST_WRAPPER+1,
            FT_LIMIT           = FT_LAST_WRAPPER+2;
    private static int afIndex(byte formOp, boolean isVolatile, int ftypeKind) {
        return ((formOp * FT_LIMIT * 2)
                + (isVolatile ? FT_LIMIT : 0)
                + ftypeKind);
    }
    private static final LambdaForm[] ACCESSOR_FORMS
            = new LambdaForm[afIndex(AF_LIMIT, false, 0)];
    private static int ftypeKind(Class<?> ftype) {
        if (ftype.isPrimitive())
            return Wrapper.forPrimitiveType(ftype).ordinal();
        else if (VerifyType.isNullReferenceConversion(Object.class, ftype))
            return FT_UNCHECKED_REF;
        else
            return FT_CHECKED_REF;
    }

    /**
     * 创建一个可以访问给定字段的 LF。
     * 在具有相同 basicType 和 refKind 的所有字段之间缓存和共享此结构。
     */
    private static LambdaForm preparedFieldLambdaForm(MemberName m) {
        Class<?> ftype = m.getFieldType();
        boolean isVolatile = m.isVolatile();
        byte formOp;
        switch (m.getReferenceKind()) {
        case REF_getField:      formOp = AF_GETFIELD;    break;
        case REF_putField:      formOp = AF_PUTFIELD;    break;
        case REF_getStatic:     formOp = AF_GETSTATIC;   break;
        case REF_putStatic:     formOp = AF_PUTSTATIC;   break;
        default:  throw new InternalError(m.toString());
        }
        if (shouldBeInitialized(m)) {
            // 预计算无屏障版本：
            preparedFieldLambdaForm(formOp, isVolatile, ftype);
            assert((AF_GETSTATIC_INIT - AF_GETSTATIC) ==
                   (AF_PUTSTATIC_INIT - AF_PUTSTATIC));
            formOp += (AF_GETSTATIC_INIT - AF_GETSTATIC);
        }
        LambdaForm lform = preparedFieldLambdaForm(formOp, isVolatile, ftype);
        maybeCompile(lform, m);
        assert(lform.methodType().dropParameterTypes(0, 1)
                .equals(m.getInvocationType().basicType()))
                : Arrays.asList(m, m.getInvocationType().basicType(), lform, lform.methodType());
        return lform;
    }
    private static LambdaForm preparedFieldLambdaForm(byte formOp, boolean isVolatile, Class<?> ftype) {
        int afIndex = afIndex(formOp, isVolatile, ftypeKind(ftype));
        LambdaForm lform = ACCESSOR_FORMS[afIndex];
        if (lform != null)  return lform;
        lform = makePreparedFieldLambdaForm(formOp, isVolatile, ftypeKind(ftype));
        ACCESSOR_FORMS[afIndex] = lform;  // 不使用 CAS
        return lform;
    }

    private static LambdaForm makePreparedFieldLambdaForm(byte formOp, boolean isVolatile, int ftypeKind) {
        boolean isGetter  = (formOp & 1) == (AF_GETFIELD & 1);
        boolean isStatic  = (formOp >= AF_GETSTATIC);
        boolean needsInit = (formOp >= AF_GETSTATIC_INIT);
        boolean needsCast = (ftypeKind == FT_CHECKED_REF);
        Wrapper fw = (needsCast ? Wrapper.OBJECT : Wrapper.values()[ftypeKind]);
        Class<?> ft = fw.primitiveType();
        assert(ftypeKind(needsCast ? String.class : ft) == ftypeKind);
        String tname  = fw.primitiveSimpleName();
        String ctname = Character.toUpperCase(tname.charAt(0)) + tname.substring(1);
        if (isVolatile)  ctname += "Volatile";
        String getOrPut = (isGetter ? "get" : "put");
        String linkerName = (getOrPut + ctname);  // getObject, putIntVolatile, etc.
        MethodType linkerType;
        if (isGetter)
            linkerType = MethodType.methodType(ft, Object.class, long.class);
        else
            linkerType = MethodType.methodType(void.class, Object.class, long.class, ft);
        MemberName linker = new MemberName(Unsafe.class, linkerName, linkerType, REF_invokeVirtual);
        try {
            linker = IMPL_NAMES.resolveOrFail(REF_invokeVirtual, linker, null, NoSuchMethodException.class);
        } catch (ReflectiveOperationException ex) {
            throw newInternalError(ex);
        }

        // Lambda 形式的外部类型是什么？
        MethodType mtype;
        if (isGetter)
            mtype = MethodType.methodType(ft);
        else
            mtype = MethodType.methodType(void.class, ft);
        mtype = mtype.basicType();  // 将 short 转换为 int 等
        if (!isStatic)
            mtype = mtype.insertParameterTypes(0, Object.class);
        final int DMH_THIS  = 0;
        final int ARG_BASE  = 1;
        final int ARG_LIMIT = ARG_BASE + mtype.parameterCount();
        // 如果这是非静态访问，基指针存储在此索引处：
        final int OBJ_BASE  = isStatic ? -1 : ARG_BASE;
        // 如果这是写访问，要写入的值存储在此索引处：
        final int SET_VALUE  = isGetter ? -1 : ARG_LIMIT - 1;
        int nameCursor = ARG_LIMIT;
        final int F_HOLDER  = (isStatic ? nameCursor++ : -1);  // 静态基（如果有）
        final int F_OFFSET  = nameCursor++;  // 静态偏移量或字段偏移量。
        final int OBJ_CHECK = (OBJ_BASE >= 0 ? nameCursor++ : -1);
        final int INIT_BAR  = (needsInit ? nameCursor++ : -1);
        final int PRE_CAST  = (needsCast && !isGetter ? nameCursor++ : -1);
        final int LINKER_CALL = nameCursor++;
        final int POST_CAST = (needsCast && isGetter ? nameCursor++ : -1);
        final int RESULT    = nameCursor-1;  // 调用或转换
        Name[] names = arguments(nameCursor - ARG_LIMIT, mtype.invokerType());
        if (needsInit)
            names[INIT_BAR] = new Name(Lazy.NF_ensureInitialized, names[DMH_THIS]);
        if (needsCast && !isGetter)
            names[PRE_CAST] = new Name(Lazy.NF_checkCast, names[DMH_THIS], names[SET_VALUE]);
        Object[] outArgs = new Object[1 + linkerType.parameterCount()];
        assert(outArgs.length == (isGetter ? 3 : 4));
        outArgs[0] = UNSAFE;
        if (isStatic) {
            outArgs[1] = names[F_HOLDER]  = new Name(Lazy.NF_staticBase, names[DMH_THIS]);
            outArgs[2] = names[F_OFFSET]  = new Name(Lazy.NF_staticOffset, names[DMH_THIS]);
        } else {
            outArgs[1] = names[OBJ_CHECK] = new Name(Lazy.NF_checkBase, names[OBJ_BASE]);
            outArgs[2] = names[F_OFFSET]  = new Name(Lazy.NF_fieldOffset, names[DMH_THIS]);
        }
        if (!isGetter) {
            outArgs[3] = (needsCast ? names[PRE_CAST] : names[SET_VALUE]);
        }
        for (Object a : outArgs)  assert(a != null);
        names[LINKER_CALL] = new Name(linker, outArgs);
        if (needsCast && isGetter)
            names[POST_CAST] = new Name(Lazy.NF_checkCast, names[DMH_THIS], names[LINKER_CALL]);
        for (Name n : names)  assert(n != null);
        String fieldOrStatic = (isStatic ? "Static" : "Field");
        String lambdaName = (linkerName + fieldOrStatic);  // 仅用于调试
        if (needsCast)  lambdaName += "Cast";
        if (needsInit)  lambdaName += "Init";
        return new LambdaForm(lambdaName, ARG_LIMIT, names, RESULT);
    }

    /**
     * 用于引导目的的预初始化 NamedFunctions。
     * 为了延迟初始化直到首次使用，将其分解为一个内部类。
     */
    private static class Lazy {
        static final NamedFunction
                NF_internalMemberName,
                NF_internalMemberNameEnsureInit,
                NF_ensureInitialized,
                NF_fieldOffset,
                NF_checkBase,
                NF_staticBase,
                NF_staticOffset,
                NF_checkCast,
                NF_allocateInstance,
                NF_constructorMethod,
                NF_checkReceiver;
        static {
            try {
                NamedFunction nfs[] = {
                        NF_internalMemberName = new NamedFunction(DirectMethodHandle.class
                                .getDeclaredMethod("internalMemberName", Object.class)),
                        NF_internalMemberNameEnsureInit = new NamedFunction(DirectMethodHandle.class
                                .getDeclaredMethod("internalMemberNameEnsureInit", Object.class)),
                        NF_ensureInitialized = new NamedFunction(DirectMethodHandle.class
                                .getDeclaredMethod("ensureInitialized", Object.class)),
                        NF_fieldOffset = new NamedFunction(DirectMethodHandle.class
                                .getDeclaredMethod("fieldOffset", Object.class)),
                        NF_checkBase = new NamedFunction(DirectMethodHandle.class
                                .getDeclaredMethod("checkBase", Object.class)),
                        NF_staticBase = new NamedFunction(DirectMethodHandle.class
                                .getDeclaredMethod("staticBase", Object.class)),
                        NF_staticOffset = new NamedFunction(DirectMethodHandle.class
                                .getDeclaredMethod("staticOffset", Object.class)),
                        NF_checkCast = new NamedFunction(DirectMethodHandle.class
                                .getDeclaredMethod("checkCast", Object.class, Object.class)),
                        NF_allocateInstance = new NamedFunction(DirectMethodHandle.class
                                .getDeclaredMethod("allocateInstance", Object.class)),
                        NF_constructorMethod = new NamedFunction(DirectMethodHandle.class
                                .getDeclaredMethod("constructorMethod", Object.class)),
                        NF_checkReceiver = new NamedFunction(new MemberName(Interface.class
                                .getDeclaredMethod("checkReceiver", Object.class)))
                };
                for (NamedFunction nf : nfs) {
                    // 每个 nf 必须是静态可调用的，否则我们会陷入自引导。
                    assert(InvokerBytecodeGenerator.isStaticallyInvocable(nf.member)) : nf;
                    nf.resolve();
                }
            } catch (ReflectiveOperationException ex) {
                throw newInternalError(ex);
            }
        }
    }
}
