
/*
 * 版权所有 (c) 1995, 2015，Oracle 和/或其附属公司。保留所有权利。
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

package java.util.zip;

import static java.util.zip.ZipUtils.*;
import java.nio.file.attribute.FileTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.util.zip.ZipConstants64.*;

/**
 * 该类用于表示 ZIP 文件条目。
 *
 * @author      David Connelly
 */
public
class ZipEntry implements ZipConstants, Cloneable {

    String name;        // 条目名称
    long xdostime = -1; // 最后修改时间（以扩展 DOS 时间表示，
                        // 其中转换中丢失的毫秒可能编码到较高部分）
    FileTime mtime;     // 最后修改时间，从额外字段数据中获取
    FileTime atime;     // 最后访问时间，从额外字段数据中获取
    FileTime ctime;     // 创建时间，从额外字段数据中获取
    long crc = -1;      // 条目数据的 crc-32
    long size = -1;     // 条目数据的未压缩大小
    long csize = -1;    // 条目数据的压缩大小
    int method = -1;    // 压缩方法
    int flag = 0;       // 通用目的标志
    byte[] extra;       // 条目的可选额外字段数据
    String comment;     // 条目的可选注释字符串

    /**
     * 未压缩条目的压缩方法。
     */
    public static final int STORED = 0;

    /**
     * 压缩（放气）条目的压缩方法。
     */
    public static final int DEFLATED = 8;

    /**
     * 表示 1980 年之前的时戳的 DOS 时间常量。
     */
    static final long DOSTIME_BEFORE_1980 = (1 << 21) | (1 << 16);

    /**
     * 大约 128 年，以毫秒为单位（忽略闰年等）。
     *
     * 这为自纪元以来的 DOS 时间的毫秒值建立了一个近似上限，用于启用一个高效但
     * 足够的边界检查，以避免生成扩展的最后修改时间条目。
     *
     * 计算确切的数字是地区依赖的，需要提前加载时区数据，并且在实际中几乎没有意义。由于 DOS
     * 时间理论上到 2107 年 - 2099 年后兼容性不保证 - 将此设置为 2099 年之前但接近 2099 年的时间
     * 应该足够。
     */
    private static final long UPPER_DOSTIME_BOUND =
            128L * 365 * 24 * 60 * 60 * 1000;

    /**
     * 使用指定名称创建一个新的 ZIP 条目。
     *
     * @param  name
     *         条目名称
     *
     * @throws NullPointerException 如果条目名称为 null
     * @throws IllegalArgumentException 如果条目名称超过 0xFFFF 字节
     */
    public ZipEntry(String name) {
        Objects.requireNonNull(name, "name");
        if (name.length() > 0xFFFF) {
            throw new IllegalArgumentException("entry name too long");
        }
        this.name = name;
    }

    /**
     * 使用指定的 ZIP 条目中的字段创建一个新的 ZIP 条目。
     *
     * @param  e
     *         一个 ZIP 条目对象
     *
     * @throws NullPointerException 如果条目对象为 null
     */
    public ZipEntry(ZipEntry e) {
        Objects.requireNonNull(e, "entry");
        name = e.name;
        xdostime = e.xdostime;
        mtime = e.mtime;
        atime = e.atime;
        ctime = e.ctime;
        crc = e.crc;
        size = e.size;
        csize = e.csize;
        method = e.method;
        flag = e.flag;
        extra = e.extra;
        comment = e.comment;
    }

    /**
     * 创建一个新的未初始化的 ZIP 条目
     */
    ZipEntry() {}

