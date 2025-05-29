
/*
 * 版权所有 (c) 2008, 2019, Oracle 和/或其附属公司。保留所有权利。
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

import sun.invoke.util.Wrapper;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import sun.invoke.util.BytecodeDescriptor;
import static java.lang.invoke.MethodHandleStatics.*;
import sun.invoke.util.VerifyType;

/**
 * 方法类型表示方法句柄接受和返回的参数和返回类型，或者方法句柄调用者传递和期望的参数和返回类型。
 * 方法类型必须在方法句柄及其所有调用者之间正确匹配，
 * JVM 的操作在调用 {@link MethodHandle#invokeExact MethodHandle.invokeExact}
 * 和 {@link MethodHandle#invoke MethodHandle.invoke} 时强制执行此匹配，以及在执行 {@code invokedynamic} 指令时。
 * <p>
 * 该结构由返回类型和任意数量的参数类型组成。
 * 类型（原始类型、{@code void} 和引用类型）由 {@link Class} 对象表示。
 * （为了便于说明，我们将 {@code void} 视为一种类型。
 * 实际上，它表示没有返回类型。）
 * <p>
 * 所有 {@code MethodType} 实例都是不可变的。
 * 如果两个实例相等，则它们可以完全互换。
 * 相等性仅取决于返回类型和参数类型的逐对对应关系，不受其他因素影响。
 * <p>
 * 该类型只能通过工厂方法创建。
 * 所有工厂方法都可能缓存值，但不保证缓存。
 * 一些工厂方法是静态的，而另一些是虚拟方法，这些方法修改前驱方法类型，例如，通过更改选定的参数。
 * <p>
 * 操作参数类型组的工厂方法系统地提供两个版本，以便可以使用 Java 数组和
 * Java 列表来处理参数类型组。
 * 查询方法 {@code parameterArray} 和 {@code parameterList}
 * 也提供了数组和列表之间的选择。
 * <p>
 * 有时从字节码指令（如 {@code invokedynamic}）派生 {@code MethodType} 对象，
 * 具体来说，从类文件常量池中与指令关联的类型描述符字符串派生。
 * <p>
 * 像类和字符串一样，方法类型也可以直接表示
 * 在类文件的常量池中作为常量。
 * 可以通过引用合适的 {@code CONSTANT_MethodType} 常量池条目的 {@code ldc} 指令加载方法类型。
 * 该条目引用了描述符字符串的 {@code CONSTANT_Utf8} 拼写。
 * （有关方法类型常量的完整详细信息，
 * 请参阅《Java 虚拟机规范》的第 4.4.8 节和第 5.4.3.5 节。）
 * <p>
 * 当 JVM 从描述符字符串实例化 {@code MethodType} 时，
 * 描述符中命名的所有类都必须可访问，并且将被加载。
 * （但这些类不必初始化，就像 {@code CONSTANT_Class} 一样。）
 * 此加载可能在首次派生 {@code MethodType} 对象之前的任何时间发生。
 * @author John Rose, JSR 292 EG
 */
public final
class MethodType implements java.io.Serializable {
    private static final long serialVersionUID = 292L;  // {rtype, {ptype...}}

    // rtype 和 ptypes 字段定义了方法类型的结构身份：
    private final Class<?>   rtype;
    private final Class<?>[] ptypes;

    // 剩余的字段是各种缓存：
    private @Stable MethodTypeForm form; // 擦除形式，加上关于原始类型的缓存数据
    private @Stable Object wrapAlt;  // 替代的包装/解包版本和
                                     // 用于 readObject 和 readResolve 的私有通信
    private @Stable Invokers invokers;   // 方便的高阶适配器缓存
    private @Stable String methodDescriptor;  // toMethodDescriptorString 的缓存

    /**
     * 检查给定参数的有效性并将其存储到最终字段中。
     */
    private MethodType(Class<?> rtype, Class<?>[] ptypes, boolean trusted) {
        checkRtype(rtype);
        checkPtypes(ptypes);
        this.rtype = rtype;
        // 防御性地复制用户传递的数组
        this.ptypes = trusted ? ptypes : Arrays.copyOf(ptypes, ptypes.length);
    }

    /**
     * 构造一个临时的未经检查的 MethodType 实例，仅用作 intern 表的键。
     * 不检查给定参数的有效性，并且在用作搜索键后必须丢弃。
     * 该构造函数的参数是反转的，以防止意外使用。
     */
    private MethodType(Class<?>[] ptypes, Class<?> rtype) {
        this.rtype = rtype;
        this.ptypes = ptypes;
    }

    /*trusted*/ MethodTypeForm form() { return form; }
    /*trusted*/ Class<?> rtype() { return rtype; }
    /*trusted*/ Class<?>[] ptypes() { return ptypes; }

    void setForm(MethodTypeForm f) { form = f; }

    /** 根据 JVM 规范规定的 255，
     *  是任何 Java 方法在其参数列表中可以接收的最大 <em>插槽</em> 数量。
     *  它限制了 JVM 签名和方法类型对象。
     *  最长的可能调用看起来像
     *  {@code staticMethod(arg1, arg2, ..., arg255)} 或
     *  {@code x.virtualMethod(arg1, arg2, ..., arg254)}。
     */
    /*non-public*/ static final int MAX_JVM_ARITY = 255;  // 这是由 JVM 规范规定的。

    /** 这个数字是方法句柄的最大元数，254。
     *  它是从绝对的 JVM 强制元数中减去一得出的，
     *  这是因为方法句柄本身在用于调用方法句柄的参数列表的开头占用了一个插槽。
     *  最长的可能调用看起来像
     *  {@code mh.invoke(arg1, arg2, ..., arg254)}。
     */
    // 问题：我们应该允许 MH.invokeWithArguments 达到完整的 255 吗？
    /*non-public*/ static final int MAX_MH_ARITY = MAX_JVM_ARITY-1;  // 减去一个用于方法句柄接收者


                /** 这个数字是方法句柄调用者的最大元数，253。
     *  它是从绝对的JVM强加的元数中减去两个得到的，
     *  这两个是调用方法句柄和目标方法句柄占用的槽位，
     *  它们都在用于调用目标方法句柄的参数列表的开头。
     *  最长的可能调用看起来像
     *  {@code invokermh.invoke(targetmh, arg1, arg2, ..., arg253)}。
     */
    /*non-public*/ static final int MAX_MH_INVOKER_ARITY = MAX_MH_ARITY-1;  // 再减去一个用于调用者

