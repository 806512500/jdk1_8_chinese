
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

package java.nio.charset;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.lang.ref.WeakReference;
import java.nio.charset.CoderMalfunctionError;                  // javadoc
import java.util.Arrays;


/**
 * 一个可以将一系列十六位 Unicode 字符转换为特定字符集的字节序列的引擎。
 *
 * <a name="steps"></a>
 *
 * <p> 输入字符序列通过字符缓冲区或一系列此类缓冲区提供。输出字节序列写入字节缓冲区或一系列此类缓冲区。编码器应始终通过以下方法调用序列使用，以下称为 <i>编码操作</i>：
 *
 * <ol>
 *
 *   <li><p> 通过 {@link #reset reset} 方法重置编码器，除非它之前未被使用； </p></li>
 *
 *   <li><p> 零次或多次调用 {@link #encode encode} 方法，只要可能有更多输入，传递 <tt>false</tt> 作为 <tt>endOfInput</tt> 参数，并在调用之间填充输入缓冲区和刷新输出缓冲区； </p></li>
 *
 *   <li><p> 最后一次调用 {@link #encode encode} 方法，传递 <tt>true</tt> 作为 <tt>endOfInput</tt> 参数；然后 </p></li>
 *
 *   <li><p> 调用 {@link #flush flush} 方法，以便编码器可以将内部状态刷新到输出缓冲区。 </p></li>
 *
 * </ol>
 *
 * 每次调用 {@link #encode encode} 方法将尽可能多地从输入缓冲区编码字符，将结果字节写入输出缓冲区。当需要更多输入时，当输出缓冲区空间不足时，或当发生编码错误时，{@link #encode encode} 方法返回。在每种情况下，返回一个 {@link CoderResult} 对象来描述终止的原因。调用者可以检查此对象并填充输入缓冲区，刷新输出缓冲区，或根据需要尝试从编码错误中恢复，然后重试。
 *
 * <a name="ce"></a>
 *
 * <p> 编码错误通常分为两种类型。如果输入字符序列不是合法的十六位 Unicode 序列，则输入被认为是 <i>格式错误的</i>。如果输入字符序列合法但无法映射到给定字符集的有效字节序列，则遇到了 <i>无法映射的字符</i>。
 *
 * <a name="cae"></a>
 *
 * <p> 如何处理编码错误取决于请求的该类型错误的操作，该操作由 {@link CodingErrorAction} 类的实例描述。可能的错误操作包括 {@linkplain CodingErrorAction#IGNORE 忽略} 错误输入，{@linkplain CodingErrorAction#REPORT 通过返回的 {@link CoderResult} 对象报告} 错误，或 {@linkplain CodingErrorAction#REPLACE 用当前的替换字节数组值替换} 错误输入。替换
 *

 * 初始设置为编码器的默认替换，通常（但不总是）初始值为&nbsp;<tt>{</tt>&nbsp;<tt>(byte)'?'</tt>&nbsp;<tt>}</tt>;




 *
 * 可以通过 {@link #replaceWith(byte[]) replaceWith} 方法更改其值。
 *
 * <p> 格式错误输入和无法映射字符错误的默认操作是 {@linkplain CodingErrorAction#REPORT 报告} 它们。可以通过 {@link #onMalformedInput(CodingErrorAction) onMalformedInput} 方法更改格式错误输入的错误操作；可以通过 {@link #onUnmappableCharacter(CodingErrorAction) onUnmappableCharacter} 方法更改无法映射字符的错误操作。
 *
 * <p> 本类旨在处理编码过程中的许多细节，包括错误操作的实现。特定字符集的编码器（本类的具体子类）只需要实现抽象的 {@link #encodeLoop encodeLoop} 方法，该方法封装了基本的编码循环。维护内部状态的子类还应覆盖 {@link #implFlush implFlush} 和 {@link #implReset implReset} 方法。
 *
 * <p> 本类的实例不适用于多个并发线程使用。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家组
 * @since 1.4
 *
 * @see ByteBuffer
 * @see CharBuffer
 * @see Charset
 * @see CharsetDecoder
 */

public abstract class CharsetEncoder {

    private final Charset charset;
    private final float averageBytesPerChar;
    private final float maxBytesPerChar;

