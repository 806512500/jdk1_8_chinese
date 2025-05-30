/*
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi;

/**
 * 当在远程方法调用中解组参数或结果时，如果发生以下任何情况，可以抛出 <code>UnmarshalException</code>：
 * <ul>
 * <li> 如果在解组调用头时发生异常
 * <li> 如果返回值的协议无效
 * <li> 如果在解组参数（在服务器端）或返回值（在客户端）时发生 <code>java.io.IOException</code>
 * <li> 如果在解组参数或返回值时发生 <code>java.lang.ClassNotFoundException</code>
 * <li> 如果在服务器端无法加载任何骨架；注意，骨架在 1.1 存根协议中是必需的，但在 1.2 存根协议中不是必需的。
 * <li> 如果方法哈希无效（即，缺少方法）。
 * <li> 如果在解组远程对象的存根时无法创建远程引用对象。
 * </ul>
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 */
public class UnmarshalException extends RemoteException {

    /* 表示与 JDK 1.1.x 版本类的兼容性 */
    private static final long serialVersionUID = 594380845140740218L;

    /**
     * 使用指定的详细消息构造 <code>UnmarshalException</code>。
     *
     * @param s 详细消息
     * @since JDK1.1
     */
    public UnmarshalException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>UnmarshalException</code>。
     *
     * @param s 详细消息
     * @param ex 嵌套异常
     * @since JDK1.1
     */
    public UnmarshalException(String s, Exception ex) {
        super(s, ex);
    }
}
