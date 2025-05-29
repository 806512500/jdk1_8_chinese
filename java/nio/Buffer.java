/*
 * 版权所有 (c) 2000, 2020, Oracle 和/或其附属公司。保留所有权利。
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

import java.util.Spliterator;

/**
 * 用于存储特定基本类型数据的容器。
 *
 * <p> 缓冲区是特定基本类型元素的线性、有限序列。除了其内容外，缓冲区的基本属性是其容量、限制和位置： </p>
 *
 * <blockquote>
 *
 *   <p> 缓冲区的 <i>容量</i> 是它包含的元素数量。缓冲区的容量永远不会为负且永远不会改变。 </p>
 *
 *   <p> 缓冲区的 <i>限制</i> 是不应读取或写入的第一个元素的索引。缓冲区的限制永远不会为负且永远不会大于其容量。 </p>
 *
 *   <p> 缓冲区的 <i>位置</i> 是下一个要读取或写入的元素的索引。缓冲区的位置永远不会为负且永远不会大于其限制。 </p>
 *
 * </blockquote>
 *
 * <p> 本类的每个非布尔基本类型都有一个子类。 </p>
 *
 *
 * <h2> 数据传输 </h2>
 *
 * <p> 本类的每个子类定义了两类 <i>get</i> 和 <i>put</i> 操作： </p>
 *
 * <blockquote>
 *
 *   <p> <i>相对</i> 操作从当前位置开始读取或写入一个或多个元素，然后将位置增加所传输的元素数量。如果请求的传输超过限制，则相对 <i>get</i> 操作将抛出 {@link BufferUnderflowException}，相对 <i>put</i> 操作将抛出 {@link BufferOverflowException}；在这两种情况下，都不会传输数据。 </p>
 *
 *   <p> <i>绝对</i> 操作采用一个明确的元素索引，且不影响位置。绝对 <i>get</i> 和 <i>put</i> 操作如果索引参数超过限制，则会抛出 {@link IndexOutOfBoundsException}。 </p>
 *
 * </blockquote>
 *
 * <p> 当然，数据也可以通过适当通道的 I/O 操作传输到缓冲区或从缓冲区传输出去，这些操作总是相对于当前位置的。 </p>
 *
 *
 * <h2> 标记和重置 </h2>
 *
 * <p> 缓冲区的 <i>标记</i> 是其位置在调用 {@link #reset reset} 方法时将重置到的索引。标记并不总是被定义，但如果被定义，则永远不会为负且永远不会大于位置。如果定义了标记，则当位置或限制调整为小于标记的值时，标记将被丢弃。如果未定义标记，则调用 {@link #reset reset} 方法将导致抛出 {@link InvalidMarkException}。 </p>
 *
 *
 * <h2> 不变量 </h2>
 *
 * <p> 以下不变量适用于标记、位置、限制和容量值：
 *
 * <blockquote>
 *     <tt>0</tt> <tt>&lt;=</tt>
 *     <i>标记</i> <tt>&lt;=</tt>
 *     <i>位置</i> <tt>&lt;=</tt>
 *     <i>限制</i> <tt>&lt;=</tt>
 *     <i>容量</i>
 * </blockquote>
 *
 * <p> 新创建的缓冲区总是具有零位置和未定义的标记。初始限制可能是零，也可能是取决于缓冲区类型及其构造方式的其他值。新分配的缓冲区的每个元素都初始化为零。 </p>
 *
 *
 * <h2> 清除、翻转和倒回 </h2>
 *
 * <p> 除了访问位置、限制和容量值的方法以及标记和重置的方法外，本类还定义了以下对缓冲区的操作：
 *
 * <ul>
 *
 *   <li><p> {@link #clear} 使缓冲区准备好进行新的通道读取或相对 <i>put</i> 操作：它将限制设置为容量并将位置设置为零。 </p></li>
 *
 *   <li><p> {@link #flip} 使缓冲区准备好进行新的通道写入或相对 <i>get</i> 操作：它将限制设置为当前位置，然后将位置设置为零。 </p></li>
 *
 *   <li><p> {@link #rewind} 使缓冲区准备好重新读取其已包含的数据：它保持限制不变并将位置设置为零。 </p></li>
 *
 * </ul>
 *
 *
 * <h2> 只读缓冲区 </h2>
 *
 * <p> 每个缓冲区都是可读的，但并非每个缓冲区都是可写的。每个缓冲区类的变异方法被指定为 <i>可选操作</i>，当在只读缓冲区上调用时将抛出 {@link ReadOnlyBufferException}。只读缓冲区不允许其内容被更改，但其标记、位置和限制值是可变的。是否为只读缓冲区可以通过调用其 {@link #isReadOnly isReadOnly} 方法来确定。 </p>
 *
 *
 * <h2> 线程安全性 </h2>
 *
 * <p> 缓冲区不适合多个并发线程使用。如果缓冲区将由多个线程使用，则应对缓冲区的访问进行适当的同步控制。 </p>
 *
 *
 * <h2> 方法调用链 </h2>
 *
 * <p> 本类中没有其他值要返回的方法被指定为返回调用它们的缓冲区。这允许方法调用链；例如，以下语句序列
 *
 * <blockquote><pre>
 * b.flip();
 * b.position(23);
 * b.limit(42);</pre></blockquote>
 *
 * 可以替换为更紧凑的单个语句
 *
 * <blockquote><pre>
 * b.flip().position(23).limit(42);</pre></blockquote>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家组
 * @since 1.4
 */

