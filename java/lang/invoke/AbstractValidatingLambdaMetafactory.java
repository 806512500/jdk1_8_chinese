
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

import sun.invoke.util.Wrapper;

import static sun.invoke.util.Wrapper.forPrimitiveType;
import static sun.invoke.util.Wrapper.forWrapperType;
import static sun.invoke.util.Wrapper.isWrapperType;

/**
 * 抽象实现了一个 lambda 元工厂，提供了参数展开和输入验证。
 *
 * @see LambdaMetafactory
 */
/* package */ abstract class AbstractValidatingLambdaMetafactory {

    /*
     * 为了上下文，以下字段的注释用引号标记了它们的值，给定以下程序：
     * interface II<T> {  Object foo(T x); }
     * interface JJ<R extends Number> extends II<R> { }
     * class CC {  String impl(int i) { return "impl:"+i; }}
     * class X {
     *     public static void main(String[] args) {
     *         JJ<Integer> iii = (new CC())::impl;
     *         System.out.printf(">>> %s\n", iii.foo(44));
     * }}
     */
    final Class<?> targetClass;               // 调用元工厂的类 "class X"
    final MethodType invokedType;             // 调用方法的类型 "(CC)II"
    final Class<?> samBase;                   // 返回实例的类型 "interface JJ"
    final String samMethodName;               // SAM 方法的名称 "foo"
    final MethodType samMethodType;           // SAM 方法的类型 "(Object)Object"
    final MethodHandle implMethod;            // 实现方法的原始方法句柄
    final MethodHandleInfo implInfo;          // 实现方法句柄的信息 "MethodHandleInfo[5 CC.impl(int)String]"
    final int implKind;                       // 实现方法的调用类型 "5"=invokevirtual
    final boolean implIsInstanceMethod;       // 实现方法是否为实例方法 "true"
    final Class<?> implDefiningClass;         // 定义实现方法的类型 "class CC"
    final MethodType implMethodType;          // 实现方法的类型 "(int)String"
    final MethodType instantiatedMethodType;  // 实例化后功能接口方法的类型 "(Integer)Object"
    final boolean isSerializable;             // 返回的实例是否应可序列化
    final Class<?>[] markerInterfaces;        // 额外的标记接口
    final MethodType[] additionalBridges;     // 需要桥接的额外方法签名


    /**
     * 元工厂构造函数。
     *
     * @param caller 由 VM 自动堆栈；表示具有调用者访问权限的查找上下文。
     * @param invokedType 由 VM 自动堆栈；调用方法的签名，包括返回的 lambda 对象的预期静态类型和 lambda 的捕获参数的静态类型。如果实现方法是实例方法，则调用签名中的第一个参数将对应于接收者。
     * @param samMethodName 要转换为 lambda 或方法引用的功能接口方法的名称，表示为字符串。
     * @param samMethodType 要转换为 lambda 或方法引用的功能接口方法的类型，表示为 MethodType。
     * @param implMethod 当调用结果功能接口实例的方法时，应调用的实现方法（需要适当调整参数类型、返回类型和捕获参数）。
     * @param instantiatedMethodType 类型变量被捕获站点的实例化替换后，主要功能接口方法的签名。
     * @param isSerializable lambda 是否应可序列化？如果设置，目标类型或额外的 SAM 类型之一必须扩展 {@code Serializable}。
     * @param markerInterfaces lambda 对象应实现的额外接口。
     * @param additionalBridges 需要桥接到实现方法的额外方法签名。
     * @throws LambdaConversionException 如果违反了任何元工厂协议不变性
     */
    AbstractValidatingLambdaMetafactory(MethodHandles.Lookup caller,
                                       MethodType invokedType,
                                       String samMethodName,
                                       MethodType samMethodType,
                                       MethodHandle implMethod,
                                       MethodType instantiatedMethodType,
                                       boolean isSerializable,
                                       Class<?>[] markerInterfaces,
                                       MethodType[] additionalBridges)
            throws LambdaConversionException {
        if ((caller.lookupModes() & MethodHandles.Lookup.PRIVATE) == 0) {
            throw new LambdaConversionException(String.format(
                    "无效的调用者: %s",
                    caller.lookupClass().getName()));
        }
        this.targetClass = caller.lookupClass();
        this.invokedType = invokedType;

        this.samBase = invokedType.returnType();

        this.samMethodName = samMethodName;
        this.samMethodType  = samMethodType;

        this.implMethod = implMethod;
        this.implInfo = caller.revealDirect(implMethod);
        this.implKind = implInfo.getReferenceKind();
        this.implIsInstanceMethod =
                implKind == MethodHandleInfo.REF_invokeVirtual ||
                implKind == MethodHandleInfo.REF_invokeSpecial ||
                implKind == MethodHandleInfo.REF_invokeInterface;
        this.implDefiningClass = implInfo.getDeclaringClass();
        this.implMethodType = implInfo.getMethodType();
        this.instantiatedMethodType = instantiatedMethodType;
        this.isSerializable = isSerializable;
        this.markerInterfaces = markerInterfaces;
        this.additionalBridges = additionalBridges;

        if (!samBase.isInterface()) {
            throw new LambdaConversionException(String.format(
                    "功能接口 %s 不是接口",
                    samBase.getName()));
        }

        for (Class<?> c : markerInterfaces) {
            if (!c.isInterface()) {
                throw new LambdaConversionException(String.format(
                        "标记接口 %s 不是接口",
                        c.getName()));
            }
        }
    }

    /**
     * 构建 CallSite。
     *
     * @return 一个 CallSite，当被调用时，将返回功能接口的实例
     * @throws ReflectiveOperationException
     */
    abstract CallSite buildCallSite()
            throws LambdaConversionException;

    /**
     * 检查元工厂参数是否有错误
     * @throws LambdaConversionException 如果有不正确的转换
     */
    void validateMetafactoryArgs() throws LambdaConversionException {
        switch (implKind) {
            case MethodHandleInfo.REF_invokeInterface:
            case MethodHandleInfo.REF_invokeVirtual:
            case MethodHandleInfo.REF_invokeStatic:
            case MethodHandleInfo.REF_newInvokeSpecial:
            case MethodHandleInfo.REF_invokeSpecial:
                break;
            default:
                throw new LambdaConversionException(String.format("不支持的 MethodHandle 类型: %s", implInfo));
        }

        // 检查参数数量：可选接收者 + 捕获参数 + SAM == 实现
        final int implArity = implMethodType.parameterCount();
        final int receiverArity = implIsInstanceMethod ? 1 : 0;
        final int capturedArity = invokedType.parameterCount();
        final int samArity = samMethodType.parameterCount();
        final int instantiatedArity = instantiatedMethodType.parameterCount();
        if (implArity + receiverArity != capturedArity + samArity) {
            throw new LambdaConversionException(
                    String.format("参数数量不正确 %s 方法 %s; %d 捕获参数, %d 功能接口方法参数, %d 实现参数",
                                  implIsInstanceMethod ? "实例" : "静态", implInfo,
                                  capturedArity, samArity, implArity));
        }
        if (instantiatedArity != samArity) {
            throw new LambdaConversionException(
                    String.format("参数数量不正确 %s 方法 %s; %d 实例化参数, %d 功能接口方法参数",
                                  implIsInstanceMethod ? "实例" : "静态", implInfo,
                                  instantiatedArity, samArity));
        }
        for (MethodType bridgeMT : additionalBridges) {
            if (bridgeMT.parameterCount() != samArity) {
                throw new LambdaConversionException(
                        String.format("桥接签名 %s 的参数数量不正确; 与 %s 不兼容",
                                      bridgeMT, samMethodType));
            }
        }

        // 如果是实例方法：第一个捕获参数（接收者）必须是实现方法定义类的子类型
        final int capturedStart;
        final int samStart;
        if (implIsInstanceMethod) {
            final Class<?> receiverClass;

            // 实现是实例方法，调整捕获变量和 SAM 参数中的接收者
            if (capturedArity == 0) {
                // 接收者是函数参数
                capturedStart = 0;
                samStart = 1;
                receiverClass = instantiatedMethodType.parameterType(0);
            } else {
                // 接收者是捕获变量
                capturedStart = 1;
                samStart = 0;
                receiverClass = invokedType.parameterType(0);
            }

            // 检查接收者类型
            if (!implDefiningClass.isAssignableFrom(receiverClass)) {
                throw new LambdaConversionException(
                        String.format("无效的接收者类型 %s; 不是实现类型 %s 的子类型",
                                      receiverClass, implDefiningClass));
            }

            Class<?> implReceiverClass = implMethod.type().parameterType(0);
            if (implReceiverClass != implDefiningClass && !implReceiverClass.isAssignableFrom(receiverClass)) {
                throw new LambdaConversionException(
                        String.format("无效的接收者类型 %s; 不是实现接收者类型 %s 的子类型",
                                     receiverClass, implReceiverClass));
            }
        } else {
            // 没有接收者
            capturedStart = 0;
            samStart = 0;
        }

        // 检查非接收者捕获参数的精确匹配
        final int implFromCaptured = capturedArity - capturedStart;
        for (int i=0; i<implFromCaptured; i++) {
            Class<?> implParamType = implMethodType.parameterType(i);
            Class<?> capturedParamType = invokedType.parameterType(i + capturedStart);
            if (!capturedParamType.equals(implParamType)) {
                throw new LambdaConversionException(
                        String.format("捕获的 lambda 参数 %d 类型不匹配: 期望 %s, 找到 %s",
                                      i, capturedParamType, implParamType));
            }
        }
        // 检查 SAM 参数的适配匹配
        final int samOffset = samStart - implFromCaptured;
        for (int i=implFromCaptured; i<implArity; i++) {
            Class<?> implParamType = implMethodType.parameterType(i);
            Class<?> instantiatedParamType = instantiatedMethodType.parameterType(i + samOffset);
            if (!isAdaptableTo(instantiatedParamType, implParamType, true)) {
                throw new LambdaConversionException(
                        String.format("lambda 参数 %d 类型不匹配: %s 不能转换为 %s",
                                      i, instantiatedParamType, implParamType));
            }
        }

        // 适配匹配：返回类型
        Class<?> expectedType = instantiatedMethodType.returnType();
        Class<?> actualReturnType =
                (implKind == MethodHandleInfo.REF_newInvokeSpecial)
                  ? implDefiningClass
                  : implMethodType.returnType();
        Class<?> samReturnType = samMethodType.returnType();
        if (!isAdaptableToAsReturn(actualReturnType, expectedType)) {
            throw new LambdaConversionException(
                    String.format("lambda 返回类型不匹配: %s 不能转换为 %s",
                                  actualReturnType, expectedType));
        }
        if (!isAdaptableToAsReturnStrict(expectedType, samReturnType)) {
            throw new LambdaConversionException(
                    String.format("lambda 期望返回类型不匹配: %s 不能转换为 %s",
                                  expectedType, samReturnType));
        }
        for (MethodType bridgeMT : additionalBridges) {
            if (!isAdaptableToAsReturnStrict(expectedType, bridgeMT.returnType())) {
                throw new LambdaConversionException(
                        String.format("lambda 期望返回类型不匹配: %s 不能转换为 %s",
                                      expectedType, bridgeMT.returnType()));
            }
        }
     }


                /**
     * 检查参数类型的适应性。
     * @param fromType 要转换的类型
     * @param toType 要转换到的类型
     * @param strict 如果为 true，则进行严格检查，否则允许 fromType 可能是参数化的
     * @return 如果 'fromType' 可以传递给 'toType' 的参数，则返回 true
     */
    private boolean isAdaptableTo(Class<?> fromType, Class<?> toType, boolean strict) {
        if (fromType.equals(toType)) {
            return true;
        }
        if (fromType.isPrimitive()) {
            Wrapper wfrom = forPrimitiveType(fromType);
            if (toType.isPrimitive()) {
                // 两者都是原始类型：扩展
                Wrapper wto = forPrimitiveType(toType);
                return wto.isConvertibleFrom(wfrom);
            } else {
                // 从原始类型到引用类型：装箱
                return toType.isAssignableFrom(wfrom.wrapperType());
            }
        } else {
            if (toType.isPrimitive()) {
                // 从引用类型到原始类型：拆箱
                Wrapper wfrom;
                if (isWrapperType(fromType) && (wfrom = forWrapperType(fromType)).primitiveType().isPrimitive()) {
                    // fromType 是一个原始包装器；拆箱+扩展
                    Wrapper wto = forPrimitiveType(toType);
                    return wto.isConvertibleFrom(wfrom);
                } else {
                    // 必须可以转换为原始类型
                    return !strict;
                }
            } else {
                // 两者都是引用类型：fromType 应该是 toType 的超类。
                return !strict || toType.isAssignableFrom(fromType);
            }
        }
    }

    /**
     * 检查返回类型的适应性 --
     * 特殊处理 void 类型和参数化的 fromType
     * @return 如果 'fromType' 可以转换为 'toType'，则返回 true
     */
    private boolean isAdaptableToAsReturn(Class<?> fromType, Class<?> toType) {
        return toType.equals(void.class)
               || !fromType.equals(void.class) && isAdaptableTo(fromType, toType, false);
    }
    private boolean isAdaptableToAsReturnStrict(Class<?> fromType, Class<?> toType) {
        if (fromType.equals(void.class)) return toType.equals(void.class);
        return isAdaptableTo(fromType, toType, true);
    }


    /*********** 日志支持 -- 仅用于调试，按需取消注释
    static final Executor logPool = Executors.newSingleThreadExecutor();
    protected static void log(final String s) {
        MethodHandleProxyLambdaMetafactory.logPool.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(s);
            }
        });
    }

    protected static void log(final String s, final Throwable e) {
        MethodHandleProxyLambdaMetafactory.logPool.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(s);
                e.printStackTrace(System.out);
            }
        });
    }
    ***********************/
}
