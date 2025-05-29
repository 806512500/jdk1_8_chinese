
/*
 * 版权所有 (c) 1994, 2022，Oracle 和/或其关联公司。保留所有权利。
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

package java.io;

import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ArrayList;
import java.security.AccessController;
import java.security.SecureRandom;
import java.nio.file.Path;
import java.nio.file.FileSystems;
import sun.security.action.GetPropertyAction;

/**
 * 文件和目录路径名的抽象表示。
 *
 * <p> 用户界面和操作系统使用系统依赖的 <em>路径名字符串</em> 来命名文件和目录。此类提供了一个抽象的、系统独立的层次路径名视图。一个 <em>抽象路径名</em> 包含两个组件：
 *
 * <ol>
 * <li> 一个可选的系统依赖的 <em>前缀</em> 字符串，例如磁盘驱动器标识符，UNIX 根目录的 <code>"/"</code>，或 Microsoft Windows UNC 路径名的 <code>"\\\\"</code>，以及
 * <li> 零个或多个字符串 <em>名称</em> 的序列。
 * </ol>
 *
 * 抽象路径名中的第一个名称可以是目录名，或者在 Microsoft Windows UNC 路径名的情况下，可以是主机名。抽象路径名中的每个后续名称表示一个目录；最后一个名称可以表示一个目录或文件。<em>空</em> 抽象路径名没有前缀且名称序列为空。
 *
 * <p> 路径名字符串与抽象路径名之间的转换本质上是系统依赖的。当抽象路径名转换为路径名字符串时，每个名称由默认的 <em>分隔符字符</em> 分隔。默认的名称分隔符字符由系统属性 <code>file.separator</code> 定义，并在本类的公共静态字段 <code>{@link
 * #separator}</code> 和 <code>{@link #separatorChar}</code> 中提供。当路径名字符串转换为抽象路径名时，名称之间可以由默认的名称分隔符字符或底层系统支持的任何其他名称分隔符字符分隔。
 *
 * <p> 无论是抽象的还是字符串形式的路径名，都可能是 <em>绝对的</em> 或 <em>相对的</em>。绝对路径名是完整的，不需要其他信息即可定位它所表示的文件。相对路径名则必须根据从其他路径名获取的信息进行解释。默认情况下，<code>java.io</code> 包中的类总是将相对路径名解析为当前用户目录。此目录由系统属性 <code>user.dir</code> 命名，通常是启动 Java 虚拟机的目录。
 *
 * <p> 通过调用本类的 {@link #getParent} 方法可以获取抽象路径名的 <em>父级</em>，它由路径名的前缀和路径名名称序列中的所有名称（最后一个除外）组成。每个目录的绝对路径名是任何具有以该目录的绝对路径名开头的绝对抽象路径名的 <tt>File</tt> 对象的祖先。例如，由抽象路径名 <tt>"/usr"</tt> 表示的目录是路径名 <tt>"/usr/local/bin"</tt> 表示的目录的祖先。
 *
 * <p> 前缀概念用于处理 UNIX 平台上的根目录，以及 Microsoft Windows 平台上的驱动器标识符、根目录和 UNC 路径名，如下所示：
 *
 * <ul>
 *
 * <li> 对于 UNIX 平台，绝对路径名的前缀始终是 <code>"/"</code>。相对路径名没有前缀。表示根目录的抽象路径名的前缀是 <code>"/"</code>，名称序列为空。
 *
 * <li> 对于 Microsoft Windows 平台，包含驱动器标识符的路径名的前缀由驱动器字母后跟 <code>":"</code> 组成，如果是绝对路径名，可能后跟 <code>"\\"</code>。UNC 路径名的前缀是 <code>"\\\\"</code>；主机名和共享名是名称序列中的前两个名称。不指定驱动器的相对路径名没有前缀。
 *
 * </ul>
 *
 * <p> 本类的实例可能表示也可能不表示实际的文件系统对象，如文件或目录。如果它表示这样的对象，则该对象位于一个 <i>分区</i> 中。分区是操作系统特定的文件系统存储部分。单个存储设备（例如物理磁盘驱动器、闪存、CD-ROM）可能包含多个分区。该对象，如果有的话，将位于由此路径名的绝对形式的某个祖先命名的分区 <a name="partName">命名</a>。
 *
 * <p> 文件系统可能对实际文件系统对象的某些操作实施限制，如读取、写入和执行。这些限制统称为 <i>访问权限</i>。文件系统可能在单个对象上有多组访问权限。例如，一组可能适用于对象的 <i>所有者</i>，另一组可能适用于所有其他用户。对象上的访问权限可能导致本类中的某些方法失败。
 *
 * <p> 本类的实例是不可变的；也就是说，一旦创建，<code>File</code> 对象表示的抽象路径名将永远不会改变。
 *
 * <h3>与 {@code java.nio.file} 包的互操作性</h3>
 *
 * <p> <a href="../../java/nio/file/package-summary.html">{@code java.nio.file}</a>
 * 包定义了接口和类，用于 Java 虚拟机访问文件、文件属性和文件系统。此 API 可用于克服 {@code java.io.File} 类的许多限制。
 * {@link #toPath toPath} 方法可用于获取一个 {@link
 * Path}，该路径使用 {@code File} 对象表示的抽象路径来定位文件。生成的 {@code Path} 可与 {@link
 * java.nio.file.Files} 类一起使用，以提供更高效和广泛的文件操作、文件属性访问和 I/O 异常，以帮助诊断文件操作失败时的错误。
 *
 * @author 未署名
 * @since JDK1.0
 */


