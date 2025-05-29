
/*
 * 版权所有 (c) 2000, 2013, Oracle 及/或其附属公司。保留所有权利。
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

// -- 本文件由机械生成：请勿编辑！ -- //

package java.nio;










/**
 * 一个字节缓冲区。
 *
 * <p> 本类定义了六类对字节缓冲区的操作：
 *
 * <ul>
 *
 *   <li><p> 绝对和相对的 {@link #get() <i>get</i>} 和
 *   {@link #put(byte) <i>put</i>} 方法，用于读取和写入单个字节； </p></li>
 *
 *   <li><p> 相对的 {@link #get(byte[]) <i>bulk get</i>}
 *   方法，用于将连续的字节序列从缓冲区传输到数组中； </p></li>
 *
 *   <li><p> 相对的 {@link #put(byte[]) <i>bulk put</i>}
 *   方法，用于将来自字节数组或其他字节
 *   缓冲区的连续字节序列传输到此缓冲区中； </p></li>
 *

 *
 *   <li><p> 绝对和相对的 {@link #getChar() <i>get</i>}
 *   和 {@link #putChar(char) <i>put</i>} 方法，用于读取和
 *   写入其他基本类型值，将它们转换为特定字节顺序的字节序列； </p></li>
 *
 *   <li><p> 用于创建 <i><a href="#views">视图缓冲区</a></i> 的方法，
 *   允许将字节缓冲区视为包含其他基本类型值的缓冲区； </p></li>
 *

 *
 *   <li><p> 用于 {@link #compact 压缩}、{@link
 *   #duplicate 复制} 和 {@link #slice 切片}
 *   字节缓冲区的方法。 </p></li>
 *
 * </ul>
 *
 * <p> 字节缓冲区可以通过 {@link #allocate
 * <i>分配</i>} 创建，这会为缓冲区的内容分配空间，或者通过 {@link #wrap(byte[]) <i>包装</i>}
 * 将现有的字节数组包装到缓冲区中。
 *







 *

 *
 * <a name="direct"></a>
 * <h2> 直接 <i>vs.</i> 非直接缓冲区 </h2>
 *
 * <p> 字节缓冲区可以是 <i>直接</i> 或 <i>非直接</i>。对于直接字节缓冲区，Java 虚拟机会尽力直接在其上执行本机 I/O 操作。也就是说，它会尝试避免在每次调用底层操作系统的本机 I/O 操作之前（或之后）将缓冲区的内容复制到（或从）中间缓冲区。
 *
 * <p> 可以通过调用本类的 {@link
 * #allocateDirect(int) allocateDirect} 工厂方法创建直接字节缓冲区。通过此方法返回的缓冲区通常比非直接缓冲区具有更高的分配和释放成本。直接缓冲区的内容可能位于常规垃圾回收堆之外，因此它们对应用程序内存占用的影响可能不明显。因此，建议主要为大型、长期存在的缓冲区分配直接缓冲区，这些缓冲区受底层系统本机 I/O 操作的影响。通常情况下，只有在直接缓冲区能够显著提高程序性能时才应使用它们。
 *
 * <p> 也可以通过 {@link
 * java.nio.channels.FileChannel#map 映射} 文件的区域直接到内存中来创建直接字节缓冲区。Java 平台的实现可以选择通过 JNI 从本机代码创建直接字节缓冲区。如果这些类型之一的缓冲区实例引用了不可访问的内存区域，则尝试访问该区域不会改变缓冲区的内容，并且会在访问时或稍后抛出未指定的异常。
 *
 * <p> 可以通过调用缓冲区的 {@link #isDirect isDirect} 方法来确定字节缓冲区是直接的还是非直接的。提供此方法是为了在性能关键代码中进行显式的缓冲区管理。
 *
 *
 * <a name="bin"></a>
 * <h2> 二进制数据访问 </h2>
 *
 * <p> 本类定义了读取和写入所有其他基本类型值的方法，但 <tt>boolean</tt> 除外。基本值根据缓冲区的当前字节顺序转换为（或从）字节序列，该顺序可以通过 {@link #order order}
 * 方法检索和修改。特定的字节顺序由 {@link
 * ByteOrder} 类的实例表示。字节缓冲区的初始顺序始终为 {@link
 * ByteOrder#BIG_ENDIAN BIG_ENDIAN}。
 *
 * <p> 为了访问异构二进制数据，即不同类型的值序列，本类为每种类型定义了一组绝对和相对的 <i>get</i> 和 <i>put</i> 方法。例如，对于 32 位浮点值，本类定义了：
 *
 * <blockquote><pre>
 * float  {@link #getFloat()}
 * float  {@link #getFloat(int) getFloat(int index)}
 *  void  {@link #putFloat(float) putFloat(float f)}
 *  void  {@link #putFloat(int,float) putFloat(int index, float f)}</pre></blockquote>
 *
 * <p> 还为 <tt>char</tt>、<tt>short</tt>、<tt>int</tt>、<tt>long</tt> 和 <tt>double</tt> 类型定义了相应的访问方法。绝对 <i>get</i> 和 <i>put</i> 方法的索引参数是以字节为单位，而不是以读取或写入的类型为单位。
 *
 * <a name="views"></a>
 *
 * <p> 为了访问同构二进制数据，即相同类型的值序列，本类定义了可以创建 <i>视图</i> 的方法。一个 <i>视图缓冲区</i> 只是另一个缓冲区，其内容由字节缓冲区支持。对字节缓冲区内容的更改将在视图缓冲区中可见，反之亦然；两个缓冲区的位置、限制和标记值是独立的。例如，{@link
 * #asFloatBuffer() asFloatBuffer} 方法创建了一个由调用该方法的字节缓冲区支持的 {@link FloatBuffer} 类的实例。还为 <tt>char</tt>、<tt>short</tt>、<tt>int</tt>、<tt>long</tt> 和 <tt>double</tt> 类型定义了相应的视图创建方法。
 *
 * <p> 视图缓冲区相对于上述类型的 <i>get</i> 和 <i>put</i> 方法系列具有三个重要的优势：
 *
 * <ul>
 *
 *   <li><p> 视图缓冲区不是以字节为单位索引，而是以特定类型的值大小为单位索引； </p></li>
 *
 *   <li><p> 视图缓冲区提供了相对的批量 <i>get</i> 和 <i>put</i>
 *   方法，可以将同类型值的连续序列在缓冲区和数组或其他同类型缓冲区之间传输； </p></li>
 *
 *   <li><p> 视图缓冲区可能更高效，因为只有当其支持的字节缓冲区是直接的时，它才会是直接的。 </p></li>
 *
 * </ul>
 *
 * <p> 视图缓冲区的字节顺序固定为其创建时的字节缓冲区的字节顺序。 </p>
 *


            *











*








 *

 * <h2> 方法链调用 </h2>

 *
 * <p> 本类中没有其他返回值的方法被指定为返回调用它们的缓冲区。这允许方法调用链式调用。
 *

 *
 * 以下语句序列
 *
 * <blockquote><pre>
 * bb.putInt(0xCAFEBABE);
 * bb.putShort(3);
 * bb.putShort(45);</pre></blockquote>
 *
 * 可以例如被替换为单个语句
 *
 * <blockquote><pre>
 * bb.putInt(0xCAFEBABE).putShort(3).putShort(45);</pre></blockquote>
 *

















 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
 * @since 1.4
 */

