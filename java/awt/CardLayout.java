
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.IOException;

/**
 * <code>CardLayout</code> 对象是一个容器的布局管理器。它将容器中的每个组件视为一张卡片。一次只有一张卡片可见，容器充当卡片堆。第一个添加到 <code>CardLayout</code> 对象中的组件是在容器首次显示时可见的组件。
 * <p>
 * 卡片的顺序由容器内部组件对象的顺序决定。 <code>CardLayout</code> 定义了一组方法，允许应用程序按顺序翻阅这些卡片，或显示指定的卡片。 {@link CardLayout#addLayoutComponent} 方法可用于将字符串标识符与给定卡片关联，以便快速随机访问。
 *
 * @author      Arthur van Hoff
 * @see         java.awt.Container
 * @since       JDK1.0
 */

public class CardLayout implements LayoutManager2,
                                   Serializable {

    private static final long serialVersionUID = -4328196481005934313L;

    /*
     * 这创建了一个 Vector 用于存储组件及其名称的关联对。
     * @see java.util.Vector
     */
    Vector<Card> vector = new Vector<>();

    /*
     * 一个包含组件及其名称的字符串对。
     */
    class Card implements Serializable {
        static final long serialVersionUID = 6640330810709497518L;
        public String name;
        public Component comp;
        public Card(String cardName, Component cardComponent) {
            name = cardName;
            comp = cardComponent;
        }
    }

    /*
     * 当前由 CardLayout 显示的组件的索引。
     */
    int currentCard = 0;


    /*
    * 卡片的水平布局间距（内边距）。它指定了容器的左边缘和右边缘与当前组件之间的空间。
    * 这应该是一个非负整数。
    * @see getHgap()
    * @see setHgap()
    */
    int hgap;

    /*
    * 卡片的垂直布局间距（内边距）。它指定了容器的上边缘和下边缘与当前组件之间的空间。
    * 这应该是一个非负整数。
    * @see getVgap()
    * @see setVgap()
    */
    int vgap;

    /**
     * @serialField tab         Hashtable
     *      已废弃，仅用于向前兼容
     * @serialField hgap        int
     * @serialField vgap        int
     * @serialField vector      Vector
     * @serialField currentCard int
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("tab", Hashtable.class),
        new ObjectStreamField("hgap", Integer.TYPE),
        new ObjectStreamField("vgap", Integer.TYPE),
        new ObjectStreamField("vector", Vector.class),
        new ObjectStreamField("currentCard", Integer.TYPE)
    };

    /**
     * 创建一个新的卡片布局，间距为零。
     */
    public CardLayout() {
        this(0, 0);
    }

    /**
     * 创建一个新的卡片布局，具有指定的水平和垂直间距。水平间距位于左侧和右侧边缘。垂直间距位于顶部和底部边缘。
     * @param     hgap   水平间距。
     * @param     vgap   垂直间距。
     */
    public CardLayout(int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
    }

    /**
     * 获取组件之间的水平间距。
     * @return    组件之间的水平间距。
     * @see       java.awt.CardLayout#setHgap(int)
     * @see       java.awt.CardLayout#getVgap()
     * @since     JDK1.1
     */
    public int getHgap() {
        return hgap;
    }

    /**
     * 设置组件之间的水平间距。
     * @param hgap 组件之间的水平间距。
     * @see       java.awt.CardLayout#getHgap()
     * @see       java.awt.CardLayout#setVgap(int)
     * @since     JDK1.1
     */
    public void setHgap(int hgap) {
        this.hgap = hgap;
    }

    /**
     * 获取组件之间的垂直间距。
     * @return 组件之间的垂直间距。
     * @see       java.awt.CardLayout#setVgap(int)
     * @see       java.awt.CardLayout#getHgap()
     */
    public int getVgap() {
        return vgap;
    }

    /**
     * 设置组件之间的垂直间距。
     * @param     vgap 组件之间的垂直间距。
     * @see       java.awt.CardLayout#getVgap()
     * @see       java.awt.CardLayout#setHgap(int)
     * @since     JDK1.1
     */
    public void setVgap(int vgap) {
        this.vgap = vgap;
    }

    /**
     * 将指定的组件添加到此卡片布局的内部名称表中。 <code>constraints</code> 指定的对象必须是字符串。卡片布局将此字符串存储为键值对，可用于随机访问特定卡片。
     * 通过调用 <code>show</code> 方法，应用程序可以显示具有指定名称的组件。
     * @param     comp          要添加的组件。
     * @param     constraints   识别布局中特定卡片的标签。
     * @see       java.awt.CardLayout#show(java.awt.Container, java.lang.String)
     * @exception  IllegalArgumentException  如果约束不是字符串。
     */
    public void addLayoutComponent(Component comp, Object constraints) {
      synchronized (comp.getTreeLock()) {
          if (constraints == null){
              constraints = "";
          }
        if (constraints instanceof String) {
            addLayoutComponent((String)constraints, comp);
        } else {
            throw new IllegalArgumentException("cannot add to layout: constraint must be a string");
        }
      }
    }

    /**
     * @deprecated   被 <code>addLayoutComponent(Component, Object)</code> 替代。
     */
    @Deprecated
    public void addLayoutComponent(String name, Component comp) {
        synchronized (comp.getTreeLock()) {
            if (!vector.isEmpty()) {
                comp.setVisible(false);
            }
            for (int i=0; i < vector.size(); i++) {
                if (((Card)vector.get(i)).name.equals(name)) {
                    ((Card)vector.get(i)).comp = comp;
                    return;
                }
            }
            vector.add(new Card(name, comp));
        }
    }

    /**
     * 从布局中移除指定的组件。如果该卡片在顶部可见，则显示其下方的下一张卡片。
     * @param   comp   要移除的组件。
     * @see     java.awt.Container#remove(java.awt.Component)
     * @see     java.awt.Container#removeAll()
     */
    public void removeLayoutComponent(Component comp) {
        synchronized (comp.getTreeLock()) {
            for (int i = 0; i < vector.size(); i++) {
                if (((Card)vector.get(i)).comp == comp) {
                    // 如果移除当前组件，应显示下一个组件
                    if (comp.isVisible() && (comp.getParent() != null)) {
                        next(comp.getParent());
                    }

                    vector.remove(i);

                    // 如果需要，调整 currentCard
                    if (currentCard > i) {
                        currentCard--;
                    }
                    break;
                }
            }
        }
    }

    /**
     * 确定使用此卡片布局的容器的首选大小。
     * @param   parent 要进行布局的父容器
     * @return  用于布局指定容器的子组件的首选尺寸
     * @see     java.awt.Container#getPreferredSize
     * @see     java.awt.CardLayout#minimumLayoutSize
     */
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int w = 0;
            int h = 0;

            for (int i = 0 ; i < ncomponents ; i++) {
                Component comp = parent.getComponent(i);
                Dimension d = comp.getPreferredSize();
                if (d.width > w) {
                    w = d.width;
                }
                if (d.height > h) {
                    h = d.height;
                }
            }
            return new Dimension(insets.left + insets.right + w + hgap*2,
                                 insets.top + insets.bottom + h + vgap*2);
        }
    }

    /**
     * 计算指定面板的最小尺寸。
     * @param     parent 要进行布局的父容器
     * @return    用于布局指定容器的子组件的最小尺寸
     * @see       java.awt.Container#doLayout
     * @see       java.awt.CardLayout#preferredLayoutSize
     */
    public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int w = 0;
            int h = 0;

            for (int i = 0 ; i < ncomponents ; i++) {
                Component comp = parent.getComponent(i);
                Dimension d = comp.getMinimumSize();
                if (d.width > w) {
                    w = d.width;
                }
                if (d.height > h) {
                    h = d.height;
                }
            }
            return new Dimension(insets.left + insets.right + w + hgap*2,
                                 insets.top + insets.bottom + h + vgap*2);
        }
    }

    /**
     * 返回给定目标容器中组件的最大尺寸。
     * @param target 需要布局的组件
     * @see Container
     * @see #minimumLayoutSize
     * @see #preferredLayoutSize
     */
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * 返回 x 轴上的对齐方式。这指定了组件相对于其他组件的对齐方式。值应在 0 到 1 之间，其中 0 表示对齐到原点，1 表示对齐到远离原点的最远位置，0.5 表示居中，等等。
     */
    public float getLayoutAlignmentX(Container parent) {
        return 0.5f;
    }

    /**
     * 返回 y 轴上的对齐方式。这指定了组件相对于其他组件的对齐方式。值应在 0 到 1 之间，其中 0 表示对齐到原点，1 表示对齐到远离原点的最远位置，0.5 表示居中，等等。
     */
    public float getLayoutAlignmentY(Container parent) {
        return 0.5f;
    }

    /**
     * 使布局无效，指示布局管理器应丢弃缓存的信息。
     */
    public void invalidateLayout(Container target) {
    }

    /**
     * 使用此卡片布局对指定容器进行布局。
     * <p>
     * <code>parent</code> 容器中的每个组件的大小将被调整为容器的大小，减去周围内边距、水平间距和垂直间距的空间。
     *
     * @param     parent 要进行布局的父容器
     * @see       java.awt.Container#doLayout
     */
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            Component comp = null;
            boolean currentFound = false;

            for (int i = 0 ; i < ncomponents ; i++) {
                comp = parent.getComponent(i);
                comp.setBounds(hgap + insets.left, vgap + insets.top,
                               parent.width - (hgap*2 + insets.left + insets.right),
                               parent.height - (vgap*2 + insets.top + insets.bottom));
                if (comp.isVisible()) {
                    currentFound = true;
                }
            }

            if (!currentFound && ncomponents > 0) {
                parent.getComponent(0).setVisible(true);
            }
        }
    }

    /**
     * 确保容器确实安装了 CardLayout。否则可能会导致混乱！
     */
    void checkLayout(Container parent) {
        if (parent.getLayout() != this) {
            throw new IllegalArgumentException("wrong parent for CardLayout");
        }
    }

    /**
     * 翻到容器中的第一张卡片。
     * @param     parent   要进行布局的父容器
     * @see       java.awt.CardLayout#last
     */
    public void first(Container parent) {
        synchronized (parent.getTreeLock()) {
            checkLayout(parent);
            int ncomponents = parent.getComponentCount();
            for (int i = 0 ; i < ncomponents ; i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible()) {
                    comp.setVisible(false);
                    break;
                }
            }
            if (ncomponents > 0) {
                currentCard = 0;
                parent.getComponent(0).setVisible(true);
                parent.validate();
            }
        }
    }

    /**
     * 翻到指定容器中的下一张卡片。如果当前可见的卡片是最后一张，则此方法将翻到布局中的第一张卡片。
     * @param     parent   要进行布局的父容器
     * @see       java.awt.CardLayout#previous
     */
    public void next(Container parent) {
        synchronized (parent.getTreeLock()) {
            checkLayout(parent);
            int ncomponents = parent.getComponentCount();
            for (int i = 0 ; i < ncomponents ; i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible()) {
                    comp.setVisible(false);
                    currentCard = (i + 1) % ncomponents;
                    comp = parent.getComponent(currentCard);
                    comp.setVisible(true);
                    parent.validate();
                    return;
                }
            }
            showDefaultComponent(parent);
        }
    }


                /**
     * 切换到指定容器的上一张卡片。如果当前可见的卡片是第一张，此方法将切换到布局中的最后一张卡片。
     * @param     parent   进行布局的父容器
     * @see       java.awt.CardLayout#next
     */
    public void previous(Container parent) {
        synchronized (parent.getTreeLock()) {
            checkLayout(parent);
            int ncomponents = parent.getComponentCount();
            for (int i = 0 ; i < ncomponents ; i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible()) {
                    comp.setVisible(false);
                    currentCard = ((i > 0) ? i-1 : ncomponents-1);
                    comp = parent.getComponent(currentCard);
                    comp.setVisible(true);
                    parent.validate();
                    return;
                }
            }
            showDefaultComponent(parent);
        }
    }

    void showDefaultComponent(Container parent) {
        if (parent.getComponentCount() > 0) {
            currentCard = 0;
            parent.getComponent(0).setVisible(true);
            parent.validate();
        }
    }

    /**
     * 切换到容器的最后一张卡片。
     * @param     parent   进行布局的父容器
     * @see       java.awt.CardLayout#first
     */
    public void last(Container parent) {
        synchronized (parent.getTreeLock()) {
            checkLayout(parent);
            int ncomponents = parent.getComponentCount();
            for (int i = 0 ; i < ncomponents ; i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible()) {
                    comp.setVisible(false);
                    break;
                }
            }
            if (ncomponents > 0) {
                currentCard = ncomponents - 1;
                parent.getComponent(currentCard).setVisible(true);
                parent.validate();
            }
        }
    }

    /**
     * 切换到使用 <code>addLayoutComponent</code> 方法添加到此布局中的指定 <code>name</code> 的组件。
     * 如果不存在这样的组件，则不执行任何操作。
     * @param     parent   进行布局的父容器
     * @param     name     组件名称
     * @see       java.awt.CardLayout#addLayoutComponent(java.awt.Component, java.lang.Object)
     */
    public void show(Container parent, String name) {
        synchronized (parent.getTreeLock()) {
            checkLayout(parent);
            Component next = null;
            int ncomponents = vector.size();
            for (int i = 0; i < ncomponents; i++) {
                Card card = (Card)vector.get(i);
                if (card.name.equals(name)) {
                    next = card.comp;
                    currentCard = i;
                    break;
                }
            }
            if ((next != null) && !next.isVisible()) {
                ncomponents = parent.getComponentCount();
                for (int i = 0; i < ncomponents; i++) {
                    Component comp = parent.getComponent(i);
                    if (comp.isVisible()) {
                        comp.setVisible(false);
                        break;
                    }
                }
                next.setVisible(true);
                parent.validate();
            }
        }
    }

    /**
     * 返回此卡片布局状态的字符串表示。
     * @return    此卡片布局的字符串表示。
     */
    public String toString() {
        return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + "]";
    }

    /**
     * 从流中读取可序列化的字段。
     */
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException
    {
        ObjectInputStream.GetField f = s.readFields();

        hgap = f.get("hgap", 0);
        vgap = f.get("vgap", 0);

        if (f.defaulted("vector")) {
            // 1.4 之前的流
            Hashtable<String, Component> tab = (Hashtable)f.get("tab", null);
            vector = new Vector<>();
            if (tab != null && !tab.isEmpty()) {
                for (Enumeration<String> e = tab.keys() ; e.hasMoreElements() ; ) {
                    String key = (String)e.nextElement();
                    Component comp = (Component)tab.get(key);
                    vector.add(new Card(key, comp));
                    if (comp.isVisible()) {
                        currentCard = vector.size() - 1;
                    }
                }
            }
        } else {
            vector = (Vector)f.get("vector", null);
            currentCard = f.get("currentCard", 0);
        }
    }

    /**
     * 将可序列化的字段写入流。
     */
    private void writeObject(ObjectOutputStream s)
        throws IOException
    {
        Hashtable<String, Component> tab = new Hashtable<>();
        int ncomponents = vector.size();
        for (int i = 0; i < ncomponents; i++) {
            Card card = (Card)vector.get(i);
            tab.put(card.name, card.comp);
        }

        ObjectOutputStream.PutField f = s.putFields();
        f.put("hgap", hgap);
        f.put("vgap", vgap);
        f.put("vector", vector);
        f.put("currentCard", currentCard);
        f.put("tab", tab);
        s.writeFields();
    }
}
