
/*
 * 版权所有 (c) 2001, 2013，Oracle 及/或其附属公司。保留所有权利。
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

package java.nio.charset;

import java.lang.ref.WeakReference;
import java.nio.*;
import java.util.Map;
import java.util.HashMap;


/**
 * 编码器结果状态的描述。
 *
 * <p> 字符集编码器，即解码器或编码器，从输入缓冲区中消耗字节（或字符），将它们转换，并将结果字符（或字节）写入输出缓冲区。编码过程因以下四类原因之一终止，这些原因由此类的实例描述：
 *
 * <ul>
 *
 *   <li><p> <i>下溢</i> 在没有更多输入需要处理，或者输入不足且需要更多输入时报告。此条件由唯一的结果对象 {@link #UNDERFLOW} 表示，其 {@link #isUnderflow() isUnderflow} 方法返回 <tt>true</tt>。 </p></li>
 *
 *   <li><p> <i>上溢</i> 在输出缓冲区中剩余空间不足时报告。此条件由唯一的结果对象 {@link #OVERFLOW} 表示，其 {@link #isOverflow() isOverflow} 方法返回 <tt>true</tt>。 </p></li>
 *
 *   <li><p> <i>输入格式错误</i> 在输入单元序列格式不正确时报告。此类错误由此类的实例描述，其 {@link #isMalformed() isMalformed} 方法返回 <tt>true</tt>，其 {@link #length() length} 方法返回格式错误序列的长度。对于给定长度的所有输入格式错误，此类有一个唯一的实例。 </p></li>
 *
 *   <li><p> <i>无法映射的字符错误</i> 在输入单元序列表示一个无法在输出字符集中表示的字符时报告。此类错误由此类的实例描述，其 {@link #isUnmappable() isUnmappable} 方法返回 <tt>true</tt>，其 {@link #length() length} 方法返回表示无法映射字符的输入序列的长度。对于给定长度的所有无法映射的字符错误，此类有一个唯一的实例。 </p></li>
 *
 * </ul>
 *
 * <p> 为了方便，{@link #isError() isError} 方法对于描述输入格式错误和无法映射的字符错误的结果对象返回 <tt>true</tt>，但对于描述下溢或上溢条件的结果对象返回 <tt>false</tt>。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家小组
 * @since 1.4
 */

public class CoderResult {

    private static final int CR_UNDERFLOW  = 0;
    private static final int CR_OVERFLOW   = 1;
    private static final int CR_ERROR_MIN  = 2;
    private static final int CR_MALFORMED  = 2;
    private static final int CR_UNMAPPABLE = 3;

    private static final String[] names
        = { "UNDERFLOW", "OVERFLOW", "MALFORMED", "UNMAPPABLE" };

    private final int type;
    private final int length;

    private CoderResult(int type, int length) {
        this.type = type;
        this.length = length;
    }

    /**
     * 返回描述此编码器结果的字符串。
     *
     * @return  描述性字符串
     */
    public String toString() {
        String nm = names[type];
        return isError() ? nm + "[" + length + "]" : nm;
    }

    /**
     * 告诉此对象是否描述下溢条件。
     *
     * @return  如果且仅如果此对象表示下溢，则返回 <tt>true</tt>
     */
    public boolean isUnderflow() {
        return (type == CR_UNDERFLOW);
    }

    /**
     * 告诉此对象是否描述上溢条件。
     *
     * @return  如果且仅如果此对象表示上溢，则返回 <tt>true</tt>
     */
    public boolean isOverflow() {
        return (type == CR_OVERFLOW);
    }

    /**
     * 告诉此对象是否描述错误条件。
     *
     * @return  如果且仅如果此对象表示输入格式错误或无法映射的字符错误，则返回 <tt>true</tt>
     */
    public boolean isError() {
        return (type >= CR_ERROR_MIN);
    }