    private byte[] replacement;
    private CodingErrorAction malformedInputAction
        = CodingErrorAction.REPORT;
    private CodingErrorAction unmappableCharacterAction
        = CodingErrorAction.REPORT;

    // 内部状态
    //
    private static final int ST_RESET   = 0;
    private static final int ST_CODING  = 1;
    private static final int ST_END     = 2;
    private static final int ST_FLUSHED = 3;

    private int state = ST_RESET;

    private static String stateNames[]
        = { "RESET", "CODING", "CODING_END", "FLUSHED" };


    /**
     * 初始化一个新的编码器。新的编码器将具有给定的每字符字节数和替换值。
     *
     * @param  cs
     *         创建此编码器的字符集
     *
     * @param  averageBytesPerChar
     *         表示每个输入字符预期生成的字节数的正浮点值
     *
     * @param  maxBytesPerChar
     *         表示每个输入字符可能生成的最大字节数的正浮点值
     *
     * @param  replacement
     *         初始替换；不得为 <tt>null</tt>，长度不得为零，不得超过 maxBytesPerChar，
     *         并且必须 {@linkplain #isLegalReplacement 合法}
     *
     * @throws  IllegalArgumentException
     *          如果参数的前置条件不成立
     */
    protected
    CharsetEncoder(Charset cs,
                   float averageBytesPerChar,
                   float maxBytesPerChar,
                   byte[] replacement)
    {
        this.charset = cs;
        if (averageBytesPerChar <= 0.0f)
            throw new IllegalArgumentException("非正数的 "
                                               + "averageBytesPerChar");
        if (maxBytesPerChar <= 0.0f)
            throw new IllegalArgumentException("非正数的 "
                                               + "maxBytesPerChar");
        if (!Charset.atBugLevel("1.4")) {
            if (averageBytesPerChar > maxBytesPerChar)
                throw new IllegalArgumentException("averageBytesPerChar"
                                                   + " 超过 "
                                                   + "maxBytesPerChar");
        }
        this.replacement = replacement;
        this.averageBytesPerChar = averageBytesPerChar;
        this.maxBytesPerChar = maxBytesPerChar;
        replaceWith(replacement);
    }

                /**
     * 初始化一个新的编码器。新的编码器将具有给定的每字符字节数值，其替换值为字节数组 <tt>{</tt>&nbsp;<tt>(byte)'?'</tt>&nbsp;<tt>}</tt>。
     *
     * @param  cs
     *         创建此编码器的字符集
     *
     * @param  averageBytesPerChar
     *         表示每个输入字符预期产生的字节数的正浮点值
     *
     * @param  maxBytesPerChar
     *         表示每个输入字符可能产生的最大字节数的正浮点值
     *
     * @throws  IllegalArgumentException
     *          如果参数的先决条件不成立
     */
    protected CharsetEncoder(Charset cs,
                             float averageBytesPerChar,
                             float maxBytesPerChar)
    {
        this(cs,
             averageBytesPerChar, maxBytesPerChar,
             new byte[] { (byte)'?' });
    }

    /**
     * 返回创建此编码器的字符集。
     *
     * @return  此编码器的字符集
     */
    public final Charset charset() {
        return charset;
    }

    /**
     * 返回此编码器的替换值。
     *
     * @return  此编码器的当前替换值，永远不会为 <tt>null</tt> 且永远不会为空
     */
    public final byte[] replacement() {




        return Arrays.copyOf(replacement, replacement.length);

    }

    /**
     * 更改此编码器的替换值。
     *
     * <p> 此方法调用 {@link #implReplaceWith implReplaceWith} 方法，传递新的替换值，在此之前会检查新的替换值是否可接受。 </p>
     *
     * @param  newReplacement  替换值
     *





     *         新的替换值；不得为 <tt>null</tt>，长度不得为零，长度不得超过 {@link #maxBytesPerChar() maxBytesPerChar} 方法返回的值，且必须是 {@link #isLegalReplacement 合法的}

     *
     * @return  此编码器
     *
     * @throws  IllegalArgumentException
     *          如果参数的先决条件不成立
     */
    public final CharsetEncoder replaceWith(byte[] newReplacement) {
        if (newReplacement == null)
            throw new IllegalArgumentException("Null replacement");
        int len = newReplacement.length;
        if (len == 0)
            throw new IllegalArgumentException("Empty replacement");
        if (len > maxBytesPerChar)
            throw new IllegalArgumentException("Replacement too long");




        if (!isLegalReplacement(newReplacement))
            throw new IllegalArgumentException("Illegal replacement");
        this.replacement = Arrays.copyOf(newReplacement, newReplacement.length);

        implReplaceWith(this.replacement);
        return this;
    }

