
/*
 * 版权所有 (c) 1995, 2016，Oracle 及/或其附属公司。保留所有权利。
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
import java.util.Hashtable;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.security.Permission;
import java.security.AccessController;
import sun.security.util.SecurityConstants;
import sun.net.www.MessageHeader;

/**
 * 抽象类 {@code URLConnection} 是所有表示应用程序与 URL 之间通信链接的类的超类。
 * 该类的实例可以用于读取和写入由 URL 引用的资源。通常，创建到 URL 的连接是一个多步骤的过程：
 *
 * <center><table border=2 summary="描述创建到 URL 的连接的过程：openConnection() 和 connect() 随时间的变化。">
 * <tr><th>{@code openConnection()}</th>
 *     <th>{@code connect()}</th></tr>
 * <tr><td>操作影响与远程资源连接的参数。</td>
 *     <td>与资源交互；查询头字段和内容。</td></tr>
 * </table>
 * ----------------------------&gt;
 * <br>时间</center>
 *
 * <ol>
 * <li>通过调用 URL 上的 {@code openConnection} 方法创建连接对象。
 * <li>操作设置参数和通用请求属性。
 * <li>使用 {@code connect} 方法建立与远程对象的实际连接。
 * <li>远程对象变得可用。可以访问远程对象的头字段和内容。
 * </ol>
 * <p>
 * 使用以下方法修改设置参数：
 * <ul>
 *   <li>{@code setAllowUserInteraction}
 *   <li>{@code setDoInput}
 *   <li>{@code setDoOutput}
 *   <li>{@code setIfModifiedSince}
 *   <li>{@code setUseCaches}
 * </ul>
 * <p>
 * 使用以下方法修改通用请求属性：
 * <ul>
 *   <li>{@code setRequestProperty}
 * </ul>
 * <p>
 * 可以使用方法 {@code setDefaultAllowUserInteraction} 和
 * {@code setDefaultUseCaches} 设置 {@code AllowUserInteraction} 和
 * {@code UseCaches} 参数的默认值。
 * <p>
 * 每个上述的 {@code set} 方法都有一个对应的 {@code get} 方法来检索参数或
 * 通用请求属性的值。适用的具体参数和通用请求属性是特定于协议的。
 * <p>
 * 使用以下方法在建立与远程对象的连接后访问头字段和内容：
 * <ul>
 *   <li>{@code getContent}
 *   <li>{@code getHeaderField}
 *   <li>{@code getInputStream}
 *   <li>{@code getOutputStream}
 * </ul>
 * <p>
 * 某些头字段经常被访问。以下方法：
 * <ul>
 *   <li>{@code getContentEncoding}
 *   <li>{@code getContentLength}
 *   <li>{@code getContentType}
 *   <li>{@code getDate}
 *   <li>{@code getExpiration}
 *   <li>{@code getLastModifed}
 * </ul>
 * <p>
 * 提供了对这些字段的便捷访问。{@code getContentType} 方法用于
 * {@code getContent} 方法确定远程对象的类型；子类可能会发现覆盖
 * {@code getContentType} 方法很方便。
 * <p>
 * 在大多数情况下，可以忽略所有预连接参数和通用请求属性：预连接
 * 参数和请求属性默认为合理的值。对于此接口的大多数客户端，只有两个有趣的方法：
 * {@code getInputStream} 和 {@code getContent}，这些方法在 {@code URL} 类中
 * 有对应的便捷方法。
 * <p>
 * 有关 {@code http} 连接的请求属性和头字段的更多信息可以在以下位置找到：
 * <blockquote><pre>
 * <a href="http://www.ietf.org/rfc/rfc2616.txt">http://www.ietf.org/rfc/rfc2616.txt</a>
 * </pre></blockquote>
 *
 * 在请求后调用 {@code URLConnection} 的 {@code InputStream} 或 {@code OutputStream} 的
 * {@code close()} 方法可能会释放与此实例相关联的网络资源，除非特定协议规范指定了不同的行为。
 *
 * @author  James Gosling
 * @see     java.net.URL#openConnection()
 * @see     java.net.URLConnection#connect()
 * @see     java.net.URLConnection#getContent()
 * @see     java.net.URLConnection#getContentEncoding()
 * @see     java.net.URLConnection#getContentLength()
 * @see     java.net.URLConnection#getContentType()
 * @see     java.net.URLConnection#getDate()
 * @see     java.net.URLConnection#getExpiration()
 * @see     java.net.URLConnection#getHeaderField(int)
 * @see     java.net.URLConnection#getHeaderField(java.lang.String)
 * @see     java.net.URLConnection#getInputStream()
 * @see     java.net.URLConnection#getLastModified()
 * @see     java.net.URLConnection#getOutputStream()
 * @see     java.net.URLConnection#setAllowUserInteraction(boolean)
 * @see     java.net.URLConnection#setDefaultUseCaches(boolean)
 * @see     java.net.URLConnection#setDoInput(boolean)
 * @see     java.net.URLConnection#setDoOutput(boolean)
 * @see     java.net.URLConnection#setIfModifiedSince(long)
 * @see     java.net.URLConnection#setRequestProperty(java.lang.String, java.lang.String)
 * @see     java.net.URLConnection#setUseCaches(boolean)
 * @since   JDK1.0
 */
public abstract class URLConnection {

   /**
     * URL 表示在万维网上此连接打开的远程对象。
     * <p>
     * 可以通过 {@code getURL} 方法访问此字段的值。
     * <p>
     * 此变量的默认值是 {@code URLConnection} 构造函数中的 URL 参数的值。
     *
     * @see     java.net.URLConnection#getURL()
     * @see     java.net.URLConnection#url
     */
    protected URL url;

               /**
     * 该变量由 {@code setDoInput} 方法设置。其值由 {@code getDoInput} 方法返回。
     * <p>
     * URL 连接可以用于输入和/或输出。将 {@code doInput} 标志设置为 {@code true} 表示
     * 应用程序打算从 URL 连接读取数据。
     * <p>
     * 该字段的默认值为 {@code true}。
     *
     * @see     java.net.URLConnection#getDoInput()
     * @see     java.net.URLConnection#setDoInput(boolean)
     */
    protected boolean doInput = true;

   /**
     * 该变量由 {@code setDoOutput} 方法设置。其值由 {@code getDoOutput} 方法返回。
     * <p>
     * URL 连接可以用于输入和/或输出。将 {@code doOutput} 标志设置为 {@code true} 表示
     * 应用程序打算向 URL 连接写入数据。
     * <p>
     * 该字段的默认值为 {@code false}。
     *
     * @see     java.net.URLConnection#getDoOutput()
     * @see     java.net.URLConnection#setDoOutput(boolean)
     */
    protected boolean doOutput = false;

    private static boolean defaultAllowUserInteraction = false;

