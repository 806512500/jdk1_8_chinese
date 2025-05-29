
/*
 * 版权所有 (c) 2011, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;

import sun.invoke.util.Wrapper;
import java.lang.reflect.Field;

import static java.lang.invoke.LambdaForm.BasicType.*;
import static java.lang.invoke.MethodHandleStatics.*;
import static java.lang.invoke.MethodHandleNatives.Constants.*;

/**
 * 方法句柄调用语义的符号、非可执行形式。
 * 它由一系列名称组成。
 * 前 N (N=arity) 个名称是参数，
 * 而任何剩余的名称都是临时值。
 * 每个临时值指定将某个函数应用于某些参数。
 * 这些函数是方法句柄，而参数是常量值和局部名称的混合。
 * lambda 的结果被定义为其中一个名称，通常是最后一个。
 * <p>
 * 以下是一个近似的语法：
 * <blockquote><pre>{@code
 * LambdaForm = "(" ArgName* ")=>{" TempName* Result "}"
 * ArgName = "a" N ":" T
 * TempName = "t" N ":" T "=" Function "(" Argument* ");"
 * Function = ConstantValue
 * Argument = NameRef | ConstantValue
 * Result = NameRef | "void"
 * NameRef = "a" N | "t" N
 * N = (任何整数)
 * T = "L" | "I" | "J" | "F" | "D" | "V"
 * }</pre></blockquote>
 * 名称从左到右依次编号，从零开始。
 * （字母只是语法糖的一部分。）
 * 因此，第一个临时值（如果有）总是编号为 N（其中 N=arity）。
 * 参数列表中的每个名称引用必须引用在同一个 lambda 中先前定义的名称。
 * lambda 的结果是 void 当且仅当其结果索引为 -1。
 * 如果一个临时值的类型为 "V"，则它不能成为 NameRef 的对象，
 * 即使它拥有一个编号。
 * 注意，所有引用类型都被擦除为 "L"，表示 {@code Object}。
 * 所有子字节类型（boolean, byte, short, char）都被擦除为 "I"，即 {@code int}。
 * 其他类型表示通常的原始类型。
 * <p>
 * 函数调用严格遵循 Java 验证器的静态规则。
 * 参数和返回值在考虑其 "Name" 类型时必须完全匹配。
 * 只有在不改变擦除类型的情况下才允许转换。
 * <ul>
 * <li>L = Object: 可以自由地使用强制转换来转换引用类型
 * <li>I = int: 子字节类型在作为参数传递时会被强制缩小（参见 {@code explicitCastArguments}）
 * <li>J = long: 没有隐式转换
 * <li>F = float: 没有隐式转换
 * <li>D = double: 没有隐式转换
 * <li>V = void: 函数结果可以是 void 当且仅当其 Name 类型为 "V"
 * </ul>
 * 虽然不允许隐式转换，但可以轻松地通过使用调用类型转换身份函数的临时表达式来编码显式转换。
 * <p>
 * 示例：
 * <blockquote><pre>{@code
 * (a0:J)=>{ a0 }
 *     == identity(long)
 * (a0:I)=>{ t1:V = System.out#println(a0); void }
 *     == System.out#println(int)
 * (a0:L)=>{ t1:V = System.out#println(a0); a0 }
 *     == 具有打印副作用的身份函数
 * (a0:L, a1:L)=>{ t2:L = BoundMethodHandle#argument(a0);
 *                 t3:L = BoundMethodHandle#target(a0);
 *                 t4:L = MethodHandle#invoke(t3, t2, a1); t4 }
 *     == 用于一元 insertArgument 组合的一般调用者
 * (a0:L, a1:L)=>{ t2:L = FilterMethodHandle#filter(a0);
 *                 t3:L = MethodHandle#invoke(t2, a1);
 *                 t4:L = FilterMethodHandle#target(a0);
 *                 t5:L = MethodHandle#invoke(t4, t3); t5 }
 *     == 用于一元 filterArgument 组合的一般调用者
 * (a0:L, a1:L)=>{ ...(与上一个示例相同)...
 *                 t5:L = MethodHandle#invoke(t4, t3, a1); t5 }
 *     == 用于一元/一元 foldArgument 组合的一般调用者
 * (a0:L, a1:I)=>{ t2:I = identity(long).asType((int)->long)(a1); t2 }
 *     == 用于执行 i2l 的身份方法句柄的调用者
 * (a0:L, a1:L)=>{ t2:L = BoundMethodHandle#argument(a0);
 *                 t3:L = Class#cast(t2,a1); t3 }
 *     == 用于执行强制转换的身份方法句柄的调用者
 * }</pre></blockquote>
 * <p>
 * @author John Rose, JSR 292 EG
 */
class LambdaForm {
    final int arity;
    final int result;
    final boolean forceInline;
    final MethodHandle customized;
    @Stable final Name[] names;
    final String debugName;
    MemberName vmentry;   // 低级行为，或如果尚未准备则为 null
    private boolean isCompiled;

    // 要么是 LambdaForm 缓存（由 LambdaFormEditor 管理），要么是未自定义版本的链接（对于自定义的 LF）
    volatile Object transformCache;

    public static final int VOID_RESULT = -1, LAST_RESULT = -2;

    enum BasicType {
        L_TYPE('L', Object.class, Wrapper.OBJECT),  // 所有引用类型
        I_TYPE('I', int.class,    Wrapper.INT),
        J_TYPE('J', long.class,   Wrapper.LONG),
        F_TYPE('F', float.class,  Wrapper.FLOAT),
        D_TYPE('D', double.class, Wrapper.DOUBLE),  // 所有原始类型
        V_TYPE('V', void.class,   Wrapper.VOID);    // 并非所有上下文中都有效

        static final BasicType[] ALL_TYPES = BasicType.values();
        static final BasicType[] ARG_TYPES = Arrays.copyOf(ALL_TYPES, ALL_TYPES.length-1);

        static final int ARG_TYPE_LIMIT = ARG_TYPES.length;
        static final int TYPE_LIMIT = ALL_TYPES.length;

        private final char btChar;
        private final Class<?> btClass;
        private final Wrapper btWrapper;

        private BasicType(char btChar, Class<?> btClass, Wrapper wrapper) {
            this.btChar = btChar;
            this.btClass = btClass;
            this.btWrapper = wrapper;
        }

        char basicTypeChar() {
            return btChar;
        }
        Class<?> basicTypeClass() {
            return btClass;
        }
        Wrapper basicTypeWrapper() {
            return btWrapper;
        }
        int basicTypeSlots() {
            return btWrapper.stackSlots();
        }


                    static BasicType basicType(byte type) {
            return ALL_TYPES[type];
        }
        static BasicType basicType(char type) {
            switch (type) {
                case 'L': return L_TYPE;
                case 'I': return I_TYPE;
                case 'J': return J_TYPE;
                case 'F': return F_TYPE;
                case 'D': return D_TYPE;
                case 'V': return V_TYPE;
                // 所有子字类型都表示为整数
                case 'Z':
                case 'B':
                case 'S':
                case 'C':
                    return I_TYPE;
                default:
                    throw newInternalError("未知类型字符: '"+type+"'");
            }
        }
        static BasicType basicType(Wrapper type) {
            char c = type.basicTypeChar();
            return basicType(c);
        }
        static BasicType basicType(Class<?> type) {
            if (!type.isPrimitive())  return L_TYPE;
            return basicType(Wrapper.forPrimitiveType(type));
        }

        static char basicTypeChar(Class<?> type) {
            return basicType(type).btChar;
        }
        static BasicType[] basicTypes(List<Class<?>> types) {
            BasicType[] btypes = new BasicType[types.size()];
            for (int i = 0; i < btypes.length; i++) {
                btypes[i] = basicType(types.get(i));
            }
            return btypes;
        }
        static BasicType[] basicTypes(String types) {
            BasicType[] btypes = new BasicType[types.length()];
            for (int i = 0; i < btypes.length; i++) {
                btypes[i] = basicType(types.charAt(i));
            }
            return btypes;
        }
        static byte[] basicTypesOrd(BasicType[] btypes) {
            byte[] ords = new byte[btypes.length];
            for (int i = 0; i < btypes.length; i++) {
                ords[i] = (byte)btypes[i].ordinal();
            }
            return ords;
        }
        static boolean isBasicTypeChar(char c) {
            return "LIJFDV".indexOf(c) >= 0;
        }
        static boolean isArgBasicTypeChar(char c) {
            return "LIJFD".indexOf(c) >= 0;
        }

