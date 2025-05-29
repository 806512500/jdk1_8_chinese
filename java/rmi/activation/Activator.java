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
import java.rmi.activation.UnknownObjectException;

/**
 * <code>Activator</code> 促进远程对象激活。一个“故障”远程引用调用激活器的
 * <code>activate</code> 方法以获得一个“活跃”的引用到一个“可激活”的远程对象。当接收到激活请求时，
 * 激活器查找激活标识符 <code>id</code> 的激活描述符，确定对象应在哪个组中激活，并通过组的
 * <code>ActivationInstantiator</code>（通过调用 <code>newInstance</code> 方法）启动对象的重新创建。激活器根据需要启动激活组的执行。例如，如果特定组标识符的激活组尚未执行，
 * 激活器将启动该组的虚拟机。 <p>
 *
 * <code>Activator</code> 与 <code>ActivationSystem</code> 密切合作，后者提供了在组内注册对象的方法，以及 <code>ActivationMonitor</code>，
 * 它接收有关活动和非活动对象及非活动组的信息。 <p>
 *
 * 激活器负责监控并检测激活组何时失败，以便它可以移除组和组内活动对象的陈旧远程引用。<p>
 *
 * @author      Ann Wollrath
 * @see         ActivationInstantiator
 * @see         ActivationGroupDesc
 * @see         ActivationGroupID
 * @since       1.2
 */
public interface Activator extends Remote {
    /**
     * 激活与激活标识符 <code>id</code> 关联的对象。如果激活器已经知道对象是活跃的，并且 <code>force</code> 为 false，
     * 则立即将具有“活跃”引用的存根返回给调用者；否则，如果激活器不知道对应的远程对象是否活跃，
     * 激活器将使用激活描述符信息（先前注册的）来确定对象应激活的组（虚拟机）。如果与
     * 对象组描述符相对应的 <code>ActivationInstantiator</code> 已经存在，激活器将调用激活组的
     * <code>newInstance</code> 方法，并传递对象的 id 和描述符。 <p>
     *
     * 如果对象组描述符的激活组尚不存在，激活器将启动一个 <code>ActivationInstantiator</code>（例如，通过生成一个子进程）。
     * 当激活器接收到激活组的回调（通过 <code>ActivationSystem</code> 的 <code>activeGroup</code> 方法）指定激活组的引用时，
     * 激活器可以调用该激活实例化器的 <code>newInstance</code> 方法，将每个待处理的激活请求转发到激活组，并将结果（一个序列化的远程对象引用，即存根）返回给调用者。<p>
     *
     * 注意，激活器接收的是一个“序列化”的对象而不是一个 Remote 对象，这样激活器就不需要加载该对象的代码，也不需要参与该对象的分布式垃圾回收。如果激活器保留了远程对象的强引用，
     * 激活器将阻止该对象在正常的分布式垃圾回收机制下被回收。 <p>
     *
     * @param id 要激活的对象的激活标识符
     * @param force 如果为 true，激活器联系组以获取远程对象的引用；如果为 false，允许返回缓存的值。
     * @return 序列化形式的远程对象（存根）
     * @exception ActivationException 如果对象激活失败
     * @exception UnknownObjectException 如果对象未知（未注册）
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public MarshalledObject<? extends Remote> activate(ActivationID id,
                                                       boolean force)
        throws ActivationException, UnknownObjectException, RemoteException;

}
