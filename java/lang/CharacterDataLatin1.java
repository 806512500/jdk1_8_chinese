
// 该文件于 2022年6月16日星期四 15:54:14 UTC 从模板文件自动生成
/*
 * 版权所有 (c) 2002, 2013, Oracle 及/或其附属公司。保留所有权利。
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

/** CharacterData 类封装了 Java.lang.Character 中发现的大表。 */

class CharacterDataLatin1 extends CharacterData {

    /* 当前字符属性编码为 32 位，方式如下：
        1 位   镜像属性
        4 位   方向性属性
        9 位   用于转换大小写的带符号偏移
        1 位   如果为 1，则加上带符号偏移将字符转换为小写
        1 位   如果为 1，则减去带符号偏移将字符转换为大写
        1 位   如果为 1，则该字符有标题大小写等效形式（可能是其本身）
        3 位   0  可能不是标识符的一部分
                1  可忽略的控制字符；可以继续 Unicode 标识符或 Java 标识符
                2  可以继续 Java 标识符但不能开始 Unicode 标识符（未使用）
                3  可以继续 Unicode 标识符或 Java 标识符
                4  是 Java 空白字符
                5  可以开始或继续 Java 标识符；
                   可以继续但不能开始 Unicode 标识符（下划线）
                6  可以开始或继续 Java 标识符但不能开始 Unicode 标识符（$）
                7  可以开始或继续 Unicode 标识符或 Java 标识符
                因此：
                   5, 6, 7 可以开始 Java 标识符
                   1, 2, 3, 5, 6, 7 可以继续 Java 标识符
                   7 可以开始 Unicode 标识符
                   1, 3, 5, 7 可以继续 Unicode 标识符
                   1 在标识符内可忽略
                   4 是 Java 空白
        2 位   0  该字符没有数字属性
                1  将数字偏移量加到字符代码上，然后
                   与 0x1F 进行按位与运算将产生所需的数字值
                2  该字符有一个“奇怪”的数字值
                3  Java 超十进制数字：将数字偏移量加到
                   字符代码上，然后与 0x1F 进行按位与运算，然后加 10
                   将产生所需的数字值
        5 位   数字偏移量
        5 位   字符类型

        字符属性的编码随时可能更改。
     */

    int getProperties(int ch) {
        char offset = (char)ch;
        int props = A[offset];
        return props;
    }

    int getPropertiesEx(int ch) {
        char offset = (char)ch;
        int props = B[offset];
        return props;
    }

    boolean isOtherLowercase(int ch) {
        int props = getPropertiesEx(ch);
        return (props & 0x0001) != 0;
    }

    boolean isOtherUppercase(int ch) {
        int props = getPropertiesEx(ch);
        return (props & 0x0002) != 0;
    }

    boolean isOtherAlphabetic(int ch) {
        int props = getPropertiesEx(ch);
        return (props & 0x0004) != 0;
    }

    boolean isIdeographic(int ch) {
        int props = getPropertiesEx(ch);
        return (props & 0x0010) != 0;
    }

    int getType(int ch) {
        int props = getProperties(ch);
        return (props & 0x1F);
    }

    boolean isJavaIdentifierStart(int ch) {
        int props = getProperties(ch);
        return ((props & 0x00007000) >= 0x00005000);
    }

    boolean isJavaIdentifierPart(int ch) {
        int props = getProperties(ch);
        return ((props & 0x00003000) != 0);
    }

    boolean isUnicodeIdentifierStart(int ch) {
        int props = getProperties(ch);
        return ((props & 0x00007000) == 0x00007000);
    }

    boolean isUnicodeIdentifierPart(int ch) {
        int props = getProperties(ch);
        return ((props & 0x00001000) != 0);
    }

    boolean isIdentifierIgnorable(int ch) {
        int props = getProperties(ch);
        return ((props & 0x00007000) == 0x00001000);
    }

    int toLowerCase(int ch) {
        int mapChar = ch;
        int val = getProperties(ch);

        if (((val & 0x00020000) != 0) && 
                ((val & 0x07FC0000) != 0x07FC0000)) { 
            int offset = val << 5 >> (5+18);
            mapChar = ch + offset;
        }
        return mapChar;
    }

    int toUpperCase(int ch) {
        int mapChar = ch;
        int val = getProperties(ch);

        if ((val & 0x00010000) != 0) {
            if ((val & 0x07FC0000) != 0x07FC0000) {
                int offset = val  << 5 >> (5+18);
                mapChar =  ch - offset;
            } else if (ch == 0x00B5) {
                mapChar = 0x039C;
            }
        }
        return mapChar;
    }

    int toTitleCase(int ch) {
        return toUpperCase(ch);
    }

    int digit(int ch, int radix) {
        int value = -1;
        if (radix >= Character.MIN_RADIX && radix <= Character.MAX_RADIX) {
            int val = getProperties(ch);
            int kind = val & 0x1F;
            if (kind == Character.DECIMAL_DIGIT_NUMBER) {
                value = ch + ((val & 0x3E0) >> 5) & 0x1F;
            }
            else if ((val & 0xC00) == 0x00000C00) {
                // Java 超十进制数字
                value = (ch + ((val & 0x3E0) >> 5) & 0x1F) + 10;
            }
        }
        return (value < radix) ? value : -1;
    }

    int getNumericValue(int ch) {
        int val = getProperties(ch);
        int retval = -1;

        switch (val & 0xC00) {
            default: // 不可能发生
            case (0x00000000):         // 非数字
                retval = -1;
                break;
            case (0x00000400):              // 简单数字
                retval = ch + ((val & 0x3E0) >> 5) & 0x1F;
                break;
            case (0x00000800)      :       // “奇怪”的数字
                 retval = -2; 
                 break;
            case (0x00000C00):           // Java 超十进制
                retval = (ch + ((val & 0x3E0) >> 5) & 0x1F) + 10;
                break;
        }
        return retval;
    }


