/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi.activation;

import java.io.Serializable;
import java.rmi.MarshalledObject;

/**
 * 激活描述符包含激活对象所需的信息： <ul>
 * <li> 对象的组标识符，
 * <li> 对象的完全限定类名，
 * <li> 对象的代码位置（类的位置），代码库 URL 路径，
 * <li> 对象的重启模式，以及
 * <li> 一个“序列化”对象，可以包含对象特定的初始化数据。 </ul>
 *
 * <p>注册到激活系统的描述符可以用于重新创建/激活描述符指定的对象。描述符中的
 * <code>MarshalledObject</code> 作为远程对象构造函数的第二个参数传递，
 * 用于对象在重新初始化/激活期间使用。
 *
 * @author      Ann Wollrath
 * @since       1.2
 * @see         java.rmi.activation.Activatable
 */
public final class ActivationDesc implements Serializable {

    /**
     * @serial 组的标识符
     */
    private ActivationGroupID groupID;

    /**
     * @serial 对象的类名
     */
    private String className;

    /**
     * @serial 对象的代码位置
     */
    private String location;

    /**
     * @serial 对象的初始化数据
     */
    private MarshalledObject<?> data;

    /**
     * @serial 表示对象是否应重启
     */
    private boolean restart;

    /** 表示与 Java 2 SDK v1.2 版本的类兼容 */
    private static final long serialVersionUID = 7455834104417690957L;

    /**
     * 构造一个对象描述符，其类名为 <code>className</code>，可以从
     * 代码 <code>location</code> 加载，其初始化信息为 <code>data</code>。如果使用此形式的构造函数，
     * <code>groupID</code> 默认为当前 VM 的 <code>ActivationGroup</code> 的当前 ID。所有具有相同
     * <code>ActivationGroupID</code> 的对象都在同一 VM 中激活。
     *
     * <p>请注意，使用此构造函数创建的描述符指定的对象仅在需要时激活（默认情况下，重启模式为 <code>false</code>）。
     * 如果可激活对象需要重启服务，请使用带有布尔参数 <code>restart</code> 的 <code>ActivationDesc</code> 构造函数之一。
     *
     * <p>如果当前 VM 没有激活组，此构造函数将抛出 <code>ActivationException</code>。要创建
     * <code>ActivationGroup</code>，请使用 <code>ActivationGroup.createGroup</code> 方法。
     *
     * @param className 对象的完全包限定类名
     * @param location 对象的代码位置（类的加载位置）
     * @param data 对象的初始化（激活）数据，以序列化形式包含。
     * @exception ActivationException 如果当前组不存在
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @since 1.2
     */
    public ActivationDesc(String className,
                          String location,
                          MarshalledObject<?> data)
        throws ActivationException
    {
        this(ActivationGroup.internalCurrentGroupID(),
             className, location, data, false);
    }

    /**
     * 构造一个对象描述符，其类名为 <code>className</code>，可以从
     * 代码 <code>location</code> 加载，其初始化信息为 <code>data</code>。如果使用此形式的构造函数，
     * <code>groupID</code> 默认为当前 VM 的 <code>ActivationGroup</code> 的当前 ID。所有具有相同
     * <code>ActivationGroupID</code> 的对象都在同一 VM 中激活。
     *
     * <p>如果当前 VM 没有激活组，此构造函数将抛出 <code>ActivationException</code>。要创建
     * <code>ActivationGroup</code>，请使用 <code>ActivationGroup.createGroup</code> 方法。
     *
     * @param className 对象的完全包限定类名
     * @param location 对象的代码位置（类的加载位置）
     * @param data 对象的初始化（激活）数据，以序列化形式包含。
     * @param restart 如果为 true，对象在激活器重启或对象的激活组在意外崩溃后重启时重启（重新激活）；
     * 如果为 false，对象仅在需要时激活。指定 <code>restart</code> 为 <code>true</code> 不会强制新注册的对象立即激活；
     * 初始激活是惰性的。
     * @exception ActivationException 如果当前组不存在
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @since 1.2
     */
    public ActivationDesc(String className,
                          String location,
                          MarshalledObject<?> data,
                          boolean restart)
        throws ActivationException
    {
        this(ActivationGroup.internalCurrentGroupID(),
             className, location, data, restart);
    }

