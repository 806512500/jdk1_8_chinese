
/*
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.beans.TypeResolver;
import com.sun.beans.WeakCache;
import com.sun.beans.finder.ClassFinder;
import com.sun.beans.finder.MethodFinder;

import java.awt.Component;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.TreeMap;

import sun.reflect.misc.ReflectUtil;

/**
 * Introspector 类提供了一种标准方法，用于了解目标 Java Bean 支持的属性、事件和方法。
 * <p>
 * 对于这三种信息中的每一种，Introspector 将分别分析 bean 的类和超类，寻找显式或隐式信息，并使用这些信息构建一个 BeanInfo 对象，全面描述目标 bean。
 * <p>
 * 对于每种类型的类 "Foo"，如果存在一个对应的 "FooBeanInfo" 类，并且在查询信息时返回非空值，那么可以提供显式信息。我们首先通过将目标 bean 类的完整包限定名称加上 "BeanInfo" 来形成一个新的类名来查找 BeanInfo 类。如果这失败了，那么我们将在 BeanInfo 包搜索路径中查找该名称的类。
 * <p>
 * 因此，对于像 "sun.xyz.OurButton" 这样的类，我们首先查找一个名为 "sun.xyz.OurButtonBeanInfo" 的 BeanInfo 类，如果失败了，我们会在 BeanInfo 搜索路径中的每个包中查找 OurButtonBeanInfo 类。使用默认搜索路径，这意味着查找 "sun.beans.infos.OurButtonBeanInfo"。
 * <p>
 * 如果一个类提供了关于自身的显式 BeanInfo，我们将这些信息添加到从分析派生类获得的 BeanInfo 信息中，但认为显式信息对于当前类及其基类是确定性的，并且不会继续向上分析超类链。
 * <p>
 * 如果我们在类上没有找到显式 BeanInfo，我们将使用低级反射来研究类的方法，并应用标准设计模式来识别属性访问器、事件源或公共方法。然后我们继续分析类的超类，并添加从超类获得的信息（可能继续向上分析超类链）。
 * <p>
 * 有关内省和设计模式的更多信息，请参阅
 *  <a href="http://www.oracle.com/technetwork/java/javase/documentation/spec-136004.html">JavaBeans&trade; 规范</a>。
 */

public class Introspector {

    // 可用于控制 getBeanInfo 的标志：
    /**
     * 标志表示使用所有 BeanInfo。
     */
    public final static int USE_ALL_BEANINFO           = 1;
    /**
     * 标志表示忽略直接的 BeanInfo。
     */
    public final static int IGNORE_IMMEDIATE_BEANINFO  = 2;
    /**
     * 标志表示忽略所有 BeanInfo。
     */
    public final static int IGNORE_ALL_BEANINFO        = 3;

    // 静态缓存以加速内省。
    private static final WeakCache<Class<?>, Method[]> declaredMethodCache = new WeakCache<>();

    private Class<?> beanClass;
    private BeanInfo explicitBeanInfo;
    private BeanInfo superBeanInfo;
    private BeanInfo additionalBeanInfo[];

    private boolean propertyChangeSource = false;
    private static Class<EventListener> eventListenerType = EventListener.class;

    // 这些应该被移除。
    private String defaultEventName;
    private String defaultPropertyName;
    private int defaultEventIndex = -1;
    private int defaultPropertyIndex = -1;

    // 方法映射从方法名称到 MethodDescriptors
    private Map<String, MethodDescriptor> methods;

    // 属性映射从字符串名称到 PropertyDescriptors
    private Map<String, PropertyDescriptor> properties;

    // 事件映射从字符串名称到 EventSetDescriptors
    private Map<String, EventSetDescriptor> events;

    private final static EventSetDescriptor[] EMPTY_EVENTSETDESCRIPTORS = new EventSetDescriptor[0];

    static final String ADD_PREFIX = "add";
    static final String REMOVE_PREFIX = "remove";
    static final String GET_PREFIX = "get";
    static final String SET_PREFIX = "set";
    static final String IS_PREFIX = "is";

    //======================================================================
    //                          公共方法
    //======================================================================

