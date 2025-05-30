/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.charset.spi;

import java.nio.charset.Charset;
import java.util.Iterator;


/**
 * 字符集服务提供者类。
 *
 * <p> 字符集提供者是此类的具体子类，具有无参数构造函数和一些关联的字符集实现类。字符集提供者可以作为扩展安装在 Java 平台的实例中，即放置在通常的扩展目录中的 jar 文件。提供者也可以通过将其添加到 applet 或应用程序的类路径中或通过其他平台特定的方式提供。通过当前线程的 {@link java.lang.Thread#getContextClassLoader() 上下文类加载器} 查找字符集提供者。
 *
 * <p> 字符集提供者通过名为 <tt>java.nio.charset.spi.CharsetProvider</tt> 的提供者配置文件在资源目录 <tt>META-INF/services</tt> 中标识自己。该文件应包含每个类一行的完全限定的具体字符集提供者类名。行由换行符 (<tt>'\n'</tt>)、回车符 (<tt>'\r'</tt>) 或回车符后立即跟换行符终止。行首和行尾的空格和制表符字符以及空行将被忽略。注释字符是 <tt>'#'</tt> (<tt>'&#92;u0023'</tt>)；每行中第一个注释字符之后的所有字符都将被忽略。文件必须使用 UTF-8 编码。
 *
 * <p> 如果在多个配置文件中命名了特定的具体字符集提供者类，或者在同一个配置文件中命名了多次，则重复项将被忽略。命名特定提供者的配置文件不必与提供者本身位于同一个 jar 文件或其他分发单元中。提供者必须可以从最初查询以定位配置文件的同一个类加载器访问；这不一定是加载文件的类加载器。 </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 专家组
 * @since 1.4
 *
 * @see java.nio.charset.Charset
 */

public abstract class CharsetProvider {

    /**
     * 初始化新的字符集提供者。
     *
     * @throws  SecurityException
     *          如果已安装了安全经理，并且它拒绝
     *          {@link RuntimePermission}<tt>("charsetProvider")</tt>
     */
    protected CharsetProvider() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("charsetProvider"));
    }

    /**
     * 创建一个迭代器，用于迭代此提供者支持的字符集。此方法用于实现 {@link
     * java.nio.charset.Charset#availableCharsets Charset.availableCharsets}
     * 方法。
     *
     * @return  新的迭代器
     */
    public abstract Iterator<Charset> charsets();

    /**
     * 检索给定字符集名称的字符集。
     *
     * @param  charsetName
     *         请求的字符集的名称；可以是规范名称或别名
     *
     * @return  命名字符集的字符集对象，
     *          或者如果此提供者不支持命名字符集，则返回 <tt>null</tt>
     */
    public abstract Charset charsetForName(String charsetName);

}
