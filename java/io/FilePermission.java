
/*
 * Copyright (c) 1997, 2019, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.file.InvalidPathException;
import java.security.*;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Collections;
import sun.security.util.SecurityConstants;

/**
 * 该类表示对文件或目录的访问。FilePermission 由路径名和该路径名的有效操作集组成。
 * <P>
 * 路径名是授予指定操作的文件或目录的路径名。以 "/*" 结尾的路径名（其中 "/" 是文件分隔符字符，<code>File.separatorChar</code>）表示该目录中的所有文件和子目录。以 "/-" 结尾的路径名表示（递归地）该目录中的所有文件和子目录。由特殊标记 "&lt;&lt;ALL FILES&gt;&gt;" 组成的路径名匹配 <b>任何</b> 文件。
 * <P>
 * 注意：由单个 "*" 组成的路径名表示当前目录中的所有文件，而由单个 "-" 组成的路径名表示当前目录中的所有文件以及（递归地）当前目录中的所有文件和子目录。
 * <P>
 * 要授予的操作通过包含一个或多个逗号分隔的关键字的字符串传递给构造函数。可能的关键字是 "read"、"write"、"execute"、"delete" 和 "readlink"。它们的含义定义如下：
 *
 * <DL>
 *    <DT> read <DD> 读取权限
 *    <DT> write <DD> 写入权限
 *    <DT> execute
 *    <DD> 执行权限。允许调用 <code>Runtime.exec</code>。对应于 <code>SecurityManager.checkExec</code>。
 *    <DT> delete
 *    <DD> 删除权限。允许调用 <code>File.delete</code>。对应于 <code>SecurityManager.checkDelete</code>。
 *    <DT> readlink
 *    <DD> 读取链接权限。允许通过调用 {@link java.nio.file.Files#readSymbolicLink readSymbolicLink} 方法读取 <a href="../nio/file/package-summary.html#links">符号链接</a> 的目标。
 * </DL>
 * <P>
 * 操作字符串在处理前转换为小写。
 * <P>
 * 授予 FilePermissions 时要小心。考虑授予对各种文件和目录的读取和特别是写入访问权限的影响。带有写入操作的 "&lt;&lt;ALL FILES&gt;&gt;" 权限尤其危险。这授予对整个文件系统的写入权限。这实际上允许替换系统二进制文件，包括 JVM 运行时环境。
 *
 * <p>请注意：代码总是可以从它所在的目录（或该目录的子目录）读取文件；它不需要显式权限来这样做。
 *
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 *
 *
 * @author Marianne Mueller
 * @author Roland Schemers
 * @since 1.2
 *
 * @serial exclude
 */

public final class FilePermission extends Permission implements Serializable {

    /**
     * 执行操作。
     */
    private final static int EXECUTE = 0x1;
    /**
     * 写入操作。
     */
    private final static int WRITE   = 0x2;
    /**
     * 读取操作。
     */
    private final static int READ    = 0x4;
    /**
     * 删除操作。
     */
    private final static int DELETE  = 0x8;
    /**
     * 读取链接操作。
     */
    private final static int READLINK    = 0x10;

    /**
     * 所有操作（读取、写入、执行、删除、读取链接）
     */
    private final static int ALL     = READ|WRITE|EXECUTE|DELETE|READLINK;
    /**
     * 无操作。
     */
    private final static int NONE    = 0x0;

    // 操作掩码
    private transient int mask;

    // 路径是否表示目录？（通配符或递归）
    private transient boolean directory;

    // 是否是递归目录规范？
    private transient boolean recursive;

    /**
     * 操作字符串。
     *
     * @serial
     */
    private String actions; // 尽可能长时间保持为空，然后在 getAction 函数中创建并重用。

    // 规范化的目录路径。在目录的情况下，它是名称 "/blah/*" 或 "/blah/-"，不包括最后一个字符（"*" 或 "-"）。

    private transient String cpath;
    private transient boolean invalid;  // 输入路径是否无效
    private transient boolean allFiles; // 是否为 <<ALL FILES>>

    // init(int mask) 使用的静态字符串
    private static final char RECURSIVE_CHAR = '-';
    private static final char WILD_CHAR = '*';

/*
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("***\n");
        sb.append("cpath = "+cpath+"\n");
        sb.append("mask = "+mask+"\n");
        sb.append("actions = "+getActions()+"\n");
        sb.append("directory = "+directory+"\n");
        sb.append("recursive = "+recursive+"\n");
        sb.append("***\n");
        return sb.toString();
    }
*/

