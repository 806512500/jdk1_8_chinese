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
 * 用于写入字符文件的便捷类。此类的构造函数假设默认字符编码和默认字节缓冲区大小是可以接受的。要指定这些值，请在 FileOutputStream 上构造一个 OutputStreamWriter。
 *
 * <p>文件是否可用或可创建取决于底层平台。特别是，某些平台允许文件仅由一个 <tt>FileWriter</tt>（或其他文件写入对象）一次打开写入。在这些情况下，如果涉及的文件已经打开，此类中的构造函数将失败。
 *
 * <p><code>FileWriter</code> 用于写入字符流。对于写入原始字节流，请考虑使用 <code>FileOutputStream</code>。
 *
 * @see OutputStreamWriter
 * @see FileOutputStream
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public class FileWriter extends OutputStreamWriter {

    /**
     * 根据文件名构造一个 FileWriter 对象。
     *
     * @param fileName  String 系统依赖的文件名。
     * @throws IOException  如果命名的文件存在但不是常规文件，或者不存在但无法创建，或者由于任何其他原因无法打开。
     */
    public FileWriter(String fileName) throws IOException {
        super(new FileOutputStream(fileName));
    }

    /**
     * 根据文件名和一个布尔值构造一个 FileWriter 对象，指示是否追加写入的数据。
     *
     * @param fileName  String 系统依赖的文件名。
     * @param append    boolean 如果 <code>true</code>，则数据将写入文件末尾而不是开头。
     * @throws IOException  如果命名的文件存在但不是常规文件，或者不存在但无法创建，或者由于任何其他原因无法打开。
     */
    public FileWriter(String fileName, boolean append) throws IOException {
        super(new FileOutputStream(fileName, append));
    }

    /**
     * 根据 File 对象构造一个 FileWriter 对象。
     *
     * @param file  要写入的 File 对象。
     * @throws IOException  如果文件存在但不是常规文件，或者不存在但无法创建，或者由于任何其他原因无法打开。
     */
    public FileWriter(File file) throws IOException {
        super(new FileOutputStream(file));
    }

    /**
     * 根据 File 对象构造一个 FileWriter 对象。如果第二个参数为 <code>true</code>，则字节将写入文件末尾而不是开头。
     *
     * @param file  要写入的 File 对象
     * @param     append    如果 <code>true</code>，则字节将写入文件末尾而不是开头
     * @throws IOException  如果文件存在但不是常规文件，或者不存在但无法创建，或者由于任何其他原因无法打开
     * @since 1.4
     */
    public FileWriter(File file, boolean append) throws IOException {
        super(new FileOutputStream(file, append));
    }

    /**
     * 构造一个与文件描述符关联的 FileWriter 对象。
     *
     * @param fd  要写入的 FileDescriptor 对象。
     */
    public FileWriter(FileDescriptor fd) {
        super(new FileOutputStream(fd));
    }

}
