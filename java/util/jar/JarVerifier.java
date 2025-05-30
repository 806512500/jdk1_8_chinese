
/*
 * Copyright (c) 1997, 2024, Oracle and/or its affiliates. All rights reserved.
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
import java.net.URL;
import java.util.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.zip.ZipEntry;

import sun.misc.JarIndex;
import sun.security.util.ManifestDigester;
import sun.security.util.ManifestEntryVerifier;
import sun.security.util.SignatureFileVerifier;
import sun.security.util.Debug;

/**
 *
 * @author      Roland Schemers
 */
class JarVerifier {

    public static final String MULTIPLE_MANIFEST_WARNING =
            "警告: 发现多个 MANIFEST.MF。将 JAR 文件视为未签名。";

    /* 是否调试？ */
    static final Debug debug = Debug.getInstance("jar");

    /* 一个将名称映射到代码签名者的表，用于已验证实际哈希值的 JAR 条目 */
    private Hashtable<String, CodeSigner[]> verifiedSigners;

    /* 一个将名称映射到代码签名者的表，用于已通过 .SF/.DSA/.EC -> MANIFEST 检查的 JAR 条目 */
    private Hashtable<String, CodeSigner[]> sigFileSigners;

    /* 一个用于存储 .SF 字节的哈希表 */
    private Hashtable<String, byte[]> sigFileData;

    /** "队列"，包含我们无法解析的待处理 PKCS7 块，直到解析 .SF 文件 */
    private ArrayList<SignatureFileVerifier> pendingBlocks;

    /* 代码签名者对象的缓存 */
    private ArrayList<CodeSigner[]> signerCache;

    /* 是否正在解析块？ */
    private boolean parsingBlockOrSF = false;

    /* 是否已完成解析 META-INF 条目？ */
    private boolean parsingMeta = true;

    /* 是否有文件需要验证？ */
    private boolean anyToVerify = true;

    /* 用于记录我们感兴趣的文件的输出流 */
    private ByteArrayOutputStream baos;

    /** ManifestDigester 对象 */
    private volatile ManifestDigester manDig;

    /** manDig 对象的字节 */
    byte manifestRawBytes[] = null;

    /** 创建此 JarVerifier 时的清单名称 */
    final String manifestName;

    /** 控制签名验证的急切性 */
    boolean eagerValidation;

    /** 使代码源单例实例对我们来说是唯一的 */
    private Object csdomain = new Object();

    /** 收集 -DIGEST-MANIFEST 值以用于黑名单 */
    private List<Object> manifestDigests;

    /* 一个缓存，将代码签名者映射到用于摘要 JAR 条目的算法，以及这些算法是否被允许 */
    private Map<CodeSigner[], Map<String, Boolean>> signersToAlgs;

    public JarVerifier(String name, byte rawBytes[]) {
        manifestName = name;
        manifestRawBytes = rawBytes;
        sigFileSigners = new Hashtable<>();
        verifiedSigners = new Hashtable<>();
        sigFileData = new Hashtable<>(11);
        pendingBlocks = new ArrayList<>();
        baos = new ByteArrayOutputStream();
        manifestDigests = new ArrayList<>();
        signersToAlgs = new HashMap<>();
    }

