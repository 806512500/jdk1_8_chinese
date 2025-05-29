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
 * 一个只读的 HeapFloatBuffer。这个类扩展了相应的读/写类，重写变异方法以抛出 {@link
 * ReadOnlyBufferException}，并重写视图缓冲区方法以返回此类的实例而不是超类的实例。
 */

class HeapFloatBufferR
    extends HeapFloatBuffer
{

    // 为了速度，这些字段实际上是在 X-Buffer 中声明的；
    // 这些声明在这里作为文档
    /*




    */

    HeapFloatBufferR(int cap, int lim) {            // 包私有







        super(cap, lim);
        this.isReadOnly = true;

    }

    HeapFloatBufferR(float[] buf, int off, int len) { // 包私有







        super(buf, off, len);
        this.isReadOnly = true;

    }

    protected HeapFloatBufferR(float[] buf,
                                   int mark, int pos, int lim, int cap,
                                   int off)
    {







        super(buf, mark, pos, lim, cap, off);
        this.isReadOnly = true;

    }

    public FloatBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        return new HeapFloatBufferR(hb,
                                        -1,
                                        0,
                                        rem,
                                        rem,
                                        pos + offset);
    }

    public FloatBuffer duplicate() {
        return new HeapFloatBufferR(hb,
                                        this.markValue(),
                                        this.position(),
                                        this.limit(),
                                        this.capacity(),
                                        offset);
    }

    public FloatBuffer asReadOnlyBuffer() {








        return duplicate();

    }




































    public boolean isReadOnly() {
        return true;
    }

    public FloatBuffer put(float x) {




        throw new ReadOnlyBufferException();

    }

    public FloatBuffer put(int i, float x) {




        throw new ReadOnlyBufferException();

    }

    public FloatBuffer put(float[] src, int offset, int length) {








        throw new ReadOnlyBufferException();

    }

    public FloatBuffer put(FloatBuffer src) {

























        throw new ReadOnlyBufferException();

    }

    public FloatBuffer compact() {







        throw new ReadOnlyBufferException();

    }






































































































































































































































































































































    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }



}
