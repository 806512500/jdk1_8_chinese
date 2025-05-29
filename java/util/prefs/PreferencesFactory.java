/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.util.prefs;
import java.util.*;

/**
 * 一个生成 Preferences 对象的工厂对象。提供新的 {@link Preferences} 实现的提供者应提供相应的
 * <tt>PreferencesFactory</tt> 实现，以便新的 <tt>Preferences</tt> 实现可以替代平台特定的默认实现。
 *
 * <p><strong>此类仅适用于 <tt>Preferences</tt> 实现者。正常使用 <tt>Preferences</tt> 功能的用户无需查阅此文档。</strong>
 *
 * @author  Josh Bloch
 * @see     Preferences
 * @since   1.4
 */
public interface PreferencesFactory {
    /**
     * 返回系统根偏好节点。多次调用此方法将返回相同的对象引用。
     * @return 系统根偏好节点
     */
    Preferences systemRoot();

    /**
     * 返回与调用用户相对应的用户根偏好节点。在服务器中，返回的值通常取决于某些隐式的客户端上下文。
     * @return 与调用用户相对应的用户根偏好节点
     */
    Preferences userRoot();
}