        static { assert(checkBasicType()); }
        private static boolean checkBasicType() {
            for (int i = 0; i < ARG_TYPE_LIMIT; i++) {
                assert ARG_TYPES[i].ordinal() == i;
                assert ARG_TYPES[i] == ALL_TYPES[i];
            }
            for (int i = 0; i < TYPE_LIMIT; i++) {
                assert ALL_TYPES[i].ordinal() == i;
            }
            assert ALL_TYPES[TYPE_LIMIT - 1] == V_TYPE;
            assert !Arrays.asList(ARG_TYPES).contains(V_TYPE);
            return true;
        }
    }

    LambdaForm(String debugName,
               int arity, Name[] names, int result) {
        this(debugName, arity, names, result, /*forceInline=*/true, /*customized=*/null);
    }
    LambdaForm(String debugName,
               int arity, Name[] names, int result, boolean forceInline, MethodHandle customized) {
        assert(namesOK(arity, names));
        this.arity = arity;
        this.result = fixResult(result, names);
        this.names = names.clone();
        this.debugName = fixDebugName(debugName);
        this.forceInline = forceInline;
        this.customized = customized;
        int maxOutArity = normalize();
        if (maxOutArity > MethodType.MAX_MH_INVOKER_ARITY) {
            // 不能在非常高元数的表达式上使用LF解释器。
            assert(maxOutArity <= MethodType.MAX_JVM_ARITY);
            compileToBytecode();
        }
    }
    LambdaForm(String debugName,
               int arity, Name[] names) {
        this(debugName, arity, names, LAST_RESULT, /*forceInline=*/true, /*customized=*/null);
    }
    LambdaForm(String debugName,
               int arity, Name[] names, boolean forceInline) {
        this(debugName, arity, names, LAST_RESULT, forceInline, /*customized=*/null);
    }
    LambdaForm(String debugName,
               Name[] formals, Name[] temps, Name result) {
        this(debugName,
             formals.length, buildNames(formals, temps, result), LAST_RESULT, /*forceInline=*/true, /*customized=*/null);
    }
    LambdaForm(String debugName,
               Name[] formals, Name[] temps, Name result, boolean forceInline) {
        this(debugName,
             formals.length, buildNames(formals, temps, result), LAST_RESULT, forceInline, /*customized=*/null);
    }

    private static Name[] buildNames(Name[] formals, Name[] temps, Name result) {
        int arity = formals.length;
        int length = arity + temps.length + (result == null ? 0 : 1);
        Name[] names = Arrays.copyOf(formals, length);
        System.arraycopy(temps, 0, names, arity, temps.length);
        if (result != null)
            names[length - 1] = result;
        return names;
    }

    private LambdaForm(String sig) {
        // 创建一个空白的lambda形式，返回常量零或null。
        // 它用作管理类似非空形式调用的模板。
        // 仅从getPreparedForm调用。
        assert(isValidSignature(sig));
        this.arity = signatureArity(sig);
        this.result = (signatureReturn(sig) == V_TYPE ? -1 : arity);
        this.names = buildEmptyNames(arity, sig);
        this.debugName = "LF.zero";
        this.forceInline = true;
        this.customized = null;
        assert(nameRefsAreLegal());
        assert(isEmpty());
        assert(sig.equals(basicTypeSignature())) : sig + " != " + basicTypeSignature();
    }

    private static Name[] buildEmptyNames(int arity, String basicTypeSignature) {
        assert(isValidSignature(basicTypeSignature));
        int resultPos = arity + 1;  // 跳过 '_'
        if (arity < 0 || basicTypeSignature.length() != resultPos+1)
            throw new IllegalArgumentException("错误的元数: "+basicTypeSignature);
        int numRes = (basicType(basicTypeSignature.charAt(resultPos)) == V_TYPE ? 0 : 1);
        Name[] names = arguments(numRes, basicTypeSignature.substring(0, arity));
        for (int i = 0; i < numRes; i++) {
            Name zero = new Name(constantZero(basicType(basicTypeSignature.charAt(resultPos + i))));
            names[arity + i] = zero.newIndex(arity + i);
        }
        return names;
    }


                private static int fixResult(int result, Name[] names) {
        if (result == LAST_RESULT)
            result = names.length - 1;  // 可能仍然是 void
        if (result >= 0 && names[result].type == V_TYPE)
            result = VOID_RESULT;
        return result;
    }

    private static String fixDebugName(String debugName) {
        if (DEBUG_NAME_COUNTERS != null) {
            int under = debugName.indexOf('_');
            int length = debugName.length();
            if (under < 0)  under = length;
            String debugNameStem = debugName.substring(0, under);
            Integer ctr;
            synchronized (DEBUG_NAME_COUNTERS) {
                ctr = DEBUG_NAME_COUNTERS.get(debugNameStem);
                if (ctr == null)  ctr = 0;
                DEBUG_NAME_COUNTERS.put(debugNameStem, ctr+1);
            }
            StringBuilder buf = new StringBuilder(debugNameStem);
            buf.append('_');
            int leadingZero = buf.length();
            buf.append((int) ctr);
            for (int i = buf.length() - leadingZero; i < 3; i++)
                buf.insert(leadingZero, '0');
            if (under < length) {
                ++under;    // 跳过 "_"
                while (under < length && Character.isDigit(debugName.charAt(under))) {
                    ++under;
                }
                if (under < length && debugName.charAt(under) == '_')  ++under;
                if (under < length)
                    buf.append('_').append(debugName, under, length);
            }
            return buf.toString();
        }
        return debugName;
    }

    private static boolean namesOK(int arity, Name[] names) {
        for (int i = 0; i < names.length; i++) {
            Name n = names[i];
            assert(n != null) : "n is null";
            if (i < arity)
                assert( n.isParam()) : n + " is not param at " + i;
            else
                assert(!n.isParam()) : n + " is param at " + i;
        }
        return true;
    }

    /** 为特定的 MethodHandle 定制 LambdaForm */
    LambdaForm customize(MethodHandle mh) {
        LambdaForm customForm = new LambdaForm(debugName, arity, names, result, forceInline, mh);
        if (COMPILE_THRESHOLD > 0 && isCompiled) {
            // 如果共享的 LambdaForm 已经被编译，也编译定制的版本。
            customForm.compileToBytecode();
        }
        customForm.transformCache = this; // LambdaFormEditor 应该始终使用未定制的版本。
        return customForm;
    }

    /** 获取未定制的 LambdaForm 版本 */
    LambdaForm uncustomize() {
        if (customized == null) {
            return this;
        }
        assert(transformCache != null); // 定制的 LambdaForm 应该始终有一个链接到未定制的版本。
        LambdaForm uncustomizedForm = (LambdaForm)transformCache;
        if (COMPILE_THRESHOLD > 0 && isCompiled) {
            // 如果定制的 LambdaForm 已经被编译，也编译未定制的版本。
            uncustomizedForm.compileToBytecode();
        }
        return uncustomizedForm;
    }

    /** 重新编号和/或替换参数，使它们被内部化并规范编号。
     *  @return 名称中的最大参数列表长度（因为我们无论如何都要遍历它们）
     */
    private int normalize() {
        Name[] oldNames = null;
        int maxOutArity = 0;
        int changesStart = 0;
        for (int i = 0; i < names.length; i++) {
            Name n = names[i];
            if (!n.initIndex(i)) {
                if (oldNames == null) {
                    oldNames = names.clone();
                    changesStart = i;
                }
                names[i] = n.cloneWithIndex(i);
            }
            if (n.arguments != null && maxOutArity < n.arguments.length)
                maxOutArity = n.arguments.length;
        }
        if (oldNames != null) {
            int startFixing = arity;
            if (startFixing <= changesStart)
                startFixing = changesStart+1;
            for (int i = startFixing; i < names.length; i++) {
                Name fixed = names[i].replaceNames(oldNames, names, changesStart, i);
                names[i] = fixed.newIndex(i);
            }
        }
        assert(nameRefsAreLegal());
        int maxInterned = Math.min(arity, INTERNED_ARGUMENT_LIMIT);
        boolean needIntern = false;
        for (int i = 0; i < maxInterned; i++) {
            Name n = names[i], n2 = internArgument(n);
            if (n != n2) {
                names[i] = n2;
                needIntern = true;
            }
        }
        if (needIntern) {
            for (int i = arity; i < names.length; i++) {
                names[i].internArguments();
            }
        }
        assert(nameRefsAreLegal());
        return maxOutArity;
    }

