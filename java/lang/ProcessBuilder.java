
/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 该类用于创建操作系统进程。
 *
 * <p>每个 {@code ProcessBuilder} 实例管理一组进程属性。 {@link #start()} 方法使用这些属性创建一个新的
 * {@link Process} 实例。可以从同一个实例多次调用 {@link #start()} 方法，以使用相同或相关的属性创建新的子进程。
 *
 * <p>每个进程构建器管理以下进程属性：
 *
 * <ul>
 *
 * <li>一个 <i>命令</i>，一个字符串列表，表示要调用的外部程序文件及其参数（如果有）。哪些字符串列表表示有效的操作系统命令是系统依赖的。例如，每个概念上的参数通常是此列表中的一个元素，但有些操作系统期望程序自己解析命令行字符串 - 在这种系统上，Java 实现可能要求命令包含正好两个元素。
 *
 * <li>一个 <i>环境</i>，这是一个系统依赖的从 <i>变量</i> 到 <i>值</i> 的映射。初始值是当前进程的环境的副本（参见 {@link System#getenv()}）。
 *
 * <li>一个 <i>工作目录</i>。默认值是当前进程的当前工作目录，通常是系统属性 {@code user.dir} 指定的目录。
 *
 * <li><a name="redirect-input">一个 <i>标准输入</i> 的来源</a>。默认情况下，子进程从管道读取输入。Java 代码可以通过
 * {@link Process#getOutputStream()} 返回的输出流访问此管道。但是，可以使用
 * {@link #redirectInput(Redirect) redirectInput} 将标准输入重定向到其他来源。在这种情况下，{@link Process#getOutputStream()} 将返回一个
 * <i>null 输出流</i>，其特性为：
 *
 * <ul>
 * <li>{@link OutputStream#write(int) write} 方法总是抛出 {@code IOException}
 * <li>{@link OutputStream#close() close} 方法不执行任何操作
 * </ul>
 *
 * <li><a name="redirect-output">一个 <i>标准输出</i> 和 <i>标准错误</i> 的目标</a>。默认情况下，子进程将标准输出和标准错误写入管道。Java 代码可以通过
 * {@link Process#getInputStream()} 和 {@link Process#getErrorStream()} 返回的输入流访问这些管道。但是，可以使用
 * {@link #redirectOutput(Redirect) redirectOutput} 和
 * {@link #redirectError(Redirect) redirectError} 将标准输出和标准错误重定向到其他目标。在这种情况下，{@link Process#getInputStream()} 和/或
 * {@link Process#getErrorStream()} 将返回一个 <i>null 输入流</i>，其特性为：
 *
 * <ul>
 * <li>{@link InputStream#read() read} 方法总是返回 {@code -1}
 * <li>{@link InputStream#available() available} 方法总是返回 {@code 0}
 * <li>{@link InputStream#close() close} 方法不执行任何操作
 * </ul>
 *
 * <li>一个 <i>redirectErrorStream</i> 属性。初始情况下，此属性为 {@code false}，意味着子进程的标准输出和错误输出被发送到两个不同的流，可以使用
 * {@link Process#getInputStream()} 和 {@link Process#getErrorStream()} 方法访问。
 *
 * <p>如果将此值设置为 {@code true}，则：
 *
 * <ul>
 * <li>标准错误与标准输出合并，并始终发送到同一目标（这使得错误消息与相应的输出更容易关联）
 * <li>可以使用
 * {@link #redirectOutput(Redirect) redirectOutput} 将标准错误和标准输出的共同目标重定向
 * <li>通过
 * {@link #redirectError(Redirect) redirectError} 方法设置的任何重定向在创建子进程时将被忽略
 * <li>从 {@link Process#getErrorStream()} 返回的流将始终是一个 <a href="#redirect-output">null 输入流</a>
 * </ul>
 *
 * </ul>
 *
 * <p>修改进程构建器的属性将影响该对象的 {@link #start()} 方法随后启动的进程，但绝不会影响之前启动的进程或 Java 进程本身。
 *
 * <p>大多数错误检查是由 {@link #start()} 方法执行的。有可能修改对象的状态，使 {@link #start()} 失败。例如，将命令属性设置为空列表不会抛出异常，除非调用 {@link #start()}。
 *
 * <p><strong>请注意，此类不是同步的。</strong>
 * 如果多个线程同时访问一个 {@code ProcessBuilder} 实例，并且至少有一个线程结构上修改了其中一个属性，则必须外部同步。
 *
 * <p>使用默认工作目录和环境启动新进程非常简单：
 *
 * <pre> {@code
 * Process p = new ProcessBuilder("myCommand", "myArg").start();
 * }</pre>
 *
 * <p>以下是一个示例，该示例启动一个进程，修改工作目录和环境，并将标准输出和错误重定向到追加到日志文件：
 *
 * <pre> {@code
 * ProcessBuilder pb =
 *   new ProcessBuilder("myCommand", "myArg1", "myArg2");
 * Map<String, String> env = pb.environment();
 * env.put("VAR1", "myValue");
 * env.remove("OTHERVAR");
 * env.put("VAR2", env.get("VAR1") + "suffix");
 * pb.directory(new File("myDir"));
 * File log = new File("log");
 * pb.redirectErrorStream(true);
 * pb.redirectOutput(Redirect.appendTo(log));
 * Process p = pb.start();
 * assert pb.redirectInput() == Redirect.PIPE;
 * assert pb.redirectOutput().file() == log;
 * assert p.getInputStream().read() == -1;
 * }</pre>
 *
 * <p>要使用一组显式设置的环境变量启动进程，首先调用 {@link java.util.Map#clear() Map.clear()}
 * 以清除环境变量，然后再添加环境变量。
 *
 * @author Martin Buchholz
 * @since 1.5
 */


public final class ProcessBuilder
{
    private List<String> command;
    private File directory;
    private Map<String,String> environment;
    private boolean redirectErrorStream;
    private Redirect[] redirects;

    /**
     * 使用指定的操作系统程序和参数构造一个进程构建器。此构造函数不会复制 {@code command} 列表。对列表的后续更新将反映在进程构建器的状态中。不检查 {@code command} 是否对应于有效的操作系统命令。
     *
     * @param  command 包含程序及其参数的列表
     * @throws NullPointerException 如果参数为 null
     */
    public ProcessBuilder(List<String> command) {
        if (command == null)
            throw new NullPointerException();
        this.command = command;
    }

    /**
     * 使用指定的操作系统程序和参数构造一个进程构建器。这是一个方便的构造函数，将进程构建器的命令设置为包含与 {@code command} 数组相同字符串的字符串列表，顺序相同。不检查 {@code command} 是否对应于有效的操作系统命令。
     *
     * @param command 包含程序及其参数的字符串数组
     */
    public ProcessBuilder(String... command) {
        this.command = new ArrayList<>(command.length);
        for (String arg : command)
            this.command.add(arg);
    }

    /**
     * 设置此进程构建器的操作系统程序和参数。此方法不会复制 {@code command} 列表。对列表的后续更新将反映在进程构建器的状态中。不检查 {@code command} 是否对应于有效的操作系统命令。
     *
     * @param  command 包含程序及其参数的列表
     * @return 此进程构建器
     *
     * @throws NullPointerException 如果参数为 null
     */
    public ProcessBuilder command(List<String> command) {
        if (command == null)
            throw new NullPointerException();
        this.command = command;
        return this;
    }

    /**
     * 设置此进程构建器的操作系统程序和参数。这是一个方便的方法，将命令设置为包含与 {@code command} 数组相同字符串的字符串列表，顺序相同。不检查 {@code command} 是否对应于有效的操作系统命令。
     *
     * @param  command 包含程序及其参数的字符串数组
     * @return 此进程构建器
     */
    public ProcessBuilder command(String... command) {
        this.command = new ArrayList<>(command.length);
        for (String arg : command)
            this.command.add(arg);
        return this;
    }

    /**
     * 返回此进程构建器的操作系统程序和参数。返回的列表不是副本。对列表的后续更新将反映在此进程构建器的状态中。
     *
     * @return 此进程构建器的程序及其参数
     */
    public List<String> command() {
        return command;
    }

    /**
     * 返回此进程构建器环境的字符串映射视图。
     *
     * 每当创建进程构建器时，环境将初始化为当前进程环境的副本（参见 {@link System#getenv()}）。随后通过此对象的 {@link #start()} 方法启动的子进程将使用此映射作为其环境。
     *
     * <p>可以使用普通的 {@link java.util.Map Map} 操作修改返回的对象。这些修改将对通过 {@link #start()} 方法启动的子进程可见。两个 {@code ProcessBuilder} 实例总是包含独立的进程环境，因此对返回映射的修改永远不会反映在任何其他 {@code ProcessBuilder} 实例或 {@link System#getenv System.getenv} 返回的值中。
     *
     * <p>如果系统不支持环境变量，则返回空映射。
     *
     * <p>返回的映射不允许 null 键或值。尝试插入或查询 null 键或值将抛出 {@link NullPointerException}。尝试查询不是 {@link String} 类型的键或值的存在将抛出 {@link ClassCastException}。
     *
     * <p>返回映射的行为是系统依赖的。系统可能不允许修改环境变量或可能禁止某些变量名或值。因此，如果修改不受操作系统允许，尝试修改映射可能会因 {@link UnsupportedOperationException} 或 {@link IllegalArgumentException} 而失败。
     *
     * <p>由于环境变量名和值的外部格式是系统依赖的，因此它们与 Java 的 Unicode 字符串之间可能没有一对一的映射。尽管如此，映射的实现方式是，未被 Java 代码修改的环境变量将在子进程中具有未修改的本机表示。
     *
     * <p>返回的映射及其集合视图可能不遵守 {@link Object#equals} 和 {@link Object#hashCode} 方法的一般契约。
     *
     * <p>在所有平台上，返回的映射通常都是区分大小写的。
     *
     * <p>如果存在安全经理，其 {@link SecurityManager#checkPermission checkPermission} 方法将被调用，使用 {@link RuntimePermission}{@code ("getenv.*")} 权限。这可能导致抛出 {@link SecurityException}。
     *
     * <p>当向 Java 子进程传递信息时，通常首选 <a href=System.html#EnvironmentVSSystemProperties>系统属性</a> 而不是环境变量。
     *
     * @return 此进程构建器的环境
     *
     * @throws SecurityException
     *         如果存在安全经理，且其 {@link SecurityManager#checkPermission checkPermission}
     *         方法不允许访问进程环境
     *
     * @see    Runtime#exec(String[],String[],java.io.File)
     * @see    System#getenv()
     */
    public Map<String,String> environment() {
        SecurityManager security = System.getSecurityManager();
        if (security != null)
            security.checkPermission(new RuntimePermission("getenv.*"));


                        if (environment == null)
            environment = ProcessEnvironment.environment();

        assert environment != null;

        return environment;
    }

    // 仅用于 Runtime.exec(...envp...)
    ProcessBuilder environment(String[] envp) {
        assert environment == null;
        if (envp != null) {
            environment = ProcessEnvironment.emptyEnvironment(envp.length);
            assert environment != null;

            for (String envstring : envp) {
                // 在 1.5 之前，我们盲目地将无效的 envstrings 传递给子进程。
                // 我们希望抛出一个异常，但为了与旧的错误代码兼容，我们没有这样做。

                // 静默丢弃任何尾随的垃圾。
                if (envstring.indexOf((int) '\u0000') != -1)
                    envstring = envstring.replaceFirst("\u0000.*", "");

                int eqlsign =
                    envstring.indexOf('=', ProcessEnvironment.MIN_NAME_LENGTH);
                // 静默忽略缺少必需的 `=` 的 envstrings。
                if (eqlsign != -1)
                    environment.put(envstring.substring(0,eqlsign),
                                    envstring.substring(eqlsign+1));
            }
        }
        return this;
    }

    /**
     * 返回此进程生成器的工作目录。
     *
     * 由此对象的 {@link
     * #start()} 方法随后启动的子进程将使用此目录作为其工作目录。
     * 返回的值可能为 {@code null} -- 这意味着使用当前 Java 进程的工作目录，通常是
     * 由系统属性 {@code user.dir} 指定的目录，
     * 作为子进程的工作目录。
     *
     * @return 此进程生成器的工作目录
     */
    public File directory() {
        return directory;
    }

    /**
     * 设置此进程生成器的工作目录。
     *
     * 由此对象的 {@link
     * #start()} 方法随后启动的子进程将使用此目录作为其工作目录。
     * 参数可能为 {@code null} -- 这意味着使用
     * 当前 Java 进程的工作目录，通常是
     * 由系统属性 {@code user.dir} 指定的目录，
     * 作为子进程的工作目录。
     *
     * @param  directory 新的工作目录
     * @return 此进程生成器
     */
    public ProcessBuilder directory(File directory) {
        this.directory = directory;
        return this;
    }

    // ---------------- I/O 重定向 ----------------

    /**
     * 实现一个 <a href="#redirect-output">空输入流</a>。
     */
    static class NullInputStream extends InputStream {
        static final NullInputStream INSTANCE = new NullInputStream();
        private NullInputStream() {}
        public int read()      { return -1; }
        public int available() { return 0; }
    }

    /**
     * 实现一个 <a href="#redirect-input">空输出流</a>。
     */
    static class NullOutputStream extends OutputStream {
        static final NullOutputStream INSTANCE = new NullOutputStream();
        private NullOutputStream() {}
        public void write(int b) throws IOException {
            throw new IOException("Stream closed");
        }
    }

    /**
     * 表示子进程输入的来源或子进程输出的目的地。
     *
     * 每个 {@code Redirect} 实例都是以下之一：
     *
     * <ul>
     * <li>特殊值 {@link #PIPE Redirect.PIPE}
     * <li>特殊值 {@link #INHERIT Redirect.INHERIT}
     * <li>从文件读取的重定向，由调用
     *     {@link Redirect#from Redirect.from(File)} 创建
     * <li>写入文件的重定向，由调用
     *     {@link Redirect#to Redirect.to(File)} 创建
     * <li>追加到文件的重定向，由调用
     *     {@link Redirect#appendTo Redirect.appendTo(File)} 创建
     * </ul>
     *
     * <p>上述每个类别的类型都有一个关联的唯一
     * {@link Type Type}。
     *
     * @since 1.7
     */
    public static abstract class Redirect {
        /**
         * {@link Redirect} 的类型。
         */
        public enum Type {
            /**
             * {@link Redirect#PIPE Redirect.PIPE} 的类型。
             */
            PIPE,

            /**
             * {@link Redirect#INHERIT Redirect.INHERIT} 的类型。
             */
            INHERIT,

            /**
             * 从 {@link Redirect#from Redirect.from(File)} 返回的重定向的类型。
             */
            READ,

            /**
             * 从 {@link Redirect#to Redirect.to(File)} 返回的重定向的类型。
             */
            WRITE,

            /**
             * 从 {@link Redirect#appendTo Redirect.appendTo(File)} 返回的重定向的类型。
             */
            APPEND
        };

        /**
         * 返回此 {@code Redirect} 的类型。
         * @return 此 {@code Redirect} 的类型
         */
        public abstract Type type();

        /**
         * 表示子进程 I/O 将通过管道连接到当前 Java 进程。
         *
         * 这是子进程标准 I/O 的默认处理方式。
         *
         * <p>以下条件始终为真
         *  <pre> {@code
         * Redirect.PIPE.file() == null &&
         * Redirect.PIPE.type() == Redirect.Type.PIPE
         * }</pre>
         */
        public static final Redirect PIPE = new Redirect() {
                public Type type() { return Type.PIPE; }
                public String toString() { return type().toString(); }};

        /**
         * 表示子进程 I/O 的源或目标将与当前进程相同。这是大多数操作系统命令解释器（shell）的正常行为。
         *
         * <p>以下条件始终为真
         *  <pre> {@code
         * Redirect.INHERIT.file() == null &&
         * Redirect.INHERIT.type() == Redirect.Type.INHERIT
         * }</pre>
         */
        public static final Redirect INHERIT = new Redirect() {
                public Type type() { return Type.INHERIT; }
                public String toString() { return type().toString(); }};


                        /**
         * 返回与此重定向关联的 {@link File} 源或目标，
         * 如果没有这样的文件，则返回 {@code null}。
         *
         * @return 与此重定向关联的文件，
         *         如果没有这样的文件，则返回 {@code null}
         */
        public File file() { return null; }

        /**
         * 当重定向到目标文件时，指示输出是否应写入文件的末尾。
         */
        boolean append() {
            throw new UnsupportedOperationException();
        }

        /**
         * 返回一个从指定文件读取的重定向。
         *
         * <p>以下条件总是成立的
         *  <pre> {@code
         * Redirect.from(file).file() == file &&
         * Redirect.from(file).type() == Redirect.Type.READ
         * }</pre>
         *
         * @param file 用于 {@code Redirect} 的 {@code File}。
         * @throws NullPointerException 如果指定的文件为 null
         * @return 一个从指定文件读取的重定向
         */
        public static Redirect from(final File file) {
            if (file == null)
                throw new NullPointerException();
            return new Redirect() {
                    public Type type() { return Type.READ; }
                    public File file() { return file; }
                    public String toString() {
                        return "redirect to read from file \"" + file + "\"";
                    }
                };
        }

        /**
         * 返回一个写入指定文件的重定向。
         * 如果在子进程启动时指定的文件已存在，
         * 则其先前的内容将被丢弃。
         *
         * <p>以下条件总是成立的
         *  <pre> {@code
         * Redirect.to(file).file() == file &&
         * Redirect.to(file).type() == Redirect.Type.WRITE
         * }</pre>
         *
         * @param file 用于 {@code Redirect} 的 {@code File}。
         * @throws NullPointerException 如果指定的文件为 null
         * @return 一个写入指定文件的重定向
         */
        public static Redirect to(final File file) {
            if (file == null)
                throw new NullPointerException();
            return new Redirect() {
                    public Type type() { return Type.WRITE; }
                    public File file() { return file; }
                    public String toString() {
                        return "redirect to write to file \"" + file + "\"";
                    }
                    boolean append() { return false; }
                };
        }

        /**
         * 返回一个追加到指定文件的重定向。
         * 每次写入操作首先将位置推进到文件的末尾，然后写入请求的数据。
         * 位置的推进和数据的写入是否作为一个单一的原子操作完成是系统依赖的，因此未指定。
         *
         * <p>以下条件总是成立的
         *  <pre> {@code
         * Redirect.appendTo(file).file() == file &&
         * Redirect.appendTo(file).type() == Redirect.Type.APPEND
         * }</pre>
         *
         * @param file 用于 {@code Redirect} 的 {@code File}。
         * @throws NullPointerException 如果指定的文件为 null
         * @return 一个追加到指定文件的重定向
         */
        public static Redirect appendTo(final File file) {
            if (file == null)
                throw new NullPointerException();
            return new Redirect() {
                    public Type type() { return Type.APPEND; }
                    public File file() { return file; }
                    public String toString() {
                        return "redirect to append to file \"" + file + "\"";
                    }
                    boolean append() { return true; }
                };
        }

        /**
         * 将指定的对象与此 {@code Redirect} 进行比较以确定相等性。仅当两个对象相同或都是相同类型的 {@code Redirect}
         * 实例，并且与非空且相等的 {@code File} 实例关联时，返回 {@code true}。
         */
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (! (obj instanceof Redirect))
                return false;
            Redirect r = (Redirect) obj;
            if (r.type() != this.type())
                return false;
            assert this.file() != null;
            return this.file().equals(r.file());
        }

        /**
         * 返回此 {@code Redirect} 的哈希码值。
         * @return 此 {@code Redirect} 的哈希码值
         */
        public int hashCode() {
            File file = file();
            if (file == null)
                return super.hashCode();
            else
                return file.hashCode();
        }

        /**
         * 没有公共构造函数。客户端必须使用预定义的
         * 静态 {@code Redirect} 实例或工厂方法。
         */
        private Redirect() {}
    }

    private Redirect[] redirects() {
        if (redirects == null)
            redirects = new Redirect[] {
                Redirect.PIPE, Redirect.PIPE, Redirect.PIPE
            };
        return redirects;
    }

    /**
     * 设置此进程构建器的标准输入源。
     *
     * 由此对象的 {@link #start()} 方法随后启动的子进程将从该源获取其标准输入。
     *
     * <p>如果源是 {@link Redirect#PIPE Redirect.PIPE}（初始值），则子进程的标准输入可以使用
     * {@link Process#getOutputStream()} 返回的输出流写入。
     * 如果源设置为任何其他值，则 {@link Process#getOutputStream()} 将返回一个
     * <a href="#redirect-input">null 输出流</a>。
     *
     * @param  source 新的标准输入源
     * @return 此进程构建器
     * @throws IllegalArgumentException
     *         如果重定向不对应于有效的数据源，即类型为
     *         {@link Redirect.Type#WRITE WRITE} 或
     *         {@link Redirect.Type#APPEND APPEND}
     * @since  1.7
     */
    public ProcessBuilder redirectInput(Redirect source) {
        if (source.type() == Redirect.Type.WRITE ||
            source.type() == Redirect.Type.APPEND)
            throw new IllegalArgumentException(
                "Redirect invalid for reading: " + source);
        redirects()[0] = source;
        return this;
    }


/**
 * 设置此进程生成器的标准输出目标。
 *
 * 由该对象的 {@link #start()} 方法随后启动的子进程将它们的标准输出发送到此目标。
 *
 * <p>如果目标是 {@link Redirect#PIPE Redirect.PIPE}
 * （初始值），则可以使用 {@link
 * Process#getInputStream()} 返回的输入流读取子进程的标准输出。
 * 如果目标设置为任何其他值，则
 * {@link Process#getInputStream()} 将返回一个
 * <a href="#redirect-output">空输入流</a>。
 *
 * @param  destination 新的标准输出目标
 * @return 此进程生成器
 * @throws IllegalArgumentException
 *         如果重定向不对应于有效的
 *         数据目标，即，类型为
 *         {@link Redirect.Type#READ READ}
 * @since  1.7
 */
public ProcessBuilder redirectOutput(Redirect destination) {
    if (destination.type() == Redirect.Type.READ)
        throw new IllegalArgumentException(
            "重定向无效，无法写入: " + destination);
    redirects()[1] = destination;
    return this;
}

/**
 * 设置此进程生成器的标准错误目标。
 *
 * 由该对象的 {@link #start()} 方法随后启动的子进程将它们的标准错误发送到此目标。
 *
 * <p>如果目标是 {@link Redirect#PIPE Redirect.PIPE}
 * （初始值），则可以使用 {@link
 * Process#getErrorStream()} 返回的输入流读取子进程的错误输出。
 * 如果目标设置为任何其他值，则
 * {@link Process#getErrorStream()} 将返回一个
 * <a href="#redirect-output">空输入流</a>。
 *
 * <p>如果 {@link #redirectErrorStream redirectErrorStream}
 * 属性已设置为 {@code true}，则此方法设置的重定向无效。
 *
 * @param  destination 新的标准错误目标
 * @return 此进程生成器
 * @throws IllegalArgumentException
 *         如果重定向不对应于有效的
 *         数据目标，即，类型为
 *         {@link Redirect.Type#READ READ}
 * @since  1.7
 */
public ProcessBuilder redirectError(Redirect destination) {
    if (destination.type() == Redirect.Type.READ)
        throw new IllegalArgumentException(
            "重定向无效，无法写入: " + destination);
    redirects()[2] = destination;
    return this;
}

/**
 * 将此进程生成器的标准输入源设置为文件。
 *
 * <p>这是一个便捷方法。形式为
 * {@code redirectInput(file)}
 * 的调用与
 * {@link #redirectInput(Redirect) redirectInput}
 * {@code (Redirect.from(file))} 的调用行为完全相同。
 *
 * @param  file 新的标准输入源
 * @return 此进程生成器
 * @since  1.7
 */
public ProcessBuilder redirectInput(File file) {
    return redirectInput(Redirect.from(file));
}

/**
 * 将此进程生成器的标准输出目标设置为文件。
 *
 * <p>这是一个便捷方法。形式为
 * {@code redirectOutput(file)}
 * 的调用与
 * {@link #redirectOutput(Redirect) redirectOutput}
 * {@code (Redirect.to(file))} 的调用行为完全相同。
 *
 * @param  file 新的标准输出目标
 * @return 此进程生成器
 * @since  1.7
 */
public ProcessBuilder redirectOutput(File file) {
    return redirectOutput(Redirect.to(file));
}

/**
 * 将此进程生成器的标准错误目标设置为文件。
 *
 * <p>这是一个便捷方法。形式为
 * {@code redirectError(file)}
 * 的调用与
 * {@link #redirectError(Redirect) redirectError}
 * {@code (Redirect.to(file))} 的调用行为完全相同。
 *
 * @param  file 新的标准错误目标
 * @return 此进程生成器
 * @since  1.7
 */
public ProcessBuilder redirectError(File file) {
    return redirectError(Redirect.to(file));
}

/**
 * 返回此进程生成器的标准输入源。
 *
 * 由该对象的 {@link #start()}
 * 方法随后启动的子进程从该源获取其标准输入。
 * 初始值为 {@link Redirect#PIPE Redirect.PIPE}。
 *
 * @return 此进程生成器的标准输入源
 * @since  1.7
 */
public Redirect redirectInput() {
    return (redirects == null) ? Redirect.PIPE : redirects[0];
}

/**
 * 返回此进程生成器的标准输出目标。
 *
 * 由该对象的 {@link #start()}
 * 方法随后启动的子进程将它们的标准输出重定向到此目标。
 * 初始值为 {@link Redirect#PIPE Redirect.PIPE}。
 *
 * @return 此进程生成器的标准输出目标
 * @since  1.7
 */
public Redirect redirectOutput() {
    return (redirects == null) ? Redirect.PIPE : redirects[1];
}

/**
 * 返回此进程生成器的标准错误目标。
 *
 * 由该对象的 {@link #start()}
 * 方法随后启动的子进程将它们的标准错误重定向到此目标。
 * 初始值为 {@link Redirect#PIPE Redirect.PIPE}。
 *
 * @return 此进程生成器的标准错误目标
 * @since  1.7
 */
public Redirect redirectError() {
    return (redirects == null) ? Redirect.PIPE : redirects[2];
}

/**
 * 将子进程标准 I/O 的源和目标设置为与当前 Java 进程相同。
 *
 * <p>这是一个便捷方法。形式为
 *  <pre> {@code
 * pb.inheritIO()
 * }</pre>
 * 的调用与
 *  <pre> {@code
 * pb.redirectInput(Redirect.INHERIT)
 *   .redirectOutput(Redirect.INHERIT)
 *   .redirectError(Redirect.INHERIT)
 * }</pre>
 * 的调用行为完全相同。
 *
 * 这提供了与大多数操作系统
 * 命令解释器或标准 C 库函数
 * {@code system()} 等效的行为。
 *
 * @return 此进程生成器
 * @since  1.7
 */
public ProcessBuilder inheritIO() {
    Arrays.fill(redirects(), Redirect.INHERIT);
    return this;
}

                    /**
     * 告诉此进程构建器是否合并标准错误和标准输出。
     *
     * <p>如果此属性为 {@code true}，则由此对象的
     * {@link #start()} 方法随后启动的子进程生成的任何错误输出
     * 将与标准输出合并，因此可以使用
     * {@link Process#getInputStream()} 方法读取两者。这使得
     * 错误消息与相应输出的关联更加容易。
     * 初始值为 {@code false}。
     *
     * @return 此进程构建器的 {@code redirectErrorStream} 属性
     */
    public boolean redirectErrorStream() {
        return redirectErrorStream;
    }

    /**
     * 设置此进程构建器的 {@code redirectErrorStream} 属性。
     *
     * <p>如果此属性为 {@code true}，则由此对象的
     * {@link #start()} 方法随后启动的子进程生成的任何错误输出
     * 将与标准输出合并，因此可以使用
     * {@link Process#getInputStream()} 方法读取两者。这使得
     * 错误消息与相应输出的关联更加容易。
     * 初始值为 {@code false}。
     *
     * @param  redirectErrorStream 新的属性值
     * @return 此进程构建器
     */
    public ProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    /**
     * 使用此进程构建器的属性启动新进程。
     *
     * <p>新进程将
     * 调用由 {@link #command()} 给出的命令和参数，
     * 在由 {@link #directory()} 给出的工作目录中，
     * 以及由 {@link #environment()} 给出的进程环境。
     *
     * <p>此方法检查命令是否为有效的操作系统命令。哪些命令有效是系统依赖的，
     * 但至少命令必须是非空的非空字符串列表。
     *
     * <p>在某些操作系统上，可能需要最小的系统依赖环境变量集来启动进程。
     * 因此，子进程可能继承额外的环境变量设置，超出进程构建器的 {@link #environment()}。
     *
     * <p>如果有安全管理者，其
     * {@link SecurityManager#checkExec checkExec}
     * 方法将使用此对象的
     * {@code command} 数组的第一个组件作为参数调用。这可能导致
     * 抛出 {@link SecurityException}。
     *
     * <p>启动操作系统进程高度依赖于系统。可能出现的问题包括：
     * <ul>
     * <li>操作系统程序文件未找到。
     * <li>访问程序文件被拒绝。
     * <li>工作目录不存在。
     * </ul>
     *
     * <p>在这种情况下将抛出异常。异常的具体性质是系统依赖的，但总是
     * {@link IOException} 的子类。
     *
     * <p>对本进程构建器的后续修改不会影响返回的 {@link Process}。
     *
     * @return 用于管理子进程的新 {@link Process} 对象
     *
     * @throws NullPointerException
     *         如果命令列表中的元素为 null
     *
     * @throws IndexOutOfBoundsException
     *         如果命令是空列表（大小为 {@code 0}）
     *
     * @throws SecurityException
     *         如果存在安全管理者，并且
     *         <ul>
     *
     *         <li>其
     *         {@link SecurityManager#checkExec checkExec}
     *         方法不允许创建子进程，或者
     *
     *         <li>子进程的标准输入
     *         {@linkplain #redirectInput 从文件重定向}
     *         而安全管理者的
     *         {@link SecurityManager#checkRead checkRead} 方法
     *         拒绝读取文件的访问，或者
     *
     *         <li>子进程的标准输出或标准错误
     *         {@linkplain #redirectOutput 重定向到文件}
     *         而安全管理者的
     *         {@link SecurityManager#checkWrite checkWrite} 方法
     *         拒绝写入文件的访问
     *
     *         </ul>
     *
     * @throws IOException 如果发生 I/O 错误
     *
     * @see Runtime#exec(String[], String[], java.io.File)
     */
    public Process start() throws IOException {
        // 必须先转换为数组——恶意用户提供的列表可能试图绕过安全检查。
        String[] cmdarray = command.toArray(new String[command.size()]);
        cmdarray = cmdarray.clone();

        for (String arg : cmdarray)
            if (arg == null)
                throw new NullPointerException();
        // 如果命令为空，则抛出 IndexOutOfBoundsException
        String prog = cmdarray[0];

        SecurityManager security = System.getSecurityManager();
        if (security != null)
            security.checkExec(prog);

        String dir = directory == null ? null : directory.toString();

        for (int i = 1; i < cmdarray.length; i++) {
            if (cmdarray[i].indexOf('\u0000') >= 0) {
                throw new IOException("命令中包含无效的空字符");
            }
        }

        try {
            return ProcessImpl.start(cmdarray,
                                     environment,
                                     dir,
                                     redirects,
                                     redirectErrorStream);
        } catch (IOException | IllegalArgumentException e) {
            String exceptionInfo = ": " + e.getMessage();
            Throwable cause = e;
            if ((e instanceof IOException) && security != null) {
                // 不能披露受保护文件的失败原因。
                try {
                    security.checkRead(prog);
                } catch (SecurityException se) {
                    exceptionInfo = "";
                    cause = se;
                }
            }
            // 由我们创建高质量的错误消息比低级 C 代码更容易。
            throw new IOException(
                "无法运行程序 \"" + prog + "\""
                + (dir == null ? "" : " (在目录 \"" + dir + "\")")
                + exceptionInfo,
                cause);
        }
    }
}
