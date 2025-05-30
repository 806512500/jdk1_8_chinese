/*
 * Copyright (c) 1996, 2005, Oracle and/or its affiliates. All rights reserved.
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

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.util;

/**
 * 表示资源缺失。
 * @see java.lang.Exception
 * @see ResourceBundle
 * @author      Mark Davis
 * @since       JDK1.1
 */
public
class MissingResourceException extends RuntimeException {

    /**
     * 使用指定的信息构造一个 MissingResourceException。
     * 详细消息是一个描述此特定异常的字符串。
     * @param s 详细消息
     * @param className 资源类的名称
     * @param key 缺失资源的键。
     */
    public MissingResourceException(String s, String className, String key) {
        super(s);
        this.className = className;
        this.key = key;
    }

    /**
     * 构造一个带有 <code>message</code>、<code>className</code>、<code>key</code>
     * 和 <code>cause</code> 的 <code>MissingResourceException</code>。
     * 此构造函数是包私有的，用于 <code>ResourceBundle.getBundle</code>。
     *
     * @param message
     *        详细消息
     * @param className
     *        资源类的名称
     * @param key
     *        缺失资源的键。
     * @param cause
     *        原因（稍后可通过 {@link Throwable.getCause()} 方法检索）。允许为 null，表示原因不存在或未知。
     */
    MissingResourceException(String message, String className, String key, Throwable cause) {
        super(message, cause);
        this.className = className;
        this.key = key;
    }

    /**
     * 获取构造函数传递的参数。
     *
     * @return 资源类的名称
     */
    public String getClassName() {
        return className;
    }

    /**
     * 获取构造函数传递的参数。
     *
     * @return 缺失资源的键
     */
    public String getKey() {
        return key;
    }

    //============ privates ============

    // 与 JDK1.1 兼容的序列化
    private static final long serialVersionUID = -4876345176062000401L;

    /**
     * 用户请求的资源包的类名。
     * @serial
     */
    private String className;

    /**
     * 用户请求的特定资源的名称。
     * @serial
     */
    private String key;
}