    /**
     * 检查所有嵌入的 Name 引用是否可以本地化到此 lambda，
     * 并且在它们对应的定义之后正确排序。
     * <p>
     * 注意，一个 Name 可以属于多个 lambda，只要
     * 它在每个使用站点中具有相同的索引即可。
     * 这允许 Name 引用在构造新的 lambda 时自由重用，而不会引起混淆。
     */
    boolean nameRefsAreLegal() {
        assert(arity >= 0 && arity <= names.length);
        assert(result >= -1 && result < names.length);
        // 所有名称是否具有与其本地定义顺序一致的索引？
        for (int i = 0; i < arity; i++) {
            Name n = names[i];
            assert(n.index() == i) : Arrays.asList(n.index(), i);
            assert(n.isParam());
        }
        // 同样，所有本地名称引用
        for (int i = arity; i < names.length; i++) {
            Name n = names[i];
            assert(n.index() == i);
            for (Object arg : n.arguments) {
                if (arg instanceof Name) {
                    Name n2 = (Name) arg;
                    int i2 = n2.index;
                    assert(0 <= i2 && i2 < names.length) : n.debugString() + ": 0 <= i2 && i2 < names.length: 0 <= " + i2 + " < " + names.length;
                    assert(names[i2] == n2) : Arrays.asList("-1-", i, "-2-", n.debugString(), "-3-", i2, "-4-", n2.debugString(), "-5-", names[i2].debugString(), "-6-", this);
                    assert(i2 < i);  // 引用必须在定义之后！
                }
            }
        }
        return true;
    }


                /** 调用给定参数的此表单。 */
    // final Object invoke(Object... args) throws Throwable {
    //     // NYI: 将此纳入快速路径？
    //     return interpretWithArguments(args);
    // }

    /** 报告返回类型。 */
    BasicType returnType() {
        if (result < 0)  return V_TYPE;
        Name n = names[result];
        return n.type;
    }

    /** 报告第 N 个参数类型。 */
    BasicType parameterType(int n) {
        return parameter(n).type;
    }

    /** 报告第 N 个参数名称。 */
    Name parameter(int n) {
        assert(n < arity);
        Name param = names[n];
        assert(param.isParam());
        return param;
    }

    /** 报告第 N 个参数类型约束。 */
    Object parameterConstraint(int n) {
        return parameter(n).constraint;
    }

    /** 报告参数数量。 */
    int arity() {
        return arity;
    }

    /** 报告表达式数量（非参数名称）。 */
    int expressionCount() {
        return names.length - arity;
    }

    /** 返回与我的基本类型签名对应的方法类型。 */
    MethodType methodType() {
        return signatureType(basicTypeSignature());
    }
    /** 返回 ABC_Z，其中 ABC 是参数类型字符，Z 是返回类型字符。 */
    final String basicTypeSignature() {
        StringBuilder buf = new StringBuilder(arity() + 3);
        for (int i = 0, a = arity(); i < a; i++)
            buf.append(parameterType(i).basicTypeChar());
        return buf.append('_').append(returnType().basicTypeChar()).toString();
    }
    static int signatureArity(String sig) {
        assert(isValidSignature(sig));
        return sig.indexOf('_');
    }
    static BasicType signatureReturn(String sig) {
        return basicType(sig.charAt(signatureArity(sig) + 1));
    }
    static boolean isValidSignature(String sig) {
        int arity = sig.indexOf('_');
        if (arity < 0)  return false;  // 必须是 *_* 形式
        int siglen = sig.length();
        if (siglen != arity + 2)  return false;  // *_X
        for (int i = 0; i < siglen; i++) {
            if (i == arity)  continue;  // 跳过 '_'
            char c = sig.charAt(i);
            if (c == 'V')
                return (i == siglen - 1 && arity == siglen - 2);
            if (!isArgBasicTypeChar(c))  return false; // 必须是 [LIJFD]
        }
        return true;  // [LIJFD]*_[LIJFDV]
    }
    static MethodType signatureType(String sig) {
        Class<?>[] ptypes = new Class<?>[signatureArity(sig)];
        for (int i = 0; i < ptypes.length; i++)
            ptypes[i] = basicType(sig.charAt(i)).btClass;
        Class<?> rtype = signatureReturn(sig).btClass;
        return MethodType.methodType(rtype, ptypes);
    }

    /*
     * 代码生成问题：
     *
     * 编译的 LFs 通常应该是可重用的。
     * 最大的问题是如何决定何时将名称拉入字节码，而不是从 MH 数据中加载其具体形式。
     *
     * 例如，asType 包装器可能需要在调用 MH 后执行强制转换。
     * 强制转换的目标类型可以作为 LF 本身的常量放置。
     * 这将迫使强制转换类型编译到 MH 的字节码和本机代码中。
     * 或者，可以在 LF 中擦除强制转换的目标类型，并从 MH 数据中加载。
     * （稍后，如果整个 MH 被内联，数据将流入 LF 的内联实例，
     * 作为常量，最终结果将是最佳的强制转换。）
     *
     * 这种强制转换类型的擦除可以用于任何引用类型。
     * 它也可以用于整个方法句柄。擦除方法句柄可能会留下
     * 可以正确执行任何给定类型的 MH 的 LF 代码，并从包含的 MH 数据中加载所需的 MH。
     * 或者，擦除甚至可以擦除预期的 MT。
     *
     * 对于直接 MH，目标的 MemberName 可以被擦除，并从包含的直接 MH 中加载。
     * 作为一个简单的例子，所有 int 值的非静态字段获取器的 LF
     * 将对其输入参数执行强制转换（转换为从 MemberName 导出的非常量基类型）
     * 并从输入对象中加载一个整数值（在从 MemberName 导出的非常量偏移量处）。
     * 这样的 MN 擦除的 LF 可以在有常量包含 DMH 可用的情况下内联回优化代码，
     * 以从其数据中提供常量 MN。
     *
     * 这里的主要问题是保持 LF 的合理通用性，同时确保热点将内联良好的实例。
     * “合理通用”意味着我们不会生成在优化形式上没有差异的重复版本的字节码或机器代码。
     * 重复版本的机器代码会有不必要的开销：
     * (a) 冗余的编译工作和 (b) 额外的 I$ 压力。
     * 为了控制重复版本，我们需要准备好从 LF 中擦除细节并将它们移入 MH 数据，
     * 只要这些细节与显著优化无关。“显著”意味着实际热点的代码优化。
     *
     * 实现这一点可能需要动态拆分 MH，通过在同一 MH 上用更专业的 LF 替换通用 LF，
     * 如果 (a) MH 频繁执行且 (b) MH 不能内联到包含的调用者中，例如 invokedynamic。
     *
     * 不再使用的编译 LF 应该可以被 GC 回收。
     * 如果它们包含非 BCP 引用，它们应该与它们依赖的类加载器正确关联。
     * 这可能意味着可重用的编译 LF 将在相关的类加载器上进行制表（索引），
     * 或者缓存它们的表将具有弱链接。
     */

    /**
     * 使此 LF 可直接作为 MethodHandle 的一部分执行。
     * 不变性：每个被调用的 MH 必须在调用前准备其 LF。
     * （原则上，JVM 可以非常懒惰地执行此操作，
     * 作为某种预调用链接步骤。）
     */
    public void prepare() {
        if (COMPILE_THRESHOLD == 0 && !isCompiled) {
            compileToBytecode();
        }
        if (this.vmentry != null) {
            // 已经准备好了（例如，原始 DMH 调用形式）
            return;
        }
        LambdaForm prep = getPreparedForm(basicTypeSignature());
        this.vmentry = prep.vmentry;
        // TO DO: 可能添加 invokeGeneric, invokeWithArguments
    }


                /** 为这种形式生成可优化的字节码。 */
    MemberName compileToBytecode() {
        if (vmentry != null && isCompiled) {
            return vmentry;  // 已经以某种方式编译
        }
        MethodType invokerType = methodType();
        assert(vmentry == null || vmentry.getMethodType().basicType().equals(invokerType));
        try {
            vmentry = InvokerBytecodeGenerator.generateCustomizedCode(this, invokerType);
            if (TRACE_INTERPRETER)
                traceInterpreter("compileToBytecode", this);
            isCompiled = true;
            return vmentry;
        } catch (Error | Exception ex) {
            throw newInternalError(this.toString(), ex);
        }
    }

    private static void computeInitialPreparedForms() {
        // 查找所有预定义的调用者，并将它们与规范的空 lambda 形式关联。
        for (MemberName m : MemberName.getFactory().getMethods(LambdaForm.class, false, null, null, null)) {
            if (!m.isStatic() || !m.isPackage())  continue;
            MethodType mt = m.getMethodType();
            if (mt.parameterCount() > 0 &&
                mt.parameterType(0) == MethodHandle.class &&
                m.getName().startsWith("interpret_")) {
                String sig = basicTypeSignature(mt);
                assert(m.getName().equals("interpret" + sig.substring(sig.indexOf('_'))));
                LambdaForm form = new LambdaForm(sig);
                form.vmentry = m;
                form = mt.form().setCachedLambdaForm(MethodTypeForm.LF_INTERPRET, form);
            }
        }
    }

    // 将此设置为 false 以禁用在此文件中定义的 interpret_L 方法的使用。
    private static final boolean USE_PREDEFINED_INTERPRET_METHODS = true;

