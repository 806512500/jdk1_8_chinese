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

/**
 * <code>ActivationInstantiator</code> 负责创建“可激活”对象的实例。一个具体的 <code>ActivationGroup</code> 子类实现
 * <code>newInstance</code> 方法以处理在组内创建对象。
 *
 * @author      Ann Wollrath
 * @see         ActivationGroup
 * @since       1.2
 */
public interface ActivationInstantiator extends Remote {

   /**
    * 激活器调用实例化器的 <code>newInstance</code> 方法，以便在该组中重新创建具有激活标识符 <code>id</code> 和描述符
    * <code>desc</code> 的对象。实例化器负责以下任务：<ul>
    *
    * <li> 使用描述符的 <code>getClassName</code> 方法确定对象的类，
    *
    * <li> 从描述符中获取的代码位置（使用 <code>getLocation</code> 方法）加载类，
    *
    * <li> 通过调用对象类的特殊“激活”构造函数来创建类的实例，该构造函数接受两个参数：对象的 <code>ActivationID</code> 和
    * <code>MarshalledObject</code>，其中包含对象特定的初始化数据，
    *
    * <li> 返回一个包含创建的远程对象存根的 MarshalledObject </ul>
    *
    * @param id 对象的激活标识符
    * @param desc 对象的描述符
    * @return 包含远程对象存根的序列化表示的 marshalled 对象
    * @exception ActivationException 如果对象激活失败
    * @exception RemoteException 如果远程调用失败
    * @since 1.2
    */
    public MarshalledObject<? extends Remote> newInstance(ActivationID id,
                                                          ActivationDesc desc)
        throws ActivationException, RemoteException;
}
