
/*
 * 版权所有 (c) 1995, 2019，Oracle 和/或其附属公司。保留所有权利。
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

package java.lang;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetPropertyAction;

/* 本类仅供 ProcessBuilder.start() 使用，以创建新进程。
 *
 * @author Martin Buchholz
 * @since   1.5
 */

final class ProcessImpl extends Process {
    private static final sun.misc.JavaIOFileDescriptorAccess fdAccess
        = sun.misc.SharedSecrets.getJavaIOFileDescriptorAccess();

    /**
     * 打开文件以供写入。如果 {@code append} 为 {@code true}，则文件将直接以原子追加模式打开，并使用生成的句柄构造 FileOutputStream。
     * 这是因为创建用于追加到文件的 FileOutputStream 不会以保证子进程写入操作为原子的方式打开文件。
     */
    private static FileOutputStream newFileOutputStream(File f, boolean append)
        throws IOException
    {
        if (append) {
            String path = f.getPath();
            SecurityManager sm = System.getSecurityManager();
            if (sm != null)
                sm.checkWrite(path);
            long handle = openForAtomicAppend(path);
            final FileDescriptor fd = new FileDescriptor();
            fdAccess.setHandle(fd, handle);
            return AccessController.doPrivileged(
                new PrivilegedAction<FileOutputStream>() {
                    public FileOutputStream run() {
                        return new FileOutputStream(fd);
                    }
                }
            );
        } else {
            return new FileOutputStream(f);
        }
    }

    // ProcessBuilder.start() 的系统依赖部分
    static Process start(String cmdarray[],
                         java.util.Map<String,String> environment,
                         String dir,
                         ProcessBuilder.Redirect[] redirects,
                         boolean redirectErrorStream)
        throws IOException
    {
        String envblock = ProcessEnvironment.toEnvironmentBlock(environment);

        FileInputStream  f0 = null;
        FileOutputStream f1 = null;
        FileOutputStream f2 = null;

        try {
            long[] stdHandles;
            if (redirects == null) {
                stdHandles = new long[] { -1L, -1L, -1L };
            } else {
                stdHandles = new long[3];

                if (redirects[0] == Redirect.PIPE)
                    stdHandles[0] = -1L;
                else if (redirects[0] == Redirect.INHERIT)
                    stdHandles[0] = fdAccess.getHandle(FileDescriptor.in);
                else {
                    f0 = new FileInputStream(redirects[0].file());
                    stdHandles[0] = fdAccess.getHandle(f0.getFD());
                }

                if (redirects[1] == Redirect.PIPE)
                    stdHandles[1] = -1L;
                else if (redirects[1] == Redirect.INHERIT)
                    stdHandles[1] = fdAccess.getHandle(FileDescriptor.out);
                else {
                    f1 = newFileOutputStream(redirects[1].file(),
                                             redirects[1].append());
                    stdHandles[1] = fdAccess.getHandle(f1.getFD());
                }

                if (redirects[2] == Redirect.PIPE)
                    stdHandles[2] = -1L;
                else if (redirects[2] == Redirect.INHERIT)
                    stdHandles[2] = fdAccess.getHandle(FileDescriptor.err);
                else {
                    f2 = newFileOutputStream(redirects[2].file(),
                                             redirects[2].append());
                    stdHandles[2] = fdAccess.getHandle(f2.getFD());
                }
            }

            return new ProcessImpl(cmdarray, envblock, dir,
                                   stdHandles, redirectErrorStream);
        } finally {
            // 理论上，close() 可能会抛出 IOException
            // （尽管在这里发生的可能性不大）
            try { if (f0 != null) f0.close(); }
            finally {
                try { if (f1 != null) f1.close(); }
                finally { if (f2 != null) f2.close(); }
            }
        }

    }

    private static class LazyPattern {
        // 支持转义的版本：
        //    "(\")((?:\\\\\\1|.)+?)\\1|([^\\s\"]+)";
        private static final Pattern PATTERN =
            Pattern.compile("[^\\s\"]+|\"[^\"]*\"");
    };

