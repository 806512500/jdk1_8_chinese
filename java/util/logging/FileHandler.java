
/*
 * 版权所有 (c) 2000, 2021，Oracle 和/或其附属公司。保留所有权利。
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

package java.util.logging;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;

/**
 * 简单的文件日志 <tt>Handler</tt>。
 * <p>
 * <tt>FileHandler</tt> 可以写入指定的文件，也可以写入一组循环文件。
 * <p>
 * 对于一组循环文件，当每个文件达到给定的大小限制时，它将被关闭、旋转出，并打开一个新文件。
 * 较旧的文件通过在基础文件名中添加 "0"、"1"、"2" 等来命名。
 * <p>
 * 默认情况下，IO 库中启用了缓冲，但每个日志记录在完成时都会被刷新。
 * <p>
 * 默认情况下使用 <tt>XMLFormatter</tt> 类进行格式化。
 * <p>
 * <b>配置：</b>
 * 默认情况下，每个 <tt>FileHandler</tt> 使用以下 <tt>LogManager</tt> 配置属性进行初始化，其中 <tt>&lt;handler-name&gt;</tt>
 * 是处理器的完全限定类名。如果属性未定义
 * （或具有无效值），则使用指定的默认值。
 * <ul>
 * <li>   &lt;handler-name&gt;.level
 *        指定 <tt>Handler</tt> 的默认级别
 *        （默认为 <tt>Level.ALL</tt>）。 </li>
 * <li>   &lt;handler-name&gt;.filter
 *        指定要使用的 <tt>Filter</tt> 类的名称
 *        （默认为不使用 <tt>Filter</tt>）。 </li>
 * <li>   &lt;handler-name&gt;.formatter
 *        指定要使用的 <tt>Formatter</tt> 类的名称
 *        （默认为 <tt>java.util.logging.XMLFormatter</tt>）。 </li>
 * <li>   &lt;handler-name&gt;.encoding
 *        要使用的字符集编码的名称（默认为
 *        平台默认编码）。 </li>
 * <li>   &lt;handler-name&gt;.limit
 *        指定写入任何单个文件的近似最大量（以字节为单位）。 如果此值为零，则没有限制。
 *        （默认为无限制）。 </li>
 * <li>   &lt;handler-name&gt;.count
 *        指定要循环使用的输出文件数（默认为 1）。 </li>
 * <li>   &lt;handler-name&gt;.pattern
 *        指定生成输出文件名的模式。 请参见
 *        下文中的详细信息。（默认为 "%h/java%u.log"）。 </li>
 * <li>   &lt;handler-name&gt;.append
 *        指定 <tt>FileHandler</tt> 是否应追加到
 *        任何现有文件（默认为 false）。 </li>
 * </ul>
 * <p>
 * 例如，{@code FileHandler} 的属性为：
 * <ul>
 * <li>   java.util.logging.FileHandler.level=INFO </li>
 * <li>   java.util.logging.FileHandler.formatter=java.util.logging.SimpleFormatter </li>
 * </ul>
 * <p>
 * 对于自定义处理器，例如 com.foo.MyHandler，属性为：
 * <ul>
 * <li>   com.foo.MyHandler.level=INFO </li>
 * <li>   com.foo.MyHandler.formatter=java.util.logging.SimpleFormatter </li>
 * </ul>
 * <p>
 * 模式由一个字符串组成，该字符串包含在运行时将被替换的以下特殊组件：
 * <ul>
 * <li>    "/"    本地路径名分隔符 </li>
 * <li>     "%t"   系统临时目录 </li>
 * <li>     "%h"   "user.home" 系统属性的值 </li>
 * <li>     "%g"   用于区分循环日志的生成号 </li>
 * <li>     "%u"   用于解决冲突的唯一编号 </li>
 * <li>     "%%"   转换为单个百分号 "%" </li>
 * </ul>
 * 如果未指定 "%g" 字段且文件计数大于一，则生成号将被添加到生成的文件名的末尾，后跟一个点。
 * <p>
 * 因此，例如，模式为 "%t/java%g.log" 且计数为 2 时，通常会在 Solaris 上将日志文件写入
 * /var/tmp/java0.log 和 /var/tmp/java1.log，而在 Windows 95 上通常会写入 C:\TEMP\java0.log 和 C:\TEMP\java1.log
 * <p>
 * 生成号遵循 0, 1, 2 等的序列。
 * <p>
 * 通常 "%u" 唯一字段设置为 0。但是，如果 <tt>FileHandler</tt>
 * 尝试打开文件名并发现文件当前正被另一个进程使用，它将递增唯一编号字段并重试。 这将重复进行，直到 <tt>FileHandler</tt> 找到一个当前未被使用的文件名。 如果存在冲突且未指定 "%u" 字段，则会在文件名末尾添加该字段，后跟一个点。
 * （这将在任何自动添加的生成号之后。）
 * <p>
 * 因此，如果三个进程都试图记录到 fred%u.%g.txt，则
 * 它们可能会使用 fred0.0.txt、fred1.0.txt、fred2.0.txt 作为它们循环序列中的第一个文件。
 * <p>
 * 请注意，使用唯一 ID 避免冲突仅在使用本地磁盘文件系统时才能可靠地工作。
 *
 * @since 1.4
 */