    /**
     * 报告此编码器的替换值的更改。
     *
     * <p> 此方法的默认实现不执行任何操作。需要替换值更改通知的编码器应重写此方法。 </p>
     *
     * @param  newReplacement    替换值
     */
    protected void implReplaceWith(byte[] newReplacement) {
    }



    private WeakReference<CharsetDecoder> cachedDecoder = null;

    /**
     * 告知给定的字节数组是否为该编码器的合法替换值。
     *
     * <p> 替换值是合法的，当且仅当它是此编码器字符集中的合法字节序列；也就是说，必须能够将替换值解码为一个或多个十六位的Unicode字符。
     *
     * <p> 此方法的默认实现效率不高；通常应重写以提高性能。 </p>
     *
     * @param  repl  要测试的字节数组
     *
     * @return  <tt>true</tt> 如果且仅当给定的字节数组是此编码器的合法替换值
     */
    public boolean isLegalReplacement(byte[] repl) {
        WeakReference<CharsetDecoder> wr = cachedDecoder;
        CharsetDecoder dec = null;
        if ((wr == null) || ((dec = wr.get()) == null)) {
            dec = charset().newDecoder();
            dec.onMalformedInput(CodingErrorAction.REPORT);
            dec.onUnmappableCharacter(CodingErrorAction.REPORT);
            cachedDecoder = new WeakReference<CharsetDecoder>(dec);
        } else {
            dec.reset();
        }
        ByteBuffer bb = ByteBuffer.wrap(repl);
        CharBuffer cb = CharBuffer.allocate((int)(bb.remaining()
                                                  * dec.maxCharsPerByte()));
        CoderResult cr = dec.decode(bb, cb, true);
        return !cr.isError();
    }



    /**
     * 返回此编码器当前的非法输入错误操作。
     *
     * @return 当前的非法输入操作，永远不会为 <tt>null</tt>
     */
    public CodingErrorAction malformedInputAction() {
        return malformedInputAction;
    }

    /**
     * 更改此编码器的非法输入错误操作。
     *
     * <p> 此方法调用 {@link #implOnMalformedInput
     * implOnMalformedInput} 方法，传递新的操作。 </p>
     *
     * @param  newAction  新的操作；不得为 <tt>null</tt>
     *
     * @return  此编码器
     *
     * @throws IllegalArgumentException
     *         如果参数的先决条件不成立
     */
    public final CharsetEncoder onMalformedInput(CodingErrorAction newAction) {
        if (newAction == null)
            throw new IllegalArgumentException("Null action");
        malformedInputAction = newAction;
        implOnMalformedInput(newAction);
        return this;
    }

