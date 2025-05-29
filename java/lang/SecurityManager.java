/*
 * 版权所有 (c) 1995, 2013，Oracle 和/或其附属公司。保留所有权利。
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

package java.lang;

import java.security.*;
import java.io.FileDescriptor;
import java.io.File;
import java.io.FilePermission;
import java.awt.AWTPermission;
import java.util.PropertyPermission;
import java.lang.RuntimePermission;
import java.net.SocketPermission;
import java.net.NetPermission;
import java.util.Hashtable;
import java.net.InetAddress;
import java.lang.reflect.*;
import java.net.URL;

import sun.reflect.CallerSensitive;
import sun.security.util.SecurityConstants;

/**
 * 安全管理器是一个允许
 * 应用程序实现安全策略的类。它允许一个
 * 应用程序在执行可能不安全或
 * 敏感的操作之前，确定操作是什么以及
 * 是否在允许执行该操作的安全上下文中尝试该操作。应用程序可以允许或禁止该操作。
 * <p>
 * <code>SecurityManager</code> 类包含许多以
 * <code>check</code> 开头的方法。这些方法
 * 在 Java 库中的各种方法执行某些潜在敏感操作之前被调用。调用这样的 <code>check</code> 方法通常如下所示：
 * <blockquote><pre>
 *     SecurityManager security = System.getSecurityManager();
 *     if (security != null) {
 *         security.check<i>XXX</i>(argument, &nbsp;.&nbsp;.&nbsp;.&nbsp;);
 *     }
 * </pre></blockquote>
 * <p>
 * 安全管理器因此有机会通过抛出异常来阻止
 * 操作的完成。如果操作被允许，安全
 * 管理器例程简单地返回，但如果操作不
 * 被允许，则抛出 <code>SecurityException</code>。唯一的例外是
 * <code>checkTopLevelWindow</code>，它返回一个
 * <code>boolean</code> 值。
 * <p>
 * 当前的安全管理器通过
 * <code>System</code> 类中的 <code>setSecurityManager</code> 方法设置。通过
 * <code>getSecurityManager</code> 方法获取当前的安全管理器。
 * <p>
 * 特殊方法
 * {@link SecurityManager#checkPermission(java.security.Permission)}
 * 确定是否应该授予或拒绝由指定
 * 权限表示的访问请求。默认实现调用
 *
 * <pre>
 *   AccessController.checkPermission(perm);
 * </pre>
 *
 * <p>
 * 如果请求的访问被允许，
 * <code>checkPermission</code> 静默返回。如果被拒绝，则抛出
 * <code>SecurityException</code>。
 * <p>
 * 从 Java 2 SDK v1.2 开始，<code>SecurityManager</code> 中每个其他
 * <code>check</code> 方法的默认实现是调用
 * <code>SecurityManager checkPermission</code> 方法
 * 来确定调用线程是否有权限执行请求的操作。
 * <p>
 * 请注意，具有
 * 仅一个权限参数的 <code>checkPermission</code> 方法始终在当前执行线程的上下文中执行安全检查。
 * 有时需要在给定上下文中进行的安全检查
 * 实际上需要从
 * <i>不同的</i> 上下文中完成（例如，从工作线程中）。为此提供了
 * {@link SecurityManager#getSecurityContext getSecurityContext} 方法
 * 和包括上下文参数的 {@link SecurityManager#checkPermission(java.security.Permission,
 * java.lang.Object) checkPermission}
 * 方法。<code>getSecurityContext</code> 方法返回一个“快照”
 * 当前调用上下文。（默认实现返回一个 AccessControlContext 对象。）一个示例调用如下：
 *
 * <pre>
 *   Object context = null;
 *   SecurityManager sm = System.getSecurityManager();
 *   if (sm != null) context = sm.getSecurityContext();
 * </pre>
 *
 * <p>
 * 除了权限之外还接受上下文对象的 <code>checkPermission</code> 方法
 * 基于该上下文做出访问决策，
 * 而不是基于当前执行线程的上下文。
 * 因此，不同上下文中的代码可以调用该方法，
 * 传递权限和
 * 之前保存的上下文对象。使用上例中获得的
 * SecurityManager <code>sm</code> 的示例调用如下：
 *
 * <pre>
 *   if (sm != null) sm.checkPermission(permission, context);
 * </pre>
 *
 * <p>权限分为以下类别：文件、套接字、网络、
 * 安全、运行时、属性、AWT、反射和可序列化。
 * 管理这些各种
 * 权限类别的类是 <code>java.io.FilePermission</code>，
 * <code>java.net.SocketPermission</code>，
 * <code>java.net.NetPermission</code>，
 * <code>java.security.SecurityPermission</code>，
 * <code>java.lang.RuntimePermission</code>，
 * <code>java.util.PropertyPermission</code>，
 * <code>java.awt.AWTPermission</code>，
 * <code>java.lang.reflect.ReflectPermission</code> 和
 * <code>java.io.SerializablePermission</code>。
 *
 * <p>除了前两个（FilePermission 和 SocketPermission）之外的所有权限都是
 * <code>java.security.BasicPermission</code> 的子类，而 BasicPermission 本身
 * 是顶级权限类
 * <code>java.security.Permission</code> 的抽象子类。BasicPermission 定义了
 * 所有包含名称并遵循分层属性命名约定的权限所需的
 * 功能（例如，"exitVM"、"setFactory"、"queuePrintJob" 等）。
 * 名称末尾可以出现星号
 * 跟在 "." 后面，或者单独出现，以表示通配符匹配。例如："a.*" 或 "*" 是有效的，
 * "*a" 或 "a*b" 是无效的。
 *
 * <p>FilePermission 和 SocketPermission 是顶级权限类
 * (<code>java.security.Permission</code>) 的子类。像这样的类
 * 比 BasicPermission 有更复杂的名称语法，直接从 Permission 而不是从
 * BasicPermission 继承。例如，
 * 对于 <code>java.io.FilePermission</code> 对象，权限名称是
 * 文件（或目录）的路径名。
 *
 * <p>某些权限类有一个“操作”列表，告诉
 * 对象允许的操作。例如，
 * 对于 <code>java.io.FilePermission</code> 对象，操作列表
 * （如 "read, write"）指定了授予的
 * 指定文件（或目录中的文件）的操作。
 *
 * <p>其他权限类是“命名”权限 -
 * 包含名称但没有操作列表的权限；你要么有命名的权限，要么没有。
 *
 * <p>注意：还有一个 <code>java.security.AllPermission</code>
 * 权限，意味着所有权限。它存在是为了简化需要执行多个
 * 需要所有（或许多）权限的任务的系统管理员的工作。
 * <p>
 * 有关权限的相关信息，请参阅 <a href ="../../../technotes/guides/security/permissions.html">
 * JDK 中的权限</a>。该文档包括，例如，一个表格列出了各种 SecurityManager
 * <code>check</code> 方法及其默认
 * 实现所需权限。它还包含一个所有 1.2 版本方法的表格
 * 需要权限，并说明每个方法需要的权限。
 * <p>
 * 有关 JDK 中 <code>SecurityManager</code> 更改的信息以及关于 1.1 风格安全管理器移植的建议，
 * 请参阅 <a href="../../../technotes/guides/security/index.html">安全文档</a>。
 *
 * @author  Arthur van Hoff
 * @author  Roland Schemers
 *
 * @see     java.lang.ClassLoader
 * @see     java.lang.SecurityException
 * @see     java.lang.SecurityManager#checkTopLevelWindow(java.lang.Object)
 *  checkTopLevelWindow
 * @see     java.lang.System#getSecurityManager() getSecurityManager
 * @see     java.lang.System#setSecurityManager(java.lang.SecurityManager)
 *  setSecurityManager
 * @see     java.security.AccessController AccessController
 * @see     java.security.AccessControlContext AccessControlContext
 * @see     java.security.AccessControlException AccessControlException
 * @see     java.security.Permission
 * @see     java.security.BasicPermission
 * @see     java.io.FilePermission
 * @see     java.net.SocketPermission
 * @see     java.util.PropertyPermission
 * @see     java.lang.RuntimePermission
 * @see     java.awt.AWTPermission
 * @see     java.security.Policy Policy
 * @see     java.security.SecurityPermission SecurityPermission
 * @see     java.security.ProtectionDomain
 *
 * @since   JDK1.0
 */
