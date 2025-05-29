/*
 * 版权所有 (c) 2012, 2020, Oracle 和/或其附属公司。保留所有权利。
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
 *
 *
 *
 *
 *
 * 版权所有 (c) 2007-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * 保留所有权利。
 *
 * 重新分发源代码和二进制形式，无论是否修改，前提是保留上述版权声明、
 * 本许可条件列表和以下免责声明。
 *
 * 重新分发二进制形式必须在随附的文档和/或其他材料中复制上述版权声明、
 * 本许可条件列表和以下免责声明。
 *
 * 未经特定事先书面许可，不得使用 JSR-310 或其贡献者的名字来支持或推广
 * 从本软件衍生的产品。
 *
 * 本软件由版权所有者和贡献者“按原样”提供，不提供任何明示或暗示的保证，
 * 包括但不限于适销性和特定用途适用性的暗示保证。在任何情况下，版权所有者或
 * 贡献者均不对任何直接、间接、偶然、特殊、示范性或后果性损害（包括但不限于
 * 采购替代商品或服务；使用、数据或利润损失；或业务中断）承担责任，无论是在合同、
 * 严格责任或侵权（包括疏忽或其他）理论下，即使已告知可能发生此类损害。
 */
package java.time;

import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

/**
 * 时区 ID，例如 {@code Europe/Paris}。
 * <p>
 * {@code ZoneId} 用于标识用于在 {@link Instant} 和 {@link LocalDateTime} 之间转换的规则。
 * 有两种不同类型的 ID：
 * <ul>
 * <li>固定偏移 - 从 UTC/Greenwich 完全解析的偏移，对于所有本地日期时间使用相同的偏移
 * <li>地理区域 - 一个特定的规则集适用的区域，用于确定从 UTC/Greenwich 的偏移
 * </ul>
 * 大多数固定偏移由 {@link ZoneOffset} 表示。
 * 调用任何 {@code ZoneId} 的 {@link #normalized()} 将确保固定偏移 ID 被表示为 {@code ZoneOffset}。
 * <p>
 * 实际的规则，描述偏移何时和如何变化，由 {@link ZoneRules} 定义。
 * 该类只是一个用于获取底层规则的 ID。
 * 采用这种方法是因为规则由政府定义且经常变化，而 ID 是稳定的。
 * <p>
 * 区分这一点还有其他效果。序列化 {@code ZoneId} 仅发送 ID，而序列化规则则发送整个数据集。
 * 同样，两个 ID 的比较仅检查 ID，而两个规则的比较则检查整个数据集。
 *
 * <h3>时区 ID</h3>
 * ID 在系统内是唯一的。
 * 有三种类型的 ID。
 * <p>
 * 最简单的 ID 类型来自 {@code ZoneOffset}。
 * 这包括 'Z' 和以 '+' 或 '-' 开头的 ID。
 * <p>
 * 下一种 ID 类型是带有某种前缀的偏移样式 ID，例如 'GMT+2' 或 'UTC+01:00'。
 * 认可的前缀是 'UTC'、'GMT' 和 'UT'。
 * 偏移是后缀，并将在创建期间进行规范化。
 * 这些 ID 可以使用 {@code normalized()} 规范化为 {@code ZoneOffset}。
 * <p>
 * 第三种类型的 ID 是基于区域的 ID。基于区域的 ID 必须有两个或更多字符，并且不能以 'UTC'、'GMT'、'UT'、'+' 或 '-' 开头。
 * 基于区域的 ID 由配置定义，参见 {@link ZoneRulesProvider}。
 * 配置的重点是提供从 ID 到底层 {@code ZoneRules} 的查找。
 * <p>
 * 时区规则由政府定义且经常变化。
 * 有一些组织，这里称为组，监控时区变化并汇总它们。
 * 默认组是 IANA 时区数据库 (TZDB)。
 * 其他组织包括 IATA（航空业机构）和 Microsoft。
 * <p>
 * 每个组定义其提供的区域 ID 的格式。
 * TZDB 组定义了如 'Europe/London' 或 'America/New_York' 的 ID。
 * TZDB ID 优先于其他组。
 * <p>
 * 强烈建议在所有非 TZDB 组提供的 ID 中包含组名，以避免冲突。例如，IATA 航空时区
 * 区域 ID 通常与机场的三字母代码相同。
 * 然而，乌得勒支机场的代码是 'UTC'，这显然存在冲突。
 * 非 TZDB 组的区域 ID 的推荐格式是 'group~region'。
 * 因此，如果定义了 IATA 数据，乌得勒支机场将是 'IATA~UTC'。
 *
 * <h3>序列化</h3>
 * 该类可以序列化，并在外部形式中存储字符串时区 ID。
 * {@code ZoneOffset} 子类使用仅存储 UTC/Greenwich 偏移的专用格式。
 * <p>
 * {@code ZoneId} 可以在 Java 运行时中反序列化，其中 ID 是未知的。
 * 例如，如果服务器端 Java 运行时已更新为包含新的时区 ID，但
 * 客户端 Java 运行时尚未更新。在这种情况下，{@code ZoneId}
 * 对象将存在，并且可以使用 {@code getId}、{@code equals}、
 * {@code hashCode}、{@code toString}、{@code getDisplayName} 和 {@code normalized} 进行查询。
 * 但是，任何对 {@code getRules} 的调用都会因 {@code ZoneRulesException} 而失败。
 * 该方法旨在允许在时区信息不完整的 Java 运行时中加载和查询 {@link ZonedDateTime}，但不能修改。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值</a>
 * 的类；对 {@code ZoneId} 实例使用身份敏感操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）可能会产生不可预测的结果，应避免。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 此抽象类有两个实现，这两个实现都是不可变且线程安全的。
 * 一个实现建模基于区域的 ID，另一个是 {@code ZoneOffset} 建模基于偏移的 ID。这种差异在序列化中可见。
 *
 * @since 1.8
 */
