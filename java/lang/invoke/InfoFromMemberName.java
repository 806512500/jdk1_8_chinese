/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.invoke;

import java.security.*;
import java.lang.reflect.*;
import java.lang.invoke.MethodHandleNatives.Constants;
import java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodHandleStatics.*;

/*
 * MethodHandleInfo的辅助类，希望嵌套在MethodHandleInfo中但必须是非公共的。
 */
/*non-public*/
final
class InfoFromMemberName implements MethodHandleInfo {
    private final MemberName member;
    private final int referenceKind;

    InfoFromMemberName(Lookup lookup, MemberName member, byte referenceKind) {
        assert(member.isResolved() || member.isMethodHandleInvoke());
        assert(member.referenceKindIsConsistentWith(referenceKind));
        this.member = member;
        this.referenceKind = referenceKind;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return member.getDeclaringClass();
    }

    @Override
    public String getName() {
        return member.getName();
    }

    @Override
    public MethodType getMethodType() {
        return member.getMethodOrFieldType();
    }

    @Override
    public int getModifiers() {
        return member.getModifiers();
    }

    @Override
    public int getReferenceKind() {
        return referenceKind;
    }

    @Override
    public String toString() {
        return MethodHandleInfo.toString(getReferenceKind(), getDeclaringClass(), getName(), getMethodType());
    }

    @Override
    public <T extends Member> T reflectAs(Class<T> expected, Lookup lookup) {
        if (member.isMethodHandleInvoke() && !member.isVarargs()) {
            // 此成员是签名多态方法的一个实例，无法反射
            // 方法句柄调用者可以有两种形式：
            // 一个通用占位符（存在于源代码中，且是可变参数）
            // 和一个签名多态实例（合成且不是可变参数）。
            // 更多信息请参阅 {@link MethodHandleNatives#linkMethod} 的注释。
            throw new IllegalArgumentException("无法反射签名多态方法");
        }
        Member mem = AccessController.doPrivileged(new PrivilegedAction<Member>() {
                public Member run() {
                    try {
                        return reflectUnchecked();
                    } catch (ReflectiveOperationException ex) {
                        throw new IllegalArgumentException(ex);
                    }
                }
            });
        try {
            Class<?> defc = getDeclaringClass();
            byte refKind = (byte) getReferenceKind();
            lookup.checkAccess(refKind, defc, convertToMemberName(refKind, mem));
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        }
        return expected.cast(mem);
    }

    private Member reflectUnchecked() throws ReflectiveOperationException {
        byte refKind = (byte) getReferenceKind();
        Class<?> defc = getDeclaringClass();
        boolean isPublic = Modifier.isPublic(getModifiers());
        if (MethodHandleNatives.refKindIsMethod(refKind)) {
            if (isPublic)
                return defc.getMethod(getName(), getMethodType().parameterArray());
            else
                return defc.getDeclaredMethod(getName(), getMethodType().parameterArray());
        } else if (MethodHandleNatives.refKindIsConstructor(refKind)) {
            if (isPublic)
                return defc.getConstructor(getMethodType().parameterArray());
            else
                return defc.getDeclaredConstructor(getMethodType().parameterArray());
        } else if (MethodHandleNatives.refKindIsField(refKind)) {
            if (isPublic)
                return defc.getField(getName());
            else
                return defc.getDeclaredField(getName());
        } else {
            throw new IllegalArgumentException("referenceKind="+refKind);
        }
    }

    private static MemberName convertToMemberName(byte refKind, Member mem) throws IllegalAccessException {
        if (mem instanceof Method) {
            boolean wantSpecial = (refKind == REF_invokeSpecial);
            return new MemberName((Method) mem, wantSpecial);
        } else if (mem instanceof Constructor) {
            return new MemberName((Constructor) mem);
        } else if (mem instanceof Field) {
            boolean isSetter = (refKind == REF_putField || refKind == REF_putStatic);
            return new MemberName((Field) mem, isSetter);
        }
        throw new InternalError(mem.getClass().getName());
    }
}
