
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
 * 这是一个实用类，可以被支持绑定属性的 bean 使用。它管理一个监听器列表，并将 {@link PropertyChangeEvent} 分发给它们。
 * 你可以在你的 bean 中使用这个类的实例，并将这些类型的工作委托给它。
 * {@link PropertyChangeListener} 可以注册为所有属性的监听器，也可以注册为特定名称的属性的监听器。
 * <p>
 * 以下是一个使用 {@code PropertyChangeSupport} 的示例，遵循 JavaBeans&trade; 规范中的规则和建议：
 * <pre>
 * public class MyBean {
 *     private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 *
 *     public void addPropertyChangeListener(PropertyChangeListener listener) {
 *         this.pcs.addPropertyChangeListener(listener);
 *     }
 *
 *     public void removePropertyChangeListener(PropertyChangeListener listener) {
 *         this.pcs.removePropertyChangeListener(listener);
 *     }
 *
 *     private String value;
 *
 *     public String getValue() {
 *         return this.value;
 *     }
 *
 *     public void setValue(String newValue) {
 *         String oldValue = this.value;
 *         this.value = newValue;
 *         this.pcs.firePropertyChange("value", oldValue, newValue);
 *     }
 *
 *     [...]
 * }
 * </pre>
 * <p>
 * {@code PropertyChangeSupport} 实例是线程安全的。
 * <p>
 * 该类是可序列化的。当它被序列化时，它将保存（并恢复）所有可序列化的监听器。任何不可序列化的监听器将在序列化期间被跳过。
 *
 * @see VetoableChangeSupport
 */
public class PropertyChangeSupport implements Serializable {
    private PropertyChangeListenerMap map = new PropertyChangeListenerMap();

    /**
     * 构造一个 <code>PropertyChangeSupport</code> 对象。
     *
     * @param sourceBean  作为任何事件源的 bean。
     */
    public PropertyChangeSupport(Object sourceBean) {
        if (sourceBean == null) {
            throw new NullPointerException();
        }
        source = sourceBean;
    }

    /**
     * 将 PropertyChangeListener 添加到监听器列表中。
     * 监听器将注册为所有属性的监听器。
     * 同一个监听器对象可以添加多次，并且每次添加都会被调用。
     * 如果 <code>listener</code> 为 null，则不会抛出异常，也不会采取任何操作。
     *
     * @param listener  要添加的 PropertyChangeListener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (listener instanceof PropertyChangeListenerProxy) {
            PropertyChangeListenerProxy proxy =
                   (PropertyChangeListenerProxy)listener;
            // 调用带有两个参数的添加方法。
            addPropertyChangeListener(proxy.getPropertyName(),
                                      proxy.getListener());
        } else {
            this.map.add(null, listener);
        }
    }

    /**
     * 从监听器列表中移除 PropertyChangeListener。
     * 这将移除一个注册为所有属性的 PropertyChangeListener。
     * 如果 <code>listener</code> 被添加到同一个事件源多次，则移除后它将少被通知一次。
     * 如果 <code>listener</code> 为 null，或者从未添加过，则不会抛出异常，也不会采取任何操作。
     *
     * @param listener  要移除的 PropertyChangeListener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (listener instanceof PropertyChangeListenerProxy) {
            PropertyChangeListenerProxy proxy =
                    (PropertyChangeListenerProxy)listener;
            // 调用带有两个参数的移除方法。
            removePropertyChangeListener(proxy.getPropertyName(),
                                         proxy.getListener());
        } else {
            this.map.remove(null, listener);
        }
    }

    /**
     * 返回通过 addPropertyChangeListener() 添加到 PropertyChangeSupport 对象的所有监听器的数组。
     * <p>
     * 如果某些监听器是通过命名属性添加的，则返回的数组将包含 PropertyChangeListeners 和 <code>PropertyChangeListenerProxy</code> 的混合。
     * 如果调用方法对区分监听器感兴趣，则必须测试每个元素以查看它是否是 <code>PropertyChangeListenerProxy</code>，执行强制转换，并检查参数。
     *
     * <pre>{@code
     * PropertyChangeListener[] listeners = bean.getPropertyChangeListeners();
     * for (int i = 0; i < listeners.length; i++) {
     *   if (listeners[i] instanceof PropertyChangeListenerProxy) {
     *     PropertyChangeListenerProxy proxy =
     *                    (PropertyChangeListenerProxy)listeners[i];
     *     if (proxy.getPropertyName().equals("foo")) {
     *       // proxy 是与名为 "foo" 的属性关联的 PropertyChangeListener
     *     }
     *   }
     * }
     * }</pre>
     *
     * @see PropertyChangeListenerProxy
     * @return 所有添加的 <code>PropertyChangeListeners</code>，如果没有添加任何监听器，则返回一个空数组
     * @since 1.4
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return this.map.getListeners();
    }

    /**
     * 为特定属性添加 PropertyChangeListener。监听器仅在调用 firePropertyChange 时指定该特定属性时才会被调用。
     * 同一个监听器对象可以添加多次。对于每个属性，监听器将被调用的次数等于它为该属性添加的次数。
     * 如果 <code>propertyName</code> 或 <code>listener</code> 为 null，则不会抛出异常，也不会采取任何操作。
     *
     * @param propertyName  要监听的属性名称。
     * @param listener  要添加的 PropertyChangeListener
     */
    public void addPropertyChangeListener(
                String propertyName,
                PropertyChangeListener listener) {
        if (listener == null || propertyName == null) {
            return;
        }
        listener = this.map.extract(listener);
        if (listener != null) {
            this.map.add(propertyName, listener);
        }
    }

