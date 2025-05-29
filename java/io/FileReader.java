/*
 * 版权所有 (c) 1996, 2001, Oracle 和/或其附属公司。保留所有权利。
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
 * 用于读取字符文件的便捷类。此类的构造函数假设默认字符编码和默认字节缓冲区大小是合适的。如果要指定这些值，请使用 FileInputStream 构造一个 InputStreamReader。
 *
 * <p><code>FileReader</code> 用于读取字符流。对于读取原始字节流，请考虑使用 <code>FileInputStream</code>。
 *
 * @see InputStreamReader
 * @see FileInputStream
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */
public class FileReader extends InputStreamReader {

   /**
    * 给定要读取的文件名，创建一个新的 <tt>FileReader</tt>。
    *
    * @param fileName 要读取的文件名
    * @exception  FileNotFoundException  如果命名的文件不存在，是目录而不是普通文件，或由于其他原因无法打开以进行读取。
    */
    public FileReader(String fileName) throws FileNotFoundException {
        super(new FileInputStream(fileName));
    }

   /**
    * 给定要读取的 <tt>File</tt>，创建一个新的 <tt>FileReader</tt>。
    *
    * @param file 要读取的 <tt>File</tt>
    * @exception  FileNotFoundException  如果文件不存在，是目录而不是普通文件，或由于其他原因无法打开以进行读取。
    */
    public FileReader(File file) throws FileNotFoundException {
        super(new FileInputStream(file));
    }

   /**
    * 给定要读取的 <tt>FileDescriptor</tt>，创建一个新的 <tt>FileReader</tt>。
    *
    * @param fd 要读取的 FileDescriptor
    */
    public FileReader(FileDescriptor fd) {
        super(new FileInputStream(fd));
    }

}
