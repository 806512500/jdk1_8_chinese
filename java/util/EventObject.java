/*
 * 版权所有 (c) 1996, 2003, Oracle 和/或其关联公司。保留所有权利。
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

package java.util;

/**
 * <p>
 * 所有事件状态对象应派生的根类。
 * <p>
 * 所有事件都是使用一个引用对象（“源”）构造的，该对象逻辑上被认为是事件最初发生的对象。
 *
 * @since JDK1.1
 */

public class EventObject implements java.io.Serializable {

    private static final long serialVersionUID = 5516075349620653480L;

    /**
     * 事件最初发生的对象。
     */
    protected transient Object source;

    /**
     * 构造一个原型事件。
     *
     * @param    source    事件最初发生的对象。
     * @exception  IllegalArgumentException  如果源为 null。
     */
    public EventObject(Object source) {
        if (source == null)
            throw new IllegalArgumentException("null source");

        this.source = source;
    }

    /**
     * 获取事件最初发生的对象。
     *
     * @return   事件最初发生的对象。
     */
    public Object getSource() {
        return source;
    }

    /**
     * 返回此 EventObject 的字符串表示形式。
     *
     * @return  此 EventObject 的字符串表示形式。
     */
    public String toString() {
        return getClass().getName() + "[source=" + source + "]";
    }
}