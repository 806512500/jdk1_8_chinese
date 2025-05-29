/*
 * 版权所有 (c) 1995, 2008, Oracle和/或其附属公司。保留所有权利。
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

/**
 * 当线程正在等待、睡眠或以其他方式占用时，如果线程在活动开始之前或期间被中断，则抛出此异常。
 * 有时，方法可能希望测试当前线程是否已被中断，如果是，则立即抛出此异常。以下代码可用于实现此效果：
 * <pre>
 *  if (Thread.interrupted())  // 清除中断状态！
 *      throw new InterruptedException();
 * </pre>
 *
 * @author  Frank Yellin
 * @see     java.lang.Object#wait()
 * @see     java.lang.Object#wait(long)
 * @see     java.lang.Object#wait(long, int)
 * @see     java.lang.Thread#sleep(long)
 * @see     java.lang.Thread#interrupt()
 * @see     java.lang.Thread#interrupted()
 * @since   JDK1.0
 */
public
class InterruptedException extends Exception {
    private static final long serialVersionUID = 6700697376100628473L;

    /**
     * 构造一个没有详细消息的<code>InterruptedException</code>。
     */
    public InterruptedException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个<code>InterruptedException</code>。
     *
     * @param   s   详细消息。
     */
    public InterruptedException(String s) {
        super(s);
    }
}
