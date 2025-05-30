
/*
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import sun.misc.MessageUtils;
import sun.nio.cs.HistoricallyNamedCharset;
import sun.nio.cs.ArrayDecoder;
import sun.nio.cs.ArrayEncoder;

/**
 * 字符串编码和解码的工具类。
 */

class StringCoding {

    private StringCoding() { }

    /** 每个线程的缓存编码器 */
    private final static ThreadLocal<SoftReference<StringDecoder>> decoder =
        new ThreadLocal<>();
    private final static ThreadLocal<SoftReference<StringEncoder>> encoder =
        new ThreadLocal<>();

    private static boolean warnUnsupportedCharset = true;

    private static <T> T deref(ThreadLocal<SoftReference<T>> tl) {
        SoftReference<T> sr = tl.get();
        if (sr == null)
            return null;
        return sr.get();
    }

    private static <T> void set(ThreadLocal<SoftReference<T>> tl, T ob) {
        tl.set(new SoftReference<T>(ob));
    }

    // 将给定的字节数组裁剪到指定的长度
    //
    private static byte[] safeTrim(byte[] ba, int len, Charset cs, boolean isTrusted) {
        if (len == ba.length && (isTrusted || System.getSecurityManager() == null))
            return ba;
        else
            return Arrays.copyOf(ba, len);
    }

    // 将给定的字符数组裁剪到指定的长度
    //
    private static char[] safeTrim(char[] ca, int len,
                                   Charset cs, boolean isTrusted) {
        if (len == ca.length && (isTrusted || System.getSecurityManager() == null))
            return ca;
        else
            return Arrays.copyOf(ca, len);
    }

    private static int scale(int len, float expansionFactor) {
        // 需要执行双精度浮点数运算，而不是单精度浮点数运算；否则当 len 大于 2**24 时会丢失低位。
        return (int)(len * (double)expansionFactor);
    }

    private static Charset lookupCharset(String csn) {
        if (Charset.isSupported(csn)) {
            try {
                return Charset.forName(csn);
            } catch (UnsupportedCharsetException x) {
                throw new Error(x);
            }
        }
        return null;
    }

    private static void warnUnsupportedCharset(String csn) {
        if (warnUnsupportedCharset) {
            // 使用 sun.misc.MessageUtils 而不是日志 API 或 System.err，因为此方法可能在 VM 初始化期间调用，此时日志 API 或 System.err 尚不可用。
            MessageUtils.err("WARNING: 默认字符集 " + csn +
                             " 不支持，使用 ISO-8859-1 代替");
            warnUnsupportedCharset = false;
        }
    }


    // -- 解码 --
    private static class StringDecoder {
        private final String requestedCharsetName;
        private final Charset cs;
        private final CharsetDecoder cd;
        private final boolean isTrusted;

        private StringDecoder(Charset cs, String rcn) {
            this.requestedCharsetName = rcn;
            this.cs = cs;
            this.cd = cs.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
            this.isTrusted = (cs.getClass().getClassLoader0() == null);
        }

        String charsetName() {
            if (cs instanceof HistoricallyNamedCharset)
                return ((HistoricallyNamedCharset)cs).historicalName();
            return cs.name();
        }

        final String requestedCharsetName() {
            return requestedCharsetName;
        }

        char[] decode(byte[] ba, int off, int len) {
            int en = scale(len, cd.maxCharsPerByte());
            char[] ca = new char[en];
            if (len == 0)
                return ca;
            if (cd instanceof ArrayDecoder) {
                int clen = ((ArrayDecoder)cd).decode(ba, off, len, ca);
                return safeTrim(ca, clen, cs, isTrusted);
            } else {
                cd.reset();
                ByteBuffer bb = ByteBuffer.wrap(ba, off, len);
                CharBuffer cb = CharBuffer.wrap(ca);
                try {
                    CoderResult cr = cd.decode(bb, cb, true);
                    if (!cr.isUnderflow())
                        cr.throwException();
                    cr = cd.flush(cb);
                    if (!cr.isUnderflow())
                        cr.throwException();
                } catch (CharacterCodingException x) {
                    // 替换总是启用的，
                    // 所以这不应该发生
                    throw new Error(x);
                }
                return safeTrim(ca, cb.position(), cs, isTrusted);
            }
        }
    }

