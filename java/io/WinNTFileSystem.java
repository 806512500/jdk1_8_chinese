
/*
 * Copyright (c) 2001, 2022, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import sun.security.action.GetPropertyAction;

/**
 * Windows NT/2000 的 Unicode 意识文件系统。
 *
 * @author Konstantin Kladko
 * @since 1.4
 */
class WinNTFileSystem extends FileSystem {

    private final char slash;
    private final char altSlash;
    private final char semicolon;

    // 是否启用替代数据流 (ADS)，通过抑制对路径中无效字符（特别是“:”）的检查。
    // 默认情况下，ADS 支持是启用的，只有在属性设置为“false”（忽略大小写）时才会禁用。
    private static final boolean ENABLE_ADS;
    static {
        String enableADS = GetPropertyAction.privilegedGetProperty("jdk.io.File.enableADS");
        if (enableADS != null) {
            ENABLE_ADS = !enableADS.equalsIgnoreCase(Boolean.FALSE.toString());
        } else {
            ENABLE_ADS = true;
        }
    }


    public WinNTFileSystem() {
        slash = AccessController.doPrivileged(
            new GetPropertyAction("file.separator")).charAt(0);
        semicolon = AccessController.doPrivileged(
            new GetPropertyAction("path.separator")).charAt(0);
        altSlash = (this.slash == '\\') ? '/' : '\\';
    }

    private boolean isSlash(char c) {
        return (c == '\\') || (c == '/');
    }

    private boolean isLetter(char c) {
        return ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'));
    }

    private String slashify(String p) {
        if (!p.isEmpty() && p.charAt(0) != slash) return slash + p;
        else return p;
    }

    /* -- 归一化和构造 -- */

    @Override
    public char getSeparator() {
        return slash;
    }

    @Override
    public char getPathSeparator() {
        return semicolon;
    }

    /* 检查给定的路径名是否正常。如果不是，则对需要归一化的路径名部分调用实际的归一化器。
       这样我们只需遍历整个路径名字符串一次。 */
    @Override
    public String normalize(String path) {
        int n = path.length();
        char slash = this.slash;
        char altSlash = this.altSlash;
        char prev = 0;
        for (int i = 0; i < n; i++) {
            char c = path.charAt(i);
            if (c == altSlash)
                return normalize(path, n, (prev == slash) ? i - 1 : i);
            if ((c == slash) && (prev == slash) && (i > 1))
                return normalize(path, n, i - 1);
            if ((c == ':') && (i > 1))
                return normalize(path, n, 0);
            prev = c;
        }
        if (prev == slash) return normalize(path, n, n - 1);
        return path;
    }

    /* 归一化给定的路径名，其长度为 len，从给定的偏移量开始；此偏移量之前的部分已经归一化。 */
    private String normalize(String path, int len, int off) {
        if (len == 0) return path;
        if (off < 3) off = 0;   /* 避免 UNC 路径中的围栏柱情况 */
        int src;
        char slash = this.slash;
        StringBuffer sb = new StringBuffer(len);

        if (off == 0) {
            /* 完全归一化，包括前缀 */
            src = normalizePrefix(path, len, sb);
        } else {
            /* 部分归一化 */
            src = off;
            sb.append(path.substring(0, off));
        }

        /* 从路径的其余部分中删除多余的斜杠，并将所有斜杠强制转换为首选斜杠 */
        while (src < len) {
            char c = path.charAt(src++);
            if (isSlash(c)) {
                while ((src < len) && isSlash(path.charAt(src))) src++;
                if (src == len) {
                    /* 检查尾随分隔符 */
                    int sn = sb.length();
                    if ((sn == 2) && (sb.charAt(1) == ':')) {
                        /* "z:\\" */
                        sb.append(slash);
                        break;
                    }
                    if (sn == 0) {
                        /* "\\" */
                        sb.append(slash);
                        break;
                    }
                    if ((sn == 1) && (isSlash(sb.charAt(0)))) {
                        /* "\\\\" 不会折叠为 "\\"，因为 "\\\\" 标记了 UNC 路径的开始。
                           即使它本身不是一个有效的 UNC 路径，我们仍然保留它，以与 win32 API 保持一致，
                           这些 API 将这种情况视为无效的 UNC 路径，而不是当前驱动器根目录的别名。 */
                        sb.append(slash);
                        break;
                    }
                    /* 路径不表示根目录，因此不附加尾随斜杠 */
                    break;
                } else {
                    sb.append(slash);
                }
            } else {
                sb.append(c);
            }
        }

        String rv = sb.toString();
        return rv;
    }

