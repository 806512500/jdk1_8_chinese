
/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.ref.SoftReference;
import java.util.Arrays;
import static java.lang.invoke.LambdaForm.*;
import static java.lang.invoke.LambdaForm.BasicType.*;
import static java.lang.invoke.MethodHandleImpl.Intrinsic;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import sun.invoke.util.Wrapper;

/** LambdaForm 的转换。
 *  一个 LambdaForm 编辑器可以从其基础 LambdaForm 派生新的 LambdaForm。
 *  编辑器可以缓存派生的 LambdaForm，这简化了其底层字节码的重用。
 *  为了支持这种缓存，LambdaForm 有一个可选的指向其编辑器的指针。
 */
class LambdaFormEditor {
    final LambdaForm lambdaForm;

    private LambdaFormEditor(LambdaForm lambdaForm) {
        this.lambdaForm = lambdaForm;
    }

    // 工厂方法。
    static LambdaFormEditor lambdaFormEditor(LambdaForm lambdaForm) {
        // TO DO:  考虑在此处放置 intern 逻辑，以减少重复。
        // lambdaForm = findPreexistingEquivalent(lambdaForm)

        // 始终使用未自定义的版本进行编辑。
        // 这有助于缓存，自定义的 LambdaForm 重用 transformCache 字段来保持与未自定义版本的链接。
        return new LambdaFormEditor(lambdaForm.uncustomize());
    }

    /** 缓存转换的描述，可能与转换结果相关联。
     *  逻辑内容是从 Kind.ordinal 值开始的一系列字节值。
     *  该序列未终止，以不定数量的零字节结束。
     *  简单的序列（足够短且值足够小）可以打包成一个 64 位的 long。
     */
    private static final class Transform extends SoftReference<LambdaForm> {
        final long packedBytes;
        final byte[] fullBytes;

        private enum Kind {
            NO_KIND,  // 必要的，因为 ordinal 必须大于零
            BIND_ARG, ADD_ARG, DUP_ARG,
            SPREAD_ARGS,
            FILTER_ARG, FILTER_RETURN, FILTER_RETURN_TO_ZERO,
            COLLECT_ARGS, COLLECT_ARGS_TO_VOID, COLLECT_ARGS_TO_ARRAY,
            FOLD_ARGS, FOLD_ARGS_TO_VOID,
            PERMUTE_ARGS
            // 可能添加更多用于 guard with test, catch exception, pointwise type conversions
        }

        private static final boolean STRESS_TEST = false; // 打开以禁用大多数打包
        private static final int
                PACKED_BYTE_SIZE = (STRESS_TEST ? 2 : 4),
                PACKED_BYTE_MASK = (1 << PACKED_BYTE_SIZE) - 1,
                PACKED_BYTE_MAX_LENGTH = (STRESS_TEST ? 3 : 64 / PACKED_BYTE_SIZE);

        private static long packedBytes(byte[] bytes) {
            if (bytes.length > PACKED_BYTE_MAX_LENGTH)  return 0;
            long pb = 0;
            int bitset = 0;
            for (int i = 0; i < bytes.length; i++) {
                int b = bytes[i] & 0xFF;
                bitset |= b;
                pb |= (long)b << (i * PACKED_BYTE_SIZE);
            }
            if (!inRange(bitset))
                return 0;
            return pb;
        }
        private static long packedBytes(int b0, int b1) {
            assert(inRange(b0 | b1));
            return (  (b0 << 0*PACKED_BYTE_SIZE)
                    | (b1 << 1*PACKED_BYTE_SIZE));
        }
        private static long packedBytes(int b0, int b1, int b2) {
            assert(inRange(b0 | b1 | b2));
            return (  (b0 << 0*PACKED_BYTE_SIZE)
                    | (b1 << 1*PACKED_BYTE_SIZE)
                    | (b2 << 2*PACKED_BYTE_SIZE));
        }
        private static long packedBytes(int b0, int b1, int b2, int b3) {
            assert(inRange(b0 | b1 | b2 | b3));
            return (  (b0 << 0*PACKED_BYTE_SIZE)
                    | (b1 << 1*PACKED_BYTE_SIZE)
                    | (b2 << 2*PACKED_BYTE_SIZE)
                    | (b3 << 3*PACKED_BYTE_SIZE));
        }
        private static boolean inRange(int bitset) {
            assert((bitset & 0xFF) == bitset);  // 输入值必须适合 *无符号* 字节
            return ((bitset & ~PACKED_BYTE_MASK) == 0);
        }
        private static byte[] fullBytes(int... byteValues) {
            byte[] bytes = new byte[byteValues.length];
            int i = 0;
            for (int bv : byteValues) {
                bytes[i++] = bval(bv);
            }
            assert(packedBytes(bytes) == 0);
            return bytes;
        }