    /**
     * 此方法扫描我们正在解析的条目，并根据解析的文件类型保持各种状态信息。
     */
    public void beginEntry(JarEntry je, ManifestEntryVerifier mev)
        throws IOException
    {
        if (je == null)
            return;

        if (debug != null) {
            debug.println("beginEntry " + je.getName());
        }

        String name = je.getName();

        /*
         * 假设：
         * 1. 清单应该是 META-INF 目录中的第一个条目。
         * 2. .SF/.DSA/.EC 文件在清单之后出现，但在任何正常条目之前。
         * 3. 以下任何情况都会抛出 SecurityException：
         *    a. 清单部分与 SF 部分之间的摘要不匹配。
         *    b. 实际 JAR 条目与清单之间的摘要不匹配。
         */

        if (parsingMeta) {
            String uname = name.toUpperCase(Locale.ENGLISH);
            if ((uname.startsWith("META-INF/") ||
                 uname.startsWith("/META-INF/"))) {

                if (je.isDirectory()) {
                    mev.setEntry(null, je);
                    return;
                }

                if (uname.equals(JarFile.MANIFEST_NAME) ||
                        uname.equals(JarIndex.INDEX_NAME)) {
                    return;
                }

                if (SignatureFileVerifier.isBlockOrSF(uname)) {
                    /* 我们只解析 DSA、RSA 或 EC PKCS7 块。 */
                    parsingBlockOrSF = true;
                    baos.reset();
                    mev.setEntry(null, je);
                    return;
                }

                // 如果 META-INF 条目不是 MF 或块或 SF，它们应该是正常条目。根据上述 2，不会再出现块或 SF。让我们完成 META。
            }
        }

        if (parsingMeta) {
            doneWithMeta();
        }

        if (je.isDirectory()) {
            mev.setEntry(null, je);
            return;
        }

        // 宽容地接受。如果名称以 ./ 开头，删除它，因为我们内部将其规范化为没有 ./。
        if (name.startsWith("./"))
            name = name.substring(2);

        // 宽容地接受。如果名称以 / 开头，删除它，因为我们内部将其规范化为没有 /。
        if (name.startsWith("/"))
            name = name.substring(1);

        // 仅当条目有签名（已验证或未验证）时才设置 jev 对象
        if (!name.equalsIgnoreCase(JarFile.MANIFEST_NAME)) {
            if (sigFileSigners.get(name) != null ||
                    verifiedSigners.get(name) != null) {
                mev.setEntry(name, je);
                return;
            }
        }

        // 不计算此条目的摘要
        mev.setEntry(null, je);

        return;
    }

    /**
     * 更新单个字节。
     */

    public void update(int b, ManifestEntryVerifier mev)
        throws IOException
    {
        if (b != -1) {
            if (parsingBlockOrSF) {
                baos.write(b);
            } else {
                mev.update((byte)b);
            }
        } else {
            processEntry(mev);
        }
    }

    /**
     * 更新字节数组。
     */

    public void update(int n, byte[] b, int off, int len,
                       ManifestEntryVerifier mev)
        throws IOException
    {
        if (n != -1) {
            if (parsingBlockOrSF) {
                baos.write(b, off, n);
            } else {
                mev.update(b, off, n);
            }
        } else {
            processEntry(mev);
        }
    }

    /**
     * 当我们在 read() 方法之一中到达条目的末尾时调用。
     */
    private void processEntry(ManifestEntryVerifier mev)
        throws IOException
    {
        if (!parsingBlockOrSF) {
            JarEntry je = mev.getEntry();
            if ((je != null) && (je.signers == null)) {
                je.signers = mev.verify(verifiedSigners, sigFileSigners,
                                        signersToAlgs);
                je.certs = mapSignersToCertArray(je.signers);
            }
        } else {

            try {
                parsingBlockOrSF = false;

                if (debug != null) {
                    debug.println("processEntry: processing block");
                }

                String uname = mev.getEntry().getName()
                                             .toUpperCase(Locale.ENGLISH);

                if (uname.endsWith(".SF")) {
                    String key = uname.substring(0, uname.length()-3);
                    byte bytes[] = baos.toByteArray();
                    // 添加到 sigFileData 中，以备将来块需要
                    sigFileData.put(key, bytes);
                    // 检查待处理的块，我们现在可以处理
                    // 任何等待此 .SF 文件的块
                    Iterator<SignatureFileVerifier> it = pendingBlocks.iterator();
                    while (it.hasNext()) {
                        SignatureFileVerifier sfv = it.next();
                        if (sfv.needSignatureFile(key)) {
                            if (debug != null) {
                                debug.println(
                                 "processEntry: processing pending block");
                            }

                            sfv.setSignatureFile(bytes);
                            sfv.process(sigFileSigners, manifestDigests,
                                    manifestName);
                        }
                    }
                    return;
                }

                // 现在我们正在解析一个签名块文件

                String key = uname.substring(0, uname.lastIndexOf("."));

                if (signerCache == null)
                    signerCache = new ArrayList<>();

                if (manDig == null) {
                    synchronized(manifestRawBytes) {
                        if (manDig == null) {
                            manDig = new ManifestDigester(manifestRawBytes);
                            manifestRawBytes = null;
                        }
                    }
                }

                SignatureFileVerifier sfv =
                  new SignatureFileVerifier(signerCache,
                                            manDig, uname, baos.toByteArray());

                if (sfv.needSignatureFileBytes()) {
                    // 查看我们是否已经解析了外部 .SF 文件
                    byte[] bytes = sigFileData.get(key);

                    if (bytes == null) {
                        // 将此块放入队列以稍后处理
                        // 因为我们还没有 .SF 字节
                        // (uname, block);
                        if (debug != null) {
                            debug.println("adding pending block");
                        }
                        pendingBlocks.add(sfv);
                        return;
                    } else {
                        sfv.setSignatureFile(bytes);
                    }
                }
                sfv.process(sigFileSigners, manifestDigests, manifestName);

            } catch (IOException ioe) {
                // 例如 sun.security.pkcs.ParsingException
                if (debug != null) debug.println("processEntry caught: " + ioe);
                // 忽略并视为未签名
            } catch (SignatureException se) {
                if (debug != null) debug.println("processEntry caught: " + se);
                // 忽略并视为未签名
            } catch (NoSuchAlgorithmException nsae) {
                if (debug != null) debug.println("processEntry caught: " + nsae);
                // 忽略并视为未签名
            } catch (CertificateException ce) {
                if (debug != null) debug.println("processEntry caught: " + ce);
                // 忽略并视为未签名
            }
        }
    }

