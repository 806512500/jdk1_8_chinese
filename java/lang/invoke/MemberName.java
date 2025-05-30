
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

import sun.invoke.util.BytecodeDescriptor;
import sun.invoke.util.VerifyAccess;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import static java.lang.invoke.MethodHandleNatives.Constants.*;
import static java.lang.invoke.MethodHandleStatics.*;
import java.util.Objects;

/**
 * {@code MemberName} 是一个紧凑的符号数据，完全描述了一个方法或字段引用。
 * 一个成员名称可以引用一个字段、方法、构造器或成员类型。
 * 每个成员名称都有一个简单名称（字符串）和一个类型（类或方法类型）。
 * 成员名称可能有一个非空的声明类，也可能只是一个裸名称/类型对。
 * 成员名称可能具有非零的修饰符标志。
 * 最后，成员名称可能是已解析的或未解析的。
 * 如果已解析，命名的成员存在。
 * <p>
 * 无论是否已解析，成员名称都不提供任何访问权限或调用能力。
 * 它只是一个紧凑的表示，包含链接到和正确使用命名成员所需的所有符号信息。
 * <p>
 * 当已解析时，成员名称的内部实现可能包含对JVM元数据的引用。
 * 此表示是无状态的，仅描述性的。
 * 它不提供任何私有信息，也不提供使用成员的能力。
 * <p>
 * 相比之下，{@linkplain java.lang.reflect.Method} 包含了关于方法内部的更完整信息
 * （除了字节码）并且还允许调用。MemberName 比 Method 轻量得多，
 * 因为它包含大约7个字段，而 Method 包含16个字段（加上其子数组），
 * 这些字段省略了 Method 中的许多信息。
 * @author jrose
 */
