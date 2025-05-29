
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.InputStream;
import java.io.IOException;
import java.security.Permission;
import java.util.Date;

/**
 * 一个支持HTTP特定功能的URLConnection。详情请参阅
 * <A HREF="http://www.w3.org/pub/WWW/Protocols/"> 规范 </A>。
 * <p>
 *
 * 每个HttpURLConnection实例用于发出单个请求
 * 但与HTTP服务器的底层网络连接可能
 * 由其他实例透明地共享。在HttpURLConnection的
 * InputStream或OutputStream上调用close()方法
 * 可能会释放与此实例相关的网络资源，但对任何共享的持久连接没有影响。
 * 调用disconnect()方法可能会关闭底层套接字
 * 如果持久连接在那时处于空闲状态。
 *
 * <P>HTTP协议处理器有几个可以通过
 * 系统属性访问的设置。这包括
 * <a href="doc-files/net-properties.html#Proxies">代理设置</a>以及
 * <a href="doc-files/net-properties.html#MiscHTTP">其他各种设置</a>。
 * </P>
 * <p>
 * <b>安全权限</b>
 * <p>
 * 如果安装了安全管理器，并且如果调用了一个导致尝试打开连接的方法，
 * 调用者必须具备以下权限之一：-
 * <ul><li>一个“connect” {@link SocketPermission} 到目标URL的主机/端口组合，或</li>
 * <li>一个 {@link URLPermission} 允许此请求。</li>
 * </ul><p>
 * 如果启用了自动重定向，并且此请求被重定向到另一个
 * 目的地，那么调用者还必须具有连接到
 * 重定向主机/URL的权限。
 *
 * @see     java.net.HttpURLConnection#disconnect()
 * @since JDK1.1
 */
abstract public class HttpURLConnection extends URLConnection {
    /* 实例变量 */

    /**
     * HTTP方法（GET, POST, PUT等）。
     */
    protected String method = "GET";

    /**
     * 使用分块编码流模式输出时的块长度。
     * 值为 {@code -1} 表示输出时禁用分块编码。
     * @since 1.5
     */
    protected int chunkLength = -1;

    /**
     * 使用固定长度流模式时的固定内容长度。
     * 值为 {@code -1} 表示输出时禁用固定长度流模式。
     *
     * <P> <B>注意：</B> 推荐使用 {@link #fixedContentLengthLong} 而不是此字段，
     * 因为它可以设置更大的内容长度。
     *
     * @since 1.5
     */
    protected int fixedContentLength = -1;

    /**
     * 使用固定长度流模式时的固定内容长度。
     * 值为 {@code -1} 表示输出时禁用固定长度流模式。
     *
     * @since 1.7
     */
    protected long fixedContentLengthLong = -1;

    /**
     * 返回第 {@code n}<sup>th</sup> 个头字段的键。
     * 某些实现可能将第 {@code 0}<sup>th</sup> 个
     * 头字段视为特殊，即HTTP服务器返回的状态行。在这种情况下，
     * {@link #getHeaderField(int) getHeaderField(0)} 返回状态行，
     * 但 {@code getHeaderFieldKey(0)} 返回 null。
     *
     * @param   n   索引，其中 {@code n >=0}。
     * @return  第 {@code n}<sup>th</sup> 个头字段的键，
     *          如果键不存在，则返回 {@code null}。
     */
    public String getHeaderFieldKey (int n) {
        return null;
    }