public abstract class ZoneId implements Serializable {


                /**
     * 一个区域覆盖映射，用于启用短时间区域名称的使用。
     * <p>
     * 短区域ID的使用已在 {@code java.util.TimeZone} 中被弃用。
     * 该映射允许通过 {@link #of(String, Map)} 工厂方法继续使用这些ID。
     * <p>
     * 该映射包含与TZDB 2005r及更高版本一致的ID映射，其中 'EST'、'MST' 和 'HST' 映射到不包括夏令时的ID。
     * <p>
     * 该映射如下：
     * <ul>
     * <li>EST - -05:00</li>
     * <li>HST - -10:00</li>
     * <li>MST - -07:00</li>
     * <li>ACT - Australia/Darwin</li>
     * <li>AET - Australia/Sydney</li>
     * <li>AGT - America/Argentina/Buenos_Aires</li>
     * <li>ART - Africa/Cairo</li>
     * <li>AST - America/Anchorage</li>
     * <li>BET - America/Sao_Paulo</li>
     * <li>BST - Asia/Dhaka</li>
     * <li>CAT - Africa/Harare</li>
     * <li>CNT - America/St_Johns</li>
     * <li>CST - America/Chicago</li>
     * <li>CTT - Asia/Shanghai</li>
     * <li>EAT - Africa/Addis_Ababa</li>
     * <li>ECT - Europe/Paris</li>
     * <li>IET - America/Indiana/Indianapolis</li>
     * <li>IST - Asia/Kolkata</li>
     * <li>JST - Asia/Tokyo</li>
     * <li>MIT - Pacific/Apia</li>
     * <li>NET - Asia/Yerevan</li>
     * <li>NST - Pacific/Auckland</li>
     * <li>PLT - Asia/Karachi</li>
     * <li>PNT - America/Phoenix</li>
     * <li>PRT - America/Puerto_Rico</li>
     * <li>PST - America/Los_Angeles</li>
     * <li>SST - Pacific/Guadalcanal</li>
     * <li>VST - Asia/Ho_Chi_Minh</li>
     * </ul>
     * 该映射是不可修改的。
     */
    public static final Map<String, String> SHORT_IDS;
    static {
        Map<String, String> map = new HashMap<>(64);
        map.put("ACT", "Australia/Darwin");
        map.put("AET", "Australia/Sydney");
        map.put("AGT", "America/Argentina/Buenos_Aires");
        map.put("ART", "Africa/Cairo");
        map.put("AST", "America/Anchorage");
        map.put("BET", "America/Sao_Paulo");
        map.put("BST", "Asia/Dhaka");
        map.put("CAT", "Africa/Harare");
        map.put("CNT", "America/St_Johns");
        map.put("CST", "America/Chicago");
        map.put("CTT", "Asia/Shanghai");
        map.put("EAT", "Africa/Addis_Ababa");
        map.put("ECT", "Europe/Paris");
        map.put("IET", "America/Indiana/Indianapolis");
        map.put("IST", "Asia/Kolkata");
        map.put("JST", "Asia/Tokyo");
        map.put("MIT", "Pacific/Apia");
        map.put("NET", "Asia/Yerevan");
        map.put("NST", "Pacific/Auckland");
        map.put("PLT", "Asia/Karachi");
        map.put("PNT", "America/Phoenix");
        map.put("PRT", "America/Puerto_Rico");
        map.put("PST", "America/Los_Angeles");
        map.put("SST", "Pacific/Guadalcanal");
        map.put("VST", "Asia/Ho_Chi_Minh");
        map.put("EST", "-05:00");
        map.put("MST", "-07:00");
        map.put("HST", "-10:00");
        SHORT_IDS = Collections.unmodifiableMap(map);
    }
    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 8352817235686L;

