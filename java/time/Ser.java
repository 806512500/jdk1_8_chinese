
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
 * 版权所有 (c) 2011-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * 保留所有权利。
 *
 * 重新分发源代码和二进制形式，无论是否修改，都必须保留上述版权声明，
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
 * 贡献者不对任何直接、间接、偶然、特殊、示范性或后果性损害（包括但不限于
 * 采购替代商品或服务；使用损失、数据损失或利润损失；或业务中断）负责，
 * 无论是在合同、严格责任或侵权（包括疏忽或其他）理论下，即使已告知有此类损害的可能性。
 */
package java.time;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;

/**
 * 本包的共享序列化代理。
 *
 * @implNote
 * 该类包装了被序列化的对象，并采用一个字节表示要序列化的类的类型。
 * 该字节也可用于版本化序列化格式。在这种情况下，另一个字节标志用于指定类型格式的替代版本。
 * 例如 {@code LOCAL_DATE_TYPE_VERSION_2 = 21}。
 * <p>
 * 为了序列化对象，它会写入其字节，然后回调到适当的类中执行序列化。
 * 为了反序列化对象，它会读取类型字节，然后切换以选择要回调的类。
 * <p>
 * 序列化格式是按类确定的。对于基于字段的类，每个字段都以适当的大小格式按字段大小降序写入。
 * 例如，在 {@link LocalDate} 中，年份在月份之前写入。复合类，如
 * {@link LocalDateTime} 作为单个对象进行序列化。
 * <p>
 * 该类是可变的，应在每次序列化时创建一次。
 *
 * @serial include
 * @since 1.8
 */
final class Ser implements Externalizable {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = -7683839454370182990L;

    static final byte DURATION_TYPE = 1;
    static final byte INSTANT_TYPE = 2;
    static final byte LOCAL_DATE_TYPE = 3;
    static final byte LOCAL_TIME_TYPE = 4;
    static final byte LOCAL_DATE_TIME_TYPE = 5;
    static final byte ZONE_DATE_TIME_TYPE = 6;
    static final byte ZONE_REGION_TYPE = 7;
    static final byte ZONE_OFFSET_TYPE = 8;
    static final byte OFFSET_TIME_TYPE = 9;
    static final byte OFFSET_DATE_TIME_TYPE = 10;
    static final byte YEAR_TYPE = 11;
    static final byte YEAR_MONTH_TYPE = 12;
    static final byte MONTH_DAY_TYPE = 13;
    static final byte PERIOD_TYPE = 14;

    /** 正在序列化的类型。 */
    private byte type;
    /** 正在序列化的对象。 */
    private Object object;

    /**
     * 用于反序列化的构造函数。
     */
    public Ser() {
    }

    /**
     * 创建一个用于序列化的实例。
     *
     * @param type  类型
     * @param object  对象
     */
    Ser(byte type, Object object) {
        this.type = type;
        this.object = object;
    }

