/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 此接口定义了内容处理器的工厂。此接口的实现应将 MIME 类型映射到 {@code ContentHandler} 的实例。
 * <p>
 * 该接口由 {@code URLStreamHandler} 类使用，以创建 MIME 类型的内容处理器。
 *
 * @author  James Gosling
 * @see     java.net.ContentHandler
 * @see     java.net.URLStreamHandler
 * @since   JDK1.0
 */
public interface ContentHandlerFactory {
    /**
     * 创建一个新的 {@code ContentHandler} 以从 {@code URLStreamHandler} 读取对象。
     *
     * @param   mimetype   所需内容处理器的 MIME 类型。
     *
     * @return  一个新的 {@code ContentHandler} 以从 {@code URLStreamHandler} 读取对象。
     * @see     java.net.ContentHandler
     * @see     java.net.URLStreamHandler
     */
    ContentHandler createContentHandler(String mimetype);
}
