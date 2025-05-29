/*
 * 版权所有 (c) 2012, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * 版权所有 (c) 2007-2012, Stephen Colebourne 和 Michael Nascimento Santos
 *
 * 保留所有权利。
 *
 * 重新分发源代码和二进制形式，无论是否修改，前提是保留上述版权声明，
 * 本许可条件列表和以下免责声明。
 *
 * 重新分发二进制形式必须在随附的文档和/或其他材料中复制上述版权声明，
 * 本许可条件列表和以下免责声明。
 *
 * 未经特定事先书面许可，不得使用 JSR-310 或其贡献者的名字来支持或推广
 * 从本软件衍生的产品。
 *
 * 本软件由版权所有者和贡献者“按原样”提供，不提供任何明示或暗示的保证，
 * 包括但不限于适销性和特定用途适用性的暗示保证。在任何情况下，版权所有者或
 * 贡献者均不对任何直接、间接、偶然、特殊、示范性或后果性损害（包括但不限于
 * 采购替代商品或服务；使用损失、数据丢失或利润损失；或业务中断）承担责任，
 * 无论是在合同、严格责任或侵权（包括疏忽或其他）理论下，即使已告知发生此类损害的可能性。
 */
package java.time;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.Objects;

/**
 * 一个应用相同时区规则的地理区域。
 * <p>
 * 时区信息被分类为定义 UTC/格林尼治偏移何时以及如何变化的一组规则。这些规则使用基于地理区域的标识符访问，
 * 例如国家或州。最常见的区域分类是时区数据库（TZDB），它定义了如 'Europe/Paris' 和 'Asia/Tokyo' 的区域。
 * <p>
 * 由本类建模的区域标识符与由 {@link ZoneRules} 建模的底层规则是不同的。
 * 规则由政府定义，经常发生变化。相比之下，区域标识符定义明确且长期存在。
 * 这种分离还允许在适当的情况下在区域之间共享规则。
 *
 * @implSpec
 * 本类是不可变的且线程安全的。
 *
 * @since 1.8
 */
final class ZoneRegion extends ZoneId implements Serializable {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 8386373296231747096L;
    /**
     * 时区ID，不为空。
     */
    private final String id;
    /**
     * 时区规则，如果区域ID是宽松加载的，则为null。
     */
    private final transient ZoneRules rules;

    /**
     * 从标识符获取 {@code ZoneId} 的实例。
     *
     * @param zoneId  时区ID，不为空
     * @param checkAvailable  是否检查区域ID是否可用
     * @return 时区ID，不为空
     * @throws DateTimeException 如果ID格式无效
     * @throws ZoneRulesException 如果检查可用性且找不到ID
     */
    static ZoneRegion ofId(String zoneId, boolean checkAvailable) {
        Objects.requireNonNull(zoneId, "zoneId");
        checkName(zoneId);
        ZoneRules rules = null;
        try {
            // 总是尝试加载以在反序列化后获得更好的行为
            rules = ZoneRulesProvider.getRules(zoneId, true);
        } catch (ZoneRulesException ex) {
            if (checkAvailable) {
                throw ex;
            }
        }
        return new ZoneRegion(zoneId, rules);
    }

    /**
     * 检查给定字符串是否为合法的 ZoneId 名称。
     *
     * @param zoneId  时区ID，不为空
     * @throws DateTimeException 如果ID格式无效
     */
    private static void checkName(String zoneId) {
        int n = zoneId.length();
        if (n < 2) {
           throw new DateTimeException("基于区域的 ZoneId 的无效ID，格式无效: " + zoneId);
        }
        for (int i = 0; i < n; i++) {
            char c = zoneId.charAt(i);
            if (c >= 'a' && c <= 'z') continue;
            if (c >= 'A' && c <= 'Z') continue;
            if (c == '/' && i != 0) continue;
            if (c >= '0' && c <= '9' && i != 0) continue;
            if (c == '~' && i != 0) continue;
            if (c == '.' && i != 0) continue;
            if (c == '_' && i != 0) continue;
            if (c == '+' && i != 0) continue;
            if (c == '-' && i != 0) continue;
            throw new DateTimeException("基于区域的 ZoneId 的无效ID，格式无效: " + zoneId);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * 构造函数。
     *
     * @param id  时区ID，不为空
     * @param rules  规则，null 表示延迟查找
     */
    ZoneRegion(String id, ZoneRules rules) {
        this.id = id;
        this.rules = rules;
    }

    //-----------------------------------------------------------------------
    @Override
    public String getId() {
        return id;
    }

    @Override
    public ZoneRules getRules() {
        // 当为null时，额外查询组提供者允许在 ZoneId 创建后提供者被更新的可能性
        return (rules != null ? rules : ZoneRulesProvider.getRules(id, false));
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(7);  // 识别 ZoneId（而非 ZoneOffset）
     *  out.writeUTF(zoneId);
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    private Object writeReplace() {
        return new Ser(Ser.ZONE_REGION_TYPE, this);
    }

    /**
     * 防御恶意流。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("通过序列化代理进行反序列化");
    }

    @Override
    void write(DataOutput out) throws IOException {
        out.writeByte(Ser.ZONE_REGION_TYPE);
        writeExternal(out);
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeUTF(id);
    }

    static ZoneId readExternal(DataInput in) throws IOException {
        String id = in.readUTF();
        return ZoneId.of(id, false);
    }

}
