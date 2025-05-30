/*
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.beans.decoder.DocumentHandler;

import java.io.Closeable;
import java.io.InputStream;
import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <code>XMLDecoder</code> 类用于读取由 <code>XMLEncoder</code> 创建的 XML 文档，
 * 使用方式与 <code>ObjectInputStream</code> 类似。例如，可以使用以下代码片段读取由
 * <code>XMLEncoder</code> 类写入的 XML 文档中的第一个对象：
 * <pre>
 *       XMLDecoder d = new XMLDecoder(
 *                          new BufferedInputStream(
 *                              new FileInputStream("Test.xml")));
 *       Object result = d.readObject();
 *       d.close();
 * </pre>
 *
 *<p>
 * 有关更多信息，还可以查阅
 * <a
 href="http://java.sun.com/products/jfc/tsc/articles/persistence3">JavaBeans 组件的长期持久性：XML 模式</a>，
 * 这是一篇发表在 <em>The Swing Connection</em> 上的文章。
 * @see XMLEncoder
 * @see java.io.ObjectInputStream
 *
 * @since 1.4
 *
 * @author Philip Milne
 */
public class XMLDecoder implements AutoCloseable {
    private final AccessControlContext acc = AccessController.getContext();
    private final DocumentHandler handler = new DocumentHandler();
    private final InputSource input;
    private Object owner;
    private Object[] array;
    private int index;

    /**
     * 创建一个新的输入流，用于读取由 <code>XMLEncoder</code> 类创建的归档文件。
     *
     * @param in 底层流。
     *
     * @see XMLEncoder#XMLEncoder(java.io.OutputStream)
     */
    public XMLDecoder(InputStream in) {
        this(in, null);
    }

    /**
     * 创建一个新的输入流，用于读取由 <code>XMLEncoder</code> 类创建的归档文件。
     *
     * @param in 底层流。
     * @param owner 此流的所有者。
     *
     */
    public XMLDecoder(InputStream in, Object owner) {
        this(in, owner, null);
    }

    /**
     * 创建一个新的输入流，用于读取由 <code>XMLEncoder</code> 类创建的归档文件。
     *
     * @param in 底层流。
     * @param owner 此流的所有者。
     * @param exceptionListener 流的异常处理器；如果为 <code>null</code>，则使用默认的异常监听器。
     */
    public XMLDecoder(InputStream in, Object owner, ExceptionListener exceptionListener) {
        this(in, owner, exceptionListener, null);
    }

    /**
     * 创建一个新的输入流，用于读取由 <code>XMLEncoder</code> 类创建的归档文件。
     *
     * @param in 底层流。可以传递 <code>null</code>，但生成的 XMLDecoder 将无用。
     * @param owner 此流的所有者。可以为 <code>null</code>。
     * @param exceptionListener 流的异常处理器，或 <code>null</code> 以使用默认值。
     * @param cl 用于实例化对象的类加载器。如果为 <code>null</code>，则使用默认类加载器。
     * @since 1.5
     */
    public XMLDecoder(InputStream in, Object owner,
                      ExceptionListener exceptionListener, ClassLoader cl) {
        this(new InputSource(in), owner, exceptionListener, cl);
    }


    /**
     * 创建一个新的解码器，用于解析由 {@code XMLEncoder} 类创建的 XML 归档文件。
     * 如果输入源 {@code is} 为 {@code null}，则不会抛出异常，也不会执行解析。
     * 这种行为类似于使用 {@code InputStream} 作为参数的其他构造函数。
     *
     * @param is  要解析的输入源。
     *
     * @since 1.7
     */
    public XMLDecoder(InputSource is) {
        this(is, null, null, null);
    }

    /**
     * 创建一个新的解码器，用于解析由 {@code XMLEncoder} 类创建的 XML 归档文件。
     *
     * @param is     要解析的输入源。
     * @param owner  此解码器的所有者。
     * @param el     解析器的异常处理器，或 {@code null} 以使用默认异常处理器。
     * @param cl     用于实例化对象的类加载器，或 {@code null} 以使用默认类加载器。
     *
     * @since 1.7
     */
    private XMLDecoder(InputSource is, Object owner, ExceptionListener el, ClassLoader cl) {
        this.input = is;
        this.owner = owner;
        setExceptionListener(el);
        this.handler.setClassLoader(cl);
        this.handler.setOwner(this);
    }

