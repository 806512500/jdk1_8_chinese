
/*
 * Copyright (c) 1997, 2021, Oracle and/or its affiliates. All rights reserved.
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

package java.util.jar;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.AbstractSet;
import java.util.Iterator;
import sun.util.logging.PlatformLogger;
import java.util.Comparator;
import sun.misc.ASCIICaseInsensitiveComparator;

/**
 * Attributes 类将 Manifest 属性名称映射到关联的字符串值。有效的属性名称是不区分大小写的，限制为 ASCII 字符集 [0-9a-zA-Z_-] 中的字符，并且长度不能超过 70 个字符。属性值可以包含任何字符，并在写入输出流时进行 UTF8 编码。有关有效属性名称和值的更多信息，请参阅
 * <a href="../../../../technotes/guides/jar/jar.html">JAR 文件规范</a>。
 *
 * @author  David Connelly
 * @see     Manifest
 * @since   1.2
 */
public class Attributes implements Map<Object,Object>, Cloneable {
    /**
     * 属性名称-值映射。
     */
    protected Map<Object,Object> map;

    /**
     * 构造一个新的、空的 Attributes 对象，默认大小。
     */
    public Attributes() {
        this(11);
    }

    /**
     * 构造一个新的、空的 Attributes 对象，指定初始大小。
     *
     * @param size 属性的初始数量
     */
    public Attributes(int size) {
        map = new HashMap<>(size);
    }

    /**
     * 构造一个新的 Attributes 对象，具有与指定 Attributes 相同的属性名称-值映射。
     *
     * @param attr 指定的 Attributes
     */
    public Attributes(Attributes attr) {
        map = new HashMap<>(attr);
    }


    /**
     * 返回指定属性名称的值，如果未找到属性名称，则返回 null。
     *
     * @param name 属性名称
     * @return 指定属性名称的值，如果未找到则返回 null。
     */
    public Object get(Object name) {
        return map.get(name);
    }

    /**
     * 返回指定为字符串的属性名称的值，如果未找到属性，则返回 null。属性名称不区分大小写。
     * <p>
     * 此方法定义为：
     * <pre>
     *      return (String)get(new Attributes.Name((String)name));
     * </pre>
     *
     * @param name 作为字符串的属性名称
     * @return 指定属性名称的字符串值，如果未找到则返回 null。
     * @throws IllegalArgumentException 如果属性名称无效
     */
    public String getValue(String name) {
        return (String)get(new Attributes.Name(name));
    }

    /**
     * 返回指定 Attributes.Name 的值，如果未找到属性，则返回 null。
     * <p>
     * 此方法定义为：
     * <pre>
     *     return (String)get(name);
     * </pre>
     *
     * @param name Attributes.Name 对象
     * @return 指定 Attribute.Name 的字符串值，如果未找到则返回 null。
     */
    public String getValue(Name name) {
        return (String)get(name);
    }

    /**
     * 在此 Map 中将指定的值与指定的属性名称（键）关联。如果 Map 之前包含属性名称的映射，则旧值将被替换。
     *
     * @param name 属性名称
     * @param value 属性值
     * @return 属性的先前值，如果没有则返回 null
     * @exception ClassCastException 如果名称不是 Attributes.Name 或值不是 String
     */
    public Object put(Object name, Object value) {
        return map.put((Attributes.Name)name, (String)value);
    }

    /**
     * 将指定的值与指定为字符串的属性名称关联。属性名称不区分大小写。如果 Map 之前包含属性名称的映射，则旧值将被替换。
     * <p>
     * 此方法定义为：
     * <pre>
     *      return (String)put(new Attributes.Name(name), value);
     * </pre>
     *
     * @param name 作为字符串的属性名称
     * @param value 属性值
     * @return 属性的先前值，如果没有则返回 null
     * @exception IllegalArgumentException 如果属性名称无效
     */
    public String putValue(String name, String value) {
        return (String)put(new Name(name), value);
    }

