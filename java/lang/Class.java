/*
 * Copyright (c) 1994, 2014, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Field;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.AnnotatedType;
import java.lang.ref.SoftReference;
import java.io.InputStream;
import java.io.ObjectStreamField;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import sun.misc.Unsafe;
import sun.reflect.CallerSensitive;
import sun.reflect.ConstantPool;
import sun.reflect.Reflection;
import sun.reflect.ReflectionFactory;
import sun.reflect.generics.factory.CoreReflectionFactory;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.repository.ClassRepository;
import sun.reflect.generics.repository.MethodRepository;
import sun.reflect.generics.repository.ConstructorRepository;
import sun.reflect.generics.scope.ClassScope;
import sun.security.util.SecurityConstants;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import sun.reflect.annotation.*;
import sun.reflect.misc.ReflectUtil;

/**
 * {@code Class} 类的实例表示运行中的 Java 应用程序中的类和接口。枚举是一种类，注解是一种接口。每个数组也属于一个类，该类被表示为一个 {@code Class} 对象，
 * 该对象由具有相同元素类型和维度的所有数组共享。Java 的基本类型（{@code boolean}、{@code byte}、{@code char}、{@code short}、
 * {@code int}、{@code long}、{@code float} 和 {@code double}）以及关键字 {@code void} 也被表示为 {@code Class} 对象。
 *
 * <p> {@code Class} 没有公共构造函数。相反，{@code Class} 对象由 Java 虚拟机在加载类时自动构造，或通过类加载器中的 {@code defineClass} 方法调用构造。
 *
 * <p> 下面的示例使用 {@code Class} 对象打印对象的类名称：
 *
 * <blockquote><pre>
 *     void printClassName(Object obj) {
 *         System.out.println("对象 " + obj + " 的类是 " + obj.getClass().getName());
 *     }
 * </pre></blockquote>
 *
 * <p> 也可以通过类字面量获取命名的类型（或 void）的 {@code Class} 对象。参见 <cite>Java™ 语言规范</cite> 的第 15.8.2 节。
 * 例如：
 *
 * <blockquote>
 *     {@code System.out.println("类 Foo 的名称是：" + Foo.class.getName());}
 * </blockquote>
 *
 * @param <T> 由此 {@code Class} 对象建模的类的类型。例如，{@code String.class} 的类型是 {@code Class<String>}。
 *            如果建模的类未知，则使用 {@code Class<?>}。
 *
 * @author  未署名
 * @see     java.lang.ClassLoader#defineClass(byte[], int, int)
 * @since   JDK1.0
 */
