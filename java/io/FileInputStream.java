
/*
 * 版权所有 (c) 1994, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.nio.channels.FileChannel;
import sun.nio.ch.FileChannelImpl;


/**
 * <code>FileInputStream</code> 从文件系统中的文件获取输入字节。可用的文件取决于主机环境。
 *
 * <p><code>FileInputStream</code> 用于读取原始字节流，如图像数据。对于读取字符流，建议使用
 * <code>FileReader</code>。
 *
 * @author  Arthur van Hoff
 * @see     java.io.File
 * @see     java.io.FileDescriptor
 * @see     java.io.FileOutputStream
 * @see     java.nio.file.Files#newInputStream
 * @since   JDK1.0
 */
public
class FileInputStream extends InputStream
{
    /* 文件描述符 - 打开文件的句柄 */
    private final FileDescriptor fd;

    /**
     * 引用文件的路径
     * （如果流是使用文件描述符创建的，则为 null）
     */
    private final String path;

    private FileChannel channel = null;

    private final Object closeLock = new Object();
    private volatile boolean closed = false;

    /**
     * 通过打开与实际文件的连接来创建 <code>FileInputStream</code>，
     * 该文件由文件系统中的路径名 <code>name</code> 命名。创建一个新的 <code>FileDescriptor</code>
     * 对象来表示此文件连接。
     * <p>
     * 首先，如果有安全经理，其 <code>checkRead</code> 方法
     * 将使用 <code>name</code> 参数作为其参数调用。
     * <p>
     * 如果命名的文件不存在，是目录而不是普通文件，或者由于其他原因无法打开以供读取，则抛出
     * <code>FileNotFoundException</code>。
     *
     * @param      name   系统相关的文件名。
     * @exception  FileNotFoundException  如果文件不存在，
     *                   是目录而不是普通文件，
     *                   或由于其他原因无法打开以供读取。
     * @exception  SecurityException      如果存在安全经理且其
     *               <code>checkRead</code> 方法拒绝读取文件的访问。
     * @see        java.lang.SecurityManager#checkRead(java.lang.String)
     */
    public FileInputStream(String name) throws FileNotFoundException {
        this(name != null ? new File(name) : null);
    }

