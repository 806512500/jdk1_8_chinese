
/*
 * Copyright (c) 1997, 2018, Oracle and/or its affiliates. All rights reserved.
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

package java.util.jar;

import java.io.FilterInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Manifest 类用于维护 Manifest 条目名称及其关联的属性。除了主 Manifest 属性外，还有每个条目的属性。有关 Manifest 格式的详细信息，请参阅
 * <a href="../../../../technotes/guides/jar/jar.html">
 * Manifest 格式规范</a>。
 *
 * @author  David Connelly
 * @see     Attributes
 * @since   1.2
 */
public class Manifest implements Cloneable {
    // manifest 主属性
    private final Attributes attr = new Attributes();

    // manifest 条目
    private final Map<String, Attributes> entries = new HashMap<>();

    // 关联的 JarVerifier，当由 JarFile::getManifest 调用时非空。
    private final JarVerifier jv;

    /**
     * 构造一个新的空 Manifest。
     */
    public Manifest() {
        jv = null;
    }

    /**
     * 从指定的输入流构造一个新的 Manifest。
     *
     * @param is 包含 manifest 数据的输入流
     * @throws IOException 如果发生 I/O 错误
     */
    public Manifest(InputStream is) throws IOException {
        this(null, is);
    }

    /**
     * 从指定的输入流构造一个新的 Manifest，并将其与 JarVerifier 关联。
     */
    Manifest(JarVerifier jv, InputStream is) throws IOException {
        read(is);
        this.jv = jv;
    }

    /**
     * 构造一个新的 Manifest，它是指定 Manifest 的副本。
     *
     * @param man 要复制的 Manifest
     */
    public Manifest(Manifest man) {
        attr.putAll(man.getMainAttributes());
        entries.putAll(man.getEntries());
        jv = man.jv;
    }

    /**
     * 返回 Manifest 的主属性。
     * @return Manifest 的主属性
     */
    public Attributes getMainAttributes() {
        return attr;
    }

    /**
     * 返回此 Manifest 中包含的条目映射。每个条目由一个字符串名称（键）和关联的属性（值）表示。
     * 该映射允许 {@code null} 键，但 {@link #read} 不会创建具有 null 键的条目，使用 {@link #write} 也不会写入这样的条目。
     *
     * @return 包含在此 Manifest 中的条目映射
     */
    public Map<String,Attributes> getEntries() {
        return entries;
    }

    /**
     * 返回指定条目名称的属性。
     * 此方法定义为：
     * <pre>
     *      return (Attributes)getEntries().get(name)
     * </pre>
     * 虽然 {@code null} 是一个有效的 {@code name}，但在从 jar 文件中获取的 {@code Manifest} 上调用 {@code getAttributes(null)} 时，将返回 {@code null}。
     * 虽然 jar 文件本身不允许 {@code null}-named 属性，但可以通过调用 {@link #getEntries} 并在其结果上调用 {@code put} 方法，传入 null 键和任意值，来创建这样的条目。随后调用 {@code getAttributes(null)} 将返回刚刚 {@code put} 的值。
     * <p>
     * 注意，此方法不会返回 manifest 的主属性；请参阅 {@link #getMainAttributes}。
     *
     * @param name 条目名称
     * @return 指定条目名称的属性
     */
    public Attributes getAttributes(String name) {
        return getEntries().get(name);
    }

    /**
     * 如果可信，则返回指定条目名称的属性。
     *
     * @param name 条目名称
     * @return 返回与 {@link #getAttributes(String)} 相同的结果
     * @throws SecurityException 如果关联的 jar 已签名但此条目在签名后被修改（即，manifest 中的该部分在所有签名者的 SF 文件中不存在）。
     */
    Attributes getTrustedAttributes(String name) {
        // 注意：在 MANIFEST.MF/.SF/.RSA 文件的验证完成之前，jv.isTrustedManifestEntry() 无法检测到 MANIFEST.MF 的更改。
        // 使用此方法的用户应首先调用 SharedSecrets.javaUtilJarAccess().ensureInitialization()。
        Attributes result = getAttributes(name);
        if (result != null && jv != null && ! jv.isTrustedManifestEntry(name)) {
            throw new SecurityException("不可信的 manifest 条目: " + name);
        }
        return result;
    }