public final class Class<T> implements java.io.Serializable,
                              GenericDeclaration,
                              Type,
                              AnnotatedElement {
    private static final int ANNOTATION= 0x00002000;
    private static final int ENUM      = 0x00004000;
    private static final int SYNTHETIC = 0x00001000;

    private static native void registerNatives();
    static {
        registerNatives();
    }

    /*
     * 私有构造函数。只有 Java 虚拟机创建 Class 对象。
     * 此构造函数未被使用，防止生成默认构造函数。
     */
    private Class(ClassLoader loader) {
        // 初始化类加载器的最终字段。非空初始化值防止未来的 JIT 优化假设此最终字段为 null。
        classLoader = loader;
    }

    /**
     * 将对象转换为字符串。字符串表示形式为字符串 "class" 或 "interface"，后跟一个空格，然后是类的完全限定名称，格式由 {@code getName} 返回。
     * 如果此 {@code Class} 对象表示基本类型，则此方法返回基本类型的名称。如果此 {@code Class} 对象表示 void，则此方法返回 "void"。
     *
     * @return 此类对象的字符串表示形式。
     */
    public String toString() {
        return (isInterface() ? "interface " : (isPrimitive() ? "" : "class "))
            + getName();
    }

    /**
     * 返回描述此 {@code Class} 的字符串，包括修饰符和类型参数的信息。
     *
     * 字符串格式为：首先列出类型修饰符（如果有），然后是类型种类（对于基本类型为空字符串，对于类是 {@code class}，枚举是 {@code enum}，接口是 {@code interface}，
     * 或对于注解接口是 <code>@</code>{@code interface}），接着是类型的名称，然后是一个用尖括号括起来的、逗号分隔的类型参数列表（如果有）。
     *
     * 修饰符之间以及修饰符与类型种类之间使用空格分隔。修饰符按规范顺序排列。如果没有类型参数，则省略类型参数列表。
     *
     * <p> 请注意，由于生成的是关于类型运行时表示的信息，可能会出现源代码中不存在或在源代码中非法的修饰符。
     *
     * @return 描述此 {@code Class} 的字符串，包括修饰符和类型参数的信息
     *
     * @since 1.8
     */
    public String toGenericString() {
        if (isPrimitive()) {
            return toString();
        } else {
            StringBuilder sb = new StringBuilder();

            // 类修饰符是接口修饰符的超集
            int modifiers = getModifiers() & Modifier.classModifiers();
            if (modifiers != 0) {
                sb.append(Modifier.toString(modifiers));
                sb.append(' ');
            }

            if (isAnnotation()) {
                sb.append('@');
            }
            if (isInterface()) { // 注意：所有注解类型都是接口
                sb.append("interface");
            } else {
                if (isEnum())
                    sb.append("enum");
                else
                    sb.append("class");
            }
            sb.append(' ');
            sb.append(getName());

            TypeVariable<?>[] typeparms = getTypeParameters();
            if (typeparms.length > 0) {
                boolean first = true;
                sb.append('<');
                for(TypeVariable<?> typeparm: typeparms) {
                    if (!first)
                        sb.append(',');
                    sb.append(typeparm.getTypeName());
                    first = false;
                }
                sb.append('>');
            }

            return sb.toString();
        }
    }

    /**
     * 返回与具有给定字符串名称的类或接口关联的 {@code Class} 对象。调用此方法等同于：
     *
     * <blockquote>
     *  {@code Class.forName(className, true, currentLoader)}
     * </blockquote>
     *
     * 其中 {@code currentLoader} 表示当前类的定义类加载器。
     *
     * <p> 例如，以下代码片段返回名为 {@code java.lang.Thread} 的类的运行时 {@code Class} 描述符：
     *
     * <blockquote>
     *   {@code Class t = Class.forName("java.lang.Thread")}
     * </blockquote>
     * <p>
     * 调用 {@code forName("X")} 会导致名为 {@code X} 的类被初始化。
     *
     * @param      className   所需类的完全限定名称。
     * @return     表示具有指定名称的类的 {@code Class} 对象。
     * @exception LinkageError 如果链接失败
     * @exception ExceptionInInitializerError 如果由此方法触发的初始化失败
     * @exception ClassNotFoundException 如果无法找到该类
     */
    @CallerSensitive
    public static Class<?> forName(String className)
                throws ClassNotFoundException {
        Class<?> caller = Reflection.getCallerClass();
        return forName0(className, true, ClassLoader.getClassLoader(caller), caller);
    }

    /**
     * 返回与具有给定字符串名称的类或接口关联的 {@code Class} 对象，使用给定的类加载器。
     * 给定类或接口的完全限定名称（格式与 {@code getName} 返回的相同），此方法尝试定位、加载和链接该类或接口。
     * 使用指定的类加载器加载类或接口。如果参数 {@code loader} 为 null，则通过引导类加载器加载类。
     * 仅当 {@code initialize} 参数为 {@code true} 且类尚未初始化时，类才会被初始化。
     *
     * <p> 如果 {@code name} 表示基本类型或 void，将尝试在未命名包中定位用户定义的名为 {@code name} 的类。
     * 因此，此方法不能用于获取表示基本类型或 void 的任何 {@code Class} 对象。
     *
     * <p> 如果 {@code name} 表示数组类，则加载数组类的组件类型，但不初始化。
     *
     * <p> 例如，在实例方法中，表达式：
     *
     * <blockquote>
     *  {@code Class.forName("Foo")}
     * </blockquote>
     *
     * 等同于：
     *
     * <blockquote>
     *  {@code Class.forName("Foo", true, this.getClass().getClassLoader())}
     * </blockquote>
     *
     * 请注意，此方法会抛出与加载、链接或初始化相关的错误，如 <em>Java 语言规范</em> 的第 12.2、12.3 和 12.4 节所述。
     * 请注意，此方法不会检查请求的类是否对调用者可访问。
     *
     * <p> 如果 {@code loader} 为 {@code null}，且存在安全管理器，且调用者的类加载器不为 null，则此方法会调用安全管理器的 {@code checkPermission} 方法，
     * 使用 {@code RuntimePermission("getClassLoader")} 权限，以确保可以访问引导类加载器。
     *
     * @param name       所需类的完全限定名称
     * @param initialize 如果为 {@code true}，类将被初始化。
     *                   参见 <em>Java 语言规范</em> 的第 12.4 节。
     * @param loader     用于加载类的类加载器
     * @return           表示所需类的类对象
     *
     * @exception LinkageError 如果链接失败
     * @exception ExceptionInInitializerError 如果由此方法触发的初始化失败
     * @exception ClassNotFoundException 如果指定的类加载器无法找到该类
     *
     * @see       java.lang.Class#forName(String)
     * @see       java.lang.ClassLoader
     * @since     1.2
     */
    @CallerSensitive
    public static Class<?> forName(String name, boolean initialize,
                                   ClassLoader loader)
        throws ClassNotFoundException
    {
        Class<?> caller = null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            // 仅当存在安全管理器时才需要通过反射调用获取调用者类。
            // 否则避免此调用的开销。
            caller = Reflection.getCallerClass();
            if (sun.misc.VM.isSystemDomainLoader(loader)) {
                ClassLoader ccl = ClassLoader.getClassLoader(caller);
                if (!sun.misc.VM.isSystemDomainLoader(ccl)) {
                    sm.checkPermission(
                        SecurityConstants.GET_CLASSLOADER_PERMISSION);
                }
            }
        }
        return forName0(name, initialize, loader, caller);
    }

    /** 在进行系统加载器访问检查后调用。 */
    private static native Class<?> forName0(String name, boolean initialize,
                                            ClassLoader loader,
                                            Class<?> caller)
        throws ClassNotFoundException;

    /**
     * 创建由此 {@code Class} 对象表示的类的新实例。类如同通过带有空参数列表的 {@code new} 表达式实例化。
     * 如果类尚未初始化，则会进行初始化。
     *
     * <p> 请注意，此方法会传播由无参构造函数抛出的任何异常，包括受检异常。
     * 使用此方法实际上绕过了编译器通常执行的编译时异常检查。
     * {@link java.lang.reflect.Constructor#newInstance(java.lang.Object...) Constructor.newInstance} 方法通过将构造函数抛出的任何异常包装在（受检的）{@link java.lang.reflect.InvocationTargetException} 中避免了此问题。
     *
     * @return  由此对象表示的类的新分配实例。
     * @throws  IllegalAccessException  如果类或其无参构造函数不可访问。
     * @throws  InstantiationException
     *          如果此 {@code Class} 表示抽象类、接口、数组类、基本类型或 void；
     *          或如果类没有无参构造函数；
     *          或由于其他原因实例化失败。
     * @throws  ExceptionInInitializerError 如果由此方法触发的初始化失败。
     * @throws  SecurityException
     *          如果存在安全管理器 <i>s</i>，且满足以下任一条件：
     *          <ul>
     *          <li> 调用者的类加载器与当前类的类加载器不同或不是其祖先，
     *          且调用 {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} 拒绝访问此类的包
     *          </ul>
     */
    @CallerSensitive
    public T newInstance()
        throws InstantiationException, IllegalAccessException
    {
        if (System.getSecurityManager() != null) {
            checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), false);
        }

        // 注意：以下代码在当前 Java 内存模型下可能并非严格正确。

        // 构造函数查找
        if (cachedConstructor == null) {
            if (this == Class.class) {
                throw new IllegalAccessException(
                    "无法在 java.lang.Class 的 Class 上调用 newInstance()"
                );
            }
            try {
                Class<?>[] empty = {};
                final Constructor<T> c = getConstructor0(empty, Member.DECLARED);
                // 在构造函数上禁用访问检查
                // 因为我们无论如何都要在这里进行安全检查
                //（构造函数的安全检查栈深度不正确）
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<Void>() {
                        public Void run() {
                                c.setAccessible(true);
                                return null;
                            }
                        });
                cachedConstructor = c;
            } catch (NoSuchMethodException e) {
                throw (InstantiationException)
                    new InstantiationException(getName()).initCause(e);
            }
        }
        Constructor<T> tmpConstructor = cachedConstructor;
        // 安全检查（与 java.lang.reflect.Constructor 相同）
        int modifiers = tmpConstructor.getModifiers();
        if (!Reflection.quickCheckMemberAccess(this, modifiers)) {
            Class<?> caller = Reflection.getCallerClass();
            if (newInstanceCallerCache != caller) {
                Reflection.ensureMemberAccess(caller, this, null, modifiers);
                newInstanceCallerCache = caller;
            }
        }
        // 运行构造函数
        try {
            return tmpConstructor.newInstance((Object[])null);
        } catch (InvocationTargetException e) {
            Unsafe.getUnsafe().throwException(e.getTargetException());
            // 不会到达
            return null;
        }
    }
    private volatile transient Constructor<T> cachedConstructor;
    private volatile transient Class<?>       newInstanceCallerCache;

    /**
     * 确定指定的 {@code Object} 是否与由此 {@code Class} 对象表示的对象具有赋值兼容性。
     * 此方法是 Java 语言 {@code instanceof} 操作符的动态等价物。如果指定的 {@code Object} 参数非空且可以转换为由此 {@code Class} 对象表示的引用类型而不会引发 {@code ClassCastException}，则方法返回 {@code true}。否则返回 {@code false}。
     *
     * <p> 具体来说，如果此 {@code Class} 对象表示一个声明的类，则当指定的 {@code Object} 参数是表示的类（或其任何子类）的实例时，此方法返回 {@code true}；否则返回 {@code false}。
     * 如果此 {@code Class} 对象表示一个数组类，则当指定的 {@code Object} 参数可以通过同一性转换或拓宽引用转换转换为数组类的对象时，此方法返回 {@code true}；否则返回 {@code false}。
     * 如果此 {@code Class} 对象表示一个接口，则当指定的 {@code Object} 参数的类或任何超类实现此接口时，此方法返回 {@code true}；否则返回 {@code false}。
     * 如果此 {@code Class} 对象表示基本类型，则此方法返回 {@code false}。
     *
     * @param   obj 要检查的对象
     * @return  如果 {@code obj} 是此类的实例，则返回 true
     *
     * @since JDK1.1
     */
    public native boolean isInstance(Object obj);

    /**
     * 确定由此 {@code Class} 对象表示的类或接口是否与指定的 {@code Class} 参数表示的类或接口相同，或是其超类或超接口。如果是，则返回 {@code true}；否则返回 {@code false}。
     * 如果此 {@code Class} 对象表示基本类型，则仅当指定的 {@code Class} 参数正是此 {@code Class} 对象时，此方法返回 {@code true}；否则返回 {@code false}。
     *
     * <p> 具体来说，此方法测试指定的 {@code Class} 参数表示的类型是否可以通过同一性转换或拓宽引用转换转换为由此 {@code Class} 对象表示的类型。
     * 详情参见 <em>Java 语言规范</em> 的第 5.1.1 和 5.1.4 节。
     *
     * @param cls 要检查的 {@code Class} 对象
     * @return 表示 {@code cls} 类型的对象是否可以赋给此类的对象的 {@code boolean} 值
     * @exception NullPointerException 如果指定的 Class 参数为 null。
     * @since JDK1.1
     */
    public native boolean isAssignableFrom(Class<?> cls);

    /**
     * 确定指定的 {@code Class} 对象是否表示接口类型。
     *
     * @return  如果此对象表示接口，则返回 {@code true}；否则返回 {@code false}。
     */
    public native boolean isInterface();

    /**
     * 确定此 {@code Class} 对象是否表示数组类。
     *
     * @return  如果此对象表示数组类，则返回 {@code true}；否则返回 {@code false}。
     * @since   JDK1.1
     */
    public native boolean isArray();

    /**
     * 确定指定的 {@code Class} 对象是否表示基本类型。
     *
     * <p> 有九个预定义的 {@code Class} 对象表示八个基本类型和 void。这些由 Java 虚拟机创建，名称与它们表示的基本类型相同，
     * 即 {@code boolean}、{@code byte}、{@code char}、{@code short}、{@code int}、{@code long}、{@code float} 和 {@code double}。
     *
     * <p> 这些对象只能通过以下公共静态最终变量访问，并且是唯一返回 {@code true} 的 {@code Class} 对象。
     *
     * @return  当且仅当此类表示基本类型时返回 true
     *
     * @see     java.lang.Boolean#TYPE
     * @see     java.lang.Character#TYPE
     * @see     java.lang.Byte#TYPE
     * @see     java.lang.Short#TYPE
     * @see     java.lang.Integer#TYPE
     * @see     java.lang.Long#TYPE
     * @see     java.lang.Float#TYPE
     * @see     java.lang.Double#TYPE
     * @see     java.lang.Void#TYPE
     * @since JDK1.1
     */
    public native boolean isPrimitive();

    /**
     * 如果此 {@code Class} 对象表示注解类型，则返回 true。请注意，如果此方法返回 true，则 {@link #isInterface()} 也将返回 true，因为所有注解类型也是接口。
     *
     * @return 如果此类对象表示注解类型，则返回 {@code true}；否则返回 {@code false}
     * @since 1.5
     */
    public boolean isAnnotation() {
        return (getModifiers() & ANNOTATION) != 0;
    }

    /**
     * 如果此类是合成类，则返回 {@code true}；否则返回 {@code false}。
     * @return 当且仅当此类是 <em>Java 语言规范</em> 定义的合成类时，返回 {@code true}。
     * @jls 13.1 二进制格式
     * @since 1.5
     */
    public boolean isSynthetic() {
        return (getModifiers() & SYNTHETIC) != 0;
    }

    /**
     * 返回由此 {@code Class} 对象表示的实体（类、接口、数组类、基本类型或 void）的名称，作为 {@code String}。
     *
     * <p> 如果此类对象表示非数组类型的引用类型，则返回类的二进制名称，如 <cite>Java™ 语言规范</cite> 所述。
     *
     * <p> 如果此类对象表示基本类型或 void，则返回的名称是与基本类型或 void 对应的 Java 语言关键字的 {@code String}。
     *
     * <p> 如果此类对象表示数组类，则名称的内部形式由元素类型的名称组成，前面有一个或多个 '{@code [}' 字符，表示数组嵌套的深度。元素类型名称的编码如下：
     *
     * <blockquote><table summary="元素类型和编码">
     * <tr><th> 元素类型 <th> &nbsp;&nbsp;&nbsp; <th> 编码
     * <tr><td> boolean      <td> &nbsp;&nbsp;&nbsp; <td align=center> Z
     * <tr><td> byte         <td> &nbsp;&nbsp;&nbsp; <td align=center> B
     * <tr><td> char         <td> &nbsp;&nbsp;&nbsp; <td align=center> C
     * <tr><td> 类或接口
     *                       <td> &nbsp;&nbsp;&nbsp; <td align=center> L<i>类名</i>;
     * <tr><td> double       <td> &nbsp;&nbsp;&nbsp; <td align=center> D
     * <tr><td> float        <td> &nbsp;&nbsp;&nbsp; <td align=center> F
     * <tr><td> int          <td> &nbsp;&nbsp;&nbsp; <td align=center> I
     * <tr><td> long         <td> &nbsp;&nbsp;&nbsp; <td align=center> J
     * <tr><td> short        <td> &nbsp;&nbsp;&nbsp; <td align=center> S
     * </table></blockquote>
     *
     * <p> 类或接口名称 <i>类名</i> 是上述指定的类的二进制名称。
     *
     * <p> 示例：
     * <blockquote><pre>
     * String.class.getName()
     *     返回 "java.lang.String"
     * byte.class.getName()
     *     返回 "byte"
     * (new Object[3]).getClass().getName()
     *     返回 "[Ljava.lang.Object;"
     * (new int[3][4][5][6][7][8][9]).getClass().getName()
     *     返回 "[[[[[[[I"
     * </pre></blockquote>
     *
     * @return  由此对象表示的类或接口的名称。
     */
    public String getName() {
        String name = this.name;
        if (name == null)
            this.name = name = getName0();
        return name;
    }

    // 缓存名称以减少对虚拟机的调用
    private transient String name;
    private native String getName0();

    /**
     * 返回此类的类加载器。某些实现可能使用 null 表示引导类加载器。如果此类由引导类加载器加载，则此方法将返回 null。
     *
     * <p> 如果存在安全管理器，且调用者的类加载器不为 null 且与请求的类的类加载器不同或不是其祖先，则此方法会调用安全管理器的 {@code checkPermission} 方法，
     * 使用 {@code RuntimePermission("getClassLoader")} 权限，以确保可以访问类的类加载器。
     *
     * <p> 如果此对象表示基本类型或 void，则返回 null。
     *
     * @return  加载由此对象表示的类或接口的类加载器。
     * @throws SecurityException
     *    如果存在安全管理器且其 {@code checkPermission} 方法拒绝访问此类的类加载器。
     * @see java.lang.ClassLoader
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     */
    @CallerSensitive
    public ClassLoader getClassLoader() {
        ClassLoader cl = getClassLoader0();
        if (cl == null)
            return null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader.checkClassLoaderPermission(cl, Reflection.getCallerClass());
        }
        return cl;
    }

    // 包私有以允许 ClassLoader 访问
    ClassLoader getClassLoader0() { return classLoader; }

    // 由 JVM 初始化，而非私有构造函数
    // 此字段被过滤，防止反射访问，即 getDeclaredField 将抛出 NoSuchFieldException
    private final ClassLoader classLoader;

    /**
     * 返回表示由此 {@code GenericDeclaration} 对象表示的泛型声明所声明的类型变量的 {@code TypeVariable} 对象数组，按声明顺序排列。
     * 如果底层的泛型声明未声明类型变量，则返回长度为 0 的数组。
     *
     * @return 表示由此泛型声明声明的类型变量的 {@code TypeVariable} 对象数组
     * @throws java.lang.reflect.GenericSignatureFormatError 如果泛型签名不符合 <cite>Java™ 虚拟机规范</cite> 指定的格式
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public TypeVariable<Class<T>>[] getTypeParameters() {
        ClassRepository info = getGenericInfo();
        if (info != null)
            return (TypeVariable<Class<T>>[])info.getTypeParameters();
        else
            return (TypeVariable<Class<T>>[])new TypeVariable<?>[0];
    }

    /**
     * 返回表示由此 {@code Class} 表示的实体（类、接口、基本类型或 void）的超类的 {@code Class}。
     * 如果此 {@code Class} 表示 {@code Object} 类、接口、基本类型或 void，则返回 null。
     * 如果此对象表示数组类，则返回表示 {@code Object} 类的 {@code Class} 对象。
     *
     * @return 由此对象表示的类的超类。
     */
    public native Class<? super T> getSuperclass();

    /**
     * 返回表示由此 {@code Class} 表示的实体（类、接口、基本类型或 void）的直接超类的 {@code Type}。
     *
     * <p> 如果超类是参数化类型，则返回的 {@code Type} 对象必须准确反映源代码中使用的实际类型参数。
     * 如果之前未创建表示超类的参数化类型，则会创建该类型。参见 {@link java.lang.reflect.ParameterizedType ParameterizedType} 的声明，了解参数化类型创建过程的语义。
     * 如果此 {@code Class} 表示 {@code Object} 类、接口、基本类型或 void，则返回 null。
     * 如果此对象表示数组类，则返回表示 {@code Object} 类的 {@code Class} 对象。
     *
     * @throws java.lang.reflect.GenericSignatureFormatError 如果泛型类签名不符合 <cite>Java™ 虚拟机规范</cite> 指定的格式
     * @throws TypeNotPresentException 如果泛型超类引用了不存在的类型声明
     * @throws java.lang.reflect.MalformedParameterizedTypeException 如果泛型超类引用了由于任何原因无法实例化的参数化类型
     * @return 由此对象表示的类的超类
     * @since 1.5
     */
    public Type getGenericSuperclass() {
        ClassRepository info = getGenericInfo();
        if (info == null) {
            return getSuperclass();
        }

        // 历史不规范：
        // 泛型签名将接口的超类标记为 Object，但此 API 对于接口返回 null
        if (isInterface()) {
            return null;
        }

        return info.getSuperclass();
    }

    /**
     * 获取此类的包。此类的类加载器用于查找包。如果类由引导类加载器加载，则搜索从 CLASSPATH 加载的包集合以找到该类的包。
     * 如果此类的类加载器未创建包对象，则返回 null。
     *
     * <p> 包仅在伴随类的清单中定义了版本和规范信息，并且类加载器使用清单中的属性创建了包实例时，才具有版本和规范属性。
     *
     * @return 类的包，如果无法从归档或代码库获取包信息，则返回 null。
     */
    public Package getPackage() {
        return Package.getPackage(this);
    }

    /**
     * 确定由此对象表示的类或接口实现的接口。
     *
     * <p> 如果此对象表示一个类，则返回值是一个包含表示该类实现的所有接口的对象的数组。
     * 数组中接口对象的顺序对应于由此对象表示的类的声明中的 {@code implements} 子句中的接口名称顺序。
     * 例如，给定声明：
     * <blockquote>
     * {@code class Shimmer implements FloorWax, DessertTopping { ... }}
     * </blockquote>
     * 假设 {@code s} 是 {@code Shimmer} 的实例，以下表达式的值：
     * <blockquote>
     * {@code s.getClass().getInterfaces()[0]}
     * </blockquote>
     * 是表示接口 {@code FloorWax} 的 {@code Class} 对象；以及以下表达式的值：
     * <blockquote>
     * {@code s.getClass().getInterfaces()[1]}
     * </blockquote>
     * 是表示接口 {@code DessertTopping} 的 {@code Class} 对象。
     *
     * <p> 如果此对象表示一个接口，则数组包含表示该接口直接扩展的所有接口的对象。
     * 数组中接口对象的顺序对应于由此对象表示的接口声明中的 {@code extends} 子句中的接口名称顺序。
     *
     * <p> 如果此对象表示一个未实现任何接口的类或接口，则方法返回长度为 0 的数组。
     *
     * <p> 如果此对象表示基本类型或 void，则方法返回长度为 0 的数组。
     *
     * <p> 如果此 {@code Class} 对象表示数组类型，则按顺序返回接口 {@code Cloneable} 和 {@code java.io.Serializable}。
     *
     * @return 由此类实现的接口数组。
     */
    public Class<?>[] getInterfaces() {
        ReflectionData<T> rd = reflectionData();
        if (rd == null) {
            // 无需克隆
            return getInterfaces0();
        } else {
            Class<?>[] interfaces = rd.interfaces;
            if (interfaces == null) {
                interfaces = getInterfaces0();
                rd.interfaces = interfaces;
            }
            // 在交给用户代码之前进行防御性复制
            return interfaces.clone();
        }
    }

    private native Class<?>[] getInterfaces0();

    /**
     * 返回表示由此对象表示的类或接口直接实现的接口的 {@code Type} 数组。
     *
     * <p> 如果超接口是参数化类型，则为它返回的 {@code Type} 对象必须准确反映源代码中使用的实际类型参数。
     * 如果之前未创建表示每个超接口的参数化类型，则会创建该类型。参见 {@link java.lang.reflect.ParameterizedType ParameterizedType} 的声明，了解参数化类型创建过程的语义。
     *
     * <p> 如果此对象表示一个类，则返回值是一个包含表示该类实现的所有接口的对象的数组。
     * 数组中接口对象的顺序对应于由此对象表示的类的声明中的 {@code implements} 子句中的接口名称顺序。
     * 对于数组类，按顺序返回接口 {@code Cloneable} 和 {@code Serializable}。
     *
     * <p> 如果此对象表示一个接口，则数组包含表示该接口直接扩展的所有接口的对象。
     * 数组中接口对象的顺序对应于由此对象表示的接口声明中的 {@code extends} 子句中的接口名称顺序。
     *
     * <p> 如果此对象表示一个未实现任何接口的类或接口，则方法返回长度为 0 的数组。
     *
     * <p> 如果此对象表示基本类型或 void，则方法返回长度为 0 的数组。
     *
     * @throws java.lang.reflect.GenericSignatureFormatError
     *     如果泛型类签名不符合 <cite>Java™ 虚拟机规范</cite> 指定的格式
     * @throws TypeNotPresentException 如果任何泛型超接口引用了不存在的类型声明
     * @throws java.lang.reflect.MalformedParameterizedTypeException
     *     如果任何泛型超接口引用了由于任何原因无法实例化的参数化类型
     * @return 由此类实现的接口数组
     * @since 1.5
     */
    public Type[] getGenericInterfaces() {
        ClassRepository info = getGenericInfo();
        return (info == null) ?  getInterfaces() : info.getSuperInterfaces();
    }

    /**
     * 返回表示数组的组件类型的 {@code Class}。如果此类不表示数组类，则此方法返回 null。
     *
     * @return 如果此类是数组，则返回表示此类的组件类型的 {@code Class}
     * @see     java.lang.reflect.Array
     * @since JDK1.1
     */
    public native Class<?> getComponentType();

    /**
     * 返回此类或接口的 Java 语言修饰符，以整数编码。修饰符包括 Java 虚拟机的常量：{@code public}、{@code protected}、
     * {@code private}、{@code final}、{@code static}、{@code abstract} 和 {@code interface}；应使用 {@code Modifier} 类的方法解码。
     *
     * <p> 如果底层类是数组类，则其 {@code public}、{@code private} 和 {@code protected} 修饰符与其组件类型相同。
     * 如果此 {@code Class} 表示基本类型或 void，则其 {@code public} 修饰符始终为 {@code true}，其 {@code protected} 和 {@code private} 修饰符始终为 {@code false}。
     * 如果此对象表示数组类、基本类型或 void，则其 {@code final} 修饰符始终为 {@code true}，其接口修饰符始终为 {@code false}。
     * 其他修饰符的值不由本规范确定。
     *
     * <p> 修饰符编码定义在 <em>Java 虚拟机规范</em> 的表 4.1 中。
     *
     * @return 表示此类修饰符的 {@code int}
     * @see     java.lang.reflect.Modifier
     * @since JDK1.1
     */
    public native int getModifiers();

    /**
     * 获取此类的签名者。
     *
     * @return  此类的签名者，如果没有签名者则返回 null。特别是，如果此对象表示基本类型或 void，则此方法返回 null。
     * @since   JDK1.1
     */
    public native Object[] getSigners();

    /**
     * 设置此类的签名者。
     */
    native void setSigners(Object[] signers);

    /**
     * 如果此 {@code Class} 对象表示方法中的局部或匿名类，则返回表示底层类的直接封闭方法的 {@link java.lang.reflect.Method Method} 对象。否则返回 {@code null}。
     *
     * 特别是，如果底层类是直接由类型声明、实例初始化器或静态初始化器封闭的局部或匿名类，则此方法返回 {@code null}。
     *
     * @return 如果底层类是局部或匿名类，则返回其直接封闭方法；否则返回 {@code null}。
     *
     * @throws SecurityException
     *         如果存在安全管理器 <i>s</i>，且满足以下任一条件：
     *
     *         <ul>
     *
     *         <li> 调用者的类加载器与封闭类的类加载器不同，
     *         且调用 {@link SecurityManager#checkPermission s.checkPermission} 方法
     *         使用 {@code RuntimePermission("accessDeclaredMembers")} 权限拒绝访问封闭类中的方法
     *
     *         <li> 调用者的类加载器与封闭类的类加载器不同或不是其祖先，
     *         且调用 {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} 拒绝访问封闭类的包
     *
     *         </ul>
     * @since 1.5
     */
    @CallerSensitive
    public Method getEnclosingMethod() throws SecurityException {
        EnclosingMethodInfo enclosingInfo = getEnclosingMethodInfo();

        if (enclosingInfo == null)
            return null;
        else {
            if (!enclosingInfo.isMethod())
                return null;

            MethodRepository typeInfo = MethodRepository.make(enclosingInfo.getDescriptor(),
                                                              getFactory());
            Class<?>   returnType       = toClass(typeInfo.getReturnType());
            Type []    parameterTypes   = typeInfo.getParameterTypes();
            Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];

            // 将 Types 转换为 Classes；返回的类型 *应该* 是类对象，
            // 因为使用的 methodDescriptor 不包含泛型信息
            for(int i = 0; i < parameterClasses.length; i++)
                parameterClasses[i] = toClass(parameterTypes[i]);

            // 执行访问检查
            Class<?> enclosingCandidate = enclosingInfo.getEnclosingClass();
            enclosingCandidate.checkMemberAccess(Member.DECLARED,
                                                 Reflection.getCallerClass(), true);
            /*
             * 遍历所有声明的方法；匹配方法名称、参数数量和类型，以及返回类型。
             * 由于协变返回等原因，匹配返回类型也是必要的。
             */
            for(Method m: enclosingCandidate.getDeclaredMethods()) {
                if (m.getName().equals(enclosingInfo.getName()) ) {
                    Class<?>[] candidateParamClasses = m.getParameterTypes();
                    if (candidateParamClasses.length == parameterClasses.length) {
                        boolean matches = true;
                        for(int i = 0; i < candidateParamClasses.length; i++) {
                            if (!candidateParamClasses[i].equals(parameterClasses[i])) {
                                matches = false;
                                break;
                            }
                        }

                        if (matches) { // 最后，检查返回类型
                            if (m.getReturnType().equals(returnType) )
                                return m;
                        }
                    }
                }
            }

            throw new InternalError("未找到封闭方法");
        }
    }

    private native Object[] getEnclosingMethod0();

    private EnclosingMethodInfo getEnclosingMethodInfo() {
        Object[] enclosingInfo = getEnclosingMethod0();
        if (enclosingInfo == null)
            return null;
        else {
            return new EnclosingMethodInfo(enclosingInfo);
        }
    }

    private final static class EnclosingMethodInfo {
        private Class<?> enclosingClass;
        private String name;
        private String descriptor;

        private EnclosingMethodInfo(Object[] enclosingInfo) {
            if (enclosingInfo.length != 3)
                throw new InternalError("封闭方法信息格式错误");
            try {
                // 数组预期有三个元素：

                // 直接封闭类
                enclosingClass = (Class<?>) enclosingInfo[0];
                assert(enclosingClass != null);

                // 直接封闭方法或构造函数的名称（可以为 null）。
                name            = (String)   enclosingInfo[1];

                // 直接封闭方法或构造函数的描述符（如果名称为 null，则为 null）。
                descriptor      = (String)   enclosingInfo[2];
                assert((name != null && descriptor != null) || name == descriptor);
            } catch (ClassCastException cce) {
                throw new InternalError("封闭方法信息中的类型无效", cce);
            }
        }

        boolean isPartial() {
            return enclosingClass == null || name == null || descriptor == null;
        }

        boolean isConstructor() { return !isPartial() && "<init>".equals(name); }

        boolean isMethod() { return !isPartial() && !isConstructor() && !"<clinit>".equals(name); }

        Class<?> getEnclosingClass() { return enclosingClass; }

        String getName() { return name; }

        String getDescriptor() { return descriptor; }
    }

    private static Class<?> toClass(Type o) {
        if (o instanceof GenericArrayType)
            return Array.newInstance(toClass(((GenericArrayType)o).getGenericComponentType()),
                                     0)
                .getClass();
        return (Class<?>)o;
    }

    /**
    * 如果此{@code Class}对象表示构造方法中的局部类或匿名类，则返回一个{@link 
    * java.lang.reflect.Constructor Constructor}对象，该对象表示底层类的直接外围构造方法。否则返回{@code null}。特别地，如果底层类是由类型声明、实例初始化块或静态初始化块直接封装的局部类或匿名类，则此方法返回{@code null}。
    *
    * @return 底层类的直接外围构造方法（如果该类是局部类或匿名类）；否则为{@code null}。
    * @throws SecurityException
    *         如果存在安全管理器<i>s</i>，且满足以下任一条件：
    *
    *         <ul>
    *
    *         <li> 调用者的类加载器与外围类的类加载器不同，且调用{@link SecurityManager#checkPermission 
    *         s.checkPermission}方法时，{@code RuntimePermission("accessDeclaredMembers")}权限拒绝访问外围类中的构造方法
    *
    *         <li> 调用者的类加载器不是外围类加载器的相同或祖先加载器，且调用{@link SecurityManager#checkPackageAccess 
    *         s.checkPackageAccess()}方法时拒绝访问外围类的包
    *
    *         </ul>
    * @since 1.5
    */
    @CallerSensitive
    public Constructor<?> getEnclosingConstructor() throws SecurityException {
        EnclosingMethodInfo enclosingInfo = getEnclosingMethodInfo();

        if (enclosingInfo == null)
            return null;
        else {
            if (!enclosingInfo.isConstructor())
                return null;

            ConstructorRepository typeInfo = ConstructorRepository.make(enclosingInfo.getDescriptor(),
                                                                        getFactory());
            Type []    parameterTypes   = typeInfo.getParameterTypes();
            Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];

            // 将Type转换为Class；返回的类型*应该*是类对象，因为使用的方法描述符不包含泛型信息
            for(int i = 0; i < parameterClasses.length; i++)
                parameterClasses[i] = toClass(parameterTypes[i]);

            // 执行访问检查
            Class<?> enclosingCandidate = enclosingInfo.getEnclosingClass();
            enclosingCandidate.checkMemberAccess(Member.DECLARED,
                                                Reflection.getCallerClass(), true);
            /*
            * 遍历所有声明的构造方法；匹配参数数量和类型。
            */
            for(Constructor<?> c: enclosingCandidate.getDeclaredConstructors()) {
                Class<?>[] candidateParamClasses = c.getParameterTypes();
                if (candidateParamClasses.length == parameterClasses.length) {
                    boolean matches = true;
                    for(int i = 0; i < candidateParamClasses.length; i++) {
                        if (!candidateParamClasses[i].equals(parameterClasses[i])) {
                            matches = false;
                            break;
                        }
                    }

                    if (matches)
                        return c;
                }
            }

            throw new InternalError("未找到外围构造方法");
        }
    }


    /**
    * 如果此{@code Class}对象表示的类或接口是另一个类的成员，则返回表示其声明所在类的{@code Class}对象。如果此类或接口不是任何其他类的成员，则此方法返回null。如果此{@code Class}对象表示数组类、基本类型或void，则此方法返回null。
    *
    * @return 此类的声明类
    * @throws SecurityException
    *         如果存在安全管理器<i>s</i>，且调用者的类加载器不是声明类加载器的相同或祖先加载器，且调用{@link 
    *         SecurityManager#checkPackageAccess s.checkPackageAccess()}方法时拒绝访问声明类的包
    * @since JDK1.1
    */
    @CallerSensitive
    public Class<?> getDeclaringClass() throws SecurityException {
        final Class<?> candidate = getDeclaringClass0();

        if (candidate != null)
            candidate.checkPackageAccess(
                    ClassLoader.getClassLoader(Reflection.getCallerClass()), true);
        return candidate;
    }

    private native Class<?> getDeclaringClass0();


    /**
    * 返回底层类的直接外围类。如果底层类是顶级类，则此方法返回{@code null}。
    * @return 底层类的直接外围类
    * @exception  SecurityException
    *             如果存在安全管理器<i>s</i>，且调用者的类加载器不是外围类加载器的相同或祖先加载器，且调用{@link 
    *             SecurityManager#checkPackageAccess s.checkPackageAccess()}方法时拒绝访问外围类的包
    * @since 1.5
    */
    @CallerSensitive
    public Class<?> getEnclosingClass() throws SecurityException {
        // 类（或接口）有五种类型：
        // a) 顶级类
        // b) 嵌套类（静态成员类）
        // c) 内部类（非静态成员类）
        // d) 局部类（在方法中声明的命名类）
        // e) 匿名类


        // JVM规范4.8.6：当且仅当类是局部类或匿名类时，必须具有EnclosingMethod属性。
        EnclosingMethodInfo enclosingInfo = getEnclosingMethodInfo();
        Class<?> enclosingCandidate;

        if (enclosingInfo == null) {
            // 这是顶级类、嵌套类或内部类（a、b或c）
            enclosingCandidate = getDeclaringClass();
        } else {
            Class<?> enclosingClass = enclosingInfo.getEnclosingClass();
            // 这是局部类或匿名类（d或e）
            if (enclosingClass == this || enclosingClass == null)
                throw new InternalError("外围方法信息格式错误");
            else
                enclosingCandidate = enclosingClass;
        }

        if (enclosingCandidate != null)
            enclosingCandidate.checkPackageAccess(
                    ClassLoader.getClassLoader(Reflection.getCallerClass()), true);
        return enclosingCandidate;
    }

    /**
    * 返回底层类在源代码中给出的简单名称。如果底层类是匿名类，则返回空字符串。
    *
    * <p>数组的简单名称是组件类型的简单名称后附加"[]"。特别地，组件类型为匿名类的数组的简单名称是"[]"。
    *
    * @return 底层类的简单名称
    * @since 1.5
    */
    public String getSimpleName() {
        if (isArray())
            return getComponentType().getSimpleName()+"[]";

        String simpleName = getSimpleBinaryName();
        if (simpleName == null) { // 顶级类
            simpleName = getName();
            return simpleName.substring(simpleName.lastIndexOf(".")+1); // 去除包名
        }
        // 根据JLS3“二进制兼容性”（13.1），非包类（非顶级类）的二进制名称是直接外围类的二进制名称后跟'$'，再跟：
        // （对于嵌套类和内部类）：简单名称。
        // （对于局部类）：1个或多个数字后跟简单名称。
        // （对于匿名类）：1个或多个数字。

        // 由于getSimpleBinaryName()将去除直接外围类的二进制名称，我们现在看到的字符串匹配正则表达式"\$[0-9]*"
        // 后跟简单名称（将匿名类的简单名称视为空字符串）。

        // 从名称中删除前导"\$[0-9]*"
        int length = simpleName.length();
        if (length < 1 || simpleName.charAt(0) != '$')
            throw new InternalError("类名格式错误");
        int index = 1;
        while (index < length && isAsciiDigit(simpleName.charAt(index)))
            index++;
        // 最终，当且仅当这是匿名类时，这是空字符串
        return simpleName.substring(index);
    }

    /**
    * 返回此类型名称的信息性字符串。
    *
    * @return 此类型名称的信息性字符串
    * @since 1.8
    */
    public String getTypeName() {
        if (isArray()) {
            try {
                Class<?> cl = this;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                StringBuilder sb = new StringBuilder();
                sb.append(cl.getName());
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) { /*FALLTHRU*/ }
        }
        return getName();
    }

    /**
    * Character.isDigit对某些非ASCII数字返回{@code true}。此方法不返回{@code true}。
    */
    private static boolean isAsciiDigit(char c) {
        return '0' <= c && c <= '9';
    }

    /**
    * 返回底层类的规范名称（由Java语言规范定义）。如果底层类没有规范名称（即，如果它是局部类或匿名类，或者是组件类型没有规范名称的数组），则返回null。
    * @return 底层类的规范名称（如果存在），否则为{@code null}。
    * @since 1.5
    */
    public String getCanonicalName() {
        if (isArray()) {
            String canonicalName = getComponentType().getCanonicalName();
            if (canonicalName != null)
                return canonicalName + "[]";
            else
                return null;
        }
        if (isLocalOrAnonymousClass())
            return null;
        Class<?> enclosingClass = getEnclosingClass();
        if (enclosingClass == null) { // 顶级类
            return getName();
        } else {
            String enclosingName = enclosingClass.getCanonicalName();
            if (enclosingName == null)
                return null;
            return enclosingName + "." + getSimpleName();
        }
    }

    /**
    * 当且仅当底层类是匿名类时返回{@code true}。
    *
    * @return 当且仅当此类是匿名类时返回{@code true}。
    * @since 1.5
    */
    public boolean isAnonymousClass() {
        return "".equals(getSimpleName());
    }

    /**
    * 当且仅当底层类是局部类时返回{@code true}。
    *
    * @return 当且仅当此类是局部类时返回{@code true}。
    * @since 1.5
    */
    public boolean isLocalClass() {
        return isLocalOrAnonymousClass() && !isAnonymousClass();
    }

    /**
    * 当且仅当底层类是成员类时返回{@code true}。
    *
    * @return 当且仅当此类是成员类时返回{@code true}。
    * @since 1.5
    */
    public boolean isMemberClass() {
        return getSimpleBinaryName() != null && !isLocalOrAnonymousClass();
    }

    /**
    * 返回底层类的“简单二进制名称”，即没有前导外围类名称的二进制名称。如果底层类是顶级类，则返回{@code null}。
    */
    private String getSimpleBinaryName() {
        Class<?> enclosingClass = getEnclosingClass();
        if (enclosingClass == null) // 顶级类
            return null;
        // 否则，去除外围类的名称
        try {
            return getName().substring(enclosingClass.getName().length());
        } catch (IndexOutOfBoundsException ex) {
            throw new InternalError("类名格式错误", ex);
        }
    }

    /**
    * 如果这是局部类或匿名类，则返回{@code true}。否则返回{@code false}。
    */
    private boolean isLocalOrAnonymousClass() {
        // JVM规范4.8.6：当且仅当类是局部类或匿名类时，必须具有EnclosingMethod属性。
        return getEnclosingMethodInfo() != null;
    }

    /**
    * 返回一个数组，包含表示由此{@code Class}对象表示的类的所有公共类和接口的{@code Class}对象。这包括从超类继承的公共类和接口成员，以及该类声明的公共类和接口成员。如果此{@code Class}对象没有公共成员类或接口，则此方法返回长度为0的数组。如果此{@code Class}对象表示基本类型、数组类或void，此方法也返回长度为0的数组。
    *
    * @return 表示此类公共成员的{@code Class}对象数组
    * @throws SecurityException
    *         如果存在安全管理器<i>s</i>，且调用者的类加载器不是当前类加载器的相同或祖先加载器，且调用{@link SecurityManager#checkPackageAccess 
    *         s.checkPackageAccess()}方法时拒绝访问当前类的包。
    *
    * @since JDK1.1
    */
    @CallerSensitive
    public Class<?>[] getClasses() {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), false);

        // 特权操作，因此此实现可以查看DECLARED类，这是调用者可能没有权限执行的操作。此处的代码允许查看DECLARED类，因为（1）它不会传递除公共成员之外的任何内容，并且（2）公共成员访问已由SecurityManager确认。

        return java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Class<?>[]>() {
                public Class<?>[] run() {
                    List<Class<?>> list = new ArrayList<>();
                    Class<?> currentClass = Class.this;
                    while (currentClass != null) {
                        Class<?>[] members = currentClass.getDeclaredClasses();
                        for (int i = 0; i < members.length; i++) {
                            if (Modifier.isPublic(members[i].getModifiers())) {
                                list.add(members[i]);
                            }
                        }
                        currentClass = currentClass.getSuperclass();
                    }
                    return list.toArray(new Class<?>[0]);
                }
            });
    }


    /**
    * 返回一个数组，包含反映由此{@code Class}对象表示的类或接口的所有可访问公共字段的{@code Field}对象。
    *
    * <p>如果此{@code Class}对象表示的类或接口没有可访问的公共字段，则此方法返回长度为0的数组。
    *
    * <p>如果此{@code Class}对象表示类，则此方法返回该类及其所有超类的公共字段。
    *
    * <p>如果此{@code Class}对象表示接口，则此方法返回该接口及其所有超接口的字段。
    *
    * <p>如果此{@code Class}对象表示数组类型、基本类型或void，则此方法返回长度为0的数组。
    *
    * <p>返回数组中的元素未排序，也没有任何特定顺序。
    *
    * @return 表示公共字段的{@code Field}对象数组
    * @throws SecurityException
    *         如果存在安全管理器<i>s</i>，且调用者的类加载器不是当前类加载器的相同或祖先加载器，且调用{@link SecurityManager#checkPackageAccess 
    *         s.checkPackageAccess()}方法时拒绝访问当前类的包。
    *
    * @since JDK1.1
    * @jls 8.2 Class Members
    * @jls 8.3 Field Declarations
    */
    @CallerSensitive
    public Field[] getFields() throws SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        return copyFields(privateGetPublicFields(null));
    }

    /**
    * 返回一个数组，包含反映由此{@code Class}对象表示的类或接口的所有公共方法的{@code Method}对象，包括该类或接口声明的方法及从超类和超接口继承的方法。
    *
    * <p>如果此{@code Class}对象表示的类型具有多个名称和参数类型相同但返回类型不同的公共方法，则返回的数组为每个此类方法包含一个{@code Method}对象。
    *
    * <p>如果此{@code Class}对象表示的类型具有类初始化方法{@code <clinit>}，则返回的数组<strong>不</strong>包含对应的{@code Method}对象。
    *
    * <p>如果此{@code Class}对象表示数组类型，则返回的数组包含数组类型从{@code Object}继承的每个公共方法的{@code Method}对象，但不包含{@code clone()}方法的{@code Method}对象。
    *
    * <p>如果此{@code Class}对象表示接口，则返回的数组不包含从{@code Object}隐式声明的方法。因此，如果此接口或其任何超接口中未显式声明方法，则返回的数组长度为0。（注意，表示类的{@code Class}对象始终具有从{@code Object}继承的公共方法。）
    *
    * <p>如果此{@code Class}对象表示基本类型或void，则返回的数组长度为0。
    *
    * <p>由此{@code Class}对象表示的类或接口的超接口中声明的静态方法不视为该类或接口的成员。
    *
    * <p>返回数组中的元素未排序，也没有任何特定顺序。
    *
    * @return 表示此类公共方法的{@code Method}对象数组
    * @throws SecurityException
    *         如果存在安全管理器<i>s</i>，且调用者的类加载器不是当前类加载器的相同或祖先加载器，且调用{@link SecurityManager#checkPackageAccess 
    *         s.checkPackageAccess()}方法时拒绝访问当前类的包。
    *
    * @jls 8.2 Class Members
    * @jls 8.4 Method Declarations
    * @since JDK1.1
    */
    @CallerSensitive
    public Method[] getMethods() throws SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        return copyMethods(privateGetPublicMethods());
    }


    /**
    * 返回一个数组，包含反映由此{@code Class}对象表示的类的所有公共构造方法的{@code Constructor}对象。如果该类没有公共构造方法，或者该类是数组类，或者该类反映基本类型或void，则返回长度为0的数组。
    *
    * 请注意，虽然此方法返回{@code Constructor<T>}对象数组（即从此类的构造方法数组），但此方法的返回类型是{@code Constructor<?>[]}，而不是预期的{@code Constructor<T>[]}。这种信息较少的返回类型是必要的，因为从此方法返回后，数组可能被修改为包含其他类的{@code Constructor}对象，这将违反{@code Constructor<T>[]}的类型保证。
    *
    * @return 表示此类公共构造方法的{@code Constructor}对象数组
    * @throws SecurityException
    *         如果存在安全管理器<i>s</i>，且调用者的类加载器不是当前类加载器的相同或祖先加载器，且调用{@link SecurityManager#checkPackageAccess 
    *         s.checkPackageAccess()}方法时拒绝访问当前类的包。
    *
    * @since JDK1.1
    */
    @CallerSensitive
    public Constructor<?>[] getConstructors() throws SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        return copyConstructors(privateGetDeclaredConstructors(true));
    }


    /**
    * 返回一个{@code Field}对象，该对象反映由此{@code Class}对象表示的类或接口的指定公共成员字段。{@code name}参数是一个{@code String}，指定所需字段的简单名称。
    *
    * <p>要反射的字段由以下算法确定。设C为此对象表示的类或接口：
    *
    * <OL>
    * <LI> 如果C声明了一个具有指定名称的公共字段，则该字段即为要反射的字段。</LI>
    * <LI> 如果在上述步骤1中未找到字段，则将此算法递归应用于C的每个直接超接口。直接超接口按其声明顺序搜索。</LI>
    * <LI> 如果在步骤1和2中未找到字段，且C有超类S，则对此算法递归调用S。如果C没有超类，则抛出{@code NoSuchFieldException}。</LI>
    * </OL>
    *
    * <p>如果此{@code Class}对象表示数组类型，则此方法不会找到数组类型的{@code length}字段。
    *
    * @param name 字段名称
    * @return 由此{@code name}指定的此类的{@code Field}对象
    * @throws NoSuchFieldException 如果未找到具有指定名称的字段。
    * @throws NullPointerException 如果{@code name}为{@code null}
    * @throws SecurityException
    *         如果存在安全管理器<i>s</i>，且调用者的类加载器不是当前类加载器的相同或祖先加载器，且调用{@link SecurityManager#checkPackageAccess 
    *         s.checkPackageAccess()}方法时拒绝访问当前类的包。
    *
    * @since JDK1.1
    * @jls 8.2 Class Members
    * @jls 8.3 Field Declarations
    */
    @CallerSensitive
    public Field getField(String name)
        throws NoSuchFieldException, SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        Field field = getField0(name);
        if (field == null) {
            throw new NoSuchFieldException(name);
        }
        return field;
    }


    /**
    * 返回一个{@code Method}对象，该对象反映由此{@code Class}对象表示的类或接口的指定公共成员方法。{@code name}参数是一个{@code String}，指定所需方法的简单名称。{@code parameterTypes}参数是一个{@code Class}对象数组，按声明顺序标识方法的形式参数类型。如果{@code parameterTypes}为{@code null}，则视为空数组。
    *
    * <p>如果{@code name}为“{@code <init>}”或“{@code <clinit>}”，则抛出{@code NoSuchMethodException}。否则，要反射的方法由以下算法确定。设C为此对象表示的类或接口：
    * <OL>
    * <LI> 在C中搜索<I>匹配方法</I>（如下定义）。如果找到匹配方法，则进行反射。</LI>
    * <LI> 如果步骤1中未找到匹配方法，则：
    *   <OL TYPE="a">
    *   <LI> 如果C是除{@code Object}之外的类，则对此算法递归调用C的超类。</LI>
    *   <LI> 如果C是{@code Object}类，或者C是接口，则搜索C的超接口（如果有）以查找匹配方法。如果找到任何此类方法，则进行反射。</LI>
    *   </OL></LI>
    * </OL>
    *
    * <p>要在类或接口C中查找匹配方法：如果C恰好声明了一个具有指定名称和完全相同形式参数类型的公共方法，则该方法即为要反射的方法。如果在C中找到多个此类方法，且其中一个方法的返回类型比其他方法更具体，则反射该方法；否则，任意选择其中一个方法。
    *
    * <p>请注意，类中可能存在多个匹配方法，因为虽然Java语言禁止类声明多个具有相同签名但不同返回类型的方法，但Java虚拟机并不禁止。虚拟机的这种灵活性可用于实现各种语言特性。例如，协变返回类型可以通过{@linkplain 
    * java.lang.reflect.Method#isBridge 桥接方法}实现；桥接方法和被覆盖的方法将具有相同的签名但不同的返回类型。
    *
    * <p>如果此{@code Class}对象表示数组类型，则此方法不会找到{@code clone()}方法。
    *
    * <p>由此{@code Class}对象表示的类或接口的超接口中声明的静态方法不视为该类或接口的成员。
    *
    * @param name 方法名称
    * @param parameterTypes 参数列表
    * @return 匹配指定{@code name}和{@code parameterTypes}的{@code Method}对象
    * @throws NoSuchMethodException 如果未找到匹配方法，或名称为“&lt;init&gt;”或“&lt;clinit&gt;”。
    * @throws NullPointerException 如果{@code name}为{@code null}
    * @throws SecurityException
    *         如果存在安全管理器<i>s</i>，且调用者的类加载器不是当前类加载器的相同或祖先加载器，且调用{@link SecurityManager#checkPackageAccess 
    *         s.checkPackageAccess()}方法时拒绝访问当前类的包。
    *
    * @jls 8.2 Class Members
    * @jls 8.4 Method Declarations
    * @since JDK1.1
    */
    @CallerSensitive
    public Method getMethod(String name, Class<?>... parameterTypes)
        throws NoSuchMethodException, SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        Method method = getMethod0(name, parameterTypes, true);
        if (method == null) {
            throw new NoSuchMethodException(getName() + "." + name + argumentTypesToString(parameterTypes));
        }
        return method;
    }


    /**
    * 返回一个{@code Constructor}对象，该对象反映由此{@code Class}对象表示的类的指定公共构造方法。{@code parameterTypes}参数是一个{@code Class}对象数组，按声明顺序标识构造方法的形式参数类型。
    *
    * 如果此{@code Class}对象表示在非静态上下文中声明的内部类，则形式参数类型包括作为第一个参数的显式外围实例。
    *
    * <p>要反射的构造方法是由此{@code Class}对象表示的类的公共构造方法，其形式参数类型与{@code parameterTypes}指定的类型匹配。
    *
    * @param parameterTypes 参数数组
    * @return 匹配指定{@code parameterTypes}的公共构造方法的{@code Constructor}对象
    * @throws NoSuchMethodException 如果未找到匹配方法。
    * @throws SecurityException
    *         如果存在安全管理器<i>s</i>，且调用者的类加载器不是当前类加载器的相同或祖先加载器，且调用{@link SecurityManager#checkPackageAccess 
    *         s.checkPackageAccess()}方法时拒绝访问当前类的包。
    *
    * @since JDK1.1
    */
    @CallerSensitive
    public Constructor<T> getConstructor(Class<?>... parameterTypes)
        throws NoSuchMethodException, SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        return getConstructor0(parameterTypes, Member.PUBLIC);
    }


    /**
    * 返回一个{@code Class}对象数组，反映由此{@code Class}对象表示的类声明的所有作为成员的类和接口。这包括该类声明的公共、受保护、默认（包）访问和私有类和接口，但不包括继承的类和接口。如果该类未声明任何作为成员的类或接口，或者此{@code Class}对象表示基本类型、数组类或void，则此方法返回长度为0的数组。
    *
    * @return 表示此类所有声明成员的{@code Class}对象数组
    * @throws SecurityException
    *         如果存在安全管理器<i>s</i>，且满足以下任一条件：
    *
    *         <ul>
    *
    *         <li> 调用者的类加载器与此类的类加载器不同，且调用{@link SecurityManager#checkPermission 
    *         s.checkPermission}方法时，{@code RuntimePermission("accessDeclaredMembers")}权限拒绝访问此类中的声明类
    *
    *         <li> 调用者的类加载器不是当前类加载器的相同或祖先加载器，且调用{@link SecurityManager#checkPackageAccess 
    *         s.checkPackageAccess()}方法时拒绝访问当前类的包
    *
    *         </ul>
    *
    * @since JDK1.1
    */
    @CallerSensitive
    public Class<?>[] getDeclaredClasses() throws SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), false);
        return getDeclaredClasses0();
    }


    /**
    * 返回一个{@code Field}对象数组，反映由此{@code Class}对象表示的类或接口声明的所有字段。这包括公共、受保护、默认（包）访问和私有字段，但不包括继承的字段。
    *
    * <p>如果此{@code Class}对象表示的类或接口没有声明字段，则此方法返回长度为0的数组。
    *
    * <p>如果此{@code Class}对象表示数组类型、基本类型或void，则此方法返回长度为0的数组。
    *
    * <p>返回数组中的元素未排序，也没有任何特定顺序。
    *
    * @return 表示此类所有声明字段的{@code Field}对象数组
    * @throws SecurityException
    *          如果存在安全管理器<i>s</i>，且满足以下任一条件：
    *
    *          <ul>
    *
    *          <li> 调用者的类加载器与此类的类加载器不同，且调用{@link SecurityManager#checkPermission 
    *          s.checkPermission}方法时，{@code RuntimePermission("accessDeclaredMembers")}权限拒绝访问此类中的声明字段
    *
    *          <li> 调用者的类加载器不是当前类加载器的相同或祖先加载器，且调用{@link SecurityManager#checkPackageAccess 
    *          s.checkPackageAccess()}方法时拒绝访问当前类的包
    *
    *          </ul>
    *
    * @since JDK1.1
    * @jls 8.2 Class Members
    * @jls 8.3 Field Declarations
    */
    @CallerSensitive
    public Field[] getDeclaredFields() throws SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        return copyFields(privateGetDeclaredFields(false));
    }


    /**
    * 返回一个数组，包含反映由此{@code Class}对象表示的类或接口的所有声明方法的{@code Method}对象，包括公共、受保护、默认（包）访问和私有方法，但不包括继承的方法。
    *
    * <p>如果此{@code Class}对象表示的类型具有多个声明的名称和参数类型相同但返回类型不同的方法，则返回的数组为每个此类方法包含一个{@code Method}对象。
    *
    * <p>如果此{@code Class}对象表示的类型具有类初始化方法{@code <clinit>}，则返回的数组<strong>不</strong>包含对应的{@code Method}对象。
    *
    * <p>如果此{@code Class}对象表示的类或接口没有声明方法，则返回的数组长度为0。
    *
    * <p>如果此{@code Class}对象表示数组类型、基本类型或void，则返回的数组长度为0。
    *
    * <p>返回数组中的元素未排序，也没有任何特定顺序。
    *
    * @return 表示此类所有声明方法的{@code Method}对象数组
    * @throws SecurityException
    *          如果存在安全管理器<i>s</i>，且满足以下任一条件：
    *
    *          <ul>
    *
    *          <li> 调用者的类加载器与此类的类加载器不同，且调用{@link SecurityManager#checkPermission 
    *          s.checkPermission}方法时，{@code RuntimePermission("accessDeclaredMembers")}权限拒绝访问此类中的声明方法
    *
    *          <li> 调用者的类加载器不是当前类加载器的相同或祖先加载器，且调用{@link SecurityManager#checkPackageAccess 
    *          s.checkPackageAccess()}方法时拒绝访问当前类的包
    *
    *          </ul>
    *
    * @jls 8.2 Class Members
    * @jls 8.4 Method Declarations
    * @since JDK1.1
    */
    @CallerSensitive
    public Method[] getDeclaredMethods() throws SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        return copyMethods(privateGetDeclaredMethods(false));
    }

    /**
     * 返回一个数组，包含反映由此{@code Class}对象表示的类声明的所有构造方法的{@code Constructor}对象。这些构造方法包括公共、受保护、默认（包）访问和私有构造方法。返回数组中的元素未排序，也没有任何特定顺序。如果该类具有默认构造方法，则会包含在返回的数组中。如果此{@code Class}对象表示接口、基本类型、数组类或void，则此方法返回长度为0的数组。
     *
     * <p> 请参见《Java语言规范》第8.2节。
     *
     * @return 表示此类所有声明构造方法的{@code Constructor}对象数组
     * @throws SecurityException
     *          如果存在安全管理器<i>s</i>，且满足以下任一条件：
     *
     *          <ul>
     *
     *          <li> 调用者的类加载器与此类的类加载器不同，且调用{@link SecurityManager#checkPermission 
     *          s.checkPermission}方法时，{@code RuntimePermission("accessDeclaredMembers")}权限拒绝访问此类中的声明构造方法
     *
     *          <li> 调用者的类加载器不是当前类加载器的相同或祖先加载器，且调用{@link SecurityManager#checkPackageAccess 
     *          s.checkPackageAccess()}方法时拒绝访问当前类的包
     *
     *          </ul>
     *
     * @since JDK1.1
     */
    @CallerSensitive
    public Constructor<?>[] getDeclaredConstructors() throws SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        return copyConstructors(privateGetDeclaredConstructors(false));
    }


    /**
     * 返回一个{@code Field}对象，该对象反映由此{@code Class}对象表示的类或接口的指定声明字段。{@code name}参数是一个{@code String}，指定所需字段的简单名称。
     *
     * <p> 如果此{@code Class}对象表示数组类型，则此方法不会找到数组类型的{@code length}字段。
     *
     * @param name 字段名称
     * @return 此类中指定字段的{@code Field}对象
     * @throws NoSuchFieldException 如果未找到具有指定名称的字段。
     * @throws NullPointerException 如果{@code name}为{@code null}
     * @throws SecurityException
     *          如果存在安全管理器<i>s</i>，且满足以下任一条件：
     *
     *          <ul>
     *
     *          <li> 调用者的类加载器与此类的类加载器不同，且调用{@link SecurityManager#checkPermission 
     *          s.checkPermission}方法时，{@code RuntimePermission("accessDeclaredMembers")}权限拒绝访问声明的字段
     *
     *          <li> 调用者的类加载器不是当前类加载器的相同或祖先加载器，且调用{@link SecurityManager#checkPackageAccess 
     *          s.checkPackageAccess()}方法时拒绝访问当前类的包
     *
     *          </ul>
     *
     * @since JDK1.1
     * @jls 8.2 Class Members
     * @jls 8.3 Field Declarations
     */
    @CallerSensitive
    public Field getDeclaredField(String name)
        throws NoSuchFieldException, SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        Field field = searchFields(privateGetDeclaredFields(false), name);
        if (field == null) {
            throw new NoSuchFieldException(name);
        }
        return field;
    }


    /**
     * 返回一个{@code Method}对象，该对象反映由此{@code Class}对象表示的类或接口的指定声明方法。{@code name}参数是一个{@code String}，指定所需方法的简单名称，{@code parameterTypes}参数是一个{@code Class}对象数组，按声明顺序标识方法的形式参数类型。如果类中声明了多个具有相同参数类型的方法，且其中一个方法的返回类型比其他方法更具体，则返回该方法；否则，任意选择其中一个方法。如果名称为“&lt;init&gt;”或“&lt;clinit&gt;”，则抛出{@code NoSuchMethodException}。
     *
     * <p> 如果此{@code Class}对象表示数组类型，则此方法不会找到{@code clone()}方法。
     *
     * @param name 方法名称
     * @param parameterTypes 参数数组
     * @return 与此类中指定名称和参数匹配的方法的{@code Method}对象
     * @throws NoSuchMethodException 如果未找到匹配方法。
     * @throws NullPointerException 如果{@code name}为{@code null}
     * @throws SecurityException
     *          如果存在安全管理器<i>s</i>，且满足以下任一条件：
     *
     *          <ul>
     *
     *          <li> 调用者的类加载器与此类的类加载器不同，且调用{@link SecurityManager#checkPermission 
     *          s.checkPermission}方法时，{@code RuntimePermission("accessDeclaredMembers")}权限拒绝访问声明的方法
     *
     *          <li> 调用者的类加载器不是当前类加载器的相同或祖先加载器，且调用{@link SecurityManager#checkPackageAccess 
     *          s.checkPackageAccess()}方法时拒绝访问当前类的包
     *
     *          </ul>
     *
     * @jls 8.2 Class Members
     * @jls 8.4 Method Declarations
     * @since JDK1.1
     */
    @CallerSensitive
    public Method getDeclaredMethod(String name, Class<?>... parameterTypes)
        throws NoSuchMethodException, SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        Method method = searchMethods(privateGetDeclaredMethods(false), name, parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException(getName() + "." + name + argumentTypesToString(parameterTypes));
        }
        return method;
    }


    /**
     * 返回一个{@code Constructor}对象，该对象反映由此{@code Class}对象表示的类或接口的指定构造方法。{@code parameterTypes}参数是一个{@code Class}对象数组，按声明顺序标识构造方法的形式参数类型。
     *
     * 如果此{@code Class}对象表示在非静态上下文中声明的内部类，则形式参数类型包括作为第一个参数的显式外围实例。
     *
     * @param parameterTypes 参数数组
     * @return 具有指定参数列表的构造方法的{@code Constructor}对象
     * @throws NoSuchMethodException 如果未找到匹配方法。
     * @throws SecurityException
     *          如果存在安全管理器<i>s</i>，且满足以下任一条件：
     *
     *          <ul>
     *
     *          <li> 调用者的类加载器与此类的类加载器不同，且调用{@link SecurityManager#checkPermission 
     *          s.checkPermission}方法时，{@code RuntimePermission("accessDeclaredMembers")}权限拒绝访问声明的构造方法
     *
     *          <li> 调用者的类加载器不是当前类加载器的相同或祖先加载器，且调用{@link SecurityManager#checkPackageAccess 
     *          s.checkPackageAccess()}方法时拒绝访问当前类的包
     *
     *          </ul>
     *
     * @since JDK1.1
     */
    @CallerSensitive
    public Constructor<T> getDeclaredConstructor(Class<?>... parameterTypes)
        throws NoSuchMethodException, SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        return getConstructor0(parameterTypes, Member.DECLARED);
    }

    /**
     * 查找具有给定名称的资源。与给定类关联的资源搜索规则由该类的定义{@linkplain ClassLoader 类加载器}实现。此方法委托给该对象的类加载器。如果该对象由引导类加载器加载，则该方法委托给{@link 
     * ClassLoader#getSystemResourceAsStream}。
     *
     * <p> 在委托之前，使用以下算法从给定的资源名称构造绝对资源名称：
     *
     * <ul>
     *
     * <li> 如果{@code name}以{@code '/'}（<tt>'&#92;u002f'</tt>）开头，则资源的绝对名称为{@code name}中{@code '/'}之后的部分。
     *
     * <li> 否则，绝对名称的形式如下：
     *
     * <blockquote>
     *   {@code modified_package_name/name}
     * </blockquote>
     *
     * <p> 其中{@code modified_package_name}是此类的包名，将{@code '.'}（<tt>'&#92;u002e'</tt>）替换为{@code '/'}。
     *
     * </ul>
     *
     * @param  name 所需资源的名称
     * @return 一个{@link java.io.InputStream}对象；如果未找到具有该名称的资源，则返回{@code null}
     * @throws NullPointerException 如果{@code name}为{@code null}
     * @since  JDK1.1
     */
    public InputStream getResourceAsStream(String name) {
        name = resolveName(name);
        ClassLoader cl = getClassLoader0();
        if (cl==null) {
            // 系统类。
            return ClassLoader.getSystemResourceAsStream(name);
        }
        return cl.getResourceAsStream(name);
    }

    /**
     * 查找具有给定名称的资源。与给定类关联的资源搜索规则由该类的定义{@linkplain ClassLoader 类加载器}实现。此方法委托给该对象的类加载器。如果该对象由引导类加载器加载，则该方法委托给{@link 
     * ClassLoader#getSystemResource}。
     *
     * <p> 在委托之前，使用以下算法从给定的资源名称构造绝对资源名称：
     *
     * <ul>
     *
     * <li> 如果{@code name}以{@code '/'}（<tt>'&#92;u002f'</tt>）开头，则资源的绝对名称为{@code name}中{@code '/'}之后的部分。
     *
     * <li> 否则，绝对名称的形式如下：
     *
     * <blockquote>
     *   {@code modified_package_name/name}
     * </blockquote>
     *
     * <p> 其中{@code modified_package_name}是此类的包名，将{@code '.'}（<tt>'&#92;u002e'</tt>）替换为{@code '/'}。
     *
     * </ul>
     *
     * @param  name 所需资源的名称
     * @return 一个{@link java.net.URL}对象；如果未找到具有该名称的资源，则返回{@code null}
     * @since  JDK1.1
     */
    public java.net.URL getResource(String name) {
        name = resolveName(name);
        ClassLoader cl = getClassLoader0();
        if (cl==null) {
            // 系统类。
            return ClassLoader.getSystemResource(name);
        }
        return cl.getResource(name);
    }


    /** 当内部域为null时返回的保护域 */
    private static java.security.ProtectionDomain allPermDomain;


    /**
     * 返回此类的{@code ProtectionDomain}。如果安装了安全管理器，此方法首先调用安全管理器的{@code checkPermission}方法，并传入{@code RuntimePermission("getProtectionDomain")}权限，以确保获取{@code ProtectionDomain}是允许的。
     *
     * @return 此类的ProtectionDomain
     *
     * @throws SecurityException
     *        如果存在安全管理器，且其{@code checkPermission}方法不允许获取ProtectionDomain。
     *
     * @see java.security.ProtectionDomain
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     * @since 1.2
     */
    public java.security.ProtectionDomain getProtectionDomain() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_PD_PERMISSION);
        }
        java.security.ProtectionDomain pd = getProtectionDomain0();
        if (pd == null) {
            if (allPermDomain == null) {
                java.security.Permissions perms =
                    new java.security.Permissions();
                perms.add(SecurityConstants.ALL_PERMISSION);
                allPermDomain =
                    new java.security.ProtectionDomain(null, perms);
            }
            pd = allPermDomain;
        }
        return pd;
    }


    /**
     * 返回此类的ProtectionDomain。
     */
    private native java.security.ProtectionDomain getProtectionDomain0();

    /*
    * 返回指定基本类型的虚拟机Class对象。
    */
    static native Class<?> getPrimitiveClass(String name);

    /*
    * 检查客户端是否允许访问成员。如果访问被拒绝，则抛出SecurityException。
    *
    * 此方法还强制执行包访问控制。
    *
    * <p> 默认策略：允许所有客户端通过正常的Java访问控制进行访问。
    */
    private void checkMemberAccess(int which, Class<?> caller, boolean checkProxyInterfaces) {
        final SecurityManager s = System.getSecurityManager();
        if (s != null) {
            /* 默认策略允许访问所有{@link Member#PUBLIC}成员，以及访问与调用者具有相同类加载器的类。
            * 在所有其他情况下，需要RuntimePermission("accessDeclaredMembers")权限。
            */
            final ClassLoader ccl = ClassLoader.getClassLoader(caller);
            final ClassLoader cl = getClassLoader0();
            if (which != Member.PUBLIC) {
                if (ccl != cl) {
                    s.checkPermission(SecurityConstants.CHECK_MEMBER_ACCESS_PERMISSION);
                }
            }
            this.checkPackageAccess(ccl, checkProxyInterfaces);
        }
    }

    /*
    * 根据当前的包访问策略，检查在类加载器ccl中加载的客户端是否允许访问此类。如果访问被拒绝，则抛出SecurityException。
    */
    private void checkPackageAccess(final ClassLoader ccl, boolean checkProxyInterfaces) {
        final SecurityManager s = System.getSecurityManager();
        if (s != null) {
            final ClassLoader cl = getClassLoader0();

            if (ReflectUtil.needsPackageAccessCheck(ccl, cl)) {
                String name = this.getName();
                int i = name.lastIndexOf('.');
                if (i != -1) {
                    // 跳过默认代理包中代理类的包访问检查
                    String pkg = name.substring(0, i);
                    if (!Proxy.isProxyClass(this) || ReflectUtil.isNonPublicProxyClass(this)) {
                        s.checkPackageAccess(pkg);
                    }
                }
            }
            // 检查代理接口的包访问
            if (checkProxyInterfaces && Proxy.isProxyClass(this)) {
                ReflectUtil.checkProxyPackageAccess(ccl, this.getInterfaces());
            }
        }
    }

    /**
     * 如果名称不是绝对路径，则添加包名前缀；如果名称是绝对路径，则删除前导"/"
     */
    private String resolveName(String name) {
        if (name == null) {
            return name;
        }
        if (!name.startsWith("/")) {
            Class<?> c = this;
            while (c.isArray()) {
                c = c.getComponentType();
            }
            String baseName = c.getName();
            int index = baseName.lastIndexOf('.');
            if (index != -1) {
                name = baseName.substring(0, index).replace('.', '/')
                    +"/"+name;
            }
        } else {
            name = name.substring(1);
        }
        return name;
    }

    /**
     * 原子操作支持。
     */
    private static class Atomic {
        // 在此处初始化Unsafe机制，因为我们需要调用Class.class的实例方法，并且必须避免在Class类的静态初始化器中调用它...
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        // Class.reflectionData实例字段的偏移量
        private static final long reflectionDataOffset;
        // Class.annotationType实例字段的偏移量
        private static final long annotationTypeOffset;
        // Class.annotationData实例字段的偏移量
        private static final long annotationDataOffset;

        static {
            Field[] fields = Class.class.getDeclaredFields0(false); // 绕过缓存
            reflectionDataOffset = objectFieldOffset(fields, "reflectionData");
            annotationTypeOffset = objectFieldOffset(fields, "annotationType");
            annotationDataOffset = objectFieldOffset(fields, "annotationData");
        }

        private static long objectFieldOffset(Field[] fields, String fieldName) {
            Field field = searchFields(fields, fieldName);
            if (field == null) {
                throw new Error("No " + fieldName + " field found in java.lang.Class");
            }
            return unsafe.objectFieldOffset(field);
        }

        static <T> boolean casReflectionData(Class<?> clazz,
                                            SoftReference<ReflectionData<T>> oldData,
                                            SoftReference<ReflectionData<T>> newData) {
            return unsafe.compareAndSwapObject(clazz, reflectionDataOffset, oldData, newData);
        }

        static <T> boolean casAnnotationType(Class<?> clazz,
                                            AnnotationType oldType,
                                            AnnotationType newType) {
            return unsafe.compareAndSwapObject(clazz, annotationTypeOffset, oldType, newType);
        }

        static <T> boolean casAnnotationData(Class<?> clazz,
                                            AnnotationData oldData,
                                            AnnotationData newData) {
            return unsafe.compareAndSwapObject(clazz, annotationDataOffset, oldData, newData);
        }
    }

    /**
     * 反射支持。
     */

    // 某些反射结果的缓存
    private static boolean useCaches = true;

    // 当JVM TI调用RedefineClasses()时可能失效的反射数据
    private static class ReflectionData<T> {
        volatile Field[] declaredFields;         // 声明的字段
        volatile Field[] publicFields;           // 公共字段
        volatile Method[] declaredMethods;       // 声明的方法
        volatile Method[] publicMethods;         // 公共方法
        volatile Constructor<T>[] declaredConstructors; // 声明的构造方法
        volatile Constructor<T>[] publicConstructors;   // 公共构造方法
        // getFields和getMethods的中间结果
        volatile Field[] declaredPublicFields;    // 声明的公共字段（中间结果）
        volatile Method[] declaredPublicMethods;  // 声明的公共方法（中间结果）
        volatile Class<?>[] interfaces;           // 接口数组

        // 创建此ReflectionData实例时的classRedefinedCount值
        final int redefinedCount;

        ReflectionData(int redefinedCount) {
            this.redefinedCount = redefinedCount;
        }
    }

    private volatile transient SoftReference<ReflectionData<T>> reflectionData;

    // 当JVM TI调用RedefineClasses()重定义此类或其父类时，由VM递增该计数器
    private volatile transient int classRedefinedCount = 0;

    // 延迟创建并缓存ReflectionData
    private ReflectionData<T> reflectionData() {
        SoftReference<ReflectionData<T>> reflectionData = this.reflectionData;
        int classRedefinedCount = this.classRedefinedCount;
        ReflectionData<T> rd;
        if (useCaches && 
            reflectionData != null && 
            (rd = reflectionData.get()) != null && 
            rd.redefinedCount == classRedefinedCount) {
            return rd;
        }
        // 否则：无软引用、软引用已清除或反射数据过期 -> 创建并替换新实例
        return newReflectionData(reflectionData, classRedefinedCount);
    }

    private ReflectionData<T> newReflectionData(SoftReference<ReflectionData<T>> oldReflectionData,
                                                int classRedefinedCount) {
        if (!useCaches) return null;

        while (true) {
            ReflectionData<T> rd = new ReflectionData<>(classRedefinedCount);
            // 尝试使用CAS替换
            if (Atomic.casReflectionData(this, oldReflectionData, new SoftReference<>(rd))) {
                return rd;
            }
            // 否则重试
            oldReflectionData = this.reflectionData;
            classRedefinedCount = this.classRedefinedCount;
            if (oldReflectionData != null && 
                (rd = oldReflectionData.get()) != null && 
                rd.redefinedCount == classRedefinedCount) {
                return rd;
            }
        }
    }

    // 泛型签名处理
    private native String getGenericSignature0();

    // 泛型信息存储库；延迟初始化
    private volatile transient ClassRepository genericInfo;

    // 工厂访问器
    private GenericsFactory getFactory() {
        // 创建作用域和工厂
        return CoreReflectionFactory.make(this, ClassScope.make(this));
    }

    // 泛型信息存储库访问器；泛型信息延迟初始化
    private ClassRepository getGenericInfo() {
        ClassRepository genericInfo = this.genericInfo;
        if (genericInfo == null) {
            String signature = getGenericSignature0();
            if (signature == null) {
                genericInfo = ClassRepository.NONE;
            } else {
                genericInfo = ClassRepository.make(signature, getFactory());
            }
            this.genericInfo = genericInfo;
        }
        return (genericInfo != ClassRepository.NONE) ? genericInfo : null;
    }

    // 注解处理
    native byte[] getRawAnnotations();
    // 从1.8开始
    native byte[] getRawTypeAnnotations();
    static byte[] getExecutableTypeAnnotationBytes(Executable ex) {
        return getReflectionFactory().getExecutableTypeAnnotationBytes(ex);
    }

    native ConstantPool getConstantPool();

    //
    //
    // java.lang.reflect.Field 处理
    //
    //

    // 返回"根"字段数组。这些Field对象不得公开传播，而应通过ReflectionFactory.copyField复制
    private Field[] privateGetDeclaredFields(boolean publicOnly) {
        checkInitted();
        Field[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = publicOnly ? rd.declaredPublicFields : rd.declaredFields;
            if (res != null) return res;
        }
        // 无缓存值可用；从VM请求值
        res = Reflection.filterFields(this, getDeclaredFields0(publicOnly));
        if (rd != null) {
            if (publicOnly) {
                rd.declaredPublicFields = res;
            } else {
                rd.declaredFields = res;
            }
        }
        return res;
    }

    // 返回"根"字段数组。这些Field对象不得公开传播，而应通过ReflectionFactory.copyField复制
    private Field[] privateGetPublicFields(Set<Class<?>> traversedInterfaces) {
        checkInitted();
        Field[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = rd.publicFields;
            if (res != null) return res;
        }

        // 无缓存值可用；递归计算值（按getField()的正确顺序遍历）
        List<Field> fields = new ArrayList<>();
        if (traversedInterfaces == null) {
            traversedInterfaces = new HashSet<>();
        }

        // 本类字段
        Field[] tmp = privateGetDeclaredFields(true);
        addAll(fields, tmp);

        // 直接超接口（递归）
        for (Class<?> c : getInterfaces()) {
            if (!traversedInterfaces.contains(c)) {
                traversedInterfaces.add(c);
                addAll(fields, c.privateGetPublicFields(traversedInterfaces));
            }
        }

        // 直接超类（递归）
        if (!isInterface()) {
            Class<?> c = getSuperclass();
            if (c != null) {
                addAll(fields, c.privateGetPublicFields(traversedInterfaces));
            }
        }

        res = new Field[fields.size()];
        fields.toArray(res);
        if (rd != null) {
            rd.publicFields = res;
        }
        return res;
    }

    private static void addAll(Collection<Field> c, Field[] o) {
        for (int i = 0; i < o.length; i++) {
            c.add(o[i]);
        }
    }


    //
    //
    // java.lang.reflect.Constructor 处理
    //
    //

    // 返回"根"构造方法数组。这些Constructor对象不得公开传播，而应通过ReflectionFactory.copyConstructor复制
    private Constructor<T>[] privateGetDeclaredConstructors(boolean publicOnly) {
        checkInitted();
        Constructor<T>[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = publicOnly ? rd.publicConstructors : rd.declaredConstructors;
            if (res != null) return res;
        }
        // 无缓存值可用；从VM请求值
        if (isInterface()) {
            @SuppressWarnings("unchecked")
            Constructor<T>[] temporaryRes = (Constructor<T>[]) new Constructor<?>[0];
            res = temporaryRes;
        } else {
            res = getDeclaredConstructors0(publicOnly);
        }
        if (rd != null) {
            if (publicOnly) {
                rd.publicConstructors = res;
            } else {
                rd.declaredConstructors = res;
            }
        }
        return res;
    }

    //
    //
    // java.lang.reflect.Method 处理
    //
    //

    // 返回"根"方法数组。这些Method对象不得公开传播，而应通过ReflectionFactory.copyMethod复制
    private Method[] privateGetDeclaredMethods(boolean publicOnly) {
        checkInitted();
        Method[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = publicOnly ? rd.declaredPublicMethods : rd.declaredMethods;
            if (res != null) return res;
        }
        // 无缓存值可用；从VM请求值
        res = Reflection.filterMethods(this, getDeclaredMethods0(publicOnly));
        if (rd != null) {
            if (publicOnly) {
                rd.declaredPublicMethods = res;
            } else {
                rd.declaredMethods = res;
            }
        }
        return res;
    }

    static class MethodArray {
        // 仅通过add()或remove()调用添加/删除方法
        private Method[] methods;
        private int length;
        private int defaults;

        MethodArray() {
            this(20);
        }

        MethodArray(int initialSize) {
            if (initialSize < 2)
                throw new IllegalArgumentException("Size should be 2 or more");

            methods = new Method[initialSize];
            length = 0;
            defaults = 0;
        }

        boolean hasDefaults() {
            return defaults != 0;
        }

        void add(Method m) {
            if (length == methods.length) {
                methods = Arrays.copyOf(methods, 2 * methods.length);
            }
            methods[length++] = m;

            if (m != null && m.isDefault())
                defaults++;
        }

        void addAll(Method[] ma) {
            for (int i = 0; i < ma.length; i++) {
                add(ma[i]);
            }
        }

        void addAll(MethodArray ma) {
            for (int i = 0; i < ma.length(); i++) {
                add(ma.get(i));
            }
        }

        void addIfNotPresent(Method newMethod) {
            for (int i = 0; i < length; i++) {
                Method m = methods[i];
                if (m == newMethod || (m != null && m.equals(newMethod))) {
                    return;
                }
            }
            add(newMethod);
        }

        void addAllIfNotPresent(MethodArray newMethods) {
            for (int i = 0; i < newMethods.length(); i++) {
                Method m = newMethods.get(i);
                if (m != null) {
                    addIfNotPresent(m);
                }
            }
        }

        /* 将接口中声明的方法添加到此MethodArray。
        * 接口中声明的静态方法不被继承。
        */
        void addInterfaceMethods(Method[] methods) {
            for (Method candidate : methods) {
                if (!Modifier.isStatic(candidate.getModifiers())) {
                    add(candidate);
                }
            }
        }

        int length() {
            return length;
        }

        Method get(int i) {
            return methods[i];
        }

        Method getFirst() {
            for (Method m : methods)
                if (m != null)
                    return m;
            return null;
        }

        void removeByNameAndDescriptor(Method toRemove) {
            for (int i = 0; i < length; i++) {
                Method m = methods[i];
                if (m != null && matchesNameAndDescriptor(m, toRemove)) {
                    remove(i);
                }
            }
        }

        private void remove(int i) {
            if (methods[i] != null && methods[i].isDefault())
                defaults--;
            methods[i] = null;
        }

        private boolean matchesNameAndDescriptor(Method m1, Method m2) {
            return m1.getReturnType() == m2.getReturnType() &&
                m1.getName() == m2.getName() && // 名称已确保为 interned
                arrayContentsEq(m1.getParameterTypes(), m2.getParameterTypes());
        }

        void compactAndTrim() {
            int newPos = 0;
            // 移除null槽位
            for (int pos = 0; pos < length; pos++) {
                Method m = methods[pos];
                if (m != null) {
                    if (pos != newPos) {
                        methods[newPos] = m;
                    }
                    newPos++;
                }
            }
            if (newPos != methods.length) {
                methods = Arrays.copyOf(methods, newPos);
            }
        }

        /* 从此MethodArray中移除所有在数组中存在更具体默认方法的Method。
        * MethodArray的使用者负责修剪存在更具体<em>具体</em>方法的Method。
        */
        void removeLessSpecifics() {
            if (!hasDefaults())
                return;

            for (int i = 0; i < length; i++) {
                Method m = get(i);
                if  (m == null || !m.isDefault())
                    continue;

                for (int j  = 0; j < length; j++) {
                    if (i == j)
                        continue;

                    Method candidate = get(j);
                    if (candidate == null)
                        continue;

                    if (!matchesNameAndDescriptor(m, candidate))
                        continue;

                    if (hasMoreSpecificClass(m, candidate))
                        remove(j);
                }
            }
        }

        Method[] getArray() {
            return methods;
        }

        // 返回true如果m1比m2更具体
        static boolean hasMoreSpecificClass(Method m1, Method m2) {
            Class<?> m1Class = m1.getDeclaringClass();
            Class<?> m2Class = m2.getDeclaringClass();
            return m1Class != m2Class && m2Class.isAssignableFrom(m1Class);
        }
    }


    // 返回"根"方法数组。这些Method对象不得公开传播，而应通过ReflectionFactory.copyMethod复制
    private Method[] privateGetPublicMethods() {
        checkInitted();
        Method[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = rd.publicMethods;
            if (res != null) return res;
        }

        // 无缓存值可用；递归计算值
        // 首先获取声明的公共方法
        MethodArray methods = new MethodArray();
        {
            Method[] tmp = privateGetDeclaredMethods(true);
            methods.addAll(tmp);
        }
        // 现在递归遍历超类和直接超接口。先处理超接口以便最后过滤从超类继承的具体实现
        MethodArray inheritedMethods = new MethodArray();
        for (Class<?> i : getInterfaces()) {
            inheritedMethods.addInterfaceMethods(i.privateGetPublicMethods());
        }
        if (!isInterface()) {
            Class<?> c = getSuperclass();
            if (c != null) {
                MethodArray supers = new MethodArray();
                supers.addAll(c.privateGetPublicMethods());
                // 过滤掉任何接口方法的具体实现
                for (int i = 0; i < supers.length(); i++) {
                    Method m = supers.get(i);
                    if (m != null &&
                        !Modifier.isAbstract(m.getModifiers()) &&
                        !m.isDefault()) {
                        inheritedMethods.removeByNameAndDescriptor(m);
                    }
                }
                // 在超接口之前插入超类的继承方法以满足getMethod的搜索顺序
                supers.addAll(inheritedMethods);
                inheritedMethods = supers;
            }
        }
        // 从继承方法中过滤掉所有本类方法
        for (int i = 0; i < methods.length(); i++) {
            Method m = methods.get(i);
            inheritedMethods.removeByNameAndDescriptor(m);
        }
        methods.addAllIfNotPresent(inheritedMethods);
        methods.removeLessSpecifics();
        methods.compactAndTrim();
        res = methods.getArray();
        if (rd != null) {
            rd.publicMethods = res;
        }
        return res;
    }


    //
    // 字段、方法、构造方法查找辅助方法
    //

    private static Field searchFields(Field[] fields, String name) {
        String internedName = name.intern();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName() == internedName) {
                return getReflectionFactory().copyField(fields[i]);
            }
        }
        return null;
    }

    private Field getField0(String name) throws NoSuchFieldException {
        // 注意：此例程使用的搜索算法应与privateGetPublicFields()的顺序一致。
        // 但它仅获取每个类声明的公共字段，以减少在请求字段在查询类中声明的常见情况下需要创建的Field对象数量。
        Field res;
        // 搜索声明的公共字段
        if ((res = searchFields(privateGetDeclaredFields(true), name)) != null) {
            return res;
        }
        // 直接超接口，递归
        Class<?>[] interfaces = getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class<?> c = interfaces[i];
            if ((res = c.getField0(name)) != null) {
                return res;
            }
        }
        // 直接超类，递归
        if (!isInterface()) {
            Class<?> c = getSuperclass();
            if (c != null) {
                if ((res = c.getField0(name)) != null) {
                    return res;
                }
            }
        }
        return null;
    }

    private static Method searchMethods(Method[] methods,
                                        String name,
                                        Class<?>[] parameterTypes)
    {
        Method res = null;
        String internedName = name.intern();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (m.getName() == internedName
                && arrayContentsEq(parameterTypes, m.getParameterTypes())
                && (res == null
                    || res.getReturnType().isAssignableFrom(m.getReturnType())))
                res = m;
        }

        return (res == null ? res : getReflectionFactory().copyMethod(res));
    }

    private Method getMethod0(String name, Class<?>[] parameterTypes, boolean includeStaticMethods) {
        MethodArray interfaceCandidates = new MethodArray(2);
        Method res =  privateGetMethodRecursive(name, parameterTypes, includeStaticMethods, interfaceCandidates);
        if (res != null)
            return res;

        // 未在类或超类中直接找到
        interfaceCandidates.removeLessSpecifics();
        return interfaceCandidates.getFirst(); // 可能为null
    }

    private Method privateGetMethodRecursive(String name,
            Class<?>[] parameterTypes,
            boolean includeStaticMethods,
            MethodArray allInterfaceCandidates) {
        // 注意：此例程使用的搜索算法应与privateGetPublicMethods()的顺序一致。
        // 但它仅获取每个类声明的公共方法，以减少在请求方法在查询类中声明的常见情况下需要创建的Method对象数量。
        // 由于默认方法的存在，除非在超类中找到方法，否则需要考虑任何超接口中声明的方法。
        // 在{@code allInterfaceCandidates}中收集超接口中声明的所有候选方法，并在超类中未找到匹配项时选择最具体的方法。

        // 禁止返回根方法
        Method res;
        // 搜索声明的公共方法
        if ((res = searchMethods(privateGetDeclaredMethods(true),
                                 name,
                                 parameterTypes)) != null) {
            if (includeStaticMethods || !Modifier.isStatic(res.getModifiers()))
                return res;
        }
        // 搜索超类的方法
        if (!isInterface()) {
            Class<? super T> c = getSuperclass();
            if (c != null) {
                if ((res = c.getMethod0(name, parameterTypes, true)) != null) {
                    return res;
                }
            }
        }
        // 搜索超接口的方法
        Class<?>[] interfaces = getInterfaces();
        for (Class<?> c : interfaces)
            if ((res = c.getMethod0(name, parameterTypes, false)) != null)
                allInterfaceCandidates.add(res);
        // 未找到
        return null;
    }

    private Constructor<T> getConstructor0(Class<?>[] parameterTypes,
                                        int which) throws NoSuchMethodException
    {
        Constructor<T>[] constructors = privateGetDeclaredConstructors((which == Member.PUBLIC));
        for (Constructor<T> constructor : constructors) {
            if (arrayContentsEq(parameterTypes,
                                constructor.getParameterTypes())) {
                return getReflectionFactory().copyConstructor(constructor);
            }
        }
        throw new NoSuchMethodException(getName() + ".<init>" + argumentTypesToString(parameterTypes));
    }

    //
    // 其他辅助方法和基础实现
    //

    private static boolean arrayContentsEq(Object[] a1, Object[] a2) {
        if (a1 == null) {
            return a2 == null || a2.length == 0;
        }

        if (a2 == null) {
            return a1.length == 0;
        }

        if (a1.length != a2.length) {
            return false;
        }

        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }

        return true;
    }

    private static Field[] copyFields(Field[] arg) {
        Field[] out = new Field[arg.length];
        ReflectionFactory fact = getReflectionFactory();
        for (int i = 0; i < arg.length; i++) {
            out[i] = fact.copyField(arg[i]);
        }
        return out;
    }

    private static Method[] copyMethods(Method[] arg) {
        Method[] out = new Method[arg.length];
        ReflectionFactory fact = getReflectionFactory();
        for (int i = 0; i < arg.length; i++) {
            out[i] = fact.copyMethod(arg[i]);
        }
        return out;
    }

    private static <U> Constructor<U>[] copyConstructors(Constructor<U>[] arg) {
        Constructor<U>[] out = arg.clone();
        ReflectionFactory fact = getReflectionFactory();
        for (int i = 0; i < out.length; i++) {
            out[i] = fact.copyConstructor(out[i]);
        }
        return out;
    }

    private native Field[]       getDeclaredFields0(boolean publicOnly);
    private native Method[]      getDeclaredMethods0(boolean publicOnly);
    private native Constructor<T>[] getDeclaredConstructors0(boolean publicOnly);
    private native Class<?>[]   getDeclaredClasses0();

    private static String        argumentTypesToString(Class<?>[] argTypes) {
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        if (argTypes != null) {
            for (int i = 0; i < argTypes.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                Class<?> c = argTypes[i];
                buf.append((c == null) ? "null" : c.getName());
            }
        }
        buf.append(")");
        return buf.toString();
    }

    /** 使用JDK 1.1的serialVersionUID以实现互操作性 */
    private static final long serialVersionUID = 3206093459760846163L;


    /**
     * Class类在序列化流协议中是特殊处理的。
     *
     * Class实例最初以以下格式写入ObjectOutputStream：
     * <pre>
     *      {@code TC_CLASS} ClassDescriptor
     *      ClassDescriptor是{@code java.io.ObjectStreamClass}实例的特殊序列化形式。
     * </pre>
     * 首次将类描述符写入流时会生成新句柄。后续对类描述符的引用将作为对初始类描述符实例的引用写入。
     *
     * @see java.io.ObjectStreamClass
     */
    private static final ObjectStreamField[] serialPersistentFields =
        new ObjectStreamField[0];

        
    /**
     * 返回如果在此方法调用时初始化此类将分配给该类的断言状态。如果此类已设置断言状态，将返回最新设置；否则，如果有任何适用于此类的包默认断言状态，将返回最新的最具体包默认断言状态设置；否则，如果此类不是系统类（即它有类加载器），则返回其类加载器的默认断言状态；否则，返回系统类的默认断言状态。
     * <p>
     * 很少有程序员需要此方法；它是为JRE自身提供的。（它允许类在初始化时确定是否应启用断言。）请注意，此方法不保证返回与指定类在初始化时（或将在初始化时）关联的实际断言状态。
     *
     * @return 指定类的所需断言状态。
     * @see    java.lang.ClassLoader#setClassAssertionStatus
     * @see    java.lang.ClassLoader#setPackageAssertionStatus
     * @see    java.lang.ClassLoader#setDefaultAssertionStatus
     * @since  1.4
     */
    public boolean desiredAssertionStatus() {
        ClassLoader loader = getClassLoader();
        // 如果加载器为null，则这是系统类，因此询问VM
        if (loader == null)
            return desiredAssertionStatus0(this);

        // 如果类加载器已使用断言指令初始化，则询问它。否则，询问VM。
        synchronized(loader.assertionLock) {
            if (loader.classAssertionStatus != null) {
                return loader.desiredAssertionStatus(getName());
            }
        }
        return desiredAssertionStatus0(this);
    }

    // 从VM检索此类的所需断言状态
    private static native boolean desiredAssertionStatus0(Class<?> clazz);

    /**
     * 当且仅当该类在源代码中声明为枚举时返回true。
     *
     * @return 当且仅当该类在源代码中声明为枚举时返回true
     * @since 1.5
     */
    public boolean isEnum() {
        // 枚举必须同时直接继承java.lang.Enum并设置ENUM标志；专用枚举常量的类不执行前者。
        return (this.getModifiers() & ENUM) != 0 &&
            this.getSuperclass() == java.lang.Enum.class;
    }

    // 获取反射对象的工厂
    private static ReflectionFactory getReflectionFactory() {
        if (reflectionFactory == null) {
            reflectionFactory =
                java.security.AccessController.doPrivileged
                    (new sun.reflect.ReflectionFactory.GetReflectionFactoryAction());
        }
        return reflectionFactory;
    }
    private static ReflectionFactory reflectionFactory;

    // 以便在系统属性可用时立即查询
    private static boolean initted = false;
    private static void checkInitted() {
        if (initted) return;
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    // 测试以确保系统属性表完全初始化。这是必需的，因为反射代码在初始化过程中很早就被调用（在解析命令行参数并因此安装这些用户可设置的属性之前）。我们假设如果System.out不为null，则System类已完全初始化，并且大部分启动代码已运行。

                    if (System.out == null) {
                        // java.lang.System尚未完全初始化
                        return null;
                    }

                    // 不使用Boolean.getBoolean以避免类初始化。
                    String val =
                        System.getProperty("sun.reflect.noCaches");
                    if (val != null && val.equals("true")) {
                        useCaches = false;
                    }

                    initted = true;
                    return null;
                }
            });
    }

    /**
     * 返回此枚举类的元素，如果此Class对象不表示枚举类型，则返回null。
     *
     * @return 包含由此Class对象表示的枚举类的枚举值的数组（按声明顺序），如果此Class对象不表示枚举类型，则返回null
     * @since 1.5
     */
    public T[] getEnumConstants() {
        T[] values = getEnumConstantsShared();
        return (values != null) ? values.clone() : null;
    }

    /**
     * 返回此枚举类的元素，如果此Class对象不表示枚举类型，则返回null；与getEnumConstants相同，但结果未克隆、已缓存并由所有调用者共享。
     */
    T[] getEnumConstantsShared() {
        if (enumConstants == null) {
            if (!isEnum()) return null;
            try {
                final Method values = getMethod("values");
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<Void>() {
                        public Void run() {
                                values.setAccessible(true);
                                return null;
                            }
                        });
                @SuppressWarnings("unchecked")
                T[] temporaryConstants = (T[])values.invoke(null);
                enumConstants = temporaryConstants;
            }
            // 当用户构造不符合枚举规范的类似枚举的类时，可能会发生这些异常。
            catch (InvocationTargetException | NoSuchMethodException |
                IllegalAccessException ex) { return null; }
        }
        return enumConstants;
    }
    private volatile transient T[] enumConstants = null;

    /**
     * 返回从简单名称到枚举常量的映射。此包私有方法由Enum内部用于高效实现{@code public static <T extends Enum<T>> T valueOf(Class<T>, String)}。注意，此方法返回的映射是在首次使用时延迟创建的。通常它永远不会被创建。
     */
    Map<String, T> enumConstantDirectory() {
        if (enumConstantDirectory == null) {
            T[] universe = getEnumConstantsShared();
            if (universe == null)
                throw new IllegalArgumentException(
                    getName() + " is not an enum type");
            Map<String, T> m = new HashMap<>(2 * universe.length);
            for (T constant : universe)
                m.put(((Enum<?>)constant).name(), constant);
            enumConstantDirectory = m;
        }
        return enumConstantDirectory;
    }
    private volatile transient Map<String, T> enumConstantDirectory = null;

    /**
     * 将对象强制转换为此{@code Class}对象表示的类或接口。
     *
     * @param obj 要强制转换的对象
     * @return 转换后的对象，如果obj为null则返回null
     *
     * @throws ClassCastException 如果对象不为null且不可分配给类型T。
     *
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public T cast(Object obj) {
        if (obj != null && !isInstance(obj))
            throw new ClassCastException(cannotCastMsg(obj));
        return (T) obj;
    }

    private String cannotCastMsg(Object obj) {
        return "Cannot cast " + obj.getClass().getName() + " to " + getName();
    }

    /**
     * 将此{@code Class}对象强制转换为表示指定类对象所表示的类的子类。检查强制转换是否有效，如果无效则抛出{@code ClassCastException}。如果此方法成功，它始终返回对此类对象的引用。
     *
     * <p>当客户端需要“缩小”{@code Class}对象的类型以将其传递给限制其愿意接受的{@code Class}对象的API时，此方法非常有用。强制转换会生成编译时警告，因为强制转换的正确性无法在运行时检查（因为泛型类型是通过擦除实现的）。
     *
     * @param <U> 要将此类对象强制转换为的类型
     * @param clazz 要将此类对象强制转换为的类型的类
     * @return 此{@code Class}对象，强制转换为表示指定类对象的子类。
     * @throws ClassCastException 如果此{@code Class}对象不表示指定类的子类（此处“子类”包括类本身）。
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public <U> Class<? extends U> asSubclass(Class<U> clazz) {
        if (clazz.isAssignableFrom(this))
            return (Class<? extends U>) this;
        else
            throw new ClassCastException(this.toString());
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);

        return (A) annotationData().annotations.get(annotationClass);
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return GenericDeclaration.super.isAnnotationPresent(annotationClass);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);

        AnnotationData annotationData = annotationData();
        return AnnotationSupport.getAssociatedAnnotations(annotationData.declaredAnnotations,
                                                        this,
                                                        annotationClass);
    }

    /**
     * @since 1.5
     */
    public Annotation[] getAnnotations() {
        return AnnotationParser.toArray(annotationData().annotations);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);

        return (A) annotationData().declaredAnnotations.get(annotationClass);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);

        return AnnotationSupport.getDirectlyAndIndirectlyPresent(annotationData().declaredAnnotations,
                                                                    annotationClass);
    }

    /**
     * @since 1.5
     */
    public Annotation[] getDeclaredAnnotations()  {
        return AnnotationParser.toArray(annotationData().declaredAnnotations);
    }

    // 当JVM TI调用RedefineClasses()时可能失效的注解数据
    private static class AnnotationData {
        final Map<Class<? extends Annotation>, Annotation> annotations;         // 注解（继承+声明）
        final Map<Class<? extends Annotation>, Annotation> declaredAnnotations; // 声明的注解
        final int redefinedCount;                                              // 创建此AnnotationData实例时的classRedefinedCount值

        AnnotationData(Map<Class<? extends Annotation>, Annotation> annotations,
                    Map<Class<? extends Annotation>, Annotation> declaredAnnotations,
                    int redefinedCount) {
            this.annotations = annotations;
            this.declaredAnnotations = declaredAnnotations;
            this.redefinedCount = redefinedCount;
        }
    }

    // 注解缓存
    @SuppressWarnings("UnusedDeclaration")
    private volatile transient AnnotationData annotationData;

    private AnnotationData annotationData() {
        while (true) { // 重试循环
            AnnotationData annotationData = this.annotationData;
            int classRedefinedCount = this.classRedefinedCount;
            if (annotationData != null &&
                annotationData.redefinedCount == classRedefinedCount) {
                return annotationData;
            }
            // 注解数据为null或过期 -> 乐观创建新实例
            AnnotationData newAnnotationData = createAnnotationData(classRedefinedCount);
            // 尝试安装
            if (Atomic.casAnnotationData(this, annotationData, newAnnotationData)) {
                // 成功安装新AnnotationData
                return newAnnotationData;
            }
        }
    }

    private AnnotationData createAnnotationData(int classRedefinedCount) {
        Map<Class<? extends Annotation>, Annotation> declaredAnnotations =
            AnnotationParser.parseAnnotations(getRawAnnotations(), getConstantPool(), this);
        Class<?> superClass = getSuperclass();
        Map<Class<? extends Annotation>, Annotation> annotations = null;
        if (superClass != null) {
            Map<Class<? extends Annotation>, Annotation> superAnnotations =
                superClass.annotationData().annotations;
            for (Map.Entry<Class<? extends Annotation>, Annotation> e : superAnnotations.entrySet()) {
                Class<? extends Annotation> annotationClass = e.getKey();
                if (AnnotationType.getInstance(annotationClass).isInherited()) {
                    if (annotations == null) { // 延迟构造
                        annotations = new LinkedHashMap<>((Math.max(
                                declaredAnnotations.size(),
                                Math.min(12, declaredAnnotations.size() + superAnnotations.size())
                            ) * 4 + 2) / 3
                        );
                    }
                    annotations.put(annotationClass, e.getValue());
                }
            }
        }
        if (annotations == null) {
            // 无继承注解 -> 共享声明注解的Map
            annotations = declaredAnnotations;
        } else {
            // 至少有一个继承注解 -> 声明注解可能覆盖继承的注解
            annotations.putAll(declaredAnnotations);
        }
        return new AnnotationData(annotations, declaredAnnotations, classRedefinedCount);
    }

    // 注解类型缓存其内部（AnnotationType）形式
    @SuppressWarnings("UnusedDeclaration")
    private volatile transient AnnotationType annotationType;

    boolean casAnnotationType(AnnotationType oldType, AnnotationType newType) {
        return Atomic.casAnnotationType(this, oldType, newType);
    }

    AnnotationType getAnnotationType() {
        return annotationType;
    }

    Map<Class<? extends Annotation>, Annotation> getDeclaredAnnotationMap() {
        return annotationData().declaredAnnotations;
    }

    /* 与此类相关的用户定义值的后备存储。由ClassValue类维护。 */
    transient ClassValue.ClassValueMap classValueMap;

    /**
     * 返回一个{@code AnnotatedType}对象，该对象表示使用类型来指定由此{@code Class}对象表示的实体的超类。（在“... extends Foo”中使用类型Foo来指定超类与类型Foo的声明不同。）
     *
     * <p>如果此{@code Class}对象表示其声明未明确指示带注解的超类的类型，则返回值是表示没有注解的元素的{@code AnnotatedType}对象。
     *
     * <p>如果此{@code Class}表示{@code Object}类、接口类型、数组类型、基本类型或void，则返回值为{@code null}。
     *
     * @return 表示超类的对象
     * @since 1.8
     */
    public AnnotatedType getAnnotatedSuperclass() {
        if (this == Object.class ||
                isInterface() ||
                isArray() ||
                isPrimitive() ||
                this == Void.TYPE) {
            return null;
        }

        return TypeAnnotationParser.buildAnnotatedSuperclass(getRawTypeAnnotations(), getConstantPool(), this);
    }

    /**
     * 返回{@code AnnotatedType}对象数组，这些对象表示使用类型来指定由此{@code Class}对象表示的实体的超接口。（在“... implements Foo”中使用类型Foo来指定超接口与类型Foo的声明不同。）
     *
     * <p>如果此{@code Class}对象表示类，则返回值是包含表示类实现的接口类型的对象的数组。数组中对象的顺序对应于此{@code Class}对象声明的“implements”子句中使用的接口类型的顺序。
     *
     * <p>如果此{@code Class}对象表示接口，则返回值是包含表示接口直接扩展的接口类型的对象的数组。数组中对象的顺序对应于此{@code Class}对象声明的“extends”子句中使用的接口类型的顺序。
     *
     * <p>如果此{@code Class}对象表示其声明未明确指示任何带注解的超接口的类或接口，则返回值是长度为0的数组。
     *
     * <p>如果此{@code Class}对象表示{@code Object}类、数组类型、基本类型或void，则返回值是长度为0的数组。
     *
     * @return 表示超接口的数组
     * @since 1.8
     */
    public AnnotatedType[] getAnnotatedInterfaces() {
        return TypeAnnotationParser.buildAnnotatedInterfaces(getRawTypeAnnotations(), getConstantPool(), this);
    }

}
