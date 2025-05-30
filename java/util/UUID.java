
/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.security.*;

/**
 * 表示一个不可变的全局唯一标识符（UUID）的类。UUID 表示一个 128 位的值。
 *
 * <p> 存在不同变体的这些全局标识符。此类的方法用于操作 Leach-Salz 变体，尽管构造函数允许创建任何变体的 UUID（如下所述）。
 *
 * <p> Leach-Salz 变体（变体 2）的布局如下：
 *
 * 最显著的长整数部分包含以下无符号字段：
 * <pre>
 * 0xFFFFFFFF00000000 time_low
 * 0x00000000FFFF0000 time_mid
 * 0x000000000000F000 version
 * 0x0000000000000FFF time_hi
 * </pre>
 * 最不显著的长整数部分包含以下无符号字段：
 * <pre>
 * 0xC000000000000000 variant
 * 0x3FFF000000000000 clock_seq
 * 0x0000FFFFFFFFFFFF node
 * </pre>
 *
 * <p> 变体字段包含一个标识 UUID 布局的值。上述位布局仅适用于变体值为 2 的 UUID，表示 Leach-Salz 变体。
 *
 * <p> 版本字段包含一个描述此 UUID 类型的值。有四种基本类型的 UUID：基于时间的、DCE 安全的、基于名称的和随机生成的 UUID。这些类型的版本值分别为 1、2、3 和 4。
 *
 * <p> 有关创建 UUID 的算法等更多信息，请参见 <a href="http://www.ietf.org/rfc/rfc4122.txt"> <i>RFC&nbsp;4122: A
 * Universally Unique IDentifier (UUID) URN Namespace</i></a>，第 4.2 节“创建基于时间的 UUID 的算法”。
 *
 * @since   1.5
 */
public final class UUID implements java.io.Serializable, Comparable<UUID> {

    /**
     * 用于互操作性的显式 serialVersionUID。
     */
    private static final long serialVersionUID = -4856846361193249489L;

    /*
     * 此 UUID 的最显著 64 位。
     *
     * @serial
     */
    private final long mostSigBits;

    /*
     * 此 UUID 的最不显著 64 位。
     *
     * @serial
     */
    private final long leastSigBits;

    /*
     * 用于创建基于随机数的 UUID 的随机数生成器。在持有者类中以延迟初始化。
     */
    private static class Holder {
        static final SecureRandom numberGenerator = new SecureRandom();
    }

    // 构造函数和工厂方法

    /*
     * 使用字节数组构造新的 UUID 的私有构造函数。
     */
    private UUID(byte[] data) {
        long msb = 0;
        long lsb = 0;
        assert data.length == 16 : "data must be 16 bytes in length";
        for (int i=0; i<8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i=8; i<16; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);
        this.mostSigBits = msb;
        this.leastSigBits = lsb;
    }

    /**
     * 使用指定的数据构造一个新的 {@code UUID}。{@code
     * mostSigBits} 用于 {@code UUID} 的最显著 64 位，{@code leastSigBits} 用于 {@code UUID} 的最不显著 64 位。
     *
     * @param  mostSigBits
     *         {@code UUID} 的最显著位
     *
     * @param  leastSigBits
     *         {@code UUID} 的最不显著位
     */
    public UUID(long mostSigBits, long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
    }

    /**
     * 静态工厂方法，用于获取类型 4（伪随机生成）的 UUID。
     *
     * 该 {@code UUID} 是使用加密强度的伪随机数生成器生成的。
     *
     * @return  一个随机生成的 {@code UUID}
     */
    public static UUID randomUUID() {
        SecureRandom ng = Holder.numberGenerator;

        byte[] randomBytes = new byte[16];
        ng.nextBytes(randomBytes);
        randomBytes[6]  &= 0x0f;  /* 清除版本        */
        randomBytes[6]  |= 0x40;  /* 设置为版本 4     */
        randomBytes[8]  &= 0x3f;  /* 清除变体        */
        randomBytes[8]  |= 0x80;  /* 设置为 IETF 变体  */
        return new UUID(randomBytes);
    }