public
class SecurityManager {

                    /**
     * 如果正在进行安全检查，此字段为 <code>true</code>；否则为 <code>false</code>。
     *
     * @deprecated 不推荐使用这种类型的安全检查。
     *  建议使用 <code>checkPermission</code>
     *  调用代替。
     */
    @Deprecated
    protected boolean inCheck;

    /*
     * 我们是否已被初始化。有效防止终结器攻击。
     */
    private boolean initialized = false;


    /**
     * 如果当前上下文已被授予 AllPermission，则返回 true
     */
    private boolean hasAllPermission()
    {
        try {
            checkPermission(SecurityConstants.ALL_PERMISSION);
            return true;
        } catch (SecurityException se) {
            return false;
        }
    }

    /**
     * 测试是否正在进行安全检查。
     *
     * @return <code>inCheck</code> 字段的值。如果正在进行安全检查，
     *          该字段应包含 <code>true</code>，
     *          否则为 <code>false</code>。
     * @see     java.lang.SecurityManager#inCheck
     * @deprecated 不推荐使用这种类型的安全检查。
     *  建议使用 <code>checkPermission</code>
     *  调用代替。
     */
    @Deprecated
    public boolean getInCheck() {
        return inCheck;
    }

    /**
     * 构造一个新的 <code>SecurityManager</code>。
     *
     * <p> 如果已经安装了安全经理，此方法首先调用安全经理的 <code>checkPermission</code> 方法
     * 用 <code>RuntimePermission("createSecurityManager")</code>
     * 权限确保调用线程有权限创建新的安全经理。
     * 这可能导致抛出 <code>SecurityException</code>。
     *
     * @exception  java.lang.SecurityException 如果已经存在安全经理且其 <code>checkPermission</code> 方法
     *             不允许创建新的安全经理。
     * @see        java.lang.System#getSecurityManager()
     * @see        #checkPermission(java.security.Permission) checkPermission
     * @see java.lang.RuntimePermission
     */
    public SecurityManager() {
        synchronized(SecurityManager.class) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                // 询问当前安装的安全经理是否可以创建新的安全经理。
                sm.checkPermission(new RuntimePermission
                                   ("createSecurityManager"));
            }
            initialized = true;
        }
    }

    /**
     * 以类数组的形式返回当前执行堆栈。
     * <p>
     * 数组的长度是执行堆栈上的方法数。索引 <code>0</code> 处的元素是当前执行方法的类，
     * 索引 <code>1</code> 处的元素是该方法的调用者的类，依此类推。
     *
     * @return  执行堆栈。
     */
    protected native Class[] getClassContext();

    /**
     * 返回从使用非系统类加载器定义的类中最近执行的方法的类加载器。非系统
     * 类加载器是指不等于系统类加载器（由 {@link ClassLoader#getSystemClassLoader} 返回）
     * 或其祖先的类加载器。
     * <p>
     * 在以下三种情况下，此方法将返回
     * <code>null</code>：
     * <ol>
     *   <li>执行堆栈上的所有方法都是从使用系统类加载器或其祖先定义的类中调用的。
     *
     *   <li>执行堆栈上的所有方法，直到第一个
     *   "特权" 调用者
     *   （参见 {@link java.security.AccessController#doPrivileged}）
     *   都是从使用系统类加载器或其祖先定义的类中调用的。
     *
     *   <li> 对 <code>checkPermission</code> 的调用
     *   用 <code>java.security.AllPermission</code> 不
     *   会导致 SecurityException。
     *
     * </ol>
     *
     * @return  堆栈上最近出现的从使用非系统类加载器定义的类中调用的方法的类加载器。
     *
     * @deprecated 不推荐使用这种类型的安全检查。
     *  建议使用 <code>checkPermission</code>
     *  调用代替。
     *
     * @see  java.lang.ClassLoader#getSystemClassLoader() getSystemClassLoader
     * @see  #checkPermission(java.security.Permission) checkPermission
     */
    @Deprecated
    protected ClassLoader currentClassLoader()
    {
        ClassLoader cl = currentClassLoader0();
        if ((cl != null) && hasAllPermission())
            cl = null;
        return cl;
    }

    private native ClassLoader currentClassLoader0();

    /**
     * 返回从使用非系统类加载器定义的类中最近执行的方法的类。非系统
     * 类加载器是指不等于系统类加载器（由 {@link ClassLoader#getSystemClassLoader} 返回）
     * 或其祖先的类加载器。
     * <p>
     * 在以下三种情况下，此方法将返回
     * <code>null</code>：
     * <ol>
     *   <li>执行堆栈上的所有方法都是从使用系统类加载器或其祖先定义的类中调用的。
     *
     *   <li>执行堆栈上的所有方法，直到第一个
     *   "特权" 调用者
     *   （参见 {@link java.security.AccessController#doPrivileged}）
     *   都是从使用系统类加载器或其祖先定义的类中调用的。
     *
     *   <li> 对 <code>checkPermission</code> 的调用
     *   用 <code>java.security.AllPermission</code> 不
     *   会导致 SecurityException。
     *
     * </ol>
     *
     * @return  堆栈上最近出现的从使用非系统类加载器定义的类中调用的方法的类。
     *
     * @deprecated 不推荐使用这种类型的安全检查。
     *  建议使用 <code>checkPermission</code>
     *  调用代替。
     *
     * @see  java.lang.ClassLoader#getSystemClassLoader() getSystemClassLoader
     * @see  #checkPermission(java.security.Permission) checkPermission
     */
    @Deprecated
    protected Class<?> currentLoadedClass() {
        Class<?> c = currentLoadedClass0();
        if ((c != null) && hasAllPermission())
            c = null;
        return c;
    }

                    /**
     * 返回指定类的堆栈深度。
     *
     * @param   name   要搜索的类的完全限定名称。
     * @return  指定名称类的方法首次出现的堆栈帧深度；
     *          如果找不到这样的帧，则返回<code>-1</code>。
     * @deprecated 不推荐使用这种类型的安全检查。
     *  建议使用<code>checkPermission</code>
     *  调用代替。
     *
     */
    @Deprecated
    protected native int classDepth(String name);

    /**
     * 返回最近执行的使用非系统类加载器定义的类的方法的堆栈深度。非系统
     * 类加载器定义为不等于系统类加载器（由
     * {@link ClassLoader#getSystemClassLoader}返回）或其祖先的类加载器。
     * <p>
     * 在以下三种情况下，此方法将返回
     * -1：
     * <ol>
     *   <li>执行堆栈上的所有方法都是由系统类加载器或其祖先定义的类的方法。
     *
     *   <li>执行堆栈上的所有方法，直到第一个
     *   "特权"调用者
     *   （参见{@link java.security.AccessController#doPrivileged}）
     *   都是由系统类加载器或其祖先定义的类的方法。
     *
     *   <li> 对<code>checkPermission</code>的调用
     *   与<code>java.security.AllPermission</code>不
     *   导致SecurityException。
     *
     * </ol>
     *
     * @return 最近出现的使用非系统类加载器定义的类的方法的堆栈帧深度。
     *
     * @deprecated 不推荐使用这种类型的安全检查。
     *  建议使用<code>checkPermission</code>
     *  调用代替。
     *
     * @see   java.lang.ClassLoader#getSystemClassLoader() getSystemClassLoader
     * @see   #checkPermission(java.security.Permission) checkPermission
     */
    @Deprecated
    protected int classLoaderDepth()
    {
        int depth = classLoaderDepth0();
        if (depth != -1) {
            if (hasAllPermission())
                depth = -1;
            else
                depth--; // 确保不包括我们自己
        }
        return depth;
    }

    private native int classLoaderDepth0();

    /**
     * 测试指定名称的类的方法是否在执行堆栈上。
     *
     * @param  name   类的完全限定名称。
     * @return <code>true</code> 如果指定名称的类的方法在执行堆栈上；否则返回<code>false</code>。
     * @deprecated 不推荐使用这种类型的安全检查。
     *  建议使用<code>checkPermission</code>
     *  调用代替。
     */
    @Deprecated
    protected boolean inClass(String name) {
        return classDepth(name) >= 0;
    }

    /**
     * 基本上，测试使用类加载器定义的类的方法是否在执行堆栈上。
     *
     * @return  如果对<code>currentClassLoader</code>的调用
     *          有非空返回值，则返回<code>true</code>。
     *
     * @deprecated 不推荐使用这种类型的安全检查。
     *  建议使用<code>checkPermission</code>
     *  调用代替。
     * @see        #currentClassLoader() currentClassLoader
     */
    @Deprecated
    protected boolean inClassLoader() {
        return currentClassLoader() != null;
    }

    /**
     * 创建一个封装当前执行
     * 环境的对象。此方法的结果用于，例如，三参数的<code>checkConnect</code>方法和
     * 两参数的<code>checkRead</code>方法。
     * 这些方法是必要的，因为一个受信任的方法可能会被调用来代表另一个方法读取文件或打开套接字。
     * 受信任的方法需要确定另一个（可能是不受信任的）方法是否可以自行执行该操作。
     * <p> 该方法的默认实现是返回
     * 一个<code>AccessControlContext</code>对象。
     *
     * @return  一个实现依赖的对象，封装了足够的信息关于当前执行环境
     *          以便稍后执行某些安全检查。
     * @see     java.lang.SecurityManager#checkConnect(java.lang.String, int,
     *   java.lang.Object) checkConnect
     * @see     java.lang.SecurityManager#checkRead(java.lang.String,
     *   java.lang.Object) checkRead
     * @see     java.security.AccessControlContext AccessControlContext
     */
    public Object getSecurityContext() {
        return AccessController.getContext();
    }

    /**
     * 如果根据当前生效的安全策略，不授予请求的
     * 访问权限（由给定的权限指定），则抛出<code>SecurityException</code>。
     * <p>
     * 此方法调用<code>AccessController.checkPermission</code>
     * 并传递给定的权限。
     *
     * @param     perm   请求的权限。
     * @exception SecurityException 如果根据当前的安全策略，不允许访问。
     * @exception NullPointerException 如果权限参数为
     *            <code>null</code>。
     * @since     1.2
     */
    public void checkPermission(Permission perm) {
        java.security.AccessController.checkPermission(perm);
    }

    /**
     * 如果指定的安全上下文被拒绝访问由给定权限指定的资源，
     * 则抛出<code>SecurityException</code>。
     * 该上下文必须是由
     * <code>getSecurityContext</code>的先前调用返回的安全上下文，
     * 访问控制决策基于该安全上下文的配置安全策略。
     * <p>
     * 如果<code>context</code>是
     * <code>AccessControlContext</code>的实例，则调用
     * <code>AccessControlContext.checkPermission</code>方法并传递指定的权限。
     * <p>
     * 如果<code>context</code>不是
     * <code>AccessControlContext</code>的实例，则抛出
     * <code>SecurityException</code>。
     *
     * @param      perm      指定的权限
     * @param      context   系统依赖的安全上下文。
     * @exception  SecurityException  如果指定的安全上下文
     *             不是<code>AccessControlContext</code>的实例
     *             （例如，为<code>null</code>），或者被拒绝访问由给定权限指定的资源。
     * @exception  NullPointerException 如果权限参数为
     *             <code>null</code>。
     * @see        java.lang.SecurityManager#getSecurityContext()
     * @see java.security.AccessControlContext#checkPermission(java.security.Permission)
     * @since      1.2
     */
    public void checkPermission(Permission perm, Object context) {
        if (context instanceof AccessControlContext) {
            ((AccessControlContext)context).checkPermission(perm);
        } else {
            throw new SecurityException();
        }
    }


                    /**
     * 如果调用线程不允许创建新的类加载器，则抛出 <code>SecurityException</code>。
     * <p>
     * 此方法调用 <code>checkPermission</code>，并使用
     * <code>RuntimePermission("createClassLoader")</code>
     * 权限。
     * <p>
     * 如果你重写了此方法，则应在重写方法通常会抛出异常的点调用
     * <code>super.checkCreateClassLoader</code>。
     *
     * @exception SecurityException 如果调用线程没有权限
     *             创建新的类加载器。
     * @see        java.lang.ClassLoader#ClassLoader()
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkCreateClassLoader() {
        checkPermission(SecurityConstants.CREATE_CLASSLOADER_PERMISSION);
    }

    /**
     * 用于 checkAccess 方法的根线程组的引用。
     */

    private static ThreadGroup rootGroup = getRootGroup();

    private static ThreadGroup getRootGroup() {
        ThreadGroup root =  Thread.currentThread().getThreadGroup();
        while (root.getParent() != null) {
            root = root.getParent();
        }
        return root;
    }

    /**
     * 如果调用线程不允许修改线程参数，则抛出 <code>SecurityException</code>。
     * <p>
     * 当前安全管理器通过 <code>Thread</code> 类的 <code>stop</code>、<code>suspend</code>、<code>resume</code>、
     * <code>setPriority</code>、<code>setName</code> 和 <code>setDaemon</code> 方法调用此方法。
     * <p>
     * 如果线程参数是系统线程（属于具有 <code>null</code> 父级的线程组），则此方法调用
     * <code>checkPermission</code>，并使用 <code>RuntimePermission("modifyThread")</code> 权限。
     * 如果线程参数 <i>不是</i> 系统线程，此方法则静默返回。
     * <p>
     * 需要更严格策略的应用程序应重写此方法。如果此方法被重写，重写的方法还应检查调用线程是否具有
     * <code>RuntimePermission("modifyThread")</code> 权限，如果有，则静默返回。这是为了确保被授予
     * 该权限的代码（如 JDK 本身）可以操作任何线程。
     * <p>
     * 如果此方法被重写，则应在重写方法的第一个语句中调用 <code>super.checkAccess</code>，或者在重写方法中放置等效的安全检查。
     *
     * @param      t   要检查的线程。
     * @exception  SecurityException  如果调用线程没有权限修改线程。
     * @exception  NullPointerException 如果线程参数为 <code>null</code>。
     * @see        java.lang.Thread#resume() resume
     * @see        java.lang.Thread#setDaemon(boolean) setDaemon
     * @see        java.lang.Thread#setName(java.lang.String) setName
     * @see        java.lang.Thread#setPriority(int) setPriority
     * @see        java.lang.Thread#stop() stop
     * @see        java.lang.Thread#suspend() suspend
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkAccess(Thread t) {
        if (t == null) {
            throw new NullPointerException("thread can't be null");
        }
        if (t.getThreadGroup() == rootGroup) {
            checkPermission(SecurityConstants.MODIFY_THREAD_PERMISSION);
        } else {
            // just return
        }
    }
    /**
     * 如果调用线程不允许修改线程组参数，则抛出 <code>SecurityException</code>。
     * <p>
     * 当前安全管理器在创建新的子线程或子线程组时，以及通过 <code>ThreadGroup</code> 类的
     * <code>setDaemon</code>、<code>setMaxPriority</code>、<code>stop</code>、<code>suspend</code>、
     * <code>resume</code> 和 <code>destroy</code> 方法时调用此方法。
     * <p>
     * 如果线程组参数是系统线程组（具有 <code>null</code> 父级），则此方法调用
     * <code>checkPermission</code>，并使用 <code>RuntimePermission("modifyThreadGroup")</code> 权限。
     * 如果线程组参数 <i>不是</i> 系统线程组，此方法则静默返回。
     * <p>
     * 需要更严格策略的应用程序应重写此方法。如果此方法被重写，重写的方法还应检查调用线程是否具有
     * <code>RuntimePermission("modifyThreadGroup")</code> 权限，如果有，则静默返回。这是为了确保被授予
     * 该权限的代码（如 JDK 本身）可以操作任何线程。
     * <p>
     * 如果此方法被重写，则应在重写方法的第一个语句中调用 <code>super.checkAccess</code>，或者在重写方法中放置等效的安全检查。
     *
     * @param      g   要检查的线程组。
     * @exception  SecurityException  如果调用线程没有权限修改线程组。
     * @exception  NullPointerException 如果线程组参数为 <code>null</code>。
     * @see        java.lang.ThreadGroup#destroy() destroy
     * @see        java.lang.ThreadGroup#resume() resume
     * @see        java.lang.ThreadGroup#setDaemon(boolean) setDaemon
     * @see        java.lang.ThreadGroup#setMaxPriority(int) setMaxPriority
     * @see        java.lang.ThreadGroup#stop() stop
     * @see        java.lang.ThreadGroup#suspend() suspend
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkAccess(ThreadGroup g) {
        if (g == null) {
            throw new NullPointerException("thread group can't be null");
        }
        if (g == rootGroup) {
            checkPermission(SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
        } else {
            // just return
        }
    }

                    /**
     * 如果调用线程不允许使用指定的状态码使 Java 虚拟机停止，则抛出 <code>SecurityException</code>。
     * <p>
     * 该方法由 <code>Runtime</code> 类的 <code>exit</code> 方法调用当前的安全管理器。状态码为 <code>0</code> 表示成功；其他值表示各种错误。
     * <p>
     * 该方法使用 <code>RuntimePermission("exitVM."+status)</code> 权限调用 <code>checkPermission</code>。
     * <p>
     * 如果你重写了此方法，那么你应该在重写方法通常会抛出异常的地方调用 <code>super.checkExit</code>。
     *
     * @param      status   退出状态。
     * @exception SecurityException 如果调用线程没有权限以指定状态使 Java 虚拟机停止。
     * @see        java.lang.Runtime#exit(int) exit
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkExit(int status) {
        checkPermission(new RuntimePermission("exitVM."+status));
    }

    /**
     * 如果调用线程不允许创建子进程，则抛出 <code>SecurityException</code>。
     * <p>
     * 该方法由 <code>Runtime</code> 类的 <code>exec</code> 方法调用当前的安全管理器。
     * <p>
     * 如果 cmd 是绝对路径，该方法使用 <code>FilePermission(cmd,"execute")</code> 权限调用 <code>checkPermission</code>；
     * 否则，它使用 <code>FilePermission("&lt;&lt;ALL FILES&gt;&gt;","execute")</code> 权限调用 <code>checkPermission</code>。
     * <p>
     * 如果你重写了此方法，那么你应该在重写方法通常会抛出异常的地方调用 <code>super.checkExec</code>。
     *
     * @param      cmd   指定的系统命令。
     * @exception  SecurityException 如果调用线程没有权限创建子进程。
     * @exception  NullPointerException 如果 <code>cmd</code> 参数为 <code>null</code>。
     * @see     java.lang.Runtime#exec(java.lang.String)
     * @see     java.lang.Runtime#exec(java.lang.String, java.lang.String[])
     * @see     java.lang.Runtime#exec(java.lang.String[])
     * @see     java.lang.Runtime#exec(java.lang.String[], java.lang.String[])
     * @see     #checkPermission(java.security.Permission) checkPermission
     */
    public void checkExec(String cmd) {
        File f = new File(cmd);
        if (f.isAbsolute()) {
            checkPermission(new FilePermission(cmd,
                SecurityConstants.FILE_EXECUTE_ACTION));
        } else {
            checkPermission(new FilePermission("<<ALL FILES>>",
                SecurityConstants.FILE_EXECUTE_ACTION));
        }
    }

    /**
     * 如果调用线程不允许动态链接由字符串参数文件指定的库代码，则抛出 <code>SecurityException</code>。参数可以是简单的库名或完整的文件名。
     * <p>
     * 该方法由 <code>Runtime</code> 类的 <code>load</code> 和 <code>loadLibrary</code> 方法调用当前的安全管理器。
     * <p>
     * 该方法使用 <code>RuntimePermission("loadLibrary."+lib)</code> 权限调用 <code>checkPermission</code>。
     * <p>
     * 如果你重写了此方法，那么你应该在重写方法通常会抛出异常的地方调用 <code>super.checkLink</code>。
     *
     * @param      lib   库的名称。
     * @exception  SecurityException 如果调用线程没有权限动态链接库。
     * @exception  NullPointerException 如果 <code>lib</code> 参数为 <code>null</code>。
     * @see        java.lang.Runtime#load(java.lang.String)
     * @see        java.lang.Runtime#loadLibrary(java.lang.String)
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkLink(String lib) {
        if (lib == null) {
            throw new NullPointerException("library can't be null");
        }
        checkPermission(new RuntimePermission("loadLibrary."+lib));
    }

    /**
     * 如果调用线程不允许从指定的文件描述符读取，则抛出 <code>SecurityException</code>。
     * <p>
     * 该方法使用 <code>RuntimePermission("readFileDescriptor")</code> 权限调用 <code>checkPermission</code>。
     * <p>
     * 如果你重写了此方法，那么你应该在重写方法通常会抛出异常的地方调用 <code>super.checkRead</code>。
     *
     * @param      fd   系统依赖的文件描述符。
     * @exception  SecurityException  如果调用线程没有权限访问指定的文件描述符。
     * @exception  NullPointerException 如果文件描述符参数为 <code>null</code>。
     * @see        java.io.FileDescriptor
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkRead(FileDescriptor fd) {
        if (fd == null) {
            throw new NullPointerException("file descriptor can't be null");
        }
        checkPermission(new RuntimePermission("readFileDescriptor"));
    }

    /**
     * 如果调用线程不允许读取由字符串参数指定的文件，则抛出 <code>SecurityException</code>。
     * <p>
     * 该方法使用 <code>FilePermission(file,"read")</code> 权限调用 <code>checkPermission</code>。
     * <p>
     * 如果你重写了此方法，那么你应该在重写方法通常会抛出异常的地方调用 <code>super.checkRead</code>。
     *
     * @param      file   系统依赖的文件名。
     * @exception  SecurityException 如果调用线程没有权限访问指定的文件。
     * @exception  NullPointerException 如果 <code>file</code> 参数为 <code>null</code>。
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkRead(String file) {
        checkPermission(new FilePermission(file,
            SecurityConstants.FILE_READ_ACTION));
    }

                    /**
     * 如果指定的安全上下文不允许读取由字符串参数指定的文件，则抛出 <code>SecurityException</code>。
     * 该上下文必须是由先前调用 <code>getSecurityContext</code> 返回的安全上下文。
     * <p> 如果 <code>context</code> 是 <code>AccessControlContext</code> 的实例，则
     * <code>AccessControlContext.checkPermission</code> 方法将
     * 被调用，权限为 <code>FilePermission(file,"read")</code>。
     * <p> 如果 <code>context</code> 不是 <code>AccessControlContext</code> 的实例，则抛出
     * <code>SecurityException</code>。
     * <p>
     * 如果你重写此方法，则应在重写方法通常会抛出异常的位置调用
     * <code>super.checkRead</code>。
     *
     * @param      file      系统依赖的文件名。
     * @param      context   系统依赖的安全上下文。
     * @exception  SecurityException  如果指定的安全上下文不是 <code>AccessControlContext</code> 的实例
     *             （例如，为 <code>null</code>），或者没有权限读取指定的文件。
     * @exception  NullPointerException 如果 <code>file</code> 参数为
     *             <code>null</code>。
     * @see        java.lang.SecurityManager#getSecurityContext()
     * @see        java.security.AccessControlContext#checkPermission(java.security.Permission)
     */
    public void checkRead(String file, Object context) {
        checkPermission(
            new FilePermission(file, SecurityConstants.FILE_READ_ACTION),
            context);
    }

    /**
     * 如果调用线程不允许写入指定的文件描述符，则抛出 <code>SecurityException</code>。
     * <p>
     * 此方法调用 <code>checkPermission</code>，权限为
     * <code>RuntimePermission("writeFileDescriptor")</code>。
     * <p>
     * 如果你重写此方法，则应在重写方法通常会抛出异常的位置调用
     * <code>super.checkWrite</code>。
     *
     * @param      fd   系统依赖的文件描述符。
     * @exception SecurityException  如果调用线程没有权限访问指定的文件描述符。
     * @exception  NullPointerException 如果文件描述符参数为
     *             <code>null</code>。
     * @see        java.io.FileDescriptor
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkWrite(FileDescriptor fd) {
        if (fd == null) {
            throw new NullPointerException("file descriptor can't be null");
        }
        checkPermission(new RuntimePermission("writeFileDescriptor"));

    }

    /**
     * 如果调用线程不允许写入由字符串参数指定的文件，则抛出 <code>SecurityException</code>。
     * <p>
     * 此方法调用 <code>checkPermission</code>，权限为
     * <code>FilePermission(file,"write")</code>。
     * <p>
     * 如果你重写此方法，则应在重写方法通常会抛出异常的位置调用
     * <code>super.checkWrite</code>。
     *
     * @param      file   系统依赖的文件名。
     * @exception  SecurityException  如果调用线程没有权限访问指定的文件。
     * @exception  NullPointerException 如果 <code>file</code> 参数为
     *             <code>null</code>。
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkWrite(String file) {
        checkPermission(new FilePermission(file,
            SecurityConstants.FILE_WRITE_ACTION));
    }

    /**
     * 如果调用线程不允许删除指定的文件，则抛出 <code>SecurityException</code>。
     * <p>
     * 此方法由 <code>File</code> 类的 <code>delete</code> 方法调用当前安全管理器。
     * <p>
     * 此方法调用 <code>checkPermission</code>，权限为
     * <code>FilePermission(file,"delete")</code>。
     * <p>
     * 如果你重写此方法，则应在重写方法通常会抛出异常的位置调用
     * <code>super.checkDelete</code>。
     *
     * @param      file   系统依赖的文件名。
     * @exception  SecurityException 如果调用线程没有权限删除文件。
     * @exception  NullPointerException 如果 <code>file</code> 参数为
     *             <code>null</code>。
     * @see        java.io.File#delete()
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkDelete(String file) {
        checkPermission(new FilePermission(file,
            SecurityConstants.FILE_DELETE_ACTION));
    }

    /**
     * 如果调用线程不允许打开到指定主机和端口号的套接字连接，则抛出 <code>SecurityException</code>。
     * <p>
     * 端口号为 <code>-1</code> 表示调用方法正在尝试确定指定主机名的 IP 地址。
     * <p>
     * 如果端口不等于 -1，则此方法调用 <code>checkPermission</code>，权限为
     * <code>SocketPermission(host+":"+port,"connect")</code>。如果端口等于 -1，则
     * 调用 <code>checkPermission</code>，权限为
     * <code>SocketPermission(host,"resolve")</code>。
     * <p>
     * 如果你重写此方法，则应在重写方法通常会抛出异常的位置调用
     * <code>super.checkConnect</code>。
     *
     * @param      host   要连接的主机名端口。
     * @param      port   要连接的协议端口。
     * @exception  SecurityException  如果调用线程没有权限打开到指定
     *               <code>host</code> 和 <code>port</code> 的套接字连接。
     * @exception  NullPointerException 如果 <code>host</code> 参数为
     *             <code>null</code>。
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkConnect(String host, int port) {
        if (host == null) {
            throw new NullPointerException("host can't be null");
        }
        if (!host.startsWith("[") && host.indexOf(':') != -1) {
            host = "[" + host + "]";
        }
        if (port == -1) {
            checkPermission(new SocketPermission(host,
                SecurityConstants.SOCKET_RESOLVE_ACTION));
        } else {
            checkPermission(new SocketPermission(host+":"+port,
                SecurityConstants.SOCKET_CONNECT_ACTION));
        }
    }


/**
 * 如果指定的安全上下文不允许打开到指定主机和端口号的套接字连接，则抛出 <code>SecurityException</code>。
 * <p>
 * 端口号为 <code>-1</code> 表示调用方法正在尝试确定指定主机名的 IP 地址。
 * <p> 如果 <code>context</code> 不是 <code>AccessControlContext</code> 的实例，则抛出
 * <code>SecurityException</code>。
 * <p>
 * 否则，检查端口号。如果它不等于 -1，则使用
 * <code>SocketPermission(host+":"+port,"connect")</code> 权限调用 <code>context</code> 的
 * <code>checkPermission</code> 方法。如果端口号等于 -1，则使用
 * <code>SocketPermission(host,"resolve")</code> 权限调用 <code>context</code> 的
 * <code>checkPermission</code> 方法。
 * <p>
 * 如果你重写了此方法，则应在重写方法通常会抛出异常的地方调用
 * <code>super.checkConnect</code>。
 *
 * @param      host      要连接的主机名。
 * @param      port      要连接的协议端口。
 * @param      context   系统依赖的安全上下文。
 * @exception  SecurityException 如果指定的安全上下文不是 <code>AccessControlContext</code>
 *             的实例（例如，为 <code>null</code>），或者没有权限打开到指定
 *             <code>host</code> 和 <code>port</code> 的套接字连接。
 * @exception  NullPointerException 如果 <code>host</code> 参数为
 *             <code>null</code>。
 * @see        java.lang.SecurityManager#getSecurityContext()
 * @see        java.security.AccessControlContext#checkPermission(java.security.Permission)
 */
