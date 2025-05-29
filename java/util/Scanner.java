
/*
 * 版权所有 (c) 2003, 2020，Oracle 及/或其附属公司。保留所有权利。
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

package java.util;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.regex.*;
import java.io.*;
import java.math.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.text.*;
import java.util.Locale;

import sun.misc.LRUCache;

/**
 * 一个简单的文本扫描器，可以使用正则表达式解析基本类型和字符串。
 *
 * <p>一个 <code>Scanner</code> 使用分隔符模式将其输入分解为标记，默认情况下该模式匹配空白字符。然后可以使用各种 <tt>next</tt> 方法将生成的标记转换为不同类型的值。
 *
 * <p>例如，以下代码允许用户从 <tt>System.in</tt> 读取一个数字：
 * <blockquote><pre>{@code
 *     Scanner sc = new Scanner(System.in);
 *     int i = sc.nextInt();
 * }</pre></blockquote>
 *
 * <p>作为另一个例子，以下代码允许从文件 <code>myNumbers</code> 中的条目分配 <code>long</code> 类型：
 * <blockquote><pre>{@code
 *      Scanner sc = new Scanner(new File("myNumbers"));
 *      while (sc.hasNextLong()) {
 *          long aLong = sc.nextLong();
 *      }
 * }</pre></blockquote>
 *
 * <p>扫描器还可以使用非空白字符的分隔符。例如，以下代码从字符串中读取多个项目：
 * <blockquote><pre>{@code
 *     String input = "1 fish 2 fish red fish blue fish";
 *     Scanner s = new Scanner(input).useDelimiter("\\s*fish\\s*");
 *     System.out.println(s.nextInt());
 *     System.out.println(s.nextInt());
 *     System.out.println(s.next());
 *     System.out.println(s.next());
 *     s.close();
 * }</pre></blockquote>
 * <p>
 * 打印以下输出：
 * <blockquote><pre>{@code
 *     1
 *     2
 *     red
 *     blue
 * }</pre></blockquote>
 *
 * <p>使用正则表达式解析所有四个标记的代码也可以生成相同的输出：
 * <blockquote><pre>{@code
 *     String input = "1 fish 2 fish red fish blue fish";
 *     Scanner s = new Scanner(input);
 *     s.findInLine("(\\d+) fish (\\d+) fish (\\w+) fish (\\w+)");
 *     MatchResult result = s.match();
 *     for (int i=1; i<=result.groupCount(); i++)
 *         System.out.println(result.group(i));
 *     s.close();
 * }</pre></blockquote>
 *
 * <p><a name="default-delimiter">默认空白分隔符</a> 是由 {@link java.lang.Character}.{@link
 * java.lang.Character#isWhitespace(char) isWhitespace} 识别的。{@link #reset} 方法将扫描器的分隔符重置为默认的空白分隔符，无论其是否之前已更改。
 *
 * <p>扫描操作可能会阻塞等待输入。
 *
 * <p>{@link #next} 和 {@link #hasNext} 方法及其基本类型伴生方法（如 {@link #nextInt} 和
 * {@link #hasNextInt}）首先跳过与分隔符模式匹配的任何输入，然后尝试返回下一个标记。两个 <tt>hasNext</tt>
 * 和 <tt>next</tt> 方法都可能阻塞等待进一步的输入。一个 <tt>hasNext</tt> 方法是否阻塞与它的 <tt>next</tt> 方法是否阻塞没有关系。
 *
 * <p>{@link #findInLine}、{@link #findWithinHorizon} 和 {@link #skip} 方法独立于分隔符模式运行。这些方法将尝试匹配指定的模式，而不考虑输入中的分隔符，因此可以在分隔符不相关的特殊情况下使用。这些方法可能会阻塞等待更多输入。
 *
 * <p>当扫描器抛出 {@link InputMismatchException} 时，扫描器不会跳过导致异常的标记，因此可以通过其他方法检索或跳过该标记。
 *
 * <p>根据分隔符模式的不同，可能会返回空标记。例如，模式 <tt>"\\s+"</tt> 不会返回空标记，因为它匹配多个分隔符实例。分隔符模式 <tt>"\\s"</tt> 可能会返回空标记，因为它一次只通过一个空格。
 *
 * <p>扫描器可以从任何实现 {@link java.lang.Readable} 接口的对象读取文本。如果底层可读对象的 {@link java.lang.Readable#read} 方法抛出 {@link
 * java.io.IOException}，则扫描器假定输入已结束。可以通过 {@link #ioException} 方法检索底层可读对象最近抛出的 <tt>IOException</tt>。
 *
 * <p>当 <code>Scanner</code> 关闭时，如果输入源实现了 {@link java.io.Closeable} 接口，它将关闭输入源。
 *
 * <p>没有外部同步的情况下，<code>Scanner</code> 不适用于多线程使用。
 *
 * <p>除非另有说明，将 <code>null</code> 参数传递给 <code>Scanner</code> 的任何方法将导致抛出 <code>NullPointerException</code>。
 *
 * <p>除非使用 {@link #useRadix} 方法设置了不同的基数，否则扫描器将默认以十进制解释数字。{@link #reset} 方法将扫描器的基数重置为 <code>10</code>，无论其是否之前已更改。
 *
 * <h3> <a name="localized-numbers">本地化数字</a> </h3>
 *
 * <p>此类的实例能够扫描标准格式以及扫描器所在地区的格式中的数字。扫描器的 <a name="initial-locale">初始地区</a> 是 {@link
 * java.util.Locale#getDefault(Locale.Category)
 * Locale.getDefault(Locale.Category.FORMAT)} 方法返回的值；可以通过 {@link #useLocale} 方法更改。{@link #reset} 方法将扫描器的地区重置为初始地区，无论其是否之前已更改。
 *
 * <p>本地化格式定义为以下参数，这些参数对于特定地区，从该地区的 {@link java.text.DecimalFormat DecimalFormat} 对象 <tt>df</tt> 和其
 * {@link java.text.DecimalFormatSymbols DecimalFormatSymbols} 对象 <tt>dfs</tt> 中获取。
 *
 * <blockquote><dl>
 *     <dt><i>本地组分隔符&nbsp;&nbsp;</i>
 *         <dd>用于分隔千位的字符，<i>即，</i>&nbsp;<tt>dfs.</tt>{@link
 *         java.text.DecimalFormatSymbols#getGroupingSeparator
 *         getGroupingSeparator()}
 *     <dt><i>本地小数点&nbsp;&nbsp;</i>
 *         <dd>用于小数点的字符，<i>即，</i>&nbsp;<tt>dfs.</tt>{@link
 *     java.text.DecimalFormatSymbols#getDecimalSeparator
 *     getDecimalSeparator()}
 *     <dt><i>本地正数前缀&nbsp;&nbsp;</i>
 *         <dd>出现在正数之前的字符串（可能是空的），<i>即，</i>&nbsp;<tt>df.</tt>{@link
 *         java.text.DecimalFormat#getPositivePrefix
 *         getPositivePrefix()}
 *     <dt><i>本地正数后缀&nbsp;&nbsp;</i>
 *         <dd>出现在正数之后的字符串（可能是空的），<i>即，</i>&nbsp;<tt>df.</tt>{@link
 *         java.text.DecimalFormat#getPositiveSuffix
 *         getPositiveSuffix()}
 *     <dt><i>本地负数前缀&nbsp;&nbsp;</i>
 *         <dd>出现在负数之前的字符串（可能是空的），<i>即，</i>&nbsp;<tt>df.</tt>{@link
 *         java.text.DecimalFormat#getNegativePrefix
 *         getNegativePrefix()}
 *     <dt><i>本地负数后缀&nbsp;&nbsp;</i>
 *         <dd>出现在负数之后的字符串（可能是空的），<i>即，</i>&nbsp;<tt>df.</tt>{@link
 *     java.text.DecimalFormat#getNegativeSuffix
 *     getNegativeSuffix()}
 *     <dt><i>本地NaN&nbsp;&nbsp;</i>
 *         <dd>表示浮点值的非数字的字符串，<i>即，</i>&nbsp;<tt>dfs.</tt>{@link
 *         java.text.DecimalFormatSymbols#getNaN
 *         getNaN()}
 *     <dt><i>本地无穷大&nbsp;&nbsp;</i>
 *         <dd>表示浮点值的无穷大的字符串，<i>即，</i>&nbsp;<tt>dfs.</tt>{@link
 *         java.text.DecimalFormatSymbols#getInfinity
 *         getInfinity()}
 * </dl></blockquote>
 *
 * <h4> <a name="number-syntax">数字语法</a> </h4>
 *
 * <p>此类实例可以解析为数字的字符串由以下正则表达式语法指定，其中 Rmax 是所用基数中的最高数字（例如，在十进制中 Rmax 是 9）。
 *
 * <dl>
 *   <dt><i>非ASCII数字</i>:
 *       <dd>非ASCII字符 c，对于该字符
 *            {@link java.lang.Character#isDigit Character.isDigit}<tt>(c)</tt>
 *                        返回&nbsp;true
 *
 *   <dt><i>非0数字</i>:
 *       <dd><tt>[1-</tt><i>Rmax</i><tt>] | </tt><i>非ASCII数字</i>
 *
 *   <dt><i>数字</i>:
 *       <dd><tt>[0-</tt><i>Rmax</i><tt>] | </tt><i>非ASCII数字</i>
 *
 *   <dt><i>分组数字</i>:
 *       <dd><tt>(&nbsp;</tt><i>非0数字</i>
 *                   <i>数字</i><tt>?
 *                   </tt><i>数字</i><tt>?
 *       <dd>&nbsp;&nbsp;&nbsp;&nbsp;<tt>(&nbsp;</tt><i>本地组分隔符</i>
 *                         <i>数字</i>
 *                         <i>数字</i>
 *                         <i>数字</i><tt> )+ )</tt>
 *
 *   <dt><i>数字</i>:
 *       <dd><tt>( ( </tt><i>数字</i><tt>+ )
 *               | </tt><i>分组数字</i><tt> )</tt>
 *
 *   <dt><a name="Integer-regex"><i>整数</i>:</a>
 *       <dd><tt>( [-+]? ( </tt><i>数字</i><tt>
 *                               ) )</tt>
 *       <dd><tt>| </tt><i>本地正数前缀</i> <i>数字</i>
 *                      <i>本地正数后缀</i>
 *       <dd><tt>| </tt><i>本地负数前缀</i> <i>数字</i>
 *                 <i>本地负数后缀</i>
 *
 *   <dt><i>十进制数字</i>:
 *       <dd><i>数字</i>
 *       <dd><tt>| </tt><i>数字</i>
 *                 <i>本地小数点</i>
 *                 <i>数字</i><tt>*</tt>
 *       <dd><tt>| </tt><i>本地小数点</i>
 *                 <i>数字</i><tt>+</tt>
 *
 *   <dt><i>指数</i>:
 *       <dd><tt>( [eE] [+-]? </tt><i>数字</i><tt>+ )</tt>
 *
 *   <dt><a name="Decimal-regex"><i>十进制数</i>:</a>
 *       <dd><tt>( [-+]? </tt><i>十进制数字</i>
 *                         <i>指数</i><tt>? )</tt>
 *       <dd><tt>| </tt><i>本地正数前缀</i>
 *                 <i>十进制数字</i>
 *                 <i>本地正数后缀</i>
 *                 <i>指数</i><tt>?</tt>
 *       <dd><tt>| </tt><i>本地负数前缀</i>
 *                 <i>十进制数字</i>
 *                 <i>本地负数后缀</i>
 *                 <i>指数</i><tt>?</tt>
 *
 *   <dt><i>十六进制浮点数</i>:
 *       <dd><tt>[-+]? 0[xX][0-9a-fA-F]*\.[0-9a-fA-F]+
 *                 ([pP][-+]?[0-9]+)?</tt>
 *
 *   <dt><i>非数字</i>:
 *       <dd><tt>NaN
 *                          | </tt><i>本地NaN</i><tt>
 *                          | Infinity
 *                          | </tt><i>本地无穷大</i>
 *
 *   <dt><i>带符号的非数字</i>:
 *       <dd><tt>( [-+]? </tt><i>非数字</i><tt> )</tt>
 *       <dd><tt>| </tt><i>本地正数前缀</i>
 *                 <i>非数字</i>
 *                 <i>本地正数后缀</i>
 *       <dd><tt>| </tt><i>本地负数前缀</i>
 *                 <i>非数字</i>
 *                 <i>本地负数后缀</i>
 *
 *   <dt><a name="Float-regex"><i>浮点数</i></a>:
 *       <dd><i>十进制数</i>
 *           <tt>| </tt><i>十六进制浮点数</i>
 *           <tt>| </tt><i>带符号的非数字</i>
 *
 * </dl>
 * <p>上述正则表达式中的空白字符不重要。
 *
 * @since   1.5
 */