    /**
     * 返回条目的名称。
     * @return 条目的名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置条目的最后修改时间。
     *
     * <p> 如果条目输出到 ZIP 文件或 ZIP 文件格式的输出流，最后修改时间将
     * 被存储到 ZIP 文件条目的 {@code date and time fields} 中，并以标准
     * {@code MS-DOS 日期和时间格式} 编码。使用 {@link java.util.TimeZone#getDefault() 默认时区}
     * 将纪元时间转换为 MS-DOS 日期和时间。
     *
     * @param  time
     *         自纪元以来条目的最后修改时间，以毫秒为单位
     *
     * @see #getTime()
     * @see #getLastModifiedTime()
     */
    public void setTime(long time) {
        this.xdostime = javaToExtendedDosTime(time);
        // 如果时间在有效的 DOS 时间范围内，则避免设置 mtime 字段
        if (xdostime != DOSTIME_BEFORE_1980 && time <= UPPER_DOSTIME_BOUND) {
            this.mtime = null;
        } else {
            this.mtime = FileTime.from(time, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 返回条目的最后修改时间。
     *
     * <p> 如果条目从 ZIP 文件或 ZIP 文件格式的输入流中读取，这是 ZIP 文件条目的
     * {@code date and time fields} 中的最后修改时间。使用
     * {@link java.util.TimeZone#getDefault() 默认时区} 将标准 MS-DOS 格式的日期和时间转换为
     * 纪元时间。
     *
     * @return  自纪元以来条目的最后修改时间，以毫秒为单位，或未指定时为 -1
     *
     * @see #setTime(long)
     * @see #setLastModifiedTime(FileTime)
     */
    public long getTime() {
        if (mtime != null) {
            return mtime.toMillis();
        }
        return (xdostime != -1) ? extendedDosToJavaTime(xdostime) : -1;
    }

    /**
     * 设置条目的最后修改时间。
     *
     * <p> 当输出到 ZIP 文件或 ZIP 文件格式的输出流时，此方法设置的最后修改时间将
     * 被存储到 ZIP 文件条目的 {@code date and time fields} 中，以 {@code 标准
     * MS-DOS 日期和时间格式}，并在 {@code 可选额外数据} 中以 UTC 时间存储扩展时间戳字段。
     *
     * @param  time
     *         条目的最后修改时间
     * @return 该 ZIP 条目
     *
     * @throws NullPointerException 如果 {@code time} 为 null
     *
     * @see #getLastModifiedTime()
     * @since 1.8
     */
    public ZipEntry setLastModifiedTime(FileTime time) {
        this.mtime = Objects.requireNonNull(time, "lastModifiedTime");
        this.xdostime = javaToExtendedDosTime(time.to(TimeUnit.MILLISECONDS));
        return this;
    }


                /**
     * 返回条目的最后修改时间。
     *
     * <p> 如果条目是从 ZIP 文件或 ZIP 文件格式的输入流中读取的，这是 ZIP 文件条目的 {@code optional extra data} 中的最后修改时间，前提是扩展时间戳字段存在。否则，最后修改时间将从条目的 {@code date and time fields} 中读取，使用 {@link
     * java.util.TimeZone#getDefault() 默认时区} 将标准 MS-DOS 格式的时间转换为纪元时间。
     *
     * @return 条目的最后修改时间，未指定时返回 null
     *
     * @see #setLastModifiedTime(FileTime)
     * @since 1.8
     */
    public FileTime getLastModifiedTime() {
        if (mtime != null)
            return mtime;
        if (xdostime == -1)
            return null;
        return FileTime.from(getTime(), TimeUnit.MILLISECONDS);
    }

    /**
     * 设置条目的最后访问时间。
     *
     * <p> 如果设置，最后访问时间将在输出到 ZIP 文件或 ZIP 文件格式的流时存储到条目 {@code optional extra data} 的扩展时间戳字段中。
     *
     * @param  time
     *         条目的最后访问时间
     * @return 该 ZIP 条目
     *
     * @throws NullPointerException 如果 {@code time} 为 null
     *
     * @see #getLastAccessTime()
     * @since 1.8
     */
    public ZipEntry setLastAccessTime(FileTime time) {
        this.atime = Objects.requireNonNull(time, "lastAccessTime");
        return this;
    }

    /**
     * 返回条目的最后访问时间。
     *
     * <p> 最后访问时间是从 ZIP 文件或 ZIP 文件格式的流中读取时，条目 {@code optional extra data} 的扩展时间戳字段中获取的。
     *
     * @return 条目的最后访问时间，未指定时返回 null
     *
     * @see #setLastAccessTime(FileTime)
     * @since 1.8
     */
    public FileTime getLastAccessTime() {
        return atime;
    }

    /**
     * 设置条目的创建时间。
     *
     * <p> 如果设置，创建时间将在输出到 ZIP 文件或 ZIP 文件格式的流时存储到条目 {@code optional extra data} 的扩展时间戳字段中。
     *
     * @param  time
     *         条目的创建时间
     * @return 该 ZIP 条目
     *
     * @throws NullPointerException 如果 {@code time} 为 null
     *
     * @see #getCreationTime()
     * @since 1.8
     */
    public ZipEntry setCreationTime(FileTime time) {
        this.ctime = Objects.requireNonNull(time, "creationTime");
        return this;
    }

    /**
     * 返回条目的创建时间。
     *
     * <p> 创建时间是从 ZIP 文件或 ZIP 文件格式的流中读取时，条目 {@code optional extra data} 的扩展时间戳字段中获取的。
     *
     * @return 条目的创建时间，未指定时返回 null
     * @see #setCreationTime(FileTime)
     * @since 1.8
     */
    public FileTime getCreationTime() {
        return ctime;
    }

    /**
     * 设置条目数据的未压缩大小。
     *
     * @param size 未压缩大小，以字节为单位
     *
     * @throws IllegalArgumentException 如果指定的大小小于 0，或者在不支持 <a href="package-summary.html#zip64">ZIP64 格式</a> 时大于 0xFFFFFFFF，或者在支持 ZIP64 时小于 0
     * @see #getSize()
     */
    public void setSize(long size) {
        if (size < 0) {
            throw new IllegalArgumentException("无效的条目大小");
        }
        this.size = size;
    }

    /**
     * 返回条目数据的未压缩大小。
     *
     * @return 条目数据的未压缩大小，未知时返回 -1
     * @see #setSize(long)
     */
    public long getSize() {
        return size;
    }

    /**
     * 返回条目数据的压缩大小。
     *
     * <p> 对于存储条目，压缩大小将与条目的未压缩大小相同。
     *
     * @return 条目数据的压缩大小，未知时返回 -1
     * @see #setCompressedSize(long)
     */
    public long getCompressedSize() {
        return csize;
    }

    /**
     * 设置条目数据的压缩大小。
     *
     * @param csize 要设置的压缩大小
     *
     * @see #getCompressedSize()
     */
    public void setCompressedSize(long csize) {
        this.csize = csize;
    }

    /**
     * 设置条目数据的 CRC-32 校验和。
     *
     * @param crc CRC-32 值
     *
     * @throws IllegalArgumentException 如果指定的 CRC-32 值小于 0 或大于 0xFFFFFFFF
     * @see #getCrc()
     */
    public void setCrc(long crc) {
        if (crc < 0 || crc > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("无效的条目 crc-32");
        }
        this.crc = crc;
    }

    /**
     * 返回条目数据的 CRC-32 校验和。
     *
     * @return 条目数据的 CRC-32 校验和，未知时返回 -1
     *
     * @see #setCrc(long)
     */
    public long getCrc() {
        return crc;
    }

    /**
     * 设置条目的压缩方法。
     *
     * @param method 压缩方法，可以是 STORED 或 DEFLATED
     *
     * @throws  IllegalArgumentException 如果指定的压缩方法无效
     * @see #getMethod()
     */
    public void setMethod(int method) {
        if (method != STORED && method != DEFLATED) {
            throw new IllegalArgumentException("无效的压缩方法");
        }
        this.method = method;
    }

    /**
     * 返回条目的压缩方法。
     *
     * @return 条目的压缩方法，未指定时返回 -1
     * @see #setMethod(int)
     */
    public int getMethod() {
        return method;
    }

    /**
     * 设置条目的可选额外字段数据。
     *
     * <p> 调用此方法可能会更改此条目的最后修改时间、最后访问时间和创建时间，如果 {@code extra} 字段数据包含扩展时间戳字段，例如 {@code NTFS tag 0x0001} 或 {@code Info-ZIP Extended Timestamp}，如 <a href="http://www.info-zip.org/doc/appnote-19970311-iz.zip">Info-ZIP
     * Application Note 970311</a> 中所指定的。
     *
     * @param  extra
     *         额外字段数据字节
     *
     * @throws IllegalArgumentException 如果指定的额外字段数据长度大于 0xFFFF 字节
     *
     * @see #getExtra()
     */
    public void setExtra(byte[] extra) {
        setExtra0(extra, false);
    }


                /**
     * 设置条目的可选额外字段数据。
     *
     * @param extra
     *        额外字段数据字节
     * @param doZIP64
     *        如果为 true，则从 ZIP64 字段中设置大小和压缩大小（如果存在）
     */
    void setExtra0(byte[] extra, boolean doZIP64) {
        if (extra != null) {
            if (extra.length > 0xFFFF) {
                throw new IllegalArgumentException("无效的额外字段长度");
            }
            // 额外字段格式为 "HeaderID(2)DataSize(2)Data..."
            int off = 0;
            int len = extra.length;
            while (off + 4 < len) {
                int tag = get16(extra, off);
                int sz = get16(extra, off + 2);
                off += 4;
                if (off + sz > len)         // 无效数据
                    break;
                switch (tag) {
                case EXTID_ZIP64:
                    if (doZIP64) {
                        // LOC 额外的 ZIP64 条目必须同时包含原始文件大小和压缩文件大小字段。
                        // 如果 ZIP64 额外字段无效，直接跳过。即使很少见，条目大小可能恰好是魔术值，
                        // 并且“意外地”在额外字段中匹配某些字节。
                        if (sz >= 16) {
                            size = get64(extra, off);
                            csize = get64(extra, off + 8);
                        }
                    }
                    break;
                case EXTID_NTFS:
                    if (sz < 32) // 保留 4 字节 + 标签 2 字节 + 大小 2 字节
                        break;   // 修改时间和访问时间 24 字节
                    int pos = off + 4;               // 保留 4 字节
                    if (get16(extra, pos) !=  0x0001 || get16(extra, pos + 2) != 24)
                        break;
                    mtime = winTimeToFileTime(get64(extra, pos + 4));
                    atime = winTimeToFileTime(get64(extra, pos + 12));
                    ctime = winTimeToFileTime(get64(extra, pos + 20));
                    break;
                case EXTID_EXTT:
                    int flag = Byte.toUnsignedInt(extra[off]);
                    int sz0 = 1;
                    // CEN-header 额外字段只包含修改时间，或者根本没有时间戳。'sz' 用于标记其存在或不存在。
                    // 但如果 LOC 中有 mtime，则 CEN 中也必须有。
                    if ((flag & 0x1) != 0 && (sz0 + 4) <= sz) {
                        mtime = unixTimeToFileTime(get32(extra, off + sz0));
                        sz0 += 4;
                    }
                    if ((flag & 0x2) != 0 && (sz0 + 4) <= sz) {
                        atime = unixTimeToFileTime(get32(extra, off + sz0));
                        sz0 += 4;
                    }
                    if ((flag & 0x4) != 0 && (sz0 + 4) <= sz) {
                        ctime = unixTimeToFileTime(get32(extra, off + sz0));
                        sz0 += 4;
                    }
                    break;
                 default:
                }
                off += sz;
            }
        }
        this.extra = extra;
    }

    /**
     * 返回条目的额外字段数据。
     *
     * @return 条目的额外字段数据，如果没有则返回 null
     *
     * @see #setExtra(byte[])
     */
    public byte[] getExtra() {
        return extra;
    }

    /**
     * 设置条目的可选注释字符串。
     *
     * <p>ZIP 条目注释的最大长度为 0xffff。如果指定的注释字符串在编码后的长度大于 0xFFFF 字节，
     * 则只有前 0xFFFF 字节会被输出到 ZIP 文件条目中。
     *
     * @param comment 注释字符串
     *
     * @see #getComment()
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * 返回条目的注释字符串。
     *
     * @return 条目的注释字符串，如果没有则返回 null
     *
     * @see #setComment(String)
     */
    public String getComment() {
        return comment;
    }

    /**
     * 如果这是目录条目，则返回 true。目录条目定义为名称以 '/' 结尾的条目。
     * @return 如果这是目录条目，则返回 true
     */
    public boolean isDirectory() {
        return name.endsWith("/");
    }

    /**
     * 返回 ZIP 条目的字符串表示形式。
     */
    public String toString() {
        return getName();
    }

    /**
     * 返回此条目的哈希码值。
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * 返回此条目的副本。
     */
    public Object clone() {
        try {
            ZipEntry e = (ZipEntry)super.clone();
            e.extra = (extra == null) ? null : extra.clone();
            return e;
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们实现了 Cloneable
            throw new InternalError(e);
        }
    }
}