    //-----------------------------------------------------------------------
    /**
     * 获取系统默认时区。
     * <p>
     * 该方法查询 {@link TimeZone#getDefault()} 以找到默认时区，并将其转换为 {@code ZoneId}。
     * 如果系统默认时区发生更改，则该方法的结果也会更改。
     *
     * @return 时区ID，不为空
     * @throws DateTimeException 如果转换的时区ID格式无效
     * @throws ZoneRulesException 如果转换的时区区域ID无法找到
     */
    public static ZoneId systemDefault() {
        return TimeZone.getDefault().toZoneId();
    }

    /**
     * 获取可用的时区ID集。
     * <p>
     * 该集合包括所有可用的基于区域的ID的字符串形式。
     * 基于偏移量的时区ID不包含在返回的集合中。
     * 可以将ID传递给 {@link #of(String)} 以创建 {@code ZoneId}。
     * <p>
     * 时区ID集可以随着时间增加，尽管在典型应用中，ID集是固定的。
     * 每次调用此方法都是线程安全的。
     *
     * @return 时区ID集的可修改副本，不为空
     */
    public static Set<String> getAvailableZoneIds() {
        return ZoneRulesProvider.getAvailableZoneIds();
    }

    //-----------------------------------------------------------------------
    /**
     * 使用ID和别名映射获取 {@code ZoneId} 的实例。
     * <p>
     * 许多时区用户使用短缩写，例如 PST 表示 '太平洋标准时间'，PDT 表示 '太平洋夏令时'。
     * 这些缩写不是唯一的，因此不能用作ID。
     * 该方法允许在应用程序中设置和重用字符串到时区的映射。
     *
     * @param zoneId  时区ID，不为空
     * @param aliasMap  别名时区ID（通常是缩写）到实际时区ID的映射，不为空
     * @return 时区ID，不为空
     * @throws DateTimeException 如果时区ID格式无效
     * @throws ZoneRulesException 如果时区ID是无法找到的区域ID
     */
    public static ZoneId of(String zoneId, Map<String, String> aliasMap) {
        Objects.requireNonNull(zoneId, "zoneId");
        Objects.requireNonNull(aliasMap, "aliasMap");
        String id = aliasMap.get(zoneId);
        id = (id != null ? id : zoneId);
        return of(id);
    }

