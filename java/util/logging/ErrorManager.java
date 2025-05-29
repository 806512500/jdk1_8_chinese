/*
 * 版权所有 (c) 2001, 2004, Oracle 和/或其附属公司。保留所有权利。
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

/**
 * ErrorManager 对象可以附加到 Handlers 以处理在日志记录期间发生的任何错误。
 * <p>
 * 在处理日志输出时，如果 Handler 遇到问题，而不是向发出日志调用的用户（可能不感兴趣）抛出异常，
 * Handler 应该调用其关联的 ErrorManager。
 */

public class ErrorManager {
   private boolean reported = false;

    /*
     * 我们为重要的错误类别声明标准错误代码。
     */

    /**
     * GENERIC_FAILURE 用于不符合其他类别的失败。
     */
    public final static int GENERIC_FAILURE = 0;
    /**
     * WRITE_FAILURE 用于向输出流写入失败的情况。
     */
    public final static int WRITE_FAILURE = 1;
    /**
     * FLUSH_FAILURE 用于向输出流刷新失败的情况。
     */
    public final static int FLUSH_FAILURE = 2;
    /**
     * CLOSE_FAILURE 用于关闭输出流失败的情况。
     */
    public final static int CLOSE_FAILURE = 3;
    /**
     * OPEN_FAILURE 用于打开输出流失败的情况。
     */
    public final static int OPEN_FAILURE = 4;
    /**
     * FORMAT_FAILURE 用于因任何原因格式化失败的情况。
     */
    public final static int FORMAT_FAILURE = 5;

    /**
     * 当 Handler 发生失败时调用 error 方法。
     * <p>
     * 该方法可以在子类中重写。在此基类中的默认行为是第一次调用报告给 System.err，后续调用被忽略。
     *
     * @param msg    描述性字符串（可以为 null）
     * @param ex     一个异常（可以为 null）
     * @param code   在 ErrorManager 中定义的错误代码
     */
    public synchronized void error(String msg, Exception ex, int code) {
        if (reported) {
            // 我们只报告第一次错误，以避免屏幕被阻塞。
            return;
        }
        reported = true;
        String text = "java.util.logging.ErrorManager: " + code;
        if (msg != null) {
            text = text + ": " + msg;
        }
        System.err.println(text);
        if (ex != null) {
            ex.printStackTrace();
        }
    }
}