public class FileHandler extends StreamHandler {
    private MeteredStream meter;
    private boolean append;
    private int limit;       // zero => no limit.
    private int count;
    private String pattern;
    private String lockFileName;
    private FileChannel lockFileChannel;
    private File files[];
    private static final int DEFAULT_MAX_LOCKS = 100;
    private static int maxLocks;
    private static final Set<String> locks = new HashSet<>();


                /*
     * 从系统属性初始化 maxLocks，如果设置。
     * 如果提供的属性无效或未提供，则使用 100 作为默认值。
     */
    static {
        maxLocks = java.security.AccessController.doPrivileged(
                (PrivilegedAction<Integer>) () ->
                        Integer.getInteger(
                                "jdk.internal.FileHandlerLogging.maxLocks",
                                DEFAULT_MAX_LOCKS)
        );

        if (maxLocks <= 0) {
            maxLocks = DEFAULT_MAX_LOCKS;
        }
    }

    /**
     * 计量流是 OutputStream 的一个子类，它
     * (a) 将所有输出转发到目标流
     * (b) 跟踪已写入的字节数
     */
    private class MeteredStream extends OutputStream {
        final OutputStream out;
        int written;

        MeteredStream(OutputStream out, int written) {
            this.out = out;
            this.written = written;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            written++;
        }

        @Override
        public void write(byte buff[]) throws IOException {
            out.write(buff);
            written += buff.length;
        }

        @Override
        public void write(byte buff[], int off, int len) throws IOException {
            out.write(buff, off, len);
            written += len;
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }

        @Override
        public void close() throws IOException {
            out.close();
        }
    }

    private void open(File fname, boolean append) throws IOException {
        int len = 0;
        if (append) {
            len = (int) fname.length();
        }
        FileOutputStream fout = new FileOutputStream(fname.toString(), append);
        BufferedOutputStream bout = new BufferedOutputStream(fout);
        meter = new MeteredStream(bout, len);
        setOutputStream(meter);
    }

    /**
     * 从 LogManager 属性和/或类 javadoc 中指定的默认值配置 FileHandler。
     */
    private void configure() {
        LogManager manager = LogManager.getLogManager();

        String cname = getClass().getName();

        pattern = manager.getStringProperty(cname + ".pattern", "%h/java%u.log");
        limit = manager.getIntProperty(cname + ".limit", 0);
        if (limit < 0) {
            limit = 0;
        }
        count = manager.getIntProperty(cname + ".count", 1);
        if (count <= 0) {
            count = 1;
        }
        append = manager.getBooleanProperty(cname + ".append", false);
        setLevel(manager.getLevelProperty(cname + ".level", Level.ALL));
        setFilter(manager.getFilterProperty(cname + ".filter", null));
        setFormatter(manager.getFormatterProperty(cname + ".formatter", new XMLFormatter()));
        try {
            setEncoding(manager.getStringProperty(cname + ".encoding", null));
        } catch (Exception ex) {
            try {
                setEncoding(null);
            } catch (Exception ex2) {
                // 使用 null 进行 setEncoding 应该总是有效的。
                // assert false;
            }
        }
    }


