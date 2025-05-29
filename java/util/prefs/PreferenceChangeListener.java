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

/**
 * 一个用于接收偏好设置更改事件的监听器。
 *
 * @author  Josh Bloch
 * @see Preferences
 * @see PreferenceChangeEvent
 * @see NodeChangeListener
 * @since   1.4
 */
@FunctionalInterface
public interface PreferenceChangeListener extends java.util.EventListener {
    /**
     * 当偏好设置被添加、移除或其值被更改时，此方法将被调用。
     * <p>
     * @param evt 一个描述事件源和已更改的偏好的 PreferenceChangeEvent 对象。
     */
    void preferenceChange(PreferenceChangeEvent evt);
}
