
/*
 * Copyright (c) 2000, 2013, Oracle and/或其附属公司。保留所有权利。
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

// -- 本文件由机器生成：请勿编辑！ -- //

package java.nio;

import java.io.FileDescriptor;
import sun.misc.Cleaner;
import sun.misc.Unsafe;
import sun.misc.VM;
import sun.nio.ch.DirectBuffer;


class DirectShortBufferS

    extends ShortBuffer



    implements DirectBuffer
{



    // 缓存的 unsafe 访问对象
    protected static final Unsafe unsafe = Bits.unsafe();

    // 缓存的数组基偏移量
    private static final long arrayBaseOffset = (long)unsafe.arrayBaseOffset(short[].class);

    // 缓存的未对齐访问能力
    protected static final boolean unaligned = Bits.unaligned();

    // 基地址，用于所有索引计算
    // 注意：已移至 Buffer.java 以加快 JNI GetDirectBufferAddress 速度
    //    protected long address;

    // 附加到此缓冲区的对象。如果此缓冲区是另一个缓冲区的视图，则使用此字段保持对该缓冲区的引用，
    // 以确保在我们完成之前不会释放其内存。
    private final Object att;

    public Object attachment() {
        return att;
    }






































    public Cleaner cleaner() { return null; }
















































































    // 用于复制和切片
    //
    DirectShortBufferS(DirectBuffer db,         // 包私有
                               int mark, int pos, int lim, int cap,
                               int off)
    {

        super(mark, pos, lim, cap);
        address = db.address() + off;



        att = db;



    }

    public ShortBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        int off = (pos << 1);
        assert (off >= 0);
        return new DirectShortBufferS(this, -1, 0, rem, rem, off);
    }

    public ShortBuffer duplicate() {
        return new DirectShortBufferS(this,
                                              this.markValue(),
                                              this.position(),
                                              this.limit(),
                                              this.capacity(),
                                              0);
    }

    public ShortBuffer asReadOnlyBuffer() {

        return new DirectShortBufferRS(this,
                                           this.markValue(),
                                           this.position(),
                                           this.limit(),
                                           this.capacity(),
                                           0);



    }



    public long address() {
        return address;
    }

    private long ix(int i) {
        return address + ((long)i << 1);
    }

    public short get() {
        return (Bits.swap(unsafe.getShort(ix(nextGetIndex()))));
    }

    public short get(int i) {
        return (Bits.swap(unsafe.getShort(ix(checkIndex(i)))));
    }







    public ShortBuffer get(short[] dst, int offset, int length) {

        if (((long)length << 1) > Bits.JNI_COPY_TO_ARRAY_THRESHOLD) {
            checkBounds(offset, length, dst.length);
            int pos = position();
            int lim = limit();
            assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);
            if (length > rem)
                throw new BufferUnderflowException();


            if (order() != ByteOrder.nativeOrder())
                Bits.copyToShortArray(ix(pos), dst,
                                          (long)offset << 1,
                                          (long)length << 1);
            else

                Bits.copyToArray(ix(pos), dst, arrayBaseOffset,
                                 (long)offset << 1,
                                 (long)length << 1);
            position(pos + length);
        } else {
            super.get(dst, offset, length);
        }
        return this;



    }



    public ShortBuffer put(short x) {

        unsafe.putShort(ix(nextPutIndex()), Bits.swap((x)));
        return this;



    }

    public ShortBuffer put(int i, short x) {

        unsafe.putShort(ix(checkIndex(i)), Bits.swap((x)));
        return this;



    }

    public ShortBuffer put(ShortBuffer src) {

        if (src instanceof DirectShortBufferS) {
            if (src == this)
                throw new IllegalArgumentException();
            DirectShortBufferS sb = (DirectShortBufferS)src;

            int spos = sb.position();
            int slim = sb.limit();
            int srem = (spos <= slim ? slim - spos : 0);

            int pos = position();
            int lim = limit();
            int rem = (pos <= lim ? lim - pos : 0);

            if (srem > rem)
                throw new BufferOverflowException();
            unsafe.copyMemory(sb.ix(spos), ix(pos), (long)srem << 1);
            sb.position(spos + srem);
            position(pos + srem);
        } else if (src.hb != null) {

            int spos = src.position();
            int slim = src.limit();
            assert (spos <= slim);
            int srem = (spos <= slim ? slim - spos : 0);

            put(src.hb, src.offset + spos, srem);
            src.position(spos + srem);

        } else {
            super.put(src);
        }
        return this;



    }

    public ShortBuffer put(short[] src, int offset, int length) {

        if (((long)length << 1) > Bits.JNI_COPY_FROM_ARRAY_THRESHOLD) {
            checkBounds(offset, length, src.length);
            int pos = position();
            int lim = limit();
            assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);
            if (length > rem)
                throw new BufferOverflowException();


            if (order() != ByteOrder.nativeOrder())
                Bits.copyFromShortArray(src,
                                            (long)offset << 1,
                                            ix(pos),
                                            (long)length << 1);
            else


                            Bits.copyFromArray(src, arrayBaseOffset,
                                   (long)offset << 1,
                                   ix(pos),
                                   (long)length << 1);
            position(pos + length);
        } else {
            super.put(src, offset, length);
        }
        return this;



    }

    public ShortBuffer compact() {

        int pos = position();
        int lim = limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);

        unsafe.copyMemory(ix(pos), ix(0), (long)rem << 1);
        position(rem);
        limit(capacity());
        discardMark();
        return this;



    }

    public boolean isDirect() {
        // 返回是否为直接缓冲区
        return true;
    }

    public boolean isReadOnly() {
        // 返回是否为只读缓冲区
        return false;
    }















































    public ByteOrder order() {

        // 返回当前缓冲区的字节序，如果本机字节序为大端序，则返回小端序，反之亦然
        return ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN)
                ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);





    }


























}