    /**
     * 构造一个默认的 <tt>FileHandler</tt>。这将完全从 <tt>LogManager</tt> 属性（或其默认值）配置。
     * <p>
     * @exception  IOException 如果打开文件时出现 IO 问题。
     * @exception  SecurityException 如果存在安全经理并且调用者没有 <tt>LoggingPermission("control"))</tt>。
     * @exception  NullPointerException 如果模式属性是空字符串。
     */
    public FileHandler() throws IOException, SecurityException {
        checkPermission();
        configure();
        openFiles();
    }

    /**
     * 初始化一个 <tt>FileHandler</tt> 以写入给定的文件名。
     * <p>
     * <tt>FileHandler</tt> 的配置基于 <tt>LogManager</tt> 属性（或其默认值），但使用给定的模式参数作为文件名模式，文件限制设置为无限制，文件计数设置为一个。
     * <p>
     * 写入的数据量没有限制，所以请谨慎使用。
     *
     * @param pattern  输出文件的名称
     * @exception  IOException 如果打开文件时出现 IO 问题。
     * @exception  SecurityException 如果存在安全经理并且调用者没有 <tt>LoggingPermission("control")</tt>。
     * @exception  IllegalArgumentException 如果模式是空字符串
     */
    public FileHandler(String pattern) throws IOException, SecurityException {
        if (pattern.length() < 1 ) {
            throw new IllegalArgumentException();
        }
        checkPermission();
        configure();
        this.pattern = pattern;
        this.limit = 0;
        this.count = 1;
        openFiles();
    }

    /**
     * 初始化一个 <tt>FileHandler</tt> 以写入给定的文件名，可选追加模式。
     * <p>
     * <tt>FileHandler</tt> 的配置基于 <tt>LogManager</tt> 属性（或其默认值），但使用给定的模式参数作为文件名模式，文件限制设置为无限制，文件计数设置为一个，追加模式设置为给定的 <tt>append</tt> 参数。
     * <p>
     * 写入的数据量没有限制，所以请谨慎使用。
     *
     * @param pattern  输出文件的名称
     * @param append  指定追加模式
     * @exception  IOException 如果打开文件时出现 IO 问题。
     * @exception  SecurityException 如果存在安全经理并且调用者没有 <tt>LoggingPermission("control")</tt>。
     * @exception  IllegalArgumentException 如果模式是空字符串
     */
    public FileHandler(String pattern, boolean append) throws IOException,
            SecurityException {
        if (pattern.length() < 1 ) {
            throw new IllegalArgumentException();
        }
        checkPermission();
        configure();
        this.pattern = pattern;
        this.limit = 0;
        this.count = 1;
        this.append = append;
        openFiles();
    }

                /**
     * 初始化一个 <tt>FileHandler</tt> 以写入一组文件。当
     * （大约）写入到一个文件的字节数达到给定的限制时，
     * 将打开另一个文件。输出将在一组文件中循环。
     * <p>
     * <tt>FileHandler</tt> 的配置基于 <tt>LogManager</tt>
     * 属性（或其默认值），但使用给定的模式参数作为文件名模式，
     * 文件限制设置为限制参数，文件计数设置为给定的计数参数。
     * <p>
     * 计数必须至少为 1。
     *
     * @param pattern  用于命名输出文件的模式
     * @param limit  写入任何单个文件的最大字节数
     * @param count  要使用的文件数
     * @exception  IOException 如果打开文件时出现 IO 问题。
     * @exception  SecurityException  如果存在安全管理器并且
     *             调用者没有 <tt>LoggingPermission("control")</tt>。
     * @exception  IllegalArgumentException 如果 {@code limit < 0}，或 {@code count < 1}。
     * @exception  IllegalArgumentException 如果模式是空字符串
     */
    public FileHandler(String pattern, int limit, int count)
                                        throws IOException, SecurityException {
        if (limit < 0 || count < 1 || pattern.length() < 1) {
            throw new IllegalArgumentException();
        }
        checkPermission();
        configure();
        this.pattern = pattern;
        this.limit = limit;
        this.count = count;
        openFiles();
    }

