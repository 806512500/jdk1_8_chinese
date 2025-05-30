
/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Locale;

/**
 * 一组控制打印页面输出的属性。
 * <p>
 * 该类的实例控制每一页的颜色状态、纸张大小（介质类型）、方向、逻辑原点、打印质量和分辨率。
 * 属性名称尽可能符合 Internet Printing Protocol (IPP) 1.1。属性值在可能的情况下部分符合。
 * <p>
 * 要使用接受内部类类型的参数的方法，传递内部类的常量字段之一的引用。客户端代码不能创建
 * 内部类类型的实例，因为这些类中没有一个具有公共构造函数。例如，要将颜色状态设置为
 * 黑白，使用以下代码：
 * <pre>
 * import java.awt.PageAttributes;
 *
 * public class MonochromeExample {
 *     public void setMonochrome(PageAttributes pageAttributes) {
 *         pageAttributes.setColor(PageAttributes.ColorType.MONOCHROME);
 *     }
 * }
 * </pre>
 * <p>
 * 每个支持 <i>attributeName</i>-default 值的 IPP 属性都有一个对应的 <code>set<i>attributeName</i>ToDefault</code> 方法。
 * 不提供默认值字段。
 *
 * @author      David Mendenhall
 * @since 1.3
 */
public final class PageAttributes implements Cloneable {
    /**
     * 可能的颜色状态的类型安全枚举。
     * @since 1.3
     */
    public static final class ColorType extends AttributeValue {
        private static final int I_COLOR = 0;
        private static final int I_MONOCHROME = 1;

        private static final String NAMES[] = {
            "color", "monochrome"
        };

        /**
         * 用于指定彩色打印的 ColorType 实例。
         */
        public static final ColorType COLOR = new ColorType(I_COLOR);
        /**
         * 用于指定黑白打印的 ColorType 实例。
         */
        public static final ColorType MONOCHROME = new ColorType(I_MONOCHROME);

        private ColorType(int type) {
            super(type, NAMES);
        }
    }

    /**
     * 可能的纸张大小的类型安全枚举。这些大小符合 IPP 1.1。
     * @since 1.3
     */
    public static final class MediaType extends AttributeValue {
        private static final int I_ISO_4A0 = 0;
        private static final int I_ISO_2A0 = 1;
        private static final int I_ISO_A0 = 2;
        private static final int I_ISO_A1 = 3;
        private static final int I_ISO_A2 = 4;
        private static final int I_ISO_A3 = 5;
        private static final int I_ISO_A4 = 6;
        private static final int I_ISO_A5 = 7;
        private static final int I_ISO_A6 = 8;
        private static final int I_ISO_A7 = 9;
        private static final int I_ISO_A8 = 10;
        private static final int I_ISO_A9 = 11;
        private static final int I_ISO_A10 = 12;
        private static final int I_ISO_B0 = 13;
        private static final int I_ISO_B1 = 14;
        private static final int I_ISO_B2 = 15;
        private static final int I_ISO_B3 = 16;
        private static final int I_ISO_B4 = 17;
        private static final int I_ISO_B5 = 18;
        private static final int I_ISO_B6 = 19;
        private static final int I_ISO_B7 = 20;
        private static final int I_ISO_B8 = 21;
        private static final int I_ISO_B9 = 22;
        private static final int I_ISO_B10 = 23;
        private static final int I_JIS_B0 = 24;
        private static final int I_JIS_B1 = 25;
        private static final int I_JIS_B2 = 26;
        private static final int I_JIS_B3 = 27;
        private static final int I_JIS_B4 = 28;
        private static final int I_JIS_B5 = 29;
        private static final int I_JIS_B6 = 30;
        private static final int I_JIS_B7 = 31;
        private static final int I_JIS_B8 = 32;
        private static final int I_JIS_B9 = 33;
        private static final int I_JIS_B10 = 34;
        private static final int I_ISO_C0 = 35;
        private static final int I_ISO_C1 = 36;
        private static final int I_ISO_C2 = 37;
        private static final int I_ISO_C3 = 38;
        private static final int I_ISO_C4 = 39;
        private static final int I_ISO_C5 = 40;
        private static final int I_ISO_C6 = 41;
        private static final int I_ISO_C7 = 42;
        private static final int I_ISO_C8 = 43;
        private static final int I_ISO_C9 = 44;
        private static final int I_ISO_C10 = 45;
        private static final int I_ISO_DESIGNATED_LONG = 46;
        private static final int I_EXECUTIVE = 47;
        private static final int I_FOLIO = 48;
        private static final int I_INVOICE = 49;
        private static final int I_LEDGER = 50;
        private static final int I_NA_LETTER = 51;
        private static final int I_NA_LEGAL = 52;
        private static final int I_QUARTO = 53;
        private static final int I_A = 54;
        private static final int I_B = 55;
        private static final int I_C = 56;
        private static final int I_D = 57;
        private static final int I_E = 58;
        private static final int I_NA_10X15_ENVELOPE = 59;
        private static final int I_NA_10X14_ENVELOPE = 60;
        private static final int I_NA_10X13_ENVELOPE = 61;
        private static final int I_NA_9X12_ENVELOPE = 62;
        private static final int I_NA_9X11_ENVELOPE = 63;
        private static final int I_NA_7X9_ENVELOPE = 64;
        private static final int I_NA_6X9_ENVELOPE = 65;
        private static final int I_NA_NUMBER_9_ENVELOPE = 66;
        private static final int I_NA_NUMBER_10_ENVELOPE = 67;
        private static final int I_NA_NUMBER_11_ENVELOPE = 68;
        private static final int I_NA_NUMBER_12_ENVELOPE = 69;
        private static final int I_NA_NUMBER_14_ENVELOPE = 70;
        private static final int I_INVITE_ENVELOPE = 71;
        private static final int I_ITALY_ENVELOPE = 72;
        private static final int I_MONARCH_ENVELOPE = 73;
        private static final int I_PERSONAL_ENVELOPE = 74;

