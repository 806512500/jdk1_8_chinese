/*
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
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
     * @return 一个 InputStream，可以通过它访问响应体
     * @throws IOException 如果在获取响应体时发生 I/O 错误
     */
    public abstract InputStream getBody() throws IOException;
}
