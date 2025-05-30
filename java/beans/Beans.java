
/*
 * Copyright (c) 1996, 2015, Oracle and/or its affiliates. All rights reserved.
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

package java.beans;

import com.sun.beans.finder.ClassFinder;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;

import java.awt.Image;

import java.beans.beancontext.BeanContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

import java.lang.reflect.Modifier;

import java.net.URL;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * 该类提供了一些通用的 beans 控制方法。
 */

public class Beans {

    /**
     * <p>
     * 实例化一个 JavaBean。
     * </p>
     * @return 一个 JavaBean
     * @param     cls         用于创建 bean 的类加载器。如果为 null，则使用系统类加载器。
     * @param     beanName    类加载器中的 bean 名称。例如 "sun.beanbox.foobah"
     *
     * @exception ClassNotFoundException 如果找不到序列化对象的类。
     * @exception IOException 如果发生 I/O 错误。
     */

    public static Object instantiate(ClassLoader cls, String beanName) throws IOException, ClassNotFoundException {
        return Beans.instantiate(cls, beanName, null, null);
    }

    /**
     * <p>
     * 实例化一个 JavaBean。
     * </p>
     * @return 一个 JavaBean
     *
     * @param     cls         用于创建 bean 的类加载器。如果为 null，则使用系统类加载器。
     * @param     beanName    类加载器中的 bean 名称。例如 "sun.beanbox.foobah"
     * @param     beanContext 要嵌套新 bean 的 BeanContext
     *
     * @exception ClassNotFoundException 如果找不到序列化对象的类。
     * @exception IOException 如果发生 I/O 错误。
     */

    public static Object instantiate(ClassLoader cls, String beanName, BeanContext beanContext) throws IOException, ClassNotFoundException {
        return Beans.instantiate(cls, beanName, beanContext, null);
    }

    /**
     * 实例化一个 bean。
     * <p>
     * 该 bean 是基于相对于类加载器的名称创建的。该名称应为点分隔的名称，例如 "a.b.c"。
     * <p>
     * 在 Beans 1.0 中，给定名称可以表示序列化对象或类。其他机制可能会在未来添加。在
     * Beans 1.0 中，我们首先尝试将 beanName 作为序列化对象名称处理，然后作为类名称处理。
     * <p>
     * 当使用 beanName 作为序列化对象名称时，我们将给定的 beanName 转换为资源路径名，并添加一个尾部 ".ser" 后缀。
     * 然后尝试从该资源加载序列化对象。
     * <p>
     * 例如，给定 beanName 为 "x.y"，Beans.instantiate 首先尝试从资源 "x/y.ser" 读取序列化对象，
     * 如果失败，则尝试加载类 "x.y" 并创建该类的实例。
     * <p>
     * 如果 bean 是 java.applet.Applet 的子类型，则会对其进行一些特殊初始化。首先，为其提供默认的
     * AppletStub 和 AppletContext。其次，如果它是从类名实例化的，则调用 applet 的 "init" 方法。
     * （如果 bean 是反序列化的，则跳过此步骤。）
     * <p>
     * 注意，对于 applet 类型的 beans，调用者有责任调用 "start" 方法。为了正确的行为，应在 applet
     * 被添加到可见的 AWT 容器后调用 "start"。
     * <p>
     * 注意，通过 beans.instantiate 创建的 applet 运行的环境与在浏览器中运行的 applet 略有不同。
     * 特别是，bean applet 无法访问 "parameters"，因此它们可能希望提供属性的 get/set 方法来设置参数值。
     * 我们建议 bean-applet 开发者使用 JDK appletviewer（作为参考浏览器环境）和 BDK BeanBox（作为参考 bean 容器）测试他们的 bean-applet。
     *
     * @return 一个 JavaBean
     * @param     cls         用于创建 bean 的类加载器。如果为 null，则使用系统类加载器。
     * @param     beanName    类加载器中的 bean 名称。例如 "sun.beanbox.foobah"
     * @param     beanContext 要嵌套新 bean 的 BeanContext
     * @param     initializer 新 bean 的 AppletInitializer
     *
     * @exception ClassNotFoundException 如果找不到序列化对象的类。
     * @exception IOException 如果发生 I/O 错误。
     */

