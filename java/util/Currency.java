
/*
 * Copyright (c) 2000, 2015, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.spi.CurrencyNameProvider;
import sun.util.locale.provider.LocaleServiceProviderPool;
import sun.util.logging.PlatformLogger;


/**
 * 表示货币。货币由其 ISO 4217 货币代码标识。访问 <a href="http://www.iso.org/iso/home/standards/currency_codes.htm">
 * ISO 网站</a>以获取更多信息。
 * <p>
 * 该类设计为对于任何给定的货币，最多只有一个 <code>Currency</code> 实例。因此，没有公共构造函数。您可以通过
 * <code>getInstance</code> 方法获取 <code>Currency</code> 实例。
 * <p>
 * 用户可以通过系统属性 {@code java.util.currency.data} 覆盖 Java 运行时货币数据。如果定义了此系统属性，则其值是属性文件的位置，
 * 其内容是 ISO 3166 国家代码和 ISO 4217 货币数据的键值对。值部分由三个 ISO 4217 值组成，即字母代码、数字代码和小单位。
 * 这三个 ISO 4217 值用逗号分隔。以 '#' 开头的行被视为注释行。如果用户需要指定一个生效日期以指示新数据何时生效，可以为每个货币条目指定一个可选的 UTC 时间戳。
 * 时间戳附加在货币属性的末尾，并使用逗号作为分隔符。如果存在且有效，JRE 仅在当前 UTC 日期晚于类加载时指定的日期时使用新的货币属性。
 * 时间戳的格式必须为 ISO 8601 格式：{@code 'yyyy-MM-dd'T'HH:mm:ss'}。例如，
 * <p>
 * <code>
 * #示例货币属性<br>
 * JP=JPZ,999,0
 * </code>
 * <p>
 * 将覆盖日本的货币数据。
 *
 * <p>
 * <code>
 * #带有生效日期的示例货币属性<br>
 * JP=JPZ,999,0,2014-01-01T00:00:00
 * </code>
 * <p>
 * 如果 {@code Currency} 类在 2014 年 1 月 1 日 00:00:00 GMT 之后加载，将覆盖日本的货币数据。
 * <p>
 * 遇到语法错误的条目时，将忽略该条目并处理文件中的其余条目。对于存在重复国家代码条目的情况，该 {@code Currency} 的货币信息行为是未定义的，并处理文件中的其余条目。
 *
 * @since 1.4
 */
public final class Currency implements Serializable {

    private static final long serialVersionUID = -158308464356906721L;

    /**
     * 此货币的 ISO 4217 货币代码。
     *
     * @serial
     */
    private final String currencyCode;

    /**
     * 此货币的默认分数位数。
     * 从货币数据表中设置。
     */
    transient private final int defaultFractionDigits;

    /**
     * 此货币的 ISO 4217 数字代码。
     * 从货币数据表中设置。
     */
    transient private final int numericCode;


    // 类数据：实例映射

    private static ConcurrentMap<String, Currency> instances = new ConcurrentHashMap<>(7);
    private static HashSet<Currency> available;

