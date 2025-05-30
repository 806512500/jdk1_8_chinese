
/*
 * Copyright (c) 2008, 2018, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.invoke.WrapperInstance;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.reflect.misc.ReflectUtil;
import static java.lang.invoke.MethodHandleStatics.*;

/**
 * 本类仅包含静态方法，用于将方法句柄适配为其他JVM类型，如接口。
 */
public class MethodHandleProxies {

    private MethodHandleProxies() { }  // 不允许实例化

    /**
     * 生成给定单方法接口的一个实例，该实例将调用重定向到给定的方法句柄。
     * <p>
     * 单方法接口是指声明了唯一命名方法的接口。
     * 在确定单方法接口的唯一命名方法时，公共的 {@code Object} 方法（如 {@code toString}、{@code equals}、{@code hashCode}）
     * 会被忽略，任何默认（非抽象）方法也会被忽略。
     * 例如，{@link java.util.Comparator} 是一个单方法接口，
     * 即使它重新声明了 {@code Object.equals} 方法，还声明了默认方法，如 {@code Comparator.reverse}。
     * <p>
     * 接口必须是公共的。不会进行额外的访问检查。
     * <p>
     * 所生成的所需类型的实例在调用该类型的唯一命名方法时，将调用给定的目标方法句柄，
     * 并返回或抛出目标方法句柄返回或抛出的内容。调用方式类似于 {@code target.invoke}。
     * 在创建实例之前，会检查目标方法句柄的类型，类似于调用 {@code asType}，
     * 这可能会导致 {@code WrongMethodTypeException} 异常。
     * <p>
     * 唯一命名方法可以有多个声明，具有不同的类型描述符。例如，它可以是重载的，或者具有桥接方法。
     * 所有这些声明都直接连接到目标方法句柄。每个单独的声明的参数和返回类型都通过 {@code asType} 进行调整。
     * <p>
     * 包装实例将实现请求的接口及其超类型，但不会实现其他单方法接口。
     * 这意味着该实例不会意外地通过任何未请求类型的 {@code instanceof} 测试。
     * <p style="font-size:smaller;">
     * <em>实现说明：</em>
     * 因此，每个实例必须实现一个唯一的单方法接口。
     * 实现不能将多个单方法接口捆绑到单个实现类上，类似于 {@link java.awt.AWTEventMulticaster} 的风格。
     * <p>
     * 方法句柄可能会抛出一个 <em>未声明的异常</em>，即任何未被请求类型单抽象方法声明的检查异常（或其他检查的可抛出对象）。
     * 如果发生这种情况，该可抛出对象将被包装在一个 {@link java.lang.reflect.UndeclaredThrowableException UndeclaredThrowableException} 实例中，并以包装的形式抛出。
     * <p>
     * 类似于 {@link java.lang.Integer#valueOf Integer.valueOf}，
     * {@code asInterfaceInstance} 是一个工厂方法，其结果由其行为定义。
     * 不保证每次调用都会返回一个新的实例。
     * <p>
     * 由于可能存在 {@linkplain java.lang.reflect.Method#isBridge 桥接方法} 和其他特殊情况，
     * 接口可能有多个具有相同名称但类型描述符不同的抽象方法。
     * 在这种情况下，所有方法都共同绑定到给定的目标。类型检查和有效的 {@code asType} 转换应用于每个方法类型描述符，
     * 所有抽象方法都共同绑定到目标。除了这种类型检查外，不会进行进一步的检查来确定抽象方法之间是否存在任何关系。
     * <p>
     * 未来版本的此 API 可能会接受其他类型，如具有单个抽象方法的抽象类。
     * 未来版本的此 API 也可能为包装实例配备一个或多个额外的公共“标记”接口。
     * <p>
     * 如果安装了安全管理器，此方法是调用者敏感的。
     * 在通过返回的包装器调用目标方法句柄的任何调用期间，原始创建者（调用者）将对安全管理器请求的上下文检查可见。
     *
     * @param <T> 包装器所需的类型，一个单方法接口
     * @param intfc 代表 {@code T} 的类对象
     * @param target 从包装器调用的方法句柄
     * @return 给定目标的正确类型的包装器
     * @throws NullPointerException 如果任一参数为 null
     * @throws IllegalArgumentException 如果 {@code intfc} 不是此方法的有效参数
     * @throws WrongMethodTypeException 如果目标无法转换为请求接口所需类型
     */
    // 其他实现者注意事项：
    // <p>
    // 不承诺单方法接口和实现类 C 之间有稳定的映射。随着时间的推移，同一类型可能会使用多个实现类。
    // <p>
    // 如果实现能够证明已经为给定的方法句柄或具有相同行为的其他方法句柄创建了所需类型的包装器，
    // 实现可以返回该包装器，而不是创建新的包装器。
    // <p>
    // 该方法旨在适用于常见的用例，即单个方法句柄必须与实现函数式 API 的接口互操作。
    // 其他变体，如具有私有构造函数的单抽象方法类，或具有多个但相关入口点的接口，必须通过手写或自动生成的适配器类来覆盖。
    //
    @CallerSensitive
    public static
    <T> T asInterfaceInstance(final Class<T> intfc, final MethodHandle target) {
        if (!intfc.isInterface() || !Modifier.isPublic(intfc.getModifiers()))
            throw newIllegalArgumentException("not a public interface", intfc.getName());
        final MethodHandle mh;
        if (System.getSecurityManager() != null) {
            final Class<?> caller = Reflection.getCallerClass();
            final ClassLoader ccl = caller != null ? caller.getClassLoader() : null;
            ReflectUtil.checkProxyPackageAccess(ccl, intfc);
            mh = ccl != null ? bindCaller(target, caller) : target;
        } else {
            mh = target;
        }
        ClassLoader proxyLoader = intfc.getClassLoader();
        if (proxyLoader == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader(); // 避免使用 BCP
            proxyLoader = cl != null ? cl : ClassLoader.getSystemClassLoader();
        }
        final Method[] methods = getSingleNameMethods(intfc);
        if (methods == null)
            throw newIllegalArgumentException("not a single-method interface", intfc.getName());
        final MethodHandle[] vaTargets = new MethodHandle[methods.length];
        for (int i = 0; i < methods.length; i++) {
            Method sm = methods[i];
            MethodType smMT = MethodType.methodType(sm.getReturnType(), sm.getParameterTypes());
            MethodHandle checkTarget = mh.asType(smMT);  // 使 WMT 抛出
            checkTarget = checkTarget.asType(checkTarget.type().changeReturnType(Object.class));
            vaTargets[i] = checkTarget.asSpreader(Object[].class, smMT.parameterCount());
        }
        final ConcurrentHashMap<Method, MethodHandle> defaultMethodMap =
                hasDefaultMethods(intfc) ? new ConcurrentHashMap<>() : null;
        final InvocationHandler ih = new InvocationHandler() {
                private Object getArg(String name) {
                    if ((Object)name == "getWrapperInstanceTarget")  return target;
                    if ((Object)name == "getWrapperInstanceType")    return intfc;
                    throw new AssertionError();
                }
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    for (int i = 0; i < methods.length; i++) {
                        if (method.equals(methods[i]))
                            return vaTargets[i].invokeExact(args);
                    }
                    if (method.getDeclaringClass() == WrapperInstance.class)
                        return getArg(method.getName());
                    if (isObjectMethod(method))
                        return callObjectMethod(proxy, method, args);
                    if (isDefaultMethod(method)) {
                        return callDefaultMethod(defaultMethodMap, proxy, intfc, method, args);
                    }
                    throw newInternalError("bad proxy method: "+method);
                }
            };

