/*
 * 版权所有 (c) 2000, 2017，Oracle 和/或其附属公司。保留所有权利。
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

import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

/**
 * Logger 对象用于记录特定系统或应用程序组件的消息。日志记录器通常被命名，
 * 使用分层的点分隔命名空间。日志记录器名称可以是任意字符串，但通常应基于
 * 被记录组件的包名或类名，例如 java.net 或 javax.swing。此外，还可以创建
 * “匿名”日志记录器，这些日志记录器不会存储在日志记录器命名空间中。
 * <p>
 * 可以通过调用其中一个 getLogger 工厂方法来获取 Logger 对象。这些方法将
 * 创建一个新的 Logger 或返回一个合适的现有 Logger。需要注意的是，通过
 * {@code getLogger} 工厂方法返回的 Logger 可能会在任何时候被垃圾回收，如果
 * 没有保持对 Logger 的强引用。
 * <p>
 * 日志消息将被转发到已注册的 Handler 对象，这些对象可以将消息转发到各种
 * 目的地，包括控制台、文件、操作系统日志等。
 * <p>
 * 每个 Logger 都会跟踪一个“父”Logger，即其在 Logger 命名空间中的最近
 * 现有祖先。
 * <p>
 * 每个 Logger 都有一个“级别”与之关联。这反映了此 Logger 关心的最小级别。
 * 如果 Logger 的级别被设置为 <tt>null</tt>，则其有效级别将从其父级继承，父级
 * 可能会递归地从其父级获取，依此类推。
 * <p>
 * 日志级别可以根据日志配置文件中的属性进行配置，如 LogManager 类的描述中所述。
 * 但是，也可以通过调用 Logger.setLevel 方法动态更改。如果更改了 Logger 的级别，
 * 更改可能会影响子 Logger，因为任何子 Logger 如果其级别为 <tt>null</tt>，将继承
 * 其父级的有效级别。
 * <p>
 * 在每次日志记录调用时，Logger 会首先将请求级别（例如，SEVERE 或 FINE）与
 * Logger 的有效日志级别进行快速检查。如果请求级别低于日志级别，日志记录调用
 * 立即返回。
 * <p>
 * 通过了初始（快速）测试后，Logger 将分配一个 LogRecord 来描述日志消息。然后
 * 调用一个 Filter（如果存在）来对记录是否应发布进行更详细的检查。如果通过了
 * 这个检查，它将把 LogRecord 发布到其输出 Handler。默认情况下，日志记录器还会
 * 将日志发布到其父级的 Handler，递归地向上遍历树。
 * <p>
 * 每个 Logger 可能会有一个 {@code ResourceBundle} 与之关联。可以使用
 * {@link #getLogger(java.lang.String, java.lang.String)} 工厂方法通过名称指定
 * {@code ResourceBundle}，或使用 {@link
 * #setResourceBundle(java.util.ResourceBundle) setResourceBundle} 方法通过值指定。
 * 此包将用于本地化日志消息。如果 Logger 没有自己的 {@code ResourceBundle} 或
 * 资源包名称，则它将从其父级继承 {@code ResourceBundle} 或资源包名称，递归地向上
 * 遍历树。
 * <p>
 * 大多数日志记录器输出方法接受一个“msg”参数。此 msg 参数可以是原始值或
 * 本地化键。在格式化期间，如果 Logger（或继承了）一个本地化 {@code ResourceBundle}，
 * 并且 {@code ResourceBundle} 有 msg 字符串的映射，则 msg 字符串将被替换为本地化值。
 * 否则，将使用原始 msg 字符串。通常，格式化程序使用 java.text.MessageFormat 风格
 * 的格式化来格式化参数，因此例如格式字符串 "{0} {1}" 将格式化两个参数为字符串。
 * <p>
 * 一组方法接受一个“msgSupplier”而不是“msg”参数。这些方法接受一个
 * {@link Supplier}{@code <String>} 函数，该函数仅在基于有效日志级别实际记录消息时
 * 被调用以构建所需的日志消息，从而消除不必要的消息构建。例如，如果开发人员希望
 * 记录系统健康状态以进行诊断，使用接受字符串的版本，代码如下：
 <pre><code>

   class DiagnosisMessages {
     static String systemHealthStatus() {
       // 收集系统健康信息
       ...
     }
   }
   ...
   logger.log(Level.FINER, DiagnosisMessages.systemHealthStatus());
</code></pre>
 * 使用上述代码，即使 FINER 日志级别被禁用，健康状态也会被不必要地收集。使用接受
 * Supplier 的版本如下，状态仅在 FINER 日志级别启用时收集。
 <pre><code>

   logger.log(Level.FINER, DiagnosisMessages::systemHealthStatus);
</code></pre>
 * <p>
 * 在查找 {@code ResourceBundle} 时，Logger 首先会查看是否使用 {@link
 * #setResourceBundle(java.util.ResourceBundle) setResourceBundle} 指定了一个包，然后
 * 查看是否通过 {@link
 * #getLogger(java.lang.String, java.lang.String) getLogger} 工厂方法指定了一个资源包名称。
 * 如果没有找到 {@code ResourceBundle} 或资源包名称，则将使用从其父级树继承的最近的
 * {@code ResourceBundle} 或资源包名称。<br>
 * 当通过 {@link
 * #setResourceBundle(java.util.ResourceBundle) setResourceBundle} 方法继承或指定了
 * 一个 {@code ResourceBundle} 时，将使用该 {@code ResourceBundle}。否则，如果 Logger
 * 仅具有或继承了一个资源包名称，则将使用默认区域设置将该资源包名称映射到一个
 * {@code ResourceBundle} 对象。
 * <br id="ResourceBundleMapping">当将资源包名称映射到 {@code ResourceBundle} 对象时，
 * Logger 首先尝试使用线程的 {@linkplain java.lang.Thread#getContextClassLoader() 上下文类加载器}
 * 将给定的资源包名称映射到一个 {@code ResourceBundle}。如果线程上下文类加载器为 {@code null}，
 * 它将尝试使用 {@linkplain java.lang.ClassLoader#getSystemClassLoader() 系统类加载器}。
 * 如果仍然找不到 {@code ResourceBundle}，它将使用调用 {@link
 * #getLogger(java.lang.String, java.lang.String) getLogger} 工厂方法的第一个调用者的类加载器。
 * <p>
 * 格式化（包括本地化）是输出 Handler 的责任，通常会调用一个 Formatter。
 * <p>
 * 需要注意的是，格式化不一定同步发生。它可能会延迟到 LogRecord 实际写入外部接收器时。
 * <p>
 * 日志记录方法分为五个主要类别：
 * <ul>
 * <li><p>
 *     有一组“log”方法，接受日志级别、消息字符串，以及可选的消息字符串参数。
 * <li><p>
 *     有一组“logp”方法（用于“精确日志记录”），类似于“log”方法，但还接受显式的源类名
 *     和方法名。
 * <li><p>
 *     有一组“logrb”方法（用于“使用资源包日志记录”），类似于“logp”方法，但还接受显式的
 *     资源包对象，用于本地化日志消息。
 * <li><p>
 *     有用于跟踪方法进入（“entering”方法）、方法返回（“exiting”方法）和抛出异常
 *     （“throwing”方法）的便捷方法。
 * <li><p>
 *     最后，有一组用于最简单情况的便捷方法，当开发人员只想在给定日志级别记录一个简单字符串时。
 *     这些方法以标准级别名称命名（“severe”、“warning”、“info”等），并接受一个参数，即消息字符串。
 * </ul>
 * <p>
 * 对于不接受显式源名和方法名的方法，日志记录框架将尽力确定调用日志记录方法的类和方法。
 * 但是，需要注意的是，自动推断的信息可能只是近似的（甚至可能是完全错误的）。虚拟机允许
 * 在 JIT 编译时进行广泛的优化，并可能完全移除堆栈帧，使得无法可靠地定位调用类和方法。
 * <P>
 * Logger 上的所有方法都是多线程安全的。
 * <p>
 * <b>子类化信息：</b>注意，LogManager 类可能为命名空间中的任何点提供自己的 Logger 实现。
 * 因此，任何 Logger 的子类（除非它们与新的 LogManager 类一起实现）应从 LogManager 类
 * 获取 Logger 实例，并将“isLoggable”和“log(LogRecord)”等操作委托给该实例。注意，为了
 * 拦截所有日志输出，子类只需覆盖 log(LogRecord) 方法。所有其他日志记录方法都是通过调用
 * 此 log(LogRecord) 方法实现的。
 *
 * @since 1.4
 */
public class Logger {
    private static final Handler emptyHandlers[] = new Handler[0];
    private static final int offValue = Level.OFF.intValue();

                static final String SYSTEM_LOGGER_RB_NAME = "sun.util.logging.resources.logging";

