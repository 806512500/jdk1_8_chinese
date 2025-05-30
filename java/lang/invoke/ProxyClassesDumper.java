/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.lang.invoke;

import sun.util.logging.PlatformLogger;

import java.io.FilePermission;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 由 InnerClassLambdaMetafactory 用于记录生成的类的辅助类。
 *
 * @implNote
 * <p> 因为这个类被 LambdaMetafactory 调用，使用 lambda 会导致递归调用，从而引起堆栈溢出。
 */
final class ProxyClassesDumper {
    private static final char[] HEX = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    private static final char[] BAD_CHARS = {
        '\\', ':', '*', '?', '"', '<', '>', '|'
    };
    private static final String[] REPLACEMENT = {
        "%5C", "%3A", "%2A", "%3F", "%22", "%3C", "%3E", "%7C"
    };

    private final Path dumpDir;

    public static ProxyClassesDumper getInstance(String path) {
        if (null == path) {
            return null;
        }
        try {
            path = path.trim();
            final Path dir = Paths.get(path.length() == 0 ? "." : path);
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        validateDumpDir(dir);
                        return null;
                    }
                }, null, new FilePermission("<<ALL FILES>>", "read, write"));
            return new ProxyClassesDumper(dir);
        } catch (InvalidPathException ex) {
            PlatformLogger.getLogger(ProxyClassesDumper.class.getName())
                          .warning("路径 " + path + " 无效 - 禁用转储", ex);
        } catch (IllegalArgumentException iae) {
            PlatformLogger.getLogger(ProxyClassesDumper.class.getName())
                          .warning(iae.getMessage() + " - 禁用转储");
        }
        return null;
    }

    private ProxyClassesDumper(Path path) {
        dumpDir = Objects.requireNonNull(path);
    }

    private static void validateDumpDir(Path path) {
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("目录 " + path + " 不存在");
        } else if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("路径 " + path + " 不是目录");
        } else if (!Files.isWritable(path)) {
            throw new IllegalArgumentException("目录 " + path + " 不可写");
        }
    }

    public static String encodeForFilename(String className) {
        final int len = className.length();
        StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            char c = className.charAt(i);
            // 控制字符
            if (c <= 31) {
                sb.append('%');
                sb.append(HEX[c >> 4 & 0x0F]);
                sb.append(HEX[c & 0x0F]);
            } else {
                int j = 0;
                for (; j < BAD_CHARS.length; j++) {
                    if (c == BAD_CHARS[j]) {
                        sb.append(REPLACEMENT[j]);
                        break;
                    }
                }
                if (j >= BAD_CHARS.length) {
                    sb.append(c);
                }
            }
        }

        return sb.toString();
    }

    public void dumpClass(String className, final byte[] classBytes) {
        Path file;
        try {
            file = dumpDir.resolve(encodeForFilename(className) + ".class");
        } catch (InvalidPathException ex) {
            PlatformLogger.getLogger(ProxyClassesDumper.class.getName())
                          .warning("类 " + className + " 的路径无效");
            return;
        }

        try {
            Path dir = file.getParent();
            Files.createDirectories(dir);
            Files.write(file, classBytes);
        } catch (Exception ignore) {
            PlatformLogger.getLogger(ProxyClassesDumper.class.getName())
                          .warning("写入路径 " + file.toString() + " 时发生异常");
            // 如果此操作失败，不予理会
        }
    }
}
