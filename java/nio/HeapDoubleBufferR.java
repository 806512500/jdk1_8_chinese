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

// -- 本文件由机器生成：请勿编辑！ -- //

package java.nio;


/**



 * 一个只读的 HeapDoubleBuffer。此类扩展了相应的读/写类，重写变异方法以抛出 {@link
 * ReadOnlyBufferException}，并重写视图缓冲区方法以返回此类的实例而不是超类的实例。

 */

class HeapDoubleBufferR
    extends HeapDoubleBuffer
{

    // 为了速度，这些字段实际上是在 X-Buffer 中声明的；
    // 这些声明在此处作为文档
    /*




    */

    HeapDoubleBufferR(int cap, int lim) {            // 包私有







        super(cap, lim);
        this.isReadOnly = true;

    }

    HeapDoubleBufferR(double[] buf, int off, int len) { // 包私有







        super(buf, off, len);
        this.isReadOnly = true;

    }

    protected HeapDoubleBufferR(double[] buf,
                                   int mark, int pos, int lim, int cap,
                                   int off)
    {







        super(buf, mark, pos, lim, cap, off);
        this.isReadOnly = true;

    }

    public DoubleBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        return new HeapDoubleBufferR(hb,
                                        -1,
                                        0,
                                        rem,
                                        rem,
                                        pos + offset);
    }

    public DoubleBuffer duplicate() {
        return new HeapDoubleBufferR(hb,
                                        this.markValue(),
                                        this.position(),
                                        this.limit(),
                                        this.capacity(),
                                        offset);
    }

    public DoubleBuffer asReadOnlyBuffer() {








        return duplicate();

    }




































    public boolean isReadOnly() {
        return true;
    }

    public DoubleBuffer put(double x) {




        throw new ReadOnlyBufferException();

    }

    public DoubleBuffer put(int i, double x) {




        throw new ReadOnlyBufferException();

    }

    public DoubleBuffer put(double[] src, int offset, int length) {








        throw new ReadOnlyBufferException();

    }

    public DoubleBuffer put(DoubleBuffer src) {

























        throw new ReadOnlyBufferException();

    }

    public DoubleBuffer compact() {







        throw new ReadOnlyBufferException();

    }






































































































































































































































































































































    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }



}