    private static void checkRtype(Class<?> rtype) {
        Objects.requireNonNull(rtype);
    }
    private static void checkPtype(Class<?> ptype) {
        Objects.requireNonNull(ptype);
        if (ptype == void.class)
            throw newIllegalArgumentException("参数类型不能是void");
    }
    /** 返回额外槽位的数量（long/double参数的计数）。 */
    private static int checkPtypes(Class<?>[] ptypes) {
        int slots = 0;
        for (Class<?> ptype : ptypes) {
            checkPtype(ptype);
            if (ptype == double.class || ptype == long.class) {
                slots++;
            }
        }
        checkSlotCount(ptypes.length + slots);
        return slots;
    }
    static void checkSlotCount(int count) {
        assert((MAX_JVM_ARITY & (MAX_JVM_ARITY+1)) == 0);
        // MAX_JVM_ARITY 必须是2的幂减1，以便以下代码技巧生效：
        if ((count & MAX_JVM_ARITY) != count)
            throw newIllegalArgumentException("错误的参数计数 "+count);
    }
    private static IndexOutOfBoundsException newIndexOutOfBoundsException(Object num) {
        if (num instanceof Integer)  num = "错误的索引: "+num;
        return new IndexOutOfBoundsException(num.toString());
    }

    static final ConcurrentWeakInternSet<MethodType> internTable = new ConcurrentWeakInternSet<>();

    static final Class<?>[] NO_PTYPES = {};

    /**
     * 查找或创建给定方法类型的实例。
     * @param rtype  返回类型
     * @param ptypes 参数类型
     * @return 具有给定组件的方法类型
     * @throws NullPointerException 如果 {@code rtype} 或 {@code ptypes} 或 {@code ptypes} 的任何元素为 null
     * @throws IllegalArgumentException 如果 {@code ptypes} 的任何元素为 {@code void.class}
     */
    public static
    MethodType methodType(Class<?> rtype, Class<?>[] ptypes) {
        return makeImpl(rtype, ptypes, false);
    }

    /**
     * 查找或创建具有给定组件的方法类型。
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param rtype  返回类型
     * @param ptypes 参数类型
     * @return 具有给定组件的方法类型
     * @throws NullPointerException 如果 {@code rtype} 或 {@code ptypes} 或 {@code ptypes} 的任何元素为 null
     * @throws IllegalArgumentException 如果 {@code ptypes} 的任何元素为 {@code void.class}
     */
    public static
    MethodType methodType(Class<?> rtype, List<Class<?>> ptypes) {
        boolean notrust = false;  // 随机 List 实现可能会返回恶意的 ptypes 数组
        return makeImpl(rtype, listToArray(ptypes), notrust);
    }

    private static Class<?>[] listToArray(List<Class<?>> ptypes) {
        // 在 toArray 调用之前检查大小，因为大小可能非常大
        checkSlotCount(ptypes.size());
        return ptypes.toArray(NO_PTYPES);
    }

    /**
     * 查找或创建具有给定组件的方法类型。
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 领先的参数类型被添加到剩余数组的前面。
     * @param rtype  返回类型
     * @param ptype0 第一个参数类型
     * @param ptypes 剩余的参数类型
     * @return 具有给定组件的方法类型
     * @throws NullPointerException 如果 {@code rtype} 或 {@code ptype0} 或 {@code ptypes} 或 {@code ptypes} 的任何元素为 null
     * @throws IllegalArgumentException 如果 {@code ptype0} 或 {@code ptypes} 或 {@code ptypes} 的任何元素为 {@code void.class}
     */
    public static
    MethodType methodType(Class<?> rtype, Class<?> ptype0, Class<?>... ptypes) {
        Class<?>[] ptypes1 = new Class<?>[1+ptypes.length];
        ptypes1[0] = ptype0;
        System.arraycopy(ptypes, 0, ptypes1, 1, ptypes.length);
        return makeImpl(rtype, ptypes1, true);
    }

    /**
     * 查找或创建具有给定组件的方法类型。
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 结果方法没有参数类型。
     * @param rtype  返回类型
     * @return 具有给定返回值的方法类型
     * @throws NullPointerException 如果 {@code rtype} 为 null
     */
    public static
    MethodType methodType(Class<?> rtype) {
        return makeImpl(rtype, NO_PTYPES, true);
    }

    /**
     * 查找或创建具有给定组件的方法类型。
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 结果方法具有单个给定的参数类型。
     * @param rtype  返回类型
     * @param ptype0 参数类型
     * @return 具有给定返回值和参数类型的方法类型
     * @throws NullPointerException 如果 {@code rtype} 或 {@code ptype0} 为 null
     * @throws IllegalArgumentException 如果 {@code ptype0} 为 {@code void.class}
     */
    public static
    MethodType methodType(Class<?> rtype, Class<?> ptype0) {
        return makeImpl(rtype, new Class<?>[]{ ptype0 }, true);
    }

    /**
     * 查找或创建具有给定组件的方法类型。
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 结果方法具有与 {@code ptypes} 相同的参数类型，
     * 并具有指定的返回类型。
     * @param rtype  返回类型
     * @param ptypes 提供参数类型的方法类型
     * @return 具有给定组件的方法类型
     * @throws NullPointerException 如果 {@code rtype} 或 {@code ptypes} 为 null
     */
    public static
    MethodType methodType(Class<?> rtype, MethodType ptypes) {
        return makeImpl(rtype, ptypes.ptypes, true);
    }


                /**
     * 唯一的工厂方法，用于查找或创建一个内部化的方法类型。
     * @param rtype 期望的返回类型
     * @param ptypes 期望的参数类型
     * @param trusted 是否可以不复制 ptypes 直接使用
     * @return 具有所需结构的唯一方法类型
     */
    /*trusted*/ static
    MethodType makeImpl(Class<?> rtype, Class<?>[] ptypes, boolean trusted) {
        MethodType mt = internTable.get(new MethodType(ptypes, rtype));
        if (mt != null)
            return mt;
        if (ptypes.length == 0) {
            ptypes = NO_PTYPES; trusted = true;
        }
        mt = new MethodType(rtype, ptypes, trusted);
        // 将对象提升为实际对象，并重新探测
        mt.form = MethodTypeForm.findForm(mt);
        return internTable.add(mt);
    }
    private static final MethodType[] objectOnlyTypes = new MethodType[20];

