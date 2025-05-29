
/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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
 * 存在一个全局的 LogManager 对象，用于维护一组关于 Loggers 和日志服务的共享状态。
 * <p>
 * 这个 LogManager 对象：
 * <ul>
 * <li> 管理一个 Logger 对象的分层命名空间。所有命名的 Logger 都存储在这个命名空间中。
 * <li> 管理一组日志控制属性。这些是简单的键值对，可以由 Handlers 和其他日志对象使用来配置自己。
 * </ul>
 * <p>
 * 可以使用 LogManager.getLogManager() 获取全局的 LogManager 对象。LogManager 对象在类初始化时创建，并且之后不能更改。
 * <p>
 * 在启动时，LogManager 类通过 java.util.logging.manager 系统属性定位。
 * <p>
 * LogManager 定义了两个可选的系统属性，允许控制初始配置：
 * <ul>
 * <li>"java.util.logging.config.class"
 * <li>"java.util.logging.config.file"
 * </ul>
 * 这两个属性可以在 "java" 命令的命令行中指定，或者作为传递给 JNI_CreateJavaVM 的系统属性定义。
 * <p>
 * 如果设置了 "java.util.logging.config.class" 属性，则该属性值将被视为类名。给定的类将被加载，创建一个对象，并且该对象的构造函数负责读取初始配置。（该对象可以使用其他系统属性来控制其配置。）替代配置类可以使用 <tt>readConfiguration(InputStream)</tt> 在 LogManager 中定义属性。
 * <p>
 * 如果没有设置 "java.util.logging.config.class" 属性，则可以使用 "java.util.logging.config.file" 系统属性来指定一个属性文件（以 java.util.Properties 格式）。初始日志配置将从该文件中读取。
 * <p>
 * 如果没有定义这两个属性，则 LogManager 使用其默认配置。默认配置通常从 Java 安装目录中的属性文件 "{@code lib/logging.properties}" 加载。
 * <p>
 * 日志记录器和 Handlers 的属性名称将以处理程序或日志记录器的点分隔名称开头。
 * <p>
 * 全局日志属性可能包括：
 * <ul>
 * <li>一个 "handlers" 属性。这定义了一个空白或逗号分隔的处理程序类名列表，用于加载并注册为根 Logger（名为 ""）的处理程序。每个类名必须是一个具有默认构造函数的处理程序类。注意，这些处理程序可能在首次使用时懒加载。
 *
 * <li>一个 "&lt;logger&gt;.handlers" 属性。这定义了一个空白或逗号分隔的处理程序类名列表，用于加载并注册为指定日志记录器的处理程序。每个类名必须是一个具有默认构造函数的处理程序类。注意，这些处理程序可能在首次使用时懒加载。
 *
 * <li>一个 "&lt;logger&gt;.useParentHandlers" 属性。这定义了一个布尔值。默认情况下，每个日志记录器都会调用其父日志记录器，除了处理日志消息本身之外，这通常会导致消息也被根日志记录器处理。当将此属性设置为 false 时，需要为此日志记录器配置一个处理程序，否则不会传递任何日志消息。
 *
 * <li>一个 "config" 属性。此属性旨在允许运行任意配置代码。该属性定义了一个空白或逗号分隔的类名列表。每个命名类将创建一个新实例。每个类的默认构造函数可以执行任意代码来更新日志配置，例如设置日志记录器级别、添加处理程序、添加过滤器等。
 * </ul>
 * <p>
 * 注意，在 LogManager 配置期间加载的所有类首先在系统类路径中搜索，然后再在用户类路径中搜索。这包括 LogManager 类、任何配置类和任何处理程序类。
 * <p>
 * 日志记录器基于它们的点分隔名称组织成一个命名层次结构。因此 "a.b.c" 是 "a.b" 的子节点，而 "a.b1" 和 "a.b2" 是同级节点。
 * <p>
 * 所有以 ".level" 结尾的属性都被假定为定义了 Logger 的日志级别。因此 "foo.level" 为名为 "foo" 的日志记录器及其在命名层次结构中的任何子节点定义了一个日志级别。日志级别按照它们在属性文件中定义的顺序应用。因此，树中子节点的级别设置应该在它们的父节点设置之后。
 * <p>
 * LogManager 对象上的所有方法都是多线程安全的。
 *
 * @since 1.4
*/

public class LogManager {
    // 全局的 LogManager 对象
    private static final LogManager manager;

    // 'props' 在锁内分配但在没有锁的情况下访问。
    // 声明它为 volatile 确保另一个线程不会看到部分构建的 'props' 对象。
    // （看到部分构建的 'props' 对象可能会导致 Hashtable.get() 抛出 NPE，因为这会使得在 Hashtable 构造函数实际完成之前调用 props.getProperties() 成为可能）。
    private volatile Properties props = new Properties();
    private final static Level defaultLevel = Level.INFO;

