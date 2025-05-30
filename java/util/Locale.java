
/*
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
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

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.security.AccessController;
import java.text.MessageFormat;
import java.util.spi.LocaleNameProvider;

import sun.security.action.GetPropertyAction;
import sun.util.locale.BaseLocale;
import sun.util.locale.InternalLocaleBuilder;
import sun.util.locale.LanguageTag;
import sun.util.locale.LocaleExtensions;
import sun.util.locale.LocaleMatcher;
import sun.util.locale.LocaleObjectCache;
import sun.util.locale.LocaleSyntaxException;
import sun.util.locale.LocaleUtils;
import sun.util.locale.ParseStatus;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;
import sun.util.locale.provider.LocaleServiceProviderPool;
import sun.util.locale.provider.ResourceBundleBasedAdapter;

/**
 * <code>Locale</code> 对象表示一个特定的地理、政治或文化区域。需要 <code>Locale</code> 来执行任务的操作称为 <em>区域敏感</em> 操作，它使用 <code>Locale</code> 来为用户定制信息。例如，显示数字是一个区域敏感操作——数字应根据用户的本国、地区或文化的习惯和惯例进行格式化。
 *
 * <p> {@code Locale} 类实现了 IETF BCP 47，由 <a href="http://tools.ietf.org/html/rfc4647">RFC 4647 "语言标签匹配"</a> 和 <a href="http://tools.ietf.org/html/rfc5646">RFC 5646 "标识语言的标签"</a> 组成，并支持用于本地数据交换的 LDML（UTS#35，“Unicode 本地数据标记语言”）BCP 47 兼容扩展。
 *
 * <p> 一个 <code>Locale</code> 对象逻辑上由以下字段组成。
 *
 * <dl>
 *   <dt><a name="def_language"><b>语言</b></a></dt>
 *
 *   <dd>ISO 639 alpha-2 或 alpha-3 语言代码，或注册的最多 8 个字母的语言子标签（用于未来的增强）。当一种语言同时有 alpha-2 代码和 alpha-3 代码时，必须使用 alpha-2 代码。您可以在 IANA 语言子标签注册表中找到所有有效的语言代码（搜索 "Type: language"）。语言字段不区分大小写，但 <code>Locale</code> 始终规范为小写。</dd>
 *
 *   <dd>格式良好的语言值形式为 <code>[a-zA-Z]{2,8}</code>。注意，这不是完整的 BCP47 语言生成，因为它排除了 extlang。它们不需要，因为现代的三字母语言代码取代了它们。</dd>
 *
 *   <dd>示例："en"（英语），"ja"（日语），"kok"（康卡尼语）</dd>
 *
 *   <dt><a name="def_script"><b>脚本</b></a></dt>
 *
 *   <dd>ISO 15924 alpha-4 脚本代码。您可以在 IANA 语言子标签注册表中找到所有有效的脚本代码（搜索 "Type: script"）。脚本字段不区分大小写，但 <code>Locale</code> 始终规范为首字母大写（其余字母小写）。</dd>
 *
 *   <dd>格式良好的脚本值形式为 <code>[a-zA-Z]{4}</code></dd>
 *
 *   <dd>示例："Latn"（拉丁文），"Cyrl"（西里尔文）</dd>
 *
 *   <dt><a name="def_region"><b>国家（地区）</b></a></dt>
 *
 *   <dd>ISO 3166 alpha-2 国家代码或 UN M.49 数字-3 区域代码。您可以在 IANA 语言子标签注册表中找到所有有效的国家和区域代码（搜索 "Type: region"）。国家（地区）字段不区分大小写，但 <code>Locale</code> 始终规范为大写。</dd>
 *
 *   <dd>格式良好的国家/地区值形式为 <code>[a-zA-Z]{2} | [0-9]{3}</code></dd>
 *
 *   <dd>示例："US"（美国），"FR"（法国），"029"（加勒比海）</dd>
 *
 *   <dt><a name="def_variant"><b>变体</b></a></dt>
 *
 *   <dd>用于指示 <code>Locale</code> 变体的任意值。当有两个或多个变体值各自表示其自身的语义时，这些值应按重要性排序，最重要的在前，用下划线（'_'）分隔。变体字段区分大小写。</dd>
 *
 *   <dd>注意：IETF BCP 47 对变体子标签施加了语法限制。此外，BCP 47 子标签严格用于指示任何组合的语言、脚本和区域子标签未涵盖的语言或其方言的额外变体。您可以在 IANA 语言子标签注册表中找到所有有效的变体代码（搜索 "Type: variant"）。
 *
 *   <p>然而，<code>Locale</code> 中的变体字段历史上一直用于任何种类的变体，而不仅仅是语言变体。例如，Java SE 运行时环境中支持的一些变体表示替代的文化行为，如日历类型或数字脚本。在 BCP 47 中，这种不标识语言的信息由扩展子标签或私有用途子标签支持。</dd>
 *
 *   <dd>格式良好的变体值形式为 <code>SUBTAG (('_'|'-') SUBTAG)*</code>，其中 <code>SUBTAG = [0-9][0-9a-zA-Z]{3} | [0-9a-zA-Z]{5,8}</code>。（注意：BCP 47 仅使用连字符（'-'）作为分隔符，这里更宽松）。</dd>
 *
 *   <dd>示例："polyton"（多音希腊语），"POSIX"</dd>
 *
 *   <dt><a name="def_extensions"><b>扩展</b></a></dt>
 *
 *   <dd>从单个字符键到字符串值的映射，表示除语言识别之外的扩展。<code>Locale</code> 中的扩展实现了 BCP 47 扩展子标签和私有用途子标签的语义和语法。扩展不区分大小写，但 <code>Locale</code> 始终将所有扩展键和值规范为小写。注意，扩展不能有空值。</dd>
 *
 *   <dd>格式良好的键是单个字符，来自集合 <code>[0-9a-zA-Z]</code>。格式良好的值形式为 <code>SUBTAG ('-' SUBTAG)*</code>，对于键 'x' <code>SUBTAG = [0-9a-zA-Z]{1,8}</code>，对于其他键 <code>SUBTAG = [0-9a-zA-Z]{2,8}</code>（即，'x' 允许单字符子标签）。</dd>
 *
 *   <dd>示例：键="u"/值="ca-japanese"（日本日历），键="x"/值="java-1-7"</dd>
 * </dl>
 *
 * <b>注意：</b>虽然 BCP 47 要求字段值在 IANA 语言子标签注册表中注册，但 <code>Locale</code> 类不提供任何验证功能。<code>Builder</code> 仅检查单个字段是否满足语法要求（是否格式良好），但不验证值本身。详情请参见 {@link Builder}。
 *
 * <h3><a name="def_locale_extension">Unicode 本地/语言扩展</a></h3>
 *
 * <p>UTS#35，“Unicode 本地数据标记语言”定义了可选的属性和关键字，以覆盖或细化与本地关联的默认行为。关键字由键和类型对表示。例如，“nu-thai”表示应使用泰国本地数字（值：“thai”）来格式化数字（键：“nu”）。
 *
 * <p>关键字映射到使用扩展键 'u'（{@link #UNICODE_LOCALE_EXTENSION}）的 BCP 47 扩展值。上述示例，“nu-thai”，变为扩展“u-nu-thai”。
 *
 * <p>因此，当 <code>Locale</code> 对象包含 Unicode 本地属性和关键字时，<code>getExtension(UNICODE_LOCALE_EXTENSION)</code> 将返回表示此信息的字符串，例如，“nu-thai”。<code>Locale</code> 类还提供了 {@link #getUnicodeLocaleAttributes}、{@link #getUnicodeLocaleKeys} 和 {@link #getUnicodeLocaleType}，允许您直接访问 Unicode 本地属性和键/类型对。当表示为字符串时，Unicode 本地扩展列出属性，按字母顺序排列，后跟键/类型序列，键按字母顺序排列（组成键的类型子标签的顺序在定义类型时是固定的）。
 *
 * <p>格式良好的本地键形式为 <code>[0-9a-zA-Z]{2}</code>。格式良好的本地类型形式为 <code>"" | [0-9a-zA-Z]{3,8} ('-' [0-9a-zA-Z]{3,8})*</code>（它可以为空，或一系列 3-8 个字母数字的子标签）。格式良好的本地属性形式为 <code>[0-9a-zA-Z]{3,8}</code>（它是一个单个子标签，形式与本地类型子标签相同）。
 *
 * <p>Unicode 本地扩展指定了本地敏感服务中的可选行为。尽管 LDML 规范定义了各种键和值，但 Java 运行时环境中的实际本地敏感服务实现可能不支持任何特定的 Unicode 本地属性或键/类型对。
 *
 * <h4>创建 Locale</h4>
 *
 * <p>有几种不同的方法可以创建 <code>Locale</code> 对象。
 *
 * <h5>构建器</h5>
 *
 * <p>使用 {@link Builder} 您可以构建一个符合 BCP 47 语法的 <code>Locale</code> 对象。
 *
 * <h5>构造函数</h5>
 *
 * <p><code>Locale</code> 类提供了三个构造函数：
 * <blockquote>
 * <pre>
 *     {@link #Locale(String language)}
 *     {@link #Locale(String language, String country)}
 *     {@link #Locale(String language, String country, String variant)}
 * </pre>
 * </blockquote>
 * 这些构造函数允许您创建一个包含语言、国家和变体的 <code>Locale</code> 对象，但您不能指定脚本或扩展。
 *
 * <h5>工厂方法</h5>
 *
 * <p>方法 {@link #forLanguageTag} 为格式良好的 BCP 47 语言标签创建一个 <code>Locale</code> 对象。
 *
 * <h5>Locale 常量</h5>
 *
 * <p><code>Locale</code> 类提供了许多方便的常量，您可以使用它们为常用的本地创建 <code>Locale</code> 对象。例如，以下代码创建一个表示美国的 <code>Locale</code> 对象：
 * <blockquote>
 * <pre>
 *     Locale.US
 * </pre>
 * </blockquote>
 *
 * <h4><a name="LocaleMatching">本地匹配</a></h4>
 *
 * <p>如果一个应用程序或系统已国际化并提供了多个本地的本地化资源，有时需要找到一个或多个满足每个用户特定偏好的本地（或语言标签）。注意，在此本地匹配文档中，“语言标签”与“本地”互换使用。
 *
 * <p>为了将用户的首选本地与一组语言标签匹配，<a href="http://tools.ietf.org/html/rfc4647">RFC 4647 语言标签匹配</a> 定义了两种机制：过滤和查找。<em>过滤</em> 用于获取所有匹配的本地，而 <em>查找</em> 用于选择最佳匹配的本地。匹配不区分大小写。这些匹配机制在以下部分中描述。
 *
 * <p>用户的偏好称为 <em>语言优先级列表</em>，表示为一系列语言范围。语言范围在语法上有两种类型：基本和扩展。详情请参见 {@link Locale.LanguageRange Locale.LanguageRange}。
 *
 * <h5>过滤</h5>
 *
 * <p>过滤操作返回所有匹配的语言标签。它在 RFC 4647 中定义如下：
 * “在过滤中，每个语言范围表示最不具体的语言标签（即，具有最少子标签数的语言标签），它是可接受的匹配。所有匹配的语言标签集中的语言标签将具有与语言范围相同或更多的子标签。每个语言范围中的每个非通配符子标签将出现在每个匹配的语言标签中。”
 *
 * <p>过滤有两种类型：基本语言范围的过滤（称为“基本过滤”）和扩展语言范围的过滤（称为“扩展过滤”）。它们可能因包含在给定语言优先级列表中的语言范围类型不同而返回不同的结果。{@link Locale.FilteringMode} 是一个参数，用于指定如何进行过滤。
 *
 * <h5>查找</h5>
 *
 * <p>查找操作返回最佳匹配的语言标签。它在 RFC 4647 中定义如下：
 * “与过滤相反，每个语言范围表示最具体的标签，是可接受的匹配。根据用户的优先级，找到的第一个匹配标签被认为是最佳匹配，并且是返回的项。”
 *
 * <p>例如，如果一个语言优先级列表由两个语言范围组成，{@code "zh-Hant-TW"} 和 {@code "en-US"}，按优先级顺序排列，查找方法将按以下顺序逐步搜索语言标签，以找到最佳匹配的语言标签。
 * <blockquote>
 * <pre>
 *    1. zh-Hant-TW
 *    2. zh-Hant
 *    3. zh
 *    4. en-US
 *    5. en
 * </pre>
 * </blockquote>
 * 如果有一个语言标签完全匹配上述语言范围，则返回该语言标签。
 *
 * <p>{@code "*"} 是一个特殊的语言范围，在查找中被忽略。
 *
 * <p>如果多个语言标签因语言范围中包含子标签 {@code '*'} 而匹配，那么由一个 {@link Iterator} 遍历一个 {@link Collection} 的语言标签返回的第一个匹配语言标签被视为最佳匹配。
 *
 * <h4>Locale 的使用</h4>
 *
 * <p>一旦创建了 <code>Locale</code>，您就可以查询有关它本身的信息。使用 <code>getCountry</code> 获取国家（或地区）代码，使用 <code>getLanguage</code> 获取语言代码。您可以使用 <code>getDisplayCountry</code> 获取适合显示给用户的国家名称。同样，您可以使用 <code>getDisplayLanguage</code> 获取适合显示给用户的语言名称。有趣的是，<code>getDisplayXXX</code> 方法本身是区域敏感的，并且有两个版本：一个使用默认的 {@link Locale.Category#DISPLAY DISPLAY} 区域，另一个使用作为参数指定的区域。
 *
 * <p>Java 平台提供了许多执行区域敏感操作的类。例如，<code>NumberFormat</code> 类以区域敏感的方式格式化数字、货币和百分比。这些类（如 <code>NumberFormat</code>）提供了几个方便的方法来创建该类型的默认对象。例如，<code>NumberFormat</code> 类提供了以下三个方便的方法来创建默认的 <code>NumberFormat</code> 对象：
 * <blockquote>
 * <pre>
 *     NumberFormat.getInstance()
 *     NumberFormat.getCurrencyInstance()
 *     NumberFormat.getPercentInstance()
 * </pre>
 * </blockquote>
 * 每个方法都有两个变体；一个带有显式区域，一个没有；后者使用默认的 {@link Locale.Category#FORMAT FORMAT} 区域：
 * <blockquote>
 * <pre>
 *     NumberFormat.getInstance(myLocale)
 *     NumberFormat.getCurrencyInstance(myLocale)
 *     NumberFormat.getPercentInstance(myLocale)
 * </pre>
 * </blockquote>
 * <code>Locale</code> 是识别某种对象（如 <code>NumberFormat</code>）的机制。区域只是识别对象的机制，而不是对象本身的容器。
 *
 * <h4>兼容性</h4>
 *
 * <p>为了保持与现有用法的兼容性，Locale 的构造函数保留了 Java 运行时环境版本 1.7 之前的旧行为。同样，<code>toString</code> 方法在很大程度上也是如此。因此，Locale 对象可以继续像以前一样使用。特别是，解析 <code>toString</code> 的输出到语言、国家和变体字段的客户端可以继续这样做（尽管强烈不建议这样做），尽管如果存在脚本或扩展，变体字段将包含额外的信息。
 *
 * <p>此外，BCP 47 施加了语法限制，而这些限制并未由 Locale 的构造函数施加。这意味着在不丢失信息的情况下，某些 Locale 和 BCP 47 语言标签之间的转换无法进行。因此，<code>toLanguageTag</code> 无法表示语言、国家或变体不符合 BCP 47 的 Locale。
 *
 * <p>由于这些问题，建议客户端迁移到非符合标准的 Locale 的构建，并改用 <code>forLanguageTag</code> 和 <code>Locale.Builder</code> API。希望获得完整 Locale 字符串表示的客户端可以始终依赖 <code>toLanguageTag</code>。
 *
 * <h5><a name="special_cases_constructor">特殊情况</a></h5>
 *
 * <p>为了兼容性原因，两个非符合标准的 Locale 被视为特殊情况。这些是 <b><tt>ja_JP_JP</tt></b> 和 <b><tt>th_TH_TH</tt></b>。这些在 BCP 47 中是无效的，因为变体太短。为了便于迁移到 BCP 47，这些在构造时被特别处理。只有这两种情况会导致构造函数生成扩展，所有其他值的行为与 Java 7 之前完全相同。
 *
 * <p>Java 使用 <tt>ja_JP_JP</tt> 表示使用日本天皇历的日本日语。现在可以使用 Unicode 本地扩展来表示，通过指定 Unicode 本地键 <tt>ca</tt>（表示“日历”）和类型 <tt>japanese</tt>。当使用参数 "ja"、"JP"、"JP" 调用 Locale 构造函数时，会自动添加扩展 "u-ca-japanese"。
 *
 * <p>Java 使用 <tt>th_TH_TH</tt> 表示使用泰国数字的泰国语。现在也可以使用 Unicode 本地扩展来表示，通过指定 Unicode 本地键 <tt>nu</tt>（表示“数字”）和值 <tt>thai</tt>。当使用参数 "th"、"TH"、"TH" 调用 Locale 构造函数时，会自动添加扩展 "u-nu-thai"。
 *
 * <h5>序列化</h5>
 *
 * <p>在序列化期间，writeObject 将所有字段写入输出流，包括扩展。
 *
 * <p>在反序列化期间，readResolve 会根据 <a href="#special_cases_constructor">特殊情况</a> 中的描述添加扩展，仅适用于 th_TH_TH 和 ja_JP_JP 两种情况。
 *
 * <h5>旧语言代码</h5>
 *
 * <p>Locale 的构造函数始终将三个语言代码转换为它们较早的废弃形式：<tt>he</tt> 映射到 <tt>iw</tt>，<tt>yi</tt> 映射到 <tt>ji</tt>，<tt>id</tt> 映射到 <tt>in</tt>。为了不破坏向后兼容性，这种情况继续存在。
 *
 * <p>1.7 版本中添加的 API 在旧语言代码和新语言代码之间进行映射，保持旧代码在 Locale 内部（因此 <code>getLanguage</code> 和 <code>toString</code> 反映旧代码），但在 BCP 47 语言标签 API 中使用新代码（因此 <code>toLanguageTag</code> 反映新代码）。这保留了无论使用哪种代码或 API 构建它们，Locale 之间的等价性。Java 的默认资源包查找机制也实现了这种映射，因此资源可以使用任何约定命名，参见 {@link ResourceBundle.Control}。
 *
 * <h5>三字母语言/国家（地区）代码</h5>
 *
 * <p>Locale 构造函数始终指定语言和国家参数应为两个字符长，尽管实际上它们接受任何长度。规范现在已放宽，允许语言代码为 2 到 8 个字符，国家（地区）代码为 2 到 3 个字符，特别是 IANA 语言子标签注册表中指定的三字母语言代码和三位数字区域代码。为了兼容性，实现仍然不施加长度限制。
 *
 * @see Builder
 * @see ResourceBundle
 * @see java.text.Format
 * @see java.text.NumberFormat
 * @see java.text.Collator
 * @author Mark Davis
 * @since 1.1
 */
