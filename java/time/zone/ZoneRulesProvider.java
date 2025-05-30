
/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Copyright (c) 2009-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package java.time.zone;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 系统的时间区规则提供者。
 * <p>
 * 该类管理时间区规则的配置。
 * 静态方法提供了可以用于管理提供者的公共API。
 * 抽象方法提供了允许提供规则的服务提供者接口。
 * <p>
 * ZoneRulesProvider 可以作为扩展类安装在 Java 平台的实例中，即放置在通常的扩展目录中的 jar 文件。
 * 安装的提供者使用 {@link ServiceLoader} 类定义的服务提供者加载设施加载。
 * ZoneRulesProvider 通过在资源目录 {@code META-INF/services} 中的提供者配置文件
 * {@code java.time.zone.ZoneRulesProvider} 来标识自己。该文件应包含一行指定完全限定的具体 zonerules-provider 类名。
 * 提供者也可以通过将其添加到类路径或通过 {@link #registerProvider} 方法注册自己来提供。
 * <p>
 * Java 虚拟机有一个默认提供者，提供 IANA Time Zone Database (TZDB) 定义的时间区的规则。
 * 如果系统属性 {@code java.time.zone.DefaultZoneRulesProvider} 已定义，则其值被视为要加载为默认提供者的具体 ZoneRulesProvider 类的完全限定名，使用系统类加载器加载。
 * 如果此系统属性未定义，则将加载系统默认提供者以作为默认提供者。
 * <p>
 * 规则主要通过区 ID 查找，如 {@link ZoneId} 所使用。
 * 只能使用区区域 ID，区偏移 ID 在这里不使用。
 * <p>
 * 时间区规则是政治性的，因此数据可以随时更改。
 * 每个提供者将为每个区 ID 提供最新的规则，但它们也可能提供规则如何更改的历史记录。
 *
 * @implSpec
 * 该接口是一个服务提供者，可以被多个线程调用。
 * 实现必须是不可变的和线程安全的。
 * <p>
 * 提供者必须确保一旦规则被应用程序看到，该规则必须继续可用。
 * <p>
 * 建议提供者实现一个有意义的 {@code toString} 方法。
 * <p>
 * 许多系统希望在不停止 JVM 的情况下动态更新时间区规则。
 * 详细检查后，这是一个复杂的问题。
 * 提供者可以选择处理动态更新，但默认提供者不处理。
 *
 * @since 1.8
 */
public abstract class ZoneRulesProvider {

    /**
     * 已加载的提供者集合。
     */
    private static final CopyOnWriteArrayList<ZoneRulesProvider> PROVIDERS = new CopyOnWriteArrayList<>();
    /**
     * 从区 ID 到提供者的查找。
     */
    private static final ConcurrentMap<String, ZoneRulesProvider> ZONES = new ConcurrentHashMap<>(512, 0.75f, 2);

    static {
        // 如果属性 java.time.zone.DefaultZoneRulesProvider 已设置，则其值是默认提供者的类名
        final List<ZoneRulesProvider> loaded = new ArrayList<>();
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                String prop = System.getProperty("java.time.zone.DefaultZoneRulesProvider");
                if (prop != null) {
                    try {
                        Class<?> c = Class.forName(prop, true, ClassLoader.getSystemClassLoader());
                        ZoneRulesProvider provider = ZoneRulesProvider.class.cast(c.newInstance());
                        registerProvider(provider);
                        loaded.add(provider);
                    } catch (Exception x) {
                        throw new Error(x);
                    }
                } else {
                    registerProvider(new TzdbZoneRulesProvider());
                }
                return null;
            }
        });

        ServiceLoader<ZoneRulesProvider> sl = ServiceLoader.load(ZoneRulesProvider.class, ClassLoader.getSystemClassLoader());
        Iterator<ZoneRulesProvider> it = sl.iterator();
        while (it.hasNext()) {
            ZoneRulesProvider provider;
            try {
                provider = it.next();
            } catch (ServiceConfigurationError ex) {
                if (ex.getCause() instanceof SecurityException) {
                    continue;  // 忽略安全异常，尝试下一个提供者
                }
                throw ex;
            }
            boolean found = false;
            for (ZoneRulesProvider p : loaded) {
                if (p.getClass() == provider.getClass()) {
                    found = true;
                }
            }
            if (!found) {
                registerProvider0(provider);
                loaded.add(provider);
            }
        }
        // 如果提供者很多且每个都单独添加，CopyOnWriteList 可能会很慢
        PROVIDERS.addAll(loaded);
    }

    //-------------------------------------------------------------------------
    /**
     * 获取可用的区 ID 集合。
     * <p>
     * 这些 ID 是 {@link ZoneId} 的字符串形式。
     *
     * @return 区 ID 集合的可修改副本，不为空
     */
    public static Set<String> getAvailableZoneIds() {
        return new HashSet<>(ZONES.keySet());
    }

    /**
     * 获取区 ID 的规则。
     * <p>
     * 这返回区 ID 的最新可用规则。
     * <p>
     * 该方法依赖于配置的时间区数据提供者文件。
     * 这些文件使用 {@code ServiceLoader} 加载。
     * <p>
     * 缓存标志旨在允许提供者实现防止规则在 {@code ZoneId} 中缓存。
     * 在正常情况下，缓存区规则是非常可取的，因为它将提供更高的性能。然而，有一种使用情况是
     * 缓存是不可取的，参见 {@link #provideRules}。
     *
     * @param zoneId 由 {@code ZoneId} 定义的区 ID，不为空
     * @param forCaching 是否查询规则用于缓存，
     * true 表示返回的规则将被 {@code ZoneId} 缓存，
     * false 表示规则将返回给用户而不会被 {@code ZoneId} 缓存
     * @return 规则，如果 {@code forCaching} 为 true 且这是动态提供者，希望防止在 {@code ZoneId} 中缓存，则为 null，否则不为空
     * @throws ZoneRulesException 如果无法为区 ID 获取规则
     */
    public static ZoneRules getRules(String zoneId, boolean forCaching) {
        Objects.requireNonNull(zoneId, "zoneId");
        return getProvider(zoneId).provideRules(zoneId, forCaching);
    }

    /**
     * 获取区 ID 的规则历史。
     * <p>
     * 时间区由政府定义，经常发生变化。
     * 该方法允许应用程序找到单个区 ID 的规则更改历史。映射的键是一个字符串，该字符串是与规则关联的版本字符串。
     * <p>
     * 版本的确切含义和格式是提供者特定的。
     * 版本必须遵循字典顺序，因此返回的映射将从已知的最旧规则排序到最新的可用规则。
     * 默认的 'TZDB' 组使用由年份和字母组成的版本编号，例如 '2009e' 或 '2012f'。
     * <p>
     * 实现必须为每个有效的区 ID 提供结果，但不需要提供规则的历史。
     * 因此，映射将始终包含一个元素，如果有关于历史规则的信息，则将包含多个元素。
     *
     * @param zoneId 由 {@code ZoneId} 定义的区 ID，不为空
     * @return 区 ID 的规则历史的可修改副本，按从旧到新的顺序排序，不为空
     * @throws ZoneRulesException 如果无法为区 ID 获取历史
     */
    public static NavigableMap<String, ZoneRules> getVersions(String zoneId) {
        Objects.requireNonNull(zoneId, "zoneId");
        return getProvider(zoneId).provideVersions(zoneId);
    }

    /**
     * 获取区 ID 的提供者。
     *
     * @param zoneId 由 {@code ZoneId} 定义的区 ID，不为空
     * @return 提供者，不为空
     * @throws ZoneRulesException 如果区 ID 未知
     */
    private static ZoneRulesProvider getProvider(String zoneId) {
        ZoneRulesProvider provider = ZONES.get(zoneId);
        if (provider == null) {
            if (ZONES.isEmpty()) {
                throw new ZoneRulesException("没有注册时间区数据文件");
            }
            throw new ZoneRulesException("未知的时间区 ID: " + zoneId);
        }
        return provider;
    }

    //-------------------------------------------------------------------------
    /**
     * 注册一个区规则提供者。
     * <p>
     * 这将添加一个新的提供者到当前可用的提供者中。
     * 提供者为一个或多个区 ID 提供规则。
     * 如果提供者提供的区 ID 已经注册，则无法注册该提供者。参见 {@link ZoneId} 中关于时间区 ID 的说明，特别是
     * 使用“组”概念使 ID 唯一的部分。
     * <p>
     * 为了确保已创建的时间区的完整性，没有方法可以注销提供者。
     *
     * @param provider 要注册的提供者，不为空
     * @throws ZoneRulesException 如果区 ID 已经注册
     */
    public static void registerProvider(ZoneRulesProvider provider) {
        Objects.requireNonNull(provider, "provider");
        registerProvider0(provider);
        PROVIDERS.add(provider);
    }

    /**
     * 注册提供者。
     *
     * @param provider 要注册的提供者，不为空
     * @throws ZoneRulesException 如果无法完成注册
     */
    private static void registerProvider0(ZoneRulesProvider provider) {
        for (String zoneId : provider.provideZoneIds()) {
            Objects.requireNonNull(zoneId, "zoneId");
            ZoneRulesProvider old = ZONES.putIfAbsent(zoneId, provider);
            if (old != null) {
                throw new ZoneRulesException(
                    "无法注册区，因为已有一个注册了相同 ID 的区: " + zoneId +
                    ", 当前正在从提供者加载: " + provider);
            }
        }
    }

    /**
     * 从底层数据提供者刷新规则。
     * <p>
     * 该方法允许应用程序请求提供者检查提供的规则是否有任何更新。
     * 调用此方法后，任何 {@link ZonedDateTime} 中存储的偏移可能对区 ID 无效。
     * <p>
     * 动态更新规则是一个复杂的问题，大多数应用程序不应使用此方法或动态规则。
     * 要实现动态规则，必须编写一个提供者实现，符合该类的规范。
     * 此外，应用程序中不应缓存 {@code ZoneRules} 的实例，因为它们将变得陈旧。然而，{@link #provideRules(String, boolean)} 的布尔标志允许提供者实现
     * 控制 {@code ZoneId} 的缓存，确保系统中的所有对象都能看到新规则。
     * 注意，动态规则提供者可能会有性能成本。注意，本规范中没有动态规则提供者。
     *
     * @return 如果规则已更新，则返回 true
     * @throws ZoneRulesException 如果刷新过程中发生错误
     */
    public static boolean refresh() {
        boolean changed = false;
        for (ZoneRulesProvider provider : PROVIDERS) {
            changed |= provider.provideRefresh();
        }
        return changed;
    }


                /**
     * 构造函数。
     */
    protected ZoneRulesProvider() {
    }

    //-----------------------------------------------------------------------
    /**
     * SPI 方法，用于获取可用的时区 ID。
     * <p>
     * 此方法获取此 {@code ZoneRulesProvider} 提供的时区 ID。
     * 提供者应至少提供一个时区 ID 的数据。
     * <p>
     * 返回的时区 ID 在应用程序的生命周期内保持可用和有效。
     * 动态提供者可能会随着更多数据的可用而增加 ID 的集合。
     *
     * @return 提供的时区 ID 集合，不为空
     * @throws ZoneRulesException 如果在提供 ID 时出现问题
     */
    protected abstract Set<String> provideZoneIds();

    /**
     * SPI 方法，用于获取指定时区 ID 的规则。
     * <p>
     * 此方法加载指定时区 ID 的规则。
     * 提供者实现必须验证时区 ID 是否有效和可用，如果无效则抛出 {@code ZoneRulesException}。
     * 有效情况下的方法结果取决于缓存标志。
     * <p>
     * 如果提供者实现不是动态的，那么该方法的结果必须是 ID 选择的非空规则集。
     * <p>
     * 如果提供者实现是动态的，那么该标志提供了防止返回的规则在 {@link ZoneId} 中缓存的选项。
     * 当标志为 true 时，提供者可以返回 null，其中 null 将防止规则在 {@code ZoneId} 中缓存。
     * 当标志为 false 时，提供者必须返回非空规则。
     *
     * @param zoneId 由 {@code ZoneId} 定义的时区 ID，不为空
     * @param forCaching 是否查询规则以进行缓存，
     * true 表示返回的规则将由 {@code ZoneId} 缓存，
     * false 表示它们将返回给用户而不会在 {@code ZoneId} 中缓存
     * @return 规则，如果 {@code forCaching} 为 true 且这是希望防止在 {@code ZoneId} 中缓存的动态提供者，则返回 null，
     * 否则不为空
     * @throws ZoneRulesException 如果无法为时区 ID 获取规则
     */
    protected abstract ZoneRules provideRules(String zoneId, boolean forCaching);

    /**
     * SPI 方法，用于获取指定时区 ID 的规则历史。
     * <p>
     * 此方法返回一个以版本字符串为键的历史规则映射。
     * 版本的确切含义和格式由提供者特定。
     * 版本必须遵循字典顺序，因此返回的映射将从最旧的已知规则到最新的可用规则排序。
     * 默认的 'TZDB' 组使用由年份和字母组成的版本编号，例如 '2009e' 或 '2012f'。
     * <p>
     * 实现必须为每个有效的时区 ID 提供结果，但不必提供规则历史。
     * 因此，映射将至少包含一个元素，只有在有历史规则信息可用时才会包含多个元素。
     * <p>
     * 返回的版本在应用程序的生命周期内保持可用和有效。
     * 动态提供者可能会随着更多数据的可用而增加版本集。
     *
     * @param zoneId 由 {@code ZoneId} 定义的时区 ID，不为空
     * @return 按从旧到新排序的 ID 的规则历史的可修改副本，不为空
     * @throws ZoneRulesException 如果无法为时区 ID 获取历史
     */
    protected abstract NavigableMap<String, ZoneRules> provideVersions(String zoneId);

    /**
     * SPI 方法，用于从底层数据提供者刷新规则。
     * <p>
     * 此方法为提供者提供了动态重新检查底层数据提供者以找到最新规则的机会。
     * 这可以用于在不关闭 JVM 的情况下加载新规则。
     * 动态行为完全是可选的，大多数提供者不支持它。
     * <p>
     * 此实现返回 false。
     *
     * @return 如果规则已更新，则返回 true
     * @throws ZoneRulesException 如果在刷新过程中发生错误
     */
    protected boolean provideRefresh() {
        return false;
    }

}
