/*
 * 版权所有 (c) 1995, 2008, Oracle 和/或其附属公司。保留所有权利。
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
 * 表示在数据输入流或实现数据输入接口的任何类中读取的修改后的 UTF-8 格式的字符串格式不正确。
 * 请参阅 <a href="DataInput.html#modified-utf-8"><code>DataInput</code></a>
 * 类描述，了解修改后的 UTF-8 字符串的读写格式。
 *
 * @author  Frank Yellin
 * @see     java.io.DataInput
 * @see     java.io.DataInputStream#readUTF(java.io.DataInput)
 * @see     java.io.IOException
 * @since   JDK1.0
 */
public
class UTFDataFormatException extends IOException {
    private static final long serialVersionUID = 420743449228280612L;

    /**
     * 构造一个 <code>UTFDataFormatException</code>，其错误详细信息消息为 <code>null</code>。
     */
    public UTFDataFormatException() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 <code>UTFDataFormatException</code>。字符串 <code>s</code> 可以通过
     * <code>{@link java.lang.Throwable#getMessage}</code>
     * 类 <code>java.lang.Throwable</code> 的方法稍后检索。
     *
     * @param   s   详细信息消息。
     */
    public UTFDataFormatException(String s) {
        super(s);
    }
}
