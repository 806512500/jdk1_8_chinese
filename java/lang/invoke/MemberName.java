
/*
 * 版权所有 (c) 2008, 2013, Oracle 和/或其关联公司。保留所有权利。
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
 * 一个成员名称可以引用一个字段、方法、构造函数或成员类型。
 * 每个成员名称都有一个简单名称（字符串）和一个类型（Class 或 MethodType）。
 * 成员名称也可能有一个非空的声明类，或者它可能只是一个裸名称/类型对。
 * 成员名称也可能有非零的修饰符标志。
 * 最后，成员名称可能是已解析的或未解析的。
 * 如果已解析，命名的成员的存在性是确定的。
 * <p>
 * 无论是否已解析，成员名称都不为其持有者提供访问权限或调用能力。
 * 它只是一个紧凑的表示，包含所有必要的符号信息，以链接到并正确使用命名的成员。
 * <p>
 * 当解析时，成员名称的内部实现可能包括对 JVM 元数据的引用。
 * 此表示是无状态的，仅描述性的。
 * 它不提供任何私有信息，也不提供使用成员的能力。
 * <p>
 * 相比之下，{@linkplain java.lang.reflect.Method} 包含了关于方法内部的更完整信息（除了字节码），
 * 并且还允许调用。MemberName 比 Method 轻得多，因为它只包含大约 7 个字段，
 * 而 Method 包含 16 个字段（加上其子数组），并且这 7 个字段省略了 Method 中的许多信息。
 * @author jrose
 */
