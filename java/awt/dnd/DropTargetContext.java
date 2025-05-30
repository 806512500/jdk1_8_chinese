/*
 * Copyright (c) 1997, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.dnd;

import java.awt.Component;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.awt.dnd.peer.DropTargetContextPeer;

import java.io.IOException;
import java.io.Serializable;

import java.util.Arrays;
import java.util.List;


/**
 * 一个 <code>DropTargetContext</code> 在拖放操作中，当逻辑光标与
 * 与 <code>DropTarget</code> 关联的 <code>Component</code> 的可见几何形状重合时创建。
 * <code>DropTargetContext</code> 提供了一种机制，使潜在的接收者
 * 既可以为最终用户提供适当的拖动反馈，也可以在适当的情况下执行后续的数据传输。
 *
 * @since 1.2
 */

public class DropTargetContext implements Serializable {

    private static final long serialVersionUID = -634158968993743371L;

    /**
     * 给定一个指定的 <code>DropTarget</code> 构造一个 <code>DropTargetContext</code>。
     * <P>
     * @param dt 要关联的 DropTarget
     */

    DropTargetContext(DropTarget dt) {
        super();

        dropTarget = dt;
    }

    /**
     * 返回与此 <code>DropTargetContext</code> 关联的 <code>DropTarget</code>。
     * <P>
     * @return 与此 <code>DropTargetContext</code> 关联的 <code>DropTarget</code>
     */

    public DropTarget getDropTarget() { return dropTarget; }

    /**
     * 返回与此 <code>DropTargetContext</code> 关联的 <code>Component</code>。
     * <P>
     * @return 与此上下文关联的 Component
     */

    public Component getComponent() { return dropTarget.getComponent(); }

    /**
     * 当与 <code>DropTargetContextPeer</code> 关联时调用。
     * <P>
     * @param dtcp <code>DropTargetContextPeer</code>
     */

    public void addNotify(DropTargetContextPeer dtcp) {
        dropTargetContextPeer = dtcp;
    }

    /**
     * 当与 <code>DropTargetContextPeer</code> 解除关联时调用。
     */

    public void removeNotify() {
        dropTargetContextPeer = null;
        transferable          = null;
    }

    /**
     * 设置此 <code>DropTarget</code> 当前可接受的操作。
     * <P>
     * @param actions 一个表示支持的操作的 <code>int</code>
     */

    protected void setTargetActions(int actions) {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        if (peer != null) {
            synchronized (peer) {
                peer.setTargetActions(actions);
                getDropTarget().doSetDefaultActions(actions);
            }
        } else {
            getDropTarget().doSetDefaultActions(actions);
        }
    }

    /**
     * 返回一个表示此 <code>DropTarget</code> 当前可接受的操作的 <code>int</code>。
     * <P>
     * @return 此 <code>DropTarget</code> 当前可接受的操作
     */

    protected int getTargetActions() {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        return ((peer != null)
                        ? peer.getTargetActions()
                        : dropTarget.getDefaultActions()
        );
    }

    /**
     * 信号指示拖放操作已完成，以及是否成功。
     * <P>
     * @param success 成功为 true，否则为 false
     * <P>
     * @throws InvalidDnDOperationException 如果没有正在进行的拖放操作
     */

    public void dropComplete(boolean success) throws InvalidDnDOperationException{
        DropTargetContextPeer peer = getDropTargetContextPeer();
        if (peer != null) {
            peer.dropComplete(success);
        }
    }

    /**
     * 接受拖动。
     * <P>
     * @param dragOperation 支持的操作
     */

    protected void acceptDrag(int dragOperation) {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        if (peer != null) {
            peer.acceptDrag(dragOperation);
        }
    }

    /**
     * 拒绝拖动。
     */

    protected void rejectDrag() {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        if (peer != null) {
            peer.rejectDrag();
        }
    }

    /**
     * 信号指示使用指定的操作进行的拖放是可以接受的。
     * 必须在 DropTargetListener.drop 方法调用期间调用。
     * <P>
     * @param dropOperation 支持的操作
     */

    protected void acceptDrop(int dropOperation) {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        if (peer != null) {
            peer.acceptDrop(dropOperation);
        }
    }

    /**
     * 信号指示拖放是不可接受的。
     * 必须在 DropTargetListener.drop 方法调用期间调用。
     */

    protected void rejectDrop() {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        if (peer != null) {
            peer.rejectDrop();
        }
    }

    /**
     * 获取此操作的 <code>Transferable</code> 操作数的可用 DataFlavor。
     * <P>
     * @return 包含 <code>Transferable</code> 操作数支持的 <code>DataFlavor</code> 的 <code>DataFlavor[]</code>
     */

    protected DataFlavor[] getCurrentDataFlavors() {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        return peer != null ? peer.getTransferDataFlavors() : new DataFlavor[0];
    }

    /**
     * 返回此 <code>Transferable</code> 操作数当前可用的 DataFlavor
     * 作为 <code>java.util.List</code>。
     * <P>
     * @return 当前可用的 DataFlavor 作为 <code>java.util.List</code>
     */