    public static Object instantiate(ClassLoader cls, String beanName, BeanContext beanContext, AppletInitializer initializer)
                        throws IOException, ClassNotFoundException {

        InputStream ins;
        ObjectInputStream oins = null;
        Object result = null;
        boolean serialized = false;
        IOException serex = null;

        // 如果给定的类加载器为 null，我们检查是否有系统类加载器可用，并（如果可用）
        // 使用该类加载器。
        // 注意，对系统类加载器的调用将首先在引导类加载器中查找。
        if (cls == null) {
            try {
                cls = ClassLoader.getSystemClassLoader();
            } catch (SecurityException ex) {
                // 不允许访问系统类加载器。
                // 继续。
            }
        }

        // 尝试找到具有该名称的序列化对象
        final String serName = beanName.replace('.','/').concat(".ser");
        if (cls == null)
            ins =  ClassLoader.getSystemResourceAsStream(serName);
        else
            ins =  cls.getResourceAsStream(serName);
        if (ins != null) {
            try {
                if (cls == null) {
                    oins = new ObjectInputStream(ins);
                } else {
                    oins = new ObjectInputStreamWithLoader(ins, cls);
                }
                result = oins.readObject();
                serialized = true;
                oins.close();
            } catch (IOException ex) {
                ins.close();
                // 继续尝试打开类。但如果找不到类，记住该异常。
                serex = ex;
            } catch (ClassNotFoundException ex) {
                ins.close();
                throw ex;
            }
        }

        if (result == null) {
            // 没有找到序列化对象，尝试实例化类
            Class<?> cl;

            try {
                cl = ClassFinder.findClass(beanName, cls);
            } catch (ClassNotFoundException ex) {
                // 没有找到合适的类。如果之前尝试反序列化对象并遇到 I/O 异常，抛出该异常，
                // 否则重新抛出 ClassNotFoundException。
                if (serex != null) {
                    throw serex;
                }
                throw ex;
            }

            if (!Modifier.isPublic(cl.getModifiers())) {
                throw new ClassNotFoundException("" + cl + " : 无公共访问权限");
            }

            /*
             * 尝试实例化类。
             */

            try {
                result = cl.newInstance();
            } catch (Exception ex) {
                // 必须将异常映射到签名中的一个异常。
                // 但在详细消息中传递额外信息。
                throw new ClassNotFoundException("" + cl + " : " + ex, ex);
            }
        }

        if (result != null) {

            // 好的，如果结果是 applet，则初始化它。

            AppletStub stub = null;

            if (result instanceof Applet) {
                Applet  applet      = (Applet) result;
                boolean needDummies = initializer == null;

                if (needDummies) {

                    // 确定 codebase 和 docbase URL。我们通过查找已知资源的 URL，然后
                    // 处理 URL 来实现这一点。

                    // 首先找到与 bean 本身对应的 "资源名称"。因此，序列化 bean "a.b.c" 将意味着
                    // 资源名称为 "a/b/c.ser"，而类名为 "x.y" 将意味着资源名称为 "x/y.class"。

                    final String resourceName;

                    if (serialized) {
                        // 序列化 bean
                        resourceName = beanName.replace('.','/').concat(".ser");
                    } else {
                        // 普通类
                        resourceName = beanName.replace('.','/').concat(".class");
                    }

                    URL objectUrl = null;
                    URL codeBase  = null;
                    URL docBase   = null;

                    // 现在获取与资源名称对应的 URL。
                    if (cls == null) {
                        objectUrl = ClassLoader.getSystemResource(resourceName);
                    } else
                        objectUrl = cls.getResource(resourceName);

                    // 如果找到了 URL，我们尝试通过删除最后一个路径名称组件来定位 docbase，
                    // 并通过删除完整的 resourceName 来定位 codebase。
                    // 因此，如果 resourceName 为 "a/b/c.class"，并且我们得到了一个
                    // objectURL "file://bert/classes/a/b/c.class"，那么我们希望将 codebase 设置为
                    // "file://bert/classes/"，将 docbase 设置为 "file://bert/classes/a/b/"

                    if (objectUrl != null) {
                        String s = objectUrl.toExternalForm();

                        if (s.endsWith(resourceName)) {
                            int ix   = s.length() - resourceName.length();
                            codeBase = new URL(s.substring(0,ix));
                            docBase  = codeBase;

                            ix = s.lastIndexOf('/');

                            if (ix >= 0) {
                                docBase = new URL(s.substring(0,ix+1));
                            }
                        }
                    }

                    // 设置默认的上下文和存根。
                    BeansAppletContext context = new BeansAppletContext(applet);

                    stub = (AppletStub)new BeansAppletStub(applet, context, codeBase, docBase);
                    applet.setStub(stub);
                } else {
                    initializer.initialize(applet, beanContext);
                }

                // 现在，如果有 BeanContext，添加 bean，如果适用。

                if (beanContext != null) {
                    unsafeBeanContextAdd(beanContext, result);
                }

                // 如果它是反序列化的，则已经初始化。
                // 否则我们需要初始化它。

                if (!serialized) {
                    // 需要设置一个合理的初始大小，因为许多 applet 如果没有显式设置大小就启动会不高兴。
                    applet.setSize(100,100);
                    applet.init();
                }

                if (needDummies) {
                  ((BeansAppletStub)stub).active = true;
                } else initializer.activate(applet);

            } else if (beanContext != null) unsafeBeanContextAdd(beanContext, result);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static void unsafeBeanContextAdd(BeanContext beanContext, Object res) {
        beanContext.add(res);
    }

    /**
     * 从给定的 bean 中，获取表示该源对象指定类型视图的对象。
     * <p>
     * 结果可能是同一个对象或不同的对象。如果请求的目标视图不可用，则返回给定的
     * bean。
     * <p>
     * 该方法在 Beans 1.0 中提供，作为允许在未来添加更灵活的 bean 行为的钩子。
     *
     * @return 表示源对象指定类型视图的对象
     * @param bean        我们希望从中获取视图的对象。
     * @param targetType  我们希望获取的视图类型。
     *
     */
    public static Object getInstanceOf(Object bean, Class<?> targetType) {
        return bean;
    }

    /**
     * 检查 bean 是否可以视为给定的目标类型。
     * 如果可以使用 Beans.getInstanceof 方法在给定的 bean 上获取表示指定 targetType 类型视图的对象，
     * 则结果为 true。
     *
     * @param bean  我们希望从中获取视图的 bean。
     * @param targetType  我们希望获取的视图类型。
     * @return 如果给定的 bean 支持给定的 targetType，则返回 "true"。
     *
     */
    public static boolean isInstanceOf(Object bean, Class<?> targetType) {
        return Introspector.isSubclass(bean.getClass(), targetType);
    }

    /**
     * 测试是否处于设计模式。
     *
     * @return 如果我们正在应用程序构建环境中运行，则返回 True。
     *
     * @see DesignMode
     */
    public static boolean isDesignTime() {
        return ThreadGroupContext.getContext().isDesignTime();
    }


/**
 * 确定 beans 是否可以假设 GUI 可用。
 *
 * @return  如果我们正在运行的环境中 beans 可以假设交互式 GUI 可用，那么返回 True，因此它们可以弹出对话框等。通常在窗口环境中返回 true，而在服务器环境或应用程序作为批处理作业的一部分运行时通常返回 false。
 *
 * @see Visibility
 *
 */
public static boolean isGuiAvailable() {
    return ThreadGroupContext.getContext().isGuiAvailable();
}

/**
 * 用于指示我们是否在应用程序构建器环境中运行。
 *
 * <p>注意，此方法进行了安全检查，并且不可用于（例如）不受信任的 applet。
 * 更具体地说，如果有安全经理，将调用其 <code>checkPropertiesAccess</code>
 * 方法。这可能导致 SecurityException。
 *
 * @param isDesignTime  如果我们在应用程序构建工具中，则为 True。
 * @exception  SecurityException  如果存在安全经理并且其
 *             <code>checkPropertiesAccess</code> 方法不允许设置
 *              系统属性。
 * @see SecurityManager#checkPropertiesAccess
 */

public static void setDesignTime(boolean isDesignTime)
                        throws SecurityException {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
        sm.checkPropertiesAccess();
    }
    ThreadGroupContext.getContext().setDesignTime(isDesignTime);
}

/**
 * 用于指示我们是否在 GUI 交互可用的环境中运行。
 *
 * <p>注意，此方法进行了安全检查，并且不可用于（例如）不受信任的 applet。
 * 更具体地说，如果有安全经理，将调用其 <code>checkPropertiesAccess</code>
 * 方法。这可能导致 SecurityException。
 *
 * @param isGuiAvailable  如果 GUI 交互可用，则为 True。
 * @exception  SecurityException  如果存在安全经理并且其
 *             <code>checkPropertiesAccess</code> 方法不允许设置
 *              系统属性。
 * @see SecurityManager#checkPropertiesAccess
 */

public static void setGuiAvailable(boolean isGuiAvailable)
                        throws SecurityException {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
        sm.checkPropertiesAccess();
    }
    ThreadGroupContext.getContext().setGuiAvailable(isGuiAvailable);
}
}

