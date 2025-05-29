
/*
 * 版权所有 (c) 1996, 2001, Oracle 和/或其附属公司。保留所有权利。
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
package java.rmi.registry;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * <code>Registry</code> 是一个简单的远程对象注册表的远程接口，提供了存储和检索与任意字符串名称绑定的远程对象引用的方法。 
 * <code>bind</code>、<code>unbind</code> 和 <code>rebind</code>
 * 方法用于更改注册表中的名称绑定，而 <code>lookup</code> 和 <code>list</code> 方法用于查询当前的名称绑定。
 *
 * <p>在典型使用中，<code>Registry</code> 使 RMI 客户端能够启动：它提供了一种简单的方法，使客户端能够获得对远程对象的初始引用。
 * 因此，注册表的远程对象实现通常使用一个众所周知的地址导出，例如使用众所周知的 {@link
 * java.rmi.server.ObjID#REGISTRY_ID ObjID} 和 TCP 端口号
 * （默认为 {@link #REGISTRY_PORT 1099}）。
 *
 * <p>{@link LocateRegistry} 类提供了用于构建对位于远程地址的 <code>Registry</code> 的引导引用的编程 API
 * （参见静态 <code>getRegistry</code> 方法）以及在当前 VM 上创建和导出 <code>Registry</code> 的静态方法
 * （参见静态 <code>createRegistry</code> 方法）。
 *
 * <p><code>Registry</code> 实现可以选择限制对其某些或所有方法的访问（例如，更改注册表绑定的方法可能限制来自本地主机的调用）。
 * 如果 <code>Registry</code> 方法选择拒绝给定调用的访问，其实现可能会抛出 {@link java.rmi.AccessException}，
 * 由于它扩展了 {@link java.rmi.RemoteException}，因此当被远程客户端捕获时，它将被包装在 {@link java.rmi.ServerException} 中。
 *
 * <p>在 <code>Registry</code> 中用于绑定的名称是纯字符串，不进行解析。将远程引用存储在 <code>Registry</code> 中的服务可能希望使用包名作为名称绑定的前缀，
 * 以减少注册表中名称冲突的可能性。
 *
 * @author      Ann Wollrath
 * @author      Peter Jones
 * @since       JDK1.1
 * @see         LocateRegistry
 */
public interface Registry extends Remote {

    /** 注册表的知名端口。 */
    public static final int REGISTRY_PORT = 1099;

    /**
     * 返回在该注册表中与指定 <code>name</code> 绑定的远程引用。
     *
     * @param   name 要查找的远程引用的名称
     *
     * @return  对远程对象的引用
     *
     * @throws  NotBoundException 如果 <code>name</code> 当前未绑定
     *
     * @throws  RemoteException 如果与注册表的远程通信失败；如果异常是 <code>ServerException</code>
     * 包含 <code>AccessException</code>，则注册表拒绝调用者执行此操作
     *
     * @throws  AccessException 如果此注册表是本地的并且拒绝调用者执行此操作
     *
     * @throws  NullPointerException 如果 <code>name</code> 为 <code>null</code>
     */
    public Remote lookup(String name)
        throws RemoteException, NotBoundException, AccessException;

    /**
     * 在该注册表中将远程引用绑定到指定的 <code>name</code>。
     *
     * @param   name 要与远程引用关联的名称
     * @param   obj 对远程对象的引用（通常是存根）
     *
     * @throws  AlreadyBoundException 如果 <code>name</code> 已经绑定
     *
     * @throws  RemoteException 如果与注册表的远程通信失败；如果异常是 <code>ServerException</code>
     * 包含 <code>AccessException</code>，则注册表拒绝调用者执行此操作（如果调用来自非本地主机，例如）
     *
     * @throws  AccessException 如果此注册表是本地的并且拒绝调用者执行此操作
     *
     * @throws  NullPointerException 如果 <code>name</code> 为 <code>null</code>，或 <code>obj</code> 为 <code>null</code>
     */
    public void bind(String name, Remote obj)
        throws RemoteException, AlreadyBoundException, AccessException;

    /**
     * 从该注册表中移除指定 <code>name</code> 的绑定。
     *
     * @param   name 要移除的绑定的名称
     *
     * @throws  NotBoundException 如果 <code>name</code> 当前未绑定
     *
     * @throws  RemoteException 如果与注册表的远程通信失败；如果异常是 <code>ServerException</code>
     * 包含 <code>AccessException</code>，则注册表拒绝调用者执行此操作（如果调用来自非本地主机，例如）
     *
     * @throws  AccessException 如果此注册表是本地的并且拒绝调用者执行此操作
     *
     * @throws  NullPointerException 如果 <code>name</code> 为 <code>null</code>
     */
    public void unbind(String name)
        throws RemoteException, NotBoundException, AccessException;

    /**
     * 用提供的远程引用替换该注册表中指定 <code>name</code> 的绑定。如果指定的 <code>name</code> 已有现有绑定，则丢弃现有绑定。
     *
     * @param   name 要与远程引用关联的名称
     * @param   obj 对远程对象的引用（通常是存根）
     *
     * @throws  RemoteException 如果与注册表的远程通信失败；如果异常是 <code>ServerException</code>
     * 包含 <code>AccessException</code>，则注册表拒绝调用者执行此操作（如果调用来自非本地主机，例如）
     *
     * @throws  AccessException 如果此注册表是本地的并且拒绝调用者执行此操作
     *
     * @throws  NullPointerException 如果 <code>name</code> 为 <code>null</code>，或 <code>obj</code> 为 <code>null</code>
     */
    public void rebind(String name, Remote obj)
        throws RemoteException, AccessException;

                /**
     * 返回在此注册表中绑定的名称数组。该数组将包含在此方法调用时此注册表中绑定的名称的快照。
     *
     * @return  一个包含在此注册表中绑定的名称的数组
     *
     * @throws  RemoteException 如果与注册表的远程通信失败；如果异常是一个 <code>ServerException</code>
     * 包含一个 <code>AccessException</code>，则注册表拒绝调用者执行此操作的权限
     *
     * @throws  AccessException 如果此注册表是本地的，并且它拒绝调用者执行此操作的权限
     */
    public String[] list() throws RemoteException, AccessException;
}
