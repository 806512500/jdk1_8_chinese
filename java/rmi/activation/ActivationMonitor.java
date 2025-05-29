/*
 * 版权所有 (c) 1997, 2005, Oracle 和/或其附属公司。保留所有权利。
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

package java.rmi.activation;

import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.activation.UnknownGroupException;
import java.rmi.activation.UnknownObjectException;

/**
 * 一个 <code>ActivationMonitor</code> 是特定于一个 <code>ActivationGroup</code> 的，并在组通过调用
 * <code>ActivationSystem.activeGroup</code> 被报告为活动时获得（这是内部完成的）。激活组负责通知其
 * <code>ActivationMonitor</code>，当其对象变为活动或非活动状态，或者整个组变为非活动状态时。
 *
 * @author      Ann Wollrath
 * @see         Activator
 * @see         ActivationSystem
 * @see         ActivationGroup
 * @since       1.2
 */
public interface ActivationMonitor extends Remote {

   /**
     * 当组中的对象变为非活动状态（去激活）时，激活组会调用其监视器的
     * <code>inactiveObject</code> 方法。激活组通过调用激活组的
     * <code>inactiveObject</code> 方法发现其虚拟机中的某个对象（它参与激活的对象）不再处于活动状态。 <p>
     *
     * <code>inactiveObject</code> 调用通知 <code>ActivationMonitor</code>，它持有的具有激活标识符
     * <code>id</code> 的远程对象引用不再有效。监视器认为与 <code>id</code> 关联的引用是一个过期引用。
     * 由于引用被认为是过期的，因此对同一激活标识符的后续 <code>activate</code> 调用将导致重新激活远程对象。<p>
     *
     * @param id 对象的激活标识符
     * @exception UnknownObjectException 如果对象未知
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public void inactiveObject(ActivationID id)
        throws UnknownObjectException, RemoteException;

    /**
     * 通知对象现在处于活动状态。当组中的对象通过直接激活以外的其他方式变为活动状态时，<code>ActivationGroup</code>
     * 会通知其监视器。
     *
     * @param id 活动对象的 id
     * @param obj 对象存根的序列化形式
     * @exception UnknownObjectException 如果对象未知
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public void activeObject(ActivationID id,
                             MarshalledObject<? extends Remote> obj)
        throws UnknownObjectException, RemoteException;

    /**
     * 通知组现在处于非活动状态。组将在随后请求激活组内的对象时被重新创建。当组中的所有对象报告它们是非活动状态时，组变为非活动状态。
     *
     * @param id 组的 id
     * @param incarnation 组的化身编号
     * @exception UnknownGroupException 如果组未知
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public void inactiveGroup(ActivationGroupID id,
                              long incarnation)
        throws UnknownGroupException, RemoteException;

}
