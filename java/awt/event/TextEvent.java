/*
 * Copyright (c) 1996, 2008, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.AWTEvent;

/**
 * 表示对象的文本已更改的语义事件。
 * 这个高级事件由对象（如 TextComponent）在其文本更改时生成。该事件传递给
 * 每个使用组件的 <code>addTextListener</code> 方法注册以接收此类事件的
 * <code>TextListener</code> 对象。
 * <P>
 * 实现 <code>TextListener</code> 接口的对象在事件发生时会收到此 <code>TextEvent</code>。
 * 监听器可以处理“有意义的”（语义的）事件，如“文本已更改”，而无需处理单个鼠标移动和按键事件的细节。
 * <p>
 * 如果任何特定 <code>TextEvent</code> 实例的 {@code id} 参数不在
 * {@code TEXT_FIRST} 到 {@code TEXT_LAST} 的范围内，则会导致未指定的行为。
 *
 * @author Georges Saab
 *
 * @see java.awt.TextComponent
 * @see TextListener
 *
 * @since 1.1
 */

public class TextEvent extends AWTEvent {

    /**
     * 用于文本事件的 id 范围中的第一个数字。
     */
    public static final int TEXT_FIRST  = 900;

    /**
     * 用于文本事件的 id 范围中的最后一个数字。
     */
    public static final int TEXT_LAST   = 900;

    /**
     * 此事件 id 表示对象的文本已更改。
     */
    public static final int TEXT_VALUE_CHANGED  = TEXT_FIRST;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = 6269902291250941179L;

    /**
     * 构造一个 <code>TextEvent</code> 对象。
     * <p> 如果 <code>source</code> 为 <code>null</code>，此方法将抛出
     * <code>IllegalArgumentException</code>。
     *
     * @param source 事件的来源对象（<code>TextComponent</code>）
     * @param id 一个标识事件类型的整数。
     *          有关允许值的信息，请参阅 {@link TextEvent} 的类描述
     * @throws IllegalArgumentException 如果 <code>source</code> 为 null
     * @see #getSource()
     * @see #getID()
     */
    public TextEvent(Object source, int id) {
        super(source, id);
    }


    /**
     * 返回一个标识此文本事件的参数字符串。
     * 此方法对于事件记录和调试非常有用。
     *
     * @return 一个标识事件及其属性的字符串
     */
    public String paramString() {
        String typeStr;
        switch(id) {
          case TEXT_VALUE_CHANGED:
              typeStr = "TEXT_VALUE_CHANGED";
              break;
          default:
              typeStr = "unknown type";
        }
        return typeStr;
    }
}
