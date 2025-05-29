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
 * 用于写入过滤字符流的抽象类。
 * 抽象类 <code>FilterWriter</code> 本身
 * 提供了将所有请求传递给
 * 包含的流的默认方法。<code>FilterWriter</code>
 * 的子类应覆盖其中一些方法，并且还可以
 * 提供额外的方法和字段。
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public abstract class FilterWriter extends Writer {

    /**
     * 底层字符输出流。
     */
    protected Writer out;

    /**
     * 创建一个新的过滤写入器。
     *
     * @param out  提供底层流的 Writer 对象。
     * @throws NullPointerException 如果 <code>out</code> 为 <code>null</code>
     */
    protected FilterWriter(Writer out) {
        super(out);
        this.out = out;
    }

    /**
     * 写入单个字符。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void write(int c) throws IOException {
        out.write(c);
    }

    /**
     * 写入字符数组的一部分。
     *
     * @param  cbuf  要写入的字符缓冲区
     * @param  off   开始读取字符的偏移量
     * @param  len   要写入的字符数
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void write(char cbuf[], int off, int len) throws IOException {
        out.write(cbuf, off, len);
    }

    /**
     * 写入字符串的一部分。
     *
     * @param  str  要写入的字符串
     * @param  off  开始读取字符的偏移量
     * @param  len  要写入的字符数
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void write(String str, int off, int len) throws IOException {
        out.write(str, off, len);
    }

    /**
     * 刷新流。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void flush() throws IOException {
        out.flush();
    }

    public void close() throws IOException {
        out.close();
    }

}
