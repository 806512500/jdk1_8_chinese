/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.file;

import java.nio.file.attribute.BasicFileAttributes;
import java.io.IOException;
import java.util.Objects;

/**
 * 一个简单的文件访问者，具有默认行为，访问所有文件并重新抛出 I/O 错误。
 *
 * <p> 本类中的方法可以根据其通用合同进行重写。
 *
 * @param   <T>     文件的引用类型
 *
 * @since 1.7
 */

public class SimpleFileVisitor<T> implements FileVisitor<T> {
    /**
     * 初始化此类的新实例。
     */
    protected SimpleFileVisitor() {
    }

    /**
     * 在访问目录中的条目之前调用。
     *
     * <p> 除非被重写，此方法返回 {@link FileVisitResult#CONTINUE
     * CONTINUE}。
     */
    @Override
    public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs)
        throws IOException
    {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(attrs);
        return FileVisitResult.CONTINUE;
    }

    /**
     * 调用目录中的文件。
     *
     * <p> 除非被重写，此方法返回 {@link FileVisitResult#CONTINUE
     * CONTINUE}。
     */
    @Override
    public FileVisitResult visitFile(T file, BasicFileAttributes attrs)
        throws IOException
    {
        Objects.requireNonNull(file);
        Objects.requireNonNull(attrs);
        return FileVisitResult.CONTINUE;
    }

    /**
     * 调用无法访问的文件。
     *
     * <p> 除非被重写，此方法重新抛出导致文件无法访问的 I/O 异常。
     */
    @Override
    public FileVisitResult visitFileFailed(T file, IOException exc)
        throws IOException
    {
        Objects.requireNonNull(file);
        throw exc;
    }

    /**
     * 在访问目录及其所有后代条目之后调用。
     *
     * <p> 除非被重写，如果目录迭代在没有 I/O 异常的情况下完成，此方法返回 {@link FileVisitResult#CONTINUE
     * CONTINUE}；否则，此方法重新抛出导致目录迭代提前终止的 I/O 异常。
     */
    @Override
    public FileVisitResult postVisitDirectory(T dir, IOException exc)
        throws IOException
    {
        Objects.requireNonNull(dir);
        if (exc != null)
            throw exc;
        return FileVisitResult.CONTINUE;
    }
}