/**
 * ObjectInputStream 的子类，将类的加载委托给现有的 ClassLoader。
 */

class ObjectInputStreamWithLoader extends ObjectInputStream
{
    private ClassLoader loader;

    /**
     * 加载器必须非空；
     */

    public ObjectInputStreamWithLoader(InputStream in, ClassLoader loader)
            throws IOException, StreamCorruptedException {

        super(in);
        if (loader == null) {
            throw new IllegalArgumentException("传递给 ObjectInputStreamWithLoader 的非法空参数");
        }
        this.loader = loader;
    }

    /**
     * 使用给定的 ClassLoader 而不是使用系统类
     */
    @SuppressWarnings("rawtypes")
    protected Class resolveClass(ObjectStreamClass classDesc)
        throws IOException, ClassNotFoundException {

        String cname = classDesc.getName();
        return ClassFinder.resolveClass(cname, this.loader);
    }
}

/**
 * 包私有支持类。这为作为 applet 的 beans 提供默认的 AppletContext。
 */

class BeansAppletContext implements AppletContext {
    Applet target;
    Hashtable<URL,Object> imageCache = new Hashtable<>();

    BeansAppletContext(Applet target) {
        this.target = target;
    }

    public AudioClip getAudioClip(URL url) {
        // 我们目前在 Beans.instantiate 的 applet 上下文中不支持音频剪辑，除非通过某种运气存在一个可以从音频 URL 生成 AudioClip 的 URL 内容类。
        try {
            return (AudioClip) url.getContent();
        } catch (Exception ex) {
            return null;
        }
    }

