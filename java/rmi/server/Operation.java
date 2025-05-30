/*
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi.server;

/**
 * <code>Operation</code> 包含对 Java 方法的描述。
 * <code>Operation</code> 对象在 JDK1.1 版本的存根和骨架中使用。
 * <code>Operation</code> 类对于 1.2 风格的存根（使用 <code>rmic -v1.2</code> 生成的存根）不是必需的；
 * 因此，此类已废弃。
 *
 * @since JDK1.1
 * @deprecated 没有替代
 */
@Deprecated
public class Operation {
    private String operation;

    /**
     * 创建一个新的 Operation 对象。
     * @param op 方法名
     * @deprecated 没有替代
     * @since JDK1.1
     */
    @Deprecated
    public Operation(String op) {
        operation = op;
    }

    /**
     * 返回方法的名称。
     * @return 方法名
     * @deprecated 没有替代
     * @since JDK1.1
     */
    @Deprecated
    public String getOperation() {
        return operation;
    }

    /**
     * 返回操作的字符串表示形式。
     * @deprecated 没有替代
     * @since JDK1.1
     */
    @Deprecated
    public String toString() {
        return operation;
    }
}
