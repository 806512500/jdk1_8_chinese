
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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

import sun.invoke.empty.Empty;
import sun.invoke.util.ValueConversions;
import sun.invoke.util.VerifyType;
import sun.invoke.util.Wrapper;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import static java.lang.invoke.LambdaForm.*;
import static java.lang.invoke.MethodHandleStatics.*;
import static java.lang.invoke.MethodHandles.Lookup.IMPL_LOOKUP;

/**
 * MethodHandle 的受信任实现代码。
 * @author jrose
 */
/*non-public*/ abstract class MethodHandleImpl {
    // 除非为特殊平台，否则不要调整此值：
    private static final int MAX_ARITY;
    static {
        final Object[] values = { 255 };
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                values[0] = Integer.getInteger(MethodHandleImpl.class.getName()+".MAX_ARITY", 255);
                return null;
            }
        });
        MAX_ARITY = (Integer) values[0];
    }

    /// 创建方法句柄的工厂方法：

    static void initStatics() {
        // 触发选定的静态初始化。
        MemberName.Factory.INSTANCE.getClass();
    }

    static MethodHandle makeArrayElementAccessor(Class<?> arrayClass, boolean isSetter) {
        if (arrayClass == Object[].class)
            return (isSetter ? ArrayAccessor.OBJECT_ARRAY_SETTER : ArrayAccessor.OBJECT_ARRAY_GETTER);
        if (!arrayClass.isArray())
            throw newIllegalArgumentException("不是数组: "+arrayClass);
        MethodHandle[] cache = ArrayAccessor.TYPED_ACCESSORS.get(arrayClass);
        int cacheIndex = (isSetter ? ArrayAccessor.SETTER_INDEX : ArrayAccessor.GETTER_INDEX);
        MethodHandle mh = cache[cacheIndex];
        if (mh != null)  return mh;
        mh = ArrayAccessor.getAccessor(arrayClass, isSetter);
        MethodType correctType = ArrayAccessor.correctType(arrayClass, isSetter);
        if (mh.type() != correctType) {
            assert(mh.type().parameterType(0) == Object[].class);
            assert((isSetter ? mh.type().parameterType(2) : mh.type().returnType()) == Object.class);
            assert(isSetter || correctType.parameterType(0).getComponentType() == correctType.returnType());
            // 安全地将非严格视图，因为元素类型遵循数组类型
            mh = mh.viewAsType(correctType, false);
        }
        mh = makeIntrinsic(mh, (isSetter ? Intrinsic.ARRAY_STORE : Intrinsic.ARRAY_LOAD));
        // 原子更新访问器缓存。
        synchronized(cache) {
            if (cache[cacheIndex] == null) {
                cache[cacheIndex] = mh;
            } else {
                // 抛弃新构造的访问器并使用缓存版本。
                mh = cache[cacheIndex];
            }
        }
        return mh;
    }

    static final class ArrayAccessor {
        /// 支持数组元素访问
        static final int GETTER_INDEX = 0, SETTER_INDEX = 1, INDEX_LIMIT = 2;
        static final ClassValue<MethodHandle[]> TYPED_ACCESSORS
                = new ClassValue<MethodHandle[]>() {
                    @Override
                    protected MethodHandle[] computeValue(Class<?> type) {
                        return new MethodHandle[INDEX_LIMIT];
                    }
                };
        static final MethodHandle OBJECT_ARRAY_GETTER, OBJECT_ARRAY_SETTER;
        static {
            MethodHandle[] cache = TYPED_ACCESSORS.get(Object[].class);
            cache[GETTER_INDEX] = OBJECT_ARRAY_GETTER = makeIntrinsic(getAccessor(Object[].class, false), Intrinsic.ARRAY_LOAD);
            cache[SETTER_INDEX] = OBJECT_ARRAY_SETTER = makeIntrinsic(getAccessor(Object[].class, true),  Intrinsic.ARRAY_STORE);

            assert(InvokerBytecodeGenerator.isStaticallyInvocable(ArrayAccessor.OBJECT_ARRAY_GETTER.internalMemberName()));
            assert(InvokerBytecodeGenerator.isStaticallyInvocable(ArrayAccessor.OBJECT_ARRAY_SETTER.internalMemberName()));
        }

        static int     getElementI(int[]     a, int i)            { return              a[i]; }
        static long    getElementJ(long[]    a, int i)            { return              a[i]; }
        static float   getElementF(float[]   a, int i)            { return              a[i]; }
        static double  getElementD(double[]  a, int i)            { return              a[i]; }
        static boolean getElementZ(boolean[] a, int i)            { return              a[i]; }
        static byte    getElementB(byte[]    a, int i)            { return              a[i]; }
        static short   getElementS(short[]   a, int i)            { return              a[i]; }
        static char    getElementC(char[]    a, int i)            { return              a[i]; }
        static Object  getElementL(Object[]  a, int i)            { return              a[i]; }

        static void    setElementI(int[]     a, int i, int     x) {              a[i] = x; }
        static void    setElementJ(long[]    a, int i, long    x) {              a[i] = x; }
        static void    setElementF(float[]   a, int i, float   x) {              a[i] = x; }
        static void    setElementD(double[]  a, int i, double  x) {              a[i] = x; }
        static void    setElementZ(boolean[] a, int i, boolean x) {              a[i] = x; }
        static void    setElementB(byte[]    a, int i, byte    x) {              a[i] = x; }
        static void    setElementS(short[]   a, int i, short   x) {              a[i] = x; }
        static void    setElementC(char[]    a, int i, char    x) {              a[i] = x; }
        static void    setElementL(Object[]  a, int i, Object  x) {              a[i] = x; }

        static String name(Class<?> arrayClass, boolean isSetter) {
            Class<?> elemClass = arrayClass.getComponentType();
            if (elemClass == null)  throw newIllegalArgumentException("不是数组", arrayClass);
            return (!isSetter ? "getElement" : "setElement") + Wrapper.basicTypeChar(elemClass);
        }
        static MethodType type(Class<?> arrayClass, boolean isSetter) {
            Class<?> elemClass = arrayClass.getComponentType();
            Class<?> arrayArgClass = arrayClass;
            if (!elemClass.isPrimitive()) {
                arrayArgClass = Object[].class;
                elemClass = Object.class;
            }
            return !isSetter ?
                    MethodType.methodType(elemClass,  arrayArgClass, int.class) :
                    MethodType.methodType(void.class, arrayArgClass, int.class, elemClass);
        }
        static MethodType correctType(Class<?> arrayClass, boolean isSetter) {
            Class<?> elemClass = arrayClass.getComponentType();
            return !isSetter ?
                    MethodType.methodType(elemClass,  arrayClass, int.class) :
                    MethodType.methodType(void.class, arrayClass, int.class, elemClass);
        }
        static MethodHandle getAccessor(Class<?> arrayClass, boolean isSetter) {
            String     name = name(arrayClass, isSetter);
            MethodType type = type(arrayClass, isSetter);
            try {
                return IMPL_LOOKUP.findStatic(ArrayAccessor.class, name, type);
            } catch (ReflectiveOperationException ex) {
                throw uncaughtException(ex);
            }
        }
    }

    /**
     * 创建一个 JVM 级别的适配器方法句柄，以使给定的方法句柄符合新的类型，仅使用成对的参数转换。
     * 对于每个参数，将传入的参数转换为所需的精确类型。
     * 允许的参数转换包括类型转换、装箱和拆箱、整数扩展或截断、以及浮点数扩展或截断。
     * @param srcType 要求的调用类型
     * @param target 原始方法句柄
     * @param strict 如果为 true，仅允许 asType 转换；如果为 false，允许 explicitCastArguments 转换
     * @param monobox 如果为 true，拆箱转换假定为精确类型（仅 Integer 到 int，而不是 long 或 double）
     * @return 一个适配器，用于将原始句柄转换为所需的类型，如果类型已经相同则返回原始目标，或者如果无法进行适配则返回 null
     */
    static MethodHandle makePairwiseConvert(MethodHandle target, MethodType srcType,
                                            boolean strict, boolean monobox) {
        MethodType dstType = target.type();
        if (srcType == dstType)
            return target;
        return makePairwiseConvertByEditor(target, srcType, strict, monobox);
    }

    private static int countNonNull(Object[] array) {
        int count = 0;
        for (Object x : array) {
            if (x != null)  ++count;
        }
        return count;
    }

    static MethodHandle makePairwiseConvertByEditor(MethodHandle target, MethodType srcType,
                                                    boolean strict, boolean monobox) {
        Object[] convSpecs = computeValueConversions(srcType, target.type(), strict, monobox);
        int convCount = countNonNull(convSpecs);
        if (convCount == 0)
            return target.viewAsType(srcType, strict);
        MethodType basicSrcType = srcType.basicType();
        MethodType midType = target.type().basicType();
        BoundMethodHandle mh = target.rebind();
        // FIXME: 当有多个 Class 转换时减少绑定次数。
        // FIXME: 当有重复转换时减少绑定次数。
        for (int i = 0; i < convSpecs.length-1; i++) {
            Object convSpec = convSpecs[i];
            if (convSpec == null)  continue;
            MethodHandle fn;
            if (convSpec instanceof Class) {
                fn = Lazy.MH_castReference.bindTo(convSpec);
            } else {
                fn = (MethodHandle) convSpec;
            }
            Class<?> newType = basicSrcType.parameterType(i);
            if (--convCount == 0)
                midType = srcType;
            else
                midType = midType.changeParameterType(i, newType);
            LambdaForm form2 = mh.editor().filterArgumentForm(1+i, BasicType.basicType(newType));
            mh = mh.copyWithExtendL(midType, form2, fn);
            mh = mh.rebind();
        }
        Object convSpec = convSpecs[convSpecs.length-1];
        if (convSpec != null) {
            MethodHandle fn;
            if (convSpec instanceof Class) {
                if (convSpec == void.class)
                    fn = null;
                else
                    fn = Lazy.MH_castReference.bindTo(convSpec);
            } else {
                fn = (MethodHandle) convSpec;
            }
            Class<?> newType = basicSrcType.returnType();
            assert(--convCount == 0);
            midType = srcType;
            if (fn != null) {
                mh = mh.rebind();  // 如果太复杂则重新绑定
                LambdaForm form2 = mh.editor().filterReturnForm(BasicType.basicType(newType), false);
                mh = mh.copyWithExtendL(midType, form2, fn);
            } else {
                LambdaForm form2 = mh.editor().filterReturnForm(BasicType.basicType(newType), true);
                mh = mh.copyWith(midType, form2);
            }
        }
        assert(convCount == 0);
        assert(mh.type().equals(srcType));
        return mh;
    }

    static MethodHandle makePairwiseConvertIndirect(MethodHandle target, MethodType srcType,
                                                    boolean strict, boolean monobox) {
        assert(target.type().parameterCount() == srcType.parameterCount());
        // 计算在 names 数组中所需的额外参数（临时变量）。
        Object[] convSpecs = computeValueConversions(srcType, target.type(), strict, monobox);
        final int INARG_COUNT = srcType.parameterCount();
        int convCount = countNonNull(convSpecs);
        boolean retConv = (convSpecs[INARG_COUNT] != null);
        boolean retVoid = srcType.returnType() == void.class;
        if (retConv && retVoid) {
            convCount -= 1;
            retConv = false;
        }

        final int IN_MH         = 0;
        final int INARG_BASE    = 1;
        final int INARG_LIMIT   = INARG_BASE + INARG_COUNT;
        final int NAME_LIMIT    = INARG_LIMIT + convCount + 1;
        final int RETURN_CONV   = (!retConv ? -1         : NAME_LIMIT - 1);
        final int OUT_CALL      = (!retConv ? NAME_LIMIT : RETURN_CONV) - 1;
        final int RESULT        = (retVoid ? -1 : NAME_LIMIT - 1);

        // 现在构建一个 LambdaForm。
        MethodType lambdaType = srcType.basicType().invokerType();
        Name[] names = arguments(NAME_LIMIT - INARG_LIMIT, lambdaType);

        // 收集出站调用的参数，可能带有转换：
        final int OUTARG_BASE = 0;  // 目标 MH 是 Name.function，名称 Name.arguments[0]
        Object[] outArgs = new Object[OUTARG_BASE + INARG_COUNT];

        int nameCursor = INARG_LIMIT;
        for (int i = 0; i < INARG_COUNT; i++) {
            Object convSpec = convSpecs[i];
            if (convSpec == null) {
                // 什么都不做：差异是微不足道的
                outArgs[OUTARG_BASE + i] = names[INARG_BASE + i];
                continue;
            }

            Name conv;
            if (convSpec instanceof Class) {
                Class<?> convClass = (Class<?>) convSpec;
                conv = new Name(Lazy.MH_castReference, convClass, names[INARG_BASE + i]);
            } else {
                MethodHandle fn = (MethodHandle) convSpec;
                conv = new Name(fn, names[INARG_BASE + i]);
            }
            assert(names[nameCursor] == null);
            names[nameCursor++] = conv;
            assert(outArgs[OUTARG_BASE + i] == null);
            outArgs[OUTARG_BASE + i] = conv;
        }

        // 构建调用的参数数组。
        assert(nameCursor == OUT_CALL);
        names[OUT_CALL] = new Name(target, outArgs);


                    Object convSpec = convSpecs[INARG_COUNT];
        if (!retConv) {
            assert(OUT_CALL == names.length-1);
        } else {
            Name conv;
            if (convSpec == void.class) {
                conv = new Name(LambdaForm.constantZero(BasicType.basicType(srcType.returnType())));
            } else if (convSpec instanceof Class) {
                Class<?> convClass = (Class<?>) convSpec;
                conv = new Name(Lazy.MH_castReference, convClass, names[OUT_CALL]);
            } else {
                MethodHandle fn = (MethodHandle) convSpec;
                if (fn.type().parameterCount() == 0)
                    conv = new Name(fn);  // 不传递返回值进行 void 转换
                else
                    conv = new Name(fn, names[OUT_CALL]);
            }
            assert(names[RETURN_CONV] == null);
            names[RETURN_CONV] = conv;
            assert(RETURN_CONV == names.length-1);
        }

        LambdaForm form = new LambdaForm("convert", lambdaType.parameterCount(), names, RESULT);
        return SimpleMethodHandle.make(srcType, form);
    }

    /**
     * 身份函数，带有引用类型转换。
     * @param t 任意引用类型
     * @param x 任意引用值
     * @return 相同的值 x
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    static <T,U> T castReference(Class<? extends T> t, U x) {
        // 内联 Class.cast，因为我们不能 ForceInline 它
        if (x != null && !t.isInstance(x))
            throw newClassCastException(t, x);
        return (T) x;
    }

    private static ClassCastException newClassCastException(Class<?> t, Object obj) {
        return new ClassCastException("无法将 " + obj.getClass().getName() + " 转换为 " + t.getName());
    }

    static Object[] computeValueConversions(MethodType srcType, MethodType dstType,
                                            boolean strict, boolean monobox) {
        final int INARG_COUNT = srcType.parameterCount();
        Object[] convSpecs = new Object[INARG_COUNT+1];
        for (int i = 0; i <= INARG_COUNT; i++) {
            boolean isRet = (i == INARG_COUNT);
            Class<?> src = isRet ? dstType.returnType() : srcType.parameterType(i);
            Class<?> dst = isRet ? srcType.returnType() : dstType.parameterType(i);
            if (!VerifyType.isNullConversion(src, dst, /*keepInterfaces=*/ strict)) {
                convSpecs[i] = valueConversion(src, dst, strict, monobox);
            }
        }
        return convSpecs;
    }
    static MethodHandle makePairwiseConvert(MethodHandle target, MethodType srcType,
                                            boolean strict) {
        return makePairwiseConvert(target, srcType, strict, /*monobox=*/ false);
    }

    /**
     * 查找从给定源到给定目标的转换函数。
     * 此转换函数将用作 LF NamedFunction。
     * 如果需要简单的类型转换，则返回一个 Class 对象。
     * 如果涉及 void，则返回 void.class。
     */
    static Object valueConversion(Class<?> src, Class<?> dst, boolean strict, boolean monobox) {
        assert(!VerifyType.isNullConversion(src, dst, /*keepInterfaces=*/ strict));  // 调用者责任
        if (dst == void.class)
            return dst;
        MethodHandle fn;
        if (src.isPrimitive()) {
            if (src == void.class) {
                return void.class;  // 调用者必须特别识别此情况
            } else if (dst.isPrimitive()) {
                // 示例：int->byte, byte->int, boolean->int (!strict)
                fn = ValueConversions.convertPrimitive(src, dst);
            } else {
                // 示例：int->Integer, boolean->Object, float->Number
                Wrapper wsrc = Wrapper.forPrimitiveType(src);
                fn = ValueConversions.boxExact(wsrc);
                assert(fn.type().parameterType(0) == wsrc.primitiveType());
                assert(fn.type().returnType() == wsrc.wrapperType());
                if (!VerifyType.isNullConversion(wsrc.wrapperType(), dst, strict)) {
                    // 特殊情况，例如 int->Long，这可能会失败。
                    MethodType mt = MethodType.methodType(dst, src);
                    if (strict)
                        fn = fn.asType(mt);
                    else
                        fn = MethodHandleImpl.makePairwiseConvert(fn, mt, /*strict=*/ false);
                }
            }
        } else if (dst.isPrimitive()) {
            Wrapper wdst = Wrapper.forPrimitiveType(dst);
            if (monobox || src == wdst.wrapperType()) {
                // 如果可能，使用强类型解包器。
                fn = ValueConversions.unboxExact(wdst, strict);
            } else {
                // 示例：Object->int, Number->int, Comparable->int, Byte->int
                // 必须在运行时检查 src，以检测 Byte, Character 等。
                fn = (strict
                        ? ValueConversions.unboxWiden(wdst)
                        : ValueConversions.unboxCast(wdst));
            }
        } else {
            // 简单的引用类型转换。
            // 注意：不要检查 src 和 dst 之间的类层次关系。在所有情况下，'null' 参数都会通过类型转换。
            return dst;
        }
        assert(fn.type().parameterCount() <= 1) : "pc"+Arrays.asList(src.getSimpleName(), dst.getSimpleName(), fn);
        return fn;
    }

    static MethodHandle makeVarargsCollector(MethodHandle target, Class<?> arrayType) {
        MethodType type = target.type();
        int last = type.parameterCount() - 1;
        if (type.parameterType(last) != arrayType)
            target = target.asType(type.changeParameterType(last, arrayType));
        target = target.asFixedArity();  // 确保此属性已关闭
        return new AsVarargsCollector(target, arrayType);
    }

    private static final class AsVarargsCollector extends DelegatingMethodHandle {
        private final MethodHandle target;
        private final Class<?> arrayType;
        private @Stable MethodHandle asCollectorCache;

        AsVarargsCollector(MethodHandle target, Class<?> arrayType) {
            this(target.type(), target, arrayType);
        }
        AsVarargsCollector(MethodType type, MethodHandle target, Class<?> arrayType) {
            super(type, target);
            this.target = target;
            this.arrayType = arrayType;
            this.asCollectorCache = target.asCollector(arrayType, 0);
        }

        @Override
        public boolean isVarargsCollector() {
            return true;
        }

        @Override
        protected MethodHandle getTarget() {
            return target;
        }

        @Override
        public MethodHandle asFixedArity() {
            return target;
        }

        @Override
        MethodHandle setVarargs(MemberName member) {
            if (member.isVarargs())  return this;
            return asFixedArity();
        }

        @Override
        public MethodHandle asTypeUncached(MethodType newType) {
            MethodType type = this.type();
            int collectArg = type.parameterCount() - 1;
            int newArity = newType.parameterCount();
            if (newArity == collectArg+1 &&
                type.parameterType(collectArg).isAssignableFrom(newType.parameterType(collectArg))) {
                // 如果参数数量和尾参数兼容，则执行正常操作
                return asTypeCache = asFixedArity().asType(newType);
            }
            // 检查缓存
            MethodHandle acc = asCollectorCache;
            if (acc != null && acc.type().parameterCount() == newArity)
                return asTypeCache = acc.asType(newType);
            // 构建并缓存一个收集器
            int arrayLength = newArity - collectArg;
            MethodHandle collector;
            try {
                collector = asFixedArity().asCollector(arrayType, arrayLength);
                assert(collector.type().parameterCount() == newArity) : "newArity="+newArity+" but collector="+collector;
            } catch (IllegalArgumentException ex) {
                throw new WrongMethodTypeException("无法构建收集器", ex);
            }
            asCollectorCache = collector;
            return asTypeCache = collector.asType(newType);
        }

        @Override
        boolean viewAsTypeChecks(MethodType newType, boolean strict) {
            super.viewAsTypeChecks(newType, true);
            if (strict) return true;
            // 非严格检查的额外断言：
            assert (type().lastParameterType().getComponentType()
                    .isAssignableFrom(
                            newType.lastParameterType().getComponentType()))
                    : Arrays.asList(this, newType);
            return true;
        }
    }

    /** 工厂方法：展开选定的参数。 */
    static MethodHandle makeSpreadArguments(MethodHandle target,
                                            Class<?> spreadArgType, int spreadArgPos, int spreadArgCount) {
        MethodType targetType = target.type();

        for (int i = 0; i < spreadArgCount; i++) {
            Class<?> arg = VerifyType.spreadArgElementType(spreadArgType, i);
            if (arg == null)  arg = Object.class;
            targetType = targetType.changeParameterType(spreadArgPos + i, arg);
        }
        target = target.asType(targetType);

        MethodType srcType = targetType
                .replaceParameterTypes(spreadArgPos, spreadArgPos + spreadArgCount, spreadArgType);
        // 现在构建一个 LambdaForm。
        MethodType lambdaType = srcType.invokerType();
        Name[] names = arguments(spreadArgCount + 2, lambdaType);
        int nameCursor = lambdaType.parameterCount();
        int[] indexes = new int[targetType.parameterCount()];

        for (int i = 0, argIndex = 1; i < targetType.parameterCount() + 1; i++, argIndex++) {
            Class<?> src = lambdaType.parameterType(i);
            if (i == spreadArgPos) {
                // 展开数组。
                MethodHandle aload = MethodHandles.arrayElementGetter(spreadArgType);
                Name array = names[argIndex];
                names[nameCursor++] = new Name(Lazy.NF_checkSpreadArgument, array, spreadArgCount);
                for (int j = 0; j < spreadArgCount; i++, j++) {
                    indexes[i] = nameCursor;
                    names[nameCursor++] = new Name(aload, array, j);
                }
            } else if (i < indexes.length) {
                indexes[i] = argIndex;
            }
        }
        assert(nameCursor == names.length-1);  // 为最终调用留出空间

        // 构建调用的参数数组。
        Name[] targetArgs = new Name[targetType.parameterCount()];
        for (int i = 0; i < targetType.parameterCount(); i++) {
            int idx = indexes[i];
            targetArgs[i] = names[idx];
        }
        names[names.length - 1] = new Name(target, (Object[]) targetArgs);

        LambdaForm form = new LambdaForm("spread", lambdaType.parameterCount(), names);
        return SimpleMethodHandle.make(srcType, form);
    }

    static void checkSpreadArgument(Object av, int n) {
        if (av == null) {
            if (n == 0)  return;
        } else if (av instanceof Object[]) {
            int len = ((Object[])av).length;
            if (len == n)  return;
        } else {
            int len = java.lang.reflect.Array.getLength(av);
            if (len == n)  return;
        }
        // 跳转到错误：
        throw newIllegalArgumentException("数组长度不是 " + n);
    }

    /**
     * 用于引导目的的预初始化 NamedFunctions。
     * 为了延迟初始化直到首次使用，将其提取到一个内部类中。
     */
    static class Lazy {
        private static final Class<?> MHI = MethodHandleImpl.class;

        private static final MethodHandle[] ARRAYS;
        private static final MethodHandle[] FILL_ARRAYS;

        static final NamedFunction NF_checkSpreadArgument;
        static final NamedFunction NF_guardWithCatch;
        static final NamedFunction NF_throwException;
        static final NamedFunction NF_profileBoolean;

        static final MethodHandle MH_castReference;
        static final MethodHandle MH_selectAlternative;
        static final MethodHandle MH_copyAsPrimitiveArray;
        static final MethodHandle MH_fillNewTypedArray;
        static final MethodHandle MH_fillNewArray;
        static final MethodHandle MH_arrayIdentity;

        static {
            ARRAYS      = makeArrays();
            FILL_ARRAYS = makeFillArrays();

            try {
                NF_checkSpreadArgument = new NamedFunction(MHI.getDeclaredMethod("checkSpreadArgument", Object.class, int.class));
                NF_guardWithCatch      = new NamedFunction(MHI.getDeclaredMethod("guardWithCatch", MethodHandle.class, Class.class,
                                                                                 MethodHandle.class, Object[].class));
                NF_throwException      = new NamedFunction(MHI.getDeclaredMethod("throwException", Throwable.class));
                NF_profileBoolean      = new NamedFunction(MHI.getDeclaredMethod("profileBoolean", boolean.class, int[].class));

                NF_checkSpreadArgument.resolve();
                NF_guardWithCatch.resolve();
                NF_throwException.resolve();
                NF_profileBoolean.resolve();

                MH_castReference        = IMPL_LOOKUP.findStatic(MHI, "castReference",
                                            MethodType.methodType(Object.class, Class.class, Object.class));
                MH_copyAsPrimitiveArray = IMPL_LOOKUP.findStatic(MHI, "copyAsPrimitiveArray",
                                            MethodType.methodType(Object.class, Wrapper.class, Object[].class));
                MH_arrayIdentity        = IMPL_LOOKUP.findStatic(MHI, "identity",
                                            MethodType.methodType(Object[].class, Object[].class));
                MH_fillNewArray         = IMPL_LOOKUP.findStatic(MHI, "fillNewArray",
                                            MethodType.methodType(Object[].class, Integer.class, Object[].class));
                MH_fillNewTypedArray    = IMPL_LOOKUP.findStatic(MHI, "fillNewTypedArray",
                                            MethodType.methodType(Object[].class, Object[].class, Integer.class, Object[].class));

                MH_selectAlternative    = makeIntrinsic(
                        IMPL_LOOKUP.findStatic(MHI, "selectAlternative",
                                MethodType.methodType(MethodHandle.class, boolean.class, MethodHandle.class, MethodHandle.class)),
                        Intrinsic.SELECT_ALTERNATIVE);
            } catch (ReflectiveOperationException ex) {
                throw newInternalError(ex);
            }
        }
    }

    /** 工厂方法：收集或过滤选定的参数。 */
    static MethodHandle makeCollectArguments(MethodHandle target,
                MethodHandle collector, int collectArgPos, boolean retainOriginalArgs) {
        MethodType targetType = target.type();          // (a..., c, [b...])=>r
        MethodType collectorType = collector.type();    // (b...)=>c
        int collectArgCount = collectorType.parameterCount();
        Class<?> collectValType = collectorType.returnType();
        int collectValCount = (collectValType == void.class ? 0 : 1);
        MethodType srcType = targetType                 // (a..., [b...])=>r
                .dropParameterTypes(collectArgPos, collectArgPos+collectValCount);
        if (!retainOriginalArgs) {                      // (a..., b...)=>r
            srcType = srcType.insertParameterTypes(collectArgPos, collectorType.parameterList());
        }
        // in  arglist: [0: ...keep1 | cpos: collect...  | cpos+cacount: keep2... ]
        // out arglist: [0: ...keep1 | cpos: collectVal? | cpos+cvcount: keep2... ]
        // out(retain): [0: ...keep1 | cpos: cV? coll... | cpos+cvc+cac: keep2... ]


                    // 现在构建一个 LambdaForm。
        MethodType lambdaType = srcType.invokerType();
        Name[] names = arguments(2, lambdaType);
        final int collectNamePos = names.length - 2;
        final int targetNamePos  = names.length - 1;

        Name[] collectorArgs = Arrays.copyOfRange(names, 1 + collectArgPos, 1 + collectArgPos + collectArgCount);
        names[collectNamePos] = new Name(collector, (Object[]) collectorArgs);

        // 为目标构建参数数组。
        // 输入的 LF 参数为：[ (mh) headArgs collectArgs tailArgs ]。
        // 输出参数数组为 [ headArgs (collectVal)? (collectArgs)? tailArgs ]。
        Name[] targetArgs = new Name[targetType.parameterCount()];
        int inputArgPos  = 1;  // 输入的 LF 参数复制到目标
        int targetArgPos = 0;  // targetArgs 的填充指针
        int chunk = collectArgPos;  // |headArgs|
        System.arraycopy(names, inputArgPos, targetArgs, targetArgPos, chunk);
        inputArgPos  += chunk;
        targetArgPos += chunk;
        if (collectValType != void.class) {
            targetArgs[targetArgPos++] = names[collectNamePos];
        }
        chunk = collectArgCount;
        if (retainOriginalArgs) {
            System.arraycopy(names, inputArgPos, targetArgs, targetArgPos, chunk);
            targetArgPos += chunk;   // 可选地传递收集的块
        }
        inputArgPos += chunk;
        chunk = targetArgs.length - targetArgPos;  // 剩余部分
        System.arraycopy(names, inputArgPos, targetArgs, targetArgPos, chunk);
        assert(inputArgPos + chunk == collectNamePos);  // 使用剩余的输入参数
        names[targetNamePos] = new Name(target, (Object[]) targetArgs);

        LambdaForm form = new LambdaForm("collect", lambdaType.parameterCount(), names);
        return SimpleMethodHandle.make(srcType, form);
    }

    @LambdaForm.Hidden
    static
    MethodHandle selectAlternative(boolean testResult, MethodHandle target, MethodHandle fallback) {
        if (testResult) {
            return target;
        } else {
            return fallback;
        }
    }

    // 由 C2 内联化。计数器在解析期间用于计算分支频率。
    @LambdaForm.Hidden
    static
    boolean profileBoolean(boolean result, int[] counters) {
        // 计数器是 int[2]，其中 [0] 和 [1] 分别对应 false 和 true 的出现次数。
        int idx = result ? 1 : 0;
        try {
            counters[idx] = Math.addExact(counters[idx], 1);
        } catch (ArithmeticException e) {
            // 通过将有问题的计数减半来避免连续溢出。
            counters[idx] = counters[idx] / 2;
        }
        return result;
    }

    static
    MethodHandle makeGuardWithTest(MethodHandle test,
                                   MethodHandle target,
                                   MethodHandle fallback) {
        MethodType type = target.type();
        assert(test.type().equals(type.changeReturnType(boolean.class)) && fallback.type().equals(type));
        MethodType basicType = type.basicType();
        LambdaForm form = makeGuardWithTestForm(basicType);
        BoundMethodHandle mh;
        try {
            if (PROFILE_GWT) {
                int[] counts = new int[2];
                mh = (BoundMethodHandle)
                        BoundMethodHandle.speciesData_LLLL().constructor().invokeBasic(type, form,
                                (Object) test, (Object) profile(target), (Object) profile(fallback), counts);
            } else {
                mh = (BoundMethodHandle)
                        BoundMethodHandle.speciesData_LLL().constructor().invokeBasic(type, form,
                                (Object) test, (Object) profile(target), (Object) profile(fallback));
            }
        } catch (Throwable ex) {
            throw uncaughtException(ex);
        }
        assert(mh.type() == type);
        return mh;
    }


    static
    MethodHandle profile(MethodHandle target) {
        if (DONT_INLINE_THRESHOLD >= 0) {
            return makeBlockInlningWrapper(target);
        } else {
            return target;
        }
    }

    /**
     * 在 JIT 编译目标方法句柄时，如果调用次数不足，则阻止内联。
     * 对应的 LambdaForm 在编译成字节码时带有 @DontInline。
     */
    static
    MethodHandle makeBlockInlningWrapper(MethodHandle target) {
        LambdaForm lform = PRODUCE_BLOCK_INLINING_FORM.apply(target);
        return new CountingWrapper(target, lform,
                PRODUCE_BLOCK_INLINING_FORM, PRODUCE_REINVOKER_FORM,
                                   DONT_INLINE_THRESHOLD);
    }

    /** 构建在 JIT 编译时阻止内联的特定方法句柄的重新调用 LambdaForm */
    private static final Function<MethodHandle, LambdaForm> PRODUCE_BLOCK_INLINING_FORM = new Function<MethodHandle, LambdaForm>() {
        @Override
        public LambdaForm apply(MethodHandle target) {
            return DelegatingMethodHandle.makeReinvokerForm(target,
                               MethodTypeForm.LF_DELEGATE_BLOCK_INLINING, CountingWrapper.class, "reinvoker.dontInline", false,
                               DelegatingMethodHandle.NF_getTarget, CountingWrapper.NF_maybeStopCounting);
        }
    };

    /** 构建特定方法句柄的简单重新调用 LambdaForm */
    private static final Function<MethodHandle, LambdaForm> PRODUCE_REINVOKER_FORM = new Function<MethodHandle, LambdaForm>() {
        @Override
        public LambdaForm apply(MethodHandle target) {
            return DelegatingMethodHandle.makeReinvokerForm(target,
                    MethodTypeForm.LF_DELEGATE, DelegatingMethodHandle.class, DelegatingMethodHandle.NF_getTarget);
        }
    };

    /**
     * 计数方法句柄。它有 2 个状态：计数和非计数。
     * 在前 n 次调用中处于计数状态，然后转换为非计数状态。
     * 计数和非计数状态的行为分别由 countingFormProducer 和 nonCountingFormProducer 生成的 LambdaForm 确定。
     */
    static class CountingWrapper extends DelegatingMethodHandle {
        private final MethodHandle target;
        private int count;
        private Function<MethodHandle, LambdaForm> countingFormProducer;
        private Function<MethodHandle, LambdaForm> nonCountingFormProducer;
        private volatile boolean isCounting;

        private CountingWrapper(MethodHandle target, LambdaForm lform,
                                Function<MethodHandle, LambdaForm> countingFromProducer,
                                Function<MethodHandle, LambdaForm> nonCountingFormProducer,
                                int count) {
            super(target.type(), lform);
            this.target = target;
            this.count = count;
            this.countingFormProducer = countingFromProducer;
            this.nonCountingFormProducer = nonCountingFormProducer;
            this.isCounting = (count > 0);
        }

        @Hidden
        @Override
        protected MethodHandle getTarget() {
            return target;
        }

        @Override
        public MethodHandle asTypeUncached(MethodType newType) {
            MethodHandle newTarget = target.asType(newType);
            MethodHandle wrapper;
            if (isCounting) {
                LambdaForm lform;
                lform = countingFormProducer.apply(newTarget);
                wrapper = new CountingWrapper(newTarget, lform, countingFormProducer, nonCountingFormProducer, DONT_INLINE_THRESHOLD);
            } else {
                wrapper = newTarget; // 不再需要计数包装器
            }
            return (asTypeCache = wrapper);
        }

        boolean countDown() {
            if (count <= 0) {
                // 尽量减少更新次数。MethodHandle.updateForm() 不保证 LF 更新的可见性。
                if (isCounting) {
                    isCounting = false;
                    return true;
                } else {
                    return false;
                }
            } else {
                --count;
                return false;
            }
        }

        @Hidden
        static void maybeStopCounting(Object o1) {
             CountingWrapper wrapper = (CountingWrapper) o1;
             if (wrapper.countDown()) {
                 // 达到调用阈值。用非计数行为替换计数行为。
                 LambdaForm lform = wrapper.nonCountingFormProducer.apply(wrapper.target);
                 lform.compileToBytecode(); // 通过避免在转换后再次解释 LF 来加速预热
                 wrapper.updateForm(lform);
             }
        }

        static final NamedFunction NF_maybeStopCounting;
        static {
            Class<?> THIS_CLASS = CountingWrapper.class;
            try {
                NF_maybeStopCounting = new NamedFunction(THIS_CLASS.getDeclaredMethod("maybeStopCounting", Object.class));
            } catch (ReflectiveOperationException ex) {
                throw newInternalError(ex);
            }
        }
    }

    static
    LambdaForm makeGuardWithTestForm(MethodType basicType) {
        LambdaForm lform = basicType.form().cachedLambdaForm(MethodTypeForm.LF_GWT);
        if (lform != null)  return lform;
        final int THIS_MH      = 0;  // the BMH_LLL
        final int ARG_BASE     = 1;  // 输入参数的开始
        final int ARG_LIMIT    = ARG_BASE + basicType.parameterCount();
        int nameCursor = ARG_LIMIT;
        final int GET_TEST     = nameCursor++;
        final int GET_TARGET   = nameCursor++;
        final int GET_FALLBACK = nameCursor++;
        final int GET_COUNTERS = PROFILE_GWT ? nameCursor++ : -1;
        final int CALL_TEST    = nameCursor++;
        final int PROFILE      = (GET_COUNTERS != -1) ? nameCursor++ : -1;
        final int TEST         = nameCursor-1; // 上一条语句：要么是 PROFILE 要么是 CALL_TEST
        final int SELECT_ALT   = nameCursor++;
        final int CALL_TARGET  = nameCursor++;
        assert(CALL_TARGET == SELECT_ALT+1);  // 必须为真以触发 IBG.emitSelectAlternative

        MethodType lambdaType = basicType.invokerType();
        Name[] names = arguments(nameCursor - ARG_LIMIT, lambdaType);

        BoundMethodHandle.SpeciesData data =
                (GET_COUNTERS != -1) ? BoundMethodHandle.speciesData_LLLL()
                                     : BoundMethodHandle.speciesData_LLL();
        names[THIS_MH] = names[THIS_MH].withConstraint(data);
        names[GET_TEST]     = new Name(data.getterFunction(0), names[THIS_MH]);
        names[GET_TARGET]   = new Name(data.getterFunction(1), names[THIS_MH]);
        names[GET_FALLBACK] = new Name(data.getterFunction(2), names[THIS_MH]);
        if (GET_COUNTERS != -1) {
            names[GET_COUNTERS] = new Name(data.getterFunction(3), names[THIS_MH]);
        }
        Object[] invokeArgs = Arrays.copyOfRange(names, 0, ARG_LIMIT, Object[].class);

        // 调用测试
        MethodType testType = basicType.changeReturnType(boolean.class).basicType();
        invokeArgs[0] = names[GET_TEST];
        names[CALL_TEST] = new Name(testType, invokeArgs);

        // 分支配置文件
        if (PROFILE != -1) {
            names[PROFILE] = new Name(Lazy.NF_profileBoolean, names[CALL_TEST], names[GET_COUNTERS]);
        }
        // 调用 selectAlternative
        names[SELECT_ALT] = new Name(Lazy.MH_selectAlternative, names[TEST], names[GET_TARGET], names[GET_FALLBACK]);

        // 调用目标或回退
        invokeArgs[0] = names[SELECT_ALT];
        names[CALL_TARGET] = new Name(basicType, invokeArgs);

        lform = new LambdaForm("guard", lambdaType.parameterCount(), names, /*forceInline=*/true);

        return basicType.form().setCachedLambdaForm(MethodTypeForm.LF_GWT, lform);
    }

    /**
     * catchException 组合器的 LambdaForm 形状如下：
     * <blockquote><pre>{@code
     *  guardWithCatch=Lambda(a0:L,a1:L,a2:L)=>{
     *    t3:L=BoundMethodHandle$Species_LLLLL.argL0(a0:L);
     *    t4:L=BoundMethodHandle$Species_LLLLL.argL1(a0:L);
     *    t5:L=BoundMethodHandle$Species_LLLLL.argL2(a0:L);
     *    t6:L=BoundMethodHandle$Species_LLLLL.argL3(a0:L);
     *    t7:L=BoundMethodHandle$Species_LLLLL.argL4(a0:L);
     *    t8:L=MethodHandle.invokeBasic(t6:L,a1:L,a2:L);
     *    t9:L=MethodHandleImpl.guardWithCatch(t3:L,t4:L,t5:L,t8:L);
     *   t10:I=MethodHandle.invokeBasic(t7:L,t9:L);t10:I}
     * }</pre></blockquote>
     *
     * argL0 和 argL2 是目标和捕获器方法句柄。argL1 是异常类。
     * argL3 和 argL4 是辅助方法句柄：argL3 将参数打包并将其包装成 Object[]
     * (ValueConversions.array())，argL4 在必要时解包结果 (ValueConversions.unbox())。
     *
     * 将 t8 和 t10 传递到外部而不是硬编码到 LambdaForm 中，允许在具有相同基本类型的 catchException 组合器之间共享 LambdaForm。
     */
    private static LambdaForm makeGuardWithCatchForm(MethodType basicType) {
        MethodType lambdaType = basicType.invokerType();

        LambdaForm lform = basicType.form().cachedLambdaForm(MethodTypeForm.LF_GWC);
        if (lform != null) {
            return lform;
        }
        final int THIS_MH      = 0;  // the BMH_LLLLL
        final int ARG_BASE     = 1;  // 输入参数的开始
        final int ARG_LIMIT    = ARG_BASE + basicType.parameterCount();

        int nameCursor = ARG_LIMIT;
        final int GET_TARGET       = nameCursor++;
        final int GET_CLASS        = nameCursor++;
        final int GET_CATCHER      = nameCursor++;
        final int GET_COLLECT_ARGS = nameCursor++;
        final int GET_UNBOX_RESULT = nameCursor++;
        final int BOXED_ARGS       = nameCursor++;
        final int TRY_CATCH        = nameCursor++;
        final int UNBOX_RESULT     = nameCursor++;

        Name[] names = arguments(nameCursor - ARG_LIMIT, lambdaType);

        BoundMethodHandle.SpeciesData data = BoundMethodHandle.speciesData_LLLLL();
        names[THIS_MH]          = names[THIS_MH].withConstraint(data);
        names[GET_TARGET]       = new Name(data.getterFunction(0), names[THIS_MH]);
        names[GET_CLASS]        = new Name(data.getterFunction(1), names[THIS_MH]);
        names[GET_CATCHER]      = new Name(data.getterFunction(2), names[THIS_MH]);
        names[GET_COLLECT_ARGS] = new Name(data.getterFunction(3), names[THIS_MH]);
        names[GET_UNBOX_RESULT] = new Name(data.getterFunction(4), names[THIS_MH]);

        // FIXME: 重新设计 LF 解释时的参数打包/结果解包逻辑

        // t_{i}:L=MethodHandle.invokeBasic(collectArgs:L,a1:L,...);
        MethodType collectArgsType = basicType.changeReturnType(Object.class);
        MethodHandle invokeBasic = MethodHandles.basicInvoker(collectArgsType);
        Object[] args = new Object[invokeBasic.type().parameterCount()];
        args[0] = names[GET_COLLECT_ARGS];
        System.arraycopy(names, ARG_BASE, args, 1, ARG_LIMIT-ARG_BASE);
        names[BOXED_ARGS] = new Name(makeIntrinsic(invokeBasic, Intrinsic.GUARD_WITH_CATCH), args);

        // t_{i+1}:L=MethodHandleImpl.guardWithCatch(target:L,exType:L,catcher:L,t_{i}:L);
        Object[] gwcArgs = new Object[] {names[GET_TARGET], names[GET_CLASS], names[GET_CATCHER], names[BOXED_ARGS]};
        names[TRY_CATCH] = new Name(Lazy.NF_guardWithCatch, gwcArgs);


                    // t_{i+2}:I=MethodHandle.invokeBasic(unbox:L,t_{i+1}:L);
        MethodHandle invokeBasicUnbox = MethodHandles.basicInvoker(MethodType.methodType(basicType.rtype(), Object.class));
        Object[] unboxArgs  = new Object[] {names[GET_UNBOX_RESULT], names[TRY_CATCH]};
        names[UNBOX_RESULT] = new Name(invokeBasicUnbox, unboxArgs);

        lform = new LambdaForm("guardWithCatch", lambdaType.parameterCount(), names);

        return basicType.form().setCachedLambdaForm(MethodTypeForm.LF_GWC, lform);
    }

    static
    MethodHandle makeGuardWithCatch(MethodHandle target,
                                    Class<? extends Throwable> exType,
                                    MethodHandle catcher) {
        MethodType type = target.type();
        LambdaForm form = makeGuardWithCatchForm(type.basicType());

        // 准备在 LambdaForm 解释期间使用的辅助方法句柄。
        // 将参数装箱并将其包装到 Object[] 中: ValueConversions.array()。
        MethodType varargsType = type.changeReturnType(Object[].class);
        MethodHandle collectArgs = varargsArray(type.parameterCount()).asType(varargsType);
        // 结果拆箱: ValueConversions.unbox() 或 ValueConversions.identity() 或 ValueConversions.ignore()。
        MethodHandle unboxResult;
        Class<?> rtype = type.returnType();
        if (rtype.isPrimitive()) {
            if (rtype == void.class) {
                unboxResult = ValueConversions.ignore();
            } else {
                Wrapper w = Wrapper.forPrimitiveType(type.returnType());
                unboxResult = ValueConversions.unboxExact(w);
            }
        } else {
            unboxResult = MethodHandles.identity(Object.class);
        }

        BoundMethodHandle.SpeciesData data = BoundMethodHandle.speciesData_LLLLL();
        BoundMethodHandle mh;
        try {
            mh = (BoundMethodHandle)
                    data.constructor().invokeBasic(type, form, (Object) target, (Object) exType, (Object) catcher,
                                                   (Object) collectArgs, (Object) unboxResult);
        } catch (Throwable ex) {
            throw uncaughtException(ex);
        }
        assert(mh.type() == type);
        return mh;
    }

    /**
     * 在 LambdaForm 编译期间内联化
     * (参见 {@link InvokerBytecodeGenerator#emitGuardWithCatch emitGuardWithCatch})。
     */
    @LambdaForm.Hidden
    static Object guardWithCatch(MethodHandle target, Class<? extends Throwable> exType, MethodHandle catcher,
                                 Object... av) throws Throwable {
        // 使用 asFixedArity() 避免在 VarargsCollector 情况下对最后一个参数进行不必要的装箱。
        try {
            return target.asFixedArity().invokeWithArguments(av);
        } catch (Throwable t) {
            if (!exType.isInstance(t)) throw t;
            return catcher.asFixedArity().invokeWithArguments(prepend(t, av));
        }
    }

    /** 将元素 {@code elem} 添加到 {@code array} 的前面。 */
    @LambdaForm.Hidden
    private static Object[] prepend(Object elem, Object[] array) {
        Object[] newArray = new Object[array.length+1];
        newArray[0] = elem;
        System.arraycopy(array, 0, newArray, 1, array.length);
        return newArray;
    }

    static
    MethodHandle throwException(MethodType type) {
        assert(Throwable.class.isAssignableFrom(type.parameterType(0)));
        int arity = type.parameterCount();
        if (arity > 1) {
            MethodHandle mh = throwException(type.dropParameterTypes(1, arity));
            mh = MethodHandles.dropArguments(mh, 1, type.parameterList().subList(1, arity));
            return mh;
        }
        return makePairwiseConvert(Lazy.NF_throwException.resolvedHandle(), type, false, true);
    }

    static <T extends Throwable> Empty throwException(T t) throws T { throw t; }

    static MethodHandle[] FAKE_METHOD_HANDLE_INVOKE = new MethodHandle[2];
    static MethodHandle fakeMethodHandleInvoke(MemberName method) {
        int idx;
        assert(method.isMethodHandleInvoke());
        switch (method.getName()) {
        case "invoke":       idx = 0; break;
        case "invokeExact":  idx = 1; break;
        default:             throw new InternalError(method.getName());
        }
        MethodHandle mh = FAKE_METHOD_HANDLE_INVOKE[idx];
        if (mh != null)  return mh;
        MethodType type = MethodType.methodType(Object.class, UnsupportedOperationException.class,
                                                MethodHandle.class, Object[].class);
        mh = throwException(type);
        mh = mh.bindTo(new UnsupportedOperationException("cannot reflectively invoke MethodHandle"));
        if (!method.getInvocationType().equals(mh.type()))
            throw new InternalError(method.toString());
        mh = mh.withInternalMemberName(method, false);
        mh = mh.asVarargsCollector(Object[].class);
        assert(method.isVarargs());
        FAKE_METHOD_HANDLE_INVOKE[idx] = mh;
        return mh;
    }

    /**
     * 为方法句柄创建一个别名，当调用时，
     * 看起来是从与 hostClass 相同的类加载器和保护域调用的。
     * 除非调用的方法对其调用者敏感，否则这是一个昂贵的空操作。一些系统方法
     * 属于此类别，包括 Class.forName 和 Method.invoke。
     */
    static
    MethodHandle bindCaller(MethodHandle mh, Class<?> hostClass) {
        return BindCaller.bindCaller(mh, hostClass);
    }

    // 将整个混乱放入自己的嵌套类中。
    // 这样可以延迟加载代码并设置常量。
    private static class BindCaller {
        static
        MethodHandle bindCaller(MethodHandle mh, Class<?> hostClass) {
            // 不要使用此函数将调用注入系统类。
            if (hostClass == null
                ||    (hostClass.isArray() ||
                       hostClass.isPrimitive() ||
                       hostClass.getName().startsWith("java.") ||
                       hostClass.getName().startsWith("sun."))) {
                throw new InternalError();  // 不会发生，也不应该发生
            }
            // 为简单起见，将 mh 转换为类似 varargs 的方法。
            MethodHandle vamh = prepareForInvoker(mh);
            // 每个参数类缓存一次 makeInjectedInvoker 的结果。
            MethodHandle bccInvoker = CV_makeInjectedInvoker.get(hostClass);
            return restoreToType(bccInvoker.bindTo(vamh), mh, hostClass);
        }

        private static MethodHandle makeInjectedInvoker(Class<?> hostClass) {
            Class<?> bcc = UNSAFE.defineAnonymousClass(hostClass, T_BYTES, null);
            if (hostClass.getClassLoader() != bcc.getClassLoader())
                throw new InternalError(hostClass.getName()+" (CL)");
            try {
                if (hostClass.getProtectionDomain() != bcc.getProtectionDomain())
                    throw new InternalError(hostClass.getName()+" (PD)");
            } catch (SecurityException ex) {
                // 安全管理器阻止了自我检查。这是可以的。
                // 事实上，整个 try 体可以转换为断言。
            }
            try {
                MethodHandle init = IMPL_LOOKUP.findStatic(bcc, "init", MethodType.methodType(void.class));
                init.invokeExact();  // 强制初始化类
            } catch (Throwable ex) {
                throw uncaughtException(ex);
            }
            MethodHandle bccInvoker;
            try {
                MethodType invokerMT = MethodType.methodType(Object.class, MethodHandle.class, Object[].class);
                bccInvoker = IMPL_LOOKUP.findStatic(bcc, "invoke_V", invokerMT);
            } catch (ReflectiveOperationException ex) {
                throw uncaughtException(ex);
            }
            // 测试调用者，以确保它确实注入到正确的位置。
            try {
                MethodHandle vamh = prepareForInvoker(MH_checkCallerClass);
                Object ok = bccInvoker.invokeExact(vamh, new Object[]{hostClass, bcc});
            } catch (Throwable ex) {
                throw new InternalError(ex);
            }
            return bccInvoker;
        }
        private static ClassValue<MethodHandle> CV_makeInjectedInvoker = new ClassValue<MethodHandle>() {
            @Override protected MethodHandle computeValue(Class<?> hostClass) {
                return makeInjectedInvoker(hostClass);
            }
        };

        // 适应 mh 以便可以从注入的调用者直接调用：
        private static MethodHandle prepareForInvoker(MethodHandle mh) {
            mh = mh.asFixedArity();
            MethodType mt = mh.type();
            int arity = mt.parameterCount();
            MethodHandle vamh = mh.asType(mt.generic());
            vamh.internalForm().compileToBytecode();  // 消除 LFI 堆栈帧
            vamh = vamh.asSpreader(Object[].class, arity);
            vamh.internalForm().compileToBytecode();  // 消除 LFI 堆栈帧
            return vamh;
        }

        // 撤销 prepareForInvoker 的适配效果：
        private static MethodHandle restoreToType(MethodHandle vamh,
                                                  MethodHandle original,
                                                  Class<?> hostClass) {
            MethodType type = original.type();
            MethodHandle mh = vamh.asCollector(Object[].class, type.parameterCount());
            MemberName member = original.internalMemberName();
            mh = mh.asType(type);
            mh = new WrappedMember(mh, type, member, original.isInvokeSpecial(), hostClass);
            return mh;
        }

        private static final MethodHandle MH_checkCallerClass;
        static {
            final Class<?> THIS_CLASS = BindCaller.class;
            assert(checkCallerClass(THIS_CLASS, THIS_CLASS));
            try {
                MH_checkCallerClass = IMPL_LOOKUP
                    .findStatic(THIS_CLASS, "checkCallerClass",
                                MethodType.methodType(boolean.class, Class.class, Class.class));
                assert((boolean) MH_checkCallerClass.invokeExact(THIS_CLASS, THIS_CLASS));
            } catch (Throwable ex) {
                throw new InternalError(ex);
            }
        }

        @CallerSensitive
        private static boolean checkCallerClass(Class<?> expected, Class<?> expected2) {
            // 该方法通过 MH_checkCallerClass 调用，因此在这里询问直接调用者是正确的。
            Class<?> actual = Reflection.getCallerClass();
            if (actual != expected && actual != expected2)
                throw new InternalError("found "+actual.getName()+", expected "+expected.getName()
                                        +(expected == expected2 ? "" : ", or else "+expected2.getName()));
            return true;
        }

        private static final byte[] T_BYTES;
        static {
            final Object[] values = {null};
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        try {
                            Class<T> tClass = T.class;
                            String tName = tClass.getName();
                            String tResource = tName.substring(tName.lastIndexOf('.')+1)+".class";
                            java.net.URLConnection uconn = tClass.getResource(tResource).openConnection();
                            int len = uconn.getContentLength();
                            byte[] bytes = new byte[len];
                            try (java.io.InputStream str = uconn.getInputStream()) {
                                int nr = str.read(bytes);
                                if (nr != len)  throw new java.io.IOException(tResource);
                            }
                            values[0] = bytes;
                        } catch (java.io.IOException ex) {
                            throw new InternalError(ex);
                        }
                        return null;
                    }
                });
            T_BYTES = (byte[]) values[0];
        }

        // 以下类用作 Unsafe.defineAnonymousClass 的模板：
        private static class T {
            static void init() { }  // 附带效果：初始化此类
            static Object invoke_V(MethodHandle vamh, Object[] args) throws Throwable {
                return vamh.invokeExact(args);
            }
        }
    }


    /** 此子类允许将包装的方法句柄重新关联到任意成员名称。 */
    private static final class WrappedMember extends DelegatingMethodHandle {
        private final MethodHandle target;
        private final MemberName member;
        private final Class<?> callerClass;
        private final boolean isInvokeSpecial;

        private WrappedMember(MethodHandle target, MethodType type,
                              MemberName member, boolean isInvokeSpecial,
                              Class<?> callerClass) {
            super(type, target);
            this.target = target;
            this.member = member;
            this.callerClass = callerClass;
            this.isInvokeSpecial = isInvokeSpecial;
        }

        @Override
        MemberName internalMemberName() {
            return member;
        }
        @Override
        Class<?> internalCallerClass() {
            return callerClass;
        }
        @Override
        boolean isInvokeSpecial() {
            return isInvokeSpecial;
        }
        @Override
        protected MethodHandle getTarget() {
            return target;
        }
        @Override
        public MethodHandle asTypeUncached(MethodType newType) {
            // 此 MH 是 target 的别名，除了 MemberName
            // 如果有任何转换，则丢弃 MemberName。
            return asTypeCache = target.asType(newType);
        }
    }

    static MethodHandle makeWrappedMember(MethodHandle target, MemberName member, boolean isInvokeSpecial) {
        if (member.equals(target.internalMemberName()) && isInvokeSpecial == target.isInvokeSpecial())
            return target;
        return new WrappedMember(target, target.type(), member, isInvokeSpecial, null);
    }

    /** 内联 ID */
    /*non-public*/
    enum Intrinsic {
        SELECT_ALTERNATIVE,
        GUARD_WITH_CATCH,
        NEW_ARRAY,
        ARRAY_LOAD,
        ARRAY_STORE,
        IDENTITY,
        ZERO,
        NONE // 无关联的内联
    }

    /** 标记任意方法句柄为内联。
     * InvokerBytecodeGenerator 使用此信息生成更高效的字节码形状。 */
    private static final class IntrinsicMethodHandle extends DelegatingMethodHandle {
        private final MethodHandle target;
        private final Intrinsic intrinsicName;

        IntrinsicMethodHandle(MethodHandle target, Intrinsic intrinsicName) {
            super(target.type(), target);
            this.target = target;
            this.intrinsicName = intrinsicName;
        }

        @Override
        protected MethodHandle getTarget() {
            return target;
        }


                    @Override
        Intrinsic intrinsicName() {
            return intrinsicName;
        }

        @Override
        public MethodHandle asTypeUncached(MethodType newType) {
            // 该方法句柄是目标方法句柄的别名，除了内在名称
            // 如果有任何转换，则丢弃名称。
            return asTypeCache = target.asType(newType);
        }

        @Override
        String internalProperties() {
            return super.internalProperties() +
                    "\n& Intrinsic="+intrinsicName;
        }

        @Override
        public MethodHandle asCollector(Class<?> arrayType, int arrayLength) {
            if (intrinsicName == Intrinsic.IDENTITY) {
                MethodType resultType = type().asCollectorType(arrayType, arrayLength);
                MethodHandle newArray = MethodHandleImpl.varargsArray(arrayType, arrayLength);
                return newArray.asType(resultType);
            }
            return super.asCollector(arrayType, arrayLength);
        }
    }

    static MethodHandle makeIntrinsic(MethodHandle target, Intrinsic intrinsicName) {
        if (intrinsicName == target.intrinsicName())
            return target;
        return new IntrinsicMethodHandle(target, intrinsicName);
    }

    static MethodHandle makeIntrinsic(MethodType type, LambdaForm form, Intrinsic intrinsicName) {
        return new IntrinsicMethodHandle(SimpleMethodHandle.make(type, form), intrinsicName);
    }

    /// 收集多个参数。

    private static MethodHandle findCollector(String name, int nargs, Class<?> rtype, Class<?>... ptypes) {
        MethodType type = MethodType.genericMethodType(nargs)
                .changeReturnType(rtype)
                .insertParameterTypes(0, ptypes);
        try {
            return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, name, type);
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private static final Object[] NO_ARGS_ARRAY = {};
    private static Object[] makeArray(Object... args) { return args; }
    private static Object[] array() { return NO_ARGS_ARRAY; }
    private static Object[] array(Object a0)
                { return makeArray(a0); }
    private static Object[] array(Object a0, Object a1)
                { return makeArray(a0, a1); }
    private static Object[] array(Object a0, Object a1, Object a2)
                { return makeArray(a0, a1, a2); }
    private static Object[] array(Object a0, Object a1, Object a2, Object a3)
                { return makeArray(a0, a1, a2, a3); }
    private static Object[] array(Object a0, Object a1, Object a2, Object a3,
                                  Object a4)
                { return makeArray(a0, a1, a2, a3, a4); }
    private static Object[] array(Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5)
                { return makeArray(a0, a1, a2, a3, a4, a5); }
    private static Object[] array(Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6)
                { return makeArray(a0, a1, a2, a3, a4, a5, a6); }
    private static Object[] array(Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6, Object a7)
                { return makeArray(a0, a1, a2, a3, a4, a5, a6, a7); }
    private static Object[] array(Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6, Object a7,
                                  Object a8)
                { return makeArray(a0, a1, a2, a3, a4, a5, a6, a7, a8); }
    private static Object[] array(Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6, Object a7,
                                  Object a8, Object a9)
                { return makeArray(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
    private static MethodHandle[] makeArrays() {
        ArrayList<MethodHandle> mhs = new ArrayList<>();
        for (;;) {
            MethodHandle mh = findCollector("array", mhs.size(), Object[].class);
            if (mh == null)  break;
            mh = makeIntrinsic(mh, Intrinsic.NEW_ARRAY);
            mhs.add(mh);
        }
        assert(mhs.size() == 11);  // 当前方法的数量
        return mhs.toArray(new MethodHandle[MAX_ARITY+1]);
    }

    // 填充版本的上述方法：
    // 使用 Integer len 而不是 int len 以及没有可变参数以避免引导问题
    private static Object[] fillNewArray(Integer len, Object[] /*not ...*/ args) {
        Object[] a = new Object[len];
        fillWithArguments(a, 0, args);
        return a;
    }
    private static Object[] fillNewTypedArray(Object[] example, Integer len, Object[] /*not ...*/ args) {
        Object[] a = Arrays.copyOf(example, len);
        assert(a.getClass() != Object[].class);
        fillWithArguments(a, 0, args);
        return a;
    }
    private static void fillWithArguments(Object[] a, int pos, Object... args) {
        System.arraycopy(args, 0, a, pos, args.length);
    }
    // 使用 Integer pos 而不是 int pos 以避免引导问题
    private static Object[] fillArray(Integer pos, Object[] a, Object a0)
                { fillWithArguments(a, pos, a0); return a; }
    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1)
                { fillWithArguments(a, pos, a0, a1); return a; }
    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2)
                { fillWithArguments(a, pos, a0, a1, a2); return a; }
    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2, Object a3)
                { fillWithArguments(a, pos, a0, a1, a2, a3); return a; }
    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2, Object a3,
                                  Object a4)
                { fillWithArguments(a, pos, a0, a1, a2, a3, a4); return a; }
    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5)
                { fillWithArguments(a, pos, a0, a1, a2, a3, a4, a5); return a; }
    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6)
                { fillWithArguments(a, pos, a0, a1, a2, a3, a4, a5, a6); return a; }
    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6, Object a7)
                { fillWithArguments(a, pos, a0, a1, a2, a3, a4, a5, a6, a7); return a; }
    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6, Object a7,
                                  Object a8)
                { fillWithArguments(a, pos, a0, a1, a2, a3, a4, a5, a6, a7, a8); return a; }
    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6, Object a7,
                                  Object a8, Object a9)
                { fillWithArguments(a, pos, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); return a; }

    private static final int FILL_ARRAYS_COUNT = 11; // 当前 fillArray 方法的数量

    private static MethodHandle[] makeFillArrays() {
        ArrayList<MethodHandle> mhs = new ArrayList<>();
        mhs.add(null);  // 没有空的填充；至少需要 a0
        for (;;) {
            MethodHandle mh = findCollector("fillArray", mhs.size(), Object[].class, Integer.class, Object[].class);
            if (mh == null)  break;
            mhs.add(mh);
        }
        assert(mhs.size() == FILL_ARRAYS_COUNT);
        return mhs.toArray(new MethodHandle[0]);
    }

    private static Object copyAsPrimitiveArray(Wrapper w, Object... boxes) {
        Object a = w.makeArray(boxes.length);
        w.copyArrayUnboxing(boxes, 0, a, 0, boxes.length);
        return a;
    }

    /** 返回一个方法句柄，该方法句柄接受指定数量的 Object 参数并返回一个包含它们的 Object 数组，就像用于可变参数一样。 */
    static MethodHandle varargsArray(int nargs) {
        MethodHandle mh = Lazy.ARRAYS[nargs];
        if (mh != null)  return mh;
        mh = findCollector("array", nargs, Object[].class);
        if (mh != null)  mh = makeIntrinsic(mh, Intrinsic.NEW_ARRAY);
        if (mh != null)  return Lazy.ARRAYS[nargs] = mh;
        mh = buildVarargsArray(Lazy.MH_fillNewArray, Lazy.MH_arrayIdentity, nargs);
        assert(assertCorrectArity(mh, nargs));
        mh = makeIntrinsic(mh, Intrinsic.NEW_ARRAY);
        return Lazy.ARRAYS[nargs] = mh;
    }

    private static boolean assertCorrectArity(MethodHandle mh, int arity) {
        assert(mh.type().parameterCount() == arity) : "arity != "+arity+": "+mh;
        return true;
    }

    // 数组标识函数（用作 Lazy.MH_arrayIdentity）。
    static <T> T[] identity(T[] x) {
        return x;
    }

    private static MethodHandle buildVarargsArray(MethodHandle newArray, MethodHandle finisher, int nargs) {
        // 构建结果 mh 作为一系列填充，如下所示：
        //   finisher(fill(fill(newArrayWA(23,x1..x10),10,x11..x20),20,x21..x23))
        // 各种 fill(_,10*I,___*[J]) 是可重用的。
        int leftLen = Math.min(nargs, LEFT_ARGS);  // 立即吸收一些参数
        int rightLen = nargs - leftLen;
        MethodHandle leftCollector = newArray.bindTo(nargs);
        leftCollector = leftCollector.asCollector(Object[].class, leftLen);
        MethodHandle mh = finisher;
        if (rightLen > 0) {
            MethodHandle rightFiller = fillToRight(LEFT_ARGS + rightLen);
            if (mh == Lazy.MH_arrayIdentity)
                mh = rightFiller;
            else
                mh = MethodHandles.collectArguments(mh, 0, rightFiller);
        }
        if (mh == Lazy.MH_arrayIdentity)
            mh = leftCollector;
        else
            mh = MethodHandles.collectArguments(mh, 0, leftCollector);
        return mh;
    }

    private static final int LEFT_ARGS = FILL_ARRAYS_COUNT - 1;
    private static final MethodHandle[] FILL_ARRAY_TO_RIGHT = new MethodHandle[MAX_ARITY+1];
    /** fill_array_to_right(N).invoke(a, argL..arg[N-1])
     *  用相应的参数填充 a[L]..a[N-1]，
     *  然后返回 a。值 L 是一个全局常量 (LEFT_ARGS)。
     */
    private static MethodHandle fillToRight(int nargs) {
        MethodHandle filler = FILL_ARRAY_TO_RIGHT[nargs];
        if (filler != null)  return filler;
        filler = buildFiller(nargs);
        assert(assertCorrectArity(filler, nargs - LEFT_ARGS + 1));
        return FILL_ARRAY_TO_RIGHT[nargs] = filler;
    }
    private static MethodHandle buildFiller(int nargs) {
        if (nargs <= LEFT_ARGS)
            return Lazy.MH_arrayIdentity;  // 没有参数需要填充；返回数组不变
        // 我们需要为 mh 和 a 在 mh.invoke(a, arg*[nargs]) 中留出空间
        final int CHUNK = LEFT_ARGS;
        int rightLen = nargs % CHUNK;
        int midLen = nargs - rightLen;
        if (rightLen == 0) {
            midLen = nargs - (rightLen = CHUNK);
            if (FILL_ARRAY_TO_RIGHT[midLen] == null) {
                // 从左到右构建一些前驱
                for (int j = LEFT_ARGS % CHUNK; j < midLen; j += CHUNK)
                    if (j > LEFT_ARGS)  fillToRight(j);
            }
        }
        if (midLen < LEFT_ARGS) rightLen = nargs - (midLen = LEFT_ARGS);
        assert(rightLen > 0);
        MethodHandle midFill = fillToRight(midLen);  // 递归填充
        MethodHandle rightFill = Lazy.FILL_ARRAYS[rightLen].bindTo(midLen);  // [midLen..nargs-1]
        assert(midFill.type().parameterCount()   == 1 + midLen - LEFT_ARGS);
        assert(rightFill.type().parameterCount() == 1 + rightLen);

        // 结合两个填充：
        //   right(mid(a, x10..x19), x20..x23)
        // 最终产品将如下所示：
        //   right(mid(newArrayLeft(24, x0..x9), x10..x19), x20..x23)
        if (midLen == LEFT_ARGS)
            return rightFill;
        else
            return MethodHandles.collectArguments(rightFill, 0, midFill);
    }

    // 类型多态版本的可变参数生成器。
    private static final ClassValue<MethodHandle[]> TYPED_COLLECTORS
        = new ClassValue<MethodHandle[]>() {
            @Override
            protected MethodHandle[] computeValue(Class<?> type) {
                return new MethodHandle[256];
            }
    };

    static final int MAX_JVM_ARITY = 255;  // JVM 施加的限制

    /** 返回一个方法句柄，该方法句柄接受指定数量的
     *  类型化参数并返回一个包含它们的数组。
     *  类型参数是数组类型。
     */
    static MethodHandle varargsArray(Class<?> arrayType, int nargs) {
        Class<?> elemType = arrayType.getComponentType();
        if (elemType == null)  throw new IllegalArgumentException("not an array: "+arrayType);
        // FIXME: 需要更多的特殊处理和缓存。
        if (nargs >= MAX_JVM_ARITY/2 - 1) {
            int slots = nargs;
            final int MAX_ARRAY_SLOTS = MAX_JVM_ARITY - 1;  // 1 用于接收器 MH
            if (slots <= MAX_ARRAY_SLOTS && elemType.isPrimitive())
                slots *= Wrapper.forPrimitiveType(elemType).stackSlots();
            if (slots > MAX_ARRAY_SLOTS)
                throw new IllegalArgumentException("too many arguments: "+arrayType.getSimpleName()+", length "+nargs);
        }
        if (elemType == Object.class)
            return varargsArray(nargs);
        // 其他情况：原始数组，Object[] 的子类型
        MethodHandle cache[] = TYPED_COLLECTORS.get(elemType);
        MethodHandle mh = nargs < cache.length ? cache[nargs] : null;
        if (mh != null)  return mh;
        if (nargs == 0) {
            Object example = java.lang.reflect.Array.newInstance(arrayType.getComponentType(), 0);
            mh = MethodHandles.constant(arrayType, example);
        } else if (elemType.isPrimitive()) {
            MethodHandle builder = Lazy.MH_fillNewArray;
            MethodHandle producer = buildArrayProducer(arrayType);
            mh = buildVarargsArray(builder, producer, nargs);
        } else {
            Class<? extends Object[]> objArrayType = arrayType.asSubclass(Object[].class);
            Object[] example = Arrays.copyOf(NO_ARGS_ARRAY, 0, objArrayType);
            MethodHandle builder = Lazy.MH_fillNewTypedArray.bindTo(example);
            MethodHandle producer = Lazy.MH_arrayIdentity; // 必须是弱类型
            mh = buildVarargsArray(builder, producer, nargs);
        }
        mh = mh.asType(MethodType.methodType(arrayType, Collections.<Class<?>>nCopies(nargs, elemType)));
        mh = makeIntrinsic(mh, Intrinsic.NEW_ARRAY);
        assert(assertCorrectArity(mh, nargs));
        if (nargs < cache.length)
            cache[nargs] = mh;
        return mh;
    }

    private static MethodHandle buildArrayProducer(Class<?> arrayType) {
        Class<?> elemType = arrayType.getComponentType();
        assert(elemType.isPrimitive());
        return Lazy.MH_copyAsPrimitiveArray.bindTo(Wrapper.forPrimitiveType(elemType));
    }

    /*non-public*/ static void assertSame(Object mh1, Object mh2) {
        if (mh1 != mh2) {
            String msg = String.format("mh1 != mh2: mh1 = %s (form: %s); mh2 = %s (form: %s)",
                    mh1, ((MethodHandle)mh1).form,
                    mh2, ((MethodHandle)mh2).form);
            throw newInternalError(msg);
        }
    }
}
