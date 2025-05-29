
/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

// -- This file was mechanically generated: Do not edit! -- //

package java.nio;

import java.io.FileDescriptor;
import sun.misc.Cleaner;
import sun.misc.Unsafe;
import sun.misc.VM;
import sun.nio.ch.DirectBuffer;


class DirectLongBufferU

    extends LongBuffer



    implements DirectBuffer
{



    // 缓存的不安全访问对象
    protected static final Unsafe unsafe = Bits.unsafe();

    // 缓存的数组基偏移
    private static final long arrayBaseOffset = (long)unsafe.arrayBaseOffset(long[].class);

    // 缓存的未对齐访问能力
    protected static final boolean unaligned = Bits.unaligned();

    // 基地址，用于所有索引计算
    // 注意：已移至 Buffer.java 以加快 JNI GetDirectBufferAddress 的速度
    //    protected long address;

    // 附加到此缓冲区的对象。如果此缓冲区是另一个缓冲区的视图，则使用此字段保持对该缓冲区的引用，以确保在我们完成之前不会释放其内存。
    private final Object att;

    public Object attachment() {
        return att;
    }






































    public Cleaner cleaner() { return null; }
















































































    // 用于复制和切片
    //
    DirectLongBufferU(DirectBuffer db,         // 包私有
                               int mark, int pos, int lim, int cap,
                               int off)
    {

        super(mark, pos, lim, cap);
        address = db.address() + off;



        att = db;



    }

    public LongBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        int off = (pos << 3);
        assert (off >= 0);
        return new DirectLongBufferU(this, -1, 0, rem, rem, off);
    }

    public LongBuffer duplicate() {
        return new DirectLongBufferU(this,
                                              this.markValue(),
                                              this.position(),
                                              this.limit(),
                                              this.capacity(),
                                              0);
    }

    public LongBuffer asReadOnlyBuffer() {

        return new DirectLongBufferRU(this,
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
        return address + ((long)i << 3);
    }

    public long get() {
        return ((unsafe.getLong(ix(nextGetIndex()))));
    }

    public long get(int i) {
        return ((unsafe.getLong(ix(checkIndex(i)))));
    }







    public LongBuffer get(long[] dst, int offset, int length) {

        if (((long)length << 3) > Bits.JNI_COPY_TO_ARRAY_THRESHOLD) {
            checkBounds(offset, length, dst.length);
            int pos = position();
            int lim = limit();
            assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);
            if (length > rem)
                throw new BufferUnderflowException();


            if (order() != ByteOrder.nativeOrder())
                Bits.copyToLongArray(ix(pos), dst,
                                          (long)offset << 3,
                                          (long)length << 3);
            else

                Bits.copyToArray(ix(pos), dst, arrayBaseOffset,
                                 (long)offset << 3,
                                 (long)length << 3);
            position(pos + length);
        } else {
            super.get(dst, offset, length);
        }
        return this;



    }



    public LongBuffer put(long x) {

        unsafe.putLong(ix(nextPutIndex()), ((x)));
        return this;



    }

    public LongBuffer put(int i, long x) {

        unsafe.putLong(ix(checkIndex(i)), ((x)));
        return this;



    }

    public LongBuffer put(LongBuffer src) {

        if (src instanceof DirectLongBufferU) {
            if (src == this)
                throw new IllegalArgumentException();
            DirectLongBufferU sb = (DirectLongBufferU)src;

            int spos = sb.position();
            int slim = sb.limit();
            int srem = (spos <= slim ? slim - spos : 0);

            int pos = position();
            int lim = limit();
            int rem = (pos <= lim ? lim - pos : 0);

            if (srem > rem)
                throw new BufferOverflowException();
            unsafe.copyMemory(sb.ix(spos), ix(pos), (long)srem << 3);
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

    public LongBuffer put(long[] src, int offset, int length) {

        if (((long)length << 3) > Bits.JNI_COPY_FROM_ARRAY_THRESHOLD) {
            checkBounds(offset, length, src.length);
            int pos = position();
            int lim = limit();
            assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);
            if (length > rem)
                throw new BufferOverflowException();


            if (order() != ByteOrder.nativeOrder())
                Bits.copyFromLongArray(src,
                                            (long)offset << 3,
                                            ix(pos),
                                            (long)length << 3);
            else


                            Bits.copyFromArray(src, arrayBaseOffset,
                                   (long)offset << 3,
                                   ix(pos),
                                   (long)length << 3);
            position(pos + length);
        } else {
            super.put(src, offset, length);
        }
        return this;



    }

    public LongBuffer compact() {

        int pos = position();
        int lim = limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);

        unsafe.copyMemory(ix(pos), ix(0), (long)rem << 3);
        position(rem);
        limit(capacity());
        discardMark();
        return this;



    }

    public boolean isDirect() {
        return true;
    }

    public boolean isReadOnly() {
        return false;
    }















































    public ByteOrder order() {





        return ((ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN)
                ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);

    }


























}
