
/*
 * 版权所有 (c) 2007, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.nio.channels;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.io.IOException;
import java.net.ProtocolFamily;             // javadoc
import java.net.StandardProtocolFamily;     // javadoc
import java.net.StandardSocketOptions;      // javadoc

/**
 * 支持 Internet 协议 (IP) 组播的网络通道。
 *
 * <p> IP 组播是指向由单个目标地址标识的 <em>组</em> 中的零个或多个主机传输 IP 数据报。
 *
 * <p> 对于 {@link StandardProtocolFamily#INET IPv4} 套接字的通道，底层操作系统支持 <a href="http://www.ietf.org/rfc/rfc2236.txt">
 * <i>RFC&nbsp;2236: Internet Group Management Protocol, Version 2 (IGMPv2)</i></a>。
 * 它可能可选地支持 <a href="http://www.ietf.org/rfc/rfc3376.txt"> <i>RFC&nbsp;3376: Internet Group
 * Management Protocol, Version 3 (IGMPv3)</i></a> 中指定的源过滤。
 * 对于 {@link StandardProtocolFamily#INET6 IPv6} 套接字的通道，等效标准是 <a href="http://www.ietf.org/rfc/rfc2710.txt"> <i>RFC&nbsp;2710:
 * Multicast Listener Discovery (MLD) for IPv6</i></a> 和 <a
 * href="http://www.ietf.org/rfc/rfc3810.txt"> <i>RFC&nbsp;3810: Multicast Listener
 * Discovery Version 2 (MLDv2) for IPv6</i></a>。
 *
 * <p> {@link #join(InetAddress,NetworkInterface)} 方法用于加入一个组并接收发送到该组的所有组播数据报。一个通道可以加入多个组播组，并且可以在多个
 * {@link NetworkInterface 接口} 上加入同一个组。通过调用返回的 {@link MembershipKey} 上的 {@link
 * MembershipKey#drop drop} 方法可以取消成员资格。如果底层平台支持源过滤，则可以使用 {@link MembershipKey#block
 * block} 和 {@link MembershipKey#unblock unblock} 方法来阻止或取消阻止来自特定源地址的组播数据报。
 *
 * <p> {@link #join(InetAddress,NetworkInterface,InetAddress)} 方法用于开始接收源地址匹配给定源地址的组的数据报。如果底层平台不支持源过滤，此方法将抛出 {@link UnsupportedOperationException}。
 * 成员资格是 <em>累积的</em>，可以再次调用此方法，使用相同的组和接口来允许接收来自其他源地址的数据报。该方法返回一个 {@link MembershipKey}，表示接收来自给定源地址的数据报的成员资格。调用该键的 {@link
 * MembershipKey#drop drop} 方法将取消成员资格，使得不再接收来自该源地址的数据报。
 *
 * <h2>平台依赖性</h2>
 *
 * 组播实现旨在直接映射到本机组播设施。因此，在开发接收 IP 组播数据报的应用程序时，应考虑以下事项：
 *
 * <ol>
 *
 * <li><p> 通道的创建应指定与通道将加入的组播组的地址类型相对应的 {@link ProtocolFamily}。没有保证一个协议族的套接字通道可以加入并接收地址对应于另一个协议族的组播组的数据报。例如，实现特定的是，一个 {@link StandardProtocolFamily#INET6 IPv6}
 * 套接字的通道是否可以加入一个 {@link StandardProtocolFamily#INET IPv4} 组播组并接收发送到该组的数据报。 </p></li>
 *
 * <li><p> 通道的套接字应绑定到 {@link
 * InetAddress#isAnyLocalAddress 通配符} 地址。如果套接字绑定到特定地址，而不是通配符地址，则实现特定的是套接字是否接收组播数据报。 </p></li>
 *
 * <li><p> 应在 {@link NetworkChannel#bind 绑定} 套接字之前启用 {@link StandardSocketOptions#SO_REUSEADDR SO_REUSEADDR} 选项。这是为了允许组的多个成员绑定到相同的
 * 地址。 </p></li>
 *
 * </ol>
 *
 * <p> <b>使用示例：</b>
 * <pre>
 *     // 在此接口上加入组播组，并使用此接口发送组播数据报
 *     NetworkInterface ni = NetworkInterface.getByName("hme0");
 *
 *     DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)
 *         .setOption(StandardSocketOptions.SO_REUSEADDR, true)
 *         .bind(new InetSocketAddress(5000))
 *         .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
 *
 *     InetAddress group = InetAddress.getByName("225.4.5.6");
 *
 *     MembershipKey key = dc.join(group, ni);
 * </pre>
 *
 * @since 1.7
 */

