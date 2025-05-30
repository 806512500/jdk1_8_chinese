/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.file.attribute.*;
import java.io.IOException;

/**
 * 文件存储。一个 {@code FileStore} 表示一个存储池、设备、分区、卷、具体的文件系统或其他实现特定的文件存储方式。通过调用 {@link Files#getFileStore getFileStore} 方法可以获取文件存储的位置，或者通过调用 {@link FileSystem#getFileStores getFileStores} 方法可以枚举所有文件存储。
 *
 * <p> 除了此类定义的方法外，文件存储可能支持一个或多个 {@link FileStoreAttributeView FileStoreAttributeView} 类，这些类提供了一组文件存储属性的只读或可更新视图。
 *
 * @since 1.7
 */

public abstract class FileStore {

    /**
     * 初始化此类的新实例。
     */
    protected FileStore() {
    }

    /**
     * 返回此文件存储的名称。名称的格式高度依赖于具体实现。通常情况下，它将是存储池或卷的名称。
     *
     * <p> 通过此方法返回的字符串可能与通过 {@link Object#toString() toString} 方法返回的字符串不同。
     *
     * @return  此文件存储的名称
     */
    public abstract String name();

    /**
     * 返回此文件存储的<em>类型</em>。通过此方法返回的字符串格式高度依赖于具体实现。例如，它可能表示使用的格式，或文件存储是本地还是远程。
     *
     * @return  一个表示此文件存储类型的字符串
     */
    public abstract String type();

    /**
     * 告诉此文件存储是否为只读。如果文件存储不支持写操作或其他文件更改操作，则它是只读的。任何尝试创建文件、打开现有文件进行写入等操作都会导致抛出 {@code IOException}。
     *
     * @return  如果且仅当此文件存储为只读时返回 {@code true}
     */
    public abstract boolean isReadOnly();

    /**
     * 返回文件存储的大小（以字节为单位）。
     *
     * @return  文件存储的大小，以字节为单位
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract long getTotalSpace() throws IOException;

    /**
     * 返回此 Java 虚拟机在文件存储上可用的字节数。
     *
     * <p> 返回的可用字节数是一个提示，但不是保证可以使用这些字节中的大部分或任何部分。在获取空间属性后，可用字节数最有可能是准确的。任何外部 I/O 操作（包括在系统外部进行的 I/O 操作）都可能导致可用字节数变得不准确。
     *
     * @return  可用的字节数
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract long getUsableSpace() throws IOException;

    /**
     * 返回文件存储中未分配的字节数。
     *
     * <p> 返回的未分配字节数是一个提示，但不是保证可以使用这些字节中的大部分或任何部分。在获取空间属性后，未分配字节数最有可能是准确的。任何外部 I/O 操作（包括在系统外部进行的 I/O 操作）都可能导致未分配字节数变得不准确。
     *
     * @return  未分配的字节数
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract long getUnallocatedSpace() throws IOException;

    /**
     * 告诉此文件存储是否支持给定文件属性视图标识的文件属性。
     *
     * <p> 调用此方法测试文件存储是否支持 {@link BasicFileAttributeView} 将始终返回 {@code true}。对于默认提供者，当文件存储不是本地存储设备时，此方法不能保证给出正确结果。具体原因取决于实现，因此未指定。
     *
     * @param   type
     *          文件属性视图类型
     *
     * @return  如果且仅当文件属性视图受支持时返回 {@code true}
     */
    public abstract boolean supportsFileAttributeView(Class<? extends FileAttributeView> type);

    /**
     * 告诉此文件存储是否支持给定文件属性视图标识的文件属性。
     *
     * <p> 调用此方法测试文件存储是否支持 {@link BasicFileAttributeView}，标识名为 "{@code basic}" 的视图将始终返回 {@code true}。对于默认提供者，当文件存储不是本地存储设备时，此方法不能保证给出正确结果。具体原因取决于实现，因此未指定。
     *
     * @param   name
     *          文件属性视图的 {@link FileAttributeView#name 名称}
     *
     * @return  如果且仅当文件属性视图受支持时返回 {@code true}
     */
    public abstract boolean supportsFileAttributeView(String name);

    /**
     * 返回给定类型的 {@code FileStoreAttributeView}。
     *
     * <p> 此方法旨在用于文件存储属性视图定义了类型安全的方法来读取或更新文件存储属性的情况。{@code type} 参数是所需的属性视图类型，如果支持，该方法将返回该类型的实例。
     *
     * @param   <V>
     *          {@code FileStoreAttributeView} 类型
     * @param   type
     *          对应于属性视图的 {@code Class} 对象
     *
     * @return  指定类型的文件存储属性视图，如果属性视图不可用则返回 {@code null}
     */
    public abstract <V extends FileStoreAttributeView> V
        getFileStoreAttributeView(Class<V> type);

    /**
     * 读取文件存储属性的值。
     *
     * <p> {@code attribute} 参数标识要读取的属性，其形式为：
     * <blockquote>
     * <i>view-name</i><b>:</b><i>attribute-name</i>
     * </blockquote>
     * 其中字符 {@code ':'} 代表其本身。
     *
     * <p> <i>view-name</i> 是 {@link FileStoreAttributeView#name 名称} 的 {@link FileStore AttributeView}，标识一组文件属性。<i>attribute-name</i> 是属性的名称。
     *
     * <p> <b>使用示例：</b>
     * 假设支持 "zfs" 视图，我们想知道 ZFS 压缩是否已启用：
     * <pre>
     *    boolean compression = (Boolean)fs.getAttribute("zfs:compression");
     * </pre>
     *
     * @param   attribute
     *          要读取的属性
     *
     * @return  属性值；对于某些属性，{@code null} 可能是有效值
     *
     * @throws  UnsupportedOperationException
     *          如果属性视图不可用或不支持读取属性
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public abstract Object getAttribute(String attribute) throws IOException;
}