    // 该类是不可变的，保持其不可变性非常重要。
    private static final class LoggerBundle {
        final String resourceBundleName; // 资源包的基本名称。
        final ResourceBundle userBundle; // 通过 setResourceBundle 设置的资源包。
        private LoggerBundle(String resourceBundleName, ResourceBundle bundle) {
            this.resourceBundleName = resourceBundleName;
            this.userBundle = bundle;
        }
        boolean isSystemBundle() {
            return SYSTEM_LOGGER_RB_NAME.equals(resourceBundleName);
        }
        static LoggerBundle get(String name, ResourceBundle bundle) {
            if (name == null && bundle == null) {
                return NO_RESOURCE_BUNDLE;
            } else if (SYSTEM_LOGGER_RB_NAME.equals(name) && bundle == null) {
                return SYSTEM_BUNDLE;
            } else {
                return new LoggerBundle(name, bundle);
            }
        }
    }

    // 此实例将由系统代码创建的所有记录器共享
    private static final LoggerBundle SYSTEM_BUNDLE =
            new LoggerBundle(SYSTEM_LOGGER_RB_NAME, null);

    // 此实例表示尚未指定资源包，
    // 它将由所有没有资源包的记录器共享。
    private static final LoggerBundle NO_RESOURCE_BUNDLE =
            new LoggerBundle(null, null);

    private volatile LogManager manager;
    private String name;
    private final CopyOnWriteArrayList<Handler> handlers =
        new CopyOnWriteArrayList<>();
    private volatile LoggerBundle loggerBundle = NO_RESOURCE_BUNDLE;
    private volatile boolean useParentHandlers = true;
    private volatile Filter filter;
    private boolean anonymous;

    // 缓存以加快 findResourceBundle 的行为：
    private ResourceBundle catalog;     // 缓存的资源包
    private String catalogName;         // 与目录关联的名称
    private Locale catalogLocale;       // 与目录关联的区域设置

    // 与父子关系和级别相关的字段
    // 在单独的锁 treeLock 下管理。
    private static final Object treeLock = new Object();
    // 我们从父级到子级保持弱引用，但从子级到父级保持强引用。
    private volatile Logger parent;    // 我们最近的父级。
    private ArrayList<LogManager.LoggerWeakRef> kids;   // 以我们为父级的日志记录器的弱引用
    private volatile Level levelObject;
    private volatile int levelValue;  // 当前有效的级别值
    private WeakReference<ClassLoader> callersClassLoaderRef;
    private final boolean isSystemLogger;

    /**
     * GLOBAL_LOGGER_NAME 是全局记录器的名称。
     *
     * @since 1.6
     */
    public static final String GLOBAL_LOGGER_NAME = "global";

    /**
     * 返回名为 Logger.GLOBAL_LOGGER_NAME 的全局记录器对象。
     *
     * @return 全局记录器对象
     * @since 1.7
     */
    public static final Logger getGlobal() {
        // 为了打破 LogManager
        // 和 Logger 静态初始化器之间的循环依赖导致的死锁，全局
        // 记录器是使用一个特殊的构造函数创建的，该构造函数不会
        // 初始化其日志管理器。
        //
        // 如果应用程序在任何记录器初始化之前调用 Logger.getGlobal()，
        // 那么 LogManager 类可能尚未初始化，因此
        // Logger.global.manager 将为 null。
        //
        // 为了完成全局记录器的初始化，我们
        // 将在这里调用 LogManager.getLogManager()。
        //
        // 为了防止竞争条件，我们还需要在这里无条件地调用
        // LogManager.getLogManager()。
        // 确实，我们不能依赖 global.manager 的观察值，
        // 因为 global.manager 将在 LogManager 初始化期间的某个时刻变为非 null。
        // 如果两个线程同时调用 getGlobal()，一个线程会看到 global.manager 为 null 并调用 LogManager.getLogManager()，
        // 但另一个线程可能在 global.manager 已经设置但 ensureLogManagerInitialized 尚未完成时进入...
        // 无条件调用 LogManager.getLogManager() 将解决这个问题。

        LogManager.getLogManager();

        // 现在全局 LogManager 应该已经初始化，
        // 并且全局记录器应该已经被添加到
        // 除非我们在 LogManager 子类的构造函数中调用它，该子类被安装为 LogManager，
        // 在这种情况下 global.manager 仍然为 null，global 将稍后被延迟初始化。

        return global;
    }

    /**
     * 提供了“global”记录器对象，以方便开发人员
     * 轻松使用日志记录包。对于认真使用日志记录包的开发人员（例如
     * 在产品中），应创建并使用自己的记录器对象，
     * 并使用适当的名称，以便可以按合适的记录器粒度控制日志记录。开发人员还需要保持对他们的记录器对象的强引用，以防止它们被垃圾回收。
     * <p>
     * @deprecated 该字段的初始化容易导致死锁。
     * 该字段必须由 Logger 类初始化，这可能导致与 LogManager 类初始化的死锁。
     * 在这种情况下，两个类初始化相互等待完成。
     * 获取全局记录器对象的首选方法是通过调用
     * <code>Logger.getGlobal()</code>。
     * 为了与旧的 JDK 版本兼容，其中
     * <code>Logger.getGlobal()</code> 不可用，可以使用调用
     * <code>Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)</code>
     * 或 <code>Logger.getLogger("global")</code>。
     */
    @Deprecated
    public static final Logger global = new Logger(GLOBAL_LOGGER_NAME);

                /**
     * 受保护的方法，用于构建命名子系统的日志记录器。
     * <p>
     * 日志记录器将最初配置为 Level 为 null
     * 并且 useParentHandlers 设置为 true。
     *
     * @param   name    日志记录器的名称。这应该是一个点分隔的名称，通常
     *                          基于子系统的包名或类名，例如 java.net
     *                          或 javax.swing。对于匿名日志记录器，它可以为 null。
     * @param   resourceBundleName  用于本地化此日志记录器消息的 ResourceBundle 的名称。如果
     *                          没有消息需要本地化，可以为 null。
     * @throws MissingResourceException 如果 resourceBundleName 非 null 且
     *             无法找到相应的资源。
     */
    protected Logger(String name, String resourceBundleName) {
        this(name, resourceBundleName, null, LogManager.getLogManager(), false);
    }

    Logger(String name, String resourceBundleName, Class<?> caller, LogManager manager, boolean isSystemLogger) {
        this.manager = manager;
        this.isSystemLogger = isSystemLogger;
        setupResourceInfo(resourceBundleName, caller);
        this.name = name;
        levelValue = Level.INFO.intValue();
    }

    private void setCallersClassLoaderRef(Class<?> caller) {
        ClassLoader callersClassLoader = ((caller != null)
                                         ? caller.getClassLoader()
                                         : null);
        if (callersClassLoader != null) {
            this.callersClassLoaderRef = new WeakReference<>(callersClassLoader);
        }
    }

    private ClassLoader getCallersClassLoader() {
        return (callersClassLoaderRef != null)
                ? callersClassLoaderRef.get()
                : null;
    }

    // 仅用于创建全局日志记录器的构造函数。
    // 需要它来打破 LogManager
    // 和 Logger 静态初始化器之间的循环依赖，从而避免死锁。
    private Logger(String name) {
        // manager 字段在此处未初始化。
        this.name = name;
        this.isSystemLogger = true;
        levelValue = Level.INFO.intValue();
    }

    // 当日志记录器实际上被添加到 LogManager 时，从 LoggerContext.addLocalLogger() 调用。
    void setLogManager(LogManager manager) {
        this.manager = manager;
    }

    private void checkPermission() throws SecurityException {
        if (!anonymous) {
            if (manager == null) {
                // 完成全局日志记录器的初始化。
                manager = LogManager.getLogManager();
            }
            manager.checkPermission();
        }
    }

