
/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.reflect;

import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import sun.misc.ProxyGenerator;
import sun.misc.VM;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.reflect.misc.ReflectUtil;
import sun.security.util.SecurityConstants;

/**
 * {@code Proxy} 提供了用于创建动态代理类和实例的静态方法，并且也是通过这些方法创建的所有动态代理类的超类。
 *
 * <p>要为某个接口 {@code Foo} 创建代理：
 * <pre>
 *     InvocationHandler handler = new MyInvocationHandler(...);
 *     Class&lt;?&gt; proxyClass = Proxy.getProxyClass(Foo.class.getClassLoader(), Foo.class);
 *     Foo f = (Foo) proxyClass.getConstructor(InvocationHandler.class).
 *                     newInstance(handler);
 * </pre>
 * 或者更简单地：
 * <pre>
 *     Foo f = (Foo) Proxy.newProxyInstance(Foo.class.getClassLoader(),
 *                                          new Class&lt;?&gt;[] { Foo.class },
 *                                          handler);
 * </pre>
 *
 * <p><i>动态代理类</i>（以下简称<i>代理类</i>）是在类创建时指定的接口列表，并且具有以下行为。
 *
 * <i>代理接口</i>是指由代理类实现的接口。
 *
 * <i>代理实例</i>是指代理类的实例。
 *
 * 每个代理实例都有一个关联的<i>调用处理器</i>对象，该对象实现了 {@link InvocationHandler} 接口。
 * 通过代理实例的代理接口之一调用方法时，将分派到实例的调用处理器的 {@link InvocationHandler#invoke
 * invoke} 方法，传递代理实例、标识被调用方法的 {@code java.lang.reflect.Method} 对象和类型为 {@code Object}
 * 的参数数组。调用处理器根据需要处理编码的方法调用，并返回的结果将作为代理实例的方法调用的结果返回。
 *
 * <p>代理类具有以下属性：
 *
 * <ul>
 * <li>如果所有代理接口都是公共的，则代理类是<em>公共、最终且不是抽象的</em>。</li>
 *
 * <li>如果任何代理接口是非公共的，则代理类是<em>非公共、最终且不是抽象的</em>。</li>
 *
 * <li>代理类的未限定名称是未指定的。然而，以字符串 {@code "$Proxy"}
 * 开头的类名空间应保留给代理类。</li>
 *
 * <li>代理类扩展 {@code java.lang.reflect.Proxy}。</li>
 *
 * <li>代理类实现的接口在创建时指定，顺序相同。</li>
 *
 * <li>如果代理类实现非公共接口，则它将定义在与该接口相同的包中。否则，代理类的包也是未指定的。注意，包密封不会阻止代理类在运行时在特定包中成功定义，类加载器和相同包中已定义的类的特定签名者也不会。</li>
 *
 * <li>由于代理类实现了在创建时指定的所有接口，因此在其 {@code Class} 对象上调用 {@code getInterfaces}
 * 将返回包含相同接口列表的数组（按创建时指定的顺序），在其 {@code Class} 对象上调用 {@code getMethods}
 * 将返回包含这些接口中所有方法的 {@code Method} 对象数组，调用 {@code getMethod} 将找到代理接口中的方法，如预期的那样。</li>
 *
 * <li>{@link Proxy#isProxyClass Proxy.isProxyClass} 方法将返回 true，如果传入的是代理类——由
 * {@code Proxy.getProxyClass} 返回的类或由 {@code Proxy.newProxyInstance} 返回的对象的类——否则返回 false。</li>
 *
 * <li>代理类的 {@code java.security.ProtectionDomain} 与引导类加载器加载的系统类（如 {@code java.lang.Object}）相同，因为代理类的代码是由受信任的系统代码生成的。此保护域通常会被授予
 * {@code java.security.AllPermission}。</li>
 *
 * <li>每个代理类都有一个公共构造函数，接受一个 {@link InvocationHandler} 接口的实现，用于设置代理实例的调用处理器。除了使用反射 API 访问公共构造函数外，还可以通过调用
 * {@link Proxy#newProxyInstance Proxy.newProxyInstance} 方法来创建代理实例，该方法结合了调用
 * {@link Proxy#getProxyClass Proxy.getProxyClass} 和使用调用处理器调用构造函数的操作。
 * </ul>
 *
 * <p>代理实例具有以下属性：
 *
 * <ul>
 * <li>给定一个代理实例 {@code proxy} 和其代理类实现的接口之一 {@code Foo}，以下表达式将返回 true：
 * <pre>
 *     {@code proxy instanceof Foo}
 * </pre>
 * 以下类型转换操作将成功（而不是抛出 {@code ClassCastException}）：
 * <pre>
 *     {@code (Foo) proxy}
 * </pre>
 *
 * <li>每个代理实例都有一个关联的调用处理器，即传递给其构造函数的调用处理器。静态
 * {@link Proxy#getInvocationHandler Proxy.getInvocationHandler} 方法将返回与传入的代理实例关联的调用处理器。</li>
 *
 * <li>在代理实例上调用接口方法时，将编码并分派到调用处理器的 {@link
 * InvocationHandler#invoke invoke} 方法，如该方法的文档所述。</li>
 *
 * <li>在代理实例上调用 {@code hashCode}、
 * {@code equals} 或 {@code toString} 方法（这些方法在
 * {@code java.lang.Object} 中声明）时，将编码并分派到调用处理器的 {@code invoke} 方法，与接口方法调用的编码和分派方式相同，如上所述。传递给 {@code invoke} 的
 * {@code Method} 对象的声明类将是 {@code java.lang.Object}。代理实例从 {@code java.lang.Object}
 * 继承的其他公共方法不由代理类覆盖，因此这些方法的调用行为与 {@code java.lang.Object} 实例的调用行为相同。</li>
 * </ul>
 *
 * <h3>在多个代理接口中重复的方法</h3>
 *
 * <p>当代理类的两个或多个接口包含具有相同名称和参数签名的方法时，代理类的接口顺序变得重要。当这样的<i>重复方法</i>
 * 在代理实例上调用时，传递给调用处理器的 {@code Method} 对象不一定是可以从调用方法的接口引用类型的声明类。这个限制存在是因为生成的代理类中的相应方法实现无法确定它是通过哪个接口调用的。因此，当在代理实例上调用重复方法时，传递给调用处理器的 {@code invoke} 方法的
 * {@code Method} 对象是代理类接口列表中最前面包含该方法的接口（直接或通过超接口继承）的方法，无论方法调用是通过哪个引用类型发生的。
 *
 * <p>如果代理接口包含与 {@code java.lang.Object} 中的 {@code hashCode}、
 * {@code equals} 或 {@code toString} 方法具有相同名称和参数签名的方法，当在代理实例上调用这些方法时，传递给调用处理器的
 * {@code Method} 对象的声明类将是 {@code java.lang.Object}。换句话说，{@code java.lang.Object}
 * 的公共、非最终方法在确定传递给调用处理器的 {@code Method} 对象时，逻辑上先于所有代理接口。
 *
 * <p>还应注意，当重复方法被分派到调用处理器时，{@code invoke} 方法只能抛出可以分配给所有可以通过的代理接口的方法的
 * {@code throws} 子句中的异常类型的检查异常。如果 {@code invoke} 方法抛出一个检查异常，该异常不能分配给可以通过的代理接口的方法的
 * {@code throws} 子句中的任何异常类型，则将由代理实例的调用抛出未检查的 {@code UndeclaredThrowableException}。
 * 这个限制意味着，通过调用 {@code getExceptionTypes} 传递给 {@code invoke} 方法的
 * {@code Method} 对象返回的异常类型不一定都能由 {@code invoke} 方法成功抛出。
 *
 * @author      Peter Jones
 * @see         InvocationHandler
 * @since       1.3
 */
