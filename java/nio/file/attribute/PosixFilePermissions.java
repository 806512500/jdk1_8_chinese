/*
 * 版权所有 (c) 2007, 2011, Oracle 和/或其附属公司。保留所有权利。
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

package java.nio.file.attribute;

import static java.nio.file.attribute.PosixFilePermission.*;
import java.util.*;

/**
 * 本类仅包含操作 {@link PosixFilePermission} 对象集合的静态方法。
 *
 * @since 1.7
 */

public final class PosixFilePermissions {
    private PosixFilePermissions() { }

    // 将权限位的字符串表示写入 {@code sb}。
    private static void writeBits(StringBuilder sb, boolean r, boolean w, boolean x) {
        if (r) {
            sb.append('r');
        } else {
            sb.append('-');
        }
        if (w) {
            sb.append('w');
        } else {
            sb.append('-');
        }
        if (x) {
            sb.append('x');
        } else {
            sb.append('-');
        }
    }

    /**
     * 返回权限集的 {@code String} 表示。保证返回的 {@code String} 可以由
     * {@link #fromString} 方法解析。
     *
     * <p> 如果集合包含 {@code null} 或非 {@code PosixFilePermission} 类型的元素，则忽略这些元素。
     *
     * @param   perms
     *          权限集
     *
     * @return  权限集的字符串表示
     */
    public static String toString(Set<PosixFilePermission> perms) {
        StringBuilder sb = new StringBuilder(9);
        writeBits(sb, perms.contains(OWNER_READ), perms.contains(OWNER_WRITE),
          perms.contains(OWNER_EXECUTE));
        writeBits(sb, perms.contains(GROUP_READ), perms.contains(GROUP_WRITE),
          perms.contains(GROUP_EXECUTE));
        writeBits(sb, perms.contains(OTHERS_READ), perms.contains(OTHERS_WRITE),
          perms.contains(OTHERS_EXECUTE));
        return sb.toString();
    }

    private static boolean isSet(char c, char setValue) {
        if (c == setValue)
            return true;
        if (c == '-')
            return false;
        throw new IllegalArgumentException("无效模式");
    }
    private static boolean isR(char c) { return isSet(c, 'r'); }
    private static boolean isW(char c) { return isSet(c, 'w'); }
    private static boolean isX(char c) { return isSet(c, 'x'); }

    /**
     * 返回给定 {@code String} 表示对应的权限集。
     *
     * <p> {@code perms} 参数是一个表示权限的 {@code String}。它有 9 个字符，解释为三组三个字符。第一组指的是所有者的权限；接下来是组权限，最后是其他人的权限。在每组中，第一个字符是 {@code 'r'} 表示读取权限，第二个字符是 {@code 'w'} 表示写入权限，第三个字符是 {@code 'x'} 表示执行权限。如果未设置权限，则相应字符设置为 {@code '-'}。
     *
     * <p> <b>使用示例：</b>
     * 假设我们需要一组权限，表示所有者具有读取、写入和执行权限，组具有读取和执行权限，其他人没有权限。
     * <pre>
     *   Set&lt;PosixFilePermission&gt; perms = PosixFilePermissions.fromString("rwxr-x---");
     * </pre>
     *
     * @param   perms
     *          表示一组权限的字符串
     *
     * @return  结果权限集
     *
     * @throws  IllegalArgumentException
     *          如果字符串无法转换为权限集
     *
     * @see #toString(Set)
     */
    public static Set<PosixFilePermission> fromString(String perms) {
        if (perms.length() != 9)
            throw new IllegalArgumentException("无效模式");
        Set<PosixFilePermission> result = EnumSet.noneOf(PosixFilePermission.class);
        if (isR(perms.charAt(0))) result.add(OWNER_READ);
        if (isW(perms.charAt(1))) result.add(OWNER_WRITE);
        if (isX(perms.charAt(2))) result.add(OWNER_EXECUTE);
        if (isR(perms.charAt(3))) result.add(GROUP_READ);
        if (isW(perms.charAt(4))) result.add(GROUP_WRITE);
        if (isX(perms.charAt(5))) result.add(GROUP_EXECUTE);
        if (isR(perms.charAt(6))) result.add(OTHERS_READ);
        if (isW(perms.charAt(7))) result.add(OTHERS_WRITE);
        if (isX(perms.charAt(8))) result.add(OTHERS_EXECUTE);
        return result;
    }

    /**
     * 创建一个 {@link FileAttribute}，封装给定文件权限的副本，适用于传递给
     * {@link java.nio.file.Files#createFile createFile} 或
     * {@link java.nio.file.Files#createDirectory createDirectory} 方法。
     *
     * @param   perms
     *          权限集
     *
     * @return  封装给定文件权限的属性，其 {@link FileAttribute#name 名称} 为 {@code "posix:permissions"}
     *
     * @throws  ClassCastException
     *          如果集合包含非 {@code PosixFilePermission} 类型的元素
     */
    public static FileAttribute<Set<PosixFilePermission>>
        asFileAttribute(Set<PosixFilePermission> perms)
    {
        // 复制集合并检查空值（如果元素不是 PosixFilePermission，将抛出 CCE）
        perms = new HashSet<PosixFilePermission>(perms);
        for (PosixFilePermission p: perms) {
            if (p == null)
                throw new NullPointerException();
        }
        final Set<PosixFilePermission> value = perms;
        return new FileAttribute<Set<PosixFilePermission>>() {
            @Override
            public String name() {
                return "posix:permissions";
            }
            @Override
            public Set<PosixFilePermission> value() {
                return Collections.unmodifiableSet(value);
            }
        };
    }
}
