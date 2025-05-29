
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
 * 这是 PlainSocketImpl 的一个子类。
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
    /* 如果代理是通过编程方式设置的，则为 true */
    private boolean applicationSetProxy;  /* false */


    SocksSocketImpl() {
        super(false);
    }

    SocksSocketImpl(String server, int port) {
        super(false);
        this.server = server;
        this.serverPort = (port == -1 ? DEFAULT_PORT : port);
    }

    SocksSocketImpl(Proxy proxy) {
        super(false);
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
        for (int attempts = 0; received < len && attempts < 3; attempts++) {
            int count;
            try {
                count = ((SocketInputStream)in).read(data, received, len - received, remainingMillis(deadlineMillis));
            } catch (SocketTimeoutException e) {
                throw new SocketTimeoutException("连接超时");
            }
            if (count < 0)
                throw new SocketException("来自 SOCKS 服务器的格式错误的响应");
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
         * - 如果有，应用程序提供的 Authenticator
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
                /* RFC 1929 指定如果认证失败，连接 <b>必须</b> 关闭 */
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
//              /* RFC 1961 指定如果上下文初始化失败，连接 <b>必须</b> 关闭 */
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
            throw new SocketException("SOCKS V4 requires IPv4 only addresses");
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
            throw new SocketException("Reply from SOCKS server has bad length: " + n);
        if (data[0] != 0 && data[0] != 4)
            throw new SocketException("Reply from SOCKS server has bad version");
        SocketException ex = null;
        switch (data[1]) {
        case 90:
            // Success!
            external_address = endpoint;
            break;
        case 91:
            ex = new SocketException("SOCKS request rejected");
            break;
        case 92:
            ex = new SocketException("SOCKS server couldn't reach destination");
            break;
        case 93:
            ex = new SocketException("SOCKS authentication failed");
            break;
        default:
            ex = new SocketException("Reply from SOCKS server contains bad status");
            break;
        }
        if (ex != null) {
            in.close();
            out.close();
            throw ex;
        }
    }

    /**
     * Connects the Socks Socket to the specified endpoint. It will first
     * connect to the SOCKS proxy and negotiate the access. If the proxy
     * grants the connections, then the connect is successful and all
     * further traffic will go to the "real" endpoint.
     *
     * @param   endpoint        the {@code SocketAddress} to connect to.
     * @param   timeout         the timeout value in milliseconds
     * @throws  IOException     if the connection can't be established.
     * @throws  SecurityException if there is a security manager and it
     *                          doesn't allow the connection
     * @throws  IllegalArgumentException if endpoint is null or a
     *          SocketAddress subclass not supported by this socket
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
            throw new IllegalArgumentException("Unsupported address type");
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
            // This is the general case
            // server is not null only when the socket was created with a
            // specified proxy in which case it does bypass the ProxySelector
            ProxySelector sel = java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<ProxySelector>() {
                    public ProxySelector run() {
                            return ProxySelector.getDefault();
                        }
                    });
            if (sel == null) {
                /*
                 * No default proxySelector --> direct connection
                 */
                super.connect(epoint, remainingMillis(deadlineMillis));
                return;
            }
            URI uri;
            // Use getHostString() to avoid reverse lookups
            String host = epoint.getHostString();
            // IPv6 literal?
            if (epoint.getAddress() instanceof Inet6Address &&
                (!host.startsWith("[")) && (host.indexOf(":") >= 0)) {
                host = "[" + host + "]";
            }
            try {
                uri = new URI("socket://" + ParseUtil.encodePath(host) + ":"+ epoint.getPort());
            } catch (URISyntaxException e) {
                // This shouldn't happen
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
                    throw new SocketException("Unknown address type for proxy: " + p);
                // Use getHostString() to avoid reverse lookups
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
                    // 哦哦，让我们通知 ProxySelector
                    sel.connectFailed(uri,p.address(),e);
                    server = null;
                    serverPort = -1;
                    savedExc = e;
                    // 将继续 while 循环并尝试下一个代理
                }
            }

            /*
             * 如果此时 server 仍然为 null，说明没有一个代理成功
             */
            if (server == null) {
                throw new SocketException("Can't connect to SOCKS proxy:"
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
            // 未解析的地址类型（这里的未解析地址）
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
            // 也许它根本不是 V5 服务器
            // 在放弃之前，让我们尝试 V4
            // SOCKS 协议版本 4 不知道如何处理
            // 未解析的地址类型（这里的未解析地址）
            if (epoint.isUnresolved())
                throw new UnknownHostException(epoint.toString());
            connectV4(in, out, epoint, deadlineMillis);
            return;
        }
        if (((int)data[1]) == NO_METHODS)
            throw new SocketException("SOCKS : No acceptable methods");
        if (!authenticate(data[1], in, out, deadlineMillis)) {
            throw new SocketException("SOCKS : authentication failed");
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
            throw new SocketException("Reply from SOCKS server has bad length");
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
                    throw new SocketException("Reply from SOCKS server badly formatted");
                data = new byte[2];
                i = readSocksReply(in, data, deadlineMillis);
                if (i != 2)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                break;
            case DOMAIN_NAME:
                len = data[1];
                byte[] host = new byte[len];
                i = readSocksReply(in, host, deadlineMillis);
                if (i != len)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                data = new byte[2];
                i = readSocksReply(in, data, deadlineMillis);
                if (i != 2)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                break;
            case IPV6:
                len = data[1];
                addr = new byte[len];
                i = readSocksReply(in, addr, deadlineMillis);
                if (i != len)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                data = new byte[2];
                i = readSocksReply(in, data, deadlineMillis);
                if (i != 2)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                break;
            default:
                ex = new SocketException("Reply from SOCKS server contains wrong code");
                break;
            }
            break;
        case GENERAL_FAILURE:
            ex = new SocketException("SOCKS server general failure");
            break;
        case NOT_ALLOWED:
            ex = new SocketException("SOCKS: Connection not allowed by ruleset");
            break;
        case NET_UNREACHABLE:
            ex = new SocketException("SOCKS: Network unreachable");
            break;
        case HOST_UNREACHABLE:
            ex = new SocketException("SOCKS: Host unreachable");
            break;
        case CONN_REFUSED:
            ex = new SocketException("SOCKS: Connection refused");
            break;
        case TTL_EXPIRED:
            ex =  new SocketException("SOCKS: TTL expired");
            break;
        case CMD_NOT_SUPPORTED:
            ex = new SocketException("SOCKS: Command not supported");
            break;
        case ADDR_TYPE_NOT_SUP:
            ex = new SocketException("SOCKS: address type not supported");
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
        throw new SocketException("SOCKS V4 requires IPv4 only addresses");
    }
    super.bind(baddr, lport);
    byte[] addr1 = baddr.getAddress();
    /* Test for AnyLocal */
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
        throw new SocketException("Reply from SOCKS server has bad length: " + n);
    if (data[0] != 0 && data[0] != 4)
        throw new SocketException("Reply from SOCKS server has bad version");
    SocketException ex = null;
    switch (data[1]) {
    case 90:
        // Success!
        external_address = new InetSocketAddress(baddr, lport);
        break;
    case 91:
        ex = new SocketException("SOCKS request rejected");
        break;
    case 92:
        ex = new SocketException("SOCKS server couldn't reach destination");
        break;
    case 93:
        ex = new SocketException("SOCKS authentication failed");
        break;
    default:
        ex = new SocketException("Reply from SOCKS server contains bad status");
        break;
    }
    if (ex != null) {
        in.close();
        out.close();
        throw ex;
    }

}

