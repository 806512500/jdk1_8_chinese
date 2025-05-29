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

// -- 本文件由机械生成：请勿编辑！ -- //

package java.nio;










/**
 * 一个 long 缓冲区。
 *
 * <p> 本类定义了对 long 缓冲区的四类操作：
 *
 * <ul>
 *
 *   <li><p> 读取和写入单个 long 的绝对和相对 {@link #get() <i>get</i>} 和
 *   {@link #put(long) <i>put</i>} 方法； </p></li>
 *
 *   <li><p> 从缓冲区传输连续的 long 序列到数组的相对 {@link #get(long[]) <i>bulk get</i>}
 *   方法；和</p></li>
 *
 *   <li><p> 从 long 数组或其他 long
 *   缓冲区传输连续的 long 序列到此缓冲区的相对 {@link #put(long[]) <i>bulk put</i>}
 *   方法；和 </p></li>
 *












 *
 *   <li><p> 对 long 缓冲区进行 {@link #compact 压缩}，{@link
 *   #duplicate 复制} 和 {@link #slice 切片}
 *   的方法。 </p></li>
 *
 * </ul>
 *
 * <p> long 缓冲区可以通过 {@link #allocate
 * <i>分配</i>} 创建，这会为缓冲区的内容分配空间，通过 {@link #wrap(long[]) <i>包装</i>} 现有的
 * long 数组，或者通过创建现有字节缓冲区的
 * <a href="ByteBuffer.html#views"><i>视图</i></a>。
 *

 *



































































































*

 *
 * <p> 像字节缓冲区一样，long 缓冲区可以是 <a
 * href="ByteBuffer.html#direct"><i>直接的</i> 或 <i>非直接的</i></a>。通过本类的 <tt>wrap</tt>
 * 方法创建的 long 缓冲区将是非直接的。作为字节缓冲区视图创建的 long 缓冲区将直接，当且仅当字节缓冲区本身是直接的。是否
 * long 缓冲区是直接的可以通过调用 {@link
 * #isDirect isDirect} 方法来确定。 </p>
 *

*








 *



 *
 * <p> 本类中没有其他值可返回的方法被指定为返回调用它们的缓冲区。这允许方法调用链式调用。
 *































 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
 * @since 1.4
 */

