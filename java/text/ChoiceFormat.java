
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.text;

import java.io.InvalidObjectException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

/**
 * <code>ChoiceFormat</code> 允许您将格式附加到数字范围。通常在 <code>MessageFormat</code> 中用于处理复数。
 * 选择是通过一个升序的双精度浮点数列表指定的，每个项目指定一个半开区间，直到下一个项目：
 * <blockquote>
 * <pre>
 * X 匹配 j 当且仅当 limit[j] &le; X &lt; limit[j+1]
 * </pre>
 * </blockquote>
 * 如果没有匹配项，则使用第一个或最后一个索引，具体取决于数字（X）是太低还是太高。如果 limit 数组不是升序的，格式化的结果将是不正确的。ChoiceFormat 还接受 <code>&#92;u221E</code> 作为无穷大（INF）的等效值。
 *
 * <p>
 * <strong>注意：</strong>
 * <code>ChoiceFormat</code> 与其他 <code>Format</code> 类不同，您需要使用构造函数创建 <code>ChoiceFormat</code> 对象（而不是使用 <code>getInstance</code> 风格的工厂方法）。工厂方法不是必需的，因为 <code>ChoiceFormat</code> 不需要为特定区域设置进行任何复杂的设置。实际上，<code>ChoiceFormat</code> 不实现任何特定于区域设置的行为。
 *
 * <p>
 * 创建 <code>ChoiceFormat</code> 时，必须指定一个格式数组和一个限制数组。这些数组的长度必须相同。
 * 例如，
 * <ul>
 * <li>
 *     <em>limits</em> = {1,2,3,4,5,6,7}<br>
 *     <em>formats</em> = {"Sun","Mon","Tue","Wed","Thur","Fri","Sat"}
 * <li>
 *     <em>limits</em> = {0, 1, ChoiceFormat.nextDouble(1)}<br>
 *     <em>formats</em> = {"no files", "one file", "many files"}<br>
 *     （<code>nextDouble</code> 可用于获取下一个更高的双精度浮点数，以创建半开区间。）
 * </ul>
 *
 * <p>
 * 以下是一个简单的示例，展示了格式化和解析：
 * <blockquote>
 * <pre>{@code
 * double[] limits = {1,2,3,4,5,6,7};
 * String[] dayOfWeekNames = {"Sun","Mon","Tue","Wed","Thur","Fri","Sat"};
 * ChoiceFormat form = new ChoiceFormat(limits, dayOfWeekNames);
 * ParsePosition status = new ParsePosition(0);
 * for (double i = 0.0; i <= 8.0; ++i) {
 *     status.setIndex(0);
 *     System.out.println(i + " -> " + form.format(i) + " -> "
 *                              + form.parse(form.format(i),status));
 * }
 * }</pre>
 * </blockquote>
 * 以下是一个更复杂的示例，带有模式格式：
 * <blockquote>
 * <pre>{@code
 * double[] filelimits = {0,1,2};
 * String[] filepart = {"are no files","is one file","are {2} files"};
 * ChoiceFormat fileform = new ChoiceFormat(filelimits, filepart);
 * Format[] testFormats = {fileform, null, NumberFormat.getInstance()};
 * MessageFormat pattform = new MessageFormat("There {0} on {1}");
 * pattform.setFormats(testFormats);
 * Object[] testArgs = {null, "ADisk", null};
 * for (int i = 0; i < 4; ++i) {
 *     testArgs[0] = new Integer(i);
 *     testArgs[2] = testArgs[0];
 *     System.out.println(pattform.format(testArgs));
 * }
 * }</pre>
 * </blockquote>
 * <p>
 * 为 ChoiceFormat 对象指定模式相对简单。例如：
 * <blockquote>
 * <pre>{@code
 * ChoiceFormat fmt = new ChoiceFormat(
 *      "-1#is negative| 0#is zero or fraction | 1#is one |1.0<is 1+ |2#is two |2<is more than 2.");
 * System.out.println("Formatter Pattern : " + fmt.toPattern());
 *
 * System.out.println("Format with -INF : " + fmt.format(Double.NEGATIVE_INFINITY));
 * System.out.println("Format with -1.0 : " + fmt.format(-1.0));
 * System.out.println("Format with 0 : " + fmt.format(0));
 * System.out.println("Format with 0.9 : " + fmt.format(0.9));
 * System.out.println("Format with 1.0 : " + fmt.format(1));
 * System.out.println("Format with 1.5 : " + fmt.format(1.5));
 * System.out.println("Format with 2 : " + fmt.format(2));
 * System.out.println("Format with 2.1 : " + fmt.format(2.1));
 * System.out.println("Format with NaN : " + fmt.format(Double.NaN));
 * System.out.println("Format with +INF : " + fmt.format(Double.POSITIVE_INFINITY));
 * }</pre>
 * </blockquote>
 * 输出结果如下：
 * <blockquote>
 * <pre>{@code
 * Format with -INF : is negative
 * Format with -1.0 : is negative
 * Format with 0 : is zero or fraction
 * Format with 0.9 : is zero or fraction
 * Format with 1.0 : is one
 * Format with 1.5 : is 1+
 * Format with 2 : is two
 * Format with 2.1 : is more than 2.
 * Format with NaN : is negative
 * Format with +INF : is more than 2.
 * }</pre>
 * </blockquote>
 *
 * <h3><a name="synchronization">同步</a></h3>
 *
 * <p>
 * ChoiceFormat 不是同步的。建议为每个线程创建单独的格式实例。如果多个线程同时访问一个格式，必须在外部进行同步。
 *
 *
 * @see          DecimalFormat
 * @see          MessageFormat
 * @author       Mark Davis
 */