                // 已注册监听器的映射。映射值是注册次数，允许同一个监听器注册多次的情况。
    // 已注册监听器的映射。映射值是注册次数，允许同一个监听器注册多次的情况。
    private final Map<Object,Integer> listenerMap = new HashMap<>();

    // 系统日志记录器和用户日志记录器的 LoggerContext
    private final LoggerContext systemContext = new SystemLoggerContext();
    private final LoggerContext userContext = new LoggerContext();
    // 非 final 字段 - 使其成为 volatile 类型以确保其他线程在 ensureLogManagerInitialized() 执行完毕后能看到新值。
    private volatile Logger rootLogger;
    // 是否已经完成了配置文件的初始读取？
    // （必须在足够的 java.lang.System 初始化完成后才能进行）
    private volatile boolean readPrimordialConfiguration;
    // 全局（根）处理器是否已经初始化？
    // 在 readConfiguration 中设置为 false
    private boolean initializedGlobalHandlers = true;
    // 如果 JVM 即将死亡并且退出钩子已被调用，则为 true。
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
                    System.err.println("无法加载 Logmanager \"" + cname + "\"");
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
    // 它执行“重置”以关闭所有打开的处理器。
    private class Cleaner extends Thread {

        private Cleaner() {
            /* 将上下文类加载器设置为 null 以避免
             * 保持对应用程序类加载器的强引用。
             */
            this.setContextClassLoader(null);
        }

        @Override
        public void run() {
            // 这是为了确保 LogManager.<clinit> 在同步块之前完成。
            // 否则可能会出现死锁。
            LogManager mgr = manager;

            // 如果全局处理器尚未初始化，我们
            // 不希望只是为了关闭它们而初始化它们！
            synchronized (LogManager.this) {
                // 注意死亡即将来临。
                deathImminent = true;
                initializedGlobalHandlers = true;
            }

            // 执行重置以关闭所有活动处理器。
            reset();
        }
    }


    /**
     * 受保护的构造函数。这是受保护的，以便容器应用程序
     * （如 J2EE 容器）可以子类化该对象。它是非公共的，因为
     * 意图是只有一个 LogManager 对象，其值通过调用 LogManager.getLogManager 获取。
     */
    protected LogManager() {
        this(checkSubclassPermissions());
    }