    /**
     * 报告此编码器的非法输入操作的更改。
     *
     * <p> 此方法的默认实现不执行任何操作。需要非法输入操作更改通知的编码器应重写此方法。 </p>
     *
     * @param  newAction  新的操作
     */
    protected void implOnMalformedInput(CodingErrorAction newAction) { }


/**
 * 返回此编码器当前对无法映射字符错误的处理动作。
 *
 * @return 当前的无法映射字符处理动作，永远不会为
 *         <tt>null</tt>
 */
public CodingErrorAction unmappableCharacterAction() {
    return unmappableCharacterAction;
}

/**
 * 更改此编码器对无法映射字符错误的处理动作。
 *
 * <p> 此方法调用 {@link #implOnUnmappableCharacter
 * implOnUnmappableCharacter} 方法，传递新的处理动作。 </p>
 *
 * @param  newAction  新的处理动作；必须不为 <tt>null</tt>
 *
 * @return  此编码器
 *
 * @throws IllegalArgumentException
 *         如果参数的前置条件不成立
 */
public final CharsetEncoder onUnmappableCharacter(CodingErrorAction
                                                  newAction)
{
    if (newAction == null)
        throw new IllegalArgumentException("Null action");
    unmappableCharacterAction = newAction;
    implOnUnmappableCharacter(newAction);
    return this;
}

/**
 * 报告此编码器的无法映射字符处理动作的更改。
 *
 * <p> 该方法的默认实现不执行任何操作。如果编码器需要通知无法映射字符处理动作的更改，则应覆盖此方法。 </p>
 *
 * @param  newAction  新的处理动作
 */
protected void implOnUnmappableCharacter(CodingErrorAction newAction) { }

/**
 * 返回每个输入字符将产生的平均字节数。此估算值可用于估计给定输入序列所需的输出缓冲区大小。
 *
 * @return  每个输入字符产生的平均字节数
 */
public final float averageBytesPerChar() {
    return averageBytesPerChar;
}

/**
 * 返回每个输入字符将产生的最大字节数。此值可用于计算给定输入序列所需的输出缓冲区的最大大小。
 *
 * @return  每个输入字符产生的最大字节数
 */
public final float maxBytesPerChar() {
    return maxBytesPerChar;
}

/**
 * 尽可能多地从给定的输入缓冲区中编码字符，并将结果写入给定的输出缓冲区。
 *
 * <p> 从输入缓冲区和输出缓冲区的当前位置开始读取和写入。最多读取 {@link Buffer#remaining in.remaining()} 个字符，
 * 并最多写入 {@link Buffer#remaining out.remaining()} 个字节。缓冲区的位置将根据读取的字符和写入的字节进行推进，
 * 但它们的标记和限制不会被修改。 </p>
 *
 * <p> 除了从输入缓冲区读取字符并写入输出缓冲区外，此方法还返回一个 {@link CoderResult} 对象来描述其终止原因：
 *
 * <ul>
 *
 *   <li><p> {@link CoderResult#UNDERFLOW} 表示尽可能多的输入缓冲区已被编码。如果没有进一步的输入，则调用者可以继续进行
 *   <a href="#steps">编码操作</a> 的下一步。否则应再次调用此方法并提供进一步的输入。 </p></li>
 *
 *   <li><p> {@link CoderResult#OVERFLOW} 表示输出缓冲区的空间不足以编码更多的字符。应再次调用此方法并提供一个具有更多
 *   {@linkplain Buffer#remaining 剩余} 字节的输出缓冲区。通常通过从输出缓冲区中排出已编码的字节来实现。 </p></li>
 *
 *   <li><p> 一个 {@linkplain CoderResult#malformedForLength
 *   无法解析的输入} 结果表示检测到了无法解析的输入错误。无法解析的字符从输入缓冲区的（可能已递增的）位置开始；
 *   无法解析的字符数量可以通过调用结果对象的 {@link
 *   CoderResult#length() length} 方法来确定。只有当此编码器的
 *   {@linkplain #onMalformedInput 无法解析的输入} 处理动作是 {@link CodingErrorAction#REPORT} 时，此情况才适用；
 *   否则将根据请求忽略或替换无法解析的输入。 </p></li>
 *
 *   <li><p> 一个 {@linkplain CoderResult#unmappableForLength
 *   无法映射的字符} 结果表示检测到了无法映射的字符错误。编码无法映射的字符从输入缓冲区的（可能已递增的）位置开始；
 *   这些字符的数量可以通过调用结果对象的 {@link CoderResult#length() length} 方法来确定。只有当此编码器的
 *   {@linkplain #onUnmappableCharacter 无法映射的字符} 处理动作是 {@link
 *   CodingErrorAction#REPORT} 时，此情况才适用；否则将根据请求忽略或替换无法映射的字符。 </p></li>
 *
 * </ul>
 *
 * 在任何情况下，如果此方法将在同一编码操作中再次调用，则应小心保留输入缓冲区中剩余的任何字符，以便它们可用于下一次调用。
 *
 * <p> <tt>endOfInput</tt> 参数告知此方法调用者是否可以提供给定输入缓冲区之外的进一步输入。如果有可能提供额外的输入，
 * 则调用者应为此参数传递 <tt>false</tt>；如果没有可能提供进一步的输入，则调用者应传递 <tt>true</tt>。
 * 传递 <tt>false</tt> 并在稍后发现实际上没有进一步的输入是不错误的，而且相当常见。然而，至关重要的是，在一系列调用中的
 * 最后一次调用此方法时，必须始终传递 <tt>true</tt>，以便将任何剩余的未编码输入视为无法解析。 </p>
 *
 * <p> 此方法通过调用 {@link #encodeLoop encodeLoop} 方法，解释其结果，处理错误条件，并在必要时重新调用该方法来工作。 </p>
 *
 *
 * @param  in
 *         输入字符缓冲区
 *
 * @param  out
 *         输出字节缓冲区
 *
 * @param  endOfInput
 *         <tt>true</tt> 表示调用者无法提供给定缓冲区之外的任何额外输入字符
 *
 * @return  一个编码器结果对象，描述终止原因
 *
 * @throws  IllegalStateException
 *          如果编码操作已经在进行中，并且前一步骤不是调用 {@link #reset reset} 方法，
 *          也不是调用此方法且 <tt>endOfInput</tt> 参数值为 <tt>false</tt>，
 *          也不是调用此方法且 <tt>endOfInput</tt> 参数值为 <tt>true</tt> 但返回值表示不完整的编码操作
 *
 * @throws  CoderMalfunctionError
 *          如果 encodeLoop 方法的调用抛出了意外的异常
 */
public final CoderResult encode(CharBuffer in, ByteBuffer out,
                                boolean endOfInput)
{
    int newState = endOfInput ? ST_END : ST_CODING;
    if ((state != ST_RESET) && (state != ST_CODING)
        && !(endOfInput && (state == ST_END)))
        throwIllegalStateException(state, newState);
    state = newState;


                    for (;;) {

            CoderResult cr;
            try {
                cr = encodeLoop(in, out);
            } catch (BufferUnderflowException x) {
                throw new CoderMalfunctionError(x);
            } catch (BufferOverflowException x) {
                throw new CoderMalfunctionError(x);
            }

            if (cr.isOverflow())
                return cr;

            if (cr.isUnderflow()) {
                if (endOfInput && in.hasRemaining()) {
                    cr = CoderResult.malformedForLength(in.remaining());
                    // Fall through to malformed-input case
                } else {
                    return cr;
                }
            }

            CodingErrorAction action = null;
            if (cr.isMalformed())
                action = malformedInputAction;
            else if (cr.isUnmappable())
                action = unmappableCharacterAction;
            else
                assert false : cr.toString();

            if (action == CodingErrorAction.REPORT)
                return cr;

            if (action == CodingErrorAction.REPLACE) {
                if (out.remaining() < replacement.length)
                    return CoderResult.OVERFLOW;
                out.put(replacement);
            }

            if ((action == CodingErrorAction.IGNORE)
                || (action == CodingErrorAction.REPLACE)) {
                // Skip erroneous input either way
                in.position(in.position() + cr.length());
                continue;
            }

            assert false;
        }

    }

