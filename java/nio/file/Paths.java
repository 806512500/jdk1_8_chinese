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

import java.nio.file.spi.FileSystemProvider;
import java.net.URI;

/**
 * 该类仅包含静态方法，这些方法通过转换路径字符串或 {@link URI} 返回一个 {@link Path}。
 *
 * @since 1.7
 */

public final class Paths {
    private Paths() { }

    /**
     * 将路径字符串或一系列字符串（这些字符串连接起来形成一个路径字符串）转换为一个 {@code Path}。如果 {@code more} 没有指定任何元素，则 {@code first} 参数的值是需要转换的路径字符串。如果 {@code more} 指定了一个或多个元素，则每个非空字符串（包括 {@code first}）都被视为一个名称元素序列（参见 {@link Path}），并连接起来形成一个路径字符串。字符串连接的具体方式取决于提供者，但通常会使用 {@link FileSystem#getSeparator 名称分隔符} 作为分隔符。例如，如果名称分隔符是 "{@code /}" 并且调用了 {@code getPath("/foo","bar","gus")}，则路径字符串 {@code "/foo/bar/gus"} 将被转换为一个 {@code Path}。如果 {@code first} 是空字符串且 {@code more} 不包含任何非空字符串，则返回一个表示空路径的 {@code Path}。
     *
     * <p> 通过调用默认 {@link FileSystem} 的 {@link FileSystem#getPath getPath} 方法来获取 {@code Path}。
     *
     * <p> 虽然此方法非常方便，但使用它会隐含地引用默认的 {@code FileSystem}，从而限制了调用代码的通用性。因此，不应在旨在灵活重用的库代码中使用。一个更灵活的替代方案是使用现有的 {@code Path} 实例作为锚点，例如：
     * <pre>
     *     Path dir = ...
     *     Path path = dir.resolve("file");
     * </pre>
     *
     * @param   first
     *          路径字符串或路径字符串的初始部分
     * @param   more
     *          需要连接起来形成路径字符串的其他字符串
     *
     * @return  结果的 {@code Path}
     *
     * @throws  InvalidPathException
     *          如果路径字符串无法转换为 {@code Path}
     *
     * @see FileSystem#getPath
     */
    public static Path get(String first, String... more) {
        return FileSystems.getDefault().getPath(first, more);
    }

    /**
     * 将给定的 URI 转换为一个 {@link Path} 对象。
     *
     * <p> 该方法遍历已安装的 {@link FileSystemProvider} 以定位由给定 URI 的 {@link URI#getScheme 方案} 标识的提供者。URI 方案比较时不区分大小写。如果找到提供者，则调用其 {@link FileSystemProvider#getPath getPath} 方法来转换 URI。
     *
     * <p> 对于默认提供者，其由 URI 方案 "file" 标识，给定的 URI 应具有非空路径组件，且查询和片段组件未定义。返回的 {@code Path} 与默认文件系统相关联。
     *
     * <p> 默认提供者提供了与 {@link java.io.File} 类类似的 <em>往返</em> 保证。对于给定的 {@code Path} <i>p</i>，可以保证
     * <blockquote><tt>
     * Paths.get(</tt><i>p</i><tt>.{@link Path#toUri() toUri}()).equals(</tt>
     * <i>p</i><tt>.{@link Path#toAbsolutePath() toAbsolutePath}())</tt>
     * </blockquote>
     * 只要原始的 {@code Path}、{@code URI} 和新的 {@code Path} 都是在（可能不同的）Java 虚拟机调用中创建的。其他提供者是否提供任何保证是提供者特定的，因此未指定。
     *
     * @param   uri
     *          要转换的 URI
     *
     * @return  结果的 {@code Path}
     *
     * @throws  IllegalArgumentException
     *          如果 {@code uri} 参数的前置条件不满足。URI 的格式是提供者特定的。
     * @throws  FileSystemNotFoundException
     *          由 URI 标识的文件系统不存在且无法自动创建，或者由 URI 的方案组件标识的提供者未安装
     * @throws  SecurityException
     *          如果安装了安全管理器且其拒绝访问文件系统的未指定权限
     */
    public static Path get(URI uri) {
        String scheme =  uri.getScheme();
        if (scheme == null)
            throw new IllegalArgumentException("缺少方案");

        // 检查默认提供者以避免加载已安装的提供者
        if (scheme.equalsIgnoreCase("file"))
            return FileSystems.getDefault().provider().getPath(uri);

        // 尝试查找提供者
        for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
            if (provider.getScheme().equalsIgnoreCase(scheme)) {
                return provider.getPath(uri);
            }
        }

        throw new FileSystemNotFoundException("提供者 \"" + scheme + "\" 未安装");
    }
}
