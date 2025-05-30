
/*
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.datatransfer;

import sun.awt.datatransfer.DataTransferer;
import sun.reflect.misc.ReflectUtil;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OptionalDataException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import static sun.security.util.SecurityConstants.GET_CLASSLOADER_PERMISSION;

/**
 * {@code DataFlavor} 提供有关数据的元信息。{@code DataFlavor}
 * 通常用于访问剪贴板上的数据，或在拖放操作期间使用。
 * <p>
 * {@code DataFlavor} 的实例封装了在 <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>
 * 和 <a href="http://www.ietf.org/rfc/rfc2046.txt">RFC 2046</a> 中定义的内容类型。
 * 内容类型通常称为 MIME 类型。
 * <p>
 * 内容类型由媒体类型（称为主要类型）、子类型和可选参数组成。有关 MIME 类型语法的详细信息，请参阅
 * <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>。
 * <p>
 * JRE 数据传输实现将 MIME 类型的参数 &quot;class&quot; 解释为 <b>表示类</b>。
 * 表示类反映了正在传输的对象的类。换句话说，表示类是 {@link Transferable#getTransferData} 返回的对象类型。
 * 例如，{@link #imageFlavor} 的 MIME 类型为
 * {@code "image/x-java-image;class=java.awt.Image"}，
 * 主要类型为 {@code image}，子类型为
 * {@code x-java-image}，表示类为
 * {@code java.awt.Image}。当使用 {@code imageFlavor} 的 {@code DataFlavor} 调用 {@code getTransferData} 时，
 * 返回一个 {@code java.awt.Image} 的实例。
 * 需要注意的是，{@code DataFlavor} 不会对表示类进行错误检查。由 {@code DataFlavor} 的使用者（如 {@code Transferable}）来遵守表示类。
 * <br>
 * 注意，如果在创建 {@code DataFlavor} 时未指定表示类，则使用默认的表示类。请参阅 {@code DataFlavor} 的构造函数的适当文档。
 * <p>
 * 此外，具有 &quot;text&quot; 主 MIME 类型的 {@code DataFlavor} 实例可能具有 &quot;charset&quot; 参数。有关 &quot;text&quot; MIME 类型
 * 和 &quot;charset&quot; 参数的详细信息，请参阅
 * <a href="http://www.ietf.org/rfc/rfc2046.txt">RFC 2046</a> 和
 * {@link #selectBestTextFlavor}。
 * <p>
 * {@code DataFlavor} 的相等性由主要类型、子类型和表示类确定。请参阅 {@link #equals(DataFlavor)} 了解详细信息。在确定相等性时，任何可选参数都会被忽略。
 * 例如，以下代码生成两个被认为是相同的 {@code DataFlavor}：
 * <pre>
 *   DataFlavor flavor1 = new DataFlavor(Object.class, &quot;X-test/test; class=&lt;java.lang.Object&gt;; foo=bar&quot;);
 *   DataFlavor flavor2 = new DataFlavor(Object.class, &quot;X-test/test; class=&lt;java.lang.Object&gt;; x=y&quot;);
 *   // 以下返回 true。
 *   flavor1.equals(flavor2);
 * </pre>
 * 如前所述，{@code flavor1} 和 {@code flavor2} 被认为是相同的。因此，向 {@code Transferable} 请求任一 {@code DataFlavor} 都会返回相同的结果。
 * <p>
 * 有关使用 Swing 进行数据传输的更多信息，请参阅
 * <a href="https://docs.oracle.com/javase/tutorial/uiswing/dnd/index.html">
 * 如何使用拖放和数据传输</a>，
 * <em>Java 教程</em> 中的章节。
 *
 * @author      Blake Sullivan
 * @author      Laurence P. G. Cable
 * @author      Jeff Dunn
 */
public class DataFlavor implements Externalizable, Cloneable {

    private static final long serialVersionUID = 8367026044764648243L;
    private static final Class<InputStream> ioInputStreamClass = InputStream.class;

