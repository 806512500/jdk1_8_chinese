/*
 * 版权所有 (c) 2000, 2013，Oracle 和/或其附属公司。保留所有权利。
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

// -- 本文件由机器生成：请勿编辑！-- //

package java.nio;










/**
 * 一个 int 缓冲区。
 *
 * <p> 本类定义了对 int 缓冲区的四类操作：
 *
 * <ul>
 *
 *   <li><p> 读取和写入单个 int 的绝对和相对 {@link #get() <i>get</i>} 和
 *   {@link #put(int) <i>put</i>} 方法； </p></li>
 *
 *   <li><p> 将连续的 int 序列从该缓冲区传输到数组的相对 {@link #get(int[]) <i>bulk get</i>}
 *   方法；以及</p></li>
 *
 *   <li><p> 将 int 数组或其他 int
 *   缓冲区中的连续 int 序列传输到该缓冲区的相对 {@link #put(int[]) <i>bulk put</i>}
 *   方法； </p></li>
 *












 *
 *   <li><p> 用于 {@link #compact 压缩}、{@link
 *   #duplicate 复制} 和 {@link #slice 切片}
 *   int 缓冲区的方法。 </p></li>
 *
 * </ul>
 *
 * <p> int 缓冲区可以通过 {@link #allocate
 * <i>分配</i>} 创建，这会为缓冲区的内容分配空间，也可以通过 {@link #wrap(int[]) <i>包装</i>}
 * 现有的 int 数组，或者通过创建现有字节缓冲区的
 * <a href="ByteBuffer.html#views"><i>视图</i></a> 来创建。
 *

 *



































































































*

 *
 * <p> 与字节缓冲区一样，int 缓冲区可以是 <a
 * href="ByteBuffer.html#direct"><i>直接的</i> 或 <i>非直接的</i></a>。 通过本类的 <tt>wrap</tt>
 * 方法创建的 int 缓冲区将是非直接的。 作为字节缓冲区视图创建的 int 缓冲区将是直接的，当且仅当该字节缓冲区本身是直接的。 是否
 * 一个 int 缓冲区是直接的可以通过调用 {@link
 * #isDirect isDirect} 方法来确定。 </p>
 *

*








 *



 *
 * <p> 本类中没有其他值可返回的方法被指定为返回调用它们的缓冲区。 这允许方法调用链式调用。
 *































 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家组
 * @since 1.4
 */

