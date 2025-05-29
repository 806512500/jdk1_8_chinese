/*
 * 版权所有 (c) 1996, 2005, Oracle 和/或其附属公司。保留所有权利。
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
package java.io;

/**
 * 异常，指示由于流中存在未读的原始数据或属于序列化对象的数据结束而导致的对象读取操作失败。此异常可能在两种情况下抛出：
 *
 * <ul>
 *   <li>尝试读取对象时，流中的下一个元素是原始数据。在这种情况下，OptionalDataException 的 length 字段设置为可以从流中立即读取的原始数据字节数，eof 字段设置为 false。
 *
 *   <li>尝试读取超出由类定义的 readObject 或 readExternal 方法可消耗的数据末尾。在这种情况下，OptionalDataException 的 eof 字段设置为 true，length 字段设置为 0。
 * </ul>
 *
 * @作者 未署名
 * @自  JDK1.1
 */
public class OptionalDataException extends ObjectStreamException {

    private static final long serialVersionUID = -8011121865681257820L;

    /*
     * 创建一个带有长度的 <code>OptionalDataException</code>。
     */
    OptionalDataException(int len) {
        eof = false;
        length = len;
    }

    /*
     * 创建一个表示没有更多原始数据可用的 <code>OptionalDataException</code>。
     */
    OptionalDataException(boolean end) {
        length = 0;
        eof = end;
    }

    /**
     * 当前缓冲区中可读取的原始数据的字节数。
     *
     * @serial
     */
    public int length;

    /**
     * 如果流的缓冲部分中没有更多数据，则为 true。
     *
     * @serial
     */
    public boolean eof;
}
