
/*
 * Copyright (c) 1998, 2022, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

import java.lang.annotation.Native;

/**
 * 包私有抽象类，用于本地文件系统的抽象。
 */

abstract class FileSystem {

    /* -- 归一化和构造 -- */

    /**
     * 返回本地文件系统的名称分隔符字符。
     */
    public abstract char getSeparator();

    /**
     * 返回本地文件系统的路径分隔符字符。
     */
    public abstract char getPathSeparator();

    /**
     * 将给定的路径名字符串转换为标准形式。如果字符串已经是标准形式，则直接返回。
     */
    public abstract String normalize(String path);

    /**
     * 计算此路径名字符串的前缀长度。路径名字符串必须是标准形式。
     */
    public abstract int prefixLength(String path);

    /**
     * 将子路径名字符串解析为父路径。两个字符串都必须是标准形式，结果也将是标准形式。
     */
    public abstract String resolve(String parent, String child);

    /**
     * 返回在两个参数的 File 构造函数中，当父目录参数为空路径名时使用的父路径名字符串。
     */
    public abstract String getDefaultParent();

    /**
     * 如果必要，后处理给定的 URI 路径字符串。这在 win32 上使用，例如将 "/c:/foo" 转换为 "c:/foo"。路径字符串仍然使用斜杠分隔符；File 类中的代码将在该方法返回后进行转换。
     */
    public abstract String fromURIPath(String path);


    /* -- 路径操作 -- */

    /**
     * 告诉给定的抽象路径名是否为绝对路径。
     */
    public abstract boolean isAbsolute(File f);

    /**
     * 告诉给定的抽象路径名是否无效。
     */
    public abstract boolean isInvalid(File f);

    /**
     * 将给定的抽象路径名解析为绝对形式。由 File 类中的 getAbsolutePath 和 getCanonicalPath 方法调用。
     */
    public abstract String resolve(File f);

    public abstract String canonicalize(String path) throws IOException;


    /* -- 属性访问器 -- */

    /* 简单布尔属性的常量 */
    @Native public static final int BA_EXISTS    = 0x01;
    @Native public static final int BA_REGULAR   = 0x02;
    @Native public static final int BA_DIRECTORY = 0x04;
    @Native public static final int BA_HIDDEN    = 0x08;

    /**
     * 返回由给定抽象路径名表示的文件或目录的简单布尔属性，如果不存在或发生其他 I/O 错误，则返回零。
     */
    public abstract int getBooleanAttributes(File f);

    @Native public static final int ACCESS_READ    = 0x04;
    @Native public static final int ACCESS_WRITE   = 0x02;
    @Native public static final int ACCESS_EXECUTE = 0x01;

    /**
     * 检查由给定抽象路径名表示的文件或目录是否可被此进程访问。第二个参数指定要检查的访问权限，ACCESS_READ、ACCESS_WRITE 或 ACCESS_EXECUTE。如果访问被拒绝或发生 I/O 错误，则返回 false。
     */
    public abstract boolean checkAccess(File f, int access);
    /**
     * 根据参数 enable、access 和 owneronly，设置由给定抽象路径名表示的文件或目录的访问权限（仅对所有者或所有人）。
     */
    public abstract boolean setPermission(File f, int access, boolean enable, boolean owneronly);

    /**
     * 返回由给定抽象路径名表示的文件或目录的最后修改时间，如果不存在或发生其他 I/O 错误，则返回零。
     */
    public abstract long getLastModifiedTime(File f);

    /**
     * 返回由给定抽象路径名表示的文件的长度（以字节为单位），如果不存在、是目录或发生其他 I/O 错误，则返回零。
     */
    public abstract long getLength(File f);


    /* -- 文件操作 -- */

    /**
     * 使用给定的路径名创建一个新的空文件。如果文件被创建，则返回 <code>true</code>；如果具有给定路径名的文件或目录已存在，则返回 <code>false</code>。如果发生 I/O 错误，则抛出 IOException。
     */
    public abstract boolean createFileExclusively(String pathname)
        throws IOException;

    /**
     * 删除由给定抽象路径名表示的文件或目录，仅当操作成功时返回 <code>true</code>。
     */
    public abstract boolean delete(File f);

    /**
     * 列出由给定抽象路径名表示的目录的元素。如果成功，则返回一个字符串数组，命名目录中的元素；否则，返回 <code>null</code>。
     */
    public abstract String[] list(File f);

    /**
     * 创建由给定抽象路径名表示的新目录，仅当操作成功时返回 <code>true</code>。
     */
    public abstract boolean createDirectory(File f);

    /**
     * 将由第一个抽象路径名表示的文件或目录重命名为第二个抽象路径名表示的文件或目录，仅当操作成功时返回 <code>true</code>。
     */
    public abstract boolean rename(File f1, File f2);

    /**
     * 设置由给定抽象路径名表示的文件或目录的最后修改时间，仅当操作成功时返回 <code>true</code>。
     */
    public abstract boolean setLastModifiedTime(File f, long time);

    /**
     * 将由给定抽象路径名表示的文件或目录标记为只读，仅当操作成功时返回 <code>true</code>。
     */
    public abstract boolean setReadOnly(File f);

    /* -- 文件系统接口 -- */

    /**
     * 列出可用的文件系统根目录。
     */
    public abstract File[] listRoots();

    /* -- 磁盘使用情况 -- */
    @Native public static final int SPACE_TOTAL  = 0;
    @Native public static final int SPACE_FREE   = 1;
    @Native public static final int SPACE_USABLE = 2;

    public abstract long getSpace(File f, int t);

    /* -- 基础设施 -- */

    /**
     * 按字典顺序比较两个抽象路径名。
     */
    public abstract int compare(File f1, File f2);

    /**
     * 计算一个抽象路径名的哈希码。
     */
    public abstract int hashCode(File f);

    // 用于启用/禁用文件名规范化的性能优化标志
    static boolean useCanonCaches      = true;
    static boolean useCanonPrefixCache = true;

    private static boolean getBooleanProperty(String prop, boolean defaultVal) {
        String val = System.getProperty(prop);
        if (val == null) return defaultVal;
        if (val.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    static {
        useCanonCaches      = getBooleanProperty("sun.io.useCanonCaches",
                                                 useCanonCaches);
        useCanonPrefixCache = getBooleanProperty("sun.io.useCanonPrefixCache",
                                                 useCanonPrefixCache);
    }
}