    /**
     * 清除此 Manifest 的主属性和条目。
     */
    public void clear() {
        attr.clear();
        entries.clear();
    }

    /**
     * 将 Manifest 写入指定的输出流。在调用此方法之前，必须在 MainAttributes 中设置 Attributes.Name.MANIFEST_VERSION。
     *
     * @param out 输出流
     * @exception IOException 如果发生 I/O 错误
     * @see #getMainAttributes
     */
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        // 写入 manifest 的主属性
        attr.writeMain(dos);
        // 现在写入预条目属性
        Iterator<Map.Entry<String, Attributes>> it = entries.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Attributes> e = it.next();
            StringBuffer buffer = new StringBuffer("Name: ");
            String value = e.getKey();
            if (value != null) {
                byte[] vb = value.getBytes("UTF8");
                value = new String(vb, 0, 0, vb.length);
            }
            buffer.append(value);
            buffer.append("\r\n");
            make72Safe(buffer);
            dos.writeBytes(buffer.toString());
            e.getValue().write(dos);
        }
        dos.flush();
    }

    /**
     * 添加换行符以确保每行最多 72 个字节。
     */
    static void make72Safe(StringBuffer line) {
        int length = line.length();
        if (length > 72) {
            int index = 70;
            while (index < length - 2) {
                line.insert(index, "\r\n ");
                index += 72;
                length += 3;
            }
        }
        return;
    }

    /**
     * 从指定的输入流读取 Manifest。读取的条目名称和属性将与当前 manifest 条目合并。
     *
     * @param is 输入流
     * @exception IOException 如果发生 I/O 错误
     */
    public void read(InputStream is) throws IOException {
        // 用于读取 manifest 数据的缓冲输入流
        FastInputStream fis = new FastInputStream(is);
        // 行缓冲区
        byte[] lbuf = new byte[512];
        // 读取 manifest 的主属性
        attr.read(fis, lbuf);
        // 总条目数，属性数
        int ecount = 0, acount = 0;
        // 条目属性的平均大小
        int asize = 2;
        // 现在解析 manifest 条目
        int len;
        String name = null;
        boolean skipEmptyLines = true;
        byte[] lastline = null;

        while ((len = fis.readLine(lbuf)) != -1) {
            if (lbuf[--len] != '\n') {
                throw new IOException("manifest 行太长");
            }
            if (len > 0 && lbuf[len-1] == '\r') {
                --len;
            }
            if (len == 0 && skipEmptyLines) {
                continue;
            }
            skipEmptyLines = false;

            if (name == null) {
                name = parseName(lbuf, len);
                if (name == null) {
                    throw new IOException("无效的 manifest 格式");
                }
                if (fis.peek() == ' ') {
                    // 名称换行
                    lastline = new byte[len - 6];
                    System.arraycopy(lbuf, 6, lastline, 0, len - 6);
                    continue;
                }
            } else {
                // 续行
                byte[] buf = new byte[lastline.length + len - 1];
                System.arraycopy(lastline, 0, buf, 0, lastline.length);
                System.arraycopy(lbuf, 1, buf, lastline.length, len - 1);
                if (fis.peek() == ' ') {
                    // 名称换行
                    lastline = buf;
                    continue;
                }
                name = new String(buf, 0, buf.length, "UTF8");
                lastline = null;
            }
            Attributes attr = getAttributes(name);
            if (attr == null) {
                attr = new Attributes(asize);
                entries.put(name, attr);
            }
            attr.read(fis, lbuf);
            ecount++;
            acount += attr.size();
            //XXX: 修复平均值为 0 的情况。当平均值为 0 时，
            // 会创建一个初始容量为 0 的 Attributes 对象，这会触发 HashMap 的一个 bug。
            asize = Math.max(2, acount / ecount);

            name = null;
            skipEmptyLines = true;
        }
    }

    private String parseName(byte[] lbuf, int len) {
        if (toLower(lbuf[0]) == 'n' && toLower(lbuf[1]) == 'a' &&
            toLower(lbuf[2]) == 'm' && toLower(lbuf[3]) == 'e' &&
            lbuf[4] == ':' && lbuf[5] == ' ') {
            try {
                return new String(lbuf, 6, len - 6, "UTF8");
            }
            catch (Exception e) {
            }
        }
        return null;
    }

    private int toLower(int c) {
        return (c >= 'A' && c <= 'Z') ? 'a' + (c - 'A') : c;
    }

    /**
     * 如果指定的对象也是一个 Manifest 并且具有相同的主属性和条目，则返回 true。
     *
     * @param o 要比较的对象
     * @return 如果指定的对象也是一个 Manifest 并且具有相同的主属性和条目，则返回 true
     */
    public boolean equals(Object o) {
        if (o instanceof Manifest) {
            Manifest m = (Manifest)o;
            return attr.equals(m.getMainAttributes()) &&
                   entries.equals(m.getEntries());
        } else {
            return false;
        }
    }

    /**
     * 返回此 Manifest 的哈希码。
     */
    public int hashCode() {
        return attr.hashCode() + entries.hashCode();
    }

    /**
     * 返回此 Manifest 的浅拷贝。浅拷贝的实现如下：
     * <pre>
     *     public Object clone() { return new Manifest(this); }
     * </pre>
     * @return 此 Manifest 的浅拷贝
     */
    public Object clone() {
        return new Manifest(this);
    }

    /*
     * 用于解析 manifest 文件的快速缓冲输入流。
     */
    static class FastInputStream extends FilterInputStream {
        private byte buf[];
        private int count = 0;
        private int pos = 0;

        FastInputStream(InputStream in) {
            this(in, 8192);
        }

        FastInputStream(InputStream in, int size) {
            super(in);
            buf = new byte[size];
        }

        public int read() throws IOException {
            if (pos >= count) {
                fill();
                if (pos >= count) {
                    return -1;
                }
            }
            return Byte.toUnsignedInt(buf[pos++]);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            int avail = count - pos;
            if (avail <= 0) {
                if (len >= buf.length) {
                    return in.read(b, off, len);
                }
                fill();
                avail = count - pos;
                if (avail <= 0) {
                    return -1;
                }
            }
            if (len > avail) {
                len = avail;
            }
            System.arraycopy(buf, pos, b, off, len);
            pos += len;
            return len;
        }

        /*
         * 从输入流中读取 'len' 个字节，或直到遇到换行符。返回读取的字节数。
         */
        public int readLine(byte[] b, int off, int len) throws IOException {
            byte[] tbuf = this.buf;
            int total = 0;
            while (total < len) {
                int avail = count - pos;
                if (avail <= 0) {
                    fill();
                    avail = count - pos;
                    if (avail <= 0) {
                        return -1;
                    }
                }
                int n = len - total;
                if (n > avail) {
                    n = avail;
                }
                int tpos = pos;
                int maxpos = tpos + n;
                while (tpos < maxpos && tbuf[tpos++] != '\n') ;
                n = tpos - pos;
                System.arraycopy(tbuf, pos, b, off, n);
                off += n;
                total += n;
                pos = tpos;
                if (tbuf[tpos-1] == '\n') {
                    break;
                }
            }
            return total;
        }

        public byte peek() throws IOException {
            if (pos == count)
                fill();
            if (pos == count)
                return -1; // 缓冲区中没有剩余内容
            return buf[pos];
        }

        public int readLine(byte[] b) throws IOException {
            return readLine(b, 0, b.length);
        }

        public long skip(long n) throws IOException {
            if (n <= 0) {
                return 0;
            }
            long avail = count - pos;
            if (avail <= 0) {
                return in.skip(n);
            }
            if (n > avail) {
                n = avail;
            }
            pos += n;
            return n;
        }

        public int available() throws IOException {
            return (count - pos) + in.available();
        }

        public void close() throws IOException {
            if (in != null) {
                in.close();
                in = null;
                buf = null;
            }
        }

        private void fill() throws IOException {
            count = pos = 0;
            int n = in.read(buf, 0, buf.length);
            if (n > 0) {
                count = n;
            }
        }
    }
}