public class File
    implements Serializable, Comparable<File>
{

    /**
     * 表示平台本地文件系统的 FileSystem 对象。
     */
    private static final FileSystem fs = DefaultFileSystem.getFileSystem();

    /**
     * 此抽象路径名的规范化路径名字符串。规范化路径名字符串使用默认的名称分隔符字符，且不包含任何重复或冗余的分隔符。
     *
     * @serial
     */
    private final String path;

    /**
     * 枚举类型，指示文件路径的状态。
     */
    private static enum PathStatus { INVALID, CHECKED };

    /**
     * 表示文件路径是否无效的标志。
     */
    private transient PathStatus status = null;

    /**
     * 检查文件是否有无效路径。目前，文件路径的检查非常有限，仅覆盖空字符检查，除非通过系统属性显式启用进一步检查。
     * 返回 true 表示路径肯定无效/垃圾，但返回 false 并不能保证路径有效。
     *
     * @return 如果文件路径无效则返回 true。
     */
    final boolean isInvalid() {
         PathStatus s = status;
        if (status == null) {
            s = fs.isInvalid(this) ? PathStatus.INVALID : PathStatus.CHECKED;
            status = s;
        }
        return status == PathStatus.INVALID;
    }

    /**
     * 此抽象路径名的前缀长度，如果没有前缀则为零。
     */
    private final transient int prefixLength;

    /**
     * 返回此抽象路径名的前缀长度。供 FileSystem 类使用。
     */
    int getPrefixLength() {
        return prefixLength;
    }

    /**
     * 系统依赖的默认名称分隔符字符。此字段初始化为包含系统属性 <code>file.separator</code> 的第一个字符。
     * 在 UNIX 系统上，此字段的值为 <code>'/'</code>；在 Microsoft Windows 系统上，其值为 <code>'\\'</code>。
     *
     * @see     java.lang.System#getProperty(java.lang.String)
     */
    public static final char separatorChar = fs.getSeparator();

    /**
     * 系统依赖的默认名称分隔符字符，为了方便表示为字符串。此字符串包含一个字符，即 <code>{@link #separatorChar}</code>。
     */
    public static final String separator = "" + separatorChar;

    /**
     * 系统依赖的路径分隔符字符。此字段初始化为包含系统属性 <code>path.separator</code> 的第一个字符。
     * 此字符用于在给定为 <em>路径列表</em> 的文件序列中分隔文件名。在 UNIX 系统上，此字符为 <code>':'</code>；
     * 在 Microsoft Windows 系统上，其值为 <code>';'</code>。
     *
     * @see     java.lang.System#getProperty(java.lang.String)
     */
    public static final char pathSeparatorChar = fs.getPathSeparator();

    /**
     * 系统依赖的路径分隔符字符，为了方便表示为字符串。此字符串包含一个字符，即 <code>{@link #pathSeparatorChar}</code>。
     */
    public static final String pathSeparator = "" + pathSeparatorChar;


    /* -- 构造函数 -- */

    /**
     * 已规范化路径名字符串的内部构造函数。
     */
    private File(String pathname, int prefixLength) {
        this.path = pathname;
        this.prefixLength = prefixLength;
    }

    /**
     * 已规范化路径名字符串的内部构造函数。
     * 参数顺序用于区分此方法与公共 (File, String) 构造函数。
     */
    private File(String child, File parent) {
        assert parent.path != null;
        assert (!parent.path.equals(""));
        this.path = fs.resolve(parent.path, child);
        this.prefixLength = parent.prefixLength;
    }

    /**
     * 通过将给定的路径名字符串转换为抽象路径名来创建新的 <code>File</code> 实例。
     * 如果给定的字符串为空字符串，则结果是空的抽象路径名。
     *
     * @param   pathname  路径名字符串
     * @throws  NullPointerException
     *          如果 <code>pathname</code> 参数为 <code>null</code>
     */
    public File(String pathname) {
        if (pathname == null) {
            throw new NullPointerException();
        }
        this.path = fs.normalize(pathname);
        this.prefixLength = fs.prefixLength(this.path);
    }

    /* 注意：两个参数的 File 构造函数不会将空的父抽象路径名解释为当前用户目录。
       而是将子路径名解析为由 FileSystem.getDefaultParent 方法定义的系统依赖目录。
       在 Unix 上，默认值为 "/"，而在 Microsoft Windows 上，默认值为 "\\"。
       这是为了与此类的原始行为兼容。 */

    /**
     * 从父路径名字符串和子路径名字符串创建新的 <code>File</code> 实例。
     *
     * <p> 如果 <code>parent</code> 为 <code>null</code>，则新的 <code>File</code> 实例将通过调用单参数 <code>File</code> 构造函数
     * 用给定的 <code>child</code> 路径名字符串创建。
     *
     * <p> 否则，<code>parent</code> 路径名字符串被视为表示目录，<code>child</code> 路径名字符串被视为表示目录或文件。
     * 如果 <code>child</code> 路径名字符串是绝对路径名，则将以系统依赖的方式将其转换为相对路径名。
     * 如果 <code>parent</code> 为空字符串，则新的 <code>File</code> 实例通过将 <code>child</code> 转换为抽象路径名并解析结果
     * 以系统依赖的默认目录来创建。否则，每个路径名字符串将转换为抽象路径名，并将子抽象路径名解析为父路径名。
     *
     * @param   parent  父路径名字符串
     * @param   child   子路径名字符串
     * @throws  NullPointerException
     *          如果 <code>child</code> 为 <code>null</code>
     */
    public File(String parent, String child) {
        if (child == null) {
            throw new NullPointerException();
        }
        if (parent != null) {
            if (parent.equals("")) {
                this.path = fs.resolve(fs.getDefaultParent(),
                                       fs.normalize(child));
            } else {
                this.path = fs.resolve(fs.normalize(parent),
                                       fs.normalize(child));
            }
        } else {
            this.path = fs.normalize(child);
        }
        this.prefixLength = fs.prefixLength(this.path);
    }

                /**
     * 从父抽象路径名和子路径字符串创建新的 <code>File</code> 实例。
     *
     * <p> 如果 <code>parent</code> 为 <code>null</code>，则新的 <code>File</code> 实例将通过调用单参数的 <code>File</code> 构造函数，使用给定的 <code>child</code> 路径字符串创建。
     *
     * <p> 否则，<code>parent</code> 抽象路径名被视为目录，<code>child</code> 路径字符串被视为目录或文件。如果 <code>child</code> 路径字符串是绝对路径，则将以系统依赖的方式将其转换为相对路径。如果 <code>parent</code> 是空的抽象路径名，则通过将 <code>child</code> 转换为抽象路径名并解析结果以系统依赖的默认目录来创建新的 <code>File</code> 实例。否则，每个路径字符串将转换为抽象路径名，并将子抽象路径名解析为父路径名。
     *
     * @param   parent  父抽象路径名
     * @param   child   子路径字符串
     * @throws  NullPointerException
     *          如果 <code>child</code> 为 <code>null</code>
     */
    public File(File parent, String child) {
        if (child == null) {
            throw new NullPointerException();
        }
        if (parent != null) {
            if (parent.path.equals("")) {
                this.path = fs.resolve(fs.getDefaultParent(),
                                       fs.normalize(child));
            } else {
                this.path = fs.resolve(parent.path,
                                       fs.normalize(child));
            }
        } else {
            this.path = fs.normalize(child);
        }
        this.prefixLength = fs.prefixLength(this.path);
    }

    /**
     * 通过将给定的 <tt>file:</tt> URI 转换为抽象路径名来创建新的 <tt>File</tt> 实例。
     *
     * <p> <tt>file:</tt> URI 的确切形式是系统依赖的，因此此构造函数执行的转换也是系统依赖的。
     *
     * <p> 对于给定的抽象路径名 <i>f</i>，保证
     *
     * <blockquote><tt>
     * new File(</tt><i>&nbsp;f</i><tt>.{@link #toURI() toURI}()).equals(</tt><i>&nbsp;f</i><tt>.{@link #getAbsoluteFile() getAbsoluteFile}())
     * </tt></blockquote>
     *
     * 只要原始抽象路径名、URI 和新的抽象路径名都是在（可能不同的）同一个 Java 虚拟机中创建的。然而，当在一个操作系统的虚拟机中创建的 <tt>file:</tt> URI 被转换为另一个操作系统的虚拟机中的抽象路径名时，这种关系通常不成立。
     *
     * @param  uri
     *         一个绝对的、分层的 URI，其方案等于 <tt>"file"</tt>，具有非空的路径组件，且权威、查询和片段组件未定义
     *
     * @throws  NullPointerException
     *          如果 <tt>uri</tt> 为 <tt>null</tt>
     *
     * @throws  IllegalArgumentException
     *          如果参数的前置条件不满足
     *
     * @see #toURI()
     * @see java.net.URI
     * @since 1.4
     */
    public File(URI uri) {

        // 检查许多前置条件
        if (!uri.isAbsolute())
            throw new IllegalArgumentException("URI is not absolute");
        if (uri.isOpaque())
            throw new IllegalArgumentException("URI is not hierarchical");
        String scheme = uri.getScheme();
        if ((scheme == null) || !scheme.equalsIgnoreCase("file"))
            throw new IllegalArgumentException("URI scheme is not \"file\"");
        if (uri.getAuthority() != null)
            throw new IllegalArgumentException("URI has an authority component");
        if (uri.getFragment() != null)
            throw new IllegalArgumentException("URI has a fragment component");
        if (uri.getQuery() != null)
            throw new IllegalArgumentException("URI has a query component");
        String p = uri.getPath();
        if (p.equals(""))
            throw new IllegalArgumentException("URI path component is empty");

        // 好了，现在初始化
        p = fs.fromURIPath(p);
        if (File.separatorChar != '/')
            p = p.replace('/', File.separatorChar);
        this.path = fs.normalize(p);
        this.prefixLength = fs.prefixLength(this.path);
    }


    /* -- 路径组件访问器 -- */

    /**
     * 返回此抽象路径名表示的文件或目录的名称。这仅仅是路径名名称序列中的最后一个名称。如果路径名的名称序列为空，则返回空字符串。
     *
     * @return  此抽象路径名表示的文件或目录的名称，或者如果此路径名的名称序列为空，则返回空字符串
     */
    public String getName() {
        int index = path.lastIndexOf(separatorChar);
        if (index < prefixLength) return path.substring(prefixLength);
        return path.substring(index + 1);
    }

    /**
     * 返回此抽象路径名的父路径名字符串，如果此路径名不命名父目录，则返回 <code>null</code>。
     *
     * <p> 抽象路径名的 <em>父</em> 由路径名的前缀（如果有）和路径名名称序列中的所有名称组成，但最后一个除外。如果名称序列为空，则路径名不命名父目录。
     *
     * @return  此抽象路径名命名的父目录的路径名字符串，或者如果此路径名不命名父目录，则返回 <code>null</code>
     */
    public String getParent() {
        int index = path.lastIndexOf(separatorChar);
        if (index < prefixLength) {
            if ((prefixLength > 0) && (path.length() > prefixLength))
                return path.substring(0, prefixLength);
            return null;
        }
        return path.substring(0, index);
    }

                /**
     * 返回此抽象路径名的父路径名，如果此路径名不命名父目录，则返回 <code>null</code>。
     *
     * <p> 抽象路径名的 <em>父级</em> 由路径名的前缀（如果有）和路径名名称序列中的每个名称（除了最后一个）组成。如果名称序列为空，则路径名不命名父目录。
     *
     * @return  由此抽象路径名命名的父目录的抽象路径名，如果此路径名不命名父目录，则返回 <code>null</code>
     *
     * @since 1.2
     */
    public File getParentFile() {
        String p = this.getParent();
        if (p == null) return null;
        if (getClass() != File.class) {
            p = fs.normalize(p);
        }
        return new File(p, this.prefixLength);
    }

    /**
     * 将此抽象路径名转换为路径字符串。结果字符串使用 {@link #separator 默认名称分隔符字符} 来分隔名称序列中的名称。
     *
     * @return  此抽象路径名的字符串形式
     */
    public String getPath() {
        return path;
    }


    /* -- 路径操作 -- */

    /**
     * 测试此抽象路径名是否为绝对路径名。绝对路径名的定义是系统依赖的。在 UNIX 系统上，如果路径名的前缀是 <code>"/"</code>，则路径名是绝对的。在 Microsoft Windows 系统上，如果路径名的前缀是驱动器标识符后跟 <code>"\"</code>，或者前缀是 <code>"\\\"</code>，则路径名是绝对的。
     *
     * @return  如果此抽象路径名是绝对的，则返回 <code>true</code>，否则返回 <code>false</code>
     */
    public boolean isAbsolute() {
        return fs.isAbsolute(this);
    }

    /**
     * 返回此抽象路径名的绝对路径字符串。
     *
     * <p> 如果此抽象路径名已经是绝对的，那么路径字符串将简单地通过 <code>{@link #getPath}</code> 方法返回。如果此抽象路径名是空的抽象路径名，则返回当前用户目录的路径字符串，该路径字符串由系统属性 <code>user.dir</code> 命名。否则，此路径名将以系统依赖的方式解析。在 UNIX 系统上，相对路径名通过解析当前用户目录来变为绝对路径。在 Microsoft Windows 系统上，相对路径名通过解析路径名命名的驱动器的当前目录来变为绝对路径（如果有）；如果没有，它将解析当前用户目录。
     *
     * @return  表示与此抽象路径名相同的文件或目录的绝对路径字符串
     *
     * @throws  SecurityException
     *          如果无法访问所需的系统属性值。
     *
     * @see     java.io.File#isAbsolute()
     */
    public String getAbsolutePath() {
        return fs.resolve(this);
    }

    /**
     * 返回此抽象路径名的绝对形式。等同于 <code>new&nbsp;File(this.{@link #getAbsolutePath})</code>。
     *
     * @return  表示与此抽象路径名相同的文件或目录的绝对抽象路径名
     *
     * @throws  SecurityException
     *          如果无法访问所需的系统属性值。
     *
     * @since 1.2
     */
    public File getAbsoluteFile() {
        String absPath = getAbsolutePath();
        if (getClass() != File.class) {
            absPath = fs.normalize(absPath);
        }
        return new File(absPath, fs.prefixLength(absPath));
    }

    /**
     * 返回此抽象路径名的规范路径字符串。
     *
     * <p> 规范路径名既是绝对的也是唯一的。规范形式的精确定义是系统依赖的。此方法首先将此路径名转换为绝对形式（如果必要的话），就像调用 {@link #getAbsolutePath} 方法一样，然后以系统依赖的方式将其映射到其唯一形式。这通常涉及从路径名中删除冗余名称（如 <tt>"."</tt> 和 <tt>".."</tt>），解析符号链接（在 UNIX 平台上），以及将驱动器字母转换为标准大小写（在 Microsoft Windows 平台上）。
     *
     * <p> 每个表示现有文件或目录的路径名都有一个唯一的规范形式。每个表示不存在文件或目录的路径名也有一个唯一的规范形式。不存在文件或目录的路径名的规范形式可能与创建该文件或目录后的相同路径名的规范形式不同。同样，现有文件或目录的路径名的规范形式可能与删除该文件或目录后的相同路径名的规范形式不同。
     *
     * @return  表示与此抽象路径名相同的文件或目录的规范路径字符串
     *
     * @throws  IOException
     *          如果发生 I/O 错误，这是可能的，因为构造规范路径名可能需要文件系统查询
     *
     * @throws  SecurityException
     *          如果无法访问所需的系统属性值，或者存在安全管理器并且其 <code>{@link
     *          java.lang.SecurityManager#checkRead}</code> 方法拒绝读取文件的访问权限
     *
     * @since   JDK1.1
     * @see     Path#toRealPath
     */
    public String getCanonicalPath() throws IOException {
        if (isInvalid()) {
            throw new IOException("Invalid file path");
        }
        return fs.canonicalize(fs.resolve(this));
    }

    /**
     * 返回此抽象路径名的规范形式。等同于 <code>new&nbsp;File(this.{@link #getCanonicalPath})</code>。
     *
     * @return  表示与此抽象路径名相同的文件或目录的规范路径字符串
     *
     * @throws  IOException
     *          如果发生 I/O 错误，这是可能的，因为构造规范路径名可能需要文件系统查询
     *
     * @throws  SecurityException
     *          如果无法访问所需的系统属性值，或者存在安全管理器并且其 <code>{@link
     *          java.lang.SecurityManager#checkRead}</code> 方法拒绝读取文件的访问权限
     *
     * @since 1.2
     * @see     Path#toRealPath
     */
    public File getCanonicalFile() throws IOException {
        String canonPath = getCanonicalPath();
        if (getClass() != File.class) {
            canonPath = fs.normalize(canonPath);
        }
        return new File(canonPath, fs.prefixLength(canonPath));
    }


                private static String slashify(String path, boolean isDirectory) {
        String p = path;
        if (File.separatorChar != '/')
            p = p.replace(File.separatorChar, '/');
        if (!p.startsWith("/"))
            p = "/" + p;
        if (!p.endsWith("/") && isDirectory)
            p = p + "/";
        return p;
    }

    /**
     * 将此抽象路径名转换为 <code>file:</code> URL。URL 的确切形式是系统相关的。如果可以确定此抽象路径名表示的文件是一个目录，则生成的 URL 将以斜杠结尾。
     *
     * @return  表示等效文件 URL 的 URL 对象
     *
     * @throws  MalformedURLException
     *          如果路径不能解析为 URL
     *
     * @see     #toURI()
     * @see     java.net.URI
     * @see     java.net.URI#toURL()
     * @see     java.net.URL
     * @since   1.2
     *
     * @deprecated 此方法不会自动转义在 URL 中非法的字符。建议新代码通过首先将抽象路径名转换为 URI（使用 {@link #toURI() toURI} 方法），然后通过 {@link java.net.URI#toURL() URI.toURL} 方法将 URI 转换为 URL。
     */
    @Deprecated
    public URL toURL() throws MalformedURLException {
        if (isInvalid()) {
            throw new MalformedURLException("Invalid file path");
        }
        return new URL("file", "", slashify(getAbsolutePath(), isDirectory()));
    }

    /**
     * 构造一个表示此抽象路径名的 <tt>file:</tt> URI。
     *
     * <p> URI 的确切形式是系统相关的。如果可以确定此抽象路径名表示的文件是一个目录，则生成的 URI 将以斜杠结尾。
     *
     * <p> 对于给定的抽象路径名 <i>f</i>，保证
     *
     * <blockquote><tt>
     * new {@link #File(java.net.URI) File}(</tt><i>&nbsp;f</i><tt>.toURI()).equals(</tt><i>&nbsp;f</i><tt>.{@link #getAbsoluteFile() getAbsoluteFile}())
     * </tt></blockquote>
     *
     * 只要原始抽象路径名、URI 和新的抽象路径名都是在（可能不同的）同一个 Java 虚拟机中创建的。然而，由于抽象路径名的系统相关性，当在不同操作系统的虚拟机中将 <tt>file:</tt> URI 转换为抽象路径名时，这种关系通常不成立。
     *
     * <p> 注意，当此抽象路径名表示一个 UNC 路径名时，UNC 的所有组件（包括服务器名称组件）都编码在 {@code URI} 路径中。权威组件未定义，表示为 {@code null}。{@link Path} 类定义了 {@link Path#toUri toUri} 方法，以将服务器名称编码在生成的 {@code URI} 的权威组件中。可以使用 {@link #toPath toPath} 方法获取表示此抽象路径名的 {@code Path}。
     *
     * @return  一个绝对的、分层的 URI，其方案等于 <tt>"file"</tt>，路径表示此抽象路径名，权威、查询和片段组件未定义
     * @throws SecurityException 如果无法访问所需的系统属性值。
     *
     * @see #File(java.net.URI)
     * @see java.net.URI
     * @see java.net.URI#toURL()
     * @since 1.4
     */
    public URI toURI() {
        try {
            File f = getAbsoluteFile();
            String sp = slashify(f.getPath(), f.isDirectory());
            if (sp.startsWith("//"))
                sp = "//" + sp;
            return new URI("file", null, sp, null);
        } catch (URISyntaxException x) {
            throw new Error(x);         // 不可能发生
        }
    }


    /* -- 属性访问器 -- */

    /**
     * 测试应用程序是否可以读取此抽象路径名表示的文件。在某些平台上，可能可以以特殊权限启动 Java 虚拟机，使其能够读取标记为不可读的文件。因此，即使文件没有读权限，此方法也可能返回 {@code true}。
     *
     * @return  <code>true</code> 如果且仅当此抽象路径名指定的文件存在 <em>并且</em> 可以被应用程序读取；否则返回 <code>false</code>
     *
     * @throws  SecurityException
     *          如果存在安全管理器且其 <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
     *          方法拒绝读取文件的权限
     */
    public boolean canRead() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        if (isInvalid()) {
            return false;
        }
        return fs.checkAccess(this, FileSystem.ACCESS_READ);
    }

    /**
     * 测试应用程序是否可以修改此抽象路径名表示的文件。在某些平台上，可能可以以特殊权限启动 Java 虚拟机，使其能够修改标记为只读的文件。因此，即使文件被标记为只读，此方法也可能返回 {@code true}。
     *
     * @return  <code>true</code> 如果且仅当文件系统实际包含此抽象路径名表示的文件 <em>并且</em> 应用程序可以写入文件；否则返回 <code>false</code>。
     *
     * @throws  SecurityException
     *          如果存在安全管理器且其 <code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
     *          方法拒绝写入文件的权限
     */
    public boolean canWrite() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        if (isInvalid()) {
            return false;
        }
        return fs.checkAccess(this, FileSystem.ACCESS_WRITE);
    }

                /**
     * 测试由此抽象路径名表示的文件或目录是否存在。
     *
     * @return  如果且仅当由该抽象路径名表示的文件或目录存在时返回 <code>true</code>；否则返回 <code>false</code>
     *
     * @throws  SecurityException
     *          如果存在安全管理者，并且其 <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
     *          方法拒绝读取文件或目录的权限
     */
    public boolean exists() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        if (isInvalid()) {
            return false;
        }
        return ((fs.getBooleanAttributes(this) & FileSystem.BA_EXISTS) != 0);
    }

    /**
     * 测试由该抽象路径名表示的文件是否为目录。
     *
     * <p> 如果需要区分 I/O 异常与文件不是目录的情况，或者需要同时获取同一文件的多个属性，则可以使用 {@link
     * java.nio.file.Files#readAttributes(Path,Class,LinkOption[])
     * Files.readAttributes} 方法。
     *
     * @return <code>true</code> 如果且仅当由该抽象路径名表示的文件存在 <em>并且</em> 是目录；
     *          否则返回 <code>false</code>
     *
     * @throws  SecurityException
     *          如果存在安全管理者，并且其 <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
     *          方法拒绝读取文件的权限
     */
    public boolean isDirectory() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        if (isInvalid()) {
            return false;
        }
        return ((fs.getBooleanAttributes(this) & FileSystem.BA_DIRECTORY)
                != 0);
    }

    /**
     * 测试由该抽象路径名表示的文件是否为普通文件。文件 <em>普通</em> 如果它不是目录，并且满足其他系统依赖的条件。任何由 Java 应用程序创建的非目录文件都保证是普通文件。
     *
     * <p> 如果需要区分 I/O 异常与文件不是普通文件的情况，或者需要同时获取同一文件的多个属性，则可以使用 {@link
     * java.nio.file.Files#readAttributes(Path,Class,LinkOption[])
     * Files.readAttributes} 方法。
     *
     * @return  <code>true</code> 如果且仅当由该抽象路径名表示的文件存在 <em>并且</em> 是普通文件；
     *          否则返回 <code>false</code>
     *
     * @throws  SecurityException
     *          如果存在安全管理者，并且其 <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
     *          方法拒绝读取文件的权限
     */
    public boolean isFile() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        if (isInvalid()) {
            return false;
        }
        return ((fs.getBooleanAttributes(this) & FileSystem.BA_REGULAR) != 0);
    }

    /**
     * 测试由该抽象路径名表示的文件是否为隐藏文件。隐藏文件的确切定义是系统依赖的。在 UNIX 系统上，如果文件名以点字符 (<code>'.'</code>) 开头，则认为该文件是隐藏的。在 Microsoft Windows 系统上，如果文件在文件系统中标记为隐藏，则认为该文件是隐藏的。
     *
     * @return  <code>true</code> 如果且仅当由该抽象路径名表示的文件根据底层平台的约定是隐藏的
     *
     * @throws  SecurityException
     *          如果存在安全管理者，并且其 <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
     *          方法拒绝读取文件的权限
     *
     * @since 1.2
     */
    public boolean isHidden() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        if (isInvalid()) {
            return false;
        }
        return ((fs.getBooleanAttributes(this) & FileSystem.BA_HIDDEN) != 0);
    }

    /**
     * 返回由该抽象路径名表示的文件的最后修改时间。
     *
     * <p> 如果需要区分 I/O 异常与返回 {@code 0L} 的情况，或者需要同时获取同一文件的多个属性，或者需要最后访问时间或创建时间，则可以使用 {@link
     * java.nio.file.Files#readAttributes(Path,Class,LinkOption[])
     * Files.readAttributes} 方法。
     *
     * @return  一个 <code>long</code> 值，表示文件最后修改的时间，以自纪元（1970年1月1日00:00:00 GMT）以来的毫秒数表示，如果文件不存在或发生 I/O 错误，则返回 <code>0L</code>
     *
     * @throws  SecurityException
     *          如果存在安全管理者，并且其 <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
     *          方法拒绝读取文件的权限
     */
    public long lastModified() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        if (isInvalid()) {
            return 0L;
        }
        return fs.getLastModifiedTime(this);
    }

    /**
     * 返回由该抽象路径名表示的文件的长度。如果该路径名表示的是目录，则返回值未指定。
     *
     * <p> 如果需要区分 I/O 异常与返回 {@code 0L} 的情况，或者需要同时获取同一文件的多个属性，则可以使用 {@link
     * java.nio.file.Files#readAttributes(Path,Class,LinkOption[])
     * Files.readAttributes} 方法。
     *
     * @return  由该抽象路径名表示的文件的长度（以字节为单位），如果文件不存在，则返回 <code>0L</code>。某些操作系统可能为表示系统依赖实体（如设备或管道）的路径名返回 <code>0L</code>。
     *
     * @throws  SecurityException
     *          如果存在安全管理者，并且其 <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
     *          方法拒绝读取文件的权限
     */
    public long length() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        if (isInvalid()) {
            return 0L;
        }
        return fs.getLength(this);
    }

    /* -- 文件操作 -- */

    /**
     * 如果且仅当由该抽象路径名命名的文件尚不存在时，原子地创建一个新的空文件。
     * 检查文件是否存在以及如果文件不存在则创建文件的操作是单个操作，相对于所有可能影响文件的文件系统活动都是原子的。
     * <P>
     * 注意：此方法不应用于文件锁定，因为由此产生的协议无法可靠地工作。应改用 {@link java.nio.channels.FileLock FileLock} 设施。
     *
     * @return  如果命名的文件不存在且成功创建，则返回 <code>true</code>；如果命名的文件已存在，则返回 <code>false</code>
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  SecurityException
     *          如果存在安全管理者且其 <code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
     *          方法拒绝对文件的写入访问
     *
     * @since 1.2
     */
    public boolean createNewFile() throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) security.checkWrite(path);
        if (isInvalid()) {
            throw new IOException("Invalid file path");
        }
        return fs.createFileExclusively(path);
    }

    /**
     * 删除由该抽象路径名表示的文件或目录。如果此路径名表示一个目录，则该目录必须为空才能被删除。
     *
     * <p> 注意，{@link java.nio.file.Files} 类定义了 {@link
     * java.nio.file.Files#delete(Path) delete} 方法，当文件无法删除时会抛出 {@link IOException}。
     * 这对于错误报告和诊断文件为何无法删除非常有用。
     *
     * @return  如果且仅当文件或目录成功被删除时返回 <code>true</code>；否则返回 <code>false</code>
     *
     * @throws  SecurityException
     *          如果存在安全管理者且其 <code>{@link
     *          java.lang.SecurityManager#checkDelete}</code> 方法拒绝删除文件的访问
     */
    public boolean delete() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkDelete(path);
        }
        if (isInvalid()) {
            return false;
        }
        return fs.delete(this);
    }

    /**
     * 请求在虚拟机终止时删除由该抽象路径名表示的文件或目录。
     * 文件（或目录）将按它们注册的相反顺序删除。如果文件或目录已注册为删除，则调用此方法不会产生任何效果。
     * 仅在虚拟机正常终止时才会尝试删除，如 Java 语言规范所定义。
     *
     * <p> 一旦请求删除，就无法取消请求。因此，应谨慎使用此方法。
     *
     * <P>
     * 注意：此方法不应用于文件锁定，因为由此产生的协议无法可靠地工作。应改用 {@link java.nio.channels.FileLock FileLock} 设施。
     *
     * @throws  SecurityException
     *          如果存在安全管理者且其 <code>{@link
     *          java.lang.SecurityManager#checkDelete}</code> 方法拒绝删除文件的访问
     *
     * @see #delete
     *
     * @since 1.2
     */
    public void deleteOnExit() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkDelete(path);
        }
        if (isInvalid()) {
            return;
        }
        DeleteOnExitHook.add(path);
    }

    /**
     * 返回一个字符串数组，命名由该抽象路径名表示的目录中的文件和目录。确保字符串表示规范化路径。
     *
     * @return  一个字符串数组，命名由该抽象路径名表示的目录中的文件和目录。如果目录为空，则数组为空。如果此抽象路径名不表示目录，或发生 I/O 错误，则返回 {@code null}。
     *
     * @throws  SecurityException
     *          如果存在安全管理者且其 {@link
     *          SecurityManager#checkRead(String)} 方法拒绝对目录的读取访问
     */
    private final String[] normalizedList() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        if (isInvalid()) {
            return null;
        }
        String[] s = fs.list(this);
        if (s != null && getClass() != File.class) {
            String[] normalized = new String[s.length];
            for (int i = 0; i < s.length; i++) {
                normalized[i] = fs.normalize(s[i]);
            }
            s = normalized;
        }
        return s;
    }

    /**
     * 返回一个字符串数组，命名由该抽象路径名表示的目录中的文件和目录。
     *
     * <p> 如果此抽象路径名不表示目录，则此方法返回 {@code null}。否则返回一个字符串数组，每个文件或目录一个。不包括表示目录本身的名称和目录的父目录的名称。每个字符串都是文件名，而不是完整路径。
     *
     * <p> 结果数组中的名称字符串不保证以任何特定顺序出现；特别是，它们不保证按字母顺序出现。
     *
     * <p> 注意，{@link java.nio.file.Files} 类定义了 {@link
     * java.nio.file.Files#newDirectoryStream(Path) newDirectoryStream} 方法来打开目录并迭代目录中的文件名。
     * 这在处理非常大的目录时可能使用较少的资源，并且在处理远程目录时可能更响应。
     *
     * @return  一个字符串数组，命名由该抽象路径名表示的目录中的文件和目录。如果目录为空，则数组为空。如果此抽象路径名不表示目录，或发生 I/O 错误，则返回 {@code null}。
     *
     * @throws  SecurityException
     *          如果存在安全管理者且其 {@link
     *          SecurityManager#checkRead(String)} 方法拒绝对目录的读取访问
     */
    public String[] list() {
        return normalizedList();
    }

                /**
     * 返回一个字符串数组，命名由该抽象路径名表示的目录中的文件和目录，这些文件和目录满足指定的过滤器。此方法的行为与 {@link #list()} 方法相同，
     * 但返回的数组中的字符串必须满足过滤器。如果给定的 {@code filter} 为 {@code null}，则接受所有名称。否则，名称只有在调用过滤器的
     * {@link FilenameFilter#accept FilenameFilter.accept(File,&nbsp;String)} 方法时返回 {@code true} 值，才满足过滤器。
     *
     * @param  filter
     *         文件名过滤器
     *
     * @return  一个字符串数组，命名由该抽象路径名表示的目录中被给定 {@code filter} 接受的文件和目录。如果目录为空或没有名称被过滤器接受，则数组为空。
     *          如果该抽象路径名不表示目录，或发生 I/O 错误，则返回 {@code null}。
     *
     * @throws  SecurityException
     *          如果存在安全经理，并且其 {@link SecurityManager#checkRead(String)} 方法拒绝读取目录的权限
     *
     * @see java.nio.file.Files#newDirectoryStream(Path,String)
     */
    public String[] list(FilenameFilter filter) {
        String names[] = normalizedList();
        if ((names == null) || (filter == null)) {
            return names;
        }
        List<String> v = new ArrayList<>();
        for (int i = 0 ; i < names.length ; i++) {
            if (filter.accept(this, names[i])) {
                v.add(names[i]);
            }
        }
        return v.toArray(new String[v.size()]);
    }

    /**
     * 返回一个抽象路径名数组，表示由该抽象路径名表示的目录中的文件。
     *
     * <p> 如果该抽象路径名不表示目录，则此方法返回 {@code null}。否则，返回一个 {@code File} 对象数组，每个文件或目录一个。不包括表示目录本身和目录父目录的路径名。
     * 每个结果的抽象路径名都是通过使用 {@link #File(File, String) File(File,&nbsp;String)} 构造函数从该抽象路径名构造的。因此，如果该路径名是绝对的，则每个结果路径名也是绝对的；
     * 如果该路径名是相对的，则每个结果路径名将相对于同一目录。
     *
     * <p> 结果数组中的名称字符串不保证以任何特定顺序出现；特别是，它们不保证按字母顺序出现。
     *
     * <p> 注意，{@link java.nio.file.Files} 类定义了 {@link java.nio.file.Files#newDirectoryStream(Path) newDirectoryStream} 方法
     * 以打开目录并迭代目录中的文件名。这在处理非常大的目录时可能使用较少的资源。
     *
     * @return  一个抽象路径名数组，表示由该抽象路径名表示的目录中的文件和目录。如果目录为空，则数组为空。如果该抽象路径名不表示目录，或发生 I/O 错误，则返回 {@code null}。
     *
     * @throws  SecurityException
     *          如果存在安全经理，并且其 {@link SecurityManager#checkRead(String)} 方法拒绝读取目录的权限
     *
     * @since  1.2
     */
    public File[] listFiles() {
        String[] ss = normalizedList();
        if (ss == null) return null;
        int n = ss.length;
        File[] fs = new File[n];
        for (int i = 0; i < n; i++) {
            fs[i] = new File(ss[i], this);
        }
        return fs;
    }

    /**
     * 返回一个抽象路径名数组，表示由该抽象路径名表示的目录中满足指定过滤器的文件和目录。此方法的行为与 {@link #listFiles()} 方法相同，
     * 但返回的数组中的路径名必须满足过滤器。如果给定的 {@code filter} 为 {@code null}，则接受所有路径名。否则，路径名只有在调用过滤器的
     * {@link FilenameFilter#accept FilenameFilter.accept(File,&nbsp;String)} 方法时返回 {@code true} 值，才满足过滤器。
     *
     * @param  filter
     *         文件名过滤器
     *
     * @return  一个抽象路径名数组，表示由该抽象路径名表示的目录中的文件和目录。如果目录为空，则数组为空。如果该抽象路径名不表示目录，或发生 I/O 错误，则返回 {@code null}。
     *
     * @throws  SecurityException
     *          如果存在安全经理，并且其 {@link SecurityManager#checkRead(String)} 方法拒绝读取目录的权限
     *
     * @since  1.2
     * @see java.nio.file.Files#newDirectoryStream(Path,String)
     */
    public File[] listFiles(FilenameFilter filter) {
        String ss[] = normalizedList();
        if (ss == null) return null;
        ArrayList<File> files = new ArrayList<>();
        for (String s : ss)
            if ((filter == null) || filter.accept(this, s))
                files.add(new File(s, this));
        return files.toArray(new File[files.size()]);
    }