                    boolean isWhitespace(int ch) {
        int props = getProperties(ch);
        return ((props & 0x00007000) == 0x00004000);
    }

    byte getDirectionality(int ch) {
        int val = getProperties(ch);
        byte directionality = (byte)((val & 0x78000000) >> 27);

        if (directionality == 0xF ) {
            directionality = -1;
        }
        return directionality;
    }

    boolean isMirrored(int ch) {
        int props = getProperties(ch);
        return ((props & 0x80000000) != 0);
    }

    int toUpperCaseEx(int ch) {
        int mapChar = ch;
        int val = getProperties(ch);

        if ((val & 0x00010000) != 0) {
            if ((val & 0x07FC0000) != 0x07FC0000) {
                int offset = val  << 5 >> (5+18);
                mapChar =  ch - offset;
            }
            else {
                switch(ch) {
                    // map overflow characters
                    case 0x00B5 : mapChar = 0x039C; break;
                    default       : mapChar = Character.ERROR; break;
                }
            }
        }
        return mapChar;
    }

    static char[] sharpsMap = new char[] {'S', 'S'};

    char[] toUpperCaseCharArray(int ch) {
        char[] upperMap = {(char)ch};
        if (ch == 0x00DF) {
            upperMap = sharpsMap;
        }
        return upperMap;
    }

    static final CharacterDataLatin1 instance = new CharacterDataLatin1();
    private CharacterDataLatin1() {};

    // The following tables and code generated using:
  // java GenerateCharacter -template c:/jenkins/workspace/8-2-build-windows-x64-cygwin/jdk8u341/2692/jdk/make/data/characterdata/CharacterDataLatin1.java.template -spec c:/jenkins/workspace/8-2-build-windows-x64-cygwin/jdk8u341/2692/jdk/make/data/unicodedata/UnicodeData.txt -specialcasing c:/jenkins/workspace/8-2-build-windows-x64-cygwin/jdk8u341/2692/jdk/make/data/unicodedata/SpecialCasing.txt -proplist c:/jenkins/workspace/8-2-build-windows-x64-cygwin/jdk8u341/2692/jdk/make/data/unicodedata/PropList.txt -o c:/jenkins/workspace/8-2-build-windows-x64-cygwin/jdk8u341/2692/build/windows-x64/jdk/gensrc/java/lang/CharacterDataLatin1.java -string -usecharforbyte -latin1 8
  // The A table has 256 entries for a total of 1024 bytes.

  static final int A[] = new int[256];
  static final String A_DATA =
    "\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800"+
    "\u100F\u4800\u100F\u4800\u100F\u5800\u400F\u5000\u400F\u5800\u400F\u6000\u400F"+
    "\u5000\u400F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800"+
    "\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F"+
    "\u4800\u100F\u4800\u100F\u5000\u400F\u5000\u400F\u5000\u400F\u5800\u400F\u6000"+
    "\u400C\u6800\030\u6800\030\u2800\030\u2800\u601A\u2800\030\u6800\030\u6800"+
    "\030\uE800\025\uE800\026\u6800\030\u2000\031\u3800\030\u2000\024\u3800\030"+
    "\u3800\030\u1800\u3609\u1800\u3609\u1800\u3609\u1800\u3609\u1800\u3609\u1800"+
    "\u3609\u1800\u3609\u1800\u3609\u1800\u3609\u1800\u3609\u3800\030\u6800\030"+
    "\uE800\031\u6800\031\uE800\031\u6800\030\u6800\030\202\u7FE1\202\u7FE1\202"+
    "\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1"+
    "\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202"+
    "\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1"+
    "\202\u7FE1\uE800\025\u6800\030\uE800\026\u6800\033\u6800\u5017\u6800\033\201"+
    "\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2"+
    "\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201"+
    "\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2"+
    "\201\u7FE2\201\u7FE2\201\u7FE2\uE800\025\u6800\031\uE800\026\u6800\031\u4800"+
    "\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u5000\u100F"+
    "\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800"+
    "\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F"+
    "\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800"+
    "\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F"+
    "\u3800\014\u6800\030\u2800\u601A\u2800\u601A\u2800\u601A\u2800\u601A\u6800"+
    "\034\u6800\030\u6800\033\u6800\034\000\u7005\uE800\035\u6800\031\u4800\u1010"+
    "\u6800\034\u6800\033\u2800\034\u2800\031\u1800\u060B\u1800\u060B\u6800\033"+
    "\u07FD\u7002\u6800\030\u6800\030\u6800\033\u1800\u050B\000\u7005\uE800\036"+
    "\u6800\u080B\u6800\u080B\u6800\u080B\u6800\030\202\u7001\202\u7001\202\u7001"+
    "\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001\202"+
    "\u7001\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001"+
    "\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001\u6800\031\202\u7001\202"+
    "\u7001\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001\u07FD\u7002\201\u7002"+
    "\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201"+
    "\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002"+
    "\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\u6800\031\201"+
    "\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\u061D\u7002";

  // The B table has 256 entries for a total of 512 bytes.

  static final char B[] = (
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000").toCharArray();

                  // 总的来说，字符属性表需要 1024 字节。

    static {
                { // 以下代码由 GenerateCharacter 自动生成：
            char[] data = A_DATA.toCharArray();
            assert (data.length == (256 * 2));
            int i = 0, j = 0;
            while (i < (256 * 2)) {
                int entry = data[i++] << 16;
                A[j++] = entry | data[i++];
            }
        }

    }        
}
