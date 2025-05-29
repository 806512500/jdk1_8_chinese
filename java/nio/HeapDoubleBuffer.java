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
 * 一个读/写 HeapDoubleBuffer。
 */



class HeapDoubleBuffer
    extends DoubleBuffer
{

    // 为了速度，这些字段实际上在 X-Buffer 中声明；
    // 这些声明在这里作为文档
    /*

    protected final double[] hb;
    protected final int offset;

    */

    HeapDoubleBuffer(int cap, int lim) {            // 包私有

        super(-1, 0, lim, cap, new double[cap], 0);
        /*
        hb = new double[cap];
        offset = 0;
        */




    }

    HeapDoubleBuffer(double[] buf, int off, int len) { // 包私有

        super(-1, off, off + len, buf.length, buf, 0);
        /*
        hb = buf;
        offset = 0;
        */




    }

    protected HeapDoubleBuffer(double[] buf,
                                   int mark, int pos, int lim, int cap,
                                   int off)
    {

        super(mark, pos, lim, cap, buf, off);
        /*
        hb = buf;
        offset = off;
        */




    }

    public DoubleBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        return new HeapDoubleBuffer(hb,
                                        -1,
                                        0,
                                        rem,
                                        rem,
                                        pos + offset);
    }

    public DoubleBuffer duplicate() {
        return new HeapDoubleBuffer(hb,
                                        this.markValue(),
                                        this.position(),
                                        this.limit(),
                                        this.capacity(),
                                        offset);
    }

    public DoubleBuffer asReadOnlyBuffer() {

        return new HeapDoubleBufferR(hb,
                                     this.markValue(),
                                     this.position(),
                                     this.limit(),
                                     this.capacity(),
                                     offset);



    }



    protected int ix(int i) {
        return i + offset;
    }

    public double get() {
        return hb[ix(nextGetIndex())];
    }

    public double get(int i) {
        return hb[ix(checkIndex(i))];
    }







    public DoubleBuffer get(double[] dst, int offset, int length) {
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

    public DoubleBuffer put(double x) {

        hb[ix(nextPutIndex())] = x;
        return this;



    }

    public DoubleBuffer put(int i, double x) {

        hb[ix(checkIndex(i))] = x;
        return this;



    }

    public DoubleBuffer put(double[] src, int offset, int length) {

        checkBounds(offset, length, src.length);
        if (length > remaining())
            throw new BufferOverflowException();
        System.arraycopy(src, offset, hb, ix(position()), length);
        position(position() + length);
        return this;



    }

    public DoubleBuffer put(DoubleBuffer src) {

        if (src instanceof HeapDoubleBuffer) {
            if (src == this)
                throw new IllegalArgumentException();
            HeapDoubleBuffer sb = (HeapDoubleBuffer)src;
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

    public DoubleBuffer compact() {

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