public class ChoiceFormat extends NumberFormat {

    // 声明与 1.1 FCS 兼容
    private static final long serialVersionUID = 1795184449645032964L;

    /**
     * 设置模式。
     * @param newPattern 请参阅类描述。
     */
    public void applyPattern(String newPattern) {
        StringBuffer[] segments = new StringBuffer[2];
        for (int i = 0; i < segments.length; ++i) {
            segments[i] = new StringBuffer();
        }
        double[] newChoiceLimits = new double[30];
        String[] newChoiceFormats = new String[30];
        int count = 0;
        int part = 0;
        double startValue = 0;
        double oldStartValue = Double.NaN;
        boolean inQuote = false;
        for (int i = 0; i < newPattern.length(); ++i) {
            char ch = newPattern.charAt(i);
            if (ch=='\'') {
                // 检查 "''" 表示一个字面量引号
                if ((i+1)<newPattern.length() && newPattern.charAt(i+1)==ch) {
                    segments[part].append(ch);
                    ++i;
                } else {
                    inQuote = !inQuote;
                }
            } else if (inQuote) {
                segments[part].append(ch);
            } else if (ch == '<' || ch == '#' || ch == '\u2264') {
                if (segments[0].length() == 0) {
                    throw new IllegalArgumentException();
                }
                try {
                    String tempBuffer = segments[0].toString();
                    if (tempBuffer.equals("\u221E")) {
                        startValue = Double.POSITIVE_INFINITY;
                    } else if (tempBuffer.equals("-\u221E")) {
                        startValue = Double.NEGATIVE_INFINITY;
                    } else {
                        startValue = Double.valueOf(segments[0].toString()).doubleValue();
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException();
                }
                if (ch == '<' && startValue != Double.POSITIVE_INFINITY &&
                        startValue != Double.NEGATIVE_INFINITY) {
                    startValue = nextDouble(startValue);
                }
                if (startValue <= oldStartValue) {
                    throw new IllegalArgumentException();
                }
                segments[0].setLength(0);
                part = 1;
            } else if (ch == '|') {
                if (count == newChoiceLimits.length) {
                    newChoiceLimits = doubleArraySize(newChoiceLimits);
                    newChoiceFormats = doubleArraySize(newChoiceFormats);
                }
                newChoiceLimits[count] = startValue;
                newChoiceFormats[count] = segments[1].toString();
                ++count;
                oldStartValue = startValue;
                segments[1].setLength(0);
                part = 0;
            } else {
                segments[part].append(ch);
            }
        }
        // 清理最后一个
        if (part == 1) {
            if (count == newChoiceLimits.length) {
                newChoiceLimits = doubleArraySize(newChoiceLimits);
                newChoiceFormats = doubleArraySize(newChoiceFormats);
            }
            newChoiceLimits[count] = startValue;
            newChoiceFormats[count] = segments[1].toString();
            ++count;
        }
        choiceLimits = new double[count];
        System.arraycopy(newChoiceLimits, 0, choiceLimits, 0, count);
        choiceFormats = new String[count];
        System.arraycopy(newChoiceFormats, 0, choiceFormats, 0, count);
    }

    /**
     * 获取模式。
     *
     * @return 模式字符串
     */
    public String toPattern() {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < choiceLimits.length; ++i) {
            if (i != 0) {
                result.append('|');
            }
            // 选择精度较低的
            // 通过选择最接近整数的值来近似。可以做得更好，但不值得。
            double less = previousDouble(choiceLimits[i]);
            double tryLessOrEqual = Math.abs(Math.IEEEremainder(choiceLimits[i], 1.0d));
            double tryLess = Math.abs(Math.IEEEremainder(less, 1.0d));

            if (tryLessOrEqual < tryLess) {
                result.append(""+choiceLimits[i]);
                result.append('#');
            } else {
                if (choiceLimits[i] == Double.POSITIVE_INFINITY) {
                    result.append("\u221E");
                } else if (choiceLimits[i] == Double.NEGATIVE_INFINITY) {
                    result.append("-\u221E");
                } else {
                    result.append(""+less);
                }
                result.append('<');
            }
            // 追加 choiceFormats[i]，如果包含特殊字符则使用引号。
            // 单引号本身必须在任何情况下都转义。
            String text = choiceFormats[i];
            boolean needQuote = text.indexOf('<') >= 0
                || text.indexOf('#') >= 0
                || text.indexOf('\u2264') >= 0
                || text.indexOf('|') >= 0;
            if (needQuote) result.append('\'');
            if (text.indexOf('\'') < 0) result.append(text);
            else {
                for (int j=0; j<text.length(); ++j) {
                    char c = text.charAt(j);
                    result.append(c);
                    if (c == '\'') result.append(c);
                }
            }
            if (needQuote) result.append('\'');
        }
        return result.toString();
    }