    // 以下为预定义的精确调用者。系统必须为每个不同的签名构建一个单独的调用者。
    static Object interpret_L(MethodHandle mh) throws Throwable {
        Object[] av = {mh};
        String sig = null;
        assert(argumentTypesMatch(sig = "L_L", av));
        Object res = mh.form.interpretWithArguments(av);
        assert(returnTypesMatch(sig, av, res));
        return res;
    }
    static Object interpret_L(MethodHandle mh, Object x1) throws Throwable {
        Object[] av = {mh, x1};
        String sig = null;
        assert(argumentTypesMatch(sig = "LL_L", av));
        Object res = mh.form.interpretWithArguments(av);
        assert(returnTypesMatch(sig, av, res));
        return res;
    }
    static Object interpret_L(MethodHandle mh, Object x1, Object x2) throws Throwable {
        Object[] av = {mh, x1, x2};
        String sig = null;
        assert(argumentTypesMatch(sig = "LLL_L", av));
        Object res = mh.form.interpretWithArguments(av);
        assert(returnTypesMatch(sig, av, res));
        return res;
    }
    private static LambdaForm getPreparedForm(String sig) {
        MethodType mtype = signatureType(sig);
        LambdaForm prep =  mtype.form().cachedLambdaForm(MethodTypeForm.LF_INTERPRET);
        if (prep != null)  return prep;
        assert(isValidSignature(sig));
        prep = new LambdaForm(sig);
        prep.vmentry = InvokerBytecodeGenerator.generateLambdaFormInterpreterEntryPoint(sig);
        return mtype.form().setCachedLambdaForm(MethodTypeForm.LF_INTERPRET, prep);
    }

    // 以下几个例程仅在断言表达式中调用
    // 它们验证内置调用者处理正确的原始数据类型。
    private static boolean argumentTypesMatch(String sig, Object[] av) {
        int arity = signatureArity(sig);
        assert(av.length == arity) : "av.length == arity: av.length=" + av.length + ", arity=" + arity;
        assert(av[0] instanceof MethodHandle) : "av[0] not instace of MethodHandle: " + av[0];
        MethodHandle mh = (MethodHandle) av[0];
        MethodType mt = mh.type();
        assert(mt.parameterCount() == arity-1);
        for (int i = 0; i < av.length; i++) {
            Class<?> pt = (i == 0 ? MethodHandle.class : mt.parameterType(i-1));
            assert(valueMatches(basicType(sig.charAt(i)), pt, av[i]));
        }
        return true;
    }
    private static boolean valueMatches(BasicType tc, Class<?> type, Object x) {
        // 以下行是必需的，因为 (...)void 方法句柄可以使用非 void 调用者
        if (type == void.class)  tc = V_TYPE;   // 可以丢弃任何类型的值
        assert tc == basicType(type) : tc + " == basicType(" + type + ")=" + basicType(type);
        switch (tc) {
        case I_TYPE: assert checkInt(type, x)   : "checkInt(" + type + "," + x +")";   break;
        case J_TYPE: assert x instanceof Long   : "instanceof Long: " + x;             break;
        case F_TYPE: assert x instanceof Float  : "instanceof Float: " + x;            break;
        case D_TYPE: assert x instanceof Double : "instanceof Double: " + x;           break;
        case L_TYPE: assert checkRef(type, x)   : "checkRef(" + type + "," + x + ")";  break;
        case V_TYPE: break;  // 允许任何值；将被丢弃
        default:  assert(false);
        }
        return true;
    }
    private static boolean returnTypesMatch(String sig, Object[] av, Object res) {
        MethodHandle mh = (MethodHandle) av[0];
        return valueMatches(signatureReturn(sig), mh.type().returnType(), res);
    }
    private static boolean checkInt(Class<?> type, Object x) {
        assert(x instanceof Integer);
        if (type == int.class)  return true;
        Wrapper w = Wrapper.forBasicType(type);
        assert(w.isSubwordOrInt());
        Object x1 = Wrapper.INT.wrap(w.wrap(x));
        return x.equals(x1);
    }
    private static boolean checkRef(Class<?> type, Object x) {
        assert(!type.isPrimitive());
        if (x == null)  return true;
        if (type.isInterface())  return true;
        return type.isInstance(x);
    }

    /** 如果调用次数达到阈值，我们将生成字节码并随后调用它。 */
    private static final int COMPILE_THRESHOLD;
    static {
        COMPILE_THRESHOLD = Math.max(-1, MethodHandleStatics.COMPILE_THRESHOLD);
    }
    private int invocationCounter = 0;


                @Hidden
    @DontInline
    /** 解释性地调用此形式的给定参数。 */
    Object interpretWithArguments(Object... argumentValues) throws Throwable {
        if (TRACE_INTERPRETER)
            return interpretWithArgumentsTracing(argumentValues);
        checkInvocationCounter();
        assert(arityCheck(argumentValues));
        Object[] values = Arrays.copyOf(argumentValues, names.length);
        for (int i = argumentValues.length; i < values.length; i++) {
            values[i] = interpretName(names[i], values);
        }
        Object rv = (result < 0) ? null : values[result];
        assert(resultCheck(argumentValues, rv));
        return rv;
    }

    @Hidden
    @DontInline
    /** 在此形式内评估单个名称，将其函数应用于其参数。 */
    Object interpretName(Name name, Object[] values) throws Throwable {
        if (TRACE_INTERPRETER)
            traceInterpreter("| interpretName", name.debugString(), (Object[]) null);
        Object[] arguments = Arrays.copyOf(name.arguments, name.arguments.length, Object[].class);
        for (int i = 0; i < arguments.length; i++) {
            Object a = arguments[i];
            if (a instanceof Name) {
                int i2 = ((Name)a).index();
                assert(names[i2] == a);
                a = values[i2];
                arguments[i] = a;
            }
        }
        return name.function.invokeWithArguments(arguments);
    }

    private void checkInvocationCounter() {
        if (COMPILE_THRESHOLD != 0 &&
            invocationCounter < COMPILE_THRESHOLD) {
            invocationCounter++;  // 良性的竞争条件
            if (invocationCounter >= COMPILE_THRESHOLD) {
                // 用此LF的字节码版本替换vmentry。
                compileToBytecode();
            }
        }
    }
    Object interpretWithArgumentsTracing(Object... argumentValues) throws Throwable {
        traceInterpreter("[ interpretWithArguments", this, argumentValues);
        if (invocationCounter < COMPILE_THRESHOLD) {
            int ctr = invocationCounter++;  // 良性的竞争条件
            traceInterpreter("| invocationCounter", ctr);
            if (invocationCounter >= COMPILE_THRESHOLD) {
                compileToBytecode();
            }
        }
        Object rval;
        try {
            assert(arityCheck(argumentValues));
            Object[] values = Arrays.copyOf(argumentValues, names.length);
            for (int i = argumentValues.length; i < values.length; i++) {
                values[i] = interpretName(names[i], values);
            }
            rval = (result < 0) ? null : values[result];
        } catch (Throwable ex) {
            traceInterpreter("] throw =>", ex);
            throw ex;
        }
        traceInterpreter("] return =>", rval);
        return rval;
    }

    static void traceInterpreter(String event, Object obj, Object... args) {
        if (TRACE_INTERPRETER) {
            System.out.println("LFI: "+event+" "+(obj != null ? obj : "")+(args != null && args.length != 0 ? Arrays.asList(args) : ""));
        }
    }
    static void traceInterpreter(String event, Object obj) {
        traceInterpreter(event, obj, (Object[])null);
    }
    private boolean arityCheck(Object[] argumentValues) {
        assert(argumentValues.length == arity) : arity+"!="+Arrays.asList(argumentValues)+".length";
        // 还检查前导（接收者）参数是否以某种方式绑定到此LF：
        assert(argumentValues[0] instanceof MethodHandle) : "not MH: " + argumentValues[0];
        MethodHandle mh = (MethodHandle) argumentValues[0];
        assert(mh.internalForm() == this);
        // 注意：将来参数#0也可以是接口包装器
        argumentTypesMatch(basicTypeSignature(), argumentValues);
        return true;
    }
    private boolean resultCheck(Object[] argumentValues, Object result) {
        MethodHandle mh = (MethodHandle) argumentValues[0];
        MethodType mt = mh.type();
        assert(valueMatches(returnType(), mt.returnType(), result));
        return true;
    }

