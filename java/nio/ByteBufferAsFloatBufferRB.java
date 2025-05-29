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


class ByteBufferAsFloatBufferRB                  // 包私有
    extends ByteBufferAsFloatBufferB
{








    ByteBufferAsFloatBufferRB(ByteBuffer bb) {   // 包私有












        super(bb);

    }

    ByteBufferAsFloatBufferRB(ByteBuffer bb,
                                     int mark, int pos, int lim, int cap,
                                     int off)
    {





        super(bb, mark, pos, lim, cap, off);

    }

    public FloatBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        int rem = (pos <= lim ? lim - pos : 0);
        int off = (pos << 2) + offset;
        assert (off >= 0);
        return new ByteBufferAsFloatBufferRB(bb, -1, 0, rem, rem, off);
    }

    public FloatBuffer duplicate() {
        return new ByteBufferAsFloatBufferRB(bb,
                                                    this.markValue(),
                                                    this.position(),
                                                    this.limit(),
                                                    this.capacity(),
                                                    offset);
    }

    public FloatBuffer asReadOnlyBuffer() {








        return duplicate();

    }























    public FloatBuffer put(float x) {




        throw new ReadOnlyBufferException();

    }

    public FloatBuffer put(int i, float x) {




        throw new ReadOnlyBufferException();

    }

    public FloatBuffer compact() {

















        throw new ReadOnlyBufferException();

    }

    public boolean isDirect() {
        return bb.isDirect();
    }

    public boolean isReadOnly() {
        return true;
    }











































    public ByteOrder order() {

        return ByteOrder.BIG_ENDIAN;




    }

}
