/*
 * 版权所有 (c) 2007, 2011, Oracle 和/或其附属公司。保留所有权利。
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

package java.nio.file;

import java.io.IOException;

/**
 * 可以注册到监视服务的对象，以便监视其更改和事件。
 *
 * <p> 该接口定义了 {@link #register register} 方法，用于将对象注册到 {@link WatchService}，并返回一个 {@link WatchKey} 以表示注册。一个对象可以注册到多个监视服务。通过调用键的 {@link WatchKey#cancel cancel} 方法可以取消与监视服务的注册。
 *
 * @since 1.7
 *
 * @see Path#register
 */

public interface Watchable {

    /**
     * 将对象注册到监视服务。
     *
     * <p> 如果此对象标识的文件系统对象当前已注册到监视服务，则在更改事件集或修改器以匹配 {@code events} 和 {@code modifiers} 参数指定的内容后，返回表示该注册的监视键。更改事件集不会导致对象的待处理事件被丢弃。对象会自动注册 {@link
     * StandardWatchEventKinds#OVERFLOW OVERFLOW} 事件。此事件不需要出现在事件数组中。
     *
     * <p> 否则，如果文件系统对象尚未注册到给定的监视服务，则进行注册并返回新的键。
     *
     * <p> 该接口的实现应指定它们支持的事件。
     *
     * @param   watcher
     *          要注册此对象的监视服务
     * @param   events
     *          要注册此对象的事件
     * @param   modifiers
     *          修改对象注册方式的修改器（如果有）
     *
     * @return  一个表示此对象与给定监视服务注册的键
     *
     * @throws  UnsupportedOperationException
     *          如果指定了不支持的事件或修改器
     * @throws  IllegalArgumentException
     *          如果指定了无效的事件或修改器组合
     * @throws  ClosedWatchServiceException
     *          如果监视服务已关闭
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          如果安装了安全经理并且它拒绝了监视此对象所需的未指定权限。该接口的实现应指定权限检查。
     */
    WatchKey register(WatchService watcher,
                      WatchEvent.Kind<?>[] events,
                      WatchEvent.Modifier... modifiers)
        throws IOException;


    /**
     * 将对象注册到监视服务。
     *
     * <p> 调用此方法的行为与调用
     * <pre>
     *     watchable.{@link #register(WatchService,WatchEvent.Kind[],WatchEvent.Modifier[]) register}(watcher, events, new WatchEvent.Modifier[0]);
     * </pre>
     * 完全相同。
     *
     * @param   watcher
     *          要注册此对象的监视服务
     * @param   events
     *          要注册此对象的事件
     *
     * @return  一个表示此对象与给定监视服务注册的键
     *
     * @throws  UnsupportedOperationException
     *          如果指定了不支持的事件
     * @throws  IllegalArgumentException
     *          如果指定了无效的事件组合
     * @throws  ClosedWatchServiceException
     *          如果监视服务已关闭
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          如果安装了安全经理并且它拒绝了监视此对象所需的未指定权限。该接口的实现应指定权限检查。
     */
    WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events)
        throws IOException;
}
