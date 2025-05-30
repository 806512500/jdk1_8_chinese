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

import java.nio.file.attribute.BasicFileAttributes;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import sun.nio.fs.BasicFileAttributesHolder;

/**
 * 遍历文件树，生成对应于文件树中的文件的一系列事件。
 *
 * <pre>{@code
 *     Path top = ...
 *     Set<FileVisitOption> options = ...
 *     int maxDepth = ...
 *
 *     try (FileTreeWalker walker = new FileTreeWalker(options, maxDepth)) {
 *         FileTreeWalker.Event ev = walker.walk(top);
 *         do {
 *             process(ev);
 *             ev = walker.next();
 *         } while (ev != null);
 *     }
 * }</pre>
 *
 * @see Files#walkFileTree
 */

class FileTreeWalker implements Closeable {
    private final boolean followLinks;
    private final LinkOption[] linkOptions;
    private final int maxDepth;
    private final ArrayDeque<DirectoryNode> stack = new ArrayDeque<>();
    private boolean closed;

    /**
     * 遍历栈中对应于目录节点的元素。
     */
    private static class DirectoryNode {
        private final Path dir;
        private final Object key;
        private final DirectoryStream<Path> stream;
        private final Iterator<Path> iterator;
        private boolean skipped;

        DirectoryNode(Path dir, Object key, DirectoryStream<Path> stream) {
            this.dir = dir;
            this.key = key;
            this.stream = stream;
            this.iterator = stream.iterator();
        }

        Path directory() {
            return dir;
        }

        Object key() {
            return key;
        }

        DirectoryStream<Path> stream() {
            return stream;
        }

        Iterator<Path> iterator() {
            return iterator;
        }

        void skip() {
            skipped = true;
        }

        boolean skipped() {
            return skipped;
        }
    }

    /**
     * 事件类型。
     */
    static enum EventType {
        /**
         * 目录的开始
         */
        START_DIRECTORY,
        /**
         * 目录的结束
         */
        END_DIRECTORY,
        /**
         * 目录中的一个条目
         */
        ENTRY;
    }

    /**
     * 由 {@link #walk} 和 {@link #next} 方法返回的事件。
     */
    static class Event {
        private final EventType type;
        private final Path file;
        private final BasicFileAttributes attrs;
        private final IOException ioe;

        private Event(EventType type, Path file, BasicFileAttributes attrs, IOException ioe) {
            this.type = type;
            this.file = file;
            this.attrs = attrs;
            this.ioe = ioe;
        }

        Event(EventType type, Path file, BasicFileAttributes attrs) {
            this(type, file, attrs, null);
        }

        Event(EventType type, Path file, IOException ioe) {
            this(type, file, null, ioe);
        }

        EventType type() {
            return type;
        }

        Path file() {
            return file;
        }

        BasicFileAttributes attributes() {
            return attrs;
        }

        IOException ioeException() {
            return ioe;
        }
    }

    /**
     * 创建一个 {@code FileTreeWalker}。
     *
     * @throws  IllegalArgumentException
     *          如果 {@code maxDepth} 为负数
     * @throws  ClassCastException
     *          如果 (@code options} 包含一个不是 {@code FileVisitOption} 的元素
     * @throws  NullPointerException
     *          如果 {@code options} 为 {@ocde null} 或选项数组包含一个 {@code null} 元素
     */
    FileTreeWalker(Collection<FileVisitOption> options, int maxDepth) {
        boolean fl = false;
        for (FileVisitOption option: options) {
            // 如果选项包含 null，则会抛出 NPE
            switch (option) {
                case FOLLOW_LINKS : fl = true; break;
                default:
                    throw new AssertionError("不应该到达这里");
            }
        }
        if (maxDepth < 0)
            throw new IllegalArgumentException("'maxDepth' 为负数");

        this.followLinks = fl;
        this.linkOptions = (fl) ? new LinkOption[0] :
            new LinkOption[] { LinkOption.NOFOLLOW_LINKS };
        this.maxDepth = maxDepth;
    }

    /**
     * 返回给定文件的属性，考虑是否跟踪符号链接。{@code canUseCached}
     * 参数确定此方法是否可以使用缓存的属性。
     */
    private BasicFileAttributes getAttributes(Path file, boolean canUseCached)
        throws IOException
    {
        // 如果属性已缓存，则在可能的情况下使用它们
        if (canUseCached &&
            (file instanceof BasicFileAttributesHolder) &&
            (System.getSecurityManager() == null))
        {
            BasicFileAttributes cached = ((BasicFileAttributesHolder)file).get();
            if (cached != null && (!followLinks || !cached.isSymbolicLink())) {
                return cached;
            }
        }

        // 尝试获取文件的属性。如果失败且我们正在跟踪链接，则链接目标可能不存在，因此获取链接的属性
        BasicFileAttributes attrs;
        try {
            attrs = Files.readAttributes(file, BasicFileAttributes.class, linkOptions);
        } catch (IOException ioe) {
            if (!followLinks)
                throw ioe;

            // 尝试不跟踪链接获取属性
            attrs = Files.readAttributes(file,
                                         BasicFileAttributes.class,
                                         LinkOption.NOFOLLOW_LINKS);
        }
        return attrs;
    }

