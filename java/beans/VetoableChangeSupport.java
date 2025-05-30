
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.Serializable;
import java.io.ObjectStreamField;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map.Entry;

/**
 * 这是一个实用类，可以被支持受限属性的 bean 使用。它管理一个监听器列表并分发
 * {@link PropertyChangeEvent} 到这些监听器。你可以在你的 bean 中使用这个类的实例
 * 并将这些类型的工作委托给它。
 * {@link VetoableChangeListener} 可以注册为所有属性的监听器，也可以注册为特定名称的属性的监听器。
 * <p>
 * 以下是一个遵循 JavaBeans&trade; 规范的规则和建议的 {@code VetoableChangeSupport} 用法示例：
 * <pre>{@code
 * public class MyBean {
 *     private final VetoableChangeSupport vcs = new VetoableChangeSupport(this);
 *
 *     public void addVetoableChangeListener(VetoableChangeListener listener) {
 *         this.vcs.addVetoableChangeListener(listener);
 *     }
 *
 *     public void removeVetoableChangeListener(VetoableChangeListener listener) {
 *         this.vcs.removeVetoableChangeListener(listener);
 *     }
 *
 *     private String value;
 *
 *     public String getValue() {
 *         return this.value;
 *     }
 *
 *     public void setValue(String newValue) throws PropertyVetoException {
 *         String oldValue = this.value;
 *         this.vcs.fireVetoableChange("value", oldValue, newValue);
 *         this.value = newValue;
 *     }
 *
 *     [...]
 * }
 * }</pre>
 * <p>
 * {@code VetoableChangeSupport} 实例是线程安全的。
 * <p>
 * 该类是可序列化的。当它被序列化时，它将保存（并恢复）所有可序列化的监听器。任何不可序列化的监听器将在序列化过程中被跳过。
 *
 * @see PropertyChangeSupport
 */
public class VetoableChangeSupport implements Serializable {
    private VetoableChangeListenerMap map = new VetoableChangeListenerMap();

    /**
     * 构造一个 <code>VetoableChangeSupport</code> 对象。
     *
     * @param sourceBean  作为任何事件源的 bean。
     */
    public VetoableChangeSupport(Object sourceBean) {
        if (sourceBean == null) {
            throw new NullPointerException();
        }
        source = sourceBean;
    }

    /**
     * 将 VetoableChangeListener 添加到监听器列表中。
     * 监听器将注册为所有属性的监听器。
     * 同一个监听器对象可以添加多次，并且每次添加都会被调用。
     * 如果 <code>listener</code> 为 null，则不抛出异常且不执行任何操作。
     *
     * @param listener  要添加的 VetoableChangeListener
     */
    public void addVetoableChangeListener(VetoableChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (listener instanceof VetoableChangeListenerProxy) {
            VetoableChangeListenerProxy proxy =
                    (VetoableChangeListenerProxy)listener;
            // 调用带有两个参数的添加方法。
            addVetoableChangeListener(proxy.getPropertyName(),
                                      proxy.getListener());
        } else {
            this.map.add(null, listener);
        }
    }