    /**
     * 为特定属性移除 PropertyChangeListener。
     * 如果 <code>listener</code> 被添加到同一个事件源多次，则移除后它将少被通知一次。
     * 如果 <code>propertyName</code> 为 null，则不会抛出异常，也不会采取任何操作。
     * 如果 <code>listener</code> 为 null，或者从未为指定属性添加过，则不会抛出异常，也不会采取任何操作。
     *
     * @param propertyName  被监听的属性名称。
     * @param listener  要移除的 PropertyChangeListener
     */
    public void removePropertyChangeListener(
                String propertyName,
                PropertyChangeListener listener) {
        if (listener == null || propertyName == null) {
            return;
        }
        listener = this.map.extract(listener);
        if (listener != null) {
            this.map.remove(propertyName, listener);
        }
    }

    /**
     * 返回与命名属性关联的所有监听器的数组。
     *
     * @param propertyName  被监听的属性名称
     * @return 与命名属性关联的所有 <code>PropertyChangeListeners</code>。如果没有添加这样的监听器，或者 <code>propertyName</code> 为 null，则返回一个空数组。
     * @since 1.4
     */
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return this.map.getListeners(propertyName);
    }

    /**
     * 向注册为跟踪所有属性更新或具有指定名称的属性更新的监听器报告绑定属性更新。
     * <p>
     * 如果旧值和新值相等且非 null，则不会触发事件。
     * <p>
     * 这仅是更通用的 {@link #firePropertyChange(PropertyChangeEvent)} 方法的便利包装。
     *
     * @param propertyName  被更改的属性的程序名称
     * @param oldValue      属性的旧值
     * @param newValue      属性的新值
     */
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            firePropertyChange(new PropertyChangeEvent(this.source, propertyName, oldValue, newValue));
        }
    }

    /**
     * 向注册为跟踪所有属性更新或具有指定名称的属性更新的监听器报告整数绑定属性更新。
     * <p>
     * 如果旧值和新值相等，则不会触发事件。
     * <p>
     * 这仅是更通用的 {@link #firePropertyChange(String, Object, Object)} 方法的便利包装。
     *
     * @param propertyName  被更改的属性的程序名称
     * @param oldValue      属性的旧值
     * @param newValue      属性的新值
     */
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
        if (oldValue != newValue) {
            firePropertyChange(propertyName, Integer.valueOf(oldValue), Integer.valueOf(newValue));
        }
    }

    /**
     * 向注册为跟踪所有属性更新或具有指定名称的属性更新的监听器报告布尔绑定属性更新。
     * <p>
     * 如果旧值和新值相等，则不会触发事件。
     * <p>
     * 这仅是更通用的 {@link #firePropertyChange(String, Object, Object)} 方法的便利包装。
     *
     * @param propertyName  被更改的属性的程序名称
     * @param oldValue      属性的旧值
     * @param newValue      属性的新值
     */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        if (oldValue != newValue) {
            firePropertyChange(propertyName, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
        }
    }

    /**
     * 向注册为跟踪所有属性更新或具有指定名称的属性更新的监听器触发属性更改事件。
     * <p>
     * 如果给定事件的旧值和新值相等且非 null，则不会触发事件。
     *
     * @param event  要触发的 {@code PropertyChangeEvent}
     */
    public void firePropertyChange(PropertyChangeEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            String name = event.getPropertyName();

            PropertyChangeListener[] common = this.map.get(null);
            PropertyChangeListener[] named = (name != null)
                        ? this.map.get(name)
                        : null;

            fire(common, event);
            fire(named, event);
        }
    }

    private static void fire(PropertyChangeListener[] listeners, PropertyChangeEvent event) {
        if (listeners != null) {
            for (PropertyChangeListener listener : listeners) {
                listener.propertyChange(event);
            }
        }
    }

    /**
     * 向注册为跟踪所有属性更新或具有指定名称的属性更新的监听器报告绑定索引属性更新。
     * <p>
     * 如果旧值和新值相等且非 null，则不会触发事件。
     * <p>
     * 这仅是更通用的 {@link #firePropertyChange(PropertyChangeEvent)} 方法的便利包装。
     *
     * @param propertyName  被更改的属性的程序名称
     * @param index         被更改的属性元素的索引
     * @param oldValue      属性的旧值
     * @param newValue      属性的新值
     * @since 1.5
     */
    public void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            firePropertyChange(new IndexedPropertyChangeEvent(source, propertyName, oldValue, newValue, index));
        }
    }

    /**
     * 向注册为跟踪所有属性更新或具有指定名称的属性更新的监听器报告整数绑定索引属性更新。
     * <p>
     * 如果旧值和新值相等，则不会触发事件。
     * <p>
     * 这仅是更通用的 {@link #fireIndexedPropertyChange(String, int, Object, Object)} 方法的便利包装。
     *
     * @param propertyName  被更改的属性的程序名称
     * @param index         被更改的属性元素的索引
     * @param oldValue      属性的旧值
     * @param newValue      属性的新值
     * @since 1.5
     */
    public void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue) {
        if (oldValue != newValue) {
            fireIndexedPropertyChange(propertyName, index, Integer.valueOf(oldValue), Integer.valueOf(newValue));
        }
    }


                /**
     * 向已注册以跟踪所有属性或指定名称属性更新的监听器报告布尔值绑定属性更新。
     * <p>
     * 如果旧值和新值相等，则不会触发事件。
     * <p>
     * 这仅仅是更通用的 {@link #fireIndexedPropertyChange(String, int, Object, Object)} 方法的便利包装。
     *
     * @param propertyName  被更改的属性的程序名称
     * @param index         被更改的属性元素的索引
     * @param oldValue      属性的旧值
     * @param newValue      属性的新值
     * @since 1.5
     */
    public void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue) {
        if (oldValue != newValue) {
            fireIndexedPropertyChange(propertyName, index, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
        }
    }

    /**
     * 检查是否有特定属性的监听器，包括注册在所有属性上的监听器。如果 <code>propertyName</code>
     * 为 null，则仅检查注册在所有属性上的监听器。
     *
     * @param propertyName  属性名称。
     * @return 如果给定属性有一个或多个监听器，则返回 true
     */
    public boolean hasListeners(String propertyName) {
        return this.map.hasListeners(propertyName);
    }

    /**
     * @serialData 以 null 结尾的 <code>PropertyChangeListeners</code> 列表。
     * <p>
     * 在序列化时，我们跳过不可序列化的监听器，仅序列化可序列化的监听器。
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        Hashtable<String, PropertyChangeSupport> children = null;
        PropertyChangeListener[] listeners = null;
        synchronized (this.map) {
            for (Entry<String, PropertyChangeListener[]> entry : this.map.getEntries()) {
                String property = entry.getKey();
                if (property == null) {
                    listeners = entry.getValue();
                } else {
                    if (children == null) {
                        children = new Hashtable<>();
                    }
                    PropertyChangeSupport pcs = new PropertyChangeSupport(this.source);
                    pcs.map.set(null, entry.getValue());
                    children.put(property, pcs);
                }
            }
        }
        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("children", children);
        fields.put("source", this.source);
        fields.put("propertyChangeSupportSerializedDataVersion", 2);
        s.writeFields();

        if (listeners != null) {
            for (PropertyChangeListener l : listeners) {
                if (l instanceof Serializable) {
                    s.writeObject(l);
                }
            }
        }
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        this.map = new PropertyChangeListenerMap();

        ObjectInputStream.GetField fields = s.readFields();

        @SuppressWarnings("unchecked")
        Hashtable<String, PropertyChangeSupport> children = (Hashtable<String, PropertyChangeSupport>) fields.get("children", null);
        this.source = fields.get("source", null);
        fields.get("propertyChangeSupportSerializedDataVersion", 2);

        Object listenerOrNull;
        while (null != (listenerOrNull = s.readObject())) {
            this.map.add(null, (PropertyChangeListener)listenerOrNull);
        }
        if (children != null) {
            for (Entry<String, PropertyChangeSupport> entry : children.entrySet()) {
                for (PropertyChangeListener listener : entry.getValue().getPropertyChangeListeners()) {
                    this.map.add(entry.getKey(), listener);
                }
            }
        }
    }

    /**
     * 作为生成的事件的“源”提供的对象。
     */
    private Object source;

    /**
     * @serialField children                                   Hashtable
     * @serialField source                                     Object
     * @serialField propertyChangeSupportSerializedDataVersion int
     */
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("children", Hashtable.class),
            new ObjectStreamField("source", Object.class),
            new ObjectStreamField("propertyChangeSupportSerializedDataVersion", Integer.TYPE)
    };

    /**
     * 序列化版本 ID，以便与 JDK 1.1 兼容
     */
    static final long serialVersionUID = 6401253773779951803L;

    /**
     * 这是一个与 {@link PropertyChangeListener PropertyChangeListener} 对象一起工作的
     * {@link ChangeListenerMap ChangeListenerMap} 实现。
     */
    private static final class PropertyChangeListenerMap extends ChangeListenerMap<PropertyChangeListener> {
        private static final PropertyChangeListener[] EMPTY = {};

        /**
         * 创建一个 {@link PropertyChangeListener PropertyChangeListener} 对象数组。
         * 当 {@code length} 等于 {@code 0} 时，此方法使用空数组的同一实例。
         *
         * @param length  数组长度
         * @return        指定长度的数组
         */
        @Override
        protected PropertyChangeListener[] newArray(int length) {
            return (0 < length)
                    ? new PropertyChangeListener[length]
                    : EMPTY;
        }

        /**
         * 为指定属性创建一个 {@link PropertyChangeListenerProxy PropertyChangeListenerProxy} 对象。
         *
         * @param name      要监听的属性名称
         * @param listener  处理事件的监听器
         * @return          一个 {@code PropertyChangeListenerProxy} 对象
         */
        @Override
        protected PropertyChangeListener newProxy(String name, PropertyChangeListener listener) {
            return new PropertyChangeListenerProxy(name, listener);
        }

        /**
         * {@inheritDoc}
         */
        public final PropertyChangeListener extract(PropertyChangeListener listener) {
            while (listener instanceof PropertyChangeListenerProxy) {
                listener = ((PropertyChangeListenerProxy) listener).getListener();
            }
            return listener;
        }
    }
}