public void checkConnect(String host, int port, Object context) {
    if (host == null) {
        throw new NullPointerException("host can't be null");
    }
    if (!host.startsWith("[") && host.indexOf(':') != -1) {
        host = "[" + host + "]";
    }
    if (port == -1)
        checkPermission(new SocketPermission(host,
            SecurityConstants.SOCKET_RESOLVE_ACTION),
            context);
    else
        checkPermission(new SocketPermission(host+":"+port,
            SecurityConstants.SOCKET_CONNECT_ACTION),
            context);
}

/**
 * 如果调用线程不允许等待指定本地端口号的连接请求，则抛出 <code>SecurityException</code>。
 * <p>
 * 此方法调用 <code>checkPermission</code>，使用
 * <code>SocketPermission("localhost:"+port,"listen")</code> 权限。
 * <p>
 * 如果你重写了此方法，则应在重写方法通常会抛出异常的地方调用
 * <code>super.checkListen</code>。
 *
 * @param      port   本地端口。
 * @exception  SecurityException  如果调用线程没有权限监听指定端口。
 * @see        #checkPermission(java.security.Permission) checkPermission
 */
public void checkListen(int port) {
    checkPermission(new SocketPermission("localhost:"+port,
        SecurityConstants.SOCKET_LISTEN_ACTION));
}

