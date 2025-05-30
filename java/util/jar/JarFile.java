
/*
 * Copyright (c) 1997, 2023, Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.*;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.security.AccessController;
import java.security.CodeSource;
import sun.misc.IOUtils;
import sun.security.action.GetPropertyAction;
import sun.security.util.ManifestEntryVerifier;
import sun.misc.SharedSecrets;
import sun.security.util.SignatureFileVerifier;

/**
 * <code>JarFile</code> 类用于从任何可以使用 <code>java.io.RandomAccessFile</code> 打开的文件中读取 jar 文件的内容。
 * 它扩展了 <code>java.util.zip.ZipFile</code> 类，支持读取可选的 <code>Manifest</code> 条目。该 <code>Manifest</code>
 * 可用于指定关于 jar 文件及其条目的元信息。
 *
 * <p>除非另有说明，向此类的构造函数或方法传递 <tt>null</tt> 参数将导致抛出 {@link NullPointerException}。
 *
 * 如果在打开已签名的 jar 文件时启用了验证标志，则文件内容将根据嵌入文件中的签名进行验证。请注意，验证过程不包括验证签名者的证书。
 * 调用者应检查 {@link JarEntry#getCodeSigners()} 的返回值，以进一步确定签名是否可信。
 *
 * @author  David Connelly
 * @see     Manifest
 * @see     java.util.zip.ZipFile
 * @see     java.util.jar.JarEntry
 * @since   1.2
 */
public
class JarFile extends ZipFile {
    private SoftReference<Manifest> manRef;
    private JarEntry manEntry;
    private JarVerifier jv;
    private boolean jvInitialized;
    private boolean verify;

    // 指示是否存在 Class-Path 属性（仅在 hasCheckedSpecialAttributes 为 true 时有效）
    private boolean hasClassPathAttribute;
    // 表示是否已检查清单中的特殊属性
    private volatile boolean hasCheckedSpecialAttributes;

    // 在 SharedSecrets 中设置 JavaUtilJarAccess
    static {
        SharedSecrets.setJavaUtilJarAccess(new JavaUtilJarAccessImpl());
    }

    private static final sun.misc.JavaUtilZipFileAccess JUZFA =
            sun.misc.SharedSecrets.getJavaUtilZipFileAccess();

    /**
     * JAR 清单文件名。
     */
    public static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";

    /**
     * 创建一个新的 <code>JarFile</code> 以从指定的文件 <code>name</code> 读取。如果该文件已签名，则将对其进行验证。
     * @param name 要打开以进行读取的 jar 文件的名称
     * @throws IOException 如果发生 I/O 错误
     * @throws SecurityException 如果 SecurityManager 拒绝访问文件
     */
    public JarFile(String name) throws IOException {
        this(new File(name), true, ZipFile.OPEN_READ);
    }

    /**
     * 创建一个新的 <code>JarFile</code> 以从指定的文件 <code>name</code> 读取。
     * @param name 要打开以进行读取的 jar 文件的名称
     * @param verify 如果文件已签名，是否进行验证
     * @throws IOException 如果发生 I/O 错误
     * @throws SecurityException 如果 SecurityManager 拒绝访问文件
     */
    public JarFile(String name, boolean verify) throws IOException {
        this(new File(name), verify, ZipFile.OPEN_READ);
    }

    /**
     * 创建一个新的 <code>JarFile</code> 以从指定的 <code>File</code> 对象读取。如果该文件已签名，则将对其进行验证。
     * @param file 要打开以进行读取的 jar 文件
     * @throws IOException 如果发生 I/O 错误
     * @throws SecurityException 如果 SecurityManager 拒绝访问文件
     */
    public JarFile(File file) throws IOException {
        this(file, true, ZipFile.OPEN_READ);
    }