        private static final String NAMES[] = {
            "iso-4a0", "iso-2a0", "iso-a0", "iso-a1", "iso-a2", "iso-a3",
            "iso-a4", "iso-a5", "iso-a6", "iso-a7", "iso-a8", "iso-a9",
            "iso-a10", "iso-b0", "iso-b1", "iso-b2", "iso-b3", "iso-b4",
            "iso-b5", "iso-b6", "iso-b7", "iso-b8", "iso-b9", "iso-b10",
            "jis-b0", "jis-b1", "jis-b2", "jis-b3", "jis-b4", "jis-b5",
            "jis-b6", "jis-b7", "jis-b8", "jis-b9", "jis-b10", "iso-c0",
            "iso-c1", "iso-c2", "iso-c3", "iso-c4", "iso-c5", "iso-c6",
            "iso-c7", "iso-c8", "iso-c9", "iso-c10", "iso-designated-long",
            "executive", "folio", "invoice", "ledger", "na-letter", "na-legal",
            "quarto", "a", "b", "c", "d", "e", "na-10x15-envelope",
            "na-10x14-envelope", "na-10x13-envelope", "na-9x12-envelope",
            "na-9x11-envelope", "na-7x9-envelope", "na-6x9-envelope",
            "na-number-9-envelope", "na-number-10-envelope",
            "na-number-11-envelope", "na-number-12-envelope",
            "na-number-14-envelope", "invite-envelope", "italy-envelope",
            "monarch-envelope", "personal-envelope"
        };

