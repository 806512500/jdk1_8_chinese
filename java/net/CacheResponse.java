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

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import java.io.IOException;

/**
 * 表示从 ResponseCache 检索资源的通道。此类的实例提供一个 InputStream，用于返回实体主体，还提供一个 getHeaders() 方法，用于返回关联的响应头。
 *
 * @author Yingxian Wang
 * @since 1.5
 */
public abstract class CacheResponse {

    /**
     * 以 Map 的形式返回响应头。
     *
     * @return 一个不可变的 Map，从响应头字段名称映射到字段值列表。状态行的字段名称为 null。
     * @throws IOException 如果在获取响应头时发生 I/O 错误
     */
    public abstract Map<String, List<String>> getHeaders() throws IOException;

    /**
     * 以 InputStream 的形式返回响应体。
     *
     * @return 一个 InputStream，从中可以访问响应体
     * @throws IOException 如果在获取响应体时发生 I/O 错误
     */
    public abstract InputStream getBody() throws IOException;
}