    /**
     * 创建一个新的 <code>JarFile</code> 以从指定的 <code>File</code> 对象读取。
     * @param file 要打开以进行读取的 jar 文件
     * @param verify 如果文件已签名，是否进行验证
     * @throws IOException 如果发生 I/O 错误
     * @throws SecurityException 如果 SecurityManager 拒绝访问文件
     */
    public JarFile(File file, boolean verify) throws IOException {
        this(file, verify, ZipFile.OPEN_READ);
    }


    /**
     * 创建一个新的 <code>JarFile</code> 以在指定模式下从指定的 <code>File</code> 对象读取。模式参数必须是 <tt>OPEN_READ</tt>
     * 或 <tt>OPEN_READ | OPEN_DELETE</tt>。
     *
     * @param file 要打开以进行读取的 jar 文件
     * @param verify 如果文件已签名，是否进行验证
     * @param mode 打开文件的模式
     * @throws IOException 如果发生 I/O 错误
     * @throws IllegalArgumentException 如果 <tt>mode</tt> 参数无效
     * @throws SecurityException 如果 SecurityManager 拒绝访问文件
     * @since 1.3
     */
    public JarFile(File file, boolean verify, int mode) throws IOException {
        super(file, mode);
        this.verify = verify;
    }

    /**
     * 返回 jar 文件的清单，如果没有则返回 <code>null</code>。
     *
     * @return jar 文件的清单，如果没有则返回 <code>null</code>
     *
     * @throws IllegalStateException 如果 jar 文件已关闭，可能会抛出此异常
     * @throws IOException 如果发生 I/O 错误
     */
    public Manifest getManifest() throws IOException {
        return getManifestFromReference();
    }

    private Manifest getManifestFromReference() throws IOException {
        Manifest man = manRef != null ? manRef.get() : null;

        if (man == null) {

            JarEntry manEntry = getManEntry();

            // 如果找到则加载清单
            if (manEntry != null) {
                if (verify) {
                    byte[] b = getBytes(manEntry);
                    if (!jvInitialized) {
                        if (JUZFA.getManifestNum(this) == 1) {
                            jv = new JarVerifier(manEntry.getName(), b);
                        } else {
                            if (JarVerifier.debug != null) {
                                JarVerifier.debug.println(
                                        JarVerifier.MULTIPLE_MANIFEST_WARNING);
                            }
                        }
                    }
                    man = new Manifest(jv, new ByteArrayInputStream(b));
                } else {
                    man = new Manifest(super.getInputStream(manEntry));
                }
                manRef = new SoftReference<>(man);
            }
        }
        return man;
    }

    private native String[] getMetaInfEntryNames();

    /**
     * 返回给定条目名称的 <code>JarEntry</code>，如果未找到则返回 <code>null</code>。
     *
     * @param name jar 文件条目名称
     * @return 给定条目名称的 <code>JarEntry</code>，如果未找到则返回 <code>null</code>
     *
     * @throws IllegalStateException 如果 jar 文件已关闭，可能会抛出此异常
     *
     * @see java.util.jar.JarEntry
     */
    public JarEntry getJarEntry(String name) {
        return (JarEntry)getEntry(name);
    }

    /**
     * 返回给定条目名称的 <code>ZipEntry</code>，如果未找到则返回 <code>null</code>。
     *
     * @param name jar 文件条目名称
     * @return 给定条目名称的 <code>ZipEntry</code>，如果未找到则返回 <code>null</code>
     *
     * @throws IllegalStateException 如果 jar 文件已关闭，可能会抛出此异常
     *
     * @see java.util.zip.ZipEntry
     */
    public ZipEntry getEntry(String name) {
        ZipEntry ze = super.getEntry(name);
        if (ze != null) {
            return new JarFileEntry(ze);
        }
        return null;
    }

    private class JarEntryIterator implements Enumeration<JarEntry>,
            Iterator<JarEntry>
    {
        final Enumeration<? extends ZipEntry> e = JarFile.super.entries();

        public boolean hasNext() {
            return e.hasMoreElements();
        }

        public JarEntry next() {
            ZipEntry ze = e.nextElement();
            return new JarFileEntry(ze);
        }

        public boolean hasMoreElements() {
            return hasNext();
        }

        public JarEntry nextElement() {
            return next();
        }
    }