    protected List<DataFlavor> getCurrentDataFlavorsAsList() {
        return Arrays.asList(getCurrentDataFlavors());
    }

    /**
     * 返回一个 <code>boolean</code>，指示给定的 <code>DataFlavor</code>
     * 是否由此 <code>DropTargetContext</code> 支持。
     * <P>
     * @param df <code>DataFlavor</code>
     * <P>
     * @return 指定的 <code>DataFlavor</code> 是否支持
     */

    protected boolean isDataFlavorSupported(DataFlavor df) {
        return getCurrentDataFlavorsAsList().contains(df);
    }

    /**
     * 获取此操作的 <code>Transferable</code>（代理）操作数
     * <P>
     * @throws InvalidDnDOperationException 如果没有正在进行的拖放操作
     * <P>
     * @return <code>Transferable</code>
     */

    protected Transferable getTransferable() throws InvalidDnDOperationException {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        if (peer == null) {
            throw new InvalidDnDOperationException();
        } else {
            if (transferable == null) {
                Transferable t = peer.getTransferable();
                boolean isLocal = peer.isTransferableJVMLocal();
                synchronized (this) {
                    if (transferable == null) {
                        transferable = createTransferableProxy(t, isLocal);
                    }
                }
            }

            return transferable;
        }
    }

    /**
     * 获取 <code>DropTargetContextPeer</code>
     * <P>
     * @return 平台对等体
     */

    DropTargetContextPeer getDropTargetContextPeer() {
        return dropTargetContextPeer;
    }

    /**
     * 创建一个代理，代理指定的 <code>Transferable</code>。
     *
     * @param t 要代理的 <tt>Transferable</tt>
     * @param local <tt>true</tt> 如果 <tt>t</tt> 表示本地拖放操作的结果。
     * @return 新的 <tt>TransferableProxy</tt> 实例。
     */
    protected Transferable createTransferableProxy(Transferable t, boolean local) {
        return new TransferableProxy(t, local);
    }

/****************************************************************************/


    /**
     * <code>TransferableProxy</code> 是一个辅助内部类，实现了
     * <code>Transferable</code> 接口，作为另一个 <code>Transferable</code> 对象的代理，
     * 该对象表示特定拖放操作的数据传输。
     * <p>
     * 代理将所有请求转发给封装的传输对象，并在本地传输时自动对封装的传输对象返回的数据进行额外的转换。
     */

    protected class TransferableProxy implements Transferable {

        /**
         * 构造一个 <code>TransferableProxy</code>，给定一个表示特定拖放操作数据传输的
         * <code>Transferable</code> 对象和一个表示拖放操作是否为本地（在同一 JVM 内）的 <code>boolean</code>。
         * <p>
         * @param t <code>Transferable</code> 对象
         * @param local <code>true</code>，如果 <code>t</code> 表示本地拖放操作的结果
         */
        TransferableProxy(Transferable t, boolean local) {
            proxy = new sun.awt.datatransfer.TransferableProxy(t, local);
            transferable = t;
            isLocal      = local;
        }

        /**
         * 返回一个 DataFlavor 对象数组，表示封装的传输对象可以提供的数据格式。
         * <p>
         * @return 封装的传输对象可以提供的数据格式的数组
         */
        public DataFlavor[] getTransferDataFlavors() {
            return proxy.getTransferDataFlavors();
        }

        /**
         * 返回指定的数据格式是否由封装的传输对象支持。
         * @param flavor 请求的数据格式
         * @return <code>true</code> 如果支持数据格式，<code>false</code> 否则
         */
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return proxy.isDataFlavorSupported(flavor);
        }

        /**
         * 返回一个表示封装的传输对象为请求的数据格式提供的数据的对象。
         * <p>
         * 在本地传输的情况下，当数据以 application/x-java-serialized-object
         * 数据格式请求时，提供封装的传输对象返回的对象的序列化副本。
         *
         * @param df 请求的数据格式
         * @throws IOException 如果请求的数据格式不再可用
         * @throws UnsupportedFlavorException 如果请求的数据格式不受支持
         */
        public Object getTransferData(DataFlavor df)
            throws UnsupportedFlavorException, IOException
        {
            return proxy.getTransferData(df);
        }

        /*
         * 字段
         */

        // 我们不需要担心客户端代码更改这些变量的值。由于 TransferableProxy 是一个受保护的类，只有
        // DropTargetContext 的子类可以访问它。而 DropTargetContext 不能被客户端代码子类化，因为它没有
        // 公共构造函数。

        /**
         * 封装的 <code>Transferable</code> 对象。
         */
        protected Transferable  transferable;

        /**
         * 一个 <code>boolean</code>，表示封装的 <code>Transferable</code> 对象是否表示
         * 本地拖放操作（在同一 JVM 内）的结果。
         */
        protected boolean       isLocal;

        private sun.awt.datatransfer.TransferableProxy proxy;
    }

/****************************************************************************/

    /*
     * 字段
     */

    /**
     * 与此 DropTargetContext 关联的 DropTarget。
     *
     * @serial
     */
    private DropTarget dropTarget;

    private transient DropTargetContextPeer dropTargetContextPeer;

    private transient Transferable transferable;
}
