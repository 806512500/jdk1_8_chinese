/*
 * Copyright (c) 1996, 1997, Oracle and/or its affiliates. All rights reserved.
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

package java.beans;

/**
 * ParameterDescriptor 类允许 bean 实现者为他们的每个参数提供
 * 除了由 java.lang.reflect.Method 类提供的低级类型信息之外的
 * 更多信息。
 * <p>
 * 目前，我们所有的状态都来自 FeatureDescriptor 基类。
 */

public class ParameterDescriptor extends FeatureDescriptor {

    /**
     * 公共默认构造函数。
     */
    public ParameterDescriptor() {
    }

    /**
     * 包私有复制构造函数。
     * 必须使新对象与旧对象的任何更改隔离。
     */
    ParameterDescriptor(ParameterDescriptor old) {
        super(old);
    }

}