    /**
     * 从ID获取 {@code ZoneId} 的实例，确保ID有效且可用。
     * <p>
     * 该方法解析ID，生成 {@code ZoneId} 或 {@code ZoneOffset}。
     * 如果ID为 'Z'，或以 '+' 或 '-' 开头，则返回 {@code ZoneOffset}。
     * 结果将始终是一个有效的ID，可以从中获取 {@link ZoneRules}。
     * <p>
     * 解析过程按以下步骤匹配时区ID：
     * <ul>
     * <li>如果时区ID等于 'Z'，则结果为 {@code ZoneOffset.UTC}。
     * <li>如果时区ID仅由一个字母组成，则时区ID无效，将抛出 {@code DateTimeException}。
     * <li>如果时区ID以 '+' 或 '-' 开头，则使用 {@link ZoneOffset#of(String)} 解析ID为 {@code ZoneOffset}。
     * <li>如果时区ID等于 'GMT'、'UTC' 或 'UT'，则结果为具有相同ID和与 {@code ZoneOffset.UTC} 等效规则的 {@code ZoneId}。
     * <li>如果时区ID以 'UTC+'、'UTC-'、'GMT+'、'GMT-'、'UT+' 或 'UT-' 开头，则该ID是一个带有前缀的基于偏移量的ID。
     *     ID被拆分为两部分，前缀为两到三个字母，后缀以符号开头。
     *     后缀被解析为 {@link ZoneOffset#of(String) ZoneOffset}。
     *     结果将是一个具有指定UTC/GMT/UT前缀和 {@link ZoneOffset#getId()} 规范化偏移ID的 {@code ZoneId}。
     *     返回的 {@code ZoneId} 的规则将与解析的 {@code ZoneOffset} 等效。
     * <li>所有其他ID被解析为基于区域的时区ID。区域ID必须匹配正则表达式 <code>[A-Za-z][A-Za-z0-9~/._+-]+</code>，
     *     否则将抛出 {@code DateTimeException}。如果时区ID不在配置的ID集中，则抛出 {@code ZoneRulesException}。
     *     区域ID的详细格式取决于提供数据的组。默认的数据集由IANA时区数据库（TZDB）提供。
     *     这些区域ID的形式为 '{area}/{city}'，例如 'Europe/Paris' 或 'America/New_York'。
     *     这与大多数 {@link java.util.TimeZone} 的ID兼容。
     * </ul>
     *
     * @param zoneId  时区ID，不为空
     * @return 时区ID，不为空
     * @throws DateTimeException 如果时区ID格式无效
     * @throws ZoneRulesException 如果时区ID是无法找到的区域ID
     */
    public static ZoneId of(String zoneId) {
        return of(zoneId, true);
    }


                /**
     * 获取一个包装了偏移量的 {@code ZoneId} 实例。
     * <p>
     * 如果前缀是 "GMT"、"UTC" 或 "UT"，则返回带有前缀和非零偏移量的 {@code ZoneId}。
     * 如果前缀为空 {@code ""}，则返回 {@code ZoneOffset}。
     *
     * @param prefix  时区ID，不为空
     * @param offset  偏移量，不为空
     * @return 时区ID，不为空
     * @throws IllegalArgumentException 如果前缀不是 "GMT"、"UTC" 或 "UT"，或 ""
     */
    public static ZoneId ofOffset(String prefix, ZoneOffset offset) {
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(offset, "offset");
        if (prefix.isEmpty()) {
            return offset;
        }

        if (!prefix.equals("GMT") && !prefix.equals("UTC") && !prefix.equals("UT")) {
             throw new IllegalArgumentException("prefix should be GMT, UTC or UT, is: " + prefix);
        }

        if (offset.getTotalSeconds() != 0) {
            prefix = prefix.concat(offset.getId());
        }
        return new ZoneRegion(prefix, offset.getRules());
    }

    /**
     * 解析ID，根据标志指示是否应抛出 {@code ZoneRulesException}，用于反序列化。
     *
     * @param zoneId  时区ID，不为空
     * @param checkAvailable  是否检查时区ID是否可用
     * @return 时区ID，不为空
     * @throws DateTimeException 如果ID格式无效
     * @throws ZoneRulesException 如果检查可用性且找不到ID
     */
    static ZoneId of(String zoneId, boolean checkAvailable) {
        Objects.requireNonNull(zoneId, "zoneId");
        if (zoneId.length() <= 1 || zoneId.startsWith("+") || zoneId.startsWith("-")) {
            return ZoneOffset.of(zoneId);
        } else if (zoneId.startsWith("UTC") || zoneId.startsWith("GMT")) {
            return ofWithPrefix(zoneId, 3, checkAvailable);
        } else if (zoneId.startsWith("UT")) {
            return ofWithPrefix(zoneId, 2, checkAvailable);
        }
        return ZoneRegion.ofId(zoneId, checkAvailable);
    }