    private boolean isEmpty() {
        if (result < 0)
            return (names.length == arity);
        else if (result == arity && names.length == arity + 1)
            return names[arity].isConstantZero();
        else
            return false;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(debugName+"=Lambda(");
        for (int i = 0; i < names.length; i++) {
            if (i == arity)  buf.append(")=>{");
            Name n = names[i];
            if (i >= arity)  buf.append("\n    ");
            buf.append(n.paramString());
            if (i < arity) {
                if (i+1 < arity)  buf.append(",");
                continue;
            }
            buf.append("=").append(n.exprString());
            buf.append(";");
        }
        if (arity == names.length)  buf.append(")=>{");
        buf.append(result < 0 ? "void" : names[result]).append("}");
        if (TRACE_INTERPRETER) {
            // 额外的详细信息：
            buf.append(":").append(basicTypeSignature());
            buf.append("/").append(vmentry);
        }
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LambdaForm && equals((LambdaForm)obj);
    }
    public boolean equals(LambdaForm that) {
        if (this.result != that.result)  return false;
        return Arrays.equals(this.names, that.names);
    }
    public int hashCode() {
        return result + 31 * Arrays.hashCode(names);
    }
    LambdaFormEditor editor() {
        return LambdaFormEditor.lambdaFormEditor(this);
    }

    boolean contains(Name name) {
        int pos = name.index();
        if (pos >= 0) {
            return pos < names.length && name.equals(names[pos]);
        }
        for (int i = arity; i < names.length; i++) {
            if (name.equals(names[i]))
                return true;
        }
        return false;
    }

    LambdaForm addArguments(int pos, BasicType... types) {
        // names数组在槽0中有MH；跳过它。
        int argpos = pos + 1;
        assert(argpos <= arity);
        int length = names.length;
        int inTypes = types.length;
        Name[] names2 = Arrays.copyOf(names, length + inTypes);
        int arity2 = arity + inTypes;
        int result2 = result;
        if (result2 >= argpos)
            result2 += inTypes;
        // 注意：LF构造函数将重命名names2[argpos...]。
        // 为新参数腾出空间（移动临时变量）。
        System.arraycopy(names, argpos, names2, argpos + inTypes, length - argpos);
        for (int i = 0; i < inTypes; i++) {
            names2[argpos + i] = new Name(types[i]);
        }
        return new LambdaForm(debugName, arity2, names2, result2);
    }


                LambdaForm addArguments(int pos, List<Class<?>> types) {
        return addArguments(pos, basicTypes(types));
    }

    LambdaForm permuteArguments(int skip, int[] reorder, BasicType[] types) {
        // 注意: 当 inArg = reorder[outArg] 时，outArg 由 inArg 的副本提供。
        // 类型是新（传入）参数的类型。
        int length = names.length;
        int inTypes = types.length;
        int outArgs = reorder.length;
        assert(skip+outArgs == arity);
        assert(permutedTypesMatch(reorder, types, names, skip));
        int pos = 0;
        // 跳过重新排序的简单部分：
        while (pos < outArgs && reorder[pos] == pos)  pos += 1;
        Name[] names2 = new Name[length - outArgs + inTypes];
        System.arraycopy(names, 0, names2, 0, skip+pos);
        // 复制主体：
        int bodyLength = length - arity;
        System.arraycopy(names, skip+outArgs, names2, skip+inTypes, bodyLength);
        int arity2 = names2.length - bodyLength;
        int result2 = result;
        if (result2 >= 0) {
            if (result2 < skip+outArgs) {
                // 返回对应的 inArg
                result2 = reorder[result2-skip];
            } else {
                result2 = result2 - outArgs + inTypes;
            }
        }
        // 重新处理主体中的名称：
        for (int j = pos; j < outArgs; j++) {
            Name n = names[skip+j];
            int i = reorder[j];
            // 用 names2[skip+i] 替换 names[skip+j]
            Name n2 = names2[skip+i];
            if (n2 == null)
                names2[skip+i] = n2 = new Name(types[i]);
            else
                assert(n2.type == types[i]);
            for (int k = arity2; k < names2.length; k++) {
                names2[k] = names2[k].replaceName(n, n2);
            }
        }
        // 一些名称未使用，但必须填充
        for (int i = skip+pos; i < arity2; i++) {
            if (names2[i] == null)
                names2[i] = argument(i, types[i - skip]);
        }
        for (int j = arity; j < names.length; j++) {
            int i = j - arity + arity2;
            // 用 names[j] 替换 names2[i]
            Name n = names[j];
            Name n2 = names2[i];
            if (n != n2) {
                for (int k = i+1; k < names2.length; k++) {
                    names2[k] = names2[k].replaceName(n, n2);
                }
            }
        }
        return new LambdaForm(debugName, arity2, names2, result2);
    }

    static boolean permutedTypesMatch(int[] reorder, BasicType[] types, Name[] names, int skip) {
        int inTypes = types.length;
        int outArgs = reorder.length;
        for (int i = 0; i < outArgs; i++) {
            assert(names[skip+i].isParam());
            assert(names[skip+i].type == types[reorder[i]]);
        }
        return true;
    }

    static class NamedFunction {
        final MemberName member;
        @Stable MethodHandle resolvedHandle;
        @Stable MethodHandle invoker;

        NamedFunction(MethodHandle resolvedHandle) {
            this(resolvedHandle.internalMemberName(), resolvedHandle);
        }
        NamedFunction(MemberName member, MethodHandle resolvedHandle) {
            this.member = member;
            this.resolvedHandle = resolvedHandle;
             // 以下断言几乎总是正确的，但在某些边缘情况下会失败，例如 PrivateInvokeTest。
             // assert(!isInvokeBasic(member));
        }
        NamedFunction(MethodType basicInvokerType) {
            assert(basicInvokerType == basicInvokerType.basicType()) : basicInvokerType;
            if (basicInvokerType.parameterSlotCount() < MethodType.MAX_MH_INVOKER_ARITY) {
                this.resolvedHandle = basicInvokerType.invokers().basicInvoker();
                this.member = resolvedHandle.internalMemberName();
            } else {
                // 必要的，以通过 BigArityTest
                this.member = Invokers.invokeBasicMethod(basicInvokerType);
            }
            assert(isInvokeBasic(member));
        }

        private static boolean isInvokeBasic(MemberName member) {
            return member != null &&
                   member.getDeclaringClass() == MethodHandle.class &&
                  "invokeBasic".equals(member.getName());
        }

        // 以下 3 个构造函数用于打破对 MH.invokeStatic 等的循环依赖。
        // 任何包含此类成员的 LambdaForm 都不可解释。
        // 这是可以的，因为所有这些 LF 都使用特殊的原始 vmentry 点准备。
        // 即使没有 resolvedHandle，名称仍然可以编译和优化。
        NamedFunction(Method method) {
            this(new MemberName(method));
        }
        NamedFunction(Field field) {
            this(new MemberName(field));
        }
        NamedFunction(MemberName member) {
            this.member = member;
            this.resolvedHandle = null;
        }

        MethodHandle resolvedHandle() {
            if (resolvedHandle == null)  resolve();
            return resolvedHandle;
        }

        void resolve() {
            resolvedHandle = DirectMethodHandle.make(member);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null) return false;
            if (!(other instanceof NamedFunction)) return false;
            NamedFunction that = (NamedFunction) other;
            return this.member != null && this.member.equals(that.member);
        }

        @Override
        public int hashCode() {
            if (member != null)
                return member.hashCode();
            return super.hashCode();
        }

        // 将预定义的 NamedFunction 调用者放入表中。
        static void initializeInvokers() {
            for (MemberName m : MemberName.getFactory().getMethods(NamedFunction.class, false, null, null, null)) {
                if (!m.isStatic() || !m.isPackage())  continue;
                MethodType type = m.getMethodType();
                if (type.equals(INVOKER_METHOD_TYPE) &&
                    m.getName().startsWith("invoke_")) {
                    String sig = m.getName().substring("invoke_".length());
                    int arity = LambdaForm.signatureArity(sig);
                    MethodType srcType = MethodType.genericMethodType(arity);
                    if (LambdaForm.signatureReturn(sig) == V_TYPE)
                        srcType = srcType.changeReturnType(void.class);
                    MethodTypeForm typeForm = srcType.form();
                    typeForm.setCachedMethodHandle(MethodTypeForm.MH_NF_INV, DirectMethodHandle.make(m));
                }
            }
        }


