/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.nio;

import java.io.FileDescriptor;
import sun.misc.Unsafe;


/**
 * 一个直接字节缓冲区，其内容是一个文件的内存映射区域。
 *
 * <p> 映射字节缓冲区是通过 {@link
 * java.nio.channels.FileChannel#map FileChannel.map} 方法创建的。此类通过特定于内存映射文件区域的操作扩展了 {@link ByteBuffer} 类。
 *
 * <p> 映射字节缓冲区和它所表示的文件映射在缓冲区本身被垃圾回收之前一直有效。
 *
 * <p> 映射字节缓冲区的内容可以在任何时候发生变化，例如如果此程序或另一个程序更改了映射文件的相应区域。无论是否发生此类更改，以及何时发生，都是操作系统依赖的，因此未指定。
 *
 * <a name="inaccess"></a><p> 映射字节缓冲区的全部或部分可能在任何时候变得不可访问，例如如果映射文件被截断。尝试访问映射字节缓冲区的不可访问区域不会改变缓冲区的内容，并将导致未指定的异常在访问时或稍后被抛出。因此，强烈建议采取适当的预防措施，避免此程序或并发运行的程序对映射文件进行除读取或写入文件内容之外的任何操作。
 *
 * <p> 除了上述行为外，映射字节缓冲区的行为与普通直接字节缓冲区无异。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家组
 * @since 1.4
 */

public abstract class MappedByteBuffer
    extends ByteBuffer
{

    // 这有点反常：按理说 MappedByteBuffer 应该是 DirectByteBuffer 的子类，但为了保持规范的清晰和简单，以及优化的目的，这样做更容易。
    // 这是因为 DirectByteBuffer 是一个包私有的类。

    // 对于映射缓冲区，如果有效，则可以用于映射操作的 FileDescriptor；如果缓冲区未映射，则为 null。
    private final FileDescriptor fd;

    // 这应该只由 DirectByteBuffer 构造函数调用
    //
    MappedByteBuffer(int mark, int pos, int lim, int cap, // 包私有的
                     FileDescriptor fd)
    {
        super(mark, pos, lim, cap);
        this.fd = fd;
    }

    MappedByteBuffer(int mark, int pos, int lim, int cap) { // 包私有的
        super(mark, pos, lim, cap);
        this.fd = null;
    }

    private void checkMapped() {
        if (fd == null)
            // 只有当用户显式地将直接字节缓冲区转换为映射字节缓冲区时才会发生
            throw new UnsupportedOperationException();
    }

    // 返回缓冲区与映射的页面对齐地址之间的距离（以字节为单位）。每次计算以避免在每个直接缓冲区中存储。
    private long mappingOffset() {
        int ps = Bits.pageSize();
        long offset = address % ps;
        return (offset >= 0) ? offset : (ps + offset);
    }

    private long mappingAddress(long mappingOffset) {
        return address - mappingOffset;
    }

    private long mappingLength(long mappingOffset) {
        return (long)capacity() + mappingOffset;
    }

    /**
     * 告知此缓冲区的内容是否驻留在物理内存中。
     *
     * <p> 返回值为 <tt>true</tt> 意味着此缓冲区的内容很可能驻留在物理内存中，因此可以访问而不引发任何虚拟内存页面错误或 I/O 操作。返回值为 <tt>false</tt> 并不意味着缓冲区的内容不在物理内存中。
     *
     * <p> 返回的值是一个提示，而不是保证，因为底层操作系统可能在调用此方法返回时已经将缓冲区的部分数据分页出去。 </p>
     *
     * @return  如果此缓冲区的内容很可能驻留在物理内存中，则返回 <tt>true</tt>
     */
    public final boolean isLoaded() {
        checkMapped();
        if ((address == 0) || (capacity() == 0))
            return true;
        long offset = mappingOffset();
        long length = mappingLength(offset);
        return isLoaded0(mappingAddress(offset), length, Bits.pageCount(length));
    }

    // 未使用，但可能是存储的目标，详见 load() 方法的详细信息。
    private static byte unused;

    /**
     * 将此缓冲区的内容加载到物理内存中。
     *
     * <p> 此方法会尽力确保在返回时，此缓冲区的内容驻留在物理内存中。调用此方法可能会导致一些页面错误和 I/O 操作发生。 </p>
     *
     * @return  此缓冲区
     */
    public final MappedByteBuffer load() {
        checkMapped();
        if ((address == 0) || (capacity() == 0))
            return this;
        long offset = mappingOffset();
        long length = mappingLength(offset);
        load0(mappingAddress(offset), length);

        // 从每个页面读取一个字节以将其带入内存。随着我们进行，计算校验和以防止编译器将循环视为死代码。
        Unsafe unsafe = Unsafe.getUnsafe();
        int ps = Bits.pageSize();
        int count = Bits.pageCount(length);
        long a = mappingAddress(offset);
        byte x = 0;
        for (int i=0; i<count; i++) {
            x ^= unsafe.getByte(a);
            a += ps;
        }
        if (unused != 0)
            unused = x;

        return this;
    }

    /**
     * 强制将对此缓冲区内容所做的任何更改写入包含映射文件的存储设备。
     *
     * <p> 如果此缓冲区映射的文件位于本地存储设备上，则当此方法返回时，可以保证自创建此缓冲区或上次调用此方法以来对缓冲区所做的所有更改都将写入该设备。
     *
     * <p> 如果文件不位于本地设备上，则不作任何保证。
     *
     * <p> 如果此缓冲区不是以读/写模式（{@link
     * java.nio.channels.FileChannel.MapMode#READ_WRITE}）映射的，则调用此方法不会产生任何效果。 </p>
     *
     * @return  此缓冲区
     */
    public final MappedByteBuffer force() {
        checkMapped();
        if ((address != 0) && (capacity() != 0)) {
            long offset = mappingOffset();
            force0(fd, mappingAddress(offset), mappingLength(offset));
        }
        return this;
    }

    private native boolean isLoaded0(long address, long length, int pageCount);
    private native void load0(long address, long length);
    private native void force0(FileDescriptor fd, long address, long length);
}
