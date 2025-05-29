/*
 * 版权所有 (c) 2012, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang.invoke;

import java.security.*;
import java.lang.reflect.*;
import java.lang.invoke.MethodHandleNatives.Constants;
import java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodHandleStatics.*;

/*
 * MethodHandleInfo 的辅助类，希望嵌套在 MethodHandleInfo 中但必须是非公共的。
 */
/*非公共*/
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
            // 此成员是签名多态方法的一个实例，不能反射
            // 方法句柄调用者可以有两种形式：
            // 一个通用占位符（存在于源代码中，且是 varargs）
            // 和一个签名多态实例（合成的且不是 varargs）。
            // 更多信息请参见 {@link MethodHandleNatives#linkMethod} 的注释。
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