    // 类数据：从 currency.data 文件中获取的货币数据。
    // 目的：
    // - 确定有效的国家代码
    // - 确定有效的货币代码
    // - 将国家代码映射到货币代码
    // - 获取货币代码的默认分数位数
    //
    // sc = 特殊情况；dfd = 默认分数位数
    // 简单国家是指国家代码是货币代码的前缀，并且没有已知的计划更改货币。
    //
    // 表格格式：
    // - mainTable:
    //   - 将国家代码映射到 32 位整数
    //   - 26*26 个条目，对应于 [A-Z]*[A-Z]
    //   - \u007F -> 无效国家
    //   - 位 20-31：未使用
    //   - 位 10-19：数字代码（0 到 1023）
    //   - 位 9：1 - 特殊情况，位 0-4 表示哪一个
    //            0 - 简单国家，位 0-4 表示货币代码的最后一个字符
    //   - 位 5-8：简单国家的分数位数，特殊情况为 0
    //   - 位 0-4：简单国家货币代码的最后一个字符，或特殊情况的 ID
    // - 特殊情况 ID：
    //   - 0: 国家没有货币
    //   - 其他：sc* 数组的索引 + 1
    // - scCutOverTimes: 特殊情况国家更改货币的转换时间（毫秒），由
    //   System.currentTimeMillis 返回；对于不更改货币的国家，为 Long.MAX_VALUE
    // - scOldCurrencies: 特殊情况国家的旧货币
    // - scNewCurrencies: 更改货币的特殊情况国家的新货币；对于其他情况为 null
    // - scOldCurrenciesDFD: 旧货币的默认分数位数
    // - scNewCurrenciesDFD: 新货币的默认分数位数，对于不更改货币的国家为 0
    // - otherCurrencies: 所有不是简单国家主要货币的货币代码的连接，用 "-" 分隔
    // - otherCurrenciesDFD: otherCurrencies 中货币的十进制格式位数，顺序相同


/**
 * 刷新流。
 */
public void flush() { }

/**
 * 关闭流。
 */
public void close() { }

}


                static int formatVersion;
    static int dataVersion;
    static int[] mainTable;
    static long[] scCutOverTimes;
    static String[] scOldCurrencies;
    static String[] scNewCurrencies;
    static int[] scOldCurrenciesDFD;
    static int[] scNewCurrenciesDFD;
    static int[] scOldCurrenciesNumericCode;
    static int[] scNewCurrenciesNumericCode;
    static String otherCurrencies;
    static int[] otherCurrenciesDFD;
    static int[] otherCurrenciesNumericCode;

    // 方便的常量 - 必须与 GenerateCurrencyData 中的定义匹配
    // 魔术数字
    private static final int MAGIC_NUMBER = 0x43757244;
    // 从 A 到 Z 的字符数
    private static final int A_TO_Z = ('Z' - 'A') + 1;
    // 无效国家代码的条目
    private static final int INVALID_COUNTRY_ENTRY = 0x0000007F;
    // 没有货币的国家的条目
    private static final int COUNTRY_WITHOUT_CURRENCY_ENTRY = 0x00000200;
    // 简单情况国家条目的掩码
    private static final int SIMPLE_CASE_COUNTRY_MASK = 0x00000000;
    // 简单情况国家条目的最终字符掩码
    private static final int SIMPLE_CASE_COUNTRY_FINAL_CHAR_MASK = 0x0000001F;
    // 简单情况国家条目的默认货币位数掩码
    private static final int SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_MASK = 0x000001E0;
    // 简单情况国家条目的默认货币位数移位数
    private static final int SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_SHIFT = 5;
    // 简单情况国家条目的最大默认货币位数
    private static final int SIMPLE_CASE_COUNTRY_MAX_DEFAULT_DIGITS = 9;
    // 特殊情况国家条目的掩码
    private static final int SPECIAL_CASE_COUNTRY_MASK = 0x00000200;
    // 特殊情况国家索引的掩码
    private static final int SPECIAL_CASE_COUNTRY_INDEX_MASK = 0x0000001F;
    // 主表中条目索引组件到特殊情况表索引的增量
    private static final int SPECIAL_CASE_COUNTRY_INDEX_DELTA = 1;
    // 区分简单和特殊情况国家的掩码
    private static final int COUNTRY_TYPE_MASK = SIMPLE_CASE_COUNTRY_MASK | SPECIAL_CASE_COUNTRY_MASK;
    // 货币的数字代码掩码
    private static final int NUMERIC_CODE_MASK = 0x000FFC00;
    // 货币的数字代码移位数
    private static final int NUMERIC_CODE_SHIFT = 10;

    // 货币数据格式版本
    private static final int VALID_FORMAT_VERSION = 2;

    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                String homeDir = System.getProperty("java.home");
                try {
                    String dataFile = homeDir + File.separator +
                            "lib" + File.separator + "currency.data";
                    try (DataInputStream dis = new DataInputStream(
                             new BufferedInputStream(
                             new FileInputStream(dataFile)))) {
                        if (dis.readInt() != MAGIC_NUMBER) {
                            throw new InternalError("货币数据可能已损坏");
                        }
                        formatVersion = dis.readInt();
                        if (formatVersion != VALID_FORMAT_VERSION) {
                            throw new InternalError("货币数据格式不正确");
                        }
                        dataVersion = dis.readInt();
                        mainTable = readIntArray(dis, A_TO_Z * A_TO_Z);
                        int scCount = dis.readInt();
                        scCutOverTimes = readLongArray(dis, scCount);
                        scOldCurrencies = readStringArray(dis, scCount);
                        scNewCurrencies = readStringArray(dis, scCount);
                        scOldCurrenciesDFD = readIntArray(dis, scCount);
                        scNewCurrenciesDFD = readIntArray(dis, scCount);
                        scOldCurrenciesNumericCode = readIntArray(dis, scCount);
                        scNewCurrenciesNumericCode = readIntArray(dis, scCount);
                        int ocCount = dis.readInt();
                        otherCurrencies = dis.readUTF();
                        otherCurrenciesDFD = readIntArray(dis, ocCount);
                        otherCurrenciesNumericCode = readIntArray(dis, ocCount);
                    }
                } catch (IOException e) {
                    throw new InternalError(e);
                }

                // 查找属性文件以进行覆盖
                String propsFile = System.getProperty("java.util.currency.data");
                if (propsFile == null) {
                    propsFile = homeDir + File.separator + "lib" +
                        File.separator + "currency.properties";
                }
                try {
                    File propFile = new File(propsFile);
                    if (propFile.exists()) {
                        Properties props = new Properties();
                        try (FileReader fr = new FileReader(propFile)) {
                            props.load(fr);
                        }
                        Set<String> keys = props.stringPropertyNames();
                        Pattern propertiesPattern =
                            Pattern.compile("([A-Z]{3})\\s*,\\s*(\\d{3})\\s*,\\s*" +
                                "(\\d+)\\s*,?\\s*(\\d{4}-\\d{2}-\\d{2}T\\d{2}:" +
                                "\\d{2}:\\d{2})?");
                        for (String key : keys) {
                           replaceCurrencyData(propertiesPattern,
                               key.toUpperCase(Locale.ROOT),
                               props.getProperty(key).toUpperCase(Locale.ROOT));
                        }
                    }
                } catch (IOException e) {
                    info("currency.properties 因 IOException 被忽略", e);
                }
                return null;
            }
        });
    }

    /**
     * 用于从名称提供者中检索本地化名称的常量。
     */
    private static final int SYMBOL = 0;
    private static final int DISPLAYNAME = 1;


    /**
     * 构造一个 <code>Currency</code> 实例。构造函数是私有的
     * 以便我们可以确保对于给定的货币，永远不会有多于一个的实例。
     */
    private Currency(String currencyCode, int defaultFractionDigits, int numericCode) {
        this.currencyCode = currencyCode;
        this.defaultFractionDigits = defaultFractionDigits;
        this.numericCode = numericCode;
    }

    /**
     * 返回给定货币代码的 <code>Currency</code> 实例。
     *
     * @param currencyCode 货币的 ISO 4217 代码
     * @return 给定货币代码的 <code>Currency</code> 实例
     * @exception NullPointerException 如果 <code>currencyCode</code> 为 null
     * @exception IllegalArgumentException 如果 <code>currencyCode</code> 不是
     * 支持的 ISO 4217 代码。
     */
    public static Currency getInstance(String currencyCode) {
        return getInstance(currencyCode, Integer.MIN_VALUE, 0);
    }

    private static Currency getInstance(String currencyCode, int defaultFractionDigits,
        int numericCode) {
        // 尝试在实例表中查找货币代码。
        // 这会作为副作用进行空指针检查。
        // 另外，如果已经存在条目，货币代码必须是有效的。
        Currency instance = instances.get(currencyCode);
        if (instance != null) {
            return instance;
        }

        if (defaultFractionDigits == Integer.MIN_VALUE) {
            // 货币代码不是内部生成的，需要先验证
            // 货币代码必须有 3 个字符，并且存在于主表
            // 或其他货币列表中。
            if (currencyCode.length() != 3) {
                throw new IllegalArgumentException();
            }
            char char1 = currencyCode.charAt(0);
            char char2 = currencyCode.charAt(1);
            int tableEntry = getMainTableEntry(char1, char2);
            if ((tableEntry & COUNTRY_TYPE_MASK) == SIMPLE_CASE_COUNTRY_MASK
                    && tableEntry != INVALID_COUNTRY_ENTRY
                    && currencyCode.charAt(2) - 'A' == (tableEntry & SIMPLE_CASE_COUNTRY_FINAL_CHAR_MASK)) {
                defaultFractionDigits = (tableEntry & SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_MASK) >> SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_SHIFT;
                numericCode = (tableEntry & NUMERIC_CODE_MASK) >> NUMERIC_CODE_SHIFT;
            } else {
                // 单独检查 '-' 以避免在表中出现误报。
                if (currencyCode.charAt(2) == '-') {
                    throw new IllegalArgumentException();
                }
                int index = otherCurrencies.indexOf(currencyCode);
                if (index == -1) {
                    throw new IllegalArgumentException();
                }
                defaultFractionDigits = otherCurrenciesDFD[index / 4];
                numericCode = otherCurrenciesNumericCode[index / 4];
            }
        }

        Currency currencyVal =
            new Currency(currencyCode, defaultFractionDigits, numericCode);
        instance = instances.putIfAbsent(currencyCode, currencyVal);
        return (instance != null ? instance : currencyVal);
    }

    /**
     * 返回给定区域设置国家的 <code>Currency</code> 实例。区域设置的语言和变体组件将被忽略。
     * 结果可能会随着时间变化，因为国家会更改其货币。例如，对于欧洲货币联盟的原始成员国，
     * 该方法在 2001 年 12 月 31 日之前返回旧的国家货币，在 2002 年 1 月 1 日，各自国家的本地时间
     * 返回欧元。
     * <p>
     * 对于没有货币的领土，例如南极洲，该方法返回 <code>null</code>。
     *
     * @param locale 需要 <code>Currency</code> 实例的国家的区域设置
     * @return 给定区域设置国家的 <code>Currency</code> 实例，或 {@code null}
     * @exception NullPointerException 如果 <code>locale</code> 或其国家代码为 {@code null}
     * @exception IllegalArgumentException 如果给定 {@code locale} 的国家不是支持的 ISO 3166 国家代码。
     */
    public static Currency getInstance(Locale locale) {
        String country = locale.getCountry();
        if (country == null) {
            throw new NullPointerException();
        }

        if (country.length() != 2) {
            throw new IllegalArgumentException();
        }

        char char1 = country.charAt(0);
        char char2 = country.charAt(1);
        int tableEntry = getMainTableEntry(char1, char2);
        if ((tableEntry & COUNTRY_TYPE_MASK) == SIMPLE_CASE_COUNTRY_MASK
                    && tableEntry != INVALID_COUNTRY_ENTRY) {
            char finalChar = (char) ((tableEntry & SIMPLE_CASE_COUNTRY_FINAL_CHAR_MASK) + 'A');
            int defaultFractionDigits = (tableEntry & SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_MASK) >> SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_SHIFT;
            int numericCode = (tableEntry & NUMERIC_CODE_MASK) >> NUMERIC_CODE_SHIFT;
            StringBuilder sb = new StringBuilder(country);
            sb.append(finalChar);
            return getInstance(sb.toString(), defaultFractionDigits, numericCode);
        } else {
            // 特殊情况
            if (tableEntry == INVALID_COUNTRY_ENTRY) {
                throw new IllegalArgumentException();
            }
            if (tableEntry == COUNTRY_WITHOUT_CURRENCY_ENTRY) {
                return null;
            } else {
                int index = (tableEntry & SPECIAL_CASE_COUNTRY_INDEX_MASK) - SPECIAL_CASE_COUNTRY_INDEX_DELTA;
                if (scCutOverTimes[index] == Long.MAX_VALUE || System.currentTimeMillis() < scCutOverTimes[index]) {
                    return getInstance(scOldCurrencies[index], scOldCurrenciesDFD[index],
                        scOldCurrenciesNumericCode[index]);
                } else {
                    return getInstance(scNewCurrencies[index], scNewCurrenciesDFD[index],
                        scNewCurrenciesNumericCode[index]);
                }
            }
        }
    }


                /**
     * 获取可用货币的集合。返回的货币集合包含所有可用的货币，可能包括代表过时的ISO 4217代码的货币。该集合可以修改，而不会影响运行时中的可用货币。
     *
     * @return 可用货币的集合。如果运行时中没有可用的货币，返回的集合为空。
     * @since 1.7
     */
    public static Set<Currency> getAvailableCurrencies() {
        synchronized(Currency.class) {
            if (available == null) {
                available = new HashSet<>(256);

                // 先添加简单的货币
                for (char c1 = 'A'; c1 <= 'Z'; c1 ++) {
                    for (char c2 = 'A'; c2 <= 'Z'; c2 ++) {
                        int tableEntry = getMainTableEntry(c1, c2);
                        if ((tableEntry & COUNTRY_TYPE_MASK) == SIMPLE_CASE_COUNTRY_MASK
                             && tableEntry != INVALID_COUNTRY_ENTRY) {
                            char finalChar = (char) ((tableEntry & SIMPLE_CASE_COUNTRY_FINAL_CHAR_MASK) + 'A');
                            int defaultFractionDigits = (tableEntry & SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_MASK) >> SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_SHIFT;
                            int numericCode = (tableEntry & NUMERIC_CODE_MASK) >> NUMERIC_CODE_SHIFT;
                            StringBuilder sb = new StringBuilder();
                            sb.append(c1);
                            sb.append(c2);
                            sb.append(finalChar);
                            available.add(getInstance(sb.toString(), defaultFractionDigits, numericCode));
                        }
                    }
                }

                // 现在添加其他货币
                StringTokenizer st = new StringTokenizer(otherCurrencies, "-");
                while (st.hasMoreElements()) {
                    available.add(getInstance((String)st.nextElement()));
                }
            }
        }

        @SuppressWarnings("unchecked")
        Set<Currency> result = (Set<Currency>) available.clone();
        return result;
    }

    /**
     * 获取此货币的ISO 4217货币代码。
     *
     * @return 此货币的ISO 4217货币代码。
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * 获取此货币在默认 {@link Locale.Category#DISPLAY DISPLAY} 语言环境中的符号。
     * 例如，对于美元，如果默认语言环境是美国，则符号为"$"，而对于其他语言环境，可能是"US$"。如果无法确定符号，则返回ISO 4217货币代码。
     * <p>
     * 这相当于调用
     * {@link #getSymbol(Locale)
     *     getSymbol(Locale.getDefault(Locale.Category.DISPLAY))}。
     *
     * @return 此货币在默认 {@link Locale.Category#DISPLAY DISPLAY} 语言环境中的符号
     */
    public String getSymbol() {
        return getSymbol(Locale.getDefault(Locale.Category.DISPLAY));
    }

    /**
     * 获取此货币在指定语言环境中的符号。
     * 例如，对于美元，如果指定的语言环境是美国，则符号为"$"，而对于其他语言环境，可能是"US$"。如果无法确定符号，则返回ISO 4217货币代码。
     *
     * @param locale 需要此货币显示名称的语言环境
     * @return 此货币在指定语言环境中的符号
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     */
    public String getSymbol(Locale locale) {
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(CurrencyNameProvider.class);
        String symbol = pool.getLocalizedObject(
                                CurrencyNameGetter.INSTANCE,
                                locale, currencyCode, SYMBOL);
        if (symbol != null) {
            return symbol;
        }

        // 作为最后的手段，使用货币代码作为符号
        return currencyCode;
    }

    /**
     * 获取与此货币一起使用的默认小数位数。
     * 例如，欧元的默认小数位数为2，而日元为0。
     * 对于类似IMF特别提款权的伪货币，返回-1。
     *
     * @return 与此货币一起使用的默认小数位数
     */
    public int getDefaultFractionDigits() {
        return defaultFractionDigits;
    }

    /**
     * 返回此货币的ISO 4217数字代码。
     *
     * @return 此货币的ISO 4217数字代码
     * @since 1.7
     */
    public int getNumericCode() {
        return numericCode;
    }

    /**
     * 获取适合在默认 {@link Locale.Category#DISPLAY DISPLAY} 语言环境中显示此货币的名称。
     * 如果在默认语言环境中找不到合适的显示名称，则返回ISO 4217货币代码。
     * <p>
     * 这相当于调用
     * {@link #getDisplayName(Locale)
     *     getDisplayName(Locale.getDefault(Locale.Category.DISPLAY))}。
     *
     * @return 适合在默认 {@link Locale.Category#DISPLAY DISPLAY} 语言环境中显示此货币的名称
     * @since 1.7
     */
    public String getDisplayName() {
        return getDisplayName(Locale.getDefault(Locale.Category.DISPLAY));
    }

    /**
     * 获取适合在指定语言环境中显示此货币的名称。如果在指定语言环境中找不到合适的显示名称，则返回ISO 4217货币代码。
     *
     * @param locale 需要此货币显示名称的语言环境
     * @return 适合在指定语言环境中显示此货币的名称
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @since 1.7
     */
    public String getDisplayName(Locale locale) {
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(CurrencyNameProvider.class);
        String result = pool.getLocalizedObject(
                                CurrencyNameGetter.INSTANCE,
                                locale, currencyCode, DISPLAYNAME);
        if (result != null) {
            return result;
        }


                    // 使用货币代码作为最后的符号
        return currencyCode;
    }

    /**
     * 返回此货币的 ISO 4217 货币代码。
     *
     * @return 此货币的 ISO 4217 货币代码
     */
    @Override
    public String toString() {
        return currencyCode;
    }

    /**
     * 解析反序列化的实例，使其每个货币只有一个实例。
     */
    private Object readResolve() {
        return getInstance(currencyCode);
    }

    /**
     * 获取国家代码由 char1 和 char2 组成的国家的主要表条目。
     */
    private static int getMainTableEntry(char char1, char char2) {
        if (char1 < 'A' || char1 > 'Z' || char2 < 'A' || char2 > 'Z') {
            throw new IllegalArgumentException();
        }
        return mainTable[(char1 - 'A') * A_TO_Z + (char2 - 'A')];
    }

    /**
     * 设置国家代码由 char1 和 char2 组成的国家的主要表条目。
     */
    private static void setMainTableEntry(char char1, char char2, int entry) {
        if (char1 < 'A' || char1 > 'Z' || char2 < 'A' || char2 > 'Z') {
            throw new IllegalArgumentException();
        }
        mainTable[(char1 - 'A') * A_TO_Z + (char2 - 'A')] = entry;
    }

    /**
     * 从 CurrencyNameProvider 实现中获取本地化的货币名称。
     */
    private static class CurrencyNameGetter
        implements LocaleServiceProviderPool.LocalizedObjectGetter<CurrencyNameProvider,
                                                                   String> {
        private static final CurrencyNameGetter INSTANCE = new CurrencyNameGetter();

        @Override
        public String getObject(CurrencyNameProvider currencyNameProvider,
                                Locale locale,
                                String key,
                                Object... params) {
            assert params.length == 1;
            int type = (Integer)params[0];

            switch(type) {
            case SYMBOL:
                return currencyNameProvider.getSymbol(key, locale);
            case DISPLAYNAME:
                return currencyNameProvider.getDisplayName(key, locale);
            default:
                assert false; // 不应发生
            }

            return null;
        }
    }

    private static int[] readIntArray(DataInputStream dis, int count) throws IOException {
        int[] ret = new int[count];
        for (int i = 0; i < count; i++) {
            ret[i] = dis.readInt();
        }

        return ret;
    }

    private static long[] readLongArray(DataInputStream dis, int count) throws IOException {
        long[] ret = new long[count];
        for (int i = 0; i < count; i++) {
            ret[i] = dis.readLong();
        }

        return ret;
    }

    private static String[] readStringArray(DataInputStream dis, int count) throws IOException {
        String[] ret = new String[count];
        for (int i = 0; i < count; i++) {
            ret[i] = dis.readUTF();
        }

        return ret;
    }

    /**
     * 替换在 currencydata.properties 文件中找到的货币数据
     *
     * @param pattern 属性的正则表达式模式
     * @param ctry 国家代码
     * @param curdata 货币数据。这是一个由逗号分隔的字符串，包含 "三位字母代码"、"三位数字代码" 和 "一位（0-9）默认分数位数"。
     *    例如，"JPZ,392,0"。
     *    可以在字符串末尾附加一个 UTC 日期（逗号分隔），以允许货币更改在指定日期后生效。
     *    例如，"JP=JPZ,999,0,2014-01-01T00:00:00" 在 UTC 时间未超过 2014 年 1 月 1 日 00:00:00 GMT 之前不会生效。
     */
    private static void replaceCurrencyData(Pattern pattern, String ctry, String curdata) {

        if (ctry.length() != 2) {
            // 忽略无效的国家代码
            info("currency.properties entry for " + ctry +
                    " is ignored because of the invalid country code.", null);
            return;
        }

        Matcher m = pattern.matcher(curdata);
        if (!m.find() || (m.group(4) == null && countOccurrences(curdata, ',') >= 3)) {
            // 未识别的格式。忽略数据
            // 如果 group(4) 日期字符串为 null 且有 4 个值，表示日期值无效
            info("currency.properties entry for " + ctry +
                    " ignored because the value format is not recognized.", null);
            return;
        }

        try {
            if (m.group(4) != null && !isPastCutoverDate(m.group(4))) {
                info("currency.properties entry for " + ctry +
                        " ignored since cutover date has not passed :" + curdata, null);
                return;
            }
        } catch (ParseException ex) {
            info("currency.properties entry for " + ctry +
                        " ignored since exception encountered :" + ex.getMessage(), null);
            return;
        }

        String code = m.group(1);
        int numeric = Integer.parseInt(m.group(2));
        int entry = numeric << NUMERIC_CODE_SHIFT;
        int fraction = Integer.parseInt(m.group(3));
        if (fraction > SIMPLE_CASE_COUNTRY_MAX_DEFAULT_DIGITS) {
            info("currency.properties entry for " + ctry +
                " ignored since the fraction is more than " +
                SIMPLE_CASE_COUNTRY_MAX_DEFAULT_DIGITS + ":" + curdata, null);
            return;
        }

        int index;
        for (index = 0; index < scOldCurrencies.length; index++) {
            if (scOldCurrencies[index].equals(code)) {
                break;
            }
        }

        if (index == scOldCurrencies.length) {
            // 简单情况
            entry |= (fraction << SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_SHIFT) |
                     (code.charAt(2) - 'A');
        } else {
            // 特殊情况
            entry |= SPECIAL_CASE_COUNTRY_MASK |
                     (index + SPECIAL_CASE_COUNTRY_INDEX_DELTA);
        }
        setMainTableEntry(ctry.charAt(0), ctry.charAt(1), entry);
    }