    /* 正常的 Win32 路径名不包含重复的斜杠，除非可能是 UNC 前缀，并且不以斜杠结尾。它可以是空字符串。
       归一化的 Win32 路径名有一个方便的属性，即前缀的长度几乎唯一地标识了路径的类型以及它是绝对路径还是相对路径：

           0  相对于驱动器和目录
           1  驱动器相对（以 '\\' 开头）
           2  绝对 UNC（如果第一个字符是 '\\'），否则目录相对（形式为 "z:foo"）
           3  绝对本地路径名（以 "z:\\" 开头）
     */
    private int normalizePrefix(String path, int len, StringBuffer sb) {
        int src = 0;
        while ((src < len) && isSlash(path.charAt(src))) src++;
        char c;
        if ((len - src >= 2)
            && isLetter(c = path.charAt(src))
            && path.charAt(src + 1) == ':') {
            /* 如果驱动器指定符后面有前导斜杠，则删除前导斜杠。
               这个 hack 是为了支持包含驱动器指定符的文件 URL（例如，“file://c:/path”）。
               作为副作用，“/c:/path” 可以作为 “c:/path” 的替代品。 */
            sb.append(c);
            sb.append(':');
            src += 2;
        } else {
            src = 0;
            if ((len >= 2)
                && isSlash(path.charAt(0))
                && isSlash(path.charAt(1))) {
                /* UNC 路径名：保留第一个斜杠；将 src 指向第二个斜杠，以便进一步的斜杠将被折叠到第二个斜杠。
                   结果将是一个以 "\\\\" 开头的路径名，后面（很可能）是一个主机名。 */
                src = 1;
                sb.append(slash);
            }
        }
        return src;
    }


                @Override
    public int prefixLength(String path) {
        char slash = this.slash;
        int n = path.length();
        if (n == 0) return 0;
        char c0 = path.charAt(0);
        char c1 = (n > 1) ? path.charAt(1) : 0;
        if (c0 == slash) {
            if (c1 == slash) return 2;  /* 绝对UNC路径名 "\\\\foo" */
            return 1;                   /* 驱动器相对 "\\foo" */
        }
        if (isLetter(c0) && (c1 == ':')) {
            if ((n > 2) && (path.charAt(2) == slash))
                return 3;               /* 绝对本地路径名 "z:\\foo" */
            return 2;                   /* 目录相对 "z:foo" */
        }
        return 0;                       /* 完全相对 */
    }

    @Override
    public boolean isInvalid(File f) {
        if (f.getPath().indexOf('\u0000') >= 0)
            return true;

        if (ENABLE_ADS)
            return false;

        // 如果在位置大于1处有":"，或者在位置1处有":"且第一个字符不是字母，则无效
        String pathname = f.getPath();
        int lastColon = pathname.lastIndexOf(":");

        // 如果没有":"，或者最后一个":"在索引1处且第一个字符是字母，则有效
        if (lastColon < 0 ||
            (lastColon == 1 && isLetter(pathname.charAt(0))))
            return false;

        // 如果路径创建失败，则无效
        Path path = null;
        try {
            path = sun.nio.fs.DefaultFileSystemProvider.theFileSystem().getPath(pathname);
            return false;
        } catch (InvalidPathException ignored) {
        }

        return true;
    }