    /**
     * 返回 JAR 中给定文件的 java.security.cert.Certificate 对象数组。
     * @deprecated
     */
    @Deprecated
    public java.security.cert.Certificate[] getCerts(String name)
    {
        return mapSignersToCertArray(getCodeSigners(name));
    }

    public java.security.cert.Certificate[] getCerts(JarFile jar, JarEntry entry)
    {
        return mapSignersToCertArray(getCodeSigners(jar, entry));
    }

    /**
     * 返回 JAR 中给定文件的 CodeSigner 对象数组。此数组未克隆。
     *
     */
    public CodeSigner[] getCodeSigners(String name)
    {
        return verifiedSigners.get(name);
    }

    public CodeSigner[] getCodeSigners(JarFile jar, JarEntry entry)
    {
        String name = entry.getName();
        if (eagerValidation && sigFileSigners.get(name) != null) {
            /*
             * 强制读取条目数据以生成
             * 验证哈希。
             */
            try {
                InputStream s = jar.getInputStream(entry);
                byte[] buffer = new byte[1024];
                int n = buffer.length;
                while (n != -1) {
                    n = s.read(buffer, 0, buffer.length);
                }
                s.close();
            } catch (IOException e) {
            }
        }
        return getCodeSigners(name);
    }

    /*
     * 将签名者数组转换为连接的证书数组。
     */
    private static java.security.cert.Certificate[] mapSignersToCertArray(
        CodeSigner[] signers) {

        if (signers != null) {
            ArrayList<java.security.cert.Certificate> certChains = new ArrayList<>();
            for (int i = 0; i < signers.length; i++) {
                certChains.addAll(
                    signers[i].getSignerCertPath().getCertificates());
            }

            // 转换为 Certificate[]
            return certChains.toArray(
                    new java.security.cert.Certificate[certChains.size()]);
        }
        return null;
    }

    /**
     * 如果没有文件需要验证，则返回 true。
     * 应在处理完所有 META-INF 条目后调用。
     */
    boolean nothingToVerify()
    {
        return (anyToVerify == false);
    }

    /**
     * 通知我们已处理完所有
     * META-INF 条目，如果重新读取其中一个条目，则不要
     * 重新处理。同时删除解析 META-INF 条目时需要的任何数据结构。
     */
    void doneWithMeta()
    {
        parsingMeta = false;
        anyToVerify = !sigFileSigners.isEmpty();
        baos = null;
        sigFileData = null;
        pendingBlocks = null;
        signerCache = null;
        manDig = null;
        // MANIFEST.MF 始终被视为已签名和已验证，
        // 将其签名者从 sigFileSigners 移动到 verifiedSigners。
        if (sigFileSigners.containsKey(manifestName)) {
            CodeSigner[] codeSigners = sigFileSigners.remove(manifestName);
            verifiedSigners.put(manifestName, codeSigners);
        }
    }


                static class VerifierStream extends java.io.InputStream {

