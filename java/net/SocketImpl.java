
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileDescriptor;

/**
 * 抽象类 {@code SocketImpl} 是所有实际实现套接字的类的公共超类。它用于创建客户端和服务器套接字。
 * <p>
 * “普通”套接字按照描述准确实现这些方法，不尝试通过防火墙或代理。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public abstract class SocketImpl implements SocketOptions {
    /**
     * 实际的 Socket 对象。
     */
    Socket socket = null;
    ServerSocket serverSocket = null;

    /**
     * 此套接字的文件描述符对象。
     */
    protected FileDescriptor fd;

    /**
     * 此套接字远端的 IP 地址。
     */
    protected InetAddress address;

    /**
     * 此套接字连接的远端主机的端口号。
     */
    protected int port;

    /**
     * 此套接字连接的本地端口号。
     */
    protected int localport;

    /**
     * 创建流套接字或数据报套接字。
     *
     * @param      stream   如果为 {@code true}，则创建流套接字；
     *                      否则，创建数据报套接字。
     * @exception  IOException  如果在创建套接字时发生 I/O 错误。
     */
    protected abstract void create(boolean stream) throws IOException;

    /**
     * 将此套接字连接到指定主机上的指定端口。
     *
     * @param      host   远程主机的名称。
     * @param      port   端口号。
     * @exception  IOException  如果在连接到远程主机时发生 I/O 错误。
     */
    protected abstract void connect(String host, int port) throws IOException;

    /**
     * 将此套接字连接到指定主机上的指定端口号。
     *
     * @param      address   远程主机的 IP 地址。
     * @param      port      端口号。
     * @exception  IOException  如果在尝试连接时发生 I/O 错误。
     */
    protected abstract void connect(InetAddress address, int port) throws IOException;

    /**
     * 将此套接字连接到指定主机上的指定端口号。超时时间为零表示无限超时。连接将阻塞，直到建立连接或发生错误。
     *
     * @param      address   远程主机的套接字地址。
     * @param     timeout  超时值，以毫秒为单位，或零表示无超时。
     * @exception  IOException  如果在尝试连接时发生 I/O 错误。
     * @since 1.4
     */
    protected abstract void connect(SocketAddress address, int timeout) throws IOException;

    /**
     * 将此套接字绑定到指定的本地 IP 地址和端口号。
     *
     * @param      host   属于本地接口的 IP 地址。
     * @param      port   端口号。
     * @exception  IOException  如果在绑定此套接字时发生 I/O 错误。
     */
    protected abstract void bind(InetAddress host, int port) throws IOException;

    /**
     * 设置传入连接指示（连接请求）的最大队列长度为 {@code count} 参数。如果连接指示到达时队列已满，连接将被拒绝。
     *
     * @param      backlog   队列的最大长度。
     * @exception  IOException  如果在创建队列时发生 I/O 错误。
     */
    protected abstract void listen(int backlog) throws IOException;

    /**
     * 接受连接。
     *
     * @param      s   接受的连接。
     * @exception  IOException  如果在接受连接时发生 I/O 错误。
     */
    protected abstract void accept(SocketImpl s) throws IOException;

    /**
     * 返回此套接字的输入流。
     *
     * @return     用于从此套接字读取的流。
     * @exception  IOException  如果在创建输入流时发生 I/O 错误。
    */
    protected abstract InputStream getInputStream() throws IOException;

    /**
     * 返回此套接字的输出流。
     *
     * @return     用于写入此套接字的输出流。
     * @exception  IOException  如果在创建输出流时发生 I/O 错误。
     */
    protected abstract OutputStream getOutputStream() throws IOException;

    /**
     * 返回从此套接字读取而不阻塞的字节数。
     *
     * @return     从此套接字读取而不阻塞的字节数。
     * @exception  IOException  如果在确定可用字节数时发生 I/O 错误。
     */
    protected abstract int available() throws IOException;

    /**
     * 关闭此套接字。
     *
     * @exception  IOException  如果在关闭此套接字时发生 I/O 错误。
     */
    protected abstract void close() throws IOException;

    /**
     * 将此套接字的输入流置于“流结束”状态。发送到此套接字的任何数据都将被确认并默默地丢弃。
     *
     * 如果在对此套接字调用此方法后从套接字输入流读取，流的 {@code available} 方法将返回 0，其 {@code read} 方法将返回 {@code -1}（流结束）。
     *
     * @exception IOException 如果在关闭此套接字时发生 I/O 错误。
     * @see java.net.Socket#shutdownOutput()
     * @see java.net.Socket#close()
     * @see java.net.Socket#setSoLinger(boolean, int)
     * @since 1.3
     */
    protected void shutdownInput() throws IOException {
      throw new IOException("Method not implemented!");
    }


