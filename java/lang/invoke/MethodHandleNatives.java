
/*
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import static java.lang.invoke.MethodHandleNatives.Constants.*;
import static java.lang.invoke.MethodHandleStatics.*;
import static java.lang.invoke.MethodHandles.Lookup.IMPL_LOOKUP;

/**
 * The JVM interface for the method handles package is all here.
 * This is an interface internal and private to an implementation of JSR 292.
 * <em>This class is not part of the JSR 292 standard.</em>
 * @author jrose
 */
class MethodHandleNatives {

    private MethodHandleNatives() { } // static only

    /// MemberName support

    static native void init(MemberName self, Object ref);
    static native void expand(MemberName self);
    static native MemberName resolve(MemberName self, Class<?> caller) throws LinkageError, ClassNotFoundException;
    static native int getMembers(Class<?> defc, String matchName, String matchSig,
            int matchFlags, Class<?> caller, int skip, MemberName[] results);

    /// Field layout queries parallel to sun.misc.Unsafe:
    static native long objectFieldOffset(MemberName self);  // e.g., returns vmindex
    static native long staticFieldOffset(MemberName self);  // e.g., returns vmindex
    static native Object staticFieldBase(MemberName self);  // e.g., returns clazz
    static native Object getMemberVMInfo(MemberName self);  // returns {vmindex,vmtarget}

    /// MethodHandle support

    /** Fetch MH-related JVM parameter.
     *  which=0 retrieves MethodHandlePushLimit
     *  which=1 retrieves stack slot push size (in address units)
     */
    static native int getConstant(int which);

    static final boolean COUNT_GWT;

    /// CallSite support

    /** Tell the JVM that we need to change the target of a CallSite. */
    static native void setCallSiteTargetNormal(CallSite site, MethodHandle target);
    static native void setCallSiteTargetVolatile(CallSite site, MethodHandle target);

    private static native void registerNatives();
    static {
        registerNatives();
        COUNT_GWT                   = getConstant(Constants.GC_COUNT_GWT) != 0;

        // The JVM calls MethodHandleNatives.<clinit>.  Cascade the <clinit> calls as needed:
        MethodHandleImpl.initStatics();
    }

    // All compile-time constants go here.
    // There is an opportunity to check them against the JVM's idea of them.
    static class Constants {
        Constants() { } // static only
        // MethodHandleImpl
        static final int // for getConstant
                GC_COUNT_GWT = 4,
                GC_LAMBDA_SUPPORT = 5;

        // MemberName
        // The JVM uses values of -2 and above for vtable indexes.
        // Field values are simple positive offsets.
        // Ref: src/share/vm/oops/methodOop.hpp
        // This value is negative enough to avoid such numbers,
        // but not too negative.
        static final int
                MN_IS_METHOD           = 0x00010000, // method (not constructor)
                MN_IS_CONSTRUCTOR      = 0x00020000, // constructor
                MN_IS_FIELD            = 0x00040000, // field
                MN_IS_TYPE             = 0x00080000, // nested type
                MN_CALLER_SENSITIVE    = 0x00100000, // @CallerSensitive annotation detected
                MN_REFERENCE_KIND_SHIFT = 24, // refKind
                MN_REFERENCE_KIND_MASK = 0x0F000000 >> MN_REFERENCE_KIND_SHIFT,
                // The SEARCH_* bits are not for MN.flags but for the matchFlags argument of MHN.getMembers:
                MN_SEARCH_SUPERCLASSES = 0x00100000,
                MN_SEARCH_INTERFACES   = 0x00200000;

        /**
         * Basic types as encoded in the JVM.  These code values are not
         * intended for use outside this class.  They are used as part of
         * a private interface between the JVM and this class.
         */
        static final int
            T_BOOLEAN  =  4,
            T_CHAR     =  5,
            T_FLOAT    =  6,
            T_DOUBLE   =  7,
            T_BYTE     =  8,
            T_SHORT    =  9,
            T_INT      = 10,
            T_LONG     = 11,
            T_OBJECT   = 12,
            //T_ARRAY    = 13
            T_VOID     = 14,
            //T_ADDRESS  = 15
            T_ILLEGAL  = 99;

