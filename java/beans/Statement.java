/*
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
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
package java.beans;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import com.sun.beans.finder.ClassFinder;
import com.sun.beans.finder.ConstructorFinder;
import com.sun.beans.finder.MethodFinder;
import sun.reflect.misc.MethodUtil;

/**
 * 一个 <code>Statement</code> 对象表示一个基本语句，其中单个方法应用于目标和一组参数 - 例如 <code>"a.setFoo(b)"</code>。
 * 请注意，此示例中使用名称表示目标及其参数，而语句对象不需要命名空间，并使用值本身构造。
 * 语句对象将命名方法与其环境关联为一组简单的值：目标和参数值数组。
 *
 * @since 1.4
 *
 * @author Philip Milne
 */
public class Statement {

    private static Object[] emptyArray = new Object[]{};

    static ExceptionListener defaultExceptionListener = new ExceptionListener() {
        public void exceptionThrown(Exception e) {
            System.err.println(e);
            // e.printStackTrace();
            System.err.println("继续...");
        }
    };

    private final AccessControlContext acc = AccessController.getContext();
    private final Object target;
    private final String methodName;
    private final Object[] arguments;
    ClassLoader loader;

    /**
     * 创建一个新的 {@link Statement} 对象，用于指定的目标对象调用由名称和参数数组指定的方法。
     * <p>
     * {@code target} 和 {@code methodName} 的值不应为 {@code null}。
     * 否则，尝试执行此 {@code Expression} 将导致 {@code NullPointerException}。
     * 如果 {@code arguments} 值为 {@code null}，则使用空数组作为 {@code arguments} 属性的值。
     *
     * @param target  该语句的目标对象
     * @param methodName  要在指定目标上调用的方法的名称
     * @param arguments  要调用指定方法的参数数组
     */
    @ConstructorProperties({"target", "methodName", "arguments"})
    public Statement(Object target, String methodName, Object[] arguments) {
        this.target = target;
        this.methodName = methodName;
        this.arguments = (arguments == null) ? emptyArray : arguments.clone();
    }

    /**
     * 返回此语句的目标对象。
     * 如果此方法返回 {@code null}，则 {@link #execute} 方法
     * 将抛出 {@code NullPointerException}。
     *
     * @return 此语句的目标对象
     */
    public Object getTarget() {
        return target;
    }

    /**
     * 返回要调用的方法的名称。
     * 如果此方法返回 {@code null}，则 {@link #execute} 方法
     * 将抛出 {@code NullPointerException}。
     *
     * @return 方法的名称
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * 返回要调用的方法的参数。
     * 参数的数量和类型必须与要调用的方法匹配。
     * {@code null} 可以用作空数组的同义词。
     *
     * @return 参数数组
     */
    public Object[] getArguments() {
        return this.arguments.clone();
    }

    /**
     * {@code execute} 方法查找名称与 {@code methodName} 属性相同的 方法，并在目标上调用该方法。
     *
     * 当目标类定义了许多具有给定名称的方法时，实现应使用 Java 语言规范中指定的算法
     * (15.11) 选择最具体的方法。使用目标和参数的动态类代替编译时类型信息，并且像
     * {@link java.lang.reflect.Method} 类本身一样，处理原始值和其关联的包装类之间的转换。
     * <p>
     * 以下方法类型作为特殊情况处理：
     * <ul>
     * <li>
     * 可以使用类对象作为目标调用静态方法。
     * <li>
     * 可以使用保留的方法名称 "new" 调用类的构造函数，就像所有类都定义了静态 "new" 方法一样。构造函数调用通常被认为是 {@code Expression} 而不是 {@code Statement}，
     * 因为它们返回一个值。
     * <li>
     * 定义在 {@link java.util.List} 接口中的方法名称 "get" 和 "set" 也可以应用于数组实例，映射到 {@code Array} 类中同名的静态方法。
     * </ul>
     *
     * @throws NullPointerException 如果 {@code target} 或 {@code methodName} 属性的值为 {@code null}
     * @throws NoSuchMethodException 如果未找到匹配的方法
     * @throws SecurityException 如果存在安全管理者并且它拒绝方法调用
     * @throws Exception 由调用的方法抛出的异常
     *
     * @see java.lang.reflect.Method
     */
    public void execute() throws Exception {
        invoke();
    }

