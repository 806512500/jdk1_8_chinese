/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
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
 * href="../../../../technotes/guides/extensions/index.html">Java 扩展机制</a> 包装为已安装的扩展。有关扩展包装，请参阅 {@link java.util.ServiceLoader}。
 * 任何已安装的 {@code
 * ResourceBundleControlProvider} 实现都将在 {@code ResourceBundle} 类加载时使用 {@link
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
