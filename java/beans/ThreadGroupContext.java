/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.beans.finder.BeanInfoFinder;
import com.sun.beans.finder.PropertyEditorFinder;

import java.awt.GraphicsEnvironment;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * {@code ThreadGroupContext} 是由特定 {@link ThreadGroup} 引用的应用程序依赖上下文。
 * 这是 {@link sun.awt.AppContext} 的替代品。
 *
 * @author  Sergey Malenkov
 */
final class ThreadGroupContext {

    private static final WeakIdentityMap<ThreadGroupContext> contexts = new WeakIdentityMap<ThreadGroupContext>() {
        protected ThreadGroupContext create(Object key) {
            return new ThreadGroupContext();
        }
    };

    /**
     * 返回调用者的适当 {@code ThreadGroupContext}，由其 {@code ThreadGroup} 确定。
     *
     * @return  应用程序依赖的上下文
     */
    static ThreadGroupContext getContext() {
        return contexts.get(Thread.currentThread().getThreadGroup());
    }

    private volatile boolean isDesignTime;
    private volatile Boolean isGuiAvailable;

    private Map<Class<?>, BeanInfo> beanInfoCache;
    private BeanInfoFinder beanInfoFinder;
    private PropertyEditorFinder propertyEditorFinder;

    private ThreadGroupContext() {
    }

    boolean isDesignTime() {
        return this.isDesignTime;
    }

    void setDesignTime(boolean isDesignTime) {
        this.isDesignTime = isDesignTime;
    }


    boolean isGuiAvailable() {
        Boolean isGuiAvailable = this.isGuiAvailable;
        return (isGuiAvailable != null)
                ? isGuiAvailable.booleanValue()
                : !GraphicsEnvironment.isHeadless();
    }

    void setGuiAvailable(boolean isGuiAvailable) {
        this.isGuiAvailable = Boolean.valueOf(isGuiAvailable);
    }


    BeanInfo getBeanInfo(Class<?> type) {
        return (this.beanInfoCache != null)
                ? this.beanInfoCache.get(type)
                : null;
    }

    BeanInfo putBeanInfo(Class<?> type, BeanInfo info) {
        if (this.beanInfoCache == null) {
            this.beanInfoCache = new WeakHashMap<>();
        }
        return this.beanInfoCache.put(type, info);
    }

    void removeBeanInfo(Class<?> type) {
        if (this.beanInfoCache != null) {
            this.beanInfoCache.remove(type);
        }
    }

    void clearBeanInfoCache() {
        if (this.beanInfoCache != null) {
            this.beanInfoCache.clear();
        }
    }


    synchronized BeanInfoFinder getBeanInfoFinder() {
        if (this.beanInfoFinder == null) {
            this.beanInfoFinder = new BeanInfoFinder();
        }
        return this.beanInfoFinder;
    }

    synchronized PropertyEditorFinder getPropertyEditorFinder() {
        if (this.propertyEditorFinder == null) {
            this.propertyEditorFinder = new PropertyEditorFinder();
        }
        return this.propertyEditorFinder;
    }
}