/**
 * 返回一个抽象路径名数组，表示由此抽象路径名表示的目录中的文件和目录，
 * 这些文件和目录满足指定的过滤器。此方法的行为与 {@link #listFiles()} 方法相同，
 * 但返回的数组中的路径名必须满足过滤器。如果给定的 {@code filter} 为 {@code null}，
 * 则所有路径名都将被接受。否则，路径名只有在调用过滤器的
 * {@link FileFilter#accept FileFilter.accept(File)} 方法时返回 {@code true} 值时才满足过滤器。
 *
 * @param  filter
 *         文件过滤器
 *
 * @return  一个抽象路径名数组，表示由该抽象路径名表示的目录中的文件和目录。
 *          如果目录为空，则数组将为空。如果此抽象路径名不表示目录，或者发生 I/O 错误，则返回
 *          {@code null}。
 *
 * @throws  SecurityException
 *          如果存在安全经理，并且其 {@link
 *          SecurityManager#checkRead(String)} 方法拒绝读取目录的权限
 *
 * @since  1.2
 * @see java.nio.file.Files#newDirectoryStream(Path,java.nio.file.DirectoryStream.Filter)
 */
public File[] listFiles(FileFilter filter) {
    String ss[] = normalizedList();
    if (ss == null) return null;
    ArrayList<File> files = new ArrayList<>();
    for (String s : ss) {
        File f = new File(s, this);
        if ((filter == null) || filter.accept(f))
            files.add(f);
    }
    return files.toArray(new File[files.size()]);
}