    /**
     * 构造一个对象描述符，其类名为 <code>className</code>，可以从
     * 代码 <code>location</code> 加载，其初始化信息为 <code>data</code>。所有具有相同
     * <code>groupID</code> 的对象都在同一 Java VM 中激活。
     *
     * <p>请注意，使用此构造函数创建的描述符指定的对象仅在需要时激活（默认情况下，重启模式为 <code>false</code>）。
     * 如果可激活对象需要重启服务，请使用带有布尔参数 <code>restart</code> 的 <code>ActivationDesc</code> 构造函数之一。
     *
     * @param groupID 组的标识符（从注册 <code>ActivationSystem.registerGroup</code> 方法获得）。组表示对象应激活的 VM。
     * @param className 对象的完全包限定类名
     * @param location 对象的代码位置（类的加载位置）
     * @param data 对象的初始化（激活）数据，以序列化形式包含。
     * @exception IllegalArgumentException 如果 <code>groupID</code> 为 null
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @since 1.2
     */
    public ActivationDesc(ActivationGroupID groupID,
                          String className,
                          String location,
                          MarshalledObject<?> data)
    {
        this(groupID, className, location, data, false);
    }

    /**
     * 构造一个对象描述符，其类名为 <code>className</code>，可以从
     * 代码 <code>location</code> 加载，其初始化信息为 <code>data</code>。所有具有相同
     * <code>groupID</code> 的对象都在同一 Java VM 中激活。
     *
     * @param groupID 组的标识符（从注册 <code>ActivationSystem.registerGroup</code> 方法获得）。组表示对象应激活的 VM。
     * @param className 对象的完全包限定类名
     * @param location 对象的代码位置（类的加载位置）
     * @param data 对象的初始化（激活）数据，以序列化形式包含。
     * @param restart 如果为 true，对象在激活器重启或对象的激活组在意外崩溃后重启时重启（重新激活）；
     * 如果为 false，对象仅在需要时激活。指定 <code>restart</code> 为 <code>true</code> 不会强制新注册的对象立即激活；
     * 初始激活是惰性的。
     * @exception IllegalArgumentException 如果 <code>groupID</code> 为 null
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @since 1.2
     */
    public ActivationDesc(ActivationGroupID groupID,
                          String className,
                          String location,
                          MarshalledObject<?> data,
                          boolean restart)
    {
        if (groupID == null)
            throw new IllegalArgumentException("groupID can't be null");
        this.groupID = groupID;
        this.className = className;
        this.location = location;
        this.data = data;
        this.restart = restart;
    }

    /**
     * 返回此描述符指定的对象的组标识符。组提供了一种将对象聚合到单个 Java 虚拟机中的方式。RMI 创建/激活具有相同
     * <code>groupID</code> 的对象在同一个虚拟机中。
     *
     * @return 组标识符
     * @since 1.2
     */
    public ActivationGroupID getGroupID() {
        return groupID;
    }

    /**
     * 返回此描述符指定的对象的类名。
     * @return 类名
     * @since 1.2
     */
    public String getClassName() {
        return className;
    }

    /**
     * 返回此描述符指定的对象的代码位置。
     * @return 代码位置
     * @since 1.2
     */
    public String getLocation() {
        return location;
    }

    /**
     * 返回一个“序列化对象”，其中包含此描述符指定的对象的初始化/激活数据。
     * @return 对象特定的“初始化”数据
     * @since 1.2
     */
    public MarshalledObject<?> getData() {
        return data;
    }

    /**
     * 返回与此激活描述符关联的对象的“重启”模式。
     *
     * @return 如果与此激活描述符关联的可激活对象在激活守护进程启动或对象的组在意外崩溃后重启时重启，则返回 true；
     * 否则返回 false，表示对象仅在需要时通过方法调用激活。请注意，如果重启模式为 <code>true</code>，激活器不会强制新注册的对象立即激活；
     * 初始激活是惰性的。
     * @since 1.2
     */
    public boolean getRestartMode() {
        return restart;
    }

    /**
     * 比较两个激活描述符的内容是否相等。
     *
     * @param   obj     要比较的对象
     * @return  如果这些对象相等，则返回 true；否则返回 false。
     * @see             java.util.Hashtable
     * @since 1.2
     */
    public boolean equals(Object obj) {

        if (obj instanceof ActivationDesc) {
            ActivationDesc desc = (ActivationDesc) obj;
            return
                ((groupID == null ? desc.groupID == null :
                  groupID.equals(desc.groupID)) &&
                 (className == null ? desc.className == null :
                  className.equals(desc.className)) &&
                 (location == null ? desc.location == null:
                  location.equals(desc.location)) &&
                 (data == null ? desc.data == null :
                  data.equals(desc.data)) &&
                 (restart == desc.restart));

        } else {
            return false;
        }
    }

    /**
     * 为相似的 <code>ActivationDesc</code> 返回相同的 hashCode。
     * @return 一个整数
     * @see java.util.Hashtable
     */
    public int hashCode() {
        return ((location == null
                    ? 0
                    : location.hashCode() << 24) ^
                (groupID == null
                    ? 0
                    : groupID.hashCode() << 16) ^
                (className == null
                    ? 0
                    : className.hashCode() << 9) ^
                (data == null
                    ? 0
                    : data.hashCode() << 1) ^
                (restart
                    ? 1
                    : 0));
    }
}
