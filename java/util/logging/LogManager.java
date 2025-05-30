
/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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


package java.util.logging;

import java.io.*;
import java.util.*;
import java.security.*;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.beans.PropertyChangeListener;
import sun.misc.JavaAWTAccess;
import sun.misc.SharedSecrets;

/**
 * 存在一个全局的 LogManager 对象，用于维护关于 Loggers 和日志服务的共享状态。
 * <p>
 * 这个 LogManager 对象：
 * <ul>
 * <li> 管理一个 Logger 对象的分层命名空间。所有命名的 Logger 都存储在这个命名空间中。
 * <li> 管理一组日志控制属性。这些是简单的键值对，可以由 Handlers 和其他日志对象用于配置自己。
 * </ul>
 * <p>
 * 可以通过 LogManager.getLogManager() 获取全局的 LogManager 对象。LogManager 对象在类初始化时创建，并且不能随后更改。
 * <p>
 * 在启动时，LogManager 类通过 java.util.logging.manager 系统属性定位。
 * <p>
 * LogManager 定义了两个可选的系统属性，允许控制初始配置：
 * <ul>
 * <li>"java.util.logging.config.class"
 * <li>"java.util.logging.config.file"
 * </ul>
 * 这两个属性可以在 "java" 命令的命令行上指定，或者作为传递给 JNI_CreateJavaVM 的系统属性定义。
 * <p>
 * 如果设置了 "java.util.logging.config.class" 属性，则该属性值将被视为类名。给定的类将被加载，创建一个对象，该对象的构造函数负责读取初始配置。（该对象可以使用其他系统属性来控制其配置。）备用配置类可以使用 <tt>readConfiguration(InputStream)</tt> 在 LogManager 中定义属性。
 * <p>
 * 如果未设置 "java.util.logging.config.class" 属性，则可以使用 "java.util.logging.config.file" 系统属性指定一个属性文件（格式为 java.util.Properties）。初始日志配置将从该文件中读取。
 * <p>
 * 如果未定义这两个属性，则 LogManager 使用其默认配置。默认配置通常从 Java 安装目录中的属性文件 "{@code lib/logging.properties}" 加载。
 * <p>
 * 日志记录器和 Handlers 的属性名称将以处理程序或日志记录器的点分隔名称开头。
 * <p>
 * 全局日志属性可能包括：
 * <ul>
 * <li>一个 "handlers" 属性。这定义了一个以空白或逗号分隔的类名列表，用于加载并注册为根 Logger（名为 "" 的 Logger）的处理程序类。每个类名必须是一个具有默认构造函数的 Handler 类。注意，这些处理程序可能在首次使用时懒加载。
 *
 * <li>一个 "&lt;logger&gt;.handlers" 属性。这定义了一个以空白或逗号分隔的类名列表，用于加载并注册为指定日志记录器的处理程序类。每个类名必须是一个具有默认构造函数的 Handler 类。注意，这些处理程序可能在首次使用时懒加载。
 *
 * <li>一个 "&lt;logger&gt;.useParentHandlers" 属性。这定义了一个布尔值。默认情况下，每个日志记录器都会调用其父级，除了处理日志消息本身之外，这通常会导致消息也被根日志记录器处理。当将此属性设置为 false 时，需要为此日志记录器配置一个处理程序，否则不会传递任何日志消息。
 *
 * <li>一个 "config" 属性。此属性旨在允许运行任意配置代码。该属性定义了一个以空白或逗号分隔的类名列表。将为每个命名的类创建一个新实例。每个类的默认构造函数可以执行任意代码以更新日志配置，例如设置日志记录器级别、添加处理程序、添加过滤器等。
 * </ul>
 * <p>
 * 注意，在 LogManager 配置期间加载的所有类首先在系统类路径上搜索，然后再在用户类路径上搜索。这包括 LogManager 类、任何配置类和任何处理程序类。
 * <p>
 * 日志记录器基于其点分隔名称组织成一个命名层次结构。因此 "a.b.c" 是 "a.b" 的子级，但 "a.b1" 和 "a.b2" 是同级。
 * <p>
 * 所有以 ".level" 结尾的属性都假定定义了 Logger 的日志级别。因此 "foo.level" 为名为 "foo" 的日志记录器及其在命名层次结构中的任何子级定义了一个日志级别。日志级别按照它们在属性文件中定义的顺序应用。因此，树中子节点的级别设置应出现在其父节点设置之后。
 * <p>
 * LogManager 对象上的所有方法都是多线程安全的。
 *
 * @since 1.4
*/

public class LogManager {
    // 全局的 LogManager 对象
    private static final LogManager manager;

    // 'props' 在锁内分配但在没有锁的情况下访问。
    // 声明它为 volatile 以确保另一个线程不会看到部分构造的 'props' 对象。
    // （看到部分构造的 'props' 对象可能会导致 Hashtable.get() 抛出 NPE，因为它允许在 Hashtable 构造函数实际完成之前调用 props.getProperties()）。
    private volatile Properties props = new Properties();
    private final static Level defaultLevel = Level.INFO;

    // 注册监听器的映射。映射值是注册计数，以允许在同一个监听器注册多次的情况下。
    private final Map<Object, Integer> listenerMap = new HashMap<>();

    // 系统日志记录器和用户日志记录器的 LoggerContext
    private final LoggerContext systemContext = new SystemLoggerContext();
    private final LoggerContext userContext = new LoggerContext();
    // 非 final 字段 - 使其为 volatile 以确保其他线程在 ensureLogManagerInitialized() 执行完毕后能看到新值。
    private volatile Logger rootLogger;
    // 是否已经读取了配置文件？
    // （必须在足够的 java.lang.System 初始化完成后进行）
    private volatile boolean readPrimordialConfiguration;
    // 是否已经初始化了全局（根）处理程序？
    // 这个值在 readConfiguration 中被设置为 false
    private boolean initializedGlobalHandlers = true;
    // 如果 JVM 即将关闭且退出钩子已被调用，则为 true。
    private boolean deathImminent;

