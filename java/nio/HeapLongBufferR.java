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



 * 一个只读的 HeapLongBuffer。这个类扩展了相应的读/写类，重写了变异方法以抛出 {@link
 * ReadOnlyBufferException}，并重写了视图缓冲区方法以返回这个类的实例而不是超类的实例。

 */

class HeapLongBufferR
    extends HeapLongBuffer
{

    // 为了速度，这些字段实际上是在 X-Buffer 中声明的；
    // 这些声明在这里作为文档
    /*




    */

    HeapLongBufferR(int cap, int lim) {            // package-private







        super(cap, lim);
        this.isReadOnly = true;

    }

    HeapLongBufferR(long[] buf, int off, int len) { // package-private







        super(buf, off, len);
        this.isReadOnly = true;

    }

    protected HeapLongBufferR(long[] buf,
                                   int mark, int pos, int lim, int cap,
                                   int off)
    {







        super(buf, mark, pos, lim, cap, off);
        this.isReadOnly = true;

    }

    public LongBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        return new HeapLongBufferR(hb,
                                        -1,
                                        0,
                                        rem,
                                        rem,
                                        pos + offset);
    }

    public LongBuffer duplicate() {
        return new HeapLongBufferR(hb,
                                        this.markValue(),
                                        this.position(),
                                        this.limit(),
                                        this.capacity(),
                                        offset);
    }

    public LongBuffer asReadOnlyBuffer() {








        return duplicate();

    }




































    public boolean isReadOnly() {
        return true;
    }

    public LongBuffer put(long x) {




        throw new ReadOnlyBufferException();

    }

    public LongBuffer put(int i, long x) {




        throw new ReadOnlyBufferException();

    }

    public LongBuffer put(long[] src, int offset, int length) {








        throw new ReadOnlyBufferException();

    }

    public LongBuffer put(LongBuffer src) {

























        throw new ReadOnlyBufferException();

    }

    public LongBuffer compact() {







        throw new ReadOnlyBufferException();

    }






































































































































































































































































































































    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }



}