        /**
         * ISO/DIN 和 JIS 4A0，1682 x 2378 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_4A0 = new MediaType(I_ISO_4A0);
        /**
         * ISO/DIN 和 JIS 2A0，1189 x 1682 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_2A0 = new MediaType(I_ISO_2A0);
        /**
         * ISO/DIN 和 JIS A0，841 x 1189 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_A0 = new MediaType(I_ISO_A0);
        /**
         * ISO/DIN 和 JIS A1，594 x 841 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_A1 = new MediaType(I_ISO_A1);
        /**
         * ISO/DIN 和 JIS A2，420 x 594 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_A2 = new MediaType(I_ISO_A2);
        /**
         * ISO/DIN 和 JIS A3，297 x 420 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_A3 = new MediaType(I_ISO_A3);
        /**
         * ISO/DIN 和 JIS A4，210 x 297 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_A4 = new MediaType(I_ISO_A4);
        /**
         * ISO/DIN 和 JIS A5，148 x 210 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_A5 = new MediaType(I_ISO_A5);
        /**
         * ISO/DIN 和 JIS A6，105 x 148 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_A6 = new MediaType(I_ISO_A6);
        /**
         * ISO/DIN 和 JIS A7，74 x 105 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_A7 = new MediaType(I_ISO_A7);
        /**
         * ISO/DIN 和 JIS A8，52 x 74 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_A8 = new MediaType(I_ISO_A8);
        /**
         * ISO/DIN 和 JIS A9，37 x 52 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_A9 = new MediaType(I_ISO_A9);
        /**
         * ISO/DIN 和 JIS A10，26 x 37 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_A10 = new MediaType(I_ISO_A10);
        /**
         * ISO/DIN B0，1000 x 1414 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_B0 = new MediaType(I_ISO_B0);
        /**
         * ISO/DIN B1，707 x 1000 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_B1 = new MediaType(I_ISO_B1);
        /**
         * ISO/DIN B2，500 x 707 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_B2 = new MediaType(I_ISO_B2);
        /**
         * ISO/DIN B3，353 x 500 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_B3 = new MediaType(I_ISO_B3);
        /**
         * ISO/DIN B4，250 x 353 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_B4 = new MediaType(I_ISO_B4);
        /**
         * ISO/DIN B5，176 x 250 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_B5 = new MediaType(I_ISO_B5);
        /**
         * ISO/DIN B6，125 x 176 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_B6 = new MediaType(I_ISO_B6);
        /**
         * ISO/DIN B7，88 x 125 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_B7 = new MediaType(I_ISO_B7);
        /**
         * ISO/DIN B8，62 x 88 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_B8 = new MediaType(I_ISO_B8);
        /**
         * ISO/DIN B9，44 x 62 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_B9 = new MediaType(I_ISO_B9);
        /**
         * ISO/DIN B10，31 x 44 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_B10 = new MediaType(I_ISO_B10);
        /**
         * JIS B0，1030 x 1456 mm 的 MediaType 实例。
         */
        public static final MediaType JIS_B0 = new MediaType(I_JIS_B0);
        /**
         * JIS B1，728 x 1030 mm 的 MediaType 实例。
         */
        public static final MediaType JIS_B1 = new MediaType(I_JIS_B1);
        /**
         * JIS B2，515 x 728 mm 的 MediaType 实例。
         */
        public static final MediaType JIS_B2 = new MediaType(I_JIS_B2);
        /**
         * JIS B3，364 x 515 mm 的 MediaType 实例。
         */
        public static final MediaType JIS_B3 = new MediaType(I_JIS_B3);
        /**
         * JIS B4，257 x 364 mm 的 MediaType 实例。
         */
        public static final MediaType JIS_B4 = new MediaType(I_JIS_B4);
        /**
         * JIS B5，182 x 257 mm 的 MediaType 实例。
         */
        public static final MediaType JIS_B5 = new MediaType(I_JIS_B5);
        /**
         * JIS B6，128 x 182 mm 的 MediaType 实例。
         */
        public static final MediaType JIS_B6 = new MediaType(I_JIS_B6);
        /**
         * JIS B7，91 x 128 mm 的 MediaType 实例。
         */
        public static final MediaType JIS_B7 = new MediaType(I_JIS_B7);
        /**
         * JIS B8，64 x 91 mm 的 MediaType 实例。
         */
        public static final MediaType JIS_B8 = new MediaType(I_JIS_B8);
        /**
         * JIS B9，45 x 64 mm 的 MediaType 实例。
         */
        public static final MediaType JIS_B9 = new MediaType(I_JIS_B9);
        /**
         * JIS B10，32 x 45 mm 的 MediaType 实例。
         */
        public static final MediaType JIS_B10 = new MediaType(I_JIS_B10);
        /**
         * ISO/DIN C0，917 x 1297 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_C0 = new MediaType(I_ISO_C0);
        /**
         * ISO/DIN C1，648 x 917 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_C1 = new MediaType(I_ISO_C1);
        /**
         * ISO/DIN C2，458 x 648 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_C2 = new MediaType(I_ISO_C2);
        /**
         * ISO/DIN C3，324 x 458 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_C3 = new MediaType(I_ISO_C3);
        /**
         * ISO/DIN C4，229 x 324 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_C4 = new MediaType(I_ISO_C4);
        /**
         * ISO/DIN C5，162 x 229 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_C5 = new MediaType(I_ISO_C5);
        /**
         * ISO/DIN C6，114 x 162 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_C6 = new MediaType(I_ISO_C6);
        /**
         * ISO/DIN C7，81 x 114 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_C7 = new MediaType(I_ISO_C7);
        /**
         * ISO/DIN C8，57 x 81 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_C8 = new MediaType(I_ISO_C8);
        /**
         * ISO/DIN C9，40 x 57 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_C9 = new MediaType(I_ISO_C9);
        /**
         * ISO/DIN C10，28 x 40 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_C10 = new MediaType(I_ISO_C10);
        /**
         * ISO Designated Long，110 x 220 mm 的 MediaType 实例。
         */
        public static final MediaType ISO_DESIGNATED_LONG =
            new MediaType(I_ISO_DESIGNATED_LONG);
        /**
         * Executive，7 1/4 x 10 1/2 in 的 MediaType 实例。
         */
        public static final MediaType EXECUTIVE = new MediaType(I_EXECUTIVE);
        /**
         * Folio，8 1/2 x 13 in 的 MediaType 实例。
         */
        public static final MediaType FOLIO = new MediaType(I_FOLIO);
        /**
         * Invoice，5 1/2 x 8 1/2 in 的 MediaType 实例。
         */
        public static final MediaType INVOICE = new MediaType(I_INVOICE);
        /**
         * Ledger，11 x 17 in 的 MediaType 实例。
         */
        public static final MediaType LEDGER = new MediaType(I_LEDGER);
        /**
         * North American Letter，8 1/2 x 11 in 的 MediaType 实例。
         */
        public static final MediaType NA_LETTER = new MediaType(I_NA_LETTER);
        /**
         * North American Legal，8 1/2 x 14 in 的 MediaType 实例。
         */
        public static final MediaType NA_LEGAL = new MediaType(I_NA_LEGAL);
        /**
         * Quarto，215 x 275 mm 的 MediaType 实例。
         */
        public static final MediaType QUARTO = new MediaType(I_QUARTO);
        /**
         * Engineering A，8 1/2 x 11 in 的 MediaType 实例。
         */
        public static final MediaType A = new MediaType(I_A);
        /**
         * Engineering B，11 x 17 in 的 MediaType 实例。
         */
        public static final MediaType B = new MediaType(I_B);
        /**
         * Engineering C，17 x 22 in 的 MediaType 实例。
         */
        public static final MediaType C = new MediaType(I_C);
        /**
         * Engineering D，22 x 34 in 的 MediaType 实例。
         */
        public static final MediaType D = new MediaType(I_D);
        /**
         * Engineering E，34 x 44 in 的 MediaType 实例。
         */
        public static final MediaType E = new MediaType(I_E);
        /**
         * North American 10 x 15 in 的 MediaType 实例。
         */
        public static final MediaType NA_10X15_ENVELOPE =
            new MediaType(I_NA_10X15_ENVELOPE);
        /**
         * North American 10 x 14 in 的 MediaType 实例。
         */
        public static final MediaType NA_10X14_ENVELOPE =
            new MediaType(I_NA_10X14_ENVELOPE);
        /**
         * North American 10 x 13 in 的 MediaType 实例。
         */
        public static final MediaType NA_10X13_ENVELOPE =
            new MediaType(I_NA_10X13_ENVELOPE);
        /**
         * North American 9 x 12 in 的 MediaType 实例。
         */
        public static final MediaType NA_9X12_ENVELOPE =
            new MediaType(I_NA_9X12_ENVELOPE);
        /**
         * North American 9 x 11 in 的 MediaType 实例。
         */
        public static final MediaType NA_9X11_ENVELOPE =
            new MediaType(I_NA_9X11_ENVELOPE);
        /**
         * North American 7 x 9 in 的 MediaType 实例。
         */
        public static final MediaType NA_7X9_ENVELOPE =
            new MediaType(I_NA_7X9_ENVELOPE);
        /**
         * North American 6 x 9 in 的 MediaType 实例。
         */
        public static final MediaType NA_6X9_ENVELOPE =
            new MediaType(I_NA_6X9_ENVELOPE);
        /**
         * North American #9 商务信封，3 7/8 x 8 7/8 in 的 MediaType 实例。
         */
        public static final MediaType NA_NUMBER_9_ENVELOPE =
            new MediaType(I_NA_NUMBER_9_ENVELOPE);
        /**
         * North American #10 商务信封，4 1/8 x 9 1/2 in 的 MediaType 实例。
         */
        public static final MediaType NA_NUMBER_10_ENVELOPE =
            new MediaType(I_NA_NUMBER_10_ENVELOPE);
        /**
         * North American #11 商务信封，4 1/2 x 10 3/8 in 的 MediaType 实例。
         */
        public static final MediaType NA_NUMBER_11_ENVELOPE =
            new MediaType(I_NA_NUMBER_11_ENVELOPE);
        /**
         * North American #12 商务信封，4 3/4 x 11 in 的 MediaType 实例。
         */
        public static final MediaType NA_NUMBER_12_ENVELOPE =
            new MediaType(I_NA_NUMBER_12_ENVELOPE);
        /**
         * North American #14 商务信封，5 x 11 1/2 in 的 MediaType 实例。
         */
        public static final MediaType NA_NUMBER_14_ENVELOPE =
            new MediaType(I_NA_NUMBER_14_ENVELOPE);
        /**
         * 邀请信封，220 x 220 mm 的 MediaType 实例。
         */
        public static final MediaType INVITE_ENVELOPE =
            new MediaType(I_INVITE_ENVELOPE);
        /**
         * 意大利信封，110 x 230 mm 的 MediaType 实例。
         */
        public static final MediaType ITALY_ENVELOPE =
            new MediaType(I_ITALY_ENVELOPE);
        /**
         * 王朝信封，3 7/8 x 7 1/2 in 的 MediaType 实例。
         */
        public static final MediaType MONARCH_ENVELOPE =
            new MediaType(I_MONARCH_ENVELOPE);
        /**
         * 6 3/4 信封，3 5/8 x 6 1/2 in 的 MediaType 实例。
         */
        public static final MediaType PERSONAL_ENVELOPE =
            new MediaType(I_PERSONAL_ENVELOPE);
        /**
         * ISO_A0 的别名。
         */
        public static final MediaType A0 = ISO_A0;
        /**
         * ISO_A1 的别名。
         */
        public static final MediaType A1 = ISO_A1;
        /**
         * ISO_A2 的别名。
         */
        public static final MediaType A2 = ISO_A2;
        /**
         * ISO_A3 的别名。
         */
        public static final MediaType A3 = ISO_A3;
        /**
         * ISO_A4 的别名。
         */
        public static final MediaType A4 = ISO_A4;
        /**
         * ISO_A5 的别名。
         */
        public static final MediaType A5 = ISO_A5;
        /**
         * ISO_A6 的别名。
         */
        public static final MediaType A6 = ISO_A6;
        /**
         * ISO_A7 的别名。
         */
        public static final MediaType A7 = ISO_A7;
        /**
         * ISO_A8 的别名。
         */
        public static final MediaType A8 = ISO_A8;
        /**
         * ISO_A9 的别名。
         */
        public static final MediaType A9 = ISO_A9;
        /**
         * ISO_A10 的别名。
         */
        public static final MediaType A10 = ISO_A10;
        /**
         * ISO_B0 的别名。
         */
        public static final MediaType B0 = ISO_B0;
        /**
         * ISO_B1 的别名。
         */
        public static final MediaType B1 = ISO_B1;
        /**
         * ISO_B2 的别名。
         */
        public static final MediaType B2 = ISO_B2;
        /**
         * ISO_B3 的别名。
         */
        public static final MediaType B3 = ISO_B3;
        /**
         * ISO_B4 的别名。
         */
        public static final MediaType B4 = ISO_B4;
        /**
         * ISO_B4 的别名。
         */
        public static final MediaType ISO_B4_ENVELOPE = ISO_B4;
        /**
         * ISO_B5 的别名。
         */
        public static final MediaType B5 = ISO_B5;
        /**
         * ISO_B5 的别名。
         */
        public static final MediaType ISO_B5_ENVELOPE = ISO_B5;
        /**
         * ISO_B6 的别名。
         */
        public static final MediaType B6 = ISO_B6;
        /**
         * ISO_B7 的别名。
         */
        public static final MediaType B7 = ISO_B7;
        /**
         * ISO_B8 的别名。
         */
        public static final MediaType B8 = ISO_B8;
        /**
         * ISO_B9 的别名。
         */
        public static final MediaType B9 = ISO_B9;
        /**
         * ISO_B10 的别名。
         */
        public static final MediaType B10 = ISO_B10;
        /**
         * ISO_C0 的别名。
         */
        public static final MediaType C0 = ISO_C0;
        /**
         * ISO_C0 的别名。
         */
        public static final MediaType ISO_C0_ENVELOPE = ISO_C0;
        /**
         * ISO_C1 的别名。
         */
        public static final MediaType C1 = ISO_C1;
        /**
         * ISO_C1 的别名。
         */
        public static final MediaType ISO_C1_ENVELOPE = ISO_C1;
        /**
         * ISO_C2 的别名。
         */
        public static final MediaType C2 = ISO_C2;
        /**
         * ISO_C2 的别名。
         */
        public static final MediaType ISO_C2_ENVELOPE = ISO_C2;
        /**
         * ISO_C3 的别名。
         */
        public static final MediaType C3 = ISO_C3;
        /**
         * ISO_C3 的别名。
         */
        public static final MediaType ISO_C3_ENVELOPE = ISO_C3;
        /**
         * ISO_C4 的别名。
         */
        public static final MediaType C4 = ISO_C4;
        /**
         * ISO_C4 的别名。
         */
        public static final MediaType ISO_C4_ENVELOPE = ISO_C4;
        /**
         * ISO_C5 的别名。
         */
        public static final MediaType C5 = ISO_C5;
        /**
         * ISO_C5 的别名。
         */
        public static final MediaType ISO_C5_ENVELOPE = ISO_C5;
        /**
         * ISO_C6 的别名。
         */
        public static final MediaType C6 = ISO_C6;
        /**
         * ISO_C6 的别名。
         */
        public static final MediaType ISO_C6_ENVELOPE = ISO_C6;
        /**
         * ISO_C7 的别名。
         */
        public static final MediaType C7 = ISO_C7;
        /**
         * ISO_C7 的别名。
         */
        public static final MediaType ISO_C7_ENVELOPE = ISO_C7;
        /**
         * ISO_C8 的别名。
         */
        public static final MediaType C8 = ISO_C8;
        /**
         * ISO_C8 的别名。
         */
        public static final MediaType ISO_C8_ENVELOPE = ISO_C8;
        /**
         * ISO_C9 的别名。
         */
        public static final MediaType C9 = ISO_C9;
        /**
         * ISO_C9 的别名。
         */
        public static final MediaType ISO_C9_ENVELOPE = ISO_C9;
        /**
         * ISO_C10 的别名。
         */
        public static final MediaType C10 = ISO_C10;
        /**
         * ISO_C10 的别名。
         */
        public static final MediaType ISO_C10_ENVELOPE = ISO_C10;
        /**
         * ISO_DESIGNATED_LONG 的别名。
         */
        public static final MediaType ISO_DESIGNATED_LONG_ENVELOPE =
                ISO_DESIGNATED_LONG;
        /**
         * INVOICE 的别名。
         */
        public static final MediaType STATEMENT = INVOICE;
        /**
         * LEDGER 的别名。
         */
        public static final MediaType TABLOID = LEDGER;
        /**
         * NA_LETTER 的别名。
         */
        public static final MediaType LETTER = NA_LETTER;
        /**
         * NA_LETTER 的别名。
         */
        public static final MediaType NOTE = NA_LETTER;
        /**
         * NA_LEGAL 的别名。
         */
        public static final MediaType LEGAL = NA_LEGAL;
        /**
         * NA_10X15_ENVELOPE 的别名。
         */
        public static final MediaType ENV_10X15 = NA_10X15_ENVELOPE;
        /**
         * NA_10X14_ENVELOPE 的别名。
         */
        public static final MediaType ENV_10X14 = NA_10X14_ENVELOPE;
        /**
         * NA_10X13_ENVELOPE 的别名。
         */
        public static final MediaType ENV_10X13 = NA_10X13_ENVELOPE;
        /**
         * NA_9X12_ENVELOPE 的别名。
         */
        public static final MediaType ENV_9X12 = NA_9X12_ENVELOPE;
        /**
         * NA_9X11_ENVELOPE 的别名。
         */
        public static final MediaType ENV_9X11 = NA_9X11_ENVELOPE;
        /**
         * NA_7X9_ENVELOPE 的别名。
         */
        public static final MediaType ENV_7X9 = NA_7X9_ENVELOPE;
        /**
         * NA_6X9_ENVELOPE 的别名。
         */
        public static final MediaType ENV_6X9 = NA_6X9_ENVELOPE;
        /**
         * NA_NUMBER_9_ENVELOPE 的别名。
         */
        public static final MediaType ENV_9 = NA_NUMBER_9_ENVELOPE;
        /**
         * NA_NUMBER_10_ENVELOPE 的别名。
         */
        public static final MediaType ENV_10 = NA_NUMBER_10_ENVELOPE;
        /**
         * NA_NUMBER_11_ENVELOPE 的别名。
         */
        public static final MediaType ENV_11 = NA_NUMBER_11_ENVELOPE;
        /**
         * NA_NUMBER_12_ENVELOPE 的别名。
         */
        public static final MediaType ENV_12 = NA_NUMBER_12_ENVELOPE;
        /**
         * NA_NUMBER_14_ENVELOPE 的别名。
         */
        public static final MediaType ENV_14 = NA_NUMBER_14_ENVELOPE;
        /**
         * INVITE_ENVELOPE 的别名。
         */
        public static final MediaType ENV_INVITE = INVITE_ENVELOPE;
        /**
         * ITALY_ENVELOPE 的别名。
         */
        public static final MediaType ENV_ITALY = ITALY_ENVELOPE;
        /**
         * MONARCH_ENVELOPE 的别名。
         */
        public static final MediaType ENV_MONARCH = MONARCH_ENVELOPE;
        /**
         * PERSONAL_ENVELOPE 的别名。
         */
        public static final MediaType ENV_PERSONAL = PERSONAL_ENVELOPE;
        /**
         * INVITE_ENVELOPE 的别名。
         */
        public static final MediaType INVITE = INVITE_ENVELOPE;
        /**
         * ITALY_ENVELOPE 的别名。
         */
        public static final MediaType ITALY = ITALY_ENVELOPE;
        /**
         * MONARCH_ENVELOPE 的别名。
         */
        public static final MediaType MONARCH = MONARCH_ENVELOPE;
        /**
         * PERSONAL_ENVELOPE 的别名。
         */
        public static final MediaType PERSONAL = PERSONAL_ENVELOPE;