    /**
     * 查找或创建一个其组件为 {@code Object} 的方法类型，可选地在末尾附加一个 {@code Object[]} 数组。
     * 用于 {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 所有参数和返回类型都将是 {@code Object}，
     * 除非有最终的数组参数，否则其类型为 {@code Object[]}。
     * @param objectArgCount 参数数量（不包括最终的数组参数）
     * @param finalArray 是否有尾随的数组参数，类型为 {@code Object[]}
     * @return 适用于所有给定固定参数数量和收集的进一步参数数组的调用的一般方法类型
     * @throws IllegalArgumentException 如果 {@code objectArgCount} 为负数或大于 255（如果 {@code finalArray} 为 true，则为 254）
     * @see #genericMethodType(int)
     */
    public static
    MethodType genericMethodType(int objectArgCount, boolean finalArray) {
        MethodType mt;
        checkSlotCount(objectArgCount);
        int ivarargs = (!finalArray ? 0 : 1);
        int ootIndex = objectArgCount*2 + ivarargs;
        if (ootIndex < objectOnlyTypes.length) {
            mt = objectOnlyTypes[ootIndex];
            if (mt != null)  return mt;
        }
        Class<?>[] ptypes = new Class<?>[objectArgCount + ivarargs];
        Arrays.fill(ptypes, Object.class);
        if (ivarargs != 0)  ptypes[objectArgCount] = Object[].class;
        mt = makeImpl(Object.class, ptypes, true);
        if (ootIndex < objectOnlyTypes.length) {
            objectOnlyTypes[ootIndex] = mt;     // 也缓存到这里！
        }
        return mt;
    }

    /**
     * 查找或创建一个其组件全部为 {@code Object} 的方法类型。
     * 用于 {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 所有参数和返回类型都将是 Object。
     * @param objectArgCount 参数数量
     * @return 适用于所有给定参数数量的调用的一般方法类型
     * @throws IllegalArgumentException 如果 {@code objectArgCount} 为负数或大于 255
     * @see #genericMethodType(int, boolean)
     */
    public static
    MethodType genericMethodType(int objectArgCount) {
        return genericMethodType(objectArgCount, false);
    }

    /**
     * 查找或创建一个具有单个不同参数类型的方法类型。
     * 用于 {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param num 要更改的参数类型的索引（从零开始）
     * @param nptype 用于替换旧参数类型的新参数类型
     * @return 除了选定参数被更改外，其他都相同的方法类型
     * @throws IndexOutOfBoundsException 如果 {@code num} 不是 {@code parameterArray()} 的有效索引
     * @throws IllegalArgumentException 如果 {@code nptype} 是 {@code void.class}
     * @throws NullPointerException 如果 {@code nptype} 为 null
     */
    public MethodType changeParameterType(int num, Class<?> nptype) {
        if (parameterType(num) == nptype)  return this;
        checkPtype(nptype);
        Class<?>[] nptypes = ptypes.clone();
        nptypes[num] = nptype;
        return makeImpl(rtype, nptypes, true);
    }

    /**
     * 查找或创建一个具有附加参数类型的方法类型。
     * 用于 {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param num 插入参数类型的位置（从零开始）
     * @param ptypesToInsert 要插入到参数列表中的零个或多个新参数类型
     * @return 除了选定参数被插入外，其他都相同的方法类型
     * @throws IndexOutOfBoundsException 如果 {@code num} 为负数或大于 {@code parameterCount()}
     * @throws IllegalArgumentException 如果 {@code ptypesToInsert} 的任何元素是 {@code void.class}
     *                                  或者如果结果方法类型将有超过 255 个参数槽
     * @throws NullPointerException 如果 {@code ptypesToInsert} 或其任何元素为 null
     */
    public MethodType insertParameterTypes(int num, Class<?>... ptypesToInsert) {
        int len = ptypes.length;
        if (num < 0 || num > len)
            throw newIndexOutOfBoundsException(num);
        int ins = checkPtypes(ptypesToInsert);
        checkSlotCount(parameterSlotCount() + ptypesToInsert.length + ins);
        int ilen = ptypesToInsert.length;
        if (ilen == 0)  return this;
        Class<?>[] nptypes = Arrays.copyOfRange(ptypes, 0, len+ilen);
        System.arraycopy(nptypes, num, nptypes, num+ilen, len-num);
        System.arraycopy(ptypesToInsert, 0, nptypes, num, ilen);
        return makeImpl(rtype, nptypes, true);
    }

    /**
     * 查找或创建一个具有附加参数类型的方法类型。
     * 用于 {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param ptypesToInsert 要插入到参数列表末尾的零个或多个新参数类型
     * @return 除了选定参数被追加外，其他都相同的方法类型
     * @throws IllegalArgumentException 如果 {@code ptypesToInsert} 的任何元素是 {@code void.class}
     *                                  或者如果结果方法类型将有超过 255 个参数槽
     * @throws NullPointerException 如果 {@code ptypesToInsert} 或其任何元素为 null
     */
    public MethodType appendParameterTypes(Class<?>... ptypesToInsert) {
        return insertParameterTypes(parameterCount(), ptypesToInsert);
    }

                /**
     * 查找或创建具有额外参数类型的 方法类型。
     * 用于 {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param num    要插入的参数类型的位置（从零开始）
     * @param ptypesToInsert 要插入到参数列表中的零个或多个新参数类型
     * @return 除了选中的参数被插入外，其他相同的类型
     * @throws IndexOutOfBoundsException 如果 {@code num} 为负数或大于 {@code parameterCount()}
     * @throws IllegalArgumentException 如果 {@code ptypesToInsert} 的任何元素为 {@code void.class}
     *                                  或者如果结果的方法类型将有超过 255 个参数槽
     * @throws NullPointerException 如果 {@code ptypesToInsert} 或其任何元素为 null
     */
    public MethodType insertParameterTypes(int num, List<Class<?>> ptypesToInsert) {
        return insertParameterTypes(num, listToArray(ptypesToInsert));
    }

    /**
     * 查找或创建具有额外参数类型的 方法类型。
     * 用于 {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param ptypesToInsert 要插入到参数列表末尾的零个或多个新参数类型
     * @return 除了选中的参数被追加外，其他相同的类型
     * @throws IllegalArgumentException 如果 {@code ptypesToInsert} 的任何元素为 {@code void.class}
     *                                  或者如果结果的方法类型将有超过 255 个参数槽
     * @throws NullPointerException 如果 {@code ptypesToInsert} 或其任何元素为 null
     */
    public MethodType appendParameterTypes(List<Class<?>> ptypesToInsert) {
        return insertParameterTypes(parameterCount(), ptypesToInsert);
    }

