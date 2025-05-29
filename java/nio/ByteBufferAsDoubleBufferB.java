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


class ByteBufferAsDoubleBufferB                  // 包私有
    extends DoubleBuffer
{



    protected final ByteBuffer bb;
    protected final int offset;



    ByteBufferAsDoubleBufferB(ByteBuffer bb) {   // 包私有

        super(-1, 0,
              bb.remaining() >> 3,
              bb.remaining() >> 3);
        this.bb = bb;
        // 强制限制 == 容量
        int cap = this.capacity();
        this.limit(cap);
        int pos = this.position();
        assert (pos <= cap);
        offset = pos;



    }

    ByteBufferAsDoubleBufferB(ByteBuffer bb,
                                     int mark, int pos, int lim, int cap,
                                     int off)
    {

        super(mark, pos, lim, cap);
        this.bb = bb;
        offset = off;



    }

    public DoubleBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        int off = (pos << 3) + offset;
        assert (off >= 0);
        return new ByteBufferAsDoubleBufferB(bb, -1, 0, rem, rem, off);
    }

    public DoubleBuffer duplicate() {
        return new ByteBufferAsDoubleBufferB(bb,
                                                    this.markValue(),
                                                    this.position(),
                                                    this.limit(),
                                                    this.capacity(),
                                                    offset);
    }

    public DoubleBuffer asReadOnlyBuffer() {

        return new ByteBufferAsDoubleBufferRB(bb,
                                                 this.markValue(),
                                                 this.position(),
                                                 this.limit(),
                                                 this.capacity(),
                                                 offset);



    }



    protected int ix(int i) {
        return (i << 3) + offset;
    }

    public double get() {
        return Bits.getDoubleB(bb, ix(nextGetIndex()));
    }

    public double get(int i) {
        return Bits.getDoubleB(bb, ix(checkIndex(i)));
    }









    public DoubleBuffer put(double x) {

        Bits.putDoubleB(bb, ix(nextPutIndex()), x);
        return this;



    }

    public DoubleBuffer put(int i, double x) {

        Bits.putDoubleB(bb, ix(checkIndex(i)), x);
        return this;



    }

    public DoubleBuffer compact() {

        int pos = position();
        int lim = limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);

        ByteBuffer db = bb.duplicate();
        db.limit(ix(lim));
        db.position(ix(0));
        ByteBuffer sb = db.slice();
        sb.position(pos << 3);
        sb.compact();
        position(rem);
        limit(capacity());
        discardMark();
        return this;



    }

    public boolean isDirect() {
        return bb.isDirect();
    }

    public boolean isReadOnly() {
        return false;
    }











































    public ByteOrder order() {

        return ByteOrder.BIG_ENDIAN;




    }

}
