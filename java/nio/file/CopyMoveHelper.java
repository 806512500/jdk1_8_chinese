/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.file;

import java.nio.file.attribute.*;
import java.io.InputStream;
import java.io.IOException;

/**
 * 辅助类，用于支持当源文件和目标文件关联不同的提供者时的文件复制或移动操作。
 */

class CopyMoveHelper {
    private CopyMoveHelper() { }

    /**
     * 解析文件复制操作的参数。
     */
    private static class CopyOptions {
        boolean replaceExisting = false;
        boolean copyAttributes = false;
        boolean followLinks = true;

        private CopyOptions() { }

        static CopyOptions parse(CopyOption... options) {
            CopyOptions result = new CopyOptions();
            for (CopyOption option: options) {
                if (option == StandardCopyOption.REPLACE_EXISTING) {
                    result.replaceExisting = true;
                    continue;
                }
                if (option == LinkOption.NOFOLLOW_LINKS) {
                    result.followLinks = false;
                    continue;
                }
                if (option == StandardCopyOption.COPY_ATTRIBUTES) {
                    result.copyAttributes = true;
                    continue;
                }
                if (option == null)
                    throw new NullPointerException();
                throw new UnsupportedOperationException("'" + option +
                    "' is not a recognized copy option");
            }
            return result;
        }
    }

    /**
     * 将给定的文件移动选项数组转换为适合在移动操作实现为复制+删除时使用的选项。
     */
    private static CopyOption[] convertMoveToCopyOptions(CopyOption... options)
        throws AtomicMoveNotSupportedException
    {
        int len = options.length;
        CopyOption[] newOptions = new CopyOption[len+2];
        for (int i=0; i<len; i++) {
            CopyOption option = options[i];
            if (option == StandardCopyOption.ATOMIC_MOVE) {
                throw new AtomicMoveNotSupportedException(null, null,
                    "Atomic move between providers is not supported");
            }
            newOptions[i] = option;
        }
        newOptions[len] = LinkOption.NOFOLLOW_LINKS;
        newOptions[len+1] = StandardCopyOption.COPY_ATTRIBUTES;
        return newOptions;
    }

    /**
     * 当源文件和目标文件关联不同的提供者时使用的简单复制方法。
     */
    static void copyToForeignTarget(Path source, Path target,
                                    CopyOption... options)
        throws IOException
    {
        CopyOptions opts = CopyOptions.parse(options);
        LinkOption[] linkOptions = (opts.followLinks) ? new LinkOption[0] :
            new LinkOption[] { LinkOption.NOFOLLOW_LINKS };

        // 获取源文件的属性
        BasicFileAttributes attrs = Files.readAttributes(source,
                                                         BasicFileAttributes.class,
                                                         linkOptions);
        if (attrs.isSymbolicLink())
            throw new IOException("Copying of symbolic links not supported");

        // 如果目标文件存在且指定了 REPLACE_EXISTING，则删除目标文件
        if (opts.replaceExisting) {
            Files.deleteIfExists(target);
        } else if (Files.exists(target))
            throw new FileAlreadyExistsException(target.toString());

        // 创建目录或复制文件
        if (attrs.isDirectory()) {
            Files.createDirectory(target);
        } else {
            try (InputStream in = Files.newInputStream(source)) {
                Files.copy(in, target);
            }
        }

        // 将基本属性复制到目标文件
        if (opts.copyAttributes) {
            BasicFileAttributeView view =
                Files.getFileAttributeView(target, BasicFileAttributeView.class);
            try {
                view.setTimes(attrs.lastModifiedTime(),
                              attrs.lastAccessTime(),
                              attrs.creationTime());
            } catch (Throwable x) {
                // 回滚
                try {
                    Files.delete(target);
                } catch (Throwable suppressed) {
                    x.addSuppressed(suppressed);
                }
                throw x;
            }
        }
    }

    /**
     * 当源文件和目标文件关联不同的提供者时使用的简单移动方法，实现为复制+删除。
     */
    static void moveToForeignTarget(Path source, Path target,
                                    CopyOption... options) throws IOException
    {
        copyToForeignTarget(source, target, convertMoveToCopyOptions(options));
        Files.delete(source);
    }
}