/**
 * 判断给定的日期字符串是否已经过了转换日期。
 * 
 * @param s 日期字符串，格式为 "yyyy-MM-dd'T'HH:mm:ss"。
 * @return 如果当前时间大于给定的日期时间，则返回 true，否则返回 false。
 * @throws ParseException 如果日期字符串格式不正确。
 */
private static boolean isPastCutoverDate(String s) throws ParseException {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);
    format.setTimeZone(TimeZone.getTimeZone("UTC"));
    format.setLenient(false);
    long time = format.parse(s.trim()).getTime();
    return System.currentTimeMillis() > time;

}

/**
 * 计算字符串中指定字符出现的次数。
 * 
 * @param value 要检查的字符串。
 * @param match 要计数的字符。
 * @return 指定字符在字符串中出现的次数。
 */
private static int countOccurrences(String value, char match) {
    int count = 0;
    for (char c : value.toCharArray()) {
        if (c == match) {
           ++count;
        }
    }
    return count;
}

/**
 * 记录信息日志。
 * 
 * @param message 要记录的消息。
 * @param t 可选的异常对象，如果存在则一并记录。
 */
private static void info(String message, Throwable t) {
    PlatformLogger logger = PlatformLogger.getLogger("java.util.Currency");
    if (logger.isLoggable(PlatformLogger.Level.INFO)) {
        if (t != null) {
            logger.info(message, t);
        } else {
            logger.info(message);
        }
    }
}
}
