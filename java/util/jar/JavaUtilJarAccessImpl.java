/*
 * 版权所有 (c) 2002, 2020, Oracle 和/或其附属公司。保留所有权利。
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

package java.util.jar;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.List;
import sun.misc.JavaUtilJarAccess;

class JavaUtilJarAccessImpl implements JavaUtilJarAccess {
    public boolean jarFileHasClassPathAttribute(JarFile jar) throws IOException {
        return jar.hasClassPathAttribute();
    }

    public CodeSource[] getCodeSources(JarFile jar, URL url) {
        return jar.getCodeSources(url);
    }

    public CodeSource getCodeSource(JarFile jar, URL url, String name) {
        return jar.getCodeSource(url, name);
    }

    public Enumeration<String> entryNames(JarFile jar, CodeSource[] cs) {
        return jar.entryNames(cs);
    }

    public Enumeration<JarEntry> entries2(JarFile jar) {
        return jar.entries2();
    }

    public void setEagerValidation(JarFile jar, boolean eager) {
        jar.setEagerValidation(eager);
    }

    public List<Object> getManifestDigests(JarFile jar) {
        return jar.getManifestDigests();
    }

    public Attributes getTrustedAttributes(Manifest man, String name) {
        return man.getTrustedAttributes(name);
    }

    public void ensureInitialization(JarFile jar) {
        jar.ensureInitialization();
    }

    public boolean isInitializing() {
        return JarFile.isInitializing();
    }
}