    private static final long serialVersionUID = 7930732926638008763L;

    /**
     * 初始化 FilePermission 对象。所有构造函数的公共部分。
     * 反序列化时也会调用。
     *
     * @param mask 要使用操作掩码。
     *
     */
    private void init(int mask) {
        if ((mask & ALL) != mask)
                throw new IllegalArgumentException("无效的操作掩码");

        if (mask == NONE)
                throw new IllegalArgumentException("无效的操作掩码");

        if ((cpath = getName()) == null)
                throw new NullPointerException("名称不能为空");

        this.mask = mask;

        if (cpath.equals("<<ALL FILES>>")) {
            allFiles = true;
            directory = true;
            recursive = true;
            cpath = "";
            return;
        }


                    if (isPathInvalid()) {
            invalid = true;
            return;
        }

        // 仅在可能的情况下存储规范路径
        cpath = AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                try {
                    String path = cpath;
                    if (cpath.endsWith("*")) {
                        // 用通配符字符替换，调用 getCanonicalPath
                        // 以避免使用旨在匹配目录中所有条目的路径调用它
                        path = path.substring(0, path.length()-1) + "-";
                        path = new File(path).getCanonicalPath();
                        return path.substring(0, path.length()-1) + "*";
                    } else {
                        return new File(path).getCanonicalPath();
                    }
                } catch (IOException ioe) {
                    return cpath;
                }
            }
        });

        int len = cpath.length();
        char last = ((len > 0) ? cpath.charAt(len - 1) : 0);

        if (last == RECURSIVE_CHAR &&
            cpath.charAt(len - 2) == File.separatorChar) {
            directory = true;
            recursive = true;
            cpath = cpath.substring(0, --len);
        } else if (last == WILD_CHAR &&
            cpath.charAt(len - 2) == File.separatorChar) {
            directory = true;
            //recursive = false;
            cpath = cpath.substring(0, --len);
        } else {
            // 过度处理，因为它们已经初始化为 false，但
            // 在这里注释掉以提醒我们...
            //directory = false;
            //recursive = false;
        }

        // XXX: 此时路径应该是绝对路径。如果不是，是否应该终止？
    }

    // 检查路径的有效性
    private boolean isPathInvalid() {
        if (cpath.indexOf(". ") != -1) {
            try {
                String name = cpath.endsWith("*") ? cpath.substring(0, cpath.length() - 1) + "-" : cpath;
                new File(name).toPath();
            } catch (InvalidPathException ipe) {
                return true;
            }
        }
        return false;
    }

    /**
     * 使用指定的操作创建一个新的 FilePermission 对象。
     * <i>path</i> 是文件或目录的路径名，<i>actions</i>
     * 包含一个逗号分隔的列表，表示授予文件或目录的操作。可能的操作有
     * "read", "write", "execute", "delete" 和 "readlink"。
     *
     * <p>以 "/*" 结尾的路径名（其中 "/" 是文件分隔符字符，<code>File.separatorChar</code>）
     * 表示该目录中包含的所有文件和目录。
     * 以 "/-" 结尾的路径名表示（递归地）该目录中包含的所有文件和子目录。特殊路径名
     * "&lt;&lt;ALL FILES&gt;&gt;" 匹配任何文件。
     *
     * <p>由单个 "*" 组成的路径名表示当前目录中的所有文件，
     * 而由单个 "-" 组成的路径名表示当前目录中的所有文件
     * 以及（递归地）当前目录中包含的所有文件和子目录。
     *
     * <p>包含空字符串的路径名表示空路径。
     *
     * @param path 文件/目录的路径名。
     * @param actions 操作字符串。
     *
     * @throws IllegalArgumentException
     *          如果 actions 为 <code>null</code>，为空或包含指定可能操作之外的操作。
     */
    public FilePermission(String path, String actions) {
        super(path);
        init(getMask(actions));
    }

    /**
     * 使用操作掩码创建一个新的 FilePermission 对象。
     * 比 FilePermission(String, String) 构造函数更高效。
     * 可以从需要创建 FilePermission 对象并传递给
     * <code>implies</code> 方法的代码中使用。
     *
     * @param path 文件/目录的路径名。
     * @param mask 要使用操作掩码。
     */

    // 包私有，供 FilePermissionCollection add 方法使用
    FilePermission(String path, int mask) {
        super(path);
        init(mask);
    }

    /**
     * 检查此 FilePermission 对象是否 "implies" 指定的权限。
     * <P>
     * 更具体地说，如果满足以下条件，此方法返回 true：
     * <ul>
     * <li> <i>p</i> 是 FilePermission 的实例，
     * <li> <i>p</i> 的操作是此对象操作的适当子集，
     * <li> <i>p</i> 的路径名由此对象的路径名隐含。例如，"/tmp/*" 隐含 "/tmp/foo"，因为
     *      "/tmp/*" 包含 "/tmp" 目录中的所有文件，包括名为 "foo" 的文件。
     * </ul>
     *
     * @param p 要检查的权限。
     *
     * @return 如果指定的权限不为 <code>null</code> 并且由此对象隐含，则返回 <code>true</code>，
     *                  否则返回 <code>false</code>。
     */
    public boolean implies(Permission p) {
        if (!(p instanceof FilePermission))
            return false;

        FilePermission that = (FilePermission) p;

        // 获取有效的掩码。即，此对象和 that 对象的 "and"。
        // 它们必须等于 that.mask，implies 才返回 true。

        return ((this.mask & that.mask) == that.mask) && impliesIgnoreMask(that);
    }

    /**
     * 检查权限的操作是否是此对象操作的适当子集。如果
     * 此 FilePermission 的路径也隐含 that FilePermission 的路径，则返回有效的掩码。
     *
     * @param that 要检查的 FilePermission。
     * @return 有效的掩码
     */
    boolean impliesIgnoreMask(FilePermission that) {
        if (this == that) {
            return true;
        }
        if (allFiles) {
            return true;
        }
        if (this.invalid || that.invalid) {
            return false;
        }
        if (that.allFiles) {
            return false;
        }
        if (this.directory) {
            if (this.recursive) {
                // 确保 that.path 比 path 长，以便
                // 像 /foo/- 这样的路径不会隐含 /foo
                if (that.directory) {
                    return (that.cpath.length() >= this.cpath.length()) &&
                            that.cpath.startsWith(this.cpath);
                }  else {
                    return ((that.cpath.length() > this.cpath.length()) &&
                        that.cpath.startsWith(this.cpath));
                }
            } else {
                if (that.directory) {
                    // 如果传入的权限是目录
                    // 规范，确保非递归
                    // 权限（即，此对象）不能隐含递归
                    // 权限。
                    if (that.recursive)
                        return false;
                    else
                        return (this.cpath.equals(that.cpath));
                } else {
                    int last = that.cpath.lastIndexOf(File.separatorChar);
                    if (last == -1)
                        return false;
                    else {
                        // this.cpath.equals(that.cpath.substring(0, last+1));
                        // 使用 regionMatches 以避免创建新字符串
                        return (this.cpath.length() == (last + 1)) &&
                            this.cpath.regionMatches(0, that.cpath, 0, last+1);
                    }
                }
            }
        } else if (that.directory) {
            // 如果此对象不是递归/通配符，
            // 不要让它隐含递归/通配符权限
            return false;
        } else {
            return (this.cpath.equals(that.cpath));
        }
    }


                /**
     * 检查两个 FilePermission 对象是否相等。检查 <i>obj</i> 是否为
     * FilePermission，并且具有与该对象相同的路径名和操作。
     *
     * @param obj 要与该对象进行相等性测试的对象。
     * @return 如果 obj 是 FilePermission，并且具有与该 FilePermission 对象相同的
     *          路径名和操作，则返回 <code>true</code>，否则返回 <code>false</code>。
     */
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (! (obj instanceof FilePermission))
            return false;

        FilePermission that = (FilePermission) obj;

        if (this.invalid || that.invalid) {
            return false;
        }
        return (this.mask == that.mask) &&
               (this.allFiles == that.allFiles) &&
            this.cpath.equals(that.cpath) &&
            (this.directory == that.directory) &&
            (this.recursive == that.recursive);
    }

    /**
     * 返回该对象的哈希码值。
     *
     * @return 该对象的哈希码值。
     */
    public int hashCode() {
        return 0;
    }

    /**
     * 将操作字符串转换为操作掩码。
     *
     * @param actions 操作字符串。
     * @return 操作掩码。
     */
    private static int getMask(String actions) {
        int mask = NONE;

        // 空操作有效？
        if (actions == null) {
            return mask;
        }

        // 使用对象身份比较已知的字符串以提高性能（这些值在 JDK 中大量使用）。
        if (actions == SecurityConstants.FILE_READ_ACTION) {
            return READ;
        } else if (actions == SecurityConstants.FILE_WRITE_ACTION) {
            return WRITE;
        } else if (actions == SecurityConstants.FILE_EXECUTE_ACTION) {
            return EXECUTE;
        } else if (actions == SecurityConstants.FILE_DELETE_ACTION) {
            return DELETE;
        } else if (actions == SecurityConstants.FILE_READLINK_ACTION) {
            return READLINK;
        }

        char[] a = actions.toCharArray();

        int i = a.length - 1;
        if (i < 0)
            return mask;

        while (i != -1) {
            char c;

            // 跳过空格
            while ((i!=-1) && ((c = a[i]) == ' ' ||
                               c == '\r' ||
                               c == '\n' ||
                               c == '\f' ||
                               c == '\t'))
                i--;

            // 检查已知的字符串
            int matchlen;

            if (i >= 3 && (a[i-3] == 'r' || a[i-3] == 'R') &&
                          (a[i-2] == 'e' || a[i-2] == 'E') &&
                          (a[i-1] == 'a' || a[i-1] == 'A') &&
                          (a[i] == 'd' || a[i] == 'D'))
            {
                matchlen = 4;
                mask |= READ;

            } else if (i >= 4 && (a[i-4] == 'w' || a[i-4] == 'W') &&
                                 (a[i-3] == 'r' || a[i-3] == 'R') &&
                                 (a[i-2] == 'i' || a[i-2] == 'I') &&
                                 (a[i-1] == 't' || a[i-1] == 'T') &&
                                 (a[i] == 'e' || a[i] == 'E'))
            {
                matchlen = 5;
                mask |= WRITE;

            } else if (i >= 6 && (a[i-6] == 'e' || a[i-6] == 'E') &&
                                 (a[i-5] == 'x' || a[i-5] == 'X') &&
                                 (a[i-4] == 'e' || a[i-4] == 'E') &&
                                 (a[i-3] == 'c' || a[i-3] == 'C') &&
                                 (a[i-2] == 'u' || a[i-2] == 'U') &&
                                 (a[i-1] == 't' || a[i-1] == 'T') &&
                                 (a[i] == 'e' || a[i] == 'E'))
            {
                matchlen = 7;
                mask |= EXECUTE;

            } else if (i >= 5 && (a[i-5] == 'd' || a[i-5] == 'D') &&
                                 (a[i-4] == 'e' || a[i-4] == 'E') &&
                                 (a[i-3] == 'l' || a[i-3] == 'L') &&
                                 (a[i-2] == 'e' || a[i-2] == 'E') &&
                                 (a[i-1] == 't' || a[i-1] == 'T') &&
                                 (a[i] == 'e' || a[i] == 'E'))
            {
                matchlen = 6;
                mask |= DELETE;

            } else if (i >= 7 && (a[i-7] == 'r' || a[i-7] == 'R') &&
                                 (a[i-6] == 'e' || a[i-6] == 'E') &&
                                 (a[i-5] == 'a' || a[i-5] == 'A') &&
                                 (a[i-4] == 'd' || a[i-4] == 'D') &&
                                 (a[i-3] == 'l' || a[i-3] == 'L') &&
                                 (a[i-2] == 'i' || a[i-2] == 'I') &&
                                 (a[i-1] == 'n' || a[i-1] == 'N') &&
                                 (a[i] == 'k' || a[i] == 'K'))
            {
                matchlen = 8;
                mask |= READLINK;

            } else {
                // 解析错误
                throw new IllegalArgumentException(
                        "无效的权限: " + actions);
            }

            // 确保我们没有匹配到单词的尾部
            // 例如 "ackbarfaccept"。同时，跳到逗号。
            boolean seencomma = false;
            while (i >= matchlen && !seencomma) {
                switch(a[i-matchlen]) {
                case ',':
                    seencomma = true;
                    break;
                case ' ': case '\r': case '\n':
                case '\f': case '\t':
                    break;
                default:
                    throw new IllegalArgumentException(
                            "无效的权限: " + actions);
                }
                i--;
            }

            // 将 i 指向逗号前一个位置（或 -1）。
            i -= matchlen;
        }

        return mask;
    }

    /**
     * 返回当前的操作掩码。由 FilePermissionCollection 使用。
     *
     * @return 操作掩码。
     */
    int getMask() {
        return mask;
    }

                /**
     * 返回操作的规范字符串表示形式。
     * 始终按照以下顺序返回存在的操作：
     * 读取，写入，执行，删除，读取链接。
     *
     * @return 操作的规范字符串表示形式。
     */
    private static String getActions(int mask) {
        StringBuilder sb = new StringBuilder();
        boolean comma = false;

        if ((mask & READ) == READ) {
            comma = true;
            sb.append("read");
        }

        if ((mask & WRITE) == WRITE) {
            if (comma) sb.append(',');
            else comma = true;
            sb.append("write");
        }

        if ((mask & EXECUTE) == EXECUTE) {
            if (comma) sb.append(',');
            else comma = true;
            sb.append("execute");
        }

        if ((mask & DELETE) == DELETE) {
            if (comma) sb.append(',');
            else comma = true;
            sb.append("delete");
        }

        if ((mask & READLINK) == READLINK) {
            if (comma) sb.append(',');
            else comma = true;
            sb.append("readlink");
        }

        return sb.toString();
    }

    /**
     * 返回操作的“规范字符串表示形式”。
     * 也就是说，此方法始终按照以下顺序返回存在的操作：
     * 读取，写入，执行，删除，读取链接。例如，如果此 FilePermission
     * 对象允许读取和写入操作，则调用 <code>getActions</code>
     * 将返回字符串 "read,write"。
     *
     * @return 操作的规范字符串表示形式。
     */
    public String getActions() {
        if (actions == null)
            actions = getActions(this.mask);

        return actions;
    }

    /**
     * 返回一个用于存储 FilePermission
     * 对象的新 PermissionCollection 对象。
     * <p>
     * FilePermission 对象必须以允许它们按任何顺序插入集合的方式存储，但同时也要能够使
     * PermissionCollection <code>implies</code>
     * 方法以高效（且一致）的方式实现。
     *
     * <p>例如，如果你有两个 FilePermissions：
     * <OL>
     * <LI>  <code>"/tmp/-", "read"</code>
     * <LI>  <code>"/tmp/scratch/foo", "write"</code>
     * </OL>
     *
     * <p>并且你正在调用 <code>implies</code> 方法，使用 FilePermission：
     *
     * <pre>
     *   "/tmp/scratch/foo", "read,write",
     * </pre>
     *
     * 那么 <code>implies</code> 函数必须
     * 考虑到 "/tmp/-" 和 "/tmp/scratch/foo"
     * 权限，因此有效的权限是 "read,write"，
     * <code>implies</code> 返回 true。FilePermissions 的 "implies" 语义由
     * 此 <code>newPermissionCollection</code> 方法返回的 PermissionCollection 对象正确处理。
     *
     * @return 一个适合存储 FilePermissions 的新 PermissionCollection 对象。
     */
    public PermissionCollection newPermissionCollection() {
        return new FilePermissionCollection();
    }

    /**
     * WriteObject 被调用来将 FilePermission
     * 的状态保存到流中。序列化操作，超类负责名称。
     */
    private void writeObject(ObjectOutputStream s)
        throws IOException
    {
        // 写出操作。超类负责名称
        // 调用 getActions 以确保 actions 字段已初始化
        if (actions == null)
            getActions();
        s.defaultWriteObject();
    }

    /**
     * readObject 被调用来从流中恢复 FilePermission 的状态。
     */
    private void readObject(ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        // 读取操作，然后通过调用 init 恢复所有其他内容。
        s.defaultReadObject();
        init(getMask(actions));
    }
}

