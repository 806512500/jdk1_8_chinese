/*
 * 版权所有 (c) 1994, 2013, Oracle 和/或其关联公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款限制。
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

import java.io.*;
import java.lang.reflect.Executable;
import java.lang.annotation.Annotation;
import java.security.AccessControlContext;
import java.util.Properties;
import java.util.PropertyPermission;
import java.util.StringTokenizer;
import java.util.Map;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.AllPermission;
import java.nio.channels.Channel;
import java.nio.channels.spi.SelectorProvider;
import sun.nio.ch.Interruptible;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.security.util.SecurityConstants;
import sun.reflect.annotation.AnnotationType;

/**
 * {@code System} 类包含若干有用的类字段和方法。它不能被实例化。
 *
 * <p>{@code System} 类提供的功能包括标准输入、标准输出和错误输出流；
 * 访问外部定义的属性和环境变量；加载文件和库的方法；以及快速复制数组部分内容的实用方法。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public final class System {

    /* 通过静态初始化器注册本地方法。
     *
     * 虚拟机会调用 initializeSystemClass 方法来完成此类的初始化，与 clinit 分离。
     * 注意，要使用虚拟机设置的属性，请参见 initializeSystemClass 方法中描述的约束。
     */
    private static native void registerNatives();
    static {
        registerNatives();
    }

    /** 禁止任何人实例化此类 */
    private System() {
    }

    /**
     * “标准”输入流。此流已经打开并准备好提供输入数据。通常，此流对应于键盘输入
     * 或主机环境或用户指定的其他输入源。
     */
    public final static InputStream in = null;

    /**
     * “标准”输出流。此流已经打开并准备好接受输出数据。通常，此流对应于显示输出
     * 或主机环境或用户指定的其他输出目标。
     * <p>
     * 对于简单的独立 Java 应用程序，写入一行输出数据的典型方法是：
     * <blockquote><pre>
     *     System.out.println(data)
     * </pre></blockquote>
     * <p>
     * 请参见类 {@code PrintStream} 中的 {@code println} 方法。
     *
     * @see     java.io.PrintStream#println()
     * @see     java.io.PrintStream#println(boolean)
     * @see     java.io.PrintStream#println(char)
     * @see     java.io.PrintStream#println(char[])
     * @see     java.io.PrintStream#println(double)
     * @see     java.io.PrintStream#println(float)
     * @see     java.io.PrintStream#println(int)
     * @see     java.io.PrintStream#println(long)
     * @see     java.io.PrintStream#println(java.lang.Object)
     * @see     java.io.PrintStream#println(java.lang.String)
     */
    public final static PrintStream out = null;

    /**
     * “标准”错误输出流。此流已经打开并准备好接受输出数据。
     * <p>
     * 通常，此流对应于显示输出或主机环境或用户指定的其他输出目标。
     * 按照惯例，此输出流用于显示错误消息或其他需要立即引起用户注意的信息，
     * 即使主输出流（变量 {@code out} 的值）已被重定向到文件或其他通常不持续监控的目标。
     */
    public final static PrintStream err = null;

    /* 系统的安全管理器。
     */
    private static volatile SecurityManager security = null;

    /**
     * 重新分配“标准”输入流。
     *
     * <p>首先，如果存在安全管理器，则调用其 {@code checkPermission} 方法，
     * 使用 {@code RuntimePermission("setIO")} 权限，以检查是否允许重新分配标准输入流。
     * <p>
     *
     * @param in 新的标准输入流。
     *
     * @throws SecurityException
     *        如果存在安全管理器，并且其 {@code checkPermission} 方法
     *        不允许重新分配标准输入流。
     *
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     *
     * @since   JDK1.1
     */
    public static void setIn(InputStream in) {
        checkIO();
        setIn0(in);
    }

    /**
     * 重新分配“标准”输出流。
     *
     * <p>首先，如果存在安全管理器，则调用其 {@code checkPermission} 方法，
     * 使用 {@code RuntimePermission("setIO")} 权限，以检查是否允许重新分配标准输出流。
     *
     * @param out 新的标准输出流
     *
     * @throws SecurityException
     *        如果存在安全管理器，并且其 {@code checkPermission} 方法
     *        不允许重新分配标准输出流。
     *
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     *
     * @since   JDK1.1
     */
    public static void setOut(PrintStream out) {
        checkIO();
        setOut0(out);
    }

    /**
     * 重新分配“标准”错误输出流。
     *
     * <p>首先，如果存在安全管理器，则调用其 {@code checkPermission} 方法，
     * 使用 {@code RuntimePermission("setIO")} 权限，以检查是否允许重新分配标准错误输出流。
     *
     * @param err 新的标准错误输出流。
     *
     * @throws SecurityException
     *        如果存在安全管理器，并且其 {@code checkPermission} 方法
     *        不允许重新分配标准错误输出流。
     *
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     *
     * @since   JDK1.1
     */
    public static void setErr(PrintStream err) {
        checkIO();
        setErr0(err);
    }

    private static volatile Console cons = null;
    /**
     * 返回与当前 Java 虚拟机关联的唯一 {@link java.io.Console Console} 对象（如果有）。
     *
     * @return  系统控制台（如果有），否则返回 {@code null}。
     *
     * @since   1.6
     */
     public static Console console() {
         if (cons == null) {
             synchronized (System.class) {
                 cons = sun.misc.SharedSecrets.getJavaIOAccess().console();
             }
         }
         return cons;
     }

    /**
     * 返回从创建此 Java 虚拟机的实体继承的通道。
     *
     * <p>此方法返回通过调用系统范围默认 {@link java.nio.channels.spi.SelectorProvider} 对象的
     * {@link java.nio.channels.spi.SelectorProvider#inheritedChannel inheritedChannel} 方法获得的通道。
     *
     * <p>除了在 {@link java.nio.channels.spi.SelectorProvider#inheritedChannel inheritedChannel} 中描述的面向网络的通道外，
     * 此方法未来可能返回其他类型的通道。
     *
     * @return  继承的通道（如果有），否则返回 {@code null}。
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且其不允许访问该通道。
     *
     * @since 1.5
     */
    public static Channel inheritedChannel() throws IOException {
        return SelectorProvider.provider().inheritedChannel();
    }

    private static void checkIO() {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("setIO"));
        }
    }

    private static native void setIn0(InputStream in);
    private static native void setOut0(PrintStream out);
    private static native void setErr0(PrintStream err);

    /**
     * 设置系统安全管理器。
     *
     * <p>如果已经安装了安全管理器，此方法首先调用安全管理器的 {@code checkPermission} 方法，
     * 使用 {@code RuntimePermission("setSecurityManager")} 权限，以确保可以替换现有的安全管理器。
     * 这可能会抛出 {@code SecurityException}。
     *
     * <p>否则，参数将被设置为当前的安全管理器。如果参数为 {@code null} 且未建立安全管理器，
     * 则不执行任何操作，方法直接返回。
     *
     * @param      s   安全管理器。
     * @exception  SecurityException  如果已经设置了安全管理器，并且其 {@code checkPermission} 方法
     *             不允许替换。
     * @see #getSecurityManager
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     */
    public static
    void setSecurityManager(final SecurityManager s) {
        try {
            s.checkPackageAccess("java.lang");
        } catch (Exception e) {
            // 无操作
        }
        setSecurityManager0(s);
    }

    private static synchronized
    void setSecurityManager0(final SecurityManager s) {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            // 询问当前安装的安全管理器是否允许替换。
            sm.checkPermission(new RuntimePermission
                                     ("setSecurityManager"));
        }

        if ((s != null) && (s.getClass().getClassLoader() != null)) {
            // 新安全管理器类不在引导类路径上。
            // 在安装新安全管理器之前，先初始化策略，以防止在初始化策略时出现无限循环
            // （通常涉及访问某些安全和/或系统属性，这会调用已安装的安全管理器的 checkPermission 方法，
            // 如果栈上有一个非系统类（在此例中为新安全管理器类），会导致无限循环）。
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    s.getClass().getProtectionDomain().implies
                        (SecurityConstants.ALL_PERMISSION);
                    return null;
                }
            });
        }

        security = s;
    }

    /**
     * 获取系统安全接口。
     *
     * @return  如果当前应用程序已建立安全管理器，则返回该安全管理器；
     *          否则返回 {@code null}。
     * @see     #setSecurityManager
     */
    public static SecurityManager getSecurityManager() {
        return security;
    }

    /**
     * 返回以毫秒为单位的当前时间。请注意，
     * 虽然返回值的时间单位是毫秒，但值的粒度取决于底层操作系统，
     * 可能较大。例如，许多操作系统以十毫秒为单位测量时间。
     *
     * <p>有关“计算机时间”与协调世界时 (UTC) 之间可能出现的细微差异的讨论，
     * 请参见类 {@code Date} 的描述。
     *
     * @return  当前时间与 1970 年 1 月 1 日午夜 UTC 之间的差值，以毫秒为单位。
     * @see     java.util.Date
     */
    public static native long currentTimeMillis();

    /**
     * 返回运行中的 Java 虚拟机高分辨率时间源的当前值，以纳秒为单位。
     *
     * <p>此方法只能用于测量经过时间，与任何其他系统或挂钟时间概念无关。
     * 返回的值表示自某个固定但任意的<i>起点</i>时间以来的纳秒数（可能在未来，因此值可能为负）。
     * 同一个 Java 虚拟机实例中的所有调用使用相同的起点；其他虚拟机实例可能使用不同的起点。
     *
     * <p>此方法提供纳秒精度，但不一定提供纳秒分辨率（即值的变化频率）
     * ——仅保证分辨率至少与 {@link #currentTimeMillis()} 一样好。
     *
     * <p>连续调用之间的差值超过大约 292 年（2<sup>63</sup> 纳秒）时，
     * 由于数值溢出，将无法正确计算经过时间。
     *
     * <p>此方法返回的值仅在计算同一 Java 虚拟机实例内获得的两个值的差值时才有意义。
     *
     * <p>例如，测量某段代码执行时间：
     *  <pre> {@code
     * long startTime = System.nanoTime();
     * // ... 被测量的代码 ...
     * long estimatedTime = System.nanoTime() - startTime;}</pre>
     *
     * <p>比较两个 nanoTime 值：
     *  <pre> {@code
     * long t0 = System.nanoTime();
     * ...
     * long t1 = System.nanoTime();}</pre>
     *
     * 应使用 {@code t1 - t0 < 0}，而不是 {@code t1 < t0}，
     * 因为可能发生数值溢出。
     *
     * @return 运行中的 Java 虚拟机高分辨率时间源的当前值，以纳秒为单位
     * @since 1.5
     */
    public static native long nanoTime();

    /**
     * 从指定源数组的指定位置开始，将数组复制到目标数组的指定位置。
     * 从由 {@code src} 引用的源数组中复制一组数组元素到由 {@code dest} 引用的目标数组。
     * 复制的元素数量等于 {@code length} 参数。源数组中从 {@code srcPos} 到
     * {@code srcPos+length-1} 的位置的元素被复制到目标数组的
     * {@code destPos} 到 {@code destPos+length-1} 位置。
     * <p>
     * 如果 {@code src} 和 {@code dest} 参数引用同一个数组对象，
     * 则复制操作如同先将从 {@code srcPos} 到 {@code srcPos+length-1} 的元素
     * 复制到一个具有 {@code length} 个元素的临时数组，然后将临时数组的内容
     * 复制到目标数组的 {@code destPos} 到 {@code destPos+length-1} 位置。
     * <p>
     * 如果 {@code dest} 为 {@code null}，则抛出 {@code NullPointerException}。
     * <p>
     * 如果 {@code src} 为 {@code null}，则抛出 {@code NullPointerException}，
     * 且目标数组不被修改。
     * <p>
     * 否则，如果以下任一情况为真，则抛出 {@code ArrayStoreException}，
     * 且目标数组不被修改：
     * <ul>
     * <li>{@code src} 参数引用的对象不是数组。
     * <li>{@code dest} 参数引用的对象不是数组。
     * <li>{@code src} 参数和 {@code dest} 参数引用具有不同基本类型的数组。
     * <li>{@code src} 参数引用具有基本类型组件的数组，
     *     而 {@code dest} 参数引用具有引用类型组件的数组。
     * <li>{@code src} 参数引用具有引用类型组件的数组，
     *     而 {@code dest} 参数引用具有基本类型组件的数组。
     * </ul>
     * <p>
     * 否则，如果以下任一情况为真，则抛出 {@code IndexOutOfBoundsException}，
     * 且目标数组不被修改：
     * <ul>
     * <li>{@code srcPos} 参数为负。
     * <li>{@code destPos} 参数为负。
     * <li>{@code length} 参数为负。
     * <li>{@code srcPos+length} 大于源数组的长度 {@code src.length}。
     * <li>{@code destPos+length} 大于目标数组的长度 {@code dest.length}。
     * </ul>
     * <p>
     * 否则，如果源数组从 {@code srcPos} 到 {@code srcPos+length-1} 的任何实际组件
     * 无法通过赋值转换转换为目标数组的组件类型，则抛出 {@code ArrayStoreException}。
     * 在这种情况下，设 <b><i>k</i></b> 为小于 length 的最小非负整数，
     * 使得 {@code src[srcPos+}<i>k</i>{@code ]} 无法转换为目标数组的组件类型；
     * 当抛出异常时，源数组从 {@code srcPos} 到 {@code srcPos+}<i>k</i>{@code -1} 的组件
     * 已经复制到目标数组的 {@code destPos} 到 {@code destPos+}<i>k</i>{@code -1} 位置，
     * 且目标数组的其他位置不会被修改。
     * （由于已列出的限制，此段实际上仅适用于两个数组的组件类型均为引用类型的情况。）
     *
     * @param      src      源数组。
     * @param      srcPos   源数组中的起始位置。
     * @param      dest     目标数组。
     * @param      destPos  目标数组中的起始位置。
     * @param      length   要复制的数组元素数量。
     * @exception  IndexOutOfBoundsException  如果复制会导致访问数组边界外的数据。
     * @exception  ArrayStoreException  如果 {@code src} 数组中的元素
     *               由于类型不匹配无法存储到 {@code dest} 数组。
     * @exception  NullPointerException 如果 {@code src} 或 {@code dest} 为 {@code null}。
     */
    public static native void arraycopy(Object src,  int  srcPos,
                                        Object dest, int destPos,
                                        int length);

    /**
     * 为给定对象返回与默认方法 {@code hashCode()} 返回的相同哈希码，
     * 无论给定对象的类是否覆盖了 {@code hashCode()}。
     * 空引用的哈希码为零。
     *
     * @param x 要计算哈希码的对象
     * @return  哈希码
     * @since   JDK1.1
     */
    public static native int identityHashCode(Object x);

    /**
     * 系统属性。以下属性保证被定义：
     * <dl>
     * <dt>java.version         <dd>Java 版本号
     * <dt>java.vendor          <dd>Java 供应商特定字符串
     * <dt>java.vendor.url      <dd>Java 供应商 URL
     * <dt>java.home            <dd>Java 安装目录
     * <dt>java.class.version   <dd>Java 类版本号
     * <dt>java.class.path      <dd>Java 类路径
     * <dt>os.name              <dd>操作系统名称
     * <dt>os.arch              <dd>操作系统架构
     * <dt>os.version           <dd>操作系统版本
     * <dt>file.separator       <dd>文件分隔符（Unix 上为 "/"）
     * <dt>path.separator       <dd>路径分隔符（Unix 上为 ":"）
     * <dt>line.separator       <dd>行分隔符（Unix 上为 "\n"）
     * <dt>user.name            <dd>用户账户名称
     * <dt>user.home            <dd>用户主目录
     * <dt>user.dir             <dd>用户当前工作目录
     * </dl>
     */

    private static Properties props;
    private static native Properties initProperties(Properties props);

    /**
     * 确定当前系统属性。
     * <p>
     * 首先，如果存在安全管理器，则调用其 {@code checkPropertiesAccess} 方法，
     * 不带参数。这可能导致抛出安全异常。
     * <p>
     * 返回供 {@link #getProperty(String)} 方法使用的当前系统属性集，
     * 作为 {@code Properties} 对象。如果当前没有系统属性集，
     * 则首先创建并初始化一组系统属性。此系统属性集始终包括以下键的值：
     * <table summary="显示属性键及其关联值的表格">
     * <tr><th>键</th>
     *     <th>关联值的描述</th></tr>
     * <tr><td><code>java.version</code></td>
     *     <td>Java 运行时环境版本</td></tr>
     * <tr><td><code>java.vendor</code></td>
     *     <td>Java 运行时环境供应商</td></tr>
     * <tr><td><code>java.vendor.url</code></td>
     *     <td>Java 供应商 URL</td></tr>
     * <tr><td><code>java.home</code></td>
     *     <td>Java 安装目录</td></tr>
     * <tr><td><code>java.vm.specification.version</code></td>
     *     <td>Java 虚拟机规范版本</td></tr>
     * <tr><td><code>java.vm.specification.vendor</code></td>
     *     <td>Java 虚拟机规范供应商</td></tr>
     * <tr><td><code>java.vm.specification.name</code></td>
     *     <td>Java 虚拟机规范名称</td></tr>
     * <tr><td><code>java.vm.version</code></td>
     *     <td>Java 虚拟机实现版本</td></tr>
     * <tr><td><code>java.vm.vendor</code></td>
     *     <td>Java 虚拟机实现供应商</td></tr>
     * <tr><td><code>java.vm.name</code></td>
     *     <td>Java 虚拟机实现名称</td></tr>
     * <tr><td><code>java.specification.version</code></td>
     *     <td>Java 运行时环境规范版本</td></tr>
     * <tr><td><code>java.specification.vendor</code></td>
     *     <td>Java 运行时环境规范供应商</td></tr>
     * <tr><td><code>java.specification.name</code></td>
     *     <td>Java 运行时环境规范名称</td></tr>
     * <tr><td><code>java.class.version</code></td>
     *     <td>Java 类格式版本号</td></tr>
     * <tr><td><code>java.class.path</code></td>
     *     <td>Java 类路径</td></tr>
     * <tr><td><code>java.library.path</code></td>
     *     <td>加载库时搜索的路径列表</td></tr>
     * <tr><td><code>java.io.tmpdir</code></td>
     *     <td>默认临时文件路径</td></tr>
     * <tr><td><code>java.compiler</code></td>
     *     <td>要使用的 JIT 编译器名称</td></tr>
     * <tr><td><code>java.ext.dirs</code></td>
     *     <td>扩展目录或目录的路径
     *         <b>已弃用。</b> <i>此属性及其实现机制可能在未来版本中移除。</i> </td></tr>
     * <tr><td><code>os.name</code></td>
     *     <td>操作系统名称</td></tr>
     * <tr><td><code>os.arch</code></td>
     *     <td>操作系统架构</td></tr>
     * <tr><td><code>os.version</code></td>
     *     <td>操作系统版本</td></tr>
     * <tr><td><code>file.separator</code></td>
     *     <td>文件分隔符（UNIX 上为 "/"）</td></tr>
     * <tr><td><code>path.separator</code></td>
     *     <td>路径分隔符（UNIX 上为 ":"）</td></tr>
     * <tr><td><code>line.separator</code></td>
     *     <td>行分隔符（UNIX 上为 "\n"）</td></tr>
     * <tr><td><code>user.name</code></td>
     *     <td>用户账户名称</td></tr>
     * <tr><td><code>user.home</code></td>
     *     <td>用户主目录</td></tr>
     * <tr><td><code>user.dir</code></td>
     *     <td>用户当前工作目录</td></tr>
     * </table>
     * <p>
     * 系统属性值中的多个路径由平台的分隔符字符分隔。
     * <p>
     * 请注意，即使安全管理器不允许 {@code getProperties} 操作，
     * 它可能允许 {@link #getProperty(String)} 操作。
     *
     * @return     系统属性
     * @exception  SecurityException  如果存在安全管理器，
     *             并且其 {@code checkPropertiesAccess} 方法不允许访问系统属性。
     * @see        #setProperties
     * @see        java.lang.SecurityException
     * @see        java.lang.SecurityManager#checkPropertiesAccess()
     * @see        java.util.Properties
     */
    public static Properties getProperties() {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }

        return props;
    }

    /**
     * 返回系统相关的行分隔符字符串。它始终返回相同的初始值，
     * 即 {@linkplain #getProperty(String) 系统属性} {@code line.separator} 的初始值。
     *
     * <p>在 UNIX 系统上，返回 {@code "\n"}；在 Microsoft Windows 系统上，返回 {@code "\r\n"}。
     *
     * @return 系统相关的行分隔符字符串
     * @since 1.7
     */
    public static String lineSeparator() {
        return lineSeparator;
    }

    private static String lineSeparator;

    /**
     * 将系统属性设置为 {@code Properties} 参数。
     * <p>
     * 首先，如果存在安全管理器，则调用其 {@code checkPropertiesAccess} 方法，
     * 不带参数。这可能导致抛出安全异常。
     * <p>
     * 参数将成为供 {@link #getProperty(String)} 方法使用的当前系统属性集。
     * 如果参数为 {@code null}，则当前系统属性集将被清空。
     *
     * @param      props   新的系统属性。
     * @exception  SecurityException  如果存在安全管理器，
     *             并且其 {@code checkPropertiesAccess} 方法不允许访问系统属性。
     * @see        #getProperties
     * @see        java.util.Properties
     * @see        java.lang.SecurityException
     * @see        java.lang.SecurityManager#checkPropertiesAccess()
     */
    public static void setProperties(Properties props) {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }
        if (props == null) {
            props = new Properties();
            initProperties(props);
        }
        System.props = props;
    }

    /**
     * 获取由指定键指示的系统属性。
     * <p>
     * 首先，如果存在安全管理器，则调用其 {@code checkPropertyAccess} 方法，
     * 以键作为参数。这可能导致抛出 {@code SecurityException}。
     * <p>
     * 如果当前没有系统属性集，则首先以与 {@code getProperties} 方法相同的方式
     * 创建并初始化一组系统属性。
     *
     * @param      key   系统属性的名称。
     * @return     系统属性的字符串值，如果没有该键的属性，则返回 {@code null}。
     *
     * @exception  SecurityException  如果存在安全管理器，
     *             并且其 {@code checkPropertyAccess} 方法不允许访问指定的系统属性。
     * @exception  NullPointerException 如果 {@code key} 为 {@code null}。
     * @exception  IllegalArgumentException 如果 {@code key} 为空。
     * @see        #setProperty
     * @see        java.lang.SecurityException
     * @see        java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see        java.lang.System#getProperties()
     */
    public static String getProperty(String key) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPropertyAccess(key);
        }

        return props.getProperty(key);
    }

    /**
     * 获取由指定键指示的系统属性。
     * <p>
     * 首先，如果存在安全管理器，则调用其 {@code checkPropertyAccess} 方法，
     * 以 {@code key} 作为参数。
     * <p>
     * 如果当前没有系统属性集，则首先以与 {@code getProperties} 方法相同的方式
     * 创建并初始化一组系统属性。
     *
     * @param      key   系统属性的名称。
     * @param      def   默认值。
     * @return     系统属性的字符串值，如果没有该键的属性，则返回默认值。
     *
     * @exception  SecurityException  如果存在安全管理器，
     *             并且其 {@code checkPropertyAccess} 方法不允许访问指定的系统属性。
     * @exception  NullPointerException 如果 {@code key} 为 {@code null}。
     * @exception  IllegalArgumentException 如果 {@code key} 为空。
     * @see        #setProperty
     * @see        java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see        java.lang.System#getProperties()
     */
    public static String getProperty(String key, String def) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPropertyAccess(key);
        }

        return props.getProperty(key, def);
    }

    /**
     * 设置由指定键指示的系统属性。
     * <p>
     * 首先，如果存在安全管理器，则调用其 {@code SecurityManager.checkPermission} 方法，
     * 使用 {@code PropertyPermission(key, "write")} 权限。这可能导致抛出 {@code SecurityException}。
     * 如果未抛出异常，则将指定属性设置为给定值。
     * <p>
     *
     * @param      key   系统属性的名称。
     * @param      value 系统属性的值。
     * @return     系统属性的前一个值，如果没有，则返回 {@code null}。
     *
     * @exception  SecurityException  如果存在安全管理器，
     *             并且其 {@code checkPermission} 方法不允许设置指定的属性。
     * @exception  NullPointerException 如果 {@code key} 或 {@code value} 为 {@code null}。
     * @exception  IllegalArgumentException 如果 {@code key} 为空。
     * @see        #getProperty
     * @see        java.lang.System#getProperty(java.lang.String)
     * @see        java.lang.System#getProperty(java.lang.String, java.lang.String)
     * @see        java.util.PropertyPermission
     * @see        SecurityManager#checkPermission
     * @since      1.2
     */
    public static String setProperty(String key, String value) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new PropertyPermission(key,
                SecurityConstants.PROPERTY_WRITE_ACTION));
        }

        return (String) props.setProperty(key, value);
    }

    /**
     * 移除由指定键指示的系统属性。
     * <p>
     * 首先，如果存在安全管理器，则调用其 {@code SecurityManager.checkPermission} 方法，
     * 使用 {@code PropertyPermission(key, "write")} 权限。这可能导致抛出 {@code SecurityException}。
     * 如果未抛出异常，则移除指定属性。
     * <p>
     *
     * @param      key   要移除的系统属性的名称。
     * @return     系统属性的前一个字符串值，如果没有该键的属性，则返回 {@code null}。
     *
     * @exception  SecurityException  如果存在安全管理器，
     *             并且其 {@code checkPropertyAccess} 方法不允许访问指定的系统属性。
     * @exception  NullPointerException 如果 {@code key} 为 {@code null}。
     * @exception  IllegalArgumentException 如果 {@code key} 为空。
     * @see        #getProperty
     * @see        #setProperty
     * @see        java.util.Properties
     * @see        java.lang.SecurityException
     * @see        java.lang.SecurityManager#checkPropertiesAccess()
     * @since 1.5
     */
    public static String clearProperty(String key) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new PropertyPermission(key, "write"));
        }

        return (String) props.remove(key);
    }

    private static void checkKey(String key) {
        if (key == null) {
            throw new NullPointerException("键不能为空");
        }
        if (key.equals("")) {
            throw new IllegalArgumentException("键不能为空字符串");
        }
    }

    /**
     * 获取指定环境变量的值。环境变量是系统相关的外部命名值。
     *
     * <p>如果存在安全管理器，则调用其 {@link SecurityManager#checkPermission checkPermission} 方法，
     * 使用 {@code RuntimePermission("getenv."+name)} 权限。这可能导致抛出 {@link SecurityException}。
     * 如果未抛出异常，则返回变量 {@code name} 的值。
     *
     * <p><a name="EnvironmentVSSystemProperties"><i>系统属性</i>和<i>环境变量</i></a>
     * 都是名称与值之间的概念性映射。两者都可以用来向 Java 进程传递用户定义的信息。
     * 环境变量具有更全局的影响，因为它们对定义它们的进程的所有后代进程可见，
     * 而不只是直接的 Java 子进程。在不同操作系统上，它们的语义可能略有不同，
     * 例如大小写不敏感。因此，环境变量更容易产生意外的副作用。
     * 尽可能使用系统属性是最佳选择。当需要全局影响或外部系统接口需要环境变量
     * （如 {@code PATH}）时，应使用环境变量。
     *
     * <p>在 UNIX 系统上，{@code name} 的大小写通常是重要的，
     * 而在 Microsoft Windows 系统上通常不重要。例如，表达式
     * {@code System.getenv("FOO").equals(System.getenv("foo"))}
     * 在 Microsoft Windows 上很可能是 {@code true}。
     *
     * @param  name 环境变量的名称
     * @return 变量的字符串值，如果系统环境中未定义该变量，则返回 {@code null}
     * @throws NullPointerException 如果 {@code name} 为 {@code null}
     * @throws SecurityException
     *         如果存在安全管理器，并且其 {@link SecurityManager#checkPermission checkPermission}
     *         方法不允许访问环境变量 {@code name}
     * @see    #getenv()
     * @see    ProcessBuilder#environment()
     */
    public static String getenv(String name) {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getenv."+name));
        }

        return ProcessEnvironment.getenv(name);
    }

    /**
     * 返回当前系统环境的不可修改字符串映射视图。
     * 环境是从父进程传递到子进程的系统相关名称到值的映射。
     *
     * <p>如果系统不支持环境变量，则返回一个空映射。
     *
     * <p>返回的映射永远不会包含空键或空值。
     * 尝试查询空键或空值的存在将抛出 {@link NullPointerException}。
     * 尝试查询非 {@link String} 类型的键或值将抛出 {@link ClassCastException}。
     *
     * <p>返回的映射及其集合视图可能不遵守 {@link Object#equals} 和
     * {@link Object#hashCode} 方法的通用约定。
     *
     * <p>返回的映射在所有平台上通常是大小写敏感的。
     *
     * <p>如果存在安全管理器，则调用其 {@link SecurityManager#checkPermission checkPermission} 方法，
     * 使用 {@code RuntimePermission("getenv.*")} 权限。这可能导致抛出 {@link SecurityException}。
     *
     * <p>当向 Java 子进程传递信息时，
     * <a href=#EnvironmentVSSystemProperties>系统属性</a> 通常优于环境变量。
     *
     * @return 环境作为变量名称到值的映射
     * @throws SecurityException
     *         如果存在安全管理器，并且其 {@link SecurityManager#checkPermission checkPermission}
     *         方法不允许访问进程环境
     * @see    #getenv(String)
     * @see    ProcessBuilder#environment()
     * @since  1.5
     */
    public static java.util.Map<String,String> getenv() {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getenv.*"));
        }

        return ProcessEnvironment.getenv();
    }

    /**
     * 终止当前运行的 Java 虚拟机。参数作为状态码；
     * 按照惯例，非零状态码表示异常终止。
     * <p>
     * 此方法调用类 {@code Runtime} 的 {@code exit} 方法。此方法永远不会正常返回。
     * <p>
     * 调用 {@code System.exit(n)} 等效于调用：
     * <blockquote><pre>
     * Runtime.getRuntime().exit(n)
     * </pre></blockquote>
     *
     * @param      status   退出状态。
     * @throws  SecurityException
     *        如果存在安全管理器，并且其 {@code checkExit} 方法
     *        不允许以指定状态退出。
     * @see        java.lang.Runtime#exit(int)
     */
    public static void exit(int status) {
        Runtime.getRuntime().exit(status);
    }

    /**
     * 运行垃圾回收器。
     * <p>
     * 调用 {@code gc} 方法建议 Java 虚拟机努力回收未使用的对象，
     * 以使它们当前占用的内存可供快速重用。
     * 当控制从方法调用返回时，Java 虚拟机已尽力回收所有废弃对象的空间。
     * <p>
     * 调用 {@code System.gc()} 等效于调用：
     * <blockquote><pre>
     * Runtime.getRuntime().gc()
     * </pre></blockquote>
     *
     * @see     java.lang.Runtime#gc()
     */
    public static void gc() {
        Runtime.getRuntime().gc();
    }

    /**
     * 运行待定终结的对象上的终结方法。
     * <p>
     * 调用此方法建议 Java 虚拟机努力运行已被发现废弃但尚未运行
     * {@code finalize} 方法的对象的 {@code finalize} 方法。
     * 当控制从方法调用返回时，Java 虚拟机已尽力完成所有未完成的终结操作。
     * <p>
     * 调用 {@code System.runFinalization()} 等效于调用：
     * <blockquote><pre>
     * Runtime.getRuntime().runFinalization()
     * </pre></blockquote>
     *
     * @see     java.lang.Runtime#runFinalization()
     */
    public static void runFinalization() {
        Runtime.getRuntime().runFinalization();
    }

    /**
     * 启用或禁用退出时的终结操作；这样做指定在 Java 运行时退出之前，
     * 运行所有尚未自动调用的终结器的对象的终结器。
     * 默认情况下，退出时的终结是禁用的。
     *
     * <p>如果存在安全管理器，则首先调用其 {@code checkExit} 方法，
     * 以 0 作为参数，以确保允许退出。这可能导致抛出 {@code SecurityException}。
     *
     * @deprecated  此方法本质上不安全。可能导致在其他线程并发操作这些对象时，
     *      对活动对象调用终结器，导致不稳定行为或死锁。
     * @param value 表示启用或禁用终结
     * @throws  SecurityException
     *        如果存在安全管理器，并且其 {@code checkExit} 方法
     *        不允许退出。
     *
     * @see     java.lang.Runtime#exit(int)
     * @see     java.lang.Runtime#gc()
     * @see     java.lang.SecurityManager#checkExit(int)
     * @since   JDK1.1
     */
    @Deprecated
    public static void runFinalizersOnExit(boolean value) {
        Runtime.runFinalizersOnExit(value);
    }

    /**
     * 加载由 filename 参数指定的本地库。filename 参数必须是绝对路径名称。
     *
     * 如果 filename 参数在去除任何平台特定库前缀、路径和文件扩展名后，
     * 表示一个名为 L 的库，并且一个名为 L 的本地库与虚拟机静态链接，
     * 则调用该库导出的 JNI_OnLoad_L 函数，而不是尝试加载动态库。
     * 文件系统中不必存在与参数匹配的文件名。
     * 有关更多细节，请参见 JNI 规范。
     *
     * 否则，filename 参数以实现相关的方式映射到本地库映像。
     *
     * <p>
     * 调用 {@code System.load(name)} 等效于调用：
     * <blockquote><pre>
     * Runtime.getRuntime().load(name)
     * </pre></blockquote>
     *
     * @param      filename   要加载的文件。
     * @exception  SecurityException  如果存在安全管理器，
     *             并且其 {@code checkLink} 方法不允许加载指定的动态库
     * @exception  UnsatisfiedLinkError  如果 filename 不是绝对路径名，
     *             本地库未与虚拟机静态链接，或主机系统无法将库映射到本地库映像。
     * @exception  NullPointerException 如果 {@code filename} 为 {@code null}
     * @see        java.lang.Runtime#load(java.lang.String)
     * @see        java.lang.SecurityManager#checkLink(java.lang.String)
     */
    @CallerSensitive
    public static void load(String filename) {
        Runtime.getRuntime().load0(Reflection.getCallerClass(), filename);
    }

    /**
     * 加载由 {@code libname} 参数指定的本地库。{@code libname} 参数不得包含任何平台特定的前缀、
     * 文件扩展名或路径。如果一个名为 {@code libname} 的本地库与虚拟机静态链接，
     * 则调用该库导出的 JNI_OnLoad_{@code libname} 函数。
     * 有关更多细节，请参见 JNI 规范。
     *
     * 否则，libname 参数从系统库位置加载，并以实现相关的方式映射到本地库映像。
     * <p>
     * 调用 {@code System.loadLibrary(name)} 等效于调用：
     * <blockquote><pre>
     * Runtime.getRuntime().loadLibrary(name)
     * </pre></blockquote>
     *
     * @param      libname   库的名称。
     * @exception  SecurityException  如果存在安全管理器，
     *             并且其 {@code checkLink} 方法不允许加载指定的动态库
     * @exception  UnsatisfiedLinkError 如果 libname 参数包含文件路径，
     *             本地库未与虚拟机静态链接，或主机系统无法将库映射到本地库映像。
     * @exception  NullPointerException 如果 {@code libname} 为 {@code null}
     * @see        java.lang.Runtime#loadLibrary(java.lang.String)
     * @see        java.lang.SecurityManager#checkLink(java.lang.String)
     */
    @CallerSensitive
    public static void loadLibrary(String libname) {
        Runtime.getRuntime().loadLibrary0(Reflection.getCallerClass(), libname);
    }

    /**
     * 将库名称映射为表示本地库的平台特定字符串。
     *
     * @param      libname 库的名称。
     * @return     平台相关的本地库名称。
     * @exception  NullPointerException 如果 {@code libname} 为 {@code null}
     * @see        java.lang.System#loadLibrary(java.lang.String)
     * @see        java.lang.ClassLoader#findLibrary(java.lang.String)
     * @since      1.2
     */
    public static native String mapLibraryName(String libname);

    /**
     * 基于编码创建 stdout/err 的 PrintStream。
     */
    private static PrintStream newPrintStream(FileOutputStream fos, String enc) {
       if (enc != null) {
            try {
                return new PrintStream(new BufferedOutputStream(fos, 128), true, enc);
            } catch (UnsupportedEncodingException uee) {}
        }
        return new PrintStream(new BufferedOutputStream(fos, 128), true);
    }

    /**
     * 初始化系统类。在线程初始化后调用。
     */
    private static void initializeSystemClass() {

        // 虚拟机可能调用 JNU_NewStringPlatform() 来设置那些对编码敏感的属性
        // （user.home、user.name、boot.class.path 等）在“props”初始化期间，
        // 此时可能需要通过 System.getProperty() 访问已初始化的相关系统编码属性
        // （已放入“props”）。因此，确保“props”在初始化的最开始可用，
        // 并且所有系统属性直接放入其中。
        props = new Properties();
        initProperties(props);  // 由虚拟机初始化

        // 某些系统配置可能由虚拟机选项控制，例如最大直接内存量和
        // 用于支持自动装箱的对象标识语义的整数缓存大小。
        // 通常，库会从虚拟机设置的属性中获取这些值。
        // 如果属性仅用于内部实现，则应从系统属性中移除这些属性。
        //
        // 参见 java.lang.Integer.IntegerCache 和
        // sun.misc.VM.saveAndRemoveProperties 方法的示例。
        //
        // 保存系统属性对象的私有副本，仅供内部实现访问。
        // 移除某些不打算公开访问的系统属性。
        sun.misc.VM.saveAndRemoveProperties(props);

        lineSeparator = props.getProperty("line.separator");
        sun.misc.Version.init();

        FileInputStream fdIn = new FileInputStream(FileDescriptor.in);
        FileOutputStream fdOut = new FileOutputStream(FileDescriptor.out);
        FileOutputStream fdErr = new FileOutputStream(FileDescriptor.err);
        setIn0(new BufferedInputStream(fdIn));
        setOut0(newPrintStream(fdOut, props.getProperty("sun.stdout.encoding")));
        setErr0(newPrintStream(fdErr, props.getProperty("sun.stderr.encoding")));

        // 现在加载 zip 库，以防止 java.util.zip.ZipFile 稍后尝试使用自身加载此库。
        loadLibrary("zip");

        // 为 HUP、TERM 和 INT（如果可用）设置 Java 信号处理程序。
        Terminator.setup();

        // 初始化任何需要为类库设置的杂项操作系统设置。
        // 目前在除 Windows 外的所有平台上此操作为空操作，
        // 在 Windows 上会在使用 java.io 类之前设置进程范围的错误模式。
        sun.misc.VM.initializeOSEnvironment();

        // 主线程未以与其他线程相同的方式添加到其线程组；
        // 我们必须在此自行完成。
        Thread current = Thread.currentThread();
        current.getThreadGroup().add(current);

        // 注册共享秘密
        setJavaLangAccess();

        // 初始化期间调用的子系统可以调用 sun.misc.VM.isBooted()，
        // 以避免执行应等到应用程序类加载器设置完成的事情。
        // 重要：确保这是最后一个初始化操作！
        sun.misc.VM.booted();
    }

    private static void setJavaLangAccess() {
        // 允许 java.lang 外的特权类
        sun.misc.SharedSecrets.setJavaLangAccess(new sun.misc.JavaLangAccess(){
            public sun.reflect.ConstantPool getConstantPool(Class<?> klass) {
                return klass.getConstantPool();
            }
            public boolean casAnnotationType(Class<?> klass, AnnotationType oldType, AnnotationType newType) {
                return klass.casAnnotationType(oldType, newType);
            }
            public AnnotationType getAnnotationType(Class<?> klass) {
                return klass.getAnnotationType();
            }
            public Map<Class<? extends Annotation>, Annotation> getDeclaredAnnotationMap(Class<?> klass) {
                return klass.getDeclaredAnnotationMap();
            }
            public byte[] getRawClassAnnotations(Class<?> klass) {
                return klass.getRawAnnotations();
            }
            public byte[] getRawClassTypeAnnotations(Class<?> klass) {
                return klass.getRawTypeAnnotations();
            }
            public byte[] getRawExecutableTypeAnnotations(Executable executable) {
                return Class.getExecutableTypeAnnotationBytes(executable);
            }
            public <E extends Enum<E>>
                    E[] getEnumConstantsShared(Class<E> klass) {
                return klass.getEnumConstantsShared();
            }
            public void blockedOn(Thread t, Interruptible b) {
                t.blockedOn(b);
            }
            public void registerShutdownHook(int slot, boolean registerShutdownInProgress, Runnable hook) {
                Shutdown.add(slot, registerShutdownInProgress, hook);
            }
            public int getStackTraceDepth(Throwable t) {
                return t.getStackTraceDepth();
            }
            public StackTraceElement getStackTraceElement(Throwable t, int i) {
                return t.getStackTraceElement(i);
            }
            public String newStringUnsafe(char[] chars) {
                return new String(chars, true);
            }
            public Thread newThreadWithAcc(Runnable target, AccessControlContext acc) {
                return new Thread(target, acc);
            }
            public void invokeFinalize(Object o) throws Throwable {
                o.finalize();
            }
        });
    }
}