    /**
     * 刷新此编码器。
     *
     * <p> 一些编码器维护内部状态，并可能需要在读取整个输入序列后将一些最终字节写入输出缓冲区。
     *
     * <p> 任何额外的输出都从输出缓冲区的当前位置开始写入。最多写入 {@link Buffer#remaining out.remaining()}
     * 个字节。缓冲区的位置将相应地前进，但其标记和限制不会被修改。
     *
     * <p> 如果此方法成功完成，则返回 {@link CoderResult#UNDERFLOW}。如果输出缓冲区空间不足，则返回
     * {@link CoderResult#OVERFLOW}。如果发生这种情况，则必须再次调用此方法，使用具有更多空间的输出缓冲区，以完成当前的
     * <a href="#steps">编码操作</a>。
     *
     * <p> 如果此编码器已经刷新，则调用此方法不会产生任何效果。
     *
     * <p> 此方法调用 {@link #implFlush implFlush} 方法来执行实际的刷新操作。 </p>
     *
     * @param  out
     *         输出字节缓冲区
     *
     * @return  编码结果对象，可以是 {@link CoderResult#UNDERFLOW} 或 {@link CoderResult#OVERFLOW}
     *
     * @throws  IllegalStateException
     *          如果当前编码操作的前一步既不是调用 {@link #flush flush} 方法，也不是调用带有 <tt>true</tt>
     *          值的 <tt>endOfInput</tt> 参数的三参数 {@link #encode(CharBuffer,ByteBuffer,boolean) encode} 方法
     */
    public final CoderResult flush(ByteBuffer out) {
        if (state == ST_END) {
            CoderResult cr = implFlush(out);
            if (cr.isUnderflow())
                state = ST_FLUSHED;
            return cr;
        }

        if (state != ST_FLUSHED)
            throwIllegalStateException(state, ST_FLUSHED);

        return CoderResult.UNDERFLOW; // 已经刷新
    }

