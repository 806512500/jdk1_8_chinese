/*
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.nio.charset;

import java.lang.ref.WeakReference;
import java.nio.*;
import java.util.Map;
import java.util.HashMap;


/**
 * 编码器结果状态的描述。
 *
 * <p> 字符集编码器，即解码器或编码器，从输入缓冲区中消耗字节（或字符），将其翻译，并将结果字符（或字节）写入输出缓冲区。编码过程因以下四类原因之一终止，这些原因由此类的实例描述：
 *
 * <ul>
 *
 *   <li><p> <i>输入不足</i> 是指没有更多输入需要处理，或者输入不足且需要额外输入。此条件由唯一的 {@link #UNDERFLOW} 结果对象表示，其 {@link #isUnderflow() isUnderflow} 方法返回 <tt>true</tt>。 </p></li>
 *
 *   <li><p> <i>输出溢出</i> 是指输出缓冲区中没有足够的空间。此条件由唯一的 {@link #OVERFLOW} 结果对象表示，其 {@link #isOverflow() isOverflow} 方法返回 <tt>true</tt>。 </p></li>
 *
 *   <li><p> <i>输入格式错误</i> 是指输入单元序列格式不正确。此类错误由 {@link #isMalformed() isMalformed} 方法返回 <tt>true</tt> 且 {@link #length() length} 方法返回格式错误序列长度的此类实例描述。对于给定长度的所有输入格式错误，此类有一个唯一的实例。 </p></li>
 *
 *   <li><p> <i>字符无法映射</i> 是指输入单元序列表示一个在输出字符集中无法表示的字符。此类错误由 {@link #isUnmappable() isUnmappable} 方法返回 <tt>true</tt> 且 {@link #length() length} 方法返回表示无法映射字符的输入序列长度的此类实例描述。对于给定长度的所有字符无法映射错误，此类有一个唯一的实例。 </p></li>
 *
 * </ul>
 *
 * <p> 为了方便，{@link #isError() isError} 方法对于描述输入格式错误和字符无法映射错误的结果对象返回 <tt>true</tt>，而对于描述输入不足或输出溢出条件的结果对象返回 <tt>false</tt>。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
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
     * 告诉此对象是否描述输入不足条件。
     *
     * @return  如果且仅当此对象表示输入不足时返回 <tt>true</tt>
     */
    public boolean isUnderflow() {
        return (type == CR_UNDERFLOW);
    }

    /**
     * 告诉此对象是否描述输出溢出条件。
     *
     * @return  如果且仅当此对象表示输出溢出时返回 <tt>true</tt>
     */
    public boolean isOverflow() {
        return (type == CR_OVERFLOW);
    }

    /**
     * 告诉此对象是否描述错误条件。
     *
     * @return  如果且仅当此对象表示输入格式错误或字符无法映射错误时返回 <tt>true</tt>
     */
    public boolean isError() {
        return (type >= CR_ERROR_MIN);
    }

    /**
     * 告诉此对象是否描述输入格式错误。
     *
     * @return  如果且仅当此对象表示输入格式错误时返回 <tt>true</tt>
     */
    public boolean isMalformed() {
        return (type == CR_MALFORMED);
    }

    /**
     * 告诉此对象是否描述字符无法映射错误。
     *
     * @return  如果且仅当此对象表示字符无法映射错误时返回 <tt>true</tt>
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
     *          如果此对象不描述错误条件，即 {@link #isError() isError} 不返回 <tt>true</tt>
     */
    public int length() {
        if (!isError())
            throw new UnsupportedOperationException();
        return length;
    }

    /**
     * 表示输入不足的结果对象，意味着输入缓冲区已被完全消耗，或者如果输入缓冲区尚未为空，则需要额外输入。
     */
    public static final CoderResult UNDERFLOW
        = new CoderResult(CR_UNDERFLOW, 0);

    /**
     * 表示输出溢出的结果对象，意味着输出缓冲区中没有足够的空间。
     */
    public static final CoderResult OVERFLOW
        = new CoderResult(CR_OVERFLOW, 0);

    private static abstract class Cache {

        private Map<Integer,WeakReference<CoderResult>> cache = null;

        protected abstract CoderResult create(int len);

        private synchronized CoderResult get(int len) {
            if (len <= 0)
                throw new IllegalArgumentException("非正长度");
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
     * 静态工厂方法，返回描述给定长度的输入格式错误的唯一对象。
     *
     * @param   length
     *          给定长度
     *
     * @return  请求的编码器结果对象
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
     * 静态工厂方法，返回描述给定长度的字符无法映射错误的唯一结果对象。
     *
     * @param   length
     *          给定长度
     *
     * @return  请求的编码器结果对象
     */
    public static CoderResult unmappableForLength(int length) {
        return unmappableCache.get(length);
    }

    /**
     * 抛出与此对象描述的结果相适应的异常。
     *
     * @throws  BufferUnderflowException
     *          如果此对象是 {@link #UNDERFLOW}
     *
     * @throws  BufferOverflowException
     *          如果此对象是 {@link #OVERFLOW}
     *
     * @throws  MalformedInputException
     *          如果此对象表示输入格式错误；异常的长度值将是此对象的长度
     *
     * @throws  UnmappableCharacterException
     *          如果此对象表示字符无法映射错误；异常的长度值将是此对象的长度
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