    /* 解析命令字符串参数为可执行文件名和程序参数。
     *
     * 命令字符串被分解成标记。标记分隔符是空格或引号字符。引号内的空格不是标记分隔符。
     * 没有转义序列。
     */
    private static String[] getTokensFromCommand(String command) {
        ArrayList<String> matchList = new ArrayList<>(8);
        Matcher regexMatcher = LazyPattern.PATTERN.matcher(command);
        while (regexMatcher.find())
            matchList.add(regexMatcher.group());
        return matchList.toArray(new String[matchList.size()]);
    }

    private static final int VERIFICATION_CMD_BAT = 0;
    private static final int VERIFICATION_WIN32 = 1;
    private static final int VERIFICATION_WIN32_SAFE = 2; // 不允许在引号内
    private static final int VERIFICATION_LEGACY = 3;
    // 有关特殊字符的文档，请参阅命令 shell 概述。
    // https://docs.microsoft.com/en-us/previous-versions/windows/it-pro/windows-xp/bb490954(v=technet.10)
    private static final char ESCAPE_VERIFICATION[][] = {
        // 我们保证仅在隐式 [cmd.exe] 运行时执行命令文件。
        //    http://technet.microsoft.com/en-us/library/bb490954.aspx
        {' ', '\t', '\"', '<', '>', '&', '|', '^'},
        {' ', '\t', '\"', '<', '>'},
        {' ', '\t', '\"', '<', '>'},
        {' ', '\t'}
    };


private static String createCommandLine(int verificationType,
                                     final String executablePath,
                                     final String cmd[])
    {
        StringBuilder cmdbuf = new StringBuilder(80);

        cmdbuf.append(executablePath);

        for (int i = 1; i < cmd.length; ++i) {
            cmdbuf.append(' ');
            String s = cmd[i];
            if (needsEscaping(verificationType, s)) {
                cmdbuf.append('"');

                if (verificationType == VERIFICATION_WIN32_SAFE) {
                    // 插入参数，添加 '\' 以引用任何内部引号
                    int length = s.length();
                    for (int j = 0; j < length; j++) {
                        char c = s.charAt(j);
                        if (c == DOUBLEQUOTE) {
                            int count = countLeadingBackslash(verificationType, s, j);
                            while (count-- > 0) {
                                cmdbuf.append(BACKSLASH);   // 将反斜杠的数量翻倍
                            }
                            cmdbuf.append(BACKSLASH);       // 反斜杠以引用引号
                        }
                        cmdbuf.append(c);
                    }
                } else {
                    cmdbuf.append(s);
                }
                // 该代码保护 [java.exe] 和控制台命令行解析器，
                // 该解析器将 [\"] 组合解释为 ["] 字符的转义序列。
                //     http://msdn.microsoft.com/en-us/library/17w5ykft.aspx
                //
                // 如果参数是文件系统路径，则非控制台应用程序中的尾部 [\] 字符的翻倍不是问题。
                //
                // [\"] 序列不是 [cmd.exe] 命令行解析器的转义序列。由于参数验证过程，
                // [""] 尾部转义序列的情况无法实现。
                int count = countLeadingBackslash(verificationType, s, s.length());
                while (count-- > 0) {
                    cmdbuf.append(BACKSLASH);   // 将反斜杠的数量翻倍
                }
                cmdbuf.append('"');
            } else {
                cmdbuf.append(s);
            }
        }
        return cmdbuf.toString();
    }

    /**
     * 如果字符串正确引用，则返回不带引号（第一个和最后一个）的参数，否则返回参数。
     * 正确引用的字符串的第一个和最后一个字符为引号，
     * 且最后一个引号未被转义。
     * @param str 一个字符串
     * @return 不带引号的字符串
     */
    private static String unQuote(String str) {
        if (!str.startsWith("\"") || !str.endsWith("\"") || str.length() < 2)
            return str;    // 没有开始或结束引号，或太短未被引用

        if (str.endsWith("\\\"")) {
            return str;    // 未正确引用，视为未引用
        }
        // 去除首尾引号
        return str.substring(1, str.length() - 1);
    }

