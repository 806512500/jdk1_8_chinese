/*
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.instrument;

/*
 * Copyright 2003 Wily Technology, Inc.
 */

/**
 * 该类作为 <code>Instrumentation.redefineClasses</code> 方法的参数块。
 * 用于将需要重新定义的 <code>Class</code> 与新的类文件字节绑定在一起。
 *
 * @see     java.lang.instrument.Instrumentation#redefineClasses
 * @since   1.5
 */
public final class ClassDefinition {
    /**
     * 需要重新定义的类
     */
    private final Class<?> mClass;

    /**
     * 替换的类文件字节
     */
    private final byte[]   mClassFile;

    /**
     * 使用提供的类和类文件字节创建一个新的 <code>ClassDefinition</code> 绑定。不复制提供的缓冲区，只是捕获一个引用。
     *
     * @param theClass 需要重新定义的 <code>Class</code>
     * @param theClassFile 新的类文件字节
     *
     * @throws java.lang.NullPointerException 如果提供的类或数组为 <code>null</code>。
     */
    public
    ClassDefinition(    Class<?> theClass,
                        byte[]  theClassFile) {
        if (theClass == null || theClassFile == null) {
            throw new NullPointerException();
        }
        mClass      = theClass;
        mClassFile  = theClassFile;
    }

    /**
     * 返回类。
     *
     * @return    指定的 <code>Class</code> 对象。
     */
    public Class<?>
    getDefinitionClass() {
        return mClass;
    }

    /**
     * 返回包含新类文件的字节数组。
     *
     * @return    类文件字节。
     */
    public byte[]
    getDefinitionClassFile() {
        return mClassFile;
    }
}