    @Override
    public String resolve(String parent, String child) {
        int pn = parent.length();
        if (pn == 0) return child;
        int cn = child.length();
        if (cn == 0) return parent;

        String c = child;
        int childStart = 0;
        int parentEnd = pn;

        if ((cn > 1) && (c.charAt(0) == slash)) {
            if (c.charAt(1) == slash) {
                /* 当子路径是UNC路径名时，丢弃前缀 */
                childStart = 2;
            } else {
                /* 当子路径是驱动器相对时，丢弃前缀 */
                childStart = 1;

            }
            if (cn == childStart) { // 子路径是双斜杠
                if (parent.charAt(pn - 1) == slash)
                    return parent.substring(0, pn - 1);
                return parent;
            }
        }

        if (parent.charAt(pn - 1) == slash)
            parentEnd--;

        int strlen = parentEnd + cn - childStart;
        char[] theChars = null;
        if (child.charAt(childStart) == slash) {
            theChars = new char[strlen];
            parent.getChars(0, parentEnd, theChars, 0);
            child.getChars(childStart, cn, theChars, parentEnd);
        } else {
            theChars = new char[strlen + 1];
            parent.getChars(0, parentEnd, theChars, 0);
            theChars[parentEnd] = slash;
            child.getChars(childStart, cn, theChars, parentEnd + 1);
        }
        return new String(theChars);
    }

    @Override
    public String getDefaultParent() {
        return ("" + slash);
    }