   /**
     * 如果为 {@code true}，则此 {@code URL} 正在被检查的上下文中允许用户交互，
     * 例如弹出身份验证对话框。如果为 {@code false}，则不允许任何用户交互。
     * <p>
     * 该字段的值可以由 {@code setAllowUserInteraction} 方法设置。
     * 其值由 {@code getAllowUserInteraction} 方法返回。
     * 其默认值是最后一次调用 {@code setDefaultAllowUserInteraction} 方法时的参数值。
     *
     * @see     java.net.URLConnection#getAllowUserInteraction()
     * @see     java.net.URLConnection#setAllowUserInteraction(boolean)
     * @see     java.net.URLConnection#setDefaultAllowUserInteraction(boolean)
     */
    protected boolean allowUserInteraction = defaultAllowUserInteraction;

    private static boolean defaultUseCaches = true;

   /**
     * 如果为 {@code true}，则协议允许在任何时候使用缓存。如果为 {@code false}，则协议必须始终
     * 尝试获取对象的新副本。
     * <p>
     * 该字段由 {@code setUseCaches} 方法设置。其值由 {@code getUseCaches} 方法返回。
     * <p>
     * 其默认值是最后一次调用 {@code setDefaultUseCaches} 方法时的参数值。
     *
     * @see     java.net.URLConnection#setUseCaches(boolean)
     * @see     java.net.URLConnection#getUseCaches()
     * @see     java.net.URLConnection#setDefaultUseCaches(boolean)
     */
    protected boolean useCaches = defaultUseCaches;

   /**
     * 某些协议支持除非对象自某个时间以来已被修改，否则跳过对象的获取。
     * <p>
     * 非零值给出一个时间，以自 1970 年 1 月 1 日 GMT 以来的毫秒数表示。只有在对象自该时间以来已被修改的情况下，才会获取对象。
     * <p>
     * 该变量由 {@code setIfModifiedSince} 方法设置。其值由 {@code getIfModifiedSince} 方法返回。
     * <p>
     * 该字段的默认值为 {@code 0}，表示必须始终进行获取。
     *
     * @see     java.net.URLConnection#getIfModifiedSince()
     * @see     java.net.URLConnection#setIfModifiedSince(long)
     */
    protected long ifModifiedSince = 0;

   /**
     * 如果为 {@code false}，则此连接对象尚未创建到指定 URL 的通信链接。如果为 {@code true}，
     * 则已建立通信链接。
     */
    protected boolean connected = false;

    /**
     * @since 1.5
     */
    private int connectTimeout;
    private int readTimeout;

    /**
     * @since 1.6
     */
    private MessageHeader requests;

   /**
    * @since   JDK1.1
    */

    private static volatile FileNameMap fileNameMap;

    /**
     * 从数据文件加载文件名映射（一个 mimetable）。它首先尝试加载由 &quot;content.types.user.table&quot; 属性定义的用户特定表。
     * 如果失败，它将尝试加载默认的内置表。
     *
     * @return 文件名映射
     * @since 1.2
     * @see #setFileNameMap(java.net.FileNameMap)
     */
    public static FileNameMap getFileNameMap() {
        FileNameMap map = fileNameMap;

        if (map == null) {
            fileNameMap = map = new FileNameMap() {
                private FileNameMap internalMap =
                        sun.net.www.MimeTable.loadTable();

                public String getContentTypeFor(String fileName) {
                    return internalMap.getContentTypeFor(fileName);
                }
            };
        }

        return map;
    }

