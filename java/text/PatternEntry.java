
/*
 * 版权所有 (c) 1996, 2020, Oracle 和/或其附属公司。保留所有权利。
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

/*
 * 版权所有 (C) 1996, 1997 - 保留所有权利
 * 版权所有 (C) 1996, 1997 - 保留所有权利
 *
 *   本源代码和文档的原始版本受版权保护并归 Taligent, Inc. 所有，它是 IBM 的全资子公司。这些材料是根据 Taligent 和 Sun 之间的许可协议提供的。这项技术受到多项美国和国际专利的保护。此通知和对 Taligent 的归属不得移除。
 *   Taligent 是 Taligent, Inc. 的注册商标。
 *
 */

package java.text;

import java.lang.Character;

/**
 * 用于规范化和合并排序模式的工具类。
 * 该类与 MergeCollation 一起使用，用于向现有的规则表中添加模式。
 * @see        MergeCollation
 * @author     Mark Davis, Helena Shih
 */

class PatternEntry {
    /**
     * 获取当前的扩展，带引号
     */
    public void appendQuotedExtension(StringBuffer toAddTo) {
        appendQuoted(extension,toAddTo);
    }

    /**
     * 获取当前的字符，带引号
     */
    public void appendQuotedChars(StringBuffer toAddTo) {
        appendQuoted(chars,toAddTo);
    }

    /**
     * 警告：此方法用于在 Vector 中搜索。
     * 由于 Vector.indexOf 不接受比较器，
     * 此方法定义不明确且忽略强度。
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        PatternEntry other = (PatternEntry) obj;
        boolean result = chars.equals(other.chars);
        return result;
    }

    public int hashCode() {
        return chars.hashCode();
    }

    /**
     * 用于调试。
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        addToBuffer(result, true, false, null);
        return result.toString();
    }

    /**
     * 获取条目的强度。
     */
    final int getStrength() {
        return strength;
    }

    /**
     * 获取条目的扩展字符。
     */
    final String getExtension() {
        return extension;
    }

    /**
     * 获取条目的核心字符。
     */
    final String getChars() {
        return chars;
    }

    // ===== 私有方法 =====

    void addToBuffer(StringBuffer toAddTo,
                     boolean showExtension,
                     boolean showWhiteSpace,
                     PatternEntry lastEntry)
    {
        if (showWhiteSpace && toAddTo.length() > 0)
            if (strength == Collator.PRIMARY || lastEntry != null)
                toAddTo.append('\n');
            else
                toAddTo.append(' ');
        if (lastEntry != null) {
            toAddTo.append('&');
            if (showWhiteSpace)
                toAddTo.append(' ');
            lastEntry.appendQuotedChars(toAddTo);
            appendQuotedExtension(toAddTo);
            if (showWhiteSpace)
                toAddTo.append(' ');
        }
        switch (strength) {
        case Collator.IDENTICAL: toAddTo.append('='); break;
        case Collator.TERTIARY:  toAddTo.append(','); break;
        case Collator.SECONDARY: toAddTo.append(';'); break;
        case Collator.PRIMARY:   toAddTo.append('<'); break;
        case RESET: toAddTo.append('&'); break;
        case UNSET: toAddTo.append('?'); break;
        }
        if (showWhiteSpace)
            toAddTo.append(' ');
        appendQuoted(chars,toAddTo);
        if (showExtension && !extension.isEmpty()) {
            toAddTo.append('/');
            appendQuoted(extension,toAddTo);
        }
    }

    static void appendQuoted(String chars, StringBuffer toAddTo) {
        boolean inQuote = false;
        char ch = chars.charAt(0);
        if (Character.isSpaceChar(ch)) {
            inQuote = true;
            toAddTo.append('\'');
        } else {
          if (PatternEntry.isSpecialChar(ch)) {
                inQuote = true;
                toAddTo.append('\'');
            } else {
                switch (ch) {
                    case 0x0010: case '\f': case '\r':
                    case '\t': case '\n':  case '@':
                    inQuote = true;
                    toAddTo.append('\'');
                    break;
                case '\'':
                    inQuote = true;
                    toAddTo.append('\'');
                    break;
                default:
                    if (inQuote) {
                        inQuote = false; toAddTo.append('\'');
                    }
                    break;
                }
           }
        }
        toAddTo.append(chars);
        if (inQuote)
            toAddTo.append('\'');
    }