    //-----------------------------------------------------------------------
    /**
     * 实现 {@code Externalizable} 接口以写入对象。
     * @serialData
     *
     * 每个可序列化的类都映射到一个类型，该类型是流中的第一个字节。
     * 参考每个类的 {@code writeReplace} 序列化形式以获取类型的值和类型的值序列。
     * <ul>
     * <li><a href="../../serialized-form.html#java.time.Duration">Duration.writeReplace</a>
     * <li><a href="../../serialized-form.html#java.time.Instant">Instant.writeReplace</a>
     * <li><a href="../../serialized-form.html#java.time.LocalDate">LocalDate.writeReplace</a>
     * <li><a href="../../serialized-form.html#java.time.LocalDateTime">LocalDateTime.writeReplace</a>
     * <li><a href="../../serialized-form.html#java.time.LocalTime">LocalTime.writeReplace</a>
     * <li><a href="../../serialized-form.html#java.time.MonthDay">MonthDay.writeReplace</a>
     * <li><a href="../../serialized-form.html#java.time.OffsetTime">OffsetTime.writeReplace</a>
     * <li><a href="../../serialized-form.html#java.time.OffsetDateTime">OffsetDateTime.writeReplace</a>
     * <li><a href="../../serialized-form.html#java.time.Period">Period.writeReplace</a>
     * <li><a href="../../serialized-form.html#java.time.Year">Year.writeReplace</a>
     * <li><a href="../../serialized-form.html#java.time.YearMonth">YearMonth.writeReplace</a>
     * <li><a href="../../serialized-form.html#java.time.ZoneId">ZoneId.writeReplace</a>
     * <li><a href="../../serialized-form.html#java.time.ZoneOffset">ZoneOffset.writeReplace</a>
     * <li><a href="../../serialized-form.html#java.time.ZonedDateTime">ZonedDateTime.writeReplace</a>
     * </ul>
     *
     * @param out  要写入的数据流，不为空
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writeInternal(type, object, out);
    }


                static void writeInternal(byte type, Object object, ObjectOutput out) throws IOException {
        out.writeByte(type);
        switch (type) {
            case DURATION_TYPE:
                ((Duration) object).writeExternal(out);
                break;
            case INSTANT_TYPE:
                ((Instant) object).writeExternal(out);
                break;
            case LOCAL_DATE_TYPE:
                ((LocalDate) object).writeExternal(out);
                break;
            case LOCAL_DATE_TIME_TYPE:
                ((LocalDateTime) object).writeExternal(out);
                break;
            case LOCAL_TIME_TYPE:
                ((LocalTime) object).writeExternal(out);
                break;
            case ZONE_REGION_TYPE:
                ((ZoneRegion) object).writeExternal(out);
                break;
            case ZONE_OFFSET_TYPE:
                ((ZoneOffset) object).writeExternal(out);
                break;
            case ZONE_DATE_TIME_TYPE:
                ((ZonedDateTime) object).writeExternal(out);
                break;
            case OFFSET_TIME_TYPE:
                ((OffsetTime) object).writeExternal(out);
                break;
            case OFFSET_DATE_TIME_TYPE:
                ((OffsetDateTime) object).writeExternal(out);
                break;
            case YEAR_TYPE:
                ((Year) object).writeExternal(out);
                break;
            case YEAR_MONTH_TYPE:
                ((YearMonth) object).writeExternal(out);
                break;
            case MONTH_DAY_TYPE:
                ((MonthDay) object).writeExternal(out);
                break;
            case PERIOD_TYPE:
                ((Period) object).writeExternal(out);
                break;
            default:
                throw new InvalidClassException("Unknown serialized type");
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 实现 {@code Externalizable} 接口以读取对象。
     * @serialData
     *
     * 由类型 {@code writeReplace} 方法定义的流类型和参数被读取，并传递给类型对应的静态工厂以创建新实例。该实例作为反序列化的 {@code Ser} 对象返回。
     *
     * <ul>
     * <li><a href="../../serialized-form.html#java.time.Duration">Duration</a> - {@code Duration.ofSeconds(seconds, nanos);}
     * <li><a href="../../serialized-form.html#java.time.Instant">Instant</a> - {@code Instant.ofEpochSecond(seconds, nanos);}
     * <li><a href="../../serialized-form.html#java.time.LocalDate">LocalDate</a> - {@code LocalDate.of(year, month, day);}
     * <li><a href="../../serialized-form.html#java.time.LocalDateTime">LocalDateTime</a> - {@code LocalDateTime.of(date, time);}
     * <li><a href="../../serialized-form.html#java.time.LocalTime">LocalTime</a> - {@code LocalTime.of(hour, minute, second, nano);}
     * <li><a href="../../serialized-form.html#java.time.MonthDay">MonthDay</a> - {@code MonthDay.of(month, day);}
     * <li><a href="../../serialized-form.html#java.time.OffsetTime">OffsetTime</a> - {@code OffsetTime.of(time, offset);}
     * <li><a href="../../serialized-form.html#java.time.OffsetDateTime">OffsetDateTime</a> - {@code OffsetDateTime.of(dateTime, offset);}
     * <li><a href="../../serialized-form.html#java.time.Period">Period</a> - {@code Period.of(years, months, days);}
     * <li><a href="../../serialized-form.html#java.time.Year">Year</a> - {@code Year.of(year);}
     * <li><a href="../../serialized-form.html#java.time.YearMonth">YearMonth</a> - {@code YearMonth.of(year, month);}
     * <li><a href="../../serialized-form.html#java.time.ZonedDateTime">ZonedDateTime</a> - {@code ZonedDateTime.ofLenient(dateTime, offset, zone);}
     * <li><a href="../../serialized-form.html#java.time.ZoneId">ZoneId</a> - {@code ZoneId.of(id);}
     * <li><a href="../../serialized-form.html#java.time.ZoneOffset">ZoneOffset</a> - {@code (offsetByte == 127 ? ZoneOffset.ofTotalSeconds(in.readInt()) : ZoneOffset.ofTotalSeconds(offsetByte * 900));}
     * </ul>
     *
     * @param in  要读取的数据，不为 null
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        type = in.readByte();
        object = readInternal(type, in);
    }

    static Object read(ObjectInput in) throws IOException, ClassNotFoundException {
        byte type = in.readByte();
        return readInternal(type, in);
    }

    private static Object readInternal(byte type, ObjectInput in) throws IOException, ClassNotFoundException {
        switch (type) {
            case DURATION_TYPE: return Duration.readExternal(in);
            case INSTANT_TYPE: return Instant.readExternal(in);
            case LOCAL_DATE_TYPE: return LocalDate.readExternal(in);
            case LOCAL_DATE_TIME_TYPE: return LocalDateTime.readExternal(in);
            case LOCAL_TIME_TYPE: return LocalTime.readExternal(in);
            case ZONE_DATE_TIME_TYPE: return ZonedDateTime.readExternal(in);
            case ZONE_OFFSET_TYPE: return ZoneOffset.readExternal(in);
            case ZONE_REGION_TYPE: return ZoneRegion.readExternal(in);
            case OFFSET_TIME_TYPE: return OffsetTime.readExternal(in);
            case OFFSET_DATE_TIME_TYPE: return OffsetDateTime.readExternal(in);
            case YEAR_TYPE: return Year.readExternal(in);
            case YEAR_MONTH_TYPE: return YearMonth.readExternal(in);
            case MONTH_DAY_TYPE: return MonthDay.readExternal(in);
            case PERIOD_TYPE: return Period.readExternal(in);
            default:
                throw new StreamCorruptedException("Unknown serialized type");
        }
    }

    /**
     * 返回将替换此对象的对象。
     *
     * @return 读取的对象，不应为 null
     */
    private Object readResolve() {
         return object;
    }

}