                    private MediaType(int type) {
            super(type, NAMES);
        }
    }

    /**
     * 一种类型安全的可能方向枚举。这些方向部分符合 IPP 1.1。
     * @since 1.3
     */
    public static final class OrientationRequestedType extends AttributeValue {
        private static final int I_PORTRAIT = 0;
        private static final int I_LANDSCAPE = 1;

        private static final String NAMES[] = {
            "portrait", "landscape"
        };

        /**
         * 用于指定肖像方向的 OrientationRequestedType 实例。
         */
        public static final OrientationRequestedType PORTRAIT =
            new OrientationRequestedType(I_PORTRAIT);
        /**
         * 用于指定风景方向的 OrientationRequestedType 实例。
         */
        public static final OrientationRequestedType LANDSCAPE =
            new OrientationRequestedType(I_LANDSCAPE);

        private OrientationRequestedType(int type) {
            super(type, NAMES);
        }
    }

    /**
     * 一种类型安全的可能原点枚举。
     * @since 1.3
     */
    public static final class OriginType extends AttributeValue {
        private static final int I_PHYSICAL = 0;
        private static final int I_PRINTABLE = 1;

        private static final String NAMES[] = {
            "physical", "printable"
        };

        /**
         * 用于指定物理原点的 OriginType 实例。
         */
        public static final OriginType PHYSICAL = new OriginType(I_PHYSICAL);
        /**
         * 用于指定可打印原点的 OriginType 实例。
         */
        public static final OriginType PRINTABLE = new OriginType(I_PRINTABLE);