    /**
     * 从此 Map 中移除指定名称（键）的属性。返回先前的属性值，如果没有则返回 null。
     *
     * @param name 属性名称
     * @return 属性的先前值，如果没有则返回 null
     */
    public Object remove(Object name) {
        return map.remove(name);
    }

    /**
     * 如果此 Map 将一个或多个属性名称（键）映射到指定的值，则返回 true。
     *
     * @param value 属性值
     * @return 如果此 Map 将一个或多个属性名称映射到指定的值，则返回 true
     */
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * 如果此 Map 包含指定的属性名称（键），则返回 true。
     *
     * @param name 属性名称
     * @return 如果此 Map 包含指定的属性名称，则返回 true
     */
    public boolean containsKey(Object name) {
        return map.containsKey(name);
    }

    /**
     * 将指定 Attributes 中的所有属性名称-值映射复制到此 Map。重复的映射将被替换。
     *
     * @param attr 要存储在此映射中的 Attributes
     * @exception ClassCastException 如果 attr 不是 Attributes
     */
    public void putAll(Map<?,?> attr) {
        // ## javac bug?
        if (!Attributes.class.isInstance(attr))
            throw new ClassCastException();
        for (Map.Entry<?,?> me : (attr).entrySet())
            put(me.getKey(), me.getValue());
    }


                /**
     * 从此映射中移除所有属性。
     */
    public void clear() {
        map.clear();
    }

    /**
     * 返回此映射中的属性数量。
     */
    public int size() {
        return map.size();
    }

    /**
     * 如果此映射不包含任何属性，则返回 true。
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * 返回一个包含此映射中的属性名称（键）的 Set 视图。
     */
    public Set<Object> keySet() {
        return map.keySet();
    }

    /**
     * 返回一个包含此映射中的属性值的 Collection 视图。
     */
    public Collection<Object> values() {
        return map.values();
    }

    /**
     * 返回一个包含此映射中的属性名称-值映射的 Collection 视图。
     */
    public Set<Map.Entry<Object,Object>> entrySet() {
        return map.entrySet();
    }

    /**
     * 将指定的 Attributes 对象与此映射进行相等性比较。
     * 如果给定的对象也是一个 Attributes 实例，并且两个 Attributes 对象表示相同的映射，则返回 true。
     *
     * @param o 要比较的对象
     * @return 如果指定的对象与此映射相等，则返回 true
     */
    public boolean equals(Object o) {
        return map.equals(o);
    }

    /**
     * 返回此映射的哈希码值。
     */
    public int hashCode() {
        return map.hashCode();
    }

    /**
     * 返回 Attributes 的副本，实现如下：
     * <pre>
     *     public Object clone() { return new Attributes(this); }
     * </pre>
     * 由于属性名称和值本身是不可变的，因此返回的 Attributes 可以安全地修改，而不会影响原始对象。
     */
    public Object clone() {
        return new Attributes(this);
    }

