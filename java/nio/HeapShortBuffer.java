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


/**
 * 一个读/写 HeapShortBuffer。
 *
 *
 */

class HeapShortBuffer
    extends ShortBuffer
{

    // 为了提高速度，这些字段实际上是在 X-Buffer 中声明的；
    // 这些声明在这里作为文档
    /*

    protected final short[] hb;
    protected final int offset;

    */

    HeapShortBuffer(int cap, int lim) {            // 包私有

        super(-1, 0, lim, cap, new short[cap], 0);
        /*
        hb = new short[cap];
        offset = 0;
        */



    }

    HeapShortBuffer(short[] buf, int off, int len) { // 包私有

        super(-1, off, off + len, buf.length, buf, 0);
        /*
        hb = buf;
        offset = 0;
        */



    }

    protected HeapShortBuffer(short[] buf,
                                   int mark, int pos, int lim, int cap,
                                   int off)
    {

        super(mark, pos, lim, cap, buf, off);
        /*
        hb = buf;
        offset = off;
        */



    }

    public ShortBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        return new HeapShortBuffer(hb,
                                        -1,
                                        0,
                                        rem,
                                        rem,
                                        pos + offset);
    }

    public ShortBuffer duplicate() {
        return new HeapShortBuffer(hb,
                                        this.markValue(),
                                        this.position(),
                                        this.limit(),
                                        this.capacity(),
                                        offset);
    }

    public ShortBuffer asReadOnlyBuffer() {

        return new HeapShortBufferR(hb,
                                     this.markValue(),
                                     this.position(),
                                     this.limit(),
                                     this.capacity(),
                                     offset);



    }



    protected int ix(int i) {
        return i + offset;
    }

    public short get() {
        return hb[ix(nextGetIndex())];
    }

    public short get(int i) {
        return hb[ix(checkIndex(i))];
    }





    public ShortBuffer get(short[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        if (length > remaining())
            throw new BufferUnderflowException();
        System.arraycopy(hb, ix(position()), dst, offset, length);
        position(position() + length);
        return this;
    }

    public boolean isDirect() {
        return false;
    }



    public boolean isReadOnly() {
        return false;
    }

    public ShortBuffer put(short x) {

        hb[ix(nextPutIndex())] = x;
        return this;



    }

    public ShortBuffer put(int i, short x) {

        hb[ix(checkIndex(i))] = x;
        return this;



    }

    public ShortBuffer put(short[] src, int offset, int length) {

        checkBounds(offset, length, src.length);
        if (length > remaining())
            throw new BufferOverflowException();
        System.arraycopy(src, offset, hb, ix(position()), length);
        position(position() + length);
        return this;



    }

    public ShortBuffer put(ShortBuffer src) {

        if (src instanceof HeapShortBuffer) {
            if (src == this)
                throw new IllegalArgumentException();
            HeapShortBuffer sb = (HeapShortBuffer)src;
            int spos = sb.position();
            int pos = position();
            int n = sb.remaining();
            if (n > remaining())
                throw new BufferOverflowException();
            System.arraycopy(sb.hb, sb.ix(spos),
                             hb, ix(pos), n);
            sb.position(spos + n);
            position(pos + n);
        } else if (src.isDirect()) {
            int n = src.remaining();
            if (n > remaining())
                throw new BufferOverflowException();
            src.get(hb, ix(position()), n);
            position(position() + n);
        } else {
            super.put(src);
        }
        return this;



    }

    public ShortBuffer compact() {

        System.arraycopy(hb, ix(position()), hb, ix(0), remaining());
        position(remaining());
        limit(capacity());
        discardMark();
        return this;



    }






































































































































































































































































































































    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }



}
