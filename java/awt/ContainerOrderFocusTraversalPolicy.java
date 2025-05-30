
/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.awt;

import java.util.List;
import java.util.ArrayList;
import sun.util.logging.PlatformLogger;

/**
 * 一种基于容器中子组件顺序确定遍历顺序的焦点遍历策略。从特定的焦点循环根开始，该策略对组件层次结构进行先序遍历，并根据
 * <code>Container.getComponents()</code> 返回的数组顺序遍历容器的子组件。不可见和不可显示的部分不会被搜索。
 * <p>
 * 默认情况下，ContainerOrderFocusTraversalPolicy 隐式地将焦点向下传递。也就是说，在正常的前向焦点遍历中，焦点循环根之后的
 * 组件将是焦点循环根的默认焦点组件。可以使用 <code>setImplicitDownCycleTraversal</code> 方法禁用此行为。
 * <p>
 * 默认情况下，此类的方法仅在组件可见、可显示、启用且可聚焦时返回该组件。子类可以通过重写 <code>accept</code> 方法来修改此行为。
 * <p>
 * 该策略考虑了 <a
 * href="doc-files/FocusSpec.html#FocusTraversalPolicyProviders">焦点遍历策略提供者</a>。在搜索第一个/最后一个/下一个/上一个组件时，
 * 如果遇到焦点遍历策略提供者，将使用其焦点遍历策略执行搜索操作。
 *
 * @author David Mendenhall
 *
 * @see Container#getComponents
 * @since 1.4
 */