    /**
     * 刷新此编码器。
     *
     * <p> 此方法的默认实现什么也不做，总是返回 {@link CoderResult#UNDERFLOW}。此方法应由可能需要在读取整个输入序列后
     * 将最终字节写入输出缓冲区的编码器覆盖。 </p>
     *
     * @param  out
     *         输出字节缓冲区
     *
     * @return  编码结果对象，可以是 {@link CoderResult#UNDERFLOW} 或 {@link CoderResult#OVERFLOW}
     */
    protected CoderResult implFlush(ByteBuffer out) {
        return CoderResult.UNDERFLOW;
    }

    /**
     * 重置此编码器，清除任何内部状态。
     *
     * <p> 此方法重置与字符集无关的状态，并调用 {@link #implReset() implReset} 方法以执行任何字符集特定的重置操作。 </p>
     *
     * @return  此编码器
     *
     */
    public final CharsetEncoder reset() {
        implReset();
        state = ST_RESET;
        return this;
    }

    /**
     * 重置此编码器，清除任何字符集特定的内部状态。
     *
     * <p> 此方法的默认实现什么也不做。此方法应由维护内部状态的编码器覆盖。 </p>
     */
    protected void implReset() { }

    /**
     * 将一个或多个字符编码为一个或多个字节。
     *
     * <p> 此方法封装了基本的编码循环，尽可能多地编码字符，直到它耗尽输入，输出缓冲区空间不足，或遇到编码错误。此方法由
     * {@link #encode encode} 方法调用，后者处理结果解释和错误恢复。
     *
     * <p> 从输入缓冲区的当前位置开始读取，从输出缓冲区的当前位置开始写入。最多读取 {@link Buffer#remaining in.remaining()}
     * 个字符，最多写入 {@link Buffer#remaining out.remaining()} 个字节。缓冲区的位置将相应地前进，以反映读取的字符和
     * 写入的字节，但其标记和限制不会被修改。
     *
     * <p> 此方法返回一个 {@link CoderResult} 对象，以描述其终止原因，方式与 {@link #encode encode} 方法相同。此方法的
     * 大多数实现将通过返回适当的结果对象来处理编码错误，以供 {@link #encode encode} 方法解释。优化的实现可能会检查相关的
     * 错误操作并自行实现该操作。
     *
     * <p> 此方法的实现可以通过返回 {@link CoderResult#UNDERFLOW} 直到接收到足够的输入来进行任意的向前查看。 </p>
     *
     * @param  in
     *         输入字符缓冲区
     *
     * @param  out
     *         输出字节缓冲区
     *
     * @return  描述终止原因的编码结果对象
     */
    protected abstract CoderResult encodeLoop(CharBuffer in,
                                              ByteBuffer out);

