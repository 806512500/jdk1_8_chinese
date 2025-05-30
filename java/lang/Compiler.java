/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

/**
 * {@code Compiler} 类用于支持从 Java 到本地代码的编译器及相关服务。设计上，{@code Compiler} 类不执行任何操作；它作为 JIT 编译器实现的占位符。
 *
 * <p> 当 Java 虚拟机首次启动时，它会确定系统属性 {@code java.compiler} 是否存在。（系统属性可以通过 {@link System#getProperty(String)} 和 {@link
 * System#getProperty(String, String)} 访问。） 如果存在，假定它是库的名称（具有平台依赖的确切位置和类型）；将调用 {@link
 * System#loadLibrary} 加载该库。如果加载成功，将调用该库中的 {@code java_lang_Compiler_start()} 函数。
 *
 * <p> 如果没有可用的编译器，这些方法将不执行任何操作。
 *
 * @author  Frank Yellin
 * @since   JDK1.0
 */
public final class Compiler  {
    private Compiler() {}               // 不创建实例

    private static native void initialize();

    private static native void registerNatives();

    static {
        registerNatives();
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    boolean loaded = false;
                    String jit = System.getProperty("java.compiler");
                    if ((jit != null) && (!jit.equals("NONE")) &&
                        (!jit.equals("")))
                    {
                        try {
                            System.loadLibrary(jit);
                            initialize();
                            loaded = true;
                        } catch (UnsatisfiedLinkError e) {
                            System.err.println("警告: 未找到 JIT 编译器 \"" +
                              jit + "\"。将使用解释器。");
                        }
                    }
                    String info = System.getProperty("java.vm.info");
                    if (loaded) {
                        System.setProperty("java.vm.info", info + ", " + jit);
                    } else {
                        System.setProperty("java.vm.info", info + ", nojit");
                    }
                    return null;
                }
            });
    }

    /**
     * 编译指定的类。
     *
     * @param  clazz
     *         一个类
     *
     * @return  如果编译成功返回 {@code true}；如果编译失败或没有可用的编译器返回 {@code false}
     *
     * @throws  NullPointerException
     *          如果 {@code clazz} 为 {@code null}
     */
    public static native boolean compileClass(Class<?> clazz);

    /**
     * 编译名称与指定字符串匹配的所有类。
     *
     * @param  string
     *         要编译的类的名称
     *
     * @return  如果编译成功返回 {@code true}；如果编译失败或没有可用的编译器返回 {@code false}
     *
     * @throws  NullPointerException
     *          如果 {@code string} 为 {@code null}
     */
    public static native boolean compileClasses(String string);

    /**
     * 检查参数类型及其字段并执行某些记录的操作。不需要执行特定的操作。
     *
     * @param  any
     *         一个参数
     *
     * @return  如果没有可用的编译器返回 {@code null}，否则返回编译器特定的值
     *
     * @throws  NullPointerException
     *          如果 {@code any} 为 {@code null}
     */
    public static native Object command(Object any);

    /**
     * 使编译器恢复操作。
     */
    public static native void enable();

    /**
     * 使编译器停止操作。
     */
    public static native void disable();
}
