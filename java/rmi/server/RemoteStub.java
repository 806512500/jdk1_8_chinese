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
package java.rmi.server;

/**
 * {@code RemoteStub} 类是静态生成的客户端
 * 慕课的公共超类，提供了支持广泛远程引用语义的框架。慕课对象是代理，支持
 * 由远程对象的实际实现定义的完全相同的远程接口集。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 *
 * @deprecated 静态生成的慕课已弃用，因为
 * 慕课是动态生成的。有关动态慕课生成的信息，请参见 {@link UnicastRemoteObject}。
 */
@Deprecated
abstract public class RemoteStub extends RemoteObject {

    /** 表示与 JDK 1.1.x 版本的类的兼容性 */
    private static final long serialVersionUID = -1585587260594494182L;

    /**
     * 构造一个 {@code RemoteStub}。
     */
    protected RemoteStub() {
        super();
    }

    /**
     * 使用指定的远程引用构造一个 {@code RemoteStub}。
     *
     * @param ref 远程引用
     * @since JDK1.1
     */
    protected RemoteStub(RemoteRef ref) {
        super(ref);
    }

    /**
     * 抛出 {@link UnsupportedOperationException}。
     *
     * @param stub 远程慕课
     * @param ref 远程引用
     * @throws UnsupportedOperationException 始终抛出
     * @since JDK1.1
     * @deprecated 没有替代方法。{@code setRef} 方法
     * 用于设置远程慕课的远程引用。这是不必要的，因为可以通过使用
     * {@link #RemoteStub(RemoteRef)} 构造函数创建和初始化带有远程引用的 {@code RemoteStub}。
     */
    @Deprecated
    protected static void setRef(RemoteStub stub, RemoteRef ref) {
        throw new UnsupportedOperationException();
    }
}
