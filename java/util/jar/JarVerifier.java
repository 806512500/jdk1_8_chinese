
/*
 * Copyright (c) 1997, 2022, Oracle and/or its affiliates. All rights reserved.
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

    /* Are we debugging ? */
    static final Debug debug = Debug.getInstance("jar");

    /* a table mapping names to code signers, for jar entries that have
       had their actual hashes verified */
    private Hashtable<String, CodeSigner[]> verifiedSigners;

    /* a table mapping names to code signers, for jar entries that have
       passed the .SF/.DSA/.EC -> MANIFEST check */
    private Hashtable<String, CodeSigner[]> sigFileSigners;

    /* a hash table to hold .SF bytes */
    private Hashtable<String, byte[]> sigFileData;

    /** "queue" of pending PKCS7 blocks that we couldn't parse
     *  until we parsed the .SF file */
    private ArrayList<SignatureFileVerifier> pendingBlocks;

    /* cache of CodeSigner objects */
    private ArrayList<CodeSigner[]> signerCache;

    /* Are we parsing a block? */
    private boolean parsingBlockOrSF = false;

    /* Are we done parsing META-INF entries? */
    private boolean parsingMeta = true;

    /* Are there are files to verify? */
    private boolean anyToVerify = true;

    /* The output stream to use when keeping track of files we are interested
       in */
    private ByteArrayOutputStream baos;

    /** The ManifestDigester object */
    private volatile ManifestDigester manDig;

    /** the bytes for the manDig object */
    byte manifestRawBytes[] = null;

    /** the manifest name this JarVerifier is created upon */
    final String manifestName;

    /** controls eager signature validation */
    boolean eagerValidation;

    /** makes code source singleton instances unique to us */
    private Object csdomain = new Object();

    /** collect -DIGEST-MANIFEST values for blacklist */
    private List<Object> manifestDigests;

    /* A cache mapping code signers to the algorithms used to digest jar
       entries, and whether or not the algorithms are permitted. */
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
     * This method scans to see which entry we're parsing and
     * keeps various state information depending on what type of
     * file is being parsed.
     */
    public void beginEntry(JarEntry je, ManifestEntryVerifier mev)
        throws IOException
    {
        if (je == null)
            return;

        if (debug != null) {
            debug.println("beginEntry "+je.getName());
        }

        String name = je.getName();

        /*
         * Assumptions:
         * 1. The manifest should be the first entry in the META-INF directory.
         * 2. The .SF/.DSA/.EC files follow the manifest, before any normal entries
         * 3. Any of the following will throw a SecurityException:
         *    a. digest mismatch between a manifest section and
         *       the SF section.
         *    b. digest mismatch between the actual jar entry and the manifest
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
                    /* We parse only DSA, RSA or EC PKCS7 blocks. */
                    parsingBlockOrSF = true;
                    baos.reset();
                    mev.setEntry(null, je);
                    return;
                }

                // If a META-INF entry is not MF or block or SF, they should
                // be normal entries. According to 2 above, no more block or
                // SF will appear. Let's doneWithMeta.
            }
        }

        if (parsingMeta) {
            doneWithMeta();
        }

        if (je.isDirectory()) {
            mev.setEntry(null, je);
            return;
        }

        // be liberal in what you accept. If the name starts with ./, remove
        // it as we internally canonicalize it with out the ./.
        if (name.startsWith("./"))
            name = name.substring(2);

        // be liberal in what you accept. If the name starts with /, remove
        // it as we internally canonicalize it with out the /.
        if (name.startsWith("/"))
            name = name.substring(1);

        // only set the jev object for entries that have a signature
        // (either verified or not)
        if (!name.equalsIgnoreCase(JarFile.MANIFEST_NAME)) {
            if (sigFileSigners.get(name) != null ||
                    verifiedSigners.get(name) != null) {
                mev.setEntry(name, je);
                return;
            }
        }

        // don't compute the digest for this entry
        mev.setEntry(null, je);

        return;
    }

    /**
     * update a single byte.
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
     * 更新一个字节数组。
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
     * 在 read() 方法之一中到达条目末尾时调用。
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
                    // 添加到 sigFileData 以备将来块需要
                    sigFileData.put(key, bytes);
                    // 检查待处理的块，我们现在可以处理
                    // 等待此 .SF 文件的任何人
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
                        // 将此块放入队列以供稍后处理
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
                if (debug != null) debug.println("processEntry caught: "+ioe);
                // 忽略并视为未签名
            } catch (SignatureException se) {
                if (debug != null) debug.println("processEntry caught: "+se);
                // 忽略并视为未签名
            } catch (NoSuchAlgorithmException nsae) {
                if (debug != null) debug.println("processEntry caught: "+nsae);
                // 忽略并视为未签名
            } catch (CertificateException ce) {
                if (debug != null) debug.println("processEntry caught: "+ce);
                // 忽略并视为未签名
            }
        }
    }

    /**
     * 返回 jar 中给定文件的 java.security.cert.Certificate 对象数组。
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
     * 返回 jar 中给定文件的 CodeSigner 对象数组。此数组不会被克隆。
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
     * 将签名者数组转换为证书数组的连接数组。
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
     * 通知我们已处理完所有 META-INF 条目，如果重新读取其中一个条目，不要重新处理。
     * 同时释放解析 META-INF 条目时所需的数据结构。
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
        // MANIFEST.MF 始终被视为已签名和验证，
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
     * 从 codeSigner 缓存条目创建到 CodeSource 的唯一映射。
     * 理论上，多个 URL 源可以映射到单个本地缓存和共享的 JAR 文件，尽管实际上只会使用一个 URL。
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
     * 匹配 CodeSource 到 signer 缓存中的 CodeSigner[]。
     */
    private CodeSigner[] findMatchingSigners(CodeSource cs) {
        if (cs instanceof VerifierCodeSource) {
            VerifierCodeSource vcs = (VerifierCodeSource) cs;
            if (vcs.isSameDomain(csdomain)) {
                return ((VerifierCodeSource) cs).getPrivateSigners();
            }
        }

        /*
         * 实际上，签名者应始终在上述过程中进行优化，
         * 但这是为了处理任何类型的 CodeSource，以防万一。
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
     * 该类的实例持有内部签名数据的未复制引用，可以通过对象引用身份进行比较。
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
            vsigners = signers; // 从 signerCache 获取
        }

        VerifierCodeSource(Object csdomain, URL location, java.security.cert.Certificate[] certs) {
            super(location, certs);
            this.csdomain = csdomain;
            vlocation = location;
            vcerts = certs; // 从 signerCache 获取
        }

        /*
         * 所有的 VerifierCodeSource 实例都是基于每个唯一签名者的单例 signerCache 或 signerCacheCert 条目构建的。
         * 不需要进行 CodeSigner<->Certificate[] 转换。
         * 我们使用这些假设来优化相等性比较。
         */
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof VerifierCodeSource) {
                VerifierCodeSource that = (VerifierCodeSource) obj;

                /*
                 * 仅与其他为相同 JarFile 实例构建的每个签名者单例进行比较。否则，以较慢的方式进行比较。
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
             * 捕获签名者状态的快照，以防止其发生变化。我们只关心断言的签名。签名有效性的验证通过 JarEntry API 进行。
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
         * 获取 CodeSigner 数组的单个副本。检查是否可以优化 CodeSigner 相等性测试。
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


                            // 还有映射条目吗？
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
        // 首先检查 sigFileSigners，因为我们主要处理的是
        // 非文件条目，这些条目将永远保留在 sigFileSigners 中。
        CodeSigner[] forName = sigFileSigners.get(name);
        if (forName == null) {
            forName = verifiedSigners.get(name);
        }
        // 如果所有签名者都签名了该条目，则返回受信任
        return forName != null && forName.length == forMan.length;
    }
}