        /**
         * Constant pool entry types.
         */
        static final byte
            CONSTANT_Utf8                = 1,
            CONSTANT_Integer             = 3,
            CONSTANT_Float               = 4,
            CONSTANT_Long                = 5,
            CONSTANT_Double              = 6,
            CONSTANT_Class               = 7,
            CONSTANT_String              = 8,
            CONSTANT_Fieldref            = 9,
            CONSTANT_Methodref           = 10,
            CONSTANT_InterfaceMethodref  = 11,
            CONSTANT_NameAndType         = 12,
            CONSTANT_MethodHandle        = 15,  // JSR 292
            CONSTANT_MethodType          = 16,  // JSR 292
            CONSTANT_InvokeDynamic       = 18,
            CONSTANT_LIMIT               = 19;   // Limit to tags found in classfiles

        /**
         * Access modifier flags.
         */
        static final char
            ACC_PUBLIC                 = 0x0001,
            ACC_PRIVATE                = 0x0002,
            ACC_PROTECTED              = 0x0004,
            ACC_STATIC                 = 0x0008,
            ACC_FINAL                  = 0x0010,
            ACC_SYNCHRONIZED           = 0x0020,
            ACC_VOLATILE               = 0x0040,
            ACC_TRANSIENT              = 0x0080,
            ACC_NATIVE                 = 0x0100,
            ACC_INTERFACE              = 0x0200,
            ACC_ABSTRACT               = 0x0400,
            ACC_STRICT                 = 0x0800,
            ACC_SYNTHETIC              = 0x1000,
            ACC_ANNOTATION             = 0x2000,
            ACC_ENUM                   = 0x4000,
            // aliases:
            ACC_SUPER                  = ACC_SYNCHRONIZED,
            ACC_BRIDGE                 = ACC_VOLATILE,
            ACC_VARARGS                = ACC_TRANSIENT;


                    /**
         * 常量池引用类型代码，用于 CONSTANT_MethodHandle CP 条目。
         */
        static final byte
            REF_NONE                    = 0,  // null 值
            REF_getField                = 1,
            REF_getStatic               = 2,
            REF_putField                = 3,
            REF_putStatic               = 4,
            REF_invokeVirtual           = 5,
            REF_invokeStatic            = 6,
            REF_invokeSpecial           = 7,
            REF_newInvokeSpecial        = 8,
            REF_invokeInterface         = 9,
            REF_LIMIT                  = 10;
    }

    static boolean refKindIsValid(int refKind) {
        return (refKind > REF_NONE && refKind < REF_LIMIT);
    }
    static boolean refKindIsField(byte refKind) {
        assert(refKindIsValid(refKind));
        return (refKind <= REF_putStatic);
    }
    static boolean refKindIsGetter(byte refKind) {
        assert(refKindIsValid(refKind));
        return (refKind <= REF_getStatic);
    }
    static boolean refKindIsSetter(byte refKind) {
        return refKindIsField(refKind) && !refKindIsGetter(refKind);
    }
    static boolean refKindIsMethod(byte refKind) {
        return !refKindIsField(refKind) && (refKind != REF_newInvokeSpecial);
    }
    static boolean refKindIsConstructor(byte refKind) {
        return (refKind == REF_newInvokeSpecial);
    }
    static boolean refKindHasReceiver(byte refKind) {
        assert(refKindIsValid(refKind));
        return (refKind & 1) != 0;
    }
    static boolean refKindIsStatic(byte refKind) {
        return !refKindHasReceiver(refKind) && (refKind != REF_newInvokeSpecial);
    }
    static boolean refKindDoesDispatch(byte refKind) {
        assert(refKindIsValid(refKind));
        return (refKind == REF_invokeVirtual ||
                refKind == REF_invokeInterface);
    }
    static {
        final int HR_MASK = ((1 << REF_getField) |
                             (1 << REF_putField) |
                             (1 << REF_invokeVirtual) |
                             (1 << REF_invokeSpecial) |
                             (1 << REF_invokeInterface)
                            );
        for (byte refKind = REF_NONE+1; refKind < REF_LIMIT; refKind++) {
            assert(refKindHasReceiver(refKind) == (((1<<refKind) & HR_MASK) != 0)) : refKind;
        }
    }
    static String refKindName(byte refKind) {
        assert(refKindIsValid(refKind));
        switch (refKind) {
        case REF_getField:          return "getField";
        case REF_getStatic:         return "getStatic";
        case REF_putField:          return "putField";
        case REF_putStatic:         return "putStatic";
        case REF_invokeVirtual:     return "invokeVirtual";
        case REF_invokeStatic:      return "invokeStatic";
        case REF_invokeSpecial:     return "invokeSpecial";
        case REF_newInvokeSpecial:  return "newInvokeSpecial";
        case REF_invokeInterface:   return "invokeInterface";
        default:                    return "REF_???";
        }
    }