    /**
     * 设置 FileNameMap。
     * <p>
     * 如果存在安全管理器，此方法首先调用安全管理器的 {@code checkSetFactory} 方法
     * 以确保允许此操作。这可能导致 SecurityException。
     *
     * @param map 要设置的 FileNameMap
     * @exception  SecurityException 如果存在安全管理器且其
     *             {@code checkSetFactory} 方法不允许此操作。
     * @see        SecurityManager#checkSetFactory
     * @see #getFileNameMap()
     * @since 1.2
     */
    public static void setFileNameMap(FileNameMap map) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkSetFactory();
        fileNameMap = map;
    }

    /**
     * 如果尚未建立连接，则打开到此 URL 引用的资源的通信链接。
     * <p>
     * 如果在连接已经打开时（由 {@code connected} 字段的值为 {@code true} 表示）调用 {@code connect} 方法，
     * 则调用将被忽略。
     * <p>
     * URLConnection 对象经历两个阶段：首先被创建，然后被连接。在创建后且连接前，
     * 可以设置各种选项（例如 doInput 和 UseCaches）。连接后，尝试设置它们将导致错误。
     * 依赖于已连接的操作（如 getContentLength）将在必要时隐式执行连接。
     *
     * @throws SocketTimeoutException 如果在建立连接之前超时到期
     * @exception  IOException 如果在打开连接时发生 I/O 错误。
     * @see java.net.URLConnection#connected
     * @see #getConnectTimeout()
     * @see #setConnectTimeout(int)
     */
    abstract public void connect() throws IOException;

                /**
     * 设置一个指定的超时值（以毫秒为单位），用于打开与该 URLConnection 引用的资源的通信链接。如果在建立连接之前超时过期，
     * 将引发 java.net.SocketTimeoutException。超时为零表示无限超时。

     * <p> 一些非标准的实现可能忽略指定的超时。要查看设置的连接超时，请调用 getConnectTimeout()。
     *
     * @param timeout 一个 {@code int}，指定连接超时值（以毫秒为单位）
     * @throws IllegalArgumentException 如果超时参数为负数
     *
     * @see #getConnectTimeout()
     * @see #connect()
     * @since 1.5
     */
    public void setConnectTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout can not be negative");
        }
        connectTimeout = timeout;
    }

    /**
     * 返回连接超时设置。
     * <p>
     * 0 表示该选项已禁用（即无限超时）。
     *
     * @return 一个 {@code int}，指示连接超时值（以毫秒为单位）
     * @see #setConnectTimeout(int)
     * @see #connect()
     * @since 1.5
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 设置读取超时为指定的超时值（以毫秒为单位）。非零值指定在与资源建立连接后从输入流读取时的超时。如果在有数据可读之前超时过期，
     * 将引发 java.net.SocketTimeoutException。超时为零表示无限超时。
     *
     *<p> 一些非标准的实现可能忽略指定的超时。要查看设置的读取超时，请调用 getReadTimeout()。
     *
     * @param timeout 一个 {@code int}，指定超时值（以毫秒为单位）
     * @throws IllegalArgumentException 如果超时参数为负数
     *
     * @see #getReadTimeout()
     * @see InputStream#read()
     * @since 1.5
     */
    public void setReadTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout can not be negative");
        }
        readTimeout = timeout;
    }

    /**
     * 返回读取超时设置。0 表示该选项已禁用（即无限超时）。
     *
     * @return 一个 {@code int}，指示读取超时值（以毫秒为单位）
     *
     * @see #setReadTimeout(int)
     * @see InputStream#read()
     * @since 1.5
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * 构建到指定 URL 的 URL 连接。不会创建与 URL 引用的对象的连接。
     *
     * @param   url   指定的 URL。
     */
    protected URLConnection(URL url) {
        this.url = url;
    }

    /**
     * 返回此 {@code URLConnection} 的 {@code URL} 字段的值。
     *
     * @return  此 {@code URLConnection} 的 {@code URL} 字段的值。
     * @see     java.net.URLConnection#url
     */
    public URL getURL() {
        return url;
    }

    /**
     * 返回 {@code content-length} 头字段的值。
     * <P>
     * <B>注意</B>：应优先使用 {@link #getContentLengthLong() getContentLengthLong()} 方法，因为它返回一个 {@code long} 类型，
     * 因此更具可移植性。</P>
     *
     * @return  此连接 URL 引用的资源的内容长度，如果内容长度未知，或者内容长度大于 Integer.MAX_VALUE，则返回 {@code -1}。
     */
    public int getContentLength() {
        long l = getContentLengthLong();
        if (l > Integer.MAX_VALUE)
            return -1;
        return (int) l;
    }

    /**
     * 返回 {@code content-length} 头字段的值，作为 long 类型。
     *
     * @return  此连接 URL 引用的资源的内容长度，如果内容长度未知，则返回 {@code -1}。
     * @since 7.0
     */
    public long getContentLengthLong() {
        return getHeaderFieldLong("content-length", -1);
    }

    /**
     * 返回 {@code content-type} 头字段的值。
     *
     * @return  URL 引用的资源的内容类型，如果未知，则返回 {@code null}。
     * @see     java.net.URLConnection#getHeaderField(java.lang.String)
     */
    public String getContentType() {
        return getHeaderField("content-type");
    }

    /**
     * 返回 {@code content-encoding} 头字段的值。
     *
     * @return  URL 引用的资源的内容编码，如果未知，则返回 {@code null}。
     * @see     java.net.URLConnection#getHeaderField(java.lang.String)
     */
    public String getContentEncoding() {
        return getHeaderField("content-encoding");
    }

    /**
     * 返回 {@code expires} 头字段的值。
     *
     * @return  此 URL 引用的资源的过期日期，如果未知，则返回 0。返回值是自 1970 年 1 月 1 日 GMT 以来的毫秒数。
     * @see     java.net.URLConnection#getHeaderField(java.lang.String)
     */
    public long getExpiration() {
        return getHeaderFieldDate("expires", 0);
    }

    /**
     * 返回 {@code date} 头字段的值。
     *
     * @return  URL 引用的资源的发送日期，如果未知，则返回 {@code 0}。返回值是自 1970 年 1 月 1 日 GMT 以来的毫秒数。
     * @see     java.net.URLConnection#getHeaderField(java.lang.String)
     */
    public long getDate() {
        return getHeaderFieldDate("date", 0);
    }

                /**
     * 返回 {@code last-modified} 标头字段的值。
     * 结果是从 1970 年 1 月 1 日 GMT 开始的毫秒数。
     *
     * @return  由 {@code URLConnection} 引用的资源最后修改的日期，如果未知则返回 0。
     * @see     java.net.URLConnection#getHeaderField(java.lang.String)
     */
    public long getLastModified() {
        return getHeaderFieldDate("last-modified", 0);
    }

    /**
     * 返回指定名称的标头字段的值。
     * <p>
     * 如果在设置同一标头多次的连接上调用此方法，则仅返回最后一个值。
     *
     *
     * @param   name   标头字段的名称。
     * @return  指定名称的标头字段的值，如果标头中没有该字段，则返回 {@code null}。
     */
    public String getHeaderField(String name) {
        return null;
    }

    /**
     * 返回标头字段的不可修改映射。
     * 映射的键是表示响应标头字段名称的字符串。每个映射值都是一个不可修改的字符串列表，
     * 表示相应的字段值。
     *
     * @return 标头字段的映射
     * @since 1.4
     */
    public Map<String,List<String>> getHeaderFields() {
        return Collections.emptyMap();
    }

    /**
     * 返回解析为数字的指定字段的值。
     * <p>
     * 此形式的 {@code getHeaderField} 存在是因为某些连接类型（例如 {@code http-ng}）具有预解析的标头。
     * 该连接类型的类可以重写此方法并绕过解析。
     *
     * @param   name      标头字段的名称。
     * @param   Default   默认值。
     * @return  指定字段的值，解析为整数。如果字段缺失或格式错误，则返回 {@code Default} 值。
     */
    public int getHeaderFieldInt(String name, int Default) {
        String value = getHeaderField(name);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) { }
        return Default;
    }

    /**
     * 返回解析为数字的指定字段的值。
     * <p>
     * 此形式的 {@code getHeaderField} 存在是因为某些连接类型（例如 {@code http-ng}）具有预解析的标头。
     * 该连接类型的类可以重写此方法并绕过解析。
     *
     * @param   name      标头字段的名称。
     * @param   Default   默认值。
     * @return  指定字段的值，解析为长整数。如果字段缺失或格式错误，则返回 {@code Default} 值。
     * @since 7.0
     */
    public long getHeaderFieldLong(String name, long Default) {
        String value = getHeaderField(name);
        try {
            return Long.parseLong(value);
        } catch (Exception e) { }
        return Default;
    }

    /**
     * 返回解析为日期的指定字段的值。
     * 结果是从 1970 年 1 月 1 日 GMT 开始的毫秒数，由指定字段表示。
     * <p>
     * 此形式的 {@code getHeaderField} 存在是因为某些连接类型（例如 {@code http-ng}）具有预解析的标头。
     * 该连接类型的类可以重写此方法并绕过解析。
     *
     * @param   name     标头字段的名称。
     * @param   Default   默认值。
     * @return  指定字段的值，解析为日期。如果字段缺失或格式错误，则返回 {@code Default} 参数的值。
     */
    @SuppressWarnings("deprecation")
    public long getHeaderFieldDate(String name, long Default) {
        String value = getHeaderField(name);
        try {
            return Date.parse(value);
        } catch (Exception e) { }
        return Default;
    }

    /**
     * 返回第 {@code n} 个标头字段的键。
     * 如果字段少于 {@code n+1} 个，则返回 {@code null}。
     *
     * @param   n   索引，其中 {@code n>=0}
     * @return  第 {@code n} 个标头字段的键，如果字段少于 {@code n+1} 个，则返回 {@code null}。
     */
    public String getHeaderFieldKey(int n) {
        return null;
    }

    /**
     * 返回第 {@code n} 个标头字段的值。
     * 如果字段少于 {@code n+1} 个，则返回 {@code null}。
     * <p>
     * 该方法可以与 {@link #getHeaderFieldKey(int) getHeaderFieldKey} 方法结合使用，以遍历消息中的所有标头。
     *
     * @param   n   索引，其中 {@code n>=0}
     * @return  第 {@code n} 个标头字段的值，如果字段少于 {@code n+1} 个，则返回 {@code null}
     * @see     java.net.URLConnection#getHeaderFieldKey(int)
     */
    public String getHeaderField(int n) {
        return null;
    }

    /**
     * 检索此 URL 连接的内容。
     * <p>
     * 该方法首先通过调用 {@code getContentType} 方法确定对象的内容类型。如果这是应用程序第一次看到该特定内容类型，
     * 则为该内容类型创建一个内容处理器：
     * <ol>
     * <li>如果应用程序已使用 {@code setContentHandlerFactory} 方法设置了一个内容处理器工厂实例，
     *     则调用该实例的 {@code createContentHandler} 方法，并将内容类型作为参数传递；结果是该内容类型的内容处理器。
     * <li>如果尚未设置内容处理器工厂，或者工厂的 {@code createContentHandler} 方法返回 {@code null}，
     *     则应用程序加载名为：
     *     <blockquote><pre>
     *         sun.net.www.content.&lt;<i>contentType</i>&gt;
     *     </pre></blockquote>
     *     的类，其中 &lt;<i>contentType</i>&gt; 是通过取内容类型字符串，将所有斜杠字符替换为句点（'.'），
     *     并将所有其他非字母数字字符替换为下划线字符 '{@code _}' 形成的。字母数字字符具体是指 26 个大写 ASCII 字母
     *     '{@code A}' 到 '{@code Z}'，26 个小写 ASCII 字母 '{@code a}' 到 '{@code z}'，以及 10 个 ASCII 数字
     *     '{@code 0}' 到 '{@code 9}'。如果指定的类不存在，或者不是 {@code ContentHandler} 的子类，
     *     则抛出 {@code UnknownServiceException}。
     * </ol>
     *
     * @return     检索到的对象。应使用 {@code instanceof} 运算符确定返回的具体对象类型。
     * @exception  IOException              如果在获取内容时发生 I/O 错误。
     * @exception  UnknownServiceException  如果协议不支持内容类型。
     * @see        java.net.ContentHandlerFactory#createContentHandler(java.lang.String)
     * @see        java.net.URLConnection#getContentType()
     * @see        java.net.URLConnection#setContentHandlerFactory(java.net.ContentHandlerFactory)
     */
    public Object getContent() throws IOException {
        // 必须在调用 GetHeaderField 之前调用 getInputStream，以便在没有被捕获的情况下有机会从这里抛出 FileNotFoundException
        getInputStream();
        return getContentHandler().getContent(this);
    }

                /**
     * 获取此 URL 连接的内容。
     *
     * @param classes 表示请求类型的 {@code Class} 数组
     * @return     从 classes 数组中指定的类型中匹配的第一个对象。如果请求的类型都不支持，则返回 null。
     *               应使用 {@code instanceof} 操作符来确定返回对象的具体类型。
     * @exception  IOException              如果在获取内容时发生 I/O 错误。
     * @exception  UnknownServiceException  如果协议不支持内容类型。
     * @see        java.net.URLConnection#getContent()
     * @see        java.net.ContentHandlerFactory#createContentHandler(java.lang.String)
     * @see        java.net.URLConnection#getContent(java.lang.Class[])
     * @see        java.net.URLConnection#setContentHandlerFactory(java.net.ContentHandlerFactory)
     * @since 1.3
     */
    public Object getContent(Class[] classes) throws IOException {
        // 必须在调用 GetHeaderField 之前调用 getInputStream
        // 这样才能有机会在不被捕获的情况下从这里抛出 FileNotFoundException
        getInputStream();
        return getContentHandler().getContent(this, classes);
    }

    /**
     * 返回一个表示此对象表示的连接所需权限的权限对象。如果不需要权限来建立连接，则此方法返回 null。默认情况下，此方法
     * 返回 {@code java.security.AllPermission}。子类应覆盖此方法并返回最能代表建立到 URL 的连接所需权限的权限。
     * 例如，表示 {@code file:} URL 的 {@code URLConnection} 将返回一个 {@code java.io.FilePermission} 对象。
     *
     * <p>返回的权限可能取决于连接的状态。例如，连接前的权限可能与连接后的权限不同。例如，HTTP
     * 服务器（如 foo.com）可能会将连接重定向到不同的主机（如 bar.com）。在连接之前，连接返回的权限将表示连接
     * 到 foo.com 所需的权限，而在连接后返回的权限将是到 bar.com 的权限。
     *
     * <p>权限通常用于两个目的：保护通过 URLConnections 获取的对象的缓存，以及检查接收者了解特定 URL 的权利。
     * 在第一种情况下，应在获取对象后获取权限。例如，在 HTTP 连接中，这将表示连接到实际获取数据的主机的权限。
     * 在第二种情况下，应在连接前获取并测试权限。
     *
     * @return 表示此 URLConnection 所表示的连接所需权限的权限对象。
     *
     * @exception IOException 如果在计算权限时需要网络或文件 I/O 并且在计算过程中发生异常。
     */
    public Permission getPermission() throws IOException {
        return SecurityConstants.ALL_PERMISSION;
    }

    /**
     * 返回一个从这个打开的连接读取的输入流。
     *
     * 如果读取超时在数据可用之前到期，则从返回的输入流读取时可能会抛出 SocketTimeoutException。
     *
     * @return     一个从这个打开的连接读取的输入流。
     * @exception  IOException              如果在创建输入流时发生 I/O 错误。
     * @exception  UnknownServiceException  如果协议不支持输入。
     * @see #setReadTimeout(int)
     * @see #getReadTimeout()
     */
    public InputStream getInputStream() throws IOException {
        throw new UnknownServiceException("protocol doesn't support input");
    }

    /**
     * 返回一个写入此连接的输出流。
     *
     * @return     一个写入此连接的输出流。
     * @exception  IOException              如果在创建输出流时发生 I/O 错误。
     * @exception  UnknownServiceException  如果协议不支持输出。
     */
    public OutputStream getOutputStream() throws IOException {
        throw new UnknownServiceException("protocol doesn't support output");
    }

    /**
     * 返回此 URL 连接的字符串表示形式。
     *
     * @return  此 {@code URLConnection} 的字符串表示形式。
     */
    public String toString() {
        return this.getClass().getName() + ":" + url;
    }

    /**
     * 将此 {@code URLConnection} 的 {@code doInput} 字段设置为指定的值。
     * <p>
     * URL 连接可以用于输入和/或输出。如果打算使用 URL 连接进行输入，则将 DoInput
     * 标志设置为 true，否则设置为 false。默认值为 true。
     *
     * @param   doinput   新值。
     * @throws IllegalStateException 如果已连接
     * @see     java.net.URLConnection#doInput
     * @see #getDoInput()
     */
    public void setDoInput(boolean doinput) {
        if (connected)
            throw new IllegalStateException("Already connected");
        doInput = doinput;
    }

    /**
     * 返回此 {@code URLConnection} 的 {@code doInput} 标志的值。
     *
     * @return  此 {@code URLConnection} 的 {@code doInput} 标志的值。
     * @see     #setDoInput(boolean)
     */
    public boolean getDoInput() {
        return doInput;
    }

    /**
     * 将此 {@code URLConnection} 的 {@code doOutput} 字段设置为指定的值。
     * <p>
     * URL 连接可以用于输入和/或输出。如果打算使用 URL 连接进行输出，则将 DoOutput
     * 标志设置为 true，否则设置为 false。默认值为 false。
     *
     * @param   dooutput   新值。
     * @throws IllegalStateException 如果已连接
     * @see #getDoOutput()
     */
    public void setDoOutput(boolean dooutput) {
        if (connected)
            throw new IllegalStateException("Already connected");
        doOutput = dooutput;
    }

                /**
     * 返回此 {@code URLConnection} 的
     * {@code doOutput} 标志的值。
     *
     * @return  此 {@code URLConnection} 的
     *          {@code doOutput} 标志的值。
     * @see     #setDoOutput(boolean)
     */
    public boolean getDoOutput() {
        return doOutput;
    }

    /**
     * 设置此 {@code URLConnection} 的
     * {@code allowUserInteraction} 字段的值。
     *
     * @param   allowuserinteraction   新值。
     * @throws IllegalStateException 如果已连接
     * @see     #getAllowUserInteraction()
     */
    public void setAllowUserInteraction(boolean allowuserinteraction) {
        if (connected)
            throw new IllegalStateException("Already connected");
        allowUserInteraction = allowuserinteraction;
    }

    /**
     * 返回此对象的
     * {@code allowUserInteraction} 字段的值。
     *
     * @return  此对象的
     *          {@code allowUserInteraction} 字段的值。
     * @see     #setAllowUserInteraction(boolean)
     */
    public boolean getAllowUserInteraction() {
        return allowUserInteraction;
    }

    /**
     * 将所有未来的 {@code URLConnection} 对象的
     * {@code allowUserInteraction} 字段的默认值设置为指定值。
     *
     * @param   defaultallowuserinteraction   新值。
     * @see     #getDefaultAllowUserInteraction()
     */
    public static void setDefaultAllowUserInteraction(boolean defaultallowuserinteraction) {
        defaultAllowUserInteraction = defaultallowuserinteraction;
    }

    /**
     * 返回 {@code allowUserInteraction}
     * 字段的默认值。
     * <p>
     * 此默认值是“粘性的”，是所有 URLConnections 的静态状态的一部分。此标志适用于下一个及所有随后创建的 URLConnections。
     *
     * @return  {@code allowUserInteraction}
     *          字段的默认值。
     * @see     #setDefaultAllowUserInteraction(boolean)
     */
    public static boolean getDefaultAllowUserInteraction() {
        return defaultAllowUserInteraction;
    }

    /**
     * 将此 {@code URLConnection} 的
     * {@code useCaches} 字段的值设置为指定值。
     * <p>
     * 某些协议会缓存文档。有时，重要的是能够“穿透”并忽略缓存（例如，浏览器中的“重新加载”按钮）。如果连接的 UseCaches 标志为 true，则允许连接使用所有可能的缓存。如果为 false，则忽略缓存。默认值来自 DefaultUseCaches，默认为 true。
     *
     * @param usecaches 一个 {@code boolean} 值，指示是否允许缓存
     * @throws IllegalStateException 如果已连接
     * @see #getUseCaches()
     */
    public void setUseCaches(boolean usecaches) {
        if (connected)
            throw new IllegalStateException("Already connected");
        useCaches = usecaches;
    }

    /**
     * 返回此 {@code URLConnection} 的
     * {@code useCaches} 字段的值。
     *
     * @return  此 {@code URLConnection} 的
     *          {@code useCaches} 字段的值。
     * @see #setUseCaches(boolean)
     */
    public boolean getUseCaches() {
        return useCaches;
    }

    /**
     * 将此 {@code URLConnection} 的
     * {@code ifModifiedSince} 字段的值设置为指定值。
     *
     * @param   ifmodifiedsince   新值。
     * @throws IllegalStateException 如果已连接
     * @see     #getIfModifiedSince()
     */
    public void setIfModifiedSince(long ifmodifiedsince) {
        if (connected)
            throw new IllegalStateException("Already connected");
        ifModifiedSince = ifmodifiedsince;
    }

    /**
     * 返回此对象的 {@code ifModifiedSince} 字段的值。
     *
     * @return  此对象的 {@code ifModifiedSince} 字段的值。
     * @see #setIfModifiedSince(long)
     */
    public long getIfModifiedSince() {
        return ifModifiedSince;
    }

   /**
     * 返回 {@code URLConnection} 的
     * {@code useCaches} 标志的默认值。
     * <p>
     * 此默认值是“粘性的”，是所有 URLConnections 的静态状态的一部分。此标志适用于下一个及所有随后创建的 URLConnections。
     *
     * @return  {@code URLConnection} 的
     *          {@code useCaches} 标志的默认值。
     * @see     #setDefaultUseCaches(boolean)
     */
    public boolean getDefaultUseCaches() {
        return defaultUseCaches;
    }

   /**
     * 将 {@code useCaches} 字段的默认值设置为
     * 指定值。
     *
     * @param   defaultusecaches   新值。
     * @see     #getDefaultUseCaches()
     */
    public void setDefaultUseCaches(boolean defaultusecaches) {
        defaultUseCaches = defaultusecaches;
    }

    /**
     * 设置通用请求属性。如果已存在具有相同键的属性，则用新值覆盖其值。
     *
     * <p> 注意：HTTP 要求所有可以合法地具有多个相同键实例的请求属性
     * 使用逗号分隔列表语法，这使得多个属性可以合并为一个属性。
     *
     * @param   key     请求所知的关键词
     *                  （例如，“{@code Accept}”）。
     * @param   value   与之关联的值。
     * @throws IllegalStateException 如果已连接
     * @throws NullPointerException 如果键为 <CODE>null</CODE>
     * @see #getRequestProperty(java.lang.String)
     */
    public void setRequestProperty(String key, String value) {
        if (connected)
            throw new IllegalStateException("Already connected");
        if (key == null)
            throw new NullPointerException ("key is null");

        if (requests == null)
            requests = new MessageHeader();

        requests.set(key, value);
    }


