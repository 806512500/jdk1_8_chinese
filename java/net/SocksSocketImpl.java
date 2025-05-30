
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
package java.net;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import sun.net.SocksProxy;
import sun.net.www.ParseUtil;
/* import org.ietf.jgss.*; */

/**
 * SOCKS (V4 & V5) TCP 套接字实现 (RFC 1928)。
 * 这是 PlainSocketImpl 的子类。
 * 注意这个类 <b>不应该</b> 是公共的。
 */

class SocksSocketImpl extends PlainSocketImpl implements SocksConsts {
    private String server = null;
    private int serverPort = DEFAULT_PORT;
    private InetSocketAddress external_address;
    private boolean useV4 = false;
    private Socket cmdsock = null;
    private InputStream cmdIn = null;
    private OutputStream cmdOut = null;
    /* 如果代理是通过程序设置的，则为 true */
    private boolean applicationSetProxy;  /* false */


    SocksSocketImpl() {
        // 无需任何操作
    }

    SocksSocketImpl(String server, int port) {
        this.server = server;
        this.serverPort = (port == -1 ? DEFAULT_PORT : port);
    }

    SocksSocketImpl(Proxy proxy) {
        SocketAddress a = proxy.address();
        if (a instanceof InetSocketAddress) {
            InetSocketAddress ad = (InetSocketAddress) a;
            // 使用 getHostString() 以避免反向查找
            server = ad.getHostString();
            serverPort = ad.getPort();
        }
    }

    void setV4() {
        useV4 = true;
    }

    private synchronized void privilegedConnect(final String host,
                                              final int port,
                                              final int timeout)
         throws IOException
    {
        try {
            AccessController.doPrivileged(
                new java.security.PrivilegedExceptionAction<Void>() {
                    public Void run() throws IOException {
                              superConnectServer(host, port, timeout);
                              cmdIn = getInputStream();
                              cmdOut = getOutputStream();
                              return null;
                          }
                      });
        } catch (java.security.PrivilegedActionException pae) {
            throw (IOException) pae.getException();
        }
    }

    private void superConnectServer(String host, int port,
                                    int timeout) throws IOException {
        super.connect(new InetSocketAddress(host, port), timeout);
    }

    private static int remainingMillis(long deadlineMillis) throws IOException {
        if (deadlineMillis == 0L)
            return 0;

        final long remaining = deadlineMillis - System.currentTimeMillis();
        if (remaining > 0)
            return (int) remaining;

        throw new SocketTimeoutException();
    }

    private int readSocksReply(InputStream in, byte[] data) throws IOException {
        return readSocksReply(in, data, 0L);
    }

    private int readSocksReply(InputStream in, byte[] data, long deadlineMillis) throws IOException {
        int len = data.length;
        int received = 0;
        while (received < len) {
            int count;
            try {
                count = ((SocketInputStream)in).read(data, received, len - received, remainingMillis(deadlineMillis));
            } catch (SocketTimeoutException e) {
                throw new SocketTimeoutException("连接超时");
            }
            if (count < 0)
                throw new SocketException("来自 SOCKS 服务器的响应格式错误");
            received += count;
        }
        return received;
    }

    /**
     * 提供代理所需的认证机制。
     */
    private boolean authenticate(byte method, InputStream in,
                                 BufferedOutputStream out) throws IOException {
        return authenticate(method, in, out, 0L);
    }

