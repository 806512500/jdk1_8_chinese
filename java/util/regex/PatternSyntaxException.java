/*
 * 版权所有 (c) 1999, 2018, Oracle 和/或其附属公司。保留所有权利。
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

package java.util.regex;

import sun.security.action.GetPropertyAction;


/**
 * 用于指示正则表达式模式中语法错误的未检查异常。
 *
 * @author 未署名
 * @since 1.4
 * @spec JSR-51
 */

public class PatternSyntaxException
    extends IllegalArgumentException
{
    private static final long serialVersionUID = -3864639126226059218L;

    private final String desc;
    private final String pattern;
    private final int index;

    /**
     * 构造此类的新实例。
     *
     * @param  desc
     *         错误的描述
     *
     * @param  regex
     *         错误的模式
     *
     * @param  index
     *         模式中错误的大致索引，
     *         或 <tt>-1</tt> 如果索引未知
     */
    public PatternSyntaxException(String desc, String regex, int index) {
        this.desc = desc;
        this.pattern = regex;
        this.index = index;
    }

    /**
     * 获取错误索引。
     *
     * @return  模式中错误的大致索引，
     *         或 <tt>-1</tt> 如果索引未知
     */
    public int getIndex() {
        return index;
    }

    /**
     * 获取错误的描述。
     *
     * @return  错误的描述
     */
    public String getDescription() {
        return desc;
    }

    /**
     * 获取错误的正则表达式模式。
     *
     * @return  错误的模式
     */
    public String getPattern() {
        return pattern;
    }

    private static final String nl =
        java.security.AccessController
            .doPrivileged(new GetPropertyAction("line.separator"));

    /**
     * 返回包含语法错误描述及其索引、错误的正则表达式模式以及模式中错误索引的视觉指示的多行字符串。
     *
     * @return  完整的详细消息
     */
    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(desc);
        if (index >= 0) {
            sb.append(" near index ");
            sb.append(index);
        }
        sb.append(nl);
        sb.append(pattern);
        if (index >= 0 && pattern != null && index < pattern.length()) {
            sb.append(nl);
            for (int i = 0; i < index; i++) sb.append(' ');
            sb.append('^');
        }
        return sb.toString();
    }

}