public final class Locale implements Cloneable, Serializable {


                static private final  Cache LOCALECACHE = new Cache();

    /** 有用的常量，表示语言。
     */
    static public final Locale ENGLISH = createConstant("en", "");

    /** 有用的常量，表示语言。
     */
    static public final Locale FRENCH = createConstant("fr", "");

    /** 有用的常量，表示语言。
     */
    static public final Locale GERMAN = createConstant("de", "");

    /** 有用的常量，表示语言。
     */
    static public final Locale ITALIAN = createConstant("it", "");

    /** 有用的常量，表示语言。
     */
    static public final Locale JAPANESE = createConstant("ja", "");

    /** 有用的常量，表示语言。
     */
    static public final Locale KOREAN = createConstant("ko", "");

    /** 有用的常量，表示语言。
     */
    static public final Locale CHINESE = createConstant("zh", "");

    /** 有用的常量，表示语言。
     */
    static public final Locale SIMPLIFIED_CHINESE = createConstant("zh", "CN");

    /** 有用的常量，表示语言。
     */
    static public final Locale TRADITIONAL_CHINESE = createConstant("zh", "TW");

    /** 有用的常量，表示国家。
     */
    static public final Locale FRANCE = createConstant("fr", "FR");

    /** 有用的常量，表示国家。
     */
    static public final Locale GERMANY = createConstant("de", "DE");

    /** 有用的常量，表示国家。
     */
    static public final Locale ITALY = createConstant("it", "IT");

    /** 有用的常量，表示国家。
     */
    static public final Locale JAPAN = createConstant("ja", "JP");

    /** 有用的常量，表示国家。
     */
    static public final Locale KOREA = createConstant("ko", "KR");

    /** 有用的常量，表示国家。
     */
    static public final Locale CHINA = SIMPLIFIED_CHINESE;

    /** 有用的常量，表示国家。
     */
    static public final Locale PRC = SIMPLIFIED_CHINESE;

    /** 有用的常量，表示国家。
     */
    static public final Locale TAIWAN = TRADITIONAL_CHINESE;

    /** 有用的常量，表示国家。
     */
    static public final Locale UK = createConstant("en", "GB");

    /** 有用的常量，表示国家。
     */
    static public final Locale US = createConstant("en", "US");

    /** 有用的常量，表示国家。
     */
    static public final Locale CANADA = createConstant("en", "CA");

    /** 有用的常量，表示国家。
     */
    static public final Locale CANADA_FRENCH = createConstant("fr", "CA");

    /**
     * 有用的常量，表示根区域设置。根区域设置的语言、国家和变体都是空字符串。这被视为所有区域设置的基础，并用于区域敏感操作的语言/国家中立区域设置。
     *
     * @since 1.6
     */
    static public final Locale ROOT = createConstant("", "");

    /**
     * 私有扩展的键（'x'）。
     *
     * @see #getExtension(char)
     * @see Builder#setExtension(char, String)
     * @since 1.7
     */
    static public final char PRIVATE_USE_EXTENSION = 'x';

    /**
     * Unicode 区域设置扩展的键（'u'）。
     *
     * @see #getExtension(char)
     * @see Builder#setExtension(char, String)
     * @since 1.7
     */
    static public final char UNICODE_LOCALE_EXTENSION = 'u';

    /** 序列化 ID
     */
    static final long serialVersionUID = 9149081749638150636L;

    /**
     * 用于从名称提供程序中检索本地化名称的显示类型。
     */
    private static final int DISPLAY_LANGUAGE = 0;
    private static final int DISPLAY_COUNTRY  = 1;
    private static final int DISPLAY_VARIANT  = 2;
    private static final int DISPLAY_SCRIPT   = 3;

    /**
     * 由 getInstance 方法使用的私有构造函数。
     */
    private Locale(BaseLocale baseLocale, LocaleExtensions extensions) {
        this.baseLocale = baseLocale;
        this.localeExtensions = extensions;
    }

    /**
     * 从语言、国家和变体构建区域设置。此构造函数将语言值规范化为小写，将国家值规范化为大写。
     * <p>
     * <b>注意：</b>
     * <ul>
     * <li>ISO 639 不是一个稳定的标准；它定义的一些语言代码（特别是 "iw"、"ji" 和 "in"）已经改变。此构造函数接受旧代码（"iw"、"ji" 和 "in"）和新代码（"he"、"yi" 和 "id"），但所有其他 Locale API 将只返回旧代码。
     * <li>出于向后兼容的原因，此构造函数不对输入进行任何语法检查。
     * <li>两种情况（"ja"、"JP"、"JP"）和（"th"、"TH"、"TH"）被特殊处理，更多信息请参见 <a href="#special_cases_constructor">特殊情况</a>。
     * </ul>
     *
     * @param language ISO 639 alpha-2 或 alpha-3 语言代码，或长度不超过 8 个字符的语言子标签。有关有效的语言值，请参见 <code>Locale</code> 类描述。
     * @param country ISO 3166 alpha-2 国家代码或 UN M.49 数字-3 区域代码。有关有效的国家值，请参见 <code>Locale</code> 类描述。
     * @param variant 用于表示 <code>Locale</code> 变体的任意值。有关详细信息，请参见 <code>Locale</code> 类描述。
     * @exception NullPointerException 如果任何参数为 null，则抛出此异常。
     */
    public Locale(String language, String country, String variant) {
        if (language == null || country == null || variant == null) {
            throw new NullPointerException();
        }
        baseLocale = BaseLocale.getInstance(convertOldISOCodes(language), "", country, variant);
        localeExtensions = getCompatibilityExtensions(language, "", country, variant);
    }

    /**
     * 从语言和国家构建区域设置。此构造函数将语言值规范化为小写，将国家值规范化为大写。
     * <p>
     * <b>注意：</b>
     * <ul>
     * <li>ISO 639 不是一个稳定的标准；它定义的一些语言代码（特别是 "iw"、"ji" 和 "in"）已经改变。此构造函数接受旧代码（"iw"、"ji" 和 "in"）和新代码（"he"、"yi" 和 "id"），但所有其他 Locale API 将只返回旧代码。
     * <li>出于向后兼容的原因，此构造函数不对输入进行任何语法检查。
     * </ul>
     *
     * @param language ISO 639 alpha-2 或 alpha-3 语言代码，或长度不超过 8 个字符的语言子标签。有关有效的语言值，请参见 <code>Locale</code> 类描述。
     * @param country ISO 3166 alpha-2 国家代码或 UN M.49 数字-3 区域代码。有关有效的国家值，请参见 <code>Locale</code> 类描述。
     * @exception NullPointerException 如果任何参数为 null，则抛出此异常。
     */
    public Locale(String language, String country) {
        this(language, country, "");
    }

