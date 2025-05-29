/*
 * 版权所有 (c) 2009, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.nio.file;

import java.util.Set;
import java.util.EnumSet;
import java.security.SecureRandom;
import static java.security.AccessController.*;
import java.io.IOException;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import static java.nio.file.attribute.PosixFilePermission.*;
import sun.security.action.GetPropertyAction;


/**
 * 辅助类，用于支持具有初始属性的临时文件和目录的创建。
 */

class TempFileHelper {
    private TempFileHelper() { }

    // 临时目录位置
    private static final Path tmpdir =
        Paths.get(doPrivileged(new GetPropertyAction("java.io.tmpdir")));

    private static final boolean isPosix =
        FileSystems.getDefault().supportedFileAttributeViews().contains("posix");

    // 文件名生成，目前与 java.io.File 相同
    private static final SecureRandom random = new SecureRandom();
    private static Path generatePath(String prefix, String suffix, Path dir) {
        long n = random.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);
        Path name = dir.getFileSystem().getPath(prefix + Long.toString(n) + suffix);
        // 生成的名称应为简单文件名
        if (name.getParent() != null)
            throw new IllegalArgumentException("无效的前缀或后缀");
        return dir.resolve(name);
    }

    // 默认文件和目录权限（延迟初始化）
    private static class PosixPermissions {
        static final FileAttribute<Set<PosixFilePermission>> filePermissions =
            PosixFilePermissions.asFileAttribute(EnumSet.of(OWNER_READ, OWNER_WRITE));
        static final FileAttribute<Set<PosixFilePermission>> dirPermissions =
            PosixFilePermissions.asFileAttribute(EnumSet
                .of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE));
    }

    /**
     * 在给定目录中创建文件或目录（如果 dir 为 {@code null}，则在临时目录中创建）。
     */
    private static Path create(Path dir,
                               String prefix,
                               String suffix,
                               boolean createDirectory,
                               FileAttribute<?>[] attrs)
        throws IOException
    {
        if (prefix == null)
            prefix = "";
        if (suffix == null)
            suffix = (createDirectory) ? "" : ".tmp";
        if (dir == null)
            dir = tmpdir;

        // 在 POSIX 环境中，如果调用者未提供初始权限，则使用默认文件和目录权限
        if (isPosix && (dir.getFileSystem() == FileSystems.getDefault())) {
            if (attrs.length == 0) {
                // 没有属性，因此使用默认权限
                attrs = new FileAttribute<?>[1];
                attrs[0] = (createDirectory) ? PosixPermissions.dirPermissions :
                                               PosixPermissions.filePermissions;
            } else {
                // 检查是否提供了 POSIX 权限；如果没有，则使用默认值
                boolean hasPermissions = false;
                for (int i=0; i<attrs.length; i++) {
                    if (attrs[i].name().equals("posix:permissions")) {
                        hasPermissions = true;
                        break;
                    }
                }
                if (!hasPermissions) {
                    FileAttribute<?>[] copy = new FileAttribute<?>[attrs.length+1];
                    System.arraycopy(attrs, 0, copy, 0, attrs.length);
                    attrs = copy;
                    attrs[attrs.length-1] = (createDirectory) ?
                        PosixPermissions.dirPermissions :
                        PosixPermissions.filePermissions;
                }
            }
        }

        // 循环生成随机名称，直到文件或目录可以创建
        SecurityManager sm = System.getSecurityManager();
        for (;;) {
            Path f;
            try {
                f = generatePath(prefix, suffix, dir);
            } catch (InvalidPathException e) {
                // 不泄露临时目录位置
                if (sm != null)
                    throw new IllegalArgumentException("无效的前缀或后缀");
                throw e;
            }
            try {
                if (createDirectory) {
                    return Files.createDirectory(f, attrs);
                } else {
                    return Files.createFile(f, attrs);
                }
            } catch (SecurityException e) {
                // 不泄露临时目录位置
                if (dir == tmpdir && sm != null)
                    throw new SecurityException("无法创建临时文件或目录");
                throw e;
            } catch (FileAlreadyExistsException e) {
                // 忽略
            }
        }
    }

    /**
     * 在给定目录中创建临时文件，如果 dir 为 {@code null}，则在临时目录中创建。
     */
    static Path createTempFile(Path dir,
                               String prefix,
                               String suffix,
                               FileAttribute<?>[] attrs)
        throws IOException
    {
        return create(dir, prefix, suffix, false, attrs);
    }

    /**
     * 在给定目录中创建临时目录，如果 dir 为 {@code null}，则在临时目录中创建。
     */
    static Path createTempDirectory(Path dir,
                                    String prefix,
                                    FileAttribute<?>[] attrs)
        throws IOException
    {
        return create(dir, prefix, null, true, attrs);
    }
}