public abstract class Buffer {

    /**
     * 遍历和拆分缓冲区中维护的元素的 Spliterators 的特性。
     */
    static final int SPLITERATOR_CHARACTERISTICS =
        Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED;

    // 不变量：mark <= position <= limit <= capacity
    private int mark = -1;
    private int position = 0;
    private int limit;
    private int capacity;

    // 仅由直接缓冲区使用
    // 注意：提升到这里以加快 JNI GetDirectBufferAddress 的速度
    long address;


                // 创建一个新的缓冲区，具有给定的标记、位置、限制和容量，
    // 并检查不变性。
    //
    Buffer(int mark, int pos, int lim, int cap) {       // 包私有
        if (cap < 0)
            throw new IllegalArgumentException("Negative capacity: " + cap);
        this.capacity = cap;
        limit(lim);
        position(pos);
        if (mark >= 0) {
            if (mark > pos)
                throw new IllegalArgumentException("mark > position: ("
                                                   + mark + " > " + pos + ")");
            this.mark = mark;
        }
    }

    /**
     * 返回此缓冲区的容量。
     *
     * @return  此缓冲区的容量
     */
    public final int capacity() {
        return capacity;
    }

    /**
     * 返回此缓冲区的位置。
     *
     * @return  此缓冲区的位置
     */
    public final int position() {
        return position;
    }

    /**
     * 设置此缓冲区的位置。如果已定义标记且大于新位置，则丢弃标记。
     *
     * @param  newPosition
     *         新的位置值；必须是非负数且不大于当前限制
     *
     * @return  此缓冲区
     *
     * @throws  IllegalArgumentException
     *          如果 <tt>newPosition</tt> 的前置条件不成立
     */
    public final Buffer position(int newPosition) {
        if ((newPosition > limit) || (newPosition < 0))
            throw createPositionException(newPosition);
        if (mark > newPosition) mark = -1;
        position = newPosition;
        return this;
    }

    /**
     * 验证 {@code 0 < newPosition <= limit}
     *
     * @param newPosition
     *        新的位置值
     *
     * @throws IllegalArgumentException
     *         如果指定的位置超出范围。
     */
    private IllegalArgumentException createPositionException(int newPosition) {
        String msg = null;

        if (newPosition > limit) {
            msg = "newPosition > limit: (" + newPosition + " > " + limit + ")";
        } else { // 假定为负数
            assert newPosition < 0 : "newPosition expected to be negative";
            msg = "newPosition < 0: (" + newPosition + " < 0)";
        }

        return new IllegalArgumentException(msg);
    }

    /**
     * 返回此缓冲区的限制。
     *
     * @return  此缓冲区的限制
     */
    public final int limit() {
        return limit;
    }

