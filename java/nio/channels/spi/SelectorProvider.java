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

package java.nio.channels.spi;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.nio.channels.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.ServiceConfigurationError;
import sun.security.action.GetPropertyAction;


/**
 * 选择器和服务通道的服务提供者类。
 *
 * <p> 选择器提供者是此类的具体子类，具有无参数构造函数并实现了以下指定的抽象方法。给定的 Java 虚拟机调用将维护一个系统范围的默认提供者实例，该实例由 {@link
 * #provider() provider} 方法返回。该方法的第一次调用将根据以下指定定位默认提供者。
 *
 * <p> 系统范围的默认提供者由 {@link java.nio.channels.DatagramChannel#open
 * DatagramChannel}，{@link java.nio.channels.Pipe#open Pipe}，{@link
 * java.nio.channels.Selector#open Selector}，{@link
 * java.nio.channels.ServerSocketChannel#open ServerSocketChannel} 和 {@link
 * java.nio.channels.SocketChannel#open SocketChannel} 类的静态 <tt>open</tt>
 * 方法使用。它还用于 {@link java.lang.System#inheritedChannel System.inheritedChannel()}
 * 方法。程序可以通过实例化该提供者并直接调用此类中定义的 <tt>open</tt>
 * 方法来使用除默认提供者之外的提供者。
 *
 * <p> 本类中的所有方法都可安全地由多个并发线程使用。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
 * @since 1.4
 */

public abstract class SelectorProvider {

    private static final Object lock = new Object();
    private static SelectorProvider provider = null;

