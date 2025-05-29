/*
 * Copyright (c) 2000, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 用于接收偏好设置节点更改事件的监听器。
 *
 * @author  Josh Bloch
 * @see     Preferences
 * @see     NodeChangeEvent
 * @see     PreferenceChangeListener
 * @since   1.4
 */

public interface NodeChangeListener extends java.util.EventListener {
    /**
     * 当子节点被添加时调用此方法。
     *
     * @param evt 描述父节点和子节点的节点更改事件对象。
     */
    void childAdded(NodeChangeEvent evt);

    /**
     * 当子节点被移除时调用此方法。
     *
     * @param evt 描述父节点和子节点的节点更改事件对象。
     */
    void childRemoved(NodeChangeEvent evt);
}