    /**
     * 从监听器列表中移除 VetoableChangeListener。
     * 这将移除注册为所有属性的 VetoableChangeListener。
     * 如果 <code>listener</code> 被添加到同一个事件源多次，则移除后它将被通知一次。
     * 如果 <code>listener</code> 为 null，或从未添加，则不抛出异常且不执行任何操作。
     *
     * @param listener  要移除的 VetoableChangeListener
     */
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (listener instanceof VetoableChangeListenerProxy) {
            VetoableChangeListenerProxy proxy =
                    (VetoableChangeListenerProxy)listener;
            // 调用带有两个参数的移除方法。
            removeVetoableChangeListener(proxy.getPropertyName(),
                                         proxy.getListener());
        } else {
            this.map.remove(null, listener);
        }
    }

    /**
     * 返回所有通过 addVetoableChangeListener() 方法添加到 VetoableChangeSupport 对象的监听器数组。
     * <p>
     * 如果某些监听器是通过命名属性添加的，则返回的数组将包含 VetoableChangeListeners
     * 和 <code>VetoableChangeListenerProxy</code> 的混合。如果调用方法对区分监听器感兴趣，则必须
     * 测试每个元素以查看它是否是 <code>VetoableChangeListenerProxy</code>，执行强制转换，并检查参数。
     *
     * <pre>{@code
     * VetoableChangeListener[] listeners = bean.getVetoableChangeListeners();
     * for (int i = 0; i < listeners.length; i++) {
     *        if (listeners[i] instanceof VetoableChangeListenerProxy) {
     *     VetoableChangeListenerProxy proxy =
     *                    (VetoableChangeListenerProxy)listeners[i];
     *     if (proxy.getPropertyName().equals("foo")) {
     *       // proxy 是一个与名为 "foo" 的属性关联的 VetoableChangeListener
     *     }
     *   }
     * }
     * }</pre>
     *
     * @see VetoableChangeListenerProxy
     * @return 所有添加的 <code>VetoableChangeListeners</code>，如果没有添加任何监听器，则返回一个空数组
     * @since 1.4
     */
    public VetoableChangeListener[] getVetoableChangeListeners(){
        return this.map.getListeners();
    }

    /**
     * 为特定属性添加 VetoableChangeListener。监听器仅在调用 fireVetoableChange 时指定该特定属性时才会被调用。
     * 同一个监听器对象可以添加多次。对于每个属性，监听器将被调用的次数等于它为该属性添加的次数。
     * 如果 <code>propertyName</code> 或 <code>listener</code> 为 null，则不抛出异常且不执行任何操作。
     *
     * @param propertyName  要监听的属性名称。
     * @param listener  要添加的 VetoableChangeListener
     */
    public void addVetoableChangeListener(
                                String propertyName,
                VetoableChangeListener listener) {
        if (listener == null || propertyName == null) {
            return;
        }
        listener = this.map.extract(listener);
        if (listener != null) {
            this.map.add(propertyName, listener);
        }
    }

    /**
     * 为特定属性移除 VetoableChangeListener。
     * 如果 <code>listener</code> 被添加到同一个事件源多次，则移除后它将被通知一次。
     * 如果 <code>propertyName</code> 为 null，则不抛出异常且不执行任何操作。
     * 如果 <code>listener</code> 为 null，或从未为指定属性添加，则不抛出异常且不执行任何操作。
     *
     * @param propertyName  被监听的属性名称。
     * @param listener  要移除的 VetoableChangeListener
     */
    public void removeVetoableChangeListener(
                                String propertyName,
                VetoableChangeListener listener) {
        if (listener == null || propertyName == null) {
            return;
        }
        listener = this.map.extract(listener);
        if (listener != null) {
            this.map.remove(propertyName, listener);
        }
    }

    /**
     * 返回与命名属性关联的所有监听器数组。
     *
     * @param propertyName  被监听的属性名称
     * @return 与命名属性关联的所有 <code>VetoableChangeListeners</code>。如果没有添加这样的监听器，
     *         或 <code>propertyName</code> 为 null，则返回一个空数组。
     * @since 1.4
     */
    public VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        return this.map.getListeners(propertyName);
    }

    /**
     * 向已注册以跟踪所有属性或指定名称属性更新的监听器报告受限属性更新。
     * <p>
     * 任何监听器都可以抛出 {@code PropertyVetoException} 以否决更新。
     * 如果其中一个监听器否决了更新，此方法将传递一个新的 "undo" {@code PropertyChangeEvent}，
     * 该事件将恢复到旧值，并将 {@code PropertyVetoException} 再次抛出给所有已确认此更新的监听器。
     * <p>
     * 如果旧值和新值相等且非空，则不会触发事件。
     * <p>
     * 这仅是更通用的 {@link #fireVetoableChange(PropertyChangeEvent)} 方法的便利包装。
     *
     * @param propertyName  即将更改的属性的程序名称
     * @param oldValue      属性的旧值
     * @param newValue      属性的新值
     * @throws PropertyVetoException 如果其中一个监听器否决了属性更新
     */
    public void fireVetoableChange(String propertyName, Object oldValue, Object newValue)
            throws PropertyVetoException {
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            fireVetoableChange(new PropertyChangeEvent(this.source, propertyName, oldValue, newValue));
        }
    }

    /**
     * 向已注册以跟踪所有属性或指定名称属性更新的监听器报告整数受限属性更新。
     * <p>
     * 任何监听器都可以抛出 {@code PropertyVetoException} 以否决更新。
     * 如果其中一个监听器否决了更新，此方法将传递一个新的 "undo" {@code PropertyChangeEvent}，
     * 该事件将恢复到旧值，并将 {@code PropertyVetoException} 再次抛出给所有已确认此更新的监听器。
     * <p>
     * 如果旧值和新值相等，则不会触发事件。
     * <p>
     * 这仅是更通用的 {@link #fireVetoableChange(String, Object, Object)} 方法的便利包装。
     *
     * @param propertyName  即将更改的属性的程序名称
     * @param oldValue      属性的旧值
     * @param newValue      属性的新值
     * @throws PropertyVetoException 如果其中一个监听器否决了属性更新
     */
    public void fireVetoableChange(String propertyName, int oldValue, int newValue)
            throws PropertyVetoException {
        if (oldValue != newValue) {
            fireVetoableChange(propertyName, Integer.valueOf(oldValue), Integer.valueOf(newValue));
        }
    }

    /**
     * 向已注册以跟踪所有属性或指定名称属性更新的监听器报告布尔受限属性更新。
     * <p>
     * 任何监听器都可以抛出 {@code PropertyVetoException} 以否决更新。
     * 如果其中一个监听器否决了更新，此方法将传递一个新的 "undo" {@code PropertyChangeEvent}，
     * 该事件将恢复到旧值，并将 {@code PropertyVetoException} 再次抛出给所有已确认此更新的监听器。
     * <p>
     * 如果旧值和新值相等，则不会触发事件。
     * <p>
     * 这仅是更通用的 {@link #fireVetoableChange(String, Object, Object)} 方法的便利包装。
     *
     * @param propertyName  即将更改的属性的程序名称
     * @param oldValue      属性的旧值
     * @param newValue      属性的新值
     * @throws PropertyVetoException 如果其中一个监听器否决了属性更新
     */
    public void fireVetoableChange(String propertyName, boolean oldValue, boolean newValue)
            throws PropertyVetoException {
        if (oldValue != newValue) {
            fireVetoableChange(propertyName, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
        }
    }

    /**
     * 向已注册以跟踪所有属性或指定名称属性更新的监听器报告属性更改事件。
     * <p>
     * 任何监听器都可以抛出 {@code PropertyVetoException} 以否决更新。
     * 如果其中一个监听器否决了更新，此方法将传递一个新的 "undo" {@code PropertyChangeEvent}，
     * 该事件将恢复到旧值，并将 {@code PropertyVetoException} 再次抛出给所有已确认此更新的监听器。
     * <p>
     * 如果给定事件的旧值和新值相等且非空，则不会触发事件。
     *
     * @param event  要触发的 {@code PropertyChangeEvent}
     * @throws PropertyVetoException 如果其中一个监听器否决了属性更新
     */
    public void fireVetoableChange(PropertyChangeEvent event)
            throws PropertyVetoException {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            String name = event.getPropertyName();

            VetoableChangeListener[] common = this.map.get(null);
            VetoableChangeListener[] named = (name != null)
                        ? this.map.get(name)
                        : null;

            VetoableChangeListener[] listeners;
            if (common == null) {
                listeners = named;
            }
            else if (named == null) {
                listeners = common;
            }
            else {
                listeners = new VetoableChangeListener[common.length + named.length];
                System.arraycopy(common, 0, listeners, 0, common.length);
                System.arraycopy(named, 0, listeners, common.length, named.length);
            }
            if (listeners != null) {
                int current = 0;
                try {
                    while (current < listeners.length) {
                        listeners[current].vetoableChange(event);
                        current++;
                    }
                }
                catch (PropertyVetoException veto) {
                    event = new PropertyChangeEvent(this.source, name, newValue, oldValue);
                    for (int i = 0; i < current; i++) {
                        try {
                            listeners[i].vetoableChange(event);
                        }
                        catch (PropertyVetoException exception) {
                            // 忽略在回滚过程中发生的异常
                        }
                    }
                    throw veto; // 重新抛出否决异常
                }
            }
        }
    }


                /**
     * 检查特定属性是否有任何监听器，包括在所有属性上注册的监听器。如果 <code>propertyName</code>
     * 为 null，则仅检查在所有属性上注册的监听器。
     *
     * @param propertyName  属性名称。
     * @return 如果给定属性有一个或多个监听器，则返回 true。
     */
    public boolean hasListeners(String propertyName) {
        return this.map.hasListeners(propertyName);
    }

    /**
     * @serialData 以 null 结尾的 <code>VetoableChangeListeners</code> 列表。
     * <p>
     * 在序列化时，我们跳过不可序列化的监听器，仅序列化可序列化的监听器。
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        Hashtable<String, VetoableChangeSupport> children = null;
        VetoableChangeListener[] listeners = null;
        synchronized (this.map) {
            for (Entry<String, VetoableChangeListener[]> entry : this.map.getEntries()) {
                String property = entry.getKey();
                if (property == null) {
                    listeners = entry.getValue();
                } else {
                    if (children == null) {
                        children = new Hashtable<>();
                    }
                    VetoableChangeSupport vcs = new VetoableChangeSupport(this.source);
                    vcs.map.set(null, entry.getValue());
                    children.put(property, vcs);
                }
            }
        }
        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("children", children);
        fields.put("source", this.source);
        fields.put("vetoableChangeSupportSerializedDataVersion", 2);
        s.writeFields();

        if (listeners != null) {
            for (VetoableChangeListener l : listeners) {
                if (l instanceof Serializable) {
                    s.writeObject(l);
                }
            }
        }
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        this.map = new VetoableChangeListenerMap();

        ObjectInputStream.GetField fields = s.readFields();

        @SuppressWarnings("unchecked")
        Hashtable<String, VetoableChangeSupport> children = (Hashtable<String, VetoableChangeSupport>)fields.get("children", null);
        this.source = fields.get("source", null);
        fields.get("vetoableChangeSupportSerializedDataVersion", 2);

        Object listenerOrNull;
        while (null != (listenerOrNull = s.readObject())) {
            this.map.add(null, (VetoableChangeListener)listenerOrNull);
        }
        if (children != null) {
            for (Entry<String, VetoableChangeSupport> entry : children.entrySet()) {
                for (VetoableChangeListener listener : entry.getValue().getVetoableChangeListeners()) {
                    this.map.add(entry.getKey(), listener);
                }
            }
        }
    }

    /**
     * 作为生成的事件的 "source" 提供的对象。
     */
    private Object source;

    /**
     * @serialField children                                   Hashtable
     * @serialField source                                     Object
     * @serialField vetoableChangeSupportSerializedDataVersion int
     */
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("children", Hashtable.class),
            new ObjectStreamField("source", Object.class),
            new ObjectStreamField("vetoableChangeSupportSerializedDataVersion", Integer.TYPE)
    };

    /**
     * 序列化版本 ID，以便与 JDK 1.1 兼容。
     */
    static final long serialVersionUID = -5090210921595982017L;

    /**
     * 这是一个 {@link ChangeListenerMap ChangeListenerMap} 实现，
     * 适用于 {@link VetoableChangeListener VetoableChangeListener} 对象。
     */
    private static final class VetoableChangeListenerMap extends ChangeListenerMap<VetoableChangeListener> {
        private static final VetoableChangeListener[] EMPTY = {};

        /**
         * 创建一个 {@link VetoableChangeListener VetoableChangeListener} 对象数组。
         * 当 {@code length} 等于 {@code 0} 时，此方法使用相同的空数组实例。
         *
         * @param length  数组长度
         * @return        指定长度的数组
         */
        @Override
        protected VetoableChangeListener[] newArray(int length) {
            return (0 < length)
                    ? new VetoableChangeListener[length]
                    : EMPTY;
        }

        /**
         * 为指定属性创建一个 {@link VetoableChangeListenerProxy VetoableChangeListenerProxy}
         * 对象。
         *
         * @param name      要监听的属性名称
         * @param listener  处理事件的监听器
         * @return          一个 {@code VetoableChangeListenerProxy} 对象
         */
        @Override
        protected VetoableChangeListener newProxy(String name, VetoableChangeListener listener) {
            return new VetoableChangeListenerProxy(name, listener);
        }

        /**
         * {@inheritDoc}
         */
        public final VetoableChangeListener extract(VetoableChangeListener listener) {
            while (listener instanceof VetoableChangeListenerProxy) {
                listener = ((VetoableChangeListenerProxy) listener).getListener();
            }
            return listener;
        }
    }
}