/**
 * 禁用此套接字的输出流。
 * 对于TCP套接字，任何先前写入的数据都将被发送，
 * 然后是TCP的正常连接终止序列。
 *
 * 如果在调用shutdownOutput()后向套接字输出流写入数据，
 * 流将抛出一个IOException。
 *
 * @exception IOException 如果关闭此套接字时发生I/O错误。
 * @see java.net.Socket#shutdownInput()
 * @see java.net.Socket#close()
 * @see java.net.Socket#setSoLinger(boolean, int)
 * @since 1.3
 */
protected void shutdownOutput() throws IOException {
  throw new IOException("Method not implemented!");
}

/**
 * 返回此套接字的{@code fd}字段的值。
 *
 * @return 此套接字的{@code fd}字段的值。
 * @see java.net.SocketImpl#fd
 */
protected FileDescriptor getFileDescriptor() {
    return fd;
}

/**
 * 返回此套接字的{@code address}字段的值。
 *
 * @return 此套接字的{@code address}字段的值。
 * @see java.net.SocketImpl#address
 */
protected InetAddress getInetAddress() {
    return address;
}

/**
 * 返回此套接字的{@code port}字段的值。
 *
 * @return 此套接字的{@code port}字段的值。
 * @see java.net.SocketImpl#port
 */
protected int getPort() {
    return port;
}

/**
 * 返回此SocketImpl是否支持发送紧急数据。默认情况下，返回false
 * 除非在子类中重写此方法
 *
 * @return 如果支持紧急数据，则返回true
 * @see java.net.SocketImpl#address
 * @since 1.4
 */
protected boolean supportsUrgentData () {
    return false; // 必须在子类中重写
}

/**
 * 在套接字上发送一个字节的紧急数据。
 * 要发送的字节是参数的低八位
 * @param data 要发送的数据字节
 * @exception IOException 如果发送数据时发生错误。
 * @since 1.4
 */
protected abstract void sendUrgentData (int data) throws IOException;

/**
 * 返回此套接字的{@code localport}字段的值。
 *
 * @return 此套接字的{@code localport}字段的值。
 * @see java.net.SocketImpl#localport
 */
protected int getLocalPort() {
    return localport;
}

void setSocket(Socket soc) {
    this.socket = soc;
}

Socket getSocket() {
    return socket;
}

void setServerSocket(ServerSocket soc) {
    this.serverSocket = soc;
}

ServerSocket getServerSocket() {
    return serverSocket;
}

/**
 * 以{@code String}的形式返回此套接字的地址和端口。
 *
 * @return 此套接字的字符串表示形式。
 */
public String toString() {
    return "Socket[addr=" + getInetAddress() +
        ",port=" + getPort() + ",localport=" + getLocalPort()  + "]";
}

void reset() throws IOException {
    address = null;
    port = 0;
    localport = 0;
}

