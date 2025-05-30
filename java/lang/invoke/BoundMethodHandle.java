
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

import static jdk.internal.org.objectweb.asm.Opcodes.*;
import static java.lang.invoke.LambdaForm.*;
import static java.lang.invoke.LambdaForm.BasicType.*;
import static java.lang.invoke.MethodHandleStatics.*;

import java.lang.invoke.LambdaForm.NamedFunction;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Function;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import jdk.internal.org.objectweb.asm.FieldVisitor;
import sun.invoke.util.ValueConversions;
import sun.invoke.util.Wrapper;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;

/**
 * 模拟调用指令的特定参数的方法句柄。JVM 在创建句柄时调度到正确的方法，而不是在调用时。
 *
 * 所有绑定的参数都封装在专用的物种中。
 */
/*non-public*/ abstract class BoundMethodHandle extends MethodHandle {

    /*non-public*/ BoundMethodHandle(MethodType type, LambdaForm form) {
        super(type, form);
        assert(speciesData() == speciesData(form));
    }

    //
    // BMH API 和内部
    //

    static BoundMethodHandle bindSingle(MethodType type, LambdaForm form, BasicType xtype, Object x) {
        // 对于某些类型签名，存在预定义的具体 BMH 类
        try {
            switch (xtype) {
            case L_TYPE:
                return bindSingle(type, form, x);  // 使用已知的快速路径。
            case I_TYPE:
                return (BoundMethodHandle) SpeciesData.EMPTY.extendWith(I_TYPE).constructor().invokeBasic(type, form, ValueConversions.widenSubword(x));
            case J_TYPE:
                return (BoundMethodHandle) SpeciesData.EMPTY.extendWith(J_TYPE).constructor().invokeBasic(type, form, (long) x);
            case F_TYPE:
                return (BoundMethodHandle) SpeciesData.EMPTY.extendWith(F_TYPE).constructor().invokeBasic(type, form, (float) x);
            case D_TYPE:
                return (BoundMethodHandle) SpeciesData.EMPTY.extendWith(D_TYPE).constructor().invokeBasic(type, form, (double) x);
            default : throw newInternalError("unexpected xtype: " + xtype);
            }
        } catch (Throwable t) {
            throw newInternalError(t);
        }
    }

    /*non-public*/
    LambdaFormEditor editor() {
        return form.editor();
    }

    static BoundMethodHandle bindSingle(MethodType type, LambdaForm form, Object x) {
        return Species_L.make(type, form, x);
    }

    @Override // 超类中有一个默认的绑定器，仅适用于 'L' 类型
    /*non-public*/
    BoundMethodHandle bindArgumentL(int pos, Object value) {
        return editor().bindArgumentL(this, pos, value);
    }
    /*non-public*/
    BoundMethodHandle bindArgumentI(int pos, int value) {
        return editor().bindArgumentI(this, pos, value);
    }
    /*non-public*/
    BoundMethodHandle bindArgumentJ(int pos, long value) {
        return editor().bindArgumentJ(this, pos, value);
    }
    /*non-public*/
    BoundMethodHandle bindArgumentF(int pos, float value) {
        return editor().bindArgumentF(this, pos, value);
    }
    /*non-public*/
    BoundMethodHandle bindArgumentD(int pos, double value) {
        return editor().bindArgumentD(this, pos, value);
    }

    @Override
    BoundMethodHandle rebind() {
        if (!tooComplex()) {
            return this;
        }
        return makeReinvoker(this);
    }

    private boolean tooComplex() {
        return (fieldCount() > FIELD_COUNT_THRESHOLD ||
                form.expressionCount() > FORM_EXPRESSION_THRESHOLD);
    }
    private static final int FIELD_COUNT_THRESHOLD = 12;      // 最大方便的 BMH 字段数
    private static final int FORM_EXPRESSION_THRESHOLD = 24;  // 最大方便的 BMH 表达式数

    /**
     * 重新调用 MH 的形式如下：
     * {@code lambda (bmh, arg*) { thismh = bmh[0]; invokeBasic(thismh, arg*) }}
     */
    static BoundMethodHandle makeReinvoker(MethodHandle target) {
        LambdaForm form = DelegatingMethodHandle.makeReinvokerForm(
                target, MethodTypeForm.LF_REBIND,
                Species_L.SPECIES_DATA, Species_L.SPECIES_DATA.getterFunction(0));
        return Species_L.make(target.type(), form, target);
    }

    /**
     * 返回表示此 BMH 物种的 {@link SpeciesData} 实例。所有子类都必须提供一个包含此值的静态字段，并且必须相应地实现此方法。
     */
    /*non-public*/ abstract SpeciesData speciesData();

    /*non-public*/ static SpeciesData speciesData(LambdaForm form) {
        Object c = form.names[0].constraint;
        if (c instanceof SpeciesData)
            return (SpeciesData) c;
        // 如果没有 BMH 约束，则使用空约束
        return SpeciesData.EMPTY;
    }

    /**
     * 返回此 BMH 中的字段数。等同于 speciesData().fieldCount()。
     */
    /*non-public*/ abstract int fieldCount();

    @Override
    Object internalProperties() {
        return "\n& BMH="+internalValues();
    }

    @Override
    final Object internalValues() {
        Object[] boundValues = new Object[speciesData().fieldCount()];
        for (int i = 0; i < boundValues.length; ++i) {
            boundValues[i] = arg(i);
        }
        return Arrays.asList(boundValues);
    }

    /*non-public*/ final Object arg(int i) {
        try {
            switch (speciesData().fieldType(i)) {
            case L_TYPE: return          speciesData().getters[i].invokeBasic(this);
            case I_TYPE: return (int)    speciesData().getters[i].invokeBasic(this);
            case J_TYPE: return (long)   speciesData().getters[i].invokeBasic(this);
            case F_TYPE: return (float)  speciesData().getters[i].invokeBasic(this);
            case D_TYPE: return (double) speciesData().getters[i].invokeBasic(this);
            }
        } catch (Throwable ex) {
            throw newInternalError(ex);
        }
        throw new InternalError("unexpected type: " + speciesData().typeChars+"."+i);
    }

    //
    // 克隆 API
    //

    /*non-public*/ abstract BoundMethodHandle copyWith(MethodType mt, LambdaForm lf);
    /*non-public*/ abstract BoundMethodHandle copyWithExtendL(MethodType mt, LambdaForm lf, Object narg);
    /*non-public*/ abstract BoundMethodHandle copyWithExtendI(MethodType mt, LambdaForm lf, int    narg);
    /*non-public*/ abstract BoundMethodHandle copyWithExtendJ(MethodType mt, LambdaForm lf, long   narg);
    /*non-public*/ abstract BoundMethodHandle copyWithExtendF(MethodType mt, LambdaForm lf, float  narg);
    /*non-public*/ abstract BoundMethodHandle copyWithExtendD(MethodType mt, LambdaForm lf, double narg);

    //
    // 必须关闭引导循环的具体 BMH 类
    //

    private  // 使其私有以强制用户首先访问封闭类
    static final class Species_L extends BoundMethodHandle {
        final Object argL0;
        private Species_L(MethodType mt, LambdaForm lf, Object argL0) {
            super(mt, lf);
            this.argL0 = argL0;
        }
        @Override
        /*non-public*/ SpeciesData speciesData() {
            return SPECIES_DATA;
        }
        @Override
        /*non-public*/ int fieldCount() {
            return 1;
        }
        /*non-public*/ static final SpeciesData SPECIES_DATA = new SpeciesData("L", Species_L.class);
        /*non-public*/ static BoundMethodHandle make(MethodType mt, LambdaForm lf, Object argL0) {
            return new Species_L(mt, lf, argL0);
        }
        @Override
        /*non-public*/ final BoundMethodHandle copyWith(MethodType mt, LambdaForm lf) {
            return new Species_L(mt, lf, argL0);
        }
        @Override
        /*non-public*/ final BoundMethodHandle copyWithExtendL(MethodType mt, LambdaForm lf, Object narg) {
            try {
                return (BoundMethodHandle) SPECIES_DATA.extendWith(L_TYPE).constructor().invokeBasic(mt, lf, argL0, narg);
            } catch (Throwable ex) {
                throw uncaughtException(ex);
            }
        }
        @Override
        /*non-public*/ final BoundMethodHandle copyWithExtendI(MethodType mt, LambdaForm lf, int narg) {
            try {
                return (BoundMethodHandle) SPECIES_DATA.extendWith(I_TYPE).constructor().invokeBasic(mt, lf, argL0, narg);
            } catch (Throwable ex) {
                throw uncaughtException(ex);
            }
        }
        @Override
        /*non-public*/ final BoundMethodHandle copyWithExtendJ(MethodType mt, LambdaForm lf, long narg) {
            try {
                return (BoundMethodHandle) SPECIES_DATA.extendWith(J_TYPE).constructor().invokeBasic(mt, lf, argL0, narg);
            } catch (Throwable ex) {
                throw uncaughtException(ex);
            }
        }
        @Override
        /*non-public*/ final BoundMethodHandle copyWithExtendF(MethodType mt, LambdaForm lf, float narg) {
            try {
                return (BoundMethodHandle) SPECIES_DATA.extendWith(F_TYPE).constructor().invokeBasic(mt, lf, argL0, narg);
            } catch (Throwable ex) {
                throw uncaughtException(ex);
            }
        }
        @Override
        /*non-public*/ final BoundMethodHandle copyWithExtendD(MethodType mt, LambdaForm lf, double narg) {
            try {
                return (BoundMethodHandle) SPECIES_DATA.extendWith(D_TYPE).constructor().invokeBasic(mt, lf, argL0, narg);
            } catch (Throwable ex) {
                throw uncaughtException(ex);
            }
        }
    }

    //
    // BMH 物种元数据
    //

    /**
     * 具体 BMH 类型的元数据包装器。
     * 每个 BMH 类型对应于给定的基本字段类型序列（LIJFD）。
     * 字段是不可变的；它们的值在对象构造时完全指定。
     * 每个 BMH 类型提供一个 getter 函数数组，这些函数可以在 lambda 表达式中使用。
     * BMH 通过克隆较短的 BMH 并添加一个或多个新的字段值来构造。
     * 最短的 BMH 没有字段；其类为 SimpleMethodHandle。
     * BMH 物种之间没有子类型关系，即使看起来较短的 BMH 可以作为较长的 BMH 的超类型。
     */
    static class SpeciesData {
        private final String                             typeChars;
        private final BasicType[]                        typeCodes;
        private final Class<? extends BoundMethodHandle> clazz;
        // 引导需要循环关系 MH -> BMH -> SpeciesData -> MH
        // 因此，我们需要链中的一个非最终链接。使用数组元素。
        @Stable private final MethodHandle[]             constructor;
        @Stable private final MethodHandle[]             getters;
        @Stable private final NamedFunction[]            nominalGetters;
        @Stable private final SpeciesData[]              extensions;

        /*non-public*/ int fieldCount() {
            return typeCodes.length;
        }
        /*non-public*/ BasicType fieldType(int i) {
            return typeCodes[i];
        }
        /*non-public*/ char fieldTypeChar(int i) {
            return typeChars.charAt(i);
        }
        Object fieldSignature() {
            return typeChars;
        }
        public Class<? extends BoundMethodHandle> fieldHolder() {
            return clazz;
        }
        public String toString() {
            return "SpeciesData<"+fieldSignature()+">";
        }

        /**
         * 返回包含一个 {@link LambdaForm.NamedFunction} 的 {@link LambdaForm.Name}，该函数表示绑定到通用调用器的 MH，该调用器又转发到相应的 getter。
         */
        NamedFunction getterFunction(int i) {
            NamedFunction nf = nominalGetters[i];
            assert(nf.memberDeclaringClassOrNull() == fieldHolder());
            assert(nf.returnType() == fieldType(i));
            return nf;
        }

        NamedFunction[] getterFunctions() {
            return nominalGetters;
        }

        MethodHandle[] getterHandles() { return getters; }

        MethodHandle constructor() {
            return constructor[0];
        }

        static final SpeciesData EMPTY = new SpeciesData("", BoundMethodHandle.class);

        SpeciesData(String types, Class<? extends BoundMethodHandle> clazz) {
            this.typeChars = types;
            this.typeCodes = basicTypes(types);
            this.clazz = clazz;
            if (!INIT_DONE) {
                this.constructor = new MethodHandle[1];  // 只有一个构造函数
                this.getters = new MethodHandle[types.length()];
                this.nominalGetters = new NamedFunction[types.length()];
            } else {
                this.constructor = Factory.makeCtors(clazz, types, null);
                this.getters = Factory.makeGetters(clazz, types, null);
                this.nominalGetters = Factory.makeNominalGetters(types, null, this.getters);
            }
            this.extensions = new SpeciesData[ARG_TYPE_LIMIT];
        }

        private void initForBootstrap() {
            assert(!INIT_DONE);
            if (constructor() == null) {
                String types = typeChars;
                CACHE.put(types, this);
                Factory.makeCtors(clazz, types, this.constructor);
                Factory.makeGetters(clazz, types, this.getters);
                Factory.makeNominalGetters(types, this.nominalGetters, this.getters);
            }
        }

        private static final ConcurrentMap<String, SpeciesData> CACHE = new ConcurrentHashMap<>();
        private static final boolean INIT_DONE;  // 在 <clinit> 完成后设置...

        SpeciesData extendWith(byte type) {
            return extendWith(BasicType.basicType(type));
        }

        SpeciesData extendWith(BasicType type) {
            int ord = type.ordinal();
            SpeciesData d = extensions[ord];
            if (d != null)  return d;
            extensions[ord] = d = get(typeChars+type.basicTypeChar());
            return d;
        }


                    private static SpeciesData get(String types) {
            return CACHE.computeIfAbsent(types, new Function<String, SpeciesData>() {
                @Override
                public SpeciesData apply(String types) {
                    Class<? extends BoundMethodHandle> bmhcl = Factory.getConcreteBMHClass(types);
                    // 物种数据实例化可能会因为代码缓存溢出而抛出 VirtualMachineError...
                    SpeciesData speciesData = new SpeciesData(types, bmhcl);
                    // CHM.computeIfAbsent 确保只有在具体 BMH 类上设置一个 SpeciesData 实例时才会成功
                    Factory.setSpeciesDataToConcreteBMHClass(bmhcl, speciesData);
                    // 具体的 BMH 类仅在 SPECIES_DATA 字段设置后通过 SpeciesData 实例发布
                    return speciesData;
                }
            });
        }

        /**
         * 当启用断言时调用此方法。它检查是否已将所有静态定义的 BoundMethodHandle 子类的 SpeciesData 添加到 SpeciesData 缓存中。参见下面的静态初始化器
         */
        static boolean speciesDataCachePopulated() {
            Class<BoundMethodHandle> rootCls = BoundMethodHandle.class;
            try {
                for (Class<?> c : rootCls.getDeclaredClasses()) {
                    if (rootCls.isAssignableFrom(c)) {
                        final Class<? extends BoundMethodHandle> cbmh = c.asSubclass(BoundMethodHandle.class);
                        SpeciesData d = Factory.getSpeciesDataFromConcreteBMHClass(cbmh);
                        assert(d != null) : cbmh.getName();
                        assert(d.clazz == cbmh);
                        assert(CACHE.get(d.typeChars) == d);
                    }
                }
            } catch (Throwable e) {
                throw newInternalError(e);
            }
            return true;
        }

        static {
            // 预填充 BMH 物种数据缓存，包括 EMPTY 和所有 BMH 的内部子类。
            EMPTY.initForBootstrap();
            Species_L.SPECIES_DATA.initForBootstrap();
            // 检查所有静态 SpeciesData 实例是否已初始化
            assert speciesDataCachePopulated();
            // 注意：不要简化此操作，因为在引导过程中 INIT_DONE 不能是编译时常量。
            INIT_DONE = Boolean.TRUE;
        }
    }

    static SpeciesData getSpeciesData(String types) {
        return SpeciesData.get(types);
    }

    /**
     * 具体 BMH 类的生成。
     *
     * 具体的 BMH 物种适合绑定符合给定类型模式的值。引用类型被擦除。
     *
     * BMH 物种按类型模式缓存。
     *
     * 具体的 BMH 物种具有绑定值的具体（可能是擦除的）类型字段。BMH 中提供了设置器作为 API。获取器作为 MH 暴露，可以包含在 lambda 形式中。
     */
    static class Factory {

        static final String JLO_SIG  = "Ljava/lang/Object;";
        static final String JLS_SIG  = "Ljava/lang/String;";
        static final String JLC_SIG  = "Ljava/lang/Class;";
        static final String MH       = "java/lang/invoke/MethodHandle";
        static final String MH_SIG   = "L"+MH+";";
        static final String BMH      = "java/lang/invoke/BoundMethodHandle";
        static final String BMH_SIG  = "L"+BMH+";";
        static final String SPECIES_DATA     = "java/lang/invoke/BoundMethodHandle$SpeciesData";
        static final String SPECIES_DATA_SIG = "L"+SPECIES_DATA+";";
        static final String STABLE_SIG       = "Ljava/lang/invoke/Stable;";

        static final String SPECIES_PREFIX_NAME = "Species_";
        static final String SPECIES_PREFIX_PATH = BMH + "$" + SPECIES_PREFIX_NAME;

        static final String BMHSPECIES_DATA_EWI_SIG = "(B)" + SPECIES_DATA_SIG;
        static final String BMHSPECIES_DATA_GFC_SIG = "(" + JLS_SIG + JLC_SIG + ")" + SPECIES_DATA_SIG;
        static final String MYSPECIES_DATA_SIG = "()" + SPECIES_DATA_SIG;
        static final String VOID_SIG   = "()V";
        static final String INT_SIG    = "()I";

        static final String SIG_INCIPIT = "(Ljava/lang/invoke/MethodType;Ljava/lang/invoke/LambdaForm;";

        static final String[] E_THROWABLE = new String[] { "java/lang/Throwable" };

        static final ConcurrentMap<String, Class<? extends BoundMethodHandle>> CLASS_CACHE = new ConcurrentHashMap<>();

        /**
         * 获取给定绑定类型组合的具体 BMH 子类。
         *
         * @param types 类型签名，其中引用类型被擦除为 'L'
         * @return 具体的 BMH 类
         */
        static Class<? extends BoundMethodHandle> getConcreteBMHClass(String types) {
            // CHM.computeIfAbsent 确保 generateConcreteBMHClass 仅在每个键上被调用一次。
            return CLASS_CACHE.computeIfAbsent(
                types, new Function<String, Class<? extends BoundMethodHandle>>() {
                    @Override
                    public Class<? extends BoundMethodHandle> apply(String types) {
                        return generateConcreteBMHClass(types);
                    }
                });
        }

        /**
         * 生成给定绑定类型组合的具体 BMH 子类。
         *
         * 具体的 BMH 物种遵循以下模式：
         *
         * <pre>
         * class Species_[[types]] extends BoundMethodHandle {
         *     [[fields]]
         *     final SpeciesData speciesData() { return SpeciesData.get("[[types]]"); }
         * }
         * </pre>
         *
         * {@code [[types]]} 签名正是传递给此方法的字符串。
         *
         * {@code [[fields]]} 部分由类型签名中的每个字符的一个字段定义组成，遵循 {@link #makeFieldName} 中描述的命名模式。
         *
         * 例如，具有两个引用类型和一个整数绑定值的具体 BMH 物种将具有以下形状：
         *
         * <pre>
         * class BoundMethodHandle { ... private static
         * final class Species_LLI extends BoundMethodHandle {
         *     final Object argL0;
         *     final Object argL1;
         *     final int argI2;
         *     private Species_LLI(MethodType mt, LambdaForm lf, Object argL0, Object argL1, int argI2) {
         *         super(mt, lf);
         *         this.argL0 = argL0;
         *         this.argL1 = argL1;
         *         this.argI2 = argI2;
         *     }
         *     final SpeciesData speciesData() { return SPECIES_DATA; }
         *     final int fieldCount() { return 3; }
         *     &#64;Stable static SpeciesData SPECIES_DATA; // 之后注入
         *     static BoundMethodHandle make(MethodType mt, LambdaForm lf, Object argL0, Object argL1, int argI2) {
         *         return new Species_LLI(mt, lf, argL0, argL1, argI2);
         *     }
         *     final BoundMethodHandle copyWith(MethodType mt, LambdaForm lf) {
         *         return new Species_LLI(mt, lf, argL0, argL1, argI2);
         *     }
         *     final BoundMethodHandle copyWithExtendL(MethodType mt, LambdaForm lf, Object narg) {
         *         return SPECIES_DATA.extendWith(L_TYPE).constructor().invokeBasic(mt, lf, argL0, argL1, argI2, narg);
         *     }
         *     final BoundMethodHandle copyWithExtendI(MethodType mt, LambdaForm lf, int narg) {
         *         return SPECIES_DATA.extendWith(I_TYPE).constructor().invokeBasic(mt, lf, argL0, argL1, argI2, narg);
         *     }
         *     final BoundMethodHandle copyWithExtendJ(MethodType mt, LambdaForm lf, long narg) {
         *         return SPECIES_DATA.extendWith(J_TYPE).constructor().invokeBasic(mt, lf, argL0, argL1, argI2, narg);
         *     }
         *     final BoundMethodHandle copyWithExtendF(MethodType mt, LambdaForm lf, float narg) {
         *         return SPECIES_DATA.extendWith(F_TYPE).constructor().invokeBasic(mt, lf, argL0, argL1, argI2, narg);
         *     }
         *     public final BoundMethodHandle copyWithExtendD(MethodType mt, LambdaForm lf, double narg) {
         *         return SPECIES_DATA.extendWith(D_TYPE).constructor().invokeBasic(mt, lf, argL0, argL1, argI2, narg);
         *     }
         * }
         * </pre>
         *
         * @param types 类型签名，其中引用类型被擦除为 'L'
         * @return 生成的具体 BMH 类
         */
        static Class<? extends BoundMethodHandle> generateConcreteBMHClass(String types) {
            final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);

            String shortTypes = LambdaForm.shortenSignature(types);
            final String className  = SPECIES_PREFIX_PATH + shortTypes;
            final String sourceFile = SPECIES_PREFIX_NAME + shortTypes;
            final int NOT_ACC_PUBLIC = 0;  // 不是 ACC_PUBLIC
            cw.visit(V1_6, NOT_ACC_PUBLIC + ACC_FINAL + ACC_SUPER, className, null, BMH, null);
            cw.visitSource(sourceFile, null);

            // 发射静态类型和 SPECIES_DATA 字段
            FieldVisitor fw = cw.visitField(NOT_ACC_PUBLIC + ACC_STATIC, "SPECIES_DATA", SPECIES_DATA_SIG, null, null);
            fw.visitAnnotation(STABLE_SIG, true);
            fw.visitEnd();

            // 发射绑定参数字段
            for (int i = 0; i < types.length(); ++i) {
                final char t = types.charAt(i);
                final String fieldName = makeFieldName(types, i);
                final String fieldDesc = t == 'L' ? JLO_SIG : String.valueOf(t);
                cw.visitField(ACC_FINAL, fieldName, fieldDesc, null, null).visitEnd();
            }

            MethodVisitor mv;

            // 发射构造函数
            mv = cw.visitMethod(ACC_PRIVATE, "<init>", makeSignature(types, true), null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0); // this
            mv.visitVarInsn(ALOAD, 1); // type
            mv.visitVarInsn(ALOAD, 2); // form

            mv.visitMethodInsn(INVOKESPECIAL, BMH, "<init>", makeSignature("", true), false);

            for (int i = 0, j = 0; i < types.length(); ++i, ++j) {
                // i 计算参数，j 计算对应的参数槽
                char t = types.charAt(i);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(typeLoadOp(t), j + 3); // 参数从 3 开始
                mv.visitFieldInsn(PUTFIELD, className, makeFieldName(types, i), typeSig(t));
                if (t == 'J' || t == 'D') {
                    ++j; // 调整参数寄存器访问
                }
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            // 发射 speciesData() 的实现
            mv = cw.visitMethod(NOT_ACC_PUBLIC + ACC_FINAL, "speciesData", MYSPECIES_DATA_SIG, null, null);
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC, className, "SPECIES_DATA", SPECIES_DATA_SIG);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            // 发射 fieldCount() 的实现
            mv = cw.visitMethod(NOT_ACC_PUBLIC + ACC_FINAL, "fieldCount", INT_SIG, null, null);
            mv.visitCode();
            int fc = types.length();
            if (fc <= (ICONST_5 - ICONST_0)) {
                mv.visitInsn(ICONST_0 + fc);
            } else {
                mv.visitIntInsn(SIPUSH, fc);
            }
            mv.visitInsn(IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            // 发射 make() ... 包装构造函数的工厂方法
            mv = cw.visitMethod(NOT_ACC_PUBLIC + ACC_STATIC, "make", makeSignature(types, false), null, null);
            mv.visitCode();
            // 创建实例
            mv.visitTypeInsn(NEW, className);
            mv.visitInsn(DUP);
            // 加载 mt, lf
            mv.visitVarInsn(ALOAD, 0);  // type
            mv.visitVarInsn(ALOAD, 1);  // form
            // 加载工厂方法参数
            for (int i = 0, j = 0; i < types.length(); ++i, ++j) {
                // i 计算参数，j 计算对应的参数槽
                char t = types.charAt(i);
                mv.visitVarInsn(typeLoadOp(t), j + 2); // 参数从 3 开始
                if (t == 'J' || t == 'D') {
                    ++j; // 调整参数寄存器访问
                }
            }

            // 最后，调用构造函数并返回
            mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", makeSignature(types, true), false);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            // 发射 copyWith()
            mv = cw.visitMethod(NOT_ACC_PUBLIC + ACC_FINAL, "copyWith", makeSignature("", false), null, null);
            mv.visitCode();
            // 创建实例
            mv.visitTypeInsn(NEW, className);
            mv.visitInsn(DUP);
            // 加载 mt, lf
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            // 将字段推送到栈上
            emitPushFields(types, className, mv);
            // 最后，调用构造函数并返回
            mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", makeSignature(types, true), false);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            // 对每种类型，发射 copyWithExtendT()
            for (BasicType type : BasicType.ARG_TYPES) {
                int ord = type.ordinal();
                char btChar = type.basicTypeChar();
                mv = cw.visitMethod(NOT_ACC_PUBLIC + ACC_FINAL, "copyWithExtend" + btChar, makeSignature(String.valueOf(btChar), false), null, E_THROWABLE);
                mv.visitCode();
                // 返回 SPECIES_DATA.extendWith(t).constructor().invokeBasic(mt, lf, argL0, ..., narg)
                // 获取构造函数
                mv.visitFieldInsn(GETSTATIC, className, "SPECIES_DATA", SPECIES_DATA_SIG);
                int iconstInsn = ICONST_0 + ord;
                assert(iconstInsn <= ICONST_5);
                mv.visitInsn(iconstInsn);
                mv.visitMethodInsn(INVOKEVIRTUAL, SPECIES_DATA, "extendWith", BMHSPECIES_DATA_EWI_SIG, false);
                mv.visitMethodInsn(INVOKEVIRTUAL, SPECIES_DATA, "constructor", "()" + MH_SIG, false);
                // 加载 mt, lf
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 2);
                // 将字段推送到栈上
                emitPushFields(types, className, mv);
                // 将 narg 推送到栈上
                mv.visitVarInsn(typeLoadOp(btChar), 3);
                // 最后，调用构造函数并返回
                mv.visitMethodInsn(INVOKEVIRTUAL, MH, "invokeBasic", makeSignature(types + btChar, false), false);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }


                        cw.visitEnd();

            // 加载类
            final byte[] classFile = cw.toByteArray();
            InvokerBytecodeGenerator.maybeDump(className, classFile);
            Class<? extends BoundMethodHandle> bmhClass =
                //UNSAFE.defineAnonymousClass(BoundMethodHandle.class, classFile, null).asSubclass(BoundMethodHandle.class);
                UNSAFE.defineClass(className, classFile, 0, classFile.length,
                                   BoundMethodHandle.class.getClassLoader(), null)
                    .asSubclass(BoundMethodHandle.class);

            return bmhClass;
        }

        private static int typeLoadOp(char t) {
            switch (t) {
            case 'L': return ALOAD;
            case 'I': return ILOAD;
            case 'J': return LLOAD;
            case 'F': return FLOAD;
            case 'D': return DLOAD;
            default : throw newInternalError("未识别的类型 " + t);
            }
        }

        private static void emitPushFields(String types, String className, MethodVisitor mv) {
            for (int i = 0; i < types.length(); ++i) {
                char tc = types.charAt(i);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, makeFieldName(types, i), typeSig(tc));
            }
        }

        static String typeSig(char t) {
            return t == 'L' ? JLO_SIG : String.valueOf(t);
        }

        //
        // Getter MH 生成。
        //

        private static MethodHandle makeGetter(Class<?> cbmhClass, String types, int index) {
            String fieldName = makeFieldName(types, index);
            Class<?> fieldType = Wrapper.forBasicType(types.charAt(index)).primitiveType();
            try {
                return LOOKUP.findGetter(cbmhClass, fieldName, fieldType);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw newInternalError(e);
            }
        }

        static MethodHandle[] makeGetters(Class<?> cbmhClass, String types, MethodHandle[] mhs) {
            if (mhs == null)  mhs = new MethodHandle[types.length()];
            for (int i = 0; i < mhs.length; ++i) {
                mhs[i] = makeGetter(cbmhClass, types, i);
                assert(mhs[i].internalMemberName().getDeclaringClass() == cbmhClass);
            }
            return mhs;
        }

        static MethodHandle[] makeCtors(Class<? extends BoundMethodHandle> cbmh, String types, MethodHandle mhs[]) {
            if (mhs == null)  mhs = new MethodHandle[1];
            if (types.equals(""))  return mhs;  // 为空 BMH 类型的 hack
            mhs[0] = makeCbmhCtor(cbmh, types);
            return mhs;
        }

        static NamedFunction[] makeNominalGetters(String types, NamedFunction[] nfs, MethodHandle[] getters) {
            if (nfs == null)  nfs = new NamedFunction[types.length()];
            for (int i = 0; i < nfs.length; ++i) {
                nfs[i] = new NamedFunction(getters[i]);
            }
            return nfs;
        }

        //
        // 辅助方法。
        //

        static SpeciesData getSpeciesDataFromConcreteBMHClass(Class<? extends BoundMethodHandle> cbmh) {
            try {
                Field F_SPECIES_DATA = cbmh.getDeclaredField("SPECIES_DATA");
                return (SpeciesData) F_SPECIES_DATA.get(null);
            } catch (ReflectiveOperationException ex) {
                throw newInternalError(ex);
            }
        }

        static void setSpeciesDataToConcreteBMHClass(Class<? extends BoundMethodHandle> cbmh, SpeciesData speciesData) {
            try {
                Field F_SPECIES_DATA = cbmh.getDeclaredField("SPECIES_DATA");
                assert F_SPECIES_DATA.getDeclaredAnnotation(Stable.class) != null;
                F_SPECIES_DATA.set(null, speciesData);
            } catch (ReflectiveOperationException ex) {
                throw newInternalError(ex);
            }
        }

        /**
         * 具体 BMH 中的字段名称遵循此模式：
         * arg + 类型 + 索引
         * 其中类型是一个字符 (L, I, J, F, D)。
         */
        private static String makeFieldName(String types, int index) {
            assert index >= 0 && index < types.length();
            return "arg" + types.charAt(index) + index;
        }

        private static String makeSignature(String types, boolean ctor) {
            StringBuilder buf = new StringBuilder(SIG_INCIPIT);
            for (char c : types.toCharArray()) {
                buf.append(typeSig(c));
            }
            return buf.append(')').append(ctor ? "V" : BMH_SIG).toString();
        }

        static MethodHandle makeCbmhCtor(Class<? extends BoundMethodHandle> cbmh, String types) {
            try {
                return LOOKUP.findStatic(cbmh, "make", MethodType.fromMethodDescriptorString(makeSignature(types, false), null));
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | TypeNotPresentException e) {
                throw newInternalError(e);
            }
        }
    }

    private static final Lookup LOOKUP = Lookup.IMPL_LOOKUP;

    /**
     * 所有子类都必须提供一个值来描述它们的类型签名。
     */
    static final SpeciesData SPECIES_DATA = SpeciesData.EMPTY;

    private static final SpeciesData[] SPECIES_DATA_CACHE = new SpeciesData[5];
    private static SpeciesData checkCache(int size, String types) {
        int idx = size - 1;
        SpeciesData data = SPECIES_DATA_CACHE[idx];
        if (data != null)  return data;
        SPECIES_DATA_CACHE[idx] = data = getSpeciesData(types);
        return data;
    }
    static SpeciesData speciesData_L()     { return checkCache(1, "L"); }
    static SpeciesData speciesData_LL()    { return checkCache(2, "LL"); }
    static SpeciesData speciesData_LLL()   { return checkCache(3, "LLL"); }
    static SpeciesData speciesData_LLLL()  { return checkCache(4, "LLLL"); }
    static SpeciesData speciesData_LLLLL() { return checkCache(5, "LLLLL"); }
}