     /**
     * 查找或创建具有修改后的参数类型的 方法类型。
     * 用于 {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param start  要替换的第一个参数类型的位置（从零开始）
     * @param end    要替换的最后一个参数类型之后的位置（从零开始）
     * @param ptypesToInsert 要插入到参数列表中的零个或多个新参数类型
     * @return 除了选中的参数被替换外，其他相同的类型
     * @throws IndexOutOfBoundsException 如果 {@code start} 为负数或大于 {@code parameterCount()}
     *                                  或者如果 {@code end} 为负数或大于 {@code parameterCount()}
     *                                  或者如果 {@code start} 大于 {@code end}
     * @throws IllegalArgumentException 如果 {@code ptypesToInsert} 的任何元素为 {@code void.class}
     *                                  或者如果结果的方法类型将有超过 255 个参数槽
     * @throws NullPointerException 如果 {@code ptypesToInsert} 或其任何元素为 null
     */
    /*non-public*/ MethodType replaceParameterTypes(int start, int end, Class<?>... ptypesToInsert) {
        if (start == end)
            return insertParameterTypes(start, ptypesToInsert);
        int len = ptypes.length;
        if (!(0 <= start && start <= end && end <= len))
            throw newIndexOutOfBoundsException("start="+start+" end="+end);
        int ilen = ptypesToInsert.length;
        if (ilen == 0)
            return dropParameterTypes(start, end);
        return dropParameterTypes(start, end).insertParameterTypes(start, ptypesToInsert);
    }

    /** 用数组类型的组件类型替换最后的 arrayLength 参数类型。
     * @param arrayType 任何数组类型
     * @param arrayLength 要更改的参数类型数量
     * @return 结果类型
     */
    /*non-public*/ MethodType asSpreaderType(Class<?> arrayType, int arrayLength) {
        assert(parameterCount() >= arrayLength);
        int spreadPos = ptypes.length - arrayLength;
        if (arrayLength == 0)  return this;  // 没有要更改的
        if (arrayType == Object[].class) {
            if (isGeneric())  return this;  // 没有要更改的
            if (spreadPos == 0) {
                // 没有要保留的前导参数；转为通用
                MethodType res = genericMethodType(arrayLength);
                if (rtype != Object.class) {
                    res = res.changeReturnType(rtype);
                }
                return res;
            }
        }
        Class<?> elemType = arrayType.getComponentType();
        assert(elemType != null);
        for (int i = spreadPos; i < ptypes.length; i++) {
            if (ptypes[i] != elemType) {
                Class<?>[] fixedPtypes = ptypes.clone();
                Arrays.fill(fixedPtypes, i, ptypes.length, elemType);
                return methodType(rtype, fixedPtypes);
            }
        }
        return this;  // 参数检查通过；无需更改
    }

    /** 返回前导参数类型，该类型必须存在且为引用类型。
     *  @return 经过错误检查后的前导参数类型
     */
    /*non-public*/ Class<?> leadingReferenceParameter() {
        Class<?> ptype;
        if (ptypes.length == 0 ||
            (ptype = ptypes[0]).isPrimitive())
            throw newIllegalArgumentException("没有前导引用参数");
        return ptype;
    }

    /** 删除最后一个参数类型，并用 arrayLength 个数组类型的组件类型的副本替换它。
     * @param arrayType 任何数组类型
     * @param arrayLength 要插入的参数类型数量
     * @return 结果类型
     */
    /*non-public*/ MethodType asCollectorType(Class<?> arrayType, int arrayLength) {
        assert(parameterCount() >= 1);
        assert(lastParameterType().isAssignableFrom(arrayType));
        MethodType res;
        if (arrayType == Object[].class) {
            res = genericMethodType(arrayLength);
            if (rtype != Object.class) {
                res = res.changeReturnType(rtype);
            }
        } else {
            Class<?> elemType = arrayType.getComponentType();
            assert(elemType != null);
            res = methodType(rtype, Collections.nCopies(arrayLength, elemType));
        }
        if (ptypes.length == 1) {
            return res;
        } else {
            return res.insertParameterTypes(0, parameterList().subList(0, ptypes.length-1));
        }
    }


                /**
     * 查找或创建一个省略了某些参数类型的方法类型。
     * 用于 {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param start  要移除的第一个参数类型的索引（从零开始）
     * @param end    要保留的第一个参数类型的索引（大于 {@code start}）
     * @return 除了选中的参数被移除外，其余相同的类型
     * @throws IndexOutOfBoundsException 如果 {@code start} 为负数或大于 {@code parameterCount()}
     *                                  或者如果 {@code end} 为负数或大于 {@code parameterCount()}
     *                                  或者如果 {@code start} 大于 {@code end}
     */
    public MethodType dropParameterTypes(int start, int end) {
        int len = ptypes.length;
        if (!(0 <= start && start <= end && end <= len))
            throw newIndexOutOfBoundsException("start="+start+" end="+end);
        if (start == end)  return this;
        Class<?>[] nptypes;
        if (start == 0) {
            if (end == len) {
                // 移除所有参数
                nptypes = NO_PTYPES;
            } else {
                // 移除初始参数
                nptypes = Arrays.copyOfRange(ptypes, end, len);
            }
        } else {
            if (end == len) {
                // 移除尾部参数
                nptypes = Arrays.copyOfRange(ptypes, 0, start);
            } else {
                int tail = len - end;
                nptypes = Arrays.copyOfRange(ptypes, 0, start + tail);
                System.arraycopy(ptypes, end, nptypes, start, tail);
            }
        }
        return makeImpl(rtype, nptypes, true);
    }

    /**
     * 查找或创建一个具有不同返回类型的方法类型。
     * 用于 {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param nrtype 用于替换旧返回类型的返回参数类型
     * @return 除了返回类型更改外，其余相同的类型
     * @throws NullPointerException 如果 {@code nrtype} 为 null
     */
    public MethodType changeReturnType(Class<?> nrtype) {
        if (returnType() == nrtype)  return this;
        return makeImpl(nrtype, ptypes, true);
    }

    /**
     * 报告此类型是否包含原始参数或返回值。
     * 返回类型 {@code void} 被视为原始类型。
     * @return 如果任何类型为原始类型，则返回 true
     */
    public boolean hasPrimitives() {
        return form.hasPrimitives();
    }