    /**
     * 初始化一个 <tt>FileHandler</tt> 以写入一组文件
     * 并可选地追加。当（大约）写入到一个文件的字节数达到给定的限制时，
     * 将打开另一个文件。输出将在一组文件中循环。
     * <p>
     * <tt>FileHandler</tt> 的配置基于 <tt>LogManager</tt>
     * 属性（或其默认值），但使用给定的模式参数作为文件名模式，
     * 文件限制设置为限制参数，文件计数设置为给定的计数参数，追加模式设置为给定的
     * <tt>append</tt> 参数。
     * <p>
     * 计数必须至少为 1。
     *
     * @param pattern  用于命名输出文件的模式
     * @param limit  写入任何单个文件的最大字节数
     * @param count  要使用的文件数
     * @param append  指定追加模式
     * @exception  IOException 如果打开文件时出现 IO 问题。
     * @exception  SecurityException  如果存在安全管理器并且
     *             调用者没有 <tt>LoggingPermission("control")</tt>。
     * @exception  IllegalArgumentException 如果 {@code limit < 0}，或 {@code count < 1}。
     * @exception  IllegalArgumentException 如果模式是空字符串
     *
     */
    public FileHandler(String pattern, int limit, int count, boolean append)
                                        throws IOException, SecurityException {
        if (limit < 0 || count < 1 || pattern.length() < 1) {
            throw new IllegalArgumentException();
        }
        checkPermission();
        configure();
        this.pattern = pattern;
        this.limit = limit;
        this.count = count;
        this.append = append;
        openFiles();
    }

    private  boolean isParentWritable(Path path) {
        Path parent = path.getParent();
        if (parent == null) {
            parent = path.toAbsolutePath().getParent();
        }
        return parent != null && Files.isWritable(parent);
    }

