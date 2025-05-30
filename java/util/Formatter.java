


                private IOException lastException;

    private final char zero;
    private static double scaleUp;

    // 1 (符号) + 19 (最大有效数字) + 1 ('.') + 1 ('e') + 1 (符号)
    // + 3 (最大指数位数) + 4 (错误) = 30
    private static final int MAX_FD_CHARS = 30;

    /**
     * 返回给定字符集名称的字符集对象。
     * @throws NullPointerException          如果 csn 为 null
     * @throws UnsupportedEncodingException  如果字符集不受支持
     */
    private static Charset toCharset(String csn)
        throws UnsupportedEncodingException
    {
        Objects.requireNonNull(csn, "charsetName");
        try {
            return Charset.forName(csn);
        } catch (IllegalCharsetNameException|UnsupportedCharsetException unused) {
            // 应该抛出 UnsupportedEncodingException
            throw new UnsupportedEncodingException(csn);
        }
    }

    private static final Appendable nonNullAppendable(Appendable a) {
        if (a == null)
            return new StringBuilder();

        return a;
    }

    /* 私有构造函数 */
    private Formatter(Locale l, Appendable a) {
        this.a = a;
        this.l = l;
        this.zero = getZero(l);
    }

    private Formatter(Charset charset, Locale l, File file)
        throws FileNotFoundException
    {
        this(l,
             new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset)));
    }

    /**
     * 构造一个新的格式化器。
     *
     * <p> 格式化输出的目标是一个 {@link StringBuilder}，可以通过调用 {@link #out out()} 获取，并且可以通过调用 {@link
     * #toString toString()} 将其当前内容转换为字符串。使用的区域设置是此 Java 虚拟机实例的 {@linkplain
     * Locale#getDefault(Locale.Category) 默认区域设置}，用于 {@linkplain Locale.Category#FORMAT 格式化}。
     */
    public Formatter() {
        this(Locale.getDefault(Locale.Category.FORMAT), new StringBuilder());
    }

    /**
     * 构造一个新的格式化器，指定目标。
     *
     * <p> 使用的区域设置是此 Java 虚拟机实例的 {@linkplain
     * Locale#getDefault(Locale.Category) 默认区域设置}，用于 {@linkplain Locale.Category#FORMAT 格式化}。
     *
     * @param  a
     *         格式化输出的目标。如果 {@code a} 为 {@code null}，则创建一个 {@link StringBuilder}。
     */
    public Formatter(Appendable a) {
        this(Locale.getDefault(Locale.Category.FORMAT), nonNullAppendable(a));
    }

    /**
     * 构造一个新的格式化器，指定区域设置。
     *
     * <p> 格式化输出的目标是一个 {@link StringBuilder}，可以通过调用 {@link #out out()} 获取，其当前内容可以通过调用 {@link #toString
     * toString()} 转换为字符串。
     *
     * @param  l
     *         在格式化过程中应用的 {@linkplain java.util.Locale 区域设置}。如果 {@code l} 为 {@code null}，则不应用本地化。
     */
    public Formatter(Locale l) {
        this(l, new StringBuilder());
    }

    /**
     * 构造一个新的格式化器，指定目标和区域设置。
     *
     * @param  a
     *         格式化输出的目标。如果 {@code a} 为 {@code null}，则创建一个 {@link StringBuilder}。
     *
     * @param  l
     *         在格式化过程中应用的 {@linkplain java.util.Locale 区域设置}。如果 {@code l} 为 {@code null}，则不应用本地化。
     */
    public Formatter(Appendable a, Locale l) {
        this(l, nonNullAppendable(a));
    }

    /**
     * 构造一个新的格式化器，指定文件名。
     *
     * <p> 使用的字符集是此 Java 虚拟机实例的 {@linkplain
     * java.nio.charset.Charset#defaultCharset() 默认字符集}。
     *
     * <p> 使用的区域设置是此 Java 虚拟机实例的 {@linkplain
     * Locale#getDefault(Locale.Category) 默认区域设置}，用于 {@linkplain Locale.Category#FORMAT 格式化}。
     *
     * @param  fileName
     *         用作此格式化器目标的文件名。如果文件存在，则将其截断为零大小；否则，将创建一个新文件。输出将写入文件并进行缓冲。
     *
     * @throws  SecurityException
     *          如果存在安全管理器且 {@link
     *          SecurityManager#checkWrite checkWrite(fileName)} 拒绝写入文件的权限
     *
     * @throws  FileNotFoundException
     *          如果给定的文件名不表示一个现有的、可写的普通文件，并且无法创建一个同名的新普通文件，或者在打开或创建文件时发生其他错误
     */
    public Formatter(String fileName) throws FileNotFoundException {
        this(Locale.getDefault(Locale.Category.FORMAT),
             new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName))));
    }

    /**
     * 构造一个新的格式化器，指定文件名和字符集。
     *
     * <p> 使用的区域设置是此 Java 虚拟机实例的 {@linkplain
     * Locale#getDefault(Locale.Category) 默认区域设置}，用于 {@linkplain Locale.Category#FORMAT 格式化}。
     *
     * @param  fileName
     *         用作此格式化器目标的文件名。如果文件存在，则将其截断为零大小；否则，将创建一个新文件。输出将写入文件并进行缓冲。
     *
     * @param  csn
     *         支持的 {@linkplain java.nio.charset.Charset 字符集} 的名称
     *
     * @throws  FileNotFoundException
     *          如果给定的文件名不表示一个现有的、可写的普通文件，并且无法创建一个同名的新普通文件，或者在打开或创建文件时发生其他错误
     *
     * @throws  SecurityException
     *          如果存在安全管理器且 {@link
     *          SecurityManager#checkWrite checkWrite(fileName)} 拒绝写入文件的权限
     *
     * @throws  UnsupportedEncodingException
     *          如果命名的字符集不受支持
     */
    public Formatter(String fileName, String csn)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        this(fileName, csn, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 构造一个新的格式化器，指定文件名、字符集和区域设置。
     *
     * @param  fileName
     *         用作此格式化器目标的文件名。如果文件存在，则将其截断为零大小；否则，将创建一个新文件。输出将写入文件并进行缓冲。
     *
     * @param  csn
     *         支持的 {@linkplain java.nio.charset.Charset 字符集} 的名称
     *
     * @param  l
     *         在格式化过程中应用的 {@linkplain java.util.Locale 区域设置}。如果 {@code l} 为 {@code null}，则不应用本地化。
     *
     * @throws  FileNotFoundException
     *          如果给定的文件名不表示一个现有的、可写的普通文件，并且无法创建一个同名的新普通文件，或者在打开或创建文件时发生其他错误
     *
     * @throws  SecurityException
     *          如果存在安全管理器且 {@link
     *          SecurityManager#checkWrite checkWrite(fileName)} 拒绝写入文件的权限
     *
     * @throws  UnsupportedEncodingException
     *          如果命名的字符集不受支持
     */
    public Formatter(String fileName, String csn, Locale l)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        this(toCharset(csn), l, new File(fileName));
    }

    /**
     * 构造一个新的格式化器，指定文件。
     *
     * <p> 使用的字符集是此 Java 虚拟机实例的 {@linkplain
     * java.nio.charset.Charset#defaultCharset() 默认字符集}。
     *
     * <p> 使用的区域设置是此 Java 虚拟机实例的 {@linkplain
     * Locale#getDefault(Locale.Category) 默认区域设置}，用于 {@linkplain Locale.Category#FORMAT 格式化}。
     *
     * @param  file
     *         用作此格式化器目标的文件。如果文件存在，则将其截断为零大小；否则，将创建一个新文件。输出将写入文件并进行缓冲。
     *
     * @throws  SecurityException
     *          如果存在安全管理器且 {@link
     *          SecurityManager#checkWrite checkWrite(file.getPath())} 拒绝写入文件的权限
     *
     * @throws  FileNotFoundException
     *          如果给定的文件对象不表示一个现有的、可写的普通文件，并且无法创建一个同名的新普通文件，或者在打开或创建文件时发生其他错误
     */
    public Formatter(File file) throws FileNotFoundException {
        this(Locale.getDefault(Locale.Category.FORMAT),
             new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))));
    }

    /**
     * 构造一个新的格式化器，指定文件和字符集。
     *
     * <p> 使用的区域设置是此 Java 虚拟机实例的 {@linkplain
     * Locale#getDefault(Locale.Category) 默认区域设置}，用于 {@linkplain Locale.Category#FORMAT 格式化}。
     *
     * @param  file
     *         用作此格式化器目标的文件。如果文件存在，则将其截断为零大小；否则，将创建一个新文件。输出将写入文件并进行缓冲。
     *
     * @param  csn
     *         支持的 {@linkplain java.nio.charset.Charset 字符集} 的名称
     *
     * @throws  FileNotFoundException
     *          如果给定的文件对象不表示一个现有的、可写的普通文件，并且无法创建一个同名的新普通文件，或者在打开或创建文件时发生其他错误
     *
     * @throws  SecurityException
     *          如果存在安全管理器且 {@link
     *          SecurityManager#checkWrite checkWrite(file.getPath())} 拒绝写入文件的权限
     *
     * @throws  UnsupportedEncodingException
     *          如果命名的字符集不受支持
     */
    public Formatter(File file, String csn)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        this(file, csn, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 构造一个新的格式化器，指定文件、字符集和区域设置。
     *
     * @param  file
     *         用作此格式化器目标的文件。如果文件存在，则将其截断为零大小；否则，将创建一个新文件。输出将写入文件并进行缓冲。
     *
     * @param  csn
     *         支持的 {@linkplain java.nio.charset.Charset 字符集} 的名称
     *
     * @param  l
     *         在格式化过程中应用的 {@linkplain java.util.Locale 区域设置}。如果 {@code l} 为 {@code null}，则不应用本地化。
     *
     * @throws  FileNotFoundException
     *          如果给定的文件对象不表示一个现有的、可写的普通文件，并且无法创建一个同名的新普通文件，或者在打开或创建文件时发生其他错误
     *
     * @throws  SecurityException
     *          如果存在安全管理器且 {@link
     *          SecurityManager#checkWrite checkWrite(file.getPath())} 拒绝写入文件的权限
     *
     * @throws  UnsupportedEncodingException
     *          如果命名的字符集不受支持
     */
    public Formatter(File file, String csn, Locale l)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        this(toCharset(csn), l, file);
    }

    /**
     * 构造一个新的格式化器，指定打印流。
     *
     * <p> 使用的区域设置是此 Java 虚拟机实例的 {@linkplain
     * Locale#getDefault(Locale.Category) 默认区域设置}，用于 {@linkplain Locale.Category#FORMAT 格式化}。
     *
     * <p> 字符将写入给定的 {@link java.io.PrintStream PrintStream} 对象，并因此使用该对象的字符集进行编码。
     *
     * @param  ps
     *         用作此格式化器目标的流。
     */
    public Formatter(PrintStream ps) {
        this(Locale.getDefault(Locale.Category.FORMAT),
             (Appendable)Objects.requireNonNull(ps));
    }

    /**
     * 构造一个新的格式化器，指定输出流。
     *
     * <p> 使用的字符集是此 Java 虚拟机实例的 {@linkplain
     * java.nio.charset.Charset#defaultCharset() 默认字符集}。
     *
     * <p> 使用的区域设置是此 Java 虚拟机实例的 {@linkplain
     * Locale#getDefault(Locale.Category) 默认区域设置}，用于 {@linkplain Locale.Category#FORMAT 格式化}。
     *
     * @param  os
     *         用作此格式化器目标的输出流。输出将进行缓冲。
     */
    public Formatter(OutputStream os) {
        this(Locale.getDefault(Locale.Category.FORMAT),
             new BufferedWriter(new OutputStreamWriter(os)));
    }

    /**
     * 构造一个新的格式化器，指定输出流和字符集。
     *
     * <p> 使用的区域设置是此 Java 虚拟机实例的 {@linkplain
     * Locale#getDefault(Locale.Category) 默认区域设置}，用于 {@linkplain Locale.Category#FORMAT 格式化}。
     *
     * @param  os
     *         用作此格式化器目标的输出流。输出将进行缓冲。
     *
     * @param  csn
     *         支持的 {@linkplain java.nio.charset.Charset 字符集} 的名称
     *
     * @throws  UnsupportedEncodingException
     *          如果命名的字符集不受支持
     */
    public Formatter(OutputStream os, String csn)
        throws UnsupportedEncodingException
    {
        this(os, csn, Locale.getDefault(Locale.Category.FORMAT));
    }


                /**
     * 构造一个具有指定输出流、字符集和区域设置的新格式化程序。
     *
     * @param  os
     *         用作此格式化程序目标的输出流。输出将被缓冲。
     *
     * @param  csn
     *         支持的 {@linkplain java.nio.charset.Charset
     *         字符集} 的名称
     *
     * @param  l
     *         在格式化期间应用的 {@linkplain java.util.Locale 区域设置}。如果 {@code l} 为 {@code null}，则不应用本地化。
     *
     * @throws  UnsupportedEncodingException
     *          如果不支持命名的字符集
     */
    public Formatter(OutputStream os, String csn, Locale l)
        throws UnsupportedEncodingException
    {
        this(l, new BufferedWriter(new OutputStreamWriter(os, csn)));
    }

    private static char getZero(Locale l) {
        if ((l != null) && !l.equals(Locale.US)) {
            DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(l);
            return dfs.getZeroDigit();
        } else {
            return '0';
        }
    }

    /**
     * 返回此格式化程序构造时设置的区域设置。
     *
     * <p> 该对象的具有区域设置参数的 {@link #format(java.util.Locale,String,Object...) format} 方法不会更改此值。
     *
     * @return  如果不应用本地化，则返回 {@code null}，否则返回一个区域设置
     *
     * @throws  FormatterClosedException
     *          如果通过调用其 {@link
     *          #close()} 方法关闭了此格式化程序
     */
    public Locale locale() {
        ensureOpen();
        return l;
    }

    /**
     * 返回输出的目标。
     *
     * @return  输出的目标
     *
     * @throws  FormatterClosedException
     *          如果通过调用其 {@link
     *          #close()} 方法关闭了此格式化程序
     */
    public Appendable out() {
        ensureOpen();
        return a;
    }

    /**
     * 返回在输出目标上调用 {@code toString()} 的结果。例如，以下代码将文本格式化到 {@link StringBuilder} 中，然后检索结果字符串：
     *
     * <blockquote><pre>
     *   Formatter f = new Formatter();
     *   f.format("Last reboot at %tc", lastRebootDate);
     *   String s = f.toString();
     *   // -&gt; s == "Last reboot at Sat Jan 01 00:00:00 PST 2000"
     * </pre></blockquote>
     *
     * <p> 调用此方法的行为与调用
     *
     * <pre>
     *     out().toString() </pre>
     *
     * <p> 完全相同。根据 {@code toString} 对 {@link
     * Appendable} 的规范，返回的字符串可能包含或不包含写入目标的字符。例如，缓冲区通常在 {@code toString()} 中返回其内容，但流不能，因为数据被丢弃。
     *
     * @return  在输出目标上调用 {@code toString()} 的结果
     *
     * @throws  FormatterClosedException
     *          如果通过调用其 {@link
     *          #close()} 方法关闭了此格式化程序
     */
    public String toString() {
        ensureOpen();
        return a.toString();
    }

    /**
     * 刷新此格式化程序。如果目标实现了 {@link
     * java.io.Flushable} 接口，将调用其 {@code flush} 方法。
     *
     * <p> 刷新格式化程序会将目标中的任何缓冲输出写入底层流。
     *
     * @throws  FormatterClosedException
     *          如果通过调用其 {@link
     *          #close()} 方法关闭了此格式化程序
     */
    public void flush() {
        ensureOpen();
        if (a instanceof Flushable) {
            try {
                ((Flushable)a).flush();
            } catch (IOException ioe) {
                lastException = ioe;
            }
        }
    }

    /**
     * 关闭此格式化程序。如果目标实现了 {@link
     * java.io.Closeable} 接口，将调用其 {@code close} 方法。
     *
     * <p> 关闭格式化程序允许其释放可能持有的资源（如打开的文件）。如果格式化程序已关闭，则调用此方法没有效果。
     *
     * <p> 在此格式化程序关闭后尝试调用任何方法（除了 {@link #ioException()}）将导致 {@link
     * FormatterClosedException}。
     */
    public void close() {
        if (a == null)
            return;
        try {
            if (a instanceof Closeable)
                ((Closeable)a).close();
        } catch (IOException ioe) {
            lastException = ioe;
        } finally {
            a = null;
        }
    }

    private void ensureOpen() {
        if (a == null)
            throw new FormatterClosedException();
    }

    /**
     * 返回此格式化程序的 {@link
     * Appendable} 最后抛出的 {@code IOException}。
     *
     * <p> 如果目标的 {@code append()} 方法从未抛出 {@code IOException}，则此方法将始终返回 {@code null}。
     *
     * @return  Appendable 抛出的最后一个异常或 {@code null}，如果不存在这样的异常。
     */
    public IOException ioException() {
        return lastException;
    }

    /**
     * 使用指定的格式字符串和参数将格式化的字符串写入此对象的目标。使用的区域设置是在构造此格式化程序时定义的。
     *
     * @param  format
     *         格式字符串，如 <a href="#syntax">格式字符串语法</a> 所述。
     *
     * @param  args
     *         格式字符串中的格式说明符引用的参数。如果参数多于格式说明符，多余的参数将被忽略。参数的最大数量受 <cite>The Java&trade; Virtual Machine Specification</cite> 中定义的 Java 数组的最大维度限制。
     *
     * @throws  IllegalFormatException
     *          如果格式字符串包含非法语法、与给定参数不兼容的格式说明符、格式字符串的参数不足或其他非法条件。有关所有可能的格式错误的规范，请参阅格式化程序类规范的 <a href="#detail">详细信息</a> 部分。
     *
     * @throws  FormatterClosedException
     *          如果通过调用其 {@link
     *          #close()} 方法关闭了此格式化程序
     *
     * @return  此格式化程序
     */
    public Formatter format(String format, Object ... args) {
        return format(l, format, args);
    }

    /**
     * 使用指定的区域设置、格式字符串和参数将格式化的字符串写入此对象的目标。
     *
     * @param  l
     *         在格式化期间应用的 {@linkplain java.util.Locale 区域设置}。如果 {@code l} 为 {@code null}，则不应用本地化。这不会更改此对象在构造时设置的区域设置。
     *
     * @param  format
     *         格式字符串，如 <a href="#syntax">格式字符串语法</a> 所述
     *
     * @param  args
     *         格式字符串中的格式说明符引用的参数。如果参数多于格式说明符，多余的参数将被忽略。参数的最大数量受 <cite>The Java&trade; Virtual Machine Specification</cite> 中定义的 Java 数组的最大维度限制。
     *
     * @throws  IllegalFormatException
     *          如果格式字符串包含非法语法、与给定参数不兼容的格式说明符、格式字符串的参数不足或其他非法条件。有关所有可能的格式错误的规范，请参阅格式化程序类规范的 <a href="#detail">详细信息</a> 部分。
     *
     * @throws  FormatterClosedException
     *          如果通过调用其 {@link
     *          #close()} 方法关闭了此格式化程序
     *
     * @return  此格式化程序
     */
    public Formatter format(Locale l, String format, Object ... args) {
        ensureOpen();

        // 最后引用的参数索引
        int last = -1;
        // 最后一个普通索引
        int lasto = -1;

        FormatString[] fsa = parse(format);
        for (int i = 0; i < fsa.length; i++) {
            FormatString fs = fsa[i];
            int index = fs.index();
            try {
                switch (index) {
                case -2:  // 固定字符串，"%n"，或 "%%"
                    fs.print(null, l);
                    break;
                case -1:  // 相对索引
                    if (last < 0 || (args != null && last > args.length - 1))
                        throw new MissingFormatArgumentException(fs.toString());
                    fs.print((args == null ? null : args[last]), l);
                    break;
                case 0:  // 普通索引
                    lasto++;
                    last = lasto;
                    if (args != null && lasto > args.length - 1)
                        throw new MissingFormatArgumentException(fs.toString());
                    fs.print((args == null ? null : args[lasto]), l);
                    break;
                default:  // 显式索引
                    last = index - 1;
                    if (args != null && last > args.length - 1)
                        throw new MissingFormatArgumentException(fs.toString());
                    fs.print((args == null ? null : args[last]), l);
                    break;
                }
            } catch (IOException x) {
                lastException = x;
            }
        }
        return this;
    }

    // %[argument_index$][flags][width][.precision][t]conversion
    private static final String formatSpecifier
        = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";

    private static Pattern fsPattern = Pattern.compile(formatSpecifier);

    /**
     * 在格式字符串中查找格式说明符。
     */
    private FormatString[] parse(String s) {
        ArrayList<FormatString> al = new ArrayList<>();
        Matcher m = fsPattern.matcher(s);
        for (int i = 0, len = s.length(); i < len; ) {
            if (m.find(i)) {
                // 从字符串的开始到格式说明符的开始之间的任何内容都是固定文本或包含无效的格式字符串。
                if (m.start() != i) {
                    // 确保我们没有错过任何无效的格式说明符
                    checkText(s, i, m.start());
                    // 假设前面的字符是固定文本
                    al.add(new FixedString(s.substring(i, m.start())));
                }

                al.add(new FormatSpecifier(m));
                i = m.end();
            } else {
                // 没有更多的有效格式说明符。检查可能的无效格式说明符。
                checkText(s, i, len);
                // 剩余的字符串是固定文本
                al.add(new FixedString(s.substring(i)));
                break;
            }
        }
        return al.toArray(new FormatString[al.size()]);
    }

    private static void checkText(String s, int start, int end) {
        for (int i = start; i < end; i++) {
            // 区域中的任何 '%' 都开始一个无效的格式说明符。
            if (s.charAt(i) == '%') {
                char c = (i == end - 1) ? '%' : s.charAt(i + 1);
                throw new UnknownFormatConversionException(String.valueOf(c));
            }
        }
    }

    private interface FormatString {
        int index();
        void print(Object arg, Locale l) throws IOException;
        String toString();
    }

    private class FixedString implements FormatString {
        private String s;
        FixedString(String s) { this.s = s; }
        public int index() { return -2; }
        public void print(Object arg, Locale l)
            throws IOException { a.append(s); }
        public String toString() { return s; }
    }

    /**
     * {@code BigDecimal} 格式化的枚举。
     */
    public enum BigDecimalLayoutForm {
        /**
         * 以计算机化的科学记数法格式化 {@code BigDecimal}。
         */
        SCIENTIFIC,

        /**
         * 以十进制数格式化 {@code BigDecimal}。
         */
        DECIMAL_FLOAT
    };

    private class FormatSpecifier implements FormatString {
        private int index = -1;
        private Flags f = Flags.NONE;
        private int width;
        private int precision;
        private boolean dt = false;
        private char c;

        private int index(String s) {
            if (s != null) {
                try {
                    index = Integer.parseInt(s.substring(0, s.length() - 1));
                } catch (NumberFormatException x) {
                    assert(false);
                }
            } else {
                index = 0;
            }
            return index;
        }

        public int index() {
            return index;
        }

        private Flags flags(String s) {
            f = Flags.parse(s);
            if (f.contains(Flags.PREVIOUS))
                index = -1;
            return f;
        }

        Flags flags() {
            return f;
        }

        private int width(String s) {
            width = -1;
            if (s != null) {
                try {
                    width  = Integer.parseInt(s);
                    if (width < 0)
                        throw new IllegalFormatWidthException(width);
                } catch (NumberFormatException x) {
                    assert(false);
                }
            }
            return width;
        }

        int width() {
            return width;
        }

        private int precision(String s) {
            precision = -1;
            if (s != null) {
                try {
                    // 去掉 '.'
                    precision = Integer.parseInt(s.substring(1));
                    if (precision < 0)
                        throw new IllegalFormatPrecisionException(precision);
                } catch (NumberFormatException x) {
                    assert(false);
                }
            }
            return precision;
        }

        int precision() {
            return precision;
        }

        private char conversion(String s) {
            c = s.charAt(0);
            if (!dt) {
                if (!Conversion.isValid(c))
                    throw new UnknownFormatConversionException(String.valueOf(c));
                if (Character.isUpperCase(c))
                    f.add(Flags.UPPERCASE);
                c = Character.toLowerCase(c);
                if (Conversion.isText(c))
                    index = -2;
            }
            return c;
        }


                    private char conversion() {
            return c;
        }

        FormatSpecifier(Matcher m) {
            int idx = 1;

            index(m.group(idx++));
            flags(m.group(idx++));
            width(m.group(idx++));
            precision(m.group(idx++));

            String tT = m.group(idx++);
            if (tT != null) {
                dt = true;
                if (tT.equals("T"))
                    f.add(Flags.UPPERCASE);
            }

            conversion(m.group(idx));

            if (dt)
                checkDateTime();
            else if (Conversion.isGeneral(c))
                checkGeneral();
            else if (Conversion.isCharacter(c))
                checkCharacter();
            else if (Conversion.isInteger(c))
                checkInteger();
            else if (Conversion.isFloat(c))
                checkFloat();
            else if (Conversion.isText(c))
                checkText();
            else
                throw new UnknownFormatConversionException(String.valueOf(c));
        }

        public void print(Object arg, Locale l) throws IOException {
            if (dt) {
                printDateTime(arg, l);
                return;
            }
            switch(c) {
            case Conversion.DECIMAL_INTEGER:
            case Conversion.OCTAL_INTEGER:
            case Conversion.HEXADECIMAL_INTEGER:
                printInteger(arg, l);
                break;
            case Conversion.SCIENTIFIC:
            case Conversion.GENERAL:
            case Conversion.DECIMAL_FLOAT:
            case Conversion.HEXADECIMAL_FLOAT:
                printFloat(arg, l);
                break;
            case Conversion.CHARACTER:
            case Conversion.CHARACTER_UPPER:
                printCharacter(arg);
                break;
            case Conversion.BOOLEAN:
                printBoolean(arg);
                break;
            case Conversion.STRING:
                printString(arg, l);
                break;
            case Conversion.HASHCODE:
                printHashCode(arg);
                break;
            case Conversion.LINE_SEPARATOR:
                a.append(System.lineSeparator());
                break;
            case Conversion.PERCENT_SIGN:
                a.append('%');
                break;
            default:
                assert false;
            }
        }

        private void printInteger(Object arg, Locale l) throws IOException {
            if (arg == null)
                print("null");
            else if (arg instanceof Byte)
                print(((Byte)arg).byteValue(), l);
            else if (arg instanceof Short)
                print(((Short)arg).shortValue(), l);
            else if (arg instanceof Integer)
                print(((Integer)arg).intValue(), l);
            else if (arg instanceof Long)
                print(((Long)arg).longValue(), l);
            else if (arg instanceof BigInteger)
                print(((BigInteger)arg), l);
            else
                failConversion(c, arg);
        }

        private void printFloat(Object arg, Locale l) throws IOException {
            if (arg == null)
                print("null");
            else if (arg instanceof Float)
                print(((Float)arg).floatValue(), l);
            else if (arg instanceof Double)
                print(((Double)arg).doubleValue(), l);
            else if (arg instanceof BigDecimal)
                print(((BigDecimal)arg), l);
            else
                failConversion(c, arg);
        }

        private void printDateTime(Object arg, Locale l) throws IOException {
            if (arg == null) {
                print("null");
                return;
            }
            Calendar cal = null;

            // 代替 Calendar.setLenient(true)，或许我们应该
            // 包装可能抛出的 IllegalArgumentException？
            if (arg instanceof Long) {
                // 注意以下方法使用默认时区的实例（TimeZone.getDefaultRef()。
                cal = Calendar.getInstance(l == null ? Locale.US : l);
                cal.setTimeInMillis((Long)arg);
            } else if (arg instanceof Date) {
                // 注意以下方法使用默认时区的实例（TimeZone.getDefaultRef()。
                cal = Calendar.getInstance(l == null ? Locale.US : l);
                cal.setTime((Date)arg);
            } else if (arg instanceof Calendar) {
                cal = (Calendar) ((Calendar) arg).clone();
                cal.setLenient(true);
            } else if (arg instanceof TemporalAccessor) {
                print((TemporalAccessor) arg, c, l);
                return;
            } else {
                failConversion(c, arg);
            }
            // 使用提供的区域设置，以便调用
            // localizedMagnitude() 时使用对 null 的优化。
            print(cal, c, l);
        }

        private void printCharacter(Object arg) throws IOException {
            if (arg == null) {
                print("null");
                return;
            }
            String s = null;
            if (arg instanceof Character) {
                s = ((Character)arg).toString();
            } else if (arg instanceof Byte) {
                byte i = ((Byte)arg).byteValue();
                if (Character.isValidCodePoint(i))
                    s = new String(Character.toChars(i));
                else
                    throw new IllegalFormatCodePointException(i);
            } else if (arg instanceof Short) {
                short i = ((Short)arg).shortValue();
                if (Character.isValidCodePoint(i))
                    s = new String(Character.toChars(i));
                else
                    throw new IllegalFormatCodePointException(i);
            } else if (arg instanceof Integer) {
                int i = ((Integer)arg).intValue();
                if (Character.isValidCodePoint(i))
                    s = new String(Character.toChars(i));
                else
                    throw new IllegalFormatCodePointException(i);
            } else {
                failConversion(c, arg);
            }
            print(s);
        }

        private void printString(Object arg, Locale l) throws IOException {
            if (arg instanceof Formattable) {
                Formatter fmt = Formatter.this;
                if (fmt.locale() != l)
                    fmt = new Formatter(fmt.out(), l);
                ((Formattable)arg).formatTo(fmt, f.valueOf(), width, precision);
            } else {
                if (f.contains(Flags.ALTERNATE))
                    failMismatch(Flags.ALTERNATE, 's');
                if (arg == null)
                    print("null");
                else
                    print(arg.toString());
            }
        }

        private void printBoolean(Object arg) throws IOException {
            String s;
            if (arg != null)
                s = ((arg instanceof Boolean)
                     ? ((Boolean)arg).toString()
                     : Boolean.toString(true));
            else
                s = Boolean.toString(false);
            print(s);
        }

        private void printHashCode(Object arg) throws IOException {
            String s = (arg == null
                        ? "null"
                        : Integer.toHexString(arg.hashCode()));
            print(s);
        }

        private void print(String s) throws IOException {
            if (precision != -1 && precision < s.length())
                s = s.substring(0, precision);
            if (f.contains(Flags.UPPERCASE))
                s = s.toUpperCase();
            a.append(justify(s));
        }

        private String justify(String s) {
            if (width == -1)
                return s;
            StringBuilder sb = new StringBuilder();
            boolean pad = f.contains(Flags.LEFT_JUSTIFY);
            int sp = width - s.length();
            if (!pad)
                for (int i = 0; i < sp; i++) sb.append(' ');
            sb.append(s);
            if (pad)
                for (int i = 0; i < sp; i++) sb.append(' ');
            return sb.toString();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("%");
            // Flags.UPPERCASE 为合法转换内部设置。
            Flags dupf = f.dup().remove(Flags.UPPERCASE);
            sb.append(dupf.toString());
            if (index > 0)
                sb.append(index).append('$');
            if (width != -1)
                sb.append(width);
            if (precision != -1)
                sb.append('.').append(precision);
            if (dt)
                sb.append(f.contains(Flags.UPPERCASE) ? 'T' : 't');
            sb.append(f.contains(Flags.UPPERCASE)
                      ? Character.toUpperCase(c) : c);
            return sb.toString();
        }

        private void checkGeneral() {
            if ((c == Conversion.BOOLEAN || c == Conversion.HASHCODE)
                && f.contains(Flags.ALTERNATE))
                failMismatch(Flags.ALTERNATE, c);
            // '-' 需要宽度
            if (width == -1 && f.contains(Flags.LEFT_JUSTIFY))
                throw new MissingFormatWidthException(toString());
            checkBadFlags(Flags.PLUS, Flags.LEADING_SPACE, Flags.ZERO_PAD,
                          Flags.GROUP, Flags.PARENTHESES);
        }

        private void checkDateTime() {
            if (precision != -1)
                throw new IllegalFormatPrecisionException(precision);
            if (!DateTime.isValid(c))
                throw new UnknownFormatConversionException("t" + c);
            checkBadFlags(Flags.ALTERNATE, Flags.PLUS, Flags.LEADING_SPACE,
                          Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);
            // '-' 需要宽度
            if (width == -1 && f.contains(Flags.LEFT_JUSTIFY))
                throw new MissingFormatWidthException(toString());
        }

        private void checkCharacter() {
            if (precision != -1)
                throw new IllegalFormatPrecisionException(precision);
            checkBadFlags(Flags.ALTERNATE, Flags.PLUS, Flags.LEADING_SPACE,
                          Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);
            // '-' 需要宽度
            if (width == -1 && f.contains(Flags.LEFT_JUSTIFY))
                throw new MissingFormatWidthException(toString());
        }

        private void checkInteger() {
            checkNumeric();
            if (precision != -1)
                throw new IllegalFormatPrecisionException(precision);

            if (c == Conversion.DECIMAL_INTEGER)
                checkBadFlags(Flags.ALTERNATE);
            else if (c == Conversion.OCTAL_INTEGER)
                checkBadFlags(Flags.GROUP);
            else
                checkBadFlags(Flags.GROUP);
        }

        private void checkBadFlags(Flags ... badFlags) {
            for (int i = 0; i < badFlags.length; i++)
                if (f.contains(badFlags[i]))
                    failMismatch(badFlags[i], c);
        }

        private void checkFloat() {
            checkNumeric();
            if (c == Conversion.DECIMAL_FLOAT) {
            } else if (c == Conversion.HEXADECIMAL_FLOAT) {
                checkBadFlags(Flags.PARENTHESES, Flags.GROUP);
            } else if (c == Conversion.SCIENTIFIC) {
                checkBadFlags(Flags.GROUP);
            } else if (c == Conversion.GENERAL) {
                checkBadFlags(Flags.ALTERNATE);
            }
        }

        private void checkNumeric() {
            if (width != -1 && width < 0)
                throw new IllegalFormatWidthException(width);

            if (precision != -1 && precision < 0)
                throw new IllegalFormatPrecisionException(precision);

            // '-' 和 '0' 需要宽度
            if (width == -1
                && (f.contains(Flags.LEFT_JUSTIFY) || f.contains(Flags.ZERO_PAD)))
                throw new MissingFormatWidthException(toString());

            // 不良组合
            if ((f.contains(Flags.PLUS) && f.contains(Flags.LEADING_SPACE))
                || (f.contains(Flags.LEFT_JUSTIFY) && f.contains(Flags.ZERO_PAD)))
                throw new IllegalFormatFlagsException(f.toString());
        }

        private void checkText() {
            if (precision != -1)
                throw new IllegalFormatPrecisionException(precision);
            switch (c) {
            case Conversion.PERCENT_SIGN:
                if (f.valueOf() != Flags.LEFT_JUSTIFY.valueOf()
                    && f.valueOf() != Flags.NONE.valueOf())
                    throw new IllegalFormatFlagsException(f.toString());
                // '-' 需要宽度
                if (width == -1 && f.contains(Flags.LEFT_JUSTIFY))
                    throw new MissingFormatWidthException(toString());
                break;
            case Conversion.LINE_SEPARATOR:
                if (width != -1)
                    throw new IllegalFormatWidthException(width);
                if (f.valueOf() != Flags.NONE.valueOf())
                    throw new IllegalFormatFlagsException(f.toString());
                break;
            default:
                assert false;
            }
        }

        private void print(byte value, Locale l) throws IOException {
            long v = value;
            if (value < 0
                && (c == Conversion.OCTAL_INTEGER
                    || c == Conversion.HEXADECIMAL_INTEGER)) {
                v += (1L << 8);
                assert v >= 0 : v;
            }
            print(v, l);
        }

        private void print(short value, Locale l) throws IOException {
            long v = value;
            if (value < 0
                && (c == Conversion.OCTAL_INTEGER
                    || c == Conversion.HEXADECIMAL_INTEGER)) {
                v += (1L << 16);
                assert v >= 0 : v;
            }
            print(v, l);
        }

        private void print(int value, Locale l) throws IOException {
            long v = value;
            if (value < 0
                && (c == Conversion.OCTAL_INTEGER
                    || c == Conversion.HEXADECIMAL_INTEGER)) {
                v += (1L << 32);
                assert v >= 0 : v;
            }
            print(v, l);
        }

        private void print(long value, Locale l) throws IOException {

            StringBuilder sb = new StringBuilder();

            if (c == Conversion.DECIMAL_INTEGER) {
                boolean neg = value < 0;
                char[] va;
                if (value < 0)
                    va = Long.toString(value, 10).substring(1).toCharArray();
                else
                    va = Long.toString(value, 10).toCharArray();

                // 前导符号指示符
                leadingSign(sb, neg);

                // 值
                localizedMagnitude(sb, va, f, adjustWidth(width, f, neg), l);

                // 后导符号指示符
                trailingSign(sb, neg);
            } else if (c == Conversion.OCTAL_INTEGER) {
                checkBadFlags(Flags.PARENTHESES, Flags.LEADING_SPACE,
                              Flags.PLUS);
                String s = Long.toOctalString(value);
                int len = (f.contains(Flags.ALTERNATE)
                           ? s.length() + 1
                           : s.length());


                            // 在应用ZERO_PAD之前应用ALTERNATE（八进制的基数指示符）
                if (f.contains(Flags.ALTERNATE))
                    sb.append('0');
                if (f.contains(Flags.ZERO_PAD))
                    for (int i = 0; i < width - len; i++) sb.append('0');
                sb.append(s);
            } else if (c == Conversion.HEXADECIMAL_INTEGER) {
                checkBadFlags(Flags.PARENTHESES, Flags.LEADING_SPACE,
                              Flags.PLUS);
                String s = Long.toHexString(value);
                int len = (f.contains(Flags.ALTERNATE)
                           ? s.length() + 2
                           : s.length());

                // 在应用ZERO_PAD之前应用ALTERNATE（十六进制的基数指示符）
                if (f.contains(Flags.ALTERNATE))
                    sb.append(f.contains(Flags.UPPERCASE) ? "0X" : "0x");
                if (f.contains(Flags.ZERO_PAD))
                    for (int i = 0; i < width - len; i++) sb.append('0');
                if (f.contains(Flags.UPPERCASE))
                    s = s.toUpperCase();
                sb.append(s);
            }

            // 根据宽度进行对齐
            a.append(justify(sb.toString()));
        }

        // neg := val < 0
        private StringBuilder leadingSign(StringBuilder sb, boolean neg) {
            if (!neg) {
                if (f.contains(Flags.PLUS)) {
                    sb.append('+');
                } else if (f.contains(Flags.LEADING_SPACE)) {
                    sb.append(' ');
                }
            } else {
                if (f.contains(Flags.PARENTHESES))
                    sb.append('(');
                else
                    sb.append('-');
            }
            return sb;
        }

        // neg := val < 0
        private StringBuilder trailingSign(StringBuilder sb, boolean neg) {
            if (neg && f.contains(Flags.PARENTHESES))
                sb.append(')');
            return sb;
        }

        private void print(BigInteger value, Locale l) throws IOException {
            StringBuilder sb = new StringBuilder();
            boolean neg = value.signum() == -1;
            BigInteger v = value.abs();

            // 前导符号指示符
            leadingSign(sb, neg);

            // 值
            if (c == Conversion.DECIMAL_INTEGER) {
                char[] va = v.toString().toCharArray();
                localizedMagnitude(sb, va, f, adjustWidth(width, f, neg), l);
            } else if (c == Conversion.OCTAL_INTEGER) {
                String s = v.toString(8);

                int len = s.length() + sb.length();
                if (neg && f.contains(Flags.PARENTHESES))
                    len++;

                // 在应用ZERO_PAD之前应用ALTERNATE（八进制的基数指示符）
                if (f.contains(Flags.ALTERNATE)) {
                    len++;
                    sb.append('0');
                }
                if (f.contains(Flags.ZERO_PAD)) {
                    for (int i = 0; i < width - len; i++)
                        sb.append('0');
                }
                sb.append(s);
            } else if (c == Conversion.HEXADECIMAL_INTEGER) {
                String s = v.toString(16);

                int len = s.length() + sb.length();
                if (neg && f.contains(Flags.PARENTHESES))
                    len++;

                // 在应用ZERO_PAD之前应用ALTERNATE（十六进制的基数指示符）
                if (f.contains(Flags.ALTERNATE)) {
                    len += 2;
                    sb.append(f.contains(Flags.UPPERCASE) ? "0X" : "0x");
                }
                if (f.contains(Flags.ZERO_PAD))
                    for (int i = 0; i < width - len; i++)
                        sb.append('0');
                if (f.contains(Flags.UPPERCASE))
                    s = s.toUpperCase();
                sb.append(s);
            }

            // 尾随符号指示符
            trailingSign(sb, (value.signum() == -1));

            // 根据宽度进行对齐
            a.append(justify(sb.toString()));
        }

        private void print(float value, Locale l) throws IOException {
            print((double) value, l);
        }

        private void print(double value, Locale l) throws IOException {
            StringBuilder sb = new StringBuilder();
            boolean neg = Double.compare(value, 0.0) == -1;

            if (!Double.isNaN(value)) {
                double v = Math.abs(value);

                // 前导符号指示符
                leadingSign(sb, neg);

                // 值
                if (!Double.isInfinite(v))
                    print(sb, v, l, f, c, precision, neg);
                else
                    sb.append(f.contains(Flags.UPPERCASE)
                              ? "INFINITY" : "Infinity");

                // 尾随符号指示符
                trailingSign(sb, neg);
            } else {
                sb.append(f.contains(Flags.UPPERCASE) ? "NAN" : "NaN");
            }

            // 根据宽度进行对齐
            a.append(justify(sb.toString()));
        }

        // !Double.isInfinite(value) && !Double.isNaN(value)
        private void print(StringBuilder sb, double value, Locale l,
                           Flags f, char c, int precision, boolean neg)
            throws IOException
        {
            if (c == Conversion.SCIENTIFIC) {
                // 创建具有所需精度的新FormattedFloatingDecimal。
                int prec = (precision == -1 ? 6 : precision);

                FormattedFloatingDecimal fd
                        = FormattedFloatingDecimal.valueOf(value, prec,
                          FormattedFloatingDecimal.Form.SCIENTIFIC);

                char[] mant = addZeros(fd.getMantissa(), prec);

                // 如果精度为零且设置了'#'标志，则添加请求的小数点。
                if (f.contains(Flags.ALTERNATE) && (prec == 0))
                    mant = addDot(mant);

                char[] exp = (value == 0.0)
                    ? new char[] {'+','0','0'} : fd.getExponent();

                int newW = width;
                if (width != -1)
                    newW = adjustWidth(width - exp.length - 1, f, neg);
                localizedMagnitude(sb, mant, f, newW, l);

                sb.append(f.contains(Flags.UPPERCASE) ? 'E' : 'e');

                Flags flags = f.dup().remove(Flags.GROUP);
                char sign = exp[0];
                assert(sign == '+' || sign == '-');
                sb.append(sign);

                char[] tmp = new char[exp.length - 1];
                System.arraycopy(exp, 1, tmp, 0, exp.length - 1);
                sb.append(localizedMagnitude(null, tmp, flags, -1, l));
            } else if (c == Conversion.DECIMAL_FLOAT) {
                // 创建具有所需精度的新FormattedFloatingDecimal。
                int prec = (precision == -1 ? 6 : precision);

                FormattedFloatingDecimal fd
                        = FormattedFloatingDecimal.valueOf(value, prec,
                          FormattedFloatingDecimal.Form.DECIMAL_FLOAT);

                char[] mant = addZeros(fd.getMantissa(), prec);

                // 如果精度为零且设置了'#'标志，则添加请求的小数点。
                if (f.contains(Flags.ALTERNATE) && (prec == 0))
                    mant = addDot(mant);

                int newW = width;
                if (width != -1)
                    newW = adjustWidth(width, f, neg);
                localizedMagnitude(sb, mant, f, newW, l);
            } else if (c == Conversion.GENERAL) {
                int prec = precision;
                if (precision == -1)
                    prec = 6;
                else if (precision == 0)
                    prec = 1;

                char[] exp;
                char[] mant;
                int expRounded;
                if (value == 0.0) {
                    exp = null;
                    mant = new char[] {'0'};
                    expRounded = 0;
                } else {
                    FormattedFloatingDecimal fd
                        = FormattedFloatingDecimal.valueOf(value, prec,
                          FormattedFloatingDecimal.Form.GENERAL);
                    exp = fd.getExponent();
                    mant = fd.getMantissa();
                    expRounded = fd.getExponentRounded();
                }

                if (exp != null) {
                    prec -= 1;
                } else {
                    prec -= expRounded + 1;
                }

                mant = addZeros(mant, prec);
                // 如果精度为零且设置了'#'标志，则添加请求的小数点。
                if (f.contains(Flags.ALTERNATE) && (prec == 0))
                    mant = addDot(mant);

                int newW = width;
                if (width != -1) {
                    if (exp != null)
                        newW = adjustWidth(width - exp.length - 1, f, neg);
                    else
                        newW = adjustWidth(width, f, neg);
                }
                localizedMagnitude(sb, mant, f, newW, l);

                if (exp != null) {
                    sb.append(f.contains(Flags.UPPERCASE) ? 'E' : 'e');

                    Flags flags = f.dup().remove(Flags.GROUP);
                    char sign = exp[0];
                    assert(sign == '+' || sign == '-');
                    sb.append(sign);

                    char[] tmp = new char[exp.length - 1];
                    System.arraycopy(exp, 1, tmp, 0, exp.length - 1);
                    sb.append(localizedMagnitude(null, tmp, flags, -1, l));
                }
            } else if (c == Conversion.HEXADECIMAL_FLOAT) {
                int prec = precision;
                if (precision == -1)
                    // 假设我们想要所有的数字
                    prec = 0;
                else if (precision == 0)
                    prec = 1;

                String s = hexDouble(value, prec);

                char[] va;
                boolean upper = f.contains(Flags.UPPERCASE);
                sb.append(upper ? "0X" : "0x");

                if (f.contains(Flags.ZERO_PAD))
                    for (int i = 0; i < width - s.length() - 2; i++)
                        sb.append('0');

                int idx = s.indexOf('p');
                va = s.substring(0, idx).toCharArray();
                if (upper) {
                    String tmp = new String(va);
                    // 不进行本地化处理
                    tmp = tmp.toUpperCase(Locale.US);
                    va = tmp.toCharArray();
                }
                sb.append(prec != 0 ? addZeros(va, prec) : va);
                sb.append(upper ? 'P' : 'p');
                sb.append(s.substring(idx+1));
            }
        }

        // 添加零以达到请求的精度。
        private char[] addZeros(char[] v, int prec) {
            // 查找小数点。如果没有找到，则需要在添加零之前添加小数点。
            int i;
            for (i = 0; i < v.length; i++) {
                if (v[i] == '.')
                    break;
            }
            boolean needDot = false;
            if (i == v.length) {
                needDot = true;
            }

            // 确定现有精度。
            int outPrec = v.length - i - (needDot ? 0 : 1);
            assert (outPrec <= prec);
            if (outPrec == prec)
                return v;

            // 创建具有现有内容的新数组。
            char[] tmp
                = new char[v.length + prec - outPrec + (needDot ? 1 : 0)];
            System.arraycopy(v, 0, tmp, 0, v.length);

            // 如果之前确定需要添加小数点，则添加小数点。
            int start = v.length;
            if (needDot) {
                tmp[v.length] = '.';
                start++;
            }

            // 添加零。
            for (int j = start; j < tmp.length; j++)
                tmp[j] = '0';

            return tmp;
        }

        // 方法假设 d > 0。
        private String hexDouble(double d, int prec) {
            // 让 Double.toHexString 处理简单情况
            if(!Double.isFinite(d) || d == 0.0 || prec == 0 || prec >= 13)
                // 删除 "0x"
                return Double.toHexString(d).substring(2);
            else {
                assert(prec >= 1 && prec <= 12);

                int exponent  = Math.getExponent(d);
                boolean subnormal
                    = (exponent == DoubleConsts.MIN_EXPONENT - 1);

                // 如果这是次正规输入，则进行规范化（可以更快地作为整数操作）。
                if (subnormal) {
                    scaleUp = Math.scalb(1.0, 54);
                    d *= scaleUp;
                    // 计算指数。这不仅仅是 exponent + 54，因为前者不是规范化指数。
                    exponent = Math.getExponent(d);
                    assert exponent >= DoubleConsts.MIN_EXPONENT &&
                        exponent <= DoubleConsts.MAX_EXPONENT: exponent;
                }

                int precision = 1 + prec*4;
                int shiftDistance
                    =  DoubleConsts.SIGNIFICAND_WIDTH - precision;
                assert(shiftDistance >= 1 && shiftDistance < DoubleConsts.SIGNIFICAND_WIDTH);

                long doppel = Double.doubleToLongBits(d);
                // 确定要保留的位数。
                long newSignif
                    = (doppel & (DoubleConsts.EXP_BIT_MASK
                                 | DoubleConsts.SIGNIF_BIT_MASK))
                                     >> shiftDistance;
                // 要舍弃的位。
                long roundingBits = doppel & ~(~0L << shiftDistance);

                // 为了决定如何舍入，查看工作有效数的最低位、最高位舍弃位（舍入位）以及较低位舍弃位是否非零（粘位）。

                boolean leastZero = (newSignif & 0x1L) == 0L;
                boolean round
                    = ((1L << (shiftDistance - 1) ) & roundingBits) != 0L;
                boolean sticky  = shiftDistance > 1 &&
                    (~(1L<< (shiftDistance - 1)) & roundingBits) != 0;
                if((leastZero && round && sticky) || (!leastZero && round)) {
                    newSignif++;
                }

                long signBit = doppel & DoubleConsts.SIGN_BIT_MASK;
                newSignif = signBit | (newSignif << shiftDistance);
                double result = Double.longBitsToDouble(newSignif);

                if (Double.isInfinite(result) ) {
                    // 由舍入生成的无限结果
                    return "1.0p1024";
                } else {
                    String res = Double.toHexString(result).substring(2);
                    if (!subnormal)
                        return res;
                    else {
                        // 创建规范化的次正规字符串。
                        int idx = res.indexOf('p');
                        if (idx == -1) {
                            // 十六进制字符串中没有 'p' 字符。
                            assert false;
                            return null;
                        } else {
                            // 获取指数并附加在末尾。
                            String exp = res.substring(idx + 1);
                            int iexp = Integer.parseInt(exp) -54;
                            return res.substring(0, idx) + "p"
                                + Integer.toString(iexp);
                        }
                    }
                }
            }
        }


                    private void print(BigDecimal value, Locale l) throws IOException {
            if (c == Conversion.HEXADECIMAL_FLOAT)
                failConversion(c, value);
            StringBuilder sb = new StringBuilder();
            boolean neg = value.signum() == -1;
            BigDecimal v = value.abs();
            // 前导符号指示器
            leadingSign(sb, neg);

            // 值
            print(sb, v, l, f, c, precision, neg);

            // 后导符号指示器
            trailingSign(sb, neg);

            // 根据宽度调整
            a.append(justify(sb.toString()));
        }

        // value > 0
        private void print(StringBuilder sb, BigDecimal value, Locale l,
                           Flags f, char c, int precision, boolean neg)
            throws IOException
        {
            if (c == Conversion.SCIENTIFIC) {
                // 创建具有所需精度的新 BigDecimal。
                int prec = (precision == -1 ? 6 : precision);
                int scale = value.scale();
                int origPrec = value.precision();
                int nzeros = 0;
                int compPrec;

                if (prec > origPrec - 1) {
                    compPrec = origPrec;
                    nzeros = prec - (origPrec - 1);
                } else {
                    compPrec = prec + 1;
                }

                MathContext mc = new MathContext(compPrec);
                BigDecimal v
                    = new BigDecimal(value.unscaledValue(), scale, mc);

                BigDecimalLayout bdl
                    = new BigDecimalLayout(v.unscaledValue(), v.scale(),
                                           BigDecimalLayoutForm.SCIENTIFIC);

                char[] mant = bdl.mantissa();

                // 如果需要，添加小数点。如果比例为零（内部表示没有小数部分）或原始精度为一，则尾数可能不包含小数点。如果设置了 '#' 或需要零填充以达到所需的精度，则追加小数点。
                if ((origPrec == 1 || !bdl.hasDot())
                    && (nzeros > 0 || (f.contains(Flags.ALTERNATE))))
                    mant = addDot(mant);

                // 如果精度大于小数分隔符后的可用数字数量，则添加尾随零。
                mant = trailingZeros(mant, nzeros);

                char[] exp = bdl.exponent();
                int newW = width;
                if (width != -1)
                    newW = adjustWidth(width - exp.length - 1, f, neg);
                localizedMagnitude(sb, mant, f, newW, l);

                sb.append(f.contains(Flags.UPPERCASE) ? 'E' : 'e');

                Flags flags = f.dup().remove(Flags.GROUP);
                char sign = exp[0];
                assert(sign == '+' || sign == '-');
                sb.append(exp[0]);

                char[] tmp = new char[exp.length - 1];
                System.arraycopy(exp, 1, tmp, 0, exp.length - 1);
                sb.append(localizedMagnitude(null, tmp, flags, -1, l));
            } else if (c == Conversion.DECIMAL_FLOAT) {
                // 创建具有所需精度的新 BigDecimal。
                int prec = (precision == -1 ? 6 : precision);
                int scale = value.scale();

                if (scale > prec) {
                    // 请求的精度小于比例的位数
                    int compPrec = value.precision();
                    if (compPrec <= scale) {
                        // 0.xxxxxx 的情况
                        value = value.setScale(prec, RoundingMode.HALF_UP);
                    } else {
                        compPrec -= (scale - prec);
                        value = new BigDecimal(value.unscaledValue(),
                                               scale,
                                               new MathContext(compPrec));
                    }
                }
                BigDecimalLayout bdl = new BigDecimalLayout(
                                           value.unscaledValue(),
                                           value.scale(),
                                           BigDecimalLayoutForm.DECIMAL_FLOAT);

                char mant[] = bdl.mantissa();
                int nzeros = (bdl.scale() < prec ? prec - bdl.scale() : 0);

                // 如果需要，添加小数点。如果比例为零（内部表示没有小数部分），则尾数可能不包含小数点。如果设置了 '#' 或需要零填充以达到所需的精度，则追加小数点。
                if (bdl.scale() == 0 && (f.contains(Flags.ALTERNATE) || nzeros > 0))
                    mant = addDot(bdl.mantissa());

                // 如果精度大于小数分隔符后的可用数字数量，则添加尾随零。
                mant = trailingZeros(mant, nzeros);

                localizedMagnitude(sb, mant, f, adjustWidth(width, f, neg), l);
            } else if (c == Conversion.GENERAL) {
                int prec = precision;
                if (precision == -1)
                    prec = 6;
                else if (precision == 0)
                    prec = 1;

                BigDecimal tenToTheNegFour = BigDecimal.valueOf(1, 4);
                BigDecimal tenToThePrec = BigDecimal.valueOf(1, -prec);
                if ((value.equals(BigDecimal.ZERO))
                    || ((value.compareTo(tenToTheNegFour) != -1)
                        && (value.compareTo(tenToThePrec) == -1))) {

                    int e = - value.scale()
                        + (value.unscaledValue().toString().length() - 1);

                    // xxx.yyy
                    //   g 精度（# 有效数字） = #x + #y
                    //   f 精度 = #y
                    //   指数 = #x - 1
                    // => f 精度 = g 精度 - 指数 - 1
                    // 0.000zzz
                    //   g 精度（# 有效数字） = #z
                    //   f 精度 = #0（小数点后） + #z
                    //   指数 = - #0（小数点后） - 1
                    // => f 精度 = g 精度 - 指数 - 1
                    prec = prec - e - 1;

                    print(sb, value, l, f, Conversion.DECIMAL_FLOAT, prec,
                          neg);
                } else {
                    print(sb, value, l, f, Conversion.SCIENTIFIC, prec - 1, neg);
                }
            } else if (c == Conversion.HEXADECIMAL_FLOAT) {
                // 此转换不受支持。错误应在更早时报告。
                assert false;
            }
        }

        private class BigDecimalLayout {
            private StringBuilder mant;
            private StringBuilder exp;
            private boolean dot = false;
            private int scale;

            public BigDecimalLayout(BigInteger intVal, int scale, BigDecimalLayoutForm form) {
                layout(intVal, scale, form);
            }

            public boolean hasDot() {
                return dot;
            }

            public int scale() {
                return scale;
            }

            // 具有规范字符串表示的 char[]
            public char[] layoutChars() {
                StringBuilder sb = new StringBuilder(mant);
                if (exp != null) {
                    sb.append('E');
                    sb.append(exp);
                }
                return toCharArray(sb);
            }

            public char[] mantissa() {
                return toCharArray(mant);
            }

            // 指数将格式化为一个符号（'+' 或 '-'），后跟至少两位的零填充指数。
            public char[] exponent() {
                return toCharArray(exp);
            }

            private char[] toCharArray(StringBuilder sb) {
                if (sb == null)
                    return null;
                char[] result = new char[sb.length()];
                sb.getChars(0, result.length, result, 0);
                return result;
            }

            private void layout(BigInteger intVal, int scale, BigDecimalLayoutForm form) {
                char coeff[] = intVal.toString().toCharArray();
                this.scale = scale;

                // 构造一个缓冲区，容量足以应对所有情况。
                // 如果需要 E-notation，长度将为：+1 如果为负，+1 如果需要 '.'，+2 用于 "E+"，+ 最多 10 位调整后的指数。否则，它可以有 +1 如果为负，加上前导 "0.00000"
                mant = new StringBuilder(coeff.length + 14);

                if (scale == 0) {
                    int len = coeff.length;
                    if (len > 1) {
                        mant.append(coeff[0]);
                        if (form == BigDecimalLayoutForm.SCIENTIFIC) {
                            mant.append('.');
                            dot = true;
                            mant.append(coeff, 1, len - 1);
                            exp = new StringBuilder("+");
                            if (len < 10)
                                exp.append("0").append(len - 1);
                            else
                                exp.append(len - 1);
                        } else {
                            mant.append(coeff, 1, len - 1);
                        }
                    } else {
                        mant.append(coeff);
                        if (form == BigDecimalLayoutForm.SCIENTIFIC)
                            exp = new StringBuilder("+00");
                    }
                    return;
                }
                long adjusted = -(long) scale + (coeff.length - 1);
                if (form == BigDecimalLayoutForm.DECIMAL_FLOAT) {
                    // 填充零的数量
                    int pad = scale - coeff.length;
                    if (pad >= 0) {
                        // 0.xxx 形式
                        mant.append("0.");
                        dot = true;
                        for (; pad > 0 ; pad--) mant.append('0');
                        mant.append(coeff);
                    } else {
                        if (-pad < coeff.length) {
                            // xx.xx 形式
                            mant.append(coeff, 0, -pad);
                            mant.append('.');
                            dot = true;
                            mant.append(coeff, -pad, scale);
                        } else {
                            // xx 形式
                            mant.append(coeff, 0, coeff.length);
                            for (int i = 0; i < -scale; i++)
                                mant.append('0');
                            this.scale = 0;
                        }
                    }
                } else {
                    // x.xxx 形式
                    mant.append(coeff[0]);
                    if (coeff.length > 1) {
                        mant.append('.');
                        dot = true;
                        mant.append(coeff, 1, coeff.length-1);
                    }
                    exp = new StringBuilder();
                    if (adjusted != 0) {
                        long abs = Math.abs(adjusted);
                        // 需要符号
                        exp.append(adjusted < 0 ? '-' : '+');
                        if (abs < 10)
                            exp.append('0');
                        exp.append(abs);
                    } else {
                        exp.append("+00");
                    }
                }
            }
        }

        private int adjustWidth(int width, Flags f, boolean neg) {
            int newW = width;
            if (newW != -1 && neg && f.contains(Flags.PARENTHESES))
                newW--;
            return newW;
        }

        // 如果需要，向尾数添加 '.'
        private char[] addDot(char[] mant) {
            char[] tmp = mant;
            tmp = new char[mant.length + 1];
            System.arraycopy(mant, 0, tmp, 0, mant.length);
            tmp[tmp.length - 1] = '.';
            return tmp;
        }

        // 如果精度大于小数分隔符后的可用数字数量，则添加尾随零。
        private char[] trailingZeros(char[] mant, int nzeros) {
            char[] tmp = mant;
            if (nzeros > 0) {
                tmp = new char[mant.length + nzeros];
                System.arraycopy(mant, 0, tmp, 0, mant.length);
                for (int i = mant.length; i < tmp.length; i++)
                    tmp[i] = '0';
            }
            return tmp;
        }

        private void print(Calendar t, char c, Locale l)  throws IOException
        {
            StringBuilder sb = new StringBuilder();
            print(sb, t, c, l);

            // 根据宽度调整
            String s = justify(sb.toString());
            if (f.contains(Flags.UPPERCASE))
                s = s.toUpperCase();

            a.append(s);
        }

        private Appendable print(StringBuilder sb, Calendar t, char c,
                                 Locale l)
            throws IOException
        {
            if (sb == null)
                sb = new StringBuilder();
            switch (c) {
            case DateTime.HOUR_OF_DAY_0: // 'H' (00 - 23)
            case DateTime.HOUR_0:        // 'I' (01 - 12)
            case DateTime.HOUR_OF_DAY:   // 'k' (0 - 23) -- 类似于 H
            case DateTime.HOUR:        { // 'l' (1 - 12) -- 类似于 I
                int i = t.get(Calendar.HOUR_OF_DAY);
                if (c == DateTime.HOUR_0 || c == DateTime.HOUR)
                    i = (i == 0 || i == 12 ? 12 : i % 12);
                Flags flags = (c == DateTime.HOUR_OF_DAY_0
                               || c == DateTime.HOUR_0
                               ? Flags.ZERO_PAD
                               : Flags.NONE);
                sb.append(localizedMagnitude(null, i, flags, 2, l));
                break;
            }
            case DateTime.MINUTE:      { // 'M' (00 - 59)
                int i = t.get(Calendar.MINUTE);
                Flags flags = Flags.ZERO_PAD;
                sb.append(localizedMagnitude(null, i, flags, 2, l));
                break;
            }
            case DateTime.NANOSECOND:  { // 'N' (000000000 - 999999999)
                int i = t.get(Calendar.MILLISECOND) * 1000000;
                Flags flags = Flags.ZERO_PAD;
                sb.append(localizedMagnitude(null, i, flags, 9, l));
                break;
            }
            case DateTime.MILLISECOND: { // 'L' (000 - 999)
                int i = t.get(Calendar.MILLISECOND);
                Flags flags = Flags.ZERO_PAD;
                sb.append(localizedMagnitude(null, i, flags, 3, l));
                break;
            }
            case DateTime.MILLISECOND_SINCE_EPOCH: { // 'Q' (0 - 99...?)
                long i = t.getTimeInMillis();
                Flags flags = Flags.NONE;
                sb.append(localizedMagnitude(null, i, flags, width, l));
                break;
            }
            case DateTime.AM_PM:       { // 'p' (am 或 pm)
                // Calendar.AM = 0, Calendar.PM = 1, LocaleElements 定义大写
                String[] ampm = { "AM", "PM" };
                if (l != null && l != Locale.US) {
                    DateFormatSymbols dfs = DateFormatSymbols.getInstance(l);
                    ampm = dfs.getAmPmStrings();
                }
                String s = ampm[t.get(Calendar.AM_PM)];
                sb.append(s.toLowerCase(l != null ? l : Locale.US));
                break;
            }
            case DateTime.SECONDS_SINCE_EPOCH: { // 's' (0 - 99...?)
                long i = t.getTimeInMillis() / 1000;
                Flags flags = Flags.NONE;
                sb.append(localizedMagnitude(null, i, flags, width, l));
                break;
            }
            case DateTime.SECOND:      { // 'S' (00 - 60 - 跃秒)
                int i = t.get(Calendar.SECOND);
                Flags flags = Flags.ZERO_PAD;
                sb.append(localizedMagnitude(null, i, flags, 2, l));
                break;
            }
            case DateTime.ZONE_NUMERIC: { // 'z' ({-|+}####) - ls 减号？
                int i = t.get(Calendar.ZONE_OFFSET) + t.get(Calendar.DST_OFFSET);
                boolean neg = i < 0;
                sb.append(neg ? '-' : '+');
                if (neg)
                    i = -i;
                int min = i / 60000;
                // 将分钟和小时组合成一个整数
                int offset = (min / 60) * 100 + (min % 60);
                Flags flags = Flags.ZERO_PAD;


                            sb.append(localizedMagnitude(null, offset, flags, 4, l));
                break;
            }
            case DateTime.ZONE:        { // 'Z' (符号)
                TimeZone tz = t.getTimeZone();
                sb.append(tz.getDisplayName((t.get(Calendar.DST_OFFSET) != 0),
                                           TimeZone.SHORT,
                                            (l == null) ? Locale.US : l));
                break;
            }

            // 日期
            case DateTime.NAME_OF_DAY_ABBREV:     // 'a'
            case DateTime.NAME_OF_DAY:          { // 'A'
                int i = t.get(Calendar.DAY_OF_WEEK);
                Locale lt = ((l == null) ? Locale.US : l);
                DateFormatSymbols dfs = DateFormatSymbols.getInstance(lt);
                if (c == DateTime.NAME_OF_DAY)
                    sb.append(dfs.getWeekdays()[i]);
                else
                    sb.append(dfs.getShortWeekdays()[i]);
                break;
            }
            case DateTime.NAME_OF_MONTH_ABBREV:   // 'b'
            case DateTime.NAME_OF_MONTH_ABBREV_X: // 'h' -- 与 b 相同
            case DateTime.NAME_OF_MONTH:        { // 'B'
                int i = t.get(Calendar.MONTH);
                Locale lt = ((l == null) ? Locale.US : l);
                DateFormatSymbols dfs = DateFormatSymbols.getInstance(lt);
                if (c == DateTime.NAME_OF_MONTH)
                    sb.append(dfs.getMonths()[i]);
                else
                    sb.append(dfs.getShortMonths()[i]);
                break;
            }
            case DateTime.CENTURY:                // 'C' (00 - 99)
            case DateTime.YEAR_2:                 // 'y' (00 - 99)
            case DateTime.YEAR_4:               { // 'Y' (0000 - 9999)
                int i = t.get(Calendar.YEAR);
                int size = 2;
                switch (c) {
                case DateTime.CENTURY:
                    i /= 100;
                    break;
                case DateTime.YEAR_2:
                    i %= 100;
                    break;
                case DateTime.YEAR_4:
                    size = 4;
                    break;
                }
                Flags flags = Flags.ZERO_PAD;
                sb.append(localizedMagnitude(null, i, flags, size, l));
                break;
            }
            case DateTime.DAY_OF_MONTH_0:         // 'd' (01 - 31)
            case DateTime.DAY_OF_MONTH:         { // 'e' (1 - 31) -- 与 d 相同
                int i = t.get(Calendar.DATE);
                Flags flags = (c == DateTime.DAY_OF_MONTH_0
                               ? Flags.ZERO_PAD
                               : Flags.NONE);
                sb.append(localizedMagnitude(null, i, flags, 2, l));
                break;
            }
            case DateTime.DAY_OF_YEAR:          { // 'j' (001 - 366)
                int i = t.get(Calendar.DAY_OF_YEAR);
                Flags flags = Flags.ZERO_PAD;
                sb.append(localizedMagnitude(null, i, flags, 3, l));
                break;
            }
            case DateTime.MONTH:                { // 'm' (01 - 12)
                int i = t.get(Calendar.MONTH) + 1;
                Flags flags = Flags.ZERO_PAD;
                sb.append(localizedMagnitude(null, i, flags, 2, l));
                break;
            }

            // 组合
            case DateTime.TIME:         // 'T' (24 小时 hh:mm:ss - %tH:%tM:%tS)
            case DateTime.TIME_24_HOUR:    { // 'R' (hh:mm 与 %H:%M 相同)
                char sep = ':';
                print(sb, t, DateTime.HOUR_OF_DAY_0, l).append(sep);
                print(sb, t, DateTime.MINUTE, l);
                if (c == DateTime.TIME) {
                    sb.append(sep);
                    print(sb, t, DateTime.SECOND, l);
                }
                break;
            }
            case DateTime.TIME_12_HOUR:    { // 'r' (hh:mm:ss [AP]M)
                char sep = ':';
                print(sb, t, DateTime.HOUR_0, l).append(sep);
                print(sb, t, DateTime.MINUTE, l).append(sep);
                print(sb, t, DateTime.SECOND, l).append(' ');
                // 对于某些语言环境，这可能位置不正确
                StringBuilder tsb = new StringBuilder();
                print(tsb, t, DateTime.AM_PM, l);
                sb.append(tsb.toString().toUpperCase(l != null ? l : Locale.US));
                break;
            }
            case DateTime.DATE_TIME:    { // 'c' (Sat Nov 04 12:02:33 EST 1999)
                char sep = ' ';
                print(sb, t, DateTime.NAME_OF_DAY_ABBREV, l).append(sep);
                print(sb, t, DateTime.NAME_OF_MONTH_ABBREV, l).append(sep);
                print(sb, t, DateTime.DAY_OF_MONTH_0, l).append(sep);
                print(sb, t, DateTime.TIME, l).append(sep);
                print(sb, t, DateTime.ZONE, l).append(sep);
                print(sb, t, DateTime.YEAR_4, l);
                break;
            }
            case DateTime.DATE:            { // 'D' (mm/dd/yy)
                char sep = '/';
                print(sb, t, DateTime.MONTH, l).append(sep);
                print(sb, t, DateTime.DAY_OF_MONTH_0, l).append(sep);
                print(sb, t, DateTime.YEAR_2, l);
                break;
            }
            case DateTime.ISO_STANDARD_DATE: { // 'F' (%Y-%m-%d)
                char sep = '-';
                print(sb, t, DateTime.YEAR_4, l).append(sep);
                print(sb, t, DateTime.MONTH, l).append(sep);
                print(sb, t, DateTime.DAY_OF_MONTH_0, l);
                break;
            }
            default:
                assert false;
            }
            return sb;
        }

        private void print(TemporalAccessor t, char c, Locale l)  throws IOException {
            StringBuilder sb = new StringBuilder();
            print(sb, t, c, l);
            // 根据宽度进行对齐
            String s = justify(sb.toString());
            if (f.contains(Flags.UPPERCASE))
                s = s.toUpperCase();
            a.append(s);
        }

        private Appendable print(StringBuilder sb, TemporalAccessor t, char c,
                                 Locale l) throws IOException {
            if (sb == null)
                sb = new StringBuilder();
            try {
                switch (c) {
                case DateTime.HOUR_OF_DAY_0: {  // 'H' (00 - 23)
                    int i = t.get(ChronoField.HOUR_OF_DAY);
                    sb.append(localizedMagnitude(null, i, Flags.ZERO_PAD, 2, l));
                    break;
                }
                case DateTime.HOUR_OF_DAY: {   // 'k' (0 - 23) -- 与 H 相同
                    int i = t.get(ChronoField.HOUR_OF_DAY);
                    sb.append(localizedMagnitude(null, i, Flags.NONE, 2, l));
                    break;
                }
                case DateTime.HOUR_0:      {  // 'I' (01 - 12)
                    int i = t.get(ChronoField.CLOCK_HOUR_OF_AMPM);
                    sb.append(localizedMagnitude(null, i, Flags.ZERO_PAD, 2, l));
                    break;
                }
                case DateTime.HOUR:        { // 'l' (1 - 12) -- 与 I 相同
                    int i = t.get(ChronoField.CLOCK_HOUR_OF_AMPM);
                    sb.append(localizedMagnitude(null, i, Flags.NONE, 2, l));
                    break;
                }
                case DateTime.MINUTE:      { // 'M' (00 - 59)
                    int i = t.get(ChronoField.MINUTE_OF_HOUR);
                    Flags flags = Flags.ZERO_PAD;
                    sb.append(localizedMagnitude(null, i, flags, 2, l));
                    break;
                }
                case DateTime.NANOSECOND:  { // 'N' (000000000 - 999999999)
                    int i = t.get(ChronoField.MILLI_OF_SECOND) * 1000000;
                    Flags flags = Flags.ZERO_PAD;
                    sb.append(localizedMagnitude(null, i, flags, 9, l));
                    break;
                }
                case DateTime.MILLISECOND: { // 'L' (000 - 999)
                    int i = t.get(ChronoField.MILLI_OF_SECOND);
                    Flags flags = Flags.ZERO_PAD;
                    sb.append(localizedMagnitude(null, i, flags, 3, l));
                    break;
                }
                case DateTime.MILLISECOND_SINCE_EPOCH: { // 'Q' (0 - 99...?)
                    long i = t.getLong(ChronoField.INSTANT_SECONDS) * 1000L +
                             t.getLong(ChronoField.MILLI_OF_SECOND);
                    Flags flags = Flags.NONE;
                    sb.append(localizedMagnitude(null, i, flags, width, l));
                    break;
                }
                case DateTime.AM_PM:       { // 'p' (am 或 pm)
                    // Calendar.AM = 0, Calendar.PM = 1, LocaleElements 定义大写
                    String[] ampm = { "AM", "PM" };
                    if (l != null && l != Locale.US) {
                        DateFormatSymbols dfs = DateFormatSymbols.getInstance(l);
                        ampm = dfs.getAmPmStrings();
                    }
                    String s = ampm[t.get(ChronoField.AMPM_OF_DAY)];
                    sb.append(s.toLowerCase(l != null ? l : Locale.US));
                    break;
                }
                case DateTime.SECONDS_SINCE_EPOCH: { // 's' (0 - 99...?)
                    long i = t.getLong(ChronoField.INSTANT_SECONDS);
                    Flags flags = Flags.NONE;
                    sb.append(localizedMagnitude(null, i, flags, width, l));
                    break;
                }
                case DateTime.SECOND:      { // 'S' (00 - 60 - 跃秒)
                    int i = t.get(ChronoField.SECOND_OF_MINUTE);
                    Flags flags = Flags.ZERO_PAD;
                    sb.append(localizedMagnitude(null, i, flags, 2, l));
                    break;
                }
                case DateTime.ZONE_NUMERIC: { // 'z' ({-|+}####) - ls 减号？
                    int i = t.get(ChronoField.OFFSET_SECONDS);
                    boolean neg = i < 0;
                    sb.append(neg ? '-' : '+');
                    if (neg)
                        i = -i;
                    int min = i / 60;
                    // 将分钟和小时组合成一个整数
                    int offset = (min / 60) * 100 + (min % 60);
                    Flags flags = Flags.ZERO_PAD;
                    sb.append(localizedMagnitude(null, offset, flags, 4, l));
                    break;
                }
                case DateTime.ZONE:        { // 'Z' (符号)
                    ZoneId zid = t.query(TemporalQueries.zone());
                    if (zid == null) {
                        throw new IllegalFormatConversionException(c, t.getClass());
                    }
                    if (!(zid instanceof ZoneOffset) &&
                        t.isSupported(ChronoField.INSTANT_SECONDS)) {
                        Instant instant = Instant.from(t);
                        sb.append(TimeZone.getTimeZone(zid.getId())
                                          .getDisplayName(zid.getRules().isDaylightSavings(instant),
                                                          TimeZone.SHORT,
                                                          (l == null) ? Locale.US : l));
                        break;
                    }
                    sb.append(zid.getId());
                    break;
                }
                // 日期
                case DateTime.NAME_OF_DAY_ABBREV:     // 'a'
                case DateTime.NAME_OF_DAY:          { // 'A'
                    int i = t.get(ChronoField.DAY_OF_WEEK) % 7 + 1;
                    Locale lt = ((l == null) ? Locale.US : l);
                    DateFormatSymbols dfs = DateFormatSymbols.getInstance(lt);
                    if (c == DateTime.NAME_OF_DAY)
                        sb.append(dfs.getWeekdays()[i]);
                    else
                        sb.append(dfs.getShortWeekdays()[i]);
                    break;
                }
                case DateTime.NAME_OF_MONTH_ABBREV:   // 'b'
                case DateTime.NAME_OF_MONTH_ABBREV_X: // 'h' -- 与 b 相同
                case DateTime.NAME_OF_MONTH:        { // 'B'
                    int i = t.get(ChronoField.MONTH_OF_YEAR) - 1;
                    Locale lt = ((l == null) ? Locale.US : l);
                    DateFormatSymbols dfs = DateFormatSymbols.getInstance(lt);
                    if (c == DateTime.NAME_OF_MONTH)
                        sb.append(dfs.getMonths()[i]);
                    else
                        sb.append(dfs.getShortMonths()[i]);
                    break;
                }
                case DateTime.CENTURY:                // 'C' (00 - 99)
                case DateTime.YEAR_2:                 // 'y' (00 - 99)
                case DateTime.YEAR_4:               { // 'Y' (0000 - 9999)
                    int i = t.get(ChronoField.YEAR_OF_ERA);
                    int size = 2;
                    switch (c) {
                    case DateTime.CENTURY:
                        i /= 100;
                        break;
                    case DateTime.YEAR_2:
                        i %= 100;
                        break;
                    case DateTime.YEAR_4:
                        size = 4;
                        break;
                    }
                    Flags flags = Flags.ZERO_PAD;
                    sb.append(localizedMagnitude(null, i, flags, size, l));
                    break;
                }
                case DateTime.DAY_OF_MONTH_0:         // 'd' (01 - 31)
                case DateTime.DAY_OF_MONTH:         { // 'e' (1 - 31) -- 与 d 相同
                    int i = t.get(ChronoField.DAY_OF_MONTH);
                    Flags flags = (c == DateTime.DAY_OF_MONTH_0
                                   ? Flags.ZERO_PAD
                                   : Flags.NONE);
                    sb.append(localizedMagnitude(null, i, flags, 2, l));
                    break;
                }
                case DateTime.DAY_OF_YEAR:          { // 'j' (001 - 366)
                    int i = t.get(ChronoField.DAY_OF_YEAR);
                    Flags flags = Flags.ZERO_PAD;
                    sb.append(localizedMagnitude(null, i, flags, 3, l));
                    break;
                }
                case DateTime.MONTH:                { // 'm' (01 - 12)
                    int i = t.get(ChronoField.MONTH_OF_YEAR);
                    Flags flags = Flags.ZERO_PAD;
                    sb.append(localizedMagnitude(null, i, flags, 2, l));
                    break;
                }

                // 组合
                case DateTime.TIME:         // 'T' (24 小时 hh:mm:ss - %tH:%tM:%tS)
                case DateTime.TIME_24_HOUR:    { // 'R' (hh:mm 与 %H:%M 相同)
                    char sep = ':';
                    print(sb, t, DateTime.HOUR_OF_DAY_0, l).append(sep);
                    print(sb, t, DateTime.MINUTE, l);
                    if (c == DateTime.TIME) {
                        sb.append(sep);
                        print(sb, t, DateTime.SECOND, l);
                    }
                    break;
                }
                case DateTime.TIME_12_HOUR:    { // 'r' (hh:mm:ss [AP]M)
                    char sep = ':';
                    print(sb, t, DateTime.HOUR_0, l).append(sep);
                    print(sb, t, DateTime.MINUTE, l).append(sep);
                    print(sb, t, DateTime.SECOND, l).append(' ');
                    // 对于某些语言环境，这可能位置不正确
                    StringBuilder tsb = new StringBuilder();
                    print(tsb, t, DateTime.AM_PM, l);
                    sb.append(tsb.toString().toUpperCase(l != null ? l : Locale.US));
                    break;
                }
                case DateTime.DATE_TIME:    { // 'c' (Sat Nov 04 12:02:33 EST 1999)
                    char sep = ' ';
                    print(sb, t, DateTime.NAME_OF_DAY_ABBREV, l).append(sep);
                    print(sb, t, DateTime.NAME_OF_MONTH_ABBREV, l).append(sep);
                    print(sb, t, DateTime.DAY_OF_MONTH_0, l).append(sep);
                    print(sb, t, DateTime.TIME, l).append(sep);
                    print(sb, t, DateTime.ZONE, l).append(sep);
                    print(sb, t, DateTime.YEAR_4, l);
                    break;
                }
                case DateTime.DATE:            { // 'D' (mm/dd/yy)
                    char sep = '/';
                    print(sb, t, DateTime.MONTH, l).append(sep);
                    print(sb, t, DateTime.DAY_OF_MONTH_0, l).append(sep);
                    print(sb, t, DateTime.YEAR_2, l);
                    break;
                }
                case DateTime.ISO_STANDARD_DATE: { // 'F' (%Y-%m-%d)
                    char sep = '-';
                    print(sb, t, DateTime.YEAR_4, l).append(sep);
                    print(sb, t, DateTime.MONTH, l).append(sep);
                    print(sb, t, DateTime.DAY_OF_MONTH_0, l);
                    break;
                }
                default:
                    assert false;
                }
            } catch (DateTimeException x) {
                throw new IllegalFormatConversionException(c, t.getClass());
            }
            return sb;
        }


                    // -- 支持抛出异常的方法 --

        private void failMismatch(Flags f, char c) {
            String fs = f.toString();
            throw new FormatFlagsConversionMismatchException(fs, c);
        }

        private void failConversion(char c, Object arg) {
            throw new IllegalFormatConversionException(c, arg.getClass());
        }

        private char getZero(Locale l) {
            if ((l != null) &&  !l.equals(locale())) {
                DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(l);
                return dfs.getZeroDigit();
            }
            return zero;
        }

        private StringBuilder
            localizedMagnitude(StringBuilder sb, long value, Flags f,
                               int width, Locale l)
        {
            char[] va = Long.toString(value, 10).toCharArray();
            return localizedMagnitude(sb, va, f, width, l);
        }

        private StringBuilder
            localizedMagnitude(StringBuilder sb, char[] value, Flags f,
                               int width, Locale l)
        {
            if (sb == null)
                sb = new StringBuilder();
            int begin = sb.length();

            char zero = getZero(l);

            // 确定本地化分组分隔符和大小
            char grpSep = '\0';
            int  grpSize = -1;
            char decSep = '\0';

            int len = value.length;
            int dot = len;
            for (int j = 0; j < len; j++) {
                if (value[j] == '.') {
                    dot = j;
                    break;
                }
            }

            if (dot < len) {
                if (l == null || l.equals(Locale.US)) {
                    decSep  = '.';
                } else {
                    DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(l);
                    decSep  = dfs.getDecimalSeparator();
                }
            }

            if (f.contains(Flags.GROUP)) {
                if (l == null || l.equals(Locale.US)) {
                    grpSep = ',';
                    grpSize = 3;
                } else {
                    DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(l);
                    grpSep = dfs.getGroupingSeparator();
                    DecimalFormat df = (DecimalFormat) NumberFormat.getIntegerInstance(l);
                    grpSize = df.getGroupingSize();
                }
            }

            // 本地化数字，必要时插入分组分隔符
            for (int j = 0; j < len; j++) {
                if (j == dot) {
                    sb.append(decSep);
                    // 小数点后不再插入分组分隔符
                    grpSep = '\0';
                    continue;
                }

                char c = value[j];
                sb.append((char) ((c - '0') + zero));
                if (grpSep != '\0' && j != dot - 1 && ((dot - j) % grpSize == 1))
                    sb.append(grpSep);
            }

            // 应用零填充
            len = sb.length();
            if (width > len && f.contains(Flags.ZERO_PAD)) {
                char[] zeros = new char[width - len];
                Arrays.fill(zeros, zero);
                sb.insert(begin, zeros);
            }

            return sb;
        }
    }

    private static class Flags {
        private int flags;

        static final Flags NONE          = new Flags(0);      // ''

        // 从 Formattable.java 复制的声明
        static final Flags LEFT_JUSTIFY  = new Flags(1<<0);   // '-'
        static final Flags UPPERCASE     = new Flags(1<<1);   // '^'
        static final Flags ALTERNATE     = new Flags(1<<2);   // '#'

        // 数值
        static final Flags PLUS          = new Flags(1<<3);   // '+'
        static final Flags LEADING_SPACE = new Flags(1<<4);   // ' '
        static final Flags ZERO_PAD      = new Flags(1<<5);   // '0'
        static final Flags GROUP         = new Flags(1<<6);   // ','
        static final Flags PARENTHESES   = new Flags(1<<7);   // '('

        // 索引
        static final Flags PREVIOUS      = new Flags(1<<8);   // '<'

        private Flags(int f) {
            flags = f;
        }

        public int valueOf() {
            return flags;
        }

        public boolean contains(Flags f) {
            return (flags & f.valueOf()) == f.valueOf();
        }

        public Flags dup() {
            return new Flags(flags);
        }

        private Flags add(Flags f) {
            flags |= f.valueOf();
            return this;
        }

        public Flags remove(Flags f) {
            flags &= ~f.valueOf();
            return this;
        }

        public static Flags parse(String s) {
            char[] ca = s.toCharArray();
            Flags f = new Flags(0);
            for (int i = 0; i < ca.length; i++) {
                Flags v = parse(ca[i]);
                if (f.contains(v))
                    throw new DuplicateFormatFlagsException(v.toString());
                f.add(v);
            }
            return f;
        }

        // 解析用户可能提供的标志
        private static Flags parse(char c) {
            switch (c) {
            case '-': return LEFT_JUSTIFY;
            case '#': return ALTERNATE;
            case '+': return PLUS;
            case ' ': return LEADING_SPACE;
            case '0': return ZERO_PAD;
            case ',': return GROUP;
            case '(': return PARENTHESES;
            case '<': return PREVIOUS;
            default:
                throw new UnknownFormatFlagsException(String.valueOf(c));
            }
        }

        // 返回当前 {@code Flags} 的字符串表示形式
        public static String toString(Flags f) {
            return f.toString();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (contains(LEFT_JUSTIFY))  sb.append('-');
            if (contains(UPPERCASE))     sb.append('^');
            if (contains(ALTERNATE))     sb.append('#');
            if (contains(PLUS))          sb.append('+');
            if (contains(LEADING_SPACE)) sb.append(' ');
            if (contains(ZERO_PAD))      sb.append('0');
            if (contains(GROUP))         sb.append(',');
            if (contains(PARENTHESES))   sb.append('(');
            if (contains(PREVIOUS))      sb.append('<');
            return sb.toString();
        }
    }

    private static class Conversion {
        // Byte, Short, Integer, Long, BigInteger
        // (以及由于自动装箱而关联的原始类型)
        static final char DECIMAL_INTEGER     = 'd';
        static final char OCTAL_INTEGER       = 'o';
        static final char HEXADECIMAL_INTEGER = 'x';
        static final char HEXADECIMAL_INTEGER_UPPER = 'X';

        // Float, Double, BigDecimal
        // (以及由于自动装箱而关联的原始类型)
        static final char SCIENTIFIC          = 'e';
        static final char SCIENTIFIC_UPPER    = 'E';
        static final char GENERAL             = 'g';
        static final char GENERAL_UPPER       = 'G';
        static final char DECIMAL_FLOAT       = 'f';
        static final char HEXADECIMAL_FLOAT   = 'a';
        static final char HEXADECIMAL_FLOAT_UPPER = 'A';

        // Character, Byte, Short, Integer
        // (以及由于自动装箱而关联的原始类型)
        static final char CHARACTER           = 'c';
        static final char CHARACTER_UPPER     = 'C';

        // java.util.Date, java.util.Calendar, long
        static final char DATE_TIME           = 't';
        static final char DATE_TIME_UPPER     = 'T';

        // 如果 (arg.TYPE != boolean) 返回 boolean
        // 如果 (arg != null) 返回 true; 否则返回 false;
        static final char BOOLEAN             = 'b';
        static final char BOOLEAN_UPPER       = 'B';
        // 如果 (arg instanceof Formattable) arg.formatTo()
        // 否则 arg.toString();
        static final char STRING              = 's';
        static final char STRING_UPPER        = 'S';
        // arg.hashCode()
        static final char HASHCODE            = 'h';
        static final char HASHCODE_UPPER      = 'H';

        static final char LINE_SEPARATOR      = 'n';
        static final char PERCENT_SIGN        = '%';

        static boolean isValid(char c) {
            return (isGeneral(c) || isInteger(c) || isFloat(c) || isText(c)
                    || c == 't' || isCharacter(c));
        }

        // 如果转换适用于所有对象，则返回 true
        static boolean isGeneral(char c) {
            switch (c) {
            case BOOLEAN:
            case BOOLEAN_UPPER:
            case STRING:
            case STRING_UPPER:
            case HASHCODE:
            case HASHCODE_UPPER:
                return true;
            default:
                return false;
            }
        }

        // 如果转换适用于字符，则返回 true
        static boolean isCharacter(char c) {
            switch (c) {
            case CHARACTER:
            case CHARACTER_UPPER:
                return true;
            default:
                return false;
            }
        }

        // 如果转换是整数类型，则返回 true
        static boolean isInteger(char c) {
            switch (c) {
            case DECIMAL_INTEGER:
            case OCTAL_INTEGER:
            case HEXADECIMAL_INTEGER:
            case HEXADECIMAL_INTEGER_UPPER:
                return true;
            default:
                return false;
            }
        }

        // 如果转换是浮点类型，则返回 true
        static boolean isFloat(char c) {
            switch (c) {
            case SCIENTIFIC:
            case SCIENTIFIC_UPPER:
            case GENERAL:
            case GENERAL_UPPER:
            case DECIMAL_FLOAT:
            case HEXADECIMAL_FLOAT:
            case HEXADECIMAL_FLOAT_UPPER:
                return true;
            default:
                return false;
            }
        }

        // 如果转换不需要参数，则返回 true
        static boolean isText(char c) {
            switch (c) {
            case LINE_SEPARATOR:
            case PERCENT_SIGN:
                return true;
            default:
                return false;
            }
        }
    }

    private static class DateTime {
        static final char HOUR_OF_DAY_0 = 'H'; // (00 - 23)
        static final char HOUR_0        = 'I'; // (01 - 12)
        static final char HOUR_OF_DAY   = 'k'; // (0 - 23) -- 类似于 H
        static final char HOUR          = 'l'; // (1 - 12) -- 类似于 I
        static final char MINUTE        = 'M'; // (00 - 59)
        static final char NANOSECOND    = 'N'; // (000000000 - 999999999)
        static final char MILLISECOND   = 'L'; // jdk, 不在 gnu 中 (000 - 999)
        static final char MILLISECOND_SINCE_EPOCH = 'Q'; // (0 - 99...?)
        static final char AM_PM         = 'p'; // (am 或 pm)
        static final char SECONDS_SINCE_EPOCH = 's'; // (0 - 99...?)
        static final char SECOND        = 'S'; // (00 - 60 - 跃秒)
        static final char TIME          = 'T'; // (24 小时 hh:mm:ss)
        static final char ZONE_NUMERIC  = 'z'; // (-1200 - +1200) - ls 减号？
        static final char ZONE          = 'Z'; // (符号)

        // 日期
        static final char NAME_OF_DAY_ABBREV    = 'a'; // 'a'
        static final char NAME_OF_DAY           = 'A'; // 'A'
        static final char NAME_OF_MONTH_ABBREV  = 'b'; // 'b'
        static final char NAME_OF_MONTH         = 'B'; // 'B'
        static final char CENTURY               = 'C'; // (00 - 99)
        static final char DAY_OF_MONTH_0        = 'd'; // (01 - 31)
        static final char DAY_OF_MONTH          = 'e'; // (1 - 31) -- 类似于 d
// *    static final char ISO_WEEK_OF_YEAR_2    = 'g'; // 跨 %y %V
// *    static final char ISO_WEEK_OF_YEAR_4    = 'G'; // 跨 %Y %V
        static final char NAME_OF_MONTH_ABBREV_X  = 'h'; // -- 同 b
        static final char DAY_OF_YEAR           = 'j'; // (001 - 366)
        static final char MONTH                 = 'm'; // (01 - 12)
// *    static final char DAY_OF_WEEK_1         = 'u'; // (1 - 7) 星期一
// *    static final char WEEK_OF_YEAR_SUNDAY   = 'U'; // (0 - 53) 星期日+
// *    static final char WEEK_OF_YEAR_MONDAY_01 = 'V'; // (01 - 53) 星期一+
// *    static final char DAY_OF_WEEK_0         = 'w'; // (0 - 6) 星期日
// *    static final char WEEK_OF_YEAR_MONDAY   = 'W'; // (00 - 53) 星期一
        static final char YEAR_2                = 'y'; // (00 - 99)
        static final char YEAR_4                = 'Y'; // (0000 - 9999)

        // 组合
        static final char TIME_12_HOUR  = 'r'; // (hh:mm:ss [AP]M)
        static final char TIME_24_HOUR  = 'R'; // (hh:mm 与 %H:%M 相同)
// *    static final char LOCALE_TIME   = 'X'; // (%H:%M:%S) - 解析格式？
        static final char DATE_TIME             = 'c';
                                            // (Sat Nov 04 12:02:33 EST 1999)
        static final char DATE                  = 'D'; // (mm/dd/yy)
        static final char ISO_STANDARD_DATE     = 'F'; // (%Y-%m-%d)
// *    static final char LOCALE_DATE           = 'x'; // (mm/dd/yy)

        static boolean isValid(char c) {
            switch (c) {
            case HOUR_OF_DAY_0:
            case HOUR_0:
            case HOUR_OF_DAY:
            case HOUR:
            case MINUTE:
            case NANOSECOND:
            case MILLISECOND:
            case MILLISECOND_SINCE_EPOCH:
            case AM_PM:
            case SECONDS_SINCE_EPOCH:
            case SECOND:
            case TIME:
            case ZONE_NUMERIC:
            case ZONE:

            // 日期
            case NAME_OF_DAY_ABBREV:
            case NAME_OF_DAY:
            case NAME_OF_MONTH_ABBREV:
            case NAME_OF_MONTH:
            case CENTURY:
            case DAY_OF_MONTH_0:
            case DAY_OF_MONTH:
// *        case ISO_WEEK_OF_YEAR_2:
// *        case ISO_WEEK_OF_YEAR_4:
            case NAME_OF_MONTH_ABBREV_X:
            case DAY_OF_YEAR:
            case MONTH:
// *        case DAY_OF_WEEK_1:
// *        case WEEK_OF_YEAR_SUNDAY:
// *        case WEEK_OF_YEAR_MONDAY_01:
// *        case DAY_OF_WEEK_0:
// *        case WEEK_OF_YEAR_MONDAY:
            case YEAR_2:
            case YEAR_4:

            // 组合
            case TIME_12_HOUR:
            case TIME_24_HOUR:
// *        case LOCALE_TIME:
            case DATE_TIME:
            case DATE:
            case ISO_STANDARD_DATE:
// *        case LOCALE_DATE:
                return true;
            default:
                return false;
            }
        }
    }
}