        private InputStream is;
        private JarVerifier jv;
        private ManifestEntryVerifier mev;
        private long numLeft;

        VerifierStream(Manifest man,
                       JarEntry je,
                       InputStream is,
                       JarVerifier jv) throws IOException
        {
            this.is = is;
            this.jv = jv;
            this.mev = new ManifestEntryVerifier(man, jv.manifestName);
            this.jv.beginEntry(je, mev);
            this.numLeft = je.getSize();
            if (this.numLeft == 0)
                this.jv.update(-1, this.mev);
        }

        public int read() throws IOException
        {
            if (numLeft > 0) {
                int b = is.read();
                jv.update(b, mev);
                numLeft--;
                if (numLeft == 0)
                    jv.update(-1, mev);
                return b;
            } else {
                return -1;
            }
        }

        public int read(byte b[], int off, int len) throws IOException {
            if ((numLeft > 0) && (numLeft < len)) {
                len = (int)numLeft;
            }

            if (numLeft > 0) {
                int n = is.read(b, off, len);
                jv.update(n, b, off, len, mev);
                numLeft -= n;
                if (numLeft == 0)
                    jv.update(-1, b, off, len, mev);
                return n;
            } else {
                return -1;
            }
        }

        public void close()
            throws IOException
        {
            if (is != null)
                is.close();
            is = null;
            mev = null;
            jv = null;
        }

        public int available() throws IOException {
            return is.available();
        }

    }

    // 扩展 JavaUtilJarAccess CodeSource API 支持

    private Map<URL, Map<CodeSigner[], CodeSource>> urlToCodeSourceMap = new HashMap<>();
    private Map<CodeSigner[], CodeSource> signerToCodeSource = new HashMap<>();
    private URL lastURL;
    private Map<CodeSigner[], CodeSource> lastURLMap;

    /*
     * 从代码签名者缓存条目创建到 CodeSource 的唯一映射。
     * 理论上，多个 URL 源可以映射到单个本地缓存的共享 JAR 文件，尽管在实际中通常只有一个 URL 在使用。
     */
    private synchronized CodeSource mapSignersToCodeSource(URL url, CodeSigner[] signers) {
        Map<CodeSigner[], CodeSource> map;
        if (url == lastURL) {
            map = lastURLMap;
        } else {
            map = urlToCodeSourceMap.get(url);
            if (map == null) {
                map = new HashMap<>();
                urlToCodeSourceMap.put(url, map);
            }
            lastURLMap = map;
            lastURL = url;
        }
        CodeSource cs = map.get(signers);
        if (cs == null) {
            cs = new VerifierCodeSource(csdomain, url, signers);
            signerToCodeSource.put(signers, cs);
        }
        return cs;
    }

    private CodeSource[] mapSignersToCodeSources(URL url, List<CodeSigner[]> signers, boolean unsigned) {
        List<CodeSource> sources = new ArrayList<>();

        for (int i = 0; i < signers.size(); i++) {
            sources.add(mapSignersToCodeSource(url, signers.get(i)));
        }
        if (unsigned) {
            sources.add(mapSignersToCodeSource(url, null));
        }
        return sources.toArray(new CodeSource[sources.size()]);
    }
    private CodeSigner[] emptySigner = new CodeSigner[0];

    /*
     * 匹配 CodeSource 到签名者缓存中的 CodeSigner[]。
     */
    private CodeSigner[] findMatchingSigners(CodeSource cs) {
        if (cs instanceof VerifierCodeSource) {
            VerifierCodeSource vcs = (VerifierCodeSource) cs;
            if (vcs.isSameDomain(csdomain)) {
                return ((VerifierCodeSource) cs).getPrivateSigners();
            }
        }

        /*
         * 实际上签名者应该总是被优化的
         * 但这里处理任何类型的 CodeSource，以防万一。
         */
        CodeSource[] sources = mapSignersToCodeSources(cs.getLocation(), getJarCodeSigners(), true);
        List<CodeSource> sourceList = new ArrayList<>();
        for (int i = 0; i < sources.length; i++) {
            sourceList.add(sources[i]);
        }
        int j = sourceList.indexOf(cs);
        if (j != -1) {
            CodeSigner[] match;
            match = ((VerifierCodeSource) sourceList.get(j)).getPrivateSigners();
            if (match == null) {
                match = emptySigner;
            }
            return match;
        }
        return null;
    }