    private static native int getNamedCon(int which, Object[] name);
    static boolean verifyConstants() {
        Object[] box = { null };
        for (int i = 0; ; i++) {
            box[0] = null;
            int vmval = getNamedCon(i, box);
            if (box[0] == null)  break;
            String name = (String) box[0];
            try {
                Field con = Constants.class.getDeclaredField(name);
                int jval = con.getInt(null);
                if (jval == vmval)  continue;
                String err = (name+": JVM has "+vmval+" while Java has "+jval);
                if (name.equals("CONV_OP_LIMIT")) {
                    System.err.println("warning: "+err);
                    continue;
                }
                throw new InternalError(err);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                String err = (name+": JVM has "+vmval+" which Java does not define");
                // 忽略 JVM 关心的异域操作；我们只是不会发出它们
                //System.err.println("warning: "+err);
                continue;
            }
        }
        return true;
    }
    static {
        assert(verifyConstants());
    }

    // 来自 JVM 的上层调用。
    // 这些必须不是 public 的。

    /**
     * JVM 正在链接一个 invokedynamic 指令。为它创建一个具象的调用站点。
     */
    static MemberName linkCallSite(Object callerObj,
                                   Object bootstrapMethodObj,
                                   Object nameObj, Object typeObj,
                                   Object staticArguments,
                                   Object[] appendixResult) {
        MethodHandle bootstrapMethod = (MethodHandle)bootstrapMethodObj;
        Class<?> caller = (Class<?>)callerObj;
        String name = nameObj.toString().intern();
        MethodType type = (MethodType)typeObj;
        if (!TRACE_METHOD_LINKAGE)
            return linkCallSiteImpl(caller, bootstrapMethod, name, type,
                                    staticArguments, appendixResult);
        return linkCallSiteTracing(caller, bootstrapMethod, name, type,
                                   staticArguments, appendixResult);
    }
    static MemberName linkCallSiteImpl(Class<?> caller,
                                       MethodHandle bootstrapMethod,
                                       String name, MethodType type,
                                       Object staticArguments,
                                       Object[] appendixResult) {
        CallSite callSite = CallSite.makeSite(bootstrapMethod,
                                              name,
                                              type,
                                              staticArguments,
                                              caller);
        if (callSite instanceof ConstantCallSite) {
            appendixResult[0] = callSite.dynamicInvoker();
            return Invokers.linkToTargetMethod(type);
        } else {
            appendixResult[0] = callSite;
            return Invokers.linkToCallSiteMethod(type);
        }
    }
    // 跟踪逻辑：
    static MemberName linkCallSiteTracing(Class<?> caller,
                                          MethodHandle bootstrapMethod,
                                          String name, MethodType type,
                                          Object staticArguments,
                                          Object[] appendixResult) {
        Object bsmReference = bootstrapMethod.internalMemberName();
        if (bsmReference == null)  bsmReference = bootstrapMethod;
        Object staticArglist = (staticArguments instanceof Object[] ?
                                java.util.Arrays.asList((Object[]) staticArguments) :
                                staticArguments);
        System.out.println("linkCallSite "+caller.getName()+" "+
                           bsmReference+" "+
                           name+type+"/"+staticArglist);
        try {
            MemberName res = linkCallSiteImpl(caller, bootstrapMethod, name, type,
                                              staticArguments, appendixResult);
            System.out.println("linkCallSite => "+res+" + "+appendixResult[0]);
            return res;
        } catch (Throwable ex) {
            System.out.println("linkCallSite => throw "+ex);
            throw ex;
        }
    }


                /**
     * JVM 需要一个指向 MethodType 的指针。通过查找或创建一个来满足它的需求。
     */
    static MethodType findMethodHandleType(Class<?> rtype, Class<?>[] ptypes) {
        return MethodType.makeImpl(rtype, ptypes, true);
    }