/**
 * 如果调用线程不允许接受来自指定主机和端口号的套接字连接，则抛出 <code>SecurityException</code>。
 * <p>
 * 此方法由类 <code>ServerSocket</code> 的 <code>accept</code> 方法调用当前安全管理器。
 * <p>
 * 此方法调用 <code>checkPermission</code>，使用
 * <code>SocketPermission(host+":"+port,"accept")</code> 权限。
 * <p>
 * 如果你重写了此方法，则应在重写方法通常会抛出异常的地方调用
 * <code>super.checkAccept</code>。
 *
 * @param      host   套接字连接的主机名。
 * @param      port   套接字连接的端口号。
 * @exception  SecurityException  如果调用线程没有权限接受连接。
 * @exception  NullPointerException 如果 <code>host</code> 参数为
 *             <code>null</code>。
 * @see        java.net.ServerSocket#accept()
 * @see        #checkPermission(java.security.Permission) checkPermission
 */
public void checkAccept(String host, int port) {
    if (host == null) {
        throw new NullPointerException("host can't be null");
    }
    if (!host.startsWith("[") && host.indexOf(':') != -1) {
        host = "[" + host + "]";
    }
    checkPermission(new SocketPermission(host+":"+port,
        SecurityConstants.SOCKET_ACCEPT_ACTION));
}