        private OriginType(int type) {
            super(type, NAMES);
        }
    }

    /**
     * 一种类型安全的可能打印质量枚举。这些打印质量符合 IPP 1.1。
     * @since 1.3
     */
    public static final class PrintQualityType extends AttributeValue {
        private static final int I_HIGH = 0;
        private static final int I_NORMAL = 1;
        private static final int I_DRAFT = 2;

        private static final String NAMES[] = {
            "high", "normal", "draft"
        };

        /**
         * 用于指定高打印质量的 PrintQualityType 实例。
         */
        public static final PrintQualityType HIGH =
            new PrintQualityType(I_HIGH);
        /**
         * 用于指定正常打印质量的 PrintQualityType 实例。
         */
        public static final PrintQualityType NORMAL =
            new PrintQualityType(I_NORMAL);
        /**
         * 用于指定草稿打印质量的 PrintQualityType 实例。
         */
        public static final PrintQualityType DRAFT =
            new PrintQualityType(I_DRAFT);

        private PrintQualityType(int type) {
            super(type, NAMES);
        }
    }

    private ColorType color;
    private MediaType media;
    private OrientationRequestedType orientationRequested;
    private OriginType origin;
    private PrintQualityType printQuality;
    private int[] printerResolution;

    /**
     * 构造一个具有每个属性默认值的 PageAttributes 实例。
     */
    public PageAttributes() {
        setColor(ColorType.MONOCHROME);
        setMediaToDefault();
        setOrientationRequestedToDefault();
        setOrigin(OriginType.PHYSICAL);
        setPrintQualityToDefault();
        setPrinterResolutionToDefault();
    }