public abstract class LongBuffer
    extends Buffer
    implements Comparable<LongBuffer>
{

    // 这些字段在这里声明而不是在 Heap-X-Buffer 中声明，以减少访问这些
    // 值所需的虚拟方法调用次数，这在编码小缓冲区时特别昂贵。
    //
    final long[] hb;                  // 仅对堆缓冲区非空
    final int offset;
    boolean isReadOnly;                 // 仅对堆缓冲区有效

    // 使用给定的标记、位置、限制、容量、后备数组和数组偏移创建新的缓冲区
    //
    LongBuffer(int mark, int pos, int lim, int cap,   // 包私有
                 long[] hb, int offset)
    {
        super(mark, pos, lim, cap);
        this.hb = hb;
        this.offset = offset;
    }

    // 使用给定的标记、位置、限制和容量创建新的缓冲区
    //
    LongBuffer(int mark, int pos, int lim, int cap) { // 包私有
        this(mark, pos, lim, cap, null, 0);
    }

























    /**
     * 分配一个新的 long 缓冲区。
     *
     * <p> 新缓冲区的位置将为零，其限制将为其容量，其标记将未定义，每个元素将初始化为零。它将有一个 {@link #array 后备数组}，
     * 其 {@link #arrayOffset 数组偏移} 将为零。
     *
     * @param  capacity
     *         新缓冲区的容量，以 long 为单位
     *
     * @return  新的 long 缓冲区
     *
     * @throws  IllegalArgumentException
     *          如果 <tt>capacity</tt> 是负整数
     */
    public static LongBuffer allocate(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        return new HeapLongBuffer(capacity, capacity);
    }

    /**
     * 将 long 数组包装成缓冲区。
     *
     * <p> 新缓冲区将由给定的 long 数组支持；
     * 即，对缓冲区的修改将导致数组被修改，反之亦然。新缓冲区的容量将是
     * <tt>array.length</tt>，其位置将是 <tt>offset</tt>，其限制
     * 将是 <tt>offset + length</tt>，其标记将未定义。其
     * {@link #array 后备数组} 将是给定数组，其
     * {@link #arrayOffset 数组偏移} 将为零。 </p>
     *
     * @param  array
     *         将支持新缓冲区的数组
     *
     * @param  offset
     *         要使用的子数组的偏移；必须是非负数且不大于 <tt>array.length</tt>。新缓冲区的位置
     *         将设置为该值。
     *
     * @param  length
     *         要使用的子数组的长度；
     *         必须是非负数且不大于
     *         <tt>array.length - offset</tt>。
     *         新缓冲区的限制将设置为 <tt>offset + length</tt>。
     *
     * @return  新的 long 缓冲区
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>offset</tt> 和 <tt>length</tt>
     *          参数的前置条件不成立
     */
    public static LongBuffer wrap(long[] array,
                                    int offset, int length)
    {
        try {
            return new HeapLongBuffer(array, offset, length);
        } catch (IllegalArgumentException x) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * 将 long 数组包装成缓冲区。
     *
     * <p> 新缓冲区将由给定的 long 数组支持；
     * 即，对缓冲区的修改将导致数组被修改，反之亦然。新缓冲区的容量和限制将是
     * <tt>array.length</tt>，其位置将为零，其标记将未定义。其
     * {@link #array 后备数组} 将是
     * 给定数组，其 {@link #arrayOffset 数组偏移} 将
     * 为零。 </p>
     *
     * @param  array
     *         将支持此缓冲区的数组
     *
     * @return  新的 long 缓冲区
     */
    public static LongBuffer wrap(long[] array) {
        return wrap(array, 0, array.length);
    }


    /**
     * 创建一个新 long 缓冲区，其内容是此缓冲区内容的共享子序列。
     *
     * <p> 新缓冲区的内容将从该缓冲区的当前位置开始。对本缓冲区内容的更改将在新缓冲区中可见，反之亦然；两个缓冲区的位置、限制和标记值将独立。
     *
     * <p> 新缓冲区的位置将为零，其容量和限制将为本缓冲区中剩余的 long 数量，其标记将未定义。新缓冲区将直接存在，当且仅当此缓冲区直接存在时，它将是只读的，当且仅当此缓冲区是只读的。 </p>
     *
     * @return  新的 long 缓冲区
     */
    public abstract LongBuffer slice();

    /**
     * 创建一个与本缓冲区内容共享的新 long 缓冲区。
     *
     * <p> 新缓冲区的内容将与本缓冲区的内容相同。对本缓冲区内容的更改将在新缓冲区中可见，反之亦然；两个缓冲区的位置、限制和标记值将独立。
     *
     * <p> 新缓冲区的容量、限制、位置和标记值将与本缓冲区相同。新缓冲区将直接存在，当且仅当此缓冲区直接存在时，它将是只读的，当且仅当此缓冲区是只读的。 </p>
     *
     * @return  新的 long 缓冲区
     */
    public abstract LongBuffer duplicate();

    /**
     * 创建一个新的只读 long 缓冲区，其内容与本缓冲区共享。
     *
     * <p> 新缓冲区的内容将与本缓冲区的内容相同。对本缓冲区内容的更改将在新缓冲区中可见；然而，新缓冲区本身将是只读的，不允许修改共享内容。两个缓冲区的位置、限制和标记值将独立。
     *
     * <p> 新缓冲区的容量、限制、位置和标记值将与本缓冲区相同。
     *
     * <p> 如果本缓冲区本身是只读的，那么此方法的行为与 {@link #duplicate duplicate} 方法完全相同。 </p>
     *
     * @return  新的只读 long 缓冲区
     */
    public abstract LongBuffer asReadOnlyBuffer();


    // -- 单例 get/put 方法 --

    /**
     * 相对 <i>get</i> 方法。读取此缓冲区当前位置的 long，然后递增位置。
     *
     * @return  缓冲区当前位置的 long
     *
     * @throws  BufferUnderflowException
     *          如果缓冲区的当前位置不小于其限制
     */
    public abstract long get();

    /**
     * 相对 <i>put</i> 方法&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 将给定的 long 写入此缓冲区的当前位置，然后递增位置。 </p>
     *
     * @param  l
     *         要写入的 long
     *
     * @return  此缓冲区
     *
     * @throws  BufferOverflowException
     *          如果此缓冲区的当前位置不小于其限制
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区是只读的
     */
    public abstract LongBuffer put(long l);

    /**
     * 绝对 <i>get</i> 方法。读取给定索引处的 long。
     *
     * @param  index
     *         要读取 long 的索引
     *
     * @return  给定索引处的 long
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负数
     *          或不小于缓冲区的限制
     */
    public abstract long get(int index);














    /**
     * 绝对 <i>put</i> 方法&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 将给定的 long 写入此缓冲区的给定索引处。 </p>
     *
     * @param  index
     *         要写入 long 的索引
     *
     * @param  l
     *         要写入的 long 值
     *
     * @return  此缓冲区
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负数
     *          或不小于缓冲区的限制
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区是只读的
     */
    public abstract LongBuffer put(int index, long l);


    // -- 批量 get 操作 --

    /**
     * 相对批量 <i>get</i> 方法。
     *
     * <p> 此方法将 long 从此缓冲区传输到给定的目标数组。如果缓冲区中剩余的 long 数量少于请求的数量，即 <tt>length</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>，则不会传输任何 long，并抛出 {@link BufferUnderflowException}。
     *
     * <p> 否则，此方法将 <tt>length</tt> 个 long 从此缓冲区复制到给定数组，从该缓冲区的当前位置开始，并在数组中给定的偏移量处开始。此缓冲区的位置然后递增 <tt>length</tt>。
     *
     * <p> 换句话说，形式为 <tt>src.get(dst,&nbsp;off,&nbsp;len)</tt> 的此方法调用与以下循环具有完全相同的效果
     *
     * <pre>{@code
     *     for (int i = off; i < off + len; i++)
     *         dst[i] = src.get():
     * }</pre>
     *
     * 除了它首先检查此缓冲区中是否有足够的 long，并且它可能更高效。
     *
     * @param  dst
     *         要写入 long 的数组
     *
     * @param  offset
     *         数组中第一个 long 要写入的偏移量；必须是非负数且不大于 <tt>dst.length</tt>
     *
     * @param  length
     *         要写入给定数组的最大 long 数量；必须是非负数且不大于 <tt>dst.length - offset</tt>
     *
     * @return  此缓冲区
     *
     * @throws  BufferUnderflowException
     *          如果此缓冲区中剩余的 long 数量少于 <tt>length</tt>
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>offset</tt> 和 <tt>length</tt> 参数的前置条件不成立
     */
    public LongBuffer get(long[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        if (length > remaining())
            throw new BufferUnderflowException();
        int end = offset + length;
        for (int i = offset; i < end; i++)
            dst[i] = get();
        return this;
    }


                /**
     * 相对批量 <i>get</i> 方法。
     *
     * <p> 此方法将 long 从该缓冲区传输到给定的目标数组。形式为
     * <tt>src.get(a)</tt> 的此方法调用与
     *
     * <pre>
     *     src.get(a, 0, a.length) </pre>
     *
     * 的调用行为完全相同。
     *
     * @param   dst
     *          目标数组
     *
     * @return  该缓冲区
     *
     * @throws  BufferUnderflowException
     *          如果该缓冲区中剩余的 long 数量少于 <tt>length</tt>
     */
    public LongBuffer get(long[] dst) {
        return get(dst, 0, dst.length);
    }


    // -- 批量 put 操作 --

    /**
     * 相对批量 <i>put</i> 方法&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 此方法将给定源缓冲区中剩余的 long 传输到该缓冲区。如果源缓冲区中剩余的 long 比该缓冲区中剩余的多，即
     * <tt>src.remaining()</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>，
     * 则不传输任何 long，并抛出 {@link
     * BufferOverflowException}。
     *
     * <p> 否则，此方法从给定缓冲区中复制
     * <i>n</i>&nbsp;=&nbsp;<tt>src.remaining()</tt> 个 long 到该缓冲区，从每个缓冲区的当前位置开始。
     * 两个缓冲区的位置都增加 <i>n</i>。
     *
     * <p> 换句话说，形式为
     * <tt>dst.put(src)</tt> 的此方法调用与循环
     *
     * <pre>
     *     while (src.hasRemaining())
     *         dst.put(src.get()); </pre>
     *
     * 的效果完全相同，只是它首先检查该缓冲区是否有足够的空间，并且可能更高效。
     *
     * @param  src
     *         从中读取 long 的源缓冲区；
     *         不能是该缓冲区
     *
     * @return  该缓冲区
     *
     * @throws  BufferOverflowException
     *          如果该缓冲区中没有足够的空间容纳源缓冲区中剩余的 long
     *
     * @throws  IllegalArgumentException
     *          如果源缓冲区是该缓冲区
     *
     * @throws  ReadOnlyBufferException
     *          如果该缓冲区是只读的
     */
    public LongBuffer put(LongBuffer src) {
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
     * <p> 此方法从给定的源数组中将 long 传输到该缓冲区。如果从数组中要复制的 long 比该缓冲区中剩余的多，即
     * <tt>length</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>，则不传输任何 long，并抛出 {@link BufferOverflowException}。
     *
     * <p> 否则，此方法从给定数组中复制 <tt>length</tt> 个 long 到该缓冲区，从数组中的给定偏移量开始，并从该缓冲区的当前位置开始。该缓冲区的位置增加 <tt>length</tt>。
     *
     * <p> 换句话说，形式为
     * <tt>dst.put(src,&nbsp;off,&nbsp;len)</tt> 的此方法调用与循环
     *
     * <pre>{@code
     *     for (int i = off; i < off + len; i++)
     *         dst.put(a[i]);
     * }</pre>
     *
     * 的效果完全相同，只是它首先检查该缓冲区是否有足够的空间，并且可能更高效。
     *
     * @param  src
     *         从中读取 long 的数组
     *
     * @param  offset
     *         数组中第一个要读取的 long 的偏移量；
     *         必须是非负数且不大于 <tt>array.length</tt>
     *
     * @param  length
     *         从给定数组中读取的 long 数量；
     *         必须是非负数且不大于
     *         <tt>array.length - offset</tt>
     *
     * @return  该缓冲区
     *
     * @throws  BufferOverflowException
     *          如果该缓冲区中没有足够的空间
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>offset</tt> 和 <tt>length</tt> 参数的前置条件不成立
     *
     * @throws  ReadOnlyBufferException
     *          如果该缓冲区是只读的
     */
    public LongBuffer put(long[] src, int offset, int length) {
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
     * <p> 此方法将给定源 long 数组的全部内容传输到该缓冲区。形式为
     * <tt>dst.put(a)</tt> 的此方法调用与
     *
     * <pre>
     *     dst.put(a, 0, a.length) </pre>
     *
     * 的调用行为完全相同。
     *
     * @param   src
     *          源数组
     *
     * @return  该缓冲区
     *
     * @throws  BufferOverflowException
     *          如果该缓冲区中没有足够的空间
     *
     * @throws  ReadOnlyBufferException
     *          如果该缓冲区是只读的
     */
    public final LongBuffer put(long[] src) {
        return put(src, 0, src.length);
    }































































































    // -- 其他内容 --

    /**
     * 告知该缓冲区是否由一个可访问的 long 数组支持。
     *
     * <p> 如果此方法返回 <tt>true</tt>，则可以安全地调用 {@link #array() array}
     * 和 {@link #arrayOffset() arrayOffset} 方法。
     * </p>
     *
     * @return  <tt>true</tt>，当且仅当该缓冲区由数组支持且不是只读时
     */
    public final boolean hasArray() {
        return (hb != null) && !isReadOnly;
    }


/**
 * 返回支持此缓冲区的 long 数组&nbsp;&nbsp;<i>(可选操作)</i>。
 *
 * <p> 对此缓冲区内容的修改将导致返回的数组内容被修改，反之亦然。
 *
 * <p> 在调用此方法之前调用 {@link #hasArray hasArray} 方法以确保此缓冲区具有可访问的后端数组。 </p>
 *
 * @return  支持此缓冲区的数组
 *
 * @throws  ReadOnlyBufferException
 *          如果此缓冲区由数组支持但只读
 *
 * @throws  UnsupportedOperationException
 *          如果此缓冲区没有由可访问的数组支持
 */
public final long[] array() {
    if (hb == null)
        throw new UnsupportedOperationException();
    if (isReadOnly)
        throw new ReadOnlyBufferException();
    return hb;
}

/**
 * 返回缓冲区后端数组中第一个缓冲区元素的偏移量&nbsp;&nbsp;<i>(可选操作)</i>。
 *
 * <p> 如果此缓冲区由数组支持，则缓冲区位置 <i>p</i>
 * 对应于数组索引 <i>p</i>&nbsp;+&nbsp;<tt>arrayOffset()</tt>。
 *
 * <p> 在调用此方法之前调用 {@link #hasArray hasArray} 方法以确保此缓冲区具有可访问的后端数组。 </p>
 *
 * @return  缓冲区数组中第一个缓冲区元素的偏移量
 *
 * @throws  ReadOnlyBufferException
 *          如果此缓冲区由数组支持但只读
 *
 * @throws  UnsupportedOperationException
 *          如果此缓冲区没有由可访问的数组支持
 */
public final int arrayOffset() {
    if (hb == null)
        throw new UnsupportedOperationException();
    if (isReadOnly)
        throw new ReadOnlyBufferException();
    return offset;
}

/**
 * 压缩此缓冲区&nbsp;&nbsp;<i>(可选操作)</i>。
 *
 * <p> 缓冲区当前位置和其限制之间的 longs，如果有，将被复制到缓冲区的开头。也就是说，
 * 索引 <i>p</i>&nbsp;=&nbsp;<tt>position()</tt> 处的 long 将被复制到索引零，
 * 索引 <i>p</i>&nbsp;+&nbsp;1 处的 long 将被复制到索引一，依此类推，
 * 直到索引 <tt>limit()</tt>&nbsp;-&nbsp;1 处的 long 被复制到索引
 * <i>n</i>&nbsp;=&nbsp;<tt>limit()</tt>&nbsp;-&nbsp;<tt>1</tt>&nbsp;-&nbsp;<i>p</i>。
 * 然后将缓冲区的位置设置为 <i>n+1</i>，其限制设置为其容量。如果定义了标记，则会被丢弃。
 *
 * <p> 缓冲区的位置被设置为复制的 longs 数量，而不是零，因此此方法的调用可以立即
 * 跟随另一个相对 <i>put</i> 方法的调用。 </p>
 *
















 *
 * @return  此缓冲区
 *
 * @throws  ReadOnlyBufferException
 *          如果此缓冲区是只读的
 */
public abstract LongBuffer compact();

/**
 * 告知此 long 缓冲区是否为直接缓冲区。
 *
 * @return  <tt>true</tt> 如果且仅如果此缓冲区是直接缓冲区
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
 * <p> long 缓冲区的哈希码仅取决于其剩余元素；也就是说，从 <tt>position()</tt> 到
 * 包括 <tt>limit()</tt>&nbsp;-&nbsp;<tt>1</tt> 的元素。
 *
 * <p> 由于缓冲区哈希码依赖于内容，因此除非已知其内容不会改变，否则不建议将缓冲区用作哈希映射或类似数据结构中的键。 </p>
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
 * <p> 两个 long 缓冲区相等当且仅当：
 *
 * <ol>
 *
 *   <li><p> 它们具有相同的元素类型，  </p></li>
 *
 *   <li><p> 它们具有相同数量的剩余元素，以及
 *   </p></li>
 *
 *   <li><p> 两个剩余元素序列，独立于它们在各自缓冲区中的起始位置，逐点相等。
 *
 *
 *
 *   </p></li>
 *
 * </ol>
 *
 * <p> long 缓冲区不等于任何其他类型的对象。 </p>
 *
 * @param  ob  要与此缓冲区进行比较的对象
 *
 * @return  <tt>true</tt> 如果且仅如果此缓冲区等于给定对象
 */
public boolean equals(Object ob) {
    if (this == ob)
        return true;
    if (!(ob instanceof LongBuffer))
        return false;
    LongBuffer that = (LongBuffer)ob;
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

private static boolean equals(long x, long y) {



    return x == y;

}

/**
 * 比较此缓冲区与另一个缓冲区。
 *
 * <p> 两个 long 缓冲区通过比较其剩余元素序列的字典序来比较，不考虑每个序列在其对应缓冲区中的起始位置。


     * {@code long} 元素对的比较方式如同调用了 {@link Long#compare(long,long)}。

     *
     * <p> 一个长整型缓冲区不能与任何其他类型的对象进行比较。
     *
     * @return  如果此缓冲区小于、等于或大于给定缓冲区，则分别返回负整数、零或正整数
     */
    public int compareTo(LongBuffer that) {
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

    private static int compare(long x, long y) {






        return Long.compare(x, y);

    }

    // -- 其他字符相关的内容 --


































































































































































































    // -- 其他字节相关的内容：访问二进制数据 --



    /**
     * 获取此缓冲区的字节顺序。
     *
     * <p> 通过分配或包装现有 <tt>long</tt> 数组创建的长整型缓冲区的字节顺序是底层硬件的 {@link
     * ByteOrder#nativeOrder 本地顺序}。作为字节缓冲区的 <a
     * href="ByteBuffer.html#views">视图</a> 创建的长整型缓冲区的字节顺序是在创建视图时字节缓冲区的字节顺序。 </p>
     *
     * @return  此缓冲区的字节顺序
     */
    public abstract ByteOrder order();

































































}