/**
 * FilePermissionCollection 存储一组 FilePermission 权限。
 * FilePermission 对象
 * 必须以允许它们按任何顺序插入的方式存储，但同时也要能够使 implies 函数评估 implies 方法。
 * 例如，如果你有两个 FilePermissions：
 * <OL>
 * <LI> "/tmp/-", "read"
 * <LI> "/tmp/scratch/foo", "write"
 * </OL>
 * 并且你正在调用 implies 函数，使用 FilePermission：
 * "/tmp/scratch/foo", "read,write"，那么 implies 函数必须
 * 考虑到 "/tmp/-" 和 "/tmp/scratch/foo"
 * 权限，因此有效的权限是 "read,write"。
 *
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 *
 *
 * @author Marianne Mueller
 * @author Roland Schemers
 *
 * @serial include
 *
 */

final class FilePermissionCollection extends PermissionCollection
    implements Serializable
{
    // 不序列化；请参阅类末尾的序列化部分
    private transient List<Permission> perms;

    /**
     * 创建一个空的 FilePermissionCollection 对象。
     */
    public FilePermissionCollection() {
        perms = new ArrayList<>();
    }

    /**
     * 向 FilePermissionCollection 添加一个权限。哈希的键是
     * permission.path。
     *
     * @param permission 要添加的 Permission 对象。
     *
     * @exception IllegalArgumentException - 如果权限不是
     *                                       FilePermission
     *
     * @exception SecurityException - 如果此 FilePermissionCollection 对象
     *                                已被标记为只读
     */
    public void add(Permission permission) {
        if (! (permission instanceof FilePermission))
            throw new IllegalArgumentException("无效的权限: "+
                                               permission);
        if (isReadOnly())
            throw new SecurityException(
                "尝试向只读 PermissionCollection 添加权限");

        synchronized (this) {
            perms.add(permission);
        }
    }

    /**
     * 检查并查看此权限集是否隐含 "permission" 中表达的权限。
     *
     * @param permission 要比较的 Permission 对象
     *
     * @return 如果 "permission" 是权限集中的一个权限的适当子集，则返回 true，否则返回 false。
     */
    public boolean implies(Permission permission) {
        if (! (permission instanceof FilePermission))
            return false;


                    FilePermission fp = (FilePermission) permission;

        int desired = fp.getMask();
        int effective = 0;
        int needed = desired;

        synchronized (this) {
            int len = perms.size();
            for (int i = 0; i < len; i++) {
                FilePermission x = (FilePermission) perms.get(i);
                if (((needed & x.getMask()) != 0) && x.impliesIgnoreMask(fp)) {
                    effective |=  x.getMask();
                    if ((effective & desired) == desired)
                        return true;
                    needed = (desired ^ effective);
                }
            }
        }
        return false;
    }

    /**
     * 返回容器中所有 FilePermission 对象的枚举。
     *
     * @return 所有 FilePermission 对象的枚举。
     */
    public Enumeration<Permission> elements() {
        // 将 Iterator 转换为 Enumeration
        synchronized (this) {
            return Collections.enumeration(perms);
        }
    }

    private static final long serialVersionUID = 2202956749081564585L;

    // 需要保持与早期版本的序列化互操作性，
    // 这些版本具有可序列化的字段：
    //    private Vector permissions;

    /**
     * @serialField permissions java.util.Vector
     *     FilePermission 对象的列表。
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("permissions", Vector.class),
    };

    /**
     * @serialData "permissions" 字段（包含 FilePermissions 的 Vector）。
     */
    /*
     * 将 perms 字段的内容作为 Vector 写出，
     * 以保持与早期版本的序列化兼容性。
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        // 不调用 out.defaultWriteObject()

        // 写出 Vector
        Vector<Permission> permissions = new Vector<>(perms.size());
        synchronized (this) {
            permissions.addAll(perms);
        }

        ObjectOutputStream.PutField pfields = out.putFields();
        pfields.put("permissions", permissions);
        out.writeFields();
    }

    /*
     * 读取 FilePermissions 的 Vector 并将其保存在 perms 字段中。
     */
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        // 不调用 defaultReadObject()

        // 读取序列化的字段
        ObjectInputStream.GetField gfields = in.readFields();

        // 获取我们需要的字段
        @SuppressWarnings("unchecked")
        Vector<Permission> permissions = (Vector<Permission>)gfields.get("permissions", null);
        perms = new ArrayList<>(permissions.size());
        for (Permission perm : permissions) {
            perms.add(perm);
        }
    }
}