    /**
     * 构造一个 PageAttributes 实例，该实例是提供的 PageAttributes 的副本。
     *
     * @param   obj 要复制的 PageAttributes。
     */
    public PageAttributes(PageAttributes obj) {
        set(obj);
    }

    /**
     * 构造一个具有每个属性指定值的 PageAttributes 实例。
     *
     * @param   color ColorType.COLOR 或 ColorType.MONOCHROME。
     * @param   media MediaType 类的常量字段之一。
     * @param   orientationRequested OrientationRequestedType.PORTRAIT 或
     *          OrientationRequestedType.LANDSCAPE。
     * @param   origin OriginType.PHYSICAL 或 OriginType.PRINTABLE
     * @param   printQuality PrintQualityType.DRAFT, PrintQualityType.NORMAL,
     *          或 PrintQualityType.HIGH
     * @param   printerResolution 一个包含 3 个元素的整数数组。第一个元素必须大于 0。第二个元素必须
     *          大于 0。第三个元素必须是 <code>3</code> 或 <code>4</code>。
     * @throws  IllegalArgumentException 如果上述条件之一被违反。
     */
    public PageAttributes(ColorType color, MediaType media,
                          OrientationRequestedType orientationRequested,
                          OriginType origin, PrintQualityType printQuality,
                          int[] printerResolution) {
        setColor(color);
        setMedia(media);
        setOrientationRequested(orientationRequested);
        setOrigin(origin);
        setPrintQuality(printQuality);
        setPrinterResolution(printerResolution);
    }

    /**
     * 创建并返回此 PageAttributes 的副本。
     *
     * @return  新创建的副本。可以安全地将此 Object 转换为 PageAttributes。
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // 由于我们实现了 Cloneable，这不应该发生
            throw new InternalError(e);
        }
    }

    /**
     * 将此 PageAttributes 的所有属性设置为与 obj 的属性相同的值。
     *
     * @param   obj 要复制的 PageAttributes。
     */
    public void set(PageAttributes obj) {
        color = obj.color;
        media = obj.media;
        orientationRequested = obj.orientationRequested;
        origin = obj.origin;
        printQuality = obj.printQuality;
        // 没问题，因为我们从不修改 printerResolution 的内容
        printerResolution = obj.printerResolution;
    }

    /**
     * 返回使用这些属性的页面是否将以彩色或单色呈现。此属性将更新为用户选择的值。
     *
     * @return  ColorType.COLOR 或 ColorType.MONOCHROME。
     */
    public ColorType getColor() {
        return color;
    }

    /**
     * 指定使用这些属性的页面是否将以彩色或单色呈现。不指定此属性等同于指定 ColorType.MONOCHROME。
     *
     * @param   color ColorType.COLOR 或 ColorType.MONOCHROME。
     * @throws  IllegalArgumentException 如果 color 为 null。
     */
    public void setColor(ColorType color) {
        if (color == null) {
            throw new IllegalArgumentException("属性 color 的无效值");
        }
        this.color = color;
    }

    /**
     * 返回使用这些属性的页面的纸张大小。此属性将更新为用户选择的值。
     *
     * @return  MediaType 类的常量字段之一。
     */
    public MediaType getMedia() {
        return media;
    }

    /**
     * 指定使用这些属性的页面的所需纸张大小。实际纸张大小将由目标打印机的限制确定。如果找不到精确匹配，实现将选择最接近的可能匹配。不指定此属性等同于指定默认区域设置的默认大小。美国和加拿大的默认大小为
     * MediaType.NA_LETTER。其他所有区域的默认大小为 MediaType.ISO_A4。
     *
     * @param   media MediaType 类的常量字段之一。
     * @throws  IllegalArgumentException 如果 media 为 null。
     */
    public void setMedia(MediaType media) {
        if (media == null) {
            throw new IllegalArgumentException("属性 media 的无效值");
        }
        this.media = media;
    }

