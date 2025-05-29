/*
 * 版权所有 (c) 1994, 2008, Oracle 和/或其附属公司。保留所有权利。
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
 * 表示尝试打开由指定路径名表示的文件失败。
 *
 * <p> 当指定路径名的文件不存在时，此异常将由 {@link FileInputStream}、{@link
 * FileOutputStream} 和 {@link RandomAccessFile} 构造函数抛出。当文件确实存在但因某种原因无法访问时，例如尝试打开只读文件进行写入时，这些构造函数也会抛出此异常。
 *
 * @author  未署名
 * @since   JDK1.0
 */

public class FileNotFoundException extends IOException {
    private static final long serialVersionUID = -897856973823710492L;

    /**
     * 构造一个带有 <code>null</code> 作为错误详细信息消息的 <code>FileNotFoundException</code>。
     */
    public FileNotFoundException() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 <code>FileNotFoundException</code>。字符串 <code>s</code> 可以通过
     * <code>{@link java.lang.Throwable#getMessage}</code>
     * 类 <code>java.lang.Throwable</code> 的方法稍后检索。
     *
     * @param   s   详细信息消息。
     */
    public FileNotFoundException(String s) {
        super(s);
    }

    /**
     * 使用给定的路径名字符串和给定的原因字符串构造一个 <code>FileNotFoundException</code>，详细信息消息由这两部分组成。如果 <code>reason</code> 参数为 <code>null</code>，则将被省略。此私有构造函数仅由本机 I/O 方法调用。
     *
     * @since 1.2
     */
    private FileNotFoundException(String path, String reason) {
        super(path + ((reason == null)
                      ? ""
                      : " (" + reason + ")"));
    }

}
