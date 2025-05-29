/*
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

import java.io.IOException;

/**
 * <tt>Flushable</tt> 是一个可以刷新的数据目的地。调用 flush 方法将任何缓冲的输出写入底层流。
 *
 * @since 1.5
 */
public interface Flushable {

    /**
     * 通过将任何缓冲的输出写入底层流来刷新此流。
     *
     * @throws IOException 如果发生 I/O 错误
     */
    void flush() throws IOException;
}