    /**
     * 报告此类型是否包含包装器参数或返回值。
     * 包装器是用于包装原始值的类型，例如 {@link Integer}。
     * 如果返回类型为 {@code java.lang.Void}，则被视为包装器。
     * @return 如果任何类型为包装器，则返回 true
     */
    public boolean hasWrappers() {
        return unwrap() != this;
    }

    /**
     * 将所有引用类型擦除为 {@code Object}。
     * 用于 {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 所有原始类型（包括 {@code void}）将保持不变。
     * @return 一个版本的原始类型，其中所有引用类型被替换
     */
    public MethodType erase() {
        return form.erasedType();
    }

    /**
     * 将所有引用类型擦除为 {@code Object}，并将所有子字类型擦除为 {@code int}。
     * 这是由私有方法（如 {@link MethodHandle#invokeBasic invokeBasic}）使用的减少类型多态性。
     * @return 一个版本的原始类型，其中所有引用和子字类型被替换
     */
    /*non-public*/ MethodType basicType() {
        return form.basicType();
    }

    /**
     * 返回一个版本的原始类型，其中 MethodHandle 被添加为第一个参数
     */
    /*non-public*/ MethodType invokerType() {
        return insertParameterTypes(0, MethodHandle.class);
    }

    /**
     * 将所有类型（包括引用和原始类型）转换为 {@code Object}。
     * 用于 {@link #genericMethodType(int) genericMethodType} 的便捷方法。
     * 表达式 {@code type.wrap().erase()} 产生的值与 {@code type.generic()} 相同。
     * @return 一个版本的原始类型，其中所有类型被替换
     */
    public MethodType generic() {
        return genericMethodType(parameterCount());
    }

    /*non-public*/ boolean isGeneric() {
        return this == erase() && !hasPrimitives();
    }

    /**
     * 将所有原始类型转换为相应的包装类型。
     * 用于 {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 所有引用类型（包括包装类型）将保持不变。
     * 返回类型 {@code void} 被更改为类型 {@code java.lang.Void}。
     * 表达式 {@code type.wrap().erase()} 产生的值与 {@code type.generic()} 相同。
     * @return 一个版本的原始类型，其中所有原始类型被替换
     */
    public MethodType wrap() {
        return hasPrimitives() ? wrapWithPrims(this) : this;
    }

    /**
     * 将所有包装类型转换为相应的原始类型。
     * 用于 {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 所有原始类型（包括 {@code void}）将保持不变。
     * 返回类型 {@code java.lang.Void} 被更改为 {@code void}。
     * @return 一个版本的原始类型，其中所有包装类型被替换
     */
    public MethodType unwrap() {
        MethodType noprims = !hasPrimitives() ? this : wrapWithPrims(this);
        return unwrapWithNoPrims(noprims);
    }

    private static MethodType wrapWithPrims(MethodType pt) {
        assert(pt.hasPrimitives());
        MethodType wt = (MethodType)pt.wrapAlt;
        if (wt == null) {
            // 惰性填充
            wt = MethodTypeForm.canonicalize(pt, MethodTypeForm.WRAP, MethodTypeForm.WRAP);
            assert(wt != null);
            pt.wrapAlt = wt;
        }
        return wt;
    }


                private static MethodType unwrapWithNoPrims(MethodType wt) {
        assert(!wt.hasPrimitives());
        MethodType uwt = (MethodType)wt.wrapAlt;
        if (uwt == null) {
            // 懒惰填充
            uwt = MethodTypeForm.canonicalize(wt, MethodTypeForm.UNWRAP, MethodTypeForm.UNWRAP);
            if (uwt == null)
                uwt = wt;    // 类型没有任何包装或原始类型
            wt.wrapAlt = uwt;
        }
        return uwt;
    }

    /**
     * 返回此方法类型中指定索引处的参数类型。
     * @param num 所需参数类型的索引（从零开始）
     * @return 选定的参数类型
     * @throws IndexOutOfBoundsException 如果 {@code num} 不是 {@code parameterArray()} 的有效索引
     */
    public Class<?> parameterType(int num) {
        return ptypes[num];
    }
    /**
     * 返回此方法类型的参数类型数量。
     * @return 参数类型的数量
     */
    public int parameterCount() {
        return ptypes.length;
    }
    /**
     * 返回此方法类型的返回类型。
     * @return 返回类型
     */
    public Class<?> returnType() {
        return rtype;
    }

    /**
     * 以列表形式呈现参数类型（方便方法）。
     * 列表将是不可变的。
     * @return 参数类型（作为不可变列表）
     */
    public List<Class<?>> parameterList() {
        return Collections.unmodifiableList(Arrays.asList(ptypes.clone()));
    }

    /*non-public*/ Class<?> lastParameterType() {
        int len = ptypes.length;
        return len == 0 ? void.class : ptypes[len-1];
    }

    /**
     * 以数组形式呈现参数类型（方便方法）。
     * 对数组的更改不会导致类型的变化。
     * @return 参数类型（必要时作为新副本）
     */
    public Class<?>[] parameterArray() {
        return ptypes.clone();
    }

    /**
     * 将指定对象与此类型进行相等性比较。
     * 即，当且仅当指定对象也是具有完全相同参数和返回类型的方法类型时，返回 <tt>true</tt>。
     * @param x 要比较的对象
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object x) {
        return this == x || x instanceof MethodType && equals((MethodType)x);
    }

    private boolean equals(MethodType that) {
        return this.rtype == that.rtype
            && Arrays.equals(this.ptypes, that.ptypes);
    }

    /**
     * 返回此方法类型的哈希码值。
     * 定义为与列表的哈希码相同，列表的元素是返回类型后跟参数类型。
     * @return 此方法类型的哈希码值
     * @see Object#hashCode()
     * @see #equals(Object)
     * @see List#hashCode()
     */
    @Override
    public int hashCode() {
      int hashCode = 31 + rtype.hashCode();
      for (Class<?> ptype : ptypes)
          hashCode = 31*hashCode + ptype.hashCode();
      return hashCode;
    }