    //========================================================================
    // 将模式解析为 PatternEntry 列表......
    //========================================================================

    PatternEntry(int strength,
                 StringBuffer chars,
                 StringBuffer extension)
    {
        this.strength = strength;
        this.chars = chars.toString();
        this.extension = (extension.length() > 0) ? extension.toString()
                                                  : "";
    }

    static class Parser {
        private String pattern;
        private int i;

        public Parser(String pattern) {
            this.pattern = pattern;
            this.i = 0;
        }

        public PatternEntry next() throws ParseException {
            int newStrength = UNSET;

            newChars.setLength(0);
            newExtension.setLength(0);

            boolean inChars = true;
            boolean inQuote = false;
        mainLoop:
            while (i < pattern.length()) {
                char ch = pattern.charAt(i);
                if (inQuote) {
                    if (ch == '\'') {
                        inQuote = false;
                    } else {
                        if (newChars.length() == 0) newChars.append(ch);
                        else if (inChars) newChars.append(ch);
                        else newExtension.append(ch);
                    }
                } else switch (ch) {
                case '=': if (newStrength != UNSET) break mainLoop;
                    newStrength = Collator.IDENTICAL; break;
                case ',': if (newStrength != UNSET) break mainLoop;
                    newStrength = Collator.TERTIARY; break;
                case ';': if (newStrength != UNSET) break mainLoop;
                    newStrength = Collator.SECONDARY; break;
                case '<': if (newStrength != UNSET) break mainLoop;
                    newStrength = Collator.PRIMARY; break;
                case '&': if (newStrength != UNSET) break mainLoop;
                    newStrength = RESET; break;
                case '\t':
                case '\n':
                case '\f':
                case '\r':
                case ' ': break; // 跳过空白 TODO 使用 Character
                case '/': inChars = false; break;
                case '\'':
                    inQuote = true;
                    ch = pattern.charAt(++i);
                    if (newChars.length() == 0) newChars.append(ch);
                    else if (inChars) newChars.append(ch);
                    else newExtension.append(ch);
                    break;
                default:
                    if (newStrength == UNSET) {
                        throw new ParseException
                            ("缺少字符 (=,;<&) : " +
                             pattern.substring(i,
                                (i+10 < pattern.length()) ?
                                 i+10 : pattern.length()),
                             i);
                    }
                    if (PatternEntry.isSpecialChar(ch) && (inQuote == false))
                        throw new ParseException
                            ("未引用的标点字符 : " + Integer.toString(ch, 16), i);
                    if (inChars) {
                        newChars.append(ch);
                    } else {
                        newExtension.append(ch);
                    }
                    break;
                }
                i++;
            }
            if (newStrength == UNSET)
                return null;
            if (newChars.length() == 0) {
                throw new ParseException
                    ("缺少字符 (=,;<&) : " +
                      pattern.substring(i,
                          (i+10 < pattern.length()) ?
                           i+10 : pattern.length()),
                     i);
            }


                        return new PatternEntry(newStrength, newChars, newExtension);
        }

        // 我们重用这些对象以提高性能
        private StringBuffer newChars = new StringBuffer();
        private StringBuffer newExtension = new StringBuffer();

    }

    static boolean isSpecialChar(char ch) {
        return ((ch == '\u0020') ||
                ((ch <= '\u002F') && (ch >= '\u0022')) ||
                ((ch <= '\u003F') && (ch >= '\u003A')) ||
                ((ch <= '\u0060') && (ch >= '\u005B')) ||
                ((ch <= '\u007E') && (ch >= '\u007B')));
    }


    static final int RESET = -2;
    static final int UNSET = -1;

    int strength = UNSET;
    String chars = "";
    String extension = "";
}