    /**
     * 用于在已知内容长度的情况下启用HTTP请求体的流式传输
     * 而不进行内部缓冲。
     * <p>
     * 如果应用程序尝试写入的数据超过指示的内容长度，或者应用程序在写入指示的量之前关闭了OutputStream，
     * 将抛出异常。
     * <p>
     * 当启用输出流式传输时，身份验证和重定向
     * 无法自动处理。如果需要身份验证或重定向，读取响应时将抛出
     * HttpRetryException。可以查询此异常以获取错误的详细信息。
     * <p>
     * 必须在URLConnection连接之前调用此方法。
     * <p>
     * <B>注意：</B> 推荐使用 {@link #setFixedLengthStreamingMode(long)} 而不是此方法，
     * 因为它可以设置更大的内容长度。
     *
     * @param   contentLength 将写入OutputStream的字节数。
     *
     * @throws  IllegalStateException 如果URLConnection已连接
     *          或已启用不同的流式传输模式。
     *
     * @throws  IllegalArgumentException 如果指定了小于零的内容长度。
     *
     * @see     #setChunkedStreamingMode(int)
     * @since 1.5
     */
    public void setFixedLengthStreamingMode (int contentLength) {
        if (connected) {
            throw new IllegalStateException ("Already connected");
        }
        if (chunkLength != -1) {
            throw new IllegalStateException ("Chunked encoding streaming mode set");
        }
        if (contentLength < 0) {
            throw new IllegalArgumentException ("invalid content length");
        }
        fixedContentLength = contentLength;
    }

    /**
     * 用于在已知内容长度的情况下启用HTTP请求体的流式传输
     * 而不进行内部缓冲。
     *
     * <P> 如果应用程序尝试写入的数据超过指示的内容长度，或者应用程序在写入指示的量之前关闭了OutputStream，
     * 将抛出异常。
     *
     * <P> 当启用输出流式传输时，身份验证和重定向
     * 无法自动处理。如果需要身份验证或重定向，读取响应时将抛出
     * {@linkplain HttpRetryException}。可以查询此异常以获取错误的详细信息。
     *
     * <P> 必须在URLConnection连接之前调用此方法。
     *
     * <P> 通过调用此方法设置的内容长度优先于
     * 通过 {@link #setFixedLengthStreamingMode(int)} 设置的任何值。
     *
     * @param  contentLength
     *         将写入OutputStream的字节数。
     *
     * @throws  IllegalStateException
     *          如果URLConnection已连接或已启用不同的
     *          流式传输模式。
     *
     * @throws  IllegalArgumentException
     *          如果指定了小于零的内容长度。
     *
     * @since 1.7
     */
    public void setFixedLengthStreamingMode(long contentLength) {
        if (connected) {
            throw new IllegalStateException("Already connected");
        }
        if (chunkLength != -1) {
            throw new IllegalStateException(
                "Chunked encoding streaming mode set");
        }
        if (contentLength < 0) {
            throw new IllegalArgumentException("invalid content length");
        }
        fixedContentLengthLong = contentLength;
    }


                /* 默认块大小（包括块头），如果未指定；
     * 我们希望与在 sun.net.www.http.ChunkedOutputStream 中定义的保持同步
     */
    private static final int DEFAULT_CHUNK_SIZE = 4096;

    /**
     * 此方法用于在不知道内容长度的情况下启用HTTP请求体的流式传输
     * 而不进行内部缓冲。在这种模式下，使用分块传输编码发送请求体。注意，并非所有HTTP服务器
     * 都支持此模式。
     * <p>
     * 当启用输出流时，身份验证
     * 和重定向不能自动处理。
     * 如果需要身份验证或重定向，读取响应时将抛出 HttpRetryException。
     * 可以查询此异常以获取错误的详细信息。
     * <p>
     * 必须在 URLConnection 连接之前调用此方法。
     *
     * @param   chunklen 每个块中要写入的字节数。
     *          如果 chunklen 小于或等于零，则使用默认值。
     *
     * @throws  IllegalStateException 如果 URLConnection 已连接
     *          或已启用不同的流式传输模式。
     *
     * @see     #setFixedLengthStreamingMode(int)
     * @since 1.5
     */
    public void setChunkedStreamingMode (int chunklen) {
        if (connected) {
            throw new IllegalStateException ("Can't set streaming mode: already connected");
        }
        if (fixedContentLength != -1 || fixedContentLengthLong != -1) {
            throw new IllegalStateException ("Fixed length streaming mode set");
        }
        chunkLength = chunklen <=0? DEFAULT_CHUNK_SIZE : chunklen;
    }

