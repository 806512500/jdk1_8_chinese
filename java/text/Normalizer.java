
/*
 * 版权所有 (c) 2005, 2013, Oracle 和/或其附属公司。保留所有权利。
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

/*
 *******************************************************************************
 * 版权所有 (C) IBM Corp. 1996-2005 - 保留所有权利                                 *
 *                                                                             *
 * 本源代码和文档的原始版本受版权保护并归 IBM 所有，这些材料根据 IBM 和 Sun 之间的许可协议提供。 *
 * 该技术受多项美国和国际专利保护。此通知和对 IBM 的归属不得移除。                                      *
 *******************************************************************************
 */

package java.text;

import sun.text.normalizer.NormalizerBase;
import sun.text.normalizer.NormalizerImpl;

/**
 * 该类提供了 <code>normalize</code> 方法，该方法将 Unicode 文本转换为等效的组合或分解形式，以便于文本的排序和搜索。
 * <code>normalize</code> 方法支持在
 * <a href="http://www.unicode.org/unicode/reports/tr15/tr15-23.html">
 * Unicode 标准附录 #15 &mdash; Unicode 归一化形式</a> 中描述的标准归一化形式。
 * <p>
 * 带有重音或其他装饰的字符在 Unicode 中可以有多种不同的编码方式。例如，以带重音的 A 字符为例。在 Unicode 中，这可以编码为一个字符（“组合”形式）：
 *
 * <pre>
 *      U+00C1    LATIN CAPITAL LETTER A WITH ACUTE</pre>
 *
 * 或者编码为两个单独的字符（“分解”形式）：
 *
 * <pre>
 *      U+0041    LATIN CAPITAL LETTER A
 *      U+0301    COMBINING ACUTE ACCENT</pre>
 *
 * 但是，对于您的程序的用户来说，这两个序列应该被视为相同的“用户级别”字符“A 带重音”。在搜索或比较文本时，必须确保这两个序列被视为等效的。此外，必须处理带有多个重音的字符。有时字符的组合重音的顺序是重要的，而在其他情况下，不同顺序的重音序列实际上是等效的。
 * <p>
 * 同样，字符串 "ffi" 可以编码为三个单独的字母：
 *
 * <pre>
 *      U+0066    LATIN SMALL LETTER F
 *      U+0066    LATIN SMALL LETTER F
 *      U+0069    LATIN SMALL LETTER I</pre>
 *
 * 或者编码为单个字符：
 *
 * <pre>
 *      U+FB03    LATIN SMALL LIGATURE FFI</pre>
 *
 * "ffi" 连字不是一个独立的语义字符，严格来说，它根本不应该在 Unicode 中，但为了与已经提供它的现有字符集兼容而被包括进来。Unicode 标准通过为这些字符提供“兼容性”分解到相应的语义字符来识别这些字符。在排序和搜索时，您通常希望使用这些映射。
 * <p>
 * <code>normalize</code> 方法通过将文本转换为如上第一个示例所示的规范组合和分解形式来帮助解决这些问题。此外，您可以要求它执行兼容性分解，以便可以将兼容性字符与其等效字符视为相同。最后，<code>normalize</code> 方法会重新排列重音到正确的规范顺序，因此您不必自己担心重音的重新排列。
 * <p>
 * W3C 通常建议交换 NFC 格式的文本。
 * 还需注意，大多数旧字符编码仅使用预组合形式，并且通常不单独编码任何组合标记。对于转换到此类字符编码，Unicode 文本需要规范化为 NFC。
 * 更多使用示例，请参见 Unicode 标准附录。
 *
 * @since 1.6
 */
public final class Normalizer {

   private Normalizer() {};

    /**
     * 该枚举提供了在
     * <a href="http://www.unicode.org/unicode/reports/tr15/tr15-23.html">
     * Unicode 标准附录 #15 &mdash; Unicode 归一化形式</a> 中描述的四种 Unicode 归一化形式的常量
     * 和两种访问它们的方法。
     *
     * @since 1.6
     */
    public static enum Form {

        /**
         * 规范分解。
         */
        NFD,

        /**
         * 规范分解，后跟规范组合。
         */
        NFC,

        /**
         * 兼容性分解。
         */
        NFKD,

        /**
         * 兼容性分解，后跟规范组合。
         */
        NFKC
    }

    /**
     * 规范化一系列 char 值。
     * 该序列将根据指定的归一化形式进行规范化。
     * @param src        要规范化的 char 值序列。
     * @param form       归一化形式；可以是
     *                   {@link java.text.Normalizer.Form#NFC}，
     *                   {@link java.text.Normalizer.Form#NFD}，
     *                   {@link java.text.Normalizer.Form#NFKC}，
     *                   {@link java.text.Normalizer.Form#NFKD} 之一。
     * @return 规范化后的字符串
     * @throws NullPointerException 如果 <code>src</code> 或 <code>form</code>
     * 是 null。
     */
    public static String normalize(CharSequence src, Form form) {
        return NormalizerBase.normalize(src.toString(), form);
    }

    /**
     * 确定给定的 char 值序列是否已规范化。
     * @param src        要检查的 char 值序列。
     * @param form       归一化形式；可以是
     *                   {@link java.text.Normalizer.Form#NFC}，
     *                   {@link java.text.Normalizer.Form#NFD}，
     *                   {@link java.text.Normalizer.Form#NFKC}，
     *                   {@link java.text.Normalizer.Form#NFKD} 之一。
     * @return 如果 char 值序列已规范化，则返回 true；否则返回 false。
     * @throws NullPointerException 如果 <code>src</code> 或 <code>form</code>
     * 是 null。
     */
    public static boolean isNormalized(CharSequence src, Form form) {
        return NormalizerBase.isNormalized(src.toString(), form);
    }
}
