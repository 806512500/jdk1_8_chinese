/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.util.spi;

import java.util.ResourceBundle;

/**
 * 一个接口，用于提供 {@link
 * java.util.ResourceBundle.Control} 的实现。通过 {@code
 * ResourceBundleControlProvider} 实现可以修改不带 {@link java.util.ResourceBundle.Control} 实例的 {@code ResourceBundle.getBundle}
 * 工厂方法的 <a
 * href="../ResourceBundle.html#default_behavior">默认资源包加载行为</a>。
 *
 * <p>提供者实现必须使用 <a
 * href="../../../../technotes/guides/extensions/index.html">Java 扩展机制</a>
 * 作为已安装的扩展进行打包。有关扩展打包，请参阅 {@link java.util.ServiceLoader}。任何已安装的 {@code
 * ResourceBundleControlProvider} 实现都在 {@code ResourceBundle} 类加载时使用 {@link
 * java.util.ServiceLoader} 加载。
 *
 * @author Masayoshi Okutsu
 * @since 1.8
 * @see ResourceBundle#getBundle(String, java.util.Locale, ClassLoader, ResourceBundle.Control)
 *      ResourceBundle.getBundle
 * @see java.util.ServiceLoader#loadInstalled(Class)
 */
public interface ResourceBundleControlProvider {
    /**
     * 返回一个用于处理给定 {@code
     * baseName} 的资源包加载的 {@code ResourceBundle.Control} 实例。如果给定的 {@code
     * baseName} 不由此提供者处理，则此方法必须返回 {@code null}。
     *
     * @param baseName 资源包的基本名称
     * @return 一个 {@code ResourceBundle.Control} 实例，
     *         或如果给定的 {@code baseName} 不适用于此提供者，则返回 {@code null}。
     * @throws NullPointerException 如果 {@code baseName} 为 {@code null}
     */
    public ResourceBundle.Control getControl(String baseName);
}
