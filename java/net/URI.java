
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
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.CharacterCodingException;
import java.text.Normalizer;
import sun.nio.cs.ThreadLocalCoders;

import java.lang.Character;             // for javadoc
import java.lang.NullPointerException;  // for javadoc


/**
 * 表示统一资源标识符 (URI) 引用。
 *
 * <p> 除了下面提到的一些小的偏差外，此类的实例表示由
 * <a href="http://www.ietf.org/rfc/rfc2396.txt"><i>RFC&nbsp;2396: 统一资源标识符 (URI): 通用语法</i></a> 定义的 URI 引用，由 <a
 * href="http://www.ietf.org/rfc/rfc2732.txt"><i>RFC&nbsp;2732: URL 中的字面 IPv6 地址格式</i></a> 修正。字面 IPv6 地址格式还支持 scope_ids。scope_ids 的语法和用法描述
 * <a href="Inet6Address.html#scoped">这里</a>。
 * 此类提供了从其组件或通过解析其字符串形式创建 URI 实例的构造函数，访问实例各个组件的方法，以及规范化、解析和相对化 URI 实例的方法。此类的实例是不可变的。
 *
 *
 * <h3> URI 语法和组件 </h3>
 *
 * 在最高级别上，字符串形式的 URI 引用（以下简称“URI”）具有以下语法
 *
 * <blockquote>
 * [<i>方案</i><b>{@code :}</b>]<i>方案特定部分</i>[<b>{@code #}</b><i>片段</i>]
 * </blockquote>
 *
 * 其中方括号 [...] 标记可选组件，字符 <b>{@code :}</b> 和 <b>{@code #}</b> 代表它们自己。
 *
 * <p> 一个 <i>绝对</i> URI 指定一个方案；不指定方案的 URI 被称为 <i>相对</i> URI。URI 还根据它们是 <i>不透明</i> 还是 <i>分层</i> 进行分类。
 *
 * <p> 一个 <i>不透明</i> URI 是一个绝对 URI，其方案特定部分不以斜杠字符 ({@code '/'}) 开头。不透明 URI 不受进一步解析。一些不透明 URI 的例子是：
 *
 * <blockquote><table cellpadding=0 cellspacing=0 summary="布局">
 * <tr><td>{@code mailto:java-net@java.sun.com}<td></tr>
 * <tr><td>{@code news:comp.lang.java}<td></tr>
 * <tr><td>{@code urn:isbn:096139210x}</td></tr>
 * </table></blockquote>
 *
 * <p> 一个 <i>分层</i> URI 是一个绝对 URI，其方案特定部分以斜杠字符开头，或者是一个相对 URI，即不指定方案的 URI。一些分层 URI 的例子是：
 *
 * <blockquote>
 * {@code http://java.sun.com/j2se/1.3/}<br>
 * {@code docs/guide/collections/designfaq.html#28}<br>
 * {@code ../../../demo/jfc/SwingSet2/src/SwingSet2.java}<br>
 * {@code file:///~/calendar}
 * </blockquote>
 *
 * <p> 分层 URI 进一步根据以下语法进行解析
 *
 * <blockquote>
 * [<i>方案</i><b>{@code :}</b>][<b>{@code //}</b><i>权限</i>][<i>路径</i>][<b>{@code ?}</b><i>查询</i>][<b>{@code #}</b><i>片段</i>]
 * </blockquote>
 *
 * 其中字符 <b>{@code :}</b>、<b>{@code /}</b>、
 * <b>{@code ?}</b> 和 <b>{@code #}</b> 代表它们自己。分层 URI 的方案特定部分由方案和片段组件之间的字符组成。
 *
 * <p> 分层 URI 的权限组件，如果指定，要么是 <i>基于服务器的</i>，要么是 <i>基于注册表的</i>。基于服务器的权限解析为熟悉的语法
 *
 * <blockquote>
 * [<i>用户信息</i><b>{@code @}</b>]<i>主机</i>[<b>{@code :}</b><i>端口</i>]
 * </blockquote>
 *
 * 其中字符 <b>{@code @}</b> 和 <b>{@code :}</b> 代表它们自己。几乎所有当前使用的 URI 方案都是基于服务器的。以这种方式解析的权限组件被认为是基于注册表的。
 *
 * <p> 分层 URI 的路径组件如果以斜杠字符 ({@code '/'}) 开头，则被认为是绝对的；否则是相对的。如果分层 URI 是绝对的或指定了权限，则其路径始终是绝对的。
 *
 * <p> 总的来说，URI 实例有以下九个组件：
 *
 * <blockquote><table summary="描述 URI 的组件：方案、方案特定部分、权限、用户信息、主机、端口、路径、查询、片段">
 * <tr><th><i>组件</i></th><th><i>类型</i></th></tr>
 * <tr><td>方案</td><td>{@code String}</td></tr>
 * <tr><td>方案特定部分&nbsp;&nbsp;&nbsp;&nbsp;</td><td>{@code String}</td></tr>
 * <tr><td>权限</td><td>{@code String}</td></tr>
 * <tr><td>用户信息</td><td>{@code String}</td></tr>
 * <tr><td>主机</td><td>{@code String}</td></tr>
 * <tr><td>端口</td><td>{@code int}</td></tr>
 * <tr><td>路径</td><td>{@code String}</td></tr>
 * <tr><td>查询</td><td>{@code String}</td></tr>
 * <tr><td>片段</td><td>{@code String}</td></tr>
 * </table></blockquote>
 *
 * 在给定实例中，任何特定组件要么是 <i>未定义的</i>，要么是 <i>定义的</i> 并具有特定值。未定义的字符串组件由 {@code null} 表示，而未定义的整数组件由 {@code -1} 表示。字符串组件可以定义为空字符串作为其值；这不等同于该组件未定义。
 *
 * <p> 在实例中是否定义了特定组件取决于所表示的 URI 类型。绝对 URI 有一个方案组件。不透明 URI 有一个方案、一个方案特定部分，可能还有一个片段，但没有其他组件。分层 URI 始终有一个路径（尽管它可能是空的）和一个方案特定部分（至少包含路径），并且可能有其他任何组件。如果权限组件存在并且是基于服务器的，则主机组件将被定义，用户信息和端口组件可能被定义。
 *
 *
 * <h4> URI 实例上的操作 </h4>
 *
 * 本类支持的关键操作是 <i>规范化</i>、<i>解析</i> 和 <i>相对化</i>。
 *
 * <p> <i>规范化</i> 是从分层 URI 的路径组件中移除不必要的 {@code "."} 和 {@code ".."} 段的过程。每个 {@code "."} 段都被简单地移除。只有在 {@code ".."} 段前面有一个非 {@code ".."} 段时，才会移除 {@code ".."} 段。规范化对不透明 URI 没有影响。
 *
 * <p> <i>解析</i> 是将一个 URI 解析为另一个，<i>基准</i> URI 的过程。结果 URI 是根据 RFC&nbsp;2396 指定的方式从两个 URI 的组件构造的，从基准 URI 中获取未在原始 URI 中指定的组件。对于分层 URI，原始路径解析为基准路径，然后进行规范化。例如，解析
 *
 * <blockquote>
 * {@code docs/guide/collections/designfaq.html#28}
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;(1)
 * </blockquote>
 *
 * 以基准 URI {@code http://java.sun.com/j2se/1.3/} 为基准的结果 URI 是
 *
 * <blockquote>
 * {@code https://docs.oracle.com/javase/1.3/docs/guide/collections/designfaq.html#28}
 * </blockquote>
 *
 * 以这个结果为基准解析相对 URI
 *
 * <blockquote>
 * {@code ../../../demo/jfc/SwingSet2/src/SwingSet2.java}&nbsp;&nbsp;&nbsp;&nbsp;(2)
 * </blockquote>
 *
 * 进而得到
 *
 * <blockquote>
 * {@code http://java.sun.com/j2se/1.3/demo/jfc/SwingSet2/src/SwingSet2.java}
 * </blockquote>
 *
 * 支持解析绝对和相对 URI，以及分层 URI 的绝对和相对路径。解析绝对 URI {@code file:///~calendar} 与任何其他 URI 都会得到原始 URI，因为它已经是绝对的。以相对基准 URI (1) 解析相对 URI (2) 会得到规范化但仍为相对的 URI
 *
 * <blockquote>
 * {@code demo/jfc/SwingSet2/src/SwingSet2.java}
 * </blockquote>
 *
 * <p> <i>相对化</i>，最后，是解析的逆过程：对于任何两个规范化 URI <i>u</i> 和&nbsp;<i>v</i>，
 *
 * <blockquote>
 *   <i>u</i>{@code .relativize(}<i>u</i>{@code .resolve(}<i>v</i>{@code )).equals(}<i>v</i>{@code )}&nbsp;&nbsp;和<br>
 *   <i>u</i>{@code .resolve(}<i>u</i>{@code .relativize(}<i>v</i>{@code )).equals(}<i>v</i>{@code )}&nbsp;&nbsp;.<br>
 * </blockquote>
 *
 * 当构造包含必须相对于文档基准 URI 尽可能相对的 URI 的文档时，此操作通常很有用。例如，以基准 URI
 *
 * <blockquote>
 * {@code http://java.sun.com/j2se/1.3}
 * </blockquote>
 *
 * 相对化 URI
 *
 * <blockquote>
 * {@code https://docs.oracle.com/javase/1.3/docs/guide/index.html}
 * </blockquote>
 *
 * 会得到相对 URI {@code docs/guide/index.html}。
 *
 *
 * <h4> 字符类别 </h4>
 *
 * RFC&nbsp;2396 精确地指定了允许在 URI 引用各个组件中出现的字符。以下类别，大多数取自该规范，用于描述这些约束：
 *
 * <blockquote><table cellspacing=2 summary="描述类别 alpha、digit、alphanum、unreserved、punct、reserved、escaped 和 other">
 *   <tr><th valign=top><i>alpha</i></th>
 *       <td>US-ASCII 字母字符，
 *        {@code 'A'}&nbsp;到&nbsp;{@code 'Z'}
 *        和 {@code 'a'}&nbsp;到&nbsp;{@code 'z'}</td></tr>
 *   <tr><th valign=top><i>digit</i></th>
 *       <td>US-ASCII 十进制数字字符，
 *       {@code '0'}&nbsp;到&nbsp;{@code '9'}</td></tr>
 *   <tr><th valign=top><i>alphanum</i></th>
 *       <td>所有 <i>alpha</i> 和 <i>digit</i> 字符</td></tr>
 *   <tr><th valign=top><i>unreserved</i>&nbsp;&nbsp;&nbsp;&nbsp;</th>
 *       <td>所有 <i>alphanum</i> 字符加上字符串
 *        {@code "_-!.~'()*"} 中的字符</td></tr>
 *   <tr><th valign=top><i>punct</i></th>
 *       <td>字符串 {@code ",;:$&+="} 中的字符</td></tr>
 *   <tr><th valign=top><i>reserved</i></th>
 *       <td>所有 <i>punct</i> 字符加上字符串
 *        {@code "?/[]@"} 中的字符</td></tr>
 *   <tr><th valign=top><i>escaped</i></th>
 *       <td>转义的八位组，即由百分号
 *           ({@code '%'}) 后跟两个十六进制数字
 *           ({@code '0'}-{@code '9'}，{@code 'A'}-{@code 'F'}，和
 *           {@code 'a'}-{@code 'f'}) 组成的三元组</td></tr>
 *   <tr><th valign=top><i>other</i></th>
 *       <td>Unicode 字符中不属于 US-ASCII 字符集的字符，
 *           不是控制字符（根据 {@link
 *           java.lang.Character#isISOControl(char) Character.isISOControl}
 *           方法），也不是空格字符（根据 {@link
 *           java.lang.Character#isSpaceChar(char) Character.isSpaceChar}
 *           方法）&nbsp;&nbsp;<i>(<b>与 RFC 2396 的偏差</b>，后者仅限于 US-ASCII)</i></td></tr>
 * </table></blockquote>
 *
 * <p><a name="legal-chars"></a> 所有合法 URI 字符的集合由 <i>unreserved</i>、<i>reserved</i>、<i>escaped</i> 和 <i>other</i> 字符组成。
 *
 *
 * <h4> 转义的八位组、引用、编码和解码 </h4>
 *
 * RFC 2396 允许在用户信息、路径、查询和片段组件中出现转义的八位组。转义在 URI 中有两个目的：
 *
 * <ul>
 *
 *   <li><p> 为了 <i>编码</i> 非 US-ASCII 字符，当 URI 需要严格符合 RFC&nbsp;2396 而不包含任何 <i>other</i> 字符时。 </p></li>
 *
 *   <li><p> 为了 <i>引用</i> 否则在组件中非法的字符。用户信息、路径、查询和片段组件在哪些字符被认为是合法和非法方面略有不同。 </p></li>
 *
 * </ul>
 *
 * 这些目的在本类中由三个相关操作实现：
 *
 * <ul>
 *
 *   <li><p><a name="encode"></a> 通过将字符替换为 UTF-8 字符集中表示该字符的转义八位组序列来 <i>编码</i> 字符。例如，欧元符号 ({@code '\u005Cu20AC'}) 被编码为 {@code "%E2%82%AC"}。 <i>(<b>与 RFC&nbsp;2396 的偏差</b>，后者未指定任何特定字符集。)</i> </p></li>
 *
 *   <li><p><a name="quote"></a> 通过编码非法字符来 <i>引用</i> 它。例如，空格字符被替换为 {@code "%20"}。UTF-8 包含 US-ASCII，因此对于 US-ASCII 字符，此转换完全符合 RFC&nbsp;2396 的要求。 </p></li>
 *
 *   <li><p><a name="decode"></a>
 *   通过将转义八位组序列替换为 UTF-8 字符集中表示的字符序列来 <i>解码</i> 转义八位组。UTF-8 包含 US-ASCII，因此解码不仅会取消引用任何引用的 US-ASCII 字符，还会解码任何编码的非 US-ASCII 字符。如果在解码转义八位组时发生 <a
 *   href="../nio/charset/CharsetDecoder.html#ce">解码错误</a>，则错误的八位组将被替换为 {@code '\u005CuFFFD'}，即 Unicode 替换字符。 </p></li>
 *
 * </ul>
 *
 * 这些操作在本类的构造函数和方法中如下暴露：
 *
 * <ul>
 *
 *   <li><p> {@linkplain #URI(java.lang.String) 单参数构造函数} 要求其参数中的任何非法字符被引用，并保留任何出现的转义八位组和 <i>other</i> 字符。 </p></li>
 *
 *   <li><p> {@linkplain
 *   #URI(java.lang.String,java.lang.String,java.lang.String,int,java.lang.String,java.lang.String,java.lang.String)
 *   多参数构造函数} 根据它们出现的组件引用非法字符。这些构造函数始终引用百分号
 *   ({@code '%'})。任何 <i>other</i> 字符都被保留。 </p></li>
 *
 *   <li><p> {@link #getRawUserInfo() getRawUserInfo}、{@link #getRawPath()
 *   getRawPath}、{@link #getRawQuery() getRawQuery}、{@link #getRawFragment()
 *   getRawFragment}、{@link #getRawAuthority() getRawAuthority} 和 {@link
 *   #getRawSchemeSpecificPart() getRawSchemeSpecificPart} 方法以原始形式返回其相应组件的值，不解释任何转义八位组。这些方法返回的字符串可能包含转义八位组和 <i>other</i> 字符，但不会包含任何非法字符。 </p></li>
 *
 *   <li><p> {@link #getUserInfo() getUserInfo}、{@link #getPath()
 *   getPath}、{@link #getQuery() getQuery}、{@link #getFragment()
 *   getFragment}、{@link #getAuthority() getAuthority} 和 {@link
 *   #getSchemeSpecificPart() getSchemeSpecificPart} 方法解码其相应组件中的任何转义八位组。这些方法返回的字符串可能包含 <i>other</i> 字符和非法字符，但不会包含任何转义八位组。 </p></li>
 *
 *   <li><p> {@link #toString() toString} 方法返回一个 URI 字符串，其中包含所有必要的引用，但可能包含 <i>other</i> 字符。 </p></li>
 *
 *   <li><p> {@link #toASCIIString() toASCIIString} 方法返回一个完全引用和编码的 URI 字符串，不包含任何 <i>other</i> 字符。 </p></li>
 *
 * </ul>
 *
 *
 * <h4> 恒等式 </h4>
 *
 * 对于任何 URI <i>u</i>，总是有
 *
 * <blockquote>
 * {@code new URI(}<i>u</i>{@code .toString()).equals(}<i>u</i>{@code )}&nbsp;.
 * </blockquote>
 *
 * 对于任何不包含冗余语法（如在空权限前有两个斜杠（如 {@code file:///tmp/}）或在主机名后跟一个冒号但没有端口（如
 * {@code http://java.sun.com:}））且不编码字符（除非必须引用）的 URI <i>u</i>，以下恒等式也成立：
 * <pre>
 *     new URI(<i>u</i>.getScheme(),
 *             <i>u</i>.getSchemeSpecificPart(),
 *             <i>u</i>.getFragment())
 *     .equals(<i>u</i>)</pre>
 * 在所有情况下，
 * <pre>
 *     new URI(<i>u</i>.getScheme(),
 *             <i>u</i>.getUserInfo(), <i>u</i>.getAuthority(),
 *             <i>u</i>.getPath(), <i>u</i>.getQuery(),
 *             <i>u</i>.getFragment())
 *     .equals(<i>u</i>)</pre>
 * 如果 <i>u</i> 是分层的，以及
 * <pre>
 *     new URI(<i>u</i>.getScheme(),
 *             <i>u</i>.getUserInfo(), <i>u</i>.getHost(), <i>u</i>.getPort(),
 *             <i>u</i>.getPath(), <i>u</i>.getQuery(),
 *             <i>u</i>.getFragment())
 *     .equals(<i>u</i>)</pre>
 * 如果 <i>u</i> 是分层的并且没有权限或有一个基于服务器的权限。
 *
 *
 * <h4> URI、URL 和 URN </h4>
 *
 * URI 是统一资源 <i>标识符</i>，而 URL 是统一资源 <i>定位符</i>。因此，从抽象意义上讲，每个 URL 都是一个 URI，但不是每个 URI 都是一个 URL。这是因为 URI 另有一个子类别，即统一资源 <i>名称</i>（URN），它们命名资源但不指定如何定位它们。上面显示的 {@code mailto}、{@code news} 和
 * {@code isbn} URI 就是 URN 的例子。
 *
 * <p> URI 和 URL 之间的概念区别反映在本类和 {@link URL} 类之间的差异中。
 *
 * <p> 本类的实例表示由 RFC&nbsp;2396 定义的 URI 引用的语法意义。URI 可以是绝对的也可以是相对的。URI 字符串根据通用语法进行解析，而不考虑其指定的方案（如果有）。不执行任何主机（如果有）的查找，也不为 URI 构造任何依赖于方案的流处理器。相等性、哈希和比较严格地根据实例的字符内容定义。换句话说，URI 实例基本上是一个支持比较、规范化、解析和相对化等语法、与方案无关的操作的结构化字符串。
 *
 * <p> 相比之下，{@link URL} 类的实例表示 URL 的语法组件以及访问其描述的资源所需的一些信息。URL 必须是绝对的，即必须始终指定一个方案。URL 字符串根据其方案进行解析。总是为 URL 建立一个流处理器，事实上，不可能为没有可用处理器的方案创建 URL 实例。相等性和哈希取决于方案和主机（如果有）的 Internet 地址；比较未定义。换句话说，URL 是一个支持解析语法操作以及查找主机和打开到指定资源的连接的网络 I/O 操作的结构化字符串。
 *
 *
 * @author Mark Reinhold
 * @since 1.4
 *
 * @see <a href="http://www.ietf.org/rfc/rfc2279.txt"><i>RFC&nbsp;2279: UTF-8, a
 * transformation format of ISO 10646</i></a>, <br><a
 * href="http://www.ietf.org/rfc/rfc2373.txt"><i>RFC&nbsp;2373: IPv6 Addressing
 * Architecture</i></a>, <br><a
 * href="http://www.ietf.org/rfc/rfc2396.txt"><i>RFC&nbsp;2396: Uniform
 * Resource Identifiers (URI): Generic Syntax</i></a>, <br><a
 * href="http://www.ietf.org/rfc/rfc2732.txt"><i>RFC&nbsp;2732: Format for
 * Literal IPv6 Addresses in URLs</i></a>, <br><a
 * href="URISyntaxException.html">URISyntaxException</a>
 */


            public final class URI
    implements Comparable<URI>, Serializable
{

    // 注意：包含单词 "ASSERT" 的注释表示在启用断言的构建中，应将抛出 InternalError 的地方替换为适当的断言语句。

    static final long serialVersionUID = -6052424284110960213L;


    // -- 该实例的属性和组件 --

    // 所有 URI 的组件：[<scheme>:]<scheme-specific-part>[#<fragment>]
    private transient String scheme;            // null 表示相对 URI
    private transient String fragment;

    // 分层 URI 的组件：[//<authority>]<path>[?<query>]
    private transient String authority;         // 注册或服务器

    // 基于服务器的权限：[<userInfo>@]<host>[:<port>]
    private transient String userInfo;
    private transient String host;              // null 表示基于注册
    private transient int port = -1;            // -1 表示未定义

    // 剩余的分层 URI 组件
    private transient String path;              // null 表示不透明
    private transient String query;

    // 以下字段可能按需计算

    private volatile transient String schemeSpecificPart;
    private volatile transient int hash;        // 0 表示未定义

    private volatile transient String decodedUserInfo = null;
    private volatile transient String decodedAuthority = null;
    private volatile transient String decodedPath = null;
    private volatile transient String decodedQuery = null;
    private volatile transient String decodedFragment = null;
    private volatile transient String decodedSchemeSpecificPart = null;

    /**
     * 该 URI 的字符串形式。
     *
     * @serial
     */
    private volatile String string;             // 唯一可序列化的字段



    // -- 构造函数和工厂方法 --

    private URI() { }                           // 用于内部

    /**
     * 通过解析给定字符串构造 URI。
     *
     * <p> 该构造函数严格按 <a
     * href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a> 附录 A 中指定的语法解析给定字符串，
     * 但有以下偏差： </p>
     *
     * <ul>
     *
     *   <li><p> 如果空的权限组件后面跟着非空的路径、查询组件或片段组件，则允许空的权限组件。这允许解析如
     *   {@code "file:///foo/bar"} 这样的 URI，这似乎是 RFC 2396 的意图，尽管语法不允许。如果权限组件为空，则用户信息、主机和端口组件未定义。 </p></li>
     *
     *   <li><p> 允许空的相对路径；这似乎是 RFC 2396 的意图，尽管语法不允许。此偏差的主要后果是，如 {@code "#foo"} 这样的独立片段将解析为具有空路径和给定片段的相对 URI，并可以针对基础 URI <a
     *   href="#resolve-frag">解析</a>。 </p></li>
     *
     *   <li><p> 严格解析主机组件中的 IPv4 地址，如 <a
     *   href="http://www.ietf.org/rfc/rfc2732.txt">RFC 2732</a> 所指定：点分四段地址的每个元素必须包含不超过三个十进制数字。每个元素进一步限制为值不大于 255。 </p></li>
     *
     *   <li> <p> 如果主机组件仅由一个域名标签组成，则允许其以 <i>alphanum</i>
     *   字符开头。这似乎是 <a
     *   href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>
     *   第 3.2.2 节的意图，尽管语法不允许。此偏差的后果是，如 {@code s://123} 这样的分层 URI 的权限组件将解析为基于服务器的权限。 </p></li>
     *
     *   <li><p> 允许主机组件中的 IPv6 地址。IPv6 地址必须用方括号 ({@code '['} 和
     *   {@code ']'}) 包围，如 <a
     *   href="http://www.ietf.org/rfc/rfc2732.txt">RFC 2732</a> 所指定。IPv6 地址本身必须根据 <a
     *   href="http://www.ietf.org/rfc/rfc2373.txt">RFC 2373</a> 解析。IPv6 地址进一步限制为描述不超过十六字节的地址信息，这是 RFC 2373 中隐含的约束，但语法中未表达。 </p></li>
     *
     *   <li><p> 允许在 RFC 2396 允许 <i>escaped</i> 八位字节的任何地方使用 <i>other</i> 类别的字符，即在
     *   用户信息、路径、查询和片段组件中，以及在权限组件为基于注册时。这允许 URI 包含超出 US-ASCII 字符集的 Unicode 字符。 </p></li>
     *
     * </ul>
     *
     * @param  str   要解析为 URI 的字符串
     *
     * @throws  NullPointerException
     *          如果 {@code str} 为 {@code null}
     *
     * @throws  URISyntaxException
     *          如果给定字符串违反 RFC 2396，包括上述偏差
     */
    public URI(String str) throws URISyntaxException {
        new Parser(str).parse(false);
    }

    /**
     * 从给定组件构造分层 URI。
     *
     * <p> 如果提供了方案，则如果也提供了路径，则路径必须为空或以斜杠字符 ({@code '/'}) 开头。否则，可以通过传递 {@code null}
     * 为相应的参数或在 {@code port} 参数的情况下传递 {@code -1} 来使新 URI 的组件未定义。
     *
     * <p> 该构造函数首先根据 <a
     * href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a> 第 5.2 节第 7 步中指定的规则从给定组件构建 URI 字符串： </p>
     *
     * <ol>
     *
     *   <li><p> 最初，结果字符串为空。 </p></li>
     *
     *   <li><p> 如果提供了方案，则将其附加到结果后，再附加一个冒号字符 ({@code ':'})。 </p></li>
     *
     *   <li><p> 如果提供了用户信息、主机或端口，则附加字符串 {@code "//"}。 </p></li>
     *
     *   <li><p> 如果提供了用户信息，则将其附加，后跟一个商业符号字符 ({@code '@'})。任何不属于 <i>unreserved</i>、<i>punct</i>、<i>escaped</i> 或 <i>other</i>
     *   类别的字符将被 <a href="#quote">引用</a>。 </p></li>
     *
     *   <li><p> 如果提供了主机，则将其附加。如果主机是文字 IPv6 地址但未用方括号 ({@code '['} 和 {@code ']'}) 包围，则添加方括号。
     *   </p></li>
     *
     *   <li><p> 如果提供了端口号，则附加一个冒号字符 ({@code ':'})，后跟十进制的端口号。
     *   </p></li>
     *
     *   <li><p> 如果提供了路径，则将其附加。任何不属于 <i>unreserved</i>、<i>punct</i>、<i>escaped</i> 或 <i>other</i>
     *   类别且不等于斜杠字符 ({@code '/'}) 或商业符号字符 ({@code '@'}) 的字符将被引用。 </p></li>
     *
     *   <li><p> 如果提供了查询，则附加一个问号字符 ({@code '?'})，后跟查询。任何不是 <a href="#legal-chars">合法 URI 字符</a> 的字符将被引用。
     *   </p></li>
     *
     *   <li><p> 最后，如果提供了片段，则附加一个井号字符 ({@code '#'})，后跟片段。任何不是合法 URI 字符的字符将被引用。 </p></li>
     *
     * </ol>
     *
     * <p> 然后，将生成的 URI 字符串解析为调用 {@link
     * #URI(String)} 构造函数并调用结果的 {@link
     * #parseServerAuthority()} 方法；这可能会抛出 {@link
     * URISyntaxException}。 </p>
     *
     * @param   scheme    方案名称
     * @param   userInfo  用户名和授权信息
     * @param   host      主机名
     * @param   port      端口号
     * @param   path      路径
     * @param   query     查询
     * @param   fragment  片段
     *
     * @throws URISyntaxException
     *         如果同时提供了方案和路径但路径是相对的，如果从给定组件构建的 URI 字符串违反 RFC 2396，或者如果字符串的权限组件存在但不能解析为基于服务器的权限
     */
    public URI(String scheme,
               String userInfo, String host, int port,
               String path, String query, String fragment)
        throws URISyntaxException
    {
        String s = toString(scheme, null,
                            null, userInfo, host, port,
                            path, query, fragment);
        checkPath(s, scheme, path);
        new Parser(s).parse(true);
    }

    /**
     * 从给定组件构造分层 URI。
     *
     * <p> 如果提供了方案，则如果也提供了路径，则路径必须为空或以斜杠字符 ({@code '/'}) 开头。否则，可以通过传递 {@code null}
     * 为相应的参数来使新 URI 的组件未定义。
     *
     * <p> 该构造函数首先根据 <a
     * href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a> 第 5.2 节第 7 步中指定的规则从给定组件构建 URI 字符串： </p>
     *
     * <ol>
     *
     *   <li><p> 最初，结果字符串为空。 </p></li>
     *
     *   <li><p> 如果提供了方案，则将其附加到结果后，再附加一个冒号字符 ({@code ':'})。 </p></li>
     *
     *   <li><p> 如果提供了权限，则附加字符串 {@code "//"}，后跟权限。如果权限包含文字 IPv6 地址，则该地址必须用方括号 ({@code '['} 和 {@code ']'})
     *   包围。任何不属于 <i>unreserved</i>、<i>punct</i>、<i>escaped</i> 或 <i>other</i>
     *   类别且不等于商业符号字符 ({@code '@'}) 的字符将被 <a href="#quote">引用</a>。 </p></li>
     *
     *   <li><p> 如果提供了路径，则将其附加。任何不属于 <i>unreserved</i>、<i>punct</i>、<i>escaped</i> 或 <i>other</i>
     *   类别且不等于斜杠字符 ({@code '/'}) 或商业符号字符 ({@code '@'}) 的字符将被引用。 </p></li>
     *
     *   <li><p> 如果提供了查询，则附加一个问号字符 ({@code '?'})，后跟查询。任何不是 <a href="#legal-chars">合法 URI 字符</a> 的字符将被引用。
     *   </p></li>
     *
     *   <li><p> 最后，如果提供了片段，则附加一个井号字符 ({@code '#'})，后跟片段。任何不是合法 URI 字符的字符将被引用。 </p></li>
     *
     * </ol>
     *
     * <p> 然后，将生成的 URI 字符串解析为调用 {@link
     * #URI(String)} 构造函数并调用结果的 {@link
     * #parseServerAuthority()} 方法；这可能会抛出 {@link
     * URISyntaxException}。 </p>
     *
     * @param   scheme     方案名称
     * @param   authority  权限
     * @param   path       路径
     * @param   query      查询
     * @param   fragment   片段
     *
     * @throws URISyntaxException
     *         如果同时提供了方案和路径但路径是相对的，如果从给定组件构建的 URI 字符串违反 RFC 2396，或者如果字符串的权限组件存在但不能解析为基于服务器的权限
     */
    public URI(String scheme,
               String authority,
               String path, String query, String fragment)
        throws URISyntaxException
    {
        String s = toString(scheme, null,
                            authority, null, null, -1,
                            path, query, fragment);
        checkPath(s, scheme, path);
        new Parser(s).parse(false);
    }

    /**
     * 从给定组件构造分层 URI。
     *
     * <p> 可以通过传递 {@code null} 使组件未定义。
     *
     * <p> 该便捷构造函数的工作方式如下，就像调用了七参数构造函数：
     *
     * <blockquote>
     * {@code new} {@link #URI(String, String, String, int, String, String, String)
     * URI}{@code (scheme, null, host, -1, path, null, fragment);}
     * </blockquote>
     *
     * @param   scheme    方案名称
     * @param   host      主机名
     * @param   path      路径
     * @param   fragment  片段
     *
     * @throws  URISyntaxException
     *          如果从给定组件构建的 URI 字符串违反 RFC 2396
     */
    public URI(String scheme, String host, String path, String fragment)
        throws URISyntaxException
    {
        this(scheme, null, host, -1, path, null, fragment);
    }

    /**
     * 从给定组件构造 URI。
     *
     * <p> 可以通过传递 {@code null} 使组件未定义。
     *
     * <p> 该构造函数首先使用给定组件构建 URI 的字符串形式，如下所示： </p>
     *
     * <ol>
     *
     *   <li><p> 最初，结果字符串为空。 </p></li>
     *
     *   <li><p> 如果提供了方案，则将其附加到结果后，再附加一个冒号字符 ({@code ':'})。 </p></li>
     *
     *   <li><p> 如果提供了方案特定部分，则将其附加。任何不是 <a href="#legal-chars">合法 URI 字符</a> 的字符将被 <a href="#quote">引用</a>。 </p></li>
     *
     *   <li><p> 最后，如果提供了片段，则将井号字符 ({@code '#'}) 附加到字符串，后跟片段。任何不是合法 URI 字符的字符将被引用。 </p></li>
     *
     * </ol>
     *
     * <p> 然后，将生成的 URI 字符串解析为调用 {@link #URI(String)} 构造函数以创建新的 URI 实例；
     * 这可能会抛出 {@link URISyntaxException}。 </p>
     *
     * @param   scheme    方案名称
     * @param   ssp       方案特定部分
     * @param   fragment  片段
     *
     * @throws  URISyntaxException
     *          如果从给定组件构建的 URI 字符串违反 RFC 2396
     */
    public URI(String scheme, String ssp, String fragment)
        throws URISyntaxException
    {
        new Parser(toString(scheme, ssp,
                            null, null, null, -1,
                            null, null, fragment))
            .parse(false);
    }


                /**
     * 通过解析给定的字符串创建一个URI。
     *
     * <p> 这个便利的工厂方法的作用类似于调用 {@link
     * #URI(String)} 构造函数；构造函数抛出的任何 {@link URISyntaxException} 都被捕获并包装在一个新的 {@link
     * IllegalArgumentException} 对象中，然后抛出。
     *
     * <p> 提供此方法是为了在已知给定字符串是合法URI的情况下使用，例如在程序中声明的URI常量，因此，如果字符串无法解析为URI，则会被认为是编程错误。
     * 构造函数，它们直接抛出 {@link URISyntaxException}，应该在从用户输入或其他可能出错的来源构建URI时使用。 </p>
     *
     * @param  str   要解析成URI的字符串
     * @return 新的URI
     *
     * @throws  NullPointerException
     *          如果 {@code str} 是 {@code null}
     *
     * @throws  IllegalArgumentException
     *          如果给定的字符串违反了RFC&nbsp;2396
     */
    public static URI create(String str) {
        try {
            return new URI(str);
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException(x.getMessage(), x);
        }
    }


    // -- 操作 --

    /**
     * 尝试解析此URI的权威组件（如果已定义），将其解析为用户信息、主机和端口组件。
     *
     * <p> 如果此URI的权威组件已经被识别为基于服务器的，则它将已经被解析为用户信息、主机和端口组件。在这种情况下，或者如果此URI没有权威组件，此方法将简单地返回此URI。
     *
     * <p> 否则，此方法将再次尝试将权威组件解析为用户信息、主机和端口组件，并抛出一个描述为什么权威组件无法以这种方式解析的异常。
     *
     * <p> 提供此方法是因为在 <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC&nbsp;2396</a>
     * 中指定的通用URI语法无法总是区分格式错误的基于服务器的权威和合法的基于注册表的权威。因此，它必须将某些前者的实例视为后者的实例。例如，URI字符串 {@code "//foo:bar"} 的权威组件不是一个合法的基于服务器的权威，但它作为基于注册表的权威是合法的。
     *
     * <p> 在许多常见情况下，例如在处理已知为URN或URL的URI时，正在使用的分层URI将始终是基于服务器的。因此，它们必须被解析为基于服务器的，或者被视为错误。在这种情况下，可以使用如下语句
     *
     * <blockquote>
     * {@code URI }<i>u</i>{@code  = new URI(str).parseServerAuthority();}
     * </blockquote>
     *
     * <p> 以确保 <i>u</i> 始终引用一个URI，如果它有权威组件，则该权威组件是一个具有适当的用户信息、主机和端口组件的基于服务器的权威。调用此方法还确保如果权威组件无法以这种方式解析，则可以基于抛出的异常发出适当的诊断消息。 </p>
     *
     * @return  其权威字段已解析为基于服务器的权威的URI
     *
     * @throws  URISyntaxException
     *          如果此URI的权威组件已定义，但无法根据RFC&nbsp;2396解析为基于服务器的权威
     */
    public URI parseServerAuthority()
        throws URISyntaxException
    {
        // 我们可以更聪明一些，缓存原始解析期间抛出的异常的消息和索引，但这需要更多的字段或更复杂的表示。
        if ((host != null) || (authority == null))
            return this;
        defineString();
        new Parser(string).parse(true);
        return this;
    }

    /**
     * 规范化此URI的路径。
     *
     * <p> 如果此URI是不透明的，或者其路径已经处于规范形式，则返回此URI。否则，构造一个新的URI，该URI与此URI相同，只是其路径是通过规范化此URI的路径来计算的，方式与 <a
     * href="http://www.ietf.org/rfc/rfc2396.txt">RFC&nbsp;2396</a>，
     * 第5.2节，步骤6，子步骤c到f一致；即：
     * </p>
     *
     * <ol>
     *
     *   <li><p> 所有 {@code "."} 段都被移除。 </p></li>
     *
     *   <li><p> 如果一个 {@code ".."} 段前面有一个非 {@code ".."} 段，则这两个段都被移除。此步骤重复执行，直到不再适用。 </p></li>
     *
     *   <li><p> 如果路径是相对的，并且其第一个段包含冒号字符 ({@code ':'})，则在前面插入一个 {@code "."} 段。这防止了一个相对URI，其路径如
     *   {@code "a:b/c/d"}，稍后被重新解析为一个不透明的URI，其方案为 {@code "a"}，其方案特定部分为 {@code "b/c/d"}。
     *   <b><i>(与RFC&nbsp;2396的偏差)</i></b> </p></li>
     *
     * </ol>
     *
     * <p> 规范化的路径将以一个或多个 {@code ".."} 段开始，如果前面没有足够的非 {@code ".."} 段来移除它们。规范化的路径将以一个 {@code "."} 段开始，如果第3步插入了一个。否则，规范化的路径将不包含任何 {@code "."} 或 {@code ".."} 段。 </p>
     *
     * @return  与此URI等效的URI，
     *          但其路径处于规范形式
     */
    public URI normalize() {
        return normalize(this);
    }

    /**
     * 解析给定的URI与此URI。
     *
     * <p> 如果给定的URI已经是绝对的，或者此URI是不透明的，则返回给定的URI。
     *
     * <p><a name="resolve-frag"></a> 如果给定的URI的片段组件已定义，其路径组件为空，且其方案、权威和查询组件未定义，则返回一个具有给定片段但所有其他组件与此URI相同的URI。这允许一个表示独立片段引用的URI，如
     * {@code "#foo"}，可以有意义地解析为基准URI。
     *
     * <p> 否则，此方法以与 <a
     * href="http://www.ietf.org/rfc/rfc2396.txt">RFC&nbsp;2396</a>，
     * 第5.2节一致的方式构造一个新的分层URI；即： </p>
     *
     * <ol>
     *
     *   <li><p> 构造一个新的URI，其方案为此URI的方案，查询和片段组件为给定URI的查询和片段组件。 </p></li>
     *
     *   <li><p> 如果给定的URI有权威组件，则新URI的权威和路径组件取自给定的URI。 </p></li>
     *
     *   <li><p> 否则，新URI的权威组件从此URI复制，其路径组件按如下方式计算： </p>
     *
     *   <ol>
     *
     *     <li><p> 如果给定的URI的路径是绝对的，则新URI的路径取自给定的URI。 </p></li>
     *
     *     <li><p> 否则，给定的URI的路径是相对的，因此新URI的路径通过将此URI的路径（如果有）除最后一段外的所有段与给定URI的路径连接起来，然后像调用 {@link
     *     #normalize() normalize} 方法一样规范化结果来计算。 </p></li>
     *
     *   </ol></li>
     *
     * </ol>
     *
     * <p> 仅当此URI或给定的URI是绝对的时，此方法的结果才是绝对的。 </p>
     *
     * @param  uri  要解析的URI
     * @return 结果URI
     *
     * @throws  NullPointerException
     *          如果 {@code uri} 是 {@code null}
     */
    public URI resolve(URI uri) {
        return resolve(this, uri);
    }

    /**
     * 通过解析给定的字符串并将其解析与此URI来构造新的URI。
     *
     * <p> 调用此便利方法等同于评估表达式 {@link #resolve(java.net.URI)
     * resolve}{@code (URI.}{@link #create(String) create}{@code (str))}。 </p>
     *
     * @param  str   要解析成URI的字符串
     * @return 结果URI
     *
     * @throws  NullPointerException
     *          如果 {@code str} 是 {@code null}
     *
     * @throws  IllegalArgumentException
     *          如果给定的字符串违反了RFC&nbsp;2396
     */
    public URI resolve(String str) {
        return resolve(URI.create(str));
    }

    /**
     * 相对此URI相对化给定的URI。
     *
     * <p> 给定的URI与此URI的相对化按如下方式计算： </p>
     *
     * <ol>
     *
     *   <li><p> 如果此URI或给定的URI是不透明的，或者两个URI的方案和权威组件不相同，或者此URI的路径不是给定URI路径的前缀，则返回给定的URI。 </p></li>
     *
     *   <li><p> 否则，构造一个新的相对分层URI，其查询和片段组件取自给定的URI，其路径组件通过从给定URI路径的开头移除此URI的路径来计算。 </p></li>
     *
     * </ol>
     *
     * @param  uri  要相对化的URI
     * @return 结果URI
     *
     * @throws  NullPointerException
     *          如果 {@code uri} 是 {@code null}
     */
    public URI relativize(URI uri) {
        return relativize(this, uri);
    }

    /**
     * 从此URI构造URL。
     *
     * <p> 调用此便利方法等同于评估表达式 {@code new URL(this.toString())}，但在构造URL之前先检查此URI是否为绝对的。 </p>
     *
     * @return  从此URI构造的URL
     *
     * @throws  IllegalArgumentException
     *          如果此URL不是绝对的
     *
     * @throws  MalformedURLException
     *          如果找不到URL的协议处理器，或者在构造URL时发生其他错误
     */
    public URL toURL()
        throws MalformedURLException {
        if (!isAbsolute())
            throw new IllegalArgumentException("URI is not absolute");
        return new URL(toString());
    }

    // -- 组件访问方法 --

    /**
     * 返回此URI的方案组件。
     *
     * <p> 如果定义了URI的方案组件，则它只包含 <i>alphanum</i> 类别的字符和字符串 {@code "-.+"} 中的字符。方案始终以 <i>alpha</i> 字符开始。 <p>
     *
     * URI的方案组件不能包含转义的八位字节，因此此方法不执行任何解码。
     *
     * @return  此URI的方案组件，
     *          或者如果方案未定义则返回 {@code null}
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * 告知此URI是否为绝对的。
     *
     * <p> 如果且仅当URI具有方案组件时，该URI才是绝对的。 </p>
     *
     * @return  如果且仅当此URI为绝对时返回 {@code true}
     */
    public boolean isAbsolute() {
        return scheme != null;
    }

    /**
     * 告知此URI是否为不透明的。
     *
     * <p> 如果且仅当URI为绝对的且其方案特定部分不以斜杠字符 ('/') 开头时，该URI才是不透明的。
     * 不透明的URI具有方案、方案特定部分，可能还有片段；所有其他组件均未定义。 </p>
     *
     * @return  如果且仅当此URI为不透明时返回 {@code true}
     */
    public boolean isOpaque() {
        return path == null;
    }

    /**
     * 返回此URI的原始方案特定部分。方案特定部分永远不会未定义，尽管它可以为空。
     *
     * <p> URI的方案特定部分只包含合法的URI字符。 </p>
     *
     * @return  此URI的原始方案特定部分
     *          （从不为 {@code null}）
     */
    public String getRawSchemeSpecificPart() {
        defineSchemeSpecificPart();
        return schemeSpecificPart;
    }

    /**
     * 返回此URI的解码方案特定部分。
     *
     * <p> 由此方法返回的字符串与 {@link #getRawSchemeSpecificPart() getRawSchemeSpecificPart} 方法返回的字符串相同，只是所有转义的八位字节序列都被 <a
     * href="#decode">解码</a>。 </p>
     *
     * @return  此URI的解码方案特定部分
     *          （从不为 {@code null}）
     */
    public String getSchemeSpecificPart() {
        if (decodedSchemeSpecificPart == null)
            decodedSchemeSpecificPart = decode(getRawSchemeSpecificPart());
        return decodedSchemeSpecificPart;
    }

    /**
     * 返回此URI的原始权威组件。
     *
     * <p> 如果定义了URI的权威组件，则它只包含商业字符 ({@code '@'}) 和 <i>unreserved</i>、<i>punct</i>、<i>escaped</i> 和 <i>other</i>
     * 类别的字符。如果权威是基于服务器的，则它进一步受制于具有有效的用户信息、主机和端口组件。 </p>
     *
     * @return  此URI的原始权威组件，
     *          或者如果权威未定义则返回 {@code null}
     */
    public String getRawAuthority() {
        return authority;
    }

    /**
     * 返回此URI的解码权威组件。
     *
     * <p> 由此方法返回的字符串与 {@link #getRawAuthority() getRawAuthority} 方法返回的字符串相同，只是所有转义的八位字节序列都被 <a href="#decode">解码</a>。 </p>
     *
     * @return  此URI的解码权威组件，
     *          或者如果权威未定义则返回 {@code null}
     */
    public String getAuthority() {
        if (decodedAuthority == null)
            decodedAuthority = decode(authority);
        return decodedAuthority;
    }

    /**
     * 返回此URI的原始用户信息组件。
     *
     * <p> 如果定义了URI的用户信息组件，则它只包含 <i>unreserved</i>、<i>punct</i>、<i>escaped</i> 和 <i>other</i>
     * 类别的字符。 </p>
     *
     * @return  此URI的原始用户信息组件，
     *          或者如果用户信息未定义则返回 {@code null}
     */
    public String getRawUserInfo() {
        return userInfo;
    }


                /**
     * 返回此 URI 的解码用户信息组件。
     *
     * <p> 该方法返回的字符串与 {@link #getRawUserInfo() getRawUserInfo} 方法返回的字符串相同，除了所有
     * 转义的八位组序列都被 <a href="#decode">解码</a>。  </p>
     *
     * @return  此 URI 的解码用户信息组件，
     *          或者如果用户信息未定义则返回 {@code null}
     */
    public String getUserInfo() {
        if ((decodedUserInfo == null) && (userInfo != null))
            decodedUserInfo = decode(userInfo);
        return decodedUserInfo;
    }

    /**
     * 返回此 URI 的主机组件。
     *
     * <p> URI 的主机组件（如果已定义）将具有以下形式之一： </p>
     *
     * <ul>
     *
     *   <li><p> 由一个或多个 <i>标签</i> 组成的域名，标签之间用句点字符 ({@code '.'}) 分隔，可选地以句点字符结尾。
     *   每个标签由 <i>alphanum</i> 字符以及连字符字符 ({@code '-'}) 组成，但连字符不会出现在标签的开头或结尾。
     *   由两个或多个标签组成的域名的最右侧标签以 <i>alpha</i> 字符开头。 </li>
     *
     *   <li><p> 以点分十进制形式表示的 IPv4 地址，形式为
     *   <i>digit</i>{@code +.}<i>digit</i>{@code +.}<i>digit</i>{@code +.}<i>digit</i>{@code +}，
     *   其中没有 <i>digit</i> 序列长度超过三个字符，且没有序列的值大于 255。 </p></li>
     *
     *   <li><p> 用方括号 ({@code '['} 和 {@code ']'}) 包围的 IPv6 地址，由十六进制数字、冒号字符
     *   ({@code ':'}) 以及可能嵌入的 IPv4 地址组成。 IPv6 地址的完整语法在 <a
     *   href="http://www.ietf.org/rfc/rfc2373.txt"><i>RFC&nbsp;2373: IPv6
     *   地址架构</i></a> 中指定。  </p></li>
     *
     * </ul>
     *
     * URI 的主机组件不能包含转义的八位组，因此此方法不执行任何解码。
     *
     * @return  此 URI 的主机组件，
     *          或者如果主机未定义则返回 {@code null}
     */
    public String getHost() {
        return host;
    }

    /**
     * 返回此 URI 的端口号。
     *
     * <p> URI 的端口组件（如果已定义）是非负整数。 </p>
     *
     * @return  此 URI 的端口组件，
     *          或者如果端口未定义则返回 {@code -1}
     */
    public int getPort() {
        return port;
    }

    /**
     * 返回此 URI 的原始路径组件。
     *
     * <p> URI 的路径组件（如果已定义）仅包含斜杠字符 ({@code '/'}), 商业字符 ({@code '@'})，
     * 以及 <i>unreserved</i>, <i>punct</i>, <i>escaped</i>, 和 <i>other</i> 类别的字符。 </p>
     *
     * @return  此 URI 的路径组件，
     *          或者如果路径未定义则返回 {@code null}
     */
    public String getRawPath() {
        return path;
    }

    /**
     * 返回此 URI 的解码路径组件。
     *
     * <p> 该方法返回的字符串与 {@link #getRawPath() getRawPath} 方法返回的字符串相同，除了所有
     * 转义的八位组序列都被 <a href="#decode">解码</a>。  </p>
     *
     * @return  此 URI 的解码路径组件，
     *          或者如果路径未定义则返回 {@code null}
     */
    public String getPath() {
        if ((decodedPath == null) && (path != null))
            decodedPath = decode(path);
        return decodedPath;
    }

    /**
     * 返回此 URI 的原始查询组件。
     *
     * <p> URI 的查询组件（如果已定义）仅包含合法的 URI 字符。 </p>
     *
     * @return  此 URI 的原始查询组件，
     *          或者如果查询未定义则返回 {@code null}
     */
    public String getRawQuery() {
        return query;
    }

    /**
     * 返回此 URI 的解码查询组件。
     *
     * <p> 该方法返回的字符串与 {@link #getRawQuery() getRawQuery} 方法返回的字符串相同，除了所有
     * 转义的八位组序列都被 <a href="#decode">解码</a>。  </p>
     *
     * @return  此 URI 的解码查询组件，
     *          或者如果查询未定义则返回 {@code null}
     */
    public String getQuery() {
        if ((decodedQuery == null) && (query != null))
            decodedQuery = decode(query);
        return decodedQuery;
    }

    /**
     * 返回此 URI 的原始片段组件。
     *
     * <p> URI 的片段组件（如果已定义）仅包含合法的 URI 字符。 </p>
     *
     * @return  此 URI 的原始片段组件，
     *          或者如果片段未定义则返回 {@code null}
     */
    public String getRawFragment() {
        return fragment;
    }

    /**
     * 返回此 URI 的解码片段组件。
     *
     * <p> 该方法返回的字符串与 {@link #getRawFragment() getRawFragment} 方法返回的字符串相同，除了所有
     * 转义的八位组序列都被 <a href="#decode">解码</a>。  </p>
     *
     * @return  此 URI 的解码片段组件，
     *          或者如果片段未定义则返回 {@code null}
     */
    public String getFragment() {
        if ((decodedFragment == null) && (fragment != null))
            decodedFragment = decode(fragment);
        return decodedFragment;
    }


    // -- 等价性、比较、哈希码、toString 和序列化 --

    /**
     * 测试此 URI 是否与另一个对象相等。
     *
     * <p> 如果给定的对象不是 URI，则此方法立即返回 {@code false}。
     *
     * <p> 要认为两个 URI 相等，要求它们要么都是不透明的，要么都是分层的。它们的方案必须都未定义或相等，不区分大小写。
     * 它们的片段必须都未定义或相等。
     *
     * <p> 要认为两个不透明的 URI 相等，它们的方案特定部分必须相等。
     *
     * <p> 要认为两个分层的 URI 相等，它们的路径必须相等，它们的查询必须都未定义或相等。
     * 它们的权限必须都未定义，或都是基于注册表的，或都是基于服务器的。如果它们的权限已定义且基于注册表，则它们必须相等。
     * 如果它们的权限已定义且基于服务器，则它们的主机必须相等，不区分大小写，它们的端口号必须相等，它们的用户信息组件必须相等。
     *
     * <p> 在测试两个 URI 的用户信息、路径、查询、片段、权限或方案特定部分的等价性时，比较的是这些组件的原始形式而不是编码形式，
     * 且转义八位组的十六进制数字比较时不区分大小写。
     *
     * <p> 此方法满足 {@link java.lang.Object#equals(Object) Object.equals} 方法的一般约定。 </p>
     *
     * @param   ob   要与此对象进行比较的对象
     *
     * @return  如果且仅如果给定对象是与此 URI 相同的 URI，则返回 {@code true}
     */
    public boolean equals(Object ob) {
        if (ob == this)
            return true;
        if (!(ob instanceof URI))
            return false;
        URI that = (URI)ob;
        if (this.isOpaque() != that.isOpaque()) return false;
        if (!equalIgnoringCase(this.scheme, that.scheme)) return false;
        if (!equal(this.fragment, that.fragment)) return false;

        // 不透明
        if (this.isOpaque())
            return equal(this.schemeSpecificPart, that.schemeSpecificPart);

        // 分层
        if (!equal(this.path, that.path)) return false;
        if (!equal(this.query, that.query)) return false;

        // 权限
        if (this.authority == that.authority) return true;
        if (this.host != null) {
            // 基于服务器
            if (!equal(this.userInfo, that.userInfo)) return false;
            if (!equalIgnoringCase(this.host, that.host)) return false;
            if (this.port != that.port) return false;
        } else if (this.authority != null) {
            // 基于注册表
            if (!equal(this.authority, that.authority)) return false;
        } else if (this.authority != that.authority) {
            return false;
        }

        return true;
    }

    /**
     * 返回此 URI 的哈希码值。哈希码基于 URI 的所有组件，并满足
     * {@link java.lang.Object#hashCode() Object.hashCode} 方法的一般约定。
     *
     * @return  此 URI 的哈希码值
     */
    public int hashCode() {
        if (hash != 0)
            return hash;
        int h = hashIgnoringCase(0, scheme);
        h = hash(h, fragment);
        if (isOpaque()) {
            h = hash(h, schemeSpecificPart);
        } else {
            h = hash(h, path);
            h = hash(h, query);
            if (host != null) {
                h = hash(h, userInfo);
                h = hashIgnoringCase(h, host);
                h += 1949 * port;
            } else {
                h = hash(h, authority);
            }
        }
        hash = h;
        return h;
    }

    /**
     * 将此 URI 与另一个对象进行比较，该对象必须是 URI。
     *
     * <p> 在比较两个 URI 的相应组件时，如果一个组件未定义但另一个组件已定义，则第一个组件被认为小于第二个组件。
     * 除非另有说明，字符串组件的顺序根据其自然的、区分大小写的顺序定义，即 {@link java.lang.String#compareTo(Object)
     * String.compareTo} 方法。受编码影响的字符串组件通过比较其原始形式而不是编码形式来比较。
     *
     * <p> URI 的排序定义如下： </p>
     *
     * <ul>
     *
     *   <li><p> 具有不同方案的两个 URI 按照它们的方案的顺序进行排序，不区分大小写。 </p></li>
     *
     *   <li><p> 具有相同方案的分层 URI 被认为小于具有相同方案的不透明 URI。 </p></li>
     *
     *   <li><p> 具有相同方案的两个不透明 URI 按照它们的方案特定部分的顺序进行排序。 </p></li>
     *
     *   <li><p> 具有相同方案和方案特定部分的两个不透明 URI 按照它们的片段的顺序进行排序。 </p></li>
     *
     *   <li><p> 具有相同方案的两个分层 URI 按照它们的权限组件的顺序进行排序： </p>
     *
     *   <ul>
     *
     *     <li><p> 如果两个权限组件都是基于服务器的，则按照它们的用户信息组件的顺序进行排序；如果这些组件相同，
     *     则按照它们的主机的顺序进行排序，不区分大小写；如果主机相同，则按照它们的端口的顺序进行排序。 </p></li>
     *
     *     <li><p> 如果一个或两个权限组件是基于注册表的，则按照它们的权限组件的顺序进行排序。 </p></li>
     *
     *   </ul></li>
     *
     *   <li><p> 最后，具有相同方案和权限组件的两个分层 URI 按照它们的路径的顺序进行排序；如果路径相同，则按照它们的查询的顺序进行排序；
     *   如果查询相同，则按照它们的片段的顺序进行排序。 </p></li>
     *
     * </ul>
     *
     * <p> 此方法满足 {@link java.lang.Comparable#compareTo(Object) Comparable.compareTo}
     * 方法的一般约定。 </p>
     *
     * @param   that
     *          要与此 URI 进行比较的对象
     *
     * @return  一个负整数、零或正整数，表示此 URI 小于、等于或大于给定的 URI
     *
     * @throws  ClassCastException
     *          如果给定的对象不是 URI
     */
    public int compareTo(URI that) {
        int c;

        if ((c = compareIgnoringCase(this.scheme, that.scheme)) != 0)
            return c;

        if (this.isOpaque()) {
            if (that.isOpaque()) {
                // 两个都是不透明的
                if ((c = compare(this.schemeSpecificPart,
                                 that.schemeSpecificPart)) != 0)
                    return c;
                return compare(this.fragment, that.fragment);
            }
            return +1;                  // 不透明 > 分层
        } else if (that.isOpaque()) {
            return -1;                  // 分层 < 不透明
        }

        // 分层
        if ((this.host != null) && (that.host != null)) {
            // 两个都是基于服务器的
            if ((c = compare(this.userInfo, that.userInfo)) != 0)
                return c;
            if ((c = compareIgnoringCase(this.host, that.host)) != 0)
                return c;
            if ((c = this.port - that.port) != 0)
                return c;
        } else {
            // 如果一个或两个权限组件是基于注册表的，则我们简单地按照通常的、区分大小写的方式进行比较。
            // 如果一个是基于注册表的而另一个是基于服务器的，则字符串保证不相等，因此比较结果永远不会返回零，
            // 且 compareTo 和 equals 方法将保持一致。
            if ((c = compare(this.authority, that.authority)) != 0) return c;
        }

        if ((c = compare(this.path, that.path)) != 0) return c;
        if ((c = compare(this.query, that.query)) != 0) return c;
        return compare(this.fragment, that.fragment);
    }

    /**
     * 以字符串形式返回此 URI 的内容。
     *
     * <p> 如果此 URI 是通过调用此类中的一个构造函数创建的，则返回一个与原始输入字符串等效的字符串，或者根据最初给定的组件计算出的字符串。
     * 否则，此 URI 是通过规范化、解析或相对化创建的，因此根据 <a
     * href="http://www.ietf.org/rfc/rfc2396.txt">RFC&nbsp;2396</a>，
     * 第 5.2 节，步骤 7 的规则从此 URI 的组件构造字符串。 </p>
     *
     * @return  此 URI 的字符串形式
     */
    public String toString() {
        defineString();
        return string;
    }

    /**
     * 以 US-ASCII 字符串形式返回此 URI 的内容。
     *
     * <p> 如果此 URI 不包含任何 <i>other</i> 类别的字符，则调用此方法将返回与调用 {@link #toString() toString} 方法相同的值。
     * 否则，此方法的工作方式就像调用该方法并然后 <a
     * href="#encode">编码</a> 结果一样。  </p>
     *
     * @return  此 URI 的字符串形式，根据需要进行编码
     *          以便只包含 US-ASCII 字符集中的字符
     */
    public String toASCIIString() {
        defineString();
        return encode(string);
    }


    // -- Serialization support --

    /**
     * 将此 URI 的内容保存到给定的序列化流中。
     *
     * <p> URI 实例中唯一可序列化的字段是其 {@code string} 字段。如果该字段还没有值，则会为其赋值，
     * 然后调用给定对象输出流的 {@link java.io.ObjectOutputStream#defaultWriteObject()}
     * 方法。 </p>
     *
     * @param  os  要写入此对象的对象输出流
     */
    private void writeObject(ObjectOutputStream os)
        throws IOException
    {
        defineString();
        os.defaultWriteObject();        // 仅写入 string 字段
    }

    /**
     * 从给定的序列化流中重新构建 URI。
     *
     * <p> 调用 {@link java.io.ObjectInputStream#defaultReadObject()} 方法读取 {@code string} 字段的值。
     * 然后以通常的方式解析结果。 </p>
     *
     * @param  is  从中读取此对象的对象输入流
     */
    private void readObject(ObjectInputStream is)
        throws ClassNotFoundException, IOException
    {
        port = -1;                      // Argh
        is.defaultReadObject();
        try {
            new Parser(string).parse(false);
        } catch (URISyntaxException x) {
            IOException y = new InvalidObjectException("无效的 URI");
            y.initCause(x);
            throw y;
        }
    }


    // -- 公共方法结束 --


    // -- 用于字符串字段比较和哈希的实用方法 --

    // 这些方法为 null 字符串参数返回适当的值，从而简化 equals、hashCode 和 compareTo 方法。
    //
    // 忽略大小写的方法只能应用于所有字符均为 US-ASCII 的字符串。由于这一限制，这些方法比 String 类中的类似方法更快。

    // 仅限 US-ASCII
    private static int toLower(char c) {
        if ((c >= 'A') && (c <= 'Z'))
            return c + ('a' - 'A');
        return c;
    }

    // 仅限 US-ASCII
    private static int toUpper(char c) {
        if ((c >= 'a') && (c <= 'z'))
            return c - ('a' - 'A');
        return c;
    }

    private static boolean equal(String s, String t) {
        if (s == t) return true;
        if ((s != null) && (t != null)) {
            if (s.length() != t.length())
                return false;
            if (s.indexOf('%') < 0)
                return s.equals(t);
            int n = s.length();
            for (int i = 0; i < n;) {
                char c = s.charAt(i);
                char d = t.charAt(i);
                if (c != '%') {
                    if (c != d)
                        return false;
                    i++;
                    continue;
                }
                if (d != '%')
                    return false;
                i++;
                if (toLower(s.charAt(i)) != toLower(t.charAt(i)))
                    return false;
                i++;
                if (toLower(s.charAt(i)) != toLower(t.charAt(i)))
                    return false;
                i++;
            }
            return true;
        }
        return false;
    }

    // 仅限 US-ASCII
    private static boolean equalIgnoringCase(String s, String t) {
        if (s == t) return true;
        if ((s != null) && (t != null)) {
            int n = s.length();
            if (t.length() != n)
                return false;
            for (int i = 0; i < n; i++) {
                if (toLower(s.charAt(i)) != toLower(t.charAt(i)))
                    return false;
            }
            return true;
        }
        return false;
    }

    private static int hash(int hash, String s) {
        if (s == null) return hash;
        return s.indexOf('%') < 0 ? hash * 127 + s.hashCode()
                                  : normalizedHash(hash, s);
    }


    private static int normalizedHash(int hash, String s) {
        int h = 0;
        for (int index = 0; index < s.length(); index++) {
            char ch = s.charAt(index);
            h = 31 * h + ch;
            if (ch == '%') {
                /*
                 * 处理接下来的两个编码字符
                 */
                for (int i = index + 1; i < index + 3; i++)
                    h = 31 * h + toUpper(s.charAt(i));
                index += 2;
            }
        }
        return hash * 127 + h;
    }

    // 仅限 US-ASCII
    private static int hashIgnoringCase(int hash, String s) {
        if (s == null) return hash;
        int h = hash;
        int n = s.length();
        for (int i = 0; i < n; i++)
            h = 31 * h + toLower(s.charAt(i));
        return h;
    }

    private static int compare(String s, String t) {
        if (s == t) return 0;
        if (s != null) {
            if (t != null)
                return s.compareTo(t);
            else
                return +1;
        } else {
            return -1;
        }
    }

    // 仅限 US-ASCII
    private static int compareIgnoringCase(String s, String t) {
        if (s == t) return 0;
        if (s != null) {
            if (t != null) {
                int sn = s.length();
                int tn = t.length();
                int n = sn < tn ? sn : tn;
                for (int i = 0; i < n; i++) {
                    int c = toLower(s.charAt(i)) - toLower(t.charAt(i));
                    if (c != 0)
                        return c;
                }
                return sn - tn;
            }
            return +1;
        } else {
            return -1;
        }
    }


    // -- 字符串构造 --

    // 如果提供了 scheme，则如果提供了 path，path 必须是绝对路径
    //
    private static void checkPath(String s, String scheme, String path)
        throws URISyntaxException
    {
        if (scheme != null) {
            if ((path != null)
                && ((path.length() > 0) && (path.charAt(0) != '/')))
                throw new URISyntaxException(s,
                                             "绝对 URI 中的相对路径");
        }
    }

    private void appendAuthority(StringBuffer sb,
                                 String authority,
                                 String userInfo,
                                 String host,
                                 int port)
    {
        if (host != null) {
            sb.append("//");
            if (userInfo != null) {
                sb.append(quote(userInfo, L_USERINFO, H_USERINFO));
                sb.append('@');
            }
            boolean needBrackets = ((host.indexOf(':') >= 0)
                                    && !host.startsWith("[")
                                    && !host.endsWith("]"));
            if (needBrackets) sb.append('[');
            sb.append(host);
            if (needBrackets) sb.append(']');
            if (port != -1) {
                sb.append(':');
                sb.append(port);
            }
        } else if (authority != null) {
            sb.append("//");
            if (authority.startsWith("[")) {
                // authority 应该（但可能不）包含嵌入的 IPv6 地址
                int end = authority.indexOf("]");
                String doquote = authority, dontquote = "";
                if (end != -1 && authority.indexOf(":") != -1) {
                    // authority 包含 IPv6 地址
                    if (end == authority.length()) {
                        dontquote = authority;
                        doquote = "";
                    } else {
                        dontquote = authority.substring(0 , end + 1);
                        doquote = authority.substring(end + 1);
                    }
                }
                sb.append(dontquote);
                sb.append(quote(doquote,
                            L_REG_NAME | L_SERVER,
                            H_REG_NAME | H_SERVER));
            } else {
                sb.append(quote(authority,
                            L_REG_NAME | L_SERVER,
                            H_REG_NAME | H_SERVER));
            }
        }
    }

    private void appendSchemeSpecificPart(StringBuffer sb,
                                          String opaquePart,
                                          String authority,
                                          String userInfo,
                                          String host,
                                          int port,
                                          String path,
                                          String query)
    {
        if (opaquePart != null) {
            /* 检查 SSP 是否以 IPv6 地址开头
             * 因为我们不能对字面 IPv6 地址进行编码
             */
            if (opaquePart.startsWith("//[")) {
                int end =  opaquePart.indexOf("]");
                if (end != -1 && opaquePart.indexOf(":")!=-1) {
                    String doquote, dontquote;
                    if (end == opaquePart.length()) {
                        dontquote = opaquePart;
                        doquote = "";
                    } else {
                        dontquote = opaquePart.substring(0,end+1);
                        doquote = opaquePart.substring(end+1);
                    }
                    sb.append (dontquote);
                    sb.append(quote(doquote, L_URIC, H_URIC));
                }
            } else {
                sb.append(quote(opaquePart, L_URIC, H_URIC));
            }
        } else {
            appendAuthority(sb, authority, userInfo, host, port);
            if (path != null)
                sb.append(quote(path, L_PATH, H_PATH));
            if (query != null) {
                sb.append('?');
                sb.append(quote(query, L_URIC, H_URIC));
            }
        }
    }

    private void appendFragment(StringBuffer sb, String fragment) {
        if (fragment != null) {
            sb.append('#');
            sb.append(quote(fragment, L_URIC, H_URIC));
        }
    }

    private String toString(String scheme,
                            String opaquePart,
                            String authority,
                            String userInfo,
                            String host,
                            int port,
                            String path,
                            String query,
                            String fragment)
    {
        StringBuffer sb = new StringBuffer();
        if (scheme != null) {
            sb.append(scheme);
            sb.append(':');
        }
        appendSchemeSpecificPart(sb, opaquePart,
                                 authority, userInfo, host, port,
                                 path, query);
        appendFragment(sb, fragment);
        return sb.toString();
    }

    private void defineSchemeSpecificPart() {
        if (schemeSpecificPart != null) return;
        StringBuffer sb = new StringBuffer();
        appendSchemeSpecificPart(sb, null, getAuthority(), getUserInfo(),
                                 host, port, getPath(), getQuery());
        schemeSpecificPart = sb.toString();
    }

    private void defineString() {
        if (string != null) return;

        StringBuffer sb = new StringBuffer();
        if (scheme != null) {
            sb.append(scheme);
            sb.append(':');
        }
        if (isOpaque()) {
            sb.append(schemeSpecificPart);
        } else {
            if (host != null) {
                sb.append("//");
                if (userInfo != null) {
                    sb.append(userInfo);
                    sb.append('@');
                }
                boolean needBrackets = ((host.indexOf(':') >= 0)
                                    && !host.startsWith("[")
                                    && !host.endsWith("]"));
                if (needBrackets) sb.append('[');
                sb.append(host);
                if (needBrackets) sb.append(']');
                if (port != -1) {
                    sb.append(':');
                    sb.append(port);
                }
            } else if (authority != null) {
                sb.append("//");
                sb.append(authority);
            }
            if (path != null)
                sb.append(path);
            if (query != null) {
                sb.append('?');
                sb.append(query);
            }
        }
        if (fragment != null) {
            sb.append('#');
            sb.append(fragment);
        }
        string = sb.toString();
    }


    // -- 规范化、解析和相对化 --

    // RFC2396 5.2 (6)
    private static String resolvePath(String base, String child,
                                      boolean absolute)
    {
        int i = base.lastIndexOf('/');
        int cn = child.length();
        String path = "";

        if (cn == 0) {
            // 5.2 (6a)
            if (i >= 0)
                path = base.substring(0, i + 1);
        } else {
            StringBuffer sb = new StringBuffer(base.length() + cn);
            // 5.2 (6a)
            if (i >= 0)
                sb.append(base.substring(0, i + 1));
            // 5.2 (6b)
            sb.append(child);
            path = sb.toString();
        }

        // 5.2 (6c-f)
        String np = normalize(path);

        // 5.2 (6g): 如果结果是绝对的但路径以 "../" 开头，
        // 则我们简单地保持路径不变

        return np;
    }

    // RFC2396 5.2
    private static URI resolve(URI base, URI child) {
        // 首先检查 child 是否为不透明，以便在 child 为 null 时抛出 NPE。
        if (child.isOpaque() || base.isOpaque())
            return child;

        // 5.2 (2): 当前文档的引用（单独的片段）
        if ((child.scheme == null) && (child.authority == null)
            && child.path.equals("") && (child.fragment != null)
            && (child.query == null)) {
            if ((base.fragment != null)
                && child.fragment.equals(base.fragment)) {
                return base;
            }
            URI ru = new URI();
            ru.scheme = base.scheme;
            ru.authority = base.authority;
            ru.userInfo = base.userInfo;
            ru.host = base.host;
            ru.port = base.port;
            ru.path = base.path;
            ru.fragment = child.fragment;
            ru.query = base.query;
            return ru;
        }

        // 5.2 (3): Child 是绝对的
        if (child.scheme != null)
            return child;

        URI ru = new URI();             // 解析后的 URI
        ru.scheme = base.scheme;
        ru.query = child.query;
        ru.fragment = child.fragment;

        // 5.2 (4): Authority
        if (child.authority == null) {
            ru.authority = base.authority;
            ru.host = base.host;
            ru.userInfo = base.userInfo;
            ru.port = base.port;

            String cp = (child.path == null) ? "" : child.path;
            if ((cp.length() > 0) && (cp.charAt(0) == '/')) {
                // 5.2 (5): Child 路径是绝对的
                ru.path = child.path;
            } else {
                // 5.2 (6): 解析相对路径
                ru.path = resolvePath(base.path, cp, base.isAbsolute());
            }
        } else {
            ru.authority = child.authority;
            ru.host = child.host;
            ru.userInfo = child.userInfo;
            ru.host = child.host;
            ru.port = child.port;
            ru.path = child.path;
        }


    /**
     * 重新组合（这里无事可做）
     */
    return ru;
}

/**
 * 如果给定的URI路径是正常的，则返回该URI；
 * 否则，返回一个包含规范化路径的新URI。
 */
private static URI normalize(URI u) {
    if (u.isOpaque() || (u.path == null) || (u.path.length() == 0))
        return u;

    String np = normalize(u.path);
    if (np == u.path)
        return u;

    URI v = new URI();
    v.scheme = u.scheme;
    v.fragment = u.fragment;
    v.authority = u.authority;
    v.userInfo = u.userInfo;
    v.host = u.host;
    v.port = u.port;
    v.path = np;
    v.query = u.query;
    return v;
}

/**
 * 如果两个URI都是分层的，它们的方案和权限组件相同，并且基础路径是子路径的前缀，
 * 则返回一个相对URI，该URI在与基础路径解析时会生成子路径；否则，返回子路径。
 */
private static URI relativize(URI base, URI child) {
    // 首先检查子路径是否不透明，以便在子路径为null时抛出NPE。
    if (child.isOpaque() || base.isOpaque())
        return child;
    if (!equalIgnoringCase(base.scheme, child.scheme)
        || !equal(base.authority, child.authority))
        return child;

    String bp = normalize(base.path);
    String cp = normalize(child.path);
    if (!bp.equals(cp)) {
        if (!bp.endsWith("/"))
            bp = bp + "/";
        if (!cp.startsWith(bp))
            return child;
    }

    URI v = new URI();
    v.path = cp.substring(bp.length());
    v.query = child.query;
    v.fragment = child.fragment;
    return v;
}


// -- 路径规范化 --

/**
 * 以下路径规范化算法通过使用单个字符数组并在原地编辑它来避免为每个段创建字符串对象，
 * 以及使用字符串缓冲区来计算最终结果。数组首先被分割成段，将每个斜杠替换为'\0'，
 * 并创建一个段索引数组，其中每个元素是相应段的第一个字符的索引。然后我们遍历两个数组，
 * 通过将它们在索引数组中的条目设置为-1来移除"."、".."和其他段。最后，使用两个数组重新连接段，
 * 并计算最终结果。
 *
 * 此代码基于 src/solaris/native/java/io/canonicalize_md.c
 */

/**
 * 检查给定路径是否可能需要规范化。如果路径包含重复的斜杠、"."段或".."段，则可能需要规范化。
 * 如果不需要进一步规范化，则返回-1，否则返回找到的段数。
 *
 * 该方法接受字符串参数而不是字符数组，以便在不调用 path.toCharArray() 的情况下执行此测试。
 */
static private int needsNormalization(String path) {
    boolean normal = true;
    int ns = 0;                     // 段数
    int end = path.length() - 1;    // 路径中的最后一个字符的索引
    int p = 0;                      // 路径中的下一个字符的索引

    // 跳过初始斜杠
    while (p <= end) {
        if (path.charAt(p) != '/') break;
        p++;
    }
    if (p > 1) normal = false;

    // 扫描段
    while (p <= end) {

        // 查看是否为"."或".."？
        if ((path.charAt(p) == '.')
            && ((p == end)
                || ((path.charAt(p + 1) == '/')
                    || ((path.charAt(p + 1) == '.')
                        && ((p + 1 == end)
                            || (path.charAt(p + 2) == '/')))))) {
            normal = false;
        }
        ns++;

        // 查找下一个段的开始
        while (p <= end) {
            if (path.charAt(p++) != '/')
                continue;

            // 跳过冗余的斜杠
            while (p <= end) {
                if (path.charAt(p) != '/') break;
                normal = false;
                p++;
            }

            break;
        }
    }

    return normal ? -1 : ns;
}

/**
 * 将给定路径分割成段，将斜杠替换为null，并填充给定的段索引数组。
 *
 * 前提条件：
 *   segs.length == 路径中的段数
 *
 * 后置条件：
 *   路径中的所有斜杠都被替换为 '\0'
 *   segs[i] == 段 i 的第一个字符的索引 (0 <= i < segs.length)
 */
static private void split(char[] path, int[] segs) {
    int end = path.length - 1;      // 路径中的最后一个字符的索引
    int p = 0;                      // 路径中的下一个字符的索引
    int i = 0;                      // 当前段的索引

    // 跳过初始斜杠
    while (p <= end) {
        if (path[p] != '/') break;
        path[p] = '\0';
        p++;
    }

    while (p <= end) {

        // 记录段的开始
        segs[i++] = p++;

        // 查找下一个段的开始
        while (p <= end) {
            if (path[p++] != '/')
                continue;
            path[p - 1] = '\0';

            // 跳过冗余的斜杠
            while (p <= end) {
                if (path[p] != '/') break;
                path[p++] = '\0';
            }
            break;
        }
    }

    if (i != segs.length)
        throw new InternalError();  // 断言
}

/**
 * 根据给定的段索引数组连接给定路径中的段，忽略那些索引条目被设置为-1的段，
 * 并在需要时插入斜杠。返回结果路径的长度。
 *
 * 前提条件：
 *   segs[i] == -1 意味着段 i 被忽略
 *   路径由 split 计算，如上所述，用 '\0' 替换了 '/'
 *
 * 后置条件：
 *   path[0] .. path[返回值] == 结果路径
 */
static private int join(char[] path, int[] segs) {
    int ns = segs.length;           // 段数
    int end = path.length - 1;      // 路径中的最后一个字符的索引
    int p = 0;                      // 要写入的下一个路径字符的索引

    if (path[p] == '\0') {
        // 恢复绝对路径的初始斜杠
        path[p++] = '/';
    }

    for (int i = 0; i < ns; i++) {
        int q = segs[i];            // 当前段
        if (q == -1)
            // 忽略此段
            continue;

        if (p == q) {
            // 我们已经在这一段了，所以只需跳到它的末尾
            while ((p <= end) && (path[p] != '\0'))
                p++;
            if (p <= end) {
                // 保留尾部斜杠
                path[p++] = '/';
            }
        } else if (p < q) {
            // 将 q 复制到 p
            while ((q <= end) && (path[q] != '\0'))
                path[p++] = path[q++];
            if (q <= end) {
                // 保留尾部斜杠
                path[p++] = '/';
            }
        } else
            throw new InternalError(); // 断言 false
    }

    return p;
}

/**
 * 从给定路径中移除 "." 段，并移除由非 ".." 段后跟 ".." 段组成的段对。
 */
private static void removeDots(char[] path, int[] segs) {
    int ns = segs.length;
    int end = path.length - 1;

    for (int i = 0; i < ns; i++) {
        int dots = 0;               // 找到的点数 (0, 1, 或 2)

        // 查找下一个 "." 或 ".." 的出现
        do {
            int p = segs[i];
            if (path[p] == '.') {
                if (p == end) {
                    dots = 1;
                    break;
                } else if (path[p + 1] == '\0') {
                    dots = 1;
                    break;
                } else if ((path[p + 1] == '.')
                           && ((p + 1 == end)
                               || (path[p + 2] == '\0'))) {
                    dots = 2;
                    break;
                }
            }
            i++;
        } while (i < ns);
        if ((i > ns) || (dots == 0))
            break;

        if (dots == 1) {
            // 移除此 "." 的出现
            segs[i] = -1;
        } else {
            // 如果有前面的非 ".." 段，则移除该段和此 ".." 的出现；
            // 否则，保留此 ".." 段。
            int j;
            for (j = i - 1; j >= 0; j--) {
                if (segs[j] != -1) break;
            }
            if (j >= 0) {
                int q = segs[j];
                if (!((path[q] == '.')
                      && (path[q + 1] == '.')
                      && (path[q + 2] == '\0'))) {
                    segs[i] = -1;
                    segs[j] = -1;
                }
            }
        }
    }
}

/**
 * 偏差：如果规范化路径是相对的，并且如果第一个段可以解析为方案名称，则在前面添加一个 "." 段
 */
private static void maybeAddLeadingDot(char[] path, int[] segs) {

    if (path[0] == '\0')
        // 路径是绝对的
        return;

    int ns = segs.length;
    int f = 0;                      // 第一个段的索引
    while (f < ns) {
        if (segs[f] >= 0)
            break;
        f++;
    }
    if ((f >= ns) || (f == 0))
        // 路径为空，或者原始第一个段已保留，
        // 在这种情况下，我们已经知道不需要前导 "."
        return;

    int p = segs[f];
    while ((p < path.length) && (path[p] != ':') && (path[p] != '\0')) p++;
    if (p >= path.length || path[p] == '\0')
        // 第一个段中没有冒号，因此不需要 "."
        return;

    // 此时我们知道第一个段未使用，
    // 因此可以在该位置插入一个 "." 段
    path[0] = '.';
    path[1] = '\0';
    segs[0] = 0;
}

/**
 * 规范化给定的路径字符串。规范化路径字符串没有空段（即，"//" 的出现）、
 * 没有等于 "." 的段，也没有由不等于 ".." 的段前导的等于 ".." 的段。
 * 与 Unix 风格的路径名规范化不同，对于 URI 路径，我们总是保留尾部斜杠。
 */
private static String normalize(String ps) {

    // 此路径是否需要规范化？
    int ns = needsNormalization(ps);        // 段数
    if (ns < 0)
        // 不需要 —— 直接返回
        return ps;

    char[] path = ps.toCharArray();         // 字符数组形式的路径

    // 将路径分割成段
    int[] segs = new int[ns];               // 段索引数组
    split(path, segs);

    // 移除点
    removeDots(path, segs);

    // 防止方案名称混淆
    maybeAddLeadingDot(path, segs);

    // 连接剩余的段并返回结果
    String s = new String(path, 0, join(path, segs));
    if (s.equals(ps)) {
        // 字符串已经规范化
        return ps;
    }
    return s;
}

// -- 用于解析的字符类 --

/**
 * RFC2396 精确地指定了 US-ASCII 字符集中哪些字符允许出现在 URI 引用的各个组件中。
 * 我们在这里定义了一组掩码对，以帮助强制执行这些限制。每个掩码对由两个 long 组成，
 * 一个低掩码和一个高掩码。它们一起表示一个 128 位掩码，其中位 i 被设置当且仅当值为 i 的字符被允许。
 *
 * 这种方法比顺序搜索允许字符的数组更高效。通过预编译掩码信息，可以进一步提高效率，
 * 使得可以通过单次表查找来确定字符是否存在于给定掩码中。
 */

// 计算给定字符串中字符的低阶掩码
private static long lowMask(String chars) {
    int n = chars.length();
    long m = 0;
    for (int i = 0; i < n; i++) {
        char c = chars.charAt(i);
        if (c < 64)
            m |= (1L << c);
    }
    return m;
}

// 计算给定字符串中字符的高阶掩码
private static long highMask(String chars) {
    int n = chars.length();
    long m = 0;
    for (int i = 0; i < n; i++) {
        char c = chars.charAt(i);
        if ((c >= 64) && (c < 128))
            m |= (1L << (c - 64));
    }
    return m;
}

// 计算从 first 到 last（包括）的字符的低阶掩码
private static long lowMask(char first, char last) {
    long m = 0;
    int f = Math.max(Math.min(first, 63), 0);
    int l = Math.max(Math.min(last, 63), 0);
    for (int i = f; i <= l; i++)
        m |= 1L << i;
    return m;
}

// 计算从 first 到 last（包括）的字符的高阶掩码
private static long highMask(char first, char last) {
    long m = 0;
    int f = Math.max(Math.min(first, 127), 64) - 64;
    int l = Math.max(Math.min(last, 127), 64) - 64;
    for (int i = f; i <= l; i++)
        m |= 1L << i;
    return m;
}

// 告诉给定字符是否被给定的掩码对允许
private static boolean match(char c, long lowMask, long highMask) {
    if (c == 0) // 0 在掩码中没有位置。因此，它从不匹配。
        return false;
    if (c < 64)
        return ((1L << c) & lowMask) != 0;
    if (c < 128)
        return ((1L << (c - 64)) & highMask) != 0;
    return false;
}

// 字符类掩码，按 RFC2396 的相反顺序，因为静态字段的初始化器不能向前引用。

// digit    = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" |
//            "8" | "9"
private static final long L_DIGIT = lowMask('0', '9');
private static final long H_DIGIT = 0L;

// upalpha  = "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" |
//            "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" |
//            "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"
private static final long L_UPALPHA = 0L;
private static final long H_UPALPHA = highMask('A', 'Z');

// lowalpha = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" |
//            "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" |
//            "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"
private static final long L_LOWALPHA = 0L;
private static final long H_LOWALPHA = highMask('a', 'z');


                // alpha         = lowalpha | upalpha
    private static final long L_ALPHA = L_LOWALPHA | L_UPALPHA;
    private static final long H_ALPHA = H_LOWALPHA | H_UPALPHA;

    // alphanum      = alpha | digit
    private static final long L_ALPHANUM = L_DIGIT | L_ALPHA;
    private static final long H_ALPHANUM = H_DIGIT | H_ALPHA;

    // hex           = digit | "A" | "B" | "C" | "D" | "E" | "F" |
    //                         "a" | "b" | "c" | "d" | "e" | "f"
    private static final long L_HEX = L_DIGIT;
    private static final long H_HEX = highMask('A', 'F') | highMask('a', 'f');

    // mark          = "-" | "_" | "." | "!" | "~" | "*" | "'" |
    //                 "(" | ")"
    private static final long L_MARK = lowMask("-_.!~*'()");
    private static final long H_MARK = highMask("-_.!~*'()");

    // unreserved    = alphanum | mark
    private static final long L_UNRESERVED = L_ALPHANUM | L_MARK;
    private static final long H_UNRESERVED = H_ALPHANUM | H_MARK;

    // reserved      = ";" | "/" | "?" | ":" | "@" | "&" | "=" | "+" |
    //                 "$" | "," | "[" | "]"
    // 根据 RFC2732 添加: "[", "]"
    private static final long L_RESERVED = lowMask(";/?:@&=+$,[]");
    private static final long H_RESERVED = highMask(";/?:@&=+$,[]");

    // 第零位用于指示允许转义对和非 US-ASCII 字符；这由下面的 scanEscape 方法处理。
    private static final long L_ESCAPED = 1L;
    private static final long H_ESCAPED = 0L;

    // uric          = reserved | unreserved | escaped
    private static final long L_URIC = L_RESERVED | L_UNRESERVED | L_ESCAPED;
    private static final long H_URIC = H_RESERVED | H_UNRESERVED | H_ESCAPED;

    // pchar         = unreserved | escaped |
    //                 ":" | "@" | "&" | "=" | "+" | "$" | ","
    private static final long L_PCHAR
        = L_UNRESERVED | L_ESCAPED | lowMask(":@&=+$,");
    private static final long H_PCHAR
        = H_UNRESERVED | H_ESCAPED | highMask(":@&=+$,");

    // 所有有效的路径字符
    private static final long L_PATH = L_PCHAR | lowMask(";/");
    private static final long H_PATH = H_PCHAR | highMask(";/");

    // 连字符，用于 domainlabel 和 toplabel
    private static final long L_DASH = lowMask("-");
    private static final long H_DASH = highMask("-");

    // 点，用于主机名
    private static final long L_DOT = lowMask(".");
    private static final long H_DOT = highMask(".");

    // userinfo      = *( unreserved | escaped |
    //                    ";" | ":" | "&" | "=" | "+" | "$" | "," )
    private static final long L_USERINFO
        = L_UNRESERVED | L_ESCAPED | lowMask(";:&=+$,");
    private static final long H_USERINFO
        = H_UNRESERVED | H_ESCAPED | highMask(";:&=+$,");

    // reg_name      = 1*( unreserved | escaped | "$" | "," |
    //                     ";" | ":" | "@" | "&" | "=" | "+" )
    private static final long L_REG_NAME
        = L_UNRESERVED | L_ESCAPED | lowMask("$,;:@&=+");
    private static final long H_REG_NAME
        = H_UNRESERVED | H_ESCAPED | highMask("$,;:@&=+");

    // 所有有效的服务器基于权威的字符
    private static final long L_SERVER
        = L_USERINFO | L_ALPHANUM | L_DASH | lowMask(".:@[]");
    private static final long H_SERVER
        = H_USERINFO | H_ALPHANUM | H_DASH | highMask(".:@[]");

    // 服务器权威的特殊情况，表示 IPv6 地址
    // 在这种情况下，% 不表示转义序列
    private static final long L_SERVER_PERCENT
        = L_SERVER | lowMask("%");
    private static final long H_SERVER_PERCENT
        = H_SERVER | highMask("%");
    private static final long L_LEFT_BRACKET = lowMask("[");
    private static final long H_LEFT_BRACKET = highMask("[");

    // scheme        = alpha *( alpha | digit | "+" | "-" | "." )
    private static final long L_SCHEME = L_ALPHA | L_DIGIT | lowMask("+-.");
    private static final long H_SCHEME = H_ALPHA | H_DIGIT | highMask("+-.");

    // uric_no_slash = unreserved | escaped | ";" | "?" | ":" | "@" |
    //                 "&" | "=" | "+" | "$" | ","
    private static final long L_URIC_NO_SLASH
        = L_UNRESERVED | L_ESCAPED | lowMask(";?:@&=+$,");
    private static final long H_URIC_NO_SLASH
        = H_UNRESERVED | H_ESCAPED | highMask(";?:@&=+$,");


    // -- 转义和编码 --

    private final static char[] hexDigits = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static void appendEscape(StringBuffer sb, byte b) {
        sb.append('%');
        sb.append(hexDigits[(b >> 4) & 0x0f]);
        sb.append(hexDigits[(b >> 0) & 0x0f]);
    }

    private static void appendEncoded(StringBuffer sb, char c) {
        ByteBuffer bb = null;
        try {
            bb = ThreadLocalCoders.encoderFor("UTF-8")
                .encode(CharBuffer.wrap("" + c));
        } catch (CharacterCodingException x) {
            assert false;
        }
        while (bb.hasRemaining()) {
            int b = bb.get() & 0xff;
            if (b >= 0x80)
                appendEscape(sb, (byte)b);
            else
                sb.append((char)b);
        }
    }

    // 引用 s 中不允许的任何字符
    // 由给定的掩码对
    //
    private static String quote(String s, long lowMask, long highMask) {
        int n = s.length();
        StringBuffer sb = null;
        boolean allowNonASCII = ((lowMask & L_ESCAPED) != 0);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '\u0080') {
                if (!match(c, lowMask, highMask)) {
                    if (sb == null) {
                        sb = new StringBuffer();
                        sb.append(s.substring(0, i));
                    }
                    appendEscape(sb, (byte)c);
                } else {
                    if (sb != null)
                        sb.append(c);
                }
            } else if (allowNonASCII
                       && (Character.isSpaceChar(c)
                           || Character.isISOControl(c))) {
                if (sb == null) {
                    sb = new StringBuffer();
                    sb.append(s.substring(0, i));
                }
                appendEncoded(sb, c);
            } else {
                if (sb != null)
                    sb.append(c);
            }
        }
        return (sb == null) ? s : sb.toString();
    }

    // 将所有 >= \u0080 的字符编码为转义的、规范化的 UTF-8 八位字节，
    // 假设 s 否则是合法的
    //
    private static String encode(String s) {
        int n = s.length();
        if (n == 0)
            return s;

        // 首先检查是否实际需要编码
        for (int i = 0;;) {
            if (s.charAt(i) >= '\u0080')
                break;
            if (++i >= n)
                return s;
        }

        String ns = Normalizer.normalize(s, Normalizer.Form.NFC);
        ByteBuffer bb = null;
        try {
            bb = ThreadLocalCoders.encoderFor("UTF-8")
                .encode(CharBuffer.wrap(ns));
        } catch (CharacterCodingException x) {
            assert false;
        }

        StringBuffer sb = new StringBuffer();
        while (bb.hasRemaining()) {
            int b = bb.get() & 0xff;
            if (b >= 0x80)
                appendEscape(sb, (byte)b);
            else
                sb.append((char)b);
        }
        return sb.toString();
    }

    private static int decode(char c) {
        if ((c >= '0') && (c <= '9'))
            return c - '0';
        if ((c >= 'a') && (c <= 'f'))
            return c - 'a' + 10;
        if ((c >= 'A') && (c <= 'F'))
            return c - 'A' + 10;
        assert false;
        return -1;
    }

    private static byte decode(char c1, char c2) {
        return (byte)(  ((decode(c1) & 0xf) << 4)
                      | ((decode(c2) & 0xf) << 0));
    }

    // 评估 s 中的所有转义，必要时应用 UTF-8 解码。假设
    // 转义在语法上是格式良好的，即形式为 %XX。如果
    // 转义的八位字节序列不是有效的 UTF-8，则错误的八位字节
    // 将被替换为 '\uFFFD'。
    // 例外：在 "[]" 之间的任何 "%" 都保持不变。它是一个 IPv6 字面量
    //       带有 scope_id
    //
    private static String decode(String s) {
        if (s == null)
            return s;
        int n = s.length();
        if (n == 0)
            return s;
        if (s.indexOf('%') < 0)
            return s;

        StringBuffer sb = new StringBuffer(n);
        ByteBuffer bb = ByteBuffer.allocate(n);
        CharBuffer cb = CharBuffer.allocate(n);
        CharsetDecoder dec = ThreadLocalCoders.decoderFor("UTF-8")
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE);

        // 这不是很高效，但目前可以使用
        char c = s.charAt(0);
        boolean betweenBrackets = false;

        for (int i = 0; i < n;) {
            assert c == s.charAt(i);    // 循环不变式
            if (c == '[') {
                betweenBrackets = true;
            } else if (betweenBrackets && c == ']') {
                betweenBrackets = false;
            }
            if (c != '%' || betweenBrackets) {
                sb.append(c);
                if (++i >= n)
                    break;
                c = s.charAt(i);
                continue;
            }
            bb.clear();
            int ui = i;
            for (;;) {
                assert (n - i >= 2);
                bb.put(decode(s.charAt(++i), s.charAt(++i)));
                if (++i >= n)
                    break;
                c = s.charAt(i);
                if (c != '%')
                    break;
            }
            bb.flip();
            cb.clear();
            dec.reset();
            CoderResult cr = dec.decode(bb, cb, true);
            assert cr.isUnderflow();
            cr = dec.flush(cb);
            assert cr.isUnderflow();
            sb.append(cb.flip().toString());
        }

        return sb.toString();
    }


    // -- 解析 --

    // 为了方便，我们将输入的 URI 字符串包装在以下内部类的新实例中。这节省了总是
    // 将输入字符串作为参数传递给每个内部扫描/解析方法。

    private class Parser {

        private String input;           // URI 输入字符串
        private boolean requireServerAuthority = false;

        Parser(String s) {
            input = s;
            string = s;
        }

        // -- 以各种方式抛出 URISyntaxException 的方法 --

        private void fail(String reason) throws URISyntaxException {
            throw new URISyntaxException(input, reason);
        }

        private void fail(String reason, int p) throws URISyntaxException {
            throw new URISyntaxException(input, reason, p);
        }

        private void failExpecting(String expected, int p)
            throws URISyntaxException
        {
            fail("Expected " + expected, p);
        }

        private void failExpecting(String expected, String prior, int p)
            throws URISyntaxException
        {
            fail("Expected " + expected + " following " + prior, p);
        }


        // -- 对输入字符串的简单访问 --

        // 返回输入字符串的子字符串
        //
        private String substring(int start, int end) {
            return input.substring(start, end);
        }

        // 返回位置 p 处的字符，
        // 假设 p < input.length()
        //
        private char charAt(int p) {
            return input.charAt(p);
        }

        // 告诉 start < end 且，如果是，则 charAt(start) == c
        //
        private boolean at(int start, int end, char c) {
            return (start < end) && (charAt(start) == c);
        }

        // 告诉 start + s.length() < end 且，如果是，则
        // start 位置的字符与 s 完全匹配
        //
        private boolean at(int start, int end, String s) {
            int p = start;
            int sn = s.length();
            if (sn > end - p)
                return false;
            int i = 0;
            while (i < sn) {
                if (charAt(p++) != s.charAt(i)) {
                    break;
                }
                i++;
            }
            return (i == sn);
        }


        // -- 扫描 --

        // 以下的各种扫描和解析方法使用一个统一的
        // 从当前开始位置和结束索引作为前两个参数的约定。开始是包含的，而结束
        // 是排除的，就像在 String 类中一样，即，开始/结束对
        // 表示输入字符串的左开区间 [start, end)。
        //
        // 这些方法永远不会超过结束位置。它们可能会返回
        // -1 以表示完全失败，但更常见的是它们简单地返回
        // 扫描的最后一个字符之后的第一个字符的位置。因此
        // 一个典型的用法是
        //
        //     int p = start;
        //     int q = scan(p, end, ...);
        //     if (q > p)
        //         // 我们扫描了一些内容
        //         ...;
        //     else if (q == p)
        //         // 我们没有扫描任何内容
        //         ...;
        //     else if (q == -1)
        //         // 出了问题
        //         ...;


        // 扫描特定字符：如果给定开始位置的字符
        // 等于 c，则返回下一个字符的索引；否则，返回
        // 开始位置。
        //
        private int scan(int start, int end, char c) {
            if ((start < end) && (charAt(start) == c))
                return start + 1;
            return start;
        }

        // 从给定的开始位置向前扫描。在第一个字符
        // 在 err 字符串中停止（在这种情况下返回 -1），或第一个字符
        // 在 stop 字符串中停止（在这种情况下返回前一个字符的索引），或
        // 输入字符串的末尾（在这种情况下返回输入字符串的长度）。可能返回
        // 开始位置如果没有任何匹配。
        //
        private int scan(int start, int end, String err, String stop) {
            int p = start;
            while (p < end) {
                char c = charAt(p);
                if (err.indexOf(c) >= 0)
                    return -1;
                if (stop.indexOf(c) >= 0)
                    break;
                p++;
            }
            return p;
        }

        // 从给定位置开始扫描潜在的转义序列，
        // 以给定的第一个字符（即 charAt(start) == c）。
        //
        // 此方法假设如果允许转义，则可见的非 US-ASCII 字符
        // 也是允许的。
        //
        private int scanEscape(int start, int n, char first)
            throws URISyntaxException
        {
            int p = start;
            char c = first;
            if (c == '%') {
                // 处理转义对
                if ((p + 3 <= n)
                    && match(charAt(p + 1), L_HEX, H_HEX)
                    && match(charAt(p + 2), L_HEX, H_HEX)) {
                    return p + 3;
                }
                fail("Malformed escape pair", p);
            } else if ((c > 128)
                       && !Character.isSpaceChar(c)
                       && !Character.isISOControl(c)) {
                // 允许未转义但可见的非 US-ASCII 字符
                return p + 1;
            }
            return p;
        }


                    // 扫描与给定掩码对匹配的字符
        //
        private int scan(int start, int n, long lowMask, long highMask)
            throws URISyntaxException
        {
            int p = start;
            while (p < n) {
                char c = charAt(p);
                if (match(c, lowMask, highMask)) {
                    p++;
                    continue;
                }
                if ((lowMask & L_ESCAPED) != 0) {
                    int q = scanEscape(p, n, c);
                    if (q > p) {
                        p = q;
                        continue;
                    }
                }
                break;
            }
            return p;
        }

        // 检查 [start, end) 范围内的每个字符是否与给定的掩码匹配
        //
        private void checkChars(int start, int end,
                                long lowMask, long highMask,
                                String what)
            throws URISyntaxException
        {
            int p = scan(start, end, lowMask, highMask);
            if (p < end)
                fail("非法字符在 " + what, p);
        }

        // 检查位置 p 处的字符是否与给定的掩码匹配
        //
        private void checkChar(int p,
                               long lowMask, long highMask,
                               String what)
            throws URISyntaxException
        {
            checkChars(p, p + 1, lowMask, highMask, what);
        }


        // -- 解析 --

        // [<scheme>:]<scheme-specific-part>[#<fragment>]
        //
        void parse(boolean rsa) throws URISyntaxException {
            requireServerAuthority = rsa;
            int ssp;                    // scheme-specific 部分的开始
            int n = input.length();
            int p = scan(0, n, "/?#", ":");
            if ((p >= 0) && at(p, n, ':')) {
                if (p == 0)
                    failExpecting("方案名称", 0);
                checkChar(0, L_ALPHA, H_ALPHA, "方案名称");
                checkChars(1, p, L_SCHEME, H_SCHEME, "方案名称");
                scheme = substring(0, p);
                p++;                    // 跳过 ':'
                ssp = p;
                if (at(p, n, '/')) {
                    p = parseHierarchical(p, n);
                } else {
                    int q = scan(p, n, "", "#");
                    if (q <= p)
                        failExpecting("scheme-specific 部分", p);
                    checkChars(p, q, L_URIC, H_URIC, "不透明部分");
                    p = q;
                }
            } else {
                ssp = 0;
                p = parseHierarchical(0, n);
            }
            schemeSpecificPart = substring(ssp, p);
            if (at(p, n, '#')) {
                checkChars(p + 1, n, L_URIC, H_URIC, "片段");
                fragment = substring(p + 1, n);
                p = n;
            }
            if (p < n)
                fail("URI 结束", p);
        }

        // [//authority]<path>[?<query>]
        //
        // 与 RFC2396 的偏差：我们允许空的 authority 组件，只要它后面跟着非空的路径、查询组件或片段组件。这是为了让如 "file:///foo/bar" 这样的 URI 能够解析。这似乎是 RFC2396 的意图，尽管语法不允许这样做。如果 authority 为空，则 userInfo、host 和 port 组件未定义。
        //
        // 与 RFC2396 的偏差：我们允许空的相对路径。这似乎是 RFC2396 的意图，但语法不允许这样做。这种偏差的主要后果是 "#f" 解析为一个相对 URI，路径为空。
        //
        private int parseHierarchical(int start, int n)
            throws URISyntaxException
        {
            int p = start;
            if (at(p, n, '/') && at(p + 1, n, '/')) {
                p += 2;
                int q = scan(p, n, "", "/?#");
                if (q > p) {
                    p = parseAuthority(p, q);
                } else if (q < n) {
                    // 偏差：允许在非空路径、查询组件或片段标识符之前为空的 authority
                } else
                    failExpecting("authority", p);
            }
            int q = scan(p, n, "", "?#"); // 偏差：可以为空
            checkChars(p, q, L_PATH, H_PATH, "路径");
            path = substring(p, q);
            p = q;
            if (at(p, n, '?')) {
                p++;
                q = scan(p, n, "", "#");
                checkChars(p, q, L_URIC, H_URIC, "查询");
                query = substring(p, q);
                p = q;
            }
            return p;
        }

        // authority     = server | reg_name
        //
        // 模糊性：一个 authority 可能是注册名称而不是服务器，它可能有一个解析为服务器的前缀。我们使用 authority 组件总是以 '/' 或输入字符串的结束来解决这个问题：如果完整的 authority 未解析为服务器，则我们尝试将其解析为注册名称。
        //
        private int parseAuthority(int start, int n)
            throws URISyntaxException
        {
            int p = start;
            int q = p;
            URISyntaxException ex = null;

            boolean serverChars;
            boolean regChars;

            if (scan(p, n, "", "]") > p) {
                // 包含一个字面 IPv6 地址，因此允许使用 '%'
                serverChars = (scan(p, n, L_SERVER_PERCENT, H_SERVER_PERCENT) == n);
            } else {
                serverChars = (scan(p, n, L_SERVER, H_SERVER) == n);
            }
            regChars = (scan(p, n, L_REG_NAME, H_REG_NAME) == n);

            if (regChars && !serverChars) {
                // 必须是基于注册的 authority
                authority = substring(p, n);
                return n;
            }

            if (serverChars) {
                // 可能是（通常是）基于服务器的 authority，因此尝试将其解析为服务器。如果尝试失败，尝试将其作为基于注册的 authority 处理。
                try {
                    q = parseServer(p, n);
                    if (q < n)
                        failExpecting("authority 结束", q);
                    authority = substring(p, n);
                } catch (URISyntaxException x) {
                    // 撤销失败解析的结果
                    userInfo = null;
                    host = null;
                    port = -1;
                    if (requireServerAuthority) {
                        // 如果我们坚持基于服务器的 authority，则重新抛出异常
                        throw x;
                    } else {
                        // 如果它也不能解析为注册，则保存异常
                        ex = x;
                        q = p;
                    }
                }
            }

            if (q < n) {
                if (regChars) {
                    // 基于注册的 authority
                    authority = substring(p, n);
                } else if (ex != null) {
                    // 重新抛出异常；可能是由于格式错误的 IPv6 地址
                    throw ex;
                } else {
                    fail("非法字符在 authority 中", q);
                }
            }

            return n;
        }


        // [<userinfo>@]<host>[:<port>]
        //
        private int parseServer(int start, int n)
            throws URISyntaxException
        {
            int p = start;
            int q;

            // userinfo
            q = scan(p, n, "/?#", "@");
            if ((q >= p) && at(q, n, '@')) {
                checkChars(p, q, L_USERINFO, H_USERINFO, "用户信息");
                userInfo = substring(p, q);
                p = q + 1;              // 跳过 '@'
            }

            // 主机名、IPv4 地址或 IPv6 地址
            if (at(p, n, '[')) {
                // 与 RFC2396 的偏差：支持 IPv6 地址，根据 RFC2732
                p++;
                q = scan(p, n, "/?#", "]");
                if ((q > p) && at(q, n, ']')) {
                    // 查找 "%" 范围 ID
                    int r = scan (p, q, "", "%");
                    if (r > p) {
                        parseIPv6Reference(p, r);
                        if (r+1 == q) {
                            fail ("需要范围 ID");
                        }
                        checkChars (r+1, q, L_ALPHANUM, H_ALPHANUM,
                                                "范围 ID");
                    } else {
                        parseIPv6Reference(p, q);
                    }
                    host = substring(p-1, q+1);
                    p = q + 1;
                } else {
                    failExpecting("IPv6 地址的结束括号", q);
                }
            } else {
                q = parseIPv4Address(p, n);
                if (q <= p)
                    q = parseHostname(p, n);
                p = q;
            }

            // 端口
            if (at(p, n, ':')) {
                p++;
                q = scan(p, n, "", "/");
                if (q > p) {
                    checkChars(p, q, L_DIGIT, H_DIGIT, "端口号");
                    try {
                        port = Integer.parseInt(substring(p, q));
                    } catch (NumberFormatException x) {
                        fail("格式错误的端口号", p);
                    }
                    p = q;
                }
            }
            if (p < n)
                failExpecting("端口号", p);

            return p;
        }

        // 扫描一个值适合字节的十进制数字字符串
        //
        private int scanByte(int start, int n)
            throws URISyntaxException
        {
            int p = start;
            int q = scan(p, n, L_DIGIT, H_DIGIT);
            if (q <= p) return q;
            if (Integer.parseInt(substring(p, q)) > 255) return p;
            return q;
        }

        // 扫描 IPv4 地址。
        //
        // 如果 strict 参数为 true，则我们要求给定区间除了 IPv4 地址外不包含其他内容；如果为 false，则我们只要求它以 IPv4 地址开始。
        //
        // 如果区间不包含或不以（取决于 strict 参数）合法的 IPv4 地址字符，则我们立即返回 -1；否则我们坚持这些字符解析为合法的 IPv4 地址，并在失败时抛出异常。
        //
        // 我们假设任何十进制数字和点的字符串必须是 IPv4 地址。无论如何它不会解析为主机名，因此在这里做出这样的假设可以抛出更有意义的异常。
        //
        private int scanIPv4Address(int start, int n, boolean strict)
            throws URISyntaxException
        {
            int p = start;
            int q;
            int m = scan(p, n, L_DIGIT | L_DOT, H_DIGIT | H_DOT);
            if ((m <= p) || (strict && (m != n)))
                return -1;
            for (;;) {
                // 根据 RFC2732：每个字节最多三个数字
                // 进一步约束：每个元素适合一个字节
                if ((q = scanByte(p, m)) <= p) break;   p = q;
                if ((q = scan(p, m, '.')) <= p) break;  p = q;
                if ((q = scanByte(p, m)) <= p) break;   p = q;
                if ((q = scan(p, m, '.')) <= p) break;  p = q;
                if ((q = scanByte(p, m)) <= p) break;   p = q;
                if ((q = scan(p, m, '.')) <= p) break;  p = q;
                if ((q = scanByte(p, m)) <= p) break;   p = q;
                if (q < m) break;
                return q;
            }
            fail("格式错误的 IPv4 地址", q);
            return -1;
        }

        // 获取 IPv4 地址：如果给定区间包含除 IPv4 地址外的任何内容，则抛出异常
        //
        private int takeIPv4Address(int start, int n, String expected)
            throws URISyntaxException
        {
            int p = scanIPv4Address(start, n, true);
            if (p <= start)
                failExpecting(expected, start);
            return p;
        }

        // 尝试解析 IPv4 地址，失败时返回 -1，但允许给定区间在 IPv4 地址后包含 [:<字符>]。
        //
        private int parseIPv4Address(int start, int n) {
            int p;

            try {
                p = scanIPv4Address(start, n, false);
            } catch (URISyntaxException x) {
                return -1;
            } catch (NumberFormatException nfe) {
                return -1;
            }

            if (p > start && p < n) {
                // IPv4 地址后面跟着某些东西 - 检查它是否是 ":"，因为这是唯一可以跟在地址后面的合法字符。
                if (charAt(p) != ':') {
                    p = -1;
                }
            }

            if (p > start)
                host = substring(start, p);

            return p;
        }

        // hostname      = domainlabel [ "." ] | 1*( domainlabel "." ) toplabel [ "." ]
        // domainlabel   = alphanum | alphanum *( alphanum | "-" ) alphanum
        // toplabel      = alpha | alpha *( alphanum | "-" ) alphanum
        //
        private int parseHostname(int start, int n)
            throws URISyntaxException
        {
            int p = start;
            int q;
            int l = -1;                 // 最后解析的标签的开始

            do {
                // domainlabel = alphanum [ *( alphanum | "-" ) alphanum ]
                q = scan(p, n, L_ALPHANUM, H_ALPHANUM);
                if (q <= p)
                    break;
                l = p;
                if (q > p) {
                    p = q;
                    q = scan(p, n, L_ALPHANUM | L_DASH, H_ALPHANUM | H_DASH);
                    if (q > p) {
                        if (charAt(q - 1) == '-')
                            fail("非法字符在主机名中", q - 1);
                        p = q;
                    }
                }
                q = scan(p, n, '.');
                if (q <= p)
                    break;
                p = q;
            } while (p < n);

            if ((p < n) && !at(p, n, ':'))
                fail("非法字符在主机名中", p);

            if (l < 0)
                failExpecting("主机名", start);

            // 对于完全限定的主机名，检查最右边的标签是否以字母字符开始。
            if (l > start && !match(charAt(l), L_ALPHA, H_ALPHA)) {
                fail("非法字符在主机名中", l);
            }

            host = substring(start, p);
            return p;
        }


        // IPv6 地址解析，来自 RFC2373: IPv6 地址架构
        //
        // Bug：RFC2373 附录 B 中的语法不允许 ::12.34.56.78 这样的地址，这些地址在文档前面的例子中明确显示。以下是原始语法：
        //
        //   IPv6address = hexpart [ ":" IPv4address ]
        //   hexpart     = hexseq | hexseq "::" [ hexseq ] | "::" [ hexseq ]
        //   hexseq      = hex4 *( ":" hex4)
        //   hex4        = 1*4HEXDIG
        //
        // 因此我们使用以下修订的语法：
        //
        //   IPv6address = hexseq [ ":" IPv4address ]
        //                 | hexseq [ "::" [ hexpost ] ]
        //                 | "::" [ hexpost ]
        //   hexpost     = hexseq | hexseq ":" IPv4address | IPv4address
        //   hexseq      = hex4 *( ":" hex4)
        //   hex4        = 1*4HEXDIG
        //
        // 这涵盖了以下所有情况：
        //
        //   hexseq
        //   hexseq : IPv4address
        //   hexseq ::
        //   hexseq :: hexseq
        //   hexseq :: hexseq : IPv4address
        //   hexseq :: IPv4address
        //   :: hexseq
        //   :: hexseq : IPv4address
        //   :: IPv4address
        //   ::
        //
        // 另外，我们对 IPv6 地址进行如下约束：
        //
        //  i.  不含压缩零的 IPv6 地址应包含正好 16 个字节。
        //
        //  ii. 含有压缩零的 IPv6 地址应包含少于 16 个字节。


                    private int ipv6byteCount = 0;

        private int parseIPv6Reference(int start, int n)
            throws URISyntaxException
        {
            int p = start;
            int q;
            boolean compressedZeros = false;

            q = scanHexSeq(p, n);

            if (q > p) {
                p = q;
                if (at(p, n, "::")) {
                    compressedZeros = true;
                    p = scanHexPost(p + 2, n);
                } else if (at(p, n, ':')) {
                    p = takeIPv4Address(p + 1,  n, "IPv4 地址");
                    ipv6byteCount += 4;
                }
            } else if (at(p, n, "::")) {
                compressedZeros = true;
                p = scanHexPost(p + 2, n);
            }
            if (p < n)
                fail("格式错误的 IPv6 地址", start);
            if (ipv6byteCount > 16)
                fail("IPv6 地址过长", start);
            if (!compressedZeros && ipv6byteCount < 16)
                fail("IPv6 地址过短", start);
            if (compressedZeros && ipv6byteCount == 16)
                fail("格式错误的 IPv6 地址", start);

            return p;
        }

        private int scanHexPost(int start, int n)
            throws URISyntaxException
        {
            int p = start;
            int q;

            if (p == n)
                return p;

            q = scanHexSeq(p, n);
            if (q > p) {
                p = q;
                if (at(p, n, ':')) {
                    p++;
                    p = takeIPv4Address(p, n, "十六进制数字或 IPv4 地址");
                    ipv6byteCount += 4;
                }
            } else {
                p = takeIPv4Address(p, n, "十六进制数字或 IPv4 地址");
                ipv6byteCount += 4;
            }
            return p;
        }

        // 扫描一个十六进制序列；如果无法扫描则返回 -1
        //
        private int scanHexSeq(int start, int n)
            throws URISyntaxException
        {
            int p = start;
            int q;

            q = scan(p, n, L_HEX, H_HEX);
            if (q <= p)
                return -1;
            if (at(q, n, '.'))          // IPv4 地址的开始
                return -1;
            if (q > p + 4)
                fail("IPv6 十六进制数字序列过长", p);
            ipv6byteCount += 2;
            p = q;
            while (p < n) {
                if (!at(p, n, ':'))
                    break;
                if (at(p + 1, n, ':'))
                    break;              // "::"
                p++;
                q = scan(p, n, L_HEX, H_HEX);
                if (q <= p)
                    failExpecting("IPv6 地址的数字", p);
                if (at(q, n, '.')) {    // IPv4 地址的开始
                    p--;
                    break;
                }
                if (q > p + 4)
                    fail("IPv6 十六进制数字序列过长", p);
                ipv6byteCount += 2;
                p = q;
            }

            return p;
        }

    }

}