public class Proxy implements java.io.Serializable {

    private static final long serialVersionUID = -2222568056686623797L;

    /** 代理类构造函数的参数类型 */
    private static final Class<?>[] constructorParams =
        { InvocationHandler.class };

    /**
     * 代理类的缓存
     */
    private static final WeakCache<ClassLoader, Class<?>[], Class<?>>
        proxyClassCache = new WeakCache<>(new KeyFactory(), new ProxyClassFactory());

    /**
     * 该代理实例的调用处理器。
     * @serial
     */
    protected InvocationHandler h;

    /**
     * 禁止实例化。
     */
    private Proxy() {
    }

    /**
     * 从子类（通常是动态代理类）构造新的 {@code Proxy} 实例，并指定其调用处理器的值。
     *
     * @param  h 该代理实例的调用处理器
     *
     * @throws NullPointerException 如果给定的调用处理器 {@code h} 为 {@code null}。
     */
    protected Proxy(InvocationHandler h) {
        Objects.requireNonNull(h);
        this.h = h;
    }

    /**
     * 给定类加载器和接口数组，返回代理类的 {@code java.lang.Class} 对象。代理类将由指定的类加载器定义，并实现所有提供的接口。如果给定的接口中有任何是非公共的，代理类也将是非公共的。如果类加载器已经定义了相同接口组合的代理类，则返回现有的代理类；否则，将动态生成实现这些接口的代理类，并由类加载器定义。
     *
     * <p>传递给 {@code Proxy.getProxyClass} 的参数有几项限制：
     *
     * <ul>
     * <li>{@code interfaces} 数组中的所有 {@code Class} 对象都必须表示接口，而不是类或基本类型。
     *
     * <li>{@code interfaces} 数组中不能有两个元素引用相同的 {@code Class} 对象。
     *
     * <li>所有接口类型必须通过指定的类加载器按名称可见。换句话说，对于类加载器
     * {@code cl} 和每个接口 {@code i}，以下表达式必须为真：
     * <pre>
     *     Class.forName(i.getName(), false, cl) == i
     * </pre>
     *
     * <li>所有非公共接口必须在同一个包中；否则，无论代理类定义在哪个包中，都无法实现所有接口。
     *
     * <li>对于指定接口的具有相同签名的成员方法集：
     * <ul>
     * <li>如果任何方法的返回类型是基本类型或 void，则所有方法必须具有相同的返回类型。
     * <li>否则，其中一个方法必须具有可以分配给其余方法的所有返回类型的返回类型。
     * </ul>
     *
     * <li>生成的代理类不得超出虚拟机对类施加的任何限制。例如，虚拟机可能限制类可以实现的接口数量为 65535；在这种情况下，{@code interfaces} 数组的大小不得超过 65535。
     * </ul>
     *
     * <p>如果违反了这些限制中的任何一项，
     * {@code Proxy.getProxyClass} 将抛出 {@code IllegalArgumentException}。如果 {@code interfaces}
     * 数组参数或其任何元素为 {@code null}，将抛出 {@code NullPointerException}。
     *
     * <p>注意，指定的代理接口的顺序是重要的：对于具有相同接口组合但顺序不同的两个请求，将生成两个不同的代理类。
     *
     * @param   loader 定义代理类的类加载器
     * @param   interfaces 代理类要实现的接口列表
     * @return  在指定类加载器中定义并实现指定接口的代理类
     * @throws  IllegalArgumentException 如果违反了可以传递给 {@code getProxyClass} 的参数的任何限制
     * @throws  SecurityException 如果存在安全管理器 <em>s</em>，并且满足以下任何条件：
     *          <ul>
     *             <li>给定的 {@code loader} 为 {@code null} 且调用者的类加载器不为 {@code null}，并且调用
     *             {@link SecurityManager#checkPermission s.checkPermission} 时，
     *             {@code RuntimePermission("getClassLoader")} 权限被拒绝。</li>
     *             <li>对于每个代理接口，{@code intf}，调用者的类加载器不是 {@code intf} 的类加载器或其祖先，并且调用
     *             {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} 时，访问 {@code intf} 被拒绝。</li>
     *          </ul>


    /*
     * @throws  NullPointerException if the {@code interfaces} array
     *          argument or any of its elements are {@code null}
     */
    @CallerSensitive
    public static Class<?> getProxyClass(ClassLoader loader,
                                         Class<?>... interfaces)
        throws IllegalArgumentException
    {
        final Class<?>[] intfs = interfaces.clone();
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkProxyAccess(Reflection.getCallerClass(), loader, intfs);
        }

