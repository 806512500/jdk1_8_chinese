/*
 * Copyright (c) 2003, 2019, Oracle and/or its affiliates. All rights reserved.
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
package java.rmi.server;

import java.io.InvalidObjectException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnexpectedException;
import java.rmi.activation.Activatable;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.WeakHashMap;
import sun.rmi.server.Util;
import sun.rmi.server.WeakClassHashMap;

/**
 * 一个实现了 <code>InvocationHandler</code> 接口的类，用于 Java 远程方法调用 (Java RMI)。此调用处理程序可以与动态代理实例结合使用，作为预生成存根类的替代。
 *
 * <p>应用程序不期望直接使用此类。使用 {@link UnicastRemoteObject} 或 {@link Activatable} 导出的远程对象具有此类的一个实例作为该代理的调用处理程序。
 *
 * @author  Ann Wollrath
 * @since   1.5
 **/
public class RemoteObjectInvocationHandler
    extends RemoteObject
    implements InvocationHandler
{
    private static final long serialVersionUID = 2L;

    // 如果调用处理程序允许 finalize 方法（遗留行为），则设置为 true
    private static final boolean allowFinalizeInvocation;

    static {
        String propName = "sun.rmi.server.invocationhandler.allowFinalizeInvocation";
        String allowProp = java.security.AccessController.doPrivileged(
            new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return System.getProperty(propName);
                }
            });
        if ("".equals(allowProp)) {
            allowFinalizeInvocation = true;
        } else {
            allowFinalizeInvocation = Boolean.parseBoolean(allowProp);
        }
    }

    /**
     * 一个弱哈希映射，将类映射到弱哈希映射，后者将方法对象映射到方法哈希。
     **/
    private static final MethodToHash_Maps methodToHash_Maps =
        new MethodToHash_Maps();

    /**
     * 使用指定的 <code>RemoteRef</code> 构造一个新的 <code>RemoteObjectInvocationHandler</code>。
     *
     * @param ref 远程引用
     *
     * @throws NullPointerException 如果 <code>ref</code> 为 <code>null</code>
     **/
    public RemoteObjectInvocationHandler(RemoteRef ref) {
        super(ref);
        if (ref == null) {
            throw new NullPointerException();
        }
    }

    /**
     * 处理在封装的代理实例 <code>proxy</code> 上调用的方法，并返回结果。
     *
     * <p><code>RemoteObjectInvocationHandler</code> 以如下方式实现此方法：
     *
     * <p>如果 <code>method</code> 是以下方法之一，则按如下方式处理：
     *
     * <ul>
     *
     * <li>{@link Object#hashCode Object.hashCode}: 返回代理的哈希码值。
     *
     * <li>{@link Object#equals Object.equals}: 如果参数 (<code>args[0]</code>) 是动态代理类的实例且此调用处理程序与该参数的调用处理程序相等，则返回 <code>true</code>，否则返回 <code>false</code>。
     *
     * <li>{@link Object#toString Object.toString}: 返回代理的字符串表示形式。
     * </ul>
     *
     * <p>否则，按如下方式执行远程调用：
     *
     * <ul>
     * <li>如果 <code>proxy</code> 不是 {@link Remote} 接口的实例，则抛出 {@link IllegalArgumentException}。
     *
     * <li>否则，调用此调用处理程序的 <code>RemoteRef</code> 的 {@link RemoteRef#invoke invoke} 方法，传递 <code>proxy</code>、<code>method</code>、<code>args</code> 和 <code>method</code> 的方法哈希（定义在“Java 远程方法调用 (RMI) 规范”的第 8.3 节），并返回结果。
     *
     * <li>如果 <code>RemoteRef.invoke</code> 抛出异常且该异常不是 <code>proxy</code> 类实现的方法的 <code>throws</code> 子句中声明的任何异常，则该异常将被包装在 {@link UnexpectedException} 中并抛出包装后的异常。否则，抛出 <code>invoke</code> 抛出的异常。
     * </ul>
     *
     * <p>如果参数不能由包含此调用处理程序的某些有效动态代理类的实例生成，则此方法的行为未指定。
     *
     * @param proxy 调用方法的代理实例
     * @param method 与在代理实例上调用的接口方法对应的 <code>Method</code> 实例
     * @param args 包含传递给代理实例上方法调用的参数值的对象数组，如果方法没有参数，则为 <code>null</code>
     * @return 从代理实例上的方法调用返回的值
     * @throws  Throwable 从代理实例上的方法调用抛出的异常
     **/
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {
        if (! Proxy.isProxyClass(proxy.getClass())) {
            throw new IllegalArgumentException("not a proxy");
        }

        if (Proxy.getInvocationHandler(proxy) != this) {
            throw new IllegalArgumentException("handler mismatch");
        }

        if (method.getDeclaringClass() == Object.class) {
            return invokeObjectMethod(proxy, method, args);
        } else if ("finalize".equals(method.getName()) && method.getParameterCount() == 0 &&
            !allowFinalizeInvocation) {
            return null; // 忽略
        } else {
            return invokeRemoteMethod(proxy, method, args);
        }
    }

    /**
     * 处理 java.lang.Object 方法。
     **/
    private Object invokeObjectMethod(Object proxy,
                                      Method method,
                                      Object[] args)
    {
        String name = method.getName();

        if (name.equals("hashCode")) {
            return hashCode();

        } else if (name.equals("equals")) {
            Object obj = args[0];
            InvocationHandler hdlr;
            return
                proxy == obj ||
                (obj != null &&
                 Proxy.isProxyClass(obj.getClass()) &&
                 (hdlr = Proxy.getInvocationHandler(obj)) instanceof RemoteObjectInvocationHandler &&
                 this.equals(hdlr));

        } else if (name.equals("toString")) {
            return proxyToString(proxy);

        } else {
            throw new IllegalArgumentException(
                "unexpected Object method: " + method);
        }
    }

    /**
     * 处理远程方法。
     **/
    private Object invokeRemoteMethod(Object proxy,
                                      Method method,
                                      Object[] args)
        throws Exception
    {
        try {
            if (!(proxy instanceof Remote)) {
                throw new IllegalArgumentException(
                    "proxy not Remote instance");
            }

            // 验证该方法是在扩展 Remote 的接口上声明的
            Class<?> decl = method.getDeclaringClass();
            if (!Remote.class.isAssignableFrom(decl)) {
                throw new RemoteException("Method is not Remote: " + decl + "::" + method);
            }

            return ref.invoke((Remote) proxy, method, args,
                              getMethodHash(method));
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) {
                Class<?> cl = proxy.getClass();
                try {
                    method = cl.getMethod(method.getName(),
                                          method.getParameterTypes());
                } catch (NoSuchMethodException nsme) {
                    throw (IllegalArgumentException)
                        new IllegalArgumentException().initCause(nsme);
                }
                Class<?> thrownType = e.getClass();
                for (Class<?> declaredType : method.getExceptionTypes()) {
                    if (declaredType.isAssignableFrom(thrownType)) {
                        throw e;
                    }
                }
                e = new UnexpectedException("unexpected exception", e);
            }
            throw e;
        }
    }

    /**
     * 返回使用此调用处理程序的代理的字符串表示形式。
     **/
    private String proxyToString(Object proxy) {
        Class<?>[] interfaces = proxy.getClass().getInterfaces();
        if (interfaces.length == 0) {
            return "Proxy[" + this + "]";
        }
        String iface = interfaces[0].getName();
        if (iface.equals("java.rmi.Remote") && interfaces.length > 1) {
            iface = interfaces[1].getName();
        }
        int dot = iface.lastIndexOf('.');
        if (dot >= 0) {
            iface = iface.substring(dot + 1);
        }
        return "Proxy[" + iface + "," + this + "]";
    }

    /**
     * 无条件抛出 InvalidObjectException。
     **/
    private void readObjectNoData() throws InvalidObjectException {
        throw new InvalidObjectException("no data in stream; class: " +
                                         this.getClass().getName());
    }

    /**
     * 返回指定方法的方法哈希。后续调用传递相同方法参数的 "getMethodHash" 应该更快，因为此方法内部缓存了方法到方法哈希的映射。方法哈希使用 "computeMethodHash" 方法计算。
     *
     * @param method 远程方法
     * @return 指定方法的方法哈希
     */
    private static long getMethodHash(Method method) {
        return methodToHash_Maps.get(method.getDeclaringClass()).get(method);
    }

    /**
     * 一个弱哈希映射，将类映射到弱哈希映射，后者将方法对象映射到方法哈希。
     **/
    private static class MethodToHash_Maps
        extends WeakClassHashMap<Map<Method,Long>>
    {
        MethodToHash_Maps() {}

        protected Map<Method,Long> computeValue(Class<?> remoteClass) {
            return new WeakHashMap<Method,Long>() {
                public synchronized Long get(Object key) {
                    Long hash = super.get(key);
                    if (hash == null) {
                        Method method = (Method) key;
                        hash = Util.computeMethodHash(method);
                        put(method, hash);
                    }
                    return hash;
                }
            };
        }
    }
}