/**
 * 添加由键值对指定的一般请求属性。此方法不会覆盖与相同键关联的现有值。
 *
 * @param   key     请求的关键词（例如，“{@code Accept}”）。
 * @param   value  与之关联的值。
 * @throws IllegalStateException 如果已连接
 * @throws NullPointerException 如果键为 null
 * @see #getRequestProperties()
 * @since 1.4
 */
public void addRequestProperty(String key, String value) {
    if (connected)
        throw new IllegalStateException("Already connected");
    if (key == null)
        throw new NullPointerException ("key is null");

    if (requests == null)
        requests = new MessageHeader();

    requests.add(key, value);
}


/**
 * 返回此连接的命名一般请求属性的值。
 *
 * @param key 请求的关键词（例如，“Accept”）。
 * @return  此连接的命名一般请求属性的值。如果键为 null，则返回 null。
 * @throws IllegalStateException 如果已连接
 * @see #setRequestProperty(java.lang.String, java.lang.String)
 */
public String getRequestProperty(String key) {
    if (connected)
        throw new IllegalStateException("Already connected");

    if (requests == null)
        return null;

    return requests.findValue(key);
}

/**
 * 返回此连接的一般请求属性的不可修改 Map。Map 的键是表示请求头字段名称的字符串。每个 Map 值是一个不可修改的字符串列表，表示相应的字段值。
 *
 * @return  此连接的一般请求属性的 Map。
 * @throws IllegalStateException 如果已连接
 * @since 1.4
 */
