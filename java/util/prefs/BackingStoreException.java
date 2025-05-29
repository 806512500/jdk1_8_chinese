/*
 * 版权所有 (c) 2000, 2003, Oracle 和/或其附属公司。保留所有权利。
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

package java.util.prefs;

import java.io.NotSerializableException;

/**
 * 抛出此异常表示由于后端存储的失败，或者无法联系到后端存储，导致首选项操作无法完成。
 *
 * @author  Josh Bloch
 * @since   1.4
 */
public class BackingStoreException extends Exception {
    /**
     * 使用指定的详细消息构造 BackingStoreException。
     *
     * @param s 详细消息。
     */
    public BackingStoreException(String s) {
        super(s);
    }

    /**
     * 使用指定的原因构造 BackingStoreException。
     *
     * @param cause 原因
     */
    public BackingStoreException(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = 859796500401108469L;
}