    /**
     * 通过打开与实际文件的连接来创建 <code>FileInputStream</code>，
     * 该文件由文件系统中的 <code>File</code>
     * 对象 <code>file</code> 命名。创建一个新的 <code>FileDescriptor</code>
     * 对象来表示此文件连接。
     * <p>
     * 首先，如果有安全经理，其 <code>checkRead</code> 方法
     * 将使用 <code>file</code> 参数表示的路径作为其参数调用。
     * <p>
     * 如果命名的文件不存在，是目录而不是普通文件，或者由于其他原因无法打开以供读取，则抛出
     * <code>FileNotFoundException</code>。
     *
     * @param      file   要打开以供读取的文件。
     * @exception  FileNotFoundException  如果文件不存在，
     *                   是目录而不是普通文件，
     *                   或由于其他原因无法打开以供读取。
     * @exception  SecurityException      如果存在安全经理且其
     *               <code>checkRead</code> 方法拒绝读取文件的访问。
     * @see        java.io.File#getPath()
     * @see        java.lang.SecurityManager#checkRead(java.lang.String)
     */
    public FileInputStream(File file) throws FileNotFoundException {
        String name = (file != null ? file.getPath() : null);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(name);
        }
        if (name == null) {
            throw new NullPointerException();
        }
        if (file.isInvalid()) {
            throw new FileNotFoundException("无效的文件路径");
        }
        fd = new FileDescriptor();
        fd.attach(this);
        path = name;
        open(name);
    }

    /**
     * 通过使用文件描述符 <code>fdObj</code> 创建 <code>FileInputStream</code>，
     * 该文件描述符表示文件系统中已存在的文件连接。
     * <p>
     * 如果存在安全经理，其 <code>checkRead</code> 方法将
     * 使用文件描述符 <code>fdObj</code> 作为其参数调用，以检查是否允许读取文件描述符。如果拒绝
     * 文件描述符的读取访问权限，则抛出 <code>SecurityException</code>。
     * <p>
     * 如果 <code>fdObj</code> 为 null，则抛出 <code>NullPointerException</code>。
     * <p>
     * 如果 <code>fdObj</code> 无效，此构造函数不会抛出异常。
     * 但是，如果尝试在结果流上调用方法以进行 I/O 操作，则会抛出 <code>IOException</code>。
     *
     * @param      fdObj   要打开以供读取的文件描述符。
     * @throws     SecurityException      如果存在安全经理且其
     *                 <code>checkRead</code> 方法拒绝读取文件描述符的访问。
     * @see        SecurityManager#checkRead(java.io.FileDescriptor)
     */
    public FileInputStream(FileDescriptor fdObj) {
        SecurityManager security = System.getSecurityManager();
        if (fdObj == null) {
            throw new NullPointerException();
        }
        if (security != null) {
            security.checkRead(fdObj);
        }
        fd = fdObj;
        path = null;

        /*
         * 文件描述符被多个流共享。
         * 将此流注册到文件描述符跟踪器。
         */
        fd.attach(this);
    }

                /**
     * 打开指定文件以供读取。
     * @param name 文件的名称
     */
    private native void open0(String name) throws FileNotFoundException;

    // 包装原生调用以允许仪器化
    /**
     * 打开指定文件以供读取。
     * @param name 文件的名称
     */
    private void open(String name) throws FileNotFoundException {
        open0(name);
    }

    /**
     * 从该输入流中读取一个字节的数据。如果目前没有输入可用，此方法将阻塞。
     *
     * @return     下一个字节的数据，或如果到达文件末尾，则返回<code>-1</code>。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public int read() throws IOException {
        return read0();
    }

    private native int read0() throws IOException;

    /**
     * 读取一个子数组作为一系列字节。
     * @param b 要写入的数据
     * @param off 数据中的起始偏移量
     * @param len 要写入的字节数
     * @exception IOException 如果发生 I/O 错误。
     */
    private native int readBytes(byte b[], int off, int len) throws IOException;

    /**
     * 从该输入流中读取最多 <code>b.length</code> 个字节的数据到字节数组中。此方法在有输入可用之前会阻塞。
     *
     * @param      b   读取数据的缓冲区。
     * @return     读入缓冲区的总字节数，或如果因为到达文件末尾而没有更多数据，则返回
     *             <code>-1</code>。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public int read(byte b[]) throws IOException {
        return readBytes(b, 0, b.length);
    }

    /**
     * 从该输入流中读取最多 <code>len</code> 个字节的数据到字节数组中。如果 <code>len</code> 不为零，该方法
     * 会阻塞直到有输入可用；否则，不读取任何字节并返回 <code>0</code>。
     *
     * @param      b     读取数据的缓冲区。
     * @param      off   目标数组 <code>b</code> 中的起始偏移量
     * @param      len   最大读取字节数。
     * @return     读入缓冲区的总字节数，或如果因为到达文件末尾而没有更多数据，则返回
     *             <code>-1</code>。
     * @exception  NullPointerException 如果 <code>b</code> 为 <code>null</code>。
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负数，
     * <code>len</code> 为负数，或 <code>len</code> 大于
     * <code>b.length - off</code>
     * @exception  IOException  如果发生 I/O 错误。
     */
    public int read(byte b[], int off, int len) throws IOException {
        return readBytes(b, off, len);
    }

    /**
     * 从输入流中跳过并丢弃 <code>n</code> 个字节的数据。
     *
     * <p><code>skip</code> 方法可能由于各种原因最终跳过更少的字节数，
     * 可能是 <code>0</code>。如果 <code>n</code> 为负数，该方法将尝试向后跳过。如果支持文件在其当前位置不支持向后跳过，则抛出 <code>IOException</code>。实际跳过的字节数将被返回。如果向前跳过，返回正值。如果向后跳过，返回负值。
     *
     * <p>此方法可能跳过的字节数超过支持文件中剩余的字节数。这不会引发异常，跳过的字节数可能包括一些超出支持文件 EOF 的字节数。尝试在跳过文件末尾后从流中读取将返回 -1，表示文件末尾。
     *
     * @param      n   要跳过的字节数。
     * @return     实际跳过的字节数。
     * @exception  IOException  如果 n 为负数，如果流不支持 seek，或发生 I/O 错误。
     */
    public long skip(long n) throws IOException {
        return skip0(n);
    }

    private native long skip0(long n) throws IOException;

    /**
     * 返回一个估计值，表示在下一次调用此输入流的方法时，可以从该输入流中读取（或跳过）而不阻塞的剩余字节数。当文件位置超出 EOF 时返回 0。下一次调用可能是同一线程或另一个线程。单次读取或跳过这么多字节不会阻塞，但可能会读取或跳过更少的字节。
     *
     * <p>在某些情况下，非阻塞读取（或跳过）可能看起来被阻塞，实际上只是较慢，例如通过慢速网络读取大文件时。
     *
     * @return     一个估计值，表示可以从该输入流中读取（或跳过）而不阻塞的剩余字节数。
     * @exception  IOException  如果此文件输入流已通过调用 {@code close} 关闭或发生 I/O 错误。
     */
    public int available() throws IOException {
        return available0();
    }

    private native int available0() throws IOException;

    /**
     * 关闭此文件输入流并释放与该流关联的任何系统资源。
     *
     * <p>如果此流有关联的通道，则通道也将被关闭。
     *
     * @exception  IOException  如果发生 I/O 错误。
     *
     * @revised 1.4
     * @spec JSR-51
     */
    public void close() throws IOException {
        synchronized (closeLock) {
            if (closed) {
                return;
            }
            closed = true;
        }
        if (channel != null) {
           channel.close();
        }

        fd.closeAll(new Closeable() {
            public void close() throws IOException {
               close0();
           }
        });
    }

                /**
     * 返回表示此 <code>FileInputStream</code> 使用的文件系统中实际文件连接的
     * <code>FileDescriptor</code> 对象。
     *
     * @return     与此流关联的文件描述符对象。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FileDescriptor
     */
    public final FileDescriptor getFD() throws IOException {
        if (fd != null) {
            return fd;
        }
        throw new IOException();
    }

    /**
     * 返回与此文件输入流关联的唯一 {@link java.nio.channels.FileChannel FileChannel}
     * 对象。
     *
     * <p> 返回的通道的初始 {@link java.nio.channels.FileChannel#position()
     * 位置} 将等于从文件中读取的字节数。从这个流中读取字节将增加通道的位置。更改通道的位置，
     * 无论是显式更改还是通过读取，都将更改此流的文件位置。
     *
     * @return  与此文件输入流关联的文件通道
     *
     * @since 1.4
     * @spec JSR-51
     */
    public FileChannel getChannel() {
        synchronized (this) {
            if (channel == null) {
                channel = FileChannelImpl.open(fd, path, true, false, this);
            }
            return channel;
        }
    }

    private static native void initIDs();

    private native void close0() throws IOException;

    static {
        initIDs();
    }

    /**
     * 确保当没有更多引用指向此文件输入流时，调用其 <code>close</code> 方法。
     *
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FileInputStream#close()
     */
    protected void finalize() throws IOException {
        if ((fd != null) &&  (fd != FileDescriptor.in)) {
            /* 如果 fd 被共享，文件描述符中的引用将确保仅在安全时调用终结器。
             * 所有使用 fd 的引用都已变得不可达。我们可以调用 close()
             */
            close();
        }
    }
}