        return getProxyClass0(loader, intfs);
    }

    /*
     * 检查创建代理类所需的权限。
     *
     * 定义代理类时，它执行与 Class.forName（VM 将调用 ClassLoader.checkPackageAccess）相同的访问检查：
     * 1. 如果 loader == null，则进行 "getClassLoader" 权限检查
     * 2. 对实现的接口进行 checkPackageAccess 检查
     *
     * 获取代理类的构造函数和新实例时，它对实现的接口执行包访问检查
     * 与 Class.getConstructor 相同。
     *
     * 如果接口是非公共的，代理类必须由接口的定义加载器定义。如果调用者的类加载器
     * 与接口的定义加载器不同，VM 在通过 defineClass0 方法定义生成的代理类时
     * 将抛出 IllegalAccessError。
     */
    private static void checkProxyAccess(Class<?> caller,
                                         ClassLoader loader,
                                         Class<?>... interfaces)
    {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader ccl = caller.getClassLoader();
            if (VM.isSystemDomainLoader(loader) && !VM.isSystemDomainLoader(ccl)) {
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
            }
            ReflectUtil.checkProxyPackageAccess(ccl, interfaces);
        }
    }

    /**
     * 生成代理类。调用此方法之前必须调用 checkProxyAccess 方法
     * 以执行权限检查。
     */
    private static Class<?> getProxyClass0(ClassLoader loader,
                                           Class<?>... interfaces) {
        if (interfaces.length > 65535) {
            throw new IllegalArgumentException("接口数量超出限制");
        }

        // 如果给定加载器定义的实现给定接口的代理类已存在，这将简单地返回缓存的副本；
        // 否则，它将通过 ProxyClassFactory 创建代理类
        return proxyClassCache.get(loader, interfaces);
    }

    /*
     * 用于实现 0 个接口的代理类的键
     */
    private static final Object key0 = new Object();

    /*
     * Key1 和 Key2 优化了实现 1 个或 2 个接口的动态代理的常见用法。
     */

    /*
     * 用于实现 1 个接口的代理类的键
     */
    private static final class Key1 extends WeakReference<Class<?>> {
        private final int hash;

        Key1(Class<?> intf) {
            super(intf);
            this.hash = intf.hashCode();
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            Class<?> intf;
            return this == obj ||
                   obj != null &&
                   obj.getClass() == Key1.class &&
                   (intf = get()) != null &&
                   intf == ((Key1) obj).get();
        }
    }

    /*
     * 用于实现 2 个接口的代理类的键
     */
    private static final class Key2 extends WeakReference<Class<?>> {
        private final int hash;
        private final WeakReference<Class<?>> ref2;

        Key2(Class<?> intf1, Class<?> intf2) {
            super(intf1);
            hash = 31 * intf1.hashCode() + intf2.hashCode();
            ref2 = new WeakReference<Class<?>>(intf2);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            Class<?> intf1, intf2;
            return this == obj ||
                   obj != null &&
                   obj.getClass() == Key2.class &&
                   (intf1 = get()) != null &&
                   intf1 == ((Key2) obj).get() &&
                   (intf2 = ref2.get()) != null &&
                   intf2 == ((Key2) obj).ref2.get();
        }
    }

    /*
     * 用于实现任意数量接口的代理类的键
     * （此处仅用于 3 个或更多接口）
     */
    private static final class KeyX {
        private final int hash;
        private final WeakReference<Class<?>>[] refs;

        @SuppressWarnings("unchecked")
        KeyX(Class<?>[] interfaces) {
            hash = Arrays.hashCode(interfaces);
            refs = (WeakReference<Class<?>>[])new WeakReference<?>[interfaces.length];
            for (int i = 0; i < interfaces.length; i++) {
                refs[i] = new WeakReference<>(interfaces[i]);
            }
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj ||
                   obj != null &&
                   obj.getClass() == KeyX.class &&
                   equals(refs, ((KeyX) obj).refs);
        }

        private static boolean equals(WeakReference<Class<?>>[] refs1,
                                      WeakReference<Class<?>>[] refs2) {
            if (refs1.length != refs2.length) {
                return false;
            }
            for (int i = 0; i < refs1.length; i++) {
                Class<?> intf = refs1[i].get();
                if (intf == null || intf != refs2[i].get()) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 一个将接口数组映射到最优键的函数，其中表示接口的 Class 对象是弱引用的。
     */
    private static final class KeyFactory
        implements BiFunction<ClassLoader, Class<?>[], Object>
    {
        @Override
        public Object apply(ClassLoader classLoader, Class<?>[] interfaces) {
            switch (interfaces.length) {
                case 1: return new Key1(interfaces[0]); // 最常见的
                case 2: return new Key2(interfaces[0], interfaces[1]);
                case 0: return key0;
                default: return new KeyX(interfaces);
            }
        }
    }

    /**
     * 一个生成、定义并返回给定类加载器和接口数组的代理类的工厂函数。
     */
    private static final class ProxyClassFactory
        implements BiFunction<ClassLoader, Class<?>[], Class<?>>
    {
        // 所有代理类名称的前缀
        private static final String proxyClassNamePrefix = "$Proxy";

        // 用于生成唯一代理类名称的下一个数字
        private static final AtomicLong nextUniqueNumber = new AtomicLong();

        @Override
        public Class<?> apply(ClassLoader loader, Class<?>[] interfaces) {

            Map<Class<?>, Boolean> interfaceSet = new IdentityHashMap<>(interfaces.length);
            for (Class<?> intf : interfaces) {
                /*
                 * 验证类加载器解析此接口的名称是否为相同的 Class 对象。
                 */
                Class<?> interfaceClass = null;
                try {
                    interfaceClass = Class.forName(intf.getName(), false, loader);
                } catch (ClassNotFoundException e) {
                }
                if (interfaceClass != intf) {
                    throw new IllegalArgumentException(
                        intf + " 在类加载器中不可见: " + loader);
                }
                /*
                 * 验证 Class 对象实际上表示一个接口。
                 */
                if (!interfaceClass.isInterface()) {
                    throw new IllegalArgumentException(
                        interfaceClass.getName() + " 不是一个接口");
                }
                /*
                 * 验证此接口不是重复的。
                 */
                if (interfaceSet.put(interfaceClass, Boolean.TRUE) != null) {
                    throw new IllegalArgumentException(
                        "重复的接口: " + interfaceClass.getName());
                }
            }

            String proxyPkg = null;     // 定义代理类的包
            int accessFlags = Modifier.PUBLIC | Modifier.FINAL;

            /*
             * 记录非公共代理接口的包，以便在相同的包中定义代理类。验证所有非公共代理接口都在相同的包中。
             */
            for (Class<?> intf : interfaces) {
                int flags = intf.getModifiers();
                if (!Modifier.isPublic(flags)) {
                    accessFlags = Modifier.FINAL;
                    String name = intf.getName();
                    int n = name.lastIndexOf('.');
                    String pkg = ((n == -1) ? "" : name.substring(0, n + 1));
                    if (proxyPkg == null) {
                        proxyPkg = pkg;
                    } else if (!pkg.equals(proxyPkg)) {
                        throw new IllegalArgumentException(
                            "非公共接口来自不同的包");
                    }
                }
            }

            if (proxyPkg == null) {
                // 如果没有非公共代理接口，使用 com.sun.proxy 包
                proxyPkg = ReflectUtil.PROXY_PACKAGE + ".";
            }

            /*
             * 选择要生成的代理类的名称。
             */
            long num = nextUniqueNumber.getAndIncrement();
            String proxyName = proxyPkg + proxyClassNamePrefix + num;

            /*
             * 生成指定的代理类。
             */
            byte[] proxyClassFile = ProxyGenerator.generateProxyClass(
                proxyName, interfaces, accessFlags);
            try {
                return defineClass0(loader, proxyName,
                                    proxyClassFile, 0, proxyClassFile.length);
            } catch (ClassFormatError e) {
                /*
                 * 这里的 ClassFormatError 意味着（排除代理类生成代码中的错误）提供的参数
                 * 存在某些其他无效的方面（例如虚拟机限制超出）。
                 */
                throw new IllegalArgumentException(e.toString());
            }
        }
    }

    /**
     * 返回一个代理类的实例，该类实现了指定的接口，并将方法调用分派到指定的调用处理程序。
     *
     * <p>{@code Proxy.newProxyInstance} 抛出 {@code IllegalArgumentException} 的原因与
     * {@code Proxy.getProxyClass} 相同。
     *
     * @param   loader 定义代理类的类加载器
     * @param   interfaces 代理类要实现的接口列表
     * @param   h 分派方法调用的调用处理程序
     * @return  一个具有指定调用处理程序的代理实例，该代理类由指定的类加载器定义
     *          并实现了指定的接口
     * @throws  IllegalArgumentException 如果违反了传递给 {@code getProxyClass}
     *          的参数的任何限制
     * @throws  SecurityException 如果存在安全管理器，<em>s</em>，并且满足以下任一条件：
     *          <ul>
     *          <li> 给定的 {@code loader} 为 {@code null} 且
     *               调用者的类加载器不为 {@code null}，并且调用
     *               {@link SecurityManager#checkPermission s.checkPermission} 时
     *               使用 {@code RuntimePermission("getClassLoader")} 权限拒绝访问；</li>
     *          <li> 对于每个代理接口，{@code intf}，
     *               调用者的类加载器不是 {@code intf} 的类加载器或其祖先，并且调用
     *               {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} 拒绝访问 {@code intf}；</li>
     *          <li> 任何给定的代理接口是非公共的，且调用者类不在与非公共接口相同的 {@linkplain Package 运行时包} 中，
     *               并且调用 {@link SecurityManager#checkPermission s.checkPermission} 时
     *               使用 {@code ReflectPermission("newProxyInPackage.{package name}")} 权限拒绝访问。</li>
     *          </ul>
     * @throws  NullPointerException 如果 {@code interfaces} 数组
     *          参数或其任何元素为 {@code null}，或者调用处理程序，{@code h}，
     *          为 {@code null}
     */
    @CallerSensitive
    public static Object newProxyInstance(ClassLoader loader,
                                          Class<?>[] interfaces,
                                          InvocationHandler h)
        throws IllegalArgumentException
    {
        Objects.requireNonNull(h);

        final Class<?>[] intfs = interfaces.clone();
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkProxyAccess(Reflection.getCallerClass(), loader, intfs);
        }

        /*
         * 查找或生成指定的代理类。
         */
        Class<?> cl = getProxyClass0(loader, intfs);

        /*
         * 使用指定的调用处理程序调用其构造函数。
         */
        try {
            if (sm != null) {
                checkNewProxyPermission(Reflection.getCallerClass(), cl);
            }

            final Constructor<?> cons = cl.getConstructor(constructorParams);
            final InvocationHandler ih = h;
            if (!Modifier.isPublic(cl.getModifiers())) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        cons.setAccessible(true);
                        return null;
                    }
                });
            }
            return cons.newInstance(new Object[]{h});
        } catch (IllegalAccessException|InstantiationException e) {
            throw new InternalError(e.toString(), e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new InternalError(t.toString(), t);
            }
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.toString(), e);
        }
    }


                private static void checkNewProxyPermission(Class<?> caller, Class<?> proxyClass) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (ReflectUtil.isNonPublicProxyClass(proxyClass)) {
                ClassLoader ccl = caller.getClassLoader();
                ClassLoader pcl = proxyClass.getClassLoader();

                // 如果调用者和代理类不在同一个运行时包中，则进行权限检查
                int n = proxyClass.getName().lastIndexOf('.');
                String pkg = (n == -1) ? "" : proxyClass.getName().substring(0, n);

                n = caller.getName().lastIndexOf('.');
                String callerPkg = (n == -1) ? "" : caller.getName().substring(0, n);

                if (pcl != ccl || !pkg.equals(callerPkg)) {
                    sm.checkPermission(new ReflectPermission("newProxyInPackage." + pkg));
                }
            }
        }
    }

    /**
     * 如果且仅当指定的类是使用 {@code getProxyClass} 方法或 {@code newProxyInstance} 方法动态生成的代理类时，返回 true。
     *
     * <p>此方法的可靠性对于使用它进行安全决策的能力至关重要，因此其实现不应仅仅测试该类是否扩展了 {@code Proxy}。
     *
     * @param   cl 要测试的类
     * @return  如果类是代理类则返回 {@code true}，否则返回 {@code false}
     * @throws  NullPointerException 如果 {@code cl} 为 {@code null}
     */
    public static boolean isProxyClass(Class<?> cl) {
        return Proxy.class.isAssignableFrom(cl) && proxyClassCache.containsValue(cl);
    }

    /**
     * 返回指定代理实例的调用处理器。
     *
     * @param   proxy 要返回调用处理器的代理实例
     * @return  代理实例的调用处理器
     * @throws  IllegalArgumentException 如果参数不是代理实例
     * @throws  SecurityException 如果存在安全经理 <em>s</em>，且调用者的类加载器不是调用处理器类加载器的相同或祖先，并且调用 {@link SecurityManager#checkPackageAccess
     *          s.checkPackageAccess()} 拒绝访问调用处理器的类。
     */
    @CallerSensitive
    public static InvocationHandler getInvocationHandler(Object proxy)
        throws IllegalArgumentException
    {
        /*
         * 验证对象是否确实是代理实例。
         */
        if (!isProxyClass(proxy.getClass())) {
            throw new IllegalArgumentException("not a proxy instance");
        }

        final Proxy p = (Proxy) proxy;
        final InvocationHandler ih = p.h;
        if (System.getSecurityManager() != null) {
            Class<?> ihClass = ih.getClass();
            Class<?> caller = Reflection.getCallerClass();
            if (ReflectUtil.needsPackageAccessCheck(caller.getClassLoader(),
                                                    ihClass.getClassLoader()))
            {
                ReflectUtil.checkPackageAccess(ihClass);
            }
        }

        return ih;
    }

    private static native Class<?> defineClass0(ClassLoader loader, String name,
                                                byte[] b, int off, int len);
}