public abstract class IntBuffer
    extends Buffer
    implements Comparable<IntBuffer>
{

    // 这些字段在这里声明而不是在 Heap-X-Buffer 中声明，以减少访问这些
    // 值所需的虚拟方法调用次数，这对于编码小型缓冲区尤其昂贵。
    //
    final int[] hb;                  // 仅对堆缓冲区非空
    final int offset;
    boolean isReadOnly;                 // 仅对堆缓冲区有效

    // 创建具有给定标记、位置、限制、容量、后备数组和数组偏移量的新缓冲区
    //
    IntBuffer(int mark, int pos, int lim, int cap,   // 包私有
                 int[] hb, int offset)
    {
        super(mark, pos, lim, cap);
        this.hb = hb;
        this.offset = offset;
    }

    // 创建具有给定标记、位置、限制和容量的新缓冲区
    //
    IntBuffer(int mark, int pos, int lim, int cap) { // 包私有
        this(mark, pos, lim, cap, null, 0);
    }

























    /**
     * 分配一个新的 int 缓冲区。
     *
     * <p> 新缓冲区的位置将为零，其限制将为其容量，其标记将未定义，其每个元素将
     * 初始化为零。 它将有一个 {@link #array 后备数组}，
     * 其 {@link #arrayOffset 数组偏移量} 将为零。
     *
     * @param  capacity
     *         新缓冲区的容量，以 int 为单位
     *
     * @return  新的 int 缓冲区
     *
     * @throws  IllegalArgumentException
     *          如果 <tt>capacity</tt> 是负整数
     */
    public static IntBuffer allocate(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        return new HeapIntBuffer(capacity, capacity);
    }

    /**
     * 将 int 数组包装到缓冲区中。
     *
     * <p> 新缓冲区将由给定的 int 数组支持；
     * 即，对缓冲区的修改将导致数组被修改，反之亦然。 新缓冲区的容量将为
     * <tt>array.length</tt>，其位置将为 <tt>offset</tt>，其限制
     * 将为 <tt>offset + length</tt>，其标记将未定义。 其
     * {@link #array 后备数组} 将是给定数组，其
     * {@link #arrayOffset 数组偏移量} 将为零。 </p>
     *
     * @param  array
     *         将支持新缓冲区的数组
     *
     * @param  offset
     *         要使用的子数组的偏移量；必须非负且
     *         不大于 <tt>array.length</tt>。 新缓冲区的位置
     *         将设置为此值。
     *
     * @param  length
     *         要使用的子数组的长度；
     *         必须非负且不大于
     *         <tt>array.length - offset</tt>。
     *         新缓冲区的限制将设置为 <tt>offset + length</tt>。
     *
     * @return  新的 int 缓冲区
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>offset</tt> 和 <tt>length</tt>
     *          参数的前置条件不成立
     */
    public static IntBuffer wrap(int[] array,
                                    int offset, int length)
    {
        try {
            return new HeapIntBuffer(array, offset, length);
        } catch (IllegalArgumentException x) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * 将 int 数组包装到缓冲区中。
     *
     * <p> 新缓冲区将由给定的 int 数组支持；
     * 即，对缓冲区的修改将导致数组被修改，反之亦然。 新缓冲区的容量和限制将为
     * <tt>array.length</tt>，其位置将为零，其标记将未定义。 其
     * {@link #array 后备数组} 将是
     * 给定数组，其 {@link #arrayOffset 数组偏移量} 将
     * 为零。 </p>
     *
     * @param  array
     *         将支持此缓冲区的数组
     *
     * @return  新的 int 缓冲区
     */
    public static IntBuffer wrap(int[] array) {
        return wrap(array, 0, array.length);
    }

    /**
     * 创建一个新 int 缓冲区，其内容是此缓冲区内容的共享子序列。
     *
     * <p> 新缓冲区的内容将从此缓冲区的当前位置开始。对本缓冲区内容的更改将在新缓冲区中可见，反之亦然；两个缓冲区的位置、限制和标记值将独立。
     *
     * <p> 新缓冲区的位置将为零，其容量和限制将为本缓冲区中剩余的 int 数量，其标记将未定义。新缓冲区将直接（direct）当且仅当本缓冲区直接，且将只读（read-only）当且仅当本缓冲区只读。 </p>
     *
     * @return  新的 int 缓冲区
     */
    public abstract IntBuffer slice();

    /**
     * 创建一个与本缓冲区内容共享的新 int 缓冲区。
     *
     * <p> 新缓冲区的内容将为此缓冲区的内容。对本缓冲区内容的更改将在新缓冲区中可见，反之亦然；两个缓冲区的位置、限制和标记值将独立。
     *
     * <p> 新缓冲区的容量、限制、位置和标记值将与此缓冲区相同。新缓冲区将直接（direct）当且仅当本缓冲区直接，且将只读（read-only）当且仅当本缓冲区只读。 </p>
     *
     * @return  新的 int 缓冲区
     */
    public abstract IntBuffer duplicate();

    /**
     * 创建一个新的只读 int 缓冲区，其内容与此缓冲区共享。
     *
     * <p> 新缓冲区的内容将为此缓冲区的内容。对本缓冲区内容的更改将在新缓冲区中可见；然而，新缓冲区本身将是只读的，不允许修改共享内容。两个缓冲区的位置、限制和标记值将独立。
     *
     * <p> 新缓冲区的容量、限制、位置和标记值将与此缓冲区相同。
     *
     * <p> 如果本缓冲区本身是只读的，则此方法的行为与 {@link #duplicate duplicate} 方法完全相同。 </p>
     *
     * @return  新的只读 int 缓冲区
     */
    public abstract IntBuffer asReadOnlyBuffer();


    // -- 单例 get/put 方法 --

    /**
     * 相对 <i>get</i> 方法。读取此缓冲区当前位置的 int，然后递增位置。
     *
     * @return  缓冲区当前位置的 int
     *
     * @throws  BufferUnderflowException
     *          如果缓冲区的当前位置不小于其限制
     */
    public abstract int get();

    /**
     * 相对 <i>put</i> 方法&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 将给定的 int 写入此缓冲区的当前位置，然后递增位置。 </p>
     *
     * @param  i
     *         要写入的 int
     *
     * @return  本缓冲区
     *
     * @throws  BufferOverflowException
     *          如果本缓冲区的当前位置不小于其限制
     *
     * @throws  ReadOnlyBufferException
     *          如果本缓冲区是只读的
     */
    public abstract IntBuffer put(int i);

    /**
     * 绝对 <i>get</i> 方法。读取给定索引处的 int。
     *
     * @param  index
     *         要读取 int 的索引
     *
     * @return  给定索引处的 int
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负数
     *          或不小于缓冲区的限制
     */
    public abstract int get(int index);














    /**
     * 绝对 <i>put</i> 方法&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 将给定的 int 写入此缓冲区的给定索引处。 </p>
     *
     * @param  index
     *         要写入 int 的索引
     *
     * @param  i
     *         要写入的 int 值
     *
     * @return  本缓冲区
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>index</tt> 为负数
     *          或不小于缓冲区的限制
     *
     * @throws  ReadOnlyBufferException
     *          如果本缓冲区是只读的
     */
    public abstract IntBuffer put(int index, int i);


    // -- 批量 get 操作 --

    /**
     * 相对批量 <i>get</i> 方法。
     *
     * <p> 该方法将 int 从此缓冲区传输到给定的目标数组。如果缓冲区中剩余的 int 数量少于请求的数量，即
     * <tt>length</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>，则不传输任何 int 并抛出 {@link BufferUnderflowException}。
     *
     * <p> 否则，该方法从本缓冲区的当前位置开始，从给定偏移量开始将 <tt>length</tt> 个 int 复制到给定数组中。然后将本缓冲区的位置递增 <tt>length</tt>。
     *
     * <p> 换句话说，形式为 <tt>src.get(dst,&nbsp;off,&nbsp;len)</tt> 的此方法调用与以下循环具有完全相同的效果：
     *
     * <pre>{@code
     *     for (int i = off; i < off + len; i++)
     *         dst[i] = src.get():
     * }</pre>
     *
     * 除了它首先检查此缓冲区中是否有足够的 int，并且可能效率更高。
     *
     * @param  dst
     *         要写入 int 的数组
     *
     * @param  offset
     *         数组中要写入的第一个 int 的偏移量；必须为非负数且不大于
     *         <tt>dst.length</tt>
     *
     * @param  length
     *         要写入给定数组的最大 int 数量；必须为非负数且不大于
     *         <tt>dst.length - offset</tt>
     *
     * @return  本缓冲区
     *
     * @throws  BufferUnderflowException
     *          如果此缓冲区中剩余的 int 数量少于 <tt>length</tt>
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>offset</tt> 和 <tt>length</tt> 参数的前置条件不成立
     */
    public IntBuffer get(int[] dst, int offset, int length) {
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
     * <p> 该方法将 int 值从该缓冲区传输到给定的目标数组。该方法的调用形式
     * <tt>src.get(a)</tt> 的行为与以下调用完全相同
     *
     * <pre>
     *     src.get(a, 0, a.length) </pre>
     *
     * @param   dst
     *          目标数组
     *
     * @return  该缓冲区
     *
     * @throws  BufferUnderflowException
     *          如果该缓冲区中剩余的 int 值少于 <tt>length</tt>
     */
    public IntBuffer get(int[] dst) {
        return get(dst, 0, dst.length);
    }


    // -- 批量 put 操作 --

    /**
     * 相对批量 <i>put</i> 方法&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 该方法将给定源缓冲区中剩余的 int 值传输到该缓冲区。如果源缓冲区中剩余的 int 值多于该缓冲区中的剩余空间，即
     * <tt>src.remaining()</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>，
     * 则不传输任何 int 值并抛出 {@link
     * BufferOverflowException}。
     *
     * <p> 否则，该方法从给定缓冲区中复制
     * <i>n</i>&nbsp;=&nbsp;<tt>src.remaining()</tt> 个 int 值到该缓冲区，从每个缓冲区的当前位置开始。
     * 然后将两个缓冲区的位置都增加 <i>n</i>。
     *
     * <p> 换句话说，该方法的调用形式
     * <tt>dst.put(src)</tt> 与以下循环具有完全相同的效果
     *
     * <pre>
     *     while (src.hasRemaining())
     *         dst.put(src.get()); </pre>
     *
     * 除了它首先检查该缓冲区是否有足够的空间，并且可能更高效。
     *
     * @param  src
     *         从中读取 int 值的源缓冲区；
     *         不得是该缓冲区
     *
     * @return  该缓冲区
     *
     * @throws  BufferOverflowException
     *          如果该缓冲区中没有足够的空间来容纳源缓冲区中剩余的 int 值
     *
     * @throws  IllegalArgumentException
     *          如果源缓冲区是该缓冲区
     *
     * @throws  ReadOnlyBufferException
     *          如果该缓冲区是只读的
     */
    public IntBuffer put(IntBuffer src) {
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
     * <p> 该方法从给定的源数组中将 int 值传输到该缓冲区。如果要从数组中复制的 int 值多于该缓冲区中的剩余空间，即
     * <tt>length</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>，则不传输任何 int 值并抛出 {@link BufferOverflowException}。
     *
     * <p> 否则，该方法从给定数组中复制 <tt>length</tt> 个 int 值到该缓冲区，从数组中的给定偏移量开始，并从该缓冲区的当前位置开始。然后将该缓冲区的位置增加 <tt>length</tt>。
     *
     * <p> 换句话说，该方法的调用形式
     * <tt>dst.put(src,&nbsp;off,&nbsp;len)</tt> 与以下循环具有完全相同的效果
     *
     * <pre>{@code
     *     for (int i = off; i < off + len; i++)
     *         dst.put(a[i]);
     * }</pre>
     *
     * 除了它首先检查该缓冲区是否有足够的空间，并且可能更高效。
     *
     * @param  src
     *         从中读取 int 值的数组
     *
     * @param  offset
     *         数组中第一个要读取的 int 值的偏移量；
     *         必须是非负数且不大于 <tt>array.length</tt>
     *
     * @param  length
     *         要从给定数组中读取的 int 值的数量；
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
    public IntBuffer put(int[] src, int offset, int length) {
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
     * <p> 该方法将给定源 int 数组的全部内容传输到该缓冲区。该方法的调用形式
     * <tt>dst.put(a)</tt> 的行为与以下调用完全相同
     *
     * <pre>
     *     dst.put(a, 0, a.length) </pre>
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
    public final IntBuffer put(int[] src) {
        return put(src, 0, src.length);
    }































































































    // -- 其他内容 --

    /**
     * 告知该缓冲区是否由一个可访问的 int 数组支持。
     *
     * <p> 如果此方法返回 <tt>true</tt>，则可以安全地调用 {@link #array() array}
     * 和 {@link #arrayOffset() arrayOffset} 方法。
     * </p>
     *
     * @return  <tt>true</tt>，当且仅当该缓冲区由数组支持且不是只读的
     */
    public final boolean hasArray() {
        return (hb != null) && !isReadOnly;
    }


/**
 * 返回支持此缓冲区的 int 数组&nbsp;&nbsp;<i>(可选操作)</i>。
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
 *          如果此缓冲区没有可访问的后端数组
 */
public final int[] array() {
    if (hb == null)
        throw new UnsupportedOperationException();
    if (isReadOnly)
        throw new ReadOnlyBufferException();
    return hb;
}

/**
 * 返回缓冲区后端数组中缓冲区第一个元素的偏移量&nbsp;&nbsp;<i>(可选操作)</i>。
 *
 * <p> 如果此缓冲区由数组支持，则缓冲区位置 <i>p</i>
 * 对应于数组索引 <i>p</i>&nbsp;+&nbsp;<tt>arrayOffset()</tt>。
 *
 * <p> 在调用此方法之前调用 {@link #hasArray hasArray} 方法以确保此缓冲区具有可访问的后端数组。 </p>
 *
 * @return  缓冲区数组中缓冲区第一个元素的偏移量
 *
 * @throws  ReadOnlyBufferException
 *          如果此缓冲区由数组支持但只读
 *
 * @throws  UnsupportedOperationException
 *          如果此缓冲区没有可访问的后端数组
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
 * <p> 缓冲区当前位置和其限制之间的 int 值，如果有，将被复制到缓冲区的开头。也就是说，
 * 索引 <i>p</i>&nbsp;=&nbsp;<tt>position()</tt> 处的 int 值将被复制到索引零，
 * 索引 <i>p</i>&nbsp;+&nbsp;1 处的 int 值将被复制到索引一，依此类推，直到索引
 * <tt>limit()</tt>&nbsp;-&nbsp;1 处的 int 值被复制到索引
 * <i>n</i>&nbsp;=&nbsp;<tt>limit()</tt>&nbsp;-&nbsp;<tt>1</tt>&nbsp;-&nbsp;<i>p</i>。
 * 然后将缓冲区的位置设置为 <i>n+1</i>，其限制设置为其容量。如果定义了标记，则会被丢弃。
 *
 * <p> 缓冲区的位置被设置为复制的 int 值的数量，而不是设置为零，因此可以立即调用此方法后
 * 跟随调用另一个相对 <i>put</i> 方法。 </p>
 *
















 *
 * @return  此缓冲区
 *
 * @throws  ReadOnlyBufferException
 *          如果此缓冲区只读
 */
public abstract IntBuffer compact();

/**
 * 告知此 int 缓冲区是否为直接缓冲区。
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
 * <p> int 缓冲区的哈希码仅取决于其剩余元素；即，从 <tt>position()</tt> 到
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

        h = 31 * h + get(i);



    return h;
}

/**
 * 告知此缓冲区是否等于另一个对象。
 *
 * <p> 两个 int 缓冲区相等当且仅当，
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
     *   </p></li>
 *
 * </ol>
 *
 * <p> int 缓冲区不等于任何其他类型的对象。 </p>
 *
 * @param  ob  要与此缓冲区进行比较的对象
 *
 * @return  <tt>true</tt> 如果且仅如果此缓冲区等于给定对象
 */
public boolean equals(Object ob) {
    if (this == ob)
        return true;
    if (!(ob instanceof IntBuffer))
        return false;
    IntBuffer that = (IntBuffer)ob;
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

private static boolean equals(int x, int y) {



    return x == y;

}

/**
 * 比较此缓冲区与另一个缓冲区。
 *
 * <p> 两个 int 缓冲区通过比较它们的剩余元素序列的字典顺序进行比较，不考虑每个序列在其对应缓冲区中的起始位置。


     * 整数对元素的比较如同调用了 {@link Integer#compare(int,int)} 一样进行。

     *
     * <p> 整数缓冲区与其他类型的对象不可比较。
     *
     * @return  如果此缓冲区小于、等于或大于给定的缓冲区，则分别返回负整数、零或正整数
     */
    public int compareTo(IntBuffer that) {
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

    private static int compare(int x, int y) {






        return Integer.compare(x, y);

    }

    // -- 其他字符相关的内容 --


































































































































































































    // -- 其他字节相关的内容：访问二进制数据 --



    /**
     * 获取此缓冲区的字节顺序。
     *
     * <p> 通过分配或包装现有 <tt>int</tt> 数组创建的整数缓冲区的字节顺序是底层硬件的 {@link
     * ByteOrder#nativeOrder 本地顺序}。作为字节缓冲区的<a
     * href="ByteBuffer.html#views">视图</a>创建的整数缓冲区的字节顺序是在创建视图时字节缓冲区的顺序。 </p>
     *
     * @return  此缓冲区的字节顺序
     */
    public abstract ByteOrder order();

































































}