    public synchronized Image getImage(URL url) {
        Object o = imageCache.get(url);
        if (o != null) {
            return (Image)o;
        }
        try {
            o = url.getContent();
            if (o == null) {
                return null;
            }
            if (o instanceof Image) {
                imageCache.put(url, o);
                return (Image) o;
            }
            // 否则它必须是一个 ImageProducer。
            Image img = target.createImage((java.awt.image.ImageProducer)o);
            imageCache.put(url, img);
            return img;

        } catch (Exception ex) {
            return null;
        }
    }

    public Applet getApplet(String name) {
        return null;
    }

    public Enumeration<Applet> getApplets() {
        Vector<Applet> applets = new Vector<>();
        applets.addElement(target);
        return applets.elements();
    }

    public void showDocument(URL url) {
        // 我们什么也不做。
    }

    public void showDocument(URL url, String target) {
        // 我们什么也不做。
    }

    public void showStatus(String status) {
        // 我们什么也不做。
    }

    public void setStream(String key, InputStream stream)throws IOException{
        // 我们什么也不做。
    }

    public InputStream getStream(String key){
        // 我们什么也不做。
        return null;
    }

    public Iterator<String> getStreamKeys(){
        // 我们什么也不做。
        return null;
    }
}

/**
 * 包私有支持类。这为作为 applet 的 beans 提供 AppletStub。
 */
class BeansAppletStub implements AppletStub {
    transient boolean active;
    transient Applet target;
    transient AppletContext context;
    transient URL codeBase;
    transient URL docBase;

    BeansAppletStub(Applet target,
                AppletContext context, URL codeBase,
                                URL docBase) {
        this.target = target;
        this.context = context;
        this.codeBase = codeBase;
        this.docBase = docBase;
    }

    public boolean isActive() {
        return active;
    }

    public URL getDocumentBase() {
        // 使用 applet 类加载器的根目录
        return docBase;
    }

    public URL getCodeBase() {
        // 使用我们找到类或序列化对象的目录。
        return codeBase;
    }

    public String getParameter(String name) {
        return null;
    }

    public AppletContext getAppletContext() {
        return context;
    }

    public void appletResize(int width, int height) {
        // 我们什么也不做。
    }
}