    /**
     * JVM 需要链接一个需要动态类型检查的调用点。
     * 名称是一个类型检查调用者，如 invokeExact 或 invoke。
     * 返回一个 JVM 方法（MemberName）来处理调用。
     * 该方法假设堆栈上有以下参数：
     * 0: 被调用的方法句柄
     * 1-N: 方法句柄调用的参数
     * N+1: 一个可选的隐式添加的参数（通常是给定的 MethodType）
     * <p>
     * 这样的调用点的名义方法是一个签名多态方法（参见 @PolymorphicSignature）的实例。
     * 这样的方法实例是用户可见的实体，它们是从 {@code MethodHandle} 中的通用占位符方法“拆分”出来的。
     * （请注意，占位符方法与它的任何实例都不相同。如果反射地调用它，保证会抛出一个
     * {@code UnsupportedOperationException}。）
     * 如果签名多态方法实例被具体化，它将作为原始占位符的“副本”出现
     * （即 {@code MethodHandle} 的本地最终成员），但其类型描述符的形状由实例决定，
     * 且该方法实例 <em>不是</em> 可变参数。
     * 方法实例还被标记为合成的，因为该方法（按定义）不会出现在 Java 源代码中。
     * <p>
     * JVM 可以将此方法作为实例元数据具体化。
     * 例如，{@code invokeBasic} 总是被具体化。
     * 但 JVM 也可以调用 {@code linkMethod}。
     * 如果结果是一个 * 有序对 {@code (method, appendix)}，
     * 则该方法接收所有参数（0..N 包括 N）
     * 加上附录（N+1），并使用附录来完成调用。
     * 这样，一个可重用的方法（称为“链接器方法”）
     * 可以执行任意数量的多态实例方法的功能。
     * <p>
     * 链接器方法可以是弱类型的，任何或所有引用都可以重写为 {@code Object}，任何基本类型
     * （除了 {@code long}/{@code float}/{@code double}）
     * 都可以重写为 {@code int}。
     * 链接器方法被信任返回一个强类型的结果，
     * 根据它所模拟的签名多态实例的具体方法类型描述符。
     * 这可能涉及（必要时）使用从附录参数中提取的数据进行动态检查。
     * <p>
     * JVM 不检查附录，除了将其逐字传递给链接器方法外。
     * 这意味着 JDK 运行时在选择每个链接器方法及其
     * 对应附录的形状方面有很大的灵活性。
     * 链接器方法应从 {@code LambdaForm} 生成
     * 以便它们不会出现在堆栈跟踪中。
     * <p>
     * {@code linkMethod} 调用可以省略附录
     * （返回 null）并完全在链接器方法中模拟所需的功能。
     * 作为一个特殊情况，如果 N==255，不可能有附录。
     * 在这种情况下，返回的方法必须自定义生成以
     * 执行任何必要的类型检查。
     * <p>
     * 如果 JVM 在调用点不具体化一个方法，而是
     * 调用 {@code linkMethod}，则字节码中表示的相应调用可能提到一个有效的方法，该方法不能
     * 用 {@code MemberName} 表示。
     * 因此，{@code linkMethod} 的使用场景往往对应于
     * 反射代码中的特殊情况，如 {@code findVirtual}
     * 或 {@code revealDirect}。
     */
    static MemberName linkMethod(Class<?> callerClass, int refKind,
                                 Class<?> defc, String name, Object type,
                                 Object[] appendixResult) {
        if (!TRACE_METHOD_LINKAGE)
            return linkMethodImpl(callerClass, refKind, defc, name, type, appendixResult);
        return linkMethodTracing(callerClass, refKind, defc, name, type, appendixResult);
    }
    static MemberName linkMethodImpl(Class<?> callerClass, int refKind,
                                     Class<?> defc, String name, Object type,
                                     Object[] appendixResult) {
        try {
            if (defc == MethodHandle.class && refKind == REF_invokeVirtual) {
                return Invokers.methodHandleInvokeLinkerMethod(name, fixMethodType(callerClass, type), appendixResult);
            }
        } catch (Throwable ex) {
            if (ex instanceof LinkageError)
                throw (LinkageError) ex;
            else
                throw new LinkageError(ex.getMessage(), ex);
        }
        throw new LinkageError("no such method "+defc.getName()+"."+name+type);
    }
    private static MethodType fixMethodType(Class<?> callerClass, Object type) {
        if (type instanceof MethodType)
            return (MethodType) type;
        else
            return MethodType.fromMethodDescriptorString((String)type, callerClass.getClassLoader());
    }
    // 跟踪逻辑：
    static MemberName linkMethodTracing(Class<?> callerClass, int refKind,
                                        Class<?> defc, String name, Object type,
                                        Object[] appendixResult) {
        System.out.println("linkMethod "+defc.getName()+"."+
                           name+type+"/"+Integer.toHexString(refKind));
        try {
            MemberName res = linkMethodImpl(callerClass, refKind, defc, name, type, appendixResult);
            System.out.println("linkMethod => "+res+" + "+appendixResult[0]);
            return res;
        } catch (Throwable ex) {
            System.out.println("linkMethod => throw "+ex);
            throw ex;
        }
    }