    private LogManager(Void checked) {

        // 添加一个关闭钩子以关闭全局处理器。
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
            // 以便将 Cleaner() 线程注册为关闭钩子。
            // 在这里检查它们以避免构造对象等的开销。
            sm.checkPermission(new RuntimePermission("shutdownHooks"));
            sm.checkPermission(new RuntimePermission("setContextClassLoader"));
        }
        return null;
    }

    /**
     * 惰性初始化：如果此管理器实例是全局
     * 管理器，则此方法将读取初始配置并
     * 通过调用 addLogger() 添加根记录器和全局记录器。
     *
     * 请注意，这与我们在 LoggerContext 中所做的略有不同。
     * 在 LoggerContext 中，我们修补日志记录器上下文树以将
     * 根记录器和全局记录器 *添加到上下文树*。
     *
     * 为了使这有效，必须已经对 LogManager 实例调用过一次 addLogger()
     * 以添加默认记录器。
     *
     * 这就是为什么在将任何记录器添加到任何记录器上下文之前
     * 必须调用 ensureLogManagerInitialized()。
     *
     */
    private boolean initializedCalled = false;
    private volatile boolean initializationDone = false;
    final void ensureLogManagerInitialized() {
        final LogManager owner = this;
        if (initializationDone || owner != manager) {
            // 我们不希望执行两次，也不希望在
            // 私有管理器实例上执行。
            return;
        }

        // 可能另一个线程在我们之前调用了 ensureLogManagerInitialized()
        // 并且仍在执行。如果是这样，我们将阻塞直到
        // 日志管理器完成初始化，然后获取监视器，
        // 发现 initializationDone 现在为 true 并返回。
        // 否则 - 我们是第一个到达这里的！我们将获取监视器，
        // 发现 initializationDone 仍然为 false，并执行
        // 初始化。
        //
        synchronized(this) {
            // 如果 initializedCalled 为 true，这意味着我们已经在
            // 本线程中初始化 LogManager 的过程中。
            // 确保LogManagerInitialized() 递归调用。
            final boolean isRecursiveInitialization = (initializedCalled == true);


                        assert initializedCalled || !initializationDone
                    : "Initialization can't be done if initialized has not been called!";

            if (isRecursiveInitialization || initializationDone) {
                // 如果 isRecursiveInitialization 为 true，这意味着我们已经在
                // 这个线程中初始化 LogManager 的过程中。ensureLogManagerInitialized()
                // 被递归调用。我们不应该继续，因为这会导致无限递归。
                //
                // 如果 initializationDone 为 true，则表示管理器已经完成初始化；
                // 只需返回：我们已经完成了。
                return;
            }
            // 下面调用 addLogger 将会调用 requiresDefaultLogger()
            // 这将调用 ensureLogManagerInitialized()。
            // 我们使用 initializedCalled 来打破递归。
            initializedCalled = true;
            try {
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    @Override
                    public Object run() {
                        assert rootLogger == null;
                        assert initializedCalled && !initializationDone;

                        // 读取配置。
                        owner.readPrimordialConfiguration();

                        // 为命名空间的根创建并保留 Logger。
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

                        // 确保全局 logger 会被注册到全局管理器中
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
     * 返回全局的 LogManager 对象。
     * @return 全局的 LogManager 对象
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
                    // 指示，表明我们仍然处于引导阶段
                    if (System.out == null) {
                        return;
                    }
                    readPrimordialConfiguration = true;

                    try {
                        AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                                @Override
                                public Void run() throws Exception {
                                    readConfiguration();

                                    // 平台日志记录器开始委托给 java.util.logging.Logger
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
     * 添加一个事件监听器，在重新读取日志属性时调用。添加多个相同事件监听器实例
     * 会导致属性事件监听器表中有多个条目。
     *
     * <p><b>警告：</b>此方法在所有不包含 {@code java.beans} 包的 Java SE 子集
     * 配置文件中被省略。</p>
     *
     * @param l  事件监听器
     * @exception  SecurityException  如果存在安全经理，并且调用者没有
     *             LoggingPermission("control") 权限。
     * @exception NullPointerException 如果 PropertyChangeListener 为 null。
     * @deprecated 对 {@code PropertyChangeListener} 的依赖对 Java
     *             平台的未来模块化造成了重大障碍。此方法将在未来的版本中移除。
     *             全局 {@code LogManager} 可以通过重写 {@link
     *             #readConfiguration readConfiguration} 方法来检测日志配置的更改。
     */
    @Deprecated
    public void addPropertyChangeListener(PropertyChangeListener l) throws SecurityException {
        PropertyChangeListener listener = Objects.requireNonNull(l);
        checkPermission();
        synchronized (listenerMap) {
            // 如果已经注册，则增加注册计数
            Integer value = listenerMap.get(listener);
            value = (value == null) ? 1 : (value + 1);
            listenerMap.put(listener, value);
        }
    }

    /**
     * 移除属性更改事件的事件监听器。
     * 如果相同的监听器实例通过多次调用 <CODE>addPropertyChangeListener</CODE>
     * 添加到监听器表中，则需要相同数量的
     * <CODE>removePropertyChangeListener</CODE> 调用来移除所有实例。
     * <P>
     * 如果未找到给定的监听器，则静默返回。
     *
     * <p><b>警告：</b>此方法在所有不包含 {@code java.beans} 包的 Java SE 子集
     * 配置文件中被省略。</p>
     *
     * @param l  事件监听器（可以为 null）
     * @exception  SecurityException  如果存在安全经理，并且调用者没有
     *             LoggingPermission("control") 权限。
     * @deprecated 对 {@code PropertyChangeListener} 的依赖对 Java
     *             平台的未来模块化造成了重大障碍。此方法将在未来的版本中移除。
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
    // 日志记录器在每个 AppContext 中是隔离的。
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
                    // 如果我们在主应用程序上下文中，这将为 null。
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

    // 查找或创建指定的日志记录器实例。如果已经使用给定名称创建了日志记录器，则返回该日志记录器。
    // 否则，在 LogManager 全局命名空间中创建并注册一个新的日志记录器实例。
    // 此方法总是返回一个非空的 Logger 对象。
    // 这里不需要同步。添加新 Logger 对象的所有同步由 addLogger() 处理。
    //
    // 此方法必须委托给 LogManager 实现来添加新 Logger 或返回之前添加的 Logger，
    // 因为 LogManager 子类可能会覆盖 addLogger、getLogger、readConfiguration 等方法。
    Logger demandLogger(String name, String resourceBundleName, Class<?> caller) {
        Logger result = getLogger(name);
        if (result == null) {
            // 仅分配一次新的日志记录器
            Logger newLogger = new Logger(name, resourceBundleName, caller, this, false);
            do {
                if (addLogger(newLogger)) {
                    // 我们成功添加了上面创建的新 Logger，因此直接返回它，无需重新获取。
                    return newLogger;
                }

                // 我们没有添加上面创建的新 Logger，因为另一个线程在我们上面的 null 检查之后和调用 addLogger() 之前添加了一个同名的 Logger。
                // 我们必须重新获取 Logger，因为 addLogger() 返回一个布尔值而不是 Logger 引用本身。
                // 然而，如果创建其他 Logger 的线程没有持有对该 Logger 的强引用，则在我们看到它在 addLogger() 中并尝试重新获取它之前，该 Logger 可能会被垃圾回收。
                // 如果它已经被垃圾回收，我们将再次循环并重试。
                result = getLogger(name);
            } while (result == null);
        }
        return result;
    }

    Logger demandSystemLogger(String name, String resourceBundleName) {
        // 在系统上下文的命名空间中添加一个系统日志记录器
        final Logger sysLogger = getSystemContext().demandLogger(name, resourceBundleName);

        // 如果不存在，则将系统日志记录器添加到 LogManager 的命名空间中
        // 以确保给定名称只有一个日志记录器。
        // 系统日志记录器对应用程序可见，除非已经添加了同名的日志记录器。
        Logger logger;
        do {
            // 首先尝试调用 addLogger 而不是 getLogger
            // 这可以避免自定义 LogManager.getLogger 实现中可能存在的错误，即如果不存在则添加日志记录器
            if (addLogger(sysLogger)) {
                // 成功添加了新的系统日志记录器
                logger = sysLogger;
            } else {
                logger = getLogger(name);
            }
        } while (logger == null);

        // LogManager 将通过 LogManager.addLogger 方法设置 sysLogger 的处理程序。
        if (logger != sysLogger && sysLogger.accessCheckedHandlers().length == 0) {
            // 如果日志记录器已经存在但未设置处理程序
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

    // LoggerContext 每个上下文维护一个日志记录器命名空间。
    // 默认的 LogManager 实现有一个系统上下文和用户上下文。
    // 系统上下文用于维护所有系统日志记录器的命名空间，并由系统代码查询。
    // 如果系统日志记录器在用户上下文中不存在，则也会添加到用户上下文中。
    // 用户上下文由用户代码查询，所有其他日志记录器都添加到用户上下文中。
    class LoggerContext {
        // 命名日志记录器的表，将名称映射到日志记录器。
        private final Hashtable<String,LoggerWeakRef> namedLoggers = new Hashtable<>();
        // 命名日志记录器的树
        private final LogNode root;
        private LoggerContext() {
            this.root = new LogNode(null, this);
        }


        // 告诉在此上下文中是否需要默认的日志记录器。
        // 如果为 true，则默认的日志记录器将被惰性添加。
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

        // 此上下文所有者的根日志记录器，如果它不为 null，并且
        // 该上下文需要默认的日志记录器，则它将被添加到上下文
        // 日志记录器的树中。
        final Logger getRootLogger() {
            return getOwner().rootLogger;
        }

        // 全局日志记录器，如果它不为 null，并且
        // 该上下文需要默认的日志记录器，则它将被添加到上下文
        // 日志记录器的树中。
        final Logger getGlobalLogger() {
            @SuppressWarnings("deprecated") // 避免初始化循环。
            final Logger global = Logger.global;
            return global;
        }

        Logger demandLogger(String name, String resourceBundleName) {
            // LogManager 的子类可能有自己的实现来添加和
            // 获取日志记录器。因此委托给 LogManager 来执行此操作。
            final LogManager owner = getOwner();
            return owner.demandLogger(name, resourceBundleName, null);
        }


        // 由于微妙的死锁问题，getUserContext() 不再
        // 调用 addLocalLogger(rootLogger);
        // 因此，我们需要稍后添加默认的日志记录器。
        // 检查上下文是否已正确初始化
        // 在调用例如 find(name)
        // 或 getLoggerNames() 之前，这是必要的。
        //
        private void ensureInitialized() {
            if (requiresDefaultLoggers()) {
                // 确保根日志记录器和全局日志记录器已设置。
                ensureDefaultLogger(getRootLogger());
                ensureDefaultLogger(getGlobalLogger());
            }
        }


        synchronized Logger findLogger(String name) {
            // 确保在查找日志记录器之前
            // 该上下文已正确初始化。
            ensureInitialized();
            LoggerWeakRef ref = namedLoggers.get(name);
            if (ref == null) {
                return null;
            }
            Logger logger = ref.get();
            if (logger == null) {
                // Hashtable 持有已过期的弱引用
                // 到已被垃圾回收的日志记录器。
                ref.dispose();
            }
            return logger;
        }

        // 在将日志记录器添加到
        // 上下文之前调用此方法。
        // 'logger' 是将要添加的上下文。
        // 此方法将确保在添加 'logger' 之前
        // 添加默认的日志记录器。
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
            // 用于惰性添加根日志记录器和全局日志记录器
            // 到 LoggerContext。

            // 此检查是简单的健全性检查：我们不希望此
            // 方法被调用于 Logger.global
            // 或 owner.rootLogger 以外的任何内容。
            if (!requiresDefaultLoggers() || logger == null
                    || logger != Logger.global && logger != LogManager.this.rootLogger) {

                // 非 null 日志记录器既不是
                // Logger.global 也不是 manager.rootLogger 的情况表明了一个严重
                // 问题 - 因为 ensureDefaultLogger 不应被调用
                // 除这两个（或 null - 如果
                // 例如 manager.rootLogger 尚未初始化）之外的任何日志记录器...
                assert logger == null;

                return;
            }

            // 如果它还没有被添加，则添加该日志记录器。
            if (!namedLoggers.containsKey(logger.getName())) {
                // 重要的是防止 addLocalLogger
                // 在添加默认日志记录器的过程中调用
                // ensureAllDefaultLoggers - 因为这会立即导致堆栈溢出。
                // 因此，即使 requiresDefaultLoggers 为 true，我们也必须传递 addDefaultLoggersIfNeeded=false。
                addLocalLogger(logger, false);
            }
        }

        boolean addLocalLogger(Logger logger) {
            // 如果不需要，则无需添加默认日志记录器
            return addLocalLogger(logger, requiresDefaultLoggers());
        }

        // 将日志记录器添加到此上下文中。此方法仅设置其级别
        // 并处理父日志记录器。它不设置其处理程序。
        synchronized boolean addLocalLogger(Logger logger, boolean addDefaultLoggersIfNeeded) {
            // addDefaultLoggersIfNeeded 用于在添加
            // 默认日志记录器时打破递归。如果我们正在添加一个默认日志记录器
            // （我们被 ensureDefaultLogger() 调用），则
            // addDefaultLoggersIfNeeded 将为 false：我们不希望再次调用
            // ensureAllDefaultLoggers。
            //
            // 注意：当 requiresDefaultLoggers 为 false 时，addDefaultLoggersIfNeeded 也可以为 false -
            //       因为在这种情况下调用 ensureAllDefaultLoggers 没有效果。
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
                    // 可能在上面调用 drainLoggerRefQueueBounded() 之后
                    // 日志记录器已被垃圾回收，因此允许
                    // 注册一个新的。
                    ref.dispose();
                } else {
                    // 我们已经有一个具有给定名称的已注册日志记录器。
                    return false;
                }
            }


                        // 我们正在添加一个新的日志记录器。
            // 注意，我们在这里创建了一个弱引用。
            final LogManager owner = getOwner();
            logger.setLogManager(owner);
            ref = owner.new LoggerWeakRef(logger);
            namedLoggers.put(name, ref);

            // 应用为新日志记录器定义的任何初始级别，除非
            // 日志记录器的级别已经初始化
            Level level = owner.getLevelProperty(name + ".level", null);
            if (level != null && !logger.isLevelInitialized()) {
                doSetLevel(logger, level);
            }

            // 处理程序的实例化是在 LogManager.addLogger
            // 实现中完成的，因为处理程序类可能只对 LogManager
            // 子类可见，适用于自定义日志管理器的情况
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
            // 新的 LogNode 已准备好，所以告诉 LoggerWeakRef 关于它
            ref.setNode(node);
            return true;
        }

        synchronized void removeLoggerRef(String name, LoggerWeakRef ref) {
            namedLoggers.remove(name, ref);
        }

        synchronized Enumeration<String> getLoggerNames() {
            // 确保在返回日志记录器名称之前，此上下文已正确初始化。
            ensureInitialized();
            return namedLoggers.keys();
        }

        // 如果 logger.getUseParentHandlers() 返回 'true' 并且日志记录器的
        // 父节点中定义了级别或处理程序，确保它们被实例化。
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
                    // 此 pname 有级别/处理程序定义。
                    // 确保它存在。
                    demandLogger(pname, null);
                }
                ix = ix2+1;
            }
        }

        // 获取我们日志记录器节点树中的一个节点。
        // 如果必要，创建它。
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
        // LogManager 的命名空间中添加一个系统日志记录器，如果不存在的话，以确保
        // 只有一个给定名称的单个日志记录器。系统日志记录器对应用程序可见，
        // 除非已添加了同名的日志记录器。
        @Override
        Logger demandLogger(String name, String resourceBundleName) {
            Logger result = findLogger(name);
            if (result == null) {
                // 仅分配一次新的系统日志记录器
                Logger newLogger = new Logger(name, resourceBundleName, null, getOwner(), true);
                do {
                    if (addLocalLogger(newLogger)) {
                        // 我们成功添加了上面创建的新日志记录器，所以直接返回它，无需重新获取。
                        result = newLogger;
                    } else {
                        // 我们没有添加上面创建的新日志记录器，
                        // 因为在我们上面的空检查之后和调用 addLogger() 之前，
                        // 另一个线程添加了一个同名的日志记录器。我们必须重新获取日志记录器，
                        // 因为 addLogger() 返回一个布尔值而不是日志记录器引用本身。然而，如果创建
                        // 其他日志记录器的线程没有持有对该日志记录器的强引用，
                        // 那么在我们在 addLogger() 中看到它之后和我们能够重新获取它之前，
                        // 其他日志记录器可能已经被垃圾回收。如果它已经被垃圾回收，
                        // 那么我们将循环并再次尝试。
                        result = findLogger(name);
                    }
                } while (result == null);
            }
            return result;
        }
    }

                // 添加新的每记录器处理器。
    // 我们需要在这里提升权限。所有我们的决定都将
    // 基于日志记录配置，这只能由受信任的代码修改。
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
                        // 检查是否有属性定义了
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
                        // 将此处理器添加到记录器中
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


    // loggerRefQueue 持有 Logger 对象的 LoggerWeakRef 对象
    // 这些 Logger 对象已经被垃圾回收。
    private final ReferenceQueue<Logger> loggerRefQueue
        = new ReferenceQueue<>();

    // 包级内部类。
    // 用于管理 Logger 对象的 WeakReference 的辅助类。
    //
    // LogManager.namedLoggers
    //     - 持有所有命名记录器的弱引用
    //     - namedLoggers 保留命名记录器的 LoggerWeakRef 对象，直到我们可以处理
    //       被垃圾回收的命名记录器的账目。
    // LogManager.LogNode.loggerRef
    //     - 持有一个命名记录器的弱引用
    //     - LogNode 也会保留命名记录器的 LoggerWeakRef 对象；目前 LogNodes 永远不会消失。
    // Logger.kids
    //     - 持有每个直接子记录器的弱引用；这包括匿名和命名记录器
    //     - 匿名记录器总是根记录器的子记录器，根记录器是一个强引用；rootLogger.kids 保留
    //       匿名记录器的 LoggerWeakRef 对象，直到我们可以处理账目。
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
            // 避免两次调用 dispose。当一个记录器被垃圾回收时，其
            // LoggerWeakRef 将被入队。
            // 但是，在队列被清空之前，可能会添加（或查找）同名的新记录器。当这种情况发生时，dispose()
            // 将由 addLocalLogger() 或 findLogger() 调用。
            // 稍后当队列被清空时，dispose() 将再次为同一个 LoggerWeakRef 调用。
            // 标记 LoggerWeakRef 为已处理避免两次处理数据（即使代码现在应该是可重入的）。
            synchronized(this) {
                // 注意给维护者：
                // 在这个块内不要调用任何尝试获取
                // 另一个锁的方法 - 因为这肯定会导致死锁，考虑到 dispose() 可以被
                // 多个线程调用，并且在不同的同步方法/块内调用。
                if (disposed) return;
                disposed = true;
            }

            final LogNode n = node;
            if (n != null) {
                // n.loggerRef 只能安全地在
                // LoggerContext 锁内修改。removeLoggerRef 已经
                // 在 LoggerContext 上同步，所以从这个锁内调用
                // n.context.removeLoggerRef 是安全的。
                synchronized (n.context) {
                    // 如果我们有一个 LogNode，那么我们就是一个命名记录器
                    // 因此清除 namedLoggers 对我们的弱引用
                    n.context.removeLoggerRef(name, this);
                    name = null;  // 清除我们对记录器名称的引用

                    // LogNode 可能已经被重用 - 所以只有在 LogNode.loggerRef == this 时才清除
                    // LogNode.loggerRef
                    if (n.loggerRef == this) {
                        n.loggerRef = null;  // 清除 LogNode 对我们的弱引用
                    }
                    node = null;            // 清除我们对 LogNode 的引用
                }
            }

            if (parentRef != null) {
                // 此 LoggerWeakRef 有或曾经有一个父记录器
                Logger parent = parentRef.get();
                if (parent != null) {
                    // 父记录器仍然存在，因此清除
                    // 父记录器对我们的弱引用
                    parent.removeChildLogger(this);
                }
                parentRef = null;  // 清除我们对父记录器的弱引用
            }
        }

}


                    // 设置节点字段为指定的值
        void setNode(LogNode node) {
            this.node = node;
        }

        // 设置 parentRef 字段为指定的值
        void setParentRef(WeakReference<Logger> parentRef) {
            this.parentRef = parentRef;
        }
    }

    // 包级方法。
    // 清理一些已被垃圾回收的 Logger 对象。
    //
    // drainLoggerRefQueueBounded() 由下面的 addLogger() 调用
    // 和 Logger.getAnonymousLogger(String) 调用，因此我们每次添加一个 Logger 时
    // 会清理多达 MAX_ITERATIONS 个已垃圾回收的 Logger。
    //
    // 在 WinXP VMware 客户端上，MAX_ITERATIONS 值为 400 时
    // 在 AnonLoggerWeakRefLeak 测试中，我们大约有 50/50 的弱引用计数增加与
    // 减少的比例。以下是清理 400 个匿名 Logger 的统计：
    //   - 测试持续时间 1 分钟
    //   - 样本大小 125 组 400 个
    //   - 平均值：1.99 毫秒
    //   - 最小值：0.57 毫秒
    //   - 最大值：25.3 毫秒
    //
    // 同样的配置在 LoggerWeakRefLeak 测试中给出了更好的弱引用计数减少
    // 比增加的弱引用计数。以下是清理 400 个命名 Logger 的统计：
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
                // 还未完成加载 LogManager
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
     * 添加一个命名的 Logger。如果已经注册了同名的 Logger，则此方法不执行任何操作并返回 false。
     * <p>
     * Logger 工厂方法调用此方法来注册每个新创建的 Logger。
     * <p>
     * 应用程序应保留对 Logger 对象的引用以防止其被垃圾回收。LogManager
     * 可能只保留弱引用。
     *
     * @param   logger 新的 Logger。
     * @return  如果参数 Logger 注册成功，则返回 true，
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
            // 我们是否有每个 Logger 的处理器？
            // 注意：这将增加 200 毫秒的延迟
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
     * 进行安全敏感的日志记录。还应注意，与 String {@code name} 关联的
     * Logger 可能随时被垃圾回收，如果没有任何对 Logger 的强引用。调用此方法的
     * 必须检查返回值是否为 null 以正确处理 Logger 已被垃圾回收的情况。
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
     * 而不是对 Logger 本身的强引用。返回的字符串不会阻止 Logger 被垃圾回收。特别是，
     * 如果返回的名称传递给 {@code LogManager.getLogger()}，则调用者必须检查
     * {@code LogManager.getLogger()} 的返回值是否为 null，以正确处理自返回名称以来
     * Logger 已被垃圾回收的情况。
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
     * 新配置文件中的任何日志级别定义都将使用 Logger.setLevel() 应用，如果目标 Logger 存在的话。
     * <p>
     * 在读取属性后将触发 PropertyChangeEvent。
     *
     * @exception  SecurityException  如果存在安全管理器并且调用者没有 LoggingPermission("control") 权限。
     * @exception  IOException 如果读取配置时遇到 IO 问题。
     */
    public void readConfiguration() throws IOException, SecurityException {
        checkPermission();

        // 如果指定了配置类，则加载并使用它。
        String cname = System.getProperty("java.util.logging.config.class");
        if (cname != null) {
            try {
                // 实例化命名的类。由其构造函数负责通过调用 readConfiguration(InputStream) 并提供合适的流来初始化日志配置。
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
                System.err.println("日志配置类 \"" + cname + "\" 失败");
                System.err.println("" + ex);
                // 继续并使用有用的配置文件。
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
     * 对于所有命名的日志记录器，重置操作将移除并关闭所有处理程序，并（除了根记录器外）将级别设置为 null。根记录器的级别设置为 Level.INFO。
     *
     * @exception  SecurityException  如果存在安全管理器并且调用者没有 LoggingPermission("control") 权限。
     */

    public void reset() throws SecurityException {
        checkPermission();
        synchronized (this) {
            props = new Properties();
            // 由于我们正在进行重置，因此如果尚未初始化全局处理程序，我们不再希望初始化它们。
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

    // 重置单个目标记录器的私有方法。
    private void resetLogger(Logger logger) {
        // 关闭记录器的所有处理程序。
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
            // 这是根记录器。
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
            ix = end+1;
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
     * 在读取属性后将触发 PropertyChangeEvent。
     * <p>
     * 新配置文件中的任何日志级别定义都将使用 Logger.setLevel() 应用，如果目标 Logger 存在的话。
     *
     * @param ins       从中读取属性的流
     * @exception  SecurityException  如果存在安全管理器并且调用者没有 LoggingPermission("control") 权限。
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

        // 根据新属性设置任何已存在的日志记录器的级别。
        setLevelsOnExistingLoggers();

        // 通知任何感兴趣的方我们的属性已更改。
        // 我们首先复制监听器映射，以便在调用监听器时不会持有任何锁。
        Map<Object,Integer> listeners = null;
        synchronized (listenerMap) {
            if (!listenerMap.isEmpty())
                listeners = new HashMap<>(listenerMap);
        }
        if (listeners != null) {
            assert Beans.isBeansPresent();
            Object ev = Beans.newPropertyChangeEvent(LogManager.class, null, null, null);
            for (Map.Entry<Object,Integer> entry : listeners.entrySet()) {
                Object listener = entry.getKey();
                int count = entry.getValue().intValue();
                for (int i = 0; i < count; i++) {
                    Beans.invokePropertyChange(listener, ev);
                }
            }
        }


        // 注意，当它们首次被引用时，我们需要重新初始化全局句柄。
        synchronized (this) {
            initializedGlobalHandlers = false;
        }
    }

    /**
     * 获取日志属性的值。
     * 如果未找到属性，则返回 null。
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
            // 在创建类或创建实例时，我们遇到了各种异常之一。
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
            // 在创建类或创建实例时，我们遇到了各种异常之一。
            // 继续执行。
        }
        // 我们遇到了异常。返回 defaultValue。
        return defaultValue;
    }

    // 私有方法，用于加载全局处理程序。
    // 当全局处理程序首次使用时，我们懒惰地执行实际工作。
    private synchronized void initializeGlobalHandlers() {
        if (initializedGlobalHandlers) {
            return;
        }

        initializedGlobalHandlers = true;

        if (deathImminent) {
            // 哎呀...
            // 虚拟机正在关闭，我们的退出钩子已被调用。
            // 避免分配全局处理程序。
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
 * 检查当前上下文是否有权限修改日志配置。这需要 LoggingPermission("control")。
 * <p>
 * 如果检查失败，我们将抛出一个 SecurityException，否则
 * 我们正常返回。
 *
 * @exception  SecurityException  如果存在安全经理并且调用者没有 LoggingPermission("control")。
 */
public void checkAccess() throws SecurityException {
    checkPermission();
}

// 嵌套类，用于表示命名日志记录器树中的节点。
private static class LogNode {
    HashMap<String, LogNode> children;
    LoggerWeakRef loggerRef;
    LogNode parent;
    final LoggerContext context;

    LogNode(LogNode parent, LoggerContext context) {
        this.parent = parent;
        this.context = context;
    }

    // 递归方法，用于遍历节点下的树并设置新的父日志记录器。
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

// 我们使用 Logger 的子类作为根日志记录器，以便仅在首次需要时实例化全局处理器。
private final class RootLogger extends Logger {
    private RootLogger() {
        // 我们不在此处调用受保护的 Logger 两参数构造函数，
        // 以避免在 RootLogger 构造函数中调用 LogManager.getLogManager()。
        super("", null, null, LogManager.this, true);
    }

    @Override
    public void log(LogRecord record) {
        // 确保全局处理器已被实例化。
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


// 配置更改时调用的私有方法，以将任何级别设置应用到现有的日志记录器。
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
            System.err.println("Bad level value for property: " + key);
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
 * {@link javax.management.ObjectName} 的字符串表示形式。
 *
 * @see java.lang.management.PlatformLoggingMXBean
 * @see java.util.logging.LoggingMXBean
 *
 * @since 1.5
 */
public final static String LOGGING_MXBEAN_NAME
    = "java.util.logging:type=Logging";

/**
 * 返回用于管理日志记录器的 <tt>LoggingMXBean</tt>。
 * 管理日志记录器的另一种方法是通过
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
 * 提供对 java.beans.PropertyChangeListener 和 java.beans.PropertyChangeEvent 的访问，
 * 而不创建对 java.beans 的静态依赖。一旦 addPropertyChangeListener 和 removePropertyChangeListener 方法被移除，
 * 这个类也可以被移除。
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
         * 如果存在 java.beans，则返回 {@code true}。
         */
        static boolean isBeansPresent() {
            return propertyChangeListenerClass != null &&
                   propertyChangeEventClass != null;
        }

        /**
         * 返回一个新的 PropertyChangeEvent，其中包含给定的源、属性名、旧值和新值。
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
         * 使用给定的事件调用给定的 PropertyChangeListener 的 propertyChange 方法。
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