    /**
     * 静态工厂方法，用于基于指定的字节数组获取类型 3（基于名称）的 {@code UUID}。
     *
     * @param  name
     *         用于构造 {@code UUID} 的字节数组
     *
     * @return  从指定数组生成的 {@code UUID}
     */
    public static UUID nameUUIDFromBytes(byte[] name) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("MD5 not supported", nsae);
        }
        byte[] md5Bytes = md.digest(name);
        md5Bytes[6]  &= 0x0f;  /* 清除版本        */
        md5Bytes[6]  |= 0x30;  /* 设置为版本 3     */
        md5Bytes[8]  &= 0x3f;  /* 清除变体        */
        md5Bytes[8]  |= 0x80;  /* 设置为 IETF 变体  */
        return new UUID(md5Bytes);
    }


                /**
     * 从字符串标准表示形式创建一个 {@code UUID}，如 {@link #toString} 方法中所述。
     *
     * @param  name
     *         指定一个 {@code UUID} 的字符串。
     *
     * @return  具有指定值的 {@code UUID}
     *
     * @throws  IllegalArgumentException
     *          如果 name 不符合 {@link #toString} 中描述的字符串表示形式。
     *
     */
    public static UUID fromString(String name) {
        String[] components = name.split("-");
        if (components.length != 5)
            throw new IllegalArgumentException("Invalid UUID string: "+name);
        for (int i=0; i<5; i++)
            components[i] = "0x"+components[i];

        long mostSigBits = Long.decode(components[0]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[1]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[2]).longValue();

        long leastSigBits = Long.decode(components[3]).longValue();
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(components[4]).longValue();

        return new UUID(mostSigBits, leastSigBits);
    }

    // 字段访问方法

    /**
     * 返回此 UUID 的 128 位值的最低 64 位。
     *
     * @return  此 UUID 的 128 位值的最低 64 位
     */
    public long getLeastSignificantBits() {
        return leastSigBits;
    }

    /**
     * 返回此 UUID 的 128 位值的最高 64 位。
     *
     * @return  此 UUID 的 128 位值的最高 64 位
     */
    public long getMostSignificantBits() {
        return mostSigBits;
    }

    /**
     * 与此 {@code UUID} 关联的版本号。版本号描述了此 {@code UUID} 的生成方式。
     *
     * 版本号的含义如下：
     * <ul>
     * <li>1    基于时间的 UUID
     * <li>2    DCE 安全 UUID
     * <li>3    基于名称的 UUID
     * <li>4    随机生成的 UUID
     * </ul>
     *
     * @return  此 {@code UUID} 的版本号
     */
    public int version() {
        // 版本号是 MS 长整数中由 0x000000000000F000 掩码的位
        return (int)((mostSigBits >> 12) & 0x0f);
    }

    /**
     * 与此 {@code UUID} 关联的变体号。变体号描述了 {@code UUID} 的布局。
     *
     * 变体号的含义如下：
     * <ul>
     * <li>0    保留用于 NCS 向后兼容
     * <li>2    <a href="http://www.ietf.org/rfc/rfc4122.txt">IETF&nbsp;RFC&nbsp;4122</a>
     * (Leach-Salz)，由此类使用
     * <li>6    保留，用于 Microsoft Corporation 向后兼容
     * <li>7    保留用于未来定义
     * </ul>
     *
     * @return  此 {@code UUID} 的变体号
     */
    public int variant() {
        // 此字段由不同数量的位组成。
        // 0    -    -    保留用于 NCS 向后兼容
        // 1    0    -    IETF 即 Leach-Salz 变体（由此类使用）
        // 1    1    0    保留，用于 Microsoft 向后兼容
        // 1    1    1    保留用于未来定义。
        return (int) ((leastSigBits >>> (64 - (leastSigBits >>> 62)))
                      & (leastSigBits >> 63));
    }

    /**
     * 与此 UUID 关联的时间戳值。
     *
     * <p> 60 位时间戳值由此 {@code UUID} 的 time_low、time_mid 和 time_hi 字段构造而成。结果时间戳以 100 纳秒为单位，自 1582 年 10 月 15 日午夜 UTC 开始计算。
     *
     * <p> 时间戳值仅在基于时间的 UUID 中有意义，其版本类型为 1。如果此 {@code UUID} 不是基于时间的 UUID，则此方法抛出 UnsupportedOperationException。
     *
     * @throws UnsupportedOperationException
     *         如果此 UUID 不是版本 1 的 UUID
     * @return 此 {@code UUID} 的时间戳。
     */
    public long timestamp() {
        if (version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        }

        return (mostSigBits & 0x0FFFL) << 48
             | ((mostSigBits >> 16) & 0x0FFFFL) << 32
             | mostSigBits >>> 32;
    }

    /**
     * 与此 UUID 关联的时钟序列值。
     *
     * <p> 14 位时钟序列值由此 UUID 的时钟序列字段构造而成。时钟序列字段用于保证基于时间的 UUID 的时间唯一性。
     *
     * <p> 时钟序列值仅在基于时间的 UUID 中有意义，其版本类型为 1。如果此 UUID 不是基于时间的 UUID，则此方法抛出 UnsupportedOperationException。
     *
     * @return  此 {@code UUID} 的时钟序列
     *
     * @throws  UnsupportedOperationException
     *          如果此 UUID 不是版本 1 的 UUID
     */
    public int clockSequence() {
        if (version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        }

        return (int)((leastSigBits & 0x3FFF000000000000L) >>> 48);
    }

    /**
     * 与此 UUID 关联的节点值。
     *
     * <p> 48 位节点值由此 UUID 的节点字段构造而成。此字段旨在保存生成此 UUID 的机器的 IEEE 802 地址，以保证空间唯一性。
     *
     * <p> 节点值仅在基于时间的 UUID 中有意义，其版本类型为 1。如果此 UUID 不是基于时间的 UUID，则此方法抛出 UnsupportedOperationException。
     *
     * @return  此 {@code UUID} 的节点值
     *
     * @throws  UnsupportedOperationException
     *          如果此 UUID 不是版本 1 的 UUID
     */
    public long node() {
        if (version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        }

        return leastSigBits & 0x0000FFFFFFFFFFFFL;
    }


                // Object Inherited Methods

    /**
     * 返回表示此 {@code UUID} 的 {@code String} 对象。
     *
     * <p> UUID 的字符串表示形式如下 BNF 所述：
     * <blockquote><pre>
     * {@code
     * UUID                   = <time_low> "-" <time_mid> "-"
     *                          <time_high_and_version> "-"
     *                          <variant_and_sequence> "-"
     *                          <node>
     * time_low               = 4*<hexOctet>
     * time_mid               = 2*<hexOctet>
     * time_high_and_version  = 2*<hexOctet>
     * variant_and_sequence   = 2*<hexOctet>
     * node                   = 6*<hexOctet>
     * hexOctet               = <hexDigit><hexDigit>
     * hexDigit               =
     *       "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
     *       | "a" | "b" | "c" | "d" | "e" | "f"
     *       | "A" | "B" | "C" | "D" | "E" | "F"
     * }</pre></blockquote>
     *
     * @return  此 {@code UUID} 的字符串表示形式
     */
    public String toString() {
        return (digits(mostSigBits >> 32, 8) + "-" +
                digits(mostSigBits >> 16, 4) + "-" +
                digits(mostSigBits, 4) + "-" +
                digits(leastSigBits >> 48, 4) + "-" +
                digits(leastSigBits, 12));
    }

    /** 返回由指定数量的十六进制数字表示的 val。 */
    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    /**
     * 返回此 {@code UUID} 的哈希码。
     *
     * @return  此 {@code UUID} 的哈希码值
     */
    public int hashCode() {
        long hilo = mostSigBits ^ leastSigBits;
        return ((int)(hilo >> 32)) ^ (int) hilo;
    }

    /**
     * 将此对象与指定对象进行比较。结果为 {@code
     * true} 当且仅当参数不为 {@code null}，是 {@code UUID}
     * 对象，具有相同的变体，并且包含与此 {@code UUID} 相同的值，逐位相同。
     *
     * @param  obj
     *         要比较的对象
     *
     * @return  如果对象相同则返回 {@code true}；否则返回 {@code false}
     */
    public boolean equals(Object obj) {
        if ((null == obj) || (obj.getClass() != UUID.class))
            return false;
        UUID id = (UUID)obj;
        return (mostSigBits == id.mostSigBits &&
                leastSigBits == id.leastSigBits);
    }

    // 比较操作

    /**
     * 将此 UUID 与指定的 UUID 进行比较。
     *
     * <p> 如果两个 UUID 在最显著的字段中不同，且第一个 UUID 的该字段值大于第二个 UUID，则第一个 UUID 大于第二个 UUID。
     *
     * @param  val
     *         要与此 {@code UUID} 进行比较的 {@code UUID}
     *
     * @return  如果此 {@code UUID} 小于、等于或大于 {@code val}，则分别返回 -1、0 或 1
     *
     */
    public int compareTo(UUID val) {
        // 有意设置顺序，以便 UUID 可以简单地作为两个数字进行数值比较
        return (this.mostSigBits < val.mostSigBits ? -1 :
                (this.mostSigBits > val.mostSigBits ? 1 :
                 (this.leastSigBits < val.leastSigBits ? -1 :
                  (this.leastSigBits > val.leastSigBits ? 1 :
                   0))));
    }
}
