/*
 * 版权所有 (c) 1996, 2005, Oracle 和/或其附属公司。保留所有权利。
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

package java.io;

/**
 * 当从对象流中读取的控制信息违反内部一致性检查时抛出。
 *
 * @author  未署名
 * @since   JDK1.1
 */
public class StreamCorruptedException extends ObjectStreamException {

    private static final long serialVersionUID = 8983558202217591746L;

    /**
     * 创建一个 StreamCorruptedException 并列出抛出的原因。
     *
     * @param reason  描述异常原因的字符串。
     */
    public StreamCorruptedException(String reason) {
        super(reason);
    }

    /**
     * 创建一个 StreamCorruptedException 并不列出抛出的原因。
     */
    public StreamCorruptedException() {
        super();
    }
}
