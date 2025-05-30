/*
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * PropertyEditorManager 可用于为任何给定类型名称定位属性编辑器。此属性编辑器必须支持 java.beans.PropertyEditor 接口，用于编辑给定对象。
 * <P>
 * PropertyEditorManager 使用三种技术为给定类型定位编辑器。首先，它提供了一个 registerEditor 方法，允许为给定类型专门注册一个编辑器。其次，它尝试通过在给定类型的完整类名后添加 "Editor" 来定位合适的类（例如 "foo.bah.FozEditor"）。最后，它取简单类名（不带包名），添加 "Editor" 并在搜索路径中的包中查找匹配的类。
 * <P>
 * 因此，对于输入类 foo.bah.Fred，PropertyEditorManager 会首先查看其表中是否已为 foo.bah.Fred 注册了编辑器，如果已注册则使用该编辑器。然后它将查找 foo.bah.FredEditor 类。然后它将查找（例如）standardEditorsPackage.FredEditor 类。
 * <p>
 * 将为 Java 基本类型 "boolean"、"byte"、"short"、"int"、"long"、"float" 和 "double" 以及类 java.lang.String、java.awt.Color 和 java.awt.Font 提供默认的 PropertyEditors。
 */

public class PropertyEditorManager {

    /**
     * 为给定目标类注册一个编辑器类。如果编辑器类为 {@code null}，
     * 则会移除任何现有的定义。因此，此方法可用于取消注册。
     * 如果目标类或编辑器类被卸载，注册将自动取消。
     * <p>
     * 如果存在安全经理，将调用其 {@code checkPropertiesAccess} 方法。这可能导致 {@linkplain SecurityException}。
     *
     * @param targetType   要编辑的类型的类对象
     * @param editorClass  编辑器类的类对象
     * @throws SecurityException  如果存在安全经理且其 {@code checkPropertiesAccess} 方法不允许设置系统属性
     *
     * @see SecurityManager#checkPropertiesAccess
     */
    public static void registerEditor(Class<?> targetType, Class<?> editorClass) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }
        ThreadGroupContext.getContext().getPropertyEditorFinder().register(targetType, editorClass);
    }

    /**
     * 为给定目标类型定位一个值编辑器。
     *
     * @param targetType  要编辑的类型的类对象
     * @return 给定目标类的编辑器对象。如果没有找到合适的编辑器，则返回 null。
     */
    public static PropertyEditor findEditor(Class<?> targetType) {
        return ThreadGroupContext.getContext().getPropertyEditorFinder().find(targetType);
    }

    /**
     * 获取将用于查找属性编辑器的包名。
     *
     * @return  按顺序搜索以查找属性编辑器的包名数组。
     * <p>     此数组的默认值取决于实现，例如 Sun 实现最初设置为 {"sun.beans.editors"}。
     */
    public static String[] getEditorSearchPath() {
        return ThreadGroupContext.getContext().getPropertyEditorFinder().getPackages();
    }

    /**
     * 更改将用于查找属性编辑器的包名列表。
     *
     * <p>首先，如果存在安全经理，将调用其 <code>checkPropertiesAccess</code>
     * 方法。这可能导致 SecurityException。
     *
     * @param path  包名数组。
     * @exception  SecurityException  如果存在安全经理且其
     *             <code>checkPropertiesAccess</code> 方法不允许设置
     *              系统属性。
     * @see SecurityManager#checkPropertiesAccess
     */
    public static void setEditorSearchPath(String[] path) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }
        ThreadGroupContext.getContext().getPropertyEditorFinder().setPackages(path);
    }
}
