
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.event;

import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.font.TextHitInfo;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.lang.annotation.Native;

/**
 * 输入方法事件包含使用输入方法输入的文本信息。每当文本发生变化时，输入方法会发送一个事件。如果当前使用输入方法的文本组件是活动客户端，事件将被分发到该组件。否则，事件将被分发到一个单独的组合窗口。
 *
 * <p>
 * 输入方法事件中包含的文本由两部分组成：已提交的文本和组合中的文本。这两部分中任何一部分都可能为空。这两部分一起替换之前事件中发送的任何未提交的组合文本，或当前选中的已提交文本。
 * 已提交的文本应集成到文本组件的持久数据中，不会再次发送。组合中的文本可能会重复发送，以反映用户的编辑操作。已提交的文本始终在组合中的文本之前。
 *
 * @author JavaSoft Asia/Pacific
 * @since 1.2
 */
public class InputMethodEvent extends AWTEvent {

    /**
     * 序列化版本ID。
     */
    private static final long serialVersionUID = 4727190874778922661L;

    /**
     * 标记输入方法事件ID范围的第一个整数ID。
     */
    @Native public static final int INPUT_METHOD_FIRST = 1100;

    /**
     * 表示输入方法文本更改的事件类型。此事件由输入方法在处理输入时生成。
     */
    @Native public static final int INPUT_METHOD_TEXT_CHANGED = INPUT_METHOD_FIRST;

    /**
     * 表示输入方法文本中插入点更改的事件类型。此事件由输入方法在处理输入时生成，仅当光标更改时。
     */
    @Native public static final int CARET_POSITION_CHANGED = INPUT_METHOD_FIRST + 1;

    /**
     * 标记输入方法事件ID范围的最后一个整数ID。
     */
    @Native public static final int INPUT_METHOD_LAST = INPUT_METHOD_FIRST + 1;

    /**
     * 表示事件创建时间的时间戳。
     *
     * @serial
     * @see #getWhen
     * @since 1.4
     */
    long when;

    // 文本对象
    private transient AttributedCharacterIterator text;
    private transient int committedCharacterCount;
    private transient TextHitInfo caret;
    private transient TextHitInfo visiblePosition;

    /**
     * 使用指定的源组件、类型、时间、文本、光标和可见位置构造一个 <code>InputMethodEvent</code>。
     * <p>
     * 光标和可见位置的偏移量相对于当前组合文本；即，如果这是 <code>INPUT_METHOD_TEXT_CHANGED</code> 事件，则为 <code>text</code> 中的组合文本；
     * 否则为前一个 <code>INPUT_METHOD_TEXT_CHANGED</code> 事件中的组合文本。
     * <p>注意，传递无效的 <code>id</code> 会导致未指定的行为。如果 <code>source</code> 为 <code>null</code>，此方法将抛出 <code>IllegalArgumentException</code>。
     *
     * @param source 事件的源对象
     * @param id 事件类型
     * @param when 指定事件发生时间的长整数
     * @param text 合并的已提交和组合文本，已提交文本在前；当事件类型为 <code>CARET_POSITION_CHANGED</code> 时，必须为 <code>null</code>；
     *      对于 <code>INPUT_METHOD_TEXT_CHANGED</code>，如果没有已提交或组合文本，可以为 <code>null</code>
     * @param committedCharacterCount 文本中已提交字符的数量
     * @param caret 光标（即插入点）；如果当前组合文本中没有光标，则为 <code>null</code>
     * @param visiblePosition 最重要的可见位置；如果当前组合文本中没有推荐的可见位置，则为 <code>null</code>
     * @throws IllegalArgumentException 如果 <code>id</code> 不在 <code>INPUT_METHOD_FIRST</code>..<code>INPUT_METHOD_LAST</code> 范围内；
     *      或者 <code>id</code> 为 <code>CARET_POSITION_CHANGED</code> 且 <code>text</code> 不为 <code>null</code>；
     *      或者 <code>committedCharacterCount</code> 不在 <code>0</code>..<code>(text.getEndIndex() - text.getBeginIndex())</code> 范围内
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     *
     * @since 1.4
     */
    public InputMethodEvent(Component source, int id, long when,
            AttributedCharacterIterator text, int committedCharacterCount,
            TextHitInfo caret, TextHitInfo visiblePosition) {
        super(source, id);
        if (id < INPUT_METHOD_FIRST || id > INPUT_METHOD_LAST) {
            throw new IllegalArgumentException("id outside of valid range");
        }

        if (id == CARET_POSITION_CHANGED && text != null) {
            throw new IllegalArgumentException("text must be null for CARET_POSITION_CHANGED");
        }

        this.when = when;
        this.text = text;
        int textLength = 0;
        if (text != null) {
            textLength = text.getEndIndex() - text.getBeginIndex();
        }

        if (committedCharacterCount < 0 || committedCharacterCount > textLength) {
            throw new IllegalArgumentException("committedCharacterCount outside of valid range");
        }
        this.committedCharacterCount = committedCharacterCount;

        this.caret = caret;
        this.visiblePosition = visiblePosition;
   }