    /**
     * 将使用这些属性的页面的纸张大小设置为默认区域设置的默认大小。美国和加拿大的默认大小为 MediaType.NA_LETTER。其他所有区域的默认大小为
     * MediaType.ISO_A4。
     */
    public void setMediaToDefault(){
        String defaultCountry = Locale.getDefault().getCountry();
        if (defaultCountry != null &&
            (defaultCountry.equals(Locale.US.getCountry()) ||
             defaultCountry.equals(Locale.CANADA.getCountry()))) {
            setMedia(MediaType.NA_LETTER);
        } else {
            setMedia(MediaType.ISO_A4);
        }
    }

    /**
     * 返回使用这些属性的页面的打印方向。此属性将更新为用户选择的值。
     *
     * @return  OrientationRequestedType.PORTRAIT 或
     *          OrientationRequestedType.LANDSCAPE。
     */
    public OrientationRequestedType getOrientationRequested() {
        return orientationRequested;
    }

    /**
     * 指定使用这些属性的页面的打印方向。不指定此属性等同于指定
     * OrientationRequestedType.PORTRAIT。
     *
     * @param   orientationRequested OrientationRequestedType.PORTRAIT 或
     *          OrientationRequestedType.LANDSCAPE。
     * @throws  IllegalArgumentException 如果 orientationRequested 为 null。
     */
    public void setOrientationRequested(OrientationRequestedType
                                        orientationRequested) {
        if (orientationRequested == null) {
            throw new IllegalArgumentException("属性 orientationRequested 的无效值");
        }
        this.orientationRequested = orientationRequested;
    }

    /**
     * 指定使用这些属性的页面的打印方向。
     * 指定 <code>3</code> 表示肖像。指定 <code>4</code> 表示风景。指定任何其他值将生成
     * IllegalArgumentException。不指定此属性等同于调用 setOrientationRequested(OrientationRequestedType.PORTRAIT)。
     *
     * @param   orientationRequested <code>3</code> 或 <code>4</code>
     * @throws  IllegalArgumentException 如果 orientationRequested 不是
     *          <code>3</code> 或 <code>4</code>
     */
    public void setOrientationRequested(int orientationRequested) {
        switch (orientationRequested) {
          case 3:
            setOrientationRequested(OrientationRequestedType.PORTRAIT);
            break;
          case 4:
            setOrientationRequested(OrientationRequestedType.LANDSCAPE);
            break;
          default:
            // 这将抛出 IllegalArgumentException
            setOrientationRequested(null);
            break;
        }
    }

    /**
     * 将使用这些属性的页面的打印方向设置为默认。默认方向为肖像。
     */
    public void setOrientationRequestedToDefault() {
        setOrientationRequested(OrientationRequestedType.PORTRAIT);
    }

    /**
     * 返回使用这些属性的页面在 (0, 0) 处绘制是否在物理页面的左上角，或在可打印区域的左上角。（注意，这些位置可能是等效的。）此属性不能被修改，
     * 也不受实现或目标打印机的任何限制。
     *
     * @return  OriginType.PHYSICAL 或 OriginType.PRINTABLE
     */
    public OriginType getOrigin() {
        return origin;
    }

    /**
     * 指定使用这些属性的页面在 (0, 0) 处绘制是否在物理页面的左上角，或在可打印区域的左上角。（注意，这些位置可能是等效的。）不指定此属性等同于指定
     * OriginType.PHYSICAL。
     *
     * @param   origin OriginType.PHYSICAL 或 OriginType.PRINTABLE
     * @throws  IllegalArgumentException 如果 origin 为 null。
     */
    public void setOrigin(OriginType origin) {
        if (origin == null) {
            throw new IllegalArgumentException("属性 origin 的无效值");
        }
        this.origin = origin;
    }

    /**
     * 返回使用这些属性的页面的打印质量。此属性将更新为用户选择的值。
     *
     * @return  PrintQualityType.DRAFT, PrintQualityType.NORMAL, 或
     *          PrintQualityType.HIGH
     */
    public PrintQualityType getPrintQuality() {
        return printQuality;
    }

    /**
     * 指定使用这些属性的页面的打印质量。不指定此属性等同于指定
     * PrintQualityType.NORMAL。
     *
     * @param   printQuality PrintQualityType.DRAFT, PrintQualityType.NORMAL,
     *          或 PrintQualityType.HIGH
     * @throws  IllegalArgumentException 如果 printQuality 为 null。
     */
    public void setPrintQuality(PrintQualityType printQuality) {
        if (printQuality == null) {
            throw new IllegalArgumentException("属性 printQuality 的无效值");
        }
        this.printQuality = printQuality;
    }

    /**
     * 指定使用这些属性的页面的打印质量。
     * 指定 <code>3</code> 表示草稿。指定 <code>4</code> 表示正常。指定 <code>5</code> 表示高。指定
     * 任何其他值将生成 IllegalArgumentException。不指定此属性等同于调用
     * setPrintQuality(PrintQualityType.NORMAL)。
     *
     * @param   printQuality <code>3</code>, <code>4</code>, 或 <code>5</code>
     * @throws  IllegalArgumentException 如果 printQuality 不是 <code>3
     *          </code>, <code>4</code>, 或 <code>5</code>
     */
    public void setPrintQuality(int printQuality) {
        switch (printQuality) {
          case 3:
            setPrintQuality(PrintQualityType.DRAFT);
            break;
          case 4:
            setPrintQuality(PrintQualityType.NORMAL);
            break;
          case 5:
            setPrintQuality(PrintQualityType.HIGH);
            break;
          default:
            // 这将抛出 IllegalArgumentException
            setPrintQuality(null);
            break;
        }
    }

