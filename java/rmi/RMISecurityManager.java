/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi;

import java.security.*;

/**
 * {@code RMISecurityManager} 实现了一个与 {@link SecurityManager} 实现的策略相同的策略。RMI 应用程序
 * 应该使用 {@code SecurityManager} 类或另一个合适的 {@code SecurityManager} 实现，而不是使用此类。RMI 的类
 * 加载器仅在设置了安全经理的情况下才会从远程位置下载类。
 *
 * @implNote
 * <p>通常，小程序在已经有一个安全经理的容器中运行，因此通常不需要小程序设置安全经理。如果您有一个独立的应用程序，您可能需要设置一个
 * {@code SecurityManager} 以启用类下载。这可以通过在代码中添加以下内容来完成。（它需要在 RMI 从远程主机下载代码之前执行，因此最有可能需要出现在
 * 应用程序的 {@code main} 方法中。）
 *
 * <pre>{@code
 *    if (System.getSecurityManager() == null) {
 *        System.setSecurityManager(new SecurityManager());
 *    }
 * }</pre>
 *
 * @author  Roger Riggs
 * @author  Peter Jones
 * @since JDK1.1
 * @deprecated 使用 {@link SecurityManager} 代替。
 */
@Deprecated
public class RMISecurityManager extends SecurityManager {

    /**
     * 构造一个新的 {@code RMISecurityManager}。
     * @since JDK1.1
     */
    public RMISecurityManager() {
    }
}