    /**
     * 初始化此类的新实例。
     *
     * @throws  SecurityException
     *          如果已安装了安全经理并且它拒绝
     *          {@link RuntimePermission}<tt>("selectorProvider")</tt>
     */
    protected SelectorProvider() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("selectorProvider"));
    }

    private static boolean loadProviderFromProperty() {
        String cn = System.getProperty("java.nio.channels.spi.SelectorProvider");
        if (cn == null)
            return false;
        try {
            Class<?> c = Class.forName(cn, true,
                                       ClassLoader.getSystemClassLoader());
            provider = (SelectorProvider)c.newInstance();
            return true;
        } catch (ClassNotFoundException x) {
            throw new ServiceConfigurationError(null, x);
        } catch (IllegalAccessException x) {
            throw new ServiceConfigurationError(null, x);
        } catch (InstantiationException x) {
            throw new ServiceConfigurationError(null, x);
        } catch (SecurityException x) {
            throw new ServiceConfigurationError(null, x);
        }
    }

    private static boolean loadProviderAsService() {

        ServiceLoader<SelectorProvider> sl =
            ServiceLoader.load(SelectorProvider.class,
                               ClassLoader.getSystemClassLoader());
        Iterator<SelectorProvider> i = sl.iterator();
        for (;;) {
            try {
                if (!i.hasNext())
                    return false;
                provider = i.next();
                return true;
            } catch (ServiceConfigurationError sce) {
                if (sce.getCause() instanceof SecurityException) {
                    // 忽略安全异常，尝试下一个提供者
                    continue;
                }
                throw sce;
            }
        }
    }

    /**
     * 返回此 Java 虚拟机调用的系统范围的默认选择器提供者。
     *
     * <p> 该方法的第一次调用按以下方式定位默认提供者对象： </p>
     *
     * <ol>
     *
     *   <li><p> 如果定义了系统属性
     *   <tt>java.nio.channels.spi.SelectorProvider</tt>，则将其视为具体提供者类的完全限定名。
     *   加载并实例化该类；如果此过程失败，则抛出未指定的错误。 </p></li>
     *
     *   <li><p> 如果在系统类加载器可见的 jar 文件中安装了提供者类，并且该 jar 文件包含名为
     *   <tt>java.nio.channels.spi.SelectorProvider</tt> 的提供者配置文件，在资源目录 <tt>META-INF/services</tt> 中，
     *   则采用该文件中指定的第一个类名。加载并实例化该类；如果此过程失败，则抛出未指定的错误。 </p></li>
     *
     *   <li><p> 最后，如果未通过上述任何方式指定提供者，则实例化系统默认提供者类并返回结果。 </p></li>
     *
     * </ol>
     *
     * <p> 该方法的后续调用返回第一次调用返回的提供者。 </p>
     *
     * @return  系统范围的默认选择器提供者
     */
    public static SelectorProvider provider() {
        synchronized (lock) {
            if (provider != null)
                return provider;
            return AccessController.doPrivileged(
                new PrivilegedAction<SelectorProvider>() {
                    public SelectorProvider run() {
                            if (loadProviderFromProperty())
                                return provider;
                            if (loadProviderAsService())
                                return provider;
                            provider = sun.nio.ch.DefaultSelectorProvider.create();
                            return provider;
                        }
                    });
        }
    }

}

                /**
     * 打开一个数据报通道。
     *
     * @return  新的通道
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract DatagramChannel openDatagramChannel()
        throws IOException;

    /**
     * 打开一个数据报通道。
     *
     * @param   family
     *          协议族
     *
     * @return  一个新的数据报通道
     *
     * @throws  UnsupportedOperationException
     *          如果指定的协议族不受支持
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @since 1.7
     */
    public abstract DatagramChannel openDatagramChannel(ProtocolFamily family)
        throws IOException;

    /**
     * 打开一个管道。
     *
     * @return  新的管道
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract Pipe openPipe()
        throws IOException;

    /**
     * 打开一个选择器。
     *
     * @return  新的选择器
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract AbstractSelector openSelector()
        throws IOException;

    /**
     * 打开一个服务器套接字通道。
     *
     * @return  新的通道
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract ServerSocketChannel openServerSocketChannel()
        throws IOException;

    /**
     * 打开一个套接字通道。
     *
     * @return  新的通道
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract SocketChannel openSocketChannel()
        throws IOException;

    /**
     * 返回从创建此 Java 虚拟机的实体继承的通道。
     *
     * <p> 在许多操作系统中，一个进程（如 Java 虚拟机）可以以一种允许该进程从创建该进程的实体继承通道的方式启动。这样做的方式是系统依赖的，连接的可能实体也是系统依赖的。例如，在 UNIX 系统上，Internet 服务守护进程（<i>inetd</i>）用于在关联的网络端口上有请求到达时启动程序以提供服务。在这个例子中，启动的进程继承了一个表示网络套接字的通道。
     *
     * <p> 如果继承的通道表示一个网络套接字，则此方法返回的 {@link java.nio.channels.Channel Channel} 类型如下确定：
     *
     * <ul>
     *
     *  <li><p> 如果继承的通道表示一个流导向的已连接套接字，则返回一个 {@link java.nio.channels.SocketChannel SocketChannel}。套接字通道至少最初处于阻塞模式，绑定到一个套接字地址，并连接到一个对等体。
     *  </p></li>
     *
     *  <li><p> 如果继承的通道表示一个流导向的监听套接字，则返回一个 {@link java.nio.channels.ServerSocketChannel
     *  ServerSocketChannel}。服务器套接字通道至少最初处于阻塞模式，并绑定到一个套接字地址。
     *  </p></li>
     *
     *  <li><p> 如果继承的通道是一个数据报套接字，则返回一个 {@link java.nio.channels.DatagramChannel DatagramChannel}。数据报通道至少最初处于阻塞模式，并绑定到一个套接字地址。
     *  </p></li>
     *
     * </ul>
     *
     * <p> 除了上述网络导向的通道外，此方法将来可能返回其他类型的通道。
     *
     * <p> 首次调用此方法会创建并返回该通道。后续调用此方法将返回同一个通道。 </p>
     *
     * @return  继承的通道，如果有的话，否则返回 <tt>null</tt>。
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  SecurityException
     *          如果已安装了安全管理者并且它拒绝
     *          {@link RuntimePermission}<tt>("inheritedChannel")</tt>
     *
     * @since 1.5
     */
   public Channel inheritedChannel() throws IOException {
        return null;
   }

}