                /**
     * 便捷方法，将单个输入字符缓冲区的剩余内容编码到新分配的字节缓冲区中。
     *
     * <p>此方法实现了一个完整的<a href="#steps">编码操作</a>；即，它重置此编码器，然后编码给定字符缓冲区中的字符，最后刷新此编码器。因此，如果编码操作已经在进行中，则不应调用此方法。</p>
     *
     * @param  in
     *         输入字符缓冲区
     *
     * @return 包含编码操作结果的新分配的字节缓冲区。缓冲区的位置将为零，其限制将跟随最后一个写入的字节。
     *
     * @throws  IllegalStateException
     *          如果编码操作已经在进行中
     *
     * @throws  MalformedInputException
     *          如果从输入缓冲区当前位置开始的字符序列不是合法的十六位Unicode序列，并且当前的非法输入操作为 {@link CodingErrorAction#REPORT}
     *
     * @throws  UnmappableCharacterException
     *          如果从输入缓冲区当前位置开始的字符序列无法映射到等效的字节序列，并且当前的不可映射字符操作为 {@link
     *          CodingErrorAction#REPORT}
     */
    public final ByteBuffer encode(CharBuffer in)
        throws CharacterCodingException
    {
        int n = (int)(in.remaining() * averageBytesPerChar());
        ByteBuffer out = ByteBuffer.allocate(n);

        if ((n == 0) && (in.remaining() == 0))
            return out;
        reset();
        for (;;) {
            CoderResult cr = in.hasRemaining() ?
                encode(in, out, true) : CoderResult.UNDERFLOW;
            if (cr.isUnderflow())
                cr = flush(out);

            if (cr.isUnderflow())
                break;
            if (cr.isOverflow()) {
                n = 2*n + 1;    // 确保进度；n 可能为 0！
                ByteBuffer o = ByteBuffer.allocate(n);
                out.flip();
                o.put(out);
                out = o;
                continue;
            }
            cr.throwException();
        }
        out.flip();
        return out;
    }















































































    private boolean canEncode(CharBuffer cb) {
        if (state == ST_FLUSHED)
            reset();
        else if (state != ST_RESET)
            throwIllegalStateException(state, ST_CODING);
        CodingErrorAction ma = malformedInputAction();
        CodingErrorAction ua = unmappableCharacterAction();
        try {
            onMalformedInput(CodingErrorAction.REPORT);
            onUnmappableCharacter(CodingErrorAction.REPORT);
            encode(cb);
        } catch (CharacterCodingException x) {
            return false;
        } finally {
            onMalformedInput(ma);
            onUnmappableCharacter(ua);
            reset();
        }
        return true;
    }

    /**
     * 告知此编码器是否可以编码给定的字符。
     *
     * <p>如果给定的字符是代理字符，则此方法返回<tt>false</tt>；这样的字符只有当它们是高代理字符后跟低代理字符的对时才能被解释。可以使用 {@link #canEncode(java.lang.CharSequence)
     * canEncode(CharSequence)} 方法来测试字符序列是否可以被编码。
     *
     * <p>此方法可能会修改此编码器的状态；因此，如果<a href="#steps">编码操作</a>已经在进行中，则不应调用此方法。
     *
     * <p>此方法的默认实现效率不高；通常应覆盖此方法以提高性能。</p>
     *
     * @param   c
     *          给定的字符
     *
     * @return  <tt>true</tt>，当且仅当此编码器可以编码给定的字符
     *
     * @throws  IllegalStateException
     *          如果编码操作已经在进行中
     */
    public boolean canEncode(char c) {
        CharBuffer cb = CharBuffer.allocate(1);
        cb.put(c);
        cb.flip();
        return canEncode(cb);
    }

    /**
     * 告知此编码器是否可以编码给定的字符序列。
     *
     * <p>如果此方法对特定字符序列返回<tt>false</tt>，则可以通过执行完整的<a href="#steps">编码操作</a>来获取更多关于序列为何无法被编码的信息。
     *
     * <p>此方法可能会修改此编码器的状态；因此，如果编码操作已经在进行中，则不应调用此方法。
     *
     * <p>此方法的默认实现效率不高；通常应覆盖此方法以提高性能。</p>
     *
     * @param   cs
     *          给定的字符序列
     *
     * @return  <tt>true</tt>，当且仅当此编码器可以编码给定的字符而不抛出任何异常且不执行任何替换
     *
     * @throws  IllegalStateException
     *          如果编码操作已经在进行中
     */
    public boolean canEncode(CharSequence cs) {
        CharBuffer cb;
        if (cs instanceof CharBuffer)
            cb = ((CharBuffer)cs).duplicate();
        else
            cb = CharBuffer.wrap(cs.toString());
        return canEncode(cb);
    }




    private void throwIllegalStateException(int from, int to) {
        throw new IllegalStateException("当前状态 = " + stateNames[from]
                                        + ", 新状态 = " + stateNames[to]);
    }

}
