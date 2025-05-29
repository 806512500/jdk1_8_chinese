
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


class DirectCharBufferS

    extends CharBuffer



    implements DirectBuffer
{



    // 缓存的不安全访问对象
    protected static final Unsafe unsafe = Bits.unsafe();

    // 缓存的数组基偏移量
    private static final long arrayBaseOffset = (long)unsafe.arrayBaseOffset(char[].class);

    // 缓存的未对齐访问能力
    protected static final boolean unaligned = Bits.unaligned();

    // 基地址，用于所有索引计算
    // 注意：为了在 JNI GetDirectBufferAddress 中提高速度，已移至 Buffer.java
    //    protected long address;

    // 附加到此缓冲区的对象。如果此缓冲区是另一个缓冲区的视图，则使用此字段来保持对该缓冲区的引用，以确保在我们完成之前不会释放其内存。
    private final Object att;

    public Object attachment() {
        return att;
    }






































    public Cleaner cleaner() { return null; }
















































































    // 用于复制和切片
    //
    DirectCharBufferS(DirectBuffer db,         // 包私有
                               int mark, int pos, int lim, int cap,
                               int off)
    {

        super(mark, pos, lim, cap);
        address = db.address() + off;



        att = db;



    }

    public CharBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        int off = (pos << 1);
        assert (off >= 0);
        return new DirectCharBufferS(this, -1, 0, rem, rem, off);
    }

    public CharBuffer duplicate() {
        return new DirectCharBufferS(this,
                                              this.markValue(),
                                              this.position(),
                                              this.limit(),
                                              this.capacity(),
                                              0);
    }

    public CharBuffer asReadOnlyBuffer() {

        return new DirectCharBufferRS(this,
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

    public char get() {
        return (Bits.swap(unsafe.getChar(ix(nextGetIndex()))));
    }

    public char get(int i) {
        return (Bits.swap(unsafe.getChar(ix(checkIndex(i)))));
    }


    char getUnchecked(int i) {
        return (Bits.swap(unsafe.getChar(ix(i))));
    }


    public CharBuffer get(char[] dst, int offset, int length) {

        if (((long)length << 1) > Bits.JNI_COPY_TO_ARRAY_THRESHOLD) {
            checkBounds(offset, length, dst.length);
            int pos = position();
            int lim = limit();
            assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);
            if (length > rem)
                throw new BufferUnderflowException();


            if (order() != ByteOrder.nativeOrder())
                Bits.copyToCharArray(ix(pos), dst,
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



    public CharBuffer put(char x) {

        unsafe.putChar(ix(nextPutIndex()), Bits.swap((x)));
        return this;



    }

    public CharBuffer put(int i, char x) {

        unsafe.putChar(ix(checkIndex(i)), Bits.swap((x)));
        return this;



    }

    public CharBuffer put(CharBuffer src) {

        if (src instanceof DirectCharBufferS) {
            if (src == this)
                throw new IllegalArgumentException();
            DirectCharBufferS sb = (DirectCharBufferS)src;

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

    public CharBuffer put(char[] src, int offset, int length) {

        if (((long)length << 1) > Bits.JNI_COPY_FROM_ARRAY_THRESHOLD) {
            checkBounds(offset, length, src.length);
            int pos = position();
            int lim = limit();
            assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);
            if (length > rem)
                throw new BufferOverflowException();


            if (order() != ByteOrder.nativeOrder())
                Bits.copyFromCharArray(src,
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

    public CharBuffer compact() {

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




    public String toString(int start, int end) {
        // 检查索引是否越界
        if ((end > limit()) || (start > end))
            throw new IndexOutOfBoundsException();
        try {
            int len = end - start;
            char[] ca = new char[len];
            CharBuffer cb = CharBuffer.wrap(ca);
            CharBuffer db = this.duplicate();
            db.position(start);
            db.limit(end);
            cb.put(db);
            return new String(ca);
        } catch (StringIndexOutOfBoundsException x) {
            throw new IndexOutOfBoundsException();
        }
    }


    // --- Methods to support CharSequence ---

    public CharBuffer subSequence(int start, int end) {
        int pos = position();
        int lim = limit();
        assert (pos <= lim);
        pos = (pos <= lim ? pos : lim);
        int len = lim - pos;

        // 检查索引是否越界
        if ((start < 0) || (end > len) || (start > end))
            throw new IndexOutOfBoundsException();
        return new DirectCharBufferS(this,
                                            -1,
                                            pos + start,
                                            pos + end,
                                            capacity(),
                                            offset);
    }







    public ByteOrder order() {

        // 返回与本机字节序相反的字节序
        return ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN)
                ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);





    }


























}