    /**
     * 设置此缓冲区的限制。如果位置大于新限制，则将其设置为新限制。如果已定义标记且大于新限制，则丢弃标记。
     *
     * @param  newLimit
     *         新的限制值；必须是非负数且不大于此缓冲区的容量
     *
     * @return  此缓冲区
     *
     * @throws  IllegalArgumentException
     *          如果 <tt>newLimit</tt> 的前置条件不成立
     */
    public final Buffer limit(int newLimit) {
        if ((newLimit > capacity) || (newLimit < 0))
            throw new IllegalArgumentException();
        limit = newLimit;
        if (position > newLimit) position = newLimit;
        if (mark > newLimit) mark = -1;
        return this;
    }

    /**
     * 将此缓冲区的标记设置为其位置。
     *
     * @return  此缓冲区
     */
    public final Buffer mark() {
        mark = position;
        return this;
    }

    /**
     * 将此缓冲区的位置重置为先前标记的位置。
     *
     * <p> 调用此方法不会更改或丢弃标记的值。 </p>
     *
     * @return  此缓冲区
     *
     * @throws  InvalidMarkException
     *          如果未设置标记
     */
    public final Buffer reset() {
        int m = mark;
        if (m < 0)
            throw new InvalidMarkException();
        position = m;
        return this;
    }

    /**
     * 清除此缓冲区。位置设置为零，限制设置为容量，标记被丢弃。
     *
     * <p> 在使用一系列通道读取或 <i>put</i> 操作填充此缓冲区之前调用此方法。例如：
     *
     * <blockquote><pre>
     * buf.clear();     // 准备缓冲区以进行读取
     * in.read(buf);    // 读取数据</pre></blockquote>
     *
     * <p> 此方法实际上不会擦除缓冲区中的数据，但命名为好像会擦除一样，因为大多数情况下这可能是适用的。 </p>
     *
     * @return  此缓冲区
     */
    public final Buffer clear() {
        position = 0;
        limit = capacity;
        mark = -1;
        return this;
    }

    /**
     * 翻转此缓冲区。限制设置为当前位置，然后位置设置为零。如果已定义标记，则丢弃标记。
     *
     * <p> 在一系列通道写入或相对 <i>get</i> 操作之前调用此方法，假设限制已适当设置。例如：
     *
     * <blockquote><pre>
     * buf.put(magic);    // 添加头部
     * in.read(buf);      // 将数据读取到缓冲区的其余部分
     * buf.flip();        // 翻转缓冲区
     * out.write(buf);    // 将头部 + 数据写入通道</pre></blockquote>
     *
     * <p> 此方法通常与 {@link
     * java.nio.ByteBuffer#compact compact} 方法结合使用，当从一个地方传输数据到另一个地方时。 </p>
     *
     * @return  此缓冲区
     */
    public final Buffer flip() {
        limit = position;
        position = 0;
        mark = -1;
        return this;
    }

    /**
     * 重绕此缓冲区。位置设置为零，标记被丢弃。
     *
     * <p> 在一系列通道写入或 <i>get</i> 操作之前调用此方法，假设限制已适当设置。例如：
     *
     * <blockquote><pre>
     * out.write(buf);    // 写入剩余数据
     * buf.rewind();      // 重绕缓冲区
     * buf.get(array);    // 将数据复制到数组中</pre></blockquote>
     *
     * @return  此缓冲区
     */
    public final Buffer rewind() {
        position = 0;
        mark = -1;
        return this;
    }

                /**
     * 返回当前位置与限制之间的元素数量。
     *
     * @return  该缓冲区中剩余的元素数量
     */
    public final int remaining() {
        int rem = limit - position;
        return rem > 0 ? rem : 0;
    }

    /**
     * 告知当前位置与限制之间是否有任何元素。
     *
     * @return  如果且仅当该缓冲区中至少有一个剩余元素时，返回 <tt>true</tt>
     */
    public final boolean hasRemaining() {
        return position < limit;
    }

    /**
     * 告知该缓冲区是否为只读。
     *
     * @return  如果且仅当该缓冲区为只读时，返回 <tt>true</tt>
     */
    public abstract boolean isReadOnly();