/**
 * 创建由该抽象路径名表示的目录。
 *
 * @return  <code>true</code> 如果且仅当目录被创建；<code>false</code> 否则
 *
 * @throws  SecurityException
 *          如果存在安全经理，并且其 <code>{@link
 *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
 *          方法不允许创建命名的目录
 */
public boolean mkdir() {
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
        security.checkWrite(path);
    }
    if (isInvalid()) {
        return false;
    }
    return fs.createDirectory(this);
}

/**
 * 创建由该抽象路径名表示的目录，包括所有必要的但不存在的父目录。注意，如果此操作失败，
 * 它可能已经成功创建了一些必要的父目录。
 *
 * @return  <code>true</code> 如果且仅当目录被创建，包括所有必要的父目录；<code>false</code> 否则
 *
 * @throws  SecurityException
 *          如果存在安全经理，并且其 <code>{@link
 *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
 *          方法不允许验证命名的目录和所有必要父目录的存在；或者其 <code>{@link
 *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
 *          方法不允许创建命名的目录和所有必要父目录
 */
public boolean mkdirs() {
    if (exists()) {
        return false;
    }
    if (mkdir()) {
        return true;
    }
    File canonFile = null;
    try {
        canonFile = getCanonicalFile();
    } catch (IOException e) {
        return false;
    }

    File parent = canonFile.getParentFile();
    return (parent != null && (parent.mkdirs() || parent.exists()) &&
            canonFile.mkdir());
}