/*非公开*/ final class MemberName implements Member, Cloneable {
    private Class<?> clazz;       // 定义方法的类
    private String   name;        // 如果尚未具体化，则可能为 null
    private Object   type;        // 如果尚未具体化，则可能为 null
    private int      flags;       // 修饰符位；参见 reflect.Modifier
    //@Injected JVM_Method* vmtarget;
    //@Injected int         vmindex;
    private Object   resolution;  // 如果为 null，则此成员已解析

    /** 返回此成员的声明类。
     *  对于裸名称和类型，声明类将为 null。
     */
    public Class<?> getDeclaringClass() {
        return clazz;
    }

    /** 生成声明类的类加载器的实用方法。 */
    public ClassLoader getClassLoader() {
        return clazz.getClassLoader();
    }

    /** 返回此成员的简单名称。
     *  对于类型，它与 {@link Class#getSimpleName} 相同。
     *  对于方法或字段，它是成员的简单名称。
     *  对于构造函数，它始终为 {@code "<init>"}。
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

    /** 返回此成员的声明类型，必须是方法或构造函数。 */
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
            // 获取一个类型快照，不会被竞争线程改变。
            final Object type = this.type;
            if (type instanceof MethodType) {
                return (MethodType) type;
            }
        }

        // 类型还不是 MethodType。线程安全地转换它。
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
            // 确保类型是 MethodType，以供竞争线程使用。
            assert type instanceof MethodType : "bad method type " + type;
        }
        return (MethodType) type;
    }

    /** 返回此方法或构造函数必须调用的实际类型。
     *  对于非静态方法或构造函数，这是带有前导参数的类型，即声明类的引用。
     *  对于静态方法，它与声明类型相同。
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


                /** 用于生成方法类型的返回类型的方法。 */
    public Class<?> getReturnType() {
        return getMethodType().returnType();
    }

    /** 返回此成员的声明类型，必须是字段或类型。
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
            // 获取类型的一个快照，该快照不会被竞争线程改变。
            final Object type = this.type;
            if (type instanceof Class<?>) {
                return (Class<?>) type;
            }
        }

        // 类型还不是Class。线程安全地转换它。
        synchronized (this) {
            if (type instanceof String) {
                String sig = (String) type;
                MethodType mtype = MethodType.fromMethodDescriptorString("()"+sig, getClassLoader());
                Class<?> res = mtype.returnType();
                type = res;
            }
            // 确保类型对于竞争线程来说是一个Class。
            assert type instanceof Class<?> : "bad field type " + type;
        }
        return (Class<?>) type;
    }

    /** 用于生成此成员的方法类型或字段类型的方法。 */
    public Object getType() {
        return (isInvocable() ? getMethodType() : getFieldType());
    }

    /** 用于生成此成员的签名的方法，该签名在类文件格式中用于描述其类型。 */
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

    /** 返回此成员的引用类型，如果没有则返回零。 */
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
            // 查找接口方法，可以获取（例如）Object.hashCode
            assert(refKind == REF_invokeVirtual ||
                   refKind == REF_invokeSpecial) : this;
            return true;
        case REF_invokeVirtual:
        case REF_newInvokeSpecial:
            // 查找虚拟方法，可以获取（例如）final String.hashCode。
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


                /** 实用方法，用于查询此成员是否为方法句柄调用（invoke 或 invokeExact）。 */
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

    /** 实用方法，用于查询此成员的修饰符标志。 */
    public boolean isStatic() {
        return Modifier.isStatic(flags);
    }
    /** 实用方法，用于查询此成员的修饰符标志。 */
    public boolean isPublic() {
        return Modifier.isPublic(flags);
    }
    /** 实用方法，用于查询此成员的修饰符标志。 */
    public boolean isPrivate() {
        return Modifier.isPrivate(flags);
    }
    /** 实用方法，用于查询此成员的修饰符标志。 */
    public boolean isProtected() {
        return Modifier.isProtected(flags);
    }
    /** 实用方法，用于查询此成员的修饰符标志。 */
    public boolean isFinal() {
        return Modifier.isFinal(flags);
    }
    /** 实用方法，用于查询此成员或其定义类是否为 final。 */
    public boolean canBeStaticallyBound() {
        return Modifier.isFinal(flags | clazz.getModifiers());
    }
    /** 实用方法，用于查询此成员的修饰符标志。 */
    public boolean isVolatile() {
        return Modifier.isVolatile(flags);
    }
    /** 实用方法，用于查询此成员的修饰符标志。 */
    public boolean isAbstract() {
        return Modifier.isAbstract(flags);
    }
    /** 实用方法，用于查询此成员的修饰符标志。 */
    public boolean isNative() {
        return Modifier.isNative(flags);
    }
    // 通过 Modifier.isFoo 测试其余的（native, volatile, transient 等）

    // HotSpot 使用的非官方修饰符标志：
    static final int BRIDGE    = 0x00000040;
    static final int VARARGS   = 0x00000080;
    static final int SYNTHETIC = 0x00001000;
    static final int ANNOTATION= 0x00002000;
    static final int ENUM      = 0x00004000;
    /** 实用方法，用于查询此成员的修饰符标志；如果成员不是方法，则返回 false。 */
    public boolean isBridge() {
        return testAllFlags(IS_METHOD | BRIDGE);
    }
    /** 实用方法，用于查询此成员的修饰符标志；如果成员不是方法，则返回 false。 */
    public boolean isVarargs() {
        return testAllFlags(VARARGS) && isInvocable();
    }
    /** 实用方法，用于查询此成员的修饰符标志；如果成员不是方法，则返回 false。 */
    public boolean isSynthetic() {
        return testAllFlags(SYNTHETIC);
    }

    static final String CONSTRUCTOR_NAME = "<init>";  // 非常流行的

    // JVM 导出的修饰符：
    static final int RECOGNIZED_MODIFIERS = 0xFFFF;

    // 不属于 RECOGNIZED_MODIFIERS 的私有标志：
    static final int
            IS_METHOD        = MN_IS_METHOD,        // 方法（不是构造函数）
            IS_CONSTRUCTOR   = MN_IS_CONSTRUCTOR,   // 构造函数
            IS_FIELD         = MN_IS_FIELD,         // 字段
            IS_TYPE          = MN_IS_TYPE,          // 嵌套类型
            CALLER_SENSITIVE = MN_CALLER_SENSITIVE; // 检测到 @CallerSensitive 注解

    static final int ALL_ACCESS = Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED;
    static final int ALL_KINDS = IS_METHOD | IS_CONSTRUCTOR | IS_FIELD | IS_TYPE;
    static final int IS_INVOCABLE = IS_METHOD | IS_CONSTRUCTOR;
    static final int IS_FIELD_OR_METHOD = IS_METHOD | IS_FIELD;
    static final int SEARCH_ALL_SUPERS = MN_SEARCH_SUPERCLASSES | MN_SEARCH_INTERFACES;

    /** 实用方法，用于查询此成员是否为方法或构造函数。 */
    public boolean isInvocable() {
        return testAnyFlags(IS_INVOCABLE);
    }
    /** 实用方法，用于查询此成员是否为方法、构造函数或字段。 */
    public boolean isFieldOrMethod() {
        return testAnyFlags(IS_FIELD_OR_METHOD);
    }
    /** 查询此成员是否为方法。 */
    public boolean isMethod() {
        return testAllFlags(IS_METHOD);
    }
    /** 查询此成员是否为构造函数。 */
    public boolean isConstructor() {
        return testAllFlags(IS_CONSTRUCTOR);
    }
    /** 查询此成员是否为字段。 */
    public boolean isField() {
        return testAllFlags(IS_FIELD);
    }
    /** 查询此成员是否为类型。 */
    public boolean isType() {
        return testAllFlags(IS_TYPE);
    }
    /** 实用方法，用于查询此成员是否既不是 public、private 也不是 protected。 */
    public boolean isPackage() {
        return !testAnyFlags(ALL_ACCESS);
    }
    /** 查询此成员是否有 CallerSensitive 注解。 */
    public boolean isCallerSensitive() {
        return testAllFlags(CALLER_SENSITIVE);
    }

    /** 实用方法，用于查询此成员是否可以从给定的查找类访问。 */
    public boolean isAccessibleFrom(Class<?> lookupClass) {
        return VerifyAccess.isMemberAccessible(this.getDeclaringClass(), this.getDeclaringClass(), flags,
                                               lookupClass, ALL_ACCESS|MethodHandles.Lookup.PACKAGE);
    }

    /** 初始化一个查询。它尚未解析。 */
    private void init(Class<?> defClass, String name, Object type, int flags) {
        // 定义类可以为 null（对于裸名称/类型对）
        //name.toString();  // null 检查
        //type.equals(type);  // null 检查
        // 填充字段：
        this.clazz = defClass;
        this.name = name;
        this.type = type;
        this.flags = flags;
        assert(testAnyFlags(ALL_KINDS));
        assert(this.resolution == null);  // 任何人都不应该在此之前触及
        //assert(referenceKindIsConsistent());  // 在解析后执行此操作
    }


                /**
     * 调用 VM 以填充字段。此方法已同步以避免竞争调用。
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

    // 从核心反射 API 捕获信息：
    private static int flagsMods(int flags, int mods, byte refKind) {
        assert((flags & RECOGNIZED_MODIFIERS) == 0);
        assert((mods & ~RECOGNIZED_MODIFIERS) == 0);
        assert((refKind & ~MN_REFERENCE_KIND_MASK) == 0);
        return flags | mods | (refKind << MN_REFERENCE_KIND_SHIFT);
    }
    /** 为给定的反射方法创建一个名称。生成的名称将处于已解析状态。 */
    public MemberName(Method m) {
        this(m, false);
    }
    @SuppressWarnings("LeakingThisInConstructor")
    public MemberName(Method m, boolean wantSpecial) {
        m.getClass();  // NPE 检查
        // 在拥有 m 时填充 vmtarget, vmindex：
        MethodHandleNatives.init(this, m);
        if (clazz == null) {  // MHN.init 失败
            if (m.getDeclaringClass() == MethodHandle.class &&
                isMethodHandleInvokeName(m.getName())) {
                // JVM 未具体化此签名多态实例。
                // 需要在此处进行特殊情况处理。
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
    /** 如果此 MN 是 REF_invokeSpecial，则返回具有“正常”类型 REF_invokeVirtual 的克隆；
     *  如果 clazz.isInterface，则也切换到 REF_invokeInterface。
     *  最终结果是获取 MN 的完全虚拟化版本。
     *  （请注意，JVM 中的解析有时会去虚拟化，将 final 的 REF_invokeVirtual 更改为 REF_invokeSpecial，
     *  并在某些情况下将 REF_invokeInterface 更改为前两者之一；此转换在假设发生的情况下撤销该更改。）
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
    /** 为给定的反射构造函数创建一个名称。生成的名称将处于已解析状态。 */
    @SuppressWarnings("LeakingThisInConstructor")
    public MemberName(Constructor<?> ctor) {
        ctor.getClass();  // NPE 检查
        // 在拥有 ctor 时填充 vmtarget, vmindex：
        MethodHandleNatives.init(this, ctor);
        assert(isResolved() && this.clazz != null);
        this.name = CONSTRUCTOR_NAME;
        if (this.type == null)
            this.type = new Object[] { void.class, ctor.getParameterTypes() };
    }
    /** 为给定的反射字段创建一个名称。生成的名称将处于已解析状态。
     */
    public MemberName(Field fld) {
        this(fld, false);
    }
    @SuppressWarnings("LeakingThisInConstructor")
    public MemberName(Field fld, boolean makeSetter) {
        fld.getClass();  // NPE 检查
        // 在拥有 fld 时填充 vmtarget, vmindex：
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
    /** 为给定的类创建一个名称。生成的名称将处于已解析状态。 */
    public MemberName(Class<?> type) {
        init(type.getDeclaringClass(), type.getSimpleName(), type,
                flagsMods(IS_TYPE, type.getModifiers(), REF_NONE));
        initResolved(true);
    }


                /**
     * 创建一个签名多态调用者的名字。
     * 这是一个签名多态实例的占位符
     * （如 MH.invokeExact 等），JVM 不会具体化。
     * 请参阅 {@link MethodHandleNatives#linkMethod} 的注释。
     */
    static MemberName makeMethodHandleInvoke(String name, MethodType type) {
        return makeMethodHandleInvoke(name, type, MH_INVOKE_MODS | SYNTHETIC);
    }
    static MemberName makeMethodHandleInvoke(String name, MethodType type, int mods) {
        MemberName mem = new MemberName(MethodHandle.class, name, type, REF_invokeVirtual);
        mem.flags |= mods;  // 尽管未解析，但无论如何都要添加这些修饰符
        assert(mem.isMethodHandleInvoke()) : mem;
        return mem;
    }

    // 简单的构造函数；JVM 将填充它
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
     *  不考虑任何实际的类成员，因此即使两个成员名称解析为相同的实际成员，它们也可能是不同的引用。
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
     *  如果这是要成为一个裸名称和类型，声明类可以提供为 null。
     *  结果名称将处于未解析状态。
     */
    public MemberName(Class<?> defClass, String name, Class<?> type, byte refKind) {
        init(defClass, name, type, flagsMods(IS_FIELD, 0, refKind));
        initResolved(false);
    }
    /** 从给定的组件创建方法或构造函数名称：
     *  声明类、名称、类型、引用类型。
     *  如果且仅如果名称是 {@code "<init>"}，它将是一个构造函数。
     *  如果这是要成为一个裸名称和类型，声明类可以提供为 null。
     *  最后一个参数是可选的，一个布尔值，请求 REF_invokeSpecial。
     *  结果名称将处于未解析状态。
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
            throw newIllegalArgumentException("错误的引用类型 "+refKind);
        }
        init(defClass, name, type, flagsMods(kindFlags, 0, refKind));
        initResolved(false);
    }
    /** 查询此成员名称是否解析为非静态、非最终方法。
     */
    public boolean hasReceiverTypeDispatch() {
        return MethodHandleNatives.refKindDoesDispatch(getReferenceKind());
    }

    /** 查询此成员名称是否已解析。
     *  已解析的成员名称是指 JVM 已找到
     *  与名称完全对应的方法、构造函数、字段或类型绑定。
     *  （文档？）
     */
    public boolean isResolved() {
        return resolution == null;
    }

    private void initResolved(boolean isResolved) {
        assert(this.resolution == null);  // 尚未初始化！
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
            throw new LinkageError("错误的方法类型别名: "+type+" 从 "+refc+" 不可见");
        } else {
            Class<?> type;
            if (this.type instanceof Class<?>)
                type = (Class<?>) this.type;
            else
                this.type = type = getFieldType();
            if (VerifyAccess.isTypeVisible(type, refc))  return;
            throw new LinkageError("错误的字段类型别名: "+type+" 从 "+refc+" 不可见");
        }
    }


    /** 生成此成员名称的字符串形式。
     *  对于类型，它只是类型自己的字符串（如 {@code toString} 所报告）。
     *  对于字段，它是 {@code "DeclaringClass.name/type"}。
     *  对于方法和构造函数，它是 {@code "DeclaringClass.name(ptype...)rtype"}。
     *  如果声明类为 null，则省略前缀 {@code "DeclaringClass."}。
     *  如果成员未解决，则添加前缀 {@code "*."}。
     */
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    @Override
    public String toString() {
        if (isType())
            return type.toString();  // class java.lang.String
        // 否则它是一个字段、方法或构造函数
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

    /** 实际进行查询需要进行访问检查。 */
    /*non-public*/ static Factory getFactory() {
        return Factory.INSTANCE;
    }
    /** 用于在 VM 的帮助下解析成员名称的工厂类型。
     *  待办：为该工厂定义访问安全的公共构造函数。
     */
    /*non-public*/ static class Factory {
        private Factory() { } // 单例模式
        static Factory INSTANCE = new Factory();

        private static int ALLOWED_FLAGS = ALL_KINDS;

        /// 查询
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
                // JVM 故意返回溢出！
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
            // 签名匹配不同于类型匹配，因为一个签名可能对应多个类型。
            // 因此，如果 matchType 是 Class 或 MethodType，则重新过滤结果。
            if (matchType != null && matchType != matchSig) {
                for (Iterator<MemberName> it = result.iterator(); it.hasNext();) {
                    MemberName m = it.next();
                    if (!matchType.equals(m.getType()))
                        it.remove();
                }
            }
            return result;
        }
        /** 生成给定成员的已解析版本。
         *  如果 {@code searchSupers} 为 true，则搜索超类型（继承成员）。
         *  代表给定的 {@code lookupClass} 进行访问检查。
         *  如果查找失败或不允许访问，则返回 null。
         *  否则返回给定成员的新副本，并填充修饰符位。
         */
        private MemberName resolve(byte refKind, MemberName ref, Class<?> lookupClass) {
            MemberName m = ref.clone();  // JVM 将对 ref 进行副作用
            assert(refKind == m.getReferenceKind());
            try {
                // 这里有 4 个实体：
                //   * LC: lookupClass
                //   * REFC: 符号引用类 (MN.clazz 在解析之前);
                //   * DEFC: 解析后的方法持有者 (MN.clazz 在解析之后);
                //   * PTYPES: 参数类型 (MN.type)
                //
                // 我们在解析 MemberName 时关心的是 DEFC 和 PTYPES 之间的一致性。
                // 我们对 DEFC 进行类型别名 (TA) 检查以确保这一点。DEFC 在 JVM 完成解析之前是未知的，
                // 因此在 MHN.resolve() 结束后立即进行 TA 检查。
                //
                // 所有由调用者传递的参数在每次调用时都会针对 MH 类型 (PTYPES) 进行检查，
                // 因此从任何上下文调用 MH 是安全的。
                //
                // REFC 对 PTYPES 的视图无关紧要，因为它仅用作解析的起点，不参与方法选择。
                m = MethodHandleNatives.resolve(m, lookupClass);
                m.checkForTypeAlias(m.getDeclaringClass());
                m.resolution = null;
            } catch (ClassNotFoundException | LinkageError ex) {
                // JVM 报告“字节码行为”将导致错误
                assert(!m.isResolved());
                m.resolution = ex;
                return m;
            }
            assert(m.referenceKindIsConsistent());
            m.initResolved(true);
            assert(m.vminfoIsConsistent());
            return m;
        }
        /** 生成给定成员的已解析版本。
         *  如果 {@code searchSupers} 为 true，则搜索超类型（继承成员）。
         *  代表给定的 {@code lookupClass} 进行访问检查。
         *  如果查找失败或不允许访问，则抛出 {@linkplain ReflectiveOperationException}。
         *  否则返回给定成员的新副本，并填充修饰符位。
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
        /** 生成给定成员的已解析版本。
         *  如果 {@code searchSupers} 为 true，则搜索超类型（继承成员）。
         *  代表给定的 {@code lookupClass} 进行访问检查。
         *  如果查找失败或不允许访问，则返回 null。
         *  否则返回给定成员的新副本，并填充修饰符位。
         */
        public
        MemberName resolveOrNull(byte refKind, MemberName m, Class<?> lookupClass) {
            MemberName result = resolve(refKind, m, lookupClass);
            if (result.isResolved())
                return result;
            return null;
        }
        /** 返回给定类定义的所有方法的列表。
         *  如果 {@code searchSupers} 为 true，则搜索超类型（继承成员）。
         *  代表给定的 {@code lookupClass} 进行访问检查。
         *  不可访问的成员不会添加到最后。
         */
        public List<MemberName> getMethods(Class<?> defc, boolean searchSupers,
                Class<?> lookupClass) {
            return getMethods(defc, searchSupers, null, null, lookupClass);
        }
        /** 返回给定类定义的匹配方法的列表。
         *  如果 {@code searchSupers} 为 true，则搜索超类型（继承成员）。
         *  返回的方法将匹配名称（如果非 null）和类型（如果非 null）。
         *  代表给定的 {@code lookupClass} 进行访问检查。
         *  不可访问的成员不会添加到最后。
         */
        public List<MemberName> getMethods(Class<?> defc, boolean searchSupers,
                String name, MethodType type, Class<?> lookupClass) {
            int matchFlags = IS_METHOD | (searchSupers ? SEARCH_ALL_SUPERS : 0);
            return getMembers(defc, name, type, matchFlags, lookupClass);
        }
        /** 返回给定类定义的所有构造函数的列表。
         *  代表给定的 {@code lookupClass} 进行访问检查。
         *  不可访问的成员不会添加到最后。
         */
        public List<MemberName> getConstructors(Class<?> defc, Class<?> lookupClass) {
            return getMembers(defc, null, null, IS_CONSTRUCTOR, lookupClass);
        }
        /** 返回给定类定义的所有字段的列表。
         *  如果 {@code searchSupers} 为 true，则搜索超类型（继承成员）。
         *  代表给定的 {@code lookupClass} 进行访问检查。
         *  不可访问的成员不会添加到最后。
         */
        public List<MemberName> getFields(Class<?> defc, boolean searchSupers,
                Class<?> lookupClass) {
            return getFields(defc, searchSupers, null, null, lookupClass);
        }
        /** 返回给定类定义的所有字段的列表。
         *  如果 {@code searchSupers} 为 true，则搜索超类型（继承成员）。
         *  返回的字段将匹配名称（如果非 null）和类型（如果非 null）。
         *  代表给定的 {@code lookupClass} 进行访问检查。
         *  不可访问的成员不会添加到最后。
         */
        public List<MemberName> getFields(Class<?> defc, boolean searchSupers,
                String name, Class<?> type, Class<?> lookupClass) {
            int matchFlags = IS_FIELD | (searchSupers ? SEARCH_ALL_SUPERS : 0);
            return getMembers(defc, name, type, matchFlags, lookupClass);
        }
        /** 返回给定类定义的所有嵌套类型的列表。
         *  如果 {@code searchSupers} 为 true，则搜索超类型（继承成员）。
         *  代表给定的 {@code lookupClass} 进行访问检查。
         *  不可访问的成员不会添加到最后。
         */
        public List<MemberName> getNestedTypes(Class<?> defc, boolean searchSupers,
                Class<?> lookupClass) {
            int matchFlags = IS_TYPE | (searchSupers ? SEARCH_ALL_SUPERS : 0);
            return getMembers(defc, null, null, matchFlags, lookupClass);
        }
        private static MemberName[] newMemberBuffer(int length) {
            MemberName[] buf = new MemberName[length];
            // 用虚拟结构填充缓冲区，供 JVM 填充
            for (int i = 0; i < length; i++)
                buf[i] = new MemberName();
            return buf;
        }
    }

//    static {
//        System.out.println("Hello world!  My methods are:");
//        System.out.println(Factory.INSTANCE.getMethods(MemberName.class, true, null));
//    }
}