    private static boolean needsEscaping(int verificationType, String arg) {
        if (arg.isEmpty())
            return true;            // 空字符串需要引用

        // 关闭 MS 对内部 ["] 的启发式处理。
        // 如果需要内部 ["]，请使用显式的 [cmd.exe] 调用。
        //    示例: "cmd.exe", "/C", "Extended_MS_Syntax"

        // 对于 [.exe] 或 [.com] 文件，参数中的未配对/内部 ["]
        // 不是问题。
        String unquotedArg = unQuote(arg);
        boolean argIsQuoted = !arg.equals(unquotedArg);
        boolean embeddedQuote = unquotedArg.indexOf(DOUBLEQUOTE) >= 0;

        switch (verificationType) {
            case VERIFICATION_CMD_BAT:
                if (embeddedQuote) {
                    throw new IllegalArgumentException("参数包含嵌入的引号，" +
                            "使用显式的 CMD.EXE 调用。");
                }
                break;  // 确定是否需要引用
            case VERIFICATION_WIN32_SAFE:
                if (argIsQuoted && embeddedQuote)  {
                    throw new IllegalArgumentException("格式错误的参数包含嵌入的引号: "
                            + unquotedArg);
                }
                break;
            default:
                break;
        }

        if (!argIsQuoted) {
            char testEscape[] = ESCAPE_VERIFICATION[verificationType];
            for (int i = 0; i < testEscape.length; ++i) {
                if (arg.indexOf(testEscape[i]) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getExecutablePath(String path)
        throws IOException
    {
        String name = unQuote(path);
        if (name.indexOf(DOUBLEQUOTE) >= 0) {
            throw new IllegalArgumentException("可执行文件名包含嵌入的引号，" +
                    "拆分参数: " + name);
        }
        // Win32 CreateProcess 需要路径被规范化
        File fileToRun = new File(name);

        // 从 [CreateProcess] 函数文档：
        //
        // "如果文件名不包含扩展名，则附加 .exe。
        // 因此，如果文件名扩展名为 .com，此参数
        // 必须包含 .com 扩展名。如果文件名以
        // 一个句点 (.) 结尾且没有扩展名，或者文件名包含路径，
        // 则不附加 .exe。"
        //
        // "如果文件名 !不包含目录路径!,
        // 系统将按以下顺序搜索可执行文件：..."
        //
        // 实际上，任何不存在的路径都会在 [CreateProcess] 函数中
        // 通过 [.exe] 扩展名进行扩展，唯一的例外是：
        // 路径以 (.) 结尾。

        return fileToRun.getPath();
    }

    /**
     * 可执行文件是任何扩展名为 .exe 或没有扩展名的程序，
     * Windows createProcess 将查找 .exe。
     * 比较基于名称，不区分大小写。
     * @param executablePath 可执行文件
     * @return 如果路径以 .exe 结尾或没有扩展名，则返回 true。
     */
    private boolean isExe(String executablePath) {
        File file = new File(executablePath);
        String upName = file.getName().toUpperCase(Locale.ROOT);
        return (upName.endsWith(".EXE") || upName.indexOf('.') < 0);
    }


                    // 旧版本，可以绕过
    private boolean isShellFile(String executablePath) {
        String upPath = executablePath.toUpperCase();
        return (upPath.endsWith(".CMD") || upPath.endsWith(".BAT"));
    }

    private String quoteString(String arg) {
        StringBuilder argbuf = new StringBuilder(arg.length() + 2);
        return argbuf.append('"').append(arg).append('"').toString();
    }

    // 计算字符串开始索引前的反斜杠数量。
    // .bat 文件不将反斜杠作为引号的一部分
    private static int countLeadingBackslash(int verificationType,
                                             CharSequence input, int start) {
        if (verificationType == VERIFICATION_CMD_BAT)
            return 0;
        int j;
        for (j = start - 1; j >= 0 && input.charAt(j) == BACKSLASH; j--) {
            // 仅向后扫描
        }
        return (start - 1) - j;  // 反斜杠的数量
    }

    private static final char DOUBLEQUOTE = '\"';
    private static final char BACKSLASH = '\\';

    private long handle = 0;
    private OutputStream stdin_stream;
    private InputStream stdout_stream;
    private InputStream stderr_stream;

    private ProcessImpl(String cmd[],
                        final String envblock,
                        final String path,
                        final long[] stdHandles,
                        final boolean redirectErrorStream)
        throws IOException
    {
        String cmdstr;
        final SecurityManager security = System.getSecurityManager();
        final String value = GetPropertyAction.
                privilegedGetProperty("jdk.lang.Process.allowAmbiguousCommands",
                        (security == null ? "true" : "false"));
        final boolean allowAmbiguousCommands = !"false".equalsIgnoreCase(value);

        if (allowAmbiguousCommands && security == null) {
            // 传统模式。

            // 尽可能规范化路径。
            String executablePath = new File(cmd[0]).getPath();

            // 不需要担心内部未配对的 ["] 和重定向/管道。
            if (needsEscaping(VERIFICATION_LEGACY, executablePath) )
                executablePath = quoteString(executablePath);

            cmdstr = createCommandLine(
                // 传统模式不担心扩展验证
                VERIFICATION_LEGACY,
                executablePath,
                cmd);
        } else {
            String executablePath;
            try {
                executablePath = getExecutablePath(cmd[0]);
            } catch (IllegalArgumentException e) {
                // 解决类似以下调用的问题
                // Runtime.getRuntime().exec("\"C:\\Program Files\\foo\" bar")

                // 除了从一开始就正确处理外，几乎无法避免 CMD/BAT 注入，否则我们会有太多来自
                //    Runtime.getRuntime().exec(String[] cmd [, ...])
                // 调用的边缘情况，其中包含内部 ["] 和转义序列。

                // 恢复原始命令行。
                StringBuilder join = new StringBuilder();
                // 命令行末尾的空格是可以的
                for (String s : cmd)
                    join.append(s).append(' ');

                // 再次解析命令行。
                cmd = getTokensFromCommand(join.toString());
                executablePath = getExecutablePath(cmd[0]);

                // 再次检查新的可执行文件名
                if (security != null)
                    security.checkExec(executablePath);
            }

            // 引号可以防止将 [path] 参数解释为更长路径的开始，其中包含空格。引号不会影响
            // [.exe] 扩展名的启发式。
            boolean isShell = allowAmbiguousCommands ? isShellFile(executablePath)
                    : !isExe(executablePath);
            cmdstr = createCommandLine(
                    // 我们需要扩展的验证程序
                    isShell ? VERIFICATION_CMD_BAT
                            : (allowAmbiguousCommands ? VERIFICATION_WIN32 : VERIFICATION_WIN32_SAFE),
                    quoteString(executablePath),
                    cmd);
        }

        handle = create(cmdstr, envblock, path,
                        stdHandles, redirectErrorStream);

        java.security.AccessController.doPrivileged(
        new java.security.PrivilegedAction<Void>() {
        public Void run() {
            if (stdHandles[0] == -1L)
                stdin_stream = ProcessBuilder.NullOutputStream.INSTANCE;
            else {
                FileDescriptor stdin_fd = new FileDescriptor();
                fdAccess.setHandle(stdin_fd, stdHandles[0]);
                stdin_stream = new BufferedOutputStream(
                    new FileOutputStream(stdin_fd));
            }

            if (stdHandles[1] == -1L)
                stdout_stream = ProcessBuilder.NullInputStream.INSTANCE;
            else {
                FileDescriptor stdout_fd = new FileDescriptor();
                fdAccess.setHandle(stdout_fd, stdHandles[1]);
                stdout_stream = new BufferedInputStream(
                    new FileInputStream(stdout_fd));
            }

            if (stdHandles[2] == -1L)
                stderr_stream = ProcessBuilder.NullInputStream.INSTANCE;
            else {
                FileDescriptor stderr_fd = new FileDescriptor();
                fdAccess.setHandle(stderr_fd, stdHandles[2]);
                stderr_stream = new FileInputStream(stderr_fd);
            }

            return null; }});
    }

    public OutputStream getOutputStream() {
        return stdin_stream;
    }

    public InputStream getInputStream() {
        return stdout_stream;
    }

    public InputStream getErrorStream() {
        return stderr_stream;
    }

    protected void finalize() {
        closeHandle(handle);
    }

    private static final int STILL_ACTIVE = getStillActive();
    private static native int getStillActive();


                    public int exitValue() {
        int exitCode = getExitCodeProcess(handle);
        if (exitCode == STILL_ACTIVE)
            throw new IllegalThreadStateException("process has not exited");
        return exitCode;
    }
    private static native int getExitCodeProcess(long handle);

    public int waitFor() throws InterruptedException {
        waitForInterruptibly(handle);
        if (Thread.interrupted())
            throw new InterruptedException();
        return exitValue();
    }

    private static native void waitForInterruptibly(long handle);

    @Override
    public boolean waitFor(long timeout, TimeUnit unit)
        throws InterruptedException
    {
        long remainingNanos = unit.toNanos(timeout);    // throw NPE before other conditions
        if (getExitCodeProcess(handle) != STILL_ACTIVE) return true;
        if (timeout <= 0) return false;

        long deadline = System.nanoTime() + remainingNanos;
        do {
            // Round up to next millisecond
            long msTimeout = TimeUnit.NANOSECONDS.toMillis(remainingNanos + 999_999L);
            if (msTimeout < 0) {
                // if wraps around then wait a long while
                msTimeout = Integer.MAX_VALUE;
            }
            waitForTimeoutInterruptibly(handle, msTimeout);
            if (Thread.interrupted())
                throw new InterruptedException();
            if (getExitCodeProcess(handle) != STILL_ACTIVE) {
                return true;
            }
            remainingNanos = deadline - System.nanoTime();
        } while (remainingNanos > 0);

        return (getExitCodeProcess(handle) != STILL_ACTIVE);
    }

    private static native void waitForTimeoutInterruptibly(
        long handle, long timeoutMillis);

    public void destroy() { terminateProcess(handle); }

    @Override
    public Process destroyForcibly() {
        destroy();
        return this;
    }

    private static native void terminateProcess(long handle);

    @Override
    public boolean isAlive() {
        return isProcessAlive(handle);
    }

    private static native boolean isProcessAlive(long handle);

    /**
     * 使用 win32 函数 CreateProcess 创建一个进程。
     * 该方法是同步的，因为存在 MS kb315939 问题。
     * 所有本机句柄应在调用结束时恢复继承标志。
     *
     * @param cmdstr Windows 命令行
     * @param envblock 以 NUL 分隔、双 NUL 结尾的环境字符串列表，格式为 VAR=VALUE
     * @param dir 进程的工作目录，如果为 null，则从父进程继承当前目录
     * @param stdHandles 窗口句柄数组。索引 0、1 和 2 分别对应标准输入、标准输出和标准错误。输入时值为 -1 表示创建一个管道以连接子进程和父进程。输出时值不为 -1 表示已创建的管道的父句柄。数组的某个元素在输入时为 -1 当且仅当它在输出时不为 -1。
     * @param redirectErrorStream redirectErrorStream 属性
     * @return CreateProcess 返回的本机子进程句柄
     */
    private static synchronized native long create(String cmdstr,
                                      String envblock,
                                      String dir,
                                      long[] stdHandles,
                                      boolean redirectErrorStream)
        throws IOException;

    /**
     * 以原子追加方式打开文件。如果文件不存在，则创建文件。
     *
     * @param file 要打开或创建的文件
     * @return 本机句柄
     */
    private static native long openForAtomicAppend(String path)
        throws IOException;

    private static native boolean closeHandle(long handle);
}
