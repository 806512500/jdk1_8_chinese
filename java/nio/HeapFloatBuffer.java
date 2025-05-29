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
 * 一个读/写堆浮点缓冲区。
 */


class HeapFloatBuffer
    extends FloatBuffer
{

    // 为了速度，这些字段实际上是在 X-Buffer 中声明的；
    // 这些声明在这里作为文档
    /*

    protected final float[] hb;
    protected final int offset;

    */

    HeapFloatBuffer(int cap, int lim) {            // 包私有

        super(-1, 0, lim, cap, new float[cap], 0);
        /*
        hb = new float[cap];
        offset = 0;
        */




    }

    HeapFloatBuffer(float[] buf, int off, int len) { // 包私有

        super(-1, off, off + len, buf.length, buf, 0);
        /*
        hb = buf;
        offset = 0;
        */




    }

    protected HeapFloatBuffer(float[] buf,
                                   int mark, int pos, int lim, int cap,
                                   int off)
    {

        super(mark, pos, lim, cap, buf, off);
        /*
        hb = buf;
        offset = off;
        */




    }

    public FloatBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        return new HeapFloatBuffer(hb,
                                        -1,
                                        0,
                                        rem,
                                        rem,
                                        pos + offset);
    }

    public FloatBuffer duplicate() {
        return new HeapFloatBuffer(hb,
                                        this.markValue(),
                                        this.position(),
                                        this.limit(),
                                        this.capacity(),
                                        offset);
    }

    public FloatBuffer asReadOnlyBuffer() {

        return new HeapFloatBufferR(hb,
                                     this.markValue(),
                                     this.position(),
                                     this.limit(),
                                     this.capacity(),
                                     offset);



    }



    protected int ix(int i) {
        return i + offset;
    }

    public float get() {
        return hb[ix(nextGetIndex())];
    }

    public float get(int i) {
        return hb[ix(checkIndex(i))];
    }







    public FloatBuffer get(float[] dst, int offset, int length) {
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

    public FloatBuffer put(float x) {

        hb[ix(nextPutIndex())] = x;
        return this;



    }

    public FloatBuffer put(int i, float x) {

        hb[ix(checkIndex(i))] = x;
        return this;



    }

    public FloatBuffer put(float[] src, int offset, int length) {

        checkBounds(offset, length, src.length);
        if (length > remaining())
            throw new BufferOverflowException();
        System.arraycopy(src, offset, hb, ix(position()), length);
        position(position() + length);
        return this;



    }

    public FloatBuffer put(FloatBuffer src) {

        if (src instanceof HeapFloatBuffer) {
            if (src == this)
                throw new IllegalArgumentException();
            HeapFloatBuffer sb = (HeapFloatBuffer)src;
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

    public FloatBuffer compact() {

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