    static {
        manager = AccessController.doPrivileged(new PrivilegedAction<LogManager>() {
            @Override
            public LogManager run() {
                LogManager mgr = null;
                String cname = null;
                try {
                    cname = System.getProperty("java.util.logging.manager");
                    if (cname != null) {
                        try {
                            Class<?> clz = ClassLoader.getSystemClassLoader()
                                    .loadClass(cname);
                            mgr = (LogManager) clz.newInstance();
                        } catch (ClassNotFoundException ex) {
                            Class<?> clz = Thread.currentThread()
                                    .getContextClassLoader().loadClass(cname);
                            mgr = (LogManager) clz.newInstance();
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Could not load Logmanager \"" + cname + "\"");
                    ex.printStackTrace();
                }
                if (mgr == null) {
                    mgr = new LogManager();
                }
                return mgr;

            }
        });
    }


    // 这个私有类用作关闭钩子。
    // 它执行 "reset" 以关闭所有打开的处理程序。
    private class Cleaner extends Thread {

        private Cleaner() {
            /* 将上下文类加载器设置为 null 以避免保持对应用程序类加载器的强引用。 */
            this.setContextClassLoader(null);
        }

        @Override
        public void run() {
            // 这是为了确保 LogManager.<clinit> 在同步块之前完成。
            // 否则可能会出现死锁。
            LogManager mgr = manager;

            // 如果全局处理程序尚未初始化，我们不希望只是为了关闭它们而初始化它们！
            synchronized (LogManager.this) {
                // 注意死亡迫在眉睫。
                deathImminent = true;
                initializedGlobalHandlers = true;
            }

            // 执行重置以关闭所有活动处理程序。
            reset();
        }
    }


    /**
     * 受保护的构造函数。此构造函数受保护，以便容器应用程序（如 J2EE 容器）可以子类化该对象。它是非公共的，因为预期只有一个 LogManager 对象，其值通过调用 LogManager.getLogManager 获取。
     */
    protected LogManager() {
        this(checkSubclassPermissions());
    }

    private LogManager(Void checked) {

        // 添加一个关闭钩子以关闭全局处理程序。
        try {
            Runtime.getRuntime().addShutdownHook(new Cleaner());
        } catch (IllegalStateException e) {
            // 如果 VM 已经正在关闭，
            // 我们不需要注册关闭钩子。
        }
    }

    private static Void checkSubclassPermissions() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            // 这些权限将在 LogManager 构造函数中检查，
            // 以注册 Cleaner() 线程作为关闭钩子。
            // 在这里检查它们以避免构造对象等的开销。
            sm.checkPermission(new RuntimePermission("shutdownHooks"));
            sm.checkPermission(new RuntimePermission("setContextClassLoader"));
        }
        return null;
    }

    /**
     * 懒初始化：如果此管理器实例是全局管理器，则此方法将读取初始配置并调用 addLogger() 添加根日志记录器和全局日志记录器。
     *
     * 注意，这与我们在 LoggerContext 中所做的略有不同。
     * 在 LoggerContext 中，我们修补日志记录器上下文树以将根日志记录器和全局日志记录器 *添加到上下文树*。
     *
     * 为了使这有效，必须已经调用过 LogManager 实例的 addLogger() 一次，以添加默认日志记录器。
     *
     * 这就是为什么在将任何日志记录器添加到任何日志记录器上下文之前，需要调用 ensureLogManagerInitialized()。
     *
     */
    private boolean initializedCalled = false;
    private volatile boolean initializationDone = false;
    final void ensureLogManagerInitialized() {
        final LogManager owner = this;
        if (initializationDone || owner != manager) {
            // 我们不希望执行两次，也不希望在私有管理器实例上执行。
            return;
        }

        // 可能另一个线程已经调用了 ensureLogManagerInitialized() 并且仍在执行。
        // 如果是这样，我们将阻塞，直到日志管理器完成初始化，然后获取监视器，
        // 注意 initializationDone 现在为 true 并返回。
        // 否则 - 我们是第一个到达这里的！我们将获取监视器，
        // 看到 initializationDone 仍然为 false，并执行初始化。
        //
        synchronized(this) {
            // 如果 initializedCalled 为 true，这意味着我们已经在当前线程中初始化 LogManager。
            // ensureLogManagerInitialized() 已经被递归调用。
            final boolean isRecursiveInitialization = (initializedCalled == true);

            assert initializedCalled || !initializationDone
                    : "如果未调用 initialized，则初始化不能完成！";

            if (isRecursiveInitialization || initializationDone) {
                // 如果 isRecursiveInitialization 为 true，这意味着我们已经在当前线程中初始化 LogManager。
                // ensureLogManagerInitialized() 已经被递归调用。我们不应继续，因为这会导致无限递归。
                //
                // 如果 initializationDone 为 true，则表示管理器已经完成初始化；直接返回：我们完成了。
                return;
            }
            // 下面调用 addLogger 将反过来调用 requiresDefaultLogger()
            // 这将调用 ensureLogManagerInitialized()。
            // 我们使用 initializedCalled 来中断递归。
            initializedCalled = true;
            try {
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    @Override
                    public Object run() {
                        assert rootLogger == null;
                        assert initializedCalled && !initializationDone;

                        // 读取配置。
                        owner.readPrimordialConfiguration();


                                    // 创建并保留命名空间根目录的 Logger。
                        owner.rootLogger = owner.new RootLogger();
                        owner.addLogger(owner.rootLogger);
                        if (!owner.rootLogger.isLevelInitialized()) {
                            owner.rootLogger.setLevel(defaultLevel);
                        }

                        // 添加全局 Logger。
                        // 不要在这里调用 Logger.getGlobal()，因为这可能会触发
                        // 细微的相互依赖问题。
                        @SuppressWarnings("deprecation")
                        final Logger global = Logger.global;

                        // 确保全局 logger 将被注册到全局管理器中
                        owner.addLogger(global);
                        return null;
                    }
                });
            } finally {
                initializationDone = true;
            }
        }
    }

    /**
     * 返回全局 LogManager 对象。
     * @return 全局 LogManager 对象
     */
    public static LogManager getLogManager() {
        if (manager != null) {
            manager.ensureLogManagerInitialized();
        }
        return manager;
    }

    private void readPrimordialConfiguration() {
        if (!readPrimordialConfiguration) {
            synchronized (this) {
                if (!readPrimordialConfiguration) {
                    // 如果 System.in/out/err 为 null，这是一个很好的
                    // 指示，说明我们仍然处于引导阶段
                    if (System.out == null) {
                        return;
                    }
                    readPrimordialConfiguration = true;

                    try {
                        AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                                @Override
                                public Void run() throws Exception {
                                    readConfiguration();

                                    // 平台 logger 开始委托给 java.util.logging.Logger
                                    sun.util.logging.PlatformLogger.redirectPlatformLoggers();
                                    return null;
                                }
                            });
                    } catch (Exception ex) {
                        assert false : "读取日志配置时引发异常: " + ex;
                    }
                }
            }
        }
    }

    /**
     * 添加一个事件监听器，当日志属性被重新读取时调用。如果多次添加相同的事件监听器，
     * 则会在属性事件监听器表中生成多个条目。
     *
     * <p><b>警告：</b>此方法在所有不包含 {@code java.beans} 包的 Java SE 子集配置文件中被省略。
     * </p>
     *
     * @param l 事件监听器
     * @exception  SecurityException 如果存在安全经理，并且调用者没有 LoggingPermission("control") 权限。
     * @exception NullPointerException 如果 PropertyChangeListener 为 null。
     * @deprecated 对 {@code PropertyChangeListener} 的依赖为 Java 平台的模块化带来了重大障碍。此方法将在未来的版本中移除。
     *             全局 {@code LogManager} 可以通过重写 {@link
     *             #readConfiguration readConfiguration} 方法来检测日志配置的更改。
     */
    @Deprecated
    public void addPropertyChangeListener(PropertyChangeListener l) throws SecurityException {
        PropertyChangeListener listener = Objects.requireNonNull(l);
        checkPermission();
        synchronized (listenerMap) {
            // 如果已注册，则增加注册计数
            Integer value = listenerMap.get(listener);
            value = (value == null) ? 1 : (value + 1);
            listenerMap.put(listener, value);
        }
    }

    /**
     * 移除属性更改事件的事件监听器。
     * 如果相同的监听器实例通过多次调用 <CODE>addPropertyChangeListener</CODE> 添加到监听器表中，
     * 则需要相同数量的 <CODE>removePropertyChangeListener</CODE> 调用来移除所有该监听器的实例。
     * <P>
     * 如果未找到给定的监听器，则静默返回。
     *
     * <p><b>警告：</b>此方法在所有不包含 {@code java.beans} 包的 Java SE 子集配置文件中被省略。
     * </p>
     *
     * @param l 事件监听器（可以为 null）
     * @exception  SecurityException 如果存在安全经理，并且调用者没有 LoggingPermission("control") 权限。
     * @deprecated 对 {@code PropertyChangeListener} 的依赖为 Java 平台的模块化带来了重大障碍。此方法将在未来的版本中移除。
     *             全局 {@code LogManager} 可以通过重写 {@link
     *             #readConfiguration readConfiguration} 方法来检测日志配置的更改。
     */
    @Deprecated
    public void removePropertyChangeListener(PropertyChangeListener l) throws SecurityException {
        checkPermission();
        if (l != null) {
            PropertyChangeListener listener = l;
            synchronized (listenerMap) {
                Integer value = listenerMap.get(listener);
                if (value != null) {
                    // 如果注册计数为 1，则从映射中移除，否则
                    // 只减少其计数
                    int i = value.intValue();
                    if (i == 1) {
                        listenerMap.remove(listener);
                    } else {
                        assert i > 1;
                        listenerMap.put(listener, i - 1);
                    }
                }
            }
        }
    }

    // LoggerContext 从 AppContext 映射
    private WeakHashMap<Object, LoggerContext> contextsMap = null;

    // 返回用户代码（即应用程序或 AppContext）的 LoggerContext。
    // Logger 与每个 AppContext 隔离。
    private LoggerContext getUserContext() {
        LoggerContext context = null;

        SecurityManager sm = System.getSecurityManager();
        JavaAWTAccess javaAwtAccess = SharedSecrets.getJavaAWTAccess();
        if (sm != null && javaAwtAccess != null) {
            // 每个 applet 都有自己的 LoggerContext，与其他 applet 隔离
            final Object ecx = javaAwtAccess.getAppletContext();
            if (ecx != null) {
                synchronized (javaAwtAccess) {
                    // 查找 applet 代码的 AppContext
                    // 如果在主应用上下文中，则为 null。
                    if (contextsMap == null) {
                        contextsMap = new WeakHashMap<>();
                    }
                    context = contextsMap.get(ecx);
                    if (context == null) {
                        // 为 applet 创建一个新的 LoggerContext。
                        context = new LoggerContext();
                        contextsMap.put(ecx, context);
                    }
                }
            }
        }
        // 对于独立应用程序，返回 userContext
        return context != null ? context : userContext;
    }

    // 系统上下文。
    final LoggerContext getSystemContext() {
        return systemContext;
    }

    private List<LoggerContext> contexts() {
        List<LoggerContext> cxs = new ArrayList<>();
        cxs.add(getSystemContext());
        cxs.add(getUserContext());
        return cxs;
    }

    // 查找或创建指定的 logger 实例。如果已经使用给定名称创建了 logger，则返回该 logger。
    // 否则，创建一个新的 logger 实例并注册到 LogManager 的全局命名空间中。
    // 此方法总是返回一个非 null 的 Logger 对象。
    // 这里不需要同步。添加新 Logger 对象的所有同步由 addLogger() 处理。
    //
    // 此方法必须委托给 LogManager 实现来添加新的 Logger 或返回之前添加的 Logger，
    // 因为 LogManager 子类可能重写了 addLogger, getLogger, readConfiguration 等方法。
    Logger demandLogger(String name, String resourceBundleName, Class<?> caller) {
        Logger result = getLogger(name);
        if (result == null) {
            // 只分配一次新的 logger
            Logger newLogger = new Logger(name, resourceBundleName, caller, this, false);
            do {
                if (addLogger(newLogger)) {
                    // 我们成功添加了上面创建的新 Logger，因此直接返回它，无需重新获取。
                    return newLogger;
                }

                // 我们没有添加上面创建的新 Logger，因为另一个线程在我们上面的 null 检查之后和调用 addLogger() 之前
                // 添加了一个具有相同名称的 Logger。我们必须重新获取 Logger，因为 addLogger() 返回一个布尔值
                // 而不是 Logger 引用本身。然而，如果创建其他 Logger 的线程没有持有对该 Logger 的强引用，
                // 则该 Logger 可能在我们通过 addLogger() 看到它之后和重新获取它之前被垃圾回收。如果已被垃圾回收，
                // 我们将循环并再次尝试。
                result = getLogger(name);
            } while (result == null);
        }
        return result;
    }

    Logger demandSystemLogger(String name, String resourceBundleName) {
        // 在系统上下文的命名空间中添加一个系统 logger
        final Logger sysLogger = getSystemContext().demandLogger(name, resourceBundleName);

        // 如果不存在，则将系统 logger 添加到 LogManager 的命名空间中
        // 以确保只有一个给定名称的 logger。
        // 系统 logger 对应用程序可见，除非已添加同名的 logger。
        Logger logger;
        do {
            // 首先尝试调用 addLogger 而不是 getLogger
            // 这可以避免自定义 LogManager.getLogger 实现中的潜在错误，即如果不存在则添加 logger
            if (addLogger(sysLogger)) {
                // 成功添加了新的系统 logger
                logger = sysLogger;
            } else {
                logger = getLogger(name);
            }
        } while (logger == null);

        // LogManager 通过 LogManager.addLogger 方法设置 sysLogger 的处理器。
        if (logger != sysLogger && sysLogger.accessCheckedHandlers().length == 0) {
            // 如果 logger 已存在但未设置处理器
            final Logger l = logger;
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    for (Handler hdl : l.accessCheckedHandlers()) {
                        sysLogger.addHandler(hdl);
                    }
                    return null;
                }
            });
        }
        return sysLogger;
    }

    // LoggerContext 维护每个上下文的 logger 命名空间。
    // 默认的 LogManager 实现有一个系统上下文和用户上下文。系统上下文用于维护所有系统 logger 的命名空间，
    // 并由系统代码查询。如果系统 logger 在用户上下文中不存在，则也会添加到用户上下文中。
    // 用户上下文由用户代码查询，所有其他 logger 都添加到用户上下文中。
    class LoggerContext {
        // 命名 logger 表，将名称映射到 logger。
        private final Hashtable<String, LoggerWeakRef> namedLoggers = new Hashtable<>();
        // 命名 logger 树
        private final LogNode root;
        private LoggerContext() {
            this.root = new LogNode(null, this);
        }


        // 告诉此上下文是否需要默认 logger。
        // 如果为 true，则默认 logger 将被延迟添加。
        final boolean requiresDefaultLoggers() {
            final boolean requiresDefaultLoggers = (getOwner() == manager);
            if (requiresDefaultLoggers) {
                getOwner().ensureLogManagerInitialized();
            }
            return requiresDefaultLoggers;
        }

        // 此上下文的 LogManager。
        final LogManager getOwner() {
            return LogManager.this;
        }

        // 此上下文所有者的根 logger，如果非 null，并且
        // 上下文需要默认 logger，则将其添加到上下文 logger 的树中。
        final Logger getRootLogger() {
            return getOwner().rootLogger;
        }

        // 全局 logger，如果非 null，并且
        // 上下文需要默认 logger，则将其添加到上下文 logger 的树中。
        final Logger getGlobalLogger() {
            @SuppressWarnings("deprecated") // 避免初始化循环。
            final Logger global = Logger.global;
            return global;
        }

        Logger demandLogger(String name, String resourceBundleName) {
            // LogManager 子类可能有自己的实现来添加和
            // 获取 logger。因此委托给 LogManager 来完成工作。
            final LogManager owner = getOwner();
            return owner.demandLogger(name, resourceBundleName, null);
        }


        // 由于微妙的死锁问题，getUserContext() 不再
        // 调用 addLocalLogger(rootLogger);
        // 因此，我们需要稍后添加默认 logger。
        // 检查上下文是否已正确初始化
        // 在调用例如 find(name) 或 getLoggerNames() 之前这是必要的。
        //
        private void ensureInitialized() {
            if (requiresDefaultLoggers()) {
                // 确保根 logger 和全局 logger 已设置。
                ensureDefaultLogger(getRootLogger());
                ensureDefaultLogger(getGlobalLogger());
            }
        }


        synchronized Logger findLogger(String name) {
            // 确保在查找 logger 之前
            // 此上下文已正确初始化。
            ensureInitialized();
            LoggerWeakRef ref = namedLoggers.get(name);
            if (ref == null) {
                return null;
            }
            Logger logger = ref.get();
            if (logger == null) {
                // Hashtable 持有已垃圾回收的 logger 的陈旧弱引用。
                ref.dispose();
            }
            return logger;
        }


        // 在将记录器添加到上下文之前调用此方法。
        // 'logger' 是将要添加的上下文。
        // 此方法将确保在添加 'logger' 之前添加默认记录器。
        //
        private void ensureAllDefaultLoggers(Logger logger) {
            if (requiresDefaultLoggers()) {
                final String name = logger.getName();
                if (!name.isEmpty()) {
                    ensureDefaultLogger(getRootLogger());
                    if (!Logger.GLOBAL_LOGGER_NAME.equals(name)) {
                        ensureDefaultLogger(getGlobalLogger());
                    }
                }
            }
        }

        private void ensureDefaultLogger(Logger logger) {
            // 用于延迟添加根记录器和全局记录器
            // 到 LoggerContext。

            // 此检查是简单的合理性检查：我们不希望此
            // 方法被调用除 Logger.global
            // 或 owner.rootLogger 之外的任何其他对象。
            if (!requiresDefaultLoggers() || logger == null
                    || logger != Logger.global && logger != LogManager.this.rootLogger) {

                // 如果我们有一个非空记录器，但它既不是
                // Logger.global 也不是 manager.rootLogger，这表明一个严重
                // 的问题 - 因为 ensureDefaultLogger 不应被调用
                // 除这两个记录器之一（或 null - 如果
                // 例如 manager.rootLogger 尚未初始化）...
                assert logger == null;

                return;
            }

            // 如果记录器尚未存在，则添加它。
            if (!namedLoggers.containsKey(logger.getName())) {
                // 在添加默认记录器的过程中，防止 addLocalLogger
                // 调用 ensureAllDefaultLoggers - 因为这会
                // 立即导致堆栈溢出。
                // 因此，即使 requiresDefaultLoggers 为 true，我们也必须传递 addDefaultLoggersIfNeeded=false。
                addLocalLogger(logger, false);
            }
        }

        boolean addLocalLogger(Logger logger) {
            // 如果不需要，则无需添加默认记录器
            return addLocalLogger(logger, requiresDefaultLoggers());
        }

        // 将记录器添加到此上下文。此方法仅设置其级别
        // 并处理父记录器。它不会设置其处理器。
        synchronized boolean addLocalLogger(Logger logger, boolean addDefaultLoggersIfNeeded) {
            // addDefaultLoggersIfNeeded 用于在添加
            // 默认记录器时中断递归。如果我们正在添加一个默认记录器
            // （我们被 ensureDefaultLogger() 调用），则
            // addDefaultLoggersIfNeeded 将为 false：我们不希望再次调用
            // ensureAllDefaultLoggers。
            //
            // 注意：当 requiresDefaultLoggers 为 false 时，addDefaultLoggersIfNeeded 也可以为 false - 因为在这种情况下调用
            // ensureAllDefaultLoggers 将没有效果。
            if (addDefaultLoggersIfNeeded) {
                ensureAllDefaultLoggers(logger);
            }

            final String name = logger.getName();
            if (name == null) {
                throw new NullPointerException();
            }
            LoggerWeakRef ref = namedLoggers.get(name);
            if (ref != null) {
                if (ref.get() == null) {
                    // 可能记录器在上面的 drainLoggerRefQueueBounded() 调用后被 GC，因此允许
                    // 注册一个新的记录器。
                    ref.dispose();
                } else {
                    // 我们已经有一个具有给定名称的已注册记录器。
                    return false;
                }
            }

            // 我们正在添加一个新的记录器。
            // 注意，我们在这里创建了一个弱引用。
            final LogManager owner = getOwner();
            logger.setLogManager(owner);
            ref = owner.new LoggerWeakRef(logger);
            namedLoggers.put(name, ref);

            // 除非记录器的级别已初始化，否则应用新记录器的任何初始级别。
            Level level = owner.getLevelProperty(name + ".level", null);
            if (level != null && !logger.isLevelInitialized()) {
                doSetLevel(logger, level);
            }

            // 处理器的实例化在 LogManager.addLogger
            // 实现中完成，因为处理器类可能仅对 LogManager
            // 子类可见，以处理自定义日志管理器的情况。
            processParentHandlers(logger, name);

            // 查找新节点及其父节点。
            LogNode node = getNode(name);
            node.loggerRef = ref;
            Logger parent = null;
            LogNode nodep = node.parent;
            while (nodep != null) {
                LoggerWeakRef nodeRef = nodep.loggerRef;
                if (nodeRef != null) {
                    parent = nodeRef.get();
                    if (parent != null) {
                        break;
                    }
                }
                nodep = nodep.parent;
            }

            if (parent != null) {
                doSetParent(logger, parent);
            }
            // 遍历子节点并告诉它们我们是它们的新父节点。
            node.walkAndSetParent(logger);
            // 新 LogNode 已准备好，因此告诉 LoggerWeakRef 关于它
            ref.setNode(node);
            return true;
        }

        synchronized void removeLoggerRef(String name, LoggerWeakRef ref) {
            namedLoggers.remove(name, ref);
        }

        synchronized Enumeration<String> getLoggerNames() {
            // 确保在返回记录器名称之前
            // 此上下文已正确初始化。
            ensureInitialized();
            return namedLoggers.keys();
        }

        // 如果 logger.getUseParentHandlers() 返回 'true' 并且记录器的
        // 父节点中有级别或处理器定义，确保它们被实例化。
        private void processParentHandlers(final Logger logger, final String name) {
            final LogManager owner = getOwner();
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    if (logger != owner.rootLogger) {
                        boolean useParent = owner.getBooleanProperty(name + ".useParentHandlers", true);
                        if (!useParent) {
                            logger.setUseParentHandlers(false);
                        }
                    }
                    return null;
                }
            });

            int ix = 1;
            for (;;) {
                int ix2 = name.indexOf(".", ix);
                if (ix2 < 0) {
                    break;
                }
                String pname = name.substring(0, ix2);
                if (owner.getProperty(pname + ".level") != null ||
                    owner.getProperty(pname + ".handlers") != null) {
                    // 此 pname 有级别/处理器定义。
                    // 确保它存在。
                    demandLogger(pname, null);
                }
                ix = ix2+1;
            }
        }

        // 获取我们记录器节点树中的一个节点。
        // 如果需要，创建它。
        LogNode getNode(String name) {
            if (name == null || name.equals("")) {
                return root;
            }
            LogNode node = root;
            while (name.length() > 0) {
                int ix = name.indexOf(".");
                String head;
                if (ix > 0) {
                    head = name.substring(0, ix);
                    name = name.substring(ix + 1);
                } else {
                    head = name;
                    name = "";
                }
                if (node.children == null) {
                    node.children = new HashMap<>();
                }
                LogNode child = node.children.get(head);
                if (child == null) {
                    child = new LogNode(node, this);
                    node.children.put(head, child);
                }
                node = child;
            }
            return node;
        }
    }

    final class SystemLoggerContext extends LoggerContext {
        // 在系统上下文的命名空间以及
        // LogManager 的命名空间中添加一个系统记录器，如果不存在的话，以确保只有一个
        // 给定名称的记录器。系统记录器对应用程序可见，除非已添加同名的记录器。
        @Override
        Logger demandLogger(String name, String resourceBundleName) {
            Logger result = findLogger(name);
            if (result == null) {
                // 仅分配一次新的系统记录器
                Logger newLogger = new Logger(name, resourceBundleName, null, getOwner(), true);
                do {
                    if (addLocalLogger(newLogger)) {
                        // 我们成功添加了上面创建的新记录器，因此返回它而无需重新获取。
                        result = newLogger;
                    } else {
                        // 我们没有添加上面创建的新记录器
                        // 因为在我们上面的 null 检查之后和调用
                        // addLogger() 之前，另一个线程添加了同名的记录器。我们必须重新获取记录器，因为
                        // addLogger() 返回一个布尔值而不是记录器引用本身。然而，如果创建其他记录器的线程
                        // 没有持有对其他记录器的强引用，则其他记录器可能在我们看到它后但在我们重新获取它之前被 GC。
                        // 如果它已被 GC，则我们将循环并再次尝试。
                        result = findLogger(name);
                    }
                } while (result == null);
            }
            return result;
        }
    }

    // 添加新的每记录器处理器。
    // 我们需要在这里提升权限。所有决策都将
    // 基于日志配置，这只能由受信任的代码修改。
    private void loadLoggerHandlers(final Logger logger, final String name,
                                    final String handlersPropertyName)
    {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                String names[] = parseClassNames(handlersPropertyName);
                for (int i = 0; i < names.length; i++) {
                    String word = names[i];
                    try {
                        Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(word);
                        Handler hdl = (Handler) clz.newInstance();
                        // 检查是否有属性定义
                        // 此处理器的级别。
                        String levs = getProperty(word + ".level");
                        if (levs != null) {
                            Level l = Level.findLevel(levs);
                            if (l != null) {
                                hdl.setLevel(l);
                            } else {
                                // 可能是一个错误的级别。跳过。
                                System.err.println("Can't set level for " + word);
                            }
                        }
                        // 将此处理器添加到记录器
                        logger.addHandler(hdl);
                    } catch (Exception ex) {
                        System.err.println("Can't load log handler \"" + word + "\"");
                        System.err.println("" + ex);
                        ex.printStackTrace();
                    }
                }
                return null;
            }
        });
    }


    // loggerRefQueue 持有 LoggerWeakRef 对象，这些对象引用的 Logger 对象
    // 已被 GC。
    private final ReferenceQueue<Logger> loggerRefQueue
        = new ReferenceQueue<>();

    // 包级内部类。
    // 用于管理 Logger 对象的弱引用的辅助类。
    //
    // LogManager.namedLoggers
    //     - 持有所有命名记录器的弱引用
    //     - namedLoggers 保留命名记录器的 LoggerWeakRef 对象，直到我们可以处理
    //       被 GC 的命名记录器的账务。
    // LogManager.LogNode.loggerRef
    //     - 持有命名记录器的弱引用
    //     - LogNode 也将保留命名记录器的 LoggerWeakRef 对象；目前 LogNodes 永远不会消失。
    // Logger.kids
    //     - 持有每个直接子记录器的弱引用；这包括匿名和命名记录器
    //     - 匿名记录器总是根记录器的子记录器，根记录器是一个强引用；rootLogger.kids 保留
    //       匿名记录器的 LoggerWeakRef 对象，直到我们可以处理账务。
    //
    final class LoggerWeakRef extends WeakReference<Logger> {
        private String                name;       // 用于 namedLoggers 清理
        private LogNode               node;       // 用于 loggerRef 清理
        private WeakReference<Logger> parentRef;  // 用于 kids 清理
        private boolean disposed = false;         // 避免两次调用 dispose

        LoggerWeakRef(Logger logger) {
            super(logger, loggerRefQueue);

            name = logger.getName();  // 保存用于 namedLoggers 清理
        }

        // 处理此 LoggerWeakRef 对象
        void dispose() {
            // 避免两次调用 dispose。当记录器被 GC 时，其
            // LoggerWeakRef 将被入队。
            // 然而，在队列被清空之前，可能会添加（或查找）同名的新记录器。当这种情况发生时，dispose()
            // 将由 addLocalLogger() 或 findLogger() 调用。
            // 后来当队列被清空时，将再次为同一个 LoggerWeakRef 调用 dispose()。标记 LoggerWeakRef 为已处理
            // 避免两次处理数据（即使代码现在应该是可重入的）。
            synchronized(this) {
                // 注意：维护者
                // 在此块内不要调用任何尝试获取
                // 另一个锁的方法 - 因为这肯定会导致死锁，鉴于 dispose() 可以被
                // 多个线程调用，并且从不同的同步方法/块中调用。
                if (disposed) return;
                disposed = true;
            }


                        final LogNode n = node;
            if (n != null) {
                // n.loggerRef 仅能从 LoggerContext 锁内安全修改。removeLoggerRef 已经
                // 在 LoggerContext 上同步，因此在此锁内调用
                // n.context.removeLoggerRef 是安全的。
                synchronized (n.context) {
                    // 如果我们有一个 LogNode，那么我们就是一个命名的 Logger
                    // 因此清除 namedLoggers 对我们的弱引用
                    n.context.removeLoggerRef(name, this);
                    name = null;  // 清除我们对 Logger 名称的引用

                    // LogNode 可能已被重用 - 因此仅在 LogNode.loggerRef == this 时清除
                    // LogNode.loggerRef
                    if (n.loggerRef == this) {
                        n.loggerRef = null;  // 清除 LogNode 对我们的弱引用
                    }
                    node = null;            // 清除我们对 LogNode 的引用
                }
            }

            if (parentRef != null) {
                // 此 LoggerWeakRef 有或曾有父 Logger
                Logger parent = parentRef.get();
                if (parent != null) {
                    // 父 Logger 仍然存在，因此清除
                    // 父 Logger 对我们的弱引用
                    parent.removeChildLogger(this);
                }
                parentRef = null;  // 清除我们对父 Logger 的弱引用
            }
        }

        // 将 node 字段设置为指定的值
        void setNode(LogNode node) {
            this.node = node;
        }

        // 将 parentRef 字段设置为指定的值
        void setParentRef(WeakReference<Logger> parentRef) {
            this.parentRef = parentRef;
        }
    }

    // 包级方法。
    // 排干一些已被垃圾回收的 Logger 对象。
    //
    // drainLoggerRefQueueBounded() 由下面的 addLogger() 调用
    // 和 Logger.getAnonymousLogger(String)，因此我们每次添加一个 Logger 时都会排干最多
    // MAX_ITERATIONS 个已垃圾回收的 Logger。
    //
    // 在 WinXP VMware 客户端上，MAX_ITERATIONS 值为 400 时
    // 在 AnonLoggerWeakRefLeak 测试中，我们大约有 50/50 的混合增加弱引用计数与
    // 减少弱引用计数。以下是清理 400 个匿名 Logger 的统计信息：
    //   - 测试持续时间 1 分钟
    //   - 样本大小 125 组 400 个
    //   - 平均值：1.99 毫秒
    //   - 最小值：0.57 毫秒
    //   - 最大值：25.3 毫秒
    //
    // 相同的配置在 LoggerWeakRefLeak 测试中给我们提供了更好的减少弱引用计数
    // 而不是增加弱引用计数。以下是清理 400 个命名 Logger 的统计信息：
    //   - 测试持续时间 2 分钟
    //   - 样本大小 506 组 400 个
    //   - 平均值：0.57 毫秒
    //   - 最小值：0.02 毫秒
    //   - 最大值：10.9 毫秒
    //
    private final static int MAX_ITERATIONS = 400;
    final void drainLoggerRefQueueBounded() {
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            if (loggerRefQueue == null) {
                // 尚未完成加载 LogManager
                break;
            }

            LoggerWeakRef ref = (LoggerWeakRef) loggerRefQueue.poll();
            if (ref == null) {
                break;
            }
            // 一个 Logger 对象已被垃圾回收，因此清理它
            ref.dispose();
        }
    }

    /**
     * 添加一个命名的 Logger。如果已注册同名的 Logger，则此方法不执行任何操作并返回 false。
     * <p>
     * Logger 工厂方法调用此方法来注册每个新创建的 Logger。
     * <p>
     * 应用程序应保留对 Logger 对象的引用，以避免其被垃圾回收。LogManager
     * 可能仅保留弱引用。
     *
     * @param   logger 新的 Logger。
     * @return  如果参数 Logger 注册成功，则返回 true；
     *          如果同名的 Logger 已存在，则返回 false。
     * @exception NullPointerException 如果 Logger 名称为空。
     */
    public boolean addLogger(Logger logger) {
        final String name = logger.getName();
        if (name == null) {
            throw new NullPointerException();
        }
        drainLoggerRefQueueBounded();
        LoggerContext cx = getUserContext();
        if (cx.addLocalLogger(logger)) {
            // 我们有每个 Logger 的处理程序吗？
            // 注意：这将增加 200 毫秒的开销
            loadLoggerHandlers(logger, name, name + ".handlers");
            return true;
        } else {
            return false;
        }
    }

    // 私有方法，用于在 Logger 上设置级别。
    // 如果需要，我们在调用 setLevel 之前提升权限。
    private static void doSetLevel(final Logger logger, final Level level) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            // 没有安全经理，所以事情很简单。
            logger.setLevel(level);
            return;
        }
        // 有一个安全经理。在调用 setLevel 之前提升权限。
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                logger.setLevel(level);
                return null;
            }});
    }

    // 私有方法，用于在 Logger 上设置父 Logger。
    // 如果需要，我们在调用 setParent 之前提升权限。
    private static void doSetParent(final Logger logger, final Logger parent) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            // 没有安全经理，所以事情很简单。
            logger.setParent(parent);
            return;
        }
        // 有一个安全经理。在调用 setParent 之前提升权限。
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                logger.setParent(parent);
                return null;
            }});
    }

    /**
     * 查找命名的 Logger。
     * <p>
     * 注意，由于不受信任的代码可以创建具有任意名称的 Logger，因此不应依赖此方法
     * 来查找安全敏感日志记录的 Logger。
     * 还需要注意的是，与 String {@code name} 关联的 Logger 可能随时被垃圾回收，如果
     * 没有对 Logger 的强引用。此方法的调用者必须检查返回值是否为 null，以正确处理
     * Logger 已被垃圾回收的情况。
     * <p>
     * @param name Logger 的名称
     * @return 匹配的 Logger 或如果没有找到则返回 null
     */
    public Logger getLogger(String name) {
        return getUserContext().findLogger(name);
    }

    /**
     * 获取已知 Logger 名称的枚举。
     * <p>
     * 注意：随着新类的加载，Logger 可能会动态添加。
     * 此方法仅报告当前注册的 Logger。还应注意，此方法仅返回 Logger 的名称，
     * 而不是对 Logger 本身的强引用。
     * 返回的 String 不会阻止 Logger 被垃圾回收。特别是，如果返回的名称传递给
     * {@code LogManager.getLogger()}，则调用者必须检查 {@code LogManager.getLogger()} 的返回值
     * 是否为 null，以正确处理 Logger 在其名称返回后被垃圾回收的情况。
     * <p>
     * @return Logger 名称字符串的枚举
     */
    public Enumeration<String> getLoggerNames() {
        return getUserContext().getLoggerNames();
    }

    /**
     * 重新初始化日志属性并重新读取日志配置。
     * <p>
     * 用于定位配置属性的规则与启动时使用的规则相同。因此，通常日志属性将
     * 从启动时使用的同一文件中重新读取。
     * <P>
     * 新配置文件中的任何日志级别定义都将使用 Logger.setLevel() 应用，如果目标 Logger 存在。
     * <p>
     * 读取属性后将触发 PropertyChangeEvent。
     *
     * @exception  SecurityException  如果存在安全经理并且调用者没有 LoggingPermission("control")。
     * @exception  IOException 如果读取配置时遇到 IO 问题。
     */
    public void readConfiguration() throws IOException, SecurityException {
        checkPermission();

        // 如果指定了配置类，加载并使用它。
        String cname = System.getProperty("java.util.logging.config.class");
        if (cname != null) {
            try {
                // 实例化命名的类。其构造函数负责通过调用 readConfiguration(InputStream)
                // 并使用合适的流来初始化日志配置。
                try {
                    Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(cname);
                    clz.newInstance();
                    return;
                } catch (ClassNotFoundException ex) {
                    Class<?> clz = Thread.currentThread().getContextClassLoader().loadClass(cname);
                    clz.newInstance();
                    return;
                }
            } catch (Exception ex) {
                System.err.println("日志配置类 \"" + cname + "\" 加载失败");
                System.err.println("" + ex);
                // 继续使用有用的配置文件。
            }
        }

        String fname = System.getProperty("java.util.logging.config.file");
        if (fname == null) {
            fname = System.getProperty("java.home");
            if (fname == null) {
                throw new Error("找不到 java.home ??");
            }
            File f = new File(fname, "lib");
            f = new File(f, "logging.properties");
            fname = f.getCanonicalPath();
        }
        try (final InputStream in = new FileInputStream(fname)) {
            final BufferedInputStream bin = new BufferedInputStream(in);
            readConfiguration(bin);
        }
    }

    /**
     * 重置日志配置。
     * <p>
     * 对于所有命名的 Logger，重置操作将移除并关闭所有处理程序，并（除了根 Logger）将级别
     * 设置为 null。根 Logger 的级别设置为 Level.INFO。
     *
     * @exception  SecurityException  如果存在安全经理并且调用者没有 LoggingPermission("control")。
     */

    public void reset() throws SecurityException {
        checkPermission();
        synchronized (this) {
            props = new Properties();
            // 由于我们正在进行重置，因此不再希望初始化全局处理程序，如果它们尚未初始化。
            initializedGlobalHandlers = true;
        }
        for (LoggerContext cx : contexts()) {
            Enumeration<String> enum_ = cx.getLoggerNames();
            while (enum_.hasMoreElements()) {
                String name = enum_.nextElement();
                Logger logger = cx.findLogger(name);
                if (logger != null) {
                    resetLogger(logger);
                }
            }
        }
    }

    // 私有方法，用于重置单个目标 Logger。
    private void resetLogger(Logger logger) {
        // 关闭 Logger 的所有处理程序。
        Handler[] targets = logger.getHandlers();
        for (int i = 0; i < targets.length; i++) {
            Handler h = targets[i];
            logger.removeHandler(h);
            try {
                h.close();
            } catch (Exception ex) {
                // 关闭处理程序时出现问题？继续...
            }
        }
        String name = logger.getName();
        if (name != null && name.equals("")) {
            // 这是根 Logger。
            logger.setLevel(defaultLevel);
        } else {
            logger.setLevel(null);
        }
    }

    // 从属性中获取由空格分隔的类名列表。
    private String[] parseClassNames(String propertyName) {
        String hands = getProperty(propertyName);
        if (hands == null) {
            return new String[0];
        }
        hands = hands.trim();
        int ix = 0;
        final List<String> result = new ArrayList<>();
        while (ix < hands.length()) {
            int end = ix;
            while (end < hands.length()) {
                if (Character.isWhitespace(hands.charAt(end))) {
                    break;
                }
                if (hands.charAt(end) == ',') {
                    break;
                }
                end++;
            }
            String word = hands.substring(ix, end);
            ix = end + 1;
            word = word.trim();
            if (word.length() == 0) {
                continue;
            }
            result.add(word);
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * 重新初始化日志属性并从给定的流中重新读取日志配置，该流应为 java.util.Properties 格式。
     * 读取属性后将触发 PropertyChangeEvent。
     * <p>
     * 新配置文件中的任何日志级别定义都将使用 Logger.setLevel() 应用，如果目标 Logger 存在。
     *
     * @param ins       从中读取属性的流
     * @exception  SecurityException  如果存在安全经理并且调用者没有 LoggingPermission("control")。
     * @exception  IOException 如果从流中读取时遇到问题。
     */
    public void readConfiguration(InputStream ins) throws IOException, SecurityException {
        checkPermission();
        reset();

        // 加载属性
        props.load(ins);
        // 实例化新的配置对象。
        String names[] = parseClassNames("config");

        for (int i = 0; i < names.length; i++) {
            String word = names[i];
            try {
                Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(word);
                clz.newInstance();
            } catch (Exception ex) {
                System.err.println("无法加载配置类 \"" + word + "\"");
                System.err.println("" + ex);
                // ex.printStackTrace();
            }
        }

        // 基于新属性设置任何现有 Logger 的级别。
        setLevelsOnExistingLoggers();

        // 通知任何感兴趣的方我们的属性已更改。
        // 我们首先复制监听器映射，以便在调用监听器时不持有任何锁。
        Map<Object, Integer> listeners = null;
        synchronized (listenerMap) {
            if (!listenerMap.isEmpty())
                listeners = new HashMap<>(listenerMap);
        }
        if (listeners != null) {
            assert Beans.isBeansPresent();
            Object ev = Beans.newPropertyChangeEvent(LogManager.class, null, null, null);
            for (Map.Entry<Object, Integer> entry : listeners.entrySet()) {
                Object listener = entry.getKey();
                int count = entry.getValue().intValue();
                for (int i = 0; i < count; i++) {
                    Beans.invokePropertyChange(listener, ev);
                }
            }
        }


        // 注意，当首次引用时，需要重新初始化全局句柄。
        synchronized (this) {
            initializedGlobalHandlers = false;
        }
    }

    /**
     * 获取日志属性的值。
     * 如果属性未找到，该方法返回 null。
     * @param name      属性名称
     * @return          属性值
     */
    public String getProperty(String name) {
        return props.getProperty(name);
    }

    // 包私有方法，用于获取字符串属性。
    // 如果属性未定义，我们返回给定的默认值。
    String getStringProperty(String name, String defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        return val.trim();
    }

    // 包私有方法，用于获取整数属性。
    // 如果属性未定义或无法解析，我们返回给定的默认值。
    int getIntProperty(String name, int defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    // 包私有方法，用于获取布尔属性。
    // 如果属性未定义或无法解析，我们返回给定的默认值。
    boolean getBooleanProperty(String name, boolean defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        val = val.toLowerCase();
        if (val.equals("true") || val.equals("1")) {
            return true;
        } else if (val.equals("false") || val.equals("0")) {
            return false;
        }
        return defaultValue;
    }

    // 包私有方法，用于获取级别属性。
    // 如果属性未定义或无法解析，我们返回给定的默认值。
    Level getLevelProperty(String name, Level defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        Level l = Level.findLevel(val.trim());
        return l != null ? l : defaultValue;
    }

    // 包私有方法，用于获取过滤器属性。
    // 我们返回由 "name" 属性命名的类的实例。如果属性未定义或有问题，我们返回 defaultValue。
    Filter getFilterProperty(String name, Filter defaultValue) {
        String val = getProperty(name);
        try {
            if (val != null) {
                Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(val);
                return (Filter) clz.newInstance();
            }
        } catch (Exception ex) {
            // 在创建类或实例时，我们遇到了各种异常之一。
            // 继续执行。
        }
        // 我们遇到了异常。返回 defaultValue。
        return defaultValue;
    }


    // 包私有方法，用于获取格式化器属性。
    // 我们返回由 "name" 属性命名的类的实例。如果属性未定义或有问题，我们返回 defaultValue。
    Formatter getFormatterProperty(String name, Formatter defaultValue) {
        String val = getProperty(name);
        try {
            if (val != null) {
                Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(val);
                return (Formatter) clz.newInstance();
            }
        } catch (Exception ex) {
            // 在创建类或实例时，我们遇到了各种异常之一。
            // 继续执行。
        }
        // 我们遇到了异常。返回 defaultValue。
        return defaultValue;
    }

    // 私有方法，用于加载全局句柄。
    // 当全局句柄首次使用时，我们懒惰地执行实际工作。
    private synchronized void initializeGlobalHandlers() {
        if (initializedGlobalHandlers) {
            return;
        }

        initializedGlobalHandlers = true;

        if (deathImminent) {
            // 哎呀...
            // 虚拟机正在关闭，我们的退出钩子已被调用。
            // 避免分配全局句柄。
            return;
        }
        loadLoggerHandlers(rootLogger, null, "handlers");
    }

    private final Permission controlPermission = new LoggingPermission("control", null);

    void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(controlPermission);
    }

    /**
     * 检查当前上下文是否被信任以修改日志配置。这需要 LoggingPermission("control")。
     * <p>
     * 如果检查失败，我们抛出 SecurityException，否则正常返回。
     *
     * @exception  SecurityException  如果存在安全管理器且调用者没有 LoggingPermission("control")。
     */
    public void checkAccess() throws SecurityException {
        checkPermission();
    }

    // 嵌套类，用于表示命名日志器树中的节点。
    private static class LogNode {
        HashMap<String,LogNode> children;
        LoggerWeakRef loggerRef;
        LogNode parent;
        final LoggerContext context;

        LogNode(LogNode parent, LoggerContext context) {
            this.parent = parent;
            this.context = context;
        }

        // 递归方法，用于遍历节点下的树并设置新的父日志器。
        void walkAndSetParent(Logger parent) {
            if (children == null) {
                return;
            }
            Iterator<LogNode> values = children.values().iterator();
            while (values.hasNext()) {
                LogNode node = values.next();
                LoggerWeakRef ref = node.loggerRef;
                Logger logger = (ref == null) ? null : ref.get();
                if (logger == null) {
                    node.walkAndSetParent(parent);
                } else {
                    doSetParent(logger, parent);
                }
            }
        }
    }

    // 我们使用 Logger 的子类作为根日志器，以便仅在首次需要时实例化全局句柄。
    private final class RootLogger extends Logger {
        private RootLogger() {
            // 我们在这里不调用受保护的 Logger 两参数构造函数，
            // 以避免在 RootLogger 构造函数中调用 LogManager.getLogManager()。
            super("", null, null, LogManager.this, true);
        }

        @Override
        public void log(LogRecord record) {
            // 确保全局句柄已被实例化。
            initializeGlobalHandlers();
            super.log(record);
        }

        @Override
        public void addHandler(Handler h) {
            initializeGlobalHandlers();
            super.addHandler(h);
        }

        @Override
        public void removeHandler(Handler h) {
            initializeGlobalHandlers();
            super.removeHandler(h);
        }

        @Override
        Handler[] accessCheckedHandlers() {
            initializeGlobalHandlers();
            return super.accessCheckedHandlers();
        }
    }


    // 私有方法，当配置更改时调用，以将任何级别设置应用到已存在的日志器。
    synchronized private void setLevelsOnExistingLoggers() {
        Enumeration<?> enum_ = props.propertyNames();
        while (enum_.hasMoreElements()) {
            String key = (String)enum_.nextElement();
            if (!key.endsWith(".level")) {
                // 不是级别定义。
                continue;
            }
            int ix = key.length() - 6;
            String name = key.substring(0, ix);
            Level level = getLevelProperty(key, null);
            if (level == null) {
                System.err.println("属性的级别值无效: " + key);
                continue;
            }
            for (LoggerContext cx : contexts()) {
                Logger l = cx.findLogger(name);
                if (l == null) {
                    continue;
                }
                l.setLevel(level);
            }
        }
    }

    // 管理支持
    private static LoggingMXBean loggingMXBean = null;
    /**
     * 日志设施管理接口的
     * {@link javax.management.ObjectName} 的字符串表示。
     *
     * @see java.lang.management.PlatformLoggingMXBean
     * @see java.util.logging.LoggingMXBean
     *
     * @since 1.5
     */
    public final static String LOGGING_MXBEAN_NAME
        = "java.util.logging:type=Logging";

    /**
     * 返回用于管理日志器的 <tt>LoggingMXBean</tt>。
     * 管理日志器的另一种方法是通过
     * {@link java.lang.management.PlatformLoggingMXBean} 接口，
     * 可以通过调用以下代码获取：
     * <pre>
     *     PlatformLoggingMXBean logging = {@link java.lang.management.ManagementFactory#getPlatformMXBean(Class)
     *         ManagementFactory.getPlatformMXBean}(PlatformLoggingMXBean.class);
     * </pre>
     *
     * @return 一个 {@link LoggingMXBean} 对象。
     *
     * @see java.lang.management.PlatformLoggingMXBean
     * @since 1.5
     */
    public static synchronized LoggingMXBean getLoggingMXBean() {
        if (loggingMXBean == null) {
            loggingMXBean =  new Logging();
        }
        return loggingMXBean;
    }

    /**
     * 提供对 java.beans.PropertyChangeListener
     * 和 java.beans.PropertyChangeEvent 的访问，而不创建对 java.beans 的静态依赖。
     * 一旦 addPropertyChangeListener 和 removePropertyChangeListener 方法被移除，此类也可以移除。
     */
    private static class Beans {
        private static final Class<?> propertyChangeListenerClass =
            getClass("java.beans.PropertyChangeListener");

        private static final Class<?> propertyChangeEventClass =
            getClass("java.beans.PropertyChangeEvent");

        private static final Method propertyChangeMethod =
            getMethod(propertyChangeListenerClass,
                      "propertyChange",
                      propertyChangeEventClass);

        private static final Constructor<?> propertyEventCtor =
            getConstructor(propertyChangeEventClass,
                           Object.class,
                           String.class,
                           Object.class,
                           Object.class);

        private static Class<?> getClass(String name) {
            try {
                return Class.forName(name, true, Beans.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        private static Constructor<?> getConstructor(Class<?> c, Class<?>... types) {
            try {
                return (c == null) ? null : c.getDeclaredConstructor(types);
            } catch (NoSuchMethodException x) {
                throw new AssertionError(x);
            }
        }

        private static Method getMethod(Class<?> c, String name, Class<?>... types) {
            try {
                return (c == null) ? null : c.getMethod(name, types);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        }

        /**
         * 如果 java.beans 存在，返回 {@code true}。
         */
        static boolean isBeansPresent() {
            return propertyChangeListenerClass != null &&
                   propertyChangeEventClass != null;
        }

        /**
         * 返回具有给定源、属性名称、旧值和新值的新 PropertyChangeEvent。
         */
        static Object newPropertyChangeEvent(Object source, String prop,
                                             Object oldValue, Object newValue)
        {
            try {
                return propertyEventCtor.newInstance(source, prop, oldValue, newValue);
            } catch (InstantiationException | IllegalAccessException x) {
                throw new AssertionError(x);
            } catch (InvocationTargetException x) {
                Throwable cause = x.getCause();
                if (cause instanceof Error)
                    throw (Error)cause;
                if (cause instanceof RuntimeException)
                    throw (RuntimeException)cause;
                throw new AssertionError(x);
            }
        }

        /**
         * 使用给定的事件调用给定 PropertyChangeListener 的 propertyChange 方法。
         */
        static void invokePropertyChange(Object listener, Object ev) {
            try {
                propertyChangeMethod.invoke(listener, ev);
            } catch (IllegalAccessException x) {
                throw new AssertionError(x);
            } catch (InvocationTargetException x) {
                Throwable cause = x.getCause();
                if (cause instanceof Error)
                    throw (Error)cause;
                if (cause instanceof RuntimeException)
                    throw (RuntimeException)cause;
                throw new AssertionError(x);
            }
        }
    }
}
