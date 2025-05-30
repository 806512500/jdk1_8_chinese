/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 基本的 SocketImpl，依赖于内部 HTTP 协议处理程序的实现来执行 HTTP 隧道和身份验证。在隧道成功建立后，套接字实现将被交换并替换为 HTTP 处理程序中的套接字。
 *
 * @since 1.8
 */

/*package*/ class HttpConnectSocketImpl extends PlainSocketImpl {

    private static final String httpURLClazzStr =
                                  "sun.net.www.protocol.http.HttpURLConnection";
    private static final String netClientClazzStr = "sun.net.NetworkClient";
    private static final String doTunnelingStr = "doTunneling";
    private static final Field httpField;
    private static final Field serverSocketField;
    private static final Method doTunneling;

    private final String server;
    private InetSocketAddress external_address;
    private HashMap<Integer, Object> optionsMap = new HashMap<>();

    static  {
        try {
            Class<?> httpClazz = Class.forName(httpURLClazzStr, true, null);
            httpField = httpClazz.getDeclaredField("http");
            doTunneling = httpClazz.getDeclaredMethod(doTunnelingStr);
            Class<?> netClientClazz = Class.forName(netClientClazzStr, true, null);
            serverSocketField = netClientClazz.getDeclaredField("serverSocket");

            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<Void>() {
                    public Void run() {
                        httpField.setAccessible(true);
                        serverSocketField.setAccessible(true);
                        return null;
                }
            });
        } catch (ReflectiveOperationException x) {
            throw new InternalError("不应该到达这里", x);
        }
    }

    HttpConnectSocketImpl(String server, int port) {
        this.server = server;
        this.port = port;
    }

    HttpConnectSocketImpl(Proxy proxy) {
        SocketAddress a = proxy.address();
        if ( !(a instanceof InetSocketAddress) )
            throw new IllegalArgumentException("不支持的地址类型");

        InetSocketAddress ad = (InetSocketAddress) a;
        server = ad.getHostString();
        port = ad.getPort();
    }

    @Override
    protected void connect(SocketAddress endpoint, int timeout)
        throws IOException
    {
        if (endpoint == null || !(endpoint instanceof InetSocketAddress))
            throw new IllegalArgumentException("不支持的地址类型");
        final InetSocketAddress epoint = (InetSocketAddress)endpoint;
        final String destHost = epoint.isUnresolved() ? epoint.getHostName()
                                                      : epoint.getAddress().getHostAddress();
        final int destPort = epoint.getPort();

        SecurityManager security = System.getSecurityManager();
        if (security != null)
            security.checkConnect(destHost, destPort);

        // 连接到 HTTP 代理服务器
        String urlString = "http://" + destHost + ":" + destPort;
        Socket httpSocket = privilegedDoTunnel(urlString, timeout);

        // 成功！
        external_address = epoint;

        // 关闭原始的套接字实现并释放其描述符
        close();

        // 更新套接字实现为来自 http 套接字的实现
        AbstractPlainSocketImpl psi = (AbstractPlainSocketImpl) httpSocket.impl;
        this.getSocket().impl = psi;

        // 尽力尝试重置之前设置的选项
        Set<Map.Entry<Integer,Object>> options = optionsMap.entrySet();
        try {
            for(Map.Entry<Integer,Object> entry : options) {
                psi.setOption(entry.getKey(), entry.getValue());
            }
        } catch (IOException x) {  /* 吞下异常！ */  }
    }

    @Override
    public void setOption(int opt, Object val) throws SocketException {
        super.setOption(opt, val);

        if (external_address != null)
            return;  // 已连接，直接返回

        // 存储选项，以便在连接后重新应用到实现中
        optionsMap.put(opt, val);
    }

    private Socket privilegedDoTunnel(final String urlString,
                                      final int timeout)
        throws IOException
    {
        try {
            return java.security.AccessController.doPrivileged(
                new java.security.PrivilegedExceptionAction<Socket>() {
                    public Socket run() throws IOException {
                        return doTunnel(urlString, timeout);
                }
            });
        } catch (java.security.PrivilegedActionException pae) {
            throw (IOException) pae.getException();
        }
    }

    private Socket doTunnel(String urlString, int connectTimeout)
        throws IOException
    {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(server, port));
        URL destURL = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) destURL.openConnection(proxy);
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(this.timeout);
        conn.connect();
        doTunneling(conn);
        try {
            Object httpClient = httpField.get(conn);
            return (Socket) serverSocketField.get(httpClient);
        } catch (IllegalAccessException x) {
            throw new InternalError("不应该到达这里", x);
        }
    }

    private void doTunneling(HttpURLConnection conn) {
        try {
            doTunneling.invoke(conn);
        } catch (ReflectiveOperationException x) {
            throw new InternalError("不应该到达这里", x);
        }
    }

    @Override
    protected InetAddress getInetAddress() {
        if (external_address != null)
            return external_address.getAddress();
        else
            return super.getInetAddress();
    }

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
}
