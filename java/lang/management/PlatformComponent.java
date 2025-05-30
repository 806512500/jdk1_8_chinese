
/*
 * Copyright (c) 2008, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.UnixOperatingSystemMXBean;

import sun.management.ManagementFactoryHelper;
import sun.management.Util;

/**
 * 此枚举类定义了提供监视和管理支持的平台组件列表。
 * 每个枚举代表一个 MXBean 接口。一个 MXBean 实例可以实现一个或多个 MXBean 接口。
 *
 * 例如，com.sun.management.GarbageCollectorMXBean
 * 扩展了 java.lang.management.GarbageCollectorMXBean
 * 并且有一组垃圾收集 MXBean 实例，每个实例都实现了 c.s.m. 和 j.l.m. 接口。
 * 有两个单独的枚举 GARBAGE_COLLECTOR 和 SUN_GARBAGE_COLLECTOR，以便 ManagementFactory.getPlatformMXBeans(Class)
 * 将返回指定类型的 MXBean 列表。
 *
 * 要为 Java 平台添加新的 MXBean 接口，
 * 请添加一个新的枚举常量并实现 MXBeanFetcher。
 */
enum PlatformComponent {

    /**
     * Java 虚拟机的类加载系统。
     */
    CLASS_LOADING(
        "java.lang.management.ClassLoadingMXBean",
        "java.lang", "ClassLoading", defaultKeyProperties(),
        true, // 单例
        new MXBeanFetcher<ClassLoadingMXBean>() {
            public List<ClassLoadingMXBean> getMXBeans() {
                return Collections.singletonList(ManagementFactoryHelper.getClassLoadingMXBean());
            }
        }),

    /**
     * Java 虚拟机的编译系统。
     */
    COMPILATION(
        "java.lang.management.CompilationMXBean",
        "java.lang", "Compilation", defaultKeyProperties(),
        true, // 单例
        new MXBeanFetcher<CompilationMXBean>() {
            public List<CompilationMXBean> getMXBeans() {
                CompilationMXBean m = ManagementFactoryHelper.getCompilationMXBean();
                if (m == null) {
                   return Collections.emptyList();
                } else {
                   return Collections.singletonList(m);
                }
            }
        }),

    /**
     * Java 虚拟机的内存系统。
     */
    MEMORY(
        "java.lang.management.MemoryMXBean",
        "java.lang", "Memory", defaultKeyProperties(),
        true, // 单例
        new MXBeanFetcher<MemoryMXBean>() {
            public List<MemoryMXBean> getMXBeans() {
                return Collections.singletonList(ManagementFactoryHelper.getMemoryMXBean());
            }
        }),

    /**
     * Java 虚拟机中的垃圾收集器。
     */
    GARBAGE_COLLECTOR(
        "java.lang.management.GarbageCollectorMXBean",
        "java.lang", "GarbageCollector", keyProperties("name"),
        false, // 零个或多个实例
        new MXBeanFetcher<GarbageCollectorMXBean>() {
            public List<GarbageCollectorMXBean> getMXBeans() {
                return ManagementFactoryHelper.getGarbageCollectorMXBeans();
            }
        }),

    /**
     * Java 虚拟机中的内存管理器。
     */
    MEMORY_MANAGER(
        "java.lang.management.MemoryManagerMXBean",
        "java.lang", "MemoryManager", keyProperties("name"),
        false, // 零个或多个实例
        new MXBeanFetcher<MemoryManagerMXBean>() {
            public List<MemoryManagerMXBean> getMXBeans() {
                return ManagementFactoryHelper.getMemoryManagerMXBeans();
            }
        },
        GARBAGE_COLLECTOR),

    /**
     * Java 虚拟机中的内存池。
     */
    MEMORY_POOL(
        "java.lang.management.MemoryPoolMXBean",
        "java.lang", "MemoryPool", keyProperties("name"),
        false, // 零个或多个实例
        new MXBeanFetcher<MemoryPoolMXBean>() {
            public List<MemoryPoolMXBean> getMXBeans() {
                return ManagementFactoryHelper.getMemoryPoolMXBeans();
            }
        }),

    /**
     * Java 虚拟机运行的操作系统。
     */
    OPERATING_SYSTEM(
        "java.lang.management.OperatingSystemMXBean",
        "java.lang", "OperatingSystem", defaultKeyProperties(),
        true, // 单例
        new MXBeanFetcher<OperatingSystemMXBean>() {
            public List<OperatingSystemMXBean> getMXBeans() {
                return Collections.singletonList(ManagementFactoryHelper.getOperatingSystemMXBean());
            }
        }),

    /**
     * Java 虚拟机的运行时系统。
     */
    RUNTIME(
        "java.lang.management.RuntimeMXBean",
        "java.lang", "Runtime", defaultKeyProperties(),
        true, // 单例
        new MXBeanFetcher<RuntimeMXBean>() {
            public List<RuntimeMXBean> getMXBeans() {
                return Collections.singletonList(ManagementFactoryHelper.getRuntimeMXBean());
            }
        }),

