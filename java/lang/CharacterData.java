/*
 * 版权所有 (c) 2006, 2011，Oracle 和/或其附属公司。保留所有权利。
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

package java.lang;

abstract class CharacterData {
    abstract int getProperties(int ch);
    abstract int getType(int ch);
    abstract boolean isWhitespace(int ch);
    abstract boolean isMirrored(int ch);
    abstract boolean isJavaIdentifierStart(int ch);
    abstract boolean isJavaIdentifierPart(int ch);
    abstract boolean isUnicodeIdentifierStart(int ch);
    abstract boolean isUnicodeIdentifierPart(int ch);
    abstract boolean isIdentifierIgnorable(int ch);
    abstract int toLowerCase(int ch);
    abstract int toUpperCase(int ch);
    abstract int toTitleCase(int ch);
    abstract int digit(int ch, int radix);
    abstract int getNumericValue(int ch);
    abstract byte getDirectionality(int ch);

    // 需要为 JSR204 实现
    int toUpperCaseEx(int ch) {
        return toUpperCase(ch);
    }

    char[] toUpperCaseCharArray(int ch) {
        return null;
    }

    boolean isOtherLowercase(int ch) {
        return false;
    }

    boolean isOtherUppercase(int ch) {
        return false;
    }

    boolean isOtherAlphabetic(int ch) {
        return false;
    }

    boolean isIdeographic(int ch) {
        return false;
    }

    // 字符 <= 0xff（基本拉丁文）由内部快速路径处理
    // 以避免初始化大型表。
    // 注意：由于范围复杂，某些访问器在负案例中的“快速路径”代码性能可能不佳。
    // 在优化表初始化后应重新评估。

    static final CharacterData of(int ch) {
        if (ch >>> 8 == 0) {     // 快速路径
            return CharacterDataLatin1.instance;
        } else {
            switch(ch >>> 16) {  // 平面 00-16
            case(0):
                return CharacterData00.instance;
            case(1):
                return CharacterData01.instance;
            case(2):
                return CharacterData02.instance;
            case(14):
                return CharacterData0E.instance;
            case(15):   // 私有使用
            case(16):   // 私有使用
                return CharacterDataPrivateUse.instance;
            default:
                return CharacterDataUndefined.instance;
            }
        }
    }
}