                    // 以下为预定义的 NamedFunction 调用者。系统必须为每个不同的签名构建一个单独的调用者。
        /** 无返回值类型的调用者。 */
        @Hidden
        static Object invoke__V(MethodHandle mh, Object[] a) throws Throwable {
            assert(arityCheck(0, void.class, mh, a));
            mh.invokeBasic();
            return null;
        }
        @Hidden
        static Object invoke_L_V(MethodHandle mh, Object[] a) throws Throwable {
            assert(arityCheck(1, void.class, mh, a));
            mh.invokeBasic(a[0]);
            return null;
        }
        @Hidden
        static Object invoke_LL_V(MethodHandle mh, Object[] a) throws Throwable {
            assert(arityCheck(2, void.class, mh, a));
            mh.invokeBasic(a[0], a[1]);
            return null;
        }
        @Hidden
        static Object invoke_LLL_V(MethodHandle mh, Object[] a) throws Throwable {
            assert(arityCheck(3, void.class, mh, a));
            mh.invokeBasic(a[0], a[1], a[2]);
            return null;
        }
        @Hidden
        static Object invoke_LLLL_V(MethodHandle mh, Object[] a) throws Throwable {
            assert(arityCheck(4, void.class, mh, a));
            mh.invokeBasic(a[0], a[1], a[2], a[3]);
            return null;
        }
        @Hidden
        static Object invoke_LLLLL_V(MethodHandle mh, Object[] a) throws Throwable {
            assert(arityCheck(5, void.class, mh, a));
            mh.invokeBasic(a[0], a[1], a[2], a[3], a[4]);
            return null;
        }
        /** 对象返回值类型的调用者。 */
        @Hidden
        static Object invoke__L(MethodHandle mh, Object[] a) throws Throwable {
            assert(arityCheck(0, mh, a));
            return mh.invokeBasic();
        }
        @Hidden
        static Object invoke_L_L(MethodHandle mh, Object[] a) throws Throwable {
            assert(arityCheck(1, mh, a));
            return mh.invokeBasic(a[0]);
        }
        @Hidden
        static Object invoke_LL_L(MethodHandle mh, Object[] a) throws Throwable {
            assert(arityCheck(2, mh, a));
            return mh.invokeBasic(a[0], a[1]);
        }
        @Hidden
        static Object invoke_LLL_L(MethodHandle mh, Object[] a) throws Throwable {
            assert(arityCheck(3, mh, a));
            return mh.invokeBasic(a[0], a[1], a[2]);
        }
        @Hidden
        static Object invoke_LLLL_L(MethodHandle mh, Object[] a) throws Throwable {
            assert(arityCheck(4, mh, a));
            return mh.invokeBasic(a[0], a[1], a[2], a[3]);
        }
        @Hidden
        static Object invoke_LLLLL_L(MethodHandle mh, Object[] a) throws Throwable {
            assert(arityCheck(5, mh, a));
            return mh.invokeBasic(a[0], a[1], a[2], a[3], a[4]);
        }
        private static boolean arityCheck(int arity, MethodHandle mh, Object[] a) {
            return arityCheck(arity, Object.class, mh, a);
        }
        private static boolean arityCheck(int arity, Class<?> rtype, MethodHandle mh, Object[] a) {
            assert(a.length == arity)
                    : Arrays.asList(a.length, arity);
            assert(mh.type().basicType() == MethodType.genericMethodType(arity).changeReturnType(rtype))
                    : Arrays.asList(mh, rtype, arity);
            MemberName member = mh.internalMemberName();
            if (isInvokeBasic(member)) {
                assert(arity > 0);
                assert(a[0] instanceof MethodHandle);
                MethodHandle mh2 = (MethodHandle) a[0];
                assert(mh2.type().basicType() == MethodType.genericMethodType(arity-1).changeReturnType(rtype))
                        : Arrays.asList(member, mh2, rtype, arity);
            }
            return true;
        }

        static final MethodType INVOKER_METHOD_TYPE =
            MethodType.methodType(Object.class, MethodHandle.class, Object[].class);

        private static MethodHandle computeInvoker(MethodTypeForm typeForm) {
            typeForm = typeForm.basicType().form();  // 规范化为基本类型
            MethodHandle mh = typeForm.cachedMethodHandle(MethodTypeForm.MH_NF_INV);
            if (mh != null)  return mh;
            MemberName invoker = InvokerBytecodeGenerator.generateNamedFunctionInvoker(typeForm);  // 这可能需要一些时间
            mh = DirectMethodHandle.make(invoker);
            MethodHandle mh2 = typeForm.cachedMethodHandle(MethodTypeForm.MH_NF_INV);
            if (mh2 != null)  return mh2;  // 无害的竞争条件
            if (!mh.type().equals(INVOKER_METHOD_TYPE))
                throw newInternalError(mh.debugString());
            return typeForm.setCachedMethodHandle(MethodTypeForm.MH_NF_INV, mh);
        }

        @Hidden
        Object invokeWithArguments(Object... arguments) throws Throwable {
            // 如果我们有缓存的调用者，立即调用它。
            // 注意：调用者总是返回一个引用值。
            if (TRACE_INTERPRETER)  return invokeWithArgumentsTracing(arguments);
            assert(checkArgumentTypes(arguments, methodType()));
            return invoker().invokeBasic(resolvedHandle(), arguments);
        }

        @Hidden
        Object invokeWithArgumentsTracing(Object[] arguments) throws Throwable {
            Object rval;
            try {
                traceInterpreter("[ call", this, arguments);
                if (invoker == null) {
                    traceInterpreter("| getInvoker", this);
                    invoker();
                }
                if (resolvedHandle == null) {
                    traceInterpreter("| resolve", this);
                    resolvedHandle();
                }
                assert(checkArgumentTypes(arguments, methodType()));
                rval = invoker().invokeBasic(resolvedHandle(), arguments);
            } catch (Throwable ex) {
                traceInterpreter("] throw =>", ex);
                throw ex;
            }
            traceInterpreter("] return =>", rval);
            return rval;
        }

        private MethodHandle invoker() {
            if (invoker != null)  return invoker;
            // 获取一个调用者并缓存它。
            return invoker = computeInvoker(methodType().form());
        }


                    private static boolean checkArgumentTypes(Object[] arguments, MethodType methodType) {
            if (true)  return true;  // FIXME
            MethodType dstType = methodType.form().erasedType();
            MethodType srcType = dstType.basicType().wrap();
            Class<?>[] ptypes = new Class<?>[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                Object arg = arguments[i];
                Class<?> ptype = arg == null ? Object.class : arg.getClass();
                // 如果目标类型是基本类型，我们保留参数类型。
                ptypes[i] = dstType.parameterType(i).isPrimitive() ? ptype : Object.class;
            }
            MethodType argType = MethodType.methodType(srcType.returnType(), ptypes).wrap();
            assert(argType.isConvertibleTo(srcType)) : "错误的参数类型: 无法将 " + argType + " 转换为 " + srcType;
            return true;
        }

        MethodType methodType() {
            if (resolvedHandle != null)
                return resolvedHandle.type();
            else
                // 仅在引导期间为某些内部 LFs 使用
                return member.getInvocationType();
        }

        MemberName member() {
            assert(assertMemberIsConsistent());
            return member;
        }

        // 仅在断言中调用。
        private boolean assertMemberIsConsistent() {
            if (resolvedHandle instanceof DirectMethodHandle) {
                MemberName m = resolvedHandle.internalMemberName();
                assert(m.equals(member));
            }
            return true;
        }

        Class<?> memberDeclaringClassOrNull() {
            return (member == null) ? null : member.getDeclaringClass();
        }

        BasicType returnType() {
            return basicType(methodType().returnType());
        }

        BasicType parameterType(int n) {
            return basicType(methodType().parameterType(n));
        }

        int arity() {
            return methodType().parameterCount();
        }

        public String toString() {
            if (member == null)  return String.valueOf(resolvedHandle);
            return member.getDeclaringClass().getSimpleName()+"."+member.getName();
        }

        public boolean isIdentity() {
            return this.equals(identity(returnType()));
        }

        public boolean isConstantZero() {
            return this.equals(constantZero(returnType()));
        }

