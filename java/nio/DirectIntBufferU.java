
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


class DirectIntBufferU

    extends IntBuffer



    implements DirectBuffer
{



    // 缓存的不安全访问对象
    protected static final Unsafe unsafe = Bits.unsafe();

    // 缓存的数组基偏移量
    private static final long arrayBaseOffset = (long)unsafe.arrayBaseOffset(int[].class);

    // 缓存的未对齐访问能力
    protected static final boolean unaligned = Bits.unaligned();

    // 基地址，用于所有索引计算
    // 注意：为了在 JNI GetDirectBufferAddress 中提高速度，已移至 Buffer.java
    //    protected long address;

    // 附加到此缓冲区的对象。如果此缓冲区是另一个缓冲区的视图，则使用此字段保持对该缓冲区的引用，
    // 以确保在我们完成之前其内存不会被释放。
    private final Object att;

    public Object attachment() {
        return att;
    }






































    public Cleaner cleaner() { return null; }
















































































    // 用于复制和切片
    //
    DirectIntBufferU(DirectBuffer db,         // 包私有
                               int mark, int pos, int lim, int cap,
                               int off)
    {

        super(mark, pos, lim, cap);
        address = db.address() + off;



        att = db;



    }

    public IntBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        int off = (pos << 2);
        assert (off >= 0);
        return new DirectIntBufferU(this, -1, 0, rem, rem, off);
    }

    public IntBuffer duplicate() {
        return new DirectIntBufferU(this,
                                              this.markValue(),
                                              this.position(),
                                              this.limit(),
                                              this.capacity(),
                                              0);
    }

    public IntBuffer asReadOnlyBuffer() {

        return new DirectIntBufferRU(this,
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
        return address + ((long)i << 2);
    }

    public int get() {
        return ((unsafe.getInt(ix(nextGetIndex()))));
    }

    public int get(int i) {
        return ((unsafe.getInt(ix(checkIndex(i)))));
    }







    public IntBuffer get(int[] dst, int offset, int length) {

        if (((long)length << 2) > Bits.JNI_COPY_TO_ARRAY_THRESHOLD) {
            checkBounds(offset, length, dst.length);
            int pos = position();
            int lim = limit();
            assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);
            if (length > rem)
                throw new BufferUnderflowException();


            if (order() != ByteOrder.nativeOrder())
                Bits.copyToIntArray(ix(pos), dst,
                                          (long)offset << 2,
                                          (long)length << 2);
            else

                Bits.copyToArray(ix(pos), dst, arrayBaseOffset,
                                 (long)offset << 2,
                                 (long)length << 2);
            position(pos + length);
        } else {
            super.get(dst, offset, length);
        }
        return this;



    }



    public IntBuffer put(int x) {

        unsafe.putInt(ix(nextPutIndex()), ((x)));
        return this;



    }

    public IntBuffer put(int i, int x) {

        unsafe.putInt(ix(checkIndex(i)), ((x)));
        return this;



    }

    public IntBuffer put(IntBuffer src) {

        if (src instanceof DirectIntBufferU) {
            if (src == this)
                throw new IllegalArgumentException();
            DirectIntBufferU sb = (DirectIntBufferU)src;

            int spos = sb.position();
            int slim = sb.limit();
            int srem = (spos <= slim ? slim - spos : 0);

            int pos = position();
            int lim = limit();
            int rem = (pos <= lim ? lim - pos : 0);

            if (srem > rem)
                throw new BufferOverflowException();
            unsafe.copyMemory(sb.ix(spos), ix(pos), (long)srem << 2);
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

    public IntBuffer put(int[] src, int offset, int length) {

        if (((long)length << 2) > Bits.JNI_COPY_FROM_ARRAY_THRESHOLD) {
            checkBounds(offset, length, src.length);
            int pos = position();
            int lim = limit();
            assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);
            if (length > rem)
                throw new BufferOverflowException();


            if (order() != ByteOrder.nativeOrder())
                Bits.copyFromIntArray(src,
                                            (long)offset << 2,
                                            ix(pos),
                                            (long)length << 2);
            else


                            Bits.copyFromArray(src, arrayBaseOffset,
                                   (long)offset << 2,
                                   ix(pos),
                                   (long)length << 2);
            position(pos + length);
        } else {
            super.put(src, offset, length);
        }
        return this;



    }

    public IntBuffer compact() {

        int pos = position();
        int lim = limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);

        unsafe.copyMemory(ix(pos), ix(0), (long)rem << 2);
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