public Map<String,List<String>> getRequestProperties() {
    if (connected)
        throw new IllegalStateException("Already connected");

    if (requests == null)
        return Collections.emptyMap();

    return requests.getHeaders(null);
}

/**
 * 设置一般请求属性的默认值。当创建 {@code URLConnection} 时，它将使用这些属性进行初始化。
 *
 * @param   key     请求的关键词（例如，“{@code Accept}”）。
 * @param   value   与键关联的值。
 *
 * @see java.net.URLConnection#setRequestProperty(java.lang.String,java.lang.String)
 *
 * @deprecated 应在获取适当的 URLConnection 实例后使用实例特定的 setRequestProperty 方法。调用此方法将不会产生任何效果。
 *
 * @see #getDefaultRequestProperty(java.lang.String)
 */
@Deprecated
public static void setDefaultRequestProperty(String key, String value) {
}

/**
 * 返回默认请求属性的值。默认请求属性为每个连接设置。
 *
 * @param key 请求的关键词（例如，“Accept”）。
 * @return  指定键的默认请求属性的值。
 *
 * @see java.net.URLConnection#getRequestProperty(java.lang.String)
 *
 * @deprecated 应在获取适当的 URLConnection 实例后使用实例特定的 getRequestProperty 方法。
 *
 * @see #setDefaultRequestProperty(java.lang.String, java.lang.String)
 */