public class ContainerOrderFocusTraversalPolicy extends FocusTraversalPolicy
    implements java.io.Serializable
{
    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.ContainerOrderFocusTraversalPolicy");

    final private int FORWARD_TRAVERSAL = 0;
    final private int BACKWARD_TRAVERSAL = 1;

    /*
     * JDK 1.4 serialVersionUID
     */
    private static final long serialVersionUID = 486933713763926351L;

    private boolean implicitDownCycleTraversal = true;

    /**
     * 为了提高效率，由 getComponentAfter 和 getComponentBefore 使用。为了保持与 FocusTraversalPolicy 规范的兼容性，
     * 如果遍历循环，则应调用 getFirstComponent 或 getLastComponent。这些方法可以在子类中被重写以表现出非通用的行为。
     * 然而，在通用情况下，这些方法将分别返回排序列表中的第一个或最后一个组件。由于 getComponentAfter 和
     * getComponentBefore 在确定需要调用 getFirstComponent 或 getLastComponent 之前已经构建了列表，如果可能的话，应重用该列表。
     */
    transient private Container cachedRoot;
    transient private List<Component> cachedCycle;

    /*
     * 我们打算使用 getFocusTraversalCycle 和 getComponentIndex 方法将策略分为两部分：
     * 1) 创建焦点遍历循环。
     * 2) 遍历循环。
     * 第一点假设生成一个表示焦点遍历循环的组件列表。上述两个方法应实现此逻辑。
     * 第二点假设实现操作循环的通用概念：前后遍历，检索初始/默认/第一个/最后一个组件。这些概念在 AWT 焦点规范中描述，并应用于
     * FocusTraversalPolicy。因此，该策略的后代可能希望不重新实现第二点的逻辑，而只是重写第一点的实现。
     * javax.swing.SortingFocusTraversalPolicy 是这样一个后代的典型例子。
     */
    /*protected*/ private List<Component> getFocusTraversalCycle(Container aContainer) {
        List<Component> cycle = new ArrayList<Component>();
        enumerateCycle(aContainer, cycle);
        return cycle;
    }
    /*protected*/ private int getComponentIndex(List<Component> cycle, Component aComponent) {
        return cycle.indexOf(aComponent);
    }

    private void enumerateCycle(Container container, List<Component> cycle) {
        if (!(container.isVisible() && container.isDisplayable())) {
            return;
        }

        cycle.add(container);

        Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component comp = components[i];
            if (comp instanceof Container) {
                Container cont = (Container)comp;

                if (!cont.isFocusCycleRoot() && !cont.isFocusTraversalPolicyProvider()) {
                    enumerateCycle(cont, cycle);
                    continue;
                }
            }
            cycle.add(comp);
        }
    }

    private Container getTopmostProvider(Container focusCycleRoot, Component aComponent) {
        Container aCont = aComponent.getParent();
        Container ftp = null;
        while (aCont  != focusCycleRoot && aCont != null) {
            if (aCont.isFocusTraversalPolicyProvider()) {
                ftp = aCont;
            }
            aCont = aCont.getParent();
        }
        if (aCont == null) {
            return null;
        }
        return ftp;
    }

    /*
     * 检查是否发生新的焦点循环，并返回要遍历焦点到的组件。
     * @param comp 可能的焦点循环根或策略提供者
     * @param traversalDirection 遍历方向
     * @return 如果 {@code comp} 是根或提供者且隐式向下遍历已设置，则返回要遍历焦点到的组件，否则返回 {@code null}
     */
    private Component getComponentDownCycle(Component comp, int traversalDirection) {
        Component retComp = null;

        if (comp instanceof Container) {
            Container cont = (Container)comp;

            if (cont.isFocusCycleRoot()) {
                if (getImplicitDownCycleTraversal()) {
                    retComp = cont.getFocusTraversalPolicy().getDefaultComponent(cont);

                    if (retComp != null && log.isLoggable(PlatformLogger.Level.FINE)) {
                        log.fine("### Transfered focus down-cycle to " + retComp +
                                 " in the focus cycle root " + cont);
                    }
                } else {
                    return null;
                }
            } else if (cont.isFocusTraversalPolicyProvider()) {
                retComp = (traversalDirection == FORWARD_TRAVERSAL ?
                           cont.getFocusTraversalPolicy().getDefaultComponent(cont) :
                           cont.getFocusTraversalPolicy().getLastComponent(cont));

                if (retComp != null && log.isLoggable(PlatformLogger.Level.FINE)) {
                    log.fine("### Transfered focus to " + retComp + " in the FTP provider " + cont);
                }
            }
        }
        return retComp;
    }

    /**
     * 返回应接收焦点的组件，该组件在 aComponent 之后。aContainer 必须是 aComponent 的焦点循环根或焦点遍历策略提供者。
     * <p>
     * 默认情况下，ContainerOrderFocusTraversalPolicy 隐式地将焦点向下传递。也就是说，在正常的前向焦点遍历中，焦点循环根之后的
     * 组件将是焦点循环根的默认焦点组件。可以使用 <code>setImplicitDownCycleTraversal</code> 方法禁用此行为。
     * <p>
     * 如果 aContainer 是 <a href="doc-files/FocusSpec.html#FocusTraversalPolicyProviders">焦点遍历策略提供者</a>，
     * 则焦点总是向下传递。
     *
     * @param aContainer aComponent 的焦点循环根或焦点遍历策略提供者
     * @param aComponent aContainer 的（可能间接的）子组件，或 aContainer 本身
     * @return 应接收焦点的组件，或如果没有找到合适的组件则返回 null
     * @throws IllegalArgumentException 如果 aContainer 不是 aComponent 的焦点循环根或焦点遍历策略提供者，或 aContainer 或 aComponent 为 null
     */
    public Component getComponentAfter(Container aContainer, Component aComponent) {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("### Searching in " + aContainer + " for component after " + aComponent);
        }

        if (aContainer == null || aComponent == null) {
            throw new IllegalArgumentException("aContainer and aComponent cannot be null");
        }
        if (!aContainer.isFocusTraversalPolicyProvider() && !aContainer.isFocusCycleRoot()) {
            throw new IllegalArgumentException("aContainer should be focus cycle root or focus traversal policy provider");

        } else if (aContainer.isFocusCycleRoot() && !aComponent.isFocusCycleRoot(aContainer)) {
            throw new IllegalArgumentException("aContainer is not a focus cycle root of aComponent");
        }

        synchronized(aContainer.getTreeLock()) {

            if (!(aContainer.isVisible() && aContainer.isDisplayable())) {
                return null;
            }

            // 在进行所有检查之前，首先检查它是否是 FTP 提供者或焦点循环根。
            // 如果是这种情况，只需向下传递（如果设置为“隐式”）。
            Component comp = getComponentDownCycle(aComponent, FORWARD_TRAVERSAL);
            if (comp != null) {
                return comp;
            }

            // 检查组件是否在策略提供者内部。
            Container provider = getTopmostProvider(aContainer, aComponent);
            if (provider != null) {
                if (log.isLoggable(PlatformLogger.Level.FINE)) {
                    log.fine("### Asking FTP " + provider + " for component after " + aComponent);
                }

                // FTP 知道如何找到给定组件之后的组件。我们不知道。
                FocusTraversalPolicy policy = provider.getFocusTraversalPolicy();
                Component afterComp = policy.getComponentAfter(provider, aComponent);

                // null 结果意味着我们超出了 FTP 循环的限制。
                // 在这种情况下，我们必须退出循环，否则返回找到的组件。
                if (afterComp != null) {
                    if (log.isLoggable(PlatformLogger.Level.FINE)) {
                        log.fine("### FTP returned " + afterComp);
                    }
                    return afterComp;
                }
                aComponent = provider;
            }

            List<Component> cycle = getFocusTraversalCycle(aContainer);

            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine("### Cycle is " + cycle + ", component is " + aComponent);
            }

            int index = getComponentIndex(cycle, aComponent);

            if (index < 0) {
                if (log.isLoggable(PlatformLogger.Level.FINE)) {
                    log.fine("### Didn't find component " + aComponent + " in a cycle " + aContainer);
                }
                return getFirstComponent(aContainer);
            }

            for (index++; index < cycle.size(); index++) {
                comp = cycle.get(index);
                if (accept(comp)) {
                    return comp;
                } else if ((comp = getComponentDownCycle(comp, FORWARD_TRAVERSAL)) != null) {
                    return comp;
                }
            }

            if (aContainer.isFocusCycleRoot()) {
                this.cachedRoot = aContainer;
                this.cachedCycle = cycle;

                comp = getFirstComponent(aContainer);

                this.cachedRoot = null;
                this.cachedCycle = null;

                return comp;
            }
        }
        return null;
    }

    /**
     * 返回应接收焦点的组件，该组件在 aComponent 之前。aContainer 必须是 aComponent 的焦点循环根或 <a
     * href="doc-files/FocusSpec.html#FocusTraversalPolicyProviders">焦点遍历策略提供者</a>。
     *
     * @param aContainer aComponent 的焦点循环根或焦点遍历策略提供者
     * @param aComponent aContainer 的（可能间接的）子组件，或 aContainer 本身
     * @return 应接收焦点的组件，或如果没有找到合适的组件则返回 null
     * @throws IllegalArgumentException 如果 aContainer 不是 aComponent 的焦点循环根或焦点遍历策略提供者，或 aContainer 或 aComponent 为 null
     */
    public Component getComponentBefore(Container aContainer, Component aComponent) {
        if (aContainer == null || aComponent == null) {
            throw new IllegalArgumentException("aContainer and aComponent cannot be null");
        }
        if (!aContainer.isFocusTraversalPolicyProvider() && !aContainer.isFocusCycleRoot()) {
            throw new IllegalArgumentException("aContainer should be focus cycle root or focus traversal policy provider");

        } else if (aContainer.isFocusCycleRoot() && !aComponent.isFocusCycleRoot(aContainer)) {
            throw new IllegalArgumentException("aContainer is not a focus cycle root of aComponent");
        }

        synchronized(aContainer.getTreeLock()) {

            if (!(aContainer.isVisible() && aContainer.isDisplayable())) {
                return null;
            }

            // 检查组件是否在策略提供者内部。
            Container provider = getTopmostProvider(aContainer, aComponent);
            if (provider != null) {
                if (log.isLoggable(PlatformLogger.Level.FINE)) {
                    log.fine("### Asking FTP " + provider + " for component after " + aComponent);
                }

                // FTP 知道如何找到给定组件之后的组件。我们不知道。
                FocusTraversalPolicy policy = provider.getFocusTraversalPolicy();
                Component beforeComp = policy.getComponentBefore(provider, aComponent);


                            // 空结果意味着我们超出了FTP周期的限制。
                // 在这种情况下，我们必须退出周期，否则返回找到的组件。
                if (beforeComp != null) {
                    if (log.isLoggable(PlatformLogger.Level.FINE)) {
                        log.fine("### FTP 返回 " + beforeComp);
                    }
                    return beforeComp;
                }
                aComponent = provider;

                // 如果提供者是可遍历的，则返回它。
                if (accept(aComponent)) {
                    return aComponent;
                }
            }

            List<Component> cycle = getFocusTraversalCycle(aContainer);

            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine("### 周期是 " + cycle + ", 组件是 " + aComponent);
            }

            int index = getComponentIndex(cycle, aComponent);

            if (index < 0) {
                if (log.isLoggable(PlatformLogger.Level.FINE)) {
                    log.fine("### 没有在周期 " + aContainer + " 中找到组件 " + aComponent);
                }
                return getLastComponent(aContainer);
            }

            Component comp = null;
            Component tryComp = null;

            for (index--; index >= 0; index--) {
                comp = cycle.get(index);
                if (comp != aContainer && (tryComp = getComponentDownCycle(comp, BACKWARD_TRAVERSAL)) != null) {
                    return tryComp;
                } else if (accept(comp)) {
                    return comp;
                }
            }

            if (aContainer.isFocusCycleRoot()) {
                this.cachedRoot = aContainer;
                this.cachedCycle = cycle;

                comp = getLastComponent(aContainer);

                this.cachedRoot = null;
                this.cachedCycle = null;

                return comp;
            }
        }
        return null;
    }

    /**
     * 返回遍历周期中的第一个组件。此方法用于确定在正向遍历时下一个要聚焦的组件。
     *
     * @param aContainer 要返回第一个组件的焦点周期根或焦点遍历策略提供者
     * @return aContainer 遍历周期中的第一个组件，
     *         如果找不到合适的组件则返回 null
     * @throws IllegalArgumentException 如果 aContainer 为 null
     */
    public Component getFirstComponent(Container aContainer) {
        List<Component> cycle;

        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("### 获取 " + aContainer + " 中的第一个组件");
        }
        if (aContainer == null) {
            throw new IllegalArgumentException("aContainer 不能为 null");

        }

        synchronized(aContainer.getTreeLock()) {

            if (!(aContainer.isVisible() && aContainer.isDisplayable())) {
                return null;
            }

            if (this.cachedRoot == aContainer) {
                cycle = this.cachedCycle;
            } else {
                cycle = getFocusTraversalCycle(aContainer);
            }

            if (cycle.size() == 0) {
                if (log.isLoggable(PlatformLogger.Level.FINE)) {
                    log.fine("### 周期为空");
                }
                return null;
            }
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine("### 周期是 " + cycle);
            }

            for (Component comp : cycle) {
                if (accept(comp)) {
                    return comp;
                } else if (comp != aContainer &&
                           (comp = getComponentDownCycle(comp, FORWARD_TRAVERSAL)) != null)
                {
                    return comp;
                }
            }
        }
        return null;
    }

    /**
     * 返回遍历周期中的最后一个组件。此方法用于确定在反向遍历时下一个要聚焦的组件。
     *
     * @param aContainer 要返回最后一个组件的焦点周期根或焦点遍历策略提供者
     * @return aContainer 遍历周期中的最后一个组件，
     *         如果找不到合适的组件则返回 null
     * @throws IllegalArgumentException 如果 aContainer 为 null
     */
    public Component getLastComponent(Container aContainer) {
        List<Component> cycle;
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("### 获取 " + aContainer + " 中的最后一个组件");
        }

        if (aContainer == null) {
            throw new IllegalArgumentException("aContainer 不能为 null");
        }

        synchronized(aContainer.getTreeLock()) {

            if (!(aContainer.isVisible() && aContainer.isDisplayable())) {
                return null;
            }

            if (this.cachedRoot == aContainer) {
                cycle = this.cachedCycle;
            } else {
                cycle = getFocusTraversalCycle(aContainer);
            }

            if (cycle.size() == 0) {
                if (log.isLoggable(PlatformLogger.Level.FINE)) {
                    log.fine("### 周期为空");
                }
                return null;
            }
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine("### 周期是 " + cycle);
            }

            for (int i = cycle.size() - 1; i >= 0; i--) {
                Component comp = cycle.get(i);
                if (accept(comp)) {
                    return comp;
                } else if (comp instanceof Container && comp != aContainer) {
                    Container cont = (Container)comp;
                    if (cont.isFocusTraversalPolicyProvider()) {
                        Component retComp = cont.getFocusTraversalPolicy().getLastComponent(cont);
                        if (retComp != null) {
                            return retComp;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 返回默认的聚焦组件。此组件将是遍历进入以 aContainer 为根的新焦点遍历周期时第一个接收焦点的组件。
     * 此方法的默认实现返回与 <code>getFirstComponent</code> 相同的组件。
     *
     * @param aContainer 要返回默认组件的焦点周期根或焦点遍历策略提供者
     * @return aContainer 遍历周期中的默认组件，
     *         如果找不到合适的组件则返回 null
     * @see #getFirstComponent
     * @throws IllegalArgumentException 如果 aContainer 为 null
     */
    public Component getDefaultComponent(Container aContainer) {
        return getFirstComponent(aContainer);
    }

    /**
     * 设置此 ContainerOrderFocusTraversalPolicy 是否隐式地向下传递焦点。
     * 如果为 <code>true</code>，在正常的正向焦点遍历时，焦点周期根之后遍历的组件将是焦点周期根的默认聚焦组件。
     * 如果为 <code>false</code>，则将遍历指定焦点周期根的焦点遍历周期中的下一个组件。
     * 此属性的默认值为 <code>true</code>。
     *
     * @param implicitDownCycleTraversal 是否此 ContainerOrderFocusTraversalPolicy 隐式地向下传递焦点
     * @see #getImplicitDownCycleTraversal
     * @see #getFirstComponent
     */
    public void setImplicitDownCycleTraversal(boolean implicitDownCycleTraversal) {
        this.implicitDownCycleTraversal = implicitDownCycleTraversal;
    }

    /**
     * 返回此 ContainerOrderFocusTraversalPolicy 是否隐式地向下传递焦点。
     * 如果为 <code>true</code>，在正常的正向焦点遍历时，焦点周期根之后遍历的组件将是焦点周期根的默认聚焦组件。
     * 如果为 <code>false</code>，则将遍历指定焦点周期根的焦点遍历周期中的下一个组件。
     *
     * @return 是否此 ContainerOrderFocusTraversalPolicy 隐式地向下传递焦点
     * @see #setImplicitDownCycleTraversal
     * @see #getFirstComponent
     */
    public boolean getImplicitDownCycleTraversal() {
        return implicitDownCycleTraversal;
    }

    /**
     * 确定一个组件是否可以作为新的焦点所有者。默认情况下，此方法仅在组件可见、可显示、启用且可聚焦时接受该组件。
     *
     * @param aComponent 要测试其作为焦点所有者的适配性的组件
     * @return <code>true</code> 如果 aComponent 可见、可显示、启用且可聚焦；否则返回 <code>false</code>
     */
    protected boolean accept(Component aComponent) {
        if (!aComponent.canBeFocusOwner()) {
            return false;
        }

        // 验证组件是否递归启用。禁用重量级容器会禁用其子组件，而禁用轻量级容器则不会。
        if (!(aComponent instanceof Window)) {
            for (Container enableTest = aComponent.getParent();
                 enableTest != null;
                 enableTest = enableTest.getParent())
            {
                if (!(enableTest.isEnabled() || enableTest.isLightweight())) {
                    return false;
                }
                if (enableTest instanceof Window) {
                    break;
                }
            }
        }

        return true;
    }
}