    /**
     * JVM 正在解析一个 CONSTANT_MethodHandle 常量池条目。它需要我们的帮助。
     * 它将调用此方法。 （不要更改名称或签名。）
     * 类型参数对于字段请求是一个 Class，对于非字段是一个 MethodType。
     * <p>
     * 最近版本的 JVM 还可以为类型传递一个已解析的 MemberName。
     * 在这种情况下，名称被忽略，可以是 null。
     */
    static MethodHandle linkMethodHandleConstant(Class<?> callerClass, int refKind,
                                                 Class<?> defc, String name, Object type) {
        try {
            Lookup lookup = IMPL_LOOKUP.in(callerClass);
            assert(refKindIsValid(refKind));
            return lookup.linkMethodHandleConstant((byte) refKind, defc, name, type);
        } catch (IllegalAccessException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof AbstractMethodError) {
                throw (AbstractMethodError) cause;
            } else {
                Error err = new IllegalAccessError(ex.getMessage());
                throw initCauseFrom(err, ex);
            }
        } catch (NoSuchMethodException ex) {
            Error err = new NoSuchMethodError(ex.getMessage());
            throw initCauseFrom(err, ex);
        } catch (NoSuchFieldException ex) {
            Error err = new NoSuchFieldError(ex.getMessage());
            throw initCauseFrom(err, ex);
        } catch (ReflectiveOperationException ex) {
            Error err = new IncompatibleClassChangeError();
            throw initCauseFrom(err, ex);
        }
    }


                /**
     * 使用 err.initCause() 的最佳可能原因，如果原因具有相同（或更好）的类型，则用原因替换 err 本身。
     */
    static private Error initCauseFrom(Error err, Exception ex) {
        Throwable th = ex.getCause();
        if (err.getClass().isInstance(th))
           return (Error) th;
        err.initCause(th == null ? ex : th);
        return err;
    }

    /**
     * 这个方法是调用者敏感的方法吗？
     * 也就是说，它是否调用了 Reflection.getCallerClass 或类似的方法
     * 来询问其调用者的身份？
     */
    static boolean isCallerSensitive(MemberName mem) {
        if (!mem.isInvocable())  return false;  // 字段不是调用者敏感的

        return mem.isCallerSensitive() || canBeCalledVirtual(mem);
    }

    static boolean canBeCalledVirtual(MemberName mem) {
        assert(mem.isInvocable());
        Class<?> defc = mem.getDeclaringClass();
        switch (mem.getName()) {
        case "checkMemberAccess":
            return canBeCalledVirtual(mem, java.lang.SecurityManager.class);
        case "getContextClassLoader":
            return canBeCalledVirtual(mem, java.lang.Thread.class);
        }
        return false;
    }

    static boolean canBeCalledVirtual(MemberName symbolicRef, Class<?> definingClass) {
        Class<?> symbolicRefClass = symbolicRef.getDeclaringClass();
        if (symbolicRefClass == definingClass)  return true;
        if (symbolicRef.isStatic() || symbolicRef.isPrivate())  return false;
        return (definingClass.isAssignableFrom(symbolicRefClass) ||  // Msym 覆盖 Mdef
                symbolicRefClass.isInterface());                     // Mdef 实现 Msym
    }
}
