
/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

import java.io.FilterOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 本类仅包含用于获取 Base64 编码方案的编码器和解码器的静态方法。此类的实现支持以下类型的 Base64，
 * 如 <a href="http://www.ietf.org/rfc/rfc4648.txt">RFC 4648</a> 和
 * <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a> 中所指定。
 *
 * <ul>
 * <li><a name="basic"><b>基本</b></a>
 * <p> 使用 RFC 4648 和 RFC 2045 表 1 中指定的 "Base64 字母表" 进行编码和解码操作。
 *     编码器不会添加任何换行符（行分隔符）。解码器会拒绝包含不在 base64 字母表中的字符的数据。</p></li>
 *
 * <li><a name="url"><b>URL 和文件名安全</b></a>
 * <p> 使用 RFC 4648 表 2 中指定的 "URL 和文件名安全 Base64 字母表" 进行编码和解码。编码器不会添加任何换行符（行分隔符）。
 *     解码器会拒绝包含不在 base64 字母表中的字符的数据。</p></li>
 *
 * <li><a name="mime"><b>MIME</b></a>
 * <p> 使用 RFC 2045 表 1 中指定的 "Base64 字母表" 进行编码和解码操作。编码后的输出必须表示为每行不超过 76 个字符，
 *     并使用回车 {@code '\r'} 紧跟一个换行符 {@code '\n'} 作为行分隔符。编码输出的末尾不会添加行分隔符。
 *     解码操作中会忽略所有行分隔符或其他不在 base64 字母表中的字符。</p></li>
 * </ul>
 *
 * <p> 除非另有说明，否则将 {@code null} 参数传递给此类的任何方法将导致抛出 {@link java.lang.NullPointerException
 * NullPointerException}。</p>
 *
 * @author  Xueming Shen
 * @since   1.8
 */

public class Base64 {

    private Base64() {}

    /**
     * 返回一个使用 <a href="#basic">基本</a> 类型 base64 编码方案的 {@link Encoder}。
     *
     * @return  一个 Base64 编码器。
     */
    public static Encoder getEncoder() {
         return Encoder.RFC4648;
    }

    /**
     * 返回一个使用 <a href="#url">URL 和文件名安全</a> 类型 base64 编码方案的 {@link Encoder}。
     *
     * @return  一个 Base64 编码器。
     */
    public static Encoder getUrlEncoder() {
         return Encoder.RFC4648_URLSAFE;
    }

    /**
     * 返回一个使用 <a href="#mime">MIME</a> 类型 base64 编码方案的 {@link Encoder}。
     *
     * @return  一个 Base64 编码器。
     */
    public static Encoder getMimeEncoder() {
        return Encoder.RFC2045;
    }

    /**
     * 返回一个使用 <a href="#mime">MIME</a> 类型 base64 编码方案的 {@link Encoder}，并指定行长度和行分隔符。
     *
     * @param   lineLength
     *          每个输出行的长度（向下取最近的 4 的倍数）。如果 {@code lineLength <= 0}，则输出不会分隔成行。
     * @param   lineSeparator
     *          每个输出行的行分隔符。
     *
     * @return  一个 Base64 编码器。
     *
     * @throws  IllegalArgumentException 如果 {@code lineSeparator} 包含 RFC 2045 表 1 中指定的 "Base64 字母表" 中的任何字符。
     */
    public static Encoder getMimeEncoder(int lineLength, byte[] lineSeparator) {
         Objects.requireNonNull(lineSeparator);
         int[] base64 = Decoder.fromBase64;
         for (byte b : lineSeparator) {
             if (base64[b & 0xff] != -1)
                 throw new IllegalArgumentException(
                     "非法的 base64 行分隔符字符 0x" + Integer.toString(b, 16));
         }
         if (lineLength <= 0) {
             return Encoder.RFC4648;
         }
         return new Encoder(false, lineSeparator, lineLength >> 2 << 2, true);
    }

