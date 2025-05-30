
/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.org.objectweb.asm.*;
import sun.invoke.util.BytecodeDescriptor;
import sun.misc.Unsafe;
import sun.security.action.GetPropertyAction;

import java.io.FilePermission;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.PropertyPermission;
import java.util.Set;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

/**
 * Lambda 元工厂实现，动态创建每个 lambda 调用点的类似内部类的类。
 *
 * @see LambdaMetafactory
 */
/* package */ final class InnerClassLambdaMetafactory extends AbstractValidatingLambdaMetafactory {
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    private static final int CLASSFILE_VERSION = 52;
    private static final String METHOD_DESCRIPTOR_VOID = Type.getMethodDescriptor(Type.VOID_TYPE);
    private static final String JAVA_LANG_OBJECT = "java/lang/Object";
    private static final String NAME_CTOR = "<init>";
    private static final String NAME_FACTORY = "get$Lambda";

    // 序列化支持
    private static final String NAME_SERIALIZED_LAMBDA = "java/lang/invoke/SerializedLambda";
    private static final String NAME_NOT_SERIALIZABLE_EXCEPTION = "java/io/NotSerializableException";
    private static final String DESCR_METHOD_WRITE_REPLACE = "()Ljava/lang/Object;";
    private static final String DESCR_METHOD_WRITE_OBJECT = "(Ljava/io/ObjectOutputStream;)V";
    private static final String DESCR_METHOD_READ_OBJECT = "(Ljava/io/ObjectInputStream;)V";
    private static final String NAME_METHOD_WRITE_REPLACE = "writeReplace";
    private static final String NAME_METHOD_READ_OBJECT = "readObject";
    private static final String NAME_METHOD_WRITE_OBJECT = "writeObject";
    private static final String DESCR_CTOR_SERIALIZED_LAMBDA
            = MethodType.methodType(void.class,
                                    Class.class,
                                    String.class, String.class, String.class,
                                    int.class, String.class, String.class, String.class,
                                    String.class,
                                    Object[].class).toMethodDescriptorString();
    private static final String DESCR_CTOR_NOT_SERIALIZABLE_EXCEPTION
            = MethodType.methodType(void.class, String.class).toMethodDescriptorString();
    private static final String[] SER_HOSTILE_EXCEPTIONS = new String[] {NAME_NOT_SERIALIZABLE_EXCEPTION};


    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    // 用于确保每个生成的类名是唯一的
    private static final AtomicInteger counter = new AtomicInteger(0);

    // 用于将生成的类转储到磁盘，以便调试
    private static final ProxyClassesDumper dumper;

    static {
        final String key = "jdk.internal.lambda.dumpProxyClasses";
        String path = AccessController.doPrivileged(
                new GetPropertyAction(key), null,
                new PropertyPermission(key , "read"));
        dumper = (null == path) ? null : ProxyClassesDumper.getInstance(path);
    }

    // 请参阅 AbstractValidatingLambdaMetafactory 中的上下文值
    private final String implMethodClassName;        // 包含实现的类型的名称 "CC"
    private final String implMethodName;             // 实现方法的名称 "impl"
    private final String implMethodDesc;             // 实现方法的类型描述符 "(I)Ljava/lang/String;"
    private final Class<?> implMethodReturnClass;    // 实现方法返回类型的类 "Ljava/lang/String;"
    private final MethodType constructorType;        // 生成的类的构造函数类型 "(CC)void"
    private final ClassWriter cw;                    // ASM 类写入器
    private final String[] argNames;                 // 生成的构造函数参数名称
    private final String[] argDescs;                 // 构造函数参数的类型描述符
    private final String lambdaClassName;            // 生成的类的名称 "X$$Lambda$1"

    /**
     * 通用元工厂构造函数，支持标准情况，并允许不常见的选项，如序列化或桥接。
     *
     * @param caller 由 VM 自动堆叠；表示具有调用者访问权限的查找上下文。
     * @param invokedType 由 VM 自动堆叠；调用方法的签名，包括返回的 lambda 对象的预期静态类型和 lambda 的捕获参数的静态类型。如果实现方法是实例方法，则调用签名中的第一个参数将对应于接收者。
     * @param samMethodName 要转换为 lambda 或方法引用的功能接口方法的名称，表示为字符串。
     * @param samMethodType 要转换为 lambda 或方法引用的功能接口方法的类型，表示为 MethodType。
     * @param implMethod 当调用结果功能接口实例的方法时，应调用的实现方法（需要适当调整参数类型、返回类型和捕获参数）。
     * @param instantiatedMethodType 类型变量被从捕获站点替换后的主要功能接口方法的签名
     * @param isSerializable lambda 是否应可序列化？如果设置，则目标类型或附加的 SAM 类型之一必须扩展 {@code Serializable}。
     * @param markerInterfaces lambda 对象应实现的附加接口。
     * @param additionalBridges 需要桥接到实现方法的附加签名的方法类型
     * @throws LambdaConversionException 如果违反了任何元工厂协议不变量
     */
    public InnerClassLambdaMetafactory(MethodHandles.Lookup caller,
                                       MethodType invokedType,
                                       String samMethodName,
                                       MethodType samMethodType,
                                       MethodHandle implMethod,
                                       MethodType instantiatedMethodType,
                                       boolean isSerializable,
                                       Class<?>[] markerInterfaces,
                                       MethodType[] additionalBridges)
            throws LambdaConversionException {
        super(caller, invokedType, samMethodName, samMethodType,
              implMethod, instantiatedMethodType,
              isSerializable, markerInterfaces, additionalBridges);
        implMethodClassName = implDefiningClass.getName().replace('.', '/');
        implMethodName = implInfo.getName();
        implMethodDesc = implMethodType.toMethodDescriptorString();
        implMethodReturnClass = (implKind == MethodHandleInfo.REF_newInvokeSpecial)
                ? implDefiningClass
                : implMethodType.returnType();
        constructorType = invokedType.changeReturnType(Void.TYPE);
        lambdaClassName = targetClass.getName().replace('.', '/') + "$$Lambda$" + counter.incrementAndGet();
        cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        int parameterCount = invokedType.parameterCount();
        if (parameterCount > 0) {
            argNames = new String[parameterCount];
            argDescs = new String[parameterCount];
            for (int i = 0; i < parameterCount; i++) {
                argNames[i] = "arg$" + (i + 1);
                argDescs[i] = BytecodeDescriptor.unparse(invokedType.parameterType(i));
            }
        } else {
            argNames = argDescs = EMPTY_STRING_ARRAY;
        }
    }

    /**
     * 构建 CallSite。生成实现功能接口的类文件，定义类，如果没有参数，则创建类的实例，该实例将由 CallSite 返回，否则，生成调用类构造函数的句柄。
     *
     * @return 一个 CallSite，当被调用时，将返回功能接口的实例
     * @throws ReflectiveOperationException
     * @throws LambdaConversionException 如果未找到正确形成的功能接口
     */
    @Override
    CallSite buildCallSite() throws LambdaConversionException {
        final Class<?> innerClass = spinInnerClass();
        if (invokedType.parameterCount() == 0) {
            final Constructor<?>[] ctrs = AccessController.doPrivileged(
                    new PrivilegedAction<Constructor<?>[]>() {
                @Override
                public Constructor<?>[] run() {
                    Constructor<?>[] ctrs = innerClass.getDeclaredConstructors();
                    if (ctrs.length == 1) {
                        // 实现 lambda 的内部类构造函数是私有的，设置
                        // 它可访问（由我们）在创建常量唯一实例之前
                        ctrs[0].setAccessible(true);
                    }
                    return ctrs;
                }
                    });
            if (ctrs.length != 1) {
                throw new LambdaConversionException("Expected one lambda constructor for "
                        + innerClass.getCanonicalName() + ", got " + ctrs.length);
            }

            try {
                Object inst = ctrs[0].newInstance();
                return new ConstantCallSite(MethodHandles.constant(samBase, inst));
            }
            catch (ReflectiveOperationException e) {
                throw new LambdaConversionException("Exception instantiating lambda object", e);
            }
        } else {
            try {
                UNSAFE.ensureClassInitialized(innerClass);
                return new ConstantCallSite(
                        MethodHandles.Lookup.IMPL_LOOKUP
                             .findStatic(innerClass, NAME_FACTORY, invokedType));
            }
            catch (ReflectiveOperationException e) {
                throw new LambdaConversionException("Exception finding constructor", e);
            }
        }
    }

    /**
     * 生成实现功能接口的类文件，定义并返回类。
     *
     * @implNote 生成的类不包含 SAM 方法上可能存在的异常的签名信息。
     * 这是为了减少类文件的大小，而且这是无害的，因为检查异常会被擦除，没有人会针对此类文件进行编译，
     * 我们不对 lambda 对象的反射属性做出任何保证。
     *
     * @return 实现功能接口的类
     * @throws LambdaConversionException 如果未找到正确形成的功能接口
     */
    private Class<?> spinInnerClass() throws LambdaConversionException {
        String[] interfaces;
        String samIntf = samBase.getName().replace('.', '/');
        boolean accidentallySerializable = !isSerializable && Serializable.class.isAssignableFrom(samBase);
        if (markerInterfaces.length == 0) {
            interfaces = new String[]{samIntf};
        } else {
            // 确保没有重复的接口（ClassFormatError）
            Set<String> itfs = new LinkedHashSet<>(markerInterfaces.length + 1);
            itfs.add(samIntf);
            for (Class<?> markerInterface : markerInterfaces) {
                itfs.add(markerInterface.getName().replace('.', '/'));
                accidentallySerializable |= !isSerializable && Serializable.class.isAssignableFrom(markerInterface);
            }
            interfaces = itfs.toArray(new String[itfs.size()]);
        }

        cw.visit(CLASSFILE_VERSION, ACC_SUPER + ACC_FINAL + ACC_SYNTHETIC,
                 lambdaClassName, null,
                 JAVA_LANG_OBJECT, interfaces);

        // 生成由构造函数填充的 final 字段
        for (int i = 0; i < argDescs.length; i++) {
            FieldVisitor fv = cw.visitField(ACC_PRIVATE + ACC_FINAL,
                                            argNames[i],
                                            argDescs[i],
                                            null, null);
            fv.visitEnd();
        }

        generateConstructor();

        if (invokedType.parameterCount() != 0) {
            generateFactory();
        }

        // 转发 SAM 方法
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, samMethodName,
                                          samMethodType.toMethodDescriptorString(), null, null);
        mv.visitAnnotation("Ljava/lang/invoke/LambdaForm$Hidden;", true);
        new ForwardingMethodGenerator(mv).generate(samMethodType);

        // 转发桥接方法
        if (additionalBridges != null) {
            for (MethodType mt : additionalBridges) {
                mv = cw.visitMethod(ACC_PUBLIC|ACC_BRIDGE, samMethodName,
                                    mt.toMethodDescriptorString(), null, null);
                mv.visitAnnotation("Ljava/lang/invoke/LambdaForm$Hidden;", true);
                new ForwardingMethodGenerator(mv).generate(mt);
            }
        }


                    if (isSerializable)
            generateSerializationFriendlyMethods();
        else if (accidentallySerializable)
            generateSerializationHostileMethods();

        cw.visitEnd();

        // 在此 VM 中定义生成的类。

        final byte[] classBytes = cw.toByteArray();

        // 如果需要，将类输出到文件以供调试
        if (dumper != null) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    dumper.dumpClass(lambdaClassName, classBytes);
                    return null;
                }
            }, null,
            new FilePermission("<<ALL FILES>>", "read, write"),
            // createDirectories 可能需要它
            new PropertyPermission("user.dir", "read"));
        }

        return UNSAFE.defineAnonymousClass(targetClass, classBytes, null);
    }

    /**
     * 生成类的工厂方法
     */
    private void generateFactory() {
        MethodVisitor m = cw.visitMethod(ACC_PRIVATE | ACC_STATIC, NAME_FACTORY, invokedType.toMethodDescriptorString(), null, null);
        m.visitCode();
        m.visitTypeInsn(NEW, lambdaClassName);
        m.visitInsn(Opcodes.DUP);
        int parameterCount = invokedType.parameterCount();
        for (int typeIndex = 0, varIndex = 0; typeIndex < parameterCount; typeIndex++) {
            Class<?> argType = invokedType.parameterType(typeIndex);
            m.visitVarInsn(getLoadOpcode(argType), varIndex);
            varIndex += getParameterSize(argType);
        }
        m.visitMethodInsn(INVOKESPECIAL, lambdaClassName, NAME_CTOR, constructorType.toMethodDescriptorString(), false);
        m.visitInsn(ARETURN);
        m.visitMaxs(-1, -1);
        m.visitEnd();
    }

    /**
     * 生成类的构造函数
     */
    private void generateConstructor() {
        // 生成构造函数
        MethodVisitor ctor = cw.visitMethod(ACC_PRIVATE, NAME_CTOR,
                                            constructorType.toMethodDescriptorString(), null, null);
        ctor.visitCode();
        ctor.visitVarInsn(ALOAD, 0);
        ctor.visitMethodInsn(INVOKESPECIAL, JAVA_LANG_OBJECT, NAME_CTOR,
                             METHOD_DESCRIPTOR_VOID, false);
        int parameterCount = invokedType.parameterCount();
        for (int i = 0, lvIndex = 0; i < parameterCount; i++) {
            ctor.visitVarInsn(ALOAD, 0);
            Class<?> argType = invokedType.parameterType(i);
            ctor.visitVarInsn(getLoadOpcode(argType), lvIndex + 1);
            lvIndex += getParameterSize(argType);
            ctor.visitFieldInsn(PUTFIELD, lambdaClassName, argNames[i], argDescs[i]);
        }
        ctor.visitInsn(RETURN);
        // Maxs 由 ClassWriter.COMPUTE_MAXS 计算，这些参数被忽略
        ctor.visitMaxs(-1, -1);
        ctor.visitEnd();
    }

    /**
     * 生成支持序列化的 writeReplace 方法
     */
    private void generateSerializationFriendlyMethods() {
        TypeConvertingMethodAdapter mv
                = new TypeConvertingMethodAdapter(
                    cw.visitMethod(ACC_PRIVATE + ACC_FINAL,
                    NAME_METHOD_WRITE_REPLACE, DESCR_METHOD_WRITE_REPLACE,
                    null, null));

        mv.visitCode();
        mv.visitTypeInsn(NEW, NAME_SERIALIZED_LAMBDA);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(Type.getType(targetClass));
        mv.visitLdcInsn(invokedType.returnType().getName().replace('.', '/'));
        mv.visitLdcInsn(samMethodName);
        mv.visitLdcInsn(samMethodType.toMethodDescriptorString());
        mv.visitLdcInsn(implInfo.getReferenceKind());
        mv.visitLdcInsn(implInfo.getDeclaringClass().getName().replace('.', '/'));
        mv.visitLdcInsn(implInfo.getName());
        mv.visitLdcInsn(implInfo.getMethodType().toMethodDescriptorString());
        mv.visitLdcInsn(instantiatedMethodType.toMethodDescriptorString());
        mv.iconst(argDescs.length);
        mv.visitTypeInsn(ANEWARRAY, JAVA_LANG_OBJECT);
        for (int i = 0; i < argDescs.length; i++) {
            mv.visitInsn(DUP);
            mv.iconst(i);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, lambdaClassName, argNames[i], argDescs[i]);
            mv.boxIfTypePrimitive(Type.getType(argDescs[i]));
            mv.visitInsn(AASTORE);
        }
        mv.visitMethodInsn(INVOKESPECIAL, NAME_SERIALIZED_LAMBDA, NAME_CTOR,
                DESCR_CTOR_SERIALIZED_LAMBDA, false);
        mv.visitInsn(ARETURN);
        // Maxs 由 ClassWriter.COMPUTE_MAXS 计算，这些参数被忽略
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    /**
     * 生成对序列化不友好的 readObject/writeObject 方法
     */
    private void generateSerializationHostileMethods() {
        MethodVisitor mv = cw.visitMethod(ACC_PRIVATE + ACC_FINAL,
                                          NAME_METHOD_WRITE_OBJECT, DESCR_METHOD_WRITE_OBJECT,
                                          null, SER_HOSTILE_EXCEPTIONS);
        mv.visitCode();
        mv.visitTypeInsn(NEW, NAME_NOT_SERIALIZABLE_EXCEPTION);
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Non-serializable lambda");
        mv.visitMethodInsn(INVOKESPECIAL, NAME_NOT_SERIALIZABLE_EXCEPTION, NAME_CTOR,
                           DESCR_CTOR_NOT_SERIALIZABLE_EXCEPTION, false);
        mv.visitInsn(ATHROW);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PRIVATE + ACC_FINAL,
                            NAME_METHOD_READ_OBJECT, DESCR_METHOD_READ_OBJECT,
                            null, SER_HOSTILE_EXCEPTIONS);
        mv.visitCode();
        mv.visitTypeInsn(NEW, NAME_NOT_SERIALIZABLE_EXCEPTION);
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Non-serializable lambda");
        mv.visitMethodInsn(INVOKESPECIAL, NAME_NOT_SERIALIZABLE_EXCEPTION, NAME_CTOR,
                           DESCR_CTOR_NOT_SERIALIZABLE_EXCEPTION, false);
        mv.visitInsn(ATHROW);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    /**
     * 该类生成一个方法体，该方法体调用 lambda 实现方法，必要时转换参数。
     */
    private class ForwardingMethodGenerator extends TypeConvertingMethodAdapter {

        ForwardingMethodGenerator(MethodVisitor mv) {
            super(mv);
        }

        void generate(MethodType methodType) {
            visitCode();

            if (implKind == MethodHandleInfo.REF_newInvokeSpecial) {
                visitTypeInsn(NEW, implMethodClassName);
                visitInsn(DUP);
            }
            for (int i = 0; i < argNames.length; i++) {
                visitVarInsn(ALOAD, 0);
                visitFieldInsn(GETFIELD, lambdaClassName, argNames[i], argDescs[i]);
            }

            convertArgumentTypes(methodType);

            // 调用要转发的方法
            visitMethodInsn(invocationOpcode(), implMethodClassName,
                            implMethodName, implMethodDesc,
                            implDefiningClass.isInterface());

            // 转换返回值（如果有）并返回
            // 注意：如果从非 void 转换为 void，'return' 指令将弹出不需要的结果
            Class<?> samReturnClass = methodType.returnType();
            convertType(implMethodReturnClass, samReturnClass, samReturnClass);
            visitInsn(getReturnOpcode(samReturnClass));
            // Maxs 由 ClassWriter.COMPUTE_MAXS 计算，这些参数被忽略
            visitMaxs(-1, -1);
            visitEnd();
        }

        private void convertArgumentTypes(MethodType samType) {
            int lvIndex = 0;
            boolean samIncludesReceiver = implIsInstanceMethod &&
                                                   invokedType.parameterCount() == 0;
            int samReceiverLength = samIncludesReceiver ? 1 : 0;
            if (samIncludesReceiver) {
                // 推送接收者
                Class<?> rcvrType = samType.parameterType(0);
                visitVarInsn(getLoadOpcode(rcvrType), lvIndex + 1);
                lvIndex += getParameterSize(rcvrType);
                convertType(rcvrType, implDefiningClass, instantiatedMethodType.parameterType(0));
            }
            int samParametersLength = samType.parameterCount();
            int argOffset = implMethodType.parameterCount() - samParametersLength;
            for (int i = samReceiverLength; i < samParametersLength; i++) {
                Class<?> argType = samType.parameterType(i);
                visitVarInsn(getLoadOpcode(argType), lvIndex + 1);
                lvIndex += getParameterSize(argType);
                convertType(argType, implMethodType.parameterType(argOffset + i), instantiatedMethodType.parameterType(i));
            }
        }

        private int invocationOpcode() throws InternalError {
            switch (implKind) {
                case MethodHandleInfo.REF_invokeStatic:
                    return INVOKESTATIC;
                case MethodHandleInfo.REF_newInvokeSpecial:
                    return INVOKESPECIAL;
                 case MethodHandleInfo.REF_invokeVirtual:
                    return INVOKEVIRTUAL;
                case MethodHandleInfo.REF_invokeInterface:
                    return INVOKEINTERFACE;
                case MethodHandleInfo.REF_invokeSpecial:
                    return INVOKESPECIAL;
                default:
                    throw new InternalError("Unexpected invocation kind: " + implKind);
            }
        }
    }

    static int getParameterSize(Class<?> c) {
        if (c == Void.TYPE) {
            return 0;
        } else if (c == Long.TYPE || c == Double.TYPE) {
            return 2;
        }
        return 1;
    }

    static int getLoadOpcode(Class<?> c) {
        if(c == Void.TYPE) {
            throw new InternalError("Unexpected void type of load opcode");
        }
        return ILOAD + getOpcodeOffset(c);
    }

    static int getReturnOpcode(Class<?> c) {
        if(c == Void.TYPE) {
            return RETURN;
        }
        return IRETURN + getOpcodeOffset(c);
    }

    private static int getOpcodeOffset(Class<?> c) {
        if (c.isPrimitive()) {
            if (c == Long.TYPE) {
                return 1;
            } else if (c == Float.TYPE) {
                return 2;
            } else if (c == Double.TYPE) {
                return 3;
            }
            return 0;
        } else {
            return 4;
        }
    }

}