public interface MulticastChannel
    extends NetworkChannel
{
    /**
     * 关闭此通道。
     *
     * <p> 如果通道是组播组的成员，则成员资格将被 {@link MembershipKey#drop 取消}。返回时，{@link
     * MembershipKey 成员资格键} 将 {@link MembershipKey#isValid 无效}。
     *
     * <p> 此方法的行为与 {@link
     * Channel} 接口指定的行为完全相同。
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    @Override void close() throws IOException;

    /**
     * 加入组播组以开始接收发送到该组的所有数据报，返回成员资格键。
     *
     * <p> 如果此通道当前在给定接口上是组的成员以接收所有数据报，则返回表示该成员资格的成员资格键。否则，此通道将加入该组，并返回新的成员资格键。结果的成员资格键不是 {@link MembershipKey#sourceAddress 源特定的}。
     *
     * <p> 一个组播通道可以加入多个组播组，包括在多个接口上加入同一个组。实现可能对同时加入的组数施加限制。
     *
     * @param   group
     *          要加入的组播地址
     * @param   interf
     *          要加入组的网络接口
     *
     * @return  成员资格键
     *
     * @throws  IllegalArgumentException
     *          如果组参数不是 {@link InetAddress#isMulticastAddress
     *          组播} 地址，或者组参数的地址类型不受此通道支持
     * @throws  IllegalStateException
     *          如果通道已经在接口上具有该组的源特定成员资格
     * @throws  UnsupportedOperationException
     *          如果通道的套接字不是 Internet 协议套接字
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          如果设置了安全经理，并且其
     *          {@link SecurityManager#checkMulticast(InetAddress) checkMulticast}
     *          方法拒绝访问多播组
     */
    MembershipKey join(InetAddress group, NetworkInterface interf)
        throws IOException;

                /**
     * 加入一个组播组，以开始接收来自给定源地址发送到该组的数据报。
     *
     * <p> 如果此通道当前已经是该接口上的组成员，以接收来自给定源地址的数据报，则返回表示该成员资格的成员资格密钥。否则，此通道将加入该组，并返回新的成员资格密钥。生成的成员资格密钥是 {@link MembershipKey#sourceAddress
     * 源特定的}。
     *
     * <p> 成员资格是<em>累积的</em>，此方法可以再次使用相同的组和接口调用，以允许接收由其他源地址发送到该组的数据报。
     *
     * @param   group
     *          要加入的组播地址
     * @param   interf
     *          要加入组的网络接口
     * @param   source
     *          源地址
     *
     * @return  成员资格密钥
     *
     * @throws  IllegalArgumentException
     *          如果组参数不是 {@link
     *          InetAddress#isMulticastAddress 组播} 地址，源参数不是单播地址，组参数是此通道不支持的地址类型，或者源参数与组的地址类型不同
     * @throws  IllegalStateException
     *          如果通道当前已经是该接口上的组成员，以接收所有数据报
     * @throws  UnsupportedOperationException
     *          如果通道的套接字不是Internet协议套接字，或者不支持源过滤
     * @throws  ClosedChannelException
     *          如果此通道已关闭
     * @throws  IOException
     *          如果发生I/O错误
     * @throws  SecurityException
     *          如果设置了安全经理，并且其
     *          {@link SecurityManager#checkMulticast(InetAddress) checkMulticast}
     *          方法拒绝访问多播组
     */
    MembershipKey join(InetAddress group, NetworkInterface interf, InetAddress source)
        throws IOException;
}
