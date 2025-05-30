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

package java.lang.management;

/**
 * {@link MemoryPoolMXBean 内存池}的类型。
 *
 * @author  Mandy Chung
 * @since   1.5
 */
public enum MemoryType {

    /**
     * 堆内存类型。
     * <p>
     * Java虚拟机有一个<i>堆</i>，这是运行时数据区域，从该区域为所有类实例和数组分配内存。
     */
    HEAP("堆内存"),

    /**
     * 非堆内存类型。
     * <p>
     * Java虚拟机管理除堆之外的内存（称为<i>非堆内存</i>）。非堆内存包括<i>方法区</i>和Java虚拟机内部处理或优化所需的内存。
     * 它存储每个类的结构，如运行时常量池、字段和方法数据，以及方法和构造函数的代码。
     */
    NON_HEAP("非堆内存");

    private final String description;

    private MemoryType(String s) {
        this.description = s;
    }

    /**
     * 返回此<tt>MemoryType</tt>的字符串表示形式。
     * @return 此<tt>MemoryType</tt>的字符串表示形式。
     */
    public String toString() {
        return description;
    }

    private static final long serialVersionUID = 6992337162326171013L;
}