/**
 * 重命名由该抽象路径名表示的文件。
 *
 * <p> 此方法的行为在许多方面是平台相关的：重命名操作可能无法将文件从一个文件系统移动到另一个文件系统，
 * 它可能不是原子的，如果目标抽象路径名已经存在，它可能不会成功。应始终检查返回值以确保重命名操作成功。
 *
 * <p> 注意，{@link java.nio.file.Files} 类定义了 {@link
 * java.nio.file.Files#move move} 方法，以平台无关的方式移动或重命名文件。
 *
 * @param  dest  命名文件的新抽象路径名
 *
 * @return  <code>true</code> 如果且仅当重命名成功；<code>false</code> 否则
 *
 * @throws  SecurityException
 *          如果存在安全经理，并且其 <code>{@link
 *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
 *          方法拒绝对旧路径名或新路径名的写入权限
 *
 * @throws  NullPointerException
 *          如果参数 <code>dest</code> 为 <code>null</code>
 */
public boolean renameTo(File dest) {
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
        security.checkWrite(path);
        security.checkWrite(dest.path);
    }
    if (dest == null) {
        throw new NullPointerException();
    }
    if (this.isInvalid() || dest.isInvalid()) {
        return false;
    }
    return fs.rename(this, dest);
}

/**
 * 设置由该抽象路径名表示的文件或目录的最后修改时间。
 *
 * <p> 所有平台都支持以秒为单位的文件修改时间，但有些平台提供更高的精度。参数将被截断以适应支持的精度。
 * 如果操作成功且文件上没有进行任何其他操作，则下一次调用 <code>{@link #lastModified}</code> 方法
 * 将返回传递给此方法的（可能被截断的）<code>time</code> 参数。
 *
 * @param  time  新的最后修改时间，以自纪元（1970年1月1日00:00:00 GMT）以来的毫秒数表示
 *
 * @return <code>true</code> 如果且仅当操作成功；<code>false</code> 否则
 *
 * @throws  IllegalArgumentException  如果参数为负数
 *
 * @throws  SecurityException
 *          如果存在安全经理，并且其 <code>{@link
 *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
 *          方法拒绝对命名文件的写入权限
 *
 * @since 1.2
 */