    /**
     * 根据配置的实例变量打开一组输出文件。
     */
    private void openFiles() throws IOException {
        LogManager manager = LogManager.getLogManager();
        manager.checkPermission();
        if (count < 1) {
           throw new IllegalArgumentException("file count = " + count);
        }
        if (limit < 0) {
            limit = 0;
        }

        // 在初始化期间注册我们自己的 ErrorManager
        // 以便我们可以记录异常。
        InitializationErrorManager em = new InitializationErrorManager();
        setErrorManager(em);

        // 创建一个锁文件。这在我们存活期间授予我们对输出文件集的独占访问权。
        int unique = -1;
        for (;;) {
            unique++;
            if (unique > maxLocks) {
                throw new IOException("Couldn't get lock for " + pattern
                        + ", maxLocks: " + maxLocks);
            }
            // 从 "unique" int 生成一个锁文件名。
            lockFileName = generate(pattern, 0, unique).toString() + ".lck";
            // 现在尝试锁定该文件名。
            // 由于某些系统（例如 Solaris）只能在进程之间（而不是进程内部）进行文件锁定，
            // 我们首先检查我们自己是否已经锁定了该文件。
            synchronized(locks) {
                if (locks.contains(lockFileName)) {
                    // 我们已经为另一个 FileHandler
                    // 对象拥有此锁。再试一次。
                    continue;
                }

                final Path lockFilePath = Paths.get(lockFileName);
                FileChannel channel = null;
                int retries = -1;
                boolean fileCreated = false;
                while (channel == null && retries++ < 1) {
                    try {
                        channel = FileChannel.open(lockFilePath,
                                CREATE_NEW, WRITE);
                        fileCreated = true;
                    } catch (AccessDeniedException ade) {
                        // 这可能是暂时的，也可能是更持久的问题。
                        // 锁文件可能仍待删除（暂时），或者父目录可能不可访问，
                        // 不可写等。
                        // 如果我们可以写入当前目录，并且这是一个常规文件，
                        // 让我们再试一次。
                        if (Files.isRegularFile(lockFilePath, LinkOption.NOFOLLOW_LINKS)
                            && isParentWritable(lockFilePath)) {
                            // 再试一次。如果仍然不成功，这最终会
                            // 确保我们增加 "unique" 并使用另一个文件名。
                            continue;
                        } else {
                            throw ade; // 无需重试
                        }
                    } catch (FileAlreadyExistsException ix) {
                        // 这可能是前一次执行留下的僵尸文件。如果我们可以实际
                        // 写入其目录，则重用它。
                        // 请注意，这种情况可能会发生，但不会太频繁。
                        if (Files.isRegularFile(lockFilePath, LinkOption.NOFOLLOW_LINKS)
                            && isParentWritable(lockFilePath)) {
                            try {
                                channel = FileChannel.open(lockFilePath,
                                    WRITE, APPEND);
                            } catch (NoSuchFileException x) {
                                // 竞争条件 - 再试一次，如果再次失败
                                // 就使用序列中的下一个名称。
                                continue;
                            } catch(IOException x) {
                                // 文件可能对我们不可写。
                                // 使用序列中的下一个名称。
                                break;
                            }
                        } else {
                            // 此时 channel 应该仍然是 null。
                            // 使用序列中的下一个名称。
                            break;
                        }
                    }
                }


                            if (channel == null) continue; // 尝试下一个名称;
                lockFileChannel = channel;

                boolean available;
                try {
                    available = lockFileChannel.tryLock() != null;
                    // 我们成功获取了锁。
                    // 在这里我们可以调用 File.deleteOnExit()。
                    // 但是，如 JDK-4872014 所示，这可能会产生不良的副作用。
                    // 因此，我们将依赖于 close() 将删除锁文件的事实，
                    // 以及创建 FileHandlers 的人应该负责关闭它们。
                } catch (IOException ix) {
                    // 在尝试获取锁时我们遇到了 IOException。
                    // 这通常表明目标目录不支持锁定。
                    // 我们必须在没有获取锁的情况下继续。
                    // 但是，只有在我们创建了文件的情况下才继续。
                    available = fileCreated;
                } catch (OverlappingFileLockException x) {
                    // 在这个虚拟机中，其他人已经通过其他通道锁定了这个文件
                    // —— 即使用了 new FileHandler(...);
                    // 继续搜索可用的锁。
                    available = false;
                }
                if (available) {
                    // 我们获取了锁。 记住它。
                    locks.add(lockFileName);
                    break;
                }

                // 我们未能获取锁。 尝试下一个文件。
                lockFileChannel.close();
            }
        }

        files = new File[count];
        for (int i = 0; i < count; i++) {
            files[i] = generate(pattern, i, unique);
        }

        // 创建初始日志文件。
        if (append) {
            open(files[0], true);
        } else {
            rotate();
        }

        // 在初始化期间是否检测到任何异常？
        Exception ex = em.lastException;
        if (ex != null) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else if (ex instanceof SecurityException) {
                throw (SecurityException) ex;
            } else {
                throw new IOException("Exception: " + ex);
            }
        }

