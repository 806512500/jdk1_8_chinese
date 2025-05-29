
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
 * 一个引擎，可以将特定字符集中的字节序列转换为十六位Unicode字符序列。
 *
 * <a name="steps"></a>
 *
 * <p> 输入字节序列通过字节缓冲区或一系列这样的缓冲区提供。输出字符序列写入字符缓冲区或一系列这样的缓冲区。解码器应始终通过以下方法调用序列进行使用，以下称为
 * <i>解码操作</i>：
 *
 * <ol>
 *
 *   <li><p> 通过 {@link #reset reset} 方法重置解码器，除非它之前未被使用； </p></li>
 *
 *   <li><p> 调用 {@link #decode decode} 方法零次或多次，只要可能有更多输入，传递 <tt>false</tt> 作为 <tt>endOfInput</tt> 参数，并在调用之间填充输入缓冲区和刷新输出缓冲区； </p></li>
 *
 *   <li><p> 最后一次调用 {@link #decode decode} 方法，传递 <tt>true</tt> 作为 <tt>endOfInput</tt> 参数；然后 </p></li>
 *
 *   <li><p> 调用 {@link #flush flush} 方法，以便解码器可以将内部状态刷新到输出缓冲区。 </p></li>
 *
 * </ol>
 *
 * 每次调用 {@link #decode decode} 方法时，都会尽可能多地从输入缓冲区解码字节，将结果字符写入输出缓冲区。当需要更多输入时，输出缓冲区空间不足时，或发生解码错误时，{@link #decode decode} 方法将返回。在每种情况下，都会返回一个 {@link CoderResult} 对象来描述终止原因。调用者可以检查此对象并填充输入缓冲区，刷新输出缓冲区，或尝试从解码错误中恢复，然后重试。
 *
 * <a name="ce"></a>
 *
 * <p> 解码错误通常有两种类型。如果输入字节序列对于此字符集不合法，则输入被认为是<i>格式错误的</i>。如果输入字节序列合法但无法映射到有效的
 * Unicode字符，则遇到了<i>无法映射的字符</i>。
 *
 * <a name="cae"></a>
 *
 * <p> 如何处理解码错误取决于为该类型错误请求的操作，这由 {@link
 * CodingErrorAction} 类的实例描述。可能的错误操作包括 {@linkplain
 * CodingErrorAction#IGNORE 忽略} 错误输入，{@linkplain
 * CodingErrorAction#REPORT 通过返回的 {@link CoderResult} 对象报告} 错误，或 {@linkplain CodingErrorAction#REPLACE
 * 用当前替换字符串的值替换} 错误输入。替换
 *



 * 初始值为 <tt>"&#92;uFFFD"</tt>;

 *
 * 可以通过 {@link #replaceWith(java.lang.String)
 * replaceWith} 方法更改其值。
 *
 * <p> 格式错误输入和无法映射字符的默认操作是 {@linkplain CodingErrorAction#REPORT 报告} 它们。可以通过 {@link
 * #onMalformedInput(CodingErrorAction) onMalformedInput} 方法更改格式错误输入的错误操作；可以通过 {@link
 * #onUnmappableCharacter(CodingErrorAction) onUnmappableCharacter} 方法更改无法映射字符的错误操作。
 *
 * <p> 本类旨在处理解码过程中的许多细节，包括错误操作的实现。特定字符集的解码器，即本类的具体子类，只需实现抽象的 {@link #decodeLoop decodeLoop} 方法，该方法封装了基本的解码循环。维护内部状态的子类还应覆盖 {@link #implFlush implFlush} 和
 * {@link #implReset implReset} 方法。
 *
 * <p> 本类的实例不适合多个并发线程使用。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 *
 * @see ByteBuffer
 * @see CharBuffer
 * @see Charset
 * @see CharsetEncoder
 */

public abstract class CharsetDecoder {

    private final Charset charset;
    private final float averageCharsPerByte;
    private final float maxCharsPerByte;

    private String replacement;
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
     * 初始化一个新的解码器。新的解码器将具有给定的
     * 每字节字符数和替换值。
     *
     * @param  cs
     *         创建此解码器的字符集
     *
     * @param  averageCharsPerByte
     *         表示每个输入字节预期产生的字符数的正浮点值
     *
     * @param  maxCharsPerByte
     *         表示每个输入字节可能产生的最大字符数的正浮点值
     *
     * @param  replacement
     *         初始替换；不得为 <tt>null</tt>，长度不得为零，不得超过 maxCharsPerByte，
     *         并且必须是 {@linkplain #isLegalReplacement 合法的}
     *
     * @throws  IllegalArgumentException
     *          如果参数的前置条件不成立
     */
    private
    CharsetDecoder(Charset cs,
                   float averageCharsPerByte,
                   float maxCharsPerByte,
                   String replacement)
    {
        this.charset = cs;
        if (averageCharsPerByte <= 0.0f)
            throw new IllegalArgumentException("Non-positive "
                                               + "averageCharsPerByte");
        if (maxCharsPerByte <= 0.0f)
            throw new IllegalArgumentException("Non-positive "
                                               + "maxCharsPerByte");
        if (!Charset.atBugLevel("1.4")) {
            if (averageCharsPerByte > maxCharsPerByte)
                throw new IllegalArgumentException("averageCharsPerByte"
                                                   + " exceeds "
                                                   + "maxCharsPerByte");
        }
        this.replacement = replacement;
        this.averageCharsPerByte = averageCharsPerByte;
        this.maxCharsPerByte = maxCharsPerByte;
        replaceWith(replacement);
    }


    /**
     * 初始化一个新的解码器。新的解码器将具有给定的每个字节的字符数值，其替换值为字符串 <tt>"&#92;uFFFD"</tt>。
     *
     * @param  cs
     *         创建此解码器的字符集
     *
     * @param  averageCharsPerByte
     *         表示每个输入字节预期产生的字符数的正浮点值
     *
     * @param  maxCharsPerByte
     *         表示每个输入字节产生的最大字符数的正浮点值
     *
     * @throws  IllegalArgumentException
     *          如果参数的先决条件不成立
     */
    protected CharsetDecoder(Charset cs,
                             float averageCharsPerByte,
                             float maxCharsPerByte)
    {
        this(cs,
             averageCharsPerByte, maxCharsPerByte,
             "\uFFFD");
    }

    /**
     * 返回创建此解码器的字符集。
     *
     * @return  此解码器的字符集
     */
    public final Charset charset() {
        return charset;
    }

    /**
     * 返回此解码器的替换值。
     *
     * @return  此解码器当前的替换值，永远不会为 <tt>null</tt> 且永远不会为空
     */
    public final String replacement() {

        return replacement;




    }

    /**
     * 更改此解码器的替换值。
     *
     * <p> 此方法在检查新替换值是否可接受后，调用 {@link #implReplaceWith implReplaceWith} 方法，传递新的替换值。 </p>
     *
     * @param  newReplacement  替换值
     *

     *         新的替换值；必须不为 <tt>null</tt>
     *         且长度不为零







     *
     * @return  此解码器
     *
     * @throws  IllegalArgumentException
     *          如果参数的先决条件不成立
     */
    public final CharsetDecoder replaceWith(String newReplacement) {
        if (newReplacement == null)
            throw new IllegalArgumentException("Null replacement");
        int len = newReplacement.length();
        if (len == 0)
            throw new IllegalArgumentException("Empty replacement");
        if (len > maxCharsPerByte)
            throw new IllegalArgumentException("Replacement too long");

        this.replacement = newReplacement;






        implReplaceWith(this.replacement);
        return this;
    }

    /**
     * 报告此解码器的替换值的更改。
     *
     * <p> 此方法的默认实现不做任何事情。需要替换值更改通知的解码器应覆盖此方法。 </p>
     *
     * @param  newReplacement    替换值
     */
    protected void implReplaceWith(String newReplacement) {
    }









































    /**
     * 返回此解码器当前的非法输入错误操作。
     *
     * @return 当前的非法输入错误操作，永远不会为 <tt>null</tt>
     */
    public CodingErrorAction malformedInputAction() {
        return malformedInputAction;
    }

    /**
     * 更改此解码器的非法输入错误操作。
     *
     * <p> 此方法调用 {@link #implOnMalformedInput implOnMalformedInput} 方法，传递新的操作。 </p>
     *
     * @param  newAction  新的操作；必须不为 <tt>null</tt>
     *
     * @return  此解码器
     *
     * @throws IllegalArgumentException
     *         如果参数的先决条件不成立
     */
    public final CharsetDecoder onMalformedInput(CodingErrorAction newAction) {
        if (newAction == null)
            throw new IllegalArgumentException("Null action");
        malformedInputAction = newAction;
        implOnMalformedInput(newAction);
        return this;
    }

    /**
     * 报告此解码器的非法输入错误操作的更改。
     *
     * <p> 此方法的默认实现不做任何事情。需要非法输入错误操作更改通知的解码器应覆盖此方法。 </p>
     *
     * @param  newAction  新的操作
     */
    protected void implOnMalformedInput(CodingErrorAction newAction) { }

    /**
     * 返回此解码器当前的不可映射字符错误操作。
     *
     * @return 当前的不可映射字符错误操作，永远不会为 <tt>null</tt>
     */
    public CodingErrorAction unmappableCharacterAction() {
        return unmappableCharacterAction;
    }

    /**
     * 更改此解码器的不可映射字符错误操作。
     *
     * <p> 此方法调用 {@link #implOnUnmappableCharacter implOnUnmappableCharacter} 方法，传递新的操作。 </p>
     *
     * @param  newAction  新的操作；必须不为 <tt>null</tt>
     *
     * @return  此解码器
     *
     * @throws IllegalArgumentException
     *         如果参数的先决条件不成立
     */
    public final CharsetDecoder onUnmappableCharacter(CodingErrorAction
                                                      newAction)
    {
        if (newAction == null)
            throw new IllegalArgumentException("Null action");
        unmappableCharacterAction = newAction;
        implOnUnmappableCharacter(newAction);
        return this;
    }

    /**
     * 报告此解码器的不可映射字符错误操作的更改。
     *
     * <p> 此方法的默认实现不做任何事情。需要不可映射字符错误操作更改通知的解码器应覆盖此方法。 </p>
     *
     * @param  newAction  新的操作
     */
    protected void implOnUnmappableCharacter(CodingErrorAction newAction) { }

    /**
     * 返回每个输入字节将产生的平均字符数。此启发式值可用于估计给定输入序列所需的输出缓冲区大小。
     *
     * @return  每个输入字节产生的平均字符数
     */
    public final float averageCharsPerByte() {
        return averageCharsPerByte;
    }


/**
 * 返回每个输入字节可能产生的最大字符数。此值可用于计算给定输入序列所需的输出缓冲区的最坏情况大小。
 *
 * @return 每个输入字节将产生的最大字符数
 */
public final float maxCharsPerByte() {
    return maxCharsPerByte;
}

/**
 * 从给定的输入缓冲区尽可能多地解码字节，并将结果写入给定的输出缓冲区。
 *
 * <p> 从缓冲区的当前位置开始读取和写入。最多读取 {@link Buffer#remaining in.remaining()} 个字节，
 * 并且最多写入 {@link Buffer#remaining out.remaining()} 个字符。缓冲区的位置将被推进以反映读取的字节和写入的字符，
 * 但它们的标记和限制不会被修改。
 *
 * <p> 除了从输入缓冲区读取字节并将字符写入输出缓冲区外，此方法还返回一个 {@link CoderResult} 对象来描述其终止原因：
 *
 * <ul>
 *
 *   <li><p> {@link CoderResult#UNDERFLOW} 表示尽可能多的输入缓冲区已被解码。如果没有进一步的输入，则调用者可以继续进行
 *   <a href="#steps">解码操作</a> 的下一步。否则，应使用更多输入再次调用此方法。 </p></li>
 *
 *   <li><p> {@link CoderResult#OVERFLOW} 表示输出缓冲区中没有足够的空间来解码更多的字节。
 *   应使用具有更多 {@linkplain Buffer#remaining 剩余} 字符的输出缓冲区再次调用此方法。通常通过从输出缓冲区中排出已解码的字符来实现。 </p></li>
 *
 *   <li><p> 一个 {@linkplain CoderResult#malformedForLength 错误输入} 结果表示检测到错误输入。
 *   错误字节从输入缓冲区的（可能已递增的）位置开始；错误字节的数量可以通过调用结果对象的 {@link
 *   CoderResult#length() length} 方法来确定。只有当此解码器的 {@linkplain #onMalformedInput 错误输入} 动作是
 *   {@link CodingErrorAction#REPORT} 时，此情况才适用；否则，错误输入将被忽略或替换，如请求的那样。 </p></li>
 *
 *   <li><p> 一个 {@linkplain CoderResult#unmappableForLength 无法映射的字符} 结果表示检测到无法映射的字符错误。
 *   解码无法映射的字符的字节从输入缓冲区的（可能已递增的）位置开始；这些字节的数量可以通过调用结果对象的 {@link
 *   CoderResult#length() length} 方法来确定。只有当此解码器的 {@linkplain #onUnmappableCharacter 无法映射的字符} 动作是
 *   {@link CodingErrorAction#REPORT} 时，此情况才适用；否则，无法映射的字符将被忽略或替换，如请求的那样。 </p></li>
 *
 * </ul>
 *
 * 在任何情况下，如果此方法在同一解码操作中重新调用，则应小心保留输入缓冲区中剩余的任何字节，以便它们可用于下次调用。
 *
 * <p> <tt>endOfInput</tt> 参数通知此方法调用者是否可以提供给定输入缓冲区之外的进一步输入。如果有可能提供额外的输入，
 * 则调用者应为此参数传递 <tt>false</tt>；如果没有可能提供进一步的输入，则调用者应传递 <tt>true</tt>。
 * 传递 <tt>false</tt> 并在稍后发现实际上没有进一步的输入是不错误的，而且很常见。然而，至关重要的是，在一系列调用中的最后一次调用
 * 必须始终传递 <tt>true</tt>，以便将任何剩余的未解码输入视为错误输入。
 *
 * <p> 此方法通过调用 {@link #decodeLoop decodeLoop} 方法，解释其结果，处理错误条件，并在必要时重新调用它来工作。 </p>
 *
 *
 * @param  in
 *         输入字节缓冲区
 *
 * @param  out
 *         输出字符缓冲区
 *
 * @param  endOfInput
 *         <tt>true</tt> 表示调用者不能提供给定缓冲区之外的任何额外输入字节
 *
 * @return 一个描述终止原因的解码器结果对象
 *
 * @throws  IllegalStateException
 *          如果解码操作已经在进行中，并且前一步既不是调用 {@link #reset reset} 方法，
 *          也不是调用此方法并为 <tt>endOfInput</tt> 参数传递 <tt>false</tt>，
 *          也不是调用此方法并为 <tt>endOfInput</tt> 参数传递 <tt>true</tt> 但返回值表示不完整的解码操作
 *
 * @throws  CoderMalfunctionError
 *          如果 decodeLoop 方法调用抛出了意外的异常
 */
public final CoderResult decode(ByteBuffer in, CharBuffer out,
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
            cr = decodeLoop(in, out);
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
                    // 跳转到不合法输入的情况
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
                if (out.remaining() < replacement.length())
                    return CoderResult.OVERFLOW;
                out.put(replacement);
            }

            if ((action == CodingErrorAction.IGNORE)
                || (action == CodingErrorAction.REPLACE)) {
                // 无论哪种情况，都跳过错误输入
                in.position(in.position() + cr.length());
                continue;
            }

            assert false;
        }

    }

    /**
     * 刷新此解码器。
     *
     * <p> 一些解码器维护内部状态，并可能在读取整个输入序列后需要将一些最终字符写入输出缓冲区。
     *
     * <p> 任何额外的输出将从输出缓冲区的当前位置开始写入。最多写入 {@link Buffer#remaining out.remaining()}
     * 个字符。缓冲区的位置将相应地前进，但其标记和限制不会被修改。
     *
     * <p> 如果此方法成功完成，则返回 {@link CoderResult#UNDERFLOW}。如果输出缓冲区空间不足，则返回
     * {@link CoderResult#OVERFLOW}。如果发生这种情况，则必须再次调用此方法，使用具有更多空间的输出缓冲区，以完成当前的
     * <a href="#steps">解码操作</a>。
     *
     * <p> 如果此解码器已经刷新，则调用此方法不会产生任何效果。
     *
     * <p> 此方法调用 {@link #implFlush implFlush} 方法来执行实际的刷新操作。 </p>
     *
     * @param  out
     *         输出字符缓冲区
     *
     * @return  一个解码结果对象，可以是 {@link CoderResult#UNDERFLOW} 或 {@link CoderResult#OVERFLOW}
     *
     * @throws  IllegalStateException
     *          如果当前解码操作的前一个步骤既不是调用 {@link #flush flush} 方法，也不是调用带有
     *          <tt>true</tt> 值的 <tt>endOfInput</tt> 参数的三参数 {@link
     *          #decode(ByteBuffer,CharBuffer,boolean) decode} 方法
     */
    public final CoderResult flush(CharBuffer out) {
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
     * 刷新此解码器。
     *
     * <p> 此方法的默认实现不执行任何操作，并始终返回 {@link CoderResult#UNDERFLOW}。此方法应由可能需要在读取整个输入序列后将最终字符写入输出缓冲区的解码器覆盖。 </p>
     *
     * @param  out
     *         输出字符缓冲区
     *
     * @return  一个解码结果对象，可以是 {@link CoderResult#UNDERFLOW} 或 {@link CoderResult#OVERFLOW}
     */
    protected CoderResult implFlush(CharBuffer out) {
        return CoderResult.UNDERFLOW;
    }

    /**
     * 重置此解码器，清除任何内部状态。
     *
     * <p> 此方法重置与字符集无关的状态，并调用 {@link #implReset() implReset} 方法以执行任何字符集特定的重置操作。 </p>
     *
     * @return  此解码器
     *
     */
    public final CharsetDecoder reset() {
        implReset();
        state = ST_RESET;
        return this;
    }

    /**
     * 重置此解码器，清除任何字符集特定的内部状态。
     *
     * <p> 此方法的默认实现不执行任何操作。此方法应由维护内部状态的解码器覆盖。 </p>
     */
    protected void implReset() { }

    /**
     * 解码一个或多个字节为一个或多个字符。
     *
     * <p> 此方法封装了基本的解码循环，尽可能多地解码字节，直到它耗尽输入、输出缓冲区空间不足或遇到解码错误。此方法由
     * {@link #decode decode} 方法调用，后者处理结果解释和错误恢复。
     *
     * <p> 从输入缓冲区的当前位置开始读取，从输出缓冲区的当前位置开始写入。最多读取 {@link Buffer#remaining in.remaining()}
     * 个字节，最多写入 {@link Buffer#remaining out.remaining()} 个字符。缓冲区的位置将相应地前进，以反映读取的字节数和写入的字符数，但其标记和限制不会被修改。
     *
     * <p> 此方法返回一个 {@link CoderResult} 对象来描述其终止原因，方式与 {@link #decode decode} 方法相同。大多数此方法的实现将通过返回适当的结果对象来处理解码错误，以便由
     * {@link #decode decode} 方法解释。优化的实现可能会检查相关的错误操作并自行实现该操作。
     *
     * <p> 此方法的实现可以通过返回 {@link CoderResult#UNDERFLOW} 直到接收到足够的输入来执行任意的向前查看。 </p>
     *
     * @param  in
     *         输入字节缓冲区
     *
     * @param  out
     *         输出字符缓冲区
     *
     * @return  描述终止原因的解码结果对象
     */
    protected abstract CoderResult decodeLoop(ByteBuffer in,
                                              CharBuffer out);


                /**
     * 便捷方法，将单个输入字节缓冲区的剩余内容解码到新分配的字符缓冲区中。
     *
     * <p> 此方法实现了一个完整的<a href="#steps">解码操作</a>；也就是说，它重置此解码器，然后解码给定字节缓冲区中的字节，最后刷新此解码器。因此，如果解码操作已经在进行中，不应调用此方法。 </p>
     *
     * @param  in
     *         输入字节缓冲区
     *
     * @return 包含解码操作结果的新分配的字符缓冲区。缓冲区的位置将为零，其限制将跟随最后一个写入的字符。
     *
     * @throws  IllegalStateException
     *          如果解码操作已经在进行中
     *
     * @throws  MalformedInputException
     *          如果从输入缓冲区当前位置开始的字节序列对于此字符集不合法，并且当前的非法输入操作是 {@link CodingErrorAction#REPORT}
     *
     * @throws  UnmappableCharacterException
     *          如果从输入缓冲区当前位置开始的字节序列无法映射到等效的字符序列，并且当前的无法映射字符操作是 {@link
     *          CodingErrorAction#REPORT}
     */
    public final CharBuffer decode(ByteBuffer in)
        throws CharacterCodingException
    {
        int n = (int)(in.remaining() * averageCharsPerByte());
        CharBuffer out = CharBuffer.allocate(n);

        if ((n == 0) && (in.remaining() == 0))
            return out;
        reset();
        for (;;) {
            CoderResult cr = in.hasRemaining() ?
                decode(in, out, true) : CoderResult.UNDERFLOW;
            if (cr.isUnderflow())
                cr = flush(out);

            if (cr.isUnderflow())
                break;
            if (cr.isOverflow()) {
                n = 2*n + 1;    // 确保进度；n 可能为 0！
                CharBuffer o = CharBuffer.allocate(n);
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



    /**
     * 告知此解码器是否实现了自动检测字符集。
     *
     * <p> 此方法的默认实现总是返回<tt>false</tt>；应由自动检测解码器覆盖以返回<tt>true</tt>。 </p>
     *
     * @return  如果且仅如果此解码器实现了自动检测字符集，则返回<tt>true</tt>
     */
    public boolean isAutoDetecting() {
        return false;
    }

    /**
     * 告知此解码器是否已经检测到字符集&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 如果此解码器实现了自动检测字符集，则在解码操作的某个点上，此方法可能开始返回<tt>true</tt>，以指示在输入字节序列中检测到了特定的字符集。一旦发生这种情况，可以调用 {@link #detectedCharset
     * detectedCharset} 方法来检索检测到的字符集。
     *
     * <p> 此方法返回<tt>false</tt>并不意味着尚未解码任何字节。一些自动检测解码器能够在不固定特定字符集的情况下解码输入字节序列的一部分，甚至全部。
     *
     * <p> 此方法的默认实现总是抛出一个 {@link
     * UnsupportedOperationException}；应由自动检测解码器覆盖以在确定输入字符集后返回<tt>true</tt>。 </p>
     *
     * @return  如果且仅如果此解码器已经检测到特定的字符集，则返回<tt>true</tt>
     *
     * @throws  UnsupportedOperationException
     *          如果此解码器未实现自动检测字符集
     */
    public boolean isCharsetDetected() {
        throw new UnsupportedOperationException();
    }

    /**
     * 检索此解码器检测到的字符集&nbsp;&nbsp;<i>(可选操作)</i>。
     *
     * <p> 如果此解码器实现了自动检测字符集，则此方法在检测到实际字符集后返回该字符集。从那一点开始，此方法在整个当前解码操作期间返回相同的值。如果尚未读取足够的输入字节以确定实际字符集，则此方法抛出一个 {@link
     * IllegalStateException}。
     *
     * <p> 此方法的默认实现总是抛出一个 {@link
     * UnsupportedOperationException}；应由自动检测解码器覆盖以返回适当的值。 </p>
     *
     * @return  此自动检测解码器检测到的字符集，
     *          或者如果尚未确定字符集，则返回<tt>null</tt>
     *
     * @throws  IllegalStateException
     *          如果读取的字节不足以确定字符集
     *
     * @throws  UnsupportedOperationException
     *          如果此解码器未实现自动检测字符集
     */
    public Charset detectedCharset() {
        throw new UnsupportedOperationException();
    }
































































































    private void throwIllegalStateException(int from, int to) {
        throw new IllegalStateException("Current state = " + stateNames[from]
                                        + ", new state = " + stateNames[to]);
    }

}
