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



 * 一个只读的 HeapByteBuffer。此类扩展了对应的读/写类，重写突变方法以抛出 {@link
 * ReadOnlyBufferException} 并重写视图缓冲区方法以返回此类的实例而不是超类的实例。

 */

class HeapByteBufferR
    extends HeapByteBuffer
{

    // 为了速度，这些字段实际上是在 X-Buffer 中声明的；
    // 这些声明在这里作为文档
    /*




    */

    HeapByteBufferR(int cap, int lim) {            // 包私有







        super(cap, lim);
        this.isReadOnly = true;

    }

    HeapByteBufferR(byte[] buf, int off, int len) { // 包私有







        super(buf, off, len);
        this.isReadOnly = true;

    }

    protected HeapByteBufferR(byte[] buf,
                                   int mark, int pos, int lim, int cap,
                                   int off)
    {







        super(buf, mark, pos, lim, cap, off);
        this.isReadOnly = true;

    }

    public ByteBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        return new HeapByteBufferR(hb,
                                        -1,
                                        0,
                                        rem,
                                        rem,
                                        pos + offset);
    }

    public ByteBuffer duplicate() {
        return new HeapByteBufferR(hb,
                                        this.markValue(),
                                        this.position(),
                                        this.limit(),
                                        this.capacity(),
                                        offset);
    }

    public ByteBuffer asReadOnlyBuffer() {








        return duplicate();

    }




































    public boolean isReadOnly() {
        return true;
    }

    public ByteBuffer put(byte x) {




        throw new ReadOnlyBufferException();

    }

    public ByteBuffer put(int i, byte x) {




        throw new ReadOnlyBufferException();

    }

    public ByteBuffer put(byte[] src, int offset, int length) {








        throw new ReadOnlyBufferException();

    }

    public ByteBuffer put(ByteBuffer src) {

























        throw new ReadOnlyBufferException();

    }

    public ByteBuffer compact() {







        throw new ReadOnlyBufferException();

    }





    byte _get(int i) {                          // 包私有
        return hb[i];
    }

    void _put(int i, byte b) {                  // 包私有



        throw new ReadOnlyBufferException();

    }

    // char













    public ByteBuffer putChar(char x) {




        throw new ReadOnlyBufferException();

    }

    public ByteBuffer putChar(int i, char x) {




        throw new ReadOnlyBufferException();

    }

    public CharBuffer asCharBuffer() {
        int size = this.remaining() >> 1;
        int off = offset + position();
        return (bigEndian
                ? (CharBuffer)(new ByteBufferAsCharBufferRB(this,
                                                               -1,
                                                               0,
                                                               size,
                                                               size,
                                                               off))
                : (CharBuffer)(new ByteBufferAsCharBufferRL(this,
                                                               -1,
                                                               0,
                                                               size,
                                                               size,
                                                               off)));
    }


    // short













    public ByteBuffer putShort(short x) {




        throw new ReadOnlyBufferException();

    }

    public ByteBuffer putShort(int i, short x) {




        throw new ReadOnlyBufferException();

    }

    public ShortBuffer asShortBuffer() {
        int size = this.remaining() >> 1;
        int off = offset + position();
        return (bigEndian
                ? (ShortBuffer)(new ByteBufferAsShortBufferRB(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off))
                : (ShortBuffer)(new ByteBufferAsShortBufferRL(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off)));
    }


    // int













    public ByteBuffer putInt(int x) {




        throw new ReadOnlyBufferException();

    }

    public ByteBuffer putInt(int i, int x) {




        throw new ReadOnlyBufferException();

    }

    public IntBuffer asIntBuffer() {
        int size = this.remaining() >> 2;
        int off = offset + position();
        return (bigEndian
                ? (IntBuffer)(new ByteBufferAsIntBufferRB(this,
                                                             -1,
                                                             0,
                                                             size,
                                                             size,
                                                             off))
                : (IntBuffer)(new ByteBufferAsIntBufferRL(this,
                                                             -1,
                                                             0,
                                                             size,
                                                             size,
                                                             off)));
    }


    // 长整型













    public ByteBuffer putLong(long x) {




        throw new ReadOnlyBufferException();

    }

    public ByteBuffer putLong(int i, long x) {




        throw new ReadOnlyBufferException();

    }

    public LongBuffer asLongBuffer() {
        int size = this.remaining() >> 3;
        int off = offset + position();
        return (bigEndian
                ? (LongBuffer)(new ByteBufferAsLongBufferRB(this,
                                                               -1,
                                                               0,
                                                               size,
                                                               size,
                                                               off))
                : (LongBuffer)(new ByteBufferAsLongBufferRL(this,
                                                               -1,
                                                               0,
                                                               size,
                                                               size,
                                                               off)));
    }


    // 浮点型













    public ByteBuffer putFloat(float x) {




        throw new ReadOnlyBufferException();

    }

    public ByteBuffer putFloat(int i, float x) {




        throw new ReadOnlyBufferException();

    }

    public FloatBuffer asFloatBuffer() {
        int size = this.remaining() >> 2;
        int off = offset + position();
        return (bigEndian
                ? (FloatBuffer)(new ByteBufferAsFloatBufferRB(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off))
                : (FloatBuffer)(new ByteBufferAsFloatBufferRL(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off)));
    }


    // 双精度浮点型













    public ByteBuffer putDouble(double x) {




        throw new ReadOnlyBufferException();

    }

    public ByteBuffer putDouble(int i, double x) {




        throw new ReadOnlyBufferException();

    }

    public DoubleBuffer asDoubleBuffer() {
        int size = this.remaining() >> 3;
        int off = offset + position();
        return (bigEndian
                ? (DoubleBuffer)(new ByteBufferAsDoubleBufferRB(this,
                                                                   -1,
                                                                   0,
                                                                   size,
                                                                   size,
                                                                   off))
                : (DoubleBuffer)(new ByteBufferAsDoubleBufferRL(this,
                                                                   -1,
                                                                   0,
                                                                   size,
                                                                   size,
                                                                   off)));
    }











































}