    /**
     * 使用指定的源组件、类型、文本、光标和可见位置构造一个 <code>InputMethodEvent</code>。
     * <p>
     * 光标和可见位置的偏移量相对于当前组合文本；即，如果这是 <code>INPUT_METHOD_TEXT_CHANGED</code> 事件，则为 <code>text</code> 中的组合文本；
     * 否则为前一个 <code>INPUT_METHOD_TEXT_CHANGED</code> 事件中的组合文本。
     * 事件的时间戳通过调用 {@link java.awt.EventQueue#getMostRecentEventTime()} 初始化。
     * <p>注意，传递无效的 <code>id</code> 会导致未指定的行为。如果 <code>source</code> 为 <code>null</code>，此方法将抛出 <code>IllegalArgumentException</code>。
     *
     * @param source 事件的源对象
     * @param id 事件类型
     * @param text 合并的已提交和组合文本，已提交文本在前；当事件类型为 <code>CARET_POSITION_CHANGED</code> 时，必须为 <code>null</code>；
     *      对于 <code>INPUT_METHOD_TEXT_CHANGED</code>，如果没有已提交或组合文本，可以为 <code>null</code>
     * @param committedCharacterCount 文本中已提交字符的数量
     * @param caret 光标（即插入点）；如果当前组合文本中没有光标，则为 <code>null</code>
     * @param visiblePosition 最重要的可见位置；如果当前组合文本中没有推荐的可见位置，则为 <code>null</code>
     * @throws IllegalArgumentException 如果 <code>id</code> 不在 <code>INPUT_METHOD_FIRST</code>..<code>INPUT_METHOD_LAST</code> 范围内；
     *      或者 <code>id</code> 为 <code>CARET_POSITION_CHANGED</code> 且 <code>text</code> 不为 <code>null</code>；
     *      或者 <code>committedCharacterCount</code> 不在 <code>0</code>..<code>(text.getEndIndex() - text.getBeginIndex())</code> 范围内
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     */
    public InputMethodEvent(Component source, int id,
            AttributedCharacterIterator text, int committedCharacterCount,
            TextHitInfo caret, TextHitInfo visiblePosition) {
        this(source, id,
                getMostRecentEventTimeForSource(source),
                text, committedCharacterCount,
                caret, visiblePosition);
    }

    /**
     * 使用指定的源组件、类型、光标和可见位置构造一个 <code>InputMethodEvent</code>。文本设置为 <code>null</code>，<code>committedCharacterCount</code> 设置为 0。
     * <p>
     * <code>caret</code> 和 <code>visiblePosition</code> 的偏移量相对于当前组合文本；即，如果构造的事件为 <code>CARET_POSITION_CHANGED</code> 事件，则为前一个 <code>INPUT_METHOD_TEXT_CHANGED</code> 事件中的组合文本。
     * 对于没有文本的 <code>INPUT_METHOD_TEXT_CHANGED</code> 事件，<code>caret</code> 和 <code>visiblePosition</code> 必须为 <code>null</code>。
     * 事件的时间戳通过调用 {@link java.awt.EventQueue#getMostRecentEventTime()} 初始化。
     * <p>注意，传递无效的 <code>id</code> 会导致未指定的行为。如果 <code>source</code> 为 <code>null</code>，此方法将抛出 <code>IllegalArgumentException</code>。
     *
     * @param source 事件的源对象
     * @param id 事件类型
     * @param caret 光标（即插入点）；如果当前组合文本中没有光标，则为 <code>null</code>
     * @param visiblePosition 最重要的可见位置；如果当前组合文本中没有推荐的可见位置，则为 <code>null</code>
     * @throws IllegalArgumentException 如果 <code>id</code> 不在 <code>INPUT_METHOD_FIRST</code>..<code>INPUT_METHOD_LAST</code> 范围内
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     */
    public InputMethodEvent(Component source, int id, TextHitInfo caret,
            TextHitInfo visiblePosition) {
        this(source, id,
                getMostRecentEventTimeForSource(source),
                null, 0, caret, visiblePosition);
    }

    /**
     * 获取合并的已提交和组合文本。
     * 从索引 0 到索引 <code>getCommittedCharacterCount() - 1</code> 的字符是已提交文本，剩余的字符是组合文本。
     *
     * @return 文本。
     * 对于 CARET_POSITION_CHANGED，始终为 null；
     * 对于 INPUT_METHOD_TEXT_CHANGED，如果没有组合或已提交文本，可以为 null。
     */
    public AttributedCharacterIterator getText() {
        return text;
    }

