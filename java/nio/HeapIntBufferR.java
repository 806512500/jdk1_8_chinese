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
 * 一个只读的 HeapIntBuffer。此类扩展了相应的读/写类，重写变异方法以抛出 {@link
 * ReadOnlyBufferException}，并重写视图缓冲区方法以返回此类的实例而不是超类的实例。
 */

class HeapIntBufferR
    extends HeapIntBuffer
{

    // 为了速度，这些字段实际上是在 X-Buffer 中声明的；
    // 这些声明在这里作为文档
    /*




    */

    HeapIntBufferR(int cap, int lim) {            // package-private







        super(cap, lim);
        this.isReadOnly = true;

    }

    HeapIntBufferR(int[] buf, int off, int len) { // package-private







        super(buf, off, len);
        this.isReadOnly = true;

    }

    protected HeapIntBufferR(int[] buf,
                                   int mark, int pos, int lim, int cap,
                                   int off)
    {







        super(buf, mark, pos, lim, cap, off);
        this.isReadOnly = true;

    }

    public IntBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        return new HeapIntBufferR(hb,
                                        -1,
                                        0,
                                        rem,
                                        rem,
                                        pos + offset);
    }

    public IntBuffer duplicate() {
        return new HeapIntBufferR(hb,
                                        this.markValue(),
                                        this.position(),
                                        this.limit(),
                                        this.capacity(),
                                        offset);
    }

    public IntBuffer asReadOnlyBuffer() {








        return duplicate();

    }




































    public boolean isReadOnly() {
        return true;
    }

    public IntBuffer put(int x) {




        throw new ReadOnlyBufferException();

    }

    public IntBuffer put(int i, int x) {




        throw new ReadOnlyBufferException();

    }

    public IntBuffer put(int[] src, int offset, int length) {








        throw new ReadOnlyBufferException();

    }

    public IntBuffer put(IntBuffer src) {

























        throw new ReadOnlyBufferException();

    }

    public IntBuffer compact() {







        throw new ReadOnlyBufferException();

    }






































































































































































































































































































































    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }



}
