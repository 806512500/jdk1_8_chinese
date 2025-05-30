/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.datatransfer;

import java.awt.EventQueue;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import java.io.IOException;

import sun.awt.EventListenerAggregate;

/**
 * 一个实现使用剪切/复制/粘贴操作传输数据的机制的类。
 * <p>
 * 可以在 Clipboard 类的实例上注册 {@link FlavorListener} 以在该剪贴板上可用的
 * {@link DataFlavor} 集合发生变化时收到通知（参见 {@link #addFlavorListener}）。
 *
 * @see java.awt.Toolkit#getSystemClipboard
 * @see java.awt.Toolkit#getSystemSelection
 *
 * @author      Amy Fowler
 * @author      Alexander Gerasimov
 */
public class Clipboard {

    String name;

    protected ClipboardOwner owner;
    protected Transferable contents;

    /**
     * 注册在此本地剪贴板上的风味监听器的集合。
     *
     * @since 1.5
     */
    private EventListenerAggregate flavorListeners;

    /**
     * 一个在该本地剪贴板上可用的 <code>DataFlavor</code> 集合。用于跟踪该剪贴板上
     * 可用的 <code>DataFlavor</code> 的变化。
     *
     * @since 1.5
     */
    private Set<DataFlavor> currentDataFlavors;

    /**
     * 创建一个剪贴板对象。
     *
     * @see java.awt.Toolkit#getSystemClipboard
     */
    public Clipboard(String name) {
        this.name = name;
    }

    /**
     * 返回此剪贴板对象的名称。
     *
     * @see java.awt.Toolkit#getSystemClipboard
     */
    public String getName() {
        return name;
    }

