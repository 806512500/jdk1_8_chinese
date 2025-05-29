/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

package java.security.cert;

import java.util.Iterator;
import java.util.Set;

/**
 * 由 PKIX 证书路径验证算法定义的不可变有效策略树节点。
 *
 * <p>PKIX 证书路径验证算法的一个输出是有效策略树，该树包括确定为有效的策略、如何进行此确定以及遇到的任何策略限定符。此树的深度为
 * <i>n</i>，其中 <i>n</i> 是已验证的证书路径的长度。
 *
 * <p>大多数应用程序不需要检查有效策略树。它们可以通过设置 {@code PKIXParameters} 中的策略相关参数来实现其策略处理目标。然而，
 * 有效策略树可用于更复杂的应用程序，特别是那些处理策略限定符的应用程序。
 *
 * <p>{@link PKIXCertPathValidatorResult#getPolicyTree()
 * PKIXCertPathValidatorResult.getPolicyTree} 返回有效策略树的根节点。可以使用
 * {@link #getChildren getChildren} 和 {@link #getParent getParent} 方法遍历树。
 * 可以使用 {@code PolicyNode} 的其他方法检索特定节点的数据。
 *
 * <p><b>并发访问</b>
 * <p>所有 {@code PolicyNode} 对象必须是不可变的并且线程安全的。多个线程可以同时调用定义在此类中的方法
 * 单个 {@code PolicyNode} 对象（或多个）而不会产生不良影响。此规定适用于此类的所有公共字段和方法以及子类添加或覆盖的任何方法。
 *
 * @since       1.4
 * @author      Sean Mullan
 */
public interface PolicyNode {

    /**
     * 返回此节点的父节点，如果这是根节点，则返回 {@code null}。
     *
     * @return 此节点的父节点，如果这是根节点，则返回 {@code null}
     */
    PolicyNode getParent();

    /**
     * 返回此节点的子节点的迭代器。任何尝试通过
     * {@code Iterator} 的 remove 方法修改此节点的子节点都必须抛出
     * {@code UnsupportedOperationException}。
     *
     * @return 此节点的子节点的迭代器
     */
    Iterator<? extends PolicyNode> getChildren();

    /**
     * 返回此节点在有效策略树中的深度。
     *
     * @return 此节点的深度（根节点为 0，其子节点为 1，依此类推）
     */
    int getDepth();

    /**
     * 返回此节点表示的有效策略。
     *
     * @return 此节点表示的有效策略的 {@code String} OID。对于根节点，此方法始终返回
     * 特殊的 anyPolicy OID: "2.5.29.32.0"。
     */
    String getValidPolicy();

    /**
     * 返回与此节点表示的有效策略关联的策略限定符集。
     *
     * @return 一个不可变的 {@code Set}，包含 {@code PolicyQualifierInfo}。对于根节点，这
     * 始终是一个空的 {@code Set}。
     */
    Set<? extends PolicyQualifierInfo> getPolicyQualifiers();

    /**
     * 返回在处理下一个证书时满足此节点有效策略的预期策略集。
     *
     * @return 一个不可变的 {@code Set}，包含预期策略的 {@code String} OID。对于根节点，此方法
     * 始终返回一个包含一个元素的 {@code Set}，即特殊的 anyPolicy OID: "2.5.29.32.0"。
     */
    Set<String> getExpectedPolicies();

    /**
     * 返回最近处理的证书中证书策略扩展的关键性指示符。
     *
     * @return 如果扩展标记为关键，则返回 {@code true}，否则返回 {@code false}。对于根节点，始终返回 {@code false}。
     */
    boolean isCritical();
}