    /**
     * 对 Java Bean 进行内省，了解其所有属性、公开的方法和事件。
     * <p>
     * 如果 Java Bean 的 BeanInfo 类之前已经内省过，那么 BeanInfo 类将从 BeanInfo 缓存中检索。
     *
     * @param beanClass  要分析的 bean 类。
     * @return  描述目标 bean 的 BeanInfo 对象。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     * @see #flushCaches
     * @see #flushFromCaches
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass)
        throws IntrospectionException
    {
        if (!ReflectUtil.isPackageAccessible(beanClass)) {
            return (new Introspector(beanClass, null, USE_ALL_BEANINFO)).getBeanInfo();
        }
        ThreadGroupContext context = ThreadGroupContext.getContext();
        BeanInfo beanInfo;
        synchronized (declaredMethodCache) {
            beanInfo = context.getBeanInfo(beanClass);
        }
        if (beanInfo == null) {
            beanInfo = new Introspector(beanClass, null, USE_ALL_BEANINFO).getBeanInfo();
            synchronized (declaredMethodCache) {
                context.putBeanInfo(beanClass, beanInfo);
            }
        }
        return beanInfo;
    }

    /**
     * 对 Java bean 进行内省，了解其所有属性、公开的方法和事件，并受某些控制标志的影响。
     * <p>
     * 如果 Java Bean 的 BeanInfo 类之前已经基于相同的参数内省过，那么 BeanInfo 类将从 BeanInfo 缓存中检索。
     *
     * @param beanClass  要分析的 bean 类。
     * @param flags  控制内省的标志。
     *     如果 flags == USE_ALL_BEANINFO，则使用我们发现的所有 BeanInfo 类。
     *     如果 flags == IGNORE_IMMEDIATE_BEANINFO，则忽略与指定 beanClass 关联的任何 BeanInfo。
     *     如果 flags == IGNORE_ALL_BEANINFO，则忽略与指定 beanClass 或其任何父类关联的所有 BeanInfo。
     * @return  描述目标 bean 的 BeanInfo 对象。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass, int flags)
                                                throws IntrospectionException {
        return getBeanInfo(beanClass, null, flags);
    }

    /**
     * 对 Java bean 进行内省，了解其所有属性、公开的方法，直到给定的“停止”点。
     * <p>
     * 如果 Java Bean 的 BeanInfo 类之前已经基于相同的参数内省过，那么 BeanInfo 类将从 BeanInfo 缓存中检索。
     * @return bean 的 BeanInfo
     * @param beanClass 要分析的 bean 类。
     * @param stopClass 停止分析的基类。在 stopClass 或其基类中的任何方法/属性/事件将被忽略。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass, Class<?> stopClass)
                                                throws IntrospectionException {
        return getBeanInfo(beanClass, stopClass, USE_ALL_BEANINFO);
    }

    /**
     * 对 Java Bean 进行内省，了解其所有属性、公开的方法和事件，直到给定的 {@code stopClass} 点，并受某些控制 {@code flags} 的影响。
     * <dl>
     *  <dt>USE_ALL_BEANINFO</dt>
     *  <dd>可以发现的所有 BeanInfo 都将被使用。</dd>
     *  <dt>IGNORE_IMMEDIATE_BEANINFO</dt>
     *  <dd>与指定的 {@code beanClass} 关联的任何 BeanInfo 将被忽略。</dd>
     *  <dt>IGNORE_ALL_BEANINFO</dt>
     *  <dd>与指定的 {@code beanClass} 或其任何父类关联的所有 BeanInfo 将被忽略。</dd>
     * </dl>
     * 在 {@code stopClass} 或其父类中的任何方法/属性/事件将被忽略。
     * <p>
     * 如果 Java Bean 的 BeanInfo 类之前已经基于相同的参数内省过，那么 BeanInfo 类将从 BeanInfo 缓存中检索。
     *
     * @param beanClass  要分析的 bean 类。
     * @param stopClass  停止分析的父类。
     * @param flags      控制内省的标志。
     * @return 描述目标 bean 的 BeanInfo 对象。
     * @exception IntrospectionException 如果在内省过程中发生异常。
     *
     * @since 1.7
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass, Class<?> stopClass,
                                        int flags) throws IntrospectionException {
        BeanInfo bi;
        if (stopClass == null && flags == USE_ALL_BEANINFO) {
            // 相同的参数以利用缓存。
            bi = getBeanInfo(beanClass);
        } else {
            bi = (new Introspector(beanClass, stopClass, flags)).getBeanInfo();
        }
        return bi;

        // 旧的行为：创建 BeanInfo 的独立副本。
        //return new GenericBeanInfo(bi);
    }


    /**
     * 实用方法，将字符串转换为正常的 Java 变量名称大小写。这通常意味着将第一个字符从大写转换为小写，但在（不常见）特殊情况下，当字符串长度大于1且前两个字符都是大写时，我们保持不变。
     * <p>
     * 因此 "FooBah" 变为 "fooBah"，"X" 变为 "x"，但 "URL" 保持为 "URL"。
     *
     * @param  name 要首字母小写的字符串。
     * @return  首字母小写的字符串版本。
     */
    public static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
                        Character.isUpperCase(name.charAt(0))){
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    /**
     * 获取将用于查找 BeanInfo 类的包名称列表。
     *
     * @return  用于查找 BeanInfo 类的包名称数组。此数组的默认值取决于实现；例如，Sun 实现最初将其设置为 {"sun.beans.infos"}。
     */

    public static String[] getBeanInfoSearchPath() {
        return ThreadGroupContext.getContext().getBeanInfoFinder().getPackages();
    }

    /**
     * 更改将用于查找 BeanInfo 类的包名称列表。如果参数 path 为 null，此方法的行为是未定义的。
     *
     * <p>首先，如果有安全经理，将调用其 <code>checkPropertiesAccess</code> 方法。这可能导致 SecurityException。
     *
     * @param path  包名称数组。
     * @exception  SecurityException  如果存在安全经理且其
     *             <code>checkPropertiesAccess</code> 方法不允许设置系统属性。
     * @see SecurityManager#checkPropertiesAccess
     */

    public static void setBeanInfoSearchPath(String[] path) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }
        ThreadGroupContext.getContext().getBeanInfoFinder().setPackages(path);
    }


    /**
     * 刷新 Introspector 的所有内部缓存。此方法通常不需要。通常只有高级工具在原地更新现有 "Class" 对象并需要使 Introspector 重新分析现有 Class 对象时才需要。
     */

    public static void flushCaches() {
        synchronized (declaredMethodCache) {
            ThreadGroupContext.getContext().clearBeanInfoCache();
            declaredMethodCache.clear();
        }
    }

    /**
     * 刷新给定类的 Introspector 的内部缓存信息。此方法通常不需要。通常只有高级工具在原地更新现有 "Class" 对象并需要使 Introspector 重新分析现有 Class 对象时才需要。
     *
     * 注意，只有与目标 Class 对象直接关联的状态才会被刷新。我们不会刷新具有相同名称的其他 Class 对象的状态，也不会刷新任何相关 Class 对象（如子类）的状态，即使这些状态可能包括从目标 Class 对象间接获得的信息。
     *
     * @param clz  要刷新的 Class 对象。
     * @throws NullPointerException 如果 Class 对象为 null。
     */
    public static void flushFromCaches(Class<?> clz) {
        if (clz == null) {
            throw new NullPointerException();
        }
        synchronized (declaredMethodCache) {
            ThreadGroupContext.getContext().removeBeanInfo(clz);
            declaredMethodCache.put(clz, null);
        }
    }


    //======================================================================
    //                  Private implementation methods
    //======================================================================

    private Introspector(Class<?> beanClass, Class<?> stopClass, int flags)
                                            throws IntrospectionException {
        this.beanClass = beanClass;

        // 检查 stopClass 是否是 startClass 的超类。
        if (stopClass != null) {
            boolean isSuper = false;
            for (Class<?> c = beanClass.getSuperclass(); c != null; c = c.getSuperclass()) {
                if (c == stopClass) {
                    isSuper = true;
                }
            }
            if (!isSuper) {
                throw new IntrospectionException(stopClass.getName() + " not superclass of " +
                                        beanClass.getName());
            }
        }

        if (flags == USE_ALL_BEANINFO) {
            explicitBeanInfo = findExplicitBeanInfo(beanClass);
        }

        Class<?> superClass = beanClass.getSuperclass();
        if (superClass != stopClass) {
            int newFlags = flags;
            if (newFlags == IGNORE_IMMEDIATE_BEANINFO) {
                newFlags = USE_ALL_BEANINFO;
            }
            superBeanInfo = getBeanInfo(superClass, stopClass, newFlags);
        }
        if (explicitBeanInfo != null) {
            additionalBeanInfo = explicitBeanInfo.getAdditionalBeanInfo();
        }
        if (additionalBeanInfo == null) {
            additionalBeanInfo = new BeanInfo[0];
        }
    }

    /**
     * 从 Introspector 的状态构造一个 GenericBeanInfo 类。
     */
    private BeanInfo getBeanInfo() throws IntrospectionException {

        // 这里的评估顺序很重要，因为我们会在查找属性之前评估事件集并定位 PropertyChangeListeners。
        BeanDescriptor bd = getTargetBeanDescriptor();
        MethodDescriptor mds[] = getTargetMethodInfo();
        EventSetDescriptor esds[] = getTargetEventInfo();
        PropertyDescriptor pds[] = getTargetPropertyInfo();

        int defaultEvent = getTargetDefaultEventIndex();
        int defaultProperty = getTargetDefaultPropertyIndex();

        return new GenericBeanInfo(bd, esds, defaultEvent, pds,
                        defaultProperty, mds, explicitBeanInfo);

    }

    /**
     * 查找与 Class 对应的显式 BeanInfo 类。
     * 首先在定义 Class 的现有包中查找，然后检查类是否是其自己的 BeanInfo。
     * 最后，将 BeanInfo 搜索路径前置到类并进行搜索。
     *
     * @param beanClass  要查找的 bean 的类类型
     * @return 显式 BeanInfo 类的实例，如果未找到则返回 null。
     */
    private static BeanInfo findExplicitBeanInfo(Class<?> beanClass) {
        return ThreadGroupContext.getContext().getBeanInfoFinder().find(beanClass);
    }

    /**
     * 返回一个 PropertyDescriptor 数组，描述目标 bean 支持的可编辑属性。
     */

    private PropertyDescriptor[] getTargetPropertyInfo() {

        // 检查 bean 是否有自己的 BeanInfo 以提供显式信息。
        PropertyDescriptor[] explicitProperties = null;
        if (explicitBeanInfo != null) {
            explicitProperties = getPropertyDescriptors(this.explicitBeanInfo);
        }

        if (explicitProperties == null && superBeanInfo != null) {
            // 我们没有显式的 BeanInfo 属性。检查父类。
            addPropertyDescriptors(getPropertyDescriptors(this.superBeanInfo));
        }

        for (int i = 0; i < additionalBeanInfo.length; i++) {
            addPropertyDescriptors(additionalBeanInfo[i].getPropertyDescriptors());
        }

        if (explicitProperties != null) {
            // 将显式的 BeanInfo 数据添加到结果中。
            addPropertyDescriptors(explicitProperties);

        } else {

            // 对当前类应用一些反射。

            // 首先获取此级别的所有公共方法的数组
            Method methodList[] = getPublicDeclaredMethods(beanClass);

            // 现在分析每个方法。
            for (int i = 0; i < methodList.length; i++) {
                Method method = methodList[i];
                if (method == null) {
                    continue;
                }
                // 跳过静态方法。
                int mods = method.getModifiers();
                if (Modifier.isStatic(mods)) {
                    continue;
                }
                String name = method.getName();
                Class<?>[] argTypes = method.getParameterTypes();
                Class<?> resultType = method.getReturnType();
                int argCount = argTypes.length;
                PropertyDescriptor pd = null;

                if (name.length() <= 3 && !name.startsWith(IS_PREFIX)) {
                    // 优化。不要处理无效的属性名。
                    continue;
                }

                try {

                    if (argCount == 0) {
                        if (name.startsWith(GET_PREFIX)) {
                            // 简单的 getter
                            pd = new PropertyDescriptor(this.beanClass, name.substring(3), method, null);
                        } else if (resultType == boolean.class && name.startsWith(IS_PREFIX)) {
                            // 布尔 getter
                            pd = new PropertyDescriptor(this.beanClass, name.substring(2), method, null);
                        }
                    } else if (argCount == 1) {
                        if (int.class.equals(argTypes[0]) && name.startsWith(GET_PREFIX)) {
                            pd = new IndexedPropertyDescriptor(this.beanClass, name.substring(3), null, null, method, null);
                        } else if (void.class.equals(resultType) && name.startsWith(SET_PREFIX)) {
                            // 简单的 setter
                            pd = new PropertyDescriptor(this.beanClass, name.substring(3), null, method);
                            if (throwsException(method, PropertyVetoException.class)) {
                                pd.setConstrained(true);
                            }
                        }
                    } else if (argCount == 2) {
                            if (void.class.equals(resultType) && int.class.equals(argTypes[0]) && name.startsWith(SET_PREFIX)) {
                            pd = new IndexedPropertyDescriptor(this.beanClass, name.substring(3), null, null, null, method);
                            if (throwsException(method, PropertyVetoException.class)) {
                                pd.setConstrained(true);
                            }
                        }
                    }
                } catch (IntrospectionException ex) {
                    // 如果 PropertyDescriptor 或 IndexedPropertyDescriptor
                    // 构造函数发现方法违反了设计模式的细节，
                    // 例如名称为空，或 getter 返回 void 等。
                    pd = null;
                }

                if (pd != null) {
                    // 如果此类或其基类是 PropertyChange
                    // 源，则假设我们发现的任何属性都是 "bound"。
                    if (propertyChangeSource) {
                        pd.setBound(true);
                    }
                    addPropertyDescriptor(pd);
                }
            }
        }
        processPropertyDescriptors();

        // 分配并填充结果数组。
        PropertyDescriptor result[] =
                properties.values().toArray(new PropertyDescriptor[properties.size()]);

        // 设置默认索引。
        if (defaultPropertyName != null) {
            for (int i = 0; i < result.length; i++) {
                if (defaultPropertyName.equals(result[i].getName())) {
                    defaultPropertyIndex = i;
                }
            }
        }

        return result;
    }

    private HashMap<String, List<PropertyDescriptor>> pdStore = new HashMap<>();

    /**
     * 将属性描述符添加到列表存储中。
     */
    private void addPropertyDescriptor(PropertyDescriptor pd) {
        String propName = pd.getName();
        List<PropertyDescriptor> list = pdStore.get(propName);
        if (list == null) {
            list = new ArrayList<>();
            pdStore.put(propName, list);
        }
        if (this.beanClass != pd.getClass0()) {
            // 仅在我们有需要解析的类型时
            // 替换现有的属性描述符
            // 在 this.beanClass 的上下文中
            Method read = pd.getReadMethod();
            Method write = pd.getWriteMethod();
            boolean cls = true;
            if (read != null) cls = cls && read.getGenericReturnType() instanceof Class;
            if (write != null) cls = cls && write.getGenericParameterTypes()[0] instanceof Class;
            if (pd instanceof IndexedPropertyDescriptor) {
                IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
                Method readI = ipd.getIndexedReadMethod();
                Method writeI = ipd.getIndexedWriteMethod();
                if (readI != null) cls = cls && readI.getGenericReturnType() instanceof Class;
                if (writeI != null) cls = cls && writeI.getGenericParameterTypes()[1] instanceof Class;
                if (!cls) {
                    pd = new IndexedPropertyDescriptor(ipd);
                    pd.updateGenericsFor(this.beanClass);
                }
            }
            else if (!cls) {
                pd = new PropertyDescriptor(pd);
                pd.updateGenericsFor(this.beanClass);
            }
        }
        list.add(pd);
    }

    private void addPropertyDescriptors(PropertyDescriptor[] descriptors) {
        if (descriptors != null) {
            for (PropertyDescriptor descriptor : descriptors) {
                addPropertyDescriptor(descriptor);
            }
        }
    }

    private PropertyDescriptor[] getPropertyDescriptors(BeanInfo info) {
        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
        int index = info.getDefaultPropertyIndex();
        if ((0 <= index) && (index < descriptors.length)) {
            this.defaultPropertyName = descriptors[index].getName();
        }
        return descriptors;
    }

    /**
     * 通过合并属性描述符列表来填充属性描述符表。
     */
    private void processPropertyDescriptors() {
        if (properties == null) {
            properties = new TreeMap<>();
        }

        List<PropertyDescriptor> list;

        PropertyDescriptor pd, gpd, spd;
        IndexedPropertyDescriptor ipd, igpd, ispd;

        Iterator<List<PropertyDescriptor>> it = pdStore.values().iterator();
        while (it.hasNext()) {
            pd = null; gpd = null; spd = null;
            ipd = null; igpd = null; ispd = null;

            list = it.next();

            // 第一次遍历。找到最新的 getter 方法。合并之前的 getter 方法的属性。
            for (int i = 0; i < list.size(); i++) {
                pd = list.get(i);
                if (pd instanceof IndexedPropertyDescriptor) {
                    ipd = (IndexedPropertyDescriptor)pd;
                    if (ipd.getIndexedReadMethod() != null) {
                        if (igpd != null) {
                            igpd = new IndexedPropertyDescriptor(igpd, ipd);
                        } else {
                            igpd = ipd;
                        }
                    }
                } else {
                    if (pd.getReadMethod() != null) {
                        String pdName = pd.getReadMethod().getName();
                        if (gpd != null) {
                            // 如果现有的读取方法以 "is" 开头，则不要替换它
                            String gpdName = gpd.getReadMethod().getName();
                            if (gpdName.equals(pdName) || !gpdName.startsWith(IS_PREFIX)) {
                                gpd = new PropertyDescriptor(gpd, pd);
                            }
                        } else {
                            gpd = pd;
                        }
                    }
                }
            }

            // 第二次遍历。找到与 getter 方法类型相同的最新的 setter 方法。
            for (int i = 0; i < list.size(); i++) {
                pd = list.get(i);
                if (pd instanceof IndexedPropertyDescriptor) {
                    ipd = (IndexedPropertyDescriptor)pd;
                    if (ipd.getIndexedWriteMethod() != null) {
                        if (igpd != null) {
                            if (isAssignable(igpd.getIndexedPropertyType(), ipd.getIndexedPropertyType())) {
                                if (ispd != null) {
                                    ispd = new IndexedPropertyDescriptor(ispd, ipd);
                                } else {
                                    ispd = ipd;
                                }
                            }
                        } else {
                            if (ispd != null) {
                                ispd = new IndexedPropertyDescriptor(ispd, ipd);
                            } else {
                                ispd = ipd;
                            }
                        }
                    }
                } else {
                    if (pd.getWriteMethod() != null) {
                        if (gpd != null) {
                            if (isAssignable(gpd.getPropertyType(), pd.getPropertyType())) {
                                if (spd != null) {
                                    spd = new PropertyDescriptor(spd, pd);
                                } else {
                                    spd = pd;
                                }
                            }
                        } else {
                            if (spd != null) {
                                spd = new PropertyDescriptor(spd, pd);
                            } else {
                                spd = pd;
                            }
                        }
                    }
                }
            }

            // 此时我们应该有代表 getter 和 setter 的 PDs 或 IPDs。
            // 属性描述符的确定顺序代表属性的优先级。
            pd = null; ipd = null;

            if (igpd != null && ispd != null) {
                // 完整的索引属性集
                // 合并任何经典属性描述符
                if ((gpd == spd) || (gpd == null)) {
                    pd = spd;
                } else if (spd == null) {
                    pd = gpd;
                } else if (spd instanceof IndexedPropertyDescriptor) {
                    pd = mergePropertyWithIndexedProperty(gpd, (IndexedPropertyDescriptor) spd);
                } else if (gpd instanceof IndexedPropertyDescriptor) {
                    pd = mergePropertyWithIndexedProperty(spd, (IndexedPropertyDescriptor) gpd);
                } else {
                    pd = mergePropertyDescriptor(gpd, spd);
                }
                if (igpd == ispd) {
                    ipd = igpd;
                } else {
                    ipd = mergePropertyDescriptor(igpd, ispd);
                }
                if (pd == null) {
                    pd = ipd;
                } else {
                    Class<?> propType = pd.getPropertyType();
                    Class<?> ipropType = ipd.getIndexedPropertyType();
                    if (propType.isArray() && propType.getComponentType() == ipropType) {
                        pd = pd.getClass0().isAssignableFrom(ipd.getClass0())
                                ? new IndexedPropertyDescriptor(pd, ipd)
                                : new IndexedPropertyDescriptor(ipd, pd);
                    } else if (pd.getClass0().isAssignableFrom(ipd.getClass0())) {
                        pd = pd.getClass0().isAssignableFrom(ipd.getClass0())
                                ? new PropertyDescriptor(pd, ipd)
                                : new PropertyDescriptor(ipd, pd);
                    } else {
                        pd = ipd;
                    }
                }
            } else if (gpd != null && spd != null) {
                if (igpd != null) {
                    gpd = mergePropertyWithIndexedProperty(gpd, igpd);
                }
                if (ispd != null) {
                    spd = mergePropertyWithIndexedProperty(spd, ispd);
                }
                // 完整的简单属性集
                if (gpd == spd) {
                    pd = gpd;
                } else if (spd instanceof IndexedPropertyDescriptor) {
                    pd = mergePropertyWithIndexedProperty(gpd, (IndexedPropertyDescriptor) spd);
                } else if (gpd instanceof IndexedPropertyDescriptor) {
                    pd = mergePropertyWithIndexedProperty(spd, (IndexedPropertyDescriptor) gpd);
                } else {
                    pd = mergePropertyDescriptor(gpd, spd);
                }
            } else if (ispd != null) {
                // 索引 setter
                pd = ispd;
                // 合并任何经典属性描述符
                if (spd != null) {
                    pd = mergePropertyDescriptor(ispd, spd);
                }
                if (gpd != null) {
                    pd = mergePropertyDescriptor(ispd, gpd);
                }
            } else if (igpd != null) {
                // 索引 getter
                pd = igpd;
                // 合并任何经典属性描述符
                if (gpd != null) {
                    pd = mergePropertyDescriptor(igpd, gpd);
                }
                if (spd != null) {
                    pd = mergePropertyDescriptor(igpd, spd);
                }
            } else if (spd != null) {
                // 简单 setter
                pd = spd;
            } else if (gpd != null) {
                // 简单 getter
                pd = gpd;
            }


                        // 特殊情况，确保 IndexedPropertyDescriptor
            // 不包含比封装的 PropertyDescriptor 更少的信息。如果确实包含更少的信息，则重新创建为
            // PropertyDescriptor。参见 4168833
            if (pd instanceof IndexedPropertyDescriptor) {
                ipd = (IndexedPropertyDescriptor)pd;
                if (ipd.getIndexedReadMethod() == null && ipd.getIndexedWriteMethod() == null) {
                    pd = new PropertyDescriptor(ipd);
                }
            }

            // 查找第一个没有 getter 和 setter 方法的属性描述符。
            // 参见回归错误 4984912。
            if ( (pd == null) && (list.size() > 0) ) {
                pd = list.get(0);
            }

            if (pd != null) {
                properties.put(pd.getName(), pd);
            }
        }
    }

    private static boolean isAssignable(Class<?> current, Class<?> candidate) {
        return ((current == null) || (candidate == null)) ? current == candidate : current.isAssignableFrom(candidate);
    }

    private PropertyDescriptor mergePropertyWithIndexedProperty(PropertyDescriptor pd, IndexedPropertyDescriptor ipd) {
        Class<?> type = pd.getPropertyType();
        if (type.isArray() && (type.getComponentType() == ipd.getIndexedPropertyType())) {
            return pd.getClass0().isAssignableFrom(ipd.getClass0())
                    ? new IndexedPropertyDescriptor(pd, ipd)
                    : new IndexedPropertyDescriptor(ipd, pd);
        }
        return pd;
    }

    /**
     * 仅当类型相同时，将属性描述符添加到索引属性描述符中。
     *
     * 最具体的属性描述符将优先。
     */
    private PropertyDescriptor mergePropertyDescriptor(IndexedPropertyDescriptor ipd,
                                                       PropertyDescriptor pd) {
        PropertyDescriptor result = null;

        Class<?> propType = pd.getPropertyType();
        Class<?> ipropType = ipd.getIndexedPropertyType();

        if (propType.isArray() && propType.getComponentType() == ipropType) {
            if (pd.getClass0().isAssignableFrom(ipd.getClass0())) {
                result = new IndexedPropertyDescriptor(pd, ipd);
            } else {
                result = new IndexedPropertyDescriptor(ipd, pd);
            }
        } else if ((ipd.getReadMethod() == null) && (ipd.getWriteMethod() == null)) {
            if (pd.getClass0().isAssignableFrom(ipd.getClass0())) {
                result = new PropertyDescriptor(pd, ipd);
            } else {
                result = new PropertyDescriptor(ipd, pd);
            }
        } else {
            // 由于类型不匹配，无法合并 pd
            // 返回最具体的 pd
            if (pd.getClass0().isAssignableFrom(ipd.getClass0())) {
                result = ipd;
            } else {
                result = pd;
                // 尝试添加在类型更改中可能丢失的方法
                // 参见 4168833
                Method write = result.getWriteMethod();
                Method read = result.getReadMethod();

                if (read == null && write != null) {
                    read = findMethod(result.getClass0(),
                                      GET_PREFIX + NameGenerator.capitalize(result.getName()), 0);
                    if (read != null) {
                        try {
                            result.setReadMethod(read);
                        } catch (IntrospectionException ex) {
                            // 失败没有后果。
                        }
                    }
                }
                if (write == null && read != null) {
                    write = findMethod(result.getClass0(),
                                       SET_PREFIX + NameGenerator.capitalize(result.getName()), 1,
                                       new Class<?>[] { FeatureDescriptor.getReturnType(result.getClass0(), read) });
                    if (write != null) {
                        try {
                            result.setWriteMethod(write);
                        } catch (IntrospectionException ex) {
                            // 失败没有后果。
                        }
                    }
                }
            }
        }
        return result;
    }

    // 处理常规 pd 合并
    private PropertyDescriptor mergePropertyDescriptor(PropertyDescriptor pd1,
                                                       PropertyDescriptor pd2) {
        if (pd1.getClass0().isAssignableFrom(pd2.getClass0())) {
            return new PropertyDescriptor(pd1, pd2);
        } else {
            return new PropertyDescriptor(pd2, pd1);
        }
    }

    // 处理常规 ipd 合并
    private IndexedPropertyDescriptor mergePropertyDescriptor(IndexedPropertyDescriptor ipd1,
                                                       IndexedPropertyDescriptor ipd2) {
        if (ipd1.getClass0().isAssignableFrom(ipd2.getClass0())) {
            return new IndexedPropertyDescriptor(ipd1, ipd2);
        } else {
            return new IndexedPropertyDescriptor(ipd2, ipd1);
        }
    }

    /**
     * @return 一个 EventSetDescriptor 数组，描述目标 bean 触发的事件类型。
     */
    private EventSetDescriptor[] getTargetEventInfo() throws IntrospectionException {
        if (events == null) {
            events = new HashMap<>();
        }

        // 检查 bean 是否有自己的 BeanInfo，提供明确的信息。
        EventSetDescriptor[] explicitEvents = null;
        if (explicitBeanInfo != null) {
            explicitEvents = explicitBeanInfo.getEventSetDescriptors();
            int ix = explicitBeanInfo.getDefaultEventIndex();
            if (ix >= 0 && ix < explicitEvents.length) {
                defaultEventName = explicitEvents[ix].getName();
            }
        }

        if (explicitEvents == null && superBeanInfo != null) {
            // 没有显式的 BeanInfo 事件。检查父级。
            EventSetDescriptor supers[] = superBeanInfo.getEventSetDescriptors();
            for (int i = 0 ; i < supers.length; i++) {
                addEvent(supers[i]);
            }
            int ix = superBeanInfo.getDefaultEventIndex();
            if (ix >= 0 && ix < supers.length) {
                defaultEventName = supers[ix].getName();
            }
        }

        for (int i = 0; i < additionalBeanInfo.length; i++) {
            EventSetDescriptor additional[] = additionalBeanInfo[i].getEventSetDescriptors();
            if (additional != null) {
                for (int j = 0 ; j < additional.length; j++) {
                    addEvent(additional[j]);
                }
            }
        }

        if (explicitEvents != null) {
            // 将显式的 explicitBeanInfo 数据添加到结果中。
            for (int i = 0 ; i < explicitEvents.length; i++) {
                addEvent(explicitEvents[i]);
            }

        } else {

            // 对当前类应用一些反射。

            // 获取此级别的所有公共 bean 方法的数组
            Method methodList[] = getPublicDeclaredMethods(beanClass);

            // 查找所有合适的 "add"、"remove" 和 "get" Listener 方法
            // 监听器类型的名称是这些哈希表的键
            // 例如，ActionListener
            Map<String, Method> adds = null;
            Map<String, Method> removes = null;
            Map<String, Method> gets = null;

            for (int i = 0; i < methodList.length; i++) {
                Method method = methodList[i];
                if (method == null) {
                    continue;
                }
                // 跳过静态方法。
                int mods = method.getModifiers();
                if (Modifier.isStatic(mods)) {
                    continue;
                }
                String name = method.getName();
                // 优化，避免 getParameterTypes
                if (!name.startsWith(ADD_PREFIX) && !name.startsWith(REMOVE_PREFIX)
                    && !name.startsWith(GET_PREFIX)) {
                    continue;
                }

                if (name.startsWith(ADD_PREFIX)) {
                    Class<?> returnType = method.getReturnType();
                    if (returnType == void.class) {
                        Type[] parameterTypes = method.getGenericParameterTypes();
                        if (parameterTypes.length == 1) {
                            Class<?> type = TypeResolver.erase(TypeResolver.resolveInClass(beanClass, parameterTypes[0]));
                            if (Introspector.isSubclass(type, eventListenerType)) {
                                String listenerName = name.substring(3);
                                if (listenerName.length() > 0 &&
                                    type.getName().endsWith(listenerName)) {
                                    if (adds == null) {
                                        adds = new HashMap<>();
                                    }
                                    adds.put(listenerName, method);
                                }
                            }
                        }
                    }
                }
                else if (name.startsWith(REMOVE_PREFIX)) {
                    Class<?> returnType = method.getReturnType();
                    if (returnType == void.class) {
                        Type[] parameterTypes = method.getGenericParameterTypes();
                        if (parameterTypes.length == 1) {
                            Class<?> type = TypeResolver.erase(TypeResolver.resolveInClass(beanClass, parameterTypes[0]));
                            if (Introspector.isSubclass(type, eventListenerType)) {
                                String listenerName = name.substring(6);
                                if (listenerName.length() > 0 &&
                                    type.getName().endsWith(listenerName)) {
                                    if (removes == null) {
                                        removes = new HashMap<>();
                                    }
                                    removes.put(listenerName, method);
                                }
                            }
                        }
                    }
                }
                else if (name.startsWith(GET_PREFIX)) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 0) {
                        Class<?> returnType = FeatureDescriptor.getReturnType(beanClass, method);
                        if (returnType.isArray()) {
                            Class<?> type = returnType.getComponentType();
                            if (Introspector.isSubclass(type, eventListenerType)) {
                                String listenerName  = name.substring(3, name.length() - 1);
                                if (listenerName.length() > 0 &&
                                    type.getName().endsWith(listenerName)) {
                                    if (gets == null) {
                                        gets = new HashMap<>();
                                    }
                                    gets.put(listenerName, method);
                                }
                            }
                        }
                    }
                }
            }

            if (adds != null && removes != null) {
                // 现在查找匹配的 addFooListener+removeFooListener 对。
                // 如果有匹配的 getFooListeners 方法则更好。
                Iterator<String> keys = adds.keySet().iterator();
                while (keys.hasNext()) {
                    String listenerName = keys.next();
                    // 跳过任何没有匹配的 "remove" 或监听器名称不以 Listener 结尾的 "add"。
                    if (removes.get(listenerName) == null || !listenerName.endsWith("Listener")) {
                        continue;
                    }
                    String eventName = decapitalize(listenerName.substring(0, listenerName.length()-8));
                    Method addMethod = adds.get(listenerName);
                    Method removeMethod = removes.get(listenerName);
                    Method getMethod = null;
                    if (gets != null) {
                        getMethod = gets.get(listenerName);
                    }
                    Class<?> argType = FeatureDescriptor.getParameterTypes(beanClass, addMethod)[0];

                    // 为每个目标方法生成一个 Method 对象列表：
                    Method allMethods[] = getPublicDeclaredMethods(argType);
                    List<Method> validMethods = new ArrayList<>(allMethods.length);
                    for (int i = 0; i < allMethods.length; i++) {
                        if (allMethods[i] == null) {
                            continue;
                        }

                        if (isEventHandler(allMethods[i])) {
                            validMethods.add(allMethods[i]);
                        }
                    }
                    Method[] methods = validMethods.toArray(new Method[validMethods.size()]);

                    EventSetDescriptor esd = new EventSetDescriptor(eventName, argType,
                                                                    methods, addMethod,
                                                                    removeMethod,
                                                                    getMethod);

                    // 如果 adder 方法抛出 TooManyListenersException，则
                    // 它是一个单播事件源。
                    if (throwsException(addMethod,
                                        java.util.TooManyListenersException.class)) {
                        esd.setUnicast(true);
                    }
                    addEvent(esd);
                }
            } // if (adds != null ...
        }
        EventSetDescriptor[] result;
        if (events.size() == 0) {
            result = EMPTY_EVENTSETDESCRIPTORS;
        } else {
            // 分配并填充结果数组。
            result = new EventSetDescriptor[events.size()];
            result = events.values().toArray(result);

            // 设置默认索引。
            if (defaultEventName != null) {
                for (int i = 0; i < result.length; i++) {
                    if (defaultEventName.equals(result[i].getName())) {
                        defaultEventIndex = i;
                    }
                }
            }
        }
        return result;
    }

    private void addEvent(EventSetDescriptor esd) {
        String key = esd.getName();
        if (esd.getName().equals("propertyChange")) {
            propertyChangeSource = true;
        }
        EventSetDescriptor old = events.get(key);
        if (old == null) {
            events.put(key, esd);
            return;
        }
        EventSetDescriptor composite = new EventSetDescriptor(old, esd);
        events.put(key, composite);
    }


                /**
     * @return 一个描述目标 bean 支持的私有方法的 MethodDescriptors 数组。
     */
    private MethodDescriptor[] getTargetMethodInfo() {
        if (methods == null) {
            methods = new HashMap<>(100);
        }

        // 检查 bean 是否有自己的 BeanInfo，这将提供显式信息。
        MethodDescriptor[] explicitMethods = null;
        if (explicitBeanInfo != null) {
            explicitMethods = explicitBeanInfo.getMethodDescriptors();
        }

        if (explicitMethods == null && superBeanInfo != null) {
            // 我们没有显式的 BeanInfo 方法。检查父类。
            MethodDescriptor supers[] = superBeanInfo.getMethodDescriptors();
            for (int i = 0 ; i < supers.length; i++) {
                addMethod(supers[i]);
            }
        }

        for (int i = 0; i < additionalBeanInfo.length; i++) {
            MethodDescriptor additional[] = additionalBeanInfo[i].getMethodDescriptors();
            if (additional != null) {
                for (int j = 0 ; j < additional.length; j++) {
                    addMethod(additional[j]);
                }
            }
        }

        if (explicitMethods != null) {
            // 将显式的 explicitBeanInfo 数据添加到结果中。
            for (int i = 0 ; i < explicitMethods.length; i++) {
                addMethod(explicitMethods[i]);
            }

        } else {

            // 对当前类应用一些反射。

            // 首先获取此级别的所有 bean 方法的数组
            Method methodList[] = getPublicDeclaredMethods(beanClass);

            // 现在分析每个方法。
            for (int i = 0; i < methodList.length; i++) {
                Method method = methodList[i];
                if (method == null) {
                    continue;
                }
                MethodDescriptor md = new MethodDescriptor(method);
                addMethod(md);
            }
        }

        // 分配并填充结果数组。
        MethodDescriptor result[] = new MethodDescriptor[methods.size()];
        result = methods.values().toArray(result);

        return result;
    }

    private void addMethod(MethodDescriptor md) {
        // 我们必须在这里区分方法的名称和参数列表。
        // 此方法被调用很多次，所以我们尽量高效。
        String name = md.getName();

        MethodDescriptor old = methods.get(name);
        if (old == null) {
            // 这是常见的情况。
            methods.put(name, md);
            return;
        }

        // 方法名称发生冲突。这是罕见的情况。

        // 检查 old 和 md 是否具有相同的类型。
        String[] p1 = md.getParamNames();
        String[] p2 = old.getParamNames();

        boolean match = false;
        if (p1.length == p2.length) {
            match = true;
            for (int i = 0; i < p1.length; i++) {
                if (p1[i] != p2[i]) {
                    match = false;
                    break;
                }
            }
        }
        if (match) {
            MethodDescriptor composite = new MethodDescriptor(old, md);
            methods.put(name, composite);
            return;
        }

        // 方法名称发生冲突，但类型签名不同。这是非常罕见的情况。

        String longKey = makeQualifiedMethodName(name, p1);
        old = methods.get(longKey);
        if (old == null) {
            methods.put(longKey, md);
            return;
        }
        MethodDescriptor composite = new MethodDescriptor(old, md);
        methods.put(longKey, composite);
    }

    /**
     * 为方法缓存创建一个键。
     */
    private static String makeQualifiedMethodName(String name, String[] params) {
        StringBuffer sb = new StringBuffer(name);
        sb.append('=');
        for (int i = 0; i < params.length; i++) {
            sb.append(':');
            sb.append(params[i]);
        }
        return sb.toString();
    }

    private int getTargetDefaultEventIndex() {
        return defaultEventIndex;
    }

    private int getTargetDefaultPropertyIndex() {
        return defaultPropertyIndex;
    }

    private BeanDescriptor getTargetBeanDescriptor() {
        // 如果可用，使用显式信息，
        if (explicitBeanInfo != null) {
            BeanDescriptor bd = explicitBeanInfo.getBeanDescriptor();
            if (bd != null) {
                return (bd);
            }
        }
        // 好的，创建一个默认的 BeanDescriptor。
        return new BeanDescriptor(this.beanClass, findCustomizerClass(this.beanClass));
    }

    private static Class<?> findCustomizerClass(Class<?> type) {
        String name = type.getName() + "Customizer";
        try {
            type = ClassFinder.findClass(name, type.getClassLoader());
            // 每个定制器都应该继承 java.awt.Component 并实现 java.beans.Customizer
            // 根据 JavaBeans™ 规范的第 9.3 节
            if (Component.class.isAssignableFrom(type) && Customizer.class.isAssignableFrom(type)) {
                return type;
            }
        }
        catch (Exception exception) {
            // 忽略任何异常
        }
        return null;
    }

    private boolean isEventHandler(Method m) {
        // 我们假设如果一个方法有一个参数，其类型继承自 java.util.Event，则该方法是事件处理程序。
        Type argTypes[] = m.getGenericParameterTypes();
        if (argTypes.length != 1) {
            return false;
        }
        return isSubclass(TypeResolver.erase(TypeResolver.resolveInClass(beanClass, argTypes[0])), EventObject.class);
    }

    /*
     * 返回类内的 *public* 方法的内部方法。
     */
    private static Method[] getPublicDeclaredMethods(Class<?> clz) {
        // 查找 Class.getDeclaredMethods 是相对昂贵的，
        // 所以我们缓存结果。
        if (!ReflectUtil.isPackageAccessible(clz)) {
            return new Method[0];
        }
        synchronized (declaredMethodCache) {
            Method[] result = declaredMethodCache.get(clz);
            if (result == null) {
                result = clz.getMethods();
                for (int i = 0; i < result.length; i++) {
                    Method method = result[i];
                    if (!method.getDeclaringClass().equals(clz)) {
                        result[i] = null; // 忽略在其他地方声明的方法
                    }
                    else {
                        try {
                            method = MethodFinder.findAccessibleMethod(method);
                            Class<?> type = method.getDeclaringClass();
                            result[i] = type.equals(clz) || type.isInterface()
                                    ? method
                                    : null; // 忽略来自父类的方法
                        }
                        catch (NoSuchMethodException exception) {
                            // 由于 6976577 被注释掉
                            // result[i] = null; // 忽略不可访问的方法
                        }
                    }
                }
                declaredMethodCache.put(clz, result);
            }
            return result;
        }
    }

    //======================================================================
    // 包私有支持方法。
    //======================================================================

    /**
     * 在给定类上查找具有给定参数列表的目标 methodName 的内部支持。
     */
    private static Method internalFindMethod(Class<?> start, String methodName,
                                                 int argCount, Class args[]) {
        // 对于重写的方法，我们需要找到最派生的版本。
        // 因此，我们从给定的类开始，沿着超类链向上遍历。

        Method method = null;

        for (Class<?> cl = start; cl != null; cl = cl.getSuperclass()) {
            Method methods[] = getPublicDeclaredMethods(cl);
            for (int i = 0; i < methods.length; i++) {
                method = methods[i];
                if (method == null) {
                    continue;
                }

                // 确保方法签名匹配。
                if (method.getName().equals(methodName)) {
                    Type[] params = method.getGenericParameterTypes();
                    if (params.length == argCount) {
                        if (args != null) {
                            boolean different = false;
                            if (argCount > 0) {
                                for (int j = 0; j < argCount; j++) {
                                    if (TypeResolver.erase(TypeResolver.resolveInClass(start, params[j])) != args[j]) {
                                        different = true;
                                        continue;
                                    }
                                }
                                if (different) {
                                    continue;
                                }
                            }
                        }
                        return method;
                    }
                }
            }
        }
        method = null;

        // 现在检查继承的接口。这在参数类本身是接口时是必要的，
        // 以及当参数类是抽象类时。
        Class ifcs[] = start.getInterfaces();
        for (int i = 0 ; i < ifcs.length; i++) {
            // 注意：原始实现中的两个方法都调用了 3 个参数的方法。这是保留的，但也许应该
            // 传递 args 数组而不是 null。
            method = internalFindMethod(ifcs[i], methodName, argCount, null);
            if (method != null) {
                break;
            }
        }
        return method;
    }

    /**
     * 在给定类上查找目标 methodName。
     */
    static Method findMethod(Class<?> cls, String methodName, int argCount) {
        return findMethod(cls, methodName, argCount, null);
    }

    /**
     * 在给定类上查找具有特定参数列表的目标 methodName。
     * <p>
     * 用于 EventSetDescriptor、PropertyDescriptor 和 IndexedPropertyDescriptor 的构造函数中。
     * <p>
     * @param cls 要检索方法的 Class 对象。
     * @param methodName 方法的名称。
     * @param argCount 所需方法的参数数量。
     * @param args 方法的参数类型数组。
     * @return 找到的方法或未找到时返回 null
     */
    static Method findMethod(Class<?> cls, String methodName, int argCount,
                             Class args[]) {
        if (methodName == null) {
            return null;
        }
        return internalFindMethod(cls, methodName, argCount, args);
    }

    /**
     * 如果类 a 等同于类 b，或者类 a 是类 b 的子类，即 a “extends” 或 “implements” b，则返回 true。
     * 注意，两个 “Class” 对象中的任何一个都可能表示接口。
     */
    static  boolean isSubclass(Class<?> a, Class<?> b) {
        // 我们依赖于对于任何给定的 Java 类或
        // 原始类型，都有一个唯一的 Class 对象，因此
        // 我们可以使用对象等价性进行比较。
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        for (Class<?> x = a; x != null; x = x.getSuperclass()) {
            if (x == b) {
                return true;
            }
            if (b.isInterface()) {
                Class<?>[] interfaces = x.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    if (isSubclass(interfaces[i], b)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 如果给定的方法抛出给定的异常，则返回 true。
     */
    private boolean throwsException(Method method, Class<?> exception) {
        Class exs[] = method.getExceptionTypes();
        for (int i = 0; i < exs.length; i++) {
            if (exs[i] == exception) {
                return true;
            }
        }
        return false;
    }

    /**
     * 尝试创建一个命名类的实例。
     * 首先尝试 “sibling” 的类加载器，然后尝试系统类加载器，然后是当前线程的类加载器。
     */
    static Object instantiate(Class<?> sibling, String className)
                 throws InstantiationException, IllegalAccessException,
                                                ClassNotFoundException {
        // 首先检查 sibling 的类加载器（如果有的话）。
        ClassLoader cl = sibling.getClassLoader();
        Class<?> cls = ClassFinder.findClass(className, cl);
        return cls.newInstance();
    }

} // end class Introspector

//===========================================================================

/**
 * 用于 Introspector 内部使用的包私有实现支持类。
 * <p>
 * 主要用于作为描述符的占位符。
 */

class GenericBeanInfo extends SimpleBeanInfo {

    private BeanDescriptor beanDescriptor;
    private EventSetDescriptor[] events;
    private int defaultEvent;
    private PropertyDescriptor[] properties;
    private int defaultProperty;
    private MethodDescriptor[] methods;
    private Reference<BeanInfo> targetBeanInfoRef;

    public GenericBeanInfo(BeanDescriptor beanDescriptor,
                EventSetDescriptor[] events, int defaultEvent,
                PropertyDescriptor[] properties, int defaultProperty,
                MethodDescriptor[] methods, BeanInfo targetBeanInfo) {
        this.beanDescriptor = beanDescriptor;
        this.events = events;
        this.defaultEvent = defaultEvent;
        this.properties = properties;
        this.defaultProperty = defaultProperty;
        this.methods = methods;
        this.targetBeanInfoRef = (targetBeanInfo != null)
                ? new SoftReference<>(targetBeanInfo)
                : null;
    }

    /**
     * 包私有的复制构造函数
     * 必须使新对象与旧对象的任何更改隔离。
     */
    GenericBeanInfo(GenericBeanInfo old) {

        beanDescriptor = new BeanDescriptor(old.beanDescriptor);
        if (old.events != null) {
            int len = old.events.length;
            events = new EventSetDescriptor[len];
            for (int i = 0; i < len; i++) {
                events[i] = new EventSetDescriptor(old.events[i]);
            }
        }
        defaultEvent = old.defaultEvent;
        if (old.properties != null) {
            int len = old.properties.length;
            properties = new PropertyDescriptor[len];
            for (int i = 0; i < len; i++) {
                PropertyDescriptor oldp = old.properties[i];
                if (oldp instanceof IndexedPropertyDescriptor) {
                    properties[i] = new IndexedPropertyDescriptor(
                                        (IndexedPropertyDescriptor) oldp);
                } else {
                    properties[i] = new PropertyDescriptor(oldp);
                }
            }
        }
        defaultProperty = old.defaultProperty;
        if (old.methods != null) {
            int len = old.methods.length;
            methods = new MethodDescriptor[len];
            for (int i = 0; i < len; i++) {
                methods[i] = new MethodDescriptor(old.methods[i]);
            }
        }
        this.targetBeanInfoRef = old.targetBeanInfoRef;
    }


                public PropertyDescriptor[] getPropertyDescriptors() {
        return properties;
    }

    public int getDefaultPropertyIndex() {
        return defaultProperty;
    }

    public EventSetDescriptor[] getEventSetDescriptors() {
        return events;
    }

    public int getDefaultEventIndex() {
        return defaultEvent;
    }

    public MethodDescriptor[] getMethodDescriptors() {
        return methods;
    }

    public BeanDescriptor getBeanDescriptor() {
        return beanDescriptor;
    }

    public java.awt.Image getIcon(int iconKind) {
        BeanInfo targetBeanInfo = getTargetBeanInfo();
        if (targetBeanInfo != null) {
            return targetBeanInfo.getIcon(iconKind);
        }
        return super.getIcon(iconKind);
    }

    private BeanInfo getTargetBeanInfo() {
        if (this.targetBeanInfoRef == null) {
            return null;
        }
        BeanInfo targetBeanInfo = this.targetBeanInfoRef.get();
        if (targetBeanInfo == null) {
            targetBeanInfo = ThreadGroupContext.getContext().getBeanInfoFinder()
                    .find(this.beanDescriptor.getBeanClass());
            if (targetBeanInfo != null) {
                this.targetBeanInfoRef = new SoftReference<>(targetBeanInfo);
            }
        }
        return targetBeanInfo;
    }
}