    /**
     * 将剪贴板的当前内容设置为指定的可传输对象，并注册指定的剪贴板所有者作为新内容的所有者。
     * <p>
     * 如果现有所有者与参数 <code>owner</code> 不同，该所有者将通过调用该所有者的
     * <code>ClipboardOwner.lostOwnership()</code> 方法通知其不再拥有剪贴板内容。
     * <code>setContents()</code> 的实现可以不直接从此方法调用 <code>lostOwnership()</code>。
     * 例如，<code>lostOwnership()</code> 可能在稍后在不同的线程上调用。同样的规则适用于在此剪贴板上注册的
     * <code>FlavorListener</code>。
     * <p>
     * 如果剪贴板当前不可用，该方法将抛出 <code>IllegalStateException</code>。例如，在某些平台上，当系统剪贴板
     * 被另一个应用程序访问时，系统剪贴板不可用。
     *
     * @param contents 代表剪贴板内容的可传输对象
     * @param owner 拥有剪贴板内容的对象
     * @throws IllegalStateException 如果剪贴板当前不可用
     * @see java.awt.Toolkit#getSystemClipboard
     */
    public synchronized void setContents(Transferable contents, ClipboardOwner owner) {
        final ClipboardOwner oldOwner = this.owner;
        final Transferable oldContents = this.contents;

        this.owner = owner;
        this.contents = contents;

        if (oldOwner != null && oldOwner != owner) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    oldOwner.lostOwnership(Clipboard.this, oldContents);
                }
            });
        }
        fireFlavorsChanged();
    }

    /**
     * 返回一个代表剪贴板当前内容的可传输对象。如果剪贴板当前没有内容，它返回 <code>null</code>。
     * 参数 Object requestor 当前未使用。如果剪贴板当前不可用，该方法将抛出
     * <code>IllegalStateException</code>。例如，在某些平台上，当系统剪贴板被另一个应用程序访问时，系统剪贴板
     * 不可用。
     *
     * @param requestor 请求剪贴数据的对象（未使用）
     * @return 剪贴板上的当前可传输对象
     * @throws IllegalStateException 如果剪贴板当前不可用
     * @see java.awt.Toolkit#getSystemClipboard
     */
    public synchronized Transferable getContents(Object requestor) {
        return contents;
    }


    /**
     * 返回一个 <code>DataFlavor</code> 数组，表示此剪贴板当前内容可用的格式。如果没有
     * <code>DataFlavor</code> 可用，此方法返回一个零长度的数组。
     *
     * @return 一个 <code>DataFlavor</code> 数组，表示此剪贴板当前内容可用的格式
     *
     * @throws IllegalStateException 如果此剪贴板当前不可用
     *
     * @since 1.5
     */
    public DataFlavor[] getAvailableDataFlavors() {
        Transferable cntnts = getContents(null);
        if (cntnts == null) {
            return new DataFlavor[0];
        }
        return cntnts.getTransferDataFlavors();
    }

    /**
     * 返回此剪贴板当前内容是否可以以指定的 <code>DataFlavor</code> 提供。
     *
     * @param flavor 请求的 <code>DataFlavor</code> 格式
     *
     * @return 如果此剪贴板当前内容可以以指定的 <code>DataFlavor</code> 提供，则返回 <code>true</code>；
     *         否则返回 <code>false</code>
     *
     * @throws NullPointerException 如果 <code>flavor</code> 为 <code>null</code>
     * @throws IllegalStateException 如果此剪贴板当前不可用
     *
     * @since 1.5
     */
    public boolean isDataFlavorAvailable(DataFlavor flavor) {
        if (flavor == null) {
            throw new NullPointerException("flavor");
        }

        Transferable cntnts = getContents(null);
        if (cntnts == null) {
            return false;
        }
        return cntnts.isDataFlavorSupported(flavor);
    }

    /**
     * 返回一个对象，表示此剪贴板当前内容在指定的 <code>DataFlavor</code> 中的表示。
     * 返回对象的类由 <code>flavor</code> 的表示类定义。
     *
     * @param flavor 请求的 <code>DataFlavor</code> 格式
     *
     * @return 一个对象，表示此剪贴板当前内容在指定的 <code>DataFlavor</code> 中的表示
     *
     * @throws NullPointerException 如果 <code>flavor</code> 为 <code>null</code>
     * @throws IllegalStateException 如果此剪贴板当前不可用
     * @throws UnsupportedFlavorException 如果请求的 <code>DataFlavor</code> 不可用
     * @throws IOException 如果无法检索请求的 <code>DataFlavor</code> 中的数据
     *
     * @see DataFlavor#getRepresentationClass
     *
     * @since 1.5
     */
    public Object getData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
        if (flavor == null) {
            throw new NullPointerException("flavor");
        }

        Transferable cntnts = getContents(null);
        if (cntnts == null) {
            throw new UnsupportedFlavorException(flavor);
        }
        return cntnts.getTransferData(flavor);
    }


    /**
     * 注册指定的 <code>FlavorListener</code> 以从该剪贴板接收 <code>FlavorEvent</code>。
     * 如果 <code>listener</code> 为 <code>null</code>，则不抛出异常且不执行任何操作。
     *
     * @param listener 要添加的监听器
     *
     * @see #removeFlavorListener
     * @see #getFlavorListeners
     * @see FlavorListener
     * @see FlavorEvent
     * @since 1.5
     */
    public synchronized void addFlavorListener(FlavorListener listener) {
        if (listener == null) {
            return;
        }
        if (flavorListeners == null) {
            currentDataFlavors = getAvailableDataFlavorSet();
            flavorListeners = new EventListenerAggregate(FlavorListener.class);
        }
        flavorListeners.add(listener);
    }

    /**
     * 移除指定的 <code>FlavorListener</code>，使其不再从该 <code>Clipboard</code> 接收 <code>FlavorEvent</code>。
     * 如果参数指定的监听器之前未添加到此 <code>Clipboard</code>，则此方法不执行任何操作，也不抛出异常。
     * 如果 <code>listener</code> 为 <code>null</code>，则不抛出异常且不执行任何操作。
     *
     * @param listener 要移除的监听器
     *
     * @see #addFlavorListener
     * @see #getFlavorListeners
     * @see FlavorListener
     * @see FlavorEvent
     * @since 1.5
     */
    public synchronized void removeFlavorListener(FlavorListener listener) {
        if (listener == null || flavorListeners == null) {
            return;
        }
        flavorListeners.remove(listener);
    }

    /**
     * 返回在此 <code>Clipboard</code> 上当前注册的所有 <code>FlavorListener</code> 的数组。
     *
     * @return 此剪贴板的所有 <code>FlavorListener</code> 或如果当前未注册任何监听器，则返回一个空数组
     * @see #addFlavorListener
     * @see #removeFlavorListener
     * @see FlavorListener
     * @see FlavorEvent
     * @since 1.5
     */
    public synchronized FlavorListener[] getFlavorListeners() {
        return flavorListeners == null ? new FlavorListener[0] :
                (FlavorListener[])flavorListeners.getListenersCopy();
    }

    /**
     * 检查 <code>DataFlavor</code> 的变化，如有必要，通知所有对 <code>FlavorEvent</code>
     * 感兴趣的监听器。
     *
     * @since 1.5
     */
    private void fireFlavorsChanged() {
        if (flavorListeners == null) {
            return;
        }
        Set<DataFlavor> prevDataFlavors = currentDataFlavors;
        currentDataFlavors = getAvailableDataFlavorSet();
        if (prevDataFlavors.equals(currentDataFlavors)) {
            return;
        }
        FlavorListener[] flavorListenerArray =
                (FlavorListener[])flavorListeners.getListenersInternal();
        for (int i = 0; i < flavorListenerArray.length; i++) {
            final FlavorListener listener = flavorListenerArray[i];
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    listener.flavorsChanged(new FlavorEvent(Clipboard.this));
                }
            });
        }
    }

    /**
     * 返回此剪贴板上当前可用的 <code>DataFlavor</code> 集合。
     *
     * @return 此剪贴板上当前可用的 <code>DataFlavor</code> 集合
     *
     * @since 1.5
     */
    private Set<DataFlavor> getAvailableDataFlavorSet() {
        Set<DataFlavor> set = new HashSet<>();
        Transferable contents = getContents(null);
        if (contents != null) {
            DataFlavor[] flavors = contents.getTransferDataFlavors();
            if (flavors != null) {
                set.addAll(Arrays.asList(flavors));
            }
        }
        return set;
    }
}