    /**
     * 将使用这些属性的页面的打印质量设置为默认。默认打印质量为正常。
     */
    public void setPrintQualityToDefault() {
        setPrintQuality(PrintQualityType.NORMAL);
    }


                /**
     * 返回使用这些属性的页面的打印分辨率。
     * 数组的索引 0 指定横向分辨率（通常是水平分辨率）。数组的索引 1 指定纵向分辨率（通常是垂直分辨率）。
     * 数组的索引 2 指定分辨率是以每英寸点数还是每厘米点数表示。<code>3</code> 表示每英寸点数。<code>4</code> 表示每厘米点数。
     *
     * @return  一个包含 3 个元素的整数数组。第一个元素必须大于 0。第二个元素必须大于 0。第三个元素必须是 <code>3</code> 或 <code>4</code>。
     */
    public int[] getPrinterResolution() {
        // 返回一个副本，因为否则客户端代码可以通过修改返回的数组来绕过在 setPrinterResolution 中进行的检查。
        int[] copy = new int[3];
        copy[0] = printerResolution[0];
        copy[1] = printerResolution[1];
        copy[2] = printerResolution[2];
        return copy;
    }

    /**
     * 指定使用这些属性的页面的期望打印分辨率。
     * 实际分辨率将由实现和目标打印机的限制决定。数组的索引 0 指定横向分辨率（通常是水平分辨率）。
     * 数组的索引 1 指定纵向分辨率（通常是垂直分辨率）。数组的索引 2 指定分辨率是以每英寸点数还是每厘米点数表示。
     * <code>3</code> 表示每英寸点数。<code>4</code> 表示每厘米点数。注意，1.1 打印实现（Toolkit.getPrintJob）要求横向和纵向分辨率相同。
     * 不指定该属性等同于调用 setPrinterResolution(72)。
     *
     * @param   printerResolution 一个包含 3 个元素的整数数组。第一个元素必须大于 0。第二个元素必须大于 0。第三个元素必须是 <code>3</code> 或 <code>4</code>。
     * @throws  IllegalArgumentException 如果上述条件中的一个或多个被违反。
     */
    public void setPrinterResolution(int[] printerResolution) {
        if (printerResolution == null ||
            printerResolution.length != 3 ||
            printerResolution[0] <= 0 ||
            printerResolution[1] <= 0 ||
            (printerResolution[2] != 3 && printerResolution[2] != 4)) {
            throw new IllegalArgumentException("Invalid value for attribute "+
                                               "printerResolution");
        }
        // 存储一个副本，因为否则客户端代码可以通过持有数组的引用并在调用 setPrinterResolution 后修改它来绕过上述检查。
        int[] copy = new int[3];
        copy[0] = printerResolution[0];
        copy[1] = printerResolution[1];
        copy[2] = printerResolution[2];
        this.printerResolution = copy;
    }

    /**
     * 指定使用这些属性的页面的期望横向和纵向打印分辨率（以每英寸点数为单位）。两个分辨率使用相同的值。
     * 实际分辨率将由实现和目标打印机的限制决定。不指定该属性等同于指定 <code>72</code>。
     *
     * @param   printerResolution 一个大于 0 的整数。
     * @throws  IllegalArgumentException 如果 printerResolution 小于或等于 0。
     */
    public void setPrinterResolution(int printerResolution) {
        setPrinterResolution(new int[] { printerResolution, printerResolution,
                                         3 } );
    }

    /**
     * 将使用这些属性的页面的打印分辨率设置为默认值。默认值是横向和纵向分辨率均为 72 dpi。
     */
    public void setPrinterResolutionToDefault() {
        setPrinterResolution(72);
    }

    /**
     * 确定两个 PageAttributes 是否相等。
     * <p>
     * 两个 PageAttributes 相等当且仅当它们的每个属性都相等。枚举类型的属性相等当且仅当字段引用相同的唯一枚举对象。
     * 这意味着别名媒体等于其底层的唯一媒体。打印分辨率相等当且仅当纵向分辨率、横向分辨率和单位都相等。
     *
     * @param   obj 要检查其相等性的对象。
     * @return  根据上述标准，obj 是否等于此 PageAttribute。
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof PageAttributes)) {
            return false;
        }

        PageAttributes rhs = (PageAttributes)obj;

        return (color == rhs.color &&
                media == rhs.media &&
                orientationRequested == rhs.orientationRequested &&
                origin == rhs.origin &&
                printQuality == rhs.printQuality &&
                printerResolution[0] == rhs.printerResolution[0] &&
                printerResolution[1] == rhs.printerResolution[1] &&
                printerResolution[2] == rhs.printerResolution[2]);
    }

    /**
     * 返回此 PageAttributes 的哈希码值。
     *
     * @return  哈希码。
     */
    public int hashCode() {
        return (color.hashCode() << 31 ^
                media.hashCode() << 24 ^
                orientationRequested.hashCode() << 23 ^
                origin.hashCode() << 22 ^
                printQuality.hashCode() << 20 ^
                printerResolution[2] >> 2 << 19 ^
                printerResolution[1] << 10 ^
                printerResolution[0]);
    }

    /**
     * 返回此 PageAttributes 的字符串表示形式。
     *
     * @return  字符串表示形式。
     */
    public String toString() {
        // int[] printerResolution = getPrinterResolution();
        return "color=" + getColor() + ",media=" + getMedia() +
            ",orientation-requested=" + getOrientationRequested() +
            ",origin=" + getOrigin() + ",print-quality=" + getPrintQuality() +
            ",printer-resolution=[" + printerResolution[0] + "," +
            printerResolution[1] + "," + printerResolution[2] + "]";
    }
}