    /**
     * 返回方法类型的形式化字符串表示，形式为 {@code "(PT0,PT1...)RT"}。
     * 方法类型的形式化字符串表示是一个括号包围的、逗号分隔的类型名称列表，后跟返回类型。
     * <p>
     * 每个类型由其 {@link java.lang.Class#getSimpleName 简单名称} 表示。
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < ptypes.length; i++) {
            if (i > 0)  sb.append(",");
            sb.append(ptypes[i].getSimpleName());
        }
        sb.append(")");
        sb.append(rtype.getSimpleName());
        return sb.toString();
    }

    /** 如果旧的返回类型可以总是被视为（无需强制转换）新的返回类型，
     *  并且新的参数可以被视为（无需强制转换）旧的参数类型，则为真。
     */
    /*non-public*/
    boolean isViewableAs(MethodType newType, boolean keepInterfaces) {
        if (!VerifyType.isNullConversion(returnType(), newType.returnType(), keepInterfaces))
            return false;
        return parametersAreViewableAs(newType, keepInterfaces);
    }
    /** 如果新的参数可以被视为（无需强制转换）旧的参数类型，则为真。 */
    /*non-public*/
    boolean parametersAreViewableAs(MethodType newType, boolean keepInterfaces) {
        if (form == newType.form && form.erasedType == this)
            return true;  // 我的引用参数都是 Object
        if (ptypes == newType.ptypes)
            return true;
        int argc = parameterCount();
        if (argc != newType.parameterCount())
            return false;
        for (int i = 0; i < argc; i++) {
            if (!VerifyType.isNullConversion(newType.parameterType(i), parameterType(i), keepInterfaces))
                return false;
        }
        return true;
    }
    /*non-public*/
    boolean isConvertibleTo(MethodType newType) {
        MethodTypeForm oldForm = this.form();
        MethodTypeForm newForm = newType.form();
        if (oldForm == newForm)
            // 相同的参数数量，相同的原始/对象混合
            return true;
        if (!canConvert(returnType(), newType.returnType()))
            return false;
        Class<?>[] srcTypes = newType.ptypes;
        Class<?>[] dstTypes = ptypes;
        if (srcTypes == dstTypes)
            return true;
        int argc;
        if ((argc = srcTypes.length) != dstTypes.length)
            return false;
        if (argc <= 1) {
            if (argc == 1 && !canConvert(srcTypes[0], dstTypes[0]))
                return false;
            return true;
        }
        if ((oldForm.primitiveParameterCount() == 0 && oldForm.erasedType == this) ||
            (newForm.primitiveParameterCount() == 0 && newForm.erasedType == newType)) {
            // 为了避免 2 次或更多次的循环，进行了一些复杂的测试。
            // 如果任意类型只有 Object 参数，我们知道可以转换。
            assert(canConvertParameters(srcTypes, dstTypes));
            return true;
        }
        return canConvertParameters(srcTypes, dstTypes);
    }


                /** 返回 true，如果 MHs.explicitCastArguments 产生的结果与 MH.asType 相同。
     *  如果任一方法的类型转换是不可能的，结果应为 false。
     */
    /*non-public*/
    boolean explicitCastEquivalentToAsType(MethodType newType) {
        if (this == newType)  return true;
        if (!explicitCastEquivalentToAsType(rtype, newType.rtype)) {
            return false;
        }
        Class<?>[] srcTypes = newType.ptypes;
        Class<?>[] dstTypes = ptypes;
        if (dstTypes == srcTypes) {
            return true;
        }
        assert(dstTypes.length == srcTypes.length);
        for (int i = 0; i < dstTypes.length; i++) {
            if (!explicitCastEquivalentToAsType(srcTypes[i], dstTypes[i])) {
                return false;
            }
        }
        return true;
    }

    /** 如果 src 可以通过 asType 和 MHs.eCE 转换为 dst，并且效果相同，则返回 true。
     *  MHs.eCA 对 MH.asType 有以下“升级”：
     *  1. 接口是未经检查的（即，被视为 Object 的别名）
     *     因此，{@code Object->CharSequence} 在两种情况下都是可能的，但语义不同
     *  2. 支持完整的原始类型到原始类型的转换矩阵
     *     窄化如 {@code long->byte} 和基本类型转换如 {@code boolean->int}
     *     不被 asType 支持，但 asType 支持的任何内容都与 MHs.eCE 等效
     *  3a. 解装转换可以跟随完整的原始类型转换矩阵
     *  3b. 解装 null 是允许的（生成一个零原始值）
     *  除了接口，引用到引用的转换是相同的。
     *  将原始类型装箱为引用对于两个操作符是相同的。
     */
    private static boolean explicitCastEquivalentToAsType(Class<?> src, Class<?> dst) {
        if (src == dst || dst == Object.class || dst == void.class)  return true;
        if (src.isPrimitive()) {
            // 可能是原始类型到原始类型的转换，其中转换是一个严格的超集。
            // 或者是一个装箱转换，总是转换为一个精确的包装类。
            return canConvert(src, dst);
        } else if (dst.isPrimitive()) {
            // 解装行为在 MHs.eCA 与 MH.asType 之间不同（见 3b）。
            return false;
        } else {
            // R->R 总是可行的，但我们必须避免对接口进行检查转换。
            return !dst.isInterface() || dst.isAssignableFrom(src);
        }
    }

    private boolean canConvertParameters(Class<?>[] srcTypes, Class<?>[] dstTypes) {
        for (int i = 0; i < srcTypes.length; i++) {
            if (!canConvert(srcTypes[i], dstTypes[i])) {
                return false;
            }
        }
        return true;
    }