    static char[] decode(String charsetName, byte[] ba, int off, int len)
        throws UnsupportedEncodingException
    {
        StringDecoder sd = deref(decoder);
        String csn = (charsetName == null) ? "ISO-8859-1" : charsetName;
        if ((sd == null) || !(csn.equals(sd.requestedCharsetName())
                              || csn.equals(sd.charsetName()))) {
            sd = null;
            try {
                Charset cs = lookupCharset(csn);
                if (cs != null)
                    sd = new StringDecoder(cs, csn);
            } catch (IllegalCharsetNameException x) {}
            if (sd == null)
                throw new UnsupportedEncodingException(csn);
            set(decoder, sd);
        }
        return sd.decode(ba, off, len);
    }

    static char[] decode(Charset cs, byte[] ba, int off, int len) {
        // (1)我们从不缓存“外部”cs，创建额外的 StringDe/Encoder 对象来包装它的唯一好处是共享 de/encode() 方法。这些 SD/E 对象是短命的，年轻代 GC 应该能够很好地处理它们。但最好的方法仍然是在确实必要时才生成它们。
        // (2)输入字节/字符数组的防御性拷贝对性能有很大影响，以及输出结果字节/字符数组。需要对 (sm==null && classLoader0==null) 进行优化检查。
        // (3)getClass().getClassLoader0() 是昂贵的
        // (4)isTrusted 设置可能存在时间差。当 (SM==null) 时，仅检查 getClassLoader0()（然后设置 isTrusted），但在调用 safeTrim() 时 SM 可能不为 null...“安全”的做法是在 trim 中冗余检查 (... && (isTrusted || SM == null || getClassLoader0()))，但可以争论的是，操作开始时 SM 为 null...
        CharsetDecoder cd = cs.newDecoder();
        int en = scale(len, cd.maxCharsPerByte());
        char[] ca = new char[en];
        if (len == 0)
            return ca;
        boolean isTrusted = false;
        if (System.getSecurityManager() != null) {
            if (!(isTrusted = (cs.getClass().getClassLoader0() == null))) {
                ba =  Arrays.copyOfRange(ba, off, off + len);
                off = 0;
            }
        }
        cd.onMalformedInput(CodingErrorAction.REPLACE)
          .onUnmappableCharacter(CodingErrorAction.REPLACE)
          .reset();
        if (cd instanceof ArrayDecoder) {
            int clen = ((ArrayDecoder)cd).decode(ba, off, len, ca);
            return safeTrim(ca, clen, cs, isTrusted);
        } else {
            ByteBuffer bb = ByteBuffer.wrap(ba, off, len);
            CharBuffer cb = CharBuffer.wrap(ca);
            try {
                CoderResult cr = cd.decode(bb, cb, true);
                if (!cr.isUnderflow())
                    cr.throwException();
                cr = cd.flush(cb);
                if (!cr.isUnderflow())
                    cr.throwException();
            } catch (CharacterCodingException x) {
                // 替换总是启用的，
                // 所以这不应该发生
                throw new Error(x);
            }
            return safeTrim(ca, cb.position(), cs, isTrusted);
        }
    }

    static char[] decode(byte[] ba, int off, int len) {
        String csn = Charset.defaultCharset().name();
        try {
            // 使用字符集名称解码变体，它提供缓存。
            return decode(csn, ba, off, len);
        } catch (UnsupportedEncodingException x) {
            warnUnsupportedCharset(csn);
        }
        try {
            return decode("ISO-8859-1", ba, off, len);
        } catch (UnsupportedEncodingException x) {
            // 如果在 VM 初始化期间命中此代码，MessageUtils 是我们唯一能够获取任何错误消息的方式。
            MessageUtils.err("ISO-8859-1 字符集不可用: "
                             + x.toString());
            // 如果找不到 ISO-8859-1（必需的编码），则安装存在问题。
            System.exit(1);
            return null;
        }
    }

    // -- 编码 --
    private static class StringEncoder {
        private Charset cs;
        private CharsetEncoder ce;
        private final String requestedCharsetName;
        private final boolean isTrusted;

        private StringEncoder(Charset cs, String rcn) {
            this.requestedCharsetName = rcn;
            this.cs = cs;
            this.ce = cs.newEncoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
            this.isTrusted = (cs.getClass().getClassLoader0() == null);
        }