@Deprecated
public static String getDefaultRequestProperty(String key) {
    return null;
}

/**
 * 内容处理器工厂。
 */
static ContentHandlerFactory factory;

/**
 * 设置应用程序的 {@code ContentHandlerFactory}。应用程序最多只能调用一次此方法。
 * <p>
 * {@code ContentHandlerFactory} 实例用于从内容类型构建内容处理器。
 * <p>
 * 如果存在安全管理者，此方法首先调用安全管理者的 {@code checkSetFactory} 方法
 * 以确保操作被允许。这可能会导致 SecurityException。
 *
 * @param      fac   所需的工厂。
 * @exception  Error  如果工厂已定义。
 * @exception  SecurityException  如果存在安全管理者且其 {@code checkSetFactory} 方法不允许操作。
 * @see        java.net.ContentHandlerFactory
 * @see        java.net.URLConnection#getContent()
 * @see        SecurityManager#checkSetFactory
 */
public static synchronized void setContentHandlerFactory(ContentHandlerFactory fac) {
    if (factory != null) {
        throw new Error("factory already defined");
    }
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
        security.checkSetFactory();
    }
    factory = fac;
}

private static Hashtable<String, ContentHandler> handlers = new Hashtable<>();

/**
 * 获取适用于此连接的内容处理器。
 */