    /**
     * Java 虚拟机的线程系统。
     */
    THREADING(
        "java.lang.management.ThreadMXBean",
        "java.lang", "Threading", defaultKeyProperties(),
        true, // 单例
        new MXBeanFetcher<ThreadMXBean>() {
            public List<ThreadMXBean> getMXBeans() {
                return Collections.singletonList(ManagementFactoryHelper.getThreadMXBean());
            }
        }),

    /**
     * 日志记录设施。
     */
    LOGGING(
        "java.lang.management.PlatformLoggingMXBean",
        "java.util.logging", "Logging", defaultKeyProperties(),
        true, // 单例
        new MXBeanFetcher<PlatformLoggingMXBean>() {
            public List<PlatformLoggingMXBean> getMXBeans() {
                PlatformLoggingMXBean m = ManagementFactoryHelper.getPlatformLoggingMXBean();
                if (m == null) {
                   return Collections.emptyList();
                } else {
                   return Collections.singletonList(m);
                }
            }
        }),

    /**
     * 缓冲池。
     */
    BUFFER_POOL(
        "java.lang.management.BufferPoolMXBean",
        "java.nio", "BufferPool", keyProperties("name"),
        false, // 零个或多个实例
        new MXBeanFetcher<BufferPoolMXBean>() {
            public List<BufferPoolMXBean> getMXBeans() {
                return ManagementFactoryHelper.getBufferPoolMXBeans();
            }
        }),

    // Sun 平台扩展

    /**
     * Sun 扩展垃圾收集器，以周期方式执行收集。
     */
    SUN_GARBAGE_COLLECTOR(
        "com.sun.management.GarbageCollectorMXBean",
        "java.lang", "GarbageCollector", keyProperties("name"),
        false, // 零个或多个实例
        new MXBeanFetcher<com.sun.management.GarbageCollectorMXBean>() {
            public List<com.sun.management.GarbageCollectorMXBean> getMXBeans() {
                return getGcMXBeanList(com.sun.management.GarbageCollectorMXBean.class);
            }
        }),

    /**
     * Java 虚拟机运行的 Sun 扩展操作系统。
     */
    SUN_OPERATING_SYSTEM(
        "com.sun.management.OperatingSystemMXBean",
        "java.lang", "OperatingSystem", defaultKeyProperties(),
        true, // 单例
        new MXBeanFetcher<com.sun.management.OperatingSystemMXBean>() {
            public List<com.sun.management.OperatingSystemMXBean> getMXBeans() {
                return getOSMXBeanList(com.sun.management.OperatingSystemMXBean.class);
            }
        }),

    /**
     * Unix 操作系统。
     */
    SUN_UNIX_OPERATING_SYSTEM(
        "com.sun.management.UnixOperatingSystemMXBean",
        "java.lang", "OperatingSystem", defaultKeyProperties(),
        true, // 单例
        new MXBeanFetcher<UnixOperatingSystemMXBean>() {
            public List<UnixOperatingSystemMXBean> getMXBeans() {
                return getOSMXBeanList(com.sun.management.UnixOperatingSystemMXBean.class);
            }
        }),

    /**
     * HotSpot 虚拟机的诊断支持。
     */
    HOTSPOT_DIAGNOSTIC(
        "com.sun.management.HotSpotDiagnosticMXBean",
        "com.sun.management", "HotSpotDiagnostic", defaultKeyProperties(),
        true, // 单例
        new MXBeanFetcher<HotSpotDiagnosticMXBean>() {
            public List<HotSpotDiagnosticMXBean> getMXBeans() {
                return Collections.singletonList(ManagementFactoryHelper.getDiagnosticMXBean());
            }
        });


    /**
     * 返回组件的 MXBeans 的任务。
     */
    interface MXBeanFetcher<T extends PlatformManagedObject> {
        public List<T> getMXBeans();
    }

    /*
     * 返回给定类型的 GC MXBeans 列表。
     */
    private static <T extends GarbageCollectorMXBean>
            List<T> getGcMXBeanList(Class<T> gcMXBeanIntf) {
        List<GarbageCollectorMXBean> list =
            ManagementFactoryHelper.getGarbageCollectorMXBeans();
        List<T> result = new ArrayList<>(list.size());
        for (GarbageCollectorMXBean m : list) {
            if (gcMXBeanIntf.isInstance(m)) {
                result.add(gcMXBeanIntf.cast(m));
            }
        }
        return result;
    }

    /*
     * 返回给定类型的 OS mxbean 实例。
     */
    private static <T extends OperatingSystemMXBean>
            List<T> getOSMXBeanList(Class<T> osMXBeanIntf) {
        OperatingSystemMXBean m =
            ManagementFactoryHelper.getOperatingSystemMXBean();
        if (osMXBeanIntf.isInstance(m)) {
            return Collections.singletonList(osMXBeanIntf.cast(m));
        } else {
            return Collections.emptyList();
        }
    }