    /**
     * 返回 zip 文件条目的枚举。
     */
    public Enumeration<JarEntry> entries() {
        return new JarEntryIterator();
    }

    @Override
    public Stream<JarEntry> stream() {
        return StreamSupport.stream(Spliterators.spliterator(
                new JarEntryIterator(), size(),
                Spliterator.ORDERED | Spliterator.DISTINCT |
                        Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
    }

    private class JarFileEntry extends JarEntry {
        JarFileEntry(ZipEntry ze) {
            super(ze);
        }
        public Attributes getAttributes() throws IOException {
            Manifest man = JarFile.this.getManifest();
            if (man != null) {
                return man.getAttributes(getName());
            } else {
                return null;
            }
        }
        public Certificate[] getCertificates() {
            try {
                maybeInstantiateVerifier();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (certs == null && jv != null) {
                certs = jv.getCerts(JarFile.this, this);
            }
            return certs == null ? null : certs.clone();
        }
        public CodeSigner[] getCodeSigners() {
            try {
                maybeInstantiateVerifier();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (signers == null && jv != null) {
                signers = jv.getCodeSigners(JarFile.this, this);
            }
            return signers == null ? null : signers.clone();
        }
    }

    /*
     * 确保如果需要（即，jar 文件似乎是已签名的）则创建 JarVerifier 对象。这作为一个快速检查，以避免处理未签名 jar 文件的清单。
     */
    private void maybeInstantiateVerifier() throws IOException {
        if (jv != null) {
            return;
        }

        if (verify) {
            String[] names = getMetaInfEntryNames();
            if (names != null) {
                for (int i = 0; i < names.length; i++) {
                    String name = names[i].toUpperCase(Locale.ENGLISH);
                    if (name.endsWith(".DSA") ||
                        name.endsWith(".RSA") ||
                        name.endsWith(".EC") ||
                        name.endsWith(".SF")) {
                        // 假设由于找到了签名相关的文件，jar 文件是已签名的，因此需要 JarVerifier 和 Manifest
                        getManifest();
                        return;
                    }
                }
            }
            // 没有签名相关的文件；不实例化验证器
            verify = false;
        }
    }


    /*
     * 通过读取所有清单条目并将它们传递给验证器来初始化验证器对象。
     */
    private void initializeVerifier() {
        ManifestEntryVerifier mev = null;

        // 验证 "META-INF/" 条目...
        try {
            String[] names = getMetaInfEntryNames();
            if (names != null) {
                for (int i = 0; i < names.length; i++) {
                    String uname = names[i].toUpperCase(Locale.ENGLISH);
                    if (MANIFEST_NAME.equals(uname)
                            || SignatureFileVerifier.isBlockOrSF(uname)) {
                        JarEntry e = getJarEntry(names[i]);
                        if (e == null) {
                            throw new JarException("corrupted jar file");
                        }
                        if (mev == null) {
                            mev = new ManifestEntryVerifier
                                (getManifestFromReference(), jv.manifestName);
                        }
                        byte[] b = getBytes(e);
                        if (b != null && b.length > 0) {
                            jv.beginEntry(e, mev);
                            jv.update(b.length, b, 0, b.length, mev);
                            jv.update(-1, null, 0, 0, mev);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            // 如果在解析任何块时发生错误，仅将 jar 文件视为未签名
            jv = null;
            verify = false;
            if (JarVerifier.debug != null) {
                JarVerifier.debug.println("jarfile parsing error!");
                ex.printStackTrace();
            }
        }

        // 如果在初始化验证器后没有任何已签名的内容，我们将其置为 null。

        if (jv != null) {

            jv.doneWithMeta();
            if (JarVerifier.debug != null) {
                JarVerifier.debug.println("done with meta!");
            }

            if (jv.nothingToVerify()) {
                if (JarVerifier.debug != null) {
                    JarVerifier.debug.println("nothing to verify!");
                }
                jv = null;
                verify = false;
            }
        }
    }

    /*
     * 读取给定条目的所有字节。用于处理 META-INF 文件。
     */
    private byte[] getBytes(ZipEntry ze) throws IOException {
        try (InputStream is = super.getInputStream(ze)) {
            long uncompressedSize = ze.getSize();
            if (uncompressedSize > SignatureFileVerifier.MAX_SIG_FILE_SIZE) {
                throw new IOException("Unsupported size: " + uncompressedSize +
                        " for JarEntry " + ze.getName() +
                        ". Allowed max size: " +
                        SignatureFileVerifier.MAX_SIG_FILE_SIZE + " bytes. " +
                        "You can use the jdk.jar.maxSignatureFileSize " +
                        "system property to increase the default value.");
            }
            int len = (int)uncompressedSize;
            byte[] b = IOUtils.readAllBytes(is);
            if (len != -1 && b.length != len)
                throw new EOFException("Expected:" + len + ", read:" + b.length);


                        return b;
        }
    }

    /**
     * 返回一个输入流，用于读取指定的 zip 文件条目的内容。
     * @param ze zip 文件条目
     * @return 一个输入流，用于读取指定的 zip 文件条目的内容
     * @throws ZipException 如果发生 zip 文件格式错误
     * @throws IOException 如果发生 I/O 错误
     * @throws SecurityException 如果任何 jar 文件条目签名不正确
     * @throws IllegalStateException 如果 jar 文件已关闭
     */
    public synchronized InputStream getInputStream(ZipEntry ze)
        throws IOException
    {
        maybeInstantiateVerifier();
        if (jv == null) {
            return super.getInputStream(ze);
        }
        if (!jvInitialized) {
            initializeVerifier();
            jvInitialized = true;
            // 可能在调用 initializeVerifier 后被设置为 null，如果没有任何需要验证的内容
            if (jv == null)
                return super.getInputStream(ze);
        }

        // 在真实流周围包装一个验证器流
        return new JarVerifier.VerifierStream(
            getManifestFromReference(),
            ze instanceof JarFileEntry ?
            (JarEntry) ze : getJarEntry(ze.getName()),
            super.getInputStream(ze),
            jv);
    }

    // 用于手编码的 Boyer-Moore 搜索的静态变量
    private static final char[] CLASSPATH_CHARS = {'c','l','a','s','s','-','p','a','t','h'};
    // "class-path" 的坏字符移位
    private static final int[] CLASSPATH_LASTOCC;
    // "class-path" 的好后缀移位
    private static final int[] CLASSPATH_OPTOSFT;

    static {
        CLASSPATH_LASTOCC = new int[128];
        CLASSPATH_OPTOSFT = new int[10];
        CLASSPATH_LASTOCC[(int)'c'] = 1;
        CLASSPATH_LASTOCC[(int)'l'] = 2;
        CLASSPATH_LASTOCC[(int)'s'] = 5;
        CLASSPATH_LASTOCC[(int)'-'] = 6;
        CLASSPATH_LASTOCC[(int)'p'] = 7;
        CLASSPATH_LASTOCC[(int)'a'] = 8;
        CLASSPATH_LASTOCC[(int)'t'] = 9;
        CLASSPATH_LASTOCC[(int)'h'] = 10;
        for (int i=0; i<9; i++)
            CLASSPATH_OPTOSFT[i] = 10;
        CLASSPATH_OPTOSFT[9]=1;
    }

    private JarEntry getManEntry() {
        if (manEntry == null) {
            // 首先使用标准名称查找清单条目
            manEntry = getJarEntry(MANIFEST_NAME);
            if (manEntry == null) {
                // 如果未找到，则遍历所有 "META-INF/" 条目以找到匹配项。
                String[] names = getMetaInfEntryNames();
                if (names != null) {
                    for (int i = 0; i < names.length; i++) {
                        if (MANIFEST_NAME.equals(
                                                 names[i].toUpperCase(Locale.ENGLISH))) {
                            manEntry = getJarEntry(names[i]);
                            break;
                        }
                    }
                }
            }
        }
        return manEntry;
    }

   /**
    * 如果此 JAR 文件的清单包含 Class-Path 属性，则返回 {@code true}
    */
    boolean hasClassPathAttribute() throws IOException {
        checkForSpecialAttributes();
        return hasClassPathAttribute;
    }

    /**
     * 如果在 {@code b} 中找到模式 {@code src}，则返回 true。
     * {@code lastOcc} 和 {@code optoSft} 数组是预计算的坏字符和好后缀移位。
     */
    private boolean match(char[] src, byte[] b, int[] lastOcc, int[] optoSft) {
        int len = src.length;
        int last = b.length - len;
        int i = 0;
        next:
        while (i<=last) {
            for (int j=(len-1); j>=0; j--) {
                char c = (char) b[i+j];
                c = (((c-'A')|('Z'-c)) >= 0) ? (char)(c + 32) : c;
                if (c != src[j]) {
                    i += Math.max(j + 1 - lastOcc[c&0x7F], optoSft[j]);
                    continue next;
                 }
            }
            return true;
        }
        return false;
    }

    /**
     * 在首次调用时，检查 JAR 文件是否具有 Class-Path 属性。后续调用时为无操作。
     */
    private void checkForSpecialAttributes() throws IOException {
        if (hasCheckedSpecialAttributes) return;
        if (!isKnownNotToHaveSpecialAttributes()) {
            JarEntry manEntry = getManEntry();
            if (manEntry != null) {
                byte[] b = getBytes(manEntry);
                if (match(CLASSPATH_CHARS, b, CLASSPATH_LASTOCC, CLASSPATH_OPTOSFT))
                    hasClassPathAttribute = true;
            }
        }
        hasCheckedSpecialAttributes = true;
    }

    private static String javaHome;
    private static volatile String[] jarNames;
    private boolean isKnownNotToHaveSpecialAttributes() {
        // 优化掉我们提供的不包含 class-path 属性的 jar 文件的清单扫描。如果这些 jar 文件中的一个被更改以包含此类属性，则必须更改此代码。
        if (javaHome == null) {
            javaHome = AccessController.doPrivileged(
                new GetPropertyAction("java.home"));
        }
        if (jarNames == null) {
            String[] names = new String[11];
            String fileSep = File.separator;
            int i = 0;
            names[i++] = fileSep + "rt.jar";
            names[i++] = fileSep + "jsse.jar";
            names[i++] = fileSep + "jce.jar";
            names[i++] = fileSep + "charsets.jar";
            names[i++] = fileSep + "dnsns.jar";
            names[i++] = fileSep + "zipfs.jar";
            names[i++] = fileSep + "localedata.jar";
            names[i++] = fileSep = "cldrdata.jar";
            names[i++] = fileSep + "sunjce_provider.jar";
            names[i++] = fileSep + "sunpkcs11.jar";
            names[i++] = fileSep + "sunec.jar";
            jarNames = names;
        }

        String name = getName();
        String localJavaHome = javaHome;
        if (name.startsWith(localJavaHome)) {
            String[] names = jarNames;
            for (int i = 0; i < names.length; i++) {
                if (name.endsWith(names[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    synchronized void ensureInitialization() {
        try {
            maybeInstantiateVerifier();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (jv != null && !jvInitialized) {
            initializeVerifier();
            jvInitialized = true;
        }
    }

    JarEntry newEntry(ZipEntry ze) {
        return new JarFileEntry(ze);
    }

    Enumeration<String> entryNames(CodeSource[] cs) {
        ensureInitialization();
        if (jv != null) {
            return jv.entryNames(this, cs);
        }

        /*
         * JAR 文件没有签名内容。是否有非签名的代码源？
         */
        boolean includeUnsigned = false;
        for (int i = 0; i < cs.length; i++) {
            if (cs[i].getCodeSigners() == null) {
                includeUnsigned = true;
                break;
            }
        }
        if (includeUnsigned) {
            return unsignedEntryNames();
        } else {
            return new Enumeration<String>() {

                public boolean hasMoreElements() {
                    return false;
                }

                public String nextElement() {
                    throw new NoSuchElementException();
                }
            };
        }
    }

    /**
     * 返回一个 zip 文件条目的枚举，排除内部 JAR 机制条目，并包括 ZIP 目录中缺失的已签名条目。
     */
    Enumeration<JarEntry> entries2() {
        ensureInitialization();
        if (jv != null) {
            return jv.entries2(this, super.entries());
        }

        // 筛选出永远不会签名的条目
        final Enumeration<? extends ZipEntry> enum_ = super.entries();
        return new Enumeration<JarEntry>() {

            ZipEntry entry;

            public boolean hasMoreElements() {
                if (entry != null) {
                    return true;
                }
                while (enum_.hasMoreElements()) {
                    ZipEntry ze = enum_.nextElement();
                    if (JarVerifier.isSigningRelated(ze.getName())) {
                        continue;
                    }
                    entry = ze;
                    return true;
                }
                return false;
            }

            public JarFileEntry nextElement() {
                if (hasMoreElements()) {
                    ZipEntry ze = entry;
                    entry = null;
                    return new JarFileEntry(ze);
                }
                throw new NoSuchElementException();
            }
        };
    }

    CodeSource[] getCodeSources(URL url) {
        ensureInitialization();
        if (jv != null) {
            return jv.getCodeSources(this, url);
        }

        /*
         * JAR 文件没有签名内容。是否有非签名的代码源？
         */
        Enumeration<String> unsigned = unsignedEntryNames();
        if (unsigned.hasMoreElements()) {
            return new CodeSource[]{JarVerifier.getUnsignedCS(url)};
        } else {
            return null;
        }
    }

    private Enumeration<String> unsignedEntryNames() {
        final Enumeration<JarEntry> entries = entries();
        return new Enumeration<String>() {

            String name;

            /*
             * 从 ZIP 目录中获取条目，但筛选出元数据。
             */
            public boolean hasMoreElements() {
                if (name != null) {
                    return true;
                }
                while (entries.hasMoreElements()) {
                    String value;
                    ZipEntry e = entries.nextElement();
                    value = e.getName();
                    if (e.isDirectory() || JarVerifier.isSigningRelated(value)) {
                        continue;
                    }
                    name = value;
                    return true;
                }
                return false;
            }

            public String nextElement() {
                if (hasMoreElements()) {
                    String value = name;
                    name = null;
                    return value;
                }
                throw new NoSuchElementException();
            }
        };
    }

    CodeSource getCodeSource(URL url, String name) {
        ensureInitialization();
        if (jv != null) {
            if (jv.eagerValidation) {
                CodeSource cs = null;
                JarEntry je = getJarEntry(name);
                if (je != null) {
                    cs = jv.getCodeSource(url, this, je);
                } else {
                    cs = jv.getCodeSource(url, name);
                }
                return cs;
            } else {
                return jv.getCodeSource(url, name);
            }
        }

        return JarVerifier.getUnsignedCS(url);
    }

    void setEagerValidation(boolean eager) {
        try {
            maybeInstantiateVerifier();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (jv != null) {
            jv.setEagerValidation(eager);
        }
    }

    List<Object> getManifestDigests() {
        ensureInitialization();
        if (jv != null) {
            return jv.getManifestDigests();
        }
        return new ArrayList<Object>();
    }
}
