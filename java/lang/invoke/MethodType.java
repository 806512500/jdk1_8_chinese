
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
 * 方法类型表示方法句柄接受和返回的参数和返回类型，或者方法句柄调用者传递和期望的参数和返回类型。方法类型必须在方法句柄和所有调用者之间正确匹配，
 * 并且JVM的操作在调用 {@link MethodHandle#invokeExact MethodHandle.invokeExact} 和 {@link MethodHandle#invoke MethodHandle.invoke} 时，
 * 以及在执行 {@code invokedynamic} 指令时强制执行这种匹配。
 * <p>
 * 该结构由返回类型和任意数量的参数类型组成。类型（基本类型、{@code void} 和引用类型）由 {@link Class} 对象表示。
 * （为了便于说明，我们把 {@code void} 当作一种类型来处理。实际上，它表示没有返回类型。）
 * <p>
 * 所有 {@code MethodType} 的实例都是不可变的。如果两个实例相等，则它们可以完全互换。相等取决于返回类型和参数类型的逐对对应，除此之外没有其他因素。
 * <p>
 * 该类型只能通过工厂方法创建。所有工厂方法都可能缓存值，但不保证缓存。一些工厂方法是静态的，而另一些则是虚拟方法，它们修改前导方法类型，
 * 例如通过更改选定的参数。
 * <p>
 * 操作参数类型组的工厂方法系统地提供两个版本，以便可以使用 Java 数组和 Java 列表来处理参数类型组。
 * 查询方法 {@code parameterArray} 和 {@code parameterList} 也提供了数组和列表之间的选择。
 * <p>
 * 有时 {@code MethodType} 对象是从字节码指令（如 {@code invokedynamic}）派生的，特别是从类文件常量池中与指令关联的类型描述符字符串派生的。
 * <p>
 * 像类和字符串一样，方法类型也可以直接在类文件的常量池中作为常量表示。
 * 可以通过引用合适的 {@code CONSTANT_MethodType} 常量池条目的 {@code ldc} 指令加载方法类型。
 * 该条目引用 {@code CONSTANT_Utf8} 类型描述符字符串的拼写。
 * （有关方法类型常量的详细信息，请参阅 Java 虚拟机规范的 4.4.8 和 5.4.3.5 节。）
 * <p>
 * 当 JVM 从描述符字符串中具体化一个 {@code MethodType} 时，描述符中命名的所有类都必须是可访问的，并且将被加载。
 * （但这些类不必初始化，就像 {@code CONSTANT_Class} 一样。）这种加载可能在 {@code MethodType} 对象首次派生之前的任何时候发生。
 * @author John Rose, JSR 292 EG
 */
public final
class MethodType implements java.io.Serializable {
    private static final long serialVersionUID = 292L;  // {rtype, {ptype...}}

    // rtype 和 ptypes 字段定义了方法类型的身份：
    private final Class<?>   rtype;
    private final Class<?>[] ptypes;

    // 剩余的字段是各种缓存：
    private @Stable MethodTypeForm form; // 擦除形式，加上关于基本类型的缓存数据
    private @Stable Object wrapAlt;  // 替代的包装/解包版本和
                                     // 用于 readObject 和 readResolve 的私有通信
    private @Stable Invokers invokers;   // 方便的高阶适配器缓存
    private @Stable String methodDescriptor;  // toMethodDescriptorString 的缓存

    /**
     * 检查给定的参数是否有效，并将它们存储到最终字段中。
     */
    private MethodType(Class<?> rtype, Class<?>[] ptypes, boolean trusted) {
        checkRtype(rtype);
        checkPtypes(ptypes);
        this.rtype = rtype;
        // 防御性地复制用户传递的数组
        this.ptypes = trusted ? ptypes : Arrays.copyOf(ptypes, ptypes.length);
    }

    /**
     * 构建一个临时的未检查的 MethodType 实例，仅作为 intern 表的键使用。
     * 不检查给定的参数是否有效，并且在用作搜索键后必须丢弃。
     * 该构造函数的参数是反向的，以防止意外使用。
     */
    private MethodType(Class<?>[] ptypes, Class<?> rtype) {
        this.rtype = rtype;
        this.ptypes = ptypes;
    }

    /*trusted*/ MethodTypeForm form() { return form; }
    /*trusted*/ Class<?> rtype() { return rtype; }
    /*trusted*/ Class<?>[] ptypes() { return ptypes; }

    void setForm(MethodTypeForm f) { form = f; }

    /** 根据 JVM 规范规定的 255，这是任何 Java 方法在其参数列表中可以接收的最大 <em>槽位</em> 数量。
     *  它限制了 JVM 签名和方法类型对象。最长的可能调用将类似于
     *  {@code staticMethod(arg1, arg2, ..., arg255)} 或
     *  {@code x.virtualMethod(arg1, arg2, ..., arg254)}。
     */
    /*non-public*/ static final int MAX_JVM_ARITY = 255;  // 这是由 JVM 规范规定的。

    /** 这是方法句柄的最大参数数量，254。
     *  它是从绝对的 JVM 强制参数数量中减去一得到的，减去的是方法句柄本身在调用方法句柄时的参数列表开头占用的槽位。
     *  最长的可能调用将类似于
     *  {@code mh.invoke(arg1, arg2, ..., arg254)}。
     */
    // 问题：我们应该允许 MH.invokeWithArguments 达到 255 吗？
    /*non-public*/ static final int MAX_MH_ARITY = MAX_JVM_ARITY-1;  // 为方法句柄接收者减去一个槽位

    /** 这是方法句柄调用者的最大参数数量，253。
     *  它是从绝对的 JVM 强制参数数量中减去二得到的，减去的是调用方法句柄和目标方法句柄在调用目标方法句柄时的参数列表开头占用的两个槽位。
     *  最长的可能调用将类似于
     *  {@code invokermh.invoke(targetmh, arg1, arg2, ..., arg253)}。
     */
    /*non-public*/ static final int MAX_MH_INVOKER_ARITY = MAX_MH_ARITY-1;  // 为调用者再减去一个槽位

    private static void checkRtype(Class<?> rtype) {
        Objects.requireNonNull(rtype);
    }
    private static void checkPtype(Class<?> ptype) {
        Objects.requireNonNull(ptype);
        if (ptype == void.class)
            throw newIllegalArgumentException("参数类型不能为 void");
    }
    /** 返回额外槽位的数量（long/double 参数的数量）。 */
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
        // MAX_JVM_ARITY 必须是 2 的幂减 1，以便以下代码技巧生效：
        if ((count & MAX_JVM_ARITY) != count)
            throw newIllegalArgumentException("错误的参数数量 " + count);
    }
    private static IndexOutOfBoundsException newIndexOutOfBoundsException(Object num) {
        if (num instanceof Integer)  num = "错误的索引: " + num;
        return new IndexOutOfBoundsException(num.toString());
    }

    static final ConcurrentWeakInternSet<MethodType> internTable = new ConcurrentWeakInternSet<>();

    static final Class<?>[] NO_PTYPES = {};

    /**
     * 查找或创建给定方法类型的一个实例。
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
        // 在调用 toArray 之前检查大小，因为大小可能非常大
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
     * 结果方法具有给定的单个参数类型。
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
     * 结果方法具有与 {@code ptypes} 相同的参数类型，以及指定的返回类型。
     * @param rtype  返回类型
     * @param ptypes 提供参数类型的类型
     * @return 具有给定组件的方法类型
     * @throws NullPointerException 如果 {@code rtype} 或 {@code ptypes} 为 null
     */
    public static
    MethodType methodType(Class<?> rtype, MethodType ptypes) {
        return makeImpl(rtype, ptypes.ptypes, true);
    }

    /**
     * 唯一的工厂方法，用于查找或创建一个 interned 的方法类型。
     * @param rtype 期望的返回类型
     * @param ptypes 期望的参数类型
     * @param trusted 是否可以不克隆 ptypes 就使用
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
        // 将对象提升为真正的对象，并重新探测
        mt.form = MethodTypeForm.findForm(mt);
        return internTable.add(mt);
    }
    private static final MethodType[] objectOnlyTypes = new MethodType[20];

    /**
     * 查找或创建一个组件为 {@code Object} 的方法类型，可选地包含一个尾随的 {@code Object[]} 数组。
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 所有参数和返回类型都将是 {@code Object}，除了最后一个数组参数（如果有），它将是 {@code Object[]}。
     * @param objectArgCount 参数数量（不包括最后一个数组参数，如果有）
     * @param finalArray 是否有尾随的数组参数，类型为 {@code Object[]}
     * @return 适用于所有具有给定固定参数数量和收集的进一步参数数组的调用的一般方法类型
     * @throws IllegalArgumentException 如果 {@code objectArgCount} 为负数或大于 255（如果 {@code finalArray} 为 true，则大于 254）
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
            objectOnlyTypes[ootIndex] = mt;     // 也缓存在这里！
        }
        return mt;
    }


                /**
     * 查找或创建一个所有组件均为 {@code Object} 的方法类型。
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 所有参数和返回类型都将是 Object。
     * @param objectArgCount 参数数量
     * @return 适用于所有给定参数数量调用的一般方法类型
     * @throws IllegalArgumentException 如果 {@code objectArgCount} 为负数或大于 255
     * @see #genericMethodType(int, boolean)
     */
    public static
    MethodType genericMethodType(int objectArgCount) {
        return genericMethodType(objectArgCount, false);
    }

    /**
     * 查找或创建一个具有不同参数类型的方法类型。
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param num    要更改的参数类型的索引（从零开始）
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
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param num    要插入的参数类型的索引（从零开始）
     * @param ptypesToInsert 要插入到参数列表中的一个或多个新参数类型
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
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param ptypesToInsert 要插入到参数列表末尾的一个或多个新参数类型
     * @return 除了选定参数被追加外，其他都相同的方法类型
     * @throws IllegalArgumentException 如果 {@code ptypesToInsert} 的任何元素是 {@code void.class}
     *                                  或者如果结果方法类型将有超过 255 个参数槽
     * @throws NullPointerException 如果 {@code ptypesToInsert} 或其任何元素为 null
     */
    public MethodType appendParameterTypes(Class<?>... ptypesToInsert) {
        return insertParameterTypes(parameterCount(), ptypesToInsert);
    }

    /**
     * 查找或创建一个具有附加参数类型的方法类型。
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param num    要插入的参数类型的索引（从零开始）
     * @param ptypesToInsert 要插入到参数列表中的一个或多个新参数类型
     * @return 除了选定参数被插入外，其他都相同的方法类型
     * @throws IndexOutOfBoundsException 如果 {@code num} 为负数或大于 {@code parameterCount()}
     * @throws IllegalArgumentException 如果 {@code ptypesToInsert} 的任何元素是 {@code void.class}
     *                                  或者如果结果方法类型将有超过 255 个参数槽
     * @throws NullPointerException 如果 {@code ptypesToInsert} 或其任何元素为 null
     */
    public MethodType insertParameterTypes(int num, List<Class<?>> ptypesToInsert) {
        return insertParameterTypes(num, listToArray(ptypesToInsert));
    }

    /**
     * 查找或创建一个具有附加参数类型的方法类型。
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param ptypesToInsert 要插入到参数列表末尾的一个或多个新参数类型
     * @return 除了选定参数被追加外，其他都相同的方法类型
     * @throws IllegalArgumentException 如果 {@code ptypesToInsert} 的任何元素是 {@code void.class}
     *                                  或者如果结果方法类型将有超过 255 个参数槽
     * @throws NullPointerException 如果 {@code ptypesToInsert} 或其任何元素为 null
     */
    public MethodType appendParameterTypes(List<Class<?>> ptypesToInsert) {
        return insertParameterTypes(parameterCount(), ptypesToInsert);
    }

     /**
     * 查找或创建一个具有修改参数类型的方法类型。
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param start  要替换的第一个参数类型的索引（从零开始）
     * @param end    要替换的最后一个参数类型之后的索引（从零开始）
     * @param ptypesToInsert 要插入到参数列表中的一个或多个新参数类型
     * @return 除了选定参数被替换外，其他都相同的方法类型
     * @throws IndexOutOfBoundsException 如果 {@code start} 为负数或大于 {@code parameterCount()}
     *                                  或者如果 {@code end} 为负数或大于 {@code parameterCount()}
     *                                  或者如果 {@code start} 大于 {@code end}
     * @throws IllegalArgumentException 如果 {@code ptypesToInsert} 的任何元素是 {@code void.class}
     *                                  或者如果结果方法类型将有超过 255 个参数槽
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

    /** 用 arrayType 的组件类型替换最后的 arrayLength 个参数类型。
     * @param arrayType 任何数组类型
     * @param arrayLength 要更改的参数类型数量
     * @return 结果类型
     */
    /*non-public*/ MethodType asSpreaderType(Class<?> arrayType, int arrayLength) {
        assert(parameterCount() >= arrayLength);
        int spreadPos = ptypes.length - arrayLength;
        if (arrayLength == 0)  return this;  // 无需更改
        if (arrayType == Object[].class) {
            if (isGeneric())  return this;  // 无需更改
            if (spreadPos == 0) {
                // 没有要保留的前导参数；转为通用类型
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

    /** 返回前导参数类型，该参数必须存在且为引用类型。
     *  @return 前导参数类型，经过错误检查
     */
    /*non-public*/ Class<?> leadingReferenceParameter() {
        Class<?> ptype;
        if (ptypes.length == 0 ||
            (ptype = ptypes[0]).isPrimitive())
            throw newIllegalArgumentException("no leading reference parameter");
        return ptype;
    }

    /** 删除最后一个参数类型，并用 arrayLength 个 arrayType 的组件类型替换。
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
     * 查找或创建一个省略某些参数类型的方法类型。
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param start  要删除的第一个参数类型的索引（从零开始）
     * @param end    要删除的最后一个参数类型之后的索引（大于 {@code start}）
     * @return 除了选定参数被删除外，其他都相同的方法类型
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
                // 删除所有参数
                nptypes = NO_PTYPES;
            } else {
                // 删除初始参数
                nptypes = Arrays.copyOfRange(ptypes, end, len);
            }
        } else {
            if (end == len) {
                // 删除尾部参数
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
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * @param nrtype 用于替换旧返回类型的返回参数类型
     * @return 除了返回类型被更改外，其他都相同的方法类型
     * @throws NullPointerException 如果 {@code nrtype} 为 null
     */
    public MethodType changeReturnType(Class<?> nrtype) {
        if (returnType() == nrtype)  return this;
        return makeImpl(nrtype, ptypes, true);
    }

    /**
     * 报告此类型是否包含原始参数或返回值。
     * 返回类型 {@code void} 也被视为原始类型。
     * @return 如果任何类型是原始类型，则返回 true
     */
    public boolean hasPrimitives() {
        return form.hasPrimitives();
    }

    /**
     * 报告此类型是否包含包装器参数或返回值。
     * 包装器是用于包装原始值的类型，例如 {@link Integer}。
     * 如果作为返回类型出现，引用类型 {@code java.lang.Void} 也被视为包装器。
     * @return 如果任何类型是包装器，则返回 true
     */
    public boolean hasWrappers() {
        return unwrap() != this;
    }

    /**
     * 将所有引用类型擦除为 {@code Object}。
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 所有原始类型（包括 {@code void}）将保持不变。
     * @return 原始类型中所有引用类型被替换的版本
     */
    public MethodType erase() {
        return form.erasedType();
    }

    /**
     * 将所有引用类型擦除为 {@code Object}，并将所有子字类型擦除为 {@code int}。
     * 这是私有方法（如 {@link MethodHandle#invokeBasic invokeBasic}）使用的减少类型多态性。
     * @return 原始类型中所有引用和子字类型被替换的版本
     */
    /*non-public*/ MethodType basicType() {
        return form.basicType();
    }

    /**
     * @return 原始类型中 MethodHandle 作为第一个参数的版本
     */
    /*non-public*/ MethodType invokerType() {
        return insertParameterTypes(0, MethodHandle.class);
    }

    /**
     * 将所有类型（包括引用和原始类型）转换为 {@code Object}。
     * {@link #genericMethodType(int) genericMethodType} 的便捷方法。
     * 表达式 {@code type.wrap().erase()} 产生的值与 {@code type.generic()} 相同。
     * @return 原始类型中所有类型被替换的版本
     */
    public MethodType generic() {
        return genericMethodType(parameterCount());
    }

    /*non-public*/ boolean isGeneric() {
        return this == erase() && !hasPrimitives();
    }

    /**
     * 将所有原始类型转换为其对应的包装器类型。
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 所有引用类型（包括包装器类型）将保持不变。
     * 返回类型 {@code void} 将被更改为类型 {@code java.lang.Void}。
     * 表达式 {@code type.wrap().erase()} 产生的值与 {@code type.generic()} 相同。
     * @return 原始类型中所有原始类型被替换的版本
     */
    public MethodType wrap() {
        return hasPrimitives() ? wrapWithPrims(this) : this;
    }


                /**
     * 将所有包装类型转换为其对应的原始类型。
     * 用于 {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 所有原始类型（包括 {@code void}）将保持不变。
     * 返回类型为 {@code java.lang.Void} 的将变为 {@code void}。
     * @return 一个版本的原始类型，其中所有包装类型都被替换
     */
    public MethodType unwrap() {
        MethodType noprims = !hasPrimitives() ? this : wrapWithPrims(this);
        return unwrapWithNoPrims(noprims);
    }

    private static MethodType wrapWithPrims(MethodType pt) {
        assert(pt.hasPrimitives());
        MethodType wt = (MethodType)pt.wrapAlt;
        if (wt == null) {
            // 延迟填充
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
            // 延迟填充
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
     * 返回此方法类型中的参数类型数量。
     * @return 参数类型的数量
     */
    public int parameterCount() {
        return ptypes.length;
    }
    /**
     * 返回此方法类型的方法返回类型。
     * @return 方法返回类型
     */
    public Class<?> returnType() {
        return rtype;
    }

    /**
     * 将参数类型作为列表呈现（便捷方法）。
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
     * 将参数类型作为数组呈现（便捷方法）。
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
     * 它被定义为与列表的哈希码相同，列表的元素是返回类型后跟参数类型。
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
     * 返回方法类型字符串表示形式，形式为 {@code "(PT0,PT1...)RT"}。
     * 方法类型字符串表示形式是一个括号包围的、逗号分隔的类型名称列表，后跟返回类型。
     * <p>
     * 每个类型由其
     * {@link java.lang.Class#getSimpleName 简单名称} 表示。
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
     *  并且新的参数可以被视为（无需强制转换）旧的参数类型，则返回 true。
     */
    /*non-public*/
    boolean isViewableAs(MethodType newType, boolean keepInterfaces) {
        if (!VerifyType.isNullConversion(returnType(), newType.returnType(), keepInterfaces))
            return false;
        return parametersAreViewableAs(newType, keepInterfaces);
    }
    /** 如果新的参数可以被视为（无需强制转换）旧的参数类型，则返回 true。 */
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
            // 稍微复杂的测试以避免 2 次或更多次循环。
            // 如果任何类型只有 Object 参数，我们知道可以转换。
            assert(canConvertParameters(srcTypes, dstTypes));
            return true;
        }
        return canConvertParameters(srcTypes, dstTypes);
    }

    /** 如果 MHs.explicitCastArguments 产生的结果与 MH.asType 相同，则返回 true。
     *  如果类型转换对两者都不可能，结果应为 false。
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

    /** 如果 src 可以通过 MH.asType 和 MHs.eCE 转换为 dst，并且效果相同，则返回 true。
     *  MHs.eCA 对 MH.asType 有以下“升级”：
     *  1. 接口未经检查（即，视为与 Object 等效）
     *     因此，{@code Object->CharSequence} 在两种情况下都是可能的，但语义不同
     *  2. 支持完整的原始类型到原始类型的转换矩阵
     *     窄化如 {@code long->byte} 和基本类型如 {@code boolean->int}
     *     不受 asType 支持，但 asType 支持的任何内容都与 MHs.eCE 等效
     *  3a. 可以进行拆箱转换，然后进行完整的原始类型转换
     *  3b. 拆箱 null 是允许的（生成零原始值）
     *  除了接口，引用到引用的转换是相同的。
     *  将原始类型装箱为引用对于两个操作符是相同的。
     */
    private static boolean explicitCastEquivalentToAsType(Class<?> src, Class<?> dst) {
        if (src == dst || dst == Object.class || dst == void.class)  return true;
        if (src.isPrimitive()) {
            // 可能是原始类型到原始类型的转换，其中强制转换是一个严格的超集。
            // 或者是装箱转换，总是到一个精确的包装类。
            return canConvert(src, dst);
        } else if (dst.isPrimitive()) {
            // 拆箱行为在 MHs.eCA 和 MH.asType 之间不同（见 3b）。
            return false;
        } else {
            // R->R 总是有效，但必须避免对接口的检查转换。
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
        // 剩余的逻辑在 MethodHandle.asType 中有文档说明
        if (src.isPrimitive()) {
            // 可以将 void 强制转换为显式的 null，类似于 reflect.Method.invoke
            // 也可以将 void 强制转换为原始零值，作为类比
            if (src == void.class)  return true;  //or !dst.isPrimitive()?
            Wrapper sw = Wrapper.forPrimitiveType(src);
            if (dst.isPrimitive()) {
                // P->P 必须扩展
                return Wrapper.forPrimitiveType(dst).isConvertibleFrom(sw);
            } else {
                // P->R 必须装箱并扩展
                return dst.isAssignableFrom(sw.wrapperType());
            }
        } else if (dst.isPrimitive()) {
            // 任何值都可以被丢弃
            if (dst == void.class)  return true;
            Wrapper dw = Wrapper.forPrimitiveType(dst);
            // R->P 必须能够拆箱（从动态选择的类型）并扩展
            // 例如：
            //   Byte/Number/Comparable/Object -> dw:Byte -> byte.
            //   Character/Comparable/Object -> dw:Character -> char
            //   Boolean/Comparable/Object -> dw:Boolean -> boolean
            // 这意味着 dw 必须与 src 兼容。
            if (src.isAssignableFrom(dw.wrapperType())) {
                return true;
            }
            // 如果源引用被强烈类型化为一个其原始类型必须扩展的包装器，则上述情况不起作用。
            // 例如：
            //   Byte -> unbox:byte -> short/int/long/float/double
            //   Character -> unbox:char -> int/long/float/double
            if (Wrapper.isWrapperType(src) &&
                dw.isConvertibleFrom(Wrapper.forWrapperType(src))) {
                // 可以从 src 拆箱，然后扩展到 dst
                return true;
            }
            // 我们已经处理了由于运行时拆箱
            // 一个覆盖多个包装器类型的引用类型而引起的情况：
            //   Object -> cast:Integer -> unbox:int -> long/float/double
            //   Serializable -> cast:Byte -> unbox:byte -> byte/short/int/long/float/double
            // 一个边缘情况是 Number -> dw:Character -> char，如果有一个
            // 包装一个可以转换为 char 的值的 Number 子类，这是可以的。
            // 由于没有这样的子类，我们不需要在这里进行额外的检查来覆盖 char 或 boolean。
            return false;
        } else {
            // R->R 总是有效，因为 null 在动态上总是有效的
            return true;
        }
    }

    /// 与字节码架构相关的查询

    /** 报告调用此类型的方法所需的 JVM 栈槽数量。
     * 请注意，由于历史原因，JVM 要求为 long 和 double 参数传递第二个栈槽。
     * 因此，此方法返回 {@link #parameterCount() parameterCount} 加上
     * long 和 double 参数的数量（如果有）。
     * <p>
     * 此方法包括在内是为了使必须生成处理方法句柄和 invokedynamic 的字节码的应用程序受益。
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

    /** 报告包括并从给定位置开始的所有参数占用的 JVM 栈槽数量，
     * 该位置必须在 0 到 {@code parameterCount}（包括）的范围内。 后续参数
     * 在栈中更浅，参数在字节码中
     * 按其尾部边缘索引。 因此，要获取
     * 参数 {@code N} 在传出调用栈中的深度，获取
     * 其尾部边缘的 {@code parameterSlotDepth}
     * 位置为 {@code N+1}。
     * <p>
     * 类型为 {@code long} 和 {@code double} 的参数占用
     * 两个栈槽（由于历史原因），所有其他类型占用一个。
     * 因此，返回的数量是包括并从给定参数开始的参数数量，
     * 加上给定参数及其之后的 long 或 double 参数的数量。
     * <p>
     * 此方法包括在内是为了使必须生成处理方法句柄和 invokedynamic 的字节码的应用程序受益。
     * @param num 参数类型中的索引（从零开始，包括）
     * @return 传输给定参数的（最浅的）JVM 栈槽的索引
     * @throws IllegalArgumentException 如果 {@code num} 为负或大于 {@code parameterCount()}
     */
    /*non-public*/ int parameterSlotDepth(int num) {
        if (num < 0 || num > ptypes.length)
            parameterType(num);  // 强制范围检查
        return form.parameterToArgSlot(num-1);
    }


    /** 报告接收此类型方法返回值所需的JVM堆栈槽数。
     * 如果 {@link #returnType() 返回类型} 是 void，它将是零，
     * 否则如果返回类型是 long 或 double，它将是两个，否则是一个。
     * <p>
     * 该方法是为了应用程序生成处理方法句柄和 invokedynamic 的字节码而包含的。
     * @return 此类型返回值的JVM堆栈槽数（0, 1, 或 2）
     * 将在PFD中移除。
     */
    /*non-public*/ int returnSlotCount() {
        return form.returnSlotCount();
    }

    /**
     * 根据其字节码描述符的拼写查找或创建一个方法类型的实例。
     * {@link #methodType(java.lang.Class, java.lang.Class[]) methodType} 的便捷方法。
     * 描述符字符串中嵌入的任何类或接口名称
     * 将通过调用 {@link ClassLoader#loadClass(java.lang.String)}
     * 在给定的加载器（如果为 null，则在系统类加载器）中解析。
     * <p>
     * 请注意，可能会遇到无法通过此方法构造的方法类型，
     * 因为它们的组件类型不是从一个公共类加载器可访问的。
     * <p>
     * 该方法是为了应用程序生成处理方法句柄和 {@code invokedynamic} 的字节码而包含的。
     * @param descriptor 字节码级别的类型描述符字符串 "(T...)T"
     * @param loader 查找类型的类加载器
     * @return 匹配字节码级别类型描述符的方法类型
     * @throws NullPointerException 如果字符串为 null
     * @throws IllegalArgumentException 如果字符串格式不正确
     * @throws TypeNotPresentException 如果命名类型找不到
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
     * 生成方法类型的字节码描述符表示。
     * <p>
     * 请注意，这并不是 {@link #fromMethodDescriptorString fromMethodDescriptorString} 的严格逆操作。
     * 两个共享公共名称但具有不同类加载器的类
     * 在描述符字符串中看起来是相同的。
     * <p>
     * 该方法是为了应用程序生成处理方法句柄和 {@code invokedynamic} 的字节码而包含的。
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
     * 使用两次调用 {@code s.writeObject}，如下所示：
     * <blockquote><pre>{@code
s.writeObject(this.returnType());
s.writeObject(this.parameterArray());
     * }</pre></blockquote>
     * <p>
     * 反序列化时，将检查反序列化的字段值，就像它们被提供给工厂方法 {@link #methodType(Class,Class[]) methodType} 一样。
     * 例如，null 值或 {@code void} 参数类型，
     * 将在反序列化期间导致异常。
     * @param s 将对象写入的流
     * @throws java.io.IOException 如果写入对象时出现问题
     */
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();  // 需要 serialPersistentFields 是一个空数组
        s.writeObject(returnType());
        s.writeObject(parameterArray());
    }

    /**
     * 从流中重新构建 {@code MethodType} 实例（即反序列化它）。
     * 此实例是一个带有虚假最终字段的临时对象。
     * 它提供调用 {@link #readResolve readResolve} 方法的参数。
     * 调用后该对象将被丢弃。
     * @param s 从其读取对象的流
     * @throws java.io.IOException 如果读取对象时出现问题
     * @throws ClassNotFoundException 如果无法解析组件类之一
     * @see #readResolve
     * @see #writeObject
     */
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        // 分配默认值以防此对象泄露
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
     * 反序列化后解析和初始化 {@code MethodType} 对象。
     * @return 完全初始化的 {@code MethodType} 对象
     */
    private Object readResolve() {
        // 反序列化时不要使用受信任的路径：
        //    return makeImpl(rtype, ptypes, true);
        // 验证所有操作数，并确保 ptypes 是未共享的：
        // 返回一个新验证的 MethodType，用于 readObject 传递的 rtype 和 ptypes。
        MethodType mt = ((MethodType[])wrapAlt)[0];
        wrapAlt = null;
        return mt;
    }

    /**
     * 简单实现弱并发内部集。
     *
     * @param <T> 内部类型
     */
    private static class ConcurrentWeakInternSet<T> {

        private final ConcurrentMap<WeakEntry<T>, WeakEntry<T>> map;
        private final ReferenceQueue<T> stale;

        public ConcurrentWeakInternSet() {
            this.map = new ConcurrentHashMap<>();
            this.stale = new ReferenceQueue<>();
        }

        /**
         * 获取已存在的内部元素。
         * 如果没有元素被内部化，此方法返回 null。
         *
         * @param elem 要查找的元素
         * @return 已内部化的元素
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
         * 内部化元素。
         * 始终返回非 null 元素，匹配内部集中的元素。
         * 在与其他 add() 的竞争中，如果另一个线程抢先内部化了它，它可以返回 <i>不同的</i>
         * 元素。
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
