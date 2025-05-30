/*
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.dnd;

import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * 测试一个对象是否可以真正被序列化，通过将其序列化到一个 null 的 OutputStream。
 *
 * @since 1.4
 */
final class SerializationTester {
    private static ObjectOutputStream stream;
    static {
        try {
            stream = new ObjectOutputStream(new OutputStream() {
                    public void write(int b) {}
                });
        } catch (IOException cannotHappen) {
        }
    }

    static boolean test(Object obj) {
        if (!(obj instanceof Serializable)) {
            return false;
        }

        try {
            stream.writeObject(obj);
        } catch (IOException e) {
            return false;
        } finally {
            // 修复 4503661。
            // 重置流，使其不保留对已写对象的引用。
            try {
                stream.reset();
            } catch (IOException e) {
                // 忽略异常。
            }
        }
        return true;
    }

    private SerializationTester() {}
}
