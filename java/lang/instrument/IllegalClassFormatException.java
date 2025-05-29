/*
 * 版权所有 (c) 2003, 2008, Oracle 和/或其关联公司。保留所有权利。
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

package java.lang.instrument;

/*
 * 版权所有 2003 Wily Technology, Inc.
 */

/**
 * 当 {@link java.lang.instrument.ClassFileTransformer#transform ClassFileTransformer.transform} 的输入参数无效时抛出。
 * 这可能是因为初始类文件字节无效，或者先前应用的转换破坏了字节。
 *
 * @see     java.lang.instrument.ClassFileTransformer#transform
 * @since   1.5
 */
public class IllegalClassFormatException extends Exception {
    private static final long serialVersionUID = -3841736710924794009L;

    /**
     * 构造一个没有详细消息的 <code>IllegalClassFormatException</code>。
     */
    public
    IllegalClassFormatException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>IllegalClassFormatException</code>。
     *
     * @param   s   详细消息。
     */
    public
    IllegalClassFormatException(String s) {
        super(s);
    }
}
