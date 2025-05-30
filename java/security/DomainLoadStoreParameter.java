/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security;

import java.net.URI;
import java.util.*;
import static java.security.KeyStore.*;

/**
 * 配置数据，用于指定密钥库域中的密钥库。
 * 密钥库域是一组密钥库，这些密钥库被呈现为一个单一的逻辑密钥库。配置数据在
 * {@code KeyStore}
 * {@link KeyStore#load(KeyStore.LoadStoreParameter) load} 和
 * {@link KeyStore#store(KeyStore.LoadStoreParameter) store} 操作期间使用。
 * <p>
 * 配置数据支持以下语法：
 * <pre>{@code
 *     domain <domainName> [<property> ...] {
 *         keystore <keystoreName> [<property> ...] ;
 *         ...
 *     };
 *     ...
 * }</pre>
 * 其中 {@code domainName} 和 {@code keystoreName} 是标识符，而 {@code property} 是键值对。键和值之间用等号分隔，值用双引号括起来。属性值可以是可打印字符串或由冒号分隔的十六进制数字对组成的二进制字符串。多值属性表示为用逗号分隔的值列表，用方括号括起来。
 * 参见 {@link Arrays#toString(java.lang.Object[])}。
 * <p>
 * 为了确保密钥库条目是唯一标识的，每个条目的别名都会以前缀 {@code keystoreName} 和条目名称分隔符开头，每个 {@code keystoreName} 在其域中必须是唯一的。在存储密钥库时，条目名称前缀会被省略。
 * <p>
 * 属性是上下文敏感的：适用于域中所有密钥库的属性位于域子句中，仅适用于特定密钥库的属性位于该密钥库的子句中。
 * 除非另有说明，密钥库子句中的属性会覆盖域子句中同名的属性。所有属性名称不区分大小写。支持以下属性：
 * <dl>
 * <dt> {@code keystoreType="<type>"} </dt>
 *     <dd> 密钥库类型。 </dd>
 * <dt> {@code keystoreURI="<url>"} </dt>
 *     <dd> 密钥库位置。 </dd>
 * <dt> {@code keystoreProviderName="<name>"} </dt>
 *     <dd> 密钥库的 JCE 提供程序名称。 </dd>
 * <dt> {@code keystorePasswordEnv="<environment-variable>"} </dt>
 *     <dd> 存储密钥库密码的环境变量。
 *          或者，密码可以通过构造方法在 {@code Map<String, ProtectionParameter>} 中提供。 </dd>
 * <dt> {@code entryNameSeparator="<separator>"} </dt>
 *     <dd> 密钥库名称前缀和条目名称之间的分隔符。
 *          指定时，它适用于域中的所有条目。
 *          默认值是一个空格。 </dd>
 * </dl>
 * <p>
 * 例如，一个包含三个密钥库的简单密钥库域的配置数据如下所示：
 * <pre>
 *
 * domain app1 {
 *     keystore app1-truststore
 *         keystoreURI="file:///app1/etc/truststore.jks";
 *
 *     keystore system-truststore
 *         keystoreURI="${java.home}/lib/security/cacerts";
 *
 *     keystore app1-keystore
 *         keystoreType="PKCS12"
 *         keystoreURI="file:///app1/etc/keystore.p12";
 * };
 *
 * </pre>
 * @since 1.8
 */
public final class DomainLoadStoreParameter implements LoadStoreParameter {

    private final URI configuration;
    private final Map<String,ProtectionParameter> protectionParams;

    /**
     * 构造一个用于密钥库域的 DomainLoadStoreParameter，包含用于保护密钥库数据的参数。
     *
     * @param configuration 域配置数据的标识符。
     *     当需要在同一位置区分多个域配置时，目标域的名称应在 {@code java.net.URI} 的片段组件中指定。
     *
     * @param protectionParams 从密钥库名称到用于保护密钥库数据的参数的映射。
     *     如果不需要保护参数或已在域配置数据的属性中指定，则应使用 {@code java.util.Collections.EMPTY_MAP}。
     *     它会被克隆以防止后续修改。
     *
     * @exception NullPointerException 如果 {@code configuration} 或
     *     {@code protectionParams} 为 {@code null}
     */
    public DomainLoadStoreParameter(URI configuration,
        Map<String,ProtectionParameter> protectionParams) {
        if (configuration == null || protectionParams == null) {
            throw new NullPointerException("invalid null input");
        }
        this.configuration = configuration;
        this.protectionParams =
            Collections.unmodifiableMap(new HashMap<>(protectionParams));
    }

    /**
     * 获取域配置数据的标识符。
     *
     * @return 配置数据的标识符
     */
    public URI getConfiguration() {
        return configuration;
    }

    /**
     * 获取此域中密钥库的保护参数。
     *
     * @return 一个不可修改的密钥库名称到保护参数的映射
     */
    public Map<String,ProtectionParameter> getProtectionParams() {
        return protectionParams;
    }

    /**
     * 获取此域的密钥库保护参数。
     * 密钥库域不支持保护参数。
     *
     * @return 始终返回 {@code null}
     */
    @Override
    public KeyStore.ProtectionParameter getProtectionParameter() {
        return null;
    }
}
