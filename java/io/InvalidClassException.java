/*
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

/**
 * 当序列化运行时检测到类的以下问题之一时抛出。
 * <UL>
 * <LI> 类的序列化版本与从流中读取的类描述符不匹配
 * <LI> 类包含未知的数据类型
 * <LI> 类没有可访问的无参构造函数
 * </UL>
 *
 * @author  未署名
 * @since   JDK1.1
 */
public class InvalidClassException extends ObjectStreamException {

    private static final long serialVersionUID = -4333316296251054416L;

    /**
     * 无效类的名称。
     *
     * @serial 无效类的名称。
     */
    public String classname;

    /**
     * 为指定的原因报告一个 InvalidClassException。
     *
     * @param reason  描述异常原因的字符串。
     */
    public InvalidClassException(String reason) {
        super(reason);
    }

    /**
     * 构造一个 InvalidClassException 对象。
     *
     * @param cname   命名无效类的字符串。
     * @param reason  描述异常原因的字符串。
     */
    public InvalidClassException(String cname, String reason) {
        super(reason);
        classname = cname;
    }

    /**
     * 生成消息并包含类名（如果存在）。
     */
    public String getMessage() {
        if (classname == null)
            return super.getMessage();
        else
            return classname + "; " + super.getMessage();
    }
}