    /**
     * 尝试从引导加载器、系统加载器、上下文加载器（如果存在）和最终指定的加载器加载类。
     *
     * @param className 要加载的类的名称
     * @param fallback 备用加载器
     * @return 加载的类
     * @exception ClassNotFoundException 如果类未找到
     */
    protected final static Class<?> tryToLoadClass(String className,
                                                   ClassLoader fallback)
        throws ClassNotFoundException
    {
        ReflectUtil.checkPackageAccess(className);
        try {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(GET_CLASSLOADER_PERMISSION);
            }
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            try {
                // 引导类加载器和系统类加载器（如果存在）
                return Class.forName(className, true, loader);
            }
            catch (ClassNotFoundException exception) {
                // 线程上下文类加载器（如果存在）
                loader = Thread.currentThread().getContextClassLoader();
                if (loader != null) {
                    try {
                        return Class.forName(className, true, loader);
                    }
                    catch (ClassNotFoundException e) {
                        // 回退到用户的类加载器
                    }
                }
            }
        } catch (SecurityException exception) {
            // 忽略安全的类加载器
        }
        return Class.forName(className, true, fallback);
    }

    /*
     * 私有初始化器
     */
    static private DataFlavor createConstant(Class<?> rc, String prn) {
        try {
            return new DataFlavor(rc, prn);
        } catch (Exception e) {
            return null;
        }
    }

    /*
     * 私有初始化器
     */
    static private DataFlavor createConstant(String mt, String prn) {
        try {
            return new DataFlavor(mt, prn);
        } catch (Exception e) {
            return null;
        }
    }

    /*
     * 私有初始化器
     */
    static private DataFlavor initHtmlDataFlavor(String htmlFlavorType) {
        try {
            return new DataFlavor ("text/html; class=java.lang.String;document=" +
                                       htmlFlavorType + ";charset=Unicode");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 表示 Java Unicode 字符串类的 {@code DataFlavor}，其中：
     * <pre>
     *     representationClass = java.lang.String
     *     mimeType           = "application/x-java-serialized-object"
     * </pre>
     */
    public static final DataFlavor stringFlavor = createConstant(java.lang.String.class, "Unicode String");

    /**
     * 表示 Java 图像类的 {@code DataFlavor}，其中：
     * <pre>
     *     representationClass = java.awt.Image
     *     mimeType            = "image/x-java-image"
     * </pre>
     */
    public static final DataFlavor imageFlavor = createConstant("image/x-java-image; class=java.awt.Image", "Image");

    /**
     * 表示带有 Unicode 编码的纯文本的 {@code DataFlavor}，其中：
     * <pre>
     *     representationClass = InputStream
     *     mimeType            = "text/plain; charset=unicode"
     * </pre>
     * 该 {@code DataFlavor} 已被 <b>弃用</b>，因为
     * (1) 其表示形式为 InputStream，这是一个基于 8 位的表示形式，而 Unicode 是一个 16 位字符集；(2) 字符集 "unicode" 未明确定义。"unicode" 暗示了一个特定平台的 Unicode 实现，而不是跨平台的实现。
     *
     * @deprecated 从 1.3 版本开始。使用 <code>DataFlavor.getReaderForText(Transferable)</code>
     *             代替 <code>Transferable.getTransferData(DataFlavor.plainTextFlavor)</code>。
     */
    @Deprecated
    public static final DataFlavor plainTextFlavor = createConstant("text/plain; charset=unicode; class=java.io.InputStream", "Plain Text");

    /**
     * MIME 内容类型 application/x-java-serialized-object 表示已持久化的 Java 对象图。
     *
     * 与此 {@code DataFlavor} 关联的表示类标识了从 <code>java.awt.datatransfer.getTransferData</code> 调用返回的对象的 Java 类型。
     */
    public static final String javaSerializedObjectMimeType = "application/x-java-serialized-object";

    /**
     * 要在 Java（和底层平台）之间传输文件列表，使用此类型/子类型和表示类为 <code>java.util.List</code> 的 {@code DataFlavor}。
     * 列表中的每个元素必须/保证是 <code>java.io.File</code> 类型。
     */
    public static final DataFlavor javaFileListFlavor = createConstant("application/x-java-file-list;class=java.util.List", null);

    /**
     * 要在同一个 JVM 内通过 <code>Transferable</code> 接口传输没有关联 MIME 内容类型的任意 Java 对象引用，
     * 使用此类型/子类型的 {@code DataFlavor}，表示类等于要通过 <code>Transferable</code> 传递的类/接口的类型。
     * <p>
     * 从 <code>Transferable.getTransferData</code> 返回的对象引用必须是
     * 该 {@code DataFlavor} 的表示类的实例。
     */
    public static final String javaJVMLocalObjectMimeType = "application/x-java-jvm-local-objectref";

    /**
     * 为了通过拖放 <code>ACTION_LINK</code> 操作传递对远程对象的实时链接，应使用 MIME 内容类型
     * application/x-java-remote-object，其中 <code>DataFlavor</code> 的表示类表示要传输的 <code>Remote</code> 接口的类型。
     */
    public static final String javaRemoteObjectMimeType = "application/x-java-remote-object";

    /**
     * 表示 HTML 标记的一部分。标记由源端选择的部分组成。因此，标记中的某些标签可能是未配对的。如果使用此风味表示
     * {@link Transferable} 实例中的数据，则不会进行任何额外的更改。
     * 该 DataFlavor 实例表示与 DataFlavor 实例相同的 HTML 标记，这些 DataFlavor 实例的内容 MIME 类型不包含文档参数，
     * 且表示类为 String 类。
     * <pre>
     *     representationClass = String
     *     mimeType           = "text/html"
     * </pre>
     */
    public static DataFlavor selectionHtmlFlavor = initHtmlDataFlavor("selection");

    /**
     * 表示 HTML 标记的一部分。如果可能，从本地系统接收的标记将补充配对标签以形成格式良好的 HTML 标记。如果使用此风味表示
     * {@link Transferable} 实例中的数据，则不会进行任何额外的更改。
     * <pre>
     *     representationClass = String
     *     mimeType           = "text/html"
     * </pre>
     */
    public static DataFlavor fragmentHtmlFlavor = initHtmlDataFlavor("fragment");

    /**
     * 表示 HTML 标记的一部分。如果可能，从本地系统接收的标记将补充额外的标签以形成格式良好的 HTML 文档。如果使用此风味表示
     * {@link Transferable} 实例中的数据，则不会进行任何额外的更改。
     * <pre>
     *     representationClass = String
     *     mimeType           = "text/html"
     * </pre>
     */
    public static  DataFlavor allHtmlFlavor = initHtmlDataFlavor("all");

    /**
     * 构造一个新的 {@code DataFlavor}。此构造函数仅用于支持
     * <code>Externalizable</code> 接口。不
     * 用于公共（客户端）使用。
     *
     * @since 1.2
     */
    public DataFlavor() {
        super();
    }

    /**
     * 构造一个完全指定的 {@code DataFlavor}。
     *
     * @exception NullPointerException 如果 <code>primaryType</code>、
     *            <code>subType</code> 或 <code>representationClass</code> 为 null
     */
    private DataFlavor(String primaryType, String subType, MimeTypeParameterList params, Class<?> representationClass, String humanPresentableName) {
        super();
        if (primaryType == null) {
            throw new NullPointerException("primaryType");
        }
        if (subType == null) {
            throw new NullPointerException("subType");
        }
        if (representationClass == null) {
            throw new NullPointerException("representationClass");
        }

        if (params == null) params = new MimeTypeParameterList();

        params.set("class", representationClass.getName());

        if (humanPresentableName == null) {
            humanPresentableName = params.get("humanPresentableName");

            if (humanPresentableName == null)
                humanPresentableName = primaryType + "/" + subType;
        }

        try {
            mimeType = new MimeType(primaryType, subType, params);
        } catch (MimeTypeParseException mtpe) {
            throw new IllegalArgumentException("MimeType Parse Exception: " + mtpe.getMessage());
        }


                    this.representationClass  = representationClass;
        this.humanPresentableName = humanPresentableName;

        mimeType.removeParameter("humanPresentableName");
    }

    /**
     * 构造一个表示 Java 类的 <code>DataFlavor</code>。
     * <p>
     * 返回的 <code>DataFlavor</code> 将具有以下特征：
     * <pre>
     *    representationClass = representationClass
     *    mimeType            = application/x-java-serialized-object
     * </pre>
     * @param representationClass 用于在此风味中传输数据的类
     * @param humanPresentableName 用于标识此风味的可读字符串；如果此参数为 <code>null</code>，
     *                 则使用 MIME 内容类型的值
     * @exception NullPointerException 如果 <code>representationClass</code> 为 null
     */
    public DataFlavor(Class<?> representationClass, String humanPresentableName) {
        this("application", "x-java-serialized-object", null, representationClass, humanPresentableName);
        if (representationClass == null) {
            throw new NullPointerException("representationClass");
        }
    }

    /**
     * 构造一个表示 <code>MimeType</code> 的 <code>DataFlavor</code>。
     * <p>
     * 返回的 <code>DataFlavor</code> 将具有以下特征：
     * <p>
     * 如果 <code>mimeType</code> 为
     * "application/x-java-serialized-object; class=&lt;representation class&gt;",
     * 结果与调用 <code>new DataFlavor(Class:forName(&lt;representation class&gt;)</code> 相同。
     * <p>
     * 否则：
     * <pre>
     *     representationClass = InputStream
     *     mimeType            = mimeType
     * </pre>
     * @param mimeType 用于标识此风味的 MIME 类型的字符串；
     *                 如果 <code>mimeType</code> 没有指定 "class=" 参数，或者类未成功加载，
     *                 则抛出 <code>IllegalArgumentException</code>
     * @param humanPresentableName 用于标识此风味的可读字符串；如果此参数为 <code>null</code>，
     *                 则使用 MIME 内容类型的值
     * @exception IllegalArgumentException 如果 <code>mimeType</code> 无效或类未成功加载
     * @exception NullPointerException 如果 <code>mimeType</code> 为 null
     */
    public DataFlavor(String mimeType, String humanPresentableName) {
        super();
        if (mimeType == null) {
            throw new NullPointerException("mimeType");
        }
        try {
            initialize(mimeType, humanPresentableName, this.getClass().getClassLoader());
        } catch (MimeTypeParseException mtpe) {
            throw new IllegalArgumentException("failed to parse:" + mimeType);
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException("can't find specified class: " + cnfe.getMessage());
        }
    }

    /**
     * 构造一个表示 <code>MimeType</code> 的 <code>DataFlavor</code>。
     * <p>
     * 返回的 <code>DataFlavor</code> 将具有以下特征：
     * <p>
     * 如果 mimeType 为
     * "application/x-java-serialized-object; class=&lt;representation class&gt;",
     * 结果与调用 <code>new DataFlavor(Class:forName(&lt;representation class&gt;)</code> 相同。
     * <p>
     * 否则：
     * <pre>
     *     representationClass = InputStream
     *     mimeType            = mimeType
     * </pre>
     * @param mimeType 用于标识此风味的 MIME 类型的字符串
     * @param humanPresentableName 用于标识此风味的可读字符串
     * @param classLoader 要使用的类加载器
     * @exception ClassNotFoundException 如果类未加载
     * @exception IllegalArgumentException 如果 <code>mimeType</code> 无效
     * @exception NullPointerException 如果 <code>mimeType</code> 为 null
     */
    public DataFlavor(String mimeType, String humanPresentableName, ClassLoader classLoader) throws ClassNotFoundException {
        super();
        if (mimeType == null) {
            throw new NullPointerException("mimeType");
        }
        try {
            initialize(mimeType, humanPresentableName, classLoader);
        } catch (MimeTypeParseException mtpe) {
            throw new IllegalArgumentException("failed to parse:" + mimeType);
        }
    }

    /**
     * 从 <code>mimeType</code> 字符串构造一个 <code>DataFlavor</code>。
     * 字符串可以指定 "class=&lt;fully specified Java class name&gt;" 参数来创建具有所需
     * 表示类的 <code>DataFlavor</code>。如果字符串不包含 "class=" 参数，
     * 则使用 <code>java.io.InputStream</code> 作为默认值。
     *
     * @param mimeType 用于标识此风味的 MIME 类型的字符串；
     *                 如果 "class=" 参数指定的类未成功加载，
     *                 则抛出 <code>ClassNotFoundException</code>
     * @exception ClassNotFoundException 如果类未加载
     * @exception IllegalArgumentException 如果 <code>mimeType</code> 无效
     * @exception NullPointerException 如果 <code>mimeType</code> 为 null
     */
    public DataFlavor(String mimeType) throws ClassNotFoundException {
        super();
        if (mimeType == null) {
            throw new NullPointerException("mimeType");
        }
        try {
            initialize(mimeType, null, this.getClass().getClassLoader());
        } catch (MimeTypeParseException mtpe) {
            throw new IllegalArgumentException("failed to parse:" + mimeType);
        }
    }

   /**
    * 从各种构造函数调用的公共初始化代码。
    *
    * @param mimeType MIME 内容类型（必须有 class= 参数）
    * @param humanPresentableName 可读名称或 <code>null</code>
    * @param classLoader 用于解析的备用类加载器
    *
    * @throws MimeTypeParseException
    * @throws ClassNotFoundException
    * @throws  NullPointerException 如果 <code>mimeType</code> 为 null
    *
    * @see #tryToLoadClass
    */
    private void initialize(String mimeType, String humanPresentableName, ClassLoader classLoader) throws MimeTypeParseException, ClassNotFoundException {
        if (mimeType == null) {
            throw new NullPointerException("mimeType");
        }

        this.mimeType = new MimeType(mimeType); // 抛出异常

        String rcn = getParameter("class");

        if (rcn == null) {
            if ("application/x-java-serialized-object".equals(this.mimeType.getBaseType()))

                throw new IllegalArgumentException("no representation class specified for:" + mimeType);
            else
                representationClass = java.io.InputStream.class; // 默认值
        } else { // 获取类名
            representationClass = DataFlavor.tryToLoadClass(rcn, classLoader);
        }

        this.mimeType.setParameter("class", representationClass.getName());

        if (humanPresentableName == null) {
            humanPresentableName = this.mimeType.getParameter("humanPresentableName");
            if (humanPresentableName == null)
                humanPresentableName = this.mimeType.getPrimaryType() + "/" + this.mimeType.getSubType();
        }

        this.humanPresentableName = humanPresentableName; // 设置它

        this.mimeType.removeParameter("humanPresentableName"); // 以防万一
    }

    /**
     * 此 <code>DataFlavor</code> 及其参数的字符串表示形式。结果的 <code>String</code> 包含
     * <code>DataFlavor</code> 类的名称、此风味的 MIME 类型及其表示类。如果此风味的主 MIME 类型为 "text"，
     * 支持 charset 参数，并且有编码表示形式，则风味的 charset 也包括在内。参见 <code>selectBestTextFlavor</code>
     * 以获取支持 charset 参数的文本风味列表。
     *
     * @return 此 <code>DataFlavor</code> 的字符串表示形式
     * @see #selectBestTextFlavor
     */
    public String toString() {
        String string = getClass().getName();
        string += "["+paramString()+"]";
        return string;
    }

    private String paramString() {
        String params = "";
        params += "mimetype=";
        if (mimeType == null) {
            params += "null";
        } else {
            params += mimeType.getBaseType();
        }
        params += ";representationclass=";
        if (representationClass == null) {
           params += "null";
        } else {
           params += representationClass.getName();
        }
        if (DataTransferer.isFlavorCharsetTextType(this) &&
            (isRepresentationClassInputStream() ||
             isRepresentationClassByteBuffer() ||
             byte[].class.equals(representationClass)))
        {
            params += ";charset=" + DataTransferer.getTextCharset(this);
        }
        return params;
    }

    /**
     * 返回一个表示带有 Unicode 编码的纯文本的 <code>DataFlavor</code>，其中：
     * <pre>
     *     representationClass = java.io.InputStream
     *     mimeType            = "text/plain;
     *                            charset=&lt;平台默认 Unicode 编码&gt;"
     * </pre>
     * Sun 的 Microsoft Windows 实现使用编码 <code>utf-16le</code>。
     * Sun 的 Solaris 和 Linux 实现使用编码 <code>iso-10646-ucs-2</code>。
     *
     * @return 一个表示带有 Unicode 编码的纯文本的 <code>DataFlavor</code>
     * @since 1.3
     */
    public static final DataFlavor getTextPlainUnicodeFlavor() {
        String encoding = null;
        DataTransferer transferer = DataTransferer.getInstance();
        if (transferer != null) {
            encoding = transferer.getDefaultUnicodeEncoding();
        }
        return new DataFlavor(
            "text/plain;charset="+encoding
            +";class=java.io.InputStream", "Plain Text");
    }

    /**
     * 从 <code>DataFlavor</code> 数组中选择最佳的文本 <code>DataFlavor</code>。仅考虑
     * <code>DataFlavor.stringFlavor</code> 和等效风味，以及主 MIME 类型为 "text" 的风味。
     * <p>
     * 风味首先按其 MIME 类型按以下顺序排序：
     * <ul>
     * <li>"text/sgml"
     * <li>"text/xml"
     * <li>"text/html"
     * <li>"text/rtf"
     * <li>"text/enriched"
     * <li>"text/richtext"
     * <li>"text/uri-list"
     * <li>"text/tab-separated-values"
     * <li>"text/t140"
     * <li>"text/rfc822-headers"
     * <li>"text/parityfec"
     * <li>"text/directory"
     * <li>"text/css"
     * <li>"text/calendar"
     * <li>"application/x-java-serialized-object"
     * <li>"text/plain"
     * <li>"text/&lt;其他&gt;"
     * </ul>
     * <p>例如，"text/sgml" 优先于 "text/html"，而 <code>DataFlavor.stringFlavor</code>
     * 优先于 <code>DataFlavor.plainTextFlavor</code>。
     * <p>
     * 如果数组中有两个或多个风味共享最佳 MIME 类型，则将检查该 MIME 类型是否支持 charset 参数。
     * <p>
     * 以下 MIME 类型支持或被视为支持 charset 参数：
     * <ul>
     * <li>"text/sgml"
     * <li>"text/xml"
     * <li>"text/html"
     * <li>"text/enriched"
     * <li>"text/richtext"
     * <li>"text/uri-list"
     * <li>"text/directory"
     * <li>"text/css"
     * <li>"text/calendar"
     * <li>"application/x-java-serialized-object"
     * <li>"text/plain"
     * </ul>
     * 以下 MIME 类型不支持或被视为不支持 charset 参数：
     * <ul>
     * <li>"text/rtf"
     * <li>"text/tab-separated-values"
     * <li>"text/t140"
     * <li>"text/rfc822-headers"
     * <li>"text/parityfec"
     * </ul>
     * 对于 "text/&lt;其他&gt;" MIME 类型，JRE 首次需要确定 MIME 类型是否支持 charset 参数时，
     * 将检查是否在任意选择的使用该 MIME 类型的 <code>DataFlavor</code> 中明确列出了该参数。
     * 如果如此，JRE 将从此假设该 MIME 类型支持 charset 参数，并不再检查。如果未明确列出，
     * JRE 将从此假设该 MIME 类型不支持 charset 参数，并不再检查。因为此检查是在任意选择的
     * <code>DataFlavor</code> 上进行的，开发人员必须确保所有具有 "text/&lt;其他&gt;" MIME 类型的
     * <code>DataFlavor</code> 指定 charset 参数（如果该 MIME 类型支持该参数）。开发人员不应依赖
     * JRE 为 "text/&lt;其他&gt;" DataFlavor 替换平台的默认 charset。不遵守此限制将导致未定义的行为。
     * <p>
     * 如果数组中的最佳 MIME 类型不支持 charset 参数，则将按以下顺序对共享该 MIME 类型的风味进行排序：
     * <code>java.io.InputStream</code>，<code>java.nio.ByteBuffer</code>，
     * <code>[B</code>，&lt;所有其他&gt;。
     * <p>
     * 如果两个或多个风味共享最佳表示类，或者没有风味具有三个指定的表示类之一，则将非确定性地选择其中一个风味。
     * <p>
     * 如果数组中的最佳 MIME 类型支持 charset 参数，则将按以下顺序对共享该 MIME 类型的风味进行排序：
     * <code>java.io.Reader</code>，<code>java.lang.String</code>，
     * <code>java.nio.CharBuffer</code>，<code>[C</code>，&lt;所有其他&gt;。
     * <p>
     * 如果两个或多个风味共享最佳表示类，并且该表示类是四个明确列出的表示类之一，则将非确定性地选择其中一个风味。
     * 然而，如果没有任何风味具有四个指定的表示类之一，则将按以下顺序对风味进行排序：Unicode 字符集（如 "UTF-16"、
     * "UTF-8"、"UTF-16BE"、"UTF-16LE" 及其别名）被认为是最佳的。之后，选择平台默认字符集及其别名。
     * "US-ASCII" 及其别名是最差的。所有其他字符集按字母顺序选择，但仅考虑此 Java 平台实现支持的字符集。
     * <p>
     * 如果两个或多个风味共享最佳字符集，则将再次按以下顺序对风味进行排序：
     * <code>java.io.InputStream</code>，<code>java.nio.ByteBuffer</code>，
     * <code>[B</code>，&lt;所有其他&gt;。
     * <p>
     * 如果两个或多个风味共享最佳表示类，或者没有风味具有三个指定的表示类之一，则将非确定性地选择其中一个风味。
     *
     * @param availableFlavors 可用的 <code>DataFlavor</code> 数组
     * @return 根据上述规则选择的最佳（最高保真度）风味，或者如果 <code>availableFlavors</code> 为 <code>null</code>、
     *         长度为零或不包含任何文本风味，则返回 <code>null</code>
     * @since 1.3
     */
    public static final DataFlavor selectBestTextFlavor(
                                       DataFlavor[] availableFlavors) {
        if (availableFlavors == null || availableFlavors.length == 0) {
            return null;
        }


                    if (textFlavorComparator == null) {
            textFlavorComparator = new TextFlavorComparator();
        }

        DataFlavor bestFlavor =
            (DataFlavor)Collections.max(Arrays.asList(availableFlavors),
                                        textFlavorComparator);

        if (!bestFlavor.isFlavorTextType()) {
            return null;
        }

        return bestFlavor;
    }

    private static Comparator<DataFlavor> textFlavorComparator;

    static class TextFlavorComparator
        extends DataTransferer.DataFlavorComparator {

        /**
         * 比较两个 <code>DataFlavor</code> 对象。如果第一个 <code>DataFlavor</code>
         * 比第二个差，返回负整数；如果相等，返回零；如果更好，返回正整数。
         * <p>
         * <code>DataFlavor</code> 的排序规则遵循 <code>selectBestTextFlavor</code>
         * 中的规则。
         *
         * @param obj1 要比较的第一个 <code>DataFlavor</code>
         * @param obj2 要比较的第二个 <code>DataFlavor</code>
         * @return 如果第一个参数比第二个差，返回负整数；如果相等，返回零；如果更好，返回正整数
         * @throws ClassCastException 如果任一参数不是 <code>DataFlavor</code> 的实例
         * @throws NullPointerException 如果任一参数为 <code>null</code>
         *
         * @see #selectBestTextFlavor
         */
        public int compare(Object obj1, Object obj2) {
            DataFlavor flavor1 = (DataFlavor)obj1;
            DataFlavor flavor2 = (DataFlavor)obj2;

            if (flavor1.isFlavorTextType()) {
                if (flavor2.isFlavorTextType()) {
                    return super.compare(obj1, obj2);
                } else {
                    return 1;
                }
            } else if (flavor2.isFlavorTextType()) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    /**
     * 获取一个文本格式的 <code>Reader</code>，必要时解码为预期的字符集（编码）。支持的表示类有
     * <code>java.io.Reader</code>、<code>java.lang.String</code>、
     * <code>java.nio.CharBuffer</code>、<code>[C</code>、
     * <code>java.io.InputStream</code>、<code>java.nio.ByteBuffer</code> 和 <code>[B</code>。
     * <p>
     * 由于不支持字符集参数的文本格式以非标准格式编码，因此不应为此类格式调用此方法。然而，为了保持向后兼容性，
     * 如果为此类格式调用此方法，此方法将视其支持字符集参数并尝试相应地解码。参见 <code>selectBestTextFlavor</code>
     * 以获取不支持字符集参数的文本格式列表。
     *
     * @param transferable 要在此格式中请求数据的 <code>Transferable</code>
     *
     * @return 用于读取 <code>Transferable</code> 数据的 <code>Reader</code>
     *
     * @exception IllegalArgumentException 如果表示类不是上述七种之一
     * @exception IllegalArgumentException 如果 <code>Transferable</code> 的数据为 <code>null</code>
     * @exception NullPointerException 如果 <code>Transferable</code> 为 <code>null</code>
     * @exception UnsupportedEncodingException 如果此格式的表示类为 <code>java.io.InputStream</code>、
     *            <code>java.nio.ByteBuffer</code> 或 <code>[B</code>，且此格式的编码不受此 Java 平台实现支持
     * @exception UnsupportedFlavorException 如果 <code>Transferable</code> 不支持此格式
     * @exception IOException 如果由于 I/O 错误无法读取数据
     * @see #selectBestTextFlavor
     * @since 1.3
     */
    public Reader getReaderForText(Transferable transferable)
        throws UnsupportedFlavorException, IOException
    {
        Object transferObject = transferable.getTransferData(this);
        if (transferObject == null) {
            throw new IllegalArgumentException
                ("getTransferData() returned null");
        }

        if (transferObject instanceof Reader) {
            return (Reader)transferObject;
        } else if (transferObject instanceof String) {
            return new StringReader((String)transferObject);
        } else if (transferObject instanceof CharBuffer) {
            CharBuffer buffer = (CharBuffer)transferObject;
            int size = buffer.remaining();
            char[] chars = new char[size];
            buffer.get(chars, 0, size);
            return new CharArrayReader(chars);
        } else if (transferObject instanceof char[]) {
            return new CharArrayReader((char[])transferObject);
        }

        InputStream stream = null;

        if (transferObject instanceof InputStream) {
            stream = (InputStream)transferObject;
        } else if (transferObject instanceof ByteBuffer) {
            ByteBuffer buffer = (ByteBuffer)transferObject;
            int size = buffer.remaining();
            byte[] bytes = new byte[size];
            buffer.get(bytes, 0, size);
            stream = new ByteArrayInputStream(bytes);
        } else if (transferObject instanceof byte[]) {
            stream = new ByteArrayInputStream((byte[])transferObject);
        }

        if (stream == null) {
            throw new IllegalArgumentException("transfer data is not Reader, String, CharBuffer, char array, InputStream, ByteBuffer, or byte array");
        }

        String encoding = getParameter("charset");
        return (encoding == null)
            ? new InputStreamReader(stream)
            : new InputStreamReader(stream, encoding);
    }

    /**
     * 返回此 <code>DataFlavor</code> 的 MIME 类型字符串。
     * @return 此格式的 MIME 类型字符串
     */
    public String getMimeType() {
        return (mimeType != null) ? mimeType.toString() : null;
    }

    /**
     * 返回支持此 <code>DataFlavor</code> 的对象在请求此 <code>DataFlavor</code> 时将返回的 <code>Class</code>。
     * @return 支持此 <code>DataFlavor</code> 的对象在请求此 <code>DataFlavor</code> 时将返回的 <code>Class</code>
     */
    public Class<?> getRepresentationClass() {
        return representationClass;
    }

    /**
     * 返回此 <code>DataFlavor</code> 代表的数据格式的人类可读名称。此名称将根据不同的国家进行本地化。
     * @return 此 <code>DataFlavor</code> 代表的数据格式的人类可读名称
     */
    public String getHumanPresentableName() {
        return humanPresentableName;
    }

    /**
     * 返回此 <code>DataFlavor</code> 的主 MIME 类型。
     * @return 此 <code>DataFlavor</code> 的主 MIME 类型
     */
    public String getPrimaryType() {
        return (mimeType != null) ? mimeType.getPrimaryType() : null;
    }

    /**
     * 返回此 <code>DataFlavor</code> 的子 MIME 类型。
     * @return 此 <code>DataFlavor</code> 的子 MIME 类型
     */
    public String getSubType() {
        return (mimeType != null) ? mimeType.getSubType() : null;
    }

    /**
     * 如果 <code>paramName</code> 等于 "humanPresentableName"，则返回此 <code>DataFlavor</code>
     * 的人类可读名称。否则返回与 <code>paramName</code> 关联的 MIME 类型值。
     *
     * @param paramName 请求的参数名称
     * @return 参数名称的值，如果没有关联的值则返回 <code>null</code>
     */
    public String getParameter(String paramName) {
        if (paramName.equals("humanPresentableName")) {
            return humanPresentableName;
        } else {
            return (mimeType != null)
                ? mimeType.getParameter(paramName) : null;
        }
    }

    /**
     * 设置此 <code>DataFlavor</code> 代表的数据格式的人类可读名称。此名称将根据不同的国家进行本地化。
     * @param humanPresentableName 新的人类可读名称
     */
    public void setHumanPresentableName(String humanPresentableName) {
        this.humanPresentableName = humanPresentableName;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <code>DataFlavor</code> 类的相等比较实现如下：两个 <code>DataFlavor</code> 被认为相等当且仅当它们的
     * MIME 主类型、子类型和表示类相等。此外，如果主类型为 "text"，子类型表示支持字符集参数的文本格式，
     * 且表示类不是 <code>java.io.Reader</code>、<code>java.lang.String</code>、
     * <code>java.nio.CharBuffer</code> 或 <code>[C</code>，则字符集参数也必须相等。
     * 如果没有为一个或两个 <code>DataFlavor</code> 显式指定字符集，则假定使用平台默认编码。参见
     * <code>selectBestTextFlavor</code> 以获取支持字符集参数的文本格式列表。
     *
     * @param o 要与 <code>this</code> 比较的 <code>Object</code>
     * @return 如果 <code>that</code> 与此 <code>DataFlavor</code> 相等，则返回 <code>true</code>；
     *         否则返回 <code>false</code>
     * @see #selectBestTextFlavor
     */
    public boolean equals(Object o) {
        return ((o instanceof DataFlavor) && equals((DataFlavor)o));
    }

    /**
     * 该方法的行为与 {@link #equals(Object)} 相同。唯一的区别是它接受一个 <code>DataFlavor</code> 实例作为参数。
     *
     * @param that 要与 <code>this</code> 比较的 <code>DataFlavor</code>
     * @return 如果 <code>that</code> 与此 <code>DataFlavor</code> 相等，则返回 <code>true</code>；
     *         否则返回 <code>false</code>
     * @see #selectBestTextFlavor
     */
    public boolean equals(DataFlavor that) {
        if (that == null) {
            return false;
        }
        if (this == that) {
            return true;
        }

        if (!Objects.equals(this.getRepresentationClass(), that.getRepresentationClass())) {
            return false;
        }

        if (mimeType == null) {
            if (that.mimeType != null) {
                return false;
            }
        } else {
            if (!mimeType.match(that.mimeType)) {
                return false;
            }

            if ("text".equals(getPrimaryType())) {
                if (DataTransferer.doesSubtypeSupportCharset(this)
                        && representationClass != null
                        && !isStandardTextRepresentationClass()) {
                    String thisCharset =
                            DataTransferer.canonicalName(this.getParameter("charset"));
                    String thatCharset =
                            DataTransferer.canonicalName(that.getParameter("charset"));
                    if (!Objects.equals(thisCharset, thatCharset)) {
                        return false;
                    }
                }

                if ("html".equals(getSubType())) {
                    String thisDocument = this.getParameter("document");
                    String thatDocument = that.getParameter("document");
                    if (!Objects.equals(thisDocument, thatDocument)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * 仅比较传递的 <code>String</code> 和 <code>representationClass</code>，不考虑 <code>representationClass</code>。
     *
     * 如果需要比较 <code>representationClass</code>，则可以使用 <code>equals(new DataFlavor(s))</code>。
     * @deprecated 与 <code>hashCode()</code> 合约不一致，改用 <code>isMimeTypeEqual(String)</code>。
     * @param s 要比较的 {@code mimeType}。
     * @return 如果字符串（MimeType）相等，则返回 true；否则返回 false 或者如果 {@code s} 为 {@code null} 则返回 false
     */
    @Deprecated
    public boolean equals(String s) {
        if (s == null || mimeType == null)
            return false;
        return isMimeTypeEqual(s);
    }

    /**
     * 返回此 <code>DataFlavor</code> 的哈希码。
     * 对于两个相等的 <code>DataFlavor</code>，哈希码也相等。
     * 对于与 <code>DataFlavor.equals(String)</code> 匹配的 <code>String</code>，
     * 不保证 <code>DataFlavor</code> 的哈希码等于 <code>String</code> 的哈希码。
     *
     * @return 此 <code>DataFlavor</code> 的哈希码
     */
    public int hashCode() {
        int total = 0;

        if (representationClass != null) {
            total += representationClass.hashCode();
        }

        if (mimeType != null) {
            String primaryType = mimeType.getPrimaryType();
            if (primaryType != null) {
                total += primaryType.hashCode();
            }

            // 不将 subType.hashCode() 加到 total 中。equals 使用 MimeType.match，该方法报告匹配
            // 如果一个或两个 subType 是 '*'，则无论另一个 subType 是什么，都报告匹配。

            if ("text".equals(primaryType)) {
                if (DataTransferer.doesSubtypeSupportCharset(this)
                        && representationClass != null
                        && !isStandardTextRepresentationClass()) {
                    String charset = DataTransferer.canonicalName(getParameter("charset"));
                    if (charset != null) {
                        total += charset.hashCode();
                    }
                }

                if ("html".equals(getSubType())) {
                    String document = this.getParameter("document");
                    if (document != null) {
                        total += document.hashCode();
                    }
                }
            }
        }

        return total;
    }

    /**
     * 与 {@link #equals(DataFlavor)} 相同。
     *
     * @param that 要与 <code>this</code> 比较的 <code>DataFlavor</code>
     * @return 如果 <code>that</code> 与此 <code>DataFlavor</code> 相等，则返回 <code>true</code>；
     *         否则返回 <code>false</code>
     * @see #selectBestTextFlavor
     * @since 1.3
     */
    public boolean match(DataFlavor that) {
        return equals(that);
    }

    /**
     * 返回传递的 MIME 类型字符串表示是否与此 <code>DataFlavor</code> 的 MIME 类型等效。
     * 参数不包括在比较中。
     *
     * @param mimeType MIME 类型的字符串表示
     * @return 如果传递的 MIME 类型字符串表示与此 <code>DataFlavor</code> 的 MIME 类型等效，则返回 true；
     *         否则返回 false
     * @throws NullPointerException 如果 mimeType 为 <code>null</code>
     */
    public boolean isMimeTypeEqual(String mimeType) {
        // JCK Test DataFlavor0117: 如果 'mimeType' 为 null，抛出 NPE
        if (mimeType == null) {
            throw new NullPointerException("mimeType");
        }
        if (this.mimeType == null) {
            return false;
        }
        try {
            return this.mimeType.match(new MimeType(mimeType));
        } catch (MimeTypeParseException mtpe) {
            return false;
        }
    }


                /**
     * 比较两个 <code>DataFlavor</code> 对象的 <code>mimeType</code>。
     * 不考虑参数。
     *
     * @param dataFlavor 要比较的 <code>DataFlavor</code>
     * @return 如果 <code>MimeType</code> 相等，则返回 true，否则返回 false
     */

    public final boolean isMimeTypeEqual(DataFlavor dataFlavor) {
        return isMimeTypeEqual(dataFlavor.mimeType);
    }

    /**
     * 比较两个 <code>DataFlavor</code> 对象的 <code>mimeType</code>。
     * 不考虑参数。
     *
     * @return 如果 <code>MimeType</code> 相等，则返回 true，否则返回 false
     */

    private boolean isMimeTypeEqual(MimeType mtype) {
        if (this.mimeType == null) {
            return (mtype == null);
        }
        return mimeType.match(mtype);
    }

    /**
     * 检查表示类是否是标准文本表示类之一。
     *
     * @return 如果表示类是标准文本表示类之一，则返回 true，否则返回 false
     */
    private boolean isStandardTextRepresentationClass() {
        return isRepresentationClassReader()
                || String.class.equals(representationClass)
                || isRepresentationClassCharBuffer()
                || char[].class.equals(representationClass);
    }

   /**
    * <code>DataFlavor</code> 是否表示一个序列化对象？
    */

    public boolean isMimeTypeSerializedObject() {
        return isMimeTypeEqual(javaSerializedObjectMimeType);
    }

    public final Class<?> getDefaultRepresentationClass() {
        return ioInputStreamClass;
    }

    public final String getDefaultRepresentationClassAsString() {
        return getDefaultRepresentationClass().getName();
    }

   /**
    * <code>DataFlavor</code> 是否表示一个 <code>java.io.InputStream</code>？
    */

    public boolean isRepresentationClassInputStream() {
        return ioInputStreamClass.isAssignableFrom(representationClass);
    }

    /**
     * 返回此 <code>DataFlavor</code> 的表示类是否为 <code>java.io.Reader</code> 或其子类。
     *
     * @since 1.4
     */
    public boolean isRepresentationClassReader() {
        return java.io.Reader.class.isAssignableFrom(representationClass);
    }

    /**
     * 返回此 <code>DataFlavor</code> 的表示类是否为 <code>java.nio.CharBuffer</code> 或其子类。
     *
     * @since 1.4
     */
    public boolean isRepresentationClassCharBuffer() {
        return java.nio.CharBuffer.class.isAssignableFrom(representationClass);
    }

    /**
     * 返回此 <code>DataFlavor</code> 的表示类是否为 <code>java.nio.ByteBuffer</code> 或其子类。
     *
     * @since 1.4
     */
    public boolean isRepresentationClassByteBuffer() {
        return java.nio.ByteBuffer.class.isAssignableFrom(representationClass);
    }

   /**
    * 返回表示类是否可以序列化。
    * @return 如果表示类可以序列化，则返回 true
    */

    public boolean isRepresentationClassSerializable() {
        return java.io.Serializable.class.isAssignableFrom(representationClass);
    }

   /**
    * 返回表示类是否为 <code>Remote</code>。
    * @return 如果表示类为 <code>Remote</code>，则返回 true
    */

    public boolean isRepresentationClassRemote() {
        return DataTransferer.isRemote(representationClass);
    }

   /**
    * 返回指定的 <code>DataFlavor</code> 是否表示一个序列化对象。
    * @return 如果指定的 <code>DataFlavor</code> 表示一个序列化对象，则返回 true
    */

    public boolean isFlavorSerializedObjectType() {
        return isRepresentationClassSerializable() && isMimeTypeEqual(javaSerializedObjectMimeType);
    }

    /**
     * 返回指定的 <code>DataFlavor</code> 是否表示一个远程对象。
     * @return 如果指定的 <code>DataFlavor</code> 表示一个远程对象，则返回 true
     */

    public boolean isFlavorRemoteObjectType() {
        return isRepresentationClassRemote()
            && isRepresentationClassSerializable()
            && isMimeTypeEqual(javaRemoteObjectMimeType);
    }


   /**
    * 返回指定的 <code>DataFlavor</code> 是否表示一个文件对象列表。
    * @return 如果指定的 <code>DataFlavor</code> 表示一个文件对象列表，则返回 true
    */

   public boolean isFlavorJavaFileListType() {
        if (mimeType == null || representationClass == null)
            return false;
        return java.util.List.class.isAssignableFrom(representationClass) &&
               mimeType.match(javaFileListFlavor.mimeType);

   }

    /**
     * 返回此 <code>DataFlavor</code> 是否是此 Java 平台实现的有效文本类型。只有等效于 <code>DataFlavor.stringFlavor</code> 和具有 "text" 主 MIME 类型的 <code>DataFlavor</code> 可以是有效的文本类型。
     * <p>
     * 如果此类型支持 charset 参数，则必须等效于 <code>DataFlavor.stringFlavor</code>，或者其表示必须是 <code>java.io.Reader</code>、<code>java.lang.String</code>、<code>java.nio.CharBuffer</code>、<code>[C</code>、<code>java.io.InputStream</code>、<code>java.nio.ByteBuffer</code> 或 <code>[B</code>。如果表示是 <code>java.io.InputStream</code>、<code>java.nio.ByteBuffer</code> 或 <code>[B</code>，则此类型的 <code>charset</code> 参数必须由此 Java 平台实现支持。如果没有指定 charset，则假定使用平台默认 charset，该 charset 始终受支持。
     * <p>
     * 如果此类型不支持 charset 参数，其表示必须是 <code>java.io.InputStream</code>、<code>java.nio.ByteBuffer</code> 或 <code>[B</code>。
     * <p>
     * 请参阅 <code>selectBestTextFlavor</code> 以获取支持 charset 参数的文本类型的列表。
     *
     * @return 如果此 <code>DataFlavor</code> 是如上所述的有效文本类型，则返回 <code>true</code>；否则返回 <code>false</code>
     * @see #selectBestTextFlavor
     * @since 1.4
     */
    public boolean isFlavorTextType() {
        return (DataTransferer.isFlavorCharsetTextType(this) ||
                DataTransferer.isFlavorNoncharsetTextType(this));
    }

   /**
    * 序列化此 <code>DataFlavor</code>。
    */

   public synchronized void writeExternal(ObjectOutput os) throws IOException {
       if (mimeType != null) {
           mimeType.setParameter("humanPresentableName", humanPresentableName);
           os.writeObject(mimeType);
           mimeType.removeParameter("humanPresentableName");
       } else {
           os.writeObject(null);
       }

       os.writeObject(representationClass);
   }

   /**
    * 从序列化状态恢复此 <code>DataFlavor</code>。
    */

   public synchronized void readExternal(ObjectInput is) throws IOException , ClassNotFoundException {
       String rcn = null;
        mimeType = (MimeType)is.readObject();

        if (mimeType != null) {
            humanPresentableName =
                mimeType.getParameter("humanPresentableName");
            mimeType.removeParameter("humanPresentableName");
            rcn = mimeType.getParameter("class");
            if (rcn == null) {
                throw new IOException("no class parameter specified in: " +
                                      mimeType);
            }
        }

        try {
            representationClass = (Class)is.readObject();
        } catch (OptionalDataException ode) {
            if (!ode.eof || ode.length != 0) {
                throw ode;
            }
            // 确保向后兼容。
            // 旧版本不会将表示类写入流中。
            if (rcn != null) {
                representationClass =
                    DataFlavor.tryToLoadClass(rcn, getClass().getClassLoader());
            }
        }
   }

   /**
    * 返回此 <code>DataFlavor</code> 的克隆。
    * @return 此 <code>DataFlavor</code> 的克隆
    */

    public Object clone() throws CloneNotSupportedException {
        Object newObj = super.clone();
        if (mimeType != null) {
            ((DataFlavor)newObj).mimeType = (MimeType)mimeType.clone();
        }
        return newObj;
    } // clone()

   /**
    * 对每个 MIME 类型参数调用 <code>DataFlavor</code>，以允许 <code>DataFlavor</code> 子类处理特殊参数，如 text/plain 的 <code>charset</code> 参数，其值不区分大小写。（MIME 类型参数值应该是区分大小写的。）
    * <p>
    * 此方法应为每个参数名称/值对返回 <code>parameterValue</code> 的规范化表示。
    *
    * 从 1.1 版本开始，此实现不再调用此方法。
    *
    * @deprecated
    */
    @Deprecated
    protected String normalizeMimeTypeParameter(String parameterName, String parameterValue) {
        return parameterValue;
    }

   /**
    * 对每个 MIME 类型字符串调用，以允许 <code>DataFlavor</code> 子类型更改 MIME 类型的规范化方式。一个可能的用途是在传入的 MIME 类型字符串中没有参数时添加默认的参数/值对。
    *
    * 从 1.1 版本开始，此实现不再调用此方法。
    *
    * @deprecated
    */
    @Deprecated
    protected String normalizeMimeType(String mimeType) {
        return mimeType;
    }

    /*
     * 字段
     */

    /* 用于缓存任何平台特定的风味数据的占位符 */

    transient int       atom;

    /* DataFlavor 的 MIME 类型 */

    MimeType            mimeType;

    private String      humanPresentableName;

    /** 此 DataFlavor 表示的对象的 Java 类 **/

    private Class<?>       representationClass;

} // class DataFlavor