    /**
     * 基于模式构造，设置限制和相应的格式。
     *
     * @param newPattern 新的模式字符串
     * @see #applyPattern
     */
    public ChoiceFormat(String newPattern)  {
        applyPattern(newPattern);
    }

    /**
     * 基于限制和相应的格式构造。
     *
     * @param limits 升序的限制
     * @param formats 对应的格式字符串
     * @see #setChoices
     */
    public ChoiceFormat(double[] limits, String[] formats) {
        setChoices(limits, formats);
    }

    /**
     * 设置格式化中使用的选项。
     * @param limits 包含您希望解析的格式的最高值，应按升序排序。当格式化 X 时，选择将是 i，其中
     * limit[i] &le; X {@literal <} limit[i+1]。
     * 如果 limit 数组不是升序的，格式化的结果将是不正确的。
     * @param formats 是您希望为每个限制使用的格式。它们可以是 Format 对象或字符串。
     * 当使用对象 Y 进行格式化时，
     * 如果对象是 NumberFormat，则调用 ((NumberFormat) Y).format(X)。否则调用 Y.toString()。
     */
    public void setChoices(double[] limits, String formats[]) {
        if (limits.length != formats.length) {
            throw new IllegalArgumentException(
                "Array and limit arrays must be of the same length.");
        }
        choiceLimits = Arrays.copyOf(limits, limits.length);
        choiceFormats = Arrays.copyOf(formats, formats.length);
    }

    /**
     * 获取构造函数中传递的限制。
     * @return 限制。
     */
    public double[] getLimits() {
        double[] newLimits = Arrays.copyOf(choiceLimits, choiceLimits.length);
        return newLimits;
    }

    /**
     * 获取构造函数中传递的格式。
     * @return 格式。
     */
    public Object[] getFormats() {
        Object[] newFormats = Arrays.copyOf(choiceFormats, choiceFormats.length);
        return newFormats;
    }