    /**
     * 返回 true，如果遍历给定目录会导致文件系统循环/循环。
     */
    private boolean wouldLoop(Path dir, Object key) {
        // 如果此目录和祖先有文件键，则比较它们；否则使用效率较低的 isSameFile 测试。
        for (DirectoryNode ancestor: stack) {
            Object ancestorKey = ancestor.key();
            if (key != null && ancestorKey != null) {
                if (key.equals(ancestorKey)) {
                    // 检测到循环
                    return true;
                }
            } else {
                try {
                    if (Files.isSameFile(dir, ancestor.directory())) {
                        // 检测到循环
                        return true;
                    }
                } catch (IOException | SecurityException x) {
                    // 忽略
                }
            }
        }
        return false;
    }

    /**
     * 访问给定文件，返回与该访问对应的 {@code Event}。
     *
     * {@code ignoreSecurityException} 参数确定是否忽略任何 SecurityException。如果抛出 SecurityException 并被忽略，则此方法返回 {@code null}，表示没有与文件访问对应的事件。
     *
     * {@code canUseCached} 参数确定是否可以使用文件的缓存属性。
     */
    private Event visit(Path entry, boolean ignoreSecurityException, boolean canUseCached) {
        // 需要文件属性
        BasicFileAttributes attrs;
        try {
            attrs = getAttributes(entry, canUseCached);
        } catch (IOException ioe) {
            return new Event(EventType.ENTRY, entry, ioe);
        } catch (SecurityException se) {
            if (ignoreSecurityException)
                return null;
            throw se;
        }

        // 达到最大深度或文件不是目录
        int depth = stack.size();
        if (depth >= maxDepth || !attrs.isDirectory()) {
            return new Event(EventType.ENTRY, entry, attrs);
        }

        // 检查循环，当跟踪链接时
        if (followLinks && wouldLoop(entry, attrs.fileKey())) {
            return new Event(EventType.ENTRY, entry,
                             new FileSystemLoopException(entry.toString()));
        }

        // 文件是目录，尝试打开它
        DirectoryStream<Path> stream = null;
        try {
            stream = Files.newDirectoryStream(entry);
        } catch (IOException ioe) {
            return new Event(EventType.ENTRY, entry, ioe);
        } catch (SecurityException se) {
            if (ignoreSecurityException)
                return null;
            throw se;
        }

        // 将目录节点推入栈并返回事件
        stack.push(new DirectoryNode(entry, attrs.fileKey(), stream));
        return new Event(EventType.START_DIRECTORY, entry, attrs);
    }


    /**
     * 从给定文件开始遍历。
     */
    Event walk(Path file) {
        if (closed)
            throw new IllegalStateException("已关闭");

        Event ev = visit(file,
                         false,   // ignoreSecurityException
                         false);  // canUseCached
        assert ev != null;
        return ev;
    }

    /**
     * 返回下一个事件，如果已没有更多事件或遍历器已关闭，则返回 {@code null}。
     */
    Event next() {
        DirectoryNode top = stack.peek();
        if (top == null)
            return null;      // 栈为空，已完成

        // 继续遍历栈顶的目录
        Event ev;
        do {
            Path entry = null;
            IOException ioe = null;

            // 获取目录中的下一个条目
            if (!top.skipped()) {
                Iterator<Path> iterator = top.iterator();
                try {
                    if (iterator.hasNext()) {
                        entry = iterator.next();
                    }
                } catch (DirectoryIteratorException x) {
                    ioe = x.getCause();
                }
            }

            // 没有下一个条目，关闭并弹出目录，创建相应的事件
            if (entry == null) {
                try {
                    top.stream().close();
                } catch (IOException e) {
                    if (ioe != null) {
                        ioe = e;
                    } else {
                        ioe.addSuppressed(e);
                    }
                }
                stack.pop();
                return new Event(EventType.END_DIRECTORY, top.directory(), ioe);
            }

            // 访问条目
            ev = visit(entry,
                       true,   // ignoreSecurityException
                       true);  // canUseCached

        } while (ev == null);

        return ev;
    }

    /**
     * 弹出栈顶的目录节点，以便不再有该目录的事件（包括没有 END_DIRECTORY 事件）。
     * 如果栈为空或遍历器已关闭，则此方法为 no-op。
     */
    void pop() {
        if (!stack.isEmpty()) {
            DirectoryNode node = stack.pop();
            try {
                node.stream().close();
            } catch (IOException ignore) { }
        }
    }

    /**
     * 跳过栈顶目录中的剩余条目。
     * 如果栈为空或遍历器已关闭，则此方法为 no-op。
     */
    void skipRemainingSiblings() {
        if (!stack.isEmpty()) {
            stack.peek().skip();
        }
    }

    /**
     * 如果遍历器是打开的，则返回 {@code true}。
     */
    boolean isOpen() {
        return !closed;
    }

    /**
     * 关闭/弹出栈上的所有目录。
     */
    @Override
    public void close() {
        if (!closed) {
            while (!stack.isEmpty()) {
                pop();
            }
            closed = true;
        }
    }
}