    /**
     * 告诉此对象是否描述输入格式错误。
     *
     * @return  如果且仅如果此对象表示输入格式错误，则返回 <tt>true</tt>
     */
    public boolean isMalformed() {
        return (type == CR_MALFORMED);
    }

    /**
     * 告诉此对象是否描述无法映射的字符错误。
     *
     * @return  如果且仅如果此对象表示无法映射的字符错误，则返回 <tt>true</tt>
     */
    public boolean isUnmappable() {
        return (type == CR_UNMAPPABLE);
    }

    /**
     * 返回此对象描述的错误输入的长度&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * @return  错误输入的长度，一个正整数
     *
     * @throws  UnsupportedOperationException
     *          如果此对象不描述错误条件，即，如果 {@link #isError() isError} 不返回 <tt>true</tt>
     */
    public int length() {
        if (!isError())
            throw new UnsupportedOperationException();
        return length;
    }

    /**
     * 表示下溢的结果对象，意味着输入缓冲区已被完全消耗，或者如果输入缓冲区尚未为空，则需要更多输入。
     */
    public static final CoderResult UNDERFLOW
        = new CoderResult(CR_UNDERFLOW, 0);

    /**
     * 表示上溢的结果对象，意味着输出缓冲区中的空间不足。
     */
    public static final CoderResult OVERFLOW
        = new CoderResult(CR_OVERFLOW, 0);

    private static abstract class Cache {

        private Map<Integer,WeakReference<CoderResult>> cache = null;

        protected abstract CoderResult create(int len);


                    private synchronized CoderResult get(int len) {
            if (len <= 0)
                throw new IllegalArgumentException("Non-positive length");
            Integer k = new Integer(len);
            WeakReference<CoderResult> w;
            CoderResult e = null;
            if (cache == null) {
                cache = new HashMap<Integer,WeakReference<CoderResult>>();
            } else if ((w = cache.get(k)) != null) {
                e = w.get();
            }
            if (e == null) {
                e = create(len);
                cache.put(k, new WeakReference<CoderResult>(e));
            }
            return e;
        }

    }

    private static Cache malformedCache
        = new Cache() {
                public CoderResult create(int len) {
                    return new CoderResult(CR_MALFORMED, len);
                }};

    /**
     * 静态工厂方法，返回描述给定长度的错误输入的唯一对象。
     *
     * @param   length
     *          给定的长度
     *
     * @return  请求的编码结果对象
     */
    public static CoderResult malformedForLength(int length) {
        return malformedCache.get(length);
    }

    private static Cache unmappableCache
        = new Cache() {
                public CoderResult create(int len) {
                    return new CoderResult(CR_UNMAPPABLE, len);
                }};

    /**
     * 静态工厂方法，返回描述给定长度的不可映射字符错误的唯一结果对象。
     *
     * @param   length
     *          给定的长度
     *
     * @return  请求的编码结果对象
     */
    public static CoderResult unmappableForLength(int length) {
        return unmappableCache.get(length);
    }

    /**
     * 抛出与此对象描述的结果相应的异常。
     *
     * @throws  BufferUnderflowException
     *          如果此对象是 {@link #UNDERFLOW}
     *
     * @throws  BufferOverflowException
     *          如果此对象是 {@link #OVERFLOW}
     *
     * @throws  MalformedInputException
     *          如果此对象表示错误输入；异常的长度值将与此对象的长度相同
     *
     * @throws  UnmappableCharacterException
     *          如果此对象表示不可映射字符错误；异常的长度值将与此对象的长度相同
     */
    public void throwException()
        throws CharacterCodingException
    {
        switch (type) {
        case CR_UNDERFLOW:   throw new BufferUnderflowException();
        case CR_OVERFLOW:    throw new BufferOverflowException();
        case CR_MALFORMED:   throw new MalformedInputException(length);
        case CR_UNMAPPABLE:  throw new UnmappableCharacterException(length);
        default:
            assert false;
        }
    }

}