    /**
     * 在确定前缀后进行解析。
     *
     * @param zoneId  时区ID，不为空
     * @param prefixLength  前缀长度，2或3
     * @return 时区ID，不为空
     * @throws DateTimeException 如果时区ID格式无效
     */
    private static ZoneId ofWithPrefix(String zoneId, int prefixLength, boolean checkAvailable) {
        String prefix = zoneId.substring(0, prefixLength);
        if (zoneId.length() == prefixLength) {
            return ofOffset(prefix, ZoneOffset.UTC);
        }
        if (zoneId.charAt(prefixLength) != '+' && zoneId.charAt(prefixLength) != '-') {
            return ZoneRegion.ofId(zoneId, checkAvailable);  // 传递到 ZoneRulesProvider
        }
        try {
            ZoneOffset offset = ZoneOffset.of(zoneId.substring(prefixLength));
            if (offset == ZoneOffset.UTC) {
                return ofOffset(prefix, offset);
            }
            return ofOffset(prefix, offset);
        } catch (DateTimeException ex) {
            throw new DateTimeException("Invalid ID for offset-based ZoneId: " + zoneId, ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code ZoneId} 实例。
     * <p>
     * 根据指定的时间对象获取时区。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，此工厂将其转换为 {@code ZoneId} 的实例。
     * <p>
     * {@code TemporalAccessor} 表示一些形式的日期和时间信息。
     * 此工厂将任意的时间对象转换为 {@code ZoneId} 的实例。
     * <p>
     * 转换将尝试以优先使用基于区域的时区而非基于偏移量的时区的方式获取时区，使用 {@link TemporalQueries#zone()}。
     * <p>
     * 此方法匹配函数接口 {@link TemporalQuery} 的签名，允许通过方法引用作为查询使用，{@code ZoneId::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 时区ID，不为空
     * @throws DateTimeException 如果无法从 {@code TemporalAccessor} 转换为 {@code ZoneId}
     */
    public static ZoneId from(TemporalAccessor temporal) {
        ZoneId obj = temporal.query(TemporalQueries.zone());
        if (obj == null) {
            throw new DateTimeException("Unable to obtain ZoneId from TemporalAccessor: " +
                    temporal + " of type " + temporal.getClass().getName());
        }
        return obj;
    }

    //-----------------------------------------------------------------------
    /**
     * 仅在包内可访问的构造函数。
     */
    ZoneId() {
        if (getClass() != ZoneOffset.class && getClass() != ZoneRegion.class) {
            throw new AssertionError("Invalid subclass");
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 获取唯一的时区ID。
     * <p>
     * 此ID唯一定义此对象。
     * 基于偏移量的ID的格式由 {@link ZoneOffset#getId()} 定义。
     *
     * @return 时区唯一ID，不为空
     */
    public abstract String getId();

    //-----------------------------------------------------------------------
    /**
     * 获取时区的文本表示，例如 'British Time' 或 '+02:00'。
     * <p>
     * 这返回用于标识时区ID的文本名称，适合向用户显示。
     * 参数控制返回文本的样式和语言环境。
     * <p>
     * 如果找不到文本映射，则返回 {@link #getId() 完整ID}。
     *
     * @param style  所需文本的长度，不为空
     * @param locale  使用的语言环境，不为空
     * @return 时区的文本值，不为空
     */
    public String getDisplayName(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendZoneText(style).toFormatter(locale).format(toTemporal());
    }

                /**
     * 将此区域转换为 {@code TemporalAccessor}。
     * <p>
     * 一个 {@code ZoneId} 可以完全表示为一个 {@code TemporalAccessor}。
     * 然而，此接口未由此类实现，因为接口上的大多数方法对 {@code ZoneId} 没有意义。
     * <p>
     * 返回的临时对象没有支持的字段，查询方法支持使用 {@link TemporalQueries#zoneId()} 返回区域。
     *
     * @return 与此区域等效的临时对象，不为空
     */
    private TemporalAccessor toTemporal() {
        return new TemporalAccessor() {
            @Override
            public boolean isSupported(TemporalField field) {
                return false;
            }
            @Override
            public long getLong(TemporalField field) {
                throw new UnsupportedTemporalTypeException("不支持的字段: " + field);
            }
            @SuppressWarnings("unchecked")
            @Override
            public <R> R query(TemporalQuery<R> query) {
                if (query == TemporalQueries.zoneId()) {
                    return (R) ZoneId.this;
                }
                return TemporalAccessor.super.query(query);
            }
        };
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此 ID 的时区规则，允许执行计算。
     * <p>
     * 规则提供了与时区相关的功能，
     * 例如查找给定瞬间或本地日期时间的偏移量。
     * <p>
     * 如果时区在没有加载相同规则的 Java 运行时中反序列化，
     * 则该时区可能无效。
     * 在这种情况下，调用此方法将抛出 {@code ZoneRulesException}。
     * <p>
     * 规则由 {@link ZoneRulesProvider} 提供。高级提供者可能支持在不重启 Java 运行时的情况下动态更新规则。
     * 如果是这样，那么此方法的结果可能会随时间变化。
     * 每个单独的调用仍然保持线程安全。
     * <p>
     * {@link ZoneOffset} 始终返回一组偏移量永不改变的规则。
     *
     * @return 规则，不为空
     * @throws ZoneRulesException 如果没有可用的规则
     */
    public abstract ZoneRules getRules();

    /**
     * 规范化时区 ID，尽可能返回 {@code ZoneOffset}。
     * <p>
     * 返回一个可以替代此 ID 的规范化 {@code ZoneId}。
     * 结果将具有与此对象返回的 {@code ZoneRules} 等效的规则，
     * 但是 {@code getId()} 返回的 ID 可能不同。
     * <p>
     * 规范化检查此 {@code ZoneId} 的规则是否具有固定偏移量。
     * 如果有，则返回等于该偏移量的 {@code ZoneOffset}。
     * 否则返回 {@code this}。
     *
     * @return 时区唯一 ID，不为空
     */
    public ZoneId normalized() {
        try {
            ZoneRules rules = getRules();
            if (rules.isFixedOffset()) {
                return rules.getOffset(Instant.EPOCH);
            }
        } catch (ZoneRulesException ex) {
            // 无效的 ZoneRegion 对此方法不重要
        }
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此时区 ID 是否等于另一个时区 ID。
     * <p>
     * 比较基于 ID。
     *
     * @param obj 要检查的对象，null 返回 false
     * @return 如果此对象等于其他时区 ID，则返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
           return true;
        }
        if (obj instanceof ZoneId) {
            ZoneId other = (ZoneId) obj;
            return getId().equals(other.getId());
        }
        return false;
    }

    /**
     * 此时区 ID 的哈希码。
     *
     * @return 适当的哈希码
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * 防止恶意流。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("通过序列化代理进行反序列化");
    }

    /**
     * 以 {@code String} 形式输出此区域，使用 ID。
     *
     * @return 此时区 ID 的字符串表示形式，不为空
     */
    @Override
    public String toString() {
        return getId();
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(7);  // 标识一个 ZoneId（不是 ZoneOffset）
     *  out.writeUTF(getId());
     * </pre>
     * <p>
     * 当读回时，{@code ZoneId} 将像使用 {@link #of(String)} 一样创建，
     * 但在 ID 格式有效但不在已知的基于区域的 ID 集合中时，不会抛出任何异常。
     *
     * @return {@code Ser} 的实例，不为空
     */
    // 这里是为了序列化 Javadoc
    private Object writeReplace() {
        return new Ser(Ser.ZONE_REGION_TYPE, this);
    }

    abstract void write(DataOutput out) throws IOException;

}