        private byte byteAt(int i) {
            long pb = packedBytes;
            if (pb == 0) {
                if (i >= fullBytes.length)  return 0;
                return fullBytes[i];
            }
            assert(fullBytes == null);
            if (i > PACKED_BYTE_MAX_LENGTH)  return 0;
            int pos = (i * PACKED_BYTE_SIZE);
            return (byte)((pb >>> pos) & PACKED_BYTE_MASK);
        }

        Kind kind() { return Kind.values()[byteAt(0)]; }

        private Transform(long packedBytes, byte[] fullBytes, LambdaForm result) {
            super(result);
            this.packedBytes = packedBytes;
            this.fullBytes = fullBytes;
        }
        private Transform(long packedBytes) {
            this(packedBytes, null, null);
            assert(packedBytes != 0);
        }
        private Transform(byte[] fullBytes) {
            this(0, fullBytes, null);
        }

        private static byte bval(int b) {
            assert((b & 0xFF) == b);  // 输入值必须适合 *无符号* 字节
            return (byte)b;
        }
        private static byte bval(Kind k) {
            return bval(k.ordinal());
        }
        static Transform of(Kind k, int b1) {
            byte b0 = bval(k);
            if (inRange(b0 | b1))
                return new Transform(packedBytes(b0, b1));
            else
                return new Transform(fullBytes(b0, b1));
        }
        static Transform of(Kind k, int b1, int b2) {
            byte b0 = (byte) k.ordinal();
            if (inRange(b0 | b1 | b2))
                return new Transform(packedBytes(b0, b1, b2));
            else
                return new Transform(fullBytes(b0, b1, b2));
        }
        static Transform of(Kind k, int b1, int b2, int b3) {
            byte b0 = (byte) k.ordinal();
            if (inRange(b0 | b1 | b2 | b3))
                return new Transform(packedBytes(b0, b1, b2, b3));
            else
                return new Transform(fullBytes(b0, b1, b2, b3));
        }
        private static final byte[] NO_BYTES = {};
        static Transform of(Kind k, int... b123) {
            return ofBothArrays(k, b123, NO_BYTES);
        }
        static Transform of(Kind k, int b1, byte[] b234) {
            return ofBothArrays(k, new int[]{ b1 }, b234);
        }
        static Transform of(Kind k, int b1, int b2, byte[] b345) {
            return ofBothArrays(k, new int[]{ b1, b2 }, b345);
        }
        private static Transform ofBothArrays(Kind k, int[] b123, byte[] b456) {
            byte[] fullBytes = new byte[1 + b123.length + b456.length];
            int i = 0;
            fullBytes[i++] = bval(k);
            for (int bv : b123) {
                fullBytes[i++] = bval(bv);
            }
            for (byte bv : b456) {
                fullBytes[i++] = bv;
            }
            long packedBytes = packedBytes(fullBytes);
            if (packedBytes != 0)
                return new Transform(packedBytes);
            else
                return new Transform(fullBytes);
        }


