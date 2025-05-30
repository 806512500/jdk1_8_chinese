
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
import java.lang.ref.SoftReference;
import static java.lang.invoke.MethodHandleStatics.*;

/**
 * 一组方法类型共享的信息，这些方法类型仅在引用类型上不同，因此共享相同的擦除和包装。
 * <p>
 * 关于方法类型结构的实证讨论，请参见
 * <a href="http://groups.google.com/group/jvm-languages/browse_thread/thread/ac9308ae74da9b7e/">
 * jvm-languages 上的 "Avoiding Boxing" 讨论</a>。
 * JDK 中大约有 2000 种不同的擦除方法类型。
 * 未擦除的类型数量是这个数字的 10 倍多一点。
 * 同时加载的类型不会超过这些数量的一半。
 * @author John Rose
 */
final class MethodTypeForm {
    final int[] argToSlotTable, slotToArgTable;
    final long argCounts;               // 打包的槽位和值计数
    final long primCounts;              // 打包的原始类型和双精度类型计数
    final MethodType erasedType;        // 规范擦除类型
    final MethodType basicType;         // 规范擦除类型，原始类型简化

    // 缓存的适配器信息：
    @Stable final SoftReference<MethodHandle>[] methodHandles;
    // methodHandles 的索引：
    static final int
            MH_BASIC_INV      =  0,  // MH.invokeBasic 的缓存实例
            MH_NF_INV         =  1,  // LF.NamedFunction 的缓存助手
            MH_UNINIT_CS      =  2,  // 未初始化的调用站点
            MH_LIMIT          =  3;

    // 缓存的 Lambda 形式信息，仅限基本类型：
    final @Stable SoftReference<LambdaForm>[] lambdaForms;
    // lambdaForms 的索引：
    static final int
            LF_INVVIRTUAL              =  0,  // DMH invokeVirtual
            LF_INVSTATIC               =  1,
            LF_INVSPECIAL              =  2,
            LF_NEWINVSPECIAL           =  3,
            LF_INVINTERFACE            =  4,
            LF_INVSTATIC_INIT          =  5,  // DMH invokeStatic 带 <clinit> 障碍
            LF_INTERPRET               =  6,  // LF 解释器
            LF_REBIND                  =  7,  // BoundMethodHandle
            LF_DELEGATE                =  8,  // DelegatingMethodHandle
            LF_DELEGATE_BLOCK_INLINING =  9,  // 带 @DontInline 的计数 DelegatingMethodHandle
            LF_EX_LINKER               = 10,  // invokeExact_MT (用于 invokehandle)
            LF_EX_INVOKER              = 11,  // MHs.invokeExact
            LF_GEN_LINKER              = 12,  // 通用 invoke_MT (用于 invokehandle)
            LF_GEN_INVOKER             = 13,  // 通用 MHs.invoke
            LF_CS_LINKER               = 14,  // linkToCallSite_CS
            LF_MH_LINKER               = 15,  // linkToCallSite_MH
            LF_GWC                     = 16,  // guardWithCatch (catchException)
            LF_GWT                     = 17,  // guardWithTest
            LF_LIMIT                   = 18;

    /** 返回唯一对应于此 MT 形式的类型。
     *  它可能具有任何原始返回值或参数，但除了 Object 之外没有引用。
     */
    public MethodType erasedType() {
        return erasedType;
    }

    /** 返回从该 MT 形式的擦除类型派生的基本类型。
     *  基本类型是擦除的（所有引用 Object），并且所有原始类型（除了 int, long, float, double, void）都被归一化为 int。
     *  这样的基本类型对应于低级别的 JVM 调用序列。
     */
    public MethodType basicType() {
        return basicType;
    }

    private boolean assertIsBasicType() {
        // 原始类型也必须扁平化
        assert(erasedType == basicType)
                : "erasedType: " + erasedType + " != basicType: " + basicType;
        return true;
    }

