
/*
 * Copyright (c) 1994, 2022, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.util.StaticProperty;

/**
 * <code>System</code> 类包含几个有用的类字段和方法。它不能被实例化。
 *
 * <p>System 类提供的设施包括标准输入、标准输出和错误输出流；
 * 访问外部定义的属性和环境变量；加载文件和库的手段；以及快速复制数组部分的实用方法。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public final class System {

    /* 通过静态初始化器注册本地方法。
     *
     * 虚拟机将调用 initializeSystemClass 方法来完成此类的初始化，与类初始化器 clinit 分离。
     * 注意，要使用虚拟机设置的属性，请参阅 initializeSystemClass 方法中的约束。
     */
    private static native void registerNatives();
    static {
        registerNatives();
    }

    /** 不允许任何人实例化此类 */
    private System() {
    }

    /**
     * 标准输入流。此流已经打开并准备好提供输入数据。通常此流
     * 对应于键盘输入或由主机环境或用户指定的其他输入源。
     */
    public final static InputStream in = null;

    /**
     * 标准输出流。此流已经打开并准备好接受输出数据。通常此流
     * 对应于显示输出或由主机环境或用户指定的其他输出目标。
     * <p>
     * 对于简单的独立 Java 应用程序，典型的写入一行输出数据的方法是：
     * <blockquote><pre>
     *     System.out.println(data)
     * </pre></blockquote>
     * <p>
     * 请参阅类 <code>PrintStream</code> 中的 <code>println</code> 方法。
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
     * 标准错误输出流。此流已经打开并准备好接受输出数据。
     * <p>
     * 通常此流对应于显示输出或由主机环境或用户指定的其他输出目标。按照惯例，此输出流用于显示错误消息
     * 或其他需要立即引起用户注意的信息，即使主要输出流（变量 <code>out</code> 的值）已被重定向到文件或其他
     * 通常不连续监控的目的地。
     */
    public final static PrintStream err = null;

    /* 系统的安全管理器。
     */
    private static volatile SecurityManager security = null;

    /**
     * 重新分配标准输入流。
     *
     * <p>首先，如果有安全管理器，其 <code>checkPermission</code>
     * 方法将被调用，使用 <code>RuntimePermission("setIO")</code> 权限
     * 来检查是否允许重新分配标准输入流。
     * <p>
     *
     * @param in 新的标准输入流。
     *
     * @throws SecurityException
     *        如果存在安全管理器且其
     *        <code>checkPermission</code> 方法不允许
     *        重新分配标准输入流。
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
     * 重新分配标准输出流。
     *
     * <p>首先，如果有安全管理器，其 <code>checkPermission</code>
     * 方法将被调用，使用 <code>RuntimePermission("setIO")</code> 权限
     * 来检查是否允许重新分配标准输出流。
     *
     * @param out 新的标准输出流
     *
     * @throws SecurityException
     *        如果存在安全管理器且其
     *        <code>checkPermission</code> 方法不允许
     *        重新分配标准输出流。
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
     * 重新分配标准错误输出流。
     *
     * <p>首先，如果有安全管理器，其 <code>checkPermission</code>
     * 方法将被调用，使用 <code>RuntimePermission("setIO")</code> 权限
     * 来检查是否允许重新分配标准错误输出流。
     *
     * @param err 新的标准错误输出流。
     *
     * @throws SecurityException
     *        如果存在安全管理器且其
     *        <code>checkPermission</code> 方法不允许
     *        重新分配标准错误输出流。
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
     * 返回与当前 Java 虚拟机关联的唯一 {@link java.io.Console Console} 对象，如果有的话。
     *
     * @return  系统控制台，如果有的话，否则返回 <tt>null</tt>。
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
     * 返回创建此 Java 虚拟机的实体继承的通道。
     *
     * <p>此方法返回通过调用系统范围默认的
     * {@link java.nio.channels.spi.SelectorProvider} 对象的
     * {@link java.nio.channels.spi.SelectorProvider#inheritedChannel
     * inheritedChannel} 方法获得的通道。 </p>
     *
     * <p>除了 {@link java.nio.channels.spi.SelectorProvider#inheritedChannel
     * inheritedChannel} 中描述的网络通道外，此方法将来可能返回其他类型的通道。
     *
     * @return  继承的通道，如果有的话，否则返回 <tt>null</tt>。
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  SecurityException
     *          如果存在安全管理器且不允许访问通道。
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
     * 设置系统安全。
     *
     * <p>如果已经安装了安全管理器，此方法首先调用安全管理器的 <code>checkPermission</code> 方法
     * 使用 <code>RuntimePermission("setSecurityManager")</code>
     * 权限来确保可以替换现有的安全管理器。
     * 这可能导致抛出 <code>SecurityException</code>。
     *
     * <p>否则，将参数设置为当前的安全管理器。如果参数为 <code>null</code> 且尚未建立安全管理器，
     * 则不采取任何行动且方法直接返回。
     *
     * @param      s   安全管理器。
     * @exception  SecurityException  如果安全管理器已经设置且其 <code>checkPermission</code> 方法
     *             不允许替换现有安全管理器。
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
            // 询问当前安装的安全管理器是否可以替换它。
            sm.checkPermission(new RuntimePermission
                                     ("setSecurityManager"));
        }

        if ((s != null) && (s.getClass().getClassLoader() != null)) {
            // 新的安全管理器类不在引导类路径上。
            // 在安装新的安全管理器之前，使策略初始化，以防止在尝试初始化策略时出现无限循环
            // （通常涉及访问一些安全和/或系统属性，这反过来又会调用已安装的安全管理器的 checkPermission 方法
            // 如果堆栈上有非系统类（在这种情况下：新的安全管理器类）则会导致无限循环）。
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
     * @return  如果当前应用程序已经建立了安全管理器，则返回该安全管理器；
     *          否则，返回 <code>null</code>。
     * @see     #setSecurityManager
     */
    public static SecurityManager getSecurityManager() {
        return security;
    }

    /**
     * 返回当前时间（以毫秒为单位）。注意，虽然返回值的单位是毫秒，
     * 但其精度取决于底层操作系统，可能更大。例如，许多
     * 操作系统以数十毫秒为单位测量时间。
     *
     * <p>有关“计算机时间”与协调世界时（UTC）之间可能存在的微小差异的讨论，
     * 请参阅类 <code>Date</code> 的描述。
     *
     * @return  当前时间与 1970 年 1 月 1 日 00:00:00 UTC 之间的差值（以毫秒为单位）。
     * @see     java.util.Date
     */
    public static native long currentTimeMillis();

    /**
     * 返回正在运行的 Java 虚拟机的高分辨率时间源的当前值（以纳秒为单位）。
     *
     * <p>此方法只能用于测量经过的时间，与任何其他系统或时钟时间的概念无关。
     * 返回的值表示自某个固定但任意的 <i>起点</i> 以来的纳秒数（可能是未来的时间，因此值可能是负数）。
     * 同一 Java 虚拟机实例中的所有此方法的调用都使用相同的起点；其他虚拟机实例可能使用不同的起点。
     *
     * <p>此方法提供纳秒精度，但不一定提供纳秒分辨率（即值变化的频率）
     * — 除保证分辨率至少与 {@link #currentTimeMillis()} 一样好外，不作任何保证。
     *
     * <p>如果两个连续调用之间的差值超过大约 292 年（2<sup>63</sup> 纳秒），则由于数值溢出，
     * 无法正确计算经过的时间。
     *
     * <p>只有在同一 Java 虚拟机实例中计算两个此类值之间的差值时，这些值才有意义。
     *
     * <p>例如，测量某段代码的执行时间：
     *  <pre> {@code
     * long startTime = System.nanoTime();
     * // ... 被测量的代码 ...
     * long estimatedTime = System.nanoTime() - startTime;}</pre>
     *
     * <p>比较两个 nanoTime 值
     *  <pre> {@code
     * long t0 = System.nanoTime();
     * ...
     * long t1 = System.nanoTime();}</pre>
     *
     * 应使用 {@code t1 - t0 < 0}，而不是 {@code t1 < t0}，
     * 因为存在数值溢出的可能性。
     *
     * @return 正在运行的 Java 虚拟机的高分辨率时间源的当前值（以纳秒为单位）
     * @since 1.5
     */
    public static native long nanoTime();


                /**
     * 从指定的源数组的指定位置开始，复制数组到目标数组的指定位置。
     * 源数组引用的 <code>src</code> 中的数组组件子序列被复制到目标数组
     * 引用的 <code>dest</code> 中。复制的组件数量等于 <code>length</code> 参数。
     * 源数组中 <code>srcPos</code> 到 <code>srcPos+length-1</code> 位置的组件
     * 分别复制到目标数组中 <code>destPos</code> 到 <code>destPos+length-1</code> 位置。
     * <p>
     * 如果 <code>src</code> 和 <code>dest</code> 参数引用的是同一个数组对象，
     * 则复制操作将如同首先将 <code>srcPos</code> 到 <code>srcPos+length-1</code> 位置的
     * 组件复制到一个具有 <code>length</code> 个组件的临时数组，然后将临时数组的内容复制到
     * 目标数组中 <code>destPos</code> 到 <code>destPos+length-1</code> 位置。
     * <p>
     * 如果 <code>dest</code> 为 <code>null</code>，则抛出 <code>NullPointerException</code>。
     * <p>
     * 如果 <code>src</code> 为 <code>null</code>，则抛出 <code>NullPointerException</code>，
     * 且目标数组不会被修改。
     * <p>
     * 否则，如果以下任何条件为真，将抛出 <code>ArrayStoreException</code>，且目标数组不会被修改：
     * <ul>
     * <li><code>src</code> 参数引用的不是数组对象。
     * <li><code>dest</code> 参数引用的不是数组对象。
     * <li><code>src</code> 参数和 <code>dest</code> 参数引用的数组的组件类型是不同的基本类型。
     * <li><code>src</code> 参数引用的数组具有基本类型组件，而 <code>dest</code> 参数引用的数组具有引用类型组件。
     * <li><code>src</code> 参数引用的数组具有引用类型组件，而 <code>dest</code> 参数引用的数组具有基本类型组件。
     * </ul>
     * <p>
     * 否则，如果以下任何条件为真，将抛出 <code>IndexOutOfBoundsException</code>，
     * 且目标数组不会被修改：
     * <ul>
     * <li><code>srcPos</code> 参数为负。
     * <li><code>destPos</code> 参数为负。
     * <li><code>length</code> 参数为负。
     * <li><code>srcPos+length</code> 大于 <code>src.length</code>，即源数组的长度。
     * <li><code>destPos+length</code> 大于 <code>dest.length</code>，即目标数组的长度。
     * </ul>
     * <p>
     * 否则，如果源数组从位置 <code>srcPos</code> 到 <code>srcPos+length-1</code> 的任何实际组件
     * 不能通过赋值转换转换为目标数组的组件类型，则抛出 <code>ArrayStoreException</code>。
     * 在这种情况下，设 <b><i>k</i></b> 是小于长度的最小非负整数，使得
     * <code>src[srcPos+</code><i>k</i><code>]</code> 不能转换为目标数组的组件类型；
     * 当抛出异常时，源数组从位置 <code>srcPos</code> 到 <code>srcPos+</code><i>k</i><code>-1</code>
     * 的组件已经复制到目标数组从位置 <code>destPos</code> 到 <code>destPos+</code><i>k</I><code>-1</code>
     * 的位置，且目标数组的其他位置未被修改。
     * （由于已经列出的限制，此段实际上仅适用于两个数组的组件类型都是引用类型的情况。）
     *
     * @param      src      源数组。
     * @param      srcPos   源数组的起始位置。
     * @param      dest     目标数组。
     * @param      destPos  目标数据的起始位置。
     * @param      length   要复制的数组元素数量。
     * @exception  IndexOutOfBoundsException  如果复制会导致访问数组边界之外的数据。
     * @exception  ArrayStoreException  如果 <code>src</code> 数组中的元素由于类型不匹配而不能存储到 <code>dest</code> 数组中。
     * @exception  NullPointerException 如果 <code>src</code> 或 <code>dest</code> 为 <code>null</code>。
     */
    public static native void arraycopy(Object src,  int  srcPos,
                                        Object dest, int destPos,
                                        int length);

    /**
     * 返回给定对象的哈希码，就像默认的 <code>hashCode()</code> 方法返回的哈希码一样，
     * 无论给定对象的类是否覆盖了 <code>hashCode()</code> 方法。
     * 对于 <code>null</code> 引用，其哈希码为零。
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
     * <dt>user.dir             <dd>用户的当前工作目录
     * </dl>
     */

    private static Properties props;
    private static native Properties initProperties(Properties props);

    /**
     * 确定当前系统属性。
     * <p>
     * 首先，如果有安全经理，其 <code>checkPropertiesAccess</code> 方法将被调用，不带任何参数。
     * 这可能导致安全异常。
     * <p>
     * 返回一个 <code>Properties</code> 对象，其中包含当前系统属性，供 <code>getProperty(String)</code> 方法使用。
     * 如果没有当前的系统属性集，则首先创建并初始化一个系统属性集。
     * 这个系统属性集总是包含以下键的值：
     * <table summary="显示属性键及其关联值">
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
     * <tr><td><code>java.specification.maintenance.version</code></td>
     *     <td>Java 运行时环境规范维护版本，可解释为正整数
     *     <em>(可选，见下文)</em></td></tr>
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
     *     <td>扩展目录的路径
     *         <b>已弃用。</b> <i>此属性及其实现机制可能在未来的版本中被移除。</i> </td></tr>
     * <tr><td><code>os.name</code></td>
     *     <td>操作系统名称</td></tr>
     * <tr><td><code>os.arch</code></td>
     *     <td>操作系统架构</td></tr>
     * <tr><td><code>os.version</code></td>
     *     <td>操作系统版本</td></tr>
     * <tr><td><code>file.separator</code></td>
     *     <td>文件分隔符（Unix 上为 "/"）</td></tr>
     * <tr><td><code>path.separator</code></td>
     *     <td>路径分隔符（Unix 上为 ":"）</td></tr>
     * <tr><td><code>line.separator</code></td>
     *     <td>行分隔符（Unix 上为 "\n"）</td></tr>
     * <tr><td><code>user.name</code></td>
     *     <td>用户的账户名称</td></tr>
     * <tr><td><code>user.home</code></td>
     *     <td>用户的主目录</td></tr>
     * <tr><td><code>user.dir</code></td>
     *     <td>用户的当前工作目录</td></tr>
     * </table>
     * <p>
     * 如果此运行时在构造时实现的规范已经历了 <a
     * href="https://jcp.org/en/procedures/jcp2#3.6.4">维护发布</a>，
     * 则定义 <code>java.specification.maintenance.version</code> 属性。
     * 当定义时，其值标识该维护发布。要表示第一次维护发布，此属性将具有值 {@code "1"}，
     * 要表示第二次维护发布，此属性将具有值 {@code "2"}，依此类推。
     * <p>
     * 系统属性值中的多个路径由平台的路径分隔符字符分隔。
     * <p>
     * 注意，即使安全经理不允许 <code>getProperties</code> 操作，
     * 它也可能允许 <code>getProperty(String)</code> 操作。
     *
     * @return     系统属性
     * @exception  SecurityException  如果存在安全经理且其
     *             <code>checkPropertiesAccess</code> 方法不允许访问系统属性。
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
     * 返回系统依赖的行分隔符字符串。它总是返回相同的值——
     * {@linkplain #getProperty(String) 系统属性} {@code line.separator} 的初始值。
     *
     * <p>在 UNIX 系统上，它返回 {@code "\n"}；在 Microsoft
     * Windows 系统上，它返回 {@code "\r\n"}。
     *
     * @return 系统依赖的行分隔符字符串
     * @since 1.7
     */
    public static String lineSeparator() {
        return lineSeparator;
    }

    private static String lineSeparator;

    /**
     * 将系统属性设置为 <code>Properties</code> 参数。
     * <p>
     * 首先，如果有安全经理，其 <code>checkPropertiesAccess</code> 方法将被调用，不带任何参数。
     * 这可能导致安全异常。
     * <p>
     * 参数将成为 <code>getProperty(String)</code> 方法使用的当前系统属性集。
     * 如果参数为 <code>null</code>，则当前的系统属性集将被忘记。
     *
     * @param      props   新的系统属性。
     * @exception  SecurityException  如果存在安全经理且其
     *             <code>checkPropertiesAccess</code> 方法不允许访问系统属性。
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
     * 首先，如果有安全经理，其 <code>checkPropertyAccess</code> 方法将被调用，参数为键。
     * 这可能导致安全异常。
     * <p>
     * 如果没有当前的系统属性集，则首先创建并初始化一个系统属性集，方式与 <code>getProperties</code> 方法相同。
     *
     * @param      key   系统属性的名称。
     * @return     系统属性的字符串值，
     *             如果没有该键的属性，则返回 <code>null</code>。
     *
     * @exception  SecurityException  如果存在安全经理且其
     *             <code>checkPropertyAccess</code> 方法不允许访问指定的系统属性。
     * @exception  NullPointerException 如果 <code>key</code> 为 <code>null</code>。
     * @exception  IllegalArgumentException 如果 <code>key</code> 为空。
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
     * 首先，如果有安全管理器，其
     * <code>checkPropertyAccess</code> 方法将使用
     * <code>key</code> 作为参数调用。
     * <p>
     * 如果当前没有系统属性集，将首先创建并初始化一个系统属性集，方式与
     * <code>getProperties</code> 方法相同。
     *
     * @param      key   系统属性的名称。
     * @param      def   默认值。
     * @return     系统属性的字符串值，
     *             如果没有该键的属性，则返回默认值。
     *
     * @exception  SecurityException  如果存在安全管理器且其
     *             <code>checkPropertyAccess</code> 方法不允许
     *             访问指定的系统属性。
     * @exception  NullPointerException 如果 <code>key</code> 为
     *             <code>null</code>。
     * @exception  IllegalArgumentException 如果 <code>key</code> 为空。
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
     * 首先，如果存在安全管理器，其
     * <code>SecurityManager.checkPermission</code> 方法
     * 将使用 <code>PropertyPermission(key, "write")</code>
     * 权限调用。这可能导致抛出 SecurityException。
     * 如果没有抛出异常，则将指定属性设置为给定值。
     * <p>
     *
     * @param      key   系统属性的名称。
     * @param      value 系统属性的值。
     * @return     系统属性的先前值，
     *             如果没有该键的属性，则返回 <code>null</code>。
     *
     * @exception  SecurityException  如果存在安全管理器且其
     *             <code>checkPermission</code> 方法不允许
     *             设置指定的属性。
     * @exception  NullPointerException 如果 <code>key</code> 或
     *             <code>value</code> 为 <code>null</code>。
     * @exception  IllegalArgumentException 如果 <code>key</code> 为空。
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
     * 删除由指定键指示的系统属性。
     * <p>
     * 首先，如果存在安全管理器，其
     * <code>SecurityManager.checkPermission</code> 方法
     * 将使用 <code>PropertyPermission(key, "write")</code>
     * 权限调用。这可能导致抛出 SecurityException。
     * 如果没有抛出异常，则删除指定属性。
     * <p>
     *
     * @param      key   要删除的系统属性的名称。
     * @return     系统属性的先前值，
     *             如果没有该键的属性，则返回 <code>null</code>。
     *
     * @exception  SecurityException  如果存在安全管理器且其
     *             <code>checkPropertyAccess</code> 方法不允许
     *              访问指定的系统属性。
     * @exception  NullPointerException 如果 <code>key</code> 为
     *             <code>null</code>。
     * @exception  IllegalArgumentException 如果 <code>key</code> 为空。
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
            throw new NullPointerException("key can't be null");
        }
        if (key.equals("")) {
            throw new IllegalArgumentException("key can't be empty");
        }
    }

    /**
     * 获取指定环境变量的值。环境变量是系统依赖的外部命名值。
     *
     * <p>如果存在安全管理器，其
     * {@link SecurityManager#checkPermission checkPermission}
     * 方法将使用 <code>{@link RuntimePermission}("getenv."+name)</code>
     * 权限调用。这可能导致抛出 {@link SecurityException}。
     * 如果没有抛出异常，则返回变量 <code>name</code> 的值。
     *
     * <p><a name="EnvironmentVSSystemProperties"><i>系统属性</i>和<i>环境变量</i></a>
     * 都是名称和值之间的概念映射。这两种机制都可以用于向 Java 进程传递用户定义的信息。
     * 环境变量具有更广泛的影响，因为它们对定义它们的进程的所有子进程都可见，而不仅仅是立即的 Java 子进程。
     * 它们在不同的操作系统上可能具有不同的语义，例如大小写不敏感。因此，环境变量更可能产生意外的副作用。
     * 最好在可能的情况下使用系统属性。如果需要全局影响或外部系统接口需要环境变量（如 <code>PATH</code>），
     * 则应使用环境变量。
     *
     * <p>在 UNIX 系统上，<code>name</code> 的字母大小写通常很重要，而在 Microsoft Windows 系统上通常不重要。
     * 例如，表达式 <code>System.getenv("FOO").equals(System.getenv("foo"))</code>
     * 在 Microsoft Windows 上可能是 true。
     *
     * @param  name 环境变量的名称
     * @return 变量的字符串值，如果系统环境中未定义该变量，则返回 <code>null</code>
     * @throws NullPointerException 如果 <code>name</code> 为 <code>null</code>
     * @throws SecurityException
     *         如果存在安全管理器且其
     *         {@link SecurityManager#checkPermission checkPermission}
     *         方法不允许访问环境变量 <code>name</code>
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
     * 环境是系统依赖的从名称到值的映射，从父进程传递到子进程。
     *
     * <p>如果系统不支持环境变量，将返回一个空映射。
     *
     * <p>返回的映射不会包含 null 键或值。
     * 尝试查询 null 键或值将抛出 {@link NullPointerException}。
     * 尝试查询不是 {@link String} 类型的键或值将抛出 {@link ClassCastException}。
     *
     * <p>返回的映射及其集合视图可能不遵守
     * {@link Object#equals} 和 {@link Object#hashCode} 方法的通用契约。
     *
     * <p>在所有平台上，返回的映射通常对大小写敏感。
     *
     * <p>如果存在安全管理器，其
     * {@link SecurityManager#checkPermission checkPermission}
     * 方法将使用 <code>{@link RuntimePermission}("getenv.*")</code>
     * 权限调用。这可能导致抛出 {@link SecurityException}。
     *
     * <p>当向 Java 子进程传递信息时，
     * <a href=#EnvironmentVSSystemProperties>系统属性</a>
     * 通常优于环境变量。
     *
     * @return 环境作为变量名称到值的映射
     * @throws SecurityException
     *         如果存在安全管理器且其
     *         {@link SecurityManager#checkPermission checkPermission}
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
     * 终止当前正在运行的 Java 虚拟机。参数用作状态码；按照惯例，非零状态码表示异常终止。
     * <p>
     * 此方法调用 <code>Runtime</code> 类中的 <code>exit</code> 方法。此方法永远不会正常返回。
     * <p>
     * 调用 <code>System.exit(n)</code> 实际上等效于调用：
     * <blockquote><pre>
     * Runtime.getRuntime().exit(n)
     * </pre></blockquote>
     *
     * @param      status   退出状态。
     * @throws  SecurityException
     *        如果存在安全管理器且其 <code>checkExit</code>
     *        方法不允许以指定状态退出。
     * @see        java.lang.Runtime#exit(int)
     */
    public static void exit(int status) {
        Runtime.getRuntime().exit(status);
    }

    /**
     * 运行垃圾收集器。
     * <p>
     * 调用 <code>gc</code> 方法建议 Java 虚拟机努力回收未使用对象占用的内存，以便快速重用。
     * 当控制返回方法调用时，Java 虚拟机已尽力回收所有已丢弃对象的空间。
     * <p>
     * 调用 <code>System.gc()</code> 实际上等效于调用：
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
     * 运行任何待定终结化的方法。
     * <p>
     * 调用此方法建议 Java 虚拟机努力运行已发现被丢弃但其 <code>finalize</code>
     * 方法尚未运行的对象的 <code>finalize</code> 方法。当控制返回方法调用时，Java 虚拟机已尽力完成所有待定的终结化。
     * <p>
     * 调用 <code>System.runFinalization()</code> 实际上等效于调用：
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
     * 抛出 {@code UnsupportedOperationException}。
     *
     * <p>调用 {@code System.runFinalizersOnExit()} 实际上等效于调用：
     * <blockquote><pre>
     * Runtime.runFinalizersOnExit()
     * </pre></blockquote>
     *
     * @param value 被忽略
     *
     * @deprecated 此方法最初设计用于启用或禁用在退出时运行终结器。在退出时运行终结器默认是禁用的。如果启用，则在 Java 运行时退出之前，将运行所有未自动调用终结器的对象的 <code>finalize</code> 方法。这种行为本质上是不安全的。它可能导致在其他线程同时操作这些对象时调用终结器，从而导致不规则行为或死锁。
     *
     * @see java.lang.Runtime#runFinalizersOnExit(boolean)
     * @since JDK1.1
     */
    @Deprecated
    public static void runFinalizersOnExit(boolean value) {
        Runtime.runFinalizersOnExit(value);
    }

    /**
     * 加载由文件名参数指定的本机库。文件名参数必须是绝对路径名。
     *
     * 如果文件名参数，去掉任何平台特定的库前缀、路径和文件扩展名后，表示一个名为 L 的库，并且该库与 VM 静态链接，
     * 则将调用库导出的 JNI_OnLoad_L 函数，而不是尝试加载动态库。
     * 不需要文件系统中存在与参数匹配的文件。
     * 有关更多详细信息，请参阅 JNI 规范。
     *
     * 否则，文件名参数将以实现依赖的方式映射到本机库映像。
     *
     * <p>
     * 调用 <code>System.load(name)</code> 实际上等效于调用：
     * <blockquote><pre>
     * Runtime.getRuntime().load(name)
     * </pre></blockquote>
     *
     * @param      filename   要加载的文件。
     * @exception  SecurityException  如果存在安全管理器且其
     *             <code>checkLink</code> 方法不允许
     *             加载指定的动态库
     * @exception  UnsatisfiedLinkError  如果文件名不是绝对路径名，本机库未与 VM 静态链接，或主机系统无法将库映射到
     *             本机库映像。
     * @exception  NullPointerException 如果 <code>filename</code> 为
     *             <code>null</code>
     * @see        java.lang.Runtime#load(java.lang.String)
     * @see        java.lang.SecurityManager#checkLink(java.lang.String)
     */
    @CallerSensitive
    public static void load(String filename) {
        Runtime.getRuntime().load0(Reflection.getCallerClass(), filename);
    }


                /**
     * 加载由 <code>libname</code> 参数指定的本地库。 <code>libname</code> 参数不得包含任何平台特定的前缀、文件扩展名或路径。如果名为 <code>libname</code> 的本地库与 VM 静态链接，则导出该库的 <code>JNI_OnLoad_<code>libname</code></code> 函数将被调用。有关更多详细信息，请参阅 JNI 规范。
     *
     * 否则，<code>libname</code> 参数将从系统库位置加载，并以实现依赖的方式映射到本地库映像。
     * <p>
     * 调用 <code>System.loadLibrary(name)</code> 实际上等效于调用
     * <blockquote><pre>
     * Runtime.getRuntime().loadLibrary(name)
     * </pre></blockquote>
     *
     * @param      libname   库的名称。
     * @exception  SecurityException  如果存在安全经理且其 <code>checkLink</code> 方法不允许
     *             加载指定的动态库
     * @exception  UnsatisfiedLinkError 如果 <code>libname</code> 参数
     *             包含文件路径，本地库未与 VM 静态链接，或者库无法映射到
     *             主机系统的本地库映像。
     * @exception  NullPointerException 如果 <code>libname</code> 为
     *             <code>null</code>
     * @see        java.lang.Runtime#loadLibrary(java.lang.String)
     * @see        java.lang.SecurityManager#checkLink(java.lang.String)
     */
    @CallerSensitive
    public static void loadLibrary(String libname) {
        Runtime.getRuntime().loadLibrary0(Reflection.getCallerClass(), libname);
    }

    /**
     * 将库名称映射到表示本地库的平台特定字符串。
     *
     * @param      libname 库的名称。
     * @return     平台依赖的本地库名称。
     * @exception  NullPointerException 如果 <code>libname</code> 为
     *             <code>null</code>
     * @see        java.lang.System#loadLibrary(java.lang.String)
     * @see        java.lang.ClassLoader#findLibrary(java.lang.String)
     * @since      1.2
     */
    public static native String mapLibraryName(String libname);

    /**
     * 基于编码创建 PrintStream 用于 stdout/err。
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

        // VM 可能会调用 JNU_NewStringPlatform() 来设置那些编码敏感的属性（如 user.home, user.name, boot.class.path 等）
        // 在“props”初始化期间，它可能需要通过 System.getProperty() 访问相关系统编码属性，这些属性已在初始化早期阶段放入“props”中。因此，确保“props”在初始化的非常早期阶段可用，并将所有系统属性直接放入其中。
        props = new Properties();
        initProperties(props);  // 由 VM 初始化

        // 有某些系统配置可能受 VM 选项控制，例如直接内存的最大量和
        // 用于支持自动装箱对象身份语义的 Integer 缓存大小。通常，库将从 VM 设置的属性中获取这些值。如果属性仅用于内部实现，应从系统属性中删除这些属性。
        //
        // 请参阅 java.lang.Integer.IntegerCache 和 sun.misc.VM.saveAndRemoveProperties 方法。
        //
        // 保存一个只能由内部实现访问的系统属性对象的私有副本。删除不打算公开访问的某些系统属性。
        sun.misc.VM.saveAndRemoveProperties(props);

        lineSeparator = props.getProperty("line.separator");
        StaticProperty.jdkSerialFilter();   // 加载 StaticProperty 以缓存属性值
        sun.misc.Version.init();

        FileInputStream fdIn = new FileInputStream(FileDescriptor.in);
        FileOutputStream fdOut = new FileOutputStream(FileDescriptor.out);
        FileOutputStream fdErr = new FileOutputStream(FileDescriptor.err);
        setIn0(new BufferedInputStream(fdIn));
        setOut0(newPrintStream(fdOut, props.getProperty("sun.stdout.encoding")));
        setErr0(newPrintStream(fdErr, props.getProperty("sun.stderr.encoding")));

        // 现在加载 zip 库，以防止 java.util.zip.ZipFile
        // 以后尝试使用自身来加载此库。
        loadLibrary("zip");

        // 为 HUP、TERM 和 INT（如果可用）设置 Java 信号处理程序。
        Terminator.setup();

        // 初始化需要为类库设置的任何其他操作系统设置。目前，除了 Windows 之外，其他地方都是空操作，Windows 会在使用 java.io 类之前设置进程范围的错误模式。
        sun.misc.VM.initializeOSEnvironment();

        // 主线程不会像其他线程那样被添加到其线程组；我们必须在这里自己添加。
        Thread current = Thread.currentThread();
        current.getThreadGroup().add(current);

        // 注册共享秘密
        setJavaLangAccess();

        // 在初始化期间调用的子系统可以调用
        // sun.misc.VM.isBooted() 以避免在应用程序类加载器设置完成之前执行某些操作。
        // 重要：确保这是最后一个初始化操作！
        sun.misc.VM.booted();
    }

    private static void setJavaLangAccess() {
        // 允许 java.lang 之外的特权类
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