    /**
     * 告知该缓冲区是否由一个可访问的数组支持。
     *
     * <p> 如果此方法返回 <tt>true</tt>，则可以安全地调用 {@link #array() array}
     * 和 {@link #arrayOffset() arrayOffset} 方法。
     * </p>
     *
     * @return  如果且仅当该缓冲区由数组支持且不是只读时，返回 <tt>true</tt>
     *
     * @since 1.6
     */
    public abstract boolean hasArray();

    /**
     * 返回支持此缓冲区的数组&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 此方法旨在允许数组支持的缓冲区更高效地传递给本机代码。具体子类为此方法提供更强烈类型的返回值。
     *
     * <p> 对此缓冲区内容的修改将导致返回数组的内容被修改，反之亦然。
     *
     * <p> 在调用此方法之前调用 {@link #hasArray hasArray} 方法以确保此缓冲区具有可访问的支持数组。 </p>
     *
     * @return  支持此缓冲区的数组
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区由数组支持但为只读
     *
     * @throws  UnsupportedOperationException
     *          如果此缓冲区不由可访问的数组支持
     *
     * @since 1.6
     */
    public abstract Object array();

    /**
     * 返回此缓冲区支持数组中第一个缓冲区元素的偏移量&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 如果此缓冲区由数组支持，则缓冲区位置 <i>p</i>
     * 对应于数组索引 <i>p</i>&nbsp;+&nbsp;<tt>arrayOffset()</tt>。
     *
     * <p> 在调用此方法之前调用 {@link #hasArray hasArray} 方法以确保此缓冲区具有可访问的支持数组。 </p>
     *
     * @return  此缓冲区数组中第一个缓冲区元素的偏移量
     *
     * @throws  ReadOnlyBufferException
     *          如果此缓冲区由数组支持但为只读
     *
     * @throws  UnsupportedOperationException
     *          如果此缓冲区不由可访问的数组支持
     *
     * @since 1.6
     */
    public abstract int arrayOffset();

    /**
     * 告知此缓冲区是否为
     * <a href="ByteBuffer.html#direct"><i>直接的</i></a>。
     *
     * @return  如果且仅当此缓冲区为直接时，返回 <tt>true</tt>
     *
     * @since 1.6
     */
    public abstract boolean isDirect();


    // -- 包私有方法，用于边界检查等 --

    /**
     * 检查当前位置是否小于限制，如果不是，则抛出 {@link
     * BufferUnderflowException}，然后增加位置。
     *
     * @return  增加前的当前位置值
     */
    final int nextGetIndex() {                          // 包私有
        int p = position;
        if (p >= limit)
            throw new BufferUnderflowException();
        position = p + 1;
        return p;
    }

    final int nextGetIndex(int nb) {                    // 包私有
        int p = position;
        if (limit - p < nb)
            throw new BufferUnderflowException();
        position = p + nb;
        return p;
    }

    /**
     * 检查当前位置是否小于限制，如果不是，则抛出 {@link
     * BufferOverflowException}，然后增加位置。
     *
     * @return  增加前的当前位置值
     */
    final int nextPutIndex() {                          // 包私有
        int p = position;
        if (p >= limit)
            throw new BufferOverflowException();
        position = p + 1;
        return p;
    }

    final int nextPutIndex(int nb) {                    // 包私有
        int p = position;
        if (limit - p < nb)
            throw new BufferOverflowException();
        position = p + nb;
        return p;
    }

    /**
     * 检查给定索引是否小于限制或小于零，如果不是，则抛出 {@link
     * IndexOutOfBoundsException}。
     */
    final int checkIndex(int i) {                       // 包私有
        if ((i < 0) || (i >= limit))
            throw new IndexOutOfBoundsException();
        return i;
    }

    final int checkIndex(int i, int nb) {               // 包私有
        if ((i < 0) || (nb > limit - i))
            throw new IndexOutOfBoundsException();
        return i;
    }

    final int markValue() {                             // 包私有
        return mark;
    }

    final void truncate() {                             // 包私有
        mark = -1;
        position = 0;
        limit = 0;
        capacity = 0;
    }

    final void discardMark() {                          // 包私有
        mark = -1;
    }

    static void checkBounds(int off, int len, int size) { // 包私有
        if ((off | len | (off + len) | (size - (off + len))) < 0)
            throw new IndexOutOfBoundsException();
    }

}