    public MethodHandle cachedMethodHandle(int which) {
        assert(assertIsBasicType());
        SoftReference<MethodHandle> entry = methodHandles[which];
        return (entry != null) ? entry.get() : null;
    }

    synchronized public MethodHandle setCachedMethodHandle(int which, MethodHandle mh) {
        // 模拟 CAS，以避免结果的竞态重复。
        SoftReference<MethodHandle> entry = methodHandles[which];
        if (entry != null) {
            MethodHandle prev = entry.get();
            if (prev != null) {
                return prev;
            }
        }
        methodHandles[which] = new SoftReference<>(mh);
        return mh;
    }

    public LambdaForm cachedLambdaForm(int which) {
        assert(assertIsBasicType());
        SoftReference<LambdaForm> entry = lambdaForms[which];
        return (entry != null) ? entry.get() : null;
    }

    synchronized public LambdaForm setCachedLambdaForm(int which, LambdaForm form) {
        // 模拟 CAS，以避免结果的竞态重复。
        SoftReference<LambdaForm> entry = lambdaForms[which];
        if (entry != null) {
            LambdaForm prev = entry.get();
            if (prev != null) {
                return prev;
            }
        }
        lambdaForms[which] = new SoftReference<>(form);
        return form;
    }

    /**
     * 为给定类型构建一个 MTF，该类型必须将所有引用擦除为 Object。
     *  此 MTF 将代表该类型及其所有未擦除的变体。
     *  立即计算该类型的一些基本属性，这些属性对所有变体都是通用的。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected MethodTypeForm(MethodType erasedType) {
        this.erasedType = erasedType;

        Class<?>[] ptypes = erasedType.ptypes();
        int ptypeCount = ptypes.length;
        int pslotCount = ptypeCount;            // 临时估计
        int rtypeCount = 1;                     // 临时估计
        int rslotCount = 1;                     // 临时估计

        int[] argToSlotTab = null, slotToArgTab = null;

        // 遍历参数类型，查找原始类型。
        int pac = 0, lac = 0, prc = 0, lrc = 0;
        Class<?>[] epts = ptypes;
        Class<?>[] bpts = epts;
        for (int i = 0; i < epts.length; i++) {
            Class<?> pt = epts[i];
            if (pt != Object.class) {
                ++pac;
                Wrapper w = Wrapper.forPrimitiveType(pt);
                if (w.isDoubleWord())  ++lac;
                if (w.isSubwordOrInt() && pt != int.class) {
                    if (bpts == epts)
                        bpts = bpts.clone();
                    bpts[i] = int.class;
                }
            }
        }
        pslotCount += lac;                  // #slots = #args + #longs
        Class<?> rt = erasedType.returnType();
        Class<?> bt = rt;
        if (rt != Object.class) {
            ++prc;          // 即使 void.class 也在这里计为原始类型
            Wrapper w = Wrapper.forPrimitiveType(rt);
            if (w.isDoubleWord())  ++lrc;
            if (w.isSubwordOrInt() && rt != int.class)
                bt = int.class;
            // 调整 #slots, #args
            if (rt == void.class)
                rtypeCount = rslotCount = 0;
            else
                rslotCount += lrc;
        }
        if (epts == bpts && bt == rt) {
            this.basicType = erasedType;
        } else {
            this.basicType = MethodType.makeImpl(bt, bpts, true);
            // 从基本类型填充其余数据：
            MethodTypeForm that = this.basicType.form();
            assert(this != that);
            this.primCounts = that.primCounts;
            this.argCounts = that.argCounts;
            this.argToSlotTable = that.argToSlotTable;
            this.slotToArgTable = that.slotToArgTable;
            this.methodHandles = null;
            this.lambdaForms = null;
            return;
        }
        if (lac != 0) {
            int slot = ptypeCount + lac;
            slotToArgTab = new int[slot+1];
            argToSlotTab = new int[1+ptypeCount];
            argToSlotTab[0] = slot;  // 参数 "-1" 超出槽位范围
            for (int i = 0; i < epts.length; i++) {
                Class<?> pt = epts[i];
                Wrapper w = Wrapper.forBasicType(pt);
                if (w.isDoubleWord())  --slot;
                --slot;
                slotToArgTab[slot] = i+1; // "+1" 参见 argSlotToParameter 注释
                argToSlotTab[1+i]  = slot;
            }
            assert(slot == 0);  // 填充了表格
        } else if (pac != 0) {
            // 有原始类型但没有长原始类型；与通用类型共享槽位计数
            assert(ptypeCount == pslotCount);
            MethodTypeForm that = MethodType.genericMethodType(ptypeCount).form();
            assert(this != that);
            slotToArgTab = that.slotToArgTable;
            argToSlotTab = that.argToSlotTable;
        } else {
            int slot = ptypeCount; // 第一个参数在堆栈中最深
            slotToArgTab = new int[slot+1];
            argToSlotTab = new int[1+ptypeCount];
            argToSlotTab[0] = slot;  // 参数 "-1" 超出槽位范围
            for (int i = 0; i < ptypeCount; i++) {
                --slot;
                slotToArgTab[slot] = i+1; // "+1" 参见 argSlotToParameter 注释
                argToSlotTab[1+i]  = slot;
            }
        }
        this.primCounts = pack(lrc, prc, lac, pac);
        this.argCounts = pack(rslotCount, rtypeCount, pslotCount, ptypeCount);
        this.argToSlotTable = argToSlotTab;
        this.slotToArgTable = slotToArgTab;

        if (pslotCount >= 256)  throw newIllegalArgumentException("参数过多");

        // 初始化缓存，但仅限基本类型
        assert(basicType == erasedType);
        this.lambdaForms   = new SoftReference[LF_LIMIT];
        this.methodHandles = new SoftReference[MH_LIMIT];
    }

    private static long pack(int a, int b, int c, int d) {
        assert(((a|b|c|d) & ~0xFFFF) == 0);
        long hw = ((a << 16) | b), lw = ((c << 16) | d);
        return (hw << 32) | lw;
    }
    private static char unpack(long packed, int word) { // word==0 => 返回 a, ==3 => 返回 d
        assert(word <= 3);
        return (char)(packed >> ((3-word) * 16));
    }

    public int parameterCount() {                      // 出参数量
        return unpack(argCounts, 3);
    }
    public int parameterSlotCount() {                  // 出参解释器槽位数
        return unpack(argCounts, 2);
    }
    public int returnCount() {                         // 返回值数量 = 0 (V), 或 1
        return unpack(argCounts, 1);
    }
    public int returnSlotCount() {                     // 返回槽位数 = 0 (V), 2 (J/D), 或 1
        return unpack(argCounts, 0);
    }
    public int primitiveParameterCount() {
        return unpack(primCounts, 3);
    }
    public int longPrimitiveParameterCount() {
        return unpack(primCounts, 2);
    }
    public int primitiveReturnCount() {                // 返回值数量 = 0 (obj), 或 1
        return unpack(primCounts, 1);
    }
    public int longPrimitiveReturnCount() {            // 返回值数量 = 1 (J/D), 或 0
        return unpack(primCounts, 0);
    }
    public boolean hasPrimitives() {
        return primCounts != 0;
    }
    public boolean hasNonVoidPrimitives() {
        if (primCounts == 0)  return false;
        if (primitiveParameterCount() != 0)  return true;
        return (primitiveReturnCount() != 0 && returnCount() != 0);
    }
    public boolean hasLongPrimitives() {
        return (longPrimitiveParameterCount() | longPrimitiveReturnCount()) != 0;
    }
    public int parameterToArgSlot(int i) {
        return argToSlotTable[1+i];
    }
    public int argSlotToParameter(int argSlot) {
        // 注意：空槽位在表中用零表示。
        // 有效的参数槽位包含递增的条目，因此非零。
        // 我们返回 -1 给调用者表示空槽位。
        return slotToArgTable[argSlot] - 1;
    }

    static MethodTypeForm findForm(MethodType mt) {
        MethodType erased = canonicalize(mt, ERASE, ERASE);
        if (erased == null) {
            // 已经擦除。创建一个新的 MethodTypeForm。
            return new MethodTypeForm(mt);
        } else {
            // 与擦除版本共享 MethodTypeForm。
            return erased.form();
        }
    }

    /** {@link #canonicalize(java.lang.Class, int)} 的代码。
     * ERASE 表示将每个引用更改为 {@code Object}。
     * WRAP 表示将原始类型（包括 {@code void}）转换为相应的包装类型。UNWRAP 表示 WRAP 的逆操作。
     * INTS 表示将所有非 void 原始类型转换为 int 或 long，根据大小。LONGS 表示将所有非 void 原始类型
     * 转换为 long，无论大小。RAW_RETURN 表示将类型（假设为返回类型）转换为 int，如果它小于 int 或为 void。
     */
    public static final int NO_CHANGE = 0, ERASE = 1, WRAP = 2, UNWRAP = 3, INTS = 4, LONGS = 5, RAW_RETURN = 6;