        String charsetName() {
            if (cs instanceof HistoricallyNamedCharset)
                return ((HistoricallyNamedCharset)cs).historicalName();
            return cs.name();
        }

        final String requestedCharsetName() {
            return requestedCharsetName;
        }

        byte[] encode(char[] ca, int off, int len) {
            int en = scale(len, ce.maxBytesPerChar());
            byte[] ba = new byte[en];
            if (len == 0)
                return ba;
            if (ce instanceof ArrayEncoder) {
                int blen = ((ArrayEncoder)ce).encode(ca, off, len, ba);
                return safeTrim(ba, blen, cs, isTrusted);
            } else {
                ce.reset();
                ByteBuffer bb = ByteBuffer.wrap(ba);
                CharBuffer cb = CharBuffer.wrap(ca, off, len);
                try {
                    CoderResult cr = ce.encode(cb, bb, true);
                    if (!cr.isUnderflow())
                        cr.throwException();
                    cr = ce.flush(bb);
                    if (!cr.isUnderflow())
                        cr.throwException();
                } catch (CharacterCodingException x) {
                    // 替换总是启用的，
                    // 所以这不应该发生
                    throw new Error(x);
                }
                return safeTrim(ba, bb.position(), cs, isTrusted);
            }
        }
    }

    static byte[] encode(String charsetName, char[] ca, int off, int len)
        throws UnsupportedEncodingException
    {
        StringEncoder se = deref(encoder);
        String csn = (charsetName == null) ? "ISO-8859-1" : charsetName;
        if ((se == null) || !(csn.equals(se.requestedCharsetName())
                              || csn.equals(se.charsetName()))) {
            se = null;
            try {
                Charset cs = lookupCharset(csn);
                if (cs != null)
                    se = new StringEncoder(cs, csn);
            } catch (IllegalCharsetNameException x) {}
            if (se == null)
                throw new UnsupportedEncodingException (csn);
            set(encoder, se);
        }
        return se.encode(ca, off, len);
    }

    static byte[] encode(Charset cs, char[] ca, int off, int len) {
        CharsetEncoder ce = cs.newEncoder();
        int en = scale(len, ce.maxBytesPerChar());
        byte[] ba = new byte[en];
        if (len == 0)
            return ba;
        boolean isTrusted = false;
        if (System.getSecurityManager() != null) {
            if (!(isTrusted = (cs.getClass().getClassLoader0() == null))) {
                ca =  Arrays.copyOfRange(ca, off, off + len);
                off = 0;
            }
        }
        ce.onMalformedInput(CodingErrorAction.REPLACE)
          .onUnmappableCharacter(CodingErrorAction.REPLACE)
          .reset();
        if (ce instanceof ArrayEncoder) {
            int blen = ((ArrayEncoder)ce).encode(ca, off, len, ba);
            return safeTrim(ba, blen, cs, isTrusted);
        } else {
            ByteBuffer bb = ByteBuffer.wrap(ba);
            CharBuffer cb = CharBuffer.wrap(ca, off, len);
            try {
                CoderResult cr = ce.encode(cb, bb, true);
                if (!cr.isUnderflow())
                    cr.throwException();
                cr = ce.flush(bb);
                if (!cr.isUnderflow())
                    cr.throwException();
            } catch (CharacterCodingException x) {
                throw new Error(x);
            }
            return safeTrim(ba, bb.position(), cs, isTrusted);
        }
    }

    static byte[] encode(char[] ca, int off, int len) {
        String csn = Charset.defaultCharset().name();
        try {
            // 使用字符集名称编码变体，它提供缓存。
            return encode(csn, ca, off, len);
        } catch (UnsupportedEncodingException x) {
            warnUnsupportedCharset(csn);
        }
        try {
            return encode("ISO-8859-1", ca, off, len);
        } catch (UnsupportedEncodingException x) {
            // 如果在 VM 初始化期间命中此代码，MessageUtils 是我们唯一能够获取任何错误消息的方式。
            MessageUtils.err("ISO-8859-1 字符集不可用: "
                             + x.toString());
            // 如果找不到 ISO-8859-1（必需的编码），则安装存在问题。
            System.exit(1);
            return null;
        }
    }
}
