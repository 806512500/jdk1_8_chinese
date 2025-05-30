/*
 * Copyright (c) 1995, 2014, Oracle and/or its affiliates. All rights reserved.
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
package java.awt;

import java.awt.peer.LabelPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.accessibility.*;

/**
 * 一个 <code>Label</code> 对象是一个用于在容器中放置文本的组件。标签显示一行只读文本。
 * 该文本可以由应用程序更改，但用户不能直接编辑它。
 * <p>
 * 例如，代码&nbsp;.&nbsp;.&nbsp;.
 *
 * <hr><blockquote><pre>
 * setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
 * add(new Label("Hi There!"));
 * add(new Label("Another Label"));
 * </pre></blockquote><hr>
 * <p>
 * 生成以下标签：
 * <p>
 * <img src="doc-files/Label-1.gif" alt="两个标签: 'Hi There!' 和 'Another label'"
 * style="float:center; margin: 7px 10px;">
 *
 * @author      Sami Shaio
 * @since       JDK1.0
 */
public class Label extends Component implements Accessible {

    static {
        /* 确保必要的本地库已加载 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * 表示标签应左对齐。
     */
    public static final int LEFT        = 0;

    /**
     * 表示标签应居中对齐。
     */
    public static final int CENTER      = 1;

    /**
     * 表示标签应右对齐。
     * @since   JDK1.0t.
     */
    public static final int RIGHT       = 2;

    /**
     * 此标签的文本。
     * 该文本可以由程序修改，但用户不能修改。
     *
     * @serial
     * @see #getText()
     * @see #setText(String)
     */
    String text;

    /**
     * 标签的对齐方式。默认对齐方式设置为左对齐。
     *
     * @serial
     * @see #getAlignment()
     * @see #setAlignment(int)
     */
    int    alignment = LEFT;

    private static final String base = "label";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = 3094126758329070636L;

    /**
     * 构造一个空标签。
     * 标签的文本是空字符串 <code>""</code>。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public Label() throws HeadlessException {
        this("", LEFT);
    }

    /**
     * 构造一个新标签，显示指定的文本字符串，左对齐。
     * @param text 标签显示的字符串。
     *        一个 <code>null</code> 值
     *        将被接受，不会引发 NullPointerException。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public Label(String text) throws HeadlessException {
        this(text, LEFT);
    }

    /**
     * 构造一个新标签，显示指定的文本字符串，并具有指定的对齐方式。
     * <code>alignment</code> 的可能值为 <code>Label.LEFT</code>、
     * <code>Label.RIGHT</code> 和 <code>Label.CENTER</code>。
     * @param text 标签显示的字符串。
     *        一个 <code>null</code> 值
     *        将被接受，不会引发 NullPointerException。
     * @param     alignment   对齐方式值。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public Label(String text, int alignment) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        this.text = text;
        setAlignment(alignment);
    }

    /**
     * 从对象输入流中读取标签。
     * @exception HeadlessException 如果
     * <code>GraphicsEnvironment.isHeadless()</code> 返回
     * <code>true</code>
     * @serial
     * @since 1.4
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException, HeadlessException {
        GraphicsEnvironment.checkHeadless();
        s.defaultReadObject();
    }

    /**
     * 为这个组件构建一个名称。当名称为 <code>null</code> 时，由 getName() 调用。
     */
    String constructComponentName() {
        synchronized (Label.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 为这个标签创建一个对等体。对等体允许我们修改标签的外观而不改变其功能。
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createLabel(this);
            super.addNotify();
        }
    }

    /**
     * 获取此标签的当前对齐方式。可能的值为
     * <code>Label.LEFT</code>、<code>Label.RIGHT</code> 和
     * <code>Label.CENTER</code>。
     * @see        java.awt.Label#setAlignment
     */
    public int getAlignment() {
        return alignment;
    }

    /**
     * 设置此标签的对齐方式为指定的对齐方式。
     * 可能的值为 <code>Label.LEFT</code>、
     * <code>Label.RIGHT</code> 和 <code>Label.CENTER</code>。
     * @param      alignment    要设置的对齐方式。
     * @exception  IllegalArgumentException 如果给定的 <code>alignment</code> 值不正确。
     * @see        java.awt.Label#getAlignment
     */
    public synchronized void setAlignment(int alignment) {
        switch (alignment) {
          case LEFT:
          case CENTER:
          case RIGHT:
            this.alignment = alignment;
            LabelPeer peer = (LabelPeer)this.peer;
            if (peer != null) {
                peer.setAlignment(alignment);
            }
            return;
        }
        throw new IllegalArgumentException("improper alignment: " + alignment);
    }

    /**
     * 获取此标签的文本。
     * @return     此标签的文本，如果文本被设置为 <code>null</code>，则返回 <code>null</code>。
     * @see        java.awt.Label#setText
     */
    public String getText() {
        return text;
    }

    /**
     * 将此标签的文本设置为指定的文本。
     * @param      text 此标签显示的文本。如果
     *             <code>text</code> 为 <code>null</code>，则在显示时将其视为空字符串 <code>""</code>。
     * @see        java.awt.Label#getText
     */
    public void setText(String text) {
        boolean testvalid = false;
        synchronized (this) {
            if (text != this.text && (this.text == null ||
                                      !this.text.equals(text))) {
                this.text = text;
                LabelPeer peer = (LabelPeer)this.peer;
                if (peer != null) {
                    peer.setText(text);
                }
                testvalid = true;
            }
        }

        // 这可能会改变组件的首选大小。
        if (testvalid) {
            invalidateIfValid();
        }
    }

    /**
     * 返回表示此 <code>Label</code> 状态的字符串。
     * 此方法仅用于调试目的，返回字符串的内容和格式可能在不同实现之间有所不同。返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return     此标签的参数字符串
     */
    protected String paramString() {
        String align = "";
        switch (alignment) {
            case LEFT:   align = "left"; break;
            case CENTER: align = "center"; break;
            case RIGHT:  align = "right"; break;
        }
        return super.paramString() + ",align=" + align + ",text=" + text;
    }

    /**
     * 初始化 JNI 字段和方法 ID
     */
    private static native void initIDs();


/////////////////
// Accessibility support
////////////////


    /**
     * 获取与此标签关联的 AccessibleContext。
     * 对于标签，AccessibleContext 的形式为 AccessibleAWTLabel。
     * 如果必要，将创建一个新的 AccessibleAWTLabel 实例。
     *
     * @return 一个 AccessibleAWTLabel，作为此标签的 AccessibleContext
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTLabel();
        }
        return accessibleContext;
    }

    /**
     * 为 <code>Label</code> 类实现辅助功能支持。
     * 它为标签用户界面元素提供了 Java 辅助功能 API 的实现。
     * @since 1.3
     */
    protected class AccessibleAWTLabel extends AccessibleAWTComponent
    {
        /*
         * JDK 1.3 serialVersionUID
         */
        private static final long serialVersionUID = -3568967560160480438L;

        public AccessibleAWTLabel() {
            super();
        }

        /**
         * 获取此对象的辅助功能名称。
         *
         * @return 对象的本地化名称 -- 如果此对象没有名称，则可以为 null
         * @see AccessibleContext#setAccessibleName
         */
        public String getAccessibleName() {
            if (accessibleName != null) {
                return accessibleName;
            } else {
                if (getText() == null) {
                    return super.getAccessibleName();
                } else {
                    return getText();
                }
            }
        }

        /**
         * 获取此对象的角色。
         *
         * @return 一个 AccessibleRole 实例，描述对象的角色
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.LABEL;
        }

    } // inner class AccessibleAWTLabel

}