    private final String mxbeanInterfaceName;
    private final String domain;
    private final String type;
    private final Set<String> keyProperties;
    private final MXBeanFetcher<?> fetcher;
    private final PlatformComponent[] subComponents;
    private final boolean singleton;

    private PlatformComponent(String intfName,
                              String domain, String type,
                              Set<String> keyProperties,
                              boolean singleton,
                              MXBeanFetcher<?> fetcher,
                              PlatformComponent... subComponents) {
        this.mxbeanInterfaceName = intfName;
        this.domain = domain;
        this.type = type;
        this.keyProperties = keyProperties;
        this.singleton = singleton;
        this.fetcher = fetcher;
        this.subComponents = subComponents;
    }

    private static Set<String> defaultKeyProps;
    private static Set<String> defaultKeyProperties() {
        if (defaultKeyProps == null) {
            defaultKeyProps = Collections.singleton("type");
        }
        return defaultKeyProps;
    }

    private static Set<String> keyProperties(String... keyNames) {
        Set<String> set = new HashSet<>();
        set.add("type");
        for (String s : keyNames) {
            set.add(s);
        }
        return set;
    }

    boolean isSingleton() {
        return singleton;
    }

    String getMXBeanInterfaceName() {
        return mxbeanInterfaceName;
    }

    @SuppressWarnings("unchecked")
    Class<? extends PlatformManagedObject> getMXBeanInterface() {
        try {
            // 懒加载 MXBean 接口，仅在需要时加载
            return (Class<? extends PlatformManagedObject>)
                       Class.forName(mxbeanInterfaceName, false,
                                     PlatformManagedObject.class.getClassLoader());
        } catch (ClassNotFoundException x) {
            throw new AssertionError(x);
        }
    }

    @SuppressWarnings("unchecked")
    <T extends PlatformManagedObject>
        List<T> getMXBeans(Class<T> mxbeanInterface)
    {
        return (List<T>) fetcher.getMXBeans();
    }

    <T extends PlatformManagedObject> T getSingletonMXBean(Class<T> mxbeanInterface)
    {
        if (!singleton)
            throw new IllegalArgumentException(mxbeanInterfaceName +
                " 可以有零个或多个实例");

        List<T> list = getMXBeans(mxbeanInterface);
        assert list.size() == 1;
        return list.isEmpty() ? null : list.get(0);
    }

    <T extends PlatformManagedObject>
            T getSingletonMXBean(MBeanServerConnection mbs, Class<T> mxbeanInterface)
        throws java.io.IOException
    {
        if (!singleton)
            throw new IllegalArgumentException(mxbeanInterfaceName +
                " 可以有零个或多个实例");

        // 单例 MXBean 的 ObjectName 只包含域和类型
        assert keyProperties.size() == 1;
        String on = domain + ":type=" + type;
        return ManagementFactory.newPlatformMXBeanProxy(mbs,
                                                        on,
                                                        mxbeanInterface);
    }

    <T extends PlatformManagedObject>
            List<T> getMXBeans(MBeanServerConnection mbs, Class<T> mxbeanInterface)
        throws java.io.IOException
    {
        List<T> result = new ArrayList<>();
        for (ObjectName on : getObjectNames(mbs)) {
            result.add(ManagementFactory.
                newPlatformMXBeanProxy(mbs,
                                       on.getCanonicalName(),
                                       mxbeanInterface)
            );
        }
        return result;
    }

    private Set<ObjectName> getObjectNames(MBeanServerConnection mbs)
        throws java.io.IOException
    {
        String domainAndType = domain + ":type=" + type;
        if (keyProperties.size() > 1) {
            // 如果有超过一个键属性（即除了 "type" 之外）
            domainAndType += ",*";
        }
        ObjectName on = Util.newObjectName(domainAndType);
        Set<ObjectName> set =  mbs.queryNames(on, null);
        for (PlatformComponent pc : subComponents) {
            set.addAll(pc.getObjectNames(mbs));
        }
        return set;
    }

}


                // 从 MXBean 接口名称到 PlatformComponent 的映射
    private static Map<String, PlatformComponent> enumMap;
    private static synchronized void ensureInitialized() {
        if (enumMap == null) {
            enumMap = new HashMap<>();
            for (PlatformComponent pc: PlatformComponent.values()) {
                // 使用字符串作为键而不是 Class<?> 以避免不必要的管理接口类加载
                enumMap.put(pc.getMXBeanInterfaceName(), pc);
            }
        }
    }

    static boolean isPlatformMXBean(String cn) {
        ensureInitialized();
        return enumMap.containsKey(cn);
    }

    static <T extends PlatformManagedObject>
        PlatformComponent getPlatformComponent(Class<T> mxbeanInterface)
    {
        ensureInitialized();
        String cn = mxbeanInterface.getName();
        PlatformComponent pc = enumMap.get(cn);
        if (pc != null && pc.getMXBeanInterface() == mxbeanInterface)
            return pc;
        return null;
    }

    private static final long serialVersionUID = 6992337162326171013L;
}
