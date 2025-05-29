/*
 * 版权所有 (c) 2007, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.nio.file.attribute;

import java.nio.ByteBuffer;
import java.util.List;
import java.io.IOException;

/**
 * 提供文件用户定义属性视图的文件属性视图，有时称为<em>扩展属性</em>。用户定义的文件属性用于存储对文件系统无意义的元数据。它主要用于直接支持此功能的文件系统实现，但也可能被模拟。此类模拟的细节高度依赖于实现，因此未指定。
 *
 * <p> 此 {@code FileAttributeView} 以名称/值对的形式提供文件用户定义属性的视图，其中属性名称由 {@code String} 表示。实现可能需要在访问属性时从平台或文件系统的表示形式进行编码和解码。值具有不透明的内容。此属性视图定义了 {@link #read read} 和 {@link #write write} 方法，用于将值读入或写入 {@link ByteBuffer}。此 {@code FileAttributeView} 不适用于属性值大小超过 {@link Integer#MAX_VALUE} 的情况。
 *
 * <p> 在某些实现中，用户定义的属性可能用于存储与安全相关的属性，因此，默认提供程序至少在安装了安全管理器的情况下，访问用户定义的属性的所有方法都需要 {@code RuntimePermission("accessUserDefinedAttributes")} 权限。
 *
 * <p> 可以使用 {@link java.nio.file.FileStore#supportsFileAttributeView supportsFileAttributeView} 方法测试特定的 {@link java.nio.file.FileStore FileStore} 是否支持存储用户定义的属性。
 *
 * <p> 如果需要动态访问文件属性，可以使用 {@link java.nio.file.Files#getAttribute getAttribute} 方法读取属性值。属性值以字节数组 (byte[]) 的形式返回。可以使用 {@link java.nio.file.Files#setAttribute setAttribute} 方法从缓冲区（如同调用 {@link #write write} 方法）或字节数组 (byte[]) 写入用户定义的属性值。
 *
 * @since 1.7
 */

public interface UserDefinedFileAttributeView
    extends FileAttributeView
{
    /**
     * 返回此属性视图的名称。此类属性视图的名称为 {@code "user"}。
     */
    @Override
    String name();

    /**
     * 返回包含用户定义属性名称的列表。
     *
     * @return  包含文件用户定义属性名称的不可修改列表
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          如果默认提供程序安装了安全管理器，并且它拒绝了 {@link
     *          RuntimePermission}<tt>("accessUserDefinedAttributes")</tt>
     *          或其 {@link SecurityManager#checkRead(String) checkRead} 方法拒绝了对文件的读取访问。
     */
    List<String> list() throws IOException;

    /**
     * 返回用户定义属性值的大小。
     *
     * @param   name
     *          属性名称
     *
     * @return  属性值的大小，以字节为单位。
     *
     * @throws  ArithmeticException
     *          如果属性的大小超过 {@link Integer#MAX_VALUE}
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          如果默认提供程序安装了安全管理器，并且它拒绝了 {@link
     *          RuntimePermission}<tt>("accessUserDefinedAttributes")</tt>
     *          或其 {@link SecurityManager#checkRead(String) checkRead} 方法拒绝了对文件的读取访问。
     */
    int size(String name) throws IOException;

    /**
     * 将用户定义属性的值读入缓冲区。
     *
     * <p> 此方法将属性值作为一系列字节读入给定缓冲区，如果缓冲区中剩余的字节数不足以读取完整的属性值，则会失败。传输到缓冲区中的字节数为 {@code n}，其中 {@code n} 是属性值的大小。字节序列的第一个字节位于索引 {@code p}，最后一个字节位于索引 {@code p + n - 1}，其中 {@code p} 是缓冲区的位置。返回时，缓冲区的位置将等于 {@code p + n}；其限制不会改变。
     *
     * <p> <b>使用示例：</b>
     * 假设我们想读取存储为用户定义属性（名称为 "{@code user.mimetype}"）的文件的 MIME 类型。
     * <pre>
     *    UserDefinedFileAttributeView view =
     *        Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
     *    String name = "user.mimetype";
     *    ByteBuffer buf = ByteBuffer.allocate(view.size(name));
     *    view.read(name, buf);
     *    buf.flip();
     *    String value = Charset.defaultCharset().decode(buf).toString();
     * </pre>
     *
     * @param   name
     *          属性名称
     * @param   dst
     *          目标缓冲区
     *
     * @return  读取的字节数，可能为零
     *
     * @throws  IllegalArgumentException
     *          如果目标缓冲区是只读的
     * @throws  IOException
     *          如果发生 I/O 错误或目标缓冲区中没有足够的空间用于属性值
     * @throws  SecurityException
     *          如果默认提供程序安装了安全管理器，并且它拒绝了 {@link
     *          RuntimePermission}<tt>("accessUserDefinedAttributes")</tt>
     *          或其 {@link SecurityManager#checkRead(String) checkRead} 方法拒绝了对文件的读取访问。
     *
     * @see #size
     */
    int read(String name, ByteBuffer dst) throws IOException;

                /**
     * 从缓冲区写入用户定义的属性的值。
     *
     * <p> 此方法从给定的缓冲区中以一系列字节的形式写入属性的值。要传输的值的大小为 {@code r}，
     * 其中 {@code r} 是缓冲区中剩余的字节数，即 {@code src.remaining()}。从缓冲区的索引 {@code p} 开始
     * 传输字节序列，其中 {@code p} 是缓冲区的位置。返回时，缓冲区的位置将等于 {@code
     * p + n}，其中 {@code n} 是传输的字节数；其限制不会改变。
     *
     * <p> 如果已存在同名的属性，则其值将被替换。如果属性不存在，则会创建该属性。是否对属性的存在性进行测试
     * 以及属性的创建是否与其他文件系统活动原子化，这取决于具体实现。
     *
     * <p> 如果没有足够的空间存储属性，或者属性名称或值超过了特定实现的最大大小，
     * 则会抛出 {@code IOException}。
     *
     * <p> <b>使用示例：</b>
     * 假设我们要将文件的 MIME 类型作为用户定义的属性写入：
     * <pre>
     *    UserDefinedFileAttributeView view =
     *        FIles.getFileAttributeView(path, UserDefinedFileAttributeView.class);
     *    view.write("user.mimetype", Charset.defaultCharset().encode("text/html"));
     * </pre>
     *
     * @param   name
     *          属性名称
     * @param   src
     *          包含属性值的缓冲区
     *
     * @return  写入的字节数，可能为零
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，安装了安全经理，并且它拒绝 {@link
     *          RuntimePermission}<tt>("accessUserDefinedAttributes")</tt>
     *          或其 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法拒绝对文件的写入访问。
     */
    int write(String name, ByteBuffer src) throws IOException;

    /**
     * 删除用户定义的属性。
     *
     * @param   name
     *          属性名称
     *
     * @throws  IOException
     *          如果发生 I/O 错误或属性不存在
     * @throws  SecurityException
     *          在默认提供者的情况下，安装了安全经理，并且它拒绝 {@link
     *          RuntimePermission}<tt>("accessUserDefinedAttributes")</tt>
     *          或其 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法拒绝对文件的写入访问。
     */
    void delete(String name) throws IOException;
}
