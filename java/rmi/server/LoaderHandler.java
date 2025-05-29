/*
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi.server;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * <code>LoaderHandler</code> 是一个接口，用于 RMI 运行时在早期实现版本中的内部使用。应用程序代码不应访问它。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 *
 * @deprecated 没有替代品
 */
@Deprecated
public interface LoaderHandler {

    /** 系统 <code>LoaderHandler</code> 实现的包。 */
    final static String packagePrefix = "sun.rmi.server";

    /**
     * 从 <code>java.rmi.server.codebase</code> 属性指定的位置加载类。
     *
     * @param  name 要加载的类的名称
     * @return 表示已加载类的 <code>Class</code> 对象
     * @exception MalformedURLException
     *            如果系统属性 <b>java.rmi.server.codebase</b>
     *            包含无效的 URL
     * @exception ClassNotFoundException
     *            如果在代码库位置找不到类的定义。
     * @since JDK1.1
     * @deprecated 没有替代品
     */
    @Deprecated
    Class<?> loadClass(String name)
        throws MalformedURLException, ClassNotFoundException;

    /**
     * 从 URL 加载类。
     *
     * @param codebase  从中加载类的 URL
     * @param name      要加载的类的名称
     * @return 表示已加载类的 <code>Class</code> 对象
     * @exception MalformedURLException
     *            如果 <code>codebase</code> 参数
     *            包含无效的 URL
     * @exception ClassNotFoundException
     *            如果在指定的 URL 找不到类的定义
     * @since JDK1.1
     * @deprecated 没有替代品
     */
    @Deprecated
    Class<?> loadClass(URL codebase, String name)
        throws MalformedURLException, ClassNotFoundException;

    /**
     * 返回给定类加载器的安全上下文。
     *
     * @param loader  从中获取安全上下文的类加载器
     * @return 安全上下文
     * @since JDK1.1
     * @deprecated 没有替代品
     */
    @Deprecated
    Object getSecurityContext(ClassLoader loader);
}