    /**
     * 返回第 n 个头字段的值。
     * 一些实现可能将第 0 个头字段视为特殊，即HTTP
     * 服务器返回的状态行。
     * <p>
     * 此方法可以与 {@link #getHeaderFieldKey getHeaderFieldKey} 方法结合使用，以迭代消息中的所有
     * 头字段。
     *
     * @param   n   一个索引，其中 {@code n>=0}。
     * @return  第 n 个头字段的值，
     *          如果该值不存在，则返回 {@code null}。
     * @see     java.net.HttpURLConnection#getHeaderFieldKey(int)
     */
    public String getHeaderField(int n) {
        return null;
    }

    /**
     * 一个表示三位数 HTTP 状态码的 {@code int}。
     * <ul>
     * <li> 1xx: 信息性
     * <li> 2xx: 成功
     * <li> 3xx: 重定向
     * <li> 4xx: 客户端错误
     * <li> 5xx: 服务器错误
     * </ul>
     */
    protected int responseCode = -1;

    /**
     * HTTP 响应消息。
     */
    protected String responseMessage = null;

    /* 静态变量 */

    /* 是否自动跟随重定向？默认值为 true。 */
    private static boolean followRedirects = true;

    /**
     * 如果为 {@code true}，协议将自动跟随重定向。
     * 如果为 {@code false}，协议将不会自动跟随
     * 重定向。
     * <p>
     * 此字段由 {@code setInstanceFollowRedirects}
     * 方法设置。其值由 {@code getInstanceFollowRedirects}
     * 方法返回。
     * <p>
     * 其默认值基于在 HttpURLConnection 构造时静态 followRedirects 的值。
     *
     * @see     java.net.HttpURLConnection#setInstanceFollowRedirects(boolean)
     * @see     java.net.HttpURLConnection#getInstanceFollowRedirects()
     * @see     java.net.HttpURLConnection#setFollowRedirects(boolean)
     */
    protected boolean instanceFollowRedirects = followRedirects;

    /* 有效的 HTTP 方法 */
    private static final String[] methods = {
        "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
    };

    /**
     * HttpURLConnection 的构造函数。
     * @param u URL
     */
    protected HttpURLConnection (URL u) {
        super(u);
    }