    /*non-public*/
    static boolean canConvert(Class<?> src, Class<?> dst) {
        // 快速处理一些情况：
        if (src == dst || src == Object.class || dst == Object.class)  return true;
        // 本逻辑的其余部分在 MethodHandle.asType 中有文档说明
        if (src.isPrimitive()) {
            // 可以强制将 void 转换为显式的 null，类似于 reflect.Method.invoke
            // 也可以通过类比将 void 强制转换为原始零值
            if (src == void.class)  return true;  //or !dst.isPrimitive()?
            Wrapper sw = Wrapper.forPrimitiveType(src);
            if (dst.isPrimitive()) {
                // P->P 必须扩大
                return Wrapper.forPrimitiveType(dst).isConvertibleFrom(sw);
            } else {
                // P->R 必须装箱并扩大
                return dst.isAssignableFrom(sw.wrapperType());
            }
        } else if (dst.isPrimitive()) {
            // 任何值都可以被丢弃
            if (dst == void.class)  return true;
            Wrapper dw = Wrapper.forPrimitiveType(dst);
            // R->P 必须能够解箱（从动态选择的类型）并扩大
            // 例如：
            //   Byte/Number/Comparable/Object -> dw:Byte -> byte.
            //   Character/Comparable/Object -> dw:Character -> char
            //   Boolean/Comparable/Object -> dw:Boolean -> boolean
            // 这意味着 dw 必须与 src 兼容。
            if (src.isAssignableFrom(dw.wrapperType())) {
                return true;
            }
            // 如果源引用强烈类型化为一个需要扩大的包装器的原始类型，则上述情况不适用。
            // 例如：
            //   Byte -> unbox:byte -> short/int/long/float/double
            //   Character -> unbox:char -> int/long/float/double
            if (Wrapper.isWrapperType(src) &&
                dw.isConvertibleFrom(Wrapper.forWrapperType(src))) {
                // 可以从 src 解箱，然后扩大到 dst
                return true;
            }
            // 我们已经涵盖了由于运行时解箱
            // 一个覆盖多个包装器类型的引用类型而引起的情况：
            //   Object -> cast:Integer -> unbox:int -> long/float/double
            //   Serializable -> cast:Byte -> unbox:byte -> byte/short/int/long/float/double
            // 一个边缘情况是 Number -> dw:Character -> char，如果有一个
            // 包装一个可以转换为 char 的值的 Number 子类，这是可以的。
            // 由于没有这样的子类，我们不需要在这里进行额外的检查来覆盖 char 或 boolean。
            return false;
        } else {
            // R->R 总是可行的，因为 null 在动态上总是有效的
            return true;
        }
    }

    /// 与字节码架构相关的查询

    /** 报告调用此类型的方法所需的 JVM 栈槽的数量。注意（由于历史原因），JVM 需要
     * 一个额外的栈槽来传递 long 和 double 参数。
     * 因此，此方法返回 {@link #parameterCount() parameterCount} 加上
     * long 和 double 参数的数量（如果有）。
     * <p>
     * 本方法包括在内是为了应用程序的需要，这些应用程序必须
     * 生成处理方法句柄和 invokedynamic 的字节码。
     * @return 此类型参数的 JVM 栈槽数量
     */
    /*non-public*/ int parameterSlotCount() {
        return form.parameterSlotCount();
    }


                /*non-public*/ Invokers invokers() {
        Invokers inv = invokers;
        if (inv != null)  return inv;
        invokers = inv = new Invokers(this);
        return inv;
    }

    /** 报告从给定位置（包括该位置）开始的所有参数占用的JVM堆栈槽的数量，该位置必须在0到
     * {@code parameterCount}（包括）的范围内。连续的参数在堆栈中更浅地堆叠，
     * 参数在字节码中根据它们的尾部边缘进行索引。因此，要获取参数 {@code N} 在出站调用堆栈中的深度，
     * 获取其尾部边缘位置 {@code N+1} 的 {@code parameterSlotDepth}。
     * <p>
     * 类型为 {@code long} 和 {@code double} 的参数（由于历史原因）占用两个堆栈槽，而其他所有参数占用一个。
     * 因此，返回的数量是从给定参数（包括）开始的参数数量，
     * <em>加上</em> 给定参数之后的 long 或 double 参数的数量。
     * <p>
     * 该方法是为了那些必须生成处理方法句柄和 invokedynamic 的字节码的应用程序而提供的。
     * @param num 参数类型内的索引（基于零，包括）
     * @return 传输给定参数的（最浅的）JVM堆栈槽的索引
     * @throws IllegalArgumentException 如果 {@code num} 为负数或大于 {@code parameterCount()}
     */
    /*non-public*/ int parameterSlotDepth(int num) {
        if (num < 0 || num > ptypes.length)
            parameterType(num);  // 强制范围检查
        return form.parameterToArgSlot(num-1);
    }

    /** 报告接收此类型方法返回值所需的JVM堆栈槽的数量。
     * 如果 {@link #returnType() 返回类型} 是 void，则为零，
     * 否则如果返回类型是 long 或 double，则为两个，否则为一个。
     * <p>
     * 该方法是为了那些必须生成处理方法句柄和 invokedynamic 的字节码的应用程序而提供的。
     * @return 此类型返回值的JVM堆栈槽数量（0, 1, 或 2）
     * Will be removed for PFD.
     */
    /*non-public*/ int returnSlotCount() {
        return form.returnSlotCount();
    }

    /**
     * 根据其字节码描述符的拼写查找或创建一个方法类型实例。
     * 用于 {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 描述符字符串中嵌入的任何类或接口名称
     * 将通过调用 {@link ClassLoader#loadClass(java.lang.String)}
     * 在给定的加载器（或如果为 null，则在系统类加载器）中解析。
     * <p>
     * 请注意，可能会遇到无法通过此方法构造的方法类型，
     * 因为它们的组件类型不是都可以从一个公共类加载器访问的。
     * <p>
     * 该方法是为了那些必须生成处理方法句柄和 {@code invokedynamic} 的字节码的应用程序而提供的。
     * @param descriptor 字节码级别的类型描述符字符串 "(T...)T"
     * @param loader 用于查找类型的类加载器
     * @return 匹配字节码级别类型描述符的方法类型
     * @throws NullPointerException 如果字符串为 null
     * @throws IllegalArgumentException 如果字符串格式不正确
     * @throws TypeNotPresentException 如果命名的类型找不到
     */
    public static MethodType fromMethodDescriptorString(String descriptor, ClassLoader loader)
        throws IllegalArgumentException, TypeNotPresentException
    {
        if (!descriptor.startsWith("(") ||  // 也会在需要时生成 NPE
            descriptor.indexOf(')') < 0 ||
            descriptor.indexOf('.') >= 0)
            throw newIllegalArgumentException("not a method descriptor: "+descriptor);
        List<Class<?>> types = BytecodeDescriptor.parseMethod(descriptor, loader);
        Class<?> rtype = types.remove(types.size() - 1);
        checkSlotCount(types.size());
        Class<?>[] ptypes = listToArray(types);
        return makeImpl(rtype, ptypes, true);
    }