        // 安装正常的默认 ErrorManager。
        setErrorManager(new ErrorManager());
    }

    /**
     * 根据用户提供的模式、生成号和整数唯一性后缀生成文件
     * @param pattern 用于命名输出文件的模式
     * @param generation 用于区分旋转日志的生成号
     * @param unique 用于解决冲突的唯一数字
     * @return 生成的 File
     * @throws IOException
     */
    private File generate(String pattern, int generation, int unique)
            throws IOException {
        File file = null;
        String word = "";
        int ix = 0;
        boolean sawg = false;
        boolean sawu = false;
        while (ix < pattern.length()) {
            char ch = pattern.charAt(ix);
            ix++;
            char ch2 = 0;
            if (ix < pattern.length()) {
                ch2 = Character.toLowerCase(pattern.charAt(ix));
            }
            if (ch == '/') {
                if (file == null) {
                    file = new File(word);
                } else {
                    file = new File(file, word);
                }
                word = "";
                continue;
            } else  if (ch == '%') {
                if (ch2 == 't') {
                    String tmpDir = System.getProperty("java.io.tmpdir");
                    if (tmpDir == null) {
                        tmpDir = System.getProperty("user.home");
                    }
                    file = new File(tmpDir);
                    ix++;
                    word = "";
                    continue;
                } else if (ch2 == 'h') {
                    file = new File(System.getProperty("user.home"));
                    if (isSetUID()) {
                        // 好的，我们在一个 set UID 程序中。为了安全起见，
                        // 我们不允许尝试打开相对于 %h 的文件。
                        throw new IOException("不能在 set UID 程序中使用 %h");
                    }
                    ix++;
                    word = "";
                    continue;
                } else if (ch2 == 'g') {
                    word = word + generation;
                    sawg = true;
                    ix++;
                    continue;
                } else if (ch2 == 'u') {
                    word = word + unique;
                    sawu = true;
                    ix++;
                    continue;
                } else if (ch2 == '%') {
                    word = word + "%";
                    ix++;
                    continue;
                }
            }
            word = word + ch;
        }
        if (count > 1 && !sawg) {
            word = word + "." + generation;
        }
        if (unique > 0 && !sawu) {
            word = word + "." + unique;
        }
        if (word.length() > 0) {
            if (file == null) {
                file = new File(word);
            } else {
                file = new File(file, word);
            }
        }
        return file;
    }

    /**
     * 旋转输出文件集
     */
    private synchronized void rotate() {
        Level oldLevel = getLevel();
        setLevel(Level.OFF);

        super.close();
        for (int i = count-2; i >= 0; i--) {
            File f1 = files[i];
            File f2 = files[i+1];
            if (f1.exists()) {
                if (f2.exists()) {
                    f2.delete();
                }
                f1.renameTo(f2);
            }
        }
        try {
            open(files[0], false);
        } catch (IOException ix) {
            // 我们不希望在这里抛出异常，但我们
            // 将异常报告给任何注册的 ErrorManager。
            reportError(null, ix, ErrorManager.OPEN_FAILURE);

                    }
        setLevel(oldLevel);
    }

    /**
     * 格式化并发布一个 <tt>LogRecord</tt>。
     *
     * @param  record  日志事件的描述。如果记录为 null，则会被静默忽略且不会发布
     */
    @Override
    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        super.publish(record);
        flush();
        if (limit > 0 && meter.written >= limit) {
            // 我们在 "init" 方法中执行了访问检查，以确保我们只从受信任的代码初始化。因此我们假设
            // 即使我们当前被不受信任的代码调用，写入目标文件也是安全的。
            // 因此在这里提升权限是安全的。
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    rotate();
                    return null;
                }
            });
        }
    }

    /**
     * 关闭所有文件。
     *
     * @exception  SecurityException  如果存在安全经理，并且调用者没有 <tt>LoggingPermission("control")</tt> 权限。
     */
    @Override
    public synchronized void close() throws SecurityException {
        super.close();
        // 解锁任何锁定文件。
        if (lockFileName == null) {
            return;
        }
        try {
            // 关闭锁定文件通道（这也会释放任何锁）
            lockFileChannel.close();
        } catch (Exception ex) {
            // 关闭流时出现问题。放弃。
        }
        synchronized(locks) {
            locks.remove(lockFileName);
        }
        new File(lockFileName).delete();
        lockFileName = null;
        lockFileChannel = null;
    }

    private static class InitializationErrorManager extends ErrorManager {
        Exception lastException;
        @Override
        public void error(String msg, Exception ex, int code) {
            lastException = ex;
        }
    }

    /**
     * 检查我们是否在一个设置了 UID 的程序中。
     */
    private static native boolean isSetUID();
}
