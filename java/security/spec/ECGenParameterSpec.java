/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.security.spec;

/**
 * 此不可变类指定用于生成椭圆曲线（EC）域参数的参数集。
 *
 * @see AlgorithmParameterSpec
 *
 * @author Valerie Peng
 *
 * @since 1.5
 */
public class ECGenParameterSpec implements AlgorithmParameterSpec {

    private String name;

    /**
     * 使用标准（或预定义）名称 {@code stdName} 创建一个 EC 参数生成的参数规范，
     * 以便生成相应的（预计算的）椭圆曲线域参数。有关支持的名称列表，请参阅将要使用的提供者的文档。
     * @param stdName 要生成的 EC 域参数的标准名称。
     * @exception NullPointerException 如果 {@code stdName} 为 null。
     */
    public ECGenParameterSpec(String stdName) {
        if (stdName == null) {
            throw new NullPointerException("stdName is null");
        }
        this.name = stdName;
    }

    /**
     * 返回要生成的 EC 域参数的标准或预定义名称。
     * @return 标准或预定义名称。
     */
    public String getName() {
        return name;
    }
}