                    Transform withResult(LambdaForm result) {
            return new Transform(this.packedBytes, this.fullBytes, result);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Transform && equals((Transform)obj);
        }
        public boolean equals(Transform that) {
            return this.packedBytes == that.packedBytes && Arrays.equals(this.fullBytes, that.fullBytes);
        }
        @Override
        public int hashCode() {
            if (packedBytes != 0) {
                assert(fullBytes == null);
                return Long.hashCode(packedBytes);
            }
            return Arrays.hashCode(fullBytes);
        }
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            long bits = packedBytes;
            if (bits != 0) {
                buf.append("(");
                while (bits != 0) {
                    buf.append(bits & PACKED_BYTE_MASK);
                    bits >>>= PACKED_BYTE_SIZE;
                    if (bits != 0)  buf.append(",");
                }
                buf.append(")");
            }
            if (fullBytes != null) {
                buf.append("unpacked");
                buf.append(Arrays.toString(fullBytes));
            }
            LambdaForm result = get();
            if (result != null) {
                buf.append(" result=");
                buf.append(result);
            }
            return buf.toString();
        }
    }

    /** 查找与给定转换等效的先前缓存的转换，并返回其结果。 */
    private LambdaForm getInCache(Transform key) {
        assert(key.get() == null);
        // transformCache 可以是 null、Transform、Transform[] 或 ConcurrentHashMap。
        Object c = lambdaForm.transformCache;
        Transform k = null;
        if (c instanceof ConcurrentHashMap) {
            @SuppressWarnings("unchecked")
            ConcurrentHashMap<Transform,Transform> m = (ConcurrentHashMap<Transform,Transform>) c;
            k = m.get(key);
        } else if (c == null) {
            return null;
        } else if (c instanceof Transform) {
            // 单元素缓存避免数组的开销
            Transform t = (Transform)c;
            if (t.equals(key))  k = t;
        } else {
            Transform[] ta = (Transform[])c;
            for (int i = 0; i < ta.length; i++) {
                Transform t = ta[i];
                if (t == null)  break;
                if (t.equals(key)) { k = t; break; }
            }
        }
        assert(k == null || key.equals(k));
        return (k != null) ? k.get() : null;
    }

    /** 用于缓存的 Transform[] 大小的任意但合理的限制。 */
    private static final int MIN_CACHE_ARRAY_SIZE = 4, MAX_CACHE_ARRAY_SIZE = 16;

    /** 缓存一个转换及其结果，并返回该结果。
     *  但如果已经缓存了等效的转换，则返回其结果。
     */
    private LambdaForm putInCache(Transform key, LambdaForm form) {
        key = key.withResult(form);
        for (int pass = 0; ; pass++) {
            Object c = lambdaForm.transformCache;
            if (c instanceof ConcurrentHashMap) {
                @SuppressWarnings("unchecked")
                ConcurrentHashMap<Transform,Transform> m = (ConcurrentHashMap<Transform,Transform>) c;
                Transform k = m.putIfAbsent(key, key);
                if (k == null) return form;
                LambdaForm result = k.get();
                if (result != null) {
                    return result;
                } else {
                    if (m.replace(key, k, key)) {
                        return form;
                    } else {
                        continue;
                    }
                }
            }
            assert(pass == 0);
            synchronized (lambdaForm) {
                c = lambdaForm.transformCache;
                if (c instanceof ConcurrentHashMap)
                    continue;
                if (c == null) {
                    lambdaForm.transformCache = key;
                    return form;
                }
                Transform[] ta;
                if (c instanceof Transform) {
                    Transform k = (Transform)c;
                    if (k.equals(key)) {
                        LambdaForm result = k.get();
                        if (result == null) {
                            lambdaForm.transformCache = key;
                            return form;
                        } else {
                            return result;
                        }
                    } else if (k.get() == null) { // 覆盖过时的条目
                        lambdaForm.transformCache = key;
                        return form;
                    }
                    // 将单元素缓存扩展为小数组
                    ta = new Transform[MIN_CACHE_ARRAY_SIZE];
                    ta[0] = k;
                    lambdaForm.transformCache = ta;
                } else {
                    // 已经扩展
                    ta = (Transform[])c;
                }
                int len = ta.length;
                int stale = -1;
                int i;
                for (i = 0; i < len; i++) {
                    Transform k = ta[i];
                    if (k == null) {
                        break;
                    }
                    if (k.equals(key)) {
                        LambdaForm result = k.get();
                        if (result == null) {
                            ta[i] = key;
                            return form;
                        } else {
                            return result;
                        }
                    } else if (stale < 0 && k.get() == null) {
                        stale = i; // 记住第一个过时的条目索引
                    }
                }
                if (i < len || stale >= 0) {
                    // 直接进入缓存更新
                } else if (len < MAX_CACHE_ARRAY_SIZE) {
                    len = Math.min(len * 2, MAX_CACHE_ARRAY_SIZE);
                    ta = Arrays.copyOf(ta, len);
                    lambdaForm.transformCache = ta;
                } else {
                    ConcurrentHashMap<Transform, Transform> m = new ConcurrentHashMap<>(MAX_CACHE_ARRAY_SIZE * 2);
                    for (Transform k : ta) {
                        m.put(k, k);
                    }
                    lambdaForm.transformCache = m;
                    // 第二次迭代将为此查询并发更新。
                    continue;
                }
                int idx = (stale >= 0) ? stale : i;
                ta[idx] = key;
                return form;
            }
        }
    }


                private LambdaFormBuffer buffer() {
        return new LambdaFormBuffer(lambdaForm);
    }

    /// 编辑方法句柄的方法。这些方法需要有快速路径。

    private BoundMethodHandle.SpeciesData oldSpeciesData() {
        return BoundMethodHandle.speciesData(lambdaForm);
    }
    private BoundMethodHandle.SpeciesData newSpeciesData(BasicType type) {
        return oldSpeciesData().extendWith(type);
    }

    BoundMethodHandle bindArgumentL(BoundMethodHandle mh, int pos, Object value) {
        assert(mh.speciesData() == oldSpeciesData());
        BasicType bt = L_TYPE;
        MethodType type2 = bindArgumentType(mh, pos, bt);
        LambdaForm form2 = bindArgumentForm(1+pos);
        return mh.copyWithExtendL(type2, form2, value);
    }
    BoundMethodHandle bindArgumentI(BoundMethodHandle mh, int pos, int value) {
        assert(mh.speciesData() == oldSpeciesData());
        BasicType bt = I_TYPE;
        MethodType type2 = bindArgumentType(mh, pos, bt);
        LambdaForm form2 = bindArgumentForm(1+pos);
        return mh.copyWithExtendI(type2, form2, value);
    }

    BoundMethodHandle bindArgumentJ(BoundMethodHandle mh, int pos, long value) {
        assert(mh.speciesData() == oldSpeciesData());
        BasicType bt = J_TYPE;
        MethodType type2 = bindArgumentType(mh, pos, bt);
        LambdaForm form2 = bindArgumentForm(1+pos);
        return mh.copyWithExtendJ(type2, form2, value);
    }

    BoundMethodHandle bindArgumentF(BoundMethodHandle mh, int pos, float value) {
        assert(mh.speciesData() == oldSpeciesData());
        BasicType bt = F_TYPE;
        MethodType type2 = bindArgumentType(mh, pos, bt);
        LambdaForm form2 = bindArgumentForm(1+pos);
        return mh.copyWithExtendF(type2, form2, value);
    }

    BoundMethodHandle bindArgumentD(BoundMethodHandle mh, int pos, double value) {
        assert(mh.speciesData() == oldSpeciesData());
        BasicType bt = D_TYPE;
        MethodType type2 = bindArgumentType(mh, pos, bt);
        LambdaForm form2 = bindArgumentForm(1+pos);
        return mh.copyWithExtendD(type2, form2, value);
    }

    private MethodType bindArgumentType(BoundMethodHandle mh, int pos, BasicType bt) {
        assert(mh.form.uncustomize() == lambdaForm);
        assert(mh.form.names[1+pos].type == bt);
        assert(BasicType.basicType(mh.type().parameterType(pos)) == bt);
        return mh.type().dropParameterTypes(pos, pos+1);
    }

    /// 编辑 lambda 形式的方法。
    // 每个编辑方法都可以（潜在地）缓存编辑后的 LF，以便以后重用。

    LambdaForm bindArgumentForm(int pos) {
        Transform key = Transform.of(Transform.Kind.BIND_ARG, pos);
        LambdaForm form = getInCache(key);
        if (form != null) {
            assert(form.parameterConstraint(0) == newSpeciesData(lambdaForm.parameterType(pos)));
            return form;
        }
        LambdaFormBuffer buf = buffer();
        buf.startEdit();

        BoundMethodHandle.SpeciesData oldData = oldSpeciesData();
        BoundMethodHandle.SpeciesData newData = newSpeciesData(lambdaForm.parameterType(pos));
        Name oldBaseAddress = lambdaForm.parameter(0);  // 持有值的 BMH
        Name newBaseAddress;
        NamedFunction getter = newData.getterFunction(oldData.fieldCount());

        if (pos != 0) {
            // 新创建的 LF 将使用不同的 BMH 运行。
            // 将任何现有的 BMH 字段引用切换到新的 BMH 类。
            buf.replaceFunctions(oldData.getterFunctions(), newData.getterFunctions(), oldBaseAddress);
            newBaseAddress = oldBaseAddress.withConstraint(newData);
            buf.renameParameter(0, newBaseAddress);
            buf.replaceParameterByNewExpression(pos, new Name(getter, newBaseAddress));
        } else {
            // 除非 oldData 为空，否则不能绑定 MH 参数本身
            assert(oldData == BoundMethodHandle.SpeciesData.EMPTY);
            newBaseAddress = new Name(L_TYPE).withConstraint(newData);
            buf.replaceParameterByNewExpression(0, new Name(getter, newBaseAddress));
            buf.insertParameter(0, newBaseAddress);
        }

        form = buf.endEdit();
        return putInCache(key, form);
    }

    LambdaForm addArgumentForm(int pos, BasicType type) {
        Transform key = Transform.of(Transform.Kind.ADD_ARG, pos, type.ordinal());
        LambdaForm form = getInCache(key);
        if (form != null) {
            assert(form.arity == lambdaForm.arity+1);
            assert(form.parameterType(pos) == type);
            return form;
        }
        LambdaFormBuffer buf = buffer();
        buf.startEdit();

        buf.insertParameter(pos, new Name(type));

        form = buf.endEdit();
        return putInCache(key, form);
    }

    LambdaForm dupArgumentForm(int srcPos, int dstPos) {
        Transform key = Transform.of(Transform.Kind.DUP_ARG, srcPos, dstPos);
        LambdaForm form = getInCache(key);
        if (form != null) {
            assert(form.arity == lambdaForm.arity-1);
            return form;
        }
        LambdaFormBuffer buf = buffer();
        buf.startEdit();

        assert(lambdaForm.parameter(srcPos).constraint == null);
        assert(lambdaForm.parameter(dstPos).constraint == null);
        buf.replaceParameterByCopy(dstPos, srcPos);

        form = buf.endEdit();
        return putInCache(key, form);
    }

    LambdaForm spreadArgumentsForm(int pos, Class<?> arrayType, int arrayLength) {
        Class<?> elementType = arrayType.getComponentType();
        Class<?> erasedArrayType = arrayType;
        if (!elementType.isPrimitive())
            erasedArrayType = Object[].class;
        BasicType bt = basicType(elementType);
        int elementTypeKey = bt.ordinal();
        if (bt.basicTypeClass() != elementType) {
            if (elementType.isPrimitive()) {
                elementTypeKey = TYPE_LIMIT + Wrapper.forPrimitiveType(elementType).ordinal();
            }
        }
        Transform key = Transform.of(Transform.Kind.SPREAD_ARGS, pos, elementTypeKey, arrayLength);
        LambdaForm form = getInCache(key);
        if (form != null) {
            assert(form.arity == lambdaForm.arity - arrayLength + 1);
            return form;
        }
        LambdaFormBuffer buf = buffer();
        buf.startEdit();


                    assert(pos <= MethodType.MAX_JVM_ARITY);
        assert(pos + arrayLength <= lambdaForm.arity);
        assert(pos > 0);  // 不能展开 MH 参数本身

        Name spreadParam = new Name(L_TYPE);
        Name checkSpread = new Name(MethodHandleImpl.Lazy.NF_checkSpreadArgument, spreadParam, arrayLength);

        // 插入新的表达式
        int exprPos = lambdaForm.arity();
        buf.insertExpression(exprPos++, checkSpread);
        // 调整参数
        MethodHandle aload = MethodHandles.arrayElementGetter(erasedArrayType);
        for (int i = 0; i < arrayLength; i++) {
            Name loadArgument = new Name(aload, spreadParam, i);
            buf.insertExpression(exprPos + i, loadArgument);
            buf.replaceParameterByCopy(pos + i, exprPos + i);
        }
        buf.insertParameter(pos, spreadParam);

        form = buf.endEdit();
        return putInCache(key, form);
    }

    LambdaForm collectArgumentsForm(int pos, MethodType collectorType) {
        int collectorArity = collectorType.parameterCount();
        boolean dropResult = (collectorType.returnType() == void.class);
        if (collectorArity == 1 && !dropResult) {
            return filterArgumentForm(pos, basicType(collectorType.parameterType(0)));
        }
        BasicType[] newTypes = BasicType.basicTypes(collectorType.parameterList());
        Transform.Kind kind = (dropResult
                ? Transform.Kind.COLLECT_ARGS_TO_VOID
                : Transform.Kind.COLLECT_ARGS);
        if (dropResult && collectorArity == 0)  pos = 1;  // 纯副作用
        Transform key = Transform.of(kind, pos, collectorArity, BasicType.basicTypesOrd(newTypes));
        LambdaForm form = getInCache(key);
        if (form != null) {
            assert(form.arity == lambdaForm.arity - (dropResult ? 0 : 1) + collectorArity);
            return form;
        }
        form = makeArgumentCombinationForm(pos, collectorType, false, dropResult);
        return putInCache(key, form);
    }

    LambdaForm collectArgumentArrayForm(int pos, MethodHandle arrayCollector) {
        MethodType collectorType = arrayCollector.type();
        int collectorArity = collectorType.parameterCount();
        assert(arrayCollector.intrinsicName() == Intrinsic.NEW_ARRAY);
        Class<?> arrayType = collectorType.returnType();
        Class<?> elementType = arrayType.getComponentType();
        BasicType argType = basicType(elementType);
        int argTypeKey = argType.ordinal();
        if (argType.basicTypeClass() != elementType) {
            // 如果需要更多元数据（如 String[].class），则返回 null
            if (!elementType.isPrimitive())
                return null;
            argTypeKey = TYPE_LIMIT + Wrapper.forPrimitiveType(elementType).ordinal();
        }
        assert(collectorType.parameterList().equals(Collections.nCopies(collectorArity, elementType)));
        Transform.Kind kind = Transform.Kind.COLLECT_ARGS_TO_ARRAY;
        Transform key = Transform.of(kind, pos, collectorArity, argTypeKey);
        LambdaForm form = getInCache(key);
        if (form != null) {
            assert(form.arity == lambdaForm.arity - 1 + collectorArity);
            return form;
        }
        LambdaFormBuffer buf = buffer();
        buf.startEdit();

        assert(pos + 1 <= lambdaForm.arity);
        assert(pos > 0);  // 不能过滤 MH 参数本身

        Name[] newParams = new Name[collectorArity];
        for (int i = 0; i < collectorArity; i++) {
            newParams[i] = new Name(pos + i, argType);
        }
        Name callCombiner = new Name(arrayCollector, (Object[]) /*...*/ newParams);

        // 插入新的表达式
        int exprPos = lambdaForm.arity();
        buf.insertExpression(exprPos, callCombiner);

        // 插入新参数
        int argPos = pos + 1;  // 跳过结果参数
        for (Name newParam : newParams) {
            buf.insertParameter(argPos++, newParam);
        }
        assert(buf.lastIndexOf(callCombiner) == exprPos+newParams.length);
        buf.replaceParameterByCopy(pos, exprPos+newParams.length);

        form = buf.endEdit();
        return putInCache(key, form);
    }

    LambdaForm filterArgumentForm(int pos, BasicType newType) {
        Transform key = Transform.of(Transform.Kind.FILTER_ARG, pos, newType.ordinal());
        LambdaForm form = getInCache(key);
        if (form != null) {
            assert(form.arity == lambdaForm.arity);
            assert(form.parameterType(pos) == newType);
            return form;
        }

        BasicType oldType = lambdaForm.parameterType(pos);
        MethodType filterType = MethodType.methodType(oldType.basicTypeClass(),
                newType.basicTypeClass());
        form = makeArgumentCombinationForm(pos, filterType, false, false);
        return putInCache(key, form);
    }

    private LambdaForm makeArgumentCombinationForm(int pos,
                                                   MethodType combinerType,
                                                   boolean keepArguments, boolean dropResult) {
        LambdaFormBuffer buf = buffer();
        buf.startEdit();
        int combinerArity = combinerType.parameterCount();
        int resultArity = (dropResult ? 0 : 1);

        assert(pos <= MethodType.MAX_JVM_ARITY);
        assert(pos + resultArity + (keepArguments ? combinerArity : 0) <= lambdaForm.arity);
        assert(pos > 0);  // 不能过滤 MH 参数本身
        assert(combinerType == combinerType.basicType());
        assert(combinerType.returnType() != void.class || dropResult);

        BoundMethodHandle.SpeciesData oldData = oldSpeciesData();
        BoundMethodHandle.SpeciesData newData = newSpeciesData(L_TYPE);

        // 新创建的 LF 将使用不同的 BMH 运行。
        // 将任何现有的 BMH 字段引用切换到新的 BMH 类。
        Name oldBaseAddress = lambdaForm.parameter(0);  // 持有值的 BMH
        buf.replaceFunctions(oldData.getterFunctions(), newData.getterFunctions(), oldBaseAddress);
        Name newBaseAddress = oldBaseAddress.withConstraint(newData);
        buf.renameParameter(0, newBaseAddress);


                    Name getCombiner = new Name(newData.getterFunction(oldData.fieldCount()), newBaseAddress);
        Object[] combinerArgs = new Object[1 + combinerArity];
        combinerArgs[0] = getCombiner;
        Name[] newParams;
        if (keepArguments) {
            newParams = new Name[0];
            System.arraycopy(lambdaForm.names, pos + resultArity,
                             combinerArgs, 1, combinerArity);
        } else {
            newParams = new Name[combinerArity];
            BasicType[] newTypes = basicTypes(combinerType.parameterList());
            for (int i = 0; i < newTypes.length; i++) {
                newParams[i] = new Name(pos + i, newTypes[i]);
            }
            System.arraycopy(newParams, 0,
                             combinerArgs, 1, combinerArity);
        }
        Name callCombiner = new Name(combinerType, combinerArgs);

        // 插入两个新的表达式
        int exprPos = lambdaForm.arity();
        buf.insertExpression(exprPos+0, getCombiner);
        buf.insertExpression(exprPos+1, callCombiner);

        // 如果需要，插入新的参数
        int argPos = pos + resultArity;  // 跳过结果参数
        for (Name newParam : newParams) {
            buf.insertParameter(argPos++, newParam);
        }
        assert(buf.lastIndexOf(callCombiner) == exprPos+1+newParams.length);
        if (!dropResult) {
            buf.replaceParameterByCopy(pos, exprPos+1+newParams.length);
        }

        return buf.endEdit();
    }

    LambdaForm filterReturnForm(BasicType newType, boolean constantZero) {
        Transform.Kind kind = (constantZero ? Transform.Kind.FILTER_RETURN_TO_ZERO : Transform.Kind.FILTER_RETURN);
        Transform key = Transform.of(kind, newType.ordinal());
        LambdaForm form = getInCache(key);
        if (form != null) {
            assert(form.arity == lambdaForm.arity);
            assert(form.returnType() == newType);
            return form;
        }
        LambdaFormBuffer buf = buffer();
        buf.startEdit();

        int insPos = lambdaForm.names.length;
        Name callFilter;
        if (constantZero) {
            // 为给定类型合成一个常量零值。
            if (newType == V_TYPE)
                callFilter = null;
            else
                callFilter = new Name(constantZero(newType));
        } else {
            BoundMethodHandle.SpeciesData oldData = oldSpeciesData();
            BoundMethodHandle.SpeciesData newData = newSpeciesData(L_TYPE);

            // 新创建的LF将使用不同的BMH运行。
            // 将任何现有的BMH字段引用切换到新的BMH类。
            Name oldBaseAddress = lambdaForm.parameter(0);  // 持有值的BMH
            buf.replaceFunctions(oldData.getterFunctions(), newData.getterFunctions(), oldBaseAddress);
            Name newBaseAddress = oldBaseAddress.withConstraint(newData);
            buf.renameParameter(0, newBaseAddress);

            Name getFilter = new Name(newData.getterFunction(oldData.fieldCount()), newBaseAddress);
            buf.insertExpression(insPos++, getFilter);
            BasicType oldType = lambdaForm.returnType();
            if (oldType == V_TYPE) {
                MethodType filterType = MethodType.methodType(newType.basicTypeClass());
                callFilter = new Name(filterType, getFilter);
            } else {
                MethodType filterType = MethodType.methodType(newType.basicTypeClass(), oldType.basicTypeClass());
                callFilter = new Name(filterType, getFilter, lambdaForm.names[lambdaForm.result]);
            }
        }

        if (callFilter != null)
            buf.insertExpression(insPos++, callFilter);
        buf.setResult(callFilter);

        form = buf.endEdit();
        return putInCache(key, form);
    }

    LambdaForm foldArgumentsForm(int foldPos, boolean dropResult, MethodType combinerType) {
        int combinerArity = combinerType.parameterCount();
        Transform.Kind kind = (dropResult ? Transform.Kind.FOLD_ARGS_TO_VOID : Transform.Kind.FOLD_ARGS);
        Transform key = Transform.of(kind, foldPos, combinerArity);
        LambdaForm form = getInCache(key);
        if (form != null) {
            assert(form.arity == lambdaForm.arity - (kind == Transform.Kind.FOLD_ARGS ? 1 : 0));
            return form;
        }
        form = makeArgumentCombinationForm(foldPos, combinerType, true, dropResult);
        return putInCache(key, form);
    }

    LambdaForm permuteArgumentsForm(int skip, int[] reorder) {
        assert(skip == 1);  // 仅跳过前导MH参数，names[0]
        int length = lambdaForm.names.length;
        int outArgs = reorder.length;
        int inTypes = 0;
        boolean nullPerm = true;
        for (int i = 0; i < reorder.length; i++) {
            int inArg = reorder[i];
            if (inArg != i)  nullPerm = false;
            inTypes = Math.max(inTypes, inArg+1);
        }
        assert(skip + reorder.length == lambdaForm.arity);
        if (nullPerm)  return lambdaForm;  // 不缓存
        Transform key = Transform.of(Transform.Kind.PERMUTE_ARGS, reorder);
        LambdaForm form = getInCache(key);
        if (form != null) {
            assert(form.arity == skip+inTypes) : form;
            return form;
        }

        BasicType[] types = new BasicType[inTypes];
        for (int i = 0; i < outArgs; i++) {
            int inArg = reorder[i];
            types[inArg] = lambdaForm.names[skip + i].type;
        }
        assert (skip + outArgs == lambdaForm.arity);
        assert (permutedTypesMatch(reorder, types, lambdaForm.names, skip));
        int pos = 0;
        while (pos < outArgs && reorder[pos] == pos) {
            pos += 1;
        }
        Name[] names2 = new Name[length - outArgs + inTypes];
        System.arraycopy(lambdaForm.names, 0, names2, 0, skip + pos);
        int bodyLength = length - lambdaForm.arity;
        System.arraycopy(lambdaForm.names, skip + outArgs, names2, skip + inTypes, bodyLength);
        int arity2 = names2.length - bodyLength;
        int result2 = lambdaForm.result;
        if (result2 >= skip) {
            if (result2 < skip + outArgs) {
                result2 = reorder[result2 - skip] + skip;
            } else {
                result2 = result2 - outArgs + inTypes;
            }
        }
        for (int j = pos; j < outArgs; j++) {
            Name n = lambdaForm.names[skip + j];
            int i = reorder[j];
            Name n2 = names2[skip + i];
            if (n2 == null) {
                names2[skip + i] = n2 = new Name(types[i]);
            } else {
                assert (n2.type == types[i]);
            }
            for (int k = arity2; k < names2.length; k++) {
                names2[k] = names2[k].replaceName(n, n2);
            }
        }
        for (int i = skip + pos; i < arity2; i++) {
            if (names2[i] == null) {
                names2[i] = argument(i, types[i - skip]);
            }
        }
        for (int j = lambdaForm.arity; j < lambdaForm.names.length; j++) {
            int i = j - lambdaForm.arity + arity2;
            Name n = lambdaForm.names[j];
            Name n2 = names2[i];
            if (n != n2) {
                for (int k = i + 1; k < names2.length; k++) {
                    names2[k] = names2[k].replaceName(n, n2);
                }
            }
        }


                    form = new LambdaForm(lambdaForm.debugName, arity2, names2, result2);
        return putInCache(key, form);
    }

    // 检查重排序后的类型是否与给定的类型匹配
    static boolean permutedTypesMatch(int[] reorder, BasicType[] types, Name[] names, int skip) {
        for (int i = 0; i < reorder.length; i++) {
            assert (names[skip + i].isParam());
            assert (names[skip + i].type == types[reorder[i]]);
        }
        return true;
    }
}
