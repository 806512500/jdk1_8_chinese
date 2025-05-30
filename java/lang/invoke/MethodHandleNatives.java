
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

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import static java.lang.invoke.MethodHandleNatives.Constants.*;
import static java.lang.invoke.MethodHandleStatics.*;
import static java.lang.invoke.MethodHandles.Lookup.IMPL_LOOKUP;

/**
 * JVM接口用于方法句柄包，这是JSR 292实现的内部和私有接口。
 * <em>此类不是JSR 292标准的一部分。</em>
 * @author jrose
 */
class MethodHandleNatives {

    private MethodHandleNatives() { } // 静态方法专用

    /// MemberName 支持

    static native void init(MemberName self, Object ref);
    static native void expand(MemberName self);
    static native MemberName resolve(MemberName self, Class<?> caller) throws LinkageError, ClassNotFoundException;
    static native int getMembers(Class<?> defc, String matchName, String matchSig,
            int matchFlags, Class<?> caller, int skip, MemberName[] results);

    /// 字段布局查询，类似于sun.misc.Unsafe：
    static native long objectFieldOffset(MemberName self);  // 例如，返回vmindex
    static native long staticFieldOffset(MemberName self);  // 例如，返回vmindex
    static native Object staticFieldBase(MemberName self);  // 例如，返回clazz
    static native Object getMemberVMInfo(MemberName self);  // 返回 {vmindex,vmtarget}

    /// MethodHandle 支持

    /** 获取与MH相关的JVM参数。
     *  which=0 获取 MethodHandlePushLimit
     *  which=1 获取堆栈槽推送大小（以地址单位计）
     */
    static native int getConstant(int which);

    static final boolean COUNT_GWT;

    /// CallSite 支持

    /** 告诉JVM我们需要更改CallSite的目标。 */
    static native void setCallSiteTargetNormal(CallSite site, MethodHandle target);
    static native void setCallSiteTargetVolatile(CallSite site, MethodHandle target);

    private static native void registerNatives();
    static {
        registerNatives();
        COUNT_GWT                   = getConstant(Constants.GC_COUNT_GWT) != 0;

        // JVM调用 MethodHandleNatives.<clinit>。根据需要级联<clinit>调用：
        MethodHandleImpl.initStatics();
    }

    // 所有编译时常量都放在这里。
    // 有机会检查它们是否与JVM中的概念一致。
    static class Constants {
        Constants() { } // 静态方法专用
        // MethodHandleImpl
        static final int // 用于 getConstant
                GC_COUNT_GWT = 4,
                GC_LAMBDA_SUPPORT = 5;

        // MemberName
        // JVM使用-2及以上的值作为vtable索引。
        // 字段值是简单的正偏移量。
        // 参考: src/share/vm/oops/methodOop.hpp
        // 此值足够负，以避免此类数字，
        // 但不要太负。
        static final int
                MN_IS_METHOD           = 0x00010000, // 方法（不是构造函数）
                MN_IS_CONSTRUCTOR      = 0x00020000, // 构造函数
                MN_IS_FIELD            = 0x00040000, // 字段
                MN_IS_TYPE             = 0x00080000, // 嵌套类型
                MN_CALLER_SENSITIVE    = 0x00100000, // 检测到 @CallerSensitive 注解
                MN_REFERENCE_KIND_SHIFT = 24, // refKind
                MN_REFERENCE_KIND_MASK = 0x0F000000 >> MN_REFERENCE_KIND_SHIFT,
                // SEARCH_* 位不是用于 MN.flags，而是用于 MHN.getMembers 的 matchFlags 参数：
                MN_SEARCH_SUPERCLASSES = 0x00100000,
                MN_SEARCH_INTERFACES   = 0x00200000;