        public MethodHandleImpl.Intrinsic intrinsicName() {
            return resolvedHandle == null ? MethodHandleImpl.Intrinsic.NONE
                                          : resolvedHandle.intrinsicName();
        }
    }

    public static String basicTypeSignature(MethodType type) {
        char[] sig = new char[type.parameterCount() + 2];
        int sigp = 0;
        for (Class<?> pt : type.parameterList()) {
            sig[sigp++] = basicTypeChar(pt);
        }
        sig[sigp++] = '_';
        sig[sigp++] = basicTypeChar(type.returnType());
        assert(sigp == sig.length);
        return String.valueOf(sig);
    }
    public static String shortenSignature(String signature) {
        // 使方法名称中显示的签名更具可读性的技巧。
        final int NO_CHAR = -1, MIN_RUN = 3;
        int c0, c1 = NO_CHAR, c1reps = 0;
        StringBuilder buf = null;
        int len = signature.length();
        if (len < MIN_RUN)  return signature;
        for (int i = 0; i <= len; i++) {
            // 移入下一个字符：
            c0 = c1; c1 = (i == len ? NO_CHAR : signature.charAt(i));
            if (c1 == c0) { ++c1reps; continue; }
            // 移入下一个计数：
            int c0reps = c1reps; c1reps = 1;
            // 字符序列的结束
            if (c0reps < MIN_RUN) {
                if (buf != null) {
                    while (--c0reps >= 0)
                        buf.append((char)c0);
                }
                continue;
            }
            // 发现三个或更多连续字符
            if (buf == null)
                buf = new StringBuilder().append(signature, 0, i - c0reps);
            buf.append((char)c0).append(c0reps);
        }
        return (buf == null) ? signature : buf.toString();
    }

    static final class Name {
        final BasicType type;
        private short index;
        final NamedFunction function;
        final Object constraint;  // 如果不为 null，则为附加类型信息
        @Stable final Object[] arguments;

        private Name(int index, BasicType type, NamedFunction function, Object[] arguments) {
            this.index = (short)index;
            this.type = type;
            this.function = function;
            this.arguments = arguments;
            this.constraint = null;
            assert(this.index == index);
        }
        private Name(Name that, Object constraint) {
            this.index = that.index;
            this.type = that.type;
            this.function = that.function;
            this.arguments = that.arguments;
            this.constraint = constraint;
            assert(constraint == null || isParam());  // 仅参数有约束
            assert(constraint == null || constraint instanceof BoundMethodHandle.SpeciesData || constraint instanceof Class);
        }
        Name(MethodHandle function, Object... arguments) {
            this(new NamedFunction(function), arguments);
        }
        Name(MethodType functionType, Object... arguments) {
            this(new NamedFunction(functionType), arguments);
            assert(arguments[0] instanceof Name && ((Name)arguments[0]).type == L_TYPE);
        }
        Name(MemberName function, Object... arguments) {
            this(new NamedFunction(function), arguments);
        }
        Name(NamedFunction function, Object... arguments) {
            this(-1, function.returnType(), function, arguments = Arrays.copyOf(arguments, arguments.length, Object[].class));
            assert(arguments.length == function.arity()) : "参数数量不匹配: arguments.length=" + arguments.length + " == function.arity()=" + function.arity() + " in " + debugString();
            for (int i = 0; i < arguments.length; i++)
                assert(typesMatch(function.parameterType(i), arguments[i])) : "类型不匹配: function.parameterType(" + i + ")=" + function.parameterType(i) + ", arguments[" + i + "]=" + arguments[i] + " in " + debugString();
        }
        /** 创建给定类型的原始参数，具有预期索引。 */
        Name(int index, BasicType type) {
            this(index, type, null, null);
        }
        /** 创建给定类型的原始参数。 */
        Name(BasicType type) { this(-1, type); }


                    BasicType type() { return type; }
        int index() { return index; }
        boolean initIndex(int i) {
            if (index != i) {
                if (index != -1)  return false;
                index = (short)i;
            }
            return true;
        }
        char typeChar() {
            return type.btChar;
        }

        void resolve() {
            if (function != null)
                function.resolve();
        }

        Name newIndex(int i) {
            if (initIndex(i))  return this;
            return cloneWithIndex(i);
        }
        Name cloneWithIndex(int i) {
            Object[] newArguments = (arguments == null) ? null : arguments.clone();
            return new Name(i, type, function, newArguments).withConstraint(constraint);
        }
        Name withConstraint(Object constraint) {
            if (constraint == this.constraint)  return this;
            return new Name(this, constraint);
        }
        Name replaceName(Name oldName, Name newName) {  // FIXME: use replaceNames uniformly
            if (oldName == newName)  return this;
            @SuppressWarnings("LocalVariableHidesMemberVariable")
            Object[] arguments = this.arguments;
            if (arguments == null)  return this;
            boolean replaced = false;
            for (int j = 0; j < arguments.length; j++) {
                if (arguments[j] == oldName) {
                    if (!replaced) {
                        replaced = true;
                        arguments = arguments.clone();
                    }
                    arguments[j] = newName;
                }
            }
            if (!replaced)  return this;
            return new Name(function, arguments);
        }
        /** In the arguments of this Name, replace oldNames[i] pairwise by newNames[i].
         *  Limit such replacements to {@code start<=i<end}.  Return possibly changed self.
         */
        Name replaceNames(Name[] oldNames, Name[] newNames, int start, int end) {
            if (start >= end)  return this;
            @SuppressWarnings("LocalVariableHidesMemberVariable")
            Object[] arguments = this.arguments;
            boolean replaced = false;
        eachArg:
            for (int j = 0; j < arguments.length; j++) {
                if (arguments[j] instanceof Name) {
                    Name n = (Name) arguments[j];
                    int check = n.index;
                    // harmless check to see if the thing is already in newNames:
                    if (check >= 0 && check < newNames.length && n == newNames[check])
                        continue eachArg;
                    // n might not have the correct index: n != oldNames[n.index].
                    for (int i = start; i < end; i++) {
                        if (n == oldNames[i]) {
                            if (n == newNames[i])
                                continue eachArg;
                            if (!replaced) {
                                replaced = true;
                                arguments = arguments.clone();
                            }
                            arguments[j] = newNames[i];
                            continue eachArg;
                        }
                    }
                }
            }
            if (!replaced)  return this;
            return new Name(function, arguments);
        }
        void internArguments() {
            @SuppressWarnings("LocalVariableHidesMemberVariable")
            Object[] arguments = this.arguments;
            for (int j = 0; j < arguments.length; j++) {
                if (arguments[j] instanceof Name) {
                    Name n = (Name) arguments[j];
                    if (n.isParam() && n.index < INTERNED_ARGUMENT_LIMIT)
                        arguments[j] = internArgument(n);
                }
            }
        }
        boolean isParam() {
            return function == null;
        }
        boolean isConstantZero() {
            return !isParam() && arguments.length == 0 && function.isConstantZero();
        }

        public String toString() {
            return (isParam()?"a":"t")+(index >= 0 ? index : System.identityHashCode(this))+":"+typeChar();
        }
        public String debugString() {
            String s = paramString();
            return (function == null) ? s : s + "=" + exprString();
        }
        public String paramString() {
            String s = toString();
            Object c = constraint;
            if (c == null)
                return s;
            if (c instanceof Class)  c = ((Class<?>)c).getSimpleName();
            return s + "/" + c;
        }
        public String exprString() {
            if (function == null)  return toString();
            StringBuilder buf = new StringBuilder(function.toString());
            buf.append("(");
            String cma = "";
            for (Object a : arguments) {
                buf.append(cma); cma = ",";
                if (a instanceof Name || a instanceof Integer)
                    buf.append(a);
                else
                    buf.append("(").append(a).append(")");
            }
            buf.append(")");
            return buf.toString();
        }

        static boolean typesMatch(BasicType parameterType, Object object) {
            if (object instanceof Name) {
                return ((Name)object).type == parameterType;
            }
            switch (parameterType) {
                case I_TYPE:  return object instanceof Integer;
                case J_TYPE:  return object instanceof Long;
                case F_TYPE:  return object instanceof Float;
                case D_TYPE:  return object instanceof Double;
            }
            assert(parameterType == L_TYPE);
            return true;
        }

        /** Return the index of the last occurrence of n in the argument array.
         *  Return -1 if the name is not used.
         */
        int lastUseIndex(Name n) {
            if (arguments == null)  return -1;
            for (int i = arguments.length; --i >= 0; ) {
                if (arguments[i] == n)  return i;
            }
            return -1;
        }


                    /** 返回参数数组中 n 出现的次数。
         *  如果名称未使用，则返回 0。
         */
        int useCount(Name n) {
            if (arguments == null)  return 0;
            int count = 0;
            for (int i = arguments.length; --i >= 0; ) {
                if (arguments[i] == n)  ++count;
            }
            return count;
        }

        boolean contains(Name n) {
            return this == n || lastUseIndex(n) >= 0;
        }

        public boolean equals(Name that) {
            if (this == that)  return true;
            if (isParam())
                // 每个参数都是一个唯一的原子
                return false;  // this != that
            return
                //this.index == that.index &&
                this.type == that.type &&
                this.function.equals(that.function) &&
                Arrays.equals(this.arguments, that.arguments);
        }
        @Override
        public boolean equals(Object x) {
            return x instanceof Name && equals((Name)x);
        }
        @Override
        public int hashCode() {
            if (isParam())
                return index | (type.ordinal() << 8);
            return function.hashCode() ^ Arrays.hashCode(arguments);
        }
    }

    /** 返回包含 n 作为参数的最后一个名称的索引。
     *  如果名称未使用，则返回 -1。如果是返回值，则返回 names.length。
     */
    int lastUseIndex(Name n) {
        int ni = n.index, nmax = names.length;
        assert(names[ni] == n);
        if (result == ni)  return nmax;  // 一直活到最后
        for (int i = nmax; --i > ni; ) {
            if (names[i].lastUseIndex(n) >= 0)
                return i;
        }
        return -1;
    }

    /** 返回 n 作为参数或返回值的使用次数。 */
    int useCount(Name n) {
        int ni = n.index, nmax = names.length;
        int end = lastUseIndex(n);
        if (end < 0)  return 0;
        int count = 0;
        if (end == nmax) { count++; end--; }
        int beg = n.index() + 1;
        if (beg < arity)  beg = arity;
        for (int i = beg; i <= end; i++) {
            count += names[i].useCount(n);
        }
        return count;
    }

    static Name argument(int which, char type) {
        return argument(which, basicType(type));
    }
    static Name argument(int which, BasicType type) {
        if (which >= INTERNED_ARGUMENT_LIMIT)
            return new Name(which, type);
        return INTERNED_ARGUMENTS[type.ordinal()][which];
    }
    static Name internArgument(Name n) {
        assert(n.isParam()) : "not param: " + n;
        assert(n.index < INTERNED_ARGUMENT_LIMIT);
        if (n.constraint != null)  return n;
        return argument(n.index, n.type);
    }
    static Name[] arguments(int extra, String types) {
        int length = types.length();
        Name[] names = new Name[length + extra];
        for (int i = 0; i < length; i++)
            names[i] = argument(i, types.charAt(i));
        return names;
    }
    static Name[] arguments(int extra, char... types) {
        int length = types.length;
        Name[] names = new Name[length + extra];
        for (int i = 0; i < length; i++)
            names[i] = argument(i, types[i]);
        return names;
    }
    static Name[] arguments(int extra, List<Class<?>> types) {
        int length = types.size();
        Name[] names = new Name[length + extra];
        for (int i = 0; i < length; i++)
            names[i] = argument(i, basicType(types.get(i)));
        return names;
    }
    static Name[] arguments(int extra, Class<?>... types) {
        int length = types.length;
        Name[] names = new Name[length + extra];
        for (int i = 0; i < length; i++)
            names[i] = argument(i, basicType(types[i]));
        return names;
    }
    static Name[] arguments(int extra, MethodType types) {
        int length = types.parameterCount();
        Name[] names = new Name[length + extra];
        for (int i = 0; i < length; i++)
            names[i] = argument(i, basicType(types.parameterType(i)));
        return names;
    }
    static final int INTERNED_ARGUMENT_LIMIT = 10;
    private static final Name[][] INTERNED_ARGUMENTS
            = new Name[ARG_TYPE_LIMIT][INTERNED_ARGUMENT_LIMIT];
    static {
        for (BasicType type : BasicType.ARG_TYPES) {
            int ord = type.ordinal();
            for (int i = 0; i < INTERNED_ARGUMENTS[ord].length; i++) {
                INTERNED_ARGUMENTS[ord][i] = new Name(i, type);
            }
        }
    }

    private static final MemberName.Factory IMPL_NAMES = MemberName.getFactory();

    static LambdaForm identityForm(BasicType type) {
        return LF_identityForm[type.ordinal()];
    }
    static LambdaForm zeroForm(BasicType type) {
        return LF_zeroForm[type.ordinal()];
    }
    static NamedFunction identity(BasicType type) {
        return NF_identity[type.ordinal()];
    }
    static NamedFunction constantZero(BasicType type) {
        return NF_zero[type.ordinal()];
    }
    private static final LambdaForm[] LF_identityForm = new LambdaForm[TYPE_LIMIT];
    private static final LambdaForm[] LF_zeroForm = new LambdaForm[TYPE_LIMIT];
    private static final NamedFunction[] NF_identity = new NamedFunction[TYPE_LIMIT];
    private static final NamedFunction[] NF_zero = new NamedFunction[TYPE_LIMIT];
    private static void createIdentityForms() {
        for (BasicType type : BasicType.ALL_TYPES) {
            int ord = type.ordinal();
            char btChar = type.basicTypeChar();
            boolean isVoid = (type == V_TYPE);
            Class<?> btClass = type.btClass;
            MethodType zeType = MethodType.methodType(btClass);
            MethodType idType = isVoid ? zeType : zeType.appendParameterTypes(btClass);

            // 查找一些符号名称。可能不需要这些，但如果需要发出对字节码的直接引用，这会有所帮助。
            // Zero 是从带有常量零输入的 identity 函数调用构建的。
            MemberName idMem = new MemberName(LambdaForm.class, "identity_"+btChar, idType, REF_invokeStatic);
            MemberName zeMem = new MemberName(LambdaForm.class, "zero_"+btChar, zeType, REF_invokeStatic);
            try {
                zeMem = IMPL_NAMES.resolveOrFail(REF_invokeStatic, zeMem, null, NoSuchMethodException.class);
                idMem = IMPL_NAMES.resolveOrFail(REF_invokeStatic, idMem, null, NoSuchMethodException.class);
            } catch (IllegalAccessException|NoSuchMethodException ex) {
                throw newInternalError(ex);
            }


                        NamedFunction idFun = new NamedFunction(idMem);
            LambdaForm idForm;
            if (isVoid) {
                Name[] idNames = new Name[] { argument(0, L_TYPE) };
                idForm = new LambdaForm(idMem.getName(), 1, idNames, VOID_RESULT);
            } else {
                Name[] idNames = new Name[] { argument(0, L_TYPE), argument(1, type) };
                idForm = new LambdaForm(idMem.getName(), 2, idNames, 1);
            }
            LF_identityForm[ord] = idForm;
            NF_identity[ord] = idFun;

            NamedFunction zeFun = new NamedFunction(zeMem);
            LambdaForm zeForm;
            if (isVoid) {
                zeForm = idForm;
            } else {
                Object zeValue = Wrapper.forBasicType(btChar).zero();
                Name[] zeNames = new Name[] { argument(0, L_TYPE), new Name(idFun, zeValue) };
                zeForm = new LambdaForm(zeMem.getName(), 1, zeNames, 1);
            }
            LF_zeroForm[ord] = zeForm;
            NF_zero[ord] = zeFun;

            assert(idFun.isIdentity());
            assert(zeFun.isConstantZero());
            assert(new Name(zeFun).isConstantZero());
        }

        // 在单独的遍历中执行此操作，以便 SimpleMethodHandle.make 可以看到这些表。
        for (BasicType type : BasicType.ALL_TYPES) {
            int ord = type.ordinal();
            NamedFunction idFun = NF_identity[ord];
            LambdaForm idForm = LF_identityForm[ord];
            MemberName idMem = idFun.member;
            idFun.resolvedHandle = SimpleMethodHandle.make(idMem.getInvocationType(), idForm);

            NamedFunction zeFun = NF_zero[ord];
            LambdaForm zeForm = LF_zeroForm[ord];
            MemberName zeMem = zeFun.member;
            zeFun.resolvedHandle = SimpleMethodHandle.make(zeMem.getInvocationType(), zeForm);

            assert(idFun.isIdentity());
            assert(zeFun.isConstantZero());
            assert(new Name(zeFun).isConstantZero());
        }
    }

    // 避免在启动时调用 ValueConversions：
    private static int identity_I(int x) { return x; }
    private static long identity_J(long x) { return x; }
    private static float identity_F(float x) { return x; }
    private static double identity_D(double x) { return x; }
    private static Object identity_L(Object x) { return x; }
    private static void identity_V() { return; }  // 与 zeroV 相同，但没关系
    private static int zero_I() { return 0; }
    private static long zero_J() { return 0; }
    private static float zero_F() { return 0; }
    private static double zero_D() { return 0; }
    private static Object zero_L() { return null; }
    private static void zero_V() { return; }

    /**
     * 内部标记，用于字节编译的 LambdaForms。
     */
    /*non-public*/
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Compiled {
    }

    /**
     * 内部标记，用于 LambdaForm 解释器帧。
     */
    /*non-public*/
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Hidden {
    }

    private static final HashMap<String,Integer> DEBUG_NAME_COUNTERS;
    static {
        if (debugEnabled())
            DEBUG_NAME_COUNTERS = new HashMap<>();
        else
            DEBUG_NAME_COUNTERS = null;
    }

    // 将此内容放在最后，以便之前的静态初始化可以先运行。
    static {
        createIdentityForms();
        if (USE_PREDEFINED_INTERPRET_METHODS)
            computeInitialPreparedForms();
        NamedFunction.initializeInvokers();
    }

    // 以下 hack 是必要的，以便在执行此类的静态初始化时抑制 TRACE_INTERPRETER。
    // 过早地打开 TRACE_INTERPRETER 将导致在尝试跟踪 LambdaForm.<clinit> 期间发生的事件时
    // 出现堆栈溢出和其他不良行为。
    // 因此，不要将此行移至文件中的更高位置，也不要删除。
    private static final boolean TRACE_INTERPRETER = MethodHandleStatics.TRACE_INTERPRETER;
}