public final class Scanner implements Iterator<String>, Closeable {

                // 内部缓冲区，用于保存输入
    private CharBuffer buf;

    // 内部字符缓冲区的大小
    private static final int BUFFER_SIZE = 1024; // 更改为 1024;

    // 当前由 Scanner 持有的缓冲区索引
    private int position;

    // 用于查找分隔符的内部匹配器
    private Matcher matcher;

    // 用于分隔标记的模式
    private Pattern delimPattern;

    // 在上次 hasNext 操作中找到的模式
    private Pattern hasNextPattern;

    // 上次 hasNext 操作后的索引位置
    private int hasNextPosition;

    // 上次 hasNext 操作的结果
    private String hasNextResult;

    // 输入源
    private Readable source;

    // 如果源已完成，布尔值为 true
    private boolean sourceClosed = false;

    // 布尔值，表示需要更多输入
    private boolean needInput = false;

    // 布尔值，表示当前操作是否跳过了分隔符
    private boolean skipped = false;

    // 一个存储位置，Scanner 可能会回退到该位置
    private int savedScannerPosition = -1;

    // 最近扫描的原始类型缓存
    private Object typeCache = null;

    // 布尔值，表示是否有一个匹配结果可用
    private boolean matchValid = false;

    // 布尔值，表示此 Scanner 是否已关闭
    private boolean closed = false;

    // 当前由此 Scanner 使用的基数
    private int radix = 10;

    // 此 Scanner 的默认基数
    private int defaultRadix = 10;

    // 此 Scanner 使用的区域设置
    private Locale locale = null;

    // 最近使用过的模式的缓存
    private LRUCache<String,Pattern> patternCache =
    new LRUCache<String,Pattern>(7) {
        protected Pattern create(String s) {
            return Pattern.compile(s);
        }
        protected boolean hasName(Pattern p, String s) {
            return p.pattern().equals(s);
        }
    };

    // 最近遇到的最后一个 IOException
    private IOException lastException;

    // Java 空白模式
    private static Pattern WHITESPACE_PATTERN = Pattern.compile(
                                                "\\p{javaWhitespace}+");

    // 任何标记的模式
    private static Pattern FIND_ANY_PATTERN = Pattern.compile("(?s).*");

    // 非 ASCII 数字的模式
    private static Pattern NON_ASCII_DIGIT = Pattern.compile(
        "[\\p{javaDigit}&&[^0-9]]");

    // 支持扫描原始类型字段和方法

    /**
     * 用于扫描数字的区域设置依赖值
     */
    private String groupSeparator = "\\,";
    private String decimalSeparator = "\\.";
    private String nanString = "NaN";
    private String infinityString = "Infinity";
    private String positivePrefix = "";
    private String negativePrefix = "\\-";
    private String positiveSuffix = "";
    private String negativeSuffix = "";

    /**
     * 匹配布尔值的字段和访问器方法
     */
    private static volatile Pattern boolPattern;
    private static final String BOOLEAN_PATTERN = "true|false";
    private static Pattern boolPattern() {
        Pattern bp = boolPattern;
        if (bp == null)
            boolPattern = bp = Pattern.compile(BOOLEAN_PATTERN,
                                          Pattern.CASE_INSENSITIVE);
        return bp;
    }

    /**
     * 匹配字节、短整型、整型和长整型的字段和方法
     */
    private Pattern integerPattern;
    private String digits = "0123456789abcdefghijklmnopqrstuvwxyz";
    private String non0Digit = "[\\p{javaDigit}&&[^0]]";
    private int SIMPLE_GROUP_INDEX = 5;
    private String buildIntegerPatternString() {
        String radixDigits = digits.substring(0, radix);
        // \\p{javaDigit} 在这里可能不完全合适，但能做什么呢？最终的权威将是调用的解析方法，因此 Scanner 最终会做正确的事情
        String digit = "((?i)["+radixDigits+"\\p{javaDigit}])";
        String groupedNumeral = "("+non0Digit+digit+"?"+digit+"?("+
                                groupSeparator+digit+digit+digit+")+)";
        // digit++ 是占有形式，对于减少可能导致不可接受性能的回溯是必要的
        String numeral = "(("+ digit+"++)|"+groupedNumeral+")";
        String javaStyleInteger = "([-+]?(" + numeral + "))";
        String negativeInteger = negativePrefix + numeral + negativeSuffix;
        String positiveInteger = positivePrefix + numeral + positiveSuffix;
        return "("+ javaStyleInteger + ")|(" +
            positiveInteger + ")|(" +
            negativeInteger + ")";
    }
    private Pattern integerPattern() {
        if (integerPattern == null) {
            integerPattern = patternCache.forName(buildIntegerPatternString());
        }
        return integerPattern;
    }

    /**
     * 匹配行分隔符的字段和访问器方法
     */
    private static volatile Pattern separatorPattern;
    private static volatile Pattern linePattern;
    private static final String LINE_SEPARATOR_PATTERN =
                                           "\r\n|[\n\r\u2028\u2029\u0085]";
    private static final String LINE_PATTERN = ".*("+LINE_SEPARATOR_PATTERN+")|.+$";

    private static Pattern separatorPattern() {
        Pattern sp = separatorPattern;
        if (sp == null)
            separatorPattern = sp = Pattern.compile(LINE_SEPARATOR_PATTERN);
        return sp;
    }

    private static Pattern linePattern() {
        Pattern lp = linePattern;
        if (lp == null)
            linePattern = lp = Pattern.compile(LINE_PATTERN);
        return lp;
    }

    /**
     * 匹配浮点数和双精度数的字段和方法
     */
    private Pattern floatPattern;
    private Pattern decimalPattern;
    private void buildFloatAndDecimalPattern() {
        // \\p{javaDigit} 可能不完美，见上文
        String digit = "(([0-9\\p{javaDigit}]))";
        String exponent = "([eE][+-]?"+digit+"+)?";
        String groupedNumeral = "("+non0Digit+digit+"?"+digit+"?("+
                                groupSeparator+digit+digit+digit+")+)";
        // 再次使用 digit++ 以提高性能，如上文所述
        String numeral = "(("+digit+"++)|"+groupedNumeral+")";
        String decimalNumeral = "("+numeral+"|"+numeral +
            decimalSeparator + digit + "*+|"+ decimalSeparator +
            digit + "++)";
        String nonNumber = "(NaN|"+nanString+"|Infinity|"+
                               infinityString+")";
        String positiveFloat = "(" + positivePrefix + decimalNumeral +
                            positiveSuffix + exponent + ")";
        String negativeFloat = "(" + negativePrefix + decimalNumeral +
                            negativeSuffix + exponent + ")";
        String decimal = "(([-+]?" + decimalNumeral + exponent + ")|"+
            positiveFloat + "|" + negativeFloat + ")";
        String hexFloat =
            "[-+]?0[xX][0-9a-fA-F]*\\.[0-9a-fA-F]+([pP][-+]?[0-9]+)?";
        String positiveNonNumber = "(" + positivePrefix + nonNumber +
                            positiveSuffix + ")";
        String negativeNonNumber = "(" + negativePrefix + nonNumber +
                            negativeSuffix + ")";
        String signedNonNumber = "(([-+]?"+nonNumber+")|" +
                                 positiveNonNumber + "|" +
                                 negativeNonNumber + ")";
        floatPattern = Pattern.compile(decimal + "|" + hexFloat + "|" +
                                       signedNonNumber);
        decimalPattern = Pattern.compile(decimal);
    }
    private Pattern floatPattern() {
        if (floatPattern == null) {
            buildFloatAndDecimalPattern();
        }
        return floatPattern;
    }
    private Pattern decimalPattern() {
        if (decimalPattern == null) {
            buildFloatAndDecimalPattern();
        }
        return decimalPattern;
    }

                // 构造函数