public abstract class ByteBuffer
    extends Buffer
    implements Comparable<ByteBuffer>
{

    // 这些字段在此声明而不是在 Heap-X-Buffer 中声明，以减少访问这些值所需的虚拟方法调用次数，对于小缓冲区来说，这尤其昂贵。
    //
    final byte[] hb;                  // 仅对堆缓冲区非空
    final int offset;
    boolean isReadOnly;                 // 仅对堆缓冲区有效

    // 创建具有给定标记、位置、限制、容量、后端数组和数组偏移量的新缓冲区
    //
    ByteBuffer(int mark, int pos, int lim, int cap,   // 包私有
                 byte[] hb, int offset)
    {
        super(mark, pos, lim, cap);
        this.hb = hb;
        this.offset = offset;
    }

    // 创建具有给定标记、位置、限制和容量的新缓冲区
    //
    ByteBuffer(int mark, int pos, int lim, int cap) { // 包私有
        this(mark, pos, lim, cap, null, 0);
    }



    /**
     * 分配一个新的直接字节缓冲区。
     *
     * <p> 新缓冲区的位置将为零，其限制将为其容量，其标记将未定义，且其每个元素将初始化为零。是否具有
     * {@link #hasArray 后端数组} 是未指定的。
     *
     * @param  capacity
     *         新缓冲区的容量，以字节为单位
     *
     * @return  新的字节缓冲区
     *
     * @throws  IllegalArgumentException
     *          如果 <tt>capacity</tt> 是负整数
     */
    public static ByteBuffer allocateDirect(int capacity) {
        return new DirectByteBuffer(capacity);
    }



    /**
     * 分配一个新的字节缓冲区。
     *
     * <p> 新缓冲区的位置将为零，其限制将为其容量，其标记将未定义，且其每个元素将初始化为零。它将具有 {@link #array 后端数组}，
     * 且其 {@link #arrayOffset 数组偏移量} 将为零。
     *
     * @param  capacity
     *         新缓冲区的容量，以字节为单位
     *
     * @return  新的字节缓冲区
     *
     * @throws  IllegalArgumentException
     *          如果 <tt>capacity</tt> 是负整数
     */
    public static ByteBuffer allocate(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        return new HeapByteBuffer(capacity, capacity);
    }

    /**
     * 将字节数组包装成缓冲区。
     *
     * <p> 新缓冲区将由给定的字节数组支持；
     * 即，对缓冲区的修改将导致数组被修改，反之亦然。新缓冲区的容量将是
     * <tt>array.length</tt>，其位置将是 <tt>offset</tt>，其限制
     * 将是 <tt>offset + length</tt>，其标记将未定义。其
     * {@link #array 后端数组} 将是给定数组，且
     * 其 {@link #arrayOffset 数组偏移量} 将为零。 </p>
     *
     * @param  array
     *         将支持新缓冲区的数组
     *
     * @param  offset
     *         要使用的子数组的偏移量；必须非负且不大于 <tt>array.length</tt>。新缓冲区的位置
     *         将设置为该值。
     *
     * @param  length
     *         要使用的子数组的长度；
     *         必须非负且不大于
     *         <tt>array.length - offset</tt>。
     *         新缓冲区的限制将设置为 <tt>offset + length</tt>。
     *
     * @return  新的字节缓冲区
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>offset</tt> 和 <tt>length</tt>
     *          参数的前置条件不成立
     */
    public static ByteBuffer wrap(byte[] array,
                                    int offset, int length)
    {
        try {
            return new HeapByteBuffer(array, offset, length);
        } catch (IllegalArgumentException x) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * 将字节数组包装成缓冲区。
     *
     * <p> 新缓冲区将由给定的字节数组支持；
     * 即，对缓冲区的修改将导致数组被修改，反之亦然。新缓冲区的容量和限制将是
     * <tt>array.length</tt>，其位置将为零，其标记将未定义。其 {@link #array 后端数组} 将是
     * 给定数组，且其 {@link #arrayOffset 数组偏移量} 将为零。 </p>
     *
     * @param  array
     *         将支持此缓冲区的数组
     *
     * @return  新的字节缓冲区
     */
    public static ByteBuffer wrap(byte[] array) {
        return wrap(array, 0, array.length);
    }






























































































    /**
     * 创建一个新字节缓冲区，其内容是此缓冲区内容的共享子序列。
     *
     * <p> 新缓冲区的内容将从此缓冲区的当前位置开始。对此缓冲区内容的更改将在新缓冲区中可见，反之亦然；两个缓冲区的位置、限制和标记值将独立。
     *
     * <p> 新缓冲区的位置将为零，其容量和限制将为此缓冲区中剩余的字节数，其标记将未定义。新缓冲区将直接，当且仅当此缓冲区直接时；它将是只读的，当且仅当此缓冲区是只读的。 </p>
     *
     * @return  新的字节缓冲区
     */
    public abstract ByteBuffer slice();

                /**
     * 创建一个与该缓冲区内容共享的新字节缓冲区。
     *
     * <p> 新缓冲区的内容将与该缓冲区的内容相同。对该缓冲区内容的更改将在新缓冲区中可见，反之亦然；但两个缓冲区的位置、限制和标记值将独立。
     *
     * <p> 新缓冲区的容量、限制、位置和标记值将与该缓冲区完全相同。新缓冲区将直接，当且仅当该缓冲区直接时；它将只读，当且仅当该缓冲区只读。 </p>
     *
     * @return  新的字节缓冲区
     */
    public abstract ByteBuffer duplicate();

    /**
     * 创建一个新的只读字节缓冲区，该缓冲区与该缓冲区的内容共享。
     *
     * <p> 新缓冲区的内容将与该缓冲区的内容相同。对该缓冲区内容的更改将在新缓冲区中可见；然而，新缓冲区本身将是只读的，不允许修改共享内容。两个缓冲区的位置、限制和标记值将独立。
     *
     * <p> 新缓冲区的容量、限制、位置和标记值将与该缓冲区完全相同。
     *
     * <p> 如果该缓冲区本身是只读的，那么此方法的行为与 {@link #duplicate duplicate} 方法完全相同。 </p>
     *
     * @return  新的只读字节缓冲区
     */
    public abstract ByteBuffer asReadOnlyBuffer();


    // -- 单例获取/放置方法 --

    /**
     * 相对 <i>获取</i> 方法。读取该缓冲区当前位置的字节，然后递增位置。
     *
     * @return  缓冲区当前位置的字节
     *
     * @throws  BufferUnderflowException
     *          如果缓冲区的当前位置不小于其限制
     */
    public abstract byte get();

    /**
     * 相对 <i>放置</i> 方法&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 将给定的字节写入该缓冲区的当前位置，然后递增位置。 </p>
     *
     * @param  b
     *         要写入的字节
     *
     * @return  该缓冲区
     *
     * @throws  BufferOverflowException
     *          如果该缓冲区的当前位置不小于其限制
     *
     * @throws  ReadOnlyBufferException
     *          如果该缓冲区是只读的
     */
    public abstract ByteBuffer put(byte b);

    /**
     * 绝对 <i>获取</i> 方法。读取给定索引处的字节。
     *
     * @param  index
     *         要读取字节的索引
     *
     * @return  给定索引处的字节
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负数
     *          或不小于缓冲区的限制
     */
    public abstract byte get(int index);














    /**
     * 绝对 <i>放置</i> 方法&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 将给定的字节写入该缓冲区的给定索引处。 </p>
     *
     * @param  index
     *         要写入字节的索引
     *
     * @param  b
     *         要写入的字节值
     *
     * @return  该缓冲区
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负数
     *          或不小于缓冲区的限制
     *
     * @throws  ReadOnlyBufferException
     *          如果该缓冲区是只读的
     */
    public abstract ByteBuffer put(int index, byte b);


    // -- 批量获取操作 --

    /**
     * 相对批量 <i>获取</i> 方法。
     *
     * <p> 该方法将字节从该缓冲区传输到给定的目标数组。如果缓冲区中剩余的字节少于请求所需的字节数，即如果
     * <tt>length</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>，则不传输任何字节，并抛出 {@link BufferUnderflowException}。
     *
     * <p> 否则，该方法将 <tt>length</tt> 个字节从该缓冲区复制到给定数组，从该缓冲区的当前位置开始，并在数组中给定的偏移量处开始。然后将该缓冲区的位置递增 <tt>length</tt>。
     *
     * <p> 换句话说，形式为 <tt>src.get(dst,&nbsp;off,&nbsp;len)</tt> 的该方法调用与循环
     *
     * <pre>{@code
     *     for (int i = off; i < off + len; i++)
     *         dst[i] = src.get():
     * }</pre>
     *
     * 有完全相同的效果，只是它首先检查该缓冲区中是否有足够的字节，并且可能效率更高。
     *
     * @param  dst
     *         要写入字节的数组
     *
     * @param  offset
     *         数组中第一个要写入的字节的偏移量；必须是非负数且不大于
     *         <tt>dst.length</tt>
     *
     * @param  length
     *         要写入给定数组的最大字节数；必须是非负数且不大于
     *         <tt>dst.length - offset</tt>
     *
     * @return  该缓冲区
     *
     * @throws  BufferUnderflowException
     *          如果该缓冲区中剩余的字节少于 <tt>length</tt>
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>offset</tt> 和 <tt>length</tt> 参数的前置条件不成立
     */
    public ByteBuffer get(byte[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        if (length > remaining())
            throw new BufferUnderflowException();
        int end = offset + length;
        for (int i = offset; i < end; i++)
            dst[i] = get();
        return this;
    }

    /**
     * 相对批量 <i>获取</i> 方法。
     *
     * <p> 该方法将字节从该缓冲区传输到给定的目标数组。形式为
     * <tt>src.get(a)</tt> 的该方法调用的行为与调用
     *
     * <pre>
     *     src.get(a, 0, a.length) </pre>
     *
     * 完全相同。
     *
     * @param   dst
     *          目标数组
     *
     * @return  该缓冲区
     *
     * @throws  BufferUnderflowException
     *          如果该缓冲区中剩余的字节少于 <tt>length</tt>
     */
    public ByteBuffer get(byte[] dst) {
        return get(dst, 0, dst.length);
    }

    // -- 批量 put 操作 --

    /**
     * 相对批量 <i>put</i> 方法&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 此方法将给定源缓冲区中剩余的字节传输到此缓冲区。如果源缓冲区中剩余的字节数多于此缓冲区中的剩余空间，即
     * <tt>src.remaining()</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>，
     * 则不会传输任何字节，并抛出 {@link
     * BufferOverflowException}。
     *
     * <p> 否则，此方法从给定缓冲区的当前位置开始，复制
     * <i>n</i>&nbsp;=&nbsp;<tt>src.remaining()</tt> 个字节到此缓冲区。两个缓冲区的位置都会增加 <i>n</i>。
     *
     * <p> 换句话说，形式为
     * <tt>dst.put(src)</tt> 的此方法调用与以下循环具有完全相同的效果
     *
     * <pre>
     *     while (src.hasRemaining())
     *         dst.put(src.get()); </pre>
     *
     * 除了它首先检查此缓冲区是否有足够的空间，并且可能更高效。
     *
     * @param  src
     *         从中读取字节的源缓冲区；
     *         不能是此缓冲区
     *
     * @return  此缓冲区
     *
     * @throws  BufferOverflowException
     *          如果此缓冲区的空间不足以容纳源缓冲区中的剩余字节
     *
     * @throws  IllegalArgumentException
     *          如果源缓冲区是此缓冲区
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区是只读的
     */
    public ByteBuffer put(ByteBuffer src) {
        if (src == this)
            throw new IllegalArgumentException();
        if (isReadOnly())
            throw new ReadOnlyBufferException();
        int n = src.remaining();
        if (n > remaining())
            throw new BufferOverflowException();
        for (int i = 0; i < n; i++)
            put(src.get());
        return this;
    }

    /**
     * 相对批量 <i>put</i> 方法&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 此方法从给定的源数组中将字节传输到此缓冲区。如果要从数组中复制的字节数多于此缓冲区中的剩余空间，即
     * <tt>length</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>，则不会传输任何字节，并抛出 {@link BufferOverflowException}。
     *
     * <p> 否则，此方法从给定数组的当前位置开始，复制 <tt>length</tt> 个字节到此缓冲区。此缓冲区的位置会增加 <tt>length</tt>。
     *
     * <p> 换句话说，形式为
     * <tt>dst.put(src,&nbsp;off,&nbsp;len)</tt> 的此方法调用与以下循环具有完全相同的效果
     *
     * <pre>{@code
     *     for (int i = off; i < off + len; i++)
     *         dst.put(a[i]);
     * }</pre>
     *
     * 除了它首先检查此缓冲区是否有足够的空间，并且可能更高效。
     *
     * @param  src
     *         从中读取字节的数组
     *
     * @param  offset
     *         数组中第一个要读取的字节的偏移量；
     *         必须是非负数且不大于 <tt>array.length</tt>
     *
     * @param  length
     *         要从给定数组中读取的字节数；
     *         必须是非负数且不大于
     *         <tt>array.length - offset</tt>
     *
     * @return  此缓冲区
     *
     * @throws  BufferOverflowException
     *          如果此缓冲区的空间不足
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>offset</tt> 和 <tt>length</tt> 参数的前置条件不成立
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区是只读的
     */
    public ByteBuffer put(byte[] src, int offset, int length) {
        checkBounds(offset, length, src.length);
        if (length > remaining())
            throw new BufferOverflowException();
        int end = offset + length;
        for (int i = offset; i < end; i++)
            this.put(src[i]);
        return this;
    }

    /**
     * 相对批量 <i>put</i> 方法&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 此方法将给定源字节数组的全部内容传输到此缓冲区。形式为 <tt>dst.put(a)</tt> 的此方法调用的行为与
     *
     * <pre>
     *     dst.put(a, 0, a.length) </pre>
     *
     * 完全相同。
     *
     * @param   src
     *          源数组
     *
     * @return  此缓冲区
     *
     * @throws  BufferOverflowException
     *          如果此缓冲区的空间不足
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区是只读的
     */
    public final ByteBuffer put(byte[] src) {
        return put(src, 0, src.length);
    }































































































    // -- 其他内容 --

    /**
     * 告知此缓冲区是否由可访问的字节数组支持。
     *
     * <p> 如果此方法返回 <tt>true</tt>，则可以安全地调用 {@link #array() array}
     * 和 {@link #arrayOffset() arrayOffset} 方法。
     * </p>
     *
     * @return  <tt>true</tt>，当且仅当此缓冲区由数组支持且不是只读时
     */
    public final boolean hasArray() {
        return (hb != null) && !isReadOnly;
    }

    /**
     * 返回支持此缓冲区的字节数组&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 对此缓冲区内容的修改将导致返回数组内容的修改，反之亦然。
     *
     * <p> 在调用此方法之前调用 {@link #hasArray hasArray} 方法以确保此缓冲区具有可访问的备份数组。 </p>
     *
     * @return  支持此缓冲区的数组
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区由数组支持但为只读
     *
     * @throws  UnsupportedOperationException
     *          如果此缓冲区不由可访问的数组支持
     */
    public final byte[] array() {
        if (hb == null)
            throw new UnsupportedOperationException();
        if (isReadOnly)
            throw new ReadOnlyBufferException();
        return hb;
    }

                /**
     * 返回此缓冲区支持的数组中第一个缓冲区元素的偏移量（可选操作）。
     *
     * <p> 如果此缓冲区由数组支持，则缓冲区位置 <i>p</i>
     * 对应于数组索引 <i>p</i>&nbsp;+&nbsp;<tt>arrayOffset()</tt>。
     *
     * <p> 在调用此方法之前，调用 {@link #hasArray hasArray} 方法以确保此缓冲区具有可访问的支持数组。 </p>
     *
     * @return  此缓冲区数组中第一个缓冲区元素的偏移量
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区由数组支持但为只读
     *
     * @throws  UnsupportedOperationException
     *          如果此缓冲区没有可访问的支持数组
     */
    public final int arrayOffset() {
        if (hb == null)
            throw new UnsupportedOperationException();
        if (isReadOnly)
            throw new ReadOnlyBufferException();
        return offset;
    }

    /**
     * 压缩此缓冲区（可选操作）。
     *
     * <p> 缓冲区当前位置和其限制之间的字节，如果有，将被复制到缓冲区的开头。也就是说，
     * 位于索引 <i>p</i>&nbsp;=&nbsp;<tt>position()</tt> 的字节将被复制到索引零，
     * 位于索引 <i>p</i>&nbsp;+&nbsp;1 的字节将被复制到索引一，依此类推，直到位于索引
     * <tt>limit()</tt>&nbsp;-&nbsp;1 的字节被复制到索引
     * <i>n</i>&nbsp;=&nbsp;<tt>limit()</tt>&nbsp;-&nbsp;<tt>1</tt>&nbsp;-&nbsp;<i>p</i>。
     * 然后将缓冲区的位置设置为 <i>n+1</i>，其限制设置为它的容量。如果已定义，标记将被丢弃。
     *
     * <p> 缓冲区的位置被设置为复制的字节数，而不是零，因此可以立即调用此方法后跟另一个相对 <i>put</i> 方法。 </p>
     *

     *
     * <p> 在从缓冲区写入数据后调用此方法，以防写入不完整。例如，以下循环通过缓冲区 <tt>buf</tt> 将字节从一个通道复制到另一个通道：
     *
     * <blockquote><pre>{@code
     *   buf.clear();          // 准备缓冲区以使用
     *   while (in.read(buf) >= 0 || buf.position != 0) {
     *       buf.flip();
     *       out.write(buf);
     *       buf.compact();    // 以防部分写入
     *   }
     * }</pre></blockquote>
     *

     *
     * @return  此缓冲区
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区为只读
     */
    public abstract ByteBuffer compact();

    /**
     * 告知此字节缓冲区是否为直接缓冲区。
     *
     * @return  如果且仅如果此缓冲区为直接缓冲区，则返回 <tt>true</tt>
     */
    public abstract boolean isDirect();



    /**
     * 返回一个总结此缓冲区状态的字符串。
     *
     * @return  一个总结字符串
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append("[pos=");
        sb.append(position());
        sb.append(" lim=");
        sb.append(limit());
        sb.append(" cap=");
        sb.append(capacity());
        sb.append("]");
        return sb.toString();
    }






    /**
     * 返回此缓冲区的当前哈希码。
     *
     * <p> 字节缓冲区的哈希码仅取决于其剩余元素；也就是说，从 <tt>position()</tt> 到
     * 包括 <tt>limit()</tt>&nbsp;-&nbsp;<tt>1</tt> 的元素。
     *
     * <p> 由于缓冲区的哈希码依赖于内容，因此除非已知其内容不会改变，否则不建议将缓冲区用作哈希映射或类似数据结构中的键。 </p>
     *
     * @return  此缓冲区的当前哈希码
     */
    public int hashCode() {
        int h = 1;
        int p = position();
        for (int i = limit() - 1; i >= p; i--)



            h = 31 * h + (int)get(i);

        return h;
    }

    /**
     * 告知此缓冲区是否等于另一个对象。
     *
     * <p> 两个字节缓冲区相等，当且仅当，
     *
     * <ol>
     *
     *   <li><p> 它们具有相同的元素类型，  </p></li>
     *
     *   <li><p> 它们具有相同数量的剩余元素，以及
     *   </p></li>
     *
     *   <li><p> 两个剩余元素序列，独立于它们在各自缓冲区中的起始位置，逐点相等。
     *   </p></li>
     *
     * </ol>
     *
     * <p> 字节缓冲区不等于任何其他类型的对象。 </p>
     *
     * @param  ob  要与此缓冲区比较的对象
     *
     * @return  如果且仅如果此缓冲区等于给定对象，则返回 <tt>true</tt>
     */
    public boolean equals(Object ob) {
        if (this == ob)
            return true;
        if (!(ob instanceof ByteBuffer))
            return false;
        ByteBuffer that = (ByteBuffer)ob;
        int thisPos = this.position();
        int thisRem = this.limit() - thisPos;
        int thatPos = that.position();
        int thatRem = that.limit() - thatPos;
        if (thisRem < 0 || thisRem != thatRem)
            return false;
        for (int i = this.limit() - 1, j = that.limit() - 1; i >= thisPos; i--, j--)
            if (!equals(this.get(i), that.get(j)))
                return false;
        return true;
    }

    private static boolean equals(byte x, byte y) {



        return x == y;

    }

    /**
     * 比较此缓冲区与另一个缓冲区。
     *
     * <p> 两个字节缓冲区通过比较它们的剩余元素序列的字典顺序来比较，不考虑每个序列在其对应缓冲区中的起始位置。
     *
     * <p> {@code byte} 元素对的比较如同调用了
     * {@link Byte#compare(byte,byte)}。
     *
     * <p> 字节缓冲区不可与任何其他类型的对象比较。
     *
     * @return  如果此缓冲区小于、等于或大于给定缓冲区，则返回一个负整数、零或正整数
     */
    public int compareTo(ByteBuffer that) {
        int thisPos = this.position();
        int thisRem = this.limit() - thisPos;
        int thatPos = that.position();
        int thatRem = that.limit() - thatPos;
        int length = Math.min(thisRem, thatRem);
        if (length < 0)
            return -1;
        int n = thisPos + Math.min(thisRem, thatRem);
        for (int i = thisPos, j = thatPos; i < n; i++, j++) {
            int cmp = compare(this.get(i), that.get(j));
            if (cmp != 0)
                return cmp;
        }
        return thisRem - thatRem;
    }


                private static int compare(byte x, byte y) {






        return Byte.compare(x, y);

    }

    // -- 其他字符相关 --


































































































































































































    // -- 其他字节相关：二进制数据访问 --





















    boolean bigEndian                                   // 包私有
        = true;
    boolean nativeByteOrder                             // 包私有
        = (Bits.byteOrder() == ByteOrder.BIG_ENDIAN);

    /**
     * 获取此缓冲区的字节顺序。
     *
     * <p> 字节顺序用于读取或写入多字节值，以及创建此字节缓冲区的视图缓冲区。新创建的字节缓冲区的顺序始终为 {@link ByteOrder#BIG_ENDIAN
     * BIG_ENDIAN}。 </p>
     *
     * @return  此缓冲区的字节顺序
     */
    public final ByteOrder order() {
        return bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    }

    /**
     * 修改此缓冲区的字节顺序。
     *
     * @param  bo
     *         新的字节顺序，
     *         可以是 {@link ByteOrder#BIG_ENDIAN BIG_ENDIAN}
     *         或 {@link ByteOrder#LITTLE_ENDIAN LITTLE_ENDIAN}
     *
     * @return  此缓冲区
     */
    public final ByteBuffer order(ByteOrder bo) {
        bigEndian = (bo == ByteOrder.BIG_ENDIAN);
        nativeByteOrder =
            (bigEndian == (Bits.byteOrder() == ByteOrder.BIG_ENDIAN));
        return this;
    }

    // 未检查的访问器，供 ByteBufferAs-X-Buffer 类使用
    //
    abstract byte _get(int i);                          // 包私有
    abstract void _put(int i, byte b);                  // 包私有


    /**
     * 相对 <i>get</i> 方法，用于读取 char 值。
     *
     * <p> 从此缓冲区的当前位置读取接下来的两个字节，根据当前字节顺序将它们组合成一个 char 值，然后将位置增加两个字节。 </p>
     *
     * @return  缓冲区当前位置的 char 值
     *
     * @throws  BufferUnderflowException
     *          如果此缓冲区中剩余的字节数少于两个
     */
    public abstract char getChar();

    /**
     * 相对 <i>put</i> 方法，用于写入 char
     * 值&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 以当前字节顺序将包含给定 char 值的两个字节写入此缓冲区的当前位置，然后将位置增加两个字节。 </p>
     *
     * @param  value
     *         要写入的 char 值
     *
     * @return  此缓冲区
     *
     * @throws  BufferOverflowException
     *          如果此缓冲区中剩余的字节数少于两个
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区是只读的
     */
    public abstract ByteBuffer putChar(char value);

    /**
     * 绝对 <i>get</i> 方法，用于读取 char 值。
     *
     * <p> 从给定索引读取两个字节，根据当前字节顺序将它们组合成一个 char 值。 </p>
     *
     * @param  index
     *         将从中读取字节的索引
     *
     * @return  给定索引处的 char 值
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负
     *          或不小于缓冲区的限制，减去一个
     */
    public abstract char getChar(int index);

    /**
     * 绝对 <i>put</i> 方法，用于写入 char
     * 值&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 以当前字节顺序将包含给定 char 值的两个字节写入此缓冲区的给定索引。 </p>
     *
     * @param  index
     *         将写入字节的索引
     *
     * @param  value
     *         要写入的 char 值
     *
     * @return  此缓冲区
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负
     *          或不小于缓冲区的限制，减去一个
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区是只读的
     */
    public abstract ByteBuffer putChar(int index, char value);

    /**
     * 创建此字节缓冲区的 char 缓冲区视图。
     *
     * <p> 新缓冲区的内容将从此缓冲区的当前位置开始。对此缓冲区内容的更改将在新缓冲区中可见，反之亦然；两个缓冲区的位置、限制和标记值将独立。 </p>
     *
     * <p> 新缓冲区的位置将为零，其容量和限制将为此缓冲区中剩余字节数除以
     * 二，其标记将未定义。新缓冲区将直接，当且仅当此缓冲区直接时，它将是只读的，当且仅当此缓冲区是只读的。 </p>
     *
     * @return  一个新的 char 缓冲区
     */
    public abstract CharBuffer asCharBuffer();


    /**
     * 相对 <i>get</i> 方法，用于读取 short 值。
     *
     * <p> 从此缓冲区的当前位置读取接下来的两个字节，根据当前字节顺序将它们组合成一个 short 值，然后将位置增加两个字节。 </p>
     *
     * @return  缓冲区当前位置的 short 值
     *
     * @throws  BufferUnderflowException
     *          如果此缓冲区中剩余的字节数少于两个
     */
    public abstract short getShort();

    /**
     * 相对 <i>put</i> 方法，用于写入 short
     * 值&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 以当前字节顺序将包含给定 short 值的两个字节写入此缓冲区的当前位置，然后将位置增加两个字节。 </p>
     *
     * @param  value
     *         要写入的 short 值
     *
     * @return  此缓冲区
     *
     * @throws  BufferOverflowException
     *          如果此缓冲区中剩余的字节数少于两个
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区是只读的
     */
    public abstract ByteBuffer putShort(short value);

                /**
     * 绝对 <i>get</i> 方法，用于读取一个 short 值。
     *
     * <p> 在给定索引处读取两个字节，根据当前字节顺序将它们组合成一个 short 值。 </p>
     *
     * @param  index
     *         将要读取字节的索引
     *
     * @return  给定索引处的 short 值
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负数
     *          或者不小于缓冲区的限制，减去一
     */
    public abstract short getShort(int index);

    /**
     * 绝对 <i>put</i> 方法，用于写入一个 short
     * 值&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 将包含给定 short 值的两个字节，按照当前字节顺序，写入此缓冲区的给定索引处。 </p>
     *
     * @param  index
     *         将要写入字节的索引
     *
     * @param  value
     *         要写入的 short 值
     *
     * @return  本缓冲区
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负数
     *          或者不小于缓冲区的限制，减去一
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区是只读的
     */
    public abstract ByteBuffer putShort(int index, short value);

    /**
     * 创建此字节缓冲区的 short 缓冲区视图。
     *
     * <p> 新缓冲区的内容将从本缓冲区的当前位置开始。 对本缓冲区内容的更改将在新缓冲区中可见，反之亦然；两个缓冲区的位置、限制和标记值将独立。 </p>
     *
     * <p> 新缓冲区的位置将为零，其容量和限制将为本缓冲区中剩余字节数除以
     * 二，其标记将未定义。 新缓冲区将直接，当且仅当本缓冲区直接时；它将是只读的，当且仅当本缓冲区是只读的。 </p>
     *
     * @return  一个新的 short 缓冲区
     */
    public abstract ShortBuffer asShortBuffer();


    /**
     * 相对 <i>get</i> 方法，用于读取一个 int 值。
     *
     * <p> 从本缓冲区的当前位置读取接下来的四个字节，根据当前字节顺序将它们组合成一个 int 值，然后将位置增加四个字节。 </p>
     *
     * @return  缓冲区当前位置的 int 值
     *
     * @throws  BufferUnderflowException
     *          如果本缓冲区中剩余的字节少于四个
     */
    public abstract int getInt();

    /**
     * 相对 <i>put</i> 方法，用于写入一个 int
     * 值&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 将包含给定 int 值的四个字节，按照当前字节顺序，写入此缓冲区的当前位置，然后将位置增加四个字节。 </p>
     *
     * @param  value
     *         要写入的 int 值
     *
     * @return  本缓冲区
     *
     * @throws  BufferOverflowException
     *          如果本缓冲区中剩余的字节少于四个
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区是只读的
     */
    public abstract ByteBuffer putInt(int value);

    /**
     * 绝对 <i>get</i> 方法，用于读取一个 int 值。
     *
     * <p> 在给定索引处读取四个字节，根据当前字节顺序将它们组合成一个 int 值。 </p>
     *
     * @param  index
     *         将要读取字节的索引
     *
     * @return  给定索引处的 int 值
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负数
     *          或者不小于缓冲区的限制，减去三
     */
    public abstract int getInt(int index);

    /**
     * 绝对 <i>put</i> 方法，用于写入一个 int
     * 值&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 将包含给定 int 值的四个字节，按照当前字节顺序，写入此缓冲区的给定索引处。 </p>
     *
     * @param  index
     *         将要写入字节的索引
     *
     * @param  value
     *         要写入的 int 值
     *
     * @return  本缓冲区
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负数
     *          或者不小于缓冲区的限制，减去三
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区是只读的
     */
    public abstract ByteBuffer putInt(int index, int value);

    /**
     * 创建此字节缓冲区的 int 缓冲区视图。
     *
     * <p> 新缓冲区的内容将从本缓冲区的当前位置开始。 对本缓冲区内容的更改将在新缓冲区中可见，反之亦然；两个缓冲区的位置、限制和标记值将独立。 </p>
     *
     * <p> 新缓冲区的位置将为零，其容量和限制将为本缓冲区中剩余字节数除以
     * 四，其标记将未定义。 新缓冲区将直接，当且仅当本缓冲区直接时；它将是只读的，当且仅当本缓冲区是只读的。 </p>
     *
     * @return  一个新的 int 缓冲区
     */
    public abstract IntBuffer asIntBuffer();


    /**
     * 相对 <i>get</i> 方法，用于读取一个 long 值。
     *
     * <p> 从本缓冲区的当前位置读取接下来的八个字节，根据当前字节顺序将它们组合成一个 long 值，然后将位置增加八个字节。 </p>
     *
     * @return  缓冲区当前位置的 long 值
     *
     * @throws  BufferUnderflowException
     *          如果本缓冲区中剩余的字节少于八个
     */
    public abstract long getLong();

    /**
     * 相对 <i>put</i> 方法，用于写入一个 long
     * 值&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 将包含给定 long 值的八个字节，按照当前字节顺序，写入此缓冲区的当前位置，然后将位置增加八个字节。 </p>
     *
     * @param  value
     *         要写入的 long 值
     *
     * @return  本缓冲区
     *
     * @throws  BufferOverflowException
     *          如果本缓冲区中剩余的字节少于八个
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区是只读的
     */
    public abstract ByteBuffer putLong(long value);

                /**
     * 绝对 <i>get</i> 方法，用于读取一个 long 值。
     *
     * <p> 从给定索引处读取八个字节，根据当前字节序将它们组合成一个 long 值。 </p>
     *
     * @param  index
     *         将要读取字节的索引
     *
     * @return  给定索引处的 long 值
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负数
     *          或不小于缓冲区的限制值，减去七
     */
    public abstract long getLong(int index);

    /**
     * 绝对 <i>put</i> 方法，用于写入一个 long
     * 值&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 根据当前字节序，将包含给定 long 值的八个字节写入此缓冲区的给定索引处。 </p>
     *
     * @param  index
     *         将要写入字节的索引
     *
     * @param  value
     *         要写入的 long 值
     *
     * @return  此缓冲区
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负数
     *          或不小于缓冲区的限制值，减去七
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区是只读的
     */
    public abstract ByteBuffer putLong(int index, long value);

    /**
     * 创建此字节缓冲区的 long 缓冲区视图。
     *
     * <p> 新缓冲区的内容将从此缓冲区的当前位置开始。对此缓冲区内容的更改将在新缓冲区中可见，反之亦然；两个缓冲区的位置、限制和标记值将是独立的。
     *
     * <p> 新缓冲区的位置将为零，其容量和限制将为此缓冲区中剩余字节数除以
     * 八，其标记将未定义。新缓冲区将为直接缓冲区，当且仅当此缓冲区为直接缓冲区时；它将为只读缓冲区，当且仅当此缓冲区为只读缓冲区时。 </p>
     *
     * @return  一个新的 long 缓冲区
     */
    public abstract LongBuffer asLongBuffer();


    /**
     * 相对 <i>get</i> 方法，用于读取一个 float 值。
     *
     * <p> 从此缓冲区的当前位置读取接下来的四个字节，根据当前字节序将它们组合成一个 float 值，然后将位置增加四个字节。 </p>
     *
     * @return  缓冲区当前位置的 float 值
     *
     * @throws  BufferUnderflowException
     *          如果此缓冲区中剩余的字节数少于四个
     */
    public abstract float getFloat();

    /**
     * 相对 <i>put</i> 方法，用于写入一个 float
     * 值&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 根据当前字节序，将包含给定 float 值的四个字节写入此缓冲区的当前位置，然后将位置增加四个字节。 </p>
     *
     * @param  value
     *         要写入的 float 值
     *
     * @return  此缓冲区
     *
     * @throws  BufferOverflowException
     *          如果此缓冲区中剩余的字节数少于四个
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区是只读的
     */
    public abstract ByteBuffer putFloat(float value);

    /**
     * 绝对 <i>get</i> 方法，用于读取一个 float 值。
     *
     * <p> 从给定索引处读取四个字节，根据当前字节序将它们组合成一个 float 值。 </p>
     *
     * @param  index
     *         将要读取字节的索引
     *
     * @return  给定索引处的 float 值
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负数
     *          或不小于缓冲区的限制值，减去三
     */
    public abstract float getFloat(int index);

    /**
     * 绝对 <i>put</i> 方法，用于写入一个 float
     * 值&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 根据当前字节序，将包含给定 float 值的四个字节写入此缓冲区的给定索引处。 </p>
     *
     * @param  index
     *         将要写入字节的索引
     *
     * @param  value
     *         要写入的 float 值
     *
     * @return  此缓冲区
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负数
     *          或不小于缓冲区的限制值，减去三
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区是只读的
     */
    public abstract ByteBuffer putFloat(int index, float value);

    /**
     * 创建此字节缓冲区的 float 缓冲区视图。
     *
     * <p> 新缓冲区的内容将从此缓冲区的当前位置开始。对此缓冲区内容的更改将在新缓冲区中可见，反之亦然；两个缓冲区的位置、限制和标记值将是独立的。
     *
     * <p> 新缓冲区的位置将为零，其容量和限制将为此缓冲区中剩余字节数除以
     * 四，其标记将未定义。新缓冲区将为直接缓冲区，当且仅当此缓冲区为直接缓冲区时；它将为只读缓冲区，当且仅当此缓冲区为只读缓冲区时。 </p>
     *
     * @return  一个新的 float 缓冲区
     */
    public abstract FloatBuffer asFloatBuffer();


    /**
     * 相对 <i>get</i> 方法，用于读取一个 double 值。
     *
     * <p> 从此缓冲区的当前位置读取接下来的八个字节，根据当前字节序将它们组合成一个 double 值，然后将位置增加八个字节。 </p>
     *
     * @return  缓冲区当前位置的 double 值
     *
     * @throws  BufferUnderflowException
     *          如果此缓冲区中剩余的字节数少于八个
     */
    public abstract double getDouble();

                /**
     * 相对 <i>put</i> 方法，用于写入一个 double
     * 值&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 以当前字节序，将给定的 double 值写入此缓冲区的当前位置，然后将位置增加八个字节。 </p>
     *
     * @param  value
     *         要写入的 double 值
     *
     * @return  该缓冲区
     *
     * @throws  BufferOverflowException
     *          如果此缓冲区中剩余的字节少于八个
     *
     * @throws  ReadOnlyBufferException
     *          如果该缓冲区是只读的
     */
    public abstract ByteBuffer putDouble(double value);

    /**
     * 绝对 <i>get</i> 方法，用于读取一个 double 值。
     *
     * <p> 从给定索引处读取八个字节，根据当前字节序将其组合成一个 double 值。 </p>
     *
     * @param  index
     *         读取字节的索引
     *
     * @return  给定索引处的 double 值
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负
     *          或不小于缓冲区的限制，减去七个
     */
    public abstract double getDouble(int index);

    /**
     * 绝对 <i>put</i> 方法，用于写入一个 double
     * 值&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 以当前字节序，将给定的 double 值写入此缓冲区的给定索引处。 </p>
     *
     * @param  index
     *         写入字节的索引
     *
     * @param  value
     *         要写入的 double 值
     *
     * @return  该缓冲区
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负
     *          或不小于缓冲区的限制，减去七个
     *
     * @throws  ReadOnlyBufferException
     *          如果该缓冲区是只读的
     */
    public abstract ByteBuffer putDouble(int index, double value);

    /**
     * 创建此字节缓冲区的 double 缓冲区视图。
     *
     * <p> 新缓冲区的内容将从该缓冲区的当前位置开始。 对此缓冲区内容的更改将在新缓冲区中可见，反之亦然；两个缓冲区的位置、限制和标记值将独立。 </p>
     *
     * <p> 新缓冲区的位置将为零，其容量和限制将为此缓冲区中剩余字节数除以
     * 八，其标记将未定义。 新缓冲区将直接，当且仅当该缓冲区直接，且当且仅当该缓冲区为只读时，新缓冲区也为只读。 </p>
     *
     * @return  一个新的 double 缓冲区
     */
    public abstract DoubleBuffer asDoubleBuffer();

}