    /**
     * 返回一个使用 <a href="#basic">基本</a> 类型 base64 编码方案的 {@link Decoder}。
     *
     * @return  一个 Base64 解码器。
     */
    public static Decoder getDecoder() {
         return Decoder.RFC4648;
    }

    /**
     * 返回一个使用 <a href="#url">URL 和文件名安全</a> 类型 base64 编码方案的 {@link Decoder}。
     *
     * @return  一个 Base64 解码器。
     */
    public static Decoder getUrlDecoder() {
         return Decoder.RFC4648_URLSAFE;
    }

    /**
     * 返回一个使用 <a href="#mime">MIME</a> 类型 base64 编码方案的 {@link Decoder}。
     *
     * @return  一个 Base64 解码器。
     */
    public static Decoder getMimeDecoder() {
         return Decoder.RFC2045;
    }


                /**
     * 该类实现了一个编码器，用于使用 RFC 4648 和 RFC 2045 中指定的 Base64 编码方案对字节数据进行编码。
     *
     * <p> {@link Encoder} 类的实例可以安全地被多个并发线程使用。
     *
     * <p> 除非另有说明，否则将 {@code null} 参数传递给此类的方法将导致抛出
     * {@link java.lang.NullPointerException NullPointerException}。
     *
     * @see     Decoder
     * @since   1.8
     */
    public static class Encoder {

        private final byte[] newline;
        private final int linemax;
        private final boolean isURL;
        private final boolean doPadding;

        private Encoder(boolean isURL, byte[] newline, int linemax, boolean doPadding) {
            this.isURL = isURL;
            this.newline = newline;
            this.linemax = linemax;
            this.doPadding = doPadding;
        }

        /**
         * 这个数组是一个查找表，将 6 位正整数索引值转换为 RFC 2045（和 RFC 4648）中“表 1：Base64 字母表”指定的“Base64 字母表”等效值。
         */
        private static final char[] toBase64 = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
        };