    /**
     * 构造一个从指定源中返回值的 <code>Scanner</code>，这些值由指定的模式分隔。
     *
     * @param source 实现 Readable 接口的字符源
     * @param pattern 分隔模式
     */
    private Scanner(Readable source, Pattern pattern) {
        assert source != null : "source should not be null";
        assert pattern != null : "pattern should not be null";
        this.source = source;
        delimPattern = pattern;
        buf = CharBuffer.allocate(BUFFER_SIZE);
        buf.limit(0);
        matcher = delimPattern.matcher(buf);
        matcher.useTransparentBounds(true);
        matcher.useAnchoringBounds(false);
        useLocale(Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 构造一个新的 <code>Scanner</code>，该对象从指定的源中生成值。
     *
     * @param  source 实现 {@link Readable} 接口的字符源
     */
    public Scanner(Readable source) {
        this(Objects.requireNonNull(source, "source"), WHITESPACE_PATTERN);
    }

    /**
     * 构造一个新的 <code>Scanner</code>，该对象从指定的输入流中生成值。流中的字节使用底层平台的
     * {@linkplain java.nio.charset.Charset#defaultCharset() 默认字符集} 转换为字符。
     *
     * @param  source 要扫描的输入流
     */
    public Scanner(InputStream source) {
        this(new InputStreamReader(source), WHITESPACE_PATTERN);
    }

    /**
     * 构造一个新的 <code>Scanner</code>，该对象从指定的输入流中生成值。流中的字节使用指定的字符集转换为字符。
     *
     * @param  source 要扫描的输入流
     * @param charsetName 用于将流中的字节转换为字符的编码类型
     * @throws IllegalArgumentException 如果指定的字符集不存在
     */
    public Scanner(InputStream source, String charsetName) {
        this(makeReadable(Objects.requireNonNull(source, "source"), toCharset(charsetName)),
             WHITESPACE_PATTERN);
    }

    /**
     * 返回给定字符集名称的字符集对象。
     * @throws NullPointerException 如果 csn 为 null
     * @throws IllegalArgumentException 如果字符集不支持
     */
    private static Charset toCharset(String csn) {
        Objects.requireNonNull(csn, "charsetName");
        try {
            return Charset.forName(csn);
        } catch (IllegalCharsetNameException|UnsupportedCharsetException e) {
            // 应该抛出 IllegalArgumentException
            throw new IllegalArgumentException(e);
        }
    }

    private static Readable makeReadable(InputStream source, Charset charset) {
        return new InputStreamReader(source, charset);
    }

    /**
     * 构造一个新的 <code>Scanner</code>，该对象从指定的文件中生成值。文件中的字节使用底层平台的
     * {@linkplain java.nio.charset.Charset#defaultCharset() 默认字符集} 转换为字符。
     *
     * @param  source 要扫描的文件
     * @throws FileNotFoundException 如果找不到源文件
     */
    public Scanner(File source) throws FileNotFoundException {
        this((ReadableByteChannel)(new FileInputStream(source).getChannel()));
    }

    /**
     * 构造一个新的 <code>Scanner</code>，该对象从指定的文件中生成值。文件中的字节使用指定的字符集转换为字符。
     *
     * @param  source 要扫描的文件
     * @param charsetName 用于将文件中的字节转换为字符的编码类型
     * @throws FileNotFoundException 如果找不到源文件
     * @throws IllegalArgumentException 如果指定的编码不存在
     */
    public Scanner(File source, String charsetName)
        throws FileNotFoundException
    {
        this(Objects.requireNonNull(source), toDecoder(charsetName));
    }

    private Scanner(File source, CharsetDecoder dec)
        throws FileNotFoundException
    {
        this(makeReadable((ReadableByteChannel)(new FileInputStream(source).getChannel()), dec));
    }

    private static CharsetDecoder toDecoder(String charsetName) {
        Objects.requireNonNull(charsetName, "charsetName");
        try {
            return Charset.forName(charsetName).newDecoder();
        } catch (IllegalCharsetNameException|UnsupportedCharsetException unused) {
            throw new IllegalArgumentException(charsetName);
        }
    }

    private static Readable makeReadable(ReadableByteChannel source,
                                         CharsetDecoder dec) {
        return Channels.newReader(source, dec, -1);
    }

    /**
     * 构造一个新的 <code>Scanner</code>，该对象从指定的文件中生成值。文件中的字节使用底层平台的
     * {@linkplain java.nio.charset.Charset#defaultCharset() 默认字符集} 转换为字符。
     *
     * @param   source
     *          要扫描的文件的路径
     * @throws  IOException
     *          如果打开源时发生 I/O 错误
     *
     * @since   1.7
     */
    public Scanner(Path source)
        throws IOException
    {
        this(Files.newInputStream(source));
    }

    /**
     * 构造一个新的 <code>Scanner</code>，该对象从指定的文件中生成值。文件中的字节使用指定的字符集转换为字符。
     *
     * @param   source
     *          要扫描的文件的路径
     * @param   charsetName
     *          用于将文件中的字节转换为字符的编码类型
     * @throws  IOException
     *          如果打开源时发生 I/O 错误
     * @throws  IllegalArgumentException
     *          如果指定的编码不存在
     * @since   1.7
     */
    public Scanner(Path source, String charsetName) throws IOException {
        this(Objects.requireNonNull(source), toCharset(charsetName));
    }


                private Scanner(Path source, Charset charset)  throws IOException {
        this(makeReadable(Files.newInputStream(source), charset));
    }

    /**
     * 构造一个新的 <code>Scanner</code>，该对象从指定的字符串中生成值。
     *
     * @param  source 要扫描的字符串
     */
    public Scanner(String source) {
        this(new StringReader(source), WHITESPACE_PATTERN);
    }

    /**
     * 构造一个新的 <code>Scanner</code>，该对象从指定的通道中生成值。从源中读取的字节使用底层平台的
     * {@linkplain java.nio.charset.Charset#defaultCharset() 默认字符集} 转换为字符。
     *
     * @param  source 要扫描的通道
     */
    public Scanner(ReadableByteChannel source) {
        this(makeReadable(Objects.requireNonNull(source, "source")),
             WHITESPACE_PATTERN);
    }

    private static Readable makeReadable(ReadableByteChannel source) {
        return makeReadable(source, Charset.defaultCharset().newDecoder());
    }

    /**
     * 构造一个新的 <code>Scanner</code>，该对象从指定的通道中生成值。从源中读取的字节使用指定的字符集转换为字符。
     *
     * @param  source 要扫描的通道
     * @param charsetName 用于将通道中的字节转换为字符的编码类型
     * @throws IllegalArgumentException 如果指定的字符集不存在
     */
    public Scanner(ReadableByteChannel source, String charsetName) {
        this(makeReadable(Objects.requireNonNull(source, "source"), toDecoder(charsetName)),
             WHITESPACE_PATTERN);
    }

    // 用于支持扫描的私有原语

    private void saveState() {
        savedScannerPosition = position;
    }

    private void revertState() {
        this.position = savedScannerPosition;
        savedScannerPosition = -1;
        skipped = false;
    }

    private boolean revertState(boolean b) {
        this.position = savedScannerPosition;
        savedScannerPosition = -1;
        skipped = false;
        return b;
    }

    private void cacheResult() {
        hasNextResult = matcher.group();
        hasNextPosition = matcher.end();
        hasNextPattern = matcher.pattern();
    }

    private void cacheResult(String result) {
        hasNextResult = result;
        hasNextPosition = matcher.end();
        hasNextPattern = matcher.pattern();
    }

    // 清除常规缓存和类型缓存
    private void clearCaches() {
        hasNextPattern = null;
        typeCache = null;
    }

    // 同时清除常规缓存和类型缓存
    private String getCachedResult() {
        position = hasNextPosition;
        hasNextPattern = null;
        typeCache = null;
        return hasNextResult;
    }

    // 同时清除常规缓存和类型缓存
    private void useTypeCache() {
        if (closed)
            throw new IllegalStateException("Scanner closed");
        position = hasNextPosition;
        hasNextPattern = null;
        typeCache = null;
    }

    // 尝试读取更多输入。可能会阻塞。
    private void readInput() {
        if (buf.limit() == buf.capacity())
            makeSpace();

        // 准备接收数据
        int p = buf.position();
        buf.position(buf.limit());
        buf.limit(buf.capacity());

        int n = 0;
        try {
            n = source.read(buf);
        } catch (IOException ioe) {
            lastException = ioe;
            n = -1;
        }

        if (n == -1) {
            sourceClosed = true;
            needInput = false;
        }

        if (n > 0)
            needInput = false;

        // 恢复当前读取的位置和限制
        buf.limit(buf.position());
        buf.position(p);
    }

    // 调用此方法后，缓冲区中将有异常或空间
    private boolean makeSpace() {
        clearCaches();
        int offset = savedScannerPosition == -1 ?
            position : savedScannerPosition;
        buf.position(offset);
        // 通过压缩缓冲区来获得空间
        if (offset > 0) {
            buf.compact();
            translateSavedIndexes(offset);
            position -= offset;
            buf.flip();
            return true;
        }
        // 通过扩展缓冲区来获得空间
        int newSize = buf.capacity() * 2;
        CharBuffer newBuf = CharBuffer.allocate(newSize);
        newBuf.put(buf);
        newBuf.flip();
        translateSavedIndexes(offset);
        position -= offset;
        buf = newBuf;
        matcher.reset(buf);
        return true;
    }

    // 当缓冲区压缩/重新分配时，保存的索引必须相应地修改
    private void translateSavedIndexes(int offset) {
        if (savedScannerPosition != -1)
            savedScannerPosition -= offset;
    }

    // 如果我们处于输入的末尾，则抛出NoSuchElement；
    // 如果仍有输入，则抛出InputMismatch
    private void throwFor() {
        skipped = false;
        if ((sourceClosed) && (position == buf.limit()))
            throw new NoSuchElementException();
        else
            throw new InputMismatchException();
    }

    // 如果缓冲区中有一个完整的令牌或部分令牌，则返回true。
    // 不需要找到一个完整的令牌，因为部分令牌意味着在有或没有更多输入的情况下将会有另一个令牌。
    private boolean hasTokenInBuffer() {
        matchValid = false;
        matcher.usePattern(delimPattern);
        matcher.region(position, buf.limit());

        // 首先跳过分隔符
        if (matcher.lookingAt())
            position = matcher.end();

        // 如果我们处于末尾，则缓冲区中没有更多令牌
        if (position == buf.limit())
            return false;

        return true;
    }

    /*
     * 返回与指定模式匹配的“完整令牌”
     *
     * 如果令牌被分隔符包围，则它是完整的；部分令牌是指前缀为分隔符但后缀不是分隔符的令牌
     *
     * 位置将被推进到该完整令牌的末尾
     *
     * 模式 == null 表示接受任何令牌
     *
     * 三重返回：
     * 1. 有效的字符串表示已找到
     * 2. null 且 needInput=false 表示我们永远不会找到它
     * 3. null 且 needInput=true 表示在 readInput 后再次尝试
     */
    private String getCompleteTokenInBuffer(Pattern pattern) {
        matchValid = false;


                    // 跳过分隔符
        matcher.usePattern(delimPattern);
        if (!skipped) { // 仅允许跳过一次前导分隔符
            matcher.region(position, buf.limit());
            if (matcher.lookingAt()) {
                // 如果更多输入可以扩展分隔符，则必须等待更多输入
                if (matcher.hitEnd() && !sourceClosed) {
                    needInput = true;
                    return null;
                }
                // 分隔符是完整的，匹配器应该跳过它们
                skipped = true;
                position = matcher.end();
            }
        }

        // 如果已经到达末尾，缓冲区中没有更多标记
        if (position == buf.limit()) {
            if (sourceClosed)
                return null;
            needInput = true;
            return null;
        }

        // 必须查找下一个分隔符。在此处尝试匹配模式可能会找到匹配项，但可能不是第一个最长匹配项，因为缺少输入，或者可能匹配部分标记而不是整个标记。

        // 然后查找下一个分隔符
        matcher.region(position, buf.limit());
        boolean foundNextDelim = matcher.find();
        if (foundNextDelim && (matcher.end() == position)) {
            // 零长度分隔符匹配；我们应该使用自动前进过去零长度匹配来找到下一个；
            // 否则我们刚刚找到了我们刚刚跳过的同一个
            foundNextDelim = matcher.find();
        }
        if (foundNextDelim) {
            // 在极少数情况下，更多的输入可能会导致匹配丢失并且有更多输入到来时，我们必须等待更多输入。请注意，到达末尾是可以的，只要匹配不会消失即可。我们希望确保下一个分隔符的开始，我们不在乎它们是否可能进一步扩展。
            if (matcher.requireEnd() && !sourceClosed) {
                needInput = true;
                return null;
            }
            int tokenEnd = matcher.start();
            // 有一个完整的标记。
            if (pattern == null) {
                // 必须继续匹配以提供有效的 MatchResult
                pattern = FIND_ANY_PATTERN;
            }
            // 尝试匹配所需的模式
            matcher.usePattern(pattern);
            matcher.region(position, tokenEnd);
            if (matcher.matches()) {
                String s = matcher.group();
                position = matcher.end();
                return s;
            } else { // 完整的标记但不匹配
                return null;
            }
        }

        // 如果找不到下一个分隔符但没有更多输入到来，
        // 那么我们可以将剩余部分视为一个完整的标记
        if (sourceClosed) {
            if (pattern == null) {
                // 必须继续匹配以提供有效的 MatchResult
                pattern = FIND_ANY_PATTERN;
            }
            // 最后一个标记；在此处匹配模式或抛出异常
            matcher.usePattern(pattern);
            matcher.region(position, buf.limit());
            if (matcher.matches()) {
                String s = matcher.group();
                position = matcher.end();
                return s;
            }
            // 最后一部分不匹配
            return null;
        }

        // 缓冲区中有一个部分标记；必须读取更多以完成它
        needInput = true;
        return null;
    }

    // 在缓冲区中查找指定模式，直到地平线。
    // 返回指定输入模式的匹配项。
    private String findPatternInBuffer(Pattern pattern, int horizon) {
        matchValid = false;
        matcher.usePattern(pattern);
        int bufferLimit = buf.limit();
        int horizonLimit = -1;
        int searchLimit = bufferLimit;
        if (horizon > 0) {
            horizonLimit = position + horizon;
            if (horizonLimit < bufferLimit)
                searchLimit = horizonLimit;
        }
        matcher.region(position, searchLimit);
        if (matcher.find()) {
            if (matcher.hitEnd() && (!sourceClosed)) {
                // 如果没有到达地平线或实际末尾，匹配可能更长
                if (searchLimit != horizonLimit) {
                     // 达到人为的末尾；尝试扩展匹配
                    needInput = true;
                    return null;
                }
                // 匹配可能因接下来的内容而消失
                if ((searchLimit == horizonLimit) && matcher.requireEnd()) {
                    // 极少数情况：我们到达了输入的末尾，碰巧在地平线处，而输入的末尾是匹配所必需的。
                    needInput = true;
                    return null;
                }
            }
            // 没有到达末尾，或者到达了实际末尾，或者到达了地平线
            position = matcher.end();
            return matcher.group();
        }

        if (sourceClosed)
            return null;

        // 如果没有指定地平线，或者还没有搜索到指定的地平线，获取更多输入
        if ((horizon == 0) || (searchLimit != horizonLimit))
            needInput = true;
        return null;
    }

    // 返回从当前位置锚定的指定输入模式的匹配项
    private String matchPatternInBuffer(Pattern pattern) {
        matchValid = false;
        matcher.usePattern(pattern);
        matcher.region(position, buf.limit());
        if (matcher.lookingAt()) {
            if (matcher.hitEnd() && (!sourceClosed)) {
                // 获取更多输入并再次尝试
                needInput = true;
                return null;
            }
            position = matcher.end();
            return matcher.group();
        }

        if (sourceClosed)
            return null;


                    // 读取更多以找到模式
        needInput = true;
        return null;
    }

    // 如果扫描器已关闭，则抛出异常
    private void ensureOpen() {
        if (closed)
            throw new IllegalStateException("Scanner closed");
    }

    // 公有方法

    /**
     * 关闭此扫描器。
     *
     * <p>如果此扫描器尚未关闭，则如果其底层
     * {@linkplain java.lang.Readable 可读对象}也实现了 {@link
     * java.io.Closeable} 接口，则将调用可读对象的 <tt>close</tt> 方法。
     * 如果此扫描器已关闭，则调用此方法将不会产生任何效果。
     *
     * <p>在扫描器关闭后尝试执行搜索操作将导致 {@link IllegalStateException}。
     *
     */
    public void close() {
        if (closed)
            return;
        if (source instanceof Closeable) {
            try {
                ((Closeable)source).close();
            } catch (IOException ioe) {
                lastException = ioe;
            }
        }
        sourceClosed = true;
        source = null;
        closed = true;
    }

    /**
     * 返回此 <code>Scanner</code> 的底层 <code>Readable</code> 最后抛出的 <code>IOException</code>。
     * 如果没有此类异常，则此方法返回 <code>null</code>。
     *
     * @return 此扫描器的可读对象最后抛出的异常
     */
    public IOException ioException() {
        return lastException;
    }

    /**
     * 返回此 <code>Scanner</code> 当前用于匹配分隔符的 <code>Pattern</code>。
     *
     * @return 此扫描器的分隔符模式。
     */
    public Pattern delimiter() {
        return delimPattern;
    }

    /**
     * 将此扫描器的分隔符模式设置为指定的模式。
     *
     * @param pattern 分隔符模式
     * @return 此扫描器
     */
    public Scanner useDelimiter(Pattern pattern) {
        delimPattern = pattern;
        return this;
    }

    /**
     * 将此扫描器的分隔符模式设置为从指定的 <code>String</code> 构建的模式。
     *
     * <p>此方法的调用形式 <tt>useDelimiter(pattern)</tt> 的行为与
     * 调用 <tt>useDelimiter(Pattern.compile(pattern))</tt> 完全相同。
     *
     * <p>调用 {@link #reset} 方法将把扫描器的分隔符设置为
     * <a href= "#default-delimiter">默认值</a>。
     *
     * @param pattern 指定分隔符模式的字符串
     * @return 此扫描器
     */
    public Scanner useDelimiter(String pattern) {
        delimPattern = patternCache.forName(pattern);
        return this;
    }

    /**
     * 返回此扫描器的区域设置。
     *
     * <p>扫描器的区域设置会影响其默认原始匹配正则表达式的许多元素；参见
     * <a href= "#localized-numbers">本地化数字</a>。
     *
     * @return 此扫描器的区域设置
     */
    public Locale locale() {
        return this.locale;
    }

    /**
     * 将此扫描器的区域设置设置为指定的区域设置。
     *
     * <p>扫描器的区域设置会影响其默认原始匹配正则表达式的许多元素；参见
     * <a href= "#localized-numbers">本地化数字</a>。
     *
     * <p>调用 {@link #reset} 方法将把扫描器的区域设置设置为
     * <a href= "#initial-locale">初始区域设置</a>。
     *
     * @param locale 指定要使用的区域设置的字符串
     * @return 此扫描器
     */
    public Scanner useLocale(Locale locale) {
        if (locale.equals(this.locale))
            return this;

        this.locale = locale;
        DecimalFormat df =
            (DecimalFormat)NumberFormat.getNumberInstance(locale);
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(locale);

        // 必须进行字面化以避免与正则表达式的元字符（如点或括号）冲突
        groupSeparator =   "\\x{" + Integer.toHexString(dfs.getGroupingSeparator()) + "}";
        decimalSeparator = "\\x{" + Integer.toHexString(dfs.getDecimalSeparator()) + "}";

        // 对非零长度的本地化特定内容进行引用，以避免与元字符冲突
        nanString = Pattern.quote(dfs.getNaN());
        infinityString = Pattern.quote(dfs.getInfinity());
        positivePrefix = df.getPositivePrefix();
        if (!positivePrefix.isEmpty())
            positivePrefix = Pattern.quote(positivePrefix);
        negativePrefix = df.getNegativePrefix();
        if (!negativePrefix.isEmpty())
            negativePrefix = Pattern.quote(negativePrefix);
        positiveSuffix = df.getPositiveSuffix();
        if (!positiveSuffix.isEmpty())
            positiveSuffix = Pattern.quote(positiveSuffix);
        negativeSuffix = df.getNegativeSuffix();
        if (!negativeSuffix.isEmpty())
            negativeSuffix = Pattern.quote(negativeSuffix);

        // 强制重新构建和编译依赖于区域设置的原始模式
        integerPattern = null;
        floatPattern = null;

        return this;
    }

    /**
     * 返回此扫描器的默认基数。
     *
     * <p>扫描器的基数会影响其默认数字匹配正则表达式的许多元素；参见
     * <a href= "#localized-numbers">本地化数字</a>。
     *
     * @return 此扫描器的默认基数
     */
    public int radix() {
        return this.defaultRadix;
    }

    /**
     * 将此扫描器的默认基数设置为指定的基数。
     *
     * <p>扫描器的基数会影响其默认数字匹配正则表达式的许多元素；参见
     * <a href= "#localized-numbers">本地化数字</a>。
     *
     * <p>如果基数小于 <code>Character.MIN_RADIX</code>
     * 或大于 <code>Character.MAX_RADIX</code>，则抛出
     * <code>IllegalArgumentException</code>。
     *
     * <p>调用 {@link #reset} 方法将把扫描器的基数设置为
     * <code>10</code>。
     *
     * @param radix 扫描数字时使用的基数
     * @return 此扫描器
     * @throws IllegalArgumentException 如果基数超出范围
     */
    public Scanner useRadix(int radix) {
        if ((radix < Character.MIN_RADIX) || (radix > Character.MAX_RADIX))
            throw new IllegalArgumentException("radix:"+radix);


                    if (this.defaultRadix == radix)
            return this;
        this.defaultRadix = radix;
        // 强制重建和重新编译与基数相关的模式
        integerPattern = null;
        return this;
    }

    // 下一个操作应在指定的基数中进行，但默认基数保持不变。
    private void setRadix(int radix) {
        if (this.radix != radix) {
            // 强制重建和重新编译与基数相关的模式
            integerPattern = null;
            this.radix = radix;
        }
    }

    /**
     * 返回此扫描器执行的最后一个扫描操作的匹配结果。如果未执行匹配，或者最后一个匹配不成功，
     * 则此方法将抛出<code>IllegalStateException</code>。
     *
     * <p><code>Scanner</code>的各种<code>next</code>方法如果完成而没有抛出异常，则会提供匹配结果。
     * 例如，调用返回int的{@link #nextInt}方法后，此方法返回一个
     * <a href="#Integer-regex"><i>Integer</i></a>正则表达式的匹配结果。
     * 同样，如果{@link #findInLine}、{@link #findWithinHorizon}和{@link #skip}方法成功，
     * 也会提供匹配结果。
     *
     * @return 最后一次匹配操作的匹配结果
     * @throws IllegalStateException 如果没有可用的匹配结果
     */
    public MatchResult match() {
        if (!matchValid)
            throw new IllegalStateException("No match result available");
        return matcher.toMatchResult();
    }

    /**
     * <p>返回此<code>Scanner</code>的字符串表示形式。此<code>Scanner</code>的字符串表示形式包含
     * 可能对调试有用的调试信息。确切的格式未指定。
     *
     * @return 此扫描器的字符串表示形式
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("java.util.Scanner");
        sb.append("[delimiters=" + delimPattern + "]");
        sb.append("[position=" + position + "]");
        sb.append("[match valid=" + matchValid + "]");
        sb.append("[need input=" + needInput + "]");
        sb.append("[source closed=" + sourceClosed + "]");
        sb.append("[skipped=" + skipped + "]");
        sb.append("[group separator=" + groupSeparator + "]");
        sb.append("[decimal separator=" + decimalSeparator + "]");
        sb.append("[positive prefix=" + positivePrefix + "]");
        sb.append("[negative prefix=" + negativePrefix + "]");
        sb.append("[positive suffix=" + positiveSuffix + "]");
        sb.append("[negative suffix=" + negativeSuffix + "]");
        sb.append("[NaN string=" + nanString + "]");
        sb.append("[infinity string=" + infinityString + "]");
        return sb.toString();
    }

    /**
     * 如果此扫描器的输入中还有另一个标记，则返回true。此方法可能在等待输入时阻塞。
     * 扫描器不会前进到任何输入。
     *
     * @return 如果且仅如果此扫描器还有另一个标记，则返回true
     * @throws IllegalStateException 如果此扫描器已关闭
     * @see java.util.Iterator
     */
    public boolean hasNext() {
        ensureOpen();
        saveState();
        while (!sourceClosed) {
            if (hasTokenInBuffer())
                return revertState(true);
            readInput();
        }
        boolean result = hasTokenInBuffer();
        return revertState(result);
    }

    /**
     * 查找并返回此扫描器的下一个完整标记。完整标记由与分隔符模式匹配的输入前后包围。
     * 此方法可能在等待输入时阻塞，即使之前调用{@link #hasNext}返回了<code>true</code>。
     *
     * @return 下一个标记
     * @throws NoSuchElementException 如果没有更多可用的标记
     * @throws IllegalStateException 如果此扫描器已关闭
     * @see java.util.Iterator
     */
    public String next() {
        ensureOpen();
        clearCaches();

        while (true) {
            String token = getCompleteTokenInBuffer(null);
            if (token != null) {
                matchValid = true;
                skipped = false;
                return token;
            }
            if (needInput)
                readInput();
            else
                throwFor();
        }
    }

    /**
     * 此<code>Iterator</code>实现不支持删除操作。
     *
     * @throws UnsupportedOperationException 如果调用此方法
     * @see java.util.Iterator
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * 如果下一个标记与从指定字符串构造的模式匹配，则返回true。扫描器不会前进到任何输入。
     *
     * <p>此方法的调用形式<tt>hasNext(pattern)</tt>的行为与调用
     * <tt>hasNext(Pattern.compile(pattern))</tt>完全相同。
     *
     * @param pattern 指定要扫描的模式的字符串
     * @return 如果且仅如果此扫描器还有另一个与指定模式匹配的标记，则返回true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNext(String pattern)  {
        return hasNext(patternCache.forName(pattern));
    }

    /**
     * 如果匹配成功，则返回与从指定字符串构造的模式匹配的下一个标记。扫描器将前进到匹配输入之后。
     *
     * <p>此方法的调用形式<tt>next(pattern)</tt>的行为与调用
     * <tt>next(Pattern.compile(pattern))</tt>完全相同。
     *
     * @param pattern 指定要扫描的模式的字符串
     * @return 下一个标记
     * @throws NoSuchElementException 如果没有这样的标记可用
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public String next(String pattern)  {
        return next(patternCache.forName(pattern));
    }


                /**
     * 如果下一个完整的标记与指定的模式匹配，则返回 true。
     * 完整的标记由与分隔符模式匹配的前缀和后缀包围。此方法可能会在等待输入时阻塞。
     * 扫描器不会跳过任何输入。
     *
     * @param pattern 要扫描的模式
     * @return 如果且仅当此扫描器有与指定模式匹配的下一个标记时返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNext(Pattern pattern) {
        ensureOpen();
        if (pattern == null)
            throw new NullPointerException();
        hasNextPattern = null;
        saveState();

        while (true) {
            if (getCompleteTokenInBuffer(pattern) != null) {
                matchValid = true;
                cacheResult();
                return revertState(true);
            }
            if (needInput)
                readInput();
            else
                return revertState(false);
        }
    }

    /**
     * 如果下一个标记与指定的模式匹配，则返回该标记。此方法可能会在等待输入以进行扫描时阻塞，即使
     * 之前对 {@link #hasNext(Pattern)} 的调用返回了 <code>true</code>。
     * 如果匹配成功，扫描器将跳过与模式匹配的输入。
     *
     * @param pattern 要扫描的模式
     * @return 下一个标记
     * @throws NoSuchElementException 如果没有更多可用的标记
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public String next(Pattern pattern) {
        ensureOpen();
        if (pattern == null)
            throw new NullPointerException();

        // 我们是否已经找到了这个模式？
        if (hasNextPattern == pattern)
            return getCachedResult();
        clearCaches();

        // 搜索模式
        while (true) {
            String token = getCompleteTokenInBuffer(pattern);
            if (token != null) {
                matchValid = true;
                skipped = false;
                return token;
            }
            if (needInput)
                readInput();
            else
                throwFor();
        }
    }

    /**
     * 如果此扫描器的输入中还有另一行，则返回 true。此方法可能会在等待输入时阻塞。扫描器不会跳过任何输入。
     *
     * @return 如果且仅当此扫描器还有另一行输入时返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNextLine() {
        saveState();

        String result = findWithinHorizon(linePattern(), 0);
        if (result != null) {
            MatchResult mr = this.match();
            String lineSep = mr.group(1);
            if (lineSep != null) {
                result = result.substring(0, result.length() -
                                          lineSep.length());
                cacheResult(result);

            } else {
                cacheResult();
            }
        }
        revertState();
        return (result != null);
    }

    /**
     * 跳过当前行并返回被跳过的输入。
     *
     * 此方法返回当前行的其余部分，不包括行尾的任何行分隔符。位置设置为下一行的开头。
     *
     * <p>由于此方法继续在输入中搜索行分隔符，因此如果不存在行分隔符，它可能会缓冲所有输入以查找要跳过的行。
     *
     * @return 被跳过的行
     * @throws NoSuchElementException 如果没有找到行
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public String nextLine() {
        if (hasNextPattern == linePattern())
            return getCachedResult();
        clearCaches();

        String result = findWithinHorizon(linePattern, 0);
        if (result == null)
            throw new NoSuchElementException("No line found");
        MatchResult mr = this.match();
        String lineSep = mr.group(1);
        if (lineSep != null)
            result = result.substring(0, result.length() - lineSep.length());
        if (result == null)
            throw new NoSuchElementException();
        else
            return result;
    }

    // 忽略分隔符的公共方法

    /**
     * 尝试找到从指定字符串构造的模式的下一个出现，忽略分隔符。
     *
     * <p>此方法的调用形式 <tt>findInLine(pattern)</tt> 的行为与调用
     * <tt>findInLine(Pattern.compile(pattern))</tt> 完全相同。
     *
     * @param pattern 指定要搜索的模式的字符串
     * @return 与指定模式匹配的文本
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public String findInLine(String pattern) {
        return findInLine(patternCache.forName(pattern));
    }

    /**
     * 尝试找到指定模式的下一个出现，忽略分隔符。如果在下一个行分隔符之前找到模式，扫描器将跳过匹配的输入并返回与模式匹配的字符串。
     * 如果在输入中直到下一个行分隔符之前没有检测到这样的模式，则返回 <code>null</code>，并且扫描器的位置不变。此方法可能会在等待匹配模式的输入时阻塞。
     *
     * <p>由于此方法继续在输入中搜索指定的模式，因此如果不存在行分隔符，它可能会缓冲所有输入以查找所需的标记。
     *
     * @param pattern 要扫描的模式
     * @return 与指定模式匹配的文本
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public String findInLine(Pattern pattern) {
        ensureOpen();
        if (pattern == null)
            throw new NullPointerException();
        clearCaches();
        // 扩展缓冲区以包括下一个换行符或输入结束
        int endPosition = 0;
        saveState();
        while (true) {
            String token = findPatternInBuffer(separatorPattern(), 0);
            if (token != null) {
                endPosition = matcher.start();
                break; // 到下一个换行符
            }
            if (needInput) {
                readInput();
            } else {
                endPosition = buf.limit();
                break; // 到输入结束
            }
        }
        revertState();
        int horizonForLine = endPosition - position;
        // 如果当前位置和下一个换行符之间没有内容，直接返回 null，调用 findWithinHorizon
        // 时 "horizon=0" 将扫描超出行边界。
        if (horizonForLine == 0)
            return null;
        // 搜索模式
        return findWithinHorizon(pattern, horizonForLine);
    }


                /**
     * 尝试找到由指定字符串构造的模式的下一个出现，忽略分隔符。
     *
     * <p>此方法的调用形式
     * <tt>findWithinHorizon(pattern)</tt> 的行为与
     * <tt>findWithinHorizon(Pattern.compile(pattern, horizon))</tt> 的调用完全相同。
     *
     * @param pattern 指定要搜索的模式的字符串
     * @param horizon 搜索范围
     * @return 与指定模式匹配的文本
     * @throws IllegalStateException 如果此扫描器已关闭
     * @throws IllegalArgumentException 如果范围为负数
     */
    public String findWithinHorizon(String pattern, int horizon) {
        return findWithinHorizon(patternCache.forName(pattern), horizon);
    }

    /**
     * 尝试找到指定模式的下一个出现。
     *
     * <p>此方法在输入中搜索到指定的搜索范围，忽略分隔符。如果找到模式，扫描器将跳过匹配的输入并返回匹配的字符串。如果未检测到这样的模式，则返回 null，且扫描器的位置保持不变。此方法可能会阻塞，等待匹配模式的输入。
     *
     * <p>扫描器搜索的范围不会超过其当前位置之后的 <code>horizon</code> 个代码点。注意，匹配结果可能会被范围截断；也就是说，如果范围更大，任意匹配结果可能会不同。扫描器将范围视为透明的、非锚定的边界（参见 {@link
     * Matcher#useTransparentBounds} 和 {@link Matcher#useAnchoringBounds}）。
     *
     * <p>如果范围为 <code>0</code>，则忽略范围，此方法将继续搜索输入，查找指定的模式，没有边界。在这种情况下，它可能会缓冲所有输入以查找模式。
     *
     * <p>如果范围为负数，则抛出 IllegalArgumentException。
     *
     * @param pattern 要扫描的模式
     * @param horizon 搜索范围
     * @return 与指定模式匹配的文本
     * @throws IllegalStateException 如果此扫描器已关闭
     * @throws IllegalArgumentException 如果范围为负数
     */
    public String findWithinHorizon(Pattern pattern, int horizon) {
        ensureOpen();
        if (pattern == null)
            throw new NullPointerException();
        if (horizon < 0)
            throw new IllegalArgumentException("horizon < 0");
        clearCaches();

        // 搜索模式
        while (true) {
            String token = findPatternInBuffer(pattern, horizon);
            if (token != null) {
                matchValid = true;
                return token;
            }
            if (needInput)
                readInput();
            else
                break; // 到输入结束
        }
        return null;
    }

    /**
     * 跳过与指定模式匹配的输入，忽略分隔符。
     * 如果从扫描器当前位置开始的指定模式的锚定匹配成功，则此方法将跳过输入。
     *
     * <p>如果在当前位置未找到与指定模式的匹配，则不跳过任何输入，并抛出
     * <tt>NoSuchElementException</tt>。
     *
     * <p>由于此方法从扫描器的当前位置开始尝试匹配指定的模式，可以匹配大量输入的模式（例如 ".*"）可能会导致扫描器缓冲大量输入。
     *
     * <p>注意，可以使用可以匹配空内容的模式（例如 <code>sc.skip("[ \t]*")</code>）来跳过内容，而不会冒 <code>NoSuchElementException</code> 的风险。
     *
     * @param pattern 指定要跳过的模式的字符串
     * @return 此扫描器
     * @throws NoSuchElementException 如果未找到指定的模式
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public Scanner skip(Pattern pattern) {
        ensureOpen();
        if (pattern == null)
            throw new NullPointerException();
        clearCaches();

        // 搜索模式
        while (true) {
            String token = matchPatternInBuffer(pattern);
            if (token != null) {
                matchValid = true;
                position = matcher.end();
                return this;
            }
            if (needInput)
                readInput();
            else
                throw new NoSuchElementException();
        }
    }

    /**
     * 跳过与从指定字符串构造的模式匹配的输入。
     *
     * <p>此方法的调用形式 <tt>skip(pattern)</tt> 的行为与
     * <tt>skip(Pattern.compile(pattern))</tt> 的调用完全相同。
     *
     * @param pattern 指定要跳过的模式的字符串
     * @return 此扫描器
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public Scanner skip(String pattern) {
        return skip(patternCache.forName(pattern));
    }

    // 用于扫描基本类型的便捷方法

    /**
     * 如果此扫描器的下一个标记可以使用不区分大小写的模式 "true|false" 解释为布尔值，则返回 true。扫描器不会跳过匹配的输入。
     *
     * @return 如果且仅如果此扫描器的下一个标记是有效的布尔值，则返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNextBoolean()  {
        return hasNext(boolPattern());
    }

    /**
     * 将输入的下一个标记扫描为布尔值并返回该值。如果下一个标记不能转换为有效的布尔值，则此方法将抛出 <code>InputMismatchException</code>。
     * 如果匹配成功，扫描器将跳过匹配的输入。
     *
     * @return 从输入扫描的布尔值
     * @throws InputMismatchException 如果下一个标记不是有效的布尔值
     * @throws NoSuchElementException 如果输入已耗尽
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean nextBoolean()  {
        clearCaches();
        return Boolean.parseBoolean(next(boolPattern()));
    }


                /**
     * 如果此扫描器的输入中的下一个标记可以使用 {@link #nextByte} 方法解释为默认基数的字节值，则返回 true。
     * 扫描器不会前进到任何输入。
     *
     * @return 如果且仅如果此扫描器的下一个标记是有效的字节值，则返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNextByte() {
        return hasNextByte(defaultRadix);
    }

    /**
     * 如果此扫描器的输入中的下一个标记可以使用 {@link #nextByte} 方法解释为指定基数的字节值，则返回 true。
     * 扫描器不会前进到任何输入。
     *
     * @param radix 用于解释标记为字节值的基数
     * @return 如果且仅如果此扫描器的下一个标记是有效的字节值，则返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNextByte(int radix) {
        setRadix(radix);
        boolean result = hasNext(integerPattern());
        if (result) { // 缓存它
            try {
                String s = (matcher.group(SIMPLE_GROUP_INDEX) == null) ?
                    processIntegerToken(hasNextResult) :
                    hasNextResult;
                typeCache = Byte.parseByte(s, radix);
            } catch (NumberFormatException nfe) {
                result = false;
            }
        }
        return result;
    }

    /**
     * 将输入的下一个标记扫描为 <tt>byte</tt>。
     *
     * <p> 以 <tt>nextByte()</tt> 形式调用此方法的行为与调用 <tt>nextByte(radix)</tt> 完全相同，
     * 其中 <code>radix</code> 是此扫描器的默认基数。
     *
     * @return 从输入扫描的 <tt>byte</tt>
     * @throws InputMismatchException
     *         如果下一个标记不匹配 <i>Integer</i> 正则表达式，或超出范围
     * @throws NoSuchElementException 如果输入已耗尽
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public byte nextByte() {
         return nextByte(defaultRadix);
    }

    /**
     * 将输入的下一个标记扫描为 <tt>byte</tt>。
     * 如果下一个标记不能转换为有效的字节值，此方法将抛出 <code>InputMismatchException</code>。
     * 如果转换成功，扫描器将跳过匹配的输入。
     *
     * <p> 如果下一个标记匹配上述定义的 <a
     * href="#Integer-regex"><i>Integer</i></a> 正则表达式，则标记将被转换为 <tt>byte</tt> 值，就像通过
     * 移除所有特定于区域设置的前缀、分组分隔符和特定于区域设置的后缀，然后通过 {@link Character#digit Character.digit} 将非 ASCII 数字映射为 ASCII 数字，
     * 如果存在特定于区域设置的负前缀和后缀，则在前面加上负号 (-)，并将结果字符串传递给
     * {@link Byte#parseByte(String, int) Byte.parseByte}，并指定基数。
     *
     * @param radix 用于解释标记为字节值的基数
     * @return 从输入扫描的 <tt>byte</tt>
     * @throws InputMismatchException
     *         如果下一个标记不匹配 <i>Integer</i> 正则表达式，或超出范围
     * @throws NoSuchElementException 如果输入已耗尽
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public byte nextByte(int radix) {
        // 检查缓存结果
        if ((typeCache != null) && (typeCache instanceof Byte)
            && this.radix == radix) {
            byte val = ((Byte)typeCache).byteValue();
            useTypeCache();
            return val;
        }
        setRadix(radix);
        clearCaches();
        // 搜索下一个字节
        try {
            String s = next(integerPattern());
            if (matcher.group(SIMPLE_GROUP_INDEX) == null)
                s = processIntegerToken(s);
            return Byte.parseByte(s, radix);
        } catch (NumberFormatException nfe) {
            position = matcher.start(); // 不跳过无效标记
            throw new InputMismatchException(nfe.getMessage());
        }
    }

    /**
     * 如果此扫描器的输入中的下一个标记可以使用 {@link #nextShort} 方法解释为默认基数的短整型值，则返回 true。
     * 扫描器不会前进到任何输入。
     *
     * @return 如果且仅如果此扫描器的下一个标记是默认基数的有效短整型值，则返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNextShort() {
        return hasNextShort(defaultRadix);
    }

    /**
     * 如果此扫描器的输入中的下一个标记可以使用 {@link #nextShort} 方法解释为指定基数的短整型值，则返回 true。
     * 扫描器不会前进到任何输入。
     *
     * @param radix 用于解释标记为短整型值的基数
     * @return 如果且仅如果此扫描器的下一个标记是指定基数的有效短整型值，则返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNextShort(int radix) {
        setRadix(radix);
        boolean result = hasNext(integerPattern());
        if (result) { // 缓存它
            try {
                String s = (matcher.group(SIMPLE_GROUP_INDEX) == null) ?
                    processIntegerToken(hasNextResult) :
                    hasNextResult;
                typeCache = Short.parseShort(s, radix);
            } catch (NumberFormatException nfe) {
                result = false;
            }
        }
        return result;
    }

    /**
     * 将输入的下一个标记扫描为 <tt>short</tt>。
     *
     * <p> 以 <tt>nextShort()</tt> 形式调用此方法的行为与调用 <tt>nextShort(radix)</tt> 完全相同，
     * 其中 <code>radix</code> 是此扫描器的默认基数。
     *
     * @return 从输入扫描的 <tt>short</tt>
     * @throws InputMismatchException
     *         如果下一个标记不匹配 <i>Integer</i> 正则表达式，或超出范围
     * @throws NoSuchElementException 如果输入已耗尽
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public short nextShort() {
        return nextShort(defaultRadix);
    }

                /**
     * 读取输入的下一个标记作为 <tt>short</tt>。
     * 如果下一个标记不能被转换成有效的 short 值，此方法将抛出 <code>InputMismatchException</code>。
     * 如果转换成功，扫描器将跳过匹配的输入。
     *
     * <p> 如果下一个标记匹配上述定义的 <a
     * href="#Integer-regex"><i>Integer</i></a> 正则表达式，则该标记将被转换为 <tt>short</tt> 值，就像通过
     * 移除所有特定于区域设置的前缀、分组分隔符和特定于区域设置的后缀，然后通过 {@link Character#digit Character.digit} 将非 ASCII 数字映射为 ASCII
     * 数字，如果存在特定于区域设置的负数前缀和后缀，则在前面加上负号 (-)，并将结果字符串传递给
     * {@link Short#parseShort(String, int) Short.parseShort}，并指定基数。
     *
     * @param radix 用于解释标记为 short 值的基数
     * @return 从输入中扫描到的 <tt>short</tt>
     * @throws InputMismatchException
     *         如果下一个标记不符合 <i>Integer</i>
     *         正则表达式，或超出范围
     * @throws NoSuchElementException 如果输入已耗尽
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public short nextShort(int radix) {
        // 检查缓存结果
        if ((typeCache != null) && (typeCache instanceof Short)
            && this.radix == radix) {
            short val = ((Short)typeCache).shortValue();
            useTypeCache();
            return val;
        }
        setRadix(radix);
        clearCaches();
        // 搜索下一个 short
        try {
            String s = next(integerPattern());
            if (matcher.group(SIMPLE_GROUP_INDEX) == null)
                s = processIntegerToken(s);
            return Short.parseShort(s, radix);
        } catch (NumberFormatException nfe) {
            position = matcher.start(); // 不跳过无效标记
            throw new InputMismatchException(nfe.getMessage());
        }
    }

    /**
     * 如果此扫描器输入的下一个标记可以使用 {@link #nextInt} 方法在默认基数下解释为 int 值，则返回 true。扫描器不会跳过任何输入。
     *
     * @return 如果且仅如果此扫描器的下一个标记是有效的 int 值，则返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNextInt() {
        return hasNextInt(defaultRadix);
    }

    /**
     * 如果此扫描器输入的下一个标记可以使用 {@link #nextInt} 方法在指定的基数下解释为 int 值，则返回 true。扫描器不会跳过任何输入。
     *
     * @param radix 用于解释标记为 int 值的基数
     * @return 如果且仅如果此扫描器的下一个标记是有效的 int 值，则返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNextInt(int radix) {
        setRadix(radix);
        boolean result = hasNext(integerPattern());
        if (result) { // 缓存它
            try {
                String s = (matcher.group(SIMPLE_GROUP_INDEX) == null) ?
                    processIntegerToken(hasNextResult) :
                    hasNextResult;
                typeCache = Integer.parseInt(s, radix);
            } catch (NumberFormatException nfe) {
                result = false;
            }
        }
        return result;
    }

    /**
     * 必须移除整数标记的前缀、分组分隔符、后缀，并将非 ASCII 数字转换为 ASCII 数字，解析器才能接受它。
     */
    private String processIntegerToken(String token) {
        String result = token.replaceAll(""+groupSeparator, "");
        boolean isNegative = false;
        int preLen = negativePrefix.length();
        if ((preLen > 0) && result.startsWith(negativePrefix)) {
            isNegative = true;
            result = result.substring(preLen);
        }
        int sufLen = negativeSuffix.length();
        if ((sufLen > 0) && result.endsWith(negativeSuffix)) {
            isNegative = true;
            result = result.substring(result.length() - sufLen,
                                      result.length());
        }
        if (isNegative)
            result = "-" + result;
        return result;
    }

    /**
     * 读取输入的下一个标记作为 <tt>int</tt>。
     *
     * <p> 以 <tt>nextInt()</tt> 形式调用此方法的行为与调用 <tt>nextInt(radix)</tt> 完全相同，其中 <code>radix</code>
     * 是此扫描器的默认基数。
     *
     * @return 从输入中扫描到的 <tt>int</tt>
     * @throws InputMismatchException
     *         如果下一个标记不符合 <i>Integer</i>
     *         正则表达式，或超出范围
     * @throws NoSuchElementException 如果输入已耗尽
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public int nextInt() {
        return nextInt(defaultRadix);
    }

    /**
     * 读取输入的下一个标记作为 <tt>int</tt>。
     * 如果下一个标记不能被转换成有效的 int 值，此方法将抛出 <code>InputMismatchException</code>。
     * 如果转换成功，扫描器将跳过匹配的输入。
     *
     * <p> 如果下一个标记匹配上述定义的 <a
     * href="#Integer-regex"><i>Integer</i></a> 正则表达式，则该标记将被转换为 <tt>int</tt> 值，就像通过
     * 移除所有特定于区域设置的前缀、分组分隔符和特定于区域设置的后缀，然后通过 {@link Character#digit Character.digit} 将非 ASCII 数字映射为 ASCII
     * 数字，如果存在特定于区域设置的负数前缀和后缀，则在前面加上负号 (-)，并将结果字符串传递给
     * {@link Integer#parseInt(String, int) Integer.parseInt}，并指定基数。
     *
     * @param radix 用于解释标记为 int 值的基数
     * @return 从输入中扫描到的 <tt>int</tt>
     * @throws InputMismatchException
     *         如果下一个标记不符合 <i>Integer</i>
     *         正则表达式，或超出范围
     * @throws NoSuchElementException 如果输入已耗尽
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public int nextInt(int radix) {
        // 检查缓存结果
        if ((typeCache != null) && (typeCache instanceof Integer)
            && this.radix == radix) {
            int val = ((Integer)typeCache).intValue();
            useTypeCache();
            return val;
        }
        setRadix(radix);
        clearCaches();
        // 搜索下一个 int
        try {
            String s = next(integerPattern());
            if (matcher.group(SIMPLE_GROUP_INDEX) == null)
                s = processIntegerToken(s);
            return Integer.parseInt(s, radix);
        } catch (NumberFormatException nfe) {
            position = matcher.start(); // 不跳过无效标记
            throw new InputMismatchException(nfe.getMessage());
        }
    }

                /**
     * 如果此扫描器的输入中的下一个标记可以被解释为使用 {@link #nextLong} 方法的默认基数的 long 值，则返回 true。扫描器不会跳过任何输入。
     *
     * @return 如果且仅如果此扫描器的下一个标记是有效的 long 值，则返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNextLong() {
        return hasNextLong(defaultRadix);
    }

    /**
     * 如果此扫描器的输入中的下一个标记可以被解释为使用指定基数的 long 值，则返回 true。扫描器不会跳过任何输入。
     *
     * @param radix 用于将标记解释为 long 值的基数
     * @return 如果且仅如果此扫描器的下一个标记是有效的 long 值，则返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNextLong(int radix) {
        setRadix(radix);
        boolean result = hasNext(integerPattern());
        if (result) { // 缓存它
            try {
                String s = (matcher.group(SIMPLE_GROUP_INDEX) == null) ?
                    processIntegerToken(hasNextResult) :
                    hasNextResult;
                typeCache = Long.parseLong(s, radix);
            } catch (NumberFormatException nfe) {
                result = false;
            }
        }
        return result;
    }

    /**
     * 将输入的下一个标记扫描为 <tt>long</tt>。
     *
     * <p> 以 <tt>nextLong()</tt> 形式调用此方法的行为与调用 <tt>nextLong(radix)</tt> 完全相同，其中 <code>radix</code>
     * 是此扫描器的默认基数。
     *
     * @return 从输入扫描的 <tt>long</tt>
     * @throws InputMismatchException
     *         如果下一个标记不匹配 <i>Integer</i> 正则表达式，或者超出范围
     * @throws NoSuchElementException 如果输入已耗尽
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public long nextLong() {
        return nextLong(defaultRadix);
    }

    /**
     * 将输入的下一个标记扫描为 <tt>long</tt>。
     * 如果下一个标记不能被转换为有效的 long 值，此方法将抛出 <code>InputMismatchException</code>。
     * 如果转换成功，扫描器将跳过匹配的输入。
     *
     * <p> 如果下一个标记匹配上述定义的 <a
     * href="#Integer-regex"><i>Integer</i></a> 正则表达式，则该标记将被转换为 <tt>long</tt> 值，就像通过
     * 移除所有特定于区域设置的前缀、分组分隔符和特定于区域设置的后缀，然后通过 {@link Character#digit Character.digit} 将非 ASCII 数字映射为 ASCII
     * 数字，如果存在特定于区域设置的负前缀和后缀，则在前面加上负号 (-)，并将结果字符串传递给
     * {@link Long#parseLong(String, int) Long.parseLong}，指定基数。
     *
     * @param radix 用于将标记解释为 int 值的基数
     * @return 从输入扫描的 <tt>long</tt>
     * @throws InputMismatchException
     *         如果下一个标记不匹配 <i>Integer</i> 正则表达式，或者超出范围
     * @throws NoSuchElementException 如果输入已耗尽
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public long nextLong(int radix) {
        // 检查缓存结果
        if ((typeCache != null) && (typeCache instanceof Long)
            && this.radix == radix) {
            long val = ((Long)typeCache).longValue();
            useTypeCache();
            return val;
        }
        setRadix(radix);
        clearCaches();
        try {
            String s = next(integerPattern());
            if (matcher.group(SIMPLE_GROUP_INDEX) == null)
                s = processIntegerToken(s);
            return Long.parseLong(s, radix);
        } catch (NumberFormatException nfe) {
            position = matcher.start(); // 不跳过错误的标记
            throw new InputMismatchException(nfe.getMessage());
        }
    }

    /**
     * 浮点数标记必须去除前缀、分组分隔符和后缀，非 ASCII 数字必须转换为 ASCII 数字，然后 parseFloat 才能接受它。
     *
     * 如果标记中包含非 ASCII 数字，这些数字必须在将标记传递给 parseFloat 之前进行处理。
     */
    private String processFloatToken(String token) {
        String result = token.replaceAll(groupSeparator, "");
        if (!decimalSeparator.equals("\\."))
            result = result.replaceAll(decimalSeparator, ".");
        boolean isNegative = false;
        int preLen = negativePrefix.length();
        if ((preLen > 0) && result.startsWith(negativePrefix)) {
            isNegative = true;
            result = result.substring(preLen);
        }
        int sufLen = negativeSuffix.length();
        if ((sufLen > 0) && result.endsWith(negativeSuffix)) {
            isNegative = true;
            result = result.substring(result.length() - sufLen,
                                      result.length());
        }
        if (result.equals(nanString))
            result = "NaN";
        if (result.equals(infinityString))
            result = "Infinity";
        if (isNegative)
            result = "-" + result;

        // 转换非 ASCII 数字
        Matcher m = NON_ASCII_DIGIT.matcher(result);
        if (m.find()) {
            StringBuilder inASCII = new StringBuilder();
            for (int i=0; i<result.length(); i++) {
                char nextChar = result.charAt(i);
                if (Character.isDigit(nextChar)) {
                    int d = Character.digit(nextChar, 10);
                    if (d != -1)
                        inASCII.append(d);
                    else
                        inASCII.append(nextChar);
                } else {
                    inASCII.append(nextChar);
                }
            }
            result = inASCII.toString();
        }


                    return result;
    }

    /**
     * 如果此扫描器的输入中的下一个标记可以使用 {@link #nextFloat}
     * 方法解释为浮点值，则返回 true。扫描器不会跳过任何输入。
     *
     * @return 如果且仅如果此扫描器的下一个标记是有效的浮点值，则返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNextFloat() {
        setRadix(10);
        boolean result = hasNext(floatPattern());
        if (result) { // 缓存它
            try {
                String s = processFloatToken(hasNextResult);
                typeCache = Float.valueOf(Float.parseFloat(s));
            } catch (NumberFormatException nfe) {
                result = false;
            }
        }
        return result;
    }

    /**
     * 将输入的下一个标记扫描为 <tt>float</tt>。
     * 如果下一个标记不能转换为有效的浮点值，此方法将抛出 <code>InputMismatchException</code>。
     * 如果转换成功，扫描器将跳过匹配的输入。
     *
     * <p> 如果下一个标记匹配上述定义的 <a
     * href="#Float-regex"><i>Float</i></a> 正则表达式，则该标记将转换为 <tt>float</tt> 值，方法是
     * 删除所有特定于区域设置的前缀、分组分隔符和特定于区域设置的后缀，然后通过 {@link Character#digit Character.digit} 将非 ASCII 数字映射为 ASCII 数字，
     * 如果存在特定于区域设置的负前缀和后缀，则添加负号 (-)，并将结果字符串传递给
     * {@link Float#parseFloat Float.parseFloat}。如果标记匹配特定于区域设置的 NaN 或无穷大字符串，则将 "Nan" 或 "Infinity"
     * 传递给 {@link Float#parseFloat(String) Float.parseFloat}。
     *
     * @return 从输入扫描的 <tt>float</tt>
     * @throws InputMismatchException
     *         如果下一个标记不匹配 <i>Float</i>
     *         正则表达式，或超出范围
     * @throws NoSuchElementException 如果输入已耗尽
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public float nextFloat() {
        // 检查缓存结果
        if ((typeCache != null) && (typeCache instanceof Float)) {
            float val = ((Float)typeCache).floatValue();
            useTypeCache();
            return val;
        }
        setRadix(10);
        clearCaches();
        try {
            return Float.parseFloat(processFloatToken(next(floatPattern())));
        } catch (NumberFormatException nfe) {
            position = matcher.start(); // 不跳过无效的标记
            throw new InputMismatchException(nfe.getMessage());
        }
    }

    /**
     * 如果此扫描器的输入中的下一个标记可以使用 {@link #nextDouble}
     * 方法解释为双精度值，则返回 true。扫描器不会跳过任何输入。
     *
     * @return 如果且仅如果此扫描器的下一个标记是有效的双精度值，则返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNextDouble() {
        setRadix(10);
        boolean result = hasNext(floatPattern());
        if (result) { // 缓存它
            try {
                String s = processFloatToken(hasNextResult);
                typeCache = Double.valueOf(Double.parseDouble(s));
            } catch (NumberFormatException nfe) {
                result = false;
            }
        }
        return result;
    }

    /**
     * 将输入的下一个标记扫描为 <tt>double</tt>。
     * 如果下一个标记不能转换为有效的双精度值，此方法将抛出 <code>InputMismatchException</code>。
     * 如果转换成功，扫描器将跳过匹配的输入。
     *
     * <p> 如果下一个标记匹配上述定义的 <a
     * href="#Float-regex"><i>Float</i></a> 正则表达式，则该标记将转换为 <tt>double</tt> 值，方法是
     * 删除所有特定于区域设置的前缀、分组分隔符和特定于区域设置的后缀，然后通过 {@link Character#digit Character.digit} 将非 ASCII 数字映射为 ASCII 数字，
     * 如果存在特定于区域设置的负前缀和后缀，则添加负号 (-)，并将结果字符串传递给
     * {@link Double#parseDouble Double.parseDouble}。如果标记匹配特定于区域设置的 NaN 或无穷大字符串，则将 "Nan" 或 "Infinity"
     * 传递给 {@link Double#parseDouble(String) Double.parseDouble}。
     *
     * @return 从输入扫描的 <tt>double</tt>
     * @throws InputMismatchException
     *         如果下一个标记不匹配 <i>Float</i>
     *         正则表达式，或超出范围
     * @throws NoSuchElementException 如果输入已耗尽
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public double nextDouble() {
        // 检查缓存结果
        if ((typeCache != null) && (typeCache instanceof Double)) {
            double val = ((Double)typeCache).doubleValue();
            useTypeCache();
            return val;
        }
        setRadix(10);
        clearCaches();
        // 搜索下一个浮点数
        try {
            return Double.parseDouble(processFloatToken(next(floatPattern())));
        } catch (NumberFormatException nfe) {
            position = matcher.start(); // 不跳过无效的标记
            throw new InputMismatchException(nfe.getMessage());
        }
    }

    // 用于扫描多精度数字的便捷方法

    /**
     * 如果此扫描器的输入中的下一个标记可以使用 {@link #nextBigInteger} 方法解释为默认进制的 <code>BigInteger</code>，则返回 true。
     * 扫描器不会跳过任何输入。
     *
     * @return 如果且仅如果此扫描器的下一个标记是有效的 <code>BigInteger</code>，则返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNextBigInteger() {
        return hasNextBigInteger(defaultRadix);
    }


                /**
     * 如果此扫描器的输入中的下一个标记可以使用 {@link #nextBigInteger} 方法解释为指定基数的 <code>BigInteger</code>，则返回 true。
     * 扫描器不会前进到任何输入。
     *
     * @param radix 用于将标记解释为整数的基数
     * @return 如果且仅如果此扫描器的下一个标记是有效的 <code>BigInteger</code>，则返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNextBigInteger(int radix) {
        setRadix(radix);
        boolean result = hasNext(integerPattern());
        if (result) { // 缓存它
            try {
                String s = (matcher.group(SIMPLE_GROUP_INDEX) == null) ?
                    processIntegerToken(hasNextResult) :
                    hasNextResult;
                typeCache = new BigInteger(s, radix);
            } catch (NumberFormatException nfe) {
                result = false;
            }
        }
        return result;
    }

    /**
     * 将输入的下一个标记扫描为 {@link java.math.BigInteger BigInteger}。
     *
     * <p> 以 <tt>nextBigInteger()</tt> 形式调用此方法的行为与调用 <tt>nextBigInteger(radix)</tt> 完全相同，其中 <code>radix</code>
     * 是此扫描器的默认基数。
     *
     * @return 从输入扫描的 <tt>BigInteger</tt>
     * @throws InputMismatchException
     *         如果下一个标记不匹配 <i>Integer</i> 正则表达式，或超出范围
     * @throws NoSuchElementException 如果输入已耗尽
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public BigInteger nextBigInteger() {
        return nextBigInteger(defaultRadix);
    }

    /**
     * 将输入的下一个标记扫描为 {@link java.math.BigInteger BigInteger}。
     *
     * <p> 如果下一个标记匹配上述定义的 <a href="#Integer-regex"><i>Integer</i></a> 正则表达式，则标记将转换为 <tt>BigInteger</tt> 值，就像
     * 通过 {@link Character#digit Character.digit} 将所有分组分隔符移除并将非 ASCII 数字映射为 ASCII 数字，并将结果字符串传递给带有指定基数的
     * {@link java.math.BigInteger#BigInteger(java.lang.String) BigInteger(String, int)} 构造函数一样。
     *
     * @param radix 用于解释标记的基数
     * @return 从输入扫描的 <tt>BigInteger</tt>
     * @throws InputMismatchException
     *         如果下一个标记不匹配 <i>Integer</i> 正则表达式，或超出范围
     * @throws NoSuchElementException 如果输入已耗尽
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public BigInteger nextBigInteger(int radix) {
        // 检查缓存结果
        if ((typeCache != null) && (typeCache instanceof BigInteger)
            && this.radix == radix) {
            BigInteger val = (BigInteger)typeCache;
            useTypeCache();
            return val;
        }
        setRadix(radix);
        clearCaches();
        // 搜索下一个整数
        try {
            String s = next(integerPattern());
            if (matcher.group(SIMPLE_GROUP_INDEX) == null)
                s = processIntegerToken(s);
            return new BigInteger(s, radix);
        } catch (NumberFormatException nfe) {
            position = matcher.start(); // 不跳过无效标记
            throw new InputMismatchException(nfe.getMessage());
        }
    }

    /**
     * 如果此扫描器的输入中的下一个标记可以使用 {@link #nextBigDecimal} 方法解释为 <code>BigDecimal</code>，则返回 true。
     * 扫描器不会前进到任何输入。
     *
     * @return 如果且仅如果此扫描器的下一个标记是有效的 <code>BigDecimal</code>，则返回 true
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public boolean hasNextBigDecimal() {
        setRadix(10);
        boolean result = hasNext(decimalPattern());
        if (result) { // 缓存它
            try {
                String s = processFloatToken(hasNextResult);
                typeCache = new BigDecimal(s);
            } catch (NumberFormatException nfe) {
                result = false;
            }
        }
        return result;
    }

    /**
     * 将输入的下一个标记扫描为 {@link java.math.BigDecimal BigDecimal}。
     *
     * <p> 如果下一个标记匹配上述定义的 <a href="#Decimal-regex"><i>Decimal</i></a> 正则表达式，则标记将转换为 <tt>BigDecimal</tt> 值，就像
     * 通过 {@link Character#digit Character.digit} 将所有分组分隔符移除并将非 ASCII 数字映射为 ASCII 数字，并将结果字符串传递给
     * {@link java.math.BigDecimal#BigDecimal(java.lang.String) BigDecimal(String)} 构造函数一样。
     *
     * @return 从输入扫描的 <tt>BigDecimal</tt>
     * @throws InputMismatchException
     *         如果下一个标记不匹配 <i>Decimal</i> 正则表达式，或超出范围
     * @throws NoSuchElementException 如果输入已耗尽
     * @throws IllegalStateException 如果此扫描器已关闭
     */
    public BigDecimal nextBigDecimal() {
        // 检查缓存结果
        if ((typeCache != null) && (typeCache instanceof BigDecimal)) {
            BigDecimal val = (BigDecimal)typeCache;
            useTypeCache();
            return val;
        }
        setRadix(10);
        clearCaches();
        // 搜索下一个浮点数
        try {
            String s = processFloatToken(next(decimalPattern()));
            return new BigDecimal(s);
        } catch (NumberFormatException nfe) {
            position = matcher.start(); // 不跳过无效标记
            throw new InputMismatchException(nfe.getMessage());
        }
    }

    /**
     * 重置此扫描器。
     *
     * <p> 重置扫描器会丢弃所有可能已通过调用 {@link #useDelimiter}、{@link #useLocale} 或 {@link #useRadix} 更改的显式状态信息。
     *
     * <p> 以 <tt>scanner.reset()</tt> 形式调用此方法的行为与调用
     *
     * <blockquote><pre>{@code
     *   scanner.useDelimiter("\\p{javaWhitespace}+")
     *          .useLocale(Locale.getDefault(Locale.Category.FORMAT))
     *          .useRadix(10);
     * }</pre></blockquote>
     *
     * 完全相同。
     *
     * @return 此扫描器
     *
     * @since 1.6
     */
    public Scanner reset() {
        delimPattern = WHITESPACE_PATTERN;
        useLocale(Locale.getDefault(Locale.Category.FORMAT));
        useRadix(10);
        clearCaches();
        return this;
    }
}