public boolean setLastModified(long time) {
    if (time < 0) throw new IllegalArgumentException("Negative time");
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
        security.checkWrite(path);
    }
    if (isInvalid()) {
        return false;
    }
    return fs.setLastModifiedTime(this, time);
}

                /**
     * 标记此抽象路径名命名的文件或目录，使其仅允许读操作。调用此方法后，文件或目录将不会改变，直到它被删除或标记为允许写入访问。在某些平台上，可能可以使用特殊权限启动Java虚拟机，使其能够修改标记为只读的文件。文件或目录是否可以被删除取决于底层系统。
     *
     * @return 如果且仅当操作成功时返回<code>true</code>；否则返回<code>false</code>
     *
     * @throws  SecurityException
     *          如果存在安全经理，且其<code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
     *          方法拒绝对命名文件的写访问
     *
     * @since 1.2
     */
    public boolean setReadOnly() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        if (isInvalid()) {
            return false;
        }
        return fs.setReadOnly(this);
    }

    /**
     * 设置此抽象路径名的拥有者或所有人的写权限。在某些平台上，可能可以使用特殊权限启动Java虚拟机，使其能够修改禁止写操作的文件。
     *
     * <p> {@link java.nio.file.Files}类定义了操作文件属性（包括文件权限）的方法。当需要更精细地操作文件权限时，可以使用这些方法。
     *
     * @param   writable
     *          如果<code>true</code>，设置访问权限以允许写操作；如果<code>false</code>，禁止写操作
     *
     * @param   ownerOnly
     *          如果<code>true</code>，写权限仅适用于拥有者的写权限；否则，适用于所有人。如果底层文件系统不能区分拥有者的写权限和其他人的写权限，那么权限将适用于所有人，无论此值如何。
     *
     * @return  如果且仅当操作成功时返回<code>true</code>。如果用户没有权限更改此抽象路径名的访问权限，则操作将失败。
     *
     * @throws  SecurityException
     *          如果存在安全经理，且其<code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
     *          方法拒绝对命名文件的写访问
     *
     * @since 1.6
     */
    public boolean setWritable(boolean writable, boolean ownerOnly) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        if (isInvalid()) {
            return false;
        }
        return fs.setPermission(this, FileSystem.ACCESS_WRITE, writable, ownerOnly);
    }

    /**
     * 一个方便的方法，用于设置此抽象路径名的拥有者的写权限。在某些平台上，可能可以使用特殊权限启动Java虚拟机，使其能够修改禁止写操作的文件。
     *
     * <p> 以<tt>file.setWritable(arg)</tt>形式调用此方法的行为与以下调用完全相同：
     *
     * <pre>
     *     file.setWritable(arg, true) </pre>
     *
     * @param   writable
     *          如果<code>true</code>，设置访问权限以允许写操作；如果<code>false</code>，禁止写操作
     *
     * @return  如果且仅当操作成功时返回<code>true</code>。如果用户没有权限更改此抽象路径名的访问权限，则操作将失败。
     *
     * @throws  SecurityException
     *          如果存在安全经理，且其<code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
     *          方法拒绝对文件的写访问
     *
     * @since 1.6
     */
    public boolean setWritable(boolean writable) {
        return setWritable(writable, true);
    }

    /**
     * 设置此抽象路径名的拥有者或所有人的读权限。在某些平台上，可能可以使用特殊权限启动Java虚拟机，使其能够读取标记为不可读的文件。
     *
     * <p> {@link java.nio.file.Files}类定义了操作文件属性（包括文件权限）的方法。当需要更精细地操作文件权限时，可以使用这些方法。
     *
     * @param   readable
     *          如果<code>true</code>，设置访问权限以允许读操作；如果<code>false</code>，禁止读操作
     *
     * @param   ownerOnly
     *          如果<code>true</code>，读权限仅适用于拥有者的读权限；否则，适用于所有人。如果底层文件系统不能区分拥有者的读权限和其他人的读权限，那么权限将适用于所有人，无论此值如何。
     *
     * @return  如果且仅当操作成功时返回<code>true</code>。如果用户没有权限更改此抽象路径名的访问权限，或者<code>readable</code>为<code>false</code>且底层文件系统不实现读权限，则操作将失败。
     *
     * @throws  SecurityException
     *          如果存在安全经理，且其<code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
     *          方法拒绝对文件的写访问
     *
     * @since 1.6
     */
    public boolean setReadable(boolean readable, boolean ownerOnly) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        if (isInvalid()) {
            return false;
        }
        return fs.setPermission(this, FileSystem.ACCESS_READ, readable, ownerOnly);
    }

                /**
     * 一个方便的方法，用于设置此抽象路径名的所有者的读取权限。在某些平台上，可能可以使用特殊权限启动Java虚拟机，使其能够读取标记为不可读的文件。
     *
     * <p>此方法形式的调用 <tt>file.setReadable(arg)</tt>
     * 与以下调用的行为完全相同
     *
     * <pre>
     *     file.setReadable(arg, true) </pre>
     *
     * @param  readable
     *          如果 <code>true</code>，设置访问权限以允许读取操作；如果 <code>false</code> 则不允许读取操作
     *
     * @return  <code>true</code> 当且仅当操作成功。如果用户没有权限更改此抽象路径名的访问权限，操作将失败。如果 <code>readable</code> 为 <code>false</code> 且底层文件系统未实现读取权限，则操作将失败。
     *
     * @throws  SecurityException
     *          如果存在安全经理，并且其 <code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
     *          方法拒绝文件的写入访问权限
     *
     * @since 1.6
     */
    public boolean setReadable(boolean readable) {
        return setReadable(readable, true);
    }

    /**
     * 设置此抽象路径名的所有者或所有人的执行权限。在某些平台上，可能可以使用特殊权限启动Java虚拟机，使其能够执行未标记为可执行的文件。
     *
     * <p> {@link java.nio.file.Files} 类定义了操作文件属性（包括文件权限）的方法。当需要更精细地操作文件权限时，可以使用此方法。
     *
     * @param   executable
     *          如果 <code>true</code>，设置访问权限以允许执行操作；如果 <code>false</code> 则不允许执行操作
     *
     * @param   ownerOnly
     *          如果 <code>true</code>，执行权限仅适用于所有者的执行权限；否则，它适用于所有人。如果底层文件系统无法区分所有者的执行权限与其他人的执行权限，则无论此值如何，权限都将适用于所有人。
     *
     * @return  <code>true</code> 当且仅当操作成功。如果用户没有权限更改此抽象路径名的访问权限，操作将失败。如果 <code>executable</code> 为 <code>false</code> 且底层文件系统未实现执行权限，则操作将失败。
     *
     * @throws  SecurityException
     *          如果存在安全经理，并且其 <code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
     *          方法拒绝文件的写入访问权限
     *
     * @since 1.6
     */
    public boolean setExecutable(boolean executable, boolean ownerOnly) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        if (isInvalid()) {
            return false;
        }
        return fs.setPermission(this, FileSystem.ACCESS_EXECUTE, executable, ownerOnly);
    }

    /**
     * 一个方便的方法，用于设置此抽象路径名的所有者的执行权限。在某些平台上，可能可以使用特殊权限启动Java虚拟机，使其能够执行未标记为可执行的文件。
     *
     * <p>此方法形式的调用 <tt>file.setExcutable(arg)</tt>
     * 与以下调用的行为完全相同
     *
     * <pre>
     *     file.setExecutable(arg, true) </pre>
     *
     * @param   executable
     *          如果 <code>true</code>，设置访问权限以允许执行操作；如果 <code>false</code> 则不允许执行操作
     *
     * @return   <code>true</code> 当且仅当操作成功。如果用户没有权限更改此抽象路径名的访问权限，操作将失败。如果 <code>executable</code> 为 <code>false</code> 且底层文件系统未实现执行权限，则操作将失败。
     *
     * @throws  SecurityException
     *          如果存在安全经理，并且其 <code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
     *          方法拒绝文件的写入访问权限
     *
     * @since 1.6
     */
    public boolean setExecutable(boolean executable) {
        return setExecutable(executable, true);
    }

    /**
     * 测试应用程序是否可以执行此抽象路径名表示的文件。在某些平台上，可能可以使用特殊权限启动Java虚拟机，使其能够执行未标记为可执行的文件。因此，即使文件没有执行权限，此方法也可能返回 {@code true}。
     *
     * @return  <code>true</code> 当且仅当抽象路径名存在 <em>并且</em> 应用程序被允许执行文件
     *
     * @throws  SecurityException
     *          如果存在安全经理，并且其 <code>{@link
     *          java.lang.SecurityManager#checkExec(java.lang.String)}</code>
     *          方法拒绝文件的执行访问权限
     *
     * @since 1.6
     */
    public boolean canExecute() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkExec(path);
        }
        if (isInvalid()) {
            return false;
        }
        return fs.checkAccess(this, FileSystem.ACCESS_EXECUTE);
    }

    /* -- 文件系统接口 -- */

    /**
     * 列出可用的文件系统根目录。
     *
     * <p> 特定的 Java 平台可能支持零个或多个层次化的文件系统。每个文件系统都有一个
     * {@code root} 目录，从该目录可以访问该文件系统中的所有其他文件。例如，Windows 平台
     * 为每个活动驱动器都有一个根目录；UNIX 平台则有一个单一的根目录，即 {@code "/"}。可用的文件系统根目录集
     * 受系统级操作的影响，如插入或弹出可移动媒体，以及物理或虚拟磁盘驱动器的断开或卸载。
     *
     * <p> 此方法返回一个表示可用文件系统根目录的 {@code File} 对象数组。可以保证任何物理上存在于本地
     * 机器上的文件的规范路径名将以此方法返回的根目录之一开头。
     *
     * <p> 通过远程文件系统协议（如 SMB 或 NFS）访问的位于其他机器上的文件的规范路径名
     * 可能或不可能以此方法返回的根目录之一开头。如果远程文件的路径名在语法上与本地文件的路径名无法区分，
     * 则它将以此方法返回的根目录之一开头。例如，表示 Windows 平台上映射的网络驱动器的根目录的
     * {@code File} 对象将由此方法返回，而包含 UNC 路径名的 {@code File} 对象将不会由此方法返回。
     *
     * <p> 与本类中的大多数方法不同，此方法不会抛出安全异常。如果存在安全管理器，并且其 {@link
     * SecurityManager#checkRead(String)} 方法拒绝访问特定的根目录，则该目录将不会出现在结果中。
     *
     * @return  表示可用文件系统根目录的 {@code File} 对象数组，或如果无法确定根目录集，则返回 {@code null}。
     *          如果没有文件系统根目录，数组将为空。
     *
     * @since  1.2
     * @see java.nio.file.FileStore
     */
    public static File[] listRoots() {
        return fs.listRoots();
    }


    /* -- 磁盘使用情况 -- */

    /**
     * 返回此抽象路径名命名的分区的大小。
     *
     * @return  分区的大小，以字节为单位，或如果此抽象路径名不命名分区，则返回 <tt>0L</tt>
     *
     * @throws  SecurityException
     *          如果已安装安全管理器并且它拒绝
     *          {@link RuntimePermission}<tt>("getFileSystemAttributes")</tt>
     *          或其 {@link SecurityManager#checkRead(String)} 方法拒绝读取此抽象路径名命名的文件
     *
     * @since  1.6
     */
    public long getTotalSpace() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getFileSystemAttributes"));
            sm.checkRead(path);
        }
        if (isInvalid()) {
            return 0L;
        }
        return fs.getSpace(this, FileSystem.SPACE_TOTAL);
    }

    /**
     * 返回此抽象路径名命名的分区中未分配的字节数。
     *
     * <p> 返回的未分配字节数是一个提示，但不是保证，即可以使用这些字节中的大部分或任何部分。未分配的字节数
     * 在此调用后最有可能是准确的。任何外部 I/O 操作，包括在系统外部进行的操作，都可能使其变得不准确。
     * 此方法不保证对文件系统的写操作将成功。
     *
     * @return  分区上的未分配字节数，或如果抽象路径名不命名分区，则返回 <tt>0L</tt>。此值将小于或等于
     *          由 {@link #getTotalSpace} 返回的文件系统总大小。
     *
     * @throws  SecurityException
     *          如果已安装安全管理器并且它拒绝
     *          {@link RuntimePermission}<tt>("getFileSystemAttributes")</tt>
     *          或其 {@link SecurityManager#checkRead(String)} 方法拒绝读取此抽象路径名命名的文件
     *
     * @since  1.6
     */
    public long getFreeSpace() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getFileSystemAttributes"));
            sm.checkRead(path);
        }
        if (isInvalid()) {
            return 0L;
        }
        return fs.getSpace(this, FileSystem.SPACE_FREE);
    }

    /**
     * 返回此虚拟机在此抽象路径名命名的分区上可用的字节数。当可能时，此方法检查写权限和其他操作系统限制，
     * 因此通常会比 {@link #getFreeSpace} 提供更准确的估计，即实际上可以写入多少新数据。
     *
     * <p> 返回的可用字节数是一个提示，但不是保证，即可以使用这些字节中的大部分或任何部分。未分配的字节数
     * 在此调用后最有可能是准确的。任何外部 I/O 操作，包括在系统外部进行的操作，都可能使其变得不准确。
     * 此方法不保证对文件系统的写操作将成功。
     *
     * @return  分区上的可用字节数，或如果抽象路径名不命名分区，则返回 <tt>0L</tt>。在无法获取此信息的系统上，
     *          此方法将等同于调用 {@link #getFreeSpace}。
     *
     * @throws  SecurityException
     *          如果已安装安全管理器并且它拒绝
     *          {@link RuntimePermission}<tt>("getFileSystemAttributes")</tt>
     *          或其 {@link SecurityManager#checkRead(String)} 方法拒绝读取此抽象路径名命名的文件
     *
     * @since  1.6
     */
    public long getUsableSpace() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getFileSystemAttributes"));
            sm.checkRead(path);
        }
        if (isInvalid()) {
            return 0L;
        }
        return fs.getSpace(this, FileSystem.SPACE_USABLE);
    }

                /* -- 临时文件 -- */

    private static class TempDirectory {
        private TempDirectory() { }

        // 临时目录位置
        private static final File tmpdir = new File(AccessController
            .doPrivileged(new GetPropertyAction("java.io.tmpdir")));
        static File location() {
            return tmpdir;
        }

        // 文件名生成
        private static final SecureRandom random = new SecureRandom();
        static File generateFile(String prefix, String suffix, File dir)
            throws IOException
        {
            long n = random.nextLong();
            if (n == Long.MIN_VALUE) {
                n = 0;      // 特殊情况
            } else {
                n = Math.abs(n);
            }

            // 只使用提供的前缀的文件名
            prefix = (new File(prefix)).getName();

            String name = prefix + Long.toString(n) + suffix;
            File f = new File(dir, name);
            if (!name.equals(f.getName()) || f.isInvalid()) {
                if (System.getSecurityManager() != null)
                    throw new IOException("无法创建临时文件");
                else
                    throw new IOException("无法创建临时文件, " + f);
            }
            return f;
        }
    }

    /**
     * <p> 在指定目录中使用给定的前缀和后缀字符串生成其名称，创建一个新的空文件。如果此方法成功返回，则保证：
     *
     * <ol>
     * <li> 返回的抽象路径名表示的文件在调用此方法之前不存在，
     * <li> 在当前虚拟机调用中，此方法及其变体不会返回相同的抽象路径名。
     * </ol>
     *
     * 此方法仅提供临时文件设施的一部分。要自动删除由该方法创建的文件，请使用 <code>{@link #deleteOnExit}</code> 方法。
     *
     * <p> <code>prefix</code> 参数必须至少包含三个字符。建议前缀是一个简短、有意义的字符串，如 <code>"hjb"</code> 或 <code>"mail"</code>。 
     * <code>suffix</code> 参数可以为 <code>null</code>，在这种情况下将使用后缀 <code>".tmp"</code>。
     *
     * <p> 为了创建新文件，前缀和后缀可能会首先调整以适应底层平台的限制。如果前缀太长，则将被截断，但其前三个字符将始终保留。如果后缀太长，它也会被截断，但如果它以点字符
     * (<code>'.'</code>) 开头，则点和其后的前三个字符将始终保留。一旦进行了这些调整，新文件的名称将通过连接前缀、五个或更多内部生成的字符以及后缀来生成。
     *
     * <p> 如果 <code>directory</code> 参数为 <code>null</code>，则使用系统依赖的默认临时文件目录。默认临时文件目录由系统属性
     * <code>java.io.tmpdir</code> 指定。在 UNIX 系统上，此属性的默认值通常是 <code>"/tmp"</code> 或 <code>"/var/tmp"</code>；在
     * Microsoft Windows 系统上，通常是 <code>"C:\\WINNT\\TEMP"</code>。可以在调用 Java 虚拟机时为此系统属性提供不同的值，但对此属性的程序性更改不一定会影响此方法使用的临时目录。
     *
     * @param  prefix     用于生成文件名的前缀字符串；必须至少包含三个字符
     *
     * @param  suffix     用于生成文件名的后缀字符串；可以为 <code>null</code>，在这种情况下将使用后缀 <code>".tmp"</code>
     *
     * @param  directory  文件要创建的目录，或 <code>null</code> 表示使用默认临时文件目录
     *
     * @return  表示新创建的空文件的抽象路径名
     *
     * @throws  IllegalArgumentException
     *          如果 <code>prefix</code> 参数包含少于三个字符
     *
     * @throws  IOException  如果无法创建文件
     *
     * @throws  SecurityException
     *          如果存在安全管理器，并且其 <code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
     *          方法不允许创建文件
     *
     * @since 1.2
     */
    public static File createTempFile(String prefix, String suffix,
                                      File directory)
        throws IOException
    {
        if (prefix.length() < 3)
            throw new IllegalArgumentException("前缀字符串太短");
        if (suffix == null)
            suffix = ".tmp";

        File tmpdir = (directory != null) ? directory
                                          : TempDirectory.location();
        SecurityManager sm = System.getSecurityManager();
        File f;
        do {
            f = TempDirectory.generateFile(prefix, suffix, tmpdir);

            if (sm != null) {
                try {
                    sm.checkWrite(f.getPath());
                } catch (SecurityException se) {
                    // 不透露临时目录位置
                    if (directory == null)
                        throw new SecurityException("无法创建临时文件");
                    throw se;
                }
            }
        } while ((fs.getBooleanAttributes(f) & FileSystem.BA_EXISTS) != 0);

        if (!fs.createFileExclusively(f.getPath()))
            throw new IOException("无法创建临时文件");


                    return f;
    }

    /**
     * 在默认的临时文件目录中创建一个空文件，使用给定的前缀和后缀生成其名称。调用此方法等同于调用
     * <code>{@link #createTempFile(java.lang.String, java.lang.String, java.io.File)
     * createTempFile(prefix,&nbsp;suffix,&nbsp;null)}</code>。
     *
     * <p> {@link
     * java.nio.file.Files#createTempFile(String,String,java.nio.file.attribute.FileAttribute[])
     * Files.createTempFile} 方法提供了在临时文件目录中创建空文件的另一种方法。通过该方法创建的文件
     * 可能比通过此方法创建的文件具有更严格的访问权限，因此可能更适合安全敏感的应用程序。
     *
     * @param  prefix     用于生成文件名称的前缀字符串；必须至少包含三个字符
     *
     * @param  suffix     用于生成文件名称的后缀字符串；可以为 <code>null</code>，在这种情况下，将使用后缀 <code>".tmp"</code>
     *
     * @return  表示新创建的空文件的抽象路径名
     *
     * @throws  IllegalArgumentException
     *          如果 <code>prefix</code> 参数包含少于三个字符
     *
     * @throws  IOException  如果无法创建文件
     *
     * @throws  SecurityException
     *          如果存在安全管理者，并且其 <code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
     *          方法不允许创建文件
     *
     * @since 1.2
     * @see java.nio.file.Files#createTempDirectory(String,FileAttribute[])
     */
    public static File createTempFile(String prefix, String suffix)
        throws IOException
    {
        return createTempFile(prefix, suffix, null);
    }

    /* -- 基础设施 -- */

    /**
     * 按字典顺序比较两个抽象路径名。此方法定义的顺序取决于底层系统。在 UNIX 系统上，比较路径名时区分字母大小写；在 Microsoft Windows
     * 系统上则不区分。
     *
     * @param   pathname  要与此抽象路径名进行比较的抽象路径名
     *
     * @return  如果参数等于此抽象路径名，则返回零；
     *          如果此抽象路径名按字典顺序小于参数，则返回一个小于零的值；
     *          如果此抽象路径名按字典顺序大于参数，则返回一个大于零的值
     *
     * @since   1.2
     */
    public int compareTo(File pathname) {
        return fs.compare(this, pathname);
    }

    /**
     * 测试此抽象路径名是否与给定对象相等。
     * 仅当参数不为 <code>null</code> 且是一个表示与该抽象路径名相同的文件或目录的抽象路径名时，才返回 <code>true</code>。
     * 两个抽象路径名是否相等取决于底层系统。在 UNIX 系统上，比较路径名时区分字母大小写；在 Microsoft Windows
     * 系统上则不区分。
     *
     * @param   obj   要与此抽象路径名进行比较的对象
     *
     * @return  如果对象相同，则返回 <code>true</code>；
     *          否则返回 <code>false</code>
     */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof File)) {
            return compareTo((File)obj) == 0;
        }
        return false;
    }

    /**
     * 计算此抽象路径名的哈希码。由于抽象路径名的相等性本质上是系统依赖的，因此其哈希码的计算也是如此。
     * 在 UNIX 系统上，抽象路径名的哈希码等于其路径名字符串的哈希码与十进制值
     * <code>1234321</code> 的异或。在 Microsoft Windows 系统上，哈希
     * 码等于其路径名字符串转换为小写后的哈希码与十进制值 <code>1234321</code> 的异或。
     * 在将路径名字符串转换为小写时，不考虑区域设置。
     *
     * @return  此抽象路径名的哈希码
     */
    public int hashCode() {
        return fs.hashCode(this);
    }

    /**
     * 返回此抽象路径名的路径名字符串。这仅仅是 <code>{@link #getPath}</code> 方法返回的字符串。
     *
     * @return  此抽象路径名的字符串形式
     */
    public String toString() {
        return getPath();
    }

    /**
     * WriteObject 被调用来保存此文件名。
     * 分隔符字符也会被保存，以便在路径在不同主机类型上重新构造时可以替换。
     * <p>
     * @serialData  默认字段后跟分隔符字符。
     */
    private synchronized void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
        s.defaultWriteObject();
        s.writeChar(separatorChar); // 添加分隔符字符
    }

    /**
     * readObject 被调用来恢复此文件名。
     * 读取原始分隔符字符。如果它与本系统上的分隔符字符不同，则用本地分隔符替换旧的分隔符。
     */
    private synchronized void readObject(java.io.ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        ObjectInputStream.GetField fields = s.readFields();
        String pathField = (String)fields.get("path", null);
        char sep = s.readChar(); // 读取之前的分隔符字符
        if (sep != separatorChar)
            pathField = pathField.replace(sep, separatorChar);
        String path = fs.normalize(pathField);
        UNSAFE.putObject(this, PATH_OFFSET, path);
        UNSAFE.putIntVolatile(this, PREFIX_LENGTH_OFFSET, fs.prefixLength(path));
    }


                private static final long PATH_OFFSET;
    private static final long PREFIX_LENGTH_OFFSET;
    private static final sun.misc.Unsafe UNSAFE;
    static {
        try {
            sun.misc.Unsafe unsafe = sun.misc.Unsafe.getUnsafe();
            PATH_OFFSET = unsafe.objectFieldOffset(
                    File.class.getDeclaredField("path"));
            PREFIX_LENGTH_OFFSET = unsafe.objectFieldOffset(
                    File.class.getDeclaredField("prefixLength"));
            UNSAFE = unsafe;
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }


    /** 使用 JDK 1.0.2 的 serialVersionUID 以确保互操作性 */
    private static final long serialVersionUID = 301077366599181567L;

    // -- 与 java.nio.file 的集成 --

    private volatile transient Path filePath;

    /**
     * 返回一个从该抽象路径构造的 {@link Path java.nio.file.Path} 对象。生成的 {@code Path} 与
     * {@link java.nio.file.FileSystems#getDefault 默认文件系统} 关联。
     *
     * <p> 首次调用此方法时，其行为类似于调用以下表达式：
     * <blockquote><pre>
     * {@link java.nio.file.FileSystems#getDefault FileSystems.getDefault}().{@link
     * java.nio.file.FileSystem#getPath getPath}(this.{@link #getPath getPath}());
     * </pre></blockquote>
     * 后续调用此方法将返回相同的 {@code Path}。
     *
     * <p> 如果此抽象路径名为空，则此方法返回一个可以用于访问当前用户目录的 {@code Path}。
     *
     * @return 从该抽象路径构造的 {@code Path}
     *
     * @throws  java.nio.file.InvalidPathException
     *          如果无法从抽象路径构造 {@code Path} 对象（请参见 {@link java.nio.file.FileSystem#getPath FileSystem.getPath}）
     *
     * @since   1.7
     * @see Path#toFile
     */
    public Path toPath() {
        Path result = filePath;
        if (result == null) {
            synchronized (this) {
                result = filePath;
                if (result == null) {
                    result = FileSystems.getDefault().getPath(path);
                    filePath = result;
                }
            }
        }
        return result;
    }
}