    /**
     * 获取文本中已提交字符的数量。
     */
    public int getCommittedCharacterCount() {
        return committedCharacterCount;
    }

    /**
     * 获取光标。
     * <p>
     * 光标的偏移量相对于当前组合文本；即，如果这是 <code>INPUT_METHOD_TEXT_CHANGED</code> 事件，则为 <code>getText()</code> 中的组合文本；
     * 否则为前一个 <code>INPUT_METHOD_TEXT_CHANGED</code> 事件中的组合文本。
     *
     * @return 光标（即插入点）。
     * 如果当前组合文本中没有光标，则为 null。
     */
    public TextHitInfo getCaret() {
        return caret;
    }

    /**
     * 获取最重要的可见位置。
     * <p>
     * 可见位置的偏移量相对于当前组合文本；即，如果这是 <code>INPUT_METHOD_TEXT_CHANGED</code> 事件，则为 <code>getText()</code> 中的组合文本；
     * 否则为前一个 <code>INPUT_METHOD_TEXT_CHANGED</code> 事件中的组合文本。
     *
     * @return 最重要的可见位置。
     * 如果当前组合文本中没有推荐的可见位置，则为 null。
     */
    public TextHitInfo getVisiblePosition() {
        return visiblePosition;
    }

    /**
     * 消费此事件，使其不会被事件源以默认方式处理。
     */
    public void consume() {
        consumed = true;
    }

    /**
     * 返回此事件是否已被消费。
     * @see #consume
     */
    public boolean isConsumed() {
        return consumed;
    }

    /**
     * 返回此事件发生的时间戳。
     *
     * @return 事件的时间戳
     * @since 1.4
     */
    public long getWhen() {
      return when;
    }

    /**
     * 返回一个标识此事件的参数字符串。
     * 此方法对于事件日志记录和调试很有用。它包含事件ID的文本形式、已提交和组合文本的字符
     * 用 "+" 分隔、已提交字符的数量、光标和可见位置。
     *
     * @return 一个标识事件及其属性的字符串
     */
    public String paramString() {
        String typeStr;
        switch(id) {
          case INPUT_METHOD_TEXT_CHANGED:
              typeStr = "INPUT_METHOD_TEXT_CHANGED";
              break;
          case CARET_POSITION_CHANGED:
              typeStr = "CARET_POSITION_CHANGED";
              break;
          default:
              typeStr = "unknown type";
        }

        String textString;
        if (text == null) {
            textString = "no text";
        } else {
            StringBuilder textBuffer = new StringBuilder("\"");
            int committedCharacterCount = this.committedCharacterCount;
            char c = text.first();
            while (committedCharacterCount-- > 0) {
                textBuffer.append(c);
                c = text.next();
            }
            textBuffer.append("\" + \"");
            while (c != CharacterIterator.DONE) {
                textBuffer.append(c);
                c = text.next();
            }
            textBuffer.append("\"");
            textString = textBuffer.toString();
        }


                    String countString = committedCharacterCount + " characters committed";

        String caretString;
        if (caret == null) {
            caretString = "no caret";
        } else {
            caretString = "caret: " + caret.toString();
        }

        String visiblePositionString;
        if (visiblePosition == null) {
            visiblePositionString = "no visible position";
        } else {
            visiblePositionString = "visible position: " + visiblePosition.toString();
        }

        return typeStr + ", " + textString + ", " + countString + ", " + caretString + ", " + visiblePositionString;
    }

    /**
     * 如果对象输入流中不存在 <code>when</code> 字段，则初始化该字段。在这种情况下，字段将通过调用
     * {@link java.awt.EventQueue#getMostRecentEventTime()} 进行初始化。
     */
    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        if (when == 0) {
            // 不能使用 getMostRecentEventTimeForSource，因为在反序列化期间 source 始终为 null
            when = EventQueue.getMostRecentEventTime();
        }
    }

    /**
     * 获取 {@code source} 所属的 {@code EventQueue} 中最近的事件时间。
     *
     * @param source 事件的来源
     * @exception  IllegalArgumentException 如果 source 为 null。
     * @return {@code EventQueue} 中最近的事件时间
     */
    private static long getMostRecentEventTimeForSource(Object source) {
        if (source == null) {
            // 抛出 IllegalArgumentException 以符合 EventObject 规范
            throw new IllegalArgumentException("null source");
        }
        AppContext appContext = SunToolkit.targetToAppContext(source);
        EventQueue eventQueue = SunToolkit.getSystemEventQueueImplPP(appContext);
        return AWTAccessor.getEventQueueAccessor().getMostRecentEventTime(eventQueue);
    }
}
