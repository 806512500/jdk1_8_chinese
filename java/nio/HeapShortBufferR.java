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
 * 一个只读的 HeapShortBuffer。这个类扩展了相应的读/写类，重写了变异方法以抛出 {@link
 * ReadOnlyBufferException}，并重写了视图缓冲区方法以返回这个类的实例而不是超类的实例。
 */

class HeapShortBufferR
    extends HeapShortBuffer
{

    // 为了速度，这些字段实际上是在 X-Buffer 中声明的；
    // 这些声明在这里作为文档
    /*




    */

    HeapShortBufferR(int cap, int lim) {            // package-private







        super(cap, lim);
        this.isReadOnly = true;

    }

    HeapShortBufferR(short[] buf, int off, int len) { // package-private







        super(buf, off, len);
        this.isReadOnly = true;

    }

    protected HeapShortBufferR(short[] buf,
                                   int mark, int pos, int lim, int cap,
                                   int off)
    {







        super(buf, mark, pos, lim, cap, off);
        this.isReadOnly = true;

    }

    public ShortBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        return new HeapShortBufferR(hb,
                                        -1,
                                        0,
                                        rem,
                                        rem,
                                        pos + offset);
    }

    public ShortBuffer duplicate() {
        return new HeapShortBufferR(hb,
                                        this.markValue(),
                                        this.position(),
                                        this.limit(),
                                        this.capacity(),
                                        offset);
    }

    public ShortBuffer asReadOnlyBuffer() {








        return duplicate();

    }

    public boolean isReadOnly() {
        return true;
    }

    public ShortBuffer put(short x) {




        throw new ReadOnlyBufferException();

    }

    public ShortBuffer put(int i, short x) {




        throw new ReadOnlyBufferException();

    }

    public ShortBuffer put(short[] src, int offset, int length) {








        throw new ReadOnlyBufferException();

    }

    public ShortBuffer put(ShortBuffer src) {

























        throw new ReadOnlyBufferException();

    }

    public ShortBuffer compact() {







        throw new ReadOnlyBufferException();

    }

    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }



}
