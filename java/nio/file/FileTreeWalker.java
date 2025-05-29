
/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.file.attribute.BasicFileAttributes;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import sun.nio.fs.BasicFileAttributesHolder;

/**
 * 遍历文件树，生成与树中文件对应的事件序列。
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
         * 目录开始
         */
        START_DIRECTORY,
        /**
         * 目录结束
         */
        END_DIRECTORY,
        /**
         * 目录中的条目
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
            // 如果 options 包含 null，则会抛出 NPE
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
        // 如果属性已缓存，则尽可能使用它们
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
     * 如果进入给定目录会导致文件系统循环/循环，则返回 true。
     */
    private boolean wouldLoop(Path dir, Object key) {
        // 如果此目录和祖先有文件键，则我们比较它们；否则我们使用效率较低的 isSameFile 测试。
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
     * 访问给定的文件，返回与该访问相对应的 {@code Event}。
     *
     * {@code ignoreSecurityException} 参数确定是否忽略任何 SecurityException。如果抛出了 SecurityException，
     * 并且被忽略，那么此方法返回 {@code null}，表示没有与文件访问相对应的事件。
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

        // 在最大深度或文件不是目录
        int depth = stack.size();
        if (depth >= maxDepth || !attrs.isDirectory()) {
            return new Event(EventType.ENTRY, entry, attrs);
        }

        // 检查循环链接
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

        // 将目录节点推入堆栈并返回事件
        stack.push(new DirectoryNode(entry, attrs.fileKey(), stream));
        return new Event(EventType.START_DIRECTORY, entry, attrs);
    }


    /**
     * 从给定的文件开始遍历。
     */
    Event walk(Path file) {
        if (closed)
            throw new IllegalStateException("Closed");

        Event ev = visit(file,
                         false,   // ignoreSecurityException
                         false);  // canUseCached
        assert ev != null;
        return ev;
    }

    /**
     * 返回下一个 Event 或者如果没有任何更多事件或遍历器已关闭，则返回 {@code null}。
     */
    Event next() {
        DirectoryNode top = stack.peek();
        if (top == null)
            return null;      // 堆栈为空，已完成

        // 继续迭代堆栈顶部的目录
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
     * 弹出当前堆栈顶部的目录节点，以便不再有该目录的事件（包括没有 END_DIRECTORY 事件）。
     * 如果堆栈为空或遍历器已关闭，此方法为无操作。
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
     * 跳过堆栈顶部目录中的剩余条目。
     * 如果堆栈为空或遍历器已关闭，此方法为无操作。
     */
    void skipRemainingSiblings() {
        if (!stack.isEmpty()) {
            stack.peek().skip();
        }
    }

    /**
     * 如果遍历器是打开的，返回 {@code true}。
     */
    boolean isOpen() {
        return !closed;
    }

    /**
     * 关闭/弹出堆栈上的所有目录。
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