    /**
     * 关闭与此流关联的输入流。
     */
    public void close() {
        if (parsingComplete()) {
            close(this.input.getCharacterStream());
            close(this.input.getByteStream());
        }
    }

    private void close(Closeable in) {
        if (in != null) {
            try {
                in.close();
            }
            catch (IOException e) {
                getExceptionListener().exceptionThrown(e);
            }
        }
    }

    private boolean parsingComplete() {
        if (this.input == null) {
            return false;
        }
        if (this.array == null) {
            if ((this.acc == null) && (null != System.getSecurityManager())) {
                throw new SecurityException("AccessControlContext 未设置");
            }
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    XMLDecoder.this.handler.parse(XMLDecoder.this.input);
                    return null;
                }
            }, this.acc);
            this.array = this.handler.getObjects();
        }
        return true;
    }

    /**
     * 将此流的异常处理器设置为 <code>exceptionListener</code>。
     * 当此流捕获可恢复的异常时，异常处理器将收到通知。
     *
     * @param exceptionListener 此流的异常处理器；
     * 如果为 <code>null</code>，则使用默认的异常监听器。
     *
     * @see #getExceptionListener
     */
    public void setExceptionListener(ExceptionListener exceptionListener) {
        if (exceptionListener == null) {
            exceptionListener = Statement.defaultExceptionListener;
        }
        this.handler.setExceptionListener(exceptionListener);
    }

    /**
     * 获取此流的异常处理器。
     *
     * @return 此流的异常处理器。
     *     如果未显式设置，则返回默认的异常监听器。
     *
     * @see #setExceptionListener
     */
    public ExceptionListener getExceptionListener() {
        return this.handler.getExceptionListener();
    }

    /**
     * 从底层输入流中读取下一个对象。
     *
     * @return 读取的下一个对象。
     *
     * @throws ArrayIndexOutOfBoundsException 如果流中没有对象（或没有更多对象）。
     *
     * @see XMLEncoder#writeObject
     */
    public Object readObject() {
        return (parsingComplete())
                ? this.array[this.index++]
                : null;
    }

    /**
     * 将此解码器的所有者设置为 <code>owner</code>。
     *
     * @param owner 此解码器的所有者。
     *
     * @see #getOwner
     */
    public void setOwner(Object owner) {
        this.owner = owner;
    }

    /**
     * 获取此解码器的所有者。
     *
     * @return 此解码器的所有者。
     *
     * @see #setOwner
     */
    public Object getOwner() {
        return owner;
    }

    /**
     * 创建一个新的 SAX 解析器处理器，用于解析由 {@code XMLEncoder} 类创建的嵌入式 XML 归档文件。
     *
     * 如果解析的 XML 文档中包含 &lt;java&gt; 元素内的方法调用，则应使用 {@code owner}。
     * 如果 {@code owner} 为 {@code null}，则可能导致非法解析。
     * 如果 {@code owner} 类中不包含预期的方法调用，也会出现相同的问题。详情请参阅 <a
     * href="http://java.sun.com/products/jfc/tsc/articles/persistence3/">这里</a>。
     *
     * @param owner  可用作 &lt;java&gt; 元素值的默认处理器的所有者。
     * @param el     解析器的异常处理器，或 {@code null} 以使用默认异常处理器。
     * @param cl     用于实例化对象的类加载器，或 {@code null} 以使用默认类加载器。
     * @return 用于 SAX 解析器的 {@code DefaultHandler} 实例。
     *
     * @since 1.7
     */
    public static DefaultHandler createHandler(Object owner, ExceptionListener el, ClassLoader cl) {
        DocumentHandler handler = new DocumentHandler();
        handler.setOwner(owner);
        handler.setExceptionListener(el);
        handler.setClassLoader(cl);
        return handler;
    }
}
