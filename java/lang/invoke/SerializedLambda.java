
/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Objects;

/**
 * Lambda 表达式的序列化形式。此类的属性表示在 lambda 工厂站点存在的信息，包括静态元工厂参数（如主要函数接口方法和实现方法的身份）以及动态元工厂参数（如在 lambda 捕获时从词法作用域捕获的值）。
 *
 * <p>实现可序列化 lambda 的编译器或语言运行时库应确保实例正确反序列化。一种方法是确保 {@code writeReplace} 方法返回 {@code SerializedLambda} 的实例，而不是允许默认序列化继续进行。
 *
 * <p>{@code SerializedLambda} 有一个 {@code readResolve} 方法，该方法在捕获类中查找（可能是私有的）静态方法 {@code $deserializeLambda$(SerializedLambda)}，使用自身作为第一个参数调用该方法，并返回结果。实现 {@code $deserializeLambda$} 的 lambda 类负责验证 {@code SerializedLambda} 的属性是否与该类实际捕获的 lambda 一致。
 *
 * @see LambdaMetafactory
 */
public final class SerializedLambda implements Serializable {
    private static final long serialVersionUID = 8025925345765570181L;
    private final Class<?> capturingClass;
    private final String functionalInterfaceClass;
    private final String functionalInterfaceMethodName;
    private final String functionalInterfaceMethodSignature;
    private final String implClass;
    private final String implMethodName;
    private final String implMethodSignature;
    private final int implMethodKind;
    private final String instantiatedMethodType;
    private final Object[] capturedArgs;

    /**
     * 从 lambda 工厂站点的低级信息创建 {@code SerializedLambda}。
     *
     * @param capturingClass lambda 表达式出现的类
     * @param functionalInterfaceClass 返回的 lambda 对象的静态类型的名称，以斜杠分隔的形式
     * @param functionalInterfaceMethodName 出现在 lambda 工厂站点的函数接口方法的名称
     * @param functionalInterfaceMethodSignature 出现在 lambda 工厂站点的函数接口方法的签名
     * @param implMethodKind 实现方法的方法句柄类型
     * @param implClass 实现方法所在的类的名称，以斜杠分隔的形式
     * @param implMethodName 实现方法的名称
     * @param implMethodSignature 实现方法的签名
     * @param instantiatedMethodType 在捕获站点将类型变量替换为其实例化后的主要函数接口方法的签名
     * @param capturedArgs 传递给 lambda 工厂站点的动态参数，表示 lambda 捕获的变量
     */
    public SerializedLambda(Class<?> capturingClass,
                            String functionalInterfaceClass,
                            String functionalInterfaceMethodName,
                            String functionalInterfaceMethodSignature,
                            int implMethodKind,
                            String implClass,
                            String implMethodName,
                            String implMethodSignature,
                            String instantiatedMethodType,
                            Object[] capturedArgs) {
        this.capturingClass = capturingClass;
        this.functionalInterfaceClass = functionalInterfaceClass;
        this.functionalInterfaceMethodName = functionalInterfaceMethodName;
        this.functionalInterfaceMethodSignature = functionalInterfaceMethodSignature;
        this.implMethodKind = implMethodKind;
        this.implClass = implClass;
        this.implMethodName = implMethodName;
        this.implMethodSignature = implMethodSignature;
        this.instantiatedMethodType = instantiatedMethodType;
        this.capturedArgs = Objects.requireNonNull(capturedArgs).clone();
    }

    /**
     * 获取捕获此 lambda 的类的名称。
     * @return 捕获此 lambda 的类的名称
     */
    public String getCapturingClass() {
        return capturingClass.getName().replace('.', '/');
    }

    /**
     * 获取此 lambda 已转换为的调用类型名称。
     * @return 此 lambda 已转换为的函数接口类的名称
     */
    public String getFunctionalInterfaceClass() {
        return functionalInterfaceClass;
    }

    /**
     * 获取此 lambda 已转换为的函数接口的主要方法的名称。
     * @return 函数接口的主要方法的名称
     */
    public String getFunctionalInterfaceMethodName() {
        return functionalInterfaceMethodName;
    }

    /**
     * 获取此 lambda 已转换为的函数接口的主要方法的签名。
     * @return 函数接口的主要方法的签名
     */
    public String getFunctionalInterfaceMethodSignature() {
        return functionalInterfaceMethodSignature;
    }


                /**
     * 获取包含实现方法的类的名称。
     * @return 包含实现方法的类的名称
     */
    public String getImplClass() {
        return implClass;
    }

    /**
     * 获取实现方法的名称。
     * @return 实现方法的名称
     */
    public String getImplMethodName() {
        return implMethodName;
    }

    /**
     * 获取实现方法的签名。
     * @return 实现方法的签名
     */
    public String getImplMethodSignature() {
        return implMethodSignature;
    }

    /**
     * 获取实现方法的方法句柄类型（参见 {@link MethodHandleInfo}）。
     * @return 实现方法的方法句柄类型
     */
    public int getImplMethodKind() {
        return implMethodKind;
    }

    /**
     * 获取在捕获站点类型变量被替换为其实例化后的主函数接口方法的签名。
     * @return 类型变量处理后的主函数接口方法的签名
     */
    public final String getInstantiatedMethodType() {
        return instantiatedMethodType;
    }

    /**
     * 获取传递给 lambda 捕获站点的动态参数的数量。
     * @return 传递给 lambda 捕获站点的动态参数的数量
     */
    public int getCapturedArgCount() {
        return capturedArgs.length;
    }

    /**
     * 获取传递给 lambda 捕获站点的动态参数。
     * @param i 要捕获的参数
     * @return 传递给 lambda 捕获站点的动态参数
     */
    public Object getCapturedArg(int i) {
        return capturedArgs[i];
    }

    private Object readResolve() throws ReflectiveOperationException {
        try {
            Method deserialize = AccessController.doPrivileged(new PrivilegedExceptionAction<Method>() {
                @Override
                public Method run() throws Exception {
                    Method m = capturingClass.getDeclaredMethod("$deserializeLambda$", SerializedLambda.class);
                    m.setAccessible(true);
                    return m;
                }
            });

            return deserialize.invoke(null, this);
        }
        catch (PrivilegedActionException e) {
            Exception cause = e.getException();
            if (cause instanceof ReflectiveOperationException)
                throw (ReflectiveOperationException) cause;
            else if (cause instanceof RuntimeException)
                throw (RuntimeException) cause;
            else
                throw new RuntimeException("Exception in SerializedLambda.readResolve", e);
        }
    }

    @Override
    public String toString() {
        String implKind=MethodHandleInfo.referenceKindToString(implMethodKind);
        return String.format("SerializedLambda[%s=%s, %s=%s.%s:%s, " +
                             "%s=%s %s.%s:%s, %s=%s, %s=%d]",
                             "capturingClass", capturingClass,
                             "functionalInterfaceMethod", functionalInterfaceClass,
                               functionalInterfaceMethodName,
                               functionalInterfaceMethodSignature,
                             "implementation",
                               implKind,
                               implClass, implMethodName, implMethodSignature,
                             "instantiatedMethodType", instantiatedMethodType,
                             "numCaptured", capturedArgs.length);
    }
}