    // 在所有 JDK 代码转换为调用 sun.util.logging.PlatformLogger
    // （参见 7054233）之前，我们需要确定 Logger.getLogger 是要添加
    // 系统日志记录器还是用户日志记录器。
    //
    // 作为临时解决方案，如果直接调用者的调用者加载器为
    // null，我们假设它是一个系统日志记录器，并将其添加到系统上下文中。
    // 这些系统日志记录器仅将资源包设置为给定的
    // 资源包名称（而不是默认的系统资源包）。
    private static class SystemLoggerHelper {
        static boolean disableCallerCheck = getBooleanProperty("sun.util.logging.disableCallerCheck");
        private static boolean getBooleanProperty(final String key) {
            String s = AccessController.doPrivileged(new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return System.getProperty(key);
                }
            });
            return Boolean.valueOf(s);
        }
    }

    private static Logger demandLogger(String name, String resourceBundleName, Class<?> caller) {
        LogManager manager = LogManager.getLogManager();
        SecurityManager sm = System.getSecurityManager();
        if (sm != null && !SystemLoggerHelper.disableCallerCheck) {
            if (caller.getClassLoader() == null) {
                return manager.demandSystemLogger(name, resourceBundleName);
            }
        }
        return manager.demandLogger(name, resourceBundleName, caller);
        // 如果日志记录器尚不存在，则最终会调用 new Logger(name, resourceBundleName, caller)
    }

    /**
     * 查找或创建一个命名子系统的日志记录器。如果已经创建了具有给定名称的日志记录器，则返回该日志记录器。否则
     * 创建一个新的日志记录器。
     * <p>
     * 如果创建了新的日志记录器，其日志级别将基于
     * LogManager 配置进行配置，并且它将配置为也将日志输出发送到其父级的 Handlers。它将
     * 在 LogManager 的全局命名空间中注册。
     * <p>
     * 注意：LogManager 可能仅保留对新创建的 Logger 的弱引用。重要的是要理解，如果程序的其他地方没有对
     * 名为 "MyLogger" 的 Logger 的强引用，那么先前创建的名为 "MyLogger" 的 Logger 可能在任何时候被垃圾回收。
     * 特别是，这意味着如下序列：
     * {@code getLogger("MyLogger").log(...)} 可能使用不同的名为 "MyLogger" 的 Logger 对象，如果程序中没有对
     * 名为 "MyLogger" 的 Logger 的强引用。
     *
     * @param   name            日志记录器的名称。这应该是一个点分隔的名称，通常
     *                          基于子系统的包名或类名，例如 java.net
     *                          或 javax.swing
     * @return 一个合适的 Logger
     * @throws NullPointerException 如果名称为 null。
     */

    // 这里不需要同步。所有添加新的 Logger 对象的同步都由 LogManager.addLogger() 处理。
    @CallerSensitive
    public static Logger getLogger(String name) {
        // 该方法故意不包装对 getLogger(name, resourceBundleName) 的调用。如果它是，则
        // 以下序列：
        //
        //     getLogger("Foo", "resourceBundleForFoo");
        //     getLogger("Foo");
        //
        // 在第二次调用时会抛出 IllegalArgumentException
        // 因为包装器会导致尝试将现有的 "resourceBundleForFoo" 替换为 null。
        return demandLogger(name, null, Reflection.getCallerClass());
    }

                /**
     * 查找或创建一个命名子系统的日志记录器。如果已经使用给定名称创建了日志记录器，则返回该日志记录器。否则
     * 创建一个新的日志记录器。
     * <p>
     * 如果创建了新的日志记录器，其日志级别将根据 LogManager 进行配置，并且它将被配置为也将日志
     * 输出发送到其父日志记录器的处理程序。它将在 LogManager 的全局命名空间中注册。
     * <p>
     * 注意：LogManager 可能仅保留对新创建的日志记录器的弱引用。重要的是要理解，如果程序中没有
     * 对日志记录器的强引用，那么先前创建的具有给定名称的日志记录器可能在任何时候被垃圾回收。特别是，
     * 这意味着如果程序中没有对名为 "MyLogger" 的日志记录器的强引用，那么像
     * {@code getLogger("MyLogger", ...).log(...)} 这样的连续调用可能会使用不同的名为 "MyLogger" 的
     * 日志记录器对象。
     * <p>
     * 如果具有给定名称的日志记录器已经存在并且尚未具有本地化资源包，则使用给定的资源包名称。如果具有
     * 给定名称的日志记录器已经存在并且具有不同的资源包名称，则抛出 IllegalArgumentException。
     * <p>
     * @param   name    日志记录器的名称。这应该是一个点分隔的名称，通常基于子系统的包名或类名
     *                          例如 java.net 或 javax.swing
     * @param   resourceBundleName  用于本地化此日志记录器消息的资源包名称。如果不需要本地化任何消息，
     *                          可以为 {@code null}
     * @return 一个合适的日志记录器
     * @throws MissingResourceException 如果 resourceBundleName 非空且找不到相应的资源。
     * @throws IllegalArgumentException 如果日志记录器已存在并使用不同的资源包名称；或者
     *             {@code resourceBundleName} 为 {@code null} 但命名的日志记录器已设置资源包。
     * @throws NullPointerException 如果名称为 null。
     */

    // 此处不需要同步。添加新日志记录器对象的所有同步由 LogManager.addLogger() 处理。
    @CallerSensitive
    public static Logger getLogger(String name, String resourceBundleName) {
        Class<?> callerClass = Reflection.getCallerClass();
        Logger result = demandLogger(name, resourceBundleName, callerClass);

        // setupResourceInfo() 可能会抛出 MissingResourceException 或 IllegalArgumentException。
        // 如果 demandLogger 上面找到了先前创建的日志记录器，我们必须在这里设置调用者的类加载器。例如，
        // 如果调用了 Logger.getLogger(name)，随后又调用了 Logger.getLogger(name, resourceBundleName)。
        // 在这种情况下，我们可能没有保存正确的类加载器，所以我们也需要在这里设置它。

        result.setupResourceInfo(resourceBundleName, callerClass);
        return result;
    }

    // 包私有
    // 向系统上下文中添加一个平台日志记录器。
    // 即 sun.util.logging.PlatformLogger.getLogger 的调用者
    static Logger getPlatformLogger(String name) {
        LogManager manager = LogManager.getLogManager();

        // 系统上下文中的所有日志记录器将默认使用系统日志记录器的资源包
        Logger result = manager.demandSystemLogger(name, SYSTEM_LOGGER_RB_NAME);
        return result;
    }

    /**
     * 创建一个匿名日志记录器。新创建的日志记录器不会在 LogManager 命名空间中注册。对日志记录器的更新
     * 将不会进行访问检查。
     * <p>
     * 此工厂方法主要用于从 applet 中使用。因为生成的日志记录器是匿名的，所以可以由创建类保持私有。
     * 这消除了常规安全检查的需要，从而允许不受信任的 applet 代码更新日志记录器的控制状态。例如，applet
     * 可以对匿名日志记录器执行 setLevel 或 addHandler。
     * <p>
     * 即使新的日志记录器是匿名的，它也被配置为具有根日志记录器 ("") 作为其父日志记录器。这意味着
     * 默认情况下它从根日志记录器继承其有效的级别和处理程序。通过
     * {@link #setParent(java.util.logging.Logger) setParent} 方法更改其父日志记录器
     * 仍然需要该方法指定的安全权限。
     * <p>
     *
     * @return 一个新创建的私有日志记录器
     */
    public static Logger getAnonymousLogger() {
        return getAnonymousLogger(null);
    }

    /**
     * 创建一个匿名日志记录器。新创建的日志记录器不会在 LogManager 命名空间中注册。对日志记录器的更新
     * 将不会进行访问检查。
     * <p>
     * 此工厂方法主要用于从 applet 中使用。因为生成的日志记录器是匿名的，所以可以由创建类保持私有。
     * 这消除了常规安全检查的需要，从而允许不受信任的 applet 代码更新日志记录器的控制状态。例如，applet
     * 可以对匿名日志记录器执行 setLevel 或 addHandler。
     * <p>
     * 即使新的日志记录器是匿名的，它也被配置为具有根日志记录器 ("") 作为其父日志记录器。这意味着
     * 默认情况下它从根日志记录器继承其有效的级别和处理程序。通过
     * {@link #setParent(java.util.logging.Logger) setParent} 方法更改其父日志记录器
     * 仍然需要该方法指定的安全权限。
     * <p>
     * @param   resourceBundleName  用于本地化此日志记录器消息的资源包名称。
     *          如果不需要本地化任何消息，可以为 null。
     * @return 一个新创建的私有日志记录器
     * @throws MissingResourceException 如果 resourceBundleName 非空且找不到相应的资源。
     */


                // 同步在这里不是必需的。所有添加新的匿名 Logger 对象的同步都由 doSetParent() 处理。
    // Synchronization is not required here. All synchronization for
    // adding a new anonymous Logger object is handled by doSetParent().
    @CallerSensitive
    public static Logger getAnonymousLogger(String resourceBundleName) {
        LogManager manager = LogManager.getLogManager();
        // 清理一些已被垃圾回收的 Logger
        // cleanup some Loggers that have been GC'ed
        manager.drainLoggerRefQueueBounded();
        Logger result = new Logger(null, resourceBundleName,
                                   Reflection.getCallerClass(), manager, false);
        result.anonymous = true;
        Logger root = manager.getLogger("");
        result.doSetParent(root);
        return result;
    }

    /**
     * 获取此 Logger 的本地化资源包。
     * 此方法将返回一个 {@code ResourceBundle}，该资源包要么通过 {@link
     * #setResourceBundle(java.util.ResourceBundle) setResourceBundle} 方法设置，要么
     * <a href="#ResourceBundleMapping">从通过 {@link
     * Logger#getLogger(java.lang.String, java.lang.String) getLogger} 工厂方法设置的资源包名称映射</a>
     * 到当前默认区域设置。
     * <br>注意，如果结果为 {@code null}，则 Logger 将使用从其父级继承的资源包或资源包名称。
     *
     * @return 本地化包（可能是 {@code null}）
     */
    public ResourceBundle getResourceBundle() {
        return findResourceBundle(getResourceBundleName(), true);
    }

    /**
     * 获取此 Logger 的本地化资源包名称。
     * 这是通过 {@link
     * #getLogger(java.lang.String, java.lang.String) getLogger} 工厂方法指定的名称，
     * 或者是通过 {@link
     * #setResourceBundle(java.util.ResourceBundle) setResourceBundle} 方法设置的
     * {@linkplain ResourceBundle#getBaseBundleName() 基础名称}。
     * <br>注意，如果结果为 {@code null}，则 Logger 将使用从其父级继承的资源包或资源包名称。
     *
     * @return 本地化包名称（可能是 {@code null}）
     */
    public String getResourceBundleName() {
        return loggerBundle.resourceBundleName;
    }

    /**
     * 为此 Logger 设置一个过滤器以控制输出。
     * <P>
     * 在通过初始“级别”检查后，Logger 将调用此过滤器以检查日志记录是否应真正发布。
     *
     * @param   newFilter  过滤器对象（可能是 null）
     * @throws  SecurityException 如果存在安全经理，此 Logger 不是匿名的，并且调用者
     *          没有 LoggingPermission("control") 权限。
     */
    public void setFilter(Filter newFilter) throws SecurityException {
        checkPermission();
        filter = newFilter;
    }

    /**
     * 获取此 Logger 的当前过滤器。
     *
     * @return  过滤器对象（可能是 null）
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * 记录一个 LogRecord。
     * <p>
     * 本类中的所有其他日志记录方法都通过此方法实际执行日志记录。子类可以
     * 重写此单个方法以捕获所有日志活动。
     *
     * @param record 要发布的 LogRecord
     */
    public void log(LogRecord record) {
        if (!isLoggable(record.getLevel())) {
            return;
        }
        Filter theFilter = filter;
        if (theFilter != null && !theFilter.isLoggable(record)) {
            return;
        }

        // 将 LogRecord 发布到我们所有的处理程序，然后发布到
        // 我们的父级的处理程序，一直向上到树的根部。

        Logger logger = this;
        while (logger != null) {
            final Handler[] loggerHandlers = isSystemLogger
                ? logger.accessCheckedHandlers()
                : logger.getHandlers();

            for (Handler handler : loggerHandlers) {
                handler.publish(record);
            }

            final boolean useParentHdls = isSystemLogger
                ? logger.useParentHandlers
                : logger.getUseParentHandlers();

            if (!useParentHdls) {
                break;
            }

            logger = isSystemLogger ? logger.parent : logger.getParent();
        }
    }

    // 日志记录的私有支持方法。
    // 我们填写日志记录器名称、资源包名称和
    // 资源包，然后调用 "void log(LogRecord)"。
    private void doLog(LogRecord lr) {
        lr.setLoggerName(name);
        final LoggerBundle lb = getEffectiveLoggerBundle();
        final ResourceBundle  bundle = lb.userBundle;
        final String ebname = lb.resourceBundleName;
        if (ebname != null && bundle != null) {
            lr.setResourceBundleName(ebname);
            lr.setResourceBundle(bundle);
        }
        log(lr);
    }


    //================================================================
    // 没有 className 和 methodName 的便捷方法的开始
    //================================================================

    /**
     * 记录一条没有参数的消息。
     * <p>
     * 如果 Logger 当前已启用给定的消息级别，则将给定的消息转发到所有
     * 注册的输出处理程序对象。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   msg     字符串消息（或消息目录中的键）
     */
    public void log(Level level, String msg) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        doLog(lr);
    }

    /**
     * 记录一条消息，该消息只有在日志级别允许实际记录时才会构建。
     * <p>
     * 如果 Logger 当前已启用给定的消息级别，则通过调用提供的
     * 供应商函数构建消息，并转发到所有注册的输出
     * 处理程序对象。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   msgSupplier   一个函数，调用时生成所需的日志消息
     * @since 1.8
     */
    public void log(Level level, Supplier<String> msgSupplier) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msgSupplier.get());
        doLog(lr);
    }

                /**
     * 记录一条消息，带有一个对象参数。
     * <p>
     * 如果记录器当前启用了给定的消息级别，则会创建相应的 LogRecord 并转发给所有已注册的输出处理程序。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   msg     字符串消息（或消息目录中的键）
     * @param   param1  消息的参数
     */
    public void log(Level level, String msg, Object param1) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        Object params[] = { param1 };
        lr.setParameters(params);
        doLog(lr);
    }

    /**
     * 记录一条消息，带有一个对象数组参数。
     * <p>
     * 如果记录器当前启用了给定的消息级别，则会创建相应的 LogRecord 并转发给所有已注册的输出处理程序。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   msg     字符串消息（或消息目录中的键）
     * @param   params  消息的参数数组
     */
    public void log(Level level, String msg, Object params[]) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setParameters(params);
        doLog(lr);
    }

    /**
     * 记录一条消息，带有相关的 Throwable 信息。
     * <p>
     * 如果记录器当前启用了给定的消息级别，则给定的参数将存储在 LogRecord 中，并转发给所有已注册的输出处理程序。
     * <p>
     * 注意，thrown 参数存储在 LogRecord 的 thrown 属性中，而不是 LogRecord 的 parameters 属性中。因此，它由输出格式化程序特别处理，不被视为 LogRecord 消息属性的格式化参数。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   msg     字符串消息（或消息目录中的键）
     * @param   thrown  与日志消息相关的 Throwable。
     */
    public void log(Level level, String msg, Throwable thrown) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setThrown(thrown);
        doLog(lr);
    }

    /**
     * 记录一条延迟构造的消息，带有相关的 Throwable 信息。
     * <p>
     * 如果记录器当前启用了给定的消息级别，则通过调用提供的供应商函数构造消息。消息和给定的 {@link Throwable} 将存储在 {@link LogRecord} 中，并转发给所有已注册的输出处理程序。
     * <p>
     * 注意，thrown 参数存储在 LogRecord 的 thrown 属性中，而不是 LogRecord 的 parameters 属性中。因此，它由输出格式化程序特别处理，不被视为 LogRecord 消息属性的格式化参数。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   thrown  与日志消息相关的 Throwable。
     * @param   msgSupplier   一个函数，调用时生成所需的日志消息
     * @since   1.8
     */
    public void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msgSupplier.get());
        lr.setThrown(thrown);
        doLog(lr);
    }

    //================================================================
    // Start of convenience methods WITH className and methodName
    //================================================================

    /**
     * 记录一条消息，指定源类和方法，不带参数。
     * <p>
     * 如果记录器当前启用了给定的消息级别，则给定的消息将转发给所有已注册的输出处理程序。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   sourceClass    发出日志请求的类名
     * @param   sourceMethod   发出日志请求的方法名
     * @param   msg     字符串消息（或消息目录中的键）
     */
    public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        doLog(lr);
    }

    /**
     * 记录一条延迟构造的消息，指定源类和方法，不带参数。
     * <p>
     * 如果记录器当前启用了给定的消息级别，则通过调用提供的供应商函数构造消息，并转发给所有已注册的输出处理程序。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   sourceClass    发出日志请求的类名
     * @param   sourceMethod   发出日志请求的方法名
     * @param   msgSupplier   一个函数，调用时生成所需的日志消息
     * @since   1.8
     */
    public void logp(Level level, String sourceClass, String sourceMethod,
                     Supplier<String> msgSupplier) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msgSupplier.get());
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        doLog(lr);
    }

    /**
     * 记录一条消息，指定源类和方法，带有一个对象参数。
     * <p>
     * 如果记录器当前启用了给定的消息级别，则会创建相应的 LogRecord 并转发给所有已注册的输出处理程序。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   sourceClass    发出日志请求的类名
     * @param   sourceMethod   发出日志请求的方法名
     * @param   msg      字符串消息（或消息目录中的键）
     * @param   param1    日志消息的参数。
     */
    public void logp(Level level, String sourceClass, String sourceMethod,
                                                String msg, Object param1) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        Object params[] = { param1 };
        lr.setParameters(params);
        doLog(lr);
    }

                /**
     * 记录一条消息，指定源类和方法，
     * 带有对象参数数组。
     * <p>
     * 如果记录器当前已启用给定的消息
     * 级别，则会创建相应的 LogRecord 并转发
     * 到所有注册的输出处理程序。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   sourceClass    发出日志请求的类名
     * @param   sourceMethod   发出日志请求的方法名
     * @param   msg     字符串消息（或消息目录中的键）
     * @param   params  消息的参数数组
     */
    public void logp(Level level, String sourceClass, String sourceMethod,
                                                String msg, Object params[]) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setParameters(params);
        doLog(lr);
    }

    /**
     * 记录一条消息，指定源类和方法，
     * 带有关联的 Throwable 信息。
     * <p>
     * 如果记录器当前已启用给定的消息
     * 级别，则给定的参数将存储在 LogRecord 中
     * 并转发到所有注册的输出处理程序。
     * <p>
     * 注意，thrown 参数存储在 LogRecord 的 thrown
     * 属性中，而不是 LogRecord 的参数属性中。因此，它
     * 由输出格式化程序特别处理，而不是作为
     * LogRecord 消息属性的格式化参数。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   sourceClass    发出日志请求的类名
     * @param   sourceMethod   发出日志请求的方法名
     * @param   msg     字符串消息（或消息目录中的键）
     * @param   thrown  与日志消息关联的 Throwable。
     */
    public void logp(Level level, String sourceClass, String sourceMethod,
                     String msg, Throwable thrown) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setThrown(thrown);
        doLog(lr);
    }

    /**
     * 记录一条延迟构建的消息，指定源类和方法，
     * 带有关联的 Throwable 信息。
     * <p>
     * 如果记录器当前已启用给定的消息级别，则通过调用提供的供应商函数来构建消息。然后将消息和给定的 {@link Throwable} 存储在 {@link
     * LogRecord} 中，并转发到所有注册的输出处理程序。
     * <p>
     * 注意，thrown 参数存储在 LogRecord 的 thrown
     * 属性中，而不是 LogRecord 的参数属性中。因此，它
     * 由输出格式化程序特别处理，而不是作为
     * LogRecord 消息属性的格式化参数。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   sourceClass    发出日志请求的类名
     * @param   sourceMethod   发出日志请求的方法名
     * @param   thrown  与日志消息关联的 Throwable。
     * @param   msgSupplier   一个函数，调用时生成所需的日志消息
     * @since   1.8
     */
    public void logp(Level level, String sourceClass, String sourceMethod,
                     Throwable thrown, Supplier<String> msgSupplier) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msgSupplier.get());
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setThrown(thrown);
        doLog(lr);
    }


    //=========================================================================
    // 从带有类名、方法名和资源包名的便捷方法开始。
    //=========================================================================

    // 用于“logrb”方法的私有支持方法。
    // 我们填写记录器名称、资源包名称和
    // 资源包，然后调用“void log(LogRecord)”。
    private void doLog(LogRecord lr, String rbname) {
        lr.setLoggerName(name);
        if (rbname != null) {
            lr.setResourceBundleName(rbname);
            lr.setResourceBundle(findResourceBundle(rbname, false));
        }
        log(lr);
    }

    // 用于“logrb”方法的私有支持方法。
    private void doLog(LogRecord lr, ResourceBundle rb) {
        lr.setLoggerName(name);
        if (rb != null) {
            lr.setResourceBundleName(rb.getBaseBundleName());
            lr.setResourceBundle(rb);
        }
        log(lr);
    }

    /**
     * 记录一条消息，指定源类、方法和资源包名
     * 无参数。
     * <p>
     * 如果记录器当前已启用给定的消息
     * 级别，则给定的消息将转发到所有
     * 注册的输出处理程序。
     * <p>
     * 消息字符串使用命名的资源包进行本地化。如果
     * 资源包名称为 null、空字符串或无效
     * 则消息字符串不会本地化。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   sourceClass    发出日志请求的类名
     * @param   sourceMethod   发出日志请求的方法名
     * @param   bundleName     用于本地化消息的资源包名，
     *                         可以为 null
     * @param   msg     字符串消息（或消息目录中的键）
     * @deprecated 使用 {@link #logrb(java.util.logging.Level, java.lang.String,
     * java.lang.String, java.util.ResourceBundle, java.lang.String,
     * java.lang.Object...)} 代替。
     */
    @Deprecated
    public void logrb(Level level, String sourceClass, String sourceMethod,
                                String bundleName, String msg) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        doLog(lr, bundleName);
    }

                /**
     * 记录一条消息，指定源类、方法和资源包名称，并带有一个对象参数。
     * <p>
     * 如果记录器当前已启用给定的消息级别，则会创建相应的 LogRecord 并转发
     * 到所有注册的输出处理程序。
     * <p>
     * 使用命名的资源包对 msg 字符串进行本地化。如果
     * 资源包名称为 null，或为空字符串或无效
     * 则 msg 字符串不会被本地化。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   sourceClass    发出日志请求的类名
     * @param   sourceMethod   发出日志请求的方法名
     * @param   bundleName     用于本地化 msg 的资源包名称，
     *                         可以为 null
     * @param   msg      字符串消息（或消息目录中的键）
     * @param   param1    日志消息的参数。
     * @deprecated 使用 {@link #logrb(java.util.logging.Level, java.lang.String,
     *   java.lang.String, java.util.ResourceBundle, java.lang.String,
     *   java.lang.Object...)} 代替
     */
    @Deprecated
    public void logrb(Level level, String sourceClass, String sourceMethod,
                                String bundleName, String msg, Object param1) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        Object params[] = { param1 };
        lr.setParameters(params);
        doLog(lr, bundleName);
    }

    /**
     * 记录一条消息，指定源类、方法和资源包名称，并带有一个对象数组参数。
     * <p>
     * 如果记录器当前已启用给定的消息级别，则会创建相应的 LogRecord 并转发
     * 到所有注册的输出处理程序。
     * <p>
     * 使用命名的资源包对 msg 字符串进行本地化。如果
     * 资源包名称为 null，或为空字符串或无效
     * 则 msg 字符串不会被本地化。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   sourceClass    发出日志请求的类名
     * @param   sourceMethod   发出日志请求的方法名
     * @param   bundleName     用于本地化 msg 的资源包名称，
     *                         可以为 null。
     * @param   msg     字符串消息（或消息目录中的键）
     * @param   params  消息的参数数组
     * @deprecated 使用 {@link #logrb(java.util.logging.Level, java.lang.String,
     *      java.lang.String, java.util.ResourceBundle, java.lang.String,
     *      java.lang.Object...)} 代替。
     */
    @Deprecated
    public void logrb(Level level, String sourceClass, String sourceMethod,
                                String bundleName, String msg, Object params[]) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setParameters(params);
        doLog(lr, bundleName);
    }

    /**
     * 记录一条消息，指定源类、方法和资源包，并带有一个可选的消息参数列表。
     * <p>
     * 如果记录器当前已启用给定的消息级别，则会创建相应的 LogRecord 并转发
     * 到所有注册的输出处理程序。
     * <p>
     * 使用给定的资源包对 {@code msg} 字符串进行本地化。
     * 如果资源包为 {@code null}，则 {@code msg} 字符串不会
     * 被本地化。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   sourceClass    发出日志请求的类名
     * @param   sourceMethod   发出日志请求的方法名
     * @param   bundle         用于本地化 {@code msg} 的资源包，
     *                         可以为 {@code null}。
     * @param   msg     字符串消息（或消息目录中的键）
     * @param   params  消息的参数（可选，可以没有）。
     * @since 1.8
     */
    public void logrb(Level level, String sourceClass, String sourceMethod,
                      ResourceBundle bundle, String msg, Object... params) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        if (params != null && params.length != 0) {
            lr.setParameters(params);
        }
        doLog(lr, bundle);
    }

    /**
     * 记录一条消息，指定源类、方法和资源包名称，并带有相关的 Throwable 信息。
     * <p>
     * 如果记录器当前已启用给定的消息级别，则给定的参数将存储在 LogRecord 中
     * 并转发到所有注册的输出处理程序。
     * <p>
     * 使用命名的资源包对 msg 字符串进行本地化。如果
     * 资源包名称为 null，或为空字符串或无效
     * 则 msg 字符串不会被本地化。
     * <p>
     * 注意，thrown 参数存储在 LogRecord 的 thrown 属性中，而不是 LogRecord 的参数属性中。因此，它
     * 由输出格式化程序特别处理，不会被视为 LogRecord 消息属性的格式化参数。
     * <p>
     * @param   level   消息级别标识符之一，例如 SEVERE
     * @param   sourceClass    发出日志请求的类名
     * @param   sourceMethod   发出日志请求的方法名
     * @param   bundleName     用于本地化 msg 的资源包名称，
     *                         可以为 null
     * @param   msg     字符串消息（或消息目录中的键）
     * @param   thrown  与日志消息关联的 Throwable。
     * @deprecated 使用 {@link #logrb(java.util.logging.Level, java.lang.String,
     *     java.lang.String, java.util.ResourceBundle, java.lang.String,
     *     java.lang.Throwable)} 代替。
     */
    @Deprecated
    public void logrb(Level level, String sourceClass, String sourceMethod,
                                        String bundleName, String msg, Throwable thrown) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setThrown(thrown);
        doLog(lr, bundleName);
    }


                /**
     * 记录一条消息，指定源类、方法和资源包，并附带关联的异常信息。
     * <p>
     * 如果日志记录器当前已启用给定的消息级别，则将给定的参数存储在LogRecord中，
     * 并转发给所有注册的输出处理器。
     * <p>
     * 使用给定的资源包对{@code msg}字符串进行本地化。
     * 如果资源包为{@code null}，则不进行本地化。
     * <p>
     * 注意，抛出的参数存储在LogRecord的thrown属性中，而不是LogRecord的parameters属性中。
     * 因此，它由输出格式化程序特别处理，而不是作为LogRecord消息属性的格式化参数处理。
     * <p>
     * @param   level   消息级别标识符之一，例如SEVERE
     * @param   sourceClass    发出日志请求的类的名称
     * @param   sourceMethod   发出日志请求的方法的名称
     * @param   bundle         用于本地化{@code msg}的资源包，
     *                         可以为{@code null}
     * @param   msg     字符串消息（或消息目录中的键）
     * @param   thrown  与日志消息关联的异常。
     * @since 1.8
     */
    public void logrb(Level level, String sourceClass, String sourceMethod,
                      ResourceBundle bundle, String msg, Throwable thrown) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setThrown(thrown);
        doLog(lr, bundle);
    }

    //======================================================================
    // 日志记录方法入口和返回的便捷方法的开始。
    //======================================================================

    /**
     * 记录方法入口。
     * <p>
     * 这是一个便捷方法，可用于记录方法的入口。记录一条消息为"ENTRY"、日志级别为
     * FINER，并带有给定的sourceMethod和sourceClass的LogRecord。
     * <p>
     * @param   sourceClass    发出日志请求的类的名称
     * @param   sourceMethod   正在进入的方法的名称
     */
    public void entering(String sourceClass, String sourceMethod) {
        logp(Level.FINER, sourceClass, sourceMethod, "ENTRY");
    }

    /**
     * 记录带有单个参数的方法入口。
     * <p>
     * 这是一个便捷方法，可用于记录方法的入口。记录一条消息为"ENTRY {0}"、日志级别为
     * FINER，并带有给定的sourceMethod、sourceClass和参数的LogRecord。
     * <p>
     * @param   sourceClass    发出日志请求的类的名称
     * @param   sourceMethod   正在进入的方法的名称
     * @param   param1         正在进入的方法的参数
     */
    public void entering(String sourceClass, String sourceMethod, Object param1) {
        logp(Level.FINER, sourceClass, sourceMethod, "ENTRY {0}", param1);
    }

    /**
     * 记录带有参数数组的方法入口。
     * <p>
     * 这是一个便捷方法，可用于记录方法的入口。记录一条消息为"ENTRY"（后面跟随每个参数数组条目的格式{N}指示符）、
     * 日志级别为FINER，并带有给定的sourceMethod、sourceClass和参数的LogRecord。
     * <p>
     * @param   sourceClass    发出日志请求的类的名称
     * @param   sourceMethod   正在进入的方法的名称
     * @param   params         正在进入的方法的参数数组
     */
    public void entering(String sourceClass, String sourceMethod, Object params[]) {
        String msg = "ENTRY";
        if (params == null ) {
           logp(Level.FINER, sourceClass, sourceMethod, msg);
           return;
        }
        if (!isLoggable(Level.FINER)) return;
        for (int i = 0; i < params.length; i++) {
            msg = msg + " {" + i + "}";
        }
        logp(Level.FINER, sourceClass, sourceMethod, msg, params);
    }

    /**
     * 记录方法返回。
     * <p>
     * 这是一个便捷方法，可用于记录从方法返回。记录一条消息为"RETURN"、日志级别为
     * FINER，并带有给定的sourceMethod和sourceClass的LogRecord。
     * <p>
     * @param   sourceClass    发出日志请求的类的名称
     * @param   sourceMethod   方法的名称
     */
    public void exiting(String sourceClass, String sourceMethod) {
        logp(Level.FINER, sourceClass, sourceMethod, "RETURN");
    }


    /**
     * 记录带有结果对象的方法返回。
     * <p>
     * 这是一个便捷方法，可用于记录从方法返回。记录一条消息为"RETURN {0}"、日志级别为
     * FINER，并带有给定的sourceMethod、sourceClass和结果对象的LogRecord。
     * <p>
     * @param   sourceClass    发出日志请求的类的名称
     * @param   sourceMethod   方法的名称
     * @param   result  正在返回的对象
     */
    public void exiting(String sourceClass, String sourceMethod, Object result) {
        logp(Level.FINER, sourceClass, sourceMethod, "RETURN {0}", result);
    }

    /**
     * 记录抛出异常。
     * <p>
     * 这是一个便捷方法，用于记录方法通过抛出异常终止。使用FINER级别进行记录。
     * <p>
     * 如果日志记录器当前已启用给定的消息级别，则将给定的参数存储在LogRecord中，
     * 并转发给所有注册的输出处理器。LogRecord的消息设置为"THROW"。
     * <p>
     * 注意，抛出的参数存储在LogRecord的thrown属性中，而不是LogRecord的parameters属性中。
     * 因此，它由输出格式化程序特别处理，而不是作为LogRecord消息属性的格式化参数处理。
     * <p>
     * @param   sourceClass    发出日志请求的类的名称
     * @param   sourceMethod  方法的名称。
     * @param   thrown  正在抛出的异常。
     */
    public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
        if (!isLoggable(Level.FINER)) {
            return;
        }
        LogRecord lr = new LogRecord(Level.FINER, "THROW");
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setThrown(thrown);
        doLog(lr);
    }

                //=======================================================================
    // 简单的便捷方法，使用级别名称作为方法名称的开始
    //=======================================================================

    /**
     * 记录一个 SEVERE 消息。
     * <p>
     * 如果日志记录器当前已启用 SEVERE 消息级别，则给定的消息将转发给所有已注册的输出处理程序对象。
     * <p>
     * @param   msg     字符串消息（或消息目录中的键）
     */
    public void severe(String msg) {
        log(Level.SEVERE, msg);
    }

    /**
     * 记录一个 WARNING 消息。
     * <p>
     * 如果日志记录器当前已启用 WARNING 消息级别，则给定的消息将转发给所有已注册的输出处理程序对象。
     * <p>
     * @param   msg     字符串消息（或消息目录中的键）
     */
    public void warning(String msg) {
        log(Level.WARNING, msg);
    }

    /**
     * 记录一个 INFO 消息。
     * <p>
     * 如果日志记录器当前已启用 INFO 消息级别，则给定的消息将转发给所有已注册的输出处理程序对象。
     * <p>
     * @param   msg     字符串消息（或消息目录中的键）
     */
    public void info(String msg) {
        log(Level.INFO, msg);
    }

    /**
     * 记录一个 CONFIG 消息。
     * <p>
     * 如果日志记录器当前已启用 CONFIG 消息级别，则给定的消息将转发给所有已注册的输出处理程序对象。
     * <p>
     * @param   msg     字符串消息（或消息目录中的键）
     */
    public void config(String msg) {
        log(Level.CONFIG, msg);
    }

    /**
     * 记录一个 FINE 消息。
     * <p>
     * 如果日志记录器当前已启用 FINE 消息级别，则给定的消息将转发给所有已注册的输出处理程序对象。
     * <p>
     * @param   msg     字符串消息（或消息目录中的键）
     */
    public void fine(String msg) {
        log(Level.FINE, msg);
    }

    /**
     * 记录一个 FINER 消息。
     * <p>
     * 如果日志记录器当前已启用 FINER 消息级别，则给定的消息将转发给所有已注册的输出处理程序对象。
     * <p>
     * @param   msg     字符串消息（或消息目录中的键）
     */
    public void finer(String msg) {
        log(Level.FINER, msg);
    }

    /**
     * 记录一个 FINEST 消息。
     * <p>
     * 如果日志记录器当前已启用 FINEST 消息级别，则给定的消息将转发给所有已注册的输出处理程序对象。
     * <p>
     * @param   msg     字符串消息（或消息目录中的键）
     */
    public void finest(String msg) {
        log(Level.FINEST, msg);
    }

    //=======================================================================
    // 简单的便捷方法，使用级别名称作为方法名称，并使用 Supplier<String>
    //=======================================================================

    /**
     * 记录一个 SEVERE 消息，该消息仅在日志级别允许实际记录消息时才构建。
     * <p>
     * 如果日志记录器当前已启用 SEVERE 消息级别，则通过调用提供的供应商函数构建消息，并转发给所有已注册的输出处理程序对象。
     * <p>
     * @param   msgSupplier   一个函数，调用时生成所需的日志消息
     * @since   1.8
     */
    public void severe(Supplier<String> msgSupplier) {
        log(Level.SEVERE, msgSupplier);
    }

    /**
     * 记录一个 WARNING 消息，该消息仅在日志级别允许实际记录消息时才构建。
     * <p>
     * 如果日志记录器当前已启用 WARNING 消息级别，则通过调用提供的供应商函数构建消息，并转发给所有已注册的输出处理程序对象。
     * <p>
     * @param   msgSupplier   一个函数，调用时生成所需的日志消息
     * @since   1.8
     */
    public void warning(Supplier<String> msgSupplier) {
        log(Level.WARNING, msgSupplier);
    }

    /**
     * 记录一个 INFO 消息，该消息仅在日志级别允许实际记录消息时才构建。
     * <p>
     * 如果日志记录器当前已启用 INFO 消息级别，则通过调用提供的供应商函数构建消息，并转发给所有已注册的输出处理程序对象。
     * <p>
     * @param   msgSupplier   一个函数，调用时生成所需的日志消息
     * @since   1.8
     */
    public void info(Supplier<String> msgSupplier) {
        log(Level.INFO, msgSupplier);
    }

    /**
     * 记录一个 CONFIG 消息，该消息仅在日志级别允许实际记录消息时才构建。
     * <p>
     * 如果日志记录器当前已启用 CONFIG 消息级别，则通过调用提供的供应商函数构建消息，并转发给所有已注册的输出处理程序对象。
     * <p>
     * @param   msgSupplier   一个函数，调用时生成所需的日志消息
     * @since   1.8
     */
    public void config(Supplier<String> msgSupplier) {
        log(Level.CONFIG, msgSupplier);
    }

    /**
     * 记录一个 FINE 消息，该消息仅在日志级别允许实际记录消息时才构建。
     * <p>
     * 如果日志记录器当前已启用 FINE 消息级别，则通过调用提供的供应商函数构建消息，并转发给所有已注册的输出处理程序对象。
     * <p>
     * @param   msgSupplier   一个函数，调用时生成所需的日志消息
     * @since   1.8
     */
    public void fine(Supplier<String> msgSupplier) {
        log(Level.FINE, msgSupplier);
    }

                /**
     * 记录一个 FINER 级别的消息，只有在日志级别设置为会实际记录该消息的情况下才会构造该消息。
     * <p>
     * 如果当前日志记录器启用了 FINER 级别的消息，则通过调用提供的供应函数构造消息，并转发给所有已注册的输出
     * 处理器对象。
     * <p>
     * @param   msgSupplier   一个函数，调用时生成所需的日志消息
     * @since   1.8
     */
    public void finer(Supplier<String> msgSupplier) {
        log(Level.FINER, msgSupplier);
    }

    /**
     * 记录一个 FINEST 级别的消息，只有在日志级别设置为会实际记录该消息的情况下才会构造该消息。
     * <p>
     * 如果当前日志记录器启用了 FINEST 级别的消息，则通过调用提供的供应函数构造消息，并转发给所有已注册的输出
     * 处理器对象。
     * <p>
     * @param   msgSupplier   一个函数，调用时生成所需的日志消息
     * @since   1.8
     */
    public void finest(Supplier<String> msgSupplier) {
        log(Level.FINEST, msgSupplier);
    }

    //================================================================
    // 日志记录便捷方法结束
    //================================================================

    /**
     * 设置日志级别，指定此记录器将记录的消息级别。低于此值的消息级别将被丢弃。级别值 Level.OFF
     * 可用于关闭日志记录。
     * <p>
     * 如果新的级别为 null，则表示此节点应从其最近的具有特定（非 null）级别值的祖先继承其级别。
     *
     * @param newLevel   日志级别的新值（可以为 null）
     * @throws  SecurityException 如果存在安全经理，此记录器不是匿名的，且调用者
     *          没有 LoggingPermission("control") 权限。
     */
    public void setLevel(Level newLevel) throws SecurityException {
        checkPermission();
        synchronized (treeLock) {
            levelObject = newLevel;
            updateEffectiveLevel();
        }
    }

    final boolean isLevelInitialized() {
        return levelObject != null;
    }

    /**
     * 获取为此记录器指定的日志级别。结果可能为 null，这意味着此记录器的有效级别将从其父记录器继承。
     *
     * @return  此记录器的级别
     */
    public Level getLevel() {
        return levelObject;
    }

    /**
     * 检查给定级别的消息是否会被此记录器实际记录。此检查基于记录器的有效级别，
     * 该级别可能从其父记录器继承。
     *
     * @param   level   一个消息日志级别
     * @return  如果给定的消息级别当前正在记录，则返回 true。
     */
    public boolean isLoggable(Level level) {
        if (level.intValue() < levelValue || levelValue == offValue) {
            return false;
        }
        return true;
    }

    /**
     * 获取此记录器的名称。
     * @return 记录器名称。对于匿名记录器，返回 null。
     */
    public String getName() {
        return name;
    }

    /**
     * 添加一个日志处理器以接收日志消息。
     * <p>
     * 默认情况下，记录器也会将其输出发送到其父记录器。通常，根记录器会配置一组处理器，
     * 实际上充当所有记录器的默认处理器。
     *
     * @param   handler 一个日志处理器
     * @throws  SecurityException 如果存在安全经理，此记录器不是匿名的，且调用者
     *          没有 LoggingPermission("control") 权限。
     */
    public void addHandler(Handler handler) throws SecurityException {
        // 检查处理器是否为 null
        handler.getClass();
        checkPermission();
        handlers.add(handler);
    }

    /**
     * 移除一个日志处理器。
     * <P>
     * 如果未找到给定的处理器或处理器为 null，则静默返回。
     *
     * @param   handler 一个日志处理器
     * @throws  SecurityException 如果存在安全经理，此记录器不是匿名的，且调用者
     *          没有 LoggingPermission("control") 权限。
     */
    public void removeHandler(Handler handler) throws SecurityException {
        checkPermission();
        if (handler == null) {
            return;
        }
        handlers.remove(handler);
    }

    /**
     * 获取与此记录器关联的处理器。
     * <p>
     * @return  所有已注册的处理器数组
     */
    public Handler[] getHandlers() {
        return accessCheckedHandlers();
    }

    // 该方法理想情况下应标记为 final - 但不幸的是
    // 它需要被 LogManager.RootLogger 覆盖
    Handler[] accessCheckedHandlers() {
        return handlers.toArray(emptyHandlers);
    }

    /**
     * 指定此记录器是否应将其输出发送到其父记录器。这意味着任何日志记录也将被写入父记录器的处理器，
     * 并可能递归地向上发送到命名空间的父记录器。
     *
     * @param useParentHandlers   如果输出应发送到记录器的父记录器，则为 true。
     * @throws  SecurityException 如果存在安全经理，此记录器不是匿名的，且调用者
     *          没有 LoggingPermission("control") 权限。
     */
    public void setUseParentHandlers(boolean useParentHandlers) {
        checkPermission();
        this.useParentHandlers = useParentHandlers;
    }

    /**
     * 发现此记录器是否将其输出发送到其父记录器。
     *
     * @return  如果输出应发送到记录器的父记录器，则为 true
     */
    public boolean getUseParentHandlers() {
        return useParentHandlers;
    }


                private static ResourceBundle findSystemResourceBundle(final Locale locale) {
        // 资源包位于受限制的包中
        return AccessController.doPrivileged(new PrivilegedAction<ResourceBundle>() {
            @Override
            public ResourceBundle run() {
                try {
                    return ResourceBundle.getBundle(SYSTEM_LOGGER_RB_NAME,
                                                    locale);
                } catch (MissingResourceException e) {
                    throw new InternalError(e.toString());
                }
            }
        });
    }

    /**
     * 私有工具方法，用于将资源包名称映射到实际的资源包，使用简单的单条目缓存。
     * 如果名称为 null，则返回 null。
     * 如果找不到资源包且没有合适的先前缓存值，也可能返回 null。
     *
     * @param name 要定位的 ResourceBundle
     * @param userCallersClassLoader 如果为 true，则使用调用者的 ClassLoader 进行搜索
     * @return 指定名称的 ResourceBundle 或如果未找到则返回 null
     */
    private synchronized ResourceBundle findResourceBundle(String name,
                                                           boolean useCallersClassLoader) {
        // 对于所有查找，我们首先检查线程上下文类加载器
        // 如果未设置，则使用系统类加载器。如果仍然找不到，则使用
        // callersClassLoaderRef（如果已设置且 useCallersClassLoader 为 true）。
        // 我们在创建具有非 null 资源包名称的日志记录器时最初设置
        // callersClassLoaderRef。

        // 对于 null 名称，返回 null 资源包。
        if (name == null) {
            return null;
        }

        Locale currentLocale = Locale.getDefault();
        final LoggerBundle lb = loggerBundle;

        // 通常我们应该在简单的单条目缓存中命中。
        if (lb.userBundle != null &&
                name.equals(lb.resourceBundleName)) {
            return lb.userBundle;
        } else if (catalog != null && currentLocale.equals(catalogLocale)
                && name.equals(catalogName)) {
            return catalog;
        }

        if (name.equals(SYSTEM_LOGGER_RB_NAME)) {
            catalog = findSystemResourceBundle(currentLocale);
            catalogName = name;
            catalogLocale = currentLocale;
            return catalog;
        }

        // 使用线程的上下文类加载器。如果没有，则使用
        // {@linkplain java.lang.ClassLoader#getSystemClassLoader() 系统类加载器}。
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        try {
            catalog = ResourceBundle.getBundle(name, currentLocale, cl);
            catalogName = name;
            catalogLocale = currentLocale;
            return catalog;
        } catch (MissingResourceException ex) {
            // 我们无法在默认类加载器中找到资源包。继续。
        }

        if (useCallersClassLoader) {
            // 尝试使用调用者的类加载器
            ClassLoader callersClassLoader = getCallersClassLoader();

            if (callersClassLoader == null || callersClassLoader == cl) {
                return null;
            }

            try {
                catalog = ResourceBundle.getBundle(name, currentLocale,
                                                   callersClassLoader);
                catalogName = name;
                catalogLocale = currentLocale;
                return catalog;
            } catch (MissingResourceException ex) {
                return null; // 没有找到
            }
        } else {
            return null;
        }
    }

    // 私有工具方法，用于初始化我们的单条目
    // 资源包名称缓存和调用者的类加载器
    // 注意：为了保持一致性，我们仔细检查
    // 在设置 resourceBundleName 字段之前，是否存在合适的 ResourceBundle。
    // 同步以防止设置字段时发生竞争。
    private synchronized void setupResourceInfo(String name,
                                                Class<?> callersClass) {
        final LoggerBundle lb = loggerBundle;
        if (lb.resourceBundleName != null) {
            // 此日志记录器已经有一个资源包

            if (lb.resourceBundleName.equals(name)) {
                // 名称匹配，因此无需进一步操作
                return;
            }

            // 一旦设置，不能更改资源包
            throw new IllegalArgumentException(
                lb.resourceBundleName + " != " + name);
        }

        if (name == null) {
            return;
        }

        setCallersClassLoaderRef(callersClass);
        if (isSystemLogger && getCallersClassLoader() != null) {
            checkPermission();
        }
        if (findResourceBundle(name, true) == null) {
            // 无法找到预期的 ResourceBundle。
            // 由于无法使用它找到资源包，取消设置调用者的类加载器
            this.callersClassLoaderRef = null;
            throw new MissingResourceException("Can't find " + name + " bundle",
                                                name, "");
        }

        // 如果 lb.userBundle 不为 null，我们不会到达这一行。
        assert lb.userBundle == null;
        loggerBundle = LoggerBundle.get(name, null);
    }

    /**
     * 为此日志记录器设置资源包。
     * 所有消息都将使用给定的资源包及其特定的
     * {@linkplain ResourceBundle#getLocale 语言环境} 进行记录。
     * @param bundle 此日志记录器应使用的资源包。
     * @throws NullPointerException 如果给定的包为 {@code null}。
     * @throws IllegalArgumentException 如果给定的包没有
     *         {@linkplain ResourceBundle#getBaseBundleName 基本名称}，
     *         或者此日志记录器已经设置了一个资源包但给定的包具有不同的基本名称。
     * @throws SecurityException 如果存在安全经理，
     *         此日志记录器不是匿名的，且调用者
     *         没有 LoggingPermission("control")。
     * @since 1.8
     */
    public void setResourceBundle(ResourceBundle bundle) {
        checkPermission();


                    // 如果 bundle 为 null，将抛出 NPE。
        final String baseName = bundle.getBaseBundleName();

        // bundle 必须有一个名称
        if (baseName == null || baseName.isEmpty()) {
            throw new IllegalArgumentException("resource bundle must have a name");
        }

        synchronized (this) {
            LoggerBundle lb = loggerBundle;
            final boolean canReplaceResourceBundle = lb.resourceBundleName == null
                    || lb.resourceBundleName.equals(baseName);

            if (!canReplaceResourceBundle) {
                throw new IllegalArgumentException("can't replace resource bundle");
            }


            loggerBundle = LoggerBundle.get(baseName, bundle);
        }
    }

    /**
     * 返回此 Logger 的父 Logger。
     * <p>
     * 此方法返回命名空间中最近的现有父 Logger。
     * 因此，如果一个 Logger 被称为 "a.b.c.d"，并且一个称为 "a.b" 的 Logger 已经被创建，
     * 但没有 "a.b.c" 的 Logger 存在，那么在 Logger "a.b.c.d" 上调用 getParent 将返回 Logger "a.b"。
     * <p>
     * 如果在命名空间的根 Logger 上调用此方法，结果将为 null。
     *
     * @return 最近的现有父 Logger
     */
    public Logger getParent() {
        // 注意：这以前是同步在 treeLock 上的。然而，这仅提供了内存语义，
        // 因为没有保证调用者会同步在 treeLock 上（实际上，外部调用者无法这样做）。
        // 因此，我们改为将 parent 设为 volatile。
        return parent;
    }

    /**
     * 设置此 Logger 的父 Logger。此方法由 LogManager 在命名空间更改时更新 Logger 时使用。
     * <p>
     * 不应在应用程序代码中调用此方法。
     * <p>
     * @param  parent   新的父 Logger
     * @throws  SecurityException  如果存在安全管理器并且调用者没有 LoggingPermission("control") 权限。
     */
    public void setParent(Logger parent) {
        if (parent == null) {
            throw new NullPointerException();
        }

        // 检查所有 Logger 的权限，包括匿名 Logger
        if (manager == null) {
            manager = LogManager.getLogManager();
        }
        manager.checkPermission();

        doSetParent(parent);
    }

    // 私有方法，用于将子 Logger 附加到父 Logger。
    private void doSetParent(Logger newParent) {

        // System.err.println("doSetParent \"" + getName() + "\" \""
        //                              + newParent.getName() + "\"");

        synchronized (treeLock) {

            // 从任何先前的父 Logger 中移除自己。
            LogManager.LoggerWeakRef ref = null;
            if (parent != null) {
                // assert parent.kids != null;
                for (Iterator<LogManager.LoggerWeakRef> iter = parent.kids.iterator(); iter.hasNext(); ) {
                    ref = iter.next();
                    Logger kid =  ref.get();
                    if (kid == this) {
                        // ref 用于下面完成重新父化
                        iter.remove();
                        break;
                    } else {
                        ref = null;
                    }
                }
                // 现在我们已经从父 Logger 的子列表中移除了自己。
            }

            // 设置新的父 Logger。
            parent = newParent;
            if (parent.kids == null) {
                parent.kids = new ArrayList<>(2);
            }
            if (ref == null) {
                // 我们没有先前的父 Logger
                ref = manager.new LoggerWeakRef(this);
            }
            ref.setParentRef(new WeakReference<>(parent));
            parent.kids.add(ref);

            // 由于重新父化，我们和我们的子 Logger 的有效级别可能已更改。
            updateEffectiveLevel();

        }
    }

    // 包级方法。
    // 从子列表中移除指定子 Logger 的弱引用。我们只能从 LoggerWeakRef.dispose() 调用。
    final void removeChildLogger(LogManager.LoggerWeakRef child) {
        synchronized (treeLock) {
            for (Iterator<LogManager.LoggerWeakRef> iter = kids.iterator(); iter.hasNext(); ) {
                LogManager.LoggerWeakRef ref = iter.next();
                if (ref == child) {
                    iter.remove();
                    return;
                }
            }
        }
    }

    // 重新计算此节点的有效级别，并递归地计算我们的子节点的有效级别。

    private void updateEffectiveLevel() {
        // assert Thread.holdsLock(treeLock);

        // 确定我们当前的有效级别。
        int newLevelValue;
        if (levelObject != null) {
            newLevelValue = levelObject.intValue();
        } else {
            if (parent != null) {
                newLevelValue = parent.levelValue;
            } else {
                // 这可能在初始化期间发生。
                newLevelValue = Level.INFO.intValue();
            }
        }

        // 如果我们的有效值没有改变，我们就完成了。
        if (levelValue == newLevelValue) {
            return;
        }

        levelValue = newLevelValue;

        // System.err.println("effective level: \"" + getName() + "\" := " + level);

        // 递归地更新我们每个子节点的级别。
        if (kids != null) {
            for (int i = 0; i < kids.size(); i++) {
                LogManager.LoggerWeakRef ref = kids.get(i);
                Logger kid =  ref.get();
                if (kid != null) {
                    kid.updateEffectiveLevel();
                }
            }
        }
    }


    // 私有方法，用于获取此 Logger 的可能继承的资源包和资源包名称。
    // 此方法从不返回 null。
    private LoggerBundle getEffectiveLoggerBundle() {
        final LoggerBundle lb = loggerBundle;
        if (lb.isSystemBundle()) {
            return SYSTEM_BUNDLE;
        }


                    // 首先处理这个日志记录器
        final ResourceBundle b = getResourceBundle();
        if (b != null && b == lb.userBundle) {
            return lb;
        } else if (b != null) {
            // 要么 lb.userBundle 为 null，要么 getResourceBundle() 被重写
            final String rbName = getResourceBundleName();
            return LoggerBundle.get(rbName, b);
        }

        // 在此日志记录器上未指定资源包，查找父级堆栈。
        Logger target = this.parent;
        while (target != null) {
            final LoggerBundle trb = target.loggerBundle;
            if (trb.isSystemBundle()) {
                return SYSTEM_BUNDLE;
            }
            if (trb.userBundle != null) {
                return trb;
            }
            final String rbName = isSystemLogger
                // 系统日志记录器的祖先预计也是系统日志记录器。
                // 如果不是，则忽略资源包名称。
                ? (target.isSystemLogger ? trb.resourceBundleName : null)
                : target.getResourceBundleName();
            if (rbName != null) {
                return LoggerBundle.get(rbName,
                        findResourceBundle(rbName, true));
            }
            target = isSystemLogger ? target.parent : target.getParent();
        }
        return NO_RESOURCE_BUNDLE;
    }

}
