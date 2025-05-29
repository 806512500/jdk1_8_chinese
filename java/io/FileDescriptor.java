
/*
 * 版权所有 (c) 2003, 2011, Oracle 和/或其附属公司。保留所有权利。
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

import java.util.ArrayList;
import java.util.List;

/**
 * 文件描述符类的实例作为底层机器特定结构的不透明句柄，表示一个打开的文件、一个打开的套接字，或其他字节的来源或接收器。
 * 文件描述符的主要实际用途是创建一个包含它的 {@link FileInputStream} 或 {@link FileOutputStream}。
 *
 * <p>应用程序不应创建自己的文件描述符。
 *
 * @author  Pavani Diwanji
 * @since   JDK1.0
 */
public final class FileDescriptor {

    private int fd;

    private long handle;

    private Closeable parent;
    private List<Closeable> otherParents;
    private boolean closed;

    /**
     * 构造一个（无效的）FileDescriptor 对象。
     */
    public /**/ FileDescriptor() {
        fd = -1;
        handle = -1;
    }

    static {
        initIDs();
    }

    // 在 SharedSecrets 中设置 JavaIOFileDescriptorAccess
    static {
        sun.misc.SharedSecrets.setJavaIOFileDescriptorAccess(
            new sun.misc.JavaIOFileDescriptorAccess() {
                public void set(FileDescriptor obj, int fd) {
                    obj.fd = fd;
                }

                public int get(FileDescriptor obj) {
                    return obj.fd;
                }

                public void setHandle(FileDescriptor obj, long handle) {
                    obj.handle = handle;
                }

                public long getHandle(FileDescriptor obj) {
                    return obj.handle;
                }
            }
        );
    }

    /**
     * 标准输入流的句柄。通常，此文件描述符不会直接使用，而是通过名为 {@code System.in} 的输入流使用。
     *
     * @see     java.lang.System#in
     */
    public static final FileDescriptor in = standardStream(0);

    /**
     * 标准输出流的句柄。通常，此文件描述符不会直接使用，而是通过名为 {@code System.out} 的输出流使用。
     * @see     java.lang.System#out
     */
    public static final FileDescriptor out = standardStream(1);

    /**
     * 标准错误流的句柄。通常，此文件描述符不会直接使用，而是通过名为 {@code System.err} 的输出流使用。
     *
     * @see     java.lang.System#err
     */
    public static final FileDescriptor err = standardStream(2);

    /**
     * 测试此文件描述符对象是否有效。
     *
     * @return  如果文件描述符对象表示一个有效的、打开的文件、套接字或其他活动的 I/O 连接，则返回 {@code true}；
     *          否则返回 {@code false}。
     */
    public boolean valid() {
        return ((handle != -1) || (fd != -1));
    }

    /**
     * 强制所有系统缓冲区与底层设备同步。此方法在与此 FileDescriptor 相关的所有已修改数据和属性被写入相关设备后返回。
     * 特别地，如果此 FileDescriptor 指向一个物理存储介质，例如文件系统中的文件，sync 在所有与该 FileDescriptor
     * 关联的内存中已修改的缓冲区被写入物理介质之前不会返回。
     *
     * sync 旨在由需要物理存储（如文件）处于已知状态的代码使用。例如，提供简单事务设施的类可能会使用 sync
     * 来确保由特定事务引起的所有文件更改都被记录到存储介质上。
     *
     * sync 仅影响此 FileDescriptor 下游的缓冲区。如果应用程序在内存中执行任何缓冲（例如，通过 BufferedOutputStream 对象），
     * 则必须将这些缓冲区刷新到 FileDescriptor（例如，通过调用 OutputStream.flush）才能使这些数据受 sync 影响。
     *
     * @exception SyncFailedException
     *        当缓冲区无法刷新，或系统无法保证所有缓冲区已与物理介质同步时抛出。
     * @since     JDK1.1
     */
    public native void sync() throws SyncFailedException;

    /* 该例程初始化类的 JNI 字段偏移量 */
    private static native void initIDs();

    private static native long set(int d);

    private static FileDescriptor standardStream(int fd) {
        FileDescriptor desc = new FileDescriptor();
        desc.handle = set(fd);
        return desc;
    }

    /*
     * 包私有方法，用于跟踪引用。
     * 如果多个流指向同一个 FileDescriptor，我们将遍历所有引用并调用 close()
     */

    /**
     * 为跟踪目的将一个 Closeable 附加到此 FD。
     * 当需要简化 closeAll 时，parent 引用将被添加到 otherParents。
     */
    synchronized void attach(Closeable c) {
        if (parent == null) {
            // 第一个调用者可以执行此操作
            parent = c;
        } else if (otherParents == null) {
            otherParents = new ArrayList<>();
            otherParents.add(parent);
            otherParents.add(c);
        } else {
            otherParents.add(c);
        }
    }

    /**
     * 遍历所有共享此 FD 的 Closeable 并调用每个的 close()。
     *
     * 调用者 Closeable 负责调用 close0()。
     */
    @SuppressWarnings("try")
    synchronized void closeAll(Closeable releaser) throws IOException {
        if (!closed) {
            closed = true;
            IOException ioe = null;
            try (Closeable c = releaser) {
                if (otherParents != null) {
                    for (Closeable referent : otherParents) {
                        try {
                            referent.close();
                        } catch(IOException x) {
                            if (ioe == null) {
                                ioe = x;
                            } else {
                                ioe.addSuppressed(x);
                            }
                        }
                    }
                }
            } catch(IOException ex) {
                /*
                 * 如果 releaser close() 抛出 IOException
                 * 将其他异常作为受抑制的异常添加。
                 */
                if (ioe != null)
                    ex.addSuppressed(ioe);
                ioe = ex;
            } finally {
                if (ioe != null)
                    throw ioe;
            }
        }
    }
}