    /**
     * 从语言代码构建区域设置。此构造函数将语言值规范化为小写。
     * <p>
     * <b>注意：</b>
     * <ul>
     * <li>ISO 639 不是一个稳定的标准；它定义的一些语言代码（特别是 "iw"、"ji" 和 "in"）已经改变。此构造函数接受旧代码（"iw"、"ji" 和 "in"）和新代码（"he"、"yi" 和 "id"），但所有其他 Locale API 将只返回旧代码。
     * <li>出于向后兼容的原因，此构造函数不对输入进行任何语法检查。
     * </ul>
     *
     * @param language ISO 639 alpha-2 或 alpha-3 语言代码，或长度不超过 8 个字符的语言子标签。有关有效的语言值，请参见 <code>Locale</code> 类描述。
     * @exception NullPointerException 如果参数为 null，则抛出此异常。
     * @since 1.4
     */
    public Locale(String language) {
        this(language, "", "");
    }

    /**
     * 仅用于创建 Locale.* 常量，以创建快捷方式。
     */
    private static Locale createConstant(String lang, String country) {
        BaseLocale base = BaseLocale.createInstance(lang, country);
        return getInstance(base, null);
    }

    /**
     * 从给定的 <code>language</code>、<code>country</code> 和 <code>variant</code> 构建 <code>Locale</code>。如果缓存中已有相同的 <code>Locale</code> 实例，则返回该实例。否则，创建新的 <code>Locale</code> 实例并缓存。
     *
     * @param language 小写的 2 到 8 位语言代码。
     * @param country 大写的 ISO-3166 两位国家代码和 UN M.49 数字-3 区域代码。
     * @param variant 供应商和浏览器特定的代码。请参见类描述。
     * @return 请求的 <code>Locale</code> 实例
     * @exception NullPointerException 如果任何参数为 null，则抛出此异常。
     */
    static Locale getInstance(String language, String country, String variant) {
        return getInstance(language, "", country, variant, null);
    }

    static Locale getInstance(String language, String script, String country,
                                      String variant, LocaleExtensions extensions) {
        if (language == null || script == null || country == null || variant == null) {
            throw new NullPointerException();
        }

        if (extensions == null) {
            extensions = getCompatibilityExtensions(language, script, country, variant);
        }

        BaseLocale baseloc = BaseLocale.getInstance(language, script, country, variant);
        return getInstance(baseloc, extensions);
    }

    static Locale getInstance(BaseLocale baseloc, LocaleExtensions extensions) {
        LocaleKey key = new LocaleKey(baseloc, extensions);
        return LOCALECACHE.get(key);
    }

    private static class Cache extends LocaleObjectCache<LocaleKey, Locale> {
        private Cache() {
        }

        @Override
        protected Locale createObject(LocaleKey key) {
            return new Locale(key.base, key.exts);
        }
    }

    private static final class LocaleKey {
        private final BaseLocale base;
        private final LocaleExtensions exts;
        private final int hash;

        private LocaleKey(BaseLocale baseLocale, LocaleExtensions extensions) {
            base = baseLocale;
            exts = extensions;

            // 在这里计算哈希值，因为它总是被使用。
            int h = base.hashCode();
            if (exts != null) {
                h ^= exts.hashCode();
            }
            hash = h;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LocaleKey)) {
                return false;
            }
            LocaleKey other = (LocaleKey)obj;
            if (hash != other.hash || !base.equals(other.base)) {
                return false;
            }
            if (exts == null) {
                return other.exts == null;
            }
            return exts.equals(other.exts);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    /**
     * 获取此 Java 虚拟机实例的默认区域设置的当前值。
     * <p>
     * Java 虚拟机在启动时根据主机环境设置默认区域设置。如果未显式指定区域设置，许多区域敏感的方法将使用它。可以使用
     * {@link #setDefault(java.util.Locale) setDefault} 方法更改它。
     *
     * @return 此 Java 虚拟机实例的默认区域设置
     */
    public static Locale getDefault() {
        // 不要同步此方法 - 参见 4071298
        return defaultLocale;
    }

    /**
     * 获取此 Java 虚拟机实例的指定类别的默认区域设置的当前值。
     * <p>
     * Java 虚拟机在启动时根据主机环境设置默认区域设置。如果未显式指定区域设置，许多区域敏感的方法将使用它。可以使用
     * setDefault(Locale.Category, Locale) 方法更改它。
     *
     * @param category - 要获取默认区域设置的指定类别
     * @throws NullPointerException - 如果类别为 null
     * @return 此 Java 虚拟机实例的指定类别的默认区域设置
     * @see #setDefault(Locale.Category, Locale)
     * @since 1.7
     */
    public static Locale getDefault(Locale.Category category) {
        // 不要同步此方法 - 参见 4071298
        switch (category) {
        case DISPLAY:
            if (defaultDisplayLocale == null) {
                synchronized(Locale.class) {
                    if (defaultDisplayLocale == null) {
                        defaultDisplayLocale = initDefault(category);
                    }
                }
            }
            return defaultDisplayLocale;
        case FORMAT:
            if (defaultFormatLocale == null) {
                synchronized(Locale.class) {
                    if (defaultFormatLocale == null) {
                        defaultFormatLocale = initDefault(category);
                    }
                }
            }
            return defaultFormatLocale;
        default:
            assert false: "Unknown Category";
        }
        return getDefault();
    }

    private static Locale initDefault() {
        String language, region, script, country, variant;
        language = AccessController.doPrivileged(
            new GetPropertyAction("user.language", "en"));
        // 为了兼容性，检查旧的 user.region 属性
        region = AccessController.doPrivileged(
            new GetPropertyAction("user.region"));
        if (region != null) {
            // region 可以是 country、country_variant 或 _variant 的形式
            int i = region.indexOf('_');
            if (i >= 0) {
                country = region.substring(0, i);
                variant = region.substring(i + 1);
            } else {
                country = region;
                variant = "";
            }
            script = "";
        } else {
            script = AccessController.doPrivileged(
                new GetPropertyAction("user.script", ""));
            country = AccessController.doPrivileged(
                new GetPropertyAction("user.country", ""));
            variant = AccessController.doPrivileged(
                new GetPropertyAction("user.variant", ""));
        }

        return getInstance(language, script, country, variant, null);
    }

    private static Locale initDefault(Locale.Category category) {
        return getInstance(
            AccessController.doPrivileged(
                new GetPropertyAction(category.languageKey, defaultLocale.getLanguage())),
            AccessController.doPrivileged(
                new GetPropertyAction(category.scriptKey, defaultLocale.getScript())),
            AccessController.doPrivileged(
                new GetPropertyAction(category.countryKey, defaultLocale.getCountry())),
            AccessController.doPrivileged(
                new GetPropertyAction(category.variantKey, defaultLocale.getVariant())),
            null);
    }


                /**
     * 为当前 Java 虚拟机实例设置默认区域设置。
     * 这不会影响主机区域设置。
     * <p>
     * 如果存在安全经理，其 <code>checkPermission</code>
     * 方法将使用 <code>PropertyPermission("user.language", "write")</code>
     * 权限在更改默认区域设置之前被调用。
     * <p>
     * Java 虚拟机在启动时根据主机环境设置默认区域设置。
     * 如果没有显式指定区域设置，许多区域敏感的方法都会使用它。
     * <p>
     * 由于更改默认区域设置可能会影响许多不同的功能领域，
     * 只有当调用者准备在同一 Java 虚拟机中重新初始化区域敏感代码时，
     * 才应使用此方法。
     * <p>
     * 通过此方法设置默认区域设置时，每个类别的所有默认区域设置
     * 也将设置为指定的默认区域设置。
     *
     * @throws SecurityException
     *        如果存在安全经理，并且其
     *        <code>checkPermission</code> 方法不允许此操作。
     * @throws NullPointerException 如果 <code>newLocale</code> 为 null
     * @param newLocale 新的默认区域设置
     * @see SecurityManager#checkPermission
     * @see java.util.PropertyPermission
     */
    public static synchronized void setDefault(Locale newLocale) {
        setDefault(Category.DISPLAY, newLocale);
        setDefault(Category.FORMAT, newLocale);
        defaultLocale = newLocale;
    }

