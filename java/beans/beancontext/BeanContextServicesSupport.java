
/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.beans.beancontext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.TooManyListenersException;

import java.util.Locale;

/**
 * <p>
 * 该辅助类提供了 java.beans.beancontext.BeanContextServices 接口的实用实现。
 * </p>
 * <p>
 * 由于该类直接实现了 BeanContextServices 接口，因此可以通过继承此实现或通过从另一个对象委托此类的实例来使用此类，通过 BeanContextProxy 接口。
 * </p>
 *
 * @author Laurence P. G. Cable
 * @since 1.2
 */

public class BeanContextServicesSupport extends BeanContextSupport
       implements BeanContextServices {
    private static final long serialVersionUID = -8494482757288719206L;

    /**
     * <p>
     * 构造一个 BeanContextServicesSupport 实例
     * </p>
     *
     * @param peer      我们为其提供实现的对等 BeanContext，如果为 null，则此对象是其自己的对等对象
     * @param lcle      此 BeanContext 的当前区域设置。
     * @param dTime     初始状态，如果处于设计模式则为 true，如果处于运行时则为 false。
     * @param visible   初始可见性。
     *
     */

    public BeanContextServicesSupport(BeanContextServices peer, Locale lcle, boolean dTime, boolean visible) {
        super(peer, lcle, dTime, visible);
    }

    /**
     * 使用指定的区域设置和设计模式创建一个实例。
     *
     * @param peer      我们为其提供实现的对等 BeanContext，如果为 null，则此对象是其自己的对等对象
     * @param lcle      此 BeanContext 的当前区域设置。
     * @param dtime     初始状态，如果处于设计模式则为 true，如果处于运行时则为 false。
     */

    public BeanContextServicesSupport(BeanContextServices peer, Locale lcle, boolean dtime) {
        this (peer, lcle, dtime, true);
    }

    /**
     * 使用指定的区域设置创建一个实例。
     *
     * @param peer      我们为其提供实现的对等 BeanContext，如果为 null，则此对象是其自己的对等对象
     * @param lcle      此 BeanContext 的当前区域设置。
     */

    public BeanContextServicesSupport(BeanContextServices peer, Locale lcle) {
        this (peer, lcle, false, true);
    }

    /**
     * 使用一个对等对象创建一个实例。
     *
     * @param peer      我们为其提供实现的对等 BeanContext，如果为 null，则此对象是其自己的对等对象
     */

    public BeanContextServicesSupport(BeanContextServices peer) {
        this (peer, null, false, true);
    }

    /**
     * 创建一个不是其他对象的代理的实例。
     */

    public BeanContextServicesSupport() {
        this (null, null, false, true);
    }

    /**
     * 由 BeanContextSupport 超类在构造和反序列化期间调用，以初始化子类的瞬态状态。
     *
     * 子类可以封装此方法，但不应覆盖它或直接调用它。
     */

    public void initialize() {
        super.initialize();

        services     = new HashMap(serializable + 1);
        bcsListeners = new ArrayList(1);
    }

    /**
     * 获取与此 BeanContextServicesSupport 关联的 BeanContextServices。
     *
     * @return 此对象为其提供实现的 BeanContext 实例。
     */
    public BeanContextServices getBeanContextServicesPeer() {
        return (BeanContextServices)getBeanContextChildPeer();
    }

    /************************************************************************/

    /*
     * 保护的嵌套类，包含每个子对象的信息，每个子对象在 "children" 哈希表中都有一个此类的实例。
     * 子类可以扩展此类以包含它们自己的每个子对象的状态。
     *
     * 注意，当 BeanContextSupport 被序列化时，此 'value' 会与相应的子对象 'key' 一起序列化。
     */

    protected class BCSSChild extends BeanContextSupport.BCSChild  {

        private static final long serialVersionUID = -3263851306889194873L;

        /*
         * 私有的嵌套类，用于将服务类映射到提供者和请求者的监听器。
         */

        class BCSSCServiceClassRef {

            // 创建一个服务引用的实例

            BCSSCServiceClassRef(Class sc, BeanContextServiceProvider bcsp, boolean delegated) {
                super();

                serviceClass     = sc;

                if (delegated)
                    delegateProvider = bcsp;
                else
                    serviceProvider  = bcsp;
            }

            // 添加一个请求者及其关联的监听器

            void addRequestor(Object requestor, BeanContextServiceRevokedListener bcsrl) throws TooManyListenersException {
                BeanContextServiceRevokedListener cbcsrl = (BeanContextServiceRevokedListener)requestors.get(requestor);

                if (cbcsrl != null && !cbcsrl.equals(bcsrl))
                    throw new TooManyListenersException();

                requestors.put(requestor, bcsrl);
            }

            // 移除一个请求者

            void removeRequestor(Object requestor) {
                requestors.remove(requestor);
            }

            // 检查请求者的监听器

            void verifyRequestor(Object requestor, BeanContextServiceRevokedListener bcsrl) throws TooManyListenersException {
                BeanContextServiceRevokedListener cbcsrl = (BeanContextServiceRevokedListener)requestors.get(requestor);

                if (cbcsrl != null && !cbcsrl.equals(bcsrl))
                    throw new TooManyListenersException();
            }

            void verifyAndMaybeSetProvider(BeanContextServiceProvider bcsp, boolean isDelegated) {
                BeanContextServiceProvider current;

                if (isDelegated) { // 提供者是委托的
                    current = delegateProvider;

                    if (current == null || bcsp == null) {
                        delegateProvider = bcsp;
                        return;
                    }
                } else { // 提供者注册到此 BCS
                    current = serviceProvider;

                    if (current == null || bcsp == null) {
                        serviceProvider = bcsp;
                        return;
                    }
                }

                if (!current.equals(bcsp))
                    throw new UnsupportedOperationException("来自不同 BeanContextServiceProvider 的现有服务引用不受支持");

            }

            Iterator cloneOfEntries() {
                return ((HashMap)requestors.clone()).entrySet().iterator();
            }

            Iterator entries() { return requestors.entrySet().iterator(); }

            boolean isEmpty() { return requestors.isEmpty(); }

            Class getServiceClass() { return serviceClass; }

            BeanContextServiceProvider getServiceProvider() {
                return serviceProvider;
            }

            BeanContextServiceProvider getDelegateProvider() {
                return delegateProvider;
            }

            boolean isDelegated() { return delegateProvider != null; }

            void addRef(boolean delegated) {
                if (delegated) {
                    delegateRefs++;
                } else {
                    serviceRefs++;
                }
            }


            void releaseRef(boolean delegated) {
                if (delegated) {
                    if (--delegateRefs == 0) {
                        delegateProvider = null;
                    }
                } else {
                    if (--serviceRefs  <= 0) {
                        serviceProvider = null;
                    }
                }
            }

            int getRefs() { return serviceRefs + delegateRefs; }

            int getDelegateRefs() { return delegateRefs; }

            int getServiceRefs() { return serviceRefs; }

            /*
             * 字段
             */

            Class                               serviceClass;

            BeanContextServiceProvider          serviceProvider;
            int                                 serviceRefs;

            BeanContextServiceProvider          delegateProvider; // 代理
            int                                 delegateRefs;

            HashMap                             requestors = new HashMap(1);
        }

        /*
         * 每个服务引用的信息 ...
         */

        class BCSSCServiceRef {
            BCSSCServiceRef(BCSSCServiceClassRef scref, boolean isDelegated) {
                serviceClassRef = scref;
                delegated       = isDelegated;
            }

            void addRef()  { refCnt++;        }
            int  release() { return --refCnt; }

            BCSSCServiceClassRef getServiceClassRef() { return serviceClassRef; }

            boolean isDelegated() { return delegated; }

            /*
             * 字段
             */

            BCSSCServiceClassRef serviceClassRef;
            int                  refCnt    = 1;
            boolean              delegated = false;
        }

        BCSSChild(Object bcc, Object peer) { super(bcc, peer); }

        // 注意每个请求者的服务使用情况，每个服务

        synchronized void usingService(Object requestor, Object service, Class serviceClass, BeanContextServiceProvider bcsp, boolean isDelegated, BeanContextServiceRevokedListener bcsrl)  throws TooManyListenersException, UnsupportedOperationException {

            // 首先，处理从服务类到请求者的映射

            BCSSCServiceClassRef serviceClassRef = null;

            if (serviceClasses == null)
                serviceClasses = new HashMap(1);
            else
                serviceClassRef = (BCSSCServiceClassRef)serviceClasses.get(serviceClass);

            if (serviceClassRef == null) { // 新的服务正在使用 ...
                serviceClassRef = new BCSSCServiceClassRef(serviceClass, bcsp, isDelegated);
                serviceClasses.put(serviceClass, serviceClassRef);

            } else { // 现有的服务 ...
                serviceClassRef.verifyAndMaybeSetProvider(bcsp, isDelegated); // 抛出异常
                serviceClassRef.verifyRequestor(requestor, bcsrl); // 抛出异常
            }

            serviceClassRef.addRequestor(requestor, bcsrl);
            serviceClassRef.addRef(isDelegated);

            // 现在处理从请求者到服务的映射

            BCSSCServiceRef serviceRef = null;
            Map             services   = null;

            if (serviceRequestors == null) {
                serviceRequestors = new HashMap(1);
            } else {
                services = (Map)serviceRequestors.get(requestor);
            }

            if (services == null) {
                services = new HashMap(1);

                serviceRequestors.put(requestor, services);
            } else
                serviceRef = (BCSSCServiceRef)services.get(service);

            if (serviceRef == null) {
                serviceRef = new BCSSCServiceRef(serviceClassRef, isDelegated);

                services.put(service, serviceRef);
            } else {
                serviceRef.addRef();
            }
        }

        // 释放服务引用

        synchronized void releaseService(Object requestor, Object service) {
            if (serviceRequestors == null) return;

            Map services = (Map)serviceRequestors.get(requestor);

            if (services == null) return; // 哦，它已经不在了！

            BCSSCServiceRef serviceRef = (BCSSCServiceRef)services.get(service);

            if (serviceRef == null) return; // 哦，它已经不在了！

            BCSSCServiceClassRef serviceClassRef = serviceRef.getServiceClassRef();
            boolean                    isDelegated = serviceRef.isDelegated();
            BeanContextServiceProvider bcsp        = isDelegated ? serviceClassRef.getDelegateProvider() : serviceClassRef.getServiceProvider();

            bcsp.releaseService(BeanContextServicesSupport.this.getBeanContextServicesPeer(), requestor, service);

            serviceClassRef.releaseRef(isDelegated);
            serviceClassRef.removeRequestor(requestor);

            if (serviceRef.release() == 0) {

                services.remove(service);

                if (services.isEmpty()) {
                    serviceRequestors.remove(requestor);
                    serviceClassRef.removeRequestor(requestor);
                }

                if (serviceRequestors.isEmpty()) {
                    serviceRequestors = null;
                }

                if (serviceClassRef.isEmpty()) {
                    serviceClasses.remove(serviceClassRef.getServiceClass());
                }

                if (serviceClasses.isEmpty())
                    serviceClasses = null;
            }
        }

        // 撤销服务

        synchronized void revokeService(Class serviceClass, boolean isDelegated, boolean revokeNow) {
            if (serviceClasses == null) return;

            BCSSCServiceClassRef serviceClassRef = (BCSSCServiceClassRef)serviceClasses.get(serviceClass);

            if (serviceClassRef == null) return;

            Iterator i = serviceClassRef.cloneOfEntries();

            BeanContextServiceRevokedEvent bcsre       = new BeanContextServiceRevokedEvent(BeanContextServicesSupport.this.getBeanContextServicesPeer(), serviceClass, revokeNow);
            boolean                        noMoreRefs  = false;

            while (i.hasNext() && serviceRequestors != null) {
                Map.Entry                         entry    = (Map.Entry)i.next();
                BeanContextServiceRevokedListener listener = (BeanContextServiceRevokedListener)entry.getValue();


        if (revokeNow) {
                    Object  requestor = entry.getKey();
                    Map     services  = (Map)serviceRequestors.get(requestor);

                    if (services != null) {
                        Iterator i1 = services.entrySet().iterator();

                        while (i1.hasNext()) {
                            Map.Entry       tmp        = (Map.Entry)i1.next();

                            BCSSCServiceRef serviceRef = (BCSSCServiceRef)tmp.getValue();
                            if (serviceRef.getServiceClassRef().equals(serviceClassRef) && isDelegated == serviceRef.isDelegated()) {
                                i1.remove();
                            }
                        }

                        if (noMoreRefs = services.isEmpty()) {
                            serviceRequestors.remove(requestor);
                        }
                    }

                    if (noMoreRefs) serviceClassRef.removeRequestor(requestor);
                }

                listener.serviceRevoked(bcsre);
            }

            if (revokeNow && serviceClasses != null) {
                if (serviceClassRef.isEmpty())
                    serviceClasses.remove(serviceClass);

                if (serviceClasses.isEmpty())
                    serviceClasses = null;
            }

            if (serviceRequestors != null && serviceRequestors.isEmpty())
                serviceRequestors = null;
        }

        // 释放与此子上下文相关的所有引用，因为它已被取消嵌套。

        void cleanupReferences() {

            if (serviceRequestors == null) return;

            Iterator requestors = serviceRequestors.entrySet().iterator();

            while(requestors.hasNext()) {
                Map.Entry            tmp       = (Map.Entry)requestors.next();
                Object               requestor = tmp.getKey();
                Iterator             services  = ((Map)tmp.getValue()).entrySet().iterator();

                requestors.remove();

                while (services.hasNext()) {
                    Map.Entry       entry   = (Map.Entry)services.next();
                    Object          service = entry.getKey();
                    BCSSCServiceRef sref    = (BCSSCServiceRef)entry.getValue();

                    BCSSCServiceClassRef       scref = sref.getServiceClassRef();

                    BeanContextServiceProvider bcsp  = sref.isDelegated() ? scref.getDelegateProvider() : scref.getServiceProvider();

                    scref.removeRequestor(requestor);
                    services.remove();

                    while (sref.release() >= 0) {
                        bcsp.releaseService(BeanContextServicesSupport.this.getBeanContextServicesPeer(), requestor, service);
                    }
                }
            }

            serviceRequestors = null;
            serviceClasses    = null;
        }

        void revokeAllDelegatedServicesNow() {
            if (serviceClasses == null) return;

            Iterator serviceClassRefs  =
                new HashSet(serviceClasses.values()).iterator();

            while (serviceClassRefs.hasNext()) {
                BCSSCServiceClassRef serviceClassRef = (BCSSCServiceClassRef)serviceClassRefs.next();

                if (!serviceClassRef.isDelegated()) continue;

                Iterator i = serviceClassRef.cloneOfEntries();
                BeanContextServiceRevokedEvent bcsre       = new BeanContextServiceRevokedEvent(BeanContextServicesSupport.this.getBeanContextServicesPeer(), serviceClassRef.getServiceClass(), true);
                boolean                        noMoreRefs  = false;

                while (i.hasNext()) {
                    Map.Entry                         entry     = (Map.Entry)i.next();
                    BeanContextServiceRevokedListener listener  = (BeanContextServiceRevokedListener)entry.getValue();

                    Object                            requestor = entry.getKey();
                    Map                               services  = (Map)serviceRequestors.get(requestor);

                    if (services != null) {
                        Iterator i1 = services.entrySet().iterator();

                        while (i1.hasNext()) {
                            Map.Entry       tmp        = (Map.Entry)i1.next();

                            BCSSCServiceRef serviceRef = (BCSSCServiceRef)tmp.getValue();
                            if (serviceRef.getServiceClassRef().equals(serviceClassRef) && serviceRef.isDelegated()) {
                                i1.remove();
                            }
                        }

                        if (noMoreRefs = services.isEmpty()) {
                            serviceRequestors.remove(requestor);
                        }
                    }

                    if (noMoreRefs) serviceClassRef.removeRequestor(requestor);

                    listener.serviceRevoked(bcsre);

                    if (serviceClassRef.isEmpty())
                        serviceClasses.remove(serviceClassRef.getServiceClass());
                }
            }

            if (serviceClasses.isEmpty()) serviceClasses = null;

            if (serviceRequestors != null && serviceRequestors.isEmpty())
                serviceRequestors = null;
        }

        /*
         * 字段
         */

        private transient HashMap       serviceClasses;
        private transient HashMap       serviceRequestors;
    }

    /**
     * <p>
     * 子类可以重写此方法，以插入自己的子类
     * 而不必覆盖 add() 或其他将子元素添加到集合中的方法。
     * </p>
     *
     * @param targetChild 代表目标子元素创建子类
     * @param peer        如果目标子元素和代理由 BeanContextProxy 关联，则为代理
     */

    protected BCSChild createBCSChild(Object targetChild, Object peer) {
        return new BCSSChild(targetChild, peer);
    }

    /************************************************************************/

        /**
         * 子类可以扩展此嵌套类以添加每个 BeanContextServicesProvider 的行为。
         */

        protected static class BCSSServiceProvider implements Serializable {
            private static final long serialVersionUID = 861278251667444782L;

            BCSSServiceProvider(Class sc, BeanContextServiceProvider bcsp) {
                super();

                serviceProvider = bcsp;
            }

            /**
             * 返回服务提供者。
             * @return 服务提供者
             */
            protected BeanContextServiceProvider getServiceProvider() {
                return serviceProvider;
            }

            /**
             * 服务提供者。
             */

            protected BeanContextServiceProvider serviceProvider;
        }

        /**
         * 子类可以重写此方法以创建新的 BCSSServiceProvider 子类
         * 而不必覆盖 addService() 以实例化。
         * @param sc 服务类
         * @param bcsp 服务提供者
         * @return 服务提供者，无需覆盖 addService()
         */

        protected BCSSServiceProvider createBCSSServiceProvider(Class sc, BeanContextServiceProvider bcsp) {
            return new BCSSServiceProvider(sc, bcsp);
        }

    /************************************************************************/

    /**
     * 添加 BeanContextServicesListener
     *
     * @throws NullPointerException 如果参数为 null
     */

    public void addBeanContextServicesListener(BeanContextServicesListener bcsl) {
        if (bcsl == null) throw new NullPointerException("bcsl");

        synchronized(bcsListeners) {
            if (bcsListeners.contains(bcsl))
                return;
            else
                bcsListeners.add(bcsl);
        }
    }

    /**
     * 移除 BeanContextServicesListener
     */

    public void removeBeanContextServicesListener(BeanContextServicesListener bcsl) {
        if (bcsl == null) throw new NullPointerException("bcsl");

        synchronized(bcsListeners) {
            if (!bcsListeners.contains(bcsl))
                return;
            else
                bcsListeners.remove(bcsl);
        }
    }

    /**
     * 添加服务
     * @param serviceClass 服务类
     * @param bcsp 服务提供者
     */

    public boolean addService(Class serviceClass, BeanContextServiceProvider bcsp) {
        return addService(serviceClass, bcsp, true);
    }

    /**
     * 添加服务
     * @param serviceClass 服务类
     * @param bcsp 服务提供者
     * @param fireEvent 是否应触发事件
     * @return 如果服务成功添加，则返回 true
     */

    protected boolean addService(Class serviceClass, BeanContextServiceProvider bcsp, boolean fireEvent) {

        if (serviceClass == null) throw new NullPointerException("serviceClass");
        if (bcsp         == null) throw new NullPointerException("bcsp");

        synchronized(BeanContext.globalHierarchyLock) {
            if (services.containsKey(serviceClass))
                return false;
            else {
                services.put(serviceClass,  createBCSSServiceProvider(serviceClass, bcsp));

                if (bcsp instanceof Serializable) serializable++;

                if (!fireEvent) return true;


                BeanContextServiceAvailableEvent bcssae = new BeanContextServiceAvailableEvent(getBeanContextServicesPeer(), serviceClass);

                fireServiceAdded(bcssae);

                synchronized(children) {
                    Iterator i = children.keySet().iterator();

                    while (i.hasNext()) {
                        Object c = i.next();

                        if (c instanceof BeanContextServices) {
                            ((BeanContextServicesListener)c).serviceAvailable(bcssae);
                        }
                    }
                }

                return true;
            }
        }
    }

    /**
     * 移除服务
     * @param serviceClass 服务类
     * @param bcsp 服务提供者
     * @param revokeCurrentServicesNow 是否应撤销服务
     */

    public void revokeService(Class serviceClass, BeanContextServiceProvider bcsp, boolean revokeCurrentServicesNow) {

        if (serviceClass == null) throw new NullPointerException("serviceClass");
        if (bcsp         == null) throw new NullPointerException("bcsp");

        synchronized(BeanContext.globalHierarchyLock) {
            if (!services.containsKey(serviceClass)) return;

            BCSSServiceProvider bcsssp = (BCSSServiceProvider)services.get(serviceClass);

            if (!bcsssp.getServiceProvider().equals(bcsp))
                throw new IllegalArgumentException("服务提供者不匹配");

            services.remove(serviceClass);

            if (bcsp instanceof Serializable) serializable--;

            Iterator i = bcsChildren(); // 获取 BCSChild 值。

            while (i.hasNext()) {
                ((BCSSChild)i.next()).revokeService(serviceClass, false, revokeCurrentServicesNow);
            }

            fireServiceRevoked(serviceClass, revokeCurrentServicesNow);
        }
    }

    /**
     * 是否有服务，该服务可能是委托的
     */

    public synchronized boolean hasService(Class serviceClass) {
        if (serviceClass == null) throw new NullPointerException("serviceClass");

        synchronized(BeanContext.globalHierarchyLock) {
            if (services.containsKey(serviceClass)) return true;

            BeanContextServices bcs = null;

            try {
                bcs = (BeanContextServices)getBeanContext();
            } catch (ClassCastException cce) {
                return false;
            }

            return bcs == null ? false : bcs.hasService(serviceClass);
        }
    }

    /************************************************************************/

    /*
     * 用于表示委托给外部 BeanContext 的服务类的代理的嵌套子类。
     */

    protected class BCSSProxyServiceProvider implements BeanContextServiceProvider, BeanContextServiceRevokedListener {

        BCSSProxyServiceProvider(BeanContextServices bcs) {
            super();

            nestingCtxt = bcs;
        }

        public Object getService(BeanContextServices bcs, Object requestor, Class serviceClass, Object serviceSelector) {
            Object service = null;

            try {
                service = nestingCtxt.getService(bcs, requestor, serviceClass, serviceSelector, this);
            } catch (TooManyListenersException tmle) {
                return null;
            }

            return service;
        }

        public void releaseService(BeanContextServices bcs, Object requestor, Object service) {
            nestingCtxt.releaseService(bcs, requestor, service);
        }

        public Iterator getCurrentServiceSelectors(BeanContextServices bcs, Class serviceClass) {
            return nestingCtxt.getCurrentServiceSelectors(serviceClass);
        }

        public void serviceRevoked(BeanContextServiceRevokedEvent bcsre) {
            Iterator i = bcsChildren(); // 获取 BCSChild 值。

            while (i.hasNext()) {
                ((BCSSChild)i.next()).revokeService(bcsre.getServiceClass(), true, bcsre.isCurrentServiceInvalidNow());
            }
        }

        /*
         * 字段
         */

        private BeanContextServices nestingCtxt;
    }

    /************************************************************************/

    /**
     * 获取可能委托的服务
     */

     public Object getService(BeanContextChild child, Object requestor, Class serviceClass, Object serviceSelector, BeanContextServiceRevokedListener bcsrl) throws TooManyListenersException {
        if (child        == null) throw new NullPointerException("child");
        if (serviceClass == null) throw new NullPointerException("serviceClass");
        if (requestor    == null) throw new NullPointerException("requestor");
        if (bcsrl        == null) throw new NullPointerException("bcsrl");

        Object              service = null;
        BCSSChild           bcsc;
        BeanContextServices bcssp   = getBeanContextServicesPeer();

        synchronized(BeanContext.globalHierarchyLock) {
            synchronized(children) { bcsc = (BCSSChild)children.get(child); }

            if (bcsc == null) throw new IllegalArgumentException("不是此上下文的子元素"); // 不是子元素 ...

            BCSSServiceProvider bcsssp = (BCSSServiceProvider)services.get(serviceClass);

            if (bcsssp != null) {
                BeanContextServiceProvider bcsp = bcsssp.getServiceProvider();
                service = bcsp.getService(bcssp, requestor, serviceClass, serviceSelector);
                if (service != null) { // 进行簿记 ...
                    try {
                        bcsc.usingService(requestor, service, serviceClass, bcsp, false, bcsrl);
                    } catch (TooManyListenersException tmle) {
                        bcsp.releaseService(bcssp, requestor, service);
                        throw tmle;
                    } catch (UnsupportedOperationException uope) {
                        bcsp.releaseService(bcssp, requestor, service);
                        throw uope; // 未检查的运行时异常
                    }


    /**
     * 释放服务。
     */

    public void releaseService(BeanContextChild child, Object requestor, Object service) {
        if (child     == null) throw new NullPointerException("child");
        if (requestor == null) throw new NullPointerException("requestor");
        if (service   == null) throw new NullPointerException("service");

        BCSSChild bcsc;

        synchronized(BeanContext.globalHierarchyLock) {
                synchronized(children) { bcsc = (BCSSChild)children.get(child); }

                if (bcsc != null)
                    bcsc.releaseService(requestor, service);
                else
                   throw new IllegalArgumentException("child actual is not a child of this BeanContext");
        }
    }

    /**
     * @return 返回所有当前注册的服务类的迭代器。
     */

    public Iterator getCurrentServiceClasses() {
        return new BCSIterator(services.keySet().iterator());
    }

    /**
     * @return 返回指定服务的所有当前可用的服务选择器的迭代器（如果有）。
     */

    public Iterator getCurrentServiceSelectors(Class serviceClass) {

        BCSSServiceProvider bcsssp = (BCSSServiceProvider)services.get(serviceClass);

        return bcsssp != null ? new BCSIterator(bcsssp.getServiceProvider().getCurrentServiceSelectors(getBeanContextServicesPeer(), serviceClass)) : null;
    }

    /**
     * BeanContextServicesListener 回调，将事件传播到所有当前注册的监听器和 BeanContextServices 子项，
     * 如果此 BeanContextService 本身尚未实现该服务。
     *
     * 子类可以覆盖或封装此方法以实现自己的传播语义。
     */

     public void serviceAvailable(BeanContextServiceAvailableEvent bcssae) {
        synchronized(BeanContext.globalHierarchyLock) {
            if (services.containsKey(bcssae.getServiceClass())) return;

            fireServiceAdded(bcssae);

            Iterator i;

            synchronized(children) {
                i = children.keySet().iterator();
            }

            while (i.hasNext()) {
                Object c = i.next();

                if (c instanceof BeanContextServices) {
                    ((BeanContextServicesListener)c).serviceAvailable(bcssae);
                }
            }
        }
     }

    /**
     * BeanContextServicesListener 回调，将事件传播到所有当前注册的监听器和 BeanContextServices 子项，
     * 如果此 BeanContextService 本身尚未实现该服务。
     *
     * 子类可以覆盖或封装此方法以实现自己的传播语义。
     */

    public void serviceRevoked(BeanContextServiceRevokedEvent bcssre) {
        synchronized(BeanContext.globalHierarchyLock) {
            if (services.containsKey(bcssre.getServiceClass())) return;

            fireServiceRevoked(bcssre);

            Iterator i;

            synchronized(children) {
                i = children.keySet().iterator();
            }

            while (i.hasNext()) {
                Object c = i.next();

                if (c instanceof BeanContextServices) {
                    ((BeanContextServicesListener)c).serviceRevoked(bcssre);
                }
            }
        }
    }

    /**
     * 获取指定子项的 <tt>BeanContextServicesListener</tt>（如果有）。
     *
     * @param child 指定的子项
     * @return 指定子项的 BeanContextServicesListener（如果有）
     */
    protected static final BeanContextServicesListener getChildBeanContextServicesListener(Object child) {
        try {
            return (BeanContextServicesListener)child;
        } catch (ClassCastException cce) {
            return null;
        }
    }

    /**
     * 从父类的子项移除操作中调用，当子项成功移除后。调用时子项已同步。
     *
     * 该子类使用此钩子立即撤销此子项（如果它是 BeanContextChild）正在使用的所有服务。
     *
     * 子类可以封装此方法以实现自己的子项移除副作用。
     */

    protected void childJustRemovedHook(Object child, BCSChild bcsc) {
        BCSSChild bcssc = (BCSSChild)bcsc;

        bcssc.cleanupReferences();
    }

    /**
     * 从 setBeanContext 调用，通知 BeanContextChild 释放从嵌套 BeanContext 获取的资源。
     *
     * 该方法撤销从其父项获取的所有服务。
     *
     * 子类可以封装此方法以实现自己的语义。
     */

    protected synchronized void releaseBeanContextResources() {
        Object[] bcssc;

        super.releaseBeanContextResources();

        synchronized(children) {
            if (children.isEmpty()) return;

            bcssc = children.values().toArray();
        }


        for (int i = 0; i < bcssc.length; i++) {
            ((BCSSChild)bcssc[i]).revokeAllDelegatedServicesNow();
        }

        proxy = null;
    }

    /**
     * 从 setBeanContext 调用，通知 BeanContextChild 分配从嵌套 BeanContext 获取的资源。
     *
     * 子类可以封装此方法以实现自己的语义。
     */

    protected synchronized void initializeBeanContextResources() {
        super.initializeBeanContextResources();

        BeanContext nbc = getBeanContext();

        if (nbc == null) return;

        try {
            BeanContextServices bcs = (BeanContextServices)nbc;

            proxy = new BCSSProxyServiceProvider(bcs);
        } catch (ClassCastException cce) {
            // do nothing ...
        }
    }

    /**
     * 触发一个 <tt>BeanContextServiceEvent</tt> 通知新服务。
     * @param serviceClass 服务类
     */
    protected final void fireServiceAdded(Class serviceClass) {
        BeanContextServiceAvailableEvent bcssae = new BeanContextServiceAvailableEvent(getBeanContextServicesPeer(), serviceClass);

        fireServiceAdded(bcssae);
    }

    /**
     * 触发一个 <tt>BeanContextServiceAvailableEvent</tt> 指示新服务可用。
     *
     * @param bcssae <tt>BeanContextServiceAvailableEvent</tt>
     */
    protected final void fireServiceAdded(BeanContextServiceAvailableEvent bcssae) {
        Object[]                         copy;

        synchronized (bcsListeners) { copy = bcsListeners.toArray(); }

        for (int i = 0; i < copy.length; i++) {
            ((BeanContextServicesListener)copy[i]).serviceAvailable(bcssae);
        }
    }

    /**
     * 触发一个 <tt>BeanContextServiceEvent</tt> 通知服务被撤销。
     *
     * @param bcsre <tt>BeanContextServiceRevokedEvent</tt>
     */
    protected final void fireServiceRevoked(BeanContextServiceRevokedEvent bcsre) {
        Object[]                         copy;

        synchronized (bcsListeners) { copy = bcsListeners.toArray(); }

        for (int i = 0; i < copy.length; i++) {
            ((BeanContextServicesRevokedListener)copy[i]).serviceRevoked(bcsre);
        }
    }

    /**
     * 触发一个 <tt>BeanContextServiceRevokedEvent</tt>
     * 指示特定服务不再可用。
     * @param serviceClass 服务类
     * @param revokeNow 是否应立即撤销事件
     */
    protected final void fireServiceRevoked(Class serviceClass, boolean revokeNow) {
        Object[]                       copy;
        BeanContextServiceRevokedEvent bcsre = new BeanContextServiceRevokedEvent(getBeanContextServicesPeer(), serviceClass, revokeNow);

        synchronized (bcsListeners) { copy = bcsListeners.toArray(); }

        for (int i = 0; i < copy.length; i++) {
            ((BeanContextServicesListener)copy[i]).serviceRevoked(bcsre);
        }
   }

    /**
     * 从 BeanContextSupport writeObject 调用，在序列化子项之前。
     *
     * 该类将序列化任何 Serializable BeanContextServiceProviders。
     *
     * 子类可以封装此方法以插入在子项序列化之前需要进行的自己的序列化处理。
     */

    protected synchronized void bcsPreSerializationHook(ObjectOutputStream oos) throws IOException {

        oos.writeInt(serializable);

        if (serializable <= 0) return;

        int count = 0;

        Iterator i = services.entrySet().iterator();

        while (i.hasNext() && count < serializable) {
            Map.Entry           entry = (Map.Entry)i.next();
            BCSSServiceProvider bcsp  = null;

             try {
                bcsp = (BCSSServiceProvider)entry.getValue();
             } catch (ClassCastException cce) {
                continue;
             }

             if (bcsp.getServiceProvider() instanceof Serializable) {
                oos.writeObject(entry.getKey());
                oos.writeObject(bcsp);
                count++;
             }
        }

        if (count != serializable)
            throw new IOException("wrote different number of service providers than expected");
    }

    /**
     * 从 BeanContextSupport readObject 调用，在反序列化子项之前。
     *
     * 该类将反序列化之前序列化的任何 Serializable BeanContextServiceProviders，
     * 使它们在子项反序列化时可用。
     *
     * 子类可以封装此方法以插入在子项序列化之前需要进行的自己的序列化处理。
     */

    protected synchronized void bcsPreDeserializationHook(ObjectInputStream ois) throws IOException, ClassNotFoundException {

        serializable = ois.readInt();

        int count = serializable;

        while (count > 0) {
            services.put(ois.readObject(), ois.readObject());
            count--;
        }
    }

    /**
     * 序列化实例
     */

    private synchronized void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();

        serialize(oos, (Collection)bcsListeners);
    }

    /**
     * 反序列化实例
     */

    private synchronized void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {

        ois.defaultReadObject();

        deserialize(ois, (Collection)bcsListeners);
    }


    /*
     * 字段
     */

    /**
     * 所有对 <code> protected transient HashMap services </code>
     * 字段的访问都应同步在该对象上。
     */
    protected transient HashMap                  services;

    /**
     * 可序列化的 <tt>BeanContextServceProvider</tt> 实例的数量。
     */
    protected transient int                      serializable = 0;


    /**
     * <tt>BeanContextServiceProvider</tt> 的委托。
     */
    protected transient BCSSProxyServiceProvider proxy;


    /**
     * <tt>BeanContextServicesListener</tt> 对象的列表。
     */
    protected transient ArrayList                bcsListeners;
}
