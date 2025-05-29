
/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 将 LogRecord 格式化为标准的 XML 格式。
 * <p>
 * DTD 规范作为 Java 日志记录 API 规范的附录 A 提供。
 * <p>
 * XMLFormatter 可以与任意字符编码一起使用，但通常建议使用 UTF-8。可以在输出处理程序上设置字符编码。
 *
 * @since 1.4
 */

public class XMLFormatter extends Formatter {
    private LogManager manager = LogManager.getLogManager();

    // 追加两位数字。
    private void a2(StringBuilder sb, int x) {
        if (x < 10) {
            sb.append('0');
        }
        sb.append(x);
    }

    // 以 ISO 8601 格式追加时间和日期
    private void appendISO8601(StringBuilder sb, long millis) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(millis);
        sb.append(cal.get(Calendar.YEAR));
        sb.append('-');
        a2(sb, cal.get(Calendar.MONTH) + 1);
        sb.append('-');
        a2(sb, cal.get(Calendar.DAY_OF_MONTH));
        sb.append('T');
        a2(sb, cal.get(Calendar.HOUR_OF_DAY));
        sb.append(':');
        a2(sb, cal.get(Calendar.MINUTE));
        sb.append(':');
        a2(sb, cal.get(Calendar.SECOND));
    }

    // 追加给定文本字符串的转义版本，其中 XML 特殊字符已被转义。
    // 对于 null 字符串，我们追加 "<null>"
    private void escape(StringBuilder sb, String text) {
        if (text == null) {
            text = "<null>";
        }
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '<') {
                sb.append("&lt;");
            } else if (ch == '>') {
                sb.append("&gt;");
            } else if (ch == '&') {
                sb.append("&amp;");
            } else {
                sb.append(ch);
            }
        }
    }

    /**
     * 将给定的消息格式化为 XML。
     * <p>
     * 该方法可以在子类中重写。
     * 建议使用 {@link Formatter#formatMessage} 方便方法来本地化和格式化消息字段。
     *
     * @param record 要格式化的日志记录。
     * @return 格式化的日志记录
     */
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<record>\n");

        sb.append("  <date>");
        appendISO8601(sb, record.getMillis());
        sb.append("</date>\n");

        sb.append("  <millis>");
        sb.append(record.getMillis());
        sb.append("</millis>\n");

        sb.append("  <sequence>");
        sb.append(record.getSequenceNumber());
        sb.append("</sequence>\n");

        String name = record.getLoggerName();
        if (name != null) {
            sb.append("  <logger>");
            escape(sb, name);
            sb.append("</logger>\n");
        }

        sb.append("  <level>");
        escape(sb, record.getLevel().toString());
        sb.append("</level>\n");

        if (record.getSourceClassName() != null) {
            sb.append("  <class>");
            escape(sb, record.getSourceClassName());
            sb.append("</class>\n");
        }

        if (record.getSourceMethodName() != null) {
            sb.append("  <method>");
            escape(sb, record.getSourceMethodName());
            sb.append("</method>\n");
        }

        sb.append("  <thread>");
        sb.append(record.getThreadID());
        sb.append("</thread>\n");

        if (record.getMessage() != null) {
            // 格式化消息字符串及其伴随的参数。
            String message = formatMessage(record);
            sb.append("  <message>");
            escape(sb, message);
            sb.append("</message>");
            sb.append("\n");
        }

        // 如果消息正在本地化，输出键、资源包名称和参数。
        ResourceBundle bundle = record.getResourceBundle();
        try {
            if (bundle != null && bundle.getString(record.getMessage()) != null) {
                sb.append("  <key>");
                escape(sb, record.getMessage());
                sb.append("</key>\n");
                sb.append("  <catalog>");
                escape(sb, record.getResourceBundleName());
                sb.append("</catalog>\n");
            }
        } catch (Exception ex) {
            // 消息不在目录中。继续。
        }

        Object parameters[] = record.getParameters();
        //  检查参数是否不是消息文本格式，或不为 null 或为空
        if ( parameters != null && parameters.length != 0
                && record.getMessage().indexOf("{") == -1 ) {
            for (int i = 0; i < parameters.length; i++) {
                sb.append("  <param>");
                try {
                    escape(sb, parameters[i].toString());
                } catch (Exception ex) {
                    sb.append("???");
                }
                sb.append("</param>\n");
            }
        }

        if (record.getThrown() != null) {
            // 报告可抛出对象的状态。
            Throwable th = record.getThrown();
            sb.append("  <exception>\n");
            sb.append("    <message>");
            escape(sb, th.toString());
            sb.append("</message>\n");
            StackTraceElement trace[] = th.getStackTrace();
            for (int i = 0; i < trace.length; i++) {
                StackTraceElement frame = trace[i];
                sb.append("    <frame>\n");
                sb.append("      <class>");
                escape(sb, frame.getClassName());
                sb.append("</class>\n");
                sb.append("      <method>");
                escape(sb, frame.getMethodName());
                sb.append("</method>\n");
                // 检查行号。
                if (frame.getLineNumber() >= 0) {
                    sb.append("      <line>");
                    sb.append(frame.getLineNumber());
                    sb.append("</line>\n");
                }
                sb.append("    </frame>\n");
            }
            sb.append("  </exception>\n");
        }


                    sb.append("</record>\n");
        return sb.toString();
    }

    /**
     * 返回一组 XML 格式记录的头字符串。
     *
     * @param   h  目标处理程序（可以为 null）
     * @return  有效的 XML 字符串
     */
    public String getHead(Handler h) {
        StringBuilder sb = new StringBuilder();
        String encoding;
        sb.append("<?xml version=\"1.0\"");

        if (h != null) {
            encoding = h.getEncoding();
        } else {
            encoding = null;
        }

        if (encoding == null) {
            // 确定默认编码。
            encoding = java.nio.charset.Charset.defaultCharset().name();
        }
        // 尝试将编码名称映射到规范名称。
        try {
            Charset cs = Charset.forName(encoding);
            encoding = cs.name();
        } catch (Exception ex) {
            // 在查找规范名称时遇到问题。
            // 只使用原始编码名称。
        }

        sb.append(" encoding=\"");
        sb.append(encoding);
        sb.append("\"");
        sb.append(" standalone=\"no\"?>\n");
        sb.append("<!DOCTYPE log SYSTEM \"logger.dtd\">\n");
        sb.append("<log>\n");
        return sb.toString();
    }

    /**
     * 返回一组 XML 格式记录的尾字符串。
     *
     * @param   h  目标处理程序（可以为 null）
     * @return  有效的 XML 字符串
     */
    public String getTail(Handler h) {
        return "</log>\n";
    }
}
