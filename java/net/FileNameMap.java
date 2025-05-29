/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 一个简单的接口，提供了一种机制来映射文件名和MIME类型字符串。
 *
 * @author  Steven B. Byrne
 * @since   JDK1.1
 */
public interface FileNameMap {

    /**
     * 获取指定文件名的MIME类型。
     * @param fileName 指定的文件名
     * @return 表示指定文件名的MIME类型的{@code String}。
     */
    public String getContentTypeFor(String fileName);
}