    Object invoke() throws Exception {
        AccessControlContext acc = this.acc;
        if ((acc == null) && (System.getSecurityManager() != null)) {
            throw new SecurityException("AccessControlContext 未设置");
        }
        try {
            return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Object>() {
                        public Object run() throws Exception {
                            return invokeInternal();
                        }
                    },
                    acc
            );
        }
        catch (PrivilegedActionException exception) {
            throw exception.getException();
        }
    }

    private Object invokeInternal() throws Exception {
        Object target = getTarget();
        String methodName = getMethodName();

        if (target == null || methodName == null) {
            throw new NullPointerException((target == null ? "target" :
                                            "methodName") + " 不应为 null");
        }

        Object[] arguments = getArguments();
        if (arguments == null) {
            arguments = emptyArray;
        }
        // Class.forName() 不会从核心类外部加载类。特殊情况处理此方法。
        if (target == Class.class && methodName.equals("forName")) {
            return ClassFinder.resolveClass((String)arguments[0], this.loader);
        }
        Class<?>[] argClasses = new Class<?>[arguments.length];
        for(int i = 0; i < arguments.length; i++) {
            argClasses[i] = (arguments[i] == null) ? null : arguments[i].getClass();
        }

        AccessibleObject m = null;
        if (target instanceof Class) {
            /*
            对于类方法，通过将实际类的静态方法与 "Class.class" 的实例方法
            以及构造函数定义的重载 "newInstance" 方法的并集来模拟元类的效果。
            这样，例如 "System.class" 将执行 "Class.class" 中定义的静态方法 getProperties()
            和实例方法 getSuperclass()。
            */
            if (methodName.equals("new")) {
                methodName = "newInstance";
            }
            // 通过伪造一个 nary 构造函数来提供数组实例化的简短形式。
            if (methodName.equals("newInstance") && ((Class)target).isArray()) {
                Object result = Array.newInstance(((Class)target).getComponentType(), arguments.length);
                for(int i = 0; i < arguments.length; i++) {
                    Array.set(result, i, arguments[i]);
                }
                return result;
            }
            if (methodName.equals("newInstance") && arguments.length != 0) {
                // 从 1.4 开始，Character 类没有一个接受 String 的构造函数。所有其他 Java 原始类型的包装类
                // 都有一个 String 构造函数，因此在这里伪造一个这样的构造函数，以便可以忽略这种特殊情况。
                if (target == Character.class && arguments.length == 1 &&
                    argClasses[0] == String.class) {
                    return new Character(((String)arguments[0]).charAt(0));
                }
                try {
                    m = ConstructorFinder.findConstructor((Class)target, argClasses);
                }
                catch (NoSuchMethodException exception) {
                    m = null;
                }
            }
            if (m == null && target != Class.class) {
                m = getMethod((Class)target, methodName, argClasses);
            }
            if (m == null) {
                m = getMethod(Class.class, methodName, argClasses);
            }
        }
        else {
            /*
            特殊处理数组不是必需的，但会使涉及数组的文件更短，并简化归档基础设施。
            Array.set() 方法引入了一个不寻常的概念 - 静态方法更改实例的状态。通常，具有对象副作用的语句
            是对象本身的实例方法，我们通过特殊处理数组暂时恢复这一规则。
            */
            if (target.getClass().isArray() &&
                (methodName.equals("set") || methodName.equals("get"))) {
                int index = ((Integer)arguments[0]).intValue();
                if (methodName.equals("get")) {
                    return Array.get(target, index);
                }
                else {
                    Array.set(target, index, arguments[1]);
                    return null;
                }
            }
            m = getMethod(target.getClass(), methodName, argClasses);
        }
        if (m != null) {
            try {
                if (m instanceof Method) {
                    return MethodUtil.invoke((Method)m, target, arguments);
                }
                else {
                    return ((Constructor)m).newInstance(arguments);
                }
            }
            catch (IllegalAccessException iae) {
                throw new Exception("Statement 无法调用: " +
                                    methodName + " on " + target.getClass(),
                                    iae);
            }
            catch (InvocationTargetException ite) {
                Throwable te = ite.getTargetException();
                if (te instanceof Exception) {
                    throw (Exception)te;
                }
                else {
                    throw ite;
                }
            }
        }
        throw new NoSuchMethodException(toString());
    }

    String instanceName(Object instance) {
        if (instance == null) {
            return "null";
        } else if (instance.getClass() == String.class) {
            return "\""+(String)instance + "\"";
        } else {
            // 注意：使用非缓存的 NameGenerator 方法有一个小问题。返回值不会包含有关内部类名称的具体信息。例如，
            // 在 1.4.2 中，内部类将表示为 JList$1，现在将被命名为 Class。

            return NameGenerator.unqualifiedClassName(instance.getClass());
        }
    }

    /**
     * 使用 Java 风格的语法打印此语句的值。
     */
    public String toString() {
        // 尊重子类的实现。
        Object target = getTarget();
        String methodName = getMethodName();
        Object[] arguments = getArguments();
        if (arguments == null) {
            arguments = emptyArray;
        }
        StringBuffer result = new StringBuffer(instanceName(target) + "." + methodName + "(");
        int n = arguments.length;
        for(int i = 0; i < n; i++) {
            result.append(instanceName(arguments[i]));
            if (i != n -1) {
                result.append(", ");
            }
        }
        result.append(");");
        return result.toString();
    }

    static Method getMethod(Class<?> type, String name, Class<?>... args) {
        try {
            return MethodFinder.findMethod(type, name, args);
        }
        catch (NoSuchMethodException exception) {
            return null;
        }
    }
}