        /**
         * 这是 RFC 4648 中表 2 指定的“URL 和文件名安全的 Base64”查找表，其中 '+' 和 '/' 被替换为 '-' 和 '_'。当指定 BASE64_URL 时使用此表。
         */
        private static final char[] toBase64URL = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
        };

        private static final int MIMELINEMAX = 76;
        private static final byte[] CRLF = new byte[] {'\r', '\n'};

        static final Encoder RFC4648 = new Encoder(false, null, -1, true);
        static final Encoder RFC4648_URLSAFE = new Encoder(true, null, -1, true);
        static final Encoder RFC2045 = new Encoder(false, CRLF, MIMELINEMAX, true);

        private final int outLength(int srclen) {
            int len = 0;
            if (doPadding) {
                len = 4 * ((srclen + 2) / 3);
            } else {
                int n = srclen % 3;
                len = 4 * (srclen / 3) + (n == 0 ? 0 : n + 1);
            }
            if (linemax > 0)                                  // 行分隔符
                len += (len - 1) / linemax * newline.length;
            return len;
        }

        /**
         * 使用 {@link Base64} 编码方案将指定字节数组中的所有字节编码为新分配的字节数组。返回的字节数组的长度为编码后的字节数。
         *
         * @param   src
         *          要编码的字节数组
         * @return  包含编码后的字节的新分配的字节数组。
         */
        public byte[] encode(byte[] src) {
            int len = outLength(src.length);          // dst 数组大小
            byte[] dst = new byte[len];
            int ret = encode0(src, 0, src.length, dst);
            if (ret != dst.length)
                 return Arrays.copyOf(dst, ret);
            return dst;
        }

        /**
         * 使用 {@link Base64} 编码方案将指定字节数组中的所有字节编码，并将结果字节写入给定的输出字节数组，从偏移量 0 开始。
         *
         * <p> 调用此方法的调用者有责任确保输出字节数组 {@code dst} 有足够的空间来编码所有输入字节。如果输出字节数组空间不足，则不会将任何字节写入输出字节数组。
         *
         * @param   src
         *          要编码的字节数组
         * @param   dst
         *          输出字节数组
         * @return  写入输出字节数组的字节数
         *
         * @throws  IllegalArgumentException 如果 {@code dst} 没有足够的空间来编码所有输入字节。
         */
        public int encode(byte[] src, byte[] dst) {
            int len = outLength(src.length);         // dst 数组大小
            if (dst.length < len)
                throw new IllegalArgumentException(
                    "输出字节数组太小，无法编码所有输入字节");
            return encode0(src, 0, src.length, dst);
        }

        /**
         * 使用 {@link Base64} 编码方案将指定字节数组编码为字符串。
         *
         * <p> 该方法首先将所有输入字节编码为 Base64 编码的字节数组，然后使用编码的字节数组和 {@link java.nio.charset.StandardCharsets#ISO_8859_1
         * ISO-8859-1} 字符集构造一个新的字符串。
         *
         * <p> 换句话说，调用此方法的效果与调用
         * {@code new String(encode(src), StandardCharsets.ISO_8859_1)} 完全相同。
         *
         * @param   src
         *          要编码的字节数组
         * @return  包含 Base64 编码字符的字符串
         */
        @SuppressWarnings("deprecation")
        public String encodeToString(byte[] src) {
            byte[] encoded = encode(src);
            return new String(encoded, 0, 0, encoded.length);
        }


                    /**
         * 将指定字节缓冲区中剩余的所有字节编码为使用 {@link Base64} 编码方案的新分配的 ByteBuffer。
         *
         * 返回时，源缓冲区的位置将更新为其限制；其限制不会改变。返回的输出缓冲区的位置将为零，其限制将是编码字节的数量。
         *
         * @param   buffer
         *          要编码的源 ByteBuffer。
         * @return  包含编码字节的新分配的字节缓冲区。
         */
        public ByteBuffer encode(ByteBuffer buffer) {
            int len = outLength(buffer.remaining());
            byte[] dst = new byte[len];
            int ret = 0;
            if (buffer.hasArray()) {
                ret = encode0(buffer.array(),
                              buffer.arrayOffset() + buffer.position(),
                              buffer.arrayOffset() + buffer.limit(),
                              dst);
                buffer.position(buffer.limit());
            } else {
                byte[] src = new byte[buffer.remaining()];
                buffer.get(src);
                ret = encode0(src, 0, src.length, dst);
            }
            if (ret != dst.length)
                 dst = Arrays.copyOf(dst, ret);
            return ByteBuffer.wrap(dst);
        }

        /**
         * 包装一个输出流，用于使用 {@link Base64} 编码方案编码字节数据。
         *
         * <p> 建议在使用后及时关闭返回的输出流，在此期间它将把所有可能的剩余字节刷新到底层输出流。关闭返回的输出流将关闭底层输出流。
         *
         * @param   os
         *          输出流。
         * @return  用于将字节数据编码为指定 Base64 编码格式的输出流。
         */
        public OutputStream wrap(OutputStream os) {
            Objects.requireNonNull(os);
            return new EncOutputStream(os, isURL ? toBase64URL : toBase64,
                                       newline, linemax, doPadding);
        }

        /**
         * 返回一个编码实例，其编码方式与当前实例相同，但不会在编码字节数据的末尾添加任何填充字符。
         *
         * <p> 此调用不会影响此编码实例的编码方案。应使用返回的编码实例进行非填充编码操作。
         *
         * @return  一个等效的编码器，不会在末尾添加任何填充字符。
         */
        public Encoder withoutPadding() {
            if (!doPadding)
                return this;
            return new Encoder(isURL, newline, linemax, false);
        }

        private int encode0(byte[] src, int off, int end, byte[] dst) {
            char[] base64 = isURL ? toBase64URL : toBase64;
            int sp = off;
            int slen = (end - off) / 3 * 3;
            int sl = off + slen;
            if (linemax > 0 && slen  > linemax / 4 * 3)
                slen = linemax / 4 * 3;
            int dp = 0;
            while (sp < sl) {
                int sl0 = Math.min(sp + slen, sl);
                for (int sp0 = sp, dp0 = dp ; sp0 < sl0; ) {
                    int bits = (src[sp0++] & 0xff) << 16 |
                               (src[sp0++] & 0xff) <<  8 |
                               (src[sp0++] & 0xff);
                    dst[dp0++] = (byte)base64[(bits >>> 18) & 0x3f];
                    dst[dp0++] = (byte)base64[(bits >>> 12) & 0x3f];
                    dst[dp0++] = (byte)base64[(bits >>> 6)  & 0x3f];
                    dst[dp0++] = (byte)base64[bits & 0x3f];
                }
                int dlen = (sl0 - sp) / 3 * 4;
                dp += dlen;
                sp = sl0;
                if (dlen == linemax && sp < end) {
                    for (byte b : newline){
                        dst[dp++] = b;
                    }
                }
            }
            if (sp < end) {               // 1 或 2 个剩余字节
                int b0 = src[sp++] & 0xff;
                dst[dp++] = (byte)base64[b0 >> 2];
                if (sp == end) {
                    dst[dp++] = (byte)base64[(b0 << 4) & 0x3f];
                    if (doPadding) {
                        dst[dp++] = '=';
                        dst[dp++] = '=';
                    }
                } else {
                    int b1 = src[sp++] & 0xff;
                    dst[dp++] = (byte)base64[(b0 << 4) & 0x3f | (b1 >> 4)];
                    dst[dp++] = (byte)base64[(b1 << 2) & 0x3f];
                    if (doPadding) {
                        dst[dp++] = '=';
                    }
                }
            }
            return dp;
        }
    }

    /**
     * 此类实现了根据 RFC 4648 和 RFC 2045 中指定的 Base64 编码方案解码字节数据的解码器。
     *
     * <p> Base64 填充字符 {@code '='} 被接受并解释为编码字节数据的结束，但不是必需的。因此，如果编码字节数据的最后一个单元只有两个或三个 Base64 字符（没有相应的填充字符），它们将被解码为好像后面跟着填充字符一样。如果最后一个单元中存在填充字符，则必须有正确数量的填充字符，否则在解码期间将抛出 {@code IllegalArgumentException}（从 Base64 流读取时为 {@code IOException}）。
     *
     * <p> {@link Decoder} 类的实例可以安全地被多个并发线程使用。
     *
     * <p> 除非另有说明，否则将 {@code null} 参数传递给此类的方法将导致抛出 {@link java.lang.NullPointerException NullPointerException}。
     *
     * @see     Encoder
     * @since   1.8
     */
    public static class Decoder {


                    private final boolean isURL;
        private final boolean isMIME;

        private Decoder(boolean isURL, boolean isMIME) {
            this.isURL = isURL;
            this.isMIME = isMIME;
        }

        /**
         * 解码从 "Base64 字母表"（如 RFC 2045 表 1 所指定）中抽取的 Unicode 字符到
         * 它们的 6 位正整数等效值的查找表。不在 Base64 字母表中的字符但落在数组范围内的
         * 字符被编码为 -1。
         *
         */
        private static final int[] fromBase64 = new int[256];
        static {
            Arrays.fill(fromBase64, -1);
            for (int i = 0; i < Encoder.toBase64.length; i++)
                fromBase64[Encoder.toBase64[i]] = i;
            fromBase64['='] = -2;
        }

        /**
         * 解码 "URL 和文件名安全的 Base64 字母表" 的查找表，如 RFC 4648 表 2 所指定。
         */
        private static final int[] fromBase64URL = new int[256];

        static {
            Arrays.fill(fromBase64URL, -1);
            for (int i = 0; i < Encoder.toBase64URL.length; i++)
                fromBase64URL[Encoder.toBase64URL[i]] = i;
            fromBase64URL['='] = -2;
        }

        static final Decoder RFC4648         = new Decoder(false, false);
        static final Decoder RFC4648_URLSAFE = new Decoder(true, false);
        static final Decoder RFC2045         = new Decoder(false, true);

        /**
         * 使用 {@link Base64} 编码方案解码输入字节数组中的所有字节，将结果写入新分配的输出字节数组。
         * 返回的字节数组的长度为解码后的字节数。
         *
         * @param   src
         *          要解码的字节数组
         *
         * @return  包含解码字节的新分配的字节数组。
         *
         * @throws  IllegalArgumentException
         *          如果 {@code src} 不是有效的 Base64 方案
         */
        public byte[] decode(byte[] src) {
            byte[] dst = new byte[outLength(src, 0, src.length)];
            int ret = decode0(src, 0, src.length, dst);
            if (ret != dst.length) {
                dst = Arrays.copyOf(dst, ret);
            }
            return dst;
        }

        /**
         * 使用 {@link Base64} 编码方案解码 Base64 编码的字符串，将结果写入新分配的字节数组。
         *
         * <p> 调用此方法的效果与调用
         * {@code decode(src.getBytes(StandardCharsets.ISO_8859_1))} 完全相同。
         *
         * @param   src
         *          要解码的字符串
         *
         * @return  包含解码字节的新分配的字节数组。
         *
         * @throws  IllegalArgumentException
         *          如果 {@code src} 不是有效的 Base64 方案
         */
        public byte[] decode(String src) {
            return decode(src.getBytes(StandardCharsets.ISO_8859_1));
        }

        /**
         * 使用 {@link Base64} 编码方案解码输入字节数组中的所有字节，将结果写入给定的输出字节数组，
         * 从偏移量 0 开始。
         *
         * <p> 调用此方法的责任在于确保输出字节数组 {@code dst} 有足够的空间来解码输入字节数组中的所有字节。
         * 如果输出字节数组空间不足，则不会将任何字节写入输出字节数组。
         *
         * <p> 如果输入字节数组不是有效的 Base64 编码方案，则在抛出 IllegalArgumentException 之前，
         * 可能会有一些字节写入输出字节数组。
         *
         * @param   src
         *          要解码的字节数组
         * @param   dst
         *          输出字节数组
         *
         * @return  写入输出字节数组的字节数
         *
         * @throws  IllegalArgumentException
         *          如果 {@code src} 不是有效的 Base64 方案，或 {@code dst} 没有足够的空间来解码所有输入字节。
         */
        public int decode(byte[] src, byte[] dst) {
            int len = outLength(src, 0, src.length);
            if (dst.length < len)
                throw new IllegalArgumentException(
                    "输出字节数组太小，无法解码所有输入字节");
            return decode0(src, 0, src.length, dst);
        }

        /**
         * 使用 {@link Base64} 编码方案解码输入字节缓冲区中的所有字节，将结果写入新分配的 ByteBuffer。
         *
         * <p> 返回时，源缓冲区的位置将更新为其限制；其限制不会改变。返回的输出缓冲区的位置将为零，其限制将是解码字节的数量。
         *
         * <p> 如果输入缓冲区不是有效的 Base64 编码方案，则抛出 IllegalArgumentException。在这种情况下，输入缓冲区的位置不会前进。
         *
         * @param   buffer
         *          要解码的 ByteBuffer
         *
         * @return  包含解码字节的新分配的字节缓冲区
         *
         * @throws  IllegalArgumentException
         *          如果 {@code src} 不是有效的 Base64 方案。
         */
        public ByteBuffer decode(ByteBuffer buffer) {
            int pos0 = buffer.position();
            try {
                byte[] src;
                int sp, sl;
                if (buffer.hasArray()) {
                    src = buffer.array();
                    sp = buffer.arrayOffset() + buffer.position();
                    sl = buffer.arrayOffset() + buffer.limit();
                    buffer.position(buffer.limit());
                } else {
                    src = new byte[buffer.remaining()];
                    buffer.get(src);
                    sp = 0;
                    sl = src.length;
                }
                byte[] dst = new byte[outLength(src, sp, sl)];
                return ByteBuffer.wrap(dst, 0, decode0(src, sp, sl, dst));
            } catch (IllegalArgumentException iae) {
                buffer.position(pos0);
                throw iae;
            }
        }


                    /**
         * 返回一个用于解码 {@link Base64} 编码的字节流的输入流。
         *
         * <p> 返回的 {@code InputStream} 的 {@code read} 方法在读取无法解码的字节时将抛出 {@code IOException}。
         *
         * <p> 关闭返回的输入流将关闭底层的输入流。
         *
         * @param   is
         *          输入流
         *
         * @return  用于解码指定 Base64 编码的字节流的输入流
         */
        public InputStream wrap(InputStream is) {
            Objects.requireNonNull(is);
            return new DecInputStream(is, isURL ? fromBase64URL : fromBase64, isMIME);
        }

        private int outLength(byte[] src, int sp, int sl) {
            int[] base64 = isURL ? fromBase64URL : fromBase64;
            int paddings = 0;
            int len = sl - sp;
            if (len == 0)
                return 0;
            if (len < 2) {
                if (isMIME && base64[0] == -1)
                    return 0;
                throw new IllegalArgumentException(
                    "输入字节数组至少应包含 2 个 Base64 字节");
            }
            if (isMIME) {
                // 扫描所有字节以填充所有非字母表。预扫描或 Arrays.copyOf 的性能权衡
                int n = 0;
                while (sp < sl) {
                    int b = src[sp++] & 0xff;
                    if (b == '=') {
                        len -= (sl - sp + 1);
                        break;
                    }
                    if ((b = base64[b]) == -1)
                        n++;
                }
                len -= n;
            } else {
                if (src[sl - 1] == '=') {
                    paddings++;
                    if (src[sl - 2] == '=')
                        paddings++;
                }
            }
            if (paddings == 0 && (len & 0x3) !=  0)
                paddings = 4 - (len & 0x3);
            return 3 * ((len + 3) / 4) - paddings;
        }

        private int decode0(byte[] src, int sp, int sl, byte[] dst) {
            int[] base64 = isURL ? fromBase64URL : fromBase64;
            int dp = 0;
            int bits = 0;
            int shiftto = 18;       // 4 字节原子的第一个字节的位置
            while (sp < sl) {
                int b = src[sp++] & 0xff;
                if ((b = base64[b]) < 0) {
                    if (b == -2) {         // 填充字节 '='
                        // =     shiftto==18 不必要的填充
                        // x=    shiftto==12 悬挂的单个 x
                        // x     与非填充情况一起处理
                        // xx=   shiftto==6&&sp==sl 缺少最后一个 =
                        // xx=y  shiftto==6 最后一个不是 =
                        if (shiftto == 6 && (sp == sl || src[sp++] != '=') ||
                            shiftto == 18) {
                            throw new IllegalArgumentException(
                                "输入字节数组有错误的 4 字节结尾单元");
                        }
                        break;
                    }
                    if (isMIME)    // 如果是 rfc2045，则跳过
                        continue;
                    else
                        throw new IllegalArgumentException(
                            "非法的 Base64 字符 " +
                            Integer.toString(src[sp - 1], 16));
                }
                bits |= (b << shiftto);
                shiftto -= 6;
                if (shiftto < 0) {
                    dst[dp++] = (byte)(bits >> 16);
                    dst[dp++] = (byte)(bits >>  8);
                    dst[dp++] = (byte)(bits);
                    shiftto = 18;
                    bits = 0;
                }
            }
            // 到达字节数组末尾或遇到填充 '=' 字符。
            if (shiftto == 6) {
                dst[dp++] = (byte)(bits >> 16);
            } else if (shiftto == 0) {
                dst[dp++] = (byte)(bits >> 16);
                dst[dp++] = (byte)(bits >>  8);
            } else if (shiftto == 12) {
                // 悬挂的单个 "x"，编码不正确。
                throw new IllegalArgumentException(
                    "最后一个单元没有足够的有效位");
            }
            // 如果不是 MIME，则任何剩余的都是无效的。
            // 如果是 MIME，则忽略所有非 Base64 字符
            while (sp < sl) {
                if (isMIME && base64[src[sp++]] < 0)
                    continue;
                throw new IllegalArgumentException(
                    "输入字节数组在 " + sp + " 位置有不正确的结尾字节");
            }
            return dp;
        }
    }

    /*
     * 用于将字节编码为 Base64 的输出流。
     */
    private static class EncOutputStream extends FilterOutputStream {

        private int leftover = 0;
        private int b0, b1, b2;
        private boolean closed = false;

        private final char[] base64;    // 字节到 Base64 的映射
        private final byte[] newline;   // 如果需要，行分隔符
        private final int linemax;
        private final boolean doPadding;// 是否填充
        private int linepos = 0;

        EncOutputStream(OutputStream os, char[] base64,
                        byte[] newline, int linemax, boolean doPadding) {
            super(os);
            this.base64 = base64;
            this.newline = newline;
            this.linemax = linemax;
            this.doPadding = doPadding;
        }

        @Override
        public void write(int b) throws IOException {
            byte[] buf = new byte[1];
            buf[0] = (byte)(b & 0xff);
            write(buf, 0, 1);
        }

        private void checkNewline() throws IOException {
            if (linepos == linemax) {
                out.write(newline);
                linepos = 0;
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (closed)
                throw new IOException("流已关闭");
            if (off < 0 || len < 0 || len > b.length - off)
                throw new ArrayIndexOutOfBoundsException();
            if (len == 0)
                return;
            if (leftover != 0) {
                if (leftover == 1) {
                    b1 = b[off++] & 0xff;
                    len--;
                    if (len == 0) {
                        leftover++;
                        return;
                    }
                }
                b2 = b[off++] & 0xff;
                len--;
                checkNewline();
                out.write(base64[b0 >> 2]);
                out.write(base64[(b0 << 4) & 0x3f | (b1 >> 4)]);
                out.write(base64[(b1 << 2) & 0x3f | (b2 >> 6)]);
                out.write(base64[b2 & 0x3f]);
                linepos += 4;
            }
            int nBits24 = len / 3;
            leftover = len - (nBits24 * 3);
            while (nBits24-- > 0) {
                checkNewline();
                int bits = (b[off++] & 0xff) << 16 |
                           (b[off++] & 0xff) <<  8 |
                           (b[off++] & 0xff);
                out.write(base64[(bits >>> 18) & 0x3f]);
                out.write(base64[(bits >>> 12) & 0x3f]);
                out.write(base64[(bits >>> 6)  & 0x3f]);
                out.write(base64[bits & 0x3f]);
                linepos += 4;
           }
            if (leftover == 1) {
                b0 = b[off++] & 0xff;
            } else if (leftover == 2) {
                b0 = b[off++] & 0xff;
                b1 = b[off++] & 0xff;
            }
        }


/**
 * 刷新流。
 */
public void flush() { }

/**
 * 关闭流。
 */
public void close() { }

}

                    @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                if (leftover == 1) {
                    checkNewline();
                    out.write(base64[b0 >> 2]);
                    out.write(base64[(b0 << 4) & 0x3f]);
                    if (doPadding) {
                        out.write('=');
                        out.write('=');
                    }
                } else if (leftover == 2) {
                    checkNewline();
                    out.write(base64[b0 >> 2]);
                    out.write(base64[(b0 << 4) & 0x3f | (b1 >> 4)]);
                    out.write(base64[(b1 << 2) & 0x3f]);
                    if (doPadding) {
                       out.write('=');
                    }
                }
                leftover = 0;
                out.close();
            }
        }
    }

    /*
     * 用于解码 Base64 字节的输入流
     */
    private static class DecInputStream extends InputStream {

        private final InputStream is;
        private final boolean isMIME;
        private final int[] base64;      // Base64 到字节的映射
        private int bits = 0;            // 用于解码的 24 位缓冲区
        private int nextin = 18;         // "bits" 中下一个可用的输入 "off"；
                                         // -> 18, 12, 6, 0
        private int nextout = -8;        // "bits" 中下一个可用的输出 "off"；
                                         // -> 8, 0, -8（无输出字节）
        private boolean eof = false;
        private boolean closed = false;

        DecInputStream(InputStream is, int[] base64, boolean isMIME) {
            this.is = is;
            this.base64 = base64;
            this.isMIME = isMIME;
        }

        private byte[] sbBuf = new byte[1];

        @Override
        public int read() throws IOException {
            return read(sbBuf, 0, 1) == -1 ? -1 : sbBuf[0] & 0xff;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (closed)
                throw new IOException("流已关闭");
            if (eof && nextout < 0)    // 已到达文件末尾且无剩余字节
                return -1;
            if (off < 0 || len < 0 || len > b.length - off)
                throw new IndexOutOfBoundsException();
            int oldOff = off;
            if (nextout >= 0) {       // "bits" 缓冲区中有剩余的输出字节
                do {
                    if (len == 0)
                        return off - oldOff;
                    b[off++] = (byte)(bits >> nextout);
                    len--;
                    nextout -= 8;
                } while (nextout >= 0);
                bits = 0;
            }
            while (len > 0) {
                int v = is.read();
                if (v == -1) {
                    eof = true;
                    if (nextin != 18) {
                        if (nextin == 12)
                            throw new IOException("Base64 流有一个未解码的悬挂字节。");
                        // 将结尾的 xx/xxx 没有填充字符视为合法。
                        // 与 v == '=' 时的逻辑相同
                        b[off++] = (byte)(bits >> (16));
                        len--;
                        if (nextin == 0) {           // 只有一个填充字节
                            if (len == 0) {          // 输出空间不足
                                bits >>= 8;          // 移动到最低字节
                                nextout = 0;
                            } else {
                                b[off++] = (byte) (bits >>  8);
                            }
                        }
                    }
                    if (off == oldOff)
                        return -1;
                    else
                        return off - oldOff;
                }
                if (v == '=') {                  // 填充字节
                    // =     shiftto==18 不必要的填充
                    // x=    shiftto==12 悬挂的 x，无效的单元
                    // xx=   shiftto==6 && 缺少最后一个 '='
                    // xx=y  或最后一个不是 '='
                    if (nextin == 18 || nextin == 12 ||
                        nextin == 6 && is.read() != '=') {
                        throw new IOException("非法的 Base64 结束序列:" + nextin);
                    }
                    b[off++] = (byte)(bits >> (16));
                    len--;
                    if (nextin == 0) {           // 只有一个填充字节
                        if (len == 0) {          // 输出空间不足
                            bits >>= 8;          // 移动到最低字节
                            nextout = 0;
                        } else {
                            b[off++] = (byte) (bits >>  8);
                        }
                    }
                    eof = true;
                    break;
                }
                if ((v = base64[v]) == -1) {
                    if (isMIME)                 // 如果是 rfc2045，则跳过
                        continue;
                    else
                        throw new IOException("非法的 Base64 字符 " +
                            Integer.toString(v, 16));
                }
                bits |= (v << nextin);
                if (nextin == 0) {
                    nextin = 18;    // 为下一个清空
                    nextout = 16;
                    while (nextout >= 0) {
                        b[off++] = (byte)(bits >> nextout);
                        len--;
                        nextout -= 8;
                        if (len == 0 && nextout >= 0) {  // 不清理 "bits"
                            return off - oldOff;
                        }
                    }
                    bits = 0;
                } else {
                    nextin -= 6;
                }
            }
            return off - oldOff;
        }

        @Override
        public int available() throws IOException {
            if (closed)
                throw new IOException("流已关闭");
            return is.available();   // 待定：
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                is.close();
            }
        }
    }
}