    @Override
    public String fromURIPath(String path) {
        String p = path;
        if ((p.length() > 2) && (p.charAt(2) == ':')) {
            // "/c:/foo" --> "c:/foo"
            p = p.substring(1);
            // "c:/foo/" --> "c:/foo"，但 "c:/" --> "c:/"
            if ((p.length() > 3) && p.endsWith("/"))
                p = p.substring(0, p.length() - 1);
        } else if ((p.length() > 1) && p.endsWith("/")) {
            // "/foo/" --> "/foo"
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    /* -- 路径操作 -- */

    @Override
    public boolean isAbsolute(File f) {
        int pl = f.getPrefixLength();
        return (((pl == 2) && (f.getPath().charAt(0) == slash))
                || (pl == 3));
    }

    @Override
    public String resolve(File f) {
        String path = f.getPath();
        int pl = f.getPrefixLength();
        if ((pl == 2) && (path.charAt(0) == slash))
            return path;                        /* UNC */
        if (pl == 3)
            return path;                        /* 绝对本地 */
        if (pl == 0)
            return getUserPath() + slashify(path); /* 完全相对 */
        if (pl == 1) {                          /* 驱动器相对 */
            String up = getUserPath();
            String ud = getDrive(up);
            if (ud != null) return ud + path;
            return up + path;                   /* 用户目录是UNC路径 */
        }
        if (pl == 2) {                          /* 目录相对 */
            String up = getUserPath();
            String ud = getDrive(up);
            if ((ud != null) && path.startsWith(ud))
                return up + slashify(path.substring(2));
            char drive = path.charAt(0);
            String dir = getDriveDirectory(drive);
            String np;
            if (dir != null) {
                /* 当解析指向其他驱动器的目录相对路径时，要求调用者对结果有读权限 */
                String p = drive + (':' + dir + slashify(path.substring(2)));
                SecurityManager security = System.getSecurityManager();
                try {
                    if (security != null) security.checkRead(p);
                } catch (SecurityException x) {
                    /* 不要在异常中披露驱动器的目录 */
                    throw new SecurityException("无法解析路径 " + path);
                }
                return p;
            }
            return drive + ":" + slashify(path.substring(2)); /* 假设 */
        }
        throw new InternalError("无法解析的路径: " + path);
    }

    private String getUserPath() {
        /* 为了兼容性和安全性，每次必须查找 */
        return normalize(System.getProperty("user.dir"));
    }


                private String getDrive(String path) {
        int pl = prefixLength(path);
        return (pl == 3) ? path.substring(0, 2) : null;
    }

    private static String[] driveDirCache = new String[26];

    private static int driveIndex(char d) {
        if ((d >= 'a') && (d <= 'z')) return d - 'a';
        if ((d >= 'A') && (d <= 'Z')) return d - 'A';
        return -1;
    }

    private native String getDriveDirectory(int drive);

    private String getDriveDirectory(char drive) {
        int i = driveIndex(drive);
        if (i < 0) return null;
        String s = driveDirCache[i];
        if (s != null) return s;
        s = getDriveDirectory(i + 1);
        driveDirCache[i] = s;
        return s;
    }

    // 缓存规范化结果以提高启动性能。
    // 第一个缓存处理同一路径名的重复规范化。
    // 前缀缓存处理同一目录内的重复规范化，且必须不生成与规范化的算法（在 canonicalize_md.c 中）不同的结果。因此，前缀缓存是保守的，不用于复杂的路径名。
    private ExpiringCache cache       = new ExpiringCache();
    private ExpiringCache prefixCache = new ExpiringCache();

    @Override
    public String canonicalize(String path) throws IOException {
        // 如果路径仅包含驱动器字母，则跳过规范化
        int len = path.length();
        if ((len == 2) &&
            (isLetter(path.charAt(0))) &&
            (path.charAt(1) == ':')) {
            char c = path.charAt(0);
            if ((c >= 'A') && (c <= 'Z'))
                return path;
            return "" + ((char) (c-32)) + ':';
        } else if ((len == 3) &&
                   (isLetter(path.charAt(0))) &&
                   (path.charAt(1) == ':') &&
                   (path.charAt(2) == '\\')) {
            char c = path.charAt(0);
            if ((c >= 'A') && (c <= 'Z'))
                return path;
            return "" + ((char) (c-32)) + ':' + '\\';
        }
        if (!useCanonCaches) {
            return canonicalize0(path);
        } else {
            String res = cache.get(path);
            if (res == null) {
                String dir = null;
                String resDir = null;
                if (useCanonPrefixCache) {
                    dir = parentOrNull(path);
                    if (dir != null) {
                        resDir = prefixCache.get(dir);
                        if (resDir != null) {
                            /*
                             * 仅在前缀缓存中命中；完整路径已规范化，
                             * 但我们需要获取该目录中文件的规范名称以获得适当的大小写
                             */
                            String filename = path.substring(1 + dir.length());
                            res = canonicalizeWithPrefix(resDir, filename);
                            cache.put(dir + File.separatorChar + filename, res);
                        }
                    }
                }
                if (res == null) {
                    res = canonicalize0(path);
                    cache.put(path, res);
                    if (useCanonPrefixCache && dir != null) {
                        resDir = parentOrNull(res);
                        if (resDir != null) {
                            File f = new File(res);
                            if (f.exists() && !f.isDirectory()) {
                                prefixCache.put(dir, resDir);
                            }
                        }
                    }
                }
            }
            return res;
        }
    }

    private native String canonicalize0(String path)
            throws IOException;

    private String canonicalizeWithPrefix(String canonicalPrefix,
            String filename) throws IOException
    {
        return canonicalizeWithPrefix0(canonicalPrefix,
                canonicalPrefix + File.separatorChar + filename);
    }

    // 假设前缀（直到最后一个文件名之前的所有内容）已规范化；仅获取路径最后一个元素的规范名称
    private native String canonicalizeWithPrefix0(String canonicalPrefix,
            String pathWithCanonicalPrefix)
            throws IOException;

    // 尽力获取此路径的父路径；用于优化文件名规范化。对于在 canonicalize_md.c 中会抛出异常或以其他方式处理非简单路径名（如处理 "." 和 ".."）的情况，必须返回 null。在其他情况下也可以保守地返回 null。返回 null 将导致调用底层（昂贵的）规范化例程。
    private static String parentOrNull(String path) {
        if (path == null) return null;
        char sep = File.separatorChar;
        char altSep = '/';
        int last = path.length() - 1;
        int idx = last;
        int adjacentDots = 0;
        int nonDotCount = 0;
        while (idx > 0) {
            char c = path.charAt(idx);
            if (c == '.') {
                if (++adjacentDots >= 2) {
                    // 放弃包含 . 和 .. 的路径名
                    return null;
                }
                if (nonDotCount == 0) {
                    // 放弃以 . 结尾的路径名
                    return null;
                }
            } else if (c == sep) {
                if (adjacentDots == 1 && nonDotCount == 0) {
                    // 放弃包含 . 和 .. 的路径名
                    return null;
                }
                if (idx == 0 ||
                    idx >= last - 1 ||
                    path.charAt(idx - 1) == sep ||
                    path.charAt(idx - 1) == altSep) {
                    // 放弃路径名末尾包含相邻斜杠的情况
                    return null;
                }
                return path.substring(0, idx);
            } else if (c == altSep) {
                // 放弃包含前后斜杠的路径名
                return null;
            } else if (c == '*' || c == '?') {
                // 放弃包含通配符的路径名
                return null;
            } else {
                ++nonDotCount;
                adjacentDots = 0;
            }
            --idx;
        }
        return null;
    }

                /* -- 属性访问器 -- */

    @Override
    public native int getBooleanAttributes(File f);

    @Override
    public native boolean checkAccess(File f, int access);

    @Override
    public native long getLastModifiedTime(File f);

    @Override
    public native long getLength(File f);

    @Override
    public native boolean setPermission(File f, int access, boolean enable,
            boolean owneronly);

    /* -- 文件操作 -- */

    @Override
    public native boolean createFileExclusively(String path)
            throws IOException;

    @Override
    public native String[] list(File f);

    @Override
    public native boolean createDirectory(File f);

    @Override
    public native boolean setLastModifiedTime(File f, long time);

    @Override
    public native boolean setReadOnly(File f);

    @Override
    public boolean delete(File f) {
        // 文件删除和重命名操作后保持规范化缓存同步。可以更聪明地处理
        // （即，仅移除/更新受影响的条目），但可能不值得，
        // 因为这些条目无论如何在30秒后就会过期。
        cache.clear();
        prefixCache.clear();
        return delete0(f);
    }

    private native boolean delete0(File f);

    @Override
    public boolean rename(File f1, File f2) {
        // 文件删除和重命名操作后保持规范化缓存同步。可以更聪明地处理
        // （即，仅移除/更新受影响的条目），但可能不值得，
        // 因为这些条目无论如何在30秒后就会过期。
        cache.clear();
        prefixCache.clear();
        return rename0(f1, f2);
    }

    private native boolean rename0(File f1, File f2);

    /* -- 文件系统接口 -- */

    @Override
    public File[] listRoots() {
        int ds = listRoots0();
        int n = 0;
        for (int i = 0; i < 26; i++) {
            if (((ds >> i) & 1) != 0) {
                if (!access((char)('A' + i) + ":" + slash))
                    ds &= ~(1 << i);
                else
                    n++;
            }
        }
        File[] fs = new File[n];
        int j = 0;
        char slash = this.slash;
        for (int i = 0; i < 26; i++) {
            if (((ds >> i) & 1) != 0)
                fs[j++] = new File((char)('A' + i) + ":" + slash);
        }
        return fs;
    }

    private static native int listRoots0();

    private boolean access(String path) {
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) security.checkRead(path);
            return true;
        } catch (SecurityException x) {
            return false;
        }
    }

    /* -- 磁盘使用情况 -- */

    @Override
    public long getSpace(File f, int t) {
        if (f.exists()) {
            return getSpace0(f, t);
        }
        return 0;
    }

    private native long getSpace0(File f, int t);

    /* -- 基础设施 -- */

    @Override
    public int compare(File f1, File f2) {
        return f1.getPath().compareToIgnoreCase(f2.getPath());
    }

    @Override
    public int hashCode(File f) {
        /* 可以使此方法更高效：String.hashCodeIgnoreCase */
        return f.getPath().toLowerCase(Locale.ENGLISH).hashCode() ^ 1234321;
    }

    private static native void initIDs();

    static {
        initIDs();
    }
}