/**
 * 为此套接字设置性能偏好。
 *
 * <p>套接字默认使用TCP/IP协议。某些实现可能提供具有不同性能特征的替代协议。此方法允许应用程序表达其对这些权衡的偏好
 * 当实现从可用协议中选择时。性能偏好由三个整数描述，这些整数的值表示短连接时间、低延迟和高带宽的相对重要性。
 * 整数的绝对值无关紧要；为了选择协议，这些值只是进行比较，较大的值表示较强的偏好。负值表示优先级低于正值。
 * 例如，如果应用程序优先考虑短连接时间而不是低延迟和高带宽，则可以调用此方法，参数为{@code (1, 0, 0)}。
 * 如果应用程序优先考虑高带宽高于低延迟，低延迟高于短连接时间，则可以调用此方法，参数为{@code (0, 1, 2)}。
 *
 * 默认情况下，此方法不执行任何操作，除非在子类中重写。
 *
 * @param  connectionTime
 *         一个表示短连接时间相对重要性的整数。
 *
 * @param  latency
 *         一个表示低延迟相对重要性的整数。
 *
 * @param  bandwidth
 *         一个表示高带宽相对重要性的整数。
 *
 * @since 1.5
 */
protected void setPerformancePreferences(int connectionTime,
                                      int latency,
                                      int bandwidth)
{
    /* 尚未实现 */
}

<T> void setOption(SocketOption<T> name, T value) throws IOException {
    if (name == StandardSocketOptions.SO_KEEPALIVE) {
        setOption(SocketOptions.SO_KEEPALIVE, value);
    } else if (name == StandardSocketOptions.SO_SNDBUF) {
        setOption(SocketOptions.SO_SNDBUF, value);
    } else if (name == StandardSocketOptions.SO_RCVBUF) {
        setOption(SocketOptions.SO_RCVBUF, value);
    } else if (name == StandardSocketOptions.SO_REUSEADDR) {
        setOption(SocketOptions.SO_REUSEADDR, value);
    } else if (name == StandardSocketOptions.SO_LINGER) {
        setOption(SocketOptions.SO_LINGER, value);
    } else if (name == StandardSocketOptions.IP_TOS) {
        setOption(SocketOptions.IP_TOS, value);
    } else if (name == StandardSocketOptions.TCP_NODELAY) {
        setOption(SocketOptions.TCP_NODELAY, value);
    } else {
        throw new UnsupportedOperationException("unsupported option");
    }
}


                <T> T getOption(SocketOption<T> name) throws IOException {
        if (name == StandardSocketOptions.SO_KEEPALIVE) {
            // 如果选项是 SO_KEEPALIVE，则调用 getOption 方法获取该选项的值
            return (T)getOption(SocketOptions.SO_KEEPALIVE);
        } else if (name == StandardSocketOptions.SO_SNDBUF) {
            // 如果选项是 SO_SNDBUF，则调用 getOption 方法获取该选项的值
            return (T)getOption(SocketOptions.SO_SNDBUF);
        } else if (name == StandardSocketOptions.SO_RCVBUF) {
            // 如果选项是 SO_RCVBUF，则调用 getOption 方法获取该选项的值
            return (T)getOption(SocketOptions.SO_RCVBUF);
        } else if (name == StandardSocketOptions.SO_REUSEADDR) {
            // 如果选项是 SO_REUSEADDR，则调用 getOption 方法获取该选项的值
            return (T)getOption(SocketOptions.SO_REUSEADDR);
        } else if (name == StandardSocketOptions.SO_LINGER) {
            // 如果选项是 SO_LINGER，则调用 getOption 方法获取该选项的值
            return (T)getOption(SocketOptions.SO_LINGER);
        } else if (name == StandardSocketOptions.IP_TOS) {
            // 如果选项是 IP_TOS，则调用 getOption 方法获取该选项的值
            return (T)getOption(SocketOptions.IP_TOS);
        } else if (name == StandardSocketOptions.TCP_NODELAY) {
            // 如果选项是 TCP_NODELAY，则调用 getOption 方法获取该选项的值
            return (T)getOption(SocketOptions.TCP_NODELAY);
        } else {
            // 如果选项不支持，则抛出 UnsupportedOperationException 异常
            throw new UnsupportedOperationException("unsupported option");
        }
    }
}
