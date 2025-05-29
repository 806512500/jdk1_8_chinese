/*
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.net;

import java.io.OutputStream;
import java.io.IOException;

/**
 * 表示用于在 ResponseCache 中存储资源的通道。此类的实例提供一个
 * OutputStream 对象，该对象由协议处理程序调用，以将资源数据存储到缓存中，
 * 并且提供一个 abort() 方法，允许中断和放弃缓存存储操作。如果在读取响应或写入缓存时遇到
 * IOException，当前的缓存存储操作将被放弃。
 *
 * @author Yingxian Wang
 * @since 1.5
 */
public abstract class CacheRequest {

    /**
     * 返回一个可以写入响应体的 OutputStream。
     *
     * @return 一个可以写入响应体的 OutputStream
     * @throws IOException 如果在写入响应体时发生 I/O 错误
     */
    public abstract OutputStream getBody() throws IOException;

    /**
     * 中断尝试缓存响应的操作。如果在读取响应或写入缓存时遇到 IOException，
     * 当前的缓存存储操作将被放弃。
     */
    public abstract void abort();
}