    private boolean authenticate(byte method, InputStream in,
                                 BufferedOutputStream out,
                                 long deadlineMillis) throws IOException {
        // 不需要认证。我们完成了！
        if (method == NO_AUTH)
            return true;
        /**
         * 用户/密码认证。按以下顺序尝试：
         * - 应用程序提供的 Authenticator（如果有）
         * - 用户名和无密码（向后兼容行为）。
         */
        if (method == USER_PASSW) {
            String userName;
            String password = null;
            final InetAddress addr = InetAddress.getByName(server);
            PasswordAuthentication pw =
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<PasswordAuthentication>() {
                        public PasswordAuthentication run() {
                                return Authenticator.requestPasswordAuthentication(
                                       server, addr, serverPort, "SOCKS5", "SOCKS 认证", null);
                            }
                        });
            if (pw != null) {
                userName = pw.getUserName();
                password = new String(pw.getPassword());
            } else {
                userName = java.security.AccessController.doPrivileged(
                        new sun.security.action.GetPropertyAction("user.name"));
            }
            if (userName == null)
                return false;
            out.write(1);
            out.write(userName.length());
            try {
                out.write(userName.getBytes("ISO-8859-1"));
            } catch (java.io.UnsupportedEncodingException uee) {
                assert false;
            }
            if (password != null) {
                out.write(password.length());
                try {
                    out.write(password.getBytes("ISO-8859-1"));
                } catch (java.io.UnsupportedEncodingException uee) {
                    assert false;
                }
            } else
                out.write(0);
            out.flush();
            byte[] data = new byte[2];
            int i = readSocksReply(in, data, deadlineMillis);
            if (i != 2 || data[1] != 0) {
                /* RFC 1929 指定如果认证失败，连接必须关闭 */
                out.close();
                in.close();
                return false;
            }
            /* 认证成功 */
            return true;
        }
        /**
         * GSSAPI 认证机制。
         * 不幸的是，RFC 似乎与参考实现不同步。我将保留此代码以供将来完善。
         */
//      if (method == GSSAPI) {
//          try {
//              GSSManager manager = GSSManager.getInstance();
//              GSSName name = manager.createName("SERVICE:socks@"+server,
//                                                   null);
//              GSSContext context = manager.createContext(name, null, null,
//                                                         GSSContext.DEFAULT_LIFETIME);
//              context.requestMutualAuth(true);
//              context.requestReplayDet(true);
//              context.requestSequenceDet(true);
//              context.requestCredDeleg(true);
//              byte []inToken = new byte[0];
//              while (!context.isEstablished()) {
//                  byte[] outToken
//                      = context.initSecContext(inToken, 0, inToken.length);
//                  // 如果生成了输出令牌，则发送
//                  if (outToken != null) {
//                      out.write(1);
//                      out.write(1);
//                      out.writeShort(outToken.length);
//                      out.write(outToken);
//                      out.flush();
//                      data = new byte[2];
//                      i = readSocksReply(in, data, deadlineMillis);
//                      if (i != 2 || data[1] == 0xff) {
//                          in.close();
//                          out.close();
//                          return false;
//                      }
//                      i = readSocksReply(in, data, deadlineMillis);
//                      int len = 0;
//                      len = ((int)data[0] & 0xff) << 8;
//                      len += data[1];
//                      data = new byte[len];
//                      i = readSocksReply(in, data, deadlineMillis);
//                      if (i == len)
//                          return true;
//                      in.close();
//                      out.close();
//                  }
//              }
//          } catch (GSSException e) {
//              /* RFC 1961 指定如果上下文初始化失败，连接必须关闭 */
//              e.printStackTrace();
//              in.close();
//              out.close();
//          }
//      }
        return false;
    }

    private void connectV4(InputStream in, OutputStream out,
                           InetSocketAddress endpoint,
                           long deadlineMillis) throws IOException {
        if (!(endpoint.getAddress() instanceof Inet4Address)) {
            throw new SocketException("SOCKS V4 仅支持 IPv4 地址");
        }
        out.write(PROTO_VERS4);
        out.write(CONNECT);
        out.write((endpoint.getPort() >> 8) & 0xff);
        out.write((endpoint.getPort() >> 0) & 0xff);
        out.write(endpoint.getAddress().getAddress());
        String userName = getUserName();
        try {
            out.write(userName.getBytes("ISO-8859-1"));
        } catch (java.io.UnsupportedEncodingException uee) {
            assert false;
        }
        out.write(0);
        out.flush();
        byte[] data = new byte[8];
        int n = readSocksReply(in, data, deadlineMillis);
        if (n != 8)
            throw new SocketException("来自 SOCKS 服务器的响应长度错误: " + n);
        if (data[0] != 0 && data[0] != 4)
            throw new SocketException("来自 SOCKS 服务器的响应版本错误");
        SocketException ex = null;
        switch (data[1]) {
        case 90:
            // 成功！
            external_address = endpoint;
            break;
        case 91:
            ex = new SocketException("SOCKS 请求被拒绝");
            break;
        case 92:
            ex = new SocketException("SOCKS 服务器无法到达目标");
            break;
        case 93:
            ex = new SocketException("SOCKS 认证失败");
            break;
        default:
            ex = new SocketException("来自 SOCKS 服务器的响应状态错误");
            break;
        }
        if (ex != null) {
            in.close();
            out.close();
            throw ex;
        }
    }

    /**
     * 将 Socks 套接字连接到指定的端点。它将首先连接到 SOCKS 代理并协商访问。如果代理允许连接，则连接成功，所有后续流量将发送到“实际”端点。
     *
     * @param   endpoint        要连接的 {@code SocketAddress}。
     * @param   timeout         超时值（以毫秒为单位）
     * @throws  IOException     如果无法建立连接。
     * @throws  SecurityException 如果存在安全经理并且它不允许连接
     * @throws  IllegalArgumentException 如果 endpoint 为 null 或是此套接字不支持的 SocketAddress 子类
     */
    @Override
    protected void connect(SocketAddress endpoint, int timeout) throws IOException {
        final long deadlineMillis;

        if (timeout == 0) {
            deadlineMillis = 0L;
        } else {
            long finish = System.currentTimeMillis() + timeout;
            deadlineMillis = finish < 0 ? Long.MAX_VALUE : finish;
        }

        SecurityManager security = System.getSecurityManager();
        if (endpoint == null || !(endpoint instanceof InetSocketAddress))
            throw new IllegalArgumentException("不支持的地址类型");
        InetSocketAddress epoint = (InetSocketAddress) endpoint;
        if (security != null) {
            if (epoint.isUnresolved())
                security.checkConnect(epoint.getHostName(),
                                      epoint.getPort());
            else
                security.checkConnect(epoint.getAddress().getHostAddress(),
                                      epoint.getPort());
        }
        if (server == null) {
            // 这是一般情况
            // 仅当套接字是使用指定的代理创建时，server 才不为 null，此时它会绕过 ProxySelector
            ProxySelector sel = java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<ProxySelector>() {
                    public ProxySelector run() {
                            return ProxySelector.getDefault();
                        }
                    });
            if (sel == null) {
                /*
                 * 没有默认的代理选择器 --> 直接连接
                 */
                super.connect(epoint, remainingMillis(deadlineMillis));
                return;
            }
            URI uri;
            // 使用 getHostString() 以避免反向查找
            String host = epoint.getHostString();
            // IPv6 字面量？
            if (epoint.getAddress() instanceof Inet6Address &&
                (!host.startsWith("[")) && (host.indexOf(":") >= 0)) {
                host = "[" + host + "]";
            }
            try {
                uri = new URI("socket://" + ParseUtil.encodePath(host) + ":"+ epoint.getPort());
            } catch (URISyntaxException e) {
                // 这不应该发生
                assert false : e;
                uri = null;
            }
            Proxy p = null;
            IOException savedExc = null;
            java.util.Iterator<Proxy> iProxy = null;
            iProxy = sel.select(uri).iterator();
            if (iProxy == null || !(iProxy.hasNext())) {
                super.connect(epoint, remainingMillis(deadlineMillis));
                return;
            }
            while (iProxy.hasNext()) {
                p = iProxy.next();
                if (p == null || p.type() != Proxy.Type.SOCKS) {
                    super.connect(epoint, remainingMillis(deadlineMillis));
                    return;
                }


                            if (!(p.address() instanceof InetSocketAddress))
                    throw new SocketException("未知的代理地址类型: " + p);
                // 使用 getHostString() 以避免反向查找
                server = ((InetSocketAddress) p.address()).getHostString();
                serverPort = ((InetSocketAddress) p.address()).getPort();
                if (p instanceof SocksProxy) {
                    if (((SocksProxy)p).protocolVersion() == 4) {
                        useV4 = true;
                    }
                }

                // 连接到 SOCKS 服务器
                try {
                    privilegedConnect(server, serverPort, remainingMillis(deadlineMillis));
                    // 成功了，让我们离开这里
                    break;
                } catch (IOException e) {
                    // 哦，通知 ProxySelector
                    sel.connectFailed(uri,p.address(),e);
                    server = null;
                    serverPort = -1;
                    savedExc = e;
                    // 继续 while 循环并尝试下一个代理
                }
            }

            /*
             * 如果此时 server 仍为 null，说明没有代理成功
             */
            if (server == null) {
                throw new SocketException("无法连接到 SOCKS 代理:"
                                          + savedExc.getMessage());
            }
        } else {
            // 连接到 SOCKS 服务器
            try {
                privilegedConnect(server, serverPort, remainingMillis(deadlineMillis));
            } catch (IOException e) {
                throw new SocketException(e.getMessage());
            }
        }

        // cmdIn & cmdOut 在 privilegedConnect() 调用期间被初始化
        BufferedOutputStream out = new BufferedOutputStream(cmdOut, 512);
        InputStream in = cmdIn;

        if (useV4) {
            // SOCKS 协议版本 4 不知道如何处理
            // 未解析的地址（DOMAIN 类型的地址）
            if (epoint.isUnresolved())
                throw new UnknownHostException(epoint.toString());
            connectV4(in, out, epoint, deadlineMillis);
            return;
        }

        // 这是 SOCKS V5
        out.write(PROTO_VERS);
        out.write(2);
        out.write(NO_AUTH);
        out.write(USER_PASSW);
        out.flush();
        byte[] data = new byte[2];
        int i = readSocksReply(in, data, deadlineMillis);
        if (i != 2 || ((int)data[0]) != PROTO_VERS) {
            // 可能不是 V5 服务器
            // 在放弃之前尝试 V4
            // SOCKS 协议版本 4 不知道如何处理
            // 未解析的地址（DOMAIN 类型的地址）
            if (epoint.isUnresolved())
                throw new UnknownHostException(epoint.toString());
            connectV4(in, out, epoint, deadlineMillis);
            return;
        }
        if (((int)data[1]) == NO_METHODS)
            throw new SocketException("SOCKS : 没有可接受的方法");
        if (!authenticate(data[1], in, out, deadlineMillis)) {
            throw new SocketException("SOCKS : 认证失败");
        }
        out.write(PROTO_VERS);
        out.write(CONNECT);
        out.write(0);
        /* 测试 IPV4/IPV6/未解析 */
        if (epoint.isUnresolved()) {
            out.write(DOMAIN_NAME);
            out.write(epoint.getHostName().length());
            try {
                out.write(epoint.getHostName().getBytes("ISO-8859-1"));
            } catch (java.io.UnsupportedEncodingException uee) {
                assert false;
            }
            out.write((epoint.getPort() >> 8) & 0xff);
            out.write((epoint.getPort() >> 0) & 0xff);
        } else if (epoint.getAddress() instanceof Inet6Address) {
            out.write(IPV6);
            out.write(epoint.getAddress().getAddress());
            out.write((epoint.getPort() >> 8) & 0xff);
            out.write((epoint.getPort() >> 0) & 0xff);
        } else {
            out.write(IPV4);
            out.write(epoint.getAddress().getAddress());
            out.write((epoint.getPort() >> 8) & 0xff);
            out.write((epoint.getPort() >> 0) & 0xff);
        }
        out.flush();
        data = new byte[4];
        i = readSocksReply(in, data, deadlineMillis);
        if (i != 4)
            throw new SocketException("来自 SOCKS 服务器的回复长度错误");
        SocketException ex = null;
        int len;
        byte[] addr;
        switch (data[1]) {
        case REQUEST_OK:
            // 成功！
            switch(data[3]) {
            case IPV4:
                addr = new byte[4];
                i = readSocksReply(in, addr, deadlineMillis);
                if (i != 4)
                    throw new SocketException("来自 SOCKS 服务器的回复格式错误");
                data = new byte[2];
                i = readSocksReply(in, data, deadlineMillis);
                if (i != 2)
                    throw new SocketException("来自 SOCKS 服务器的回复格式错误");
                break;
            case DOMAIN_NAME:
                byte[] lenByte = new byte[1];
                i = readSocksReply(in, lenByte, deadlineMillis);
                if (i != 1)
                    throw new SocketException("来自 SOCKS 服务器的回复格式错误");
                len = lenByte[0] & 0xFF;
                byte[] host = new byte[len];
                i = readSocksReply(in, host, deadlineMillis);
                if (i != len)
                    throw new SocketException("来自 SOCKS 服务器的回复格式错误");
                data = new byte[2];
                i = readSocksReply(in, data, deadlineMillis);
                if (i != 2)
                    throw new SocketException("来自 SOCKS 服务器的回复格式错误");
                break;
            case IPV6:
                len = 16;
                addr = new byte[len];
                i = readSocksReply(in, addr, deadlineMillis);
                if (i != len)
                    throw new SocketException("来自 SOCKS 服务器的回复格式错误");
                data = new byte[2];
                i = readSocksReply(in, data, deadlineMillis);
                if (i != 2)
                    throw new SocketException("来自 SOCKS 服务器的回复格式错误");
                break;
            default:
                ex = new SocketException("来自 SOCKS 服务器的回复包含错误代码");
                break;
            }
            break;
        case GENERAL_FAILURE:
            ex = new SocketException("SOCKS 服务器通用错误");
            break;
        case NOT_ALLOWED:
            ex = new SocketException("SOCKS: 连接被规则集禁止");
            break;
        case NET_UNREACHABLE:
            ex = new SocketException("SOCKS: 网络不可达");
            break;
        case HOST_UNREACHABLE:
            ex = new SocketException("SOCKS: 主机不可达");
            break;
        case CONN_REFUSED:
            ex = new SocketException("SOCKS: 连接被拒绝");
            break;
        case TTL_EXPIRED:
            ex =  new SocketException("SOCKS: TTL 到期");
            break;
        case CMD_NOT_SUPPORTED:
            ex = new SocketException("SOCKS: 命令不支持");
            break;
        case ADDR_TYPE_NOT_SUP:
            ex = new SocketException("SOCKS: 地址类型不支持");
            break;
        }
        if (ex != null) {
            in.close();
            out.close();
            throw ex;
        }
        external_address = epoint;
    }

    private void bindV4(InputStream in, OutputStream out,
                        InetAddress baddr,
                        int lport) throws IOException {
        if (!(baddr instanceof Inet4Address)) {
            throw new SocketException("SOCKS V4 仅支持 IPv4 地址");
        }
        super.bind(baddr, lport);
        byte[] addr1 = baddr.getAddress();
        /* 测试 AnyLocal */
        InetAddress naddr = baddr;
        if (naddr.isAnyLocalAddress()) {
            naddr = AccessController.doPrivileged(
                        new PrivilegedAction<InetAddress>() {
                            public InetAddress run() {
                                return cmdsock.getLocalAddress();

                            }
                        });
            addr1 = naddr.getAddress();
        }
        out.write(PROTO_VERS4);
        out.write(BIND);
        out.write((super.getLocalPort() >> 8) & 0xff);
        out.write((super.getLocalPort() >> 0) & 0xff);
        out.write(addr1);
        String userName = getUserName();
        try {
            out.write(userName.getBytes("ISO-8859-1"));
        } catch (java.io.UnsupportedEncodingException uee) {
            assert false;
        }
        out.write(0);
        out.flush();
        byte[] data = new byte[8];
        int n = readSocksReply(in, data);
        if (n != 8)
            throw new SocketException("来自 SOCKS 服务器的回复长度错误: " + n);
        if (data[0] != 0 && data[0] != 4)
            throw new SocketException("来自 SOCKS 服务器的回复版本错误");
        SocketException ex = null;
        switch (data[1]) {
        case 90:
            // 成功！
            external_address = new InetSocketAddress(baddr, lport);
            break;
        case 91:
            ex = new SocketException("SOCKS 请求被拒绝");
            break;
        case 92:
            ex = new SocketException("SOCKS 服务器无法到达目标");
            break;
        case 93:
            ex = new SocketException("SOCKS 认证失败");
            break;
        default:
            ex = new SocketException("来自 SOCKS 服务器的回复状态错误");
            break;
        }
        if (ex != null) {
            in.close();
            out.close();
            throw ex;
        }

    }

    /**
     * 向 SOCKS 代理发送 Bind 请求。在 SOCKS 协议中，bind
     * 意味着“接受来自”的连接，因此 SocketAddress 是
     * 我们接受连接的主机的地址。
     *
     * @param      saddr   远程主机的 Socket 地址。
     * @exception  IOException  当绑定此套接字时发生 I/O 错误。
     */
    protected synchronized void socksBind(InetSocketAddress saddr) throws IOException {
        if (socket != null) {
            // 这是一个客户端套接字，而不是服务器套接字，不要
            // 为 bind 调用 SOCKS 代理！
            return;
        }

        // 连接到 SOCKS 服务器

        if (server == null) {
            // 这是一般情况
            // server 不为 null 仅当套接字是使用
            // 指定的代理创建时，此时它会绕过 ProxySelector
            ProxySelector sel = java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<ProxySelector>() {
                    public ProxySelector run() {
                            return ProxySelector.getDefault();
                        }
                    });
            if (sel == null) {
                /*
                 * 没有默认的 ProxySelector --> 直接连接
                 */
                return;
            }
            URI uri;
            // 使用 getHostString() 以避免反向查找
            String host = saddr.getHostString();
            // IPv6 字面量？
            if (saddr.getAddress() instanceof Inet6Address &&
                (!host.startsWith("[")) && (host.indexOf(":") >= 0)) {
                host = "[" + host + "]";
            }
            try {
                uri = new URI("serversocket://" + ParseUtil.encodePath(host) + ":"+ saddr.getPort());
            } catch (URISyntaxException e) {
                // 这不应该发生
                assert false : e;
                uri = null;
            }
            Proxy p = null;
            Exception savedExc = null;
            java.util.Iterator<Proxy> iProxy = null;
            iProxy = sel.select(uri).iterator();
            if (iProxy == null || !(iProxy.hasNext())) {
                return;
            }
            while (iProxy.hasNext()) {
                p = iProxy.next();
                if (p == null || p.type() != Proxy.Type.SOCKS) {
                    return;
                }

                if (!(p.address() instanceof InetSocketAddress))
                    throw new SocketException("未知的代理地址类型: " + p);
                // 使用 getHostString() 以避免反向查找
                server = ((InetSocketAddress) p.address()).getHostString();
                serverPort = ((InetSocketAddress) p.address()).getPort();
                if (p instanceof SocksProxy) {
                    if (((SocksProxy)p).protocolVersion() == 4) {
                        useV4 = true;
                    }
                }

                // 连接到 SOCKS 服务器
                try {
                    AccessController.doPrivileged(
                        new PrivilegedExceptionAction<Void>() {
                            public Void run() throws Exception {
                                cmdsock = new Socket(new PlainSocketImpl());
                                cmdsock.connect(new InetSocketAddress(server, serverPort));
                                cmdIn = cmdsock.getInputStream();
                                cmdOut = cmdsock.getOutputStream();
                                return null;
                            }
                        });
                } catch (Exception e) {
                    // 哦，通知 ProxySelector
                    sel.connectFailed(uri,p.address(),new SocketException(e.getMessage()));
                    server = null;
                    serverPort = -1;
                    cmdsock = null;
                    savedExc = e;
                    // 继续 while 循环并尝试下一个代理
                }
            }

            /*
             * 如果此时 server 仍为 null，说明没有代理成功
             */
            if (server == null || cmdsock == null) {
                throw new SocketException("无法连接到 SOCKS 代理:"
                                          + savedExc.getMessage());
            }
        } else {
            try {
                AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Void>() {
                        public Void run() throws Exception {
                            cmdsock = new Socket(new PlainSocketImpl());
                            cmdsock.connect(new InetSocketAddress(server, serverPort));
                            cmdIn = cmdsock.getInputStream();
                            cmdOut = cmdsock.getOutputStream();
                            return null;
                        }
                    });
            } catch (Exception e) {
                throw new SocketException(e.getMessage());
            }
        }
        BufferedOutputStream out = new BufferedOutputStream(cmdOut, 512);
        InputStream in = cmdIn;
        if (useV4) {
            bindV4(in, out, saddr.getAddress(), saddr.getPort());
            return;
        }
        out.write(PROTO_VERS);
        out.write(2);
        out.write(NO_AUTH);
        out.write(USER_PASSW);
        out.flush();
        byte[] data = new byte[2];
        int i = readSocksReply(in, data);
        if (i != 2 || ((int)data[0]) != PROTO_VERS) {
            // 可能不是 V5 服务器
            // 在放弃之前尝试 V4
            bindV4(in, out, saddr.getAddress(), saddr.getPort());
            return;
        }
        if (((int)data[1]) == NO_METHODS)
            throw new SocketException("SOCKS : 没有可接受的方法");
        if (!authenticate(data[1], in, out)) {
            throw new SocketException("SOCKS : 认证失败");
        }
        // 我们可以了。让我们发出 BIND 命令。
        out.write(PROTO_VERS);
        out.write(BIND);
        out.write(0);
        int lport = saddr.getPort();
        if (saddr.isUnresolved()) {
            out.write(DOMAIN_NAME);
            out.write(saddr.getHostName().length());
            try {
                out.write(saddr.getHostName().getBytes("ISO-8859-1"));
            } catch (java.io.UnsupportedEncodingException uee) {
                assert false;
            }
            out.write((lport >> 8) & 0xff);
            out.write((lport >> 0) & 0xff);
        } else if (saddr.getAddress() instanceof Inet4Address) {
            byte[] addr1 = saddr.getAddress().getAddress();
            out.write(IPV4);
            out.write(addr1);
            out.write((lport >> 8) & 0xff);
            out.write((lport >> 0) & 0xff);
            out.flush();
        } else if (saddr.getAddress() instanceof Inet6Address) {
            byte[] addr1 = saddr.getAddress().getAddress();
            out.write(IPV6);
            out.write(addr1);
            out.write((lport >> 8) & 0xff);
            out.write((lport >> 0) & 0xff);
            out.flush();
        } else {
            cmdsock.close();
            throw new SocketException("不支持的地址类型 : " + saddr);
        }
        data = new byte[4];
        i = readSocksReply(in, data);
        SocketException ex = null;
        int len, nport;
        byte[] addr;
        switch (data[1]) {
        case REQUEST_OK:
            // 成功！
            switch(data[3]) {
            case IPV4:
                addr = new byte[4];
                i = readSocksReply(in, addr);
                if (i != 4)
                    throw new SocketException("来自 SOCKS 服务器的回复格式错误");
                data = new byte[2];
                i = readSocksReply(in, data);
                if (i != 2)
                    throw new SocketException("来自 SOCKS 服务器的回复格式错误");
                nport = ((int)data[0] & 0xff) << 8;
                nport += ((int)data[1] & 0xff);
                external_address =
                    new InetSocketAddress(new Inet4Address("", addr) , nport);
                break;
            case DOMAIN_NAME:
                len = data[1];
                byte[] host = new byte[len];
                i = readSocksReply(in, host);
                if (i != len)
                    throw new SocketException("来自 SOCKS 服务器的回复格式错误");
                data = new byte[2];
                i = readSocksReply(in, data);
                if (i != 2)
                    throw new SocketException("来自 SOCKS 服务器的回复格式错误");
                nport = ((int)data[0] & 0xff) << 8;
                nport += ((int)data[1] & 0xff);
                external_address = new InetSocketAddress(new String(host), nport);
                break;
            case IPV6:
                len = data[1];
                addr = new byte[len];
                i = readSocksReply(in, addr);
                if (i != len)
                    throw new SocketException("来自 SOCKS 服务器的回复格式错误");
                data = new byte[2];
                i = readSocksReply(in, data);
                if (i != 2)
                    throw new SocketException("来自 SOCKS 服务器的回复格式错误");
                nport = ((int)data[0] & 0xff) << 8;
                nport += ((int)data[1] & 0xff);
                external_address =
                    new InetSocketAddress(new Inet6Address("", addr), nport);
                break;
            }
            break;
        case GENERAL_FAILURE:
            ex = new SocketException("SOCKS 服务器通用错误");
            break;
        case NOT_ALLOWED:
            ex = new SocketException("SOCKS: Bind 被规则集禁止");
            break;
        case NET_UNREACHABLE:
            ex = new SocketException("SOCKS: 网络不可达");
            break;
        case HOST_UNREACHABLE:
            ex = new SocketException("SOCKS: 主机不可达");
            break;
        case CONN_REFUSED:
            ex = new SocketException("SOCKS: 连接被拒绝");
            break;
        case TTL_EXPIRED:
            ex =  new SocketException("SOCKS: TTL 到期");
            break;
        case CMD_NOT_SUPPORTED:
            ex = new SocketException("SOCKS: 命令不支持");
            break;
        case ADDR_TYPE_NOT_SUP:
            ex = new SocketException("SOCKS: 地址类型不支持");
            break;
        }
        if (ex != null) {
            in.close();
            out.close();
            cmdsock.close();
            cmdsock = null;
            throw ex;
        }
        cmdIn = in;
        cmdOut = out;
    }


                /**
     * 接受来自特定主机的连接。
     *
     * @param      s   接受的连接。
     * @param      saddr 我们接受连接的主机的套接字地址
     * @exception  IOException  当接受连接时发生 I/O 错误。
     */
    protected void acceptFrom(SocketImpl s, InetSocketAddress saddr) throws IOException {
        if (cmdsock == null) {
            // 不是 Socks ServerSocket。
            return;
        }
        InputStream in = cmdIn;
        // 发送 "SOCKS BIND" 请求。
        socksBind(saddr);
        in.read();
        int i = in.read();
        in.read();
        SocketException ex = null;
        int nport;
        byte[] addr;
        InetSocketAddress real_end = null;
        switch (i) {
        case REQUEST_OK:
            // 成功！
            i = in.read();
            switch(i) {
            case IPV4:
                addr = new byte[4];
                readSocksReply(in, addr);
                nport = in.read() << 8;
                nport += in.read();
                real_end =
                    new InetSocketAddress(new Inet4Address("", addr) , nport);
                break;
            case DOMAIN_NAME:
                int len = in.read();
                addr = new byte[len];
                readSocksReply(in, addr);
                nport = in.read() << 8;
                nport += in.read();
                real_end = new InetSocketAddress(new String(addr), nport);
                break;
            case IPV6:
                addr = new byte[16];
                readSocksReply(in, addr);
                nport = in.read() << 8;
                nport += in.read();
                real_end =
                    new InetSocketAddress(new Inet6Address("", addr), nport);
                break;
            }
            break;
        case GENERAL_FAILURE:
            ex = new SocketException("SOCKS 服务器通用失败");
            break;
        case NOT_ALLOWED:
            ex = new SocketException("SOCKS: 不允许接受连接");
            break;
        case NET_UNREACHABLE:
            ex = new SocketException("SOCKS: 网络不可达");
            break;
        case HOST_UNREACHABLE:
            ex = new SocketException("SOCKS: 主机不可达");
            break;
        case CONN_REFUSED:
            ex = new SocketException("SOCKS: 连接被拒绝");
            break;
        case TTL_EXPIRED:
            ex =  new SocketException("SOCKS: TTL 过期");
            break;
        case CMD_NOT_SUPPORTED:
            ex = new SocketException("SOCKS: 不支持的命令");
            break;
        case ADDR_TYPE_NOT_SUP:
            ex = new SocketException("SOCKS: 不支持的地址类型");
            break;
        }
        if (ex != null) {
            cmdIn.close();
            cmdOut.close();
            cmdsock.close();
            cmdsock = null;
            throw ex;
        }

        /**
         * 这里需要做一些复杂的事情。
         * 由代理“接受”的套接字的数据流
         * 将通过 cmdSocket。因此，我们必须交换 socketImpls
         */
        if (s instanceof SocksSocketImpl) {
            ((SocksSocketImpl)s).external_address = real_end;
        }
        if (s instanceof PlainSocketImpl) {
            PlainSocketImpl psi = (PlainSocketImpl) s;
            psi.setInputStream((SocketInputStream) in);
            psi.setFileDescriptor(cmdsock.getImpl().getFileDescriptor());
            psi.setAddress(cmdsock.getImpl().getInetAddress());
            psi.setPort(cmdsock.getImpl().getPort());
            psi.setLocalPort(cmdsock.getImpl().getLocalPort());
        } else {
            s.fd = cmdsock.getImpl().fd;
            s.address = cmdsock.getImpl().address;
            s.port = cmdsock.getImpl().port;
            s.localport = cmdsock.getImpl().localport;
        }

        // 需要这样做，以防止当用户关闭 ServerSocket 时套接字被关闭。
        // 这样做可以将套接字“分离”，因为它现在在其他地方使用。
        cmdsock = null;
    }


    /**
     * 返回此套接字的 {@code address} 字段的值。
     *
     * @return  此套接字的 {@code address} 字段的值。
     * @see     java.net.SocketImpl#address
     */
    @Override
    protected InetAddress getInetAddress() {
        if (external_address != null)
            return external_address.getAddress();
        else
            return super.getInetAddress();
    }

    /**
     * 返回此套接字的 {@code port} 字段的值。
     *
     * @return  此套接字的 {@code port} 字段的值。
     * @see     java.net.SocketImpl#port
     */
    @Override
    protected int getPort() {
        if (external_address != null)
            return external_address.getPort();
        else
            return super.getPort();
    }

    @Override
    protected int getLocalPort() {
        if (socket != null)
            return super.getLocalPort();
        if (external_address != null)
            return external_address.getPort();
        else
            return super.getLocalPort();
    }

    @Override
    protected void close() throws IOException {
        if (cmdsock != null)
            cmdsock.close();
        cmdsock = null;
        super.close();
    }

    private String getUserName() {
        String userName = "";
        if (applicationSetProxy) {
            try {
                userName = System.getProperty("user.name");
            } catch (SecurityException se) { /* 忽略异常 */ }
        } else {
            userName = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("user.name"));
        }
        return userName;
    }
}