    /** 规范化给定方法类型中的类型。
     * 如果任何类型发生变化，内部化新类型并返回。
     * 否则返回 null。
     */
    public static MethodType canonicalize(MethodType mt, int howRet, int howArgs) {
        Class<?>[] ptypes = mt.ptypes();
        Class<?>[] ptc = MethodTypeForm.canonicalizeAll(ptypes, howArgs);
        Class<?> rtype = mt.returnType();
        Class<?> rtc = MethodTypeForm.canonicalize(rtype, howRet);
        if (ptc == null && rtc == null) {
            // 已经规范化。
            return null;
        }
        // 查找方法类型的擦除版本：
        if (rtc == null)  rtc = rtype;
        if (ptc == null)  ptc = ptypes;
        return MethodType.makeImpl(rtc, ptc, true);
    }

    /** 规范化给定的返回或参数类型。
     *  如果类型已经规范化，则返回 null。
     */
    static Class<?> canonicalize(Class<?> t, int how) {
        Class<?> ct;
        if (t == Object.class) {
            // 永不改变
        } else if (!t.isPrimitive()) {
            switch (how) {
                case UNWRAP:
                    ct = Wrapper.asPrimitiveType(t);
                    if (ct != t)  return ct;
                    break;
                case RAW_RETURN:
                case ERASE:
                    return Object.class;
            }
        } else if (t == void.class) {
            // 通常不改变
            switch (how) {
                case RAW_RETURN:
                    return int.class;
                case WRAP:
                    return Void.class;
            }
        } else {
            // 非 void 原始类型
            switch (how) {
                case WRAP:
                    return Wrapper.asWrapperType(t);
                case INTS:
                    if (t == int.class || t == long.class)
                        return null;  // 无变化
                    if (t == double.class)
                        return long.class;
                    return int.class;
                case LONGS:
                    if (t == long.class)
                        return null;  // 无变化
                    return long.class;
                case RAW_RETURN:
                    if (t == int.class || t == long.class ||
                        t == float.class || t == double.class)
                        return null;  // 无变化
                    // 其他所有类型返回为 int
                    return int.class;
            }
        }
        // 无变化；返回 null 以表示
        return null;
    }
}


                /** 将给定数组中的每个参数类型规范化。
     *  如果所有类型都已经规范化，则返回 null。
     */
    static Class<?>[] canonicalizeAll(Class<?>[] ts, int how) {
        Class<?>[] cs = null;
        for (int imax = ts.length, i = 0; i < imax; i++) {
            Class<?> c = canonicalize(ts[i], how);
            if (c == void.class)
                c = null;  // 一个 Void 参数被解包为 void；忽略
            if (c != null) {
                if (cs == null)
                    cs = ts.clone();
                cs[i] = c;
            }
        }
        return cs;
    }

    @Override
    public String toString() {
        return "Form"+erasedType;
    }
}