/**
 * Sends the Bind request to the SOCKS proxy. In the SOCKS protocol, bind
 * means "accept incoming connection from", so the SocketAddress is the
 * the one of the host we do accept connection from.
 *
 * @param      saddr   the Socket address of the remote host.
 * @exception  IOException  if an I/O error occurs when binding this socket.
 */
protected synchronized void socksBind(InetSocketAddress saddr) throws IOException {
    if (socket != null) {
        // this is a client socket, not a server socket, don't
        // call the SOCKS proxy for a bind!
        return;
    }

    // Connects to the SOCKS server

    if (server == null) {
        // This is the general case
        // server is not null only when the socket was created with a
        // specified proxy in which case it does bypass the ProxySelector
        ProxySelector sel = java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<ProxySelector>() {
                public ProxySelector run() {
                    return ProxySelector.getDefault();
                }
            });
        if (sel == null) {
            /*
             * No default proxySelector --> direct connection
             */
            return;
        }
        URI uri;
        // Use getHostString() to avoid reverse lookups
        String host = saddr.getHostString();
        // IPv6 literal?
        if (saddr.getAddress() instanceof Inet6Address &&
            (!host.startsWith("[")) && (host.indexOf(":") >= 0)) {
            host = "[" + host + "]";
        }
        try {
            uri = new URI("serversocket://" + ParseUtil.encodePath(host) + ":"+ saddr.getPort());
        } catch (URISyntaxException e) {
            // This shouldn't happen
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
                throw new SocketException("Unknown address type for proxy: " + p);
            // Use getHostString() to avoid reverse lookups
            server = ((InetSocketAddress) p.address()).getHostString();
            serverPort = ((InetSocketAddress) p.address()).getPort();
            if (p instanceof SocksProxy) {
                if (((SocksProxy)p).protocolVersion() == 4) {
                    useV4 = true;
                }
            }

            // Connects to the SOCKS server
            try {
                AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Void>() {
                        public Void run() throws Exception {
                            cmdsock = new Socket(new PlainSocketImpl(false));
                            cmdsock.connect(new InetSocketAddress(server, serverPort));
                            cmdIn = cmdsock.getInputStream();
                            cmdOut = cmdsock.getOutputStream();
                            return null;
                        }
                    });
            } catch (Exception e) {
                // Ooops, let's notify the ProxySelector
                sel.connectFailed(uri,p.address(),new SocketException(e.getMessage()));
                server = null;
                serverPort = -1;
                cmdsock = null;
                savedExc = e;
                // Will continue the while loop and try the next proxy
            }
        }


                        /*
                         * 如果此时 server 仍然为 null，说明没有代理可用
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
                                        cmdsock = new Socket(new PlainSocketImpl(false));
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
                    // 一切正常。发出 BIND 命令。
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
                        ex = new SocketException("SOCKS 服务器通用故障");
                        break;
                    case NOT_ALLOWED:
                        ex = new SocketException("SOCKS: 绑定不被规则集允许");
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
     *               连接。
     * @exception  IOException  在接受连接时发生 I/O 错误。
     */
    protected void acceptFrom(SocketImpl s, InetSocketAddress saddr) throws IOException {
        if (cmdsock == null) {
            // 不是 Socks 服务器套接字。
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
            ex = new SocketException("SOCKS: 规则集不允许接受");
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
            ex = new SocketException("SOCKS: 命令不支持");
            break;
        case ADDR_TYPE_NOT_SUP:
            ex = new SocketException("SOCKS: 地址类型不支持");
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
         * 这里需要做一些巧妙的操作。
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

        // 需要这样做，以便当用户关闭 ServerSocket 时，
        // 套接字不会被关闭。
        // 这样做实际上将套接字分离，因为它现在
        // 被用于其他地方。
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