/**
 * 如果调用线程不允许使用（加入/离开/发送/接收）IP 组播，则抛出 <code>SecurityException</code>。
 * <p>
 * 此方法调用 <code>checkPermission</code>，使用
 * <code>java.net.SocketPermission(maddr.getHostAddress(),
 * "accept,connect")</code> 权限。
 * <p>
 * 如果你重写了此方法，则应在重写方法通常会抛出异常的地方调用
 * <code>super.checkMulticast</code>。
 *
 * @param      maddr  要使用的 Internet 组地址。
 * @exception  SecurityException  如果调用线程不允许使用（加入/离开/发送/接收）IP 组播。
 * @exception  NullPointerException 如果地址参数为
 *             <code>null</code>。
 * @since      JDK1.1
 * @see        #checkPermission(java.security.Permission) checkPermission
 */
public void checkMulticast(InetAddress maddr) {
    String host = maddr.getHostAddress();
    if (!host.startsWith("[") && host.indexOf(':') != -1) {
        host = "[" + host + "]";
    }
    checkPermission(new SocketPermission(host,
        SecurityConstants.SOCKET_CONNECT_ACCEPT_ACTION));
}

                    /**
     * 如果调用线程不允许使用（加入/离开/发送/接收）IP组播，则抛出<code>SecurityException</code>。
     * <p>
     * 此方法调用<code>checkPermission</code>，使用
     * <code>java.net.SocketPermission(maddr.getHostAddress(),
     * "accept,connect")</code>权限。
     * <p>
     * 如果你重写了此方法，则应在重写方法通常会抛出异常的地方调用
     * <code>super.checkMulticast</code>。
     *
     * @param      maddr  要使用的互联网组地址。
     * @param      ttl        如果是组播发送，则使用此值。
     * 注意：此特定实现不使用ttl参数。
     * @exception  SecurityException  如果调用线程不允许使用（加入/离开/发送/接收）IP组播。
     * @exception  NullPointerException 如果地址参数为<code>null</code>。
     * @since      JDK1.1
     * @deprecated 使用#checkPermission(java.security.Permission)代替
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    @Deprecated
    public void checkMulticast(InetAddress maddr, byte ttl) {
        String host = maddr.getHostAddress();
        if (!host.startsWith("[") && host.indexOf(':') != -1) {
            host = "[" + host + "]";
        }
        checkPermission(new SocketPermission(host,
            SecurityConstants.SOCKET_CONNECT_ACCEPT_ACTION));
    }

    /**
     * 如果调用线程不允许访问或修改系统属性，则抛出<code>SecurityException</code>。
     * <p>
     * 此方法由类<code>System</code>的<code>getProperties</code>和
     * <code>setProperties</code>方法使用。
     * <p>
     * 此方法调用<code>checkPermission</code>，使用
     * <code>PropertyPermission("*", "read,write")</code>权限。
     * <p>
     * 如果你重写了此方法，则应在重写方法通常会抛出异常的地方调用
     * <code>super.checkPropertiesAccess</code>。
     * <p>
     *
     * @exception  SecurityException  如果调用线程没有权限访问或修改系统属性。
     * @see        java.lang.System#getProperties()
     * @see        java.lang.System#setProperties(java.util.Properties)
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkPropertiesAccess() {
        checkPermission(new PropertyPermission("*",
            SecurityConstants.PROPERTY_RW_ACTION));
    }

    /**
     * 如果调用线程不允许访问指定<code>key</code>名称的系统属性，则抛出<code>SecurityException</code>。
     * <p>
     * 此方法由类<code>System</code>的<code>getProperty</code>方法使用。
     * <p>
     * 此方法调用<code>checkPermission</code>，使用
     * <code>PropertyPermission(key, "read")</code>权限。
     * <p>
     * 如果你重写了此方法，则应在重写方法通常会抛出异常的地方调用
     * <code>super.checkPropertyAccess</code>。
     *
     * @param      key   系统属性键。
     *
     * @exception  SecurityException  如果调用线程没有权限访问指定的系统属性。
     * @exception  NullPointerException 如果<code>key</code>参数为<code>null</code>。
     * @exception  IllegalArgumentException 如果<code>key</code>为空。
     *
     * @see        java.lang.System#getProperty(java.lang.String)
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkPropertyAccess(String key) {
        checkPermission(new PropertyPermission(key,
            SecurityConstants.PROPERTY_READ_ACTION));
    }

    /**
     * 如果调用线程不受信任以显示由<code>window</code>参数指示的顶级窗口，则返回<code>false</code>。
     * 在这种情况下，调用者仍然可以决定显示窗口，但窗口应包含某种视觉警告。
     * 如果此方法返回<code>true</code>，则可以不受任何特殊限制地显示窗口。
     * <p>
     * 有关受信任和不受信任窗口的更多信息，请参见类<code>Window</code>。
     * <p>
     * 此方法调用
     * <code>checkPermission</code>，使用
     * <code>AWTPermission("showWindowWithoutWarningBanner")</code>权限，
     * 如果没有抛出<code>SecurityException</code>，则返回<code>true</code>，否则返回<code>false</code>。
     * 对于不包括{@code java.awt}包的Java SE子集配置文件，而是调用
     * <code>checkPermission</code>来检查权限{@code java.security.AllPermission}。
     * <p>
     * 如果你重写了此方法，则应在重写方法通常返回
     * <code>false</code>的地方调用<code>super.checkTopLevelWindow</code>，
     * 并应返回<code>super.checkTopLevelWindow</code>的值。
     *
     * @param      window   正在创建的新窗口。
     * @return     <code>true</code> 如果调用线程受信任以显示顶级窗口；否则返回<code>false</code>。
     * @exception  NullPointerException 如果<code>window</code>参数为<code>null</code>。
     * @deprecated 对{@code AWTPermission}的依赖性为Java平台的未来模块化创建了障碍。
     *             该方法的用户应直接调用
     *             {@link #checkPermission}。
     *             在未来的版本中，此方法将被更改为检查
     *             权限{@code java.security.AllPermission}。
     * @see        java.awt.Window
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    @Deprecated
    public boolean checkTopLevelWindow(Object window) {
        if (window == null) {
            throw new NullPointerException("window can't be null");
        }
        Permission perm = SecurityConstants.AWT.TOPLEVEL_WINDOW_PERMISSION;
        if (perm == null) {
            perm = SecurityConstants.ALL_PERMISSION;
        }
        try {
            checkPermission(perm);
            return true;
        } catch (SecurityException se) {
            // 只返回false
        }
        return false;
    }


                    /**
     * 如果调用线程不允许发起打印作业请求，则抛出 <code>SecurityException</code>。
     * <p>
     * 此方法使用 <code>RuntimePermission("queuePrintJob")</code> 权限调用
     * <code>checkPermission</code>。
     * <p>
     * 如果你重写了此方法，则应在重写方法通常会抛出异常的位置调用
     * <code>super.checkPrintJobAccess</code>。
     * <p>
     *
     * @exception  SecurityException  如果调用线程没有权限发起打印作业请求。
     * @since   JDK1.1
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkPrintJobAccess() {
        checkPermission(new RuntimePermission("queuePrintJob"));
    }

    /**
     * 如果调用线程不允许访问系统剪贴板，则抛出 <code>SecurityException</code>。
     * <p>
     * 此方法使用 <code>AWTPermission("accessClipboard")</code> 权限调用 <code>checkPermission</code>。
     * 对于不包含 {@code java.awt} 包的 Java SE 子集配置文件，<code>checkPermission</code> 而是用于检查
     * 权限 {@code java.security.AllPermission}。
     * <p>
     * 如果你重写了此方法，则应在重写方法通常会抛出异常的位置调用
     * <code>super.checkSystemClipboardAccess</code>。
     *
     * @since   JDK1.1
     * @exception  SecurityException  如果调用线程没有权限访问系统剪贴板。
     * @deprecated 对 {@code AWTPermission} 的依赖为 Java 平台的未来模块化创建了障碍。
     *             该方法的用户应直接调用 {@link #checkPermission}。
     *             该方法将在未来的版本中更改为检查权限 {@code java.security.AllPermission}。
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    @Deprecated
    public void checkSystemClipboardAccess() {
        Permission perm = SecurityConstants.AWT.ACCESS_CLIPBOARD_PERMISSION;
        if (perm == null) {
            perm = SecurityConstants.ALL_PERMISSION;
        }
        checkPermission(perm);
    }

    /**
     * 如果调用线程不允许访问 AWT 事件队列，则抛出 <code>SecurityException</code>。
     * <p>
     * 此方法使用 <code>AWTPermission("accessEventQueue")</code> 权限调用 <code>checkPermission</code>。
     * 对于不包含 {@code java.awt} 包的 Java SE 子集配置文件，<code>checkPermission</code> 而是用于检查
     * 权限 {@code java.security.AllPermission}。
     *
     * <p>
     * 如果你重写了此方法，则应在重写方法通常会抛出异常的位置调用
     * <code>super.checkAwtEventQueueAccess</code>。
     *
     * @since   JDK1.1
     * @exception  SecurityException  如果调用线程没有权限访问 AWT 事件队列。
     * @deprecated 对 {@code AWTPermission} 的依赖为 Java 平台的未来模块化创建了障碍。
     *             该方法的用户应直接调用 {@link #checkPermission}。
     *             该方法将在未来的版本中更改为检查权限 {@code java.security.AllPermission}。
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    @Deprecated
    public void checkAwtEventQueueAccess() {
        Permission perm = SecurityConstants.AWT.CHECK_AWT_EVENTQUEUE_PERMISSION;
        if (perm == null) {
            perm = SecurityConstants.ALL_PERMISSION;
        }
        checkPermission(perm);
    }

    /*
     * 我们为类变量提供了一个初始无效位（初始值为 false），这些变量告诉缓存是否有效。如果通过 setProperty() 更改了
     * java.security.Security 属性，Security 类将使用反射更改该变量，从而使缓存失效。
     *
     * 锁定由同步到 packageAccessLock/packageDefinitionLock 对象来处理。它们仅在此类中使用。
     *
     * 请注意，由于属性更改导致的缓存失效不会使用这些锁，因此在某个线程更新属性和其他线程更新缓存之间可能存在延迟。
     */
    private static boolean packageAccessValid = false;
    private static String[] packageAccess;
    private static final Object packageAccessLock = new Object();

    private static boolean packageDefinitionValid = false;
    private static String[] packageDefinition;
    private static final Object packageDefinitionLock = new Object();

    private static String[] getPackages(String p) {
        String packages[] = null;
        if (p != null && !p.equals("")) {
            java.util.StringTokenizer tok =
                new java.util.StringTokenizer(p, ",");
            int n = tok.countTokens();
            if (n > 0) {
                packages = new String[n];
                int i = 0;
                while (tok.hasMoreElements()) {
                    String s = tok.nextToken().trim();
                    packages[i++] = s;
                }
            }
        }

        if (packages == null)
            packages = new String[0];
        return packages;
    }

    /**
     * 如果调用线程不允许访问由参数指定的包，则抛出 <code>SecurityException</code>。
     * <p>
     * 此方法由类加载器的 <code>loadClass</code> 方法使用。
     * <p>
     * 此方法首先通过调用
     * <code>java.security.Security.getProperty("package.access")</code> 获取一个逗号分隔的列表，
     * 并检查 <code>pkg</code> 是否以任何受限制的包开头或等于它们。如果确实如此，则
     * <code>checkPermission</code> 将使用 <code>RuntimePermission("accessClassInPackage."+pkg)</code>
     * 权限调用。
     * <p>
     * 如果此方法被重写，则应在重写方法的第一行调用
     * <code>super.checkPackageAccess</code>。
     *
     * @param      pkg   包名。
     * @exception  SecurityException  如果调用线程没有权限访问指定的包。
     * @exception  NullPointerException 如果包名参数为 <code>null</code>。
     * @see        java.lang.ClassLoader#loadClass(java.lang.String, boolean)
     *  loadClass
     * @see        java.security.Security#getProperty getProperty
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkPackageAccess(String pkg) {
        if (pkg == null) {
            throw new NullPointerException("package name can't be null");
        }


                        String[] pkgs;
        synchronized (packageAccessLock) {
            /*
             * 我们需要更新我们的属性数组吗？
             */
            if (!packageAccessValid) {
                String tmpPropertyStr =
                    AccessController.doPrivileged(
                        new PrivilegedAction<String>() {
                            public String run() {
                                return java.security.Security.getProperty(
                                    "package.access");
                            }
                        }
                    );
                packageAccess = getPackages(tmpPropertyStr);
                packageAccessValid = true;
            }

            // 使用 packageAccess 的快照 -- 之后静态字段的变化无关紧要；数组内容不会改变。
            pkgs = packageAccess;
        }

        /*
         * 遍历包列表，检查是否有任何匹配项。
         */
        for (int i = 0; i < pkgs.length; i++) {
            if (pkg.startsWith(pkgs[i]) || pkgs[i].equals(pkg + ".")) {
                checkPermission(
                    new RuntimePermission("accessClassInPackage."+pkg));
                break;  // 无需继续；只需要检查一次
            }
        }
    }

    /**
     * 如果调用线程不允许在由参数指定的包中定义类，则抛出 <code>SecurityException</code>。
     * <p>
     * 一些类加载器的 <code>loadClass</code> 方法使用此方法。
     * <p>
     * 此方法首先通过调用 <code>java.security.Security.getProperty("package.definition")</code> 获取逗号分隔的列表来获取受限包列表，
     * 并检查 <code>pkg</code> 是否以或等于任何受限包。如果是，则调用 <code>checkPermission</code>，并传递
     * <code>RuntimePermission("defineClassInPackage."+pkg)</code> 权限。
     * <p>
     * 如果此方法被重写，则应在重写方法的第一行调用 <code>super.checkPackageDefinition</code>。
     *
     * @param      pkg   包名。
     * @exception  SecurityException  如果调用线程没有在指定包中定义类的权限。
     * @see        java.lang.ClassLoader#loadClass(java.lang.String, boolean)
     * @see        java.security.Security#getProperty getProperty
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkPackageDefinition(String pkg) {
        if (pkg == null) {
            throw new NullPointerException("package name can't be null");
        }

        String[] pkgs;
        synchronized (packageDefinitionLock) {
            /*
             * 我们需要更新我们的属性数组吗？
             */
            if (!packageDefinitionValid) {
                String tmpPropertyStr =
                    AccessController.doPrivileged(
                        new PrivilegedAction<String>() {
                            public String run() {
                                return java.security.Security.getProperty(
                                    "package.definition");
                            }
                        }
                    );
                packageDefinition = getPackages(tmpPropertyStr);
                packageDefinitionValid = true;
            }
            // 使用 packageDefinition 的快照 -- 之后静态字段的变化无关紧要；数组内容不会改变。
            pkgs = packageDefinition;
        }

        /*
         * 遍历包列表，检查是否有任何匹配项。
         */
        for (int i = 0; i < pkgs.length; i++) {
            if (pkg.startsWith(pkgs[i]) || pkgs[i].equals(pkg + ".")) {
                checkPermission(
                    new RuntimePermission("defineClassInPackage."+pkg));
                break; // 无需继续；只需要检查一次
            }
        }
    }

    /**
     * 如果调用线程不允许设置 <code>ServerSocket</code> 或 <code>Socket</code> 使用的套接字工厂，或 <code>URL</code> 使用的流处理器工厂，则抛出 <code>SecurityException</code>。
     * <p>
     * 此方法调用 <code>checkPermission</code>，并传递 <code>RuntimePermission("setFactory")</code> 权限。
     * <p>
     * 如果你重写此方法，则应在重写方法通常会抛出异常的地方调用 <code>super.checkSetFactory</code>。
     * <p>
     *
     * @exception  SecurityException  如果调用线程没有指定套接字工厂或流处理器工厂的权限。
     *
     * @see        java.net.ServerSocket#setSocketFactory(java.net.SocketImplFactory) setSocketFactory
     * @see        java.net.Socket#setSocketImplFactory(java.net.SocketImplFactory) setSocketImplFactory
     * @see        java.net.URL#setURLStreamHandlerFactory(java.net.URLStreamHandlerFactory) setURLStreamHandlerFactory
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkSetFactory() {
        checkPermission(new RuntimePermission("setFactory"));
    }

    /**
     * 如果调用线程不允许访问成员，则抛出 <code>SecurityException</code>。
     * <p>
     * 默认策略是允许访问 PUBLIC 成员，以及具有与调用者相同类加载器的类。在所有其他情况下，此方法调用 <code>checkPermission</code>，并传递 <code>RuntimePermission("accessDeclaredMembers")</code> 权限。
     * <p>
     * 如果此方法被重写，则不能调用 <code>super.checkMemberAccess</code>，因为 <code>checkMemberAccess</code> 的默认实现依赖于被检查的代码在调用堆栈深度为 4 的位置。
     *
     * @param clazz 要进行反射的类。
     *
     * @param which 访问类型，PUBLIC 或 DECLARED。
     *
     * @exception  SecurityException 如果调用者没有访问成员的权限。
     * @exception  NullPointerException 如果 <code>clazz</code> 参数为 <code>null</code>。
     *
     * @deprecated 此方法依赖于调用者在调用堆栈深度为 4 的位置，这是容易出错的，并且不能由运行时强制执行。此方法的用户应直接调用 {@link #checkPermission}。此方法将在未来的版本中更改，以检查权限 {@code java.security.AllPermission}。
     *
     * @see java.lang.reflect.Member
     * @since JDK1.1
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    @Deprecated
    @CallerSensitive
    public void checkMemberAccess(Class<?> clazz, int which) {
        if (clazz == null) {
            throw new NullPointerException("class can't be null");
        }
        if (which != Member.PUBLIC) {
            Class<?> stack[] = getClassContext();
            /*
             * 调用堆栈深度为 4 应该是调用 java.lang.Class 中某个反射 API 的调用者。堆栈应如下所示：
             *
             * someCaller                        [3]
             * java.lang.Class.someReflectionAPI [2]
             * java.lang.Class.checkMemberAccess [1]
             * SecurityManager.checkMemberAccess [0]
             *
             */
            if ((stack.length<4) ||
                (stack[3].getClassLoader() != clazz.getClassLoader())) {
                checkPermission(SecurityConstants.CHECK_MEMBER_ACCESS_PERMISSION);
            }
        }
    }

                    /**
     * 确定是否应该授予或拒绝具有指定权限目标名称的权限。
     *
     * <p> 如果请求的权限被允许，此方法将安静地返回。如果被拒绝，则抛出SecurityException。
     *
     * <p> 此方法为给定的权限目标名称创建一个<code>SecurityPermission</code>对象，并使用它调用<code>checkPermission</code>。
     *
     * <p> 有关可能的权限目标名称列表，请参阅
     * <code>{@link java.security.SecurityPermission}</code>的文档。
     *
     * <p> 如果你重写此方法，则应在重写的方法通常会抛出异常的地方调用
     * <code>super.checkSecurityAccess</code>。
     *
     * @param target <code>SecurityPermission</code>的目标名称。
     *
     * @exception SecurityException 如果调用线程没有请求访问的权限。
     * @exception NullPointerException 如果<code>target</code>为null。
     * @exception IllegalArgumentException 如果<code>target</code>为空。
     *
     * @since   JDK1.1
     * @see        #checkPermission(java.security.Permission) checkPermission
     */
    public void checkSecurityAccess(String target) {
        checkPermission(new SecurityPermission(target));
    }

    private native Class<?> currentLoadedClass0();

    /**
     * 返回在调用此方法时创建的任何新线程应实例化的线程组。
     * 默认情况下，它返回当前线程的线程组。特定的安全管理器应重写此方法以返回适当的线程组。
     *
     * @return  新线程实例化的ThreadGroup
     * @since   JDK1.1
     * @see     java.lang.ThreadGroup
     */
    public ThreadGroup getThreadGroup() {
        return Thread.currentThread().getThreadGroup();
    }

}