synchronized ContentHandler getContentHandler()
    throws UnknownServiceException
{
    String contentType = stripOffParameters(getContentType());
    ContentHandler handler = null;
    if (contentType == null)
        throw new UnknownServiceException("no content-type");
    try {
        handler = handlers.get(contentType);
        if (handler != null)
            return handler;
    } catch(Exception e) {
    }

    if (factory != null)
        handler = factory.createContentHandler(contentType);
    if (handler == null) {
        try {
            handler = lookupContentHandlerClassFor(contentType);
        } catch(Exception e) {
            e.printStackTrace();
            handler = UnknownContentHandler.INSTANCE;
        }
        handlers.put(contentType, handler);
    }
    return handler;
}


                /*
     * 媒体类型格式为：type/subtype*(; parameter)。
     * 在查找内容处理程序时，我们应该忽略这些参数。
     */
    private String stripOffParameters(String contentType)
    {
        if (contentType == null)
            return null;
        int index = contentType.indexOf(';');

        if (index > 0)
            return contentType.substring(0, index);
        else
            return contentType;
    }

    private static final String contentClassPrefix = "sun.net.www.content";
    private static final String contentPathProp = "java.content.handler.pkgs";

    /**
     * 在用户定义的一组位置中查找内容处理程序。
     * 默认情况下，它会在 sun.net.www.content 中查找，但用户可以通过定义
     * java.content.handler.pkgs 属性来指定一个用竖线分隔的类前缀列表进行搜索。
     * 类名必须具有以下形式：
     * <pre>
     *     {package-prefix}.{major}.{minor}
     * 例如：
     *     YoyoDyne.experimental.text.plain
     * </pre>
     */
    private ContentHandler lookupContentHandlerClassFor(String contentType)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String contentHandlerClassName = typeToPackageName(contentType);

        String contentHandlerPkgPrefixes = getContentHandlerPkgPrefixes();

        StringTokenizer packagePrefixIter =
            new StringTokenizer(contentHandlerPkgPrefixes, "|");

        while (packagePrefixIter.hasMoreTokens()) {
            String packagePrefix = packagePrefixIter.nextToken().trim();

            try {
                String clsName = packagePrefix + "." + contentHandlerClassName;
                Class<?> cls = null;
                try {
                    cls = Class.forName(clsName);
                } catch (ClassNotFoundException e) {
                    ClassLoader cl = ClassLoader.getSystemClassLoader();
                    if (cl != null) {
                        cls = cl.loadClass(clsName);
                    }
                }
                if (cls != null) {
                    ContentHandler handler =
                        (ContentHandler)cls.newInstance();
                    return handler;
                }
            } catch(Exception e) {
            }
        }

        return UnknownContentHandler.INSTANCE;
    }

    /**
     * 将 MIME 内容类型映射为等效的类名组件对。例如："text/html" 将返回 "text.html"
     */
    private String typeToPackageName(String contentType) {
        // 确保类名规范化：全部小写
        contentType = contentType.toLowerCase();
        int len = contentType.length();
        char nm[] = new char[len];
        contentType.getChars(0, len, nm, 0);
        for (int i = 0; i < len; i++) {
            char c = nm[i];
            if (c == '/') {
                nm[i] = '.';
            } else if (!('A' <= c && c <= 'Z' ||
                       'a' <= c && c <= 'z' ||
                       '0' <= c && c <= '9')) {
                nm[i] = '_';
            }
        }
        return new String(nm);
    }


    /**
     * 返回一个竖线分隔的潜在内容处理程序包前缀列表。尝试获取 java.content.handler.pkgs 属性
     * 作为要搜索的包前缀集。无论该属性是否已定义，sun.net.www.content 始终是返回的包列表中的最后一个。
     */
    private String getContentHandlerPkgPrefixes() {
        String packagePrefixList = AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction(contentPathProp, ""));

        if (packagePrefixList != "") {
            packagePrefixList += "|";
        }

        return packagePrefixList + contentClassPrefix;
    }

    /**
     * 尝试根据 URL 的指定“文件”组件确定对象的内容类型。
     * 这是一个方便的方法，可以被重写 {@code getContentType} 方法的子类使用。
     *
     * @param   fname   文件名。
     * @return  基于文件名猜测的对象的内容类型。
     * @see     java.net.URLConnection#getContentType()
     */
    public static String guessContentTypeFromName(String fname) {
        return getFileNameMap().getContentTypeFor(fname);
    }

    /**
     * 尝试根据输入流开头的字符确定输入流的类型。此方法可以被重写
     * {@code getContentType} 方法的子类使用。
     * <p>
     * 理想情况下，不需要此例程。但是许多
     * {@code http} 服务器返回错误的内容类型；此外，还有许多非标准扩展。直接检查
     * 字节以确定内容类型通常比相信 {@code http} 服务器声明的内容类型更准确。
     *
     * @param      is   支持标记的输入流。
     * @return     对内容类型的猜测，如果无法确定则返回 {@code null}。
     * @exception  IOException  如果读取输入流时发生 I/O 错误。
     * @see        java.io.InputStream#mark(int)
     * @see        java.io.InputStream#markSupported()
     * @see        java.net.URLConnection#getContentType()
     */
    static public String guessContentTypeFromStream(InputStream is)
                        throws IOException {
        // 如果不能安全地向前读取，就放弃猜测
        if (!is.markSupported())
            return null;

        is.mark(16);
        int c1 = is.read();
        int c2 = is.read();
        int c3 = is.read();
        int c4 = is.read();
        int c5 = is.read();
        int c6 = is.read();
        int c7 = is.read();
        int c8 = is.read();
        int c9 = is.read();
        int c10 = is.read();
        int c11 = is.read();
        int c12 = is.read();
        int c13 = is.read();
        int c14 = is.read();
        int c15 = is.read();
        int c16 = is.read();
        is.reset();


                    if (c1 == 0xCA && c2 == 0xFE && c3 == 0xBA && c4 == 0xBE) {
            return "application/java-vm";
        }

        if (c1 == 0xAC && c2 == 0xED) {
            // 下两个字节是版本号，目前为 0x00 0x05
            return "application/x-java-serialized-object";
        }

        if (c1 == '<') {
            if (c2 == '!'
                || ((c2 == 'h' && (c3 == 't' && c4 == 'm' && c5 == 'l' ||
                                   c3 == 'e' && c4 == 'a' && c5 == 'd') ||
                (c2 == 'b' && c3 == 'o' && c4 == 'd' && c5 == 'y'))) ||
                ((c2 == 'H' && (c3 == 'T' && c4 == 'M' && c5 == 'L' ||
                                c3 == 'E' && c4 == 'A' && c5 == 'D') ||
                (c2 == 'B' && c3 == 'O' && c4 == 'D' && c5 == 'Y')))) {
                return "text/html";
            }

            if (c2 == '?' && c3 == 'x' && c4 == 'm' && c5 == 'l' && c6 == ' ') {
                return "application/xml";
            }
        }

        // 大端和小端（相同）UTF-8 编码，带有 BOM
        if (c1 == 0xef &&  c2 == 0xbb &&  c3 == 0xbf) {
            if (c4 == '<' &&  c5 == '?' &&  c6 == 'x') {
                return "application/xml";
            }
        }

        // 大端和小端 UTF-16 编码，带有字节顺序标记
        if (c1 == 0xfe && c2 == 0xff) {
            if (c3 == 0 && c4 == '<' && c5 == 0 && c6 == '?' &&
                c7 == 0 && c8 == 'x') {
                return "application/xml";
            }
        }

        if (c1 == 0xff && c2 == 0xfe) {
            if (c3 == '<' && c4 == 0 && c5 == '?' && c6 == 0 &&
                c7 == 'x' && c8 == 0) {
                return "application/xml";
            }
        }

        // 大端和小端 UTF-32 编码，带有 BOM
        if (c1 == 0x00 &&  c2 == 0x00 &&  c3 == 0xfe &&  c4 == 0xff) {
            if (c5  == 0 && c6  == 0 && c7  == 0 && c8  == '<' &&
                c9  == 0 && c10 == 0 && c11 == 0 && c12 == '?' &&
                c13 == 0 && c14 == 0 && c15 == 0 && c16 == 'x') {
                return "application/xml";
            }
        }

        if (c1 == 0xff &&  c2 == 0xfe &&  c3 == 0x00 &&  c4 == 0x00) {
            if (c5  == '<' && c6  == 0 && c7  == 0 && c8  == 0 &&
                c9  == '?' && c10 == 0 && c11 == 0 && c12 == 0 &&
                c13 == 'x' && c14 == 0 && c15 == 0 && c16 == 0) {
                return "application/xml";
            }
        }

        if (c1 == 'G' && c2 == 'I' && c3 == 'F' && c4 == '8') {
            return "image/gif";
        }

        if (c1 == '#' && c2 == 'd' && c3 == 'e' && c4 == 'f') {
            return "image/x-bitmap";
        }

        if (c1 == '!' && c2 == ' ' && c3 == 'X' && c4 == 'P' &&
                        c5 == 'M' && c6 == '2') {
            return "image/x-pixmap";
        }

        if (c1 == 137 && c2 == 80 && c3 == 78 &&
                c4 == 71 && c5 == 13 && c6 == 10 &&
                c7 == 26 && c8 == 10) {
            return "image/png";
        }

        if (c1 == 0xFF && c2 == 0xD8 && c3 == 0xFF) {
            if (c4 == 0xE0 || c4 == 0xEE) {
                return "image/jpeg";
            }

            /**
             * 数码相机用于存储图像的文件格式。
             * Exif 格式可以被支持 JPEG 的任何应用程序读取。
             * Exif 规范可以在以下网址找到：
             * http://www.pima.net/standards/it10/PIMA15740/Exif_2-1.PDF
             */
            if ((c4 == 0xE1) &&
                (c7 == 'E' && c8 == 'x' && c9 == 'i' && c10 =='f' &&
                 c11 == 0)) {
                return "image/jpeg";
            }
        }

        if (c1 == 0xD0 && c2 == 0xCF && c3 == 0x11 && c4 == 0xE0 &&
            c5 == 0xA1 && c6 == 0xB1 && c7 == 0x1A && c8 == 0xE1) {

            /* 上面是 Microsoft Structured Storage 的签名。
             * 下面可以测试各种 SS 实体。
             * 目前，仅测试 FlashPix。
             */
            if (checkfpx(is)) {
                return "image/vnd.fpx";
            }
        }

        if (c1 == 0x2E && c2 == 0x73 && c3 == 0x6E && c4 == 0x64) {
            return "audio/basic";  // .au 格式，大端
        }

        if (c1 == 0x64 && c2 == 0x6E && c3 == 0x73 && c4 == 0x2E) {
            return "audio/basic";  // .au 格式，小端
        }

        if (c1 == 'R' && c2 == 'I' && c3 == 'F' && c4 == 'F') {
            /* 我不知道这是否是官方的，但有证据表明 .wav 文件以 "RIFF" 开头 - brown
             */
            return "audio/x-wav";
        }
        return null;
    }

    /**
     * 检查 InputStream is 中是否有 FlashPix 图像数据。如果流包含 FlashPix 数据，则返回 true，否则返回 false。
     * 在调用此方法之前，应已检查流是否包含 Microsoft Structured Storage 数据。
     */
    static private boolean checkfpx(InputStream is) throws IOException {

        /* 测试 Microsoft Structured Storage 格式中的 FlashPix 图像数据。
         * 通常，应通过调用 SS 实现来完成此操作。
         * 缺乏此功能时，需要通过偏移量来获取 FlashPix
         * ClassID。详情如下：
         *
         * 从流开始到 Fpx ClsID 的偏移量应为：
         *
         * FpxClsidOffset = rootEntryOffset + clsidOffset
         *
         * 其中：clsidOffset = 0x50。
         *        rootEntryOffset = headerSize + sectorSize*sectDirStart
         *                          + 128*rootEntryDirectory
         *
         *        其中：headerSize = 0x200（始终）
         *                sectorSize = 2 的 uSectorShift 次方，
         *                             uSectorShift 在头部的偏移量 0x1E 处找到。
         *                sectDirStart = 在头部的偏移量 0x30 处找到。
         *                rootEntryDirectory = 通常，应搜索标记为根的目录。
         *                                     我们假设值为 0（即，
         *                                     rootEntry 在第一个目录中）
         */


                    // 标记流，以便我们可以重置它。0x100 对于前几次读取已经足够，但是一旦计算出根目录条目的偏移量，
        // 就需要重置并再次设置标记。该偏移量可能非常大，并且直到从流中读取后才知道。
        is.mark(0x100);

        // 获取位于 0x1E 的字节顺序。0xFE 是 Intel，0xFF 是其他
        long toSkip = (long)0x1C;
        long posn;

        if ((posn = skipForward(is, toSkip)) < toSkip) {
          is.reset();
          return false;
        }

        int c[] = new int[16];
        if (readBytes(c, 2, is) < 0) {
            is.reset();
            return false;
        }

        int byteOrder = c[0];

        posn+=2;
        int uSectorShift;
        if (readBytes(c, 2, is) < 0) {
            is.reset();
            return false;
        }

        if(byteOrder == 0xFE) {
            uSectorShift = c[0];
            uSectorShift += c[1] << 8;
        }
        else {
            uSectorShift = c[0] << 8;
            uSectorShift += c[1];
        }

        posn += 2;
        toSkip = (long)0x30 - posn;
        long skipped = 0;
        if ((skipped = skipForward(is, toSkip)) < toSkip) {
          is.reset();
          return false;
        }
        posn += skipped;

        if (readBytes(c, 4, is) < 0) {
            is.reset();
            return false;
        }

        int sectDirStart;
        if(byteOrder == 0xFE) {
            sectDirStart = c[0];
            sectDirStart += c[1] << 8;
            sectDirStart += c[2] << 16;
            sectDirStart += c[3] << 24;
        } else {
            sectDirStart =  c[0] << 24;
            sectDirStart += c[1] << 16;
            sectDirStart += c[2] << 8;
            sectDirStart += c[3];
        }
        posn += 4;
        is.reset(); // 重置回开始位置

        toSkip = 0x200L + (long)(1<<uSectorShift)*sectDirStart + 0x50L;

        // 检查！
        if (toSkip < 0) {
            return false;
        }

        /*
         * 我们可以跳过多远？这里是否有性能问题？
         * 这个跳过可能相当长，至少在一个案例中为 0x4c650。必须假设跳过量适合 int。
         * 为读取整个根目录留出空间
         */
        is.mark((int)toSkip+0x30);

        if ((skipForward(is, toSkip)) < toSkip) {
            is.reset();
            return false;
        }

        /* 应该在 ClassID 的开始位置，其格式如下
         * （以 Intel 字节顺序）：
         *    00 67 61 56 54 C1 CE 11 85 53 00 AA 00 A1 F9 5B
         *
         * 这是从 Windows 存储的 long, short, short, char[8]。
         * 因此，对于字节顺序的变化，ClassID 的前 8 个字节的顺序会发生变化。
         *
         * 忽略第二个字节（Intel），因为这可能会根据 Fpx 文件的部分而变化。
         */

        if (readBytes(c, 16, is) < 0) {
            is.reset();
            return false;
        }

        // Intel 字节顺序
        if (byteOrder == 0xFE &&
            c[0] == 0x00 && c[2] == 0x61 && c[3] == 0x56 &&
            c[4] == 0x54 && c[5] == 0xC1 && c[6] == 0xCE &&
            c[7] == 0x11 && c[8] == 0x85 && c[9] == 0x53 &&
            c[10]== 0x00 && c[11]== 0xAA && c[12]== 0x00 &&
            c[13]== 0xA1 && c[14]== 0xF9 && c[15]== 0x5B) {
            is.reset();
            return true;
        }

        // 非 Intel 字节顺序
        else if (c[3] == 0x00 && c[1] == 0x61 && c[0] == 0x56 &&
            c[5] == 0x54 && c[4] == 0xC1 && c[7] == 0xCE &&
            c[6] == 0x11 && c[8] == 0x85 && c[9] == 0x53 &&
            c[10]== 0x00 && c[11]== 0xAA && c[12]== 0x00 &&
            c[13]== 0xA1 && c[14]== 0xF9 && c[15]== 0x5B) {
            is.reset();
            return true;
        }
        is.reset();
        return false;
    }

    /**
     * 尝试从流中读取指定数量的字节
     * 如果在读取 len 个字节之前到达 EOF，则返回 -1，否则返回 0
     */
    static private int readBytes(int c[], int len, InputStream is)
                throws IOException {

        byte buf[] = new byte[len];
        if (is.read(buf, 0, len) < len) {
            return -1;
        }

        // 填充传入的 int 数组
        for (int i = 0; i < len; i++) {
             c[i] = buf[i] & 0xff;
        }
        return 0;
    }


    /**
     * 从流中跳过指定数量的字节，直到到达 EOF 或跳过了指定数量的字节
     */
    static private long skipForward(InputStream is, long toSkip)
                throws IOException {

        long eachSkip = 0;
        long skipped = 0;

        while (skipped != toSkip) {
            eachSkip = is.skip(toSkip - skipped);

            // 检查是否到达 EOF
            if (eachSkip <= 0) {
                if (is.read() == -1) {
                    return skipped ;
                } else {
                    skipped++;
                }
            }
            skipped += eachSkip;
        }
        return skipped;
    }

}


class UnknownContentHandler extends ContentHandler {
    static final ContentHandler INSTANCE = new UnknownContentHandler();

    public Object getContent(URLConnection uc) throws IOException {
        return uc.getInputStream();
    }
}