    /**
     * 生成方法类型在字节码描述符中的表示。
     * <p>
     * 请注意，这不是 {@link #fromMethodDescriptorString fromMethodDescriptorString} 的严格逆操作。
     * 两个共享公共名称但具有不同类加载器的类
     * 在描述符字符串中看起来是相同的。
     * <p>
     * 该方法是为了那些必须生成处理方法句柄和 {@code invokedynamic} 的字节码的应用程序而提供的。
     * {@link #fromMethodDescriptorString(java.lang.String, java.lang.ClassLoader) fromMethodDescriptorString}，
     * 因为后者需要一个合适的类加载器参数。
     * @return 字节码类型描述符表示
     */
    public String toMethodDescriptorString() {
        String desc = methodDescriptor;
        if (desc == null) {
            desc = BytecodeDescriptor.unparse(this);
            methodDescriptor = desc;
        }
        return desc;
    }

    /*non-public*/ static String toFieldDescriptorString(Class<?> cls) {
        return BytecodeDescriptor.unparse(cls);
    }

    /// 序列化。

    /**
     * {@code MethodType} 没有可序列化的字段。
     */
    private static final java.io.ObjectStreamField[] serialPersistentFields = { };

    /**
     * 将 {@code MethodType} 实例写入流。
     *
     * @serialData
     * 为了可移植性，序列化格式不引用命名字段。
     * 相反，返回类型和参数类型数组直接从 {@code writeObject} 方法写入，
     * 使用两次调用 {@code s.writeObject} 如下：
     * <blockquote><pre>{@code
s.writeObject(this.returnType());
s.writeObject(this.parameterArray());
     * }</pre></blockquote>
     * <p>
     * 反序列化后的字段值将像提供给工厂方法 {@link #methodType(Class,Class[]) methodType} 一样进行检查。
     * 例如，null 值或 {@code void} 参数类型，
     * 将在反序列化期间导致异常。
     * @param s 要写入对象的流
     * @throws java.io.IOException 如果写入对象时出现问题
     */
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();  // 需要 serialPersistentFields 是一个空数组
        s.writeObject(returnType());
        s.writeObject(parameterArray());
    }


                /**
     * 从流中重建 {@code MethodType} 实例（即，反序列化它）。
     * 该实例是一个带有虚假最终字段的临时对象。
     * 它为 {@link #readResolve readResolve} 调用的工厂方法提供参数。
     * 调用之后，它将被丢弃。
     * @param s 读取对象的流
     * @throws java.io.IOException 如果读取对象时出现问题
     * @throws ClassNotFoundException 如果无法解析组件类之一
     * @see #readResolve
     * @see #writeObject
     */
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        // 如果此对象泄露，分配默认值
        UNSAFE.putObject(this, rtypeOffset, void.class);
        UNSAFE.putObject(this, ptypesOffset, NO_PTYPES);

        s.defaultReadObject();  // 需要 serialPersistentFields 是一个空数组

        Class<?>   returnType     = (Class<?>)   s.readObject();
        Class<?>[] parameterArray = (Class<?>[]) s.readObject();
        // 验证所有操作数，并确保 ptypes 是未共享的
        // 为 readResolve 缓存新的 MethodType
        wrapAlt = new MethodType[]{MethodType.methodType(returnType, parameterArray)};
    }

    // 支持在反序列化时重置最终字段
    private static final long rtypeOffset, ptypesOffset;
    static {
        try {
            rtypeOffset = UNSAFE.objectFieldOffset
                (MethodType.class.getDeclaredField("rtype"));
            ptypesOffset = UNSAFE.objectFieldOffset
                (MethodType.class.getDeclaredField("ptypes"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    /**
     * 在序列化后解析并初始化一个 {@code MethodType} 对象。
     * @return 完全初始化的 {@code MethodType} 对象
     */
    private Object readResolve() {
        // 反序列化时不要使用可信路径：
        //    return makeImpl(rtype, ptypes, true);
        // 验证所有操作数，并确保 ptypes 是未共享的：
        // 返回一个从 readObject 传递的 rtype 和 ptypes 验证后的新 MethodType。
        MethodType mt = ((MethodType[])wrapAlt)[0];
        wrapAlt = null;
        return mt;
    }

    /**
     * 简单的弱并发实习集实现。
     *
     * @param <T> 实习类型
     */
    private static class ConcurrentWeakInternSet<T> {

        private final ConcurrentMap<WeakEntry<T>, WeakEntry<T>> map;
        private final ReferenceQueue<T> stale;

        public ConcurrentWeakInternSet() {
            this.map = new ConcurrentHashMap<>();
            this.stale = new ReferenceQueue<>();
        }

        /**
         * 获取现有的实习元素。
         * 如果没有元素被实习，此方法返回 null。
         *
         * @param elem 要查找的元素
         * @return 实习的元素
         */
        public T get(T elem) {
            if (elem == null) throw new NullPointerException();
            expungeStaleElements();

            WeakEntry<T> value = map.get(new WeakEntry<>(elem));
            if (value != null) {
                T res = value.get();
                if (res != null) {
                    return res;
                }
            }
            return null;
        }

        /**
         * 实习元素。
         * 始终返回非空元素，匹配实习集中的元素。
         * 在与另一个 add() 的竞争中，如果另一个线程抢先实习了它，它可以返回 <i>不同的</i> 元素。
         *
         * @param elem 要添加的元素
         * @return 实际添加的元素
         */
        public T add(T elem) {
            if (elem == null) throw new NullPointerException();

            // 在这里进行双重竞争，因此需要自旋循环。
            // 第一个竞争是两个并发更新者之间的竞争。
            // 第二个竞争是 GC 在我们脚下清除弱引用。
            // 希望我们几乎总是以单次通过结束。
            T interned;
            WeakEntry<T> e = new WeakEntry<>(elem, stale);
            do {
                expungeStaleElements();
                WeakEntry<T> exist = map.putIfAbsent(e, e);
                interned = (exist == null) ? elem : exist.get();
            } while (interned == null);
            return interned;
        }

        private void expungeStaleElements() {
            Reference<? extends T> reference;
            while ((reference = stale.poll()) != null) {
                map.remove(reference);
            }
        }

        private static class WeakEntry<T> extends WeakReference<T> {

            public final int hashcode;

            public WeakEntry(T key, ReferenceQueue<T> queue) {
                super(key, queue);
                hashcode = key.hashCode();
            }

            public WeakEntry(T key) {
                super(key);
                hashcode = key.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof WeakEntry) {
                    Object that = ((WeakEntry) obj).get();
                    Object mine = get();
                    return (that == null || mine == null) ? (this == obj) : mine.equals(that);
                }
                return false;
            }

            @Override
            public int hashCode() {
                return hashcode;
            }

        }
    }

}