    /**
     * 设置此类是否应自动跟随 HTTP 重定向（响应代码为 3xx 的请求）。默认为 true。Applet
     * 不能更改此变量。
     * <p>
     * 如果存在安全经理，此方法首先调用
     * 安全经理的 {@code checkSetFactory} 方法
     * 以确保允许此操作。
     * 这可能导致 SecurityException。
     *
     * @param set 一个 {@code boolean}，指示是否
     * 跟随 HTTP 重定向。
     * @exception  SecurityException 如果存在安全经理且其
     *             {@code checkSetFactory} 方法不允许
     *             此操作。
     * @see        SecurityManager#checkSetFactory
     * @see #getFollowRedirects()
     */
    public static void setFollowRedirects(boolean set) {
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            // 似乎是这里最好的检查...
            sec.checkSetFactory();
        }
        followRedirects = set;
    }

    /**
     * 返回一个 {@code boolean}，指示
     * 是否应自动跟随 HTTP 重定向（3xx）。
     *
     * @return 如果应自动跟随 HTTP 重定向，则为 {@code true}，否则为 {@code false}。
     * @see #setFollowRedirects(boolean)
     */
    public static boolean getFollowRedirects() {
        return followRedirects;
    }

    /**
     * 设置此 {@code HttpURLConnection} 实例是否应自动跟随 HTTP 重定向（响应代码为 3xx 的请求）。
     * <p>
     * 默认值来自 followRedirects，默认为
     * true。
     *
     * @param followRedirects 一个 {@code boolean}，指示
     * 是否跟随 HTTP 重定向。
     *
     * @see    java.net.HttpURLConnection#instanceFollowRedirects
     * @see #getInstanceFollowRedirects
     * @since 1.3
     */
     public void setInstanceFollowRedirects(boolean followRedirects) {
        instanceFollowRedirects = followRedirects;
     }

                 /**
     * 返回此 {@code HttpURLConnection} 的
     * {@code instanceFollowRedirects} 字段的值。
     *
     * @return  此 {@code HttpURLConnection} 的
     *          {@code instanceFollowRedirects} 字段的值。
     * @see     java.net.HttpURLConnection#instanceFollowRedirects
     * @see #setInstanceFollowRedirects(boolean)
     * @since 1.3
     */
     public boolean getInstanceFollowRedirects() {
         return instanceFollowRedirects;
     }

    /**
     * 设置 URL 请求的方法，可以是以下之一：
     * <UL>
     *  <LI>GET
     *  <LI>POST
     *  <LI>HEAD
     *  <LI>OPTIONS
     *  <LI>PUT
     *  <LI>DELETE
     *  <LI>TRACE
     * </UL> 受协议限制。默认方法是 GET。
     *
     * @param method HTTP 方法
     * @exception ProtocolException 如果方法不能重置或请求的方法对 HTTP 无效。
     * @exception SecurityException 如果设置了安全管理者且方法是 "TRACE"，但未授予 "allowHttpTrace"
     *              NetPermission。
     * @see #getRequestMethod()
     */
    public void setRequestMethod(String method) throws ProtocolException {
        if (connected) {
            throw new ProtocolException("无法重置方法：已连接");
        }
        // 此限制将防止人们使用此类来
        // 通过 Java 实验新的 HTTP 方法。 但出于安全考虑应
        // 应该放置 - 请求字符串可以
        // 任意长。

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].equals(method)) {
                if (method.equals("TRACE")) {
                    SecurityManager s = System.getSecurityManager();
                    if (s != null) {
                        s.checkPermission(new NetPermission("allowHttpTrace"));
                    }
                }
                this.method = method;
                return;
            }
        }
        throw new ProtocolException("无效的 HTTP 方法: " + method);
    }

    /**
     * 获取请求方法。
     * @return HTTP 请求方法
     * @see #setRequestMethod(java.lang.String)
     */
    public String getRequestMethod() {
        return method;
    }

    /**
     * 从 HTTP 响应消息中获取状态码。
     * 例如，在以下状态行的情况下：
     * <PRE>
     * HTTP/1.0 200 OK
     * HTTP/1.0 401 Unauthorized
     * </PRE>
     * 它将分别返回 200 和 401。
     * 如果无法从响应中识别代码（即响应不是有效的 HTTP），则返回 -1。
     * @throws IOException 如果连接到服务器时发生错误。
     * @return HTTP 状态码，或 -1
     */
    public int getResponseCode() throws IOException {
        /*
         * 我们已经得到了响应码
         */
        if (responseCode != -1) {
            return responseCode;
        }

        /*
         * 确保已连接到服务器。记录
         * 异常，因为如果找不到状态行，
         * 需要重新抛出 getInputStream 抛出的任何异常。
         */
        Exception exc = null;
        try {
            getInputStream();
        } catch (Exception e) {
            exc = e;
        }

        /*
         * 如果找不到状态行，则重新抛出
         * getInputStream 抛出的任何异常。
         */
        String statusLine = getHeaderField(0);
        if (statusLine == null) {
            if (exc != null) {
                if (exc instanceof RuntimeException)
                    throw (RuntimeException)exc;
                else
                    throw (IOException)exc;
            }
            return -1;
        }

        /*
         * 检查状态行 - 应该按照
         * RFC 2616 第 6.1 节的格式：
         *
         * Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase
         *
         * 如果状态行无法解析，返回 -1。
         */
        if (statusLine.startsWith("HTTP/1.")) {
            int codePos = statusLine.indexOf(' ');
            if (codePos > 0) {

                int phrasePos = statusLine.indexOf(' ', codePos+1);
                if (phrasePos > 0 && phrasePos < statusLine.length()) {
                    responseMessage = statusLine.substring(phrasePos+1);
                }

                // 偏离 RFC 2616 - 如果 SP Reason-Phrase 未包含，
                // 不拒绝状态行。
                if (phrasePos < 0)
                    phrasePos = statusLine.length();

                try {
                    responseCode = Integer.parseInt
                            (statusLine.substring(codePos+1, phrasePos));
                    return responseCode;
                } catch (NumberFormatException e) { }
            }
        }
        return -1;
    }

    /**
     * 获取服务器返回的 HTTP 响应消息（如果有），与
     * 响应码一起。从响应中：
     * <PRE>
     * HTTP/1.0 200 OK
     * HTTP/1.0 404 Not Found
     * </PRE>
     * 分别提取字符串 "OK" 和 "Not Found"。
     * 如果无法从响应中识别，则返回 null
     * （结果不是有效的 HTTP）。
     * @throws IOException 如果连接到服务器时发生错误。
     * @return HTTP 响应消息，或 {@code null}
     */
    public String getResponseMessage() throws IOException {
        getResponseCode();
        return responseMessage;
    }

    @SuppressWarnings("deprecation")
    public long getHeaderFieldDate(String name, long Default) {
        String dateString = getHeaderField(name);
        try {
            if (dateString.indexOf("GMT") == -1) {
                dateString = dateString+" GMT";
            }
            return Date.parse(dateString);
        } catch (Exception e) {
        }
        return Default;
    }


    /**
     * 表示在不久的将来
     * 不太可能向服务器发出其他请求。调用 disconnect()
     * 不应暗示此 HttpURLConnection
     * 实例可以用于其他请求。
     */
    public abstract void disconnect();

                /**
     * 指示连接是否通过代理。
     * @return 一个布尔值，指示连接是否
     * 使用代理。
     */
    public abstract boolean usingProxy();

    /**
     * 返回一个 {@link SocketPermission} 对象，表示连接到目标主机和端口所需的
     * 权限。
     *
     * @exception IOException 如果在计算权限时发生错误。
     *
     * @return 一个 {@code SocketPermission} 对象，表示连接到目标
     *         主机和端口所需的权限。
     */
    public Permission getPermission() throws IOException {
        int port = url.getPort();
        port = port < 0 ? 80 : port;
        String host = url.getHost() + ":" + port;
        Permission permission = new SocketPermission(host, "connect");
        return permission;
    }

   /**
    * 如果连接失败但服务器仍然发送了有用的数据，则返回错误流。典型的例子是
    * 当HTTP服务器响应404时，这将在连接时引发FileNotFoundException，
    * 但服务器发送了一个包含建议的HTML帮助页面。
    *
    * <p>此方法不会导致连接被初始化。如果连接未连接，或者服务器在连接时没有错误，
    * 或者服务器有错误但没有发送错误数据，此方法将返回null。这是默认行为。
    *
    * @return 如果有错误则返回错误流，如果没有错误，连接未连接或服务器未发送
    * 任何有用数据，则返回null。
    */
    public InputStream getErrorStream() {
        return null;
    }

    /**
     * HTTP 1.1 版本的响应代码。
     */

    // REMIND: 我们需要所有这些吗？？
    // 还有其他我们需要的吗？？

    /* 2XX: 通常表示“成功” */

    /**
     * HTTP 状态码 200: 成功。
     */
    public static final int HTTP_OK = 200;

    /**
     * HTTP 状态码 201: 已创建。
     */
    public static final int HTTP_CREATED = 201;

    /**
     * HTTP 状态码 202: 已接受。
     */
    public static final int HTTP_ACCEPTED = 202;

    /**
     * HTTP 状态码 203: 非权威信息。
     */
    public static final int HTTP_NOT_AUTHORITATIVE = 203;

    /**
     * HTTP 状态码 204: 无内容。
     */
    public static final int HTTP_NO_CONTENT = 204;

    /**
     * HTTP 状态码 205: 重置内容。
     */
    public static final int HTTP_RESET = 205;

    /**
     * HTTP 状态码 206: 部分内容。
     */
    public static final int HTTP_PARTIAL = 206;

    /* 3XX: 重定向 */

    /**
     * HTTP 状态码 300: 多重选择。
     */
    public static final int HTTP_MULT_CHOICE = 300;

    /**
     * HTTP 状态码 301: 永久移动。
     */
    public static final int HTTP_MOVED_PERM = 301;

    /**
     * HTTP 状态码 302: 临时重定向。
     */
    public static final int HTTP_MOVED_TEMP = 302;

    /**
     * HTTP 状态码 303: 查看其他。
     */
    public static final int HTTP_SEE_OTHER = 303;

    /**
     * HTTP 状态码 304: 未修改。
     */
    public static final int HTTP_NOT_MODIFIED = 304;

    /**
     * HTTP 状态码 305: 使用代理。
     */
    public static final int HTTP_USE_PROXY = 305;

    /* 4XX: 客户端错误 */

    /**
     * HTTP 状态码 400: 错误请求。
     */
    public static final int HTTP_BAD_REQUEST = 400;

    /**
     * HTTP 状态码 401: 未授权。
     */
    public static final int HTTP_UNAUTHORIZED = 401;

    /**
     * HTTP 状态码 402: 需要付款。
     */
    public static final int HTTP_PAYMENT_REQUIRED = 402;

    /**
     * HTTP 状态码 403: 禁止。
     */
    public static final int HTTP_FORBIDDEN = 403;

    /**
     * HTTP 状态码 404: 未找到。
     */
    public static final int HTTP_NOT_FOUND = 404;

    /**
     * HTTP 状态码 405: 方法不允许。
     */
    public static final int HTTP_BAD_METHOD = 405;

    /**
     * HTTP 状态码 406: 不可接受。
     */
    public static final int HTTP_NOT_ACCEPTABLE = 406;

    /**
     * HTTP 状态码 407: 需要代理认证。
     */
    public static final int HTTP_PROXY_AUTH = 407;

    /**
     * HTTP 状态码 408: 请求超时。
     */
    public static final int HTTP_CLIENT_TIMEOUT = 408;

    /**
     * HTTP 状态码 409: 冲突。
     */
    public static final int HTTP_CONFLICT = 409;

    /**
     * HTTP 状态码 410: 已删除。
     */
    public static final int HTTP_GONE = 410;

    /**
     * HTTP 状态码 411: 需要长度。
     */
    public static final int HTTP_LENGTH_REQUIRED = 411;

    /**
     * HTTP 状态码 412: 前提条件失败。
     */
    public static final int HTTP_PRECON_FAILED = 412;

    /**
     * HTTP 状态码 413: 请求实体过大。
     */
    public static final int HTTP_ENTITY_TOO_LARGE = 413;

    /**
     * HTTP 状态码 414: 请求-URI 过大。
     */
    public static final int HTTP_REQ_TOO_LONG = 414;

    /**
     * HTTP 状态码 415: 不支持的媒体类型。
     */
    public static final int HTTP_UNSUPPORTED_TYPE = 415;

    /* 5XX: 服务器错误 */

    /**
     * HTTP 状态码 500: 服务器内部错误。
     * @deprecated 它被错误地放置，不应该存在。
     */
    @Deprecated
    public static final int HTTP_SERVER_ERROR = 500;

    /**
     * HTTP 状态码 500: 服务器内部错误。
     */
    public static final int HTTP_INTERNAL_ERROR = 500;

    /**
     * HTTP 状态码 501: 未实现。
     */
    public static final int HTTP_NOT_IMPLEMENTED = 501;

    /**
     * HTTP 状态码 502: 错误网关。
     */
    public static final int HTTP_BAD_GATEWAY = 502;

    /**
     * HTTP 状态码 503: 服务不可用。
     */
    public static final int HTTP_UNAVAILABLE = 503;

    /**
     * HTTP 状态码 504: 网关超时。
     */
    public static final int HTTP_GATEWAY_TIMEOUT = 504;

    /**
     * HTTP 状态码 505: HTTP 版本不支持。
     */
    public static final int HTTP_VERSION = 505;

}