        /**
         * 在JVM中编码的基本类型。这些代码值不
         * 用于此类之外。它们作为JVM和此类之间的私有接口的一部分使用。
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
         * 常量池条目类型。
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
            CONSTANT_LIMIT               = 19;   // 类文件中找到的标签限制

        /**
         * 访问修饰符标志。
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
            // 别名：
            ACC_SUPER                  = ACC_SYNCHRONIZED,
            ACC_BRIDGE                 = ACC_VOLATILE,
            ACC_VARARGS                = ACC_TRANSIENT;

        /**
         * 常量池引用类型代码，由CONSTANT_MethodHandle CP条目使用。
         */
        static final byte
            REF_NONE                    = 0,  // null值
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
                // 忽略JVM关心的异国情调操作；我们只是不会发出它们
                //System.err.println("warning: "+err);
                continue;
            }
        }
        return true;
    }
    static {
        assert(verifyConstants());
    }

    // 从JVM的上层调用。
    // 这些必须不是公共的。

    /**
     * JVM正在链接一个invokedynamic指令。为它创建一个具体的调用站点。
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
     * JVM想要一个指向MethodType的指针。通过查找或创建一个来满足它。
     */
    static MethodType findMethodHandleType(Class<?> rtype, Class<?>[] ptypes) {
        return MethodType.makeImpl(rtype, ptypes, true);
    }

    /**
     * JVM想要链接一个需要动态类型检查的调用站点。
     * 名称是一个类型检查调用者，invokeExact 或 invoke。
     * 返回一个JVM方法（MemberName）来处理调用。
     * 该方法假设堆栈上的参数如下：
     * 0: 正在调用的方法句柄
     * 1-N: 方法句柄调用的参数
     * N+1: 一个可选的、隐式添加的参数（通常是给定的MethodType）
     * <p>
     * 这样的调用站点的名义方法是
     * 签名多态方法的一个实例（参见 @PolymorphicSignature）。
     * 这样的方法实例是用户可见的实体，它们是从
     * {@code MethodHandle} 中的通用占位方法“拆分”出来的。
     * （请注意，占位方法与任何
     * 其实例都不相同。如果反射调用，保证会抛出
     * {@code UnsupportedOperationException}。）
     * 如果签名多态方法实例被具体化，
     * 它会作为原始占位方法的“副本”出现
     * （{@code MethodHandle} 的本地最终成员），除了
     * 它的类型描述符具有实例所需的形状，
     * 且该方法实例不是可变参数。
     * 该方法实例也被标记为合成的，因为
     * 该方法（按定义）不出现在Java源代码中。
     * <p>
     * JVM允许将此方法作为实例元数据具体化。
     * 例如，{@code invokeBasic} 总是被具体化。
     * 但JVM也可以调用 {@code linkMethod}。
     * 如果结果是一个 * 有序对（方法，附录），
     * 方法获取所有参数（0..N 包括）
     * 加上附录（N+1），并使用附录完成调用。
     * 这样，一个可重用的方法（称为“链接方法”）
     * 可以执行任何数量的多态实例方法的功能。
     * <p>
     * 链接方法可以是弱类型的，任何或所有引用
     * 都可以重写为 {@code Object}，任何原始类型
     * （除了 {@code long}/{@code float}/{@code double}）
     * 都可以重写为 {@code int}。
     * 链接方法被信任返回一个强类型的结果，
     * 根据它所模拟的签名多态实例的具体方法类型描述符。
     * 这可能需要（必要时）使用
     * 从附录参数中提取的数据进行动态检查。
     * <p>
     * JVM不检查附录，除了将它原封不动地传递给
     * 链接方法的每次调用。
     * 这意味着JDK运行时在选择每个链接方法及其
     * 对应附录的形状方面有很大的自由度。
     * 链接方法应从 {@code LambdaForm} 生成
     * 以使它们不会出现在堆栈跟踪中。
     * <p>
     * {@code linkMethod} 调用可以省略附录
     * （返回null）并完全在链接方法中模拟所需功能。
     * 作为一个边缘情况，如果 N==255，不可能有附录。
     * 在这种情况下，返回的方法必须自定义生成以
     * 执行任何必要的类型检查。
     * <p>
     * 如果JVM不在调用站点具体化一个方法，而是
     * 调用 {@code linkMethod}，则字节码中表示的相应调用
     * 可能提到一个有效的但不能用 {@code MemberName} 表示的方法。
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
     * 它将调用此方法。（不要更改名称或签名。）
     * 类型参数对于字段请求是 Class，对于非字段请求是 MethodType。
     * <p>
     * 最近版本的 JVM 还可能传递一个已解析的 MemberName 作为类型。
     * 在这种情况下，名称将被忽略，可以为 null。
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
     * 使用最佳可能的原因进行 err.initCause()，如果原因具有相同或更好的类型，则用原因替换 err 本身。
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
     * 也就是说，它是否调用 Reflection.getCallerClass 或类似的方法来询问其调用者的身份？
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