    // 重写

    /**
     * 格式化的特化。此方法实际上调用
     * <code>format(double, StringBuffer, FieldPosition)</code>
     * 因此支持的 long 范围仅等于 double 可存储的范围。这永远不会是一个实际的限制。
     */
    public StringBuffer format(long number, StringBuffer toAppendTo,
                               FieldPosition status) {
        return format((double)number, toAppendTo, status);
    }

    /**
     * 返回带有格式化的双精度浮点数的模式。
     * @param number 要格式化和替换的数字。
     * @param toAppendTo 追加文本的位置。
     * @param status 忽略，不返回有用的状态。
     */
   public StringBuffer format(double number, StringBuffer toAppendTo,
                               FieldPosition status) {
        // 查找数字
        int i;
        for (i = 0; i < choiceLimits.length; ++i) {
            if (!(number >= choiceLimits[i])) {
                // 与 number < choiceLimits 相同，但捕获 NaN
                break;
            }
        }
        --i;
        if (i < 0) i = 0;
        // 返回格式化的数字或字符串
        return toAppendTo.append(choiceFormats[i]);
    }


                /**
     * 从输入文本中解析一个数字。
     * @param text 源文本。
     * @param status 输入-输出参数。输入时，status.index 字段指示应解析的源文本的第一个字符。退出时，如果没有发生错误，status.index 被设置为源文本中第一个未解析的字符。退出时，如果发生错误，status.index 保持不变，status.errorIndex 被设置为导致解析失败的第一个字符的索引。
     * @return 一个表示解析数字值的 Number。
     */
    public Number parse(String text, ParsePosition status) {
        // 找到最佳数字（定义为最长解析的数字）
        int start = status.index;
        int furthest = start;
        double bestNumber = Double.NaN;
        double tempNumber = 0.0;
        for (int i = 0; i < choiceFormats.length; ++i) {
            String tempString = choiceFormats[i];
            if (text.regionMatches(start, tempString, 0, tempString.length())) {
                status.index = start + tempString.length();
                tempNumber = choiceLimits[i];
                if (status.index > furthest) {
                    furthest = status.index;
                    bestNumber = tempNumber;
                    if (furthest == text.length()) break;
                }
            }
        }
        status.index = furthest;
        if (status.index == start) {
            status.errorIndex = furthest;
        }
        return new Double(bestNumber);
    }

    /**
     * 查找大于 {@code d} 的最小 double。
     * 如果是 {@code NaN}，返回相同的值。
     * <p>用于创建半开区间。
     *
     * @param d 参考值
     * @return 大于 {@code d} 的最小 double 值
     * @see #previousDouble
     */
    public static final double nextDouble (double d) {
        return nextDouble(d, true);
    }

    /**
     * 查找小于 {@code d} 的最大 double。
     * 如果是 {@code NaN}，返回相同的值。
     *
     * @param d 参考值
     * @return 小于 {@code d} 的最大 double 值
     * @see #nextDouble
     */
    public static final double previousDouble (double d) {
        return nextDouble(d, false);
    }

    /**
     * 覆盖 Cloneable
     */
    public Object clone()
    {
        ChoiceFormat other = (ChoiceFormat) super.clone();
        // 对于基本类型或不可变类型，浅克隆就足够了
        other.choiceLimits = choiceLimits.clone();
        other.choiceFormats = choiceFormats.clone();
        return other;
    }

    /**
     * 为消息格式对象生成哈希码。
     */
    public int hashCode() {
        int result = choiceLimits.length;
        if (choiceFormats.length > 0) {
            // 足够用于合理的分布
            result ^= choiceFormats[choiceFormats.length-1].hashCode();
        }
        return result;
    }

