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

package java.security;

import java.security.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * 该类用于安全权限。
 * SecurityPermission 包含一个名称（也称为“目标名称”），但没有动作列表；你要么拥有命名的权限，要么没有。
 * <P>
 * 目标名称是安全配置参数的名称（见下文）。
 * 当前，SecurityPermission 对象用于保护对 Policy、Security、Provider、Signer 和 Identity
 * 对象的访问。
 * <P>
 * 下表列出了所有可能的 SecurityPermission 目标名称，并为每个名称提供了一个描述，说明该权限允许什么操作
 * 以及授予代码该权限的风险。
 *
 * <table border=1 cellpadding=5 summary="目标名称，该权限允许的操作，以及允许该权限的风险">
 * <tr>
 * <th>权限目标名称</th>
 * <th>该权限允许的操作</th>
 * <th>允许该权限的风险</th>
 * </tr>
 *
 * <tr>
 *   <td>createAccessControlContext</td>
 *   <td>创建 AccessControlContext</td>
 *   <td>这允许某人使用 {@code DomainCombiner} 实例化 AccessControlContext。
 * 极度小心必须在授予此权限时采取。恶意代码可以创建一个 DomainCombiner，该 DomainCombiner 增加了授予代码的权限集，
 * 甚至授予代码 {@link java.security.AllPermission}。</td>
 * </tr>
 *
 * <tr>
 *   <td>getDomainCombiner</td>
 *   <td>检索 AccessControlContext 的 DomainCombiner</td>
 *   <td>这允许某人检索 AccessControlContext 的 {@code DomainCombiner}。
 * 由于 DomainCombiners 可能包含敏感信息，这可能导致隐私泄露。</td>
 * </tr>
 *
 * <tr>
 *   <td>getPolicy</td>
 *   <td>检索系统范围的安全策略（具体来说，是当前安装的 Policy 对象）</td>
 *   <td>这允许某人通过 {@code getPermissions} 调用查询策略，
 * 这会披露授予给特定 CodeSource 的权限。虽然揭示策略不会危及系统的安全性，
 * 但它确实为恶意代码提供了额外的信息，使其能够更精准地发起攻击。明智的做法是不要泄露不必要的信息。</td>
 * </tr>
 *
 * <tr>
 *   <td>setPolicy</td>
 *   <td>设置系统范围的安全策略（具体来说，是 Policy 对象）</td>
 *   <td>授予此权限极其危险，因为恶意代码可能会授予自己所有必要的权限，
 * 从而成功地对系统发起攻击。</td>
 * </tr>
 *
 * <tr>
 *   <td>createPolicy.{policy type}</td>
 *   <td>从提供者获取 Policy 实现的实例</td>
 *   <td>授予此权限允许代码获取 Policy 对象。恶意代码可以查询 Policy 对象以确定授予给其他代码的权限。</td>
 * </tr>
 *
 * <tr>
 *   <td>getProperty.{key}</td>
 *   <td>检索指定键的安全属性</td>
 *   <td>根据授予访问权限的特定键，代码可能有权访问安全提供者的列表，
 * 以及系统范围和用户安全策略的位置。虽然揭示这些信息不会危及系统的安全性，
 * 但它确实为恶意代码提供了额外的信息，使其能够更精准地发起攻击。</td>
 * </tr>
 *
 * <tr>
 *   <td>setProperty.{key}</td>
 *   <td>设置指定键的安全属性</td>
 *   <td>这可能包括设置安全提供者或定义系统范围安全策略的位置。恶意代码如果被允许设置新的安全提供者，
 * 可能会设置一个窃取机密信息（如加密私钥）的恶意提供者。此外，恶意代码如果被允许设置系统范围安全策略的位置，
 * 可能会指向一个授予攻击者所有必要权限的安全策略，从而成功地对系统发起攻击。</td>
 * </tr>
 *
 * <tr>
 *   <td>insertProvider</td>
 *   <td>添加新的提供者</td>
 *   <td>这允许某人引入一个可能的恶意提供者（例如，一个泄露传递给它的私钥的提供者）作为最高优先级的提供者。
 * 这是可能的，因为 Security 对象（管理已安装的提供者）在附加提供者之前不会检查其完整性和真实性。
 * "insertProvider" 权限涵盖了 "insertProvider.{provider name}" 权限（见下文以获取更多信息）。</td>
 * </tr>
 *
 * <tr>
 *   <td>removeProvider.{provider name}</td>
 *   <td>移除指定的提供者</td>
 *   <td>这可能会改变程序的其他部分的行为或禁用其执行。如果程序随后请求的提供者已被移除，
 * 执行可能会失败。此外，如果被移除的提供者不是程序其余部分显式请求的，但通常是请求加密服务时选择的提供者
 * （由于其在提供者列表中的先前顺序），将选择不同的提供者，或者找不到合适的提供者，从而导致程序失败。</td>
 * </tr>
 *
 * <tr>
 *   <td>clearProviderProperties.{provider name}</td>
 *   <td>“清除”指定的 Provider，使其不再包含用于查找提供者实现的服务的属性</td>
 *   <td>这禁用了提供者实现的服务的查找。这可能会改变程序的其他部分的行为或禁用其执行，这些部分通常会使用该 Provider，
 * 详见 "removeProvider.{provider name}" 权限的描述。</td>
 * </tr>
 *
 * <tr>
 *   <td>putProviderProperty.{provider name}</td>
 *   <td>为指定的 Provider 设置属性</td>
 *   <td>提供者属性每个指定提供者实现的特定服务的名称和位置。通过授予此权限，你允许代码用另一个服务规范替换服务规范，
 * 从而指定不同的实现。</td>
 * </tr>
 *
 * <tr>
 *   <td>removeProviderProperty.{provider name}</td>
 *   <td>从指定的 Provider 中移除属性</td>
 *   <td>这禁用了提供者实现的服务的查找。由于移除了指定其名称和位置的属性，这些服务不再可访问。
 * 这可能会改变程序的其他部分的行为或禁用其执行，这些部分通常会使用该 Provider，详见 "removeProvider.{provider name}" 权限的描述。</td>
 * </tr>
 *
 * </table>
 *
 * <P>
 * 以下权限已被更新的权限取代或与已弃用的类相关：{@link Identity}、{@link IdentityScope}、{@link Signer}。
 * 建议不要使用它们。请参阅相关类以获取更多信息。
 *
 * <table border=1 cellpadding=5 summary="目标名称，该权限允许的操作，以及允许该权限的风险">
 * <tr>
 * <th>权限目标名称</th>
 * <th>该权限允许的操作</th>
 * <th>允许该权限的风险</th>
 * </tr>
 *
 * <tr>
 *   <td>insertProvider.{provider name}</td>
 *   <td>添加具有指定名称的新提供者</td>
 *   <td>建议不要进一步使用此权限，因为可以通过覆盖 {@link java.security.Provider#getName} 方法来规避名称限制。
 * 此外，授予代码插入具有特定名称或任何名称的提供者的权限具有同等的风险。用户应使用 "insertProvider" 权限。
 * <p>这允许某人引入一个可能的恶意提供者（例如，一个泄露传递给它的私钥的提供者）作为最高优先级的提供者。
 * 这是可能的，因为 Security 对象（管理已安装的提供者）在附加提供者之前不会检查其完整性和真实性。</td>
 * </tr>
 *
 * <tr>
 *   <td>setSystemScope</td>
 *   <td>设置系统身份范围</td>
 *   <td>这允许攻击者使用不应被信任的证书配置系统身份范围，从而授予使用这些证书签名的 applet 或应用程序代码
 * 本应被系统原始身份范围拒绝的特权。</td>
 * </tr>
 *
 * <tr>
 *   <td>setIdentityPublicKey</td>
 *   <td>为 Identity 设置公钥</td>
 *   <td>如果身份被标记为“受信任”，这允许攻击者引入一个不同的公钥（例如，其自己的公钥），该公钥不受系统身份范围的信任，
 * 从而授予使用该公钥签名的 applet 或应用程序代码本应被拒绝的特权。</td>
 * </tr>
 *
 * <tr>
 *   <td>setIdentityInfo</td>
 *   <td>为 Identity 设置通用信息字符串</td>
 *   <td>这允许攻击者为身份设置通用描述。这可能会导致应用程序使用不同的身份，或者阻止应用程序找到特定的身份。</td>
 * </tr>
 *
 * <tr>
 *   <td>addIdentityCertificate</td>
 *   <td>为 Identity 添加证书</td>
 *   <td>这允许攻击者为身份的公钥设置证书。这是危险的，因为它会影响系统范围的信任关系。该公钥突然变得比以前更受信任。</td>
 * </tr>
 *
 * <tr>
 *   <td>removeIdentityCertificate</td>
 *   <td>为 Identity 移除证书</td>
 *   <td>这允许攻击者移除身份的公钥的证书。这是危险的，因为它会影响系统范围的信任关系。该公钥突然变得比以前更不值得信任。</td>
 * </tr>
 *
 * <tr>
 *  <td>printIdentity</td>
 *  <td>查看主体的名称，可选地查看其使用范围，以及是否在该范围内被认为是“受信任的”</td>
 *  <td>打印出的范围可能是文件名，这可能会泄露本地系统信息。例如，以下是一个名为 "carol" 的身份的示例输出，
 * 该身份在用户的身份数据库中被标记为不受信任：<br>
 *   carol[/home/luehe/identitydb.obj][not trusted]</td>
 *</tr>
 *
 * <tr>
 *   <td>getSignerPrivateKey</td>
 *   <td>检索 Signer 的私钥</td>
 *   <td>允许访问私钥是非常危险的；私钥应保持秘密。否则，代码可以使用私钥签署各种文件，并声称签名来自 Signer。</td>
 * </tr>
 *
 * <tr>
 *   <td>setSignerKeyPair</td>
 *   <td>为 Signer 设置密钥对（公钥和私钥）</td>
 *   <td>这允许攻击者用可能较弱的密钥对（例如，较小密钥大小的密钥对）替换目标的密钥对。
 * 这还允许攻击者监听目标与其对等方之间的加密通信。目标的对等方可能会使用目标的“新”公钥包装加密会话密钥，
 * 这将允许攻击者（拥有相应私钥的攻击者）解包会话密钥并解密使用该会话密钥加密的通信数据。</td>
 * </tr>
 *
 * </table>
 *
 * @see java.security.BasicPermission
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.lang.SecurityManager
 *
 *
 * @author Marianne Mueller
 * @author Roland Schemers
 */

public final class SecurityPermission extends BasicPermission {

    private static final long serialVersionUID = 5236109936224050470L;

    /**
     * 创建具有指定名称的新 SecurityPermission。
     * 名称是 SecurityPermission 的符号名称。名称的末尾可以出现星号，跟随一个 "."，或者单独出现，表示通配符匹配。
     *
     * @param name SecurityPermission 的名称
     *
     * @throws NullPointerException 如果 {@code name} 为 {@code null}。
     * @throws IllegalArgumentException 如果 {@code name} 为空。
     */
    public SecurityPermission(String name)
    {
        super(name);
    }

    /**
     * 创建具有指定名称的新 SecurityPermission 对象。
     * 名称是 SecurityPermission 的符号名称，actions 字符串目前未使用，应为 null。
     *
     * @param name SecurityPermission 的名称
     * @param actions 应为 null。
     *
     * @throws NullPointerException 如果 {@code name} 为 {@code null}。
     * @throws IllegalArgumentException 如果 {@code name} 为空。
     */
    public SecurityPermission(String name, String actions)
    {
        super(name, actions);
    }
}