/*non-public*/ final class MemberName implements Member, Cloneable {
    private Class<?> clazz;       // 方法定义所在的类
    private String   name;        // 如果尚未具体化，则可能为 null
    private Object   type;        // 如果尚未具体化，则可能为 null
    private int      flags;       // 修饰符位；参见 reflect.Modifier
    //@Injected JVM_Method* vmtarget;
    //@Injected int         vmindex;
    private Object   resolution;  // 如果为 null，则此成员已解析

    /** 返回此成员的声明类。
     *  如果是裸名称和类型，声明类将为 null。
     */
    public Class<?> getDeclaringClass() {
        return clazz;
    }

    /** 生成声明类的类加载器的实用方法。 */
    public ClassLoader getClassLoader() {
        return clazz.getClassLoader();
    }

    /** 返回此成员的简单名称。
     *  对于类型，与 {@link Class#getSimpleName} 相同。
     *  对于方法或字段，是成员的简单名称。
     *  对于构造器，始终为 {@code "<init>"}。
     */
    public String getName() {
        if (name == null) {
            expandFromVM();
            if (name == null) {
                return null;
            }
        }
        return name;
    }

    public MethodType getMethodOrFieldType() {
        if (isInvocable())
            return getMethodType();
        if (isGetter())
            return MethodType.methodType(getFieldType());
        if (isSetter())
            return MethodType.methodType(void.class, getFieldType());
        throw new InternalError("not a method or field: "+this);
    }

    /** 返回此成员的声明类型，该成员
     *  必须是方法或构造器。
     */
    public MethodType getMethodType() {
        if (type == null) {
            expandFromVM();
            if (type == null) {
                return null;
            }
        }
        if (!isInvocable()) {
            throw newIllegalArgumentException("not invocable, no method type");
        }

        {
            // 获取一个不会被竞争线程更改的 type 快照。
            final Object type = this.type;
            if (type instanceof MethodType) {
                return (MethodType) type;
            }
        }

        // type 还不是 MethodType。线程安全地转换它。
        synchronized (this) {
            if (type instanceof String) {
                String sig = (String) type;
                MethodType res = MethodType.fromMethodDescriptorString(sig, getClassLoader());
                type = res;
            } else if (type instanceof Object[]) {
                Object[] typeInfo = (Object[]) type;
                Class<?>[] ptypes = (Class<?>[]) typeInfo[1];
                Class<?> rtype = (Class<?>) typeInfo[0];
                MethodType res = MethodType.methodType(rtype, ptypes);
                type = res;
            }
            // 确保 type 是 MethodType，以防止竞争线程。
            assert type instanceof MethodType : "bad method type " + type;
        }
        return (MethodType) type;
    }

    /** 返回此方法或构造器必须调用的实际类型。
     *  对于非静态方法或构造器，这是带有前导参数的类型，
     *  即声明类的引用。对于静态方法，它与声明类型相同。
     */
    public MethodType getInvocationType() {
        MethodType itype = getMethodOrFieldType();
        if (isConstructor() && getReferenceKind() == REF_newInvokeSpecial)
            return itype.changeReturnType(clazz);
        if (!isStatic())
            return itype.insertParameterTypes(0, clazz);
        return itype;
    }

    /** 生成方法类型的参数类型的实用方法。 */
    public Class<?>[] getParameterTypes() {
        return getMethodType().parameterArray();
    }

    /** 生成方法类型的返回类型的实用方法。 */
    public Class<?> getReturnType() {
        return getMethodType().returnType();
    }

    /** 返回此成员的声明类型，该成员
     *  必须是字段或类型。
     *  如果是类型成员，则返回该类型本身。
     */
    public Class<?> getFieldType() {
        if (type == null) {
            expandFromVM();
            if (type == null) {
                return null;
            }
        }
        if (isInvocable()) {
            throw newIllegalArgumentException("not a field or nested class, no simple type");
        }

        {
            // 获取一个不会被竞争线程更改的 type 快照。
            final Object type = this.type;
            if (type instanceof Class<?>) {
                return (Class<?>) type;
            }
        }

        // type 还不是 Class。线程安全地转换它。
        synchronized (this) {
            if (type instanceof String) {
                String sig = (String) type;
                MethodType mtype = MethodType.fromMethodDescriptorString("()"+sig, getClassLoader());
                Class<?> res = mtype.returnType();
                type = res;
            }
            // 确保 type 是 Class，以防止竞争线程。
            assert type instanceof Class<?> : "bad field type " + type;
        }
        return (Class<?>) type;
    }

    /** 生成此成员的方法类型或字段类型的实用方法。 */
    public Object getType() {
        return (isInvocable() ? getMethodType() : getFieldType());
    }

    /** 生成此成员的签名，用于在类文件格式中描述其类型。 */
    public String getSignature() {
        if (type == null) {
            expandFromVM();
            if (type == null) {
                return null;
            }
        }
        if (isInvocable())
            return BytecodeDescriptor.unparse(getMethodType());
        else
            return BytecodeDescriptor.unparse(getFieldType());
    }

    /** 返回此成员的修饰符标志。
     *  @see java.lang.reflect.Modifier
     */
    public int getModifiers() {
        return (flags & RECOGNIZED_MODIFIERS);
    }

    /** 返回此成员的引用类型，如果没有则为零。
     */
    public byte getReferenceKind() {
        return (byte) ((flags >>> MN_REFERENCE_KIND_SHIFT) & MN_REFERENCE_KIND_MASK);
    }
    private boolean referenceKindIsConsistent() {
        byte refKind = getReferenceKind();
        if (refKind == REF_NONE)  return isType();
        if (isField()) {
            assert(staticIsConsistent());
            assert(MethodHandleNatives.refKindIsField(refKind));
        } else if (isConstructor()) {
            assert(refKind == REF_newInvokeSpecial || refKind == REF_invokeSpecial);
        } else if (isMethod()) {
            assert(staticIsConsistent());
            assert(MethodHandleNatives.refKindIsMethod(refKind));
            if (clazz.isInterface())
                assert(refKind == REF_invokeInterface ||
                       refKind == REF_invokeStatic    ||
                       refKind == REF_invokeSpecial   ||
                       refKind == REF_invokeVirtual && isObjectPublicMethod());
        } else {
            assert(false);
        }
        return true;
    }
    private boolean isObjectPublicMethod() {
        if (clazz == Object.class)  return true;
        MethodType mtype = getMethodType();
        if (name.equals("toString") && mtype.returnType() == String.class && mtype.parameterCount() == 0)
            return true;
        if (name.equals("hashCode") && mtype.returnType() == int.class && mtype.parameterCount() == 0)
            return true;
        if (name.equals("equals") && mtype.returnType() == boolean.class && mtype.parameterCount() == 1 && mtype.parameterType(0) == Object.class)
            return true;
        return false;
    }
    /*non-public*/ boolean referenceKindIsConsistentWith(int originalRefKind) {
        int refKind = getReferenceKind();
        if (refKind == originalRefKind)  return true;
        switch (originalRefKind) {
        case REF_invokeInterface:
            // 查找接口方法，可以得到（例如）Object.hashCode
            assert(refKind == REF_invokeVirtual ||
                   refKind == REF_invokeSpecial) : this;
            return true;
        case REF_invokeVirtual:
        case REF_newInvokeSpecial:
            // 查找虚拟方法，可以得到（例如）final String.hashCode。
            assert(refKind == REF_invokeSpecial) : this;
            return true;
        }
        assert(false) : this+" != "+MethodHandleNatives.refKindName((byte)originalRefKind);
        return true;
    }
    private boolean staticIsConsistent() {
        byte refKind = getReferenceKind();
        return MethodHandleNatives.refKindIsStatic(refKind) == isStatic() || getModifiers() == 0;
    }
    private boolean vminfoIsConsistent() {
        byte refKind = getReferenceKind();
        assert(isResolved());  // 否则不要调用
        Object vminfo = MethodHandleNatives.getMemberVMInfo(this);
        assert(vminfo instanceof Object[]);
        long vmindex = (Long) ((Object[])vminfo)[0];
        Object vmtarget = ((Object[])vminfo)[1];
        if (MethodHandleNatives.refKindIsField(refKind)) {
            assert(vmindex >= 0) : vmindex + ":" + this;
            assert(vmtarget instanceof Class);
        } else {
            if (MethodHandleNatives.refKindDoesDispatch(refKind))
                assert(vmindex >= 0) : vmindex + ":" + this;
            else
                assert(vmindex < 0) : vmindex;
            assert(vmtarget instanceof MemberName) : vmtarget + " in " + this;
        }
        return true;
    }

    private MemberName changeReferenceKind(byte refKind, byte oldKind) {
        assert(getReferenceKind() == oldKind);
        assert(MethodHandleNatives.refKindIsValid(refKind));
        flags += (((int)refKind - oldKind) << MN_REFERENCE_KIND_SHIFT);
        return this;
    }

    private boolean testFlags(int mask, int value) {
        return (flags & mask) == value;
    }
    private boolean testAllFlags(int mask) {
        return testFlags(mask, mask);
    }
    private boolean testAnyFlags(int mask) {
        return !testFlags(mask, 0);
    }

    /** 用于查询此成员是否是方法句柄调用（invoke 或 invokeExact）的实用方法。
     */
    public boolean isMethodHandleInvoke() {
        final int bits = MH_INVOKE_MODS &~ Modifier.PUBLIC;
        final int negs = Modifier.STATIC;
        if (testFlags(bits | negs, bits) &&
            clazz == MethodHandle.class) {
            return isMethodHandleInvokeName(name);
        }
        return false;
    }
    public static boolean isMethodHandleInvokeName(String name) {
        switch (name) {
        case "invoke":
        case "invokeExact":
            return true;
        default:
            return false;
        }
    }
    private static final int MH_INVOKE_MODS = Modifier.NATIVE | Modifier.FINAL | Modifier.PUBLIC;

    /** 用于查询此成员的修饰符标志的实用方法。 */
    public boolean isStatic() {
        return Modifier.isStatic(flags);
    }
    /** 用于查询此成员的修饰符标志的实用方法。 */
    public boolean isPublic() {
        return Modifier.isPublic(flags);
    }
    /** 用于查询此成员的修饰符标志的实用方法。 */
    public boolean isPrivate() {
        return Modifier.isPrivate(flags);
    }
    /** 用于查询此成员的修饰符标志的实用方法。 */
    public boolean isProtected() {
        return Modifier.isProtected(flags);
    }
    /** 用于查询此成员的修饰符标志的实用方法。 */
    public boolean isFinal() {
        return Modifier.isFinal(flags);
    }
    /** 用于查询此成员或其定义类是否为 final 的实用方法。 */
    public boolean canBeStaticallyBound() {
        return Modifier.isFinal(flags | clazz.getModifiers());
    }
    /** 用于查询此成员的修饰符标志的实用方法。 */
    public boolean isVolatile() {
        return Modifier.isVolatile(flags);
    }
    /** 用于查询此成员的修饰符标志的实用方法。 */
    public boolean isAbstract() {
        return Modifier.isAbstract(flags);
    }
    /** 用于查询此成员的修饰符标志的实用方法。 */
    public boolean isNative() {
        return Modifier.isNative(flags);
    }
    // 通过 Modifier.isFoo 测试其余的（native, volatile, transient 等）


                // unofficial modifier flags, used by HotSpot:
    static final int BRIDGE    = 0x00000040;
    static final int VARARGS   = 0x00000080;
    static final int SYNTHETIC = 0x00001000;
    static final int ANNOTATION= 0x00002000;
    static final int ENUM      = 0x00004000;
    /** 用于查询此成员的修饰符标志的方法；如果成员不是方法，则返回 false。 */
    public boolean isBridge() {
        return testAllFlags(IS_METHOD | BRIDGE);
    }
    /** 用于查询此成员的修饰符标志的方法；如果成员不是方法，则返回 false。 */
    public boolean isVarargs() {
        return testAllFlags(VARARGS) && isInvocable();
    }
    /** 用于查询此成员的修饰符标志的方法；如果成员不是方法，则返回 false。 */
    public boolean isSynthetic() {
        return testAllFlags(SYNTHETIC);
    }

    static final String CONSTRUCTOR_NAME = "<init>";  // the ever-popular

    // 由 JVM 导出的修饰符：
    static final int RECOGNIZED_MODIFIERS = 0xFFFF;

    // 私有标志，不属于 RECOGNIZED_MODIFIERS：
    static final int
            IS_METHOD        = MN_IS_METHOD,        // 方法（非构造函数）
            IS_CONSTRUCTOR   = MN_IS_CONSTRUCTOR,   // 构造函数
            IS_FIELD         = MN_IS_FIELD,         // 字段
            IS_TYPE          = MN_IS_TYPE,          // 嵌套类型
            CALLER_SENSITIVE = MN_CALLER_SENSITIVE; // 检测到 @CallerSensitive 注解

    static final int ALL_ACCESS = Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED;
    static final int ALL_KINDS = IS_METHOD | IS_CONSTRUCTOR | IS_FIELD | IS_TYPE;
    static final int IS_INVOCABLE = IS_METHOD | IS_CONSTRUCTOR;
    static final int IS_FIELD_OR_METHOD = IS_METHOD | IS_FIELD;
    static final int SEARCH_ALL_SUPERS = MN_SEARCH_SUPERCLASSES | MN_SEARCH_INTERFACES;

    /** 用于查询此成员是否为方法或构造函数的方法。 */
    public boolean isInvocable() {
        return testAnyFlags(IS_INVOCABLE);
    }
    /** 用于查询此成员是否为方法、构造函数或字段的方法。 */
    public boolean isFieldOrMethod() {
        return testAnyFlags(IS_FIELD_OR_METHOD);
    }
    /** 查询此成员是否为方法的方法。 */
    public boolean isMethod() {
        return testAllFlags(IS_METHOD);
    }
    /** 查询此成员是否为构造函数的方法。 */
    public boolean isConstructor() {
        return testAllFlags(IS_CONSTRUCTOR);
    }
    /** 查询此成员是否为字段的方法。 */
    public boolean isField() {
        return testAllFlags(IS_FIELD);
    }
    /** 查询此成员是否为类型的方法。 */
    public boolean isType() {
        return testAllFlags(IS_TYPE);
    }
    /** 用于查询此成员是否既不是 public、private 也不是 protected 的方法。 */
    public boolean isPackage() {
        return !testAnyFlags(ALL_ACCESS);
    }
    /** 查询此成员是否有 CallerSensitive 注解的方法。 */
    public boolean isCallerSensitive() {
        return testAllFlags(CALLER_SENSITIVE);
    }

    /** 用于查询此成员是否可从给定的查找类访问的方法。 */
    public boolean isAccessibleFrom(Class<?> lookupClass) {
        return VerifyAccess.isMemberAccessible(this.getDeclaringClass(), this.getDeclaringClass(), flags,
                                               lookupClass, ALL_ACCESS|MethodHandles.Lookup.PACKAGE);
    }

    /** 初始化查询。它未解析。 */
    private void init(Class<?> defClass, String name, Object type, int flags) {
        // 定义类允许为 null（对于裸名称/类型对）
        //name.toString();  // null 检查
        //type.equals(type);  // null 检查
        // 填充字段：
        this.clazz = defClass;
        this.name = name;
        this.type = type;
        this.flags = flags;
        assert(testAnyFlags(ALL_KINDS));
        assert(this.resolution == null);  // 任何人都不应该在此时修改它
        //assert(referenceKindIsConsistent());  // 在解析后执行此操作
    }

    /**
     * 调用 VM 以填充字段。此方法是同步的，以避免竞争调用。
     */
    private void expandFromVM() {
        if (type != null) {
            return;
        }
        if (!isResolved()) {
            return;
        }
        MethodHandleNatives.expand(this);
    }

    // 从 Core Reflection API 捕获信息：
    private static int flagsMods(int flags, int mods, byte refKind) {
        assert((flags & RECOGNIZED_MODIFIERS) == 0);
        assert((mods & ~RECOGNIZED_MODIFIERS) == 0);
        assert((refKind & ~MN_REFERENCE_KIND_MASK) == 0);
        return flags | mods | (refKind << MN_REFERENCE_KIND_SHIFT);
    }
    /** 为给定的反射方法创建名称。生成的名称将处于解析状态。 */
    public MemberName(Method m) {
        this(m, false);
    }
    @SuppressWarnings("LeakingThisInConstructor")
    public MemberName(Method m, boolean wantSpecial) {
        m.getClass();  // NPE 检查
        // 在拥有 m 时填充 vmtarget 和 vmindex：
        MethodHandleNatives.init(this, m);
        if (clazz == null) {  // MHN.init 失败
            if (m.getDeclaringClass() == MethodHandle.class &&
                isMethodHandleInvokeName(m.getName())) {
                // JVM 没有具体化此签名多态实例。
                // 需要在此处进行特殊处理。
                // 请参阅 MethodHandleNatives.linkMethod 的注释。
                MethodType type = MethodType.methodType(m.getReturnType(), m.getParameterTypes());
                int flags = flagsMods(IS_METHOD, m.getModifiers(), REF_invokeVirtual);
                init(MethodHandle.class, m.getName(), type, flags);
                if (isMethodHandleInvoke())
                    return;
            }
            throw new LinkageError(m.toString());
        }
        assert(isResolved() && this.clazz != null);
        this.name = m.getName();
        if (this.type == null)
            this.type = new Object[] { m.getReturnType(), m.getParameterTypes() };
        if (wantSpecial) {
            if (isAbstract())
                throw new AbstractMethodError(this.toString());
            if (getReferenceKind() == REF_invokeVirtual)
                changeReferenceKind(REF_invokeSpecial, REF_invokeVirtual);
            else if (getReferenceKind() == REF_invokeInterface)
                // 调用默认方法的 invokeSpecial
                changeReferenceKind(REF_invokeSpecial, REF_invokeInterface);
        }
    }
    public MemberName asSpecial() {
        switch (getReferenceKind()) {
        case REF_invokeSpecial:     return this;
        case REF_invokeVirtual:     return clone().changeReferenceKind(REF_invokeSpecial, REF_invokeVirtual);
        case REF_invokeInterface:   return clone().changeReferenceKind(REF_invokeSpecial, REF_invokeInterface);
        case REF_newInvokeSpecial:  return clone().changeReferenceKind(REF_invokeSpecial, REF_newInvokeSpecial);
        }
        throw new IllegalArgumentException(this.toString());
    }
    /** 如果此 MN 不是 REF_newInvokeSpecial，则返回具有该引用类型的克隆。
     *  在这种情况下，它必须已经是 REF_invokeSpecial。
     */
    public MemberName asConstructor() {
        switch (getReferenceKind()) {
        case REF_invokeSpecial:     return clone().changeReferenceKind(REF_newInvokeSpecial, REF_invokeSpecial);
        case REF_newInvokeSpecial:  return this;
        }
        throw new IllegalArgumentException(this.toString());
    }
    /** 如果此 MN 是 REF_invokeSpecial，则返回具有“正常”类型
     *  REF_invokeVirtual 的克隆；如果 clazz.isInterface，则切换到 REF_invokeInterface。
     *  最终结果是获得 MN 的完全虚拟化版本。
     *  （请注意，JVM 中的解析有时会去虚拟化，将 final 的 REF_invokeVirtual 更改为 REF_invokeSpecial，
     *  以及在某些边缘情况下将 REF_invokeInterface 更改为前两个中的任何一个；此转换
     *  在假设这种情况发生的情况下撤销该更改。）
     */
    public MemberName asNormalOriginal() {
        byte normalVirtual = clazz.isInterface() ? REF_invokeInterface : REF_invokeVirtual;
        byte refKind = getReferenceKind();
        byte newRefKind = refKind;
        MemberName result = this;
        switch (refKind) {
        case REF_invokeInterface:
        case REF_invokeVirtual:
        case REF_invokeSpecial:
            newRefKind = normalVirtual;
            break;
        }
        if (newRefKind == refKind)
            return this;
        result = clone().changeReferenceKind(newRefKind, refKind);
        assert(this.referenceKindIsConsistentWith(result.getReferenceKind()));
        return result;
    }
    /** 为给定的反射构造函数创建名称。生成的名称将处于解析状态。 */
    @SuppressWarnings("LeakingThisInConstructor")
    public MemberName(Constructor<?> ctor) {
        ctor.getClass();  // NPE 检查
        // 在拥有 ctor 时填充 vmtarget 和 vmindex：
        MethodHandleNatives.init(this, ctor);
        assert(isResolved() && this.clazz != null);
        this.name = CONSTRUCTOR_NAME;
        if (this.type == null)
            this.type = new Object[] { void.class, ctor.getParameterTypes() };
    }
    /** 为给定的反射字段创建名称。生成的名称将处于解析状态。
     */
    public MemberName(Field fld) {
        this(fld, false);
    }
    @SuppressWarnings("LeakingThisInConstructor")
    public MemberName(Field fld, boolean makeSetter) {
        fld.getClass();  // NPE 检查
        // 在拥有 fld 时填充 vmtarget 和 vmindex：
        MethodHandleNatives.init(this, fld);
        assert(isResolved() && this.clazz != null);
        this.name = fld.getName();
        this.type = fld.getType();
        assert((REF_putStatic - REF_getStatic) == (REF_putField - REF_getField));
        byte refKind = this.getReferenceKind();
        assert(refKind == (isStatic() ? REF_getStatic : REF_getField));
        if (makeSetter) {
            changeReferenceKind((byte)(refKind + (REF_putStatic - REF_getStatic)), refKind);
        }
    }
    public boolean isGetter() {
        return MethodHandleNatives.refKindIsGetter(getReferenceKind());
    }
    public boolean isSetter() {
        return MethodHandleNatives.refKindIsSetter(getReferenceKind());
    }
    public MemberName asSetter() {
        byte refKind = getReferenceKind();
        assert(MethodHandleNatives.refKindIsGetter(refKind));
        assert((REF_putStatic - REF_getStatic) == (REF_putField - REF_getField));
        byte setterRefKind = (byte)(refKind + (REF_putField - REF_getField));
        return clone().changeReferenceKind(setterRefKind, refKind);
    }
    /** 为给定的类创建名称。生成的名称将处于解析状态。 */
    public MemberName(Class<?> type) {
        init(type.getDeclaringClass(), type.getSimpleName(), type,
                flagsMods(IS_TYPE, type.getModifiers(), REF_NONE));
        initResolved(true);
    }

    /**
     * 为签名多态调用者创建名称。
     * 这是签名多态实例（如 MH.invokeExact 等）的占位符，
     * JVM 不会具体化。
     * 请参阅 {@link MethodHandleNatives#linkMethod} 的注释。
     */
    static MemberName makeMethodHandleInvoke(String name, MethodType type) {
        return makeMethodHandleInvoke(name, type, MH_INVOKE_MODS | SYNTHETIC);
    }
    static MemberName makeMethodHandleInvoke(String name, MethodType type, int mods) {
        MemberName mem = new MemberName(MethodHandle.class, name, type, REF_invokeVirtual);
        mem.flags |= mods;  // 它未解析，但无论如何添加这些修饰符
        assert(mem.isMethodHandleInvoke()) : mem;
        return mem;
    }

    // 裸构造器；JVM 将填充它
    MemberName() { }

    // 本地有用的克隆器
    @Override protected MemberName clone() {
        try {
            return (MemberName) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw newInternalError(ex);
        }
     }

    /** 获取此成员名称的定义。
     *  这可能在声明此成员的类的超类中。
     */
    public MemberName getDefinition() {
        if (!isResolved())  throw new IllegalStateException("必须已解析: "+this);
        if (isType())  return this;
        MemberName res = this.clone();
        res.clazz = null;
        res.type = null;
        res.name = null;
        res.resolution = res;
        res.expandFromVM();
        assert(res.getName().equals(this.getName()));
        return res;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, getReferenceKind(), name, getType());
    }
    @Override
    public boolean equals(Object that) {
        return (that instanceof MemberName && this.equals((MemberName)that));
    }

    /** 决定两个成员名称是否具有完全相同的符号内容。
     *  不考虑任何实际的类成员，因此即使两个成员名称解析为相同的实际成员，
     *  它们也可能是不同的引用。
     */
    public boolean equals(MemberName that) {
        if (this == that)  return true;
        if (that == null)  return false;
        return this.clazz == that.clazz
                && this.getReferenceKind() == that.getReferenceKind()
                && Objects.equals(this.name, that.name)
                && Objects.equals(this.getType(), that.getType());
    }

    // 从符号部分构造，用于查询：
    /** 从给定的组件创建字段或类型名称：
     *  声明类、名称、类型、引用类型。
     *  如果这是裸名称和类型，则可以将声明类提供为 null。
     *  生成的名称将处于未解析状态。
     */
    public MemberName(Class<?> defClass, String name, Class<?> type, byte refKind) {
        init(defClass, name, type, flagsMods(IS_FIELD, 0, refKind));
        initResolved(false);
    }
    /** 从给定的组件创建方法或构造函数名称：
     *  声明类、名称、类型、引用类型。
     *  如果且仅如果名称为 {@code "<init>"}，则它将是一个构造函数。
     *  如果这是裸名称和类型，则可以将声明类提供为 null。
     *  最后一个参数是可选的，一个布尔值，请求 REF_invokeSpecial。
     *  生成的名称将处于未解析状态。
     */
    public MemberName(Class<?> defClass, String name, MethodType type, byte refKind) {
        int initFlags = (name != null && name.equals(CONSTRUCTOR_NAME) ? IS_CONSTRUCTOR : IS_METHOD);
        init(defClass, name, type, flagsMods(initFlags, 0, refKind));
        initResolved(false);
    }
    /** 从给定的组件创建方法、构造函数或字段名称：
     *  引用类型、声明类、名称、类型。
     */
    public MemberName(byte refKind, Class<?> defClass, String name, Object type) {
        int kindFlags;
        if (MethodHandleNatives.refKindIsField(refKind)) {
            kindFlags = IS_FIELD;
            if (!(type instanceof Class))
                throw newIllegalArgumentException("不是字段类型");
        } else if (MethodHandleNatives.refKindIsMethod(refKind)) {
            kindFlags = IS_METHOD;
            if (!(type instanceof MethodType))
                throw newIllegalArgumentException("不是方法类型");
        } else if (refKind == REF_newInvokeSpecial) {
            kindFlags = IS_CONSTRUCTOR;
            if (!(type instanceof MethodType) ||
                !CONSTRUCTOR_NAME.equals(name))
                throw newIllegalArgumentException("不是构造函数类型或名称");
        } else {
            throw newIllegalArgumentException("无效的引用类型 " + refKind);
        }
        init(defClass, name, type, flagsMods(kindFlags, 0, refKind));
        initResolved(false);
    }
    /** 查询此成员名称是否解析为非静态、非 final 方法。 */
    public boolean hasReceiverTypeDispatch() {
        return MethodHandleNatives.refKindDoesDispatch(getReferenceKind());
    }


                /** Query whether this member name is resolved.
                 *  A resolved member name is one for which the JVM has found
                 *  a method, constructor, field, or type binding corresponding exactly to the name.
                 *  (文档？)
                 */
                public boolean isResolved() {
                    return resolution == null;
                }

                private void initResolved(boolean isResolved) {
                    assert(this.resolution == null);  // not initialized yet!
                    if (!isResolved)
                        this.resolution = this;
                    assert(isResolved() == isResolved);
                }

                void checkForTypeAlias(Class<?> refc) {
                    if (isInvocable()) {
                        MethodType type;
                        if (this.type instanceof MethodType)
                            type = (MethodType) this.type;
                        else
                            this.type = type = getMethodType();
                        if (type.erase() == type)  return;
                        if (VerifyAccess.isTypeVisible(type, refc))  return;
                        throw new LinkageError("bad method type alias: "+type+" not visible from "+refc);
                    } else {
                        Class<?> type;
                        if (this.type instanceof Class<?>)
                            type = (Class<?>) this.type;
                        else
                            this.type = type = getFieldType();
                        if (VerifyAccess.isTypeVisible(type, refc))  return;
                        throw new LinkageError("bad field type alias: "+type+" not visible from "+refc);
                    }
                }


                /** Produce a string form of this member name.
                 *  For types, it is simply the type's own string (as reported by {@code toString}).
                 *  For fields, it is {@code "DeclaringClass.name/type"}.
                 *  For methods and constructors, it is {@code "DeclaringClass.name(ptype...)rtype"}.
                 *  If the declaring class is null, the prefix {@code "DeclaringClass."} is omitted.
                 *  If the member is unresolved, a prefix {@code "*."} is prepended.
                 */
                @SuppressWarnings("LocalVariableHidesMemberVariable")
                @Override
                public String toString() {
                    if (isType())
                        return type.toString();  // class java.lang.String
                    // else it is a field, method, or constructor
                    StringBuilder buf = new StringBuilder();
                    if (getDeclaringClass() != null) {
                        buf.append(getName(clazz));
                        buf.append('.');
                    }
                    String name = getName();
                    buf.append(name == null ? "*" : name);
                    Object type = getType();
                    if (!isInvocable()) {
                        buf.append('/');
                        buf.append(type == null ? "*" : getName(type));
                    } else {
                        buf.append(type == null ? "(*)*" : getName(type));
                    }
                    byte refKind = getReferenceKind();
                    if (refKind != REF_NONE) {
                        buf.append('/');
                        buf.append(MethodHandleNatives.refKindName(refKind));
                    }
                    //buf.append("#").append(System.identityHashCode(this));
                    return buf.toString();
                }
                private static String getName(Object obj) {
                    if (obj instanceof Class<?>)
                        return ((Class<?>)obj).getName();
                    return String.valueOf(obj);
                }

                public IllegalAccessException makeAccessException(String message, Object from) {
                    message = message + ": "+ toString();
                    if (from != null)  message += ", from " + from;
                    return new IllegalAccessException(message);
                }
                private String message() {
                    if (isResolved())
                        return "no access";
                    else if (isConstructor())
                        return "no such constructor";
                    else if (isMethod())
                        return "no such method";
                    else
                        return "no such field";
                }
                public ReflectiveOperationException makeAccessException() {
                    String message = message() + ": "+ toString();
                    ReflectiveOperationException ex;
                    if (isResolved() || !(resolution instanceof NoSuchMethodError ||
                                          resolution instanceof NoSuchFieldError))
                        ex = new IllegalAccessException(message);
                    else if (isConstructor())
                        ex = new NoSuchMethodException(message);
                    else if (isMethod())
                        ex = new NoSuchMethodException(message);
                    else
                        ex = new NoSuchFieldException(message);
                    if (resolution instanceof Throwable)
                        ex.initCause((Throwable) resolution);
                    return ex;
                }

                /** Actually making a query requires an access check. */
                /*non-public*/ static Factory getFactory() {
                    return Factory.INSTANCE;
                }
                /** A factory type for resolving member names with the help of the VM.
                 *  TBD: Define access-safe public constructors for this factory.
                 */
                /*non-public*/ static class Factory {
                    private Factory() { } // singleton pattern
                    static Factory INSTANCE = new Factory();

                    private static int ALLOWED_FLAGS = ALL_KINDS;

                    /// Queries
                    List<MemberName> getMembers(Class<?> defc,
                            String matchName, Object matchType,
                            int matchFlags, Class<?> lookupClass) {
                        matchFlags &= ALLOWED_FLAGS;
                        String matchSig = null;
                        if (matchType != null) {
                            matchSig = BytecodeDescriptor.unparse(matchType);
                            if (matchSig.startsWith("("))
                                matchFlags &= ~(ALL_KINDS & ~IS_INVOCABLE);
                            else
                                matchFlags &= ~(ALL_KINDS & ~IS_FIELD);
                        }
                        final int BUF_MAX = 0x2000;
                        int len1 = matchName == null ? 10 : matchType == null ? 4 : 1;
                        MemberName[] buf = newMemberBuffer(len1);
                        int totalCount = 0;
                        ArrayList<MemberName[]> bufs = null;
                        int bufCount = 0;
                        for (;;) {
                            bufCount = MethodHandleNatives.getMembers(defc,
                                    matchName, matchSig, matchFlags,
                                    lookupClass,
                                    totalCount, buf);
                            if (bufCount <= buf.length) {
                                if (bufCount < 0)  bufCount = 0;
                                totalCount += bufCount;
                                break;
                            }
                            // JVM returned to us with an intentional overflow!
                            totalCount += buf.length;
                            int excess = bufCount - buf.length;
                            if (bufs == null)  bufs = new ArrayList<>(1);
                            bufs.add(buf);
                            int len2 = buf.length;
                            len2 = Math.max(len2, excess);
                            len2 = Math.max(len2, totalCount / 4);
                            buf = newMemberBuffer(Math.min(BUF_MAX, len2));
                        }
                        ArrayList<MemberName> result = new ArrayList<>(totalCount);
                        if (bufs != null) {
                            for (MemberName[] buf0 : bufs) {
                                Collections.addAll(result, buf0);
                            }
                        }
                        result.addAll(Arrays.asList(buf).subList(0, bufCount));
                        // Signature matching is not the same as type matching, since
                        // one signature might correspond to several types.
                        // So if matchType is a Class or MethodType, refilter the results.
                        if (matchType != null && matchType != matchSig) {
                            for (Iterator<MemberName> it = result.iterator(); it.hasNext();) {
                                MemberName m = it.next();
                                if (!matchType.equals(m.getType()))
                                    it.remove();
                            }
                        }
                        return result;
                    }
                    /** Produce a resolved version of the given member.
                     *  Super types are searched (for inherited members) if {@code searchSupers} is true.
                     *  Access checking is performed on behalf of the given {@code lookupClass}.
                     *  If lookup fails or access is not permitted, null is returned.
                     *  Otherwise a fresh copy of the given member is returned, with modifier bits filled in.
                     */
                    private MemberName resolve(byte refKind, MemberName ref, Class<?> lookupClass) {
                        MemberName m = ref.clone();  // JVM will side-effect the ref
                        assert(refKind == m.getReferenceKind());
                        try {
                            // There are 4 entities in play here:
                            //   * LC: lookupClass
                            //   * REFC: symbolic reference class (MN.clazz before resolution);
                            //   * DEFC: resolved method holder (MN.clazz after resolution);
                            //   * PTYPES: parameter types (MN.type)
                            //
                            // What we care about when resolving a MemberName is consistency between DEFC and PTYPES.
                            // We do type alias (TA) checks on DEFC to ensure that. DEFC is not known until the JVM
                            // finishes the resolution, so do TA checks right after MHN.resolve() is over.
                            //
                            // All parameters passed by a caller are checked against MH type (PTYPES) on every invocation,
                            // so it is safe to call a MH from any context.
                            //
                            // REFC view on PTYPES doesn't matter, since it is used only as a starting point for resolution and doesn't
                            // participate in method selection.
                            m = MethodHandleNatives.resolve(m, lookupClass);
                            m.checkForTypeAlias(m.getDeclaringClass());
                            m.resolution = null;
                        } catch (ClassNotFoundException | LinkageError ex) {
                            // JVM reports that the "bytecode behavior" would get an error
                            assert(!m.isResolved());
                            m.resolution = ex;
                            return m;
                        }
                        assert(m.referenceKindIsConsistent());
                        m.initResolved(true);
                        assert(m.vminfoIsConsistent());
                        return m;
                    }
                    /** Produce a resolved version of the given member.
                     *  Super types are searched (for inherited members) if {@code searchSupers} is true.
                     *  Access checking is performed on behalf of the given {@code lookupClass}.
                     *  If lookup fails or access is not permitted, a {@linkplain ReflectiveOperationException} is thrown.
                     *  Otherwise a fresh copy of the given member is returned, with modifier bits filled in.
                     */
                    public
                    <NoSuchMemberException extends ReflectiveOperationException>
                    MemberName resolveOrFail(byte refKind, MemberName m, Class<?> lookupClass,
                                             Class<NoSuchMemberException> nsmClass)
                            throws IllegalAccessException, NoSuchMemberException {
                        MemberName result = resolve(refKind, m, lookupClass);
                        if (result.isResolved())
                            return result;
                        ReflectiveOperationException ex = result.makeAccessException();
                        if (ex instanceof IllegalAccessException)  throw (IllegalAccessException) ex;
                        throw nsmClass.cast(ex);
                    }
                    /** Produce a resolved version of the given member.
                     *  Super types are searched (for inherited members) if {@code searchSupers} is true.
                     *  Access checking is performed on behalf of the given {@code lookupClass}.
                     *  If lookup fails or access is not permitted, return null.
                     *  Otherwise a fresh copy of the given member is returned, with modifier bits filled in.
                     */
                    public
                    MemberName resolveOrNull(byte refKind, MemberName m, Class<?> lookupClass) {
                        MemberName result = resolve(refKind, m, lookupClass);
                        if (result.isResolved())
                            return result;
                        return null;
                    }
                    /** Return a list of all methods defined by the given class.
                     *  Super types are searched (for inherited members) if {@code searchSupers} is true.
                     *  Access checking is performed on behalf of the given {@code lookupClass}.
                     *  Inaccessible members are not added to the last.
                     */
                    public List<MemberName> getMethods(Class<?> defc, boolean searchSupers,
                            Class<?> lookupClass) {
                        return getMethods(defc, searchSupers, null, null, lookupClass);
                    }
                    /** Return a list of matching methods defined by the given class.
                     *  Super types are searched (for inherited members) if {@code searchSupers} is true.
                     *  Returned methods will match the name (if not null) and the type (if not null).
                     *  Access checking is performed on behalf of the given {@code lookupClass}.
                     *  Inaccessible members are not added to the last.
                     */
                    public List<MemberName> getMethods(Class<?> defc, boolean searchSupers,
                            String name, MethodType type, Class<?> lookupClass) {
                        int matchFlags = IS_METHOD | (searchSupers ? SEARCH_ALL_SUPERS : 0);
                        return getMembers(defc, name, type, matchFlags, lookupClass);
                    }
                    /** Return a list of all constructors defined by the given class.
                     *  Access checking is performed on behalf of the given {@code lookupClass}.
                     *  Inaccessible members are not added to the last.
                     */
                    public List<MemberName> getConstructors(Class<?> defc, Class<?> lookupClass) {
                        return getMembers(defc, null, null, IS_CONSTRUCTOR, lookupClass);
                    }
                    /** Return a list of all fields defined by the given class.
                     *  Super types are searched (for inherited members) if {@code searchSupers} is true.
                     *  Access checking is performed on behalf of the given {@code lookupClass}.
                     *  Inaccessible members are not added to the last.
                     */
                    public List<MemberName> getFields(Class<?> defc, boolean searchSupers,
                            Class<?> lookupClass) {
                        return getFields(defc, searchSupers, null, null, lookupClass);
                    }
                    /** Return a list of all fields defined by the given class.
                     *  Super types are searched (for inherited members) if {@code searchSupers} is true.
                     *  Returned fields will match the name (if not null) and the type (if not null).
                     *  Access checking is performed on behalf of the given {@code lookupClass}.
                     *  Inaccessible members are not added to the last.
                     */
                    public List<MemberName> getFields(Class<?> defc, boolean searchSupers,
                            String name, Class<?> type, Class<?> lookupClass) {
                        int matchFlags = IS_FIELD | (searchSupers ? SEARCH_ALL_SUPERS : 0);
                        return getMembers(defc, name, type, matchFlags, lookupClass);
                    }
                    /** Return a list of all nested types defined by the given class.
                     *  Super types are searched (for inherited members) if {@code searchSupers} is true.
                     *  Access checking is performed on behalf of the given {@code lookupClass}.
                     *  Inaccessible members are not added to the last.
                     */
                    public List<MemberName> getNestedTypes(Class<?> defc, boolean searchSupers,
                            Class<?> lookupClass) {
                        int matchFlags = IS_TYPE | (searchSupers ? SEARCH_ALL_SUPERS : 0);
                        return getMembers(defc, null, null, matchFlags, lookupClass);
                    }
                    private static MemberName[] newMemberBuffer(int length) {
                        MemberName[] buf = new MemberName[length];
                        // fill the buffer with dummy structs for the JVM to fill in
                        for (int i = 0; i < length; i++)
                            buf[i] = new MemberName();
                        return buf;
                    }
                }

//                static {
//                    System.out.println("Hello world!  My methods are:");
//                    System.out.println(Factory.INSTANCE.getMethods(MemberName.class, true, null));
//                }
}