    /*
     * 将当前属性写入指定的数据输出流。
     * XXX 需要处理 UTF8 值并拆分超过 72 字节的行
     */
     void write(DataOutputStream os) throws IOException {
        Iterator<Map.Entry<Object, Object>> it = entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> e = it.next();
            StringBuffer buffer = new StringBuffer(
                                        ((Name)e.getKey()).toString());
            buffer.append(": ");

            String value = (String)e.getValue();
            if (value != null) {
                byte[] vb = value.getBytes("UTF8");
                value = new String(vb, 0, 0, vb.length);
            }
            buffer.append(value);

            buffer.append("\r\n");
            Manifest.make72Safe(buffer);
            os.writeBytes(buffer.toString());
        }
        os.writeBytes("\r\n");
    }

    /*
     * 将当前属性写入指定的数据输出流，确保首先写入 MANIFEST_VERSION 或 SIGNATURE_VERSION 属性。
     *
     * XXX 需要处理 UTF8 值并拆分超过 72 字节的行
     */
    void writeMain(DataOutputStream out) throws IOException
    {
        // 如果存在，首先写入 *-Version 标头
        String vername = Name.MANIFEST_VERSION.toString();
        String version = getValue(vername);
        if (version == null) {
            vername = Name.SIGNATURE_VERSION.toString();
            version = getValue(vername);
        }

        if (version != null) {
            out.writeBytes(vername+": "+version+"\r\n");
        }

        // 写入除我们之前写入的版本之外的所有属性
        Iterator<Map.Entry<Object, Object>> it = entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> e = it.next();
            String name = ((Name)e.getKey()).toString();
            if ((version != null) && ! (name.equalsIgnoreCase(vername))) {

                StringBuffer buffer = new StringBuffer(name);
                buffer.append(": ");

                String value = (String)e.getValue();
                if (value != null) {
                    byte[] vb = value.getBytes("UTF8");
                    value = new String(vb, 0, 0, vb.length);
                }
                buffer.append(value);

                buffer.append("\r\n");
                Manifest.make72Safe(buffer);
                out.writeBytes(buffer.toString());
            }
        }
        out.writeBytes("\r\n");
    }

    /*
     * 从指定的输入流中读取属性。
     * XXX 需要处理 UTF8 值。
     */
    void read(Manifest.FastInputStream is, byte[] lbuf) throws IOException {
        String name = null, value = null;
        ByteArrayOutputStream fullLine = new ByteArrayOutputStream();

        int len;
        while ((len = is.readLine(lbuf)) != -1) {
            boolean lineContinued = false;
            if (lbuf[--len] != '\n') {
                throw new IOException("line too long");
            }
            if (len > 0 && lbuf[len-1] == '\r') {
                --len;
            }
            if (len == 0) {
                break;
            }
            int i = 0;
            if (lbuf[0] == ' ') {
                // 上一行的延续
                if (name == null) {
                    throw new IOException("misplaced continuation line");
                }
                lineContinued = true;
                fullLine.write(lbuf, 1, len - 1);
                if (is.peek() == ' ') {
                    continue;
                }
                value = fullLine.toString("UTF8");
                fullLine.reset();
            } else {
                while (lbuf[i++] != ':') {
                    if (i >= len) {
                        throw new IOException("invalid header field");
                    }
                }
                if (lbuf[i++] != ' ') {
                    throw new IOException("invalid header field");
                }
                name = new String(lbuf, 0, 0, i - 2);
                if (is.peek() == ' ') {
                    fullLine.reset();
                    fullLine.write(lbuf, i, len - i);
                    continue;
                }
                value = new String(lbuf, i, len - i, "UTF8");
            }
            try {
                if ((putValue(name, value) != null) && (!lineContinued)) {
                    PlatformLogger.getLogger("java.util.jar").warning(
                                     "Duplicate name in Manifest: " + name
                                     + ".\n"
                                     + "Ensure that the manifest does not "
                                     + "have duplicate entries, and\n"
                                     + "that blank lines separate "
                                     + "individual sections in both your\n"
                                     + "manifest and in the META-INF/MANIFEST.MF "
                                     + "entry in the jar file.");
                }
            } catch (IllegalArgumentException e) {
                throw new IOException("invalid header field name: " + name);
            }
        }
    }


                /**
     * Attributes.Name 类表示存储在此 Map 中的属性名称。有效的属性名称不区分大小写，仅限于 ASCII 字符集 [0-9a-zA-Z_-] 中的字符，且长度不得超过 70 个字符。属性值可以包含任何字符，并在写入输出流时进行 UTF8 编码。有关有效属性名称和值的更多信息，请参阅 <a href="../../../../technotes/guides/jar/jar.html">JAR 文件规范</a>。
     */
    public static class Name {
        private String name;
        private int hashCode = -1;

        /**
         * 使用给定的字符串名称构造一个新的属性名称。
         *
         * @param name 属性字符串名称
         * @exception IllegalArgumentException 如果属性名称无效
         * @exception NullPointerException 如果属性名称为 null
         */
        public Name(String name) {
            if (name == null) {
                throw new NullPointerException("name");
            }
            if (!isValid(name)) {
                throw new IllegalArgumentException(name);
            }
            this.name = name.intern();
        }

        private static boolean isValid(String name) {
            int len = name.length();
            if (len > 70 || len == 0) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                if (!isValid(name.charAt(i))) {
                    return false;
                }
            }
            return true;
        }

        private static boolean isValid(char c) {
            return isAlpha(c) || isDigit(c) || c == '_' || c == '-';
        }

        private static boolean isAlpha(char c) {
            return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
        }

        private static boolean isDigit(char c) {
            return c >= '0' && c <= '9';
        }

        /**
         * 将此属性名称与另一个进行比较以确定是否相等。
         * @param o 要比较的对象
         * @return 如果此属性名称等于指定的属性对象，则返回 true
         */
        public boolean equals(Object o) {
            if (o instanceof Name) {
                Comparator<String> c = ASCIICaseInsensitiveComparator.CASE_INSENSITIVE_ORDER;
                return c.compare(name, ((Name)o).name) == 0;
            } else {
                return false;
            }
        }

        /**
         * 计算此属性名称的哈希值。
         */
        public int hashCode() {
            if (hashCode == -1) {
                hashCode = ASCIICaseInsensitiveComparator.lowerCaseHashCode(name);
            }
            return hashCode;
        }

        /**
         * 返回属性名称的字符串形式。
         */
        public String toString() {
            return name;
        }

        /**
         * <code>Manifest-Version</code> 清单属性的 <code>Name</code> 对象。此属性表示 JAR 文件的清单符合的清单标准的版本号。
         * @see <a href="../../../../technotes/guides/jar/jar.html#JAR_Manifest">
         *      清单和签名规范</a>
         */
        public static final Name MANIFEST_VERSION = new Name("Manifest-Version");

        /**
         * 签名 JAR 文件时使用的 <code>Signature-Version</code> 清单属性的 <code>Name</code> 对象。
         * @see <a href="../../../../technotes/guides/jar/jar.html#JAR_Manifest">
         *      清单和签名规范</a>
         */
        public static final Name SIGNATURE_VERSION = new Name("Signature-Version");

        /**
         * <code>Content-Type</code> 清单属性的 <code>Name</code> 对象。
         */
        public static final Name CONTENT_TYPE = new Name("Content-Type");

        /**
         * <code>Class-Path</code> 清单属性的 <code>Name</code> 对象。捆绑扩展可以使用此属性查找包含所需类的其他 JAR 文件。
         * @see <a href="../../../../technotes/guides/jar/jar.html#classpath">
         *      JAR 文件规范</a>
         */
        public static final Name CLASS_PATH = new Name("Class-Path");

        /**
         * 用于启动打包在 JAR 文件中的应用程序的 <code>Main-Class</code> 清单属性的 <code>Name</code> 对象。 <code>Main-Class</code> 属性与 <code>-jar</code> 命令行选项一起使用，用于 <tt>java</tt> 应用程序启动器。
         */
        public static final Name MAIN_CLASS = new Name("Main-Class");

        /**
         * 用于密封的 <code>Sealed</code> 清单属性的 <code>Name</code> 对象。
         * @see <a href="../../../../technotes/guides/jar/jar.html#sealing">
         *      包密封</a>
         */
        public static final Name SEALED = new Name("Sealed");

       /**
         * 用于声明对已安装扩展的依赖关系的 <code>Extension-List</code> 清单属性的 <code>Name</code> 对象。
         * @see <a href="../../../../technotes/guides/extensions/spec.html#dependency">
         *      已安装扩展依赖关系</a>
         */
        public static final Name EXTENSION_LIST = new Name("Extension-List");

        /**
         * 用于声明对已安装扩展的依赖关系的 <code>Extension-Name</code> 清单属性的 <code>Name</code> 对象。
         * @see <a href="../../../../technotes/guides/extensions/spec.html#dependency">
         *      已安装扩展依赖关系</a>
         */
        public static final Name EXTENSION_NAME = new Name("Extension-Name");

        /**
         * 用于声明对已安装扩展的依赖关系的 <code>Extension-Name</code> 清单属性的 <code>Name</code> 对象。
         * @deprecated 扩展机制将在未来的版本中移除。请改用类路径。
         * @see <a href="../../../../technotes/guides/extensions/spec.html#dependency">
         *      已安装扩展依赖关系</a>
         */
        @Deprecated
        public static final Name EXTENSION_INSTALLATION = new Name("Extension-Installation");


                    /**
         * <code>Name</code> 对象用于 <code>Implementation-Title</code>
         * 清单属性，用于包版本控制。
         * @see <a href="../../../../technotes/guides/versioning/spec/versioning2.html#wp90779">
         *      Java 产品版本控制规范</a>
         */
        public static final Name IMPLEMENTATION_TITLE = new Name("Implementation-Title");

        /**
         * <code>Name</code> 对象用于 <code>Implementation-Version</code>
         * 清单属性，用于包版本控制。
         * @see <a href="../../../../technotes/guides/versioning/spec/versioning2.html#wp90779">
         *      Java 产品版本控制规范</a>
         */
        public static final Name IMPLEMENTATION_VERSION = new Name("Implementation-Version");

        /**
         * <code>Name</code> 对象用于 <code>Implementation-Vendor</code>
         * 清单属性，用于包版本控制。
         * @see <a href="../../../../technotes/guides/versioning/spec/versioning2.html#wp90779">
         *      Java 产品版本控制规范</a>
         */
        public static final Name IMPLEMENTATION_VENDOR = new Name("Implementation-Vendor");

        /**
         * <code>Name</code> 对象用于 <code>Implementation-Vendor-Id</code>
         * 清单属性，用于包版本控制。
         * @deprecated 扩展机制将在未来的版本中移除。
         *             请使用类路径。
         * @see <a href="../../../../technotes/guides/extensions/versioning.html#applet">
         *      可选包版本控制</a>
         */
        @Deprecated
        public static final Name IMPLEMENTATION_VENDOR_ID = new Name("Implementation-Vendor-Id");

       /**
         * <code>Name</code> 对象用于 <code>Implementation-URL</code>
         * 清单属性，用于包版本控制。
         * @deprecated 扩展机制将在未来的版本中移除。
         *             请使用类路径。
         * @see <a href="../../../../technotes/guides/extensions/versioning.html#applet">
         *      可选包版本控制</a>
         */
        @Deprecated
        public static final Name IMPLEMENTATION_URL = new Name("Implementation-URL");

        /**
         * <code>Name</code> 对象用于 <code>Specification-Title</code>
         * 清单属性，用于包版本控制。
         * @see <a href="../../../../technotes/guides/versioning/spec/versioning2.html#wp90779">
         *      Java 产品版本控制规范</a>
         */
        public static final Name SPECIFICATION_TITLE = new Name("Specification-Title");

        /**
         * <code>Name</code> 对象用于 <code>Specification-Version</code>
         * 清单属性，用于包版本控制。
         * @see <a href="../../../../technotes/guides/versioning/spec/versioning2.html#wp90779">
         *      Java 产品版本控制规范</a>
         */
        public static final Name SPECIFICATION_VERSION = new Name("Specification-Version");

        /**
         * <code>Name</code> 对象用于 <code>Specification-Vendor</code>
         * 清单属性，用于包版本控制。
         * @see <a href="../../../../technotes/guides/versioning/spec/versioning2.html#wp90779">
         *      Java 产品版本控制规范</a>
         */
        public static final Name SPECIFICATION_VENDOR = new Name("Specification-Vendor");
    }
}