    /**
     * 为当前 Java 虚拟机实例设置指定类别的默认区域设置。这不会影响主机区域设置。
     * <p>
     * 如果存在安全经理，其 <code>checkPermission</code> 方法将被调用
     * 使用 <code>PropertyPermission("user.language", "write")</code> 权限
     * 在更改默认区域设置之前。
     * <p>
     * Java 虚拟机在启动时根据主机环境设置默认区域设置。
     * 如果没有显式指定区域设置，许多区域敏感的方法都会使用它。
     * <p>
     * 由于更改默认区域设置可能会影响许多不同的功能领域，
     * 只有当调用者准备在同一 Java 虚拟机中重新初始化区域敏感代码时，
     * 才应使用此方法。
     * <p>
     *
     * @param category - 要设置默认区域设置的指定类别
     * @param newLocale - 新的默认区域设置
     * @throws SecurityException - 如果存在安全经理，并且其
     *     <code>checkPermission</code> 方法不允许此操作。
     * @throws NullPointerException - 如果 category 和/或 newLocale 为 null
     * @see SecurityManager#checkPermission(java.security.Permission)
     * @see PropertyPermission
     * @see #getDefault(Locale.Category)
     * @since 1.7
     */
    public static synchronized void setDefault(Locale.Category category,
        Locale newLocale) {
        if (category == null)
            throw new NullPointerException("类别不能为 NULL");
        if (newLocale == null)
            throw new NullPointerException("不能将默认区域设置设置为 NULL");

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new PropertyPermission
                        ("user.language", "write"));
        switch (category) {
        case DISPLAY:
            defaultDisplayLocale = newLocale;
            break;
        case FORMAT:
            defaultFormatLocale = newLocale;
            break;
        default:
            assert false: "未知类别";
        }
    }

    /**
     * 返回所有已安装的区域设置的数组。
     * 返回的数组表示 Java 运行时环境支持的区域设置和已安装的
     * {@link java.util.spi.LocaleServiceProvider LocaleServiceProvider}
     * 实现支持的区域设置的并集。它必须至少包含一个等于
     * {@link java.util.Locale#US Locale.US} 的 <code>Locale</code> 实例。
     *
     * @return 已安装的区域设置数组。
     */
    public static Locale[] getAvailableLocales() {
        return LocaleServiceProviderPool.getAllAvailableLocales();
    }

    /**
     * 返回 ISO 3166 中定义的所有 2 位国家/地区代码的列表。
     * 可用于创建区域设置。
     * <p>
     * <b>注意：</b> <code>Locale</code> 类还支持其他国家/地区代码，
     * 例如 3 位数字的 UN M.49 区域代码。
     * 因此，此方法返回的列表不包含所有可用于创建区域设置的有效代码。
     *
     * @return ISO 3166 两位国家/地区代码数组。
     */
    public static String[] getISOCountries() {
        if (isoCountries == null) {
            isoCountries = getISO2Table(LocaleISOData.isoCountryTable);
        }
        String[] result = new String[isoCountries.length];
        System.arraycopy(isoCountries, 0, result, 0, isoCountries.length);
        return result;
    }

    /**
     * 返回 ISO 639 中定义的所有 2 位语言代码的列表。
     * 可用于创建区域设置。
     * <p>
     * <b>注意：</b>
     * <ul>
     * <li>ISO 639 不是一个稳定的标准——一些语言的代码已经改变。
     * 此函数返回的列表包括代码已更改的语言的新旧代码。
     * <li><code>Locale</code> 类还支持长度为 8 个字符的语言代码。
     * 因此，此方法返回的列表不包含所有可用于创建区域设置的有效代码。
     * </ul>
     *
     * @return ISO 639 两位语言代码数组。
     */
    public static String[] getISOLanguages() {
        if (isoLanguages == null) {
            isoLanguages = getISO2Table(LocaleISOData.isoLanguageTable);
        }
        String[] result = new String[isoLanguages.length];
        System.arraycopy(isoLanguages, 0, result, 0, isoLanguages.length);
        return result;
    }

    private static String[] getISO2Table(String table) {
        int len = table.length() / 5;
        String[] isoTable = new String[len];
        for (int i = 0, j = 0; i < len; i++, j += 5) {
            isoTable[i] = table.substring(j, j + 2);
        }
        return isoTable;
    }

    /**
     * 返回此区域设置的语言代码。
     *
     * <p><b>注意：</b> ISO 639 不是一个稳定的标准——一些语言的代码已经改变。
     * 区域设置的构造函数识别代码已更改的语言的新旧代码，但此函数始终返回旧代码。如果您
     * 想检查代码已更改的特定语言，不要这样做
     * <pre>
     * if (locale.getLanguage().equals("he")) // 错误！
     *    ...
     * </pre>
     * 而是这样做
     * <pre>
     * if (locale.getLanguage().equals(new Locale("he").getLanguage()))
     *    ...
     * </pre>
     * @return 语言代码，如果未定义则返回空字符串。
     * @see #getDisplayLanguage
     */
    public String getLanguage() {
        return baseLocale.getLanguage();
    }

    /**
     * 返回此区域设置的脚本，应为
     * 空字符串或 ISO 15924 4 位脚本代码。第一个字母大写，其余字母小写，
     * 例如，'Latn'，'Cyrl'。
     *
     * @return 脚本代码，如果未定义则返回空字符串。
     * @see #getDisplayScript
     * @since 1.7
     */
    public String getScript() {
        return baseLocale.getScript();
    }

    /**
     * 返回此区域设置的国家/地区代码，应为
     * 空字符串、大写的 ISO 3166 2 位代码或
     * UN M.49 3 位代码。
     *
     * @return 国家/地区代码，如果未定义则返回空字符串。
     * @see #getDisplayCountry
     */
    public String getCountry() {
        return baseLocale.getRegion();
    }

    /**
     * 返回此区域设置的变体代码。
     *
     * @return 变体代码，如果未定义则返回空字符串。
     * @see #getDisplayVariant
     */
    public String getVariant() {
        return baseLocale.getVariant();
    }

    /**
     * 如果此 <code>Locale</code> 有任何 <a href="#def_extensions">
     * 扩展</a>，则返回 <code>true</code>。
     *
     * @return 如果此 <code>Locale</code> 有任何扩展，则返回 <code>true</code>
     * @since 1.8
     */
    public boolean hasExtensions() {
        return localeExtensions != null;
    }

    /**
     * 返回一个没有 <a href="#def_extensions">
     * 扩展</a> 的此 <code>Locale</code> 的副本。如果此 <code>Locale</code> 没有扩展，
     * 则返回此 <code>Locale</code>。
     *
     * @return 一个没有扩展的此 <code>Locale</code> 的副本，或如果此 <code>Locale</code>
     * 没有扩展，则返回 <code>this</code>
     * @since 1.8
     */
    public Locale stripExtensions() {
        return hasExtensions() ? Locale.getInstance(baseLocale, null) : this;
    }

    /**
     * 返回与指定键关联的扩展（或私有使用）值，如果没有与键关联的扩展，则返回 null。
     * 为了格式良好，键必须是 <code>[0-9A-Za-z]</code> 之一。键不区分大小写，
     * 因此例如 'z' 和 'Z' 表示相同的扩展。
     *
     * @param key 扩展键
     * @return 与键关联的扩展，如果此区域设置未定义与指定键关联的扩展，则返回 null。
     * @throws IllegalArgumentException 如果键格式不正确
     * @see #PRIVATE_USE_EXTENSION
     * @see #UNICODE_LOCALE_EXTENSION
     * @since 1.7
     */
    public String getExtension(char key) {
        if (!LocaleExtensions.isValidKey(key)) {
            throw new IllegalArgumentException("格式不正确的扩展键: " + key);
        }
        return hasExtensions() ? localeExtensions.getExtensionValue(key) : null;
    }

    /**
     * 返回与此区域设置关联的扩展键集，如果它没有扩展，则返回空集。返回的集不可修改。
     * 键将全部为小写。
     *
     * @return 扩展键集，如果此区域设置没有扩展，则返回空集。
     * @since 1.7
     */
    public Set<Character> getExtensionKeys() {
        if (!hasExtensions()) {
            return Collections.emptySet();
        }
        return localeExtensions.getKeys();
    }

    /**
     * 返回与此区域设置关联的 Unicode 区域属性集，如果它没有属性，则返回空集。返回的集不可修改。
     *
     * @return 属性集。
     * @since 1.7
     */
    public Set<String> getUnicodeLocaleAttributes() {
        if (!hasExtensions()) {
            return Collections.emptySet();
        }
        return localeExtensions.getUnicodeLocaleAttributes();
    }

    /**
     * 返回与此区域设置关联的指定 Unicode 区域键的 Unicode 区域类型。
     * 对于定义但没有类型的键，返回空字符串。如果未定义键，则返回 null。
     * 键不区分大小写。键必须是两个字母数字字符 ([0-9a-zA-Z])，否则将抛出 IllegalArgumentException。
     *
     * @param key Unicode 区域键
     * @return 与此键关联的 Unicode 区域类型，如果区域设置未定义此键，则返回 null。
     * @throws IllegalArgumentException 如果键格式不正确
     * @throws NullPointerException 如果 <code>key</code> 为 null
     * @since 1.7
     */
    public String getUnicodeLocaleType(String key) {
        if (!isUnicodeExtensionKey(key)) {
            throw new IllegalArgumentException("格式不正确的 Unicode 区域键: " + key);
        }
        return hasExtensions() ? localeExtensions.getUnicodeLocaleType(key) : null;
    }

    /**
     * 返回此区域设置定义的 Unicode 区域键集，如果此区域设置没有定义任何 Unicode 区域键，则返回空集。
     * 返回的集不可修改。键全部为小写。
     *
     * @return Unicode 区域键集，如果此区域设置没有定义任何 Unicode 区域键，则返回空集。
     * @since 1.7
     */
    public Set<String> getUnicodeLocaleKeys() {
        if (localeExtensions == null) {
            return Collections.emptySet();
        }
        return localeExtensions.getUnicodeLocaleKeys();
    }

    /**
     * 包级区域设置方法，返回区域设置的 BaseLocale，由 ResourceBundle 使用
     * @return 此区域设置的基区域设置
     */
    BaseLocale getBaseLocale() {
        return baseLocale;
    }

    /**
     * 包级私有方法，返回区域设置的 LocaleExtensions，由 ResourceBundle 使用。
     * @return 此区域设置的区域扩展，
     *         或如果未定义扩展，则返回 {@code null}
     */
     LocaleExtensions getLocaleExtensions() {
         return localeExtensions;
     }

    /**
     * 返回此 <code>Locale</code> 对象的字符串表示形式，包括语言、国家、变体、脚本和扩展，如下所示：
     * <blockquote>
     * 语言 + "_" + 国家 + "_" + (变体 + "_#" | "#") + 脚本 + "-" + 扩展
     * </blockquote>
     *
     * 语言始终小写，国家始终大写，脚本始终首字母大写，扩展始终小写。扩展和私有使用子标签
     * 将按 {@link #toLanguageTag} 中解释的规范顺序排列。
     *
     * <p>当区域设置既没有脚本也没有扩展时，结果与 Java 6 及之前的版本相同。
     *
     * <p>如果语言和国家字段都缺失，即使变体、脚本或扩展字段存在，此函数也将返回空字符串
     * （您不能只有变体，变体必须伴随一个格式良好的语言或国家代码）。
     *
     * <p>如果存在脚本或扩展且变体缺失，则不会在 "#" 前添加下划线。
     *
     * <p>此行为旨在支持调试并兼容以前使用 <code>toString</code> 的情况，这些情况只期望语言、国家和变体字段。
     * 要将区域设置表示为用于交换的字符串，请使用 {@link #toLanguageTag}。
     *
     * <p>示例： <ul>
     * <li><tt>en</tt></li>
     * <li><tt>de_DE</tt></li>
     * <li><tt>_GB</tt></li>
     * <li><tt>en_US_WIN</tt></li>
     * <li><tt>de__POSIX</tt></li>
     * <li><tt>zh_CN_#Hans</tt></li>
     * <li><tt>zh_TW_#Hant-x-java</tt></li>
     * <li><tt>th_TH_TH_#u-nu-thai</tt></li></ul>
     *
     * @return 区域设置的字符串表示形式，用于调试。
     * @see #getDisplayName
     * @see #toLanguageTag
     */
    @Override
    public final String toString() {
        boolean l = (baseLocale.getLanguage().length() != 0);
        boolean s = (baseLocale.getScript().length() != 0);
        boolean r = (baseLocale.getRegion().length() != 0);
        boolean v = (baseLocale.getVariant().length() != 0);
        boolean e = (localeExtensions != null && localeExtensions.getID().length() != 0);

        StringBuilder result = new StringBuilder(baseLocale.getLanguage());
        if (r || (l && (v || s || e))) {
            result.append('_')
                .append(baseLocale.getRegion()); // 这可能会只追加 '_'
        }
        if (v && (l || r)) {
            result.append('_')
                .append(baseLocale.getVariant());
        }


    /**
     * 返回一个表示此区域设置语言的适合显示给用户的名称。
     * 如果可能，返回的名称将针对默认的 {@link Locale.Category#DISPLAY DISPLAY} 区域设置进行本地化。
     * 例如，如果区域设置为 fr_FR，且默认的 {@link Locale.Category#DISPLAY DISPLAY} 区域设置为 en_US，
     * getDisplayLanguage() 将返回 "French"；如果区域设置为 en_US，且默认的 {@link Locale.Category#DISPLAY DISPLAY} 区域设置为 fr_FR，
     * getDisplayLanguage() 将返回 "anglais"。
     * 如果返回的名称无法针对默认的 {@link Locale.Category#DISPLAY DISPLAY} 区域设置进行本地化，
     * （例如，我们没有克罗地亚语的日语名称），
     * 此函数将回退到英语名称，并使用 ISO 代码作为最后的备用值。如果区域设置未指定语言，此函数将返回空字符串。
     *
     * @return 显示语言的名称。
     */
    public final String getDisplayLanguage() {
        return getDisplayLanguage(getDefault(Category.DISPLAY));
    }


                /**
     * 返回适合显示给用户的语言名称。
     * 如果可能，返回的名称将根据 inLocale 进行本地化。
     * 例如，如果 locale 是 fr_FR 而 inLocale
     * 是 en_US，getDisplayLanguage() 将返回 "French"；如果 locale 是 en_US 而
     * inLocale 是 fr_FR，getDisplayLanguage() 将返回 "anglais"。
     * 如果返回的名称无法根据 inLocale 进行本地化，
     * （比如说，我们没有克罗地亚语的日语名称），
     * 此函数将回退到英语名称，最后
     * 使用 ISO 代码作为最后的备用值。 如果 locale 没有指定语言，
     * 此函数将返回空字符串。
     *
     * @param inLocale 要检索显示语言的 locale。
     * @return 适合给定 locale 的显示语言名称。
     * @exception NullPointerException 如果 <code>inLocale</code> 为 <code>null</code>
     */
    public String getDisplayLanguage(Locale inLocale) {
        return getDisplayString(baseLocale.getLanguage(), inLocale, DISPLAY_LANGUAGE);
    }

    /**
     * 返回适合显示给用户的脚本名称。如果可能，名称将
     * 根据默认的 {@link Locale.Category#DISPLAY DISPLAY} locale 进行本地化。 如果此
     * locale 没有指定脚本代码，返回空字符串。
     *
     * @return 当前默认 {@link Locale.Category#DISPLAY DISPLAY} locale 的脚本代码的显示名称
     * @since 1.7
     */
    public String getDisplayScript() {
        return getDisplayScript(getDefault(Category.DISPLAY));
    }

    /**
     * 返回适合显示给用户的 locale 的脚本名称。如果可能，名称将
     * 根据给定的 locale 进行本地化。 如果此 locale 没有指定脚本代码，返回空字符串。
     *
     * @param inLocale 要检索显示脚本的 locale。
     * @return 当前默认 {@link Locale.Category#DISPLAY DISPLAY} locale 的脚本代码的显示名称
     * @throws NullPointerException 如果 <code>inLocale</code> 为 <code>null</code>
     * @since 1.7
     */
    public String getDisplayScript(Locale inLocale) {
        return getDisplayString(baseLocale.getScript(), inLocale, DISPLAY_SCRIPT);
    }

    /**
     * 返回适合显示给用户的国家名称。
     * 如果可能，返回的名称将根据默认的
     * {@link Locale.Category#DISPLAY DISPLAY} locale 进行本地化。
     * 例如，如果 locale 是 fr_FR 而默认的
     * {@link Locale.Category#DISPLAY DISPLAY} locale
     * 是 en_US，getDisplayCountry() 将返回 "France"；如果 locale 是 en_US 而
     * 默认的 {@link Locale.Category#DISPLAY DISPLAY} locale 是 fr_FR，
     * getDisplayCountry() 将返回 "Etats-Unis"。
     * 如果返回的名称无法根据默认的
     * {@link Locale.Category#DISPLAY DISPLAY} locale 进行本地化，
     * （比如说，我们没有克罗地亚的日语名称），
     * 此函数将回退到英语名称，并使用 ISO 代码作为最后的备用值。 如果 locale 没有指定国家，
     * 此函数将返回空字符串。
     *
     * @return 适合 locale 的国家名称。
     */
    public final String getDisplayCountry() {
        return getDisplayCountry(getDefault(Category.DISPLAY));
    }

    /**
     * 返回适合显示给用户的国家名称。
     * 如果可能，返回的名称将根据 inLocale 进行本地化。
     * 例如，如果 locale 是 fr_FR 而 inLocale
     * 是 en_US，getDisplayCountry() 将返回 "France"；如果 locale 是 en_US 而
     * inLocale 是 fr_FR，getDisplayCountry() 将返回 "Etats-Unis"。
     * 如果返回的名称无法根据 inLocale 进行本地化。
     * （比如说，我们没有克罗地亚的日语名称），
     * 此函数将回退到英语名称，并最终
     * 使用 ISO 代码作为最后的备用值。 如果 locale 没有指定国家，
     * 此函数将返回空字符串。
     *
     * @param inLocale 要检索显示国家的 locale。
     * @return 适合给定 locale 的国家名称。
     * @exception NullPointerException 如果 <code>inLocale</code> 为 <code>null</code>
     */
    public String getDisplayCountry(Locale inLocale) {
        return getDisplayString(baseLocale.getRegion(), inLocale, DISPLAY_COUNTRY);
    }

    private String getDisplayString(String code, Locale inLocale, int type) {
        if (code.length() == 0) {
            return "";
        }

        if (inLocale == null) {
            throw new NullPointerException();
        }

        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(LocaleNameProvider.class);
        String key = (type == DISPLAY_VARIANT ? "%%"+code : code);
        String result = pool.getLocalizedObject(
                                LocaleNameGetter.INSTANCE,
                                inLocale, key, type, code);
            if (result != null) {
                return result;
            }

        return code;
    }

    /**
     * 返回适合显示给用户的 locale 的变体代码名称。 如果可能，名称将
     * 根据默认的 {@link Locale.Category#DISPLAY DISPLAY} locale 进行本地化。 如果此
     * locale 没有指定变体代码，此函数将返回空字符串。
     *
     * @return 适合 locale 的显示变体代码名称。
     */
    public final String getDisplayVariant() {
        return getDisplayVariant(getDefault(Category.DISPLAY));
    }

    /**
     * 返回适合显示给用户的 locale 的变体代码名称。 如果可能，名称将
     * 根据 inLocale 进行本地化。 如果此 locale 没有指定变体代码，此函数将返回空字符串。
     *
     * @param inLocale 要检索显示变体代码的 locale。
     * @return 适合给定 locale 的显示变体代码名称。
     * @exception NullPointerException 如果 <code>inLocale</code> 为 <code>null</code>
     */
    public String getDisplayVariant(Locale inLocale) {
        if (baseLocale.getVariant().length() == 0)
            return "";

        LocaleResources lr = LocaleProviderAdapter.forJRE().getLocaleResources(inLocale);

        String names[] = getDisplayVariantArray(inLocale);

        // 获取用于格式化列表的本地化模式，并使用它们格式化列表。
        return formatList(names,
                          lr.getLocaleName("ListPattern"),
                          lr.getLocaleName("ListCompositionPattern"));
    }

    /**
     * 返回适合显示给用户的 locale 的名称。这将是 getDisplayLanguage()、
     * getDisplayScript()、getDisplayCountry() 和 getDisplayVariant() 返回的值组合成的单个字符串。
     * 使用非空值，按顺序使用，
     * 第二个及后续名称放在括号中。 例如：
     * <blockquote>
     * 语言 (脚本, 国家, 变体)<br>
     * 语言 (国家)<br>
     * 语言 (变体)<br>
     * 脚本 (国家)<br>
     * 国家<br>
     * </blockquote>
     * 具体取决于 locale 中指定的字段。 如果
     * 语言、脚本、国家和变体字段都为空，
     * 此函数将返回空字符串。
     *
     * @return 适合显示的 locale 名称。
     */
    public final String getDisplayName() {
        return getDisplayName(getDefault(Category.DISPLAY));
    }

    /**
     * 返回适合显示给用户的 locale 的名称。
     * 这将是 getDisplayLanguage()、getDisplayScript()、getDisplayCountry()
     * 和 getDisplayVariant() 返回的值组合成的单个字符串。
     * 使用非空值，按顺序使用，
     * 第二个及后续名称放在括号中。 例如：
     * <blockquote>
     * 语言 (脚本, 国家, 变体)<br>
     * 语言 (国家)<br>
     * 语言 (变体)<br>
     * 脚本 (国家)<br>
     * 国家<br>
     * </blockquote>
     * 具体取决于 locale 中指定的字段。 如果
     * 语言、脚本、国家和变体字段都为空，
     * 此函数将返回空字符串。
     *
     * @param inLocale 要检索显示名称的 locale。
     * @return 适合显示的 locale 名称。
     * @throws NullPointerException 如果 <code>inLocale</code> 为 <code>null</code>
     */
    public String getDisplayName(Locale inLocale) {
        LocaleResources lr =  LocaleProviderAdapter.forJRE().getLocaleResources(inLocale);

        String languageName = getDisplayLanguage(inLocale);
        String scriptName = getDisplayScript(inLocale);
        String countryName = getDisplayCountry(inLocale);
        String[] variantNames = getDisplayVariantArray(inLocale);

        // 获取用于格式化显示名称的本地化模式。
        String displayNamePattern = lr.getLocaleName("DisplayNamePattern");
        String listPattern = lr.getLocaleName("ListPattern");
        String listCompositionPattern = lr.getLocaleName("ListCompositionPattern");

        // 显示名称由主名称和限定符组成。
        // 通常，格式为 "主名称 (限定符, 限定符)"，但这也取决于显示 locale 中存储的模式。
        String   mainName       = null;
        String[] qualifierNames = null;

        // 主名称是语言，如果没有语言，则是脚本，
        // 然后如果没有脚本，则是国家。 如果没有语言/脚本/国家
        // （一种异常情况），则显示名称仅是变体的显示名称。
        if (languageName.length() == 0 && scriptName.length() == 0 && countryName.length() == 0) {
            if (variantNames.length == 0) {
                return "";
            } else {
                return formatList(variantNames, listPattern, listCompositionPattern);
            }
        }
        ArrayList<String> names = new ArrayList<>(4);
        if (languageName.length() != 0) {
            names.add(languageName);
        }
        if (scriptName.length() != 0) {
            names.add(scriptName);
        }
        if (countryName.length() != 0) {
            names.add(countryName);
        }
        if (variantNames.length != 0) {
            names.addAll(Arrays.asList(variantNames));
        }

        // 第一个作为主名称
        mainName = names.get(0);

        // 其他作为限定符
        int numNames = names.size();
        qualifierNames = (numNames > 1) ?
                names.subList(1, numNames).toArray(new String[numNames - 1]) : new String[0];

        // 创建一个数组，其第一个元素是剩余元素的数量。 这作为选择器，用于从资源中选择 ChoiceFormat 模式。
        // 第二个和第三个元素是主名称和限定符；如果没有限定符，第三个元素将不会被格式模式使用。
        Object[] displayNames = {
            new Integer(qualifierNames.length != 0 ? 2 : 1),
            mainName,
            // 我们也可以调用 formatList() 并让它处理空列表的情况，但这更高效，因为我们希望它高效，
            // 因为所有只有语言的 locale 都不会有任何限定符。
            qualifierNames.length != 0 ? formatList(qualifierNames, listPattern, listCompositionPattern) : null
        };

        if (displayNamePattern != null) {
            return new MessageFormat(displayNamePattern).format(displayNames);
        }
        else {
            // 如果无法获取消息格式模式，则使用简单的硬编码模式。 实际上，除非安装缺少某些核心文件（如 FormatData 等），
            // 否则这种情况不应该发生。
            StringBuilder result = new StringBuilder();
            result.append((String)displayNames[1]);
            if (displayNames.length > 2) {
                result.append(" (");
                result.append((String)displayNames[2]);
                result.append(')');
            }
            return result.toString();
        }
    }

    /**
     * 覆盖 Cloneable。
     */
    @Override
    public Object clone()
    {
        try {
            Locale that = (Locale)super.clone();
            return that;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 覆盖 hashCode。
     * 由于 Locales 经常用于哈希表，缓存值以提高速度。
     */
    @Override
    public int hashCode() {
        int hc = hashCodeValue;
        if (hc == 0) {
            hc = baseLocale.hashCode();
            if (localeExtensions != null) {
                hc ^= localeExtensions.hashCode();
            }
            hashCodeValue = hc;
        }
        return hc;
    }

    // 覆盖

    /**
     * 如果此 Locale 等于另一个对象，则返回 true。 如果两个 Locale 的语言、脚本、国家、
     * 变体和扩展都相同，则认为它们相等，否则不相等。
     *
     * @return 如果此 Locale 等于指定的对象，则返回 true。
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)                      // 快速检查
            return true;
        if (!(obj instanceof Locale))
            return false;
        BaseLocale otherBase = ((Locale)obj).baseLocale;
        if (!baseLocale.equals(otherBase)) {
            return false;
        }
        if (localeExtensions == null) {
            return ((Locale)obj).localeExtensions == null;
        }
        return localeExtensions.equals(((Locale)obj).localeExtensions);
    }

    // ================= 私有成员 =====================================

    private transient BaseLocale baseLocale;
    private transient LocaleExtensions localeExtensions;

    /**
     * 计算的哈希码
     */
    private transient volatile int hashCodeValue = 0;

    private volatile static Locale defaultLocale = initDefault();
    private volatile static Locale defaultDisplayLocale = null;
    private volatile static Locale defaultFormatLocale = null;

    private transient volatile String languageTag;

    /**
     * 返回变体的显示名称数组。
     * @param bundle 用于获取显示名称的 ResourceBundle
     * @return 显示名称数组，可能长度为零。
     */
    private String[] getDisplayVariantArray(Locale inLocale) {
        // 将变体名称拆分为以 '_' 分隔的标记。
        StringTokenizer tokenizer = new StringTokenizer(baseLocale.getVariant(), "_");
        String[] names = new String[tokenizer.countTokens()];


                    // 对于每个变体标记，查找显示名称。如果
        // 未找到，则使用变体名称本身。
        for (int i=0; i<names.length; ++i) {
            names[i] = getDisplayString(tokenizer.nextToken(),
                                inLocale, DISPLAY_VARIANT);
        }

        return names;
    }

    /**
     * 使用给定的模式字符串格式化列表。
     * 如果任一模式为 null，则列表通过连接分隔符 ',' 进行格式化。
     * @param stringList 要格式化的字符串列表。
     * @param listPattern 应创建一个接受 0-3 个参数的 MessageFormat
     * 并将它们格式化为列表。
     * @param listCompositionPattern 应接受 2 个参数
     * 并由 composeList 使用。
     * @return 代表列表的字符串。
     */
    private static String formatList(String[] stringList, String listPattern, String listCompositionPattern) {
        // 如果没有列表模式，则以简单、非本地化的方式组成列表。
        if (listPattern == null || listCompositionPattern == null) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < stringList.length; ++i) {
                if (i > 0) {
                    result.append(',');
                }
                result.append(stringList[i]);
            }
            return result.toString();
        }

        // 如有必要，将列表组成三个元素
        if (stringList.length > 3) {
            MessageFormat format = new MessageFormat(listCompositionPattern);
            stringList = composeList(format, stringList);
        }

        // 用列表长度作为第一个元素重建参数列表
        Object[] args = new Object[stringList.length + 1];
        System.arraycopy(stringList, 0, args, 1, stringList.length);
        args[0] = new Integer(stringList.length);

        // 使用资源中的模式进行格式化
        MessageFormat format = new MessageFormat(listPattern);
        return format.format(args);
    }

    /**
     * 给定一个字符串列表，返回一个缩短到三个元素的列表。
     * 通过递归应用给定的格式来缩短前两个元素。
     * @param format 一个接受两个参数的格式
     * @param list 字符串列表
     * @return 如果列表有三个或更少元素，则返回相同的列表；
     * 否则，返回一个包含三个元素的新列表。
     */
    private static String[] composeList(MessageFormat format, String[] list) {
        if (list.length <= 3) return list;

        // 使用给定的格式将前两个元素组合成一个
        String[] listItems = { list[0], list[1] };
        String newItem = format.format(listItems);

        // 形成一个元素较少的新列表
        String[] newList = new String[list.length-1];
        System.arraycopy(list, 2, newList, 1, newList.length-1);
        newList[0] = newItem;

        // 递归
        return composeList(format, newList);
    }

    // sun.util.locale.UnicodeLocaleExtension.isKey 的副本，以避免其类加载。
    private static boolean isUnicodeExtensionKey(String s) {
        // 2alphanum
        return (s.length() == 2) && LocaleUtils.isAlphaNumericString(s);
    }

    /**
     * @serialField language    String
     *      小写的语言子标签。 (参见 <a href="java/util/Locale.html#getLanguage()">getLanguage()</a>)
     * @serialField country     String
     *      大写的国家子标签。 (参见 <a href="java/util/Locale.html#getCountry()">getCountry()</a>)
     * @serialField variant     String
     *      由 LOWLINE 字符分隔的变体子标签。 (参见 <a href="java/util/Locale.html#getVariant()">getVariant()</a>)
     * @serialField hashcode    int
     *      已弃用，仅用于向前兼容
     * @serialField script      String
     *      标题大小写的脚本子标签 (参见 <a href="java/util/Locale.html#getScript()">getScript()</a>)
     * @serialField extensions  String
     *      扩展的规范表示，即，
     *      BCP47 扩展按字母顺序排列，后跟
     *      BCP47 私有使用子标签，所有字母均为小写
     *      由 HYPHEN-MINUS 字符分隔。
     *      (参见 <a href="java/util/Locale.html#getExtensionKeys()">getExtensionKeys()</a>,
     *      <a href="java/util/Locale.html#getExtension(char)">getExtension(char)</a>)
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("language", String.class),
        new ObjectStreamField("country", String.class),
        new ObjectStreamField("variant", String.class),
        new ObjectStreamField("hashcode", int.class),
        new ObjectStreamField("script", String.class),
        new ObjectStreamField("extensions", String.class),
    };

    /**
     * 将此 <code>Locale</code> 序列化到指定的 <code>ObjectOutputStream</code>。
     * @param out 要写入的 <code>ObjectOutputStream</code>
     * @throws IOException
     * @since 1.7
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("language", baseLocale.getLanguage());
        fields.put("script", baseLocale.getScript());
        fields.put("country", baseLocale.getRegion());
        fields.put("variant", baseLocale.getVariant());
        fields.put("extensions", localeExtensions == null ? "" : localeExtensions.getID());
        fields.put("hashcode", -1); // 仅用于向后支持的占位符
        out.writeFields();
    }

    /**
     * 反序列化此 <code>Locale</code>。
     * @param in 要读取的 <code>ObjectInputStream</code>
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllformedLocaleException
     * @since 1.7
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        String language = (String)fields.get("language", "");
        String script = (String)fields.get("script", "");
        String country = (String)fields.get("country", "");
        String variant = (String)fields.get("variant", "");
        String extStr = (String)fields.get("extensions", "");
        baseLocale = BaseLocale.getInstance(convertOldISOCodes(language), script, country, variant);
        if (extStr.length() > 0) {
            try {
                InternalLocaleBuilder bldr = new InternalLocaleBuilder();
                bldr.setExtensions(extStr);
                localeExtensions = bldr.getLocaleExtensions();
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage());
            }
        } else {
            localeExtensions = null;
        }
    }

    /**
     * 返回一个等效于反序列化的 <code>Locale</code> 的缓存 <code>Locale</code> 实例。
     * 当从对象数据流中读取的序列化语言、国家和变体字段恰好为 "ja", "JP", "JP" 或 "th", "TH", "TH" 且脚本/扩展字段为空时，
     * 此方法提供 <code>UNICODE_LOCALE_EXTENSION</code> "ca"/"japanese" (日历类型为 "japanese") 或 "nu"/"thai" (数字脚本类型为 "thai")。
     * 有关更多信息，请参见 <a href="Locale.html#special_cases_constructor">特殊情况</a>。
     *
     * @return 等效于反序列化的 <code>Locale</code> 的 <code>Locale</code> 实例。
     * @throws java.io.ObjectStreamException
     */
    private Object readResolve() throws java.io.ObjectStreamException {
        return getInstance(baseLocale.getLanguage(), baseLocale.getScript(),
                baseLocale.getRegion(), baseLocale.getVariant(), localeExtensions);
    }

    private static volatile String[] isoLanguages = null;

    private static volatile String[] isoCountries = null;

    private static String convertOldISOCodes(String language) {
        // 我们接受 ISO 代码已更改的语言的旧代码和新代码，但始终存储旧代码，以保持向后兼容性
        language = LocaleUtils.toLowerString(language).intern();
        if (language == "he") {
            return "iw";
        } else if (language == "yi") {
            return "ji";
        } else if (language == "id") {
            return "in";
        } else {
            return language;
        }
    }

    private static LocaleExtensions getCompatibilityExtensions(String language,
                                                               String script,
                                                               String country,
                                                               String variant) {
        LocaleExtensions extensions = null;
        // 用于向后兼容支持的特殊情况
        if (LocaleUtils.caseIgnoreMatch(language, "ja")
                && script.length() == 0
                && LocaleUtils.caseIgnoreMatch(country, "jp")
                && "JP".equals(variant)) {
            // ja_JP_JP -> u-ca-japanese (日历 = 日本)
            extensions = LocaleExtensions.CALENDAR_JAPANESE;
        } else if (LocaleUtils.caseIgnoreMatch(language, "th")
                && script.length() == 0
                && LocaleUtils.caseIgnoreMatch(country, "th")
                && "TH".equals(variant)) {
            // th_TH_TH -> u-nu-thai (数字系统 = 泰国)
            extensions = LocaleExtensions.NUMBER_THAI;
        }
        return extensions;
    }

    /**
     * 从 LocaleNameProvider 实现中获取本地化的区域设置名称。
     */
    private static class LocaleNameGetter
        implements LocaleServiceProviderPool.LocalizedObjectGetter<LocaleNameProvider, String> {
        private static final LocaleNameGetter INSTANCE = new LocaleNameGetter();

        @Override
        public String getObject(LocaleNameProvider localeNameProvider,
                                Locale locale,
                                String key,
                                Object... params) {
            assert params.length == 2;
            int type = (Integer)params[0];
            String code = (String)params[1];

            switch(type) {
            case DISPLAY_LANGUAGE:
                return localeNameProvider.getDisplayLanguage(code, locale);
            case DISPLAY_COUNTRY:
                return localeNameProvider.getDisplayCountry(code, locale);
            case DISPLAY_VARIANT:
                return localeNameProvider.getDisplayVariant(code, locale);
            case DISPLAY_SCRIPT:
                return localeNameProvider.getDisplayScript(code, locale);
            default:
                assert false; // 不应发生
            }

            return null;
        }
    }

    /**
     * 区域设置类别的枚举。这些区域设置类别用于获取/设置特定功能的默认区域设置。
     *
     * @see #getDefault(Locale.Category)
     * @see #setDefault(Locale.Category, Locale)
     * @since 1.7
     */
    public enum Category {

        /**
         * 用于表示显示用户界面的默认区域设置的类别。
         */
        DISPLAY("user.language.display",
                "user.script.display",
                "user.country.display",
                "user.variant.display"),

        /**
         * 用于表示格式化日期、数字和/或货币的默认区域设置的类别。
         */
        FORMAT("user.language.format",
               "user.script.format",
               "user.country.format",
               "user.variant.format");

        Category(String languageKey, String scriptKey, String countryKey, String variantKey) {
            this.languageKey = languageKey;
            this.scriptKey = scriptKey;
            this.countryKey = countryKey;
            this.variantKey = variantKey;
        }

        final String languageKey;
        final String scriptKey;
        final String countryKey;
        final String variantKey;
    }

    /**
     * <code>Builder</code> 用于从由设置器配置的值构建 <code>Locale</code> 实例。与 <code>Locale</code> 构造函数不同，
     * <code>Builder</code> 检查由设置器配置的值是否满足 <code>Locale</code> 类定义的语法要求。由 <code>Builder</code>
     * 创建的 <code>Locale</code> 对象是格式良好的，并且可以转换为格式良好的 IETF BCP 47 语言标签而不会丢失信息。
     *
     * <p><b>注意：</b> <code>Locale</code> 类不对变体提供任何语法限制，而 BCP 47 要求每个变体子标签为 5 到 8 个字母数字或一个数字后跟 3 个字母数字。
     * <code>setVariant</code> 方法对于不满足此限制的变体将抛出 <code>IllformedLocaleException</code>。如果需要支持此类变体，请使用 <code>Locale</code> 构造函数。
     * 但是，请记住，以这种方式创建的 <code>Locale</code> 对象在转换为 BCP 47 语言标签时可能会丢失变体信息。
     *
     * <p>以下示例展示了如何使用 <code>Builder</code> 创建 <code>Locale</code> 对象
     * <blockquote>
     * <pre>
     *     Locale aLocale = new Builder().setLanguage("sr").setScript("Latn").setRegion("RS").build();
     * </pre>
     * </blockquote>
     *
     * <p>构建器可以重用；<code>clear()</code> 将所有字段重置为其默认值。
     *
     * @see Locale#forLanguageTag
     * @since 1.7
     */
    public static final class Builder {
        private final InternalLocaleBuilder localeBuilder;

        /**
         * 构造一个空的构建器。所有字段、扩展和私有使用信息的默认值为空字符串。
         */
        public Builder() {
            localeBuilder = new InternalLocaleBuilder();
        }

        /**
         * 重置 <code>Builder</code> 以匹配提供的 <code>locale</code>。现有状态将被丢弃。
         *
         * <p>区域设置的所有字段必须是格式良好的，参见 {@link Locale}。
         *
         * <p>具有任何格式不良字段的区域设置将导致
         * <code>IllformedLocaleException</code> 被抛出，但以下三种情况除外，为了兼容性原因被接受：<ul>
         * <li>Locale("ja", "JP", "JP") 被视为 "ja-JP-u-ca-japanese"
         * <li>Locale("th", "TH", "TH") 被视为 "th-TH-u-nu-thai"
         * <li>Locale("no", "NO", "NY") 被视为 "nn-NO"</ul>
         *
         * @param locale 区域设置
         * @return 该构建器。
         * @throws IllformedLocaleException 如果 <code>locale</code> 有任何格式不良的字段。
         * @throws NullPointerException 如果 <code>locale</code> 为 null。
         */
        public Builder setLocale(Locale locale) {
            try {
                localeBuilder.setLocale(locale.baseLocale, locale.localeExtensions);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }


                    /**
         * 将构建器重置为提供的 IETF BCP 47
         * 语言标签的匹配项。丢弃现有状态。空字符串和 null 会导致构建器重置，类似于 {@link
         * #clear}。祖父标签（参见 {@link
         * Locale#forLanguageTag}）在处理前转换为其规范形式。否则，语言标签必须是格式良好的（参见 {@link Locale}），否则将抛出异常（与 <code>Locale.forLanguageTag</code> 不同，后者只是丢弃格式不良和后续部分的标签）。
         *
         * @param languageTag 语言标签
         * @return 此构建器。
         * @throws IllformedLocaleException 如果 <code>languageTag</code> 格式不良
         * @see Locale#forLanguageTag(String)
         */
        public Builder setLanguageTag(String languageTag) {
            ParseStatus sts = new ParseStatus();
            LanguageTag tag = LanguageTag.parse(languageTag, sts);
            if (sts.isError()) {
                throw new IllformedLocaleException(sts.getErrorMessage(), sts.getErrorIndex());
            }
            localeBuilder.setLanguageTag(tag);
            return this;
        }

        /**
         * 设置语言。如果 <code>language</code> 是空字符串或
         * null，则从构建器中移除语言。否则，
         * 语言必须是 <a href="./Locale.html#def_language">格式良好的</a>
         * 否则将抛出异常。
         *
         * <p>典型的语言值是 ISO639 定义的两到三个字母的语言代码。
         *
         * @param language 语言
         * @return 此构建器。
         * @throws IllformedLocaleException 如果 <code>language</code> 格式不良
         */
        public Builder setLanguage(String language) {
            try {
                localeBuilder.setLanguage(language);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * 设置脚本。如果 <code>script</code> 是 null 或空字符串，
         * 则从构建器中移除脚本。否则，脚本必须是 <a href="./Locale.html#def_script">格式良好的</a> 或
         * 将抛出异常。
         *
         * <p>典型的脚本值是 ISO 15924 定义的四个字母的脚本代码。
         *
         * @param script 脚本
         * @return 此构建器。
         * @throws IllformedLocaleException 如果 <code>script</code> 格式不良
         */
        public Builder setScript(String script) {
            try {
                localeBuilder.setScript(script);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * 设置区域。如果区域是 null 或空字符串，则从构建器中移除区域。否则，
         * 区域必须是 <a href="./Locale.html#def_region">格式良好的</a> 或将抛出异常。
         *
         * <p>典型的区域值是 ISO 3166 定义的两个字母的代码或
         * 三个数字的 UN M.49 区域代码。
         *
         * <p>通过 <code>Builder</code> 创建的 <code>Locale</code> 中的国家值
         * 始终被规范化为大写。
         *
         * @param region 区域
         * @return 此构建器。
         * @throws IllformedLocaleException 如果 <code>region</code> 格式不良
         */
        public Builder setRegion(String region) {
            try {
                localeBuilder.setRegion(region);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * 设置变体。如果变体是 null 或空字符串，则从构建器中移除变体。否则，它
         * 必须由一个或多个 <a href="./Locale.html#def_variant">格式良好的</a>
         * 子标签组成，否则将抛出异常。
         *
         * <p><b>注意：</b>此方法检查 <code>variant</code>
         * 是否满足 IETF BCP 47 变体子标签的语法要求，
         * 并将值规范化为小写字母。然而，
         * <code>Locale</code> 类不对变体施加任何语法限制，
         * <code>Locale</code> 中的变体值是区分大小写的。要设置这样的变体，
         * 请使用 <code>Locale</code> 构造函数。
         *
         * @param variant 变体
         * @return 此构建器。
         * @throws IllformedLocaleException 如果 <code>variant</code> 格式不良
         */
        public Builder setVariant(String variant) {
            try {
                localeBuilder.setVariant(variant);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * 设置给定键的扩展。如果值是 null 或空字符串，则移除扩展。否则，扩展
         * 必须是 <a href="./Locale.html#def_extensions">格式良好的</a> 或将抛出异常。
         *
         * <p><b>注意：</b>键 {@link Locale#UNICODE_LOCALE_EXTENSION
         * UNICODE_LOCALE_EXTENSION} ('u') 用于 Unicode 语言扩展。
         * 为该键设置值将替换任何现有的 Unicode 语言键/类型对，以扩展中定义的为准。
         *
         * <p><b>注意：</b>键 {@link Locale#PRIVATE_USE_EXTENSION
         * PRIVATE_USE_EXTENSION} ('x') 用于私有使用代码。为了格式良好，该键的值只需要有一个到八个字母数字字符的子标签，而不是通常情况下的两个到八个。
         *
         * @param key 扩展键
         * @param value 扩展值
         * @return 此构建器。
         * @throws IllformedLocaleException 如果 <code>key</code> 非法
         * 或 <code>value</code> 格式不良
         * @see #setUnicodeLocaleKeyword(String, String)
         */
        public Builder setExtension(char key, String value) {
            try {
                localeBuilder.setExtension(key, value);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * 设置给定键的 Unicode 语言关键字类型。如果类型为 null，则移除 Unicode 关键字。否则，键必须
         * 非 null 且键和类型都必须是 <a
         * href="./Locale.html#def_locale_extension">格式良好的</a> 或将抛出异常。
         *
         * <p>键和类型将转换为小写。
         *
         * <p><b>注意：</b>通过 {@link #setExtension}
         * 设置 'u' 扩展将替换所有 Unicode 语言关键字，以扩展中定义的为准。
         *
         * @param key Unicode 语言键
         * @param type Unicode 语言类型
         * @return 此构建器。
         * @throws IllformedLocaleException 如果 <code>key</code> 或 <code>type</code>
         * 格式不良
         * @throws NullPointerException 如果 <code>key</code> 为 null
         * @see #setExtension(char, String)
         */
        public Builder setUnicodeLocaleKeyword(String key, String type) {
            try {
                localeBuilder.setUnicodeLocaleKeyword(key, type);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * 如果尚未存在，则添加一个 Unicode 语言属性，否则
         * 没有任何效果。属性不能为空且必须是 <a
         * href="./Locale.html#def_locale_extension">格式良好的</a> 或将抛出异常。
         *
         * @param attribute 属性
         * @return 此构建器。
         * @throws NullPointerException 如果 <code>attribute</code> 为 null
         * @throws IllformedLocaleException 如果 <code>attribute</code> 格式不良
         * @see #setExtension(char, String)
         */
        public Builder addUnicodeLocaleAttribute(String attribute) {
            try {
                localeBuilder.addUnicodeLocaleAttribute(attribute);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * 如果存在，则移除一个 Unicode 语言属性，否则没有
         * 任何效果。属性不能为空且必须是 <a
         * href="./Locale.html#def_locale_extension">格式良好的</a> 或将抛出异常。
         *
         * <p>移除属性时的比较不区分大小写。
         *
         * @param attribute 属性
         * @return 此构建器。
         * @throws NullPointerException 如果 <code>attribute</code> 为 null
         * @throws IllformedLocaleException 如果 <code>attribute</code> 格式不良
         * @see #setExtension(char, String)
         */
        public Builder removeUnicodeLocaleAttribute(String attribute) {
            try {
                localeBuilder.removeUnicodeLocaleAttribute(attribute);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * 将构建器重置为其初始的空状态。
         *
         * @return 此构建器。
         */
        public Builder clear() {
            localeBuilder.clear();
            return this;
        }

        /**
         * 将扩展重置为其初始的空状态。
         * 语言、脚本、区域和变体保持不变。
         *
         * @return 此构建器。
         * @see #setExtension(char, String)
         */
        public Builder clearExtensions() {
            localeBuilder.clearExtensions();
            return this;
        }

        /**
         * 返回从构建器设置的字段创建的 <code>Locale</code> 实例。
         *
         * <p>这将应用 {@link Locale#forLanguageTag} 中列出的转换
         * 以构建 Locale。（祖父标签在 {@link #setLanguageTag} 中处理。）
         *
         * @return 一个 Locale。
         */
        public Locale build() {
            BaseLocale baseloc = localeBuilder.getBaseLocale();
            LocaleExtensions extensions = localeBuilder.getLocaleExtensions();
            if (extensions == null && baseloc.getVariant().length() > 0) {
                extensions = getCompatibilityExtensions(baseloc.getLanguage(), baseloc.getScript(),
                        baseloc.getRegion(), baseloc.getVariant());
            }
            return Locale.getInstance(baseloc, extensions);
        }
    }

    /**
     * 此枚举提供常量以选择语言匹配的过滤模式。请参阅 <a href="http://tools.ietf.org/html/rfc4647">RFC 4647
     * 语言标签匹配</a> 以获取详细信息。
     *
     * <p>例如，考虑两个每个只包含一个语言范围和一组后续语言标签的语言优先级列表：
     *
     * <pre>
     *    de (德语)
     *    de-DE (德语，德国)
     *    de-Deva (德语，天城文脚本)
     *    de-Deva-DE (德语，天城文脚本，德国)
     *    de-DE-1996 (德语，德国，1996 年正字法)
     *    de-Latn-DE (德语，拉丁文脚本，德国)
     *    de-Latn-DE-1996 (德语，拉丁文脚本，德国，1996 年正字法)
     * </pre>
     *
     * 过滤方法将表现如下：
     *
     * <table cellpadding=2 summary="过滤方法行为">
     * <tr>
     * <th>过滤模式</th>
     * <th>语言优先级列表: {@code "de-DE"}</th>
     * <th>语言优先级列表: {@code "de-*-DE"}</th>
     * </tr>
     * <tr>
     * <td valign=top>
     * {@link FilteringMode#AUTOSELECT_FILTERING AUTOSELECT_FILTERING}
     * </td>
     * <td valign=top>
     * 执行 <em>基本</em> 过滤并返回 {@code "de-DE"} 和
     * {@code "de-DE-1996"}。
     * </td>
     * <td valign=top>
     * 执行 <em>扩展</em> 过滤并返回 {@code "de-DE"},
     * {@code "de-Deva-DE"}, {@code "de-DE-1996"}, {@code "de-Latn-DE"}, 和
     * {@code "de-Latn-DE-1996"}。
     * </td>
     * </tr>
     * <tr>
     * <td valign=top>
     * {@link FilteringMode#EXTENDED_FILTERING EXTENDED_FILTERING}
     * </td>
     * <td valign=top>
     * 执行 <em>扩展</em> 过滤并返回 {@code "de-DE"},
     * {@code "de-Deva-DE"}, {@code "de-DE-1996"}, {@code "de-Latn-DE"}, 和
     * {@code "de-Latn-DE-1996"}。
     * </td>
     * <td valign=top>同上。</td>
     * </tr>
     * <tr>
     * <td valign=top>
     * {@link FilteringMode#IGNORE_EXTENDED_RANGES IGNORE_EXTENDED_RANGES}
     * </td>
     * <td valign=top>
     * 执行 <em>基本</em> 过滤并返回 {@code "de-DE"} 和
     * {@code "de-DE-1996"}。
     * </td>
     * <td valign=top>
     * 执行 <em>基本</em> 过滤并返回 {@code null}，因为没有匹配项。
     * </td>
     * </tr>
     * <tr>
     * <td valign=top>
     * {@link FilteringMode#MAP_EXTENDED_RANGES MAP_EXTENDED_RANGES}
     * </td>
     * <td valign=top>同上。</td>
     * <td valign=top>
     * 执行 <em>基本</em> 过滤并返回 {@code "de-DE"} 和
     * {@code "de-DE-1996"}，因为 {@code "de-*-DE"} 被映射为
     * {@code "de-DE"}。
     * </td>
     * </tr>
     * <tr>
     * <td valign=top>
     * {@link FilteringMode#REJECT_EXTENDED_RANGES REJECT_EXTENDED_RANGES}
     * </td>
     * <td valign=top>同上。</td>
     * <td valign=top>
     * 抛出 {@link IllegalArgumentException}，因为 {@code "de-*-DE"} 不是有效的基本语言范围。
     * </td>
     * </tr>
     * </table>
     *
     * @see #filter(List, Collection, FilteringMode)
     * @see #filterTags(List, Collection, FilteringMode)
     *
     * @since 1.8
     */
    public static enum FilteringMode {
        /**
         * 指定基于给定语言优先级列表中的语言范围的自动过滤模式。如果所有范围都是基本的，则选择基本过滤。否则，选择扩展过滤。
         */
        AUTOSELECT_FILTERING,

        /**
         * 指定扩展过滤。
         */
        EXTENDED_FILTERING,

        /**
         * 指定基本过滤：注意，给定语言优先级列表中包含的任何扩展语言范围将被忽略。
         */
        IGNORE_EXTENDED_RANGES,


        /**
         * 指定基本过滤：如果给定的语言优先列表中包含任何扩展语言范围，则它们将映射到基本语言范围。具体来说，以
         * 子标签 {@code "*"} 开头的语言范围被视为语言范围 {@code "*"}. 例如，{@code "*-US"} 被视为 {@code "*"}.
         * 如果 {@code "*"} 不是第一个子标签，则 {@code "*"} 和额外的 {@code "-"} 将被移除。例如，{@code "ja-*-JP"}
         * 将映射为 {@code "ja-JP"}.
         */
        MAP_EXTENDED_RANGES,

        /**
         * 指定基本过滤：如果给定的语言优先列表中包含任何扩展语言范围，则列表将被拒绝，过滤方法将抛出
         * {@link IllegalArgumentException}。
         */
        REJECT_EXTENDED_RANGES
    };

    /**
     * 该类表示 <em>语言范围</em>，定义在
     * <a href="http://tools.ietf.org/html/rfc4647">RFC 4647 匹配语言标签</a> 中。语言范围是用于选择满足特定要求的语言标签的标识符，使用
     * <a href="Locale.html#LocaleMatching">区域设置匹配</a> 中描述的机制。表示用户偏好的语言范围列表称为 <em>语言优先列表</em>。
     *
     * <p>有两种类型的语言范围：基本和扩展。在 RFC 4647 中，语言范围的语法以
     * <a href="http://tools.ietf.org/html/rfc4234">ABNF</a> 表达如下：
     * <blockquote>
     * <pre>
     *     basic-language-range    = (1*8ALPHA *("-" 1*8alphanum)) / "*"
     *     extended-language-range = (1*8ALPHA / "*")
     *                               *("-" (1*8alphanum / "*"))
     *     alphanum                = ALPHA / DIGIT
     * </pre>
     * </blockquote>
     * 例如，{@code "en"}（英语），{@code "ja-JP"}（日语，日本），{@code "*"}（匹配任何语言标签的特殊语言范围）是基本语言范围，而
     * {@code "*-CH"}（瑞士的任何语言），{@code "es-*"}（任何地区的西班牙语），和 {@code "zh-Hant-*"}（任何地区的繁体中文）是扩展语言范围。
     *
     * @see #filter
     * @see #filterTags
     * @see #lookup
     * @see #lookupTag
     *
     * @since 1.8
     */
    public static final class LanguageRange {

       /**
        * 一个常量，保存权重的最大值 1.0，表示该语言范围非常适合用户。
        */
        public static final double MAX_WEIGHT = 1.0;

       /**
        * 一个常量，保存权重的最小值 0.0，表示该语言范围不适合用户。
        */
        public static final double MIN_WEIGHT = 0.0;

        private final String range;
        private final double weight;

        private volatile int hash = 0;

        /**
         * 使用给定的 {@code range} 构造一个 {@code LanguageRange}。注意，在构造时不会对 IANA 语言子标签注册表进行验证。
         *
         * <p>这等同于 {@code LanguageRange(range, MAX_WEIGHT)}。
         *
         * @param range 一个语言范围
         * @throws NullPointerException 如果给定的 {@code range} 是 {@code null}
         */
        public LanguageRange(String range) {
            this(range, MAX_WEIGHT);
        }

        /**
         * 使用给定的 {@code range} 和 {@code weight} 构造一个 {@code LanguageRange}。注意，在构造时不会对 IANA
         * 语言子标签注册表进行验证。
         *
         * @param range 一个语言范围
         * @param weight 介于 {@code MIN_WEIGHT} 和 {@code MAX_WEIGHT} 之间的权重值
         * @throws NullPointerException 如果给定的 {@code range} 是 {@code null}
         * @throws IllegalArgumentException 如果给定的 {@code weight} 小于 {@code MIN_WEIGHT} 或大于 {@code MAX_WEIGHT}
         */
        public LanguageRange(String range, double weight) {
            if (range == null) {
                throw new NullPointerException();
            }
            if (weight < MIN_WEIGHT || weight > MAX_WEIGHT) {
                throw new IllegalArgumentException("weight=" + weight);
            }

            range = range.toLowerCase();

            // 进行语法检查。
            boolean isIllFormed = false;
            String[] subtags = range.split("-");
            if (isSubtagIllFormed(subtags[0], true)
                || range.endsWith("-")) {
                isIllFormed = true;
            } else {
                for (int i = 1; i < subtags.length; i++) {
                    if (isSubtagIllFormed(subtags[i], false)) {
                        isIllFormed = true;
                        break;
                    }
                }
            }
            if (isIllFormed) {
                throw new IllegalArgumentException("range=" + range);
            }

            this.range = range;
            this.weight = weight;
        }

        private static boolean isSubtagIllFormed(String subtag,
                                                 boolean isFirstSubtag) {
            if (subtag.equals("") || subtag.length() > 8) {
                return true;
            } else if (subtag.equals("*")) {
                return false;
            }
            char[] charArray = subtag.toCharArray();
            if (isFirstSubtag) { // ALPHA
                for (char c : charArray) {
                    if (c < 'a' || c > 'z') {
                        return true;
                    }
                }
            } else { // ALPHA / DIGIT
                for (char c : charArray) {
                    if (c < '0' || (c > '9' && c < 'a') || c > 'z') {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * 返回此 {@code LanguageRange} 的语言范围。
         *
         * @return 语言范围。
         */
        public String getRange() {
            return range;
        }

        /**
         * 返回此 {@code LanguageRange} 的权重。
         *
         * @return 权重值。
         */
        public double getWeight() {
            return weight;
        }

        /**
         * 解析给定的 {@code ranges} 以生成一个语言优先列表。
         *
         * <p>此方法对给定的 {@code ranges} 中的每个语言范围进行语法检查，但不会使用 IANA 语言子标签注册表进行验证。
         *
         * <p>可以给定的 {@code ranges} 可以采用以下形式之一：
         *
         * <pre>
         *   "Accept-Language: ja,en;q=0.4"  (带有 Accept-Language 前缀的加权列表)
         *   "ja,en;q=0.4"                   (加权列表)
         *   "ja,en"                         (优先列表)
         * </pre>
         *
         * 在加权列表中，每个语言范围都有一个权重值。权重值等同于
         * <a href="http://tools.ietf.org/html/rfc2616">RFC 2616</a> 中的“质量值”，表示用户对语言的偏好程度。权重值在相应的语言范围后跟
         * {@code ";q="} 指定，默认权重值为 {@code MAX_WEIGHT}（当省略时）。
         *
         * <p>与加权列表不同，优先列表中的语言范围按优先级降序排序。第一个语言范围具有最高优先级，最符合用户的偏好。
         *
         * <p>无论哪种情况，语言优先列表中的语言范围都按优先级或权重降序排序。如果给定的 {@code ranges} 中某个语言范围出现多次，
         * 只有第一个会被包含在语言优先列表中。
         *
         * <p>返回的列表由给定的 {@code ranges} 中的语言范围及其在 IANA 语言子标签注册表中找到的等效语言范围组成。例如，如果给定的
         * {@code ranges} 是 {@code "Accept-Language: iw,en-us;q=0.7,en;q=0.3"}，则返回列表中的元素为：
         *
         * <pre>
         *  <b>Range</b>                                   <b>Weight</b>
         *    "iw" (希伯来语的旧标签)             1.0
         *    "he" (希伯来语的新首选代码)    1.0
         *    "en-us" (美国英语)                0.7
         *    "en" (英语)                          0.3
         * </pre>
         *
         * 两个语言范围，{@code "iw"} 和 {@code "he"}，在列表中具有相同的最高优先级。通过将 {@code "he"} 添加到用户的语言优先列表中，
         * 区域设置匹配方法可以找到希伯来语作为匹配的区域设置（或语言标签），即使应用程序或系统仅提供 {@code "he"} 作为支持的区域设置（或语言标签）。
         *
         * @param ranges 由逗号分隔的语言范围列表，或以
         *     <a href="http://tools.ietf.org/html/rfc2616">RFC 2616</a> 中定义的“Accept-Language”头形式的语言范围列表
         * @return 由给定的 {@code ranges} 中包含的语言范围及其等效语言范围（如果可用）组成的语言优先列表。列表是可修改的。
         * @throws NullPointerException 如果 {@code ranges} 为 null
         * @throws IllegalArgumentException 如果在给定的 {@code ranges} 中找到的语言范围或权重格式不正确
         */
        public static List<LanguageRange> parse(String ranges) {
            return LocaleMatcher.parse(ranges);
        }

        /**
         * 解析给定的 {@code ranges} 以生成一个语言优先列表，然后使用给定的 {@code map} 自定义该列表。此方法等同于
         * {@code mapEquivalents(parse(ranges), map)}。
         *
         * @param ranges 由逗号分隔的语言范围列表，或以
         *     <a href="http://tools.ietf.org/html/rfc2616">RFC 2616</a> 中定义的“Accept-Language”头形式的语言范围列表
         * @param map 包含用于自定义语言范围的信息的映射
         * @return 自定义后的语言优先列表。列表是可修改的。
         * @throws NullPointerException 如果 {@code ranges} 为 null
         * @throws IllegalArgumentException 如果在给定的 {@code ranges} 中找到的语言范围或权重格式不正确
         * @see #parse(String)
         * @see #mapEquivalents
         */
        public static List<LanguageRange> parse(String ranges,
                                                Map<String, List<String>> map) {
            return mapEquivalents(parse(ranges), map);
        }

        /**
         * 使用给定的 {@code priorityList} 和 {@code map} 生成一个新的自定义语言优先列表。如果给定的 {@code map} 为空，
         * 此方法将返回给定的 {@code priorityList} 的副本。
         *
         * <p>在映射中，键表示一个语言范围，而值是其等效语言范围的列表。映射中不能使用 {@code '*'}。每个等效语言范围具有与其
         * 原始语言范围相同的权重值。
         *
         * <pre>
         *  映射的一个示例：
         *    <b>Key</b>                            <b>Value</b>
         *      "zh" (中文)                 "zh",
         *                                     "zh-Hans"(简体中文)
         *      "zh-HK" (中文，香港)   "zh-HK"
         *      "zh-TW" (中文，台湾)      "zh-TW"
         * </pre>
         *
         * 自定义在使用 IANA 语言子标签注册表进行修改后执行。
         *
         * <p>例如，如果用户的语言优先列表由五个语言范围组成（{@code "zh"}，{@code "zh-CN"}，{@code "en"}，
         * {@code "zh-TW"} 和 {@code "zh-HK"}），使用上述映射示例自定义后生成的新语言优先列表将由 {@code "zh"}，
         * {@code "zh-Hans"}，{@code "zh-CN"}，{@code "zh-Hans-CN"}，{@code "en"}，{@code "zh-TW"} 和
         * {@code "zh-HK"} 组成。
         *
         * <p>即使 {@code "zh-HK"} 和 {@code "zh-TW"} 包含在语言优先列表中，它们也不会转换为 {@code "zh-Hans-HK"} 或
         * {@code "zh-Hans-TW"}。在这个示例中，映射用于明确区分简体中文和繁体中文。
         *
         * <p>如果映射中不包含 {@code "zh"} 到 {@code "zh"} 的映射，将执行简单的替换，自定义列表将不包括 {@code "zh"} 和
         * {@code "zh-CN"}。
         *
         * @param priorityList 用户的语言优先列表
         * @param map 包含用于自定义语言范围的信息的映射
         * @return 自定义后的新的语言优先列表。列表是可修改的。
         * @throws NullPointerException 如果 {@code priorityList} 为 {@code null}
         * @see #parse(String, Map)
         */
        public static List<LanguageRange> mapEquivalents(
                                              List<LanguageRange> priorityList,
                                              Map<String, List<String>> map) {
            return LocaleMatcher.mapEquivalents(priorityList, map);
        }

        /**
         * 返回此对象的哈希码值。
         *
         * @return 此对象的哈希码值。
         */
        @Override
        public int hashCode() {
            if (hash == 0) {
                int result = 17;
                result = 37 * result + range.hashCode();
                long bitsWeight = Double.doubleToLongBits(weight);
                result = 37 * result + (int) (bitsWeight ^ (bitsWeight >>> 32));
                hash = result;
            }
            return hash;
        }

        /**
         * 将此对象与指定的对象进行比较。结果为 true 当且仅当参数不为 {@code null} 且是一个包含与该对象相同的 {@code range}
         * 和 {@code weight} 值的 {@code LanguageRange} 对象。
         *
         * @param obj 要比较的对象
         * @return 如果此对象的 {@code range} 和 {@code weight} 与 {@code obj} 的相同，则返回 {@code true}；否则返回 {@code false}。
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LanguageRange)) {
                return false;
            }
            LanguageRange other = (LanguageRange) obj;
            return hash == other.hash
                   && range.equals(other.range)
                   && weight == other.weight;
        }
    }


    /**
     * 返回使用 RFC 4647 中定义的过滤机制匹配的 {@code Locale} 实例列表。
     *
     * @param priorityList 用户的语言优先级列表，其中每个语言标签按优先级或权重降序排列
     * @param locales 用于匹配的 {@code Locale} 实例
     * @param mode 过滤模式
     * @return 一个按优先级或权重降序排列的匹配语言标签的 {@code Locale} 实例列表，如果没有匹配项则返回空列表。该列表是可修改的。
     * @throws NullPointerException 如果 {@code priorityList} 或 {@code locales} 为 {@code null}
     * @throws IllegalArgumentException 如果在指定 {@link FilteringMode#REJECT_EXTENDED_RANGES} 时，给定列表中包含一个或多个扩展语言范围
     *
     * @since 1.8
     */
    public static List<Locale> filter(List<LanguageRange> priorityList,
                                      Collection<Locale> locales,
                                      FilteringMode mode) {
        return LocaleMatcher.filter(priorityList, locales, mode);
    }

    /**
     * 返回使用 RFC 4647 中定义的过滤机制匹配的 {@code Locale} 实例列表。这等同于
     * {@link #filter(List, Collection, FilteringMode)} 当 {@code mode} 为
     * {@link FilteringMode#AUTOSELECT_FILTERING} 时。
     *
     * @param priorityList 用户的语言优先级列表，其中每个语言标签按优先级或权重降序排列
     * @param locales 用于匹配的 {@code Locale} 实例
     * @return 一个按优先级或权重降序排列的匹配语言标签的 {@code Locale} 实例列表，如果没有匹配项则返回空列表。该列表是可修改的。
     * @throws NullPointerException 如果 {@code priorityList} 或 {@code locales} 为 {@code null}
     *
     * @since 1.8
     */
    public static List<Locale> filter(List<LanguageRange> priorityList,
                                      Collection<Locale> locales) {
        return filter(priorityList, locales, FilteringMode.AUTOSELECT_FILTERING);
    }

    /**
     * 返回使用 RFC 4647 中定义的基本过滤机制匹配的语言标签列表。
     *
     * @param priorityList 用户的语言优先级列表，其中每个语言标签按优先级或权重降序排列
     * @param tags 语言标签
     * @param mode 过滤模式
     * @return 一个按优先级或权重降序排列的匹配语言标签列表，如果没有匹配项则返回空列表。该列表是可修改的。
     * @throws NullPointerException 如果 {@code priorityList} 或 {@code tags} 为 {@code null}
     * @throws IllegalArgumentException 如果在指定 {@link FilteringMode#REJECT_EXTENDED_RANGES} 时，给定列表中包含一个或多个扩展语言范围
     *
     * @since 1.8
     */
    public static List<String> filterTags(List<LanguageRange> priorityList,
                                          Collection<String> tags,
                                          FilteringMode mode) {
        return LocaleMatcher.filterTags(priorityList, tags, mode);
    }

    /**
     * 返回使用 RFC 4647 中定义的基本过滤机制匹配的语言标签列表。这等同于
     * {@link #filterTags(List, Collection, FilteringMode)} 当 {@code mode} 为
     * {@link FilteringMode#AUTOSELECT_FILTERING} 时。
     *
     * @param priorityList 用户的语言优先级列表，其中每个语言标签按优先级或权重降序排列
     * @param tags 语言标签
     * @return 一个按优先级或权重降序排列的匹配语言标签列表，如果没有匹配项则返回空列表。该列表是可修改的。
     * @throws NullPointerException 如果 {@code priorityList} 或 {@code tags} 为 {@code null}
     *
     * @since 1.8
     */
    public static List<String> filterTags(List<LanguageRange> priorityList,
                                          Collection<String> tags) {
        return filterTags(priorityList, tags, FilteringMode.AUTOSELECT_FILTERING);
    }

    /**
     * 返回使用 RFC 4647 中定义的查找机制匹配的最佳语言标签的 {@code Locale} 实例。
     *
     * @param priorityList 用户的语言优先级列表，其中每个语言标签按优先级或权重降序排列
     * @param locales 用于匹配的 {@code Locale} 实例
     * @return 基于优先级或权重选择的最佳匹配的 <code>Locale</code> 实例，如果没有匹配项则返回 {@code null}。
     * @throws NullPointerException 如果 {@code priorityList} 或 {@code tags} 为 {@code null}
     *
     * @since 1.8
     */
    public static Locale lookup(List<LanguageRange> priorityList,
                                Collection<Locale> locales) {
        return LocaleMatcher.lookup(priorityList, locales);
    }

    /**
     * 返回使用 RFC 4647 中定义的查找机制匹配的最佳语言标签。
     *
     * @param priorityList 用户的语言优先级列表，其中每个语言标签按优先级或权重降序排列
     * @param tags 用于匹配的语言标签
     * @return 基于优先级或权重选择的最佳匹配的语言标签，如果没有匹配项则返回 {@code null}。
     * @throws NullPointerException 如果 {@code priorityList} 或 {@code tags} 为 {@code null}
     *
     * @since 1.8
     */
    public static String lookupTag(List<LanguageRange> priorityList,
                                   Collection<String> tags) {
        return LocaleMatcher.lookupTag(priorityList, tags);
    }

}