    /**
     * 两个对象之间的相等性比较。
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj)                      // 快速检查
            return true;
        if (getClass() != obj.getClass())
            return false;
        ChoiceFormat other = (ChoiceFormat) obj;
        return (Arrays.equals(choiceLimits, other.choiceLimits)
             && Arrays.equals(choiceFormats, other.choiceFormats));
    }

    /**
     * 从输入流中读取对象后，进行简单的验证以维护类的不变性。
     * @throws InvalidObjectException 如果从流中读取的对象无效。
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (choiceLimits.length != choiceFormats.length) {
            throw new InvalidObjectException(
                    "limits and format arrays of different length.");
        }
    }

    // ===============privates===========================

    /**
     * 选择的下限列表。格式化程序将返回 <code>choiceFormats[i]</code>，如果正在格式化的数字大于或等于 <code>choiceLimits[i]</code> 且小于 <code>choiceLimits[i+1]</code>。
     * @serial
     */
    private double[] choiceLimits;

    /**
     * 选择字符串列表。格式化程序将返回 <code>choiceFormats[i]</code>，如果正在格式化的数字大于或等于 <code>choiceLimits[i]</code> 且小于 <code>choiceLimits[i+1]</code>。
     * @serial
     */
    private String[] choiceFormats;

    /*
    static final long SIGN          = 0x8000000000000000L;
    static final long EXPONENT      = 0x7FF0000000000000L;
    static final long SIGNIFICAND   = 0x000FFFFFFFFFFFFFL;

    private static double nextDouble (double d, boolean positive) {
        if (Double.isNaN(d) || Double.isInfinite(d)) {
                return d;
            }
        long bits = Double.doubleToLongBits(d);
        long significand = bits & SIGNIFICAND;
        if (bits < 0) {
            significand |= (SIGN | EXPONENT);
        }
        long exponent = bits & EXPONENT;
        if (positive) {
            significand += 1;
            // FIXME fix overflow & underflow
        } else {
            significand -= 1;
            // FIXME fix overflow & underflow
        }
        bits = exponent | (significand & ~EXPONENT);
        return Double.longBitsToDouble(bits);
    }
    */

    static final long SIGN                = 0x8000000000000000L;
    static final long EXPONENT            = 0x7FF0000000000000L;
    static final long POSITIVEINFINITY    = 0x7FF0000000000000L;

    /**
     * 查找大于 {@code d} 的最小 double（如果 {@code positive} 为 {@code true}），或小于 {@code d} 的最大 double（如果 {@code positive} 为 {@code false}）。
     * 如果是 {@code NaN}，返回相同的值。
     *
     * 不影响浮点标志，前提是这些成员函数也不影响：
     *          Double.longBitsToDouble(long)
     *          Double.doubleToLongBits(double)
     *          Double.isNaN(double)
     *
     * @param d        参考值
     * @param positive {@code true} 如果需要最小 double；否则为 {@code false}
     * @return 最小或更大的 double 值
     */
    public static double nextDouble (double d, boolean positive) {

        /* 过滤掉 NaN */
        if (Double.isNaN(d)) {
            return d;
        }

        /* 零也是一个特殊情况 */
        if (d == 0.0) {
            double smallestPositiveDouble = Double.longBitsToDouble(1L);
            if (positive) {
                return smallestPositiveDouble;
            } else {
                return -smallestPositiveDouble;
            }
        }

        /* 如果进入这里，d 是一个非零值 */

        /* 将所有位保存在一个 long 中以备后用 */
        long bits = Double.doubleToLongBits(d);

        /* 去掉符号位 */
        long magnitude = bits & ~SIGN;

        /* 如果下一个 double 远离零，增加幅度 */
        if ((bits > 0) == positive) {
            if (magnitude != POSITIVEINFINITY) {
                magnitude += 1;
            }
        }
        /* 否则减少幅度 */
        else {
            magnitude -= 1;
        }

        /* 恢复符号位并返回 */
        long signbit = bits & SIGN;
        return Double.longBitsToDouble (magnitude | signbit);
    }

    private static double[] doubleArraySize(double[] array) {
        int oldSize = array.length;
        double[] newArray = new double[oldSize * 2];
        System.arraycopy(array, 0, newArray, 0, oldSize);
        return newArray;
    }

    private String[] doubleArraySize(String[] array) {
        int oldSize = array.length;
        String[] newArray = new String[oldSize * 2];
        System.arraycopy(array, 0, newArray, 0, oldSize);
        return newArray;
    }

}
