/*
 * Copyright (c) 2000, 2018, Oracle and/or its affiliates. All rights reserved.
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
 * 实现 <tt>PreferencesFactory</tt> 以返回 WindowsPreferences 对象。
 *
 * @author  Konstantin Kladko
 * @see Preferences
 * @see WindowsPreferences
 * @since 1.4
 */
class WindowsPreferencesFactory implements PreferencesFactory  {

    /**
     * 返回 WindowsPreferences.userRoot
     */
    public Preferences userRoot() {
        return WindowsPreferences.getUserRoot();
    }

    /**
     * 返回 WindowsPreferences.systemRoot
     */
    public Preferences systemRoot() {
        return WindowsPreferences.getSystemRoot();
    }
}