    /*
     * 该类的实例持有对内部签名数据的未复制引用，可以通过对象引用身份进行比较。
     */
    private static class VerifierCodeSource extends CodeSource {
        private static final long serialVersionUID = -9047366145967768825L;

        URL vlocation;
        CodeSigner[] vsigners;
        java.security.cert.Certificate[] vcerts;
        Object csdomain;

        VerifierCodeSource(Object csdomain, URL location, CodeSigner[] signers) {
            super(location, signers);
            this.csdomain = csdomain;
            vlocation = location;
            vsigners = signers; // 从签名者缓存中获取
        }

        VerifierCodeSource(Object csdomain, URL location, java.security.cert.Certificate[] certs) {
            super(location, certs);
            this.csdomain = csdomain;
            vlocation = location;
            vcerts = certs; // 从签名者缓存中获取
        }

        /*
         * 所有 VerifierCodeSource 实例都是基于每个唯一签名者的签名者缓存或签名者缓存证书条目的单例构建的。
         * 不需要 CodeSigner<->Certificate[] 转换。我们使用这些假设来优化等式比较。
         */
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof VerifierCodeSource) {
                VerifierCodeSource that = (VerifierCodeSource) obj;

                /*
                 * 仅与其他为同一 JarFile 实例构建的每个签名者单例进行比较。否则，以较慢的方式进行比较。
                 */
                if (isSameDomain(that.csdomain)) {
                    if (that.vsigners != this.vsigners
                            || that.vcerts != this.vcerts) {
                        return false;
                    }
                    if (that.vlocation != null) {
                        return that.vlocation.equals(this.vlocation);
                    } else if (this.vlocation != null) {
                        return this.vlocation.equals(that.vlocation);
                    } else { // 两者都为 null
                        return true;
                    }
                }
            }
            return super.equals(obj);
        }

        boolean isSameDomain(Object csdomain) {
            return this.csdomain == csdomain;
        }

        private CodeSigner[] getPrivateSigners() {
            return vsigners;
        }

        private java.security.cert.Certificate[] getPrivateCertificates() {
            return vcerts;
        }
    }
    private Map<String, CodeSigner[]> signerMap;

    private synchronized Map<String, CodeSigner[]> signerMap() {
        if (signerMap == null) {
            /*
             * 拍照签名者状态，使其不会改变。我们只关心断言的签名。签名有效性的验证通过 JarEntry API 发生。
             */
            signerMap = new HashMap<>(verifiedSigners.size() + sigFileSigners.size());
            signerMap.putAll(verifiedSigners);
            signerMap.putAll(sigFileSigners);
        }
        return signerMap;
    }

    public synchronized Enumeration<String> entryNames(JarFile jar, final CodeSource[] cs) {
        final Map<String, CodeSigner[]> map = signerMap();
        final Iterator<Map.Entry<String, CodeSigner[]>> itor = map.entrySet().iterator();
        boolean matchUnsigned = false;

        /*
         * 获取签名者数组的单个副本。检查是否可以优化 CodeSigner 等式测试。
         */
        List<CodeSigner[]> req = new ArrayList<>(cs.length);
        for (int i = 0; i < cs.length; i++) {
            CodeSigner[] match = findMatchingSigners(cs[i]);
            if (match != null) {
                if (match.length > 0) {
                    req.add(match);
                } else {
                    matchUnsigned = true;
                }
            } else {
                matchUnsigned = true;
            }
        }

        final List<CodeSigner[]> signersReq = req;
        final Enumeration<String> enum2 = (matchUnsigned) ? unsignedEntryNames(jar) : emptyEnumeration;

        return new Enumeration<String>() {

            String name;

            public boolean hasMoreElements() {
                if (name != null) {
                    return true;
                }

                while (itor.hasNext()) {
                    Map.Entry<String, CodeSigner[]> e = itor.next();
                    if (signersReq.contains(e.getValue())) {
                        name = e.getKey();
                        return true;
                    }
                }
                while (enum2.hasMoreElements()) {
                    name = enum2.nextElement();
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

    /*
     * 类似于 entries()，但筛选出内部 JAR 机制条目，并包括没有 ZIP 数据的已签名条目。
     */
    public Enumeration<JarEntry> entries2(final JarFile jar, Enumeration<? extends ZipEntry> e) {
        final Map<String, CodeSigner[]> map = new HashMap<>();
        map.putAll(signerMap());
        final Enumeration<? extends ZipEntry> enum_ = e;
        return new Enumeration<JarEntry>() {

            Enumeration<String> signers = null;
            JarEntry entry;

            public boolean hasMoreElements() {
                if (entry != null) {
                    return true;
                }
                while (enum_.hasMoreElements()) {
                    ZipEntry ze = enum_.nextElement();
                    if (JarVerifier.isSigningRelated(ze.getName())) {
                        continue;
                    }
                    entry = jar.newEntry(ze);
                    return true;
                }
                if (signers == null) {
                    signers = Collections.enumeration(map.keySet());
                }
                while (signers.hasMoreElements()) {
                    String name = signers.nextElement();
                    entry = jar.newEntry(new ZipEntry(name));
                    return true;
                }

                // 任何 map 条目剩余？
                return false;
            }

            public JarEntry nextElement() {
                if (hasMoreElements()) {
                    JarEntry je = entry;
                    map.remove(je.getName());
                    entry = null;
                    return je;
                }
                throw new NoSuchElementException();
            }
        };
    }
    private Enumeration<String> emptyEnumeration = new Enumeration<String>() {

        public boolean hasMoreElements() {
            return false;
        }

        public String nextElement() {
            throw new NoSuchElementException();
        }
    };

    // 如果文件是签名机制的一部分，则返回 true
    static boolean isSigningRelated(String name) {
        return SignatureFileVerifier.isSigningRelated(name);
    }

    private Enumeration<String> unsignedEntryNames(JarFile jar) {
        final Map<String, CodeSigner[]> map = signerMap();
        final Enumeration<JarEntry> entries = jar.entries();
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
                    if (e.isDirectory() || isSigningRelated(value)) {
                        continue;
                    }
                    if (map.get(value) == null) {
                        name = value;
                        return true;
                    }
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
    private List<CodeSigner[]> jarCodeSigners;

    private synchronized List<CodeSigner[]> getJarCodeSigners() {
        CodeSigner[] signers;
        if (jarCodeSigners == null) {
            HashSet<CodeSigner[]> set = new HashSet<>();
            set.addAll(signerMap().values());
            jarCodeSigners = new ArrayList<>();
            jarCodeSigners.addAll(set);
        }
        return jarCodeSigners;
    }

    public synchronized CodeSource[] getCodeSources(JarFile jar, URL url) {
        boolean hasUnsigned = unsignedEntryNames(jar).hasMoreElements();

        return mapSignersToCodeSources(url, getJarCodeSigners(), hasUnsigned);
    }

    public CodeSource getCodeSource(URL url, String name) {
        CodeSigner[] signers;

        signers = signerMap().get(name);
        return mapSignersToCodeSource(url, signers);
    }

    public CodeSource getCodeSource(URL url, JarFile jar, JarEntry je) {
        CodeSigner[] signers;

        return mapSignersToCodeSource(url, getCodeSigners(jar, je));
    }

    public void setEagerValidation(boolean eager) {
        eagerValidation = eager;
    }

    public synchronized List<Object> getManifestDigests() {
        return Collections.unmodifiableList(manifestDigests);
    }

    static CodeSource getUnsignedCS(URL url) {
        return new VerifierCodeSource(null, url, (java.security.cert.Certificate[]) null);
    }

    /**
     * 返回名称是否受信任。用于
     * {@link Manifest#getTrustedAttributes(String)}。
     */
    boolean isTrustedManifestEntry(String name) {
        // 有多少签名者？MANIFEST.MF 总是被验证
        CodeSigner[] forMan = verifiedSigners.get(manifestName);
        if (forMan == null) {
            return true;
        }
        // 首先检查 sigFileSigners，因为我们主要处理的是非文件条目，这些条目将永远保留在 sigFileSigners 中。
        CodeSigner[] forName = sigFileSigners.get(name);
        if (forName == null) {
            forName = verifiedSigners.get(name);
        }
        // 如果所有签名者都签名了该条目，则返回受信任
        return forName != null && forName.length == forMan.length;
    }
}