        final Object proxy;
        if (System.getSecurityManager() != null) {
            // sun.invoke.WrapperInstance 是一个受限接口，任何非空类加载器都无法访问。
            final ClassLoader loader = proxyLoader;
            proxy = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    return Proxy.newProxyInstance(
                            loader,
                            new Class<?>[]{ intfc, WrapperInstance.class },
                            ih);
                }
            });
        } else {
            proxy = Proxy.newProxyInstance(proxyLoader,
                                           new Class<?>[]{ intfc, WrapperInstance.class },
                                           ih);
        }
        return intfc.cast(proxy);
    }

    private static MethodHandle bindCaller(MethodHandle target, Class<?> hostClass) {
        MethodHandle cbmh = MethodHandleImpl.bindCaller(target, hostClass);
        if (target.isVarargsCollector()) {
            MethodType type = cbmh.type();
            int arity = type.parameterCount();
            return cbmh.asVarargsCollector(type.parameterType(arity-1));
        }
        return cbmh;
    }

    /**
     * 确定给定对象是否由 {@link #asInterfaceInstance asInterfaceInstance} 调用生成。
     * @param x 任何引用
     * @return 如果引用不为 null 且指向由 {@code asInterfaceInstance} 生成的对象，则返回 true
     */
    public static
    boolean isWrapperInstance(Object x) {
        return x instanceof WrapperInstance;
    }

    private static WrapperInstance asWrapperInstance(Object x) {
        try {
            if (x != null)
                return (WrapperInstance) x;
        } catch (ClassCastException ex) {
        }
        throw newIllegalArgumentException("not a wrapper instance");
    }

    /**
     * 生成或恢复一个行为上等同于此包装实例唯一方法的目标方法句柄。
     * 对象 {@code x} 必须由 {@link #asInterfaceInstance asInterfaceInstance} 调用生成。
     * 可以通过 {@link #isWrapperInstance isWrapperInstance} 测试此要求。
     * @param x 任何引用
     * @return 实现唯一方法的方法句柄
     * @throws IllegalArgumentException 如果引用 x 不是指向包装实例的引用
     */
    public static
    MethodHandle wrapperInstanceTarget(Object x) {
        return asWrapperInstance(x).getWrapperInstanceTarget();
    }

    /**
     * 恢复此包装实例创建时的唯一单方法接口类型。
     * 对象 {@code x} 必须由 {@link #asInterfaceInstance asInterfaceInstance} 调用生成。
     * 可以通过 {@link #isWrapperInstance isWrapperInstance} 测试此要求。
     * @param x 任何引用
     * @return 为该包装创建的单方法接口类型
     * @throws IllegalArgumentException 如果引用 x 不是指向包装实例的引用
     */
    public static
    Class<?> wrapperInstanceType(Object x) {
        return asWrapperInstance(x).getWrapperInstanceType();
    }

    private static
    boolean isObjectMethod(Method m) {
        switch (m.getName()) {
        case "toString":
            return (m.getReturnType() == String.class
                    && m.getParameterTypes().length == 0);
        case "hashCode":
            return (m.getReturnType() == int.class
                    && m.getParameterTypes().length == 0);
        case "equals":
            return (m.getReturnType() == boolean.class
                    && m.getParameterTypes().length == 1
                    && m.getParameterTypes()[0] == Object.class);
        }
        return false;
    }

    private static
    Object callObjectMethod(Object self, Method m, Object[] args) {
        assert(isObjectMethod(m)) : m;
        switch (m.getName()) {
        case "toString":
            return self.getClass().getName() + "@" + Integer.toHexString(self.hashCode());
        case "hashCode":
            return System.identityHashCode(self);
        case "equals":
            return (self == args[0]);
        }
        return null;
    }

    private static
    Method[] getSingleNameMethods(Class<?> intfc) {
        ArrayList<Method> methods = new ArrayList<Method>();
        String uniqueName = null;
        for (Method m : intfc.getMethods()) {
            if (isObjectMethod(m))  continue;
            if (!Modifier.isAbstract(m.getModifiers()))  continue;
            String mname = m.getName();
            if (uniqueName == null)
                uniqueName = mname;
            else if (!uniqueName.equals(mname))
                return null;  // 太多抽象方法
            methods.add(m);
        }
        if (uniqueName == null)  return null;
        return methods.toArray(new Method[methods.size()]);
    }


                private static
    boolean isDefaultMethod(Method m) {
        // 返回方法是否不是抽象的，即是否是默认方法。
        return !Modifier.isAbstract(m.getModifiers());
    }

    private static
    boolean hasDefaultMethods(Class<?> intfc) {
        // 遍历接口的所有方法，检查是否有默认方法。
        for (Method m : intfc.getMethods()) {
            if (!isObjectMethod(m) &&
                !Modifier.isAbstract(m.getModifiers())) {
                return true;
            }
        }
        return false;
    }

    private static
    Object callDefaultMethod(ConcurrentHashMap<Method, MethodHandle> defaultMethodMap,
                             Object self, Class<?> intfc, Method m, Object[] args) throws Throwable {
        // 断言方法是默认方法且不是对象方法。
        assert(isDefaultMethod(m) && !isObjectMethod(m)) : m;

        // 惰性计算与方法关联的方法句柄
        MethodHandle dmh = defaultMethodMap.computeIfAbsent(m, mk -> {
            try {
                // 查找用于特殊调用的默认方法，从而避免递归调回到代理
                MethodHandle mh = MethodHandles.Lookup.IMPL_LOOKUP.findSpecial(
                        intfc, mk.getName(),
                        MethodType.methodType(mk.getReturnType(), mk.getParameterTypes()),
                        self.getClass());
                return mh.asSpreader(Object[].class, mk.getParameterCount());
            } catch (NoSuchMethodException | IllegalAccessException e) {
                // 已知该方法存在且应可访问，除非通过接口调用默认（公共）方法的访问控制检查，否则不会调用此方法
                throw new InternalError(e);
            }
        });
        return dmh.invoke(self, args);
    }
}
