
/*
 * Copyright (c) 2000, 2018, Oracle and/or its affiliates. All rights reserved.
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

package java.net;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import sun.security.action.*;
import java.security.AccessController;

/**
 * 该类表示由名称和分配给此接口的IP地址列表组成的网络接口。
 * 它用于标识加入多播组的本地接口。
 *
 * 接口通常以“le0”之类的名称为人所知。
 *
 * @since 1.4
 */
public final class NetworkInterface {
    private String name;
    private String displayName;
    private int index;
    private InetAddress addrs[];
    private InterfaceAddress bindings[];
    private NetworkInterface childs[];
    private NetworkInterface parent = null;
    private boolean virtual = false;
    private static final NetworkInterface defaultInterface;
    private static final int defaultIndex; /* defaultInterface 的索引 */

    static {
        AccessController.doPrivileged(
            new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    System.loadLibrary("net");
                    return null;
                }
            });

        init();
        defaultInterface = DefaultInterface.getDefault();
        if (defaultInterface != null) {
            defaultIndex = defaultInterface.getIndex();
        } else {
            defaultIndex = 0;
        }
    }

    /**
     * 返回一个索引设置为0且名称为null的NetworkInterface对象。
     * 在MulticastSocket上设置这样的接口将导致内核选择一个接口来发送多播数据包。
     *
     */
    NetworkInterface() {
    }

    NetworkInterface(String name, int index, InetAddress[] addrs) {
        this.name = name;
        this.index = index;
        this.addrs = addrs;
    }

    /**
     * 获取此网络接口的名称。
     *
     * @return 此网络接口的名称
     */
    public String getName() {
            return name;
    }

    /**
     * 返回一个包含此网络接口绑定的所有或部分InetAddress的Enumeration。
     * <p>
     * 如果存在安全管理器，其 {@code checkConnect} 方法将针对每个InetAddress调用。
     * 只有 {@code checkConnect} 不抛出SecurityException的InetAddress才会包含在Enumeration中。
     * 但是，如果调用者具有 {@link NetPermission}("getNetworkInformation") 权限，则返回所有InetAddress。
     * @return 一个包含此网络接口绑定的所有或部分InetAddress的Enumeration对象
     */
    public Enumeration<InetAddress> getInetAddresses() {

        class checkedAddresses implements Enumeration<InetAddress> {

            private int i=0, count=0;
            private InetAddress local_addrs[];

            checkedAddresses() {
                local_addrs = new InetAddress[addrs.length];
                boolean trusted = true;

                SecurityManager sec = System.getSecurityManager();
                if (sec != null) {
                    try {
                        sec.checkPermission(new NetPermission("getNetworkInformation"));
                    } catch (SecurityException e) {
                        trusted = false;
                    }
                }
                for (int j=0; j<addrs.length; j++) {
                    try {
                        if (sec != null && !trusted) {
                            sec.checkConnect(addrs[j].getHostAddress(), -1);
                        }
                        local_addrs[count++] = addrs[j];
                    } catch (SecurityException e) { }
                }

            }

            public InetAddress nextElement() {
                if (i < count) {
                    return local_addrs[i++];
                } else {
                    throw new NoSuchElementException();
                }
            }

            public boolean hasMoreElements() {
                return (i < count);
            }
        }
        return new checkedAddresses();

    }

    /**
     * 获取此网络接口的所有或部分 {@code InterfaceAddresses} 的列表。
     * <p>
     * 如果存在安全管理器，其 {@code checkConnect} 方法将针对每个InterfaceAddress的InetAddress调用。
     * 只有 {@code checkConnect} 不抛出SecurityException的InterfaceAddress才会包含在列表中。
     *
     * @return 一个包含此网络接口的所有或部分InterfaceAddress的 {@code List} 对象
     * @since 1.6
     */
    public java.util.List<InterfaceAddress> getInterfaceAddresses() {
        java.util.List<InterfaceAddress> lst = new java.util.ArrayList<InterfaceAddress>(1);
        SecurityManager sec = System.getSecurityManager();
        for (int j=0; j<bindings.length; j++) {
            try {
                if (sec != null) {
                    sec.checkConnect(bindings[j].getAddress().getHostAddress(), -1);
                }
                lst.add(bindings[j]);
            } catch (SecurityException e) { }
        }
        return lst;
    }

    /**
     * 获取此网络接口的所有子接口（也称为虚拟接口）的Enumeration。
     * <p>
     * 例如，eth0:1 将是 eth0 的子接口。
     *
     * @return 一个包含此网络接口的所有子接口的Enumeration对象
     * @since 1.6
     */
    public Enumeration<NetworkInterface> getSubInterfaces() {
        class subIFs implements Enumeration<NetworkInterface> {

            private int i=0;

            subIFs() {
            }

            public NetworkInterface nextElement() {
                if (i < childs.length) {
                    return childs[i++];
                } else {
                    throw new NoSuchElementException();
                }
            }


                        public boolean hasMoreElements() {
                return (i < childs.length);
            }
        }
        return new subIFs();

    }

    /**
     * 返回此接口的父 NetworkInterface，如果这是一个子接口，则返回父接口；
     * 如果这是一个物理（非虚拟）接口或没有父接口，则返回 {@code null}。
     *
     * @return 该接口所附加的 {@code NetworkInterface}。
     * @since 1.6
     */
    public NetworkInterface getParent() {
        return parent;
    }

    /**
     * 返回此网络接口的索引。索引是一个大于或等于零的整数，或对于未知的接口返回 {@code -1}。
     * 这是一个系统特定的值，具有相同名称的接口在不同的机器上可以有不同的索引。
     *
     * @return 此网络接口的索引，如果索引未知则返回 {@code -1}
     * @see #getByIndex(int)
     * @since 1.7
     */
    public int getIndex() {
        return index;
    }

    /**
     * 获取此网络接口的显示名称。
     * 显示名称是描述网络设备的人类可读字符串。
     *
     * @return 一个非空字符串，表示此网络接口的显示名称，如果没有可用的显示名称，则返回 null。
     */
    public String getDisplayName() {
        /* 严格的 TCK 符合性 */
        return "".equals(displayName) ? null : displayName;
    }

    /**
     * 搜索具有指定名称的网络接口。
     *
     * @param   name
     *          网络接口的名称。
     *
     * @return  具有指定名称的 {@code NetworkInterface}，
     *          如果没有具有指定名称的网络接口，则返回 {@code null}。
     *
     * @throws  SocketException
     *          如果发生 I/O 错误。
     *
     * @throws  NullPointerException
     *          如果指定的名称为 {@code null}。
     */
    public static NetworkInterface getByName(String name) throws SocketException {
        if (name == null)
            throw new NullPointerException();
        return getByName0(name);
    }

    /**
     * 根据索引获取网络接口。
     *
     * @param index 一个整数，表示接口的索引
     * @return 从其索引获取的 NetworkInterface，如果系统中没有这样的接口，则返回 {@code null}
     * @throws  SocketException  如果发生 I/O 错误。
     * @throws  IllegalArgumentException 如果索引为负数
     * @see #getIndex()
     * @since 1.7
     */
    public static NetworkInterface getByIndex(int index) throws SocketException {
        if (index < 0)
            throw new IllegalArgumentException("Interface index can't be negative");
        return getByIndex0(index);
    }

    /**
     * 用于搜索具有指定 Internet 协议 (IP) 地址的网络接口的便捷方法。
     * <p>
     * 如果指定的 IP 地址绑定到多个网络接口，则返回哪个网络接口是未定义的。
     *
     * @param   addr
     *          要搜索的 {@code InetAddress}。
     *
     * @return  一个 {@code NetworkInterface}
     *          或者如果没有任何网络接口具有指定的 IP 地址，则返回 {@code null}。
     *
     * @throws  SocketException
     *          如果发生 I/O 错误。
     *
     * @throws  NullPointerException
     *          如果指定的地址为 {@code null}。
     */
    public static NetworkInterface getByInetAddress(InetAddress addr) throws SocketException {
        if (addr == null) {
            throw new NullPointerException();
        }
        if (addr instanceof Inet4Address) {
            Inet4Address inet4Address = (Inet4Address) addr;
            if (inet4Address.holder.family != InetAddress.IPv4) {
                throw new IllegalArgumentException("invalid family type: "
                        + inet4Address.holder.family);
            }
        } else if (addr instanceof Inet6Address) {
            Inet6Address inet6Address = (Inet6Address) addr;
            if (inet6Address.holder.family != InetAddress.IPv6) {
                throw new IllegalArgumentException("invalid family type: "
                        + inet6Address.holder.family);
            }
        } else {
            throw new IllegalArgumentException("invalid address type: " + addr);
        }
        return getByInetAddress0(addr);
    }

    /**
     * 返回此机器上的所有接口。{@code Enumeration} 至少包含一个元素，可能表示一个仅支持此机器内实体间通信的回环接口。
     *
     * 注意：可以使用 getNetworkInterfaces()+getInetAddresses()
     *       来获取此节点的所有 IP 地址
     *
     * @return 在此机器上找到的 NetworkInterfaces 的 Enumeration
     * @exception  SocketException  如果发生 I/O 错误。
     */

    public static Enumeration<NetworkInterface> getNetworkInterfaces()
        throws SocketException {
        final NetworkInterface[] netifs = getAll();

        // 如果没有网络接口，则指定返回 null
        if (netifs == null)
            return null;

        return new Enumeration<NetworkInterface>() {
            private int i = 0;
            public NetworkInterface nextElement() {
                if (netifs != null && i < netifs.length) {
                    NetworkInterface netif = netifs[i++];
                    return netif;
                } else {
                    throw new NoSuchElementException();
                }
            }

            public boolean hasMoreElements() {
                return (netifs != null && i < netifs.length);
            }
        };
    }

    private native static NetworkInterface[] getAll()
        throws SocketException;

    private native static NetworkInterface getByName0(String name)
        throws SocketException;

    private native static NetworkInterface getByIndex0(int index)
        throws SocketException;


                private native static NetworkInterface getByInetAddress0(InetAddress addr)
        throws SocketException;

    /**
     * 返回网络接口是否处于活动状态。
     *
     * @return  如果接口处于活动状态，则返回 {@code true}。
     * @exception       SocketException 如果发生 I/O 错误。
     * @since 1.6
     */

    public boolean isUp() throws SocketException {
        return isUp0(name, index);
    }

    /**
     * 返回网络接口是否为回环接口。
     *
     * @return  如果接口为回环接口，则返回 {@code true}。
     * @exception       SocketException 如果发生 I/O 错误。
     * @since 1.6
     */

    public boolean isLoopback() throws SocketException {
        return isLoopback0(name, index);
    }

    /**
     * 返回网络接口是否为点对点接口。
     * 典型的点对点接口是通过调制解调器的 PPP 连接。
     *
     * @return  如果接口为点对点接口，则返回 {@code true}。
     * @exception       SocketException 如果发生 I/O 错误。
     * @since 1.6
     */

    public boolean isPointToPoint() throws SocketException {
        return isP2P0(name, index);
    }

    /**
     * 返回网络接口是否支持多播。
     *
     * @return  如果接口支持多播，则返回 {@code true}。
     * @exception       SocketException 如果发生 I/O 错误。
     * @since 1.6
     */

    public boolean supportsMulticast() throws SocketException {
        return supportsMulticast0(name, index);
    }

    /**
     * 返回接口的硬件地址（通常是 MAC 地址），如果接口有硬件地址且当前权限允许访问，则返回。
     * 如果设置了安全经理，则调用者必须具有 {@link NetPermission}("getNetworkInformation") 权限。
     *
     * @return  包含地址的字节数组，如果地址不存在、不可访问或设置了安全经理且调用者没有 NetPermission("getNetworkInformation") 权限，则返回 {@code null}。
     *
     * @exception       SocketException 如果发生 I/O 错误。
     * @since 1.6
     */
    public byte[] getHardwareAddress() throws SocketException {
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            try {
                sec.checkPermission(new NetPermission("getNetworkInformation"));
            } catch (SecurityException e) {
                if (!getInetAddresses().hasMoreElements()) {
                    // 没有连接到任何本地地址的权限
                    return null;
                }
            }
        }
        for (InetAddress addr : addrs) {
            if (addr instanceof Inet4Address) {
                return getMacAddr0(((Inet4Address)addr).getAddress(), name, index);
            }
        }
        return getMacAddr0(null, name, index);
    }

    /**
     * 返回此接口的最大传输单元 (MTU)。
     *
     * @return  该接口的 MTU 值。
     * @exception       SocketException 如果发生 I/O 错误。
     * @since 1.6
     */
    public int getMTU() throws SocketException {
        return getMTU0(name, index);
    }

    /**
     * 返回此接口是否为虚拟接口（也称为子接口）。
     * 在某些系统中，虚拟接口是作为物理接口的子接口创建的，并赋予不同的设置（如地址或 MTU）。通常，接口的名称会是父接口的名称后跟一个冒号 (:) 和一个标识子接口的数字，因为一个物理接口可以有多个虚拟接口。
     *
     * @return 如果此接口是虚拟接口，则返回 {@code true}。
     * @since 1.6
     */
    public boolean isVirtual() {
        return virtual;
    }

    private native static boolean isUp0(String name, int ind) throws SocketException;
    private native static boolean isLoopback0(String name, int ind) throws SocketException;
    private native static boolean supportsMulticast0(String name, int ind) throws SocketException;
    private native static boolean isP2P0(String name, int ind) throws SocketException;
    private native static byte[] getMacAddr0(byte[] inAddr, String name, int ind) throws SocketException;
    private native static int getMTU0(String name, int ind) throws SocketException;

    /**
     * 将此对象与指定对象进行比较。
     * 如果且仅当参数不为 {@code null} 并且表示与该对象相同的 NetworkInterface 时，结果为 {@code true}。
     * <p>
     * 两个 {@code NetworkInterface} 实例表示相同的 NetworkInterface，当且仅当它们的名称和地址相同。
     *
     * @param   obj   要比较的对象。
     * @return  如果对象相同，则返回 {@code true}；否则返回 {@code false}。
     * @see     java.net.InetAddress#getAddress()
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof NetworkInterface)) {
            return false;
        }
        NetworkInterface that = (NetworkInterface)obj;
        if (this.name != null ) {
            if (!this.name.equals(that.name)) {
                return false;
            }
        } else {
            if (that.name != null) {
                return false;
            }
        }

        if (this.addrs == null) {
            return that.addrs == null;
        } else if (that.addrs == null) {
            return false;
        }

        /* 两个地址数组都不为空。比较地址数量 */

        if (this.addrs.length != that.addrs.length) {
            return false;
        }

        InetAddress[] thatAddrs = that.addrs;
        int count = thatAddrs.length;

        for (int i=0; i<count; i++) {
            boolean found = false;
            for (int j=0; j<count; j++) {
                if (addrs[i].equals(thatAddrs[j])) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }


                public int hashCode() {
        return name == null? 0: name.hashCode();
    }

    public String toString() {
        String result = "name:";
        result += name == null? "null": name;
        if (displayName != null) {
            result += " (" + displayName + ")";
        }
        return result;
    }

    private static native void init();

    /**
     * 返回此系统的默认网络接口
     *
     * @return 默认接口
     */
    static NetworkInterface getDefault() {
        return defaultInterface;
    }
}
