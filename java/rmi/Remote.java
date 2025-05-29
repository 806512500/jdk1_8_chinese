/*
 * 版权所有 (c) 1996, 2006, Oracle 和/或其附属公司。保留所有权利。
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

package java.rmi;

/**
 * <code>Remote</code> 接口用于标识其方法可以从非本地虚拟机调用的接口。任何远程对象必须直接或间接实现此接口。
 * 只有在“远程接口”中指定的方法，即扩展 <code>java.rmi.Remote</code> 的接口中的方法，才能远程调用。
 *
 * <p>实现类可以实现任意数量的远程接口，并可以扩展其他远程实现类。RMI 提供了一些方便类，远程对象实现可以扩展这些类，以简化远程对象的创建。这些类是
 * <code>java.rmi.server.UnicastRemoteObject</code> 和
 * <code>java.rmi.activation.Activatable</code>。
 *
 * <p>有关 RMI 的完整详细信息，请参阅 <a
 href=../../../platform/rmi/spec/rmiTOC.html>RMI 规范</a>，该规范描述了 RMI API 和系统。
 *
 * @since   JDK1.1
 * @author  Ann Wollrath
 * @see     java.rmi.server.UnicastRemoteObject
 * @see     java.rmi.activation.Activatable
 */
public interface Remote {}
