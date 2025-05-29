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

import java.io.IOException;

/**
 * 抽象类 {@code ContentHandler} 是所有从
 * {@code URLConnection} 读取 {@code Object} 的类的超类。
 * <p>
 * 通常情况下，应用程序不会直接调用此类中的
 * {@code getContent} 方法。相反，应用程序会调用
 * {@code URL} 类或 {@code URLConnection} 类中的
 * {@code getContent} 方法。
 * 应用程序的内容处理器工厂（一个实现了 {@code ContentHandlerFactory} 接口的类的实例，
 * 通过调用 {@code setContentHandler} 设置）会被调用，传入一个表示
 * 从套接字接收的对象的 MIME 类型的 {@code String}。工厂返回一个
 * {@code ContentHandler} 的子类的实例，其
 * {@code getContent} 方法被调用以创建对象。
 * <p>
 * 如果找不到内容处理器，URLConnection 将会在用户定义的一系列位置中查找内容处理器。
 * 默认情况下，它会查找 sun.net.www.content，但用户可以通过定义
 * java.content.handler.pkgs 属性来指定一个用竖线分隔的类前缀列表，以便在这些位置中进行搜索。
 * 类名必须具有以下形式：
 * <pre>
 *     {package-prefix}.{major}.{minor}
 * 例如：
 *     YoyoDyne.experimental.text.plain
 * </pre>
 * 如果内容处理器类的加载是由调用者委托链之外的类加载器执行的，
 * JVM 将需要 "getClassLoader" 的 RuntimePermission。
 *
 * @author  James Gosling
 * @see     java.net.ContentHandler#getContent(java.net.URLConnection)
 * @see     java.net.ContentHandlerFactory
 * @see     java.net.URL#getContent()
 * @see     java.net.URLConnection
 * @see     java.net.URLConnection#getContent()
 * @see     java.net.URLConnection#setContentHandlerFactory(java.net.ContentHandlerFactory)
 * @since   JDK1.0
 */
abstract public class ContentHandler {
    /**
     * 给定一个位于对象表示形式开头的 URL 连接流，此方法读取该流并
     * 从中创建一个对象。
     *
     * @param      urlc   一个 URL 连接。
     * @return     由 {@code ContentHandler} 读取的对象。
     * @exception  IOException  如果在读取对象时发生 I/O 错误。
     */
    abstract public Object getContent(URLConnection urlc) throws IOException;

    /**
     * 给定一个位于对象表示形式开头的 URL 连接流，此方法读取该流并
     * 创建一个与指定类型之一匹配的对象。
     *
     * 此方法的默认实现应调用 getContent()
     * 并筛选返回类型以匹配建议的类型。
     *
     * @param      urlc   一个 URL 连接。
     * @param      classes      请求的类型数组
     * @return     由 {@code ContentHandler} 读取的与建议类型之一匹配的对象。
     *                 如果没有支持的请求类型，则返回 null。
     * @exception  IOException  如果在读取对象时发生 I/O 错误。
     * @since 1.3
     */
    @SuppressWarnings("rawtypes")
    public Object getContent(URLConnection urlc, Class[] classes) throws IOException {
        Object obj = getContent(urlc);

        for (int i = 0; i < classes.length; i++) {
          if (classes[i].isInstance(obj)) {
                return obj;
          }
        }
        return null;
    }

}
