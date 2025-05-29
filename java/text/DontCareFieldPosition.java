/*
 * Copyright (c) 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

package java.text;

/**
 * DontCareFieldPosition 定义了一个不执行任何操作的 FieldDelegate。其
 * 单例用于那些不接受 FieldPosition 的格式化方法。
 */
class DontCareFieldPosition extends FieldPosition {
    // DontCareFieldPosition 的单例。
    static final FieldPosition INSTANCE = new DontCareFieldPosition();

    private final Format.FieldDelegate noDelegate = new Format.FieldDelegate() {
        public void formatted(Format.Field attr, Object value, int start,
                              int end, StringBuffer buffer) {
        }
        public void formatted(int fieldID, Format.Field attr, Object value,
                              int start, int end, StringBuffer buffer) {
        }
    };

    private DontCareFieldPosition() {
        super(0);
    }

    Format.FieldDelegate getFieldDelegate() {
        return noDelegate;
    }
}
